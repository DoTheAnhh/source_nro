package nro.repository.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Cấu hình, mốc quà và lịch sử của trò <b>Đập Saibaman</b> — tất cả nằm trong
 * cơ sở dữ liệu, không viết cứng trong mã.
 *
 * <h2>Hai bảng</h2>
 *
 * <ul>
 *   <li><code>mg_saibaman_cau_hinh(khoa, gia_tri, mo_ta)</code> — giá vé, số lượt
 *       mỗi ngày, độ dài một ván, điểm từng loại. Thiếu khoá nào thì tự chèn giá
 *       trị mặc định lúc máy chủ chạy, có sẵn mô tả để admin sửa tay.</li>
 *   <li><code>mg_saibaman_moc(id, diem, item_id, so_luong)</code> — mỗi dòng là
 *       <b>một món quà của một mốc</b>. Các dòng cùng <code>diem</code> gộp thành
 *       một mốc. Bảng rỗng thì chèn bộ mốc mặc định đúng một lần.</li>
 * </ul>
 *
 * <p>Lịch sử từng ván ghi chung bảng <code>mg_cuoc</code> của mọi trò (mã trò 6):
 * cột <code>ket_qua</code> là điểm, <code>cua</code> là thứ tự mốc đạt được
 * (-1 là chưa tới mốc nào), <code>tien_thang</code> là số thỏi trả về.</p>
 *
 * <p>Đọc lại mỗi {@link #HAN_BO_NHO_MS}: admin sửa bảng thì tối đa nửa phút sau
 * ván mới đã theo số mới, không phải khởi động lại máy chủ.</p>
 */
public final class DapSaibamanDAO {

    private DapSaibamanDAO() {
    }

    /** Mã trò, trùng {@code MiniGameService.TRO_DAP_SAIBAMAN}. */
    public static final int TRO = 6;

    private static final long HAN_BO_NHO_MS = 30_000L;

    /** Một món quà trong một mốc. */
    public static final class Qua {
        public final int itemId;
        public final int soLuong;

        Qua(int itemId, int soLuong) {
            this.itemId = itemId;
            this.soLuong = soLuong;
        }
    }

    /** Một mốc: đạt từ {@code diem} trở lên thì nhận cả danh sách quà. */
    public static final class Moc {
        public final int diem;
        public final List<Qua> qua = new ArrayList<>();

        Moc(int diem) {
            this.diem = diem;
        }
    }

    /**
     * Giá trị mặc định, và <b>chỉ</b> dùng để chèn vào bảng lần đầu.
     *
     * <p>Mọi chỗ đọc cấu hình đều đi qua {@link #so} — đọc từ bảng.</p>
     */
    private static final Object[][] MAC_DINH = {
        { "ve", 2L, "Giá vé một ván (thỏi vàng). Thỏi thưởng mỗi ván luôn bị chặn ở ve - 1." },
        { "luot_ngay", 20L, "Số ván tối đa mỗi nhân vật mỗi ngày." },
        { "giay", 30L, "Độ dài một ván (giây), tối đa 60." },
        { "diem_thuong", 10L, "Điểm khi đập trúng Saibaman thường." },
        { "diem_vang", 30L, "Điểm khi đập trúng Saibaman vàng (hiếm, lặn nhanh)." },
        { "diem_bulma", -30L, "Điểm khi lỡ đập trúng Bulma (số âm là trừ)." },
        { "o_trong", 5L, "Số ô hành trang phải trống trước khi vào ván." }
    };

    /**
     * Mốc mặc định: { điểm, mã vật phẩm, số lượng }.
     *
     * <p>381 Cuồng nộ, 382 Bổ huyết, 383 Bổ khí, 384 Giáp Xên, 457 thỏi vàng.
     * Thỏi vàng chỉ có ở mốc cao nhất và chỉ 1 thỏi — giá vé 2 thỏi, nên kể cả
     * đạt mốc cao nhất mỗi ván vẫn tiêu hết ít nhất 1 thỏi: trò này rút thỏi vàng
     * ra khỏi máy chủ chứ không in thêm.</p>
     */
    private static final int[][] MOC_MAC_DINH = {
        { 100, 382, 1 },
        { 180, 382, 1 }, { 180, 383, 1 },
        { 260, 381, 1 }, { 260, 382, 1 }, { 260, 383, 1 },
        { 340, 381, 1 }, { 340, 384, 1 }, { 340, 382, 1 }, { 340, 383, 1 },
        { 420, 381, 2 }, { 420, 384, 1 }, { 420, 382, 2 }, { 420, 383, 2 },
        { 420, 457, 1 }
    };

    private static boolean daTaoBang;
    private static long mocDoc;
    private static Map<String, Long> cauHinh = new HashMap<>();
    private static List<Moc> dsMoc = new ArrayList<>();

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        MiniGameDAO.damBaoBang();
        CrisResultSet rs = null;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mg_saibaman_cau_hinh ("
                    + " khoa VARCHAR(32) NOT NULL,"
                    + " gia_tri BIGINT(20) NOT NULL DEFAULT 0,"
                    + " mo_ta VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (khoa)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mg_saibaman_moc ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " diem INT(11) NOT NULL DEFAULT 0,"
                    + " item_id INT(11) NOT NULL DEFAULT 0,"
                    + " so_luong INT(11) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id),"
                    + " KEY idx_diem (diem)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            for (Object[] d : MAC_DINH) {
                // INSERT IGNORE: khoa da co (admin da sua) thi giu nguyen.
                ConnectDB.executeUpdate("INSERT IGNORE INTO mg_saibaman_cau_hinh"
                        + " (khoa, gia_tri, mo_ta) VALUES (?, ?, ?)", d[0], d[1], d[2]);
            }
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM mg_saibaman_moc");
            if (rs.next() && rs.getInt("n") == 0) {
                for (int[] m : MOC_MAC_DINH) {
                    ConnectDB.executeUpdate("INSERT INTO mg_saibaman_moc"
                            + " (diem, item_id, so_luong) VALUES (?, ?, ?)", m[0], m[1], m[2]);
                }
            }
            daTaoBang = true;
        } catch (Exception ex) {
            Logger.logException(DapSaibamanDAO.class, ex, "Loi tao bang Dap Saibaman");
        } finally {
            dong(rs);
        }
    }

    /** Đọc lại cấu hình và mốc nếu bản nhớ đã cũ. */
    private static synchronized void docNeuCu() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - mocDoc < HAN_BO_NHO_MS && !dsMoc.isEmpty()) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            Map<String, Long> ch = new HashMap<>();
            rs = ConnectDB.executeQuery("SELECT khoa, gia_tri FROM mg_saibaman_cau_hinh");
            while (rs.next()) {
                ch.put(rs.getString("khoa"), rs.getLong("gia_tri"));
            }
            dong(rs);
            rs = null;
            rs = ConnectDB.executeQuery("SELECT diem, item_id, so_luong FROM mg_saibaman_moc"
                    + " ORDER BY diem ASC, id ASC");
            List<Moc> ds = new ArrayList<>();
            Moc cuoi = null;
            while (rs.next()) {
                int diem = rs.getInt("diem");
                if (cuoi == null || cuoi.diem != diem) {
                    cuoi = new Moc(diem);
                    ds.add(cuoi);
                }
                int sl = rs.getInt("so_luong");
                if (sl > 0) {
                    cuoi.qua.add(new Qua(rs.getInt("item_id"), sl));
                }
            }
            cauHinh = ch;
            dsMoc = ds;
            mocDoc = bayGio;
        } catch (Exception ex) {
            Logger.logException(DapSaibamanDAO.class, ex, "Loi doc cau hinh Dap Saibaman");
            // Giu ban cu; lan sau thu lai.
            mocDoc = bayGio;
        } finally {
            dong(rs);
        }
    }

    /** Một số cấu hình; thiếu trong bảng thì lấy mặc định. */
    public static long so(String khoa) {
        docNeuCu();
        Long v = cauHinh.get(khoa);
        if (v != null) {
            return v;
        }
        for (Object[] d : MAC_DINH) {
            if (d[0].equals(khoa)) {
                return (Long) d[1];
            }
        }
        return 0;
    }

    /** Danh sách mốc, điểm tăng dần. */
    public static List<Moc> moc() {
        docNeuCu();
        return dsMoc;
    }

    /** Số ván đã chơi hôm nay (tính cả ván bỏ dở — vé đã trừ). */
    public static int soVanHomNay(long playerId) {
        MiniGameDAO.damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM mg_cuoc"
                    + " WHERE player_id = ? AND tro = ? AND luc >= ?",
                    playerId, TRO, dauNgay());
            if (rs.next()) {
                return rs.getInt("n");
            }
        } catch (Exception ex) {
            Logger.logException(DapSaibamanDAO.class, ex);
        } finally {
            dong(rs);
        }
        return 0;
    }

    /** Số ván lớn nhất đã ghi, để đánh số ván tiếp. */
    public static long vanLonNhat() {
        MiniGameDAO.damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(phien) AS m FROM mg_cuoc WHERE tro = ?", TRO);
            if (rs.next()) {
                return rs.getLong("m");
            }
        } catch (Exception ex) {
            Logger.logException(DapSaibamanDAO.class, ex);
        } finally {
            dong(rs);
        }
        return 0;
    }

    /** Một dòng lịch sử / bảng xếp hạng. */
    public static final class Dong {
        public long van;
        public String ten;
        public int diem;
        public int moc;
        public long thoi;
        public long luc;
    }

    /** 30 ván gần nhất của một người. */
    public static List<Dong> lichSuCuaToi(long playerId) {
        MiniGameDAO.damBaoBang();
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT phien, ten, ket_qua, cua, tien_thang, luc"
                    + " FROM mg_cuoc WHERE player_id = ? AND tro = ? ORDER BY id DESC LIMIT 30",
                    playerId, TRO);
            while (rs.next()) {
                Dong d = new Dong();
                d.van = rs.getLong("phien");
                d.ten = rs.getString("ten");
                d.diem = rs.getInt("ket_qua");
                d.moc = rs.getInt("cua");
                d.thoi = rs.getLong("tien_thang");
                d.luc = rs.getLong("luc");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(DapSaibamanDAO.class, ex);
        } finally {
            dong(rs);
        }
        return ds;
    }

    /** Mười điểm cao nhất hôm nay, mỗi nhân vật một dòng (lấy ván cao nhất). */
    public static List<Dong> topHomNay() {
        MiniGameDAO.damBaoBang();
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT player_id, MAX(ten) AS ten,"
                    + " MAX(ket_qua) AS diem, COUNT(*) AS so_van"
                    + " FROM mg_cuoc WHERE tro = ? AND luc >= ?"
                    + " GROUP BY player_id ORDER BY diem DESC LIMIT 10",
                    TRO, dauNgay());
            while (rs.next()) {
                Dong d = new Dong();
                d.ten = rs.getString("ten");
                d.diem = rs.getInt("diem");
                // Cot moc cua bang xep hang chua so van hom nay.
                d.moc = rs.getInt("so_van");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(DapSaibamanDAO.class, ex);
        } finally {
            dong(rs);
        }
        return ds;
    }

    /** Mốc 0 giờ hôm nay theo giờ máy chủ, mili giây. */
    private static long dauNgay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
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
