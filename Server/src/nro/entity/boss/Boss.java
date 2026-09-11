package nro.entity.boss;

import nro.entity.boss.legacy.hanhtinhchet.Oren;
import nro.entity.boss.legacy.cumber.Cumber;
import nro.core.consts.ConstPlayer;
import nro.entity.map.Map;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.server.ServerNotify;
import nro.service.effect.EffectSkillService;
import nro.entity.mob.Mob;
import nro.entity.player.Detu;
import nro.entity.boss.api.IBoss;
import nro.service.boss.BrolyManager;
import nro.service.boss.FinalBossManager;
import nro.service.boss.GasDestroyManager;
import nro.service.boss.OtherBossManager;
import nro.service.boss.RedRibbonHQManager;
import nro.service.boss.SkillSummonedManager;
import nro.service.boss.SnakeWayManager;
import nro.service.boss.TreasureUnderSeaManager;
import nro.service.boss.YardartManager;
import nro.service.boss.BossManager;
import nro.service.boss.BossNomalManager;
import nro.service.boss.BossOfTheGangsManager;
import nro.service.MapService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.skill.SkillService;
import nro.service.TaskService;
import nro.service.fun.ChangeMapService;
import nro.core.log.Logger;
import nro.core.util.SkillUtil;
import nro.core.util.Util;
import nro.net.io.Message;
import java.io.IOException;
import java.util.List;
import nro.entity.boss.map.the23rdmartialartcongress.Locopo;

public class Boss extends Player implements IBoss {
    
    //BROLY
    public Player plAttack;
    protected int targetCountChangePlayerAttack;
    protected int countChangePlayerAttack;

    /**
     * Mọi con boss đã dựng, bất kể nằm ở manager nào.
     *
     * <p>Có mười bảy lớp manager (Yardart, Broly, FinalBoss, OtherBoss...) và
     * <b>chỉ {@code BossManager} cho lấy danh sách</b>. Panel muốn biết một
     * mẫu số liệu có con boss nào đang cầm hay không thì phải hỏi đủ, nếu
     * không sẽ báo nhầm là "chưa có trong game".</p>
     *
     * <p>Ghi ngay trong hàm dựng nên không đường sinh boss nào lọt: dù dựng từ
     * bản đồ, từ NPC, hay từ danh sách boss đi kèm.</p>
     */
    private static final java.util.List<Boss> TAT_CA
            = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

    /** Bản sao danh sách mọi boss, an toàn để duyệt. */
    public static java.util.List<Boss> tatCaBoss() {
        synchronized (TAT_CA) {
            return new java.util.ArrayList<>(TAT_CA);
        }
    }

    public int currentLevel = -1;
    public final BossData[] data;
    protected long lastTimeBossSpawn;
    protected long lastTimePlayerAttack;
    protected boolean hasPlayerAttackSinceSpawn;
    public BossStatus bossStatus;

    protected Zone lastZone;

    protected long lastTimeRest;

    /**
     * Số lượt của nhóm vào lúc con boss này nằm xuống.
     *
     * <p>Chỉ có nghĩa khi boss thuộc một nhóm; xem
     * {@code NhomBossService}.</p>
     */
    public long luotNhomLucNghi;
    protected int secondsRest;

    protected long lastTimeChatS;
    protected int timeChatS;
    protected byte indexChatS;
    
    public int idSkillPlayer = -1;
    public Player playertarget;
    public Mob mobTarget;

    protected long lastTimeChatE;
    protected int timeChatE;
    protected byte indexChatE;

    protected long lastTimeChatM;
    protected int timeChatM;

    protected long lastTimeTargetPlayer;
    protected int timeTargetPlayer;
    public Player playerTarger;

    protected Boss parentBoss;
    public Boss[][] bossAppearTogether;

    public Zone zoneFinal = null;

    public Player playerReward;

    public int lv;

    public int error;

    public boolean prepareBom;

    public boolean isNotifyDisabled;
    public boolean isZoneRandomSpawn;
    public boolean isZone02Spawn;
    public boolean isSpawnPlayer;
    
