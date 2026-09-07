package nro.entity.combine.list;

import nro.core.consts.ConstNpc;
import nro.entity.player.Player;
import nro.service.combine.CongThucDoiService;

/**
 * "Đóng thành Sách cũ" của Bà Hạt Mít.
 *
 * <p>Nguyên liệu, tỉ lệ, kết quả và phần hao khi thất bại <b>không còn viết cứng
 * ở đây</b> — tất cả đọc từ bảng {@code cong_thuc_doi}, mã
 * {@code DONG_THANH_SACH_CU}, sửa được trên panel.</p>
 */
public class CheTaoCuonSachCu {

    /** Mã công thức trong bảng {@code cong_thuc_doi}. */
    public static final String MA = "DONG_THANH_SACH_CU";

    public static void showCombine(Player player) {
        CongThucDoiService.gI().hienBang(player, MA, ConstNpc.DONG_THANH_SACH_CU);
    }

    public static void cheTaoCuonSachCu(Player player) {
        CongThucDoiService.gI().thucHien(player, MA);
    }
}
