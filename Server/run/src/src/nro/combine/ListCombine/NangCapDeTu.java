package nro.combine.ListCombine;

import nro.inventory.InventoryService;
import nro.player.Player;
import nro.services.Service;
import nro.services.DetuService;
import nro.combine.CombineService;
import Utils.Util;
import Utils.FormatStyle;
import consts.ConstFont;
import consts.ConstNpc;
import models.Item.Item;
import java.util.ArrayList;
import java.util.List;

public class NangCapDeTu {

    public static void showInfoCombine(Player player) {
        if (player.Detu == null) {
            Service.gI().sendDialogMessage(player, "Bạn chưa có đệ tử nào để tiến hóa");
            return;
        }
        if (player.Detu.typeDeTu != 1) {
            Service.gI().sendDialogMessage(player, "Chỉ đệ tử Mabư mới có thể tiến hóa thành Đệ Black");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Con có muốn tiến hóa Mabư thành Đệ Black không?\n\n")
            .append(ConstFont.BOLD_GREEN).append("Điều kiện:\n")
            .append("- Sức mạnh ≥ 70 tỷ\n")
            .append("- 10 món Thần Linh bất kỳ\n")
            .append("- 200 Thỏi vàng\n\n")
            .append(ConstFont.BOLD_BLUE).append("Sau khi tiến hóa, Mabư sẽ trở thành Đệ Black với sức mạnh bóng tối.");
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Nâng cấp", "Từ chối");
    }

    public static void startCombine(Player player) {
        try {
            // Kiểm tra có đệ Mabư
            if (player.Detu == null) {
                Service.gI().sendDialogMessage(player, "Bạn chưa có đệ tử nào");
                return;
            }
            if (player.Detu.typeDeTu != 1) {
                Service.gI().sendDialogMessage(player, "Chỉ đệ Mabư mới có thể tiến hóa");
                return;
            }

            // Kiểm tra sức mạnh
            if (player.Detu.nPoint.power < 70_000_000_000L) {
                Service.gI().sendDialogMessage(player, "Đệ Mabư cần ít nhất 70 tỷ sức mạnh để tiến hóa");
                return;
            }

            // Kiểm tra 200 Thỏi vàng
            Item thoiVang = InventoryService.gI().findItemBag(player, 457);
            if (thoiVang == null || thoiVang.quantity < 200) {
                long thieu = 200 - (thoiVang == null ? 0 : thoiVang.quantity);
                Service.gI().sendDialogMessage(player, "Cần thêm " + thieu + " Thỏi vàng");
                return;
            }

            // Kiểm tra 10 món Thần Linh
            List<Item> listThanLinh = new ArrayList<>();
            for (Item it : player.inventory.itemsBag) {
                if (it != null && it.template.name.contains("Thần Linh")) {
                    listThanLinh.add(it);
                }
            }
            if (listThanLinh.size() < 10) {
                Service.gI().sendDialogMessage(player, "Cần 10 món Thần Linh bất kỳ để tiến hóa");
                return;
            }

            // Xóa nguyên liệu
            InventoryService.gI().subQuantityItemsBag(player, thoiVang, 200);
            for (int i = 0; i < 10; i++) {
                InventoryService.gI().subQuantityItemsBag(player, listThanLinh.get(i), 1);
            }
            InventoryService.gI().sendItemBag(player);

            // Hiệu ứng và thông báo
            Service.gI().sendDialogMessage(player, "Đang tiến hành tiến hóa...");
            Service.gI().chatJustForMe(player, player.Detu, "Ta cảm nhận được... sức mạnh bóng tối đang trỗi dậy...");

            new Thread(() -> {
                try {
                    Thread.sleep(2500);
                    player.Detu.dispose();
                    player.Detu = null;
                    DetuService.gI().createBlackPet(player);
                    
                    Service.gI().sendThongBao(player, "Tiến hóa thành công! Đệ Black đã xuất hiện.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Đã xảy ra lỗi khi tiến hóa đệ tử");
        }
    }
}
