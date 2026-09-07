package nro.net;

import lombok.NonNull;
import lombok.Setter;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import nro.net.api.IMessageHandler;
import nro.net.api.ISession;
import nro.net.io.Message;

/**
 * Thread <b>xử lý nghiệp vụ</b> của một session — chân giữa của tam giác
 * Collector/QueueHandler/Sender.
 *
 * <p>{@link Collector} chỉ đọc và bỏ vào đây; lớp này mới gọi
 * {@link IMessageHandler#onMessage} tức là {@code nro.server.Controller}.
 * Tách như vậy để logic game chạy chậm không làm nghẽn socket.</p>
 *
 * <p><b>Bảo đảm thứ tự:</b> mỗi session có đúng một thread ở đây, nên các gói tin
 * <i>của cùng một người chơi</i> luôn được xử lý theo đúng thứ tự nhận.
 * Nhưng giữa các người chơi khác nhau thì hoàn toàn song song — mọi trạng thái
 * dùng chung phải tự lo đồng bộ.</p>
 *
 * <p><b>Chống lụt:</b> hàng đợi chặn ở 500 gói; vượt ngưỡng thì gói mới bị
 * <i>bỏ lặng lẽ</i> (xem {@link #addMessage}).</p>
 */
public class QueueHandler implements Runnable {

    /** Session sở hữu thread này. */
    private ISession session;

    /** Hàng đợi gói tin đến, tối đa 500 phần tử. */
    private BlockingDeque<Message> messages;

    /** Nơi xử lý nghiệp vụ. Với server luôn là {@code nro.server.Controller}. */
    @Setter
    private IMessageHandler messageHandler;

    /** @param session không được {@code null} (lombok {@code @NonNull} kiểm tra) */
    public QueueHandler(@NonNull ISession session) {
        try {
            this.session = session;
            this.messages = new LinkedBlockingDeque<>();
        } catch (Exception ignored) {
        }
    }

    /**
     * Vòng xử lý: rút cạn hàng đợi, ngủ 33ms, lặp lại.
     *
     * <p><b>Vấn đề hiệu năng:</b> mẫu "kiểm tra rồi ngủ" này thêm tới <b>33ms độ
     * trễ</b> cho mọi gói tin đến khi hàng đợi đang rỗng — tức là gần như mọi gói
     * lúc server rảnh. Đúng ra nên dùng {@code messages.take()} để chặn: gói tới
     * là xử lý ngay, và không tốn CPU quay vòng khi rỗng.</p>
     *
     * <p>Chú thích {@code ~30FPS} trong mã gốc phản ánh tư duy vòng lặp game,
     * nhưng đây không phải vòng render — không có lý do gì phải giới hạn nhịp.</p>
     *
     * <p>{@code poll(5, SECONDS)} bên trong thực tế không bao giờ chờ, vì chỉ vào
     * đó khi {@code !messages.isEmpty()}.</p>
     */
    @Override
    public void run() {
        try {
            while (session.isConnected()) {
                while (!messages.isEmpty()) {
                    Message message = messages.poll(5, TimeUnit.SECONDS);
                    if (message != null) {
                        this.messageHandler.onMessage(this.session, message);
                        message.cleanup();
                    }
                }
                TimeUnit.MILLISECONDS.sleep(33); //~30FPS
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Xếp một gói tin vào hàng đợi. Gọi từ thread {@link Collector}.
     *
     * <p><b>Ngưỡng 500 là van chống lụt:</b> client spam nhanh hơn tốc độ server
     * xử lý thì gói vượt ngưỡng bị bỏ. Bỏ <i>lặng lẽ</i> — không log, không ngắt
     * kết nối, không đếm. Người chơi chỉ thấy thao tác của mình "mất tăm".
     * Nên có ít nhất một dòng log ở đây.</p>
     */
    public void addMessage(Message msg) {
        try {
            if (session.isConnected() && messages.size() < 500) {
                messages.add(msg);
            }
        } catch (Exception ignored) {
        }
    }

    /** Xoá sạch gói tin còn chờ. Gọi khi session ngắt. */
    public void close() {
        if (messages != null) {
            messages.clear();
        }
    }

    /** Xoá tham chiếu để GC thu hồi. Gọi sau {@link #close()}. */
    public void dispose() {
        this.session = null;
        this.messages = null;
    }
}
