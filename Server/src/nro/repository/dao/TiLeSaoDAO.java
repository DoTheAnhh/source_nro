package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Tỉ lệ, giá vàng và giá ngọc của <b>từng bậc nâng sao pha lê</b> trên trang bị.
 *
 * <h2>Vì sao đưa ra bảng</h2>
 *
 * <p>Ba con số này trước đây nằm trong ba hàm {@code switch} viết cứng của
 * {@code PhaLeHoaTrangBi}. Đổi cân bằng phải sửa mã rồi biên dịch lại, mà cân
 * bằng là thứ chỉnh nhiều nhất.</p>
 *
 * <h2>Con số cũ hiện một đằng, chạy một nẻo</h2>
 *
 * <p>Bảng cũ ghi 70, 50, 30… và bảng chọn in ra "Tỉ lệ thành công: 70%". Nhưng
 * phép bốc là {@code Util.isTrue(tiLe, 1000)} — <b>trên một nghìn</b>, không
 * phải trên một trăm. Nên 70 thật ra là <b>7%</b>, và cả bảng chạy đúng một
 * phần mười con số hiện ra.</p>
 *
 * <p>Bậc 7 lên 8 ghi {@code 0.25f}, tức <b>0,025%</b> — trung bình bốn nghìn
 * lần mới lên một cái. Đó là lý do "đập mấy nghìn lần không lên".</p>
 *
 * <p>Nay tỉ lệ trong bảng này là <b>phần trăm thật</b>: gõ 70 thì đúng bảy mươi
 * phần trăm. Giá trị dựng sẵn lấy đúng dãy số mà bảng chọn vẫn in ra bấy lâu,
 * nên lời hứa trên màn hình thành ra đúng.</p>
 */
public final class TiLeSaoDAO {

    private TiLeSaoDAO() {
    }

    /** Số sao lớn nhất một món có thể mang. Khớp {@code CombineService.MAX_STAR_ITEM}. */
    public static final int MAX_SAO = 8;

    /** Một bậc nâng: từ {@code sao} lên {@code sao + 1}. */
    public static final class Bac {

        /** Số sao đang có; nâng lên {@code sao + 1}. Khoảng 0..MAX_SAO-1. */
        public int sao;
        /** Phần trăm thành công THẬT, cho phép số lẻ (0.25 là một phần bốn trăm). */
        public double tiLe;
        public long vang;
        public int ngoc;
        public boolean bat = true;
        public String ghiChu = "";

        public String moTa() {
            return "★" + sao + " → ★" + (sao + 1);
        }
    }

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `ti_le_sao` ("
            + " `sao` int(11) NOT NULL,"
            + " `ti_le` double NOT NULL DEFAULT 0,"
            + " `vang` bigint(20) NOT NULL DEFAULT 0,"
            + " `ngoc` int(11) NOT NULL DEFAULT 0,"
            + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
            + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (`sao`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    /**
     * Giá trị dựng sẵn, dùng khi bảng còn trống.
     *
     * <p>Bốn cột: sao đang có, phần trăm thật, vàng, ngọc. Tỉ lệ lấy đúng dãy
     * số mà bảng chọn trong game vẫn in ra; vàng và ngọc giữ nguyên bảng cũ.</p>
     */
    private static final double[][] MAC_DINH = {
        //  sao,  %,     vàng,        ngọc
        {0, 70, 5_000_000, 1},
        {1, 50, 10_000_000, 2},
        {2, 30, 20_000_000, 3},
        {3, 20, 40_000_000, 4},
        {4, 10, 60_000_000, 5},
        {5, 5, 90_000_000, 6},
        {6, 0.5, 130_000_000, 7},
        {7, 0.25, 160_000_000, 20},
    };

    private static volatile boolean daTao;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (TiLeSaoDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTao = true;
                gieoNeuTrong();
            } catch (Exception ex) {
                Logger.logException(TiLeSaoDAO.class, ex, "Không tạo được bảng ti_le_sao");
            }
        }
    }