    public Boss(int id, boolean isNotifyDisabled, boolean isZoneRandomSpawn, boolean isZone02Spawn, boolean isSpawnPlayer, BossData... data) throws Exception {
        this(id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZoneRandomSpawn = isZoneRandomSpawn;
        this.isZone02Spawn = isZone02Spawn;
        this.isSpawnPlayer = isSpawnPlayer;
    }

    public Boss(BossType bossType, int id, boolean isNotifyDisabled, boolean isZoneRandomSpawn, boolean isZone02Spawn, boolean isSpawnPlayer, BossData... data) throws Exception {
        this(bossType, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZoneRandomSpawn = isZoneRandomSpawn;
        this.isZone02Spawn = isZone02Spawn;
        this.isSpawnPlayer = isSpawnPlayer;
    }

    public Boss(int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }
        this.data = data;
        apDungCauHinhCSDL(id, this.data);
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        TAT_CA.add(this);
        BossManager.gI().addBoss(this);

        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    Boss boss = BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                    if (boss != null) {
                        boss.parentBoss = this;
                        boss.lv = j;
                        this.bossAppearTogether[i][j] = boss;
                    }
                }
            }
        }
    }

    public Boss(BossType bossType, int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }
        this.data = data;
        apDungCauHinhCSDL(id, this.data);
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        TAT_CA.add(this);
        switch (bossType) {
            case YARDART:
                YardartManager.gI().addBoss(this);
                break;
            case FINAL:
                FinalBossManager.gI().addBoss(this);
                break;
            case SKILLSUMMONED:
                SkillSummonedManager.gI().addBoss(this);
                break;
            case BROLY:
                BrolyManager.gI().addBoss(this);
                break;
            case PHOBAN:
                OtherBossManager.gI().addBoss(this);
                break;
            case PHOBANDT:
                RedRibbonHQManager.gI().addBoss(this);
                break;
            case PHOBANBDKB:
                TreasureUnderSeaManager.gI().addBoss(this);
                break;
            case PHOBANCDRD:
                SnakeWayManager.gI().addBoss(this);
                break;
            case PHOBANKGHD:
                GasDestroyManager.gI().addBoss(this);
                break;
            case PHOBANBBH:
                BossOfTheGangsManager.gI().addBoss(this);
                break;
            case NOMAL:
                BossNomalManager.gI().addBoss(this);
                break;
        }

        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    Boss boss = BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                    if (boss != null) {
                        boss.parentBoss = this;
                        this.bossAppearTogether[i][j] = boss;
                    }
                }
            }
        }
    }
        
    @Override
    public void initBase() {
        BossData data = this.data[this.currentLevel];
        this.name = String.format(data.getName(), Util.nextInt(0, 100));
        this.gender = data.getGender();
        this.nPoint.mpg = 31_07_2002;
        this.nPoint.dameg = data.getDame();
        this.nPoint.hpg = data.getHp()[Util.nextInt(0, data.getHp().length - 1)];
        this.nPoint.hp = nPoint.hpg;
        apChiSoPhuTuCSDL(data);
        this.nPoint.calPoint();
        this.initSkill();
        this.resetBase();
    }

    /**
     * Nạp giáp / né đòn / chính xác của con boss này từ bảng {@code boss_config}.
     *
     * <p>Ba thứ này không sống trong {@link BossData} như HP và sức đánh, mà là
     * chỉ số của {@code nPoint} — nên phải đặt ở đây, trên từng bản boss, thay vì
     * ở {@code apDungCauHinhCSDL} chỗ đè {@code BossData}.</p>
     *
     * <p>Gọi <b>trước</b> {@code calPoint} để lần tính đầu tiên đã có sẵn số.
     * Bản thân ba trường {@code *Boss} không bị {@code calPoint} xoá, nên boss lên
     * cấp hay hồi sinh vẫn giữ được.</p>
     */
    private void apChiSoPhuTuCSDL(BossData data) {
        try {
            // Lop 1: so cua MAU, go o tab "2. So lieu" (bang boss_data).
            if (data != null) {
                if (data.getGiap() >= 0) {
                    this.nPoint.giapBoss = data.getGiap();
                }
                if (data.getNeDon() >= 0) {
                    this.nPoint.neDonBoss = data.getNeDon();
                }
                if (data.getChinhXac() >= 0) {
                    this.nPoint.chinhXacBoss = data.getChinhXac();
                }
            }
            // Lop 2: so de rieng cho tung con, go o tab "1. Dang chay"
            // (bang boss_config). Co thi thang so cua mau.
            nro.repository.dao.BossDAO.Config c =
                    nro.repository.dao.BossDAO.config((int) this.id);
            if (c == null) {
                return;
            }
            if (c.giap != null) {
                this.nPoint.giapBoss = c.giap;
            }
            if (c.neDon != null) {
                this.nPoint.neDonBoss = c.neDon;
            }
            if (c.chinhXac != null) {
                this.nPoint.chinhXacBoss = c.chinhXac;
            }
        } catch (Exception ex) {
            Logger.logException(Boss.class, ex,
                    "Không áp được giáp/né/chính xác cho boss " + this.id);
        }
    }

    protected void initSkill() {
        for (Skill skill : this.playerSkill.skills) {
            skill.dispose();
        }
        this.playerSkill.skills.clear();
        this.playerSkill.skillSelect = null;
        int[][] skillTemps = data[this.currentLevel].getSkillTemp();
        for (int[] skillTemp : skillTemps) {
            Skill skill = SkillUtil.createSkill(skillTemp[0], skillTemp[1]);
            // createSkill tra null khi khong co chieu do, hoac cap vuot so cap
            // that cua chieu. Truoc day gan thang skill.coolDown nen mot dong
            // go sai trong panel la nem NullPointerException ngay giua ham dung
            // boss — boss do khong xuat hien, ma log chi noi NPE chu khong noi
            // chieu nao.
            if (skill == null) {
                Logger.error("Boss " + this.id + ": chiêu " + skillTemp[0]
                        + " cấp " + skillTemp[1] + " không có thật, bỏ qua"
                        + System.lineSeparator());
                continue;
            }
            if (skillTemp.length == 3) {
                skill.coolDown = skillTemp[2];
            }
            this.playerSkill.skills.add(skill);
        }
        // Khong con chieu nao dung duoc thi attack() se boc tu danh sach rong va
        // nem loi moi nhip. Them mot chieu danh thuong de boss van hoat dong.
        if (this.playerSkill.skills.isEmpty()) {
            Skill duPhong = SkillUtil.createSkill(Skill.DRAGON, 1);
            if (duPhong != null) {
                this.playerSkill.skills.add(duPhong);
            }
        }
    }
        
    protected void resetBase() {
        this.lastTimeChatS = 0;
        this.lastTimeChatE = 0;
        this.timeChatS = 0;
        this.timeChatE = 0;
        this.indexChatS = 0;
        this.indexChatE = 0;
    }

    //.outfit.
    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        if (this.id == BossID.oren) {
            if (((Oren) this).isFusion) {
                return 1249;
            }
        }
        
        if (this.id == BossID.LOCOPO) {
            if (((Locopo) this).transformed == true) {
                return 1273;
            }
        }
        return this.data[this.currentLevel].getOutfit()[0];
    }
    
    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        if (this.id == BossID.oren) {
            if (((Oren) this).isFusion) {
                return 1250;
            }
        }
        
        if (this.id == BossID.LOCOPO) {
            if (((Locopo) this).transformed == true) {
                return 1274;
            }
        }
        return this.data[this.currentLevel].getOutfit()[1];
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        if (this.id == BossID.oren) {
            if (((Oren) this).isFusion) {
                return 1251;
            }
        }
        
        if (this.id == BossID.LOCOPO) {
            if (((Locopo) this).transformed == true) {
                return 1275;
            }
        }
        return this.data[this.currentLevel].getOutfit()[2];
    }
    
    @Override
    public short getFlagBag() {
        return oPhucVi(3);
    }

    /**
     * Một ô của mảng trang phục, hoặc {@code -1} nếu mảng ngắn hơn thế.
     *
     * <p>Mảng phải đủ <b>sáu</b> ô: đầu, thân, chân, cờ đeo, hào quang, hiệu
     * ứng trước. Khai thiếu thì ba hàm đọc ô 3/4/5 ném
     * {@code ArrayIndexOutOfBoundsException} ngay giữa {@code joinMap}, khối
     * {@code catch} ở đó đưa boss <b>về lại REST</b> — con boss lặng lẽ không
     * bao giờ xuất hiện, mà log chỉ nói lỗi mảng chứ không nói boss nào.</p>
     */
    private short oPhucVi(int i) {
        try {
            short[] tp = this.data[this.currentLevel].getOutfit();
            return tp != null && i < tp.length ? tp[i] : (short) -1;
        } catch (Exception boQua) {
            return (short) -1;
        }
    }

    @Override
    public byte getAura() {
        return (byte) oPhucVi(4);
    }

    @Override
    public byte getEffFront() {
        return (byte) oPhucVi(5);
    }
    
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0, this.data[this.currentLevel].getMapJoin().length - 1)];
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        return map;
    }

    /**
     * Số giây còn lại tới lúc hồi sinh, hoặc {@code -1} nếu boss không đang chờ.
     *
     * <p>Chỉ để panel hiển thị. {@code lastTimeRest} được đặt ở thời điểm chết,
     * nên đếm ngược = thời gian chờ trừ đi khoảng đã trôi qua.</p>
     */
    public long getSecondsUntilRespawn() {
        if (this.bossStatus != BossStatus.REST && this.bossStatus != BossStatus.DIE) {
            return -1;
        }
        // Con theo nhom dung DONG HO CUA NHOM, khong dung giay_hoi_sinh rieng
        // — vi chinh denLuotHoiSinh() cung xet theo so luot cua nhom. Hien gio
        // rieng o day thi panel dem mot dang ma boss len mot neo.
        nro.service.boss.NhomBossService.Nhom nhom =
                nro.service.boss.NhomBossService.gI().nhomCua((int) this.id);
        if (nhom != null) {
            return nhom.conLaiGiay();
        }
        long troiQua = (System.currentTimeMillis() - this.lastTimeRest) / 1000;
        long conLai = this.secondsRest - troiQua;
        return conLai > 0 ? conLai : 0;
    }

    @Override
    public void changeStatus(BossStatus status) {
        // Tha vat pham cau hinh trong CSDL NGAY TAI DAY chu khong o reward():
        // 112/129 lop boss ghi de reward() ma khong goi super, nen moc vao do
        // gan nhu khong boss nao chay. changeStatus() khong bi lop nao ghi de.
        if (status == BossStatus.DIE && this.bossStatus != BossStatus.DIE) {
            thaVatPhamTheoCauHinh();
            congDiemSanBoss();
        }
        this.bossStatus = status;
    }

    /**
     * Điểm săn boss: hạ boss có máu tối đa từ ngưỡng quy ước trở lên (mặc định
     * 500 triệu) thì người kết liễu được cộng điểm, đổi quà ở NPC Tranh Ngọc
     * Namếc. Thay cho sự kiện Tranh Ngọc Namếc đã bỏ.
     *
     * <p>Người nhận là {@code playerReward} — {@link #die(Player)} đã đổi đệ tử /
     * phân thân thành sư phụ. Gọi từ {@link #changeStatus} như phần thả đồ, vì
     * đó là chỗ duy nhất mọi lớp boss đều đi qua.</p>
     */
    private void congDiemSanBoss() {
        try {
            Player nguoi = this.playerReward;
            if (nguoi == null || !nguoi.isPl() || nguoi.event == null || this.nPoint == null) {
                return;
            }
            long nguong = nro.repository.dao.ConfigDAO.num(
                    nro.repository.dao.ConfigDAO.SAN_BOSS_HP_TOI_THIEU, 500_000_000L);
            long diem = nro.repository.dao.ConfigDAO.num(
                    nro.repository.dao.ConfigDAO.SAN_BOSS_DIEM, 1L);
            if (diem <= 0 || this.nPoint.hpMax < nguong) {
                return;
            }
            nguoi.event.addNamekWarPoint((int) Math.min(diem, 1_000_000L));
            Service.gI().sendThongBao(nguoi, "+" + diem + " điểm săn boss (" + this.name
                    + ") — đang có " + nro.core.util.Util.soCham(nguoi.event.getNamekWarPoint()) + " điểm");
        } catch (Exception ex) {
            Logger.logException(Boss.class, ex, "Lỗi cộng điểm săn boss");
        }
    }

    /**
     * Đè chỉ số boss bằng dòng cấu hình trong bảng {@code boss_config}.
     *
     * <p>Không có dòng nào thì boss giữ nguyên toàn bộ giá trị viết cứng trong
     * mã nguồn — bảng rỗng nghĩa là máy chủ chạy y như trước khi có tính năng
     * này. Từng ô để trống cũng vậy: chỉ ô nào có giá trị mới bị đè.</p>
     *
     * <p>Gọi trong <b>hàm dựng</b>, tức là lúc máy chủ khởi động. Nên sửa chỉ số
     * trên panel phải khởi động lại máy chủ mới có hiệu lực; vật phẩm rơi thì
     * đọc mỗi lần boss chết nên có hiệu lực ngay.</p>
     */
    /**
     * Đặt thời gian chờ hồi sinh dùng chung cho mọi boss.
     *
     * <p>Chạy <b>trước</b> phần đè theo từng con, và bỏ qua ngay khi con đó đã
     * khai riêng {@code seconds_rest} — số riêng luôn thắng số chung. Nếu làm
     * ngược lại thì mọi thiết lập riêng sẽ bị số chung xoá sạch, mà người đặt
     * lại không hề được báo.</p>
     *
     * <p>Để khoá bằng {@code 0} là tắt hẳn, mỗi boss giữ nguyên số gốc viết
     * trong lớp Java của nó.</p>
     */
    private static void apGiayHoiSinhChung(BossData[] data,
            nro.repository.dao.BossDAO.Config c) {
        if (data == null || (c != null && c.secondsRest != null)) {
            return;
        }
        long chung = nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.BOSS_GIAY_HOI_SINH, 0);
        if (chung <= 0) {
            return;
        }
        for (BossData d : data) {
            if (d != null) {
                d.setSecondsRest((int) chung);
            }
        }
    }

    static void apDungCauHinhCSDL(int bossId, BossData[] data) {
        try {
            nro.repository.dao.BossDAO.Config c = nro.repository.dao.BossDAO.config(bossId);
            apGiayHoiSinhChung(data, c);
            if (c == null || data == null) {
                return;
            }
            // Tach rieng tung o: mot gia tri go sai KHONG duoc lam hong ca viec de.
            // Truoc day hai dong nay nam ngoai try rieng, hp sai la nhay thang xuong
            // catch va suc danh / giay hoi sinh cung khong duoc ap dung.
            long[] hp = null;
            int[] maps = null;
            try {
                hp = nro.repository.dao.BossDAO.parseLongs(c.hp);
            } catch (Exception ex) {
                Logger.logException(Boss.class, ex,
                        "Cột hp của boss " + bossId + " sai định dạng, bỏ qua ô này");
            }
            try {
                maps = nro.repository.dao.BossDAO.parseInts(c.mapJoin);
            } catch (Exception ex) {
                Logger.logException(Boss.class, ex,
                        "Cột map_join của boss " + bossId + " sai định dạng, bỏ qua ô này");
            }
            for (BossData d : data) {
                if (d == null) {
                    continue;
                }
                if (c.name != null) {
                    d.setName(c.name);
                }
                if (c.dame != null) {
                    d.setDame(c.dame);
                }
                if (c.secondsRest != null) {
                    d.setSecondsRest(c.secondsRest);
                }
                if (maps != null) {
                    d.setMapJoin(maps);
                }
                if (hp != null) {
                    // Boss nhieu cap co nhieu moc mau. Cau hinh it moc hon so cap
                    // thi chi de nhung moc dau, cac cap sau giu nguyen goc — an
                    // toan hon la cat bot mang lam boss cap cao mat mau.
                    long[] goc = d.getHp();
                    if (goc == null || goc.length <= hp.length) {
                        d.setHp(hp);
                    } else {
                        long[] moi = goc.clone();
                        System.arraycopy(hp, 0, moi, 0, hp.length);
                        d.setHp(moi);
                    }
                }
            }
        } catch (Exception ex) {
            // Cau hinh hong thi boss van phai dung duoc bang so goc, khong duoc
            // lam vo ca qua trinh khoi dong may chu.
            Logger.logException(Boss.class, ex, "Lỗi áp dụng boss_config cho boss " + bossId);
        }
    }

    /**
     * Thả các món trong bảng {@code boss_drop} tại chỗ boss vừa chết.
     *
     * <p>Đây là phần <b>thêm vào</b>, không thay thế: các món viết cứng trong
     * {@code reward()} của từng lớp boss vẫn rơi như cũ.</p>
     *
     * <p>Món rơi <b>không dành riêng cho ai</b> ({@code playerId = -1}). Lý do:
     * điểm móc này không biết ai là người hạ boss — 45 lớp ghi đè {@code die()}
     * và không lớp nào đặt {@code playerReward}. Thả tự do là cách duy nhất
     * không phụ thuộc vào việc lớp con có hợp tác hay không.</p>
     *
     * <p><b>Phạm vi:</b> 10 lớp boss ({@code majinbuu12h/*}, {@code trainingboss/*})
     * chết mà không đi qua {@code changeStatus(DIE)} nên không nhận phần rơi này.</p>
     */
    /**
     * Boss đang trả thưởng trên luồng hiện tại, hoặc {@code null}.
     *
     * <p>Dùng {@link ThreadLocal} chứ không phải một biến tĩnh: nhiều boss ở
     * nhiều khu có thể chết cùng lúc trên các luồng khác nhau, một biến chung sẽ
     * chặn nhầm đồ của boss khác.</p>
     */
    public static final ThreadLocal<Integer> DANG_TRA_THUONG = new ThreadLocal<>();

    /**
     * {@code true} nếu vật phẩm đang được thả là <b>đồ rơi viết cứng của boss</b>
     * — khi đó không thả xuống đất và không báo cho client.
     *
     * <h3>Đồ rơi của boss chỉ đến từ bảng {@code boss_drop}</h3>
     *
     * <p>Toàn bộ đồ rơi viết cứng trong 129 lớp boss đã bị bỏ hẳn: mọi món boss
     * thả giờ chỉ lấy từ bảng cấu hình bên panel. Trước đây có cờ bật/tắt riêng
     * cho từng boss ({@code chan_do_goc}), nhưng để mặc định tắt thì đồ cũ vẫn
     * rơi song song với đồ panel — sửa cấu hình xong vẫn thấy món lạ, không hiểu
     * ở đâu ra.</p>
     *
     * <p>Chỉ trả {@code true} khi đang <b>trong lúc boss trả thưởng</b>
     * ({@link #DANG_TRA_THUONG} khác {@code null}). Ngoài khoảng đó — quái rơi
     * đồ, người chơi bỏ đồ xuống đất, đồ từ panel — không bị ảnh hưởng gì, vì
     * {@code thaVatPhamTheoCauHinh()} chạy sau khi cờ đã được gỡ.</p>
     */
    public static boolean biChanDoGoc() {
        return DANG_TRA_THUONG.get() != null;
    }

    /**
     * Thả {@code soMon} món Thần Linh, mỗi món bốc ngẫu nhiên trong bộ 13 món.
     *
     * <h3>Thả từng món riêng, không dồn số lượng</h3>
     *
     * <p>Trang bị mỗi món chiếm một ô nên số lượng luôn là 1. Đặt số lượng 3 cho
     * một cái áo thì client hiện "Áo Thần Linh x3" trong một ô — nhặt lên là ô
     * đó hỏng. Nên vòng lặp thả ba món riêng, và <b>mỗi món bốc lại</b> nên rơi
     * ra ba món khác nhau chứ không phải ba cái giống hệt.</p>
     *
     * <p>Kèm luôn chỉ số mặc định của đồ Thần Linh — không có thì món rơi ra là
     * trang bị trắng, không cộng gì.</p>
     */
    private void thaThanLinhNgauNhien(int soMon, int x, int y,
            long hsdMin, long hsdMax, long hsdVinhVien, String chiSoCauHinh) {
        short[] bo = nro.repository.dao.BossDAO.DO_THAN_LINH;
        for (int i = 0; i < soMon; i++) {
            short itemId = bo[Util.nextInt(bo.length)];
            nro.entity.map.ItemMap im = new nro.entity.map.ItemMap(this.zone,
                    itemId, 1, x + Util.nextInt(-40, 40), y, chuDoRoi());
            im.chiNguoiGiet = true;
            java.util.List<nro.entity.item.ItemOption> ops =
                    nro.service.item.ItemService.gI().getDefaultOptionTL(itemId);
            if (ops != null && !ops.isEmpty()) {
                im.options = ops;
            }
            apChiSoNgauNhien(im, chiSoCauHinh);
            apHanSuDung(im, hsdMin, hsdMax, hsdVinhVien);
            Service.gI().dropItemMap(this.zone, im);
        }
    }

    /**
     * Gan han su dung (option 93 "HSD # ngay") cho mon do boss vua tha.
     *
     * <p>Chi ap cho TRANG BI ({@code type < 5} hoac {@code type == 32}). Da,
     * ngoc, thuoc... khong co khai niem han su dung nen bo qua.</p>
     *
     * <p>De {@code boss_hsd_min} hoac {@code boss_hsd_max} bang 0 la tat han
     * tinh nang, do roi ra vinh vien y nhu truoc. Khi da bat, moi mon con tung
     * them mot lan theo {@code boss_hsd_vinh_vien} phan tram de duoc mien han.</p>
     */
    /**
     * Gan chi so ngau nhien cho mon do boss vua tha.
     *
     * <p>Dinh dang chuoi: {@code id:min:max} ngan nhau bang dau phay, vi du
     * {@code 50:10:20,77:1:3} nghia la chi so 50 boc ngau nhien tu 10 den 20,
     * chi so 77 tu 1 den 3. Chuoi rong hoac {@code []} la khong gan gi.</p>
     *
     * <p>Dong nao viet sai thi BO QUA rieng dong do, khong lam hong ca mon do:
     * mot dau hai cham go nham khong duoc phep lam boss tha ra do trang.</p>
     */
    private void apChiSoNgauNhien(nro.entity.map.ItemMap im, String cauHinh) {
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
                if (max < min) {
                    int tmp = min;
                    min = max;
                    max = tmp;
                }
                im.addOptionParam(id, min == max ? min : Util.nextInt(min, max));
            } catch (NumberFormatException ignored) {
                // Dong go sai -> bo qua dong nay thoi.
            }
        }
    }

    private void apHanSuDung(nro.entity.map.ItemMap im, long min, long max, long ptVinhVien) {
        try {
            if (im == null || im.itemTemplate == null) {
                return;
            }
            // KHONG loc theo loai do nua. Truoc day chan bang "type < 5 || type == 32"
            // (lay tu CombineUtil, von la dieu kien PHA LE HOA chu khong phai "la trang
            // bi"). Cai trang co type 5 nen bi loai ngay, khien HSD khong bao gio duoc
            // gan va mon nao cung ra vinh vien du da dat ti le.
            // Admin dat HSD cho tung dong roi la da noi ro y dinh — khong can loc ho.
            if (min <= 0 || max <= 0) {
                return;
            }
            if (max < min) {
                long tmp = min;
                min = max;
                max = tmp;
            }
            if (ptVinhVien > 0 && Util.nextInt(1, 100) <= ptVinhVien) {
                return;
            }
            int ngay = (min == max) ? (int) min : Util.nextInt((int) min, (int) max);
            im.addOptionParam(93, ngay);
        } catch (Exception ex) {
            Logger.logException(Boss.class, ex, "Loi gan han su dung cho do roi tu boss");
        }
    }

    /**
     * Chủ của đồ boss rơi: người đánh đòn cuối. Người khác nhặt thì báo "Không
     * thể nhặt vật phẩm của người khác". Không rõ ai giết thì -1 (ai cũng nhặt).
     * Đệ tử đánh đòn cuối: id âm của đệ, ItemMap lấy trị tuyệt đối — thành id
     * sư phụ, nên sư phụ nhặt được.
     */
    private long chuDoRoi() {
        return this.playerReward != null ? this.playerReward.id : -1;
    }

    private void thaVatPhamTheoCauHinh() {
        try {
            java.util.List<nro.repository.dao.BossDAO.Drop> ds =
                    nro.repository.dao.BossDAO.drops((int) this.id);
            if (ds.isEmpty() || this.zone == null) {
                return;
            }
            int x = this.location.x;
            int y = this.location.y;
            for (nro.repository.dao.BossDAO.Drop d : ds) {
                // Loc theo phase TRUOC khi gieo ti le. Boss nhieu phase truoc
                // day dung chung mot bang roi, nen chet o phase 1 hay phase
                // cuoi deu ra cung mot thu.
                if (!d.roiOPhase(this.currentLevel)) {
                    continue;
                }
                // Dieu kien nhiem vu: xet NGUOI GIET (playerReward, dat trong
                // die() ngay truoc changeStatus nen chac chan da co).
                if (!d.hopNhiemVu(this.playerReward)) {
                    continue;
                }
                // Mon doc nhat: da co trong nguoi hoac trong ruong thi thoi.
                if (d.chiMotMon && d.daCoMon(this.playerReward)) {
                    continue;
                }
                if (!Util.isTrue(d.rateNum, d.rateDen)) {
                    continue;
                }
                int sl = d.qtyMax > d.qtyMin ? Util.nextInt(d.qtyMin, d.qtyMax) : d.qtyMin;
                if (d.itemId == nro.repository.dao.BossDAO.ITEM_THAN_LINH_NGAU_NHIEN) {
                    thaThanLinhNgauNhien(sl, x, y, d.hsdMin, d.hsdMax, d.hsdVinhVien,
                            d.options);
                    continue;
                }
                nro.entity.map.ItemMap im = new nro.entity.map.ItemMap(this.zone,
                        (short) d.itemId, sl, x + Util.nextInt(-15, 15), y, chuDoRoi());
                im.chiNguoiGiet = true;
                apChiSoNgauNhien(im, d.options);
                apHanSuDung(im, d.hsdMin, d.hsdMax, d.hsdVinhVien);
                Service.gI().dropItemMap(this.zone, im);
            }
        } catch (Exception ex) {
            Logger.logException(Boss.class, ex, "Lỗi thả vật phẩm cấu hình của boss " + this.id);
        }
    }

    @Override
    public Player getPlayerAttack() {
        if (this.zone == null) {
            return null;
        }
        if (this.playerTarger != null && (this.playerTarger.isDie() || !this.zone.equals(this.playerTarger.zone))) {
            this.playerTarger = null;
        }
        if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            this.playerTarger = this.zone.getRandomPlayerInMap();
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(5000, 7000);
        }
        if (this.playerTarger != null && this.playerTarger.isDeTu && ((Detu) this.playerTarger).master != null && ((Detu) this.playerTarger).master.equals(this)) {
            this.playerTarger = null;
        }
        
        return this.playerTarger;
    }
    
    @Override
    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }

    @Override
    public void changeToTypeNonPK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
    }

    @Override
    public void updateInfo() {
        super.update();
    }

    @Override
    public void update() {
        if (prepareBom) {
            return;
        }
        super.update();
        this.nPoint.mp = this.nPoint.mpg;
        // Dang dung chieu dac biet (Super Kamejoko, Cadic lien hoan chuong,
        // Ma phong ba) thi chi gui khung hinh cho client roi thoi — sat thuong
        // do timer rieng cua NewSkill lo.
        //
        // Ban cu de dieu kien nay o CA HAI khoi: khoi tren thoat truoc nen
        // khoi duoi thanh ma chet, client khong nhan duoc khung hinh nao.
        // Ba trang thai "chua o tren ban do" chay TRUOC hai cong chan ben duoi.
        //
        // Hai cong ay dung cho con boss dang song va dang dung tren ban do: mot
        // cai cho chieu dac biet chay het, mot cai cho hieu ung (troi, choang,
        // hoa da) tan. Nhung con boss dang nghi thi khong dang danh chieu nao,
        // va hieu ung dinh tu luc chet cung chang con nghia gi.
        //
        // De chung o tren thi chi can MOT co ket lai — vi du "an troi" von
        // khong co dong ho rieng cho toi ban nay — la update() thoat som mai
        // mai: rest() khong bao gio chay, con boss nam do khong bao gio tu len,
        // va cach duy nhat la ep hoi sinh bang panel. Dung canh da gap.
        switch (this.bossStatus) {
            case REST:
                this.rest();
                return;
            case RESPAWN:
                this.respawn();
                this.changeStatus(BossStatus.JOIN_MAP);
                return;
            case JOIN_MAP:
                this.joinMap();
                return;
            default:
                break;
        }
        if (this.newSkill != null && this.newSkill.isStartSkillSpecial) {
            SkillService.gI().newSkillNotFocus(this, 20);
            return;
        }
        if (this.effectSkill == null || this.effectSkill.isHaveEffectSkill()) {
            return;
        }
        switch (this.bossStatus) {
            case CHAT_S:
            case AFK:
            case ACTIVE:
                this.autoLeaveMap();
                break;
        }
        // REST, RESPAWN va JOIN_MAP da duoc xu ly o tren, truoc hai cong chan.
        switch (this.bossStatus) {
            case CHAT_S: {
                this.checkAutoResetBySecondsRest();
                if (chatS()) {
                    this.doneChatS();
                    this.lastTimeChatM = System.currentTimeMillis();
                    this.timeChatM = 5000;
                    if (this.bossStatus != BossStatus.AFK) {
                        this.changeStatus(BossStatus.ACTIVE);
                    }
                }
                break;
            }
            case AFK:
                this.afk();
                break;
            case ACTIVE: {
                if (this.zone == null || this.isDie()) {
                    return;
                }

                this.chatM();
                this.checkAutoResetBySecondsRest();

                // KHONG dung tay khi dang troi doi thu. "useTroi" nghia la BOSS
                // dang giu nguoi khac, khong phai bi giu — cai bi giu la
                // "anTroi", va da duoc isHaveEffectSkill() chan o tren.
                //
                // Truoc day dung ca hai nen boss chi co moi chieu Troi se: niem
                // chu -> tu dong cung suot thoi gian troi -> het gio -> niem
                // lai. Ca doi khong danh mot nhat nao, nhin y nhu khong biet
                // dung chieu.
                if (this.effectSkill.isCharging && !Util.isTrue(1, 20)) {
                    return;
                }
                this.active();
                break;
            }
            case DIE:
                this.changeStatus(BossStatus.CHAT_E);
                break;
            case CHAT_E: {
                if (chatE()) {
                    this.doneChatE();
                    this.changeStatus(BossStatus.LEAVE_MAP);
                }
                break;
            }
            case LEAVE_MAP:
                this.leaveMap();
                break;
        }
    }

    @Override
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }
        // Dang NGHI ma cap ke tiep lai la "cap sau" (ANOTHER_LEVEL): chuoi cap
        // da dut giua chung. Cap sau chi len khi cap truoc CHET TREN BAN DO
        // (leaveMap -> RESPAWN), khong bao gio qua REST. Con boss ma ve REST o
        // cap giua — tu rut vi khong ai san (checkAutoResetBySecondsRest), hoac
        // vao map loi — thi dieu kien DEFAULT_APPEAR ben duoi sai mai mai: nam
        // im vinh vien. Da gap: Xen bo hung o thi tran Ginger het gio van khong
        // len. Nay quay ve cap mot.
        if (this.data[nextLevel].getTypeAppear() == TypeAppear.ANOTHER_LEVEL) {
            this.currentLevel = this.data.length - 1;
            nextLevel = 0;
        }
        if (this.data[nextLevel].getTypeAppear() == TypeAppear.DEFAULT_APPEAR
                && nro.service.boss.NhomBossService.gI()
                        .denLuotHoiSinh(this, lastTimeRest, secondsRest)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void afk() {

    }

   @Override
public void respawn() {
    this.currentLevel++;
    if (this.currentLevel >= this.data.length) {
        this.currentLevel = 0;
    }
    this.secondsRest = this.data[this.currentLevel].getSecondsRest();
    // Cap sau (ANOTHER_LEVEL) thuong khong khai giay nghi -> 0: chet o cap cuoi
    // la len lai ngay, panel dem 0. Lay giay nghi cua cap mot.
    if (this.secondsRest <= 0 && this.currentLevel > 0) {
        this.secondsRest = this.data[0].getSecondsRest();
    }

    // Vao doi moi thi sach hieu ung cua doi cu.
    //
    // Boss chet trong luc dang bi troi, choang hay hoa da thi may cai co ay con
    // nguyen. Song lai voi mau day du va mot co "dang bi troi" la vua sai voi
    // nguoi choi nhin vao, vua nguy hiem cho chinh no: initBase() ngay duoi keo
    // mau len, luc do isHaveEffectSkill() bat dau tra ve dung va se chan moi
    // buoc tiep theo cua no.
    if (this.effectSkill != null) {
        try {
            this.effectSkill.removeSkillEffectWhenDie();
        } catch (Exception boQua) {
            // Go khong duoc thi thoi, con hon de ca ham dung lai o day.
        }
    }

    this.initBase();
    this.changeToTypeNonPK();

    this.lastTimeBossSpawn = System.currentTimeMillis();
    this.lastTimePlayerAttack = 0;
    this.hasPlayerAttackSinceSpawn = false;
}

//    @Override
//    public void joinMap() {
//        if (zoneFinal != null) {
//            joinMapByZone(zoneFinal);
//            this.notifyJoinMap();
//            this.changeStatus(BossStatus.CHAT_S);
//            this.wakeupAnotherBossWhenAppear();
//            return;
//        }
//        if (this.zone == null) {
//            if (this.parentBoss != null) {
//                this.zone = parentBoss.zone;
//            } else if (this.lastZone == null) {
//                this.zone = getMapJoin();
//            } else {
//                this.zone = this.lastZone;
//            }
//        }
//        if (this.zone == null) {
//            this.zone = getMapJoin();
//        }
//        if (this.zone != null) {
//            try {
//                if (this.currentLevel == 0) {
//                    if (this.parentBoss == null) {
//                        int zoneid = 0;
//                        //this.zone.map.mapId == 80 || this.zone.map.mapId == 103 || this.zone.map.mapId == 97 || this.zone.map.mapId == 102
//                        // Chỉ cho boss xuất hiện ở khu 2
//                        if (this.isZone02Spawn) {
//                            zoneid = 2;
//                            while (zoneid < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
//                                zoneid++;
//                            }
//
//                            if (zoneid < this.zone.map.zones.size()) {
//                                this.zone = this.zone.map.zones.get(zoneid);
//                            } else {
//                                this.changeStatus(BossStatus.REST);
//                                this.zone = null;
//                                this.lastZone = null;
//                                return;
//                            }
//                            // Chỉ cho boss xuất hiện từ khu 0 trở lên ở map thường
//                        } else if (this.isSpawnPlayer) {
//                            
//                            // Chỉ cho boss xuất hiện theo player
//                        } else if (this.isZoneRandomSpawn && this.zone.map.zones.size() > 1) {
//                            zoneid = Util.nextInt(0, this.zone.map.zones.size() - 1);
//                            while (zoneid < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
//                                zoneid++;
//                            }
//
//                            if (zoneid < this.zone.map.zones.size()) {
//                                this.zone = this.zone.map.zones.get(zoneid);
//                            } else {
//                                this.changeStatus(BossStatus.REST);
//                                this.zone = null;
//                                this.lastZone = null;
//                                return;
//                            }
//                        } else {
//                            // Check trong khu lớn hơn 10 người chuyển sang khu n + 1
//                            while (zoneid < this.zone.map.zones.size() && this.zone.map.zones.get(zoneid).getNumOfPlayers() > 10) {
//                                zoneid++;
//                            }
//                            // Check trong khu có boss sẽ chuyển sang khu n + 1
//                            while (zoneid < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
//                                zoneid++;
//                            }
//                            if (zoneid < this.zone.map.zones.size()) {
//                                this.zone = this.zone.map.zones.get(zoneid);
//                            } else {
//                                this.zone = this.zone.map.zones.get(0);
//                            }
//                        }
//                        int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
//                        int y = this.zone.map.yPhysicInTop(x, 100);
//                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
//                    } else {
//                        int x = this.parentBoss.location.x - (this.lv + 1) * 30;
//                        int y = this.zone.map.yPhysicInTop(x, 100);
//                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
//                    }
//                    this.wakeupAnotherBossWhenAppear();
//                } else {
//                    ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
//                }
//                Service.gI().sendFlagBag(this);
//                this.notifyJoinMap();
//                this.changeStatus(BossStatus.CHAT_S);
//            } catch (Exception e) {
//                this.changeStatus(BossStatus.REST);
//                if (error < 5) {
//                    Logger.error("Lỗi : " + e + "\n");
//                    error++;
//                }
//            }
//        } else {
//            this.changeStatus(BossStatus.RESPAWN);
//        }
//    }

    /**
     * Bốc ngẫu nhiên một khu còn trống của bản đồ.
     *
     * <h3>Vì sao bốc chứ không quét</h3>
     *
     * <p>Bản cũ quét từ khu 1 đi lên và lấy khu trống đầu tiên. Mọi con boss
     * đều chạy cùng một vòng quét nên con nào cũng đổ vào khu 1, 2, 3 — dồn hết
     * một góc bản đồ, còn những khu phía sau thì không bao giờ có boss. Nhóm
     * boss lại càng lộ: ba cặp xếp đúng thành khu 1, 2, 3.</p>
     *
     * <p>Bỏ qua <b>khu 0</b> vì đó là khu người mới hay đứng.</p>
     *
     * <p>Nới điều kiện theo hai bậc: ưu tiên khu <i>vừa trống boss vừa ít
     * người</i>; không còn thì lấy khu trống boss bất kể đông người. Hết cả hai
     * mới chịu thua — thà không hiện còn hơn chồng hai con vào một khu.</p>
     *
     * @return khu bốc được, hoặc {@code null} nếu không còn khu nào
     */
    private nro.entity.map.Zone khuTrongNgauNhien(nro.entity.map.Map map) {
        return khuTrongNgauNhien(map, 1);
    }

    /**
     * Như trên nhưng bắt đầu từ một khu chỉ định — dành cho loại boss chỉ được
     * hiện từ khu 2 trở đi.
     */
    private nro.entity.map.Zone khuTrongNgauNhien(nro.entity.map.Map map, int khuDau) {
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return null;
        }
        java.util.List<nro.entity.map.Zone> thoang = new java.util.ArrayList<>();
        java.util.List<nro.entity.map.Zone> trong = new java.util.ArrayList<>();
        for (int i = Math.max(1, khuDau); i < map.zones.size(); i++) {
            nro.entity.map.Zone z = map.zones.get(i);
            if (z == null || !z.getBosses().isEmpty()) {
                continue;
            }
            trong.add(z);
            if (z.getNumOfPlayers() <= 10) {
                thoang.add(z);
            }
        }
        java.util.List<nro.entity.map.Zone> dung = !thoang.isEmpty() ? thoang : trong;
        if (dung.isEmpty()) {
            return null;
        }
        return dung.get(Util.nextInt(0, dung.size() - 1));
    }
    @Override
