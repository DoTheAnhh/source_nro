package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.entity.boss.map.trainingboss.TopKillWhisManager;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Client;
import nro.server.TopServer;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Bảng xếp hạng <b>Thách Đấu Whis</b> và phần thưởng thỏi vàng mỗi ngày.
 *
 * <h2>Thưởng thay cho cộng chỉ số</h2>
 *
 * <p>Trước đây đứng top được cộng thẳng % HP, KI và sức đánh. Nay bỏ hẳn phần
 * đó: đứng top chỉ được <b>thỏi vàng mỗi ngày</b> — hạng 1 được 10, hạng 2–5
 * được 5, hạng 6 trở đi được 1 (mức nào cũng sửa được ở panel).</p>
 *
 * <h2>Dữ liệu nằm ở đâu</h2>
 *
 * <p>Giống top máy đấm, không có bảng riêng. Kỷ lục nằm trong cột
 * {@code player.data_luyentap}: ô {@code [5]} là cấp Whis đã hạ, ô {@code [6]}
 * là thời gian (mili giây). Ngày nhận thưởng gần nhất nằm ở ô {@code [9]} —
 * trường {@code lastRewardTime} vốn có sẵn mà chưa chỗ nào dùng, nên không
 * cần thêm cột nào vào CSDL.</p>
 *
 * <p>Người đang online giữ bản của họ trong bộ nhớ và ghi đè xuống CSDL ở lần
 * lưu kế tiếp, nên mọi thao tác sửa ở đây làm <i>cả hai chỗ</i>.</p>
 */
public final class TopWhisDAO {

    private TopWhisDAO() {
    }

    // =====================================================================
    //  Mức thưởng
    // =====================================================================

    public static final String KHOA_THUONG_TOP1 = "top_whis_thuong_top1";
    public static final String KHOA_THUONG_TOP2_5 = "top_whis_thuong_top2_5";
    public static final String KHOA_THUONG_TOP6 = "top_whis_thuong_top6";

    public static final long MAC_DINH_TOP1 = 10;
    public static final long MAC_DINH_TOP2_5 = 5;
    public static final long MAC_DINH_TOP6 = 1;

    /** Mã vật phẩm thỏi vàng. */
    private static final short ID_THOI_VANG = 457;

    /** Số thỏi vàng mỗi ngày cho một hạng; hạng không hợp lệ thì 0. */
    public static int soThoiTheoHang(int hang) {
        long n;
        if (hang < 1) {
            return 0;
        } else if (hang == 1) {
            n = ConfigDAO.num(KHOA_THUONG_TOP1, MAC_DINH_TOP1);
        } else if (hang <= 5) {
            n = ConfigDAO.num(KHOA_THUONG_TOP2_5, MAC_DINH_TOP2_5);
        } else {
            n = ConfigDAO.num(KHOA_THUONG_TOP6, MAC_DINH_TOP6);
        }
        return (int) Math.max(0, Math.min(n, 30_000));
    }

