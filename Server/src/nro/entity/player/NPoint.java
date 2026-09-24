package nro.entity.player;

import nro.service.effect.EffectSkillService;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstRatio;
import nro.entity.intrinsic.Intrinsic;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.skill.Skill;
import nro.server.Manager;
import nro.server.ServerManager;
import nro.service.MapService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.log.Logger;
import nro.core.util.SkillUtil;
import nro.core.util.Util;
import nro.core.util.FormatStyle;
import nro.core.consts.ConstAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Setter;
import nro.entity.item.ItemOption;
import nro.entity.reward.RewardBlackBall;
import nro.entity.attribute.Attribute;
import nro.entity.badges.BagesTemplate;
import nro.entity.boss.map.trainingboss.TopKillWhisManager;
import nro.entity.mob.Mob;
import nro.entity.card.Card;
import nro.entity.card.OptionCard;
import nro.entity.clan.Buff;
import nro.service.inventory.InventoryService;
import nro.entity.power.PowerLimit;
import nro.service.power.PowerLimitManager;

public class NPoint {

    public static final byte MAX_LIMIT = 13;

    @Setter
    private Player player;

    public NPoint(Player player) {
        this.player = player;
        this.tlHp = new ArrayList<>();
        this.tlMp = new ArrayList<>();
        this.tlDef = new ArrayList<>();
        this.tlDame = new ArrayList<>();
        this.tlDameAttMob = new ArrayList<>();
        this.tlTNSM = new ArrayList<>();
        this.tlDameCrit = new ArrayList<>();
        this.tlSpeed = new ArrayList<>();
    }

    private static final String NGUON_KHONG_RO = "Nguồn không xác định";

    private static final String[] VI_TRI_TRANG_BI = {
        "Áo", "Quần", "Găng", "Giày", "Rada", "Cải trang",
        "Giáp tập luyện", "Thú theo sau", "Danh hiệu", "Ô phụ"
    };

    private static final class NguonChiSo {

        final long giaTri;
        final String nguon;
        final String dong;

        NguonChiSo(long giaTri, String nguon, String dong) {
            this.giaTri = giaTri;
            this.nguon = nguon == null || nguon.isEmpty() ? NGUON_KHONG_RO : nguon;
            this.dong = dong == null ? "" : dong;
        }

        String moTa() {
            return dong.isEmpty() ? nguon : nguon + " | " + dong;
        }
    }

    private List<NguonChiSo> nguonHpAdd = new ArrayList<>();
    private List<NguonChiSo> nguonMpAdd = new ArrayList<>();
    private List<NguonChiSo> nguonSdAdd = new ArrayList<>();
    private List<NguonChiSo> nguonHpPct = new ArrayList<>();
    private List<NguonChiSo> nguonMpPct = new ArrayList<>();
    private List<NguonChiSo> nguonSdPct = new ArrayList<>();
    private List<NguonChiSo> nguonSdMobPct = new ArrayList<>();
    private List<NguonChiSo> nguonSdGiamPct = new ArrayList<>();
    private NguonChiSo nguonSexyDame;
    private NguonChiSo nguonCoolDame;
    private NguonChiSo nguonCuteDame;

    public boolean isCrit;
    public boolean isCrit100;
    public boolean isCritTele;

    private Intrinsic intrinsic;
    private int percentDameIntrinsic;
    public long dameAfter;
    public PowerLimit powerLimit;
    /*-----------------------Chỉ số cơ bản------------------------------------*/
    public byte numAttack;
    public int stamina, maxStamina;

    public byte limitPower;
    public long power;
    public long tiemNang;

    /**
     * Trần cứng của chỉ số gốc, áp cho <b>cả sư phụ lẫn đệ tử</b>.
     *
     * <p>Nằm ngoài {@code powerLimit} và ngoài trần riêng của đệ tử: hai cái kia
     * đọc từ bảng nên quản trị đổi được, còn ba số này là mức trần của máy chủ,
     * đứng trên tất cả. Có nó thì dù bảng bị đặt sai cũng không ai vượt qua
     * được.</p>
     */
    public static final long TRAN_HP_GOC = 600_000L;
    public static final long TRAN_KI_GOC = 600_000L;
    public static final long TRAN_SUC_DANH_GOC = 25_000L;

    public long hp, hpMax, hpg;
    public long mp, mpMax, mpg;
    public long dame, dameg;
    public int def, defg;
    public int crit, critg;
    public byte speed = 5;

    public boolean teleport;

    public boolean khangTDHS;

    public boolean KhangHoaXuong;

    public boolean isHoaBiNgoXungQuanh;

    public boolean wearingMabu;
    public boolean wearingBuiBui;

    public boolean IsTacDungHopThe;

    public void initPowerLimit() {
        powerLimit = PowerLimitManager.getInstance().get(limitPower);
    }

    public List<Integer> tlSpeed;

    /**
     * Tỉ lệ may mắn
     */
    public int tlMayman;

    /**
     * Chỉ số cộng thêm
     */
    public int hpAdd, mpAdd, dameAdd, defAdd, critAdd, hpHoiAdd, mpHoiAdd;

    /**
     * //+#% sức đánh chí mạng
     */
    public List<Integer> tlDameCrit;
    public int tlSDCM;

    /**
     * Tỉ lệ hp, mp cộng thêm
     */
    public List<Integer> tlHp, tlMp;

    /**
     * Tỉ lệ giáp cộng thêm
     */
    public List<Integer> tlDef;

    /**
     * Tỉ lệ sức đánh/ sức đánh khi đánh quái
     */
    public List<Integer> tlDame, tlDameAttMob;

    /**
     * Lượng hp, mp hồi mỗi 30s, mp hồi cho người khác
     */
    public long hpHoi, mpHoi, mpHoiCute;

    /**
     * Tỉ lệ hp, mp hồi cộng thêm
     */
    public int tlHpHoi, tlMpHoi;

    public int tlHpHoiBanthan_DongMinh;

    /**
     * Tỉ lệ hp, mp hồi bản thân và đồng đội cộng thêm
     */
    public int tlHpHoiBanThanVaDongDoi, tlMpHoiBanThanVaDongDoi;

    /**
     * Tỉ lệ hút hp, mp khi đánh, hp khi đánh quái
     */
    public int tlHutHp, tlHutMp, tlHutHpMob;

    /**
     * Tỉ lệ hút hp, mp xung quanh mỗi 5s
     */
    public int tlHutHpMpXQ;

    public int tlFixStun;

    public int tlCuteAddame;

    /**
     * Tỉ lệ phản sát thương
     */
    public int tlPST;

    /**
     * Tỉ lệ tiềm năng sức mạnh
     */
    public List<Integer> tlTNSM;

    /**
     * Tỉ lệ vàng cộng thêm
     */
    public int tlGold;

    /**
     * Tỉ lệ né đòn
     */
    public int tlNeDon;

    /**
     * Giảm bao nhiêu phần trăm <b>thời gian bị mù</b> (choáng Thái dương hạ
     * san, mù của Dịch chuyển tức thời).
     *
     * <p>Cộng từ chỉ số 175 của mọi món đang mặc và của bông tai. Trước đây chỉ
     * số này có trong bể bông tai nhưng <b>không chỗ nào đọc</b>, nên bốc được
     * dòng đó là mất trắng một dòng.</p>
     *
     * <p>Đủ 100 thì đòn mù không còn tác dụng — khác với <code>khangTDHS</code>
     * ở chỗ đây là cộng dồn từ đồ chứ không phải một cờ bật/tắt.</p>
     */
    public int tlGiamMu;

    public int tlBom;

    public int tlGiap;

    public int tlxgcc;

    public int tlxgc;

    public int tlchinhxac;

    public int tlstc;

    /**
     * Giáp đặt riêng cho boss từ bảng {@code boss_config}; {@code -1} là giữ gốc.
     *
     * <h3>Vì sao không dùng {@code defAdd} như mọi chỗ khác</h3>
     *
     * <p>{@code calPoint} xoá sạch {@code defAdd}, {@code tlNeDon},
     * {@code tlchinhxac} rồi dựng lại từ đồ đang mặc. Boss không mặc đồ nên mọi
     * giá trị gán vào đó biến mất ngay lần tính lại đầu tiên — mà boss tính lại
     * chỉ số mỗi khi lên cấp hoặc hồi sinh. Ba trường này không nằm trong danh
     * sách xoá, nên đặt một lần lúc dựng boss là sống suốt.</p>
     */
    public int giapBoss = -1;

    /** Né đòn đặt riêng cho boss, tính bằng phần trăm; {@code -1} là giữ gốc. */
    public int neDonBoss = -1;

    /** Chính xác đặt riêng cho boss, tính bằng phần trăm; {@code -1} là giữ gốc. */
    public int chinhXacBoss = -1;

    /**
     * Cộng Dồn
     */
    public int tlCongDonSD;

    public int tlSubDamePercenMp20;

    /**
     * Tấn công lên tộc
     */
    public int tlTanCongTocTraiDat;

    public int tlTanCongTocNamec;

    public int tlTanCongTocXayda;

    /**
     * Giảm sát thương lên tộc
     */
    public int tlGiamSatThuongTraiDat;

    public int tlGiamSatThuongNamec;

    public int tlGiamSatThuongXayda;

    /**
     * Tăng chỉ số khi ở gần thành viên bang hội
     */
    public int tlDameClan;

    public int tlHpClan;

    public int tlMpClan;

    /**
     * Tăng tấn công lên Boss
     */
    public int tlDameBoss;

    /**
     * Tăng tấn công lên Mob
     */
    public int tlDameMobRun;

    public int tlDameMobMonkey;

    public int tlDameMobFly;

    //Phân Tâm
    public boolean isXinbato;

    //Biến cà rot
    public boolean isThoDaiCa;

    public int tlTNSMPet;
    public int xChuong;

    public int setTinhAn;
    public int setNhatAn;
    public int setNguyetAn;

    /**
     * Tỉ lệ sức đánh đẹp cộng thêm cho bản thân và người xung quanh
     */
    public int tlSexyDame;

    public int tlCoolDame;

    /**
     * Tỉ lệ giảm sức đánh
     */
    public int tlSubSD;
    public int tlSubHP;
    public int tlSubMP;

    public int voHieuChuong;

    /*------------------------Effect skin-------------------------------------*/
    public Item trainArmor;
    public boolean wearingTrainArmor;

    public boolean wearingVoHinh;
    public boolean isKhongLanh;
    public boolean isFounder;
    public boolean isTinhAn;
    public boolean isNhatAn;
    public boolean isNguyetAn;
    public boolean isTanHinh;
    public boolean isHoaDa;
    public boolean isDoSPL;
    public boolean isThoBulma;
    public boolean isBunmaTocMau;
    public boolean isTiecBaiBien;
    public int tlHpGiamODo;

    public boolean isGogeta;

    public boolean Cong20ExpKhiAttackMob;

    public int levelBT;

    /*-------------------------------------------------------------------------*/
    /**
     * Tính toán mọi chỉ số sau khi có thay đổi
     */
    public void calPoint() {
        if (this.player.Detu != null) {
            this.player.Detu.nPoint.setPointWhenWearClothes();
        }
        this.setPointWhenWearClothes();
        // Thoi gian khi/khien va pet De Trung tinh theo do DANG mac: coi set ra
        // la giam ngay, khong giu muc luc dung chieu.
        try {
            if (this.player.effectSkill != null) {
                this.player.effectSkill.tinhLaiTheoDo();
            }
            if (this.player.DeTrung != null) {
                this.player.DeTrung.tinhLaiTheoDo();
            }
        } catch (Exception boQua) {
            // Tinh lai hieu ung chi la phan phu; loi o day khong duoc lam hong
            // viec tinh chi so.
        }
    }

    /**
     * Dựng lại toàn bộ chỉ số từ trang bị, thẻ bài, bùa, set…
     *
     * <h2>Vì sao ảnh chụp tỉ lệ máu nằm ở đây chứ không ở trong</h2>
     *
     * <p>Trần máu được dựng qua <b>hai chặng</b>: {@code setBasePoint()} tính
     * gốc cộng trang bị, rồi {@code apDungSetTuCauHinh()} mới cộng phần của set
     * kích hoạt. Trước bản này, chỗ giữ máu theo tỉ lệ nằm <b>trong</b>
     * {@code setBasePoint()} — tức là nó so trần <i>cũ đã có set</i> với trần
     * <i>mới chưa có set</i>, rồi hạ máu theo đúng cái tỉ lệ lệch ấy. Sang chặng
     * hai trần trở lại đầy đủ, nhưng máu <b>không được nâng theo</b>.</p>
     *
     * <p>Nghĩa là <b>mỗi lần</b> tính lại chỉ số, máu bị nhân với
     * {@code trần-chưa-set / trần-có-set}. Set cộng 50% HP thì mỗi lần gọi máu
     * còn hai phần ba. Mà hàm này được gọi rất thường: đổi bản đồ, mặc cởi một
     * món, bùa hết hạn, hợp thể, tách hợp thể… Máu vì thế cứ tụt dần trong khi
     * không có gì đánh — đúng cảnh "HP tự trừ".</p>
     *
     * <p>Nay chụp tỉ lệ ở <b>đầu</b> và trả lại ở <b>cuối</b>, khi cả hai chặng
     * đã xong: hai đầu so sánh là hai trần đầy đủ như nhau, nên không còn phần
     * lệch nào để mà mất.</p>
     */
    private void setPointWhenWearClothes() {
        // Chup TRUOC khi reset: sau resetPoint thi hpMax van con nguyen tri cu,
        // nhung chup o day cho ro rang la "truoc moi thu".
        final long hpMaxCu = this.hpMax;
        final long mpMaxCu = this.mpMax;
        final long hpCu = this.hp;
        final long mpCu = this.mp;
        this.dangDungLaiDayDu = true;
        try {
            resetPoint();
            if (this.player.rewardBlackBall.timeOutOfDateReward[2] > System.currentTimeMillis()) {
                tlHutHp += RewardBlackBall.R3S_1;
            }
            if (this.player.rewardBlackBall.timeOutOfDateReward[3] > System.currentTimeMillis()) {
                tlPST += RewardBlackBall.R4S_2;
            }
            if (this.player.rewardBlackBall.timeOutOfDateReward[4] > System.currentTimeMillis()) {
                tlDameCrit.add(RewardBlackBall.R5S_1);
                tlSDCM += RewardBlackBall.R5S_1;
            }
            if (this.player.rewardBlackBall.timeOutOfDateReward[6] > System.currentTimeMillis()) {
                tlNeDon += RewardBlackBall.R7S_1;
            }
    
            congChiSoSoSuuTam();
    
            // Bông tai cấp 2
            if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                this.player.inventory.itemsBag.stream().filter(it -> it.isNotNullItem() && it.template.id == 921).findFirst().ifPresent(btc2 -> {
                    for (ItemOption io : btc2.itemOptions) {
                        addOption(io, nguonItem("Hành trang - bông tai cấp 2", btc2));
                        if (io.optionTemplate.id == 72) {
                            this.levelBT = io.param;
                        }
                    }
                });
            }
            // Bông tai cấp 3
            if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                this.player.inventory.itemsBag.stream().filter(it -> it.isNotNullItem() && it.template.id == 1943).findFirst().ifPresent(btc3 -> {
                    for (ItemOption io : btc3.itemOptions) {
                        addOption(io, nguonItem("Hành trang - bông tai cấp 3", btc3));
                        if (io.optionTemplate.id == 72) {
                            this.levelBT = io.param;
                        }
                    }
                });
            }
    
            java.util.List<ItemOption> badgeOptions = BagesTemplate.sendListItemOption(player);
            if (badgeOptions != null) {
                for (ItemOption io : badgeOptions) {
                    addOption(io, "Danh hiệu/badge đang bật");
                }
            }
    
