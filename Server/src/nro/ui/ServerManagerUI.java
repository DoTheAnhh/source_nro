package nro.ui;

import nro.server.Client;
import nro.server.Maintenance;
import nro.server.Manager;
import nro.server.ServerManager;
import nro.server.TopServer;
import nro.service.autosave.DoTheAnhManager;
import nro.gameplay.bot.BotManager;
import nro.core.log.Logger;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.border.EmptyBorder;
import nro.net.Network;
import nro.net.session.MySession;
import nro.net.session.SessionManager;
import nro.gameplay.bot.newgen.BotManager_new;
import nro.service.clan.ClanService;
import nro.service.consignmentstore.ConsignShopManager;
import nro.server.proxy.ProxyManager;

//public final class ServerManagerUI extends JFrame {
//
//    private Preferences preferences;
//    private JLabel plCountLabel;
//    private JLabel SessionCountLabel;
//    private JLabel botCountLabel;
//    private JLabel botCountLabel_new;
//    private JLabel threadCountLabel;
//    private JLabel EventCountLabel;
//    private JLabel TypeDataCountLabel;
//    private JTextField minutesField;
//    private JLabel messageLabel;
//    private JLabel countdownLabel;
//    private Timer countdownTimer;
//    private int remainingSeconds;
//    private ButtonGroup maintenanceGroup;
//    private JCheckBox maintenanceOption1;
//    private JCheckBox maintenanceOption2;
//    private JLabel info;
//    public static boolean isRunning;
//
//    public ServerManagerUI() {
//        preferences = Preferences.userNodeForPackage(ServerManagerUI.class);
//        setTitle("Ngọc Rồng Tuổi Thơ");
//        setSize(500, 300);
//        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        setLocationRelativeTo(null);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                confirmExit();
//            }
//        });
//        JPanel panel = new JPanel();
//        getContentPane().add(panel);
//        panel.setLayout(new GridLayout(0, 2));
//        JButton maintenanceButton = new JButton("BẢO_TRÌ");
//        maintenanceButton.addActionListener(e -> showMaintenanceDialog());
//        panel.add(maintenanceButton);
//        JButton maintenanceButton1 = new JButton("KICK_ALL_PLAYER");
//        maintenanceButton1.addActionListener(e -> kick());
//        panel.add(maintenanceButton1);
//        JButton maintenanceButton2 = new JButton("THAY_EXP");
//        maintenanceButton2.addActionListener(e -> tnsm());
//        panel.add(maintenanceButton2);
//        
//        JButton maintenanceButton3 = new JButton("START_ANTIDDOS");
//        maintenanceButton3.addActionListener(e -> startAntiDDoS());
//        panel.add(maintenanceButton3);
//        
//        JButton saveButton = new JButton("SAVE_DATA");
//        saveButton.addActionListener((ActionEvent e) -> {
//            Logger.success("Đang tiến hành lưu data");
//            Network.gI().stopConnect();
//            
//            Maintenance.isRunning = false;
//            try {
//                Logger.error("Đang tiến hành lưu data bang hội");
//                ClanService.gI().close();
//                Thread.sleep(1000);
//                Logger.success("Lưu dữ liệu bang hội thành công");
//            } catch (InterruptedException ex) {
//                Logger.error("Lỗi lưu dữ liệu bang hội");
//            }
//            try {
//                Logger.error("Đang tiến hành lưu data ký gửi");
//                ConsignShopManager.gI().save();
//                Thread.sleep(1000);
//                Logger.success("Lưu dữ liệu ký gửi thành công");
//            } catch (InterruptedException ex) {
//                Logger.error("Lỗi lưu dữ liệu ký gửi");
//            }
//            
//            try {
//                Logger.error("Đang tiến hành đẩy người chơi");
//                Client.gI().close();