    /** Hạng hiện tại của nhân vật, hoặc -1 nếu không nằm trong top. */
    public static int hangCua(Player player) {
        List<Player> list = TopKillWhisManager.getInstance().getList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == player.id) {
                return i + 1;
            }
        }
        return -1;
    }

    static long homNay() {
        java.time.LocalDate n = java.time.LocalDate.now();
        return n.getYear() * 10000L + n.getMonthValue() * 100L + n.getDayOfMonth();
    }

    /**
     * Ngày đã nhận thưởng gần nhất, dạng {@code yyyyMMdd}.
     *
     * <p>Ô {@code lastRewardTime} có từ trước và có thể đang mang một mốc
     * mili giây cũ. Mốc ấy lớn hơn mọi {@code yyyyMMdd}, đọc thẳng thì nhân vật
     * bị coi là "đã nhận" vĩnh viễn — nên số nào không giống một ngày thì coi
     * như chưa nhận bao giờ.</p>
     */
    private static long ngayDaNhan(Player player) {
        long v = player.traning.getLastRewardTime();
        return (v >= 20000101L && v <= 99991231L) ? v : 0;
    }

    /** Lần báo "túi đầy" gần nhất theo nhân vật, để không báo mỗi giây. */
    private static final Map<Long, Long> LAN_BAO_TUI_DAY = new ConcurrentHashMap<>();

    /**
     * Phát thưởng hôm nay nếu nhân vật đang đứng top và chưa nhận.
     *
     * <p>Gọi từ vòng cập nhật của nhân vật. Kiểm ngày trước tiên nên với người
     * đã nhận thì gần như không tốn gì.</p>
     *
     * <p>Không nằm trong top thì <b>không</b> đánh dấu đã nhận: lọt top vào buổi
     * chiều vẫn nhận được trong ngày. Thưởng tính theo hạng <b>lúc nhận</b>.</p>
     */
    public static void phatNeuCan(Player player) {
        if (player == null || !player.isPl() || player.traning == null
                || player.iDMark == null || !player.iDMark.isLoadedAllDataPlayer()) {
            return;
        }
        long nay = homNay();
        if (ngayDaNhan(player) >= nay) {
            return;
        }
        int hang = hangCua(player);
        int so = soThoiTheoHang(hang);
        if (so <= 0) {
            return;
        }
        Item tv = ItemService.gI().createNewItem(ID_THOI_VANG, so);
        if (!InventoryService.gI().addItemBag(player, tv)) {
            long bay = System.currentTimeMillis();
            Long truoc = LAN_BAO_TUI_DAY.get(player.id);
            if (truoc == null || bay - truoc > 5 * 60_000L) {
                LAN_BAO_TUI_DAY.put(player.id, bay);
                Service.gI().sendThongBao(player, "Hành trang đầy, chưa nhận được "
                        + so + " thỏi vàng thưởng Top Whis hôm nay.");
            }
            return;
        }
        LAN_BAO_TUI_DAY.remove(player.id);
        player.traning.setLastRewardTime(nay);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendThongBao(player, "Thưởng Top Whis hạng " + hang
                + " hôm nay: " + so + " thỏi vàng.");
    }

    // =====================================================================
    //  Bảng quản lý
    // =====================================================================

    /** Một dòng của bảng xếp hạng. */
    public static final class Dong {

        public long id;
        public String ten = "";
        public int level;
        /** Mili giây. */
        public long thoiGian;
        public boolean daNhanHomNay;
        public boolean online;
    }

    /** Đọc bảng xếp hạng, đúng thứ tự dùng để phát thưởng. */
    public static List<Dong> danhSach(int gioiHan) {
        List<Dong> ra = new ArrayList<>();
        CrisResultSet rs = null;
        long nay = homNay();
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT player.id AS pid, name,"
                    + " CAST(JSON_UNQUOTE(JSON_EXTRACT(data_luyentap, '$[5]')) AS UNSIGNED) AS lv,"
                    + " CAST(JSON_UNQUOTE(JSON_EXTRACT(data_luyentap, '$[6]')) AS UNSIGNED) AS tg,"
                    + " CAST(JSON_UNQUOTE(JSON_EXTRACT(data_luyentap, '$[9]')) AS UNSIGNED) AS nhan"
                    + " FROM player"
                    + " INNER JOIN account ON account.id = player.account_id"
                    + " WHERE account.ban = 0"
                    + " HAVING lv > 0"
                    + " ORDER BY lv DESC, tg ASC"
                    + " LIMIT " + Math.max(1, Math.min(gioiHan, 1000)));
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getLong("pid");
                d.ten = rs.getString("name");
                d.level = rs.getInt("lv");
                d.thoiGian = rs.getLong("tg");
                d.daNhanHomNay = rs.getLong("nhan") == nay;
                Player pl = Client.gI().getPlayerByID(d.id);
                d.online = pl != null;
                if (pl != null && pl.traning != null) {
                    // Nguoi online: bo nho moi la ban dung, CSDL co the con cu.
                    d.level = pl.traning.getTop();
                    d.thoiGian = pl.traning.getTime();
                    d.daNhanHomNay = ngayDaNhan(pl) >= nay;
                }
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(TopWhisDAO.class, ex, "Lỗi đọc top Whis");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Sửa kỷ lục của một nhân vật (cả bộ nhớ lẫn CSDL). */
    public static String sua(long playerId, int level, long thoiGianMs) {
        if (level < 0 || thoiGianMs < 0) {
            return "Kỷ lục không được âm.";
        }
        if (thoiGianMs > Integer.MAX_VALUE) {
            return "Thời gian quá lớn.";
        }
        try {
            ConnectDB.executeUpdate(
                    "UPDATE player SET data_luyentap = JSON_SET(data_luyentap, '$[5]', ?, '$[6]', ?)"
                    + " WHERE id = ? AND JSON_LENGTH(data_luyentap) > 6",
                    level, thoiGianMs, playerId);
            Player pl = Client.gI().getPlayerByID(playerId);
            if (pl != null && pl.traning != null) {
                pl.traning.setTop(level);
                pl.traning.setTime((int) thoiGianMs);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(TopWhisDAO.class, ex, "Lỗi sửa kỷ lục Whis");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Xoá sạch bảng xếp hạng Whis.
     *
     * @return số nhân vật bị đặt lại, hoặc {@code -1} nếu hỏng
     */
    public static int resetTatCa() {
        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE player SET data_luyentap = JSON_SET(data_luyentap, '$[5]', 0, '$[6]', 0)"
                    + " WHERE JSON_LENGTH(data_luyentap) > 6");
            for (Player pl : Client.gI().getPlayersSnapshot()) {
                if (pl != null && pl.traning != null) {
                    pl.traning.setTop(0);
                    pl.traning.setTime(0);
                }
            }
            Logger.success("CONFIG", "Đã xoá bảng xếp hạng Whis (" + n + " nhân vật)");
            return n;
        } catch (Exception ex) {
            Logger.logException(TopWhisDAO.class, ex, "Lỗi xoá top Whis");
            return -1;
        }
    }

    /**
     * Nạp lại hai bản sao bảng xếp hạng trong bộ nhớ.
     *
     * <p>Một bản dùng để phát thưởng, một bản cho NPC hiện "Top 10". Không nạp
     * lại thì sửa xong phải chờ tới lượt tự làm mới 30 giây mới thấy.</p>
     */
    public static void napLaiBoNho() {
        try {
            TopKillWhisManager.getInstance().load();
            TopServer.LoadingTop();
        } catch (Exception ex) {
            Logger.logException(TopWhisDAO.class, ex, "Lỗi nạp lại top Whis");
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
