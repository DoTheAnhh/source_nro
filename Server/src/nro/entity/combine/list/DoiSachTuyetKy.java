package nro.entity.combine.list;

import nro.core.consts.ConstNpc;
import nro.entity.player.Player;
import nro.service.combine.CongThucDoiService;

/**
 * "Đổi Sách Tuyệt kỹ" của Bà Hạt Mít.
 *
 * <p>Đọc từ bảng {@code cong_thuc_doi}, mã {@code DOI_SACH_TUYET_KY}.</p>
 *
 * <p><b>Sửa luôn một lỗi cũ:</b> bản viết cứng hiển thị "Tỉ lệ thành công: 20%"
 * nhưng code lại chạy {@code Util.isTrue(50, 100)} — tức 50%. Nay chữ hiển thị
 * lấy từ chính con số dùng để bốc nên không lệch nhau được nữa. Giá trị nạp vào
 * bảng là <b>50</b>, giữ đúng tỉ lệ người chơi đang thực nhận.</p>
 */
public class DoiSachTuyetKy {

    /** Mã công thức trong bảng {@code cong_thuc_doi}. */
    public static final String MA = "DOI_SACH_TUYET_KY";

    public static void showCombine(Player player) {
        CongThucDoiService.gI().hienBang(player, MA, ConstNpc.DOI_SACH_TUYET_KY);
    }

    public static void doiSachTuyetKy(Player player) {
        CongThucDoiService.gI().thucHien(player, MA);
    }
}
