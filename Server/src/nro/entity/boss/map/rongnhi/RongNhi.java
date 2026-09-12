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

    /** Mỗi cú đánh trừ đúng bấy nhiêu máu, bất kể sát thương thật. */
    private static final int MAU_MAT_MOI_DON = 1;

    /** Khoảng cách giữa hai lần Thái dương hạ san, tính bằng mili giây. */
    private static final int NHIP_THAI_DUONG = 15_000;

    /**
     * Tên hành tinh rồng nhí không bay tới.
     *
     * <p>Lọc theo <b>tên</b> chứ không theo số hành tinh: mấy khu này nằm rải ở
     * nhiều số hành tinh khác nhau tuỳ bản dữ liệu, còn tên thì cố định.</p>
     */
    private static final String[] NOI_CAM = {
        "cold", "tương lai", "tuong lai", "fide", "ngục tù", "nguc tu",
        "thực vật", "thuc vat", "địa ngục", "dia nguc", "cereal", "xereal"
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
        if (m.planetId < 0 || m.planetId > 2) {
            return false;
        }
        if (MapService.gI().isMapNoNottify(m.mapId) || MapService.gI().isHome(m.mapId)) {
            return false;
        }
        String ten = (m.mapName == null) ? "" : m.mapName.toLowerCase();
        for (String c : NOI_CAM) {
            if (ten.contains(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mỗi cú đánh trừ đúng 1 HP.
     *
     * <p>Không gọi {@code super.injured}: bản gốc trừ theo sát thương thật, mà ở
     * đây sát thương thật không có nghĩa gì — người 2 tỉ sức đánh và người mới
     * chơi đều phải đánh đủ 100 cú.</p>
     */
    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        this.nPoint.subHP(MAU_MAT_MOI_DON);
        if (isDie()) {
            ghiNguoiTieuDiet(plAtt);
            this.setDie(plAtt);
            die(plAtt);
        }
        return MAU_MAT_MOI_DON;
    }

    /**
     * Chỉ làm đúng một việc: mỗi 15 giây thả một Thái dương hạ san.
     *
     * <p>Không gọi {@code super.attack()}: bản gốc chọn chiêu trong danh sách rồi
     * đánh thật, tức có đường ra sát thương. Ở đây chặn từ gốc thay vì trông vào
     * việc sức đánh đang bằng 0.</p>
     */
    @Override
    public void attack() {
        if (this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeAttack, 1000)) {
            return;
        }
        this.lastTimeAttack = System.currentTimeMillis();
        if (!Util.canDoWithTime(this.lucThaiDuongCuoi, NHIP_THAI_DUONG)) {
            return;
        }
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
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
