package nro.gameplay.dragon;

/*
 * @author DoTheAnh
 */
import nro.core.util.Functions;
import nro.entity.inventory.Inventory;
import nro.service.inventory.InventoryService;
import nro.server.Maintenance;
import nro.service.NpcService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.core.log.Logger;
import nro.core.util.Util;
import java.util.HashMap;
import java.util.Map;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstTaskBadges;
import java.util.List;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.net.io.Message;
import nro.service.badges.BadgesTaskService;
import nro.entity.map.Zone;
import nro.entity.player.Player;


public class SummonDragon {

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;

    public static final byte DRAGON_SHENRON = 0;
    public static final byte DRAGON_PORUNGA = 1;

    public static final short NGOC_RONG_1_SAO = 14;
    public static final short NGOC_RONG_2_SAO = 15;
    public static final short NGOC_RONG_3_SAO = 16;
    public static final short NGOC_RONG_4_SAO = 17;
    public static final short NGOC_RONG_5_SAO = 18;
    public static final short NGOC_RONG_6_SAO = 19;
    public static final short NGOC_RONG_7_SAO = 20;

    public static final String SUMMON_SHENRON_TUTORIAL
            = "Có 3 cách gọi rồng thần. Gọi từ ngọc 1 sao, gọi từ ngọc 2 sao, hoặc gọi từ ngọc 3 sao\n"
            + "Các ngọc 4 sao đến 7 sao không thể gọi rồng thần được\n"
            + "Để gọi rồng 1 sao cần ngọc từ 1 sao đến 7 sao\n"
            + "Để gọi rồng 2 sao cần ngọc từ 2 sao đến 7 sao\n"
            + "Để gọi rồng 3 sao cần ngọc từ 3 sao đến 7sao\n"
            + "Điều ước rồng 3 sao: Capsule 3 sao, hoặc 2 triệu sức mạnh, hoặc 200k vàng\n"
            + "Điều ước rồng 2 sao: Capsule 2 sao, hoặc 20 triệu sức mạnh, hoặc 2 triệu vàng\n"
            + "Điều ước rồng 1 sao: Capsule 1 sao, hoặc 200 triệu sức mạnh, hoặc 20 triệu vàng, hoặc đẹp trai, hoặc....\n"
            + "Ngọc rồng sẽ mất ngay khi gọi rồng dù bạn có ước hay không\n"
            + "Quá 5 phút nếu không ước rồng thần sẽ bay mất";
    public static final String SHENRON_SAY
            = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định";

