package boss.map.Nguhanhson;

import consts.ConstPlayer;
import QuanLiBoss.*;
import static QuanLiBoss.BossStatus.ACTIVE;
import static QuanLiBoss.BossStatus.JOIN_MAP;
import static QuanLiBoss.BossStatus.RESPAWN;
import nro.map.The23rdMartialArtCongress.The23rdMartialArtCongressService;
import nro.map.ItemMap;
import nro.map.Zone;
import nro.player.Player;
import nro.skill.Skill;
import nro.effect.EffectSkillService;
import nro.server.ServerNotify;
import nro.services.PlayerService;
import nro.services.Service;
import Utils.Util;

public class NgoKhongClone extends Boss {
  
    private long lastUpdate = System.currentTimeMillis();
    private long timeJoinMap;
    protected Player playerAtt;
    private int timeLive = 200000000;
    
    public NgoKhongClone(Zone zone , int dame, int hp,int id) throws Exception {
        super(id, new BossData(
                "Ngộ Không Baby", //name
                ConstPlayer.TRAI_DAT, //gender
                new short[]{462, 463, 464, -1, -1, -1}, //outfit {head, body, leg, bag, aura, eff}
                (10000), //dame
                new long[]{50000000}, //hp
                new int[]{123,124,192,193}, //map join
                new int[][]{
                {Skill.DEMON, 7, Util.nextInt(1, 700)},
                {Skill.BIEN_KHI, 1, 600000},},
                new String[]{}, //text chat 1
                new String[]{}, //text chat 2
                new String[]{}, //text chat 3
                86400
        ));
        this.zone = zone;
    }
    
    @Override
    public void reward(Player plKill) {
        if (Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(this.zone, 1566, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
            this.location.y), plKill.id);
            Service.getInstance().dropItemMap(this.zone, it);
        }
    }
    
    @Override
    public void active() {
     if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        } 
       try {
            switch (this.bossStatus) {
                case RESPAWN:
                    this.respawn();
                    this.changeStatus(BossStatus.JOIN_MAP);
                case JOIN_MAP:
                    joinMap();
                    if (this.zone != null) {
                        changeStatus(BossStatus.ACTIVE);
                        timeJoinMap = System.currentTimeMillis();
                        this.typePk = 3;
                        The23rdMartialArtCongressService.gI().sendTypePK(playerAtt, this);
                        PlayerService.gI().changeAndSendTypePK(playerAtt, ConstPlayer.PK_PVP);
                        this.changeStatus(BossStatus.ACTIVE);
                    }
                    break;
                case ACTIVE:
                    if (this.playerSkill.prepareTuSat || this.playerSkill.prepareLaze || this.playerSkill.prepareQCKK) {
                        break;
                    } else {
                        this.attack();
                    }
                    break;
            }
            if (Util.canDoWithTime(lastUpdate, 1000)) {
                lastUpdate = System.currentTimeMillis();
                if (timeLive > 0) {
                    timeLive--;
                } else {
                    super.leaveMap();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (System.currentTimeMillis() - lastTimeBlame > Util.nextInt(2000, 10000)) {
            this.chat("Con Sẽ Bảo Vệ Đại Vương!");
            this.chat("Hãy Tiếp Nhận Đòn Đánh Của Ta");
            lastTimeBlame = System.currentTimeMillis();
        }
        
    }
    long lastTimeBlame;
        
    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
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
        ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
    }
}


