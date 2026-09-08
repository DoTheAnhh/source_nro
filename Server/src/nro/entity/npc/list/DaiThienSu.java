package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.server.Manager;
import nro.service.NpcService;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.service.shop.ShopService;
import nro.gameplay.top.TopService;

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 0 || this.mapId == 7 || this.mapId == 14) {
                if (Manager.gI().HienThiTimeEventTwo() != 0) {
                    this.createOtherMenu(player, ConstNpc.MENU_DUA_TOP,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(), 
                            "Top\nSức mạnh", "Top\nChỉ Số", "Top\nĐại Gia", "Top\nNhiệm Vụ", "Từ chối");
                } else {
                    this.createOtherMenu(player, ConstNpc.MENU_DUA_TOP,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(),
                            "Top\nSức mạnh", "Top\nChỉ Số", "Top\nĐại Gia", "Top\nNhiệm Vụ", "Từ chối");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.getIndexMenu() == ConstNpc.MENU_DUA_TOP) {
                if (Manager.gI().HienThiTimeEventTwo() > 0) {
                    switch (select) {
                        case 0:
                            this.createOtherMenu(player, 0,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(), 
                            // Nhãn nút TỐI ĐA HAI DÒNG.
                            //
                            // Menu của bản mod chỉ vẽ hai dòng đầu
                            // (Menu.DONG_TOI_DA = 2), dòng thứ ba bị bỏ lặng lẽ.
                            // Nhãn cũ là "Top / Sức mạnh / Bản Thân" và
                            // "Top / Sức Mạnh / Đệ Tử" — mất dòng ba thì hai nút
                            // hiện ra y hệt nhau. Đó là chỗ trông như "menu bị
                            // lặp option".
                            "Top SM\nBản Thân", "Top SM\nĐệ Tử", "Đóng");
                            break;
                        case 1:
                            this.createOtherMenu(player, 1,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(), 
                            // Sáu nút cũ mất dòng ba nên hiện ra thành
                            // "Top HP / Top KI / Top SD" lặp lại hai lần.
                            "Top HP", "Top KI", "Top SD",
                            "HP\nĐệ Tử", "KI\nĐệ Tử", "SD\nĐệ Tử", "Đóng");
                            break;
                        case 2:
                            this.createOtherMenu(player, 2,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(), 
                            // "Top / Số Tiền / Đã Nạp" mất dòng ba nên chỉ còn
                            // "Top Số Tiền" — không biết là tiền gì.
                            "Top VNĐ", "Top Coin", "Top\nThỏi Vàng",
                            "Top\nHồng Ngọc", "Top\nĐã Nạp", "Đóng");
                            break;
                        case 3:
                            TopService.showListTopTask(player);
                            break;
                        case 4:
                            this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                "|0|Tình yêu như một dây đàn\n"
                                + "Tình vừa được thì đàn đứt dây\n"
                                + "Đứt dây này anh thay dây khác\n"
                                + "Mất em rồi anh biết thay ai?",
                                "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsMailBox))
                                + " món)",
                                "Xóa Hết\nHòm Thư", "Đóng");
                            break;
                    }
                } else {
                    switch (select) {
                        case 0:
                            this.createOtherMenu(player, 0,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(), 
                            // Nhãn nút TỐI ĐA HAI DÒNG.
                            //
                            // Menu của bản mod chỉ vẽ hai dòng đầu
                            // (Menu.DONG_TOI_DA = 2), dòng thứ ba bị bỏ lặng lẽ.
                            // Nhãn cũ là "Top / Sức mạnh / Bản Thân" và
                            // "Top / Sức Mạnh / Đệ Tử" — mất dòng ba thì hai nút
                            // hiện ra y hệt nhau. Đó là chỗ trông như "menu bị
                            // lặp option".
                            "Top SM\nBản Thân", "Top SM\nĐệ Tử", "Đóng");
                            break;
                        case 1:
                            this.createOtherMenu(player, 1,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(), 
                            // Sáu nút cũ mất dòng ba nên hiện ra thành
                            // "Top HP / Top KI / Top SD" lặp lại hai lần.
                            "Top HP", "Top KI", "Top SD",
                            "HP\nĐệ Tử", "KI\nĐệ Tử", "SD\nĐệ Tử", "Đóng");
                            break;
                        case 2:
                            this.createOtherMenu(player, 2,
                            "|2|Sự kiện đua TOP chào mừng Vũ Trụ 1\n"
                            + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                            + "Nhận thưởng vào " + Manager.timeEndNhanGiai + "\n"
                            + "Giải thưởng khủng chưa từng có, xem chi tiết tại box zalo\n"
                            + "|7|Thời gian diễn ra: " + Manager.DemTimeEvent(), 
                            // "Top / Số Tiền / Đã Nạp" mất dòng ba nên chỉ còn
                            // "Top Số Tiền" — không biết là tiền gì.
                            "Top VNĐ", "Top Coin", "Top\nThỏi Vàng",
                            "Top\nHồng Ngọc", "Top\nĐã Nạp", "Đóng");
                            break;
                        case 3:
                            TopService.showListTopTask(player);
                            break;
                        case 4:
                            this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                "|0|Tình yêu như một dây đàn\n"
                                + "Tình vừa được thì đàn đứt dây\n"
                                + "Đứt dây này anh thay dây khác\n"
                                + "Mất em rồi anh biết thay ai?",
                                "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsMailBox))
                                + " món)",
                                "Xóa Hết\nHòm Thư", "Đóng");
                            break;

                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.MAIL_BOX) {
                switch (select) {
                    case 0:
                        ShopService.gI().opendShop(player, "ITEMS_MAIL_BOX", true);
                        break;
                    case 1:
                        NpcService.gI().createMenuConMeo(player,
                                ConstNpc.CONFIRM_REMOVE_ALL_ITEM_MAIL_BOX, this.avartar,
                                "Bạn chắc muốn xóa hết vật phẩm trong hòm thư?\n"
                                + "Sau khi xóa sẽ không thể khôi phục!",
                                "Đồng ý", "Hủy bỏ");
                        break;
                    case 2:
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == 0) {
                switch (select) {
                    case 0:
                        TopService.showListTopPower(player);
                        break;
                    case 1:
                        TopService.showListTopPower_Pet(player);
                        break;
                    case 2:
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == 1) {
                switch (select) {
                    case 0:
                        TopService.showListTopHP(player);
                        break;
                    case 1:
                        TopService.showListTopKI(player);
                        break;
                    case 2:
                        TopService.showListTopSD(player);
                        break;
                    case 3:
                        TopService.showListTopHP_Pet(player);
                        break;
                    case 4:
                        TopService.showListTopKI_Pet(player);
                        break;
                    case 5:
                        TopService.showListTopSD_Pet(player);
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == 2) {
                switch (select) {
                    case 0:
                        TopService.showListTopVND(player);
                        break;
                    case 1:
                        TopService.showListTopCOIN(player);
                        break;
                    case 2:
                        Service.gI().sendThongBaoFromAdmin(player, "|2|TOP 20 NGƯỜI CHƠI NHIỀU THỎI VÀNG NHẤT\b\b|0|" + TopService.getTopThoiVang());
                        break;
                    case 3:
                        TopService.showListTopHONGNGOC(player);
                        break;
                    case 4:
                        TopService.showListTopDANAP(player);
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
                        player.NhanKeoHayBiGheoNpc_4++;
                        break;
                    case 1:
                        player.NhanKeoHayBiGheoNpc_4++;
                        break;
                }   
            }
        }
    }
}
