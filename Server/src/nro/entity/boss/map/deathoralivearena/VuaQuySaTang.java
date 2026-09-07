package nro.entity.boss.map.deathoralivearena;

/*
 * @Author: DoTheAnh
 */

import nro.entity.player.Player;
import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;
import nro.core.util.Util;

public class VuaQuySaTang extends DeathOrAliveArena {

    private long lastTimeBay;

    public VuaQuySaTang(Player player) throws Exception {
        super(PHOBAN, BossID.VUA_QUY_SA_TANG, BossesData.VUA_QUY_SA_TANG);
        this.playerAtt = player;
    }

    @Override
    public void bayLungTung() {
        if (Util.canDoWithTime(lastTimeBay, 1000)) {
            goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)), Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 200), false);
            lastTimeBay = System.currentTimeMillis();
        }
    }
}






