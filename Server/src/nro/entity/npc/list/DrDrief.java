package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.core.util.Functions;
import nro.service.inventory.InventoryService;
import nro.server.Client;
import nro.service.fun.ChangeMapService;
import nro.service.fun.Input;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstTask;
import java.util.ArrayList;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;
import nro.entity.clan.Clan;
import nro.entity.clan.ClanMember;
import nro.service.clan.ClanService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;


public class DrDrief extends Npc {

    public DrDrief(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (canOpenNpc(pl)) {
            if (this.mapId == 84) {
                this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                        "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?",
                        pl.gender == ConstPlayer.TRAI_DAT ? "Đến\nTrái Đất" : pl.gender == ConstPlayer.NAMEC ? "Đến\nNamếc" : "Đến\nXayda");
            } else if (this.mapId == 153) {

                ArrayList<String> menu = new ArrayList<>();
                Clan clan = pl.clan;
                if (clan != null) {
                    if (clan.isLeader(pl)) {
                        menu.add("Chức năng\nbang hội");
                    }
                    menu.add("Nhiệm vụ Bang\n[" + pl.playerTask.clanTask.leftTask + "/" + ConstTask.MAX_CLAN_TASK + "]");
                }
                menu.add("Đảo Kame");
                menu.add("Từ chối");
                String[] menus = menu.toArray(String[]::new);

                this.createOtherMenu(pl, ConstNpc.BASE_MENU, "Tôi có thể giúp gì cho bang hội của bạn ?", menus);
            } else if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
                ArrayList<String> menu = new ArrayList<>();
                if (!pl.canReward_MeoDen) {
                    menu.add("Đến\nNamếc");
                    menu.add("Đến\nXayda");
                    menu.add("Đến\nSiêu thị");
                } else {
                }
                String[] menus = menu.toArray(String[]::new);
                this.createOtherMenu(pl, ConstNpc.BASE_MENU, 
                        (!pl.canReward_MeoDen ? "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?" : 
                                "Ta bị bọn Pilap bắt Mèo rồi huhuhu, Ngươi tìm lại giúp ta đi..."),
                        menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward_MeoDen) {
                RewardService.gI().rewardMeoDen(player);
                return;
            }
            if (this.mapId == 84) {
                ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 24, -1, -1);
            } else if (this.mapId == 153) {
                OUTER:
                switch (player.iDMark.getIndexMenu()) {
                    case ConstNpc.BASE_MENU: {
                        Clan clan = player.clan;
                        if (clan != null) {
                            if (clan.isLeader(player)) {
                                switch (select) {
                                    case 0:
                                        createOtherMenu(player, 1, "Tôi có thể giúp gì cho bang hội của bạn ?", "Đổi tên\ntên bang\nviết tắt", "Chọn ngẫu nhiên tên bang viết tắt", "Nâng cấp Bang hội", "Đóng");
                                        break;
                                    case 1: {
                                        if (player.playerTask.clanTask.template != null) {
                                            if (player.playerTask.clanTask.isDone()) {
                                                createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ đã hoàn thành, hãy nhận " + ((player.playerTask.clanTask.level + 1) * 10) + " capsule bang", "Nhận\nthưởng", "Đóng");
                                                break;
                                            }
                                            createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
                                        } else {
                                            TaskService.gI().changeClanTask(this, player, (byte) Util.nextInt(5));
                                        }
                                        break;
                                    }
                                    case 2:
                                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, -1);
                                        break;
                                    default: {
                                        break;
                                    }
                                }
                            } else {
                                switch (select) {
                                    case 0: {
                                        if (player.playerTask.clanTask.template != null) {
                                            if (player.playerTask.clanTask.isDone()) {
                                                createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ đã hoàn thành, hãy nhận " + ((player.playerTask.clanTask.level + 1) * 10) + " capsule bang", "Nhận\nthưởng", "Đóng");
                                                break;
                                            }
                                            createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
                                        } else {
                                            TaskService.gI().changeClanTask(this, player, (byte) Util.nextInt(5));
                                        }
                                        break;
                                    }
                                    case 1:
                                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, -1);
                                        break;
                                    default: {
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case 1: {
                        Clan clan;
                        switch (select) {
                            case 0:
                                Input.gI().createFormBangHoi(player);
                                break;
                            case 1: {
                                clan = player.clan;
                                if (clan != null) {
                                    if (clan.isLeader(player)) {
                                        if (clan.canUpdateClan(player)) {
                                            String tenvt = Functions.generateRandomCharacters(Util.nextInt(2, 4));
                                            clan.name2 = tenvt;
                                            clan.update();
                                            Service.gI().sendThongBao(player, "[" + tenvt + "] OK");
                                        }
                                    }
                                }
                                break;
                            }
                            case 2: {
                                clan = player.clan;
                                if (clan != null) {
                                    int level = clan.level;
                                    if (clan.isLeader(player)) {
                                        if (level > 10) {
                                            Service.gI().sendThongBao(player, "Đang ở cấp độ cao nhất.");
                                            return;
                                        }
                                        String npcSay = "Cần " + Util.format(ClanService.gI().capsule(clan)) + " capsule bang [đang có " + Util.format(clan.capsuleClan) + " capsule bang] để nâng cấp bang hội lên cấp " + (level + 1);
                                        npcSay += "\n+1 tối đa số lượng thành viên";
                                        if (level > 1) {
                                            npcSay += "\n+1 ô trống tối đa rương bang.";
                                        }
                                        npcSay += "\n+Mở bán bùa bang cấp " + (level + 1);
                                        createOtherMenu(player, ConstNpc.MENU_CLAN_UP, npcSay, "Đồng ý", "Từ chối");
                                    }
                                }
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    }
                    case ConstNpc.MENU_CLAN_UP: {
                        switch (select) {
                        case 0:
                        Clan clan = player.clan;
                        if (clan != null) {
                            if (clan.isLeader(player)) {
                                if (clan.level > 10) {
                                    Service.gI().sendThongBao(player, "Đang ở cấp độ cao nhất.");
                                    return;
                                }
                                int capsuleCan = ClanService.gI().capsule(clan);
                                int capsuleBang = clan.capsuleClan;
                                if (capsuleBang >= capsuleCan) {
                                    clan.capsuleClan -= capsuleCan;
                                    clan.level++;
                                    clan.maxMember++;
                                    clan.itemsBoxClan.add(ItemService.gI().createItemNull());
                                    Service.gI().sendThongBao(player, "Chúc mừng bang hội của bạn đã lên cấp " + (clan.level));
                                    for (ClanMember cm : player.clan.getMembers()) {
                                        Player pl = Client.gI().getPlayerByID(cm.id);
                                        if (pl != null) {
                                            ClanService.gI().sendMyClan(player);
                                        }
                                    }
                                    clan.updateClanBasicInfo();
                                    clan.updateItemsBoxClanToSQL(clan);
                                } else {
                                    Service.gI().sendThongBao(player, "Không đủ capsule bang, cần " + Util.format(capsuleCan - capsuleBang) + " capsule bang nữa.");
                                }
                            }
                            }
                        break;
                        }
                        break;
                    }
                    case ConstNpc.MENU_CLAN_TASK: {
                        if (player.playerTask.clanTask.template != null) {
                            switch (select) {
                                case 0: {
                                    if (player.playerTask.clanTask.isDone()) {
                                        TaskService.gI().payClanTask(player);
                                    }
                                    break;
                                }
                                case 1: {
                                    if (!player.playerTask.clanTask.isDone()) {
                                        createOtherMenu(player, ConstNpc.MENU_CLAN_TASK_REMOVE, "Bạn có chắc muốn hủy nhiệm vụ này?\nNếu hủy nhiệm vụ bạn sẽ mất 1 lượt nhiệm vụ trong ngày.", "Đồng ý", "Từ chối");
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    case ConstNpc.MENU_CLAN_TASK_REMOVE: {
                        if (player.playerTask.clanTask.template != null) {
                            if (select == 0 && !player.playerTask.clanTask.isDone()) {
                                TaskService.gI().removeClanTask(player);
                            }
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                        break;
                    case 1:
                        ChangeMapService.gI().changeMapBySpaceShip(player, 26, -1, -1);
                        break;
                    case 2:
                        if (player.nPoint.power < 20000000) {
                            Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh lớn hơn 20tr");
                            return;
                        }
                        ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
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
                        player.NhanKeoHayBiGheoNpc_7++;
                        break;
                    case 1:
                        player.NhanKeoHayBiGheoNpc_7++;
                        break;
                }   
            }
        }
    }
}
