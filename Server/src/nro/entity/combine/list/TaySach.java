package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.repository.dao.SachTuyetKyDAO;
import nro.service.combine.CombineService;
import java.util.HashSet;
import java.util.Set;

/**
 * Tẩy Sách Tuyệt Kỹ: trả các dòng chỉ số đã giám định về "- Chưa giám định".
 *
 * <h2>Vì sao viết lại</h2>
 *
 * <p>Bản cũ duyệt danh sách chỉ số từ đầu và <b>dừng ở dòng đầu tiên mang
 * option 21</b>, thay mọi dòng đứng trước nó bằng dòng chưa giám định. Nhưng
 * thứ tự chỉ số trên một cuốn sách không cố định — nó do công thức ghép và các
 * lần nâng cấp quyết định. Cuốn nào có option 21 nằm ngay đầu thì vòng lặp
 * <b>thoát ở vòng đầu tiên</b>, không tẩy được dòng nào, mà
 * {@code subOptionParam(219, 1)} ngay dưới <b>vẫn trừ một lần tẩy</b>.</p>
 *
 * <p>Người chơi thấy đúng điều đó: mất lần tẩy, chỉ số cũ còn nguyên.</p>
 *
 * <h2>Cách làm mới</h2>
 *
 * <p>Tẩy là <b>phép nghịch đảo của giám định</b>, nên nó đảo đúng những gì giám
 * định làm ra: {@link GiamDinhSach} thay mỗi dòng 217 bằng một chỉ số bốc từ kho
 * trên panel, nên tẩy đưa những dòng <i>thuộc kho ấy</i> về lại 217.</p>
 *
 * <p>Xét theo <b>id chỉ số</b> chứ không theo vị trí. Vị trí thì đổi, còn kho
 * chỉ số là danh sách quản trị viên tự khai nên nó chính xác là tập những dòng
 * có thể tẩy được. Dòng nào không nằm trong kho — độ bền, số lần tẩy, yêu cầu
 * sức mạnh, khoá — không bị đụng tới, dù nằm ở đâu.</p>
 *
 * <p>Và <b>không trừ lần tẩy khi không tẩy được gì</b>.</p>
 */
public class TaySach {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        Item sachTuyetKy = player.combine.itemsCombine.get(0);
        if (sachTuyetKy == null || !sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2()) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        int conLai = sachTuyetKy.getOptionParam(219);
        if (conLai <= 0) {
            Service.gI().sendDialogMessage(player,
                    "Cuốn này đã hết lượt tẩy.");
            return;
        }
        int soDong = demDongTayDuoc(sachTuyetKy);
        if (soDong <= 0) {
            Service.gI().sendDialogMessage(player,
                    "Cuốn này chưa có dòng nào đã giám định để tẩy.");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player,
                ConstNpc.MENU_START_COMBINE,
                ConstFont.BOLD_BLUE + "Tẩy Sách Tuyệt Kỹ ?\n"
                + "Sẽ xoá " + soDong + " dòng chỉ số, còn lại "
                + (conLai - 1) + " lượt tẩy.",
                "Đồng ý", "Từ chối");
    }

    public static void taySach(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendServerMessage(player, "Cần đúng một Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        Item sachTuyetKy = player.combine.itemsCombine.get(0);
        if (sachTuyetKy == null || !sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2()) {
            Service.gI().sendServerMessage(player, "Vật phẩm này không phải Sách Tuyệt Kỹ.");
            return;
        }
        if (sachTuyetKy.getOptionParam(219) <= 0) {
            Service.gI().sendServerMessage(player, "Cuốn này đã hết lượt tẩy.");
            return;
        }
        if (sachTuyetKy.isHaveOption(SachTuyetKyDAO.OPTION_CHUA_GIAM_DINH)) {
            Service.gI().sendServerMessage(player,
                    "Giám định hết các dòng rồi hãy tẩy.");
            return;
        }

        Set<Integer> kho = idChiSoTayDuoc();
        int daTay = 0;
        for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
            ItemOption io = sachTuyetKy.itemOptions.get(i);
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            if (kho.contains(io.optionTemplate.id)) {
                sachTuyetKy.itemOptions.set(i,
                        new ItemOption(SachTuyetKyDAO.OPTION_CHUA_GIAM_DINH, 0));
                daTay++;
            }
        }

        // Khong tay duoc dong nao thi KHONG tru luot.
        //
        // Day chinh la loi cu: no tru truoc, xet sau — hoac khong xet gi ca.
        if (daTay == 0) {
            Service.gI().sendServerMessage(player,
                    "Cuốn này không có dòng nào tẩy được, chưa trừ lượt tẩy.");
            return;
        }

        sachTuyetKy.subOptionParam(219, 1);
        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Đã tẩy " + daTay
                + " dòng chỉ số, còn " + sachTuyetKy.getOptionParam(219)
                + " lượt tẩy.");
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    /**
     * Tập id chỉ số mà giám định có thể bốc ra — tức tập tẩy được.
     *
     * <p>Đọc từ kho trên panel, kể cả dòng đang tắt: một chỉ số vừa bị tắt vẫn
     * còn nằm trên những cuốn sách giám định từ trước, và người chơi phải tẩy
     * được nó.</p>
     */
    private static Set<Integer> idChiSoTayDuoc() {
        Set<Integer> ids = new HashSet<>();
        for (SachTuyetKyDAO.ChiSo cs : SachTuyetKyDAO.danhSachChiSo()) {
            if (cs != null) {
                ids.add(cs.optionId);
            }
        }
        return ids;
    }

    private static int demDongTayDuoc(Item sach) {
        if (sach == null || sach.itemOptions == null) {
            return 0;
        }
        Set<Integer> kho = idChiSoTayDuoc();
        int n = 0;
        for (ItemOption io : sach.itemOptions) {
            if (io != null && io.optionTemplate != null
                    && kho.contains(io.optionTemplate.id)) {
                n++;
            }
        }
        return n;
    }
}
