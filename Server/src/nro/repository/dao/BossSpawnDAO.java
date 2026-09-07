package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Danh sách boss được dựng lúc máy chủ khởi động, lưu ở bảng
 * {@code boss_spawn}.
 *
 * <h2>Trước đây</h2>
 *
 * <p>{@code BossManager.loadBoss()} là 33 dòng {@code createBoss(...)} gõ
 * cứng. Muốn thêm một con Black nữa, hay tắt tạm một con đang lỗi, đều phải
 * sửa mã và biên dịch lại.</p>
 *
 * <h2>Cần phân biệt hai thứ</h2>
 *
 * <p><b>Danh sách này chỉ quyết định con nào được dựng và dựng mấy bản.</b>
 * Việc một id ứng với lớp Java nào thì vẫn nằm trong
 * {@code BossManager.createBoss()} — đó là hành vi, không phải số liệu. Thêm
 * một dòng với id chưa có lớp nào nhận thì không có gì được dựng, và hàm này
 * ghi log nói rõ con nào bị bỏ.</p>
 *
 * <p>Cũng cần nói rõ: đây <b>không phải</b> mọi đường boss xuất hiện. Boss còn
 * sinh ra từ bản đồ ({@code Map.initBoss}), từ NPC, và từ danh sách "boss đi
 * kèm" của boss khác. Bảng này quản đúng nhóm boss thường mà
 * {@code loadBoss()} vẫn dựng.</p>
 */
public final class BossSpawnDAO {

    private BossSpawnDAO() {
    }

    /** Một dòng trong danh sách dựng boss. */
    public static final class Dong {

        public int bossId;
        public String ten = "";
        public int soBanSao = 1;
        public boolean bat = true;
        public int thuTu;
        public String ghiChu = "";
        /** Nhóm hồi sinh. {@code 0} là không theo nhóm nào. */
        public int nhomId;
    }

    public static void damBaoBang() throws Exception {
        ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS boss_spawn ("
                + " boss_id INT(11) NOT NULL,"
                + " ten VARCHAR(100) NOT NULL DEFAULT '',"
                + " so_ban_sao INT(11) NOT NULL DEFAULT 1,"
                + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                + " thu_tu INT(11) NOT NULL DEFAULT 0,"
                + " ghi_chu VARCHAR(255) NOT NULL DEFAULT '',"
                + " nhom_id INT(11) NOT NULL DEFAULT 0,"
                + " PRIMARY KEY (boss_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    /**
     * Cả danh sách, sắp theo thứ tự đã đặt.
     *
     * <p>Trả về <b>rỗng</b> khi bảng chưa có gì — nơi gọi sẽ hiểu là "chưa
     * gieo" và tự gieo từ danh sách gõ cứng.</p>
     */
    public static List<Dong> tatCa() {
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            damBaoBang();
            rs = ConnectDB.executeQuery("SELECT boss_id, ten, so_ban_sao, bat,"
                    + " thu_tu, ghi_chu, nhom_id FROM boss_spawn"
                    + " ORDER BY thu_tu, boss_id");
            while (rs.next()) {
                Dong d = new Dong();
                d.bossId = rs.getInt("boss_id");
                d.ten = rs.getString("ten");
                d.soBanSao = rs.getInt("so_ban_sao");
                d.bat = rs.getBoolean("bat");
                d.thuTu = rs.getInt("thu_tu");
                d.ghiChu = rs.getString("ghi_chu");
                d.nhomId = rs.getInt("nhom_id");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(BossSpawnDAO.class, ex,
                    "Không đọc được bảng boss_spawn");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                    // Dong khong duoc thi thoi.
                }
            }
        }
        return ds;
    }

