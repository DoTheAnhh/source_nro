package nro.repository.dao;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.entity.skill.Skill;

/**
 * <b>Danh mục tác dụng</b> dùng chung cho set kích hoạt và chỉ số trên đồ.
 *
 * <h2>Vì sao có lớp này</h2>
 *
 * <p>Trước đây panel dựng ô chọn bằng cách nhân mọi loại theo chiêu với mọi
 * chiêu — vài trăm dòng trong một ô thả xuống, lẫn cả những cặp vô nghĩa (chí
 * mạng cho chiêu Trị Thương) và những loại trùng nhau (né đòn có hai mã, sát
 * thương Quả Cầu Kênh Khí có hai mã). Lớp này nói rõ từng loại:</p>
 *
 * <ul>
 *   <li>thuộc <b>nhóm</b> nào — để chọn theo nhóm thay vì cuộn;</li>
 *   <li><b>đơn vị</b> của ô giá trị — %, điểm, mục tiêu;</li>
 *   <li>có đi theo <b>chiêu</b> không, và <b>chiêu nào</b> thì có nghĩa;</li>
 *   <li>có ô <b>thời gian</b> không — hiệu ứng phụ gom giá trị, thời gian và tỉ
 *       lệ vào một dòng;</li>
 *   <li>có <b>ẩn</b> không — mã cũ trùng nghĩa, chỉ giữ để dữ liệu cũ còn chạy
 *       (và được tự đổi sang mã mới).</li>
 * </ul>
 */
public final class LoaiChiSo {

    // ---- nhóm, theo thứ tự hiện trên panel ----
    public static final String N_CO_BAN = "Chỉ số cơ bản";
    public static final String N_CHI_MANG = "Chí mạng & xuyên giáp";
    public static final String N_PHONG_THU = "Phòng thủ";
    public static final String N_HOI_PHUC = "Hồi phục & hút";
    public static final String N_MUC_TIEU = "Sát thương theo mục tiêu";
    public static final String N_CHIEU = "Chiêu thức";
    public static final String N_HIEU_UNG = "Hiệu ứng phụ khi trúng chiêu";
    public static final String N_KINH_TE = "Kinh tế & rơi đồ";

    public static final String[] CAC_NHOM = {N_CO_BAN, N_CHI_MANG, N_PHONG_THU,
        N_HOI_PHUC, N_MUC_TIEU, N_CHIEU, N_HIEU_UNG, N_KINH_TE};

    // ---- nhóm chiêu ----

    /** Chiêu gây sát thương trực tiếp — chí mạng, xuyên giáp có nghĩa. */
    public static final int[] CHIEU_SAT_THUONG = {Skill.DRAGON, Skill.KAMEJOKO,
        Skill.DEMON, Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
        Skill.MAKANKOSAPPO, Skill.LIEN_HOAN, Skill.DICH_CHUYEN_TUC_THOI,
        Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG};

    /** Chiêu có sát thương (kể cả không chí mạng được: QCKK, Tự Sát, Đẻ Trứng). */
    public static final int[] CHIEU_CO_SAT_THUONG = {Skill.DRAGON, Skill.KAMEJOKO,
        Skill.DEMON, Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
        Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.DE_TRUNG, Skill.TU_SAT,
        Skill.LIEN_HOAN, Skill.DICH_CHUYEN_TUC_THOI, Skill.SUPER_KAME,
        Skill.LIEN_HOAN_CHUONG};

    /** Chiêu gây được hiệu ứng phụ lên mục tiêu trúng đòn. */
    public static final int[] CHIEU_HIEU_UNG = {Skill.DRAGON, Skill.KAMEJOKO,
        Skill.DEMON, Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
        Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.TU_SAT, Skill.LIEN_HOAN,
        Skill.DICH_CHUYEN_TUC_THOI, Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG};

    /** Chiêu có thời gian tác dụng (choáng, ngủ, trói, khiên, biến hình…). */
    public static final int[] CHIEU_THOI_GIAN = {Skill.THAI_DUONG_HA_SAN,
        Skill.DICH_CHUYEN_TUC_THOI, Skill.THOI_MIEN, Skill.TROI, Skill.SOCOLA,
        Skill.KHIEN_NANG_LUONG, Skill.BIEN_KHI, Skill.HUYT_SAO, Skill.DE_TRUNG};

    /** Chiêu có "sức mạnh" không phải sát thương: lượng hồi, % máu, % biến khỉ. */
    public static final int[] CHIEU_HIEU_LUC = {Skill.TRI_THUONG, Skill.HUYT_SAO,
        Skill.BIEN_KHI, Skill.TAI_TAO_NANG_LUONG};

