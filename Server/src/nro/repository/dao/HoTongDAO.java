package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Cấu hình và phần thưởng của <b>nhiệm vụ hộ tống Đường Tăng</b>.
 *
 * <h2>Ba bảng</h2>
 *
 * <ul>
 *   <li>{@code ho_tong_cau_hinh} — một dòng: thời gian hồi, khoảng cách tối đa,
 *       tốc độ đi, có bật cờ hay không</li>
 *   <li>{@code ho_tong_qua} — mỗi dòng một món: số lượng bốc trong khoảng, và
 *       tỉ lệ rơi</li>
 *   <li>{@code ho_tong_nguoi_choi} — lần hộ tống gần nhất của từng người</li>
 * </ul>
 *
 * <h2>Vì sao lần hộ tống gần nhất nằm trong CSDL</h2>
 *
 * <p>Giữ trong bộ nhớ nhân vật thì thoát ra vào lại là mất, và thời gian hồi
 * thành vô nghĩa — ai cũng chỉ việc đăng xuất rồi làm tiếp. Một dòng trong bảng
 * thì sống qua cả lần khởi động lại máy chủ.</p>
 */
public final class HoTongDAO {

    private HoTongDAO() {
    }

    /** Cấu hình chung của nhiệm vụ. */
    public static final class CauHinh {

        /** Bật nhiệm vụ hay không. */
        public boolean bat = true;

        /** Thời gian hồi giữa hai lần hộ tống, tính bằng phút. */
        public int phutHoi = 60;

        /**
         * Người chơi được cách Đường Tăng xa nhất mấy bản đồ.
         *
         * <p>Đếm theo <b>tuyến đi</b>, không phải theo đường chim bay: đứng ở
         * Rừng nấm trong khi Đường Tăng ở Làng Aru là cách ba bản đồ.</p>
         */
        public int khoangCachMap = 3;

        /** Mỗi bước đi cách nhau bấy nhiêu mili giây. */
        public int msMoiBuoc = 700;

        /** Mỗi bước đi được bao nhiêu điểm ảnh. */
        public int buocDiemAnh = 40;

        /**
         * Bật cờ cho người hộ tống hay không.
         *
         * <p>Bật thì lúc nhận nhiệm vụ người chơi tự bật cờ đen — ai cũng đánh
         * được họ, nên hộ tống thành chuyện phải giành nhau. Tắt thì đi yên ả.
         * Cờ tự tắt khi nhiệm vụ kết thúc, dù thành hay bại.</p>
         */
        public boolean batCo = true;
    }

    /** Một dòng phần thưởng. */
    public static final class Qua {

        public int id;
        public int itemId;
        public int slMin = 1;
        public int slMax = 1;
        /** Tỉ lệ rơi, tính bằng <b>phần nghìn</b>: 1000 là chắc chắn. */
        public int tiLe = 1000;
        public boolean bat = true;
        public String ghiChu;
    }

