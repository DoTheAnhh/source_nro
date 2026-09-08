package nro.repository.dao;

import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Những lựa chọn hiển thị, lưu <b>trên tài khoản</b>.
 *
 * <h2>Vì sao trên tài khoản chứ không phải từng nhân vật</h2>
 *
 * <p>Ẩn/hiện hợp thể là một tuỳ chọn hiển thị, giống âm thanh hay đồ hoạ — nó
 * thuộc về người ngồi chơi chứ không thuộc về một nhân vật cụ thể.</p>
 *
 * <h2>Vì sao là cột của {@code account} chứ không phải {@code player}</h2>
 *
 * <p>{@code PlayerDAO.updatePlayer} tự ghi chú là <b>hàm đắt nhất trong toàn bộ
 * hệ thống</b> — nó ghi lại hàng chục cột JSON cho mỗi nhân vật, mỗi lần lưu.
 * Thêm cột vào đó là bắt mọi lần lưu cõng thêm một giá trị mà cả đời nhân vật
 * may ra đổi vài lần; lại còn phải sửa ba chỗ theo đúng ghi chú của
 * {@code PlayerDAO}, sót một là mất dữ liệu âm thầm.</p>
 *
 * <p>Cột trên {@code account} thì chỉ ghi đúng lúc người chơi bấm đổi, và đọc
 * đúng một lần lúc đăng nhập.</p>
 */
public class CaiDatNguoiChoiDAO {

    private static boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        try {
            // Them cot vao chinh bang `account`.
            //
            // MariaDB 10.4 co "ADD COLUMN IF NOT EXISTS" nen chay lai bao nhieu
            // lan cung khong sao — khong can kiem tra truoc.
            ConnectDB.executeUpdate("ALTER TABLE account"
                    + " ADD COLUMN IF NOT EXISTS hien_thi_hop_the"
                    + " TINYINT(1) NOT NULL DEFAULT 1");
            ConnectDB.executeUpdate("ALTER TABLE account"
                    + " ADD COLUMN IF NOT EXISTS hien_thi_aura_rieng"
                    + " TINYINT(1) NOT NULL DEFAULT 1");
            daTaoBang = true;
        } catch (Exception ex) {
            Logger.logException(CaiDatNguoiChoiDAO.class, ex,
                    "Lỗi tạo bảng player_cai_dat");
        }
    }

    /**
     * Nhân vật này có hiện đồ hợp thể không.
     *
     * <p>Chưa có dòng nào thì trả về 1 — nhân vật mới mặc định <b>hiện</b>, đúng
     * như giá trị khởi tạo của {@code Player.hienThiHopThe}. Nhờ vậy mọi nhân
     * vật đã có từ trước khi có bảng này không bị đột nhiên ẩn mất hợp thể.</p>
     */
    public static int docHienThiHopThe(long accountId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT hien_thi_hop_the"
                    + " FROM account WHERE id = ?", accountId);
            if (rs.next()) {
                return rs.getInt("hien_thi_hop_the");
            }
        } catch (Exception ex) {
            Logger.logException(CaiDatNguoiChoiDAO.class, ex,
                    "Lỗi đọc cài đặt hợp thể");
        } finally {
            dong(rs);
        }
        return 1;
    }

    public static void ghiHienThiHopThe(long accountId, int giaTri) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("UPDATE account SET hien_thi_hop_the = ?"
                    + " WHERE id = ?", giaTri, accountId);
        } catch (Exception ex) {
            Logger.logException(CaiDatNguoiChoiDAO.class, ex,
                    "Lỗi ghi cài đặt hợp thể");
        }
    }

    /**
     * Nhân vật này có hiện hào quang được trao riêng không.
     *
     * <p>Chưa có cột hay chưa có dòng thì trả {@code 1} — mặc định <b>hiện</b>,
     * đúng như giá trị khởi tạo của {@code Player.hienThiAuraRieng}. Người vừa
     * được trao hào quang mà thấy ngay là đúng ý; muốn giấu thì tự tắt.</p>
     */
    public static int docHienThiAuraRieng(long accountId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT hien_thi_aura_rieng"
                    + " FROM account WHERE id = ?", accountId);
            if (rs.next()) {
                return rs.getInt("hien_thi_aura_rieng");
            }
        } catch (Exception ex) {
            Logger.logException(CaiDatNguoiChoiDAO.class, ex,
                    "Lỗi đọc cài đặt hào quang riêng");
        } finally {
            dong(rs);
        }
        return 1;
    }

    public static void ghiHienThiAuraRieng(long accountId, int giaTri) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("UPDATE account SET hien_thi_aura_rieng = ?"
                    + " WHERE id = ?", giaTri, accountId);
        } catch (Exception ex) {
            Logger.logException(CaiDatNguoiChoiDAO.class, ex,
                    "Lỗi ghi cài đặt hào quang riêng");
        }
    }

    private static void dong(CrisResultSet rs) {
        try {
            if (rs != null) {
                rs.dispose();
            }
        } catch (Exception ignored) {
            // Dong that bai thi cung khong lam gi duoc them.
        }
    }
}
