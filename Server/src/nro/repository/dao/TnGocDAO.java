package nro.repository.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.entity.template.MobTemplate;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Manager;

/**
 * Phần trăm tiềm năng của từng loại quái — bảng {@code tn_goc_quai}.
 *
 * <h2>Tiềm năng gốc của một đòn</h2>
 *
 * <p>Đúng cách tính gốc của game, chỉ đưa các con số ra panel:</p>
 * <ol>
 * <li>đòn đánh lấy đi bao nhiêu <b>phần trăm máu tối đa</b> của quái (số
 * nguyên; quái máu lớn thì phần trăm ấy nhân thêm);</li>
 * <li>nhân với <b>kho tiềm năng</b> của con quái = máu tối đa × <b>phần trăm
 * tiềm năng của loại quái</b> (bảng này) ± dao động, rồi chia 100;</li>
 * <li>mỗi cấp người chơi cao hơn quái bớt đi một phần, thấp hơn thêm một phần.</li>
 * </ol>
 * <p>Các con số của bước 1 và 3 nằm ở quy ước ({@code tn_goc_*}); bảng này giữ
 * bước 2.</p>
 *
 * <h2>Gieo sẵn từ dữ liệu game</h2>
 *
 * <p>Mỗi lần nạp, loại quái nào chưa có dòng thì được thêm với đúng số
 * {@code percent_tiem_nang} của {@code mob_template} — quái mới thêm vào game
 * cũng tự có dòng. {@code mob_template} không bị đụng tới, nên "Về mặc định"
 * luôn lấy lại được số gốc.</p>
 */
public final class TnGocDAO {

    private TnGocDAO() {
    }

    /** Một loại quái. */
    public static final class Dong {

        public int id;
        public String ten = "";
        /** Máu gốc trong {@code mob_template} — chỉ để xem. */
        public long hp;
        public int phanTram;
        /** Số gốc của game ({@code mob_template.percent_tiem_nang}). */
        public int phanTramGoc;
        public String ghiChu = "";
    }

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `tn_goc_quai` ("
            + " `mob_id` int(11) NOT NULL,"
            + " `ten` varchar(120) NOT NULL DEFAULT '',"
            + " `phan_tram` int(11) NOT NULL DEFAULT 0,"
            + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (`mob_id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTao;
    /** mob_id → phần trăm; {@code null} là đọc lại ở lần hỏi kế tiếp. */
    private static volatile Map<Integer, Integer> BANG;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (TnGocDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTao = true;
            } catch (Exception ex) {
                Logger.logException(TnGocDAO.class, ex, "Không tạo được bảng tn_goc_quai");
            }
        }
    }

    /** Thêm dòng cho loại quái chưa có, lấy đúng số của game. */
    private static void gieoThieu() {
        List<MobTemplate> ds = new ArrayList<>(Manager.MOB_TEMPLATES);
        final int lo = 100;
        int them = 0;
        for (int i = 0; i < ds.size(); i += lo) {
            List<MobTemplate> phan = ds.subList(i, Math.min(ds.size(), i + lo));
            StringBuilder sql = new StringBuilder(
                    "INSERT IGNORE INTO tn_goc_quai (mob_id, ten, phan_tram) VALUES ");
            Object[] thamSo = new Object[phan.size() * 3];
            for (int j = 0; j < phan.size(); j++) {
                MobTemplate t = phan.get(j);
                sql.append(j == 0 ? "(?,?,?)" : ",(?,?,?)");
                thamSo[j * 3] = t.id;
                thamSo[j * 3 + 1] = t.name == null ? "" : t.name;
                thamSo[j * 3 + 2] = (int) t.percentTiemNang;
            }
            try {
                them += ConnectDB.executeUpdate(sql.toString(), thamSo);
            } catch (Exception ex) {
                Logger.logException(TnGocDAO.class, ex, "Lỗi gieo tn_goc_quai");
                return;
            }
        }
        if (them > 0) {
            Logger.success("CONFIG", "Đã gieo " + them
                    + " loại quái vào bảng tiềm năng gốc (số gốc của game)");
        }
    }

    private static Map<Integer, Integer> bang() {
        Map<Integer, Integer> b = BANG;
        if (b != null) {
            return b;
        }
        synchronized (TnGocDAO.class) {
            if (BANG != null) {
                return BANG;
            }
            damBaoBang();
            gieoThieu();
            Map<Integer, Integer> moi = new HashMap<>();
            CrisResultSet rs = null;
            try {
                rs = ConnectDB.executeQuery("SELECT mob_id, phan_tram FROM tn_goc_quai");
                while (rs.next()) {
                    moi.put(rs.getInt("mob_id"), rs.getInt("phan_tram"));
                }
            } catch (Exception ex) {
                Logger.logException(TnGocDAO.class, ex, "Lỗi đọc tn_goc_quai");
            } finally {
                dong(rs);
            }
            BANG = moi;
            return moi;
        }
    }

    /** Đọc lại từ CSDL ở lần hỏi kế tiếp. */
    public static void reload() {
        BANG = null;
    }

    /**
     * Phần trăm tiềm năng của một loại quái.
     *
     * @param duPhong số dùng khi loại quái chưa có dòng — số đang gắn trên con
     *                quái
     */
    public static int phanTram(int mobId, int duPhong) {
        Integer v = bang().get(mobId);
        return v == null ? duPhong : v;
    }

    /** Cả bảng, kèm tên, máu và số gốc của game để panel hiện. */
    public static List<Dong> danhSach() {
        Map<Integer, Integer> b = bang();
        Map<Integer, String> ghiChu = new HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT mob_id, ghi_chu FROM tn_goc_quai");
            while (rs.next()) {
                ghiChu.put(rs.getInt("mob_id"), rs.getString("ghi_chu"));
            }
        } catch (Exception ex) {
            Logger.logException(TnGocDAO.class, ex, "Lỗi đọc tn_goc_quai");
        } finally {
            dong(rs);
        }
        List<Dong> ds = new ArrayList<>();
        for (MobTemplate t : new ArrayList<>(Manager.MOB_TEMPLATES)) {
            Dong d = new Dong();
            d.id = t.id;
            d.ten = t.name == null ? "" : t.name;
            d.hp = t.hp;
            d.phanTramGoc = t.percentTiemNang;
            Integer v = b.get(t.id);
            d.phanTram = v == null ? t.percentTiemNang : v;
            String g = ghiChu.get(t.id);
            d.ghiChu = g == null ? "" : g;
            ds.add(d);
        }
        return ds;
    }

    public static String luu(int mobId, int phanTram, String ghiChu) {
        if (phanTram < 0) {
            return "Phần trăm tiềm năng không được âm.";
        }
        damBaoBang();
        String ten = "";
        for (MobTemplate t : new ArrayList<>(Manager.MOB_TEMPLATES)) {
            if (t.id == mobId) {
                ten = t.name == null ? "" : t.name;
                break;
            }
        }
        try {
            ConnectDB.executeUpdate("INSERT INTO tn_goc_quai (mob_id, ten, phan_tram, ghi_chu)"
                    + " VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " phan_tram = VALUES(phan_tram), ghi_chu = VALUES(ghi_chu)",
                    mobId, ten, phanTram, ghiChu == null ? "" : ghiChu);
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(TnGocDAO.class, ex, "Lỗi lưu tn_goc_quai");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Xoá sạch rồi gieo lại đúng số gốc của game. */
    public static String veMacDinh() {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM tn_goc_quai");
            reload();
            bang();
            return null;
        } catch (Exception ex) {
            Logger.logException(TnGocDAO.class, ex, "Lỗi gieo lại tn_goc_quai");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
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
