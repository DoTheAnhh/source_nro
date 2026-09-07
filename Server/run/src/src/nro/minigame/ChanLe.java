package nro.minigame;

import nro.inventory.InventoryService;
import nro.player.Player;
import nro.server.Client;
import nro.services.ChatGlobalService;
import nro.services.Service;
import Utils.Util;
import com.mysql.jdbc.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import jbcd.ConnectDB;
import models.Item.Item;
import models.Item.ItemService;

/**
 * ChanLe – mini game cược CHẴN/LẺ dùng Thỏi Vàng (template 457) Cấu trúc và cú
 * pháp bám sát TaiXiu.java
 */
public class ChanLe implements Runnable {

    // ====== CONFIG ======
    private static final int MAX_BET = 20_000;       // tối đa mỗi lệnh
    private static final short THOI_VANG_TEMPLATE = 457;

    // ====== STATE (tổng theo cửa) ======
    public int goldChan;
    public int goldLe;

    // cờ kết quả ván
    public boolean ketquaChan = false;
    public boolean ketquaLe = false;

    public boolean baotri = false;
    public long lastTimeEnd;

    public List<Player> PlayersChan = new ArrayList<>();
    public List<Player> PlayersLe = new ArrayList<>();

    private static ChanLe instance;

    // số kết quả (0..99)
    public int number;

    // ====== Singleton ======
    public static ChanLe gI() {
        if (instance == null) {
            instance = new ChanLe();
        }
        return instance;
    }

    // ====== API công khai ======
    /**
     * Có đang trong thời gian cho phép đặt cược không (so với lastTimeEnd)
     */
    public boolean isAllowBetting() {
        return System.currentTimeMillis() < this.lastTimeEnd;
    }

    /**
     * Đặt CHẴN
     */
    public void datCuocChan(Player pl, int tienCuoc) {
        if (baotri) {
            Service.getInstance().sendThongBao(pl, "Hệ thống đang bảo trì");
            return;
        }
        if (!isAllowBetting()) {
            Service.getInstance().sendThongBao(pl, "Đang chờ ván mới!");
            return;
        }
        if (tienCuoc <= 0 || tienCuoc > MAX_BET) {
            Service.getInstance().sendThongBao(pl, "Tối đa " + Util.format(MAX_BET) + " Thỏi vàng!");
            return;
        }
        Item tv = InventoryService.gI().findItemBag(pl, THOI_VANG_TEMPLATE);
        if (tv == null || tv.quantity < tienCuoc) {
            Service.getInstance().sendThongBao(pl, "Không đủ Thỏi vàng!");
            return;
        }
        // Trừ thỏi vàng & cập nhật túi
        InventoryService.gI().subQuantityItemsBag(pl, tv, tienCuoc);
        InventoryService.gI().sendItemBag(pl);

        // Cộng tiền cược
        pl.goldChan += tienCuoc;   // của người chơi
        this.goldChan += tienCuoc; // tổng cửa CHẴN
        addPlayerChan(pl);

        Service.getInstance().sendThongBao(pl, "Bạn đã đặt CHẴN: " + Util.format(tienCuoc));
    }

    /**
     * Đặt LẺ
     */
    public void datCuocLe(Player pl, int tienCuoc) {
        if (baotri) {
            Service.getInstance().sendThongBao(pl, "Hệ thống đang bảo trì");
            return;
        }
        if (!isAllowBetting()) {
            Service.getInstance().sendThongBao(pl, "Đang chờ ván mới!");
            return;
        }
        if (tienCuoc <= 0 || tienCuoc > MAX_BET) {
            Service.getInstance().sendThongBao(pl, "Tối đa " + Util.format(MAX_BET) + " Thỏi vàng!");
            return;
        }
        Item tv = InventoryService.gI().findItemBag(pl, THOI_VANG_TEMPLATE);
        if (tv == null || tv.quantity < tienCuoc) {
            Service.getInstance().sendThongBao(pl, "Không đủ Thỏi vàng!");
            return;
        }
        // Trừ thỏi vàng & cập nhật túi
        InventoryService.gI().subQuantityItemsBag(pl, tv, tienCuoc);
        InventoryService.gI().sendItemBag(pl);

        // Cộng tiền cược
        pl.goldLe += tienCuoc;     // của người chơi
        this.goldLe += tienCuoc;   // tổng cửa LẺ
        addPlayerLe(pl);

        Service.getInstance().sendThongBao(pl, "Bạn đã đặt LẺ: " + Util.format(tienCuoc));
    }

    // ====== Quản lý danh sách người chơi ======
    public void addPlayerChan(Player pl) {
        if (pl != null && !PlayersChan.contains(pl)) {
            PlayersChan.add(pl);
        }
    }

    public void addPlayerLe(Player pl) {
        if (pl != null && !PlayersLe.contains(pl)) {
            PlayersLe.add(pl);
        }
    }

    public void removePlayerChan(Player pl) {
        if (pl != null) {
            PlayersChan.remove(pl);
        }
    }

    public void removePlayerLe(Player pl) {
        if (pl != null) {
            PlayersLe.remove(pl);
        }
    }

