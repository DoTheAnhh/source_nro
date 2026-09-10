package nro.entity.mob;

import nro.service.inventory.InventoryService;
import nro.service.Service;
import nro.service.item.ItemMapService;
import nro.service.TaskService;
import nro.service.item.ItemService;
import nro.service.MapService;
import nro.core.consts.ConstMap;
import nro.core.consts.ConstMob;
import nro.core.consts.ConstTask;
import nro.entity.map.ItemMap;
import java.util.List;
import nro.entity.map.Zone;
import nro.entity.player.Location;
import nro.entity.player.Detu;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.net.io.Message;
import nro.server.Manager;
import nro.core.util.Util;
import java.util.ArrayList;
import nro.server.Maintenance;
import nro.server.ServerManager;
import nro.core.log.Logger;
import nro.core.util.TimeUtil;
import nro.core.consts.ConstAttribute;
import nro.core.consts.ConstTaskBadges;
import java.util.Collections;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.achievement.AchievementService;
import nro.entity.attribute.Attribute;
import nro.service.badges.BadgesTaskService;
import nro.entity.boss.map.trainingboss.TrainningService;

public class Mob {

    private static Mob i;

    public static Mob gI() {
        if (i == null) {
            i = new Mob();
        }
        return i;
    }

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;

    public List<Player> temporaryEnemies = new ArrayList<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;

    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;
    public int percent_gold;

    private long lastTimeAttackPlayer;
    private long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    //------BY DoTheAnh-----
    public int MobImage;