public void joinMap() {
    // Nhom "cung khu": con dau tien chon khu, cac con sau theo dung khu do,
    // nen Zamasu va Black Goku dung chung mot khu cua mot ban do.
    Zone khuNhom = nro.service.boss.NhomBossService.gI().khuChung(this);
    if (khuNhom != null && this.parentBoss == null) {
        joinMapByZone(khuNhom);
        this.notifyJoinMap();
        this.changeStatus(BossStatus.CHAT_S);
        this.lastTimeBossSpawn = System.currentTimeMillis();
        this.lastTimePlayerAttack = 0;
        this.hasPlayerAttackSinceSpawn = false;
        this.wakeupAnotherBossWhenAppear();
        return;
    }
    if (zoneFinal != null) {
        joinMapByZone(zoneFinal);
        this.notifyJoinMap();
        this.changeStatus(BossStatus.CHAT_S);
        this.lastTimeBossSpawn = System.currentTimeMillis();
        this.lastTimePlayerAttack = 0;
        this.hasPlayerAttackSinceSpawn = false;
        this.wakeupAnotherBossWhenAppear();
        return;
    }
    if (this.zone == null) {
        if (this.parentBoss != null) {
            this.zone = parentBoss.zone;
        } else if (this.lastZone == null) {
            this.zone = getMapJoin();
        } else {
            this.zone = this.lastZone;
        }
    }
    if (this.zone == null) {
        this.zone = getMapJoin();
    }
    if (this.zone != null) {
        try {
            if (this.currentLevel == 0) {
                if (this.parentBoss == null) {
                    if (this.isZone02Spawn) {
                        // Loai boss chi hien tu khu 2 tro di — van boc ngau
                        // nhien, chi khac diem bat dau.
                        nro.entity.map.Zone chon02 = khuTrongNgauNhien(this.zone.map, 2);
                        if (chon02 == null) {
                            this.changeStatus(BossStatus.REST);
                            this.zone = null;
                            this.lastZone = null;
                            return;
                        }
                        this.zone = chon02;

                    } else if (this.isSpawnPlayer) {

                        // Chỉ cho boss xuất hiện theo player

                    } else if (this.isZoneRandomSpawn && this.zone.map.zones.size() > 1) {
                        // Dung chung mot duong boc khu voi nhanh mac dinh:
                        // truoc day hai nhanh viet rieng, moi cai mot kieu do
                        // khu, sua mot ben la quen ben kia.
                        nro.entity.map.Zone chonRd = khuTrongNgauNhien(this.zone.map);
                        if (chonRd == null) {
                            this.changeStatus(BossStatus.REST);
                            this.zone = null;
                            this.lastZone = null;
                            return;
                        }
                        this.zone = chonRd;
                    } else {
                        // Boc NGAU NHIEN trong cac khu con trong, thay vi quet
                        // tu khu 1 di len. Quet tuan tu thi con nao cung do bo
                        // vao khu 1, 2, 3 — dong het mot goc ban do, con nhung
                        // khu sau khong bao gio co boss.
                        nro.entity.map.Zone chon = khuTrongNgauNhien(this.zone.map);
                        if (chon == null) {
                            this.changeStatus(BossStatus.REST);
                            this.zone = null;
                            this.lastZone = null;
                            return;
                        }
                        this.zone = chon;
                    }

                    int x = this.zone.map.mapWidth > 100
                            ? Util.nextInt(100, this.zone.map.mapWidth - 100)
                            : Util.nextInt(100);
                    int y = this.zone.map.yPhysicInTop(x, 100);
                    ChangeMapService.gI().changeMap(this, this.zone, x, y);

                } else {
                    int x = this.parentBoss.location.x - (this.lv + 1) * 30;
                    int y = this.zone.map.yPhysicInTop(x, 100);
                    ChangeMapService.gI().changeMap(this, this.zone, x, y);
                }
                this.wakeupAnotherBossWhenAppear();
            } else {
                ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            }
            // Con dau tien cua nhom "cung khu" bao lai khu vua chon, de nhung
            // con con lai vao dung cho nay. Goi vo dieu kien: khong theo nhom
            // hoac nhom khong bat "cung khu" thi ham nay khong lam gi.
            nro.service.boss.NhomBossService.gI().ghiKhuChung(this, this.zone);
            Service.gI().sendFlagBag(this);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
        } catch (Exception e) {
            this.changeStatus(BossStatus.REST);
            if (error < 5) {
                Logger.error("Lỗi : " + e + "\n");
                error++;
            }
        }
    } else {
        this.changeStatus(BossStatus.RESPAWN);
    }
}

    public void joinMapByZone(Zone zone) {
        if (zone != null) {
            this.zone = zone;
            int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
        }
    }

    protected void notifyJoinMap() {
        if (canSendNotify()) {
            ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }

    private boolean canSendNotify() {
        return !(this.zone == null || this.isNotifyDisabled || MapService.gI().isMapNoNottify(this.zone.map.mapId));
    }

    @Override
    public boolean chatS() {
        if (Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            if (this.indexChatS == this.data[this.currentLevel].getTextS().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextS()[this.indexChatS];
            int prefix = docMauThoai(textChat);
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = textChat.length() * 100;
            if (this.timeChatS > 2000) {
                this.timeChatS = 2000;
            }
            this.indexChatS++;
        }
        return false;
    }

    @Override
    public void doneChatS() {

    }

    @Override
    public void chatM() {
        if (this.typePk == ConstPlayer.NON_PK) {
            return;
        }
        if (this.data[this.currentLevel].getTextM().length == 0) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }
        String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0, this.data[this.currentLevel].getTextM().length - 1)];
        int prefix = docMauThoai(textChat);
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
        this.chat(prefix, textChat);
        this.lastTimeChatM = System.currentTimeMillis();
        this.timeChatM = Util.nextInt(3000, 20000);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    protected long lastTimeAttack;



    /**
     * Boss đang giữa chừng một chiêu chưa xong.
     *
     * <p>Ba trường hợp, đều có chung một triệu chứng nếu bỏ qua: boss bốc chiêu
     * mới mỗi nhịp 100ms nên chiêu đang dở bị cắt ngang, nhìn vào thấy nó
     * <b>giật khựng</b> tại chỗ mà chẳng ra đòn nào.</p>
     *
     * <ul>
     *   <li><b>Trói</b> — đang giữ người chơi. Đổi chiêu hay nhảy đi là thả ra
     *       ngay, đòn trói coi như vô nghĩa.</li>
     *   <li><b>Tự phát nổ</b> — đang gồng. Bốc trúng chiêu khác ở nhịp sau là
     *       {@code prepareTuSat} kẹt bật, quả nổ không bao giờ tới và hiệu ứng
     *       gồng cũng không kịp hiện.</li>
     *   <li><b>Chiêu đặc biệt</b> (Super Kamejoko, Cađíc liên hoàn chưởng, Ma
     *       phong ba) — đang phóng, sát thương do timer riêng rải dần.</li>
     * </ul>
     */
    private boolean dangGiuaChieu() {
        if (this.effectSkill != null
                && (this.effectSkill.useTroi || this.effectSkill.isCharging)) {
            return true;
        }
        if (this.playerSkill != null && this.playerSkill.prepareTuSat) {
            return true;
        }
        return this.newSkill != null && this.newSkill.isStartSkillSpecial;
    }

    /**
     * Chiêu đang dùng dở, để giữ nguyên thay vì bốc chiêu khác.
     *
     * @return chiêu phải dùng tiếp, hoặc {@code null} nếu không có chiêu nào dở
     */
    private Skill chieuTuSat() {
        if (this.playerSkill == null || this.playerSkill.skills == null) {
            return null;
        }
        for (Skill s : this.playerSkill.skills) {
            if (s != null && s.template != null && s.template.id == Skill.TU_SAT) {
                return s;
            }
        }
        return null;
    }
    /**
     * Bốc một chiêu <b>đang dùng được</b> trong số chiêu đã gán cho boss.
     *
     * <h3>Vì sao không bốc thẳng ngẫu nhiên như trước</h3>
     *
     * <p>Bản cũ bốc một chiêu rồi dùng luôn. Trúng chiêu đang hồi, hết khí, hay
     * ngoài tầm thì {@code useSkill} lặng lẽ trả về {@code false} và boss <b>bỏ
     * hẳn lượt đó</b> — không thử chiêu khác. Gán cho boss bốn chiêu, trong đó
     * một chiêu hồi 90 giây, thì phần lớn thời gian nó đứng ngây ra. Nhìn từ
     * ngoài đúng như "gán chiêu mà boss không dùng".</p>
     *
     * <p>Nay duyệt hết danh sách, bắt đầu từ một vị trí ngẫu nhiên để không con
     * nào luôn mở màn bằng cùng một chiêu, và lấy chiêu đầu tiên vừa <b>sẵn
     * sàng</b> vừa <b>tới được</b> mục tiêu.</p>
     *
     * <p>Không chiêu nào tới được thì trả về chiêu sẵn sàng gần nhất — để
     * {@code attack()} biết đường đi lại gần thay vì đứng yên chờ.</p>
     *
     * @return chiêu để dùng, hoặc {@code null} khi mọi chiêu đều đang hồi hoặc
     *         không đủ khí — lúc đó boss chờ thật, không phải lỗi
     */
    private Skill chonChieuDungDuoc(Player muc) {
        java.util.List<Skill> ds = this.playerSkill.skills;
        if (ds == null || ds.isEmpty() || muc == null) {
            return null;
        }
        Skill giuLai = this.playerSkill.skillSelect;
        Skill sanSang = null;
        int batDau = Util.nextInt(0, ds.size() - 1);
        for (int i = 0; i < ds.size(); i++) {
            Skill s = ds.get((batDau + i) % ds.size());
            if (s == null || s.template == null) {
                continue;
            }
            // Đang biến khỉ thì KHÔNG dùng tự phát nổ — phải đợi hết khỉ đã.
            // Chỉ chặn việc chọn riêng chiêu này, không chặn cả lượt đánh: chặn
            // hết thì boss biến khỉ xong đứng im, không ra đòn nào.
            if (s.template.id == Skill.TU_SAT && this.effectSkill != null
                    && this.effectSkill.isMonkey) {
                continue;
            }
            // Hai hàm kiểm tra đọc chiêu từ skillSelect chứ không nhận tham số,
            // nên phải gán vào trước rồi hỏi. Cuối vòng trả lại giá trị cũ để
            // không để boss mang một chiêu nửa vời khi hàm này về tay không.
            this.playerSkill.skillSelect = s;
            if (!SkillService.gI().canUseSkillWithCooldown(this)
                    || !SkillService.gI().canUseSkillWithMana(this)) {
                continue;
            }
            if (sanSang == null) {
                sanSang = s;
            }
            if (Util.getDistance(this, muc) <= this.getRangeCanAttackWithSkillSelect()) {
                return s;
            }
        }
        this.playerSkill.skillSelect = giuLai;
        return sanSang;
    }
    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                // Dang gong tu phat no: phai goi tiep DUNG chieu do, vi chinh
                // lan goi sau moi kich no.
                if (this.playerSkill != null && this.playerSkill.prepareTuSat) {
                    Skill noBom = chieuTuSat();
                    if (noBom != null) {
                        this.playerSkill.skillSelect = noBom;
                        SkillService.gI().useSkill(this, pl, null, -1, null);
                    }
                    return;
                }
                // Dang van mot chieu khac (troi, chieu dac biet, dang gong):
                // DUNG YEN, khong goi useSkill nua.
                //
                // Goi lai la hong that su chu khong chi phi: useSkill mo dau
                // bang removeUseTroi(), nen troi vua dinh la chinh boss tha ra
                // ngay o nhip sau. Chieu dac biet thi bi khoi dong lai giua
                // chung, nhin nhu giat tai cho.
                if (dangGiuaChieu()) {
                    return;
                }
                Skill chon = chonChieuDungDuoc(pl);
                if (chon == null) {
                    // Moi chieu deu dang hoi hoac het khi -> cho that su.
                    return;
                }
                this.playerSkill.skillSelect = chon;
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void checkPlayerDie(Player player) {
        if (player.isDie()) {

        }
    }

    protected int getRangeCanAttackWithSkillSelect() {
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        } else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK || skillId == Skill.LIEN_HOAN || skillId == Skill.KAIOKEN) {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return 500;
    }

   @Override
