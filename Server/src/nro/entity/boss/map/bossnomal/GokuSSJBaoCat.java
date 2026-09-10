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

    @Override
    public void die(Player plAtt) {
        super.die(plAtt);
        if (plAtt != null && plAtt.isPl()) {
            Service.gI().sendThongBao(plAtt,
                    "Hạ được Gôku SSJ — tính vào nhiệm vụ đánh bại người chơi.");
        }
        // Ngã xong đứng dậy luôn, không đợi hết vòng "chào tạm biệt" như boss
        // thật: bao cát không có lời thoại nào để chào.
        this.changeStatus(BossStatus.REST);
        this.lastTimeRest = System.currentTimeMillis();
    }

    /** Không rơi đồ — đây là bao cát, không phải nguồn thu. */
    @Override
    public void reward(Player plAtt) {
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
