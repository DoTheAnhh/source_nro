package nro.server;

import nro.ui.ServerManagerUI;
import nro.service.boss.BossManager;
import nro.service.boss.BossNomalManager;
import nro.service.boss.BossOfTheGangsManager;
import nro.service.boss.BrolyManager;
import nro.service.boss.FinalBossManager;
import nro.service.boss.GasDestroyManager;
import nro.service.boss.OtherBossManager;
import nro.service.boss.RedRibbonHQManager;
import nro.service.boss.SkillSummonedManager;
import nro.service.boss.SnakeWayManager;
import nro.service.boss.TreasureUnderSeaManager;
import nro.service.boss.YardartManager;
import nro.core.log.Logger;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.dao.HistoryTransactionDAO;
import lombok.Getter;
import lombok.Setter;
import nro.net.Network;
import nro.net.api.ISession;
import nro.net.api.ISessionAcceptHandler;
import nro.net.io.MessageSendCollect;
import nro.net.io.MyKeyHandler;
import nro.net.session.MySession;
import nro.service.attribute.AttributeManager;
import nro.entity.boss.map.trainingboss.TopKillWhisManager;
import nro.gameplay.bot.BotManager;
import nro.gameplay.bot.newgen.BotAttackPlayer_1;
import nro.gameplay.bot.newgen.BotAttackPlayer_2;
import nro.gameplay.bot.newgen.BotAttackPlayer_3;
import nro.gameplay.bot.newgen.BotManager_new;
import nro.gameplay.bot.newgen.NewBot_new;
import nro.gameplay.bot.NewBot;
import nro.gameplay.bot.SellBot;
import nro.gameplay.bot.ShopBot;
import nro.entity.clan.Clan;
import nro.service.clan.ClanService;
import nro.service.consignmentstore.ConsignShopManager;
import nro.gameplay.giftcode.GiftCodeManager;
import nro.service.map.deathoralivearena.DeathOrAliveArenaManager;
import nro.entity.map.destrongas.TopDestronGas;
import nro.entity.map.dragonballnamec.NgocRongNamec;
import nro.service.map.ranksuper.SuperRankManager;
import nro.entity.map.snakeway.TopSnakeWay;
import nro.service.map.the23rdmartialartcongress.The23rdMartialArtCongressManager;
import nro.entity.map.treasureundersea.TopTreasureUnderSea;
import nro.service.map.worldmartialartstournament.WorldMartialArtsTournamentManager;
import nro.service.Service;

/**
 * Bộ khởi động và vòng đời của cả server.
 *
 * <p><b>Hai pha tách rời:</b></p>
 * <ol>
 *   <li>{@link #main(String[])} — sao lưu, đặt lịch, bật Anti-DDoS. Ở chế độ có
 *       GUI thì dừng ở đây và chờ người bấm nút; ở chế độ {@code --headless} thì
 *       gọi thẳng {@link #run()}.</li>
 *   <li>{@link #run()} — mở port, bật game loop, bật khoảng 50 thread nền
 *       (bot, boss, minigame, sự kiện).</li>
 * </ol>
 *
 * <p><b>Nợ kỹ thuật lớn nhất của lớp này — quản lý thread.</b> {@code run()} tạo
 * khoảng 50 thread bằng {@code new Thread(...).start()} thô, không qua pool nào.
 * Hệ quả: không đếm được, không giám sát được, không dừng được. {@link #close()}
 * chỉ {@code System.exit(0)} — mọi thread bị giết ngang. Xem giai đoạn 7 trong
 * docs/03-KE-HOACH-TAI-CAU-TRUC.md.</p>
 *
 * <p><b>Cấu hình nhúng cứng.</b> {@code IP}, {@code PORT}, {@code NAME},
 * {@code DOMAIN} là hằng số trong mã nguồn; đổi IP server là phải build lại.
 * ({@link #backupDatabase()} nay đã đọc tên DB, user, mật khẩu và đường dẫn
 * mysqldump từ {@code data/config/data_base.properties}, không còn nhúng cứng.)
 * Những thứ này nên nằm trong {@code config/server.properties}.</p>
 */
public class ServerManager {

    private static final long DELAY = 8000;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /** File cấu hình DB dùng chung với {@link ConnectDB} — nguồn duy nhất cho tên DB, user, mật khẩu. */
    private static final String CONFIG_DB_FILE = "data/config/data_base.properties";
    /** Tên tệp thực thi mysqldump theo hệ điều hành, dùng khi quét PATH. */
    private static final String[] MYSQLDUMP_NAMES = { "mysqldump.exe", "mysqldump" };

    /**
     * Lưới an toàn cuối cùng: vài vị trí cài đặt quen thuộc, chỉ dùng khi cả
     * {@code backup.mysqldump}, {@code XAMPP_HOME} lẫn {@code PATH} đều không
     * cho kết quả. Đây là danh sách để <i>dò</i>, không phải cấu hình — thiếu
     * hết thì server chỉ bỏ qua bước sao lưu chứ không chết.
     */
    private static final String[] MYSQLDUMP_CANDIDATES = {
        "C:/xampp/mysql/bin/mysqldump.exe",
        "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe",
        "C:/Program Files/MariaDB/bin/mysqldump.exe",
        "/opt/lampp/bin/mysqldump",
        "/usr/bin/mysqldump",
        "/usr/local/bin/mysqldump"
    };

    public static String timeStart;
    public int threadMap;

    /**
     * Đếm số kết nối đang mở theo IP, để chặn vượt {@code Manager.MAX_PER_IP}.
     *
     * <p><b>Ba vấn đề:</b></p>
     * <ul>
     *   <li>Kiểu thô ({@code Map} không tham số) nên giá trị phải chuyển đổi qua
     *       {@code Integer.parseInt(String.valueOf(o))} — vòng vo và tốn kém.</li>
     *   <li>{@code HashMap} <b>không đồng bộ</b>, nhưng bị ghi từ thread
     *       {@code Network} (khi accept) và từ các thread khác (khi ngắt).</li>
     *   <li>Chỉ thêm, không bao giờ xoá mục — mỗi IP từng kết nối giữ lại một mục
     *       vĩnh viễn.</li>
     * </ul>
     * <p>Nên đổi sang {@code ConcurrentHashMap<String, AtomicInteger>}.</p>
     */
    public static final Map CLIENTS = new HashMap();

    public static String NAME = "NRODoTheAnh";
    public static String IP = "103.10.198.119";
    public static int PORT = 14445;

    public static String DOMAIN = "NRODoTheAnh.VN";
    public static String NAME_SERVER = "NRODoTheAnh";

