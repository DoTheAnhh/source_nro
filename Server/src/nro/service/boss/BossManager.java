package nro.service.boss;

/*
 * @Author: MaiTienDung
 */

import nro.entity.boss.legacy.gomah.Gomah;
import nro.entity.boss.legacy.tramhuydiet.Berus;
import nro.entity.boss.legacy.chilled.Chilled;
import nro.entity.boss.legacy.cooler.Cooler;
import nro.entity.boss.legacy.frost.Frost;
import nro.entity.boss.legacy.blackgoku.BlackGoku;
import nro.entity.boss.legacy.blackgoku.ZamasKaio;
import nro.entity.boss.legacy.blackgoku.ZamasMax;
import nro.entity.boss.legacy.cumber.Cumber;
import nro.entity.boss.legacy.cumber.Cumber2;
import nro.entity.boss.legacy.cumber.Cumber3;

import nro.entity.boss.Boss;
import nro.entity.boss.function.TestBoss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import nro.entity.boss.TypeEventBoss;

import nro.core.util.Functions;
import nro.core.log.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import nro.net.io.Message;


import nro.entity.boss.list.broly.Broly;
import nro.entity.boss.list.broly.BrolyZone0;
import nro.entity.boss.list.broly.SuperBrolyNew;
import nro.entity.boss.list.earth.Bido;
import nro.entity.boss.list.earth.Bojack;
import nro.entity.boss.list.earth.Bujin;
import nro.entity.boss.list.earth.Kogu;
import nro.entity.boss.list.earth.SuperBojack;
import nro.entity.boss.list.earth.Zangya;
import nro.entity.boss.list.goldenfrieza.DeathBeam1;
import nro.entity.boss.list.goldenfrieza.DeathBeam2;
import nro.entity.boss.list.goldenfrieza.DeathBeam3;
import nro.entity.boss.list.goldenfrieza.DeathBeam4;
import nro.entity.boss.list.goldenfrieza.DeathBeam5;
import nro.entity.boss.list.goldenfrieza.GoldenFrieza;
import nro.entity.boss.list.pilafgang.Mai;
import nro.entity.boss.list.pilafgang.Pilap;
import nro.entity.boss.list.pilafgang.Shu;

import nro.entity.boss.map.bossnomal.AnTrom;
import nro.entity.boss.map.bossnomal.ODo;
import nro.entity.boss.map.bossnomal.RaiTi;
import nro.entity.boss.map.bossnomal.SoiHecQuyn;
import nro.entity.boss.map.bossnomal.Virus;
import nro.entity.boss.map.bossnomal.XinBaTo;
import nro.entity.boss.map.majinbuu12h.BuiBui;
import nro.entity.boss.map.majinbuu12h.BuiBui2;
import nro.entity.boss.map.majinbuu12h.Cadic;
import nro.entity.boss.map.majinbuu12h.Drabura;
import nro.entity.boss.map.majinbuu12h.Drabura2;
import nro.entity.boss.map.majinbuu12h.Drabura3;
import nro.entity.boss.map.majinbuu12h.Goku;
import nro.entity.boss.map.majinbuu12h.Mabu;
import nro.entity.boss.map.majinbuu12h.Yacon;
import nro.entity.boss.map.majinbuu14h.MaBu2H;
import nro.entity.boss.map.majinbuu14h.SuperBu;
import nro.entity.boss.map.taopaipai.TaoPaiPai;
import nro.entity.boss.map.yardart.ChienBinh0;
import nro.entity.boss.map.yardart.ChienBinh1;
import nro.entity.boss.map.yardart.ChienBinh2;
import nro.entity.boss.map.yardart.ChienBinh3;
import nro.entity.boss.map.yardart.ChienBinh4;
import nro.entity.boss.map.yardart.ChienBinh5;
import nro.entity.boss.map.yardart.DoiTruong5;
import nro.entity.boss.map.yardart.TanBinh0;
import nro.entity.boss.map.yardart.TanBinh1;
import nro.entity.boss.map.yardart.TanBinh2;
import nro.entity.boss.map.yardart.TanBinh3;
import nro.entity.boss.map.yardart.TanBinh4;
import nro.entity.boss.map.yardart.TanBinh5;
import nro.entity.boss.map.yardart.TapSu0;
import nro.entity.boss.map.yardart.TapSu1;
import nro.entity.boss.map.yardart.TapSu2;
import nro.entity.boss.map.yardart.TapSu3;
import nro.entity.boss.map.yardart.TapSu4;

