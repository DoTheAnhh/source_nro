package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.intrinsic.Intrinsic;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Manager;

/**
 * Kho dữ liệu <b>nội tại</b> — bảng {@code intrinsic}.
 *
 * <h2>Vì sao có lớp này</h2>
 *
 * <p>Bảng {@code intrinsic} vốn chỉ được đọc <b>một lần lúc khởi động</b> trong
 * {@code Manager}, rồi nằm im trong bộ nhớ. Không có đường nào xem hay sửa nó
 * từ panel, và sửa thẳng trong CSDL thì phải dựng lại máy chủ mới thấy.</p>
 *
 * <p>Lớp này là đường vào duy nhất cho tab "Nội tại" của panel: đọc, sửa, thêm,
 * xoá, và <b>nạp lại thẳng vào bộ nhớ</b> nên có hiệu lực ngay.</p>
 *
 * <h2>Id là thứ không được đổi bừa</h2>
 *
 * <p>Tác dụng của từng nội tại <b>gắn cứng theo id</b> trong mã: {@code NPoint}
 * và {@code SkillService} hỏi {@code intrinsic.id == 1}, {@code == 16}… rồi mới
 * cộng phần trăm tương ứng. Đổi id của một dòng là đổi luôn tác dụng của nó
 * sang một chiêu khác, còn dòng mang id lạ thì <b>không có tác dụng gì</b> —
 * nó vẫn hiện tên, vẫn bốc ra được, chỉ là không làm gì cả.</p>
 *
 * <p>Vì thế {@link #luu} cảnh báo khi gặp id ngoài danh sách đã có tác dụng.</p>
 */
public final class NoiTaiDAO {

    private NoiTaiDAO() {
    }

