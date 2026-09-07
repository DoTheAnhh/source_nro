package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Dò các <b>tham chiếu gãy</b> trong dữ liệu game.
 *
 * <h2>Vì sao cần</h2>
 *
 * <p>Máy chủ không kiểm tra gì khi nạp: bảng {@code item_template} chứa các cột
 * {@code head}, {@code body}, {@code leg}, {@code part} là <i>id trỏ sang bảng
 * {@code part}</i>, nhưng không có khoá ngoại nào ràng buộc. Trỏ sang một id
 * không tồn tại thì máy chủ vẫn khởi động bình thường, vẫn gửi id đó cho client,
 * và client không tìm thấy hình nên vẽ ra thứ khác — biểu hiện đúng như
 * "cải trang bị sai part với nhau" hay "mặc vào không có hình".</p>
 *
 * <p>Không có gì trong log báo chuyện này. Lớp này đi tìm thẳng trong CSDL.</p>
 */
public class DataCheckDAO {

    private DataCheckDAO() {
    }

    /** Một vấn đề tìm được. */
    public static final class Issue {

        /** Nhóm vấn đề, ví dụ {@code "Cải trang trỏ tới part không tồn tại"}. */
        public String loai;
        public int itemId;
        public String itemName;
        /** Mô tả cụ thể, ví dụ {@code "head=1984 (không có part này)"}. */
        public String chiTiet;
        /** Gợi ý cách xử lý. */
        public String goiY;
    }

    /**
     * Quét toàn bộ và trả về danh sách vấn đề.
     *
     * <p>Danh sách rỗng nghĩa là dữ liệu sạch ở những điểm đang kiểm.</p>
     */
    public static List<Issue> quet() {
        List<Issue> out = new ArrayList<>();
        partGay(out);
        caiTrangKhongPart(out);
        deoLungKhongPart(out);
        return out;
    }

    /**
     * Cột {@code head} / {@code body} / {@code leg} / {@code part} trỏ tới một id
     * không có trong bảng {@code part}.
     *
     * <p>Đây là nguyên nhân trực tiếp của cải trang hiển thị sai: client được bảo
     * "mặc part 1984" mà part đó không tồn tại.</p>
     */
    private static void partGay(List<Issue> out) {
        String sql = "SELECT t.id, t.name, t.type, t.head, t.body, t.leg, t.part"
                + " FROM item_template t WHERE"
                + " (t.head <> -1 AND t.head NOT IN (SELECT id FROM part))"
                + " OR (t.body <> -1 AND t.body NOT IN (SELECT id FROM part))"
                + " OR (t.leg  <> -1 AND t.leg  NOT IN (SELECT id FROM part))"
                + " OR (t.part <> -1 AND t.part NOT IN (SELECT id FROM part))"
                + " ORDER BY t.id";
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(sql);
            while (rs.next()) {
                Issue i = new Issue();
                i.loai = "Trỏ tới part không tồn tại";
                i.itemId = rs.getInt("id");
                i.itemName = rs.getString("name");
                StringBuilder sb = new StringBuilder();
                them(sb, "head", rs.getInt("head"));
                them(sb, "body", rs.getInt("body"));
                them(sb, "leg", rs.getInt("leg"));
                them(sb, "part", rs.getInt("part"));
                i.chiTiet = sb.toString();
                i.goiY = "Nhập thiếu bảng part — cần import các part này, "
                        + "hoặc gỡ vật phẩm khỏi shop cho tới khi có hình.";
                out.add(i);
            }
        } catch (Exception ex) {
            Logger.logException(DataCheckDAO.class, ex, "Lỗi dò part gãy");
        } finally {
            dispose(rs);
        }
    }

    private static void them(StringBuilder sb, String ten, int gt) {
        if (gt == -1) {
            return;
        }
        if (!tonTaiPart(gt)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(ten).append('=').append(gt);
        }
    }

    private static boolean tonTaiPart(int id) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM part WHERE id = ?", id);
            return rs.next();
        } catch (Exception ex) {
            return true;      // đọc hỏng thì coi như có, tránh báo động giả
        } finally {
            dispose(rs);
        }
    }

    /**
     * Cải trang (type 5) không đặt phần hình nào.
     *
     * <p>Cải trang chỉ đổi đầu là <b>bình thường</b> — {@code body = -1} nghĩa là
     * giữ nguyên thân của người chơi. Nhưng cả ba đều {@code -1} thì mặc vào
     * không có gì thay đổi cả.</p>
     */
    private static void caiTrangKhongPart(List<Issue> out) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, name FROM item_template"
                    + " WHERE type = 5 AND head = -1 AND body = -1 AND leg = -1 ORDER BY id");
            while (rs.next()) {
                Issue i = new Issue();
                i.loai = "Cải trang không có hình";
                i.itemId = rs.getInt("id");
                i.itemName = rs.getString("name");
                i.chiTiet = "head, body, leg đều = -1";
                i.goiY = "Mặc vào sẽ không đổi gì. Điền id part hoặc bỏ vật phẩm.";
                out.add(i);
            }
        } catch (Exception ex) {
            Logger.logException(DataCheckDAO.class, ex, "Lỗi dò cải trang không part");
        } finally {
            dispose(rs);
        }
    }

    /**
     * Vật phẩm đeo lưng không có {@code part}.
     *
     * <p>{@code Player.getFlagBag()} đọc thẳng {@code itemsBody.get(8)
     * .template.part} và trả về {@code -1} nếu cột đó là {@code -1} — client
     * không nhận được id hình nào nên <b>không vẽ gì và không có animation</b>.
     * Đúng biểu hiện "đeo lưng không có hiệu ứng".</p>
     *
     * <p>Ô số 8 là ô đeo lưng: {@code putItemBody()} có nhánh
     * {@code if (index == 8)} gọi {@code removeEffPlayer(player, part)}.</p>
     */
    private static void deoLungKhongPart(List<Issue> out) {
        CrisResultSet rs = null;
        try {
            // Type 17 la "Dai lung" theo doi chieu du lieu. Chung deu nam o o 8.
            rs = ConnectDB.executeQuery(
                    "SELECT id, name FROM item_template"
                    + " WHERE type = 17 AND part = -1 ORDER BY id");
            while (rs.next()) {
                Issue i = new Issue();
                i.loai = "Đeo lưng không có hình / animation";
                i.itemId = rs.getInt("id");
                i.itemName = rs.getString("name");
                i.chiTiet = "part = -1";
                i.goiY = "getFlagBag() trả -1 nên client không vẽ gì. "
                        + "Điền id part có nhiều khung hình để có animation.";
                out.add(i);
            }
        } catch (Exception ex) {
            Logger.logException(DataCheckDAO.class, ex, "Lỗi dò đeo lưng không part");
        } finally {
            dispose(rs);
        }
    }

    /** Số khung hình của một part. {@code -1} nếu không đọc được. */
    public static int soKhungHinh(int partId) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT DATA FROM part WHERE id = ?", partId);
            if (rs.next()) {
                String d = rs.getString("DATA");
                if (d == null || d.isEmpty()) {
                    return 0;
                }
                int n = 1;
                int k = d.indexOf("],[");
                while (k >= 0) {
                    n++;
                    k = d.indexOf("],[", k + 1);
                }
                return n;
            }
        } catch (Exception ex) {
            Logger.logException(DataCheckDAO.class, ex, "Lỗi đếm khung hình part " + partId);
        } finally {
            dispose(rs);
        }
        return -1;
    }

    private static void dispose(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
