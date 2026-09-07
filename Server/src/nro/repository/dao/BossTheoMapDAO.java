package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Boss gắn với một bản đồ cụ thể, lưu ở bảng {@code boss_theo_map}.
 *
 * <h2>Khác gì danh sách boss thường</h2>
 *
 * <p>Danh sách thường ({@code boss_spawn}) dựng sẵn N con lúc máy chủ khởi
 * động, rồi chúng tự tìm bản đồ theo {@code mapJoin} của mẫu. Boss ở đây thì
 * khác: nó mọc <b>trong từng khu của đúng một bản đồ</b> — kiểu boss hang, boss
 * phó bản. Trước đây phần này là một câu {@code switch} gõ cứng mười hai bản đồ
 * trong {@code Map.initBoss()}.</p>
 *
 * <p>Cột {@code moi_khu} giữ đúng hành vi cũ: mỗi khu của bản đồ một con. Bản
 * đồ có hai chục khu thì ra hai chục con — đó là lý do một mình Tàu Pảy Pảy
 * chiếm hơn năm chục con trong máy chủ.</p>
 */
public final class BossTheoMapDAO {

    private BossTheoMapDAO() {
    }

    /** Một dòng: bản đồ nào mọc boss nào. */
    public static final class Dong {

        public int id;
        public int mapId;
        public int bossId;
        /** Mỗi khu một con. Tắt thì cả bản đồ chỉ một con ở khu đầu. */
        public boolean moiKhu = true;
        public boolean bat = true;
        public String ghiChu = "";
    }

    public static void damBaoBang() throws Exception {
        ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS boss_theo_map ("
                + " id INT(11) NOT NULL AUTO_INCREMENT,"
                + " map_id INT(11) NOT NULL,"
                + " boss_id INT(11) NOT NULL,"
                + " moi_khu TINYINT(1) NOT NULL DEFAULT 1,"
                + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                + " ghi_chu VARCHAR(255) NOT NULL DEFAULT '',"
                + " PRIMARY KEY (id), KEY idx_map (map_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    /** Toàn bộ, sắp theo bản đồ. */
    public static List<Dong> tatCa() {
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            damBaoBang();
            rs = ConnectDB.executeQuery("SELECT id, map_id, boss_id, moi_khu,"
                    + " bat, ghi_chu FROM boss_theo_map ORDER BY map_id, id");
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getInt("id");
                d.mapId = rs.getInt("map_id");
                d.bossId = rs.getInt("boss_id");
                d.moiKhu = rs.getBoolean("moi_khu");
                d.bat = rs.getBoolean("bat");
                d.ghiChu = rs.getString("ghi_chu");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(BossTheoMapDAO.class, ex,
                    "Không đọc được bảng boss_theo_map");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                    // Dong khong duoc thi thoi.
                }
            }
        }
        return ds;
    }

    /**
     * Bộ nhớ đệm theo bản đồ.
     *
     * <p>{@code Map.initBoss()} chạy cho <b>từng bản đồ một</b> lúc khởi động —
     * hơn hai trăm lượt. Hỏi CSDL mỗi lượt là hai trăm truy vấn chỉ để dựng vài
     * chục con boss.</p>
     */
    private static volatile List<Dong> cache;

    public static void napLai() {
        cache = null;
    }

    public static List<Dong> cuaMap(int mapId) {
        List<Dong> c = cache;
        if (c == null) {
            c = tatCa();
            cache = c;
        }
        List<Dong> ra = new ArrayList<>();
        for (Dong d : c) {
            if (d.mapId == mapId && d.bat) {
                ra.add(d);
            }
        }
        return ra;
    }

    public static String luu(Dong d) {
        if (d == null) {
            return "Thiếu dữ liệu.";
        }
        try {
            damBaoBang();
            if (d.id > 0) {
                ConnectDB.executeUpdate("UPDATE boss_theo_map SET map_id = ?,"
                        + " boss_id = ?, moi_khu = ?, bat = ?, ghi_chu = ?"
                        + " WHERE id = ?",
                        d.mapId, d.bossId, d.moiKhu ? 1 : 0, d.bat ? 1 : 0,
                        d.ghiChu, d.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO boss_theo_map (map_id,"
                        + " boss_id, moi_khu, bat, ghi_chu) VALUES (?, ?, ?, ?, ?)",
                        d.mapId, d.bossId, d.moiKhu ? 1 : 0, d.bat ? 1 : 0,
                        d.ghiChu);
            }
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(BossTheoMapDAO.class, ex, "Lỗi lưu boss_theo_map");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM boss_theo_map WHERE id = ?", id);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(BossTheoMapDAO.class, ex, "Lỗi xoá boss_theo_map");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Khoá đánh dấu đã gieo, để bảng trống vẫn là trống thật. */
    private static final String KHOA_DA_GIEO = "boss_theo_map_da_gieo";

    private static boolean daGieo() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT v FROM panel_config WHERE k = ?", KHOA_DA_GIEO);
            return rs.next();
        } catch (Exception ex) {
            Logger.logException(BossTheoMapDAO.class, ex,
                    "Không đọc được dấu đã gieo boss_theo_map");
            return true;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                    // Nhu tren.
                }
            }
        }
    }

    /**
     * Gieo mười hai dòng gõ cứng cũ, chỉ chạy lần đầu.
     *
     * <p>Giống {@code boss_spawn}: dùng dấu riêng chứ không dựa vào "bảng
     * trống", để admin xoá sạch rồi tự khai lại mà khởi động lại không bị mọc
     * lên nguyên như cũ.</p>
     */
    public static void gieoLanDau(int[][] mapVaBoss) {
        if (daGieo()) {
            return;
        }
        try {
            damBaoBang();
            for (int[] mb : mapVaBoss) {
                Dong d = new Dong();
                d.mapId = mb[0];
                d.bossId = mb[1];
                luu(d);
            }
            ConnectDB.executeUpdate("INSERT INTO panel_config (k, v) VALUES (?, '1')"
                    + " ON DUPLICATE KEY UPDATE v = '1'", KHOA_DA_GIEO);
            Logger.success("Đã gieo " + mapVaBoss.length
                    + " dòng boss theo bản đồ xuống bảng boss_theo_map\n");
        } catch (Exception ex) {
            Logger.logException(BossTheoMapDAO.class, ex,
                    "Không gieo được boss_theo_map");
        }
    }
}
