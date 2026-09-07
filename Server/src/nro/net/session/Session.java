package nro.net.session;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import nro.net.api.IKeySessionHandler;
import nro.net.api.IMessageHandler;
import nro.net.api.IMessageSendCollect;
import nro.net.api.ISession;
import nro.net.Collector;
import nro.net.QueueHandler;
import nro.net.Sender;
import nro.net.SocketType;
import nro.net.io.Message;

/**
 * Một kết nối TCP kèm ba thread phục vụ nó.
 *
 * <pre>
 *                    +----------------+
 *   socket đọc  ---> |   Collector    | ---> hàng đợi vào
 *                    +----------------+           |
 *                                                 v
 *                    +----------------+   IMessageHandler (Controller)
 *                    |  QueueHandler  | ---------------------------->
 *                    +----------------+
 *
 *   hàng đợi ra <--- sendMessage()  <--- logic game
 *        |
 *        v           +----------------+
 *                    |     Sender     | ---> socket ghi
 *                    +----------------+
 * </pre>
 *
 * <p><b>Hai constructor cho hai vai:</b> một cho server (nhận socket đã accept),
 * một cho client (tự đi kết nối ra). Chỉ phía server được dùng trong game.</p>
 *
 * <p><b>Chi phí:</b> 3 thread mỗi kết nối. Với 500 người online là 1.500 thread,
 * mỗi thread một ngăn xếp (server chạy {@code -Xss256k}) — khoảng 375 MB chỉ để
 * chứa ngăn xếp. Đây là điểm nghẽn mở rộng chính của server. Xem
 * docs/02-AUDIT-VAN-DE.md §C2.</p>
 *
 * <p>Lớp con thực dùng là {@link MySession} — nó thêm dữ liệu tài khoản và người chơi.</p>
 */
public class Session implements ISession {

    /**
     * <b>Trường chết.</b> Không bao giờ được gán, nên {@link #gI()} luôn ném ngoại lệ.
     * Có thể xoá cả trường lẫn {@link #gI()}.
     */
    private static ISession instance;

    /**
     * Bộ đếm cấp ID, tăng dần.
     *
     * <p><b>Không an toàn luồng:</b> {@code ID_INIT++} không nguyên tử. Hai kết nối
     * vào cùng lúc có thể nhận <b>trùng ID</b>, làm {@code SessionManager.findByID}
     * trả nhầm session. Nên dùng {@code AtomicInteger}.</p>
     */
    private static int ID_INIT;

    /** Vai của kết nối. Quyết định chiều bắt tay khoá. */
    private SocketType socketType = SocketType.SERVER;

    /**
     * Khoá XOR dùng cho mọi byte sau bắt tay.
     *
     * <p>Giá trị mặc định là một chuỗi <b>nhúng cứng trong mã nguồn</b> — giống hệt
     * nhau cho mọi kết nối, mọi lần chạy. Đây là làm rối, không phải mã hoá.</p>
     */
    private byte[] KEYS;

    /** Đã bắt tay khoá xong chưa. Codec đọc cờ này để biết có XOR hay không. */
    private boolean sentKey;

    /** Số hiệu session. Để {@code public} nên ai cũng sửa được. */
    public int id;

    private Socket socket;

    /** Cờ sống của kết nối. Cả 3 thread đều lặp theo cờ này. */
    private boolean connected;

    private Sender sender;
    private Collector collector;
    private QueueHandler queueHandler;

    /** Ba thread phục vụ kết nối. {@code final} — tạo trong constructor, không thay được. */
    private final Thread tSender;
    private final Thread tCollector;
    private final Thread tQueueHandler;

    private IKeySessionHandler keyHandler;

    /** IP client, đã bỏ dấu gạch chéo đứng đầu. */
    private String ip;

    /**
     * <b>Luôn ném ngoại lệ</b> vì {@link #instance} không bao giờ được gán.
     * Tàn dư của một thiết kế singleton đã bỏ. Không nơi nào gọi.
     */
    public static ISession gI() throws Exception {
        if (instance == null) {
            throw new Exception("Instance has not been initialized!");
        }
        return instance;
    }

