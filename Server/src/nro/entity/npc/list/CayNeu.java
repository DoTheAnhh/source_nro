package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.core.util.Functions;
import nro.service.inventory.InventoryService;
import nro.server.ServerManager;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstAttribute;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.net.io.Message;
import nro.entity.attribute.Attribute;
import nro.entity.npc.Npc;
import nro.entity.player.Player;


public class CayNeu extends Npc {
    
    int TIME_12_HOUS = 43200;
    int TIME_24_HOUS = 86400;
    int TIME_36_HOUS = 129600;
    int TIME_48_HOUS = 172800;
    int TIME_60_HOUS = 216000;

    public CayNeu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
