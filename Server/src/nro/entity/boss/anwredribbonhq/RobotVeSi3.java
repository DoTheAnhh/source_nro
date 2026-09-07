package nro.entity.boss.anwredribbonhq;

/*
 * @Author: DoTheAnh
 */

import nro.service.effect.EffectSkillService;
import nro.entity.player.Player;
import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.PHOBANDT;
import nro.service.boss.RedRibbonHQManager;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.entity.skill.Skill;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.entity.map.ItemMap;
import nro.entity.map.Zone;

public class RobotVeSi3 extends Boss {

    public RobotVeSi3(Zone zone, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.ROBOT_VE_SI_3, new BossData(
                "Rôbốt Vệ Sĩ 2",
                ConstPlayer.TRAI_DAT,
                new short[]{138, 139, 140, -1, -1, -1},
                dame,
                new long[]{hp},
                new int[]{57},
                new int[][]{
                    {Skill.KAMEJOKO, Util.nextInt(6, 7), Util.nextInt(700, 1200)}
                },
                new String[]{},
                new String[]{},
                new String[]{},
                60
        ));

        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        if (plKill != null && Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    Util.nextInt(14, 16),
                    1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id
            );
        }
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 300, 312);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
        Service.gI().setPos(this, 300, 312);
    }

    @Override
    public void afk() {
        Player pl = getPlayerAttack();

        if (pl == null || pl.isDie()) {
            return;
        }

        Service.gI().setPos(this, pl.location.x + Util.nextInt(-100, 100), 0);
        this.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (Util.neDuocDon(this, plAtt, piercing)) {
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

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
        }

        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        RedRibbonHQManager.gI().removeBoss(this);
        this.dispose();
    }
}