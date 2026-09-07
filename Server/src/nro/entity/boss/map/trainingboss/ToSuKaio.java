package nro.entity.boss.map.trainingboss;

/*
 * @Author: DoTheAnh
 */

import nro.entity.player.Player;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.core.util.Util;

public class ToSuKaio extends TrainningBoss {

    private long lastTimeLuyenTap;

    public ToSuKaio(Player player) throws Exception {
        super(PHOBAN, BossID.TO_SU_KAIO, BossesData.TO_SU_KAIO);
        this.playerAtt = player;
    }

    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            ChangeMapService.gI().changeMap(this, this.zone, playerAtt.location.x, playerAtt.location.y);
            this.changeStatus(BossStatus.CHAT_S);
            lastTimeLuyenTap = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        return 0;
    }

    @Override
    public void active() {
        if (playerAtt.location != null && playerAtt != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
            if (Util.canDoWithTime(lastTimeLuyenTap, 10000)) {
                Service.gI().addSMTN(playerAtt, (byte) 2, TrainningService.gI().getTnsmMoiPhut(playerAtt) / 6, false);
                lastTimeLuyenTap = System.currentTimeMillis();
            }
        } else {
            this.leaveMap();
        }
    }
}






