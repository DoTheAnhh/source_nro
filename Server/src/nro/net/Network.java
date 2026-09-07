package nro.net;

import nro.core.log.Logger;
import nro.net.api.IServerClose;
import java.net.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import nro.net.api.ISession;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;
import nro.net.api.ISessionAcceptHandler;
import nro.net.api.INetwork;
import nro.net.session.Session;
import nro.net.session.SessionFactory;
import nro.net.session.SessionManager;

/**
 * Cửa vào của server: mở port, nhận kết nối, giao mỗi kết nối cho một session.
 *
 * <p><b>Cách dùng</b> — xem {@code ServerManager.activeServerSocket()}:</p>
 * <pre>
 * Network.gI().init()
 *        .setAcceptHandler(handler)
 *        .setTypeSessionClone(MySession.class)
 *        .setDoSomeThingWhenClose(callback)
 *        .start(PORT);
 * </pre>
 *
 * <p><b>Ghi chú kiến trúc — NIO chỉ dùng nửa vời.</b> Lớp này dùng
 * {@code Selector} + {@code ServerSocketChannel} không chặn để <i>accept</i>,
 * tức là kiểu NIO. Nhưng ngay sau khi accept nó gọi {@code .socket()} để lấy
 * {@code Socket} chặn kiểu cũ, rồi giao cho một {@code Session} đẻ ra <b>3 thread</b>.
 * Kết quả: mọi lợi ích của NIO (ít thread, mở rộng tốt) mất sạch — 500 người
 * online là 1.500 thread. Muốn dùng NIO thật thì {@code Selector} phải quản luôn
 * cả OP_READ/OP_WRITE, không chỉ OP_ACCEPT.</p>
 */
public class Network implements INetwork, Runnable {

    /**
     * Thoi gian toi da cho mot lan doc socket, mili giay.
     *
     * <p>Het gio ma khong co byte nao thi luong doc bat ra bang
     * { SocketTimeoutException}, phien dong lai va tra ba thread ve. Dat
     * rong rai — hai phut — vi choi binh thuong van co luc dung im lau; muc
     * dich la don xac ket noi chu khong phai duoi nguoi choi ranh.</p>
     */
    public static final int CHO_DOC_TOI_DA = 120_000;

    private static Network instance;

    /** Cổng đang lắng nghe; {@code -1} nghĩa là chưa {@link #start(int)}. */
    private int port;

    /** Kênh lắng nghe, chế độ không chặn, đăng ký {@code OP_ACCEPT} với selector. */
    private ServerSocketChannel serverSocketChannel;

    /** Lớp session sẽ được dựng cho mỗi client. Mặc định {@link Session}, server đặt thành {@code MySession}. */
    private Class sessionClone;

    /** Cờ chạy của vòng accept. {@link #stopConnect()} hạ cờ này. */
    private boolean start;

    /** Việc cần làm khi server đóng. */
    private IServerClose serverClose;

    /** Nơi nhận sự kiện có client mới / client rớt. Bắt buộc có trước khi {@link #start(int)}. */
    private ISessionAcceptHandler acceptHandler;

    /** Thread chạy vòng {@link #run()}. */
    private Thread loopServer;

    /** Selector NIO, hiện chỉ theo dõi {@code OP_ACCEPT}. */
    private Selector selector;

    /**
     * <b>Trường thừa và gây hiểu nhầm.</b> Không bao giờ được gán trong luồng
     * server (socket của client được giao thẳng cho {@code Session}), nên trong
     * {@link #close()} nó luôn {@code null} và khối dọn dẹp socket ở đó không bao
     * giờ chạy. Có thể xoá.
     */
    private Socket socket;

    /**
     * Singleton.
     *
     * <p><b>Không an toàn luồng</b> (thiếu khoá quanh {@code if (instance == null)}),
     * nhưng ở đây vô hại trên thực tế vì chỉ được gọi lần đầu từ luồng khởi động
     * đơn thread.</p>
     */
    public static Network gI() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    /** Riêng tư để ép dùng {@link #gI()}. */
    private Network() {
        this.port = -1;
        this.sessionClone = Session.class;
    }

