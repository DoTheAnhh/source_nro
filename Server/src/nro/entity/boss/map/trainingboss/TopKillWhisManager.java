package nro.entity.boss.map.trainingboss;

import nro.entity.player.Player;
import nro.repository.ConnectDB;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author DoTheAnh
 */

public class TopKillWhisManager {

    private static final TopKillWhisManager INSTANCE = new TopKillWhisManager();
    /**
     * Danh sach hien hanh. Nap lai thi dung danh sach MOI roi moi thay vao.
     *
     * <p>Ban truoc xoa danh sach cu roi do dan vao, ma vong cap nhat cua nhan
     * vat doc no ngay cung luc (phat thuong, hieu ung top) — dung luc dang do
     * thi thay danh sach rong hay thieu nua, va duyet theo chi so co the vang
     * loi. Thay nguyen khoi thi ben doc luon thay mot danh sach tron ven.</p>
     */
    private volatile List<Player> list = new ArrayList<>();

    public static TopKillWhisManager getInstance() {
        return INSTANCE;
    }

    public List<Player> getList() {
        return list;
    }

    public void load() {
        List<Player> moi = new ArrayList<>();

        // Truy vấn SQL đã sửa, lấy từ `data_luyentap` thay vì `levelKillWhis`
        String sql = "SELECT player.id, player.name, player.head, player.gender, "
                + "       CAST(JSON_UNQUOTE(JSON_EXTRACT(data_luyentap, '$[5]')) AS UNSIGNED) AS levelKillWhis, "
                + "       CAST(JSON_UNQUOTE(JSON_EXTRACT(data_luyentap, '$[6]')) AS UNSIGNED) AS timeKillWhis, "
                + "       player.lastTimeLoginGame, player.data_point " // Thêm "player." để tránh lỗi
                + "FROM player "
                + "INNER JOIN account ON account.id = player.account_id "
                + "WHERE account.ban = 0 "
                + "AND CAST(JSON_UNQUOTE(JSON_EXTRACT(data_luyentap, '$[5]')) AS UNSIGNED) > 0 "
                + "ORDER BY levelKillWhis DESC, timeKillWhis ASC "
                + "LIMIT 100;";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Player player = extractPlayerFromResultSet(rs);
                moi.add(player);
            }
            list = moi;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tải danh sách TopKillWhis!");
        }
    }

    private Player extractPlayerFromResultSet(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.id = rs.getInt("id");
        player.name = rs.getString("name");
        player.head = rs.getShort("head");
        player.gender = rs.getByte("gender");
        player.lastimelogin = rs.getTimestamp("lastTimeLoginGame");

        extractDataPoint(rs.getString("data_point"), player);

        return player;
    }

    private void extractDataPoint(String dataPoint, Player player) {
        if (dataPoint == null || dataPoint.isEmpty()) {
            player.nPoint.power = 0; // Nếu dữ liệu bị lỗi, đặt mặc định
            return;
        }

        try {
            JSONArray dataArray = (JSONArray) JSONValue.parse(dataPoint);
            if (dataArray != null && dataArray.size() > 11) {
                player.nPoint.power = Long.parseLong(dataArray.get(11).toString());
            } else {
                player.nPoint.power = 0;
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Lỗi khi xử lý `data_point` cho người chơi: " + player.name);
            player.nPoint.power = 0; // Tránh lỗi null
        }
    }
}