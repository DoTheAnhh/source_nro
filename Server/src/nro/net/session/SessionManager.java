package nro.net.session;

import java.util.ArrayList;
import nro.net.api.ISession;
import java.util.List;

/**
 * Sổ đăng ký mọi session TCP đang mở.
 *
 * <p>Khác với {@code nro.server.Client} (theo dõi <b>người chơi đã đăng nhập</b>),
 * lớp này theo dõi <b>kết nối</b> — kể cả kết nối chưa đăng nhập hoặc không bao
 * giờ đăng nhập. Vì thế {@code getNumSession()} luôn lớn hơn hoặc bằng số người
 * chơi online.</p>
 *
 * <p><b>Cảnh báo an toàn luồng.</b> Danh sách bên dưới là {@code ArrayList} trần,
 * <i>không đồng bộ</i>, nhưng bị ghi từ thread {@code Network} (khi accept) và
 * từ mọi thread {@code Collector} (khi client rớt). Ghi đồng thời có thể làm
 * hỏng cấu trúc trong, và duyệt danh sách trong lúc có kết nối mới có thể ném
 * {@code ConcurrentModificationException}. Nên thay bằng
 * {@code CopyOnWriteArrayList} — sửa một dòng, và mẫu dùng ở đây (đọc nhiều,
 * ghi ít) rất hợp với nó.</p>
 */
public class SessionManager {

    private static SessionManager instance;

    /** Mọi session đang mở, theo thứ tự kết nối. */
    private final List<ISession> sessions;

    /**
     * Singleton.
     *
     * <p><b>Không an toàn luồng:</b> {@code if (instance == null)} không có khoá.
     * Hai thread gọi cùng lúc lúc khởi động có thể tạo <b>hai</b> sổ đăng ký khác
     * nhau, và từ đó nửa số session biến mất khỏi mọi phép đếm.
     * Cách sửa gọn nhất: {@code private static final SessionManager instance = new SessionManager();}</p>
     */
    public static SessionManager gI() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public SessionManager() {
        this.sessions = new ArrayList<>();
    }

    /** Ghi nhận một session mới. Gọi từ {@code Network.run()} sau khi accept. */
    public void putSession(ISession session) {
        this.sessions.add(session);
    }

    /** Gỡ session. Gọi từ {@code Session.dispose()} khi kết nối đóng hẳn. */
    public void removeSession(ISession session) {
        this.sessions.remove(session);
    }

    /**
     * Trả về danh sách gốc, <b>không phải bản sao</b>. Người gọi sửa được trực tiếp,
     * và duyệt nó trong lúc có kết nối vào/ra là không an toàn.
     */
    public List<ISession> getSessions() {
        return this.sessions;
    }

    /**
     * Tìm session theo số hiệu.
     *
     * <p><b>Có lỗi logic.</b> Nhánh {@code if (session.getID() > id) throw} giả định
     * danh sách <i>luôn sắp tăng dần theo ID</i> để thoát sớm. Giả định đó sai:
     * {@link #removeSession} tạo lỗ, và tuy ID cấp tăng dần nhưng thứ tự <i>rời
     * đi</i> thì tuỳ ý. Thực tế nó vẫn còn đúng vì phần tử chỉ bị xoá chứ không
     * chèn giữa — nhưng đây là quả bom hẹn giờ nếu sau này có ai thêm session vào
     * giữa danh sách. Bỏ nhánh thoát sớm đó đi thì hàm chậm hơn chút nhưng luôn đúng.</p>
     *
     * @throws Exception khi không tìm thấy — dùng ngoại lệ cho một kết quả bình
     *         thường (không có) là đắt và khó dùng; trả {@code null} hoặc
     *         {@code Optional} sẽ hợp hơn.
     */
    public ISession findByID(long id) throws Exception {
        if (this.sessions.isEmpty()) {
            throw new Exception("Session " + id + " does not exist");
        }
        for (ISession session : this.sessions) {
            if (session.getID() > id) {
                throw new Exception("Session " + id + " does not exist");
            }
            if (session.getID() == id) {
                return session;
            }
        }
        throw new Exception("Session " + id + " does not exist");
    }

    /** Số kết nối đang mở (kể cả chưa đăng nhập). */
    public int getNumSession() {
        return this.sessions.size();
    }
}
