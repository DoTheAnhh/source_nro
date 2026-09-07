package nro.net.api;

/**
 * Vòng đời của socket lắng nghe phía server.
 *
 * <p>Cách dùng chuẩn (xem {@code ServerManager.activeServerSocket()}):</p>
 * <pre>
 * Network.gI()
 *        .init()                                  // mở Selector, tạo thread "Network"
 *        .setAcceptHandler(handler)               // gọi khi có client mới
 *        .setTypeSessionClone(MySession.class)    // lớp session sẽ được new ra
 *        .setDoSomeThingWhenClose(callback)       // dọn dẹp khi server đóng
 *        .start(PORT);                            // bind port + chạy vòng accept
 * </pre>
 *
 * <p>Kế thừa {@link Runnable} vì bản thân lớp cài đặt chính là vòng lặp
 * {@code selector.select()} chạy trên thread riêng.</p>
 */
public interface INetwork extends Runnable {

    /** Mở {@code Selector} và chuẩn bị thread accept. Chưa mở port. */
    INetwork init();

    /**
     * Bind port và bắt đầu nhận kết nối.
     *
     * @param p0 cổng lắng nghe; phải lớn hơn hoặc bằng 0
     * @throws Exception nếu chưa gắn accept handler, port sai, hoặc lớp session không hợp lệ
     */
    INetwork start(int p0) throws Exception;

    /** Gắn nơi nhận sự kiện "có client mới" / "client rớt". Bắt buộc gọi trước {@link #start(int)}. */
    INetwork setAcceptHandler(ISessionAcceptHandler p0);

    /** Đóng socket lắng nghe và chạy callback đóng server. */
    INetwork close();

    /** Xoá tham chiếu sau khi đã {@link #close()}. */
    INetwork dispose();

    /** Việc cần làm khi server đóng (hiện tại: log rồi {@code System.exit(0)}). */
    INetwork setDoSomeThingWhenClose(IServerClose serverClose);

    /**
     * Lớp session được tạo cho mỗi client. Phải cài {@link ISession} và có
     * constructor nhận {@code java.net.Socket}.
     */
    INetwork setTypeSessionClone(Class p0) throws Exception;

    /** @throws Exception nếu chưa gắn accept handler */
    ISessionAcceptHandler getAcceptHandler() throws Exception;

    /** Dừng vòng lặp accept nhưng không đóng các session đang mở. */
    void stopConnect();
}
