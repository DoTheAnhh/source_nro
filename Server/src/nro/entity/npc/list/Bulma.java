package nro.entity.npc.list;

/**
 *
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.NpcService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstPlayer;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.entity.shop.BuyBack;
import nro.service.shop.ShopService;

public class Bulma extends Npc {

    public Bulma(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item ThiepChucTet1 = InventoryService.gI().findItemBag(player, 1191);
        Item ThiepChucTet2 = InventoryService.gI().findItemBag(player, 1192);
        Item ThiepChucTet3 = InventoryService.gI().findItemBag(player, 1193);

        if (canOpenNpc(player)) {

            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (player.gender == ConstPlayer.TRAI_DAT) {
                    if (!player.inventory.itemsDaBan.isEmpty()) {
                        this.createOtherMenu(player, ConstNpc.SHOP_BUMMA_MUA_LAI,
                                "Cậu cần trang bị gì cứ đến chỗ tôi nhé",
                                "Cửa\nhàng",
                                "Mua lại\nvật phẩm\nđã bán\n[" + player.inventory.itemsDaBan.size() + "/" + BuyBack.MAX_COUNT_IN_BOX + "]",
                                "Đóng");
                    } else {
                        this.createOtherMenu(player, ConstNpc.SHOP_BUMMA_MUA_LAI_2,
                                "Cậu cần trang bị gì cứ đến chỗ tôi nhé",
                                "Cửa Hàng",
                                "Đóng");
                    }
                } else {
                    if (player.Detu == null) {
                        NpcService.gI().createTutorial(player, tempId, this.avartar,
                                "Xin lỗi cưng, chị chỉ bán đồ cho người Trái Đất");
                    } else if (player.Detu != null && player.Detu.gender != 0) {
                        NpcService.gI().createTutorial(player, tempId, this.avartar,
                                "Xin lỗi cưng, chị chỉ bán đồ cho người Trái Đất");
                    } else if (player.Detu != null && player.Detu.gender == 0) {
                        this.createOtherMenu(player, ConstNpc.SHOP_BUMMA_2,
                                "Cậu cần trang bị gì cứ đến chỗ tôi nhé",
                                "Cửa Hàng Dành Cho Đệ Tử",
                                "Đóng");
                    }
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        Item ThiepChucTet1 = InventoryService.gI().findItemBag(player, 1191);
        Item ThiepChucTet2 = InventoryService.gI().findItemBag(player, 1192);
        Item ThiepChucTet3 = InventoryService.gI().findItemBag(player, 1193);

        int HongNgoc;
        Item ThoiVang = ItemService.gI().createNewItem((short) 457);

        if (canOpenNpc(player)) {

            switch (player.iDMark.getIndexMenu()) {
                case ConstNpc.SHOP_BUMMA:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "BUNMA", true);
                            break;
                        case 1:
                            break;
                    }
                    break;

                case ConstNpc.SHOP_BUMMA_2:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "BUNMA", true);
                            break;
                    }
                    break;

                case ConstNpc.SHOP_BUMMA_MUA_LAI:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "BUNMA", true);
                            break;

                        case 1:
                            ShopService.gI().opendShop(player, "ITEMS_DABAN", true);
                            break;
                    }
                    break;

                case ConstNpc.SHOP_BUMMA_MUA_LAI_2:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "BUNMA", true);
                            break;
                    }
                    break;

                case ConstNpc.MENU_PI_LONG:
                    switch (select) {
                        case 0:
                            RewardService.gI().rewardPiLong(player);
                            break;
                    }
                    break;

                case ConstNpc.NHAN_KEO_HALLOWEEN:
                    switch (select) {
                        case 0:
                            Item KeoBanTay = ItemService.gI().createNewItem((short) 901, 1);
                            KeoBanTay.addOptionParam(86, 0);
                            KeoBanTay.addOptionParam(93, 35);

                            int quality = Util.nextInt(1, 3);
                            KeoBanTay.quantity = quality;

                            InventoryService.gI().addItemBag(player, KeoBanTay);
                            InventoryService.gI().sendItemBag(player);

                            Service.gI().chat(player, "Haha xin được " + quality + " kẹo bàn tay rồi");
                            player.NhanKeoHayBiGheoNpc_2++;
                            break;

                        case 1:
                            player.NhanKeoHayBiGheoNpc_2++;
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    }
}