    private static ServerManager instance;

    public static ServerSocket listenSocket;
    public static boolean isRunning;

    @Getter
    @Setter
    private AttributeManager attributeManager;

    private long lastUpdateAttribute;
    private ExecutorService gameExecutorService;

    /**
     * Chuẩn bị dữ liệu trước khi server lên.
     *
     * <p>Nạp {@code Manager} (bản đồ, item template, clan...), khởi tạo giftcode,
     * rồi <b>đặt lại mốc đăng nhập/đăng xuất của mọi tài khoản</b> — cần thiết vì
     * lần chạy trước có thể đã tắt đột ngột, để lại các tài khoản mắc kẹt ở trạng
     * thái "đang online".</p>
     *
     * <p>Ở chế độ {@code Manager.LOCAL} thì thoát sớm, không đụng DB.</p>
     */
    public void init() {
        Manager.gI();
        GiftCodeManager.gI().init();

        try {
            if (Manager.LOCAL) {
                return;
            }

            ConnectDB.executeUpdate("update account set last_time_login = '2000-01-01', last_time_logout = '2001-01-01'");
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e);
        }

        HistoryTransactionDAO.deleteHistory();
    }

    /**
     * Singleton, và <b>gọi luôn {@link #init()}</b> ở lần tạo đầu tiên.
     *
     * <p>Nghĩa là lần chạm đầu tiên tới {@code ServerManager.gI()} sẽ nạp toàn bộ
     * dữ liệu game và chạy vài lệnh UPDATE trên DB — một lời gọi trông vô hại mà
     * mất vài giây. Không an toàn luồng như các {@code gI()} khác.</p>
     */
    public static ServerManager gI() {
        if (instance == null) {
            instance = new ServerManager();
            instance.init();
        }
        return instance;
    }

    /**
     * Điểm vào của toàn bộ server.
     *
     * <p>Có 2 chế độ chạy:</p>
     * <ul>
     *   <li><b>Mặc định (có GUI)</b> — mở cửa sổ {@link ServerManagerUI}. Hàm main
     *       kết thúc ngay sau đó; server chỉ thật sự lên khi người dùng bấm nút
     *       trong UI (nút đó gọi {@code ServerManager.gI().run()}).</li>
     *   <li><b>Headless</b> — chạy với tham số {@code --headless} (hoặc
     *       {@code --nogui}). Không mở cửa sổ nào, gọi thẳng {@link #run()}.
     *       Dùng cho VPS không có desktop, cho auto-restart và cho systemd/nssm.</li>
     * </ul>
     *
     * <p>Trình tự chung ở cả 2 chế độ: ép stdout sang UTF-8 (log tiếng Việt không
     * bị vỡ) → ghi mốc thời gian khởi động → sao lưu source + database →
     * đặt lịch reset clan hằng ngày → bật Anti-DDoS.</p>
     *
     * @param args tham số dòng lệnh; hiện chỉ nhận {@code --headless} / {@code --nogui}
     */
    /**
     * Ghi song song mọi thứ in ra console vào {@code log-run.txt}.
     *
     * <p>Phần đầu {@code run.bat} nói rằng nó ghi log ra {@code log-run.txt},
     * nhưng dòng lệnh trong đó <b>không hề</b> có chuyển hướng nào — nên máy chủ
     * chạy xong là toàn bộ log chỉ nằm trong cửa sổ cmd, đóng cửa sổ là mất. Muốn
     * xem lại một sự cố vừa xảy ra thì không có gì để xem.</p>
     *
     * <p>Làm ở phía Java chứ không sửa {@code run.bat}: như vậy log có mặt dù
     * máy chủ được bật bằng cách nào — {@code run.bat}, {@code khoi-dong-lai.bat},
     * hay chạy thẳng từ IDE — và <b>vẫn giữ nguyên chữ trên màn hình</b>, khác
     * với việc chuyển hướng bằng {@code >} vốn nuốt sạch output của cửa sổ.</p>
     *
     * <p>Ghi đè chứ không nối thêm: mỗi lần bật là một tệp mới, khỏi phải dò xem
     * đoạn nào thuộc lần chạy nào.</p>
     */
    private static void moNhatKyConsole() {
        try {
            final java.io.PrintStream manHinh = System.out;
            final java.io.PrintStream manHinhLoi = System.err;
            final java.io.OutputStream tep = new java.io.BufferedOutputStream(
                    new java.io.FileOutputStream("log-run.txt", false));

            java.io.OutputStream ca = new java.io.OutputStream() {
                @Override
                public void write(int b) throws java.io.IOException {
                    manHinh.write(b);
                    tep.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws java.io.IOException {
                    manHinh.write(b, off, len);
                    tep.write(b, off, len);
                }

                @Override
                public void flush() throws java.io.IOException {
                    manHinh.flush();
                    tep.flush();
                }
            };
            java.io.OutputStream caLoi = new java.io.OutputStream() {
                @Override
                public void write(int b) throws java.io.IOException {
                    manHinhLoi.write(b);
                    tep.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws java.io.IOException {
                    manHinhLoi.write(b, off, len);
                    tep.write(b, off, len);
                }

                @Override
                public void flush() throws java.io.IOException {
                    manHinhLoi.flush();
                    tep.flush();
                }
            };
            // autoFlush = true: sap thi log van con nguyen toi dong cuoi. De
            // false thi dung phan quan trong nhat — nhung dong ngay truoc luc
            // sap — con nam trong bo dem va mat theo tien trinh.
            System.setOut(new PrintStream(ca, true, "UTF-8"));
            System.setErr(new PrintStream(caLoi, true, "UTF-8"));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    tep.flush();
                    tep.close();
                } catch (Exception boQua) {
                    // Dang tat may chu, khong con cho nao de bao loi.
                }
            }));
        } catch (Exception e) {
            // Khong mo duoc tep thi van chay binh thuong, chi la khong co log.
            Logger.logException(ServerManager.class, e, "Khong mo duoc log-run.txt");
        }
    }

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Logger.logException(ServerManager.class, e);
        }
        moNhatKyConsole();

        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");

        boolean headless = false;
        if (args != null) {
            for (String arg : args) {
                if ("--headless".equalsIgnoreCase(arg) || "--nogui".equalsIgnoreCase(arg)) {
                    headless = true;
                    break;
                }
            }
        }

        Logger.title("SERVER START");

        if (!headless) {
            Logger.system("SERVER", "Khởi động ServerManagerUI");
            new nro.ui.ServerManagerUI().setVisible(true);
        } else {
            Logger.system("SERVER", "Chế độ headless — bỏ qua giao diện Swing");
        }

        Logger.title("BACKUP");
        backupSrcFolder();
        backupDatabase();

        scheduleDailyReset();

        try {
            ChongDdos.batDau();
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e, "Lỗi khởi tạo Anti-DDOS");
        }

        if (headless) {
            // Ở chế độ có GUI, run() được gọi từ nút trong ServerManagerUI/MenuUI.
            // Ở headless không có ai bấm nút nên phải tự gọi.
            Logger.system("SERVER", "Tự gọi ServerManager.gI().run()");
            gI().run();
        }
    }

    /**
     * Đưa server lên trạng thái hoạt động.
     *
     * <p><b>Thứ tự có ý nghĩa:</b> mở socket trước, rồi mới bật game loop và các
     * hệ thống nền — người chơi kết nối được ngay cả khi bot/boss chưa bật xong.</p>
     *
     * <p>Trình tự đầy đủ: mở port → game loop 500ms → tự cập nhật BXH 30s → tự lưu
     * DB 8s → dọn dữ liệu sự kiện đang tắt → bot → minigame → thread hệ thống →
     * boss thường → boss sự kiện.</p>
     *
     * <p><b>Không chống gọi hai lần.</b> Bấm nút khởi động hai lần trong UI sẽ bind
     * port lần nữa (lỗi) và nhân đôi toàn bộ ~50 thread. Nên có cờ bảo vệ.</p>
     */
    public void run() {
        isRunning = true;

        Logger.title("SERVER ONLINE");

        gameExecutorService = Executors.newCachedThreadPool();

        activeServerSocket();

        Logger.system("GAME", "Đang khởi động game loop");
        activeGame();

        Logger.system("BXH", "Đang bật tự động cập nhật bảng xếp hạng");
        autoUpdateBxh();

        Logger.system("AUTO_SAVE", "Đang bật tự động lưu database");
        AutoSavedGame();

        Logger.system("BOT", "Đang khởi động bot hệ thống");
        startCoreBots();
        startShopBots();
        startNewBots();


        startCoreSystemThreads();
        startBossManagers();
    }

    /** Bật 3 thread bot người chơi ảo (bot thường, bot mới, bot sự kiện). */
    private void startCoreBots() {
        new Thread(BotManager.gI(), "New Thread Bot Player").start();
        new Thread(BotManager_new.gI(), "Thread Bot Player_New").start();

    }

    /**
     * Bật bot mua/bán ở chợ.
     *
     * <p><b>Toàn bộ danh sách ~38 bot được nhúng cứng</b> dưới dạng
     * {@code new ShopBot(itemId, mapId, giá)}. Muốn đổi giá một món phải sửa Java
     * và build lại server. Đây là ứng viên rõ ràng cho việc đưa dữ liệu ra file
     * (giai đoạn 8) — chuyển thành {@code config/shop_bots.json} là chỉnh được
     * lúc chạy.</p>
     *
     * <p>Các số như {@code 457} là mapId, {@code Util.nextInt(20, 40)} là khoảng
     * giá ngẫu nhiên cho mỗi lần khởi động.</p>
     */
    private void startShopBots() {
        new Thread(() -> {
            NewBot.gI().runBot(0, null, null, 4000);

            NewBot.gI().runBot(1, new ShopBot(14, 457, 20), null, 1);
            NewBot.gI().runBot(1, new ShopBot(222, 457, 10), null, 1);
            NewBot.gI().runBot(1, new ShopBot(1154, 457, 3), null, 1);
            NewBot.gI().runBot(1, new ShopBot(2012, 457, Util.nextInt(20, 40)), null, 1);
            NewBot.gI().runBot(1, new ShopBot(1150, 457, 15), null, 1);
            NewBot.gI().runBot(1, new ShopBot(380, 457, Util.nextInt(19, 22)), null, 1);
            NewBot.gI().runBot(1, new ShopBot(17, 457, 5), null, 1);
            NewBot.gI().runBot(1, new ShopBot(1151, 457, 15), null, 1);
            NewBot.gI().runBot(1, new ShopBot(18, 457, 4), null, 1);
            NewBot.gI().runBot(1, new ShopBot(221, 457, 8), null, 1);
            NewBot.gI().runBot(1, new ShopBot(16, 457, 8), null, 1);
            NewBot.gI().runBot(1, new ShopBot(443, 457, 1), null, 1);
            NewBot.gI().runBot(1, new ShopBot(1153, 457, 15), null, 1);
            NewBot.gI().runBot(1, new ShopBot(1045, 457, 40), null, 1);
            NewBot.gI().runBot(1, new ShopBot(1152, 457, 15), null, 1);
            NewBot.gI().runBot(1, new ShopBot(15, 457, 5), null, 1);
            NewBot.gI().runBot(1, new ShopBot(220, 457, 5), null, 1);
            NewBot.gI().runBot(1, new ShopBot(447, 457, 3), null, 1);
            NewBot.gI().runBot(1, new ShopBot(987, 457, 10), null, 1);
            NewBot.gI().runBot(1, new ShopBot(380, 457, Util.nextInt(21, 25)), null, 1);
            NewBot.gI().runBot(1, new ShopBot(441, 457, 1), null, 1);
            NewBot.gI().runBot(1, new ShopBot(987, 457, Util.nextInt(8, 12)), null, 1);
            NewBot.gI().runBot(1, new ShopBot(442, 457, 1), null, 1);
            NewBot.gI().runBot(1, new ShopBot(443, 457, 1), null, 1);
            NewBot.gI().runBot(1, new ShopBot(20, 457, 2), null, 1);
            NewBot.gI().runBot(1, new ShopBot(223, 457, 8), null, 1);
            NewBot.gI().runBot(1, new ShopBot(19, 457, 3), null, 1);
            NewBot.gI().runBot(1, new ShopBot(987, 457, Util.nextInt(8, 10)), null, 1);
            NewBot.gI().runBot(1, new ShopBot(223, 457, 8), null, 1);
            NewBot.gI().runBot(1, new ShopBot(224, 457, 10), null, 1);
            NewBot.gI().runBot(1, new ShopBot(2012, 457, Util.nextInt(20, 40)), null, 1);

            NewBot.gI().runBot(3, null, new SellBot(457, 16, 1), 1);
            NewBot.gI().runBot(3, null, new SellBot(457, 19, 99), 1);
            NewBot.gI().runBot(3, null, new SellBot(457, 14, 1), 1);
            NewBot.gI().runBot(3, null, new SellBot(457, 18, 99), 1);
            NewBot.gI().runBot(3, null, new SellBot(457, 15, 1), 1);
            NewBot.gI().runBot(3, null, new SellBot(457, 20, 99), 1);
            NewBot.gI().runBot(3, null, new SellBot(457, 17, 99), 1);
        }, "Thread Shop Bot").start();

        Logger.system("BOT", "Đã bật shop bot thường");
    }

    /** Bật 14 bot thế hệ mới, trong đó 3 bot cuối là bot chủ động tấn công người chơi. */
    private void startNewBots() {
        new Thread(() -> {
            NewBot_new.gI().runBot_new(0, null, null, null, 0);
            NewBot_new.gI().runBot_new(1, null, null, null, 0);
            NewBot_new.gI().runBot_new(2, null, null, null, 0);
            NewBot_new.gI().runBot_new(3, null, null, null, 0);
            NewBot_new.gI().runBot_new(4, null, null, null, 0);
            NewBot_new.gI().runBot_new(5, null, null, null, 0);
            NewBot_new.gI().runBot_new(6, null, null, null, 0);
            NewBot_new.gI().runBot_new(7, null, null, null, 0);
            NewBot_new.gI().runBot_new(8, null, null, null, 0);
            NewBot_new.gI().runBot_new(9, null, null, null, 0);
            NewBot_new.gI().runBot_new(10, null, null, null, 0);
            NewBot_new.gI().runBot_new(11, new BotAttackPlayer_1(true), null, null, 0);
            NewBot_new.gI().runBot_new(12, null, new BotAttackPlayer_2(true), null, 0);
            NewBot_new.gI().runBot_new(13, null, null, new BotAttackPlayer_3(true), 0);
        }, "Thread Bot Player_New_New").start();

        Logger.system("BOT", "Đã bật NewBot_new");
    }


    /**
     * Bật 9 thread hệ thống: siêu hạng, đại hội võ thuật, ĐHVT23, võ đài sinh tử,
     * Chọn Ai Đây, ngọc rồng Namek, Tài Xỉu, bảo trì tự động, pariry.
     */
    private void startCoreSystemThreads() {
        new Thread(SuperRankManager.gI(), "Update Super Rank").start();
        new Thread(WorldMartialArtsTournamentManager.gI(), "Update WMAT").start();
        new Thread(The23rdMartialArtCongressManager.gI(), "Update DHVT23").start();
        new Thread(DeathOrAliveArenaManager.gI(), "Update Võ Đài Sinh Tử").start();
        new Thread(NgocRongNamec.gI(), "Update NRNM").start();
        new Thread(AutoMaintenance.gI(), "Thread Auto bảo trì tự động").start();

        Logger.system("THREAD", "Đã bật thread hệ thống chính");
    }

    /**
     * Nạp dữ liệu boss rồi bật 12 thread quản lý boss thường.
     *
     * <p><b>Thứ tự bắt buộc:</b> {@code loadBoss()} phải xong <i>trước</i>
     * {@code MAPS.forEach(Map::initBoss)}, vì gắn boss vào bản đồ cần dữ liệu boss
     * đã có sẵn. Đảo lại thì bản đồ sẽ không có boss nào mà cũng không báo lỗi.</p>
     */
    private void startBossManagers() {
        // Nap so lieu boss tu CSDL TRUOC khi dung con boss dau tien. Ham dung
        // Boss doc data[0] ngay tai cho, nap sau la con dau tien van chay bang
        // so cu — ma no chi sai voi mot con nen rat kho phat hien.
        int soMau = nro.repository.dao.BossDataDAO.napVaoBoNho();
        Logger.success("Đã áp " + soMau + " mẫu boss từ bảng boss_data\n");

        // Nap nhom TRUOC khi dung boss: ham dung ghi lai luot cua nhom.
        // Gieo boss theo ban do TRUOC khi Map.initBoss chay, khong thi lan chay
        // dau tien bang con trong va khong ban do nao co boss.
        nro.repository.dao.BossTheoMapDAO.gieoLanDau(new int[][]{
            {111, nro.entity.boss.BossID.TAU_PAY_PAY_DONG_NAM_KARIN},
            {114, nro.entity.boss.BossID.DRABURA},
            {115, nro.entity.boss.BossID.BUI_BUI},
            {117, nro.entity.boss.BossID.BUI_BUI_2},
            {118, nro.entity.boss.BossID.YA_CON},
            {119, nro.entity.boss.BossID.DRABURA_2},
            {120, nro.entity.boss.BossID.MABU_12H},
            {127, nro.entity.boss.BossID.MABU},
            {128, nro.entity.boss.BossID.SUPERBU},
            {131, nro.entity.boss.BossID.TAN_BINH_5},
            {132, nro.entity.boss.BossID.CHIEN_BINH_5},
            {133, nro.entity.boss.BossID.DOI_TRUONG_5}});

        nro.service.boss.NhomBossService.gI().napLai();

        BossManager.gI().loadBoss();
        Manager.MAPS.forEach(nro.entity.map.Map::initBoss);

        new Thread(BossManager.gI(), "Update boss").start();
        new Thread(YardartManager.gI(), "Update yardart boss").start();
        new Thread(FinalBossManager.gI(), "Update final boss").start();
        new Thread(SkillSummonedManager.gI(), "Update Skill-summoned boss").start();
        new Thread(BrolyManager.gI(), "Update broly boss").start();
        new Thread(OtherBossManager.gI(), "Update other boss").start();
        new Thread(RedRibbonHQManager.gI(), "Update reb ribbon hq boss").start();
        new Thread(TreasureUnderSeaManager.gI(), "Update treasure under sea boss").start();
        new Thread(SnakeWayManager.gI(), "Update snake way boss").start();
        new Thread(GasDestroyManager.gI(), "Update gas destroy boss").start();
        new Thread(BossOfTheGangsManager.gI(), "Update the gangs boss").start();
        new Thread(BossNomalManager.gI(), "Update nomal boss").start();

        Logger.system("BOSS", "Đã bật boss manager thường");
    }

    /**
     * Mở port và nối tầng mạng với tầng game.
     *
     * <p>Đây là <b>chỗ duy nhất</b> hai tầng gặp nhau:</p>
     * <ul>
     *   <li>{@code Controller.getInstance()} — nơi xử lý mọi gói tin.</li>
     *   <li>{@code new MessageSendCollect()} — codec <b>riêng cho từng session</b>
     *       (bắt buộc, vì codec giữ con trỏ khoá có trạng thái).</li>
     *   <li>{@code new MyKeyHandler()} — bắt tay khoá mã hoá.</li>
     * </ul>
     *
     * <p><b>Chỉ bật 2 trong 3 thread:</b> {@code startCollect()} và
     * {@code startQueueHandler()}. Thread gửi bật muộn hơn, trong
     * {@code MySession.sendKey()}, sau khi khoá đã ra khỏi socket.</p>
     *
     * <p>{@code sessionInit} chạy trên thread {@code Network} nên phải nhanh —
     * mọi client khác chờ trong lúc nó chạy.</p>
     */
    private void activeServerSocket() {
        try {
            Network.gI().init().setAcceptHandler(new ISessionAcceptHandler() {
                @Override
                public void sessionInit(ISession is) {
                    if (!canConnectWithIp(is.getIP())) {
                        Logger.warn("CONNECT", "Từ chối kết nối IP vượt giới hạn | ip=" + is.getIP());
                        is.disconnect();
                        return;
                    }

                    is.setMessageHandler(Controller.getInstance())
                            .setSendCollect(new MessageSendCollect())
                            .setKeyHandler(new MyKeyHandler())
                            .startCollect()
                            .startQueueHandler();
                }

                @Override
                public void sessionDisconnect(ISession session) {
                    Client.gI().kickSession((MySession) session);
                }
            }).setTypeSessionClone(MySession.class)
                    .setDoSomeThingWhenClose(() -> {
                        Logger.err("SERVER", "SERVER CLOSE");
                        System.exit(0);
                    })
                    .start(PORT);

            Logger.success("SERVER", "Server đang chạy tại port " + PORT);
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e, "Lỗi activeServerSocket");
        }
    }

    /**
     * Kiểm tra và tăng bộ đếm kết nối của một IP.
     *
     * <p><b>Không nguyên tử.</b> Đọc {@code get} rồi mới {@code put} — hai kết nối
     * từ cùng IP vào đúng cùng lúc đều thấy giá trị cũ và cùng được chấp nhận,
     * vượt qua giới hạn. Với {@code MAX_PER_IP} nhỏ thì đây là lỗ hổng thật.</p>
     *
     * <p>Việc chuyển kiểu {@code Integer.parseInt(String.valueOf(o))} là hệ quả của
     * {@code Map} kiểu thô — xem ghi chú ở trường {@code CLIENTS}.</p>
     *
     * @return {@code true} nếu IP này còn được phép kết nối
     */
    private boolean canConnectWithIp(String ipAddress) {
        // Da duoc chan o vong accept (Network.run -> ChongDdos), va lop do
        // dem bang AtomicInteger nen khong con ke ho "hai ket noi cung luc
        // deu thay gia tri cu" cua ban HashMap cu.
        //
        // Giu lai ham nay lam lop chan thu hai: den day ma van khong hop le
        // thi tu choi, chu khong bo han.
        return ipAddress != null && !ipAddress.isEmpty();
    }

    /**
     * Giảm bộ đếm kết nối của IP khi một session đóng.
     *
     * <p><b>Bắt buộc phải gọi</b>, nếu không bộ đếm chỉ tăng và IP đó sẽ bị chính
     * server chặn vĩnh viễn dù không còn kết nối nào. Được gọi từ
     * {@code Client.remove()}. Có chặn sàn ở 0 để tránh đếm âm khi gọi thừa.</p>
     */
    public void disconnect(MySession session) {
        if (session != null) {
            ChongDdos.ngatKetNoi(session.getIP());
        }
    }

    /**
     * Game loop chính: nhịp 500ms, chạy trên {@code gameExecutorService}.
     *
     * <p>Mỗi nhịp gọi {@code attributeManager.update()}, và cứ 10 phút thì lưu
     * chỉ số server xuống DB.</p>
     *
     * <p>Nhịp được giữ ổn định bằng cách trừ thời gian đã tốn: {@code update()}
     * chạy 100ms thì chỉ ngủ 400ms. Nếu {@code update()} vượt 500ms thì
     * {@code Thread.sleep} bị bỏ qua và vòng lặp chạy liên tục — <b>không có cảnh
     * báo nào</b> khi điều đó xảy ra. Nên log khi bị trễ nhịp.</p>
     */
    private void activeGame() {
        long delay = 500;
        gameExecutorService.submit(() -> {
            while (isRunning) {
                try {
                    long start = System.currentTimeMillis();
                    if (attributeManager != null) {
                        attributeManager.update();
                        if (Util.canDoWithTime(lastUpdateAttribute, 600000)) {
                            Manager.gI().updateAttributeServer();
                        }
                    }
                    long timeUpdate = System.currentTimeMillis() - start;
                    if (timeUpdate < delay) {
                        Thread.sleep(delay - timeUpdate);
                    }
                } catch (Exception e) {
                    Logger.logException(ServerManager.class, e);
                }
            }
        });
    }

    /**
     * Tự lưu DB mỗi 8 giây ({@code DELAY}).
     *
     * <p><b>Tên hàm sai quy ước</b> — method phải bắt đầu bằng chữ thường.</p>
     *
     * <p>Dùng chung {@code scheduler} một-thread với {@link #scheduleDatabaseBackup()}.
     * Nghĩa là nếu một lần sao lưu bằng {@code mysqldump} chạy lâu, việc tự lưu sẽ
     * <b>bị hoãn</b> cho tới khi sao lưu xong. Hai việc này nên ở hai pool riêng.</p>
     */
    private static void AutoSavedGame() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!isRunning) {
                    scheduler.shutdown();
                    Logger.warn("AUTO_SAVE", "Server đã dừng, tắt AutoSavedGame");
                    return;
                }

                Service.gI().AutoSavedDataBase();
            } catch (Exception e) {
                Logger.err("AUTO_SAVE", "Lỗi AutoSavedGame | " + e.getMessage());
            }
        }, 0, DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Tắt máy chủ rồi tự bật lại.
     *
     * <h3>Cách làm</h3>
     *
     * <p>Ghi một tệp {@code .bat} rồi phóng tệp đó <b>tách rời</b>, sau đó mới
     * gọi {@link #close()} để lưu dữ liệu và thoát. Tệp bat tự đợi vài giây:
     * cổng 14445 chỉ được nhả khi tiến trình cũ chết hẳn, bật ngay là bản mới
     * không chiếm được cổng và chết lặng.</p>
     *
     * <h3>Vì sao phải qua tệp bat</h3>
     *
     * <p>Bản trước nhét cả dòng lệnh vào <b>một</b> tham số của
     * {@code ProcessBuilder}:</p>
     *
     * <pre>new ProcessBuilder("cmd", "/c", "start", ..., "cmd", "/c", lenh)</pre>
     *
     * <p>Java bọc tham số đó trong dấu nháy và thoát mọi dấu nháy bên trong
     * thành {@code \"}. Nhưng {@code cmd.exe} <b>không hiểu</b> {@code \"} —
     * nó coi dấu chéo ngược là ký tự thường. Mà dòng lệnh bắt buộc có nháy vì
     * {@code java.home} là {@code C:\Program Files\...}, có dấu cách. Kết quả:
     * cửa sổ mới mở ra rồi tắt ngay, máy chủ cũ đã thoát, không ai bật lại —
     * đúng cảnh "tắt xong tắt hẳn".</p>
     *
     * <p>Viết ra tệp bat thì không còn nháy lồng nháy: mọi dấu nháy nằm trong
     * nội dung tệp, nơi {@code cmd} hiểu bình thường. Tệp cũng ở lại trên đĩa
     * nên hỏng thì mở ra xem được.</p>
     *
     * <p>Dòng lệnh dựng từ chính JVM đang chạy ({@code java.home},
     * {@code java.class.path}) chứ không gọi {@code run.bat}: như vậy bật lại
     * đúng bằng thứ đang chạy.</p>
     *
     * <p><b>Không</b> biên dịch lại mã nguồn — đây là bật lại, không phải dựng
     * lại. Sửa mã xong thì vẫn phải build.</p>
     */
    public void khoiDongLai() {
        try {
            java.io.File bat = taoBatKhoiDongLai();
            // start "" /min <bat> : tham so rong la TIEU DE cua so. Thieu no thi
            // cmd lay duong dan bat lam tieu de va khong chay gi ca.
            new ProcessBuilder("cmd", "/c", "start", "", "/min",
                    bat.getAbsolutePath())
                    .directory(bat.getParentFile())
                    .start();
            Logger.system("RESTART", "Da hen bat lai sau 5 giay qua "
                    + bat.getAbsolutePath());
        } catch (Exception ex) {
            Logger.logException(ServerManager.class, ex,
                    "Không hẹn được lượt bật lại — máy chủ sẽ chỉ tắt");
        }
        close();
    }

    /**
     * Ghi tệp bat bật lại và trả về đường dẫn của nó.
     *
     * <p>Tách khỏi { #khoiDongLai()} để kiểm tra được: gọi hàm này là có
     * tệp để mở ra xem, không phải tắt cả máy chủ mới biết nội dung đúng hay
     * sai.</p>
     */
    public static java.io.File taoBatKhoiDongLai() throws Exception {
        String thuMuc = System.getProperty("user.dir");
        String javaBin = System.getProperty("java.home")
                + java.io.File.separator + "bin"
                + java.io.File.separator + "java.exe";
        String cp = System.getProperty("java.class.path");

        java.io.File bat = new java.io.File(thuMuc, "khoi-dong-lai.bat");
        StringBuilder sb = new StringBuilder();
        sb.append("@echo off\r\n");
        sb.append("title NRO\r\n");
        sb.append("rem Tep nay do ServerManager.taoBatKhoiDongLai() tu ghi ra.\r\n");
        sb.append("rem Doi cho tien trinh cu nha cong 14445 roi moi bat lai.\r\n");
        sb.append("timeout /t 5 >nul\r\n");
        // cd /d: Java KHONG doi thu muc lam viec that khi dat -Duser.dir, nen
        // duong dan tuong doi kieu "data/..." chi dung neu tien trinh moi thuc
        // su dung o thu muc Server.
        sb.append("cd /d \"").append(thuMuc).append("\"\r\n");
        sb.append('"').append(javaBin).append('"')
                .append(" -Xms128m -Xmx3g -Xss256k")
                .append(" -XX:CompressedClassSpaceSize=128m")
                .append(" -XX:ReservedCodeCacheSize=128m")
                .append(" -XX:CICompilerCount=2")
                .append(" -cp \"").append(cp).append('"')
                .append(" nro.server.ServerManager")
                .append(" > server-console.log 2>&1\r\n");
        try (java.io.Writer w = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(bat), "windows-1252")) {
            w.write(sb.toString());
        }
        return bat;
    }


    /**
     * Tắt server có trình tự.
     *
     * <p>Thứ tự lưu: clan → shop ký gửi → chỉ số server → sự kiện → đá toàn bộ
     * người chơi (mỗi người được lưu trong {@code Client.close()}) → tắt executor.
     * Mỗi bước một khối {@code try} để một bước hỏng không chặn các bước sau —
     * đúng, vì đây là lần cuối dữ liệu được lưu.</p>
     *

     * <p>Nếu đang bảo trì tự động thì gọi lại {@code run.bat} để server tự lên lại.
     * Đây là cách lách cho việc <b>không có chế độ khởi động lại</b> — với cờ
     * {@code --headless} đã thêm, giờ có thể thay bằng dịch vụ hệ thống
     * (nssm/systemd) tự restart.</p>
     *
     * <p><b>Kết thúc bằng {@code System.exit(0)}</b>: mọi thread nền bị giết ngang.
     * Chấp nhận được vì mọi thứ quan trọng đã lưu ở trên, nhưng nghĩa là các hệ
     * thống có trạng thái trong bộ nhớ (boss đang chạy dở, minigame đang mở ván)
     * mất trạng thái đó.</p>
     */
    public void close() {
        isRunning = false;

        Logger.title("SERVER CLOSE");

        try {
            ClanService.gI().close();
            Logger.success("SAVE", "Lưu clan thành công");
        } catch (Exception e) {
            Logger.err("SAVE", "Lỗi save clan");
            Logger.logException(ServerManager.class, e);
        }

        try {
            ConsignShopManager.gI().save();
            Logger.success("SAVE", "Lưu shop ký gửi thành công");
        } catch (Exception e) {
            Logger.err("SAVE", "Lỗi save shop ký gửi");
            Logger.logException(ServerManager.class, e);
        }

        try {
            Manager.gI().updateAttributeServer();
            Logger.success("SAVE", "Lưu attribute server thành công");
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e);
        }

        try {
            Logger.success("SAVE", "Lưu event thành công");
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e, "Lỗi save event");
        }

        try {
            Client.gI().close();
            Logger.success("CLIENT", "Đã đóng toàn bộ client");
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e, "Lỗi close client");
        }

        try {
            if (gameExecutorService != null) {
                gameExecutorService.shutdown();
            }
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e, "Lỗi shutdown gameExecutorService");
        }

        Logger.success("SERVER", "BẢO TRÌ THÀNH CÔNG");

        if (AutoMaintenance.isRunning) {
            AutoMaintenance.isRunning = false;
            try {
                String batchFilePath = "run.bat";
                Logger.system("AUTO_MAINT", "Đang gọi lại file " + batchFilePath);
                FileRunner.runBatchFile(batchFilePath);
            } catch (IOException e) {
                Logger.logException(ServerManager.class, e, "Lỗi chạy lại run.bat");
            }
        }

        System.exit(0);
    }

    /**
     * Đếm tổng số nhân vật <b>trong DB</b> (không phải số người đang online).
     *
     * <p><b>Nuốt lỗi:</b> {@code catch (Exception e) {}} rỗng — DB hỏng thì hàm
     * trả về {@code 0} y như khi bảng thật sự rỗng.</p>
     */
    public long getNumPlayer() {
        long num = 0;
        try {
            CrisResultSet rs = ConnectDB.executeQuery("SELECT COUNT(*) FROM `player`");
            rs.first();
            num = rs.getLong(1);
        } catch (Exception e) {
        }
        return num;
    }

    /**
     * Nạp lại toàn bộ bảng xếp hạng mỗi 30 giây.
     *
     * <p><b>Tốn kém.</b> Chạy 5 lần nạp (BXH chung, Destron Gas, Kho Báu Dưới Biển,
     * Snake Way, Kill Whis) <i>bất kể có ai đang xem hay không</i>, mỗi lần là một
     * loạt truy vấn sắp xếp. Với 2.880 lượt mỗi ngày, đây là một trong những nguồn
     * tải DB lớn nhất của server.</p>
     *
     * <p>Nên giãn chu kỳ, hoặc chỉ nạp lại khi có người mở bảng xếp hạng.</p>
     */
    public void autoUpdateBxh() {
        ScheduledExecutorService autoTop = Executors.newScheduledThreadPool(1);
        autoTop.scheduleWithFixedDelay(() -> {
            try {
                TopServer.LoadingTop();

                TopDestronGas.getInstance().load();
                TopTreasureUnderSea.getInstance().load();
                TopSnakeWay.getInstance().load();
                TopKillWhisManager.getInstance().load();
            } catch (Exception e) {
                Logger.logException(ServerManager.class, e, "Auto load BXH error");
            }
        }, 0, 30000, TimeUnit.MILLISECONDS);
    }

    /**
     * Nén thư mục {@code src/} thành {@code backupsrc/src_backup_<thời gian>.zip}
     * mỗi lần khởi động.
     *
     * <p><b>Giờ đã thừa.</b> Project đã có git từ 22-08-2026, cho lịch sử đầy đủ
     * và diff được. Thư mục {@code backupsrc/} hiện chiếm <b>117 MB</b> và tiếp tục
     * phình mỗi lần chạy. Nên tắt hàm này, hoặc đổi thành tạo một git commit.</p>
     */
    private static void backupSrcFolder() {
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFilePath = "backupsrc/src_backup_" + dateTime + ".zip";
        String srcFolderPath = "src";

        try {
            File backupFolder = new File("backupsrc");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            Path srcFolder = Paths.get(srcFolderPath);

            if (!Files.exists(srcFolder)) {
                Logger.err("BACKUP_SRC", "Không tìm thấy thư mục src để sao lưu");
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(backupFilePath);
                 ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                Files.walk(srcFolder)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                ZipEntry zipEntry = new ZipEntry(srcFolder.relativize(path).toString());
                                zipOut.putNextEntry(zipEntry);
                                Files.copy(path, zipOut);
                                zipOut.closeEntry();
                            } catch (IOException e) {
                                Logger.err("BACKUP_SRC", "Lỗi khi sao lưu file: " + path + " | " + e.getMessage());
                            }
                        });
            }

            Logger.backup("Sao lưu source thành công | file=" + backupFilePath);
        } catch (IOException e) {
            Logger.err("BACKUP_SRC", "Lỗi khi sao lưu thư mục src | " + e.getMessage());
        }
    }

    /**
     * Sao lưu DB bằng {@code mysqldump}.
     *
     * <p>Toàn bộ thông tin kết nối (tên DB, user, mật khẩu) được đọc từ
     * {@code data/config/data_base.properties} — đúng file mà {@link ConnectDB}
     * dùng để kết nối. Trước đây các giá trị này bị nhúng cứng trong mã nguồn
     * ({@code dbName = "nro"}) nên mỗi lần backup đều hỏng với lỗi
     * {@code Unknown database 'nro'} trong khi server chạy trên DB khác.</p>
     *
     * <p>Đường dẫn {@code mysqldump.exe} lấy theo thứ tự ưu tiên:</p>
     * <ol>
     *   <li>khoá {@code backup.mysqldump} trong file properties (nếu có);</li>
     *   <li>các vị trí cài đặt XAMPP/MySQL quen thuộc.</li>
     * </ol>
     * <p>Nhờ vậy đổi máy hay đổi thư mục cài XAMPP không cần sửa lại mã nguồn.</p>
     *
     * <p>Đọc cả stdout lẫn stderr của tiến trình con — cần thiết, vì bộ đệm ống dẫn
     * đầy sẽ làm {@code mysqldump} treo vô hạn.</p>
     */
    private static void backupDatabase() {
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        Properties cfg = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_DB_FILE)) {
            cfg.load(in);
        } catch (IOException e) {
            Logger.err("BACKUP_DB", "Không đọc được " + CONFIG_DB_FILE + " | " + e.getMessage());
            return;
        }

        String dbName = cfg.getProperty("database.name");
        if (dbName == null || dbName.trim().isEmpty()) {
            Logger.err("BACKUP_DB", "Thiếu khoá database.name trong " + CONFIG_DB_FILE);
            return;
        }
        dbName = dbName.trim();
        String dbUser = cfg.getProperty("database.user", "root").trim();
        String dbPass = cfg.getProperty("database.pass", "");

        String mysqldumpPath = findMysqldump(cfg.getProperty("backup.mysqldump"));
        if (mysqldumpPath == null) {
            Logger.err("BACKUP_DB", "Không tìm thấy mysqldump.exe");
            Logger.warn("BACKUP_DB", "Khai báo đường dẫn đầy đủ qua khoá backup.mysqldump trong " + CONFIG_DB_FILE);
            return;
        }

        String backupFilePath = "backupsql/" + dbName + "_backup_" + dateTime + ".sql";

        try {
            File backupFolder = new File("backupsql");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            Logger.backup("Đang sao lưu database | db=" + dbName + " | file=" + backupFilePath);

            ProcessBuilder pb = new ProcessBuilder(
                    mysqldumpPath,
                    "-u", dbUser,
                    "--databases", dbName,
                    "--password=" + dbPass,
                    "--result-file=" + backupFilePath
            );

            pb.redirectErrorStream(false);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        Logger.info("BACKUP_DB", line);
                    }
                }

                while ((line = errorReader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        Logger.err("BACKUP_DB", line);
                    }
                }
            }

            int processComplete = process.waitFor();

            if (processComplete == 0) {
                Logger.success("BACKUP_DB", "Sao lưu database thành công | file=" + backupFilePath);
            } else {
                Logger.err("BACKUP_DB", "Lệnh mysqldump không thành công | exitCode=" + processComplete);
            }
        } catch (IOException e) {
            Logger.err("BACKUP_DB", "Lỗi khi thực thi mysqldump | " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.err("BACKUP_DB", "Backup database bị ngắt | " + e.getMessage());
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e, "Lỗi backup database");
        }
    }

    /**
     * Tìm {@code mysqldump} mà không nhúng cứng đường dẫn nào, theo thứ tự:
     *
     * <ol>
     *   <li>khoá {@code backup.mysqldump} trong {@value #CONFIG_DB_FILE} — cách
     *       ép cứng khi máy có nhiều bản MySQL;</li>
     *   <li>biến môi trường {@code XAMPP_HOME} (cùng biến mà các file .bat dùng);</li>
     *   <li>từng thư mục trong biến môi trường {@code PATH};</li>
     *   <li>vài vị trí cài đặt quen thuộc — chỉ là lưới an toàn cuối cùng.</li>
     * </ol>
     *
     * <p>Nhờ vậy chép cả thư mục dự án sang máy khác hay ổ đĩa khác vẫn chạy,
     * không cần sửa lại mã nguồn.</p>
     *
     * @param configured giá trị khoá {@code backup.mysqldump}, có thể {@code null}
     * @return đường dẫn tồn tại thật, hoặc {@code null} nếu không tìm được
     */
    private static String findMysqldump(String configured) {
        if (configured != null && !configured.trim().isEmpty()) {
            File f = new File(configured.trim());
            if (f.isFile()) {
                return f.getAbsolutePath();
            }
            Logger.warn("BACKUP_DB", "backup.mysqldump trỏ tới file không tồn tại | " + configured.trim());
        }

        String xampp = System.getenv("XAMPP_HOME");
        if (xampp != null && !xampp.trim().isEmpty()) {
            String hit = firstExisting(new File(xampp.trim(), "mysql/bin"));
            if (hit != null) {
                return hit;
            }
        }

        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(File.pathSeparator)) {
                if (dir.trim().isEmpty()) {
                    continue;
                }
                String hit = firstExisting(new File(dir.trim()));
                if (hit != null) {
                    return hit;
                }
            }
        }

        for (String candidate : MYSQLDUMP_CANDIDATES) {
            File f = new File(candidate);
            if (f.isFile()) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Trả về {@code mysqldump} nằm trong thư mục {@code dir}, thử lần lượt các
     * đuôi thực thi của từng hệ điều hành.
     *
     * @param dir thư mục cần kiểm tra
     * @return đường dẫn tuyệt đối nếu có, ngược lại {@code null}
     */
    private static String firstExisting(File dir) {
        for (String name : MYSQLDUMP_NAMES) {
            File f = new File(dir, name);
            if (f.isFile()) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Đặt lịch sao lưu DB mỗi 30 phút.
     *
     * <p><b>Không nơi nào gọi hàm này</b> — {@link #main} chỉ gọi
     * {@link #backupDatabase()} một lần lúc khởi động. Tức là sao lưu định kỳ
     * <b>đang không chạy</b>, dù code đã có sẵn.</p>
     *
     * <p>Nếu bật lên, lưu ý nó dùng chung {@code scheduler} với việc tự lưu game —
     * xem ghi chú ở {@link #AutoSavedGame()}.</p>
     */
    public void scheduleDatabaseBackup() {
        Logger.system("BACKUP_DB", "Đã bật tự động sao lưu database mỗi 30 phút");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                backupDatabase();
            } catch (Exception e) {
                Logger.logException(ServerManager.class, e, "Lỗi scheduleDatabaseBackup");
            }
        }, 0, 30, TimeUnit.MINUTES);
    }

    /**
     * Đặt lịch reset lượt boss clan lúc 00:00 hằng ngày.
     *
     * <p>Độ trễ lần đầu tính bằng khoảng cách tới nửa đêm kế tiếp, sau đó lặp mỗi
     * 24 giờ.</p>
     *
     * <p><b>Hai điểm yếu:</b> tạo thêm một scheduler riêng nữa (project đã có vài
     * cái rời rạc); và chu kỳ 24 giờ cố định sẽ <b>lệch dần</b> nếu máy đổi múi giờ
     * hoặc qua mốc giờ mùa hè.</p>
     */
    public static void scheduleDailyReset() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable resetTask = () -> {
            try {
                resetClanAllClans();
            } catch (Exception e) {
                Logger.logException(ServerManager.class, e, "Lỗi reset clan hằng ngày");
            }
        };

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();

        long initialDelay = Duration.between(now, nextMidnight).toMillis();
        long oneDay = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(resetTask, initialDelay, oneDay, TimeUnit.MILLISECONDS);

        Logger.system("SCHEDULE", "Đã đặt lịch reset clan hằng ngày lúc 00:00");
    }

    /**
     * Đặt {@code boss_clan_round = 1} cho mọi clan, cả trong DB lẫn trong bộ nhớ.
     *
     * <p><b>Phải làm cả hai:</b> chỉ cập nhật DB thì các clan đang nằm trong
     * {@code Manager.CLANS} vẫn giữ giá trị cũ cho tới lần khởi động sau.</p>
     *
     * <p>Đây là một trong số ít chỗ mở kết nối thủ công thay vì dùng
     * {@code ConnectDB.executeUpdate}. Khối {@code finally} có đóng đúng, nhưng
     * dùng {@code executeUpdate} sẽ ngắn hơn và ít rủi ro rò kết nối hơn.</p>
     */
    private static void resetClanAllClans() {
        PreparedStatement ps = null;
        Connection con = null;

        try {
            con = ConnectDB.getConnection();
            ps = con.prepareStatement("UPDATE clan SET Boss_clan = 1");

            int rows = ps.executeUpdate();

            for (Clan clan : Manager.CLANS) {
                clan.boss_clan_round = 1;
            }

            Logger.success("CLAN", "Đã reset boss_clan_round về 1 | totalClan=" + rows);
        } catch (SQLException e) {
            Logger.logException(Clan.class, e, "Lỗi khi resetBossClanAllClans");
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                Logger.logException(Clan.class, e, "Lỗi khi đóng kết nối resetBossClanAllClans");
            }
        }
    }

}
