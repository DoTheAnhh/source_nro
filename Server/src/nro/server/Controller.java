package nro.server;

import nro.service.intrinsic.IntrinsicService;
import nro.service.PlayerService;
import nro.service.NpcService;
import nro.service.item.ItemMapService;
import nro.service.ChatGlobalService;
import nro.service.SubMenuService;
import nro.service.MapService;
import nro.service.skill.SkillService;
import nro.service.Service;
import nro.service.TaskService;
import nro.service.item.ItemTimeService;
import nro.service.FlagBagService;
import nro.service.FriendAndEnemyService;
import nro.entity.card.Card;
import nro.service.card.RadarService;
import nro.core.consts.ConstIgnoreName;
import nro.core.consts.ConstMap;
import nro.core.util.Util;
import nro.data.DataGame;
import java.io.IOException;
import nro.service.fun.ChangeMapService;
import nro.service.fun.UseItem;
import nro.service.fun.Input;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstTask;
import nro.data.ItemData;
import nro.repository.dao.PlayerDAO;
import nro.repository.schema.GodGK;
import nro.service.clan.ClanService;
import nro.service.npc.NpcManager;
import nro.entity.player.Player;
import nro.service.matches.PVPService;
import nro.service.shop.ShopService;
import nro.service.fun.LuckyRound;
import nro.service.fun.TransactionService;
import nro.core.log.Logger;
import nro.service.consignmentstore.ConsignShopService;
import nro.entity.boss.Boss;
import nro.service.boss.BossManager;
import nro.service.DetuService;
import nro.core.consts.ConstAchievement;
import nro.core.consts.ConstDailyGift;
import nro.core.consts.ConstTranhNgocNamek;
import nro.repository.ConnectDB;
import nro.net.api.IMessageHandler;
import nro.net.api.ISession;
import nro.entity.skill.Skill;
import nro.repository.dao.SuperRankDAO;
import nro.net.io.Message;
import nro.net.session.MySession;
import nro.service.achievement.AchievementService;
import nro.entity.boss.map.trainingboss.TopKillWhisManager;
import nro.entity.boss.map.trainingboss.TrainningService;
import nro.service.map.blackballwar.BlackBallWarService;
import nro.service.map.ranksuper.SuperRankService;
import nro.service.combine.CombineService;
import nro.service.player.dailygift.DailyGiftService;
import nro.net.Network;
import nro.service.tambao.TamBaoService;
import nro.repository.CrisResultSet;

/**
 * Bảng phân phối gói tin — ranh giới giữa tầng mạng và toàn bộ nghiệp vụ game.
 *
 * <p>Mọi gói tin từ mọi người chơi đều đi qua {@link #onMessage}. Đây là điểm
 * hội tụ đông đúc nhất của cả server.</p>
 *
 * <p><b>Vấn đề cấu trúc.</b> {@code onMessage} là <b>một</b> hàm dài khoảng 690
 * dòng chứa một {@code switch} khoảng 80 nhánh, nhiều nhánh lại lồng
 * {@code switch} con. Hệ quả:</p>
 * <ul>
 *   <li>Thêm một lệnh mới = sửa đúng file này, nên hai người không thể làm hai
 *       tính năng cùng lúc mà không đụng nhau.</li>
 *   <li>Không test được từng lệnh riêng lẻ.</li>
 *   <li>Các nhánh dùng chung biến cục bộ ({@code idItem}, {@code moneyType},
 *       {@code type}...) — một nhánh đổi tên biến có thể làm hỏng nhánh khác.</li>
 *   <li>Mã lệnh viết dưới dạng <b>số trần</b>. Lớp {@code nro.net.logic.Cmd_message}
 *       có sẵn bảng tên nhưng không được dùng ở đâu cả.</li>
 * </ul>
 * <p>Hướng sửa: {@code CommandRouter} tra bảng {@code Map<Byte, CommandHandler>},
 * chuyển từng {@code case} ra một class riêng. Xem giai đoạn 6 trong
 * docs/03-KE-HOACH-TAI-CAU-TRUC.md — làm dần được, không cần xong một lần.</p>
 *
 * <h2>Bảng tra mã lệnh (nhánh cấp 1 của onMessage)</h2>
 * <pre>
 *  -100  Shop ký gửi          ConsignShopService (ký gửi, huỷ, mua, sang trang, đẩy lên đầu)
 *   127  Thẻ ra-đa            RadarService
 *  -105  Đi tàu vũ trụ        ChangeMapService.changeMapBySpaceShip
 *    42  Đăng ký tài khoản    Service.regisAccount
 *  -127  Nội tại              IntrinsicService.showMenu
 *  -125  Nội tại              IntrinsicService.showMenu
 *   112  Nội tại              IntrinsicService.showMenu
 *   -34  Danh sách kẻ thù     FriendAndEnemyService.controllerEnemy
 *   -99  Danh sách kẻ thù     FriendAndEnemyService.controllerEnemy
 *    18  Dịch chuyển Yardrat  FriendAndEnemyService.goToPlayerWithYardrat
 *   -72  Chat riêng           FriendAndEnemyService.chatPrivate
 *   -80  Danh sách bạn        FriendAndEnemyService.controllerFriend
 *   -59  Thông báo            Service.sendThongBao
 *   -86  Giao dịch            TransactionService.controller
 *  -107  Thông tin thú cưng   Service.showInfoPet
 *  -108  (chặn khi đang giao dịch)
 *     6  Mua item             ShopService
 *     7  Bán item             ShopService
 *    29  Pop-up nhiều dòng    Service.sendPopUpMultiLine
 *    21  Pop-up nhiều dòng    Service.sendPopUpMultiLine
 *   -71  (chặn khi đang giao dịch)
 *   -79  Menu người chơi      Service.getPlayerMenu
 *  -113  Lưu 10 phím tắt skill  player.playerSkill.skillShortCut
 *  -101  Mở giao diện cờ      Service.openFlagUI
 *  -103  Mở giao diện cờ      Service.openFlagUI
 *    -7  Nhân vật chết        Service.charDie
 *   -74  Client tải dữ liệu   (ghi log IP đang tải)
 *   -81  Thông tin ghép đồ    CombineService.showInfoCombine
 *   -87  Cập nhật dữ liệu     DataGame.updateData
 *   -67  Gửi icon theo id     DataGame.sendIcon
 *    66  Gửi ảnh theo tên     DataGame.sendImageByName
 *   -66  Hiệu ứng             (theo effId)
 *   -62  Chọn biểu tượng cờ   FlagBagService.sendIconFlagChoose
 *   -63  Hiệu ứng cờ          FlagBagService.sendIconEffectFlag
 *   -32  NPC                  NpcManager.getNpc
 *    22  NPC                  NpcManager.getNpc
 *   -33  Dịch chuyển waypoint ChangeMapService.changeMapWaypoint
 *   -23  Dịch chuyển waypoint ChangeMapService.changeMapWaypoint
 *   -45  (chặn khi đang giao dịch)
 *   -46  Thông tin clan       ClanService.getClan
 *   -51  Tin nhắn clan        ClanService.clanMessage
 *   -54  Đóng góp clan        ClanService.clanDonate
 *   -49  Vào clan             ClanService.joinClan
 *   -50  Danh sách thành viên ClanService.sendListMemberClan
 *   -56  Điều khiển clan      ClanService.clanRemote
 *   -47  Danh sách clan       ClanService.sendListClan
 *   -55  Rời clan             ClanService.showMenuLeaveClan
 *   -57  Mời vào clan         ClanService.clanInvite
 *   -40  (chặn khi đang giao dịch)
 *   -41  Gửi caption          Service.sendCaption
 *   -43  (chặn khi đang giao dịch)
 *   -91  Đổi bản đồ Ngọc Đen  BlackBallWarService.changeMap
 *   -39  Nạp bản đồ xong      ChangeMapService.finishLoadMap
 *    11  (chặn khi đang giao dịch)
 *    44  (chặn khi đang giao dịch)
 *    32  Chọn mục menu NPC    MenuController.doSelectMenu
 *    33  Chọn kỹ năng         SkillService.selectSkill
 *    34  Chọn kỹ năng         SkillService.selectSkill
 *    54  Đánh quái            Service.attackMob
 *   -60  Đánh người chơi      Service.attackPlayer
 *   -27  Nhặt item            ItemMapService.pickItem
 *  -111  Nhặt item            ItemMapService.pickItem
 *   -20  Nhặt item            ItemMapService.pickItem
 *   -28  Kiểm tra map Ma Bư   MapService.isMapMaBu12H
 *   -29  Kiểm tra map Ma Bư   MapService.isMapMaBu12H
 *   -30  Kiểm tra map Ma Bư   MapService.isMapMaBu12H
 *   -15  Về nhà               ChangeMapService.changeMapBySpaceShip
 *   -16  Hồi sinh             PlayerService.hoiSinh
 *  -104  Ma bảo vệ            Service.mabaove
 *  -118  Chọn mục theo iDMark.menuType
 *   -38  Client báo update xong  finishUpdate()
 *   126  androidPack2         (rỗng)
 *   -78  checkMMove           (đọc bỏ 1 int)
 *  -114  RequestPean          (rỗng)
 *    27  (rỗng, còn để lại từ tính năng cũ)
 *   -76  Xác nhận thành tựu   AchievementService.confirmAchievement
 * </pre>
 *
 * <p><b>Lưu ý:</b> bảng trên được dựng bằng cách đọc code ngày 22-08-2026. Sửa
 * {@code onMessage} thì nhớ cập nhật bảng này, nếu không nó sẽ lệch dần và trở
 * nên tai hại hơn là không có.</p>
 */
