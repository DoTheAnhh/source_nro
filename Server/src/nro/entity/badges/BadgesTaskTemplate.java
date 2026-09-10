package nro.entity.badges;

/*
 * Author Dev By DoTheAnh
 */

public class BadgesTaskTemplate {

    // ---- Các loại nhiệm vụ đếm được tự động ----
    //
    // Loại rỗng (hoặc "go_cung") nghĩa là nhiệm vụ cũ: tiến độ do mã nguồn tự
    // gọi updateCountBagesTask ở đúng chỗ, panel chỉ sửa được tên/số lần/thưởng.
    //
    // Mỗi loại TỰ ĐẾM có ít nhất một chỗ gọi tangTheoLoai() trong mã nguồn, đặt
    // ngay tại chỗ việc đó thật sự xảy ra. Thêm một hằng số ở đây mà quên cắm
    // móc thì nhiệm vụ hiện ra bình thường nhưng đứng mãi ở 0 — và không có gì
    // báo vì sao. Đừng thêm loại trước khi có chỗ gọi.

    /** Do mã nguồn tự đếm — không tự động bắt được. */
    public static final String GO_CUNG = "go_cung";
    /** Giết quái. {@code thamSo} = id mẫu quái, {@code -1} là quái nào cũng tính. */
    public static final String GIET_QUAI = "giet_quai";
    /** Hạ boss. {@code thamSo} = id boss, {@code -1} là boss nào cũng tính. */
    public static final String GIET_BOSS = "giet_boss";
    /** Hạ người chơi khác. {@code thamSo} không dùng. */
    public static final String HA_NGUOI_CHOI = "ha_nguoi_choi";
    /** Mua vật phẩm ở cửa hàng. {@code thamSo} = id vật phẩm, {@code -1} là mọi món. */
    public static final String MUA_VAT_PHAM = "mua_vat_pham";
    /** Dùng vật phẩm. {@code thamSo} = id vật phẩm. */
    public static final String DUNG_VAT_PHAM = "dung_vat_pham";
    /** Nhặt vật phẩm rơi dưới đất. {@code thamSo} = id vật phẩm, {@code -1} là mọi món. */
    public static final String NHAT_VAT_PHAM = "nhat_vat_pham";
    /** Tiêu vàng. Mỗi đồng vàng tính một điểm. */
    public static final String TIEU_VANG = "tieu_vang";
    /** Nạp thỏi vàng / ngọc. Mỗi đơn vị tính một điểm. */
    public static final String TIEU_NGOC = "tieu_ngoc";

    /** Tới một bản đồ. {@code thamSo} = id bản đồ, {@code -1} là bản đồ nào cũng tính. */
    public static final String DEN_BAN_DO = "den_ban_do";
    /** Đập pha lê hóa thành công. {@code thamSo} = số sao đạt được, {@code -1} là bất kỳ. */
    public static final String DAP_SAO = "dap_sao";
    /** Nâng cấp bông tai thành công. {@code thamSo} không dùng. */
    public static final String NANG_BONG_TAI = "nang_bong_tai";
    /** Mở được một nội tại. {@code thamSo} = id nội tại, {@code -1} là bất kỳ. */
    public static final String MO_NOI_TAI = "mo_noi_tai";
    /** Hợp thể với đệ tử. {@code thamSo} không dùng. */
    public static final String HOP_THE = "hop_the";
    /** Câu được một con cá. {@code thamSo} = loại cá, {@code -1} là bất kỳ. */
    public static final String CAU_CA = "cau_ca";
    /** Ăn đậu thần. {@code thamSo} không dùng. */
    public static final String AN_DAU = "an_dau";
    /** Bị hạ gục. {@code thamSo} không dùng. */
    public static final String CHET = "chet";
    /** Xong một nhiệm vụ chính tuyến. {@code thamSo} = id nhiệm vụ, {@code -1} là bất kỳ. */
    public static final String XONG_NHIEM_VU = "xong_nhiem_vu";
    /** Đổi số dư đã nạp ra vật phẩm. Mỗi 1.000 VND tính một điểm. */
    public static final String NAP_THE = "nap_the";

