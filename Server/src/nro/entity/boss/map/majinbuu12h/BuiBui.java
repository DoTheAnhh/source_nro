package nro.entity.boss.map.majinbuu12h;

/*
 * @Author: DoTheAnh
 */

import nro.service.effect.EffectSkillService;
import nro.entity.player.Player;
import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.FINAL;
import nro.entity.boss.BossesData;
import nro.server.Manager;
import nro.server.ServerNotify;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import java.util.Random;
import nro.entity.map.ItemMap;
import nro.entity.skill.Skill;

public class BuiBui extends Boss {

    private long lastTimeAfk;

    private long lastTimeChatAfk;

    private int timeChat;

    public BuiBui() throws Exception {
        super(FINAL, BossID.BUI_BUI, BossesData.BUI_BUI);
    }

    @Override
    public void reward(Player plKill) {
        plKill.fightMabu.changePoint((byte) 10);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        byte random = (byte) new Random().nextInt(Manager.itemDC12.length - 1);


        if (Util.isTrue(100, 100)) {
            ItemMap mayluyentap = new ItemMap(this.zone, 521, 1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
            mayluyentap.addOptionParam(1, Util.nextInt(20, 60));
        }

        if (Util.isTrue(100, 100)) {
            ItemMap mayluyentap = new ItemMap(this.zone, 521, 1,
                    this.location.x + Util.nextInt(30, 60),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
            mayluyentap.addOptionParam(1, Util.nextInt(20, 60));
        }

        if (Util.isTrue(100, 100)) {
            ItemMap mayluyentap = new ItemMap(this.zone, 521, 1,
                    this.location.x - Util.nextInt(30, 60),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
            mayluyentap.addOptionParam(1, Util.nextInt(20, 60));
        }

        if (Util.isTrue(100, 100)) {
            ItemMap vang = new ItemMap(this.zone, 190, 30000,
                    this.location.x - 10,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
        }

        if (Util.isTrue(100, 100)) {
            ItemMap vang = new ItemMap(this.zone, 190, 30000,
                    this.location.x - 20,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
        }

        if (Util.isTrue(100, 100)) {
            ItemMap vang = new ItemMap(this.zone, 190, 30000,
                    this.location.x - 40,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
        }

        if (Util.isTrue(100, 100)) {
            ItemMap vang = new ItemMap(this.zone, 190, 30000,
                    this.location.x + 10,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
        }

        if (Util.isTrue(100, 100)) {
            ItemMap vang = new ItemMap(this.zone, 190, 30000,
                    this.location.x + 20,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
        }

        if (Util.isTrue(100, 100)) {
            ItemMap vang = new ItemMap(this.zone, 190, 30000,
                    this.location.x + 40,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y),
                    plKill.id);
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(200, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (plAtt != null && !(plAtt.playerSkill.skillSelect.template.id == Skill.TU_SAT
                    || plAtt.playerSkill.skillSelect.template.id == Skill.MAKANKOSAPPO
                    || plAtt.playerSkill.skillSelect.template.id == Skill.QUA_CAU_KENH_KHI)) {
                if (damage >= this.nPoint.hpMax / 10) {
                    damage = this.nPoint.hpMax / 10;
                }
            }

            if (plAtt != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                    case Skill.LIEN_HOAN:
                        return 0;
                }
            }

            if (plAtt.isPl() && Util.isTrue(1, 5)) {
                plAtt.fightMabu.changePercentPoint((byte) 1);
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
        }

        return 0;
    }

    @Override
    public void afk() {
        if (Util.canDoWithTime(lastTimeChatAfk, timeChat)) {
            this.chat("Đừng vội mừng, ta sẽ hồi sinh và thịt hết bọn mi");
            this.lastTimeChatAfk = System.currentTimeMillis();
            this.timeChat = Util.nextInt(10000, 15000);
        }

        if (Util.canDoWithTime(lastTimeAfk, 60000)) {
            Service.gI().hsChar(this, this.nPoint.hpMax, this.nPoint.mpMax);
            this.changeStatus(BossStatus.CHAT_S);
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }

        this.lastTimeAfk = System.currentTimeMillis();
        this.changeStatus(BossStatus.AFK);
    }
}