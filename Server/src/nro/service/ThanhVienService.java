package nro.service;

import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.repository.dao.AccountDAO;
import nro.repository.dao.PlayerDAO;
import nro.server.Client;

/**
 * Mở <b>thành viên</b> (kích hoạt tài khoản) — ba đường.
 *
 * <ol>
 *   <li><b>Nạp đủ 50K</b> (tổng nạp) — tự mở ngay, không cần làm gì.</li>
 *   <li><b>Chơi đủ 1 tháng</b> (tài khoản tạo được 30 ngày) — tự mở.</li>
 *   <li><b>Xong nhiệm vụ tiêu diệt Fide</b> — về nhà, chọn "MTV FREE" ở ông
 *       già để mở.</li>
 * </ol>
 *
 * <h2>Vì sao có luồng kiểm tra định kỳ</h2>
 *
 * <p>Tiền nạp đi vào nhiều đường: panel quản trị, trang nạp ghi thẳng vào cơ
 * sở dữ liệu, mã nạp trong game. Móc vào từng đường thì sót một đường là người
 * nạp rồi mà không được mở. Nên cứ mỗi phút đọc lại tổng nạp của những người
 * <b>đang online và chưa là thành viên</b> — nhóm nhỏ, câu hỏi nhẹ — rồi mở cho ai
 * đủ điều kiện. Lúc đăng nhập cũng kiểm tra một lần.</p>
 */
public final class ThanhVienService {

    /** Tổng nạp (VND) để tự mở thành viên. */
    public static final long NAP_DE_MO = 50_000L;

    /** Tuổi tài khoản (ngày) để tự mở thành viên. */
    public static final int NGAY_DE_MO = 30;

    /** Nhiệm vụ tiêu diệt Fide; bước 3 là đi báo cáo, tức đã diệt xong. */
    private static final int NV_FIDE = 23;
    private static final int NV_FIDE_BUOC_XONG = 3;

    private static final long CHU_KY_MS = 60_000L;

    private ThanhVienService() {
    }

    /** Đã xong nhiệm vụ tiêu diệt Fide chưa. */
    public static boolean xongNhiemVuFide(Player pl) {
        return pl != null && pl.playerTask != null && pl.playerTask.taskMain != null
                && (pl.playerTask.taskMain.id > NV_FIDE
                    || (pl.playerTask.taskMain.id == NV_FIDE
                        && pl.playerTask.taskMain.index >= NV_FIDE_BUOC_XONG));
    }

    /** Tài khoản đã tạo đủ 30 ngày chưa. */
    public static boolean duMotThang(Player pl) {
        if (pl == null || pl.getSession() == null) {
            return false;
        }
        long tao = pl.getSession().timeCreateAcount;
        return tao > 0 && System.currentTimeMillis() - tao >= NGAY_DE_MO * 86_400_000L;
    }

    /** Tổng nạp đã đủ 50K chưa. */
    public static boolean duNap(Player pl) {
        return pl != null && pl.getSession() != null && pl.getSession().tongnap >= NAP_DE_MO;
    }

    /** Mở thành viên và lưu xuống cơ sở dữ liệu. */
    public static void mo(Player pl, String lyDo) {
        if (pl == null || pl.getSession() == null || pl.getSession().actived) {
            return;
        }
        pl.getSession().actived = true;
        PlayerDAO.MuaThanhVien(pl, 0);
        Service.gI().sendMoney(pl);
        Service.gI().sendThongBao(pl, "Bạn đã được mở thành viên (" + lyDo + ")!");
    }

    /** Tự mở nếu đủ một trong hai điều kiện tự động (nạp 50K, đủ 1 tháng). */
    public static void kiemTraTuDong(Player pl) {
        if (pl == null || !pl.isPl() || pl.getSession() == null || pl.getSession().actived) {
            return;
        }
        if (duNap(pl)) {
            mo(pl, "đã nạp đủ 50K");
        } else if (duMotThang(pl)) {
            mo(pl, "đã chơi đủ 1 tháng");
        }
    }

    /** Khởi động luồng kiểm tra định kỳ. Gọi một lần lúc máy chủ dựng xong. */
    public static void batDau() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CHU_KY_MS);
                    for (Player pl : Client.gI().getPlayersSnapshot()) {
                        if (pl == null || !pl.isPl() || pl.getSession() == null
                                || pl.getSession().actived) {
                            continue;
                        }
                        // Doc lai tong nap: tien co the vua vao tu trang nap.
                        AccountDAO.Money tien = AccountDAO.load(pl.getSession().userId);
                        if (tien != null) {
                            pl.getSession().tongnap = (int) Math.min(Integer.MAX_VALUE, tien.tongNap);
                        }
                        kiemTraTuDong(pl);
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
