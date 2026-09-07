package QuanLiBoss.Manager;

/*
 * @Author: Văn KHải
 */
import Boss.list.nro.boss.list.Chilled.Chilled;
import Boss.list.nro.boss.list.Cooler.Cooler;
import Boss.list.nro.boss.list.Frost.Frost;
import Boss.nro.boss.task.BlackGoku.BlackGoku;
import Boss.nro.boss.task.BlackGoku.ZamasKaio;
import Boss.nro.boss.task.BlackGoku.ZamasMax;
import nro.boss.task.Frieza.Fide;
import nro.boss.task.Napa.Kuku;
import nro.boss.task.Napa.Rambo;
import nro.boss.task.RobotAssasinOne.Android19;
import nro.boss.task.RobotAssasinTwo.Android13;
import nro.boss.task.RobotAssasinTwo.Android14;
import nro.boss.task.RobotAssasinTwo.Android15;
import nro.boss.task.RobotAssasinThree.KingKong;
import nro.boss.task.RobotAssasinThree.Pic;
import nro.boss.task.RobotAssasinThree.Poc;
import nro.boss.task.GinyuForce.So1;
import nro.boss.task.GinyuForce.So2;
import nro.boss.task.GinyuForce.So3;
import nro.boss.task.GinyuForce.So4;
import nro.boss.task.GinyuForce.TieuDoiTruong;
import nro.boss.list.Broly.Broly;
import nro.boss.list.Broly.BrolyZone0;
import nro.boss.event.HungVuongEvent.SonTinh;
import nro.boss.event.HungVuongEvent.ThuyTinh;
import nro.boss.list.Earth.Bido;
import nro.boss.list.Earth.Bojack;
import nro.boss.list.Earth.Bujin;
import nro.boss.list.Earth.Kogu;
import nro.boss.list.Earth.Zangya;
import nro.boss.task.GinyuForceNamek.So1Namek;
import nro.boss.task.GinyuForceNamek.So2Namek;
import nro.boss.task.GinyuForceNamek.So3Namek;
import nro.boss.task.GinyuForceNamek.So4Namek;
import nro.boss.task.GinyuForceNamek.TieuDoiTruongNamek;
import nro.boss.map.TaoPaiPai.TaoPaiPai;
import nro.player.Player;
import QuanLiBoss.Boss;
import QuanLiBoss.BossFunction.TestBoss;
import QuanLiBoss.BossID;
import QuanLiBoss.TypeEventBoss;
import nro.server.Maintenance;
import nro.services.MapService;
import Utils.Logger;
import boss.list.Hanhtinhchet.Kami;
import boss.list.Hanhtinhchet.Oren;
import Boss.list.nro.list.boss.Cumber.Cumber;
import Boss.list.nro.list.boss.Cumber.Cumber2;
import boss.list.Tramhuydiet.Berus;
import boss.list.Tramhuydiet.Champa;
import boss.list.Tramhuydiet.Vados;
import boss.list.Tramhuydiet.Whis;
import boss.map.Nguhanhson.KhiCon;
import boss.map.Nguhanhson.KhiCon2;
import boss.map.Nguhanhson.KhiCon3;
import boss.map.Nguhanhson.KhiCon4;
import boss.map.Nguhanhson.KhiCon5;
import boss.map.Nguhanhson.NgoKhong;
import nro.boss.task.PresentCell.XenBoHung;
import java.util.ArrayList;
import java.util.List;
import network.io.Message;
import nro.boss.Event.Halloween.BiNgo;
import nro.boss.event.ChristmasEvent.BrolyNoel;
import nro.boss.event.ChristmasEvent.ChiChiNoel;
import nro.boss.event.ChristmasEvent.ColdChristmas;
import nro.boss.event.ChristmasEvent.GokuGodNoel;
import nro.boss.event.ChristmasEvent.GokuNoel;
import nro.boss.event.ChristmasEvent.OngGiaNoel;
import nro.boss.event.ChristmasEvent.TuanLoc;
import nro.boss.event.LunarNewYearEvent.BeNa;
import nro.boss.event.LunarNewYearEvent.LanCon;
import nro.boss.event.LunarNewYearEvent.MeoDen;
import nro.boss.event.LunarNewYearEvent.NewYearDragon;
import nro.boss.event.LunarNewYearEvent.PiLong;
import nro.boss.event.LunarNewYearEvent.ThanTai;
import nro.boss.event.VuLanFestival.Pikkon;
import nro.boss.list.Earth.SuperBojack;
import nro.boss.list.GoldenFrieza.DeathBeam1;
import nro.boss.list.GoldenFrieza.DeathBeam2;
import nro.boss.list.GoldenFrieza.DeathBeam3;
import nro.boss.list.GoldenFrieza.DeathBeam4;
import nro.boss.list.GoldenFrieza.DeathBeam5;
import nro.boss.list.GoldenFrieza.GoldenFrieza;
import nro.boss.list.PilafGang.Mai;
import nro.boss.list.PilafGang.Pilap;
import nro.boss.list.PilafGang.Shu;
import nro.boss.map.BossNomal.AnTrom;
import nro.boss.map.BossNomal.ODo;
import nro.boss.map.BossNomal.RaiTi;
import nro.boss.map.BossNomal.SoiHecQuyn;
import nro.boss.map.BossNomal.Virus;
import nro.boss.map.BossNomal.XinBaTo;
import nro.boss.map.MajinBuu12H.BuiBui;
import nro.boss.map.MajinBuu12H.BuiBui2;
import nro.boss.map.MajinBuu12H.Cadic;
import nro.boss.map.MajinBuu12H.Drabura;
import nro.boss.map.MajinBuu12H.Drabura2;
import nro.boss.map.MajinBuu12H.Drabura3;
import nro.boss.map.MajinBuu12H.Goku;
import nro.boss.map.MajinBuu12H.Mabu;
import nro.boss.map.MajinBuu12H.Yacon;
import nro.boss.map.MajinBuu14H.MaBu2H;
import nro.boss.map.MajinBuu14H.SuperBu;
import nro.boss.map.Yardart.ChienBinh0;
import nro.boss.map.Yardart.ChienBinh1;
import nro.boss.map.Yardart.ChienBinh2;
import nro.boss.map.Yardart.ChienBinh3;
import nro.boss.map.Yardart.ChienBinh4;
import nro.boss.map.Yardart.ChienBinh5;
import nro.boss.map.Yardart.DoiTruong5;
import nro.boss.map.Yardart.TanBinh0;
import nro.boss.map.Yardart.TanBinh1;
import nro.boss.map.Yardart.TanBinh2;
import nro.boss.map.Yardart.TanBinh3;
import nro.boss.map.Yardart.TanBinh4;
import nro.boss.map.Yardart.TanBinh5;
import nro.boss.map.Yardart.TapSu0;
import nro.boss.map.Yardart.TapSu1;
import nro.boss.map.Yardart.TapSu2;
import nro.boss.map.Yardart.TapSu3;
import nro.boss.map.Yardart.TapSu4;
import nro.boss.task.FutureCell.SieuBoHung;
import nro.boss.task.FutureCell.XenCon1;
import nro.boss.task.FutureCell.XenCon2;
import nro.boss.task.FutureCell.XenCon3;
import nro.boss.task.FutureCell.XenCon4;
import nro.boss.task.FutureCell.XenCon5;
import nro.boss.task.FutureCell.XenCon6;
import nro.boss.task.FutureCell.XenCon7;
import nro.boss.task.RobotAssasinOne.DrKore;
import nro.boss.task.Napa.MapDauDinh;
import nro.boss.Event.Halloween.XuongKho;
import nro.boss.Event.Halloween.Doi;
import nro.boss.Event.Halloween.MaTroi;
import nro.boss.event.HungVuongEvent.RongNhi1Sao;
import nro.boss.event.HungVuongEvent.RongNhi2Sao;
import nro.boss.event.HungVuongEvent.RongNhi3Sao;
import nro.boss.event.HungVuongEvent.RongNhi4Sao;
import nro.boss.event.HungVuongEvent.RongNhi5Sao;
import nro.boss.event.HungVuongEvent.RongNhi6Sao;
import nro.boss.event.HungVuongEvent.RongNhi7Sao;
import nro.boss.event.HungVuongEvent.SonTinhNew;
import nro.boss.event.HungVuongEvent.ThuyTinhNew;
import nro.boss.event.TrungThuEvent.Gogeta;
import nro.boss.event.TrungThuEvent.NguyetThan;
import nro.boss.event.TrungThuEvent.NhatThan;
import nro.boss.event.TrungThuEvent.Omega;
import nro.boss.event.TrungThuEvent.ThoDaiKa;
import nro.boss.event.ValentineEvent.ThoBunma;
import nro.map.Zone;
import utils.Functions;

