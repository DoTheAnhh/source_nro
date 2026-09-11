package nro.entity.boss.map.yardart;

/*
 * @Author: DoTheAnh
 */

import nro.core.consts.ConstPlayer;
import nro.core.util.Util;
import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.BossStatus;
import nro.entity.boss.BossType;
import nro.entity.map.Zone;
import nro.entity.mob.Mob;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.repository.dao.ConfigDAO;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.fun.ChangeMapService;
import nro.service.skill.SkillService;

/**
 * Lính Yardrat — tập sự, tân binh, chiến binh, đội trưởng.
 *
 * <h2>Là boss nhưng cư xử như quái thường</h2>
 *
 * <p>Chúng phải là {@link Boss} vì quái thường không dùng được chiêu, mà lính
 * Yardrat thì đấm và dịch chuyển tức thời. Ngoài chuyện đó ra, chúng sống như
 * một con quái: <b>một thanh máu</b>, hết là chết, chết thì một lúc sau sống lại
 * đúng chỗ cũ, và rơi đồ theo thẻ "Đồ rơi từ quái" trên panel.</p>
 *
 * <h2>Bản cũ không bao giờ chết</h2>
 *
 * <p>Khi đòn đánh đủ giết, {@code injured()} cũ <b>không cho chết</b>: gửi lệnh
 * hồi đầy máu cho client, đặt máu bằng 1, rồi thả thưởng. Thêm vào đó mỗi nhịp
 * 100 ms có một phần mười cơ hội hồi đầy máu, và cứ hai mươi giây hồi thêm vài
 * phần trăm. Người chơi thấy đúng cảnh "500 nghìn máu tụt về 1 rồi lại lên 500
 * nghìn, đánh mãi không chết".</p>
 *
 * <p>Vòng chết giả ấy còn che một lỗi nữa: lính là loại "xuất hiện cùng con
 * khác", mà máy hồi sinh chung chỉ tự đưa lên loại "tự xuất hiện" — nên lính
 * chết thật sẽ không bao giờ sống lại. Lớp này tự lo việc hồi sinh.</p>
 */
public abstract class Yardart extends Boss {

    protected int x;
    protected int x2;
    protected int y;
    protected int y2;
    protected int range;
    protected int range2;

    protected long lastTimeMove;

    // Ba truong duoi khong con dung — lop con van gan trong init() nen giu lai
    // cho khoi phai sua mười tám lop.
    protected long lastTimeHoiHP;
    protected int timeHoiHP;
    protected int rewardRatio;

    /** Tầm của cú đấm — gần hơn thế mới đấm, xa hơn thì đi bộ tới — không dịch chuyển tức thời. */
    private static final int TAM_DAM = 60;

    /** Xa quá tầm này thì không đuổi theo nữa, quay về tuần tra. */
    private static final int TAM_DUOI = 400;

    private long lastTimeDam;

    public Yardart(BossType DoTheAnh, int id, BossData... data) throws Exception {
        super(DoTheAnh, id, data);
    }

    protected void init() {
    }

    @Override
    public void respawn() {
        super.respawn();
        this.init();
    }

    // =====================================================================
    //  Sống, chết, sống lại — như quái thường
    // =====================================================================