            for (int i = 0; i < this.player.inventory.itemsBody.size(); i++) {
                Item item = this.player.inventory.itemsBody.get(i);
                if (item != null && item.isNotNullItem()) {
                    if (item.template.id >= 592 && item.template.id <= 594) {
                        teleport = true;
                    }
                    if (item.itemOptions != null) {
                        for (ItemOption io : item.itemOptions) {
                            addOption(io, nguonTrangBi(i, item));
                        }
                    }
                }
            }
            setTinhNhatNguyetAn();
            setDameTrainArmor();
            setBasePoint();
            setOutfitFusion();
            apDungSetTuCauHinh();
            ghiHp("Set kích hoạt (bảng set_bonus)", this.hpMax);
            ghiMp("Set kích hoạt (bảng set_bonus)", this.mpMax);
            ghiSd("Set kích hoạt (bảng set_bonus)", this.dame);
            congHpDeTuKhiHopThe();
            ghiHp("HP đệ tử khi hợp thể (đã nhân huýt sáo, bổ huyết)", this.hpMax);
        } finally {
            this.dangDungLaiDayDu = false;
        }
        // Gio tran moi da day du CA HAI chang — moi tra mau ve dung ti le cu.
        capNhatTheoTiLe(hpMaxCu, hpCu, mpMaxCu, mpCu);
        setHp();
        setMp();
    }

    /**
     * Cộng máu của đệ tử vào trần máu sư phụ khi đang hợp thể.
     *
     * <h3>Vì sao cộng ở đây, sau cùng</h3>
     *
     * <p>Trước đây phép cộng này nằm giữa {@code setHpMax()}, nên phần máu của
     * đệ tử còn phải đi qua <b>toàn bộ</b> các phép nhân phía dưới: bổ huyết
     * nhân đôi, chibi nhân đôi, hồng đào, Porata, top Whis… rồi lại đi tiếp qua
     * các dòng phần trăm của set kích hoạt ở {@code apDungSetTuCauHinh()}.</p>
     *
     * <p>Một đệ tử 100 triệu máu vì thế có thể thành vài trăm triệu trên người
     * sư phụ, chỉ vì sư phụ đang uống bổ huyết. Đó là chỗ trần máu phình lên
     * quá mức — và cũng là lý do con số hiện ra bị chặn ở 2.147.483.647, tức
     * trần của kiểu số bốn byte mà gói tin dùng.</p>
     *
     * <p>Gọi ở đây thì mọi phép nhân đã xong xuôi, nên máu đệ tử vào đúng bằng
     * chính nó — <b>cộng sau khi tính toán</b>, đúng nghĩa.</p>
     *
     * <h3>Các mức</h3>
     *
     * <p>Đệ tử loại 1 cho 120% máu của nó, loại 5 cho 140%, còn lại 100%. Phần
     * ấy <b>chỉ</b> nhân cùng huýt sáo và bổ huyết của sư phụ — bông tai, thẻ,
     * top Whis, set, % trên đồ của sư phụ không nhân vào phần của đệ.</p>
     */
    /**
     * Đệ tử đang nhập vào sư phụ — mọi kiểu hợp thể, kể cả hợp thể vĩnh viễn.
     *
     * <p>Lúc ấy khỉ, huýt sáo… của <b>đệ</b> không được tính gì: phần của đệ
     * cộng vào sư phụ là chỉ số thật của đệ, không kèm hiệu ứng đang dính trên
     * người nó. Trước chỉ xét {@code status == FUSION}, bỏ sót hợp thể vĩnh
     * viễn và lúc trạng thái chưa kịp đổi.</p>
     */
    private boolean laDeDangHopThe() {
        if (this.player == null || !this.player.isDeTu) {
            return false;
        }
        Detu de = (Detu) this.player;
        if (de.status == Detu.FUSION || de.status == Detu.HTVV) {
            return true;
        }
        return de.master != null && de.master.fusion != null
                && de.master.fusion.typeFusion != ConstPlayer.NON_FUSION;
    }

    /** Nhật ký từng bước tính trần máu — chỉ bật khi panel xin "Chi tiết HP". */
    private java.util.List<String[]> nhatKyHp;

    private long hpTruocBuoc;

    private void ghiHp(String buoc, long giaTri) {
        ghiHp(buoc, giaTri, "");
    }

    private void ghiHp(String buoc, long giaTri, String moTa) {
        if (nhatKyHp == null || giaTri == hpTruocBuoc) {
            return;
        }
        nhatKyHp.add(new String[]{buoc, String.valueOf(giaTri - hpTruocBuoc),
            String.valueOf(giaTri), moTa == null ? "" : moTa});
        hpTruocBuoc = giaTri;
    }

    /**
     * Tính lại chỉ số và trả về từng bước làm nên trần máu.
     *
     * @return mỗi dòng {bước, thay đổi, trần máu sau bước}; chỉ bước nào
     *         thật sự đổi con số mới có dòng
     */
    /** Nhật ký từng bước tính KI tối đa — chỉ bật khi panel xin xem chi tiết. */
    private java.util.List<String[]> nhatKyMp;

    private long mpTruocBuoc;

    /** Nhật ký từng bước tính sức đánh — chỉ bật khi panel xin xem chi tiết. */
    private java.util.List<String[]> nhatKySd;

    private long sdTruocBuoc;

    private void ghiMp(String buoc, long giaTri) {
        ghiMp(buoc, giaTri, "");
    }

    private void ghiMp(String buoc, long giaTri, String moTa) {
        if (nhatKyMp == null || giaTri == mpTruocBuoc) {
            return;
        }
        nhatKyMp.add(new String[]{buoc, String.valueOf(giaTri - mpTruocBuoc),
            String.valueOf(giaTri), moTa == null ? "" : moTa});
        mpTruocBuoc = giaTri;
    }

    private void ghiSd(String buoc, long giaTri) {
        ghiSd(buoc, giaTri, "");
    }

    private void ghiSd(String buoc, long giaTri, String moTa) {
        if (nhatKySd == null || giaTri == sdTruocBuoc) {
            return;
        }
        nhatKySd.add(new String[]{buoc, String.valueOf(giaTri - sdTruocBuoc),
            String.valueOf(giaTri), moTa == null ? "" : moTa});
        sdTruocBuoc = giaTri;
    }

    /**
     * Tính lại chỉ số một lần và trả về từng bước của <b>HP tối đa, KI tối đa,
     * sức đánh</b> — đúng thứ tự ấy. Mỗi dòng {bước, thay đổi, sau bước}.
     */
    public java.util.List<java.util.List<String[]>> giaiThichChiSo() {
        java.util.List<String[]> hp = new java.util.ArrayList<>();
        java.util.List<String[]> mp = new java.util.ArrayList<>();
        java.util.List<String[]> sd = new java.util.ArrayList<>();
        this.nhatKyHp = hp;
        this.hpTruocBuoc = 0;
        this.nhatKyMp = mp;
        this.mpTruocBuoc = 0;
        this.nhatKySd = sd;
        this.sdTruocBuoc = 0;
        try {
            calPoint();
        } finally {
            this.nhatKyHp = null;
            this.nhatKyMp = null;
            this.nhatKySd = null;
        }
        return java.util.Arrays.asList(hp, mp, sd);
    }

    public java.util.List<String[]> giaiThichHp() {
        java.util.List<String[]> ds = new java.util.ArrayList<>();
        this.nhatKyHp = ds;
        this.hpTruocBuoc = 0;
        try {
            calPoint();
        } finally {
            this.nhatKyHp = null;
        }
        return ds;
    }

    private void congHpDeTuKhiHopThe() {
        if (this.player.Detu == null
                || this.player.fusion.typeFusion == ConstPlayer.NON_FUSION) {
            return;
        }
        long hpDe = this.player.Detu.nPoint.hpMax;
        if (hpDe <= 0) {
            return;
        }
        long phan = hpDe;
        if (this.player.Detu.typeDeTu == 1) {
            phan += hpDe * 20 / 100L;
        } else if (this.player.Detu.typeDeTu == 5) {
            phan += hpDe * 40 / 100L;
        }
        // Chi nhan cung huyt sao va bo huyet cua su phu. Bong tai, the, top
        // Whis, set, % tren do cua su phu KHONG nhan vao phan cua de.
        if (this.player.effectSkill != null && this.player.effectSkill.tiLeHPHuytSao != 0) {
            phan += phan * this.player.effectSkill.tiLeHPHuytSao / 100L;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet) {
            phan *= 2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            phan = (long) (phan * 2.2);
        }
        this.hpMax += phan;
    }

    /**
     * Đang ở giữa một lượt dựng lại chỉ số <b>đầy đủ</b>.
     *
     * <p>Cờ này để {@code setBasePoint()} biết là nó đang bị gọi ở chặng giữa,
     * và đừng tự chỉnh máu theo một cái trần chưa cộng set — việc đó đã có
     * {@code setPointWhenWearClothes()} lo ở hai đầu.</p>
     */
    private boolean dangDungLaiDayDu;

    /**
     * Áp các dòng chỉ số set lấy từ bảng {@code set_bonus}.
     *
     * <p>Gọi <b>sau</b> {@code setBasePoint()} nên {@code hpMax}, {@code dame},
     * {@code def}, {@code crit} đã có giá trị cuối — dòng phần trăm tính trên
     * con số thật, không phải trên số gốc chưa cộng trang bị.</p>
     *
     * <p>Bảng rỗng thì hàm này không làm gì, máy chủ chạy y như trước khi có
     * tính năng. Cấu hình hỏng cũng không được làm vỡ việc tính chỉ số — bọc
     * toàn bộ trong {@code try}.</p>
     */
    private void apDungSetTuCauHinh() {
        try {
            if (this.player.setClothes == null) {
                return;
            }
            for (String setKey : nro.repository.dao.SetBonusDAO.cacSet()) {
                // Chi ap MOC CAO NHAT dat duoc cua tung set.
                //
                // Ap ca bon moc thi cac dong phan tram con NHAN CHONG len
                // nhau, vi apDungMotDong sua thang hpMax roi moc sau lai tinh
                // phan tram tren so vua sua. Set KI 10/30/50/80 khong ra +80%
                // ma ra 1,1 x 1,3 x 1,5 x 1,8 = gan +286%.
                for (nro.repository.dao.SetBonusDAO.Bonus b
                        : nro.repository.dao.SetBonusDAO.mocDangHuong(
                                this.player.setClothes, setKey)) {
                    apDungMotDong(setKey, b);
                }
            }
        } catch (Exception ex) {
            nro.core.log.Logger.logException(NPoint.class, ex,
                    "Lỗi áp chỉ số set từ cấu hình cho " + this.player.name);
        }
    }

    /**
     * Tổng phần trăm của một loại chỉ số, gộp từ mọi set đang mặc đủ số món.
     *
     * <p>Dùng cho những loại <b>không cộng vào trường nào của {@code NPoint}</b>
     * mà phải nhân vào một giá trị tính tại chỗ — sát thương quả cầu ki, sát
     * thương đánh thường, tiềm năng nhận được.</p>
     */
    private int phanTramSetTheoLoai(String loai) {
        try {
            if (this.player.setClothes == null) {
                return 0;
            }
            int tong = 0;
            for (String setKey : nro.repository.dao.SetBonusDAO.cacSet()) {
                // Trong MOT set chi lay moc cao nhat; giua CAC set thi cong.
                for (nro.repository.dao.SetBonusDAO.Bonus b
                        : nro.repository.dao.SetBonusDAO.mocDangHuong(
                                this.player.setClothes, setKey)) {
                    if (loai.equals(b.loai)) {
                        tong += (int) b.giaTri;
                    }
                }
            }
            return tong;
        } catch (Exception ex) {
            return 0;      // cấu hình hỏng không được làm vỡ việc tính sát thương
        }
    }

    private static String dauSo(long v) {
        return v >= 0 ? "+" : "";
    }

    private static String nguonSet(String setKey) {
        return "Set kích hoạt: " + (setKey == null || setKey.isEmpty() ? "không rõ" : setKey);
    }

    private static String moTaSet(String setKey, nro.repository.dao.SetBonusDAO.Bonus b) {
        if (b == null) {
            return nguonSet(setKey) + " | Dòng set_bonus không rõ.";
        }
        return nguonSet(setKey) + " | "
                + nro.repository.dao.SetBonusDAO.moTaChiSo(b.soMon, b.loai, b.giaTri, b.thamSo)
                + " | Lấy từ bảng set_bonus, đã qua SetBonusDAO.mocDangHuong.";
    }

    /** Cộng một dòng chỉ số set vào chỉ số hiện tại. */
    private void apDungMotDong(String setKey, nro.repository.dao.SetBonusDAO.Bonus b) {
        long v = b.giaTri;
        switch (b.loai) {
            case "hp":
                this.hpMax += v;
                ghiHp("Set " + setKey + " " + dauSo(v) + v + " HP", this.hpMax, moTaSet(setKey, b));
                break;
            case "hp_pct":
                this.hpMax += calPercent(this.hpMax, (int) v);
                ghiHp("Set " + setKey + " " + dauSo(v) + v + "% HP", this.hpMax, moTaSet(setKey, b));
                break;
            case "ki":
                this.mpMax += v;
                ghiMp("Set " + setKey + " " + dauSo(v) + v + " KI", this.mpMax, moTaSet(setKey, b));
                break;
            case "ki_pct":
                this.mpMax += calPercent(this.mpMax, (int) v);
                ghiMp("Set " + setKey + " " + dauSo(v) + v + "% KI", this.mpMax, moTaSet(setKey, b));
                break;
            case "dame":
                this.dame += v;
                ghiSd("Set " + setKey + " " + dauSo(v) + v + " sức đánh", this.dame, moTaSet(setKey, b));
                break;
            case "dame_pct":
                this.dame += calPercent(this.dame, (int) v);
                ghiSd("Set " + setKey + " " + dauSo(v) + v + "% sức đánh", this.dame, moTaSet(setKey, b));
                break;
            case "def":
                this.def += (int) v;
                break;
            case "def_pct":
                this.def += calPercent(this.def, (int) v);
                break;
            case "crit":
                this.crit += (int) v;
                break;
            case "sdcm":
                this.tlSDCM += (int) v;
                this.tlDameCrit.add((int) v);
                break;
            case "chi_mang_ca_hai":
                // Mot con so, an ca hai cho — nguoi dat set khoi phai them hai
                // dong roi nho giu chung bang nhau.
                this.crit += (int) v;
                this.tlSDCM += (int) v;
                this.tlDameCrit.add((int) v);
                break;
            case "hp_hoi":
                this.hpHoi += v;
                break;
            case "ne_don":
            case "ne_don_pct":
                this.tlNeDon += (int) v;
                break;
            case "pst":
                this.tlPST += (int) v;
                break;
            case "hut_hp":
                this.tlHutHp += (int) v;
                break;
            case "hut_ki":
                this.tlHutMp += (int) v;
                break;
            case "may_man":
                this.tlMayman += (int) v;
                break;
            case "gold_pct":
                this.tlGold += (int) v;
                break;
            case "dame_mob_pct":
                // Danh sach: moi phan tu duoc nhan don o getDamage khi danh quai.
                this.tlDameAttMob.add((int) v);
                this.nguonSdMobPct.add(new NguonChiSo(v, nguonSet(setKey),
                        nro.repository.dao.SetBonusDAO.moTaChiSo(b.soMon, b.loai, b.giaTri, b.thamSo)
                        + " | Lấy từ bảng set_bonus."));
                break;
            case "giap_pct":
                this.tlGiap += (int) v;
                break;
            case "chinh_xac":
                this.tlchinhxac += (int) v;
                break;
            case "xuyen_giap":
                this.tlxgc += (int) v;
                break;
            case "xuyen_giap_cm":
                this.tlxgcc += (int) v;
                break;
            case "bom_pct":
                this.tlBom += (int) v;
                break;
            case "hp_hoi_pct":
                // Tran 100 duoc ap ngay sau khi tinh xong toan bo trang bi
                // (setPointWhenWearClothes), khong can chan o day.
                this.tlHpHoi += (int) v;
                break;
            case "ki_hoi_pct":
                this.tlMpHoi += (int) v;
                break;
            case "fix_stun":
                this.tlFixStun += (int) v;
                break;
            case "tai_tao_pct":
            case "hoi_chieu_pct":
            case "choang_pct":
            case "giam_tieu_hao_pct":
                // Ba loai nay khong cong vao truong nao cua NPoint: cho dung
                // chung nam ngoai lop nay (luc gong, luc dung chieu, luc gay
                // choang). Doc thang bang SetBonusDAO.phanTramLoai() tai cho.
                break;
            default:
                // Loại lạ (dữ liệu cũ hoặc gõ tay vào CSDL) -> bỏ qua lặng lẽ,
                // không được làm hỏng chỉ số của người chơi.
                break;
        }
        // KHONG kep mau o day.
        //
        // Ham nay chay giua chung: tran moi cong duoc vai dong set, con thieu
        // cac dong sau. Kep vao mot con so dang dang la cat mau xuong theo mot
        // tran khong bao gio ton tai that — va cat roi thi khong co gi dung no
        // len lai. Cho kep dung mot lan, o cuoi setPointWhenWearClothes, khi
        // tran da la tran that.
    }

    /**
     * Chỉ số từ <b>Sổ sưu tầm</b> — đúng như màn sổ hiện cho người chơi.
     *
     * <ul>
     *   <li>Dòng cấp 0 ("Mở khoá"): có hiệu lực với <b>mọi</b> thẻ đã sưu tầm,
     *       không cần bật.</li>
     *   <li>Dòng Lv.N: có hiệu lực với thẻ <b>đang bật</b> đã đạt Lv.N trở lên —
     *       cộng dồn các cấp.</li>
     * </ul>
     *
     * <p>Đọc chỉ số từ <b>mẫu thẻ</b> (bảng radar), không từ bản chép lưu trong
     * nhân vật: sửa trên panel là mọi người nhận ngay. Đi qua {@link #addOption}
     * như đồ đang mặc — bản cũ tự viết một bảng {@code switch} riêng chỉ biết vài
     * chục chỉ số, và chỉ cộng đúng dòng của cấp hiện tại.</p>
     */
    private void congChiSoSoSuuTam() {
        if (this.player.Cards == null || this.player.Cards.isEmpty()) {
            return;
        }
        for (Card card : new java.util.ArrayList<>(this.player.Cards)) {
            if (card == null || card.Level == 0) {
                continue;
            }
            nro.entity.card.RadarCard mau = nro.service.card.RadarService.gI().theo(card.Id);
            if (mau == null || mau.Options == null) {
                continue;
            }
            int bac = nro.entity.card.RadarCard.bac(card);
            for (OptionCard oc : mau.Options) {
                if (oc == null) {
                    continue;
                }
                if (oc.active != 0 && (card.Used != 1 || oc.active > bac)) {
                    continue;
                }
                ItemOption io = new ItemOption(oc.id, oc.param);
                if (io.optionTemplate != null) {
                    String nguon = "Sổ sưu tầm: " + mau.Name + " (id " + mau.Id
                            + ", cấp hiện tại " + card.Level
                            + (oc.active == 0 ? ", dòng mở khóa" : ", dòng Lv." + oc.active)
                            + ")";
                    addOption(io, nguon);
                }
            }
        }
    }

    private static String viTriTrangBi(int viTri) {
        if (viTri >= 0 && viTri < VI_TRI_TRANG_BI.length) {
            return VI_TRI_TRANG_BI[viTri];
        }
        return "Ô trang bị " + viTri;
    }

    private static String tenItem(Item item) {
        if (item == null || item.template == null) {
            return "vật phẩm không rõ";
        }
        return item.template.name + " (id " + item.template.id + ")";
    }

    private static String nguonTrangBi(int viTri, Item item) {
        return "Trang bị - " + viTriTrangBi(viTri) + ": " + tenItem(item);
    }

    private static String nguonItem(String nhom, Item item) {
        return nhom + ": " + tenItem(item);
    }

    private static String moTaOption(ItemOption io) {
        if (io == null || io.optionTemplate == null) {
            return "Option không rõ";
        }
        String text;
        try {
            text = io.getOptionString();
        } catch (Exception ex) {
            text = io.optionTemplate.name;
        }
        if (text == null || text.isEmpty()) {
            text = io.optionTemplate.name;
        }
        return "Option " + io.optionTemplate.id + ": " + text;
    }

    private static NguonChiSo nguonChiSo(ItemOption io, String nguon, long giaTri) {
        return new NguonChiSo(giaTri, nguon, moTaOption(io));
    }

    private static String moTaNguonList(List<NguonChiSo> ds) {
        if (ds == null || ds.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (NguonChiSo n : ds) {
            if (n == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(n.moTa());
        }
        return sb.toString();
    }

    private void clearNguonChiSo() {
        if (nguonHpAdd != null) {
            nguonHpAdd.clear();
            nguonMpAdd.clear();
            nguonSdAdd.clear();
            nguonHpPct.clear();
            nguonMpPct.clear();
            nguonSdPct.clear();
            nguonSdMobPct.clear();
            nguonSdGiamPct.clear();
        }
        nguonSexyDame = null;
        nguonCoolDame = null;
        nguonCuteDame = null;
    }

    private void addOption(ItemOption io) {
        addOption(io, NGUON_KHONG_RO);
    }

    private void addOption(ItemOption io, String nguon) {
        if (io == null || io.optionTemplate == null) {
            return;
        }
        switch (io.optionTemplate.id) {
            case 0: //Tấn công +#
                this.dameAdd += io.param;
                this.nguonSdAdd.add(nguonChiSo(io, nguon, io.param));
                break;
            case 2: //HP, KI+#000
                this.hpAdd += io.param * 1000;
                this.mpAdd += io.param * 1000;
                this.nguonHpAdd.add(nguonChiSo(io, nguon, io.param * 1000L));
                this.nguonMpAdd.add(nguonChiSo(io, nguon, io.param * 1000L));
                break;
            case 3:// vô hiệu chưởng
                this.voHieuChuong += io.param;
                break;
            case 5: //+#% sức đánh chí mạng
                this.tlDameCrit.add(io.param);
                this.tlSDCM += io.param;
                break;
            case 6: //HP+#
                this.hpAdd += io.param;
                this.nguonHpAdd.add(nguonChiSo(io, nguon, io.param));
                break;
            case 7: //KI+#
                this.mpAdd += io.param;
                this.nguonMpAdd.add(nguonChiSo(io, nguon, io.param));
                break;
            case 8: //Hút #% HP, KI xung quanh mỗi 5 giây
                this.tlHutHpMpXQ += io.param;
                break;
            case 10:
                this.tlstc += io.param;
                break;
            case 14: //Chí mạng+#%
            case 192:
                this.critAdd += io.param;
                break;
            case 16: // Speed
            case 114:
            case 148:
                this.tlSpeed.add(io.param);
                break;
            case 18: //Chinh xac
                this.tlchinhxac += io.param;
                break;
            case 19: //Tấn công+#% khi đánh quái
                this.tlDameAttMob.add(io.param);
                this.nguonSdMobPct.add(nguonChiSo(io, nguon, io.param));
                break;
            case 22: //HP+#K
                this.hpAdd += io.param * 1000;
                this.nguonHpAdd.add(nguonChiSo(io, nguon, io.param * 1000L));
                break;
            case 23: //MP+#K
                this.mpAdd += io.param * 1000;
                this.nguonMpAdd.add(nguonChiSo(io, nguon, io.param * 1000L));
                break;
            case 24: //Làm chậm
                this.wearingBuiBui = true;
                break;
            case 25: //Tàn hình
                this.isTanHinh = true;
                break;
            case 26: //Hóa đá
                this.isHoaDa = true;
                break;
            case 27: //+# HP/30s
                this.hpHoiAdd += io.param;
                break;
            case 28: //+# KI/30s
                this.mpHoiAdd += io.param;
                break;
            case 29:
                this.wearingMabu = true;
                break;
            case 32:
                this.KhangHoaXuong = true;
                break;
            case 33: //dịch chuyển tức thời
                this.teleport = true;
                break;
            case 34:
                this.setTinhAn += 1;
                break;
            case 35:
                this.setNguyetAn += 1;
                break;
            case 36:
                this.setNhatAn += 1;
                break;
            case 38:
                this.IsTacDungHopThe = true;
                break;
            case 42:
                this.tlDameMobFly += io.param;
                break;
            case 43:
                this.tlDameMobMonkey += io.param;
                break;
            case 44:
                this.tlDameMobRun += io.param;
                break;
            case 45:
                this.tlTanCongTocNamec += io.param;
                break;
            case 46:
                this.tlTanCongTocTraiDat += io.param;
                break;
            case 47: //Giáp+#
                this.defAdd += io.param;
                break;
            case 48: //HP/KI+#
                this.hpAdd += io.param;
                this.mpAdd += io.param;
                this.nguonHpAdd.add(nguonChiSo(io, nguon, io.param));
                this.nguonMpAdd.add(nguonChiSo(io, nguon, io.param));
                break;
            case 49: //Tấn công+#%
            case 50: //Sức đánh+#%
                this.tlDame.add(io.param);
                this.nguonSdPct.add(nguonChiSo(io, nguon, io.param));
                break;
            case 77: //HP+#%
                this.tlHp.add(io.param);
                this.nguonHpPct.add(nguonChiSo(io, nguon, io.param));
                break;
            case 80: //HP+#%/30s
                this.tlHpHoi += io.param;
                break;
            case 81: //MP+#%/30s
                this.tlMpHoi += io.param;
                break;
            case 83:
                this.Cong20ExpKhiAttackMob = true;
                break;
            case 88: //Cộng #% exp khi đánh quái
                this.tlTNSM.add(io.param);
                break;
            case 94: //Giáp #%
                this.tlGiap += io.param;
                break;
            case 95: //Biến #% tấn công thành HP
                this.tlHutHp += io.param;
                break;
            case 96: //Biến #% tấn công thành MP
                this.tlHutMp += io.param;
                break;
            case 97: //Phản #% sát thương
                this.tlPST += io.param;
                break;
            case 98: //Xuyen giap chuong
                this.tlxgc += io.param;
                break;
            case 99: //Xuyen giap can chien
                this.tlxgcc += io.param;
                break;
            case 100: //+#% vàng từ quái
                this.tlGold += io.param;
                break;
            case 101: //+#% TN,SM
                this.tlTNSM.add(io.param);
                break;
            case 103: //KI +#%
                this.tlMp.add(io.param);
                this.nguonMpPct.add(nguonChiSo(io, nguon, io.param));
                break;
            case 104: //Biến #% tấn công quái thành HP
                this.tlHutHpMob += io.param;
                break;
            case 105: //Vô hình khi không đánh quái và boss
                this.wearingVoHinh = true;
                break;
            case 106: //Không ảnh hưởng bởi cái lạnh
                this.isKhongLanh = true;
                break;
            case 108: //#% Né đòn
                this.tlNeDon += io.param;
                break;
            case 109: //Hôi, giảm #% HP
                this.tlHpGiamODo += io.param;
                break;
            case 110: //Do spl
                this.isDoSPL = true;
                break;
            case 111: //phan tâm
                this.isXinbato = true;
                break;
            case 115: //biến cà rot
                this.isThoDaiCa = true;
                break;
            case 116: //Kháng thái dương hạ san
                this.khangTDHS = true;
                break;

            case 117: //Đẹp +#% SĐ cho mình và người xung quanh
                if (io.param > this.tlSexyDame) {
                    this.tlSexyDame = io.param;
                    this.nguonSexyDame = nguonChiSo(io, nguon, io.param);
                }
                break;
            case 147: //+#% sức đánh
                this.tlDame.add(io.param);
                this.nguonSdPct.add(nguonChiSo(io, nguon, io.param));
                break;
            case 155: //Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                this.tlSubSD += 50;
                this.tlSubHP += 50;
                this.tlSubMP += 50;
                this.tlTNSM.add(io.param);
                this.tlGold += io.param;
                this.nguonSdGiamPct.add(new NguonChiSo(50, nguon, moTaOption(io)));
                break;
            case 156:
                this.tlCongDonSD += io.param;
                break;
            case 157:
                this.tlSubDamePercenMp20 += io.param;
                break;
            case 162: //Cute hồi #% KI/s bản thân và xung quanh
                this.mpHoiCute += io.param;
                break;
            case 159: // x chưởng
                this.xChuong = io.param;
                break;
            case 160: // TNSM PET;
                this.tlTNSMPet += io.param;
                break;
            case 173: //Phục hồi #% HP và KI cho đồng đội
                this.tlHpHoiBanThanVaDongDoi += io.param;
                this.tlMpHoiBanThanVaDongDoi += io.param;
                break;
            case 175: //Giảm #% thời gian bị mù
                this.tlGiamMu += io.param;
                break;
            case 176: //
                setInfoOption176(nguon, io);
                break;
            case 197:
                this.tlTanCongTocXayda += io.param;
                break;
            case 198:
                this.tlGiamSatThuongTraiDat += io.param;
                break;
            case 199:
                this.tlGiamSatThuongNamec += io.param;
                break;
            case 200:
                this.tlGiamSatThuongXayda += io.param;
                break;
            case 201:
                this.tlDameClan += io.param;
                break;
            case 202:
                this.tlHpClan += io.param;
                break;
            case 203:
                this.tlMpClan += io.param;
                break;
            case 204:
                this.tlDameBoss += io.param;
                break;
            case 153: //% phát nổ sau khi chết
                this.tlBom += io.param;
                break;
            case 163:
                this.isHoaBiNgoXungQuanh = true;
                break;
            case 256: //founder
                this.isFounder = true;
                break;
            case 236: //may mắn
                this.tlMayman += io.param;
                break;
            case 258:
                if (io.param > this.tlCoolDame) {
                    this.tlCoolDame = io.param;
                    this.nguonCoolDame = nguonChiSo(io, nguon, io.param);
                }
                break;
            case 259: //HP+#%/10s
                this.tlHpHoiBanthan_DongMinh += io.param;
                break;
            case 226:
                if (io.param > this.tlCuteAddame) {
                    this.tlCuteAddame = io.param;
                    this.nguonCuteDame = nguonChiSo(io, nguon, io.param);
                }
                break;
            case 227:
                this.tlFixStun += io.param;
                break;
        }
    }

    private void setSpeed() {
        for (Integer tl : this.tlSpeed) {
            this.speed += calPercent(this.speed, tl);
        }
        if (this.player.effectSkill != null
                && this.player.effectSkill.giamTocChayKyNang > 0) {
            int giam = Math.min(90, this.player.effectSkill.giamTocChayKyNang);
            this.speed -= calPercent(this.speed, giam);
            if (this.speed < 1) {
                this.speed = 1;
            }
        }
        if (this.player.effectSkin.isSlow) {
            this.speed = 1;
        }
    }

    private void setInfoOption176(String nguon, ItemOption io) {
        if (player.isPl()) {
            this.tlDame.add(20);
            this.nguonSdPct.add(new NguonChiSo(20, nguon,
                    moTaOption(io) + " | Hiệu ứng đủ 5 loại: +20% sức đánh"));
            this.tlSpeed.add(50);
        }
    }

    private void setOutfitFusion() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.Detu == null || this.player.Detu.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.Detu.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isGogeta = skin.template.id == 1693 && pskin.template.id == 1553 || skin.template.id == 1553 && pskin.template.id == 1693;
        } else {
            this.isGogeta = false;
        }
    }

    private void setDameTrainArmor() {
        if (player.isPl()) {
            if (this.player.inventory.itemsBody.size() < 7) {
                return;
            }
            try {
                Item gtl = this.player.inventory.itemsBody.get(6);
                if (gtl != null && gtl.isNotNullItem()) {
                    this.wearingTrainArmor = true;
                    this.player.inventory.trainArmor = gtl;
                    int pct = ItemService.gI().getPercentTrainArmor(gtl);
                    this.tlSubSD += pct;
                    this.nguonSdGiamPct.add(new NguonChiSo(pct,
                            nguonItem("Trang bị - giáp tập luyện đang mặc", gtl),
                            "Giảm sức đánh khi đang mặc giáp tập luyện"));
                } else {
                    if (this.player.inventory.trainArmor == null) {
                        gtl = this.player.inventory.itemsBag.stream()
                                .filter(item -> item != null && item.isNotNullItem()
                                && item.template != null && item.template.type == 32
                                && item.itemOptions != null
                                && item.itemOptions.stream()
                                        .anyMatch(io -> io != null && io.optionTemplate.id == 9 && io.param > 0))
                                .findFirst().orElse(null);
                        if (gtl == null) {
                            return;
                        }
                        this.player.inventory.trainArmor = gtl;
                    }
                    this.wearingTrainArmor = false;
                    if (this.player.inventory.trainArmor != null
                            && this.player.inventory.trainArmor.itemOptions != null) {
                        for (ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                            if (io != null && io.optionTemplate.id == 9 && io.param > 0) {
                                int pct = ItemService.gI()
                                        .getPercentTrainArmor(this.player.inventory.trainArmor);
                                this.tlDame.add(pct);
                                this.nguonSdPct.add(new NguonChiSo(pct,
                                        nguonItem("Hành trang - giáp tập luyện còn hạn",
                                                this.player.inventory.trainArmor),
                                        "Không mặc trên người nên cộng +" + pct
                                        + "% sức đánh từ giáp tập luyện"));
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Lỗi get giáp tập luyện " + this.player.name + "\n" + e + "\n");
            }
        }
    }

    /**
     * Dựng lại toàn bộ chỉ số dẫn xuất từ trang bị, set, bùa, bản đồ...
     *
     * <h2>HP và KI giữ theo TỈ LỆ, không bị kẹp một chiều</h2>
     *
     * <p>Trước đây {@code setHp()} chỉ làm đúng một việc: {@code hp > hpMax} thì
     * kéo {@code hp} xuống bằng {@code hpMax}. Nghe thì hợp lý, nhưng nó là một
     * <b>bánh cóc</b> — chỉ quay được một chiều.</p>
     *
     * <p>{@code hpMax} không cố định: nó dựng lại từ đầu ở mỗi lần gọi, cộng dồn
     * trang bị, set, thẻ bài, bùa, và cả hệ số riêng của bản đồ. Chỉ cần một lần
     * gọi rơi vào lúc trang bị chưa nạp xong — đổi bản đồ, vừa đăng nhập, vừa
     * mặc/cởi một món — là {@code hpMax} tụt về gần {@code hpg} trong khoảnh
     * khắc ấy, {@code hp} bị kéo theo, rồi {@code hpMax} trở lại 330 triệu mà
     * {@code hp} <b>nằm nguyên dưới đáy</b>.</p>
     *
     * <p>Đó là lý do "hồi kiểu gì cũng không đầy": không phải các cách hồi máu
     * hỏng, mà là cái trần bị hạ xuống rồi nâng lên trong khi máu không được
     * nâng theo. Về nhà thì có đường hồi đầy riêng nên trông như chỉ ở nhà mới
     * đúng.</p>
     *
     * <p>Nay ghi lại tỉ lệ máu <b>trước</b> khi dựng lại, và sau khi có trần mới
     * thì đặt máu về đúng tỉ lệ đó. Trần tụt thật (bản đồ 5000 năm trước, Cereal
     * với Xayda) thì máu vẫn tụt theo đúng tỉ lệ — không ai được hồi máu chùa;
     * trần trở lại thì máu cũng trở lại.</p>
     */
    public void setBasePoint() {
        // Chup ti le TRUOC khi dung lai tran.
        //
        // Chi lam khi duoc goi TRUC TIEP (thu cung, Duong Tang) — duong day du
        // la setPointWhenWearClothes() da tu chup va tra o hai dau cua ca hai
        // chang, va chup them mot lan o giua chinh la loi cu: no so tran cu DA
        // CO SET voi tran moi CHUA CO SET roi ha mau theo phan lech ay.
        final boolean tuChup = !this.dangDungLaiDayDu;
        final long hpMaxCu = this.hpMax;
        final long mpMaxCu = this.mpMax;
        final long hpCu = this.hp;
        final long mpCu = this.mp;

        setHpMax();
        setMpMax();

        if (tuChup) {
            capNhatTheoTiLe(hpMaxCu, hpCu, mpMaxCu, mpCu);
            setHp();
            setMp();
        }
        setDame();
        setDef();
        setCrit();
        setCritDame();
        setHpHoi();
        setMpHoi();
        setHutHp();
        setHutMp();
        setThoBulma();
        setTiecbaiBien();
        setBunmaTocMau();
        setTinhNhatNguyetAn();
        setSpeed();
        setOptions();
    }

    private void setThoBulma() {
        this.isThoBulma = (this.player.inventory != null && this.player.inventory.itemsBody != null && this.player.inventory.itemsBody.size() >= 5
                && this.player.inventory.itemsBody.get(5).isNotNullItem() && this.player.inventory.itemsBody.get(5).template.id == 584);
    }

    private void setBunmaTocMau() {
        this.isBunmaTocMau = (this.player.inventory != null && this.player.inventory.itemsBody != null && this.player.inventory.itemsBody.size() >= 5
                && this.player.inventory.itemsBody.get(5).isNotNullItem() && this.player.inventory.itemsBody.get(5).template.id >= 1208
                && this.player.inventory.itemsBody.get(5).template.id <= 1210);
    }

    private void setTiecbaiBien() {
        this.isTiecBaiBien = (this.player.inventory != null && this.player.inventory.itemsBody != null && this.player.inventory.itemsBody.size() >= 5
                && this.player.inventory.itemsBody.get(5).isNotNullItem() && this.player.inventory.itemsBody.get(5).template.id >= 1234
                && this.player.inventory.itemsBody.get(5).template.id <= 1236);
    }

    private void setTinhNhatNguyetAn() {
        this.isTinhAn = this.setTinhAn >= 5;
        this.isNhatAn = this.setNhatAn >= 5;
        this.isNguyetAn = this.setNguyetAn >= 5;
    }

    public int getPlayerRank(List<Player> list, Player player) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == player.id) {
                return i + 1;
            }
        }
        return -1;
    }

    private void setHpHoi() {
        this.hpHoi = this.hpMax / 100;
        this.hpHoi += this.hpHoiAdd;

        // Kiểm tra giá trị tlHpHoi không vượt quá giới hạn
        if (this.tlHpHoi > 100) {
            this.tlHpHoi = 100;
        } else if (this.tlHpHoi < 0) {
            this.tlHpHoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoi / 100);

        // Kiểm tra giá trị tlHpHoiBanThanVaDongDoi không vượt quá giới hạn
        if (this.tlHpHoiBanThanVaDongDoi > 100) {
            this.tlHpHoiBanThanVaDongDoi = 100;
        } else if (this.tlHpHoiBanThanVaDongDoi < 0) {
            this.tlHpHoiBanThanVaDongDoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoiBanThanVaDongDoi / 100);

        if (this.player.itemTime != null && this.player.itemTime.Isthuocmothuong) {
            this.hpHoi += calPercent(this.hpMax, 10);
        }
        if (this.player.itemTime != null && this.player.itemTime.Isthuocmodacbiet) {
            this.hpHoi += calPercent(this.hpMax, 10);
        }
        if (this.player.setClothes != null && this.player.setClothes.ctNezuko != -1) {
            this.hpHoi += calPercent(this.hpMax, 3);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHoiSieuCap) {
            this.hpHoi += calPercent(this.hpMax, 100);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC3) {
            this.hpHoi += calPercent(this.hpMax, 10);
        }
    }

    private void setMpHoi() {
        this.mpHoi = this.mpMax / 100;
        this.mpHoi += this.mpHoiAdd;

        // Kiểm tra giá trị tlMpHoi không vượt quá giới hạn
        if (this.tlMpHoi > 100) {
            this.tlMpHoi = 100;
        } else if (this.tlMpHoi < 0) {
            this.tlMpHoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoi / 100);

        // Kiểm tra giá trị tlMpHoiBanThanVaDongDoi không vượt quá giới hạn
        if (this.tlMpHoiBanThanVaDongDoi > 100) {
            this.tlMpHoiBanThanVaDongDoi = 100;
        } else if (this.tlMpHoiBanThanVaDongDoi < 0) {
            this.tlMpHoiBanThanVaDongDoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoiBanThanVaDongDoi / 100);

        if (this.player.itemTime != null && this.player.itemTime.Isthuocmothuong) {
            this.mpHoi += calPercent(this.mpMax, 10);
        }
        if (this.player.itemTime != null && this.player.itemTime.Isthuocmodacbiet) {
            this.mpHoi += calPercent(this.mpMax, 10);
        }
        if (this.player.setClothes != null && this.player.setClothes.ctNezuko != -1) {
            this.mpHoi += calPercent(this.mpMax, 3);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHoiSieuCap) {
            this.mpHoi += calPercent(this.mpMax, 100);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC3) {
            this.mpHoi += calPercent(this.mpMax, 10);
        }
    }

    private void setHpMax() {
        if (nhatKyHp != null) {
            // setHpMax co the chay hai lan trong mot luot — chi giu luot cuoi.
            nhatKyHp.clear();
            hpTruocBuoc = 0;
        }
        // Tính toán giới hạn hpMax
        long hpMax = this.hpg;
        ghiHp("HP gốc (hpg)", hpMax, "Chỉ số HP gốc đang lưu trên nhân vật.");
        long hpAddDaGhi = 0;
        for (NguonChiSo n : this.nguonHpAdd) {
            hpMax += n.giaTri;
            hpAddDaGhi += n.giaTri;
            ghiHp("Trang bị cộng thẳng HP", hpMax, n.moTa());
        }
        long hpAddChuaRo = this.hpAdd - hpAddDaGhi;
        if (hpAddChuaRo != 0) {
            hpMax += hpAddChuaRo;
            ghiHp("HP cộng thẳng khác", hpMax,
                    "Có nguồn cộng vào hpAdd nhưng chưa truyền mô tả nguồn.");
        }
//
//        for (int tl : new ArrayList<>(this.tlHp)) {
//            hpMax += (hpMax * tl / 100L);
//        }
        // Moi dong % deu noi ro NO TU MON NAO.
        //
        // `tlHp` chi giu con so, con `nguonHpPct` giu kem ten mon va ten dong
        // chi so. Hai danh sach duoc them cung mot cho nen khop nhau theo chi
        // so; lech nhau thi van ghep duoc bao nhieu hay bay nhieu, phan con lai
        // ghi la chua ro nguon — hon la ca cum mat ten mon nhu ban truoc.
        for (int i = 0; i < this.tlHp.size(); i++) {
            Integer tl = this.tlHp.get(i);
            if (tl == null) {
                continue;
            }
            hpMax += (hpMax * tl / 100L);
            NguonChiSo n = (i < this.nguonHpPct.size())
                    ? this.nguonHpPct.get(i) : null;
            ghiHp("Trang bị +" + tl + "% HP", hpMax,
                    (n == null ? NGUON_KHONG_RO : n.moTa())
                    + " | Tính trên HP sau các dòng trước.");
        }
         // Tinh ấn
        if (hasFull5TinhAn()) {
            hpMax += calPercent(hpMax, 15);
            ghiHp("Đủ 5 Tinh ấn (+15%)", hpMax);
        }

        if (this.player.isPl()) {
            if (InventoryService.gI().findItemRongNhi(this.player)) {
                hpMax += calPercent(hpMax, 1);
                ghiHp("Rồng nhí trong hành trang (+1%)", hpMax);
            }
            hpMax += calPercent(hpMax, InventoryService.gI().HpItemsInBoxCollection(this.player));
            ghiHp("Bộ sưu tập (% HP)", hpMax);
        }

        if (this.player.tlHpClanAdd > 0) {
            hpMax += calPercent(hpMax, this.player.tlHpClanAdd);
            ghiHp("Bang hội cộng % HP", hpMax);
        }
        if (this.player.isPhanThan) {
            hpMax = calPercent(((PhanThan) this.player).master.nPoint.hpMax, SkillUtil.getPercentPhanThan(player));
            ghiHp("Phân thân (theo HP chủ)", hpMax);
        }
        if (this.player.THE_TUAN == 1 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 3);
            ghiHp("Thẻ tuần (+3%)", hpMax);
        }
        if (this.player.THE_TUAN == 2 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 5);
            ghiHp("Thẻ tuần cao cấp (+5%)", hpMax);
        }
        if (this.player.THE_THANG == 1 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 7);
            ghiHp("Thẻ tháng (+7%)", hpMax);
        }
        if (this.player.THE_THANG == 2 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 10);
            ghiHp("Thẻ tháng cao cấp (+10%)", hpMax);
        }
        if (this.player.THE_NAM == 1 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 15);
            ghiHp("Thẻ năm (+15%)", hpMax);
        }
        if (this.player.THE_NAM == 2 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 18);
            ghiHp("Thẻ năm cao cấp (+18%)", hpMax);
        }
        if (this.player.THE_CHI_TON == 1 && this.player.LASTTIME_THE_CHI_TON > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 20);
            ghiHp("Thẻ chí tôn (+20%)", hpMax);
        }
        if (this.player.isPl() && this.player.isUseDanhHieu_ThienTu == true && this.player.LastTimeDanhHieu_ThienTu > 0) {
            hpMax += calPercent(hpMax, 5);
            ghiHp("Danh hiệu Thiên Tử (+5%)", hpMax);
        }


        // Xử lý set nappa


        if (this.player.effectSkill != null && this.player.effectSkill.isVirus) {
            hpMax -= calPercent(hpMax, 10);
            ghiHp("Nhiễm virus (−10%)", hpMax);
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isBongTuyet) {
            hpMax -= calPercent(hpMax, 20);
            ghiHp("Dính bông tuyết (−20%)", hpMax);
        }

        //set worldcup

        if (this.player.itemTime != null && this.player.itemTime.isUseRocket1h) {
            hpMax += calPercent(hpMax, 20);
            ghiHp("Tên lửa 1 giờ (+20%)", hpMax);
        }

        if (this.player.itemTime != null && this.player.itemTime.isRongXuong_2) {
            hpMax += calPercent(hpMax, 15);
            ghiHp("Rồng xương 2 (+15%)", hpMax);
        }

        if (this.player.effectSkill.isMonkey) {
            if (!laDeDangHopThe()) {
                int percent = SkillUtil.getPercentHpMonkey(player.effectSkill.levelMonkey);
                // Hieu luc chieu Bien Khi (skill_power_pct).
                percent += percent * nro.repository.dao.SetBonusDAO.tongTheoChieu(
                        this.player, "skill_power_pct", Skill.BIEN_KHI) / 100;
                hpMax += (hpMax * percent / 100);
                ghiHp("Biến khỉ", hpMax);
            }
        }

        if (this.player.isPlMan()) {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.HP);
            if (at != null && !at.isExpired()) {
                hpMax += calPercent(hpMax, at.getValue());
                ghiHp("Sự kiện máy chủ: % HP", hpMax);
            }
        }

        //phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            hpMax *= this.player.effectSkin.xHPKI;
            ghiHp("Phù trong Ngọc rồng đen", hpMax);
        }

        // HP của đệ tử khi hợp thể KHÔNG cộng ở đây nữa — xem congHpDeTuKhiHopThe().
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            hpMax += calPercent(hpMax, 5);
            ghiHp("Hợp thể bông tai cấp 2 (+5%)", hpMax);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            hpMax += calPercent(hpMax, 10);
            ghiHp("Hợp thể bông tai cấp 3 (+10%)", hpMax);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
            hpMax += calPercent(hpMax, 15);
            ghiHp("Hợp thể bông tai cấp 4 (+15%)", hpMax);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5) {
            hpMax += calPercent(hpMax, 20);
            ghiHp("Hợp thể bông tai cấp 5 (+20%)", hpMax);
        }
        //huýt sáo
        if (!laDeDangHopThe()) {
            if (this.player.effectSkill.tiLeHPHuytSao != 0) {
                hpMax += (hpMax * this.player.effectSkill.tiLeHPHuytSao / 100L);
                ghiHp("Huýt sáo", hpMax);
            }
        }
        //bổ huyết
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet) {
            hpMax *= 2;
            ghiHp("Bổ huyết (×2)", hpMax);
        }

        // Xử lý chibi
        if (this.player.effectSkill != null && this.player.effectSkill.isChibi && this.player.typeChibi == 3) {
            hpMax *= 2;
            ghiHp("Chibi loại 3 (×2)", hpMax);
        }
        if (player.getBuff() == Buff.BUFF_HP) {
            hpMax += calPercent(hpMax, 20);
            ghiHp("Buff HP (+20%)", hpMax);
        }
        // Top Whis KHONG con cong chi so: gio thuong thoi vang moi ngay,
        // xem TopWhisDAO.phatNeuCan.

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            hpMax += 1_000_000;
            ghiHp("Phù map Mabu (+1 triệu HP)", hpMax);
        }
        //giảm hp
        hpMax -= (hpMax * tlSubHP / 100);
        ghiHp("Giảm HP (dòng giảm HP trên đồ)", hpMax);
        //hồng đào
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao0) {
            hpMax -= calPercent(hpMax, 99);
            ghiHp("Hồng đào 0 (−99%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao) {
            hpMax += calPercent(hpMax, 1);
            ghiHp("Hồng đào (+1%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao1) {
            hpMax += calPercent(hpMax, 2);
            ghiHp("Hồng đào 1 (+2%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao3) {
            hpMax += calPercent(hpMax, 3);
            ghiHp("Hồng đào 3 (+3%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao5) {
            hpMax += calPercent(hpMax, 5);
            ghiHp("Hồng đào 5 (+5%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao10) {
            hpMax += calPercent(hpMax, 8);
            ghiHp("Hồng đào 10 (+8%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao25) {
            hpMax += calPercent(hpMax, 12);
            ghiHp("Hồng đào 25 (+12%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao50) {
            hpMax += calPercent(hpMax, 15);
            ghiHp("Hồng đào 50 (+15%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao99) {
            hpMax += calPercent(hpMax, 20);
            ghiHp("Hồng đào 99 (+20%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao999) {
            hpMax += calPercent(hpMax, 100);
            ghiHp("Hồng đào 999 (+100%)", hpMax);
        }

        if (this.player.itemTime != null && this.player.itemTime.istrbhp) {
            hpMax += calPercent(hpMax, 30);
            ghiHp("Vật phẩm tăng HP 1 giờ (+30%)", hpMax);
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbhpxd) {
            hpMax += calPercent(hpMax, 15);
            ghiHp("Vật phẩm tăng HP 30 phút (+15%)", hpMax);
        }

        // Xử lý ngọc rồng đen 2 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            hpMax += (hpMax * RewardBlackBall.R2S_1 / 100);
            ghiHp("Thưởng ngọc rồng đen 2 sao", hpMax);
        }

        // item sieu cawsp
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2.2;
            ghiHp("Bổ huyết 2 (×2,2)", hpMax);
        }
        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            hpMax /= 2;
            ghiHp("Hành tinh lạnh, không kháng lạnh (÷2)", hpMax);
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapChristMasEvent(this.player.zone.map.mapId) && !this.isKhongLanh) {
            hpMax /= 2;
            ghiHp("Bản đồ Giáng sinh, không kháng lạnh (÷2)", hpMax);
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMap5000NamTruoc(this.player.zone.map.mapId)) {
            hpMax -= calPercent(hpMax, 90);
            ghiHp("Bản đồ 5000 năm trước (−90%)", hpMax);
        }

        if (player.gender == ConstPlayer.XAYDA) {
            if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCereal(this.player.zone.map)) {
                hpMax /= 2;
                ghiHp("Bản đồ Cereal, người Xayda (÷2)", hpMax);
            }
        }

        if (this.player.itemTime != null && this.player.itemTime.IsSupbihacam) {
            hpMax += calPercent(hpMax, 10);
            ghiHp("Súp bí hắc ám (+10%)", hpMax);
        }

        if (this.player.itemTime != null && this.player.itemTime.istomtambot) {
            hpMax += calPercent(hpMax, 5);
            ghiHp("Tôm tẩm bột (+5%)", hpMax);
        }
        this.hpMax = hpMax;
    }

    /**
     * Giữ máu và khí theo đúng <b>tỉ lệ</b> khi trần vừa đổi.
     *
     * <p>Trần y nguyên thì phép nhân chia ở đây trả về đúng con số cũ, không hơn
     * không kém — nên vẫn gọi, để dựng lại phần máu mà một chặng giữa chừng có
     * thể đã kéo xuống.</p>
     *
     * <p>Người đang chết thì để yên: máu bằng 0 là trạng thái, không phải một tỉ
     * lệ cần giữ.</p>
     */
    private void capNhatTheoTiLe(long hpMaxCu, long hpCu, long mpMaxCu, long mpCu) {
        // Khong doi hoi tran phai THAY DOI moi lam.
        //
        // Tran khong doi ma van phai dat lai: mau co the da bi mot chang giua
        // chung keo xuong, va khi hai tran bang nhau thi phep nhan chia nay tra
        // ve dung hpCu — tuc la dung mau luc dau, khong hon khong kem.
        if (hpMaxCu > 0 && hpCu > 0) {
            // Nhan truoc chia sau, va nhan bang so nguyen 128 bit de khong tran:
            // hpCu va hpMax deu co the lon hon hai ti.
            this.hp = java.math.BigInteger.valueOf(hpCu)
                    .multiply(java.math.BigInteger.valueOf(this.hpMax))
                    .divide(java.math.BigInteger.valueOf(hpMaxCu))
                    .longValue();
            if (this.hp < 1) {
                this.hp = 1;      // dang song thi khong duoc rot ve 0 vi lam tron
            }
        }
        if (mpMaxCu > 0 && mpCu > 0) {
            this.mp = java.math.BigInteger.valueOf(mpCu)
                    .multiply(java.math.BigInteger.valueOf(this.mpMax))
                    .divide(java.math.BigInteger.valueOf(mpMaxCu))
                    .longValue();
            if (this.mp < 0) {
                this.mp = 0;
            }
        }
    }

    // (hp sư phụ + hp đệ tử ) + 15%
    // (hp sư phụ + 15% +hp đệ tử)
    private void setHp() {
        if (this.hp > this.hpMax) {
            this.hp = this.hpMax;
        }
    }

    private void setMpMax() {
        if (nhatKyMp != null) {
            nhatKyMp.clear();
            mpTruocBuoc = 0;
        }
        // Tính toán giới hạn mpMax
        long mpMax = this.mpg;
        ghiMp("KI gốc (mpg)", mpMax, "Chỉ số KI gốc đang lưu trên nhân vật.");
        long mpAddDaGhi = 0;
        for (NguonChiSo n : this.nguonMpAdd) {
            mpMax += n.giaTri;
            mpAddDaGhi += n.giaTri;
            ghiMp("Trang bị cộng thẳng KI", mpMax, n.moTa());
        }
        long mpAddChuaRo = this.mpAdd - mpAddDaGhi;
        if (mpAddChuaRo != 0) {
            mpMax += mpAddChuaRo;
            ghiMp("KI cộng thẳng khác", mpMax,
                    "Có nguồn cộng vào mpAdd nhưng chưa truyền mô tả nguồn.");
        }

        // Áp dụng các yếu tố ảnh hưởng đến mpMax
//        for (Integer tl : this.tlMp) {
//            mpMax += (mpMax * tl / 100L);
//        }
        // Xem chu thich o phan HP: moi dong % noi ro no tu mon nao.
        for (int i = 0; i < this.tlMp.size(); i++) {
            Integer tl = this.tlMp.get(i);
            if (tl == null) {
                continue;
            }
            mpMax += (mpMax * tl / 100L);
            NguonChiSo n = (i < this.nguonMpPct.size())
                    ? this.nguonMpPct.get(i) : null;
            ghiMp("Trang bị +" + tl + "% KI", mpMax,
                    (n == null ? NGUON_KHONG_RO : n.moTa())
                    + " | Tính trên KI sau các dòng trước.");
        }
// nhật ấn
if (hasFull5NhatAn()) {
    mpMax += calPercent(mpMax, 15);
    ghiMp("Nhật Ấn: đủ 5 món +15% KI", mpMax,
                    "hasFull5NhatAn() — đang mặc đủ 5 món mang ấn Nhật.");
}



        // Xử lý set picolo

        if (this.player.isPl()) {
            if (InventoryService.gI().findItemRongNhi(this.player)) {
                mpMax += calPercent(mpMax, 1);
                ghiMp("Rồng nhí trong hành trang +1% KI", mpMax,
                    "InventoryService.findItemRongNhi(player) — có Rồng nhí trong hành trang.");
            }
            mpMax += calPercent(mpMax, InventoryService.gI().MpItemsInBoxCollection(this.player));
            ghiMp("Bộ sưu tập: % KI", mpMax,
                    "Tổng % KI của các thẻ đang có trong Sổ sưu tầm — InventoryService.MpItemsInBoxCollection(player).");
        }

        if (this.player.tlMpClanAdd > 0) {
            mpMax += calPercent(mpMax, this.player.tlMpClanAdd);
            ghiMp("Bang hội cộng % KI", mpMax,
                    "player.tlMpClanAdd — phần trăm KI do cấp bang hội cộng cho mọi thành viên.");
        }
        if (this.player.isPhanThan) {
            mpMax = calPercent(((PhanThan) this.player).master.nPoint.mpMax, SkillUtil.getPercentPhanThan(player));
            ghiMp("Phân thân: lấy % KI của sư phụ", mpMax,
                    "Phân thân không có KI riêng: lấy KI tối đa của sư phụ nhân với SkillUtil.getPercentPhanThan(player).");
        }
        if (this.player.THE_TUAN == 1 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 3);
            ghiMp("Thẻ tuần +3% KI", mpMax,
                    "THE_TUAN = 1 và LASTTIME_THE_TUAN còn hạn.");
        }
        if (this.player.THE_TUAN == 2 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 5);
            ghiMp("Thẻ tuần cao cấp +5% KI", mpMax,
                    "THE_TUAN = 2 và LASTTIME_THE_TUAN còn hạn.");
        }
        if (this.player.THE_THANG == 1 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 7);
            ghiMp("Thẻ tháng +7% KI", mpMax,
                    "THE_THANG = 1 và LASTTIME_THE_THANG còn hạn.");
        }
        if (this.player.THE_THANG == 2 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 10);
            ghiMp("Thẻ tháng cao cấp +10% KI", mpMax,
                    "THE_THANG = 2 và LASTTIME_THE_THANG còn hạn.");
        }
        if (this.player.THE_NAM == 1 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 15);
            ghiMp("Thẻ năm +15% KI", mpMax,
                    "THE_NAM = 1 và LASTTIME_THE_NAM còn hạn.");
        }
        if (this.player.THE_NAM == 2 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 18);
            ghiMp("Thẻ năm cao cấp +18% KI", mpMax,
                    "THE_NAM = 2 và LASTTIME_THE_NAM còn hạn.");
        }
        if (this.player.THE_CHI_TON == 1 && this.player.LASTTIME_THE_CHI_TON > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 20);
            ghiMp("Thẻ chí tôn +20% KI", mpMax,
                    "THE_CHI_TON = 1 và LASTTIME_THE_CHI_TON còn hạn.");
        }
        if (this.player.isPl() && this.player.isUseDanhHieu_ThienTu == true && this.player.LastTimeDanhHieu_ThienTu > 0) {
            mpMax += calPercent(mpMax, 5);
            ghiMp("Danh hiệu Thiên Tử +5% KI", mpMax,
                    "Đang đeo danh hiệu Thiên Tử và danh hiệu còn hạn.");
        }
        if (player.getBuff() == Buff.BUFF_KI) {
            mpMax += calPercent(mpMax, 20);
            ghiMp("Buff KI +20% KI", mpMax,
                    "player.getBuff() == Buff.BUFF_KI — đang nhận buff KI.");
        }


        if (this.player.itemTime != null && this.player.itemTime.isRongXuong_2) {
            mpMax += calPercent(mpMax, 15);
            ghiMp("Rồng xương cấp 2 +15% KI", mpMax,
                    "itemTime.isRongXuong_2 đang bật.");
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isBongTuyet) {
            mpMax -= calPercent(mpMax, 20);
            ghiMp("Dính bông tuyết −20% KI", mpMax,
                    "effectSkill.isBongTuyet — đang dính bông tuyết.");
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            mpMax += 1_000_000;
            ghiMp("Phù map Mabu +1.000.000 KI", mpMax,
                    "player.isPhuHoMapMabu đang bật. Đây là cộng THẲNG, không phải phần trăm.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseRocket1h) {
            mpMax += calPercent(mpMax, 20);
            ghiMp("Tên lửa 1 giờ +20% KI", mpMax,
                    "itemTime.isUseRocket1h đang bật.");
        }

        // Xử lý ngọc rồng đen 6 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[5] > System.currentTimeMillis()) {
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100);
            ghiMp("Ngọc rồng đen 6 sao (lần 1)", mpMax,
                    "Thưởng ngọc rồng đen 6 sao còn hạn — cộng RewardBlackBall.R6S_1%.");
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100L);
            ghiMp("Ngọc rồng đen 6 sao (lần 2)", mpMax,
                    "Cùng thưởng ấy được cộng LẦN NỮA trên giá trị vừa tính — đúng như mã đang chạy.");
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isVirus) {
            mpMax -= calPercent(mpMax, 10);
            ghiMp("Nhiễm virus −10% KI", mpMax,
                    "effectSkill.isVirus — đang nhiễm virus.");
        }

        //set worldcup

         if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 1) {
                mpMax += this.player.Detu.nPoint.mpMax *  20 / 100L;
                ghiMp("Đệ tử loại 1 khi hợp thể (+20% của đệ)", mpMax);
            }
        }
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 5) {
                mpMax += this.player.Detu.nPoint.mpMax * 40/ 100L;
                ghiMp("Đệ tử loại 5 khi hợp thể (+40% của đệ)", mpMax);
            }
        }

        //hợp thể
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            mpMax += this.player.Detu.nPoint.mpMax;
            ghiMp("Hợp thể: cộng KI của đệ tử", mpMax,
                    "Đang hợp thể: cộng NGUYÊN KI tối đa của đệ tử vào KI sư phụ.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            mpMax += calPercent(mpMax, 5);
            ghiMp("Hợp thể bông tai cấp 2 +5% KI", mpMax,
                    "fusion.typeFusion == HOP_THE_PORATA2.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            mpMax += calPercent(mpMax, 10);
            ghiMp("Hợp thể bông tai cấp 3 +10% KI", mpMax,
                    "fusion.typeFusion == HOP_THE_PORATA3.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
            mpMax += calPercent(mpMax, 15);
            ghiMp("Hợp thể bông tai cấp 4 +15% KI", mpMax,
                    "fusion.typeFusion == HOP_THE_PORATA4.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5) {
            mpMax += calPercent(mpMax, 20);
            ghiMp("Hợp thể bông tai cấp 5 +20% KI", mpMax,
                    "fusion.typeFusion == HOP_THE_PORATA5.");
        }
        //bổ khí
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi) {
            mpMax *= 2;
            ghiMp("Bổ khí ×2 KI", mpMax,
                    "itemTime.isUseBoKhi đang bật.");
        }
        if (this.player.isPlMan()) {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.KI);
            if (at != null && !at.isExpired()) {
                mpMax += calPercent(mpMax, at.getValue());
                ghiMp("Sự kiện máy chủ: % KI", mpMax,
                    "Attribute KI của máy chủ còn hiệu lực — cộng theo at.getValue()%, áp cho mọi người chơi.");
            }
        }
        // Top Whis KHONG con cong chi so: gio thuong thoi vang moi ngay,
        // xem TopWhisDAO.phatNeuCan.

        //hồng đào
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao0) {
            mpMax -= calPercent(mpMax, 99);
            ghiMp("Hồng đào 0 −99% KI", mpMax,
                    "itemTime.isUseHongDao0 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao) {
            mpMax += calPercent(mpMax, 1);
            ghiMp("Hồng đào +1% KI", mpMax,
                    "itemTime.isUseHongDao đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao1) {
            mpMax += calPercent(mpMax, 2);
            ghiMp("Hồng đào 1 +2% KI", mpMax,
                    "itemTime.isUseHongDao1 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao3) {
            mpMax += calPercent(mpMax, 3);
            ghiMp("Hồng đào 3 +3% KI", mpMax,
                    "itemTime.isUseHongDao3 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao5) {
            mpMax += calPercent(mpMax, 5);
            ghiMp("Hồng đào 5 +5% KI", mpMax,
                    "itemTime.isUseHongDao5 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao10) {
            mpMax += calPercent(mpMax, 8);
            ghiMp("Hồng đào 10 +8% KI", mpMax,
                    "itemTime.isUseHongDao10 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao25) {
            mpMax += calPercent(mpMax, 12);
            ghiMp("Hồng đào 25 +12% KI", mpMax,
                    "itemTime.isUseHongDao25 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao50) {
            mpMax += calPercent(mpMax, 15);
            ghiMp("Hồng đào 50 +15% KI", mpMax,
                    "itemTime.isUseHongDao50 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao99) {
            mpMax += calPercent(mpMax, 20);
            ghiMp("Hồng đào 99 +20% KI", mpMax,
                    "itemTime.isUseHongDao99 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao999) {
            mpMax += calPercent(mpMax, 100);
            ghiMp("Hồng đào 999 +100% KI", mpMax,
                    "itemTime.isUseHongDao999 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2.2;
            ghiMp("Bổ khí 2 (×2,2 KI)", mpMax,
                    "itemTime.isUseBoKhi2 đang bật.");
        }
        //giảm mp
        mpMax -= (mpMax * tlSubMP / 100);
        ghiMp("Giảm KI (dòng giảm KI trên đồ)", mpMax,
                    "Trừ theo tlSubMP — tổng phần trăm giảm KI của các dòng trên trang bị.");

        if (this.player.itemTime != null && this.player.itemTime.istrbki) {
            mpMax += calPercent(mpMax, 30);
            ghiMp("Trân bảo KI +30% KI", mpMax,
                    "itemTime.istrbki đang bật (vật phẩm thời gian, biểu tượng 24362).");
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbkixd) {
            mpMax += calPercent(mpMax, 15);
            ghiMp("Trân bảo KI (bản xd) +15% KI", mpMax,
                    "itemTime.istrbkixd đang bật (vật phẩm thời gian, biểu tượng 24362).");
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            mpMax /= 2;
            ghiMp("Hành tinh lạnh, không kháng lạnh (÷2)", mpMax,
                    "Đang ở bản đồ lạnh mà không có dòng kháng lạnh: KI tối đa còn một nửa.");
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapChristMasEvent(this.player.zone.map.mapId) && !this.isKhongLanh) {
            mpMax /= 2;
            ghiMp("Bản đồ Giáng sinh, không kháng lạnh (÷2)", mpMax,
                    "Đang ở bản đồ sự kiện Giáng sinh mà không có dòng kháng lạnh.");
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMap5000NamTruoc(this.player.zone.map.mapId)) {
            mpMax -= calPercent(mpMax, 90);
            ghiMp("Bản đồ 5000 năm trước (−90% KI)", mpMax,
                    "Đang ở bản đồ 5000 năm trước.");
        }

        if (player.gender == ConstPlayer.XAYDA) {
            if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCereal(this.player.zone.map)) {
                mpMax /= 2;
                ghiMp("Bản đồ Cereal, người Xayda (÷2)", mpMax,
                    "Người Xayda đứng ở bản đồ Cereal: KI tối đa còn một nửa.");
            }
        }

        //phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            mpMax *= this.player.effectSkin.xHPKI;
            ghiMp("Map Ngọc Rồng Đen: nhân phù", mpMax,
                    "Nhân theo effectSkin.xHPKI của phù đang dùng trong Black Ball War.");
        }
        //xiên cá
        if (this.player.effectFlagBag.useXienCa) {
            mpMax += calPercent(mpMax, 15);
            ghiMp("Xiên cá +15% KI", mpMax,
                    "effectFlagBag.useXienCa — đang dùng xiên cá.");
        }
        this.mpMax = mpMax;
    }

    private void setMp() {
        if (this.mp > this.mpMax) {
            this.mp = this.mpMax;
        }
    }

    public long getHP() {
        return this.hp <= this.hpMax ? this.hp : this.hpMax;
    }

    public void setHP(long hp) {
        if (hp > 0) {
            this.hp = (hp <= this.hpMax ? hp : this.hpMax);
        } else {
            player.setDie();
        }
    }

    public long getMP() {
        return this.mp <= this.mpMax ? this.mp : this.mpMax;
    }

    public void setMP(long mp) {
        if (mp > 0) {
            this.mp = (mp <= this.mpMax ? mp : this.mpMax);
        } else {
            this.mp = 0;
        }
    }

    private void setDame() {
        if (nhatKySd != null) {
            nhatKySd.clear();
            sdTruocBuoc = 0;
        }
        long dame = this.dameg;
        ghiSd("Sức đánh gốc (dameg)", dame,
                "Chỉ số sức đánh gốc đang lưu trên nhân vật.");
        long sdAddDaGhi = 0;
        for (NguonChiSo n : this.nguonSdAdd) {
            dame += n.giaTri;
            sdAddDaGhi += n.giaTri;
            ghiSd("Trang bị cộng thẳng sức đánh", dame, n.moTa());
        }
        long sdAddChuaRo = this.dameAdd - sdAddDaGhi;
        if (sdAddChuaRo != 0) {
            dame += sdAddChuaRo;
            ghiSd("Sức đánh cộng thẳng khác", dame,
                    "Có nguồn cộng vào dameAdd nhưng chưa truyền mô tả nguồn.");
        }

//        for (Integer tl : this.tlDame) {
//            dame += (dame * tl / 100L);
//        }
        // Xem chu thich o phan HP: moi dong % noi ro no tu mon nao.
        for (int i = 0; i < this.tlDame.size(); i++) {
            Integer tl = this.tlDame.get(i);
            if (tl == null) {
                continue;
            }
            dame += (dame * tl / 100L);
            NguonChiSo n = (i < this.nguonSdPct.size())
                    ? this.nguonSdPct.get(i) : null;
            ghiSd("Trang bị +" + tl + "% sức đánh", dame,
                    (n == null ? NGUON_KHONG_RO : n.moTa())
                    + " | Tính trên sức đánh sau các dòng trước.");
        }
        // Nguyệt Ấn: đủ 5 món +15% Sức đánh
        if (hasFull5NguyetAn()) {
            dame += calPercent(dame, 15);
            ghiSd("Nguyệt Ấn: đủ 5 món +15% Sức đánh (hasFull5NguyetAn())", dame);
        }

        if (this.player.isPl()) {
            if (InventoryService.gI().findItemRongNhi(this.player)) {
                dame += calPercent(dame, 1);
                ghiSd("Rồng nhí trong hành trang (+1% sức đánh)", dame,
                        "InventoryService.findItemRongNhi(player) trả về true.");
            }
            int pctBox = InventoryService.gI().DamageItemsInBoxCollection(this.player);
            if (pctBox != 0) {
                dame += calPercent(dame, pctBox);
                ghiSd("Bộ sưu tập +" + pctBox + "% sức đánh", dame,
                        "Tính theo số vật phẩm trong rương sưu tầm.");
            }
        }


        if (this.player.isPhanThan) {
            dame = calPercent(((PhanThan) this.player).master.nPoint.dame, SkillUtil.getPercentPhanThan(player));
            ghiSd("Phân thân: lấy % sức đánh sư phụ", dame,
                    "SkillUtil.getPercentPhanThan(player) quyết định phân thân nhận bao nhiêu % từ master.");
        }
        if (this.player.THE_TUAN == 1 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            dame += calPercent(dame, 3);
            ghiSd("Thẻ tuần +3% sức đánh", dame,
                    "THE_TUAN=1 và LASTTIME_THE_TUAN còn hạn.");
        }
        if (this.player.THE_TUAN == 2 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            dame += calPercent(dame, 5);
            ghiSd("Thẻ tuần cao cấp +5% sức đánh", dame,
                    "THE_TUAN=2 và LASTTIME_THE_TUAN còn hạn.");
        }
        if (this.player.THE_THANG == 1 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            dame += calPercent(dame, 7);
            ghiSd("Thẻ tháng +7% sức đánh", dame,
                    "THE_THANG=1 và LASTTIME_THE_THANG còn hạn.");
        }
        if (this.player.THE_THANG == 2 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            dame += calPercent(dame, 10);
            ghiSd("Thẻ tháng cao cấp +10% sức đánh", dame,
                    "THE_THANG=2 và LASTTIME_THE_THANG còn hạn.");
        }
        if (this.player.THE_NAM == 1 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            dame += calPercent(dame, 15);
            ghiSd("Thẻ năm +15% sức đánh", dame,
                    "THE_NAM=1 và LASTTIME_THE_NAM còn hạn.");
        }
        if (this.player.THE_NAM == 2 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            dame += calPercent(dame, 18);
            ghiSd("Thẻ năm cao cấp +18% sức đánh", dame,
                    "THE_NAM=2 và LASTTIME_THE_NAM còn hạn.");
        }
        if (this.player.THE_CHI_TON == 1 && this.player.LASTTIME_THE_CHI_TON > System.currentTimeMillis()) {
            dame += calPercent(dame, 20);
            ghiSd("Thẻ chí tôn +20% sức đánh", dame,
                    "THE_CHI_TON=1 và LASTTIME_THE_CHI_TON còn hạn.");
        }

        if (this.player.tlDameClanAdd > 0) {
            dame += calPercent(dame, this.player.tlDameClanAdd);
            ghiSd("Bang hội +" + this.player.tlDameClanAdd + "% sức đánh", dame,
                    "player.tlDameClanAdd nhận từ đồng đội/bang hội ở gần.");
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isVirus) {
            dame -= calPercent(dame, 10);
            ghiSd("Virus -10% sức đánh", dame,
                    "effectSkill.isVirus đang bật trên nhân vật.");
        }

        if (tlSexyDame > 0) {
            dame += (dame * tlSexyDame / 100);
            ghiSd("Đẹp +" + tlSexyDame + "% sức đánh", dame,
                    nguonSexyDame == null ? "Nguồn: option 117." : nguonSexyDame.moTa());
        }

        if (tlCoolDame > 0) {
            dame += (dame * tlCoolDame / 100);
            ghiSd("Cool +" + tlCoolDame + "% sức đánh", dame,
                    nguonCoolDame == null ? "Nguồn: option 258." : nguonCoolDame.moTa());
        }

        if (tlCuteAddame > 0) {
            dame += (dame * tlCuteAddame / 100);
            ghiSd("Cute +" + tlCuteAddame + "% sức đánh", dame,
                    nguonCuteDame == null ? "Nguồn: option 226." : nguonCuteDame.moTa());
        }

        if (this.player.isPl() && this.player.isUseDanhHieu_ThienTu == true && this.player.LastTimeDanhHieu_ThienTu > 0) {
            dame += calPercent(dame, 5);
            ghiSd("Danh hiệu Thiên Tử +5% sức đánh", dame,
                    "isUseDanhHieu_ThienTu bật và LastTimeDanhHieu_ThienTu còn hiệu lực.");
        }
        if (this.player.itemTime != null && this.player.itemTime.IsDuoiKhi) {
            dame += calPercent(dame, 10);
            ghiSd("Đuôi khỉ +10% sức đánh", dame,
                    "itemTime.IsDuoiKhi đang bật.");
        }
        if (this.player.getBuff() == Buff.BUFF_ATK) {
            dame += calPercent(dame, 20);
            ghiSd("Buff tấn công +20% sức đánh", dame,
                    "player.getBuff() == Buff.BUFF_ATK.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC1) {
            dame += calPercent(dame, 5);
            ghiSd("Bánh dẻo cấp 1 +5% sức đánh", dame,
                    "itemTime.isUseBanhDeoC1 đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC2) {
            dame += calPercent(dame, 10);
            ghiSd("Bánh dẻo cấp 2 +10% sức đánh", dame,
                    "itemTime.isUseBanhDeoC2 đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC3) {
            dame += calPercent(dame, 15);
            ghiSd("Bánh dẻo cấp 3 +15% sức đánh", dame,
                    "itemTime.isUseBanhDeoC3 đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseTrungThu1Trung) {
            dame += calPercent(dame, 10);
            ghiSd("Trung thu 1 trứng +10% sức đánh", dame,
                    "itemTime.isUseTrungThu1Trung đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseTrungThu2Trung) {
            dame += calPercent(dame, 15);
            ghiSd("Trung thu 2 trứng +15% sức đánh", dame,
                    "itemTime.isUseTrungThu2Trung đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseTrungThuDB) {
            dame += calPercent(dame, 20);
            ghiSd("Trung thu đặc biệt +20% sức đánh", dame,
                    "itemTime.isUseTrungThuDB đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseHBTrungThu) {
            dame += calPercent(dame, 25);
            ghiSd("Hộp bánh trung thu +25% sức đánh", dame,
                    "itemTime.isUseHBTrungThu đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseRocket1h) {
            dame += calPercent(dame, 20);
            ghiSd("Rocket 1 giờ +20% sức đánh", dame,
                    "itemTime.isUseRocket1h đang bật.");
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isBongTuyet) {
            dame -= calPercent(dame, 20);
            ghiSd("Bông tuyết -20% sức đánh", dame,
                    "effectSkill.isBongTuyet đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isRongXuong_2) {
            dame += calPercent(dame, 15);
            ghiSd("Rồng xương cấp 2 +15% sức đánh", dame,
                    "itemTime.isRongXuong_2 đang bật.");
        }

        if (this.player.effectSkin != null && this.player.effectSkin.isThoDaiKa) {
            dame -= calPercent(dame, 15);
            ghiSd("Thỏ đại ca -15% sức đánh", dame,
                    "effectSkin.isThoDaiKa đang bật.");
        }

        //set worldcup

        //thức ăn
        if (!this.player.isDeTu && this.player.itemTime.isEatMeal || this.player.isDeTu && ((Detu) this.player).master.itemTime.isEatMeal) {
            dame += calPercent(dame, 10);
            ghiSd("Thức ăn +10% sức đánh", dame,
                    "Người chơi dùng itemTime.isEatMeal; đệ tử lấy trạng thái thức ăn từ sư phụ.");
        }

        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu >= 5) {
                dame += this.player.Detu.nPoint.dame * this.player.getPointfusion().getDameFusion() / 100L;
                ghiSd("Đệ tử loại 5+ cộng sức đánh theo điểm hợp thể", dame,
                        "Cộng đệ.nPoint.dame × getPointfusion().getDameFusion() / 100 khi đang hợp thể.");
            }
        }

        //hợp thể
         if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 1) {
                dame += this.player.Detu.nPoint.dame *  20 / 100L;
                ghiSd("Đệ tử loại 1 khi hợp thể +20% sức đánh của đệ", dame,
                        "Cộng thêm 20% nPoint.dame của đệ tử loại 1.");
            }
        }
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 5) {
                dame += this.player.Detu.nPoint.dame * 40/ 100L;
                ghiSd("Đệ tử loại 5 khi hợp thể +40% sức đánh của đệ", dame,
                        "Cộng thêm 40% nPoint.dame của đệ tử loại 5.");
            }
        }
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            dame += this.player.Detu.nPoint.dame;
            ghiSd("Hợp thể cộng 100% sức đánh của đệ tử", dame,
                    "Cộng trực tiếp nPoint.dame hiện tại của đệ tử.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            dame += calPercent(dame, 5);
            ghiSd("Hợp thể bông tai cấp 2 +5% sức đánh", dame,
                    "fusion.typeFusion == HOP_THE_PORATA2.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            dame += calPercent(dame, 10);
            ghiSd("Hợp thể bông tai cấp 3 +10% sức đánh", dame,
                    "fusion.typeFusion == HOP_THE_PORATA3.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
            dame += calPercent(dame, 15);
            ghiSd("Hợp thể bông tai cấp 4 +15% sức đánh", dame,
                    "fusion.typeFusion == HOP_THE_PORATA4.");
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5) {
            dame += calPercent(dame, 20);
            ghiSd("Hợp thể bông tai cấp 5 +20% sức đánh", dame,
                    "fusion.typeFusion == HOP_THE_PORATA5.");
        }
        //cuồng nộ
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo) {
            dame *= 2;
            ghiSd("Cường nộ ×2 sức đánh", dame,
                    "itemTime.isUseCuongNo đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao) {
            dame += calPercent(dame, 1);
            ghiSd("Hồng đào +1% sức đánh", dame,
                    "itemTime.isUseHongDao đang bật.");
        }
        // hồng đào
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao0) {
            dame -= calPercent(dame, 99);
            ghiSd("Hồng đào 0 -99% sức đánh", dame,
                    "itemTime.isUseHongDao0 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao1) {
            dame += calPercent(dame, 2);
            ghiSd("Hồng đào 1 +2% sức đánh", dame,
                    "itemTime.isUseHongDao1 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao3) {
            dame += calPercent(dame, 3);
            ghiSd("Hồng đào 3 +3% sức đánh", dame,
                    "itemTime.isUseHongDao3 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao5) {
            dame += calPercent(dame, 5);
            ghiSd("Hồng đào 5 +5% sức đánh", dame,
                    "itemTime.isUseHongDao5 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao10) {
            dame += calPercent(dame, 8);
            ghiSd("Hồng đào 10 +8% sức đánh", dame,
                    "itemTime.isUseHongDao10 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao25) {
            dame += calPercent(dame, 12);
            ghiSd("Hồng đào 25 +12% sức đánh", dame,
                    "itemTime.isUseHongDao25 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao50) {
            dame += calPercent(dame, 15);
            ghiSd("Hồng đào 50 +15% sức đánh", dame,
                    "itemTime.isUseHongDao50 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao99) {
            dame += calPercent(dame, 20);
            ghiSd("Hồng đào 99 +20% sức đánh", dame,
                    "itemTime.isUseHongDao99 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao999) {
            dame += calPercent(dame, 100);
            ghiSd("Hồng đào 999 +100% sức đánh", dame,
                    "itemTime.isUseHongDao999 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo2) {
            dame += calPercent(dame, 120);
            ghiSd("Cường nộ cấp 2 (+120%, ×2.2)", dame,
                    "itemTime.isUseCuongNo2 đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbsd) {
            dame += calPercent(dame, 30);
            ghiSd("Thuốc rồng băng sức đánh +30%", dame,
                    "itemTime.istrbsd đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbsdxd) {
            dame += calPercent(dame, 15);
            ghiSd("Thuốc rồng băng sức đánh Xayda +15%", dame,
                    "itemTime.istrbsdxd đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhTet) {
            dame += calPercent(dame, 15);
            ghiSd("Bánh tét +15% sức đánh", dame,
                    "itemTime.isUseBanhTet đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhTrung) {
            dame += calPercent(dame, 25);
            ghiSd("Bánh chưng +25% sức đánh", dame,
                    "itemTime.isUseBanhTrung đang bật.");
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            dame += 10_000;
            ghiSd("Phù map Mabu +10.000 sức đánh", dame,
                    "player.isPhuHoMapMabu đang bật.");
        }
        if (this.player.isPlMan()) {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.SUC_DANH);
            if (at != null && !at.isExpired()) {
                dame += calPercent(dame, at.getValue());
                ghiSd("Sự kiện máy chủ +" + at.getValue() + "% sức đánh", dame,
                        "Attribute SUC_DANH còn hiệu lực trên toàn máy chủ.");
            }
        }
        // Top Whis KHONG con cong chi so: gio thuong thoi vang moi ngay,
        // xem TopWhisDAO.phatNeuCan.

        //SucManhBocPha
        if (player.isPl() && player.playerSkill.skillSelect != null) {
            int tiLeDameSucManhBocPha = SkillUtil.getPercentDameSucManhBocPha(player.playerSkill.skillSelect.point);
            if (this.player.effectSkill.isSUcManhBocPha) {
                dame += (dame * tiLeDameSucManhBocPha / 100);
                ghiSd("Sức mạnh bộc phá +" + tiLeDameSucManhBocPha + "% sức đánh", dame,
                        "Tính theo cấp kỹ năng đang chọn.");
            }
        }
        //giảm dame
        if (tlSubSD > 0) {
            dame -= (dame * tlSubSD / 100);
            ghiSd("Giảm sức đánh -" + tlSubSD + "%", dame,
                    moTaNguonList(nguonSdGiamPct));
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            dame /= 2;
            ghiSd("Map lạnh: giảm 50% sức đánh", dame,
                    "Không phải boss/bot, đang ở map lạnh và chưa có kháng lạnh.");
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapChristMasEvent(this.player.zone.map.mapId) && !this.isKhongLanh) {
            dame /= 2;
            ghiSd("Map Noel: giảm 50% sức đánh", dame,
                    "Không phải boss/bot, đang ở map Christmas Event và chưa có kháng lạnh.");
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMap5000NamTruoc(this.player.zone.map.mapId)) {
            dame -= calPercent(dame, 90);
            ghiSd("Map 5000 năm trước: giảm 90% sức đánh", dame,
                    "Không phải boss/bot và đang ở map 5000 năm trước.");
        }

        if (player.gender == ConstPlayer.XAYDA) {
            if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCereal(this.player.zone.map)) {
                dame /= 2;
                ghiSd("Xayda ở map Cereal: giảm 50% sức đánh", dame,
                        "Áp riêng cho hệ Xayda khi ở map Cereal.");
            }
        }

        // Xử lý ngọc rồng đen 1 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[0] > System.currentTimeMillis()) {
            dame += (dame * RewardBlackBall.R1S_2 / 100);
            ghiSd("Ngọc rồng đen 1 sao +" + RewardBlackBall.R1S_2 + "% sức đánh", dame,
                    "rewardBlackBall.timeOutOfDateReward[0] còn hạn.");
        }

        if (this.player.effectSkill.isMonkey) {
            if (!laDeDangHopThe()) {
                int percent = SkillUtil.getPercentDameMonkey(player.effectSkill.levelMonkey);
                // Hieu luc chieu Bien Khi (skill_power_pct).
                percent += percent * nro.repository.dao.SetBonusDAO.tongTheoChieu(
                        this.player, "skill_power_pct", Skill.BIEN_KHI) / 100;
                dame += (dame * percent / 100);
                ghiSd("Biến khỉ +" + percent + "% sức đánh", dame,
                        "Tính theo levelMonkey; bỏ qua khi đệ đang hợp thể.");
            }
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            dame *= this.player.effectSkin.xDame;
            ghiSd("Map ngọc rồng đen ×" + this.player.effectSkin.xDame + " sức đánh", dame,
                    "Nhân theo effectSkin.xDame trong Black Ball War.");
        }

        if (this.player.itemTime != null && this.player.itemTime.Ishamburgersau) {
            dame += calPercent(dame, 10);
            ghiSd("Hamburger sầu +10% sức đánh", dame,
                    "itemTime.Ishamburgersau đang bật.");
        }

        if (this.player.itemTime != null && this.player.itemTime.Isthuocmothuong) {
            dame += calPercent(dame, 10);
            ghiSd("Thuốc mỡ thường +10% sức đánh", dame,
                    "itemTime.Isthuocmothuong đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.Isthuocmodacbiet) {
            dame += calPercent(dame, 10);
            ghiSd("Thuốc mỡ đặc biệt +10% sức đánh", dame,
                    "itemTime.Isthuocmodacbiet đang bật.");
        }
        if (this.player.itemTime != null && this.player.itemTime.iscuarangme) {
            dame += calPercent(dame, 5);
            ghiSd("Cua rang me +5% sức đánh", dame,
                    "itemTime.iscuarangme đang bật.");
        }
        this.dame = dame;
    }

    private void setOptions() {
        // Chi so de rieng cho boss. Ap o day vi setOptions chay SAU setDef, nen
        // cong vao defAdd la khong con tac dung — phai cong thang vao def.
        if (this.giapBoss >= 0) {
            this.def += this.giapBoss;
        }
        if (this.neDonBoss >= 0) {
            this.tlNeDon += this.neDonBoss;
        }
        if (this.chinhXacBoss >= 0) {
            this.tlchinhxac += this.chinhXacBoss;
        }
        if (this.player.isPl() && this.intrinsic != null && this.intrinsic.id == 23) {
            this.tlGold += intrinsic.param1;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseNedon) {
            this.tlNeDon += 10;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseNedon2) {
            this.tlNeDon += 20;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUsePhanSatThuong) {
            this.tlPST += 3;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUsePhanSatThuong2) {
            this.tlPST += 6;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUsePhanSatThuong3) {
            this.tlPST += 12;
        }
        if (this.player.itemTime != null && this.player.itemTime.IsKhauTrang) {
            this.tlDameAttMob.add(10);
            this.nguonSdMobPct.add(new NguonChiSo(10, "Item time - khẩu trang",
                    "IsKhauTrang đang bật: +10% sát thương khi đánh quái."));
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseSatThuongChuan) {
            this.tlstc += 10;
        }
        if (this.player.itemTime != null && (this.player.itemTime.isUseSatThuongChuan2 || this.player.itemTime.isUseSauRieng)) {
            this.tlstc += 15;
        }
        //hieuunng
        if (this.player.itemTime != null && this.player.itemTime.isUseTHUOCTANGHINH) {
            this.wearingVoHinh = true;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseTHUOCTANGHINH10) {
            this.wearingVoHinh = true;
        }

        if (player.gender == ConstPlayer.XAYDA) {
            if (this.player.zone != null && MapService.gI().isMapVohinh(this.player.zone.map)) {
                this.wearingVoHinh = true;
            }
        }
        // Trang bi ne: bac n cong dung n phan tram ne don.
        //
        // Truoc day cho nay viet "tlNeDon += (tlNeDon + n)" trong muoi khoi if
        // roi nhau, tuc la NHAN DOI ne dang co roi moi cong n: ne 30 voi trang bi
        // ne bac 5 ra 65 chu khong phai 35. Cang nhieu ne tu do thi phan cong sai
        // cang lon, nen cang khong ai de y.
        if (this.player.trbne >= 1 && this.player.trbne <= 10) {
            this.tlNeDon += this.player.trbne;
        }
    }

    private void setDef() {
        this.def = this.defg * 4;
        this.def += this.defAdd;
        //đồ
        for (Integer tl : this.tlDef) {
            this.def += ((long) this.def * tl / 100);
        }
        if (this.player.itemTime != null && this.player.itemTime.Isbanhgatonhen) {
            this.def += ((long) this.def * 10 / 100);
        }
        //ngọc rồng đen 2 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            this.def += ((long) this.def * RewardBlackBall.R2S_2 / 100);
        }
        switch (player.isbienhinh) {
            case 1:
                this.def += 5000;
                break;
            case 2:
                this.def += 10000;
                break;
            case 3:
                this.def += 15000;
                break;
            case 4:
                this.def += 20000;
                break;
            case 5:
                this.def += 25000;
                break;
            default:
                break;
        }
        if (this.player.setClothes != null && this.player.setClothes.ctInosuke != -1) {
            this.def += calPercent(this.def, 50);
        }
        if (this.player.setClothes != null && this.player.setClothes.ctInoHashi != -1) {
            this.def += calPercent(this.def, 60);
        }
    }

    private void setCrit() {
        this.crit = this.critg;
        this.crit += this.critAdd;
        if (this.player.isPl()) {
            this.crit += InventoryService.gI().CritItemsInBoxCollection(this.player);
        }
        if (this.player.itemTime != null) {
            if (this.player.itemTime.isUseBanhDeoC2) {
                this.crit += 10;
            }
            if (this.player.itemTime.isUseTrungThu1Trung) {
                this.crit += 10;
            }
            if (this.player.itemTime.isUseTrungThu2Trung) {
                this.crit += 15;
            }
            if (this.player.itemTime.isUseTrungThuDB) {
                this.crit += 20;
            }
            if (this.player.itemTime.isUseHBTrungThu) {
                this.crit += 25;
            }
            if (this.player.itemTime.isUseRocket1h) {
                this.crit += 20;
            }
            if (this.player.itemTime.isUseChiMang2) {
                this.crit += 10;
            }
            if (this.player.itemTime.isUseChiMang3) {
                this.crit += 15;
            }
            if (this.player.itemTime.IsBocPha) {
                this.crit += 5;
            }
            if (this.player.itemTime.IsKeoMotMat) {
                this.crit += 5;
            }
            if (this.player.itemTime.isUseBanhTet) {
                this.crit += 15;
            }
            if (this.player.itemTime.isUseBanhTrung) {
                this.crit += 25;
            }
        }
        if (this.player.effectSkill != null) {
            // De dang hop the: khi cua de khong tinh gi — xem laDeDangHopThe.
            if (this.player.effectSkill.isMonkey && !laDeDangHopThe()) {
                this.crit = 100;
            }
            if (this.player.effectSkill.iscumber) {
                this.crit += 20;
            }
            if (this.player.effectSkill.iscumber2) {
                this.crit += 15;
            }
            if (this.player.effectSkill.iskefla) {
                this.crit = 100;
            }
        }
        if (this.player.itemTime != null && this.player.itemTime.IsDuoiKhi) {
            this.crit = 100;
        }
        if (this.player.getBuff() == Buff.BUFF_CRIT) {
            this.crit += 10;
        }
        switch (this.player.isbienhinh) {
            case 1:
                this.crit += 10;
                break;
            case 2:
                this.crit += 20;
                break;
            case 3:
                this.crit += 30;
                break;
            case 4:
                this.crit += 40;
                break;
            case 5:
                this.crit += 50;
                break;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[2] > System.currentTimeMillis()) {
            this.crit += RewardBlackBall.R3S_2;
        }
        if (this.crit > 100) {
            this.crit = 100;
        }
    }

    private void setCritDame() {
        if (this.player.setClothes != null && this.player.setClothes.ctTanjiro != -1) {
            this.tlSDCM += 30;
            this.tlDameCrit.add(30);
        }
        if (this.player.itemTime != null && this.player.itemTime.isbachtuocnuong) {
            this.tlSDCM += 5;
            this.tlDameCrit.add(5);
        }
        if (this.player.itemTime != null && this.player.itemTime.IsBocPha) {
            this.tlSDCM += 5;
            this.tlDameCrit.add(5);
        }
        //set worldcup
        // Xử lý set nail
    }

    private void setHutHp() {
        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC1) {
            this.tlHutHp += 30;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC2) {
            this.tlHutHp += 50;
        }
    }

    private void setHutMp() {
        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC1) {
            this.tlHutMp += 30;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC2) {
            this.tlHutMp += 50;
        }
    }

    public void addHp(long hp) {
        if (hp > 0) {
            long potentialHp = this.hp + hp;
            if (potentialHp > this.hpMax) {
                this.hp = this.hpMax;
            } else {
                this.hp = potentialHp;
            }
        }
    }

    public void addMp(long mp) {
        long potentialMp = this.mp + mp;
        if (potentialMp > this.mpMax) {
            this.mp = this.mpMax;
        } else if (potentialMp < 0) {
            this.mp = 0;
        } else {
            this.mp = potentialMp;
        }
    }

    public void setHp(long hp) {
        if (hp < 0) {
            this.hp = 0;
        } else {
            this.hp = hp;
        }
    }

    public void setMp(long mp) {
        if (mp < 0) {
            this.mp = 0;
        } else {
            this.mp = mp;
        }
    }

    private void resetPoint() {
        clearNguonChiSo();
        this.voHieuChuong = 0;
        this.hpAdd = 0;
        this.mpAdd = 0;
        this.dameAdd = 0;
        this.defAdd = 0;
        this.critAdd = 0;
        this.tlHp.clear();
        this.tlMp.clear();
        this.tlDef.clear();
        this.tlDame.clear();
        this.tlDameCrit.clear();
        this.tlDameAttMob.clear();
        this.tlSDCM = 0;
        this.tlHpHoiBanThanVaDongDoi = 0;
        this.tlMpHoiBanThanVaDongDoi = 0;
        this.hpHoi = 0;
        this.mpHoi = 0;
        this.mpHoiCute = 0;
        this.tlHpHoi = 0;
        this.tlHpHoiBanthan_DongMinh = 0;
        this.tlMpHoi = 0;
        this.tlHutHp = 0;
        this.tlHutMp = 0;
        this.tlHutHpMob = 0;
        this.tlHutHpMpXQ = 0;
        this.tlPST = 0;
        this.tlDameMobFly = 0;
        this.tlDameMobMonkey = 0;
        this.tlDameMobRun = 0;
        this.tlTNSM.clear();
        this.tlDameAttMob.clear();
        this.tlGold = 0;
        this.tlNeDon = 0;
        this.tlGiamMu = 0;
        this.tlMayman = 0;
        this.tlGiamSatThuongNamec = 0;
        this.tlGiamSatThuongTraiDat = 0;
        this.tlGiamSatThuongXayda = 0;
        this.tlTanCongTocNamec = 0;
        this.tlTanCongTocTraiDat = 0;
        this.tlTanCongTocXayda = 0;
        this.tlDameClan = 0;
        this.tlHpClan = 0;
        this.tlMpClan = 0;
        this.tlDameBoss = 0;
        this.tlBom = 0;
        this.tlGiap = 0;
        this.tlxgcc = 0;
        this.tlxgc = 0;
        this.tlstc = 0;
        this.tlchinhxac = 0;
        this.tlSubDamePercenMp20 = 0;
        this.tlCongDonSD = 0;
        this.tlTNSMPet = 0;
        this.xChuong = 0;
        this.isFounder = false;
        this.setTinhAn = 0;
        this.setNhatAn = 0;
        this.setNguyetAn = 0;
        this.tlSexyDame = 0;
        this.tlCuteAddame = 0;
        this.tlCoolDame = 0;
        this.tlSubSD = 0;
        this.tlSubHP = 0;
        this.tlSubMP = 0;
        this.tlHpGiamODo = 0;
        this.tlFixStun = 0;
        this.tlSpeed.clear();
        this.speed = 5;
        this.KhangHoaXuong = false;
        this.teleport = false;
        this.wearingVoHinh = false;
        this.isKhongLanh = false;
        this.isHoaBiNgoXungQuanh = false;
        this.khangTDHS = false;
        this.isTanHinh = false;
        this.isHoaDa = false;
        this.wearingBuiBui = false;
        this.wearingMabu = false;
        this.isDoSPL = false;
        this.isXinbato = false;
        this.isThoDaiCa = false;
        this.isThoBulma = false;
        this.isBunmaTocMau = false;
        this.isTiecBaiBien = false;
        this.Cong20ExpKhiAttackMob = false;
    }

    public void settlGold() {
        if (intrinsic != null && intrinsic.id == 23) {
            this.tlGold += intrinsic.param1;
        }
    }

    private void setIsCrit() {
        if (intrinsic != null && intrinsic.id == 25
                && this.getCurrPercentHP() <= intrinsic.param1) {
            isCrit = true;
        } else if (isCrit100) {
            isCrit100 = false;
            isCrit = true;
        } else {
            Skill skill = this.player.playerSkill == null
                    ? null : this.player.playerSkill.skillSelect;
            int idSkill = skill == null || skill.template == null
                    ? -1 : skill.template.id;
            int themChiMang = idSkill < 0 ? 0
                    : nro.repository.dao.SetBonusDAO.phanTramChiMangSkill(
                            this.player, idSkill);
            themChiMang += nro.service.ThuCungService.gI().themChiMang(this.player);
            int tiLeChiMang = Math.max(0, Math.min(100, this.crit + themChiMang));
            isCrit = Util.isTrue(tiLeChiMang, ConstRatio.PER100);
        }
    }

    private long apDungChiMangChoSkillReturnSom(long dameAttack, int idSkill) {
        if (!isCrit || idSkill == Skill.QUA_CAU_KENH_KHI) {
            return dameAttack;
        }
        dameAttack *= 2;
        int themSdcm = nro.repository.dao.SetBonusDAO.phanTramSdcmSkill(
                this.player, idSkill);
        return dameAttack + dameAttack * (this.tlSDCM + themSdcm) / 100L;
    }

    public double getDameAttack(boolean isAttackMob) {
        intrinsic = this.player.playerIntrinsic == null ? null : this.player.playerIntrinsic.intrinsic;
        setIsCrit();
        // Thu cung: boc xem chieu nao no, roi cong phan tram vao dame goc.
        // Dat o day vi moi duong tinh sat thuong ben duoi deu di tu dameAttack,
        // ke ca may nhanh return som.
        long dameAttack = nro.service.ThuCungService.gI().dameSauThuCung(this.player, this.dame);
        percentDameIntrinsic = 0;
        int percentDameSkill = 0;
        // int chu khong phai byte: set viet cung cho 100 roi cong them
        // cau hinh 100 nua la 200 — tran byte thanh -56, tuc tru sat
        // thuong thay vi cong.
        int percentXDame = 0;
        Skill skillSelect = player.playerSkill == null ? null : player.playerSkill.skillSelect;
        if (skillSelect == null || skillSelect.template == null) {
            return dameAttack;
        }
        if (skillSelect.template.id != Skill.DICH_CHUYEN_TUC_THOI && isCritTele) {
            isCrit = true;
            isCritTele = false;
        }
        switch (skillSelect.template.id) {
            case Skill.DRAGON:
                if (intrinsic != null && intrinsic.id == 1) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAMEJOKO:
                if (intrinsic != null && intrinsic.id == 2) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.GALICK:
                if (intrinsic != null && intrinsic.id == 16) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.ANTOMIC:
                if (intrinsic != null && intrinsic.id == 17) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.DEMON:
                if (intrinsic != null && intrinsic.id == 8) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.MASENKO:
                if (intrinsic != null && intrinsic.id == 9) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.LIEN_HOAN:
                if (intrinsic != null && intrinsic.id == 13) {
                    percentDameIntrinsic = intrinsic.param1;
                }
//                percentDameSkill = skillSelect.damage;
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAIOKEN:
                if (intrinsic != null && intrinsic.id == 26) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.TU_SAT:
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                isCrit = true;
                isCritTele = true;
                dameAttack = Util.nextLong(Util.CrisGH((dameAttack - (dameAttack / 100 * 5))), Util.CrisGH((dameAttack + (dameAttack / 100 * 5))));
                break;
            case Skill.MAKANKOSAPPO:
                percentDameSkill = skillSelect.damage;
                long dameSkill = Util.CrisGH((long) this.mpMax * percentDameSkill / 100);
                // Chieu nay return som nen khong qua duong tinh chung — phai tu
                // ap phan tram cua set o day.
                int tlMakan = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player, Skill.MAKANKOSAPPO);
                if (tlMakan != 0) {
                    dameSkill += dameSkill * tlMakan / 100;
                }
                return apDungChiMangChoSkillReturnSom(dameSkill, Skill.MAKANKOSAPPO);
            case Skill.QUA_CAU_KENH_KHI:
                // QCKK dung cong thuc rieng va khong duoc chi mang. setIsCrit()
                // chay truoc switch nen phai xoa co de packet khong hien crit ao.
                isCrit = false;
                long hpmob = 0;
                long hppl = 0;
                long hpboss = 0;

                for (Mob mob : this.player.zone.mobs) {
                    if (!mob.isDie() && Util.getDistance(this.player, mob) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hpmob += mob.point.hp;
                    }
                }

                for (Player pl : this.player.zone.getHumanoids()) {
                    if (!pl.isDie() && this.player.id != pl.id && Util.getDistance(this.player, pl) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        if (pl.isBoss) {
                            hpboss += pl.nPoint.hpMax;
                        } else {
                            hppl += pl.nPoint.hp;
                        }
                    }
                }
                long dameqckk = (hpmob * 10 / 100) + (hppl * 10 / 100)
                        + (hpboss * 10 / 100) + this.dame * 10;

                int tlQckk = phanTramSetTheoLoai("qckk_pct");
                if (tlQckk != 0) {
                    dameqckk += dameqckk * tlQckk / 100;
                }
                // Chieu nay return som — tu ap phan tram theo chieu cua set.
                int tlQckkChieu = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player, Skill.QUA_CAU_KENH_KHI);
                if (tlQckkChieu != 0) {
                    dameqckk += dameqckk * tlQckkChieu / 100;
                }

                return dameqckk;
            case Skill.DE_TRUNG:
                // Đây là damage nền của pet. Chí mạng phải được quay theo từng
                // cú pet đánh, không chốt một lần duy nhất lúc nở trứng.
                isCrit = false;
                int tlDanhThuong = phanTramSetTheoLoai("danh_thuong_pct");
                if (tlDanhThuong != 0) {
                    dameAttack += dameAttack * tlDanhThuong / 100;
                }
                // Chieu nay return som — tu ap phan tram theo chieu cua set.
                int tlDeTrung = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player, Skill.DE_TRUNG);
                if (tlDeTrung != 0) {
                    dameAttack += dameAttack * tlDeTrung / 100;
                }
                return dameAttack;
        }
        if (percentDameSkill != 0) {
            dameAttack = dameAttack * percentDameSkill / 100;
        }

        dameAttack += (dameAttack * percentDameIntrinsic / 100);
        dameAttack += (dameAttack * dameAfter / 100);

        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff
                && this.player.effectSkill.tileDameBuff > 0) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            dameAttack += (dameAttack * tiLeDame / 100L);
        }

        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl / 100);
            }
            if (this.player.isDeTu
                    && ((Detu) this.player).master != null
                    && ((Detu) this.player).master.charms != null
                    && ((Detu) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameAttack *= 2;
            }
        }

        dameAfter = 0;

        if (isCrit) {
            dameAttack *= 2;
            int themSdcmSkill = nro.repository.dao.SetBonusDAO.phanTramSdcmSkill(
                    this.player, skillSelect.template.id);
            dameAttack += (dameAttack * (tlSDCM + themSdcmSkill) / 100);
        }

        // Phan tram sat thuong chieu tu cau hinh set (bang set_bonus, loai
        // skill_pct, tham_so = id chieu). Dat o day — SAU switch — nen ap dung
        // cho MOI ky nang di qua duong tinh chung, khong phai chi 10 chieu duoc
        // liet ke tay. Ba chieu return som ben tren tu ap rieng.
        percentXDame += nro.repository.dao.SetBonusDAO.phanTramSkill(
                this.player, skillSelect.template.id);
        dameAttack += dameAttack * percentXDame / 100;

        long tempDameAttack = (long) (dameAttack / 100L * 5L);
        if (tempDameAttack <= 0) {
            tempDameAttack = 1;
        }
        dameAttack += (long) (Util.getOne(-1, 1) * Util.Crisnext(tempDameAttack) + 1);

        if (player.effectSkin != null && player.effectSkin.isXChuong && (player.playerSkill.skillSelect.template.id == Skill.KAMEJOKO || player.playerSkill.skillSelect.template.id == Skill.ANTOMIC || player.playerSkill.skillSelect.template.id == Skill.MASENKO)) {
            dameAttack *= xChuong;
            player.effectSkin.isXDame = true;
            player.effectSkin.isXChuong = false;
            player.effectSkin.lastTimeXChuong = System.currentTimeMillis();
        }
        return dameAttack;
    }

    private static void ghiDameTrace(java.util.List<String[]> ds, String buoc,
            long truoc, long sau, String moTa) {
        ds.add(new String[]{buoc, String.valueOf(sau - truoc), String.valueOf(sau),
            moTa == null ? "" : moTa});
    }

    private static long sauKhiCongPct(long giaTri, long pct) {
        return giaTri + giaTri * pct / 100L;
    }

    private static String tenSkill(Skill skill) {
        if (skill == null) {
            return "không có kỹ năng đang chọn";
        }
        String ten = skill.template != null && skill.template.name != null
                ? skill.template.name : "kỹ năng";
        int id = skill.template != null ? skill.template.id : skill.skillId;
        return ten + " (id " + id + ", cấp " + skill.point
                + ", damage " + skill.damage + "%)";
    }

    private static String moTaNhan(double heSo) {
        return String.format(java.util.Locale.US, "×%.2f", heSo).replace(".00", "");
    }

    private static boolean laSkillCongDonSatThuong(int skillId) {
        switch (skillId) {
            case Skill.DRAGON:
            case Skill.DEMON:
            case Skill.GALICK:
            case Skill.LIEN_HOAN:
            case Skill.KAIOKEN:
                return true;
            default:
                return false;
        }
    }

    private java.util.List<String[]> apDungDameSauSkillService(java.util.List<String[]> ds, long dame,
            Skill skillSelect, boolean isAttackMob) {
        long now = System.currentTimeMillis();
        if (!isAttackMob) {
            double heSo = 1.0;
            try {
                heSo = nro.core.config.SkillDamageConfig.getMultiplier(skillSelect.template.id);
            } catch (Exception ignored) {
                heSo = 1.0;
            }
            if (heSo != 1.0) {
                long truoc = dame;
                dame = (long) (dame * heSo);
                ghiDameTrace(ds, "SkillDamageConfig PvP " + moTaNhan(heSo), truoc, dame,
                        "Áp trong SkillService.playerAttackPlayer sau getDameAttack(false).");
            }
            if (laSkillCongDonSatThuong(skillSelect.template.id) && this.tlCongDonSD > 0) {
                int stackTruoc = this.player.tlDameAdd;
                int stackSau = Math.min(stackTruoc + 1, this.tlCongDonSD);
                long truoc = dame;
                dame = sauKhiCongPct(dame, stackSau);
                ghiDameTrace(ds, "Cộng dồn PvP/TestDame +" + stackSau + "%", truoc, dame,
                        "Player.injured tăng tlDameAdd từ " + stackTruoc + " lên "
                        + stackSau + " rồi cộng đúng bấy nhiêu %. Stack reset về 0 nếu 7 giây không đánh.");
            } else if (this.tlCongDonSD > 0) {
                ghiDameTrace(ds, "Ghi chú cộng dồn PvP/TestDame", dame, dame,
                        "Nhân vật có tlCongDonSD=" + this.tlCongDonSD
                        + " nhưng skill hiện tại không thuộc nhóm đấm/liên hoàn/kaioken nên Player.injured không cộng stack.");
            }
            if (this.tlTanCongTocTraiDat > 0 || this.tlTanCongTocNamec > 0
                    || this.tlTanCongTocXayda > 0) {
                ghiDameTrace(ds, "Ghi chú damage theo hệ mục tiêu", dame, dame,
                        "Player.injured sẽ cộng tiếp nếu mục tiêu đúng hệ: Trái Đất +"
                        + this.tlTanCongTocTraiDat + "%, Namek +" + this.tlTanCongTocNamec
                        + "%, Xayda +" + this.tlTanCongTocXayda + "%.");
            }
            if (this.player.isPl() && this.player.effectSkin != null
                    && this.player.effectSkin.isXDame) {
                ghiDameTrace(ds, "Ghi chú XDame khi đánh boss", dame, dame,
                        "Nếu mục tiêu PvP là boss, SkillService sẽ chia damage còn 1/3.");
            }
            if (this.player.isPlMan() && this.tlDameBoss > 0) {
                ghiDameTrace(ds, "Ghi chú +" + this.tlDameBoss + "% damage boss", dame, dame,
                        "Chỉ cộng trong PvP khi mục tiêu là boss; popup này chưa biết mục tiêu.");
            }
            ghiDameTrace(ds, "Sau đó qua phòng thủ mục tiêu", dame, dame,
                    "Player.injured còn xét né đòn, giáp, xuyên giáp, sát thương chuẩn, giảm damage theo hệ/map và HP mục tiêu.");
            return ds;
        }

        boolean batTu = this.player.charms != null && this.player.charms.tdBatTu > now;
        boolean halloween = this.player.effectSkill != null && this.player.effectSkill.isHalloween;
        if ((batTu || halloween) && this.hp <= 1 && !this.player.isDeTu) {
            long truoc = dame;
            dame = 0;
            ghiDameTrace(ds, "Bùa bất tử/Halloween khi còn 1 HP", truoc, dame,
                    "SkillService.playerAttackMob chặn người chơi thường tấn công khi đang được bảo vệ.");
        }
        if (this.player.charms != null && this.player.charms.tdManhMe > now) {
            long truoc = dame;
            dame += dame * 150 / 100L;
            ghiDameTrace(ds, "Bùa mạnh mẽ +150% damage quái", truoc, dame,
                    "Áp trong SkillService.playerAttackMob sau getDameAttack(true).");
        }
        if (this.player.clan != null && this.player.clan.BuaManhMe > now) {
            int bonusPercent = this.player.clan.level * 10;
            if (bonusPercent > 100) {
                bonusPercent = 100;
            }
            long truoc = dame;
            dame += dame * bonusPercent / 100L;
            ghiDameTrace(ds, "Bùa mạnh mẽ bang +" + bonusPercent + "% damage quái", truoc, dame,
                    "Áp theo cấp bang trong SkillService.playerAttackMob, tối đa +100%.");
        }
        if (dame > 2_000_000_000L) {
            long truoc = dame;
            dame = 2_000_000_000L;
            ghiDameTrace(ds, "Giới hạn damage gửi vào Mob.injured", truoc, dame,
                    "SkillService.playerAttackMob chặn damage quái tối đa 2.000.000.000 trước khi gọi Mob.injured.");
        }
        if (this.tlDameMobFly > 0 || this.tlDameMobMonkey > 0 || this.tlDameMobRun > 0) {
            ghiDameTrace(ds, "Ghi chú damage theo loại quái", dame, dame,
                    "Mob.injured sẽ cộng tiếp nếu đúng loại: bay +" + this.tlDameMobFly
                    + "%, khỉ +" + this.tlDameMobMonkey + "%, mặt đất/chạy +"
                    + this.tlDameMobRun + "%. Popup này chưa biết quái mục tiêu.");
        }
        ghiDameTrace(ds, "Sau đó qua luật của quái", dame, dame,
                "Mob.injured còn xét HP hiện tại, siêu quái, mộc nhân/bù nhìn, map đặc biệt và loại quái.");
        return ds;
    }

    public java.util.List<String[]> giaiThichDameDauRa(boolean isAttackMob, boolean tinhChiMang) {
        java.util.List<String[]> ds = new java.util.ArrayList<>();
        long dameAttack = this.dame;
        ghiDameTrace(ds, "Sức đánh hiện tại (nPoint.dame)", 0, dameAttack,
                "Con số cuối của tab SĐ, sau trang bị, item time, fusion, set và các hiệu ứng chỉ số.");

        if (this.player == null || this.player.playerSkill == null
                || this.player.playerSkill.skillSelect == null
                || this.player.playerSkill.skillSelect.template == null) {
            ghiDameTrace(ds, "Không có kỹ năng đang chọn", dameAttack, dameAttack,
                    "Không thể mô phỏng damage đầu ra nếu playerSkill.skillSelect đang null.");
            return ds;
        }

        Skill skillSelect = this.player.playerSkill.skillSelect;
        Intrinsic noiTai = this.player.playerIntrinsic == null
                ? null : this.player.playerIntrinsic.intrinsic;
        int percentDameIntrinsic = 0;
        int percentDameSkill = 0;
        int skillId = skillSelect.template.id;
        String skillText = tenSkill(skillSelect);
        int themChiMangSkill = nro.repository.dao.SetBonusDAO.phanTramChiMangSkill(
                this.player, skillId);
        int themSdcmSkill = nro.repository.dao.SetBonusDAO.phanTramSdcmSkill(
                this.player, skillId);
        ghiDameTrace(ds, tinhChiMang ? "Đang xem nhánh chí mạng" : "Đang xem nhánh không chí mạng",
                dameAttack, dameAttack,
                "crit=" + this.crit + "% + crit riêng chiêu=" + themChiMangSkill
                + "%, tlSDCM=" + this.tlSDCM + "% + SDCM riêng chiêu=" + themSdcmSkill
                + "%, isCrit100=" + this.isCrit100 + ", isCritTele=" + this.isCritTele
                + ". Nếu crit đạt 100% hoặc bị ép crit thì damage thật đi theo tab chí mạng.");

        switch (skillId) {
            case Skill.DRAGON:
                if (noiTai != null && noiTai.id == 1) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAMEJOKO:
                if (noiTai != null && noiTai.id == 2) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.GALICK:
                if (noiTai != null && noiTai.id == 16) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.ANTOMIC:
                if (noiTai != null && noiTai.id == 17) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.DEMON:
                if (noiTai != null && noiTai.id == 8) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.MASENKO:
                if (noiTai != null && noiTai.id == 9) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.LIEN_HOAN:
                if (noiTai != null && noiTai.id == 13) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAIOKEN:
                if (noiTai != null && noiTai.id == 26) {
                    percentDameIntrinsic = noiTai.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.TU_SAT:
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                ghiDameTrace(ds, "Dịch chuyển tức thời ép chí mạng", dameAttack, dameAttack,
                        "getDameAttack ép isCrit=true và dao động khoảng ±5% trước khi nhân chí mạng.");
                tinhChiMang = true;
                break;
            case Skill.MAKANKOSAPPO:
                long truocMakan = dameAttack;
                dameAttack = Util.CrisGH((long) this.mpMax * skillSelect.damage / 100L);
                ghiDameTrace(ds, "Makankosappo lấy KI tối đa × damage%", truocMakan, dameAttack,
                        skillText + " | Chiêu này return sớm, không đi qua công thức SĐ chung.");
                int tlMakan = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player, Skill.MAKANKOSAPPO);
                if (tlMakan != 0) {
                    long truoc = dameAttack;
                    dameAttack = sauKhiCongPct(dameAttack, tlMakan);
                    ghiDameTrace(ds, "Set skill Makankosappo +" + tlMakan + "%", truoc, dameAttack,
                            "Lấy từ set_bonus loại skill_pct, tham_so=MAKANKOSAPPO.");
                }
                return dsSauRandomSom(ds, dameAttack, skillSelect, isAttackMob,
                        tinhChiMang, themSdcmSkill);
            case Skill.QUA_CAU_KENH_KHI:
                long hpmob = 0;
                long hppl = 0;
                long hpboss = 0;
                if (this.player.zone != null) {
                    for (Mob mob : this.player.zone.mobs) {
                        if (mob != null && !mob.isDie()
                                && Util.getDistance(this.player, mob) <= SkillUtil.getRangeQCKK(skillSelect.point)) {
                            hpmob += mob.point.hp;
                        }
                    }
                    for (Player pl : this.player.zone.getHumanoids()) {
                        if (pl != null && !pl.isDie() && this.player.id != pl.id
                                && Util.getDistance(this.player, pl) <= SkillUtil.getRangeQCKK(skillSelect.point)) {
                            if (pl.isBoss) {
                                hpboss += pl.nPoint.hpMax;
                            } else {
                                hppl += pl.nPoint.hp;
                            }
                        }
                    }
                }
                long truocQckk = dameAttack;
                dameAttack = (hpmob * 10 / 100L) + (hppl * 10 / 100L)
                        + (hpboss * 10 / 100L) + this.dame * 10L;
                ghiDameTrace(ds, "Quả cầu kênh khi", truocQckk, dameAttack,
                        "10% HP hiện tại quái/người gần + 10% HP tối đa boss gần + SĐ hiện tại ×10. "
                        + "HP quái=" + hpmob + ", HP người=" + hppl
                        + ", HP tối đa boss=" + hpboss + ".");
                int tlQckk = phanTramSetTheoLoai("qckk_pct");
                if (tlQckk != 0) {
                    long truoc = dameAttack;
                    dameAttack = sauKhiCongPct(dameAttack, tlQckk);
                    ghiDameTrace(ds, "Set qckk_pct +" + tlQckk + "%", truoc, dameAttack,
                            "Tổng phần trăm từ set_bonus loại qckk_pct.");
                }
                int tlQckkChieu = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player, Skill.QUA_CAU_KENH_KHI);
                if (tlQckkChieu != 0) {
                    long truoc = dameAttack;
                    dameAttack = sauKhiCongPct(dameAttack, tlQckkChieu);
                    ghiDameTrace(ds, "Set skill QCKK +" + tlQckkChieu + "%", truoc, dameAttack,
                            "Lấy từ set_bonus loại skill_pct, tham_so=QUA_CAU_KENH_KHI.");
                }
                ghiDameTrace(ds, "QCKK cố định, không chí mạng", dameAttack, dameAttack,
                        "Không nhân chí mạng và không dao động ngẫu nhiên; HP boss dùng HP tối đa.");
                return apDungDameSauSkillService(ds, dameAttack, skillSelect, isAttackMob);
            case Skill.DE_TRUNG:
                int tlDanhThuong = phanTramSetTheoLoai("danh_thuong_pct");
                if (tlDanhThuong != 0) {
                    long truoc = dameAttack;
                    dameAttack = sauKhiCongPct(dameAttack, tlDanhThuong);
                    ghiDameTrace(ds, "Set đánh thường +" + tlDanhThuong + "%", truoc, dameAttack,
                            "Tổng phần trăm từ set_bonus loại danh_thuong_pct.");
                }
                int tlDeTrung = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player, Skill.DE_TRUNG);
                if (tlDeTrung != 0) {
                    long truoc = dameAttack;
                    dameAttack = sauKhiCongPct(dameAttack, tlDeTrung);
                    ghiDameTrace(ds, "Set skill Đẻ trứng +" + tlDeTrung + "%", truoc, dameAttack,
                            "Lấy từ set_bonus loại skill_pct, tham_so=DE_TRUNG.");
                }
                return dsSauRandomSom(ds, dameAttack, skillSelect, isAttackMob,
                        tinhChiMang, themSdcmSkill);
            default:
                break;
        }

        if (percentDameSkill != 0) {
            long truoc = dameAttack;
            dameAttack = dameAttack * percentDameSkill / 100L;
            ghiDameTrace(ds, "Kỹ năng " + skillText, truoc, dameAttack,
                    "Nhân theo damage% của skill trước nội tại và các buff damage đầu ra.");
        }
        if (percentDameIntrinsic != 0) {
            long truoc = dameAttack;
            dameAttack = sauKhiCongPct(dameAttack, percentDameIntrinsic);
            ghiDameTrace(ds, "Nội tại +" + percentDameIntrinsic + "% damage skill", truoc, dameAttack,
                    "Nội tại id " + (noiTai == null ? "null" : noiTai.id)
                    + " khớp với kỹ năng đang chọn.");
        }
        if (this.dameAfter != 0) {
            long truoc = dameAttack;
            dameAttack = sauKhiCongPct(dameAttack, this.dameAfter);
            ghiDameTrace(ds, "dameAfter +" + this.dameAfter + "%", truoc, dameAttack,
                    "Giá trị cộng một lần, getDameAttack thật sẽ reset dameAfter về 0 sau khi tính.");
        }
        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff
                && this.player.effectSkill.tileDameBuff > 0) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            long truoc = dameAttack;
            dameAttack = sauKhiCongPct(dameAttack, tiLeDame);
            ghiDameTrace(ds, "Buff damage tạm +" + tiLeDame + "%", truoc, dameAttack,
                    "effectSkill.isDameBuff/tileDameBuff, nhận từ aura Đẹp/Cool/Cute của người khác.");
        }
        if (isAttackMob) {
            if (this.nguonSdMobPct.size() == this.tlDameAttMob.size()) {
                for (NguonChiSo n : this.nguonSdMobPct) {
                    long truoc = dameAttack;
                    dameAttack = sauKhiCongPct(dameAttack, n.giaTri);
                    ghiDameTrace(ds, "Đánh quái +" + n.giaTri + "%", truoc, dameAttack,
                            n.moTa() + " | Chỉ áp một lần trong getDameAttack(true).");
                }
            } else {
                for (Integer tl : this.tlDameAttMob) {
                    if (tl != null) {
                        long truoc = dameAttack;
                        dameAttack = sauKhiCongPct(dameAttack, tl);
                        ghiDameTrace(ds, "Đánh quái +" + tl + "%", truoc, dameAttack,
                                "Nguồn chưa gắn mô tả; lấy từ tlDameAttMob.");
                    }
                }
            }
            if (this.player.isDeTu
                    && ((Detu) this.player).master != null
                    && ((Detu) this.player).master.charms != null
                    && ((Detu) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                long truoc = dameAttack;
                dameAttack *= 2;
                ghiDameTrace(ds, "Bùa đệ tử của sư phụ ×2", truoc, dameAttack,
                        "Áp một lần trong getDameAttack(true); SkillService không nhân lại lần hai nữa.");
            }
        }
        if (tinhChiMang) {
            long truoc = dameAttack;
            dameAttack *= 2;
            ghiDameTrace(ds, "Chí mạng ×2", truoc, dameAttack,
                    "Bảng này đang xem nhánh chí mạng; thực tế phụ thuộc tỉ lệ crit/isCrit100/isCritTele.");
            int tongSdcm = this.tlSDCM + themSdcmSkill;
            if (tongSdcm != 0) {
                truoc = dameAttack;
                dameAttack = sauKhiCongPct(dameAttack, tongSdcm);
                ghiDameTrace(ds, "Sát thương chí mạng +" + tongSdcm + "%", truoc, dameAttack,
                        "Gồm SDCM chung +" + this.tlSDCM + "% và SDCM riêng chiêu +"
                        + themSdcmSkill + "%.");
            }
        }
        int percentXDame = nro.repository.dao.SetBonusDAO.phanTramSkill(
                this.player, skillId);
        if (percentXDame != 0) {
            long truoc = dameAttack;
            dameAttack = sauKhiCongPct(dameAttack, percentXDame);
            ghiDameTrace(ds, "Set skill " + skillText + " +" + percentXDame + "%", truoc, dameAttack,
                    "Lấy từ set_bonus loại skill_pct, tham_so=id kỹ năng đang chọn.");
        }
        ghiDameTrace(ds, "Dao động ngẫu nhiên cuối", dameAttack, dameAttack,
                "getDameAttack cộng thêm khoảng ±5% trước khi trả damage; bảng giữ giá trị trung tâm.");
        if (this.player.effectSkin != null && this.player.effectSkin.isXChuong
                && (skillId == Skill.KAMEJOKO || skillId == Skill.ANTOMIC || skillId == Skill.MASENKO)) {
            long truoc = dameAttack;
            dameAttack *= this.xChuong;
            ghiDameTrace(ds, "X chưởng ×" + this.xChuong, truoc, dameAttack,
                    "effectSkin.isXChuong đang bật; cú thật sẽ bật isXDame rồi tắt isXChuong.");
        }
        return apDungDameSauSkillService(ds, dameAttack, skillSelect, isAttackMob);
    }

    private java.util.List<String[]> dsSauRandomSom(java.util.List<String[]> ds, long dameAttack,
            Skill skillSelect, boolean isAttackMob, boolean tinhChiMang, int themSdcmSkill) {
        ghiDameTrace(ds, "Chiêu return sớm", dameAttack, dameAttack,
                "Không qua phần skill damage chung, dameAfter hay dao động cuối của getDameAttack.");
        if (tinhChiMang) {
            long truoc = dameAttack;
            dameAttack *= 2;
            ghiDameTrace(ds, "Chí mạng chiêu return sớm ×2", truoc, dameAttack,
                    "Tỉ lệ gồm chí mạng chung và skill_crit_pct từ set/trang bị của chiêu.");
            int tongSdcm = this.tlSDCM + themSdcmSkill;
            if (tongSdcm != 0) {
                truoc = dameAttack;
                dameAttack = sauKhiCongPct(dameAttack, tongSdcm);
                ghiDameTrace(ds, "SDCM chiêu return sớm +" + tongSdcm + "%", truoc, dameAttack,
                        "Gồm SDCM chung và skill_sdcm_pct từ set/trang bị của chiêu.");
            }
        }
        return apDungDameSauSkillService(ds, dameAttack, skillSelect, isAttackMob);
    }

    public int getCurrPercentHP() {
        if (this.hpMax == 0) {
            return 100;
        }
        return (int) ((long) this.hp * 100 / this.hpMax);
    }

    public int getCurrPercentMP() {
        return (int) ((long) this.mp * 100 / this.mpMax);
    }

    public void setFullHpMp() {
        this.hp = this.hpMax;
        this.mp = this.mpMax;
    }

    public void subHP(double sub) {
        this.hp -= sub;
        if (this.hp <= 0) {
            this.hp = 0;
            this.setHp(0);
        }
    }

    public void subMP(long sub) {
        this.mp -= sub;
        if (this.mp <= 0) {
            this.mp = 0;
        }
    }

    public void setFullHp() {
        this.hp = this.hpMax;
    }

    public void setFullMp() {
        this.mp = this.mpMax;
    }

    public long calPercent(long param, long percent) {
        return param * percent / 100;
    }

    public void subSucManh(long point) {
        this.power -= point;
        if (this.power <= 0) {
            this.power = 0;
        }
    }

    public void subTiemNang(long point) {
        this.tiemNang -= point;
        if (this.tiemNang <= 0) {
            this.tiemNang = 0;
        }
    }

    public void subSucManhTiemNang(long point) {
        this.power -= point;
        this.tiemNang -= point;
        if (this.power <= 0) {
            this.power = 0;
        }
        if (this.tiemNang <= 0) {
            this.tiemNang = 0;
        }
    }

    // =====================================================================
    //  Tiềm năng một cú đánh — MỘT hàm thuần, máy chủ và panel cùng dùng
    // =====================================================================

    /**
     * Mọi thứ trong game làm tiềm năng một cú đánh to lên hay nhỏ đi.
     *
     * <h3>Vì sao tách ra</h3>
     *
     * <p>Panel cần cho xem trước "đánh ở đây, với điều kiện này, thì nhận bao
     * nhiêu" — và con số ấy phải <b>bằng đúng</b> con số hiện trên đầu nhân vật
     * trong game. Cách duy nhất để chắc chắn là panel không tự viết lại công
     * thức mà gọi <b>đúng đoạn mã máy chủ đang chạy</b>. Nên phần tính được
     * tách thành {@link #tinhTiemNang} nhận vào một bộ điều kiện: máy chủ dựng
     * bộ ấy từ nhân vật thật ({@link #dieuKienTnHienTai}), panel dựng từ các ô
     * người dùng tích. Cùng một hàm thì không thể lệch nhau.</p>
     *
     * <p>Thứ tự các bước và cách làm tròn giữ nguyên xi bản cũ. Riêng bùa tiềm
     * năng cho đệ tử nay chỉ cộng một lần — bản cũ cộng hai lần (hai lẫn mười
     * lần giá trị gốc) do một đoạn bị dán trùng.</p>
     */
    public static final class DieuKienTn {

        // --- Của chính người đánh ---
        public long sucManh;
        /** Giới hạn sức mạnh; chạm tới thì mỗi cú đánh chỉ còn 10. */
        public long gioiHanSucManh = Long.MAX_VALUE;
        /** Các dòng "+#% tiềm năng" trên trang bị, mỗi dòng nhân dồn. */
        public java.util.List<Integer> tlTnsm = new java.util.ArrayList<>();
        /** Mã cờ đang cắm: 0 không cờ, 8 cờ đặc biệt (+10%), khác (+5%). */
        public int co;
        /** Phần trăm sự kiện tiềm năng toàn máy chủ đang chạy. */
        public int thuocTinhMayChu;
        /** Thẻ tuần/tháng/năm: 0 không có, 1 loại thường, 2 loại cao. */
        public int theTuan;
        public int theThang;
        public int theNam;
        public boolean theChiTon;
        public boolean khauTrang;
        public boolean buaTriTue;
        public boolean buaTriTue3;
        public boolean buaTriTue4;
        public boolean buaTriTue5;
        public boolean buaTriTue7;
        public boolean buaTriTue10;
        public boolean buaTriTue20;
        /** Phần trăm của nội tại tiềm năng (nội tại số 24), 0 là không có. */
        public int noiTaiTn;
        public boolean chibiTn;
        /** Số lần cộng của buff tiềm năng toàn bang đang chạy, 0 là không có. */
        public long bangCongTn;
        /** Cấp bang khi bang đang bật bùa trí tuệ, -1 là không bật. */
        public int bangBuaTriTue = -1;
        public boolean veTinhTriLuc;
        public boolean vip;
        public boolean rongXuong3;
        public boolean duoiKhiTn;
        public boolean cong20KhiDanhQuai;
        /** Phần trăm tiềm năng cộng thêm từ set kích hoạt. */
        public int setTnPct;

        // --- Chỉ khi người đánh là đệ tử: những thứ lấy từ sư phụ ---
        public boolean laDeTu;
        /** Loại đệ: 0-1 chia 1, 2-4 chia 2, 5 chia 4. */
        public int loaiDeTu;
        public boolean coSuPhu;
        public boolean spBuaTnsmDeTu;
        public boolean spBuaDeTu;
        public boolean spBuaDeTu2;
        public boolean spBuaDeTu3;
        public boolean spBuaDeTu4;
        public boolean spBuaDeTu5;
        public boolean spBuaDeTu7;
        public boolean spBuaDeTu10;
        public boolean spBuaDeTu20;
        public boolean spRongXuong;
        public int spTheTuan;
        public int spTheThang;
        public int spTheNam;
        public boolean spTheChiTon;
        public boolean spCoNPoint = true;
        /** Phần trăm tiềm năng cho đệ trên đồ của sư phụ. */
        public long spTlTnsmPet;

        public boolean laBo;
        public boolean laMe;
        /** Hệ số exp của máy chủ ({@code Manager.RATE_EXP_SERVER}). */
        public int heSoMayChu = Manager.RATE_EXP_SERVER;
    }

    /** {@code calPercent} bản tĩnh — giống hệt, kể cả làm tròn xuống. */
    private static long pt(long param, long percent) {
        return param * percent / 100;
    }

    /**
     * Tiềm năng của một cú đánh sau mọi điều kiện, trước hệ số chung.
     *
     * @param tiemNang giá trị ngay sau hệ số bản đồ
     */
    public static long tinhTiemNang(long tiemNang, DieuKienTn dk) {
        if (dk.sucManh >= dk.gioiHanSucManh) {
            return 10;
        }
        long goc = tiemNang;

        if (dk.tlTnsm != null) {
            for (Integer tl : dk.tlTnsm) {
                if (tl != null) {
                    tiemNang += pt(tiemNang, tl);
                }
            }
        }
        if (dk.co != 0) {
            tiemNang += pt(tiemNang, dk.co == 8 ? 10 : 5);
        }
        if (dk.thuocTinhMayChu != 0) {
            tiemNang += pt(tiemNang, dk.thuocTinhMayChu);
        }
        if (dk.theTuan != 0) {
            tiemNang += pt(tiemNang, dk.theTuan == 1 ? 20 : 50);
        }
        if (dk.theThang != 0) {
            tiemNang += pt(tiemNang, dk.theThang == 1 ? 100 : 150);
        }
        if (dk.theNam != 0) {
            tiemNang += pt(tiemNang, dk.theNam == 1 ? 200 : 300);
        }
        if (dk.theChiTon) {
            tiemNang += pt(tiemNang, 500);
        }
        if (dk.khauTrang) {
            tiemNang += pt(tiemNang, 20);
        }

        long bua = 0;
        if (dk.buaTriTue) {
            bua += 1;
        }
        if (dk.buaTriTue3) {
            bua += 2;
        }
        if (dk.buaTriTue4) {
            bua += 3;
        }
        if (dk.buaTriTue5) {
            bua += 4;
        }
        if (dk.buaTriTue7) {
            bua += 6;
        }
        if (dk.buaTriTue10) {
            bua += 9;
        }
        if (dk.buaTriTue20) {
            bua += 19;
        }
        tiemNang += goc * bua;

        if (dk.noiTaiTn != 0) {
            tiemNang += pt(tiemNang, dk.noiTaiTn);
        }
        if (dk.chibiTn) {
            tiemNang += goc * 2;
        }
        if (dk.bangCongTn != 0) {
            tiemNang += goc * dk.bangCongTn;
        }
        if (dk.bangBuaTriTue >= 0) {
            tiemNang += pt(goc, Math.min(dk.bangBuaTriTue * 20, 200));
        }
        if (dk.veTinhTriLuc) {
            tiemNang += pt(goc, 20);
        }
        if (dk.vip) {
            tiemNang += goc * 2;
        }
        if (dk.rongXuong3) {
            tiemNang += goc * 3;
        }
        if (dk.duoiKhiTn) {
            tiemNang += goc * 10;
        }
        if (dk.cong20KhiDanhQuai) {
            tiemNang += pt(tiemNang, 20);
        }
        if (dk.setTnPct != 0) {
            tiemNang += goc * dk.setTnPct / 100;
        }

        if (dk.laDeTu && dk.coSuPhu) {
            if (dk.spBuaTnsmDeTu) {
                // Bua TN cho de (vat pham 1628): cong MOT lan, +2 lan gia tri goc.
                //
                // Ban goc cong hai lan: +2 roi ngay duoi mot dong +10 thut le lech
                // han ra ngoai — dau hieu cua mot doan dan them vao sau. Tong la
                // +12, gap sau lan cai ten "bua x2" hua hen.
                tiemNang += goc * 2;
            }
            if (dk.spBuaDeTu) {
                tiemNang += goc * 2;
            }
            if (dk.spBuaDeTu2) {
                tiemNang += goc * 3;
            }
            if (dk.spBuaDeTu3) {
                tiemNang += goc * 4;
            }
            if (dk.spBuaDeTu4) {
                tiemNang += goc * 5;
            }
            if (dk.spBuaDeTu5) {
                tiemNang += goc * 6;
            }
            if (dk.spBuaDeTu7) {
                tiemNang += goc * 8;
            }
            if (dk.spBuaDeTu10) {
                tiemNang += goc * 10;
            }
            if (dk.spBuaDeTu20) {
                tiemNang += goc * 20;
            }
            if (dk.spRongXuong) {
                tiemNang += goc * 3;
            }
            if (dk.spTheTuan != 0) {
                tiemNang += pt(tiemNang, dk.spTheTuan == 1 ? 20 : 50);
            }
            if (dk.spTheThang != 0) {
                tiemNang += pt(tiemNang, dk.spTheThang == 1 ? 100 : 150);
            }
            if (dk.spTheNam != 0) {
                tiemNang += pt(tiemNang, dk.spTheNam == 1 ? 200 : 300);
            }
            if (dk.spTheChiTon) {
                tiemNang += pt(tiemNang, 500);
            }
            if (dk.spCoNPoint) {
                tiemNang += goc / 100 * (dk.spTlTnsmPet + 100);
            }
        }

        if (dk.laBo) {
            tiemNang += goc * 2;
        }
        if (dk.laMe) {
            tiemNang += goc * 2;
        }

        // He so theo BAN DO khong o day: no nam o bang he_so_tnsm, ap trong Mob
        // truoc khi goi ham nay.
        if (dk.laDeTu) {
            double factor = 1.0;
            switch (dk.loaiDeTu) {
                case 2, 3, 4 ->
                    factor = 2.0;
                case 5 ->
                    factor = 4.0;
                default ->
                    factor = 1.0;
            }
            tiemNang = (long) (tiemNang / factor);
        }

        tiemNang *= dk.heSoMayChu;
        tiemNang = giamTheoSucManh(tiemNang, dk.sucManh, dk.gioiHanSucManh);
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        return tiemNang;
    }

    /** Bộ điều kiện của chính nhân vật này, đọc từ trạng thái thật lúc này. */
    public DieuKienTn dieuKienTnHienTai() {
        long now = System.currentTimeMillis();
        DieuKienTn dk = new DieuKienTn();
        dk.sucManh = this.power;
        dk.gioiHanSucManh = getPowerLimit();
        if (this.tlTNSM != null) {
            dk.tlTnsm = new java.util.ArrayList<>(this.tlTNSM);
        }
        if (this.player == null) {
            return dk;
        }
        dk.co = this.player.cFlag;
        if (this.player.isPl()) {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.TNSM);
            if (at != null && !at.isExpired()) {
                dk.thuocTinhMayChu = at.getValue();
            }
        }
        if (this.player.THE_TUAN != 0 && this.player.LASTTIME_THE_TUAN > now) {
            dk.theTuan = this.player.THE_TUAN;
        }
        if (this.player.THE_THANG != 0 && this.player.LASTTIME_THE_THANG > now) {
            dk.theThang = this.player.THE_THANG;
        }
        if (this.player.THE_NAM != 0 && this.player.LASTTIME_THE_NAM > now) {
            dk.theNam = this.player.THE_NAM;
        }
        dk.theChiTon = this.player.THE_CHI_TON != 0 && this.player.LASTTIME_THE_CHI_TON > now;
        dk.khauTrang = this.player.itemTime != null && this.player.itemTime.IsKhauTrang;
        if (this.player.charms != null) {
            dk.buaTriTue = this.player.charms.tdTriTue > now;
            dk.buaTriTue3 = this.player.charms.tdTriTue3 > now;
            dk.buaTriTue4 = this.player.charms.tdTriTue4 > now;
            dk.buaTriTue5 = this.player.charms.tdTriTue5 > now;
            dk.buaTriTue7 = this.player.charms.tdTriTue7 > now;
            dk.buaTriTue10 = this.player.charms.tdTriTue10 > now;
            dk.buaTriTue20 = this.player.charms.tdTriTue20 > now;
        }
        if (this.intrinsic != null && this.intrinsic.id == 24) {
            dk.noiTaiTn = this.intrinsic.param1;
        }
        dk.chibiTn = this.player.effectSkill != null && this.player.effectSkill.isChibi
                && this.player.typeChibi == 2;
        if (this.player.clan != null) {
            if (now <= this.player.clan.LasttimeBuffExp + this.player.clan.TimeStarBuffExp) {
                dk.bangCongTn = this.player.clan.CongTiemNangSucManhToanBangHoi;
            }
            if (now <= this.player.clan.BuaTriTue) {
                dk.bangBuaTriTue = this.player.clan.level;
            }
        }
        dk.veTinhTriLuc = this.player.satellite != null && this.player.satellite.isIntelligent;
        dk.vip = this.player.getSession() != null && this.player.getSession().Vip_Point > 0;
        if (this.player.itemTime != null) {
            dk.rongXuong3 = this.player.itemTime.isRongXuong_3;
            dk.duoiKhiTn = this.player.itemTime.isUseDuoiKhiTNSM;
        }
        dk.cong20KhiDanhQuai = this.Cong20ExpKhiAttackMob;
        dk.setTnPct = phanTramSetTheoLoai("tiem_nang_pct");

        if (this.player.isDeTu) {
            dk.laDeTu = true;
            Detu pet = (Detu) this.player;
            dk.loaiDeTu = pet.typeDeTu;
            Player master = pet.master;
            if (master != null) {
                dk.coSuPhu = true;
                dk.spBuaTnsmDeTu = master.itemTime != null && master.itemTime.isUseBuaTNSMDetu;
                if (master.charms != null) {
                    dk.spBuaDeTu = master.charms.tdDeTu > now;
                    dk.spBuaDeTu2 = master.charms.tdDeTu2 > now;
                    dk.spBuaDeTu3 = master.charms.tdDeTu3 > now;
                    dk.spBuaDeTu4 = master.charms.tdDeTu4 > now;
                    dk.spBuaDeTu5 = master.charms.tdDeTu5 > now;
                    dk.spBuaDeTu7 = master.charms.tdDeTu7 > now;
                    dk.spBuaDeTu10 = master.charms.tdDeTu10 > now;
                    dk.spBuaDeTu20 = master.charms.tdDeTu20 > now;
                }
                dk.spRongXuong = master.itemTime != null && master.itemTime.isRongXuong;
                if (master.THE_TUAN != 0 && master.LASTTIME_THE_TUAN > now) {
                    dk.spTheTuan = master.THE_TUAN;
                }
                if (master.THE_THANG != 0 && master.LASTTIME_THE_THANG > now) {
                    dk.spTheThang = master.THE_THANG;
                }
                if (master.THE_NAM != 0 && master.LASTTIME_THE_NAM > now) {
                    dk.spTheNam = master.THE_NAM;
                }
                dk.spTheChiTon = master.THE_CHI_TON != 0 && master.LASTTIME_THE_CHI_TON > now;
                dk.spCoNPoint = master.nPoint != null;
                if (master.nPoint != null) {
                    dk.spTlTnsmPet = master.nPoint.tlTNSMPet;
                }
            }
        }
        dk.laBo = this.player.isBo;
        dk.laMe = this.player.isMe;
        dk.heSoMayChu = Manager.RATE_EXP_SERVER;
        return dk;
    }

    public long calSucManhTiemNang(long tiemNang) {
        if (player == null || player.zone == null) {
            return 0;
        }
        if (player.zone.map.type == 3) {
            return 0;
        }
        return tinhTiemNang(tiemNang, dieuKienTnHienTai());
    }

    /**
     * Như {@link #calSucManhTiemNang} nhưng <b>bỏ những thứ chỉ tăng tiềm năng
     * cho đệ tử</b> — gốc cho phần sư phụ nhận khi đệ tử đánh.
     *
     * <p>Bùa x2 TNSM đệ tử, bùa đệ tử của Bà Hạt Mít, rồng xương cho đệ, dòng
     * "% TNSM đệ tử" trên đồ sư phụ: mua cho đệ thì chỉ đệ hưởng. Trước đây
     * phần sư phụ chia từ đúng con số đệ vừa nhận — tức là sư phụ ăn ké cả mấy
     * món ấy.</p>
     */
    public long calSucManhTiemNangChoSuPhu(long tiemNang) {
        if (player == null || player.zone == null) {
            return 0;
        }
        if (player.zone.map.type == 3) {
            return 0;
        }
        DieuKienTn dk = dieuKienTnHienTai();
        boDoChiChoDeTu(dk);
        return tinhTiemNang(tiemNang, dk);
    }

    /** Tắt mọi thứ chỉ tăng tiềm năng cho đệ tử trong một bộ điều kiện. */
    public static void boDoChiChoDeTu(DieuKienTn dk) {
        dk.spBuaTnsmDeTu = false;
        dk.spBuaDeTu = false;
        dk.spBuaDeTu2 = false;
        dk.spBuaDeTu3 = false;
        dk.spBuaDeTu4 = false;
        dk.spBuaDeTu5 = false;
        dk.spBuaDeTu7 = false;
        dk.spBuaDeTu10 = false;
        dk.spBuaDeTu20 = false;
        dk.spRongXuong = false;
        dk.spTlTnsmPet = 0;
    }

      private int countItemsHaveAn(int optionId) {
    if (this.player == null
            || this.player.inventory == null
            || this.player.inventory.itemsBody == null) {
        return 0;
    }

    int count = 0;

    for (Item item : this.player.inventory.itemsBody) {
        if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
            continue;
        }

        boolean hasAn = false;
        for (ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == optionId) {
                hasAn = true;
                break;
            }
        }

        if (hasAn) {
            count++;
        }
    }

    return count;
}
// =========================
// TINH ẤN TRANG BỊ
// =========================
private boolean hasFull5TinhAn() {
    return this.setTinhAn >= 5;
}

private boolean hasFull5NguyetAn() {
    return this.setNguyetAn >= 5;
}

private boolean hasFull5NhatAn() {
    return this.setNhatAn >= 5;
}

// Giảm exp theo mốc + giới hạn 20tr
    public static long giamTheoSucManh(long tiemNang, long sucManh, long gioiHanSucManh) {
        if (sucManh >= gioiHanSucManh) {
            return 0;
        }

        // Càng mạnh càng nhận ít — mười một bậc, đi từ nguyên vẹn xuống 1%.
        //
        // Bảng cũ chỉ có bốn bậc và bậc đầu ở tận 40 tỉ: dưới mốc đó thì mạnh
        // hay yếu cũng nhận y như nhau, rồi qua mốc là tụt thẳng một nửa. Người
        // chơi cảm thấy đúng một chuyện — "tự nhiên chững lại" — chứ không thấy
        // một đường dốc.
        //
        // Bảng này áp cho <b>cả sư phụ lẫn đệ tử</b>, vì mỗi bên gọi hàm này
        // bằng sức mạnh CỦA CHÍNH MÌNH: đệ tử gọi lúc tính phần mình, còn phần
        // chia cho sư phụ thì Service.addSMTN gọi lại bằng NPoint của sư phụ.
        // Nên sư phụ mạnh thì phần chia của sư phụ ít đi, không ăn theo bậc của
        // đệ tử nữa.
        //
        // Các mốc nay nằm trong bảng bac_giam_tnsm, sửa được trên panel ở tab
        // Tỉ lệ. Chưa chạm bậc nào thì trả về 100, tức nhận nguyên vẹn.
        int conLai = nro.repository.dao.BacGiamTnsmDAO.phanTram(sucManh);
        if (conLai != 100) {
            tiemNang = pt(tiemNang, conLai);
        }

        // Tỉ lệ đệ tử chia cho sư phụ, chỉnh được trên panel.
        //
        // 100 là hưởng trọn — đúng thiết kế và là mặc định. Trước 08/09/2026
        // mức THẬT đang chạy là 200% vì Service.addSMTN cộng cho sư phụ hai lần
        // mỗi cú đánh của đệ; sửa cho hết đúp thì đặt 200 ở đây là giữ y như cũ.
        long tyLe = nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.TL_DE_TU_CHO_SU_PHU, 100L);
        if (tyLe != 100L && tyLe >= 0L) {
            tiemNang = tiemNang / 100L * tyLe;
        }

        // Giới hạn tối đa 20 triệu.
        //
        // Đặt SAU tỉ lệ trên: trần là chốt an toàn cuối cùng, gõ tỉ lệ to đến
        // mấy cũng không vượt qua được.
        if (tiemNang > 20_000_000L) {
            tiemNang = 20_000_000L;
        }

        return tiemNang;
    }

    public long calSubTNSM(long tiemNang) {
        return giamTheoSucManh(tiemNang, this.power, getPowerLimit());
    }

    public int getTileHutHp(boolean isMob) {
        if (isMob) {
            return (this.tlHutHp + this.tlHutHpMob);
        } else {
            return this.tlHutHp;
        }
    }

    public int getTiLeHutMp() {
        return this.tlHutMp;
    }

    public double subDameInjureWithDeff(double dame) {
        long def = this.def;
        dame -= def;
        if (dame < 0) {
            dame = 1;
        }
        return dame;
    }

    public int getTlGold() {
        return this.tlGold;
    }

    /*------------------------------------------------------------------------*/
    public boolean canOpenPower() {
        return this.power >= getPowerLimit();
    }

    public long getPowerLimit() {
        if (powerLimit != null) {
            return powerLimit.getPower();
        }
        return 0;
    }

    public long getPowerNextLimit() {
        PowerLimit powerLimit = PowerLimitManager.getInstance().get(limitPower + 1);
        if (powerLimit != null) {
            return powerLimit.getPower();
        }
        return 0;
    }

    //**************************************************************************
    //POWER - TIEM NANG
    public void powerUp(long power) {
        this.power += power;
        TaskService.gI().checkDoneTaskPower(player, this.power);
    }

    public void tiemNangUp(long tiemNang) {
        this.tiemNang += tiemNang;
    }

    /**
     * Giá tiềm năng của một lần nâng, tính <b>trước khi nâng</b>.
     *
     * <h2>Vì sao cần một hàm riêng</h2>
     *
     * <p>Năm công thức này trước đây chỉ tồn tại <i>bên trong</i>
     * {@link #increasePoint}, tính xong là tiêu luôn. Nên không chỗ nào nói
     * được cho người chơi biết một lần nâng tốn bao nhiêu — họ chỉ thấy tiềm
     * năng biến mất, hoặc thấy câu "không đủ tiềm năng" mà không biết còn thiếu
     * bao nhiêu.</p>
     *
     * <p>Công thức giữ <b>y nguyên</b>; {@code increasePoint} nay gọi hàm này
     * chứ không tự tính lại, nên hai bên không thể lệch nhau.</p>
     *
     * @param type  0 HP, 1 KI, 2 sức đánh, 3 giáp, 4 chí mạng
     * @param point số điểm muốn nâng
     * @return số tiềm năng phải trả; {@code 0} nếu loại không hợp lệ
     */
    public long giaNangTiemNang(byte type, int point) {
        if (point <= 0) {
            return 0;
        }
        switch (type) {
            case 0: {
                long pointHp = point * 20L;
                return point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
            }
            case 1: {
                long pointMp = point * 20L;
                return point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
            }
            case 2:
                return (long) point * (2L * this.dameg + point - 1) / 2L * 100L;
            case 3:
                return (long) point * (2L * (this.defg + 5) + point - 1) / 2L * 100000L;
            case 4: {
                // Cong dan tung buoc: so nhan la nam nen tong lon rat nhanh, va
                // cong dan thi chan duoc tran long ngay tai buoc gay tran.
                long giaMotDiem = 50_000_000L;
                for (int i = 0; i < this.critg; i++) {
                    if (giaMotDiem > Long.MAX_VALUE / 5L) {
                        return Long.MAX_VALUE;
                    }
                    giaMotDiem *= 5L;
                }
                long tong = 0;
                for (int i = 0; i < point; i++) {
                    if (giaMotDiem >= Long.MAX_VALUE - tong) {
                        return Long.MAX_VALUE;
                    }
                    tong += giaMotDiem;
                    if (giaMotDiem > Long.MAX_VALUE / 5L) {
                        return Long.MAX_VALUE;
                    }
                    giaMotDiem *= 5L;
                }
                return tong;
            }
            default:
                return 0;
        }
    }

    /** Tên loại chỉ số, để in trong câu hỏi xác nhận. */
    public static String tenLoaiChiSo(byte type) {
        switch (type) {
            case 0:
                return "HP gốc";
            case 1:
                return "KI gốc";
            case 2:
                return "Sức đánh";
            case 3:
                return "Giáp";
            case 4:
                return "Chí mạng";
            default:
                return "?";
        }
    }

    /**
     * Bảng giá nâng <b>một điểm</b> của cả năm chỉ số.
     *
     * <p>Dùng cho lệnh chat và cho khung hỏi xác nhận: người chơi cần thấy cả
     * năm con số cùng lúc mới so được nên bỏ tiềm năng vào đâu.</p>
     */
    public String bangGiaMotDiem() {
        StringBuilder sb = new StringBuilder();
        sb.append("Giá nâng 1 điểm:\n");
        for (byte t = 0; t <= 4; t++) {
            sb.append(tenLoaiChiSo(t)).append(": ")
                    .append(Util.soCham(giaNangTiemNang(t, 1)));
            if (t < 4) {
                sb.append('\n');
            }
        }
        sb.append("\nTiềm năng đang có: ").append(Util.soCham(this.tiemNang));
        return sb.toString();
    }

    /**
     * Báo cho <b>người chơi thật</b>, bỏ qua đệ tử, phân thân, bot, boss.
     *
     * <h2>Vì sao cần lọc</h2>
     *
     * <p>Đệ tử tự tiêu tiềm năng bằng {@code Detu.increasePoint()} — hai mươi
     * lượt mỗi giây, và phần lớn số lượt ấy <b>đâm vào trần</b> vì đệ đã đầy
     * một chỉ số nào đó. Mỗi lần đâm trần là một câu "đã đạt mức tối đa" hoặc
     * "không đủ tiềm năng".</p>
     *
     * <p>Những câu ấy nói về đệ tử chứ không phải về sư phụ, mà lại gửi tới sư
     * phụ. Hai mươi câu mỗi giây thì khung chat không còn đọc được gì nữa, và
     * mỗi câu là một gói tin.</p>
     */
    private void baoChoNguoi(Player pl, String chu) {
        if (pl != null && pl.isPl()) {
            Service.gI().sendThongBao(pl, chu);
        }
    }

    /** Nâng từ bao nhiêu điểm trở lên thì hỏi lại. */
    private static final int NGUONG_HOI_LAI = 2;

    /** Yêu cầu nâng đang chờ người chơi bấm Đồng ý. */
    private byte choType = -1;
    private short choPoint;
    private boolean choChoDeTu;

    /**
     * Người chơi bấm nâng tiềm năng — hỏi lại nếu là một lượt nâng lớn.
     *
     * <h2>Vì sao chen thêm một bước</h2>
     *
     * <p>Ô nhập số trong bảng tiềm năng không có bước xác nhận nào. Gõ nhầm một
     * số không là tiêu sạch tiềm năng của cả tuần, và <b>không lấy lại
     * được</b>. Nâng từng điểm (bấm mũi tên) thì giữ nguyên như cũ — hỏi mỗi
     * lần bấm là không ai chịu nổi.</p>
     *
     * <p>Câu hỏi in ra con số <b>chính xác</b> theo định dạng 1.000, cùng số
     * tiềm năng đang có và số còn lại sau khi nâng.</p>
     */
    public void xinNangTiemNang(byte type, short point, boolean choDeTu) {
        if (point <= 0) {
            return;
        }
        if (point < NGUONG_HOI_LAI) {
            increasePoint(type, point, choDeTu);
            return;
        }
        long gia = giaNangTiemNang(type, point);
        if (gia <= 0) {
            increasePoint(type, point, choDeTu);
            return;
        }
        if (this.tiemNang < gia) {
            // Chua du thi khoi hoi — noi thang con thieu bao nhieu.
            Service.gI().sendThongBaoOK(player, "Không đủ tiềm năng.\nNâng "
                    + point + " điểm " + tenLoaiChiSo(type) + " cần "
                    + Util.soCham(gia) + "\nĐang có " + Util.soCham(this.tiemNang)
                    + "\nThiếu " + Util.soCham(gia - this.tiemNang));
            return;
        }
        this.choType = type;
        this.choPoint = point;
        this.choChoDeTu = choDeTu;
        nro.service.NpcService.gI().createMenuConMeo(player,
                nro.core.consts.ConstNpc.XAC_NHAN_NANG_TIEM_NANG, 4028,
                "Nâng " + point + " điểm " + tenLoaiChiSo(type)
                + "\nsẽ tốn " + Util.soCham(gia) + " tiềm năng."
                + "\nĐang có " + Util.soCham(this.tiemNang)
                + "\nCòn lại " + Util.soCham(this.tiemNang - gia)
                + "\n\nBạn có chắc chắn không?",
                "Đồng ý", "Huỷ");
    }

    /** Người chơi vừa trả lời câu hỏi xác nhận. */
    public void traLoiNangTiemNang(boolean dongY) {
        byte t = this.choType;
        short p = this.choPoint;
        boolean de = this.choChoDeTu;
        this.choType = -1;
        this.choPoint = 0;
        if (t < 0 || p <= 0) {
            return;
        }
        if (!dongY) {
            Service.gI().sendThongBao(player, "Đã huỷ, chưa trừ tiềm năng nào.");
            return;
        }
        increasePoint(t, p, de);
    }

    public void increasePoint(byte type, short point, boolean manualForPet) {
        if (player.baovetaikhoan) {
            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
            return;
        }
        if (powerLimit == null) {
            return;
        }
        if (point <= 0) {
            return;
        }
        boolean updatePoint = false;
        long tiemNangUse = 0;
        // Tran RIENG cua de tu, doc tu bang de_tu_chi_so (tab "De tu" tren
        // panel). powerLimit la tran dung chung voi nguoi choi nen rat cao; de
        // tu phai co tran thap hon, khong thi nang mai khong dung.
        //
        // Thu tu bon so: HP/KI, suc danh, giap, chi mang.
        long[] tranDe = null;
        if (this.player != null && this.player.isDeTu) {
            tranDe = nro.repository.dao.DeTuDAO.tranCuaDeTu(
                    ((nro.entity.player.Detu) this.player).typeDeTu);
        }
        // HP gốc và KI gốc chỉ chịu MỘT trần: 600.000.
        //
        // Trước đây còn phải qua powerLimit.getHp()/getMp() — trần theo bậc sức
        // mạnh đọc từ bảng — và bảng đó dừng thấp hơn 600.000 nhiều, nên con số
        // 600.000 viết trong mã không ai với tới được. Hai chỉ số này nay lấy
        // đúng trần cứng, không hỏi bảng nữa.
        //
        // Sức đánh, giáp, chí mạng ở dưới GIỮ NGUYÊN powerLimit: chúng đổi
        // thẳng ra sát thương nên phải lên theo bậc.
        if (type == 0) {
            int pointHp = point * 20;
            tiemNangUse = giaNangTiemNang(type, point);
            // De tu dung TRAN CUA LOAI NO, khong lay min voi tran cua nguoi
            // choi.
            //
            // Tran theo loai da nhan he so roi: de thuong 600.000 thi Mabu
            // 630.000, Cell 661.500, Bill 694.575. Lay min voi con so cung
            // 600.000 la cat phang phan hon ay di, va ca he thong loai de
            // thanh vo nghia o dung cho nguoi choi de y nhat — muc cao nhat.
            long tran = (tranDe != null) ? tranDe[0] : TRAN_HP_GOC;
            if (this.hpg + pointHp <= tran) {
                if (doUseTiemNang(tiemNangUse)) {
                    hpg += pointHp;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "HP gốc đã đạt mức tối đa ("
                        + Util.soCham(tran)
                        + "), đang có "
                        + Util.soCham(this.hpg) + ".");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 1) {
            int pointMp = point * 20;
            tiemNangUse = giaNangTiemNang(type, point);
            long tran = (tranDe != null) ? tranDe[0] : TRAN_KI_GOC;
            if (this.mpg + pointMp <= tran) {
                if (doUseTiemNang(tiemNangUse)) {
                    mpg += pointMp;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "KI gốc đã đạt mức tối đa ("
                        + Util.soCham(tran)
                        + "), đang có "
                        + Util.soCham(this.mpg) + ".");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 2) {
            tiemNangUse = giaNangTiemNang(type, point);
            // De tu: chi mot tran duy nhat, cua LOAI no.
            //
            // powerLimit la tran theo bac suc manh cua NGUOI CHOI, ma de tu
            // co bang tran rieng (de_tu_chi_so) voi he so tung loai. Hoi ca
            // hai thi cai nao thap hon thang, va bang tren panel mat tac dung.
            boolean duocNang = (tranDe != null)
                    ? (this.dameg + point <= tranDe[1])
                    : ((this.dameg + point) <= powerLimit.getDamage()
                            && this.dameg + point <= TRAN_SUC_DANH_GOC);
            if (duocNang) {
                if (doUseTiemNang(tiemNangUse)) {
                    dameg += point;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "Sức đánh của bạn đã đạt mức tối đa"
                        + (tranDe != null ? " (" + Util.soCham(tranDe[1]) + ")" : ""));
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 3) {
            // Cong don gia cua TUNG lan nang, khong lay gia mot lan roi thoi.
            //
            // Ban truoc tinh (defg + 5) * 100000 — dung gia cua DUNG MOT diem —
            // roi ap cho ca `point` diem, nen nang mot luc tram diem giap chi
            // phai tra gia cua diem dau tien. Hai loai type 0/1/2 ngay tren deu
            // tinh theo tong cap so cong; day la cho duy nhat bo sot.
            //
            // Tong cua point so hang, so hang thu k la (defg + 5 + k) * 100000.
            tiemNangUse = giaNangTiemNang(type, point);
            boolean duocNangGiap = (tranDe != null)
                    ? (this.defg + point <= tranDe[2])
                    : ((this.defg + point) <= powerLimit.getDefense());
            if (duocNangGiap) {
                if (doUseTiemNang(tiemNangUse)) {
                    defg += point;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "Giáp của bạn đã đạt mức tối đa"
                        + (tranDe != null ? " (" + Util.soCham(tranDe[2]) + ")" : ""));
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 4) {
            // Cong don gia cua TUNG lan nang, khong lay gia mot lan roi thoi.
            //
            // Gia moi diem gap NAM lan diem truoc (50 trieu x 5^critg). Ban truoc
            // tinh gia cua dung diem dau roi ap cho ca `point` diem, nen nang mot
            // luc muoi diem chi mang re hon nang tung diem tam trieu lan.
            //
            // Cong dan tung buoc chu khong dung cong thuc tong cap so nhan: so
            // nhan la 5 nen tong lon rat nhanh, va cong dan thi chan duoc tran
            // long ngay tai buoc gay tran. Tran thi de tiemNangUse bang gia tri
            // lon nhat, va doUseTiemNang se tu bao khong du tiem nang.
            tiemNangUse = giaNangTiemNang(type, point);
            boolean duocNangCrit = (tranDe != null)
                    ? (this.critg + point <= tranDe[3])
                    : ((this.critg + point) <= powerLimit.getCritical());
            if (duocNangCrit) {
                if (doUseTiemNang(tiemNangUse)) {
                    critg += point;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "Chí mạng của bạn đã đạt mức tối đa"
                        + (tranDe != null ? " (" + tranDe[3] + "%)" : ""));
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (updatePoint) {
            Service.gI().point(player);
        }
        if (manualForPet) {
            if (player.Detu != null) {
                Service.gI().InfoPetGoc(player);
                Service.gI().showInfoPet(player);
                Service.gI().point(player);
            }
        }
    }

    private boolean doUseTiemNang(long tiemNang) {
        if (this.tiemNang < tiemNang) {
            // De tu tu tieu tiem nang moi giay, va phan lon so luot ay het
            // tiem nang — bao ra la hai muoi cau moi giay gui toi su phu, noi
            // ve mot chuyen khong phai cua su phu.
            if (player == null || !player.isPl()) {
                return false;
            }
            // Noi ro CAN bao nhieu, DANG CO bao nhieu, THIEU bao nhieu.
            //
            // Cau cu chi co "Ban khong du tiem nang" — dung nhung vo dung: gia
            // moi diem tang dan, nen nguoi choi khong the tu tinh ra minh thieu
            // bao nhieu de biet phai cay them bao lau.
            Service.gI().sendThongBaoOK(player, "Không đủ tiềm năng.\nCần "
                    + Util.soCham(tiemNang)
                    + "\nĐang có "
                    + Util.soCham(this.tiemNang)
                    + "\nThiếu "
                    + Util.soCham(tiemNang - this.tiemNang));
            return false;
        }
        if (this.tiemNang >= tiemNang && this.tiemNang - tiemNang >= 0) {
            this.tiemNang -= tiemNang;
            TaskService.gI().checkDoneTaskUseTiemNang(player);
            return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------
    private long lastTimeHoiPhuc;
    private long lastTimeHoiPhuc10s;
    private long lastTimeHoiStamina;

    public void update() {
        if (player != null && player.effectSkill != null) {
            if (player.effectSkill.isCharging && player.effectSkill.countCharging < 10) {
                int tiLeHoiPhuc = SkillUtil.getPercentCharge(player.playerSkill.skillSelect.point);
                if (player.effectSkill.isCharging && !player.isDie() && !player.effectSkill.isHaveEffectSkill() && (hp < hpMax || mp < mpMax)) {
                    long hpRecovered = hpMax / 100 * tiLeHoiPhuc;
                    long mpRecovered = mpMax / 100 * tiLeHoiPhuc;
                    // Set co the tang luong hoi phuc khi gong (loai tai_tao_pct).
                    int tlTaiTao = nro.repository.dao.SetBonusDAO.phanTramLoai(
                            player.setClothes, "tai_tao_pct")
                            // Ma moi: hieu luc chieu Tai Tao (skill_power_pct).
                            + nro.repository.dao.SetBonusDAO.tongTheoChieu(
                                    player, "skill_power_pct", Skill.TAI_TAO_NANG_LUONG);
                    if (tlTaiTao != 0) {
                        hpRecovered += hpRecovered * tlTaiTao / 100;
                        mpRecovered += mpRecovered * tlTaiTao / 100;
                    }

                    PlayerService.gI().hoiPhuc(player, Util.CrisGH(hpRecovered), Util.CrisGH(mpRecovered));

                    if (player.effectSkill.countCharging % 3 == 0) {
                        Service.gI().chat(player, "Phục hồi năng lượng " + getCurrPercentHP() + "%");
                    }
                } else {
                    EffectSkillService.gI().stopCharge(player);
                }
                if (++player.effectSkill.countCharging >= 10) {
                    EffectSkillService.gI().stopCharge(player);
                }
            }

            if (Util.canDoWithTime(lastTimeHoiPhuc, 30000)) {
                PlayerService.gI().hoiPhuc(this.player, Util.CrisGH(hpHoi), Util.CrisGH(mpHoi));
                this.lastTimeHoiPhuc = System.currentTimeMillis();
            }
            if (Util.canDoWithTime(lastTimeHoiPhuc10s, 10000)) {
                PlayerService.gI().hoiPhuc(this.player, tlHpHoiBanthan_DongMinh, 0);
                this.lastTimeHoiPhuc10s = System.currentTimeMillis();
            }
            if (Util.canDoWithTime(lastTimeHoiStamina, 60000) && this.stamina < this.maxStamina) {
                this.stamina++;
                this.lastTimeHoiStamina = System.currentTimeMillis();

                if (!this.player.isBoss && !this.player.isDeTu && !this.player.isBo && !this.player.isMe && !this.player.isNguoiYeu && !this.player.isConOne && !this.player.isConTwo && !this.player.isConThree) {
                    PlayerService.gI().sendCurrentStamina(this.player);
                }
            }
        }
        //hồi phục 30s
        //hồi phục thể lực
    }

    public long getFullTN() {
        long tnhp = 0, tnki = 0, tnsd = 0, tng = 0, tncm = 0;

        if (hpg > 0) {
            tnhp = (((hpg / 20L) * (50L + (50L + (hpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (mpg > 0) {
            tnki = (((mpg / 20L) * (50L + (50L + (mpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (dameg > 0) {
            tnsd = ((dameg * (dameg - 1L) * 100L) / 2L);
        }
        if (defg > 0) {
            tng = ((defg * (500000L + (500000L + (defg - 1L) * 100000L))) / 2L);
        }
        if (critg > 0) {
            tncm = ((50L * (((long) Math.pow(5L, critg) - 1L)) / (5L - 1L) * 1000000L));
        }
        return tnhp + tnki + tnsd + tng + tncm;
    }

    public void dispose() {
        this.intrinsic = null;
        this.player = null;
        this.tlHp = null;
        this.tlMp = null;
        this.tlDef = null;
        this.tlDame = null;
        this.tlDameAttMob = null;
        this.tlTNSM = null;
        this.tlSpeed = null;
    }
}
