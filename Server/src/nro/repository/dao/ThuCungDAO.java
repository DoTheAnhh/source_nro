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
 * bảng {@code thu_cung_so_huu}), không phải vật phẩm trong hành trang.</p>
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
    /** Hút một phần sát thương gây ra thành HP, trong một lúc. */
    public static final int LOAI_HUT_MAU = 5;
    /** Phản một phần sát thương phải chịu về kẻ đánh, trong một lúc. */
    public static final int LOAI_PHAN_DON = 6;
    /** Cộng tỉ lệ né đòn, trong một lúc. */
    public static final int LOAI_NE_DON = 7;
    /** Khiên chặn sát thương, lớn bằng % HP tối đa, vỡ khi hết hoặc hết giờ. */
    public static final int LOAI_KHIEN = 8;
    /** Vùng hồi máu: mỗi giây hồi % HP tối đa cho mình và đồng đội đứng gần. */
    public static final int LOAI_VUNG_HOI_MAU = 9;
    /** Nộ kích: chính đòn vừa nổ gây thêm %. */
    public static final int LOAI_NO_KICH = 10;
    /** Hồi % KI tối đa ngay. */
    public static final int LOAI_HOI_KI = 11;
    /** Sét lan: đánh % sức đánh vào mọi quái quanh mình. */
    public static final int LOAI_SET_LAN = 12;

    /**
     * Loại ăn ngay lúc nổ, không kéo dài: không có "trong N giây", và ô HUD
     * chỉ hiện chốc lát cho người chơi biết là vừa nổ.
     */
    public static boolean laTucThi(int loai) {
        return loai == LOAI_HOI_HP || loai == LOAI_NO_KICH
                || loai == LOAI_HOI_KI || loai == LOAI_SET_LAN;
    }

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
        "Giảm % sát thương phải chịu",
        "Hút % sát thương gây ra thành HP",
        "Phản % sát thương phải chịu về kẻ đánh",
        "Cộng % né đòn",
        "Khiên chặn sát thương bằng % HP tối đa",
        "Vùng hồi máu: mỗi giây hồi % HP cho mình và đồng đội quanh đó",
        "Nộ kích: đòn này gây thêm % sát thương",
        "Hồi % KI tối đa ngay khi nổ",
        "Sét lan: đánh % sức đánh vào mọi quái quanh mình"
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
        "", "Id chiêu được cộng", "", "", "",
        "", "", "", "",
        "Bán kính (điểm ảnh, 0 = 250)",
        "", "",
        "Bán kính (điểm ảnh, 0 = 250)"
    };

    /** Bán kính mặc định của vùng hồi máu và sét lan. */
    public static final int BAN_KINH_MAC_DINH = 250;

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
        public int tiLe = 3;
        /** Nổ xong bao lâu mới được nổ lại. */
        public int hoiChieu = 30;
        /** Thú phải đạt cấp này mới mở được chiêu. */
        public int capMo = 1;
        public boolean bat = true;
        /**
         * Id hiệu ứng hình hiện trên người chơi khi chiêu nổ; 0 là không có.
         * Thử id bằng lệnh admin {@code ep <id>} trong khung chat.
         */
        public int hieuUng;

        /**
         * Sức mạnh thật của chiêu ở cấp chiêu {@code capChieu}, tính bằng
         * <b>phần vạn</b> (1.000 = 10%).
         *
         * <p><b>Cấp của thú không đụng tới con số này.</b> Chiêu có cấp riêng,
         * mở ra là cấp 1 và chỉ lên bằng sách. Mỗi cấp <b>cộng thẳng</b>
         * {@code chieuCongMoiCap()} điểm phần trăm vào chiêu: chiêu 10%, mỗi cấp
         * +2 thì cấp 2 là 12%, cấp 3 là 14%, cấp 11 là 30%.</p>
         *
         * <p>Vẫn trả phần vạn để chỗ dùng không phải đổi, và để sau này đặt số
         * lẻ (1,5 điểm mỗi cấp…) cũng không phải sửa công thức.</p>
         */
        public int phanVanTheoCapChieu(int capChieu) {
            long goc = phanTram * 100L;
            long them = (long) chieuCongMoiCap() * 100L * Math.max(0, capChieu - 1);
            return (int) Math.min(Integer.MAX_VALUE, goc + them);
        }

        /** Dòng chữ mô tả hiệu ứng, dùng cho cả game lẫn panel. */
        public String moTaHieuUng() {
            String s = tenLoai(loai).replace("%", phanTram + "%");
            if (loai == LOAI_CHIEU) {
                s += " (chiêu " + thamSo + ")";
            }
            if (!laTucThi(loai)) {
                s += " trong " + giay + "s";
            }
            return s + ", tỉ lệ " + tiLe + "% (ở cấp chiêu 1)";
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
                    + " ti_le INT(11) NOT NULL DEFAULT 3,"
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
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS thu_cung_so_huu ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " player_id INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL,"
                    + " ten VARCHAR(40) NOT NULL DEFAULT '',"
                    + " cap INT(11) NOT NULL DEFAULT 1,"
                    + " exp INT(11) NOT NULL DEFAULT 0,"
                    + " hp INT(11) NOT NULL DEFAULT 0,"
                    + " ki INT(11) NOT NULL DEFAULT 0,"
                    + " suc_danh INT(11) NOT NULL DEFAULT 0,"
                    + " giap INT(11) NOT NULL DEFAULT 0,"
                    + " chi_mang INT(11) NOT NULL DEFAULT 0,"
                    + " ra_tran TINYINT(1) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (id),"
                    + " KEY player_id (player_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS thu_cung_chi_so_bac ("
                    + " bac INT(11) NOT NULL,"
                    + " hp_min INT(11) NOT NULL DEFAULT 0, hp_max INT(11) NOT NULL DEFAULT 0,"
                    + " ki_min INT(11) NOT NULL DEFAULT 0, ki_max INT(11) NOT NULL DEFAULT 0,"
                    + " sd_min INT(11) NOT NULL DEFAULT 0, sd_max INT(11) NOT NULL DEFAULT 0,"
                    + " giap_min INT(11) NOT NULL DEFAULT 0, giap_max INT(11) NOT NULL DEFAULT 0,"
                    + " cm_min INT(11) NOT NULL DEFAULT 0, cm_max INT(11) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (bac)"
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
            themCotThieu();
            gieoCauHinh();
            gieoKyNang();
            gieoDoAn();
            gieoBac();
            gieoChiSoBac();
            try {
                // Bang ky nang tao tu ban truoc chua co cot hinh hieu ung.
                ConnectDB.executeUpdate("ALTER TABLE thu_cung_ky_nang"
                        + " ADD COLUMN hieu_ung INT(11) NOT NULL DEFAULT 0");
            } catch (Exception daCo) {
                // Cot da co: khong phai loi.
            }
            datTiLeNoMotLan();
            gieoChieuLaMotLan();
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
    public static final String K_CHIEU_CONG_MOI_CAP = "chieu_cong_moi_cap";

    /**
     * Khoá cũ, bỏ rồi: "mỗi cấp chiêu +x% CỦA GỐC". Nay là cộng thẳng điểm
     * phần trăm ({@link #K_CHIEU_CONG_MOI_CAP}). Đổi TÊN khoá chứ không dùng
     * lại: dòng cũ trong bảng đang ghi 5 theo nghĩa cũ, dùng lại tên ấy thì
     * máy nào đã chạy bản trước sẽ thành +5 điểm mỗi cấp thay vì +2.
     */
    private static final String K_CU_CHIEU_MOI_CAP = "chieu_moi_cap";
    public static final String K_CHI_SO_MOI_CAP = "chi_so_moi_cap";

    /**
     * Khoá cũ, bỏ rồi: chiêu từng mạnh thêm theo CẤP THÚ. Nay chiêu có cấp
     * riêng ({@link #K_CHIEU_CONG_MOI_CAP}), khoá này chỉ còn để xoá dòng cũ khỏi
     * bảng — để nguyên thì panel hiện một ô chỉnh không còn tác dụng gì.
     */
    private static final String K_CU_THEM_MOI_CAP = "them_moi_cap";

    private static final String[][] CAU_HINH_GOC = {
        {K_EXP_MOI_CAP, "100", "Kinh nghiệm cần cho MỖI cấp — lên cấp c cần c × số này"},
        {K_CAP_TOI_DA, "50", "Cấp cao nhất của thú cưng"},
        {K_CHIEU_CONG_MOI_CAP, "2", "Mỗi CẤP CHIÊU (nâng bằng sách) cộng thẳng bao nhiêu điểm % vào chiêu — chiêu 10%, số này 2 thì cấp 2 là 12%. Cấp thú không ảnh hưởng chiêu"},
        {K_CHI_SO_MOI_CAP, "3", "Mỗi cấp thú cưng cộng thêm bao nhiêu % chỉ số"}
    };

    /**
     * Thêm cột cho bảng đã tạo từ bản trước.
     *
     * <p>{@code CREATE TABLE IF NOT EXISTS} không sửa bảng đã có, nên máy nào
     * chạy bản thú cưng đời đầu (thú chưa có tên riêng, chưa có chỉ số bốc) sẽ
     * thiếu cột và mọi câu đọc đều hỏng. Mỗi cột một lệnh riêng, lỗi "trùng
     * tên cột" là chuyện bình thường nên nuốt.</p>
     */
    private static void themCotThieu() {
        String[] cot = {
            "ten VARCHAR(40) NOT NULL DEFAULT ''",
            "hp INT(11) NOT NULL DEFAULT 0",
            "ki INT(11) NOT NULL DEFAULT 0",
            "suc_danh INT(11) NOT NULL DEFAULT 0",
            "giap INT(11) NOT NULL DEFAULT 0",
            "chi_mang INT(11) NOT NULL DEFAULT 0",
            // Cap cua tung chieu, rieng tung con. Mo chieu ra la cap 1; sach
            // nang cap se ghi vao day.
            "cap_chieu_1 INT(11) NOT NULL DEFAULT 1",
            "cap_chieu_2 INT(11) NOT NULL DEFAULT 1",
            "cap_chieu_3 INT(11) NOT NULL DEFAULT 1"
        };
        java.util.Set<String> dangCo = new java.util.HashSet<>();
        CrisResultSet rs = null;
        try {
            // Doc theo TEN COT GOC, khong đặt bí danh: bộ đọc kết quả của máy
            // chủ tra ô theo tên cột thật (viết thường), bí danh AS không có
            // tác dụng — đặt bí danh thì mọi ô đọc ra null và lần nào cũng thử
            // ALTER lại, in đầy log lúc khởi động.
            rs = ConnectDB.executeQuery("SELECT COLUMN_NAME FROM information_schema.COLUMNS"
                    + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'thu_cung_so_huu'");
            while (rs.next()) {
                dangCo.add(String.valueOf(rs.getString("column_name")).toLowerCase());
            }
        } catch (Exception ex) {
            // Khong doc duoc danh sach cot thi thoi, de phan duoi thu them.
        } finally {
            dong(rs);
        }
        for (String c : cot) {
            String ten = c.substring(0, c.indexOf(' ')).toLowerCase();
            if (dangCo.contains(ten)) {
                continue;
            }
            try {
                ConnectDB.executeUpdate("ALTER TABLE thu_cung_so_huu ADD COLUMN " + c);
            } catch (Exception daCo) {
                // Cot da co: khong phai loi.
            }
        }
    }

    private static void gieoCauHinh() throws Exception {
        for (String[] d : CAU_HINH_GOC) {
            ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_cau_hinh"
                    + " (khoa, gia_tri, mo_ta) VALUES (?, ?, ?)", d[0], d[1], d[2]);
        }
        ConnectDB.executeUpdate("DELETE FROM thu_cung_cau_hinh WHERE khoa IN (?, ?)",
                K_CU_THEM_MOI_CAP, K_CU_CHIEU_MOI_CAP);
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

    /** Mỗi cấp chiêu cộng thẳng bao nhiêu điểm phần trăm vào chiêu. */
    public static int chieuCongMoiCap() {
        return Math.max(0, soCauHinh(K_CHIEU_CONG_MOI_CAP, 2));
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
    /** Cờ trong bảng cấu hình: đã hạ tỉ lệ nổ mọi chiêu về 3% hay chưa. */
    private static final String K_DA_HA_TI_LE_NO = "da_ha_ti_le_no_3";

    /**
     * Hạ tỉ lệ nổ của MỌI chiêu về 3% — đúng một lần.
     *
     * <p>Máy chủ thật chỉ {@code git pull} rồi chạy, không ai chạy SQL tay, nên
     * việc sửa dữ liệu phải tự làm lúc khởi động. Nhưng chỉ làm một lần: có cờ
     * trong {@code thu_cung_cau_hinh}. Không có cờ thì lần khởi động nào cũng
     * đè về 3%, và mọi con số admin chỉnh lại trên panel sau đó đều mất.</p>
     */
    private static void datTiLeNoMotLan() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM thu_cung_cau_hinh WHERE khoa = ?",
                    K_DA_HA_TI_LE_NO);
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        ConnectDB.executeUpdate("UPDATE thu_cung_ky_nang SET ti_le = 3");
        ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_cau_hinh (khoa, gia_tri, mo_ta)"
                + " VALUES (?, '1', ?)", K_DA_HA_TI_LE_NO,
                "Đã hạ tỉ lệ nổ mọi chiêu về 3% (chạy một lần). Xoá dòng này là lần khởi động sau hạ lại");
        Logger.success("Thú cưng: đã đặt tỉ lệ nổ mọi chiêu về 3%\n");
    }

    /** Cờ: đã gieo chiêu 2 và 3 kiểu mới cho mọi loại thú hay chưa. */
    private static final String K_DA_GIEO_CHIEU_LA = "da_gieo_chieu_la";

    /**
     * Mẫu chiêu kiểu mới để gieo: loại, mức %, giây, hồi chiêu, tham số, rồi
     * ba cái tên — thú nào lấy tên nào tuỳ id, cho hai con cùng loại chiêu
     * vẫn gọi khác nhau.
     */
    private static final Object[][] MAU_CHIEU_LA = {
        {LOAI_HUT_MAU, 10, 8, 30, 0, "Nanh Huyết Ảnh", "Huyết Phệ", "Cắn Hút Sinh Lực",
            "Mỗi đòn trúng hút lại một phần sát thương thành máu"},
        {LOAI_PHAN_DON, 15, 8, 35, 0, "Giáp Gai Phản Chấn", "Gương Phản Hồn", "Vảy Ngược",
            "Kẻ nào đánh vào sẽ tự lãnh lại một phần sát thương"},
        {LOAI_NE_DON, 15, 6, 35, 0, "Bước Ảnh Mờ", "Thân Pháp Như Gió", "Ảo Ảnh Phân Thân",
            "Thân hình mờ đi, dễ né đòn hơn trong chốc lát"},
        {LOAI_KHIEN, 20, 10, 45, 0, "Khiên Kim Cang", "Lá Chắn Tinh Linh", "Vòng Bảo Hộ",
            "Dựng một tấm khiên hứng sát thương thay cho chủ"},
        {LOAI_VUNG_HOI_MAU, 3, 8, 45, 0, "Suối Nguồn Sinh Mệnh", "Vòng Tròn Chữa Lành", "Mưa Hồi Xuân",
            "Tạo vùng hồi máu quanh chủ, đồng đội đứng gần cũng được hồi"},
        {LOAI_NO_KICH, 80, 0, 20, 0, "Nộ Kích Chí Mạng", "Cú Vồ Hung Tợn", "Đòn Sấm Sét",
            "Dồn lực vào một đòn, sát thương tăng vọt"},
        {LOAI_HOI_KI, 20, 0, 30, 0, "Hấp Thụ Linh Khí", "Tụ Khí Đan Điền", "Nguồn Ki Vô Tận",
            "Hút linh khí trời đất, hồi lại KI cho chủ"},
        {LOAI_SET_LAN, 50, 0, 25, 0, "Sét Lan Liên Hoàn", "Cuồng Lôi Trận", "Bão Tinh Tú",
            "Sét nổ lan ra, đánh mọi con quái quanh chủ"},
    };

    /**
     * Gieo chiêu 2 và 3 kiểu mới cho mọi loại thú — một lần, chỉ vào ô trống.
     *
     * <p>Ô đã có chiêu (admin đặt tay) thì để yên. Có cờ trong bảng cấu hình
     * nên xoá chiêu trên panel rồi khởi động lại cũng không bị gieo lại.</p>
     *
     * <p>Chọn loại theo id thú chứ không bốc ngẫu nhiên: khởi động lại hay
     * chạy trên máy khác vẫn ra đúng bộ chiêu ấy, và hai ô của cùng một con
     * không bao giờ trùng loại.</p>
     */
    private static void gieoChieuLaMotLan() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM thu_cung_cau_hinh WHERE khoa = ?",
                    K_DA_GIEO_CHIEU_LA);
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        List<Integer> ids = new ArrayList<>();
        rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM item_template WHERE TYPE = ?",
                    KIEU_THU_CUNG);
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } finally {
            dong(rs);
        }
        int n = MAU_CHIEU_LA.length;
        int gieo = 0;
        for (int id : ids) {
            int a = Math.floorMod(id * 7, n);
            int b = (a + 3) % n;
            gieo += gieoMotChieu(id, 2, MAU_CHIEU_LA[a], id);
            gieo += gieoMotChieu(id, 3, MAU_CHIEU_LA[b], id + 1);
        }
        ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_cau_hinh (khoa, gia_tri, mo_ta)"
                + " VALUES (?, '1', ?)", K_DA_GIEO_CHIEU_LA,
                "Đã gieo chiêu 2 và 3 kiểu mới cho mọi thú (chạy một lần)");
        lucDocKyNang = 0;
        Logger.success("Thú cưng: gieo " + gieo + " chiêu kiểu mới vào ô trống\n");
    }

    /** Ghi một chiêu mẫu vào ô {@code thuTu} nếu ô ấy còn trống; trả 1 nếu có ghi. */
    private static int gieoMotChieu(int itemId, int thuTu, Object[] mau, int chonTen)
            throws Exception {
        String ten = (String) mau[5 + Math.floorMod(chonTen, 3)];
        return ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_ky_nang"
                + " (item_id, thu_tu, ten, mo_ta, loai, tham_so, phan_tram,"
                + " giay, ti_le, hoi_chieu, cap_mo, bat, hieu_ung)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, 3, ?, ?, 1, 0)",
                itemId, thuTu, ten, mau[8], mau[0], mau[4], mau[1],
                mau[2], mau[3], thuTu * 10) > 0 ? 1 : 0;
    }

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
                        + " VALUES (?, 1, ?, ?, 0, 0, 10, 10, 3, 30, 1, 1)",
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
                try {
                    k.hieuUng = rs.getInt("hieu_ung");
                } catch (Exception chuaCoCot) {
                    k.hieuUng = 0;
                }
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
                    + " ti_le, hoi_chieu, cap_mo, bat, hieu_ung)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten), mo_ta = VALUES(mo_ta),"
                    + " loai = VALUES(loai), tham_so = VALUES(tham_so),"
                    + " phan_tram = VALUES(phan_tram), giay = VALUES(giay),"
                    + " ti_le = VALUES(ti_le), hoi_chieu = VALUES(hoi_chieu),"
                    + " cap_mo = VALUES(cap_mo), bat = VALUES(bat),"
                    + " hieu_ung = VALUES(hieu_ung)",
                    k.itemId, k.thuTu, k.ten, k.moTa, k.loai, k.thamSo, k.phanTram,
                    k.giay, k.tiLe, k.hoiChieu, k.capMo, k.bat ? 1 : 0, k.hieuUng);
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
     * Chia sẵn bậc cho mọi loại thú — <b>chỉ khi bảng còn rỗng</b>.
     *
     * <p>Để trống hết thì con nào cũng là D, và rương cao cấp mở ra chẳng có
     * con nào bậc S để trả — người chơi bấm mở thấy như hỏng. Chia sẵn theo
     * hình tháp: càng lên cao càng ít con.</p>
     *
     * <p>Chia theo thứ tự id, không bốc ngẫu nhiên: máy chủ nào cũng ra cùng
     * một bảng, và admin sửa lại trên panel là xong.</p>
     */
    private static void gieoBac() throws Exception {
        if (demDong("thu_cung_bac") > 0) {
            return;
        }
        CrisResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM item_template WHERE TYPE = ?"
                    + " ORDER BY id", KIEU_THU_CUNG);
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } finally {
            dong(rs);
        }
        if (ids.isEmpty()) {
            return;
        }
        // Phan tram so con cho tung bac, tu D toi SSS.
        int[] phan = {30, 25, 20, 15, 7, 2, 1};
        int tong = 0;
        for (int p : phan) {
            tong += p;
        }
        // Tinh so con cho tung bac TRUOC, phan du don vao bac thap nhat.
        //
        // Cho bac cuoi om phan du thi SSS lai dong hon SS — nguoc hinh thap.
        int[] soCon = new int[phan.length];
        int daChia = 0;
        for (int b = phan.length - 1; b >= 1; b--) {
            soCon[b] = Math.max(1, ids.size() * phan[b] / tong);
            daChia += soCon[b];
        }
        soCon[0] = Math.max(0, ids.size() - daChia);

        int i = 0;
        int[] dem = new int[TEN_BAC.length];
        for (int b = 0; b < phan.length && i < ids.size(); b++) {
            for (int k = 0; k < soCon[b] && i < ids.size(); k++, i++) {
                ConnectDB.executeUpdate("INSERT IGNORE INTO thu_cung_bac (item_id, bac)"
                        + " VALUES (?, ?)", ids.get(i), b);
                dem[b]++;
            }
        }
        StringBuilder sb = new StringBuilder("Thú cưng: chia bậc cho " + ids.size() + " con —");
        for (int b = 0; b < TEN_BAC.length; b++) {
            sb.append(' ').append(TEN_BAC[b]).append('=').append(dem[b]);
        }
        Logger.success(sb.append('\n').toString());
    }

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
    //  Chỉ số theo bậc, và thú của người chơi
    // =====================================================================
    /**
     * Khoảng chỉ số của một bậc.
     *
     * <p>Nhận một con thú thì mỗi chỉ số bốc ngẫu nhiên trong khoảng của bậc
     * ấy, nên hai con cùng loại vẫn khác nhau — chỗ để người chơi săn con
     * "ngon". Khoảng do admin khai trên panel.</p>
     */
    public static final class ChiSoBac {

        public int bac;
        public int hpMin;
        public int hpMax;
        public int kiMin;
        public int kiMax;
        public int sdMin;
        public int sdMax;
        public int giapMin;
        public int giapMax;
        /** Tỉ lệ chí mạng, phần trăm. */
        public int cmMin;
        public int cmMax;
    }

    private static final Map<Integer, ChiSoBac> CHI_SO_BAC = new HashMap<>();
    private static long lucDocChiSoBac;

    private static synchronized void docChiSoBac() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocChiSoBac < HAN_BO_NHO_MS && lucDocChiSoBac > 0) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM thu_cung_chi_so_bac ORDER BY bac");
            Map<Integer, ChiSoBac> moi = new LinkedHashMap<>();
            while (rs.next()) {
                ChiSoBac c = new ChiSoBac();
                c.bac = rs.getInt("bac");
                c.hpMin = rs.getInt("hp_min");
                c.hpMax = rs.getInt("hp_max");
                c.kiMin = rs.getInt("ki_min");
                c.kiMax = rs.getInt("ki_max");
                c.sdMin = rs.getInt("sd_min");
                c.sdMax = rs.getInt("sd_max");
                c.giapMin = rs.getInt("giap_min");
                c.giapMax = rs.getInt("giap_max");
                c.cmMin = rs.getInt("cm_min");
                c.cmMax = rs.getInt("cm_max");
                moi.put(c.bac, c);
            }
            CHI_SO_BAC.clear();
            CHI_SO_BAC.putAll(moi);
            lucDocChiSoBac = bayGio;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được chỉ số theo bậc");
        } finally {
            dong(rs);
        }
    }

    public static ChiSoBac chiSoBac(int bac) {
        docChiSoBac();
        ChiSoBac c = CHI_SO_BAC.get(bac);
        if (c != null) {
            return c;
        }
        ChiSoBac rong = new ChiSoBac();
        rong.bac = bac;
        return rong;
    }

    public static List<ChiSoBac> tatCaChiSoBac() {
        docChiSoBac();
        List<ChiSoBac> ra = new ArrayList<>();
        for (int b = 0; b < TEN_BAC.length; b++) {
            ra.add(chiSoBac(b));
        }
        return ra;
    }

    public static void luuChiSoBac(ChiSoBac c) {
        if (c == null || c.bac < 0 || c.bac >= TEN_BAC.length) {
            return;
        }
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO thu_cung_chi_so_bac"
                    + " (bac, hp_min, hp_max, ki_min, ki_max, sd_min, sd_max,"
                    + " giap_min, giap_max, cm_min, cm_max)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE hp_min = VALUES(hp_min), hp_max = VALUES(hp_max),"
                    + " ki_min = VALUES(ki_min), ki_max = VALUES(ki_max),"
                    + " sd_min = VALUES(sd_min), sd_max = VALUES(sd_max),"
                    + " giap_min = VALUES(giap_min), giap_max = VALUES(giap_max),"
                    + " cm_min = VALUES(cm_min), cm_max = VALUES(cm_max)",
                    c.bac, c.hpMin, c.hpMax, c.kiMin, c.kiMax, c.sdMin, c.sdMax,
                    c.giapMin, c.giapMax, c.cmMin, c.cmMax);
            lucDocChiSoBac = 0;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được chỉ số theo bậc");
        }
    }

    /**
     * Gieo khoảng chỉ số cho bảy bậc — chỉ khi bảng còn rỗng.
     *
     * <p>Bậc trên hơn bậc ngay dưới <b>5%</b>, nhân dồn: đúng luật đã chốt, và
     * vì con số nằm trong bảng nên admin muốn đổi thì sửa thẳng trên panel chứ
     * không phải sửa mã.</p>
     */
    private static void gieoChiSoBac() throws Exception {
        if (demDong("thu_cung_chi_so_bac") > 0) {
            return;
        }
        // Bac D lam goc.
        double[] goc = {2000, 2000, 50, 10, 1};
        for (int b = 0; b < TEN_BAC.length; b++) {
            double he = Math.pow(1.05d, b);
            ChiSoBac c = new ChiSoBac();
            c.bac = b;
            c.hpMin = (int) Math.round(goc[0] * he * 0.8);
            c.hpMax = (int) Math.round(goc[0] * he * 1.2);
            c.kiMin = (int) Math.round(goc[1] * he * 0.8);
            c.kiMax = (int) Math.round(goc[1] * he * 1.2);
            c.sdMin = (int) Math.round(goc[2] * he * 0.8);
            c.sdMax = (int) Math.round(goc[2] * he * 1.2);
            c.giapMin = (int) Math.round(goc[3] * he * 0.8);
            c.giapMax = (int) Math.round(goc[3] * he * 1.2);
            c.cmMin = b == 0 ? 0 : (int) Math.round(goc[4] * he * 0.8);
            c.cmMax = (int) Math.round(goc[4] * he * 1.2);
            luuChiSoBac(c);
        }
        Logger.success("Thú cưng: gieo khoảng chỉ số cho " + TEN_BAC.length + " bậc\n");
    }

    public static int chiSoMoiCap() {
        return Math.max(0, soCauHinh(K_CHI_SO_MOI_CAP, 3));
    }

    // ------------------------------------------------- thú của người chơi
    /** Một con thú cụ thể của một người chơi. */
    public static final class ThuSoHuu {

        public int id;
        public int playerId;
        public int itemId;
        /** Tên người chơi tự đặt; rỗng thì lấy tên loại thú. */
        public String ten = "";
        public int cap = 1;
        public int exp;
        /** Chỉ số bốc lúc nhận, ở cấp 1. */
        public int hp;
        public int ki;
        public int sucDanh;
        public int giap;
        public int chiMang;
        public boolean raTran;
        /**
         * Cấp của từng chiêu, đánh số theo thứ tự chiêu (ô 0 bỏ trống).
         * Mở chiêu ra là cấp 1.
         */
        public int[] capChieu = {1, 1, 1, 1};

        /** Cấp của chiêu thứ {@code thuTu}, ít nhất là 1. */
        public int capChieu(int thuTu) {
            if (thuTu < 1 || thuTu >= capChieu.length) {
                return 1;
            }
            return Math.max(1, capChieu[thuTu]);
        }

        /** Chỉ số sau khi cộng phần của cấp. */
        public int theoCap(int goc) {
            return (int) Math.round(goc * (1d + Math.max(0, cap - 1) * chiSoMoiCap() / 100d));
        }
    }

    /** Mọi con thú của một người chơi, con ra trận đứng đầu. */
    public static List<ThuSoHuu> thuCuaNguoi(int playerId) {
        damBaoBang();
        List<ThuSoHuu> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM thu_cung_so_huu"
                    + " WHERE player_id = ? ORDER BY ra_tran DESC, id", playerId);
            while (rs.next()) {
                ra.add(docThu(rs));
            }
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được thú của người chơi");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Con đang ra trận của một người chơi, hoặc {@code null}. */
    public static ThuSoHuu thuRaTran(int playerId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM thu_cung_so_huu"
                    + " WHERE player_id = ? AND ra_tran = 1 LIMIT 1", playerId);
            return rs.next() ? docThu(rs) : null;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được thú ra trận");
            return null;
        } finally {
            dong(rs);
        }
    }

    public static ThuSoHuu thuTheoId(int id) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM thu_cung_so_huu WHERE id = ?", id);
            return rs.next() ? docThu(rs) : null;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc được thú cưng");
            return null;
        } finally {
            dong(rs);
        }
    }

    private static ThuSoHuu docThu(CrisResultSet rs) throws Exception {
        ThuSoHuu t = new ThuSoHuu();
        t.id = rs.getInt("id");
        t.playerId = rs.getInt("player_id");
        t.itemId = rs.getInt("item_id");
        t.ten = rs.getString("ten");
        t.cap = rs.getInt("cap");
        t.exp = rs.getInt("exp");
        t.hp = rs.getInt("hp");
        t.ki = rs.getInt("ki");
        t.sucDanh = rs.getInt("suc_danh");
        t.giap = rs.getInt("giap");
        t.chiMang = rs.getInt("chi_mang");
        t.raTran = rs.getInt("ra_tran") == 1;
        for (int i = 1; i <= SO_KY_NANG; i++) {
            try {
                t.capChieu[i] = Math.max(1, rs.getInt("cap_chieu_" + i));
            } catch (Exception chuaCoCot) {
                // May chua kip them cot: moi chieu coi nhu cap 1.
                t.capChieu[i] = 1;
            }
        }
        if (t.ten == null) {
            t.ten = "";
        }
        return t;
    }

    /**
     * Đặt cấp cho một chiêu của một con thú — lối vào cho sách nâng chiêu.
     *
     * @param thuTu chiêu thứ mấy, 1..{@link #SO_KY_NANG}
     */
    public static void datCapChieu(int idThu, int thuTu, int cap) {
        if (thuTu < 1 || thuTu > SO_KY_NANG) {
            return;
        }
        try {
            damBaoBang();
            // Ten cot ghep tu thuTu da kiem tra o tren, khong phai chu nguoi
            // choi go vao.
            ConnectDB.executeUpdate("UPDATE thu_cung_so_huu SET cap_chieu_" + thuTu
                    + " = ? WHERE id = ?", Math.max(1, cap), idThu);
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được cấp chiêu");
        }
    }

    /**
     * Thêm một con thú cho người chơi, chỉ số bốc theo khoảng của bậc.
     *
     * @return dòng vừa thêm, hoặc {@code null} nếu hỏng
     */
    public static ThuSoHuu themThu(int playerId, int itemId) {
        ChiSoBac k = chiSoBac(bac(itemId));
        int hp = boc(k.hpMin, k.hpMax);
        int ki = boc(k.kiMin, k.kiMax);
        int sd = boc(k.sdMin, k.sdMax);
        int giap = boc(k.giapMin, k.giapMax);
        int cm = boc(k.cmMin, k.cmMax);
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO thu_cung_so_huu"
                    + " (player_id, item_id, ten, cap, exp, hp, ki, suc_danh, giap,"
                    + " chi_mang, ra_tran) VALUES (?, ?, '', 1, 0, ?, ?, ?, ?, ?, 0)",
                    playerId, itemId, hp, ki, sd, giap, cm);
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không thêm được thú cưng");
            return null;
        }
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM thu_cung_so_huu"
                    + " WHERE player_id = ? ORDER BY id DESC LIMIT 1", playerId);
            return rs.next() ? docThu(rs) : null;
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đọc lại được thú vừa thêm");
            return null;
        } finally {
            dong(rs);
        }
    }

    private static int boc(int min, int max) {
        if (max <= min) {
            return Math.max(0, min);
        }
        return min + nro.core.util.Util.nextInt(0, max - min);
    }

    public static void luuCapExp(int id, int cap, int exp) {
        try {
            ConnectDB.executeUpdate("UPDATE thu_cung_so_huu SET cap = ?, exp = ?"
                    + " WHERE id = ?", cap, exp, id);
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không lưu được cấp thú cưng");
        }
    }

    public static void doiTen(int id, String ten) {
        try {
            ConnectDB.executeUpdate("UPDATE thu_cung_so_huu SET ten = ? WHERE id = ?",
                    ten == null ? "" : ten, id);
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đổi được tên thú cưng");
        }
    }

    /**
     * Cho một con ra trận; mọi con khác của người ấy về nghỉ.
     *
     * <p>Hai câu lệnh chứ không một: "tắt hết rồi bật một con" là cách duy nhất
     * chắc chắn không bao giờ có hai con cùng ra trận, kể cả khi dữ liệu cũ đang
     * lỗi sẵn.</p>
     *
     * @param id dòng cho ra trận, hoặc -1 để cho tất cả về nghỉ
     */
    public static void datRaTran(int playerId, int id) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("UPDATE thu_cung_so_huu SET ra_tran = 0"
                    + " WHERE player_id = ?", playerId);
            if (id > 0) {
                ConnectDB.executeUpdate("UPDATE thu_cung_so_huu SET ra_tran = 1"
                        + " WHERE id = ? AND player_id = ?", id, playerId);
            }
        } catch (Exception ex) {
            Logger.logException(ThuCungDAO.class, ex, "Không đổi được thú ra trận");
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
    /**
     * Id vật phẩm thật của Rương Thú Cưng Thường trên máy này, hoặc -1.
     *
     * <p>Id do máy chủ tự cấp lúc dựng rương nên <b>mỗi máy một khác</b> — chỗ
     * nào cần rương này (quà NRO Pass, quà mở rương…) phải hỏi ở đây, không
     * được viết cứng con số của một máy.</p>
     */
    public static int idRuongThuong() {
        return soCauHinh(K_RUONG_THUONG, -1);
    }

    /** Id vật phẩm thật của Rương Thú Cưng Cao Cấp trên máy này, hoặc -1. */
    public static int idRuongCaoCap() {
        return soCauHinh(K_RUONG_CAO_CAP, -1);
    }

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
