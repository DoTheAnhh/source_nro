package nro.server;

import nro.bot.BotManager;
import Utils.Logger;
import VanKhaiManager.VanKhaiManager;
import event.EventManager;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import jbcd.dao.EventDAO;
import network.Network;
import network.session.MySession;
import network.session.SessionManager;
import nro.bot.New.BotManager_new;
import nro.clan.ClanService;
import nro.consignmentstore.ConsignShopManager;
import utils.Functions;

public final class ServerManagerUI extends JFrame {

    private Preferences preferences;
    private JLabel plCountLabel;
    private JLabel SessionCountLabel;
    private JLabel botCountLabel;
    private JLabel botCountLabel_new;
    private JLabel threadCountLabel;
    private JLabel EventCountLabel;
    private JLabel TypeDataCountLabel;
    private JTextField minutesField;
    private JLabel messageLabel;
    private JLabel countdownLabel;
    private Timer countdownTimer;
    private int remainingSeconds;
    private ButtonGroup maintenanceGroup;
    private JCheckBox maintenanceOption1;
    private JCheckBox maintenanceOption2;
    private JLabel info;
    public static boolean isRunning;

    public ServerManagerUI() {
        preferences = Preferences.userNodeForPackage(ServerManagerUI.class);
        setTitle("Ngọc Rồng Tuổi Thơ");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new GridLayout(0, 2));
        JButton maintenanceButton = new JButton("BẢO_TRÌ");
        maintenanceButton.addActionListener(e -> showMaintenanceDialog());
        panel.add(maintenanceButton);
        JButton maintenanceButton1 = new JButton("KICK_ALL_PLAYER");
        maintenanceButton1.addActionListener(e -> kick());
        panel.add(maintenanceButton1);
        JButton maintenanceButton2 = new JButton("THAY_EXP");
        maintenanceButton2.addActionListener(e -> tnsm());
        panel.add(maintenanceButton2);
        
        JButton maintenanceButton3 = new JButton("START_ANTIDDOS");
        maintenanceButton3.addActionListener(e -> startAntiDDoS());
        panel.add(maintenanceButton3);
        
        JButton saveButton = new JButton("SAVE_DATA");
        saveButton.addActionListener((ActionEvent e) -> {
            Logger.success("Đang tiến hành lưu data");
            Network.gI().stopConnect();
            
            Maintenance.isRunning = false;
            try {
                Logger.error("Đang tiến hành lưu data bang hội");
                ClanService.gI().close();
                Thread.sleep(1000);
                Logger.success("Lưu dữ liệu bang hội thành công");
            } catch (InterruptedException ex) {
                Logger.error("Lỗi lưu dữ liệu bang hội");
            }
            try {
                Logger.error("Đang tiến hành lưu data ký gửi");
                ConsignShopManager.gI().save();
                Thread.sleep(1000);
                Logger.success("Lưu dữ liệu ký gửi thành công");
            } catch (InterruptedException ex) {
                Logger.error("Lỗi lưu dữ liệu ký gửi");
            }
            
            try {
                Logger.error("Đang tiến hành đẩy người chơi");
                Client.gI().close();
                EventDAO.save();
                Thread.sleep(1000);
                Logger.success("Lưu dữ liệu người dùng thành công");
            } catch (InterruptedException ex) {
                Logger.error("Lỗi lưu dữ liệu người dùng");
            }
            System.exit(0);
        });
        panel.add(saveButton);
        
