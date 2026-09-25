package nro.repository.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * <b>Trang phục kỹ năng</b> — đổi hình hiệu ứng của một chiêu (mới có Quả cầu
 * kênh khi). Chỉ đổi hình, không đổi sức mạnh.
 *
 * <h2>Hai bảng</h2>
 * <ul>
 * <li>{@code trang_phuc_mau}: mỗi dòng một trang phục — chiêu nào, tên, icon
 * hiện trong menu, và hai dãy khung hình (icon id):
 * <ul>
 * <li>{@code khung_nap}: chạy một lượt lúc tụ chiêu (đá tụ lại, bùng lên…);</li>
 * <li>{@code khung_bay}: lặp mãi sau đó và lúc quả cầu bay đi.</li>
 * </ul></li>
 * <li>{@code trang_phuc_nguoi}: người chơi nào đang bật trang phục nào cho
 * chiêu nào. Không có dòng = dùng hình gốc của game.</li>
 * </ul>
 */
public final class TrangPhucDAO {

    private TrangPhucDAO() {
    }

    public static final class Mau {
        public int id;
        public int skillTpl;
        public String ten = "";
        public String moTa = "";
        public int icon;
        public short[] khungNap = new short[0];
        public short[] khungBay = new short[0];
        public boolean bat = true;
    }

    private static volatile boolean daTao;
    private static volatile List<Mau> cache;
    private static long lucDoc;
    private static final long HAN_BO_NHO_MS = 60_000L;

    /** playerId → (skillTpl → mauId). Nạp lười lần đầu cần. */
    private static final Map<Long, Map<Integer, Integer>> CHON = new ConcurrentHashMap<>();

