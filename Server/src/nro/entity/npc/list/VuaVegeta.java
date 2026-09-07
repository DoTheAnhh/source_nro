package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.fun.ChangeMapService;
import nro.service.fun.Input;
import nro.service.NpcService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.log.Logger;
import nro.core.util.SkillUtil;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstPlayer;
import java.util.ArrayList;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;
import nro.entity.clan.Clan;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.service.shop.ShopService;
import nro.entity.skill.Skill;

public class VuaVegeta extends Npc {

    public VuaVegeta(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Service.gI().addBoughtSkillAttack(player);
        Item ThiepChucTet1 = InventoryService.gI().findItemBag(player, 1191);
        Item ThiepChucTet2 = InventoryService.gI().findItemBag(player, 1192);
        Item ThiepChucTet3 = InventoryService.gI().findItemBag(player, 1193);
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (player.gender != ConstPlayer.XAYDA) {
                    NpcService.gI().createTutorial(player, tempId, avartar, "Con hãy về hành tinh của mình mà thể hiện");
                    return;
                }
                ArrayList<String> menu = new ArrayList<>();
                menu.add("Nhiệm vụ");
                menu.add("Học\nKỹ năng");
                Clan clan = player.clan;
                if (clan != null) {
                    menu.add("Về khu\nvực bang");
                    if (clan.isLeader(player)) {
                        menu.add("Giải tán\nBang hội");
                    }
                }

                String[] menus = menu.toArray(String[]::new);
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào con, ta rất vui khi gặp được con\nCon muốn làm gì nào ?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        Item ThiepChucTet1 = InventoryService.gI().findItemBag(player, 1191);
        Item ThiepChucTet2 = InventoryService.gI().findItemBag(player, 1192);
        Item ThiepChucTet3 = InventoryService.gI().findItemBag(player, 1193);
        int HongNgoc;
        Item ThoiVang = ItemService.gI().createNewItem((short) 457);
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        NpcService.gI().createTutorial(player, tempId, avartar, player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                        break;
                    case 1:
                        if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
                            player.LearnSkill.Time = -1;
                            try {
                                var curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId),
                                        SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId).point);
                                player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                SkillUtil.setSkill(player, curSkill);
                                var msg = Service.gI().messageSubCommand((byte) 62);
                                msg.writer().writeShort(curSkill.skillId);
                                player.sendMessage(msg);
                                msg.cleanup();
                                PlayerService.gI().sendInfoHpMpMoney(player);
                            } catch (Exception e) {
                                Logger.log(e.toString());
                            }
                            return;
                        }
                        if (player.LearnSkill.Time != -1) {
                            var ngoc = 5;
                            var time = player.LearnSkill.Time - System.currentTimeMillis();
                            if (time / 600_000 >= 2) ngoc += time / 600_000;
                            String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
                            byte level = Byte.parseByte(subName[subName.length - 1]);
                            createOtherMenu(player, ConstNpc.HOC_SKILL_1,
                                    "Con đang học kỹ năng\n" + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name
                                            + " cấp " + level + "\nThời gian còn lại " + TimeUtil.getTime(time),
                                    "Học Cấp tốc " + ngoc + " ngọc", "Huỷ", "Bỏ qua");
                        } else {
                            ShopService.gI().opendShop(player, "SHOP_LEARN_SKILL", false);
                        }
                        break;
                    case 2: {
                        Clan clan = player.clan;
                        if (clan != null) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
                        }
                        break;
                    }
                    case 3: {
                        Clan clan = player.clan;
                        if (clan != null) {
                            if (clan.isLeader(player)) {
                                createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                            }
                        }
                        break;
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 3) {
                Clan clan = player.clan;
                if (clan != null) {
                    if (clan.isLeader(player)) {
                        if (select == 0) {
                            Input.gI().createFormGiaiTanBangHoi(player);
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_PE_NA) {
                if (select == 0) {
                    RewardService.gI().rewardBeNa(player);
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_LAN_CON) {
                if (select == 0) {
                    RewardService.gI().rewardLancon(player);
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.HOC_SKILL_1) {
                switch (select) {
                    case 0: {
                        long time = player.LearnSkill.Time - System.currentTimeMillis();
                        int ngoc = 5;
                        if (time / 600_000 >= 2) {
                            ngoc += time / 600_000;
                        }
                        if (player.inventory.gem < ngoc) {
                            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                            return;
                        }
                        player.inventory.subGem(ngoc);
                        player.LearnSkill.Time = -1;
                        try {
                            String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
                            byte level = Byte.parseByte(subName[subName.length - 1]);
                            Skill curSkill = SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId);
                            if (curSkill.point == 0) {
                                player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);
                                SkillUtil.setSkill(player, curSkill);
                                var msg = Service.getInstance().messageSubCommand((byte) 23);
                                msg.writer().writeShort(curSkill.skillId);
                                player.sendMessage(msg);
                                msg.cleanup();
                            } else {
                                player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);
                                SkillUtil.setSkill(player, curSkill);
                                var msg = Service.getInstance().messageSubCommand((byte) 62);
                                msg.writer().writeShort(curSkill.skillId);
                                player.sendMessage(msg);
                                msg.cleanup();
                            }
                            PlayerService.gI().sendInfoHpMpMoney(player);
                        } catch (Exception e) {
                            Logger.log(e.toString());
                        }
                        break;
                    }
                    case 1: {
                        createOtherMenu(player, ConstNpc.HOC_SKILL_2, "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
                        break;
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.HOC_SKILL_2) {
                switch (select) {
                    case 0: {
                        player.nPoint.tiemNang += player.LearnSkill.Potential / 2;
                        PlayerService.gI().sendTNSM(player, (byte)1, player.LearnSkill.Potential / 2);
                        player.LearnSkill.Time = -1;
                        Service.gI().point(player);
                        PlayerService.gI().sendInfoHpMpMoney(player);
                        Service.gI().ClosePanel(player);
                        NpcService.gI().createTutorial(player, NpcService.gI().getAvatar(13 + player.gender), "Con đã huỷ học kĩ năng thành công, ta sẽ trả lại con 50% tiềm năng đã học");
                        break;
                    }
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
                        player.NhanKeoHayBiGheoNpc_12++;
                        break;
                    case 1:
                        player.NhanKeoHayBiGheoNpc_12++;
                        break;
                }   
            }
        }
    }
}
