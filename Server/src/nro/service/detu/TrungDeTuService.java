package nro.service.detu;

import java.util.HashMap;
import java.util.Map;
import nro.core.consts.ConstDetu;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.repository.dao.DeTuDAO;
import nro.repository.dao.TrungDeTuDAO;
import nro.service.DetuService;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.NpcService;

/**
 * Dùng <b>trứng đệ tử</b>: Trứng Mabư, Trứng Cell, Trứng Berus.
 *
 * <h2>Một quả trứng = một con đệ mới toanh</h2>
 *
 * <p>Nở trứng <b>thay hẳn</b> con đệ đang có, kể cả trang bị nó đang mặc. Vì
 * thế phải hỏi lại trước khi nở — mất một con đệ đã nuôi mà không kịp kêu thì
 * không lấy lại được.</p>
 *
 * <h2>Chỉ số lấy từ panel</h2>
 *
 * <p>Ở đây không có một con số chỉ số nào. Đệ vừa nở đi qua đúng đường mà mọi
 * con đệ khác đi ({@code DetuService.taoDeTuTuTrung}), tức là bốc theo khoảng
 * sơ sinh trong {@code de_tu_chi_so} rồi nhân hệ số của dòng loại tương ứng
 * trong {@code de_tu_loai} — hai bảng quản trị sửa ngay trên panel. Chép lại
 * mấy con số ấy vào đây là sớm muộn lệch với panel, và lệch kiểu đó thì không
 * có gì báo.</p>
 */
public final class TrungDeTuService {

    private static TrungDeTuService i;

    public static TrungDeTuService gI() {
        if (i == null) {
            i = new TrungDeTuService();
        }
        return i;
    }

    /** Ai đang mở bảng hỏi, và hỏi về quả trứng nào. */
    private final Map<Long, Integer> dangHoi = new HashMap<>();

    /**
     * Người chơi vừa dùng một vật phẩm — có phải trứng đệ tử không.
     *
     * @return {@code true} nếu đã xử lý, tức chỗ gọi đừng xét tiếp
     */
    public boolean dungVatPham(Player pl, Item item) {
        if (pl == null || item == null || !item.isNotNullItem()) {
            return false;
        }
        byte loai = TrungDeTuDAO.loaiCuaVatPham(item.template.id);
        if (loai < 0) {
            return false;
        }
        moBangHoi(pl, item.template.id, loai);
        return true;
    }

    /** Bảng xác nhận: nói rõ đệ cũ sẽ mất trước khi hỏi. */
    private void moBangHoi(Player pl, int itemId, byte loai) {
        dangHoi.put(pl.id, itemId);
        DeTuDAO.Loai lo = DeTuDAO.loai(loai);
        StringBuilder s = new StringBuilder();
        s.append("|1|").append(TrungDeTuDAO.tenTrung(loai)).append('\n');
        s.append("|0|Nở ra đệ tử ").append(lo.ten)
                .append(" — chỉ số bằng ").append(golon(lo.heSo))
                .append("% đệ thường\n");
        if (pl.Detu != null) {
            s.append("|2|Đệ tử hiện tại (")
                    .append(ConstDetu.tenLoai(pl.Detu.typeDeTu))
                    .append(") sẽ MẤT, kể cả đồ nó đang mặc");
        } else {
            s.append("|0|Bạn chưa có đệ tử nào");
        }
        NpcService.gI().createMenuConMeo(pl, ConstNpc.NO_TRUNG_DE_TU, -1,
                s.toString(), "Nở\ntrứng", "Từ chối");
    }

    /** Bỏ số 0 thừa sau dấu phẩy của hệ số, cho đỡ rối mắt. */
    private static String golon(double heSo) {
        String s = String.valueOf(Math.round(heSo * 100d) / 100d);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    /** Người chơi chọn ở bảng xác nhận. */
    public void chon(Player pl, int select) {
        Integer itemId = dangHoi.remove(pl.id);
        if (select != 0 || itemId == null) {
            return;
        }
        byte loai = TrungDeTuDAO.loaiCuaVatPham(itemId);
        if (loai < 0) {
            Service.gI().sendThongBao(pl, "Quả trứng này không còn dùng được.");
            return;
        }
        Item trung = timTrong(pl, itemId);
        if (trung == null) {
            // Doi day tui hay vut mat giua chung bang hoi: khong the tao de tu
            // tu mot qua trung khong con nam trong tui.
            Service.gI().sendThongBao(pl, "Không thấy quả trứng trong hành trang.");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, trung, 1);
        InventoryService.gI().sendItemBag(pl);
        DetuService.gI().taoDeTuTuTrung(pl, loai);
        Service.gI().sendThongBao(pl, "Trứng đã nở! Bạn nhận được đệ tử "
                + ConstDetu.tenLoai(loai) + ".");
    }

    /** Tìm lại quả trứng trong hành trang theo mã vật phẩm. */
    private static Item timTrong(Player pl, int itemId) {
        if (pl.inventory == null || pl.inventory.itemsBag == null) {
            return null;
        }
        for (Item it : pl.inventory.itemsBag) {
            if (it != null && it.isNotNullItem() && it.template.id == itemId
                    && it.quantity > 0) {
                return it;
            }
        }
        return null;
    }
}
