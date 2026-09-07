package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Kho lịch sử dùng chung cho <b>khu trò chơi nhỏ</b>.
 *
 * <h2>Hai bảng cho sáu trò, không phải mười hai bảng</h2>
 *
 * <ul>
 *   <li>{@code mg_phien} — mỗi ván một dòng, cột {@code tro} cho biết của trò
 *       nào. Ba cột {@code kq1..kq3} đủ chứa kết quả của mọi trò ở đây: ba con
 *       xúc xắc của Bầu Cua, bốn đồng xu gói thành một số của Xóc Đĩa, số thứ tự
 *       con ngựa thắng của Đua Ngựa.</li>
 *   <li>{@code mg_cuoc} — mỗi lượt đặt một dòng: ai, ván nào, cửa nào, bao nhiêu
 *       thỏi, thắng thua bao nhiêu.</li>
 * </ul>
 *
 * <p>Gộp lại thay vì tách theo trò vì ba lý do. Thêm một trò thì không phải thêm
 * bảng. Bảng quản lý bên panel đọc <i>một</i> chỗ là thấy hết. Và câu hỏi hay
 * gặp nhất — "người này hôm nay được thua bao nhiêu" — chỉ là một câu lệnh chứ
 * không phải sáu câu rồi cộng tay.</p>
 *
 * <h2>Ghi nền, không chặn vòng chơi</h2>
 *
 * <p>Hàm ghi được gọi ngay lúc chốt ván. Cơ sở dữ liệu chậm thì cả bàn đứng
 * theo, nên chỗ gọi đẩy sang luồng riêng — ở đây chỉ lo đúng câu lệnh.</p>
 */
public final class MiniGameDAO {

    /** Số dòng lịch sử trả về mỗi lần hỏi. Đi trong một byte nên trần là 255. */
    public static final int SO_DONG_LICH_SU = 60;

    private static boolean daTaoBang;

    private MiniGameDAO() {
    }

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mg_phien ("
                    + " id BIGINT(20) NOT NULL AUTO_INCREMENT,"
                    + " tro TINYINT(4) NOT NULL DEFAULT 0,"
                    + " phien BIGINT(20) NOT NULL DEFAULT 0,"
                    + " kq1 INT(11) NOT NULL DEFAULT 0,"
                    + " kq2 INT(11) NOT NULL DEFAULT 0,"
                    + " kq3 INT(11) NOT NULL DEFAULT 0,"
                    + " ket_qua INT(11) NOT NULL DEFAULT 0,"
                    + " tong_cuoc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " tong_tra BIGINT(20) NOT NULL DEFAULT 0,"
                    + " so_nguoi INT(11) NOT NULL DEFAULT 0,"
                    + " luc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (id),"
                    + " KEY idx_tro_phien (tro, phien)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mg_cuoc ("
                    + " id BIGINT(20) NOT NULL AUTO_INCREMENT,"
                    + " tro TINYINT(4) NOT NULL DEFAULT 0,"
                    + " phien BIGINT(20) NOT NULL DEFAULT 0,"
                    + " player_id BIGINT(20) NOT NULL DEFAULT 0,"
                    + " ten VARCHAR(80) DEFAULT NULL,"
                    + " cua INT(11) NOT NULL DEFAULT 0,"
                    + " so_thoi BIGINT(20) NOT NULL DEFAULT 0,"
                    + " ket_qua INT(11) NOT NULL DEFAULT 0,"
                    + " thang TINYINT(1) NOT NULL DEFAULT 0,"
                    + " tien_thang BIGINT(20) NOT NULL DEFAULT 0,"
                    + " luc BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (id),"
                    + " KEY idx_nguoi (player_id, id),"
                    + " KEY idx_tro (tro, id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            daTaoBang = true;
        } catch (Exception ex) {
            Logger.logException(MiniGameDAO.class, ex, "Loi tao bang mini game");
        }
    }

    /** Số phiên lớn nhất đã ghi của một trò, để đếm tiếp chứ không quay về 1. */
    public static long phienLonNhat(int tro) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT MAX(phien) AS m FROM mg_phien WHERE tro = ?", tro);
            if (rs.next()) {
                return rs.getLong("m");
            }
        } catch (Exception ex) {
            Logger.logException(MiniGameDAO.class, ex);
        } finally {
            dong(rs);
        }
        return 0;
    }

    public static void ghiPhien(int tro, long phien, int kq1, int kq2, int kq3,
            int ketQua, long tongCuoc, long tongTra, int soNguoi) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO mg_phien"
                    + " (tro, phien, kq1, kq2, kq3, ket_qua, tong_cuoc,"
                    + " tong_tra, so_nguoi, luc)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?)",
                    tro, phien, kq1, kq2, kq3, ketQua, tongCuoc, tongTra,
                    soNguoi, System.currentTimeMillis());
        } catch (Exception ex) {
            Logger.logException(MiniGameDAO.class, ex);
        }
    }

    public static void ghiCuoc(int tro, long phien, long playerId, String ten,
            int cua, long soThoi, int ketQua, boolean thang, long tienThang) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO mg_cuoc"
                    + " (tro, phien, player_id, ten, cua, so_thoi, ket_qua,"
                    + " thang, tien_thang, luc)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?)",
                    tro, phien, playerId, ten, cua, soThoi, ketQua,
                    thang ? 1 : 0, tienThang, System.currentTimeMillis());
        } catch (Exception ex) {
            Logger.logException(MiniGameDAO.class, ex);
        }
    }

    /** Một dòng trong màn "Lịch sử của tôi". */
    public static class DongCuoc {
        public long phien;
        public int cua;
        public long soThoi;
        public int ketQua;
        public boolean thang;
        public long tienThang;
    }

    /** Một dòng trong màn "Lịch sử máy chủ". */
    public static class DongPhien {
        public long phien;
        public int kq1;
        public int kq2;
        public int kq3;
        public int ketQua;
        public long tongCuoc;
        public long tongTra;
        public int soNguoi;
    }

    public static List<DongCuoc> lichSuCuaToi(int tro, long playerId) {
        damBaoBang();
        List<DongCuoc> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM mg_cuoc"
                    + " WHERE tro = ? AND player_id = ? ORDER BY id DESC LIMIT "
                    + SO_DONG_LICH_SU, tro, playerId);
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
            Logger.logException(MiniGameDAO.class, ex);
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static List<DongPhien> lichSuServer(int tro) {
        damBaoBang();
        List<DongPhien> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM mg_phien"
                    + " WHERE tro = ? ORDER BY id DESC LIMIT "
                    + SO_DONG_LICH_SU, tro);
            while (rs.next()) {
                DongPhien d = new DongPhien();
                d.phien = rs.getLong("phien");
                d.kq1 = rs.getInt("kq1");
                d.kq2 = rs.getInt("kq2");
                d.kq3 = rs.getInt("kq3");
                d.ketQua = rs.getInt("ket_qua");
                d.tongCuoc = rs.getLong("tong_cuoc");
                d.tongTra = rs.getLong("tong_tra");
                d.soNguoi = rs.getInt("so_nguoi");
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(MiniGameDAO.class, ex);
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
