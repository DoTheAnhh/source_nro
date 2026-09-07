package nro.repository.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Tên các loại vật phẩm, để sửa được thay vì gõ cứng trong mã.
 *
 * <h2>Đây chỉ là cái nhãn</h2>
 *
 * <p>Cần nói rõ để khỏi hiểu lầm: <b>thêm một loại mới ở đây không dạy cho game
 * biết làm gì với loại đó.</b> Con số loại đã được gán ý nghĩa cứng ở cả máy
 * chủ lẫn client — 0 tới 4 là năm ô trang bị, 9 và 10 và 34 là Vàng, Ngọc,
 * Hồng ngọc nên cộng thẳng vào ví, 13 là Bùa nên cộng thời gian hiệu lực. Đặt
 * ra số 120 rồi gán cho một vật phẩm thì vật phẩm đó nằm trong hành trang như
 * một món thường, không hơn.</p>
 *
 * <p>Cái bảng này giải quyết việc khác: gom nhóm và tìm kiếm trong panel cho dễ
 * nhìn. Trước đây danh sách tên nằm cứng trong mã nên loại 99 — đang có 67 vật
 * phẩm thật — hiện ra là "Loại 99", không ai biết nó là gì.</p>
 *
 * <p>Lần chạy đầu bảng được gieo từ danh sách cũ, cộng thêm mọi loại đang thực
 * sự xuất hiện trong {@code item_template} mà chưa có tên.</p>
 */
public class LoaiVatPhamDAO {

    /**
     * Mã loại lớn nhất dùng được.
     *
     * <p><b>Đây là trần cứng, không nới được.</b> {@code ItemTemplate.type} khai
     * kiểu {@code byte}, {@code Manager.loadDatabase} đọc bằng
     * {@code rs.getByte("type")}, và {@code ItemData} gửi cho client bằng
     * {@code writeByte}. Số vượt 127 làm {@code getByte} ném lỗi tràn ngay lúc
     * nạp CSDL, tức là <b>máy chủ không khởi động được nữa</b> — mà thông báo
     * lỗi lại không hề nhắc tới loại vật phẩm, nên rất mất công tìm.</p>
     *
     * <p>Muốn nới thì phải sửa cả kiểu trường, cách đọc CSDL và giao thức gửi
     * client ở cả sáu bản Game — không đáng, vì 128 loại đã quá đủ.</p>
     */
    public static final int MA_TOI_DA = 127;

    /** Tên đọc từ CSDL, nạp một lần rồi giữ. */
    private static volatile Map<Integer, String> cache;

    private LoaiVatPhamDAO() {
    }

    /**
     * Danh sách tên gõ cứng từ trước, dùng để gieo bảng lần đầu.
     *
     * <p>Cũng là chỗ dựa khi CSDL hỏng: mất kết nối thì panel vẫn còn tên để
     * hiện, chứ không trơ ra một cột số.</p>
     */
    private static Map<Integer, String> macDinh() {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(0, "Áo");
        m.put(1, "Quần");
        m.put(2, "Găng");
        m.put(3, "Giày");
        m.put(4, "Rađa");
        m.put(5, "Cải trang");
        m.put(6, "Đậu thần");
        m.put(7, "Sách kỹ năng");
        m.put(8, "Vật phẩm nhiệm vụ");
        m.put(9, "Vàng");
        m.put(10, "Ngọc");
        m.put(11, "Vật phẩm khác");
        m.put(12, "Ngọc rồng");
        m.put(13, "Bùa");
        m.put(14, "Đá nâng cấp");
        m.put(15, "Mảnh đá vụn");
        m.put(16, "Bình nước phép");
        m.put(17, "Đai lưng (đeo lưng)");
        m.put(18, "Nâng kỹ năng đệ tử");
        m.put(19, "Bông tai Porata");
        m.put(20, "Gia hạn mầm");
        m.put(21, "Đệ tử / thú cưng");
        m.put(22, "Vệ tinh");
        m.put(23, "Ván bay / phương tiện");
        m.put(24, "Ván bay VIP");
        m.put(25, "Gói rađa");
        m.put(26, "Vé tặng ngọc");
        m.put(27, "Nguyên liệu / đồ ăn");
        m.put(28, "Cờ");
        m.put(29, "Vật phẩm hỗ trợ");
        m.put(30, "Sao pha lê");
        m.put(31, "Bánh Trung Thu");
        m.put(32, "Giáp tập luyện");
        m.put(33, "Mảnh ghép");
        m.put(34, "Hồng ngọc");
        m.put(35, "Sách tuyệt kỹ");
        m.put(36, "Vật phẩm đặc biệt");
        m.put(37, "Sách nội tại");
        m.put(39, "Chân thiên tử");
        m.put(70, "Linh Thú");
        m.put(71, "Chiến Linh");
        m.put(80, "Đổi chiêu đệ tử");
        m.put(81, "Cờ đảng / quân hiệu");
        m.put(82, "Quân phục");
        m.put(83, "Mũ");
        m.put(84, "Chưa đặt tên");
        m.put(93, "Trang bị tăng chỉ số");
        return m;
    }

    private static volatile boolean daDungBang;

