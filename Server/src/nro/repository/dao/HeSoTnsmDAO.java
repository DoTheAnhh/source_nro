package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Hệ số tiềm năng <b>theo bản đồ</b> — bảng {@code he_so_tnsm}.
 *
 * <h2>Vì sao gom về một chỗ</h2>
 *
 * <p>Trước đây mấy con số này nằm rải ba nơi khác nhau: {@code NPoint} có khối
 * nhân 6 cho Bản đồ kho báu, nhân 3 cho Doanh trại, nhân 2 cho Khu vực thám
 * hiểm và chia 10 cho sáu bản đồ; {@code Mob} có phép chia riêng cho Ngũ Hành
 * Sơn đọc từ một quy ước khác. Muốn biết một bản đồ cho tiềm năng gấp mấy lần
 * bản đồ thường thì phải đọc cả ba chỗ và tự nhân tay.</p>
 *
 * <p>Nay tất cả ở đây, và panel hiện thẳng ra "gấp mấy lần bản đồ thường".</p>
 *
 * <h2>Áp ở đâu</h2>
 *
 * <p>Áp trong {@code Mob}, nơi biết <b>bản đồ của con quái</b>. Đó là chỗ đúng:
 * đệ tử cày trong Ngũ Hành Sơn thì phần chia cho sư phụ cũng phải theo hệ số của
 * Ngũ Hành Sơn, mà lúc ấy sư phụ có thể đang đứng ở bản đồ khác — hỏi bản đồ của
 * sư phụ là hỏi nhầm người.</p>
 *
 * <h2>Một bản đồ khớp nhiều dòng thì sao</h2>
 *
 * <p>Lấy <b>dòng đầu tiên</b> theo thứ tự, rồi dừng. Không nhân dồn: hai dòng
 * cùng khớp mà nhân cả hai thì con số nhảy theo cách không ai đoán được, và sửa
 * một dòng lại đổi luôn kết quả của dòng kia.</p>
 */
public final class HeSoTnsmDAO {

    private HeSoTnsmDAO() {
    }

    /** Một dòng hệ số. */
    public static final class Dong {

        public int id;
        /** Khoá riêng của nhóm, chỉ để không tạo trùng. */
        public String khoa = "";
        public String ten = "";

