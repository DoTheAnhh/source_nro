package nro.service.effect;

import nro.core.util.Util;
import nro.entity.mob.Mob;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.repository.dao.SetBonusDAO;
import nro.service.PlayerService;
import nro.service.Service;

/** Hiệu ứng phụ do option của kỹ năng gây ra sau một đòn đánh trúng. */
public final class HieuUngPhuKyNangService {

    private static final int THOI_GIAN_GOC = 5000;
    private static final int THOI_GIAN_CHOANG_GOC = 2000;
    private static final int NHIP_THIEU_DOT = 1000;

    private HieuUngPhuKyNangService() {
    }

    public static void apDung(Player nguon, Player dich) {
        if (!hopLe(nguon) || dich == null || dich.effectSkill == null || dich.isDie()) {
            return;
        }
        int id = nguon.playerSkill.skillSelect.template.id;
        int dot = layCoTiLe(nguon, "skill_burn_hp_pct", id);
        int cham = layCoTiLe(nguon, "skill_slow_pct", id);
        int chamDanh = layCoTiLe(nguon, "skill_attack_slow_pct", id);
        int yeu = layCoTiLe(nguon, "skill_weaken_pct", id);
        int choang = layCoTiLe(nguon, "skill_stun_chance_pct", id);
        if (dot <= 0 && cham <= 0 && chamDanh <= 0 && yeu <= 0 && choang <= 0) {
            return;
        }
        int tiLe = lay(nguon, "skill_debuff_chance_pct", id);
        if (tiLe > 0 && !Util.isTrue(Math.min(100, tiLe), 100)) {
            return;
        }
        int thoiGian = thoiGian(nguon, id, false);
        boolean doiToc = cham > dich.effectSkill.giamTocChayKyNang;
        dich.effectSkill.isDebuffKyNang = true;
        dich.effectSkill.lastTimeDebuffKyNang = System.currentTimeMillis();
        dich.effectSkill.timeDebuffKyNang = Math.max(dich.effectSkill.timeDebuffKyNang,
                thoiGian);
        if (dot >= dich.effectSkill.thieuDotHpPct) {
            dich.effectSkill.thieuDotHpPct = dot;
            dich.effectSkill.nguoiGayDebuffKyNang = nguon;
        }
        dich.effectSkill.giamTocChayKyNang = Math.max(
                dich.effectSkill.giamTocChayKyNang, Math.min(90, cham));
        dich.effectSkill.giamTocDanhKyNang = Math.max(
                dich.effectSkill.giamTocDanhKyNang, Math.min(90, chamDanh));
        dich.effectSkill.giamSatThuongKyNang = Math.max(
                dich.effectSkill.giamSatThuongKyNang, Math.min(95, yeu));
        if (choang > 0 && Util.isTrue(Math.min(100, choang), 100)) {
            EffectSkillService.gI().startStun(dich, System.currentTimeMillis(),
                    thoiGian(nguon, id, true));
        }
        if (doiToc && dich.nPoint != null) {
            dich.nPoint.calPoint();
            Service.gI().point(dich);
        }
    }

    public static void apDung(Player nguon, Mob dich) {
        if (!hopLe(nguon) || dich == null || dich.effectSkill == null || dich.isDie()) {
            return;
        }
        int id = nguon.playerSkill.skillSelect.template.id;
        int dot = layCoTiLe(nguon, "skill_burn_hp_pct", id);
        int cham = layCoTiLe(nguon, "skill_slow_pct", id);
        int chamDanh = layCoTiLe(nguon, "skill_attack_slow_pct", id);
        int yeu = layCoTiLe(nguon, "skill_weaken_pct", id);
        int choang = layCoTiLe(nguon, "skill_stun_chance_pct", id);
        if (dot <= 0 && cham <= 0 && chamDanh <= 0 && yeu <= 0 && choang <= 0) {
            return;
        }
        int tiLe = lay(nguon, "skill_debuff_chance_pct", id);
        if (tiLe > 0 && !Util.isTrue(Math.min(100, tiLe), 100)) {
            return;
        }
        dich.effectSkill.isDebuffKyNang = true;
        dich.effectSkill.lastTimeDebuffKyNang = System.currentTimeMillis();
        dich.effectSkill.timeDebuffKyNang = Math.max(dich.effectSkill.timeDebuffKyNang,
                thoiGian(nguon, id, false));
        if (dot >= dich.effectSkill.thieuDotHpPct) {
            dich.effectSkill.thieuDotHpPct = dot;
            dich.effectSkill.nguoiGayDebuffKyNang = nguon;
        }
        // Quái không có chỉ số tốc chạy riêng; làm chậm và giảm tốc đánh đều
        // kéo dài nhịp tấn công của nó.
        dich.effectSkill.giamTocDanhKyNang = Math.max(
                dich.effectSkill.giamTocDanhKyNang, Math.min(90, Math.max(cham, chamDanh)));
        dich.effectSkill.giamSatThuongKyNang = Math.max(
                dich.effectSkill.giamSatThuongKyNang, Math.min(95, yeu));
        if (choang > 0 && Util.isTrue(Math.min(100, choang), 100)) {
            dich.effectSkill.startStun(System.currentTimeMillis(),
                    thoiGian(nguon, id, true));
        }
    }

