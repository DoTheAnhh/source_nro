package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.combine.CombineService;

public class GiamDinhSach {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ và bùa giám định.");
            return;
        }
        Item sachTuyetKy = null;
        Item buaGiamDinh = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isSachTuyetKy() || item.isSachTuyetKy2()) {
                sachTuyetKy = item;
            } else if (item.template.id == 1284) {
                buaGiamDinh = item;
            }
        }
        if (sachTuyetKy == null || buaGiamDinh == null) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ và bùa giám định.");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_GREEN).append("Giám định ").append(sachTuyetKy.template.name).append(" ?\n");
        text.append(ConstFont.BOLD_BLUE).append("Bùa giám định ").append(buaGiamDinh.quantity).append("/1");
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Giám định", "Từ chối");
    }

    public static void giamDinhSach(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }
        Item sachTuyetKy = null;
        Item buaGiamDinh = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isSachTuyetKy() || item.isSachTuyetKy2()) {
                sachTuyetKy = item;
            } else if (item.template.id == 1284) {
                buaGiamDinh = item;
            }
        }
        if (sachTuyetKy == null || buaGiamDinh == null) {
            return;
        }
        if (!sachTuyetKy.isHaveOption(217)) {
            Service.gI().sendServerMessage(player, "Sách đã giám định rồi!");
            return;
        }
        // Kho chi so va khoang tri so lay tu tab "Sach tuyet ky" tren panel.
        // Ban cu viet cung mot mang int[] va cong thuc nextInt(1, 10 / nextInt(1, 3))
        // — long nhau den muc khong doc ra noi khoang that su la bao nhieu.
        for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
            ItemOption io = sachTuyetKy.itemOptions.get(i);
            if (io.optionTemplate.id == nro.repository.dao.SachTuyetKyDAO.OPTION_CHUA_GIAM_DINH) {
                ItemOption moi = nro.repository.dao.SachTuyetKyDAO.bocMotChiSo();
                // Kho rong hoac tat het: giu nguyen dong chua giam dinh, dung
                // lam hong cuon sach cua nguoi choi.
                if (moi != null) {
                    sachTuyetKy.itemOptions.set(i, moi);
                }
            }
        }
        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().subQuantityItemsBag(player, buaGiamDinh, 1);
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }

}