        /**
         * Các bản đồ trong nhóm — dạng {@code "68-72,102,103"}.
         *
         * <p>Thêm hay bớt một bản đồ khỏi nhóm chỉ là sửa ô chữ này, không phải
         * sửa mã.</p>
         */
        public String dsMap = "";
        /** Hệ số cho người chơi thường. {@code 1} là y như bản đồ thường. */
        public double heSo = 1;
        /** Hệ số riêng cho đệ tử. {@code <= 0} nghĩa là dùng chung {@link #heSo}. */
        public double heSoDeTu;
        /** Bật thì <b>người chơi thường đánh không được gì</b>, chỉ đệ tử mới có. */
        public boolean chiDeTu;
        public int thuTu;
        public boolean bat = true;
        public String ghiChu = "";
    }

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `he_so_tnsm` ("
            + " `id` int(11) NOT NULL AUTO_INCREMENT,"
            + " `khoa` varchar(40) NOT NULL,"
            + " `ten` varchar(120) NOT NULL DEFAULT '',"
            + " `ds_map` varchar(255) NOT NULL DEFAULT '',"
            + " `he_so` double NOT NULL DEFAULT 1,"
            + " `he_so_de_tu` double NOT NULL DEFAULT 0,"
            + " `chi_de_tu` tinyint(1) NOT NULL DEFAULT 0,"
            + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
            + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
            + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (`id`),"
            + " UNIQUE KEY `khoa` (`khoa`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTao;
    private static volatile List<Dong> CACHE;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (HeSoTnsmDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTao = true;
            } catch (Exception ex) {
                Logger.logException(HeSoTnsmDAO.class, ex,
                        "Không tạo được bảng he_so_tnsm");
            }
        }
    }

    /** Đọc bảng, tự gieo danh sách gốc nếu còn trống. */
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
                    + " dòng hệ số tiềm năng theo bản đồ");
        }
        CACHE = ds;
        return ds;
    }

    /** Đọc lại từ CSDL ở lần hỏi kế tiếp. Panel gọi sau khi lưu. */
    public static void reload() {
        CACHE = null;
    }

    private static List<Dong> doc() {
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id, khoa, ten, ds_map, he_so, he_so_de_tu,"
                    + " chi_de_tu, thu_tu, bat, ghi_chu FROM he_so_tnsm"
                    + " ORDER BY thu_tu, id");
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getInt("id");
                d.khoa = rs.getString("khoa");
                d.ten = rs.getString("ten");
                d.dsMap = rs.getString("ds_map");
                d.heSo = rs.getDouble("he_so");
                d.heSoDeTu = rs.getDouble("he_so_de_tu");
                d.chiDeTu = rs.getBoolean("chi_de_tu");
                d.thuTu = rs.getInt("thu_tu");
                d.bat = rs.getBoolean("bat");
                d.ghiChu = rs.getString("ghi_chu");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(HeSoTnsmDAO.class, ex, "Lỗi đọc hệ số tiềm năng");
        } finally {
            dong(rs);
        }
        return ds;
    }

    public static String luu(Dong d) {
        damBaoBang();
        if (d == null || d.khoa == null || d.khoa.trim().isEmpty()) {
            return "Thiếu khoá nhóm bản đồ.";
        }
        if (d.heSo < 0 || d.heSoDeTu < 0) {
            return "Hệ số không được âm.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO he_so_tnsm (khoa, ten, ds_map, he_so, he_so_de_tu,"
                    + " chi_de_tu, thu_tu, bat, ghi_chu) VALUES (?,?,?,?,?,?,?,?,?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " ds_map = VALUES(ds_map),"
                    + " he_so = VALUES(he_so), he_so_de_tu = VALUES(he_so_de_tu),"
                    + " chi_de_tu = VALUES(chi_de_tu), thu_tu = VALUES(thu_tu),"
                    + " bat = VALUES(bat), ghi_chu = VALUES(ghi_chu)",
                    d.khoa.trim(), d.ten == null ? "" : d.ten,
                    d.dsMap == null ? "" : d.dsMap.trim(), d.heSo, d.heSoDeTu,
                    d.chiDeTu ? 1 : 0, d.thuTu, d.bat ? 1 : 0,
                    d.ghiChu == null ? "" : d.ghiChu);
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(HeSoTnsmDAO.class, ex, "Lỗi lưu hệ số tiềm năng");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Xoá sạch bảng để lần hỏi sau gieo lại danh sách gốc. */
    public static String gieoLai() {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM he_so_tnsm");
            reload();
            danhSach();
            return null;
        } catch (Exception ex) {
            Logger.logException(HeSoTnsmDAO.class, ex, "Lỗi gieo lại hệ số tiềm năng");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    // =====================================================================
    //  Tra cứu
    // =====================================================================

    /**
     * Bản đồ này có nằm trong danh sách của một nhóm không.
     *
     * <p>Danh sách viết dạng {@code "68-72,102,103"} — id đơn hoặc khoảng, ngăn
     * nhau bằng dấu phẩy. Phần gõ sai bị bỏ qua chứ không làm hỏng cả dòng.</p>
     */
    public static boolean khop(String dsMap, int mapId) {
        if (dsMap == null) {
            return false;
        }
        for (String p : dsMap.split(",")) {
            String t = p.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                int g = t.indexOf('-');
                if (g > 0) {
                    int a = Integer.parseInt(t.substring(0, g).trim());
                    int b = Integer.parseInt(t.substring(g + 1).trim());
                    if (mapId >= Math.min(a, b) && mapId <= Math.max(a, b)) {
                        return true;
                    }
                } else if (Integer.parseInt(t) == mapId) {
                    return true;
                }
            } catch (NumberFormatException boQua) {
                // Phan go sai thi bo qua.
            }
        }
        return false;
    }

    /** Dòng đang chi phối một bản đồ, hoặc {@code null} nếu bản đồ thường. */
    public static Dong dongCua(int mapId) {
        for (Dong d : danhSach()) {
            if (d.bat && khop(d.dsMap, mapId)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Hệ số tiềm năng của một bản đồ.
     *
     * @param laDeTu người đang đánh là đệ tử
     * @return {@code 1} nếu bản đồ thường; {@code 0} nếu nhóm này khoá người
     *         chơi thường và người đánh không phải đệ tử
     */
    public static double heSo(int mapId, boolean laDeTu) {
        Dong d = dongCua(mapId);
        if (d == null) {
            return 1;
        }
        if (d.chiDeTu && !laDeTu) {
            return 0;
        }
        if (laDeTu && d.heSoDeTu > 0) {
            return d.heSoDeTu;
        }
        return d.heSo;
    }

    // =====================================================================
    //  Danh sách gốc
    // =====================================================================

    private static Dong d(int thuTu, String khoa, String ten, String dsMap,
            double heSo, double heSoDeTu, boolean chiDeTu, String ghiChu) {
        Dong x = new Dong();
        x.thuTu = thuTu;
        x.khoa = khoa;
        x.ten = ten;
        x.dsMap = dsMap;
        x.heSo = heSo;
        x.heSoDeTu = heSoDeTu;
        x.chiDeTu = chiDeTu;
        x.ghiChu = ghiChu;
        return x;
    }

    /**
     * Đúng các con số đang chạy trước khi có bảng này.
     *
     * <p>Danh sách bản đồ của mỗi nhóm chép từ chính các phép kiểm tra trong
     * {@code MapService} lúc viết bảng này. Từ giờ chúng là <b>dữ liệu</b>: thêm
     * hay bớt một bản đồ khỏi nhóm chỉ là sửa ô chữ, không phải sửa mã.</p>
     *
     * <p>Ba nhóm cuối — Nappa, Tương lai, Cold — vốn <b>không có hệ số riêng</b>,
     * tức bằng bản đồ thường. Gieo sẵn với hệ số 1 để chúng có mặt trên panel mà
     * chỉnh, chứ không phải đi tra id rồi tự thêm.</p>
     */
    private static List<Dong> goc() {
        List<Dong> ds = new ArrayList<>();
        ds.add(d(10, "kho_bau", "Bản đồ kho báu", "135-138",
                6, 0, false, "Trước ở NPoint: ×6"));
        ds.add(d(20, "doanh_trai", "Doanh trại", "53-62",
                3, 0, false, "Trước ở NPoint: ×3"));
        ds.add(d(30, "kvth", "Khu vực thám hiểm", "179",
                1, 2, false, "Trước ở NPoint: ×2 nhưng CHỈ cho đệ tử"));
        // Chia 3 cho ca hai cot: do la muc von co cua Ngu Hanh Son. Co "chi de
        // tu" lo phan chan nguoi thuong, nen he so ben trai khong can dat ve 0.
        ds.add(d(40, "ngu_hanh_son", "Ngũ Hành Sơn", "122-124",
                1.0 / 3, 1.0 / 3, true,
                "Trước ở Mob: chia 3. Nay chỉ đệ tử đánh mới được tiềm năng."));
        ds.add(d(50, "binh_hut", "Bình hút năng lượng", "180",
                0.1, 0, false, "Trước ở NPoint: chia 10"));
        ds.add(d(51, "dia_nguc", "Địa ngục", "167,168,172,173",
                0.1, 0, false, "Trước ở NPoint: chia 10"));
        ds.add(d(52, "hirudegarn", "Hirudegarn", "126",
                0.1, 0, false, "Trước ở NPoint: chia 10"));
        ds.add(d(53, "potara", "Potara", "189-192",
                0.1, 0, false, "Trước ở NPoint: chia 10"));
        ds.add(d(54, "thanh_dia", "Thánh địa", "156-159",
                0.1, 0, false, "Trước ở NPoint: chia 10"));
        ds.add(d(55, "hanh_tinh_thuc_vat", "Hành tinh thực vật", "160-163",
                0.1, 0, false, "Trước ở NPoint: chia 10"));
        ds.add(d(60, "nappa", "Thung lũng Nappa", "68-72",
                1, 0, false, "Vốn bằng bản đồ thường"));
        ds.add(d(61, "tuong_lai", "Tương lai", "92-94,96-100,102,103",
                1, 0, false, "Vốn bằng bản đồ thường"));
        ds.add(d(62, "cold", "Cold", "105-110,152,158,159",
                1, 0, false, "Vốn bằng bản đồ thường"));
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
