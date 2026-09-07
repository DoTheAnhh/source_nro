package nro.entity.boss.map.deathoralivearena;

/*
 * @Author: DoTheAnh
 */

import nro.entity.player.Player;
import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;
import nro.service.Service;
import nro.core.util.Util;

public class Dracula extends DeathOrAliveArena {

    private long lastTimeHutMau = System.currentTimeMillis();

    public Dracula(Player player) throws Exception {
        super(PHOBAN, BossID.DRACULA, BossesData.DRACULA);
        this.playerAtt = player;
    }

    @Override
    public void hutMau() {
        try {
            if (Util.canDoWithTime(lastTimeHutMau, 15000) && this.nPoint.hp > this.nPoint.hpMax / 30) {
                long hp = playerAtt.nPoint.hpMax / 10;
                playerAtt.nPoint.subHP(hp);
                this.nPoint.addHp(hp);
                Service.gI().Send_Info_NV(this);
                Service.gI().Send_Info_NV_do_Injure(playerAtt);
                this.chat("Máu ngon quá¡ hehe");
                lastTimeHutMau = System.currentTimeMillis();
            }
        } catch (Exception e) {
        }
    }
}






