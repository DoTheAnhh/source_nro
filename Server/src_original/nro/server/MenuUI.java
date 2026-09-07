package nro.server;

import QuanLiBoss.Boss;
import QuanLiBoss.BossID;
import QuanLiBoss.BossStatus;
import QuanLiBoss.Manager.BossManager;
import nro.services.PlayerService;
import nro.services.Service;
import nro.services.TaskService;
import Utils.Logger;
import Utils.Util;
import MaiTienDungManager.MaiTienDungManager;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.google.gson.Gson;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import event.EventManager;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import jbcd.ConnectDB;
import jbcd.dao.ShopDAO;
import jbcd.data.DatabaseUpdater;
import models.Item.Item;
import models.Item.ItemOption;
import models.Item.ItemService;
import network.interfaces.ISession;
import network.session.SessionManager;
import nro.consignmentstore.ConsignShopManager;
import nro.giftcode.GiftCode;
import nro.giftcode.GiftCodeManager;
import nro.inventory.InventoryService;
import nro.player.Player;
import nro.server.Proxy.ProxyManager;
import nro.task.TaskMain;
import nro.template.NpcTemplate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

public class MenuUI extends JFrame {

    private JLabel lblThreadCount, lblPlayerCount, lblSessionCount, lblStatus, lblCountdown, lblInfo;
    private JLabel lblCpuUsage, lblRamUsage, lblBossStatus;
    private JLabel lblUptime;
    private JLabel lblConsignItemsCount;
    private JLabel lblGiftcodeInfo;
    private JComboBox<Integer> cbHour, cbMinute, cbSecond;
    private JCheckBox chkAutoRestart;
    private JToggleButton btnToggleAutoSave;
    private final AtomicBoolean isAutoSaveEnabled = new AtomicBoolean(true);
    private ScheduledExecutorService scheduler;
    private OperatingSystemMXBean osBean;
    private final AtomicBoolean maintenanceScheduled = new AtomicBoolean(false);
    private ScheduledFuture<?> activeMaintenanceJobFuture = null;
    private ScheduledFuture<?> activeCountdownDisplayFuture = null;
    private ScheduledFuture<?> immediateMaintenanceCountdownFuture = null;
    public static volatile boolean REQUEST_AUTO_RESTART = false;
    private JTable playerTable;
    private DefaultTableModel playerTableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private final Instant serverStartTime;
    private JTable tableBlocked;
    private DefaultTableModel tableModelBlocked;
    private JTextArea logAreaDdos;
    private final Set<String> blockedIPs = ConcurrentHashMap.newKeySet();
    private final Set<String> whiteList = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> ipHitCount = new ConcurrentHashMap<>();
    private final Map<String, Long> ipLastSeen = new ConcurrentHashMap<>();
    private boolean lockdownMode = false;
    private boolean autoScanEnabled = false;
    private JToggleButton btnLockdownToggle;
    private volatile boolean ddosRunning = true;
    private static final int RATE_LIMIT_PPS = 300;
    private static final int NGUONG_TOAN_MANG = 10000;
    private ScheduledExecutorService ddosScheduler;
    private ScheduledFuture<?> autoScanFuture;

//    public MenuUI() {
//        super("Server Manager");
//        FlatDarculaLaf.setup();
//        UIManager.put("Button.background", new Color(255, 105, 180)); // hồng
//        UIManager.put("Button.foreground", Color.WHITE);
//        UIManager.put("Panel.background", new Color(255, 182, 193));
//        UIManager.put("Label.foreground", Color.BLACK);
//        UIManager.put("TextField.background", new Color(255, 228, 225));
//        UIManager.put("TextField.foreground", Color.BLACK);
//        initUI();
//        initLogic();
//        initAntiDdosSystem();
//        startServerProcesses();
//        this.serverStartTime = Instant.now();
//    }
    public MenuUI() {
    super("Server Manager");

    FlatDarculaLaf.setup();

    UIManager.put("Button.background", new Color(33, 150, 243));   // xanh accent
    UIManager.put("Button.foreground", Color.WHITE);

    UIManager.put("Panel.background", new Color(24, 26, 32));      // nền dark xịn
    UIManager.put("Label.foreground", new Color(220, 220, 220));   // chữ xám sáng

    UIManager.put("TextField.background", new Color(36, 38, 45));  // ô nhập tối
    UIManager.put("TextField.foreground", Color.WHITE);
    UIManager.put("TextField.caretForeground", Color.WHITE);

    UIManager.put("Table.background", new Color(28, 30, 36));
    UIManager.put("Table.foreground", new Color(230, 230, 230));
    UIManager.put("Table.selectionBackground", new Color(33, 150, 243));
    UIManager.put("Table.selectionForeground", Color.WHITE);

    UIManager.put("TabbedPane.background", new Color(24, 26, 32));
    UIManager.put("TabbedPane.foreground", new Color(220, 220, 220));

    UIManager.put("TitledBorder.titleColor", new Color(180, 180, 180));

    initUI();
    initLogic();
    initAntiDdosSystem();
    startServerProcesses();

    this.serverStartTime = Instant.now();
}


    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Lỗi cài đặt Look and Feel: " + e.getMessage());
        }
    }

    private void initUI() {
        setTitle("Bảng Điều Khiển Server");
        try {
            setIconImage(new ImageIcon(getClass().getResource("/icons/server-icon.png")).getImage());
        } catch (Exception e) {
            System.err.println("Lỗi: Không tìm thấy file icon '/icons/server-icon.png'");
        }
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel mainPanel = createMainDashboardPanel();
        JPanel playerManagementPanel = createPlayerManagementPanel();
        JPanel antiDdosPanel = createAntiDdosPanel();
        JPanel proxyPanel = createProxyPanel();
        JPanel eventPanel = createEventPanel();
        JPanel dataPanel = createDataPanel();
        tabbedPane.addTab("Hệ Thống", mainPanel);
        tabbedPane.addTab("Manage Player", playerManagementPanel);
        tabbedPane.addTab("Chống DDoS", antiDdosPanel);
        tabbedPane.addTab("Firewall Manager", proxyPanel);
        tabbedPane.addTab("Event", eventPanel);
        tabbedPane.addTab("Dữ Liệu", dataPanel);
        tabbedPane.addTab("Shop Manager", new OptionUI());
        setContentPane(tabbedPane);
        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    private JPanel createEventPanel() {
        PinkHeartsPanel panel = new PinkHeartsPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Quản lý Sự Kiện", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Danh sách sự kiện
        String[] events = {
            "Không có sự kiện",
            "Sự kiện Tết",
            "Sự kiện Trung Thu",
            "Sự kiện Halloween",
            "Sự kiện Noel",
            "Sự kiện Vu Lan",
            "Sự kiện Quốc tế Phụ nữ",
            "Sự kiện Hùng Vương",
            "Sự kiện Black Friday",
            "Sự kiện Valentine",
            "Sự kiện Nạp Thẻ"
        };
        JComboBox<String> eventComboBox = new JComboBox<>(events);
        eventComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton applyButton = new JButton("Áp dụng sự kiện");
        applyButton.setFont(new Font("Arial", Font.BOLD, 14));
        applyButton.addActionListener(e -> {
            int selectedIndex = eventComboBox.getSelectedIndex();

            try {
                EventManager.gI().setCurrentEvent(selectedIndex);

                JOptionPane.showMessageDialog(panel,
                        "Đã áp dụng: " + eventComboBox.getSelectedItem(),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Lỗi khi áp dụng sự kiện!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        centerPanel.add(eventComboBox);
        centerPanel.add(applyButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDataPanel() {
        PinkHeartsPanel panel = new PinkHeartsPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Dữ Liệu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Danh sách sự kiện
        String[] data = {
            "int",
            "long"
        };
        JComboBox<String> dataComboBox = new JComboBox<>(data);
        dataComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton applyButton = new JButton("Chuyển Đổi");
        applyButton.setFont(new Font("Arial", Font.BOLD, 14));
        applyButton.addActionListener(e -> {
            int selectedIndex = dataComboBox.getSelectedIndex();

            try {
                setCurrentData(selectedIndex);

                JOptionPane.showMessageDialog(panel,
                        "Đã chuyển qua : " + dataComboBox.getSelectedItem(),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Lỗi khi chuyển dữ liệu!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        centerPanel.add(dataComboBox);
        centerPanel.add(applyButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void setCurrentData(int id) {
        switch (id) {
            case 0:
                Manager.readInt = true;
                Utils.Logger.success("Int đã được khởi chạy!\n");
                break;
            case 1:
                Manager.readInt = false;
                Utils.Logger.success("Long đã được khởi chạy!\n");
                break;
            default:
                break;
        }
    }

    private JPanel createProxyPanel() {
        PinkHeartsPanel panel = new PinkHeartsPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(new TitledBorder("Add New Configuration"));

        JTextField txtTargetIp = new JTextField("127.0.0.1");
        JTextField txtTargetPort = new JTextField("14445");
        JTextField txtListenPort = new JTextField("24445");

        formPanel.add(new JLabel("Game IP:"));
        formPanel.add(txtTargetIp);
        formPanel.add(new JLabel("Game Port:"));
        formPanel.add(txtTargetPort);
        formPanel.add(new JLabel("Firewall Port:"));
        formPanel.add(txtListenPort);

        JButton btnAddProxy = new JButton("SET UP FIREWALL");
        formPanel.add(new JLabel());
        formPanel.add(btnAddProxy);

        panel.add(formPanel, BorderLayout.NORTH);
        DefaultTableModel proxyTableModel = new DefaultTableModel(new String[]{"Game IP", "Game Port", "Firewall Port", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable proxyTable = new JTable(proxyTableModel);
        JScrollPane scrollPane = new JScrollPane(proxyTable);
        scrollPane.setBorder(new TitledBorder("Your Configurations"));

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnStopAll = new JButton("Stop All Firewall");
        btnStopAll.setBackground(new Color(220, 53, 69));
        btnStopAll.setForeground(Color.WHITE);
        btnStopAll.setToolTipText("Dừng tất cả các cấu hình firewall đang chạy.");

        btnStopAll.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn dừng tất cả các firewall đang kết nối?",
                    "Xác nhận Dừng Toàn Bộ",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                while (proxyTableModel.getRowCount() > 0) {
                    // FIX: Lấy Listen Port từ cột 2 (Firewall Port), không phải cột 0 (Game IP).
                    // Cột 0 là String (IP), Cột 2 là Integer (Port).
                    int listenPort = (int) proxyTableModel.getValueAt(0, 2);
                    ProxyManager.getInstance().stopProxy(listenPort, proxyTableModel, 0);
                }
                JOptionPane.showMessageDialog(this, "Đã dừng thành công tất cả firewall.", "Hoàn Tất", JOptionPane.INFORMATION_MESSAGE);
                lblInfo.setText("Đã dừng tất cả kết nối firewall.");
            }
        });

        actionPanel.add(btnStopAll);
        panel.add(actionPanel, BorderLayout.SOUTH);

        btnAddProxy.addActionListener(e -> {
            try {
                String targetIp = txtTargetIp.getText().trim();
                int targetPort = Integer.parseInt(txtTargetPort.getText().trim());
                int listenPort = Integer.parseInt(txtListenPort.getText().trim());

                if (targetIp.isEmpty() || targetPort <= 0 || listenPort <= 0) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập thông tin hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = ProxyManager.getInstance().startProxy(targetIp, targetPort, listenPort, proxyTableModel);
                if (!success) {
                    JOptionPane.showMessageDialog(this, "Không thể bắt đầu port " + listenPort + " có thể đã được sử dụng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Port là số.", "Lỗi Định Dạng", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem stopMenuItem = new JMenuItem("Stop Firewall");
        stopMenuItem.addActionListener(e -> {
            int selectedRow = proxyTable.getSelectedRow();
            if (selectedRow != -1) {
                int listenPort = (int) proxyTableModel.getValueAt(selectedRow, 2);
                ProxyManager.getInstance().stopProxy(listenPort, proxyTableModel, selectedRow);
            }
        });
        popupMenu.add(stopMenuItem);
        proxyTable.setComponentPopupMenu(popupMenu);

        proxyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = proxyTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < proxyTable.getRowCount()) {
                    proxyTable.setRowSelectionInterval(row, row);
                }
            }
        });

        return panel;
    }

    private JPanel createAntiDdosPanel() {
        PinkHeartsPanel antiDdosPanel = new PinkHeartsPanel();
        antiDdosPanel.setLayout(new BorderLayout(10, 10));
        antiDdosPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        PinkHeartsPanel panelTop = new PinkHeartsPanel();
        panelTop.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelTop.setBorder(new TitledBorder("Điều Khiển"));
        JToggleButton btnAutoScan = new JToggleButton("Tự động quét Netstat: Bật");
        btnAutoScan.addActionListener(e -> {
            autoScanEnabled = btnAutoScan.isSelected();
            toggleAutoScan();
            btnAutoScan.setText("Tự động quét Netstat: " + (autoScanEnabled ? "Tắt" : "Bật"));
        });
        JToggleButton btnLockdown = new JToggleButton("Lockdown: Bật");
        btnLockdown.addActionListener(e -> {
            boolean shouldEnable = btnLockdown.isSelected();
            if (shouldEnable) {
                enableLockdown();
            } else {
                disableLockdown();
            }
            btnLockdown.setText("Lockdown: " + (lockdownMode ? "Tắt" : "Bật"));
            btnLockdown.setSelected(lockdownMode);
        });
        JButton btnUnblockAll = new JButton("Gỡ tất cả IP");
        btnUnblockAll.setBackground(new Color(220, 53, 69));
        btnUnblockAll.setForeground(Color.WHITE);
        btnUnblockAll.addActionListener(e -> unblockAllIps());
        panelTop.add(btnAutoScan);
        panelTop.add(btnLockdown);
        panelTop.add(btnUnblockAll);
        antiDdosPanel.add(panelTop, BorderLayout.NORTH);
        tableModelBlocked = new DefaultTableModel(new String[]{"IP", "Lý do", "Thời gian"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableBlocked = new JTable(tableModelBlocked);
        tableBlocked.setFillsViewportHeight(true);
        JScrollPane scrollBlocked = new JScrollPane(tableBlocked);
        scrollBlocked.setBorder(new TitledBorder("Danh sách IP bị chặn"));
        logAreaDdos = new JTextArea();
        logAreaDdos.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(logAreaDdos);
        scrollLog.setBorder(new TitledBorder("Nhật ký hệ thống Anti-DDoS"));
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBlocked, scrollLog);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.5);
        antiDdosPanel.add(splitPane, BorderLayout.CENTER);
        return antiDdosPanel;
    }

    private void initAntiDdosSystem() {
        ddosScheduler = Executors.newScheduledThreadPool(3);
        loadDdosState();
        startPacketCapture();
        scheduleCleanup();
    }

    private void logDdos(String msg) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            if (logAreaDdos.getDocument().getLength() > 20000) {
                logAreaDdos.setText("");
            }
            logAreaDdos.append("[" + time + "] " + msg + "\n");
            logAreaDdos.setCaretPosition(logAreaDdos.getDocument().getLength());
        });
    }

    private void startPacketCapture() {
        ddosScheduler.execute(() -> {
            try {
                var interfaces = Pcaps.findAllDevs();
                if (interfaces.isEmpty()) {
                    logDdos("LỖI: Không tìm thấy card mạng nào!");
                    return;
                }
                PcapNetworkInterface nif = interfaces.stream()
                        .filter(i -> !i.isLoopBack() && !i.getAddresses().isEmpty())
                        .findFirst()
                        .orElse(interfaces.get(0));
                logDdos("Bắt đầu bắt gói tin trên card: " + nif.getDescription() + " (" + nif.getName() + ")");
                try (PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10)) {
                    while (ddosRunning) {
                        try {
                            Packet packet = handle.getNextPacketEx();
                            IpV4Packet ipPacket = packet.get(IpV4Packet.class);
                            if (ipPacket != null) {
                                String srcAddr = ipPacket.getHeader().getSrcAddr().getHostAddress();
                                if (isValidIp(srcAddr) && !whiteList.contains(srcAddr)) {
                                    ipHitCount.merge(srcAddr, 1, Integer::sum);
                                    ipLastSeen.put(srcAddr, System.currentTimeMillis());
                                    if (ipHitCount.getOrDefault(srcAddr, 0) > RATE_LIMIT_PPS) {
                                        blockIP(srcAddr, "PPS > " + RATE_LIMIT_PPS);
                                    }
                                }
                            }
                            long totalPackets = ipHitCount.values().stream().mapToLong(Integer::longValue).sum();
                            if (totalPackets > NGUONG_TOAN_MANG && !lockdownMode) {
                                enableLockdown();
                            }
                        } catch (TimeoutException ignored) {
                        } catch (NotOpenException e) {
                            logDdos("LỖI: Handle đã bị đóng. Đang dừng bắt gói tin.");
                            break;
                        }
                    }
                }
                logDdos("Đã dừng bắt gói tin.");
            } catch (PcapNativeException | EOFException e) {
                logDdos("Lỗi Pcap: " + e.getMessage() + ". Vui lòng cài đặt Npcap/WinPcap và chạy với quyền Admin.");
            }
        });
    }

    private void scanNow() {
        ddosScheduler.execute(() -> {
            logDdos("Đang quét kết nối netstat...");
            try {
                Process p = Runtime.getRuntime().exec("netstat -n");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    Map<String, Integer> ipMap = new HashMap<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.matches(".*:(80|443|14445)\\s+.*")) {
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length >= 3) {
                                String foreignAddress = parts[2];
                                String ip = foreignAddress.split(":")[0];
                                if (isValidIp(ip) && !whiteList.contains(ip)) {
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
                logDdos("Lỗi khi chạy netstat: " + e.getMessage());
            }
        });
    }

    private void blockIP(String ip, String reason) {
        if (whiteList.contains(ip)) {
            logDdos("Bỏ qua IP trong whitelist: " + ip);
            return;
        }
        if (blockedIPs.add(ip)) {
            SwingUtilities.invokeLater(() -> tableModelBlocked.addRow(new Object[]{ip, reason, new SimpleDateFormat("HH:mm:ss").format(new Date())}));
            logDdos("CHẶN: " + ip + " (" + reason + ")");
            try {
                String command;
                if (isWindows()) {
                    command = "netsh advfirewall firewall add rule name=\"BlockDDoS_" + ip + "\" dir=in action=block remoteip=" + ip;
                } else {
                    command = "iptables -A INPUT -s " + ip + " -j DROP";
                }
                Runtime.getRuntime().exec(command);
                saveDdosState();
            } catch (IOException e) {
                logDdos("Lỗi khi thực thi lệnh chặn IP: " + e.getMessage());
            }
        }
    }

    private void unblockIP(String ip) {
        if (blockedIPs.remove(ip)) {
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < tableModelBlocked.getRowCount(); i++) {
                    if (tableModelBlocked.getValueAt(i, 0).equals(ip)) {
                        tableModelBlocked.removeRow(i);
                        break;
                    }
                }
            });
            logDdos("BỎ CHẶN: " + ip);
            try {
                String command;
                if (isWindows()) {
                    command = "netsh advfirewall firewall delete rule name=\"BlockDDoS_" + ip + "\"";
                } else {
                    command = "iptables -D INPUT -s " + ip + " -j DROP";
                }
                Runtime.getRuntime().exec(command);
                saveDdosState();
            } catch (IOException e) {
                logDdos("Lỗi khi thực thi lệnh gỡ chặn IP: " + e.getMessage());
            }
        }
    }

    private void unblockAllIps() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn gỡ chặn TẤT CẢ các địa chỉ IP?",
                "Xác nhận Gỡ Chặn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            for (String ip : new HashSet<>(blockedIPs)) {
                unblockIP(ip);
            }
            logDdos("Đã gỡ chặn tất cả các IP.");
        }
    }

    private void toggleAutoScan() {
        if (autoScanEnabled) {
            if (autoScanFuture == null || autoScanFuture.isCancelled()) {
                autoScanFuture = ddosScheduler.scheduleAtFixedRate(this::scanNow, 0, 30, TimeUnit.SECONDS);
                logDdos("Đã BẬT chế độ tự động quét Netstat.");
            }
        } else {
            if (autoScanFuture != null && !autoScanFuture.isCancelled()) {
                autoScanFuture.cancel(true);
                logDdos("Đã TẮT chế độ tự động quét Netstat.");
            }
        }
    }

    private void enableLockdown() {
        if (lockdownMode) {
            return;
        }
        logDdos("CẢNH BÁO: Đang kích hoạt chế độ LOCKDOWN...");
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("netsh advfirewall set allprofiles firewallpolicy blockinbound,allowoutbound");
            } else {
                Runtime.getRuntime().exec("iptables -P INPUT DROP");
            }
            lockdownMode = true;
            logDdos("Đã kích hoạt LOCKDOWN. Hầu hết kết nối sẽ bị chặn.");
        } catch (IOException e) {
            logDdos("Lỗi khi kích hoạt Lockdown: " + e.getMessage());
            lockdownMode = false;
        } finally {
            SwingUtilities.invokeLater(() -> {
                if (btnLockdownToggle != null) {
                    btnLockdownToggle.setText("Lockdown: " + (lockdownMode ? "Bật" : "Tắt"));
                    btnLockdownToggle.setSelected(lockdownMode);
                }
            });
        }
    }

    private void disableLockdown() {
        if (!lockdownMode) {
            return;
        }
        logDdos("Đang tắt chế độ LOCKDOWN...");
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("netsh advfirewall set allprofiles firewallpolicy allowinbound,allowoutbound");
            } else {
                Runtime.getRuntime().exec("iptables -P INPUT ACCEPT");
            }
            lockdownMode = false;
            logDdos("Đã tắt chế độ LOCKDOWN. Khôi phục chính sách tường lửa mặc định.");
        } catch (IOException e) {
            logDdos("Lỗi khi tắt Lockdown: " + e.getMessage());
            lockdownMode = true;
        } finally {
            SwingUtilities.invokeLater(() -> {
                if (btnLockdownToggle != null) {
                    btnLockdownToggle.setText("Lockdown: " + (lockdownMode ? "Bật" : "Tắt"));
                    btnLockdownToggle.setSelected(lockdownMode);
                }
            });
        }
    }

    private void scheduleCleanup() {
        ddosScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            ipHitCount.keySet().removeIf(ip -> now - ipLastSeen.getOrDefault(ip, now) > 60000);
            if (lockdownMode && ipHitCount.values().stream().mapToLong(Integer::longValue).sum() < (NGUONG_TOAN_MANG / 2)) {
                disableLockdown();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return !ip.startsWith("127.") && !ip.startsWith("192.168.") && !ip.startsWith("10.") && !ip.startsWith("172.")
                && !ip.startsWith("169.254.") && !ip.startsWith("224.") && !ip.equals("0.0.0.0") && !ip.equals("255.255.255.255");
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private void saveDdosState() {
        try {
            String blockedJson = new Gson().toJson(blockedIPs);
            Files.writeString(Paths.get("ddos_blocked.json"), blockedJson);
            String whitelistJson = new Gson().toJson(whiteList);
            Files.writeString(Paths.get("ddos_whitelist.json"), whitelistJson);
        } catch (IOException e) {
            logDdos("Lỗi khi lưu trạng thái DDoS: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadDdosState() {
        try {
            if (Files.exists(Paths.get("ddos_blocked.json"))) {
                String json = Files.readString(Paths.get("ddos_blocked.json"));
                Set<String> loadedIps = new Gson().fromJson(json, Set.class);
                if (loadedIps != null) {
                    loadedIps.forEach(ip -> blockIP(String.valueOf(ip), "Loaded from file"));
                }
            }
            if (Files.exists(Paths.get("ddos_whitelist.json"))) {
                String json = Files.readString(Paths.get("ddos_whitelist.json"));
                Set<String> loadedWhitelist = new Gson().fromJson(json, Set.class);
                if (loadedWhitelist != null) {
                    whiteList.addAll(loadedWhitelist);
                    logDdos("Đã tải " + whiteList.size() + " IP từ danh sách trắng.");
                }
            }
        } catch (IOException e) {
            logDdos("Lỗi khi tải trạng thái DDoS: " + e.getMessage());
        }
    }

    private JPanel createMainDashboardPanel() {
        PinkHeartsPanel mainPanel = new PinkHeartsPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        mainPanel.add(createHeaderStatusPanel(), gbc);
        gbc.gridy = 1;
        mainPanel.add(createActionPanel(), gbc);
        gbc.gridy = 2;
        mainPanel.add(createBossSummonPanel(), gbc);
        gbc.gridy = 3;
        mainPanel.add(createExpRatePanel(), gbc);
        gbc.gridy = 4;
        mainPanel.add(createGameStatsPanel(), gbc);
        gbc.gridy = 5;
        mainPanel.add(createSystemStatsPanel(), gbc);
        gbc.gridy = 6;
        mainPanel.add(createSchedulerPanel(), gbc);
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        mainPanel.add(Box.createVerticalGlue(), gbc);
        gbc.gridy = 8;
        gbc.weighty = 0;
        mainPanel.add(createFooterInfoPanel(), gbc);
        return mainPanel;
    }

    private static class BossSelectionItem {

        public final int id;
        public final String name;

        public BossSelectionItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private JPanel createBossSummonPanel() {
        JPanel summonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        summonPanel.setBorder(BorderFactory.createTitledBorder("Menu call boss"));
        summonPanel.add(new JLabel("Chọn Boss:"));
        JComboBox<BossSelectionItem> cbBossSelect = new JComboBox<>();
        try {
            Field[] fields = BossID.class.getFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                    String name = field.getName().replace("_", " ").toLowerCase();
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    int id = field.getInt(null);
                    cbBossSelect.addItem(new BossSelectionItem(id, name));
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
            Logger.error("Lỗi khi tải danh sách: " + e.getMessage());
            cbBossSelect.addItem(new BossSelectionItem(-1, "Lỗi tải danh sách"));
        }
        summonPanel.add(cbBossSelect);
        JButton btnSummonBoss = new JButton("Call boss");
        btnSummonBoss.addActionListener(e -> {
            BossSelectionItem selectedItem = (BossSelectionItem) cbBossSelect.getSelectedItem();
            if (selectedItem != null && selectedItem.id != -1) {
                summonSpecificBoss(selectedItem.id, selectedItem.name);
            }
        });
        summonPanel.add(btnSummonBoss);
        return summonPanel;
    }

    private void summonSpecificBoss(int bossId, String bossName) {
        try {
            if (BossManager.gI() == null) {
                JOptionPane.showMessageDialog(this, "BossManager chưa sẵn sàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Boss boss = BossManager.gI().createBoss(bossId);
            if (boss == null) {
                String errorMessage = "Call boss thất bại! Không thể tạo Boss: " + bossId;
                lblInfo.setText(errorMessage);
                JOptionPane.showMessageDialog(this, errorMessage, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boss.changeStatus(BossStatus.RESPAWN);
            String message = "Call boss successfully: " + bossName;
            lblInfo.setText(message);
            JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            Logger.error("Lỗi nghiêm trọng khi triệu hồi boss: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi triệu hồi boss. Xem console để biết thêm chi tiết.", "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createPlayerManagementPanel() {
        PinkHeartsPanel containerPanel = new PinkHeartsPanel();
        containerPanel.setLayout(new BorderLayout(10, 10));
        containerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        PinkHeartsPanel topPanel = new PinkHeartsPanel();
        topPanel.setLayout(new BorderLayout(20, 5));
        PinkHeartsPanel searchPanel = new PinkHeartsPanel();
        searchPanel.setLayout(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("Tìm kiếm:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        PinkHeartsPanel globalActionPanel = new PinkHeartsPanel();
        globalActionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton kickAllButton = new JButton("Kick Tất Cả");
        kickAllButton.setToolTipText("Kick tất cả người chơi đang online khỏi máy chủ.");
        kickAllButton.setBackground(new Color(220, 53, 69));
        kickAllButton.setForeground(Color.WHITE);
        kickAllButton.addActionListener(e -> kickAllPlayers());
        globalActionPanel.add(kickAllButton);
        topPanel.add(globalActionPanel, BorderLayout.EAST);
        containerPanel.add(topPanel, BorderLayout.NORTH);
        String[] columnNames = {"ID", "Tên Nhân Vật", "Sức Mạnh", "Nhiệm Vụ Hiện Tại"};
        playerTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        playerTable = new JTable(playerTableModel);
        sorter = new TableRowSorter<>(playerTableModel);
        playerTable.setRowSorter(sorter);
        playerTable.setRowHeight(24);
        playerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerTable.getColumnModel().getColumn(0).setMaxWidth(60);
        playerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        playerTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        playerTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        playerTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        playerTable.setDefaultRenderer(Object.class, (TableCellRenderer) new CustomPlayerTableCellRenderer());
        JScrollPane tableScrollPane = new JScrollPane(playerTable);
        containerPanel.add(tableScrollPane, BorderLayout.CENTER);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText();
                sorter.setRowFilter(text.trim().length() == 0 ? null : RowFilter.regexFilter("(?i)" + text, 1));
            }
        });
        createPlayerContextMenu();
        return containerPanel;
    }

    private static class CustomPlayerTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                int modelColumn = table.convertColumnIndexToModel(column);
                switch (modelColumn) {
                    case 0: {
                        setHorizontalAlignment(SwingConstants.CENTER);
                        setToolTipText(null);
                        break;
                    }
                    case 2: {
                        setHorizontalAlignment(SwingConstants.RIGHT);
                        setToolTipText(value.toString());
                        break;
                    }
                    default: {
                        setHorizontalAlignment(SwingConstants.LEFT);
                        setToolTipText(value.toString());
                        break;
                    }
                }
            }
            return this;
        }
    }

    private void createPlayerContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem giveItemAction = new JMenuItem("Gửi Vật Phẩm");
        JMenuItem buffVndAction = new JMenuItem("Buff VND");
        JMenuItem setTaskAction = new JMenuItem("Next Nhiệm Vụ");
        JMenuItem kickAction = new JMenuItem("Kick Người Chơi");
        JMenuItem banAction = new JMenuItem("Ban Người Chơi");
        banAction.setForeground(Color.RED);
        try {
            giveItemAction.setIcon(new ImageIcon(getClass().getResource("/icons/give-item.png")));
            buffVndAction.setIcon(new ImageIcon(getClass().getResource("/icons/money.png")));
            kickAction.setIcon(new ImageIcon(getClass().getResource("/icons/kick-player.png")));
            setTaskAction.setIcon(new ImageIcon(getClass().getResource("/icons/next-task.png")));
            banAction.setIcon(new ImageIcon(getClass().getResource("/icons/ban-player.png")));
        } catch (Exception e) {
            System.err.println("Fail load file icons");
        }
        giveItemAction.addActionListener(e -> {
            int selectedRow = playerTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = playerTable.convertRowIndexToModel(selectedRow);
                int playerId = (int) playerTableModel.getValueAt(modelRow, 0);
                openGiveItemDialog(playerId);
            }
        });
        buffVndAction.addActionListener(e -> {
            int selectedRow = playerTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = playerTable.convertRowIndexToModel(selectedRow);
                int playerId = (int) playerTableModel.getValueAt(modelRow, 0);
                openBuffVndDialogForPlayer(playerId);
            }
        });
        setTaskAction.addActionListener(e -> {
            int selectedRow = playerTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = playerTable.convertRowIndexToModel(selectedRow);
                int playerId = (int) playerTableModel.getValueAt(modelRow, 0);
                openSetTaskDialog(playerId);
            }
        });
        kickAction.addActionListener(e -> {
            int selectedRow = playerTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = playerTable.convertRowIndexToModel(selectedRow);
                int playerId = (int) playerTableModel.getValueAt(modelRow, 0);
                kickPlayer(playerId);
            }
        });
        banAction.addActionListener(e -> {
            int selectedRow = playerTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = playerTable.convertRowIndexToModel(selectedRow);
                int playerId = (int) playerTableModel.getValueAt(modelRow, 0);
                banPlayer(playerId);
            }
        });
        contextMenu.add(buffVndAction);
        contextMenu.add(giveItemAction);
        contextMenu.add(setTaskAction);
        contextMenu.add(kickAction);
        contextMenu.addSeparator();
        contextMenu.add(banAction);
        playerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = playerTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < playerTable.getRowCount()) {
                        playerTable.setRowSelectionInterval(row, row);
                        contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void openSetTaskDialog(int playerId) {
        Optional<Player> playerOpt = getPlayerById(playerId);
        if (playerOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Người chơi không online.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Player targetPlayer = playerOpt.get();
        JTextField taskIdField = new JTextField(5);
        JTextField subTaskIndexField = new JTextField(5);
        PinkHeartsPanel panel = new PinkHeartsPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(taskIdField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Index:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(subTaskIndexField, gbc);
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Next mission to player: " + targetPlayer.name,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int taskId = Integer.parseInt(taskIdField.getText());
                int subTaskIndex = Integer.parseInt(subTaskIndexField.getText());
                setPlayerTask(targetPlayer, taskId, subTaskIndex);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập ID và Index là số hợp lệ.", "Lỗi Định Dạng", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setPlayerTask(Player player, int taskId, int subTaskIndex) {
        try {
            TaskMain newTask = TaskService.gI().getTaskMainById(player, taskId);
            if (newTask.id != taskId) {
                JOptionPane.showMessageDialog(this, "ID Nhiệm vụ chính không tồn tại: " + taskId, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (subTaskIndex < 0 || subTaskIndex >= newTask.subTasks.size()) {
                JOptionPane.showMessageDialog(this,
                        "Index nhiệm vụ con không hợp lệ. Phải từ 0 đến " + (newTask.subTasks.size() - 1),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            newTask.index = (byte) subTaskIndex;
            newTask.subTasks.get(newTask.index).count = 0;
            player.playerTask.taskMain = newTask;
            TaskService.gI().sendTaskMain(player);
            lblInfo.setText("Đã next nhiệm vụ " + " Task id:" + taskId + " " + "Task index:" + subTaskIndex + " cho " + player.name);
            Service.gI().sendThongBao(player, "Nhiệm vụ hiện tại của bạn là: " + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
            JOptionPane.showMessageDialog(this, "Next nhiệm vụ thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            lblInfo.setText("Lỗi khi đặt nhiệm vụ cho: " + player.name);
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUptimeDisplay() {
        if (serverStartTime != null) {
            Duration uptime = Duration.between(serverStartTime, Instant.now());
            long seconds = uptime.getSeconds();
            long days = seconds / (24 * 3600);
            seconds %= (24 * 3600);
            long hours = seconds / 3600;
            seconds %= 3600;
            long minutes = seconds / 60;
            seconds %= 60;
            String uptimeString = String.format("Thời gian hoạt động: %d ngày, %02d:%02d:%02d", days, hours, minutes, seconds);
            lblUptime.setText(uptimeString);
        }
    }

    private void updateConsignItemsCount() {
        if (ConsignShopManager.gI() != null) {
            int count = ConsignShopManager.gI().listItem.size();
            lblConsignItemsCount.setText("Vật phẩm ký gửi: " + count);
        } else {
            lblConsignItemsCount.setText("Vật phẩm ký gửi: N/A");
        }
    }

    private void updateGiftcodeInfoDisplay() {
        if (GiftCodeManager.gI() != null) {
            int totalGiftcodes = GiftCodeManager.gI().listGiftCode.size();
            lblGiftcodeInfo.setText(String.format("Giftcode: %d", totalGiftcodes));
        } else {
            lblGiftcodeInfo.setText("Giftcode: N/A");
        }
    }

    private void updatePlayerTable() {
        try {
            var newPlayerList = (Client.gI() != null) ? Client.gI().getPlayers() : new ArrayList();
            Map<Integer, Player> onlinePlayerMap = newPlayerList.stream().collect(Collectors.toMap(p -> (int) p.id, p -> p));
            int selectedRow = playerTable.getSelectedRow();
            int previouslySelectedId = -1;
            if (selectedRow != -1) {
                try {
                    previouslySelectedId = (int) playerTable.getModel().getValueAt(playerTable.convertRowIndexToModel(selectedRow), 0);
                } catch (IndexOutOfBoundsException e) {
                    previouslySelectedId = -1;
                }
            }
            Set<Integer> playerIdsInTable = new HashSet<>();
            for (int i = playerTableModel.getRowCount() - 1; i >= 0; i--) {
                int playerId = (int) playerTableModel.getValueAt(i, 0);
                playerIdsInTable.add(playerId);
                Player player = onlinePlayerMap.get(playerId);
                if (player != null) {
                    playerTableModel.setValueAt(String.format("%,d", player.nPoint.power), i, 2);
                    playerTableModel.setValueAt(getTaskInfo(player), i, 3);
                } else {
                    playerTableModel.removeRow(i);
                }
            }
            for (Player player : newPlayerList) {
                if (!playerIdsInTable.contains((int) player.id)) {
                    playerTableModel.addRow(new Object[]{
                        (int) player.id,
                        player.name,
                        String.format("%,d", player.nPoint.power),
                        getTaskInfo(player)
                    });
                }
            }
            if (previouslySelectedId != -1) {
                for (int i = 0; i < playerTableModel.getRowCount(); i++) {
                    if ((int) playerTableModel.getValueAt(i, 0) == previouslySelectedId) {
                        int viewRow = playerTable.convertRowIndexToView(i);
                        if (viewRow != -1) {
                            playerTable.setRowSelectionInterval(viewRow, viewRow);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void kickPlayer(int playerId) {
        getPlayerById(playerId).ifPresent(playerToKick -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn kick người chơi '" + playerToKick.name + "'?",
                    "Xác nhận Kick", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                Client.gI().kickSession(playerToKick.getSession());
                Service.gI().sendThongBao(playerToKick, "Bạn đã bị kick khỏi server bởi Admin.");
                lblInfo.setText("Đã kick player: " + playerToKick.name);
            }
        });
    }

    private void banPlayer(int playerId) {
        getPlayerById(playerId).ifPresent(playerToBan -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn BAN người chơi '" + playerToBan.name + "'?\nHành động này sẽ kick và cấm họ đăng nhập.",
                    "Xác nhận BAN", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                PlayerService.gI().KhoaTaiKhoan(playerToBan);
                Service.gI().sendThongBao(playerToBan,
                        "Tài khoản của bạn đã bị khóa game sẽ mất kết nối sau 5 giây...");
                playerToBan.iDMark.setLastTimeBan(System.currentTimeMillis());
                playerToBan.iDMark.setBan(true);
                lblInfo.setText("Đã Ban Player: " + playerToBan.name);
            }
        });
    }

    private void kickAllPlayers() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn KICK TẤT CẢ người chơi khỏi server?",
                "Xác nhận Kick Toàn Bộ", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            var players = Client.gI().getPlayers();

            if (players == null || players.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có người chơi nào để kick.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int count = 0;
            for (Player p : new ArrayList<>(players)) {
                if (p != null && p.getSession() != null) {
                    Client.gI().kickSession(p.getSession());
                    count++;
                }
            }

            JOptionPane.showMessageDialog(this, "Đã kick " + count + " người chơi.", "Hoàn Tất", JOptionPane.INFORMATION_MESSAGE);
            lblInfo.setText("Đã kick toàn bộ " + count + " người chơi.");
        }
    }

    private Optional<Player> getPlayerById(int id) {
        return (Client.gI() != null) ? Client.gI().getPlayers().stream().filter(p -> p.id == id).findFirst() : Optional.empty();
    }

    private JPanel createHeaderStatusPanel() {
        PinkHeartsPanel headerPanel = new PinkHeartsPanel();
        headerPanel.setLayout(new GridLayout(1, 3, 10, 5));
        Font headerFont = new Font("Segoe UI", Font.BOLD, 16);
        lblStatus = createStatusLabel("Server Online", new Color(0, 176, 80), headerFont);
        lblPlayerCount = createStatusLabel("Online: 0", null, headerFont);
        lblCountdown = createStatusLabel("Sẵn sàng", null, headerFont);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblPlayerCount.setHorizontalAlignment(SwingConstants.CENTER);
        lblCountdown.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblStatus);
        headerPanel.add(lblPlayerCount);
        headerPanel.add(lblCountdown);
        return headerPanel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new PinkHeartsPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Menu Sever Game"));
        JPanel firstRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnMaintenance = new JButton("Bảo Trì");
        btnMaintenance.addActionListener(e -> confirmMaintenance());
        firstRowPanel.add(btnMaintenance);
        JButton btnReloadDb = new JButton("Tải lại Database");
        btnReloadDb.addActionListener(e -> showReloadOptions());
        firstRowPanel.add(btnReloadDb);
        JButton btnCleanSession = new JButton("Dọn Session");
        btnCleanSession.addActionListener(e -> cleanInactiveSessions());
        firstRowPanel.add(btnCleanSession);
        btnToggleAutoSave = new JToggleButton("Bật AutoSave");
        btnToggleAutoSave.setSelected(true);
        btnToggleAutoSave.addActionListener(e -> {
            boolean enable = isAutoSaveEnabled.get();
            if (enable) {
                MaiTienDungManager.getInstance().stopAutoSave();
                btnToggleAutoSave.setText("Tắt AutoSave");
                lblInfo.setText("Đã tắt tính năng tự động lưu dữ liệu.");
            } else {
                MaiTienDungManager.getInstance().startAutoSave();
                btnToggleAutoSave.setText("Bật AutoSave");
                lblInfo.setText("Đã bật tính năng tự động lưu dữ liệu.");
            }
            isAutoSaveEnabled.set(!enable);
            btnToggleAutoSave.setSelected(!enable);
        });
        firstRowPanel.add(btnToggleAutoSave);
        JPanel secondRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnResetAllBosses = new JButton("Reset Boss");
        btnResetAllBosses.setBackground(new Color(255, 140, 0));
        btnResetAllBosses.setToolTipText("Đặt lại trạng thái của tất cả boss về ban đầu.");
        btnResetAllBosses.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn reset TẤT CẢ BOSS?",
                    "Xác nhận Reset Boss", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                BossManager.gI().resetAllBosses();
                lblInfo.setText("Đã reset tất cả boss.");
            }
        });
        secondRowPanel.add(btnResetAllBosses);
        JButton btnRespawnRestingBosses = new JButton("Resurect all bosses");
        btnRespawnRestingBosses.setBackground(new Color(0, 123, 255));
        btnRespawnRestingBosses.setForeground(Color.WHITE);
        btnRespawnRestingBosses.setToolTipText("Buộc tất cả các boss đang trong thời gian reset hồi sinh ngay lập tức.");
        btnRespawnRestingBosses.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn hồi sinh tất cả các boss?",
                    "Xác nhận Hồi sinh", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                respawnRestingBosses();
            }
        });
        secondRowPanel.add(btnRespawnRestingBosses);
        actionPanel.add(firstRowPanel);
        actionPanel.add(secondRowPanel);
        return actionPanel;
    }

    private void respawnRestingBosses() {
        try {
            if (BossManager.gI() == null) {
                JOptionPane.showMessageDialog(this, "BossManager chưa sẵn sàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int respawnedCount = BossManager.gI().respawnAllRestingBosses();
            String message;
            if (respawnedCount > 0) {
                message = "Hồi sinh cho " + respawnedCount + " boss đang reset.";
            } else {
                message = "Không có boss nào đang trong trạng thái reset để hồi sinh.";
            }
            lblInfo.setText(message);
            JOptionPane.showMessageDialog(this, message, "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            Logger.error("Lỗi khi hồi sinh boss: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi hồi sinh boss.", "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cleanInactiveSessions() {
        if (SessionManager.gI() == null) {
            JOptionPane.showMessageDialog(this, "SessionManager chưa sẵn sàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String confirmationMessage = "Bạn có chắc muốn dọn dẹp các session không hoạt động?\n"
                + "(Các session không có người chơi đăng nhập sẽ bị đóng vĩnh viễn)";
        int choice = JOptionPane.showConfirmDialog(this, confirmationMessage,
                "Xác nhận Dọn Session", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            Set<ISession> activePlayerSessions = Client.gI().getPlayers().stream()
                    .map(Player::getSession)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            var inactiveSessions
                    = SessionManager.gI().getSessions().stream()
                            .filter(session -> !activePlayerSessions.contains(session))
                            .collect(java.util.stream.Collectors.toList());
            inactiveSessions.forEach(ISession::disconnect);
            int cleanedCount = inactiveSessions.size();
            String resultMessage = String.format("Đã dọn dẹp thành công %d session không hoạt động.", cleanedCount);
            lblInfo.setText(resultMessage);
            JOptionPane.showMessageDialog(this, resultMessage, "Hoàn Tất", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createExpRatePanel() {
        JPanel expPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        expPanel.setBorder(BorderFactory.createTitledBorder("Menu Điều Chỉnh Exp Sever"));
        expPanel.add(new JLabel("Tỷ lệ exp sever:"));
        JTextField txtExpRate = new JTextField(String.valueOf(Manager.RATE_EXP_SERVER), 5);
        expPanel.add(txtExpRate);
        JButton btnUpdateExp = new JButton("Cập nhập");
        btnUpdateExp.addActionListener(e -> {
            try {
                int newRate = Integer.parseInt(txtExpRate.getText().trim());
                if (newRate <= 0) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập một giá trị Exp hợp lệ (lớn hơn 0).", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (newRate >= Short.MAX_VALUE) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập một giá trị từ 1 - " + Short.MAX_VALUE, "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Manager.RATE_EXP_SERVER = newRate;
                lblInfo.setText("Tỷ lệ EXP server được cập nhật thành x" + Manager.RATE_EXP_SERVER);
                String notification = "Admin vừa điều chỉnh tỷ lệ kinh nghiệm máy chủ thành x" + Manager.RATE_EXP_SERVER;
                Client.gI().getPlayers().forEach(p -> Service.gI().sendThongBao(p, notification));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập một con số hợp lệ cho tỷ lệ EXP.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            }
        });
        expPanel.add(btnUpdateExp);
        return expPanel;
    }

    private JPanel createGameStatsPanel() {
        JPanel gameStatsPanel = new JPanel(new GridLayout(4, 1, 10, 5));
        gameStatsPanel.setBorder(BorderFactory.createTitledBorder("Thông số Game"));
        Font statsFont = new Font("Segoe UI", Font.PLAIN, 12);
        lblGiftcodeInfo = createStatusLabel("Giftcode: N/A", null, statsFont);
        lblConsignItemsCount = createStatusLabel("Vật phẩm ký gửi: N/A", null, statsFont);
        lblBossStatus = createStatusLabel("Boss: Đang tải...", null, statsFont);
        lblUptime = createStatusLabel("Thời gian hoạt động: N/A", null, statsFont);
        gameStatsPanel.add(lblGiftcodeInfo);
        gameStatsPanel.add(lblConsignItemsCount);
        gameStatsPanel.add(lblBossStatus);
        gameStatsPanel.add(lblUptime);
        return gameStatsPanel;
    }

    private JPanel createSystemStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thông số Hệ thống"));
        Font statsFont = new Font("Segoe UI", Font.PLAIN, 12);
        lblCpuUsage = createStatusLabel("CPU: N/A", null, statsFont);
        lblRamUsage = createStatusLabel("RAM: N/A", null, statsFont);
        lblThreadCount = createStatusLabel("Threads: 0", null, statsFont);
        lblSessionCount = createStatusLabel("Sessions: 0", null, statsFont);
        statsPanel.add(lblCpuUsage);
        statsPanel.add(lblRamUsage);
        statsPanel.add(lblThreadCount);
        statsPanel.add(lblSessionCount);
        return statsPanel;
    }

    private JPanel createSchedulerPanel() {
        JPanel schedulerPanel = new JPanel(new GridBagLayout());
        schedulerPanel.setBorder(BorderFactory.createTitledBorder(null, "Hẹn giờ bảo trì",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14),
                new Color(255, 193, 7)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        schedulerPanel.add(new JLabel("Giờ:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        cbHour = new JComboBox<>();
        for (int i = -1; i < 24; i++) {
            cbHour.addItem(i);
        }
        schedulerPanel.add(cbHour, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        schedulerPanel.add(new JLabel("Phút:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1;
        cbMinute = new JComboBox<>();
        for (int i = -1; i < 60; i++) {
            cbMinute.addItem(i);
        }
        schedulerPanel.add(cbMinute, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0;
        schedulerPanel.add(new JLabel("Giây:"), gbc);
        gbc.gridx = 5;
        gbc.weightx = 1;
        cbSecond = new JComboBox<>();
        for (int i = -1; i < 60; i++) {
            cbSecond.addItem(i);
        }
        schedulerPanel.add(cbSecond, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 6;
        chkAutoRestart = new JCheckBox("Tự động khởi động lại sau bảo trì", true);
        chkAutoRestart.setHorizontalAlignment(SwingConstants.CENTER);
        schedulerPanel.add(chkAutoRestart, gbc);
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 6;
        JButton btnSchedule = new JButton("Đặt lịch / Cập nhật");
        btnSchedule.addActionListener(e -> scheduleMaintenance());
        schedulerPanel.add(btnSchedule, gbc);
        return schedulerPanel;
    }

    private JPanel createFooterInfoPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
        Font footerFont = new Font("Segoe UI", Font.PLAIN, 12);
        lblInfo = createStatusLabel("Sẵn sàng.", null, footerFont);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(lblInfo, BorderLayout.CENTER);
        return footerPanel;
    }

    private void initLogic() {
        try {
            this.osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        } catch (Exception e) {
            Logger.error("Không thể khởi tạo bean theo dõi hệ thống. Thông số CPU/RAM sẽ không có sẵn.");
            this.osBean = null;
        }
        scheduler = Executors.newScheduledThreadPool(5);
        scheduler.scheduleAtFixedRate(() -> {
            if (osBean != null) {
                double cpuLoad = osBean.getProcessCpuLoad() * 100;
                Runtime runtime = Runtime.getRuntime();
                long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
                long totalMemory = runtime.totalMemory() / (1024 * 1024);
                String cpuText = String.format("CPU: %.2f%%", cpuLoad);
                String ramText = String.format("RAM: %d/%d MB", usedMemory, totalMemory);
                SwingUtilities.invokeLater(() -> {
                    lblCpuUsage.setText(cpuText);
                    lblRamUsage.setText(ramText);
                });
            }
            SwingUtilities.invokeLater(() -> {
                lblPlayerCount.setText("Online: " + (Client.gI() != null ? Client.gI().getPlayers().size() : "N/A"));
                lblThreadCount.setText("Threads: " + Thread.activeCount());
                lblSessionCount.setText("Sessions: " + (SessionManager.gI() != null ? SessionManager.gI().getSessions().size() : "N/A"));
                updatePlayerTable();
                updateUptimeDisplay();
                updateConsignItemsCount();
                updateGiftcodeInfoDisplay();
                if (BossManager.gI() != null) {
                    try {
                        int[] bossCounts = BossManager.gI().getBossStatusCounts();
                        lblBossStatus.setText(String.format("Boss: %d Sống / %d Hồi sinh / %d Chờ", bossCounts[0], bossCounts[1], bossCounts[2]));
                    } catch (Exception e) {
                        lblBossStatus.setText("Boss: Lỗi tải dữ liệu");
                    }
                }
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void showReloadOptions() {
        String[] options = {"Tải lại GiftCode", "Tải lại Shop", "Tải lại Npcs", "Tải list Top"};
        int choice = JOptionPane.showOptionDialog(this, "Chọn loại dữ liệu muốn tải lại:", "Tải lại Database",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0: {
                loadGiftcode();
                lblInfo.setText("Đã tải lại dữ liệu GiftCode.");
                break;
            }
            case 1: {
                loadShop();
                lblInfo.setText("Đã tải lại dữ liệu Shop.");
                break;
            }
            case 2: {
                loadNpcs();
                lblInfo.setText("Đã tải lại dữ liệu NPCs.");
                break;
            }
            case 3: {
                loadTop();
                lblInfo.setText("Đã tải lại dữ liệu Bảng Xếp Hạng.");
                break;
            }
        }
    }

    public void loadGiftcode() {
        GiftCodeManager.gI().listGiftCode.clear();
        try (java.sql.Connection con = ConnectDB.getConnection();) {
            java.sql.PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                ArrayList<Integer> tempListIdPlayer = new ArrayList<>();
                String tempDBListIdPlayers;
                giftcode.code = rs.getString("code");
                giftcode.countLeft = rs.getInt("count_left");
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                String dbListIdPlayer = rs.getString("listIdPlayers");
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("item"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);
                        giftcode.detail.put(Integer.valueOf(jsonObj.get("id").toString()),
                                Integer.valueOf(jsonObj.get("quantity").toString()));
                        jsonObj.clear();
                    }
                }
                JSONArray option = (JSONArray) JSONValue.parse(rs.getString("option"));
                if (option != null) {
                    for (int u = 0; u < option.size(); u++) {
                        JSONObject jsonobject = (JSONObject) option.get(u);
                        giftcode.option.add(new ItemOption(Integer.parseInt(jsonobject.get("id").toString()),
                                Integer.parseInt(jsonobject.get("param").toString())));
                        jsonobject.clear();
                    }
                }
                if (!dbListIdPlayer.isEmpty()) {
                    tempDBListIdPlayers = dbListIdPlayer = GiftCodeManager.removeCharAt(dbListIdPlayer, 0);
                    tempDBListIdPlayers = dbListIdPlayer = GiftCodeManager.removeCharAt(dbListIdPlayer, dbListIdPlayer.length() - 1);
                    String[] resultTempDBListPlayer = tempDBListIdPlayers.split(",");
                    for (String item : resultTempDBListPlayer) {
                        if (!item.isEmpty()) {
                            tempListIdPlayer.add(Integer.parseInt(item));
                        }
                    }
                    giftcode.listIdPlayer = tempListIdPlayer;
                }
                GiftCodeManager.gI().listGiftCode.add(giftcode);
            }
            Logger.success("Successfully reloaded " + GiftCodeManager.gI().listGiftCode.size() + " giftcodes.\n");
        } catch (Exception e) {
            Logger.error("Error reloading giftcode from database\n");
        }
    }

    private void loadNpcs() {
        Manager.NPC_TEMPLATES.clear();
        String sql = "SELECT * FROM npc_template";
        try (java.sql.Connection con = ConnectDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                Manager.NPC_TEMPLATES.add(npcTemp);
            }
            Logger.success("Successfully reloaded " + Manager.NPC_TEMPLATES.size() + " NPCs.\n");
        } catch (Exception e) {
            Logger.error("Error reloading NPCs from database\n");
        }
    }

    private void loadShop() {
        try (java.sql.Connection con = ConnectDB.getConnection()) {
            Manager.SHOPS = ShopDAO.getShops(con);
            Logger.success("Successfully reloaded " + Manager.SHOPS.size() + " shops.\n");
        } catch (Exception e) {
            Logger.error("Error reloading shops from database\n");
        }
    }

    private void loadTop() {
        TopServer.LoadingTop();
    }

    private void giveItemToPlayer(int playerId, int itemId, int quantity, int optionId, int optionParam) {
        getPlayerById(playerId).ifPresent(targetPlayer -> {
            try {
                Item newItem = ItemService.gI().createNewItem((short) itemId);
                if (newItem == null) {
                    JOptionPane.showMessageDialog(this, "Tặng vật phẩm thất bại!\nID vật phẩm không hợp lệ: " + itemId, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (itemId >= Manager.ITEM_TEMPLATES.size()) {
                    JOptionPane.showMessageDialog(this, "Tặng vật phẩm thất bại!\nID vật phẩm không tồn tại trên hệ thống", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                newItem.quantity = quantity;
                if (quantity > 9999) {
                    JOptionPane.showMessageDialog(this, "Tặng vật phẩm thất bại!\nSố lượng chỉ giới hạn từ 0 - 9999", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (optionId > 0) {
                    newItem.itemOptions.add(new ItemOption(optionId, optionParam));
                }
                InventoryService.gI().addItemBag(targetPlayer, newItem);
                InventoryService.gI().sendItemBag(targetPlayer);
                Service.gI().sendThongBao(targetPlayer, "Bạn nhận được [" + newItem.template.name + " (x" + quantity + ")] từ Admin.");
                lblInfo.setText("Đã tặng '" + newItem.template.name + "' cho " + targetPlayer.name);
            } catch (HeadlessException e) {
                Logger.error("Lỗi khi tặng vật phẩm cho player ID " + playerId + ": ");
            }
        });
    }

    private void openGiveItemDialog(int playerId) {
        getPlayerById(playerId).ifPresent(player -> {
            JTextField itemIdField = new JTextField(5);
            JTextField quantityField = new JTextField(5);
            JTextField optionIdField = new JTextField(5);
            JTextField optionParamField = new JTextField(5);
            PinkHeartsPanel panel = new PinkHeartsPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("ID Item:"), gbc);
            gbc.gridx = 1;
            panel.add(itemIdField, gbc);
            gbc.gridy = 1;
            panel.add(quantityField, gbc);
            gbc.gridx = 0;
            panel.add(new JLabel("Số lượng:"), gbc);
            gbc.gridy = 2;
            panel.add(new JLabel("ID Option: (bỏ trống nếu không có)"), gbc);
            gbc.gridx = 1;
            panel.add(optionIdField, gbc);
            gbc.gridy = 3;
            panel.add(optionParamField, gbc);
            gbc.gridx = 0;
            panel.add(new JLabel("Param:"), gbc);
            int result = JOptionPane.showConfirmDialog(this, panel, "Tặng vật phẩm cho: " + player.name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    int itemId = Integer.parseInt(itemIdField.getText());
                    int quantity = Integer.parseInt(quantityField.getText());
                    int optionId = optionIdField.getText().isEmpty() ? 0 : Integer.parseInt(optionIdField.getText());
                    int optionParam = optionParamField.getText().isEmpty() ? 0 : Integer.parseInt(optionParamField.getText());
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(this, "Số lượng phải là số dương.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (itemId >= Manager.ITEM_TEMPLATES.size()) {
                        JOptionPane.showMessageDialog(this, "Đã quá giới hạn id item_template.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    giveItemToPlayer(playerId, itemId, quantity, optionId, optionParam);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số.", "Lỗi Định Dạng", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void openBuffVndDialogForPlayer(int playerId) {
        getPlayerById(playerId).ifPresent(player -> {
            JTextField vndAmountField = new JTextField(10);
            PinkHeartsPanel panel = new PinkHeartsPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Số VND muốn buff:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(vndAmountField, gbc);
            int result = JOptionPane.showConfirmDialog(this, panel, "Buff VND cho " + player.name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    int vndAmount = Integer.parseInt(vndAmountField.getText().trim());
                    if (vndAmount <= 0) {
                        JOptionPane.showMessageDialog(this, "Không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (DatabaseUpdater.addVND_byPlayer(player, vndAmount)) {
                        Service.gI().sendThongBao(player, "Bạn đã được cộng " + Util.format(vndAmount) + " VND vào tài khoản của bạn.");
                        lblInfo.setText("Đã buff " + vndAmount + " VND cho " + player.name + ".");
                        JOptionPane.showMessageDialog(this, "Đã buff " + Util.format(vndAmount) + " VND thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        if (player.inventory != null) {
                            player.inventory.addExpVip(vndAmount / 100);
                            Service.gI().sendVipExp(player);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Buff thất bại. Nhân vật không tồn tại hoặc có lỗi.", "Thất bại", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private JLabel createStatusLabel(String text, Color foregroundColor, Font font) {
        JLabel label = new JLabel(text);
        if (font != null) {
            label.setFont(font);
        }
        if (foregroundColor != null) {
            label.setForeground(foregroundColor);
        }
        return label;
    }

    private void startServerProcesses() {
        loadMaintenanceConfig();
        ServerManager.gI().run();
        if (AutoMaintenance.AutoMaintenance) {
            AutoMaintenance.gI().start();
        }
        EventQueue.invokeLater(() -> setVisible(true));
    }

    private void loadMaintenanceConfig() {
        File file = new File("maintenanceConfig.txt");
        if (!file.exists()) {
            cbHour.setSelectedItem(-1);
            cbMinute.setSelectedItem(-1);
            cbSecond.setSelectedItem(-1);
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            cbHour.setSelectedItem(Integer.valueOf(br.readLine()));
            cbMinute.setSelectedItem(Integer.valueOf(br.readLine()));
            cbSecond.setSelectedItem(Integer.valueOf(br.readLine()));
            chkAutoRestart.setSelected(Boolean.parseBoolean(br.readLine()));
        } catch (Exception e) {
            lblInfo.setText("Lỗi đọc cấu hình bảo trì.");
        }
    }

    private void confirmMaintenance() {
        String restartMessage = chkAutoRestart.isSelected() ? "\nServer sẽ tự động khởi động lại." : "\nServer sẽ KHÔNG tự động khởi động lại.";
        if (JOptionPane.showConfirmDialog(this, "Bắt đầu bảo trì ngay (1 phút)?" + restartMessage, "Xác nhận bảo trì", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            REQUEST_AUTO_RESTART = chkAutoRestart.isSelected();
            cancelAllScheduledTasks();
            Maintenance.gI().start(60);
            lblStatus.setText("Bảo trì...");
            lblStatus.setForeground(new Color(255, 100, 0));
            var remainingSeconds = new AtomicInteger(60);
            immediateMaintenanceCountdownFuture = scheduler.scheduleAtFixedRate(() -> {
                int currentSeconds = remainingSeconds.decrementAndGet();
                if (currentSeconds >= 0) {
                    SwingUtilities.invokeLater(() -> lblCountdown.setText(String.format("%02d:%02d", currentSeconds / 60, currentSeconds % 60)));
                } else {
                    if (immediateMaintenanceCountdownFuture != null) {
                        immediateMaintenanceCountdownFuture.cancel(false);
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void scheduleMaintenance() {
        Integer h = (Integer) cbHour.getSelectedItem();
        Integer m = (Integer) cbMinute.getSelectedItem();
        Integer s = (Integer) cbSecond.getSelectedItem();
        if (h == -1 || m == -1 || s == -1) {
            return;
        }
        REQUEST_AUTO_RESTART = chkAutoRestart.isSelected();
        long delay = LocalTime.of(h, m, s).toSecondOfDay() - LocalTime.now().toSecondOfDay();
        if (delay < 0) {
            delay += 86400;
        }
        cancelAllScheduledTasks();
        maintenanceScheduled.set(true);
        lblStatus.setText("Đã hẹn giờ");
        lblStatus.setForeground(new Color(255, 193, 7));
        long triggerEpoch = System.currentTimeMillis() / 1000 + delay;
        activeMaintenanceJobFuture = scheduler.schedule(() -> {
            if (maintenanceScheduled.get()) {
                Maintenance.gI().start(1);
            }
        }, delay, TimeUnit.SECONDS);
        activeCountdownDisplayFuture = scheduler.scheduleAtFixedRate(() -> {
            if (!maintenanceScheduled.get()) {
                if (activeCountdownDisplayFuture != null) {
                    activeCountdownDisplayFuture.cancel(false);
                }
                return;
            }
            long remain = triggerEpoch - (System.currentTimeMillis() / 1000);
            long hr = remain / 3600, mn = (remain % 3600) / 60, sc = remain % 60;
            SwingUtilities.invokeLater(() -> lblCountdown.setText(String.format("%02d:%02d:%02d", hr, mn, sc)));
        }, 0, 1, TimeUnit.SECONDS);
        saveMaintenanceConfig(h, m, s, REQUEST_AUTO_RESTART);
    }

    private void saveMaintenanceConfig(int h, int m, int s, boolean autoRestart) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("maintenanceConfig.txt"))) {
            pw.println(h);
            pw.println(m);
            pw.println(s);
            pw.println(autoRestart);
            lblInfo.setText(String.format("Lịch: %02d:%02d:%02d | Restart: %s", h, m, s, autoRestart ? "On" : "Off"));
        } catch (IOException e) {
            Logger.error("Lỗi lưu cấu hình bảo trì: " + e.getMessage());
        }
    }

    private void cancelAllScheduledTasks() {
        if (activeMaintenanceJobFuture != null) {
            activeMaintenanceJobFuture.cancel(true);
        }
        if (activeCountdownDisplayFuture != null) {
            activeCountdownDisplayFuture.cancel(true);
        }
        if (immediateMaintenanceCountdownFuture != null) {
            immediateMaintenanceCountdownFuture.cancel(true);
        }
        maintenanceScheduled.set(false);
    }

    private void confirmExit() {
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát? Server sẽ tắt.", "Xác nhận thoát", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            shutdownServer();
        }
    }

    private void shutdownServer() {
        ddosRunning = false;
        if (ddosScheduler != null) {
            ddosScheduler.shutdownNow();
        }
        ProxyManager.getInstance().stopAll();
        MaiTienDungManager.getInstance().stopAutoSave();
        if (AutoMaintenance.isRunning) {
            AutoMaintenance.gI().interrupt();
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        System.exit(0);
    }

    public static void attemptAutoRestart() {
        if (!REQUEST_AUTO_RESTART) {
            return;
        }
        String osName = System.getProperty("os.name").toLowerCase();
        String currentDir = System.getProperty("user.dir");
        try {
            if (osName.contains("win")) {
                new ProcessBuilder("cmd.exe", "/c", "start", "run.bat").directory(new File(currentDir)).start();
            } else {
                new ProcessBuilder("xterm", "-e", "./run.sh").directory(new File(currentDir)).start();
            }
        } catch (IOException e) {
        }
    }
    

    private String getTaskInfo(Player player) {
        try {
            if (player != null && player.playerTask != null && player.playerTask.taskMain != null
                    && player.playerTask.taskMain.subTasks != null
                    && !player.playerTask.taskMain.subTasks.isEmpty()
                    && player.playerTask.taskMain.index < player.playerTask.taskMain.subTasks.size()) {
                String taskName = player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name;
                short count = player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count;
                short maxCount = player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount;
                if (maxCount > 0) {
                    return String.format("%s (%d/%d)", taskName, count, maxCount);
                } else {
                    return taskName;
                }
            }
        } catch (Exception e) {
            return "Lỗi dữ liệu";
        }
        return "Không có";
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(MenuUI::new);
    }
}
