package nro.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.dao.PlayerDAO;
import nro.server.Client;

/**
 * Mở <b>thành viên</b> (kích hoạt tài khoản) — ba đường.
 *
 * <ol>
 *   <li><b>Nạp đủ 50K</b> (tổng nạp) — tự mở ngay, không cần làm gì.</li>
 *   <li><b>Online đủ 3 tuần</b> (tổng thời gian online của tài khoản) — tự
 *       mở.</li>
 *   <li><b>Xong nhiệm vụ tiêu diệt Fide</b> — về nhà, chọn "MTV FREE" ở ông
 *       già để mở.</li>
 * </ol>
 *
 * <h2>Đếm thời gian online</h2>
 *
 * <p>Cột {@code account.tong_online_ms}, máy chủ tự thêm. Mỗi phút cộng một
 * phút cho tài khoản <b>chưa là thành viên</b> đang online — đã là thành viên
 * thì không cần đếm nữa, đỡ một câu ghi mỗi phút cho mỗi người. Phần lẻ dưới
 * một phút của mỗi lần vào game không được tính.</p>
 *
 * <h2>Vì sao kiểm tra định kỳ</h2>
 *
 * <p>Tiền nạp đi vào nhiều đường: panel quản trị, trang nạp ghi thẳng vào cơ
 * sở dữ liệu, mã nạp trong game. Móc vào từng đường thì sót một đường là người
 * nạp rồi mà không được mở. Nên cứ mỗi phút đọc lại tổng nạp của những người
 * đang online và chưa là thành viên, rồi mở cho ai đủ điều kiện. Lúc đăng nhập
 * cũng kiểm tra một lần.</p>
 */
public final class ThanhVienService {

    /** Tổng nạp (VND) để tự mở thành viên. */
    public static final long NAP_DE_MO = 50_000L;

    /** Tổng thời gian online để tự mở thành viên: 3 tuần. */
    public static final long ONLINE_DE_MO_MS = 21L * 86_400_000L;

    /** Nhiệm vụ tiêu diệt Fide; bước 3 là đi báo cáo, tức đã diệt xong. */
    private static final int NV_FIDE = 23;
    private static final int NV_FIDE_BUOC_XONG = 3;

    private static final long CHU_KY_MS = 60_000L;

    /** account_id → tổng thời gian online đã đếm (ms). */
    private static final Map<Integer, Long> ONLINE = new ConcurrentHashMap<>();

    private static boolean daThemCot;

    private ThanhVienService() {
    }

    private static synchronized void damBaoCot() {
        if (daThemCot) {
            return;
        }
        try {
            ConnectDB.executeUpdate("ALTER TABLE account"
                    + " ADD COLUMN tong_online_ms BIGINT(20) NOT NULL DEFAULT 0");
        } catch (Exception daCo) {
            // Cot da co san.
        }
        daThemCot = true;
    }

