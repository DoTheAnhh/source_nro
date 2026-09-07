package nro.entity.map.treasureundersea;

/*
 * @Author: DoTheAnh
 */

import nro.service.effect.EffectMapService;
import nro.entity.player.Player;
import nro.service.PlayerService;
import nro.core.util.Util;

public class TrapMap {

    public int x;
    public int y;
    public int w;
    public int h;
    public int effectId;
    public long dame;

    public void doPlayer(Player player) {
        if (this.effectId == 49) {
            if (!player.isDie() && Util.canDoWithTime(player.iDMark.getLastTimeAnXienTrapBDKB(), 1000)) {
                player.injured(null, Util.CrisGH(dame + (Util.nextLong(-10L, 10L) * dame / 100L)), false, false);
                PlayerService.gI().sendInfoHp(player);
                EffectMapService.gI().sendEffectMapToAllInMap(player.zone, effectId, 2, 1, player.location.x - 32, 1040, 1);
                player.iDMark.setLastTimeAnXienTrapBDKB(System.currentTimeMillis());
            }
        }
    }

}






