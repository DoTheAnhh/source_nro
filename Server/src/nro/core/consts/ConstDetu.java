package nro.core.consts;

/**
 *
 * @author DoTheAnh
 */
public class ConstDetu {

    public static final byte NORMAL = 0;
    public static final byte MABU = 1;
    //PET NEW
    public static final byte U_BU = 2;
    public static final byte KID_JIREN = 3;
    public static final byte KID_BEER = 4;
    public static final byte BLACK = 5;

    // ---------------- Đệ săn boss, nhận từ trứng ----------------
    /** Đệ Cell — mạnh hơn Mabư một bậc. */
    public static final byte CELL = 6;

    /** Đệ Bill — mạnh hơn Cell một bậc. */
    public static final byte BILL = 7;

    /**
     * Sức mạnh khởi điểm của ba loại đệ nhận từ trứng.
     *
     * <p>Đệ thường bắt đầu từ 2.000; ba loại này bắt đầu ở 1,5 triệu nên vào
     * việc được ngay, không phải nuôi từ đầu.</p>
     */
    public static final long SUC_MANH_TRUNG = 1_500_000L;

    /**
     * Hệ số chỉ số theo loại đệ.
     *
     * <h3>Cộng dồn 5% mỗi bậc</h3>
     *
     * <p>Mabư hơn thường 5%, Cell hơn Mabư 5%, Bill hơn Cell 5% — <b>nhân dồn</b>
     * chứ không cộng thẳng, nên Bill hơn thường 15,7625% không phải 15%. Nhân dồn
     * mới đúng nghĩa "hơn bậc liền trước 5%".</p>
     *
     * <p>Loại không nằm trong ba loại trên thì trả về 1: đệ thường và các loại cũ
     * (U_BU, KID_JIREN…) giữ nguyên chỉ số như trước.</p>
     */
    public static double heSoChiSo(byte loai) {
        switch (loai) {
            case MABU:
                return 1.05d;
            case CELL:
                return 1.05d * 1.05d;
            case BILL:
                return 1.05d * 1.05d * 1.05d;
            default:
                return 1d;
        }
    }

    /** Tên hiện trước tên đệ, để người chơi biết đang có loại nào. */
    public static String tenLoai(byte loai) {
        switch (loai) {
            case MABU:
                return "Mabư";
            case CELL:
                return "Cell";
            case BILL:
                return "Bill";
            default:
                return "Đệ tử";
        }
    }
}