    /** Đọc lại tổng nạp và tổng thời gian online của tài khoản từ cơ sở dữ liệu. */
    private static void docTaiKhoan(Player pl) {
        damBaoCot();
        CrisResultSet rs = null;
        try {
            int id = pl.getSession().userId;
            rs = ConnectDB.executeQuery(
                    "SELECT tongnap, tong_online_ms FROM account WHERE id = ?", id);
            if (rs.next()) {
                pl.getSession().tongnap = (int) Math.min(Integer.MAX_VALUE, rs.getLong("tongnap"));
                ONLINE.put(id, rs.getLong("tong_online_ms"));
            }
        } catch (Exception ex) {
            Logger.logException(ThanhVienService.class, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                    // Dong that bai thi cung khong lam gi duoc them.
                }
            }
        }
    }

    /** Đã xong nhiệm vụ tiêu diệt Fide chưa. */
    public static boolean xongNhiemVuFide(Player pl) {
        return pl != null && pl.playerTask != null && pl.playerTask.taskMain != null
                && (pl.playerTask.taskMain.id > NV_FIDE
                    || (pl.playerTask.taskMain.id == NV_FIDE
                        && pl.playerTask.taskMain.index >= NV_FIDE_BUOC_XONG));
    }

    /** Tổng thời gian online đã đếm của tài khoản (ms). */
    public static long daOnlineMs(Player pl) {
        if (pl == null || pl.getSession() == null) {
            return 0;
        }
        Long v = ONLINE.get(pl.getSession().userId);
        return v == null ? 0 : v;
    }

    /** Tổng thời gian online đã đủ 3 tuần chưa. */
    public static boolean duOnline(Player pl) {
        return daOnlineMs(pl) >= ONLINE_DE_MO_MS;
    }

    /** Tổng nạp đã đủ 50K chưa. */
    public static boolean duNap(Player pl) {
        return pl != null && pl.getSession() != null && pl.getSession().tongnap >= NAP_DE_MO;
    }

    /** Mô tả tiến độ online, ví dụ "120 / 504 giờ". */
    public static String tienDoOnline(Player pl) {
        return (daOnlineMs(pl) / 3_600_000L) + " / " + (ONLINE_DE_MO_MS / 3_600_000L) + " giờ";
    }

    /**
     * Minigame (Tài Xỉu, Đua Vịt, Câu Cá...) chỉ dành cho thành viên.
     *
     * <p>Chặn nick mới tạo hàng loạt vào cược: muốn chơi phải nạp đủ 50K hoặc
     * online đủ 3 tuần. Người chưa là thành viên vẫn mở bảng xem được, chỉ
     * không đặt cược / bắt đầu ván được.</p>
     *
     * @return {@code true} khi được chơi; không thì đã báo lý do cho người chơi
     */
    public static boolean duocChoiMiniGame(Player pl) {
        if (pl == null || pl.getSession() == null) {
            return false;
        }
        if (pl.getSession().actived) {
            return true;
        }
        Service.gI().sendThongBao(pl, "Trò chơi chỉ dành cho thành viên! Mở thành viên: nạp đủ 50K "
                + "hoặc online đủ 3 tuần (" + tienDoOnline(pl) + ").");
        return false;
    }

    /** Mở thành viên và lưu xuống cơ sở dữ liệu. */
    public static void mo(Player pl, String lyDo) {
        if (pl == null || pl.getSession() == null || pl.getSession().actived) {
            return;
        }
        pl.getSession().actived = true;
        PlayerDAO.MuaThanhVien(pl, 0);
        ONLINE.remove(pl.getSession().userId);
        Service.gI().sendMoney(pl);
        Service.gI().sendThongBao(pl, "Bạn đã được mở thành viên (" + lyDo + ")!");
    }

    private static void xetTuDong(Player pl) {
        if (duNap(pl)) {
            mo(pl, "đã nạp đủ 50K");
        } else if (duOnline(pl)) {
            mo(pl, "đã online đủ 3 tuần");
        }
    }

    /** Lúc đăng nhập: đọc số liệu và tự mở nếu đủ (nạp 50K, online 3 tuần). */
    public static void kiemTraTuDong(Player pl) {
        if (pl == null || !pl.isPl() || pl.getSession() == null || pl.getSession().actived) {
            return;
        }
        docTaiKhoan(pl);
        xetTuDong(pl);
    }

    /** Khởi động luồng đếm online và kiểm tra định kỳ. Gọi một lần lúc máy chủ dựng xong. */
    public static void batDau() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CHU_KY_MS);
                    damBaoCot();
                    for (Player pl : Client.gI().getPlayersSnapshot()) {
                        if (pl == null || !pl.isPl() || pl.getSession() == null
                                || pl.getSession().actived) {
                            continue;
                        }
                        ConnectDB.executeUpdate("UPDATE account"
                                + " SET tong_online_ms = tong_online_ms + ? WHERE id = ?",
                                CHU_KY_MS, pl.getSession().userId);
                        // Doc lai: tong nap co the vua doi tu trang nap.
                        docTaiKhoan(pl);
                        xetTuDong(pl);
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    Logger.logException(ThanhVienService.class, e);
                }
            }
        }, "Mo thanh vien");
        t.setDaemon(true);
        t.start();
    }
}
