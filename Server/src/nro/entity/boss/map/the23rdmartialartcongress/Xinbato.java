package nro.entity.boss.map.the23rdmartialartcongress;

/*
 * @Author: DoTheAnh
 */

import nro.entity.player.Player;
import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;

public class Xinbato extends The23rdMartialArtCongress {

    public Xinbato(Player player) throws Exception {
        super(PHOBAN, BossID.XINBATO, BossesData.XINBATO);
        this.playerAtt = player;
    }
}






