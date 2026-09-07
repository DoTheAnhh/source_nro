package nro.entity.badges;

/*
 * Author Dev By DoTheAnh
 */

public class BadgesTaskTemplate {

    // ---- Các loại nhiệm vụ đếm được tự động ----
    //
    // Loại rỗng (hoặc "go_cung") nghĩa là nhiệm vụ cũ: tiến độ do mã nguồn tự
    // gọi updateCountBagesTask ở đúng chỗ, panel chỉ sửa được tên/số lần/thưởng.

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
                return "Giết quái";
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
                return "Tiêu vàng";
            case TIEU_NGOC:
                return "Tiêu ngọc";
            default:
                return "Mã nguồn tự đếm";
        }
    }

    /** Các loại chọn được trên panel, theo đúng thứ tự hiện. */
    public static String[] cacLoai() {
        return new String[]{GO_CUNG, GIET_QUAI, GIET_BOSS, HA_NGUOI_CHOI,
            MUA_VAT_PHAM, DUNG_VAT_PHAM, NHAT_VAT_PHAM, TIEU_VANG, TIEU_NGOC};
    }
}






