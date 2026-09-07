package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstTranhNgocNamek;
import nro.service.map.dragonnamecwar.TranhNgocService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;

public class Fide extends Npc {

    public Fide(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            if (mapId == ConstTranhNgocNamek.MAP_ID) {
                if (player.iDMark.getTranhNgoc() == 1) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Phắn đê! Ta không nói chuyện với sinh vật hạ đẳng", "Đóng");
                    return;
                }
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Hãy mang ngọc rồng về cho ta", "Đưa ngọc", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.getIndexMenu() == ConstNpc.BASE_MENU) {
                if (this.mapId == ConstTranhNgocNamek.MAP_ID) {
                    switch (select) {
                        case 0: {
                            if (player.iDMark.getTranhNgoc() == 2 && player.isHoldNamecBallTranhDoat) {
                                if (!Util.canDoWithTime(player.lastTimePickItem, 20000)) {
                                    Service.gI().sendThongBao(player, "Vui lòng đợi " + ((player.lastTimePickItem + 20000 - System.currentTimeMillis()) / 1000) + " giây để có thể trả");
                                    return;
                                }
                                TranhNgocService.getInstance().dropBall(player, (byte) 2);
                                player.zone.pointFide++;
                                if (player.zone.pointFide > ConstTranhNgocNamek.MAX_POINT) {
                                    player.zone.pointFide = ConstTranhNgocNamek.MAX_POINT;
                                }
                                TranhNgocService.getInstance().sendUpdatePoint(player);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
