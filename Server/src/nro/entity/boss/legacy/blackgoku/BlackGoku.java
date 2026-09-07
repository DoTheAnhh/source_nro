package nro.entity.boss.legacy.blackgoku;

import nro.service.effect.EffectSkillService;
import nro.entity.boss.BossesData;
import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstTaskBadges;
import java.util.ArrayList;
import java.util.List;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.badges.BadgesTaskService;
import nro.entity.map.ItemMap;
import nro.server.Manager;
import nro.service.TaskService;
import nro.service.skill.SkillService;

public class BlackGoku extends Boss {

    private long st;
    private int timeLeaveMap;
    private long spawnTime;
    private static final int RESET_AFTER = 30 * 60 * 1000;

    public BlackGoku() throws Exception {
        super(BossID.BLACK_GOKU, false, true, false, false, BossesData.BLACK_GOKU, BossesData.SUPER_BLACK_GOKU);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
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
        if (Util.isTrue(4, 10)) {
            short[] thanLinh = {555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567};
            short itemId = thanLinh[Util.nextInt(0, thanLinh.length - 1)];
        }

        // ================== 3. Ngọc Rồng 3 sao (1/5) ==================
        if (Util.isTrue(1, 5)) {
            ItemMap nr3 = new ItemMap(this.zone, (short) 15, 1,
                    x + Util.nextInt(-15, 15), y, plKill.id);
        }

        if (Util.isTrue(1, 10)) {
            ItemMap nr3 = new ItemMap(this.zone, (short) 14, 1,
                    x + Util.nextInt(-15, 15), y, plKill.id);
        }

        // thỏi vàng
        if (Util.isTrue(2, 5)) {
            ItemMap nr3 = new ItemMap(this.zone, (short) 457, 10,
                    x + Util.nextInt(-15, 15), y, plKill.id);
        }

        // ================== 4. Vàng (luôn rơi) ==================
        ItemMap vang = new ItemMap(this.zone, (short) 190,
                Util.nextInt(28000, 30000), x + Util.nextInt(-15, 15), y, plKill.id);

        try {
            if (plKill.playerTask != null
                    && plKill.playerTask.taskMain != null
                    && plKill.playerTask.taskMain.id == 31) {

                // Ví dụ: tỉ lệ 1/3 (33%)
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
            return Util.nextInt(1, 4); // 1–3 sao
        }
        if (r < 80) {
            return 4;
        }
        return 5;
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (Util.neDuocDon(this, plAtt, piercing)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (this.currentLevel != 0) {
                damage /= 2;
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

    @Override
    public void autoLeaveMap() {
        if (this.zone != null && !this.isDie() && Util.canDoWithTime(spawnTime, RESET_AFTER)) {
            this.chat("Ta sẽ quay lại mạnh hơn!");
            this.leaveMapNew();
            return;
        }

        if (Util.canDoWithTime(st, timeLeaveMap)) {
            if (Util.isTrue(1, 2)) {
                this.leaveMap();
            } else {
                this.leaveMapNew();
            }
        }

        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
            timeLeaveMap = Util.nextInt(1800000, 3600000);
        }
    }

    @Override
    public void joinMap() {
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        super.joinMap();
        st = System.currentTimeMillis();
        timeLeaveMap = Util.nextInt(1800000, 3600000);
        spawnTime = System.currentTimeMillis();
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

                this.playerSkill.skillSelect = this.playerSkill.skills.get(
                        Util.nextInt(0, this.playerSkill.skills.size() - 1)
                );

                int dis = Util.getDistance(this, pl);

                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int move = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int move = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            } catch (Exception ex) {
                Logger.logException(Boss.class, ex);
            }
        }
    }
}