import nro.entity.boss.task.frieza.Fide;
import nro.entity.boss.task.futurecell.SieuBoHung;
import nro.entity.boss.task.futurecell.XenCon1;
import nro.entity.boss.task.futurecell.XenCon2;
import nro.entity.boss.task.futurecell.XenCon3;
import nro.entity.boss.task.futurecell.XenCon4;
import nro.entity.boss.task.futurecell.XenCon5;
import nro.entity.boss.task.futurecell.XenCon6;
import nro.entity.boss.task.futurecell.XenCon7;
import nro.entity.boss.task.ginyuforce.So1;
import nro.entity.boss.task.ginyuforce.So2;
import nro.entity.boss.task.ginyuforce.So3;
import nro.entity.boss.task.ginyuforce.So4;
import nro.entity.boss.task.ginyuforce.TieuDoiTruong;
import nro.entity.boss.task.ginyuforcenamek.So1Namek;
import nro.entity.boss.task.ginyuforcenamek.So2Namek;
import nro.entity.boss.task.ginyuforcenamek.So3Namek;
import nro.entity.boss.task.ginyuforcenamek.So4Namek;
import nro.entity.boss.task.ginyuforcenamek.TieuDoiTruongNamek;
import nro.entity.boss.task.napa.Kuku;
import nro.entity.boss.task.napa.MapDauDinh;
import nro.entity.boss.task.napa.Rambo;
import nro.entity.boss.task.presentcell.XenBoHung;
import nro.entity.boss.task.robotassasinone.Android19;
import nro.entity.boss.task.robotassasinone.DrKore;
import nro.entity.boss.task.robotassasinthree.KingKong;
import nro.entity.boss.task.robotassasinthree.Pic;
import nro.entity.boss.task.robotassasinthree.Poc;
import nro.entity.boss.task.robotassasintwo.Android13;
import nro.entity.boss.task.robotassasintwo.Android14;
import nro.entity.boss.task.robotassasintwo.Android15;

import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.server.Maintenance;
import nro.service.MapService;

public class BossManager implements Runnable {

    private static BossManager instance;
    public static byte ratioReward = 10;

    protected final List<Boss> bosses;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public List<Boss> getBosses() {
        return this.bosses;
    }

    /**
     * Dựng boss theo danh sách trong bảng {@code boss_spawn}.
     *
     * <p>Trước đây đây là 33 dòng {@code createBoss(...)} gõ cứng. Nay danh
     * sách nằm ở CSDL, sửa được từ panel: thêm con mới, tắt tạm một con, hay
     * đổi số bản sao mà không phải biên dịch lại.</p>
     *
     * <p>Bảng trống thì gieo đúng danh sách cũ xuống rồi dùng luôn — lần chạy
     * đầu sau khi đổi <b>không khác gì trước</b>.</p>
     *
     * <p>Việc id nào ứng với lớp Java nào vẫn ở {@link #createBoss(int)}. Dòng
     * nào mang id không lớp nào nhận thì bỏ qua và ghi log, chứ không làm hỏng
     * cả lượt dựng.</p>
     */
    public void loadBoss() {
        java.util.List<nro.repository.dao.BossSpawnDAO.Dong> ds
                = nro.repository.dao.BossSpawnDAO.tatCa();
        // Chi gieo khi CHUA TUNG gieo, chu khong phai khi bang trong. Admin xoá
        // sạch danh sách để tự thêm lại là chuyện bình thường; hiểu nhầm "trống"
        // thành "chưa gieo" thì mỗi lần khởi động lại danh sách cũ lại mọc lên.
        if (ds.isEmpty() && nro.repository.dao.BossSpawnDAO.canGieoLanDau()) {
            nro.repository.dao.BossSpawnDAO.gieo(danhSachMacDinh());
            ds = nro.repository.dao.BossSpawnDAO.tatCa();
            Logger.success("Đã gieo " + ds.size()
                    + " dòng danh sách boss xuống bảng boss_spawn\n");
        }
        ds = donDongCu(ds);
        int con = 0;
        int ban = 0;
        for (nro.repository.dao.BossSpawnDAO.Dong d : ds) {
            if (!d.bat || d.soBanSao <= 0) {
                continue;
            }
            // Xet GIA TRI TRA VE cua createBoss chu khong xet danh sach bosses
            // co dai ra hay khong: co nhung con tu ghi minh vao manager khac
            // (Yardart, FinalBoss, Broly...) nen dung duoc ma danh sach o day
            // van y nguyen — do la boss that, khong phai loi.
            int daDung = 0;
            for (int i = 0; i < d.soBanSao; i++) {
                if (this.createBoss(d.bossId) != null) {
                    daDung++;
                }
            }
            if (daDung == 0) {
                Logger.error("boss_spawn: id " + d.bossId + " (" + d.ten
                        + ") không có lớp nào nhận, bỏ qua\n");
                continue;
            }
            con++;
            ban += daDung;
        }
        Logger.success("Đã dựng " + con + " loại boss, tổng " + ban
                + " bản, theo bảng boss_spawn\n");
    }

