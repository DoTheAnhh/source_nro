package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;

public class Jaco extends Npc {

    public Jaco(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 24:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Gô Tên, Calích và Monaka đang gặp chuyện ở hành tinh\nPotaufeu\nHãy đến đó ngay", "Đến\nPotaufeu", "Từ chối");
                    break;
                case 139:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây.\nCậu muốn đi đâu?", "Đến\nTrái Đất", "Đến\nNamếc", "Đến\nXayda", "Từ chối");
                    break;
                default:
                break;
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (this.mapId) {
                    case 24:
                        if (player.nPoint.power < 200_000_000L) {
                            Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh trên 200 triệu!");
                            return;
                        }
                        if (select == 0) {
                            ChangeMapService.gI().goToPotaufeu(player);
                        }
                        break;
                    case 139:
                        switch (select) {
                            case 0:
                                ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                                break;
                            case 1:
                                ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                                break;
                            case 2:
                                ChangeMapService.gI().changeMapBySpaceShip(player, 26, -1, -1);
                                break;
                        }
                        break;
                }
            }
        }
    }
}
