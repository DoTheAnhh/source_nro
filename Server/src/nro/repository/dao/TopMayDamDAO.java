package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Client;

/**
 * Quản lý <b>bảng xếp hạng máy đo sức mạnh</b> — Đòn Mạnh Nhất và Sát Thương
 * 30 Giây.
 *
 * <h2>Dữ liệu nằm ở đâu</h2>
 *
 * <p>Không có bảng xếp hạng riêng. Kỷ lục nằm ngay trên từng nhân vật, ở cột
 * {@code player.data_may_dam} — một mảng JSON, ô {@code [0]} là đòn mạnh nhất,
 * ô {@code [1]} là tổng sát thương của lượt 30 giây cao nhất. Bảng xếp hạng chỉ
 * là một câu {@code ORDER BY} trên cột ấy.</p>
 *
 * <p>Hệ quả cần nhớ: <b>người đang online giữ bản của họ trong bộ nhớ</b> và ghi
 * đè xuống CSDL ở lần lưu kế tiếp. Nên mọi thao tác sửa hay xoá ở đây đều phải
 * làm <i>cả hai chỗ</i> — chỉ chạy câu SQL thôi thì mấy phút sau kỷ lục cũ quay
 * lại y nguyên, và không có gì nói vì sao.</p>
 */
public final class TopMayDamDAO {

    private TopMayDamDAO() {
    }

    /** Một dòng của bảng xếp hạng. */
    public static final class Dong {

        public long id;
        public String ten = "";
        public long donManh;
        public long dame30s;
        public boolean online;
    }

    /**
     * Đọc bảng xếp hạng, xếp giảm dần theo một trong hai cột.
     *
     * @param theoDonManh {@code true} xếp theo đòn mạnh nhất, ngược lại theo
     *                    sát thương 30 giây
     * @param gioiHan     lấy tối đa bấy nhiêu dòng
     */
    public static List<Dong> danhSach(boolean theoDonManh, int gioiHan) {
        List<Dong> ra = new ArrayList<>();
        String cot = theoDonManh ? "don_manh" : "dame_30s";
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT player.id AS pid, name,"
                    + " CAST(JSON_UNQUOTE(JSON_EXTRACT(data_may_dam, '$[0]')) AS UNSIGNED) AS don_manh,"
                    + " CAST(JSON_UNQUOTE(JSON_EXTRACT(data_may_dam, '$[1]')) AS UNSIGNED) AS dame_30s"
                    + " FROM player"
                    + " INNER JOIN account ON account.id = player.account_id"
                    + " WHERE account.ban = 0"
                    + " HAVING " + cot + " > 0"
                    + " ORDER BY " + cot + " DESC"
                    + " LIMIT " + Math.max(1, Math.min(gioiHan, 1000)));
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getLong("pid");
                d.ten = rs.getString("name");
                d.donManh = rs.getLong("don_manh");
                d.dame30s = rs.getLong("dame_30s");
                d.online = Client.gI().getPlayerByID(d.id) != null;
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(TopMayDamDAO.class, ex, "Lỗi đọc top máy đấm");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /**
     * Đặt lại kỷ lục của <b>một</b> nhân vật.
     *
     * <p>Ghi cả bộ nhớ lẫn CSDL — xem chú thích của lớp để biết vì sao thiếu một
     * bên là công cốc.</p>
     */
    public static String sua(long playerId, long donManh, long dame30s) {
        if (donManh < 0 || dame30s < 0) {
            return "Kỷ lục không được âm.";
        }
        try {
            ConnectDB.executeUpdate(
                    "UPDATE player SET data_may_dam = JSON_ARRAY(?, ?, 0) WHERE id = ?",
                    donManh, dame30s, playerId);
            Player pl = Client.gI().getPlayerByID(playerId);
            if (pl != null) {
                pl.donManhNhat = donManh;
                pl.dame30s = dame30s;
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(TopMayDamDAO.class, ex, "Lỗi sửa kỷ lục máy đấm");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Xoá sạch bảng xếp hạng.
     *
     * @return số nhân vật bị đặt lại, hoặc {@code -1} nếu hỏng
     */
    public static int resetTatCa() {
        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE player SET data_may_dam = JSON_ARRAY(0, 0, 0)");
            // Nguoi dang online giu ban cua ho trong bo nho va se ghi de xuong
            // CSDL o lan luu ke tiep. Khong xoa o day thi vai phut sau ky luc cu
            // quay lai y nguyen.
            for (Player pl : Client.gI().getPlayersSnapshot()) {
                if (pl != null) {
                    pl.donManhNhat = 0;
                    pl.dame30s = 0;
                }
            }
            Logger.success("CONFIG", "Đã xoá bảng xếp hạng máy đấm (" + n + " nhân vật)");
            return n;
        } catch (Exception ex) {
            Logger.logException(TopMayDamDAO.class, ex, "Lỗi xoá top máy đấm");
            return -1;
        }
    }

    // =====================================================================
    //  Tự xoá theo ngày
    // =====================================================================

    /** Bật/tắt việc tự xoá bảng xếp hạng mỗi ngày. */
    public static final String KHOA_RESET_NGAY = "top_may_dam_reset_ngay";

    /** Ngày đã xoá gần nhất, dạng {@code yyyyMMdd}. */
    private static final String KHOA_NGAY_DA_XOA = "top_may_dam_ngay_da_xoa";

    private static long homNay() {
        java.time.LocalDate n = java.time.LocalDate.now();
        return n.getYear() * 10000L + n.getMonthValue() * 100L + n.getDayOfMonth();
    }

    /**
     * Xoá bảng xếp hạng nếu hôm nay chưa xoá.
     *
     * <h2>Vì sao ghi lại ngày thay vì hẹn giờ nửa đêm</h2>
     *
     * <p>Hẹn giờ chỉ đúng khi máy chủ chạy liên tục qua đúng khoảnh khắc ấy. Máy
     * chủ tắt lúc 23h50 và bật lại lúc 0h10 thì cái hẹn không bao giờ nổ, và
     * bảng xếp hạng không được xoá — không ai để ý cho tới khi thấy kỷ lục hôm
     * qua vẫn còn. Ghi lại <b>ngày đã xoá</b> thì lần khởi động nào cũng tự bù,
     * kể cả tắt máy mấy hôm.</p>
     */
    public static void xoaTheoNgayNeuCan() {
        try {
            if (!ConfigDAO.on(KHOA_RESET_NGAY)) {
                return;
            }
            long nay = homNay();
            if (ConfigDAO.num(KHOA_NGAY_DA_XOA, 0) >= nay) {
                return;
            }
            if (resetTatCa() >= 0) {
                ConfigDAO.set(KHOA_NGAY_DA_XOA, String.valueOf(nay));
            }
        } catch (Exception ex) {
            Logger.logException(TopMayDamDAO.class, ex,
                    "Lỗi xoá top máy đấm theo ngày");
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
