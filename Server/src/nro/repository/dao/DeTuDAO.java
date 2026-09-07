package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Kho dữ liệu cho <b>đệ tử</b> — trần chỉ số, bậc thang theo bản đồ, và loại đệ.
 *
 * <h2>Ba bảng</h2>
 *
 * <ul>
 *   <li>{@code de_tu_chi_so} — một dòng duy nhất: khoảng bốc của <b>đệ sơ
 *       sinh</b>, và <b>trần</b> mà đệ nâng tới được bằng tiềm năng</li>
 *   <li>{@code de_tu_loai} — bốn loại đệ: tên, hệ số chỉ số, sức mạnh khởi điểm</li>
 * </ul>
 *
 * <h2>Vì sao dùng phần nghìn thay vì số tuyệt đối</h2>
 *
 * <p>Bậc thấp nhất chỉ khoảng 0,2% của trần. Viết bằng phần trăm là mất hết chữ
 * số; viết bằng số tuyệt đối thì đổi trần phải sửa lại cả chín bậc bằng tay, mà
 * sửa sai một dòng thì bậc đó lặng lẽ lệch khỏi thang.</p>
 *
 * <h2>Bảng là nguồn duy nhất</h2>
 *
 * <p>Mã nguồn không còn giữ bản sao của những con số này. {@link #damBaoBang()}
 * điền sẵn bảng lún tạo, và ba hàm đọc không bao giờ trả {@code null} — nên
 * chỗ gọi không phải giữ một bản dự phòng. Một con số ở hai chỗ là sớm
 * muộn lệch nhau.</p>
 */
public class DeTuDAO {

    private DeTuDAO() {
    }

    private static volatile boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS de_tu_chi_so ("
                    + " id INT(11) NOT NULL DEFAULT 1,"
                    // Chi so DE SO SINH — boc ngau nhien luc nhan de.
                    + " ss_hp_min INT(11) NOT NULL DEFAULT 1200,"
                    + " ss_hp_max INT(11) NOT NULL DEFAULT 3000,"
                    + " ss_dame_min INT(11) NOT NULL DEFAULT 25,"
                    + " ss_dame_max INT(11) NOT NULL DEFAULT 100,"
                    + " ss_giap_min INT(11) NOT NULL DEFAULT 1,"
                    + " ss_giap_max INT(11) NOT NULL DEFAULT 20,"
                    + " ss_crit_min INT(11) NOT NULL DEFAULT 0,"
                    + " ss_crit_max INT(11) NOT NULL DEFAULT 2,"
                    + " ss_sm_min BIGINT(20) NOT NULL DEFAULT 2000,"
                    + " ss_sm_max BIGINT(20) NOT NULL DEFAULT 100000,"
                    // TRAN — chi so cao nhat de nang toi duoc bang tiem nang.
                    + " max_hp INT(11) NOT NULL DEFAULT 600000,"
                    + " max_dame INT(11) NOT NULL DEFAULT 25000,"
                    + " max_giap INT(11) NOT NULL DEFAULT 200,"
                    + " max_crit INT(11) NOT NULL DEFAULT 10,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // Bang de_tu_tran cu gop lam mot ca hai khai niem: min tuong la san
            // cua khoang boc, that ra la chi so de SO SINH; con max la TRAN nang
            // toi. Bang moi tach ro hai nhom, nen bo bang cu.
            try {
                ConnectDB.executeUpdate("DROP TABLE IF EXISTS de_tu_tran");
                ConnectDB.executeUpdate("DROP TABLE IF EXISTS de_tu_bac");
            } catch (Exception boQua) {
                // Khong xoa duoc thi hai bang do chi nam khong, khong ai doc.
            }
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS de_tu_loai ("
                    + " loai INT(11) NOT NULL,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " he_so DOUBLE NOT NULL DEFAULT 100,"
                    + " suc_manh_dau BIGINT(20) NOT NULL DEFAULT 0,"
                    + " tu_trung TINYINT(1) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " ghi_chu VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (loai)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            dienSanNeuRong();
        } catch (Exception ex) {
            Logger.logException(DeTuDAO.class, ex, "Không tạo được bảng đệ tử");
        }
    }

    /**
     * Điền sẵn trần, bốn loại đệ và chín bậc — <b>chỉ khi bảng còn rỗng</b>.
     *
     * <h3>Vì sao điền sẵn thay vì để trống</h3>
     *
     * <p>Bảng rỗng thì tab trên panel trắng trơn: người dùng mở ra không biết
     * mình đang sửa cái gì, và phải đoán ra là có một nút "Dựng sẵn" phải bấm
     * trước. Điền sẵn đúng những con số máy chủ đang chạy thì mở tab ra là thấy
     * ngay hệ thống hiện tại, sửa dòng nào muốn đổi.</p>
     *
     * <h3>Chỉ điền khi rỗng</h3>
     *
     * <p>Kiểm số dòng trước, không dùng {@code INSERT IGNORE} hay
     * {@code ON DUPLICATE KEY}: hai cách đó chạy mỗi lần khởi động và sẽ ghi đè
     * lại những dòng quản trị đã sửa tay. Đã có dòng nào là bỏ qua hẳn.</p>
     */
    private static void dienSanNeuRong() throws Exception {
        if (demDong("de_tu_chi_so") == 0) {
            luuChiSo(new ChiSo());
        }
        if (demDong("de_tu_loai") == 0) {
            // Ma loai phai khop ConstDetu: 0 thuong, 1 Mabu, 6 Cell, 7 Bill.
            // He so nhan don 5% moi bac — Bill hon thuong 15,7625% chu khong
            // phai 15%, do moi dung nghia "hon bac lien truoc 5%".
            themLoai(0, "Đệ tử", 100d, 0L, false,
                    "Đệ mặc định, nhận ở NPC");
            themLoai(1, "Mabư", 105d, 1_500_000L, true,
                    "Hơn đệ thường 5%");
            themLoai(6, "Cell", 110.25d, 1_500_000L, true,
                    "Hơn Mabư 5% (hơn thường 10,25%)");
            themLoai(7, "Bill", 115.7625d, 1_500_000L, true,
                    "Hơn Cell 5% (hơn thường 15,7625%)");
        }
    }

    private static int demDong(String bang) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM " + bang);
            return rs.next() ? rs.getInt("n") : 0;
        } finally {
            dong(rs);
        }
    }

    private static void themLoai(int ma, String ten, double heSo, long smDau,
            boolean tuTrung, String ghiChu) throws Exception {
        ConnectDB.executeUpdate("INSERT INTO de_tu_loai"
                + " (loai, ten, he_so, suc_manh_dau, tu_trung, bat, ghi_chu)"
                + " VALUES (?, ?, ?, ?, ?, 1, ?)",
                ma, ten, heSo, smDau, tuTrung ? 1 : 0, ghiChu);
    }

    // =====================================================================
    //  Trần chỉ số
    // =====================================================================
    /**
     * Chỉ số của đệ tử — <b>hai nhóm khác nhau</b>.
     *
     * <h3>Sơ sinh: bốc ngẫu nhiên lúc nhận đệ</h3>
     *
     * <p>Các trường {@code ss*} là khoảng bốc lúc đệ mới ra. Đệ sơ sinh yếu, và
     * người chơi nâng nó lên bằng tiềm năng.</p>
     *
     * <h3>Trần: chỉ số cao nhất nâng tới được</h3>
     *
     * <p>Các trường {@code max*} là mức chặn khi nâng bằng tiềm năng — đạt tới đó
     * là game báo "đã đạt mức tối đa". Đây <b>không phải</b> mốc bốc lúc sinh.</p>
     *
     * <p>Đã bỏ bậc theo bản đồ: trước đây chỉ số bốc theo bản đồ chủ đang đứng,
     * gọi ở map 107 được đệ 600.000 HP còn gọi ở map 30 được 17.000 HP — hơn ba
     * mươi lần chênh nhau chỉ vì chỗ đứng, mà không có gì trong game nói cho người
     * chơi biết điều đó.</p>
     *
     * <p>Loại đệ xịn hơn nhân hệ số vào <b>cả hai nhóm</b>, nên Bill sơ sinh khoẻ
     * hơn đệ thường sơ sinh, và trần của Bill cũng cao hơn đúng bằng hệ số.</p>
     */
    public static final class ChiSo {

        public int ssHpMin = 1_200;
        public int ssHpMax = 3_000;
        public int ssDameMin = 25;
        public int ssDameMax = 100;
        public int ssGiapMin = 1;
        public int ssGiapMax = 20;
        public int ssCritMin = 0;
        public int ssCritMax = 2;
        public long ssSmMin = 2_000L;
        public long ssSmMax = 100_000L;

        public int maxHp = 600_000;
        public int maxDame = 25_000;
        public int maxGiap = 200;
        public int maxCrit = 10;
    }

    private static ChiSo dem;
    private static long lucDocDem;

    /** Bao lâu thì đọc lại bảng, tính bằng mili giây. */
    private static final long HAN_DEM = 15_000L;

    /**
     * Chỉ số đệ tử. <b>Không bao giờ trả {@code null}.</b>
     *
     * <h3>Có nhớ tạm</h3>
     *
     * <p>Hàm này được gọi mỗi lần người chơi bấm nâng một điểm tiềm năng cho đệ,
     * nên đọc CSDL mỗi lần là mỗi cái bấm một truy vấn. Nhớ tạm mười lăm giây:
     * sửa trên panel thì chậm nhất mười lăm giây sau là có hiệu lực.</p>
     */
    public static synchronized ChiSo chiSo() {
        long gio = System.currentTimeMillis();
        if (dem != null && gio - lucDocDem < HAN_DEM) {
            return dem;
        }
        damBaoBang();
        ChiSo t = new ChiSo();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM de_tu_chi_so WHERE id = 1");
            if (rs.next()) {
                t.ssHpMin = rs.getInt("ss_hp_min");
                t.ssHpMax = rs.getInt("ss_hp_max");
                t.ssDameMin = rs.getInt("ss_dame_min");
                t.ssDameMax = rs.getInt("ss_dame_max");
                t.ssGiapMin = rs.getInt("ss_giap_min");
                t.ssGiapMax = rs.getInt("ss_giap_max");
                t.ssCritMin = rs.getInt("ss_crit_min");
                t.ssCritMax = rs.getInt("ss_crit_max");
                t.ssSmMin = rs.getLong("ss_sm_min");
                t.ssSmMax = rs.getLong("ss_sm_max");
                t.maxHp = rs.getInt("max_hp");
                t.maxDame = rs.getInt("max_dame");
                t.maxGiap = rs.getInt("max_giap");
                t.maxCrit = rs.getInt("max_crit");
            }
        } catch (Exception ex) {
            Logger.logException(DeTuDAO.class, ex, "Lỗi đọc de_tu_chi_so");
        } finally {
            dong(rs);
        }
        dem = t;
        lucDocDem = gio;
        return t;
    }

    /** Bỏ nhớ tạm — gọi sau khi lưu để có hiệu lực ngay. */
    public static synchronized void xoaDem() {
        dem = null;
    }

    /**
     * Kiểm một khoảng chỉ số sơ sinh so với trần của nó.
     *
     * <p>Bốc sơ sinh cao hơn trần thì con đệ vừa sinh đã vượt trần, game báo "đã
     * đạt mức tối đa" ngay từ đầu và nâng tiềm năng thành vô nghĩa.</p>
     *
     * @return câu lỗi, hoặc {@code null} nếu khoảng hợp lệ
     */
    private static String kiemKhoang(String ten, int min, int max, int tran) {
        if (min < 0) {
            return "Sơ sinh " + ten + ": số tối thiểu không được âm.";
        }
        if (max < min) {
            return "Sơ sinh " + ten + ": số tối đa (" + max
                    + ") nhỏ hơn tối thiểu (" + min + ").";
        }
        if (max > tran) {
            return "Sơ sinh " + ten + ": số tối đa (" + max
                    + ") vượt trần (" + tran + ").";
        }
        return null;
    }

    public static String luuChiSo(ChiSo t) {
        damBaoBang();
        if (t == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (t.maxHp <= 0 || t.maxDame <= 0) {
            return "Trần HP/KI và sức đánh phải lớn hơn 0.";
        }
        if (t.maxCrit < 0 || t.maxCrit > 100) {
            return "Trần chí mạng phải từ 0 đến 100.";
        }
        if (t.maxGiap < 0) {
            return "Trần giáp không được âm.";
        }
        // Bao TEN chi so trong tung cau loi. Bao gop mot cau chung thi nguoi dung
        // nhin muoi bon o ma khong biet o nao hong.
        String loi = kiemKhoang("HP và KI", t.ssHpMin, t.ssHpMax, t.maxHp);
        if (loi == null) {
            loi = kiemKhoang("sức đánh", t.ssDameMin, t.ssDameMax, t.maxDame);
        }
        if (loi == null) {
            loi = kiemKhoang("giáp", t.ssGiapMin, t.ssGiapMax, t.maxGiap);
        }
        if (loi == null) {
            loi = kiemKhoang("chí mạng", t.ssCritMin, t.ssCritMax, t.maxCrit);
        }
        if (loi != null) {
            return loi;
        }
        if (t.ssSmMin < 0 || t.ssSmMax < t.ssSmMin) {
            return "Khoảng sức mạnh đệ sơ sinh không hợp lệ.";
        }
        try {
            ConnectDB.executeUpdate("INSERT INTO de_tu_chi_so"
                    + " (id, ss_hp_min, ss_hp_max, ss_dame_min, ss_dame_max,"
                    + " ss_giap_min, ss_giap_max, ss_crit_min, ss_crit_max,"
                    + " ss_sm_min, ss_sm_max, max_hp, max_dame, max_giap,"
                    + " max_crit)"
                    + " VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ss_hp_min = VALUES(ss_hp_min),"
                    + " ss_hp_max = VALUES(ss_hp_max),"
                    + " ss_dame_min = VALUES(ss_dame_min),"
                    + " ss_dame_max = VALUES(ss_dame_max),"
                    + " ss_giap_min = VALUES(ss_giap_min),"
                    + " ss_giap_max = VALUES(ss_giap_max),"
                    + " ss_crit_min = VALUES(ss_crit_min),"
                    + " ss_crit_max = VALUES(ss_crit_max),"
                    + " ss_sm_min = VALUES(ss_sm_min),"
                    + " ss_sm_max = VALUES(ss_sm_max),"
                    + " max_hp = VALUES(max_hp), max_dame = VALUES(max_dame),"
                    + " max_giap = VALUES(max_giap), max_crit = VALUES(max_crit)",
                    t.ssHpMin, t.ssHpMax, t.ssDameMin, t.ssDameMax,
                    t.ssGiapMin, t.ssGiapMax, t.ssCritMin, t.ssCritMax,
                    t.ssSmMin, t.ssSmMax, t.maxHp, t.maxDame, t.maxGiap,
                    t.maxCrit);
            xoaDem();
            return null;
        } catch (Exception ex) {
            Logger.logException(DeTuDAO.class, ex, "Lỗi lưu de_tu_chi_so");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Trần của một đệ tử cụ thể — trần chung nhân hệ số của loại.
     *
     * <p>Trả về mảng bốn số theo thứ tự HP/KI, sức đánh, giáp, chí mạng. Dùng
     * mảng thay vì bốn hàm để chỗ gọi ({@code increasePoint}) chỉ đọc CSDL một
     * lần cho cả bốn phép so.</p>
     */
    public static long[] tranCuaDeTu(int loaiDe) {
        ChiSo t = chiSo();
        Loai l = loai(loaiDe);
        double h = l.heSo / 100d;
        if (h <= 0) {
            h = 1d;
        }
        return new long[]{
            Math.round(t.maxHp * h), Math.round(t.maxDame * h),
            Math.round(t.maxGiap * h), Math.round(t.maxCrit * h)
        };
    }

    // =====================================================================
    //  Loại đệ
    // =====================================================================
    public static final class Loai {

        public int loai;
        public String ten;
        /** Hệ số chỉ số, tính bằng phần trăm: 105 là hơn đệ thường 5%. */
        public double heSo = 100;
        public long sucManhDau;
        /** Loại này nhận bằng trứng — dùng {@link #sucManhDau} thay vì bậc map. */
        public boolean tuTrung;
        public boolean bat = true;
        public String ghiChu;
    }

    public static List<Loai> dsLoai() {
        damBaoBang();
        List<Loai> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM de_tu_loai ORDER BY loai");
            while (rs.next()) {
                Loai l = new Loai();
                l.loai = rs.getInt("loai");
                l.ten = rs.getStringOrNull("ten");
                l.heSo = rs.getDouble("he_so");
                l.sucManhDau = rs.getLong("suc_manh_dau");
                l.tuTrung = rs.getBoolean("tu_trung");
                l.bat = rs.getBoolean("bat");
                l.ghiChu = rs.getStringOrNull("ghi_chu");
                ra.add(l);
            }
        } catch (Exception ex) {
            Logger.logException(DeTuDAO.class, ex, "Lỗi đọc de_tu_loai");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /**
     * Loại đệ theo mã. <b>Không bao giờ trả {@code null}.</b>
     *
     * <p>Chưa khai loại đó thì trả về một loại hệ số 100 — tức chỉ số y như đệ
     * thường. An toàn hơn là để chỗ gọi tự đoán, và người chơi vẫn có đệ dùng
     * được trong khi chờ quản trị khai thêm dòng.</p>
     */
    public static Loai loai(int loai) {
        for (Loai l : dsLoai()) {
            if (l.loai == loai && l.bat) {
                return l;
            }
        }
        Loai mac = new Loai();
        mac.loai = loai;
        mac.ten = "(chưa khai)";
        mac.heSo = 100d;
        return mac;
    }

    public static String luuLoai(Loai l) {
        damBaoBang();
        if (l == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (l.loai < 0) {
            return "Mã loại không được âm.";
        }
        if (l.heSo <= 0) {
            return "Hệ số phải lớn hơn 0.";
        }
        if (l.sucManhDau < 0) {
            return "Sức mạnh khởi điểm không được âm.";
        }
        try {
            ConnectDB.executeUpdate("INSERT INTO de_tu_loai"
                    + " (loai, ten, he_so, suc_manh_dau, tu_trung, bat, ghi_chu)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " he_so = VALUES(he_so), suc_manh_dau = VALUES(suc_manh_dau),"
                    + " tu_trung = VALUES(tu_trung), bat = VALUES(bat),"
                    + " ghi_chu = VALUES(ghi_chu)",
                    l.loai, nz(l.ten), l.heSo, l.sucManhDau,
                    l.tuTrung ? 1 : 0, l.bat ? 1 : 0, l.ghiChu);
            return null;
        } catch (Exception ex) {
            Logger.logException(DeTuDAO.class, ex, "Lỗi lưu de_tu_loai");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaLoai(int loai) {
        try {
            ConnectDB.executeUpdate("DELETE FROM de_tu_loai WHERE loai = ?", loai);
            return null;
        } catch (Exception ex) {
            Logger.logException(DeTuDAO.class, ex, "Lỗi xoá de_tu_loai");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception boQua) {
                // Dong that bai thi khong con gi de lam.
            }
        }
    }
}
