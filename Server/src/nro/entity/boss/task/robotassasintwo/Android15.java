package nro.entity.boss.task.robotassasintwo;

import nro.entity.boss.legacy.blackgoku.BlackGoku;
import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.boss.BossesData;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstTaskBadges;
import java.util.ArrayList;
import java.util.List;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.badges.BadgesTaskService;
import nro.entity.map.ItemMap;
import nro.server.Manager;

public class Android15 extends Boss {

    public boolean callApk13;

    public Android15() throws Exception {
        super(BossID.ANDROID_15, BossesData.ANDROID_15);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // ================== 1. Đồ thường Long đỏ -> Lưỡng Long, index 2-5 + rada SKH VIP ==================
        if (Util.isTrue(1, 10)) {
            // Random 50/50: rơi từ doSKHVip hoặc từ radaSKHVip
            if (Util.isTrue(1, 2)) {
                // Rơi từ doSKHVip
                short[][][] doSKHVip = Manager.doSKHVip;
                short[][] planetItems = doSKHVip[Util.nextInt(0, doSKHVip.length - 1)];
                short[] part = planetItems[Util.nextInt(0, planetItems.length - 1)];

                if (part.length > 5) {
                    int idx = Util.nextInt(2, 5); // Chỉ lấy đồ Long đỏ -> Lưỡng Long
                    short itemId = part[idx];
                }
            } else {
                // Rơi từ rada SKH VIP
                short[] rada = Manager.radaSKHVip;
                int idx = Util.nextInt(2, 5); // Theo quy tắc: index 2-5 là Long đỏ -> Lưỡng Long

                if (idx < rada.length) {
                    short itemId = rada[idx];
                }
            }
        }

        // ================== 2. Đồ Thần Linh ==================
        if (Util.isTrue(1, 100)) {
            short[] thanLinh = {555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567};
            short itemId = thanLinh[Util.nextInt(0, thanLinh.length - 1)];
        }

        // ================== 3. Ngọc Rồng ==================
        if (Util.isTrue(1, 10)) {
            ItemMap nr3 = new ItemMap(this.zone, (short) 15, 1,
                    x + Util.nextInt(-15, 15), y, plKill.id);
        }

        if (Util.isTrue(1, 100)) {
            ItemMap nr3 = new ItemMap(this.zone, (short) 14, 1,
                    x + Util.nextInt(-15, 15), y, plKill.id);
        }

        // ================== 4. Vàng luôn rơi ==================
        ItemMap vang = new ItemMap(this.zone, (short) 190,
                Util.nextInt(28000, 30000), x + Util.nextInt(-15, 15), y, plKill.id);

        try {
            if (plKill.playerTask != null
                    && plKill.playerTask.taskMain != null
                    && plKill.playerTask.taskMain.id == 31) {

                // Ví dụ: tỉ lệ 1/3
                if (Util.isTrue(1, 3)) {
                    ItemMap missionRing = new ItemMap(this.zone, (short) 992, 1,
                            x + Util.nextInt(-10, 10), y, plKill.id);
                }
            }
        } catch (Exception e) {
            Logger.logException(BlackGoku.class, e, "Lỗi drop nhẫn nhiệm vụ 31");
        }

        // Check nhiệm vụ giết boss
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }


    private boolean isNonScaleOption(int id) {
        return id == 30 || id == 73 || id == 93 || id == 199 || id == 246;
    }

    private int getRandomStar() {
        int r = Util.nextInt(100);

        if (r < 50) {
            return Util.nextInt(1, 4); // 1-3 sao
        }

        if (r < 80) {
            return 4;
        }

        return 5;
    }

    @Override
    protected void resetBase() {
        super.resetBase();
        this.callApk13 = false;
    }

    @Override
    public void active() {
        this.attack();
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.callApk13 && damage >= this.nPoint.hp) {
            if (this.parentBoss != null) {
                ((Android14) this.parentBoss).callApk13();
            }
            return 0;
        }

        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
    }
}