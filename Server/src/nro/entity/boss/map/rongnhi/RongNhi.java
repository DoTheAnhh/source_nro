package nro.entity.boss.map.rongnhi;

import nro.core.consts.ConstPlayer;
import nro.core.util.Util;
import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.BossType;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.server.Manager;
import nro.service.MapService;
import nro.service.skill.SkillService;

/**
 * Rồng nhí — bảy con, mỗi con một sao, bay lang thang khắp các bản đồ thường.
 *
 * <h2>Không đánh ai, cũng không ai đánh chết nhanh được</h2>
 *
 * <p>Rồng nhí <b>không gây một điểm sát thương nào</b>: sức đánh khai bằng 0 và
 * đòn duy nhất nó dùng là Thái dương hạ san — chỉ làm mù, không trừ máu. Đây là
 * con boss để đuổi bắt chứ không phải để đấu.</p>
 *
 * <p>Đổi lại, mỗi cú đánh chỉ trừ <b>đúng 1 HP</b> bất kể sát thương thật là bao
 * nhiêu, và nó có 100 HP — tức luôn cần đúng 100 cú, người mạnh người yếu như
 * nhau. Cùng một lối với Ăn Trộm, con boss duy nhất khác cũng chơi kiểu này.</p>
 *
 * <h2>Chọn bản đồ</h2>
 *
 * <p>Danh sách bản đồ trong {@code BossesData} chỉ là bản dự phòng.
 * {@link #getMapJoin()} bốc thật trong toàn bộ bản đồ <b>thường</b> đang có:
 * ba hành tinh gốc, trừ các hành tinh phụ (Cold, Tương Lai, Fide, Ngục Tù, Thực
 * Vật, Địa Ngục, Cereal) và trừ mọi bản đồ đặc biệt — phó bản, đại hội, nhà,
 * bang hội, map ngoại tuyến.</p>
 */
public class RongNhi extends Boss {

    /** Bao nhiêu cú đánh thì hạ được, dù người đánh mạnh hay yếu. */
    private static final int SO_DON_HA = 100;

    /** Khoảng cách giữa hai lần Thái dương hạ san, tính bằng mili giây. */
    private static final int NHIP_THAI_DUONG = 15_000;

