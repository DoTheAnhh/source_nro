package nro.server;

import nro.core.util.Functions;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.dao.PlayerDAO;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import nro.entity.item.Item;
import nro.service.item.ItemTimeService;
import nro.net.api.ISession;
import nro.net.io.Message;
import nro.net.session.MySession;
import nro.net.session.SessionManager;
import nro.gameplay.dragon.SummonDragon;
import nro.gameplay.dragon.SummonDragonNamek;
import nro.service.inventory.InventoryService;
import nro.entity.map.dragonballnamec.NgocRongNamec;
import nro.entity.map.dragonnamecwar.TranhNgoc;
import nro.entity.map.ItemMap;
import nro.entity.player.Player;
import nro.service.fun.ChangeMapService;
import nro.service.fun.TransactionService;
import nro.service.Service;

/**
 * Sổ đăng ký người chơi <b>đang online</b>, và là nơi dọn dẹp khi họ rời game.
 *
 * <p>Khác {@code nro.net.session.SessionManager} (theo dõi <b>kết nối</b>, kể cả
 * kết nối chưa đăng nhập), lớp này chỉ chứa người đã vào game thật sự.</p>
 *
 * <p><b>Bốn cấu trúc song song, phải luôn khớp nhau:</b></p>
 * <ul>
 *   <li>{@code players_id}     — tra theo id nhân vật</li>
 *   <li>{@code players_userId} — tra theo id tài khoản</li>
 *   <li>{@code players_name}   — tra theo tên nhân vật</li>
 *   <li>{@code players}        — danh sách để duyệt</li>
 * </ul>
 * <p>Ba map là để tra O(1) theo ba kiểu khoá khác nhau. Cái giá là <b>mọi thao
 * tác thêm/xoá đều phải đụng cả bốn</b> — lệch một cái là sinh ra người chơi
 * "ma": tra theo tên thì thấy, tra theo id thì không.</p>
 *
 * <p><b>Đồng bộ:</b> mọi truy cập đi qua một khoá duy nhất {@code lock}. Đơn giản
 * và đúng, nhưng là điểm nghẽn: mọi lần đăng nhập, đăng xuất và tra cứu đều xếp
 * hàng qua đúng một khoá.</p>
 *
 * <p><b>Cũng là một Runnable:</b> constructor tự bật một thread nền chạy
 * {@link #update()} mỗi giây để đá những session kết nối vào mà không đăng nhập.</p>
 */
public class Client implements Runnable {

    private static Client instance;

    /** Tra người chơi theo id nhân vật. Bảo vệ bởi {@link #lock}. */
    private final Map<Long, Player> players_id = new HashMap<>();
    /** Tra người chơi theo id tài khoản. Đây là map dùng để phát hiện đăng nhập trùng. */
    private final Map<Integer, Player> players_userId = new HashMap<>();
    /**
     * Tra người chơi theo tên nhân vật.
     *
     * <p><b>Phân biệt chữ hoa/thường</b> — {@code Goku} và {@code goku} là hai khoá
     * khác nhau. Nếu DB cho phép trùng tên khác kiểu chữ thì đây là lỗ hổng.</p>
     */
    private final Map<String, Player> players_name = new HashMap<>();

    @Getter
    /**
     * Danh sách để duyệt (chat toàn server, đếm online...).
     *
     * <p>{@code @Getter} của lombok trả về <b>danh sách gốc</b>, không phải bản sao.
     * Duyệt nó ngoài khối {@code synchronized} có thể ném
     * {@code ConcurrentModificationException} khi có người vào/ra giữa chừng.
     * Dùng {@link #getPlayersSnapshot()} thay thế.</p>
     */
    private final List<Player> players = new ArrayList<>();

    /** Khoá duy nhất bảo vệ cả bốn cấu trúc trên. Giữ khoá càng ngắn càng tốt. */
    private final Object lock = new Object();

    /**
     * Riêng tư để ép dùng {@link #gI()}.
     *
     * <p><b>Có tác dụng phụ:</b> constructor tự bật luôn một thread nền. Nghĩa là
     * chỉ cần chạm tới {@code Client.gI()} lần đầu là một thread mới sinh ra —
     * điều không ai đoán được khi đọc chỗ gọi.</p>
     */
    private Client() {
        new Thread(this, "Update Client").start();
    }

