package nro.entity.boss.map.trainingboss;

import nro.entity.boss.BossesData;
import nro.entity.boss.BossID;
import nro.entity.player.Player;
import nro.entity.boss.BossStatus;
import static nro.entity.boss.BossType.PHOBAN;
import nro.service.fun.ChangeMapService;
import nro.core.util.Util;

public class ThanVuTru extends TrainningBoss {

    public ThanVuTru(Player player) throws Exception {
        super(PHOBAN, BossID.THAN_VU_TRU, BossesData.THAN_VU_TRU);
        playerAtt = player;
    }

    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            ChangeMapService.gI().changeMap(this, this.zone, 420, 240);
            this.changeStatus(BossStatus.CHAT_S);
        }
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
    public void afk() {
        if (Util.canDoWithTime(lastTimeMove, 1000)) {
            this.goToXY(playerAtt.location.x, Util.getOne(240, 360));
            this.lastTimeMove = System.currentTimeMillis();
        }
        if (Util.canDoWithTime(lastTimeAFK, 2000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }
}





