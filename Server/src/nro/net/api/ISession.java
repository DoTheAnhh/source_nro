package nro.net.api;

import nro.net.io.Message;
import nro.net.QueueHandler;
import nro.net.SocketType;

/**
 * Một phiên kết nối TCP với một client.
 *
 * <p><b>Vị trí trong luồng:</b> {@code Network} nhận socket mới → tạo một
 * {@code ISession} → {@code ServerManager} gắn handler vào session → session tự
 * chạy 3 thread riêng (đọc / xử lý / gửi).</p>
 *
 * <p><b>Ba thread của một session</b> (xem {@code nro.net.session.Session}):</p>
 * <ol>
 *   <li>{@code Collector} — đọc byte từ socket, dựng {@link Message}, đẩy vào hàng đợi.</li>
 *   <li>{@code QueueHandler} — lấy khỏi hàng đợi, gọi {@link IMessageHandler#onMessage}.</li>
 *   <li>{@code Sender} — lấy khỏi hàng đợi gửi, ghi ra socket.</li>
 * </ol>
 *
 * <p><b>Bắt tay khoá:</b> gói đầu tiên client gửi là {@code GET_SESSION_ID}.
 * Server đáp lại bằng {@link #sendKey()}; từ đó mọi byte đều được XOR với
 * {@link #getKey()}.</p>
 *
 * <p>Các setter trả về chính {@code ISession} để gọi nối chuỗi (fluent):
 * {@code session.setMessageHandler(x).setSendCollect(y).startCollect()}.</p>
 */
public interface ISession {

    /** Gắn bộ mã hoá/giải mã gói tin (đọc và ghi dùng chung một đối tượng). */
    ISession setSendCollect(final IMessageSendCollect p0);

    /** Gắn nơi xử lý nghiệp vụ cho mọi gói tin đến. Server dùng {@code nro.server.Controller}. */
    ISession setMessageHandler(final IMessageHandler p0);

    /** Gắn bộ sinh/nhận khoá mã hoá. Server dùng {@code nro.net.io.MyKeyHandler}. */
    ISession setKeyHandler(final IKeySessionHandler p0);

    /** Bật thread gửi. */
    ISession startSend();

    /** Bật thread đọc socket. */
    ISession startCollect();

    /** Bật thread xử lý hàng đợi gói tin đến. */
    ISession startQueueHandler();

    /** Bật cả 3 thread cùng lúc. */
    ISession start();

    /** IP client, đã bỏ dấu gạch chéo đứng đầu của {@code InetAddress.toString()}. */
    String getIP();

    /** {@code false} sau khi {@link #disconnect()} được gọi; mọi vòng lặp thread dừng theo cờ này. */
    boolean isConnected();

    /** Số hiệu session, cấp tăng dần lúc tạo. */
    long getID();

    /** Xếp gói tin vào hàng đợi gửi (không chặn, gửi bất đồng bộ ở thread Sender). */
    void sendMessage(final Message p0);

    /** Ghi thẳng gói tin ra socket ngay tại thread đang gọi, bỏ qua hàng đợi. */
    void doSendMessage(final Message p0) throws Exception;

    /** Đóng socket, dừng 3 thread, rồi gọi {@link #dispose()}. */
    void disconnect();

    /** Giải phóng tham chiếu và gỡ session khỏi {@code SessionManager}. Gọi sau {@link #disconnect()}. */
    void dispose();

    /** Server gửi khoá mã hoá cho client (bước 2 của bắt tay). */
    void sendKey() throws Exception;

    /** Client nhận khoá từ gói tin server gửi về. */
    void setKey(Message msg) throws Exception;

    /** Đặt khoá trực tiếp bằng mảng byte. */
    void setKey(byte[] keys);

    /** Khoá dùng để XOR từng byte khi đọc/ghi. */
    byte[] getKey();

    /** Đã hoàn tất bắt tay khoá hay chưa. */
    boolean sentKey();

    void setSentKey(final boolean p0);

    /**
     * Phía nào của kết nối. Quan trọng vì luồng bắt tay khoá ngược nhau:
     * {@code SERVER} thì gửi khoá, {@code CLIENT} thì nhận khoá.
     */
    SocketType getSocketType();

    /** Hàng đợi gói tin đến; {@code Collector} đẩy vào đây. */
    QueueHandler getQueueHandler();
}
