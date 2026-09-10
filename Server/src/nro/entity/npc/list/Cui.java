package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.service.boss.BossManager;
import nro.service.fun.ChangeMapService;
import nro.service.MapService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.FormatStyle;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstTask;
import java.util.ArrayList;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;
import nro.entity.map.Zone;
import nro.entity.npc.Npc;
import nro.entity.player.Player;

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

        //==================== SỰ KIỆN HALLOWEEN ====================
        //==================== SỰ KIỆN TẾT ====================

        //==================== NHIỆM VỤ ====================
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
                                    + "Hoàn thành nhiệm vụ tiêu diệt để mở Khu vực Thám Hiểm (x2 TN cho đệ tử)\n"
                                    + "Cần 1 bình nước để vào.",
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
                    }
                    String[] menus = menu.toArray(String[]::new);
                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                            (!pl.canReward_MeoDen
                                    ? "Tàu vũ trụ Xayda có thể đưa ngươi đi bất kỳ đâu, chỉ cần trả tiền là được."
                                    : "Ta bị bọn Pilap bắt Mèo rồi huhuhu, Ngươi tìm lại giúp ta đi..."),
                            menus);
                    break;
                }
            }
        }
    }


    /**
     * Đưa người chơi sang bản đồ Cold, nếu đã đi đủ xa trong mạch nhiệm vụ.
     *
     * <h2>Điều kiện: xong nhiệm vụ 29, hết</h2>
     *
     * <p>Bản cũ đòi <b>hai</b> thứ: xong nhiệm vụ 26 <i>và</i> đạt 41 tỉ sức
     * mạnh. Vế sức mạnh là cái chặn thật sự — người chơi đã đi đúng mạch nhiệm
     * vụ vẫn bị chặn lại vì một con số không liên quan gì tới cốt truyện, và câu
     * báo gộp cả hai nên không nói rõ mình đang thiếu vế nào.</p>
     *
     * <p>Nay chỉ còn mốc nhiệm vụ, và dời lên <b>xong nhiệm vụ 29</b> cho đúng
     * mạch: 23 là mốc đi tới tương lai, 29 mới là mốc mở Cold.</p>
     */
    private void diCold(Player pl, int idTask) {
        if (idTask > 29) {
            ChangeMapService.gI().changeMapBySpaceShip(pl, 109, -1, 295);
            return;
        }
        Service.gI().sendThongBaoOK(pl,
                "Cần hoàn thành nhiệm vụ 29 trước khi đến Cold.");
    }

    @Override
    public void confirmMenu(Player pl, int select) {
        if (!canOpenNpc(pl)) {
            return;
        }

        //==================== TẾT ====================

        //==================== HALLOWEEN ====================

        //==================== MÈO ĐEN ====================
        if (pl.canReward_MeoDen) {
            RewardService.gI().rewardMeoDen(pl);
            return;
        }

        //==================== LOGIC MAP ====================
        switch (this.mapId) {
            case 26:
                if (pl.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0:
                            ChangeMapService.gI().changeMapBySpaceShip(pl, 24, -1, -1);
                            break;
                        case 1:
                            ChangeMapService.gI().changeMapBySpaceShip(pl, 25, -1, -1);
                            break;
                        case 2:
                            if (pl.nPoint.power < 20_000_000L) {
                                Service.getInstance().sendThongBao(pl, "Yêu cầu sức mạnh lớn hơn 20tr");
                                return;
                            }
                            ChangeMapService.gI().changeMapBySpaceShip(pl, 84, -1, -1);
                            break;
                    }
                }
                break;

            case 19:
                int idTask = pl.playerTask.taskMain.id;
                int subTask = TaskService.gI().getIdTask(pl);
                if (pl.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0: // Cold
                            diCold(pl, idTask);
                            break;
                        case 1: // Nappa
                            if (idTask >= 17 || (subTask >= ConstTask.TASK_21_0 && subTask <= ConstTask.TASK_21_2)) {
                                ChangeMapService.gI().changeMapBySpaceShip(pl, 68, -1, -1);
                            } else {
                                Service.gI().sendThongBaoOK(pl, "Hãy hoàn thành nhiệm vụ 16 đi đã!");
                            }
                            break;
                        case 2: // Thám hiểm
                            if (idTask <= 23) {
                                Service.gI().sendThongBaoOK(pl, "Cần hoàn thành nhiệm vụ tiêu diệt Fide để đến Khu vực Thám Hiểm!");
                                return;
                            }
                            Item binhNuoc = InventoryService.gI().findItemBag(pl, 456);
                            if (binhNuoc == null || binhNuoc.quantity <= 0) {
                                Service.gI().sendThongBaoOK(pl, "Bạn cần có ít nhất 1 Bình nước (ID 456)!");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(pl, binhNuoc, 1);
                            InventoryService.gI().sendItemBag(pl);
                            Service.gI().sendThongBao(pl, "Đã tiêu hao 1 Bình nước để vào Khu vực Thám Hiểm!");
                            ChangeMapService.gI().changeMapBySpaceShip(pl, 179, -1, -1);
                            break;
                    }
                } else {
                    // MENU NHIỆM VỤ KUKU - MAP DẦU ĐINH - RAMBO
                    if (pl.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_KUKU) {
                        switch (select) {
                            case 0: openBossLocation(pl, BossID.KUKU, "Kuku"); break;
                            case 1:
                                diCold(pl, idTask);
                                break;
                            case 2:
                                if (idTask >= 17 || (subTask >= ConstTask.TASK_21_0 && subTask <= ConstTask.TASK_21_2))
                                    ChangeMapService.gI().changeMapBySpaceShip(pl, 68, -1, -1);
                                else
                                    Service.gI().sendThongBaoOK(pl, "Hãy hoàn thành nhiệm vụ 16 đi đã!");
                                break;
                        }
                    } else if (pl.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_MAP_DAU_DINH) {
                        switch (select) {
                            case 0: openBossLocation(pl, BossID.MAP_DAU_DINH, "Mập đầu đinh"); break;
                            case 1:
                                diCold(pl, idTask);
                                break;
                            case 2:
                                if (idTask >= 17 || (subTask >= ConstTask.TASK_21_0 && subTask <= ConstTask.TASK_21_2))
                                    ChangeMapService.gI().changeMapBySpaceShip(pl, 68, -1, -1);
                                else
                                    Service.gI().sendThongBaoOK(pl, "Hãy hoàn thành nhiệm vụ 16 đi đã!");
                                break;
                        }
                    } else if (pl.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_RAMBO) {
                        switch (select) {
                            case 0: openBossLocation(pl, BossID.RAMBO, "Rambo"); break;
                            case 1:
                                diCold(pl, idTask);
                                break;
                            case 2:
                                if (idTask >= 17 || (subTask >= ConstTask.TASK_21_0 && subTask <= ConstTask.TASK_21_2))
                                    ChangeMapService.gI().changeMapBySpaceShip(pl, 68, -1, -1);
                                else
                                    Service.gI().sendThongBaoOK(pl, "Hãy hoàn thành nhiệm vụ 16 đi đã!");
                                break;
                        }
                    }
                }
                break;

            case 68:
                if (pl.iDMark.isBaseMenu() && select == 0) {
                    ChangeMapService.gI().changeMapBySpaceShip(pl, 19, -1, 1100);
                }
                break;
        }
    }

    private void openBossLocation(Player pl, int bossId, String bossName) {
        Boss boss = BossManager.gI().getBossById(bossId);
        if (boss == null || boss.zone == null) {
            Service.gI().sendThongBao(pl, bossName + " chưa xuất hiện");
            return;
        }
        if (boss.isDie()) {
            Service.gI().sendThongBao(pl, "Boss " + bossName + " đã chết!");
            return;
        }
        if (pl.inventory.gold < COST_FIND_BOSS) {
            Service.gI().sendThongBao(pl, "Không đủ vàng, còn thiếu " + Util.formatNumber(COST_FIND_BOSS - pl.inventory.gold, FormatStyle.VIETNAMESE) + " vàng");
            return;
        }
        Zone z = MapService.gI().getMapCanJoin(pl, boss.zone.map.mapId, boss.zone.zoneId);
        if (z == null || z.getNumOfPlayers() >= z.maxPlayer) {
            Service.gI().sendThongBao(pl, "Khu vực đã đầy!");
            return;
        }
        pl.inventory.gold -= COST_FIND_BOSS;
        Service.gI().sendMoney(pl);
        ChangeMapService.gI().changeMap(pl, boss.zone, boss.location.x, boss.location.y);
        Service.gI().sendThongBao(pl, "Đã đến chỗ boss " + bossName + "!");
    }
}
