package nro.service;

import nro.entity.player.NPoint;
import nro.entity.player.Detu;
import nro.entity.player.Player;
import nro.service.power.PowerLimitManager;

public class OpenPowerService {

    public static final int RUBY_SPEED_OPEN_LIMIT_POWER = 100;

    private static OpenPowerService i;

    private OpenPowerService() {

    }

    public static OpenPowerService gI() {
        if (i == null) {
            i = new OpenPowerService();
        }
        return i;
    }

    // Cach mo "mien phi cho 24 gio" da bo: mo gioi han la tra vang o Quoc
    // Vuong va tang ngay (openPowerSpeed). Ban cu vua +1 ngay vua bat dong ho,
    // het gio lai +1 nua.

    public boolean openPowerSpeed(Player player) {
        if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
            player.nPoint.limitPower++;

            if (player.nPoint.limitPower > NPoint.MAX_LIMIT) {
                player.nPoint.limitPower = NPoint.MAX_LIMIT;
            }

            player.nPoint.powerLimit = PowerLimitManager.getInstance().get(player.nPoint.limitPower);

            if (!player.isDeTu) {
                Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được tăng lên 1 bậc");
            } else {
                Service.gI().sendThongBao(((Detu) player).master, "Giới hạn sức mạnh của đệ tử đã được tăng lên 1 bậc");
            }

            return true;
        } else {
            if (!player.isDeTu) {
                Service.gI().sendThongBao(player, "Sức mạnh của bạn đã đạt tới mức tối đa");
            } else {
                Service.gI().sendThongBao(((Detu) player).master, "Sức mạnh của đệ tử đã đạt tới mức tối đa");
            }

            return false;
        }
    }
}