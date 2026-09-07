package nro.entity.boss.legacy.nguhanhson;

import nro.core.consts.ConstPlayer;
import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.map.ItemMap;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.service.effect.EffectSkillService;
import nro.service.boss.BossManager;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.fun.ChangeMapService;
import nro.core.util.Util;
import nro.service.skill.SkillService;
import nro.core.util.SkillUtil;

/**
 * @author Administrator
 */
public class GiaiPhongAnNgoKhong extends Boss {

    public GiaiPhongAnNgoKhong(int bossID, BossData bossData, Zone zone, int x, int y) throws Exception {
        super(bossID, bossData);
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

    long lastTimeBlame;

    @Override
    public void reward(Player plKill) {
        if (Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(this.zone, 1562, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y), plKill.id);
        }
    }

    @Override
    public void active() {
        Service.gI().changeFlag(this, 8);

        if (this.typePk == ConstPlayer.NON_PK) {
            this.attack();
            this.changeToTypeNonPK();
        }

        if (System.currentTimeMillis() - lastTimeBlame > 10000) {
            this.chat("|3|HaHa cuối cùng cũng được tự do");
            this.chat("|3|Ngươi muốn tiêu diệt ta sao?");
            this.chat("|3|Còn non lắm!");
            this.chat("|3|Hãy xem 72 phép thần thông của ta đây!!!");
            lastTimeBlame = System.currentTimeMillis();
        }

        if (this.playerTarger != null
                && this.zone != null
                && this.playerTarger.zone != null
                && this.zone.map.mapId != this.playerTarger.zone.map.mapId) {
            ChangeMapService.gI().changeMap(this, this.playerTarger.zone,
                    this.playerTarger.location.x, this.playerTarger.location.y);
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100)) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }

                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }

                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (Util.neDuocDon(this, plAtt, piercing)) {
                this.chat("Xí hụt");
                return 0;
            }

            damage = this.nPoint.subDameInjureWithDeff(10);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 10;
            }

            this.nPoint.subHP(damage);

            if (playerTarger != null && playerTarger.typePk == ConstPlayer.PK_ALL) {
                PlayerService.gI().changeAndSendTypePK(playerTarger, ConstPlayer.NON_PK);
            }

            if (playerTarger != null && playerTarger.typePk == ConstPlayer.NON_PK) {
                PlayerService.gI().changeAndSendTypePK(playerTarger, ConstPlayer.PK_ALL);
            }

            Service.gI().point(this);

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
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            return;
        }

        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }

        if (this.zone != null) {
            if (this.currentLevel == 0) {
                ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            } else {
                ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            }

            Service.getInstance().sendFlagBag(this);
            this.notifyJoinMap();
        }
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        BossManager.gI().removeBoss(this);
        this.dispose();

        if (playerTarger != null) {
            playerTarger.GiaiPhongAnNgoKhong = false;
            if (playerTarger.typePk == ConstPlayer.PK_ALL) {
                PlayerService.gI().changeAndSendTypePK(playerTarger, ConstPlayer.NON_PK);
            }
        }
    }
}