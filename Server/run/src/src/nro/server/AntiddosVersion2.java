package nro.server;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class AntiddosVersion2 {

    public static JFrame frame;
    public static JTable tableBlocked;
    public static DefaultTableModel tableModel;
    public static JTextArea logArea;

    public static Set<String> blockedIPs = ConcurrentHashMap.newKeySet();
    public static Set<String> whiteList = ConcurrentHashMap.newKeySet();
    public static Map<String, Integer> ipHitCount = new ConcurrentHashMap<>();
    public static Map<String, Long> ipLastSeen = new ConcurrentHashMap<>();

    public static boolean lockdownMode = false;
    public static boolean firewallOn = false;
    public static boolean autoScanEnabled = false;
    public static boolean ddosDetected = false;
    public static volatile boolean running = true;

    public static long lastAttackTime = 0;
    public static final int RATE_LIMIT_PPS = 300;
    public static final int NGUONG_TOAN_MANG = 10000;
    public static final long CLEANUP_INTERVAL = 10;
    public static final long AUTO_DISABLE_SECONDS = 60;

    public static final AtomicLong totalBytes = new AtomicLong(0);
    private static ScheduledFuture<?> autoScanFuture;

    public static void main(String[] args) {
        loadState();
        SwingUtilities.invokeLater(AntiddosVersion2::initGUI);
        startPacketCapture();
        scheduleCleanup();
        startAPIServer(12345, "nro_siin");
    }

    public static void initGUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ignored) {}
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.okButtonText", "Đồng ý");

        frame = new JFrame("");
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopAll();
            }
        });

        tableModel = new DefaultTableModel(new String[]{"IP", "Lý do", "Thời gian"}, 0);
        tableBlocked = new JTable(tableModel);
        JScrollPane scrollBlocked = new JScrollPane(tableBlocked);
        scrollBlocked.setBorder(new TitledBorder("Danh sách IP bị chặn"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(new TitledBorder("Nhật ký hệ thống"));

        JButton btnAutoScan = new JButton("Tự động quét: Tắt");
        btnAutoScan.addActionListener(e -> {
            autoScanEnabled = !autoScanEnabled;
            toggleAutoScan();
            btnAutoScan.setText("Tự động quét: " + (autoScanEnabled ? "Bật" : "Tắt"));
        });

        JButton btnLockdown = new JButton("Lockdown: Tắt");
        btnLockdown.addActionListener(e -> {
            if (!lockdownMode) {
                enableLockdown();
            } else {
                disableLockdown();
            }
            btnLockdown.setText("Lockdown: " + (lockdownMode ? "Bật" : "Tắt"));
        });

        JButton btnUnblockAll = new JButton("Gỡ tất cả IP");
        btnUnblockAll.addActionListener(e -> unblockAll());

        JPanel panelTop = new JPanel(new FlowLayout());
        panelTop.add(btnAutoScan);
        panelTop.add(btnLockdown);
        panelTop.add(btnUnblockAll);

        JPanel root = new JPanel(new BorderLayout());
        root.add(panelTop, BorderLayout.NORTH);
        root.add(scrollBlocked, BorderLayout.CENTER);
        root.add(scrollLog, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    public static void scanNow() {
        Threading.EXECUTOR.execute(() -> {
            logConsole("Đang quét kết nối netstat...");
            try {
                Process p = Runtime.getRuntime().exec("netstat -n");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    Map<String, Integer> ipMap = new HashMap<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.matches(".*:(80|443|3389|14445)\\b.*")) {
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length >= 3) {
                                String ip = parts[2].split(":")[0];
                                if (isValidIp(ip)) {
                                    ipMap.merge(ip, 1, Integer::sum);
                                }
                            }
                        }
                    }
                    ipMap.forEach((ip, count) -> {
                        if (count > RATE_LIMIT_PPS) {
                            blockIP(ip, "Netstat > " + RATE_LIMIT_PPS);
                        }
                    });
                }
            } catch (IOException e) {
                logConsole("Lỗi netstat: " + e.getMessage());
            }
        });
    }

    public static void startPacketCapture() {
        Threading.EXECUTOR.execute(() -> {
            try {
                List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();
                if (interfaces == null || interfaces.isEmpty()) {
                    logConsole("Không tìm thấy card mạng!");
                    return;
                }
                PcapNetworkInterface nif = interfaces.get(0);
                logConsole("Bắt packet trên card: " + nif.getName());
                PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

                while (running) {
                    try {
                        Packet packet = handle.getNextPacketEx();
                        IpV4Packet ip = packet.get(IpV4Packet.class);
                        if (ip != null) {
                            String src = ip.getHeader().getSrcAddr().getHostAddress();
                            if (isValidIp(src)) {
                                ipHitCount.merge(src, 1, Integer::sum);
                                ipLastSeen.put(src, System.currentTimeMillis());
                                totalBytes.addAndGet(packet.length());

                                if (ipHitCount.get(src) > RATE_LIMIT_PPS) {
                                    blockIP(src, "PPS > " + RATE_LIMIT_PPS);
                                }

                                long totalPackets = ipHitCount.values().stream().mapToLong(Integer::longValue).sum();
                                if (totalPackets > NGUONG_TOAN_MANG && !lockdownMode) {
                                    enableLockdown();
                                }
                            }
                        }
                    } catch (TimeoutException ignored) {}
                }
            } catch (EOFException | NotOpenException | PcapNativeException e) {
                logConsole("Lỗi PacketCapture: " + e.getMessage());
            }
        });
    }

    public static void blockIP(String ip, String reason) {
        if (whiteList.contains(ip)) {
            return;
        }

        if (blockedIPs.add(ip)) {
            tableModel.addRow(new Object[]{ip, reason, now()});
            ddosDetected = true;
            lastAttackTime = System.currentTimeMillis();

            if (!firewallOn) {
                turnOnFirewall();
            }

            logConsole("CHẶN: " + ip + " (" + reason + ")");
            try {
                if (isWindows()) {
                    Runtime.getRuntime().exec("netsh advfirewall firewall add rule name=\"Block_" + ip + "\" dir=in action=block remoteip=" + ip);
                } else {
                    Runtime.getRuntime().exec("iptables -A INPUT -s " + ip + " -j DROP");
                }
            } catch (IOException e) {
                logConsole("Lỗi chặn IP: " + e.getMessage());
            }
            saveState();
        }
    }

    public static void unblockAll() {
        for (String ip : new HashSet<>(blockedIPs)) {
            unblockIP(ip);
        }
    }

    public static void unblockIP(String ip) {
        blockedIPs.remove(ip);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(ip)) {
                tableModel.removeRow(i);
                break;
            }
        }
        logConsole("BỎ CHẶN: " + ip);
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("netsh advfirewall firewall delete rule name=\"Block_" + ip + "\"");
            } else {
                Runtime.getRuntime().exec("iptables -D INPUT -s " + ip + " -j DROP");
            }
        } catch (IOException e) {
            logConsole("Lỗi bỏ chặn IP: " + e.getMessage());
        }
        saveState();
    }

    public static void enableLockdown() {
        lockdownMode = true;
        logConsole("LOCKDOWN: CHẶN TOÀN BỘ!");
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("netsh advfirewall set allprofiles firewallpolicy blockinbound,blockoutbound");
            } else {
                Runtime.getRuntime().exec("iptables -P INPUT DROP");
                Runtime.getRuntime().exec("iptables -P OUTPUT DROP");
            }
        } catch (IOException e) {
            logConsole("Lỗi Lockdown: " + e.getMessage());
        }
    }

    public static void disableLockdown() {
        lockdownMode = false;
        logConsole("TẮT LOCKDOWN");
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("netsh advfirewall set allprofiles firewallpolicy allowinbound,allowoutbound");
            } else {
                Runtime.getRuntime().exec("iptables -P INPUT ACCEPT");
                Runtime.getRuntime().exec("iptables -P OUTPUT ACCEPT");
            }
        } catch (IOException e) {
            logConsole("Lỗi Unlock: " + e.getMessage());
        }
    }

    public static void turnOnFirewall() {
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("netsh advfirewall set allprofiles state on");
            }
            firewallOn = true;
            logConsole("Bật Firewall");
        } catch (IOException e) {
            logConsole("Lỗi bật Firewall: " + e.getMessage());
        }
    }

    public static void scheduleCleanup() {
        Threading.SCHEDULER.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            ipHitCount.keySet().removeIf(ip -> now - ipLastSeen.getOrDefault(ip, now) > 60000);

            if (lockdownMode && ipHitCount.size() < NGUONG_TOAN_MANG / 2) {
                disableLockdown();
            }

            if (ddosDetected && System.currentTimeMillis() - lastAttackTime > AUTO_DISABLE_SECONDS * 1000L) {
                ddosDetected = false;
                firewallOn = false;
                logConsole("Tắt Firewall sau khi hết tấn công");
            }
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.SECONDS);
    }

    public static void toggleAutoScan() {
        if (autoScanEnabled) {
            if (autoScanFuture == null || autoScanFuture.isCancelled()) {
                autoScanFuture = Threading.SCHEDULER.scheduleAtFixedRate(AntiddosVersion2::scanNow, 0, 10, TimeUnit.SECONDS);
            }
        } else {
            if (autoScanFuture != null) {
                autoScanFuture.cancel(true);
            }
        }
    }

    public static void startAPIServer(int port, String token) {
        Threading.EXECUTOR.execute(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

                server.createContext("/status", ex -> sendJson(ex, Map.of(
                        "blocked", blockedIPs.size(),
                        "firewallOn", firewallOn,
                        "lockdown", lockdownMode
                ), 401));

                server.createContext("/blocked", ex -> sendJson(ex, blockedIPs, 401));

                server.createContext("/block", ex -> {
                    if (!auth(ex, token)) {
                        return;
                    }
                    String ip = getParam(ex, "ip");
                    if (ip != null) {
                        blockIP(ip, "API");
                    }
                    sendJson(ex, Map.of("msg", "blocked " + ip), 401);
                });

                server.createContext("/unblock", ex -> {
                    if (!auth(ex, token)) {
                        return;
                    }
                    String ip = getParam(ex, "ip");
                    if (ip != null) {
                        unblockIP(ip);
                    }
                    sendJson(ex, Map.of("msg", "unblocked " + ip), 401);
                });

                server.start();
                logConsole("API đang chạy tại cổng " + port);
            } catch (IOException e) {
                logConsole("Lỗi API: " + e.getMessage());
            }
        });
    }

    public static boolean isValidIp(String ip) {
        return ip.matches("^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}$")
                && !(ip.startsWith("192.168") || ip.startsWith("127.") || ip.startsWith("10."));
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static void logConsole(String msg) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + time + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static String now() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static void saveState() {
        try {
            Files.writeString(Paths.get("blocked.json"), new Gson().toJson(blockedIPs));
            Files.writeString(Paths.get("whitelist.json"), new Gson().toJson(whiteList));
        } catch (IOException ignored) {}
    }

    @SuppressWarnings("unchecked")
    public static void loadState() {
        try {
            if (Files.exists(Paths.get("blocked.json"))) {
                blockedIPs.addAll(new Gson().fromJson(Files.readString(Paths.get("blocked.json")), Set.class));
            }
            if (Files.exists(Paths.get("whitelist.json"))) {
                whiteList.addAll(new Gson().fromJson(Files.readString(Paths.get("whitelist.json")), Set.class));
            }
        } catch (IOException ignored) {}
    }

    public static boolean auth(HttpExchange ex, String token) throws IOException {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.equals("Bearer " + token)) {
            sendJson(ex, Map.of("error", "Unauthorized"), 401);
            return false;
        }
        return true;
    }

    public static void sendJson(HttpExchange ex, Object data, int par) throws IOException {
        String json = new Gson().toJson(data);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(200, json.getBytes().length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(json.getBytes());
        }
    }

    public static String getParam(HttpExchange ex, String key) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) {
            return null;
        }

        for (String p : query.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }

    public static void stopAll() {
        running = false;
        if (autoScanFuture != null) {
            autoScanFuture.cancel(true);
        }
        Threading.SCHEDULER.shutdownNow();
        Threading.EXECUTOR.shutdownNow();
        logConsole("Đã dừng toàn bộ thread");
    }
}
