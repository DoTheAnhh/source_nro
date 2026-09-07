package nro.entity.boss.map.deathoralivearena;

/*
 * @Author: DoTheAnh
 */

import nro.entity.player.Player;
import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;

public class ThoDauBac extends DeathOrAliveArena {

    public ThoDauBac(Player player) throws Exception {
        super(PHOBAN, BossID.THO_DAU_BAC, BossesData.THO_DAU_BAC);
        this.playerAtt = player;
    }
}