    public static final String[] SHENRON_1_STAR_WISHES_1
            = new String[]{"Giàu có\n+2 Tỏi\nVàng", "Găng tay\nđang mang\nlên 1 cấp", "Chí mạng\nGốc +2%",
                "Thay\nChiêu 2-3\nĐệ tử", "Điều ước\nkhác"};
    public static final String[] SHENRON_1_STAR_WISHES_2
            = new String[]{"Đẹp trai\nnhất\nVũ trụ", "Giàu có\n+1K5\nNgọc", "+200 Tr\nSức mạnh\nvà tiềm\nnăng",
                "Găng tay đệ\nđang mang\nlên 1 cấp",
                "Điều ước\nkhác"};
    public static final String[] SHENRON_2_STARS_WHISHES
            = new String[]{"Giàu có\n+200\nNgọc", "+20 Tr\nSức mạnh\nvà tiềm năng", "Giàu có\n+200 Tr\nVàng"};
    public static final String[] SHENRON_3_STARS_WHISHES
            = new String[]{"Giàu có\n+50\nNgọc", "+2 Tr\nSức mạnh\nvà tiềm năng", "Giàu có\n+20 Tr\nVàng"};
    //--------------------------------------------------------------------------
    private static SummonDragon instance;
    private final Map pl_dragonStar;
    private long lastTimeShenronAppeared;
    private long lastTimeShenronWait;
    /** Doc tu quy uoc rong_goi_lai_phut, sua tren panel la an ngay. */
    private int timeResummonShenron() {
        return (int) (nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.RONG_GOI_LAI_PHUT, 5L) * 60_000L);
    }
    private boolean isShenronAppear;
    /** Doc tu quy uoc rong_cho_phut, sua tren panel la an ngay. */
    private int timeShenronWait() {
        return (int) (nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.RONG_CHO_PHUT, 5L) * 60_000L);
    }

    private final Thread update;
    private boolean active;

    public boolean isPlayerDisconnect;
    public Player playerSummonShenron;
    private int playerSummonShenronId;
    private Zone mapShenronAppear;
    private byte shenronStar;
    private int menuShenron;
    private byte select;

    private SummonDragon() {
        this.pl_dragonStar = new HashMap<>();
        this.update = new Thread(() -> {
            while (active && !Maintenance.isRunning) {
                try {
                    // Chi con HAI duong ket thuc: chon dieu uoc, hoac het nam
                    // phut. Nhanh "cho nguoi ay quay lai" da bo — thoat game la
                    // huy luon, xem SummonDragon.huyKhiThoatGame.
                    if (isShenronAppear
                            && Util.canDoWithTime(lastTimeShenronWait, timeShenronWait())) {
                        shenronLeave(playerSummonShenron, TIME_UP);
                    }
                    Functions.sleep(1000);
                } catch (Exception e) {
                    Logger.logException(SummonDragon.class, e);
                }
            }
        });
        this.active();
    }

    private void active() {
        if (!active) {
            active = true;
            this.update.start();
        }
    }

    public static SummonDragon gI() {
        if (instance == null) {
            instance = new SummonDragon();
        }
        return instance;
    }

    public void openMenuSummonShenron(Player pl, byte dragonBallStar) {
        this.pl_dragonStar.put(pl, dragonBallStar);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_SHENRON, -1, "Bạn muốn gọi rồng thần ?",
                "Hướng\ndẫn thêm\n(mới)", "Gọi\nRồng Thần\n" + dragonBallStar + " Sao");
    }

    public void summonShenron(Player pl) {
        if (pl.zone.map.mapId == 0 || pl.zone.map.mapId == 7 || pl.zone.map.mapId == 14) {
            if (checkShenronBall(pl)) {
                if (isShenronAppear) {
                    Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    return;
                }

                if (Util.canDoWithTime(lastTimeShenronAppeared, timeResummonShenron())) {
                    //gọi rồng
                    playerSummonShenron = pl;
                    playerSummonShenronId = (int) pl.id;
                    mapShenronAppear = pl.zone;
                    byte dragonStar = (byte) pl_dragonStar.get(playerSummonShenron);
                    int begin = NGOC_RONG_1_SAO;
                    switch (dragonStar) {
                        case 2:
                            begin = NGOC_RONG_2_SAO;
                            break;
                        case 3:
                            begin = NGOC_RONG_3_SAO;
                            break;
                    }
                    for (int i = begin; i <= NGOC_RONG_7_SAO; i++) {
                        try {
                            InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, i), 1);
                        } catch (Exception ex) {
                        }
                    }
                    InventoryService.gI().sendItemBag(pl);
                    sendNotifyShenronAppear();
                    activeShenron(pl, true, SummonDragon.DRAGON_SHENRON);
                    sendWhishesShenron(pl);
                } else {
                    int timeLeft = (int) ((timeResummonShenron() - (System.currentTimeMillis() - lastTimeShenronAppeared)) / 1000);
                    Service.gI().sendThongBao(pl, "Vui lòng đợi " + (timeLeft < 7200 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.gI().sendThongBao(pl, "Chỉ được gọi rồng thần ở ngôi làng trước nhà");
        }
    }

    private void reSummonShenron() {
        activeShenron(playerSummonShenron, true, SummonDragon.DRAGON_SHENRON);
        sendWhishesShenron(playerSummonShenron);
    }

    private void sendWhishesShenron(Player pl) {
        byte dragonStar;
        try {
            dragonStar = (byte) pl_dragonStar.get(pl);
            this.shenronStar = dragonStar;
        } catch (Exception e) {
            dragonStar = this.shenronStar;
        }
        switch (dragonStar) {
            case 1:
                NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
                break;
            case 2:
                NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_2, SHENRON_SAY, SHENRON_2_STARS_WHISHES);
                break;
            case 3:
                NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_3, SHENRON_SAY, SHENRON_3_STARS_WHISHES);
                break;
        }
    }

    private void activeShenron(Player pl, boolean appear, byte type) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("MaiTienDung");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(type);
                lastTimeShenronWait = System.currentTimeMillis();
                isShenronAppear = true;
                pl.iDMark.setShenronType(-1);
            }
            Service.gI().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    private boolean checkShenronBall(Player pl) {
        byte dragonStar = (byte) this.pl_dragonStar.get(pl);
        if (dragonStar == 1) {
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_2_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 2 sao");
                return false;
            }
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_3_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 3 sao");
                return false;
            }
        } else if (dragonStar == 2) {
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_3_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 3 sao");
                return false;
            }
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_4_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 4 sao");
            return false;
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_5_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 5 sao");
            return false;
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_6_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 6 sao");
            return false;
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_7_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 7 sao");
            return false;
        }
        return true;
    }

    private void sendNotifyShenronAppear() {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng thần tại "
                    + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
            Service.gI().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void confirmWish() {
        switch (this.menuShenron) {
            case ConstNpc.SHENRON_1_1:
                switch (this.select) {

                    case 0: //20 tr vàng
                        this.playerSummonShenron.inventory.gold += 2_000_000_000;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
                        break;
                    case 1: //găng tay đang đeo lên 1 cấp
                        Item item = this.playerSummonShenron.inventory.itemsBody.get(2);
                        if (item.isNotNullItem()) {
                            int level = 0;
                            for (ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id == 72) {
                                    level = io.param;
                                    if (level < 7) {
                                        io.param++;
                                    }
                                    break;
                                }
                            }
                            if (level < 7) {
                                if (level == 0) {
                                    item.itemOptions.add(new ItemOption(72, 1));
                                }
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id == 0) {
                                        io.param += (io.param * 10 / 100);
                                        break;
                                    }
                                }
                                InventoryService.gI().sendItemBody(playerSummonShenron);
                            } else {
                                Service.gI().sendThongBao(playerSummonShenron, "Găng tay của ngươi đã đạt cấp tối đa");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Ngươi hiện tại có đeo găng đâu");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
                        break;
                    case 2: //chí mạng +2%
                        if (this.playerSummonShenron.nPoint.critg < 9) {
                            this.playerSummonShenron.nPoint.critg += 2;
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Điều ước này đã quá sức với ta, ta sẽ cho ngươi chọn lại");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
                        break;
                    case 3: //thay chiêu 2-3 đệ tử
                        if (playerSummonShenron.Detu != null) {
                            if (playerSummonShenron.Detu.playerSkill.skills.get(1).skillId != -1) {
                                playerSummonShenron.Detu.openSkill2();
                                if (playerSummonShenron.Detu.playerSkill.skills.get(2).skillId != -1) {
                                    playerSummonShenron.Detu.openSkill3();
                                }
                            } else {
                                Service.gI().sendThongBao(playerSummonShenron, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Ngươi làm gì có đệ tử?");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
                        break;

                }
                break;
            case ConstNpc.SHENRON_1_2:
                switch (this.select) {
                    case 0: //đẹp trai nhất vũ trụ
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            byte gender = this.playerSummonShenron.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 227
                                    : gender == ConstPlayer.NAMEC ? 228 : 229));
                            avtVip.itemOptions.add(new ItemOption(97, Util.nextInt(5, 10)));
                            avtVip.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                            InventoryService.gI().addItemBag(playerSummonShenron, avtVip);
                            InventoryService.gI().sendItemBag(playerSummonShenron);
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
                        break;
                    case 1: //+1,5 ngọc
                        this.playerSummonShenron.inventory.ruby += 1500;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
                        break;
                    case 2: //+200 tr smtn
//                        if (this.playerSummonShenron.nPoint.power <= 20000000000L) {
//                            Service.gI().addSMTN(this.playerSummonShenron, (byte) 2, 200000000, false);
//                        } else {
//                            Service.gI().sendThongBao(playerSummonShenron, "Xin lỗi, điều ước này khó quá, ta không thể thực hiện.");
//                            reOpenShenronWishes(playerSummonShenron);
//                            return;
//                        }

//                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
//                        break;
                        Service.gI().sendThongBao(playerSummonShenron, "Điều ước này đang tạm khóa, hãy chọn điều ước khác!");
                        reOpenShenronWishes(playerSummonShenron); // mở lại menu để chọn lại
                        return; // dừng lại, không mất lượt   
                    case 3: //găng tay đệ lên 1 cấp
                        if (this.playerSummonShenron.Detu != null) {
                            Item item = this.playerSummonShenron.Detu.inventory.itemsBody.get(2);
                            if (item.isNotNullItem()) {
                                int level = 0;
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id == 72) {
                                        level = io.param;
                                        if (level < 7) {
                                            io.param++;
                                        }
                                        break;
                                    }
                                }
                                if (level < 7) {
                                    if (level == 0) {
                                        item.itemOptions.add(new ItemOption(72, 1));
                                    }
                                    for (ItemOption io : item.itemOptions) {
                                        if (io.optionTemplate.id == 0) {
                                            io.param += (io.param * 10 / 100);
                                            break;
                                        }
                                    }
                                    Service.gI().point(playerSummonShenron);
                                } else {
                                    Service.gI().sendThongBao(playerSummonShenron, "Găng tay của đệ ngươi đã đạt cấp tối đa");
                                    reOpenShenronWishes(playerSummonShenron);
                                    return;
                                }
                            } else {
                                Service.gI().sendThongBao(playerSummonShenron, "Đệ ngươi hiện tại có đeo găng đâu");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Ngươi đâu có đệ tử");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
                        break;
                }
                break;
            case ConstNpc.SHENRON_2:
                switch (this.select) {
                    case 0: //+150 ngọc
                        this.playerSummonShenron.inventory.ruby += 200;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                    case 1: //+20 tr smtn

                        Service.gI().sendThongBao(playerSummonShenron, "Điều ước này đang tạm khóa, hãy chọn điều ước khác!");
                        reOpenShenronWishes(playerSummonShenron); // mở lại menu để chọn lại
                        return; // dừng lại, không mất lượt
//                        //Service.gI().addSMTN(this.playerSummonShenron, (byte) 2, 20000000, false);
//                        break;
                    case 2: //2 tr vàng
                        if (this.playerSummonShenron.inventory.gold > 1800000000) {
                            this.playerSummonShenron.inventory.gold = Inventory.LIMIT_GOLD2;
                        } else {
                            this.playerSummonShenron.inventory.gold += 20_000_0000;
                        }
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                }
                break;
            case ConstNpc.SHENRON_3:
                switch (this.select) {
                    case 0: //+15 ngọc
                        this.playerSummonShenron.inventory.gem += 50;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                    case 1: //+2 tr smtn
                        Service.gI().sendThongBao(playerSummonShenron, "Điều ước này đang tạm khóa, hãy chọn điều ước khác!");
                        reOpenShenronWishes(playerSummonShenron); // mở lại menu để chọn lại
                        return; // dừng lại, không mất lượt  
//                        Service.gI().addSMTN(this.playerSummonShenron, (byte) 2, 2000000, false);
//                        break;
                    case 2: //200k vàng
                        if (this.playerSummonShenron.inventory.gold > (2000000000 - 20000000)) {
                            this.playerSummonShenron.inventory.gold = Inventory.LIMIT_GOLD2;
                        } else {
                            this.playerSummonShenron.inventory.gold += 20000000;
                        }
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                }
                break;
        }
        shenronLeave(this.playerSummonShenron, WISHED);
    }

    public void showConfirmShenron(Player pl, int menu, byte select) {
        this.menuShenron = menu;
        this.select = select;
        String wish = null;
        switch (menu) {
            case ConstNpc.SHENRON_1_1:
                wish = SHENRON_1_STAR_WISHES_1[select];
                break;
            case ConstNpc.SHENRON_1_2:
                wish = SHENRON_1_STAR_WISHES_2[select];
                break;
            case ConstNpc.SHENRON_2:
                wish = SHENRON_2_STARS_WHISHES[select];
                break;
            case ConstNpc.SHENRON_3:
                wish = SHENRON_3_STARS_WHISHES[select];
                break;
        }
        NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void reOpenShenronWishes(Player pl) {
        switch (menuShenron) {
            case ConstNpc.SHENRON_1_1:
                NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
                break;
            case ConstNpc.SHENRON_1_2:
                NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_1_2, SHENRON_SAY, SHENRON_1_STAR_WISHES_2);
                break;
            case ConstNpc.SHENRON_2:
                NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_2, SHENRON_SAY, SHENRON_2_STARS_WHISHES);
                break;
            case ConstNpc.SHENRON_3:
                NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.SHENRON_3, SHENRON_SAY, SHENRON_3_STARS_WHISHES);
                break;
        }
    }

    /**
     * Rồng đi ngủ và trả bầu trời lại như cũ.
     *
     * <h3>Vì sao gói "rồng biến mất" đi trước lời chào</h3>
     *
     * <p>Trước đây lời chào của rồng được gửi <b>trước</b>, mà người nhận có thể
     * đã thoát game — {@code NpcService} ném lỗi, cả hàm dừng ngay tại đó, và
     * gói báo rồng biến mất <b>không bao giờ được gửi</b>. Kết quả là con rồng
     * treo lại giữa trời tối trên màn hình của tất cả mọi người, và
     * {@code isShenronAppear} kẹt ở {@code true} nên không ai gọi rồng được
     * nữa cho tới lúc khởi động lại máy chủ.</p>
     *
     * <p>Nay gói ấy đi đầu tiên và dọn dẹp nằm trong {@code finally}: dù lời
     * chào có hỏng thế nào thì rồng vẫn biến mất và lượt gọi vẫn được mở lại.</p>
     */
    public void shenronLeave(Player pl, byte type) {
        try {
            activeShenron(pl, false, SummonDragon.DRAGON_SHENRON);
            if (pl != null) {
                if (type == WISHED) {
                    NpcService.gI().createTutorial(pl, 0, "Điều ước của ngươi đã trở thành sự thật\nHẹn gặp ngươi lần sau, ta đi ngủ đây, bái bai");
                } else {
                    NpcService.gI().createMenuRongThieng_Nomal(pl, ConstNpc.IGNORE_MENU, "Ta buồn ngủ quá rồi\nHẹn gặp ngươi lần sau, ta đi đây, bái bai");
                }
            }
        } catch (Exception ex) {
            Logger.logException(SummonDragon.class, ex, "Lỗi khi rồng thần rời đi");
        } finally {
            this.isShenronAppear = false;
            this.isPlayerDisconnect = false;
            this.menuShenron = -1;
            this.select = -1;
            this.playerSummonShenron = null;
            this.playerSummonShenronId = -1;
            this.shenronStar = -1;
            this.mapShenronAppear = null;
            lastTimeShenronAppeared = System.currentTimeMillis();
        }
    }

    /**
     * Người gọi rồng thoát game — huỷ luôn lượt gọi.
     *
     * <p>Trước đây máy chủ <b>giữ</b> con rồng lại và chờ người ấy quay vào để
     * gọi tiếp. Nhưng người thoát game thì có thể không vào lại trong năm phút
     * ấy, và suốt thời gian đó cả khu vẫn tối om với một con rồng đứng im, còn
     * người khác thì không gọi rồng được. Huỷ hẳn là dứt khoát hơn: ngọc đã
     * mất, nhưng bầu trời trả lại ngay và lượt gọi mở lại cho mọi người.</p>
     */
    public void huyKhiThoatGame(Player pl) {
        if (!this.isShenronAppear || pl == null) {
            return;
        }
        if (this.playerSummonShenronId != (int) pl.id) {
            return;
        }
        // Truyen null: nguoi nhan da roi game, gui loi chao cho ho la vo nghia
        // va con lam ca ham dung giua chung.
        shenronLeave(null, TIME_UP);
    }

}
