package nro.repository.dao;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Số vàng mỗi tài khoản đã <b>giao dịch</b> trong ngày, và giới hạn của nó.
 *
 * <h2>Tính cả hai chiều</h2>
 *
 * <p>Đưa đi và nhận về đều trừ vào cùng một hạn mức. Chỉ tính chiều đưa đi thì
 * một người lập trăm nick phụ, mỗi nick đưa đủ hạn mức, là nick chính nhận về
 * gấp trăm lần — đúng cái lỗ cần chặn.</p>
 *
 * <h2>Nằm trong cơ sở dữ liệu</h2>
 *
 * <p>Giữ trong bộ nhớ thì khởi động lại máy chủ là hạn mức về 0. Bảng
 * {@code gd_vang_ngay} tự tạo, mỗi tài khoản mỗi ngày một dòng. Có một bản sao
 * trong bộ nhớ để lần kiểm tra lúc đặt vàng vào khung giao dịch không phải hỏi
 * cơ sở dữ liệu.</p>
 */
public final class GiaoDichVangDAO {

    /** Hạn mức vàng giao dịch mỗi ngày của một tài khoản, cả đưa lẫn nhận. */
    public static final long GIOI_HAN_NGAY = 200_000_000L;

    private static boolean daTaoBang;

    /** player_id → {ngày dạng yyyymmdd, tổng đã dùng}. */
    private static final Map<Long, long[]> BO_NHO = new ConcurrentHashMap<>();

    private GiaoDichVangDAO() {
    }

    private static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS gd_vang_ngay ("
                    + " player_id BIGINT(20) NOT NULL,"
                    + " ngay INT(11) NOT NULL,"
                    + " tong BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id, ngay)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            daTaoBang = true;
        } catch (Exception ex) {
            Logger.logException(GiaoDichVangDAO.class, ex,
                    "Khong tao duoc bang gd_vang_ngay");
        }
    }

    private static int homNay() {
        LocalDate d = LocalDate.now();
        return d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
    }

    /** Số vàng tài khoản đã giao dịch hôm nay. */
    public static long daDung(long playerId) {
        int ngay = homNay();
        long[] o = BO_NHO.get(playerId);
        if (o != null && o[0] == ngay) {
            return o[1];
        }
        damBaoBang();
        long tong = 0;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT tong FROM gd_vang_ngay WHERE player_id = ? AND ngay = ?",
                    playerId, ngay);
            if (rs.next()) {
                tong = rs.getLong("tong");
            }
        } catch (Exception ex) {
            Logger.logException(GiaoDichVangDAO.class, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                    // Dong that bai thi cung khong lam gi duoc them.
                }
            }
        }
        BO_NHO.put(playerId, new long[] { ngay, tong });
        return tong;
    }

    /** Hạn mức còn lại hôm nay. */
    public static long conLai(long playerId) {
        long con = GIOI_HAN_NGAY - daDung(playerId);
        return con < 0 ? 0 : con;
    }

    /** Cộng thêm số vàng vừa giao dịch xong vào hạn mức hôm nay. */
    public static void cong(long playerId, long soVang) {
        if (soVang <= 0) {
            return;
        }
        int ngay = homNay();
        long moi = daDung(playerId) + soVang;
        BO_NHO.put(playerId, new long[] { ngay, moi });
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO gd_vang_ngay (player_id, ngay, tong)"
                    + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE tong = tong + ?",
                    playerId, ngay, soVang, soVang);
        } catch (Exception ex) {
            Logger.logException(GiaoDichVangDAO.class, ex);
        }
    }
}