    /**
     * Vào bản đồ, luôn đứng đúng vị trí tuần tra của mình.
     *
     * <p>Chọn khu theo thứ tự: khu đã chỉ định sẵn, <b>khu vừa chết</b>, khu của
     * con đầu đàn, cuối cùng mới bốc ngẫu nhiên. Khu vừa chết đứng trước để quái
     * sống lại đúng chỗ người chơi đang đứng đợi, không nhảy sang khu khác.</p>
     */
    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.changeStatus(BossStatus.CHAT_S);
            return;
        }
        if (this.zone == null) {
            if (this.lastZone != null) {
                this.zone = this.lastZone;
            } else if (this.parentBoss != null && this.parentBoss.zone != null) {
                this.zone = this.parentBoss.zone;
            } else {
                this.zone = getMapJoin();
            }
        }
        if (this.zone == null) {
            // Khong co khu nao de vao: nghi mot nhip roi thu lai, dung quay
            // RESPAWN lien tuc moi nhip 150 ms.
            this.changeStatus(BossStatus.REST);
            return;
        }
        try {
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
            this.changeStatus(BossStatus.CHAT_S);
        } catch (Exception e) {
            this.zone = null;
            this.changeStatus(BossStatus.REST);
        }
    }

    /** Rời bản đồ khi chết — nhớ lại khu để sống lại đúng chỗ ấy. */
    @Override
    public void leaveMap() {
        Zone khu = this.zone;
        super.leaveMap();
        if (khu != null) {
            this.lastZone = khu;
        }
    }

    /** Chết đủ lâu thì sống lại, không đợi ai. */
    @Override
    public void rest() {
        long cho = Math.max(1L, ConfigDAO.num(ConfigDAO.YARDRAT_HOI_SINH_GIAY, 10L)) * 1000L;
        if (Util.canDoWithTime(this.lastTimeRest, cho)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    /**
     * Con đầu đàn hiện ra thì <b>không</b> lôi lính đi đâu cả.
     *
     * <p>Bản gốc của {@code Boss} gỡ mọi lính đang đứng trên bản đồ xuống rồi cho
     * lên lại — đầu đàn sống lại một lần là cả hàng lính đang đánh dở biến mất
     * rồi hiện ra đầy máu. Lính giờ tự hồi sinh theo đồng hồ riêng nên việc ấy
     * không còn cần nữa.</p>
     */
    @Override
    public void wakeupAnotherBossWhenAppear() {
    }

    /** Quái thường không tự bỏ đi khi lâu không ai đánh. */
    @Override
    protected void checkAutoResetBySecondsRest() {
    }

    /**
     * Một thanh máu, mỗi đòn không quá mức trần, hết máu là chết.
     *
     * <p>Chặn trần rồi giao cho {@code Boss.injured()} lo phần còn lại — giáp,
     * khiên, trừ máu, và đường chết chuẩn ({@code setDie} rồi {@code die}).
     * Không còn né 10% riêng, không còn chết giả.</p>
     */
    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing,
            boolean isMobAttack) {
        long tran = ConfigDAO.num(ConfigDAO.YARDRAT_TRAN_SAT_THUONG, 50_000L);
        if (tran > 0 && damage > tran) {
            damage = tran;
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    /**
     * Rơi đồ theo thẻ "Đồ rơi từ quái" trên panel, như một con quái thật.
     *
     * <p>Bản cũ tự thả bí kiếp theo một tỉ lệ viết cứng trong mã và không hỏi
     * bảng nào — sửa panel không có tác dụng. Nay một nguồn duy nhất: muốn lính
     * Yardrat rơi bí kiếp thì khai bí kiếp ở thẻ ấy, phạm vi bản đồ phải có bản
     * đồ của lính (131, 132, 133).</p>
     */
    @Override
    public void reward(Player plKill) {
        if (plKill == null || this.zone == null || this.zone.map == null) {
            return;
        }
        int yEnd = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);
        if (yEnd <= 0) {
            yEnd = this.location.y;
        }
        Mob.roiTheoBangCauHinh(this.zone, plKill, this.location.x, yEnd,
                this.zone.map.mapId);
    }

    // =====================================================================
    //  Đánh nhau: chỉ đấm, không dịch chuyển tức thời
    // =====================================================================

    @Override
    protected int getRangeCanAttackWithSkillSelect() {
        switch (this.playerSkill.skillSelect.template.id) {
            case Skill.DRAGON:
            case Skill.DEMON:
            case Skill.GALICK:
            case Skill.LIEN_HOAN:
            case Skill.KAIOKEN:
                return 50;
            case Skill.KAMEJOKO:
            case Skill.MASENKO:
            case Skill.ANTOMIC:
                return 300;
            case Skill.DICH_CHUYEN_TUC_THOI:
                return 400;
            default:
                return 300;
        }
    }

    /**
     * Mỗi nhịp: có người trong tầm thì đánh, không thì đi tuần.
     *
     * <h3>Vì sao viết lại hẳn</h3>
     *
     * <p>Bản cũ bốc chiêu ngẫu nhiên trong hai vòng {@code while} chạy <b>cho tới
     * khi trúng một chiêu chưởng</b>. Chỉ cần lính không còn chiêu chưởng nào —
     * đúng yêu cầu "chỉ đấm và dịch chuyển" — là hai vòng ấy quay vô tận, và vì
     * mọi boss chung một luồng cập nhật, cả máy chủ đứng hình mọi boss.</p>
     *
     * <p>Nay chọn chiêu theo khoảng cách, không bốc gì cả: trong tầm đấm thì đấm,
     * còn lại thì đi bộ tới. Không dịch chuyển tức thời.</p>
     */
    @Override
    public void attack() {
        if (!Util.canDoWithTime(this.lastTimeAttack, 100)
                || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        this.lastTimeAttack = System.currentTimeMillis();
        try {
            Player pl = getPlayerAttack();
            if (pl != null && !pl.isDie() && pl.zone == this.zone
                    && Util.getDistance(this, pl) <= TAM_DUOI) {
                danh(pl);
                return;
            }
            tuanTra();
        } catch (Exception ex) {
            // Mot nhip loi thi bo nhip do, dung de ca luong boss dung lai.
        }
    }

    private void danh(Player pl) {
        long bayGio = System.currentTimeMillis();
        int kc = Util.getDistance(this, pl);

        if (kc > TAM_DAM) {
            moveToPlayer(pl);
            return;
        }

        Skill dam = getSkillById(Skill.DRAGON);
        if (dam == null && !this.playerSkill.skills.isEmpty()) {
            dam = this.playerSkill.skills.get(0);
        }
        if (dam == null) {
            return;
        }
        // Dam theo dung hoi chieu cua chieu dam, nhu nguoi choi — khong phai
        // moi nhip 100 ms mot cu.
        long hoi = dam.coolDown > 0 ? dam.coolDown : 1000;
        if (!Util.canDoWithTime(this.lastTimeDam, hoi)) {
            return;
        }
        this.playerSkill.skillSelect = dam;
        this.nPoint.dame = (int) ((long) pl.nPoint.hpMax * Util.nextInt(1, 3) / 100)
                / Util.nextInt(10, 30);
        SkillService.gI().useSkill(this, pl, null, -1, null);
        this.lastTimeDam = bayGio;
        checkPlayerDie(pl);
    }

    /** Đi qua đi lại giữa hai mốc tuần tra. */
    private void tuanTra() {
        if (!Util.canDoWithTime(lastTimeMove, 1500)) {
            return;
        }
        if (this.location.x < x) {
            Service.gI().setPos(this, x2, y2);
        } else if (this.location.x > x2) {
            Service.gI().setPos(this, x, y);
        } else if (Util.isTrue(1, 2)) {
            PlayerService.gI().playerMove(this, x2, y2);
        } else {
            PlayerService.gI().playerMove(this, x, y);
        }
        lastTimeMove = System.currentTimeMillis();
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
        byte move = (byte) Util.nextInt(10, 15);
        int xMove = x + (dir == 1 ? move : -move);
        int yMove = this.zone.map.yPhysicInTop(xMove, 100);
        PlayerService.gI().playerMove(this, xMove, yMove);
    }
}
