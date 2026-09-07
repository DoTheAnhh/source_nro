package nro.net.api;

/**
 * Nhận hai sự kiện vòng đời của một kết nối.
 *
 * <p>Cài đặt nằm ngay trong {@code ServerManager.activeServerSocket()} dưới dạng
 * lớp ẩn danh.</p>
 */
public interface ISessionAcceptHandler {

    /**
     * Có client mới kết nối.
     *
     * <p>Chạy trên thread {@code Network} — <b>không được làm việc nặng ở đây</b>,
     * vì trong lúc đó không client nào khác được accept.</p>
     *
     * <p>Cài đặt hiện tại: kiểm tra giới hạn số kết nối trên mỗi IP
     * ({@code Manager.MAX_PER_IP}) rồi gắn handler, rồi bật thread đọc và thread
     * xử lý hàng đợi.</p>
     */
    void sessionInit(final ISession p0);

    /**
     * Client rớt kết nối.
     *
     * <p>Được gọi từ thread {@code Collector} của chính session đó, ngay khi vòng
     * đọc socket thoát. Cài đặt hiện tại gọi {@code Client.gI().kickSession(...)}
     * để gỡ người chơi khỏi danh sách online và lưu dữ liệu.</p>
     */
    void sessionDisconnect(final ISession p0);
}