    /**
     * Dọn những dòng boss <b>không còn lớp nào nhận</b> ra khỏi bảng.
     *
     * <h2>Vì sao cần</h2>
     *
     * <p>Bảng {@code boss_spawn} là dữ liệu, còn lớp boss là mã nguồn. Gỡ một
     * lớp đi thì dòng của nó vẫn nằm lại trong bảng: panel vẫn liệt kê nó, quản
     * trị viên vẫn thấy nó trong "boss trong máy chủ", chỉ là nó không bao giờ
     * xuất hiện được. Dọn ở đây để hai bên khớp nhau.</p>
     *
     * <p>Hiện chỉ dọn bao cát Gôku SSJ: nó đã thành NPC thường ở Đảo Kame
     * (xem {@code GokuSSJKame}), không còn là boss nữa.</p>
     */
    private java.util.List<nro.repository.dao.BossSpawnDAO.Dong> donDongCu(
            java.util.List<nro.repository.dao.BossSpawnDAO.Dong> ds) {
        final int ID_BAO_CAT_CU = -4359;
        boolean con = false;
        for (nro.repository.dao.BossSpawnDAO.Dong d : ds) {
            if (d.bossId == ID_BAO_CAT_CU) {
                con = true;
                break;
            }
        }
        if (con) {
            String loi = nro.repository.dao.BossSpawnDAO.xoa(ID_BAO_CAT_CU);
            if (loi == null) {
                ds = nro.repository.dao.BossSpawnDAO.tatCa();
                Logger.success("Đã bỏ bao cát Gôku SSJ khỏi boss_spawn — nay là NPC thường\n");
            }
        }
        return ds;
    }

    /** Danh sách gõ cứng cũ, chỉ dùng để gieo lần đầu. */
    private java.util.List<nro.repository.dao.BossSpawnDAO.Dong> danhSachMacDinh() {
        java.util.List<nro.repository.dao.BossSpawnDAO.Dong> ds
                = new java.util.ArrayList<>();
        them(ds, BossID.TIEU_DOI_TRUONG, "TIEU_DOI_TRUONG", 1, 0);
        them(ds, BossID.KING_KONG, "KING_KONG", 1, 1);
        them(ds, BossID.XEN_BO_HUNG, "XEN_BO_HUNG", 1, 2);
        them(ds, BossID.SIEU_BO_HUNG, "SIEU_BO_HUNG", 1, 3);
        them(ds, BossID.KUKU, "KUKU", 1, 4);
        them(ds, BossID.MAP_DAU_DINH, "MAP_DAU_DINH", 1, 5);
        them(ds, BossID.RAMBO, "RAMBO", 1, 6);
        them(ds, BossID.FIDE, "FIDE", 1, 7);
        them(ds, BossID.ANDROID_14, "ANDROID_14", 1, 8);
        them(ds, BossID.DR_KORE, "DR_KORE", 1, 9);
        them(ds, BossID.TAU_PAY_PAY_DONG_NAM_KARIN, "TAU_PAY_PAY_DONG_NAM_KARIN", 1, 10);
        them(ds, BossID.BOJACK, "BOJACK", 1, 11);
        them(ds, BossID.SUPER_BOJACK, "SUPER_BOJACK", 1, 12);
        them(ds, BossID.GOLDEN_FRIEZA, "GOLDEN_FRIEZA", 1, 13);
        them(ds, BossID.PI_LAP, "PI_LAP", 1, 14);
        them(ds, BossID.TIEU_DOI_TRUONG_NAMEK, "TIEU_DOI_TRUONG_NAMEK", 1, 15);
        them(ds, BossID.BLACK_GOKU, "BLACK_GOKU", 3, 16);
        them(ds, BossID.COOLER, "COOLER", 1, 17);
        them(ds, BossID.FROST, "FROST", 1, 18);
        them(ds, BossID.CUMBER, "CUMBER", 1, 19);
        them(ds, BossID.CHILER, "CHILER", 1, 20);
        them(ds, BossID.ZAMASZIN, "ZAMASZIN", 1, 21);
        them(ds, BossID.ZAMASMAX, "ZAMASMAX", 1, 22);
        them(ds, BossID.DRABULA2, "DRABULA2", 1, 23);
        them(ds, BossID.BERUS, "BERUS", 1, 24);
        them(ds, BossID.WHIS_TWO, "WHIS_TWO", 1, 25);
        them(ds, BossID.SUPER_BROLY_NEW, "SUPER_BROLY_NEW", 5, 26);
        them(ds, BossID.GOMAH, "GOMAH", 1, 27);
        them(ds, BossID.SOI_HEC_QUYN_NOMAL, "SOI_HEC_QUYN_NOMAL", 100, 28);
        them(ds, BossID.O_DO_NOMAL, "O_DO_NOMAL", 100, 29);
        them(ds, BossID.VIRUS_NOMAL, "VIRUS_NOMAL", 100, 30);
        them(ds, BossID.XIN_BA_TO_NOMAL, "XIN_BA_TO_NOMAL", 100, 31);
        return ds;
    }

