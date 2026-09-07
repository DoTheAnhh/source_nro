package nro.repository.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Cấu hình boss lấy từ CSDL: chỉ số ({@code boss_config}) và vật phẩm rơi
 * ({@code boss_drop}).
 *
 * <h2>Vì sao phải có hai bảng này</h2>
 *
 * <p>Máy chủ <b>không có sẵn bảng boss nào</b>. Toàn bộ 129 lớp boss viết cứng
 * chỉ số ngay trong mã nguồn:</p>
 *
 * <pre>{@code
 * super(PHOBANBBH, BossID.FIDE_CLAN, new BossData(
 *         "Fide đại ca", TRAI_DAT, new short[]{189, 190, 191, -1, -1, -1},
 *         20_000,                       // sức đánh
 *         new long[]{30_000_000L},      // máu từng cấp
 *         new int[]{165},               // các map có thể xuất hiện
 *         ..., 60));                    // giây chờ hồi sinh
 * }</pre>
 *
 * <p>Hai bảng ở đây là lớp <b>đè lên</b> phần viết cứng đó: dòng nào không có
 * thì boss giữ nguyên giá trị gốc, nên bảng rỗng = máy chủ chạy y như cũ.</p>
 *
 * <h2>Bộ nhớ đệm</h2>
 *
 * <p>Đọc trong luồng game (lúc dựng boss và lúc boss chết) nên phải đệm. Panel
 * gọi {@link #reload()} sau khi sửa. Chỉ số áp dụng ở <b>hàm dựng</b> nên đổi
 * chỉ số cần khởi động lại máy chủ; vật phẩm rơi đọc mỗi lần boss chết nên có
 * hiệu lực ngay.</p>
 */
public class BossDAO {

    private BossDAO() {
    }

    // =====================================================================
    //  Chỉ số
    // =====================================================================

    /** Một dòng đè chỉ số. Trường {@code null} nghĩa là giữ nguyên giá trị gốc. */
    public static final class Config {

        public int bossId;
        public String name;
        /** Các mốc HP theo cấp, ngăn nhau bằng dấu phẩy. */
        public String hp;
        public Long dame;
        public Integer secondsRest;
        /** Danh sách map id, ngăn nhau bằng dấu phẩy. */
        public String mapJoin;
        public boolean active = true;
        public String note;
        /**
         * Giáp, né đòn, chính xác của boss. {@code null} là giữ nguyên giá trị
         * gốc viết trong lớp Java của boss.
         *
         * <p>Né và chính xác tính bằng phần trăm, cùng thang với bảng chỉ số của
         * người chơi: chính xác của người đánh trừ thẳng vào né của boss.</p>
         */
        public Integer giap;
        public Integer neDon;
        public Integer chinhXac;
        /**
         * Bỏ hết vật phẩm rơi <b>viết cứng</b> của boss này.
         *
         * <p>Bật để thay bằng các dòng trong {@code boss_drop}. Tắt (mặc định)
         * thì đồ viết cứng vẫn rơi và {@code boss_drop} là phần thêm vào.</p>
         */
        public boolean chanDoGoc;
    }

    /**
     * Thêm cột {@code chan_do_goc} nếu bảng chưa có.
     *
     * <p>Dùng {@code ADD COLUMN IF NOT EXISTS} nên gọi lại không sao.</p>
     */
    private static volatile boolean daVaCot;

    public static void damBaoCot() {
        if (daVaCot) {
            return;
        }
        daVaCot = true;
        try {
            ConnectDB.executeUpdate("ALTER TABLE boss_config ADD COLUMN IF NOT EXISTS"
                    + " chan_do_goc TINYINT(1) NOT NULL DEFAULT 0");
            // Giap / ne don / chinh xac cua boss. De NULL la giu nguyen so goc
            // viet trong lop Java, khong phai la 0 — 0 con la mot lua chon hop le
            // (vi du go ne = 0 de tat han ne cua mot con von co ne).
            ConnectDB.executeUpdate("ALTER TABLE boss_config ADD COLUMN IF NOT EXISTS"
                    + " giap INT(11) DEFAULT NULL");
            ConnectDB.executeUpdate("ALTER TABLE boss_config ADD COLUMN IF NOT EXISTS"
                    + " ne_don INT(11) DEFAULT NULL");
            ConnectDB.executeUpdate("ALTER TABLE boss_config ADD COLUMN IF NOT EXISTS"
                    + " chinh_xac INT(11) DEFAULT NULL");
            // HSD rieng cho tung mon roi. 0 = khong dat han, mon vinh vien.
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " hsd_min INT(11) NOT NULL DEFAULT 0");
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " hsd_max INT(11) NOT NULL DEFAULT 0");
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " hsd_vinh_vien INT(11) NOT NULL DEFAULT 0");
            // Phase mon nay roi. -1 = moi phase, nen dong cu giu nguyen hanh vi.
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " phase INT(11) NOT NULL DEFAULT -1");
            // Dieu kien nhiem vu cua NGUOI GIET. Tat ca -1 = khong doi hoi gi.
            // nv_toi_thieu giu ten cu nhung mang nghia "TU nhiem vu nay tro di".
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " nv_toi_thieu INT(11) NOT NULL DEFAULT -1");
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " nv_dung INT(11) NOT NULL DEFAULT -1");
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " nv_den INT(11) NOT NULL DEFAULT -1");
            // Chan roi khi nguoi giet DA CO mon nay (hanh trang + ruong o nha).
            ConnectDB.executeUpdate("ALTER TABLE boss_drop ADD COLUMN IF NOT EXISTS"
                    + " chi_mot_mon TINYINT(1) NOT NULL DEFAULT 0");
            // Doi "moi phase" (-1) thanh phase 1. Moi phase gio la mot bang roi
            // rieng, khong con gia tri nao mang nghia "ap cho tat ca".
            ConnectDB.executeUpdate("UPDATE boss_drop SET phase = 1 WHERE phase < 1");
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex,
                    "Không thêm được cột chan_do_goc — đồ rơi viết cứng vẫn rơi");
        }
    }

    /**
     * {@code true} nếu boss này đang tắt đồ rơi viết cứng.
     *
     * <p>Đọc từ bộ nhớ đệm, không truy vấn: hàm này chạy <b>mỗi lần một vật phẩm
     * rơi xuống đất</b>.</p>
     */
    public static boolean chanDoGoc(int bossId) {
        Config c = config(bossId);
        return c != null && c.chanDoGoc;
    }

    private static final Map<Integer, Config> CONFIGS = new HashMap<>();
    private static final Map<Integer, List<Drop>> DROPS = new HashMap<>();
    private static volatile boolean loaded;

    /** Đọc lại cả hai bảng. */
    public static void reload() {
        nro.repository.schema.LuocDoPanel.damBao();
        damBaoCot();
        Map<Integer, Config> cfg = new HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM boss_config");
            while (rs.next()) {
                Config c = new Config();
                c.bossId = rs.getInt("boss_id");
                c.name = trimToNull(rs.getStringOrNull("name"));
                c.hp = trimToNull(rs.getStringOrNull("hp"));
                c.dame = nullableLong(rs.getObject("dame"));
                c.secondsRest = nullableInt(rs.getObject("seconds_rest"));
                c.mapJoin = trimToNull(rs.getStringOrNull("map_join"));
                c.active = rs.getBoolean("active");
                c.note = rs.getStringOrNull("note");
                try {
                    c.chanDoGoc = rs.getBoolean("chan_do_goc");
                } catch (Exception thieuCot) {
                    // Ban CSDL cu chua co cot -> giu hanh vi cu.
                    c.chanDoGoc = false;
                }
                docChiSoPhu(rs, c);
                cfg.put(c.bossId, c);
            }
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Không đọc được boss_config");
        } finally {
            dispose(rs);
        }

        Map<Integer, List<Drop>> drops = new HashMap<>();
        rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM boss_drop WHERE active = 1");
            while (rs.next()) {
                Drop d = new Drop();
                d.id = rs.getInt("id");
                d.bossId = rs.getInt("boss_id");
                d.itemId = rs.getInt("item_id");
                d.qtyMin = rs.getInt("quantity_min");
                d.qtyMax = rs.getInt("quantity_max");
                d.rateNum = rs.getInt("rate_num");
                d.rateDen = rs.getInt("rate_den");
                d.options = rs.getStringOrNull("options");
                d.hsdMin = rs.getInt("hsd_min");
                d.hsdMax = rs.getInt("hsd_max");
                d.hsdVinhVien = rs.getInt("hsd_vinh_vien");
                d.phase = rs.getInt("phase");
                d.nvTu = rs.getInt("nv_toi_thieu");
                d.nvDung = rs.getInt("nv_dung");
                d.nvDen = rs.getInt("nv_den");
                // tinyint(1) -> driver tra ve Boolean, getInt se nem ClassCastException.
                d.chiMotMon = rs.getBoolean("chi_mot_mon");
                d.active = true;
                d.note = rs.getStringOrNull("note");
                drops.computeIfAbsent(d.bossId, k -> new ArrayList<>()).add(d);
            }
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Không đọc được boss_drop");
        } finally {
            dispose(rs);
        }

        synchronized (CONFIGS) {
            CONFIGS.clear();
            CONFIGS.putAll(cfg);
        }
        synchronized (DROPS) {
            DROPS.clear();
            DROPS.putAll(drops);
        }
        // Đặt cờ kể cả khi đọc hỏng: nếu không thì mỗi lần dựng boss lại thử
        // truy vấn thêm một lần nữa, ngay trong luồng game.
        loaded = true;
    }

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    /** Dòng đè chỉ số của một boss, {@code null} nếu không có hoặc đang tắt. */
    public static Config config(int bossId) {
        ensureLoaded();
        Config c;
        synchronized (CONFIGS) {
            c = CONFIGS.get(bossId);
        }
        return c != null && c.active ? c : null;
    }

    /** Toàn bộ dòng đè chỉ số, kể cả dòng đang tắt — dùng cho panel. */
    public static List<Config> allConfigs() {
        List<Config> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM boss_config ORDER BY boss_id");
            while (rs.next()) {
                Config c = new Config();
                c.bossId = rs.getInt("boss_id");
                c.name = rs.getStringOrNull("name");
                c.hp = rs.getStringOrNull("hp");
                c.dame = nullableLong(rs.getObject("dame"));
                c.secondsRest = nullableInt(rs.getObject("seconds_rest"));
                c.mapJoin = rs.getStringOrNull("map_join");
                c.active = rs.getBoolean("active");
                c.note = rs.getStringOrNull("note");
                try {
                    c.chanDoGoc = rs.getBoolean("chan_do_goc");
                } catch (Exception thieuCot) {
                    c.chanDoGoc = false;
                }
                docChiSoPhu(rs, c);
                out.add(c);
            }
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Lỗi đọc boss_config");
        } finally {
            dispose(rs);
        }
        return out;
    }

    /**
     * Đọc ba ô giáp / né / chính xác, chịu được bản CSDL chưa có cột.
     *
     * <p>Gộp thành một hàm vì cả {@code reload} và {@code allConfigs} đều cần, mà
     * hai chỗ đó từng đọc lệch nhau — {@code allConfigs} thiếu một cột là panel
     * hiện ô trống trong khi CSDL có số, người dùng sửa xong lại ghi đè mất.</p>
     */
    private static void docChiSoPhu(CrisResultSet rs, Config c) {
        try {
            c.giap = nullableInt(rs.getObject("giap"));
            c.neDon = nullableInt(rs.getObject("ne_don"));
            c.chinhXac = nullableInt(rs.getObject("chinh_xac"));
        } catch (Exception thieuCot) {
            c.giap = null;
            c.neDon = null;
            c.chinhXac = null;
        }
    }

    /** Ô phần trăm phải nằm trong 0–100, hoặc để trống. */
    private static String kiemTraPhanTram(Integer v, String ten) {
        if (v == null) {
            return null;
        }
        return (v < 0 || v > 100) ? (ten + " phải từ 0 đến 100.") : null;
    }

    /** Thêm hoặc sửa một dòng đè chỉ số. Trả câu lỗi, hoặc {@code null} nếu xong. */
    public static String saveConfig(Config c) {
        damBaoCot();
        String loi = kiemTraSo(c.hp, "HP");
        if (loi != null) {
            return loi;
        }
        loi = kiemTraSo(c.mapJoin, "Danh sách map");
        if (loi != null) {
            return loi;
        }
        if (c.secondsRest != null && c.secondsRest < 0) {
            return "Giây chờ hồi sinh không được âm.";
        }
        if (c.giap != null && c.giap < 0) {
            return "Giáp không được âm.";
        }
        loi = kiemTraPhanTram(c.neDon, "Né đòn");
        if (loi != null) {
            return loi;
        }
        loi = kiemTraPhanTram(c.chinhXac, "Chính xác");
        if (loi != null) {
            return loi;
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO boss_config (boss_id, name, hp, dame, seconds_rest,"
                    + " map_join, active, note, chan_do_goc, giap, ne_don, chinh_xac)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE name = VALUES(name), hp = VALUES(hp),"
                    + " dame = VALUES(dame), seconds_rest = VALUES(seconds_rest),"
                    + " map_join = VALUES(map_join), active = VALUES(active),"
                    + " note = VALUES(note), chan_do_goc = VALUES(chan_do_goc),"
                    + " giap = VALUES(giap), ne_don = VALUES(ne_don),"
                    + " chinh_xac = VALUES(chinh_xac)",
                    c.bossId, c.name, c.hp, c.dame, c.secondsRest,
                    c.mapJoin, c.active ? 1 : 0, c.note, c.chanDoGoc ? 1 : 0,
                    c.giap, c.neDon, c.chinhXac);
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Lỗi lưu boss_config " + c.bossId);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    public static boolean deleteConfig(int bossId) {
        try {
            boolean ok = ConnectDB.executeUpdate(
                    "DELETE FROM boss_config WHERE boss_id = ?", bossId) == 1;
            reload();
            return ok;
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Lỗi xoá boss_config " + bossId);
            return false;
        }
    }

    // =====================================================================
    //  Vật phẩm rơi
    // =====================================================================

    /** Một dòng vật phẩm rơi thêm khi boss chết. */
    /**
     * Giá trị {@code itemId} đặc biệt: <b>bốc ngẫu nhiên một món Thần Linh</b>.
     *
     * <p>Dùng một id âm làm dấu thay vì thêm cột mới: cột {@code item_id} đã là
     * số nguyên, và id thật của vật phẩm không bao giờ âm nên không thể đụng
     * nhau.</p>
     */
    public static final int ITEM_THAN_LINH_NGAU_NHIEN = -1;

    /**
     * Bộ Thần Linh — 13 món, đúng danh sách mã game vẫn dùng.
     *
     * <p>Áo / quần / găng / giày cho cả ba hành tinh (12 món) cộng Nhẫn Thần Linh.
     * Nhẫn không theo hành tinh nào nên dùng chung.</p>
     */
    public static final short[] DO_THAN_LINH = {
        555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567};

    public static final class Drop {

        public int id;
        public int bossId;
        public int itemId;
        public int qtyMin = 1;
        public int qtyMax = 1;
        /** Tỉ lệ {@code rateNum / rateDen}. */
        public int rateNum = 1;
        public int rateDen = 100;
        public String options = "[]";
        /** So ngay HSD it nhat. 0 = mon nay khong co han (vinh vien). */
        public int hsdMin = 0;
        /** So ngay HSD nhieu nhat. 0 = mon nay khong co han (vinh vien). */
        public int hsdMax = 0;
        /** Ty le phan tram mon roi ra la VINH VIEN, bo qua HSD o tren. */
        public int hsdVinhVien = 0;
        public boolean active = true;
        public String note;

        /**
         * Điều kiện nhiệm vụ của <b>người giết</b>. {@code -1} là bỏ qua điều kiện đó.
         *
         * <p>Ba điều kiện độc lập, đều phải thoả:</p>
         * <ul>
         *   <li>{@code nvDung} — chỉ rơi khi đang ở <b>đúng</b> nhiệm vụ này</li>
         *   <li>{@code nvTu} — chỉ rơi <b>từ</b> nhiệm vụ này trở đi</li>
         *   <li>{@code nvDen} — chỉ rơi <b>trước</b> nhiệm vụ này (không tính chính nó)</li>
         * </ul>
         *
         * <p>Dùng cho những món chỉ có nghĩa ở một chặng — Nhẫn Thời Không rơi
         * cho ai đang làm nhiệm vụ tìm nhẫn, rơi sớm quá thì người mới nhặt được
         * rồi vứt đi, rơi muộn quá thì thừa.</p>
         *
         * <p>Cột CSDL của {@code nvTu} vẫn tên {@code nv_toi_thieu} — tên cũ từ
         * lúc mới chỉ có một điều kiện, đổi tên cột thì mất dữ liệu đang có.</p>
         */
        public int nvDung = -1;
        public int nvTu = -1;
        public int nvDen = -1;

        /** Người giết có thoả mọi điều kiện nhiệm vụ không. */
        public boolean hopNhiemVu(nro.entity.player.Player nguoiGiet) {
            if (nvDung < 0 && nvTu < 0 && nvDen < 0) {
                return true;
            }
            if (nguoiGiet == null || nguoiGiet.playerTask == null
                    || nguoiGiet.playerTask.taskMain == null) {
                return false;
            }
            int nv = nguoiGiet.playerTask.taskMain.id;
            if (nvDung >= 0 && nv != nvDung) {
                return false;
            }
            if (nvTu >= 0 && nv < nvTu) {
                return false;
            }
            if (nvDen >= 0 && nv >= nvDen) {
                return false;
            }
            return true;
        }

        /** Mô tả ngắn để hiện trên bảng; rỗng nếu không có điều kiện nào. */
        public String moTaNhiemVu() {
            StringBuilder sb = new StringBuilder();
            if (nvDung >= 0) {
                sb.append("đúng NV ").append(nvDung);
            }
            if (nvTu >= 0) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append("từ NV ").append(nvTu);
            }
            if (nvDen >= 0) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append("trước NV ").append(nvDen);
            }
            return sb.toString();
        }


        /**
         * Chỉ rơi khi người giết <b>chưa có</b> món này.
         *
         * <p>Đếm gộp <b>hành trang + rương ở nhà</b>: có một cái ở rương mà
         * trong người không có thì vẫn coi như đã có, không rơi nữa. Dùng cho
         * những món độc nhất kiểu Nhẫn Thời Không — rơi cái thứ hai chỉ tổ chật
         * chỗ và làm loãng giá trị.</p>
         *
         * <p>Đếm theo <b>số lượng</b> chứ không theo số ô: một ô chứa 5 cái vẫn
         * là đã có 5.</p>
         */
        public boolean chiMotMon;

        /** Người giết đã giữ món {@code itemId} này chưa (hành trang + rương). */
        public boolean daCoMon(nro.entity.player.Player nguoiGiet) {
            if (nguoiGiet == null || nguoiGiet.inventory == null) {
                return false;
            }
            return demTrong(nguoiGiet.inventory.itemsBag)
                    + demTrong(nguoiGiet.inventory.itemsBox) > 0;
        }

        private int demTrong(java.util.List<nro.entity.item.Item> ds) {
            if (ds == null) {
                return 0;
            }
            int n = 0;
            for (nro.entity.item.Item it : ds) {
                if (it != null && it.isNotNullItem() && it.template != null
                        && it.template.id == itemId) {
                    n += Math.max(1, it.quantity);
                }
            }
            return n;
        }


        /**
         * Phase mà món này rơi, đếm từ <b>1</b>.
         *
         * <p>Mỗi phase là một bảng rơi riêng — coi như những con boss khác nhau.
         * Không còn giá trị "mọi phase": muốn một món rơi ở cả hai phase thì
         * thêm hai dòng, đổi lại đặt được tỉ lệ khác nhau cho từng phase.</p>
         *
         * <p>Trong mã {@code currentLevel} đếm từ 0 nên chỗ so sánh phải trừ một.</p>
         */
        public int phase = 1;

        /** Món này có rơi ở phase đang đánh không. {@code capHienTai} đếm từ 0. */
        public boolean roiOPhase(int capHienTai) {
            return Math.max(1, phase) - 1 == capHienTai;
        }
    }

    /** Các món rơi thêm của một boss. Danh sách rỗng nếu không cấu hình gì. */
    public static List<Drop> drops(int bossId) {
        ensureLoaded();
        List<Drop> l;
        synchronized (DROPS) {
            l = DROPS.get(bossId);
        }
        return l == null ? java.util.Collections.emptyList() : l;
    }

    /** Toàn bộ dòng rơi, kể cả dòng đang tắt — dùng cho panel. */
    public static List<Drop> allDrops() {
        List<Drop> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM boss_drop ORDER BY boss_id, id");
            while (rs.next()) {
                Drop d = new Drop();
                d.id = rs.getInt("id");
                d.bossId = rs.getInt("boss_id");
                d.itemId = rs.getInt("item_id");
                d.qtyMin = rs.getInt("quantity_min");
                d.qtyMax = rs.getInt("quantity_max");
                d.rateNum = rs.getInt("rate_num");
                d.rateDen = rs.getInt("rate_den");
                d.options = rs.getStringOrNull("options");
                d.hsdMin = rs.getInt("hsd_min");
                d.hsdMax = rs.getInt("hsd_max");
                d.hsdVinhVien = rs.getInt("hsd_vinh_vien");
                // Bon cot nay tung bi bo sot o day: reload() doc du nen trong
                // game chay dung, nhung panel doc qua allDrops() nen mo hop sua
                // ra la thay trong tron — nhin nhu luu khong an.
                d.phase = rs.getInt("phase");
                d.nvTu = rs.getInt("nv_toi_thieu");
                d.nvDung = rs.getInt("nv_dung");
                d.nvDen = rs.getInt("nv_den");
                d.chiMotMon = rs.getBoolean("chi_mot_mon");
                d.active = rs.getBoolean("active");
                d.note = rs.getStringOrNull("note");
                out.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Lỗi đọc boss_drop");
        } finally {
            dispose(rs);
        }
        return out;
    }

    /** Thêm hoặc sửa một dòng rơi. {@code d.id <= 0} là thêm mới. */
    public static String saveDrop(Drop d) {
        if (d.qtyMin <= 0 || d.qtyMax < d.qtyMin) {
            return "Số lượng phải > 0 và số lượng tối đa không được nhỏ hơn tối thiểu.";
        }
        if (d.rateDen <= 0 || d.rateNum <= 0 || d.rateNum > d.rateDen) {
            return "Tỉ lệ phải có dạng a/b với 0 < a <= b.";
        }
        try {
            if (d.id > 0) {
                ConnectDB.executeUpdate(
                        "UPDATE boss_drop SET boss_id = ?, item_id = ?, quantity_min = ?,"
                        + " quantity_max = ?, rate_num = ?, rate_den = ?, options = ?,"
                        + " active = ?, note = ?, hsd_min = ?, hsd_max = ?,"
                        + " hsd_vinh_vien = ?, phase = ?, nv_toi_thieu = ?,"
                        + " nv_dung = ?, nv_den = ?, chi_mot_mon = ?"
                        + " WHERE id = ?",
                        d.bossId, d.itemId, d.qtyMin, d.qtyMax, d.rateNum, d.rateDen,
                        d.options, d.active ? 1 : 0, d.note,
                        d.hsdMin, d.hsdMax, d.hsdVinhVien, d.phase, d.nvTu,
                        d.nvDung, d.nvDen, d.chiMotMon ? 1 : 0, d.id);
            } else {
                ConnectDB.executeUpdate(
                        "INSERT INTO boss_drop (boss_id, item_id, quantity_min, quantity_max,"
                        + " rate_num, rate_den, options, active, note, hsd_min,"
                        + " hsd_max, hsd_vinh_vien, phase, nv_toi_thieu, nv_dung, nv_den,"
                        + " chi_mot_mon)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        d.bossId, d.itemId, d.qtyMin, d.qtyMax, d.rateNum, d.rateDen,
                        d.options, d.active ? 1 : 0, d.note,
                        d.hsdMin, d.hsdMax, d.hsdVinhVien, d.phase, d.nvTu,
                        d.nvDung, d.nvDen, d.chiMotMon ? 1 : 0);
            }
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Lỗi lưu boss_drop");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    public static boolean deleteDrop(int id) {
        try {
            boolean ok = ConnectDB.executeUpdate("DELETE FROM boss_drop WHERE id = ?", id) == 1;
            reload();
            return ok;
        } catch (Exception ex) {
            Logger.logException(BossDAO.class, ex, "Lỗi xoá boss_drop " + id);
            return false;
        }
    }

    // =====================================================================
    //  Tiện ích
    // =====================================================================

    /** Tách chuỗi {@code "1,2,3"} thành mảng. Trả {@code null} nếu chuỗi rỗng. */
    public static long[] parseLongs(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return null;
        }
        String[] p = csv.split(",");
        List<Long> out = new ArrayList<>();
        for (String x : p) {
            // Bo dau cham ngan nghin — o nhap ben panel hien "2.000.000.000" cho
            // de doc, con day la con so that.
            String t = x.replace(".", "").replace(" ", "").trim();
            if (!t.isEmpty()) {
                out.add(Long.parseLong(t));
            }
        }
        if (out.isEmpty()) {
            return null;
        }
        long[] a = new long[out.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = out.get(i);
        }
        return a;
    }

    /** Như {@link #parseLongs} nhưng trả mảng {@code int}. */
    public static int[] parseInts(String csv) {
        long[] l = parseLongs(csv);
        if (l == null) {
            return null;
        }
        int[] a = new int[l.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = (int) l[i];
        }
        return a;
    }

    private static String kiemTraSo(String csv, String ten) {
        if (csv == null || csv.trim().isEmpty()) {
            return null;
        }
        try {
            parseLongs(csv);
            return null;
        } catch (NumberFormatException ex) {
            return ten + " phải là các số nguyên ngăn nhau bằng dấu phẩy, ví dụ"
                    + " 1.000,2.000.";
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Long nullableLong(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static Integer nullableInt(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }

    private static void dispose(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }

}
