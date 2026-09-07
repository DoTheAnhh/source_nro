package nro.entity.boss.task.frieza;

import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossesData;
import nro.entity.map.ItemMap;
import nro.entity.player.Player;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstTaskBadges;
import nro.service.badges.BadgesTaskService;

public class Fide extends Boss {

    private long st;

    public Fide() throws Exception {
        super(BossID.FIDE, BossesData.FIDE_DAI_CA_1, BossesData.FIDE_DAI_CA_2, BossesData.FIDE_DAI_CA_3);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
        if (Util.isTrue(15, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(16, 19), 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

}





