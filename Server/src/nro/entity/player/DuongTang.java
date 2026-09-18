package nro.entity.player;

import nro.core.util.Functions;
import nro.entity.item.Item;
import nro.service.inventory.InventoryService;
import nro.service.FriendAndEnemyService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.fun.ChangeMapService;
import nro.service.MapService;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.service.effect.EffectSkillService;
import nro.service.map.blackballwar.BlackBallWarService;
import nro.entity.map.dragonballnamec.NgocRongNamec;
import nro.service.map.dragonnamecwar.TranhNgocService;
import nro.entity.matches.TYPE_LOSE_PVP;
import nro.entity.skill.Skill;

public class DuongTang extends Player{
    
    public Player master;
    public short body;
    public short leg;
    public static int idb = -8071979;
    boolean isRun = true;

    public DuongTang(Player master) {
        this.master = master;
        this.isDuongTang = true;
        this.id = idb;
        idb--;
        int[] MapHoTong = {5, 6, 10, 13, 19, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 68, 69, 70, 71, 72};
        this.MapHoTong = MapHoTong[Util.nextInt(MapHoTong.length)];
        master.HoTongDuongTang = true;
        master.lastTimeDuongTang = System.currentTimeMillis();
    }

    @Override
    public short getHead() {
        return 467;
    }

    @Override
    public short getBody() {
        return 468;
    }

    @Override
    public short getLeg() {
        return 469;
    }
    
    public void joinMapMaster() {
        if (master == null || master.zone == null) {
            return;
        }
        this.location.x = this.master.location.x + Util.nextInt(-30, -10);
        this.location.y = this.master.location.y;
        if (isPl()) {
            this.dispose();
            return;
        }
        ChangeMapService.gI().goToMap(this, master.zone);
        this.zone.load_Me_To_Another(this);
        // Bao co ngay khi vua vao: luc nhan nhiem vu ong ay chua o dau ca nen
        // lan doi co do khong toi duoc ai.
        nro.service.hotong.HoTongService.guiLaiCo(this);
        // Duong Tang khong noi gi ca: moi cau chat cua ong ay la mot bong bong
        // che mat ban do va che luon chinh ong ay, ma nguoi choi thi chi can
        // biet phai bam theo.
    }

    private long lastTimeMoveIdle;
    private int timeMoveIdle;
    public boolean idle;

    private void moveIdle() {
        if (idle && Util.canDoWithTime(lastTimeMoveIdle, timeMoveIdle)) {
            int dir = this.location.x - master.location.x <= 0 ? -1 : 1;
            PlayerService.gI().playerMove(this, master.location.x
                    + Util.nextInt(dir == -1 ? 30 : -50, dir == -1 ? 50 : 30), master.location.y);
            lastTimeMoveIdle = System.currentTimeMillis();
            timeMoveIdle = Util.nextInt(5000, 8000);
        }
    }
    
    
    @Override
    public void update() {
        if (this.iDMark == null) {
            return;
        }
        super.update();
        if (this.isDie()) {
            if (this.master != null) {
                // Di qua HoTongService: chinh no lo tat co, don dep va bao
                // mot cau duy nhat. Tu xu ly o day thi co den van con bat, va
                // nguoi choi nhan hai cau bao khac nhau cho cung mot viec.
                nro.service.hotong.HoTongService.gI()
                        .thatBai(this.master, "Đường Tăng đã chết");
            }
            return;
        }
        // Chi nhay theo chu khi CHUA o ban do nao — tuc lan dau vua duoc goi ra.
        //
        // Dang ho tong thi TUYET DOI khong bam theo: ong ay di het lo trinh da
        // dinh, tu ban do nay sang ban do khac bang chinh cua di. Nhay theo chu
        // thi nguoi choi chi viec chay thang toi Dao Kame roi doi — ca nhiem vu
        // thanh mot cu dich chuyen, va "cach qua ba ban do" khong con nghia gi.
        //
        // Ngoai luc ho tong, ong ay van theo chu nhu cu.
        if (master != null && master.zone != null && this.zone == null) {
            joinMapMaster();
        } else if (master != null && master.zone != null
                && !master.HoTongDuongTang && this.zone != master.zone) {
            joinMapMaster();
        }
        if (master != null && master.isDie()) {
            return;
        }
        // Dang ho tong thi TU DI theo tuyen; nguoi choi la ben phai bam theo.
        //
        // Ban truoc nguoc lai: Duong Tang bam theo nguoi choi, nen nhiem vu
        // chi la chay toi dich roi quay dau — khong co gi de ho tong ca.
        if (master != null && master.HoTongDuongTang) {
            nro.service.hotong.HoTongService.gI().nhip(this);
            return;
        }
        moveIdle();
    }

