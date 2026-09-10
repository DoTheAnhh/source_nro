package nro.entity.player;

import nro.core.consts.ConstPlayer;
import nro.entity.map.Map;
import nro.entity.map.Zone;
import nro.server.Manager;
import nro.service.MapService;
import nro.service.PlayerService;
import nro.service.Service;

/**
 * <b>Gôku SSJ</b> ở Đảo Kame — một NPC hỗ trợ nhiệm vụ, <b>không phải boss</b>.
 *
 * <h2>Để làm gì</h2>
 *
 * <p>Nhiệm vụ 16 đòi <b>đánh bại 10 người chơi</b>. Trên máy chủ ít người, hoặc
 * vào giờ vắng, việc đó có thể mất nhiều ngày và hoàn toàn nằm ngoài tầm tay
 * người chơi — họ không làm gì sai, chỉ là không có ai để đánh. Con này đứng
 * sẵn ở Đảo Kame để hạ cho đủ số.</p>
 *
 * <h2>Vì sao không làm bằng lớp Boss</h2>
 *
 * <p>Bản trước dựng nó thành một {@code Boss} một máu. Chạy thì được, nhưng nó
 * mang theo cả bộ máy của boss mà con này không cần và không nên có: một dòng
 * trong bảng boss của panel, vòng hồi sinh theo hẹn giờ, trạng thái vào/ra bản
 * đồ, loa toàn máy chủ, danh sách "boss đang chạy". Nhìn từ phía quản trị thì
 * nó là một con boss lạ nằm giữa danh sách boss thật.</p>
 *
 * <p>Nay dùng đúng cơ chế của POPO ở bãi tập: một {@code Player} đứng sẵn trong
 * khu, {@code Zone} tự gọi {@code update()} cho nó mỗi nhịp, và <b>ngã xuống là
 * đứng dậy ngay trong nhịp kế tiếp</b> — không hẹn giờ, không rời bản đồ.</p>
 *
 * <h2>Nó không làm gì cả</h2>
 *
 * <ul>
 *   <li>Đứng đúng một chỗ, không đuổi theo ai, không đánh trả.</li>
 *   <li>Một máu — chạm là ngã, để hạ đủ mười lần không mất cả buổi.</li>
 *   <li>Không rơi đồ, không loa, không lời thoại.</li>
 *   <li>Có mặt ở <b>mọi khu</b> của Đảo Kame, nên tới khu nào cũng gặp. Bản
 *       boss trước chỉ hiện ở đúng một khu, ai vào nhầm khu thì không thấy đâu.</li>
 * </ul>
 */
public class GokuSSJKame extends TestDame {

    /** Đảo Kame. */
    private static final int MAP_DAO_KAME = 5;

    /** Đứng đúng chỗ này, không nhích. */
    private static final int X_DUNG = 1195;
    private static final int Y_DUNG = 408;

    /** Id riêng, âm để không đụng id nhân vật thật. */
    private static final long ID_NPC = -4359;

    @Override
    public short getHead() {
        return 60;
    }

    @Override
    public short getBody() {
        return -1;
    }

    @Override
    public short getLeg() {
        return -1;
    }

    /**
     * Ăn đúng một máu mỗi đòn, và đếm nhiệm vụ khi ngã.
     *
     * <p>Trả về {@code 1} chứ không trả về sát thương thật: con số này là thứ
     * client vẽ bay lên đầu, mà một con số khổng lồ bay lên đầu một bao cát một
     * máu thì vô nghĩa.</p>
     *
     * <p>Gọi thẳng {@code checkDoneTaskKillPlayer} ở đây vì
     * {@code Player.setDie()} <b>không</b> gọi {@code setDieLV()}, mà chỗ đếm
     * "đánh bại 10 người chơi" lại nằm trong {@code setDieLV()} — tức là hạ nó
     * sẽ không cộng gì, đúng cái việc con này sinh ra để làm.</p>
     */
    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        this.nPoint.subHP(1);
        if (this.isDie()) {
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
            Service.gI().charDie(this);
        }
        return 1;
    }

    /**
     * Mỗi nhịp: giữ nguyên chỗ đứng, luôn bật cờ đánh được, ngã thì dựng dậy.
     *
     * <p><b>Không</b> gọi {@code super.update()} của POPO: hàm kia hồi sinh
     * bằng {@code hoiSinh} rồi kéo máu về đầy hai tỉ. Con này chỉ có một máu,
     * và cần dựng lại đúng một máu.</p>
     */
    @Override
    public void update() {
        try {
            if (this.typePk == ConstPlayer.NON_PK) {
                this.changeToTypePK();
            }
            if (this.location != null) {
                this.location.x = X_DUNG;
                this.location.y = Y_DUNG;
            }
            if (this.isDie()) {
                // Dung day NGAY, khong hen gio: nguoi choi phai ha du muoi lan,
                // bat cho ba giay moi lan la mat nua phut cho khong.
                PlayerService.gI().hoiSinh(this);
                this.nPoint.hpMax = 1;
                this.nPoint.hpg = 1;
                this.nPoint.setHp(1);
                Service.gI().hsChar(this, 1, 1);
            }
        } catch (Exception boQua) {
            // Mot NPC hong khong duoc lam dung nhip cua ca khu.
        }
    }

    /** Dựng NPC này vào <b>mọi khu</b> của Đảo Kame. */
    public void dungTaiDaoKame() {
        for (Map m : Manager.MAPS) {
            if (m.mapId != MAP_DAO_KAME) {
                continue;
            }
            for (Zone z : m.zones) {
                if (z.TestDame != null) {
                    continue;   // khu nay da co san mot NPC bai tap
                }
                GokuSSJKame npc = new GokuSSJKame();
                npc.name = "Goku SSJ";
                npc.gender = ConstPlayer.TRAI_DAT;
                npc.id = ID_NPC;
                npc.nPoint.hpMax = 1;
                npc.nPoint.hpg = 1;
                npc.nPoint.mpMax = 1;
                npc.nPoint.mpg = 1;
                npc.nPoint.setHp(1);
                npc.nPoint.setMp(1);
                npc.location.x = X_DUNG;
                npc.location.y = Y_DUNG;
                MapService.gI().goToMap(npc, z);
                z.load_Me_To_Another(npc);
                z.setTestDame(npc);
            }
        }
    }
}
