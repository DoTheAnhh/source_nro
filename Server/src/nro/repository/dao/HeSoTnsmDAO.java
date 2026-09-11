package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Hệ số tiềm năng <b>theo bản đồ</b> — bảng {@code he_so_tnsm}.
 *
 * <h2>Một đòn đánh quái nhận bao nhiêu</h2>
 *
 * <p><b>tiềm năng gốc</b> (theo sát thương và cấp — {@link TnGocDAO}) × <b>hệ số
 * của nhóm bản đồ</b>, rồi mới tới bùa, thẻ, vật phẩm trong game, rồi hệ số
 * chung.</p>
 *
 * <p>Mọi nhóm luôn bật — cột {@code bat} chỉ còn để tương thích dữ liệu cũ.</p>
 *
 * <p>Ba hệ số của một nhóm là ba số thật, độc lập nhau:</p>
 * <ul>
 * <li>{@code heSo} — sư phụ (người chơi) tự đánh;</li>
 * <li>{@code heSoDeTu} — đệ tử tự đánh, phần đệ nhận;</li>
 * <li>{@code heSoSuPhu} — đệ tử đánh, phần chia lại cho sư phụ.</li>
 * </ul>
 * <p>Không có kiểu "để trống thì lấy bên trái": trống là 0. "Chỉ đệ" bật thì
 * hệ số sư phụ tự đánh luôn là 0.</p>
 *
 * <h2>Áp ở đâu</h2>
 *
 * <p>Trong {@code Mob}, nơi biết <b>bản đồ của con quái</b>: đệ tử cày trong Ngũ
 * Hành Sơn thì phần chia cho sư phụ cũng phải theo Ngũ Hành Sơn, mà lúc ấy sư
 * phụ có thể đang đứng ở bản đồ khác.</p>
 *
 * <h2>Một bản đồ khớp nhiều dòng</h2>
 *
 * <p>Lấy <b>dòng đầu tiên</b> theo thứ tự rồi dừng, không nhân dồn.</p>
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
        /** Các bản đồ trong nhóm — dạng {@code "68-72,102,103"}. */
        public String dsMap = "";
        /** Sư phụ tự đánh. */
        public double heSo = 1;
        /** Đệ tử tự đánh — phần đệ nhận. */
        public double heSoDeTu = 1;
        /** Đệ tử đánh — phần chia lại cho sư phụ. */
        public double heSoSuPhu = 1;
        /** Bật thì chỉ đệ tử đánh mới được tiềm năng; hệ số sư phụ luôn là 0. */
        public boolean chiDeTu;
        public int thuTu;
        public boolean bat = true;
        public String ghiChu = "";
    }

    /**
     * Phiên bản cách hiểu dữ liệu của bảng.
     *
     * <p>Bản 1: ô đệ tử để 0 nghĩa là "dùng hệ số sư phụ", ô sư phụ nhận để 0
     * nghĩa là "1". Bản 2: mọi ô là số thật, 0 là 0. Dòng nào còn ở bản 1 thì
     * được chuyển một lần lúc khởi động — không chuyển thì đệ tử và phần sư phụ
     * nhận ở mọi nhóm tụt về 0 ngay khi cập nhật máy chủ.</p>
     */
    private static final int PHIEN_BAN = 3;

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `he_so_tnsm` ("
            + " `id` int(11) NOT NULL AUTO_INCREMENT,"
            + " `khoa` varchar(40) NOT NULL,"
            + " `ten` varchar(120) NOT NULL DEFAULT '',"
            + " `ds_map` varchar(255) NOT NULL DEFAULT '',"
            + " `he_so` double NOT NULL DEFAULT 1,"
            + " `he_so_de_tu` double NOT NULL DEFAULT 0,"
            + " `he_so_su_phu` double NOT NULL DEFAULT 0,"
            + " `chi_de_tu` tinyint(1) NOT NULL DEFAULT 0,"
            + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
            + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
            + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
            + " `phien_ban` int(11) NOT NULL DEFAULT 0,"
            + " PRIMARY KEY (`id`),"
            + " UNIQUE KEY `khoa` (`khoa`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTao;
    private static volatile List<Dong> CACHE;
    /** Đã xét thêm hàng "Hành tinh ngục tù" cho bảng có từ trước chưa. */
    private static volatile boolean daXetNgucTu;

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
                // Bang da co tu ban truoc thi CREATE TABLE IF NOT EXISTS khong
                // them cot moi — phai xin rieng. Khong phai go SQL bang tay.
                ConnectDB.executeUpdate("ALTER TABLE `he_so_tnsm`"
                        + " ADD COLUMN IF NOT EXISTS `he_so_su_phu`"
                        + " double NOT NULL DEFAULT 0 AFTER `he_so_de_tu`");
                ConnectDB.executeUpdate("ALTER TABLE `he_so_tnsm`"
                        + " ADD COLUMN IF NOT EXISTS `phien_ban` int(11) NOT NULL DEFAULT 0");
                // Chuyen dong ban 1 sang ban 2, moi dong dung mot lan.
                //
                // MySQL gan SET tu trai sang phai: he_so_de_tu doc he_so CU,
                // roi moi toi luot he_so bi dat ve 0 cho nhom "chi de" — dung
                // thu tu can thiet.
                int doi = ConnectDB.executeUpdate("UPDATE `he_so_tnsm` SET"
                        + " he_so_de_tu = IF(he_so_de_tu <= 0, he_so, he_so_de_tu),"
                        + " he_so_su_phu = IF(he_so_su_phu <= 0, 1, he_so_su_phu),"
                        + " he_so = IF(chi_de_tu = 1, 0, he_so),"
                        + " phien_ban = 2 WHERE phien_ban < 2");
                // Ban 3: bo cot "Bat" tren panel — moi nhom luon bat. Nhom
                // nao dang tat thi bat lai, khong thi no nam im ma khong con
                // cho nao tren panel de bat.
                doi += ConnectDB.executeUpdate("UPDATE `he_so_tnsm` SET bat = 1,"
                        + " phien_ban = " + PHIEN_BAN + " WHERE phien_ban < " + PHIEN_BAN);
                if (doi > 0) {
                    Logger.success("CONFIG", "Đã chuyển " + doi
                            + " dòng hệ số tiềm năng sang cách hiểu mới (ô trống = 0)");
                }
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
        } else if (!daXetNgucTu) {
            // Bang gieo tu truoc khi co hang "Hanh tinh nguc tu" thi them
            // mot lan, khong phai go SQL hay bam "Ve mac dinh" (mat het so da
            // sua).
            daXetNgucTu = true;
            boolean co = false;
            for (Dong d : ds) {
                co |= NGUC_TU.equals(d.khoa);
            }
            if (!co) {
                luu(ngucTu());
                ds = doc();
            }
        }
        CACHE = ds;
        return ds;
    }

    /** Đọc lại từ CSDL ở lần hỏi kế tiếp. */
    public static void reload() {
        CACHE = null;
    }

    private static List<Dong> doc() {
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id, khoa, ten, ds_map, he_so, he_so_de_tu,"
                    + " he_so_su_phu, chi_de_tu, thu_tu, bat, ghi_chu FROM he_so_tnsm"
                    + " ORDER BY thu_tu, id");
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getInt("id");
                d.khoa = rs.getString("khoa");
                d.ten = rs.getString("ten");
                d.dsMap = rs.getString("ds_map");
                d.heSo = rs.getDouble("he_so");
                d.heSoDeTu = rs.getDouble("he_so_de_tu");
                d.heSoSuPhu = rs.getDouble("he_so_su_phu");
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
        if (d.heSo < 0 || d.heSoDeTu < 0 || d.heSoSuPhu < 0) {
            return "Hệ số không được âm.";
        }
        // "Chi de" thi su phu tu danh khong duoc gi — ghi han 0 xuong CSDL, de
        // bang va may chu noi cung mot con so.
        if (d.chiDeTu) {
            d.heSo = 0;
        }
        d.bat = true;
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO he_so_tnsm (khoa, ten, ds_map, he_so, he_so_de_tu,"
                    + " he_so_su_phu, chi_de_tu, thu_tu, bat, ghi_chu, phien_ban)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?," + PHIEN_BAN + ")"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " ds_map = VALUES(ds_map),"
                    + " he_so = VALUES(he_so), he_so_de_tu = VALUES(he_so_de_tu),"
                    + " he_so_su_phu = VALUES(he_so_su_phu),"
                    + " chi_de_tu = VALUES(chi_de_tu), thu_tu = VALUES(thu_tu),"
                    + " bat = VALUES(bat), ghi_chu = VALUES(ghi_chu),"
                    + " phien_ban = " + PHIEN_BAN,
                    d.khoa.trim(), d.ten == null ? "" : d.ten,
                    d.dsMap == null ? "" : d.dsMap.trim(), d.heSo, d.heSoDeTu,
                    d.heSoSuPhu, d.chiDeTu ? 1 : 0, d.thuTu, d.bat ? 1 : 0,
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
            if (khop(d.dsMap, mapId)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Hệ số của người đang đánh ở một bản đồ.
     *
     * @param laDeTu người đánh là đệ tử
     * @return {@code 1} nếu bản đồ thường; đệ tử lấy đúng ô đệ tử; người chơi
     *         lấy ô sư phụ, và 0 nếu nhóm bật "chỉ đệ"
     */
    public static double heSo(int mapId, boolean laDeTu) {
        Dong d = dongCua(mapId);
        if (d == null) {
            return 1;
        }
        if (laDeTu) {
            return d.heSoDeTu;
        }
        return d.chiDeTu ? 0 : d.heSo;
    }

    /**
     * Hệ số cho phần <b>sư phụ nhận</b> khi đệ tử đánh ở bản đồ này.
     *
     * @return {@code 1} nếu bản đồ thường, còn lại đúng ô "sư phụ nhận"
     */
    public static double heSoSuPhu(int mapId) {
        Dong d = dongCua(mapId);
        return d == null ? 1 : d.heSoSuPhu;
    }

    // =====================================================================
    //  Danh sách gốc
    // =====================================================================

    private static Dong d(int thuTu, String khoa, String ten, String dsMap,
            double heSo, double heSoDeTu, double heSoSuPhu, boolean chiDeTu,
            String ghiChu) {
        Dong x = new Dong();
        x.thuTu = thuTu;
        x.khoa = khoa;
        x.ten = ten;
        x.dsMap = dsMap;
        x.heSo = heSo;
        x.heSoDeTu = heSoDeTu;
        x.heSoSuPhu = heSoSuPhu;
        x.chiDeTu = chiDeTu;
        x.ghiChu = ghiChu;
        return x;
    }

    /**
     * Các nhóm gốc — mọi ô điền số thật.
     *
     * <p>Danh sách bản đồ chép từ các phép kiểm tra trong {@code MapService} lúc
     * viết bảng này; từ giờ chúng là dữ liệu, sửa trên panel.</p>
     */
    private static List<Dong> goc() {
        List<Dong> ds = new ArrayList<>();
        ds.add(d(10, "kho_bau", "Bản đồ kho báu", "135-138",
                6, 6, 1, false, "Trước ở NPoint: ×6"));
        ds.add(d(20, "doanh_trai", "Doanh trại", "53-62",
                3, 3, 1, false, "Trước ở NPoint: ×3"));
        ds.add(d(30, "kvth", "Khu vực thám hiểm", "179",
                1, 2, 1, false, "Trước ở NPoint: ×2 nhưng chỉ cho đệ tử"));
        ds.add(d(40, "ngu_hanh_son", "Ngũ Hành Sơn", "122-124",
                0, 1.0 / 3, 1, true, "Chỉ đệ tử đánh mới được tiềm năng"));
        ds.add(d(50, "binh_hut", "Bình hút năng lượng", "180",
                0.1, 0.1, 1, false, "Trước ở NPoint: chia 10"));
        ds.add(d(51, "dia_nguc", "Địa ngục", "167,168,172,173",
                0.1, 0.1, 1, false, "Trước ở NPoint: chia 10"));
        ds.add(d(52, "hirudegarn", "Hirudegarn", "126",
                0.1, 0.1, 1, false, "Trước ở NPoint: chia 10"));
        ds.add(d(53, "potara", "Potara", "189-192",
                0.1, 0.1, 1, false, "Trước ở NPoint: chia 10"));
        ds.add(d(54, "thanh_dia", "Thánh địa", "156-159",
                0.1, 0.1, 1, false, "Trước ở NPoint: chia 10"));
        ds.add(d(55, "hanh_tinh_thuc_vat", "Hành tinh thực vật", "160-163",
                0.1, 0.1, 1, false, "Trước ở NPoint: chia 10"));
        ds.add(d(60, "nappa", "Thung lũng Nappa", "68-72",
                1, 1, 1, false, "Vốn bằng bản đồ thường"));
        ds.add(d(61, "tuong_lai", "Tương lai", "92-94,96-100,102,103",
                1, 1, 1, false, "Vốn bằng bản đồ thường"));
        ds.add(d(62, "cold", "Cold", "105-110,152,158,159",
                1, 1, 1, false, "Vốn bằng bản đồ thường"));
        ds.add(ngucTu());
        return ds;
    }

    private static final String NGUC_TU = "nguc_tu";

    /** Hành tinh ngục tù — hàng riêng, bản đồ lấy từ {@code MapService.isMapHanhTinhNgucTu}. */
    private static Dong ngucTu() {
        return d(63, NGUC_TU, "Hành tinh ngục tù", "155,206",
                1, 1, 1, false, "Hàng riêng — vốn bằng bản đồ thường");
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
