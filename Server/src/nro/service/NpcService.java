package nro.service;

import nro.core.consts.ConstNpc;
import nro.entity.npc.Npc;
import nro.service.npc.NpcFactory;
import nro.entity.player.Player;
import nro.server.Manager;
import nro.net.io.Message;
import nro.core.log.Logger;

public class NpcService {

    private static NpcService i;

    public static NpcService gI() {
        if (i == null) {
            i = new NpcService();
        }
        return i;
    }

    public void createMenuRongThieng_Nomal(Player player, int indexMenu, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.RONG_THIENG, 5221, npcSay, menuSelect);
    }

    public void createMenuRongThieng_Namec(Player player, int indexMenu, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.RONG_THIENG, -1, npcSay, menuSelect);
    }

    public void createMenuRongThieng_Event(Player player, int indexMenu, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.RONG_THIENG, 12551, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.CON_MEO, avatar, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String[] menuSelect, Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        createMenuConMeo(player, indexMenu, avatar, npcSay, menuSelect);
    }

    private void createMenu(Player player, int indexMenu, byte npcTempId, int avatar, String npcSay, String... menuSelect) {
        if (player == null || !player.isPl() || player.iDMark == null) {
            return;
        }
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            // Bang nay KHONG phai menu goc cua NPC nen xoa dau vet nut them cua
            // menu truoc do.
            //
            // MenuController phan biet "muc goc" voi "nut them" bang phep so
            // select >= menuGocSoLuong. Giu lai so cu — vi du menu goc chi co
            // mot muc "Tu choi" — thi o bang moi, bam muc thu hai tro di lai bi
            // hieu la bam nut them va chay lai chinh cai nut vua mo bang. Dung
            // canh "bam Xoa het khong an gi".
            player.menuGocSoLuong = menuSelect.length;
            player.menuNutThem = null;
            msg = new Message(32);
            msg.writer().writeShort(npcTempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(NpcService.class, e);
        }
    }

    

    public void createTutorial(Player player, int avatar, String npcSay) {
        Message msg;
        try {
            msg = new Message(38);
            msg.writer().writeShort(ConstNpc.CON_MEO);
            msg.writer().writeUTF(npcSay);
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createTutorial(Player player, int tempId, int avatar, String npcSay) {
        Message msg;
        try {
            msg = new Message(38);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public int getAvatar(int npcId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == npcId) {
                return npc.avartar;
            }
        }
        return 1139;
    }

    public void createBigMessage(Player player, int avatar, String npcSay, byte type, String select, String confirn) {
        Message msg;
        try {
            msg = new Message(-70);
            msg.writer().writeShort(avatar);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(type);
            if (type == 1) {
                msg.writer().writeUTF(confirn);// select
                msg.writer().writeUTF(select);// string Select
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ex) {
        }
    }

}