    public static String luu(Dong d) {
        if (d == null) {
            return "Thiếu dữ liệu.";
        }
        // Tran 500 chu khong phai 50: chinh game dang dung 100 ban cho bon con
        // SOI_HEC_QUYN / O_DO / VIRUS / XIN_BA_TO. Dat tran thap hon so that
        // dang chay la lang le lam mat boss.
        if (d.soBanSao < 0 || d.soBanSao > 500) {
            return "Số bản sao phải từ 0 đến 500.";
        }
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO boss_spawn (boss_id, ten,"
                    + " so_ban_sao, bat, thu_tu, ghi_chu)"
                    + " VALUES (?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " so_ban_sao = VALUES(so_ban_sao), bat = VALUES(bat),"
                    + " thu_tu = VALUES(thu_tu), ghi_chu = VALUES(ghi_chu)",
                    d.bossId, d.ten, d.soBanSao, d.bat ? 1 : 0, d.thuTu, d.ghiChu);
            return null;
        } catch (Exception ex) {
            Logger.logException(BossSpawnDAO.class, ex, "Lỗi lưu boss_spawn");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int bossId) {
        try {
            ConnectDB.executeUpdate("DELETE FROM boss_spawn WHERE boss_id = ?", bossId);
            return null;
        } catch (Exception ex) {
            Logger.logException(BossSpawnDAO.class, ex, "Lỗi xoá boss_spawn");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Khoá đánh dấu đã gieo, để trống bảng vẫn là trống thật. */
    private static final String KHOA_DA_GIEO = "boss_spawn_da_gieo";

    /**
     * Đã từng gieo danh sách ban đầu chưa.
     *
     * <p>Không có dấu này thì <b>bảng trống bị hiểu nhầm là chưa gieo</b>: admin
     * xoá sạch danh sách để tự thêm lại, khởi động lại một cái là ba mươi hai
     * dòng cũ mọc lên nguyên vẹn, mà không ai báo gì cả.</p>
     *
     * <p>Ghi thẳng vào {@code panel_config} chứ không khai trong
     * {@code ConfigDAO}: khai ở đó thì nó hiện lên tab Quy ước như một thiết
     * lập cho người dùng chỉnh, trong khi đây chỉ là dấu vết nội bộ.</p>
     */
    private static boolean daGieo() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT v FROM panel_config WHERE k = ?", KHOA_DA_GIEO);
            return rs.next();
        } catch (Exception ex) {
            // Doc hong thi coi nhu DA gieo: tha khong gieo con hon gieo de len
            // danh sach admin dang dung.
            Logger.logException(BossSpawnDAO.class, ex,
                    "Không đọc được dấu đã gieo boss_spawn");
            return true;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                    // Dong khong duoc thi thoi.
                }
            }
        }
    }

    public static void danhDauDaGieo() {
        try {
            ConnectDB.executeUpdate("INSERT INTO panel_config (k, v) VALUES (?, '1')"
                    + " ON DUPLICATE KEY UPDATE v = '1'", KHOA_DA_GIEO);
        } catch (Exception ex) {
            Logger.logException(BossSpawnDAO.class, ex,
                    "Không ghi được dấu đã gieo boss_spawn");
        }
    }

    /** {@code true} nếu chưa từng gieo — nơi gọi mới nên gieo danh sách mặc định. */
    public static boolean canGieoLanDau() {
        return !daGieo();
    }

    /**
     * Gieo danh sách ban đầu. Chỉ chạy khi bảng còn trống.
     *
     * <p><b>Báo từng dòng hỏng.</b> Nuốt lỗi ở đây thì một dòng bị từ chối sẽ
     * biến mất không tiếng động, và boss đó lặng lẽ không bao giờ xuất hiện —
     * đúng chuyện đã xảy ra khi trần số bản sao còn để 50 mà bốn con quái
     * thường cần 100.</p>
     */
    public static void gieo(List<Dong> ds) {
        try {
            damBaoBang();
            int hong = 0;
            for (Dong d : ds) {
                String loi = luu(d);
                if (loi != null) {
                    hong++;
                    Logger.error("Không gieo được boss " + d.ten + " (id "
                            + d.bossId + "): " + loi + "\n");
                }
            }
            if (hong > 0) {
                Logger.error("Có " + hong + " dòng boss không gieo được — "
                        + "những con đó sẽ KHÔNG xuất hiện\n");
            }
            danhDauDaGieo();
        } catch (Exception ex) {
            Logger.logException(BossSpawnDAO.class, ex, "Không gieo được boss_spawn");
        }
    }
}
