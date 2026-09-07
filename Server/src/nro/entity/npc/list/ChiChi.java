package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.server.ServerManager;
import nro.service.Service;
import nro.core.util.FormatStyle;
import nro.core.util.Util;
import nro.core.consts.ConstAttribute;
import nro.core.consts.ConstNpc;
import java.util.ArrayList;
import java.util.List;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.attribute.Attribute;
import nro.entity.npc.Npc;
import nro.entity.player.Player;

public class ChiChi extends Npc {

    public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }
    
    int TIME_12_HOUS = 43200;
    int TIME_24_HOUS = 86400;
    int TIME_36_HOUS = 129600;
    int TIME_48_HOUS = 172800;
    int TIME_60_HOUS = 216000;
    

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            Item BongHoaHong = InventoryService.gI().findItemBag(player, 1530);
            List<String> menuList = new ArrayList<>();

            menuList.add("Cửa Hàng");
        



            menuList.add("Đóng");

            String[] menus = menuList.toArray(String[]::new);
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Bạn muốn hỏi chi ?", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        //Shop
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            

            }
        }
    }
    

}