    /**
     * Constructor phía <b>client</b>: tự mở kết nối tới {@code host:port}.
     *
     * <p>Không dùng trong luồng game bình thường. ID nhúng cứng {@code 31072002}
     * (trông như một ngày tháng) — không có ý nghĩa gì với hệ thống.</p>
     *
     * <p>Ba dòng khởi tạo thread bên dưới lặp lại y hệt biểu thức đã gán cho
     * {@code sender}/{@code collector} ở trên. Vì lúc đó chúng đã khác {@code null}
     * nên nhánh {@code setSocket(...)} được chạy — vô hại nhưng dư thừa và khó đọc.
     * Ngoài ra tên thread dùng {@code this.ip} lúc đó còn {@code null}.</p>
     */
    public Session(String host, int port) throws IOException {
        this.id = 31072002;
        this.socket = new Socket(host, port);
        this.socket.setSendBufferSize(0x100000);
        this.socket.setReceiveBufferSize(0x100000);
        this.socketType = SocketType.CLIENT;
        this.connected = true;
        this.sender = this.sender != null ? this.sender.setSocket(this.socket) : new Sender(this, this.socket);
        this.collector = this.collector != null ? this.collector.setSocket(this.socket) : new Collector(this, this.socket);
        this.queueHandler = new QueueHandler(this);
        this.tSender = new Thread(this.sender != null ? this.sender.setSocket(this.socket) : (this.sender = new Sender(this, this.socket)), "Sender - IP : " + this.ip);
        this.tCollector = new Thread(this.collector != null ? this.collector.setSocket(this.socket) : (this.collector = new Collector(this, this.socket)), "Collector - IP : " + this.ip);
        this.tQueueHandler = new Thread(this.queueHandler);
    }

    /**
     * Constructor phía <b>server</b>: bọc một socket vừa được accept.
     *
     * <p>Đây là constructor mà {@code SessionFactory} gọi qua reflection cho
     * <i>mọi</i> người chơi.</p>
     *
     * <p>Bộ đệm gửi/nhận đặt 1 MiB ({@code 0x100000}) mỗi chiều. Nhân với 500 kết
     * nối là khoảng 1 GB bộ đệm socket ở tầng nhân — khá lớn so với lượng dữ liệu
     * game thực tế; 64 KiB là quá đủ.</p>
     *
     * <p>Ba thread được <b>tạo</b> ở đây nhưng <b>chưa chạy</b>. {@code ServerManager}
     * mới bật {@code Collector} và {@code QueueHandler}; {@code Sender} bật muộn hơn,
     * sau khi bắt tay khoá xong (xem {@code MySession.sendKey()}).</p>
     */
    public Session(Socket socket) {
        this.KEYS = "NguyenDucVuEntertainment".getBytes();
        this.id = ID_INIT++;
        this.socket = socket;
        try {
            this.socket.setSendBufferSize(0x100000);
            this.socket.setReceiveBufferSize(0x100000);
        } catch (SocketException ignored) {
        }
        this.socketType = SocketType.SERVER;
        this.connected = true;
        this.ip = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().replace("/", "");
        this.sender = this.sender != null ? this.sender.setSocket(this.socket) : new Sender(this, this.socket);
        this.collector = this.collector != null ? this.collector.setSocket(this.socket) : new Collector(this, this.socket);
        this.queueHandler = new QueueHandler(this);
        this.tSender = new Thread(this.sender != null ? this.sender.setSocket(this.socket) : (this.sender = new Sender(this, this.socket)), "Sender - IP : " + this.ip);
        this.tCollector = new Thread(this.collector != null ? this.collector.setSocket(this.socket) : (this.collector = new Collector(this, this.socket)), "Collector - IP : " + this.ip);
        this.tQueueHandler = new Thread(this.queueHandler);
    }

    /**
     * Xếp gói tin vào hàng đợi gửi. Đây là hàm mà <b>toàn bộ logic game</b> dùng.
     * Trả về ngay; gói tin thật sự đi ở thread {@code Sender}.
     */
    @Override
    public void sendMessage(Message msg) {
        if (this.isConnected() && msg != null) {
            this.sender.sendMessage(msg);
        }
    }

    /**
     * Gắn codec cho <b>cả hai chiều</b>.
     *
     * <p>Cùng một đối tượng được dùng cho đọc và ghi. Không sao, vì codec giữ hai
     * con trỏ khoá tách rời ({@code curR}, {@code curW}).</p>
     */
    @Override
    public ISession setSendCollect(IMessageSendCollect collect) {
        this.sender.setSend(collect);
        this.collector.setCollect(collect);
        return this;
    }

    /** Gắn nơi xử lý nghiệp vụ ({@code nro.server.Controller}) cho hàng đợi vào. */
    @Override
    public ISession setMessageHandler(IMessageHandler handler) {
        this.queueHandler.setMessageHandler(handler);
        return this;
    }

    /** Gắn bộ bắt tay khoá ({@code MyKeyHandler}). */
    @Override
    public ISession setKeyHandler(IKeySessionHandler handler) {
        this.keyHandler = handler;
        return this;
    }

    /**
     * Bật thread gửi.
     *
     * <p><b>Bật sau cùng</b>, ngay sau khi khoá đã gửi xong. Gọi hai lần sẽ ném
     * {@code IllegalThreadStateException}.</p>
     */
    @Override
    public ISession startSend() {
        this.tSender.start();
        return this;
    }

