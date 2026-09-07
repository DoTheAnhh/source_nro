package nro.net;

/**
 * Mã lệnh cấp giao thức — những lệnh mà <b>tầng mạng</b> tự xử lý,
 * không đẩy lên tầng game.
 *
 * <p>Khác với hàng chục mã lệnh nghiệp vụ nằm rải trong {@code nro.server.Controller}
 * dưới dạng số trần (-100, 127, -105...), lệnh ở đây được {@code Collector.run()}
 * chặn lại và xử lý ngay, không bao giờ tới {@code Controller}.</p>
 */
public class CommandMessage {

    /**
     * Gói tin đầu tiên của mọi kết nối — yêu cầu bắt tay khoá mã hoá.
     *
     * <p>Giá trị {@code -27} = {@code 0xE5}. Client và server đều nhúng cứng số
     * này; đổi ở một phía là kết nối chết ngay từ gói đầu.</p>
     *
     * <p><b>Nên là {@code static final}.</b> Hiện tại chỉ {@code static}, nghĩa là
     * bất kỳ đoạn code nào cũng gán đè được lúc chạy và làm hỏng toàn bộ bắt tay.</p>
     */
    public static byte GET_SESSION_ID = -27;

}
