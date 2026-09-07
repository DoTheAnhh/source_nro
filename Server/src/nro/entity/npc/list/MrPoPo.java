package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.service.fun.Input;
import nro.service.NpcService;
import nro.service.Service;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.clan.Clan;
import nro.entity.clan.ClanMember;
import nro.entity.map.destrongas.DestronGas;
import nro.service.map.destrongas.DestronGasService;
import nro.entity.npc.Npc;
import static nro.service.npc.NpcFactory.PLAYERID_OBJECT;
import nro.entity.player.Player;

public class MrPoPo extends Npc {

    public MrPoPo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 0) {
                if (player.clan != null) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Thượng Đế vừa phát hiện ra 1 loại khí đang âm thầm\nhủy diệt mọi mầm sống trên Trái Đất,\nnó được gọi là Destron Gas.\nTa sẽ đưa các cậu đến nơi ấy, các cậu đã sẵn sàng chưa?",
                            "Thông tin\nChi tiết", "Top 100\nBang hội", "Thành tích\nBang", "OK", "Từ chối");
                } else {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Thượng Đế vừa phát hiện ra 1 loại khí đang âm thầm\nhủy diệt mọi mầm sống trên Trái Đất,\nnó được gọi là Destron Gas.\nTa sẽ đưa các cậu đến nơi ấy, các cậu đã sẵn sàng chưa?",
                            "Thông tin\nChi tiết", "Top 100\nBang hội", "OK", "Từ chối");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 0) {
                if (player.iDMark.isBaseMenu()) {
                    if (player.clan != null) {
                        switch (select) {
                            case 0:
                                NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_KHI_GAS_HUY_DIET);
                                break;
                            case 1:// Top 100 bang hội
                                Service.gI().showTopClanKhiGas(player);
                                break;
                            case 2:// Thành tích Bang
                                Service.gI().showMyTopClanKhiGas(player);
                                break;
                            case 3: {
                                Clan clan = player.clan;
                                if (clan != null) {
                                    ClanMember cm = clan.getClanMember((int) player.id);
                                    if (cm != null) {
                                        if (player.clanMember.getNumDateFromJoinTimeToToday() < 1) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar,
                                                    "Gia nhập bang hội trên 1 ngày mới được tham gia");
                                            return;
                                        }
                                        if (player.clan.KhiGasHuyDiet != null) {
                                            createOtherMenu(player, 2,
                                                    "Bang hội của cậu đang tham gia Destron Gas cấp độ " + player.clan.KhiGasHuyDiet.level + "\n"
                                                    + "cậu có muốn đi cùng họ không ? ("
                                                    + TimeUtil.convertTimeNow(player.clan.KhiGasHuyDiet.getLastTimeOpen())
                                                    + " trước)", "Đồng ý", "Từ chối");
                                            return;
                                        }
                                        if (!clan.isLeader(player)) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar, "Chức năng chỉ dành cho bang chủ");
                                            return;
                                        }
                                        if (clan.members.size() < DestronGas.N_PLAYER_CLAN) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar,
                                                    "Bang hội phải có ít nhất 2 thành viên mới có thể tham gia");
                                            return;
                                        }
                                        if (player.nPoint.power < DestronGas.POWER_CAN_GO_TO_KHI_GAS_HUY_DIET) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar,
                                                    "Yêu cầu sức mạnh lớn hơn 2 tỷ để có thể tham gia");
                                            return;
                                        }
                                        Input.gI().createFormChooseLevelKGHD(player);
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        switch (select) {
                            case 0:
                                NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_KHI_GAS_HUY_DIET);
                                break;
                            case 1:// Top 100 bang hội
                                Service.gI().showTopClanKhiGas(player);
                                break;
                            case 2: {
                                Clan clan = player.clan;
                                if (clan == null) {
                                    Service.gI().sendThongBao(player, "Bạn không có bang hội!");
                                } else {
                                    ClanMember cm = clan.getClanMember((int) player.id);
                                    if (cm != null) {
                                        if (player.clanMember.getNumDateFromJoinTimeToToday() < 1) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar,
                                                    "Gia nhập bang hội trên 1 ngày mới được tham gia");
                                            return;
                                        }
                                        if (player.clan.KhiGasHuyDiet != null) {
                                            createOtherMenu(player, 2,
                                                    "Bang hội của cậu đang tham gia Destron Gas cấp độ " + player.clan.KhiGasHuyDiet.level + "\n"
                                                    + "cậu có muốn đi cùng họ không ? ("
                                                    + TimeUtil.convertTimeNow(player.clan.KhiGasHuyDiet.getLastTimeOpen())
                                                    + " trước)", "Đồng ý", "Từ chối");
                                            return;
                                        }
                                        if (!clan.isLeader(player)) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar, "Chức năng chỉ dành cho bang chủ");
                                            return;
                                        }
                                        if (clan.members.size() < DestronGas.N_PLAYER_CLAN) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar,
                                                    "Bang hội phải có ít nhất 2 thành viên mới có thể tham gia");
                                            return;
                                        }
                                        if (player.nPoint.power < DestronGas.POWER_CAN_GO_TO_KHI_GAS_HUY_DIET) {
                                            NpcService.gI().createTutorial(player, tempId, this.avartar,
                                                    "Yêu cầu sức mạnh lớn hơn 2 tỷ để có thể tham gia");
                                            return;
                                        }
                                        Input.gI().createFormChooseLevelKGHD(player);
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else if (player.iDMark.getIndexMenu() == 2) {
                    if (select == 0) {
                        if (player.clan.KhiGasHuyDiet == null) {
                            DestronGasService.gI().openKhiGasHuyDiet(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                        } else {
                            DestronGasService.gI().openKhiGasHuyDiet(player, (byte) 0);
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
                            player.NhanKeoHayBiGheoNpc_5++;
                            break;
                        case 1:
                            player.NhanKeoHayBiGheoNpc_5++;
                            break;
                    }   
                }
            }
        }
    }
}
