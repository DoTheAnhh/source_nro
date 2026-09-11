package nro.service;

import nro.gameplay.bot.Bot;
import nro.gameplay.bot.BotManager;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstPlayer;
import nro.core.util.FileIO;
import nro.data.DataGame;
import nro.repository.schema.GodGK;
import nro.service.effect.EffectSkillService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nro.entity.item.Item;
import nro.entity.map.ItemMap;
import nro.entity.mob.Mob;
import nro.entity.player.Detu;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.service.skill.SkillService;
import nro.server.Client;
import nro.server.Manager;
import nro.server.ServerManager;
import nro.service.fun.ChangeMapService;
import nro.core.log.Logger;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import nro.entity.boss.BossData;
import nro.entity.boss.BossID;
import nro.server.SystemMetrics;
import nro.core.util.FormatStyle;
import nro.core.util.Functions;
import com.mysql.jdbc.Connection;
import nro.core.consts.ConstAchievement;
import nro.core.consts.ConstAttribute;
import nro.core.consts.ConstDetu;
import nro.core.consts.ConstTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import nro.repository.ConnectDB;
import nro.net.api.ISession;
import java.util.concurrent.atomic.AtomicBoolean;
import nro.repository.dao.PlayerDAO;
import nro.net.io.Message;
import nro.net.session.MySession;
import nro.net.session.Session;
import nro.net.session.SessionManager;
import nro.service.achievement.AchievementService;
import nro.entity.attribute.Attribute;
import nro.entity.boss.list.commeson.NhanBan;
import nro.entity.boss.map.trainingboss.TrainningBoss;
import nro.entity.clan.Clan;
import nro.entity.map.destrongas.MyClanTopDestronGas;
import nro.entity.map.destrongas.TopDestronGas;
import nro.entity.map.majinbuu14h.MaBuHold;
import nro.entity.map.snakeway.MyClanTopSnakeWay;
import nro.entity.map.snakeway.TopSnakeWay;
import nro.entity.map.treasureundersea.MyClanTopTreasureUnderSea;
import nro.entity.map.treasureundersea.TopTreasureUnderSea;
import nro.entity.npc.NonInteractiveNPC;
import nro.entity.power.Caption;
import nro.service.power.CaptionManager;
import nro.entity.boss.map.trainingboss.TaskTauPayPay;
import nro.gameplay.bot.newgen.BotManager_new;
import nro.gameplay.bot.newgen.Bot_new;
import nro.entity.player.SaiBaMen;
import nro.entity.item.ItemOption;
import nro.entity.clan.ClanMember;
import nro.repository.CrisResultSet;
import org.json.simple.JSONArray;

public class Service {

    private static Service instance;
    public long lasttimechatbanv = 0;
    public long lasttimechatmuav = 0;

    private final AtomicBoolean isSaveTwo = new AtomicBoolean(false);