    /** Bật thread đọc socket. Từ lúc này gói tin bắt đầu chảy vào. */
    @Override
    public ISession startCollect() {
        this.tCollector.start();
        return this;
    }

    /** Bật thread xử lý hàng đợi vào. */
    @Override
    public ISession startQueueHandler() {
        this.tQueueHandler.start();
        return this;
    }

    /**
     * Bật cả 3 thread cùng lúc.
     *
     * <p><b>Server không dùng hàm này</b> — nó bật {@code Collector} và
     * {@code QueueHandler} trước, để dành {@code Sender} tới sau bắt tay khoá.
     * Gọi {@code start()} ở phía server sẽ làm thread gửi chạy trước khi có khoá
     * và hỏng luồng mã hoá.</p>
     */
    @Override
    public ISession start() {
        this.tSender.start();
        this.tCollector.start();
        this.tQueueHandler.start();
        return this;
    }

    @Override
    public String getIP() {
        return this.ip;
    }

    @Override
    public long getID() {
        return this.id;
    }

    /**
     * Ngắt kết nối: hạ cờ sống, đóng 3 worker, đóng socket, rồi {@link #dispose()}.
     *
     * <p>Hạ {@code connected = false} <b>trước tiên</b> để 3 vòng lặp thread tự thoát.</p>
     *
     * <p><b>Cảnh báo:</b> {@code sender.close()} <i>vứt bỏ</i> mọi gói còn chờ gửi.
     * Muốn người chơi kịp nhận thông báo trước khi bị kick thì phải chờ trước khi
     * gọi hàm này — đó chính là lý do có {@code Thread.sleep(1000)} trong
     * {@code Client.safePut()}.</p>
     *
     * <p>Có thể bị gọi <b>đồng thời</b> từ thread {@code Collector} (client rớt) và
     * từ luồng game (kick). Không có khoá, nên hai lần gọi chồng nhau có thể chạm
     * vào trường vừa bị {@link #dispose()} gán {@code null}.</p>
     */
    @Override
    public void disconnect() {
        this.connected = false;
        this.sentKey = false;
        if (this.sender != null) {
            this.sender.close();
        }
        if (this.collector != null) {
            this.collector.close();
        }
        if (this.queueHandler != null) {
            this.queueHandler.close();
        }
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException ignored) {
            }
        }
        this.dispose();
    }

    /**
     * Giải phóng tham chiếu và gỡ session khỏi {@link SessionManager}.
     *
     * <p>Được gọi từ {@link #disconnect()}, không nên gọi trực tiếp.
     * Gán {@code null} hàng loạt là để GC thu hồi sớm — nhưng cũng là nguồn của
     * các NPE hiếm gặp nếu thread khác còn đang dùng session.</p>
     */
    @Override
    public void dispose() {
        if (this.sender != null) {
            this.sender.dispose();
        }
        if (this.collector != null) {
            this.collector.dispose();
        }
        if (this.queueHandler != null) {
            this.queueHandler.dispose();
        }
        this.socket = null;
        this.sender = null;
        this.collector = null;
        this.queueHandler = null;
        this.ip = null;
        SessionManager.gI().removeSession(this);
    }

    /** Uỷ quyền cho {@code keyHandler}. @throws Exception nếu chưa gắn key handler */
    @Override
    public void sendKey() throws Exception {
        if (this.keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        this.keyHandler.sendKey(this);
    }

    /** Uỷ quyền cho {@code keyHandler}. Chỉ dùng ở vai {@code CLIENT}. */
    @Override
    public void setKey(Message message) throws Exception {
        if (this.keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        this.keyHandler.setKey(this, message);
    }

    /** Đặt khoá trực tiếp. Lưu <b>tham chiếu</b>, không sao chép mảng. */
    @Override
    public void setKey(byte[] key) {
        this.KEYS = key;
    }

    @Override
    public boolean sentKey() {
        return this.sentKey;
    }

    @Override
    public void setSentKey(boolean sent) {
        this.sentKey = sent;
    }

    /**
     * Ghi thẳng gói tin ra socket, bỏ qua hàng đợi.
     *
     * <p>Chỉ dùng cho gói khoá — lúc đó thread {@code Sender} chưa chạy.
     * Dùng cho gói thường sẽ chặn luồng game trong lúc chờ ghi socket.</p>
     */
    @Override
    public void doSendMessage(Message msg) throws Exception {
        this.sender.doSendMessage(msg);
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    /** Khoá XOR hiện tại. Trả <b>tham chiếu</b>, người gọi sửa được trực tiếp. */
    @Override
    public byte[] getKey() {
        return this.KEYS;
    }

    @Override
    public SocketType getSocketType() {
        return this.socketType;
    }

    @Override
    public QueueHandler getQueueHandler() {
        return this.queueHandler;
    }

}
