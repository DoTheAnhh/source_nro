package nro.entity.npc.list;

/**
 *
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.NpcService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstPlayer;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;
import nro.gameplay.dragon.SummonDragonNamek;
import nro.entity.map.dragonballnamec.NgocRongNamec;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.entity.shop.BuyBack;
import nro.service.shop.ShopService;

public class Dende extends Npc {

    public Dende(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item ThiepChucTet1 = InventoryService.gI().findItemBag(player, 1191);
        Item ThiepChucTet2 = InventoryService.gI().findItemBag(player, 1192);
        Item ThiepChucTet3 = InventoryService.gI().findItemBag(player, 1193);
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (player.idNRNM != -1) {
                    if (player.zone.map.mapId == 7) {
                        this.createOtherMenu(player, 1, "Ồ, ngọc rồng namếc, bạn thật là may mắn\nnếu tìm đủ 7 viên sẽ được Rồng Thiêng Namếc ban cho điều ước", "Hướng\ndẫn\nGọi Rồng", "Gọi rồng", "Từ chối");
                    }
                } else {
                    if (player.gender == ConstPlayer.NAMEC) {
                        if (!player.inventory.itemsDaBan.isEmpty()) {
                            this.createOtherMenu(player, ConstNpc.SHOP_DENDE_MUA_LAI,
                            "Anh cần trang bị gì cứ đến chỗ em nhé", 
                            "Cửa\nhàng", "Mua lại\nvật phẩm\nđã bán\n[" + player.inventory.itemsDaBan.size() + "/" + BuyBack.MAX_COUNT_IN_BOX + "]", "Đóng");
                        } else {
                            this.createOtherMenu(player, ConstNpc.SHOP_DENDE_MUA_LAI_2,
                            "Anh cần trang bị gì cứ đến chỗ em nhé", 
                            "Cửa Hàng",  "Đóng");
                        }
                    } else {
                        if (player.Detu == null) {
                            NpcService.gI().createTutorial(player, tempId, this.avartar, "Xin lỗi anh, em chỉ bán đồ cho dân tộc Namếc");
                        } else if (player.Detu != null && player.Detu.gender != 1) {
                            NpcService.gI().createTutorial(player, tempId, this.avartar, "Xin lỗi anh, em chỉ bán đồ cho dân tộc Namếc");
                        } else if (player.Detu != null && player.Detu.gender == 1) {
                            this.createOtherMenu(player, ConstNpc.SHOP_DENDE_2,
                            "Anh cần trang bị gì cứ đến chỗ em nhé", 
                            "Cửa Hàng Dành Cho Đệ Tử", "Đóng");
                        }
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
                case ConstNpc.SHOP_DENDE:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "DENDE", true);
                            break;
                        case 1:
                            break;
                    }   
                    break;
                case ConstNpc.SHOP_DENDE_2:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "DENDE", true);
                            break;
                    }   
                    break;
                case ConstNpc.SHOP_DENDE_MUA_LAI:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "DENDE", true);
                            break;
                        case 1:
                            ShopService.gI().opendShop(player, "ITEMS_DABAN", true);
                            break;
                    }   
                    break;
                case ConstNpc.SHOP_DENDE_MUA_LAI_2:
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "DENDE", true);
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
                            player.NhanKeoHayBiGheoNpc_3++;
                            break;
                        case 1:
                            player.NhanKeoHayBiGheoNpc_3++;
                            break;
                    }   
                    break;
                case 1:
                    switch (select) {
                    case 0:
                        NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_NRNM);
                        break;
                    case 1: {
                        if (player.zone.map.mapId == 7 && player.idNRNM != -1) {
                            if (player.idNRNM != 353) {
                                NpcService.gI().createTutorial(player, tempId, this.avartar, "Anh phải có viên Ngọc Rồng Namek 1 sao");
                                return;
                            }
                            if (TimeUtil.getCurrHour() > 22 || TimeUtil.getCurrHour() < 8) {
                                NpcService.gI().createTutorial(player, tempId, this.avartar, "Xin lỗi mấy anh, em đang bận buôn bán nên chỉ rảnh gọi Rồng vào khoảng 8h đến 22h");
                                return;
                            }
                            if (!Util.canDoWithTime(player.lastTimePickNRNM, 600000)) {
                                NpcService.gI().createTutorial(player, tempId, this.avartar, "Ngọc bẩn quá, xin chờ em " + TimeUtil.getTimeLeft(player.lastTimePickNRNM, 600) + " nữa để lau bóng ngọc, gọi Rồng mới hiển linh");
                                return;
                            }
                            if (!NgocRongNamec.gI().canCallDragonNamec(player)) {
                                NpcService.gI().createTutorial(player, tempId, this.avartar, "Hãy gom đủ 7 viên Ngọc Rồng tại đây");
                                return;
                            }
                            NgocRongNamec.gI().tOpenNrNamec = System.currentTimeMillis() + 86400000;
                            NgocRongNamec.gI().firstNrNamec = true;
                            NgocRongNamec.gI().timeNrNamec = 0;
                            NgocRongNamec.gI().doneDragonNamec();
                            NgocRongNamec.gI().initNgocRongNamec((byte) 1);
                            NgocRongNamec.gI().reInitNrNamec((long) 86399000);
                            SummonDragonNamek.gI().summonNamec(player);
                        }
                        break;
                    }
                }
            }
        }
    }
}