public class Controller implements IMessageHandler {

    /**
     * Đếm số lỗi <b>đã ghi log</b>, tối đa 5 cho <i>cả server</i>, <i>trọn đời</i>.
     *
     * <p><b>Đây là một cái bẫy.</b> Sau 5 lỗi đầu tiên, {@code onMessage} <b>nuốt
     * im lặng mọi ngoại lệ về sau</b> — không log, không đếm, không cảnh báo.
     * Server chạy vài ngày thì hạn mức này cạn từ lâu, và mọi lỗi xử lý gói tin
     * sau đó đều biến mất không dấu vết. Đây rất có thể là lý do những sự cố
     * "không hiểu sao" không bao giờ tìm được nguyên nhân trong log.</p>
     *
     * <p>Hơn nữa nó không phải {@code volatile} và được tăng từ nhiều thread
     * {@code QueueHandler} cùng lúc, nên chính con số đếm cũng không đáng tin.</p>
     *
     * <p>Nên đổi sang giới hạn tần suất theo thời gian (ví dụ tối đa N lỗi mỗi
     * phút cho mỗi mã lệnh) thay vì một hạn mức trọn đời.</p>
     */
    private int errors;

    private static Controller instance;

    /**
     * Singleton. Cùng lỗi thiếu đồng bộ như các {@code gI()} khác trong project,
     * nhưng vô hại vì lớp chỉ giữ đúng một biến đếm lỗi.
     */
    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    /**
     * Nhận và phân phối một gói tin. Xem bảng tra mã lệnh ở Javadoc của lớp.
     *
     * <p><b>Chạy trên thread {@code QueueHandler} riêng của từng session</b> — nghĩa
     * là hàm này chạy song song cho nhiều người chơi. Gói tin của <i>cùng một</i>
     * người thì tuần tự, nhưng mọi trạng thái dùng chung ({@code Manager.MAPS},
     * boss, clan, chợ) đều bị nhiều thread đụng vào cùng lúc và <b>phải tự lo
     * đồng bộ</b>.</p>
     *
     * <p><b>{@code player} có thể là {@code null}</b> — người chơi đã kết nối nhưng
     * chưa đăng nhập. Gần như mọi nhánh đều bắt đầu bằng {@code if (player != null)}.
     * Nhánh nào quên kiểm tra là NPE, mà NPE đó sẽ bị nuốt (xem {@code errors}).</p>
     *
     * <p><b>Thứ tự đọc là bắt buộc.</b> Mỗi lời gọi {@code _msg.reader().readX()}
     * đẩy con trỏ đọc tiến lên. Đọc thừa/thiếu một trường là mọi trường sau đó lệch,
     * và triệu chứng thường là một giá trị vô lý chứ không phải một lỗi rõ ràng.</p>
     *
     * <p><b>Xử lý lỗi.</b> Toàn bộ thân hàm nằm trong một {@code try} duy nhất, nên
     * bất kỳ nhánh nào ném ngoại lệ đều bị nuốt tại đây — người chơi chỉ thấy thao
     * tác của mình "không có gì xảy ra". Và sau 5 lỗi đầu tiên thì cả log cũng
     * không còn (xem {@code errors}).</p>
     *
     * <p><b>Khối {@code finally}</b> luôn giải phóng gói tin và cảnh báo nếu xử lý
     * quá 5 giây — mốc này hữu ích để phát hiện nhánh nào gọi DB đồng bộ.
     * Lưu ý nó đọc {@code _msg.command} <i>sau khi</i> đã {@code dispose()} gói tin;
     * hiện vẫn chạy đúng vì {@code command} là trường thường, không phải luồng.</p>
     *
     * @param s    session gửi gói tin; luôn ép được về {@code MySession}
     * @param _msg gói tin ở chế độ đọc; bị huỷ trong {@code finally}
     */
    @Override
    public void onMessage(ISession s, Message _msg) {
        long st = System.currentTimeMillis();
        MySession _session = (MySession) s;
        Player player = null;
        try {
            player = _session.player;
            byte cmd = _msg.command;
            switch (cmd) {
                // [-100] Shop ký gửi: ký gửi / huỷ / nhận tiền / mua / sang trang / đẩy lên đầu
                case -100:
                    if (player == null) {
                        return;
                    }
                    if (TransactionService.gI().check(player)) {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                        return;
                    }
                    if (player.baovetaikhoan) {
                        Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                        return;
                    }
                    byte action = _msg.reader().readByte();
                    switch (action) {
                        case 0:
                            // ký gửi
                            short idItem = _msg.reader().readShort();
                            byte moneyType = _msg.reader().readByte();
                            int money = _msg.reader().readInt();
                            int quantity;
                            if (player.getSession().version >= 222) {
                                quantity = _msg.reader().readInt();
                            } else {
                                quantity = _msg.reader().readByte();
                            }
                            if (quantity > 0) {
                                ConsignShopService.gI().KiGui(player, idItem, money, moneyType, quantity);
                            }
                            break;
                        case 1:
                        case 2: // hủy ký gửi
                            // nhận tiền
                            idItem = _msg.reader().readShort();
                            ConsignShopService.gI().claimOrDel(player, action, idItem);
                            break;
                        case 3:
                            // buy item
                            idItem = _msg.reader().readShort();
                            _msg.reader().readByte();
                            _msg.reader().readInt();
                            ConsignShopService.gI().buyItem(player, idItem);
                            break;
                        case 4:
                            // next page
                            moneyType = _msg.reader().readByte();
                            money = _msg.reader().readByte();
                            ConsignShopService.gI().openShopKyGui(player, moneyType, money);
                            break;
                        case 5:
                            // up top
                            idItem = _msg.reader().readShort();
                            ConsignShopService.gI().upItemToTop(player, idItem);
                            break;
                        default:
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            break;
                        // hủy ký gửi
                    }
                    break;

                // [127] Thẻ ra-đa: xem danh sách và bật/tắt thẻ đang dùng (tối đa 1 thẻ)
                case 127:
                    if (player != null) {
                        byte actionRadar = _msg.reader().readByte();
                        switch (actionRadar) {
                            case 0:
                                RadarService.gI().sendRadar(player, player.Cards);
                                break;
                            case 1:
                                short idC = _msg.reader().readShort();
                                Card card = player.Cards.stream().filter(r -> r != null && r.Id == idC).findFirst().orElse(null);
                                if (card != null) {
                                    if (card.Level == 0) {
                                        return;
                                    }
                                    if (card.Used == 0) {
                                        if (player.Cards.stream().anyMatch(c -> c != null && c.Used == 1)) {
                                            Service.gI().sendThongBao(player, "Số thẻ sử dụng đã đạt tối đa");
                                            return;
                                        }
                                        card.Used = 1;
                                    } else {
                                        card.Used = 0;
                                    }
                                    RadarService.gI().Radar1(player, idC, card.Used);
                                    Service.gI().point(player);
                                }
                                break;
                        }
                    }
                    break;
                // [-105] Đi tàu vũ trụ tới tương lai (map 102)
                case -105:
                    if (player != null) {
                        if (player.type == 0 && player.maxTime == 30) {
                            ChangeMapService.gI().changeMapBySpaceShip(player, 102, -1, Util.nextInt(60, 200));
                            player.iDMark.setGotoFuture(false);
                        } else if (player.type == 1 && player.maxTime == 5) {
                            if (player.iDMark != null && player.iDMark.isGoToBDKB()) {
                                ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 135, -1), 35, 35);
                                player.iDMark.setGoToBDKB(false);
                            }
                        } else if (player.type == 2 && player.maxTime == 5) {
                            if (MapService.gI().isMapHanhTinhThucVat(player.zone.map.mapId)) {
                                ChangeMapService.gI().changeMap(player, 80, -1, -1, 5);
                            } else {
                                ChangeMapService.gI().changeMap(player, 160, -1, -1, 5);
                            }
                        } else if (player.type == 3 && player.maxTime == 5) {
                            ChangeMapService.gI().changeMap(player, player.iDMark.getZoneKhiGasHuyDiet(), player.iDMark.getXMapKhiGasHuyDiet(), player.iDMark.getYMapKhiGasHuyDiet());
                            player.iDMark.setZoneKhiGasHuyDiet(null);
                        } else if (player.type == 4 && player.maxTime == 5) {
                            if (player.iDMark != null && player.iDMark.isGoToKGHD()) {
                                ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 149, -1), 100 + (Util.nextInt(-10, 10)), 336);
                                player.iDMark.setGoToKGHD(false);
                            }
                        } else if (player.type == 5 && player.maxTime == 5) {
                            ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 156, -1), 100 + (Util.nextInt(-10, 10)), 336);
                        }
                    }
                    break;
                // [42] Đăng ký tài khoản từ trong game
                case 42:
                    //Đăng ký tài khoản nhanh
                    Service.gI().regisAccount(_session, _msg);
                    break;
                // [-127] Nội tại: mở menu
                case -127:
                    if (player != null) {
                        LuckyRound.gI().readOpenBall(player, _msg);
                    }
                    break;
                // [-125] Nội tại: mở menu
                case -125:
                    if (player != null) {
                        Input.gI().doInput(player, _msg);
                    }
                    break;
                // [112] Nội tại.
                //
                // Gói KHÔNG có phần thân là client cũ, giữ nguyên menu NPC bốn
                // nút như trước. Có phần thân là bảng nội tại mới (NoiTaiUI):
                //   0 xin dữ liệu bảng
                //   1 mở bằng vàng
                //   2 mở bằng ngọc
                //   3 mở nhanh — kèm byte id nội tại muốn và int chỉ số muốn
                case 112:
                    if (player != null) {
                        int viecNT = -1;
                        try {
                            viecNT = _msg.reader().readByte();
                        } catch (Exception clientCu) {
                            viecNT = -1;
                        }
                        switch (viecNT) {
                            case 0:
                                IntrinsicService.gI().guiBangNoiTai(player);
                                break;
                            case 1:
                                IntrinsicService.gI().open(player);
                                break;
                            case 2:
                                IntrinsicService.gI().moBangNgoc(player);
                                break;
                            case 3: {
                                int idNT = _msg.reader().readByte();
                                int csNT = _msg.reader().readInt();
                                IntrinsicService.gI().moNhanh(player, idNT, csNT);
                                break;
                            }
                            default:
                                IntrinsicService.gI().showMenu(player);
                                break;
                        }
                    }
                    break;
                // [-34] Danh sách kẻ thù
                case -34:
                    if (player != null) {
                        switch (_msg.reader().readByte()) {
                            case 1:
                                player.magicTree.openMenuTree();
                                break;
                            case 2:
                                player.magicTree.loadMagicTree();
                                break;
                        }
                    }
                    break;
                // [-99] Danh sách kẻ thù
                case -99:
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerEnemy(player, _msg);
                    }
                    break;
                // [18] Dịch chuyển tới người chơi khác bằng năng lực Yardrat
                case 18:
                    if (player != null) {
                        player.changeMapVIP = true;
                        FriendAndEnemyService.gI().goToPlayerWithYardrat(player, _msg);
                    }
                    break;
                // [-72] Chat riêng
                case -72:
                    if (player != null) {
                        FriendAndEnemyService.gI().chatPrivate(player, _msg);
                    }
                    break;
                // [-80] Danh sách bạn bè
                case -80:
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerFriend(player, _msg);
                    }
                    break;
                // [-59] Gửi thông báo
                case -59:
                    if (player != null) {
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        PVPService.gI().controllerThachDau(player, _msg);
                    }
                    break;
                // [-86] Giao dịch giữa hai người chơi
                case -86:
                    if (player != null) {
                        TransactionService.gI().controller(player, _msg);
                    }
                    break;
                // [-107] Xem thông tin thú cưng
                case -107:
                    if (player != null) {
                        Service.gI().showInfoPet(player);
                    }
                    break;
                // [-108] Chặn thao tác khi đang giao dịch
                case -108:
                    if (player != null && player.Detu != null) {
                        player.Detu.changeStatus(_msg.reader().readByte());
                    }
                    break;

                // [6] Mua vật phẩm ở shop
                case 6: //buy item
                    if (player != null && !Maintenance.isRunning) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        byte typeBuy = _msg.reader().readByte();
                        int tempId = _msg.reader().readShort();
                        // Client CÓ gửi số lượng khi bấm "Mua 20 lần" hoặc
                        // "Nhập số lượng", nhưng đoạn đọc số lượng trước đây bị
                        // chú thích tắt — nên mua bao nhiêu cũng chỉ tính MỘT.
                        //
                        // Mua nhiều = LẶP LẠI đúng một lần mua, chứ không nhân
                        // giá rồi cộng dồn số lượng: mỗi vòng vẫn chạy đủ kiểm
                        // tra tiền, trần số ô và chỗ trống hành trang của chính
                        // nó, nên không thể tính sai giá hay vượt trần.
                        int soLuong = 1;
                        try {
                            soLuong = _msg.reader().readShort();
                        } catch (Exception ignored) {
                            // Client cũ không gửi số lượng -> mua một.
                        }
                        if (soLuong < 1) {
                            soLuong = 1;
                        }
                        if (soLuong > 99) {
                            soLuong = 99;
                        }
                        // Gộp gói tin trong suốt vòng lặp.
                        //
                        // Mỗi lần mua bản cũ gửi lại CẢ hành trang, cả số tiền,
                        // cộng một dòng thông báo — mua 99 món là gần bốn trăm
                        // gói bắn liên tiếp, mà hành trang là gói to nhất trong
                        // giao thức. Client ngập rồi rớt: đúng cảnh "mua nhiều
                        // thì văng game". Nay gửi đúng một lần ở cuối.
                        Service.gI().batGomGoi(player);
                        try {
                            for (int lan = 0; lan < soLuong; lan++) {
                                // Hết chỗ thì dừng, đừng để mỗi vòng lại bắn một
                                // thông báo "hành trang đã đầy".
                                if (lan > 0 && nro.service.inventory.InventoryService.gI()
                                        .getCountEmptyBag(player) == 0) {
                                    break;
                                }
                                ShopService.gI().takeItem(player, typeBuy, tempId);
                            }
                        } finally {
                            Service.gI().xaGomGoi(player);
                        }
                    }
                    break;
                // [7] Bán vật phẩm cho shop
                case 7: //sell item
                    if (player != null && !Maintenance.isRunning) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        action = _msg.reader().readByte();
                        if (action == 0) {
                            ShopService.gI().showConfirmSellItem(player, _msg.reader().readByte(),
                                    _msg.reader().readShort());
                        } else {
                            ShopService.gI().sellItem(player, _msg.reader().readByte(),
                                    _msg.reader().readShort());
                        }
                    }
                    break;