    private void them(java.util.List<nro.repository.dao.BossSpawnDAO.Dong> ds,
            int id, String ten, int soBan, int thuTu) {
        nro.repository.dao.BossSpawnDAO.Dong d
                = new nro.repository.dao.BossSpawnDAO.Dong();
        d.bossId = id;
        d.ten = ten;
        d.soBanSao = soBan;
        d.thuTu = thuTu;
        ds.add(d);
    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            switch (bossID) {
                case BossID.TAP_SU_0:
                    return new TapSu0();
                case BossID.TAP_SU_1:
                    return new TapSu1();
                case BossID.TAP_SU_2:
                    return new TapSu2();
                case BossID.TAP_SU_3:
                    return new TapSu3();
                case BossID.TAP_SU_4:
                    return new TapSu4();
                case BossID.TAN_BINH_5:
                    return new TanBinh5();
                case BossID.TAN_BINH_0:
                    return new TanBinh0();
                case BossID.TAN_BINH_1:
                    return new TanBinh1();
                case BossID.TAN_BINH_2:
                    return new TanBinh2();
                case BossID.TAN_BINH_3:
                    return new TanBinh3();
                case BossID.TAN_BINH_4:
                    return new TanBinh4();
                case BossID.CHIEN_BINH_5:
                    return new ChienBinh5();
                case BossID.CHIEN_BINH_0:
                    return new ChienBinh0();
                case BossID.CHIEN_BINH_1:
                    return new ChienBinh1();
                case BossID.CHIEN_BINH_2:
                    return new ChienBinh2();
                case BossID.CHIEN_BINH_3:
                    return new ChienBinh3();
                case BossID.CHIEN_BINH_4:
                    return new ChienBinh4();
                case BossID.DOI_TRUONG_5:
                    return new DoiTruong5();
                case BossID.DRABURA:
                    return new Drabura();
                case BossID.BUI_BUI:
                    return new BuiBui();
                case BossID.BUI_BUI_2:
                    return new BuiBui2();
                case BossID.YA_CON:
                    return new Yacon();
                case BossID.DRABURA_2:
                    return new Drabura2();
                case BossID.GOKU:
                    return new Goku();
                case BossID.CADIC:
                    return new Cadic();
                case BossID.MABU_12H:
                    return new Mabu();
                case BossID.DRABURA_3:
                    return new Drabura3();
                case BossID.MABU:
                    return new MaBu2H();
                case BossID.SUPERBU:
                    return new SuperBu();
                case BossID.SO_4:
                    return new So4();
                case BossID.SO_3:
                    return new So3();
                case BossID.SO_2:
                    return new So2();
                case BossID.SO_1:
                    return new So1();
                case BossID.TIEU_DOI_TRUONG:
                    return new TieuDoiTruong();
                case BossID.KUKU:
                    return new Kuku();
                case BossID.MAP_DAU_DINH:
                    return new MapDauDinh();
                case BossID.RAMBO:
                    return new Rambo();
                case BossID.FIDE:
                    return new Fide();
                case BossID.DR_KORE:
                    return new DrKore();
                case BossID.ANDROID_19:
                    return new Android19();
                case BossID.ANDROID_13:
                    return new Android13();
                case BossID.ANDROID_14:
                    return new Android14();
                case BossID.ANDROID_15:
                    return new Android15();
                case BossID.PIC:
                    return new Pic();
                case BossID.POC:
                    return new Poc();
                case BossID.KING_KONG:
                    return new KingKong();
                case BossID.XEN_BO_HUNG:
                    return new XenBoHung();
                case BossID.SIEU_BO_HUNG:
                    return new SieuBoHung();
                case BossID.XEN_CON_1:
                    return new XenCon1();
                case BossID.XEN_CON_2:
                    return new XenCon2();
                case BossID.XEN_CON_3:
                    return new XenCon3();
                case BossID.XEN_CON_4:
                    return new XenCon4();
                case BossID.XEN_CON_5:
                    return new XenCon5();
                case BossID.XEN_CON_6:
                    return new XenCon6();
                case BossID.XEN_CON_7:
                    return new XenCon7();
                case BossID.GOLDEN_FRIEZA:
                    return new GoldenFrieza();
                case BossID.DEATH_BEAM_1:
                    return new DeathBeam1();
                case BossID.DEATH_BEAM_2:
                    return new DeathBeam2();
                case BossID.DEATH_BEAM_3:
                    return new DeathBeam3();
                case BossID.DEATH_BEAM_4:
                    return new DeathBeam4();
                case BossID.DEATH_BEAM_5:
                    return new DeathBeam5();
                case BossID.BUJIN:
                    return new Bujin();
                case BossID.KOGU:
                    return new Kogu();
                case BossID.ZANGYA:
                    return new Zangya();
                case BossID.BIDO:
                    return new Bido();
                case BossID.BOJACK:
                    return new Bojack();
                case BossID.SUPER_BOJACK:
                    return new SuperBojack();
                case BossID.TAU_PAY_PAY_DONG_NAM_KARIN:
                    return new TaoPaiPai();
                case BossID.SO_4_NAMEK:
                    return new So4Namek();
                case BossID.SO_3_NAMEK:
                    return new So3Namek();
                case BossID.SO_2_NAMEK:
                    return new So2Namek();
                case BossID.SO_1_NAMEK:
                    return new So1Namek();
                case BossID.TIEU_DOI_TRUONG_NAMEK:
                    return new TieuDoiTruongNamek();
                case BossID.PI_LAP:
                    return new Pilap();
                case BossID.MAI:
                    return new Mai();
                case BossID.SHU:
                    return new Shu();
                case BossID.SOI_HEC_QUYN_NOMAL:
                    return new SoiHecQuyn();
                case BossID.O_DO_NOMAL:
                    return new ODo();
                case BossID.AN_TROM_NOMAL:
                    return new AnTrom();
                case BossID.RAI_TI_NOMAL:
                    return new RaiTi();
                case BossID.XIN_BA_TO_NOMAL:
                    return new XinBaTo();
                case BossID.VIRUS_NOMAL:
                    return new Virus();
                case BossID.BLACK_GOKU:
                    return new BlackGoku();
                case BossID.COOLER:
                    return new Cooler();
                case BossID.FROST:
                    return new Frost();
                case BossID.CUMBER:
                    return new Cumber();
                case BossID.CHILER:
                    return new Chilled();
                case BossID.ZAMASMAX:
                    return new ZamasMax();
                case BossID.ZAMASZIN:
                    return new ZamasKaio();
                case BossID.BROLY:
                    return new Broly();
                case BossID.BROLY_ZONE_0:
                    return new BrolyZone0();
                case BossID.DRABULA2:
                    return new Cumber2();
                case BossID.DRABULA3:
                    return new Cumber3();
                case BossID.BERUS:
                    return new Berus();
                case BossID.SUPER_BROLY_NEW:
                    return new SuperBrolyNew();
                case BossID.GOMAH:
                    return new Gomah();
                default:
                    // Khong co lop rieng -> dung boss chung tu boss_data.
                    return bossChungTuCSDL(bossID);
            }
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }


