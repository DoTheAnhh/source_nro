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
 */
public final class UiTheme {

    private UiTheme() {
    }

    // ------------------------------------------------------------ thanh bên
    /** Nền thanh bên. Tối để tách hẳn khỏi vùng nội dung sáng. */
    public static final Color SIDEBAR = new Color(0x16, 0x1B, 0x24);
    /** Dải thương hiệu trên cùng thanh bên — tối hơn nền một bậc. */
    public static final Color SIDEBAR_HEADER = new Color(0x0F, 0x13, 0x1A);
    /** Nền mục đang chọn. */
    public static final Color SIDEBAR_ACTIVE = new Color(0x23, 0x2B, 0x38);
    /** Nền mục đang trỏ chuột vào. */
    public static final Color SIDEBAR_HOVER = new Color(0x1D, 0x24, 0x30);
    /** Đường kẻ mảnh trong vùng tối. */
    public static final Color SIDEBAR_LINE = new Color(0x25, 0x2C, 0x38);

    // ---------------------------------------------------------------- chữ
    public static final Color TEXT_ON_DARK = new Color(0xE6, 0xE9, 0xEF);
    public static final Color TEXT_MUTED_DARK = new Color(0x8A, 0x93, 0xA3);
    public static final Color TEXT = new Color(0x22, 0x26, 0x2E);
    public static final Color TEXT_MUTED = new Color(0x6B, 0x72, 0x80);

    // --------------------------------------------------------------- nhấn
    /**
     * Màu nhấn: cam.
     *
     * <p>Chọn cam thay vì xanh mặc định của FlatLaf vì hai lẽ: nó là màu áo
     * của game nên bảng điều khiển trông thuộc cùng một sản phẩm, và nó nổi rõ
     * trên nền thanh bên tối — xanh dương thì chìm.</p>
     */
    public static final Color ACCENT = new Color(0xFF, 0x8A, 0x1E);
    public static final Color ACCENT_DIM = new Color(0xC9, 0x6A, 0x14);

    // ------------------------------------------------------------ nội dung
    /** Nền vùng nội dung — xám rất nhạt, để panel trắng nổi lên như thẻ. */
    public static final Color CONTENT_BG = new Color(0xF4, 0xF6, 0xF9);
    public static final Color CARD = Color.WHITE;
    public static final Color LINE = new Color(0xE2, 0xE6, 0xEC);

    // ---------------------------------------------------------- trạng thái
    public static final Color OK = new Color(0x2E, 0xC4, 0x6B);
    public static final Color WARN = new Color(0xF5, 0xA6, 0x23);
    public static final Color DANGER = new Color(0xE5, 0x48, 0x4B);

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
