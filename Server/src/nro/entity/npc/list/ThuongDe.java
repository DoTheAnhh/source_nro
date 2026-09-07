package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.entity.boss.BossID;
import nro.service.fun.ChangeMapService;
import nro.service.fun.LuckyRound;
import nro.service.NpcService;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.boss.map.trainingboss.TrainningService;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.service.shop.ShopService;

public class ThuongDe extends Npc {

    // ===== LuckyRound bằng thỏi vàng =====
    private static final int MENU_LUCKYROUND_USING_THOI_VANG = 2005;
    private static final short THOI_VANG_ID = 457; 
    // ===================================

    public ThuongDe(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {


            if (player.zone.map.mapId == 45) {
                if (player.clan != null && player.clan.ConDuongRanDoc != null
                        && player.joinCDRD && player.clan.ConDuongRanDoc.allMobsDead && !player.talkToThuongDe) {
                    Service.gI().sendThongBao(player, "Hãy xuống gặp thần mèo Karin");
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy xuống gặp thần mèo Karin", "OK");
                    return;
                }
            } else if (player.zone.map.mapId == 141) {
                this.createOtherMenu(player, 0, "Hãy nắm lấy tay ta mau!", "về\nthần điện");
                return;
            }

            switch (player.levelLuyenTap) {
                case 2:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Pôpô là đệ tử của ta, luyện tập với Pôpô con sẽ có thêm nhiều kinh nghiệm\nđánh bại được Pôpô ta sẽ dạy võ công cho con",
                            player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động",
                            "Tập luyện\nvới\nMr.PôPô",
                            "Thách đấu\nMr.PôPô",
                            "Đến\nKaio",
                            "Vòng quay\nMay mắn");
                    break;
                case 3:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Từ nay con sẽ là đệ tử của ta. Ta sẽ truyền cho con tất cả tuyệt kĩ",
                            player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động",
                            "Tập luyện\nvới\nThượng Đế",
                            "Thách đấu\nThượng Đế",
                            "Đến\nKaio",
                            "Vòng quay\nMay mắn");
                    break;
                default:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Con đã mạnh hơn ta, ta sẽ chỉ đường cho con đến Kaio\nđể gặp thần Vũ Trụ Phương Bắc\nNgài là thần cai quản vũ trụ này, hãy theo ngài ấy học võ công.",
                            player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động",
                            "Tập luyện\nvới\nMr.PôPô",
                            "Tập luyện\nvới\nThượng Đế",
                            "Đến\nKaio",
                            "Vòng quay\nMay mắn");
                    break;
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {


            switch (mapId) {
                case 45:
                    if (player.iDMark.isBaseMenu()) {
                        switch (select) {
                            case 0:
                                if (player.clan != null && player.clan.ConDuongRanDoc != null
                                        && player.joinCDRD && player.clan.ConDuongRanDoc.allMobsDead && !player.talkToThuongDe) {
                                    player.talkToThuongDe = true;
                                    return;
                                }
                                if (player.dangKyTapTuDong) {
                                    player.dangKyTapTuDong = false;
                                    NpcService.gI().createTutorial(player, tempId, avartar,
                                            "Con đã hủy thành công đăng ký tập tự động\ntừ giờ con muốn tập Offline hãy tự đến đây trước");
                                    return;
                                }
                                this.createOtherMenu(player, 2001,
                                        "Đăng ký để mỗi khi Offline quá 30 phút, con sẽ được tự động luyện tập với tốc độ 1280 sức mạnh mỗi phút",
                                        "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
                                break;

                            case 1:
                                switch (player.levelLuyenTap) {
                                    case 3:
                                        this.createOtherMenu(player, 2002,
                                                "Con có chắc muốn tập luyện ?\nTập luyện với ta sẽ tăng 160 sức mạnh mỗi phút",
                                                "Đồng ý\nluyện tập", "Không\nđồng ý");
                                        break;
                                    default:
                                        this.createOtherMenu(player, 2002,
                                                "Con có chắc muốn tập luyện ?\nTập luyện với Mr.PôPô sẽ tăng 80 sức mạnh mỗi phút",
                                                "Đồng ý\nluyện tập", "Không\nđồng ý");
                                        break;
                                }
                                break;

                            case 2:
                                switch (player.levelLuyenTap) {
                                    case 2:
                                        this.createOtherMenu(player, 2003,
                                                "Con có chắc muốn thách đấu ?\nNếu thắng Mr.PôPô sẽ được tập với ta, tăng 160 sức mạnh mỗi phút",
                                                "Đồng ý\ngiao đấu", "Không\nđồng ý");
                                        break;
                                    case 3:
                                        this.createOtherMenu(player, 2003,
                                                "Con có chắc muốn thách đấu ?\nNếu thắng được ta, con sẽ được học võ với người mạnh hơn ta để tăng đến 320 sức mạnh mỗi phút",
                                                "Đồng ý\ngiao đấu", "Không\nđồng ý");
                                        break;
                                    default:
                                        this.createOtherMenu(player, 2003,
                                                "Con có chắc muốn tập luyện ?\nTập luyện với ta sẽ tăng 160 sức mạnh mỗi phút",
                                                "Đồng ý\nluyện tập", "Không\nđồng ý");
                                        break;
                                }
                                break;

                            case 3:
                                ChangeMapService.gI().changeMapBySpaceShip(player, 48, -1, 354);
                                break;

                            case 4:
                                this.createOtherMenu(player, ConstNpc.MENU_CHOOSE_LUCKY_ROUND,
                                        "Con muốn làm gì nào?",
                                        "Quay bằng\nthỏi vàng",
                                        "Rương phụ\n("
                                                + (player.inventory.itemsBoxCrackBall.size()
                                                - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsBoxCrackBall))
                                                + " món)",
                                        "Xóa hết\ntrong rương",
                                        "Đóng");
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
                                NpcService.gI().createTutorial(player, tempId, avartar,
                                        "Từ giờ, quá 30 phút Offline con sẽ được tự động luyện tập");
                                break;
                        }

                    } else if (player.iDMark.getIndexMenu() == 2002) {
                        switch (select) {
                            case 0:
                                switch (player.levelLuyenTap) {
                                    case 3:
                                        TrainningService.gI().callBoss(player, BossID.THUONG_DE, false);
                                        break;
                                    default:
                                        TrainningService.gI().callBoss(player, BossID.MR_POPO, false);
                                        break;
                                }
                                break;
                        }

                    } else if (player.iDMark.getIndexMenu() == 2003) {
                        switch (select) {
                            case 0:
                                switch (player.levelLuyenTap) {
                                    case 2:
                                        TrainningService.gI().callBoss(player, BossID.MR_POPO, true);
                                        break;
                                    case 3:
                                        TrainningService.gI().callBoss(player, BossID.THUONG_DE, true);
                                        break;
                                    default:
                                        TrainningService.gI().callBoss(player, BossID.THUONG_DE, false);
                                        break;
                                }
                                break;
                        }

                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_CHOOSE_LUCKY_ROUND) {
                        switch (select) {
                            case 0:
                                this.createOtherMenu(player, MENU_LUCKYROUND_USING_THOI_VANG,
                                        "|7|Con muốn quay mấy lần?\nMỗi lần tốn 1 thỏi vàng\nDọn dẹp rương trước khi quay",
                                        "Quay\n1 lần", "Quay\n10 lần", "Quay\n100 lần", "Đóng");
                                break;

                            case 1:
                                ShopService.gI().opendShop(player, "ITEMS_LUCKY_ROUND", true);
                                break;

                            case 2:
                                NpcService.gI().createMenuConMeo(player,
                                        ConstNpc.CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND, this.avartar,
                                        "Con có chắc muốn xóa hết vật phẩm trong rương phụ? Sau khi xóa "
                                                + "sẽ không thể khôi phục!",
                                        "Đồng ý", "Hủy bỏ");
                                break;
                        }

                    } else if (player.iDMark.getIndexMenu() == MENU_LUCKYROUND_USING_THOI_VANG) {
                        int times;
                        switch (select) {
                            case 0: times = 1; break;
                            case 1: times = 10; break;
                            case 2: times = 100; break;
                            default: return; // đóng
                        }
                        Item thoiVang = InventoryService.gI().findItemBag(player, THOI_VANG_ID);
                        if (thoiVang == null || thoiVang.quantity < times) {
                            Service.gI().sendThongBao(player,
                                    "Bạn không đủ thỏi vàng " + THOI_VANG_ID + " Cần " + times + " thỏi vàng!");
                            return;
                        }
                        LuckyRound.gI().quayBangThoiVang(player, times);
                        return;
                    }
                    break;

                case 141:
                    switch (select) {
                        case 0:
                            if (player.clan == null || player.clan.ConDuongRanDoc == null
                                    || !player.clan.ConDuongRanDoc.allMobsDead) {
                                Service.gI().sendThongBao(player, "Chưa hạ hết đối thủ");
                                return;
                            }
                            ChangeMapService.gI().changeMapYardrat(player,
                                    ChangeMapService.gI().getMapCanJoin(player, 45), 295, 408);
                            Service.gI().sendThongBao(player, "Hãy xuống gặp thần mèo Karin");
                            break;
                    }
                    break;
            }
        }
    }
}