    /**
     * Singleton.
     *
     * <p><b>Không an toàn luồng.</b> {@code if (instance == null)} không có khoá,
     * mà constructor lại bật thread. Hai thread gọi cùng lúc có thể tạo <b>hai</b>
     * đối tượng {@code Client} với hai danh sách người chơi riêng và hai thread
     * nền — từ đó nửa số người chơi biến mất khỏi mọi phép đếm.
     * Sửa: {@code private static final Client instance = new Client();}</p>
     */
    public static Client gI() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    /**
     * Không add trực tiếp nữa.
     * Tất cả đi qua safePut để chống login 2 tab / 1 account.
     */
    public void put(Player player) {
        safePut(player);
    }

    /**
     * Last login wins:
     * - Tab 1 đang online.
     * - Tab 2 đăng nhập cùng account.
     * - Tab 2 được vào.
     * - Tab 1 nhận thông báo rồi bị kick.
     *
     * <p><b>Vì sao phải dò cả ba map, không chỉ userId.</b> Một người chơi có thể
     * đang nằm trong ba map dưới ba khoá khác nhau, và dữ liệu có thể đã lệch
     * (ví dụ nhân vật đổi tên, hoặc một lần dọn dẹp trước đó hỏng giữa chừng).
     * Chỉ dò theo {@code userId} sẽ bỏ sót bản cũ còn kẹt trong
     * {@code players_name} — và người chơi đó thành "ma", không ai kick được.
     * Vì vậy hàm dò lần lượt cả ba và gỡ mọi bản cũ tìm thấy.</p>
     *
     * <p><b>Chỉ kick MỘT phiên.</b> Biến {@code oldSessionToKick} chỉ được gán khi
     * còn {@code null} — ba lần dò có thể cùng trỏ về một phiên cũ, kick hai lần
     * là chạy lại toàn bộ trình tự dọn dẹp và có nguy cơ nhân đôi vật phẩm.</p>
     *
     * <p><b>Việc kick làm NGOÀI khối {@code synchronized}</b>, vì
     * {@link #kickSession} có ghi DB — giữ khoá trong lúc đó sẽ treo mọi lần đăng
     * nhập khác.</p>
     *
     * @return {@code true} nếu ghi được vào danh sách online. Hiện tại luôn
     *         {@code true}; chỗ gọi trong {@code MySession.login} vẫn kiểm tra
     *         {@code false} để phòng khi sau này thêm điều kiện từ chối.
     */
    public boolean safePut(Player player) {
        if (player == null || player.getSession() == null) {
            return false;
        }

        MySession newSession = player.getSession();
        MySession oldSessionToKick = null;

        synchronized (lock) {
            int userId = newSession.userId;

            Player oldByUser = players_userId.get(userId);
            if (oldByUser != null && oldByUser != player) {
                MySession oldSession = oldByUser.getSession();

                if (oldSession != null && oldSession != newSession) {
                    oldSessionToKick = oldSession;
                }

                removePlayerFromCollections(oldByUser);
            }

            Player oldById = players_id.get(player.id);
            if (oldById != null && oldById != player) {
                MySession oldSession = oldById.getSession();

                if (oldSessionToKick == null && oldSession != null && oldSession != newSession) {
                    oldSessionToKick = oldSession;
                }

                removePlayerFromCollections(oldById);
            }

            Player oldByName = players_name.get(player.name);
            if (oldByName != null && oldByName != player) {
                MySession oldSession = oldByName.getSession();

                if (oldSessionToKick == null && oldSession != null && oldSession != newSession) {
                    oldSessionToKick = oldSession;
                }

                removePlayerFromCollections(oldByName);
            }

            players_id.put(player.id, player);
            players_userId.put(userId, player);
            players_name.put(player.name, player);

            if (!players.contains(player)) {
                players.add(player);
            }
        }

        if (oldSessionToKick != null && oldSessionToKick != newSession) {
            System.out.println("[LOGIN_DUP] userId=" + newSession.userId
                    + " | kickOldSession=" + oldSessionToKick
                    + " | keepNewSession=" + newSession);

            sendLoginOtherDeviceMessage(oldSessionToKick);

            // Chờ để gói thông báo kịp ra khỏi socket trước khi đóng kết nối.
            // sendMessage() chỉ XẾP gói vào hàng đợi; Sender.close() thì VỨT BỎ
            // mọi gói còn chờ. Không chờ ở đây thì người bị kick không bao giờ
            // biết vì sao mình bị văng.
            //
            // NHƯỢC ĐIỂM: đây là sleep NGAY TRONG luồng đăng nhập -> mỗi lần login
            // trùng làm người mới phải đợi thêm 1 giây. Cách đúng là đợi hàng đợi
            // gửi rỗng (hoặc hết thời gian chờ), thay vì ngủ một khoảng cố định.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                kickSession(oldSessionToKick);
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }

        return true;
    }

