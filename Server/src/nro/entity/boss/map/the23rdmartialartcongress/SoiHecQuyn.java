package nro.entity.boss.map.the23rdmartialartcongress;

/*
 * @Author: DoTheAnh
 */

import nro.entity.player.Player;
import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;

public class SoiHecQuyn extends The23rdMartialArtCongress {

    public SoiHecQuyn(Player player) throws Exception {
        super(PHOBAN, BossID.SOI_HEC_QUYN, BossesData.SOI_HEC_QUYN);
        this.playerAtt = player;
    }
}