        JButton clearFw = new JButton("CLEAR_FIREWALL");
        clearFw.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(null, "Đã clear firewall");
        });
        panel.add(clearFw);
        
        JButton loadshop = new JButton("LOADING_SHOP");
        loadshop.addActionListener((ActionEvent e) -> {
            Manager.gI().updateShop();
            JOptionPane.showMessageDialog(null, "Đã Update Shop.");
        });
        panel.add(loadshop);
        
        JButton loadtop = new JButton("LOADING_TOP");
        loadtop.addActionListener((ActionEvent e) -> {
            TopServer.LoadingTop();
            JOptionPane.showMessageDialog(null, "Đã Update TOP.");
        });
        panel.add(loadtop);
        
        info = new JLabel("");
        // Đọc giá trị từ tệp tin
        try (BufferedReader reader = new BufferedReader(new FileReader("maintenanceConfig.txt"))) {
            String hoursLine = reader.readLine();
            String minutesLine = reader.readLine();

            int hours = Integer.parseInt(hoursLine);
            int minutes = Integer.parseInt(minutesLine);

            // Thêm giá trị vào DefaultComboBoxModel
            DefaultComboBoxModel<Integer> hoursModel = new DefaultComboBoxModel<>();
            for (int i = -1; i < 24; i++) {
                hoursModel.addElement(i);
            }
            JComboBox<Integer> hoursComboBox = new JComboBox<>(hoursModel);
            panel.add(hoursComboBox);
            hoursComboBox.setSelectedItem(hours);

            // Thêm giá trị vào DefaultComboBoxModel
            DefaultComboBoxModel<Integer> minutesModel = new DefaultComboBoxModel<>();
            for (int i = -1; i < 60; i++) {
                minutesModel.addElement(i);
            }
            JComboBox<Integer> minutesComboBox = new JComboBox<>(minutesModel);
            panel.add(minutesComboBox);
            minutesComboBox.setSelectedItem(minutes);
            JLabel jLabel2 = new JLabel("SETTING_AUTO_MAINTENACE");
            panel.add(jLabel2);
            JButton scheduleButton2 = new JButton("Hẹn Giờ Bảo Trì");
            scheduleButton2.addActionListener(e -> scheduleMaintenance(hoursComboBox, minutesComboBox));
            panel.add(scheduleButton2);
            if (hours != -1 && minutes != -1) {
                scheduleMaintenance(hoursComboBox, minutesComboBox);
            }
        } catch (IOException e) {
        }
        
        // Tạo DefaultComboBoxModel với các sự kiện
        DefaultComboBoxModel<String> eventModel = new DefaultComboBoxModel<>();
        eventModel.addElement("Chọn Sự Kiện");
        eventModel.addElement("Sự Kiện Tết Nguyên Đán");
        eventModel.addElement("Sự Kiện Noel Giáng Sinh");
        eventModel.addElement("Sự Kiện Halloween");
        JComboBox<String> eventComboBox = new JComboBox<>(eventModel);
        eventComboBox.addActionListener((ActionEvent e) -> {
        String selectedEvent = (String) eventComboBox.getSelectedItem();
            handleEventSelection(selectedEvent);  // Gọi hàm xử lý khi chọn sự kiện
        });
        panel.add(eventComboBox);
        
        EventCountLabel = new JLabel("\nSự Kiện Hiện Tại : ");
        panel.add(EventCountLabel);
        
        // Tạo DefaultComboBoxModel với các sự kiện
        DefaultComboBoxModel<String> datatype = new DefaultComboBoxModel<>();
        datatype.addElement("Chọn Kiểu Dữ Liệu");
        datatype.addElement("int");
        datatype.addElement("long");
        datatype.addElement("double");
        JComboBox<String> datatypeComboBox = new JComboBox<>(datatype);
        datatypeComboBox.addActionListener((ActionEvent e) -> {
        String selecteddatatype = (String) datatypeComboBox.getSelectedItem();
            handleDataTypeSelection(selecteddatatype);  // Gọi hàm xử lý khi chọn sự kiện
        });
        panel.add(datatypeComboBox);
        
        TypeDataCountLabel = new JLabel("\nKiểu Dữ Liệu Hiện Tại : ");
        panel.add(TypeDataCountLabel);
        
        JButton Login = new JButton("LOGIN_PLAYER");
        Login.addActionListener((ActionEvent e) -> {
            int size = 0;
            MySession session;
            try {
                session = new MySession(new Socket("127.0.0.1", 14445));
                session.version = 240;
                session.login("1", "1");
                if (session.isConnected() && session.player != null) {
                    Manager.player = session.player;
                    Manager.player.isBotLogin = true;
                    size ++;
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ServerManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                session = new MySession(new Socket("127.0.0.1", 14445));
                session.version = 240;
                session.login("3", "1");
                if (session.isConnected() && session.player != null) {
                    Manager.player = session.player;
                    Manager.player.isBotLogin = true;
                    size ++;
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ServerManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            JOptionPane.showMessageDialog(null, "Đã Login Thành Công " + size + " Người Chơi.");
        });
        panel.add(Login);
        
        messageLabel = new JLabel();
        panel.add(messageLabel);

        countdownLabel = new JLabel();
        panel.add(countdownLabel);
        
        panel.add(info);
        threadCountLabel = new JLabel("Số Thread : ");
        panel.add(threadCountLabel);
        
        plCountLabel = new JLabel("Số Người Online : ");
        panel.add(plCountLabel);
        SessionCountLabel = new JLabel("Session : ");
        panel.add(SessionCountLabel);
        
        botCountLabel = new JLabel("\nTổng Số Bot Hoạt Động : ");
        panel.add(botCountLabel);
        botCountLabel_new = new JLabel("\nTổng Số Bot_New Hoạt Động : ");
        panel.add(botCountLabel_new);
        
        ScheduledExecutorService EventCountExecutor = Executors.newSingleThreadScheduledExecutor();
        EventCountExecutor.scheduleAtFixedRate(() -> {
            EventCountLabel.setText("Kiểu Dữ Liệu Hiện Tại : " + (EventManager.LUNNAR_NEW_YEAR ? "Tết Nguyên Đán" : "Không Có Sự Kiện"));
        }, 1, 1, TimeUnit.SECONDS);
        
        ScheduledExecutorService typedataCountExecutor = Executors.newSingleThreadScheduledExecutor();
        typedataCountExecutor.scheduleAtFixedRate(() -> {
            boolean typedata = Manager.readInt;
            TypeDataCountLabel.setText("Kiểu Dữ Liệu Hiện Tại : " + (typedata ? "int" : "long"));
        }, 1, 1, TimeUnit.SECONDS);

        ScheduledExecutorService threadCountExecutor = Executors.newSingleThreadScheduledExecutor();
        threadCountExecutor.scheduleAtFixedRate(() -> {
            int threadCount = Thread.activeCount();
            threadCountLabel.setText("Số Thread : " + threadCount);
        }, 1, 1, TimeUnit.SECONDS);

        ScheduledExecutorService plCountExecutor = Executors.newSingleThreadScheduledExecutor();
        plCountExecutor.scheduleAtFixedRate(() -> {
            int plcount = Client.gI().getPlayers().size();
            plCountLabel.setText("Số Người Online : " + plcount);
        }, 5, 1, TimeUnit.SECONDS);
        ScheduledExecutorService ssCountExecutor = Executors.newSingleThreadScheduledExecutor();
        ssCountExecutor.scheduleAtFixedRate(() -> {
            int sscount = SessionManager.gI().getSessions().size();
            SessionCountLabel.setText("Session : " + sscount);
        }, 5, 1, TimeUnit.SECONDS);
        
        ScheduledExecutorService botCountExecutor = Executors.newSingleThreadScheduledExecutor();
        botCountExecutor.scheduleAtFixedRate(() -> {
            int botcount = BotManager.gI().getBot().size();
            botCountLabel.setText("\nTổng Số Bot Hoạt Động : " + botcount);
        }, 5, 1, TimeUnit.SECONDS);
        ScheduledExecutorService botCountExecutor_new = Executors.newSingleThreadScheduledExecutor();
        botCountExecutor_new.scheduleAtFixedRate(() -> {
            int botcount = BotManager_new.gI().getBot().size();
            botCountLabel_new.setText("\nTổng Số Bot_New Hoạt Động : " + botcount);
        }, 5, 1, TimeUnit.SECONDS);
        
        messageLabel.setText("Server đang chạy tại port : " + ServerManager.PORT);
        
        setVisible(true);
        ServerManager.gI().run();
        
        // Đọc giá trị từ tệp
        VanKhaiManager.getInstance().startAutoSave();
    }
    
    private void handleDataTypeSelection(String selecteddata) {
        // Thực hiện hành động tùy theo sự kiện đã chọn
        switch (selecteddata) {
            case "Chọn Kiểu Dữ Liệu":
                break;
            case "int":
                Manager.readInt = true;
                JOptionPane.showMessageDialog(this, "Thành công!");
                break;
            case "long":
                Manager.readInt = false;
                JOptionPane.showMessageDialog(this, "Thành công!");
                break;
            case "double":
                JOptionPane.showMessageDialog(this, "Thành công!");                
                break;
            default:
                JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ");
                break;
        }
    }
    
    private void handleEventSelection(String selectedEvent) {
        // Thực hiện hành động tùy theo sự kiện đã chọn
        switch (selectedEvent) {
            case "Chọn Sự Kiện":
                EventManager.CHRISTMAS = false;
                EventManager.HALLOWEEN = false;
                EventManager.HUNG_VUONG = false;
                EventManager.INTERNATIONAL_WOMANS_DAY = false;
                EventManager.LUNNAR_NEW_YEAR = false;
                EventManager.TRUNG_THU = false;
                EventManager.gI().init();
                break;
            case "Sự Kiện Tết Nguyên Đán":
                EventManager.LUNNAR_NEW_YEAR = true;
                EventManager.CHRISTMAS = false;
                EventManager.HALLOWEEN = false;
                EventManager.HUNG_VUONG = false;
                EventManager.INTERNATIONAL_WOMANS_DAY = false;
                EventManager.TRUNG_THU = false;
                EventManager.gI().init();
                JOptionPane.showMessageDialog(this, "Bạn đã chọn Sự kiện Tết Nguyên Đán");
                Logger.success("Máy chủ đang diễn ra sự kiện Tết Nguyên Đán");
                break;
            case "Sự Kiện Noel Giáng Sinh":
                EventManager.HALLOWEEN = false;
                EventManager.HUNG_VUONG = false;
                EventManager.INTERNATIONAL_WOMANS_DAY = false;
                EventManager.LUNNAR_NEW_YEAR = false;
                EventManager.TRUNG_THU = false;
                EventManager.CHRISTMAS = true;
                JOptionPane.showMessageDialog(this, "Bạn đã chọn Sự kiện Noel Giáng Sinh");
                Logger.success("Máy chủ đang diễn ra sự kiện Noel Giáng Sinh");
                
                break;
            case "Sự Kiện Halloween":
                EventManager.CHRISTMAS = false;
                EventManager.HUNG_VUONG = false;
                EventManager.INTERNATIONAL_WOMANS_DAY = false;
                EventManager.LUNNAR_NEW_YEAR = false;
                EventManager.TRUNG_THU = false;
                EventManager.HALLOWEEN = true;
                JOptionPane.showMessageDialog(this, "Bạn đã chọn Sự kiện Halloween");
                Logger.success("Máy chủ đang diễn ra sự kiện Halloween");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Lựa chọn không hợp lệ");
                break;
        }
    }

    private void showMaintenanceDialog() {
        try {
            int dialogButton = JOptionPane.YES_NO_OPTION;
            int dialogResult = JOptionPane.showConfirmDialog(this, "Bắt đầu bảo trì?", "Bảo trì", dialogButton);
            if (dialogResult == 0) {
                Logger.error("Server tiến hành bảo trì");
                Maintenance.gI().start(5);

            } else {
                System.out.println("No Option");
            }
        } catch (HeadlessException e) {
        }
    }

    private void kick() {
        new Thread(() -> {
            Client.gI().close();
        }).start();

    }

    private void tnsm() {
        String exp = JOptionPane.showInputDialog(this, "Bảng Exp Server\n"
                + "Exp Server hiện tại: " + Manager.RATE_EXP_SERVER + "\nExp Server Tối Thiểu Là 100");
        if (exp != null) {
            Manager.RATE_EXP_SERVER = Byte.parseByte(exp);
            Logger.error("Exp hiện tại là: " + exp + "\n");
        }

    }
    public static void startAntiDDoS() {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = "cmd /c start run_chongddosvv.bat";
            rt.exec(command);
            Logger.success("Đã bật chống DDOS");
        } catch (IOException ex) {
            Logger.error("Không thể bật chống DDOS");
        }
    }

    private void confirmExit() {
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int dialogResult = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát chương trình?", "Thoát", dialogButton);
        if (dialogResult == 0) {
            System.exit(0);
        }
    }

    @Override
    public void setDefaultCloseOperation(int operation) {
        if (operation == JFrame.EXIT_ON_CLOSE) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    confirmExit();
                }
            });
        } else {
            super.setDefaultCloseOperation(operation);
        }
    }

    private void scheduleMaintenance(JComboBox<Integer> hoursComboBox, JComboBox<Integer> minutesComboBox) {
        int hours = hoursComboBox.getItemAt(hoursComboBox.getSelectedIndex());
        int minutes = minutesComboBox.getItemAt(minutesComboBox.getSelectedIndex());
        if (minutes == -1 || hours == -1) {
            JOptionPane.showMessageDialog(this, "Thời gian sai");
            return;
        }
        // Ghi giá trị vào tệp tin
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("maintenanceConfig.txt"))) {
            writer.write(hours + "\n");
            writer.write(minutes + "\n");
            writer.flush();
        } catch (IOException e) {
        }

        AtomicBoolean timeReached = new AtomicBoolean(false); // Sử dụng AtomicBoolean để đảm bảo tính nhất quán trong thread
        info.setText("Thời Gian Bảo Trì Tự Động : " + hours + ":" + minutes);
        new Thread(() -> {
            while (!timeReached.get()) { // Kiểm tra điều kiện dừng
                try {
                    LocalTime currentTime = LocalTime.now();
                    int hourss = hoursComboBox.getItemAt(hoursComboBox.getSelectedIndex());
                    int minutess = minutesComboBox.getItemAt(minutesComboBox.getSelectedIndex());
                    int hour_now = currentTime.getHour();
                    int minute_now = currentTime.getMinute();

                    if (hourss == hour_now && minutess == minute_now) {
                        performMaintenance();
                        timeReached.set(true); // Gán giá trị true để dừng vòng lặp
                    }
                    Functions.sleep(10000);
                } catch (Exception e) {
                }
            }
        }).start();
    }

    private void performMaintenance() {
        Maintenance.gI().start(15);

    }

    public static void runBatchFile(String batchFilePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", batchFilePath);
        Process process = processBuilder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
        }
    }
}
