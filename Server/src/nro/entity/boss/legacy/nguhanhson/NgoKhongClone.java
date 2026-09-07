package nro.entity.boss.legacy.nguhanhson;

import nro.core.consts.ConstPlayer;
import nro.entity.boss.*;
import static nro.entity.boss.BossStatus.ACTIVE;
import static nro.entity.boss.BossStatus.JOIN_MAP;
import static nro.entity.boss.BossStatus.RESPAWN;
import nro.service.map.the23rdmartialartcongress.The23rdMartialArtCongressService;
import nro.entity.map.ItemMap;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.service.effect.EffectSkillService;
import nro.server.ServerNotify;
import nro.service.PlayerService;
import nro.service.Service;
import nro.core.util.Util;

public class NgoKhongClone extends Boss {

    private long lastUpdate = System.currentTimeMillis();
    private long timeJoinMap;
    protected Player playerAtt;
    private int timeLive = 200000000;
    long lastTimeBlame;

    public NgoKhongClone(Zone zone, int dame, int hp, int id) throws Exception {
        super(id, new BossData(
                "Ngộ Không Baby",
                ConstPlayer.TRAI_DAT,
                new short[]{462, 463, 464, -1, -1, -1},
                10000,
                new long[]{50000000},
                new int[]{123, 124, 192, 193},
                new int[][]{
                    {Skill.DEMON, 7, Util.nextInt(1, 700)},
                    {Skill.BIEN_KHI, 1, 600000}
                },
                new String[]{},
                new String[]{},
                new String[]{},
                86400
        ));
        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        if (Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(this.zone, 1566, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y), plKill.id);
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

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (Util.neDuocDon(this, plAtt, piercing)) {
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
        if (this.zone != null) {
            ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }
}