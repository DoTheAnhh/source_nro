package nro.repository.dao;

import nro.core.log.Logger;
import nro.entity.template.ItemOptionTemplate;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Manager;

/**
 * Thêm và sửa các dòng trong bảng {@code item_option_template}.
 *
 * <h2>Id chính là chỉ số mảng — đọc kỹ chỗ này</h2>
 *
 * <p>Máy chủ tra một chỉ số bằng {@code ITEM_OPTION_TEMPLATES.get(id)}: danh
 * sách được nạp theo thứ tự bảng, nên <b>vị trí trong danh sách phải trùng với
 * id</b>. Hiện có đúng 273 dòng, id chạy liền từ 0 đến 272.</p>
 *
 * <p>Hai hệ quả bắt buộc:</p>
 *
 * <ul>
 *   <li><b>Thêm</b> chỉ được cấp id kế tiếp ({@code max + 1}) và nối vào cuối.
 *       Cấp một id bất kỳ ở giữa là mọi chỉ số phía sau lệch một bậc.</li>
 *   <li><b>Xoá thì không được.</b> Bỏ id 141 đi là id 142 tụt xuống 141, và mọi
 *       vật phẩm trong game đang mang chỉ số từ 141 trở lên <b>âm thầm</b> đổi
 *       sang chỉ số khác. Không có cách nào phát hiện ngoài việc người chơi kêu.
 *       Muốn ngừng dùng một chỉ số thì đổi tên nó, đừng xoá.</li>
 * </ul>
 */
public final class ChiSoOptionDAO {

    private ChiSoOptionDAO() {
    }

    /**
     * Thêm một chỉ số mới, id là {@code max + 1}.
     *
     * @return id vừa cấp, hoặc {@code -1} nếu hỏng
     */
    public static int them(String ten) {
        return them(ten, 0);
    }