    public static void update(Player dich) {
        if (dich == null || dich.effectSkill == null
                || !dich.effectSkill.isDebuffKyNang) {
            return;
        }
        if (dich.isDie() || Util.canDoWithTime(dich.effectSkill.lastTimeDebuffKyNang,
                dich.effectSkill.timeDebuffKyNang)) {
            xoa(dich);
            return;
        }
        if (dich.effectSkill.thieuDotHpPct > 0
                && Util.canDoWithTime(dich.effectSkill.lastTimeThieuDotKyNang,
                        NHIP_THIEU_DOT)) {
            Player nguon = dich.effectSkill.nguoiGayDebuffKyNang;
            if (nguon == null || nguon.zone == null || nguon.zone != dich.zone) {
                dich.effectSkill.thieuDotHpPct = 0;
                dich.effectSkill.nguoiGayDebuffKyNang = null;
                return;
            }
            dich.effectSkill.lastTimeThieuDotKyNang = System.currentTimeMillis();
            double dame = Math.max(1D, dich.nPoint.hpMax
                    * (double) dich.effectSkill.thieuDotHpPct / 100D);
            dich.injured(nguon, dame, true, false);
            Service.gI().reload_HP_NV(dich);
            PlayerService.gI().sendInfoHpMpMoney(dich);
        }
    }

    public static void update(Mob dich) {
        if (dich == null || dich.effectSkill == null
                || !dich.effectSkill.isDebuffKyNang) {
            return;
        }
        if (dich.isDie() || Util.canDoWithTime(dich.effectSkill.lastTimeDebuffKyNang,
                dich.effectSkill.timeDebuffKyNang)) {
            xoa(dich);
            return;
        }
        if (dich.effectSkill.thieuDotHpPct > 0
                && Util.canDoWithTime(dich.effectSkill.lastTimeThieuDotKyNang,
                        NHIP_THIEU_DOT)) {
            Player nguon = dich.effectSkill.nguoiGayDebuffKyNang;
            if (nguon == null || nguon.zone == null || nguon.zone != dich.zone) {
                dich.effectSkill.thieuDotHpPct = 0;
                dich.effectSkill.nguoiGayDebuffKyNang = null;
                return;
            }
            dich.effectSkill.lastTimeThieuDotKyNang = System.currentTimeMillis();
            double dame = Math.max(1D, dich.point.maxHp
                    * (double) dich.effectSkill.thieuDotHpPct / 100D);
            dich.injured(nguon, dame, true);
        }
    }

    public static double giamSatThuong(Player nguoiDanh, double dame) {
        if (nguoiDanh == null || nguoiDanh.effectSkill == null) {
            return dame;
        }
        int giam = Math.max(0, Math.min(95,
                nguoiDanh.effectSkill.giamSatThuongKyNang));
        return dame * (100 - giam) / 100D;
    }

    public static int giamTocDanh(Player player) {
        return player == null || player.effectSkill == null ? 0
                : Math.max(0, Math.min(90, player.effectSkill.giamTocDanhKyNang));
    }

    public static double giamSatThuong(Mob nguoiDanh, double dame) {
        if (nguoiDanh == null || nguoiDanh.effectSkill == null) {
            return dame;
        }
        int giam = Math.max(0, Math.min(95,
                nguoiDanh.effectSkill.giamSatThuongKyNang));
        return dame * (100 - giam) / 100D;
    }

    public static int giamTocDanh(Mob mob) {
        return mob == null || mob.effectSkill == null ? 0
                : Math.max(0, Math.min(90, mob.effectSkill.giamTocDanhKyNang));
    }

    public static void xoa(Player dich) {
        if (dich == null || dich.effectSkill == null) {
            return;
        }
        boolean doiToc = dich.effectSkill.giamTocChayKyNang > 0;
        dich.effectSkill.xoaDebuffKyNang();
        if (doiToc && dich.nPoint != null && !dich.isDie()) {
            dich.nPoint.calPoint();
            Service.gI().point(dich);
        }
    }

    public static void xoa(Mob dich) {
        if (dich != null && dich.effectSkill != null) {
            dich.effectSkill.xoaDebuffKyNang();
        }
    }

    private static boolean hopLe(Player nguon) {
        return nguon != null && nguon.playerSkill != null
                && nguon.playerSkill.skillSelect != null
                && nguon.playerSkill.skillSelect.template != null;
    }

    private static int lay(Player p, String loai, int id) {
        return Math.max(0, SetBonusDAO.tongTheoChieu(p, loai, id));
    }

    private static int layCoTiLe(Player p, String loai, int id) {
        return Math.max(0, SetBonusDAO.tongTheoChieuKichHoat(p, loai, id));
    }

    /**
     * Thời gian hiệu ứng: lấy thời gian khai ngay trong dòng set nếu có, không
     * thì thời gian gốc (5 giây, choáng 2 giây); rồi cộng phần trăm từ đồ.
     */
    private static int thoiGian(Player p, int id, boolean choang) {
        int trongDong = SetBonusDAO.thoiGianHieuUngMs(p, id, choang);
        int goc = trongDong > 0 ? trongDong
                : (choang ? THOI_GIAN_CHOANG_GOC : THOI_GIAN_GOC);
        int pct = lay(p, "skill_debuff_duration_pct", id);
        long ra = goc + (long) goc * pct / 100L;
        return ra > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ra;
    }
}
