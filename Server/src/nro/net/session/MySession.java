
package nro.net.session;

import java.net.Socket;
import nro.entity.player.Player;
import nro.server.Controller;
import nro.data.DataGame;
import nro.repository.schema.GodGK;
import nro.entity.item.Item;
import nro.server.Client;
import nro.server.Maintenance;
import nro.server.Manager;
import nro.service.Service;
import nro.core.log.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.server.AntiLogin;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import java.io.IOException;
import nro.repository.schema.DatabaseUpdater;
import nro.net.io.Message;

/**
 * Session của game — nối kết nối mạng với tài khoản và nhân vật.
 *
 * <p>{@link Session} chỉ biết byte. Lớp này thêm vào <b>mọi thứ thuộc về tài
 * khoản</b>: người chơi đang điều khiển, quyền admin, số dư nạp, vé tháng/vé
 * tuần, phiên bản client... và chứa toàn bộ <b>luồng đăng nhập</b>.</p>
 *
 * <p>Đây là lớp session duy nhất được dùng thật; đăng ký qua
 * {@code Network.setTypeSessionClone(MySession.class)}.</p>
 *
 * <p><b>Nợ kỹ thuật rõ nhất:</b> gần 40 trường {@code public} không có bao bọc.
 * Bất kỳ file nào trong 813 file cũng sửa được {@code session.vnd} hay
 * {@code session.isQuanTriVien}, nên không thể lần ra ai đã đổi giá trị.
 * Nên gom thành các nhóm ({@code AccountInfo}, {@code PaymentInfo},
 * {@code ClientInfo}) với getter/setter.</p>
 */
public class MySession extends Session {

    /**
     * Bộ đếm chống dò mật khẩu, theo <b>IP</b>.
     *
     * <p><b>Rò bộ nhớ:</b> map này chỉ thêm, không bao giờ xoá. Server chạy lâu
     * ngày sẽ tích một mục cho mọi IP từng thử đăng nhập. Nên dùng cache có hạn
     * mức hoặc dọn định kỳ.</p>
     *
     * <p>Truy cập từ nhiều thread {@code QueueHandler} nhưng là {@code HashMap}
     * trần, <b>không đồng bộ</b>.</p>
     */
    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();

    /**
     * Khoá theo <b>tên tài khoản</b> để hai lần đăng nhập cùng lúc không chen nhau.
     *
     * <p>Cũng chỉ thêm chứ không bao giờ xoá — mỗi username từng đăng nhập giữ lại
     * một đối tượng khoá vĩnh viễn.</p>
     */
    private static final Map<String, Object> LOGIN_LOCKS = new HashMap<>();

    /** Nhân vật đang điều khiển. {@code null} cho tới khi {@link #login} thành công. */
    public Player player;

    /** Đếm ngược trước khi vào game; đặt về 0 khi đăng nhập xong. */
    public byte timeWait = 100;

    /** <b>Trường thừa:</b> lớp cha {@link Session} đã có cờ kết nối riêng. Không được dùng. */
    public boolean connected;

    /** <b>Trường thừa:</b> trùng với cờ {@code sentKey} của lớp cha. */
    public boolean sentKey;

    /**
     * Khoá dùng cho {@link #sendSessionKey()} — <b>một byte 0 duy nhất</b>.
     *
     * <p>XOR với 0 là giữ nguyên, tức là đường này <b>không mã hoá gì cả</b>.
     * Khác hẳn khoá thật trong {@code Session} (chuỗi 24 ký tự).</p>
     */
    public static final byte[] KEYS = {0};

    /** <b>Trường thừa:</b> con trỏ khoá thật nằm trong {@code MessageSendCollect}. */
    public byte curR, curW;

    /** IP client. Trùng thông tin với {@code getIP()} của lớp cha nhưng lấy theo cách khác. */
    public String ipAddress;

    /** Quyền quản trị viên. */
    public boolean isQuanTriVien;

    /** Chủ server — bỏ qua giới hạn {@code MAX_PLAYER} khi đăng nhập. */
    public boolean isFounder;