    /**
     * Ghi dãy dựng sẵn nếu bảng chưa có dòng nào.
     *
     * <p>Chỉ chạy khi bảng <b>rỗng hoàn toàn</b>: quản trị viên xoá bớt một bậc
     * là cố ý, không phải để máy chủ tự ghi lại.</p>
     */
    private static void gieoNeuTrong() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) c FROM ti_le_sao");
            if (rs.next() && rs.getInt("c") > 0) {
                return;
            }
        } catch (Exception ex) {
            return;
        } finally {
            dong(rs);
        }
        for (double[] d : MAC_DINH) {
            try {
                ConnectDB.executeUpdate(
                        "INSERT INTO ti_le_sao (sao, ti_le, vang, ngoc, bat, ghi_chu)"
                        + " VALUES (?,?,?,?,1,'')",
                        (int) d[0], d[1], (long) d[2], (int) d[3]);
            } catch (Exception ex) {
                Logger.logException(TiLeSaoDAO.class, ex, "Lỗi gieo ti_le_sao");
            }
        }
        Logger.success("CONFIG", "Đã dựng bảng tỉ lệ nâng sao pha lê");
    }

    /** Bộ đệm: đọc trong luồng game nên không truy vấn mỗi lần đập. */
    private static volatile List<Bac> cache;

    public static List<Bac> danhSach() {
        List<Bac> c = cache;
        if (c != null) {
            return c;
        }
        return napLai();
    }

    public static synchronized List<Bac> napLai() {
        damBaoBang();
        List<Bac> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM ti_le_sao ORDER BY sao");
            while (rs.next()) {
                Bac b = new Bac();
                b.sao = rs.getInt("sao");
                b.tiLe = rs.getDouble("ti_le");
                b.vang = rs.getLong("vang");
                b.ngoc = rs.getInt("ngoc");
                b.bat = rs.getBoolean("bat");
                b.ghiChu = rs.getStringOrNull("ghi_chu");
                if (b.ghiChu == null) {
                    b.ghiChu = "";
                }
                ra.add(b);
            }
        } catch (Exception ex) {
            Logger.logException(TiLeSaoDAO.class, ex, "Lỗi đọc ti_le_sao");
        } finally {
            dong(rs);
        }
        cache = ra;
        return ra;
    }

    /** Bậc nâng từ {@code sao} lên {@code sao + 1}, hoặc {@code null}. */
    public static Bac bac(int sao) {
        for (Bac b : danhSach()) {
            if (b.sao == sao) {
                return b;
            }
        }
        return null;
    }

    /** Phần trăm thật; bậc chưa khai hoặc đang tắt thì {@code 0} — không nâng được. */
    public static double tiLe(int sao) {
        Bac b = bac(sao);
        return (b == null || !b.bat) ? 0 : b.tiLe;
    }

    public static long vang(int sao) {
        Bac b = bac(sao);
        return b == null ? 0 : b.vang;
    }

    public static int ngoc(int sao) {
        Bac b = bac(sao);
        return b == null ? 0 : b.ngoc;
    }

    /** Chuỗi hiện cho người chơi: bỏ số 0 thừa, "0,25%" chứ không "0.25%". */
    public static String tiLeChu(int sao) {
        double t = tiLe(sao);
        String s = (t == Math.floor(t))
                ? String.valueOf((long) t)
                : String.valueOf(t);
        return s.replace('.', ',');
    }

    public static String luu(Bac b) {
        damBaoBang();
        if (b == null) {
            return "Không có dữ liệu.";
        }
        if (b.sao < 0 || b.sao >= MAX_SAO) {
            return "Số sao phải trong khoảng 0 tới " + (MAX_SAO - 1) + ".";
        }
        if (b.tiLe < 0 || b.tiLe > 100) {
            return "Tỉ lệ phải từ 0 tới 100 phần trăm.";
        }
        if (b.vang < 0 || b.ngoc < 0) {
            return "Giá không được âm.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO ti_le_sao (sao, ti_le, vang, ngoc, bat, ghi_chu)"
                    + " VALUES (?,?,?,?,?,?)"
                    + " ON DUPLICATE KEY UPDATE ti_le = VALUES(ti_le),"
                    + " vang = VALUES(vang), ngoc = VALUES(ngoc),"
                    + " bat = VALUES(bat), ghi_chu = VALUES(ghi_chu)",
                    b.sao, b.tiLe, b.vang, b.ngoc, b.bat ? 1 : 0,
                    b.ghiChu == null ? "" : b.ghiChu);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(TiLeSaoDAO.class, ex, "Lỗi lưu ti_le_sao");
            return "Lỗi lưu: " + ex.getMessage();
        }
    }

    /** Trả cả bảng về dãy dựng sẵn. */
    public static String datLaiMacDinh() {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM ti_le_sao");
            daTao = false;
            damBaoBang();
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(TiLeSaoDAO.class, ex, "Lỗi đặt lại ti_le_sao");
            return "Lỗi: " + ex.getMessage();
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
