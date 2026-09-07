package nro.entity.boss.anwredribbonhq;

/*
 * @Author: DoTheAnh
 */

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

public class TrungUyThep extends Boss {

    private long lastTimeMove;

    public TrungUyThep(Zone zone, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.TRUNG_UY_THEP, new BossData(
                "Trung úy Thép",
                ConstPlayer.NAMEC,
                new short[]{129, 130, 131, -1, -1, -1},
                dame,
                new long[]{hp},
                new int[]{55},
                new int[][]{
                    {Skill.MASENKO, 7, Util.nextInt(800, 1100)}
                },
                new String[]{},
                new String[]{
                    "|-1|Nếu bọn mi muốn lên tiếp tầng lầu trên",
                    "|-1|Phải bước qua xác chết của ta đã"
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
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id
            );

            ItemMap vang = new ItemMap(
                    this.zone,
                    190,
                    31000,
                    this.location.x + Util.nextInt(-50, 50),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id
            );

            ItemMap vang2 = new ItemMap(
                    this.zone,
                    190,
                    31000,
                    this.location.x + Util.nextInt(-50, 50),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
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
        if (plAtt != null && !SkillUtil.isUseSkillDam(plAtt)) {
            return super.injured(plAtt, damage, piercing, isMobAttack);
        }

        damage = damage / 100;

        if (damage <= 0) {
            damage = 1;
        }

        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 884, 312);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void doneChatS() {
        Service.gI().setPos(this, 884, 312);
    }

    @Override
    public void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            int x2 = this.location.x + (dir == 1 ? move : -move);

            x2 = x2 < 640 ? 640 : x2;
            x2 = x2 > 980 ? 980 : x2;
            x2 = x < 220 ? x : x2;

            PlayerService.gI().playerMove(this, x2, getY(x));
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

            if (playerAtt == null || playerAtt.isDie() || playerAtt.location.x < 640 || playerAtt.location.x > 980) {
                if (Util.canDoWithTime(lastTimeMove, 1500)) {
                    lastTimeMove = System.currentTimeMillis();
                    goToXY(884, 312, false);
                }
                return;
            }

            if (playerAtt.location != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
                if (this.isDie()) {
                    return;
                }

                this.playerSkill.skillSelect = this.playerSkill.skills.get(
                        Util.nextInt(0, this.playerSkill.skills.size() - 1)
                );

                if (Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                    int x = playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80));

                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(x, getY(x), false);
                    }

                    if (playerAtt.location.y < 220) {
                        return;
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

    private int getY(int x) {
        if (x < 638 || x > 966) {
            return 240;
        } else if (x < 707) {
            return 264;
        } else if (x > 949) {
            return 288;
        }
        return 312;
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