public void die(Player plKill) {
    Player killer = null;

    if (plKill != null) {
        killer = plKill.getMaster() != null ? plKill.getMaster() : plKill;
        this.playerReward = killer;
    }
    this.lastTimeRest = System.currentTimeMillis();
    // Ghi lai luot cua nhom ngay luc nam xuong: con nay se hoi sinh o luot ke
    // tiep, cung voi moi con khac trong nhom.
    nro.service.boss.NhomBossService.gI().ghiLuotLucNghi(this);

    if (killer != null) {
        // Nhiem vu danh hieu loai "ha boss". Dat o day vi die() nay la duong
        // chung cua moi boss — khong phai sua tung lop boss mot.
        nro.service.badges.BadgesTaskService.tangTheoLoai(killer,
                nro.entity.badges.BadgesTaskTemplate.GIET_BOSS, (int) this.id, 1);
        // Danh dau boss nao dang tra thuong, de Zone.addItem biet co phai bo do
        // roi viet cung cua boss nay khong. Dat quanh reward() vi do la cho cac
        // lop con tha do — 18 boss co do viet cung deu dung die() nay.
        DANG_TRA_THUONG.set((int) this.id);
        try {
            reward(killer);
        } finally {
            DANG_TRA_THUONG.remove();
        }
        ServerNotify.gI().notify(killer.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
    }
    sendBossDieNotifyWithRest(killer);

    this.changeStatus(BossStatus.DIE);
}

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        plKill.event.addEventPointBHM(1);
        Service.gI().sendThongBao(plKill, "Bạn đã Đã tiêu diệt được " + this.name + " và nhận 1 điểm Bà Hạt Mít");
    }

    @Override
    public boolean chatE() {
        if (Util.canDoWithTime(lastTimeChatE, timeChatE)) {
            if (this.indexChatE == this.data[this.currentLevel].getTextE().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextE()[this.indexChatE];
            int prefix = docMauThoai(textChat);
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.lastTimeChatE = System.currentTimeMillis();
            this.timeChatE = textChat.length() * 100;
            if (this.timeChatE > 2000) {
                this.timeChatE = 2000;
            }
            this.indexChatE++;
        }
        return false;
    }

    @Override
    public void doneChatE() {

    }

    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            this.changeStatus(BossStatus.RESPAWN);
        } else {
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            // DON'T reset lastTimeRest here - it was set at death time and should be preserved for respawn countdown
            this.changeStatus(BossStatus.REST);
        }
        this.wakeupAnotherBossWhenDisappear();
    }

   @Override
