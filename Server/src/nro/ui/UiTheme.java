package nro.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;

/**
 * Bảng màu và phông chữ dùng chung cho cả bảng điều khiển.
 *
 * <h2>Vì sao gom vào một chỗ</h2>
 *
 * <p>Trước đây mỗi panel tự gọi {@code new Color(0, 120, 215)},
 * {@code new Color(220, 220, 220)}… rải khắp mã nguồn. Hệ quả là cùng một ý
 * ("màu nhấn", "màu viền") lại có ba bốn giá trị hơi khác nhau, và muốn đổi
 * tông màu thì phải đi sửa từng chỗ — chắc chắn sót.</p>
 *
 * <p>Đây là lớp <b>hằng số</b>, không có logic. Panel nào cần màu thì lấy ở
 * đây thay vì tự gõ số.</p>
 *
 * <h2>Tông tối, nhấn cam</h2>
 *
 * <p>Nền than chì ba bậc — thanh bên tối nhất, vùng nội dung, rồi thẻ sáng
 * hơn một chút — để các lớp tách nhau bằng độ sáng thay vì bằng viền dày. Cam
 * là màu áo của game, và trên nền tối nó nổi hơn hẳn trên nền trắng.</p>
 *
 * <p><b>Đừng gõ {@code Color.WHITE} làm nền</b> ở panel mới: nền tối mà chèn
 * một ô trắng là lòi ra ngay. Dùng {@link #CARD} hoặc {@link #FIELD}.</p>
 */
public final class UiTheme {

    private UiTheme() {
    }

    // ------------------------------------------------------------ thanh bên
    /** Nền thanh bên — tối nhất, để tách khỏi vùng nội dung. */
    public static final Color SIDEBAR = new Color(0x0F, 0x11, 0x15);
    /** Dải thương hiệu trên cùng thanh bên — tối hơn nền một bậc. */
    public static final Color SIDEBAR_HEADER = new Color(0x0B, 0x0C, 0x0F);
    /** Nền mục đang chọn — ánh cam rất nhẹ cho khớp vạch nhấn. */
    public static final Color SIDEBAR_ACTIVE = new Color(0x26, 0x1E, 0x16);
    /** Nền mục đang trỏ chuột vào. */
    public static final Color SIDEBAR_HOVER = new Color(0x1A, 0x1C, 0x21);
    /** Đường kẻ mảnh trong vùng tối. */
    public static final Color SIDEBAR_LINE = new Color(0x1E, 0x21, 0x27);

    // ---------------------------------------------------------------- chữ
    public static final Color TEXT_ON_DARK = new Color(0xEC, 0xEE, 0xF1);
    public static final Color TEXT_MUTED_DARK = new Color(0x8C, 0x93, 0x9F);
    /** Chữ chính trên vùng nội dung. */
    public static final Color TEXT = new Color(0xE6, 0xE8, 0xEC);
    /** Chữ phụ, chú thích, đơn vị. */
    public static final Color TEXT_MUTED = new Color(0x9A, 0xA1, 0xAD);
    /** Chữ của ô trống / chưa có dữ liệu. */
    public static final Color TEXT_FAINT = new Color(0x62, 0x68, 0x73);

    // --------------------------------------------------------------- nhấn
    /**
     * Màu nhấn: cam.
     *
     * <p>Chọn cam thay vì xanh mặc định của FlatLaf vì hai lẽ: nó là màu áo
     * của game nên bảng điều khiển trông thuộc cùng một sản phẩm, và nó nổi rõ
     * trên nền tối — xanh dương thì chìm.</p>
     */
    public static final Color ACCENT = new Color(0xFF, 0x8C, 0x1A);
    public static final Color ACCENT_DIM = new Color(0xC9, 0x6A, 0x14);
    /** Cam sáng hơn, cho chữ nhấn nằm trên nền tối. */
    public static final Color ACCENT_TEXT = new Color(0xFF, 0xB0, 0x5C);

