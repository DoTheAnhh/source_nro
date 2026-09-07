package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.fun.ChangeMapService;
import nro.service.NpcService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.service.shop.ShopService;

public class Bill extends Npc {

    public Bill(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {

            TaskService.gI().checkDoneTaskTalkNpc(player, this);

            if (mapId == 154) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "...",
                        "Về\nthánh địa\nKaio", "Từ chối");
            } else {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chưa tới giờ thi đấu, xem hướng dẫn để biết thêm chi tiết",
                        "Nói\nchuyện", "Hướng\ndẫn\nthêm", "Từ chối");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {

            switch (this.mapId) {
                case 48: {
                    switch (player.iDMark.getIndexMenu()) {
                        case ConstNpc.BASE_MENU: {
                            switch (select) {
                                case 0: {
                                    if (InventoryService.gI().canOpenBillShop(player)) {
                                        createOtherMenu(
                                                player,
                                                2,
                                                "Đói bụng quá... ngươi mang cho ta 99 phần đồ ăn\n"
                                                + "ta sẽ cho một món đồ Hủy Diệt.\n"
                                                + "Nếu tâm trạng ta vui ngươi có thể nhận trang bị tăng đến 15%",
                                                "OK",
                                                "Từ chối"
                                        );
                                    } else {
                                        createOtherMenu(
                                                player,
                                                2,
                                                "Ngươi trang bị đủ bộ 5 món trang bị Thần\n"
                                                + "và mang 99 phần đồ ăn tới đây...\n"
                                                + "rồi ta nói chuyện tiếp.",
                                                "OK"
                                        );
                                    }
                                    break;
                                }

                                case 1:
                                    NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BILL);
                                    break;
                            }
                            break;
                        }

                        case 2: {
                            if (select == 0 && InventoryService.gI().canOpenBillShop(player)) {
                                ShopService.gI().opendShop(player, "BILL", true);
                            }
                            break;
                        }
                    }
                    break;
                }

                case 154: {
                    if (select == 0) {
                        ChangeMapService.gI().changeMap(player, 50, -1, 318, 336);
                    }
                    break;
                }
            }
        }
    }
}