package nro.entity.boss.list.pilafgang;

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
import nro.service.effect.EffectSkillService;

public class Pilap extends Boss {

    private long st;

    public Pilap() throws Exception {
        super(BossID.PI_LAP, false, true, false, false, BossesData.PI_LAP);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);

        // Rơi item phụ 861

        // Rơi item chính 635
        short itTemp = 635;
        ItemMap it = new ItemMap(zone, itTemp, 1,
                this.location.x + Util.nextInt(-50, 50),
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id);

        List<ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }

        it.options.add(new ItemOption(93, Util.nextInt(1, 7)));
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public void doneChatE() {
        // Pilap là cuối, không gọi thêm boss nào
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
        if (zone != null && zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(100, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            if (damage > 1) {
                damage = 1;
            }

            this.nPoint.subHP(damage);
            return damage;
        }

        return 0;
    }
}