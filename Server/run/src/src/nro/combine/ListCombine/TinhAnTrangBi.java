package nro.combine.ListCombine;

import nro.inventory.InventoryService;
import nro.player.Player;
import nro.services.Service;
import consts.ConstNpc;
import models.Item.Item;
import models.Item.ItemOption;
import nro.combine.CombineService;

public class TinhAnTrangBi {

    private static boolean isTrangBiAn(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id >= 650 && item.template.id <= 662) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combine.itemsCombine.size() == 2) {
                Item item = player.combine.itemsCombine.get(0);
                Item dangusac = player.combine.itemsCombine.get(1);
                if (isTrangBiAn(item)) {
                    if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1724 || dangusac.template.id == 1725 || dangusac.template.id == 1726) && dangusac.quantity >= 99) {
                        String npcSay = item.template.name + "\n|2|";
                        for (ItemOption io : item.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|1|Con có muốn biến trang bị hủy diệt " + item.template.name + " thành\n"
                                + "trang bị Ấn không?\b|4|Đục là lên\n"
                                + "|7|Cần 99 " + dangusac.template.name;
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
                    } else {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                    }
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể hóa ấn", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public static void startCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (!player.combine.itemsCombine.isEmpty()) {
                Item item = player.combine.itemsCombine.get(0);
                Item dangusac = player.combine.itemsCombine.get(1);
                int star = 0;
                ItemOption optionStar = null;
                if (item != null) {
                    for (ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id == 34 || io.optionTemplate.id == 35 || io.optionTemplate.id == 36) {
                            star = io.param;
                            optionStar = io;
                            break;
                        }
                    }
                }
                if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1724 || dangusac.template.id == 1725 || dangusac.template.id == 1726) && dangusac.quantity >= 99) {
                    if (optionStar == null) {
                        switch (dangusac.template.id) {
                            case 1724:
                                item.itemOptions.add(new ItemOption(34, 1));
                                CombineService.gI().sendEffectSuccessCombine(player);
                                break;
                            case 1725:
                                item.itemOptions.add(new ItemOption(35, 1));
                                CombineService.gI().sendEffectSuccessCombine(player);
                                break;
                            case 1726:
                                item.itemOptions.add(new ItemOption(36, 1));
                                CombineService.gI().sendEffectSuccessCombine(player);
                                break;
                            default:
                                break;
                        }
                        InventoryService.gI().subQuantityItemsBag(player, dangusac, 99);
                        InventoryService.gI().sendItemBag(player);
                        CombineService.gI().reOpenItemCombine(player);
                    } else {
                        Service.gI().sendThongBao(player, "Trang bị của bạn có ấn rồi mà !!!");
                    }
                }
            }
        }
    }

}
