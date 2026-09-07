package nro.entity.boss.anw.bossofthegangs;

import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.PHOBANBBH;
import nro.service.boss.BossOfTheGangsManager;
import nro.core.util.SkillUtil;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstRatio;
import nro.entity.clan.Clan;
import nro.service.effect.EffectSkillService;
import nro.entity.map.ItemMap;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.service.fun.ChangeMapService;
import nro.service.Service;
import nro.entity.skill.Skill;
import nro.service.skill.SkillService;

/**
 *
 * @author DoTheAnh
 */
public class ThoBunmaClan extends Boss {

    private Clan clan;
    private long lastTimeSkill = System.currentTimeMillis();

    public ThoBunmaClan(Zone zone, Clan clan) throws Exception {
        super(PHOBANBBH, BossID.THO_BUNMA_CLAN, new BossData(
                "Thỏ bunma",
                ConstPlayer.TRAI_DAT,
                new short[]{409, 410, 411, -1, -1, -1},
                70_000,
                new long[]{1_000_000_000L},
                new int[]{165},
                generateSkills(Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC),
                new String[]{},
                new String[]{
                    "|-1|Em ơi đừng khóc bóng tối trước mắt sẽ bắt em đi",
                    "|-1|Em ơi đừng lo Em ơi đừng cho tương lai vụt tắt",
                    "|-1|Sâu trong màu mắt có chút tiếc nuối phút cuối chỉ vì",
                    "|-1|Em đâu hề sai em đâu thể mãi để trái tim đau",
                    "|-1|Không còn tương lai, em cũng chẳng còn thương ai",
                    "|-1|Sau bao niềm đau em mong rằng con tim em dừng lại",
                    "|-1|Nỗi nhớ này lâu phai, nhốt em trong 1 lâu đài",
                    "|-1|Lâu đài của những cơn đau bất tận",
                    "|-1|Vì sao em phải khóc?",
                    "|-1|Có đáng để buồn đâu, tình yêu như cơn lốc thoáng phút chốc lướt qua thật mau.",
                    "|-1|Vì sao em phải khóc?",
                    "|-1|Có đáng để buồn đâu, rượu kề môi em nốc, thoáng phút chốc đã vơi u sầu"
                },
                new String[]{},
                60
        ));
        this.zone = zone;
        this.clan = clan;
    }

    private static int[][] generateSkills(int... skillIds) {
        int[][] skills = new int[skillIds.length * 7][3];
        int index = 0;
        for (int skillId : skillIds) {
            for (int level = 1; level <= 7; level++) {
                skills[index++] = new int[]{skillId, level, 800};
            }
        }
        return skills;
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(100, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (plAtt != null && plAtt.clan != null && plAtt.clan.BossOfTheGang != null && plAtt.clanMember != null) {
                plAtt.clanMember.memberDamage += damage;
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
    public void effectCharger() {
        if (Util.isTrue(50, ConstRatio.PER100)) {
            EffectSkillService.gI().sendEffectCharge(this);
        }
    }

    @Override
    public void reward(Player plKill) {
        int numPlayers = this.zone.getNumOfPlayers();
        for (int i = 0; i < numPlayers; i++) {
            int distance = (i + 1) * Util.nextInt(-60, 60);
            int offsetX = Util.isTrue(50, 100) ? distance : -distance;
        }
        for (int i = 0; i < numPlayers; i++) {
        }
        if (plKill.clan != null && plKill.clan.BossOfTheGang != null) {
            plKill.clan.BossOfTheGang.BossDead = true;
        }
        if (plKill.clan != null && plKill.clan.BossOfTheGang != null && plKill.clanMember != null) {
            plKill.clan.rewardTopDamagers(plKill);
        }
    }



    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 910, 384);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
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
        if (Util.canDoWithTime(this.lastTimeAttack, 500) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                goToPlayer(pl, false);
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (System.currentTimeMillis() - lastTimeSkill >= 5000) {
                        lastTimeSkill = System.currentTimeMillis();
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * 200),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        }
                    } else {
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)), pl.location.y);
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
        BossOfTheGangsManager.gI().removeBoss(this);
        this.dispose();
    }
}