package nro.entity.boss.task.ginyuforcenamek;

import nro.entity.player.Player;
import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import nro.entity.boss.BossesData;
import nro.core.util.Util;
import nro.core.consts.ConstTaskBadges;
import java.util.List;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.badges.BadgesTaskService;
import nro.entity.map.ItemMap;
import nro.service.Service;

public class So3Namek extends Boss {

    private long st;

    public So3Namek() throws Exception {
        super(BossID.SO_3_NAMEK, false, true, false, false, BossesData.SO_3_NAMEK);
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);


        short itTemp = 292;
        ItemMap it = new ItemMap(zone, itTemp, 1,
                this.location.x + Util.nextInt(-50, 50),
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id);

        // Lấy option mặc định từ shop
        List<ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);

        if (!ops.isEmpty()) {
            it.options = ops;
        }

        // Thêm option 93 với số ngày random 1 - 7
        int soNgay = Util.nextInt(1, 7);
        it.options.add(new ItemOption(93, soNgay));

        // Drop ra map
    }

    @Override
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
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

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if ((boss.id == BossID.SO_2_NAMEK || boss.id == BossID.SO_1_NAMEK) && !boss.isDie()) {
                boss.changeStatus(BossStatus.ACTIVE);
            }
        }
    }
}