    /** Moc lan buoc gan nhat — nhip di do HoTongService quyet dinh. */
    public long lucBuocCuoi;

    
    /**
     * Hai ham <code>followPlayer</code> va <code>followMaster</code> da bo.
     *
     * <p>Chung lo viec Duong Tang bam theo nguoi choi — dung nguoc voi cach
     * nhiem vu chay bay gio. Giu lai lam "cho tuong thich" thi chi to co hai
     * duong di khac nhau cung ton tai, va mot ngay nao do co nguoi goi nham
     * cai cu.</p>
     */
    @Override
    protected void setDie(Player plAtt) {
        if (this.effectSkin.xHPKI > 1) {
            this.effectSkin.xHPKI = 1;
            Service.gI().point(this);
        }
        if (this.effectSkin.xDame > 1) {
            this.effectSkin.xDame = 1;
            Service.gI().point(this);
        }
        this.playerSkill.prepareQCKK = false;
        this.playerSkill.prepareLaze = false;
        this.playerSkill.prepareTuSat = false;
        this.effectSkill.removeSkillEffectWhenDie();
        nPoint.setHp(Util.CrisGH(0));
        nPoint.setMp(Util.CrisGH(0));
        if (this.DeTrung != null) {
            this.DeTrung.mobMeDie();
            this.DeTrung.dispose();
            this.DeTrung = null;
        }
        Service.gI().charDie(this);
        if (!this.isDeTu && !this.isBo && !this.isMe && !this.isPetFollow && !this.isDuongTang && !this.isPhanThan && !this.isNguoiYeu && !this.isConOne && !this.isConTwo && !this.isConThree && !this.isBoss && 
                plAtt != null && !plAtt.isDeTu && !plAtt.isBo && !plAtt.isMe && !plAtt.isPetFollow && !plAtt.isDuongTang && !plAtt.isBoss && !plAtt.isPhanThan && !plAtt.isNguoiYeu && !plAtt.isConOne && !plAtt.isConTwo && !plAtt.isConThree) {
            if (!plAtt.itemTime.isUseAnDanh) {
                FriendAndEnemyService.gI().addEnemy(this, plAtt);
            }
        }
        
        this.typePk = 0;
        if (this.pvp != null && this.zone.map.mapId != 140) {
            this.pvp.lose(this, TYPE_LOSE_PVP.DEAD);
        }
        if (this.PhanThan != null) {
            this.PhanThan.setDie(plAtt);
        }
        BlackBallWarService.gI().dropBlackBall(this);
        NgocRongNamec.gI().dropNamekBall(this);
        if (isHoldNamecBallTranhDoat) {
            TranhNgocService.getInstance().dropBall(this, (byte) -1);
            TranhNgocService.getInstance().sendUpdateLift(this);
        }
    }
    
