package nro.entity.boss.map.trainingboss;

/*
 * @author DoTheAnh
 */

import nro.entity.boss.BossID;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.PHOBAN;
import nro.entity.boss.BossesData;
import nro.service.fun.ChangeMapService;
import nro.service.MapService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstTask;
import java.io.IOException;
import nro.net.io.Message;
import nro.entity.player.Player;

public class TauPayPay extends TrainningBoss {

    public TauPayPay(Player player) throws Exception {
        super(PHOBAN, BossID.TAUPAYPAY, BossesData.TAUPAYPAY);
        this.playerAtt = player;
    }
    
    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            ChangeMapService.gI().changeMapBySpaceShip(this, this.zone, 775);
            this.changeStatus(BossStatus.CHAT_S);
        }
    }

    @Override
    public void checkPlayerDie(Player pl) {
        Service.gI().sendPVB(playerAtt, this, ConstPlayer.PK_PVP);
        TaskService.gI().doneTask(pl, ConstTask.TASK_9_1);
    }

    @Override
    public void die(Player plKill) {
        this.changeStatus(BossStatus.LEAVE_MAP);
        this.chatE();
        this.lastTimeAFK = 0;
        Service.gI().sendPlayerVS(playerAtt, null, (byte) 0);
        TaskService.gI().doneTask(plKill, ConstTask.TASK_10_1);
    }

    @Override
    public boolean chatS() {
        if (Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            if (this.doneChatS) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextS()[playerAtt.isThachDau ? 1 : 0];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.moveToPlayer(playerAtt);
            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = 2000;
            doneChatS = true;
        }
        return false;
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
        Message msg;
        try {
            msg = new Message(-6);
            msg.writer().writeInt((int) this.id);
            playerAtt.sendMessage(msg);
            msg.cleanup();
            this.zone = null;
        } catch (IOException e) {
            Logger.logException(MapService.class, e);
        }
    }

    @Override
    public void buffPea() {
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(400, 1000)) {
                this.chat("Xí hụtt");
                return 0;
            }

            if (TaskService.gI().getIdTask(plAtt) != ConstTask.TASK_10_1) {
                return 100;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            this.nPoint.subHP(damage);

            if (this.nPoint.hp > 0 && this.nPoint.hp < this.nPoint.hpMax / 5) {
                if (Util.canDoWithTime(lastTimeChat, 2000)) {
                    String[] text = {"AAAAAAAAA", "ai da"};
                    this.chat(text[Util.nextInt(text.length)]);
                }
            }

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
    public void afk() {
        if (Util.canDoWithTime(lastTimeAFK, 5000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }
}






