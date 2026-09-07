package nro.net.session;

import java.net.Socket;
import nro.net.api.ISession;

/**
 * Tạo đối tượng session cho một socket vừa được accept.
 *
 * <p>Tồn tại để {@code Network} không phải biết lớp session cụ thể. Lớp cụ thể
 * được khai báo qua {@code Network.setTypeSessionClone(MySession.class)} và
 * được dựng ở đây bằng phản chiếu (reflection).</p>
 *
 * <p><b>Có đáng không?</b> Trong project này chỉ có <i>một</i> lớp session
 * ({@link MySession}), nên toàn bộ lớp factory + reflection có thể thay bằng
 * một dòng {@code new MySession(socket)}. Đổi lại sẽ nhanh hơn, lỗi phát hiện
 * lúc biên dịch thay vì lúc chạy, và bỏ được một lớp gián tiếp.</p>
 */
public class SessionFactory {

    private static SessionFactory instance;

    /**
     * Singleton — cũng có cùng lỗi thiếu đồng bộ như {@link SessionManager#gI()},
     * nhưng ở đây vô hại vì lớp không giữ trạng thái gì.
     */
    public static SessionFactory gI() {
        if (instance == null) {
            instance = new SessionFactory();
        }
        return instance;
    }

    /**
     * Dựng một session mới bằng reflection.
     *
     * <p>Đòi hỏi {@code clazz} có constructor <b>public</b> nhận đúng một
     * {@code Socket}. Không thoả thì lỗi chỉ lộ ra <b>lúc chạy</b>, đúng vào lúc
     * người chơi đầu tiên kết nối vào.</p>
     *
     * @param clazz lớp session, đặt qua {@code Network.setTypeSessionClone}
     * @param socket socket vừa accept
     * @throws Exception nếu không có constructor phù hợp hoặc constructor ném lỗi
     */
    public ISession cloneSession(Class clazz, Socket socket) throws Exception {
        return (ISession) clazz.getConstructor(Socket.class).newInstance(socket);
    }
}