//                case 29:
//                    if (player != null) {
//                        ChangeMapService.gI().openZoneUI(player);
//                    }
//                    break;
//                case 21:
//                    if (player != null) {
//                        int zoneId = _msg.reader().readByte();
//                        ChangeMapService.gI().changeZone(player, zoneId);
//                    }
//                    break;
                case 29:
                    if (player != null) {
                        if (player.zone.map.mapId == ConstTranhNgocNamek.MAP_ID) {
                            Service.gI().sendPopUpMultiLine(player, 0, 7184, "Không thể thực hiện");
                            return;
                        }
                        ChangeMapService.gI().openZoneUI(player);
                    }
                    break;
                // [21] Pop-up nhiều dòng
                case 21:
                    if (player != null) {
                        if (player.zone.map.mapId == ConstTranhNgocNamek.MAP_ID) {
                            Service.gI().sendPopUpMultiLine(player, 0, 7184, "Không thể thực hiện");
                            return;
                        }
                        int zoneId = _msg.reader().readByte();
                        ChangeMapService.gI().changeZone(player, zoneId);
                    }
                    break;
                // [-71] Chặn thao tác khi đang giao dịch
                case -71:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        ChatGlobalService.gI().chat(player, _msg.reader().readUTF());
                    }
                    break;
                // [-79] Mở menu thao tác với người chơi khác
                case -79:
                    if (player != null) {
                        Service.gI().getPlayerMenu(player, _msg.reader().readInt());
                    }
                    break;
                // [-113] Lưu 10 phím tắt kỹ năng do người chơi sắp xếp
                case -113:
                    if (player != null) {
                        for (int i = 0; i < 10; i++) {
                            try {
                                player.playerSkill.skillShortCut[i] = _msg.reader().readByte();
                            } catch (IOException e) {
                                player.playerSkill.skillShortCut[i] = -1;
                            }
                        }
                        player.playerSkill.sendSkillShortCut();
                    }
                    break;
                // [-101] Nút "Chơi mới" ở màn chọn máy chủ — xin một tài khoản
                // ảo rồi vào game luôn. Lời gọi cũ ở đây bị chú thích và hàm
                // nhận nó cũng không còn, nên máy chủ im lặng còn client thì
                // treo ở hộp chờ — nó chỉ đóng hộp khi có trả lời.
                case -101:
                    if (player == null) {
                        String tenAo = _msg.reader().readUTF();
                        nro.service.TaiKhoanAoService.dangNhap(_session, tenAo);
                    }
                    break;
                // [-103] Mở giao diện cờ (flag)
                case -103:
                    if (player != null) {
                        byte act = _msg.reader().readByte();
                        switch (act) {
                            case 0:
                                Service.gI().openFlagUI(player);
                                break;
                            case 1:
                                Service.gI().chooseFlag(player, _msg.reader().readByte());
                                break;
                        }
                    }
                    break;
                // [-7] Nhân vật chết
                case -7:
                    if (player != null) {
                        if (player.isDie()) {
                            Service.gI().charDie(player);
                            return;
                        }
                        if (player.effectSkill.isHaveEffectSkill()) {
                            return;
                        }
                        int toX = player.location.x;
                        int toY = player.location.y;
                        try {
                            byte b = _msg.reader().readByte();
                            toX = _msg.reader().readShort();
                            try {
                                toY = _msg.reader().readShort();
                            } catch (IOException ex) {
                            }
                            if (player.zone != null && MapService.gI().isMapBlackBallWar(player.zone.map.mapId)
                                    && Util.getDistance(player.location.x, player.location.y, toX, toY) > 500) {
                                return;
                            }
                            if (b == 1) {
                                AchievementService.gI().checkDoneTaskFly(player, player.location.x - toX);
                            }
                        } catch (IOException e) {
                        }
                        PlayerService.gI().playerMove(player, toX, toY);
                    }
                    break;
                // [-74] Client xin tải dữ liệu nặng (ghi log IP đang tải)
                case -74:

                    String ip = _session.ipAddress;
                    Logger.warning("ip " + ip + " đang tải dữ liệu\n");

                    byte type = _msg.reader().readByte();
                    if (type == 1) {
                        DataGame.sendSizeRes(_session);
                    } else if (type == 2) {
                        DataGame.sendRes(_session);
                    }
                    break;
                // [-81] Xem thông tin ghép đồ
                case -81:
                    if (player != null) {
                        try {
                            _msg.reader().readByte();
                            int[] indexItem = new int[_msg.reader().readByte()];
                            for (int i = 0; i < indexItem.length; i++) {
                                indexItem[i] = _msg.reader().readByte();
                            }
                            CombineService.gI().showInfoCombine(player, indexItem);
                        } catch (IOException e) {
                        }
                    }
                    break;
                // [-87] Client xin cập nhật dữ liệu
                case -87:
                    DataGame.updateData(_session);
                    break;
                // [-67] Client xin một icon theo id
                case -67:
                    int id = _msg.reader().readInt();
                    DataGame.sendIcon(_session, id);
                    break;
                // [66] Client xin một ảnh theo tên
                case 66:
                    DataGame.sendImageByName(_session, _msg.reader().readUTF());
                    break;
                // [-66] Client xin dữ liệu hiệu ứng theo id
                case -66:
                    if (player != null) {
                        int effId = _msg.reader().readShort();
                        int idT = effId;
                        if (player.zone == null) {
                            break;
                        }
                        int shenronType = player.zone.shenronType;
                        if (idT == 25 && shenronType != -1 && player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14) {
                            idT = shenronType == 1 ? 59 : shenronType == 0 ? 51 : 60;
                        }
                        DataGame.sendEffectTemplate(_session, effId, idT);
                    }
                    break;
                // [-62] Chọn biểu tượng cờ
                case -62:
                    if (player != null) {
                        FlagBagService.gI().sendIconFlagChoose(player, _msg.reader().readByte());
                    }
                    break;
                // [-63] Hiệu ứng của cờ
                case -63:
                    if (player != null) {
                        byte fbid = _msg.reader().readByte();
                        int fbidz = fbid & 0xFF; //Chuyển sang byte không dấu
                        FlagBagService.gI().sendIconEffectFlag(player, fbidz);
                    }
                    break;
                // [-32] Tương tác NPC
                case -32:
                    int bgId = _msg.reader().readShort();
                    DataGame.sendItemBGTemplate(_session, bgId);
                    break;
                // [22] Tương tác NPC
                case 22:
                    if (player != null) {
                        _msg.reader().readByte();
                        NpcManager.getNpc(ConstNpc.DAU_THAN).confirmMenu(player, _msg.reader().readByte());
                    }
                    break;
                // [-33] Dịch chuyển qua waypoint (cổng dịch chuyển trên bản đồ)
                case -33:
                // [-23] Dịch chuyển qua waypoint
                case -23:
                    if (player != null) {
                        ChangeMapService.gI().changeMapWaypoint(player);
                        Service.gI().hideWaitDialog(player);
                    }
                    break;
                // [-45] Chặn thao tác khi đang giao dịch
                case -45:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        byte status = _msg.readByte();
                        SkillService.gI().useSkill(player, null, null, status, _msg);
                    }
                    break;
                // [-46] Clan: lấy thông tin clan
                case -46:
                    if (player != null) {
                        ClanService.gI().getClan(player, _msg);
                    }
                    break;
                // [-51] Clan: gửi tin nhắn trong clan
                case -51:
                    if (player != null) {
                        ClanService.gI().clanMessage(player, _msg);
                    }
                    break;
                // [-54] Clan: đóng góp
                case -54:
                    if (player != null) {
                        ClanService.gI().clanDonate(player, _msg);
                    }
                    break;
                // [-49] Clan: xin vào clan
                case -49:
                    if (player != null) {
                        ClanService.gI().joinClan(player, _msg);
                    }
                    break;
                // [-50] Clan: danh sách thành viên
                case -50:
                    if (player != null) {
                        ClanService.gI().sendListMemberClan(player, _msg.reader().readInt());
                    }
                    break;
                // [-56] Clan: thao tác quản lý (kick, thăng chức...)
                case -56:
                    if (player != null) {
                        ClanService.gI().clanRemote(player, _msg);
                    }
                    break;
                // [-47] Clan: danh sách clan để tìm kiếm
                case -47:
                    if (player != null) {
                        ClanService.gI().sendListClan(player, _msg.reader().readUTF());
                    }
                    break;
                // [-55] Clan: menu rời clan
                case -55:
                    if (player != null) {
                        ClanService.gI().showMenuLeaveClan(player);
                    }
                    break;
                // [-57] Clan: mời người chơi vào clan
                case -57:
                    if (player != null) {
                        ClanService.gI().clanInvite(player, _msg);
                    }
                    break;
                // [-40] Chặn thao tác khi đang giao dịch
                case -40:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        UseItem.gI().getItem(_session, _msg);
                    }
                    break;
                // [-41] Gửi dòng chữ nổi trên đầu nhân vật (caption)
                case -41:
                    Service.gI().sendCaption(_session, _msg.reader().readByte());
                    break;
                // [-43] Chặn thao tác khi đang giao dịch
                case -43:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        UseItem.gI().doItem(player, _msg);
                    }
                    break;
                // [-91] Chiến trường Ngọc Đen: đổi bản đồ
                case -91:
                    if (player != null) {
                        // Ghi lai MOI goi -91 kem loai dang cho.
                        //
                        // Switch nay khong co nhanh default: loai nao khong khai
                        // thi goi bi bo qua trong im lang — client da gui, may chu
                        // nhan, va khong co gi xay ra. Do la trang thai kho do
                        // nhat, nen phai co log.
                        int loaiDoi = player.iDMark.getTypeChangeMap();
                        nro.core.log.Logger.logln(nro.core.log.Logger.PURPLE,
                                "[CAPSULE] goi -91 tu " + player.name
                                + ", typeChangeMap=" + loaiDoi);
                        switch (loaiDoi) {
                            case ConstMap.CHANGE_CAPSULE: {
                                UseItem.gI().choseMapCapsule(player, _msg.reader().readByte());
                                break;
                            }
                            case ConstMap.CHANGE_BLACK_BALL: {
                                BlackBallWarService.gI().changeMap(player, _msg.reader().readByte());
                                break;
                            }
                            default:
                                nro.core.log.Logger.logln(
                                        nro.core.log.Logger.RED,
                                        "[CAPSULE] typeChangeMap=" + loaiDoi
                                        + " khong co nhanh xu ly, bo qua goi");
                                Service.gI().hideWaitDialog(player);
                                break;
                        }
                    }
                    break;
                // [-39] Client báo đã nạp xong bản đồ
                case -39:
                    if (player != null) {
                        ChangeMapService.gI().finishLoadMap(player);
                    }
                    break;
                // [11] Chặn thao tác khi đang giao dịch
                case 11:
                    byte modId = _msg.reader().readByte();
                    DataGame.requestMobTemplate(_session, modId);
                    break;
                // [-48] Xin du lieu man hinh Su kien.
                case -48:
                    nro.service.SuKienManHinhService.gI().guiDuLieu(player);
                    break;
                // [-58] Goi phuc loi: xin du lieu hoac xin nhan qua.
                case -58:
                    nro.service.PhucLoiService.gI().nhanGoi(player, _msg);
                    break;
                // [-110] Goi man hinh Boss: client xin, may chu tra danh sach.
                case 119:
                    nro.service.BossManHinhService.gI().nhanGoi(player, _msg);
                    break;
                case 120:
                    nro.service.BanDoManHinhService.gI().nhanGoi(player, _msg);
                    break;
                // [118] Goi Tai Xiu: mo bang, dat cuoc, dong bang.
                //
                // Chon 118 sau khi doi chieu CA BA bang lenh (Controller cua may
                // chu, Controller.cs va Controller2.cs cua client). Ma 121 nhin
                // thi trong o may chu nhung Controller2.cs phia client da dung.
                case 118:
                    nro.service.TaiXiuService.gI().nhanGoi(player, _msg);
                    break;
                // [117] Khu tro choi nho: Bau Cua, Xoc Dia, Dua Ngua, Dao Vang,
                // Cao Thap, Cau Ca. Mot ma goi cho ca sau tro, byte dau la ma
                // tro. Da soat 117 trong o CA BA noi: bang lenh may chu, bang
                // lenh client, va moi loi goi new Message(...) hai ben — chi
                // soat bang lenh la chua du, ma 122 nhin thi trong trong bang
                // nhung Service.java van gui di.
                case 117:
                    nro.service.MiniGameService.gI().nhanGoi(player, _msg);
                    break;
                // [-109] Goi thoai. Dat truoc case 44 cho de thay, hai cai khong lien quan.
                case -109:
                    nro.service.VoiceService.gI().nhanGoiTieng(player, _msg);
                    break;
                // [44] Chặn thao tác khi đang giao dịch
                // [59] Chat the gioi — mien phi, may chu tu gioi han nhip gui.
                case 59:
                    if (player != null) {
                        nro.service.chat.ChatTheGioiService.gI()
                                .nhanVaPhat(player, _msg.reader().readUTF());
                    }
                    break;
                // [60] Chat ban do — chi nguoi cung ban do doc duoc.
                case 60:
                    if (player != null) {
                        nro.service.chat.ChatKhuVucService.gI()
                                .nhanVaPhatMap(player, _msg.reader().readUTF());
                    }
                    break;
                // [67] Chat khu — hep hon nua, chi trong mot khu.
                case 67:
                    if (player != null) {
                        nro.service.chat.ChatKhuVucService.gI()
                                .nhanVaPhatKhu(player, _msg.reader().readUTF());
                    }
                    break;
                case 44:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        Command.gI().chat(player, _msg.reader().readUTF());
                    }
                    break;
                // [32] Chọn một mục trong menu NPC
                case 32:
                    if (player != null) {
                        int npcId = _msg.reader().readShort();
                        int select = _msg.reader().readByte();
                        MenuController.getInstance().doSelectMenu(player, npcId, select);
                    }
                    break;
                // [33] Chọn kỹ năng để dùng
                case 33:
                    if (player != null) {
                        int npcId = _msg.reader().readShort();
                        MenuController.getInstance().openMenuNPC(_session, npcId, player);
                    }
                    break;

                // [34] Chọn kỹ năng để dùng
                case 34:
                    if (player != null) {
                        try {
                            int selectSkill = _msg.reader().readShort();
                            SkillService.gI().selectSkill(player, selectSkill);
                        } catch (IOException e) {
//                            _session.disconnect();
//                            return
                        }
                    }
                    break;

                // [54] Đánh quái
                case 54:
                    long sys = System.currentTimeMillis();
                    if (player != null) {
                        int mobId = _msg.reader().readByte();
                        int masterId = -1;
                        boolean isMobMe = mobId == -1;
                        if (isMobMe) {
                            masterId = _msg.reader().readInt();
                        }
                        Service.gI().attackMob(player, mobId, isMobMe, masterId);
                    }
                    if (Manager.Jake_DEBUG) {
                        long total = System.currentTimeMillis() - sys;
                        System.out.println("TOtal real 54 : " + total);
                    }
                    break;
                // [-60] Đánh người chơi (PK)
                case -60:
                    if (player != null) {
                        int playerId = _msg.reader().readInt();
//                        _msg.reader().readByte();
                        Service.gI().attackPlayer(player, playerId);
                    }
                    break;
                // [-27] Nhặt vật phẩm dưới đất
                case -27:
                    _session.sendKey();
                    break;
                // [-111] Nhặt vật phẩm dưới đất
                case -111:
                    DataGame.sendDataImageVersion(_session);
                    break;
                // [-20] Nhặt vật phẩm dưới đất
                case -20:
                    if (player != null && !player.isDie()) {
                        int itemMapId = _msg.reader().readShort();
                        ItemMapService.gI().pickItem(player, itemMapId, false);
                    }
                    break;
                // [-28] Thao tác trong map Ma Bư 12H
                case -28:
                    messageNotMap(_session, _msg);
                    break;
                // [-29] Thao tác trong map Ma Bư 12H
                case -29:
                    messageNotLogin(_session, _msg);
                    break;
                // [-30] Thao tác trong map Ma Bư 12H
                case -30:
                    messageSubCommand(_session, _msg);
                    break;
                case -15: // về nhà
                    if (player != null) {
                        int mapId = MapService.gI().isMapMaBu12H(player.zone.map.mapId) ? 114 : player.gender + 21;
                        ChangeMapService.gI().changeMapBySpaceShip(player, mapId, 0, -1);
                    }
                    break;
                case -16: // hồi sinh
                    if (player != null && !player.isPKDHVT) {
                        PlayerService.gI().hoiSinh(player);
                    }
                    break;
                // [-104] Ma bảo vệ
                case -104:
                    if (player != null) {
                        Service.gI().mabaove(player, _msg.reader().readInt());
                    }
                    break;
                // [-118] Chọn mục, rẽ nhánh theo iDMark.getMenuType()
                case -118:
                    if (player != null) {
                        int _id = _msg.reader().readInt();
                        int menuType = player.iDMark.getMenuType();
                        switch (menuType) {
                            case 0:
                            case 1:
                            case 2: {
                                SuperRankService.gI().competing(player, _id);
                                break;
                            }
                            default: {
                                if (player.isFounder()) {
                                    Boss boss = BossManager.gI().getBoss(_id);
                                    if (boss != null) {
                                        ChangeMapService.gI().changeMapYardrat(player, boss.zone, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                                break;
                            }
                        }
                    }
                    break;
                // [88] Người chơi gõ xong ô nhập chữ do máy chủ mở
                //
                // Cùng một mã đi hai chiều: máy chủ gửi (câu hỏi, mã ô) bằng
                // Service.moHopNhapChu, client trả lại (mã ô, chuỗi đã gõ).
                case 88:
                    if (player != null) {
                        short maO = _msg.reader().readShort();
                        String chuDaGo = _msg.reader().readUTF();
                        CombineService.gI().nhanChuTuONhap(player, maO, chuDaGo);
                    }
                    break;
                // [-38] Client báo đã cập nhật xong dữ liệu
                case -38: //finish update
                    if (player != null) {
                        finishUpdate(player);
                    }
                    break;
                // [126] androidPack2 - nhánh rỗng, giữ để client cũ không lỗi
                case 126: //androidPack2
                    break;
                // [-78] checkMMove - đọc bỏ 1 int rồi thôi
                case -78: //checkMMove
                    _msg.reader().readInt(); // second
                    break;
                // [-114] RequestPean - nhánh rỗng
                case -114: //RequestPean
                    break;
                // [27] Nhánh rỗng, tàn dư của tính năng cũ (menu id)
                case 27:
//                    short menuid
                    break;
//                case -76:
//                    AchievementService.gI().confirmAchievement(player, _msg.reader().readByte());
//                    break;
                case -76:
                    AchievementService.gI().confirmAchievement(player, _msg.reader().readByte());
                    break;
//                default:
//                    Logger.log(Logger.YELLOW, "CMD: " + cmd + "\n");
//                    break;
            }
        } catch (Exception e) {
            if (errors < 5) {
                errors++;
                Logger.logException(Controller.class, e);
                if (player != null) {
                    Logger.warning("Player: " + player.name + "\n");
                }
                Logger.warning("Lỗi function: 'onMessage'\n");
                Logger.warning("Lỗi controller message command: " + _msg.command + "\n");
            }
        } finally {
            _msg.cleanup();
            _msg.dispose();
            long timeDo = System.currentTimeMillis() - st;
            if (timeDo > 5000) {
                Logger.warning(_msg.command + " - TimeOut: " + timeDo + " ms\n");
            }
        }
    }

    /**
     * Xử lý gói tin của người <b>chưa đăng nhập</b>.
     *
     * <p>Chỉ chấp nhận hai lệnh, và đây chính là điểm phòng thủ: mọi lệnh khác bị
     * bỏ qua, nên không ai gọi được chức năng game khi chưa xác thực.</p>
     * <ul>
     *   <li>{@code 0} — đăng nhập. Đọc hai chuỗi UTF: tên và mật khẩu.</li>
     *   <li>{@code 2} — client báo loại thiết bị / phiên bản.</li>
     * </ul>
     *
     * <p><b>Lỗi đọc thì ngắt kết nối ngay</b> ({@code session.disconnect()}) chứ
     * không bỏ qua. Hợp lý: gói tin sai khuôn ở giai đoạn này nghĩa là client giả
     * hoặc gói hỏng, không có gì để cứu vãn.</p>
     */
    public void messageNotLogin(MySession session, Message msg) {
        if (msg != null) {
            try {
                byte cmd = msg.reader().readByte();
                switch (cmd) {
                    case 0:
                        session.login(msg.reader().readUTF(), msg.reader().readUTF());
                        break;
                    case 1:
                        // Dang ky tu man dang ky cua client.
                        //
                        // Nhanh nay TRUOC DAY KHONG TON TAI. Client goi
                        // Service.requestRegister -> messageNotLogin(1), so 1 roi
                        // vao "default: break;" nen server im lang, khong tra gia
                        // gi. Client thi da bat hop cho "Dang ky" truoc khi gui,
                        // va hop do chi dong khi co goi tra ve — nen no XOAY MAI.
                        //
                        // Client con ghi them hai chuoi nua (tai khoan ao) sau ten
                        // va mat khau. Khong doc toi, va khong sao: doc xong goi
                        // tin bi huy ca cum, phan con lai khong troi sang goi sau.
                        GodGK.dangKy(session,
                                msg.reader().readUTF(), msg.reader().readUTF());
                        break;
                    case 2:
                        Service.gI().setClientType(session, msg);
                        break;
                    default:
                        // KHONG im lang o day.
                        //
                        // Ba nhanh tren deu la buoc TRUOC khi vao game, va o
                        // buoc do client luon bat mot hop cho ("Dang dang
                        // nhap", "Dang dang ky") ngay truoc khi gui. Hop do chi
                        // dong khi CO goi tra ve. Nen mot "default: break;" o
                        // day khong phai la "bo qua lenh la" — no la mot cai
                        // treo may vinh vien ben may nguoi choi, khong loi,
                        // khong dau vet. Do dung la loi vua gap voi so 1.
                        //
                        // Ghi log de con biet client dang goi so may, va tra ve
                        // mot cau bao de hop cho dong lai.
                        Logger.log(Logger.YELLOW,
                                "messageNotLogin: lenh la " + cmd + "\n");
                        Service.gI().sendThongBaoOK(session,
                                "Phiên bản client không khớp với máy chủ."
                                + " Vui lòng cập nhật lại.");
                        Service.gI().sendLoginFail(session, false);
                        break;
                }
            } catch (IOException e) {
                session.disconnect();
//                Logger.logException(Controller.class, e);
            } catch (Exception e) {
                // Bat ca Exception thuong: mot loi bat ngo o day (NullPointer
                // khi doc CSDL, JSON hong...) truoc kia lot ra ngoai va cung
                // cho ra dung cai hop cho xoay mai. Bao cho nguoi choi biet roi
                // ghi log de con lan ra nguyen nhan.
                Logger.logException(Controller.class, e, "messageNotLogin");
                Service.gI().sendThongBaoOK(session,
                        "Máy chủ gặp lỗi khi xử lý yêu cầu. Vui lòng thử lại.");
                Service.gI().sendLoginFail(session, false);
            }
        }
    }

    /**
     * Xử lý gói tin ở giai đoạn <b>đã đăng nhập nhưng chưa vào bản đồ</b>.
     *
     * <p>Đây là giai đoạn client tải dữ liệu nền. Trình tự bắt tay:</p>
     * <ol>
     *   <li>{@code 2}  — tạo nhân vật mới.</li>
     *   <li>{@code 6}  — xin dữ liệu bản đồ.</li>
     *   <li>{@code 7}  — xin dữ liệu kỹ năng.</li>
     *   <li>{@code 8}  — xin dữ liệu vật phẩm.</li>
     *   <li>{@code 10} — xin một map template cụ thể.</li>
     *   <li>{@code 13} — <b>client báo đã sẵn sàng</b>.</li>
     * </ol>
     *
     * <p><b>Vì sao {@code case 13} quan trọng.</b> Chỉ sau khi client xác nhận đã có
     * đủ dữ liệu nền, server mới được gửi các gói phụ thuộc nhân vật
     * ({@link #sendInfo}). Gửi sớm hơn thì client chưa có bảng tra để hiểu, và
     * biểu hiện là nhân vật vào game thiếu đồ/thiếu chỉ số.</p>
     */
    public void messageNotMap(MySession _session, Message _msg) {
        if (_msg != null) {
            Player player;
            try {
                player = _session.player;
                byte cmd = _msg.reader().readByte();
                switch (cmd) {
                    case 2:
                        createChar(_session, _msg);
                        break;
                    case 6:
                        Logger.log("[CLIENT_DATA] request map from " + _session.ipAddress + "\n");
                        DataGame.updateMap(_session);
                        break;
                    case 7:
                        Logger.log("[CLIENT_DATA] request skill from " + _session.ipAddress + "\n");
                        DataGame.updateSkill(_session);
                        break;
                    case 8:
                        Logger.log("[CLIENT_DATA] request item from " + _session.ipAddress + "\n");
                        ItemData.updateItem(_session);
                        break;
                    case 10:
                        DataGame.sendMapTemp(_session, _msg.reader().readUnsignedByte());
                        break;
                    case 3:
                    case 4:
                    case 5:
                        // Nut "Dong" o man tao nhan vat. Truoc day khong nhanh nao
                        // xu ly nen bam khong co gi xay ra — client cu dung do.
                        // NewGame() da co san nhung chua bao gio duoc goi.
                        NewGame(_session, _msg);
                        break;
                    case 13:
                        Logger.log("[CLIENT_DATA] clientOk from " + _session.ipAddress + "\n");
                        //client ok
                        if (player != null && player.isPl()) {
                            // Base data (map/skill/item) is ready on the client now.
                            // Only send player-dependent packets after this handshake.
                            Controller.getInstance().sendInfo(_session);
                            player.nPoint.initPowerLimit();
                            if (player.Detu != null) {
                                player.Detu.nPoint.initPowerLimit();
                            }

                            Service.gI().player(player);
                            Service.gI().Send_Caitrang(player);
                            // -64 my flag bag
                            Service.gI().sendFlagBag(player);
                            // -113 skill shortcut
                            player.playerSkill.sendSkillShortCut();
                            // item time
                            ItemTimeService.gI().sendAllItemTime(player);
                            // send current task
                            TaskService.gI().sendInfoCurrentTask(player);
                            //
                            Service.gI().sendTimeSkill(player);
                            TrainningService.gI().tnsmLuyenTapUp(player);
                            if (TaskService.gI().getIdTask(player) == ConstTask.TASK_0_0) {
                                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_0_0) {
                                    Service.gI().sendThongBao(player, "Nhiệm vụ của bạn là\nHãy di chuyển nhân vật");
                                    String npcSay = "Chào mừng " + player.name + " đến với thế giới Dragon Ball\n";
                                    npcSay += "Mình là " + (player.gender == 0 ? "Puaru" : player.gender == 1 ? "Piano" : "Icarus") + " sẽ đồng hành cũng bạn trên thế giới này\n";
                                    npcSay += "Để di chuyển, hãy chạm 1 lần vào nơi muốn đến";
                                    NpcService.gI().createTutorial(player, -1, npcSay);
                                }
                            } else {
                                // -70 thông báo bigmessage
                                sendThongBaoServer(player);
                            }
                            TopKillWhisManager.getInstance().load();
//                            if (player.inventory != null
//                                    && player.inventory.itemsBody.size() > 11
//                                    && player.inventory.itemsBody.get(11).isNotNullItem()) {
//                                Service.gI().sendChibi(player);
//                            }

                            if (player.inventory != null
                                    && player.inventory.itemsBody.size() > 11
                                    && player.inventory.itemsBody.get(11).isNotNullItem()) {
                                Service.getInstance().sendChanMenh(player,
                                        (short) player.inventory.itemsBody.get(11).template.id);
                            }

                            player.zone.mapInfo(player);
                            if (player.getSession().version >= 231) {
                                for (Skill skill : player.playerSkill.skills) {
                                    if (skill.currLevel <= 0 || skill.template.type != 4) {
                                        continue;
                                    }
                                    SkillService.gI().sendCurrLevelSpecial(player, skill);
                                }
                            }
                            if (player.getSession() != null && player.getSession().danap > 0) {
                                AchievementService.gI().checkDoneTask(player, ConstAchievement.LAN_DAU_NAP_NGOC);
                            }
                            if (DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_NGOC_MIEN_PHI)) {
                                Service.gI().sendThongBao(player, "Hôm nay bạn sẽ nhận được từ 1 đến 2 viên ngọc khi tiêu diệt 1 con quái");
                            }

                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                Logger.logException(Controller.class, e, "messageNotMap");
            }
        }
    }

    /**
     * Nhóm lệnh phụ — chủ yếu là cộng điểm tiềm năng.
     *
     * <ul>
     *   <li>{@code 16} — cộng điểm cho <b>nhân vật chính</b>.</li>
     *   <li>{@code 17} — cộng điểm cho <b>thú cưng</b> (rẽ tiếp theo
     *       {@code player.typeTabPet}; hiện chỉ nhánh 0 có xử lý).</li>
     *   <li>{@code 18} — cộng điểm cho <b>đệ tử</b> (tham số cuối {@code true}).</li>
     *   <li>{@code 64} — chọn mục trong menu phụ, {@code SubMenuService.controller}.</li>
     * </ul>
     *
     * <p><b>Rủi ro NPE:</b> {@code case 17} truy cập {@code player.typeTabPet} và
     * {@code player.Detu.nPoint} mà <b>không kiểm tra {@code player != null}</b> —
     * khác với {@code case 16} và {@code 18} vốn có kiểm tra. Gói tin giả gửi lệnh
     * 17 khi chưa vào game sẽ ném NPE (và bị nuốt ở khối {@code catch}).</p>
     *
     * <p>Trong {@code case 18} còn một khối bị chú thích: từng yêu cầu người chơi
     * duy trì 1.000.000 VND mới được cộng điểm cho đệ tử.</p>
     */
    public void messageSubCommand(MySession _session, Message _msg) {
        if (_msg != null) {
            Player player;
            try {
                player = _session.player;
                byte command = _msg.reader().readByte();
                switch (command) {
                    case 17:
                        byte typee = _msg.reader().readByte();
                        short pointt = _msg.reader().readShort();
                        switch (player.typeTabPet) {
                            case 0: {
                                if (player.Detu.nPoint != null) {
                                    player.Detu.nPoint.increasePoint(typee, pointt, false);
                                    Service.getInstance().InfoPetGoc(player);
                                }

                                break;
                            }
                            case 1: {

                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case 16:
                        byte type = _msg.reader().readByte();
                        short point = _msg.reader().readShort();
                        if (player != null && player.nPoint != null) {
                            // Nang nhieu diem mot luc thi hoi lai truoc.
                            // Bam tung diem van vao thang nhu cu.
                            player.nPoint.xinNangTiemNang(type, point, false);
                        }
                        break;
                    case 18:
                        byte type2 = _msg.reader().readByte();
                        short point2 = _msg.reader().readShort();
//                        if (player != null && player.getSession().vnd < 1000000) {
//                            Service.gI().sendThongBaoOK(player, "Cần duy trì VND ở mức 1.000.000 để sử dụng chức năng này!");
//                            return;
//                        }
                        if (player != null) {
                            if (player.Detu != null) {
                                player.Detu.nPoint.increasePoint(type2, point2, true);
                            }

                        }
                        break;
                    case 64:
                        int playerId = _msg.reader().readInt();
                        int menuId = _msg.reader().readShort();
                        SubMenuService.gI().controller(player, playerId, menuId);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                Logger.logException(Controller.class, e);
            }
        }
    }

    /**
     * Tạo nhân vật mới cho tài khoản.
     *
     * <p>Bị chặn hoàn toàn khi server đang bảo trì ({@code Maintenance.isRunning}) —
     * hợp lý, vì tạo nhân vật là ghi DB.</p>
     *
     * <p>Đọc kết quả truy vấn bằng {@code CrisResultSet} và giữ cờ {@code created}
     * để biết đã tạo thành công hay chưa.</p>
     */
    /**
     * Tạo nhân vật mới.
     *
     * <h3>Quy tắc đặt tên</h3>
     *
     * <p>Cho phép <b>chữ hoa, dấu tiếng Việt và khoảng trắng</b>. Trước đây tên
     * bị ép về chữ thường và chặn cả dấu lẫn khoảng trắng, nên không đặt được
     * tên kiểu <i>"Sôn Gôku"</i>.</p>
     *
     * <p>Vẫn chặn: khoảng trắng ở đầu/cuối, hai khoảng trắng liền nhau, và ký tự
     * không phải chữ/số/khoảng trắng — những thứ gây rối khi tra cứu và khi hiện
     * tên trong game.</p>
     *
     * <p>Trùng tên xét <b>không phân biệt hoa thường</b>: MySQL so chuỗi theo
     * collation mặc định là không phân biệt, nên "Goku" và "goku" là một.</p>
     */
    public void createChar(MySession session, Message msg) {
        if (Maintenance.isRunning) {
            return;
        }
        CrisResultSet rs = null;
        boolean created = false;
        try {
            String name = msg.reader().readUTF();
            int gender = msg.reader().readByte();
            int hair = msg.reader().readByte();
            name = name == null ? "" : name.trim();

            String loi = kiemTraTenNhanVat(name);
            if (loi != null) {
                Service.gI().sendThongBaoOK(session, loi);
                return;
            }
            rs = ConnectDB.executeQuery("select * from player where name = ?", name);
            if (rs.first()) {
                Service.gI().sendThongBaoOK(session, "Tên nhân vật đã tồn tại");
                return;
            }
            for (String n : ConstIgnoreName.IGNORE_NAME) {
                if (name.equalsIgnoreCase(n)) {
                    Service.gI().sendThongBaoOK(session, "Tên nhân vật không được phép dùng");
                    return;
                }
            }
            // Giu nguyen hoa thuong nguoi choi go, khong ep toLowerCase() nua.
            created = PlayerDAO.createNewPlayer(session.userId, name, (byte) gender, hair);
            if (!created) {
                Service.gI().sendThongBaoOK(session,
                        "Không tạo được nhân vật, vui lòng thử lại.");
            }
        } catch (Exception e) {
            Logger.logException(Controller.class, e);
            Service.gI().sendThongBaoOK(session, "Lỗi tạo nhân vật, vui lòng thử lại.");
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        if (created) {
            // Bo qua co che bat cho giua hai lan dang nhap — neu khong client
            // dung o man hinh cho va khong bao gio vao game.
            session.vuaTaoNhanVat = true;
            session.login(session.uu, session.pp);
        }
    }

    /**
     * Kiểm tra tên nhân vật.
     *
     * @return câu báo lỗi, hoặc {@code null} nếu hợp lệ
     */
    /**
     * Luật đặt tên nhân vật. Trả câu lỗi, hoặc {@code null} nếu tên hợp lệ.
     *
     * <p>Để {@code public} vì panel dùng chung luật này khi đổi tên — hai chỗ
     * kiểm khác nhau thì sẽ có tên tạo được đằng này mà đằng kia từ chối.</p>
     */
    public static String kiemTraTenNhanVat(String name) {
        if (name.length() < 4 || name.length() > 16) {
            return "Tên nhân vật phải từ 4 đến 16 ký tự.";
        }
        if (name.contains("  ")) {
            return "Tên nhân vật không được có hai khoảng trắng liền nhau.";
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != ' ') {
                return "Tên nhân vật chỉ được dùng chữ, số và khoảng trắng.";
            }
        }
        return null;
    }

    /**
     * Bắt đầu ván chơi mới.
     *
     * <p><b>Tên hàm sai quy ước Java</b> — method phải bắt đầu bằng chữ thường
     * ({@code newGame}). Đổi tên được vì phạm vi ảnh hưởng nhỏ.</p>
     */
    public void NewGame(MySession session, Message msg) {
        Service.gI().switchToRegisterScr(session);
//        Service.gI().sendThongBaoOK(session, "Muốn chơi thì ib đại ka MaiTienDung\n"
//                + "Zalo: 0974764064");
    }

    /**
     * Gửi trọn gói trạng thái nhân vật xuống client sau khi bắt tay dữ liệu xong.
     *
     * <p>Gọi từ {@code messageNotMap} nhánh {@code case 13} — tức là <b>sau</b> khi
     * client xác nhận đã có dữ liệu nền. Đây là bước cuối trước khi người chơi thật
     * sự vào game.</p>
     *
     * <p><b>Gửi những gì:</b> tileset, nội tại, chỉ số, nhiệm vụ chính, clan, thể
     * lực, năng động, thú cưng, bảng xếp hạng, danh hiệu, thông báo, trang phục,
     * chân mệnh, cờ tự chơi — rồi {@code player.start()}.</p>
     *
     * <p><b>Nợ kỹ thuật nghiêm trọng — 5 thread mới cho MỖI lần đăng nhập.</b>
     * Bốn khối {@code LastTimeDanhHieu_*} và một khối thú cưng đều dùng cùng một
     * mẫu: {@code new Thread(() -> { sleep(1000); gửi gói; }).start()}. Với 500
     * người đăng nhập là <b>2.500 thread</b> sinh ra chỉ để ngủ một giây.
     * Đúng ra phải dùng một {@code ScheduledExecutorService} dùng chung.
     * Bốn khối danh hiệu còn giống hệt nhau, gộp được thành một vòng lặp.</p>
     *
     * <p><b>Nuốt lỗi:</b> {@code catch (Exception e) {}} rỗng bọc toàn bộ hàm.
     * Bất kỳ bước nào hỏng thì các bước sau <b>không chạy</b> và không ai biết —
     * người chơi vào game thiếu dữ liệu mà log hoàn toàn im lặng.</p>
     */
    public void sendInfo(MySession session) {
        try {
            Player player = session.player;
            DataGame.sendTileSetInfo(session);
            TopKillWhisManager.getInstance().load();
            IntrinsicService.gI().sendInfoIntrinsic(player);
            Service.gI().point(player);
            TaskService.gI().sendTaskMain(player);
            Service.gI().clearMap(player);
            ClanService.gI().sendMyClan(player);
            PlayerService.gI().sendMaxStamina(player);
            PlayerService.gI().sendCurrentStamina(player);
            Service.gI().sendNangDong(player);
            Service.gI().sendHavePet(player);
            Service.gI().sendTopRank(player);
            // KHONG phat thu hang Sieu Hang o day nua.
            //
            // Dong cu: rank < 1 thi cap ngay getHighestRank() + 1. Tuc la chi
            // can TAO TAI KHOAN roi dang nhap mot lan la co mat trong bang xep
            // hang, va thu tu trong bang chinh la thu tu dang ky tai khoan —
            // ai lap nick truoc thi dung tren. Khong lien quan gi den thi dau.
            //
            // Gio thu hang duoc cap dung luc nguoi choi vao tran dau tien
            // (SuperRankService.competing). Ai chua danh tran nao thi rank van
            // bang 0 va khong xuat hien trong bang.
            if (player.LastTimeDanhHieu_ThienTu > 0) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Service.getInstance().sendDanhHieu(player, 0);
                    } catch (InterruptedException e) {
                    }
                }).start();
            }
            if (player.LastTimeDanhHieu_2 > 0) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Service.getInstance().sendDanhHieu(player, 1);
                    } catch (InterruptedException e) {
                    }
                }).start();
            }
            if (player.LastTimeDanhHieu_3 > 0) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Service.getInstance().sendDanhHieu(player, 2);
                    } catch (InterruptedException e) {
                    }
                }).start();
            }
            if (player.LastTimeDanhHieu_4 > 0) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Service.getInstance().sendDanhHieu(player, 3);
                    } catch (InterruptedException e) {
                    }
                }).start();
            }
            ServerNotify.gI().sendNotifyTab(player);
            player.setClothes.setup();
            if (player.Detu != null) {
                player.Detu.setClothes.setup();
            }

            if (player.inventory.itemsBody.get(7).isNotNullItem()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        DetuService.PetFollow(player, player.getHeadThuCung(), player.getBodyThuCung(), player.getLegThuCung());
                        Service.gI().point(player);
                    } catch (InterruptedException e) {
                    }
                }, "Pet update").start();
            }
