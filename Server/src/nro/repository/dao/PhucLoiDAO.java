package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Kho dữ liệu cho hệ thống <b>Phúc lợi</b> — quà theo mốc tích luỹ.
 *
 * <h2>Bốn bảng</h2>
 *
 * <ul>
 *   <li>{@code phuc_loi_nhom} — các mục bên trái ("Quà Online", "Quà Cày Chay"…),
 *       mỗi mục chọn <b>đếm theo cái gì</b> qua cột {@code loai}</li>
 *   <li>{@code phuc_loi_moc} — từng mốc trong một nhóm ("Đạt mốc 50.000")</li>
 *   <li>{@code phuc_loi_qua} — vật phẩm của một mốc, mỗi dòng một món</li>
 *   <li>{@code phuc_loi_da_nhan} — ai đã nhận mốc nào, để không nhận hai lần</li>
 * </ul>
 *
 * <h2>Vì sao tách bảng "đã nhận" riêng</h2>
 *
 * <p>Có thể nhét cờ đã-nhận vào một cột JSON trong bảng {@code player} như cách
 * phần còn lại của máy chủ vẫn làm. Nhưng làm vậy thì không truy vấn được "mốc
 * này bao nhiêu người đã nhận", và mỗi lần thêm mốc lại phải lo dữ liệu cũ thiếu
 * ô. Một bảng hai cột giải quyết cả hai.</p>
 *
 * <h2>Tiến độ lấy ở đâu</h2>
 *
 * <p>Phần lớn loại đọc thẳng từ người chơi đang online ({@code SUC_MANH},
 * {@code NHIEM_VU}, {@code MAY_DAM}) nên không cần lưu. Riêng
 * {@code ONLINE_PHUT} và {@code TU_DAT} là con số máy chủ tự cộng dồn hoặc
 * quản trị tự đặt, nên có bảng {@code phuc_loi_tien_do} giữ hộ.</p>
 */
public class PhucLoiDAO {

    /** Đếm theo sức mạnh hiện tại. */
    public static final String LOAI_SUC_MANH = "SUC_MANH";
    /** Đếm theo id nhiệm vụ chính đang làm. */
    public static final String LOAI_NHIEM_VU = "NHIEM_VU";
    /** Đếm theo kỷ lục sát thương 30 giây ở máy đo sức mạnh. */
    public static final String LOAI_MAY_DAM = "MAY_DAM";
    /** Đếm theo số phút đã online — máy chủ tự cộng. */
    public static final String LOAI_ONLINE = "ONLINE_PHUT";

    /**
     * Những loại đếm <b>reset mỗi ngày</b>.
     *
     * <p>Quà online tính theo số phút chơi trong <i>ngày hôm nay</i>, nên sang
     * ngày mới thì cả tiến độ lẫn các mốc đã nhận đều coi như chưa có. Các loại
     * khác ({@code SUC_MANH}, {@code NHIEM_VU}…) là mốc tích luỹ cả đời, nhận
     * một lần là xong — không nằm trong danh sách này.</p>
     *
     * <p>Thêm một loại reset theo ngày = thêm một dòng vào đây.</p>
     */
    private static final java.util.Set<String> LOAI_THEO_NGAY
            = new java.util.HashSet<>(java.util.Arrays.asList(LOAI_ONLINE));

    /** Loại đếm này có reset mỗi ngày không. */
    public static boolean theoNgay(String loai) {
        return loai != null && LOAI_THEO_NGAY.contains(loai);
    }

    /**
     * Số thứ tự ngày hôm nay, tính theo <b>múi giờ của máy chủ</b>.
     *
     * <p>Dùng ngày lịch chứ không phải "24 giờ kể từ lần nhận": mốc đổi ngày là
     * nửa đêm, đúng như người chơi hiểu chữ "mỗi ngày". Nếu tính theo 24 giờ
     * trôi thì ai nhận lúc 23h hôm nay phải chờ tới 23h hôm sau, và giờ nhận
     * cứ trôi dần mỗi ngày.</p>
     */
    public static int ngayHomNay() {
        return (int) java.time.LocalDate.now().toEpochDay();
    }
    /** Con số quản trị tự đặt cho từng người. */
    public static final String LOAI_TU_DAT = "TU_DAT";

