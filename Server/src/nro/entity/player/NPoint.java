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
    
            Card card = player.Cards.stream().filter(r -> r != null && r.Used == 1).findFirst().orElse(null);
            if (card != null) {
                for (OptionCard io : card.Options) {
                    if (io.active == card.Level || (card.Level == -1 && io.active == 0)) {
                        switch (io.id) {
                            case 0: //Tấn công +#
                                this.dameAdd += io.param;
                                break;
                            case 2: //HP, KI+#000
                                this.hpAdd += io.param * 1000;
                                this.mpAdd += io.param * 1000;
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
                                break;
                            case 7: //KI+#
                                this.mpAdd += io.param;
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
                                break;
                            case 22: //HP+#K
                                this.hpAdd += io.param * 1000;
                                break;
                            case 23: //MP+#K
                                this.mpAdd += io.param * 1000;
                                break;
                            case 24:
                                this.wearingBuiBui = true;
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
                            case 47: //Giáp+#
                                this.defAdd += io.param;
                                break;
                            case 48: //HP/KI+#
                                this.hpAdd += io.param;
                                this.mpAdd += io.param;
                                break;
                            case 49: //Tấn công+#%
                            case 50: //Sức đánh+#%
                                this.tlDame.add(io.param);
                                break;
                            case 77: //HP+#%
                                this.tlHp.add(io.param);
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
                            case 116: //Kháng thái dương hạ san
                                this.khangTDHS = true;
                                break;
                            case 153:
                                this.tlBom += io.param;
                                break;
                            case 117: //Đẹp +#% SĐ cho mình và người xung quanh
                                if (io.param > this.tlSexyDame) {
                                    this.tlSexyDame = io.param;
                                }
                                break;
                            case 147: //+#% sức đánh
                                this.tlDame.add(io.param);
                                break;
                            case 155: //Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                                this.tlSubSD += 50;
                                this.tlSubHP += 50;
                                this.tlSubMP += 50;
                                this.tlTNSM.add(io.param);
                                this.tlGold += io.param;
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
                            case 163:
                                this.isHoaBiNgoXungQuanh = true;
                                break;
                            case 173: //Phục hồi #% HP và KI cho đồng đội
                                this.tlHpHoiBanThanVaDongDoi += io.param;
                                this.tlMpHoiBanThanVaDongDoi += io.param;
                                break;
                            case 236: //may mắn
                                this.tlMayman += io.param;
                                break;
                            case 258:
                                if (io.param > this.tlCoolDame) {
                                    this.tlCoolDame = io.param;
                                }
                                break;
                            case 259: //HP+#%/10s
                                this.tlHpHoiBanthan_DongMinh += io.param;
                                break;
                            case 226:
                                if (io.param > this.tlCuteAddame) {
                                    this.tlCuteAddame = io.param;
                                }
                                break;
                            case 227:
                                this.tlFixStun += io.param;
                                break;
                        }
                    }
                }
            }
    
            // Bông tai cấp 2
            if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                this.player.inventory.itemsBag.stream().filter(it -> it.isNotNullItem() && it.template.id == 921).findFirst().ifPresent(btc2 -> {
                    for (ItemOption io : btc2.itemOptions) {
                        addOption(io);
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
                        addOption(io);
                        if (io.optionTemplate.id == 72) {
                            this.levelBT = io.param;
                        }
                    }
                });
            }
    
            if (BagesTemplate.sendListItemOption(player) != null) {
                for (ItemOption io : BagesTemplate.sendListItemOption(player)) {
                    addOption(io);
                }
            }
    
            for (Item item : this.player.inventory.itemsBody) {
                if (item.isNotNullItem()) {
                    if (item.template.id >= 592 && item.template.id <= 594) {
                        teleport = true;
                    }
                    for (ItemOption io : item.itemOptions) {
                        addOption(io);
                    }
                }
            }
            setDameTrainArmor();
            setBasePoint();
            setOutfitFusion();
            apDungSetTuCauHinh();
            congHpDeTuKhiHopThe();
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
     * <p>Giữ nguyên như cũ: đệ tử loại 1 cho 120% máu của nó, loại 5 cho 140%,
     * còn lại 100%.</p>
     */
    private void congHpDeTuKhiHopThe() {
        if (this.player.Detu == null
                || this.player.fusion.typeFusion == ConstPlayer.NON_FUSION) {
            return;
        }
        long hpDe = this.player.Detu.nPoint.hpMax;
        if (hpDe <= 0) {
            return;
        }
        if (this.player.Detu.typeDeTu == 1) {
            this.hpMax += hpDe * 20 / 100L;
        } else if (this.player.Detu.typeDeTu == 5) {
            this.hpMax += hpDe * 40 / 100L;
        }
        this.hpMax += hpDe;
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
                    apDungMotDong(b);
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

    /** Cộng một dòng chỉ số set vào chỉ số hiện tại. */
    private void apDungMotDong(nro.repository.dao.SetBonusDAO.Bonus b) {
        long v = b.giaTri;
        switch (b.loai) {
            case "hp":
                this.hpMax += v;
                break;
            case "hp_pct":
                this.hpMax += calPercent(this.hpMax, (int) v);
                break;
            case "ki":
                this.mpMax += v;
                break;
            case "ki_pct":
                this.mpMax += calPercent(this.mpMax, (int) v);
                break;
            case "dame":
                this.dame += v;
                break;
            case "dame_pct":
                this.dame += calPercent(this.dame, (int) v);
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

    private void addOption(ItemOption io) {
        switch (io.optionTemplate.id) {
            case 0: //Tấn công +#
                this.dameAdd += io.param;
                break;
            case 2: //HP, KI+#000
                this.hpAdd += io.param * 1000;
                this.mpAdd += io.param * 1000;
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
                break;
            case 7: //KI+#
                this.mpAdd += io.param;
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
                break;
            case 22: //HP+#K
                this.hpAdd += io.param * 1000;
                break;
            case 23: //MP+#K
                this.mpAdd += io.param * 1000;
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
                break;
            case 49: //Tấn công+#%
            case 50: //Sức đánh+#%
                this.tlDame.add(io.param);
                break;
            case 77: //HP+#%
                this.tlHp.add(io.param);
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
                }
                break;
            case 147: //+#% sức đánh
                this.tlDame.add(io.param);
                break;
            case 155: //Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                this.tlSubSD += 50;
                this.tlSubHP += 50;
                this.tlSubMP += 50;
                this.tlTNSM.add(io.param);
                this.tlGold += io.param;
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
            case 176: //
                setInfoOption176();
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
                }
                break;
            case 259: //HP+#%/10s
                this.tlHpHoiBanthan_DongMinh += io.param;
                break;
            case 226:
                if (io.param > this.tlCuteAddame) {
                    this.tlCuteAddame = io.param;
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
        if (this.player.effectSkin.isSlow) {
            this.speed = 1;
        }
    }

    private void setInfoOption176() {
        if (player.isPl()) {
            this.tlDame.add(10);
            speed = (byte) (5 + 3 * (50 / 100));
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
                    this.tlSubSD += ItemService.gI().getPercentTrainArmor(gtl);
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
                                this.tlDame.add(ItemService.gI()
                                        .getPercentTrainArmor(this.player.inventory.trainArmor));
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
        // Tính toán giới hạn hpMax
        long hpMax = this.hpg + this.hpAdd;
//
//        for (int tl : new ArrayList<>(this.tlHp)) {
//            hpMax += (hpMax * tl / 100L);
//        }
        for (Integer tl : this.tlHp) {
            if (tl != null) {
                hpMax += (hpMax * tl / 100L);
            }
        }
         // Tinh ấn
        if (hasFull5TinhAn()) {
            hpMax += calPercent(hpMax, 15);
        }

        if (this.player.isPl()) {
            if (InventoryService.gI().findItemRongNhi(this.player)) {
                hpMax += calPercent(hpMax, 1);
            }
            hpMax += calPercent(hpMax, InventoryService.gI().HpItemsInBoxCollection(this.player));
        }

        if (this.player.tlHpClanAdd > 0) {
            hpMax += calPercent(hpMax, this.player.tlHpClanAdd);
        }
        if (this.player.isPhanThan) {
            hpMax = calPercent(((PhanThan) this.player).master.nPoint.hpMax, SkillUtil.getPercentPhanThan(player));
        }
        if (this.player.THE_TUAN == 1 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 3);
        }
        if (this.player.THE_TUAN == 2 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 5);
        }
        if (this.player.THE_THANG == 1 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 7);
        }
        if (this.player.THE_THANG == 2 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 10);
        }
        if (this.player.THE_NAM == 1 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 15);
        }
        if (this.player.THE_NAM == 2 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 18);
        }
        if (this.player.THE_CHI_TON == 1 && this.player.LASTTIME_THE_CHI_TON > System.currentTimeMillis()) {
            hpMax += calPercent(hpMax, 20);
        }
        if (this.player.isPl() && this.player.isUseDanhHieu_ThienTu == true && this.player.LastTimeDanhHieu_ThienTu > 0) {
            hpMax += calPercent(hpMax, 5);
        }


        // Xử lý set nappa


        if (this.player.effectSkill != null && this.player.effectSkill.isVirus) {
            hpMax -= calPercent(hpMax, 10);
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isBongTuyet) {
            hpMax -= calPercent(hpMax, 20);
        }

        //set worldcup

        if (this.player.itemTime != null && this.player.itemTime.isUseRocket1h) {
            hpMax += calPercent(hpMax, 20);
        }

        if (this.player.itemTime != null && this.player.itemTime.isRongXuong_2) {
            hpMax += calPercent(hpMax, 15);
        }

        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isDeTu || (this.player.isDeTu && ((Detu) this.player).status != Detu.FUSION)) {
                int percent = SkillUtil.getPercentHpMonkey(player.effectSkill.levelMonkey);
                hpMax += (hpMax * percent / 100);
            }
        }

        if (this.player.isPlMan()) {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.HP);
            if (at != null && !at.isExpired()) {
                hpMax += calPercent(hpMax, at.getValue());
            }
        }

        //phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            hpMax *= this.player.effectSkin.xHPKI;
        }

        // HP của đệ tử khi hợp thể KHÔNG cộng ở đây nữa — xem congHpDeTuKhiHopThe().
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            hpMax += calPercent(hpMax, 5);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            hpMax += calPercent(hpMax, 10);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
            hpMax += calPercent(hpMax, 15);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5) {
            hpMax += calPercent(hpMax, 20);
        }
        //huýt sáo
        if (!this.player.isDeTu || (this.player.isDeTu && ((Detu) this.player).status != Detu.FUSION)) {
            if (this.player.effectSkill.tiLeHPHuytSao != 0) {
                hpMax += (hpMax * this.player.effectSkill.tiLeHPHuytSao / 100L);
            }
        }
        //bổ huyết
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet) {
            hpMax *= 2;
        }

        // Xử lý chibi
        if (this.player.effectSkill != null && this.player.effectSkill.isChibi && this.player.typeChibi == 3) {
            hpMax *= 2;
        }
        if (player.getBuff() == Buff.BUFF_HP) {
            hpMax += calPercent(hpMax, 20);
        }
        //TOP WHIS
        List<Player> list = TopKillWhisManager.getInstance().getList();
        if (!list.isEmpty() && list.size() > 2 && this.player.isPl()) {
            if (list.size() >= 5 && this.player.isPl()) {
                int playerRank = getPlayerRank(list, this.player);
                if (playerRank == 1) {
                    hpMax += calPercent(hpMax, 30);
                } else if (playerRank == 2) {
                    hpMax += calPercent(hpMax, 20);
                } else if (playerRank == 3) {
                    hpMax += calPercent(hpMax, 10);
                } else if (playerRank >= 4 && playerRank <= 5) {
                    hpMax += calPercent(hpMax, 5);
                } else if (playerRank >= 6 && playerRank <= 10) {
                    hpMax += calPercent(hpMax, 3);
                }
            }
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            hpMax += 1_000_000;
        }
        //giảm hp
        hpMax -= (hpMax * tlSubHP / 100);
        //hồng đào
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao0) {
            hpMax -= calPercent(hpMax, 99);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao) {
            hpMax += calPercent(hpMax, 1);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao1) {
            hpMax += calPercent(hpMax, 2);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao3) {
            hpMax += calPercent(hpMax, 3);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao5) {
            hpMax += calPercent(hpMax, 5);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao10) {
            hpMax += calPercent(hpMax, 8);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao25) {
            hpMax += calPercent(hpMax, 12);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao50) {
            hpMax += calPercent(hpMax, 15);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao99) {
            hpMax += calPercent(hpMax, 20);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao999) {
            hpMax += calPercent(hpMax, 100);
        }

        if (this.player.itemTime != null && this.player.itemTime.istrbhp) {
            hpMax += calPercent(hpMax, 30);
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbhpxd) {
            hpMax += calPercent(hpMax, 15);
        }

        // Xử lý ngọc rồng đen 2 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            hpMax += (hpMax * RewardBlackBall.R2S_1 / 100);
        }

        // item sieu cawsp
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2.2;
        }
        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            hpMax /= 2;
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapChristMasEvent(this.player.zone.map.mapId) && !this.isKhongLanh) {
            hpMax /= 2;
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMap5000NamTruoc(this.player.zone.map.mapId)) {
            hpMax -= calPercent(hpMax, 90);
        }

        if (player.gender == ConstPlayer.XAYDA) {
            if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCereal(this.player.zone.map)) {
                hpMax /= 2;
            }
        }

        if (this.player.itemTime != null && this.player.itemTime.IsSupbihacam) {
            hpMax += calPercent(hpMax, 10);
        }

        if (this.player.itemTime != null && this.player.itemTime.istomtambot) {
            hpMax += calPercent(hpMax, 5);
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
        // Tính toán giới hạn mpMax
        long mpMax = this.mpg + this.mpAdd;

        // Áp dụng các yếu tố ảnh hưởng đến mpMax
//        for (Integer tl : this.tlMp) {
//            mpMax += (mpMax * tl / 100L);
//        }
for (Integer tl : this.tlMp) {
    if (tl != null) {
        mpMax += (mpMax * tl / 100L);
    }
}
// nhật ấn
if (hasFull5NhatAn()) {
    mpMax += calPercent(mpMax, 15);
}



        // Xử lý set picolo

        if (this.player.isPl()) {
            if (InventoryService.gI().findItemRongNhi(this.player)) {
                mpMax += calPercent(mpMax, 1);
            }
            mpMax += calPercent(mpMax, InventoryService.gI().MpItemsInBoxCollection(this.player));
        }

        if (this.player.tlMpClanAdd > 0) {
            mpMax += calPercent(mpMax, this.player.tlMpClanAdd);
        }
        if (this.player.isPhanThan) {
            mpMax = calPercent(((PhanThan) this.player).master.nPoint.mpMax, SkillUtil.getPercentPhanThan(player));
        }
        if (this.player.THE_TUAN == 1 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 3);
        }
        if (this.player.THE_TUAN == 2 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 5);
        }
        if (this.player.THE_THANG == 1 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 7);
        }
        if (this.player.THE_THANG == 2 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 10);
        }
        if (this.player.THE_NAM == 1 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 15);
        }
        if (this.player.THE_NAM == 2 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 18);
        }
        if (this.player.THE_CHI_TON == 1 && this.player.LASTTIME_THE_CHI_TON > System.currentTimeMillis()) {
            mpMax += calPercent(mpMax, 20);
        }
        if (this.player.isPl() && this.player.isUseDanhHieu_ThienTu == true && this.player.LastTimeDanhHieu_ThienTu > 0) {
            mpMax += calPercent(mpMax, 5);
        }
        if (player.getBuff() == Buff.BUFF_KI) {
            mpMax += calPercent(mpMax, 20);
        }


        if (this.player.itemTime != null && this.player.itemTime.isRongXuong_2) {
            mpMax += calPercent(mpMax, 15);
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isBongTuyet) {
            mpMax -= calPercent(mpMax, 20);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            mpMax += 1_000_000;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseRocket1h) {
            mpMax += calPercent(mpMax, 20);
        }

        // Xử lý ngọc rồng đen 6 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[5] > System.currentTimeMillis()) {
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100);
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100L);
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isVirus) {
            mpMax -= calPercent(mpMax, 10);
        }

        //set worldcup

         if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 1) {
                mpMax += this.player.Detu.nPoint.mpMax *  20 / 100L;
            }
        }
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 5) {
                mpMax += this.player.Detu.nPoint.mpMax * 40/ 100L;
            }
        }

        //hợp thể
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            mpMax += this.player.Detu.nPoint.mpMax;
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            mpMax += calPercent(mpMax, 5);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            mpMax += calPercent(mpMax, 10);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
            mpMax += calPercent(mpMax, 15);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5) {
            mpMax += calPercent(mpMax, 20);
        }
        //bổ khí
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi) {
            mpMax *= 2;
        }
        if (this.player.isPlMan()) {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.KI);
            if (at != null && !at.isExpired()) {
                mpMax += calPercent(mpMax, at.getValue());
            }
        }
        //WHIS
        List<Player> list = TopKillWhisManager.getInstance().getList();
        if (!list.isEmpty() && list.size() > 2 && this.player.isPl()) {
            if (list.size() >= 5 && this.player.isPl()) {
                int playerRank = getPlayerRank(list, this.player);
                if (playerRank == 1) {
                    mpMax += calPercent(mpMax, 30);
                } else if (playerRank == 2) {
                    mpMax += calPercent(mpMax, 20);
                } else if (playerRank == 3) {
                    this.hpMax += calPercent(mpMax, 10);
                } else if (playerRank >= 4 && playerRank <= 5) {
                    mpMax += calPercent(mpMax, 5);
                } else if (playerRank >= 6 && playerRank <= 10) {
                    mpMax += calPercent(mpMax, 3);
                }
            }
        }

        //hồng đào
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao0) {
            mpMax -= calPercent(mpMax, 99);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao) {
            mpMax += calPercent(mpMax, 1);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao1) {
            mpMax += calPercent(mpMax, 2);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao3) {
            mpMax += calPercent(mpMax, 3);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao5) {
            mpMax += calPercent(mpMax, 5);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao10) {
            mpMax += calPercent(mpMax, 8);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao25) {
            mpMax += calPercent(mpMax, 12);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao50) {
            mpMax += calPercent(mpMax, 15);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao99) {
            mpMax += calPercent(mpMax, 20);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao999) {
            mpMax += calPercent(mpMax, 100);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2.2;
        }
        //giảm mp
        mpMax -= (mpMax * tlSubMP / 100);

        if (this.player.itemTime != null && this.player.itemTime.istrbki) {
            mpMax += calPercent(mpMax, 30);
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbkixd) {
            mpMax += calPercent(mpMax, 15);
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            mpMax /= 2;
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapChristMasEvent(this.player.zone.map.mapId) && !this.isKhongLanh) {
            mpMax /= 2;
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMap5000NamTruoc(this.player.zone.map.mapId)) {
            mpMax -= calPercent(mpMax, 90);
        }

        if (player.gender == ConstPlayer.XAYDA) {
            if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCereal(this.player.zone.map)) {
                mpMax /= 2;
            }
        }

        //phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            mpMax *= this.player.effectSkin.xHPKI;
        }
        //xiên cá
        if (this.player.effectFlagBag.useXienCa) {
            mpMax += calPercent(mpMax, 15);
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
        long dame = this.dameg + this.dameAdd;

//        for (Integer tl : this.tlDame) {
//            dame += (dame * tl / 100L);
//        }
        for (Integer tl : this.tlDame) {
            if (tl != null) {
                dame += (dame * tl / 100L);
            }
        }
        // Nguyệt Ấn: đủ 5 món +15% Sức đánh
        if (hasFull5NguyetAn()) {
            dame += calPercent(dame, 15);
        }

        if (this.player.isPl()) {
            if (InventoryService.gI().findItemRongNhi(this.player)) {
                dame += calPercent(dame, 1);
            }
            dame += calPercent(dame, InventoryService.gI().DamageItemsInBoxCollection(this.player));
        }


        if (this.player.isPhanThan) {
            dame = calPercent(((PhanThan) this.player).master.nPoint.dame, SkillUtil.getPercentPhanThan(player));
        }
        if (this.player.THE_TUAN == 1 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            dame += calPercent(dame, 3);
        }
        if (this.player.THE_TUAN == 2 && this.player.LASTTIME_THE_TUAN > System.currentTimeMillis()) {
            dame += calPercent(dame, 5);
        }
        if (this.player.THE_THANG == 1 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            dame += calPercent(dame, 7);
        }
        if (this.player.THE_THANG == 2 && this.player.LASTTIME_THE_THANG > System.currentTimeMillis()) {
            dame += calPercent(dame, 10);
        }
        if (this.player.THE_NAM == 1 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            dame += calPercent(dame, 15);
        }
        if (this.player.THE_NAM == 2 && this.player.LASTTIME_THE_NAM > System.currentTimeMillis()) {
            dame += calPercent(dame, 18);
        }
        if (this.player.THE_CHI_TON == 1 && this.player.LASTTIME_THE_CHI_TON > System.currentTimeMillis()) {
            dame += calPercent(dame, 20);
        }

        if (this.player.tlDameClanAdd > 0) {
            dame += calPercent(dame, this.player.tlDameClanAdd);
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isVirus) {
            dame -= calPercent(dame, 10);
        }

        dame += (dame * tlSexyDame / 100);

        dame += (dame * tlCoolDame / 100);

        dame += (dame * tlCuteAddame / 100);

        if (this.player.isPl() && this.player.isUseDanhHieu_ThienTu == true && this.player.LastTimeDanhHieu_ThienTu > 0) {
            dame += calPercent(dame, 5);
        }
        if (this.player.itemTime != null && this.player.itemTime.IsDuoiKhi) {
            dame += calPercent(dame, 10);
        }
        if (this.player.getBuff() == Buff.BUFF_ATK) {
            dame += calPercent(dame, 20);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC1) {
            dame += calPercent(dame, 5);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC2) {
            dame += calPercent(dame, 10);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhDeoC3) {
            dame += calPercent(dame, 15);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseTrungThu1Trung) {
            dame += calPercent(dame, 10);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseTrungThu2Trung) {
            dame += calPercent(dame, 15);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseTrungThuDB) {
            dame += calPercent(dame, 20);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseHBTrungThu) {
            dame += calPercent(dame, 25);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseRocket1h) {
            dame += calPercent(dame, 20);
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isBongTuyet) {
            dame -= calPercent(dame, 20);
        }

        if (this.player.itemTime != null && this.player.itemTime.isRongXuong_2) {
            dame += calPercent(dame, 15);
        }

        if (this.player.effectSkin != null && this.player.effectSkin.isThoDaiKa) {
            dame -= calPercent(dame, 15);
        }

        //set worldcup

        //thức ăn
        if (!this.player.isDeTu && this.player.itemTime.isEatMeal || this.player.isDeTu && ((Detu) this.player).master.itemTime.isEatMeal) {
            dame += calPercent(dame, 10);
        }

        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu >= 5) {
                dame += this.player.Detu.nPoint.dame * this.player.getPointfusion().getDameFusion() / 100L;
            }
        }

        //hợp thể
         if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 1) {
                dame += this.player.Detu.nPoint.dame *  20 / 100L;
            }
        }
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (this.player.Detu.typeDeTu == 5) {
                dame += this.player.Detu.nPoint.dame * 40/ 100L;
            }
        }
        if (this.player.Detu != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            dame += this.player.Detu.nPoint.dame;
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            dame += calPercent(dame, 5);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            dame += calPercent(dame, 10);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
            dame += calPercent(dame, 15);
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5) {
            dame += calPercent(dame, 20);
        }
        //cuồng nộ
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo) {
            dame *= 2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao) {
            dame += calPercent(dame, 1);
        }
        // hồng đào
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao0) {
            dame -= calPercent(dame, 99);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao1) {
            dame += calPercent(dame, 2);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao3) {
            dame += calPercent(dame, 3);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao5) {
            dame += calPercent(dame, 5);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao10) {
            dame += calPercent(dame, 8);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao25) {
            dame += calPercent(dame, 12);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao50) {
            dame += calPercent(dame, 15);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao99) {
            dame += calPercent(dame, 20);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseHongDao999) {
            dame += calPercent(dame, 100);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo2) {
            dame *= 2.2;
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbsd) {
            dame += calPercent(dame, 30);
        }
        if (this.player.itemTime != null && this.player.itemTime.istrbsdxd) {
            dame += calPercent(dame, 15);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhTet) {
            dame += calPercent(dame, 15);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBanhTrung) {
            dame += calPercent(dame, 25);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            dame += 10_000;
        }
        if (this.player.isPlMan()) {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.SUC_DANH);
            if (at != null && !at.isExpired()) {
                dame += calPercent(dame, at.getValue());
            }
        }
        //WHIS
        List<Player> list = TopKillWhisManager.getInstance().getList();
        if (!list.isEmpty() && list.size() > 2 && this.player.isPl()) {
            if (list.size() >= 5 && this.player.isPl()) {
                int playerRank = getPlayerRank(list, this.player);
                if (playerRank == 1) {
                    dame += calPercent(dame, 30);
                } else if (playerRank == 2) {
                    dame += calPercent(dame, 20);
                } else if (playerRank == 3) {
                    dame += calPercent(dame, 10);
                } else if (playerRank >= 4 && playerRank <= 5) {
                    dame += calPercent(dame, 5);
                } else if (playerRank >= 6 && playerRank <= 10) {
                    dame += calPercent(dame, 3);
                }
            }
        }

        //SucManhBocPha
        if (player.isPl() && player.playerSkill.skillSelect != null) {
            int tiLeDameSucManhBocPha = SkillUtil.getPercentDameSucManhBocPha(player.playerSkill.skillSelect.point);
            if (this.player.effectSkill.isSUcManhBocPha) {
                dame += (dame * tiLeDameSucManhBocPha / 100);
            }
        }
        //giảm dame
        dame -= (dame * tlSubSD / 100);

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            dame /= 2;
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapChristMasEvent(this.player.zone.map.mapId) && !this.isKhongLanh) {
            dame /= 2;
        }

        if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMap5000NamTruoc(this.player.zone.map.mapId)) {
            dame -= calPercent(dame, 90);
        }

        if (player.gender == ConstPlayer.XAYDA) {
            if (!this.player.isBoss && !this.player.getBot() && this.player.zone != null && MapService.gI().isMapCereal(this.player.zone.map)) {
                dame /= 2;
            }
        }

        // Xử lý ngọc rồng đen 1 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[0] > System.currentTimeMillis()) {
            dame += (dame * RewardBlackBall.R1S_2 / 100);
        }

        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isDeTu || (this.player.isDeTu && ((Detu) this.player).status != Detu.FUSION)) {
                int percent = SkillUtil.getPercentDameMonkey(player.effectSkill.levelMonkey);
                dame += (dame * percent / 100);
            }
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            dame *= this.player.effectSkin.xDame;
        }

        if (this.player.itemTime != null && this.player.itemTime.Ishamburgersau) {
            dame += calPercent(dame, 10);
        }

        if (this.player.itemTime != null && this.player.itemTime.Isthuocmothuong) {
            dame += calPercent(dame, 10);
        }
        if (this.player.itemTime != null && this.player.itemTime.Isthuocmodacbiet) {
            dame += calPercent(dame, 10);
        }
        if (this.player.itemTime != null && this.player.itemTime.iscuarangme) {
            dame += calPercent(dame, 5);
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
            if (this.player.effectSkill.isMonkey) {
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
            isCrit = Util.isTrue(this.crit, ConstRatio.PER100);
        }
    }

    public double getDameAttack(boolean isAttackMob) {
        setIsCrit();
        long dameAttack = this.dame;
        intrinsic = this.player.playerIntrinsic.intrinsic;
        percentDameIntrinsic = 0;
        int percentDameSkill = 0;
        // int chu khong phai byte: set viet cung cho 100 roi cong them
        // cau hinh 100 nua la 200 — tran byte thanh -56, tuc tru sat
        // thuong thay vi cong.
        int percentXDame = 0;
        Skill skillSelect = player.playerSkill.skillSelect;
        if (skillSelect.template.id != Skill.DICH_CHUYEN_TUC_THOI && isCritTele) {
            isCrit = true;
            isCritTele = false;
        }
        switch (skillSelect.template.id) {
            case Skill.DRAGON:
                if (intrinsic.id == 1) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAMEJOKO:
                if (intrinsic.id == 2) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.GALICK:
                if (intrinsic.id == 16) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.ANTOMIC:
                if (intrinsic.id == 17) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.DEMON:
                if (intrinsic.id == 8) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.MASENKO:
                if (intrinsic.id == 9) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.LIEN_HOAN:
                if (intrinsic.id == 13) {
                    percentDameIntrinsic = intrinsic.param1;
                }
//                percentDameSkill = skillSelect.damage;
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAIOKEN:
                if (intrinsic.id == 26) {
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
                        this.player.setClothes, Skill.MAKANKOSAPPO);
                if (tlMakan != 0) {
                    dameSkill += dameSkill * tlMakan / 100;
                }
                return dameSkill;
            case Skill.QUA_CAU_KENH_KHI:
                long hpmob = 0;
                long hppl = 0;

                for (Mob mob : this.player.zone.mobs) {
                    if (!mob.isDie() && Util.getDistance(this.player, mob) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hpmob += mob.point.hp;
                    }
                }

                for (Player pl : this.player.zone.getHumanoids()) {
                    if (!pl.isDie() && this.player.id != pl.id && Util.getDistance(this.player, pl) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hppl += pl.nPoint.hp;
                    }
                }
                long dameqckk = (hpmob * 10 / 100) + (hppl * 10 / 100) + this.dame * 10;

                int tlQckk = phanTramSetTheoLoai("qckk_pct");
                if (tlQckk != 0) {
                    dameqckk += dameqckk * tlQckk / 100;
                }
                // Chieu nay return som — tu ap phan tram theo chieu cua set.
                int tlQckkChieu = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player.setClothes, Skill.QUA_CAU_KENH_KHI);
                if (tlQckkChieu != 0) {
                    dameqckk += dameqckk * tlQckkChieu / 100;
                }

                dameqckk = dameqckk + (Util.nextInt(-5, 5) * dameqckk / 100);

                return dameqckk;
            case Skill.DE_TRUNG:
                int tlDanhThuong = phanTramSetTheoLoai("danh_thuong_pct");
                if (tlDanhThuong != 0) {
                    dameAttack += dameAttack * tlDanhThuong / 100;
                }
                // Chieu nay return som — tu ap phan tram theo chieu cua set.
                int tlDeTrung = nro.repository.dao.SetBonusDAO.phanTramSkill(
                        this.player.setClothes, Skill.DE_TRUNG);
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

        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff && (tlSexyDame == 0 || tlCoolDame == 0 || tlCuteAddame == 0)) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            dameAttack += (dameAttack * tiLeDame / 100L);
        }

        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl / 100);
            }
            if (this.player.isDeTu && ((Detu) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameAttack *= 2;
            }
        }

        dameAfter = 0;

        if (isCrit) {
            dameAttack *= 2;
            dameAttack += (dameAttack * tlSDCM / 100);
        }

        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl / 100);
            }
        }

        // Phan tram sat thuong chieu tu cau hinh set (bang set_bonus, loai
        // skill_pct, tham_so = id chieu). Dat o day — SAU switch — nen ap dung
        // cho MOI ky nang di qua duong tinh chung, khong phai chi 10 chieu duoc
        // liet ke tay. Ba chieu return som ben tren tu ap rieng.
        percentXDame += nro.repository.dao.SetBonusDAO.phanTramSkill(
                this.player.setClothes, skillSelect.template.id);
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
     * <p>Thứ tự các bước và cách làm tròn giữ nguyên xi bản cũ, kể cả những chỗ
     * trông lạ — ví dụ bùa tiềm năng cho đệ tử được cộng hai lần (hai lẫn mười
     * lần giá trị gốc). Đổi những chỗ ấy là đổi cân bằng game, việc riêng.</p>
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

    /** Phần trăm sự kiện tiềm năng toàn máy chủ đang chạy lúc này, 0 nếu không có. */
    public static int phanTramSuKienTnMayChu() {
        try {
            Attribute at = ServerManager.gI().getAttributeManager().find(ConstAttribute.TNSM);
            return at != null && !at.isExpired() ? at.getValue() : 0;
        } catch (Exception ex) {
            return 0;
        }
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
                // Hai lan cong nhu ban goc — xem javadoc cua DieuKienTn.
                tiemNang += goc * 2;
                tiemNang += goc * 10;
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
            if (this.hpg + pointHp <= TRAN_HP_GOC
                    && (tranDe == null || this.hpg + pointHp <= tranDe[0])) {
                if (doUseTiemNang(tiemNangUse)) {
                    hpg += pointHp;
                    updatePoint = true;
                }
            } else {
                long tran = (tranDe == null) ? TRAN_HP_GOC
                        : Math.min(TRAN_HP_GOC, tranDe[0]);
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
            if (this.mpg + pointMp <= TRAN_KI_GOC
                    && (tranDe == null || this.mpg + pointMp <= tranDe[0])) {
                if (doUseTiemNang(tiemNangUse)) {
                    mpg += pointMp;
                    updatePoint = true;
                }
            } else {
                long tran = (tranDe == null) ? TRAN_KI_GOC
                        : Math.min(TRAN_KI_GOC, tranDe[0]);
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
            if ((this.dameg + point) <= powerLimit.getDamage()
                    && this.dameg + point <= TRAN_SUC_DANH_GOC
                    && (tranDe == null || this.dameg + point <= tranDe[1])) {
                if (doUseTiemNang(tiemNangUse)) {
                    dameg += point;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "Sức đánh của bạn đã đạt mức tối đa");
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
            if ((this.defg + point) <= powerLimit.getDefense()
                    && (tranDe == null || this.defg + point <= tranDe[2])) {
                if (doUseTiemNang(tiemNangUse)) {
                    defg += point;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "Giáp của bạn đã đạt mức tối đa");
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
            if ((this.critg + point) <= powerLimit.getCritical()
                    && (tranDe == null || this.critg + point <= tranDe[3])) {
                if (doUseTiemNang(tiemNangUse)) {
                    critg += point;
                    updatePoint = true;
                }
            } else {
                baoChoNguoi(player, "Chí mạng của bạn đã đạt mức tối đa");
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
                            player.setClothes, "tai_tao_pct");
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
