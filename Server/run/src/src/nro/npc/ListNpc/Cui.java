package nro.npc.ListNpc;

/**
 * @author Văn Khải
 */
import nro.inventory.InventoryService;
import QuanLiBoss.Boss;
import QuanLiBoss.BossID;
import QuanLiBoss.Manager.BossManager;
import nro.services.Fun.ChangeMapService;
import nro.services.MapService;
import nro.services.Service;
import nro.services.TaskService;
import Utils.FormatStyle;
import Utils.Util;
import consts.ConstNpc;
import consts.ConstTask;
import event.EventManager;
import java.util.ArrayList;
import models.Item.Item;
import models.Item.ItemService;
import models.Reward.RewardService;
import nro.map.Zone;
import nro.npc.Npc;
import nro.player.Player;

public class Cui extends Npc {

    private final int COST_FIND_BOSS = 50_000_000;

    public Cui(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (!canOpenNpc(pl)) {
            return;
        }

        // Sự kiện Halloween
        if (EventManager.HALLOWEEN) {
            if (pl.NhanKeoHayBiGheoNpc_9 == 0) {
                createOtherMenu(pl, ConstNpc.NHAN_KEO_HALLOWEEN,
                        "Ồ được rồi, kẹo đây, tha cho ta hahaha.",
                        "Cho kẹo\nhay\nbị ghẹo?", "Từ chối\nnhận kẹo", "Đóng");
                return;
            }
        } // Sự kiện Tết
        else if (EventManager.LUNNAR_NEW_YEAR) {
            if (pl.NhanLiXiForNPC_24 == 0) {
                String[] chucTetMessages = {
                    "Năm mới sẽ đem an lành thịnh vượng đến với bạn",
                    "Chúc bạn và gia đình có một năm mới hạnh phúc và thịnh vượng",
                    "Phát tài phát lộc",
                    "Vạn sự như ý",
                    "Chúc bạn năm mới vui vẻ, tiền vô như nước, tình duyên rực rỡ",
                    "Năm mới phát tài phát lộc, vạn sự như ý nha",
                    "Xuân sang may mắn tràn đầy, hạnh phúc ngập lối",
                    "Năm mới bình an, vạn sự hanh thông, luôn vui cười",
                    "Tết đến rồi, quẩy hết mình và tận hưởng từng khoảnh khắc nhé",
                    "Năm mới vui như Tết, giàu như mơ, đẹp hơn xưa",
                    "Chúc bạn hạnh phúc tràn đầy, may mắn ngập tràn",
                    "Năm mới rực rỡ như pháo hoa, tươi vui như hoa mai nở",
                    "Tết đến cười thật nhiều, sống thật chill, vui hết mình",
                    "Chúc mừng năm mới"
                };
                String message = chucTetMessages[Util.nextInt(0, chucTetMessages.length - 1)];
                createOtherMenu(pl, ConstNpc.NHAN_LI_XI, message,
                        "Ok", "Chúc Mừng\nNăm Mới", "Đóng");
                return;
            }
        }

        if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
            switch (this.mapId) {
                case 19: {
                    int taskId = TaskService.gI().getIdTask(pl);
                    switch (taskId) {
                        case ConstTask.TASK_21_0:
                            this.createOtherMenu(pl, ConstNpc.MENU_FIND_KUKU,
                                    "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                    "Đến chỗ\nKuku\n(" + Util.formatNumber(COST_FIND_BOSS, FormatStyle.VIETNAMESE) + " vàng)",
                                    "Đến Cold", "Đến\nNappa", "Từ chối");
                            break;
                        case ConstTask.TASK_21_1:
                            this.createOtherMenu(pl, ConstNpc.MENU_FIND_MAP_DAU_DINH,
                                    "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                    "Đến chỗ\nMập đầu đinh\n(" + Util.formatNumber(COST_FIND_BOSS, FormatStyle.VIETNAMESE) + " vàng)",
                                    "Đến Cold", "Đến\nNappa", "Từ chối");
                            break;
                        case ConstTask.TASK_21_2:
                            this.createOtherMenu(pl, ConstNpc.MENU_FIND_RAMBO,
                                    "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                    "Đến chỗ\nRambo\n(" + Util.formatNumber(COST_FIND_BOSS, FormatStyle.VIETNAMESE) + " vàng)",
                                    "Đến Cold", "Đến\nNappa", "Từ chối");
                            break;

                        default:
                            this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                    "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó\n"
                                    + "Hoàn thành nhiệm vụ tiêu diệt đến Khu vực thám hiểm x2 TNSM cho đệ tử\n"
                                    + "Và cần 1 bình nước",
                                    "Đến Cold", "Đến\nNappa", "Đến Khu vực\nThám Hiểm", "Từ chối");
                            break;
                    }
                    break;
                }
                case 68:
                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                            "Ngươi muốn về Thành Phố Vegeta", "Đồng ý", "Từ chối");
                    break;
                default: {
                    ArrayList<String> menu = new ArrayList<>();
                    if (!pl.canReward_MeoDen) {
                        menu.add("Đến\nTrái Đất");
                        menu.add("Đến\nNamếc");
                        menu.add("Đến\nSiêu thị");
                    } else {
                        if (EventManager.LUNNAR_NEW_YEAR) {
                            menu.add("Trả Mèo");
                        }
                    }
                    String[] menus = menu.toArray(String[]::new);
                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                            (!pl.canReward_MeoDen
                                    ? "Tàu vũ trụ Xayda sử dụng công nghệ mới nhất, "
                                    + "có thể đưa ngươi đi bất kỳ đâu, chỉ cần trả tiền là được."
                                    : "Ta bị bọn Pilap bắt Mèo rồi huhuhu, Ngươi tìm lại giúp ta đi..."),
                            menus);
                    break;
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        // Tết
        if (EventManager.LUNNAR_NEW_YEAR) {
            if (player.iDMark.getIndexMenu() == ConstNpc.NHAN_LI_XI) {
                if (select == 1) {
                    Item Lixi = ItemService.gI().createNewItem((short) 1760, 1);
                    String[] chucTetMessages = {
                        "Năm mới sẽ đem an lành thịnh vượng đến với bạn",
                        "Chúc bạn và gia đình có một năm mới hạnh phúc và thịnh vượng",
                        "Chúc bạn năm mới vui vẻ, tiền vô như nước, tình duyên rực rỡ",
                        "Năm mới phát tài phát lộc, vạn sự như ý nha",
                        "Xuân sang may mắn tràn đầy, hạnh phúc ngập lối",
                        "Năm mới bình an, vạn sự hanh thông, luôn vui cười",
                        "Tết đến rồi, quẩy hết mình và tận hưởng từng khoảnh khắc nhé",
                        "Năm mới vui như Tết, giàu như mơ, đẹp hơn xưa",
                        "Chúc bạn hạnh phúc tràn đầy, may mắn ngập tràn",
                        "Năm mới rực rỡ như pháo hoa, tươi vui như hoa mai nở",
                        "Tết đến cười thật nhiều, sống thật chill, vui hết mình"
                    };
                    String NpcChat = chucTetMessages[Util.nextInt(0, chucTetMessages.length - 1)];
                    String PlayerChat = chucTetMessages[Util.nextInt(0, chucTetMessages.length - 1)];
                    this.npcChat(player, NpcChat);
                    Service.gI().chat(player, PlayerChat);
                    player.NhanLiXiForNPC_24++;
                    if (Util.isTrue(60, 100)) {
                        Lixi.addOptionParam(30, 0);
                        Lixi.addOptionParam(93, 30);
                        InventoryService.gI().addItemBag(player, Lixi);
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được " + Lixi.template.name);
                    } else {
                        Service.gI().sendThongBao(player, "(>_<)");
                    }
                }
                return;
            }
        } // Halloween
        else if (EventManager.HALLOWEEN) {
            if (player.iDMark.getIndexMenu() == ConstNpc.NHAN_KEO_HALLOWEEN) {
                switch (select) {
                    case 0: {
                        Item KeoBanTay = ItemService.gI().createNewItem((short) 901, 1);
                        KeoBanTay.addOptionParam(86, 0);
                        KeoBanTay.addOptionParam(93, 35);
                        int quality = Util.nextInt(1, 3);
                        KeoBanTay.quantity = quality;
                        InventoryService.gI().addItemBag(player, KeoBanTay);
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().chat(player, "Haha xin được " + quality + " kẹo bàn tay rồi");
                        player.NhanKeoHayBiGheoNpc_9++;
                        break;
                    }
                    case 1:
                        player.NhanKeoHayBiGheoNpc_9++;
                        break;
                }
                return;
            }
        }

