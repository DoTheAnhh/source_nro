package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Đọc/ghi bảng {@code giftcode} cho panel quản trị.
 *
 * <h2>Định dạng dữ liệu trong bảng</h2>
 *
 * <p>Ba cột là chuỗi chứ không phải quan hệ, nên phải giữ đúng định dạng mà
 * {@code GiftCodeManager.init()} đang đọc — sai một dấu là cả bảng giftcode
 * không nạp được lúc khởi động:</p>
 *
 * <ul>
 *   <li>{@code item} — mảng JSON {@code [{"id":457,"quantity":10}, ...]}</li>
 *   <li>{@code option} — mảng JSON {@code [{"id":6,"param":25}, ...]}</li>
 *   <li>{@code listIdPlayers} — danh sách id người đã dùng, dạng
 *       {@code [1,2,3]}; {@code init()} cắt cứng ký tự đầu và cuối rồi tách theo
 *       dấu phẩy, nên chuỗi rỗng phải là {@code ""} chứ không được là
 *       {@code "[]"}… thực ra {@code "[]"} cũng chạy vì cắt xong còn chuỗi rỗng,
 *       nhưng để rỗng hẳn thì an toàn hơn.</li>
 * </ul>
 *
 * <p>{@code datecreate} và {@code expired} khai báo là {@code text} nhưng được
 * đọc bằng {@code getTimestamp()}, nên phải ghi đúng dạng
 * {@code yyyy-MM-dd HH:mm:ss}.</p>
 */
public class GiftCodeDAO {

    private GiftCodeDAO() {
    }

    /** Một dòng giftcode như panel nhìn thấy. */
    public static final class Row {

        public int id;
        public String code;
        public String item;
        public String option;
        public String taoLuc;
        public String hetHan;
        public int conLai;
        /** Số lượt đã dùng, đếm từ {@code listIdPlayers}. */
        public int daDung;
        /** Mỗi người được nhập tối đa bao nhiêu lần. */
        public int gioiHanMoiNguoi;
    }

    /**
     * Thêm cột {@code limit_per_player} nếu bảng chưa có.
     *
     * <p>Dùng {@code ADD COLUMN IF NOT EXISTS} nên gọi lại không sao — MariaDB
     * 10.4 hỗ trợ cú pháp này.</p>
     *
     * <p>Mặc định 1 để các mã cũ giữ đúng hành vi cũ: mỗi người một lần.</p>
     */
    private static volatile boolean daVaCot;

    /** Máy chủ gọi lúc nạp giftcode để bản CSDL cũ cũng có cột này. */
    public static void damBaoCot() {
        vaCot();
    }

    private static void vaCot() {
        if (daVaCot) {
            return;
        }
        daVaCot = true;
        try {
            ConnectDB.executeUpdate("ALTER TABLE giftcode ADD COLUMN IF NOT EXISTS"
                    + " limit_per_player INT NOT NULL DEFAULT 1");
        } catch (Exception ex) {
            Logger.logException(GiftCodeDAO.class, ex,
                    "Không thêm được cột limit_per_player — mỗi người vẫn chỉ nhập được 1 lần");
        }
    }

