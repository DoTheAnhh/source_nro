package nro.entity.mob;

import nro.entity.player.Player;
import nro.net.io.Message;
import nro.service.Service;
import nro.entity.skill.Skill;
import nro.core.util.Util;

public class MobEffectSkill {

    private final Mob mob;

    public MobEffectSkill(Mob mob) {
        this.mob = mob;
    }

    public long lastTimeStun;
    public int timeStun;
    public boolean isStun;

    public boolean isDebuffKyNang;
    public long lastTimeDebuffKyNang;
    public int timeDebuffKyNang;
    public long lastTimeThieuDotKyNang;
    public int thieuDotHpPct;
    public int giamTocDanhKyNang;
    public int giamSatThuongKyNang;
    public Player nguoiGayDebuffKyNang;

    public void update() {
        nro.service.effect.HieuUngPhuKyNangService.update(mob);
        if (isStun && (Util.canDoWithTime(lastTimeStun, timeStun) || mob.isDie())) {
            removeStun();
        }
        if (isThoiMien && (Util.canDoWithTime(lastTimeThoiMien, timeThoiMien) || mob.isDie())) {
            removeThoiMien();
        }
        if (isBlindDCTT && (Util.canDoWithTime(lastTimeBlindDCTT, timeBlindDCTT)) || mob.isDie()) {
            removeBlindDCTT();
        }
        if (isSocola && (Util.canDoWithTime(lastTimeSocola, timeSocola) || mob.isDie())) {
            removeSocola();
        }
        if (isAnTroi && (Util.canDoWithTime(lastTimeAnTroi, timeAnTroi) || mob.isDie())) {
            removeAnTroi();
        }
        if (this.isBinh) {
            if (Util.canDoWithTime(lastTimeBinh, timeBinh) || mob.isDie()) {
                removeBinh();
            }
            if (Util.canDoWithTime(lastTimeMaPhongBa, 500) && !mob.isDie()) {
                if (playerUseMafuba != null && playerUseMafuba.playerSkill != null) {
                    double param = playerUseMafuba.playerSkill.getSkillbyId(Skill.MA_PHONG_BA).point;
                    long subHp = Util.CrisGH((long) playerUseMafuba.nPoint.hpMax * param * (playerUseMafuba.effectSkill.typeBinh == 0 ? 1 : 2) / 100);
                    if (subHp >= this.mob.point.hp) {
                        subHp = (int) (this.mob.point.hp - 1);
                    }
                    this.mob.injured(null, subHp, false);
                }
                this.lastTimeMaPhongBa = System.currentTimeMillis();
            }
        }
    }

    public void xoaDebuffKyNang() {
        isDebuffKyNang = false;
        lastTimeDebuffKyNang = 0;
        timeDebuffKyNang = 0;
        lastTimeThieuDotKyNang = 0;
        thieuDotHpPct = 0;
        giamTocDanhKyNang = 0;
        giamSatThuongKyNang = 0;
        nguoiGayDebuffKyNang = null;
    }

    public boolean isHaveEffectSkill() {
        return isAnTroi || isBlindDCTT || isStun || isThoiMien;
    }

    public void startStun(long lastTimeStartBlind, int timeBlind) {
        this.lastTimeStun = lastTimeStartBlind;
        this.timeStun = timeBlind;
        isStun = true;
    }

    private void removeStun() {
        isStun = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0);
            msg.writer().writeByte(1);
            msg.writer().writeByte(40);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
    public boolean isThoiMien;
    public long lastTimeThoiMien;
    public int timeThoiMien;

    public void setThoiMien(long lastTimeThoiMien, int timeThoiMien) {
        this.isThoiMien = true;
        this.lastTimeThoiMien = lastTimeThoiMien;
        this.timeThoiMien = timeThoiMien;
    }

    public void removeThoiMien() {
        this.isThoiMien = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0); //b5
            msg.writer().writeByte(1); //b6
            msg.writer().writeByte(41); //num6
            msg.writer().writeByte(mob.id); //b7
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public boolean isBlindDCTT;
    public long lastTimeBlindDCTT;
    public int timeBlindDCTT;

    public void setStartBlindDCTT(long lastTimeBlindDCTT, int timeBlindDCTT) {
        this.isBlindDCTT = true;
        this.lastTimeBlindDCTT = lastTimeBlindDCTT;
        this.timeBlindDCTT = timeBlindDCTT;
    }

    public void removeBlindDCTT() {
        this.isBlindDCTT = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0);
            msg.writer().writeByte(1);
            msg.writer().writeByte(40);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public boolean isAnTroi;
    public long lastTimeAnTroi;
    public int timeAnTroi;

    public void setTroi(long lastTimeAnTroi, int timeAnTroi) {
        this.lastTimeAnTroi = lastTimeAnTroi;
        this.timeAnTroi = timeAnTroi;
        this.isAnTroi = true;
    }

    public void removeAnTroi() {
        isAnTroi = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0); //b4
            msg.writer().writeByte(1);//b5
            msg.writer().writeByte(32);//num8
            msg.writer().writeByte(mob.id);//b6
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public boolean isSocola;
    private long lastTimeSocola;
    private int timeSocola;

    public void removeSocola() {
        Message msg;
        this.isSocola = false;
        try {
            msg = new Message(-112);
            msg.writer().writeByte(0);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void setSocola(long lastTimeSocola, int timeSocola) {
        this.lastTimeSocola = lastTimeSocola;
        this.timeSocola = timeSocola;
        this.isSocola = true;
    }

    public boolean isBinh;

    /** Bị trói bởi skin Trói Hồn: không đổi hình thành bình. */
    public boolean troiHon;
    private long lastTimeBinh;
    private int timeBinh;
    private Player playerUseMafuba;
    private long lastTimeMaPhongBa;
    private int typeBinh;

    public void removeBinh() {
        this.isBinh = false;
        this.troiHon = false;
        Service.gI().Send_Body_Mob(mob, 0, -1);
    }

    public void setBinh(Player plAtt, long lastTimeBinh, int timeBinh) {
        int typeBinh = plAtt.newSkill.typeItem;
        this.lastTimeBinh = lastTimeBinh;
        this.timeBinh = timeBinh;
        this.isBinh = true;
        this.typeBinh = typeBinh;
        this.playerUseMafuba = plAtt;
        if (!troiHon) {
            Service.gI().Send_Body_Mob(mob, 1, typeBinh == 0 ? 11175 : 11166);
        }
    }

}