    private static synchronized void damBaoBang() {
        if (daTao) {
            return;
        }
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS trang_phuc_mau ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " skill_tpl INT(11) NOT NULL,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " mo_ta VARCHAR(255) NOT NULL DEFAULT '',"
                    + " icon INT(11) NOT NULL DEFAULT -1,"
                    + " khung_nap VARCHAR(255) NOT NULL DEFAULT '',"
                    + " khung_bay VARCHAR(255) NOT NULL DEFAULT '',"
                    + " thu_tu INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS trang_phuc_nguoi ("
                    + " player_id BIGINT(20) NOT NULL,"
                    + " skill_tpl INT(11) NOT NULL,"
                    + " mau_id INT(11) NOT NULL,"
                    + " PRIMARY KEY (player_id, skill_tpl)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            daTao = true;
            gieoNeuTrong();
            // Bo khung moi (10 khung, nguoi dung gui lai): doi dong Dia Boc Thien Tinh
            // dang tro bo cu. Chi sua khi con dung bo cu, chay lai bao lan cung vay.
            ConnectDB.executeUpdate("UPDATE trang_phuc_mau SET icon = ?, khung_nap = ?, khung_bay = ?"
                    + " WHERE skill_tpl = 10 AND khung_nap = '25259,25260,25261,25262,25263,25264'",
                    ICON_DBTT, KHUNG_NAP_DBTT, KHUNG_BAY_DBTT);
        } catch (Exception ex) {
            Logger.logException(TrangPhucDAO.class, ex, "Không tạo được bảng trang phục");
        }
    }

    /**
     * Địa Bộc Thiên Tinh (icon 25267–25276): 8 khung tụ cầu, rồi khung ném và
     * khung chạm địch. Client hiểu {@code khung_bay} là [khung bay, khung chạm].
     */
    private static final int ICON_DBTT = 25274;
    private static final String KHUNG_NAP_DBTT = "25267,25268,25269,25270,25271,25272,25273,25274";
    private static final String KHUNG_BAY_DBTT = "25275,25276";

    /** Trang phục đầu tiên: Địa Bộc Thiên Tinh cho Quả cầu kênh khi. */
    private static void gieoNeuTrong() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS c FROM trang_phuc_mau");
            if (rs.next() && rs.getInt("c") > 0) {
                return;
            }
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        ConnectDB.executeUpdate("INSERT INTO trang_phuc_mau (skill_tpl, ten, mo_ta, icon, khung_nap, khung_bay, thu_tu)"
                + " VALUES (?,?,?,?,?,?,1)", 10, "Địa Bộc Thiên Tinh",
                "Hút đá khắp nơi nén thành một khối cầu rực lửa rồi giáng xuống kẻ địch.",
                ICON_DBTT, KHUNG_NAP_DBTT, KHUNG_BAY_DBTT);
        Logger.success("Trang phục: gieo Địa Bộc Thiên Tinh cho Quả cầu kênh khi\n");
    }

    private static short[] tachKhung(String s) {
        if (s == null || s.trim().isEmpty()) {
            return new short[0];
        }
        List<Short> ds = new ArrayList<>();
        for (String p : s.split(",")) {
            try {
                ds.add(Short.parseShort(p.trim()));
            } catch (NumberFormatException boQua) {
                // Dong hong -> bo rieng khung do.
            }
        }
        short[] ra = new short[ds.size()];
        for (int i = 0; i < ra.length; i++) {
            ra[i] = ds.get(i);
        }
        return ra;
    }

    /** Mọi trang phục (cả dòng tắt), đọc lại sau mỗi phút. */
    public static synchronized List<Mau> tatCa() {
        damBaoBang();
        long bayGio = System.currentTimeMillis();
        if (cache != null && bayGio - lucDoc < HAN_BO_NHO_MS) {
            return cache;
        }
        List<Mau> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM trang_phuc_mau ORDER BY skill_tpl, thu_tu, id");
            while (rs.next()) {
                Mau m = new Mau();
                m.id = rs.getInt("id");
                m.skillTpl = rs.getInt("skill_tpl");
                m.ten = rs.getString("ten");
                m.moTa = rs.getString("mo_ta");
                m.icon = rs.getInt("icon");
                m.khungNap = tachKhung(rs.getString("khung_nap"));
                m.khungBay = tachKhung(rs.getString("khung_bay"));
                m.bat = rs.getBoolean("bat");
                ra.add(m);
            }
        } catch (Exception ex) {
            Logger.logException(TrangPhucDAO.class, ex, "Không đọc được trang phục");
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        cache = ra;
        lucDoc = bayGio;
        return ra;
    }

    public static Mau mau(int id) {
        for (Mau m : tatCa()) {
            if (m.id == id) {
                return m;
            }
        }
        return null;
    }

    private static Map<Integer, Integer> chonCua(long playerId) {
        Map<Integer, Integer> c = CHON.get(playerId);
        if (c != null) {
            return c;
        }
        damBaoBang();
        c = new HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT skill_tpl, mau_id FROM trang_phuc_nguoi WHERE player_id = ?", playerId);
            while (rs.next()) {
                c.put(rs.getInt("skill_tpl"), rs.getInt("mau_id"));
            }
        } catch (Exception ex) {
            Logger.logException(TrangPhucDAO.class, ex, "Không đọc được trang phục người chơi");
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        CHON.put(playerId, c);
        return c;
    }

    /** Trang phục đang bật cho chiêu này (đang bật trên bảng mẫu), hoặc null = hình gốc. */
    public static Mau dangDung(long playerId, int skillTpl) {
        Integer id = chonCua(playerId).get(skillTpl);
        if (id == null) {
            return null;
        }
        Mau m = mau(id);
        return m != null && m.bat && m.skillTpl == skillTpl ? m : null;
    }

    /** Bật trang phục {@code mauId} cho chiêu, hoặc {@code mauId <= 0} để về hình gốc. */
    public static void chon(long playerId, int skillTpl, int mauId) {
        damBaoBang();
        Map<Integer, Integer> c = chonCua(playerId);
        try {
            if (mauId <= 0) {
                ConnectDB.executeUpdate("DELETE FROM trang_phuc_nguoi WHERE player_id = ? AND skill_tpl = ?",
                        playerId, skillTpl);
                c.remove(skillTpl);
            } else {
                ConnectDB.executeUpdate("INSERT INTO trang_phuc_nguoi (player_id, skill_tpl, mau_id) VALUES (?,?,?)"
                        + " ON DUPLICATE KEY UPDATE mau_id = VALUES(mau_id)", playerId, skillTpl, mauId);
                c.put(skillTpl, mauId);
            }
        } catch (Exception ex) {
            Logger.logException(TrangPhucDAO.class, ex, "Không lưu được trang phục người chơi");
        }
    }

    /** Bỏ bộ nhớ lựa chọn khi người chơi thoát (lần sau đọc lại từ CSDL). */
    public static void quen(long playerId) {
        CHON.remove(playerId);
    }
}