    /** Tài khoản đang bị giam. */
    public boolean isJail;

    /** Khoá chính của tài khoản trong bảng {@code account}. Khác {@code player.id}. */
    public int userId;

    /**
     * Vừa tạo nhân vật xong và đang đăng nhập lại ngay.
     *
     * <p>{@code GodGK.login()} có cơ chế bắt chờ {@code SECOND_WAIT_LOGIN} giây
     * giữa hai lần đăng nhập của cùng một tài khoản. Sau khi tạo nhân vật,
     * {@code Controller.createChar()} gọi đăng nhập lại <b>ngay lập tức</b> nên
     * luôn rơi vào nhánh chờ đó: client đứng ở màn hình chờ và không bao giờ vào
     * game. Cờ này bỏ qua đúng lần đăng nhập ấy rồi tự tắt.</p>
     */
    public boolean vuaTaoNhanVat;

    /**
     * Tên đăng nhập và mật khẩu, <b>lưu nguyên văn trong bộ nhớ</b> suốt phiên.
     *
     * <p>Không cần giữ sau khi xác thực xong. Bất kỳ bản dump heap nào cũng lộ
     * mật khẩu của mọi người đang online. Nên xoá ngay sau khi {@code GodGK.login}
     * trả về.</p>
     */
    public String uu;
    public String pp;

    /** Thỏi vàng. */
    public int goldBar;

    /** Hồng ngọc. */
    public int ruby;

    /** Điểm VIP. */
    public int Vip_Point;

    // vé tháng vé tuần
    /** Còn hiệu lực vé tháng / vé tuần hay không, và mốc hết hạn (epoch millis). */
    public int vethang;
    public int vetuan;
    public long vethangExpire;
    public long vetuanExpire;

    /** Loại client (Android / iOS / PC...). */
    public int typeClient;

    /** Mức phóng to màn hình client báo lên. */
    public byte zoomLevel;

    /** Lần thoát gần nhất (epoch millis). */
    public long lastTimeLogout;

    /** Đã vào tới màn chơi hay chưa (khác với "đã kết nối"). */
    public boolean joinedGame;

    /** Mốc chống spam gói tin. */
    public long lastTimeReadMessage;

    /** Tài khoản đã kích hoạt. */
    public boolean actived;

    /** Quà chờ nhận, đọc từ DB lúc đăng nhập. */
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;

    /** Hệ số sức mạnh riêng của người chơi. */
    public double bdPlayer;

    /**
     * Phiên bản client.
     *
     * <p><b>Quan trọng:</b> quyết định độ rộng trường khi ghi gói tin. Ví dụ trong
     * {@code Controller}: {@code version >= 222} thì số lượng đọc bằng
     * {@code readInt}, ngược lại {@code readByte}. Sai giá trị này là lệch khung
     * gói tin. Xem {@code Message.writeCris}.</p>
     */
    public int version;

    /** Số dư nạp và các mốc nạp tích luỹ. */
    public int vnd;
    public int coin;
    public int Bar;
    public long timeout;
    public int tongnap;

    /** Tuổi tài khoản (ngày) và thời điểm tạo — dùng cho ưu đãi người mới. */
    public int accountAgeDays;
    public long timeCreateAcount;

    /** Nhiệm vụ đang theo. */
    public int getIdTask;

    /** Tổng đã nạp trong kỳ hiện tại. */
    public int danap;

    /** Đã chạy xong bước cập nhật dữ liệu lúc đăng nhập. */
    public boolean finishUpdate;

    /** 5 mốc nạp đã nhận thưởng. */
    public int[] mocnap = new int[]{0, 0, 0, 0, 0};

    /** Tổng nạp cách tính thứ hai (song song với {@link #tongnap}). */
    public int tongnap2;