    /**
     * Mở {@code Selector} và chuẩn bị thread accept. <b>Chưa mở port.</b>
     *
     * <p>Nếu mở selector lỗi thì chỉ ghi log rồi đi tiếp — {@code selector} còn
     * {@code null} và {@link #start(int)} sẽ ném NPE sau đó.</p>
     */
    @Override
    public INetwork init() {
        try {
            this.selector = Selector.open();
        } catch (IOException ex) {
            Logger.errorln(ex.toString());
        }
        this.loopServer = new Thread(this, "Network");
        return this;
    }

    /**
     * Bind port và bắt đầu nhận kết nối.
     *
     * <p>Ba lần kiểm tra đầu là tiền điều kiện lập trình (port hợp lệ, đã có
     * accept handler, lớp session hợp lệ) — sai thì ném ngoại lệ.</p>
     *
     * <p><b>Cần lưu ý:</b> nếu bind lỗi (port đã bị chiếm) thì hàm gọi thẳng
     * {@code System.exit(0)} — <i>bên trong một thư viện mạng</i>. Tự ý giết cả
     * tiến trình như vậy khiến người gọi không có cơ hội xử lý; nên ném ngoại lệ
     * và để {@code ServerManager} quyết định.</p>
     *
     * @param port cổng lắng nghe (mặc định 14445, xem {@code ServerManager.PORT})
     */
    @Override
    public INetwork start(final int port) throws Exception {
        if (port < 0) {
            throw new Exception("Please initialize the server port!");
        }
        if (this.acceptHandler == null) {
            throw new Exception("AcceptHandler has not been initialized!");
        }
        if (!ISession.class.isAssignableFrom(this.sessionClone)) {
            throw new Exception("The type 'session clone' is invalid!");
        }
        try {
            this.port = port;
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
            this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            Logger.error("Error initializing server at port " + port + "\n");
            System.exit(0);
        }
        this.start = true;
        this.loopServer.start();
        Logger.success("Server đang chạy tại port " + this.port + "\n");
        return this;
    }

