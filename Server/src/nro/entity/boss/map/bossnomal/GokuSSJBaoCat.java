package nro.entity.boss.map.bossnomal;

import nro.core.consts.ConstPlayer;
import nro.core.util.Util;
import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import nro.entity.boss.BossType;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.service.MapService;
import nro.service.Service;

/**
 * Bao cát <b>Gôku SSJ</b> ở Đảo Kame (bản đồ 5).
 *
 * <h2>Để làm gì</h2>
 *
 * <p>Nhiệm vụ 16 đòi <b>đánh bại 10 người chơi</b>. Trên máy chủ ít người, hoặc
 * vào giờ vắng, việc đó có thể mất nhiều ngày và hoàn toàn nằm ngoài tầm tay
 * người chơi — họ không làm gì sai, chỉ là không có ai để đánh.</p>
 *
 * <p>Con này đứng một chỗ ở Đảo Kame làm bao cát: <b>một máu</b>, chạm là ngã,
 * ngã xong hồi sinh ngay. Hạ nó có tính vào nhiệm vụ, vì nó là {@code Boss} mà
 * {@code Boss} kế thừa {@code Player} — cùng đường chết, cùng chỗ đếm.</p>
 *
 * <h2>Nó không làm gì cả</h2>
 *
 * <ul>
 *   <li>{@link #attack()} để rỗng — không đánh trả, không gây sát thương.</li>
 *   <li>{@link #moveToPlayer} và {@link #moveTo} để rỗng — đứng nguyên vị trí,
 *       không đuổi theo ai.</li>
 *   <li>{@link #injured} luôn ăn đúng một máu — đấm một nhát là ngã, bất kể
 *       sức mạnh người đánh.</li>
 *   <li>Không có lời thoại, không loa toàn máy chủ khi xuất hiện hay khi
 *       chết.</li>
 * </ul>
 *
 * <p>Hồi sinh sau {@link #GIAY_HOI_SINH} giây — không phải tức thì trong cùng
 * một khung hình. Hồi sinh ngay lập tức thì một người bấm giữ nút đánh sẽ đếm
 * xong mười lượt trong hai giây, và nhiệm vụ thành ra không có nghĩa gì.</p>
 */
public class GokuSSJBaoCat extends Boss {

    /** Bản đồ Đảo Kame. */
    private static final int MAP_DAO_KAME = 5;

    /** Chờ bao lâu rồi mới đứng dậy, tính bằng giây. */
    private static final int GIAY_HOI_SINH = 3;

    /** Đứng đúng chỗ này, không nhích. */
    private static final int X_DUNG = 300;
    private static final int Y_DUNG = 336;

    public GokuSSJBaoCat() throws Exception {
        super(BossType.NOMAL, BossID.GOKU_SSJ_BAO_CAT,
                true,   // khong loa toan may chu
                false, false, false,
                new BossData(
                        "Gôku SSJ",
                        ConstPlayer.TRAI_DAT,
                        new short[]{60, -1, -1, -1, -1, -1},
                        0,                      // sát thương: không đánh ai
                        new long[]{1},          // MỘT máu
                        new int[]{MAP_DAO_KAME},
                        new int[][]{},          // không có chiêu nào
                        new String[]{},         // không nói gì lúc xuất hiện
                        new String[]{},         // không nói gì giữa trận
                        new String[]{},         // không nói gì lúc ngã
                        GIAY_HOI_SINH));
    }

    @Override
    public Zone getMapJoin() {
        nro.entity.map.Map map = MapService.gI().getMapById(MAP_DAO_KAME);
        if (map == null || map.zones.isEmpty()) {
            return null;
        }
        return map.zones.get(0);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        // Ep ve dung cho sau khi vao ban do: joinMap co the tha xuong mot toa
        // do ngau nhien, ma bao cat thi phai luon o dung mot cho de nguoi choi
        // biet duong toi.
        this.location.x = X_DUNG;
        this.location.y = Y_DUNG;
        // Danh duoc NGAY khi vua hien ra, khong doi vong CHAT_S -> ACTIVE.
        this.changeToTypePK();
    }

    /**
     * <b>Luôn đánh được.</b>
     *
     * <p>Boss thật lui về "không đánh được" giữa hai lần hồi sinh, và
     * {@code setDie()} cũng hạ cờ ấy xuống ngay lúc ngã. Với bao cát thì mỗi
     * quãng như thế là một quãng người chơi tới nơi, thấy Gôku SSJ đứng đó, đánh
     * mãi không ăn gì — đúng cảnh "nó là NPC, không đánh được".</p>
     *
     * <p>Nó một máu và không đánh trả, nên để cờ PK bật suốt không mất gì.</p>
     */
    @Override
    public void changeToTypeNonPK() {
        this.changeToTypePK();
    }

