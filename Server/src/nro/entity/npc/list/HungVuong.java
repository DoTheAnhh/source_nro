package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.core.util.Functions;
import nro.service.inventory.InventoryService;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.core.util.ItemCheckUtil;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import java.io.IOException;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.net.io.Message;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.service.shop.ShopService;
import nro.gameplay.top.TopService;


public class HungVuong extends Npc {

    public HungVuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }    

    @Override
    public void openBaseMenu(Player player) {
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
        }
    }
}