public class BossManager implements Runnable {

    private static BossManager instance;
    public static byte ratioReward = 10;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    protected final List<Boss> bosses;

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public List<Boss> getBosses() {
        return this.bosses;
    }

    public void loadBoss() {
        this.createBoss(BossID.TIEU_DOI_TRUONG);
        this.createBoss(BossID.KING_KONG);
        this.createBoss(BossID.XEN_BO_HUNG);
        this.createBoss(BossID.SIEU_BO_HUNG);
        this.createBoss(BossID.KUKU, 5);
        this.createBoss(BossID.MAP_DAU_DINH, 5);
        this.createBoss(BossID.RAMBO, 5);
        this.createBoss(BossID.FIDE);
        this.createBoss(BossID.ANDROID_14);
        this.createBoss(BossID.DR_KORE);
        this.createBoss(BossID.TAU_PAY_PAY_DONG_NAM_KARIN);
        this.createBoss(BossID.BOJACK);
        this.createBoss(BossID.SUPER_BOJACK);
        this.createBoss(BossID.GOLDEN_FRIEZA, 5);
        this.createBoss(BossID.PI_LAP);
        this.createBoss(BossID.TIEU_DOI_TRUONG_NAMEK);
        this.createBoss(BossID.BLACK_GOKU, 5);
        this.createBoss(BossID.COOLER);
        this.createBoss(BossID.FROST);
        this.createBoss(BossID.CUMBER);
        this.createBoss(BossID.CHILER);
        this.createBoss(BossID.ZAMASZIN);
        this.createBoss(BossID.ZAMASMAX);
        this.createBoss(BossID.DRABULA2);
        for (int i = 0; i < 100; i++) {
            this.createBoss(BossID.SOI_HEC_QUYN_NOMAL);
            this.createBoss(BossID.O_DO_NOMAL);
            this.createBoss(BossID.VIRUS_NOMAL);
            this.createBoss(BossID.XIN_BA_TO_NOMAL);
        }
        
//         
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
                case BossID.ONG_GIA_NOEL:
                    return new OngGiaNoel();
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
                case BossID.SON_TINH:
                    return new SonTinh();
                case BossID.THUY_TINH:
                    return new ThuyTinh();
                case BossID.SON_TINH_NEW:
                    return new SonTinhNew();
                case BossID.THUY_TINH_NEW:
                    return new ThuyTinhNew();
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
                case BossID.MEO_DEN:
                    return new MeoDen();
                case BossID.PI_LONG:
                    return new PiLong();
                case BossID.BE_NA:
                    return new BeNa();
                case BossID.LAN_CON:
                    return new LanCon();
                case BossID.THAN_TAI:
                    return new ThanTai();
                case BossID.NEW_YEAR_DRAGON:
                    return new NewYearDragon();
                case BossID.GOKU_NOEL:
                    return new GokuNoel();
                case BossID.CHICHI_NOEL:
                    return new ChiChiNoel();
                case BossID.COLD_NOEL:
                    return new ColdChristmas();
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
                case BossID.GOKU_GOD_NOEL:
                    return new GokuGodNoel();
                case BossID.BROLY_NOEL:
                    return new BrolyNoel();
                case BossID.TUAN_LOC:
                    return new TuanLoc();
                case BossID.PIKKON:
                    return new Pikkon();
                case BossID.DOI:
                    return new Doi();
                case BossID.MATROI:
                    return new MaTroi();
                case BossID.XUONG_KHO:
                    return new XuongKho();
                case BossID.BI_NGO:
                    return new BiNgo();
                case BossID.THO_DAI_KA:
                    return new ThoDaiKa();
                case BossID.NHATTHAN:
                    return new NhatThan();
                case BossID.NGUYETTHAN:
                    return new NguyetThan();
                case BossID.OMEGA:
                    return new Omega();
                case BossID.GOGETA:
                    return new Gogeta();
                case BossID.RONG_NHI_1S:
                    return new RongNhi1Sao();
                case BossID.RONG_NHI_2S:
                    return new RongNhi2Sao();
                case BossID.RONG_NHI_3S:
                    return new RongNhi3Sao();
                case BossID.RONG_NHI_4S:
                    return new RongNhi4Sao();
                case BossID.RONG_NHI_5S:
                    return new RongNhi5Sao();
                case BossID.RONG_NHI_6S:
                    return new RongNhi6Sao();
                case BossID.RONG_NHI_7S:
                    return new RongNhi7Sao();
                case BossID.THO_BUNMA:
                    return new ThoBunma();
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

                default:
                    return null;
            }
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
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