    /**
     * Mỗi đòn chỉ ăn <b>một điểm</b> máu.
     *
     * <p>Đường Tăng chỉ có một trăm máu, mà một người chơi bình thường đánh một
     * phát đã vài triệu sát thương. Không chặn thì ai đi ngang cũng kết liễu
     * được ngay, và nhiệm vụ không còn gì để chơi.</p>
     *
     * <p>Một trăm máu và mỗi đòn một điểm nghĩa là phá hỏng một chuyến hộ
     * tống phải trả đúng một trăm nhát đánh — đủ lâu để người hộ tống
     * kịp can thiệp.</p>
     */
    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (damage > 1) {
            damage = 1;
        }
        if (!this.isDie()) {
            if (plAtt != null && plAtt.playerSkill.skillSelect != null && !plAtt.isBoss && MapService.gI().isMapMaBu12H(this.zone.map.mapId)) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                    case Skill.DRAGON:
                    case Skill.DEMON:
                    case Skill.GALICK:
                    case Skill.LIEN_HOAN:
                    case Skill.KAIOKEN:
                        damage = Util.CrisGH(damage > this.nPoint.hpMax / 20 ? this.nPoint.hpMax / 20 : damage);
                }
            }
            if (plAtt != null && plAtt.effectSkill != null && plAtt.effectSkill.isBinh
                    && !Util.canDoWithTime(plAtt.effectSkill.lastTimeUpBinh, 3000)) {
                return 0;
            }
            if (plAtt != null && plAtt.isBoss) {
                this.effectSkin.isVoHinh = false;
                this.effectSkin.lastTimeVoHinh = System.currentTimeMillis();
            }
                       
            if (plAtt != null) {
                int tlDameEath = plAtt.nPoint.tlTanCongTocTraiDat;
                int tlDameNamek = plAtt.nPoint.tlTanCongTocNamec;
                int tlDameSaiyan = plAtt.nPoint.tlTanCongTocXayda;
                
                if (tlDameEath > 0 && this.gender == ConstPlayer.TRAI_DAT) {
                    damage += Util.CrisGH((damage / 100) * tlDameEath);
                }
                if (tlDameNamek > 0 && this.gender == ConstPlayer.NAMEC) {
                    damage += Util.CrisGH((damage / 100) * tlDameNamek);
                }
                if (tlDameSaiyan > 0 && this.gender == ConstPlayer.XAYDA) {
                    damage += Util.CrisGH((damage / 100) * tlDameSaiyan);
                }
            }
            if (plAtt != null && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                        if (this.nPoint.voHieuChuong > 0) {
                            nro.service.PlayerService.gI().hoiPhuc(this, 0, Util.CrisGH(damage * this.nPoint.voHieuChuong / 100));
                            return 0;
                        }
                }
            }
            int tlGiap = this.nPoint.tlGiap;
            int tlNeDon = this.nPoint.tlNeDon;
            if (plAtt != null && !isMobAttack && plAtt.playerSkill.skillSelect != null) {
                int tlCongDonSD = plAtt.nPoint.tlCongDonSD;
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.DRAGON:
                    case Skill.DEMON:
                    case Skill.GALICK:
                    case Skill.LIEN_HOAN:
                    case Skill.KAIOKEN:
                        if (tlCongDonSD > 0 && plAtt.tlDameAdd < tlCongDonSD) {
                            plAtt.tlDameAdd++;
                            plAtt.nPoint.dame += (long) (plAtt.nPoint.dame * 0.01); // Chỉ tăng 1% dame, không nhân đôi
                            if (plAtt.tlDameAdd >= tlCongDonSD) {
                                plAtt.tlDameAdd = 0;
                            }
                        }
                        break;
                }
                
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                    case Skill.DRAGON:
                    case Skill.DEMON:
                    case Skill.GALICK:
                    case Skill.LIEN_HOAN:
                    case Skill.KAIOKEN:
                    case Skill.QUA_CAU_KENH_KHI:
                    case Skill.MAKANKOSAPPO:
                    case Skill.DICH_CHUYEN_TUC_THOI:
                        tlNeDon -= plAtt.nPoint.tlchinhxac;
                        break;
                    default:
                        tlNeDon = 0;
                        break;
                }
                                
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC: {
                        if (tlGiap - plAtt.nPoint.tlxgc >= 0) {
                            tlGiap -= plAtt.nPoint.tlxgc;
                        } else {
                            tlGiap = 0;
                        }
                        break;
                    }
                    case Skill.DRAGON:
                    case Skill.DEMON:
                    case Skill.GALICK:
                    case Skill.LIEN_HOAN:
                    case Skill.KAIOKEN: {
                        if (tlGiap - plAtt.nPoint.tlxgcc >= 0) {
                            tlGiap -= plAtt.nPoint.tlxgcc;
                        } else {
                            tlGiap = 0;
                        }
                        break;
                    }
                }
            }
            if (piercing) {
                tlGiap = 0;
            }

            if (tlNeDon > 90) {
                tlNeDon = 90;
            }
            if (tlGiap > 86) {
                tlGiap = 86;
            }
            if (Util.isTrue(tlNeDon, 100)) {
                return 0;
            }
            damage -= Util.CrisGH((damage / 100) * tlGiap);
                        
            if (!piercing) {
                damage -= Util.CrisGH((damage / 100) * tlGiap);
            }
            
            int tlSubDame20Mp = this.nPoint.tlSubDamePercenMp20;
            
            if (tlSubDame20Mp > 0 && this.nPoint.mp < this.nPoint.mpMax * 0.2) {
                damage -= Util.CrisGH((damage / 100) * tlSubDame20Mp);
            }
            
            int tlSubEath = this.nPoint.tlGiamSatThuongTraiDat;
            int tlSubNamek = this.nPoint.tlGiamSatThuongNamec;
            int tlSubSaiyan = this.nPoint.tlGiamSatThuongXayda;
                
            if (tlSubEath > 99) {
                tlSubEath = 99;
            }
            if (tlSubNamek > 99) {
                tlSubNamek = 99;
            }
            if (tlSubSaiyan > 99) {
                tlSubSaiyan = 99;
            }    
            if (tlSubEath > 0 && plAtt != null && plAtt.gender == ConstPlayer.TRAI_DAT) {
                damage -= Util.CrisGH((damage / 100) * tlSubEath);
            }
            if (tlSubNamek > 0 && plAtt != null && plAtt.gender == ConstPlayer.NAMEC) {
                damage -= Util.CrisGH((damage / 100) * tlSubNamek);
            }
            if (tlSubSaiyan > 0 && plAtt != null && plAtt.gender == ConstPlayer.XAYDA) {
                damage -= Util.CrisGH((damage / 100) * tlSubSaiyan);
            }
            
            if (plAtt != null && plAtt.isPl() && this.maBuHold != null && this.zone != null && this.zone.map.mapId == 128) {
                this.precentMabuHold++;
                damage = 1;
            }
            if (plAtt != null && plAtt.idNRNM != -1 && (this.isBoss || this.isPetFollow || this.isDuongTang)) {
                return 1;
            }
            if (plAtt != null && (plAtt.idNRNM != -1 || this.idNRNM != -1) && plAtt.clan != null && this.clan != null && plAtt.clan == this.clan) {
                Service.gI().chatJustForMe(plAtt, this, "Ê cùng bang mà");
                return 0;
            }
            if (plAtt != null && plAtt.effectSkill != null && plAtt.effectSkill.isXinbato && Util.isTrue(20, 100)) {
                return 0;
            }
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (isMobAttack && this.charms.tdBatTu > System.currentTimeMillis() && damage >= this.nPoint.hp) {
                damage = this.nPoint.hp - 1;
            }
            if (!piercing && plAtt == null && isMobAttack && (this.charms.tdBatTu > System.currentTimeMillis() || this.effectSkill != null && this.effectSkill.isHalloween) 
                    && damage >= this.nPoint.hp) {
                damage = this.nPoint.hp - 1;
            }
            boolean isUseGX = false;
            if (!piercing && plAtt != null && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                    case Skill.DRAGON:
                    case Skill.DEMON:
                    case Skill.GALICK:
                    case Skill.LIEN_HOAN:
                    case Skill.KAIOKEN:
                    case Skill.QUA_CAU_KENH_KHI:
                    case Skill.MAKANKOSAPPO:
                    case Skill.DICH_CHUYEN_TUC_THOI:
                        isUseGX = true;
                        break;
                }
            }
            if ((isUseGX || isMobAttack) && this.itemTime != null) {
                if (this.itemTime.isUseGiapXen && !this.itemTime.isUseGiapXen2) {
                    damage /= 2;
                }
                if (this.itemTime.isUseGiapXen2) {
                    damage = damage / 100 * 40;
                }
            }
            
            if (!piercing && effectSkill.isShielding && !isMobAttack) {
                if (this.iDMark != null) {
                    this.iDMark.setDamePST(Util.CrisGH(damage));
                }
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
                if (MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
                    damage = 10;
                }
            }
            if (plAtt != null && this.nPoint.isFounder) {
                Service.gI().sendThongBao(plAtt, "Không thể tấn công! Vì người này là nhà sáng lập game!");
                return 0;
            }
            if (this.zone.map.mapId == 51) {
                this.totalDamageTaken += damage;
            }
            this.nPoint.subHP(Util.CrisGH(damage));                
            if (Util.isTrue(this.nPoint.tlBom, 100)) {
                setBom(plAtt);
            } else {
                setDie(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void dispose() {
        if (zone != null) {
            ChangeMapService.gI().exitMap(master);
        }
        this.master = null;
        if (this.master != null && this.master.Duongtang != null){
            this.master.Duongtang = null;
        }
        super.dispose();
    }
}
