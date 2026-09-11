package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.entity.boss.BossID;
import nro.service.NpcService;
import nro.service.OpenPowerService;
import nro.service.Service;
import nro.core.util.FormatStyle;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.boss.map.trainingboss.TrainningService;
import nro.entity.npc.Npc;
import nro.entity.player.NPoint;
import nro.entity.player.Player;

public class ToSuKaio extends Npc {

    public ToSuKaio(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (player.nPoint.limitPower > 4) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Tập luyện với Tổ sư Kaio sẽ tăng " + Util.formatNumber(TrainningService.gI().getTnsmMoiPhut(player), FormatStyle.VIETNAMESE) + " sức mạnh mỗi phút, có thể tăng giảm tùy vào khả năng đánh quái của con",
                player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động", "Đồng ý\nluyện tập", "Không\nđồng ý", "Nâng\nGiới hạn\nSức mạnh");
            } else {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Tập luyện với Tổ sư Kaio sẽ tăng " + Util.formatNumber(TrainningService.gI().getTnsmMoiPhut(player), FormatStyle.VIETNAMESE) + " sức mạnh mỗi phút, có thể tăng giảm tùy vào khả năng đánh quái của con",
                player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động", "Đồng ý\nluyện tập", "Không\nđồng ý");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                    if (player.dangKyTapTuDong) {
                        player.dangKyTapTuDong = false;
                        NpcService.gI().createTutorial(player, tempId, avartar, "Con đã hủy thành công đăng ký tập tự động\ntừ giờ con muốn tập Offline hãy tự đến đây trước");
                        return;
                    }
                    this.createOtherMenu(player, 2001, "Đăng ký để mỗi khi Offline quá 30 phút, con sẽ được tự động luyện tập với tốc độ " + TrainningService.gI().getTnsmMoiPhut(player) + " sức mạnh mỗi phút",
                        "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
                    break;
                    case 1:
                        TrainningService.gI().callBoss(player, BossID.TO_SU_KAIO, false);
                        break;
                    case 3:
                        if (player.Detu != null) {
                            this.createOtherMenu(player, ConstNpc.GIOI_HAN_POWER,
                                    "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                                    "Bản thân", "Đệ tử", "Từ chối");
                        } else {
                            this.createOtherMenu(player, ConstNpc.GIOI_HAN_POWER,
                                    "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                                    "Bản thân", "Từ chối");
                        }
                    default:
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == 2001) {
                switch (select) {
                    case 0:
                        NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.TAP_TU_DONG);
                        break;
                    case 1:
                        player.mapIdDangTapTuDong = mapId;
                        player.dangKyTapTuDong = true;
                        NpcService.gI().createTutorial(player, tempId, avartar, "Từ giờ, quá 30 phút Offline con sẽ được tự động luyện tập");
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.GIOI_HAN_POWER) {
                if (player.Detu != null) {
                    switch (select) {
                        case 0:
                            if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                this.createOtherMenu(player, ConstNpc.NANG_GIOI_HAN_POWER,
                                        "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh \n"
                                        + "của bản thân lên " + Util.formatNumber(player.nPoint.getPowerNextLimit(), FormatStyle.VIETNAMESE) + ".\n"
                                        + "Lưu ý: từ 40 tỷ trở lên sức mạnh của con sẽ tăng chậm đáng kể",
                                        "Nâng ngay\n" + 199 + " ngọc", "OK");
                            } else {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Sức mạnh của con đã đạt tới giới hạn",
                                        "Đóng");
                            }
                            break;
                        case 1:
                            if (player.Detu != null) {
                                if (player.Detu.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                    this.createOtherMenu(player, ConstNpc.NANG_GIOI_HAN_POWER_PET,
                                            "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh \n"
                                            + "của đệ tử lên " + Util.formatNumber(player.Detu.nPoint.getPowerNextLimit(), FormatStyle.VIETNAMESE) + ".\n"
                                            + "Lưu ý: từ 40 tỷ trở lên sức mạnh của đệ tử sẽ tăng chậm đáng kể",
                                            "Nâng ngay\ncho đệ tử\n" + 199 + " ngọc", "OK");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                            "Sức mạnh của đệ con đã đạt tới giới hạn",
                                            "Đóng");
                                }
                            } else {
                                Service.gI().sendThongBao(player, "Không thể thực hiện");
                            }
                            break;
                    }
                } else {
                    switch (select) {
                        case 0:
                            if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                this.createOtherMenu(player, ConstNpc.NANG_GIOI_HAN_POWER,
                                        "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh \n"
                                        + "của bản thân lên " + Util.formatNumber(player.nPoint.getPowerNextLimit(), FormatStyle.VIETNAMESE) + ".\n"
                                        + "Lưu ý: từ 40 tỷ trở lên sức mạnh của con sẽ tăng chậm đáng kể",
                                        "Nâng ngay\n" + 199 + " ngọc", "OK");
                            } else {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Sức mạnh của con đã đạt tới giới hạn",
                                        "Đóng");
                            }
                            break;
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NANG_GIOI_HAN_POWER) {
                if (player.nPoint.limitPower < 5) {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                    return;
                }
                switch (select) {
                    // Chi con nut tra ngoc, tang ngay mot bac. Nut "mien phi" cu vua
                    // +1 ngay vua bat dong ho 24 gio, het gio lai +1 nua.
                    case 0:
                        if (player.inventory.getGemAndRuby() >= 199) {
                            if (OpenPowerService.gI().openPowerSpeed(player)) {
                                player.inventory.subGemAndRuby(199);
                                Service.gI().sendMoney(player);
                            }
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Bạn không đủ ngọc để mở, còn thiếu "
                                    + Util.formatNumber((199 - player.inventory.getGemAndRuby()), FormatStyle.VIETNAMESE) + " ngọc");
                        }
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NANG_GIOI_HAN_POWER_PET) {
                if (player.Detu.nPoint.limitPower < 5) {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                    return;
                }
                if (select == 0) {
                    if (player.inventory.getGemAndRuby() >= 199) {
                        if (OpenPowerService.gI().openPowerSpeed(player.Detu)) {
                            player.inventory.subGemAndRuby(199);
                            Service.gI().sendMoney(player);
                        }
                    } else {
                        Service.gI().sendThongBao(player,
                                "Bạn không đủ ngọc để mở, còn thiếu "
                                + Util.formatNumber((199 - player.inventory.getGemAndRuby()), FormatStyle.VIETNAMESE) + " ngọc");
                    }
                }
            }
        }
    }
}