    /** Chiêu đánh được nhiều mục tiêu. */
    public static final int[] CHIEU_NHIEU_MUC_TIEU = {Skill.DRAGON, Skill.KAMEJOKO,
        Skill.DEMON, Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
        Skill.MAKANKOSAPPO, Skill.SOCOLA, Skill.DICH_CHUYEN_TUC_THOI,
        Skill.THOI_MIEN, Skill.TROI, Skill.LIEN_HOAN, Skill.SUPER_KAME,
        Skill.LIEN_HOAN_CHUONG, Skill.MA_PHONG_BA};

    /** Chiêu có thời gian vận / ra đòn. */
    public static final int[] CHIEU_TOC_DO = {Skill.DRAGON, Skill.KAMEJOKO,
        Skill.DEMON, Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
        Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.DE_TRUNG, Skill.TU_SAT,
        Skill.LIEN_HOAN, Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG, Skill.MA_PHONG_BA};

    /** Mọi chiêu — hồi chiêu và làm mới áp được cho chiêu nào cũng có nghĩa. */
    public static final int[] MOI_CHIEU;

    static {
        List<Integer> ds = new ArrayList<>(SetBonusDAO.CHIEU.keySet());
        MOI_CHIEU = new int[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            MOI_CHIEU[i] = ds.get(i);
        }
    }

    /** Một loại tác dụng. */
    public static final class Loai {
        public final String ma;
        public final String nhom;
        public final String ten;
        /** Đơn vị ô giá trị: "%", "điểm", "mục tiêu". */
        public final String donVi;
        /** Chiêu hợp lệ; {@code null} = loại không đi theo chiêu. */
        public final int[] chieu;
        /** Có ô thời gian (giây) — hiệu ứng phụ. */
        public final boolean coThoiGian;
        /** Mã cũ trùng nghĩa: không hiện để chọn, chỉ giữ cho dữ liệu cũ chạy. */
        public final boolean an;

        Loai(String ma, String nhom, String ten, String donVi, int[] chieu,
                boolean coThoiGian, boolean an) {
            this.ma = ma;
            this.nhom = nhom;
            this.ten = ten;
            this.donVi = donVi;
            this.chieu = chieu;
            this.coThoiGian = coThoiGian;
            this.an = an;
        }

        public boolean theoChieu() {
            return chieu != null;
        }

