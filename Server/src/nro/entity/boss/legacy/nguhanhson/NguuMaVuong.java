package nro.entity.boss.legacy.nguhanhson;

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
import nro.entity.skill.Skill;

public class NguuMaVuong extends Boss {

    long lastTimeBlame;

    public NguuMaVuong(int bossID, BossData bossData, Zone zone, int x, int y) throws Exception {
        super(bossID, bossData);
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void reward(Player plKill) {
        if (Util.isTrue(70, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(1572, 1573), 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y), plKill.id);
        }
    }

    @Override
    public void active() {
        this.changeToTypePK();
        super.active();

        if (System.currentTimeMillis() - lastTimeBlame > 20000) {
            this.chat("*** **, Cay Vcl");
            this.chat("Cút Ra Chỗ Khác Chơi, Bố Đang Cayy");
            this.chat("Cúttttttttttttttttttttttttttt");
            lastTimeBlame = System.currentTimeMillis();
        }
    }

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.DRAGON:
                    case Skill.GALICK:
                    case Skill.DEMON:
                    case Skill.KAIOKEN:
                    case Skill.LIEN_HOAN:
                        int hpHoi = (int) ((long) damage * 80 / 100);
                        PlayerService.gI().hoiPhuc(this, hpHoi, 0);
                        if (Util.isTrue(100, 100)) {
                            this.chat("|7|Hấp thụ.. các ngươi nghĩ sao vậy?");
                        }
                        return 0;
                }
            }

            damage = this.nPoint.subDameInjureWithDeff(damage / 2);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            this.nPoint.subHP(damage);
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
            playerTarger.Boss = false;
        }
    }
}