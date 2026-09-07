package boss.list.Tramhuydiet;

import consts.ConstPlayer;
import QuanLiBoss.Boss;
import QuanLiBoss.BossID;
import QuanLiBoss.BossesData;
import nro.map.ItemMap;
import nro.player.Player;
import nro.server.Manager;
import nro.effect.EffectSkillService;
import QuanLiBoss.Manager.BossManager;
import nro.services.PlayerService;
import nro.services.Service;
import Utils.Util;
import java.util.Random;


public class Champa extends Boss {
    private long lasttimehakai;
    private int timehakai;

    public Champa() throws Exception {
        super(BossID.CHAMPA, BossesData.CHAMPA);
    }

    @Override
    public void reward(Player plKill) {
        byte randomDo = (byte) new Random().nextInt(Manager.itemIds_HD.length - 1);
        ItemMap itemMap;
        if (Util.isTrue(80, 100)) {
            itemMap = Util.ratiDHD(zone, Manager.itemIds_HD[randomDo], 1, this.location.x, this.location.y, plKill.id);                
            Service.gI().dropItemMap(this.zone, itemMap);
    }
    }

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (playerTarger.gender == ConstPlayer.TRAI_DAT) {
            if (plAtt!= null && this.playerTarger == plAtt) {
                Service.gI().chat(this, "|2|Người Trái Đất Đều Yếu Như Ngươi Sao");
                }
            }
        if (playerTarger.gender == ConstPlayer.XAYDA) {
            if (plAtt!= null && this.playerTarger == plAtt) {
                Service.gI().chat(this, "|7|Lũ Xayda Ăn Hại Các Ngươi Mà Cũng Đòi Đánh Nhau Với Ta Sao");
                }
            }
        if (playerTarger.gender == ConstPlayer.NAMEC) {
            if (plAtt!= null && this.playerTarger == plAtt) {
                Service.gI().chat(this, "|1|Tên Da Xanh Cùi Bắp, Còn Chiêu Gì Thì Tung Hết Ra Đây");
                }
            }
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage/2);
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
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        if (BossManager.gI().getBossById(BossID.VADOS) == null) {
            this.leaveMap();
        }
        this.nPoint.khangTDHS = true;
        this.huydiet();
        this.attack();
    }

    private void huydiet() {
        if (!Util.canDoWithTime(this.lasttimehakai, this.timehakai) || !Util.isTrue(10, 100)) {
            return;
        }

        Player pl = this.zone.getRandomPlayerInMap();
        if (pl == null || pl.isDie()) {
            return;
        }
        this.nPoint.dame += (pl.nPoint.dame * 2 / 100);
        this.nPoint.hp += (pl.nPoint.hp * 2 / 100);
        this.nPoint.critg++;
        this.nPoint.calPoint();
        PlayerService.gI().hoiPhuc(this, pl.nPoint.hp, 0);
        pl.injured(null, pl.nPoint.hpMax, true, false);
        Service.gI().sendThongBao(pl, "Bạn Vừa Bị " + this.name + " Cho Bay Màu!");
        this.lasttimehakai = System.currentTimeMillis();
        this.timehakai = Util.nextInt(15000, 25000);
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
