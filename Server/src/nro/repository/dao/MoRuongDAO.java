package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Dữ liệu <b>mở rương</b> (tab gacha trong màn Sự kiện).
 *
 * <h2>Bảng</h2>
 * <ul>
 *   <li>{@code mo_ruong_loai} — mỗi loại rương một dòng: tên, mô tả, vật phẩm
 *       làm hình, giá x1 và x10 tính bằng điểm rương, thứ tự, bật/tắt.</li>
 *   <li>{@code mo_ruong_qua} — quà trong từng rương: vật phẩm, số lượng,
 *       <b>trọng số</b> (tỉ lệ thật = trọng số / tổng trọng số của rương) và
 *       độ hiếm (chỉ để tô màu: thường, hiếm, sử thi, huyền thoại).</li>
 *   <li>{@code mo_ruong_diem_loai} — điểm của từng người <b>theo từng loại
 *       rương</b>: mỗi rương một loại điểm riêng, không dùng chung. Cách kiếm
 *       điểm tính sau; hiện quản trị chỉnh trên panel.</li>
 *   <li>{@code mo_ruong_lich_su} — mỗi lượt mở một dòng, để tra khi có người
 *       kêu "mở mãi không ra".</li>
 * </ul>
 *
 * <p>Tự tạo bảng và gieo mặc định lúc dùng lần đầu: máy chủ thật chỉ
 * {@code git pull} rồi chạy, không ai chạy SQL tay.</p>
 */
public class MoRuongDAO {

    /** Tên các độ hiếm, đúng thứ tự con số lưu trong bảng. */
    public static final String[] TEN_HIEM = {"Thường", "Hiếm", "Sử thi", "Huyền thoại"};

    private static final long HAN_BO_NHO_MS = 30_000L;

    // =====================================================================
    //  Kiểu dữ liệu
    // =====================================================================
    public static final class Ruong {

        public int id;
        public String ten = "";
        public String moTa = "";
        /** Vật phẩm lấy icon làm hình rương (khi {@link #iconHinh} = 0). */
        public int itemHinh;
        /**
         * Icon id vẽ thẳng làm hình rương; 0 là lấy icon của {@link #itemHinh}.
         * Cho phép dùng hình không gắn với vật phẩm nào, vd trứng Mabư 27997.
         */
        public int iconHinh;
        /** Tên loại điểm của rương này; rỗng thì "Điểm " + tên rương. */
        public String tenDiem = "";

        public String tenDiemDeDoc() {
            return (tenDiem == null || tenDiem.trim().isEmpty()) ? ("Điểm " + ten) : tenDiem.trim();
        }
        public int giaX1;
        public int giaX10;
        public int thuTu;
        public boolean bat = true;
    }

    public static final class Qua {

        public int id;
        public int ruongId;
        public int itemId;
        public int soLuong = 1;
        /** Trọng số; tỉ lệ thật = trọng số / tổng trọng số của rương. */
        public int trongSo = 100;
        /** 0 thường · 1 hiếm · 2 sử thi · 3 huyền thoại. */
        public int hiem;
        public String chiSo = "";
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
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_loai ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " mo_ta VARCHAR(255) NOT NULL DEFAULT '',"
                    + " item_hinh INT(11) NOT NULL DEFAULT 0,"
                    + " gia_x1 INT(11) NOT NULL DEFAULT 10,"
                    + " gia_x10 INT(11) NOT NULL DEFAULT 90,"
                    + " thu_tu INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_qua ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " ruong_id INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL,"
                    + " so_luong INT(11) NOT NULL DEFAULT 1,"
                    + " trong_so INT(11) NOT NULL DEFAULT 100,"
                    + " hiem INT(11) NOT NULL DEFAULT 0,"
                    + " chi_so VARCHAR(255) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (id), KEY (ruong_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // Diem TACH THEO LOAI RUONG: moi ruong mot loai diem, khong chung.
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_diem_loai ("
                    + " player_id INT(11) NOT NULL,"
                    + " ruong_id INT(11) NOT NULL,"
                    + " diem BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id, ruong_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_lich_su ("
                    + " id BIGINT(20) NOT NULL AUTO_INCREMENT,"
                    + " player_id INT(11) NOT NULL,"
                    + " ruong_id INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL,"
                    + " so_luong INT(11) NOT NULL,"
                    + " hiem INT(11) NOT NULL DEFAULT 0,"
                    + " luc BIGINT(20) NOT NULL,"
                    + " PRIMARY KEY (id), KEY (player_id), KEY (luc)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // Them cot TRUOC khi gieo: gieo ghi vao hai cot moi.
            themCotMoi();
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_cau_hinh ("
                    + " khoa VARCHAR(40) NOT NULL,"
                    + " gia_tri VARCHAR(80) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (khoa)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            if (demDong("mo_ruong_loai") == 0) {
                gieoMacDinh();
            }
            suaQuaTheoIdThat();
        } catch (Exception ex) {
            daTaoBang = false;
            Logger.logException(MoRuongDAO.class, ex, "Không tạo được bảng mở rương");
        }
    }

