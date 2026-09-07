package nro.entity.boss.list.earth;

import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.player.Player;
import nro.entity.boss.BossesData;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstTaskBadges;
import nro.entity.map.ItemMap;
import java.util.List;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.badges.BadgesTaskService;

public class SuperBojack extends Boss {

    private long st;

    public SuperBojack() throws Exception {
        super(BossID.SUPER_BOJACK, false, true, false, false, BossesData.SUPER_BOJACK_2);
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
        short itTemp = 428;
        ItemMap it = new ItemMap(zone, itTemp, 1, this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        List<ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }
        int soNgay = Util.nextInt(1, 7); 
        it.options.add(new ItemOption(93, soNgay));
    }

    @Override
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
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





