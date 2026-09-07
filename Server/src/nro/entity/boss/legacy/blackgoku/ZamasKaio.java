package nro.entity.boss.legacy.blackgoku;

import nro.service.effect.EffectSkillService;
import nro.entity.boss.BossesData;
import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.entity.player.Player;
import nro.server.Manager;
import nro.service.Service;
import nro.core.util.Util;
import java.util.ArrayList;
import java.util.List;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.entity.map.ItemMap;
import nro.service.TaskService;

public class ZamasKaio extends Boss {

    public ZamasKaio() throws Exception {
        super(BossID.ZAMASZIN, BossesData.ZAMAS);
    }

    @Override
    public void reward(Player plKill) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // ================== 1. Đồ thường (Long đỏ -> Lưỡng Long, index 2–5 + rada SKHVip) ==================
        if (Util.isTrue(1, 1)) {
            // random 50/50: rơi từ doSKHVip hoặc từ radaSKHVip
            if (Util.isTrue(1, 2)) {
                // ---- Rơi từ doSKHVip ----
                short[][][] doSKHVip = Manager.doSKHVip;
                short[][] planetItems = doSKHVip[Util.nextInt(0, doSKHVip.length - 1)];
                short[] part = planetItems[Util.nextInt(0, planetItems.length - 1)];

                if (part.length > 5) {
                    int idx = Util.nextInt(2, 5); // chỉ lấy đồ Long đỏ → Lưỡng Long
                    short itemId = part[idx];
                }
            } else {
                // ---- Rơi từ rada SKH VIP ----
                short[] rada = Manager.radaSKHVip;
                int idx = Util.nextInt(2, 5); // theo quy tắc: index 2–5 là lông đỏ → lưỡng long
                if (idx < rada.length) {
                    short itemId = rada[idx];
                }
            }
        }

        // ================== 2. Đồ Thần Linh (1/30) ==================
        if (Util.isTrue(1, 3)) {
            short[] thanLinh = {555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567};
            short itemId = thanLinh[Util.nextInt(0, thanLinh.length - 1)];
        }

        // ================== 3. Ngọc Rồng 3 sao (1/5) ==================
        if (Util.isTrue(1, 2)) {
            ItemMap nr3 = new ItemMap(this.zone, (short) 16, 1,
                    x + Util.nextInt(-15, 15), y, plKill.id);
        }

        // ================== 4. Vàng (luôn rơi) ==================
        ItemMap vang = new ItemMap(this.zone, (short) 190,
                Util.nextInt(28000, 30000), x + Util.nextInt(-15, 15), y, plKill.id);

        // Check nhiệm vụ giết boss
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }


    private boolean isNonScaleOption(int id) {
        return id == 30 || id == 73 || id == 93 || id == 199 || id == 246;
    }

    private int getRandomStar() {
        int r = Util.nextInt(100);
        if (r < 50) {
            return Util.nextInt(1, 4); // 1–3 sao
        }
        if (r < 80) {
            return 4;
        }
        return 5;
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void joinMap() {
        super.joinMap();
    }

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (Util.neDuocDon(this, plAtt, piercing)) {
                this.chat("Xí hụt");
                return 0;
            }

            damage = this.nPoint.subDameInjureWithDeff(damage / 2);

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

    @Override
    public void leaveMap() {
        super.leaveMap();
    }
}