    /** Tạo bảng và gieo dữ liệu lần đầu. Gọi lại không sao. */
    public static void damBaoBang() {
        if (daDungBang) {
            return;
        }
        daDungBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS loai_vat_pham ("
                    + " ma INT(11) NOT NULL,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (ma)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            gieoLanDau();
        } catch (Exception ex) {
            Logger.logException(LoaiVatPhamDAO.class, ex,
                    "Không dựng được bảng loai_vat_pham — dùng tên gõ cứng");
        }
    }

    /**
     * Đổ tên mặc định vào bảng, và đặt tên tạm cho loại nào đang dùng mà chưa
     * có tên.
     *
     * <p>Dùng {@code INSERT IGNORE} nên tên bạn đã sửa không bị ghi đè.</p>
     */
    private static void gieoLanDau() throws Exception {
        for (Map.Entry<Integer, String> e : macDinh().entrySet()) {
            ConnectDB.executeUpdate(
                    "INSERT IGNORE INTO loai_vat_pham (ma, ten) VALUES (?, ?)",
                    e.getKey(), e.getValue());
        }
        // Loai dang co vat pham that ma khong ai dat ten — vi du loai 99 voi 67
        // mon. De nguyen thi panel hien "Loai 99", nhin khong ra la gi.
        ConnectDB.executeUpdate(
                "INSERT IGNORE INTO loai_vat_pham (ma, ten)"
                + " SELECT DISTINCT type, CONCAT('Loại ', type)"
                + " FROM item_template");
    }

    /** Toàn bộ tên loại, sắp theo mã. Đọc một lần rồi giữ trong bộ nhớ. */
    public static Map<Integer, String> tatCa() {
        Map<Integer, String> c = cache;
        if (c != null) {
            return c;
        }
        synchronized (LoaiVatPhamDAO.class) {
            if (cache != null) {
                return cache;
            }
            Map<Integer, String> m = new TreeMap<>();
            CrisResultSet rs = null;
            try {
                damBaoBang();
                rs = ConnectDB.executeQuery(
                        "SELECT ma, ten FROM loai_vat_pham ORDER BY ma");
                while (rs.next()) {
                    m.put(rs.getInt("ma"), rs.getString("ten"));
                }
            } catch (Exception ex) {
                Logger.logException(LoaiVatPhamDAO.class, ex,
                        "Không đọc được tên loại vật phẩm");
            } finally {
                if (rs != null) {
                    try {
                        rs.dispose();
                    } catch (Exception boQua) {
                        // Dong khong duoc thi thoi, khong dang de hong panel.
                    }
                }
            }
            if (m.isEmpty()) {
                m.putAll(macDinh());
            }
            cache = m;
            return m;
        }
    }

    /** Quên bộ nhớ đệm để lần hỏi sau đọc lại từ CSDL. */
    public static void napLai() {
        cache = null;
    }

    /** Tên của một mã, hoặc {@code null} nếu chưa đặt. */
    public static String ten(int ma) {
        return tatCa().get(ma);
    }

    /**
     * Thêm hoặc sửa một loại.
     *
     * @return câu báo lỗi, hoặc {@code null} nếu xong xuôi
     */
    public static String luu(int ma, String ten, boolean them) {
        if (ma < 0 || ma > MA_TOI_DA) {
            return "Mã loại phải từ 0 đến " + MA_TOI_DA + "."
                    + " Máy chủ giữ loại trong một byte và cũng gửi cho client"
                    + " bằng một byte, nên số lớn hơn sẽ làm máy chủ không khởi"
                    + " động được.";
        }
        if (ten == null || ten.trim().isEmpty()) {
            return "Chưa đặt tên cho loại.";
        }
        if (ten.trim().length() > 60) {
            return "Tên dài quá 60 ký tự.";
        }
        try {
            damBaoBang();
            if (them && tatCa().containsKey(ma)) {
                return "Mã " + ma + " đã có rồi: " + tatCa().get(ma);
            }
            ConnectDB.executeUpdate(
                    "INSERT INTO loai_vat_pham (ma, ten) VALUES (?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten)",
                    ma, ten.trim());
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(LoaiVatPhamDAO.class, ex, "Lỗi lưu loại vật phẩm");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Xoá một loại.
     *
     * <p>Chặn khi còn vật phẩm mang loại đó. Xoá tên đi thì vật phẩm không mất,
     * nhưng cả trăm món sẽ hiện ra là "Loại 27" và không lọc theo tên được
     * nữa — phiền hơn là để nguyên.</p>
     */
    public static String xoa(int ma) {
        try {
            damBaoBang();
            int dem = demVatPham(ma);
            if (dem > 0) {
                return "Còn " + dem + " vật phẩm thuộc loại này."
                        + " Đổi loại cho chúng trước rồi hãy xoá.";
            }
            int n = ConnectDB.executeUpdate(
                    "DELETE FROM loai_vat_pham WHERE ma = ?", ma);
            napLai();
            return n == 0 ? "Không có mã " + ma + "." : null;
        } catch (Exception ex) {
            Logger.logException(LoaiVatPhamDAO.class, ex, "Lỗi xoá loại vật phẩm");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Số vật phẩm đang mang loại này. */
    public static int demVatPham(int ma) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT COUNT(*) n FROM item_template WHERE type = ?", ma);
            if (rs.next()) {
                return rs.getInt("n");
            }
        } catch (Exception ex) {
            Logger.logException(LoaiVatPhamDAO.class, ex,
                    "Không đếm được vật phẩm theo loại");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                    // Nhu tren.
                }
            }
        }
        return 0;
    }
}