//                Thread.sleep(1000);
//                Logger.success("Lưu dữ liệu người dùng thành công");
//            } catch (InterruptedException ex) {
//                Logger.error("Lỗi lưu dữ liệu người dùng");
//            }
//            System.exit(0);
//        });
//        panel.add(saveButton);
//        
//        JButton clearFw = new JButton("CLEAR_FIREWALL");
//        clearFw.addActionListener((ActionEvent e) -> {
//            JOptionPane.showMessageDialog(null, "Đã clear firewall");
//        });
//        panel.add(clearFw);
//        
//        JButton loadshop = new JButton("LOADING_SHOP");
//        loadshop.addActionListener((ActionEvent e) -> {
//            Manager.gI().updateShop();
//            JOptionPane.showMessageDialog(null, "Đã Update Shop.");
//        });
//        panel.add(loadshop);
//        
//        JButton loadtop = new JButton("LOADING_TOP");
//        loadtop.addActionListener((ActionEvent e) -> {
//            TopServer.LoadingTop();
//            JOptionPane.showMessageDialog(null, "Đã Update TOP.");
//        });
//        panel.add(loadtop);
//        
//        info = new JLabel("");
//        // Đọc giá trị từ tệp tin
//        try (BufferedReader reader = new BufferedReader(new FileReader("maintenanceConfig.txt"))) {
//            String hoursLine = reader.readLine();
//            String minutesLine = reader.readLine();
//
//            int hours = Integer.parseInt(hoursLine);
//            int minutes = Integer.parseInt(minutesLine);
//
//            // Thêm giá trị vào DefaultComboBoxModel
//            DefaultComboBoxModel<Integer> hoursModel = new DefaultComboBoxModel<>();
//            for (int i = -1; i < 24; i++) {
//                hoursModel.addElement(i);
//            }
//            JComboBox<Integer> hoursComboBox = new JComboBox<>(hoursModel);
//            panel.add(hoursComboBox);
//            hoursComboBox.setSelectedItem(hours);
//
//            // Thêm giá trị vào DefaultComboBoxModel
//            DefaultComboBoxModel<Integer> minutesModel = new DefaultComboBoxModel<>();
//            for (int i = -1; i < 60; i++) {
//                minutesModel.addElement(i);
//            }
//            JComboBox<Integer> minutesComboBox = new JComboBox<>(minutesModel);
//            panel.add(minutesComboBox);
//            minutesComboBox.setSelectedItem(minutes);
//            JLabel jLabel2 = new JLabel("SETTING_AUTO_MAINTENACE");
//            panel.add(jLabel2);
//            JButton scheduleButton2 = new JButton("Hẹn Giờ Bảo Trì");
//            scheduleButton2.addActionListener(e -> scheduleMaintenance(hoursComboBox, minutesComboBox));
//            panel.add(scheduleButton2);
//            if (hours != -1 && minutes != -1) {
//                scheduleMaintenance(hoursComboBox, minutesComboBox);
//            }
//        } catch (IOException e) {
//        }
//        
//        // Tạo DefaultComboBoxModel với các sự kiện
//        DefaultComboBoxModel<String> eventModel = new DefaultComboBoxModel<>();
//        eventModel.addElement("Chọn Sự Kiện");
//        eventModel.addElement("Sự Kiện Tết Nguyên Đán");
//        eventModel.addElement("Sự Kiện Noel Giáng Sinh");
//        eventModel.addElement("Sự Kiện Halloween");
//        JComboBox<String> eventComboBox = new JComboBox<>(eventModel);
//        eventComboBox.addActionListener((ActionEvent e) -> {
//        String selectedEvent = (String) eventComboBox.getSelectedItem();
//            handleEventSelection(selectedEvent);  // Gọi hàm xử lý khi chọn sự kiện
//        });
//        panel.add(eventComboBox);
//        
//        EventCountLabel = new JLabel("\nSự Kiện Hiện Tại : ");
//        panel.add(EventCountLabel);
//        
//        // Tạo DefaultComboBoxModel với các sự kiện
//        DefaultComboBoxModel<String> datatype = new DefaultComboBoxModel<>();
//        datatype.addElement("Chọn Kiểu Dữ Liệu");
//        datatype.addElement("int");
//        datatype.addElement("long");
//        datatype.addElement("double");
//        JComboBox<String> datatypeComboBox = new JComboBox<>(datatype);
//        datatypeComboBox.addActionListener((ActionEvent e) -> {
//        String selecteddatatype = (String) datatypeComboBox.getSelectedItem();
//            handleDataTypeSelection(selecteddatatype);  // Gọi hàm xử lý khi chọn sự kiện
//        });
//        panel.add(datatypeComboBox);
//        
//        TypeDataCountLabel = new JLabel("\nKiểu Dữ Liệu Hiện Tại : ");
//        panel.add(TypeDataCountLabel);
//        
//        JButton Login = new JButton("LOGIN_PLAYER");
//        Login.addActionListener((ActionEvent e) -> {
//            int size = 0;
//            MySession session;
//            try {
//                session = new MySession(new Socket("127.0.0.1", 14445));
//                session.version = 240;
//                session.login("1", "1");
//                if (session.isConnected() && session.player != null) {
//                    Manager.player = session.player;
//                    Manager.player.isBotLogin = true;
//                    size ++;
//                }
//            } catch (IOException ex) {
//                java.util.logging.Logger.getLogger(ServerManagerUI.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            try {
//                session = new MySession(new Socket("127.0.0.1", 14445));
//                session.version = 240;
//                session.login("3", "1");
//                if (session.isConnected() && session.player != null) {
//                    Manager.player = session.player;
//                    Manager.player.isBotLogin = true;
//                    size ++;
//                }
//            } catch (IOException ex) {
//                java.util.logging.Logger.getLogger(ServerManagerUI.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            JOptionPane.showMessageDialog(null, "Đã Login Thành Công " + size + " Người Chơi.");
//        });
//        panel.add(Login);
//        
//        messageLabel = new JLabel();
//        panel.add(messageLabel);
//
//        countdownLabel = new JLabel();
//        panel.add(countdownLabel);
//        
//        panel.add(info);
//        threadCountLabel = new JLabel("Số Thread : ");
//        panel.add(threadCountLabel);
//        
//        plCountLabel = new JLabel("Số Người Online : ");
//        panel.add(plCountLabel);
//        SessionCountLabel = new JLabel("Session : ");
//        panel.add(SessionCountLabel);
//        
//        botCountLabel = new JLabel("\nTổng Số Bot Hoạt Động : ");
//        panel.add(botCountLabel);
//        botCountLabel_new = new JLabel("\nTổng Số Bot_New Hoạt Động : ");
//        panel.add(botCountLabel_new);
//        
//        ScheduledExecutorService EventCountExecutor = Executors.newSingleThreadScheduledExecutor();
//        EventCountExecutor.scheduleAtFixedRate(() -> {
//            EventCountLabel.setText("Kiểu Dữ Liệu Hiện Tại : " + (EventManager.LUNNAR_NEW_YEAR ? "Tết Nguyên Đán" : "Không Có Sự Kiện"));
//        }, 1, 1, TimeUnit.SECONDS);
//        
//        ScheduledExecutorService typedataCountExecutor = Executors.newSingleThreadScheduledExecutor();
//        typedataCountExecutor.scheduleAtFixedRate(() -> {
//            boolean typedata = Manager.readInt;
//            TypeDataCountLabel.setText("Kiểu Dữ Liệu Hiện Tại : " + (typedata ? "int" : "long"));
//        }, 1, 1, TimeUnit.SECONDS);
//
//        ScheduledExecutorService threadCountExecutor = Executors.newSingleThreadScheduledExecutor();
//        threadCountExecutor.scheduleAtFixedRate(() -> {
//            int threadCount = Thread.activeCount();
//            threadCountLabel.setText("Số Thread : " + threadCount);
//        }, 1, 1, TimeUnit.SECONDS);
//
//        ScheduledExecutorService plCountExecutor = Executors.newSingleThreadScheduledExecutor();
//        plCountExecutor.scheduleAtFixedRate(() -> {
//            int plcount = Client.gI().getPlayers().size();
//            plCountLabel.setText("Số Người Online : " + plcount);
//        }, 5, 1, TimeUnit.SECONDS);
//        ScheduledExecutorService ssCountExecutor = Executors.newSingleThreadScheduledExecutor();
//        ssCountExecutor.scheduleAtFixedRate(() -> {
//            int sscount = SessionManager.gI().getSessions().size();
//            SessionCountLabel.setText("Session : " + sscount);
//        }, 5, 1, TimeUnit.SECONDS);
//        
//        ScheduledExecutorService botCountExecutor = Executors.newSingleThreadScheduledExecutor();
//        botCountExecutor.scheduleAtFixedRate(() -> {
//            int botcount = BotManager.gI().getBot().size();
//            botCountLabel.setText("\nTổng Số Bot Hoạt Động : " + botcount);
//        }, 5, 1, TimeUnit.SECONDS);
//        ScheduledExecutorService botCountExecutor_new = Executors.newSingleThreadScheduledExecutor();
//        botCountExecutor_new.scheduleAtFixedRate(() -> {
//            int botcount = BotManager_new.gI().getBot().size();
//            botCountLabel_new.setText("\nTổng Số Bot_New Hoạt Động : " + botcount);
//        }, 5, 1, TimeUnit.SECONDS);
//        
//        messageLabel.setText("Server đang chạy tại port : " + ServerManager.PORT);
//        
//        setVisible(true);
//        ServerManager.gI().run();
//        
//        // Đọc giá trị từ tệp
//        MaiTienDungManager.getInstance().startAutoSave();
//    }
//    
//    private void handleDataTypeSelection(String selecteddata) {
//        // Thực hiện hành động tùy theo sự kiện đã chọn
//        switch (selecteddata) {
//            case "Chọn Kiểu Dữ Liệu":
//                break;
//            case "int":
//                Manager.readInt = true;
//                JOptionPane.showMessageDialog(this, "Thành công!");
//                break;
//            case "long":
//                Manager.readInt = false;
//                JOptionPane.showMessageDialog(this, "Thành công!");
//                break;
//            case "double":
//                JOptionPane.showMessageDialog(this, "Thành công!");                
//                break;
//            default:
//                JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ");
//                break;
//        }
//    }
//    
//    private void handleEventSelection(String selectedEvent) {
//        // Thực hiện hành động tùy theo sự kiện đã chọn
//        switch (selectedEvent) {
//            case "Chọn Sự Kiện":
//                EventManager.CHRISTMAS = false;
//                EventManager.HALLOWEEN = false;
//                EventManager.HUNG_VUONG = false;
//                EventManager.INTERNATIONAL_WOMANS_DAY = false;
//                EventManager.LUNNAR_NEW_YEAR = false;
//                EventManager.TRUNG_THU = false;
//                EventManager.gI().init();
//                break;
//            case "Sự Kiện Tết Nguyên Đán":
//                EventManager.LUNNAR_NEW_YEAR = true;
//                EventManager.CHRISTMAS = false;
//                EventManager.HALLOWEEN = false;
//                EventManager.HUNG_VUONG = false;
//                EventManager.INTERNATIONAL_WOMANS_DAY = false;
//                EventManager.TRUNG_THU = false;
//                EventManager.gI().init();
//                JOptionPane.showMessageDialog(this, "Bạn đã chọn Sự kiện Tết Nguyên Đán");
//                Logger.success("Máy chủ đang diễn ra sự kiện Tết Nguyên Đán");
//                break;
//            case "Sự Kiện Noel Giáng Sinh":
//                EventManager.HALLOWEEN = false;
//                EventManager.HUNG_VUONG = false;
//                EventManager.INTERNATIONAL_WOMANS_DAY = false;
//                EventManager.LUNNAR_NEW_YEAR = false;
//                EventManager.TRUNG_THU = false;
//                EventManager.CHRISTMAS = true;
//                JOptionPane.showMessageDialog(this, "Bạn đã chọn Sự kiện Noel Giáng Sinh");
//                Logger.success("Máy chủ đang diễn ra sự kiện Noel Giáng Sinh");
//                
//                break;
//            case "Sự Kiện Halloween":
//                EventManager.CHRISTMAS = false;
//                EventManager.HUNG_VUONG = false;
//                EventManager.INTERNATIONAL_WOMANS_DAY = false;
//                EventManager.LUNNAR_NEW_YEAR = false;
//                EventManager.TRUNG_THU = false;
//                EventManager.HALLOWEEN = true;
//                JOptionPane.showMessageDialog(this, "Bạn đã chọn Sự kiện Halloween");
//                Logger.success("Máy chủ đang diễn ra sự kiện Halloween");
//                break;
//            default:
//                JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ");
//                break;
//        }
//    }
//
//    private void showMaintenanceDialog() {
//        try {
//            int dialogButton = JOptionPane.YES_NO_OPTION;
//            int dialogResult = JOptionPane.showConfirmDialog(this, "Bắt đầu bảo trì?", "Bảo trì", dialogButton);
//            if (dialogResult == 0) {
//                Logger.error("Server tiến hành bảo trì");
//                Maintenance.gI().start(5);
//
//            } else {
//                System.out.println("No Option");
//            }
//        } catch (HeadlessException e) {
//        }
//    }
//
//    private void kick() {
//        new Thread(() -> {
//            Client.gI().close();
//        }).start();
//
//    }
//
//    private void tnsm() {
//        String exp = JOptionPane.showInputDialog(this, "Bảng Exp Server\n"
//                + "Exp Server hiện tại: " + Manager.RATE_EXP_SERVER + "\nExp Server Tối Thiểu Là 100");
//        if (exp != null) {
//            Manager.RATE_EXP_SERVER = Byte.parseByte(exp);
//            Logger.error("Exp hiện tại là: " + exp + "\n");
//        }
//
//    }
//    public static void startAntiDDoS() {
//        try {
//            Runtime rt = Runtime.getRuntime();
//            String command = "cmd /c start run_chongddosvv.bat";
//            rt.exec(command);
//            Logger.success("Đã bật chống DDOS");
//        } catch (IOException ex) {
//            Logger.error("Không thể bật chống DDOS");
//        }
//    }
//
//    private void confirmExit() {
//        int dialogButton = JOptionPane.YES_NO_OPTION;
//        int dialogResult = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát chương trình?", "Thoát", dialogButton);
//        if (dialogResult == 0) {
//            System.exit(0);
//        }
//    }
//
//    @Override
//    public void setDefaultCloseOperation(int operation) {
//        if (operation == JFrame.EXIT_ON_CLOSE) {
//            addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    confirmExit();
//                }
//            });
//        } else {
//            super.setDefaultCloseOperation(operation);
//        }
//    }
//
//    private void scheduleMaintenance(JComboBox<Integer> hoursComboBox, JComboBox<Integer> minutesComboBox) {
//        int hours = hoursComboBox.getItemAt(hoursComboBox.getSelectedIndex());
//        int minutes = minutesComboBox.getItemAt(minutesComboBox.getSelectedIndex());
//        if (minutes == -1 || hours == -1) {
//            JOptionPane.showMessageDialog(this, "Thời gian sai");
//            return;
//        }
//        // Ghi giá trị vào tệp tin
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter("maintenanceConfig.txt"))) {
//            writer.write(hours + "\n");
//            writer.write(minutes + "\n");
//            writer.flush();
//        } catch (IOException e) {
//        }
//
//        AtomicBoolean timeReached = new AtomicBoolean(false); // Sử dụng AtomicBoolean để đảm bảo tính nhất quán trong thread
//        info.setText("Thời Gian Bảo Trì Tự Động : " + hours + ":" + minutes);
//        new Thread(() -> {
//            while (!timeReached.get()) { // Kiểm tra điều kiện dừng
//                try {
//                    LocalTime currentTime = LocalTime.now();
//                    int hourss = hoursComboBox.getItemAt(hoursComboBox.getSelectedIndex());
//                    int minutess = minutesComboBox.getItemAt(minutesComboBox.getSelectedIndex());
//                    int hour_now = currentTime.getHour();
//                    int minute_now = currentTime.getMinute();
//
//                    if (hourss == hour_now && minutess == minute_now) {
//                        performMaintenance();
//                        timeReached.set(true); // Gán giá trị true để dừng vòng lặp
//                    }
//                    Functions.sleep(10000);
//                } catch (Exception e) {
//                }
//            }
//        }).start();
//    }
//
//    private void performMaintenance() {
//        Maintenance.gI().start(15);
//
//    }
//
//    public static void runBatchFile(String batchFilePath) throws IOException {
//        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", batchFilePath);
//        Process process = processBuilder.start();
//        try {
//            process.waitFor();
//        } catch (InterruptedException e) {
//        }
//    }
//}
public class ServerManagerUI extends JFrame {

    private static class NavItem {
        String name;
        Icon icon;
        String key;

        public NavItem(String name, String iconPath, String key) {
            this.name = name;
            this.key = key;
            this.icon = ServerGuiUtils.loadIcon(iconPath);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Vẽ một mục trong thanh điều hướng.
     *
     * <p>Bản cũ dùng {@code DefaultListCellRenderer} và tô nền cả ô khi được
     * chọn, nên mục đang chọn là một khối chữ nhật trải hết chiều ngang —
     * trông như một dòng bảng hơn là một mục menu.</p>
     *
     * <p>Bản này tự vẽ: mục đang chọn là một <b>viền bo góc</b> có chừa lề hai
     * bên, kèm một vạch cam ngắn ở mép trái. Mục đang trỏ chuột được tô nhạt
     * hơn, nên thanh bên phản hồi khi rê chuột.</p>
     */
    private class NavRenderer extends JPanel implements ListCellRenderer<NavItem> {

        private NavItem muc;
        private boolean dangChon;
        private boolean dangTro;

        NavRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends NavItem> list,
                NavItem value, int index, boolean isSelected, boolean cellHasFocus) {
            this.muc = value;
            this.dangChon = isSelected;
            this.dangTro = (index == navHover) && !isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(UiTheme.SIDEBAR);
            g2.fillRect(0, 0, w, h);

            // Lề 10px hai bên để viên bo góc không dán vào mép cửa sổ.
            int le = 10;
            if (dangChon || dangTro) {
                g2.setColor(dangChon ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(le, 3, w - le * 2, h - 6, 10, 10));
            }
            if (dangChon) {
                // Vạch cam: dấu hiệu duy nhất cần nhìn thấy từ xa.
                g2.setColor(UiTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(le, 9, 3, h - 18, 3, 3));
            }

            if (muc == null) {
                g2.dispose();
                return;
            }

            int xIcon = le + 16;
            if (muc.icon != null) {
                int yIcon = (h - muc.icon.getIconHeight()) / 2;
                muc.icon.paintIcon(this, g2, xIcon, yIcon);
            }

            g2.setFont(UiTheme.ui(dangChon ? Font.BOLD : Font.PLAIN, 13));
            g2.setColor(dangChon ? Color.WHITE : UiTheme.TEXT_MUTED_DARK);
            int xChu = xIcon + 30;
            int yChu = (h + g2.getFontMetrics().getAscent()) / 2 - 2;
            g2.drawString(muc.name, xChu, yChu);

            g2.dispose();
        }
    }

    /**
     * Dải thương hiệu trên cùng thanh bên.
     *
     * <p>Vẽ dấu hiệu <b>ngọc rồng bốn sao</b> — quả cầu cam với bốn ngôi sao —
     * thay vì một hình khối chung chung. Đây là chi tiết làm bảng điều khiển
     * trông thuộc cùng sản phẩm với game, chứ không phải một công cụ quản trị
     * bất kỳ.</p>
     */
    private static class BrandHeader extends JPanel {

        BrandHeader() {
            setBackground(UiTheme.SIDEBAR_HEADER);
            setPreferredSize(new Dimension(258, 78));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.SIDEBAR_LINE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            veNgocRong(g2, 22, 22, 34);

            g2.setColor(Color.WHITE);
            g2.setFont(UiTheme.ui(Font.BOLD, 17));
            g2.drawString("DoTheAnh", 68, 34);

            g2.setColor(UiTheme.TEXT_MUTED_DARK);
            g2.setFont(UiTheme.ui(Font.PLAIN, 10));
            g2.drawString(giaNhan("SERVER CONTROL"), 68, 51);

            g2.dispose();
        }

        /** Ngọc rồng bốn sao: quả cầu cam, bốn ngôi sao đỏ. */
        private void veNgocRong(Graphics2D g2, int x, int y, int d) {
            g2.setColor(new Color(0xFF, 0xA8, 0x3A));
            g2.fill(new Ellipse2D.Float(x, y, d, d));
            // Vệt sáng chéo trên, cho quả cầu ra khối thay vì một đĩa phẳng.
            g2.setColor(new Color(255, 255, 255, 90));
            g2.fill(new Ellipse2D.Float(x + d * 0.18f, y + d * 0.14f,
                    d * 0.34f, d * 0.24f));
            g2.setColor(new Color(0xD8, 0x3A, 0x1E));
            float r = d * 0.115f;
            float cx = x + d / 2f;
            float cy = y + d / 2f;
            float k = d * 0.19f;
            veSao(g2, cx - k, cy - k, r);
            veSao(g2, cx + k, cy - k, r);
            veSao(g2, cx - k, cy + k, r);
            veSao(g2, cx + k, cy + k, r);
        }

        /** Một ngôi sao năm cánh, tâm tại (cx, cy), bán kính ngoài r. */
        private void veSao(Graphics2D g2, float cx, float cy, float r) {
            Path2D p = new Path2D.Float();
            for (int i = 0; i < 10; i++) {
                double goc = -Math.PI / 2 + i * Math.PI / 5;
                float bk = (i % 2 == 0) ? r : r * 0.42f;
                float px = cx + (float) (Math.cos(goc) * bk);
                float py = cy + (float) (Math.sin(goc) * bk);
                if (i == 0) {
                    p.moveTo(px, py);
                } else {
                    p.lineTo(px, py);
                }
            }
            p.closePath();
            g2.fill(p);
        }

        /** Chèn khoảng trắng giữa các chữ cho dòng phụ đề trông thưa hơn. */
        private String giaNhan(String chu) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chu.length(); i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(chu.charAt(i));
            }
            return sb.toString();
        }
    }

    /**
     * Thanh trạng thái ở chân thanh bên.
     *
     * <p>Chỗ này trước đây vẽ một chữ ký thương hiệu (dòng "Developed by", một
     * chữ ký vector và một dòng bản quyền), với hai dòng chữ mã hoá qua một
     * phép trừ/chia để không tìm được bằng {@code grep}.</p>
     *
     * <p>Nay hiện <b>bốn thứ người vận hành cần</b>: đèn báo trực tuyến, địa
     * chỉ đang gửi cho client, thời gian máy chủ đã chạy, và số người đang
     * chơi. Địa chỉ là thứ hay phải đi tìm nhất mỗi khi client báo không nối
     * được, nên nó nằm ngay đây thay vì phải mở file cấu hình.</p>
     *
     * <p>Đọc lại mỗi giây bằng một {@code javax.swing.Timer} — loại Timer này
     * chạy trên luồng giao diện nên không cần tự đồng bộ gì. Số người chơi lấy
     * từ bản chụp danh sách, tức là một lần sao chép mảng mỗi giây; với vài
     * nghìn người vẫn không đáng kể.</p>
     */
    private class ServerAddressPanel extends JPanel {

        ServerAddressPanel() {
            setBackground(UiTheme.SIDEBAR_HEADER);
            setPreferredSize(new Dimension(258, 92));
            setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.SIDEBAR_LINE));
            new javax.swing.Timer(1000, e -> repaint()).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            boolean len = ServerManager.isRunning;
            int x = 18;

            // --- dong 1: den bao + trang thai ---
            g2.setColor(len ? UiTheme.OK : UiTheme.DANGER);
            g2.fill(new Ellipse2D.Float(x, 16, 8, 8));
            g2.setColor(len ? UiTheme.OK : UiTheme.DANGER);
            g2.setFont(UiTheme.ui(Font.BOLD, 10));
            g2.drawString(len ? "TRỰC TUYẾN" : "ĐÃ DỪNG", x + 14, 24);

            g2.setColor(UiTheme.TEXT_MUTED_DARK);
            g2.setFont(UiTheme.ui(Font.PLAIN, 10));
            g2.drawString(thoiGianChay(), x + 92, 24);

            // --- dong 2: dia chi ---
            g2.setColor(UiTheme.ACCENT);
            g2.setFont(UiTheme.mono(Font.BOLD, 13));
            g2.drawString(ServerManager.IP + ":" + ServerManager.PORT, x, 48);

            // --- dong 3: so nguoi choi ---
            g2.setColor(UiTheme.TEXT_MUTED_DARK);
            g2.setFont(UiTheme.ui(Font.PLAIN, 10));
            g2.drawString(soNguoiChoi() + " người đang chơi", x, 68);

            g2.dispose();
        }

        /** Thời gian máy chủ đã chạy, dạng {@code 3d 04:21} hoặc {@code 04:21}. */
        private String thoiGianChay() {
            if (serverStartTime == null) {
                return "";
            }
            long giay = java.time.Duration.between(
                    serverStartTime, Instant.now()).getSeconds();
            long ngay = giay / 86400;
            long gio = (giay % 86400) / 3600;
            long phut = (giay % 3600) / 60;
            if (ngay > 0) {
                return String.format("%dd %02d:%02d", ngay, gio, phut);
            }
            return String.format("%02d:%02d", gio, phut);
        }

        /** Số người đang chơi, hoặc {@code "?"} nếu chưa dựng xong danh sách. */
        private String soNguoiChoi() {
            try {
                return String.valueOf(Client.gI().getPlayersSnapshot().size());
            } catch (Exception chuaSanSang) {
                // Cửa sổ hiện trước khi Client dựng xong danh sách -> chưa
                // biết số, nhưng không được để ngoại lệ làm chết vòng vẽ.
                return "?";
            }
        }
    }

    /**
     * Mục thanh điều hướng đang bị trỏ chuột, hoặc {@code -1}.
     *
     * <p>{@code JList} không có sẵn khái niệm "hover" như CSS, nên phải tự nhớ
     * chỉ số rồi vẽ lại — thiếu nó thì thanh bên không phản hồi gì khi rê
     * chuột và trông như một danh sách chết.</p>
     */
    private int navHover = -1;

    private final Instant serverStartTime;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JList<NavItem> sidebar;

    /**
     * Bản sao cũ của cờ tự khởi động lại.
     *
     * <p><b>Đã bỏ.</b> Trước đây project có <i>ba</i> bản sao của cờ này
     * ({@code ServerManagerUI}, {@code MenuUI}, {@code DashboardPanel}) và
     * {@code Maintenance} lại đọc bản của {@code DashboardPanel} — nên bật ở cửa
     * sổ này không có tác dụng gì. Nay chỉ còn một nguồn duy nhất là
     * {@link Maintenance#REQUEST_AUTO_RESTART}.</p>
     */
    public static boolean isAutoRestart() {
        return Maintenance.REQUEST_AUTO_RESTART;
    }

    /**
     * Cửa sổ panel đang mở, hoặc {@code null} nếu chạy không giao diện.
     *
     * <p>Cần một tham chiếu tĩnh để {@link #moLenTruoc()} gọi được từ luồng
     * game — chỗ xử lý mật khẩu quản trị nằm trong {@code Input}, không có
     * đường nào khác tới cửa sổ này.</p>
     */
    private static volatile ServerManagerUI CUA_SO;

    /**
     * Đưa panel lên trước màn hình.
     *
     * <p>Gọi khi người chơi nhập đúng mật khẩu Quyền Điều Hành Hệ Thống trong
     * game. Trả {@code false} nếu server chạy không giao diện
     * ({@code --headless}) — khi đó không có gì để mở.</p>
     *
     * <p><b>Panel nằm trên máy chủ.</b> Người chơi ở máy khác nhập đúng mật khẩu
     * thì cửa sổ bật lên ở máy chạy server, không phải ở máy họ.</p>
     */
    public static boolean moLenTruoc() {
        final ServerManagerUI f = CUA_SO;
        if (f == null) {
            return false;
        }
        try {
            EventQueue.invokeLater(() -> {
                if ((f.getExtendedState() & JFrame.ICONIFIED) != 0) {
                    f.setExtendedState(f.getExtendedState() & ~JFrame.ICONIFIED);
                }
                f.setVisible(true);
                // setAlwaysOnTop bat roi tat: Windows khong cho cua so nen
                // gianh tieu diem, day la cach duy nhat chac chan noi len tren
                // ma khong ghim cua so o tren mai mai.
                f.setAlwaysOnTop(true);
                f.toFront();
                f.requestFocus();
                f.setAlwaysOnTop(false);
            });
            return true;
        } catch (Exception ex) {
            Logger.logException(ServerManagerUI.class, ex, "Lỗi mở panel");
            return false;
        }
    }

    public ServerManagerUI() {
        super("Server Control Panel - Do The Anh");
        CUA_SO = this;
        setIconImage(createAppIconImage());
        ServerGuiUtils.setupTheme();
        initUI();
        startServerProcesses();
        this.serverStartTime = Instant.now();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Maintenance.REQUEST_AUTO_RESTART) {
                triggerRestartProcess();
            }
        }));
    }

    private Image createAppIconImage() {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(new Color(0, 120, 215));
        g2.fill(new RoundRectangle2D.Double(4, 4, 56, 56, 16, 16));
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(10, 10, 44, 44, 8, 8));
        g2.setColor(new Color(52, 152, 219, 50));
        Path2D area = new Path2D.Double();
        area.moveTo(15, 45);
        area.lineTo(22, 38);
        area.lineTo(29, 42);
        area.lineTo(38, 28);
        area.lineTo(38, 45);
        area.closePath();
        g2.fill(area);
        g2.setColor(new Color(41, 128, 185));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D line = new Path2D.Double();
        line.moveTo(15, 45);
        line.lineTo(22, 38);
        line.lineTo(29, 42);
        line.lineTo(38, 28);
        g2.draw(line);
        g2.setColor(new Color(46, 204, 113));
        g2.fill(new Ellipse2D.Double(43, 16, 6, 6));
        g2.setColor(new Color(243, 156, 18));
        g2.fill(new Ellipse2D.Double(43, 26, 6, 6));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(189, 195, 199));
        g2.drawLine(40, 38, 50, 38);
        g2.setColor(new Color(52, 73, 94));
        g2.fill(new Ellipse2D.Double(42, 36, 4, 4));
        g2.setColor(new Color(189, 195, 199));
        g2.drawLine(40, 45, 50, 45);
        g2.setColor(new Color(52, 73, 94));
        g2.fill(new Ellipse2D.Double(47, 43, 4, 4));
        g2.dispose();
        return image;
    }

    public void triggerRestartProcess() {
        int seconds = 5;
        System.out.println(">>> Restarting Server in " + seconds + "s...");
        try {
            String currentDir = System.getProperty("user.dir");
            String osName = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (osName.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "cmd", "/c", "timeout /t " + seconds + " /nobreak && run.bat");
            } else {
                pb = new ProcessBuilder("bash", "-c", "sleep " + seconds + "; ./run.sh &");
            }
            pb.directory(new File(currentDir));
            pb.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Toàn bộ panel cũ đã bị gỡ (xem docs/04-PANEL-MOI.md).
        // Giờ chỉ còn một mục duy nhất; giữ lại cơ chế sidebar + CardLayout để
        // sau này thêm panel mới chỉ cần thêm một dòng ở đây và một dòng ở dưới.
        // Mỗi mục MỘT icon riêng. Trước đây cả bảy mục đều truyền
        // "/icon/players.png", nên bộ vẽ icon dự phòng
        // (ServerGuiUtils.createFallbackIcon) luôn rơi vào nhánh "player" và
        // bảy icon giống hệt nhau — icon mất hết tác dụng phân biệt. Bộ vẽ đó
        // có sẵn 13 hình; dưới đây dùng đúng bảy hình khác nhau.
        NavItem[] menuItems = {
            new NavItem("Quản Lý Người Chơi", "/icon/player.png", "PlayerManager"),
            new NavItem("Quản Lý Tài Khoản", "/icon/account.png", "Account"),
            new NavItem("Quản Lý Vật Phẩm", "/icon/item.png", "ItemManager"),
            new NavItem("Quản Lý GiftCode", "/icon/gift.png", "GiftCode"),
            new NavItem("Quản Lý Bản Đồ", "/icon/map.png", "MapShop"),
            new NavItem("Lịch Sử Giao Dịch", "/icon/shop.png", "TradeHistory"),
            new NavItem("Cấu Hình Hệ Thống", "/icon/dashboard.png", "System"),
        };

        sidebar = new JList<>(menuItems);
        sidebar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sidebar.setSelectedIndex(0);
        sidebar.setFixedCellHeight(46);
        sidebar.setBackground(UiTheme.SIDEBAR);
        sidebar.setBorder(new EmptyBorder(8, 0, 8, 0));
        sidebar.setCellRenderer(new NavRenderer());

        // Theo dõi mục đang trỏ chuột. JList không có sẵn khái niệm "hover",
        // nên phải tự nhớ chỉ số và vẽ lại — thiếu nó thì thanh bên trông chết,
        // người dùng không biết chỗ nào bấm được.
        sidebar.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int moi = sidebar.locationToIndex(e.getPoint());
                if (moi >= 0 && !sidebar.getCellBounds(moi, moi).contains(e.getPoint())) {
                    moi = -1;
                }
                if (moi != navHover) {
                    navHover = moi;
                    sidebar.repaint();
                }
            }
        });
        sidebar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                navHover = -1;
                sidebar.repaint();
            }
        });

        JPanel sidebarContainer = new JPanel(new BorderLayout());
        sidebarContainer.setPreferredSize(new Dimension(258, getHeight()));
        sidebarContainer.setBackground(UiTheme.SIDEBAR);
        sidebarContainer.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, UiTheme.SIDEBAR_HEADER));

        sidebarContainer.add(new BrandHeader(), BorderLayout.NORTH);

        JScrollPane scrollSidebar = ServerGuiUtils.cuon(sidebar);
        scrollSidebar.setBorder(null);
        // Viewport phải tô cùng màu: chỗ trống dưới mục cuối là của viewport,
        // không phải của JList, nên để mặc định sẽ hở một dải trắng.
        scrollSidebar.getViewport().setBackground(UiTheme.SIDEBAR);
        scrollSidebar.setBackground(UiTheme.SIDEBAR);
        sidebarContainer.add(scrollSidebar, BorderLayout.CENTER);

        sidebarContainer.add(new ServerAddressPanel(), BorderLayout.SOUTH);

        add(sidebarContainer, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UiTheme.CONTENT_BG);
        // Chừa một viền xám quanh nội dung: các panel con đều nền trắng, nên
        // viền này làm chúng trông như một thẻ nổi trên nền thay vì dán sát mép.
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        contentPanel.add(new PlayerManagerPanel(), "PlayerManager");
        contentPanel.add(new AccountPanel(), "Account");
        contentPanel.add(new ItemManagerPanel(), "ItemManager");
        contentPanel.add(new GiftCodePanel(), "GiftCode");
        contentPanel.add(new MapShopPanel(), "MapShop");
        contentPanel.add(new TradeHistoryPanel(), "TradeHistory");
        contentPanel.add(new SystemPanel(), "System");

        add(contentPanel, BorderLayout.CENTER);

        sidebar.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                NavItem selected = sidebar.getSelectedValue();
                if (selected != null) {
                    cardLayout.show(contentPanel, selected.key);
                }
            }
        });

        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        ServerManagerUI.this,
                        "Bạn có chắc muốn dừng Server và thoát chương trình?",
                        "Xác nhận tắt Server",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    shutdownServer();
                }
            }
        });
    }

   private void startServerProcesses() {
    Logger.connect("Starting Server Engine...");

    new Thread(() -> {
        try {
            Logger.system("SERVER_UI", "Đang gọi ServerManager.gI().run()");
            ServerManager.gI().run();

            EventQueue.invokeLater(() -> setVisible(true));
        } catch (Exception e) {
            Logger.logException(ServerManagerUI.class, e, "Lỗi startServerProcesses");
        }
    }, "Thread Start Server Engine").start();
}

    private void shutdownServer() {
        try {
            System.out.println(">> Đang lưu dữ liệu và đóng kết nối...");
            if (ProxyManager.getInstance() != null) {
                ProxyManager.getInstance().stopAll();
            }
            if (DoTheAnhManager.getInstance() != null) {
                DoTheAnhManager.getInstance().stopAutoSave();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đóng tài nguyên: " + e.getMessage());
        }
        System.out.println(">> Server shutting down... Bye!");
        System.exit(0);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(ServerManagerUI::new);
    }
}