    public boolean isMobMe;

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp(this.point.getHpFull());
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
    }

    public void setTiemNang() {
        this.maxTiemNang = (long) this.point.getHpFull() * (this.pTiemNang + Util.nextInt(-2, 2)) / 100;
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    public boolean isSieuQuai() {
        return this.lvMob > 0;
    }


    /**
     * Hut mau: hoi HP cho nguoi danh theo phan tram sat thuong vua gay ra.
     *
     * <p>Dung chi so <b>option 95 "Bien #% tan cong thanh HP"</b> — von da co
     * san trong game: no duoc cong don vao {@code nPoint.tlHutHp}, hien trong
     * bang chi so cua Ong Gohan, va duoc gan cho bot. Chi thieu dung buoc hoi
     * mau nay nen truoc gio chi so do khong co tac dung gi.</p>
     *
     * <p>Vi du: sat thuong 10.000, hut mau 50% -> hoi 5.000 HP.</p>
     *
     * <p>Chi gui goi cap nhat khi THUC SU hoi duoc mau. Danh quai lien tuc ma
     * lan nao cung gui goi du dang day mau la phi bang thong vo ich.</p>
     */
    private void apHutMau(Player plAtt, double damage) {
        try {
            if (plAtt == null || plAtt.nPoint == null || damage <= 0 || plAtt.isDie()) {
                return;
            }
            int phanTram = plAtt.nPoint.tlHutHp;
            if (phanTram <= 0) {
                return;
            }
            long thieu = plAtt.nPoint.hpMax - plAtt.nPoint.hp;
            if (thieu <= 0) {
                return;
            }
            long hoi = (long) (damage * phanTram / 100.0);
            if (hoi <= 0) {
                return;
            }
            if (hoi > thieu) {
                hoi = thieu;
            }
            plAtt.nPoint.addHp(hoi);
            nro.service.PlayerService.gI().sendInfoHpMpMoney(plAtt);
        } catch (Exception ex) {
            Logger.logException(Mob.class, ex, "Loi hut mau");
        }
    }

    public void injured(Player plAtt, double damage, boolean dieWhenHpFull) {
        long startTotal = System.currentTimeMillis();

        if (this.isDie()) {
            return;
        }
        // --- CHẶN PLAYER CHÍNH GÂY DAMAGE Ở MAP 249 ---
        if (plAtt != null && plAtt.zone != null && plAtt.zone.map.mapId == 179) {
            // Nếu là người chơi chính thì không gây damage
            if (!plAtt.isDeTu) {
                damage = 0;
            }
        }

        long start;
        long elapsed;

        start = System.currentTimeMillis();
        if (damage >= this.point.hp) {
            damage = this.point.hp;
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] damage cap to current HP: " + elapsed + "ms");
        }

        // Xử lý logic khi không cho phép chết ngay khi HP đầy
        start = System.currentTimeMillis();
        if (!dieWhenHpFull) {
            if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                if (MapService.gI().isMapRiengTu(this.zone.map.mapId)) {
                    damage = this.point.hp - 0;
                } else {
                    damage = this.point.hp - 1;
                }
            }
            if (this.tempId == ConstMob.HIRUDEGARN) {
                double maxDamageAtFullHP = 20_000_000;
                double hpRatio = (double) this.point.hp / this.point.maxHp;
                double power = 0.8;
                int calcDamage = (int) (maxDamageAtFullHP * Math.pow(hpRatio, power));
                int minDamage = (int) (calcDamage * 0.5);
                int maxDamage = Math.max(calcDamage, 10);
                damage = Util.nextInt(minDamage, maxDamage);
                if (damage < 10) {
                    damage = 10;
                }
            }
            if ((this.tempId == ConstMob.MOC_NHAN || this.tempId == ConstMob.BU_NHIN_MA_QUAI) && damage > this.point.maxHp / 10) {
                damage = this.point.maxHp / 10;
            }
            if (plAtt != null && MapService.gI().isMapDiaNguc(plAtt.zone.map.mapId)) {
                damage = this.point.maxHp / 10;
            }
            if (plAtt != null && MapService.gI().isMapNguHanhSon(plAtt.zone.map.mapId)) {
                damage = 80000;
            }
            if (plAtt != null && !MapService.gI().isMapHungVuongEvent(plAtt.zone.map.mapId)
                    && (this.tempId == ConstMob.VOI_CHIN_NGA || this.tempId == ConstMob.GA_CHIN_CUA || this.tempId == ConstMob.NGUA_CHIN_LMAO)
                    && damage > this.point.maxHp / 10) {
                damage = this.point.maxHp / 10;
            }
            if (plAtt != null && MapService.gI().isMapHungVuongEvent(plAtt.zone.map.mapId)) {
                if (this.tempId == ConstMob.VOI_CHIN_NGA && damage > 100_000) {
                    damage = 100_000;
                }
                if (this.tempId == ConstMob.NGUA_CHIN_LMAO && damage > 75_000) {
                    damage = 75_000;
                }
                if (this.tempId == ConstMob.GA_CHIN_CUA && damage > 60_000) {
                    damage = 60_000;
                }
            }
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] dieWhenHpFull checks & damage adjustment: " + elapsed + "ms");
        }

        // Tăng damage theo các hiệu ứng từ plAtt
        start = System.currentTimeMillis();
        if (plAtt != null) {
            if (this.tempId == ConstMob.MAY_DO_SUC_MANH) {
                plAtt.ghiNhanDamMayDam((long) damage);
            }
            int TlDameFly = plAtt.nPoint.tlDameMobFly;
            int TlDameMonkey = plAtt.nPoint.tlDameMobMonkey;
            int TlDameRun = plAtt.nPoint.tlDameMobRun;
            if (TlDameFly > 0 && isMobBay()) {
                damage += Util.CrisGH((damage / 100) * TlDameFly);
            }
            if (TlDameMonkey > 0 && isMobKhi()) {
                damage += Util.CrisGH((damage / 100) * TlDameMonkey);
            }
            if (TlDameRun > 0 && isMobMatDat()) {
                damage += Util.CrisGH((damage / 100) * TlDameRun);
            }
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] apply plAtt damage modifiers: " + elapsed + "ms");
        }

        // Kiểm tra map KhiGasHuyDiet và giới hạn damage theo skill
        start = System.currentTimeMillis();
        if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
            boolean mob76Die = true;
            for (Mob mob : this.zone.mobs) {
                if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                    mob76Die = false;
                    break;
                }
            }
            if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.LIEN_HOAN:
                    case Skill.ANTOMIC:
                    case Skill.MASENKO:
                    case Skill.KAMEJOKO:
                        damage = 1;
                        break;
                }
            }
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] map KhiGasHuyDiet checks: " + elapsed + "ms");
        }

        // Kiểm tra điều kiện đặc biệt khi không cho chết và map
        start = System.currentTimeMillis();
        if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0
                && plAtt != null && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
            damage = ((this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000) * (10.0 / 100));
            this.mobAttackPlayer(plAtt);
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] special death & map checks: " + elapsed + "ms");
        }

        // Mob tấn công lại player nếu là boss
        start = System.currentTimeMillis();
        if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2) && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
            this.mobAttackPlayer(plAtt);
            lastTimeAttackPlayer = System.currentTimeMillis();
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] boss retaliate attack: " + elapsed + "ms");
        }

        // Trừ HP mob
        start = System.currentTimeMillis();
        this.point.hp -= damage;
        apHutMau(plAtt, damage);
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] subtract HP: " + elapsed + "ms");
        }

        // Thêm vào danh sách kẻ thù tạm thời
        start = System.currentTimeMillis();
        addTemporaryEnemies(plAtt);
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] addTemporaryEnemies: " + elapsed + "ms");
        }

        // Xử lý khi mob chết hoặc còn sống
        start = System.currentTimeMillis();
        if (this.isDie()) {
            this.status = 0;
            this.setDie();
            this.temporaryEnemies.clear();
            if (plAtt != null) {
                this.sendMobDieAffterAttacked(plAtt, Util.CrisGH(damage));
                // Nhiệm vụ danh hiệu loại "giết quái" — đếm tự động theo mẫu
                // trên panel, không cần gõ cứng id nhiệm vụ trong mã.
                nro.service.badges.BadgesTaskService.tangTheoLoai(plAtt,
                        nro.entity.badges.BadgesTaskTemplate.GIET_QUAI,
                        this.tempId, 1);
                TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                if (plAtt.isPl()) {
                    // Nhiem vu KOL da go — khong con dem quai da diet.
                }
                TaskService.gI().checkDoneEventTaskKillMob(plAtt, this);
            }
            if (this.id == 13) {
                this.zone.isbulon1Alive = false;
            }
            if (this.id == 14) {
                this.zone.isbulon2Alive = false;
            }
        } else {
            this.sendMobStillAliveAffterAttacked(damage, plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 50) {
            System.out.println("[SLOW] mob death/alive handling: " + elapsed + "ms");
        }

        // Tiềm năng trả MỘT LẦN cho mỗi cú đánh.
        //
        // Ba lỗi của đoạn cũ, cả ba đều lặng lẽ:
        //
        //  1. Người đang có "người yêu" được cộng HAI LẦN — một lần ở nhánh
        //     isNguoiYeu, rồi lại một lần ở khối chung ngay dưới. Cùng một con
        //     số, cùng một cú đánh.
        //  2. tangTnsmLuyenTap gọi hai lần cho MỌI người, không kèm điều kiện
        //     nào — chỉ là một dòng bị dán lại.
        //  3. getTiemNangForPlayer chạy hai lần khi có người yêu; hàm đó không
        //     rẻ, mà nó nằm trong đường đi của từng cú đánh của từng người.
        //
        // Bỏ luôn cả loạt System.out.println đo giờ. Chúng in thẳng ra console
        // từ trong vòng lặp đánh quái, và console của máy chủ là một khoá chung
        // — đông người thì chính mấy dòng đo ấy làm chậm cái mà nó đang đo.
        if (plAtt != null) {
            if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                plAtt.satellite.isDefend = false;
            }
            long tiemNang = getTiemNangForPlayer(plAtt, damage);
            Service.gI().addSMTN(plAtt, (byte) 2, tiemNang, true);
            TrainningService.gI().tangTnsmLuyenTap(plAtt, tiemNang);
        }
    }

    public long getTiemNangForPlayer(Player pl, double dame) {
        long startTotal = System.currentTimeMillis();

        int levelPlayer = Service.getInstance().getCurrLevel(pl);

        long start = System.currentTimeMillis();
        int n = levelPlayer - this.level;
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 10) {
            System.out.println("[SLOW] calculate level difference n: " + elapsed + "ms");
        }

        start = System.currentTimeMillis();
        long pDameHit;
        if (point.getHpFull() >= 100000000) {
            pDameHit = Util.CrisGH(dame) * 500 / point.getHpFull();
        } else {
            pDameHit = Util.CrisGH(dame) * 100 / point.getHpFull();
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 10) {
            System.out.println("[SLOW] calculate pDameHit: " + elapsed + "ms");
        }

        start = System.currentTimeMillis();
        long tiemNang = pDameHit * maxTiemNang / 100;
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 10) {
            System.out.println("[SLOW] initial tiemNang calculation: " + elapsed + "ms");
        }

        start = System.currentTimeMillis();
        if (n >= 0) {
            for (int j = 0; j < n; j++) {
                long sub = tiemNang * 10 / 100;
                if (sub <= 0) {
                    sub = 1;
                }
                tiemNang -= sub;
            }
        } else {
            for (int j = 0; j < -n; j++) {
                long add = tiemNang * 10 / 100;
                if (add <= 0) {
                    add = 1;
                }
                tiemNang += add;
            }
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 10) {
            System.out.println("[SLOW] adjust tiemNang in loop by n: " + elapsed + "ms, n=" + n);
        }

        start = System.currentTimeMillis();
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (this.isSieuQuai()) {
            tiemNang *= 1;
        }
        if (this.tempId == ConstMob.MAY_DO_SUC_MANH) {
            tiemNang = 1;
        }
        if (this.tempId == ConstMob.HIRUDEGARN) {
            tiemNang = 1;
        }
        elapsed = System.currentTimeMillis() - start;
        if (elapsed > 10) {
            System.out.println("[SLOW] apply special case adjustments: " + elapsed + "ms");
        }

        // He so tiem nang theo BAN DO — nhan NGAY VAO GIA TRI GOC.
        //
        // Dat truoc calSucManhTiemNang, tuc la truoc bua, item, buff, co, the
        // nap: he so ban do la "con quai o day dang gia gap may lan", nen no
        // phai nhan vao chinh con so goc, roi moi den luot cac thu cong them
        // cua nguoi choi tinh tren con so da nhan ay.
        //
        // Ap o Mob chu khong o NPoint vi day biet BAN DO CUA CON QUAI: de tu cay
        // trong Ngu Hanh Son thi phan chia cho su phu cung phai theo he so cua
        // Ngu Hanh Son, ma luc ay su phu co the dang dung o ban do khac — hoi
        // ban do cua su phu la hoi nham nguoi.
        //
        // Tra ve 0 nghia la nhom ay khoa nguoi choi thuong (Ngu Hanh Son: chi de
        // tu danh moi duoc tiem nang).
        if (this.zone != null && this.zone.map != null) {
            double heSoMap = nro.repository.dao.HeSoTnsmDAO.heSo(
                    this.zone.map.mapId, pl.isDeTu);
            if (heSoMap <= 0) {
                return 0;
            }
            if (heSoMap != 1) {
                tiemNang = Math.round(tiemNang * heSoMap);
                if (tiemNang <= 0) {
                    // He so nho lam tron xuong 0 thi coi nhu duoc mot diem, con
                    // hon danh ca buoi khong len duoc gi.
                    tiemNang = 1;
                }
            }
        }

        start = System.currentTimeMillis();
        tiemNang = Util.CrisGH(pl.nPoint.calSucManhTiemNang(tiemNang));

        // He so tiem nang CHUNG cho moi ban do, chinh tren panel.
        //
        // Nhan sau cung, khi da tinh xong het. Vi moi thu o day deu la phep
        // nhan nen no khong thay the he so rieng cua tung ban do ma chong len
        // tren: ban do von x1 voi he so 0.2 thanh x0,2; ban do von x3 thanh
        // x0,6. De 1 thi con so y het nhu truoc.
        tiemNang = nro.repository.dao.ConfigDAO.nhanTiLe(
                nro.repository.dao.ConfigDAO.TL_EXP, tiemNang);

        long totalElapsed = System.currentTimeMillis() - startTotal;
        if (totalElapsed > 50) {
            System.out.println("[SLOW] Total getTiemNangForPlayer time: " + totalElapsed + "ms for player: " + pl.name);
        }

        return tiemNang;
    }

    public void update() {
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }
        if (!this.isDie() && this.tempId == ConstMob.MAY_DO_SUC_MANH) {
            this.point.hp = 2000000000;
        }
        if (this.isDie() && !Maintenance.isRunning && !isBigBoss()) {
            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI: {
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive && Util.canDoWithTime(lastTimeDie, 10000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                }
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    break;
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                    break;
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                    break;
                case ConstMap.MAP_BOSS_BANG_HOI:
                    break;
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default: {
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        long hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
                    }
                    if (Util.canDoWithTime(lastTimeDie, 5000) && isDie()) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }

                }
            }
        }
        effectSkill.update();
        attack();
    }
    // dotheanh mãi đỉnh
    //--------------------------------------------------------------------------
    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN || this.tempId == ConstMob.VUA_BACH_TUOC || this.tempId == ConstMob.ROBOT_BAO_VE || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA || this.tempId == ConstMob.GA_CHIN_CUA || this.tempId == ConstMob.NGUA_CHIN_LMAO || this.tempId == ConstMob.PIANO
                || this.tempId == ConstMob.KONG || this.tempId == ConstMob.GOZILLA);
    }

    public boolean isBigBossHungVuongEvent() {
        return (this.tempId == ConstMob.VOI_CHIN_NGA || this.tempId == ConstMob.GA_CHIN_CUA || this.tempId == ConstMob.NGUA_CHIN_LMAO || this.tempId == ConstMob.KONG || this.tempId == ConstMob.GOZILLA);
    }

    public boolean isMobBay() {
        return this.type == 4;
    }

    public boolean isMobKhi() {
        return (this.tempId == ConstMob.KHI_GIAP_SAT || this.tempId == ConstMob.KHI_LONG_DEN || this.tempId == ConstMob.KHI_LONG_DO || this.tempId == ConstMob.KHI_LONG_VANG
                || this.tempId == ConstMob.KHI_LONG_XANH);
    }

    public boolean isMobHeo() {
        return (this.tempId == ConstMob.HEO_DA_XANH || this.tempId == ConstMob.HEO_RUNG || this.tempId == ConstMob.HEO_RUNG_ME || this.tempId == ConstMob.HEO_XANH_ME
                || this.tempId == ConstMob.HEO_XAYDA || this.tempId == ConstMob.HEO_XAYDA_ME);
    }

    public boolean isMobMatDat() {
        return this.type == 1;
    }

    public void attack() {
        Player player = getPlayerCanAttack();
        if (!isDie() && !effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN && tempId != ConstMob.MAY_DO_SUC_MANH
                && tempId != ConstMob.BU_NHIN_MA_QUAI && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss()
                && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId))
                && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
            if (player != null) {
                this.mobAttackPlayer(player);
            }
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            return plAttack;
        }

        int distance = 100;
        try {
            List<Player> players = this.zone.getNotBosses();
            if (players == null || players.isEmpty()) {
                return null; // Không có player nào trong zone
            }

            for (Player pl : players) {
                if (pl == null) {
                    continue;
                }

                boolean canAttack = !pl.isDie()
                        && !pl.isBoss
                        && !pl.isPetFollow
                        && !pl.isDuongTang
                        && (pl.satellite == null || !pl.satellite.isDefend)
                        && (pl.effectSkin == null || !pl.effectSkin.isVoHinh)
                        && (this.tempId > 18 || (this.tempId > 9 && this.type == 4) || isBigBoss());

                if (canAttack) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }

            this.timeAttack = 2000;
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }

        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (Player plAttt : new ArrayList<>(playersMap)) {
                    if (plAttt == null) {
                        continue;
                    }
                    if (plAttt.isDie()
                            || plAttt.isBoss
                            || (plAttt.satellite != null && plAttt.satellite.isDefend)
                            || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh)
                            || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        double dameMob = Util.CrisGH(this.point.getDameAttack());
        if (dameMob > 2_000_000_000) {
            dameMob = 2_000_000_000;
        }
        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (player.clan != null && player.clan.BuaDaTrau > System.currentTimeMillis()) {
            int clanLevel = player.clan.level;
            int bonusPercent = clanLevel * 20;
            if (bonusPercent > 200) {
                bonusPercent = 200;
            }
            dameMob -= (dameMob * bonusPercent / 100);
        }
        if (player.isDeTu && ((Detu) player).master.charms != null && ((Detu) player).master.charms.tdDeTu > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (this.lvMob > 0 && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            dameMob = (player.nPoint.hpMax * (10.0 / 100));
        }
        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }
        if (player.itemTime != null && player.itemTime.iscommenson) {
            dameMob = Math.round(dameMob * 0.1);
        }
        if (this.lvMob > 0 && player.charms.tdOaiHung > System.currentTimeMillis()) {
            dameMob = 0;
        }
        if (MapService.gI().isMapNguHanhSon(player.zone.map.mapId)) {
            dameMob = player.nPoint.hpMax / 10;
        }
        double dame = player.injured(null, Util.CrisGH(dameMob), false, true);
        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, double dame) {
        if (!player.isDeTu && !player.isBo && !player.isMe && !player.isPetFollow && !player.isDuongTang && !player.isBot && !player.isBot_Event && !player.isBot_New && !player.isBot_Valentine && !player.isNguoiYeu && !player.isConOne && !player.isConTwo && !player.isConThree) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writeCris(Util.CrisGH(dame), Manager.readInt);
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Mob.class, e);
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writeCris(Util.CrisGH(player.nPoint.hp), Manager.readInt);
            Service.getInstance().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
    }

    /**
     * Bốc xem con này có thành <b>siêu quái</b> không, và dựng máu cho nó.
     *
     * <h2>Hai lỗi của bản cũ</h2>
     *
     * <p><b>1. Máu vượt trần máu.</b> Nó nhân {@code hp} lên mười lần nhưng để
     * nguyên {@code maxHp}. Client vẽ thanh máu bằng {@code hp/maxHp} nên hiện
     * ra "280.000/40.000" — số bên trái lớn hơn số bên phải, thanh máu tràn ra
     * ngoài khung. Đánh mãi mới thấy thanh nhúc nhích vì phải hạ hết chín phần
     * thừa mới chạm tới phần vẽ được.</p>
     *
     * <p><b>2. Siêu quái tự báo mình không phải siêu quái.</b> Vòng lặp đầu
     * duyệt <i>cả chính nó</i>: con đã là siêu quái (do bản đồ đặt sẵn, như Con
     * Đường Rắn Độc hay Kho Báu Dưới Biển) thì gặp ngay chính mình ở vòng đầu
     * và trả về 0. Mà {@code sendMobHoiSinh} gửi thẳng con số ấy xuống client,
     * nên client vẽ nó như quái thường: không hào quang, không khung riêng.</p>
     *
     * <p>Nay con đã là siêu quái thì trả về đúng bậc của nó và không bốc lại —
     * bốc lại còn có thể nhân máu chồng lên nhau.</p>
     */
    public int lvMob() {
        // Da la sieu quai roi: giu nguyen, khong boc lai, khong dung toi mau.
        if (this.lvMob > 0) {
            return this.lvMob;
        }
        // Moi khu chi mot sieu quai. Bo qua chinh minh o vong nay — nhanh tren
        // da lo truong hop do.
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap != this && mobMap.lvMob > 0) {
                return 0;
            }
        }
        this.lvMob = (this.tempId > 18 && !isBigBoss() && Util.isTrue(10, 100)) ? 1 : 0;
        if (this.lvMob > 0) {
            // Nang CA HAI so. Tran hai ti giu nguyen y bang cu: mau quai gui
            // xuong client bang so nguyen co dau.
            long mauMoi = this.point.maxHp <= 20_000_000L
                    ? this.point.maxHp * 10L : 2_000_000_000L;
            this.point.maxHp = mauMoi;
        }
        this.point.hp = this.point.maxHp;
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(lvMob());
            msg.writeCris(Util.CrisGH(this.point.hp), Manager.readInt);
