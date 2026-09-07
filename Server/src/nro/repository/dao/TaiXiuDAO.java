package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Kho lịch sử <b>Tài Xỉu</b>.
 *
 * <h2>Hai bảng</h2>
 *
 * <ul>
 *   <li>{@code taixiu_phien} — mỗi ván một dòng: ba con xúc xắc, kết quả, tổng
 *       cược hai cửa. Đây là lịch sử chung của cả máy chủ.</li>
 *   <li>{@code taixiu_cuoc} — mỗi lượt đặt một dòng: ai, ván nào, cửa nào, bao
 *       nhiêu thỏi, thắng hay thua. Lịch sử riêng của từng người lọc ra từ
 *       bảng này.</li>
 * </ul>
 *
 * <h2>Vì sao ghi xuống cơ sở dữ liệu chứ không giữ trong bộ nhớ</h2>
 *
 * <p>Đây là ăn thua bằng tài sản trong game. Giữ trong bộ nhớ thì máy chủ dựng
 * lại một cái là mất sạch, và khi người chơi khiếu nại "ván đó tôi thắng mà
 * không được trả" thì không có gì để đối chiếu.</p>
 *
 * <h2>Ghi nền, không chặn vòng chơi</h2>
 *
 * <p>Hàm ghi gọi từ luồng Tài Xỉu ngay lúc chốt ván. Nếu cơ sở dữ liệu chậm thì
 * cả bàn đứng theo — nên chỗ gọi đẩy sang luồng riêng, còn ở đây chỉ lo đúng
 * câu lệnh.</p>
 */
public class TaiXiuDAO {

    /**
     * Số ván gần nhất trả về cho màn lịch sử.
     *
     * <p>Sáu chục chứ không phải ba chục: màn "Cầu" xếp kết quả thành cột theo
     * chuỗi thắng liên tiếp, ba chục ván chỉ ra hơn chục cột — chưa đủ để nhìn
     * thấy dạng cầu nào cả. Số này đi trong một byte nên trần là 255.</p>
     */
    public static final int SO_DONG_LICH_SU = 60;

