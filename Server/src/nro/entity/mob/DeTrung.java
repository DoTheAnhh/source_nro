package nro.entity.mob;

import nro.server.Manager;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.core.util.SkillUtil;
import nro.service.Service;
import nro.core.util.Util;
import nro.net.io.Message;

public final class DeTrung extends Mob {

    private Player player;
    private final long lastTimeSpawn;
    private int timeSurvive;

    public DeTrung(Player player) {
        super();
        this.player = player;
        this.id = (int) player.id;
        int level = player.playerSkill.getSkillbyId(12).point;
        this.tempId = SkillUtil.getTempMobMe(level);
        this.point.maxHp = SkillUtil.getHPMobMe(Util.CrisGH(player.nPoint.hpMax), level);
        this.point.dame = SkillUtil.getHPMobMe(Util.CrisGH(player.nPoint.getDameAttack(false)), level);
        int tangDame = nro.repository.dao.SetBonusDAO.tongTheoLoai(
                player, "detrung_dame_pct");
        this.point.dame += this.point.dame * tangDame / 100L;
        this.point.hp = this.point.maxHp;
        this.zone = player.zone;
        this.lastTimeSpawn = System.currentTimeMillis();
        this.timeSurviveGoc = SkillUtil.getTimeSurviveMobMe(level);
        this.timeSurvive = nro.repository.dao.SetBonusDAO.thoiGianSauBonus(
                player, nro.entity.skill.Skill.DE_TRUNG, this.timeSurviveGoc);
        this.phanTramLucNo = phanTramDoHienTai();
        spawn();
    }

    /** Thời gian sống của pet theo chiêu, chưa cộng set/đồ. */
    private int timeSurviveGoc;

    /** Phần trăm sát thương pet từ set/đồ lúc trứng nở. */
    private int phanTramLucNo;

    /** Phần trăm sát thương pet từ set/đồ đang mặc. */
    private int phanTramDoHienTai() {
        if (player == null) {
            return 0;
        }
        return nro.repository.dao.SetBonusDAO.phanTramSkill(player,
                nro.entity.skill.Skill.DE_TRUNG)
                + nro.repository.dao.SetBonusDAO.tongTheoLoai(player, "detrung_dame_pct");
    }

    /**
     * Tính lại thời gian sống theo đồ <b>đang mặc</b> — gọi mỗi lần chủ tính
     * lại chỉ số (mặc/cởi đồ).
     */
    public void tinhLaiTheoDo() {
        if (player != null && timeSurviveGoc > 0) {
            this.timeSurvive = nro.repository.dao.SetBonusDAO.thoiGianSauBonus(
                    player, nro.entity.skill.Skill.DE_TRUNG, timeSurviveGoc);
        }
    }

    @Override
    public void update() {
        if (Util.canDoWithTime(lastTimeSpawn, timeSurvive)) {
            this.mobMeDie();
            this.dispose();
        }
    }
    
    public void attack(Player pl, Mob mob, boolean miss) {
        Message msg;
        try {
            long dameDon = tinhDameDon(miss);
            if (pl != null) {
                long dame = dameDon;
            }

            if (mob != null && dameDon > 0) {
                if (mob.point.gethp() > dameDon) {
                    long tnsm = mob.getTiemNangForPlayer(this.player, dameDon);
                    msg = new Message(-95);
                    msg.writer().writeByte(3);
                    msg.writer().writeInt(this.id);
                    msg.writer().writeInt((int) mob.id);
                    mob.point.sethp((mob.point.gethp() - dameDon));
                    msg.writeCris(Util.CrisGH(mob.point.gethp()), Manager.readInt);
                    msg.writeCris(Util.CrisGH(dameDon), Manager.readInt);
                    Service.gI().sendMessAllPlayerInMap(this.player, msg);
                    msg.cleanup();
                    Service.gI().addSMTN(player, (byte) 2, tnsm, true);
                }
            }
        } catch (Exception e) {
        }
    }

    /** Damage của từng cú pet đánh; chí mạng được quay độc lập ở mỗi cú. */
    private long tinhDameDon(boolean miss) {
        if (miss || player == null || player.nPoint == null) {
            return 0;
        }
        long dame = Util.CrisGH(this.point.dame);
        // Sat thuong pet theo do DANG mac, khong chot luc no: mac set +50%, no
        // trung roi coi ra thi cu tiep theo tut ve dung muc khong set.
        int nay = phanTramDoHienTai();
        if (nay != phanTramLucNo) {
            dame = Util.CrisGH(dame * (100D + nay) / Math.max(1D, 100D + phanTramLucNo));
        }
        int themCrit = nro.repository.dao.SetBonusDAO.phanTramChiMangSkill(
                player, nro.entity.skill.Skill.DE_TRUNG);
        int tiLeCrit = Math.max(0, Math.min(100, player.nPoint.crit + themCrit));
        if (!Util.isTrue(tiLeCrit, 100)) {
            return dame;
        }
        dame = Util.CrisGH(dame * 2D);
        int themSdcm = nro.repository.dao.SetBonusDAO.phanTramSdcmSkill(
                player, nro.entity.skill.Skill.DE_TRUNG);
        int tongSdcm = player.nPoint.tlSDCM + themSdcm;
        return Util.CrisGH(dame + dame * (double) tongSdcm / 100D);
    }
    
    @Override
    public synchronized void injured(Player plAtt, double damage, boolean dieWhenHpFull) {
        Message msg;
        try {
            if (damage > point.maxHp / 20) {
                damage = point.maxHp / 20;
            }
            point.hp -= damage;
            msg = new Message(-95);
            msg.writer().writeByte(5);//type
            msg.writer().writeInt((int) plAtt.id);
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.template.id); // id skill
            msg.writer().writeInt(id); //mob id
            msg.writeCris(Util.CrisGH(damage), Manager.readInt);
            msg.writeCris(Util.CrisGH(point.hp), Manager.readInt);
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
            if (isDie()) {
                mobMeDie();
                dispose();
            }
        } catch (Exception e) {
        }
    }

    //tạo mobme
    public void spawn() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(0);//type
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(this.tempId);
            msg.writeCris(Util.CrisGH(this.point.hp), Manager.readInt);// hp mob
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void goToMap(Zone zone) {
        if (zone != null) {
            this.removeMobInMap();
            this.zone = zone;
        }
    }

    //xóa mobme khỏi map
    private void removeMobInMap() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(7);//type
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void mobMeDie() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(6);//type
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void dispose() {
        player.DeTrung = null;
        this.player = null;
    }
}
