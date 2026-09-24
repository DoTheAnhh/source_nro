package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Dữ liệu <b>thẻ tháng</b>: các gói bán, quà của từng gói, thẻ đang có của
 * từng người chơi và lượt nhận quà theo ngày.
 *
 * <h2>Bảng</h2>
 * <ul>
 *   <li>{@code the_thang_goi} — mỗi bậc thẻ một dòng: tên, giá, loại tiền, số
 *       ngày, và phần trăm ưu đãi (HP, KI, sức đánh, tiềm năng).</li>
 *   <li>{@code the_thang_qua} — quà của từng bậc: loại 0 phát ngay lúc mua,
 *       loại 1 phát mỗi ngày.</li>
 *   <li>{@code the_thang_nguoi} — thẻ đang có của người chơi. <b>Đây là nguồn
 *       sự thật</b>: {@code Player.THE_THANG} chỉ là bản sao nạp vào lúc đăng
 *       nhập. Lý do: tiền trừ thẳng vào CSDL ngay lúc mua, còn dữ liệu nhân
 *       vật chỉ lưu khi thoát hoặc theo nhịp — máy chủ sập giữa hai lúc ấy thì
 *       người chơi mất tiền mà không có thẻ.</li>
 *   <li>{@code the_thang_nhan} — khoá chính (người chơi, ngày) nên một ngày chỉ
 *       ghi được một lần, kể cả khi hai gói tin nhận đến cùng lúc.</li>
 * </ul>
 *
 * <p>Tự tạo bảng và gieo gói mặc định lúc dùng lần đầu: máy chủ thật chỉ
 * {@code git pull} rồi chạy, không ai chạy SQL tay.</p>
 */
public class TheThangDAO {

    public static final String TIEN_VND = "vnd";
    public static final String TIEN_HONG_NGOC = "hong_ngoc";
    public static final String TIEN_NGOC = "ngoc";

    /** Thứ tự cho ô chọn trên panel. */
    public static final String[] CAC_LOAI_TIEN = {TIEN_VND, TIEN_HONG_NGOC, TIEN_NGOC};

    public static String tenTien(String loai) {
        if (TIEN_HONG_NGOC.equals(loai)) {
            return "hồng ngọc";
        }
        if (TIEN_NGOC.equals(loai)) {
            return "ngọc";
        }
        return "VNĐ";
    }

    /** Quà phát ngay lúc mua. */
    public static final int QUA_MUA = 0;
    /** Quà nhận mỗi ngày khi thẻ còn hạn. */
    public static final int QUA_NGAY = 1;

    public static final long MOT_NGAY_MS = 24L * 60 * 60 * 1000;

    private static final long HAN_BO_NHO_MS = 30_000L;

    // =====================================================================
    //  Kiểu dữ liệu
    // =====================================================================
    /** Một bậc thẻ đang bán. */
    public static final class Goi {

        /** 1 thường, 2 cao cấp — đúng con số lưu trong {@code Player.THE_THANG}. */
        public int bac;
        public String ten = "";
        public long gia;
        public String loaiTien = TIEN_VND;
        public int soNgay = 30;
        public int ptHp;
        public int ptKi;
        public int ptSd;
        public int ptTiemNang;
        public boolean bat = true;

        /** "50.000 VNĐ". */
        public String giaDeDoc() {
            return nro.core.util.Util.soCham(gia) + " " + tenTien(loaiTien);
        }

        /** Một dòng ưu đãi, gộp các chỉ số bằng nhau cho gọn. */
        public String uuDai() {
            StringBuilder s = new StringBuilder();
            if (ptHp == ptKi && ptKi == ptSd && ptHp > 0) {
                s.append("+").append(ptHp).append("% HP, KI, sức đánh");
            } else {
                noi(s, ptHp, "HP");
                noi(s, ptKi, "KI");
                noi(s, ptSd, "sức đánh");
            }
            noi(s, ptTiemNang, "tiềm năng");
            return s.length() == 0 ? "Không có ưu đãi chỉ số" : s.toString();
        }

        private static void noi(StringBuilder s, int pt, String ten) {
            if (pt <= 0) {
                return;
            }
            if (s.length() > 0) {
                s.append(" · ");
            }
            s.append("+").append(pt).append("% ").append(ten);
        }
    }

    /** Một món quà của một bậc. */
    public static final class Qua {

        public int id;
        public int bac;
        public int loai;
        public int itemId;
        public int soLuong = 1;
        /** Chỉ số gắn kèm, dạng "50=5,77=10"; rỗng là không có. */
        public String chiSo = "";
    }

