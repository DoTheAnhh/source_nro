package nro.entity.boss.anw.treasureundersea;

/*
 * @Author: DoTheAnh
 */

import nro.entity.player.Detu;
import nro.entity.player.Player;
import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.PHOBANBDKB;
import nro.service.boss.TreasureUnderSeaManager;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.core.util.SkillUtil;
import nro.entity.skill.Skill;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.entity.map.ItemMap;
import nro.entity.map.Zone;
import nro.service.skill.SkillService;

public class TrungUyXanhLo extends Boss {

    public TrungUyXanhLo(Zone zone, int level, long dame, long hp) throws Exception {
        super(PHOBANBDKB, BossID.TRUNG_UY_XANH_LO_BDKB, new BossData(
                "Trung úy Xanh Lơ",
                ConstPlayer.TRAI_DAT,
                new short[]{135, 136, 137, -1, -1, -1},
                dame,
                new long[]{hp},
                new int[]{137},
                new int[][]{
                    {Skill.DRAGON, 1, 1000},
                    {Skill.KAMEJOKO, 7, 3000}
                },
                new String[]{},
                new String[]{
                    "|-1|Các ngươi tới số rồi mới gặp phải ta",
                    "|-1|He he he",
                    "|-1|Xem các ngươi mạnh đến đâu"
                },
                new String[]{},
                60
        ));
        this.zone = zone;
    }


    @Override
    public void reward(Player plKill) {
        Player pl = plKill;

        if (pl == null) {
            return;
        }

        if (pl.isDeTu) {
            pl = ((Detu) pl).master;
        }

    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 198, 456);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
        Service.gI().setPos(this, 198, 456);
    }

    @Override
    public void afk() {
        Player pl = getPlayerAttack();

        if (pl == null || pl.isDie()) {
            return;
        }

        if (Util.getDistance(this, pl) <= 200) {
            this.changeStatus(BossStatus.ACTIVE);
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();

            try {
                Player pl = getPlayerAttack();

                if (pl == null || pl.isDie()) {
                    return;
                }

                goToPlayer(pl, false);

                this.playerSkill.skillSelect = this.playerSkill.skills.get(
                        Util.nextInt(0, this.playerSkill.skills.size() - 1)
                );

                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(
                                    pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0
                                            ? pl.location.y
                                            : pl.location.y - Util.nextInt(0, 70)
                            );
                        } else {
                            this.moveTo(
                                    pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0
                                            ? pl.location.y
                                            : pl.location.y - Util.nextInt(0, 50)
                            );
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
                ex.printStackTrace();
            }
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
        TreasureUnderSeaManager.gI().removeBoss(this);
        this.dispose();
    }
}