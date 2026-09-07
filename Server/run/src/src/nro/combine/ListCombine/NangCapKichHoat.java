package nro.combine.ListCombine;

import nro.inventory.InventoryService;
import nro.player.Player;
import nro.server.Manager;
import nro.services.Service;
import Utils.FormatStyle;
import Utils.Util;
import consts.ConstNpc;
import models.Item.Item;
import models.Item.ItemOption;
import models.Item.ItemService;
import models.Reward.RewardService;
import nro.combine.CombineService;

public class NangCapKichHoat {

    public static boolean isDoHuyDiet(Item item) {
        if (item.template.id >= 650 && item.template.id <= 662) {
            return true;
        }
        return false;
    }

    public static boolean isDoThanLinh(Item item) {
        if (item.template.id >= 555 && item.template.id <= 567) {
            return true;
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 2) {
            Item trangbiHuyDiet = null;
            Item doThanlinh = null;
            for (Item item : player.combine.itemsCombine) {
                if (isDoHuyDiet(item)) {
                    trangbiHuyDiet = item;
                } else if (isDoThanLinh(item)) {
                    doThanlinh = item;
                }
            }
            player.combine.rubyCombine = 1000;
            int rubyCombie = player.combine.rubyCombine;
            if (trangbiHuyDiet != null && doThanlinh != null) {
                String npcSay = "Sau khi cường hoá, sẽ được nâng cấp trang bị Huỷ Diệt thành trang bị Kích hoạt";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Cường hoá\n" + Util.formatNumber(rubyCombie, FormatStyle.VIETNAMESE) + " hồng ngọc", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị huỷ diệt và 1 món thần bất kì");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị huỷ diệt và 1 món thần bất kì");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            int ruby = player.combine.rubyCombine;
            if (player.inventory.ruby < ruby) {
                Service.gI().sendThongBao(player, "Bạn không đủ hồng ngọc, còn thiếu " + Util.formatNumber(ruby - player.inventory.ruby, FormatStyle.VIETNAMESE) + " hồng ngọc nữa");
                Service.gI().sendMoney(player);
                return;
            }
            Item trangbiHuyDiet = null;
            Item doThanlinh = null;
            for (Item item : player.combine.itemsCombine) {
                if (isDoHuyDiet(item)) {
                    trangbiHuyDiet = item;
                } else if (isDoThanLinh(item)) {
                    doThanlinh = item;
                }
            }
            int gender = trangbiHuyDiet.template.gender;
            int playerGender = player.gender;
            int[] maleOptions = {129, 141, 127, 139, 128, 140};
            int[] femaleOptions = {132, 144, 131, 143, 130, 142};
            int[] otherOptions = {135, 138, 133, 136, 134, 137};
            int[] selectedOptions;
            if (gender == 0 || gender == 3 && playerGender == 0) {
                selectedOptions = maleOptions;
            } else if (gender == 1 || gender == 3 && playerGender == 1) {
                selectedOptions = femaleOptions;
            } else {
                selectedOptions = otherOptions;
            }
            Item newItem = null;
            if (trangbiHuyDiet.template.type == 4) {
                newItem = ItemService.gI().createNewItem((short) 12);
            } else {
                newItem = ItemService.gI().createNewItem(Manager.TrangBiKichHoat[gender][trangbiHuyDiet.template.type]);
            }
            RewardService.gI().initBaseOptionClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);
            if (Util.isTrue(15, 100)) {
                newItem.itemOptions.add(new ItemOption(selectedOptions[0], 0));
                newItem.itemOptions.add(new ItemOption(selectedOptions[1], 0));
            } else {
                if (Util.isTrue(75, 100)) {
                    newItem.itemOptions.add(new ItemOption(selectedOptions[2], 0));
                    newItem.itemOptions.add(new ItemOption(selectedOptions[3], 0));
                } else {
                    newItem.itemOptions.add(new ItemOption(selectedOptions[4], 0));
                    newItem.itemOptions.add(new ItemOption(selectedOptions[5], 0));
                }
            }
            player.inventory.ruby -= ruby;
            InventoryService.gI().addItemBag(player, newItem);
            InventoryService.gI().subQuantityItemsBag(player, trangbiHuyDiet, 1);
            InventoryService.gI().subQuantityItemsBag(player, doThanlinh, 1);
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
