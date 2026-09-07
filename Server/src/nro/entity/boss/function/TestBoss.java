package nro.entity.boss.function;

import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossesData;
import nro.entity.player.Player;
import nro.server.Manager;
import nro.entity.skill.Skill;
import nro.service.effect.EffectSkillService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static nro.entity.item.ItemTime.TIME_ITEM_10S;
import nro.service.item.ItemTimeService;
import nro.service.Service;
import nro.service.skill.SkillService;
import nro.core.consts.ConstPlayer;
import nro.core.util.SkillUtil;
import nro.core.util.Util;
import nro.core.consts.ConstTaskBadges;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import nro.service.badges.BadgesTaskService;
import nro.entity.map.ItemMap;

public class TestBoss extends Boss {
        
    public TestBoss() throws Exception {
        super(BossID.TestBoss, BossesData.TestBoss);
    }
    
    @Override
    public void reward(Player plKill) {
        byte randomDo = (byte) new Random().nextInt(Manager.itemDC12.length);
        if (Util.isTrue(30, 100)) {
        } else if (Util.isTrue(30, 100)) {
        } else {
        }
    }
        
    
    
    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (plAtt.isPlayer) {
                damage = 100_000;
            } else {
                damage = 3_000_000;
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
    public void active() {
        super.active();
    }
    
}




