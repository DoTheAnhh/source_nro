package nro.entity.boss.legacy.tramhuydiet;

import nro.entity.boss.Boss;
import nro.entity.boss.BossID;
import nro.service.effect.EffectSkillService;
import nro.entity.boss.BossesData;
import nro.entity.map.ItemMap;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstTaskBadges;
import java.util.List;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.badges.BadgesTaskService;
import nro.service.TaskService;
import nro.server.ServerNotify;

public class Whis extends Boss {

    private long lastTimeNotifyAlive;
    private static final long TIME_NOTIFY_ALIVE = 10 * 60 * 1000;

    public Whis() throws Exception {
        super(BossID.WHIS, true, true, false, false, BossesData.WHIS);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        if (Util.isTrue(1, 3)) {
            ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, x + Util.nextInt(-15, 15), y, plKill.id);
        }

        if (Util.isTrue(1, 2)) {
            int group = Util.nextInt(1, 100) <= 70 ? 0 : 1;
            int[][] drops = {
                {230, 231, 232, 234, 235, 236, 238, 239, 240, 242, 243, 244, 246, 247, 248, 250, 251, 252, 266, 267, 268, 270, 271, 272, 274, 275, 276},
                {254, 255, 256, 258, 259, 260, 262, 263, 264, 278, 279, 280}
            };
            int dropOptional = drops[group][Util.nextInt(0, drops[group].length - 1)];
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, 1,
                    x + Util.nextInt(-15, 15), y, plKill.id);

            List<ItemOption> optionalOps = ItemService.gI().getListOptionItemShop((short) dropOptional);
            optionalOps.forEach(option -> option.param = (int) (option.param * Util.nextInt(100, 115) / 100.0));
            optionalItemMap.options.addAll(optionalOps);

            int value = 0;
            if (Util.isTrue(40, 100)) {
                value = Util.nextInt(1, 3);
            } else if (Util.isTrue(10, 100)) {
                value = Util.nextInt(4, 5);
            }
            optionalItemMap.options.add(new ItemOption(107, value));
        }

        if (Util.isTrue(1, 2)) {
            int[] dropItems = {15, 16, 17, 18, 19, 20};
            int dropOptional = dropItems[Util.nextInt(0, dropItems.length - 1)];
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, Util.nextInt(1, 2),
                    x + Util.nextInt(-15, 15), y, plKill.id);
        }

        ItemMap it = new ItemMap(this.zone, 190, Util.nextInt(28000, 30000),
                x + Util.nextInt(-15, 15), y, plKill.id);

        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void active() {
//        if (BossManager.gI().getBossById(BossID.BERUS) == null) {
//            this.leaveMap();
//        }
        this.nPoint.khangTDHS = true;
        notifyAliveBoss();
        super.active();
    }

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (Util.isTrue(50, 100) && plAtt != null) { // tỉ lệ hụt của thiên sứ
            if (Util.isTrue(80, 100)) {
                this.chat("Hãy Để Bản Năng Tự Vận Động");
                this.chat("Tránh Các Động Tác Thừa");
            } else if (Util.isTrue(80, 100)) {
                this.chat("Đây Chính Là Bản Năng Vô Cực");
            }
            damage = 0;
        }

        if (!this.isDie()) {
            if (Util.neDuocDon(this, plAtt, piercing)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (damage >= 1) {
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

    private void notifyAliveBoss() {
        if (this.zone != null && !this.isDie()
                && Util.canDoWithTime(this.lastTimeNotifyAlive, TIME_NOTIFY_ALIVE)) {
            ServerNotify.gI().notify("Boss " + this.name + " vẫn đang xuất hiện tại " + this.zone.map.mapName);
            this.lastTimeNotifyAlive = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        this.lastTimeNotifyAlive = System.currentTimeMillis();
        if (this.zone != null) {
            ServerNotify.gI().notify("Boss " + this.name + " đã xuất hiện tại " + this.zone.map.mapName);
        }
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
    }
}