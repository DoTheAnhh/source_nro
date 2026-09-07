package boss.list.Tramhuydiet;

import QuanLiBoss.Boss;
import QuanLiBoss.BossID;
import nro.effect.EffectSkillService;
import QuanLiBoss.BossesData;
import nro.map.ItemMap;
import nro.player.Player;
import QuanLiBoss.Manager.BossManager;
import nro.server.Manager;
import nro.services.Service;
import Utils.Util;
import java.util.Random;

public class Whis extends Boss {

    public Whis() throws Exception {
        super(BossID.WHIS, BossesData.WHIS);
    }

    @Override
    public void reward(Player plKill) {
        byte randomDo = (byte) new Random().nextInt(Manager.itemIds_HD.length - 1);
        ItemMap itemMap;
        if (Util.isTrue(50, 100)) {
            itemMap = Util.ratiDHD(zone, Manager.itemIds_HD[randomDo], 1, this.location.x, this.location.y, plKill.id);                
            Service.gI().dropItemMap(this.zone, itemMap);
    }
    }

    @Override
    public void active() {
        if (BossManager.gI().getBossById(BossID.BERUS) == null) {
            this.leaveMap();
        }
        this.nPoint.khangTDHS = true;
        super.active();
    }
    
    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (Util.isTrue(50, 100) && plAtt != null) {//tỉ lệ hụt của thiên sứ
            Util.isTrue(this.nPoint.tlNeDon, 1);
            if (Util.isTrue(80, 100)) {
                this.chat("Hãy Để Bản Năng Tự Vận Động");
                this.chat("Tránh Các Động Tác Thừa");
            } else if (Util.isTrue(80, 100)) {
                this.chat("Đây Chính Là Bản Năng Vô Cực");
            }
            damage = 0;

        }
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1)) {
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
            if (damage >= 1) {
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
        super.joinMap();
    }
    
    @Override
    public void leaveMap() {
        super.leaveMap();
    }
}
