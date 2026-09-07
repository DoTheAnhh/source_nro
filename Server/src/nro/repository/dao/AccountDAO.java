package nro.repository.dao;

import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Đọc/ghi phần <b>tiền</b> của tài khoản (bảng {@code account}).
 *
 * <p>Tách khỏi {@link PlayerDAO} vì tiền nằm ở <b>tài khoản</b>, không phải nhân
 * vật: một tài khoản có thể có nhiều nhân vật nhưng chỉ một ví.
 * {@code PlayerDAO.updatePlayer()} ghi bảng {@code player} và <b>không</b> đụng
 * tới các cột tiền — nên đổi tiền phải gọi qua đây.</p>
 *
 * <p>Bốn cột liên quan:</p>
 * <ul>
 *   <li>{@code vnd}      — số dư hiện tại, tiêu được trong game</li>
 *   <li>{@code tongnap}  — tổng đã nạp từ trước tới nay (chỉ tăng)</li>
 *   <li>{@code tong_nap2} — bộ đếm tổng nạp thứ hai, cách tính khác</li>
 *   <li>{@code coin}     — loại tiền phụ</li>
 * </ul>
 */
public class AccountDAO {

    private AccountDAO() {
    }

    /** Ảnh chụp phần tiền của một tài khoản. */
    public static final class Money {

        public int vnd;
        public long tongNap;
        public long tongNap2;
        public int coin;

        @Override
        public String toString() {
            return "vnd=" + vnd + ", tongNap=" + tongNap + ", coin=" + coin;
        }
    }