        if (player.canReward_MeoDen) {
            RewardService.gI().rewardMeoDen(player);
            return;
        }

        switch (this.mapId) {
            // Trạm thị trấn Xayda
            case 26:
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0:
                            ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                            break;
                        case 1:
                            ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                            break;
                        case 2:
                            if (player.nPoint.power < 20_000_000L) {
                                Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh lớn hơn 20tr");
                                return;
                            }
                            ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
                            break;

                    }
                }
                break;

            // Trạm vũ trụ
            case 19:
                // BASE_MENU: Cold/Nappa
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0: // Đến Cold
                            if (player.nPoint.power >= 41_000_000_000L && player.playerTask.taskMain.id > 26) {
                                ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                            } else {
                                Service.gI().sendThongBaoOK(player, "Hoàn thành nhiệm vụ 26 và đạt trên 41 tỷ sức mạnh");
                            }
                            break;
                        case 1: // Đến Nappa
                            if (player.playerTask.taskMain.id >= 17) {
                                ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                            } else {
                                Service.gI().sendThongBaoOK(player, "Hãy hoàn thành nhiệm vụ 16 đi đã");
                            }
                            break;
                        case 2: // Đến Khu vực Thám Hiểm (map 249)
                        {
                            // Điều kiện nhiệm vụ
                            if (player.playerTask.taskMain.id <= 23) {
                                Service.gI().sendThongBaoOK(player, "Cần hoàn thành nhiệm vụ tiêu diệt Fide để đến Khu vực Thám Hiểm!");
                                break;
                            }

                            // Kiểm tra Bình nước (ID 456) trong túi
                            Item binhNuoc = InventoryService.gI().findItemBag(player, 456);
                            if (binhNuoc == null || binhNuoc.quantity <= 0) {
                                Service.gI().sendThongBaoOK(player, "Bạn cần có ít nhất 1 Bình nước (ID 456) trong hành trang!");
                                break;
                            }

                            // Trừ 1 Bình nước rồi qua map
                            InventoryService.gI().subQuantityItemsBag(player, binhNuoc, 1);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Đã tiêu hao 1 Bình nước để vào Khu vực Thám Hiểm!");

                            ChangeMapService.gI().changeMapBySpaceShip(player, 179, -1, -1);
                            break;
                        }

                    }
                } // MENU_FIND_*: Teleport tới Boss
                else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_KUKU) {
                    if (select == 0) {
                        openBossLocation(player, BossID.KUKU, "Kuku");
                    }
                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_MAP_DAU_DINH) {
                    if (select == 0) {
                        openBossLocation(player, BossID.MAP_DAU_DINH, "Mập đầu đinh");
                    }
                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_RAMBO) {
                    if (select == 0) {
                        openBossLocation(player, BossID.RAMBO, "Rambo");
                    }
                }
                break;

            // Nappa → về Vegeta
            case 68:
                if (player.iDMark.isBaseMenu()) {
                    if (select == 0) {
                        ChangeMapService.gI().changeMapBySpaceShip(player, 19, -1, 1100);
                    }
                }
                break;

            default:
                break;
        }
    }

    // Dùng chung: Teleport đến boss với thông báo rõ ràng
    private void openBossLocation(Player player, int bossId, String bossName) {
        Boss boss = BossManager.gI().getBossById(bossId);

        if (boss == null) {
            Service.gI().sendThongBao(player, bossName + " chưa xuất hiện");
            return;
        }
        if (boss.isDie()) {
            Service.gI().sendThongBao(player, "Boss " + bossName + " đã chết!");
            return;
        }

        if (player.inventory.gold < COST_FIND_BOSS) {
            Service.gI().sendThongBao(player,
                    "Không đủ vàng, còn thiếu "
                    + Util.formatNumber(COST_FIND_BOSS - player.inventory.gold, FormatStyle.VIETNAMESE)
                    + " vàng");
            return;
        }

        Zone z = MapService.gI().getMapCanJoin(player, boss.zone.map.mapId, boss.zone.zoneId);
        if (z == null || z.getNumOfPlayers() >= z.maxPlayer) {
            Service.gI().sendThongBao(player, "Khu vực đã đầy!");
            return;
        }

        player.inventory.gold -= COST_FIND_BOSS;
        ChangeMapService.gI().changeMap(player, boss.zone, boss.location.x, boss.location.y);
        Service.gI().sendMoney(player);
    }
}