    public void AutoSavedDataBase() {
        if (!isSaveTwo.compareAndSet(false, true)) {
            return; // Check var
        }
        try {
            // Duyệt qua danh sách người chơi và cập nhật cơ sở dữ liệu
            for (Player player : Client.gI().getPlayers()) {
                if (player != null) {
                    PlayerDAO.updatePlayer(player);
                }
            }
            // Gim Ram + CPU
            System.gc();
            //Thời Gian Auto Lưu Dữ Liệu Người Chơi (Nhỏ Nhất = 8000)
            Thread.sleep(3_600_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            isSaveTwo.set(false);
        }
    }

    public static Service gI() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public void addEffectChar(Player pl, int id, int layer, int loop, int loopcount, int stand) {
        // Khởi tạo danh sách hiệu ứng nếu chưa có
        if (pl.idEffChar == null) {
            pl.idEffChar = new ArrayList<>();
        }

        // Thêm hiệu ứng nếu chưa có
        if (!pl.idEffChar.contains(id)) {
            pl.idEffChar.add(id);
        }

        try {
            Message msg = new Message(-128);
            msg.writer().writeByte(0);                  // Type
            msg.writer().writeInt((int) pl.id);         // ID người chơi
            msg.writer().writeShort(id);                // ID hiệu ứng
            msg.writer().writeByte(layer);              // Layer hiệu ứng
            msg.writer().writeByte(loop);               // Có lặp không
            msg.writer().writeShort(loopcount);         // Số lần lặp
            msg.writer().writeByte(stand);              // Hiệu ứng đứng hoặc gì đó

            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup(); // giải phóng tài nguyên sau khi gửi
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void LogicEffect(Player pl, int id, int layer, int loop, int loopcount, int stand, int times) {
        try {
            Message msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(id);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopcount);
            msg.writer().writeByte(stand);

            sendMessAllPlayerInMap(pl.zone, msg);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> removeEffectChar(pl, id), times, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    private void removeEffectChar(Player pl, int id) {
        try {
            Message msg = new Message(-128);
            msg.writer().writeByte(1); // Thông báo xóa hiệu ứng
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(id);

            sendMessAllPlayerInMap(pl.zone, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showListPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss"));
            msg.writer().writeByte(Client.gI().getPlayers().size());
            for (int i = 0; i < Client.gI().getPlayers().size(); i++) {
                Player pl = Client.gI().getPlayers().get(i);
                if (pl == null) {
                    pl = player;
                }
                msg.writer().writeShort(pl.getHead());
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);
                msg.writer().writeUTF(pl.isFounder() ? "Founder" : pl.isQuanTriVien() ? "Quản Trị Viên" : "Player");
                msg.writer().writeUTF("SỨC MẠNH: " + Util.formatNumber(pl.nPoint.power, FormatStyle.VIETNAMESE)
                        + "\nTIỀM NĂNG: " + Util.formatNumber(pl.nPoint.tiemNang, FormatStyle.VIETNAMESE)
                        + "\nHP: " + Util.formatNumber(pl.nPoint.hpMax, FormatStyle.VIETNAMESE)
                        + "\nKI: " + Util.formatNumber(pl.nPoint.mpMax, FormatStyle.VIETNAMESE)
                        + "\nSỨC ĐÁNH: " + Util.formatNumber(pl.nPoint.dame, FormatStyle.VIETNAMESE)
                        + "\nGIÁP: " + Util.formatNumber(pl.nPoint.def, FormatStyle.VIETNAMESE)
                        + "\nCHÍ MẠNG: " + pl.nPoint.crit + "%"
                        + "\n|7|[Map: " + pl.zone.map.mapName + "(" + pl.zone.map.mapId + ") " + "Khu: " + pl.zone.zoneId + "]");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showListBot(Player player) {
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss"));
            msg.writer().writeByte(BotManager.gI().getBot().size());
            for (int i = 0; i < BotManager.gI().getBot().size(); i++) {
                Bot pl = BotManager.gI().getBot().get(i);
                if (pl == null) {
                    pl = (Bot) player;
                }
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);
                msg.writer().writeUTF(pl.type == 0 ? "Farm Quái" : pl.type == 1 ? "Bán Item" : pl.type == 2 ? "Săn Boss" : pl.type == 3 ? "Mua Item" : "Không xác định!");
                msg.writer().writeUTF("ID: " + Util.format(pl.id)
                        + "\nSỨC MẠNH: " + Util.formatNumber(pl.nPoint.power, FormatStyle.VIETNAMESE)
                        + "\nTIỀM NĂNG: " + Util.formatNumber(pl.nPoint.tiemNang, FormatStyle.VIETNAMESE)
                        + "\nHP: " + Util.formatNumber(pl.nPoint.hpMax, FormatStyle.VIETNAMESE)
                        + "\nKI: " + Util.formatNumber(pl.nPoint.mpMax, FormatStyle.VIETNAMESE)
                        + "\nSỨC ĐÁNH: " + Util.formatNumber(pl.nPoint.dame, FormatStyle.VIETNAMESE)
                        + "\nGIÁP: " + Util.formatNumber(pl.nPoint.def, FormatStyle.VIETNAMESE)
                        + "\nCHÍ MẠNG: " + pl.nPoint.crit + "%"
                        + "\n|7|[Map: " + pl.zone.map.mapName + "(" + pl.zone.map.mapId + ") " + "Khu: " + pl.zone.zoneId + "]");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showListBot_New(Player player) {
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss"));
            msg.writer().writeByte(BotManager_new.gI().getBot().size());
            for (int i = 0; i < BotManager_new.gI().getBot().size(); i++) {
                Bot_new pl = BotManager_new.gI().getBot().get(i);
                if (pl == null) {
                    pl = (Bot_new) player;
                }
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);
                msg.writer().writeUTF(pl.type == 0 ? "Up Đệ Tử" : pl.type == 1 ? "Treo Đệ Tử" : "Không xác định!");
                msg.writer().writeUTF("ID: " + Util.format(pl.id)
                        + "\nSỨC MẠNH: " + Util.formatNumber(pl.nPoint.power, FormatStyle.VIETNAMESE)
                        + "\nTIỀM NĂNG: " + Util.formatNumber(pl.nPoint.tiemNang, FormatStyle.VIETNAMESE)
                        + "\nHP: " + Util.formatNumber(pl.nPoint.hpMax, FormatStyle.VIETNAMESE)
                        + "\nKI: " + Util.formatNumber(pl.nPoint.mpMax, FormatStyle.VIETNAMESE)
                        + "\nSỨC ĐÁNH: " + Util.formatNumber(pl.nPoint.dame, FormatStyle.VIETNAMESE)
                        + "\nGIÁP: " + Util.formatNumber(pl.nPoint.def, FormatStyle.VIETNAMESE)
                        + "\nCHÍ MẠNG: " + pl.nPoint.crit + "%"
                        + "\n|7|[Map: " + pl.zone.map.mapName + "(" + pl.zone.map.mapId + ") " + "Khu: " + pl.zone.zoneId + "]");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public String name(Player player) {
        if (player.isPl() && player.clan != null) {
            try {
                if (!player.clan.name2.isEmpty()) {
                    return "[" + player.clan.name2 + "] " + player.name;
                } else if (player.clan.name.length() > 3) {
                    return "[" + player.clan.name.substring(0, 3) + "] " + player.name;
                } else {
                    return "[" + player.clan.name + "] " + player.name;
                }
            } catch (Exception e) {
            }
        } else if (player.name == null) {
            return "";
        }
        return player.name;
    }

    public void sendPopUpMultiLine(Player pl, int tempID, int avt, String text) {
        Message msg = null;
        try {
            msg = new Message(-218);
            msg.writer().writeShort(tempID);
            msg.writer().writeUTF(text);
            msg.writer().writeShort(avt);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendLinhThu(Player player, short smallId) {
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) player.id);
            if (smallId == 0) {
                msg.writer().writeByte(0);
            } else {
                msg.writer().writeByte(1);
                msg.writer().writeShort(smallId);
                msg.writer().writeByte(1);
                int[] fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
                msg.writer().writeByte(fr.length);
                for (int i = 0; i < fr.length; i++) {
                    msg.writer().writeByte(fr[i]);
                }
                if (smallId >= 29924 && smallId <= 29949) {
                    if (smallId == 29946) {
                        msg.writer().writeShort(65);
                        msg.writer().writeShort(65);
                    } else {
                        msg.writer().writeShort(75);
                        msg.writer().writeShort(75);
                    }
                } else if (smallId >= 29950 && smallId <= 29973) {
                    msg.writer().writeShort(75);
                    msg.writer().writeShort(75);
                } else if (smallId >= 30378 && smallId <= 30419) {
                    msg.writer().writeShort(75);
                    msg.writer().writeShort(75);
                } else {
                    msg.writer().writeShort(65);
                    msg.writer().writeShort(65);
                }
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendLinhThuToMe(Player me, Player pl) {
        Item linhThu = pl.inventory.itemsBody.get(11);
        if (!linhThu.isNotNullItem()) {
            return;
        }
        short smallId = (short) (linhThu.template.iconID - 1);
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(1);
            msg.writer().writeShort(smallId);
            msg.writer().writeByte(1);
            int[] fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
            msg.writer().writeByte(fr.length);
            for (int i = 0; i < fr.length; i++) {
                msg.writer().writeByte(fr[i]);
            }
            if (smallId >= 29924 && smallId <= 29949) {
                if (smallId == 29946) {
                    msg.writer().writeShort(65);
                    msg.writer().writeShort(65);
                } else {
                    msg.writer().writeShort(75);
                    msg.writer().writeShort(75);
                }
            } else if (smallId >= 29950 && smallId <= 29973) {
                msg.writer().writeShort(75);
                msg.writer().writeShort(75);
            } else if (smallId >= 30378 && smallId <= 30419) {
                msg.writer().writeShort(75);
                msg.writer().writeShort(75);
            } else {
                msg.writer().writeShort(65);
                msg.writer().writeShort(65);
            }
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMessAllPlayer(Message msg) {
        PlayerService.gI().sendMessageAllPlayer(msg);
    }

    public void sendMessAllPlayerIgnoreMe(Player player, Message msg) {
        PlayerService.gI().sendMessageIgnore(player, msg);
    }

    public void sendMessAllPlayerInMap(Zone zone, Message msg) {
        if (zone == null) {
            msg.dispose();
            return;
        }
        List<Player> players = zone.getPlayers();
        if (players.isEmpty()) {
            msg.dispose();
            return;
        }
        for (int i = players.size() - 1; i >= 0; i--) {
            Player pl = players.get(i);
            if (pl != null) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();
    }

    public void sendMessAllPlayerInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.dispose();
            return;
        }
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            if (player instanceof TrainningBoss || player instanceof NonInteractiveNPC) {
                List<Player> players = player.zone.getPlayers();
                if (players.isEmpty()) {
                    msg.dispose();
                    return;
                }
                for (int i = 0; i < players.size(); i++) {
                    Player pl = players.get(i);
                    if (pl != null && (player instanceof NonInteractiveNPC || ((TrainningBoss) player).playerAtt.equals(pl))) {
                        pl.sendMessage(msg);
                    }
                }
            } else {
                player.sendMessage(msg);
            }
        } else {
            List<Player> players = player.zone.getPlayers();
            if (players.isEmpty()) {
                msg.dispose();
                return;
            }
            for (int i = 0; i < players.size(); i++) {
                Player pl = players.get(i);
                if (pl != null && pl.getSession() != null && pl.isPl()) {
                    pl.sendMessage(msg);
                }
            }
        }
        msg.cleanup();
    }

    public void sendMessAnotherNotMeInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.cleanup();
            return;
        }
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            if (player instanceof TrainningBoss || player instanceof NonInteractiveNPC) {
                List<Player> players = player.zone.getPlayers();
                if (players.isEmpty()) {
                    msg.dispose();
                    return;
                }
                for (int i = 0; i < players.size(); i++) {
                    Player pl = players.get(i);
                    if (pl != null && !pl.equals(player) && (player instanceof NonInteractiveNPC || ((TrainningBoss) player).playerAtt.equals(pl))) {
                        pl.sendMessage(msg);
                    }
                }
            }
        } else {
            List<Player> players = player.zone.getPlayers();
            if (players.isEmpty()) {
                msg.dispose();
                return;
            }
            for (int i = 0; i < players.size(); i++) {
                Player pl = players.get(i);
                if (pl != null && pl.getSession() != null && !pl.equals(player) && pl.isPl()) {
                    pl.sendMessage(msg);
                }
            }
        }
        msg.cleanup();
    }

    /**
     * Đọc hết các chuỗi UTF còn lại trong gói tin.
     *
     * <p>Dừng ở lỗi đọc đầu tiên — hết gói tin, hoặc phần đuôi không phải chuỗi.
     * Trả về những gì đọc được chứ không ném lỗi: chỗ gọi cần dữ liệu, không cần
     * biết gói tin dài bao nhiêu.</p>
     */
    private static java.util.List<String> docHetChuoi(Message msg) {
        java.util.List<String> ds = new java.util.ArrayList<>();
        for (int i = 0; i < 32; i++) {
            try {
                ds.add(msg.readUTF());
            } catch (Exception het) {
                break;
            }
        }
        return ds;
    }

    /** Tên đăng nhập chấp nhận được: 4–18 ký tự, chữ/số/gạch dưới. */
    private static boolean tenHopLe(String s) {
        return s != null && s.matches("[A-Za-z0-9_]{4,18}");
    }

    /** Mật khẩu chấp nhận được: 5–18 ký tự, không có khoảng trắng. */
    private static boolean matKhauHopLe(String s) {
        return s != null && s.length() >= 5 && s.length() <= 18
                && !s.contains(" ");
    }

    /**
     * Đăng ký tài khoản từ màn hình đăng nhập của client.
     *
     * <h3>Vì sao không đọc theo vị trí cố định nữa</h3>
     *
     * <p>Bản cũ bỏ qua <b>đúng bảy</b> chuỗi rồi đọc chuỗi thứ tám và thứ chín
     * làm tên và mật khẩu. Client gửi ít hơn bảy chuỗi là {@code readUTF} ném
     * lỗi hết gói tin, rơi vào {@code catch} và người chơi <b>luôn</b> thấy
     * "Đăng ký thất bại" — không phụ thuộc tên hay mật khẩu họ gõ.</p>
     *
     * <p>Nay đọc <b>hết</b> các chuỗi có trong gói tin rồi dò từ cuối về đầu để
     * tìm cặp (tên, mật khẩu) hợp lệ. Cách này đúng với mọi số trường, nên đổi
     * client cũng không hỏng lại.</p>
     */
    public void regisAccount(Session session, Message _msg) {
        try {
            java.util.List<String> truong = docHetChuoi(_msg);
            String user = null;
            String pass = null;
            for (int i = truong.size() - 2; i >= 0; i--) {
                if (tenHopLe(truong.get(i)) && matKhauHopLe(truong.get(i + 1))) {
                    user = truong.get(i);
                    pass = truong.get(i + 1);
                    break;
                }
            }
            if (user == null) {
                // Khong co cap nao hop le -> lay hai chuoi cuoi de bao ro SAI CHO
                // NAO, thay vi mot cau "that bai" chung chung.
                if (truong.size() < 2) {
                    Logger.warning("Đăng ký: gói tin chỉ có " + truong.size()
                            + " chuỗi, không đủ tên và mật khẩu\n");
                    sendThongBaoOK((MySession) session,
                            "Không đọc được tên đăng nhập và mật khẩu.");
                    return;
                }
                user = truong.get(truong.size() - 2);
                pass = truong.get(truong.size() - 1);
            }
            if (!(user.length() >= 4 && user.length() <= 18)) {
                sendThongBaoOK((MySession) session, "Tài khoản phải có độ dài 4-18 ký tự");
                return;
            }
            if (!(pass.length() >= 5 && pass.length() <= 18)) {
                sendThongBaoOK((MySession) session, "Mật khẩu phải có độ dài 5-18 ký tự");
                return;
            }
            CrisResultSet rs = ConnectDB.executeQuery("select * from account where username = ?", user);
            boolean daCo = rs.first();
            rs.dispose();
            if (daCo) {
                sendThongBaoOK((MySession) session, "Tài khoản đã tồn tại");
                return;
            }
            // Bon cot ve la NOT NULL va KHONG co gia tri mac dinh. Cau lenh cu
            // chi chen username/password nen MySQL tu choi, loi roi vao catch
            // rong ben duoi va client dung mai o man "Dang dang ky...".
            ConnectDB.executeUpdate(
                    "INSERT INTO account (username, password, vetuan, vethang,"
                    + " vetuan_expire, vethang_expire) VALUES (?, ?, 0, 0, 0, 0)",
                    user, pass);
            Logger.log(Logger.GREEN, "Tài khoản mới (đăng ký trong game): " + user + "\n");
            sendThongBaoOK((MySession) session, "Đăng ký tài khoản thành công!");
        } catch (Exception e) {
            Logger.logException(Service.class, e, "Lỗi đăng ký tài khoản");
            // PHAI bao cho client, neu khong no dung mai o man hinh cho.
            try {
                sendThongBaoOK((MySession) session,
                        "Đăng ký thất bại, vui lòng thử lại hoặc chọn tên khác.");
            } catch (Exception ignored) {
                // Client vua ngat ket noi -> khong con gi de lam.
            }
        }
    }

    public void Send_Info_NV(Player pl) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 14);
            msg.writer().writeInt((int) pl.id);
            msg.writeHp(Util.CrisGH(pl.nPoint.hp));
            msg.writer().writeByte(0);
            msg.writeHp(Util.CrisGH(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void Send_Info_NV_do_Injure(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);
            msg.writer().writeInt((int) pl.id);
            msg.writeHp(Util.CrisGH(pl.nPoint.hp));
            msg.writer().writeByte(2);
            msg.writeHp(Util.CrisGH(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendInfoPlayerEatPea(Player pl) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 14);
            msg.writer().writeInt((int) pl.id);
            msg.writeHp(Util.CrisGH(pl.nPoint.hp));
            msg.writer().writeByte(1);
            msg.writeHp(Util.CrisGH(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void loginDe(MySession session, short second) {
        Message msg;
        try {
            msg = new Message(122);
            msg.writer().writeShort(second);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void resetPoint(Player player, int x, int y) {
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            msg = new Message(46);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            player.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void clearMap(Player player) {
        Message msg;
        try {
            msg = new Message(-22);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void switchToRegisterScr(ISession session) {
        Message message;
        try {
            message = new Message(42);
            message.writeByte(0);
            session.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void chat(Player player, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeUTF(text);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void OpenMenuKeyOrAdmin(Player player) { // MENU_ADMIN
        Attribute tnsm = ServerManager.gI().getAttributeManager().find(ConstAttribute.TNSM);
        Attribute hp = ServerManager.gI().getAttributeManager().find(ConstAttribute.HP);
        Attribute ki = ServerManager.gI().getAttributeManager().find(ConstAttribute.KI);
        Attribute sd = ServerManager.gI().getAttributeManager().find(ConstAttribute.SUC_DANH);
        Attribute vang = ServerManager.gI().getAttributeManager().find(ConstAttribute.VANG);
        if (player.getSession() != null && player.isFounder()) {
            NpcService.gI().createMenuConMeo(player, ConstNpc.QUAN_TRI_ADMIN, 24222, "|7|[ - Admin : " + player.name + " - ]"
                    + "\n|1|Số Lượng Người Chơi Online : " + Client.gI().getPlayers().size() + " Người"
                    + "\n|1|Số Lượng Bot Hoạt Động : " + BotManager.gI().getBot().size() + " Bot"
                    + "\nEXP Server : X" + Manager.RATE_EXP_SERVER
                    + "\nEXP Pet: X" + Manager.TNPET
                    + "\n|2|Server Run Time : " + ServerManager.timeStart
                    + ((tnsm != null && tnsm.getValue() != 0 && tnsm.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + tnsm.getValue() + "% TNSM, thời gian còn lại " + tnsm.getTime() / 60 + " phút." : "")
                    + ((hp != null && hp.getValue() != 0 && hp.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + hp.getValue() + "% TNSM, thời gian còn lại " + hp.getTime() / 60 + " phút." : "")
                    + ((ki != null && ki.getValue() != 0 && ki.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + ki.getValue() + "% TNSM, thời gian còn lại " + ki.getTime() / 60 + " phút." : "")
                    + ((sd != null && sd.getValue() != 0 && sd.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + sd.getValue() + "% TNSM, thời gian còn lại " + sd.getTime() / 60 + " phút." : "")
                    + ((vang != null && vang.getValue() != 0 && vang.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + vang.getValue() + "% TNSM, thời gian còn lại " + vang.getTime() / 60 + " phút." : "")
                    + "\n|1|Tỉ Lệ Nạp : Thỏi Vàng x" + Manager.TY_LE_NAP_THOI_VANG + ", Hồng Ngọc x" + Util.format(Manager.TY_LE_NAP_HONG_NGOC)
                    + "\n|2|Số Thread : " + Thread.activeCount() + " , Số Session : " + SessionManager.gI().getSessions().size()
                    + "\n|7|" + SystemMetrics.ToString(),
                    "Menu Account", "Menu Buff", "Kiểm Tra\nGiftcode", "Bảo Trì", "Đóng");
        } else if (player.getSession() != null && player.isQuanTriVien()) {
            NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_KEY, 24222, "|7|[ - KEY : " + player.name + " - ]\n"
                    + "|2|Số Lượng Người Chơi Online : " + Client.gI().getPlayers().size() + " Người\n"
                    + "Server Run Time : " + ServerManager.timeStart + "\n"
                    + ((tnsm != null && tnsm.getValue() != 0 && tnsm.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + tnsm.getValue() + "% TNSM, thời gian còn lại " + tnsm.getTime() / 60 + " phút." : "")
                    + ((hp != null && hp.getValue() != 0 && hp.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + hp.getValue() + "% TNSM, thời gian còn lại " + hp.getTime() / 60 + " phút." : "")
                    + ((ki != null && ki.getValue() != 0 && ki.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + ki.getValue() + "% TNSM, thời gian còn lại " + ki.getTime() / 60 + " phút." : "")
                    + ((sd != null && sd.getValue() != 0 && sd.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + sd.getValue() + "% TNSM, thời gian còn lại " + sd.getTime() / 60 + " phút." : "")
                    + ((vang != null && vang.getValue() != 0 && vang.getTime() != 0)
                    ? "\n|8|Toàn bộ máy chủ được tăng " + vang.getValue() + "% TNSM, thời gian còn lại " + vang.getTime() / 60 + " phút." : "")
                    + "|1|Thread : " + Thread.activeCount() + ", Session : " + SessionManager.gI().getSessions().size() + "\n",
                    "Kiểm Tra\n Người Chơi", "Kiểm Tra\nGiftcode", "Buff Hồng Ngọc", "Thông Báo");
        }
    }

    public void chisonhanh(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CHI_SO_NHANH, 12713,
                "|7|CỘNG CHỈ SỐ NHANH"
                + "\n\n|2| Bạn muốn cộng nhanh chỉ số nào?",
                "HP", "KI", "SD", "Giáp");
    }

    public void chatJustForMe(Player me, Player plChat, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeUTF(text);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void Transport(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(pl.type);
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void Transport(Player pl, int type) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(type);
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public long exp_level1(long sucmanh) {
        if (sucmanh < 3000) {
            return 3000;
        } else if (sucmanh < 15000) {
            return 15000;
        } else if (sucmanh < 40000) {
            return 40000;
        } else if (sucmanh < 90000) {
            return 90000;
        } else if (sucmanh < 170000) {
            return 170000;
        } else if (sucmanh < 340000) {
            return 340000;
        } else if (sucmanh < 700000) {
            return 700000;
        } else if (sucmanh < 1500000) {
            return 1500000;
        } else if (sucmanh < 15000000) {
            return 15000000;
        } else if (sucmanh < 150000000) {
            return 150000000;
        } else if (sucmanh < 1500000000) {
            return 1500000000;
        } else if (sucmanh < 5000000000L) {
            return 5000000000L;
        } else if (sucmanh < 10000000000L) {
            return 10000000000L;
        } else if (sucmanh < 40000000000L) {
            return 40000000000L;
        } else if (sucmanh < 50010000000L) {
            return 50010000000L;
        } else if (sucmanh < 60010000000L) {
            return 60010000000L;
        } else if (sucmanh < 70010000000L) {
            return 70010000000L;
        } else if (sucmanh < 80010000000L) {
            return 80010000000L;
        } else if (sucmanh < 100010000000L) {
            return 100010000000L;
        }
        return 1000;
    }

    public void point(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        player.nPoint.calPoint();
        // Buoc "Nang suc danh goc len 10K" cua nhiem vu 29: xet o day vi day la
        // cho chi so duoc dung lai, tuc luc suc danh goc vua co the doi. Hai
        // duong goi kia (dung tiem nang, len suc manh) van giu — nguoi da du
        // tu truoc ma khong nang them lan nao cung qua duoc.
        nro.service.TaskService.gI().kiemTraSucDanhGoc(player);
        Send_Info_NV(player);
        if (!player.isDeTu && !player.isBo && !player.isMe && !player.isBoss && !player.isPetFollow && !player.isDuongTang && !player.isNguoiYeu && !player.isConOne && !player.isConTwo && !player.isConThree) {
            Message msg;
            try {
                msg = new Message(-42);
                msg.writeHp(Util.CrisGH(player.nPoint.hpg));
                msg.writeCris(Util.CrisGH(player.nPoint.mpg), Manager.readInt);
                msg.writeCris(Util.CrisGH(player.nPoint.dameg), Manager.readInt);
                msg.writeHp(Util.CrisGH(player.nPoint.hpMax));
                msg.writeCris(Util.CrisGH(player.nPoint.mpMax), Manager.readInt);
                msg.writeHp(Util.CrisGH(player.nPoint.hp));
                msg.writeCris(Util.CrisGH(player.nPoint.mp), Manager.readInt);
                msg.writer().writeByte(player.nPoint.speed);
                msg.writer().writeByte(20);
                msg.writer().writeByte(20);
                msg.writer().writeByte(1);
                msg.writeCris(Util.CrisGH(player.nPoint.dame), Manager.readInt);
                msg.writer().writeInt(player.nPoint.def);
                msg.writer().writeByte(player.nPoint.crit);
                msg.writer().writeLong(player.nPoint.tiemNang);
                msg.writer().writeShort(100);
                msg.writer().writeShort(player.nPoint.defg);
                msg.writer().writeByte(player.nPoint.critg);
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
        if (player.PhanThan != null) {
            player.PhanThan.nPoint.calPoint();
            player.PhanThan.nPoint.setFullHpMp();
            point(player.PhanThan);
        }
    }

    public void activeNamecShenron(Player pl) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(0);

            msg.writer().writeShort(22);
            msg.writer().writeShort(pl.zone.map.bgId);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) -1);
            msg.writer().writeUTF("");
            msg.writer().writeShort(-1);
            msg.writer().writeShort(-1);
            msg.writer().writeByte(-1);

            Service.gI().sendMessAllPlayerInMap(pl, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    /**
     * Mở đồng hồ đếm lùi lượt đấm máy đo sức mạnh trên màn hình người chơi.
     *
     * <p>Chỉ gửi một lần lúc mở lượt, kèm số giây; client tự đếm bằng đồng hồ
     * của nó. Bắn gói mỗi giây thì tốn đường truyền mà chữ vẫn giật theo độ trễ
     * mạng.</p>
     */
    public void guiDemNguocMayDam(Player pl, int giay) {
        Message msg = null;
        try {
            msg = new Message(-72);
            msg.writer().writeShort(giay);
            pl.sendMessage(msg);
        } catch (Exception ex) {
            nro.core.log.Logger.logException(Service.class, ex,
                    "Lỗi gửi đếm lùi máy đấm");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }



    /**
     * Gói <b>chỉ</b> mang giá trị hào quang — mã lệnh {@code -71}.
     *
     * <h3>Vì sao phải làm gói riêng</h3>
     *
     * <p>Hào quang vốn nằm trong gói thông tin nhân vật ({@link #player}), mà
     * gói đó chở theo cả đầu lẫn <b>mảng đồ đang mặc</b>. Gửi lại nó giữa game
     * là ghi đè toàn bộ ngoại hình: gửi đầu gốc thì mất cải trang, gửi đầu cải
     * trang thì đầu một kiểu thân một kiểu.</p>
     *
     * <p>Gói này chỉ hai trường nên đổi hào quang bao nhiêu lần cũng không đụng
     * gì tới hình dạng nhân vật.</p>
     */
    public void capNhatAura(Player pl) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-71);
            msg.writer().writeShort(pl.getAura());
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(Service.class, ex);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    public void player(Player pl) {
        if (pl == null) {
            return;
        }
        Message msg;
        try {
            msg = messageSubCommand((byte) 0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.playerTask.taskMain.id);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(pl.head);
            msg.writer().writeUTF(pl.name);
            msg.writer().writeByte(0); //cPK
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeLong(pl.nPoint.power);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(pl.gender);
            ArrayList<Skill> skills = (ArrayList<Skill>) pl.playerSkill.skills;
            msg.writer().writeByte(pl.playerSkill.getSizeSkill());
            for (Skill skill : skills) {
                if (skill.skillId != -1) {
                    msg.writer().writeShort(skill.skillId);
                }
            }
            if (pl.getSession().version >= 214) {
                msg.writer().writeLong(pl.inventory.gold);
            } else {
                msg.writer().writeInt((int) pl.inventory.gold);
            }
            msg.writer().writeInt(pl.inventory.ruby);
            msg.writer().writeInt(pl.inventory.gem);
            ArrayList<Item> itemsBody = (ArrayList<Item>) pl.inventory.itemsBody;
            msg.writer().writeByte(itemsBody.size());
            for (Item item : itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeInt(itemOption.optionTemplate.id);
                        msg.writer().writeInt(itemOption.param);
                    }
                }
            }
            ArrayList<Item> itemsBag = (ArrayList<Item>) pl.inventory.itemsBag;
            msg.writer().writeByte(itemsBag.size());
            for (int i = 0; i < itemsBag.size(); i++) {
                Item item = itemsBag.get(i);
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeInt(itemOption.optionTemplate.id);
                        msg.writer().writeInt(itemOption.param);
                    }
                }
            }
            ArrayList<Item> itemsBox = (ArrayList<Item>) pl.inventory.itemsBox;
            msg.writer().writeByte(itemsBox.size());
            for (int i = 0; i < itemsBox.size(); i++) {
                Item item = itemsBox.get(i);
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeInt(itemOption.optionTemplate.id);
                        msg.writer().writeInt(itemOption.param);
                    }
                }
            }
            DataGame.sendHeadAvatar(msg);
            msg.writer().writeShort(514);
            msg.writer().writeShort(515);
            msg.writer().writeShort(537);
            msg.writer().writeByte(pl.fusion.typeFusion != ConstPlayer.NON_FUSION ? 1 : 0);
            msg.writer().writeInt(pl.deltaTime);
            msg.writer().writeByte(pl.isNewMember ? 1 : 0);
            msg.writer().writeShort(pl.getAura());
            msg.writer().writeByte(pl.getEffFront());
            msg.writer().writeShort(pl.getHat());
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public Message messageNotLogin(byte command) throws IOException {
        Message ms = new Message(-29);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageNotMap(byte command) throws IOException {
        Message ms = new Message(-28);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageSubCommand(byte command) throws IOException {
        Message ms = new Message(-30);
        ms.writer().writeByte(command);
        return ms;
    }

    public void UpdateMap(Player pl) {
        DataGame.updateMap(pl.getSession());
    }

    public void addSMTN(Player player, byte type, long param, boolean isOri) {
        long start = System.currentTimeMillis();
        if (player.isDeTu || player.isBo || player.isMe || player.isNguoiYeu || player.isConOne || player.isConTwo || player.isConThree) {
            if (player.nPoint.power > player.nPoint.getPowerLimit()) {
                return;
            }
            long start2 = System.currentTimeMillis();
            player.nPoint.powerUp(Util.CrisGH(param));
            player.nPoint.tiemNangUp(Util.CrisGH(param));
            Player master = ((Detu) player).master;
            param = master.nPoint.calSubTNSM(param);
            // He so rieng cho phan SU PHU nhan, dat theo nhom ban do tren panel.
            //
            // Hoi ban do cua NGUOI DANG DANH chu khong phai cua su phu: de tu
            // dang cay o Ngu Hanh Son thi phan chia cho su phu cung phai theo
            // Ngu Hanh Son, ma luc ay su phu co the dang dung o dau khong biet.
            //
            // Nhan o day, tuc la sau khi de tu da nhan du phan minh, nen ha he
            // so nay khong lam de tu duoc it di.
            if (player.zone != null && player.zone.map != null) {
                double heSoSp = nro.repository.dao.HeSoTnsmDAO.heSoSuPhu(
                        player.zone.map.mapId);
                if (heSoSp != 1) {
                    param = Math.round(param * heSoSp);
                }
            }
            long endCal = System.currentTimeMillis();
            if (endCal - start > 50) {
                System.out.println("[SLOW] CALCULATOR POINT : " + (endCal - start) + " : " + (endCal - start2));
            }
            long start3 = System.currentTimeMillis();
            // Sư phụ nhận MỘT lần, qua đúng lời gọi đệ quy ngay dưới.
            //
            // Trước đây ở đây còn ba dòng cộng thẳng cho sư phụ:
            //
            //     if (master.nPoint.power < master.nPoint.getPowerLimit()) {
            //         master.nPoint.powerUp(param);
            //     }
            //     master.nPoint.tiemNangUp(param);
            //
            // rồi NGAY SAU đó gọi addSMTN(master, ...). Lời gọi ấy có player là
            // sư phụ, sư phụ không phải đệ tử nên rơi xuống nhánh dưới, và nhánh
            // đó làm lại đúng hai việc vừa làm. Kết quả: đệ giết một con quái
            // đáng 1.000 thì sư phụ ăn 2.000.
            //
            // Bỏ ba dòng đó, giữ lời gọi đệ quy, vì nó lo được nhiều hơn hẳn:
            // chặn trần sức mạnh cho CẢ hai chỉ số (ba dòng cũ chỉ chặn sức
            // mạnh, còn tiềm năng vẫn chảy vào sau khi chạm trần), cộng đúng
            // theo `type`, kiểm nhiệm vụ theo sức mạnh, và cộng TNSM cho bang
            // hội qua cờ isOri.
            //
            // Muốn giữ mức cũ thì đặt quy ước tl_de_tu_cho_su_phu = 200 trên
            // panel — xem NPoint.calSubTNSM.
            addSMTN(master, type, param, true);
            long endCal2 = System.currentTimeMillis();
            if (endCal2 - start3 > 50) {
                System.out.println("[SLOW] END MASTER : " + (endCal2 - start3));
            }
        } //        else if (player.isBot_Event || player.isBot_New) {
        //            player.nPoint.power += Util.CrisGH(param);
        //            player.nPoint.tiemNang += Util.CrisGH(param);
        //        }
        else {

            if (player.nPoint == null || player.nPoint.power > player.nPoint.getPowerLimit()) {
                return;
            }
            switch (type) {
                case 1:
                    player.nPoint.tiemNangUp(Util.CrisGH(param));
                    break;
                case 2:
                    long startpo = System.currentTimeMillis();
                    player.nPoint.powerUp(Util.CrisGH(param));
                    long ep = System.currentTimeMillis();
                    if (ep - startpo > 50) {
                        System.out.println("[SLOW] TASK : " + (ep - startpo));
                    }
                    player.nPoint.tiemNangUp(Util.CrisGH(param));
                    break;
                default:
                    player.nPoint.powerUp(Util.CrisGH(param));
                    break;
            }
            long ends = System.currentTimeMillis();

            if (ends - start > 50) {
                System.out.println("[SLOW] MASTER : " + (ends - start));
            }
            PlayerService.gI().sendTNSM(player, type, param);
            if (isOri) {
                if (player.clan != null) {
                    player.clan.addSMTNClan(player, param);
                }
            }
            long ends2 = System.currentTimeMillis();
            if (ends2 - ends > 50) {
                System.out.println("[SLOW] MASTER 2 : " + (ends2 - ends));
            }
        }
    }

    public void congTiemNang(Player pl, byte type, long tiemnang) {
        Message msg;
        try {
            msg = new Message(-3);
            msg.writer().writeByte(type);// 0 là cộng sm, 1 cộng tn, 2 là cộng cả 2
            msg.writeCris(Util.CrisGH(tiemnang), Manager.readInt);
            if (pl.isMaster()) {
                Player master = pl.getMaster();
                master.nPoint.powerUp(tiemnang);
                master.nPoint.tiemNangUp(tiemnang);
                master.sendMessage(msg);
            } else {
                pl.sendMessage(msg);
            }
            msg.cleanup();
            switch (type) {
                case 1:
                    pl.nPoint.tiemNangUp(tiemnang);
                    break;
                case 2:
                    pl.nPoint.powerUp(tiemnang);
                    pl.nPoint.tiemNangUp(tiemnang);
                    break;
                default:
                    pl.nPoint.powerUp(tiemnang);
                    break;
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public String get_HanhTinh(int hanhtinh) {
        switch (hanhtinh) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namếc";
            case 2:
                return "Xayda";
            default:
                return "";
        }
    }

    public List<String> ListCaption(int gender) {
        List<String> Captions = new ArrayList<>();
        Captions.add("Tân thủ");
        Captions.add("Tập sự sơ cấp");
        Captions.add("Tập sự trung cấp");
        Captions.add("Tập sự cao cấp");
        Captions.add("Tân binh");
        Captions.add("Chiến binh");
        Captions.add("Chiến binh cao cấp");
        Captions.add("Vệ binh");
        Captions.add("Vệ binh hoàng gia");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 1");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 2");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 3");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 4");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 1");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 2");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 3");
        Captions.add("Giới Vương Thần cấp 1");
        Captions.add("Giới Vương Thần cấp 2");
        Captions.add("Giới Vương Thần cấp 3");
        Captions.add("Thần hủy diệt cấp 1");
        Captions.add("Thần hủy diệt cấp 2");
        Captions.add("MaiTienDung");
        return Captions;
    }

    public String getCurrStrLevel(Player pl) {
        return ListCaption(pl.gender).get(getCurrLevel(pl));
    }

    public int getCurrLevel(Player pl) {
        if (pl.nPoint == null) {
            return 0;
        }
        long sucmanh = pl.nPoint.power;
        if (sucmanh < 3000) {
            return 0;
        } else if (sucmanh < 15000) {
            return 1;
        } else if (sucmanh < 40000) {
            return 2;
        } else if (sucmanh < 90000) {
            return 3;
        } else if (sucmanh < 170000) {
            return 4;
        } else if (sucmanh < 340000) {
            return 5;
        } else if (sucmanh < 700000) {
            return 6;
        } else if (sucmanh < 1500000) {
            return 7;
        } else if (sucmanh < 15000000) {
            return 8;
        } else if (sucmanh < 150000000) {
            return 9;
        } else if (sucmanh < 1500000000) {
            return 10;
        } else if (sucmanh < 5000000000L) {
            return 11;
        } else if (sucmanh < 10000000000L) {
            return 12;
        } else if (sucmanh < 40000000000L) {
            return 13;
        } else if (sucmanh < 50010000000L) {
            return 14;
        } else if (sucmanh < 60010000000L) {
            return 15;
        } else if (sucmanh < 70010000000L) {
            return 16;
        } else if (sucmanh < 80010000000L) {
            return 17;
        } else if (sucmanh < 100010000000L) {
            return 18;
        } else if (sucmanh < 11100010000000L) {
            return 19;
        }
        return 20;
    }

    public void hsChar(Player pl, long hp, long mp) {
        Message msg;
        try {
            // De tu SONG LAI tu cho chet thi luon day mau, day khi.
            //
            // Duong tu hoi sinh cua de tu von da truyen hpMax, nhung con it nhat
            // bon duong khac hoi sinh voi so nho hon: ba cho trong
            // ChangeMapService hoi sinh voi 1 mau, va ky nang hoi sinh dung mau
            // dang co. Chan ngay o day thi duong nao cung dung — ke ca duong
            // viet sau nay.
            //
            // Chi khi dang CHET: hsChar con duoc goi de dong bo mau cho nguoi
            // dang song, luc ay de nguyen con so duoc truyen vao.
            if (pl.isDeTu && pl.nPoint != null && pl.isDie()) {
                hp = pl.nPoint.hpMax;
                mp = pl.nPoint.mpMax;
            }
            pl.setJustRevivaled();
            if (pl.isPl() && pl.effectSkill != null && pl.effectSkill.isBodyChangeTechnique) {
                PlayerService.gI().changeAndSendTypePK(pl, 5);
            }
            pl.nPoint.setHp(Util.CrisGH(hp));
            pl.nPoint.setMp(Util.CrisGH(mp));
            if (!pl.isDeTu && !pl.isBo && !pl.isMe && !pl.isPetFollow && !pl.isDuongTang && !pl.isNguoiYeu && !pl.isConOne && !pl.isConTwo && !pl.isConThree) {
                msg = new Message(-16);
                pl.sendMessage(msg);
                msg.cleanup();
                PlayerService.gI().sendInfoHpMpMoney(pl);
            }
            msg = messageSubCommand((byte) 15);
            msg.writer().writeInt((int) pl.id);
            // Client doc hai so nay la MAU va MAU TOI DA. Ban cu gui mp vao cho
            // mau toi da, nen vua hoi sinh thanh mau hien sai toi luc goi khac ve.
            msg.writeHp(Util.CrisGH(hp));
            msg.writeHp(Util.CrisGH(pl.nPoint.hpMax));
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            Send_Info_NV(pl);
            PlayerService.gI().sendInfoHpMp(pl);
            // De tu vua song lai: bao lai cho su phu ngay. Khong thi o de tu
            // tren client cua su phu — ca khung "Thong tin de tu" — van hien
            // mau cu cho toi lan hoi ke tiep.
            if (pl.isDeTu && pl instanceof Detu && ((Detu) pl).master != null) {
                showInfoPet(((Detu) pl).master);
            }
            AchievementService.gI().checkDoneTask(pl, ConstAchievement.THANH_HOI_SINH);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void charDie(Player pl) {
        if (pl == null || pl.location == null) {
            return;
        }
        Message msg;
        try {
            if (!pl.isDeTu) {
                msg = new Message(-17);
                msg.writer().writeByte((int) pl.id);
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                pl.sendMessage(msg);
                msg.cleanup();
            } else {
                if (pl.isDeTu) {
                    ((Detu) pl).lastTimeDie = System.currentTimeMillis();
                }
            }
            msg = new Message(-8);
            msg.writer().writeShort((int) pl.id);
            int cPk = 0;
            msg.writer().writeByte(cPk); //cpk
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
            Send_Info_NV(pl);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void playerInfoUpdate(Player pl, Player plR, String plName, int plHead, int plBody, int plLeg) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            msg = messageSubCommand((byte) 7);
            msg.writer().writeInt((int) pl.id);
            if (pl.clan != null) {
                msg.writer().writeInt(pl.clan.id);
            } else if (pl.isCopy) {
                msg.writer().writeInt(-2);
            } else {
                msg.writer().writeInt(-1);
            }
            msg.writer().writeByte(CaptionManager.getInstance().getLevel(pl));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(plHead);
            msg.writer().writeUTF(plName);
            msg.writeHp(Util.CrisGH(pl.nPoint.hp));
            msg.writeHp(Util.CrisGH(pl.nPoint.hpMax));
            msg.writer().writeShort(plBody);
            msg.writer().writeShort(plLeg);
            msg.writer().writeByte(pl.getFlagBag());
            msg.writer().writeByte(-1);
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(0);
            msg.writer().writeShort(pl.getAura()); //idauraeff
            msg.writer().writeByte(pl.getEffFront()); //seteff
            msg.writer().writeShort(pl.getHat()); //id hat
            plR.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void RadarSetAura(Player pl) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(4);
            message.writer().writeInt((int) pl.id);
            message.writer().writeShort(pl.getAura());
            message.writer().writeByte(-1);
            message.writer().flush();
            Service.gI().sendMessAllPlayerInMap(pl.zone, message);
            message.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void attackMob(Player pl, int mobId, boolean isMobMe, int masterId) {
        if (pl != null && pl.zone != null) {
            if (!isMobMe) {
                for (Mob mob : pl.zone.mobs) {
                    if (mob.id == mobId) {
                        SkillService.gI().useSkill(pl, null, mob, -1, null);
                        break;
                    }
                }
            } else {
                Player plAtt = pl.zone.getPlayerInMap(masterId);
                if (plAtt != null && SkillService.gI().canAttackPlayer(pl, plAtt)) {
                    Mob mob = plAtt.DeTrung;
                    if (mob != null) {
                        mob.injured(pl, Util.CrisGH(pl.nPoint.getDameAttack(false)), true);
                    }
                }
            }
        }
    }

    public void Send_Caitrang(Player player) {
        if (player != null) {
            Message msg;
            try {
                msg = new Message(-90);
                msg.writer().writeByte(1);// check type
                msg.writer().writeInt((int) player.id); //id player
                short head = player.getHead();
                short body = player.getBody();
                short leg = player.getLeg();

                msg.writer().writeShort(head);//set head
                msg.writer().writeShort(body);//setbody
                msg.writer().writeShort(leg);//set leg
                msg.writer().writeByte((player.effectSkill.isMonkey) ? 1 : 0);// thêm biến hình
                RadarSetAura(player);
                sendMessAllPlayerInMap(player, msg);
                msg.cleanup();
                if (player.PhanThan != null) {
                    Send_Caitrang(player.PhanThan);
                }
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    public void setNotMonkey(Player player) {
        Message msg;
        try {
            msg = new Message(-90);
            msg.writer().writeByte(-1);
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFlagBag(Player pl) {
        Message msg;
        try {
            if (pl.PhanThan != null) {
                sendFlagBag(pl.PhanThan);
            }
            msg = new Message(-64);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.getFlagBag());
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoOK(Player pl, String text) {
        if (pl.isDeTu || pl.isBo || pl.isMe || pl.isPetFollow || pl.isDuongTang || pl.isNguoiYeu || pl.isConOne || pl.isConTwo || pl.isConThree) {
            return;
        }
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoOK(MySession session, String text) {
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoAllPlayer(String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            this.sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendBigMessage(Player player, int iconId, String text) {
        try {
            Message msg;
            msg = new Message(-70);
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendBigMessage(Player player, int iconId, String text, String p, String caption) {
        try {
            Message msg;
            msg = new Message(-70);
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(1);
            msg.writer().writeUTF(p);
            msg.writer().writeUTF(caption);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendBigMessageWithItem(Player player, int size, int iconNPC, String text, String name, int iconID, int quantity, int sizeOption, int idOption, int param) {
        try {
            Message msg;
            msg = new Message(-71);
            msg.writer().writeShort(iconNPC);
            msg.writer().writeInt(size);
            msg.writer().writeUTF(text);
            msg.writer().writeUTF(name);
            msg.writer().writeInt(iconID);
            msg.writer().writeInt(quantity);

            msg.writer().writeInt(sizeOption);
            msg.writer().writeInt(idOption);
            msg.writer().writeInt(param);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoFromAdmin(Player player, String text) {
        sendBigMessage(player, 1139, text);
    }

    /**
     * Mở một khối làm hàng loạt: các gói làm mới bị hoãn tới khi đóng khối.
     *
     * <p>Đếm theo độ sâu nên lồng nhau được — một hàm gộp gói gọi vào hàm khác
     * cũng gộp gói thì chỉ khối ngoài cùng mới xả.</p>
     *
     * <p><b>Luôn đặt {@link #xaGomGoi} trong khối {@code finally}.</b> Quên xả
     * là người chơi đó không nhận được hành trang hay số tiền mới nữa cho tới
     * khi có việc khác gửi lại — nhìn như hành trang đứng hình.</p>
     */
    public void batGomGoi(Player pl) {
        if (pl != null) {
            pl.gomGoi++;
        }
    }

    /** Đóng khối làm hàng loạt và gửi những gói đã hoãn. */
    public void xaGomGoi(Player pl) {
        if (pl == null) {
            return;
        }
        pl.gomGoi--;
        if (pl.gomGoi > 0) {
            return;      // con o trong mot khoi ngoai, chua toi luot xa
        }
        pl.gomGoi = 0;
        if (pl.canGuiTui) {
            pl.canGuiTui = false;
            nro.service.inventory.InventoryService.gI().sendItemBag(pl);
        }
        if (pl.canGuiTien) {
            pl.canGuiTien = false;
            sendMoney(pl);
        }
        if (pl.shopMoLaiSauGom != null) {
            String tag = pl.shopMoLaiSauGom;
            boolean ag = pl.shopMoLaiAllGender;
            pl.shopMoLaiSauGom = null;
            nro.service.shop.ShopService.gI().opendShop(pl, tag, ag);
        }
        if (pl.thongBaoGom != null) {
            String s = pl.thongBaoGom;
            pl.thongBaoGom = null;
            sendThongBao(pl, s);
        }
    }

    public void sendThongBao(Player pl, String thongBao) {
        if (pl != null && pl.gomGoi > 0) {
            // Giu cau CUOI CUNG. Mot tram vong lap moi vong mot dong thong bao
            // thi khung chat cuon het, va nguoi choi khong doc duoc gi ca —
            // cau dang ke luon la cau cuoi (ket qua, hoac ly do dung lai).
            pl.thongBaoGom = thongBao;
            return;
        }
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBao(List<Player> pl, String thongBao) {
        for (int i = 0; i < pl.size(); i++) {
            Player ply = pl.get(i);
            if (ply != null) {
                this.sendThongBao(ply, thongBao);
            }
        }
    }

    public void sendThongBaoToClan(Clan clan, String thongBao) {
        if (clan == null) {
            return;
        }
        for (ClanMember cm : clan.getMembers()) {
            Player member = Client.gI().getPlayerByID(cm.id);
            if (member != null) {
                sendThongBao(member, thongBao);
            }
        }
    }

    public void sendThongBaoToAnotherNotMe(Player me, String text) {
        for (int i = 0; i < Client.gI().getPlayers().size(); i++) {
            Player pl = Client.gI().getPlayers().get(i);
            if (pl != null && !pl.equals(me)) {
                this.sendThongBao(pl, text);
            }
        }
    }

    public void sendPercentMabuEgg(Player player, byte percent) {
        try {
            Message msg = new Message(-117);
            msg.writer().writeByte(percent);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMoney(Player pl) {
        if (pl != null && pl.gomGoi > 0) {
            pl.canGuiTien = true;
            return;
        }
        Message msg;
        try {
            msg = new Message(6);
            if (pl.isPl()) {
                if (pl.getSession().version >= 214) {
                    msg.writer().writeLong(pl.inventory.gold);
                } else {
                    msg.writer().writeInt((int) pl.inventory.gold);
                }
            }
            msg.writer().writeInt(pl.inventory.gem);
            msg.writer().writeInt(pl.inventory.ruby);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendVipExp(Player pl) {
        Message msg;
        try {
            msg = new Message(6);
            if (pl.isPl()) {
                if (pl.getSession().version >= 214) {
                    msg.writer().writeLong(pl.inventory.gold);
                } else {
                    msg.writer().writeInt((int) pl.inventory.gold);
                }
            }
            msg.writer().writeInt(pl.inventory.gem);
            msg.writer().writeInt(pl.inventory.ruby);
            msg.writer().writeInt(pl.inventory.Exp_Vip);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendToAntherMePickItem(Player player, int itemMapId) {
        Message msg;
        try {
            msg = new Message(-19);
            msg.writer().writeShort(itemMapId);
            msg.writer().writeInt((int) player.id);
            sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setNotTranformation(Player player) {
        Message msg;
        try {
            msg = new Message(-90);
            msg.writer().writeByte(-1);
            msg.writer().writeInt((int) player.id);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setNotVolution(Player player) {
        Message msg;
        try {
            msg = new Message(-90);
            msg.writer().writeByte(-1);
            msg.writer().writeInt((int) player.id);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public static final int[] flagTempId = {363, 364, 365, 366, 367, 368, 369, 370, 371, 519, 520, 747};
    public static final int[] flagIconId = {2761, 2330, 2323, 2327, 2326, 2324, 2329, 2328, 2331, 4386, 4385, 2325};

    public void openFlagUI(Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(0);
            msg.writer().writeByte(flagTempId.length);
            for (int i = 0; i < flagTempId.length; i++) {
                msg.writer().writeShort(flagTempId[i]);
                msg.writer().writeByte(1);
                switch (flagTempId[i]) {
                    case 363:
                        msg.writer().writeByte(73);
                        msg.writer().writeShort(0);
                        break;
                    case 371:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(10);
                        break;
                    default:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(5);
                        break;
                }
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    private void sendChangeFlag(Player pl, int index) throws IOException {
        pl.cFlag = (byte) index;

        Message msg = new Message(-103);
        msg.writer().writeByte(1);
        msg.writer().writeInt((int) pl.id);
        msg.writer().writeByte(index);
        Service.gI().sendMessAllPlayerInMap(pl, msg);
        msg.cleanup();

        msg = new Message(-103);
        msg.writer().writeByte(2);
        msg.writer().writeByte(index);
        msg.writer().writeShort(flagIconId[index]);
        Service.gI().sendMessAllPlayerInMap(pl, msg);
        msg.cleanup();
    }

    public void changeFlag(Player pl, int index) {
        try {
            sendChangeFlag(pl, index);

            if (pl.Detu != null) {
                sendChangeFlag(pl.Detu, index);
            }

            pl.iDMark.setLastTimeChangeFlag(System.currentTimeMillis());

            if (pl.gender == 2 && index == 8 && Util.canDoWithTime(pl.Lasttimekhisukien, 120000)) {
                try {
                    pl.khisukien = true;
                    pl.Lasttimekhisukien = System.currentTimeMillis();
                    ConnectDB.executeUpdate(
                            "update player set lastTimeKhiSuKien = ? where name = ?",
                            System.currentTimeMillis(), pl.name
                    );
                } catch (Exception e) {
                    Logger.logException(Service.class, e);
                }
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFlagPlayerToMe(Player me, Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(pl.cFlag);
            msg.writer().writeShort(flagIconId[pl.cFlag]);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void chooseFlag(Player pl, int index) {
        if (MapService.gI().isMapBlackBallWar(pl.zone.map.mapId)
                || MapService.gI().isMapMaBu12H(pl.zone.map.mapId)
                || MapService.gI().isMapPVP(pl.zone.map.mapId)
                || MapService.gI().isMapNguHanhSon(pl.zone.map.mapId)) {
            sendThongBaoFromAdmin(pl, "Không thể đổi cờ khi bạn ở khu vực này!");
            return;
        }
        // Chon dung la co dang mang: khong lam gi, va khong tinh gio.
        if (pl.cFlag == index) {
            return;
        }

        // Gio cho 60 giay CHI ap khi nguoi choi DANG MANG co.
        //
        // Luat nay co de nguoi ta khong nhay phe lien tuc giua luc danh nhau.
        // Ai chua mang co thi chua thuoc phe nao — deo co lan dau khong co gi
        // de chong, bat cho o day chi lam nguoi moi tuong game hong.
        //
        // Khong co duong vong: changeFlag ghi lai thoi diem o MOI lan doi, nen
        // deo co xong van phai cho 60 giay moi thao ra hay doi sang co khac.
        if (pl.cFlag != 0
                && !Util.canDoWithTime(pl.iDMark.getLastTimeChangeFlag(), 60000)) {
            sendThongBao(pl, "Chỉ được đổi cờ sau "
                    + TimeUtil.getTimeLeft(pl.iDMark.getLastTimeChangeFlag(), 60) + " nữa");
            return;
        }
        changeFlag(pl, index);
    }

    public void attackPlayer(Player pl, int idPlAnPem) {
        Player player;
        if (MapService.gI().isMapOffline(pl.zone.map.mapId)) {
            player = pl.zone.getPlayerInMapOffline(pl, idPlAnPem);
        } else {
            player = pl.zone.getPlayerInMap(idPlAnPem);
        }
        SkillService.gI().useSkill(pl, player, null, -1, null);
    }

    public void UpdateAllMap(Player pl) {
        pl.zone.mapInfo(pl);
        DataGame.updateMap(pl.getSession());
    }

    public void dropItemMap(Zone zone, ItemMap item) {
        // Do roi viet cung cua boss dang bat "chan do goc" thi khong bao cho
        // client — neu bao ma Zone.addItem da bo qua, nguoi choi thay mot mon
        // khong nhat duoc.
        if (nro.entity.boss.Boss.biChanDoGoc()) {
            return;
        }
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt(3);//
            sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void dropItemMapForMe(Player player, ItemMap item) {
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt(3);//
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    private static final String[] NORMAL_REQUIREMENTS = {
        null,
        "Cần đạt sức mạnh 150tr để mở",
        "Cần đạt sức mạnh 1tỷ5 để mở",
        "Cần đạt sức mạnh 20tỷ để mở"
    };

    private static final String[] MABU_REQUIREMENTS = {
        null,
        "Cần đạt sức mạnh 150tr để mở",
        "Cần đạt sức mạnh 1tỷ5 để mở",
        "Cần đạt sức mạnh 20tỷ để mở",
        "Cần đạt sức mạnh 60tỷ để mở"
    };

    public void showInfoPet(Player pl) {
        if (pl != null && pl.Detu != null) {
            Message msg;
            try {
                msg = new Message(-107);
                msg.writer().writeByte(2);
                msg.writer().writeShort(pl.Detu.getAvatar());
                msg.writer().writeByte(pl.Detu.inventory.itemsBody.size());

                for (Item item : pl.Detu.inventory.itemsBody) {
                    if (!item.isNotNullItem()) {
                        msg.writer().writeShort(-1);
                    } else {
                        msg.writer().writeShort(item.template.id);
                        msg.writer().writeInt(item.quantity);
                        msg.writer().writeUTF(item.getInfo());
                        msg.writer().writeUTF(item.getContent());

                        int countOption = item.itemOptions.size();
                        msg.writer().writeByte(countOption);
                        for (ItemOption iop : item.itemOptions) {
                            msg.writer().writeInt(iop.optionTemplate.id);
                            msg.writer().writeInt(iop.param);
                        }
                    }
                }

                msg.writeHp(Util.CrisGH(pl.Detu.nPoint.hp)); //hp
                msg.writeHp(Util.CrisGH(pl.Detu.nPoint.hpMax)); //hpfull
                msg.writeCris(Util.CrisGH(pl.Detu.nPoint.mp), Manager.readInt); //mp
                msg.writeCris(Util.CrisGH(pl.Detu.nPoint.mpMax), Manager.readInt); //mpfull
                msg.writeCris(Util.CrisGH(pl.Detu.nPoint.dame), Manager.readInt); //damefull
                msg.writer().writeUTF(pl.Detu.name); //name
                msg.writer().writeUTF(pl.Detu.getNameThuctinh(pl.Detu.thuctinh) + pl.Detu.getStrLevel()); //curr level
                msg.writer().writeLong(pl.Detu.nPoint.power); //power
                msg.writer().writeLong(pl.Detu.nPoint.tiemNang); //tiềm năng
                msg.writer().writeByte(pl.Detu.getStatus()); //status
                msg.writer().writeShort(pl.Detu.nPoint.stamina); //stamina
                msg.writer().writeShort(pl.Detu.nPoint.maxStamina); //stamina full
                msg.writer().writeByte(pl.Detu.nPoint.crit); //crit
                msg.writer().writeShort(pl.Detu.nPoint.def); //def
                int sizeSkill = pl.Detu.playerSkill.skills.size();
                msg.writer().writeByte(sizeSkill); //count pet skill
                for (int i = 0; i < sizeSkill; i++) {
                    if (pl.Detu.playerSkill.skills.get(i).skillId != -1) {
                        msg.writer().writeShort(pl.Detu.playerSkill.skills.get(i).skillId);
                    } else {
                        String[] requirements = ((pl.Detu.typeDeTu == ConstDetu.U_BU || pl.Detu.typeDeTu == ConstDetu.KID_JIREN || pl.Detu.typeDeTu == ConstDetu.KID_BEER|| pl.Detu.typeDeTu == ConstDetu.BLACK)) ? MABU_REQUIREMENTS : NORMAL_REQUIREMENTS;
                        if (i < requirements.length && requirements[i] != null) {
                            msg.writer().writeShort(-1);
                            msg.writer().writeUTF(requirements[i]);
                        }
                    }
                }

                // Gioi tinh cua DE, ghi o cuoi goi.
                //
                // De tu co gioi tinh RIENG, boc ngau nhien luc tao
                // (DetuService: pet.gender = Util.nextInt(0, 2)), khong theo su
                // phu. Truoc day goi nay khong gui no, nen client de nguyen
                // cgender = 0 va setDefaultBody/setDefaultLeg luon lay bo phan
                // cua Trai Dat. De Namec hay Xayda thi hai ma anh do khong ton
                // tai va than voi chan bien mat, chi con moi cai dau.
                //
                // Ghi o CUOI de khong day lech bat ky truong nao co san.
                msg.writer().writeByte(pl.Detu.gender);

                // Ma anh THAN va CHAN cua de.
                //
                // Khong de client tu suy ra nua. Client chi co setDefaultBody /
                // setDefaultLeg, hai ham do tra ve bo phan mac dinh theo hanh
                // tinh va CHI dung khi de dang mac ao quan. De chua mac gi thi
                // hai so do khong phai bo phan that cua no — trong game ra mot
                // cuc toi khong ra hinh gi.
                //
                // getBody / getLeg la dung ham may chu dung o moi noi khac de ve
                // nhan vat, da tinh ca cai trang, hoa khi, bien hinh.
                msg.writer().writeShort(pl.Detu.getBody());
                msg.writer().writeShort(pl.Detu.getLeg());

                // Ma anh DAU cua de.
                //
                // Truoc day client tu suy ra dau tu gioi tinh, nen de dang BIEN
                // KHI van hien dau nguoi: dau khi khong theo gioi tinh ma theo
                // CAP bien khi (ConstPlayer.HEADMONKEY[levelMonkey - 1]), va chi
                // may chu biet cap do. Cai trang, hoa da, bien hinh Halloween…
                // cung the.
                //
                // getHead() la dung ham may chu dung o moi noi khac de ve nhan
                // vat, nen mot dong nay lo het cac truong hop tren.
                msg.writer().writeShort(pl.Detu.getHead());

                pl.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    public void InfoPetGoc(Player pl) {
        if (pl != null && pl.Detu != null) {
            Message msg;
            try {
                msg = new Message(-109);
                msg.writeCris(Util.CrisGH(pl.Detu.nPoint.hpg), Manager.readInt); //hp
                msg.writeCris(Util.CrisGH(pl.Detu.nPoint.mpg), Manager.readInt); //hpfull
                msg.writeCris(Util.CrisGH(pl.Detu.nPoint.dameg), Manager.readInt); //mp
                msg.writer().writeShort(pl.Detu.nPoint.defg);
                msg.writer().writeByte(pl.Detu.nPoint.critg);
                pl.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }
    //

    public void getPlayerMenu(Player player, int playerId) {
        Message msg;
        try {
            msg = new Message(-79);
            Player pl = player.zone.getPlayerInMap(playerId);
            if (pl != null && pl.isBot_Valentine) {
                msg.writer().writeInt(playerId);
                msg.writer().writeLong(pl.nPoint.power);
                msg.writer().writeUTF(Service.gI().getCurrStrLevel(pl));
                player.sendMessage(msg);
                SubMenuService.gI().showMenuValentine(player);
                msg.cleanup();
            } else {
                if (pl != null) {
                    msg.writer().writeInt(playerId);
                    msg.writer().writeLong(pl.nPoint.power);
                    msg.writer().writeUTF(Service.gI().getCurrStrLevel(pl));
                    player.sendMessage(msg);
                }
                SubMenuService.gI().showMenu(player);
                msg.cleanup();
                if (player.iDMark.isAcpTrade()) {
                    player.iDMark.setAcpTrade(false);
                    return;
                }
                if (player.isFounder()) {
                    //                SubMenuService.gI().showMenuForAdmin(player);
                }
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void hideWaitDialog(Player pl) {
        Message msg;
        try {
            msg = new Message(-99);
            msg.writer().writeByte(-1);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void chatPrivate(Player plChat, Player plReceive, String text) {
        if (Functions.isSpam(plChat, text)) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(92);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            if (plChat.getSession().version > 214) {
                msg.writer().writeShort(-1);
            }
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag());
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plChat.sendMessage(msg);
            // Receive
            msg = new Message(92);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            if (plReceive.getSession().version > 214) {
                msg.writer().writeShort(-1);
            }
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag());
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plReceive.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void changePassword(Player player, String oldPass, String newPass, String rePass) {
        if (player.getSession().pp.equals(oldPass)) {
            if (newPass.length() >= 5) {
                if (newPass.equals(rePass)) {
                    player.getSession().pp = newPass;
                    try {
                        ConnectDB.executeUpdate("update account set password = ? where id = ? and username = ?",
                                rePass, player.getSession().userId, player.getSession().uu);
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thành công!");
                    } catch (Exception ex) {
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thất bại!");
                        Logger.logException(Service.class, ex);
                    }
                } else {
                    Service.gI().sendThongBao(player, "Mật khẩu nhập lại không đúng!");
                }
            } else {
                Service.gI().sendThongBao(player, "Mật khẩu ít nhất 5 ký tự!");
            }
        } else {
            Service.gI().sendThongBao(player, "Mật khẩu cũ không đúng!");
        }
    }

    public void switchToCreateChar(MySession session) {
        Message msg;
        try {
            msg = new Message(2);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    /**
     * Mở ô nhập chữ ở client và chờ người chơi gõ.
     *
     * <p>Gói {@code 88} đi <b>hai chiều</b> cùng một mã: máy chủ gửi
     * {@code (câu hỏi, mã ô)}, client mở ô nhập rồi gửi lại
     * {@code (mã ô, chuỗi đã gõ)}. Mã ô là thứ duy nhất cho biết chuỗi trả lời
     * câu hỏi nào — {@code Controller} nhận gói về sẽ tra theo nó, nên mỗi chỗ
     * hỏi phải có một mã riêng trong {@code ConstNpc}.</p>
     *
     * <p>Không có gì bảo đảm người chơi trả lời: họ bấm Đóng là hết chuyện, và
     * máy chủ không giữ trạng thái chờ nào cả. Vì vậy chỗ nhận chuỗi phải tự
     * kiểm tra lại toàn bộ điều kiện, đừng tin vào bối cảnh lúc hỏi.</p>
     *
     * @param maO một hằng số trong {@code ConstNpc}, ví dụ
     *            {@code O_NHAP_SO_LAN_NHAP_NGOC}
     */
    public void moHopNhapChu(Player player, String cauHoi, short maO) {
        if (player == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(88);
            msg.writer().writeUTF(cauHoi);
            msg.writer().writeShort(maO);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendCaption(MySession session, byte gender) {
        Message msg;
        try {
            List<Caption> captions = CaptionManager.getInstance().getCaptions();
            msg = new Message(-41);
            msg.writer().writeByte(captions.size());
            for (Caption caption : captions) {
                msg.writer().writeUTF(caption.getCaption(gender));
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendHavePet(Player player) {
        Message msg;
        try {
            msg = new Message(-107);
            msg.writer().writeByte(player.Detu == null ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendWaitToLogin(MySession session, int secondsWait) {
        Message msg;
        try {
            msg = new Message(122);
            msg.writer().writeShort(secondsWait);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMessage(MySession session, int cmd, String path) {
        Message msg;
        try {
            msg = new Message(cmd);
            msg.writer().write(FileIO.readFile(path));
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void createItemMap(Player player, int tempId) {
        ItemMap itemMap = new ItemMap(player.zone, tempId, 1, player.location.x, player.location.y, player.id);
        dropItemMap(player.zone, itemMap);
    }

    public void sendNangDong(Player player) {
        Message msg;
        try {
            msg = new Message(-97);
            msg.writer().writeInt(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendAutoTraning(Player player) {
        Message msg;
        try {
            msg = new Message(-116);
            msg.writer().writeByte(1);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendStopTraning(Player player) {
        Message msg;
        try {
            msg = new Message(-116);
            msg.writer().writeByte(0);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setClientType(MySession session, Message msg) {
        try {
            session.typeClient = (msg.reader().readByte());//client_type
            session.zoomLevel = msg.reader().readByte();//zoom_level
            msg.reader().readBoolean();//is_gprs
            msg.reader().readInt();//width
            msg.reader().readInt();//height
            msg.reader().readBoolean();//is_qwerty
            msg.reader().readBoolean();//is_touch
            String platform = msg.reader().readUTF();
            String[] arrPlatform = platform.split("\\|");
            session.version = Integer.parseInt(arrPlatform[1].replaceAll("\\.", ""));
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            msg.cleanup();
        }
        DataGame.sendLinkIP(session);
    }

    //------------------------------COPPY PASTE---------------------------------
    public void sendChibi(Player player) {
        Item linhThu = player.inventory.itemsBody.get(11);
        short smallId = (short) (player.effectSkill.isChibi ? player.typeChibi + 5000 : !linhThu.isNotNullItem() ? 0 : linhThu.template.iconID - 1);
        if (!player.effectSkill.isChibi) {
            //sendLinhThu(player, smallId);
            return;
        }
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) player.id);
            if (smallId == 0) {
                msg.writer().writeByte(0);
            } else {
                msg.writer().writeByte(1);
                msg.writer().writeShort(smallId);
                msg.writer().writeByte(1);
                int[] fr = new int[]{0, 1, 2};
                msg.writer().writeByte(fr.length);
                for (int i = 0; i < fr.length; i++) {
                    msg.writer().writeByte(fr[i]);
                }
                msg.writer().writeShort(32);
                msg.writer().writeShort(32);
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendHaveChibiFollowToAllMap(Player pl) {
        if (pl.zone != null) {
            for (Player plMap : pl.zone.getPlayers()) {
                if (plMap.isPlandBot()) {
                    //sendChibiFollowToMe(plMap, pl);
                }
            }
        }
    }

    public void sendChibiFollowToMe(Player me, Player pl) {
        Item linhThu = pl.inventory.itemsBody.get(11);
        short smallId = (short) (pl.effectSkill.isChibi ? pl.typeChibi + 5000 : !linhThu.isNotNullItem() ? 0 : linhThu.template.iconID - 1);
        if (!pl.effectSkill.isChibi) {
            //sendLinhThuToMe(me, pl);
            return;
        }
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(1);
            msg.writer().writeShort(smallId);
            msg.writer().writeByte(1);
            int[] fr = new int[]{0, 1, 2};
            msg.writer().writeByte(fr.length);
            for (int i = 0; i < fr.length; i++) {
                msg.writer().writeByte(fr[i]);
            }
            msg.writer().writeShort(32);
            msg.writer().writeShort(32);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendHideNpc(Player player, int npcId, boolean isHide) {
        Message msg;
        try {
            msg = new Message(-73);
            msg.writer().writeByte(npcId);
            msg.writer().writeByte(isHide ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setPos(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setPos2(Player player, int x, int y) {
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setPos0(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(0);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendPlayerVS(Player pVS1, Player pVS2, byte type) {
        Message msg = null;
        try {
            pVS1.typePk = type;
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS1.id); //ID PLAYER
            msg.writer().writeByte(type); //TYPE PK
            pVS1.sendMessage(msg);
            if (pVS2 != null && pVS2.isPl()) {
                pVS2.sendMessage(msg);
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendPVB(Player pVS1, Player pVS2, byte type) {
        Message msg = null;
        try {
            pVS1.typePk = type;
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS1.id); //ID PLAYER
            msg.writer().writeByte(type); //TYPE PK
            pVS1.sendMessage(msg);
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS2.id); //ID PLAYER
            msg.writer().writeByte(type); //TYPE PK
            pVS1.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendPVP(Player p1, Player p2) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) p2.id);
            msg.writer().writeByte(3);
            p1.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void dropSatellite(Player pl, Item item, Zone map, int x, int y) {
        ItemMap itemMap = new ItemMap(map, item.template, item.quantity, x, y, pl.id);
        itemMap.options = item.itemOptions;
        if (pl.clan != null) {
            itemMap.clanId = pl.clan.id;
        }
        map.addItem(itemMap);
        Message msg = null;
        try {
            msg = new Message(68);
            msg.writer().writeShort(itemMap.itemMapId);
            msg.writer().writeShort(itemMap.itemTemplate.id);
            msg.writer().writeShort(itemMap.x);
            msg.writer().writeShort(itemMap.y);
            msg.writer().writeInt(-2);
            msg.writer().writeShort(200);
            sendMessAllPlayerInMap(map, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void reload_HP_NV(Player pl) {
        Message msg = null;
        try {
            msg = messageSubCommand((byte) 9);
            msg.writeCris(Util.CrisGH(pl.id), Manager.readInt);
            msg.writeHp(Util.CrisGH(pl.nPoint.hp));
            msg.writeHp(Util.CrisGH(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void releaseCooldownSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                skill.lastTimeUseThisSkill = System.currentTimeMillis() - skill.coolDown;
                int leftTime = 0;
                msg.writer().writeInt(leftTime);
            }
            pl.sendMessage(msg);
            pl.nPoint.setMp(Util.CrisGH(pl.nPoint.mpMax));
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendTimeSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                msg.writer().writeInt(timeLeft);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendTimeSkill(Player pl, Skill skill) {
        Message msg;
        try {
            msg = new Message(-94);
            msg.writer().writeShort(skill.skillId);
            int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
            if (timeLeft < 0) {
                timeLeft = 0;
            }
            msg.writer().writeInt(timeLeft);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void releaseCooldownSkill(Player pl, Skill skill) {
        Message msg;
        try {
            msg = new Message(-94);
            msg.writer().writeShort(skill.skillId);
            skill.lastTimeUseThisSkill = System.currentTimeMillis() - skill.coolDown;
            int leftTime = 0;
            msg.writer().writeInt(leftTime);
            pl.sendMessage(msg);
            pl.nPoint.setMp(Util.CrisGH(pl.nPoint.mpMax));
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void Send_Body_Mob(Mob mob, int type, int idIcon) {
        Message msg = null;
        try {
            msg = new Message(-112);
            msg.writer().writeByte(type);
            msg.writer().writeByte(mob.id);
            if (type == 1) {
                msg.writer().writeShort(idIcon);//set body
            }
            sendMessAllPlayerInMap(mob.zone, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffPlayer(Player pl, Player plReceive, int idEff, int layer, int loop, int loopCount) {
        Message msg;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopCount);
            msg.writer().writeByte(0);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendEffAllPlayer(Player pl, int idEff, int layer, int loop, int loopCount) {
        Message msg;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopCount);
            msg.writer().writeByte(0);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void removeEffAllPlayer(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) pl.id);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void removeEffPlayer(Player pl, int idEff) {
        Message msg;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendEffPlayer(Player pl) {
        if (pl.isPl() && pl.inventory != null && pl.inventory.itemsBody.size() > 11) {
            Item chanMenh = pl.inventory.itemsBody.get(11);
            if (chanMenh.isNotNullItem()) {
                Service.gI().sendEffAllPlayer(pl, chanMenh.template.part, 0, -1, 1);
            }
        }
    }

    public void sendEffAllPlayerMapToMe(Player pl) {
        try {
            for (Player plM : pl.zone.getPlayers()) {
                if (plM.isPl() && plM.inventory.itemsBody.size() >11) {
                    Item chanmenh = plM.inventory.itemsBody.get(11);
                    if (chanmenh.isNotNullItem()) {
                        Service.gI().sendEffPlayer(plM, pl, chanmenh.template.part, 0, -1, 1);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void callNhanBan(Player player) {
        List<Skill> skillList = new ArrayList<>();
        for (byte i = 0; i < player.playerSkill.skills.size(); i++) {
            Skill skill = player.playerSkill.skills.get(i);
            if (skill.point > 0 && skill.template.id != Skill.TU_SAT && skill.template.id != Skill.TROI) {
                skillList.add(skill);
            }
        }
        int[][] skillTemp = new int[skillList.size()][3];
        for (byte i = 0; i < skillList.size(); i++) {
            Skill skill = skillList.get(i);
            if (skill.point > 0 && skill.template.id != Skill.TU_SAT && skill.template.id != Skill.TROI) {
                skillTemp[i][0] = skill.template.id;
                skillTemp[i][1] = skill.point;
                skillTemp[i][2] = skill.coolDown;
            }
        }
        BossData bossDataClone = new BossData(
                player.name,
                player.gender,
                new short[]{player.getHead(), player.getBody(), player.getLeg(), player.getFlagBag(), player.getAura(), player.getEffFront()},
                Functions.maxint(player.nPoint.dame + 1L),
                new long[]{Functions.maxint(player.nPoint.hpMax * 2L)},
                new int[]{140},
                skillTemp,
                new String[]{"|-2|Boss nhân bản đã xuất hiện rồi"}, //text chat 1
                new String[]{"|-1|Ta sẽ thay thế ngươi, haha"}, //text chat 2
                new String[]{"|-1|Lần khác ta sẽ xử đẹp ngươi"}, //text chat 3
                60
        );

        try {
            new NhanBan(player, bossDataClone);
            EffectSkillService.gI().setPKCommeson(player, 300000);
            player.lastPkCommesonTime = System.currentTimeMillis();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMabuHold(Player player, int action, short x, short y) {
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            if (action == 0) {
                setPos(player, x, y);
            }
            msg = new Message(52);
            msg.writer().writeByte(action); // 0 false, 1 true
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMabuHoldToMe(Player player, Player plReceive, int action, short x, short y) {
        Message msg;
        try {
            msg = new Message(52);
            msg.writer().writeByte(action); // 0 false, 1 true
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendEffMabuHoldAllPlayerMapToMe(Player pl) {
        for (Player plM : pl.zone.getPlayers()) {
            if (plM.isPl()) {
                if (plM.maBuHold != null) {
                    sendMabuHoldToMe(plM, pl, 1, (short) plM.maBuHold.x, (short) plM.maBuHold.y);
                }
            }
        }
    }

    public void sendEffMabuEat(Player player, Player plTarget) {
        Message msg;
        try {
            msg = new Message(52);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt((int) plTarget.id);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMabuEat(Player player, Player plTarget) {
        if (plTarget.isPl() && plTarget.maBuHold == null) {
            MaBuHold mabuHold = player.zone.getMaBuHold();
            if (mabuHold != null) {
                new Thread(() -> {
                    int zoneId = player.zone.zoneId;
                    player.zone.setMaBuHold(mabuHold.slot, zoneId, plTarget);
                    sendEffMabuEat(player, plTarget);
                    Functions.sleep(3000);
                    if (player.zone == null || player.zone.map.mapId != 127) {
                        return;
                    }
                    Zone zone = MapService.gI().getMapById(128).zones.get(zoneId);
                    ChangeMapService.gI().changeMap(plTarget, zone, -1, 336);
                    Functions.sleep(500);
                    plTarget.isMabuHold = false;
                    if (plTarget.effectSkill != null && !plTarget.effectSkill.isShielding) {
                        EffectSkillService.gI().setMabuHold(plTarget, mabuHold);
                        Functions.sleep(1500);
                        if (plTarget.fusion != null && plTarget.Detu != null && plTarget.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                            plTarget.Detu.unFusion();
                        }
                    }
                }).start();
            }
        }
    }

    public void sendMabuAttackSkill(Player player) {
        Message msg;
        try {
            int skillId[] = {0, 1, 3};
            int skill = skillId[Util.nextInt(3)];
            if (Util.isTrue(1, 10)) {
                skill = 2;
            }
            msg = new Message(51);
            msg.writer().writeInt((int) player.id); // charid
            msg.writer().writeByte(skill); // skill id 0 1 2 3
            msg.writer().writeShort(player.location.x); // x
            msg.writer().writeShort(player.location.y); // y
            msg.writer().writeByte(player.zone.getNotBosses().size()); // số player
            for (Player plM : player.zone.getNotBosses()) {
                msg.writer().writeInt((int) plM.id);
                double damage = plM.injured(player, player.nPoint.dame + plM.nPoint.hp / 10, true, false);
                msg.writeCris(Util.CrisGH(damage), Manager.readInt);
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendBigBoss(Zone zone, int action, int size, int id, double dame) {
        Message msg = null;
        try {
            msg = new Message(102);
            msg.writer().writeByte(action);
            if (action != 6 && action != 7) {
                msg.writer().writeByte(size); // SIZE PLAYER ATTACK
                msg.writer().writeInt(id); // PLAYER ID
                msg.writeCris(Util.CrisGH(dame), Manager.readInt); // DAME
            }
            sendMessAllPlayerInMap(zone, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendBigBoss2(Zone zone, int action, Mob bigboss) {
        Message msg = null;
        try {
            msg = new Message(101);
            msg.writer().writeByte(action);
            msg.writer().writeShort(bigboss.location.x);
            msg.writer().writeShort(bigboss.location.y);
            sendMessAllPlayerInMap(zone, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendBigBoss2(Player player, int action, Mob bigboss) {
        Message msg = null;
        try {
            msg = new Message(101);
            msg.writer().writeByte(action);
            msg.writer().writeShort(bigboss.location.x);
            msg.writer().writeShort(bigboss.location.y);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void SendPowerInfo(Player player) {
        Message msg = null;
        try {
            msg = new Message(-115);
            msg.writer().writeUTF("TL");
            msg.writer().writeShort(player.fightMabu.pointMabu);
            msg.writer().writeShort(player.fightMabu.POINT_MAX);
            msg.writer().writeShort(3);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void SendPercentPowerInfo(Player player) {
        Message msg = null;
        try {
            msg = new Message(-115);
            msg.writer().writeUTF("%");
            msg.writer().writeShort(player.fightMabu.pointPercent);
            msg.writer().writeShort(player.fightMabu.POINT_MAX * 2);
            msg.writer().writeShort(3);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void SendMabu(Zone zone, int percent) {
        Message msg = null;
        try {
            msg = new Message(-117);
            msg.writer().writeByte(percent);
            sendMessAllPlayerInMap(zone, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void mabaove(Player player, int mbv) {
        if (Integer.toString(mbv).length() != 6) {
            Service.gI().sendThongBaoOK(player, "Mã bảo vệ phải có độ dài là 6 số.");
        } else if (player.mbv == 0) {
            player.iDMark.setMbv(mbv);
            NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1, "Bạn chưa từng kích hoạt chức năng mã bảo vệ để kích hoạt bạn cần có 10tr vàng, mật khẩu của bạn là: " + mbv, "Đồng ý", "Từ chối");
        } else if (player.mbv != mbv) {
            Service.gI().sendThongBao(player, "Mật khẩu không đúng. Vui lòng kiểm tra lại");
        } else {
            if (player.baovetaikhoan) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1, "Tài khoản đang được bảo vệ\nBạn có muốn tắt bảo vệ không?", "Đồng ý", "Từ chối");
            } else {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1, "Tài khoản không được bảo vệ\nBạn muốn bật chứ năng bảo vệ tài khoản?", "Đồng ý", "Từ chối");
            }
        }
    }

    public void sendSpeedPlayer(Player pl, int speed) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 8);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(speed != -1 ? speed : pl.nPoint.speed);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void moveFast(Player pl, int x, int y) {
        Message msg;
        try {
            msg = new Message(58);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeInt((int) pl.id);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void dropHongDaoAndPickItem(Player pl, int itemId, int quantity) {
        ItemMap item = new ItemMap(pl.zone, itemId, quantity, pl.location.x + Util.nextInt(-20, 20), pl.location.y, pl.id);
        item.options.add(new ItemOption(174, 2025));
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void dropAndPickItem(Player pl, int itemId, int quantity) {
        ItemMap item = new ItemMap(pl.zone, itemId, quantity, pl.location.x, pl.location.y, pl.id);
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void dropAndPickItemDNC(Player pl, int itemId) {
        ItemMap item = new ItemMap(pl.zone, itemId, 1, pl.location.x, pl.location.y, pl.id);
        item.options.add(new ItemOption(71 - (itemId - 220), 0));
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void dropItem_OngTreNuoc(Player pl, int quantity) {
        ItemMap item = new ItemMap(pl.zone, 1527, quantity, pl.location.x + Util.nextInt(-15, 15), pl.location.y, pl.id);
        item.options.add(new ItemOption(30, 0));
        item.options.add(new ItemOption(93, 30));
        Service.gI().dropItemMap(pl.zone, item);
    }

    public void dropItemBossNomal_PickItem(Player pl) {
        ItemMap item;
        if (Util.isTrue(80, 100)) {
            int rand = Util.nextInt(0, 6);
            short idItem = (short) (rand + 441);
            item = new ItemMap(pl.zone, idItem, 1, pl.location.x + Util.nextInt(-10, 10), pl.location.y, pl.id);
            item.options.add(new ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
        } else {
            short idItem = 459;
            item = new ItemMap(pl.zone, idItem, 1, pl.location.x + Util.nextInt(-10, 10), pl.location.y, pl.id);
            item.options.add(new ItemOption(112, 80));
            item.options.add(new ItemOption(93, 90));
            item.options.add(new ItemOption(20, Util.nextInt(10000)));
        }
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void sendTopRank(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-119);
            msg.writer().writeInt(pl.superRank.rank);
            pl.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendTypePK(Player player, Player boss) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public Message messageReadOpt(byte command) throws IOException {
        Message ms = new Message(24);
        ms.writer().writeByte(command);
        return ms;
    }

    public void sendMessageServer(String data) {
        Message msg;
        try {
            msg = messageReadOpt((byte) 4);
            msg.writer().writeUTF(data);
            sendMessAllPlayer(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendLoginFail(MySession session, boolean isLoggingIn) {
        Message msg;
        try {
            msg = new Message(-102);
            msg.writer().writeByte(isLoggingIn ? 1 : 0);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showTopClanKhiGas(Player player) {
        TopDestronGas.getInstance().load();
        List<Clan> list = TopDestronGas.getInstance().getList();
        Message msg = new Message(-96);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Clan clan = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) clan.id);
                msg.writer().writeShort(clan.getLeader().head);
                msg.writer().writeShort(clan.getLeader().head == 31 ? 268 : clan.getLeader().head == 64 ? 19 : clan.getLeader().head == 30 ? 266
                        : clan.getLeader().head == 32 ? 271 : clan.getLeader().head == 29 ? 263 : clan.getLeader().head == 9 ? 121
                        : clan.getLeader().head == 28 ? 261 : clan.getLeader().head == 6 ? 91 : clan.getLeader().head == 27 ? 259 : 5223);
                msg.writer().writeShort(clan.getLeader().body);
                msg.writer().writeShort(clan.getLeader().leg);
                msg.writer().writeUTF("Bang hội: " + clan.name);
                msg.writer().writeUTF("Lv: " + clan.levelDoneKhiGas + " Trong " + Util.convertMillisecondsDouble(clan.thoiGianHoanThanhKhiGas) + " giây");
                msg.writer().writeUTF("Bang chủ: " + clan.getLeader().name);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showMyTopClanKhiGas(Player player) {
        if (player.clan != null) {
            MyClanTopDestronGas.getInstance().load2(player.clan.getLeader().id);
            List<Player> list = MyClanTopDestronGas.getInstance().getList();
            Message msg = new Message(-96);
            try {
                msg.writer().writeByte(0);
                msg.writer().writeUTF("Thành tích bang");
                msg.writer().writeByte(list.size());
                for (int i = 0; i < list.size(); i++) {
                    Player pl = list.get(i);
                    Player pl2 = GodGK.loadPlayerByID(pl.id);
                    msg.writer().writeInt(i + 1);
                    msg.writer().writeInt((int) pl.id);
                    msg.writer().writeShort(pl2.getHead());
                    msg.writer().writeShort(pl.head == 31 ? 268 : pl.head == 64 ? 19 : pl.head == 30 ? 266
                            : pl.head == 32 ? 271 : pl.head == 29 ? 263 : pl.head == 9 ? 121
                                                    : pl.head == 28 ? 261 : pl.head == 6 ? 91 : pl.head == 27 ? 259 : 5223);
                    msg.writer().writeShort(pl2.getBody());
                    msg.writer().writeShort(pl2.getLeg());
                    msg.writer().writeUTF("Bang hội: " + pl.nameClan);
                    msg.writer().writeUTF("Lv: " + pl.levelKhiGasDone + " (" + Util.convertSecondsToTime(pl.lastTimeUpdateTopKhiGas) + ")");
                    msg.writer().writeUTF("Bang chủ: " + pl.name + "\n[" + Util.convertMilliseconds(pl.timeKhiGasDone) + "]");
                }
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        } else {
            Message msg = new Message(-96);
            try {
                msg.writer().writeByte(0);
                msg.writer().writeUTF("Thành tích bang");
                msg.writer().writeByte(0);
                msg.writer().writeInt(0);
                msg.writer().writeInt(0);
                msg.writer().writeShort(-1);
                msg.writer().writeShort(5223);
                msg.writer().writeShort(-1);
                msg.writer().writeShort(-1);
                msg.writer().writeUTF("Chưa có");
                msg.writer().writeUTF("Chưa có");
                msg.writer().writeUTF("Chưa có");
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    public void showTopClanBDKB(Player player) {
        TopTreasureUnderSea.getInstance().load();
        List<Clan> list = TopTreasureUnderSea.getInstance().getList();
        Message msg = new Message(-96);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Clan clan = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) clan.id);
                msg.writer().writeShort(clan.getLeader().head);
                msg.writer().writeShort(clan.getLeader().head == 31 ? 268 : clan.getLeader().head == 64 ? 19 : clan.getLeader().head == 30 ? 266
                        : clan.getLeader().head == 32 ? 271 : clan.getLeader().head == 29 ? 263 : clan.getLeader().head == 9 ? 121
                        : clan.getLeader().head == 28 ? 261 : clan.getLeader().head == 6 ? 91 : clan.getLeader().head == 27 ? 259 : 5223);
                msg.writer().writeShort(clan.getLeader().body);
                msg.writer().writeShort(clan.getLeader().leg);
                msg.writer().writeUTF("Bang hội: " + clan.name);
                msg.writer().writeUTF("Lv: " + clan.levelDoneBanDoKhoBau + " Trong " + Util.convertMillisecondsDouble(clan.thoiGianHoanThanhBDKB) + " giây");
                msg.writer().writeUTF("Bang chủ: " + clan.getLeader().name);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showMyTopClanBDKB(Player player) {
        MyClanTopTreasureUnderSea.getInstance().load2(player.clan.getLeader().id);
        List<Player> list = MyClanTopTreasureUnderSea.getInstance().getList();
        Message msg = new Message(-96);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Thành tích bang");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player pl = list.get(i);
                Player pl2 = GodGK.loadPlayerByID(pl.id);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl2.getHead());
                msg.writer().writeShort(pl.head == 31 ? 268 : pl.head == 64 ? 19 : pl.head == 30 ? 266
                        : pl.head == 32 ? 271 : pl.head == 29 ? 263 : pl.head == 9 ? 121
                                                : pl.head == 28 ? 261 : pl.head == 6 ? 91 : pl.head == 27 ? 259 : 5223);
                msg.writer().writeShort(pl2.getBody());
                msg.writer().writeShort(pl2.getLeg());
                msg.writer().writeUTF("Bang hội: " + pl.nameClan);
                msg.writer().writeUTF("Lv: " + pl.levelBDKBDone + " (" + Util.convertSecondsToTime(pl.lastTimeUpdateTopBDKB) + ")");
                msg.writer().writeUTF("Bang chủ: " + pl.name + "\n[" + Util.convertMilliseconds(pl.timeBDKBDone) + "]");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showTopClanCDRD(Player player) {
        TopSnakeWay.getInstance().load();
        List<Clan> list = TopSnakeWay.getInstance().getList();
        Message msg = new Message(-96);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Clan clan = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) clan.id);
                msg.writer().writeShort(clan.getLeader().head);
                msg.writer().writeShort(clan.getLeader().head == 31 ? 268 : clan.getLeader().head == 64 ? 19 : clan.getLeader().head == 30 ? 266
                        : clan.getLeader().head == 32 ? 271 : clan.getLeader().head == 29 ? 263 : clan.getLeader().head == 9 ? 121
                        : clan.getLeader().head == 28 ? 261 : clan.getLeader().head == 6 ? 91 : clan.getLeader().head == 27 ? 259 : 5223);
                msg.writer().writeShort(clan.getLeader().body);
                msg.writer().writeShort(clan.getLeader().leg);
                msg.writer().writeUTF("Bang hội: " + clan.name);
                msg.writer().writeUTF("Lv: " + clan.levelDoneConDuongRanDoc + " Trong " + Util.convertMillisecondsDouble(clan.thoiGianHoanThanhCDRD) + " giây");
                msg.writer().writeUTF("Bang chủ: " + clan.getLeader().name);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showMyTopClanCDRD(Player player) {
        MyClanTopSnakeWay.getInstance().load2(player.clan.getLeader().id);
        List<Player> list = MyClanTopSnakeWay.getInstance().getList();
        Message msg = new Message(-96);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Thành tích bang");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player pl = list.get(i);
                Player pl2 = GodGK.loadPlayerByID(pl.id);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl2.getHead());
                msg.writer().writeShort(pl.head == 31 ? 268 : pl.head == 64 ? 19 : pl.head == 30 ? 266
                        : pl.head == 32 ? 271 : pl.head == 29 ? 263 : pl.head == 9 ? 121
                                                : pl.head == 28 ? 261 : pl.head == 6 ? 91 : pl.head == 27 ? 259 : 5223);
                msg.writer().writeShort(pl2.getBody());
                msg.writer().writeShort(pl2.getLeg());
                msg.writer().writeUTF("Bang hội: " + pl.nameClan);
                msg.writer().writeUTF("Lv: " + pl.levelCDRDDone + " (" + Util.convertSecondsToTime(pl.lastTimeUpdateTopCDRD) + ")");
                msg.writer().writeUTF("Bang chủ: " + pl.name + "\n[" + Util.convertMilliseconds(pl.timeCDRDDone) + "]");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendPlayerInfo(Player player) {
        if (player == null) {
            return;
        }
        Message msg;
        try {
            msg = messageSubCommand((byte) 7);
            msg.writer().writeInt((int) player.id);
            if (player.clan != null) {
                msg.writer().writeInt(player.clan.id);
            } else if (player.isCopy) {
                msg.writer().writeInt(-2);
            } else {
                msg.writer().writeInt(-1);
            }
            msg.writer().writeByte(Service.gI().getCurrLevel(player));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(player.typePk);
            msg.writer().writeByte(player.gender);
            msg.writer().writeByte(player.gender);
            msg.writer().writeShort(player.getHead());
            msg.writer().writeUTF(player.name);
            msg.writeHp(Util.CrisGH(player.nPoint.hp));
            msg.writeHp(Util.CrisGH(player.nPoint.hpMax));
            msg.writer().writeShort(player.getBody());
            msg.writer().writeShort(player.getLeg());
            msg.writer().writeByte(player.getFlagBag());
            msg.writer().writeByte(-1);
            msg.writer().writeShort(player.location.x);
            msg.writer().writeShort(player.location.y);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(0);
            sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    //---------------------------DANH HIỆU--------------------------------------
    public void sendDanhHieu(Player player, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            switch (id) {
                case 0:
                    if (player.LastTimeDanhHieu_ThienTu > 0 && player.isUseDanhHieu_ThienTu) {
                        me.writer().writeShort(1029);
                    }
                    break;
                case 1:
                    if (player.LastTimeDanhHieu_2 > 0 && player.isUseDanhHieu_2) {
                        me.writer().writeShort(67);
                    }
                    break;
                case 2:
                    if (player.LastTimeDanhHieu_3 > 0 && player.isUseDanhHieu_3) {
                        me.writer().writeShort(67);
                    }
                    break;
                case 3:
                    if (player.LastTimeDanhHieu_4 > 0 && player.isUseDanhHieu_4) {
                        me.writer().writeShort(67);
                    }
                    break;
                default:
                    break;
            }
            me.writer().writeByte(1);
            me.writer().writeByte(-1);
            me.writer().writeShort(50);
            me.writer().writeByte(-1);
            me.writer().writeByte(-1);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendDanhHieuInfo(Player player, Player p2, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            switch (id) {
                case 0:
                    if (player.LastTimeDanhHieu_ThienTu > 0 && player.isUseDanhHieu_ThienTu) {
                        me.writer().writeShort(1029);
                    }
                    break;
                case 1:
                    if (player.LastTimeDanhHieu_2 > 0 && player.isUseDanhHieu_2) {
                        me.writer().writeShort(67);
                    }
                    break;
                case 2:
                    if (player.LastTimeDanhHieu_3 > 0 && player.isUseDanhHieu_3) {
                        me.writer().writeShort(67);
                    }
                    break;
                case 3:
                    if (player.LastTimeDanhHieu_4 > 0 && player.isUseDanhHieu_4) {
                        me.writer().writeShort(67);
                    }
                    break;
                default:
                    break;
            }
            me.writer().writeByte(0);
            me.writer().writeByte(-1);
            me.writer().writeShort(1);
            me.writer().writeByte(-1);
            p2.sendMessage(me);
            me.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void removeDanhHieu(Player player) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(2);
            me.writer().writeInt((int) player.id);
            player.getSession().sendMessage(me);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();
            if (player.isUseDanhHieu_ThienTu == true && player.LastTimeDanhHieu_ThienTu > 0) {
                Service.getInstance().sendDanhHieu(player, 0);
            }
            if (player.isUseDanhHieu_2 == true && player.LastTimeDanhHieu_2 > 0) {
                Service.getInstance().sendDanhHieu(player, 1);
            }
            if (player.isUseDanhHieu_3 == true && player.LastTimeDanhHieu_3 > 0) {
                Service.getInstance().sendDanhHieu(player, 2);
            }
            if (player.isUseDanhHieu_4 == true && player.LastTimeDanhHieu_4 > 0) {
                Service.getInstance().sendDanhHieu(player, 3);
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    //---------------------------CHÂN MỆNH--------------------------------------
    public void sendChanMenh(Player player, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            switch (id) {
                case 2002:
                    me.writer().writeShort(1018);
                    break;
                case 2003:
                    me.writer().writeShort(1019);
                    break;
                case 2004:
                    me.writer().writeShort(1020);
                    break;
                case 2005:
                    me.writer().writeShort(1021);
                    break;
                case 2006:
                    me.writer().writeShort(1022);
                    break;
                case 2007:
                    me.writer().writeShort(1023);
                    break;
                case 2008:
                    me.writer().writeShort(1024);
                    break;
                case 2009:
                    me.writer().writeShort(1025);
                    break;
                case 2010:
                    me.writer().writeShort(1026);
                    break;
                default:
                    break;
            }
            me.writer().writeByte(0);
            me.writer().writeByte(-1);
            me.writer().writeShort(1);
            me.writer().writeByte(-1);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendChanMenhInfo(Player player, Player p2, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            switch (id) {
                case 2002:
                    me.writer().writeShort(1018);
                    break;
                case 2003:
                    me.writer().writeShort(1019);
                    break;
                case 2004:
                    me.writer().writeShort(1020);
                    break;
                case 2005:
                    me.writer().writeShort(1021);
                    break;
                case 2006:
                    me.writer().writeShort(1022);
                    break;
                case 2007:
                    me.writer().writeShort(1023);
                    break;
                case 2008:
                    me.writer().writeShort(1024);
                    break;
                case 2009:
                    me.writer().writeShort(1025);
                    break;
                case 2010:
                    me.writer().writeShort(1026);
                    break;
                default:
                    break;
            }
            me.writer().writeByte(0);
            me.writer().writeByte(-1);
            me.writer().writeShort(1);
            me.writer().writeByte(-1);
            p2.sendMessage(me);
            me.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void removeChanMenh(Player player) {
        try {
            Message me = new Message(-128);
            me.writer().writeByte(1);
            me.writer().writeInt((int) player.id);
            player.getSession().sendMessage(me);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();

            // Gửi lại nếu đang mặc chân mệnh
            for (Item it : player.inventory.itemsBody) {
                if (it != null && it.isNotNullItem()) {
                    if (it.template.type == 39 && it.template.id >= 2002 && it.template.id <= 2010) {
                        Service.getInstance().sendChanMenh(player, it.template.id);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    //--------------------------------------------------------------------------
    public void showYourNumber(Player player, String Number, String result, String finish, int type) {
        Message msg;
        try {
            msg = new Message(-126);
            msg.writer().writeByte(type); // 1 = RESET GAME | 0 = SHOW CON SỐ CỦA PLAYER
            if (type == 0) {
                msg.writer().writeUTF("Số may mắn bạn chọn là: " + Number);
            } else if (type == 1) {
                msg.writer().writeByte(type);
                msg.writer().writeUTF(result); //
                msg.writer().writeUTF(finish);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void addBoughtSkillAttack(Player player) {
        switch (player.gender) {
            case 0:
                if (!player.BoughtSkill.contains(66)) {
                    player.BoughtSkill.add((int) 66);
                }
                break;
            case 1:
                if (!player.BoughtSkill.contains(79)) {
                    player.BoughtSkill.add((int) 79);
                }
                break;
            case 2:
                if (!player.BoughtSkill.contains(87)) {
                    player.BoughtSkill.add((int) 87);
                }
                break;
            default:
                break;
        }
    }

    public void sendServerMessage(Player player, String text) {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendServerAlert(Player player, String text) {
        Message msg = null;
        try {
            msg = new Message(94);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendDialogMessage(Player pl, String text) {
        Message msg = null;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendDialogMessage(MySession session, String text) {
        Message msg = null;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendBadgesPlayer(Player player, int sec, int idImg) {
        Message msg;
        try {
            msg = new Message(24);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(sec);
            msg.writer().writeShort(idImg);
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void stealMoney(Player pl, int stealMoney) {
        Message msg;
        try {
            msg = new Message(95);
            msg.writer().writeInt(stealMoney);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void ClosePanel(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(7);
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void checkHocSkill_Gender0(Player player) {
        if (player.gender == 0) {
            int[] skillIds = {66, 67, 68, 69, 70, 71, 72, 94, 95, 96, 97, 98, 99, 100, 115, 116, 117, 118, 119, 120, 121, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310,
                311, 312, 313, 488, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 434, 435, 436, 437, 438, 439, 440};

            for (int id : skillIds) {
                if (!player.BoughtSkill.contains(id)) {
                    player.BoughtSkill.add(id);
                }
            }
        }
    }

    public void checkHocSkill_Gender1(Player player) {
        if (player.gender == 1) {
            int[] skillIds = {79, 80, 81, 82, 83, 84, 86, 101, 102, 103, 104, 105, 106, 107, 122, 123, 124, 125, 126, 127, 128, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337,
                338, 339, 340, 341, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 434, 435, 436, 437, 438, 439, 440};

            for (int id : skillIds) {
                if (!player.BoughtSkill.contains(id)) {
                    player.BoughtSkill.add(id);
                }
            }
        }
    }

    public void checkHocSkill_Gender2(Player player) {
        if (player.gender == 2) {
            int[] skillIds = {87, 88, 89, 90, 91, 92, 93, 108, 109, 110, 111, 112, 113, 114, 129, 130, 131, 132, 133, 134, 135, 314, 315, 316, 317, 318, 319, 320, 321, 322, 323,
                324, 325, 326, 327, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 434, 435, 436, 437, 438, 439, 440};

            for (int id : skillIds) {
                if (!player.BoughtSkill.contains(id)) {
                    player.BoughtSkill.add(id);
                }
            }
        }
    }

    public void CallTauPayPay(Player player) {
        try {
            TaskTauPayPay taskTauPayPay = new TaskTauPayPay(player, BossID.TAUPAYPAY, player.zone, (int) (player.nPoint.hpMax), 775, 100);
            taskTauPayPay.nPoint.tlNeDon = 1;
            player.zone.load_Another_To_Me(player);
            TaskService.gI().doneTask(player, ConstTask.TASK_9_1);
        } catch (Exception e) {
        }
    }

    public void CallSaiBaMen(Player player) {
        try {
            if (player.Saibamen != null) {
                player.Saibamen.dispose();
            }
            player.Saibamen = new SaiBaMen(player);
        } catch (Exception e) {
        }
    }

    public void ChangeTabPet(Player player, byte TypeTab) {
        switch (TypeTab) {
            case 0: //Đệ Tử
                if (player.Detu == null) {
                    Service.getInstance().sendThongBao(player, "Bạn chưa có đệ tử để chuyển tab!");
                }
                if (player.Detu != null && player.typeTabPet != TypeTab) {
                    Service.getInstance().sendThongBao(player, "Đã chuyển tab sang Đệ Tử");
                    player.typeTabPet = TypeTab;
                }
                break;
            case 1: // Người Yêu

                break;
            default:
                Service.getInstance().sendThongBao(player, "Chưa hỗ trợ tab loại này!!");
                player.typeTabPet = 0;
                break;
        }
    }

    public void lightSky() {
        try {
            Message msg = new Message(-83);
            msg.writer().writeByte(1);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void nightSky(Player player) {
        try {
            Message msg = new Message(-83);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.zone.map.mapId);
            msg.writer().writeShort(player.zone.map.bgId);
            msg.writer().writeByte(player.zone.zoneId);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeUTF("");
            msg.writer().writeShort(player.location.x);
            msg.writer().writeShort(player.location.y);
            msg.writer().writeByte(1);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendChatPopup(Player player, int idNpc, int avatar, String text, String[] menu) {
        Message msg;
        try {
            msg = new Message(27);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(menu.length);
            for (String msgg : menu) {
                msg.writer().writeUTF(msgg);
                msg.writer().writeShort(123);
            }
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMenuId(Player player, String text, String[][] menu) {
        Message msg;
        try {
            msg = new Message(27);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(menu.length);
            for (String[] m : menu) {
                msg.writer().writeUTF(m[0]);
                msg.writer().writeShort(Short.parseShort(m[1]));
            }
            player.sendMessage(msg);
        } catch (IOException | NumberFormatException e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showAD(Player pl) {
        Message msg;
        try {
            msg = new Message(121);
            msg.writer().writeUTF("https://www.youtube.com/");
            msg.writer().writeUTF("MaiTienDung");
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void resetButton(Player player) {
        Message msg;
        try {
            msg = new Message(47);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendYesNoDlg(Player player, String text, String textY) {
        Message msg;
        try {
            msg = new Message(-98);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(text);
            msg.writer().writeUTF(textY);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendIdHat(Player player, int idHat) {
        Message msg;
        try {
            msg = messageReadOpt((byte) 0);
            msg.writer().writeShort(idHat);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendBanner(Player player, int sec, int idImg) {
        Message msg;
        try {
            msg = messageReadOpt((byte) 2);
            msg.writer().writeInt((int) player.id); //10s send 1 lần
            msg.writer().writeByte(sec); //timeExist 5
            msg.writer().writeShort(idImg);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendIdWater1(Player player, int id) {
        Message msg;
        try {
            msg = messageReadOpt((byte) 3);
            msg.writer().writeShort(id);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMessageServer(Player player, String data) {
        Message msg;
        try {
            msg = messageReadOpt((byte) 4);
            msg.writer().writeUTF(data);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }
    public void sendBossChatVip(String text) {
    Message msg = null;
    try {
        msg = new Message(-93);
        msg.writer().writeUTF(text);
        sendMessAllPlayer(msg);
    } catch (Exception e) {
        Logger.logException(Service.class, e);
    } finally {
        if (msg != null) {
            msg.cleanup();
        }
    }
}

public void sendBossChatVipWithRest(String displayText, long dieTimeMs, int secondsRest) {
    try {
        if (displayText == null || displayText.isEmpty()) {
            return;
        }

        if (dieTimeMs <= 0) {
            dieTimeMs = System.currentTimeMillis();
        }

        if (secondsRest < 0) {
            secondsRest = 0;
        }

        String msg = displayText
                + "|BOSS_DATA"
                + "|die=" + dieTimeMs
                + "|rest=" + secondsRest;

        sendBossChatVip(msg);
    } catch (Exception e) {
        Logger.logException(Service.class, e);
    }
}

}