    /**
     * Đọc phần tiền của một tài khoản.
     *
     * @return {@code null} nếu không có tài khoản đó
     */
    public static Money load(int accountId) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT vnd, tongnap, tong_nap2, coin FROM account WHERE id = ?", accountId);
            if (rs.next()) {
                Money m = new Money();
                m.vnd = rs.getInt("vnd");
                m.tongNap = rs.getLong("tongnap");
                m.tongNap2 = rs.getLong("tong_nap2");
                m.coin = rs.getInt("coin");
                return m;
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đọc tiền tài khoản " + accountId);
        } finally {
            dispose(rs);
        }
        return null;
    }

    /** Tìm id tài khoản của một nhân vật. Trả {@code -1} nếu không thấy. */
    public static int accountIdOfPlayer(long playerId) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT account_id FROM player WHERE id = ?", playerId);
            if (rs.next()) {
                return rs.getInt("account_id");
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi tra account_id của nhân vật " + playerId);
        } finally {
            dispose(rs);
        }
        return -1;
    }

    /**
     * Cộng tiền nạp cho một tài khoản.
     *
     * <p>Cộng vào <b>cả</b> {@code vnd} (tiêu được) lẫn {@code tongnap} (thống kê
     * tích luỹ). Dùng phép cộng ngay trong câu SQL ({@code vnd = vnd + ?}) chứ
     * không đọc-rồi-ghi: nếu người chơi đang online và tiêu tiền cùng lúc, đọc
     * trước ghi sau sẽ làm mất giao dịch của họ.</p>
     *
     * @param amount số tiền cộng thêm, phải &gt; 0
     * @return {@code true} nếu có đúng một dòng được cập nhật
     */
    public static boolean addTopUp(int accountId, long amount, String note) {
        if (amount <= 0) {
            return false;
        }
        boolean ok;
        try {
            ok = ConnectDB.executeUpdate(
                    "UPDATE account SET vnd = vnd + ?, tongnap = tongnap + ? WHERE id = ?",
                    amount, amount, accountId) == 1;
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi cộng tiền nạp cho tài khoản " + accountId);
            return false;
        }
        if (ok) {
            // Ghi nhật ký SAU khi tiền đã vào. Nếu bước này hỏng thì chỉ mất dấu
            // vết, không mất tiền — nên không trả false ở đây.
            logAdminTopUp(accountId, amount, note);
        }
        return ok;
    }

    /**
     * Ghi một dòng nhật ký cho lần admin nạp tay.
     *
     * <p>Dùng chung bảng {@code history_bank} với nạp chuyển khoản để lịch sử của
     * người chơi chỉ có một chỗ để xem. Cột {@code code} là khoá duy nhất nên
     * phải sinh giá trị không trùng.</p>
     */
    private static void logAdminTopUp(int accountId, long amount, String note) {
        String user = usernameOf(accountId);
        if (user == null) {
            return;
        }
        String code = "ADMIN-" + accountId + "-" + System.nanoTime();
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO history_bank (username, amount_vnd, amount_cash, description, code)"
                    + " VALUES (?, ?, 0, ?, ?)",
                    user, (double) amount,
                    note == null || note.trim().isEmpty() ? "Admin nạp qua panel quản trị" : note,
                    code);
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex,
                    "Nạp tiền thành công nhưng KHÔNG ghi được nhật ký cho " + user);
        }
    }

    /** Tên đăng nhập của một tài khoản. {@code null} nếu không có. */
    public static String usernameOf(int accountId) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT username FROM account WHERE id = ?", accountId);
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi tra username của tài khoản " + accountId);
        } finally {
            dispose(rs);
        }
        return null;
    }

    /** Một dòng lịch sử nạp tiền. */
    public static final class TopUpLog {

        public String time;
        public String source;
        public long amount;
        public String note;
    }

    /**
     * Lịch sử nạp tiền của một tài khoản, mới nhất lên trước.
     *
     * <p>Gộp từ <b>hai</b> bảng — chúng là hai đường nạp khác nhau và cùng khoá
     * theo <i>tên đăng nhập</i>, không phải id:</p>
     * <ul>
     *   <li>{@code history_bank} — chuyển khoản, và cả các lần admin nạp tay
     *       qua panel này.</li>
     *   <li>{@code napthe} — nạp thẻ cào; {@code status} khác 1 nghĩa là thẻ
     *       không thành công, vẫn hiện để đối chiếu khi người chơi khiếu nại.</li>
     * </ul>
     */
    public static java.util.List<TopUpLog> history(int accountId, int limit) {
        java.util.List<TopUpLog> out = new java.util.ArrayList<>();
        String user = usernameOf(accountId);
        if (user == null) {
            return out;
        }

        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT created_at, amount_vnd, description FROM history_bank"
                    + " WHERE username = ? ORDER BY created_at DESC, id DESC LIMIT ?", user, limit);
            while (rs.next()) {
                TopUpLog t = new TopUpLog();
                t.time = String.valueOf(rs.getObject("created_at"));
                t.source = "Chuyển khoản / Admin";
                t.amount = (long) rs.getDouble("amount_vnd");
                t.note = rs.getString("description");
                out.add(t);
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đọc history_bank của " + user);
        } finally {
            dispose(rs);
        }

        rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT created_at, amount, telco, serial, status FROM napthe"
                    + " WHERE user_nap = ? ORDER BY created_at DESC, id DESC LIMIT ?", user, limit);
            while (rs.next()) {
                TopUpLog t = new TopUpLog();
                t.time = String.valueOf(rs.getObject("created_at"));
                t.source = "Thẻ " + rs.getString("telco");
                t.amount = rs.getInt("amount");
                int st = rs.getInt("status");
                t.note = (st == 1 ? "Thành công" : "Thất bại (status=" + st + ")")
                        + " | seri " + rs.getString("serial");
                out.add(t);
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đọc napthe của " + user);
        } finally {
            dispose(rs);
        }

        // Hai bảng đọc riêng nên phải trộn lại theo thời gian.
        // Chuỗi thời gian của MySQL có dạng yyyy-MM-dd HH:mm:ss nên so sánh
        // chuỗi cũng ra đúng thứ tự.
        //
        // Hai bản ghi TRONG CÙNG MỘT GIÂY thì so thời gian ra bằng nhau. Đã thêm
        // `id DESC` vào câu lệnh để mỗi bảng tự sắp đúng thứ tự ghi; List.sort
        // của Java là sắp xếp ỔN ĐỊNH nên thứ tự đó được giữ nguyên khi trộn.
        // Thiếu cả hai thì hai lần nạp liền nhau hiện đảo lộn ngẫu nhiên.
        out.sort((a, b) -> String.valueOf(b.time).compareTo(String.valueOf(a.time)));
        return out;
    }

    // =====================================================================
    //  Vé tuần / vé tháng
    // =====================================================================

    /** Trạng thái vé của một tài khoản. */
    public static final class Ve {

        /** {@code 0} không có vé, {@code 1} vé tuần, {@code 2} vé tháng. */
        public int loai;
        /** Thời điểm hết hạn, tính bằng mili-giây. {@code 0} là không có vé. */
        public long hetHan;

        public boolean conHan() {
            return loai != 0 && hetHan > System.currentTimeMillis();
        }

        /** Số ngày còn lại, làm tròn lên — còn 2 tiếng vẫn tính là 1 ngày. */
        public int soNgayConLai() {
            if (!conHan()) {
                return 0;
            }
            long ms = hetHan - System.currentTimeMillis();
            return (int) ((ms + 86_400_000L - 1) / 86_400_000L);
        }

        public String moTa() {
            if (!conHan()) {
                return "Không có vé";
            }
            return (loai == 2 ? "Vé tháng" : "Vé tuần") + " — còn " + soNgayConLai() + " ngày";
        }
    }

    /**
     * Đọc trạng thái vé.
     *
     * <p>Vé tháng được xét trước vé tuần: hai cột có thể cùng bằng 1 nếu dữ liệu
     * cũ bị lỗi, và vé tháng là loại có lợi hơn nên ưu tiên cho người chơi.</p>
     */
    public static Ve loadVe(int accountId) {
        Ve v = new Ve();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT vetuan, vethang, vetuan_expire, vethang_expire"
                    + " FROM account WHERE id = ?", accountId);
            if (rs.next()) {
                long hetThang = rs.getLong("vethang_expire");
                long hetTuan = rs.getLong("vetuan_expire");
                if (rs.getInt("vethang") == 1 && hetThang > System.currentTimeMillis()) {
                    v.loai = 2;
                    v.hetHan = hetThang;
                } else if (rs.getInt("vetuan") == 1 && hetTuan > System.currentTimeMillis()) {
                    v.loai = 1;
                    v.hetHan = hetTuan;
                }
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đọc vé của tài khoản " + accountId);
        } finally {
            dispose(rs);
        }
        return v;
    }

    /**
     * Cấp vé cho một tài khoản.
     *
     * <p><b>Không đụng tới tiền.</b> Đây là đường admin cấp tay, khác hẳn đường
     * người chơi tự mua ở NPC Ông Gôhan — bên đó mới trừ {@code vnd}. Admin cấp
     * là cấp thẳng, không trừ số dư và không ghi vào lịch sử nạp, vì không có
     * giao dịch tiền nào xảy ra.</p>
     *
     * <p>Cấp vé nào thì <b>xoá vé kia</b>, giống hệt {@code OngGohan.buyVeThang()}
     * — game chỉ cho giữ một loại vé tại một thời điểm.</p>
     *
     * @param thang  {@code true} cấp vé tháng, {@code false} cấp vé tuần
     * @param soNgay số ngày hiệu lực, tính từ bây giờ
     * @return {@code null} nếu thành công, hoặc câu giải thích vì sao không cấp được
     */
    public static String grantVe(int accountId, boolean thang, int soNgay) {
        if (soNgay <= 0) {
            return "Số ngày phải lớn hơn 0.";
        }
        long hetHan = System.currentTimeMillis() + soNgay * 86_400_000L;
        String cotBat = thang ? "vethang" : "vetuan";
        String cotHan = thang ? "vethang_expire" : "vetuan_expire";
        String cotTat = thang ? "vetuan" : "vethang";
        String cotHanTat = thang ? "vetuan_expire" : "vethang_expire";

        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE account SET " + cotBat + " = 1, " + cotHan + " = ?,"
                    + " " + cotTat + " = 0, " + cotHanTat + " = 0 WHERE id = ?",
                    hetHan, accountId);
            if (n != 1) {
                return "Không tìm thấy tài khoản này.";
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi cấp vé cho tài khoản " + accountId);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
        return null;
    }

    /** Gỡ cả hai loại vé. */
    public static boolean clearVe(int accountId) {
        try {
            return ConnectDB.executeUpdate(
                    "UPDATE account SET vetuan = 0, vethang = 0,"
                    + " vetuan_expire = 0, vethang_expire = 0 WHERE id = ?", accountId) == 1;
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi gỡ vé của tài khoản " + accountId);
            return false;
        }
    }

    /** Số dư VND hiện tại. {@code -1} nếu không đọc được. */
    public static long vndOf(int accountId) {
        Money m = load(accountId);
        return m == null ? -1 : m.vnd;
    }

    // =====================================================================
    //  Quyền và tiền
    // =====================================================================

    /** Ba cờ quyền của một tài khoản. */
    public static final class Quyen {

        public boolean admin;
        public boolean founder;
        public boolean quanTriVien;
    }

    public static Quyen loadQuyen(int accountId) {
        Quyen q = new Quyen();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT is_admin, isFounder, isQuanTriVien FROM account WHERE id = ?",
                    accountId);
            if (rs.next()) {
                q.admin = rs.getBoolean("is_admin");
                q.founder = rs.getBoolean("isFounder");
                q.quanTriVien = rs.getBoolean("isQuanTriVien");
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đọc quyền tài khoản " + accountId);
        } finally {
            dispose(rs);
        }
        return q;
    }

    /**
     * Ghi lại ba cờ quyền.
     *
     * <p>Ghi cả ba trong một câu chứ không ba câu riêng: đổi quyền nửa chừng rồi
     * lỗi sẽ để tài khoản ở trạng thái không ai định.</p>
     */
    public static boolean saveQuyen(int accountId, Quyen q) {
        try {
            return ConnectDB.executeUpdate(
                    "UPDATE account SET is_admin = ?, isFounder = ?, isQuanTriVien = ?"
                    + " WHERE id = ?",
                    q.admin ? 1 : 0, q.founder ? 1 : 0, q.quanTriVien ? 1 : 0, accountId) == 1;
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi lưu quyền tài khoản " + accountId);
            return false;
        }
    }

    /**
     * Đặt thẳng các con số tiền của tài khoản.
     *
     * <p>Khác {@link #addTopUp}: hàm kia <b>cộng thêm</b> và ghi vào lịch sử nạp
     * vì đó là một giao dịch nạp thật. Hàm này <b>đặt đè</b> — dùng để sửa số
     * liệu sai, nên không ghi vào lịch sử nạp: ghi vào đó sẽ làm thống kê doanh
     * thu sai lệch.</p>
     *
     * @param tongNap {@code -1} nghĩa là giữ nguyên
     */
    public static boolean setTien(int accountId, long vnd, long tongNap, long coin,
                                  long thoiVang) {
        try {
            if (tongNap >= 0) {
                return ConnectDB.executeUpdate(
                        "UPDATE account SET vnd = ?, tongnap = ?, coin = ?, thoi_vang = ?"
                        + " WHERE id = ?",
                        vnd, tongNap, coin, thoiVang, accountId) == 1;
            }
            return ConnectDB.executeUpdate(
                    "UPDATE account SET vnd = ?, coin = ?, thoi_vang = ? WHERE id = ?",
                    vnd, coin, thoiVang, accountId) == 1;
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đặt tiền tài khoản " + accountId);
            return false;
        }
    }

    /** Số thỏi vàng chưa nhận của tài khoản. */
    public static int thoiVangOf(int accountId) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT thoi_vang FROM account WHERE id = ?", accountId);
            if (rs.next()) {
                return rs.getInt("thoi_vang");
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đọc thỏi vàng " + accountId);
        } finally {
            dispose(rs);
        }
        return 0;
    }

    // =====================================================================
    //  Quản lý toàn bộ tài khoản
    // =====================================================================

    /** Một tài khoản trong danh sách quản trị. */
    public static final class Row {

        public int id;
        public String username;
        public String quyen;
        public long vnd;
        public long tongNap;
        public int thoiVang;
        public int soNhanVat;
        /** Tên các nhân vật của tài khoản, ngăn nhau bằng dấu phẩy. */
        public String tenNhanVat;
        public String lanCuoiDangNhap;
    }

    /** Toàn bộ tài khoản, kèm số nhân vật của mỗi tài khoản. */
    public static java.util.List<Row> listAll() {
        java.util.List<Row> out = new java.util.ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT a.id, a.username, a.is_admin, a.isFounder, a.isQuanTriVien,"
                    + " a.vnd, a.tongnap, a.thoi_vang, a.last_time_login,"
                    + " (SELECT COUNT(*) FROM player p WHERE p.account_id = a.id) AS so_nv,"
                    // Noi thang ten nhan vat vao danh sach: nhin la biet tai
                    // khoan nay cua ai, khong phai mo tab khac tra cheo.
                    + " (SELECT GROUP_CONCAT(p2.name ORDER BY p2.id SEPARATOR ', ')"
                    + "  FROM player p2 WHERE p2.account_id = a.id) AS ten_nv"
                    + " FROM account a ORDER BY a.id");
            while (rs.next()) {
                Row r = new Row();
                r.id = rs.getInt("id");
                r.username = rs.getString("username");
                r.quyen = PlayerDAO.tenQuyen(rs.getBoolean("is_admin"),
                        rs.getBoolean("isFounder"), rs.getBoolean("isQuanTriVien"));
                r.vnd = rs.getLong("vnd");
                r.tongNap = rs.getLong("tongnap");
                r.thoiVang = rs.getInt("thoi_vang");
                r.soNhanVat = rs.getInt("so_nv");
                r.tenNhanVat = rs.getStringOrNull("ten_nv");
                r.lanCuoiDangNhap = String.valueOf(rs.getObject("last_time_login"));
                out.add(r);
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi liệt kê tài khoản");
        } finally {
            dispose(rs);
        }
        return out;
    }

    public static int demTaiKhoan() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM account");
            if (rs.next()) {
                return rs.getInt("n");
            }
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi đếm tài khoản");
        } finally {
            dispose(rs);
        }
        return -1;
    }

    /**
     * Xoá một tài khoản, kèm nhân vật và lịch sử nạp của nó.
     *
     * <p>Xoá tài khoản mà để lại nhân vật thì nhân vật đó mồ côi: vẫn hiện trong
     * danh sách, vẫn chiếm tên, nhưng không ai đăng nhập vào được nữa.</p>
     */
    public static boolean xoaTaiKhoan(int accountId) {
        try {
            String user = usernameOf(accountId);
            ConnectDB.executeUpdate("DELETE FROM player WHERE account_id = ?", accountId);
            if (user != null) {
                ConnectDB.executeUpdate("DELETE FROM history_bank WHERE username = ?", user);
            }
            return ConnectDB.executeUpdate("DELETE FROM account WHERE id = ?", accountId) == 1;
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi xoá tài khoản " + accountId);
            return false;
        }
    }

    /**
     * Xoá <b>toàn bộ</b> tài khoản, nhân vật và lịch sử nạp.
     *
     * <p><b>Không hoàn tác được.</b> Xoá nhân vật trước rồi mới tới tài khoản —
     * ngược lại thì nhân vật thành mồ côi nếu bước sau hỏng giữa chừng.</p>
     *
     * @param giuLaiAdmin giữ lại các tài khoản có quyền Admin
     * @return số tài khoản đã xoá, hoặc {@code -1} nếu lỗi
     */
    public static int xoaHetTaiKhoan(boolean giuLaiAdmin) {
        try {
            if (giuLaiAdmin) {
                ConnectDB.executeUpdate(
                        "DELETE FROM player WHERE account_id IN"
                        + " (SELECT id FROM account WHERE is_admin = 0 AND isFounder = 0)");
                return ConnectDB.executeUpdate(
                        "DELETE FROM account WHERE is_admin = 0 AND isFounder = 0");
            }
            ConnectDB.executeUpdate("DELETE FROM player");
            ConnectDB.executeUpdate("DELETE FROM history_bank");
            return ConnectDB.executeUpdate("DELETE FROM account");
        } catch (Exception ex) {
            Logger.logException(AccountDAO.class, ex, "Lỗi xoá toàn bộ tài khoản");
            return -1;
        }
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
