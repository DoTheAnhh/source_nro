package nro.gameplay.dragon;

/*
 * @author DoTheAnh
 */

import nro.core.util.Functions;
import nro.service.inventory.InventoryService;
import nro.server.Client;
import nro.service.NpcService;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import java.util.List;
import nro.repository.dao.PlayerDAO;
import nro.repository.schema.GodGK;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.net.io.Message;
import nro.entity.map.Zone;
import nro.entity.player.Player;

public class SummonDragonNamek {

    public static final byte DRAGON_PORUNGA = 1;
    private static SummonDragonNamek instance;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;
    private boolean isShenronAppear;
    public Player playerSummonShenron;
    private int playerSummonShenronId;
    private Zone mapShenronAppear;
    private int menuShenron;
    private byte select;
    private final Thread update;
    private boolean active;
    public boolean isPlayerDisconnect;
    private long lastTimeShenronWait;
    private final int timeShenronWait = 300000;

    public static SummonDragonNamek gI() {
        if (instance == null) {
            instance = new SummonDragonNamek();
        }
        return instance;
    }

    private SummonDragonNamek() {
        this.update = new Thread(() -> {
            while (active) {
                try {
                    if (isShenronAppear) {
                        if (isPlayerDisconnect) {
                            List<Player> players = mapShenronAppear.getPlayers();
                            for (Player plMap : players) {
                                if (plMap.isPl() && plMap.id == playerSummonShenronId) {
                                    playerSummonShenron = plMap;
                                    reSummonShenron();
                                    isPlayerDisconnect = false;
                                    break;
                                }
                            }

                        }
                        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    }
                 Functions.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
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

    public void summonNamec(Player pl) {
        if (pl.zone.map.mapId == 7) {
            playerSummonShenron = pl;
            playerSummonShenronId = (int) pl.id;
            mapShenronAppear = pl.zone;
            lastTimeShenronWait = System.currentTimeMillis();
            sendNotifyShenronNamekAppear();
            activeShenron(pl, true, DRAGON_PORUNGA);
            sendWhishesNamec(pl);
        } else {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void reSummonShenron() {
        activeShenron(playerSummonShenron, true, DRAGON_PORUNGA);
        sendWhishesNamec(playerSummonShenron);
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
                isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    private void sendNotifyShenronNamekAppear() {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng thần namek tại "
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
            case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM:
                try {
                    switch (select) {
                        case 0:
                            if (playerSummonShenron.clan != null) {
                                playerSummonShenron.clan.members.forEach(m -> {
                                    if (Client.gI().getPlayerByID(m.id) != null) {
                                        Player p = Client.gI().getPlayerByID(m.id);
                                        Item it = ItemService.gI().createNewItem((short) 16);
                                        it.quantity = 1;
                                        InventoryService.gI().addItemBag(p, it);
                                        InventoryService.gI().sendItemBag(p);
                                    } else {
                                        Player p = GodGK.loadPlayerByID(m.id);
                                        if (p != null) {
                                            Item it = ItemService.gI().createNewItem((short) 16);
                                            it.quantity = 1;
                                            InventoryService.gI().addItemBag(p, it);
//                                            PlayerDAO.updatePlayer(p);
                                        }
                                    }
                                });
                            } else {
                                Item it = ItemService.gI().createNewItem((short) 16);
                                it.quantity = 1;
                                InventoryService.gI().addItemBag(playerSummonShenron, it);
                                InventoryService.gI().sendItemBag(playerSummonShenron);
                            }
                            break;
                        case 1:
                            if (playerSummonShenron.clan != null) {
                                playerSummonShenron.clan.members.forEach(m -> {
                                    if (Client.gI().getPlayerByID(m.id) != null) {
                                        Player p = Client.gI().getPlayerByID(m.id);
                                        byte[] option = {77, 80, 81, 103, 50, 94, 5};
                                        byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81 //103 //50 //94 //5 % sdcm
                                        byte optionid = 0;
                                        byte optionid_v2 = 0;
                                        byte param = 0;
                                        Item it = ItemService.gI().createNewItem((short) 942);
                                        it.itemOptions.clear();
                                        optionid = option[Util.nextInt(0, 6)];
                                        param = (byte) Util.nextInt(5, 10);
                                        it.itemOptions.add(new ItemOption(optionid, param));
                                        if (Util.isTrue(20, 100)) {
                                            optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
                                            it.itemOptions.add(new ItemOption(optionid_v2, param));
                                        }
                                        it.itemOptions.add(new ItemOption(30, 0));
                                        it.quantity = 1;
                                        InventoryService.gI().addItemBag(p, it);
                                        InventoryService.gI().sendItemBag(p);
                                    } else {
                                        Player p = GodGK.loadPlayerByID(m.id);
                                        if (p != null) {
                                            byte[] option = {77, 80, 81, 103, 50, 94, 5};
                                            byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81 //103 //50 //94 //5 % sdcm
                                            byte optionid = 0;
                                            byte optionid_v2 = 0;
                                            byte param = 0;
                                            Item it = ItemService.gI().createNewItem((short) 942);
                                            it.itemOptions.clear();
                                            optionid = option[Util.nextInt(0, 6)];
                                            param = (byte) Util.nextInt(5, 10);
                                            it.itemOptions.add(new ItemOption(optionid, param));
                                            if (Util.isTrue(20, 100)) {
                                                optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
                                                it.itemOptions.add(new ItemOption(optionid_v2, param));
                                            }
                                            it.itemOptions.add(new ItemOption(30, 0));
                                            it.quantity = 1;
                                            InventoryService.gI().addItemBag(p, it);
//                                            PlayerDAO.updatePlayer(p);
                                        }
                                    }
                                });
                            } else {
                                byte[] option = {77, 80, 81, 103, 50, 94, 5};
                                byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81 //103 //50 //94 //5 % sdcm
                                byte optionid = 0;
                                byte optionid_v2 = 0;
                                byte param = 0;
                                Item it = ItemService.gI().createNewItem((short) 942);
                                it.itemOptions.clear();
                                optionid = option[Util.nextInt(0, 6)];
                                param = (byte) Util.nextInt(5, 10);
                                it.itemOptions.add(new ItemOption(optionid, param));
                                if (Util.isTrue(20, 100)) {
                                    optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
                                    it.itemOptions.add(new ItemOption(optionid_v2, param));
                                }
                                it.itemOptions.add(new ItemOption(30, 0));
                                it.quantity = 1;
                                InventoryService.gI().addItemBag(playerSummonShenron, it);
                                InventoryService.gI().sendItemBag(playerSummonShenron);
                            }
                            break;
                        case 2:
                            if (playerSummonShenron.clan != null) {
                                playerSummonShenron.clan.members.forEach(m -> {
                                    if (Client.gI().getPlayerByID(m.id) != null) {
                                        Player p = Client.gI().getPlayerByID(m.id);
                                        Item it = ItemService.gI().createNewItem((short) 2053);
                                        it.quantity = 99;
                                        InventoryService.gI().addItemBag(p, it);
                                        InventoryService.gI().sendItemBag(p);
                                    } else {
                                        Player p = GodGK.loadPlayerByID(m.id);
                                        if (p != null) {
                                            Item it = ItemService.gI().createNewItem((short) 2053);
                                            it.quantity = 99;
                                            InventoryService.gI().addItemBag(p, it);
//                                            PlayerDAO.updatePlayer(p);
                                        }
                                    }
                                });
                            } else {
                                Item it = ItemService.gI().createNewItem((short) 2053);
                                it.quantity = 99;
                                InventoryService.gI().addItemBag(playerSummonShenron, it);
                                InventoryService.gI().sendItemBag(playerSummonShenron);
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                }
                break;
        }
        shenronLeave(this.playerSummonShenron, WISHED);
    }

    public void showConfirmShenron(Player pl, int menu, byte select) {
        String wish = getWish(menu, select);
        if (wish == null) {
            sendWhishesNamec(pl);
            return;
        }
        this.menuShenron = menu;
        this.select = select;
        NpcService.gI().createMenuRongThieng_Namec(pl, ConstNpc.SHENRON_NAMEK_CONFIRM, "Ng\u01b0\u01a1i c\u00f3 ch\u1eafc mu\u1ed1n \u01b0\u1edbc?", wish, "T\u1eeb ch\u1ed1i");
    }

    private String getWish(int menu, int select) {
        if (menu != ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM) {
            return null;
        }
        switch (select) {
            case 0:
                return "1 vi\u00ean ng\u1ecdc r\u1ed3ng 3 sao";
            case 1:
                return "Pet h\u1ed5 b\u00e9o 1 ng\u00e0y";
            case 2:
                return "x99 b\u1ed9t m\u1ef3";
            default:
                return null;
        }
    }

    public void sendWhishesNamec(Player pl) {
        NpcService.gI().createMenuRongThieng_Namec(pl, ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM, "Ta sẽ ban cho cả bang hội ngươi 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định", "1-20 viên ngọc rồng 3 sao", "pet hổ sẽ béo", "x99 bột mỳ");
    }

    public void shenronLeave(Player pl, byte type) {
        if (type == WISHED) {
            //Điều ước Bùa mạnh mẽ cho tất cả trong 7 ngày của các con đã được thực hiện...tạm biệt
            NpcService.gI().createTutorial(pl, 0, "Điều ước của ngươi đã được thực hiện...tạm biệt");
        } else {
            NpcService.gI().createMenuRongThieng_Namec(pl, ConstNpc.IGNORE_MENU, "Ta buồn ngủ quá rồi\nHẹn gặp ngươi lần sau, ta đi đây, bái bai");
        }
        activeShenron(pl, false, SummonDragon.DRAGON_SHENRON);
        this.isShenronAppear = false;
        this.menuShenron = -1;
        this.select = -1;
        this.playerSummonShenron = null;
        this.playerSummonShenronId = -1;
        this.mapShenronAppear = null;
    }
}
