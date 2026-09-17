package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */
import nro.core.util.Functions;
import nro.service.inventory.InventoryService;
import nro.service.DetuService;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;


public class DuongTang extends Npc {

    public DuongTang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    /**
     * Dong bao con bao lau nua moi toi luot ho tong tiep theo.
     *
     * <p>Hien ngay trong loi thoai chu khong doi nguoi choi bam vao roi moi
     * bao "chua toi luot": ho den tan noi mot chuyen chi de nghe mot cau tu
     * choi thi lan sau ho khong den nua.</p>
     */
    private static String dongThoiGian(Player player) {
        long con = nro.service.hotong.HoTongService.gI().conLaiMs(player);
        if (con <= 0) {
            return "|2|Đã có thể hộ tống\n";
        }
        return "|7|Lượt hộ tống sau: còn "
                + nro.service.hotong.HoTongService.moTaThoiGian(con) + "\n";
    }

    @Override
    public void openBaseMenu(Player player) {
        if (player.HoTongDuongTang) {
            Service.gI().sendThongBao(player, "Không thể thực hiện khi đang hộ tống.");
            return;
        }
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 0:
                    // Ba nut: 0 vao Ngu Hanh Son, 1 mo bang ho tong, 2 tu choi.
                    //
                    // Nut "Nhan thuong" da bo: qua vao thang ruong ngay khi ho
                    // tong xong, khong con phai quay ve doi diem lay qua.
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "A mi phò phò, thí chủ hãy giúp giải cứu đồ đệ của bần tăng đang bị\n"
                            + "phong ấn tại ngũ hành sơn\n"
                            + dongThoiGian(player),
                            "Đồng ý", "Nhiệm vụ\nhộ tống", "Từ chối");
                    break;
                case 123:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ta sẽ đưa thí chủ ra ngoài.\n",
                            "Đồng ý", "Từ chối");
                    break;
                case 122:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "A mi phò phò, thí chủ hãy thu thập bùa 'giải khai phong ấn'\n"
                            + "mỗi chữ 10 cái.\n"
                            + "|7|(Chi tiết xem tại diễn đàn, fanpage)\n",
                            "Giải\nPhong ấn", "Về\nLàng Aru");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.iDMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU:
                    switch (this.mapId) {
                        case 0:
                            switch (select) {
                                case 0: {
                                    ChangeMapService.gI().changeMap(player, 123, -1, 80, 360);
                                    break;
                                }
                                case 1: {
                                    // Chi hai nut: bang nay khong lo phan
                                    // thuong, phan do nam o nut "Nhan thuong"
                                    // cua bang truoc. De nut thu ba o day thi
                                    // bam vao khong co gi xay ra.
                                    this.createOtherMenu(player, 0,
                                            "A mi phò phò, Ngộ Không mau hộ tống sư phụ đi thỉnh chân kinh nào\n",
                                            "Đồng ý", "Từ chối");
                                    break;
                                }
                            }
                            break;
                        case 123:
                            switch (select) {
                                case 0: {
                                    // Đồng ý ra ngoài
                                    Service.gI().sendThongBao(player, "A mi phò phò, bần tăng sẽ đưa thí chủ ra ngoài...");
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(1000);
                                            // Ra làng Aru (map 0)
                                            ChangeMapService.gI().changeMap(player, 0, -1, Util.nextInt(400, 600), 432);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }).start();
                                    break;
                                }
                                case 1: {
                                    // Từ chối không ra
                                    Service.gI().sendThongBao(player, "A mi phò phò, thí chủ muốn ở lại thì tùy duyên vậy...");
                                    break;
                                }
                                default:
                                    Service.gI().sendThongBao(player, "Không hợp lệ.");
                                    break;
                            }
                            break;

                        case 122:
                            switch (select) {
                                case 0: {
                                    Item Khai = InventoryService.gI().findItemBag(player, 538);
                                    Item Giai = InventoryService.gI().findItemBag(player, 537);
                                    Item Phong = InventoryService.gI().findItemBag(player, 539);
                                    Item An = InventoryService.gI().findItemBag(player, 540);
                                    if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                        Service.gI().sendThongBao(player, "Hành trang của bạn không đủ chỗ trống");
                                        return;
                                    }
                                    if (Khai == null) {
                                        Service.gI().sendThongBao(player, "Bạn không có chữ Khai");
                                        return;
                                    }
                                    if (Khai.quantity < 1) {
                                        Service.gI().sendThongBao(player, "Bạn không có " + Khai.Name());
                                        return;
                                    } else if (Khai.quantity < 10) {
                                        Service.gI().sendThongBao(player, "Bạn không dủ 10 " + Khai.Name());
                                        return;
                                    }
                                    if (Giai == null) {
                                        Service.gI().sendThongBao(player, "Bạn không có chữ Giải");
                                        return;
                                    }
                                    if (Giai.quantity < 1) {
                                        Service.gI().sendThongBao(player, "Bạn không có " + Giai.Name());
                                        return;
                                    } else if (Giai.quantity < 10) {
                                        Service.gI().sendThongBao(player, "Bạn không dủ 10 " + Giai.Name());
                                        return;
                                    }
                                    if (Phong == null) {
                                        Service.gI().sendThongBao(player, "Bạn không có chữ Phong");
                                        return;
                                    }
                                    if (Phong.quantity < 1) {
                                        Service.gI().sendThongBao(player, "Bạn không có " + Phong.Name());
                                        return;
                                    } else if (Phong.quantity < 10) {
                                        Service.gI().sendThongBao(player, "Bạn không dủ 10 " + Phong.Name());
                                        return;
                                    }
                                    if (An == null) {
                                        Service.gI().sendThongBao(player, "Bạn không có chữ Ấn");
                                        return;
                                    }
                                    if (An.quantity < 1) {
                                        Service.gI().sendThongBao(player, "Bạn không có " + An.Name());
                                        return;
                                    } else if (An.quantity < 10) {
                                        Service.gI().sendThongBao(player, "Bạn không dủ 10 " + An.Name());
                                        return;
                                    }
                                    InventoryService.gI().subQuantityItemsBag(player, Khai, 10);
                                    InventoryService.gI().subQuantityItemsBag(player, Giai, 10);
                                    InventoryService.gI().subQuantityItemsBag(player, Phong, 10);
                                    InventoryService.gI().subQuantityItemsBag(player, An, 10);
                                    Item TonNgoKhong = ItemService.gI().createNewItem((short) (544 + (byte) player.gender), 1);
                                    TonNgoKhong.addOptionParam(50, Util.nextInt(15, 28));
                                    TonNgoKhong.addOptionParam(94, Util.nextInt(10, 18));
                                    TonNgoKhong.addOptionParam(77, Util.nextInt(50, 99));
                                    TonNgoKhong.addOptionParam(103, Util.nextInt(50, 99));
                                    TonNgoKhong.addOptionParam(101, Util.nextInt(50, 99));
                                    TonNgoKhong.addOptionParam(100, Util.nextInt(50, 99));
                                    TonNgoKhong.addOptionParam(114, Util.nextInt(50, 99));
                                    TonNgoKhong.addOptionParam(106, 0);
                                    TonNgoKhong.addOptionParam(93, Util.nextInt(3, 7));
                                    TonNgoKhong.addOptionParam(174, 2025);
                                    npcChat(player, "A mi phò phò, đa tạ thí chủ tương trợ, xin hãy nhận món quà mọn này, bần tăng sẽ niệm chú giải thoát cho Ngộ Không");
                                    new Thread(() -> {
                                        Functions.sleep(2000);
                                        InventoryService.gI().addItemBag(player, TonNgoKhong);
                                        InventoryService.gI().sendItemBag(player);
                                        ChangeMapService.gI().changeMap(player, 0, -1, Util.nextInt(500, 600), 432);
                                    }).start();
                                    break;
                                }
                                case 1: {
                                    ChangeMapService.gI().changeMap(player, 0, -1, Util.nextInt(500, 600), 432);
                                    break;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case 0:
                    switch (this.mapId) {
                        case 0:
                            switch (select) {
                                case 0: {
                                    // Moi dieu kien nam trong HoTongService:
                                    // nhiem vu con bat khong, da toi luot chua,
                                    // co dung o Lang Aru khong. De rai o day
                                    // thi panel sua mot dang ma NPC xet mot neo.
                                    //
                                    // Da bo han Vong Kim Co va diem cong duc.
                                    String loi = nro.service.hotong.HoTongService
                                            .gI().batDau(player);
                                    if (loi != null) {
                                        Service.gI().sendThongBao(player, loi);
                                    }
                                    break;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case 1:
                    int DiemCongDuc = 0;
                    for (Item io : player.inventory.itemsBody) {
                        if (io.isNotNullItem()) {
                            if (io.template.id == 543) {
                                DiemCongDuc = io.getOptionParam(11);
                            }
                        }
                    }
                    for (Item item : player.inventory.itemsBag) {
                        if (item.isNotNullItem()) {
                            if (item.template.id == 543) {
                                DiemCongDuc = item.getOptionParam(11);
                            }
                        }
                    }
                    //sư phụ
                    boolean CaiTrangHSD = false;
                    for (Item CT : player.inventory.itemsBody) {
                        if (CT.isNotNullItem()) {
                            if (CT.template.type == 5) {
                                CaiTrangHSD = CT.haveOption(93);
                            }
                        }
                    }
                    int Optionhsd = 0;
                    for (Item CT : player.inventory.itemsBody) {
                        if (CT.isNotNullItem()) {
                            if (CT.template.type == 5) {
                                Optionhsd = CT.getOptionParam(93);
                            }
                        }
                    }
                    //đệ tử
                    boolean CaiTrangHSD_Detu = false;
                    int Optionhsd_Detu = 0;
                    if (player.Detu != null) {
                        for (Item CT : player.Detu.inventory.itemsBody) {
                            if (CT.isNotNullItem()) {
                                if (CT.template.type == 5) {
                                    CaiTrangHSD_Detu = CT.haveOption(93);
                                }
                            }
                        }
                        for (Item CT : player.Detu.inventory.itemsBody) {
                            if (CT.isNotNullItem()) {
                                if (CT.template.type == 5) {
                                    Optionhsd_Detu = CT.getOptionParam(93);
                                }
                            }
                        }
                    }
                    switch (this.mapId) {
                        case 0:
                            switch (select) {
                                case 0: {
                                    if (DiemCongDuc < 500) {
                                        Service.gI().sendThongBao(player, "Bạn không đủ điểm công đức.");
                                        return;
                                    }
                                    if (!player.inventory.itemsBody.get(5).isNotNullItem()) {
                                        Service.gI().sendThongBao(player, "Bạn đang không mặc cải trang.");
                                        return;
                                    }
                                    if (!CaiTrangHSD) {
                                        Service.gI().sendThongBao(player, "Cải trang bạn đang đeo không có hạn sử dụng.");
                                        return;
                                    }
                                    if (Optionhsd > 35) {
                                        Service.gI().sendThongBao(player, "Không thể gia hạn thêm.");
                                        return;
                                    }
                                    for (Item CT : player.inventory.itemsBody) {
                                        if (CT.isNotNullItem()) {
                                            if (CT.template.type == 5) {
                                                CT.addOptionParam(93, 7);
                                            }
                                        }
                                    }
                                    for (Item io : player.inventory.itemsBody) {
                                        if (io.isNotNullItem()) {
                                            if (io.template.id == 543) {
                                                io.subOptionParam(11, 500);
                                            }
                                        }
                                    }
                                    for (Item item : player.inventory.itemsBag) {
                                        if (item.isNotNullItem()) {
                                            if (item.template.id == 543) {
                                                item.subOptionParam(11, 500);
                                            }
                                        }
                                    }
                                    InventoryService.gI().sendItemBag(player);
                                    InventoryService.gI().sendItemBody(player);
                                    Service.gI().sendThongBao(player, "Cải trang của bạn đã được cộng thêm 7 ngày sử dụng");
                                    break;
                                }
                                case 1: {
                                    if (DiemCongDuc < 600) {
                                        Service.gI().sendThongBao(player, "Bạn không đủ điểm công đức.");
                                        return;
                                    }
                                    if (player.Detu == null) {
                                        Service.gI().sendThongBao(player, "Bạn không có đệ tử.");
                                        return;
                                    }
                                    if (!player.Detu.inventory.itemsBody.get(5).isNotNullItem()) {
                                        Service.gI().sendThongBao(player, "Đệ tử đang không mặc cải trang.");
                                        return;
                                    }
                                    if (!CaiTrangHSD_Detu) {
                                        Service.gI().sendThongBao(player, "Cải trang đệ tử đang đeo không có hạn sử dụng.");
                                        return;
                                    }
                                    if (Optionhsd_Detu > 35) {
                                        Service.gI().sendThongBao(player, "Không thể gia hạn thêm.");
                                        return;
                                    }
                                    for (Item CT : player.Detu.inventory.itemsBody) {
                                        if (CT.isNotNullItem()) {
                                            if (CT.template.type == 5) {
                                                CT.addOptionParam(93, 9);
                                            }
                                        }
                                    }
                                    for (Item io : player.inventory.itemsBody) {
                                        if (io.isNotNullItem()) {
                                            if (io.template.id == 543) {
                                                io.subOptionParam(11, 600);
                                            }
                                        }
                                    }
                                    for (Item item : player.inventory.itemsBag) {
                                        if (item.isNotNullItem()) {
                                            if (item.template.id == 543) {
                                                item.subOptionParam(11, 600);
                                            }
                                        }
                                    }
                                    InventoryService.gI().sendItemBag(player);
                                    InventoryService.gI().sendItemBody(player);
                                    InventoryService.gI().sendItemBag(player.Detu);
                                    InventoryService.gI().sendItemBody(player.Detu);
                                    Service.gI().sendThongBao(player, "Cải trang của đệ tử đã được cộng thêm 9 ngày sử dụng");
                                    break;
                                }
                                case 2: {
                                    if (DiemCongDuc < 700) {
                                        Service.gI().sendThongBao(player, "Bạn không đủ điểm công đức.");
                                        return;
                                    }
                                    if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                        Service.gI().sendThongBao(player, "Hành trang của bạn không đủ chỗ trống");
                                        return;
                                    }
                                    Item BatGioi = ItemService.gI().createNewItem((short) 548, 1);
                                    BatGioi.addOptionParam(50, Util.nextInt(10, 15));
                                    BatGioi.addOptionParam(94, Util.nextInt(8, 15));
                                    BatGioi.addOptionParam(77, Util.nextInt(40, 70));
                                    BatGioi.addOptionParam(103, Util.nextInt(40, 70));
                                    BatGioi.addOptionParam(101, Util.nextInt(40, 70));
                                    BatGioi.addOptionParam(100, Util.nextInt(40, 70));
                                    BatGioi.addOptionParam(114, Util.nextInt(40, 70));
                                    BatGioi.addOptionParam(93, Util.nextInt(3, 7));
                                    for (Item io : player.inventory.itemsBody) {
                                        if (io.isNotNullItem()) {
                                            if (io.template.id == 543) {
                                                io.subOptionParam(11, 700);
                                            }
                                        }
                                    }
                                    for (Item item : player.inventory.itemsBag) {
                                        if (item.isNotNullItem()) {
                                            if (item.template.id == 543) {
                                                item.subOptionParam(11, 700);
                                            }
                                        }
                                    }
                                    InventoryService.gI().addItemBag(player, BatGioi);
                                    InventoryService.gI().sendItemBag(player);
                                    InventoryService.gI().sendItemBody(player);
                                    Service.gI().sendThongBao(player, "Bạn nhận được " + BatGioi.Name());
                                    break;
                                }
                                case 3: {
                                    if (DiemCongDuc < 1000) {
                                        Service.gI().sendThongBao(player, "Bạn không đủ điểm công đức.");
                                        return;
                                    }
                                    if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                        Service.gI().sendThongBao(player, "Hành trang của bạn không đủ chỗ trống");
                                        return;
                                    }
                                    Item TonNgoKhong = ItemService.gI().createNewItem((short) 547, 1);
                                    TonNgoKhong.addOptionParam(50, Util.nextInt(12, 18));
                                    TonNgoKhong.addOptionParam(94, Util.nextInt(10, 15));
                                    TonNgoKhong.addOptionParam(77, Util.nextInt(45, 75));
                                    TonNgoKhong.addOptionParam(103, Util.nextInt(45, 75));
                                    TonNgoKhong.addOptionParam(101, Util.nextInt(45, 75));
                                    TonNgoKhong.addOptionParam(100, Util.nextInt(45, 75));
                                    TonNgoKhong.addOptionParam(114, Util.nextInt(45, 75));
                                    TonNgoKhong.addOptionParam(93, Util.nextInt(3, 7));
                                    for (Item io : player.inventory.itemsBody) {
                                        if (io.isNotNullItem()) {
                                            if (io.template.id == 543) {
                                                io.subOptionParam(11, 1000);
                                            }
                                        }
                                    }
                                    for (Item item : player.inventory.itemsBag) {
                                        if (item.isNotNullItem()) {
                                            if (item.template.id == 543) {
                                                item.subOptionParam(11, 1000);
                                            }
                                        }
                                    }
                                    InventoryService.gI().addItemBag(player, TonNgoKhong);
                                    InventoryService.gI().sendItemBag(player);
                                    InventoryService.gI().sendItemBody(player);
                                    Service.gI().sendThongBao(player, "Bạn nhận được " + TonNgoKhong.Name());
                                    break;
                                }
                                case 4: {
                                    if (DiemCongDuc < 1300) {
                                        Service.gI().sendThongBao(player, "Bạn không đủ điểm công đức.");
                                        return;
                                    }
                                    if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                        Service.gI().sendThongBao(player, "Hành trang của bạn không đủ chỗ trống");
                                        return;
                                    }
                                    Item SaTang = ItemService.gI().createNewItem((short) 1302, 1);
                                    SaTang.addOptionParam(50, Util.nextInt(15, 22));
                                    SaTang.addOptionParam(94, Util.nextInt(10, 15));
                                    SaTang.addOptionParam(77, Util.nextInt(50, 80));
                                    SaTang.addOptionParam(103, Util.nextInt(50, 80));
                                    SaTang.addOptionParam(101, Util.nextInt(50, 80));
                                    SaTang.addOptionParam(100, Util.nextInt(50, 80));
                                    SaTang.addOptionParam(114, Util.nextInt(50, 80));
                                    SaTang.addOptionParam(93, Util.nextInt(3, 7));
                                    for (Item io : player.inventory.itemsBody) {
                                        if (io.isNotNullItem()) {
                                            if (io.template.id == 543) {
                                                io.subOptionParam(11, 1300);
                                            }
                                        }
                                    }
                                    for (Item item : player.inventory.itemsBag) {
                                        if (item.isNotNullItem()) {
                                            if (item.template.id == 543) {
                                                item.subOptionParam(11, 1300);
                                            }
                                        }
                                    }
                                    InventoryService.gI().addItemBag(player, SaTang);
                                    InventoryService.gI().sendItemBag(player);
                                    InventoryService.gI().sendItemBody(player);
                                    Service.gI().sendThongBao(player, "Bạn nhận được " + SaTang.Name());
                                    break;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
