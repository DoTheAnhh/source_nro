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
import nro.service.PlayerService;
import nro.service.Service;
import nro.entity.skill.Skill;
import nro.service.skill.SkillService;
import nro.core.util.SkillUtil;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstRatio;
import nro.entity.map.ItemMap;
import nro.entity.map.Zone;

public class TrungUyTrang extends Boss {

    public TrungUyTrang(Zone zone, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.TRUNG_UY_TRANG, new BossData(
                "Trung úy Trắng",
                ConstPlayer.TRAI_DAT,
                new short[]{141, 142, 143, -1, -1, -1},
                dame,
                new long[]{hp},
                new int[]{59},
                new int[][]{
                    {Skill.DRAGON, 1, 1000},
                    {Skill.KAMEJOKO, Util.nextInt(4, 6), 4000}
                },
                new String[]{},
                new String[]{
                    "|-1|Xem mi dùng cách nào hạ được ta",
                    "|-1|Ha ha ha",
                    "|-1|Bulon đâu tiêu diệt hết bọn chúng cho ta"
                },
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
                    this.location.x + Util.nextInt(-15, 15),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id
            );

            ItemMap it2 = new ItemMap(
                    this.zone,
                    611,
                    1,
                    this.location.x + Util.nextInt(-15, 15),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id
            );
        }
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 198, 456);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void doneChatS() {
        Service.gI().setPos(this, 198, 456);
        this.zone.isTUTAlive = true;
    }

    @Override
    public void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
        } else {
            Service.gI().setPos(this, x, y);
        }
    }

    @Override
    public void goToPlayer(Player pl, boolean isTeleport) {
        goToXY(pl.location.x, pl.location.y, isTeleport);
    }

    @Override
    public void attack() {
        try {
            Player playerAtt = getPlayerAttack();

            if (playerAtt == null || playerAtt.isDie() || playerAtt.location.x < 755 || playerAtt.location.x > 1060) {
                return;
            }

            if (this.location.x < 775) {
                goToPlayer(playerAtt, true);
            }

            if (playerAtt.location != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
                if (this.isDie()) {
                    return;
                }

                this.playerSkill.skillSelect = this.playerSkill.skills.get(
                        Util.nextInt(0, this.playerSkill.skills.size() - 1)
                );

                if (Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(
                                playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 50),
                                false
                        );
                    }

                    SkillService.gI().useSkill(this, playerAtt, null, -1, null);
                    checkPlayerDie(playerAtt);
                } else {
                    goToPlayer(playerAtt, false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie() && !this.zone.isbulon1Alive && !this.zone.isbulon2Alive) {
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
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        this.zone.isTUTAlive = false;
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        RedRibbonHQManager.gI().removeBoss(this);
        this.dispose();
    }
}