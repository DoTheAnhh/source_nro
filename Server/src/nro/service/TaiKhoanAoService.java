package nro.service;

import nro.core.log.Logger;
import nro.core.util.Util;
import nro.net.session.MySession;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Tài khoản ảo cho nút <b>"Chơi mới"</b> ở màn chọn máy chủ.
 *
 * <h2>Vì sao cần lớp này</h2>
 *
 * <p>Nút "Chơi mới" gửi lên gói {@code -101}. Trong {@code Controller} lời gọi
 * xử lý gói đó <b>đã bị chú thích</b> và hàm nhận nó thì không còn tồn tại, nên
 * máy chủ nhận gói rồi im lặng. Client thì mở hộp chờ ngay sau khi gửi và chỉ
 * đóng hộp khi có trả lời — không có trả lời thì hộp quay mãi. Đó chính là
 * triệu chứng "bấm Chơi mới rồi treo".</p>
 *
 * <h2>Vì sao không dùng đăng ký tự động sẵn có</h2>
 *
 * <p>{@code GodGK.taoTaiKhoanMoi} đã tạo được tài khoản ngay lúc đăng nhập,
 * nhưng nó bắt mật khẩu <b>từ ba ký tự trở lên</b>. Client lại gán cứng mật
 * khẩu tài khoản ảo là {@code "a"} — đúng một ký tự (xem {@code LoginScr
 * .doLogin}). Đi qua đường đó là bị chặn ngay ở khâu kiểm tra tên. Nên ở đây
 * tạo thẳng bản ghi rồi mới gọi đăng nhập bình thường: phần xác thực, nạp nhân
 * vật, vào bản đồ vẫn dùng chung một đường với mọi người chơi khác.</p>
 *
 * <h2>Tài khoản dùng một lần</h2>
 *
 * <p>Client <b>không</b> lưu lại tên máy chủ sinh ra — chỗ duy nhất nó ghi
 * {@code userAo} là ghi chuỗi rỗng. Nên mỗi lần bấm "Chơi mới" là một tài khoản
 * mới. Đúng với ý nghĩa của nút, nhưng cần biết trước: chơi xong thoát ra là
 * không quay lại nhân vật đó được nữa, trừ khi nhớ tên.</p>
 */
public final class TaiKhoanAoService {

    private TaiKhoanAoService() {
    }

    /**
     * Mật khẩu của mọi tài khoản ảo.
     *
     * <p>Gán cứng bên client ({@code LoginScr.doLogin} đặt {@code text2 = "a"}),
     * nên máy chủ buộc phải dùng đúng giá trị này. Đổi ở đây mà không sửa client
     * là tài khoản tạo xong đăng nhập không được.</p>
     */
    private static final String MAT_KHAU = "a";

    /** Tiền tố tên, để nhìn danh sách tài khoản là biết ngay cái nào là ảo. */
    private static final String TIEN_TO = "ao";

    /** Số lần bốc tên trước khi chịu thua. */
    private static final int SO_LAN_BOC = 30;

    /**
     * Xử lý gói {@code -101}.
     *
     * @param session phiên vừa gửi gói
     * @param tenCu   tên client gửi kèm. Rỗng nghĩa là xin tài khoản mới; có
     *                tên nghĩa là client còn nhớ tài khoản ảo cũ và muốn vào
     *                lại đúng nhân vật đó.
     */
    public static void dangNhap(MySession session, String tenCu) {
        if (session == null) {
            return;
        }
        String ten = tenCu == null ? "" : tenCu.trim();
        if (!ten.isEmpty()) {
            // Vao lai tai khoan ao cu. Khong tao gi ca — sai ten thi de duong
            // dang nhap thuong bao loi nhu binh thuong.
            session.login(ten, MAT_KHAU);
            return;
        }

        ten = sinhTen();
        if (ten == null) {
            Service.gI().sendThongBaoOK(session,
                    "Không tạo được tài khoản mới, thử lại giúp mình.");
            Service.gI().sendLoginFail(session, false);
            return;
        }
        if (!taoTaiKhoan(ten)) {
            Service.gI().sendThongBaoOK(session,
                    "Không tạo được tài khoản mới, thử lại giúp mình.");
            Service.gI().sendLoginFail(session, false);
            return;
        }
        Logger.success("Tạo tài khoản ảo cho nút \"Chơi mới\": " + ten + "\n");
        session.login(ten, MAT_KHAU);
    }

    /**
     * Bốc một tên chưa ai dùng.
     *
     * <p>Bốc rồi hỏi lại cơ sở dữ liệu chứ không đếm tăng dần: đếm tăng dần thì
     * hai phiên bấm cùng lúc sẽ ra cùng một số. Cột {@code username} có ràng
     * buộc duy nhất nên trường hợp hiếm hoi hai phiên bốc trúng nhau vẫn bị
     * chặn ở khâu ghi, chứ không tạo ra hai tài khoản trùng tên.</p>
     *
     * @return tên dùng được, hoặc {@code null} nếu bốc mãi không ra
     */
    private static String sinhTen() {
        for (int lan = 0; lan < SO_LAN_BOC; lan++) {
            // 8 chu so -> ten dai 10, nam gon trong varchar(20) va thoa luat
            // dat ten (4..20 ky tu, chi chu va so).
            String ten = TIEN_TO + Util.nextInt(10_000_000, 99_999_999);
            if (!daCo(ten)) {
                return ten;
            }
        }
        return null;
    }

    private static boolean daCo(String ten) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id FROM account WHERE username = ? LIMIT 1", ten);
            return rs.next();
        } catch (Exception ex) {
            Logger.logException(TaiKhoanAoService.class, ex,
                    "Lỗi kiểm tra tên tài khoản ảo");
            // Khong biet chac thi coi nhu DA CO, de boc ten khac. Bao la chua
            // co roi ghi de len tai khoan nguoi khac thi tai hai hon nhieu.
            return true;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                }
            }
        }
    }

    /**
     * Ghi bản ghi tài khoản.
     *
     * <p>Bốn cột vé là {@code NOT NULL} mà không có giá trị mặc định nên phải
     * đặt tay, giống hệt câu lệnh trong {@code GodGK.taoTaiKhoanMoi} — thiếu
     * một cột là cả câu bị từ chối.</p>
     */
    private static boolean taoTaiKhoan(String ten) {
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO account (username, password, vetuan, vethang,"
                    + " vetuan_expire, vethang_expire) VALUES (?, ?, 0, 0, 0, 0)",
                    ten, MAT_KHAU);
            return true;
        } catch (Exception ex) {
            Logger.logException(TaiKhoanAoService.class, ex,
                    "Lỗi tạo tài khoản ảo");
            return false;
        }
    }
}