    public void showListBoss(Player player) {
        if (!player.isFounder()) {
            return;
        }
        player.iDMark.setMenuType(3);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss");
            msg.writer().writeByte((int) bosses.stream().filter(boss -> !MapService.gI().isMapNoNottify(boss.data[0].getMapJoin()[0])).count());
            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = this.bosses.get(i);
                if (MapService.gI().isMapNoNottify(boss.data[0].getMapJoin()[0])) {
                    continue;
                }
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF(boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId + "");
                } else {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF("Chết rồi");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
        }
    }

    public void showListBoss(Player player, List<TypeEventBoss> activeEvents) {
        player.iDMark.setMenuType(3);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Boss Sự Kiện");
            if (activeEvents.contains(TypeEventBoss.TRUNG_THU)) {
                msg.writer().writeByte((int) bosses.stream().filter(boss -> boss instanceof ThoDaiKa).count());
            }
            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = this.bosses.get(i);
                if (activeEvents.contains(TypeEventBoss.TRUNG_THU)) {
                    if (!(boss instanceof ThoDaiKa)) {
                        continue; // Bỏ qua boss không phải ThoDaiKa
                    }
                }
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF(boss.zone.map.mapName);
                    msg.writer().writeUTF(boss.zone.map.mapName);
                } else {
                    msg.writer().writeUTF("Boss bị thằng nào Thịt rồi!");
                    msg.writer().writeUTF("Boss bị thằng nào Thịt rồi!");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
        }
    }

    public Boss getBossById(int bossId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return this.bosses.stream().filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie()).findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream().filter(boss -> boss.id < -100_000_000 && !boss.isDie()).findFirst().orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && boss.zone != null && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst().orElse(null);
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
                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    try {
                        this.bosses.get(i).update();
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
