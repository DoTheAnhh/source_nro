package nro.repository.dao;

import nro.core.util.TimeUtil;
import java.sql.*;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nhật ký giao dịch giữa hai người chơi.
 *
 * <p><b>Đây là bằng chứng để điều tra khi nghi ngờ có lỗi nhân đồ.</b>
 * {@code deleteHistory()} được gọi lúc khởi động server ({@code ServerManager.init()})
 * — cần biết rõ nó xoá theo tiêu chí gì, vì xoá nhầm là mất dấu vết điều tra.</p>
 */
public class HistoryTransactionDAO {

    public static class TransactionLog {

        public String player1NameRecord;

        public String getPlayer1namerecord() {
            return this.player1NameRecord;
        }

        public void setPlayer1namerecord(String player1NameRecord) {
            this.player1NameRecord = player1NameRecord;
        }

        public String player2NameRecord;

        public String getPlayer2namerecord() {
            return this.player2NameRecord;
        }

        public void setPlayer2namerecord(String player2NameRecord) {
            this.player2NameRecord = player2NameRecord;
        }

        public String itemsExchangedByPlayer1;

        public String getItemsexchangedbyplayer1() {
            return this.itemsExchangedByPlayer1;
        }

        public void setItemsexchangedbyplayer1(String itemsExchangedByPlayer1) {
            this.itemsExchangedByPlayer1 = itemsExchangedByPlayer1;
        }

        public String itemsExchangedByPlayer2;

        public String getItemsexchangedbyplayer2() {
            return this.itemsExchangedByPlayer2;
        }

        public void setItemsexchangedbyplayer2(String itemsExchangedByPlayer2) {
            this.itemsExchangedByPlayer2 = itemsExchangedByPlayer2;
        }

        public Timestamp transactionTime;

        public Timestamp getTransactiontime() {
            return this.transactionTime;
        }

        public void setTransactiontime(Timestamp transactionTime) {
            this.transactionTime = transactionTime;
        }

        public TransactionLog(String p1Name, String p2Name, String p1Items, String p2Items, Timestamp time) {
            this.player1NameRecord = p1Name;
            this.player2NameRecord = p2Name;
            this.itemsExchangedByPlayer1 = p1Items;
            this.itemsExchangedByPlayer2 = p2Items;
            this.transactionTime = time;
        }
    }

