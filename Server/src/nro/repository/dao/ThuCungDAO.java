package nro.repository.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Kho dữ liệu cho <b>thú cưng</b>: kỹ năng riêng của từng loại thú, đồ ăn nuôi
 * thú, và mấy con số về cấp.
 *
 * <h2>Ba bảng</h2>
 *
 * <ul>
 *   <li>{@code thu_cung_ky_nang} — mỗi loại thú tối đa {@link #SO_KY_NANG} chiêu,
 *       mỗi chiêu một dòng: làm gì khi nổ, mạnh bao nhiêu, mấy giây, tỉ lệ nổ,
 *       hồi chiêu, và <b>cấp bao nhiêu mới mở</b>.</li>
 *   <li>{@code thu_cung_do_an} — món nào cho thú ăn được và được bao nhiêu
 *       kinh nghiệm.</li>
 *   <li>{@code thu_cung_cau_hinh} — kinh nghiệm mỗi cấp, cấp trần, và mức kỹ
 *       năng mạnh thêm theo cấp.</li>
 * </ul>
 *
 * <h2>Cấp và kinh nghiệm nằm trên chính con thú</h2>
 *
 * <p>Không có bảng "thú của người chơi". Cấp và kinh nghiệm là hai <b>chỉ số
 * phụ</b> gắn thẳng vào món thú cưng trong hành trang (xem
 * {@link #idChiSoCap()} và {@link #idChiSoExp()}), nên chúng đi theo con thú
 * khi trao đổi, hiện ngay trong bảng mô tả món, và client đọc được mà không
 * cần thêm gói tin nào.</p>
 *
 * <p>Đọc lại mỗi {@link #HAN_BO_NHO_MS}: sửa trên panel thì chốc lát sau máy
 * chủ đã theo số mới, không phải khởi động lại.</p>
 */
public final class ThuCungDAO {

    private ThuCungDAO() {
    }

    /** Kiểu vật phẩm thú cưng. */
    public static final int KIEU_THU_CUNG = 21;

    /** Ô trang bị của thú cưng trong {@code itemsBody}. */
    public static final int O_THU_CUNG = 7;

    /** Mỗi con thú nhiều nhất bao nhiêu chiêu. */
    public static final int SO_KY_NANG = 3;

    private static final long HAN_BO_NHO_MS = 30_000L;

    // =====================================================================
    //  Loại hiệu ứng
    // =====================================================================
    /**
     * Kỹ năng làm gì khi nổ.
     *
     * <p>Thêm loại mới: thêm một hằng ở đây, một dòng trong {@link #TEN_LOAI},
     * và một nhánh trong {@code ThuCungService}. Panel tự có thêm mục trong ô
     * chọn vì nó đọc thẳng {@link #TEN_LOAI}.</p>
     */
    public static final int LOAI_SUC_DANH = 0;
    public static final int LOAI_CHIEU = 1;
    public static final int LOAI_CHI_MANG = 2;
    public static final int LOAI_HOI_HP = 3;
    public static final int LOAI_GIAM_SAT_THUONG = 4;

    /**
     * Tên từng loại, dùng cho panel và cho dòng chữ hiện trong game.
     *
     * <p>Thứ tự phải khớp giá trị hằng: mục thứ {@code i} là loại {@code i}.</p>
     */
    public static final String[] TEN_LOAI = {
        "Cộng % sức đánh",
        "Cộng % sát thương của một chiêu",
        "Cộng % tỉ lệ chí mạng",
        "Hồi % HP tối đa ngay khi nổ",
        "Giảm % sát thương phải chịu"
    };

    // =====================================================================
    //  Bậc của loại thú
    // =====================================================================
    /**
     * Bậc từ thấp tới cao. Chỉ số trong mảng chính là con số lưu xuống bảng.
     *
     * <p>Thêm bậc mới thì nối vào <b>cuối</b> mảng: chèn vào giữa là mọi con
     * thú đang lưu số cũ nhảy sang bậc khác.</p>
     */
    public static final String[] TEN_BAC = {"D", "C", "B", "A", "S", "SS", "SSS"};

    public static String tenBac(int bac) {
        return (bac >= 0 && bac < TEN_BAC.length) ? TEN_BAC[bac] : TEN_BAC[0];
    }

    /** Tham số phụ của loại này nghĩa là gì; rỗng là loại đó không dùng. */
    public static final String[] Y_NGHIA_THAM_SO = {
        "", "Id chiêu được cộng", "", "", ""
    };

    public static String tenLoai(int loai) {
        return (loai >= 0 && loai < TEN_LOAI.length) ? TEN_LOAI[loai] : ("Loại " + loai);
    }

    // =====================================================================
    //  Kiểu dữ liệu
    // =====================================================================
    /** Một chiêu của một loại thú. */
    public static final class KyNang {

        public int itemId;
        /** Chiêu thứ mấy của con này, từ 1 tới {@link #SO_KY_NANG}. */
        public int thuTu = 1;
        public String ten = "";
        public String moTa = "";
        /** Làm gì khi nổ — xem {@link #TEN_LOAI}. */
        public int loai = LOAI_SUC_DANH;
        /** Tham số phụ, nghĩa tuỳ loại (xem {@link #Y_NGHIA_THAM_SO}). */
        public int thamSo;
        /** Mạnh bao nhiêu phần trăm khi nổ. */
        public int phanTram = 10;
        /** Nổ rồi thì kéo dài bao nhiêu giây. */
        public int giay = 10;
        /** Mỗi đòn đánh có bao nhiêu phần trăm cơ hội nổ. */
        public int tiLe = 10;
        /** Nổ xong bao lâu mới được nổ lại. */
        public int hoiChieu = 30;
        /** Thú phải đạt cấp này mới mở được chiêu. */
        public int capMo = 1;
        public boolean bat = true;

        /**
         * Phần trăm thật sự, đã cộng thêm phần của cấp.
         *
         * <p>Đúng bằng số khai trên panel ở <b>cấp vừa mở chiêu</b>; mỗi cấp sau
         * đó cộng thêm {@code themMoiCap()} phần trăm. Không nhân dồn: nhân dồn
         * thì tới cấp trần con số vọt lên vô lý.</p>
         */
        public int phanTramTheoCap(int cap) {
            return phanTram + Math.max(0, cap - capMo) * themMoiCap();
        }

        /** Dòng chữ mô tả hiệu ứng, dùng cho cả game lẫn panel. */
        public String moTaHieuUng() {
            String s = tenLoai(loai).replace("%", phanTram + "%");
            if (loai == LOAI_CHIEU) {
                s += " (chiêu " + thamSo + ")";
            }
            if (loai != LOAI_HOI_HP) {
                s += " trong " + giay + "s";
            }
            return s + ", tỉ lệ " + tiLe + "%";
        }
    }

    /** Một món ăn nuôi thú. */
    public static final class DoAn {

        public int itemId;
        public int exp;
        public boolean bat = true;
    }

    // =====================================================================
    //  Bảng
    // =====================================================================
    private static volatile boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS thu_cung_ky_nang ("
                    + " item_id INT(11) NOT NULL,"
                    + " thu_tu INT(11) NOT NULL DEFAULT 1,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " mo_ta VARCHAR(255) NOT NULL DEFAULT '',"
                    + " loai INT(11) NOT NULL DEFAULT 0,"
                    + " tham_so INT(11) NOT NULL DEFAULT 0,"
                    + " phan_tram INT(11) NOT NULL DEFAULT 10,"
                    + " giay INT(11) NOT NULL DEFAULT 10,"
                    + " ti_le INT(11) NOT NULL DEFAULT 10,"
                    + " hoi_chieu INT(11) NOT NULL DEFAULT 30,"
                    + " cap_mo INT(11) NOT NULL DEFAULT 1,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (item_id, thu_tu)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS thu_cung_do_an ("
                    + " item_id INT(11) NOT NULL,"
                    + " exp INT(11) NOT NULL DEFAULT 10,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (item_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS thu_cung_bac ("
                    + " item_id INT(11) NOT NULL,"
                    + " bac INT(11) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (item_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS thu_cung_ruong ("
                    + " item_id INT(11) NOT NULL,"
                    + " bac INT(11) NOT NULL,"
                    + " ti_le INT(11) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (item_id, bac)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS thu_cung_cau_hinh ("
                    + " khoa VARCHAR(40) NOT NULL,"
                    + " gia_tri VARCHAR(40) NOT NULL DEFAULT '0',"
                    + " mo_ta VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (khoa)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            gieoCauHinh();
            gieoKyNang();
            gieoDoAn();
        } catch (Exception ex) {
            daTaoBang = false;
            Logger.logException(ThuCungDAO.class, ex, "Không tạo được bảng thú cưng");
        }
    }

    // =====================================================================
    //  Cấu hình
    // =====================================================================
    public static final String K_EXP_MOI_CAP = "exp_moi_cap";
    public static final String K_CAP_TOI_DA = "cap_toi_da";
    public static final String K_THEM_MOI_CAP = "them_moi_cap";

    private static final String[][] CAU_HINH_GOC = {
        {K_EXP_MOI_CAP, "100", "Kinh nghiệm cần cho MỖI cấp — lên cấp c cần c × số này"},
        {K_CAP_TOI_DA, "50", "Cấp cao nhất của thú cưng"},
        {K_THEM_MOI_CAP, "1", "Mỗi cấp sau khi mở chiêu thì chiêu mạnh thêm bao nhiêu %"}
    };

    private static void gieoCauHinh() throws Exception {
        for (String[] d : CAU_HINH_GOC) {
            ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_cau_hinh"
                    + " (khoa, gia_tri, mo_ta) VALUES (?, ?, ?)", d[0], d[1], d[2]);
        }
    }

    private static final Map<String, String> CAU_HINH = new HashMap<>();
    private static long lucDocCauHinh;

    private static String cauHinh(String khoa, String duPhong) {
        docCauHinh();
        String v = CAU_HINH.get(khoa);
        return (v == null || v.isEmpty()) ? duPhong : v;
    }

    private static int soCauHinh(String khoa, int duPhong) {
        try {
            return Integer.parseInt(cauHinh(khoa, String.valueOf(duPhong)).trim());
        } catch (NumberFormatException sai) {
            return duPhong;
        }
    }

    private static synchronized void docCauHinh() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocCauHinh < HAN_BO_NHO_MS && !CAU_HINH.isEmpty()) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT khoa, gia_tri FROM thu_cung_cau_hinh");
            Map<String, String> moi = new HashMap<>();
            while (rs.next()) {
                moi.put(rs.getString("khoa"), rs.getString("gia_tri"));
            }
            CAU_HINH.clear();
            CAU_HINH.putAll(moi);
            lucDocCauHinh = bayGio;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được cấu hình thú cưng");
        } finally {
            dong(rs);
        }
    }

    /** Kinh nghiệm cần để lên cấp {@code cap + 1}. */
    public static int expCanChoCap(int cap) {
        return Math.max(1, soCauHinh(K_EXP_MOI_CAP, 100) * Math.max(1, cap));
    }

    public static int capToiDa() {
        return Math.max(1, soCauHinh(K_CAP_TOI_DA, 50));
    }

    public static int themMoiCap() {
        return Math.max(0, soCauHinh(K_THEM_MOI_CAP, 1));
    }

    public static void datCauHinh(String khoa, String giaTri) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO thu_cung_cau_hinh (khoa, gia_tri)"
                    + " VALUES (?, ?) ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri)",
                    khoa, giaTri);
            lucDocCauHinh = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được cấu hình thú cưng");
        }
    }

    /** Cả bảng cấu hình, cho panel bày ra sửa. */
    public static Map<String, String[]> cauHinhDayDu() {
        docCauHinh();
        Map<String, String[]> ra = new LinkedHashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT khoa, gia_tri, mo_ta FROM thu_cung_cau_hinh");
            while (rs.next()) {
                ra.put(rs.getString("khoa"), new String[]{
                    rs.getString("gia_tri"), rs.getString("mo_ta")});
            }
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được cấu hình thú cưng");
        } finally {
            dong(rs);
        }
        return ra;
    }

    // =====================================================================
    //  Kỹ năng
    // =====================================================================
    /**
     * Gieo mỗi loại thú <b>một</b> chiêu mặc định — chỉ khi bảng còn rỗng.
     *
     * <p>Chiêu 1 mở ngay ở cấp 1 để con thú nào vừa nhận về cũng có cái để
     * dùng. Hai chiêu còn lại để trống: admin tự thêm và tự đặt cấp mở.</p>
     */
    private static void gieoKyNang() throws Exception {
        if (demDong("thu_cung_ky_nang") > 0) {
            return;
        }
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id, NAME FROM item_template WHERE TYPE = ?",
                    KIEU_THU_CUNG);
            List<Integer> ids = new ArrayList<>();
            List<String> ten = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
                ten.add(rs.getString("NAME"));
            }
            for (int i = 0; i < ids.size(); i++) {
                ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_ky_nang"
                        + " (item_id, thu_tu, ten, mo_ta, loai, tham_so, phan_tram,"
                        + " giay, ti_le, hoi_chieu, cap_mo, bat)"
                        + " VALUES (?, 1, ?, ?, 0, 0, 10, 10, 10, 30, 1, 1)",
                        ids.get(i), "Đòn của " + ten.get(i),
                        "Mỗi đòn đánh có tỉ lệ nổ, cộng thêm sức đánh trong chốc lát");
            }
            Logger.success("Thú cưng: gieo " + ids.size() + " chiêu mặc định\n");
        } finally {
            dong(rs);
        }
    }

    /** Kỹ năng theo từng loại thú: itemId -> danh sách chiêu, xếp theo thứ tự. */
    private static final Map<Integer, List<KyNang>> KY_NANG = new HashMap<>();
    private static long lucDocKyNang;

    private static synchronized void docKyNang() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocKyNang < HAN_BO_NHO_MS && !KY_NANG.isEmpty()) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM thu_cung_ky_nang"
                    + " ORDER BY item_id, thu_tu");
            Map<Integer, List<KyNang>> moi = new HashMap<>();
            while (rs.next()) {
                KyNang k = new KyNang();
                k.itemId = rs.getInt("item_id");
                k.thuTu = rs.getInt("thu_tu");
                k.ten = rs.getString("ten");
                k.moTa = rs.getString("mo_ta");
                k.loai = rs.getInt("loai");
                k.thamSo = rs.getInt("tham_so");
                k.phanTram = rs.getInt("phan_tram");
                k.giay = rs.getInt("giay");
                k.tiLe = rs.getInt("ti_le");
                k.hoiChieu = rs.getInt("hoi_chieu");
                k.capMo = rs.getInt("cap_mo");
                k.bat = rs.getInt("bat") == 1;
                moi.computeIfAbsent(k.itemId, x -> new ArrayList<>()).add(k);
            }
            KY_NANG.clear();
            KY_NANG.putAll(moi);
            lucDocKyNang = bayGio;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được kỹ năng thú cưng");
        } finally {
            dong(rs);
        }
    }

    /** Mọi chiêu của loại thú này, kể cả chiêu chưa tới cấp mở. */
    public static List<KyNang> kyNangCua(int itemId) {
        docKyNang();
        List<KyNang> ds = KY_NANG.get(itemId);
        return ds == null ? new ArrayList<>() : new ArrayList<>(ds);
    }

    /** Chiêu đang <b>dùng được</b>: đã bật và thú đủ cấp mở. */
    public static List<KyNang> kyNangMo(int itemId, int cap) {
        List<KyNang> ra = new ArrayList<>();
        for (KyNang k : kyNangCua(itemId)) {
            if (k.bat && cap >= k.capMo) {
                ra.add(k);
            }
        }
        return ra;
    }

    /** Cả bảng, cho panel. */
    public static List<KyNang> tatCaKyNang() {
        docKyNang();
        List<KyNang> ra = new ArrayList<>();
        for (List<KyNang> ds : KY_NANG.values()) {
            ra.addAll(ds);
        }
        ra.sort((a, b) -> a.itemId != b.itemId
                ? Integer.compare(a.itemId, b.itemId) : Integer.compare(a.thuTu, b.thuTu));
        return ra;
    }

    /** Thứ tự còn trống của con này, hoặc -1 nếu đã đủ {@link #SO_KY_NANG} chiêu. */
    public static int thuTuTrong(int itemId) {
        List<KyNang> ds = kyNangCua(itemId);
        for (int t = 1; t <= SO_KY_NANG; t++) {
            boolean co = false;
            for (KyNang k : ds) {
                if (k.thuTu == t) {
                    co = true;
                    break;
                }
            }
            if (!co) {
                return t;
            }
        }
        return -1;
    }

    public static void luuKyNang(KyNang k) {
        if (k == null || k.thuTu < 1 || k.thuTu > SO_KY_NANG) {
            return;
        }
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO thu_cung_ky_nang"
                    + " (item_id, thu_tu, ten, mo_ta, loai, tham_so, phan_tram, giay,"
                    + " ti_le, hoi_chieu, cap_mo, bat)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten), mo_ta = VALUES(mo_ta),"
                    + " loai = VALUES(loai), tham_so = VALUES(tham_so),"
                    + " phan_tram = VALUES(phan_tram), giay = VALUES(giay),"
                    + " ti_le = VALUES(ti_le), hoi_chieu = VALUES(hoi_chieu),"
                    + " cap_mo = VALUES(cap_mo), bat = VALUES(bat)",
                    k.itemId, k.thuTu, k.ten, k.moTa, k.loai, k.thamSo, k.phanTram,
                    k.giay, k.tiLe, k.hoiChieu, k.capMo, k.bat ? 1 : 0);
            lucDocKyNang = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được kỹ năng thú cưng");
        }
    }

    public static void xoaKyNang(int itemId, int thuTu) {
        try {
            ConnectDB.executeUpdate("DELETE FROM thu_cung_ky_nang"
                    + " WHERE item_id = ? AND thu_tu = ?", itemId, thuTu);
            lucDocKyNang = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không xoá được kỹ năng thú cưng");
        }
    }

    // =====================================================================
    //  Bậc: đọc và ghi
    // =====================================================================
    /**
     * Bậc của từng loại thú. Con nào chưa khai thì coi là bậc thấp nhất (D),
     * không phải bỏ công gieo sẵn cả bảng.
     */
    private static final Map<Integer, Integer> BAC = new HashMap<>();
    private static long lucDocBac;

    private static synchronized void docBac() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocBac < HAN_BO_NHO_MS && lucDocBac > 0) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT item_id, bac FROM thu_cung_bac");
            Map<Integer, Integer> moi = new HashMap<>();
            while (rs.next()) {
                moi.put(rs.getInt("item_id"), rs.getInt("bac"));
            }
            BAC.clear();
            BAC.putAll(moi);
            lucDocBac = bayGio;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được bậc thú cưng");
        } finally {
            dong(rs);
        }
    }

    public static int bac(int itemId) {
        docBac();
        Integer b = BAC.get(itemId);
        if (b == null || b < 0 || b >= TEN_BAC.length) {
            return 0;
        }
        return b;
    }

    public static void datBac(int itemId, int bac) {
        if (bac < 0 || bac >= TEN_BAC.length) {
            return;
        }
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO thu_cung_bac (item_id, bac)"
                    + " VALUES (?, ?) ON DUPLICATE KEY UPDATE bac = VALUES(bac)",
                    itemId, bac);
            lucDocBac = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được bậc thú cưng");
        }
    }

    // =====================================================================
    //  Đồ ăn
    // =====================================================================
    /** Đậu thần các cấp: cấp càng cao càng nhiều kinh nghiệm. */
    private static void gieoDoAn() throws Exception {
        if (demDong("thu_cung_do_an") > 0) {
            return;
        }
        CrisResultSet rs = null;
        try {
            // Kieu 6 la dau than — mon an co san trong game, ai cung co.
            rs = ConnectDB.executeQuery("SELECT id FROM item_template WHERE TYPE = 6 ORDER BY id");
            List<Integer> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            int exp = 10;
            for (int id : ids) {
                ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_do_an (item_id, exp, bat)"
                        + " VALUES (?, ?, 1)", id, exp);
                exp += 10;
            }
            Logger.success("Thú cưng: gieo " + ids.size() + " món ăn mặc định\n");
        } finally {
            dong(rs);
        }
    }

    private static final Map<Integer, DoAn> DO_AN = new HashMap<>();
    private static long lucDocDoAn;

    private static synchronized void docDoAn() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocDoAn < HAN_BO_NHO_MS && !DO_AN.isEmpty()) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM thu_cung_do_an");
            Map<Integer, DoAn> moi = new HashMap<>();
            while (rs.next()) {
                DoAn d = new DoAn();
                d.itemId = rs.getInt("item_id");
                d.exp = rs.getInt("exp");
                d.bat = rs.getInt("bat") == 1;
                moi.put(d.itemId, d);
            }
            DO_AN.clear();
            DO_AN.putAll(moi);
            lucDocDoAn = bayGio;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được đồ ăn thú cưng");
        } finally {
            dong(rs);
        }
    }

    /** Món này cho thú ăn được bao nhiêu kinh nghiệm; 0 là không ăn được. */
    public static int expCuaDoAn(int itemId) {
        docDoAn();
        DoAn d = DO_AN.get(itemId);
        return (d != null && d.bat && d.exp > 0) ? d.exp : 0;
    }

    public static List<DoAn> tatCaDoAn() {
        docDoAn();
        List<DoAn> ra = new ArrayList<>(DO_AN.values());
        ra.sort((a, b) -> Integer.compare(a.itemId, b.itemId));
        return ra;
    }

    public static void luuDoAn(int itemId, int exp, boolean bat) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO thu_cung_do_an (item_id, exp, bat)"
                    + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE exp = VALUES(exp),"
                    + " bat = VALUES(bat)", itemId, exp, bat ? 1 : 0);
            lucDocDoAn = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được đồ ăn thú cưng");
        }
    }

    public static void xoaDoAn(int itemId) {
        try {
            ConnectDB.executeUpdate("DELETE FROM thu_cung_do_an WHERE item_id = ?", itemId);
            lucDocDoAn = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không xoá được đồ ăn thú cưng");
        }
    }

    // =====================================================================
    //  Rương thú cưng
    // =====================================================================
    /** Khoá nhớ id vật phẩm của hai rương dựng sẵn. */
    public static final String K_RUONG_CAO_CAP = "ruong_cao_cap";
    public static final String K_RUONG_THUONG = "ruong_thuong";

    /** Ảnh hai rương — tệp nằm sẵn trong {@code data/icon/x1..x4}. */
    private static final int ICON_RUONG_CAO_CAP = 25250;
    private static final int ICON_RUONG_THUONG = 25251;

    /** Kiểu vật phẩm "mở ra được", giống mấy hộp quà có sẵn. */
    private static final int KIEU_RUONG = 27;

    /** Tỉ lệ từng bậc của từng rương: itemId -> (bậc -> tỉ lệ). */
    private static final Map<Integer, Map<Integer, Integer>> RUONG = new HashMap<>();
    private static long lucDocRuong;

    private static synchronized void docRuong() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocRuong < HAN_BO_NHO_MS && lucDocRuong > 0) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT item_id, bac, ti_le FROM thu_cung_ruong"
                    + " ORDER BY item_id, bac");
            Map<Integer, Map<Integer, Integer>> moi = new LinkedHashMap<>();
            while (rs.next()) {
                moi.computeIfAbsent(rs.getInt("item_id"), x -> new LinkedHashMap<>())
                        .put(rs.getInt("bac"), rs.getInt("ti_le"));
            }
            RUONG.clear();
            RUONG.putAll(moi);
            lucDocRuong = bayGio;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được rương thú cưng");
        } finally {
            dong(rs);
        }
    }

    /** Món này có phải rương thú cưng không. */
    public static boolean laRuong(int itemId) {
        docRuong();
        Map<Integer, Integer> m = RUONG.get(itemId);
        return m != null && !m.isEmpty();
    }

    /** Tỉ lệ từng bậc của một rương; bậc nào không có nghĩa là rương không ra. */
    public static Map<Integer, Integer> tiLeRuong(int itemId) {
        docRuong();
        Map<Integer, Integer> m = RUONG.get(itemId);
        return m == null ? new LinkedHashMap<>() : new LinkedHashMap<>(m);
    }

    /** Mọi rương đang khai, cho panel bày ra. */
    public static List<Integer> dsRuong() {
        docRuong();
        return new ArrayList<>(RUONG.keySet());
    }

    public static void luuTiLeRuong(int itemId, int bac, int tiLe) {
        if (bac < 0 || bac >= TEN_BAC.length) {
            return;
        }
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO thu_cung_ruong (item_id, bac, ti_le)"
                    + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ti_le = VALUES(ti_le)",
                    itemId, bac, Math.max(0, tiLe));
            lucDocRuong = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được tỉ lệ rương");
        }
    }

    public static void xoaTiLeRuong(int itemId, int bac) {
        try {
            ConnectDB.executeUpdate("DELETE FROM thu_cung_ruong"
                    + " WHERE item_id = ? AND bac = ?", itemId, bac);
            lucDocRuong = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không xoá được tỉ lệ rương");
        }
    }

    /**
     * Dựng sẵn hai rương: cao cấp (S · SS · SSS) và thường (D · C · B · A).
     *
     * <p>Gọi <b>sau</b> {@code loadDatabase}: phải có danh sách vật phẩm trong bộ
     * nhớ mới nối được dòng mới vào đúng vị trí.</p>
     *
     * <p>Id vật phẩm không ghi cứng — cấp bằng {@code MAX(id) + 1} rồi nhớ vào
     * {@code thu_cung_cau_hinh}, giống cách {@code TrungDeTuDAO} làm với ba quả
     * trứng. Ảnh thì ghi cứng vì tệp ảnh nằm trong mã nguồn.</p>
     */
    public static synchronized void damBaoVatPhamRuong() {
        damBaoBang();
        damBaoMotRuong(K_RUONG_CAO_CAP, "Rương Thú Cưng Cao Cấp", ICON_RUONG_CAO_CAP,
                "Mở ra ngẫu nhiên một thú cưng bậc S, SS hoặc SSS",
                new int[][]{{4, 70}, {5, 25}, {6, 5}});
        damBaoMotRuong(K_RUONG_THUONG, "Rương Thú Cưng Thường", ICON_RUONG_THUONG,
                "Mở ra ngẫu nhiên một thú cưng bậc D, C, B hoặc A",
                new int[][]{{0, 40}, {1, 30}, {2, 20}, {3, 10}});
    }

    private static void damBaoMotRuong(String khoa, String ten, int icon,
            String moTa, int[][] tiLeGoc) {
        try {
            int id = soCauHinh(khoa, -1);
            if (id > 0 && coVatPham(id)) {
                return;
            }
            id = themVatPhamRuong(ten, moTa, icon);
            if (id < 0) {
                return;
            }
            datCauHinh(khoa, String.valueOf(id));
            for (int[] d : tiLeGoc) {
                luuTiLeRuong(id, d[0], d[1]);
            }
            Logger.success("Thú cưng: thêm " + ten + " (id " + id + ")\n");
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không dựng được " + ten);
        }
    }

    private static boolean coVatPham(int id) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM item_template WHERE id = ?", id);
            return rs.next();
        } finally {
            dong(rs);
        }
    }

    /**
     * Thêm một dòng {@code item_template} cho rương và nối vào bộ nhớ.
     *
     * <p>Nối vào <b>cuối</b> danh sách trong bộ nhớ: client tra bảng vật phẩm
     * theo vị trí chứ không theo id, mà id mới luôn là {@code MAX(id) + 1} nên
     * cuối danh sách đúng là vị trí của nó.</p>
     */
    private static int themVatPhamRuong(String ten, String moTa, int icon) throws Exception {
        int id;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(id) AS m FROM item_template");
            if (!rs.next()) {
                return -1;
            }
            id = rs.getInt("m") + 1;
        } finally {
            dong(rs);
        }
        if (id < 0 || id > 32766) {
            return -1;
        }
        ConnectDB.executeUpdate("INSERT INTO item_template"
                + " (id, TYPE, gender, NAME, description, level, icon_id, part,"
                + " is_up_to_up, power_require, gold, gold_sell, gem, gem_sell,"
                + " ruby, ruby_sell, head, body, leg, TypeEvent, isGender,"
                + " dung_duoc, aura_id)"
                + " VALUES (?, ?, 3, ?, ?, 1, ?, -1, 0, 0, 0, 0, 0, 0, 0, 0,"
                + " -1, -1, -1, 0, -1, 1, -1)",
                id, KIEU_RUONG, ten, moTa, icon);
        noiVaoBoNho(id, ten, moTa, icon);
        return id;
    }

    private static void noiVaoBoNho(int id, String ten, String moTa, int icon) {
        try {
            List<nro.entity.template.ItemTemplate> ds = nro.server.Manager.ITEM_TEMPLATES;
            if (ds == null || ds.size() != id) {
                return;
            }
            nro.entity.template.ItemTemplate t = new nro.entity.template.ItemTemplate();
            t.id = (short) id;
            t.type = (byte) KIEU_RUONG;
            t.gender = 3;
            t.name = ten;
            t.description = moTa;
            t.level = 1;
            t.iconID = (short) icon;
            t.part = -1;
            t.isUpToUp = false;
            t.strRequire = 0;
            t.head = -1;
            t.body = -1;
            t.leg = -1;
            t.isGender = -1;
            t.dungDuoc = true;
            ds.add(t);
            nro.ui.LamMoi.bao(nro.ui.LamMoi.VAT_PHAM);
        } catch (Exception boQua) {
            // Khong noi duoc vao bo nho thi dong trong CSDL van con: lan khoi
            // dong sau se nap.
        }
    }

    // =====================================================================
    //  Hai chỉ số phụ giữ cấp và kinh nghiệm
    // =====================================================================
    /**
     * Id chỉ số "Cấp" của thú cưng, tự cấp một lần rồi nhớ trong
     * {@code thu_cung_cau_hinh}.
     *
     * <p>Không ghi cứng con số: bảng {@code item_option_template} của mỗi máy
     * chủ dài ngắn khác nhau, ghi cứng là đụng vào chỉ số người ta đang dùng.
     * {@link ChiSoOptionDAO#them} cấp id kế tiếp rồi nối luôn vào bộ nhớ.</p>
     *
     * <p>Kiểu 9 là <b>dòng chỉ in chữ</b>: không cộng gì vào nhân vật, chỉ hiện
     * trong bảng mô tả món.</p>
     */
    public static int idChiSoCap() {
        return idChiSo("chi_so_cap", "Cấp #");
    }

    /** Id chỉ số "Kinh nghiệm" của thú cưng. */
    public static int idChiSoExp() {
        return idChiSo("chi_so_exp", "Kinh nghiệm #");
    }

    private static synchronized int idChiSo(String khoa, String ten) {
        int id = soCauHinh(khoa, -1);
        if (id > 0) {
            return id;
        }
        int moi = ChiSoOptionDAO.them(ten, 9);
        if (moi > 0) {
            datCauHinh(khoa, String.valueOf(moi));
            return moi;
        }
        // Khong cap duoc thi thu cung KHONG giu duoc cap va kinh nghiem. Im lang
        // o day thi nguoi choi cho thu an ca buoi ma cap khong nhuc, chang ai
        // biet vi sao — nen noi to mot dong.
        Logger.warning("Thú cưng: chưa cấp được chỉ số \"" + ten + "\"."
                + " Cấp và kinh nghiệm sẽ không lưu được cho tới khi cấp xong.\n");
        return -1;
    }

    // =====================================================================
    //  Vặt
    // =====================================================================
    private static int demDong(String bang) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM " + bang);
            return rs.next() ? rs.getInt("n") : 0;
        } finally {
            dong(rs);
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.dispose();
        } catch (Exception boQua) {
            // Dong khong duoc thi cung khong lam gi them duoc.
        }
    }
}