    private static boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS taixiu_phien ("
                    + " phien BIGINT(20) NOT NULL,"
                    + " x1 TINYINT(4) NOT NULL DEFAULT 0,"
                    + " x2 TINYINT(4) NOT NULL DEFAULT 0,"
                    + " x3 TINYINT(4) NOT NULL DEFAULT 0,"
                    + " tong INT(11) NOT NULL DEFAULT 0,"
                    + " ket_qua TINYINT(4) NOT NULL DEFAULT 0,"
                    + " tong_tai BIGINT(20) NOT NULL DEFAULT 0,"
                    + " tong_xiu BIGINT(20) NOT NULL DEFAULT 0,"
                    + " so_nguoi INT(11) NOT NULL DEFAULT 0,"
                    + " luc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (phien)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS taixiu_cuoc ("
                    + " id BIGINT(20) NOT NULL AUTO_INCREMENT,"
                    + " phien BIGINT(20) NOT NULL,"
                    + " player_id BIGINT(20) NOT NULL,"
                    + " ten VARCHAR(80) DEFAULT NULL,"
                    + " cua TINYINT(4) NOT NULL DEFAULT 0,"
                    + " so_thoi BIGINT(20) NOT NULL DEFAULT 0,"
                    + " ket_qua TINYINT(4) NOT NULL DEFAULT 0,"
                    + " thang TINYINT(1) NOT NULL DEFAULT 0,"
                    + " tien_thang BIGINT(20) NOT NULL DEFAULT 0,"
                    + " luc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (id),"
                    + " KEY idx_nguoi (player_id, id),"
                    + " KEY idx_phien (phien)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            daTaoBang = true;
        } catch (Exception ex) {
            Logger.logException(TaiXiuDAO.class, ex, "Lỗi tạo bảng Tài Xỉu");
        }
    }

    /**
     * Số ván lớn nhất đã ghi.
     *
     * <p>Máy chủ dựng lại thì đếm tiếp từ đây chứ không quay về 1 — hai ván khác
     * nhau mang cùng một số thì lịch sử đọc ra vô nghĩa, mà khoá chính cũng
     * đụng nhau.</p>
     */
    public static long phienLonNhat() {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(phien) AS m FROM taixiu_phien");
            if (rs.next()) {
                return rs.getLong("m");
            }
        } catch (Exception ex) {
            Logger.logException(TaiXiuDAO.class, ex, "Lỗi đọc phiên lớn nhất");
        } finally {
            dong(rs);
        }
        return 0;
    }

    public static void ghiPhien(long phien, int x1, int x2, int x3, int ketQua,
            long tongTai, long tongXiu, int soNguoi) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO taixiu_phien"
                    + " (phien, x1, x2, x3, tong, ket_qua, tong_tai, tong_xiu,"
                    + "  so_nguoi, luc)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?)",
                    phien, x1, x2, x3, x1 + x2 + x3, ketQua, tongTai, tongXiu,
                    soNguoi, System.currentTimeMillis());
        } catch (Exception ex) {
            Logger.logException(TaiXiuDAO.class, ex, "Lỗi ghi phiên Tài Xỉu");
        }
    }

    public static void ghiCuoc(long phien, long playerId, String ten, int cua,
            long soThoi, int ketQua, boolean thang, long tienThang) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO taixiu_cuoc"
                    + " (phien, player_id, ten, cua, so_thoi, ket_qua, thang,"
                    + "  tien_thang, luc)"
                    + " VALUES (?,?,?,?,?,?,?,?,?)",
                    phien, playerId, ten, cua, soThoi, ketQua, thang ? 1 : 0,
                    tienThang, System.currentTimeMillis());
        } catch (Exception ex) {
            Logger.logException(TaiXiuDAO.class, ex, "Lỗi ghi cược Tài Xỉu");
        }
    }

    /** Một dòng lịch sử ván — dùng cho cả hai màn lịch sử. */
    public static class DongPhien {
        public long phien;
        public int x1;
        public int x2;
        public int x3;
        public int ketQua;
        public long tongTai;
        public long tongXiu;
        public int soNguoi;
    }

    /** Một dòng lịch sử cược của bản thân. */
    public static class DongCuoc {
        public long phien;
        public int cua;
        public long soThoi;
        public int ketQua;
        public boolean thang;
        public long tienThang;
    }

    public static List<DongPhien> lichSuServer() {
        damBaoBang();
        List<DongPhien> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM taixiu_phien"
                    + " ORDER BY phien DESC LIMIT " + SO_DONG_LICH_SU);
            while (rs.next()) {
                DongPhien d = new DongPhien();
                d.phien = rs.getLong("phien");
                d.x1 = rs.getInt("x1");
                d.x2 = rs.getInt("x2");
                d.x3 = rs.getInt("x3");
                d.ketQua = rs.getInt("ket_qua");
                d.tongTai = rs.getLong("tong_tai");
                d.tongXiu = rs.getLong("tong_xiu");
                d.soNguoi = rs.getInt("so_nguoi");
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(TaiXiuDAO.class, ex, "Lỗi đọc lịch sử máy chủ");
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static List<DongCuoc> lichSuCuaToi(long playerId) {
        damBaoBang();
        List<DongCuoc> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM taixiu_cuoc"
                    + " WHERE player_id = ? ORDER BY id DESC LIMIT "
                    + SO_DONG_LICH_SU, playerId);
            while (rs.next()) {
                DongCuoc d = new DongCuoc();
                d.phien = rs.getLong("phien");
                d.cua = rs.getInt("cua");
                d.soThoi = rs.getLong("so_thoi");
                d.ketQua = rs.getInt("ket_qua");
                d.thang = rs.getBoolean("thang");
                d.tienThang = rs.getLong("tien_thang");
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(TaiXiuDAO.class, ex, "Lỗi đọc lịch sử người chơi");
        } finally {
            dong(rs);
        }
        return ra;
    }

    private static void dong(CrisResultSet rs) {
        try {
            if (rs != null) {
                rs.dispose();
            }
        } catch (Exception ignored) {
            // Dong that bai thi cung khong lam gi duoc them.
        }
    }
}