    /** Toàn bộ giftcode, mới nhất lên trước. */
    public static List<Row> list() {
        List<Row> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            vaCot();
            rs = ConnectDB.executeQuery(
                    "SELECT id, code, item, `option`, datecreate, expired, count_left,"
                    + " listIdPlayers, limit_per_player FROM giftcode ORDER BY id DESC");
            while (rs.next()) {
                Row r = new Row();
                r.id = rs.getInt("id");
                r.code = rs.getString("code");
                r.item = nz(rs.getString("item"));
                r.option = nz(rs.getString("option"));
                r.taoLuc = nz(rs.getString("datecreate"));
                r.hetHan = nz(rs.getString("expired"));
                r.conLai = rs.getInt("count_left");
                r.daDung = demNguoiDung(rs.getString("listIdPlayers"));
                r.gioiHanMoiNguoi = Math.max(1, rs.getInt("limit_per_player"));
                out.add(r);
            }
        } catch (Exception ex) {
            Logger.logException(GiftCodeDAO.class, ex, "Lỗi đọc bảng giftcode");
        } finally {
            dispose(rs);
        }
        return out;
    }

    /**
     * Đếm số id trong chuỗi {@code [1,2,3]}.
     *
     * <p>Không tách bằng JSON: cột này có bản ghi cũ ghi sai định dạng, mà đếm
     * hỏng thì cả bảng không hiện được. Đếm thô theo dấu phẩy là đủ và không
     * bao giờ ném lỗi.</p>
     */
    private static int demNguoiDung(String raw) {
        if (raw == null) {
            return 0;
        }
        String s = raw.replace("[", "").replace("]", "").replace("\"", "").trim();
        if (s.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (String p : s.split(",")) {
            if (!p.trim().isEmpty()) {
                n++;
            }
        }
        return n;
    }

    /**
     * Thêm một giftcode mới.
     *
     * @return {@code null} nếu thành công, hoặc câu giải thích lỗi
     */
    public static String them(String code, String item, String option,
                             String taoLuc, String hetHan, int soLuot, int moiNguoi) {
        String loi = kiemTra(code, item, option, soLuot);
        if (loi != null) {
            return loi;
        }
        if (moiNguoi < 1) {
            return "Giới hạn mỗi người phải từ 1 trở lên.";
        }
        try {
            vaCot();
            if (tonTai(code)) {
                return "Mã \"" + code + "\" đã tồn tại.";
            }
            ConnectDB.executeUpdate(
                    "INSERT INTO giftcode (code, item, `option`, listIdPlayers,"
                    + " datecreate, expired, count_left, limit_per_player)"
                    + " VALUES (?, ?, ?, '', ?, ?, ?, ?)",
                    code, item, option, taoLuc, hetHan, soLuot, moiNguoi);
            return null;
        } catch (Exception ex) {
            Logger.logException(GiftCodeDAO.class, ex, "Lỗi thêm giftcode " + code);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /** Sửa một giftcode đã có. */
    public static String sua(int id, String code, String item, String option,
                             String taoLuc, String hetHan, int soLuot, int moiNguoi) {
        String loi = kiemTra(code, item, option, soLuot);
        if (loi != null) {
            return loi;
        }
        if (moiNguoi < 1) {
            return "Giới hạn mỗi người phải từ 1 trở lên.";
        }
        try {
            vaCot();
            ConnectDB.executeUpdate(
                    "UPDATE giftcode SET code = ?, item = ?, `option` = ?,"
                    + " datecreate = ?, expired = ?, count_left = ?,"
                    + " limit_per_player = ? WHERE id = ?",
                    code, item, option, taoLuc, hetHan, soLuot, moiNguoi, id);
            return null;
        } catch (Exception ex) {
            Logger.logException(GiftCodeDAO.class, ex, "Lỗi sửa giftcode " + code);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /** Xoá một giftcode. */
    public static boolean xoa(int id) {
        try {
            return ConnectDB.executeUpdate("DELETE FROM giftcode WHERE id = ?", id) == 1;
        } catch (Exception ex) {
            Logger.logException(GiftCodeDAO.class, ex, "Lỗi xoá giftcode id " + id);
            return false;
        }
    }

    /** Xoá danh sách người đã dùng, cho phép dùng lại từ đầu. */
    public static boolean xoaLichSuDung(int id) {
        try {
            return ConnectDB.executeUpdate(
                    "UPDATE giftcode SET listIdPlayers = '' WHERE id = ?", id) == 1;
        } catch (Exception ex) {
            Logger.logException(GiftCodeDAO.class, ex, "Lỗi xoá lịch sử dùng giftcode id " + id);
            return false;
        }
    }

    private static boolean tonTai(String code) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM giftcode WHERE code = ?", code);
            return rs.next();
        } catch (Exception ex) {
            return false;
        } finally {
            dispose(rs);
        }
    }

    /**
     * Kiểm tra trước khi ghi.
     *
     * <p>Bắt lỗi định dạng <b>ở đây</b> chứ không để tới lúc máy chủ khởi động:
     * {@code GiftCodeManager.init()} bọc toàn bộ trong {@code catch (Exception)}
     * rỗng, nên một mã sai định dạng làm <i>cả bảng</i> giftcode im lặng không
     * nạp được, không có một dòng log nào.</p>
     */
    private static String kiemTra(String code, String item, String option, int soLuot) {
        if (code == null || code.trim().isEmpty()) {
            return "Mã không được để trống.";
        }
        if (soLuot < 0) {
            return "Số lượt phải từ 0 trở lên.";
        }
        String loi = kiemTraMang(item, "item", "id", "quantity");
        if (loi != null) {
            return loi;
        }
        return kiemTraMang(option, "option", "id", "param");
    }

    private static String kiemTraMang(String raw, String ten, String khoa1, String khoa2) {
        if (raw == null || raw.trim().isEmpty()) {
            return "Ô \"" + ten + "\" không được để trống — dùng [] nếu không có gì.";
        }
        Object o = org.json.simple.JSONValue.parse(raw);
        if (!(o instanceof org.json.simple.JSONArray)) {
            return "Ô \"" + ten + "\" phải là mảng JSON, ví dụ "
                    + "[{\"" + khoa1 + "\":457,\"" + khoa2 + "\":10}]";
        }
        org.json.simple.JSONArray arr = (org.json.simple.JSONArray) o;
        for (int i = 0; i < arr.size(); i++) {
            Object p = arr.get(i);
            if (!(p instanceof org.json.simple.JSONObject)) {
                return "Ô \"" + ten + "\": phần tử " + (i + 1) + " phải là một đối tượng JSON.";
            }
            org.json.simple.JSONObject j = (org.json.simple.JSONObject) p;
            if (j.get(khoa1) == null || j.get(khoa2) == null) {
                return "Ô \"" + ten + "\": phần tử " + (i + 1)
                        + " thiếu khoá \"" + khoa1 + "\" hoặc \"" + khoa2 + "\".";
            }
            try {
                Integer.parseInt(String.valueOf(j.get(khoa1)));
                Integer.parseInt(String.valueOf(j.get(khoa2)));
            } catch (NumberFormatException ex) {
                return "Ô \"" + ten + "\": phần tử " + (i + 1) + " có giá trị không phải số.";
            }
        }
        return null;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
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