//            msg.writeLong(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendMobMaxHp(long maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writeCris(Util.CrisGH(maxHp), Manager.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void hoi_hp(long hp) {
        Message msg = null;
        try {
            this.point.sethp(this.point.gethp() + hp);
            long HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeCris(Util.CrisGH(this.point.gethp()), Manager.readInt);
            msg.writeCris(Util.CrisGH(HP), Manager.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeCris(Util.CrisGH(this.point.gethp()), Manager.readInt);
            msg.writeCris(Util.CrisGH(this.point.gethp()), Manager.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, double dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writeCris(Util.CrisGH(dameHit), Manager.readInt);
            msg.writer().writeBoolean(plKill.nPoint.isCrit);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            if (plKill.isPl()) {
                List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
                hutItem(plKill, items);
            }
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    private ItemMap createItemMap(Player player, int itemId, int x, int y, ItemOption... options) {
        ItemMap itemMap = new ItemMap(this.zone, itemId, 1, x + Util.nextInt(-10, 10), y, player.id);
        Collections.addAll(itemMap.options, options);
        return itemMap;
    }

    private ItemOption opt(int id, int param) {
        return new ItemOption(id, param);
    }

    private List<ItemMap> getItemEventMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();
        if (player.isBoss || this.tempId == 0) {
            return list;
        }

        // Truoc day moi mua le mot khoi if gõ cứng o day, va tat ca da bi xoa
        // rong — ham nay chi con lai cac dong chu thich, tuc may chu khong roi
        // vat pham su kien nao ca. Nay danh sach nam trong bang su_kien_roi:
        // moi mon mot ti le rieng, va chi roi khi su kien dang chay.
        for (int[] roi : nro.service.event.SuKienService.roiTuQuai(player)) {
            list.add(new ItemMap(this.zone, roi[0], roi[1],
                    x + Util.nextInt(-10, 10), yEnd, player.id));
        }
        return list;
    }

    //--------------------------------------------------------------------------
    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isDeTu && !player.isBo && !player.isMe && !player.isPetFollow && !player.isPhanThan && !player.isNguoiYeu && !player.isConOne && !player.isConTwo && !player.isConThree) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        } else if (player.isMaster()) {
            if (player.getMaster().charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player.getMaster(), item.itemMapId, true);
                }
            }
        } else {
            if (((Detu) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Detu) player).master, item.itemMapId, true);
                }
            }
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));
            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size()); //sl item roi
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);// itemmapid
                msg.writer().writeShort(itemMap.itemTemplate.id); // id item
                msg.writer().writeShort(itemMap.x); // xend item
                msg.writer().writeShort(itemMap.y); // yend item
                msg.writer().writeInt((int) itemMap.playerId); // id nhan vat
            }
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
        return itemReward;
    }

    private int getGoldDrop(Player player, Mob mob) {
        // Dat CA HAI o vang_roi_min va vang_roi_max thi boc deu trong khoang
        // do, bo qua cong thuc. Cong thuc cu chia cho can cua suc manh roi nhan
        // log mau quai — cang manh cang it vang, va khong ai nhin ra mot con
        // quai cho bao nhieu.
        //
        // Chi dat mot o thi van la san/tran ap len cong thuc nhu truoc.
        long min = nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.VANG_ROI_MIN);
        long max = nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.VANG_ROI_MAX);
        if (min > 0 && max > 0) {
            if (max < min) {
                long t = min;
                min = max;
                max = t;
            }
            return (int) Math.max(1, Math.min(Integer.MAX_VALUE,
                    max > min ? Util.nextInt((int) min, (int) max) : min));
        }
        long playerPower = player.nPoint.power;
        long mobHp = mob.point.maxHp;
        double baseGold = (mobHp / 200.0) * Util.nextInt(20, 30);
        double smFactor = 1 + Math.pow(playerPower / (mobHp * 1000.0 + 1), 0.5);
        double totalGold = baseGold / smFactor;
        totalGold *= Math.log10(mobHp + 1);
        // He so ngau nhien phai nhan TRUOC khi cat tran.
        // Truoc day cat tran 300.000 roi moi nhan 0,5-0,7 -> vang thuc te khong
        // bao gio vuot 210.000, tran 300.000 la con so khong bao gio cham toi.
        totalGold *= 0.5 + Math.random() * 0.2;
        if (totalGold > 300_000) {
            totalGold = 300_000;
        }
        return Math.max(1, (int) totalGold);
    }

    /** Nhân cả hai vế lên trước khi làm tròn, để hệ số nhỏ hơn 1 không mất. */
    private static final long THANG_ROI = 1_000_000L;

    /**
     * Gieo tỉ lệ rơi, phần triệu.
     *
     * <p>Không còn hệ số chung nào nhân vào đây nữa: tỉ lệ của từng món đã khai
     * riêng ở tab "Đồ rơi từ quái". Có thêm một hệ số nhân đè lên thì con số
     * ghi trong bảng không còn là con số thật, mà chẳng ai nhớ nổi đang nhân
     * mấy.</p>
     */
    private static boolean roiTheoTiLe(int tuSo, int mauSo) {
        return Util.isTrue(tuSo, mauSo);
    }

    private void dropGold(Player player, Mob mob, List<ItemMap> list, Zone zone, int x, int y) {
        Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.VANG);
        int goldAmount = getGoldDrop(player, mob);
        if (goldAmount <= 0) {
            return;
        }
        goldAmount = goldAmount + (this.percent_gold - 1);
        double playerBonus = 1.0 + player.nPoint.tlGold / 100.0;
        double totalGold = goldAmount * playerBonus;
        if (at != null && !at.isExpired()) {
            totalGold *= at.getValue() / 100.0;
        }
        // Khong con he so vang chung. Muon chinh so vang thi dat hai o
        // "Vang roi it nhat / nhieu nhat" — hai o do noi thang bao nhieu vang,
        // con mot he so nhan thi phai tu doan ra so cuoi cung.
        // TRAN phai nhan theo he so. Truoc day tran 300.000 co dinh duoc ap
        // SAU khi nhan, ma getGoldDrop() cung da cat o 300.000 mot lan roi —
        // nen dat he so 5 tren panel thi quai bao vang van chi roi toi da
        // 300.000 y nhu cu, tuong la he so khong an gi. Nhan tran len theo
        // dung he so thi "gap 5" moi thuc su la gap 5.
        // San / tran lay tu panel (vang_roi_min, vang_roi_max). De 0 la giu nguyen
        // hanh vi cu: khong co san, va tran van la 300.000 nhan he so vang.
        long vangMin = nro.repository.dao.ConfigDAO.num(nro.repository.dao.ConfigDAO.VANG_ROI_MIN);
        long vangMax = nro.repository.dao.ConfigDAO.num(nro.repository.dao.ConfigDAO.VANG_ROI_MAX);
        long tran = vangMax > 0 ? vangMax : 300_000L;
        // Go nham cho san cao hon tran thi keo san ve bang tran, khong de tran/san
        // mau thuan nhau lam vang roi ra so am hoac nhay lung tung.
        if (vangMin > tran) {
            vangMin = tran;
        }
        long g = Math.round(totalGold);
        if (g > tran) {
            g = tran;
        }
        if (vangMin > 0 && g < vangMin) {
            g = vangMin;
        }
        if (g <= 0) {
            g = 1;
        }
        int finalGold = (int) Math.min(g, Integer.MAX_VALUE);
        ItemMap itemMap = new ItemMap(zone, 190, finalGold, x + Util.nextInt(-10, 10), y, player.id);
        list.add(itemMap);
    }

    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();

        // Nhiệm vụ huy hiệu
        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);

        // Không cho boss nhận reward
        if (player.isBoss) {
            return list;
        }

        // ------------------ ADD EVENT ITEM ------------------
        list.addAll(getItemEventMobReward(player, x, yEnd));

        double tyLeMayMan = player.nPoint.tlMayman;
        int mapid = player.zone.map.mapId;

        // ------------------ DROP ĐIỂM NGŨ HÀNH SƠN ------------------
        if (MapService.gI().isMapNguHanhSon(mapid)) {
            if (Util.getChanceFromLuck(tyLeMayMan, 1, 5, player.itemTime.isUseCoBonLa)) {
                player.event.addEventPointNHS(1);
                Service.gI().sendThongBao(player, "Bạn nhận được 1 điểm Ngũ Hành Sơn");
            }
        }
        // Da go: khoi tha vang nay bi SAO CHEP HAI LAN trong cung mot ham.
        // Hai lan tung 20% doc lap -> thuc te 36% roi vang, va 4% roi HAI dong.
        // Khoi that giu o duoi, cuoi ham.

        // ------------------ DROP BẢN ĐỒ KHO BÁU ------------------
        if (MapService.gI().isMapBanDoKhoBau(mapid)) {
            DropVangForBanDoKhoBau(player, yEnd);
        }

        // ------------------ DROP KÍCH HOẠT THƯỜNG ------------------
        // Do set kich hoat (vai tho). Truoc day dong nay bi tat han bang chu

        // ------------------ ĐỒ RƠI: MỘT NGUỒN DUY NHẤT ------------------
        //
        // Mọi vật phẩm rơi từ quái thường đều lấy từ bảng quai_do_roi — tức
        // thẻ "Đồ rơi từ quái" trên panel. Không còn dòng nào thả đồ bằng mã.
        //
        // Trước đây ba khối gõ cứng (Thiên Tử, set Huỷ Diệt, Potara) chạy
        // SONG SONG với bảng, mà Thiên Tử và set Huỷ Diệt thì bảng đã có dòng
        // rồi — hai món đó rơi gấp đôi tần suất người khai trong panel định,
        // và sửa tỉ lệ trên panel cũng không hết vì nửa kia nằm trong mã.
        //
        // Còn lại trong mã hai thứ KHÔNG phải "một món rơi theo tỉ lệ":
        //  - Đồ rơi sự kiện: có thẻ cấu hình riêng, không thuộc thẻ này.
        //
        // Bình nước cũng đã chuyển sang bảng. Hai thứ mất theo nó: hạn mức
        // mỗi ngày (canDropBinhNuoc) và việc đệ tử đánh thì bình về tay sư
        // phụ — bảng không có cột nào tả được. Muốn tắt hẳn bình nước thì bỏ
        // tick hai dòng 456 trong thẻ, khỏi sửa mã.
        //
        // Điểm Ngũ Hành Sơn và bình hút năng lượng không thả vật phẩm nào —
        // một cái cộng điểm, một cái sửa chỉ số của món đang có trong túi.
        // Thay cho hơn trăm dòng gõ cứng cũ: đá nâng cấp, mảnh đá vụn, thỏi
        // vàng, ngọc rồng, đồ rác... nay khai ở bảng quai_do_roi, mỗi món một
        // tỉ lệ riêng và lọc được theo bản đồ.
        //
        // Vẫn giữ trong mã những thứ KHÔNG phải "một món rơi theo tỉ lệ" mà là
        // cơ chế có điều kiện: điểm Ngũ Hành Sơn, đồ set kích hoạt, capsule
        // theo máy dò, thức ăn khi mặc đủ set Thần Linh, bình hút năng lượng.
        // Ép chúng vào bảng thì mất điều kiện, mà bảng cũng không tả nổi.
        list.addAll(roiTheoBangCauHinh(player, x, yEnd, mapid));

        // ------------------ DROP VÀNG THƯỜNG ------------------
        if (!MapService.gI().isMapBanDoKhoBau(mapid) && !MapService.gI().isMapLang(mapid)
                && Util.isTrue(2, 10)) {
            dropGold(player, this, list, this.zone, x, yEnd);
        }
        //-------------------MAP HIRUDEGARN----------------------------
        if (MapService.gI().isMapHirudegarn(mapid)) {
//            if (Util.isTrue(5, 50)) {
//                if (Util.isTrue(1, 2)) {
//                    ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, x + Util.nextInt(-35, 35), yEnd, player.id);
//                    if (it != null) {
//                        Service.gI().dropItemMap(zone, it);
//                    }
//                }
//            }
//            if (Util.isTrue(1, 2)) {
//                ItemMap it = new ItemMap(this.zone, 568, 1, x + Util.nextInt(-10, 10), yEnd, player.id);
//                Service.gI().dropItemMap(this.zone, it);
//            }
//            if (Util.isTrue(1, 2) && player.mabuEgg != null) {
//                ItemMap it = new ItemMap(this.zone, 1911, 1, x + Util.nextInt(-15, 15), yEnd, player.id);
//                it.addOptionParam(262, 0);
//                it.addOptionParam(30, 0);
//                Service.gI().dropItemMap(this.zone, it);
//            }
        }
        if (MapService.gI().isMapBinhHutNangLuong(mapid) && InventoryService.gI().findItemBinhHutNangLuong(player)) {
            for (Item item : player.inventory.itemsBag) {
                if (!item.isNotNullItem() || item.template.id != 1911) {
                    continue;
                }
                int param = item.getOptionParam(262);
                if (param >= 3000) {
                    continue;
                }
                int chance;
                if (param < 1000) {
                    chance = 3;
                } else if (param < 2000) {
                    chance = 6;
                } else if (param < 2900) {
                    chance = 9;
                } else {
                    chance = 15;
                }
                if (Util.isTrue(1, chance)) {
                    item.addOptionParam(262, 1);
                    if (item.getOptionParam(262) > 3000) {
                        item.getOptionParam(262, 3000);
                    }
                    InventoryService.gI().sendItemBag(player);
                }
            }
        }

        return list;
    }

    /**
     * Thả các món khai ở bảng {@code quai_do_roi}.
     *
     * <p>Tỉ lệ trên panel là <b>phần trăm</b> và nhận số thập phân, nên phải
     * quy về phần triệu rồi mới gieo: {@code Util.isTrue(1, 200)} chỉ nhận số
     * nguyên, mà {@code 0.005%} làm tròn thành 0 là món đó không bao giờ rơi.</p>
     *
     * <p>Hệ số "cơ hội rơi vật phẩm" trên panel vẫn nhân vào đây, để một nút
     * vẫn chỉnh được độ hào phóng chung mà không phải sửa từng dòng.</p>
     */
    private List<ItemMap> roiTheoBangCauHinh(Player player, int x, int yEnd, int mapid) {
        List<ItemMap> ra = new ArrayList<>();
        List<nro.repository.dao.QuaiDoRoiDAO.Dong> ds
                = nro.repository.dao.QuaiDoRoiDAO.dangBat();
        if (ds.isEmpty() || this.zone == null) {
            return ra;
        }
        int hanhTinh = -1;
        String tenMap = "";
        nro.entity.template.MapTemplate mt = mauMap(mapid);
        if (mt != null) {
            hanhTinh = mt.planetId;
            tenMap = mt.name;
        }

        for (nro.repository.dao.QuaiDoRoiDAO.Dong d : ds) {
            // Lọc bản đồ TRƯỚC khi gieo tỉ lệ — gieo trước rồi mới loại thì món
            // đó ăn mất lượt gieo ở nơi nó không được phép rơi.
            if (!d.roiOMap(mapid, hanhTinh, tenMap)) {
                continue;
            }
            if (!thoaDieuKien(player, d.dieuKien)) {
                continue;
            }
            long tuSo = Math.round(d.tiLe * 10_000);
            if (tuSo <= 0) {
                continue;
            }
            if (!roiTheoTiLe((int) Math.min(Integer.MAX_VALUE, tuSo), 1_000_000)) {
                continue;
            }
            int sl = d.soLuongMax > d.soLuongMin
                    ? Util.nextInt(d.soLuongMin, d.soLuongMax) : d.soLuongMin;
            sl = Math.max(1, sl);

            // Dong "do set kich hoat": mon roi ra KHONG phai item_id. May chu
            // tu chon do vai tho dung he nguoi choi, lay chi so mac dinh cua
            // chinh mon do, roi gan set va sao theo dong nay.
            //
            // Roi TUNG mon mot chu khong gop thanh mot chong so luong: moi mon
            // set kich hoat mang chi so rieng, gop lai la ca chong dung chung
            // mot bo chi so.
            if (d.skh) {
                for (int i = 0; i < sl; i++) {
                    ItemMap mon = taoDoSetKichHoat(player,
                            x + Util.nextInt(-10, 10), yEnd, d);
                    if (mon != null) {
                        apHanSuDungQuai(mon, d.hsdMin, d.hsdMax, d.hsdVinhVien);
                        ra.add(mon);
                    }
                }
                continue;
            }

            ItemMap im = new ItemMap(this.zone, (short) d.itemId, sl,
                    x + Util.nextInt(-10, 10), yEnd, player.id);
            apChiSoNgauNhienQuai(im, d.chiSo);
            apHanSuDungQuai(im, d.hsdMin, d.hsdMax, d.hsdVinhVien);
            ra.add(im);
        }
        return ra;
    }

    /**
     * Mẫu bản đồ theo id.
     *
     * <p>{@code MAP_TEMPLATES} đánh chỉ số theo vị trí chứ không theo id nên
     * phải dò. Chỉ chạy khi một con quái chết và danh sách hơn hai trăm dòng,
     * nên dò tuyến tính là đủ.</p>
     */
    private static nro.entity.template.MapTemplate mauMap(int mapId) {
        nro.entity.template.MapTemplate[] ds = nro.server.Manager.MAP_TEMPLATES;
        if (ds == null) {
            return null;
        }
        for (nro.entity.template.MapTemplate m : ds) {
            if (m != null && m.id == mapId) {
                return m;
            }
        }
        return null;
    }

    /** Gắn chỉ số ngẫu nhiên dạng {@code id:min:max,...} cho món vừa thả. */
    private void apChiSoNgauNhienQuai(ItemMap im, String cauHinh) {
        if (im == null || cauHinh == null) {
            return;
        }
        String s = cauHinh.trim();
        if (s.isEmpty() || "[]".equals(s)) {
            return;
        }
        for (String phan : s.split(",")) {
            String[] p = phan.trim().split(":");
            if (p.length != 3) {
                continue;
            }
            try {
                int id = Integer.parseInt(p[0].trim());
                int min = Integer.parseInt(p[1].trim());
                int max = Integer.parseInt(p[2].trim());
                im.addOptionParam(id, max > min ? Util.nextInt(min, max) : min);
            } catch (NumberFormatException boQua) {
                // Dong go sai thi bo qua rieng dong do, khong lam hong ca mon.
            }
        }
    }

    /**
     * Gắn hạn sử dụng cho món vừa thả.
     *
     * <p>Để {@code min} hoặc {@code max} bằng 0 là tắt hẳn — món rơi ra vĩnh
     * viễn. Khi đã bật, mỗi món còn tung thêm một lần theo {@code ptVinhVien}
     * phần trăm để được miễn hạn.</p>
     */
    private void apHanSuDungQuai(ItemMap im, int min, int max, int ptVinhVien) {
        if (im == null || min <= 0 || max <= 0) {
            return;
        }
        if (ptVinhVien > 0 && Util.isTrue(ptVinhVien, 100)) {
            return;
        }
        int ngay = max > min ? Util.nextInt(min, max) : min;
        im.addOptionParam(93, ngay);
    }

    private static final int GOLD_MAX = 100_000_000;

    private void DropVangForBanDoKhoBau(Player pl, int LocationY) {
        Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.VANG);
        if (pl.clan == null || pl.clan.BanDoKhoBau.level <= 0) {
            return;
        }
        long baseGold = this.point.maxHp / 40;
        List<Long> goldChunks = new ArrayList<>();
        for (int j = 0; j < 8; j++) {
            double randFactor = 0.9 + (Math.random() * 0.2);
            long goldAmount = (long) (baseGold * randFactor);
            double playerBonus = (100.0 + pl.nPoint.tlGold) / 100.0;
            long totalGold = (long) (goldAmount * playerBonus);
            if (at != null && !at.isExpired()) {
                totalGold = (long) (totalGold * at.getValue() / 100.0);
            }
            if (totalGold <= 0) {
                totalGold = 1;
            }
            while (totalGold > 0) {
                long chunk = Math.min(GOLD_MAX, totalGold);
                goldChunks.add(chunk);
                totalGold -= chunk;
            }
        }
        int[] baseOffsets = new int[8];
        for (int j = 0; j < 8 && j < goldChunks.size(); j++) {
            baseOffsets[j] = (j - 5) * 15;
            int goldDrop = (int) Math.min(GOLD_MAX, goldChunks.get(j));
            dropGoldItem(baseOffsets[j], LocationY, goldDrop);
        }
        int extraCount = goldChunks.size() - 8;
        if (extraCount > 0) {
            int leftCount = extraCount / 2;
            int rightCount = extraCount - leftCount;
            int leftStart = baseOffsets[0] - 15;
            for (int j = 0; j < leftCount; j++) {
                int idx = 8 + j;
                int goldDrop = (int) Math.min(GOLD_MAX, goldChunks.get(idx));
                dropGoldItem(leftStart - (j * 15), LocationY, goldDrop);
            }
            int rightStart = baseOffsets[7] + 15;
            for (int j = 0; j < rightCount; j++) {
                int idx = 8 + leftCount + j;
                int goldDrop = (int) Math.min(GOLD_MAX, goldChunks.get(idx));
                dropGoldItem(rightStart + (j * 15), LocationY, goldDrop);
            }
        }
    }

    private void dropGoldItem(int offsetX, int offsetY, int goldAmount) {
        ItemMap item = new ItemMap(this.zone, 190, goldAmount, this.location.x + offsetX, offsetY, -1);
        Service.gI().dropItemMap(this.zone, item);
    }

    private void dropItemKichHoat(Player player, Zone zone, int mapid, int x, int yEnd, List<ItemMap> list) {
        // Cửa sổ ngày nhận set kích hoạt — đọc từ panel, 0 là không giới hạn.
        // Trước đây cửa sổ này chỉ tồn tại ở cờ isNewMember gửi cho client
        // (client dùng để vẽ giao diện) chứ KHÔNG chặn việc rơi, nên đồ vẫn rơi
        // cho tài khoản cũ. Nay chặn ở đúng chỗ rơi, để tab Set kích hoạt trên
        // panel là nơi quyết định duy nhất.
        if (nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.SKH_SO_NGAY) > 0 && !player.isNewMember) {
            return;
        }
        // He so nhan vao ti le roi. Ve kich hoat va co bon la giu nguyen muc cu
        // (x3 va x8), rieng chi so may man doi sang cung mot thang voi moi cho
        // tinh roi khac: may man 30 la nhan 1,3 lan.
        //
        // Truoc day cho nay cong may man vao mot bien goc 10 roi chia 10, tuc
        // may man 30 thanh nhan 4 lan — gap hon 10 lan y nghia cua chinh con so
        // do o cac cho khac. Cung mot chi so tren bang khong the co hai nghia.
        double heSo = 1d;
        if (player.isActive()) {
            heSo += 2d;
        }
        if (player.itemTime != null && player.itemTime.isUseCoBonLa) {
            heSo += 7d;
        }
        if (player.nPoint != null) {
            heSo *= Util.heSoMayMan(player.nPoint.tlMayman);
        }
        if (heSo > 50d) {
            heSo = 50d;
        }
        // Map nao duoc roi: danh sach tren panel neu co, khong thi giu luat cu.
        boolean choRoi;
        if (nro.repository.dao.ConfigDAO.danhSachMapTrong(
                nro.repository.dao.ConfigDAO.SKH_MAP)) {
            choRoi = MapService.gI().isMapUpSKH(mapid)
                    || MapService.gI().isMapRiengTu(mapid);
        } else {
            choRoi = nro.repository.dao.ConfigDAO.mapNamTrong(
                    nro.repository.dao.ConfigDAO.SKH_MAP, mapid);
        }
        if (!choRoi) {
            return;
        }
        // Ti le nhap bang PHAN TRAM. Quy ve tu so tren mau 100.000 de giu duoc
        // ba chu so thap phan. He so o tren la 1 khi khong co gi cong them, nen
        // ti le quan tri go tren panel dung la ti le o muc tran.
        int tiLe = (int) Math.round(nro.repository.dao.ConfigDAO.phanTram(
                nro.repository.dao.ConfigDAO.SKH_TILE) * 1000 * heSo);
        // Trung thi roi DUNG MOT mon, du chi so.
        if (tiLe > 0 && Util.isTrue(tiLe, 100_000)) {
            list.add(createItemKichHoat(zone, player, x, yEnd, true));
        }
    }

    /**
     * Gắn sao pha lê (chỉ số 107) vào món đồ set kích hoạt vừa rơi.
     *
     * <h3>Vì sao tách tỉ lệ "có sao" khỏi "mấy sao"</h3>
     *
     * <p>Bản cũ viết cứng một chuỗi {@code else if}: 10% ra 1 sao, ngược lại 8%
     * ra 2 sao, ngược lại 5% ra 3 sao… Vì là {@code else if} nên xác suất bị
     * nhân dồn — 2 sao thật ra chỉ {@code 0,9 × 0,08 = 7,2%}, 4 sao còn
     * {@code 0,78%}. Con số ghi trong code không phải tỉ lệ thật, mà cũng không
     * sửa được lúc chạy.</p>
     *
     * <p>Giờ hai bậc rạch ròi: {@code skh_sao_tile} là tỉ lệ món đồ <b>có</b>
     * sao, đọc thẳng đúng nghĩa; có rồi thì số sao bốc <b>đều</b> trong khoảng
     * min–max.</p>
     *
     * <p>Không gắn nếu món đã có sẵn chỉ số 107 — hai dòng sao trên một món là
     * client hiện hai dòng, người chơi tưởng lỗi.</p>
     */
    static void themSaoPhaLe(List<ItemOption> ops) {
        if (nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.SKH_SAO_BAT) != 1) {
            return;
        }
        for (ItemOption o : ops) {
            if (o.optionTemplate.id == 107) {
                return;
            }
        }
        // Ti le phan tram, nhan so thap phan -> quy ve tu so tren 100.000.
        int tiLe = (int) Math.round(nro.repository.dao.ConfigDAO.phanTram(
                nro.repository.dao.ConfigDAO.SKH_SAO_TILE) * 1000);
        if (tiLe <= 0 || !Util.isTrue(tiLe, 100_000)) {
            return;
        }
        int min = (int) nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.SKH_SAO_MIN);
        int max = (int) nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.SKH_SAO_MAX);
        // Go nguoc min/max thi doi lai thay vi khong gan gi — go nguoc la loi
        // cua nguoi dung, im lang khong gan la ho ngoi doi mai khong hieu.
        if (min > max) {
            int t = min;
            min = max;
            max = t;
        }
        if (max <= 0) {
            return;
        }
        if (min < 1) {
            min = 1;
        }
        ops.add(new ItemOption(107, Util.nextInt(min, max)));
    }


    /**
     * Dựng một món đồ set kích hoạt cho dòng rơi đang xét.
     *
     * <h3>Vì sao không dùng lại {@code createItemKichHoat}</h3>
     *
     * <p>Hàm cũ lấy set <b>ngẫu nhiên</b> và lấy số sao từ các quy ước chung
     * {@code skh_sao_*}. Nay mỗi dòng trên panel tự quyết định set nào và bao
     * nhiêu sao, nên phải nhận tham số từ dòng đó.</p>
     *
     * <p>Chỉ số của món là <b>chỉ số mặc định của đồ vải thô</b>
     * ({@code getListOptionItemShop}) — không bốc thêm gì, đúng như khai trên
     * panel.</p>
     *
     * @return món đã dựng, hoặc {@code null} nếu chưa có set nào dùng được
     */
    private ItemMap taoDoSetKichHoat(Player player, int x, int yEnd,
            nro.repository.dao.QuaiDoRoiDAO.Dong d) {
        short itTemp = (short) ItemService.gI()
                .randTempItemKichHoatVaiTho(player.gender);
        if (itTemp < 0) {
            return null;
        }
        ItemMap it = new ItemMap(this.zone, itTemp, 1, x, yEnd, player.id);
        List<ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }

        // Set do admin chi dinh phai DUNG HANH TINH cua nguoi choi. Set Xayda
        // roi cho nguoi Namec thi mac vao khong kich hoat duoc gi — mon do coi
        // nhu rac. Lech he thi khong roi, chu khong doi sang set khac: dong nay
        // admin da chi dinh dich danh, tu doi la khong con lam theo y ho nua.
        if (d.skhSet != null && !d.skhSet.trim().isEmpty()
                && !hopHeSet(d.skhSet, player.gender)) {
            return null;
        }
        // Set do admin chi dinh; de trong thi boc trong cac set hop he.
        int[] opSet = nro.repository.dao.SetBonusDAO.optionCuaSet(d.skhSet);
        if (opSet == null) {
            ItemService.gI().themOptionSetKichHoat(it.options, player.gender,
                    Util.isTrue(25, 100));
        } else {
            for (int id : opSet) {
                it.options.add(new ItemOption(id, 0));
            }
            // Chi so 30 la khoa mon do, giong duong boc ngau nhien.
            it.options.add(new ItemOption(30, 0));
        }

        ganSaoPhaLe(it.options, d.saoMin, d.saoMax);
        return it;
    }

    /**
     * Set này có đúng hành tinh của người chơi không.
     *
     * <p>Set để hành tinh "Khác" thì coi là dùng chung cho mọi hệ — đó là ý
     * nghĩa của mục đó trên tab Set kích hoạt.</p>
     */
    private static boolean hopHeSet(String setKey, int gender) {
        String he = nro.repository.dao.SetBonusDAO.hanhTinh(setKey);
        if (he == null || nro.repository.dao.SetBonusDAO.KHAC.equals(he)) {
            return true;
        }
        String[] ds = nro.repository.dao.SetBonusDAO.CAC_HANH_TINH;
        return gender >= 0 && gender < ds.length && ds[gender].equals(he);
    }

    /**
     * Gắn sao pha lê theo khoảng khai ở dòng rơi.
     *
     * <p>Để {@code max} bằng 0 là không gắn sao — dòng đó chỉ rơi đồ set trơn.
     * Không đụng tới nếu món đã sẵn có chỉ số 107: hai dòng sao trên một món
     * làm client hiện hai dòng, người chơi tưởng lỗi.</p>
     */
    private void ganSaoPhaLe(List<ItemOption> ops, int min, int max) {
        if (ops == null || max <= 0) {
            return;
        }
        for (ItemOption o : ops) {
            if (o.optionTemplate != null && o.optionTemplate.id == 107) {
                return;
            }
        }
        int lo = Math.max(1, min);
        int hi = Math.max(lo, max);
        ops.add(new ItemOption(107, hi > lo ? Util.nextInt(lo, hi) : lo));
    }
    private ItemMap createItemKichHoat(Zone zone, Player player, int x, int yEnd, boolean fullOption) {
        // Luon la do vai tho — bo cap thap nhat, va cua DUNG HE nguoi choi
        // (randTempItemKichHoatVaiTho loc theo player.gender).
        short itTemp = (short) ItemService.gI()
                .randTempItemKichHoatVaiTho(player.gender);
        ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
        List<ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }
        if (fullOption) {
            // Mot cho duy nhat quyet dinh mon nay thuoc set nao.
            ItemService.gI().themOptionSetKichHoat(it.options, player.gender, Util.isTrue(25, 100));
        }
        themSaoPhaLe(it.options);
        //Trai Dat
        randomizeOption(it, 47, 7, 9, 3);
        randomizeOption(it, 6, 28, 32, 6);
        randomizeOption(it, 6, 147, 153, 35);
        randomizeOption(it, 0, 13, 15, 37);
        randomizeOption(it, 7, 23, 27, 30);
        randomizeOption(it, 7, 117, 123, 39);
        //Namec
        randomizeOption(it, 47, 7, 9, 4);
        randomizeOption(it, 6, 23, 27, 43);
        randomizeOption(it, 6, 117, 123, 10);
        randomizeOption(it, 0, 11, 13, 25);
        randomizeOption(it, 7, 28, 32, 47);
        randomizeOption(it, 7, 147, 153, 31);
        //Xayda
        randomizeOption(it, 47, 9, 11, 5);
        randomizeOption(it, 6, 19, 21, 51);
        randomizeOption(it, 6, 97, 103, 11);
        randomizeOption(it, 0, 15, 17, 26);
        randomizeOption(it, 7, 19, 21, 55);
        randomizeOption(it, 7, 97, 103, 32);
        return it;
    }

    private void randomizeOption(ItemMap it, int optionId, int min, int max, int iditem) {
        if (it.itemTemplate.id == iditem) {
            it.getOptionParam(optionId, Util.nextInt(min, max));
        }
    }

    private int powerDropActiveSet(Player player) {
        if (player != null && player.nPoint != null) {
            long Power = player.nPoint.power;
            if (Power < 2000) {
                return 5_000;
            } else if (Power >= 2000 && Power < 15_000) {
                return 4_700;
            } else if (Power >= 15_000 && Power < 150_000) {
                return 4_200;
            } else if (Power >= 150_000 && Power < 500_000) {
                return 3_800;
            } else if (Power >= 500_000 && Power < 1_500_000) {
                return 3_500;
            } else if (Power >= 1_500_000 && Power < 5_000_000) {
                return 3_200;
            } else if (Power >= 5_000_000 && Power < 15_000_000) {
                return 2_900;
            } else if (Power >= 15_000_000 && Power < 50_000_000) {
                return 2_500;
            } else if (Power >= 50_000_000 && Power < 100_000_000) {
                return 2_000;
            } else if (Power >= 100_000_000 && Power < 200_000_000) {
                return 1_500;
            } else if (Power >= 200_000_000 && Power < 500_000_000) {
                return 1_000;
            } else if (Power >= 500_000_000 && Power < 1_000_000_000) {
                return 500;
            } else if (Power >= 1_000_000_000 && Power < 2_000_000_000) {
                return 100;
            } else if (Power >= 2_000_000_000 && Power < 5_000_000_000L) {
                return 0;
            } else if (Power >= 5_000_000_000L && Power < 10_000_000_000L) {
                return -500;
            } else if (Power >= 10_000_000_000L && Power < 20_000_000_000L) {
                return -1_000;
            } else if (Power >= 20_000_000_000L && Power < 40_000_000_000L) {
                return -2_000;
            } else if (Power >= 40_000_000_000L && Power < 80_000_000_000L) {
                return -5_000;
            } else if (Power >= 80_000_000_000L) {
                return -10_000;
            }
        }
        return 0;
    }