    /**
     * Bọc một socket vừa accept.
     *
     * <p>Đây là constructor mà {@code SessionFactory} gọi qua reflection cho mọi
     * người chơi — <b>chữ ký {@code (Socket)} là bắt buộc</b>, đổi là hỏng lúc chạy.</p>
     */
    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
    }

    /**
     * Lấy (hoặc tạo) đối tượng khoá cho một tên tài khoản.
     *
     * <p>Chuẩn hoá tên về chữ thường và bỏ khoảng trắng thừa, để
     * {@code "Admin "} và {@code "admin"} dùng chung một khoá — nếu không thì
     * hai biến thể chữ hoa/thường sẽ lách được cơ chế chống đăng nhập trùng.</p>
     *
     * <p>Bản thân {@code LOGIN_LOCKS} được đồng bộ, nhưng nó không bao giờ được
     * dọn (xem ghi chú ở trường).</p>
     */
    private static Object getLoginLock(String username) {
        if (username == null) {
            username = "";
        }
        username = username.trim().toLowerCase();
        synchronized (LOGIN_LOCKS) {
            Object lock = LOGIN_LOCKS.get(username);
            if (lock == null) {
                lock = new Object();
                LOGIN_LOCKS.put(username, lock);
            }
            return lock;
        }
    }

    /**
     * Gửi khoá rồi <b>bật thread gửi</b>.
     *
     * <p>Đây là mắt xích quan trọng của trình tự khởi động session: {@code Sender}
     * cố tình chưa chạy lúc accept, vì gói khoá phải đi bằng {@code doSendMessage}
     * (ghi thẳng). Chỉ sau khi khoá đã ra khỏi socket, hàng đợi gửi mới an toàn.</p>
     *
     * <p>Gọi hàm này hai lần sẽ ném {@code IllegalThreadStateException} vì thread
     * đã chạy.</p>
     */
    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    /**
     * Gửi một "khoá" rỗng ({@link #KEYS} chỉ có một byte 0) bằng lệnh {@code -27}.
     *
     * <p><b>Không có nơi nào gọi hàm này</b> — bắt tay thật đi qua
     * {@code MyKeyHandler}. Giữ lại chắc để phục vụ một bản client cũ. Nếu đúng là
     * không dùng nữa thì nên xoá cùng với trường {@link #KEYS}, vì để đó dễ khiến
     * người sau tưởng đây mới là đường bắt tay.</p>
     */
    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (IOException e) {
        }
    }

    /**
     * Toàn bộ luồng đăng nhập.
     *
     * <p>Gọi từ {@code Controller} khi nhận gói đăng nhập, tức là chạy trên thread
     * {@code QueueHandler} của chính session này.</p>
     *
     * <p><b>Thứ tự kiểm tra (thoát sớm, rẻ trước đắt sau):</b></p>
     * <ol>
     *   <li>Tham số {@code null}.</li>
     *   <li>Chống dò mật khẩu theo IP ({@link #ANTILOGIN}).</li>
     *   <li>{@code Manager.LOCAL} — server này chỉ để lưu dữ liệu.</li>
     *   <li>{@code Maintenance.isRunning} — đang bảo trì.</li>
     *   <li>Quá tải {@code Manager.MAX_PLAYER} (chủ server được miễn).</li>
     *   <li>Session này đã đăng nhập rồi.</li>
     * </ol>
     *
     * <p><b>Vì sao phải khoá theo tên tài khoản.</b> Không có
     * {@code synchronized (getLoginLock(username))}, hai kết nối cùng gửi đăng nhập
     * một tài khoản trong cùng một khoảnh khắc sẽ cùng vượt qua vòng kiểm tra rồi
     * cùng nạp nhân vật — thành hai bản {@code Player} của cùng một người, và mọi
     * thay đổi ở bản này ghi đè bản kia (nhân đôi hoặc mất đồ). Bên trong khoá,
     * điều kiện {@code player != null} được kiểm tra <b>lại lần nữa</b> — đây là
     * mẫu double-checked, cố ý chứ không phải thừa.</p>
     *
     * <p><b>Các bước sau khi xác thực xong:</b> nạp nhân vật từ DB
     * ({@code GodGK.login}) → ghi vào danh sách online ({@code Client.safePut},
     * kick phiên cũ nếu trùng) → gửi phiên bản dữ liệu → tính lại chỉ số và hồi
     * đầy máu/mana cho cả nhân vật lẫn đệ tử → thả vào khu vực bản đồ → gửi thông
     * báo còn treo và hạn tìm set cho người mới.</p>
     *
     * <p><b>Nợ kỹ thuật:</b> hàm này làm quá nhiều việc — kiểm tra điều kiện, truy
     * vấn DB, dựng trạng thái nhân vật, gửi gói tin, ghi log. Nên tách thành
     * {@code validate()} / {@code authenticate()} / {@code enterGame()}.</p>
     *
     * @param username tên đăng nhập người chơi gõ
     * @param password mật khẩu người chơi gõ
     */
    /**
     * Đổi mật khẩu ngay ở màn đăng nhập, chưa vào game (lệnh 3 của messageNotLogin).
     *
     * <p>Chặn dò mật khẩu cùng một bộ đếm với đăng nhập ({@link #ANTILOGIN}): gõ
     * sai mật khẩu hiện tại cũng tính là một lần đăng nhập sai. Người đang online
     * bằng tài khoản ấy thì cập nhật luôn mật khẩu trong phiên.</p>
     */
    public void doiMatKhau(String tk, String cu, String moi) {
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
        tk = tk == null ? "" : tk.trim();
        cu = cu == null ? "" : cu.trim();
        moi = moi == null ? "" : moi.trim();
        if (tk.isEmpty() || cu.isEmpty()) {
            Service.gI().sendThongBaoOK(this, "Nhập tài khoản và mật khẩu hiện tại vào hai ô trước.");
            return;
        }
        if (moi.length() < 5) {
            Service.gI().sendThongBaoOK(this, "Mật khẩu mới phải có ít nhất 5 ký tự.");
            return;
        }
        if (moi.equals(cu)) {
            Service.gI().sendThongBaoOK(this, "Mật khẩu mới phải khác mật khẩu cũ.");
            return;
        }
        nro.repository.CrisResultSet rs = null;
        try {
            rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT id FROM account WHERE username = ? AND password = ?", tk, cu);
            if (!rs.next()) {
                al.wrong();
                Service.gI().sendThongBaoOK(this, "Tài khoản hoặc mật khẩu hiện tại không đúng.");
                return;
            }
            int id = rs.getInt("id");
            nro.repository.ConnectDB.executeUpdate(
                    "UPDATE account SET password = ? WHERE id = ?", moi, id);
            al.reset();
            for (Player p : Client.gI().getPlayers()) {
                MySession ss = p == null ? null : p.getSession();
                if (ss != null && tk.equals(ss.uu)) {
                    ss.pp = moi;
                }
            }
            Service.gI().sendThongBaoOK(this, "Đổi mật khẩu thành công!\nHãy đăng nhập bằng mật khẩu mới.");
        } catch (Exception e) {
            Logger.logException(MySession.class, e, "Lỗi đổi mật khẩu");
            Service.gI().sendThongBaoOK(this, "Máy chủ lỗi, chưa đổi được mật khẩu. Thử lại sau.");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void login(String username, String password) {
        if (username == null || password == null) {
            Service.gI().sendThongBaoOK(this, "Thông tin tài khoản hoặc mật khẩu không chính xác");
            Service.gI().sendLoginFail(this, false);
            return;
        }

        username = username.trim();
        password = password.trim();

        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }

//        if(!"admin".equals(username)){
//            Service.gI().sendThongBaoOK(this, "Hiện tại server đang được sửa lỗi, hẹn bạn trong vài phút!");
//            return;
//        }

        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRunning) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }
        if (!this.isFounder && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player != null) {
            Service.gI().sendThongBaoOK(this, "Phiên đăng nhập này đã vào game rồi");
            return;
        }

        synchronized (getLoginLock(username)) {
            // Kiểm tra lại bên trong khoá: giữa lần kiểm tra ở trên và lúc giành
            // được khoá, một thread khác có thể đã đăng nhập xong cho session này.
            if (this.player != null) {
                Service.gI().sendThongBaoOK(this, "Phiên đăng nhập này đã vào game rồi");
                return;
            }

            Player pl = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;

                // Xác thực + nạp toàn bộ dữ liệu nhân vật từ DB.
                // Trả null nghĩa là sai mật khẩu / bị khoá — GodGK đã tự gửi thông báo.
                pl = GodGK.login(this, al);
                if (pl == null) {
                    return;
                }

                pl.setSession(this);
                this.player = pl;

                // Thread-safe check and add player to Client
                // safePut theo chính sách "phiên mới thắng": nếu tài khoản đang
                // online ở nơi khác thì phiên cũ bị kick. Trả false nghĩa là
                // không ghi được vào danh sách online -> huỷ luôn lần đăng nhập này.
                if (!Client.gI().safePut(pl)) {
                    Service.gI().sendThongBaoOK(this, "Tài khoản này đang đăng nhập ở nơi khác");
                    Service.gI().sendLoginFail(this, true);
                    try {
                        pl.dispose();
                    } catch (Exception e) {
                    }
                    this.player = null;
                    return;
                }

                DataGame.sendSmallVersion(this);
                DataGame.sendBgItemVersion(this);

                this.timeWait = 0;
                this.joinedGame = true;

                // Tính lại chỉ số từ trang bị/tiềm năng rồi hồi đầy máu, mana.
                pl.nPoint.calPoint();
                pl.nPoint.setHp(Util.CrisGH(pl.nPoint.hp));
                pl.nPoint.setMp(Util.CrisGH(pl.nPoint.mp));

                // Đệ tử có bộ chỉ số riêng, phải tính lại y hệt.
                if (pl.Detu != null) {
                    pl.Detu.nPoint.calPoint();
                    pl.Detu.nPoint.setHp(Util.CrisGH(pl.Detu.nPoint.hp));
                    pl.Detu.nPoint.setMp(Util.CrisGH(pl.Detu.nPoint.mp));
                }

                // Thả nhân vật vào khu vực bản đồ -> từ đây người khác nhìn thấy.
                pl.zone.addPlayer(pl);

                DataGame.sendVersionGame(this);
                DataGame.sendDataItemBG(this);

                Logger.primary(TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin()
                        + "m: Login Succesfully Player : " + this.player.name
                        + " {" + (System.currentTimeMillis() - st) + " ms}\n");

                // Thông báo treo sẵn trong DB (ví dụ admin nhắn khi offline).
                // Gửi xong xoá đi để lần sau không gửi lại.
                if (this.player.notify != null
                        && !this.player.notify.equals("null")
                        && !this.player.notify.isEmpty()
                        && this.player.notify.length() > 0) {
                    Service.gI().sendThongBao(this.player, this.player.notify);
                    this.player.notify = null;
                }

                if (this.player.isNewMember) {
                    // Vẫn làm mới số ngày — hạn tìm set kích hoạt phụ thuộc nó,
                    // và vật phẩm 1784/1798 cộng thêm ngày cũng đọc từ đây.
                    // Chỉ BỎ câu thông báo đập vào mặt mỗi lần đăng nhập; ai
                    // cần xem hạn thì dùng vật phẩm cộng ngày là thấy.
                    DatabaseUpdater.refreshAccountAgeDays(this.player.getSession());
                }

                // Ai vao giua chung khong nghe duoc cau loa luc mo man, khong
                // co dong nay thi voi ho su kien la vo hinh.
                nro.service.event.SuKienService.chaoNguoiChoi(this.player);
            } catch (Exception e) {
                // Đăng nhập hỏng giữa chừng: huỷ nhân vật đã dựng dở và trả session
                // về trạng thái chưa đăng nhập, để người chơi thử lại được.
                Logger.logException(MySession.class, e, "Lỗi login");
                if (pl != null) {
                    try {
                        pl.dispose();
                    } catch (Exception ex) {
                    }
                }
                this.player = null;
            }
        }
    }
}
