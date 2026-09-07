package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.inventory.InventoryService;
import nro.entity.boss.BossID;
import nro.service.fun.ChangeMapService;
import nro.service.fun.Input;
import nro.service.NpcService;
import nro.service.Service;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.boss.map.trainingboss.TrainningService;
import nro.service.map.snakeway.SnakeWayService;
import nro.entity.npc.Npc;
import static nro.service.npc.NpcFactory.PLAYERID_OBJECT;
import nro.entity.player.Player;

public class ThanVuTru extends Npc {

    public ThanVuTru(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 48) {
                switch (player.levelLuyenTap) {
                    case 4:
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Thượng đế đưa ngươi đến đây, chắc muốn ta dạy võ chứ gì\nBắt được con khỉ Bubbles rồi hãy tính",
                                player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động", "Tập luyện\nvới\nBubbles", "Thách đấu\nBubbles", "Di chuyển");
                        break;
                    case 5:
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ta là Thần Vũ Trụ Phương Bắc cai quản khu vực bắc vũ trụ\nnếu thắng được ta, ngươi sẽ được đến\nLãnh Địa Kaio, nơi ở của Thần Linh",
                                player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động", "Tập luyện\nvới\nThần Vũ Trụ", "Thách đấu\nThần Vũ Trụ", "Di chuyển");
                        break;
                    default:
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Con mạnh nhất phía bắc vũ trụ này rồi đấy\nnhưng ngoài vũ trụ bao la kia vẫn có những kẻ mạnh hơn nhìu\ncon cần phải tập luyện để mạnh hơn nữa",
                                player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động", "Tập luyện\nvới\nBubbles", "Tập luyện\nvới\nThần Vũ Trụ", "Di chuyển");
                        break;
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 48) {
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0:
                            if (player.dangKyTapTuDong) {
                                player.dangKyTapTuDong = false;
                                NpcService.gI().createTutorial(player, tempId, avartar, "Con đã hủy thành công đăng ký tập tự động\ntừ giờ con muốn tập Offline hãy tự đến đây trước");
                                return;
                            }
                            this.createOtherMenu(player, 2001, "Đăng ký để mỗi khi Offline quá 30 phút, con sẽ được tự động luyện tập với tốc độ 1280 sức mạnh mỗi phút",
                                    "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
                            break;
                        case 1:
                            switch (player.levelLuyenTap) {
                                case 5:
                                    this.createOtherMenu(player, 2002, "Con có chắc muốn tập luyện ?\nTập luyện với ta sẽ tăng 640 sức mạnh mỗi phút",
                                            "Đồng ý\nluyện tập", "Không\nđồng ý");
                                    break;
                                default:
                                    this.createOtherMenu(player, 2002, "Con có chắc muốn tập luyện ?\nTập luyện với Khỉ Bubbles sẽ tăng 320 sức mạnh mỗi phút",
                                            "Đồng ý\nluyện tập", "Không\nđồng ý");
                                    break;
                            }
                            break;
                        case 2:
                            switch (player.levelLuyenTap) {
                                case 4:
                                    this.createOtherMenu(player, 2003, "Con có chắc muốn thách đấu ?\nNếu thắng Khỉ Bubbles sẽ được tập với ta, tăng 640 sức mạnh mỗi phút",
                                            "Đồng ý\ngiao đấu", "Không\nđồng ý");
                                    break;
                                case 5:
                                    this.createOtherMenu(player, 2003, "Con có chắc muốn thách đấu ?\nNếu thắng được ta, con sẽ được học võ với người mạnh hơn ta để tăng đến 1280 sức mạnh mỗi phút",
                                            "Đồng ý\ngiao đấu", "Không\nđồng ý");
                                    break;
                                default:
                                    this.createOtherMenu(player, 2003, "Con có chắc muốn tập luyện ?\nTập luyện với ta sẽ tăng 640 sức mạnh mỗi phút",
                                            "Đồng ý\nluyện tập", "Không\nđồng ý");
                                    break;
                            }
                            break;
                        case 3:
                            this.createOtherMenu(player, ConstNpc.MENU_DI_CHUYEN,
                                    "Ta sẽ đưa con đi",
                                    "Về\nthần điện", "Thánh địa\nKaio", "Con\nđường\nrắn độc", "Từ chối");
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

                } else if (player.iDMark.getIndexMenu() == 2002) {
                    switch (select) {
                        case 0:
                            switch (player.levelLuyenTap) {
                                case 5:
                                    TrainningService.gI().callBoss(player, BossID.THAN_VU_TRU, false);
                                    break;
                                default:
                                    TrainningService.gI().callBoss(player, BossID.KHI_BUBBLES, false);
                                    break;
                            }
                            break;
                    }
                } else if (player.iDMark.getIndexMenu() == 2003) {
                    switch (select) {
                        case 0:
                            switch (player.levelLuyenTap) {
                                case 4:
                                    TrainningService.gI().callBoss(player, BossID.KHI_BUBBLES, true);
                                    break;
                                case 5:
                                    TrainningService.gI().callBoss(player, BossID.THAN_VU_TRU, true);
                                    break;
                                default:
                                    TrainningService.gI().callBoss(player, BossID.THAN_VU_TRU, false);
                                    break;
                            }
                            break;
                    }
                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_DI_CHUYEN) {
                    switch (select) {
                        case 0:
                            ChangeMapService.gI().changeMapBySpaceShip(player, 45, -1, 354);
                            break;
                        case 1:
                            ChangeMapService.gI().changeMap(player, 50, -1, 318, 336);
                            break;
                        case 2:
                            if (player.clan != null) {
                                if (player.clan.ConDuongRanDoc != null) {
                                    this.createOtherMenu(player, 2,
                                            "Bang hội con đang ở con đường rắn độc cấp độ "
                                            + player.clan.ConDuongRanDoc.level + "\ncon có muốn đi cùng họ không? ("
                                            + TimeUtil.convertTimeNow(player.clan.ConDuongRanDoc.getLastTimeOpen())
                                            + " trước)",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, 2,
                                            "Hãy mau trở về bằng con đường rắn độc\nbọn Xayda đã đến Trái Đất",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            } else {
                                NpcService.gI().createTutorial(player, tempId, this.avartar,
                                        "Hãy vào bang hội trước");
                            }
                            break;
                    }
                } else if (player.iDMark.getIndexMenu() == 2) {
                    switch (select) {
                        case 0:// Top bang hội
                            Service.gI().showTopClanCDRD(player);
                            break;
                        case 1:
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Bạn không có bang hội!");
                                return;
                            }
                            Service.getInstance().showMyTopClanCDRD(player);
                            break;
                        case 2:
                            if (player.clan == null) {
                                NpcService.gI().createTutorial(player, tempId, this.avartar,
                                        "Hãy gia nhập bang hội!");
                                return;
                            }
                            if (player.clanMember.getNumDateFromJoinTimeToToday() < 3) {
                                NpcService.gI().createTutorial(player, tempId, this.avartar,
                                        "Gia nhập bang hội trên 3 ngày mới được tham gia");
                                return;
                            }
                            if (player.clan.ConDuongRanDoc == null) {
                                Input.gI().createFormChooseLevelCDRD(player);
                            } else {
                                SnakeWayService.gI().openConDuongRanDoc(player, (byte) 0);
                            }
                            break;
                    }
                } else if (player.iDMark.getIndexMenu() == 3) {
                    if (select == 0) {
                        if (player.clan.ConDuongRanDoc != null) {
                            SnakeWayService.gI().openConDuongRanDoc(player, (byte) 0);
                        } else {
                            SnakeWayService.gI().openConDuongRanDoc(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                        }
                    }
                }
            }
        }
    }

}