    /**
     * Danh sách bản đồ <b>thường</b> của ba hành tinh gốc.
     *
     * <h3>Vì sao là danh sách trắng chứ không phải danh sách cấm</h3>
     *
     * <p>Bản trước lọc bằng cách loại tên hành tinh phụ (Cold, Tương Lai,
     * Fide...). Cách ấy sai ở chỗ nó <b>mặc định cho phép</b>: khu nào không
     * nằm trong danh sách cấm là rồng bay tới, nên Nappa, Hành Tinh Chết, và
     * mọi bản đồ sự kiện thêm sau này đều lọt lưới.</p>
     *
     * <p>Danh sách này là đúng bộ bản đồ thường mà boss lang thang vẫn dùng —
     * cùng danh sách của Ăn Trộm. Thêm bản đồ mới thì mặc định rồng KHÔNG bay
     * tới, muốn có thì khai thêm ở đây.</p>
     */
    private static final int[] MAP_THUONG = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
        63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,
        79, 80, 81, 82, 83, 84
    };

    private long lucThaiDuongCuoi;

    public RongNhi(int bossId, BossData data) throws Exception {
        super(BossType.NOMAL, bossId, true, false, false, false, data);
    }

    /**
     * Bốc một khu bất kỳ trong các bản đồ thường.
     *
     * <p>Bốc cả khu chứ không chỉ khu 0: bảy con mà con nào cũng đứng khu 0 thì
     * chúng dồn cục vào một chỗ và mất hẳn cái thú đi tìm.</p>
     */
    @Override
    public Zone getMapJoin() {
        java.util.List<Zone> hopLe = new java.util.ArrayList<>();
        for (nro.entity.map.Map m : Manager.MAPS) {
            if (m == null || m.zones == null || m.zones.isEmpty() || !laMapThuong(m)) {
                continue;
            }
            hopLe.add(m.zones.get(Util.nextInt(0, m.zones.size() - 1)));
        }
        if (hopLe.isEmpty()) {
            // Khong loc ra duoc cai nao thi ve danh sach khai trong BossesData,
            // hon la khong xuat hien lan nao.
            return super.getMapJoin();
        }
        return hopLe.get(Util.nextInt(0, hopLe.size() - 1));
    }

    private boolean laMapThuong(nro.entity.map.Map m) {
        boolean coTrongDanhSach = false;
        for (int id : MAP_THUONG) {
            if (id == m.mapId) {
                coTrongDanhSach = true;
                break;
            }
        }
        if (!coTrongDanhSach) {
            return false;
        }
        // Luoi chan them: bản đồ trong danh sách mà đang bị dùng làm phó bản hay
        // map riêng tư thì vẫn bỏ qua.
        return !MapService.gI().isMapNoNottify(m.mapId)
                && !MapService.gI().isHome(m.mapId);
    }

    /**
     * Mỗi cú đánh mất đúng <b>1% máu tối đa</b>, và trả về đúng con số ấy.
     *
     * <h3>Vì sao hai con số phải bằng nhau</h3>
     *
     * <p>Client <b>tự trừ</b> con số sát thương nhận được vào máu của mục tiêu
     * để vẽ thanh máu ngay, không đợi máy chủ gửi lại. Nên hai con số phải là
     * một: trả về 1 thì mọi đòn hiện "-1" (nhìn như đòn không ăn), còn trả về
     * sát thương thật của người chơi thì client trừ mấy nghìn khỏi một thanh máu
     * 100 điểm và thanh máu vỡ ngay cú đầu.</p>
     *
     * <h3>Cách làm</h3>
     *
     * <p>Cho con rồng 100.000 máu và mỗi đòn lấy đi 1.000 — tức <b>đúng 100
     * đòn</b> như thiết kế, người 2 tỉ sức đánh và người mới chơi như nhau —
     * nhưng con số hiện lên là 1.000 chứ không phải 1, và thanh máu tụt đúng 1%
     * mỗi lần. Sát thương thật của người đánh không còn ý nghĩa ở đây, đó là cả
     * ý đồ của con boss này.</p>
     */
    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        if (plAtt != null && !plAtt.isBoss) {
            this.lastTimePlayerAttack = System.currentTimeMillis();
            this.hasPlayerAttackSinceSpawn = true;
        }
        long mat = this.nPoint.hpMax / SO_DON_HA;
        if (mat < 1) {
            mat = 1;
        }
        if (mat > this.nPoint.hp) {
            mat = this.nPoint.hp;
        }
        this.nPoint.subHP(mat);
        if (isDie()) {
            ghiNguoiTieuDiet(plAtt);
            this.setDie(plAtt);
            die(plAtt);
        }
        return mat;
    }

    /**
     * Chạy nhảy quanh người chơi, và mỗi 15 giây thả một Thái dương hạ san.
     *
     * <h3>Vì sao phải tự lo phần di chuyển</h3>
     *
     * <p>Boss di chuyển ngay trong {@code attack()} của lớp cha — hết đường
     * chạy thì bốc chiêu, gần thì nhảy quanh, xa thì đuổi theo. Bản trước của
     * lớp này ghi đè {@code attack()} và thoát sớm khi chưa tới nhịp Thái dương,
     * nên con rồng <b>đứng chôn chân</b> suốt cả lượt.</p>
     *
     * <p>Không gọi {@code super.attack()} vì bản gốc có đường ra sát thương, mà
     * rồng nhí thì không được chạm vào máu người chơi. Ở đây chỉ có hai việc: di
     * chuyển, và thả đòn làm mù.</p>
     */
    @Override
    public void attack() {
        if (this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeAttack, 500)) {
            return;
        }
        this.lastTimeAttack = System.currentTimeMillis();
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
                return;
            }
            chayNhay(pl);
            if (!Util.canDoWithTime(this.lucThaiDuongCuoi, NHIP_THAI_DUONG)) {
                return;
            }
            nro.entity.skill.Skill tdhs = chieuThaiDuong();
            if (tdhs == null) {
                return;
            }
            this.lucThaiDuongCuoi = System.currentTimeMillis();
            this.playerSkill.skillSelect = tdhs;
            SkillService.gI().useSkill(this, pl, null, -1, null);
        } catch (Exception ex) {
            nro.core.log.Logger.logException(RongNhi.class, ex);
        }
    }

    /**
     * Ở xa thì đuổi theo, ở gần thì nhảy loanh quanh.
     *
     * <p>Cùng luật với boss thường, chỉ khác là nhảy thường xuyên hơn một chút —
     * con này không đánh ai nên đứng yên là chẳng còn gì để nhìn.</p>
     */
    private void chayNhay(Player pl) {
        if (Util.getDistance(this, pl) > 200) {
            this.moveToPlayer(pl);
            return;
        }
        if (Util.isTrue(1, 2)) {
            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(30, 120)),
                    Util.nextInt(10) % 2 == 0 ? pl.location.y
                            : pl.location.y - Util.nextInt(0, 60));
        }
    }

    private nro.entity.skill.Skill chieuThaiDuong() {
        if (this.playerSkill == null || this.playerSkill.skills == null) {
            return null;
        }
        for (nro.entity.skill.Skill s : this.playerSkill.skills) {
            if (s != null && s.template != null
                    && s.template.id == Skill.THAI_DUONG_HA_SAN) {
                return s;
            }
        }
        return null;
    }
}
