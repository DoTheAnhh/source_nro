package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Trọng số bốc <b>bậc đồ</b> khi nâng Huỷ Diệt thành đồ Set Kích Hoạt.
 *
 * <h2>Bậc là gì</h2>
 *
 * <p>{@code Manager.doSKHVip[hành tinh][ô]} là một dãy <b>bảy</b> mẫu đồ xếp từ
 * thấp lên cao cho mỗi ô trang bị của mỗi hành tinh. Bậc ở đây chính là chỉ số
 * trong dãy ấy: bậc 0 là mẫu thấp nhất, bậc 6 là mẫu cao nhất — Lưỡng Long,
 * Jeancalic, Vàng Zealot Tướng tuỳ hành tinh.</p>
 *
 * <h2>Trọng số, không phải phần trăm</h2>
 *
 * <p>Bốc theo trọng số nên không cần cộng cho tròn một trăm: sửa một dòng không
 * bắt phải sửa lại các dòng khác. Đặt {@code 0} hoặc tắt là bậc đó
 * <b>không bao giờ ra</b>.</p>
 *
 * <p>Dãy dựng sẵn giảm mạnh dần theo bậc, đúng yêu cầu "đồ càng cấp cao tỉ lệ
 * càng thấp".</p>
 */
public final class TiLeKichHoatDAO {

    private TiLeKichHoatDAO() {
    }

    /** Số bậc, khớp độ dài các dãy trong {@code Manager.doSKHVip}. */
    public static final int SO_BAC = 7;

    public static final class Bac {

        public int bac;
        public long trongSo;
        public boolean bat = true;
        public String ghiChu = "";
    }

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `ti_le_kich_hoat` ("
            + " `bac` int(11) NOT NULL,"
            + " `trong_so` bigint(20) NOT NULL DEFAULT 0,"
            + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
            + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (`bac`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    /** Trọng số dựng sẵn: bậc càng cao càng hiếm. */
    private static final long[] MAC_DINH = {4000, 2500, 1500, 900, 600, 350, 150};

    private static final String[] GHI_CHU_MAC_DINH = {
        "Bậc thấp nhất", "", "", "", "", "Áp chót",
        "Cao nhất — Lưỡng Long / Jeancalic / Vàng Zealot Tướng"
    };

    private static volatile boolean daTao;
    private static volatile List<Bac> cache;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (TiLeKichHoatDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTao = true;
                gieoNeuTrong();
            } catch (Exception ex) {
                Logger.logException(TiLeKichHoatDAO.class, ex,
                        "Không tạo được bảng ti_le_kich_hoat");
            }
        }
    }

    private static void gieoNeuTrong() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) c FROM ti_le_kich_hoat");
            if (rs.next() && rs.getInt("c") > 0) {
                return;
            }
        } catch (Exception ex) {
            return;
        } finally {
            dong(rs);
        }
        for (int i = 0; i < SO_BAC; i++) {
            try {
                ConnectDB.executeUpdate(
                        "INSERT INTO ti_le_kich_hoat (bac, trong_so, bat, ghi_chu)"
                        + " VALUES (?,?,1,?)", i, MAC_DINH[i], GHI_CHU_MAC_DINH[i]);
            } catch (Exception ex) {
                Logger.logException(TiLeKichHoatDAO.class, ex, "Lỗi gieo ti_le_kich_hoat");
            }
        }
        Logger.success("CONFIG", "Đã dựng bảng tỉ lệ bậc đồ set kích hoạt");
    }

    public static List<Bac> danhSach() {
        List<Bac> c = cache;
        return c != null ? c : napLai();
    }

    public static synchronized List<Bac> napLai() {
        damBaoBang();
        List<Bac> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM ti_le_kich_hoat ORDER BY bac");
            while (rs.next()) {
                Bac b = new Bac();
                b.bac = rs.getInt("bac");
                b.trongSo = rs.getLong("trong_so");
                b.bat = rs.getBoolean("bat");
                b.ghiChu = rs.getStringOrNull("ghi_chu");
                if (b.ghiChu == null) {
                    b.ghiChu = "";
                }
                ra.add(b);
            }
        } catch (Exception ex) {
            Logger.logException(TiLeKichHoatDAO.class, ex, "Lỗi đọc ti_le_kich_hoat");
        } finally {
            dong(rs);
        }
        cache = ra;
        return ra;
    }

    /**
     * Bốc một bậc theo trọng số.
     *
     * @return bậc trong khoảng 0..{@link #SO_BAC}-1; mọi bậc đều tắt thì trả 0
     */
    public static int bocBac() {
        List<Bac> ds = danhSach();
        long tong = 0;
        for (Bac b : ds) {
            if (b.bat && b.trongSo > 0 && b.bac >= 0 && b.bac < SO_BAC) {
                tong += b.trongSo;
            }
        }
        if (tong <= 0) {
            return 0;
        }
        long diem = (long) (Util.nextDouble(tong));
        for (Bac b : ds) {
            if (!b.bat || b.trongSo <= 0 || b.bac < 0 || b.bac >= SO_BAC) {
                continue;
            }
            diem -= b.trongSo;
            if (diem < 0) {
                return b.bac;
            }
        }
        return 0;
    }

    /** Phần trăm thật của một bậc, để hiện trên panel. */
    public static double phanTram(int bac) {
        long tong = 0;
        long cua = 0;
        for (Bac b : danhSach()) {
            if (b.bat && b.trongSo > 0) {
                tong += b.trongSo;
                if (b.bac == bac) {
                    cua = b.trongSo;
                }
            }
        }
        return tong <= 0 ? 0 : (cua * 100.0 / tong);
    }

    public static String luu(Bac b) {
        damBaoBang();
        if (b == null) {
            return "Không có dữ liệu.";
        }
        if (b.bac < 0 || b.bac >= SO_BAC) {
            return "Bậc phải trong khoảng 0 tới " + (SO_BAC - 1) + ".";
        }
        if (b.trongSo < 0) {
            return "Trọng số không được âm.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO ti_le_kich_hoat (bac, trong_so, bat, ghi_chu)"
                    + " VALUES (?,?,?,?)"
                    + " ON DUPLICATE KEY UPDATE trong_so = VALUES(trong_so),"
                    + " bat = VALUES(bat), ghi_chu = VALUES(ghi_chu)",
                    b.bac, b.trongSo, b.bat ? 1 : 0,
                    b.ghiChu == null ? "" : b.ghiChu);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(TiLeKichHoatDAO.class, ex, "Lỗi lưu ti_le_kich_hoat");
            return "Lỗi lưu: " + ex.getMessage();
        }
    }

    public static String datLaiMacDinh() {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM ti_le_kich_hoat");
            daTao = false;
            damBaoBang();
            napLai();
            return null;
        } catch (Exception ex) {
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