    private static volatile boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS ho_tong_cau_hinh ("
                    + " id INT(11) NOT NULL DEFAULT 1,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " phut_hoi INT(11) NOT NULL DEFAULT 60,"
                    + " khoang_cach_map INT(11) NOT NULL DEFAULT 3,"
                    + " ms_moi_buoc INT(11) NOT NULL DEFAULT 700,"
                    + " buoc_diem_anh INT(11) NOT NULL DEFAULT 40,"
                    + " bat_co TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS ho_tong_qua ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " item_id INT(11) NOT NULL,"
                    + " sl_min INT(11) NOT NULL DEFAULT 1,"
                    + " sl_max INT(11) NOT NULL DEFAULT 1,"
                    + " ti_le INT(11) NOT NULL DEFAULT 1000,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " ghi_chu VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS ho_tong_nguoi_choi ("
                    + " player_id INT(11) NOT NULL,"
                    + " lan_cuoi BIGINT(20) NOT NULL DEFAULT 0,"
                    + " so_lan INT(11) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            if (demDong("ho_tong_cau_hinh") == 0) {
                luuCauHinh(new CauHinh());
            }
            batCoMotLan();
        } catch (Exception ex) {
            daTaoBang = false;
            Logger.logException(HoTongDAO.class, ex,
                    "Không dựng được bảng hộ tống");
        }
    }

    /**
     * Bật cờ đen cho bảng cấu hình đã tạo từ bản trước — <b>đúng một lần</b>.
     *
     * <p>Bản đầu tạo bảng với ô bật cờ để <b>tắt</b>, nên hộ tống chạy mà
     * không ai cầm cờ, trong khi cả nhiệm vụ được thiết kế quanh chuyện bị
     * đánh cướp. Thêm một cột đánh dấu để lượt sửa này chỉ chạy một lần: sau
     * đó quản trị tắt đi trên panel thì nó giữ nguyên là tắt.</p>
     */
    private static void batCoMotLan() {
        try {
            ConnectDB.executeUpdate("ALTER TABLE ho_tong_cau_hinh"
                    + " ADD COLUMN da_bat_co_mac_dinh TINYINT(1) NOT NULL DEFAULT 0");
        } catch (Exception daCo) {
            // Cot da co san — lan nay khong phai lan dau.
        }
        try {
            ConnectDB.executeUpdate("UPDATE ho_tong_cau_hinh SET bat_co = 1,"
                    + " da_bat_co_mac_dinh = 1 WHERE id = 1"
                    + " AND da_bat_co_mac_dinh = 0");
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex,
                    "Không bật được cờ mặc định cho hộ tống");
        }
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

    // =====================================================================
    //  Cấu hình
    // =====================================================================
    private static CauHinh dem;
    private static long lucDocDem;

    /** Bao lâu thì đọc lại bảng, tính bằng mili giây. */
    private static final long HAN_DEM = 15_000L;

    /** Cấu hình hiện tại. <b>Không bao giờ trả {@code null}.</b> */
    public static synchronized CauHinh cauHinh() {
        long gio = System.currentTimeMillis();
        if (dem != null && gio - lucDocDem < HAN_DEM) {
            return dem;
        }
        damBaoBang();
        CauHinh c = new CauHinh();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT * FROM ho_tong_cau_hinh WHERE id = 1");
            if (rs.next()) {
                c.bat = rs.getInt("bat") == 1;
                c.phutHoi = rs.getInt("phut_hoi");
                c.khoangCachMap = rs.getInt("khoang_cach_map");
                c.msMoiBuoc = rs.getInt("ms_moi_buoc");
                c.buocDiemAnh = rs.getInt("buoc_diem_anh");
                c.batCo = rs.getInt("bat_co") == 1;
            }
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex,
                    "Lỗi đọc ho_tong_cau_hinh");
        } finally {
            dong(rs);
        }
        dem = c;
        lucDocDem = gio;
        return c;
    }

    /** Lưu cấu hình. Trả câu lỗi, hoặc {@code null} nếu xong. */
    public static String luuCauHinh(CauHinh c) {
        if (c == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (c.phutHoi < 0) {
            return "Thời gian hồi không được âm.";
        }
        if (c.khoangCachMap < 1) {
            return "Khoảng cách tối đa phải từ 1 bản đồ trở lên.";
        }
        if (c.msMoiBuoc < 100) {
            return "Mỗi bước phải cách nhau ít nhất 100 mili giây.";
        }
        if (c.buocDiemAnh < 4) {
            return "Mỗi bước phải đi ít nhất 4 điểm ảnh.";
        }
        try {
            ConnectDB.executeUpdate("INSERT INTO ho_tong_cau_hinh"
                    + " (id, bat, phut_hoi, khoang_cach_map, ms_moi_buoc,"
                    + " buoc_diem_anh, bat_co) VALUES (1, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE bat = VALUES(bat),"
                    + " phut_hoi = VALUES(phut_hoi),"
                    + " khoang_cach_map = VALUES(khoang_cach_map),"
                    + " ms_moi_buoc = VALUES(ms_moi_buoc),"
                    + " buoc_diem_anh = VALUES(buoc_diem_anh),"
                    + " bat_co = VALUES(bat_co)",
                    c.bat ? 1 : 0, c.phutHoi, c.khoangCachMap, c.msMoiBuoc,
                    c.buocDiemAnh, c.batCo ? 1 : 0);
            xoaDem();
            return null;
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex, "Lỗi lưu ho_tong_cau_hinh");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static synchronized void xoaDem() {
        dem = null;
        demQua = null;
    }

    // =====================================================================
    //  Phần thưởng
    // =====================================================================
    private static List<Qua> demQua;
    private static long lucDocQua;

    /** Danh sách quà. <b>Không bao giờ trả {@code null}.</b> */
    public static synchronized List<Qua> dsQua() {
        long gio = System.currentTimeMillis();
        if (demQua != null && gio - lucDocQua < HAN_DEM) {
            return demQua;
        }
        damBaoBang();
        List<Qua> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM ho_tong_qua ORDER BY id");
            while (rs.next()) {
                Qua q = new Qua();
                q.id = rs.getInt("id");
                q.itemId = rs.getInt("item_id");
                q.slMin = rs.getInt("sl_min");
                q.slMax = rs.getInt("sl_max");
                q.tiLe = rs.getInt("ti_le");
                q.bat = rs.getInt("bat") == 1;
                q.ghiChu = rs.getStringOrNull("ghi_chu");
                ds.add(q);
            }
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex, "Lỗi đọc ho_tong_qua");
        } finally {
            dong(rs);
        }
        demQua = ds;
        lucDocQua = gio;
        return ds;
    }

    /** Thêm hoặc sửa một dòng quà. Trả câu lỗi, hoặc {@code null} nếu xong. */
    public static String luuQua(Qua q) {
        if (q == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (q.itemId < 0) {
            return "Mã vật phẩm không được âm.";
        }
        if (q.slMin < 1 || q.slMax < q.slMin) {
            return "Số lượng phải từ 1, và số tối đa không nhỏ hơn tối thiểu.";
        }
        if (q.tiLe < 1 || q.tiLe > 1000) {
            return "Tỉ lệ phải từ 1 đến 1000 phần nghìn.";
        }
        try {
            if (q.id > 0) {
                ConnectDB.executeUpdate("UPDATE ho_tong_qua SET item_id = ?,"
                        + " sl_min = ?, sl_max = ?, ti_le = ?, bat = ?,"
                        + " ghi_chu = ? WHERE id = ?",
                        q.itemId, q.slMin, q.slMax, q.tiLe, q.bat ? 1 : 0,
                        q.ghiChu, q.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO ho_tong_qua"
                        + " (item_id, sl_min, sl_max, ti_le, bat, ghi_chu)"
                        + " VALUES (?, ?, ?, ?, ?, ?)",
                        q.itemId, q.slMin, q.slMax, q.tiLe, q.bat ? 1 : 0,
                        q.ghiChu);
            }
            xoaDem();
            return null;
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex, "Lỗi lưu ho_tong_qua");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaQua(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM ho_tong_qua WHERE id = ?", id);
            xoaDem();
            return null;
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex, "Lỗi xoá ho_tong_qua");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    // =====================================================================
    //  Lần hộ tống gần nhất
    // =====================================================================
    /** Mốc thời gian lần hộ tống gần nhất, hoặc 0 nếu chưa lần nào. */
    public static long lanCuoi(int playerId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT lan_cuoi FROM ho_tong_nguoi_choi"
                    + " WHERE player_id = " + playerId);
            return rs.next() ? rs.getLong("lan_cuoi") : 0L;
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex,
                    "Lỗi đọc lần hộ tống của " + playerId);
            return 0L;
        } finally {
            dong(rs);
        }
    }

    /** Ghi lại rằng người này vừa hộ tống xong. */
    public static void ghiLanCuoi(int playerId, long moc) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO ho_tong_nguoi_choi"
                    + " (player_id, lan_cuoi, so_lan) VALUES (?, ?, 1)"
                    + " ON DUPLICATE KEY UPDATE lan_cuoi = VALUES(lan_cuoi),"
                    + " so_lan = so_lan + 1", playerId, moc);
        } catch (Exception ex) {
            Logger.logException(HoTongDAO.class, ex,
                    "Lỗi ghi lần hộ tống của " + playerId);
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
