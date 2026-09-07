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

public class TrungUyXanhLo extends Boss {

    public TrungUyXanhLo(Zone zone, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.TRUNG_UY_XANH_LO, new BossData(
                "Trung úy Xanh Lơ",
                ConstPlayer.TRAI_DAT,
                new short[]{135, 136, 137, -1, -1, -1},
                dame,
                new long[]{hp},
                new int[]{62},
                new int[][]{
                    {Skill.DRAGON, 1, 1000},
                    {Skill.KAMEJOKO, Util.nextInt(4, 6), 2000},
                    {Skill.THAI_DUONG_HA_SAN, Util.nextInt(4, 7), Util.nextInt(25000, 35000)}
                },
                new String[]{},
                new String[]{
                    "|-1|Xem các ngươi mạnh đến đâu",
                    "|-1|He he he"
                },
                new String[]{},
                60
        ));

        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        if (plKill == null) {
            return;
        }

        if (Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    Util.nextInt(14, 16),
                    1,
                    this.location.x + Util.nextInt(-15, 15),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id
            );
        }

        if (Util.isTrue(70, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    611,
                    1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id
            );
        } else if (Util.isTrue(70, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    1612,
                    1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id
            );
        } else if (Util.isTrue(70, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    1621,
                    1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id
            );
        }

        if (Util.isTrue(70, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    1641,
                    1,
                    this.location.x + 30,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id
            );
        } else if (Util.isTrue(70, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    1642,
                    1,
                    this.location.x + 30,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id
            );
        } else if (Util.isTrue(70, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    1643,
                    1,
                    this.location.x + 30,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id
            );
        }
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(20, 100)) {
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
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 1210, 384);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
        Service.gI().setPos(this, 1210, 384);
    }

    @Override
    public void afk() {
        Player pl = getPlayerAttack();

        if (pl == null || pl.isDie()) {
            return;
        }

        if (Util.getDistance(this, pl) <= 500) {
            this.changeStatus(BossStatus.ACTIVE);
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