    /**
     * Thêm một chỉ số mới với {@code type} chỉ định.
     *
     * <p>{@code type} quyết định client in dòng chữ kiểu gì. Dòng <b>mô tả</b>
     * (tên bắt đầu bằng {@code $}) dùng type 9 — chỉ in chữ, không cộng gì.</p>
     */
    public static int them(String ten, int type) {
        if (ten == null || ten.trim().isEmpty()) {
            return -1;
        }
        CrisResultSet rs = null;
        try {
            int max = -1;
            rs = ConnectDB.executeQuery("SELECT MAX(id) m FROM item_option_template");
            if (rs.next()) {
                max = rs.getInt("m");
            }
            rs.dispose();
            rs = null;

            int soDong = Manager.ITEM_OPTION_TEMPLATES == null
                    ? -1 : Manager.ITEM_OPTION_TEMPLATES.size();
            if (soDong >= 0 && soDong != max + 1) {
                // Danh sach trong bo nho khong con khop id -> them nua la lech
                // them. Dung lai va noi ro thay vi lam hong am tham.
                Logger.warning("item_option_template: " + (max + 1) + " id nhưng bộ nhớ có "
                        + soDong + " dòng — không thêm chỉ số mới\n");
                return -1;
            }

            int idMoi = max + 1;
            ConnectDB.executeUpdate(
                    "INSERT INTO item_option_template (id, NAME, type) VALUES (?, ?, ?)",
                    idMoi, ten.trim(), type);

            // Noi vao CUOI danh sach trong bo nho: vi tri moi = idMoi, dung
            // bang chinh id vua cap.
            if (Manager.ITEM_OPTION_TEMPLATES != null) {
                ItemOptionTemplate t = new ItemOptionTemplate();
                t.id = idMoi;
                t.name = ten.trim();
                Manager.ITEM_OPTION_TEMPLATES.add(t);
            }
            tangPhienBanVatPham();
            Logger.log(Logger.GREEN, "Thêm chỉ số mới: " + idMoi + " — " + ten.trim() + "\n");
            return idMoi;
        } catch (Exception ex) {
            Logger.logException(ChiSoOptionDAO.class, ex, "Lỗi thêm chỉ số");
            return -1;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Đổi tên <b>và</b> loại của một chỉ số.
     *
     * <p>{@code type} quyết định client in dòng chữ bằng màu gì, nên đổi tên mà
     * không đổi loại thì dòng mô tả vẫn giữ màu cũ.</p>
     */
    public static boolean doiTen(int id, String ten, int type) {
        if (ten == null || ten.trim().isEmpty()) {
            return false;
        }
        try {
            ConnectDB.executeUpdate(
                    "UPDATE item_option_template SET NAME = ?, type = ? WHERE id = ?",
                    ten.trim(), type, id);
            if (Manager.ITEM_OPTION_TEMPLATES != null
                    && id >= 0 && id < Manager.ITEM_OPTION_TEMPLATES.size()) {
                Manager.ITEM_OPTION_TEMPLATES.get(id).name = ten.trim();
                Manager.ITEM_OPTION_TEMPLATES.get(id).type = type;
            }
            tangPhienBanVatPham();
            return true;
        } catch (Exception ex) {
            Logger.logException(ChiSoOptionDAO.class, ex, "Lỗi đổi chỉ số " + id);
            return false;
        }
    }

    /** Đổi tên một chỉ số, giữ nguyên loại. */
    public static boolean doiTen(int id, String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            return false;
        }
        try {
            ConnectDB.executeUpdate(
                    "UPDATE item_option_template SET NAME = ? WHERE id = ?", ten.trim(), id);
            if (Manager.ITEM_OPTION_TEMPLATES != null
                    && id >= 0 && id < Manager.ITEM_OPTION_TEMPLATES.size()) {
                Manager.ITEM_OPTION_TEMPLATES.get(id).name = ten.trim();
            }
            tangPhienBanVatPham();
            return true;
        } catch (Exception ex) {
            Logger.logException(ChiSoOptionDAO.class, ex, "Lỗi đổi tên chỉ số " + id);
            return false;
        }
    }

    /**
     * Xoá chỉ số — <b>chỉ xoá được cái cuối cùng</b>, và chỉ khi chưa ai dùng.
     *
     * <p>Bỏ một id ở giữa thì mọi id phía sau tụt xuống một bậc, và vật phẩm
     * đang mang chỉ số từ đó trở lên âm thầm đổi sang chỉ số khác. Riêng id cuối
     * thì không có ai phía sau để tụt, nên xoá được thật — vừa đủ để bỏ một chỉ
     * số vừa lỡ tay tạo nhầm.</p>
     *
     * @return {@code null} nếu xoá xong, hoặc câu giải thích vì sao không xoá được
     */
    public static String xoa(int id) {
        try {
            int max = -1;
            CrisResultSet rs = ConnectDB.executeQuery(
                    "SELECT MAX(id) m FROM item_option_template");
            if (rs.next()) {
                max = rs.getInt("m");
            }
            rs.dispose();

            if (id != max) {
                return viSaoKhongXoaDuoc(id);
            }
            String dung = dangDungODau(id);
            if (dung != null) {
                return "Không xoá được chỉ số " + id + " vì đang có "
                        + dung + " dùng nó.\n\n"
                        + "Xoá đi thì chỗ đó trỏ vào một chỉ số không còn tồn tại. "
                        + "Gỡ hết các chỗ dùng trước, hoặc đổi tên chỉ số này thay vì xoá.";
            }

            ConnectDB.executeUpdate("DELETE FROM item_option_template WHERE id = ?", id);
            if (Manager.ITEM_OPTION_TEMPLATES != null
                    && id == Manager.ITEM_OPTION_TEMPLATES.size() - 1) {
                // Bo phan tu CUOI: cac vi tri con lai giu nguyen, id van bang vi tri.
                Manager.ITEM_OPTION_TEMPLATES.remove(id);
            }
            tangPhienBanVatPham();
            Logger.log(Logger.YELLOW, "Đã xoá chỉ số cuối: " + id + "\n");
            return null;
        } catch (Exception ex) {
            Logger.logException(ChiSoOptionDAO.class, ex, "Lỗi xoá chỉ số " + id);
            return "Lỗi khi xoá — xem log máy chủ.";
        }
    }

    /**
     * Chỗ nào đang dùng chỉ số này; {@code null} nếu không chỗ nào.
     *
     * <p>Ba nơi giữ id chỉ số: vật phẩm bày trong cửa hàng, set kích hoạt, và
     * đồ của người chơi (lưu dạng JSON {@code [id,giá trị]} trong ba cột
     * {@code items_body} / {@code items_bag} / {@code items_box}).</p>
     */
    private static String dangDungODau(int id) throws Exception {
        if (dem("SELECT COUNT(*) c FROM item_shop_option WHERE option_id = ?", id) > 0) {
            return "vật phẩm trong cửa hàng";
        }
        // option_ids la chuoi id ngan cach bang dau phay: phai khop tron ca id,
        // khong thi id 4 lai an theo id 14.
        if (dem("SELECT COUNT(*) c FROM set_kich_hoat WHERE option_ids = ?"
                + " OR option_ids LIKE ? OR option_ids LIKE ? OR option_ids LIKE ?",
                String.valueOf(id), id + ",%", "%," + id, "%," + id + ",%") > 0) {
            return "set kích hoạt";
        }
        String mau = "%[" + id + ",%";
        if (dem("SELECT COUNT(*) c FROM player WHERE items_body LIKE ?"
                + " OR items_bag LIKE ? OR items_box LIKE ?", mau, mau, mau) > 0) {
            return "đồ của người chơi";
        }
        return null;
    }

    private static int dem(String sql, Object... tham) throws Exception {
        CrisResultSet rs = ConnectDB.executeQuery(sql, tham);
        int n = rs.next() ? rs.getInt("c") : 0;
        rs.dispose();
        return n;
    }

    /**
     * Câu giải thích vì sao không xoá được, để panel hiện nguyên văn.
     *
     * <p>Trả về câu chữ thay vì cứ thế từ chối: người dùng cần biết <i>tại sao</i>
     * và làm gì thay thế, không thì họ sẽ đi xoá thẳng trong phpMyAdmin.</p>
     */
    public static String viSaoKhongXoaDuoc(int id) {
        return "Không xoá được chỉ số " + id + ".\n\n"
                + "Máy chủ tra chỉ số bằng vị trí trong danh sách, mà vị trí đó chính là "
                + "id. Xoá " + id + " thì " + (id + 1) + " tụt xuống " + id + ", và mọi "
                + "vật phẩm đang mang chỉ số từ " + id + " trở lên sẽ âm thầm đổi sang "
                + "chỉ số khác — không có cách nào phát hiện ngoài việc người chơi kêu.\n\n"
                + "Chỉ xoá được chỉ số CUỐI CÙNG của bảng, vì phía sau nó không còn ai "
                + "để tụt. Muốn ngừng dùng chỉ số ở giữa thì đổi tên nó, ví dụ thêm "
                + "\"(không dùng)\" vào đầu.";
    }

    /**
     * Tăng phiên bản bảng vật phẩm để client tải lại tên chỉ số.
     *
     * <p>Tên chỉ số được in lên vật phẩm ở phía client; không tăng phiên bản thì
     * người chơi vẫn thấy tên cũ cho tới khi xoá bộ nhớ đệm.</p>
     */
    private static void tangPhienBanVatPham() {
        try {
            long v = ConfigDAO.num(ConfigDAO.VS_ITEM);
            ConfigDAO.set(ConfigDAO.VS_ITEM, String.valueOf((v + 1) % 128));
        } catch (Exception ex) {
            Logger.logException(ChiSoOptionDAO.class, ex, "Lỗi tăng phiên bản vật phẩm");
        }
    }
}
