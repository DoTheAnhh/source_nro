package nro.entity.boss.legacy.nguhanhson;

import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossesData;
import nro.service.effect.EffectSkillService;
import nro.service.Service;
import nro.core.util.Util;
import java.util.Random;
import nro.entity.player.Player;

public class KhiCon extends Boss {

    public KhiCon() throws Exception {
        super(BossID.KhiCon, BossesData.KhiCon);
    }

    @Override
    public void reward(Player plKill) {
        int[] itemDos = new int[]{457, 1566, 1585};
        int randomDo = new Random().nextInt(itemDos.length);
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void joinMap() {
        super.joinMap();
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
    }

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (Util.neDuocDon(this, plAtt, piercing)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 2;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }
}