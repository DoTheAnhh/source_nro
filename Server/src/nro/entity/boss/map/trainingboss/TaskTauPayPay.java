package nro.entity.boss.map.trainingboss;

import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.BossStatus;
import nro.service.boss.BossManager;
import nro.service.TaskService;
import nro.service.fun.ChangeMapService;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstTask;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.entity.skill.Skill;

public class TaskTauPayPay extends Boss {

    /**
     * Người chơi đã gọi boss này ra — boss bám theo họ, không bám theo số người
     * trong khu.
     */
    private final Player chuNhiemVu;

    public TaskTauPayPay(Player pl, int bossID, Zone zone, int dame, int x, int y) throws Exception {
        super(bossID, new BossData(
                "Tàu Pảy Pảy", // name
                ConstPlayer.TRAI_DAT, // gender
                new short[]{92, 93, 94, -1, -1, -1}, // outfit {head, body, leg, bag, aura, eff}
                (TaskService.gI().getIdTask(pl) != ConstTask.TASK_10_1 ? dame / 5 : dame / 10),
                new long[]{(TaskService.gI().getIdTask(pl) != ConstTask.TASK_10_1 ? 10000 : 1100)}, // hp
                new int[]{47}, // map join
                new int[][]{
                    {Skill.DRAGON, 1, 1000},
                    {Skill.KAMEJOKO, Util.nextInt(3, 5), 2000}
                },
                new String[]{
                    "|-1|Ta cho ngươi 10 giây suy nghĩ",
                    "|-1|Mau giao ngọc rồng ra đây",
                    "|-2|Đừng trách ta",
                    "|-1|Xem ta đây"
                }, // text chat 1
                new String[]{}, // text chat 2
                new String[]{
                    "|-2|Tuổi trẻ chưa trải sự đời"
                }, // text chat 3
                5 // second rest
        ));

        this.chuNhiemVu = pl;
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(400, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (TaskService.gI().getIdTask(plAtt) == ConstTask.TASK_9_0
                    || TaskService.gI().getIdTask(plAtt) == ConstTask.TASK_9_1
                    || TaskService.gI().getIdTask(plAtt) == ConstTask.TASK_9_2) {
                return 1;
            }

            if (TaskService.gI().getIdTask(plAtt) != ConstTask.TASK_10_1) {
                return 100;
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);
            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }

            return damage;
        }

        return 0;
    }

    /**
     * Boss ở lại chừng nào <b>chủ nhiệm vụ</b> còn trong khu.
     *
     * <h3>Lỗi của bản cũ</h3>
     *
     * <p>Điều kiện cũ là {@code zone.getNumOfPlayers() != 1} — rời map ngay khi
     * khu <b>không có đúng một người</b>. Mà {@code Zone.players} đếm cả
     * <b>Đệ tử</b>: người chơi nào dắt đệ tử ra thì khu có 2, boss vừa hiện đã
     * bỏ đi, kèm dòng "BOSS Tàu Pảy Pảy vừa rời Rừng Karin". Có thêm bất kỳ ai
     * đi ngang qua khu cũng làm hỏng y hệt.</p>
     *
     * <p>Đúng ra boss chỉ cần bám theo người đã gọi nó ra. Đó cũng là ý nghĩa
     * thật của điều kiện cũ — đếm số người chỉ là cách làm gần đúng, và sai
     * ngay khi có đệ tử.</p>
     */
    @Override
    public void update() {
        super.update();

        if (this.zone == null) {
            leaveMap();
            return;
        }
        if (this.chuNhiemVu == null || this.chuNhiemVu.zone != this.zone) {
            leaveMap();
        }
    }

    /**
     * Boss nhiệm vụ <b>không</b> tự bỏ đi vì "không có ai săn".
     *
     * <h3>Vì sao phải chặn</h3>
     *
     * <p>{@code Boss.checkAutoResetBySecondsRest()} chạy mỗi nhịp ở trạng thái
     * CHAT_S và ACTIVE. Nếu chưa ai đánh boss thì sau {@code secondsRest} giây
     * kể từ lúc hiện ra, nó gọi {@code autoResetBossBecauseNoHunter()} —
     * rời map và báo "BOSS … vừa  &lt;tên bản đồ&gt;".</p>
     *
     * <p>Tàu Pảy Pảy đặt {@code secondsRest = 5}. Mà ngay khi hiện ra nó nói
     * <i>"Ta cho ngươi 10 giây suy nghĩ"</i> — tức <b>nó biến mất giữa lúc đang
     * thoại</b>, trước khi người chơi kịp đánh. Đó là lý do "chờ một lúc thì
     * tàu pảy pảy biến mất luôn", và xảy ra bất kể có đệ tử hay không.</p>
     *
     * <p>Cơ chế "hết giờ vì không ai săn" dành cho boss thế giới tự hiện ra
     * theo chu kỳ. Boss này do <b>một người chơi gọi ra cho nhiệm vụ của
     * họ</b>, nên nó chỉ cần bám theo chủ nhiệm vụ — xem {@link #update()}.</p>
     */
    @Override
    protected void checkAutoResetBySecondsRest() {
        // Cố ý để trống.
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMapBySpaceShip(this, this.zone, 775);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        BossManager.gI().removeBoss(this);
        this.dispose();
    }
}