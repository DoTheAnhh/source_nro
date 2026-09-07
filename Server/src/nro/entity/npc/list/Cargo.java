package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import java.util.ArrayList;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;

public class Cargo extends Npc {

    public Cargo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (canOpenNpc(pl)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
                ArrayList<String> menu = new ArrayList<>();
                if (!pl.canReward_MeoDen) {
                    menu.add("Đến\nTrái Đất");
                    menu.add("Đến\nXayda");
                    menu.add("Đến\nSiêu thị");
                } else {
                }
                String[] menus = menu.toArray(String[]::new);
                this.createOtherMenu(pl, ConstNpc.BASE_MENU, 
                        (!pl.canReward_MeoDen ? "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?" : 
                                "Ta bị bọn Pilap bắt Mèo rồi huhuhu, Ngươi tìm lại giúp ta đi..."),
                        menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward_MeoDen) {
                RewardService.gI().rewardMeoDen(player);
                return;
            }
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                        break;
                    case 1: 
                        ChangeMapService.gI().changeMapBySpaceShip(player, 26, -1, -1);
                        break;
                    case 2: 
                        if (player.nPoint.power < 20000000) {
                            Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh lớn hơn 20tr");
                            return;
                        }
                        ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NHAN_KEO_HALLOWEEN) {
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
                        player.NhanKeoHayBiGheoNpc_8++;
                        break;
                    case 1:
                        player.NhanKeoHayBiGheoNpc_8++;
                        break;
                }   
            }
        }
    }
}