public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
    if (!this.isDie()) {
        if (Util.neDuocDon(this, plAtt, piercing)) {
            this.chat("Xí hụt");
            return 0;
        }

        if (plAtt != null && !plAtt.isBoss) {
            this.lastTimePlayerAttack = System.currentTimeMillis();
            this.hasPlayerAttackSinceSpawn = true;
        }

        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
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
protected void autoResetBossBecauseNoHunter() {
    try {
        if (this.zone != null && canSendNotify()) {
            // Câu cũ thiếu hẳn động từ: "BOSS X vừa  Rừng Karin" (hai dấu cách).
            ServerNotify.gI().notify("BOSS " + this.name + " vừa rời khỏi "
                    + this.zone.map.mapName);
        }

        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        
        this.lastTimeBossSpawn = 0;
        this.lastTimePlayerAttack = 0;
        this.hasPlayerAttackSinceSpawn = false;
        this.changeStatus(BossStatus.REST);
    } catch (Exception e) {
        this.lastTimeRest = System.currentTimeMillis();
        this.lastTimeBossSpawn = 0;
        this.lastTimePlayerAttack = 0;
        this.hasPlayerAttackSinceSpawn = false;
        this.changeStatus(BossStatus.REST);
    }
}

    @Override
    public void moveToPlayer(Player pl) {
        if (pl.location != null) {
            moveTo(pl.location.x, pl.location.y);
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y + (Util.isTrue(3, 10) ? -50 : 0));
    }

    public void chat(String text) {
        Service.gI().chat(this, text);
    }

    protected boolean chat(int prefix, String textChat) {
        if (prefix == -1) {
            this.chat(textChat);
        } else if (prefix == -2) {
            if (this.zone != null) {
                Player plMap = this.zone.getRandomPlayerInMap();
                if (plMap != null && !plMap.isDie() && Util.getDistance(this, plMap) <= 600) {
                    Service.gI().chat(plMap, textChat);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (prefix == -3) {
            if (this.parentBoss != null && !this.parentBoss.isDie()) {
                this.parentBoss.chat(textChat);
            }
        } else if (prefix >= 0) {
            if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
                Boss boss = this.bossAppearTogether[this.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            } else if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null
                    && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                Boss boss = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            }
        }
        return true;
    }
    protected void checkAutoResetBySecondsRest() {
    if (this.zone == null || this.isDie()) {
        return;
    }

    if (this.secondsRest <= 0) {
        return;
    }

    long now = System.currentTimeMillis();
    long resetMillis = this.secondsRest * 1000L;

    if (!this.hasPlayerAttackSinceSpawn) {
        if (now - this.lastTimeBossSpawn >= resetMillis) {
            this.autoResetBossBecauseNoHunter();
        }
        return;
    }

    if (this.lastTimePlayerAttack > 0 && now - this.lastTimePlayerAttack >= resetMillis) {
        this.autoResetBossBecauseNoHunter();
    }
}

    @Override
    public void wakeupAnotherBossWhenAppear() {
        if (!MapService.gI().isMapNoNottify(this.zone.map.mapId)) {
//            System.out.println("BOSS " + this.name + " : " + this.zone.map.mapName + " khu vực " + this.zone.zoneId + "(" + this.zone.map.mapId + ")");
        }
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            int nextLevelBoss = boss.currentLevel + 1;
            if (nextLevelBoss >= boss.data.length) {
                nextLevelBoss = 0;
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == TypeAppear.CALL_BY_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == TypeAppear.APPEAR_WITH_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
                boss.changeStatus(BossStatus.RESPAWN);
            }
        }
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
    }

    @Override
    public void autoLeaveMap() {

    }

    public void leaveMapNew() {
        if (this.data != null) {
            this.currentLevel = this.data.length;
        }
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    @Override
    public void setBom(Player plAtt) {
        try {
            if (!prepareBom) {
                prepareBom = true;
                this.nPoint.hp = 1;
                long lastTime = System.currentTimeMillis();
                //gồng tự sát
                Service.gI().chat(Boss.this, "Rồi, rồi, mày xong rồi!");
                Message msg;
                try {
                    msg = new Message(-45);
                    msg.writer().writeByte(7);
                    msg.writer().writeInt((int) Boss.this.id);
                    msg.writer().writeShort(104);
                    msg.writer().writeShort(2000);
                    Service.gI().sendMessAllPlayerInMap(Boss.this, msg);
                    msg.cleanup();
                } catch (IOException e) {
                }
                while (prepareBom) {
                    if (Util.canDoWithTime(lastTime, 2500)) {
                        setDie(this);
                        die(plAtt);
                        double dame = Util.CrisGH(Boss.this.nPoint.hpMax);
                        for (Mob mob : Boss.this.zone.mobs) {
                            mob.injured(Boss.this, dame, true);
                        }
                        List<Player> playersMap = Boss.this.zone.getNotBosses();
                        if (!MapService.gI().isMapOffline(Boss.this.zone.map.mapId)) {
                            //Sử dụng vòng for lặp ngược để hạn chế lỗi đồng bộ
                            for (int i = playersMap.size() - 1; i >= 0; i--) {
                                Player pl = playersMap.get(i);
                                if (!Boss.this.equals(pl)) {
                                    pl.injured(Boss.this, dame, false, false);
                                    PlayerService.gI().sendInfoHpMpMoney(pl);
                                    Service.gI().Send_Info_NV(pl);
                                }
                            }
                        }
                        prepareBom = false;
                    }
                }
            }
        } catch (Exception e) {
            if (prepareBom) {
                prepareBom = false;
            }
            setDie(this);
            die(plAtt);
        }
    }
    
    //--------------------------------------------------------------------------
    @Override
    public void goToPlayer(Player pl, boolean isTeleport) {
        goToXY(pl.location.x, pl.location.y, isTeleport);
    }
    
    @Override
    public void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
        } else {
            ChangeMapService.gI().changeMapYardrat(this, this.zone, x, y);
        }
    }
    
    @Override
    public Player getAttackPlayer() throws Exception {
        if (countChangePlayerAttack < targetCountChangePlayerAttack && plAttack != null && plAttack.zone != null && plAttack.zone.equals(this.zone)) {
            if (!plAttack.isDie() && !plAttack.effectSkin.isVoHinh && !plAttack.isPetFollow && !plAttack.isDuongTang) {
                this.countChangePlayerAttack++;
                return plAttack;
            } else {
                plAttack = null;
            }
        } else {
            try {
                if (plAttack != null && !plAttack.isDie() && plAttack.effectSkin.isVoHinh) {
                    plAttack = null;
                }
                this.targetCountChangePlayerAttack = Util.nextInt(10, 20);
                this.countChangePlayerAttack = 0;
                if (this.zone != null) {
                    plAttack = this.zone.getRandomPlayerInMap();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error Boss : " + this.name);
            }
        }
        return plAttack;
    }
    
    public void setJustRest() {
        this.lastTimeRest = System.currentTimeMillis();
    }
    
    public void die() {
        setJustRest();
        changeStatus(BossStatus.DIE);
    }
    
    public Zone getRandomZone(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        Zone zone = null;
        try {
            if (map != null) {
                if (map.type != 0) {
                    zone = map.zones.get(Util.nextInt(0, map.zones.size() - 1));
                } else {
                    zone = map.zones.get(Util.nextInt(0, map.zones.size() - 1));
                }
            }
        } catch (Exception e) {
        }
        return zone;
    }
    
    
    public void joinMapByZone(Player player) {
        if (player.zone != null) {
            this.zone = player.zone;
            ChangeMapService.gI().changeMapBySpaceShip(this, this.zone, -1);
        }
    }
    
    public void joinMapByZoneWithXY(Zone zone, short x, short y) {
        if (zone != null) {
            this.zone = zone;
            ChangeMapService.gI().changeMap(this, zone, x, y);
        }
    }
        
    public void effectCharger() {
        if (Util.isTrue(100, 100)) {
            EffectSkillService.gI().sendEffectCharge(this);
        }
    }
    private void sendBossDieNotifyWithRest(Player plKill) {
    try {
        String bossName = this.name;
        if ((bossName == null || bossName.isEmpty()) && this.data != null && this.data.length > 0) {
            bossName = this.data[0].getName();
        }

        String mapName = "Không rõ";
        if (this.zone != null && this.zone.map != null) {
            mapName = this.zone.map.mapName;
        } else if (this.data != null && this.data.length > 0
                && this.data[0].getMapJoin() != null
                && this.data[0].getMapJoin().length > 0) {
            mapName = "Map " + this.data[0].getMapJoin()[0];
        }

        String killerName = "Chưa có thông tin";
        if (plKill != null && plKill.name != null && !plKill.name.isEmpty()) {
            killerName = plKill.name;
        }

        long dieTimeMs = this.lastTimeRest > 0 ? this.lastTimeRest : System.currentTimeMillis();
        int rest = this.secondsRest;

        String displayText = "Boss " + bossName + " - " + mapName + " - " + killerName;

        Service.gI().sendBossChatVipWithRest(displayText, dieTimeMs, rest);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    

    public long getLastTimeBossSpawn() {
        return this.lastTimeBossSpawn;
    }

    /**
     * Cho boss hồi sinh <b>ngay</b>, bỏ hết thời gian còn phải chờ.
     *
     * <h3>Vì sao không gọi {@code active()}</h3>
     *
     * <p>{@code active()} chỉ đặt chế độ PK rồi đánh — với boss đang nghỉ hoặc đã
     * chết thì nó <b>không làm gì cả</b>. Đường hồi sinh thật là {@code rest()}:
     * nó chờ đủ {@code secondsRest} rồi mới chuyển sang {@code RESPAWN}.</p>
     *
     * <p>Nên cách đúng là <b>xoá mốc thời gian nghỉ</b> để phép đếm ngược coi như
     * đã hết, rồi để vòng cập nhật của chính boss tự chuyển trạng thái. Làm vậy
     * mới đi qua đủ các bước lên cấp, chọn map, chào sân — gọi thẳng
     * {@code changeStatus(RESPAWN)} là nhảy cóc, bỏ qua các bước đó.</p>
     *
     * @return {@code null} nếu đã cho hồi sinh, hoặc câu giải thích vì sao không được
     */
    public String hoiSinhNgay() {
        if (this.data == null || this.data.length == 0) {
            return "Boss không có dữ liệu cấp nào.";
        }
        // Bam nut la PHAI len, du dang o trang thai nao. Con dang song hay
        // dang thoai thi go xuong truoc roi cho len lai — truoc day chi bao
        // "khong phai dang cho hoi sinh" roi thoi, muon goi lai phai ngoi doi
        // no chet tu nhien.
        if (this.bossStatus != BossStatus.REST && this.bossStatus != BossStatus.DIE) {
            try {
                nro.service.fun.ChangeMapService.gI().exitMap(this);
            } catch (Exception boQua) {
                // Chua vao map thi khong co gi de go.
            }
            this.zone = null;
            this.lastZone = null;
            this.changeStatus(BossStatus.REST);
        }
        // Dat currentLevel ve CAP CUOI, khong phai ve 0.
        //
        // Nghe nguoc, nhung day la cach may hoi sinh chay: respawn() bat dau
        // bang currentLevel++ roi moi quay vong ve 0 khi vuot qua cap cuoi.
        // Nen dat 0 o day la lan hoi sinh ke tiep len CAP HAI — bam "hoi sinh
        // ngay" cho Xen bo hung thi ra Xen bo hung 2, va cap mot khong bao gio
        // thay dau. Dat cap cuoi thi phep tang kia quay vong dung ve cap mot.
        this.currentLevel = this.data.length - 1;
        this.lastTimeRest = 0;
        // Con theo nhom khong nhin lastTimeRest ma nhin so luot cua nhom, nen
        // chi xoa dong ho rieng la bam nut xong van nam im. Ha moc luot xuong
        // mot bac de dieu kien cua nhom mo ra ngay.
        nro.service.boss.NhomBossService.gI().moCongNhom(this);
        // Con khong tu xuat hien (phai trieu bang vat pham, hoac di theo con
        // khac) thi rest() khong bao gio cho len — ep thang sang RESPAWN. Bam
        // nut la phai len, do la y nghia cua nut nay.
        if (this.data[0].getTypeAppear() != TypeAppear.DEFAULT_APPEAR) {
            this.changeStatus(BossStatus.RESPAWN);
        }
        return null;
    }

    public long getLastTimeRest() {
        return this.lastTimeRest;
    }

    public int getSecondsRest() {
        return this.secondsRest;
    }

    public Player getPlayerReward() {
        return this.playerReward;
    }

    protected Skill getSkillById(int skillId) {
        return SkillUtil.getSkillbyId(this, skillId);
    }


    /**
     * Đọc mã màu ở đầu một câu thoại, dạng {@code |-1|nội dung}.
     *
     * <p>Không được để câu thoại gõ sai làm sập vòng cập nhật boss. Trước đây
     * chỗ này {@code Integer.parseInt} thẳng, nên một chuỗi hỏng — ví dụ ba câu
     * bị nối liền vì đi qua ô nhập một dòng — ném
     * {@code NumberFormatException} <b>mỗi nhịp</b>, sinh hơn mười ba nghìn
     * dòng log trong vài phút và làm boss đứng im.</p>
     *
     * @return mã màu, hoặc {@code -1} nếu câu không có tiền tố hợp lệ
     */
    private static int docMauThoai(String textChat) {
        if (textChat == null) {
            return -1;
        }
        int cuoi = textChat.lastIndexOf('|');
        if (cuoi <= 0) {
            return -1;
        }
        try {
            return Integer.parseInt(textChat.substring(1, cuoi).trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