    /**
     * Boss dựng thẳng từ {@code boss_data} khi <b>không có lớp Java riêng</b>.
     *
     * <h3>Vì sao cần</h3>
     *
     * <p>{@code BossID} khai hơn hai trăm hằng số, nhưng {@code switch} ở trên
     * chỉ nhận chưa tới một trăm. Số còn lại rơi vào {@code default} và trả
     * {@code null}, nên panel báo "không có lớp Java nào nhận" và <b>không thêm
     * vào game được</b> — dù {@code boss_data} có đủ số liệu của con đó.</p>
     *
     * <p>Lớp riêng chỉ cần khi boss có hành vi đặc biệt (đi kèm con khác, hồi
     * máu theo chu kỳ, khoá vùng...). Boss thường thì mọi thứ nằm hết trong
     * {@code BossData}: tên, giới tính, trang phục, sức đánh, HP, bản đồ, kỹ
     * năng, thoại, giây hồi sinh. Lớp {@link Boss} gốc chạy đủ những thứ đó,
     * nên dựng thẳng nó là boss hoạt động bình thường.</p>
     *
     * <p>Vẫn trả {@code null} khi không tra ra mẫu — id đó thật sự không có số
     * liệu, thêm vào chỉ tạo một dòng chết trong {@code boss_spawn}.</p>
     */
    private Boss bossChungTuCSDL(int bossID) {
        try {
            String ma = tenHangBossID(bossID);
            if (ma == null) {
                // Khong phai hang so trong BossID -> tra ma trong boss_spawn.
                // Nho vay boss tao tu panel khong can them hang so vao ma
                // nguon, tuc them boss moi khong phai bien dich lai.
                ma = maTrongBossSpawn(bossID);
            }
            if (ma == null) {
                return null;
            }
            nro.entity.boss.BossData mau =
                    nro.repository.dao.BossDataDAO.quetTrongMa().get(ma);
            if (mau == null) {
                // Ma nguon khong khai mau nao -> thu doc thang tu boss_data.
                // Nho vay them boss moi chi can them mot dong trong bang,
                // khong phai sua ma nguon roi bien dich lai.
                mau = nro.repository.dao.BossDataDAO.mauTuCSDL(ma);
            }
            if (mau == null) {
                return null;
            }
            return new Boss(bossID, mau);
        } catch (Exception ex) {
            Logger.error("Không dựng được boss chung cho id " + bossID
                    + ": " + ex + "\n");
            return null;
        }
    }