    /** Danh sách loại để đổ vào ô chọn trên panel. */
    public static final String[] CAC_LOAI = {
        LOAI_SUC_MANH, LOAI_NHIEM_VU, LOAI_MAY_DAM, LOAI_ONLINE, LOAI_TU_DAT
    };

    /** Tên tiếng Việt của một loại, để hiện trên panel và trong game. */
    public static String tenLoai(String loai) {
        if (loai == null) {
            return "(không rõ)";
        }
        switch (loai) {
            case LOAI_SUC_MANH:
                return "Sức mạnh";
            case LOAI_NHIEM_VU:
                return "Nhiệm vụ chính";
            case LOAI_MAY_DAM:
                return "Sát thương 30 giây";
            case LOAI_ONLINE:
                return "Số phút online";
            case LOAI_TU_DAT:
                return "Tự đặt";
            default:
                return loai;
        }
    }

    /**
     * Đơn vị viết sau con số của một loại đếm.
     *
     * <p>Có đơn vị thì "Mốc 30 phút · còn thiếu 23 phút" đọc ra ngay là gì; thiếu
     * nó thì "Mốc 30" có thể là 30 phút, 30 nhiệm vụ hay 30 sát thương.</p>
     */
    public static String donVi(String loai) {
        if (loai == null) {
            return "";
        }
        switch (loai) {
            case LOAI_ONLINE:
                return "phút";
            case LOAI_MAY_DAM:
                return "sát thương";
            case LOAI_SUC_MANH:
                return "sức mạnh";
            case LOAI_NHIEM_VU:
                return "";      // "Mốc nhiệm vụ 30" da du nghia, them chu la thua
            default:
                return "";
        }
    }

    /** Nhãn đứng trước con số mốc. */
    public static String tienTo(String loai) {
        return LOAI_NHIEM_VU.equals(loai) ? "Mốc nhiệm vụ" : "Mốc";
    }


    private static boolean daTaoBang;