    /**
     * Báo cho phiên cũ biết vì sao sắp bị đá, bằng lệnh {@code -70}.
     *
     * <p>Chỉ <i>xếp</i> gói tin vào hàng đợi gửi — nó chưa ra khỏi socket khi hàm
     * này trả về. Đó là lý do {@link #safePut} phải chờ trước khi kick; xem ghi
     * chú ở {@code Thread.sleep(1000)} trong hàm đó.</p>
     */
    private void sendLoginOtherDeviceMessage(MySession session) {
        if (session == null) {
            return;
        }

        Message msg = null;
        try {
            msg = new Message(-70);
            msg.writer().writeUTF("Tài khoản của bạn đã được đăng nhập ở nơi khác.");
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Client.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Gỡ người chơi khỏi bộ nhớ và ghi mốc đăng xuất vào DB.
     *
     * <p><b>Chú ý cách chia hai phần:</b> trong khối {@code synchronized} chỉ gỡ
     * khỏi các map (rất nhanh); việc nặng — lưu DB, dọn trạng thái game — làm
     * <i>ngoài</i> khoá. Cố ý như vậy, vì {@link #disposePlayerData} có truy vấn DB
     * và giữ khoá trong lúc đó sẽ treo mọi lần đăng nhập khác.</p>
     *
     * <p>{@code session.player = null} được đặt <b>bên trong</b> khoá để không
     * thread nào còn thấy nhân vật đang bị dọn dở.</p>
     *
     * <p>Cuối cùng gọi {@code ServerManager.disconnect(session)} để giảm bộ đếm số
     * kết nối trên mỗi IP — thiếu bước này thì sau một thời gian IP đó bị chính
     * server chặn vì tưởng đã đạt giới hạn.</p>
     */
    private void remove(MySession session) {
        if (session == null) {
            return;
        }

        Player playerToDispose = null;

        synchronized (lock) {
            if (session.player != null) {
                playerToDispose = session.player;
                session.player = null;
                removePlayerFromCollections(playerToDispose);
            }

            session.connected = false;
        }

        if (playerToDispose != null) {
            disposePlayerData(playerToDispose);

            try {
                playerToDispose.dispose();
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }

        if (session.joinedGame) {
            session.joinedGame = false;

            try {
                ConnectDB.executeUpdate(
                        "update account set last_time_logout = ? where id = ?",
                        new Timestamp(System.currentTimeMillis()),
                        session.userId
                );
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }

        ServerManager.gI().disconnect(session);
    }

    /**
     * Gỡ người chơi khỏi cả bốn cấu trúc.
     *
     * <p><b>Phải gọi khi đang giữ {@link #lock}.</b></p>
     *
     * <p>Hai phép kiểm tra {@code byName == player} và {@code byUser == player} là
     * <b>bắt buộc, không phải thừa</b>: khi phiên mới đã ghi đè phiên cũ trong map,
     * việc dọn phiên cũ không được phép xoá mất mục của phiên mới. So sánh bằng
     * {@code ==} (cùng đối tượng) chứ không phải {@code equals}.</p>
     */
    private void removePlayerFromCollections(Player player) {
        // Hoan cuoc Tai Xiu con treo. Khong hoan thi thoi vang da tru bien mat
        // han: may chu khong co duong nao tra vao hanh trang cua nguoi offline.
        try {
            nro.gameplay.taixiu.TaiXiuManager.gI().nguoiChoiRoiGame(player);
            nro.gameplay.minigame.NhipMiniGame.gI().nguoiChoiRoiGame(player);
            nro.gameplay.minigame.DaoVangManager.gI().nguoiChoiRoiGame(player);
            nro.gameplay.minigame.CaoThapManager.gI().nguoiChoiRoiGame(player);
            nro.gameplay.minigame.CauCaManager.gI().nguoiChoiRoiGame(player);
        } catch (Exception e) {
            nro.core.log.Logger.logException(Client.class, e);
        }
        if (player == null) {
            return;
        }

        players_id.remove(player.id);
        // Quen nhom dang mo trong menu "Ban do", khong de bang phinh mai.
        nro.service.MapNhanhService.gI().quen(player);

        Player byName = players_name.get(player.name);
        if (byName == player) {
            players_name.remove(player.name);
        }

        MySession s = player.getSession();
        if (s != null) {
            Player byUser = players_userId.get(s.userId);
            if (byUser == player) {
                players_userId.remove(s.userId);
            }
        }

        players.remove(player);
    }

    /**
     * Dọn toàn bộ dấu vết của người chơi trong thế giới game trước khi lưu.
     *
     * <p>Đây là hàm dài nhất lớp này, và mỗi khối {@code try} là <b>một hệ thống
     * khác nhau</b> cần được báo rằng người chơi đã rời đi:</p>
     * <ol>
     *   <li>Tranh ngọc (Cadic / Fide) — rút khỏi danh sách tham gia.</li>
     *   <li>Ghi lại bản đồ đang đứng để lần sau đăng nhập vào đúng chỗ.</li>
     *   <li><b>Ngọc rồng Namek</b> — đang giữ ngọc thì phải <i>rơi ngọc xuống đất</i>
     *       và trả slot về hệ thống. Bỏ bước này là ngọc biến mất vĩnh viễn khỏi server.</li>
     *   <li>Rời khu vực bản đồ (để người khác không còn thấy).</li>
     *   <li>Huỷ giao dịch đang mở — nếu không, đồ có thể bị nhân đôi.</li>
     *   <li>Gỡ khỏi danh sách thành viên clan đang online.</li>
     *   <li>Tắt Túi Đồ Lưu Trữ (item 521) nếu đang bật.</li>
     *   <li>Báo cho hệ thống triệu hồi rồng biết người triệu hồi đã rớt.</li>
     *   <li>Cho trứng/đệ tử chết và cho đệ tử rời bản đồ.</li>
     * </ol>
     *
     * <p><b>Cờ {@code beforeDispose} chống chạy hai lần.</b> Hàm có thể bị gọi từ
     * hai phía cùng lúc (client rớt mạng và admin kick), mà chạy lại lần hai sẽ
     * rơi ngọc <i>hai lần</i> — tức là nhân bản vật phẩm.</p>
     *
     * <p><b>Vì sao mỗi bước một khối {@code try} riêng:</b> để một hệ thống lỗi
     * không chặn các hệ thống còn lại. Đây là một trong số ít chỗ trong project mà
     * {@code try} lồng dày đặc là <i>đúng</i>.</p>
     *
     * <p>Khối {@code finally} luôn gọi {@code PlayerDAO.updatePlayer} — dữ liệu
     * người chơi được lưu <b>kể cả khi mọi bước dọn ở trên đều hỏng</b>.</p>
     */
    private void disposePlayerData(Player player) {
        if (player == null) {
            return;
        }

        try {
            if (!player.beforeDispose) {
                player.beforeDispose = true;

                try {
                    TranhNgoc.gI().removePlayersCadic(player);
                    TranhNgoc.gI().removePlayersFide(player);
                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }

                if (player.zone != null && player.zone.map != null) {
                    player.mapIdBeforeLogout = player.zone.map.mapId;
                }

                if (player.idNRNM != -1 && player.zone != null) {
                    try {
                        ItemMap itemMap = new ItemMap(
                                player.zone,
                                player.idNRNM,
                                1,
                                player.location.x,
                                player.location.y,
                                -1
                        );

                        Service.gI().dropItemMap(player.zone, itemMap);
                        NgocRongNamec.gI().pNrNamec[player.idNRNM - 353] = "";
                        NgocRongNamec.gI().idpNrNamec[player.idNRNM - 353] = -1;
                        player.idNRNM = -1;
                    } catch (Exception e) {
                        Logger.logException(Client.class, e);
                    }
                }

                try {
                    ChangeMapService.gI().exitMap(player);
                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }

                try {
                    TransactionService.gI().cancelTrade(player);
                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }

                try {
                    if (player.clan != null) {
                        player.clan.removeMemberOnline(null, player);
                    }
                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }

                try {
                    if (player.itemTime != null && player.itemTime.isUseTDLT) {
                        Item tdlt = null;

                        try {
                            tdlt = InventoryService.gI().findItemBag(player, 521);
                        } catch (Exception e) {
                            Logger.logException(Client.class, e);
                        }

                        if (tdlt != null) {
                            ItemTimeService.gI().turnOffTDLT(player, tdlt);
                        }
                    }
                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }

                try {
                    // Thoat game la HUY luon luot goi rong.
                    //
                    // Truoc day may chu giu con rong lai va cho nguoi ay quay
                    // vao. Nhung nguoi thoat game co the khong vao lai trong
                    // nam phut ay, ma suot thoi gian do ca khu van toi om voi
                    // mot con rong dung im, con nguoi khac thi khong goi rong
                    // duoc.
                    if (SummonDragon.gI().playerSummonShenron != null
                            && SummonDragon.gI().playerSummonShenron.id == player.id) {
                        SummonDragon.gI().huyKhiThoatGame(player);
                    }

                    if (SummonDragonNamek.gI().playerSummonShenron != null
                            && SummonDragonNamek.gI().playerSummonShenron.id == player.id) {
                        SummonDragonNamek.gI().isPlayerDisconnect = true;
                    }

                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }

                try {
                    if (player.DeTrung != null) {
                        player.DeTrung.mobMeDie();
                    }

                    if (player.Detu != null) {
                        if (player.Detu.DeTrung != null) {
                            player.Detu.DeTrung.mobMeDie();
                        }

                        try {
                            ChangeMapService.gI().exitMap(player.Detu);
                        } catch (Exception e) {
                            Logger.logException(Client.class, e);
                        }
                    }
                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }
            }
        } catch (Exception e) {
            Logger.logException(Client.class, e);
        } finally {
            try {
                // batBuoc = true: day la lan luu CUOI CUNG.
                //
                // Khong duoc bo qua vi luot tu luu dinh ky dang chay — bo qua
                // la mat het nhung gi lam sau luc luot kia chup xong, va do
                // dung la canh "cho do vao ruong roi dang xuat thi mat do".
                PlayerDAO.updatePlayer(player, true);
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }
    }

    /**
     * Đá một phiên: gỡ dữ liệu rồi đóng kết nối.
     *
     * <p>Đường ra <b>duy nhất</b> của mọi trường hợp rời game — client rớt mạng,
     * admin kick, đăng nhập trùng, hết giờ chờ, server đóng. Nhờ vậy mọi lối ra đều
     * chạy qua cùng một trình tự lưu dữ liệu.</p>
     *
     * <p>Thứ tự quan trọng: {@code remove()} <b>trước</b>, {@code disconnect()} sau.
     * Đảo lại thì {@code remove} sẽ làm việc trên một session đã bị
     * {@code dispose()} gán {@code null} các trường.</p>
     */
    public void kickSession(MySession session) {
        if (session == null) {
            return;
        }

        try {
            remove(session);
        } catch (Exception e) {
            Logger.logException(Client.class, e);
        }

        try {
            session.disconnect();
        } catch (Exception e) {
            Logger.logException(Client.class, e);
        }
    }

    /** Tra theo id nhân vật. Trả {@code null} nếu người đó không online. */
    public Player getPlayerByID(long playerId) {
        synchronized (lock) {
            return players_id.get(playerId);
        }
    }

    /** Tra theo id tài khoản. Dùng để phát hiện đăng nhập trùng. */
    public Player getPlayerByUser(int userId) {
        synchronized (lock) {
            return players_userId.get(userId);
        }
    }

    /** Tra theo tên nhân vật. <b>Phân biệt chữ hoa/thường.</b> */
    public Player getPlayerByName(String name) {
        synchronized (lock) {
            return players_name.get(name);
        }
    }

    /**
     * Bản <b>sao</b> của danh sách người chơi, an toàn để duyệt.
     *
     * <p>Luôn dùng hàm này thay cho {@code getPlayers()} khi cần lặp. Chi phí là một
     * lần sao chép mảng, đổi lại không bị {@code ConcurrentModificationException} và
     * không phải giữ khoá suốt vòng lặp.</p>
     */
    public List<Player> getPlayersSnapshot() {
        synchronized (lock) {
            return new ArrayList<>(players);
        }
    }

    /**
     * Đá toàn bộ người chơi — chạy khi server tắt hoặc vào bảo trì.
     *
     * <p>Chụp bản sao danh sách <i>trước</i> khi lặp, vì {@link #kickSession} sẽ sửa
     * chính danh sách gốc trong lúc chạy.</p>
     *
     * <p>Mỗi người một khối {@code try}: một người lỗi lúc lưu không được phép làm
     * những người còn lại mất dữ liệu.</p>
     *
     * <p><b>Chạy tuần tự.</b> Với 500 người online và mỗi người một lần ghi DB, bước
     * này có thể mất khá lâu — đó là phần lớn thời gian chờ khi bấm bảo trì.</p>
     */
    public void close() {
        List<Player> snapshot;

        synchronized (lock) {
            snapshot = new ArrayList<>(players);
        }

        Logger.log(Logger.YELLOW, "BEGIN KICK OUT SESSION " + snapshot.size() + "\n");

        for (Player pl : snapshot) {
            try {
                if (pl != null && pl.getSession() != null) {
                    this.kickSession(pl.getSession());
                }
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }

        Logger.success("SUCCESSFUL\n");
    }

    /**
     * Đá <b>mọi người chơi thường</b>, giữ lại tài khoản quản trị.
     *
     * <h2>Vì sao bảo trì phải đá người đang chơi</h2>
     *
     * <p>Cờ bảo trì vốn chỉ chặn <b>đăng nhập mới</b>. Người đã ở trong game thì
     * ở nguyên đó — nên bật bảo trì xong sân vẫn đông, và mọi lý do để bảo trì
     * (sửa lỗi đang gây hại, vá dữ liệu, chuẩn bị dựng lại máy chủ) đều diễn ra
     * ngay giữa lúc người chơi vẫn đang chơi.</p>
     *
     * <p>Đá qua {@link #kickSession} chứ không cắt kết nối thẳng: đó là đường ra
     * duy nhất có lưu dữ liệu. Cắt thẳng là mất mọi thứ làm được từ lượt tự lưu
     * gần nhất.</p>
     *
     * <p>Quản trị viên — cả cờ Founder lẫn quyền quản trị của panel — được ở
     * lại, để còn người kiểm tra trong lúc bảo trì.</p>
     *
     * @return số người đã bị đá
     */
    public int daNguoiThuong() {
        List<Player> snapshot = getPlayersSnapshot();
        int da = 0;
        for (Player pl : snapshot) {
            try {
                if (pl == null || pl.getSession() == null) {
                    continue;
                }
                if (pl.isFounder() || pl.isQuanTriVien()) {
                    continue;
                }
                Service.gI().sendThongBaoOK(pl.getSession(),
                        "Máy chủ vào bảo trì. Hẹn gặp lại sau.");
                this.kickSession(pl.getSession());
                da++;
            } catch (Exception e) {
                // Mot nguoi loi khong duoc lam nhung nguoi con lai o lai.
                Logger.logException(Client.class, e);
            }
        }
        Logger.log(Logger.YELLOW, "[BAO TRI] da " + da + " nguoi choi thuong\n");
        return da;
    }

    /**
     * Nhịp mỗi giây: đếm ngược {@code timeWait} và đá session không chịu đăng nhập.
     *
     * <p>{@code MySession.timeWait} khởi tạo bằng 100 và được đặt về 0 ngay khi đăng
     * nhập thành công. Vậy nên ai kết nối vào mà không đăng nhập sẽ bị đá sau khoảng
     * 100 giây. Đây là lớp phòng thủ chống việc mở hàng loạt kết nối rỗng.</p>
     *
     * <p><b>Duyệt ngược</b> ({@code i = size-1; i >= 0; i--}) vì phần tử bị xoá trong
     * lúc lặp — duyệt xuôi sẽ nhảy cóc qua phần tử kế tiếp.</p>
     *
     * <p><b>Rủi ro:</b> {@code SessionManager.getSessions()} trả danh sách gốc (một
     * {@code ArrayList} không đồng bộ) và hàm này <i>ghi</i> vào đó, trong khi thread
     * {@code Network} có thể đang thêm session mới. Xem ghi chú trong
     * {@code SessionManager}.</p>
     */
    private void update() {
        List<ISession> sessions = SessionManager.gI().getSessions();

        for (int i = sessions.size() - 1; i >= 0; i--) {
            ISession s = sessions.get(i);
            MySession session = (MySession) s;

            if (session == null) {
                sessions.remove(i);
                continue;
            }

            if (session.timeWait > 0) {
                session.timeWait--;

                if (session.timeWait == 0) {
                    kickSession(session);
                }
            }
        }
    }

    /**
     * Thread nền: gọi {@link #update()} mỗi giây.
     *
     * <p>Nhịp được giữ ổn định bằng cách trừ đi thời gian đã tốn cho {@code update()}
     * — nếu {@code update()} chạy 200ms thì chỉ ngủ 800ms. Sàn 10ms để không bao giờ
     * quay vòng không nghỉ khi máy quá tải.</p>
     *
     * <p>Thoát khi {@code ServerManager.isRunning} thành {@code false}.</p>
     */
    @Override
    public void run() {
        while (ServerManager.isRunning) {
            long st = System.currentTimeMillis();

            try {
                update();
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }

            Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - st), 10));
        }
    }
}