    public static void insert(Player pl1, Player pl2,
            int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After, List<Item> bag2After,
            long gold1Before, long gold2Before, long gold1After, long gold2After) {

        String player1 = pl1.name + " (" + pl1.id + ")";
        String player2 = pl2.name + " (" + pl2.id + ")";
        String itemPlayer1 = formatItemsWithGold(goldP1, itemP1);
        String itemPlayer2 = formatItemsWithGold(goldP2, itemP2);

        String beforeTran1 = formatBagItems(bag1Before);
        String beforeTran2 = formatBagItems(bag2Before);
        String afterTran1 = formatBagItems(bag1After);
        String afterTran2 = formatBagItems(bag2After);

        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO history_transaction "
                    + "(player_1, player_2, item_player_1, item_player_2, bag_1_before_tran, bag_2_before_tran, bag_1_after_tran, bag_2_after_tran, time_tran) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    player1, player2, itemPlayer1, itemPlayer2,
                    beforeTran1, beforeTran2, afterTran1, afterTran2,
                    new Timestamp(System.currentTimeMillis())
            );
        } catch (Exception ex) {
            System.err.println("Lỗi khi chèn lịch sử giao dịch: " + ex.getMessage());
        }
    }

    public static List<TransactionLog> getHistoryForPlayer(Player player, int limit) {
        List<TransactionLog> history = new ArrayList<>();
        String playerIdentifier = player.name + " (" + player.id + ")";
        String query = "SELECT player_1, player_2, item_player_1, item_player_2, time_tran "
                + "FROM history_transaction "
                + "WHERE player_1 = ? OR player_2 = ? "
                + "ORDER BY time_tran DESC LIMIT ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerIdentifier);
            ps.setString(2, playerIdentifier);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new TransactionLog(
                            rs.getString("player_1"),
                            rs.getString("player_2"),
                            rs.getString("item_player_1"),
                            rs.getString("item_player_2"),
                            rs.getTimestamp("time_tran")
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy lịch sử giao dịch cho người chơi " + player.name + ": " + e.getMessage());
        }

        return history;
    }

    private static String formatItemsWithGold(int gold, List<Item> items) {
        StringBuilder sb = new StringBuilder();

        Map<Integer, Integer> itemCountMap = new HashMap<>();
        Map<Integer, String> itemNameMap = new HashMap<>();

        for (Item item : items) {
            if (item.isNotNullItem()) {
                int itemId = item.template.id;
                itemCountMap.put(itemId, itemCountMap.getOrDefault(itemId, 0) + item.quantityGD);
                itemNameMap.putIfAbsent(itemId, item.template.name);
            }
        }

        boolean hasItems = !itemCountMap.isEmpty();

        if (gold > 0 || hasItems) {
            sb.append("Gold: ").append(gold);
        }

        for (Map.Entry<Integer, Integer> entry : itemCountMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(itemNameMap.get(entry.getKey()))
                    .append(" (").append(entry.getValue()).append(")");
        }

        return sb.toString();
    }

    private static String formatBagItems(List<Item> items) {
        StringBuilder sb = new StringBuilder();

        for (Item item : items) {
            if (item.isNotNullItem()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(item.template.name)
                        .append(" (")
                        .append(item.quantity)
                        .append(")");
            }
        }

        return sb.toString();
    }

    /**
     * Dọn nhật ký giao dịch cũ. Chạy một lần lúc khởi động máy chủ.
     *
     * <p>Số ngày giữ lấy từ {@code panel_config.giu_lich_su_gd_ngay}; trước đây
     * viết cứng là 3, nên tab Lịch Sử Giao Dịch không bao giờ xem được quá ba
     * ngày mà không ai biết vì sao. Đặt 0 thì <b>không xoá gì cả</b>.</p>
     */
    public static void deleteHistory() {
        long soNgay = ConfigDAO.num(ConfigDAO.GIU_LICH_SU_GD_NGAY);
        if (soNgay <= 0) {
            return;
        }
        PreparedStatement ps = null;

        try (Connection con = ConnectDB.getConnection()) {
            ps = con.prepareStatement("DELETE FROM history_transaction WHERE time_tran < ?");
            // KHONG dung TimeUtil.getTimeBeforeCurrent: no nhan int, ma 25 ngay
            // da la 2,16 ty ms — tran int. Tran thi moc tinh ra sai hoan toan va
            // lenh DELETE se xoa nham. Tu tinh bang long.
            long moc = System.currentTimeMillis() - soNgay * 24L * 60L * 60L * 1000L;
            ps.setTimestamp(1, new Timestamp(moc));
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa lịch sử giao dịch cũ: " + e.getMessage());
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi đóng PreparedStatement trong deleteHistory: " + ex.getMessage());
                }
            }
        }
    }

    // =====================================================================
    //  Tra cứu cho panel
    // =====================================================================

    /** Một lượt giao dịch như panel nhìn thấy. */
    public static final class Dong {

        public int id;
        /** Nguyên văn cột, dạng {@code "Tên (id)"}. */
        public String nguoi1;
        public String nguoi2;
        public String choDi1;
        public String choDi2;
        public String tuiTruoc1;
        public String tuiTruoc2;
        public String tuiSau1;
        public String tuiSau2;
        public java.sql.Timestamp luc;

        /** Phần tên, bỏ {@code " (id)"} ở cuối. */
        public static String chiTen(String raw) {
            if (raw == null) {
                return "";
            }
            int k = raw.lastIndexOf(" (");
            return k > 0 ? raw.substring(0, k) : raw;
        }
    }

    /** Điều kiện lọc của tab Lịch Sử Giao Dịch. */
    public static final class Loc {

        /** Mốc đầu, có thể {@code null}. */
        public java.sql.Timestamp tu;
        /** Mốc cuối, có thể {@code null}. */
        public java.sql.Timestamp den;
        /** Tên người chơi — khớp một phần, ở <b>bất kỳ bên nào</b>. */
        public String nguoi;
        /** Tên hoặc id vật phẩm — khớp một phần trong phần hai bên trao đi. */
        public String vatPham;
        /** Chỉ lấy lượt có vàng đổi tay. */
        public boolean chiCoVang;
        /** Chỉ lấy lượt <b>một chiều</b> — một bên đưa, bên kia không đưa gì. */
        public boolean chiMotChieu;
        public int gioiHan = 500;
    }

    /**
     * Tra nhật ký giao dịch theo bộ lọc.
     *
     * <p>Lọc <b>trong câu SQL</b> chứ không đọc hết rồi lọc bằng Java: bảng này
     * có thể lên hàng trăm nghìn dòng, kéo hết về rồi bỏ đi là vừa chậm vừa tốn
     * bộ nhớ của tiến trình đang chạy máy chủ.</p>
     *
     * <p>Riêng <b>một chiều</b> phải lọc bằng Java: điều kiện là "một bên rỗng,
     * bên kia không" mà hai cột đó là {@code text} chứa cả vàng lẫn vật phẩm,
     * không có cách nào hỏi SQL cho gọn và đúng.</p>
     */
    public static List<Dong> tim(Loc f) {
        List<Dong> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, player_1, player_2, item_player_1, item_player_2,"
                + " bag_1_before_tran, bag_2_before_tran, bag_1_after_tran,"
                + " bag_2_after_tran, time_tran FROM history_transaction WHERE 1 = 1");
        List<Object> ts = new ArrayList<>();
        if (f.tu != null) {
            sql.append(" AND time_tran >= ?");
            ts.add(f.tu);
        }
        if (f.den != null) {
            sql.append(" AND time_tran <= ?");
            ts.add(f.den);
        }
        if (f.nguoi != null && !f.nguoi.trim().isEmpty()) {
            sql.append(" AND (player_1 LIKE ? OR player_2 LIKE ?)");
            String q = "%" + f.nguoi.trim() + "%";
            ts.add(q);
            ts.add(q);
        }
        if (f.vatPham != null && !f.vatPham.trim().isEmpty()) {
            sql.append(" AND (item_player_1 LIKE ? OR item_player_2 LIKE ?)");
            String q = "%" + f.vatPham.trim() + "%";
            ts.add(q);
            ts.add(q);
        }
        if (f.chiCoVang) {
            sql.append(" AND (item_player_1 LIKE '%vàng%' OR item_player_2 LIKE '%vàng%')");
        }
        sql.append(" ORDER BY time_tran DESC, id DESC LIMIT ?");
        ts.add(Math.max(1, f.gioiHan));

        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(sql.toString(), ts.toArray());
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getInt("id");
                d.nguoi1 = rs.getString("player_1");
                d.nguoi2 = rs.getString("player_2");
                d.choDi1 = rs.getString("item_player_1");
                d.choDi2 = rs.getString("item_player_2");
                d.tuiTruoc1 = rs.getString("bag_1_before_tran");
                d.tuiTruoc2 = rs.getString("bag_2_before_tran");
                d.tuiSau1 = rs.getString("bag_1_after_tran");
                d.tuiSau2 = rs.getString("bag_2_after_tran");
                d.luc = rs.getTimestamp("time_tran");
                if (f.chiMotChieu && !motChieu(d)) {
                    continue;
                }
                out.add(d);
            }
        } catch (Exception ex) {
            nro.core.log.Logger.logException(HistoryTransactionDAO.class, ex,
                    "Lỗi tra nhật ký giao dịch");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
        return out;
    }

    /**
     * {@code true} nếu chỉ một bên đưa gì đó, bên kia không đưa gì.
     *
     * <p>Đây là dấu hiệu đáng nhìn khi soi lỗi nhân đồ hoặc bán ngoài — không
     * phải bằng chứng, nhưng là chỗ nên xem trước.</p>
     */
    public static boolean motChieu(Dong d) {
        boolean a = coGiTri(d.choDi1);
        boolean b = coGiTri(d.choDi2);
        return a != b;
    }

    private static boolean coGiTri(String s) {
        return s != null && !s.trim().isEmpty() && !"không có".equalsIgnoreCase(s.trim());
    }

    /** Tổng số dòng đang có trong bảng. */
    public static int demTatCa() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) c FROM history_transaction");
            if (rs.next()) {
                return rs.getInt("c");
            }
        } catch (Exception ex) {
            nro.core.log.Logger.logException(HistoryTransactionDAO.class, ex,
                    "Lỗi đếm nhật ký giao dịch");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
        return 0;
    }

    /** Mốc thời gian cũ nhất còn giữ, hoặc {@code null} nếu bảng trống. */
    public static java.sql.Timestamp cuNhat() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MIN(time_tran) m FROM history_transaction");
            if (rs.next()) {
                return rs.getTimestamp("m");
            }
        } catch (Exception ex) {
            // Bang trong -> khong co moc nao, khong phai loi.
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}
