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
        /** Vật phẩm dùng để mở khoá trang phục này (id mỗi máy một khác), hoặc -1. */
        public int itemId = -1;
        /** Icon cỡ vật phẩm cho vật phẩm mở khoá; -1 thì lấy {@link #icon}. */
        public int iconVp = -1;
    }

    /** playerId → các trang phục đã sở hữu. Nạp lười lần đầu cần. */
    private static final Map<Long, java.util.Set<Integer>> SO_HUU = new ConcurrentHashMap<>();

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
            // So huu: trang phuc phai co moi bat duoc (mo khoa bang vat pham).
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS trang_phuc_so_huu ("
                    + " player_id BIGINT(20) NOT NULL,"
                    + " mau_id INT(11) NOT NULL,"
                    + " luc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id, mau_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("ALTER TABLE trang_phuc_mau ADD COLUMN IF NOT EXISTS item_id INT(11) NOT NULL DEFAULT -1");
            ConnectDB.executeUpdate("ALTER TABLE trang_phuc_mau ADD COLUMN IF NOT EXISTS icon_vp INT(11) NOT NULL DEFAULT -1");
            daTao = true;
            gieoNeuTrong();
            // Them du am vu no (khung thu ba cua day bay).
            ConnectDB.executeUpdate("UPDATE trang_phuc_mau SET khung_bay = ? WHERE skill_tpl = 10"
                    + " AND khung_bay = '25285,25286'", KHUNG_BAY_DBTT);
            ConnectDB.executeUpdate("UPDATE trang_phuc_mau SET icon_vp = ? WHERE skill_tpl = 10 AND icon_vp = -1"
                    + " AND ten = 'Địa Bộc Thiên Tinh'", ICON_VP_DBTT);
            // Bo khung moi (10 khung, nguoi dung gui lai): doi dong Dia Boc Thien Tinh
            // dang tro bo cu. Chi sua khi con dung bo cu, chay lai bao lan cung vay.
            ConnectDB.executeUpdate("UPDATE trang_phuc_mau SET icon = ?, khung_nap = ?, khung_bay = ?"
                    + " WHERE skill_tpl = 10 AND khung_nap IN ('25259,25260,25261,25262,25263,25264',"
                    + " '25267,25268,25269,25270,25271,25272,25273,25274')",
                    ICON_DBTT, KHUNG_NAP_DBTT, KHUNG_BAY_DBTT);
        } catch (Exception ex) {
            Logger.logException(TrangPhucDAO.class, ex, "Không tạo được bảng trang phục");
        }
    }

    /**
     * Địa Bộc Thiên Tinh (icon 25277–25286, căn theo tâm quả cầu): 8 khung tụ cầu, rồi khung ném và
     * khung chạm địch. Client hiểu {@code khung_bay} là [khung bay, khung chạm].
     */
    private static final int ICON_DBTT = 25284;
    private static final String KHUNG_NAP_DBTT = "25277,25278,25279,25280,25281,25282,25283,25284";
    /** Khung bay: [ném, chạm địch, dư âm vụ nổ]. */
    private static final String KHUNG_BAY_DBTT = "25285,25286,25313";
    /** Icon cỡ vật phẩm (khung 8 thu nhỏ) cho "Trang phục: Địa Bộc Thiên Tinh". */
    private static final int ICON_VP_DBTT = 25288;

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
                try {
                    m.itemId = rs.getInt("item_id");
                    m.iconVp = rs.getInt("icon_vp");
                } catch (Exception cotChuaCo) {
                    // Bang cu chua co hai cot nay: giu -1.
                }
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
        return m != null && m.bat && m.skillTpl == skillTpl && coSoHuu(playerId, m.id) ? m : null;
    }

    private static java.util.Set<Integer> soHuuCua(long playerId) {
        java.util.Set<Integer> s = SO_HUU.get(playerId);
        if (s != null) {
            return s;
        }
        damBaoBang();
        s = ConcurrentHashMap.newKeySet();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT mau_id FROM trang_phuc_so_huu WHERE player_id = ?", playerId);
            while (rs.next()) {
                s.add(rs.getInt("mau_id"));
            }
        } catch (Exception ex) {
            Logger.logException(TrangPhucDAO.class, ex, "Không đọc được trang phục đã sở hữu");
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        SO_HUU.put(playerId, s);
        return s;
    }

    public static boolean coSoHuu(long playerId, int mauId) {
        return soHuuCua(playerId).contains(mauId);
    }

    /** Ghi sở hữu; trả false nếu đã có từ trước. */
    public static boolean themSoHuu(long playerId, int mauId) {
        java.util.Set<Integer> s = soHuuCua(playerId);
        if (s.contains(mauId)) {
            return false;
        }
        try {
            ConnectDB.executeUpdate("INSERT IGNORE INTO trang_phuc_so_huu (player_id, mau_id, luc) VALUES (?,?,?)",
                    playerId, mauId, System.currentTimeMillis());
            s.add(mauId);
            return true;
        } catch (Exception ex) {
            Logger.logException(TrangPhucDAO.class, ex, "Không ghi được trang phục sở hữu");
            return false;
        }
    }

    /** Trang phục mà vật phẩm {@code itemId} mở khoá, hoặc null. */
    public static Mau mauTheoVatPham(int itemId) {
        if (itemId <= 0) {
            return null;
        }
        for (Mau m : tatCa()) {
            if (m.itemId == itemId) {
                return m;
            }
        }
        return null;
    }

    /**
     * Dựng vật phẩm mở khoá ("Trang phục: tên") cho trang phục nào chưa có —
     * gọi <b>sau</b> {@code loadDatabase}. Id cấp bằng {@code MAX(id) + 1}, mỗi
     * máy một khác, nhớ vào cột {@code item_id}.
     */
    public static synchronized void damBaoVatPham() {
        damBaoBang();
        lucDoc = 0;
        for (Mau m : tatCa()) {
            try {
                if (m.itemId > 0 && coVatPham(m.itemId)) {
                    continue;
                }
                int id = themVatPham("Trang phục: " + m.ten,
                        "Dùng để mở khoá trang phục " + m.ten + " (Túi → Chức năng → Hệ thống → Trang phục)."
                        + " Chỉ đổi hình chiêu, không đổi sức mạnh.",
                        m.iconVp > 0 ? m.iconVp : m.icon);
                if (id > 0) {
                    ConnectDB.executeUpdate("UPDATE trang_phuc_mau SET item_id = ? WHERE id = ?", id, m.id);
                    Logger.success("Trang phục: thêm vật phẩm mở khoá " + m.ten + " (id " + id + ")\n");
                }
            } catch (Exception ex) {
                Logger.logException(TrangPhucDAO.class, ex, "Không dựng được vật phẩm trang phục " + m.ten);
            }
        }
        lucDoc = 0;
    }

    private static boolean coVatPham(int id) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM item_template WHERE id = ?", id);
            return rs.next();
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }

    /** Vật phẩm linh tinh kiểu 27, xếp chồng được, bấm dùng được; trả id mới hoặc -1. */
    private static int themVatPham(String ten, String moTa, int icon) throws Exception {
        int id;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(id) AS m FROM item_template");
            if (!rs.next()) {
                return -1;
            }
            id = rs.getInt("m") + 1;
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        if (id <= 0 || id > 32766) {
            return -1;
        }
        ConnectDB.executeUpdate("INSERT INTO item_template"
                + " (id, TYPE, gender, NAME, description, level, icon_id, part,"
                + " is_up_to_up, power_require, gold, gold_sell, gem, gem_sell,"
                + " ruby, ruby_sell, head, body, leg, TypeEvent, isGender,"
                + " dung_duoc, aura_id)"
                + " VALUES (?, 27, 3, ?, ?, 1, ?, -1, 1, 0, 0, 0, 0, 0, 0, 0,"
                + " -1, -1, -1, 0, -1, 1, -1)",
                id, ten, moTa, icon);
        try {
            List<nro.entity.template.ItemTemplate> ds = nro.server.Manager.ITEM_TEMPLATES;
            if (ds != null && ds.size() == id) {
                nro.entity.template.ItemTemplate t = new nro.entity.template.ItemTemplate();
                t.id = (short) id;
                t.type = 27;
                t.gender = 3;
                t.name = ten;
                t.description = moTa;
                t.level = 1;
                t.iconID = (short) icon;
                t.part = -1;
                t.isUpToUp = true;
                t.strRequire = 0;
                t.head = -1;
                t.body = -1;
                t.leg = -1;
                t.isGender = -1;
                t.dungDuoc = true;
                ds.add(t);
                nro.ui.LamMoi.bao(nro.ui.LamMoi.VAT_PHAM);
            }
        } catch (Exception boQua) {
            // Khong noi duoc vao bo nho thi dong trong CSDL van con: lan khoi dong sau se nap.
        }
        return id;
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
        SO_HUU.remove(playerId);
    }
}