    // ====== LOOP ======
    @Override
    public void run() {
        // Thiết lập ván đầu nếu cần
        if (lastTimeEnd == 0) {
            lastTimeEnd = System.currentTimeMillis() + 60_000; // 60s / ván
        }
        while (true) {
            try {
                if ((ChanLe.gI().lastTimeEnd - System.currentTimeMillis()) <= 0) {
                    ChanLe cl = ChanLe.gI();

                    int so = Util.nextInt(0, 99);
                    boolean raChan = (so % 2 == 0);
                    boolean raLe = !raChan;

                    // gán kết quả vào biến lớp
                    cl.number = so;
                    cl.ketquaChan = raChan;
                    cl.ketquaLe = raLe;

                    // Thông báo nhịp giống TaiXiu
                    sendToAll(cl, "|2|Đang quay số...");
                    Thread.sleep(1500);
                    sendToAll(cl, "|2|Chuẩn bị có kết quả...");
                    Thread.sleep(1500);

                    // Thông báo kết quả
                    if (raChan) {
                        sendToAll(cl, "|7|Kết quả: " + so + " (CHẴN)");
                        processWinners(cl.PlayersChan, true, so);
                        processLosers(cl.PlayersLe, false, so);
                    } else {
                        sendToAll(cl, "|7|Kết quả: " + so + " (LẺ)");
                        processWinners(cl.PlayersLe, false, so);
                        processLosers(cl.PlayersChan, true, so);
                    }

                    // Reset tiền cược, danh sách
                    for (Player pl : cl.PlayersChan) {
                        if (pl != null) {
                            pl.goldChan = 0;
                        }
                    }
                    for (Player pl : cl.PlayersLe) {
                        if (pl != null) {
                            pl.goldLe = 0;
                        }
                    }
                    cl.goldChan = 0;
                    cl.goldLe = 0;
                    cl.PlayersChan.clear();
                    cl.PlayersLe.clear();

                    // đặt lại timer ván kế
                    cl.lastTimeEnd = System.currentTimeMillis() + 60_000;

                    // ghi session DB + cắt lịch sử
                    addSession(this);
                } else {
                    // tránh ăn CPU
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ====== THÔNG BÁO ======
    private void sendToAll(ChanLe cl, String msg) {
        for (Player pl : cl.PlayersChan) {
            if (pl != null && Client.gI().getPlayerByName(pl.name) != null) {
                Service.getInstance().sendThongBao(pl, msg);
            }
        }
        for (Player pl : cl.PlayersLe) {
            if (pl != null && Client.gI().getPlayerByName(pl.name) != null) {
                Service.getInstance().sendThongBao(pl, msg);
            }
        }
    }

    // ====== TRẢ THƯỞNG / THUA ======
    private void processWinners(List<Player> players, boolean isChan, int so) {
        for (Player pl : players) {
            if (pl != null && Client.gI().getPlayerByName(pl.name) != null) {
                int stake = isChan ? pl.goldChan : pl.goldLe;
                if (stake <= 0) {
                    continue;
                }

                int goldC = stake * 190 / 100; // 1.9x
                Item tv = ItemService.gI().createNewItem((short) 457, goldC);
                tv.addOptionParam(30, 0); // theo cùng cú pháp như TaiXiu (nếu game dùng opt 30)
                InventoryService.gI().addItemBag(pl, tv);
                Service.getInstance().sendThongBao(pl, "|1|Bạn đã chiến thắng!!");
                Service.getInstance().sendThongBao(pl, "|2|Bạn nhận được " + Util.format(goldC) + " Thỏi Vàng");
                ChatGlobalService.gI().chat(pl, pl.name + " thắng Chẵn/Lẻ và nhận " + Util.format(goldC) + " Thỏi Vàng");
                Service.getInstance().sendMoney(pl);
                InventoryService.gI().sendItemBag(pl);
            }
        }
    }

    private void processLosers(List<Player> players, boolean raChan, int so) {
        for (Player pl : players) {
            if (pl != null && Client.gI().getPlayerByName(pl.name) != null) {
                Service.getInstance().sendThongBao(pl, "|7|Thua rồi! Thử lại ván sau nhé!");
            }
        }
    }

    // ====== LỊCH SỬ (DB) ======
    public String getHistoryGame() {
        // Lấy 10 kết quả gần nhất (có thể đổi LIMIT 10 -> 20 nếu muốn)
        final String sql = "SELECT number FROM chan_le ORDER BY id DESC LIMIT 10";

        List<Integer> nums = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                nums.add(rs.getInt(1)); // phần tử 0 là số mới nhất
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (nums.isEmpty()) {
            return "Chưa có lịch sử!";
        }

        int last = nums.get(0); // số vừa quay
        // In “Các số trước” theo thứ tự cũ -> mới (đảo ngược list)
        StringBuilder sb = new StringBuilder();
        sb.append("Các số trước: ");
        for (int i = nums.size() - 1; i >= 0; i--) {
            sb.append(nums.get(i));
            if (i > 0) {
                sb.append(", ");
            }
        }
        
        return sb.toString();
    }

    // ====== RESULT FLAG & SESSION ======
    private boolean isEven; // 0=lẻ, 1=chẵn

    public boolean getResult() {
        return isEven;
    }

    private int id;

    public void setId(int id) {
        this.id = id;
    }

    private boolean addSession(ChanLe chanle) {
        // map kết quả hiện tại vào isEven
        this.isEven = (this.number % 2 == 0);

        try (Connection conn = ConnectDB.getConnection()) {
            // 1) Insert bản ghi mới
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO `chan_le`(`number`, `result`) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, this.number);
                ps.setBoolean(2, this.isEven);

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Lỗi khi add session Chẵn/Lẻ");
                }

                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int genId = gk.getInt(1);
                        chanle.setId(genId);
                    } else {
                        throw new SQLException("Creating session failed, no ID obtained.");
                    }
                }
            }

            // 2) Xoá cũ, giữ 20 bản ghi mới nhất
            try (PreparedStatement deleteOld = conn.prepareStatement(
                    "DELETE FROM chan_le WHERE id NOT IN ("
                    + "SELECT id FROM (SELECT id FROM chan_le ORDER BY id DESC LIMIT 20) AS recent_ids)")) {
                deleteOld.executeUpdate();
            }

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