    /**
     * Đóng socket lắng nghe và chạy callback đóng server.
     *
     * <p><b>Không đóng các session đang mở.</b> Người chơi đang online vẫn giữ kết
     * nối cho tới khi bị ngắt bằng đường khác. Trên thực tế điều này bị che đi vì
     * callback đóng của {@code ServerManager} gọi luôn {@code System.exit(0)}.</p>
     *
     * <p>Khối dọn {@code this.socket} ở cuối là code chết — xem ghi chú ở trường
     * {@link #socket}.</p>
     */
    @Override
    public INetwork close() {
        this.start = false;
        if (this.serverSocketChannel != null) {
            try {
                this.serverSocketChannel.close();
            } catch (IOException ex) {
            }
        }
        if (this.serverClose != null) {
            this.serverClose.serverClose();
        }
        if (this.socket != null) {
            try {
                String ip = socket.getInetAddress().getHostAddress();
                this.socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                java.util.logging.Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this;
    }

    /** Xoá tham chiếu sau khi đã {@link #close()}. Selector <b>không</b> được đóng ở đây. */
    @Override
    public INetwork dispose() {
        this.acceptHandler = null;
        this.loopServer = null;
        this.serverSocketChannel = null;
        this.socket = null;
        return this;
    }

    /** Gắn nơi nhận sự kiện vòng đời kết nối. Bắt buộc trước {@link #start(int)}. */
    @Override
    public INetwork setAcceptHandler(final ISessionAcceptHandler handler) {
        this.acceptHandler = handler;
        return this;
    }

    /**
     * Vòng accept — chạy trên thread tên {@code "Network"}.
     *
     * <p>Mỗi vòng: {@code selector.select()} chặn tới khi có sự kiện → với mỗi
     * khoá {@code isAcceptable()} thì accept socket → dựng session qua
     * {@link SessionFactory} → gọi {@code acceptHandler.sessionInit(session)} →
     * ghi vào {@link SessionManager}.</p>
     *
     * <p><b>Quan trọng:</b> {@code sessionInit} chạy <b>trên chính thread này</b>.
     * Bất cứ việc gì chậm trong đó (truy vấn DB, chờ khoá) sẽ chặn toàn bộ client
     * khác đang muốn vào. Cài đặt hiện tại chỉ đếm IP và bật thread nên còn nhanh —
     * cần giữ nguyên tính chất đó.</p>
     *
     * <p><b>Nợ kỹ thuật:</b></p>
     * <ul>
     *   <li>{@code catch (IOException ex) {}} rỗng: accept lỗi biến mất không dấu vết.</li>
     *   <li>Biến {@code ip} được tính rồi bỏ không dùng.</li>
     *   <li>{@code selectedKeys().clear()} đặt <i>sau</i> vòng lặp và ngoài khối
     *       try trong — nếu một client làm ném ngoại lệ, tập khoá không được xoá và
     *       vòng sau sẽ xử lý lại chính khoá cũ.</li>
     * </ul>
     */
    @Override
    public void run() {
        while (start) {
            try {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        Socket socket2 = server.accept().socket();
                        String ip = socket2.getInetAddress().getHostAddress();

                        // Chan NGAY O DAY, truoc khi dung session.
                        //
                        // Moi session de ra ba thread (doc, gui, hang doi).
                        // Tu choi o buoc sau — trong sessionInit — thi ba
                        // thread do da duoc tao roi moi bi dep di, va mot con
                        // lu ket noi van keo duoc may chu xuong du no bi
                        // "tu choi". Dong socket ngay khi con chua ton gi.
                        if (!nro.server.ChongDdos.choPhepKetNoi(ip)) {
                            try {
                                socket2.close();
                            } catch (IOException boQua) {
                            }
                            continue;
                        }

                        // Ba tuy chon socket, deu chong mot kieu tan cong hoac
                        // mot kieu ket noi chet:
                        //
                        //  - setSoTimeout: khong co no thi mot ket noi mo ra
                        //    roi im lang giu thread doc mai mai. Do la
                        //    slowloris — re nhat trong cac kieu danh, va
                        //    khong ton bang thong nao ca.
                        //  - setKeepAlive: mang rot giua chung ma khong ai
                        //    dong socket thi ket noi ma nam lai chiem cho.
                        //  - setTcpNoDelay: goi cua game deu nho, gom lai
                        //    theo Nagle chi lam do tre tang len.
                        try {
                            socket2.setSoTimeout(nro.net.Network.CHO_DOC_TOI_DA);
                            socket2.setKeepAlive(true);
                            socket2.setTcpNoDelay(true);
                        } catch (Exception boQua) {
                        }

                        final ISession session = SessionFactory.gI().cloneSession(this.sessionClone, socket2);
                        this.acceptHandler.sessionInit(session);
                        SessionManager.gI().putSession(session);
                    }
                }
                selector.selectedKeys().clear();
            } catch (IOException ex) {
            } catch (Exception ex2) {
                Logger.errorln(ex2.toString());
            }
        }
    }

    /** Đặt callback chạy khi server đóng. */
    @Override
    public INetwork setDoSomeThingWhenClose(final IServerClose serverClose) {
        this.serverClose = serverClose;
        return this;
    }

    /**
     * Đặt lớp session sẽ được dựng cho mỗi client.
     *
     * <p>Tính hợp lệ chỉ được kiểm tra muộn, trong {@link #start(int)}.
     * Khai báo {@code throws Exception} nhưng thân hàm không bao giờ ném.</p>
     */
    @Override
    public INetwork setTypeSessionClone(final Class clazz) throws Exception {
        this.sessionClone = clazz;
        return this;
    }

    /** @throws Exception nếu chưa gắn accept handler */
    @Override
    public ISessionAcceptHandler getAcceptHandler() throws Exception {
        if (this.acceptHandler == null) {
            throw new Exception("AcceptHandler has not been initialized!");
        }
        return this.acceptHandler;
    }

    /**
     * Hạ cờ chạy để vòng accept thoát.
     *
     * <p><b>Có thể không có tác dụng ngay:</b> thread đang chặn trong
     * {@code selector.select()} sẽ chỉ thấy cờ mới sau khi có kết nối tiếp theo.
     * Muốn dừng ngay phải gọi thêm {@code selector.wakeup()}.</p>
     */
    @Override
    public void stopConnect() {
        this.start = false;
    }
}