    /**
     * Những id nội tại <b>đã có tác dụng</b> viết trong mã.
     *
     * <p>Đọc từ {@code NPoint.getDameAttack}, {@code NPoint.calPoint} và
     * {@code SkillService.affterUseSkill}. Dòng mang id ngoài danh sách này vẫn
     * chạy được nhưng không cộng gì cho người chơi.</p>
     */
    private static final int[] ID_CO_TAC_DUNG = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26
    };

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `intrinsic` ("
            + " `id` int(11) NOT NULL,"
            + " `name` varchar(255) NOT NULL DEFAULT '',"
            + " `param_from_1` smallint(6) NOT NULL DEFAULT 0,"
            + " `param_to_1` smallint(6) NOT NULL DEFAULT 0,"
            + " `param_from_2` smallint(6) NOT NULL DEFAULT 0,"
            + " `param_to_2` smallint(6) NOT NULL DEFAULT 0,"
            + " `icon` smallint(6) NOT NULL DEFAULT -1,"
            + " `gender` tinyint(4) NOT NULL DEFAULT 3,"
            + " PRIMARY KEY (`id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTao;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (NoiTaiDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTao = true;
            } catch (Exception ex) {
                Logger.logException(NoiTaiDAO.class, ex,
                        "Không tạo được bảng intrinsic");
            }
        }
    }

    /** Tên hành tinh của một mã {@code gender}. */
    public static String tenHanhTinh(int gender) {
        switch (gender) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namếc";
            case 2:
                return "Xayda";
            default:
                return "Dùng chung";
        }
    }

    /** Đọc toàn bộ bảng, xếp theo id. */
    public static List<Intrinsic> danhSach() {
        damBaoBang();
        List<Intrinsic> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM intrinsic ORDER BY id");
            while (rs.next()) {
                Intrinsic it = new Intrinsic();
                it.id = rs.getInt("id");
                it.name = rs.getString("name");
                it.paramFrom1 = rs.getShort("param_from_1");
                it.paramTo1 = rs.getShort("param_to_1");
                it.paramFrom2 = rs.getShort("param_from_2");
                it.paramTo2 = rs.getShort("param_to_2");
                it.icon = rs.getShort("icon");
                it.gender = (byte) rs.getInt("gender");
                ra.add(it);
            }
        } catch (Exception ex) {
            Logger.logException(NoiTaiDAO.class, ex, "Lỗi đọc bảng intrinsic");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Id này đã có tác dụng viết trong mã chưa. */
    public static boolean coTacDung(int id) {
        for (int x : ID_CO_TAC_DUNG) {
            if (x == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lưu một dòng (thêm mới hoặc ghi đè theo id), rồi nạp lại vào bộ nhớ.
     *
     * @return {@code null} nếu xong; ngược lại là câu báo lỗi
     */
    public static String luu(Intrinsic it) {
        damBaoBang();
        if (it == null) {
            return "Không có dữ liệu.";
        }
        if (it.id < 0) {
            return "Id phải từ 0 trở lên.";
        }
        if (it.name == null || it.name.trim().isEmpty()) {
            return "Phải có tên.";
        }
        if (it.paramTo1 < it.paramFrom1 || it.paramTo2 < it.paramFrom2) {
            return "Trị số \"đến\" không được nhỏ hơn \"từ\".";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO intrinsic (id, name, param_from_1, param_to_1,"
                    + " param_from_2, param_to_2, icon, gender)"
                    + " VALUES (?,?,?,?,?,?,?,?)"
                    + " ON DUPLICATE KEY UPDATE name = VALUES(name),"
                    + " param_from_1 = VALUES(param_from_1),"
                    + " param_to_1 = VALUES(param_to_1),"
                    + " param_from_2 = VALUES(param_from_2),"
                    + " param_to_2 = VALUES(param_to_2),"
                    + " icon = VALUES(icon), gender = VALUES(gender)",
                    it.id, it.name.trim(), it.paramFrom1, it.paramTo1,
                    it.paramFrom2, it.paramTo2, it.icon, it.gender);
            napLaiVaoBoNho();
            return null;
        } catch (Exception ex) {
            Logger.logException(NoiTaiDAO.class, ex, "Lỗi lưu nội tại");
            return "Lỗi lưu: " + ex.getMessage();
        }
    }

    public static String xoa(int id) {
        damBaoBang();
        if (id == 0) {
            return "Không xoá được dòng id 0 — đó là \"chưa có nội tại\".";
        }
        try {
            ConnectDB.executeUpdate("DELETE FROM intrinsic WHERE id = ?", id);
            napLaiVaoBoNho();
            return null;
        } catch (Exception ex) {
            Logger.logException(NoiTaiDAO.class, ex, "Lỗi xoá nội tại");
            return "Lỗi xoá: " + ex.getMessage();
        }
    }

    /**
     * Đọc bảng rồi <b>thay thẳng</b> bốn danh sách trong {@code Manager}.
     *
     * <h2>Vì sao phải thay cả bốn</h2>
     *
     * <p>{@code Manager} giữ một danh sách tổng và ba danh sách theo hành tinh.
     * Ba danh sách kia mới là thứ {@code IntrinsicService.getIntrinsics} đọc để
     * bốc, nên chỉ nạp lại danh sách tổng thì sửa trên panel không có tác dụng
     * gì với việc bốc nội tại.</p>
     *
     * <p>{@code gender} bằng 3 (hoặc bất kỳ trị nào ngoài 0/1/2) nghĩa là dùng
     * chung — vào cả ba danh sách, y như cách {@code Manager} nạp lúc khởi
     * động.</p>
     */
    public static synchronized void napLaiVaoBoNho() {
        List<Intrinsic> ds = danhSach();
        if (ds.isEmpty()) {
            // Bang rong: giu nguyen ban dang chay con hon xoa sach roi khong ai
            // co noi tai nao.
            Logger.error("Bảng intrinsic rỗng — giữ nguyên bản đang chạy\n");
            return;
        }
        Manager.INTRINSICS.clear();
        Manager.INTRINSIC_TD.clear();
        Manager.INTRINSIC_NM.clear();
        Manager.INTRINSIC_XD.clear();
        for (Intrinsic it : ds) {
            switch (it.gender) {
                case 0:
                    Manager.INTRINSIC_TD.add(it);
                    break;
                case 1:
                    Manager.INTRINSIC_NM.add(it);
                    break;
                case 2:
                    Manager.INTRINSIC_XD.add(it);
                    break;
                default:
                    Manager.INTRINSIC_TD.add(it);
                    Manager.INTRINSIC_NM.add(it);
                    Manager.INTRINSIC_XD.add(it);
                    break;
            }
            Manager.INTRINSICS.add(it);
        }
        Logger.success("CONFIG", "Đã nạp lại " + ds.size() + " nội tại từ bảng intrinsic");
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