        public boolean hopChieu(int id) {
            if (chieu == null) {
                return true;
            }
            for (int c : chieu) {
                if (c == id) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final Map<String, Loai> DS = new LinkedHashMap<>();

    private static void them(String ma, String nhom, String ten, String donVi) {
        DS.put(ma, new Loai(ma, nhom, ten, donVi, null, false, false));
    }

    private static void themChieu(String ma, String nhom, String ten, String donVi,
            int[] chieu, boolean coThoiGian) {
        DS.put(ma, new Loai(ma, nhom, ten, donVi, chieu, coThoiGian, false));
    }

    private static void an(String ma, String ten) {
        DS.put(ma, new Loai(ma, "(cũ)", ten, "%", null, false, true));
    }

    private static void anChieu(String ma, String ten, int[] chieu) {
        DS.put(ma, new Loai(ma, "(cũ)", ten, "%", chieu, false, true));
    }

    static {
        // ---- cơ bản
        them("hp", N_CO_BAN, "HP", "điểm");
        them("hp_pct", N_CO_BAN, "HP", "%");
        them("ki", N_CO_BAN, "KI", "điểm");
        them("ki_pct", N_CO_BAN, "KI", "%");
        them("dame", N_CO_BAN, "Sức đánh", "điểm");
        them("dame_pct", N_CO_BAN, "Sức đánh", "%");
        them("def", N_CO_BAN, "Giáp", "điểm");
        them("def_pct", N_CO_BAN, "Giáp", "%");
        // ---- chí mạng & xuyên giáp
        them("crit", N_CHI_MANG, "Tỉ lệ chí mạng", "%");
        them("sdcm", N_CHI_MANG, "Sát thương chí mạng", "%");
        them("chi_mang_ca_hai", N_CHI_MANG, "Chí mạng: cả tỉ lệ lẫn sát thương", "%");
        them("chinh_xac", N_CHI_MANG, "Chính xác (xuyên né đòn)", "%");
        them("xuyen_giap", N_CHI_MANG, "Xuyên giáp", "%");
        them("xuyen_giap_cm", N_CHI_MANG, "Xuyên giáp khi chí mạng", "%");
        // ---- phòng thủ
        them("giap_pct", N_PHONG_THU, "Giảm sát thương nhận vào", "%");
        them("ne_don", N_PHONG_THU, "Né đòn", "%");
        them("pst", N_PHONG_THU, "Phản sát thương", "%");
        them("fix_stun", N_PHONG_THU, "Giảm thời gian bị choáng (khi bật khiên)", "%");
        them("bom_pct", N_PHONG_THU, "Tỉ lệ phát nổ khi chết", "%");
        // ---- hồi phục & hút
        them("hp_hoi", N_HOI_PHUC, "HP hồi", "điểm");
        them("hp_hoi_pct", N_HOI_PHUC, "HP hồi mỗi 30 giây", "%");
        them("ki_hoi_pct", N_HOI_PHUC, "KI hồi mỗi 30 giây", "%");
        them("hut_hp", N_HOI_PHUC, "Hút HP", "%");
        them("hut_ki", N_HOI_PHUC, "Hút KI", "%");
        them("giam_tieu_hao_pct", N_HOI_PHUC, "Giảm HP/KI tiêu hao khi dùng chiêu", "%");
        // ---- theo mục tiêu
        them("dame_boss_pct", N_MUC_TIEU, "Sát thương lên Boss", "%");
        them("dame_mob_pct", N_MUC_TIEU, "Sát thương lên quái", "%");
        them("danh_thuong_pct", N_MUC_TIEU, "Sát thương đánh thường", "%");
        // ---- chiêu thức
        themChieu("skill_pct", N_CHIEU, "Sát thương chiêu", "%", CHIEU_CO_SAT_THUONG, false);
        themChieu("skill_crit_pct", N_CHIEU, "Tỉ lệ chí mạng chiêu", "%",
                concat(CHIEU_SAT_THUONG, Skill.DE_TRUNG), false);
        themChieu("skill_sdcm_pct", N_CHIEU, "Sát thương chí mạng chiêu", "%",
                concat(CHIEU_SAT_THUONG, Skill.DE_TRUNG), false);
        themChieu("skill_xuyen_giap_pct", N_CHIEU, "Xuyên giáp chiêu", "%",
                CHIEU_SAT_THUONG, false);
        themChieu("skill_power_pct", N_CHIEU,
                "Hiệu lực chiêu (lượng hồi, % máu, % biến khỉ)", "%", CHIEU_HIEU_LUC, false);
        themChieu("skill_duration_pct", N_CHIEU, "Thời gian tác dụng chiêu", "%",
                CHIEU_THOI_GIAN, false);
        themChieu("skill_target_add", N_CHIEU, "Thêm mục tiêu cho chiêu", "mục tiêu",
                CHIEU_NHIEU_MUC_TIEU, false);
        themChieu("skill_cast_speed_pct", N_CHIEU, "Tốc độ ra đòn chiêu", "%",
                CHIEU_TOC_DO, false);
        themChieu("hoi_chieu_skill_pct", N_CHIEU, "Giảm hồi chiêu", "%", MOI_CHIEU, false);
        themChieu("lam_moi_pct", N_CHIEU, "Tỉ lệ làm mới chiêu sau khi dùng", "%",
                MOI_CHIEU, false);
        themChieu("troi_giam_giap_pct", N_CHIEU, "Khi Trói: giảm giáp mục tiêu", "%",
                new int[]{Skill.TROI}, false);
        them("hoi_chieu_pct", N_CHIEU, "Giảm hồi chiêu (mọi chiêu)", "%");
        // ---- hiệu ứng phụ: giá trị + thời gian + tỉ lệ trong MỘT dòng
        themChieu("skill_burn_hp_pct", N_HIEU_UNG, "Thiêu đốt: % HP tối đa mỗi giây", "%",
                CHIEU_HIEU_UNG, true);
        themChieu("skill_slow_pct", N_HIEU_UNG, "Làm chậm tốc chạy mục tiêu", "%",
                CHIEU_HIEU_UNG, true);
        themChieu("skill_attack_slow_pct", N_HIEU_UNG, "Làm chậm tốc ra đòn mục tiêu", "%",
                CHIEU_HIEU_UNG, true);
        themChieu("skill_weaken_pct", N_HIEU_UNG, "Giảm sát thương mục tiêu gây ra", "%",
                CHIEU_HIEU_UNG, true);
        themChieu("skill_stun_chance_pct", N_HIEU_UNG, "Gây choáng: tỉ lệ", "%",
                CHIEU_HIEU_UNG, true);
        // ---- kinh tế
        them("may_man", N_KINH_TE, "May mắn", "%");
        them("gold_pct", N_KINH_TE, "Vàng rơi", "%");
        them("tiem_nang_pct", N_KINH_TE, "Tiềm năng nhận được", "%");

        // ---- mã cũ trùng nghĩa: ẩn, tự đổi sang mã mới (SetBonusDAO.gopLoaiTrung)
        an("ne_don_pct", "Né đòn (mã cũ, đã gộp vào Né đòn)");
        an("qckk_pct", "Sát thương Quả Cầu Kênh Khí (mã cũ)");
        an("detrung_dame_pct", "Sát thương Đẻ Trứng (mã cũ)");
        an("choang_pct", "Thời gian choáng Dịch Chuyển Tức Thời (mã cũ)");
        an("choang_tdhs_pct", "Thời gian choáng Thái Dương Hạ San (mã cũ)");
        an("tai_tao_pct", "Hồi phục Tái Tạo Năng Lượng (mã cũ)");
        // Tỉ lệ và thời gian hiệu ứng phụ nay nằm ngay trong dòng hiệu ứng. Hai mã
        // này vẫn chạy (đồ cũ có thể mang chúng) nhưng không hiện để chọn cho set.
        anChieu("skill_debuff_chance_pct", "Tỉ lệ kích hoạt hiệu ứng phụ (mã cũ)", CHIEU_HIEU_UNG);
        anChieu("skill_debuff_duration_pct", "Thời gian hiệu ứng phụ + % (mã cũ)", CHIEU_HIEU_UNG);
    }

    private static int[] concat(int[] a, int b) {
        int[] r = new int[a.length + 1];
        System.arraycopy(a, 0, r, 0, a.length);
        r[a.length] = b;
        return r;
    }

    private LoaiChiSo() {
    }

    public static Loai get(String ma) {
        return ma == null ? null : DS.get(ma);
    }

    /** Mọi loại, kể cả loại ẩn. */
    public static List<Loai> tatCa() {
        return Collections.unmodifiableList(new ArrayList<>(DS.values()));
    }

    /** Loại hiện để chọn, theo thứ tự nhóm. */
    public static List<Loai> dangDung() {
        List<Loai> r = new ArrayList<>();
        for (String n : CAC_NHOM) {
            for (Loai l : DS.values()) {
                if (!l.an && n.equals(l.nhom)) {
                    r.add(l);
                }
            }
        }
        return r;
    }

    /** Tên ngắn của chiêu, bỏ tiền tố hành tinh ("Trái Đất — Kamejoko" → "Kamejoko"). */
    public static String tenChieu(int id) {
        // De Trung khong tu danh: moi chi so "cua chieu" nay la cua con PET no
        // goi ra (sat thuong, chi mang tung don pet danh). Ghi ro cho khoi hieu nham.
        if (id == Skill.DE_TRUNG) {
            return "Đẻ Trứng (pet)";
        }
        String t = SetBonusDAO.CHIEU.get(id);
        if (t == null) {
            return "Chiêu " + id;
        }
        int i = t.indexOf("—");
        String s = i >= 0 ? t.substring(i + 1).trim() : t.trim();
        int ngoac = s.indexOf(" (");
        return ngoac > 0 ? s.substring(0, ngoac) : s;
    }

    /** Nhãn đầy đủ một lựa chọn: "Sát thương chiêu — Kamejoko (%)". */
    public static String nhan(Loai l, int chieu) {
        String dv = "%".equals(l.donVi) ? " (%)" : " (" + l.donVi + ")";
        return l.theoChieu() ? l.ten + " — " + tenChieu(chieu) + dv : l.ten + dv;
    }

    /**
     * Bỏ dấu tiếng Việt, hạ chữ thường — để tìm "de trung" ra "Đẻ Trứng".
     *
     * <p>{@code Normalizer} thay vì bảng đổi tay: bảng tay bao giờ cũng thiếu
     * vài chữ, mà thiếu chữ nào thì tìm chữ đó lặng lẽ không ra.</p>
     */
    public static String boDau(String s) {
        if (s == null) {
            return "";
        }
        String t = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return t.replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    /**
     * Khớp tìm kiếm: mọi từ gõ vào đều phải có trong chuỗi (không dấu, không
     * phân biệt hoa thường, không cần đúng thứ tự). Gõ "kame chi mang" ra "Tỉ lệ
     * chí mạng chiêu — Kamejoko".
     */
    public static boolean khop(String chuoi, String go) {
        String k = boDau(go);
        if (k.isEmpty()) {
            return true;
        }
        String c = boDau(chuoi);
        for (String tu : k.split("\\s+")) {
            if (!tu.isEmpty() && !c.contains(tu)) {
                return false;
            }
        }
        return true;
    }
}