    /**
     * Tạo bốn bảng nếu chưa có.
     *
     * <p>Gọi ở đầu mọi hàm công khai thay vì trông vào một bước cài đặt: người
     * dùng chép mã sang máy khác là chạy được ngay, không phải nhớ chạy SQL.</p>
     */
    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS phuc_loi_nhom ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " ten VARCHAR(80) NOT NULL,"
                    + " loai VARCHAR(24) NOT NULL DEFAULT 'SUC_MANH',"
                    + " thu_tu INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " ghi_chu VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS phuc_loi_moc ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " nhom_id INT(11) NOT NULL,"
                    + " moc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " thu_tu INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " ghi_chu VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (id), KEY idx_nhom (nhom_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS phuc_loi_qua ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " moc_id INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL,"
                    + " so_luong INT(11) NOT NULL DEFAULT 1,"
                    + " chi_so VARCHAR(255) DEFAULT '[]',"
                    + " PRIMARY KEY (id), KEY idx_moc (moc_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS phuc_loi_da_nhan ("
                    + " player_id BIGINT(20) NOT NULL,"
                    + " moc_id INT(11) NOT NULL,"
                    + " luc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id, moc_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS phuc_loi_tien_do ("
                    + " player_id BIGINT(20) NOT NULL,"
                    + " loai VARCHAR(24) NOT NULL,"
                    + " gia_tri BIGINT(20) NOT NULL DEFAULT 0,"
                    + " ngay INT(11) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id, loai)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // Hai cot `ngay` cho may chu da chay tu truoc, khi hai bang tren
            // duoc tao ma chua co cot nay.
            ConnectDB.executeUpdate("ALTER TABLE phuc_loi_tien_do"
                    + " ADD COLUMN IF NOT EXISTS ngay INT(11) NOT NULL DEFAULT 0");
            ConnectDB.executeUpdate("ALTER TABLE phuc_loi_da_nhan"
                    + " ADD COLUMN IF NOT EXISTS ngay INT(11) NOT NULL DEFAULT 0");
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Không tạo được bảng phúc lợi");
        }
    }

    // =====================================================================
    //  Nhóm
    // =====================================================================
    public static final class Nhom {

        public int id;
        public String ten;
        public String loai = LOAI_SUC_MANH;
        public int thuTu;
        public boolean bat = true;
        public String ghiChu;
    }

    public static List<Nhom> dsNhom(boolean chiBat) {
        damBaoBang();
        List<Nhom> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM phuc_loi_nhom"
                    + (chiBat ? " WHERE bat = 1" : "") + " ORDER BY thu_tu, id");
            while (rs.next()) {
                Nhom n = new Nhom();
                n.id = rs.getInt("id");
                n.ten = rs.getStringOrNull("ten");
                n.loai = rs.getStringOrNull("loai");
                n.thuTu = rs.getInt("thu_tu");
                n.bat = rs.getBoolean("bat");
                n.ghiChu = rs.getStringOrNull("ghi_chu");
                ra.add(n);
            }
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi đọc phuc_loi_nhom");
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static String luuNhom(Nhom n) {
        damBaoBang();
        if (n.ten == null || n.ten.trim().isEmpty()) {
            return "Chưa nhập tên nhóm.";
        }
        try {
            if (n.id > 0) {
                ConnectDB.executeUpdate("UPDATE phuc_loi_nhom SET ten = ?, loai = ?,"
                        + " thu_tu = ?, bat = ?, ghi_chu = ? WHERE id = ?",
                        n.ten.trim(), n.loai, n.thuTu, n.bat ? 1 : 0, n.ghiChu, n.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO phuc_loi_nhom"
                        + " (ten, loai, thu_tu, bat, ghi_chu) VALUES (?, ?, ?, ?, ?)",
                        n.ten.trim(), n.loai, n.thuTu, n.bat ? 1 : 0, n.ghiChu);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi lưu nhóm phúc lợi");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /** Xoá nhóm kèm mọi mốc và quà bên trong. */
    public static String xoaNhom(int id) {
        damBaoBang();
        try {
            for (Moc m : dsMoc(id, false)) {
                xoaMoc(m.id);
            }
            ConnectDB.executeUpdate("DELETE FROM phuc_loi_nhom WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi xoá nhóm phúc lợi");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    // =====================================================================
    //  Mốc
    // =====================================================================
    public static final class Moc {

        public int id;
        public int nhomId;
        public long moc;
        public int thuTu;
        public boolean bat = true;
        public String ghiChu;
    }

    public static List<Moc> dsMoc(int nhomId, boolean chiBat) {
        damBaoBang();
        List<Moc> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM phuc_loi_moc WHERE nhom_id = ?"
                    + (chiBat ? " AND bat = 1" : "") + " ORDER BY thu_tu, moc, id", nhomId);
            while (rs.next()) {
                Moc m = new Moc();
                m.id = rs.getInt("id");
                m.nhomId = rs.getInt("nhom_id");
                m.moc = rs.getLong("moc");
                m.thuTu = rs.getInt("thu_tu");
                m.bat = rs.getBoolean("bat");
                m.ghiChu = rs.getStringOrNull("ghi_chu");
                ra.add(m);
            }
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi đọc phuc_loi_moc");
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static String luuMoc(Moc m) {
        damBaoBang();
        if (m.nhomId <= 0) {
            return "Chưa chọn nhóm.";
        }
        try {
            if (m.id > 0) {
                ConnectDB.executeUpdate("UPDATE phuc_loi_moc SET nhom_id = ?, moc = ?,"
                        + " thu_tu = ?, bat = ?, ghi_chu = ? WHERE id = ?",
                        m.nhomId, m.moc, m.thuTu, m.bat ? 1 : 0, m.ghiChu, m.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO phuc_loi_moc"
                        + " (nhom_id, moc, thu_tu, bat, ghi_chu) VALUES (?, ?, ?, ?, ?)",
                        m.nhomId, m.moc, m.thuTu, m.bat ? 1 : 0, m.ghiChu);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi lưu mốc phúc lợi");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /** Xoá mốc kèm quà và mọi lượt đã nhận của nó. */
    public static String xoaMoc(int id) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM phuc_loi_qua WHERE moc_id = ?", id);
            ConnectDB.executeUpdate("DELETE FROM phuc_loi_da_nhan WHERE moc_id = ?", id);
            ConnectDB.executeUpdate("DELETE FROM phuc_loi_moc WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi xoá mốc phúc lợi");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    // =====================================================================
    //  Quà
    // =====================================================================
    public static final class Qua {

        public int id;
        public int mocId;
        public int itemId;
        public int soLuong = 1;
        public String chiSo = "[]";
    }

    public static List<Qua> dsQua(int mocId) {
        damBaoBang();
        List<Qua> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT * FROM phuc_loi_qua WHERE moc_id = ? ORDER BY id", mocId);
            while (rs.next()) {
                Qua q = new Qua();
                q.id = rs.getInt("id");
                q.mocId = rs.getInt("moc_id");
                q.itemId = rs.getInt("item_id");
                q.soLuong = rs.getInt("so_luong");
                q.chiSo = rs.getStringOrNull("chi_so");
                ra.add(q);
            }
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi đọc phuc_loi_qua");
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static String luuQua(Qua q) {
        damBaoBang();
        if (q.mocId <= 0) {
            return "Chưa chọn mốc.";
        }
        if (q.soLuong <= 0) {
            return "Số lượng phải lớn hơn 0.";
        }
        try {
            if (q.id > 0) {
                ConnectDB.executeUpdate("UPDATE phuc_loi_qua SET moc_id = ?, item_id = ?,"
                        + " so_luong = ?, chi_so = ? WHERE id = ?",
                        q.mocId, q.itemId, q.soLuong, q.chiSo, q.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO phuc_loi_qua"
                        + " (moc_id, item_id, so_luong, chi_so) VALUES (?, ?, ?, ?)",
                        q.mocId, q.itemId, q.soLuong, q.chiSo);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi lưu quà phúc lợi");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    public static String xoaQua(int id) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM phuc_loi_qua WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi xoá quà phúc lợi");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    // =====================================================================
    //  Đã nhận / tiến độ
    // =====================================================================
    /**
     * Người này đã nhận mốc đó chưa.
     *
     * @param loai loại đếm của nhóm chứa mốc. Với loại reset theo ngày, lượt
     *             nhận của những ngày trước <b>không</b> tính — nhờ vậy sang
     *             ngày mới là nhận lại được.
     */
    public static boolean daNhan(long playerId, int mocId, String loai) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            if (theoNgay(loai)) {
                rs = ConnectDB.executeQuery("SELECT 1 FROM phuc_loi_da_nhan"
                        + " WHERE player_id = ? AND moc_id = ? AND ngay = ?",
                        playerId, mocId, ngayHomNay());
                return rs.next();
            }
            rs = ConnectDB.executeQuery("SELECT 1 FROM phuc_loi_da_nhan"
                    + " WHERE player_id = ? AND moc_id = ?", playerId, mocId);
            return rs.next();
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi đọc phuc_loi_da_nhan");
            // Coi nhu DA nhan khi loi: tha khong phat con hon phat hai lan.
            return true;
        } finally {
            dong(rs);
        }
    }

    /**
     * Đánh dấu đã nhận.
     *
     * @return {@code true} nếu ghi được — {@code false} nghĩa là đã có sẵn dòng
     *         đó, tức người khác (hoặc chính lượt bấm trước) vừa nhận rồi
     */
    public static boolean ghiDaNhan(long playerId, int mocId, String loai) {
        damBaoBang();
        int homNay = ngayHomNay();
        try {
            if (theoNgay(loai)) {
                // Khoa chinh van la (player_id, moc_id) — mot dong cho moi moc,
                // cot `ngay` cho biet lan nhan gan nhat la ngay nao. Dieu kien
                // `ngay < ?` la cho chan hai lan bam: no chi cap nhat khi dong
                // dang mang ngay CU, nen bam lan thu hai trong cung ngay se
                // khong doi dong nao va tra ve false.
                int doi = ConnectDB.executeUpdate("INSERT INTO phuc_loi_da_nhan"
                        + " (player_id, moc_id, luc, ngay) VALUES (?, ?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE"
                        + " luc = IF(ngay < VALUES(ngay), VALUES(luc), luc),"
                        + " ngay = IF(ngay < VALUES(ngay), VALUES(ngay), ngay)",
                        playerId, mocId, System.currentTimeMillis(), homNay);
                // MySQL tra 1 khi chen moi, 2 khi cap nhat that su, 0 khi dong
                // da o dung trang thai do (tuc la da nhan trong hom nay).
                return doi != 0;
            }
            // Khoa chinh (player_id, moc_id) chan nhan hai lan ngay o CSDL, khong
            // trong vao viec kiem truoc do — hai lan bam that nhanh van an toan.
            ConnectDB.executeUpdate("INSERT INTO phuc_loi_da_nhan"
                    + " (player_id, moc_id, luc, ngay) VALUES (?, ?, ?, ?)",
                    playerId, mocId, System.currentTimeMillis(), homNay);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Xoá lượt đã nhận của một người ở một nhóm — dùng khi cần cho nhận lại. */
    public static String xoaDaNhanTheoNhom(long playerId, int nhomId) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE d FROM phuc_loi_da_nhan d"
                    + " JOIN phuc_loi_moc m ON m.id = d.moc_id"
                    + " WHERE d.player_id = ? AND m.nhom_id = ?", playerId, nhomId);
            return null;
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi xoá lượt đã nhận");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /** Con số tiến độ do máy chủ giữ hộ (ONLINE_PHUT, TU_DAT). */
    public static long tienDo(long playerId, String loai) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri, ngay FROM phuc_loi_tien_do"
                    + " WHERE player_id = ? AND loai = ?", playerId, loai);
            if (rs.next()) {
                // Loai reset theo ngay: con so cua ngay truoc coi nhu khong co.
                // Khong xoa dong ngay tai day — de dong cu lai roi ghi de o lan
                // cong dau tien cua ngay moi, nen ham DOC nay khong ghi CSDL.
                if (theoNgay(loai) && rs.getInt("ngay") != ngayHomNay()) {
                    return 0;
                }
                return rs.getLong("gia_tri");
            }
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi đọc phuc_loi_tien_do");
        } finally {
            dong(rs);
        }
        return 0;
    }

    /** Cộng thêm vào tiến độ; {@code them} âm là trừ đi. */
    public static void congTienDo(long playerId, String loai, long them) {
        damBaoBang();
        try {
            int homNay = ngayHomNay();
            if (theoNgay(loai)) {
                // Sang ngay moi thi GAN (dat lai tu 0 roi cong), cung ngay thi
                // CONG. Lam trong mot cau de khong co ke ho giua doc va ghi.
                ConnectDB.executeUpdate("INSERT INTO phuc_loi_tien_do"
                        + " (player_id, loai, gia_tri, ngay) VALUES (?, ?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE"
                        + " gia_tri = IF(ngay = VALUES(ngay),"
                        + "              gia_tri + VALUES(gia_tri),"
                        + "              VALUES(gia_tri)),"
                        + " ngay = VALUES(ngay)",
                        playerId, loai, them, homNay);
                return;
            }
            ConnectDB.executeUpdate("INSERT INTO phuc_loi_tien_do"
                    + " (player_id, loai, gia_tri, ngay) VALUES (?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE gia_tri = gia_tri + ?",
                    playerId, loai, them, homNay, them);
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi cộng tiến độ phúc lợi");
        }
    }

    /** Đặt thẳng một con số tiến độ. */
    public static void datTienDo(long playerId, String loai, long giaTri) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO phuc_loi_tien_do"
                    + " (player_id, loai, gia_tri, ngay) VALUES (?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE gia_tri = ?, ngay = VALUES(ngay)",
                    playerId, loai, giaTri, ngayHomNay(), giaTri);
        } catch (Exception ex) {
            Logger.logException(PhucLoiDAO.class, ex, "Lỗi đặt tiến độ phúc lợi");
        }
    }

    private static void dong(CrisResultSet rs) {
        try {
            if (rs != null) {
                rs.dispose();
            }
        } catch (Exception ignored) {
            // Dong that bai thi cung khong lam gi duoc them.
        }
    }
}
