package nro.entity.matches.pvp;

import nro.entity.matches.PVP;
import nro.entity.matches.TYPE_LOSE_PVP;
import nro.entity.matches.TYPE_PVP;
import nro.entity.player.Player;

/**
 * @author DoTheAnh
 */

public class DHVT extends PVP {

    public DHVT(Player p1, Player p2) {
        super(TYPE_PVP.THACH_DAU, p1, p2);
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void finish() {

    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void update() {
    }

    @Override
    public void reward(Player plWin) {
    }

    @Override
    public void sendResult(Player plLose, TYPE_LOSE_PVP typeLose) {
    }

}