    /**
     * Mã boss ghi trong {@code boss_spawn} cho một id.
     *
     * <p>Đường tra dành cho boss <b>tạo từ panel</b>: id của chúng không có
     * trong {@code BossID} nên phản chiếu không ra, nhưng dòng trong
     * {@code boss_spawn} thì có sẵn cả id lẫn mã.</p>
     */
    private static String maTrongBossSpawn(int bossID) {
        nro.repository.CrisResultSet rs = null;
        try {
            rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT ten FROM boss_spawn WHERE boss_id = ?", bossID);
            if (rs.next()) {
                String ma = rs.getString("ten");
                return ma == null || ma.trim().isEmpty() ? null : ma.trim();
            }
        } catch (Exception boQua) {
            // Khong doc duoc thi coi nhu khong tra ra.
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua2) {
                }
            }
        }
        return null;
    }
    /**
     * Tên hằng trong {@code BossID} ứng với một id.
     *
     * <p>Dò bằng phản chiếu chứ không chép tay bảng tra: hai trăm hằng số mà
     * chép tay thì thêm một con boss là phải nhớ quay lại sửa, quên là con đó
     * lặng lẽ không dựng được.</p>
     */
    private static String tenHangBossID(int bossID) {
        try {
            for (java.lang.reflect.Field f
                    : nro.entity.boss.BossID.class.getFields()) {
                if (f.getType() == int.class && f.getInt(null) == bossID) {
                    return f.getName();
                }
            }
        } catch (Exception boQua) {
            // Khong doc duoc thi coi nhu khong tra ra.
        }
        return null;
    }
    public Boss getBoss(int id) {
        try {
            Boss boss = this.bosses.get(id);
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private boolean isBossNoNotify(Boss boss) {
        try {
            if (boss == null || boss.data == null || boss.data.length == 0 || boss.data[0] == null) {
                return true;
            }

            if (boss.data[0].getMapJoin() == null || boss.data[0].getMapJoin().length == 0) {
                return true;
            }

            return MapService.gI().isMapNoNottify(boss.data[0].getMapJoin()[0]);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean needBossTimer(Boss boss) {
        if (boss == null) {
            return false;
        }

        return boss.zone == null
                || boss.isDie()
                || boss.bossStatus == BossStatus.DIE
                || boss.bossStatus == BossStatus.REST
                || boss.bossStatus == BossStatus.RESPAWN
                || boss.bossStatus == BossStatus.AFK;
    }

   private long getBossRemainingTime(Boss boss) {
    try {
        if (boss == null || boss.getLastTimeRest() <= 0 || boss.getSecondsRest() <= 0) {
            return 0L;
        }

        long remaining = (long) boss.getSecondsRest() * 1000L
                - (System.currentTimeMillis() - boss.getLastTimeRest());

        return Math.max(remaining, 0L);
    } catch (Exception e) {
        return 0L;
    }
}

   private String getBossKillerName(Boss boss) {
    try {
        if (boss != null
                && boss.getPlayerReward() != null
                && boss.getPlayerReward().name != null
                && !boss.getPlayerReward().name.isEmpty()) {
            return boss.getPlayerReward().name;
        }
    } catch (Exception e) {
    }

    return "Không rõ";
}

    private int getBossMapId(Boss boss) {
        try {
            if (boss == null) {
                return -1;
            }

            if (boss.zone != null && boss.zone.map != null) {
                return boss.zone.map.mapId;
            }

            if (boss.data != null
                    && boss.data.length > 0
                    && boss.data[0] != null
                    && boss.data[0].getMapJoin() != null
                    && boss.data[0].getMapJoin().length > 0) {
                return boss.data[0].getMapJoin()[0];
            }
        } catch (Exception e) {
        }

        return -1;
    }

    private int getBossZoneId(Boss boss) {
        try {
            if (boss != null && boss.zone != null) {
                return boss.zone.zoneId;
            }
        } catch (Exception e) {
        }

        return -1;
    }

    private String getBossLocationText(Boss boss) {
        try {
            if (boss != null && boss.zone != null && boss.zone.map != null) {
                return boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId;
            }

            int mapId = getBossMapId(boss);
            if (mapId != -1) {
                return "Chết rồi - map " + mapId;
            }
        } catch (Exception e) {
        }

        return "Chết rồi";
    }

    private long getLongField(Object obj, String fieldName) {
        if (obj == null || fieldName == null || fieldName.isEmpty()) {
            return 0L;
        }

        Class<?> clazz = obj.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.getLong(obj);
            } catch (Exception e) {
                clazz = clazz.getSuperclass();
            }
        }

        return 0L;
    }

    private long getBossSpawnTime(Boss boss) {
        long value = getLongField(boss, "lastTimeBossSpawn");
        if (value > 0) {
            return value;
        }

        value = getLongField(boss, "lastTimeJoinMap");
        if (value > 0) {
            return value;
        }

        value = getLongField(boss, "lastTimeAppear");
        if (value > 0) {
            return value;
        }

        return 0L;
    }

    private void writeBossRow(Message msg, Player player, int index, Boss boss) throws Exception {
        msg.writer().writeInt(index);
        msg.writer().writeInt(index);

        msg.writer().writeShort(boss.data[0].getOutfit()[0]);

        if (player.getSession() != null && player.getSession().version >= 214) {
            msg.writer().writeShort(-1);
        }

        msg.writer().writeShort(boss.data[0].getOutfit()[1]);
        msg.writer().writeShort(boss.data[0].getOutfit()[2]);

        msg.writer().writeUTF(boss.data[0].getName());

        String status = boss.bossStatus != null ? boss.bossStatus.toString() : "UNKNOWN";
        msg.writer().writeUTF(status);
        msg.writer().writeUTF(getBossLocationText(boss));

        long lastTimeRestVal = 0L;
        int secondsRestVal = 0;
        long remainingTimeVal = 0L;

        if (needBossTimer(boss) && boss.getLastTimeRest() > 0) {
            lastTimeRestVal = boss.getLastTimeRest();
            secondsRestVal = boss.getSecondsRest();
            remainingTimeVal = getBossRemainingTime(boss);
        }

        msg.writer().writeLong(lastTimeRestVal);
        msg.writer().writeInt(secondsRestVal);
        msg.writer().writeLong(remainingTimeVal);

        msg.writer().writeLong(getBossSpawnTime(boss));
        msg.writer().writeUTF(getBossKillerName(boss));
        msg.writer().writeInt(getBossMapId(boss));
        msg.writer().writeInt(getBossZoneId(boss));
    }

   public void showListBoss(Player player) {
    if (player == null || !player.isFounder()) {
        return;
    }

    player.iDMark.setMenuType(3);

    Message msg = null;
    try {
        msg = new Message(-96);
        msg.writer().writeByte(0);
        msg.writer().writeUTF("Boss");

        int count = 0;
        for (Boss boss : bosses) {
            if (boss == null || boss.data == null || boss.data.length == 0 || boss.data[0] == null) {
                continue;
            }
            if (boss.data[0].getMapJoin() == null || boss.data[0].getMapJoin().length == 0) {
                continue;
            }
            if (!MapService.gI().isMapNoNottify(boss.data[0].getMapJoin()[0])) {
                count++;
            }
        }

        msg.writer().writeByte(count);

        for (int i = 0; i < bosses.size(); i++) {
            Boss boss = this.bosses.get(i);

            if (boss == null || boss.data == null || boss.data.length == 0 || boss.data[0] == null) {
                continue;
            }
            if (boss.data[0].getMapJoin() == null || boss.data[0].getMapJoin().length == 0) {
                continue;
            }
            if (MapService.gI().isMapNoNottify(boss.data[0].getMapJoin()[0])) {
                continue;
            }

            msg.writer().writeInt(i);
            msg.writer().writeInt(i);
            msg.writer().writeShort(boss.data[0].getOutfit()[0]);

            if (player.getSession() != null && player.getSession().version >= 214) {
                msg.writer().writeShort(-1);
            }

            msg.writer().writeShort(boss.data[0].getOutfit()[1]);
            msg.writer().writeShort(boss.data[0].getOutfit()[2]);
            msg.writer().writeUTF(boss.data[0].getName());

            if (boss.zone != null) {
                msg.writer().writeUTF(boss.bossStatus.toString());
                msg.writer().writeUTF(boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId);
            } else {
                msg.writer().writeUTF(boss.bossStatus.toString());
                msg.writer().writeUTF("Chết rồi");
            }
        }

        player.sendMessage(msg);
    } catch (Exception e) {
        Logger.logException(BossManager.class, e);
    } finally {
        if (msg != null) {
            msg.cleanup();
        }
    }
}

    public void showListBossMember(Player player) {
        if (player == null) {
            return;
        }

        player.iDMark.setMenuType(3);

        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Danh sách Boss");

            int count = 0;
            for (Boss boss : bosses) {
                if (boss != null
                        && boss.zone != null
                        && !boss.isDie()
                        && !isBossNoNotify(boss)) {
                    count++;
                }
            }

            msg.writer().writeByte(count);

            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = bosses.get(i);

                if (boss == null
                        || boss.zone == null
                        || boss.isDie()
                        || isBossNoNotify(boss)) {
                    continue;
                }

                writeBossRow(msg, player, i, boss);
            }

            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void showListBoss(Player player, List<TypeEventBoss> activeEvents) {
    if (player == null || activeEvents == null) {
        return;
    }

    player.iDMark.setMenuType(3);

    Message msg = null;
    try {
        msg = new Message(-96);
        msg.writer().writeByte(1);
        msg.writer().writeUTF("Boss Sự Kiện");

        int count = 0;
        for (Boss boss : bosses) {
            if (boss == null) {
                continue;
            }

        }

        msg.writer().writeByte(count);

        for (int i = 0; i < bosses.size(); i++) {
            Boss boss = this.bosses.get(i);

            if (boss == null) {
                continue;
            }


            msg.writer().writeInt(i);
            msg.writer().writeInt(i);
            msg.writer().writeShort(boss.data[0].getOutfit()[0]);

            if (player.getSession() != null && player.getSession().version >= 214) {
                msg.writer().writeShort(-1);
            }

            msg.writer().writeShort(boss.data[0].getOutfit()[1]);
            msg.writer().writeShort(boss.data[0].getOutfit()[2]);
            msg.writer().writeUTF(boss.data[0].getName());

            if (boss.zone != null) {
                msg.writer().writeUTF(boss.zone.map.mapName);
                msg.writer().writeUTF(boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId);
            } else {
                msg.writer().writeUTF("Boss bị thằng nào Thịt rồi!");
                msg.writer().writeUTF("Boss bị thằng nào Thịt rồi!");
            }
        }

        player.sendMessage(msg);
    } catch (Exception e) {
        Logger.logException(BossManager.class, e);
    } finally {
        if (msg != null) {
            msg.cleanup();
        }
    }
}

    public Boss getBossById(int bossId) {
        return this.bosses.stream()
                .filter(boss -> boss.id == bossId && !boss.isDie())
                .findFirst()
                .orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return this.bosses.stream()
                .filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie())
                .findFirst()
                .orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream()
                .filter(boss -> boss.id < -100_000_000 && !boss.isDie())
                .findFirst()
                .orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream()
                .filter(boss -> boss.id == bossId
                        && boss.zone != null
                        && boss.zone.map.mapId == mapId
                        && boss.zone.zoneId == zoneId
                        && !boss.isDie())
                .findFirst()
                .orElse(null);
    }

    public Boss getBossTauPayPayByPlayer(Player player) {
        for (int i = bosses.size() - 1; i >= 0; i--) {
            if (bosses.get(i).id == (-251003 - player.id - 2000)) {
                return bosses.get(i);
            }
        }
        return null;
    }

    public void resetAllBosses() {
        try {
            for (Boss boss : this.bosses) {
                if (boss != null && boss.zone != null) {
                    boss.leaveMap();
                    boss.setDieLV(boss);
                }
            }

            this.bosses.clear();
            this.loadBoss();
            System.out.println("[BossManager] Đã reset toàn bộ boss.");
        } catch (Exception e) {
            System.err.println("[BossManager] Lỗi khi reset boss: " + e.getMessage());
        }
    }

    public int respawnAllRestingBosses() {
        int count = 0;
        for (Boss boss : bosses) {
            if (boss != null && (boss.isDie() || boss.zone == null)) {
                try {
                    boss.active();
                    count++;
                } catch (Exception e) {
                    System.err.println("Lỗi hồi sinh boss " + boss.name + ": " + e.getMessage());
                }
            }
        }
        return count;
    }

    public int[] getBossStatusCounts() {
        int alive = 0;
        int dead = 0;
        int resting = 0;

        for (Boss boss : bosses) {
            if (boss == null) {
                continue;
            }

            if (boss.zone == null) {
                resting++;
            } else if (boss.isDie()) {
                dead++;
            } else {
                alive++;
            }
        }

        return new int[]{alive, dead, resting};
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                int delay = 150;
                long st = System.currentTimeMillis();

                // Nhich dong ho cac nhom TRUOC khi cap nhat tung con: neu nhich
                // sau, con boss duoc duyet trong vong nay se thay so luot cu
                // va phai doi them mot vong nua.
                NhomBossService.gI().capNhat();

                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    try {
                        Boss boss = this.bosses.get(i);
                        if (boss != null) {
                            boss.update();
                        }
                    } catch (Exception e) {
                        Logger.logException(BossManager.class, e);
                    }
                }

                Functions.sleep(Math.max(delay - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                Logger.logException(BossManager.class, e);
            }
        }
    }
}
