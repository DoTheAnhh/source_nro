package nro.entity.boss.task.napa;

/*
 *
 * @author DoTheAnh
 */
import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import nro.entity.boss.BossesData;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstTaskBadges;
import nro.service.badges.BadgesTaskService;
import nro.entity.map.ItemMap;
import nro.entity.player.Player;
import nro.service.TaskService;

public class MapDauDinh extends Boss {

    private long st;

    public MapDauDinh() throws Exception {
        super(BossID.MAP_DAU_DINH, true, true, false, false, BossesData.MAP_DAU_DINH);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
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
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }
}






