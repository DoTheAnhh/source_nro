package nro.net.io;

/*
 * @Author: DoTheAnh
 */

import nro.data.DataGame;
import nro.net.api.ISession;
import nro.net.session.MySession;

/**
 * Bản bắt tay khoá mà server thực sự dùng.
 *
 * <p>Gắn vào session trong {@code ServerManager.activeServerSocket()}.
 * Nối thêm vào {@link KeyHandler} bước gửi <b>phiên bản tài nguyên</b> ngay sau
 * khi khoá được thiết lập — đây là 2 gói tin đầu tiên client nhận sau bắt tay,
 * và chúng quyết định client có phải tải lại ảnh/hiệu ứng hay không.</p>
 *
 * <p><b>Thứ tự bắt buộc:</b> phải gửi khoá <i>trước</i>, rồi mới gửi dữ liệu
 * phiên bản. Nếu đảo lại, client chưa có khoá sẽ giải mã ra rác.</p>
 */
public class MyKeyHandler extends KeyHandler {

    /**
     * Gửi khoá (uỷ quyền cho lớp cha) rồi gửi tiếp 2 gói phiên bản tài nguyên.
     *
     * <ul>
     *   <li>{@code sendDataImageVersion} — phiên bản bộ ảnh; client so với bản cục
     *       bộ để quyết định tải lại.</li>
     *   <li>{@code sendVersionRes} — phiên bản tài nguyên chung (map, hiệu ứng, âm thanh).</li>
     * </ul>
     *
     * <p>Ép kiểu về {@link MySession} là an toàn ở đây vì
     * {@code Network.setTypeSessionClone(MySession.class)} bảo đảm mọi session
     * phía server đều là {@code MySession}.</p>
     */
    @Override
    public void sendKey(ISession session) {
        super.sendKey(session);
        DataGame.sendDataImageVersion((MySession) session);
        DataGame.sendVersionRes((MySession) session);
    }

}
