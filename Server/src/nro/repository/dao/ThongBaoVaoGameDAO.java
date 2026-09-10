package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Thông báo hiện ra khi người chơi vừa vào game — bảng {@code thong_bao_vao_game}.
 *
 * <h2>Vì sao là nhiều dòng chứ không phải một</h2>
 *
 * <p>Trước đây chỉ có <b>một</b> câu, giữ trong quy ước {@code loi_chao_vao_game}.
 * Muốn báo hai việc cùng lúc — sự kiện cuối tuần và lịch bảo trì chẳng hạn — thì
 * phải nhét cả hai vào một đoạn dài, ai vào game cũng đọc đúng đoạn ấy tới lúc có
 * người sửa.</p>
 *
 * <h2>Chạy theo lượt</h2>
 *
 * <p>Người vào game nhận <b>dòng kế tiếp</b> trong danh sách, xoay vòng: người
 * thứ nhất nhận dòng một, người thứ hai nhận dòng hai, hết danh sách thì quay lại
 * đầu. Không phải bốc ngẫu nhiên — bốc ngẫu nhiên thì một dòng có thể xui xẻo
 * không ai thấy, còn xoay vòng thì mọi dòng đều được đọc đều nhau.</p>
 *
 * <p>Bộ đếm nằm trong bộ nhớ, khởi động lại máy chủ là về đầu danh sách. Đó là
 * điều mong muốn: sau khi khởi động lại thì dòng đầu — thường là dòng quan trọng
 * nhất — được đọc trước.</p>
 */
public final class ThongBaoVaoGameDAO {

    private ThongBaoVaoGameDAO() {
    }

    /** Một dòng thông báo. */
    public static final class Dong {

        public int id;
        public String noiDung = "";
        public int thuTu;
        public boolean bat = true;
    }

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `thong_bao_vao_game` ("
            + " `id` int(11) NOT NULL AUTO_INCREMENT,"
            + " `noi_dung` text NOT NULL,"
            + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
            + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
            + " PRIMARY KEY (`id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTao;
    private static volatile List<Dong> CACHE;

    /** Con trỏ xoay vòng, dùng chung cho cả máy chủ. */
    private static final AtomicInteger LUOT = new AtomicInteger();

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (ThongBaoVaoGameDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTao = true;
            } catch (Exception ex) {
                Logger.logException(ThongBaoVaoGameDAO.class, ex,
                        "Không tạo được bảng thong_bao_vao_game");
            }
        }
    }

    /**
     * Toàn bộ danh sách, kể cả dòng đang tắt — panel cần thấy hết.
     *
     * <p>Lần đầu chạy mà bảng còn trống thì <b>chuyển câu cũ</b> trong quy ước
     * {@code loi_chao_vao_game} thành dòng đầu tiên, để máy chủ không đột ngột
     * im lặng sau khi nâng cấp.</p>
     */
    public static List<Dong> danhSach() {
        List<Dong> c = CACHE;
        if (c != null) {
            return c;
        }
        damBaoBang();
        List<Dong> ds = doc();
        if (ds.isEmpty()) {
            String cu = ConfigDAO.chuoi(ConfigDAO.LOI_CHAO);
            if (cu != null && !cu.trim().isEmpty()) {
                Dong d = new Dong();
                d.noiDung = cu.trim();
                d.thuTu = 10;
                d.bat = true;
                luu(d);
                ds = doc();
                Logger.success("CONFIG",
                        "Đã chuyển lời chào cũ thành thông báo vào game");
            }
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
            rs = ConnectDB.executeQuery("SELECT id, noi_dung, thu_tu, bat"
                    + " FROM thong_bao_vao_game ORDER BY thu_tu, id");
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getInt("id");
                d.noiDung = rs.getString("noi_dung");
                d.thuTu = rs.getInt("thu_tu");
                d.bat = rs.getBoolean("bat");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(ThongBaoVaoGameDAO.class, ex,
                    "Lỗi đọc thông báo vào game");
        } finally {
            dong(rs);
        }
        return ds;
    }

    /** Thêm mới nếu {@code id <= 0}, còn lại là sửa dòng sẵn có. */
    public static String luu(Dong d) {
        damBaoBang();
        if (d == null || d.noiDung == null || d.noiDung.trim().isEmpty()) {
            return "Nội dung thông báo không được để trống.";
        }
        try {
            if (d.id > 0) {
                ConnectDB.executeUpdate("UPDATE thong_bao_vao_game SET noi_dung = ?,"
                        + " thu_tu = ?, bat = ? WHERE id = ?",
                        d.noiDung.trim(), d.thuTu, d.bat ? 1 : 0, d.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO thong_bao_vao_game"
                        + " (noi_dung, thu_tu, bat) VALUES (?,?,?)",
                        d.noiDung.trim(), d.thuTu, d.bat ? 1 : 0);
            }
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(ThongBaoVaoGameDAO.class, ex,
                    "Lỗi lưu thông báo vào game");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM thong_bao_vao_game WHERE id = ?", id);
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(ThongBaoVaoGameDAO.class, ex,
                    "Lỗi xoá thông báo vào game");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Thông báo dành cho người vừa vào game — dòng kế tiếp theo lượt.
     *
     * @return {@code null} khi không có dòng nào đang bật, tức là không hiện gì
     */
    public static String choNguoiVuaVao() {
        List<Dong> bat = new ArrayList<>();
        for (Dong d : danhSach()) {
            if (d.bat && d.noiDung != null && !d.noiDung.trim().isEmpty()) {
                bat.add(d);
            }
        }
        if (bat.isEmpty()) {
            return null;
        }
        // Lay phan du cua mot so KHONG AM: getAndIncrement tran qua so am sau
        // hai ti luot, va Math.abs cua Integer.MIN_VALUE van la so am.
        int i = (LUOT.getAndIncrement() % bat.size() + bat.size()) % bat.size();
        return bat.get(i).noiDung.trim();
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
