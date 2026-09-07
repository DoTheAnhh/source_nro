package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.core.util.Functions;
import nro.core.util.ItemCheckUtil;
import nro.service.inventory.InventoryService;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstTask;
import java.io.IOException;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.net.io.Message;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.service.TaskService;
import nro.service.shop.ShopService;
import nro.gameplay.top.TopService;


public class Day20_10 extends Npc {

    public Day20_10(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            ItemCheckUtil BongHoaXanh = new ItemCheckUtil(player)
            .check(1093, 10, "Đất trồng cây")
            .check(1094, 5, "Phân bón")
            .check(1095, 1, "Hạt mầm");
            ItemCheckUtil ChauHoa = new ItemCheckUtil(player)
            .check(1093, 10, "Đất trồng cây")
            .check(1094, 5, "Phân bón")
            .check(1098, 1, "Bông hoa xanh")
            .check(1097, 1, "Chậu sứ")
            .check(1096, 1, "Thuốc tăng trưởng");
        }
    }
}