    public int id;
    public String name;
    public int count;
    public int idbadgesReward;

    /** Loại nhiệm vụ — một trong các hằng số ở trên. */
    public String loai = GO_CUNG;

    /** Tham số của loại: id quái, id boss, id vật phẩm… {@code -1} là bất kỳ. */
    public int thamSo = -1;

    public BadgesTaskTemplate() {
        id = -1;
        name = "";
        count = -1;
        idbadgesReward = -1;
    }

    /** Nhiệm vụ này có tự đếm được không. */
    public boolean tuDem() {
        return loai != null && !loai.trim().isEmpty() && !GO_CUNG.equals(loai);
    }

    /** Tên loại cho người đọc panel. */
    public static String tenLoai(String loai) {
        if (loai == null) {
            return "Mã nguồn tự đếm";
        }
        switch (loai) {
            case GIET_QUAI:
                return "Tiêu diệt quái";
            case GIET_BOSS:
                return "Hạ boss";
            case HA_NGUOI_CHOI:
                return "Hạ người chơi";
            case MUA_VAT_PHAM:
                return "Mua vật phẩm";
            case DUNG_VAT_PHAM:
                return "Dùng vật phẩm";
            case NHAT_VAT_PHAM:
                return "Nhặt vật phẩm";
            case TIEU_VANG:
                return "Tiêu vàng (mỗi đồng một điểm)";
            case TIEU_NGOC:
                return "Tiêu ngọc (mỗi viên một điểm)";
            case DEN_BAN_DO:
                return "Tới bản đồ";
            case DAP_SAO:
                return "Đập pha lê hóa thành công";
            case NANG_BONG_TAI:
                return "Nâng bông tai thành công";
            case MO_NOI_TAI:
                return "Mở nội tại";
            case HOP_THE:
                return "Hợp thể với đệ tử";
            case CAU_CA:
                return "Câu được cá";
            case AN_DAU:
                return "Ăn đậu thần";
            case CHET:
                return "Bị hạ gục";
            case XONG_NHIEM_VU:
                return "Xong nhiệm vụ chính tuyến";
            case NAP_THE:
                return "Đổi tiền đã nạp (mỗi 1.000đ một điểm)";
            default:
                return "Mã nguồn tự đếm";
        }
    }

    /**
     * Ý nghĩa của ô "Tham số" với từng loại, để panel nói được thay vì bắt đoán.
     *
     * <p>Chuỗi rỗng nghĩa là loại đó <b>không dùng</b> tham số.</p>
     */
    public static String yNghiaThamSo(String loai) {
        if (loai == null) {
            return "";
        }
        switch (loai) {
            case GIET_QUAI:
                return "Id mẫu quái — để -1 là quái nào cũng tính";
            case GIET_BOSS:
                return "Id boss — để -1 là boss nào cũng tính";
            case MUA_VAT_PHAM:
            case DUNG_VAT_PHAM:
            case NHAT_VAT_PHAM:
                return "Id vật phẩm — để -1 là món nào cũng tính";
            case DEN_BAN_DO:
                return "Id bản đồ — để -1 là bản đồ nào cũng tính";
            case DAP_SAO:
                return "Số sao đạt được — để -1 là lên sao nào cũng tính";
            case MO_NOI_TAI:
                return "Id nội tại — để -1 là nội tại nào cũng tính";
            case CAU_CA:
                return "Loại cá — để -1 là con nào cũng tính";
            case XONG_NHIEM_VU:
                return "Id nhiệm vụ — để -1 là nhiệm vụ nào cũng tính";
            default:
                return "";
        }
    }

    /** Các loại chọn được trên panel, theo đúng thứ tự hiện. */
    public static String[] cacLoai() {
        return new String[]{GO_CUNG, GIET_QUAI, GIET_BOSS, HA_NGUOI_CHOI,
            MUA_VAT_PHAM, DUNG_VAT_PHAM, NHAT_VAT_PHAM, TIEU_VANG, TIEU_NGOC,
            DEN_BAN_DO, DAP_SAO, NANG_BONG_TAI, MO_NOI_TAI, HOP_THE, CAU_CA,
            AN_DAU, CHET, XONG_NHIEM_VU, NAP_THE};
    }

