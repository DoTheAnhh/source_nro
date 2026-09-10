package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Sức mạnh càng cao thì tiềm năng nhận được càng ít — bảng {@code bac_giam_tnsm}.
 *
 * <h2>Vì sao là bảng chứ không phải mảng trong mã</h2>
 *
 * <p>Mười một con số này quyết định người chơi cày bao lâu thì chững lại, tức là
 * quyết định nhịp của cả máy chủ. Trước đây chúng nằm cứng trong
 * {@code NPoint.calSubTNSM}: muốn nới một bậc phải sửa mã, dịch lại, khởi động
 * lại. Nay là dữ liệu, sửa trên panel là có hiệu lực ngay.</p>
 *
 * <h2>Cách đọc một dòng</h2>
 *
 * <p>"Từ mốc sức mạnh này trở lên thì chỉ còn nhận ngần này phần trăm". Lấy
 * <b>bậc cao nhất mà người chơi đạt tới</b>, rồi dừng — không cộng dồn nhiều bậc.
 * Dưới bậc thấp nhất là nhận nguyên vẹn 100%.</p>
 *
 * <p>Bảng áp cho <b>cả sư phụ lẫn đệ tử</b>, mỗi bên tra bằng sức mạnh của chính
 * mình: sư phụ mạnh thì phần chia của sư phụ ít đi, không ăn theo bậc của đệ.</p>
 */
public final class BacGiamTnsmDAO {

    private BacGiamTnsmDAO() {
    }

    /** Một bậc giảm. */
    public static final class Dong {

        public int id;
        /** Từ mốc sức mạnh này trở lên thì áp bậc. */
        public long moc;
        /** Phần trăm tiềm năng còn được nhận, {@code 100} là nguyên vẹn. */
        public int conLai = 100;
        public boolean bat = true;
        public String ghiChu = "";
    }

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `bac_giam_tnsm` ("
            + " `id` int(11) NOT NULL AUTO_INCREMENT,"
            + " `moc` bigint(20) NOT NULL DEFAULT 0,"
            + " `con_lai` int(11) NOT NULL DEFAULT 100,"
            + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
            + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (`id`),"
            + " UNIQUE KEY `moc` (`moc`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTao;
    private static volatile List<Dong> CACHE;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (BacGiamTnsmDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTao = true;
            } catch (Exception ex) {
                Logger.logException(BacGiamTnsmDAO.class, ex,
                        "Không tạo được bảng bac_giam_tnsm");
            }
        }
    }

    /** Các bậc, mốc cao đứng trước. Tự gieo bảng gốc nếu còn trống. */
    public static List<Dong> danhSach() {
        List<Dong> c = CACHE;
        if (c != null) {
            return c;
        }
        damBaoBang();
        List<Dong> ds = doc();
        if (ds.isEmpty()) {
            for (Dong d : goc()) {
                luu(d);
            }
            ds = doc();
            Logger.success("CONFIG", "Đã gieo " + ds.size()
                    + " bậc giảm tiềm năng theo sức mạnh");
        }
        CACHE = ds;
        return ds;
    }

    public static void reload() {
        CACHE = null;
    }

    private static List<Dong> doc() {
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id, moc, con_lai, bat, ghi_chu"
                    + " FROM bac_giam_tnsm ORDER BY moc DESC");
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getInt("id");
                d.moc = rs.getLong("moc");
                d.conLai = rs.getInt("con_lai");
                d.bat = rs.getBoolean("bat");
                d.ghiChu = rs.getString("ghi_chu");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(BacGiamTnsmDAO.class, ex, "Lỗi đọc bậc giảm tiềm năng");
        } finally {
            dong(rs);
        }
        return ds;
    }

    public static String luu(Dong d) {
        damBaoBang();
        if (d == null || d.moc <= 0) {
            return "Mốc sức mạnh phải lớn hơn 0.";
        }
        if (d.conLai < 0 || d.conLai > 100) {
            return "Phần trăm còn lại phải trong khoảng 0 đến 100.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO bac_giam_tnsm (moc, con_lai, bat, ghi_chu)"
                    + " VALUES (?,?,?,?)"
                    + " ON DUPLICATE KEY UPDATE con_lai = VALUES(con_lai),"
                    + " bat = VALUES(bat), ghi_chu = VALUES(ghi_chu)",
                    d.moc, d.conLai, d.bat ? 1 : 0,
                    d.ghiChu == null ? "" : d.ghiChu);
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(BacGiamTnsmDAO.class, ex, "Lỗi lưu bậc giảm tiềm năng");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(long moc) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM bac_giam_tnsm WHERE moc = ?", moc);
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(BacGiamTnsmDAO.class, ex, "Lỗi xoá bậc giảm tiềm năng");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Xoá sạch bảng để lần hỏi sau gieo lại các bậc gốc. */
    public static String gieoLai() {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM bac_giam_tnsm");
            reload();
            danhSach();
            return null;
        } catch (Exception ex) {
            Logger.logException(BacGiamTnsmDAO.class, ex, "Lỗi gieo lại bậc giảm");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Phần trăm tiềm năng một người có sức mạnh {@code sucManh} còn được nhận.
     *
     * @return {@code 100} nếu chưa chạm bậc nào
     */
    public static int phanTram(long sucManh) {
        for (Dong d : danhSach()) {
            if (d.bat && sucManh >= d.moc) {
                return d.conLai;
            }
        }
        return 100;
    }

    // =====================================================================
    //  Bảng gốc
    // =====================================================================

    private static Dong d(long moc, int conLai, String ghiChu) {
        Dong x = new Dong();
        x.moc = moc;
        x.conLai = conLai;
        x.ghiChu = ghiChu;
        return x;
    }

    /**
     * Đúng mười một bậc đang chạy trước khi có bảng này.
     *
     * <p>Bảng trước nữa chỉ có bốn bậc và bậc đầu ở tận 40 tỉ: dưới mốc đó thì
     * mạnh hay yếu cũng nhận y như nhau, rồi qua mốc là tụt thẳng một nửa. Người
     * chơi cảm thấy đúng một chuyện — "tự nhiên chững lại" — chứ không thấy một
     * đường dốc.</p>
     */
    private static List<Dong> goc() {
        List<Dong> ds = new ArrayList<>();
        ds.add(d(200_000_000_000L, 1, "Gần trần sức mạnh"));
        ds.add(d(120_000_000_000L, 2, ""));
        ds.add(d(100_000_000_000L, 3, ""));
        ds.add(d(80_000_000_000L, 5, ""));
        ds.add(d(60_000_000_000L, 8, ""));
        ds.add(d(50_000_000_000L, 12, ""));
        ds.add(d(40_000_000_000L, 20, ""));
        ds.add(d(30_000_000_000L, 30, ""));
        ds.add(d(20_000_000_000L, 45, ""));
        ds.add(d(10_000_000_000L, 60, ""));
        ds.add(d(5_000_000_000L, 80, "Bậc đầu tiên — dưới mốc này nhận nguyên vẹn"));
        return ds;
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