//------------------------------------------------------------------------------

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 5)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
            case ConstMob.PHI_LONG_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 5)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con phi long mẹ này không giữ ngọc, hãy tìm con phi long mẹ khác");
                    }
                }
            case ConstMob.QUY_BAY_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 5)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con quỷ bay mẹ này không giữ ngọc, hãy tìm con quỷ bay mẹ khác");
                    }
                }
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 4)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con ốc mượn hồn này không giữ truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    }
                }
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 4)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con heo xayda mẹ này không giữ truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    }
                }
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 4)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con ốc xên này không giữ truyện tranh, hãy thử tìm con ốc xên khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(double dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeCris(Util.CrisGH(this.point.gethp()), Manager.readInt);
            msg.writeCris(Util.CrisGH(dameHit), Manager.readInt);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writeCris(Util.CrisGH(this.point.hp), Manager.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writeCris(Util.CrisGH(this.point.hp), Manager.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void sendSieuQuai(int type) {
        Message msg;
        try {
            msg = new Message(-75);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(type);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void sendDisable(boolean bool) {
        Message msg;
        try {
            msg = new Message(81);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void sendDoneMove(boolean bool) {
        Message msg;
        try {
            msg = new Message(82);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void sendFire(boolean bool) {
        Message msg;
        try {
            msg = new Message(85);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void sendIce(boolean bool) {
        Message msg;
        try {
            msg = new Message(86);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void sendWind(boolean bool) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    private void phanSatThuong(Player plTarget, double dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            double damePST = Util.CrisGH(dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                long hpMob = Util.CrisGH(this.point.hp);
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writeCris(Util.CrisGH(this.point.hp), Manager.readInt);
                msg.writeCris(Util.CrisGH(damePST), Manager.readInt);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Mob.class, e);
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
    }

    public void sendMobDieAfterMobMeAttackedPet(Player plKill, double dameHit) {
        this.status = 0;
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writeCris(Util.CrisGH(dameHit), Manager.readInt);
            msg.writer().writeBoolean(false); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.getInstance().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
            Logger.logException(Mob.class, e);
        }
        this.lastTimeDie = System.currentTimeMillis();
    }

    /**
     * Người chơi có thoả điều kiện rơi của món này không.
     *
     * <p>Mã lạ — khai trong panel mà mã nguồn không biết — trả về {@code false}
     * chứ không phải {@code true}. Cho qua thì món đó rơi tràn lan mà không ai
     * hiểu vì sao; chặn lại thì im lặng nhưng đúng, và panel đã cảnh báo ngay
     * lúc chọn.</p>
     */
    private boolean thoaDieuKien(Player player, String dieuKien) {
        String dk = dieuKien == null ? "" : dieuKien.trim();
        if (dk.isEmpty()) {
            return true;
        }
        switch (dk) {
            case nro.repository.dao.QuaiDoRoiDAO.DieuKien.SET_THAN_LINH:
                return demDoTrenNguoi(player, true) == 5;
            case nro.repository.dao.QuaiDoRoiDAO.DieuKien.SET_HUY_DIET:
                return demDoTrenNguoi(player, false) == 5;
            case nro.repository.dao.QuaiDoRoiDAO.DieuKien.MAY_DO:
                return player.itemTime != null && player.itemTime.isUseMayDo;
            case nro.repository.dao.QuaiDoRoiDAO.DieuKien.MAY_DO_SIEU_HOA:
                return player.itemTime != null && player.itemTime.isUseMayDoSieuHoa;
            case nro.repository.dao.QuaiDoRoiDAO.DieuKien.DANH_HIEU_THIEN_TU:
                return player.LastTimeDanhHieu_ThienTu > 0
                        && player.isUseDanhHieu_ThienTu;
            default:
                return false;
        }
    }

    /** Đếm số món Thần Linh (hoặc Huỷ Diệt) người chơi đang mặc. */
    private int demDoTrenNguoi(Player player, boolean thanLinh) {
        if (player.inventory == null || player.inventory.itemsBody == null) {
            return 0;
        }
        int dem = 0;
        for (Item item : player.inventory.itemsBody) {
            if (item == null || !item.isNotNullItem()) {
                continue;
            }
            if (thanLinh ? item.isDTL() : item.isDHD()) {
                dem++;
            }
        }
        return dem;
    }
}