    /**
     * Vài <b>mẫu có sẵn</b> để bấm một cái là ra một cách nhận hoàn chỉnh.
     *
     * <p>Có bảng này vì phần khó của việc đặt một cách nhận không phải là gõ
     * chữ, mà là biết loại nào đếm được và con số nào thì hợp lý. Mỗi mẫu ở đây
     * là một cặp (loại, số lần) đã cân sẵn, người dùng chỉ việc chọn rồi sửa lại
     * con số nếu muốn.</p>
     */
    public static final class Mau {

        public final String ten;
        public final String loai;
        public final int soLan;
        public final int thamSo;

        Mau(String ten, String loai, int soLan, int thamSo) {
            this.ten = ten;
            this.loai = loai;
            this.soLan = soLan;
            this.thamSo = thamSo;
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    public static Mau[] cacMau() {
        return new Mau[]{
            new Mau("Tiêu diệt 100 quái bất kỳ", GIET_QUAI, 100, -1),
            new Mau("Tiêu diệt 1.000 quái bất kỳ", GIET_QUAI, 1000, -1),
            new Mau("Tiêu diệt 10.000 quái bất kỳ", GIET_QUAI, 10000, -1),
            new Mau("Tiêu diệt 100.000 quái bất kỳ", GIET_QUAI, 100000, -1),
            new Mau("Hạ 10 boss bất kỳ", GIET_BOSS, 10, -1),
            new Mau("Hạ 100 boss bất kỳ", GIET_BOSS, 100, -1),
            new Mau("Hạ 1.000 boss bất kỳ", GIET_BOSS, 1000, -1),
            new Mau("Hạ 10 người chơi", HA_NGUOI_CHOI, 10, -1),
            new Mau("Hạ 100 người chơi", HA_NGUOI_CHOI, 100, -1),
            new Mau("Hạ 1.000 người chơi", HA_NGUOI_CHOI, 1000, -1),
            new Mau("Mua 100 vật phẩm bất kỳ", MUA_VAT_PHAM, 100, -1),
            new Mau("Dùng 100 vật phẩm bất kỳ", DUNG_VAT_PHAM, 100, -1),
            new Mau("Nhặt 500 vật phẩm rơi", NHAT_VAT_PHAM, 500, -1),
            new Mau("Tiêu 1 tỉ vàng", TIEU_VANG, 1000000000, -1),
            new Mau("Tiêu 10 tỉ vàng", TIEU_VANG, Integer.MAX_VALUE, -1),
            new Mau("Tiêu 1.000 ngọc", TIEU_NGOC, 1000, -1),
            new Mau("Đi qua 100 lượt bản đồ", DEN_BAN_DO, 100, -1),
            new Mau("Đập lên ★7 một lần", DAP_SAO, 1, 7),
            new Mau("Đập pha lê hóa thành công 50 lần", DAP_SAO, 50, -1),
            new Mau("Nâng bông tai thành công 10 lần", NANG_BONG_TAI, 10, -1),
            new Mau("Mở nội tại 100 lần", MO_NOI_TAI, 100, -1),
            new Mau("Hợp thể với đệ 50 lần", HOP_THE, 50, -1),
            new Mau("Câu được 100 con cá", CAU_CA, 100, -1),
            new Mau("Ăn 500 hạt đậu thần", AN_DAU, 500, -1),
            new Mau("Bị hạ gục 100 lần", CHET, 100, -1),
            new Mau("Xong 10 nhiệm vụ chính tuyến", XONG_NHIEM_VU, 10, -1),
            new Mau("Đổi 100.000đ tiền nạp", NAP_THE, 100, -1),
            new Mau("Đổi 1.000.000đ tiền nạp", NAP_THE, 1000, -1),
            new Mau("Tự đặt (mã nguồn tự đếm)", GO_CUNG, 1, -1)
        };
    }
}
