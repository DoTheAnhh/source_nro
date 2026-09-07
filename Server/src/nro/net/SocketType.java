package nro.net;

/**
 * Phía nào của kết nối mà một {@code Session} đại diện.
 *
 * <p>Cùng một lớp {@code Session} được dùng cho cả hai vai. Điều duy nhất khác
 * nhau là <b>chiều bắt tay khoá</b>:</p>
 * <ul>
 *   <li>{@link #SERVER} — nhận {@code GET_SESSION_ID} thì <i>gửi</i> khoá đi.</li>
 *   <li>{@link #CLIENT} — nhận {@code GET_SESSION_ID} thì <i>đọc</i> khoá vào.</li>
 * </ul>
 * Xem {@code Collector.run()} — chỗ duy nhất phân biệt hai giá trị này.
 */
public enum SocketType {
    /** Session server dùng khi bản thân nó đóng vai client đi kết nối ra ngoài. */
    CLIENT,
    /** Session do server tạo cho một client vừa kết nối vào (trường hợp thường gặp). */
    SERVER
}
