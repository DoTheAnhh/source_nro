package nro.npc.ListNpc;

/**
 * @author Văn Khải
 */

import nro.services.Service;
import Utils.FormatStyle;
import Utils.Util;
import consts.ConstNpc;
import consts.ConstPlayer;
import nro.npc.Npc;
import nro.player.Player;

public class QuaTrung extends Npc {

    private final int COST_AP_TRUNG_NHANH = 1000000000;

    public QuaTrung(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == (21 + player.gender)) {
                player.mabuEgg.sendMabuEgg();
                if (player.mabuEgg.getSecondDone() != 0) {
                    this.createOtherMenu(player, ConstNpc.CAN_NOT_OPEN_EGG, "Bư bư bư...",
                            "Hủy bỏ\ntrứng", "Ấp nhanh\n" + Util.formatNumber(COST_AP_TRUNG_NHANH, FormatStyle.VIETNAMESE) + " vàng",
                            "Đóng");
                } else {
                    this.createOtherMenu(player, ConstNpc.CAN_OPEN_EGG, "Bư bư bư...", "Nở", "Hủy bỏ\ntrứng",
                            "Đóng");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == (21 + player.gender)) {
                switch (player.iDMark.getIndexMenu()) {
                    case ConstNpc.CAN_NOT_OPEN_EGG: {
                        if (select == 0) {
                            this.createOtherMenu(player, ConstNpc.CONFIRM_DESTROY_EGG,
                                    "Bạn có chắc chắn muốn hủy bỏ trứng Mabư?", "Đồng ý", "Từ chối");
                        } else if (select == 1) {
                            if (player.inventory.gold >= COST_AP_TRUNG_NHANH) {
                                player.inventory.gold -= COST_AP_TRUNG_NHANH;
                                player.mabuEgg.timeDone = 0;
                                Service.gI().sendMoney(player);
                                player.mabuEgg.sendMabuEgg();
                            } else {
                                Service.gI().sendThongBao(player,
                                        "Bạn không đủ vàng để thực hiện, còn thiếu "
                                        + Util.formatNumber(
                                                (COST_AP_TRUNG_NHANH - player.inventory.gold), FormatStyle.VIETNAMESE)
                                        + " vàng");
                            }
                        }
                        break;
                    }
                    case ConstNpc.CAN_OPEN_EGG: {
                        switch (select) {
                            case 0:
                                this.createOtherMenu(player, ConstNpc.CONFIRM_OPEN_EGG,
                                        "Bạn có chắc chắn cho trứng nở?\nĐệ tử của bạn sẽ được thay thế bằng đệ Mabư",
                                        "Đệ mabư\nTrái Đất", "Đệ mabư\nNamếc", "Đệ mabư\nXayda", "Từ chối");
                                break;
                            case 1:
                                this.createOtherMenu(player, ConstNpc.CONFIRM_DESTROY_EGG,
                                        "Bạn có chắc chắn muốn hủy bỏ trứng Mabư?", "Đồng ý", "Từ chối");
                                break;
                        }
                        break;
                    }
                    case ConstNpc.CONFIRM_OPEN_EGG: {
                        switch (select) {
                            case 0:
                                player.mabuEgg.openEgg(ConstPlayer.TRAI_DAT);
                                break;
                            case 1:
                                player.mabuEgg.openEgg(ConstPlayer.NAMEC);
                                break;
                            case 2:
                                player.mabuEgg.openEgg(ConstPlayer.XAYDA);
                                break;
                            default: {
                                break;
                            }
                        }
                        break;
                    }
                    case ConstNpc.CONFIRM_DESTROY_EGG: {
                        if (select == 0) {
                            player.mabuEgg.destroyEgg();
                        }
                        break;
                    }
                }
            }
        }
    }
}