    // ------------------------------------------------------------ nội dung
    /** Nền vùng nội dung. */
    public static final Color CONTENT_BG = new Color(0x16, 0x18, 0x1D);
    /** Nền thẻ / panel — sáng hơn nền nội dung một bậc để nổi lên. */
    public static final Color CARD = new Color(0x1E, 0x21, 0x27);
    /** Thẻ khi trỏ chuột, dòng xen kẽ của bảng. */
    public static final Color CARD_ALT = new Color(0x23, 0x26, 0x2D);
    /** Nền ô nhập liệu. */
    public static final Color FIELD = new Color(0x13, 0x15, 0x19);
    /** Nền ô bị khoá / chỉ đọc. */
    public static final Color FIELD_DISABLED = new Color(0x1A, 0x1C, 0x20);
    /** Viền và đường kẻ. */
    public static final Color LINE = new Color(0x30, 0x34, 0x3C);
    /** Đường kẻ nhạt hơn, trong lòng thẻ. */
    public static final Color LINE_SOFT = new Color(0x27, 0x2A, 0x31);
    /** Nền dòng / ô đang chọn. */
    public static final Color SELECTION = new Color(0x4A, 0x32, 0x17);
    /** Nền nhấn ấm nhẹ: ô "đã có", ô cũ trong bộ chọn. */
    public static final Color WARM_BG = new Color(0x2F, 0x25, 0x18);

    // ---------------------------------------------------------- trạng thái
    public static final Color OK = new Color(0x34, 0xC7, 0x7B);
    public static final Color WARN = new Color(0xF5, 0xB0, 0x2E);
    public static final Color DANGER = new Color(0xF0, 0x55, 0x55);
    /** Đỏ đậm cho nút xoá hàng loạt — tách hẳn khỏi đỏ cảnh báo thường. */
    public static final Color DANGER_STRONG = new Color(0xB8, 0x32, 0x32);

    // ------------------------------------------------------------ màu nút
    // Nút lệnh phụ trong các panel mang màu theo LOẠI việc. Chỉnh độ bão hoà
    // cho hợp nền tối: màu gốc cũ tươi trên nền trắng nhưng chói trên nền than.
    /** Tím: về mặc định, cài đặt phụ, xoá kỷ lục. */
    public static final Color NUT_TIM = new Color(0x7C, 0x5C, 0xD6);
    /** Xanh lá: thêm, dựng sẵn, bật. */
    public static final Color NUT_XANH_LA = new Color(0x22, 0x9A, 0x5E);
    /** Xanh dương: di chuyển, sửa. */
    public static final Color NUT_XANH = new Color(0x3A, 0x7B, 0xC8);
    /** Nâu cam: tắt, giữ lại một phần. */
    public static final Color NUT_NAU = new Color(0xB0, 0x74, 0x24);
    /** Xám: nút đang tắt / không bấm được. */
    public static final Color NUT_TAT = new Color(0x3E, 0x43, 0x4C);

    // --------------------------------------------------------- màu HTML
    // Chữ viết trong nhãn HTML ({@code <span style='color:...'>}) không theo
    // Look and Feel, nên phải có mã hex riêng cho nền tối.
    public static final String HTML_MUTED = "#9aa1ad";
    public static final String HTML_OK = "#4ade80";
    public static final String HTML_DANGER = "#ff7070";

    /**
     * Phông chữ đơn cách (mọi ký tự rộng bằng nhau) có sẵn trên máy.
     *
     * <p>Dùng cho địa chỉ IP và các con số: chữ đơn cách làm chúng không nhảy
     * ngang mỗi lần con số đổi, và một dãy số dễ đọc hơn hẳn.</p>
     *
     * <p>Dò theo danh sách thay vì gõ cứng {@code "Consolas"}: font nào không
     * có trên máy thì Java lặng lẽ thay bằng font mặc định, nên gõ cứng một
     * tên là chấp nhận việc nó <i>có thể</i> không phải chữ đơn cách nữa mà
     * không ai biết.</p>
     */
    public static Font mono(int style, int size) {
        return new Font(TEN_MONO, style, size);
    }

    public static Font ui(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    private static final String TEN_MONO = timMono();

    private static String timMono() {
        Set<String> co = new HashSet<>();
        try {
            for (String t : GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()) {
                co.add(t);
            }
        } catch (Exception khongDocDuocFont) {
            // Không đọc được danh sách font (máy chạy không có màn hình) ->
            // dùng tên logic Monospaced, Java luôn có.
            return Font.MONOSPACED;
        }
        String[] uuTien = {"Cascadia Mono", "Consolas", "JetBrains Mono",
            "DejaVu Sans Mono", "Courier New"};
        for (String t : uuTien) {
            if (co.contains(t)) {
                return t;
            }
        }
        return Font.MONOSPACED;
    }
}