    /** Thẻ của một người chơi, đọc từ bảng. */
    public static final class The {

        public int bac;
        public long hetHan;
        public int lanMua;
    }

    // =====================================================================
    //  Tạo bảng
    // =====================================================================
    private static boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS the_thang_goi ("
                    + " bac INT(11) NOT NULL,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " gia BIGINT(20) NOT NULL DEFAULT 0,"
                    + " loai_tien VARCHAR(20) NOT NULL DEFAULT 'vnd',"
                    + " so_ngay INT(11) NOT NULL DEFAULT 30,"
                    + " pt_hp INT(11) NOT NULL DEFAULT 0,"
                    + " pt_ki INT(11) NOT NULL DEFAULT 0,"
                    + " pt_sd INT(11) NOT NULL DEFAULT 0,"
                    + " pt_tiem_nang INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (bac)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS the_thang_qua ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " bac INT(11) NOT NULL,"
                    + " loai INT(11) NOT NULL DEFAULT 1,"
                    + " item_id INT(11) NOT NULL,"
                    + " so_luong INT(11) NOT NULL DEFAULT 1,"
                    + " chi_so VARCHAR(255) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (id), KEY (bac, loai)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS the_thang_nguoi ("
                    + " player_id INT(11) NOT NULL,"
                    + " bac INT(11) NOT NULL DEFAULT 0,"
                    + " het_han BIGINT(20) NOT NULL DEFAULT 0,"
                    + " lan_mua INT(11) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS the_thang_nhan ("
                    + " player_id INT(11) NOT NULL,"
                    + " ngay INT(11) NOT NULL,"
                    + " PRIMARY KEY (player_id, ngay)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            gieoGoc();
        } catch (Exception ex) {
            daTaoBang = false;
            Logger.logException(TheThangDAO.class, ex, "Không tạo được bảng thẻ tháng");
        }
    }

    /**
     * Hai gói mặc định, mang đúng con số ưu đãi mà {@code NPoint} viết cứng
     * trước đây (7%/10% chỉ số, 100%/150% tiềm năng) — để máy đang chạy
     * không thấy chỉ số của ai đổi khi cập nhật.
     */
    private static void gieoGoc() throws Exception {
        ConnectDB.executeUpdate("INSERT IGNORE INTO the_thang_goi"
                + " (bac, ten, gia, loai_tien, so_ngay, pt_hp, pt_ki, pt_sd, pt_tiem_nang, bat)"
                + " VALUES (1, 'Thẻ tháng', 50000, 'vnd', 30, 7, 7, 7, 100, 1),"
                + " (2, 'Thẻ tháng cao cấp', 100000, 'vnd', 30, 10, 10, 10, 150, 1)");
        if (demDong("the_thang_qua") == 0) {
            // 457 = thoi vang. Quan tri doi tren panel.
            ConnectDB.executeUpdate("INSERT INTO the_thang_qua (bac, loai, item_id, so_luong)"
                    + " VALUES (1, 0, 457, 20), (1, 1, 457, 2),"
                    + " (2, 0, 457, 50), (2, 1, 457, 5)");
        }
    }

    // =====================================================================
    //  Gói
    // =====================================================================
    private static final List<Goi> GOI = new ArrayList<>();
    private static long lucDocGoi;

    private static synchronized void docGoi() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocGoi < HAN_BO_NHO_MS && !GOI.isEmpty()) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM the_thang_goi ORDER BY bac");
            List<Goi> moi = new ArrayList<>();
            while (rs.next()) {
                Goi g = new Goi();
                g.bac = rs.getInt("bac");
                g.ten = rs.getString("ten");
                g.gia = rs.getLong("gia");
                g.loaiTien = rs.getString("loai_tien");
                g.soNgay = rs.getInt("so_ngay");
                g.ptHp = rs.getInt("pt_hp");
                g.ptKi = rs.getInt("pt_ki");
                g.ptSd = rs.getInt("pt_sd");
                g.ptTiemNang = rs.getInt("pt_tiem_nang");
                g.bat = rs.getInt("bat") == 1;
                if (g.ten == null) {
                    g.ten = "";
                }
                moi.add(g);
            }
            GOI.clear();
            GOI.addAll(moi);
            lucDocGoi = bayGio;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không đọc được gói thẻ tháng");
        } finally {
            dong(rs);
        }
    }

    /** Mọi gói, kể cả gói tắt (panel cần). */
    public static synchronized List<Goi> dsGoi() {
        docGoi();
        return new ArrayList<>(GOI);
    }

    /** Gói của bậc này, hoặc {@code null}. */
    public static synchronized Goi goi(int bac) {
        docGoi();
        for (Goi g : GOI) {
            if (g.bac == bac) {
                return g;
            }
        }
        return null;
    }

    public static void luuGoi(Goi g) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO the_thang_goi"
                    + " (bac, ten, gia, loai_tien, so_ngay, pt_hp, pt_ki, pt_sd, pt_tiem_nang, bat)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten), gia = VALUES(gia),"
                    + " loai_tien = VALUES(loai_tien), so_ngay = VALUES(so_ngay),"
                    + " pt_hp = VALUES(pt_hp), pt_ki = VALUES(pt_ki), pt_sd = VALUES(pt_sd),"
                    + " pt_tiem_nang = VALUES(pt_tiem_nang), bat = VALUES(bat)",
                    g.bac, g.ten, g.gia, g.loaiTien, g.soNgay, g.ptHp, g.ptKi, g.ptSd,
                    g.ptTiemNang, g.bat ? 1 : 0);
            lucDocGoi = 0;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không lưu được gói thẻ tháng");
        }
    }

    // --- ưu đãi, đọc trong NPoint mỗi lần tính chỉ số ---
    /** % HP của bậc này; bậc không có trong bảng thì 0. */
    public static int ptHp(int bac) {
        Goi g = goi(bac);
        return g == null ? 0 : g.ptHp;
    }

    public static int ptKi(int bac) {
        Goi g = goi(bac);
        return g == null ? 0 : g.ptKi;
    }

    public static int ptSd(int bac) {
        Goi g = goi(bac);
        return g == null ? 0 : g.ptSd;
    }

    public static int ptTiemNang(int bac) {
        Goi g = goi(bac);
        return g == null ? 0 : g.ptTiemNang;
    }

    /** Tên bậc để ghi vào dòng giải thích chỉ số. */
    public static String tenBac(int bac) {
        Goi g = goi(bac);
        return (g == null || g.ten.isEmpty()) ? ("Thẻ tháng bậc " + bac) : g.ten;
    }

    // =====================================================================
    //  Quà
    // =====================================================================
    private static final List<Qua> QUA = new ArrayList<>();
    private static long lucDocQua;

    private static synchronized void docQua() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocQua < HAN_BO_NHO_MS && lucDocQua != 0) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM the_thang_qua ORDER BY bac, loai, id");
            List<Qua> moi = new ArrayList<>();
            while (rs.next()) {
                Qua q = new Qua();
                q.id = rs.getInt("id");
                q.bac = rs.getInt("bac");
                q.loai = rs.getInt("loai");
                q.itemId = rs.getInt("item_id");
                q.soLuong = rs.getInt("so_luong");
                q.chiSo = rs.getString("chi_so");
                if (q.chiSo == null) {
                    q.chiSo = "";
                }
                moi.add(q);
            }
            QUA.clear();
            QUA.addAll(moi);
            lucDocQua = bayGio;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không đọc được quà thẻ tháng");
        } finally {
            dong(rs);
        }
    }

    /** Quà của một bậc, một loại ({@link #QUA_MUA} hoặc {@link #QUA_NGAY}). */
    public static synchronized List<Qua> dsQua(int bac, int loai) {
        docQua();
        List<Qua> ra = new ArrayList<>();
        for (Qua q : QUA) {
            if (q.bac == bac && q.loai == loai) {
                ra.add(q);
            }
        }
        return ra;
    }

    public static void themQua(int bac, int loai, int itemId, int soLuong, String chiSo) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO the_thang_qua (bac, loai, item_id, so_luong, chi_so)"
                    + " VALUES (?, ?, ?, ?, ?)", bac, loai, itemId, Math.max(1, soLuong),
                    chiSo == null ? "" : chiSo.trim());
            lucDocQua = 0;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không thêm được quà thẻ tháng");
        }
    }

    public static void suaQua(int id, int soLuong, String chiSo) {
        try {
            ConnectDB.executeUpdate("UPDATE the_thang_qua SET so_luong = ?, chi_so = ? WHERE id = ?",
                    Math.max(1, soLuong), chiSo == null ? "" : chiSo.trim(), id);
            lucDocQua = 0;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không sửa được quà thẻ tháng");
        }
    }

    public static void xoaQua(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM the_thang_qua WHERE id = ?", id);
            lucDocQua = 0;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không xoá được quà thẻ tháng");
        }
    }

    // =====================================================================
    //  Thẻ của người chơi
    // =====================================================================
    /** Thẻ đang lưu của người này (còn hạn hay không), hoặc {@code null}. */
    public static The theCua(long playerId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT bac, het_han, lan_mua FROM the_thang_nguoi"
                    + " WHERE player_id = ?", playerId);
            if (!rs.next()) {
                return null;
            }
            The t = new The();
            t.bac = rs.getInt("bac");
            t.hetHan = rs.getLong("het_han");
            t.lanMua = rs.getInt("lan_mua");
            return t;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không đọc được thẻ tháng của người chơi");
            return null;
        } finally {
            dong(rs);
        }
    }

    /**
     * Ghi thẻ của một người.
     *
     * @param laMua {@code true} khi người chơi tự mua — cộng lượt mua. Quản trị
     *              tặng thì không cộng.
     * @return {@code true} nếu ghi được
     */
    public static boolean datThe(long playerId, int bac, long hetHan, boolean laMua) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO the_thang_nguoi (player_id, bac, het_han, lan_mua)"
                    + " VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE bac = VALUES(bac),"
                    + " het_han = VALUES(het_han), lan_mua = lan_mua + ?",
                    playerId, bac, hetHan, laMua ? 1 : 0, laMua ? 1 : 0);
            return true;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không lưu được thẻ tháng của người chơi "
                    + playerId);
            return false;
        }
    }

    /**
     * Ghi lượt nhận quà của một ngày.
     *
     * @return {@code false} nếu ngày ấy đã có lượt — khoá chính chặn, nên hai
     *         gói tin nhận đến cùng lúc cũng chỉ một cái qua được
     */
    public static boolean ghiNhan(long playerId, int ngay) {
        try {
            damBaoBang();
            return ConnectDB.executeUpdate("INSERT IGNORE INTO the_thang_nhan (player_id, ngay)"
                    + " VALUES (?, ?)", playerId, ngay) > 0;
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không ghi được lượt nhận thẻ tháng");
            return false;
        }
    }

    /** Trả lại lượt nhận khi phát quà không thành (hết chỗ giữa chừng…). */
    public static void boNhan(long playerId, int ngay) {
        try {
            ConnectDB.executeUpdate("DELETE FROM the_thang_nhan WHERE player_id = ? AND ngay = ?",
                    playerId, ngay);
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không trả được lượt nhận thẻ tháng");
        }
    }

    public static boolean daNhan(long playerId, int ngay) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT 1 FROM the_thang_nhan WHERE player_id = ? AND ngay = ?",
                    playerId, ngay);
            return rs.next();
        } catch (Exception ex) {
            return false;
        } finally {
            dong(rs);
        }
    }

    /** Một dòng trong danh sách người đang có thẻ, cho panel. */
    public static final class NguoiCoThe {

        public long playerId;
        public String ten = "";
        public int bac;
        public long hetHan;
        public int lanMua;
    }

    /** Người đang có thẻ còn hạn, hạn gần nhất trước. */
    public static List<NguoiCoThe> dsNguoiCoThe() {
        damBaoBang();
        List<NguoiCoThe> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT t.player_id, t.bac, t.het_han, t.lan_mua, p.name"
                    + " FROM the_thang_nguoi t LEFT JOIN player p ON p.id = t.player_id"
                    + " WHERE t.het_han > ? ORDER BY t.het_han", System.currentTimeMillis());
            while (rs.next()) {
                NguoiCoThe n = new NguoiCoThe();
                n.playerId = rs.getLong("player_id");
                n.bac = rs.getInt("bac");
                n.hetHan = rs.getLong("het_han");
                n.lanMua = rs.getInt("lan_mua");
                n.ten = rs.getString("name");
                if (n.ten == null) {
                    n.ten = "#" + n.playerId;
                }
                ra.add(n);
            }
        } catch (Exception ex) {
            Logger.logException(TheThangDAO.class, ex, "Không đọc được danh sách người có thẻ tháng");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Id nhân vật theo tên, hoặc -1. */
    public static long idTheoTen(String ten) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM player WHERE name = ?", ten);
            return rs.next() ? rs.getLong("id") : -1;
        } catch (Exception ex) {
            return -1;
        } finally {
            dong(rs);
        }
    }

    // =====================================================================
    private static int demDong(String bang) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM " + bang);
            return rs.next() ? rs.getInt("n") : 0;
        } finally {
            dong(rs);
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.dispose();
        } catch (Exception boQua) {
            // Dong khong duoc thi cung khong lam gi them duoc.
        }
    }
}