//            if (player.inventory != null
//                    && player.inventory.itemsBody.size() > 11
//                    && player.inventory.itemsBody.get(11).isNotNullItem()) {
//                Service.gI().sendChibi(player);
//            }

            if (player.inventory != null
                    && player.inventory.itemsBody.size() > 11
                    && player.inventory.itemsBody.get(11).isNotNullItem()) {
                Service.getInstance().sendChanMenh(player,
                        (short) player.inventory.itemsBody.get(11).template.id);
            }

            ItemTimeService.gI().sendCanAutoPlay(player);
            player.start();
        } catch (Exception e) {
        }
    }

    /**
     * Đánh dấu client đã cập nhật xong dữ liệu (lệnh {@code -38}).
     *
     * <p>Chỉ đặt cờ {@code session.finishUpdate}; các phần khác đọc cờ này để biết
     * có được gửi dữ liệu nặng hay chưa.</p>
     */
    public void finishUpdate(Player player) {
        if (player.getSession() != null) {
            player.getSession().finishUpdate = true;
        }
    }

    /**
     * Gửi thông báo cảnh báo hack/bug tới một người chơi.
     *
     * <p><b>Hiện không nơi nào gọi</b> — code chết.</p>
     *
     * <p>Nội dung thông báo có <b>lời lẽ thô tục</b> được nhúng cứng trong mã nguồn.
     * Nếu định bật lại chức năng này thì nên sửa lại câu chữ, và đưa nội dung ra
     * file cấu hình thay vì nhúng vào code.</p>
     */
    private void sendThongBaoServer(Player player) {
        // Loi chao doc tu panel (khoa loi_chao_vao_game, tab Quy uoc). Truoc
        // day go cung ngay day nen doi mot chu la phai sua ma roi bien dich lai.
        // De trong o do thi khong hien gi ca.
        String loiChao = nro.repository.dao.ConfigDAO.chuoi(
                nro.repository.dao.ConfigDAO.LOI_CHAO);
        if (loiChao != null && !loiChao.trim().isEmpty()) {
            Service.gI().sendThongBaoFromAdmin(player, loiChao.trim() + "\n");
        }
    }
}
