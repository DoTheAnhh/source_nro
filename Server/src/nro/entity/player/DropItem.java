package nro.entity.player;

/*
 * @Author: DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.Service;
import nro.core.util.Util;
import nro.service.item.ItemMapService;
import nro.entity.item.ItemOption;
import nro.entity.map.ItemMap;
import nro.entity.map.Zone;

public class DropItem {

    private Player player;

    public DropItem(Player player) {
        this.player = player;
    }

    public void update() {
        Zone zone = player.zone;
        if (player.isPl() && zone != null && zone.map.mapId == 52
                && InventoryService.gI().getCountEmptyBag(player) > 0
                && !InventoryService.gI().isExistItemBag(player, 726)
                && !ItemMapService.gI().findItemMapByPlayer(player, 726)) {
            int x = Util.nextInt(100, zone.map.mapWidth - 100);
            int y = zone.map.yPhysicInTop(x, 100);
            ItemMap it = new ItemMap(zone, 726, 1, x, y, player.id);
            it.options.add(new ItemOption(30, 0));
            it.options.add(new ItemOption(93, 1));
            Service.gI().dropItemMapForMe(player, it);
        }
    }

    public void dispose() {
        player = null;
    }
}