    /**
     * Luôn mất đúng một máu, và không đánh trả.
     *
     * <p>Trả về {@code 1} chứ không trả về sát thương thật: con số này là thứ
     * client vẽ lên đầu, và một con số khổng lồ bay lên đầu một bao cát một máu
     * thì vô nghĩa.</p>
     */
    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        this.nPoint.subHP(1);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return 1;
    }

    /** Không đánh ai. */
    @Override
    public void attack() {
    }

    /** Không đuổi theo ai. */
    @Override
    public void moveToPlayer(Player pl) {
    }

    @Override
    public void moveTo(int x, int y) {
    }

    /**
     * Ngã xuống, đếm nhiệm vụ, rồi <b>rời bản đồ theo đúng đường của boss</b>.
     *
     * <h2>Vì sao không được tự nhảy thẳng sang REST</h2>
     *
     * <p>Bản trước gọi {@code super.die()} rồi đặt luôn trạng thái REST. Nhìn
     * thì gọn, nhưng nó <b>nhảy qua LEAVE_MAP</b> — mà {@code exitMap()} chỉ
     * được gọi ở đó. Xác con bao cát vì thế ở lại khu vĩnh viễn: máu 0, cờ PK
     * đã bị {@code setDie()} hạ về 0, và ba giây sau nó "hồi sinh" bằng cách
     * vào lại đúng cái khu nó chưa từng rời. Kết quả là một Gôku SSJ đứng chết
     * dí ở Đảo Kame mà đánh thế nào cũng không ăn — đúng thứ đã gặp.</p>
     *
     * <p>Nay đi thẳng tới LEAVE_MAP: {@code leaveMap()} gọi {@code exitMap()},
     * chuyển sang REST, và {@code rest()} cho hồi sinh sau
     * {@link #GIAY_HOI_SINH} giây. Bỏ qua CHAT_E vì bao cát không có lời thoại
     * nào để chào.</p>
     *
     * <h2>Và tự đếm nhiệm vụ</h2>
     *
     * <p>{@code Player.setDie()} <b>không</b> gọi {@code setDieLV()}, mà chỗ đếm
     * "đánh bại 10 người chơi" lại nằm trong {@code setDieLV()}. Nghĩa là hạ bao
     * cát cũng không cộng gì — chính là việc con này sinh ra để làm. Gọi thẳng
     * ở đây.</p>
     */
    @Override
    public void die(Player plAtt) {
        this.lastTimeRest = System.currentTimeMillis();
        Player nguoiHa = plAtt == null ? null
                : (plAtt.getMaster() != null ? plAtt.getMaster() : plAtt);
        if (nguoiHa != null && nguoiHa.isPl() && !nguoiHa.getBot()) {
            try {
                nro.service.TaskService.gI().checkDoneTaskKillPlayer(nguoiHa);
            } catch (Exception boQua) {
                // Nhiem vu hong thi thoi, khong duoc chan cai chet.
            }
            Service.gI().sendThongBao(nguoiHa,
                    "Hạ được Gôku SSJ — tính vào nhiệm vụ đánh bại người chơi.");
        }
        // Khong loa toan may chu, khong roi do, khong loi thoai — di thang toi
        // duong roi ban do de xac duoc don di han.
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    /** Không rơi đồ — đây là bao cát, không phải nguồn thu. */
    @Override
    public void reward(Player plAtt) {
    }

    /**
     * <b>Không bao giờ tự rời bản đồ.</b>
     *
     * <p>Bản gốc dùng {@code secondsRest} cho <i>hai</i> việc khác hẳn nhau:
     * vừa là quãng chờ hồi sinh, vừa là hạn "không ai đánh thì bỏ đi". Con này
     * đặt {@code secondsRest = 3} để đứng dậy nhanh sau khi ngã — và vì thế
     * <b>ba giây sau khi hiện ra là nó tự biến mất</b> nếu chưa ai kịp đánh.</p>
     *
     * <p>Nhìn từ trong game: tới Đảo Kame thì chẳng thấy Gôku SSJ đâu, hoặc
     * thấy loáng một cái rồi mất. Đó chính là "NPC chưa tấn công được".</p>
     *
     * <p>Bao cát thì phải luôn có mặt — nó tồn tại để người chơi tìm tới, chứ
     * không phải một con boss ghé qua rồi đi.</p>
     */
    @Override
    protected void checkAutoResetBySecondsRest() {
    }

    /** Không loa, không gọi con khác. */
    @Override
    public void wakeupAnotherBossWhenAppear() {
    }

    @Override
    public Player getPlayerAttack() {
        // Khong chon muc tieu: no khong danh ai ca.
        return null;
    }

    /** Đứng yên nên không cần đảo hướng theo ai. */
    @Override
    public void update() {
        super.update();
        if (this.location != null) {
            this.location.x = X_DUNG;
            this.location.y = Y_DUNG;
        }
        // Bao cat khong duoc phep chet lau: neu vi ly do gi do no ket o mot
        // trang thai khong tu quay ve REST, keo no ve sau mot phut.
        if (this.bossStatus != BossStatus.REST
                && this.bossStatus != BossStatus.ACTIVE
                && this.bossStatus != BossStatus.JOIN_MAP
                && this.bossStatus != BossStatus.RESPAWN
                && !Util.canDoWithTime(this.lastTimeRest, 60_000)) {
            this.changeStatus(BossStatus.REST);
            this.lastTimeRest = System.currentTimeMillis();
        }
    }
}