    /**
     * Cột thêm sau: icon hình và tên điểm của từng rương.
     */
    private static void themCotMoi() {
        try {
            ConnectDB.executeUpdate("ALTER TABLE mo_ruong_loai ADD COLUMN icon_hinh INT(11) NOT NULL DEFAULT 0");
        } catch (Exception daCo) {
            // Cot da co: khong phai loi.
        }
        try {
            ConnectDB.executeUpdate("ALTER TABLE mo_ruong_loai ADD COLUMN ten_diem VARCHAR(60) NOT NULL DEFAULT ''");
        } catch (Exception daCo) {
            // Cot da co: khong phai loi.
        }
    }

    /** Cờ: đã sửa quà theo id vật phẩm thật chưa. */
    private static final String CO_SUA_QUA = "qua_id_that_v1";

    /** Tỉ lệ của Trứng Mabư và hai rương thú cưng trong rương, tính bằng phần trăm. */
    private static final double TI_LE_MON_VANG = 2.0;

    /**
     * Đặt Trứng Mabư và hai Rương Thú Cưng vào rương theo <b>id thật của máy
     * này</b> — đúng một lần.
     *
     * <p>Ba món này là vật phẩm tự tạo, máy chủ cấp id lúc dựng nên mỗi máy một
     * khác. Bản đầu viết cứng 2453/2454 theo một máy; trên máy khác hai con số
     * ấy rơi vào quả trứng đệ tử, nên Rương Thường ra "trứng Bill".</p>
     *
     * <p>Làm gì: bỏ mọi dòng quà trỏ 2453/2454 hay trỏ tới chính ba món ấy, rồi
     * thêm lại cho đúng — Rương Thường: Trứng Mabư + Rương Thú Cưng Thường,
     * Rương Sự Kiện: Rương Thú Cưng Cao Cấp; mỗi món 2% và nền vàng (Huyền
     * thoại). Trọng số tính theo tổng của các món còn lại nên những món quản
     * trị đã chỉnh không bị đụng.</p>
     *
     * <p>Chưa tra được id (máy chưa dựng xong trứng / rương) thì không ghi cờ,
     * lần khởi động sau làm lại.</p>
     */
    private static void suaQuaTheoIdThat() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = ?", CO_SUA_QUA);
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        int trung = TrungDeTuDAO.itemCuaLoai(nro.core.consts.ConstDetu.MABU);
        int rtThuong = ThuCungDAO.idRuongThuong();
        int rtCaoCap = ThuCungDAO.idRuongCaoCap();
        if (trung <= 0 || rtThuong <= 0 || rtCaoCap <= 0) {
            Logger.warning("Mở rương: chưa tra được id trứng/rương thú cưng, để lần khởi động sau\n");
            return;
        }
        int thuong = idTheoTenRuong("Rương Thường");
        int suKien = idTheoTenRuong("Rương Sự Kiện");
        if (thuong > 0) {
            thayMonVang(thuong, new int[]{trung, rtThuong}, 2453, 2454, trung, rtThuong, rtCaoCap);
        }
        if (suKien > 0) {
            thayMonVang(suKien, new int[]{rtCaoCap}, 2453, 2454, trung, rtThuong, rtCaoCap);
        }
        ConnectDB.executeUpdate("INSERT IGNORE INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES (?, '1')", CO_SUA_QUA);
        lucDoc = 0;
        Logger.success("Mở rương: đặt Trứng Mabư (" + trung + "), Rương Thú Cưng Thường (" + rtThuong
                + "), Rương Thú Cưng Cao Cấp (" + rtCaoCap + ") theo id thật\n");
    }

    /**
     * Bỏ các dòng trỏ {@code bo}, rồi thêm {@code them} — mỗi món đúng
     * {@link #TI_LE_MON_VANG}% của tổng mới, độ hiếm Huyền thoại.
     */
    private static void thayMonVang(int ruongId, int[] them, int... bo) throws Exception {
        for (int id : bo) {
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_qua WHERE ruong_id = ? AND item_id = ?", ruongId, id);
        }
        long conLai = 0;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT SUM(trong_so) AS t FROM mo_ruong_qua WHERE ruong_id = ?", ruongId);
            if (rs.next()) {
                conLai = rs.getLong("t");
            }
        } finally {
            dong(rs);
        }
        // Cho ra DUNG 2%, khong phai 1,99%: goi D = 100 / 2 = 50 phan. Nhan cac
        // mon con lai len (D - n) lan, moi mon vang lay dung tong cu S. Tong moi
        // = S*(D-n) + n*S = S*D, nen moi mon vang = S / (S*D) = 1/D = 2% tron.
        // Ti le giua cac mon con lai khong doi vi cung nhan mot he so.
        int d = (int) Math.round(100.0 / TI_LE_MON_VANG);
        int n = them.length;
        if (conLai <= 0) {
            conLai = 100;
        } else {
            ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET trong_so = trong_so * ? WHERE ruong_id = ?",
                    d - n, ruongId);
        }
        for (int itemId : them) {
            themQua(ruongId, itemId, 1, (int) Math.min(Integer.MAX_VALUE, conLai), 3, "");
        }
    }

    private static int idTheoTenRuong(String ten) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM mo_ruong_loai WHERE ten = ? ORDER BY id LIMIT 1", ten);
            return rs.next() ? rs.getInt("id") : -1;
        } finally {
            dong(rs);
        }
    }

    /**
     * Hai rương mặc định. Rương thường rẻ, quà đa phần là đồ dùng hằng ngày;
     * rương sự kiện đắt gấp ba nhưng toàn rương con và có cửa ra thú cưng cao
     * cấp. Tỉ lệ huyền thoại để thấp (1–2%) — thấp quá thì không ai tin là có.
     */
    private static void gieoMacDinh() throws Exception {
        int thuong = themRuong("Rương Thường", "Rương cơ bản — đồ dùng hằng ngày, có cửa ra thú cưng.",
                571, 10, 90, 1);
        themQua(thuong, 457, 1, 4000, 0, "");      // Thoi vang
        themQua(thuong, 595, 5, 3000, 0, "");      // Dau than cap 10
        themQua(thuong, 380, 1, 1500, 1, "");      // Vien capsule ki bi
        themQua(thuong, 987, 1, 1000, 1, "");      // Da bao ve
        themQua(thuong, 571, 1, 400, 2, "");       // Ruong bac
        // Trung Mabu va Ruong thu cung thuong: them trong suaQuaTheoIdThat (id
        // cua hai mon nay moi may mot khac).

        int suKien = themRuong("Rương Sự Kiện", "Rương cao cấp — toàn rương con, cửa ra thú cưng cao cấp.",
                1960, 30, 270, 2);
        themQua(suKien, 457, 3, 3500, 0, "");      // Thoi vang
        themQua(suKien, 1440, 1, 2500, 1, "");     // Ruong sao pha le
        themQua(suKien, 572, 1, 2000, 1, "");      // Ruong vang
        themQua(suKien, 1560, 1, 1200, 2, "");     // Ruong ngoc rong
        themQua(suKien, 1453, 1, 600, 2, "");      // Ruong sao pha le VIP
        // Ruong thu cung cao cap: them trong suaQuaTheoIdThat.
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET ten_diem = 'Điểm Rương Thường' WHERE id = ?", thuong);
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET ten_diem = 'Điểm Rương Sự Kiện' WHERE id = ?", suKien);
        Logger.success("Mở rương: gieo 2 rương mặc định\n");
    }

    // =====================================================================
    //  Rương
    // =====================================================================
    private static final List<Ruong> RUONG = new ArrayList<>();
    private static final List<Qua> QUA = new ArrayList<>();
    private static long lucDoc;

    private static synchronized void doc() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDoc < HAN_BO_NHO_MS && lucDoc != 0) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM mo_ruong_loai ORDER BY thu_tu, id");
            List<Ruong> r = new ArrayList<>();
            while (rs.next()) {
                Ruong x = new Ruong();
                x.id = rs.getInt("id");
                x.ten = khongNull(rs.getString("ten"));
                x.moTa = khongNull(rs.getString("mo_ta"));
                x.itemHinh = rs.getInt("item_hinh");
                try {
                    x.iconHinh = rs.getInt("icon_hinh");
                    x.tenDiem = khongNull(rs.getString("ten_diem"));
                } catch (Exception chuaCoCot) {
                    x.iconHinh = 0;
                    x.tenDiem = "";
                }
                x.giaX1 = rs.getInt("gia_x1");
                x.giaX10 = rs.getInt("gia_x10");
                x.thuTu = rs.getInt("thu_tu");
                x.bat = rs.getInt("bat") == 1;
                r.add(x);
            }
            dong(rs);
            rs = ConnectDB.executeQuery("SELECT * FROM mo_ruong_qua ORDER BY ruong_id, hiem DESC, trong_so, id");
            List<Qua> q = new ArrayList<>();
            while (rs.next()) {
                Qua x = new Qua();
                x.id = rs.getInt("id");
                x.ruongId = rs.getInt("ruong_id");
                x.itemId = rs.getInt("item_id");
                x.soLuong = rs.getInt("so_luong");
                x.trongSo = rs.getInt("trong_so");
                x.hiem = rs.getInt("hiem");
                x.chiSo = khongNull(rs.getString("chi_so"));
                q.add(x);
            }
            RUONG.clear();
            RUONG.addAll(r);
            QUA.clear();
            QUA.addAll(q);
            lucDoc = bayGio;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được bảng mở rương");
        } finally {
            dong(rs);
        }
    }

    public static synchronized List<Ruong> dsRuong(boolean chiBat) {
        doc();
        List<Ruong> ra = new ArrayList<>();
        for (Ruong r : RUONG) {
            if (!chiBat || r.bat) {
                ra.add(r);
            }
        }
        return ra;
    }

    public static synchronized Ruong ruong(int id) {
        doc();
        for (Ruong r : RUONG) {
            if (r.id == id) {
                return r;
            }
        }
        return null;
    }

    public static synchronized List<Qua> dsQua(int ruongId) {
        doc();
        List<Qua> ra = new ArrayList<>();
        for (Qua q : QUA) {
            if (q.ruongId == ruongId && q.trongSo > 0) {
                ra.add(q);
            }
        }
        return ra;
    }

    /** Mọi quà của rương, kể cả trọng số 0 (panel cần thấy). */
    public static synchronized List<Qua> dsQuaDayDu(int ruongId) {
        doc();
        List<Qua> ra = new ArrayList<>();
        for (Qua q : QUA) {
            if (q.ruongId == ruongId) {
                ra.add(q);
            }
        }
        return ra;
    }

    public static int themRuong(String ten, String moTa, int itemHinh, int giaX1, int giaX10, int thuTu)
            throws Exception {
        ConnectDB.executeUpdate("INSERT INTO mo_ruong_loai (ten, mo_ta, item_hinh, gia_x1, gia_x10, thu_tu)"
                + " VALUES (?, ?, ?, ?, ?, ?)", ten, moTa, itemHinh, giaX1, giaX10, thuTu);
        lucDoc = 0;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(id) AS m FROM mo_ruong_loai");
            return rs.next() ? rs.getInt("m") : 0;
        } finally {
            dong(rs);
        }
    }

    public static void luuRuong(Ruong r) {
        try {
            ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET ten = ?, mo_ta = ?, item_hinh = ?, icon_hinh = ?,"
                    + " ten_diem = ?, gia_x1 = ?, gia_x10 = ?, thu_tu = ?, bat = ? WHERE id = ?", r.ten, r.moTa,
                    r.itemHinh, r.iconHinh, r.tenDiem == null ? "" : r.tenDiem.trim(), r.giaX1, r.giaX10,
                    r.thuTu, r.bat ? 1 : 0, r.id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không lưu được rương");
        }
    }

    public static void xoaRuong(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_qua WHERE ruong_id = ?", id);
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_loai WHERE id = ?", id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không xoá được rương");
        }
    }

    public static void themQua(int ruongId, int itemId, int soLuong, int trongSo, int hiem, String chiSo)
            throws Exception {
        ConnectDB.executeUpdate("INSERT INTO mo_ruong_qua (ruong_id, item_id, so_luong, trong_so, hiem, chi_so)"
                + " VALUES (?, ?, ?, ?, ?, ?)", ruongId, itemId, Math.max(1, soLuong), Math.max(0, trongSo),
                Math.max(0, Math.min(3, hiem)), chiSo == null ? "" : chiSo.trim());
        lucDoc = 0;
    }

    public static void luuQua(Qua q) {
        try {
            ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET so_luong = ?, trong_so = ?, hiem = ?, chi_so = ?"
                    + " WHERE id = ?", Math.max(1, q.soLuong), Math.max(0, q.trongSo),
                    Math.max(0, Math.min(3, q.hiem)), q.chiSo == null ? "" : q.chiSo.trim(), q.id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không lưu được quà rương");
        }
    }

    public static void xoaQua(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_qua WHERE id = ?", id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không xoá được quà rương");
        }
    }

    // =====================================================================
    //  Điểm
    // =====================================================================
    /** Điểm của một người ở một loại rương. */
    public static long diem(long playerId, int ruongId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT diem FROM mo_ruong_diem_loai WHERE player_id = ? AND ruong_id = ?",
                    playerId, ruongId);
            return rs.next() ? rs.getLong("diem") : 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được điểm rương");
            return 0;
        } finally {
            dong(rs);
        }
    }

    /** Điểm của một người ở mọi loại rương: id rương → điểm (loại chưa có dòng thì không có khoá). */
    public static java.util.Map<Integer, Long> diemCuaNguoi(long playerId) {
        damBaoBang();
        java.util.Map<Integer, Long> ra = new java.util.HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT ruong_id, diem FROM mo_ruong_diem_loai WHERE player_id = ?", playerId);
            while (rs.next()) {
                ra.put(rs.getInt("ruong_id"), rs.getLong("diem"));
            }
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được điểm rương");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Cộng (hoặc trừ, nếu âm) điểm một loại rương; không để xuống dưới 0. */
    public static void congDiem(long playerId, int ruongId, long them) {
        try {
            damBaoBang();
            if (them >= 0) {
                ConnectDB.executeUpdate("INSERT INTO mo_ruong_diem_loai (player_id, ruong_id, diem) VALUES (?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE diem = diem + VALUES(diem)", playerId, ruongId, them);
            } else {
                ConnectDB.executeUpdate("UPDATE mo_ruong_diem_loai SET diem = GREATEST(0, diem + ?)"
                        + " WHERE player_id = ? AND ruong_id = ?", them, playerId, ruongId);
            }
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không cộng được điểm rương");
        }
    }

    /** Đặt thẳng điểm một loại rương (panel chỉnh tay). */
    public static void datDiem(long playerId, int ruongId, long diem) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO mo_ruong_diem_loai (player_id, ruong_id, diem) VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE diem = VALUES(diem)", playerId, ruongId, Math.max(0, diem));
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đặt được điểm rương");
        }
    }

    /**
     * Trừ điểm một loại rương nếu đủ, trong MỘT câu lệnh.
     *
     * <p>Điều kiện {@code diem >= ?} nằm ngay trong câu UPDATE: hai lượt mở đến
     * cùng lúc thì chỉ lượt nào còn đủ điểm mới trừ được — không có khoảng hở
     * giữa lúc đọc và lúc ghi để trừ âm.</p>
     *
     * @return {@code true} nếu đã trừ
     */
    public static boolean truDiem(long playerId, int ruongId, long gia) {
        try {
            damBaoBang();
            return ConnectDB.executeUpdate("UPDATE mo_ruong_diem_loai SET diem = diem - ?"
                    + " WHERE player_id = ? AND ruong_id = ? AND diem >= ?", gia, playerId, ruongId, gia) > 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không trừ được điểm rương");
            return false;
        }
    }

    public static void ghiLichSu(long playerId, int ruongId, int itemId, int soLuong, int hiem) {
        try {
            ConnectDB.executeUpdate("INSERT INTO mo_ruong_lich_su (player_id, ruong_id, item_id, so_luong, hiem, luc)"
                    + " VALUES (?, ?, ?, ?, ?, ?)", playerId, ruongId, itemId, soLuong, hiem,
                    System.currentTimeMillis());
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không ghi được lịch sử mở rương");
        }
    }

    /** Một dòng lịch sử cho panel. */
    public static final class DongLichSu {

        public String nguoi = "";
        public int ruongId;
        public int itemId;
        public int soLuong;
        public int hiem;
        public long luc;
    }

    public static List<DongLichSu> lichSu(int toiDa) {
        damBaoBang();
        List<DongLichSu> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT l.ruong_id, l.item_id, l.so_luong, l.hiem, l.luc, p.name"
                    + " FROM mo_ruong_lich_su l LEFT JOIN player p ON p.id = l.player_id"
                    + " ORDER BY l.id DESC LIMIT " + Math.max(1, toiDa));
            while (rs.next()) {
                DongLichSu d = new DongLichSu();
                d.ruongId = rs.getInt("ruong_id");
                d.itemId = rs.getInt("item_id");
                d.soLuong = rs.getInt("so_luong");
                d.hiem = rs.getInt("hiem");
                d.luc = rs.getLong("luc");
                d.nguoi = khongNull(rs.getString("name"));
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được lịch sử mở rương");
        } finally {
            dong(rs);
        }
        return ra;
    }

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
    private static String khongNull(String s) {
        return s == null ? "" : s;
    }

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
