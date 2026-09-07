package nro.entity.boss.map.the23rdmartialartcongress;

/*
 * @Author: DoTheAnh
 */

import nro.service.effect.EffectSkillService;
import nro.entity.player.Player;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;
import nro.service.Service;
import nro.core.util.SkillUtil;
import nro.core.util.Util;
import nro.core.consts.ConstRatio;
import nro.entity.skill.Skill;
import nro.service.skill.SkillService;

public class JackyChun extends The23rdMartialArtCongress {

    public JackyChun(Player player) throws Exception {
        super(PHOBAN, BossID.JACKY_CHUN, BossesData.JACKY_CHUN);
        this.playerAtt = player;
    }

    private long taitaonangluong;
    private long lasttimeres;

    @Override
    public void update() {
        super.update();

        if (this.nPoint != null && this.nPoint.hp <= this.nPoint.hpMax / 20) {
            Service.getInstance().chat(this, "Phục hồi năng lượng " + this.nPoint.getCurrPercentHP() + "%");
        }
    }

    @Override
    public void attack() {
        try {
            if (Util.canDoWithTime(timeJoinMap, 10000)) {
                if (playerAtt != null
                        && playerAtt.location != null
                        && playerAtt.zone != null
                        && this.zone != null
                        && this.zone.equals(playerAtt.zone)) {

                    if (this.isDie()) {
                        return;
                    }

                    if (this.nPoint.hp <= this.nPoint.hpMax / 20) {
                        if (Util.canDoWithTime(taitaonangluong, 10000)) {
                            this.changeStatus(BossStatus.AFK);
                            effectCharger();
                            taitaonangluong = System.currentTimeMillis();
                            lasttimeres = System.currentTimeMillis();
                        }
                    } else if (this.nPoint.hp > this.nPoint.hpMax / 20) {
                        if (Util.canDoWithTime(lasttimeres, 5000)) {
                            this.changeStatus(BossStatus.ACTIVE);
                            this.playerSkill.skillSelect = this.getSkillById(Skill.DRAGON);
                            SkillService.gI().useSkill(this, playerAtt, null, -1, null);
                        }
                    }

                    if (this.playerSkill != null) {
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(
                                Util.nextInt(0, this.playerSkill.skills.size() - 1)
                        );
                    }

                    if (Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                        if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                            goToXY(
                                    playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                    Util.nextInt(10) % 2 == 0
                                            ? playerAtt.location.y
                                            : playerAtt.location.y - Util.nextInt(0, 50),
                                    false
                            );
                        }

                        checkPlayerDie(playerAtt);
                    } else {
                        goToPlayer(playerAtt, false);
                    }
                } else {
                    this.leaveMap();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void effectCharger() {
        if (Util.isTrue(100, ConstRatio.PER100)) {
            EffectSkillService.gI().sendEffectCharge(this);
        }
    }
}