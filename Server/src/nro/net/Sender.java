package nro.net;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import nro.net.api.IMessageSendCollect;
import nro.net.api.ISession;
import nro.net.io.Message;

/**
 * Thread <b>ghi</b> của một session — chân phải của tam giác
 * Collector/QueueHandler/Sender.
 *
 * <p>Logic game gọi {@code session.sendMessage(msg)} là trả về ngay; gói tin chỉ
 * được xếp vào hàng đợi. Thread này mới thật sự mã hoá và ghi ra socket.
 * Nhờ vậy một client mạng chậm không làm treo vòng lặp game.</p>
 *
 * <p><b>Bật muộn hơn hai thread kia:</b> {@code Collector} và {@code QueueHandler}
 * bật ngay lúc accept, còn {@code Sender} chỉ bật sau khi bắt tay khoá xong
 * (xem {@code MySession.sendKey()}). Vì thế gói khoá phải gửi bằng
 * {@code doSendMessage} ghi thẳng, không qua hàng đợi.</p>
 */
public final class Sender implements Runnable {

    /** Session sở hữu thread này. */
    private ISession session;

    /** Hàng đợi gói tin chờ gửi. <b>Không giới hạn kích thước</b> — xem {@link #sendMessage}. */
    private BlockingDeque<Message> messages;

    /** Luồng ghi của socket. */
    private DataOutputStream dos;

    /** Codec mã hoá. Cùng đối tượng với bên {@link Collector}. */
    private IMessageSendCollect sendCollect;

    /** @param session, socket đều không được {@code null} (lombok {@code @NonNull}) */
    public Sender(@NonNull ISession session, @NonNull Socket socket) {
        try {
            this.session = session;
            this.messages = new LinkedBlockingDeque<>();
            this.setSocket(socket);
        } catch (Exception ignored) {
        }
    }

    /** Gắn (hoặc thay) socket và mở lại luồng ghi. Trả {@code this} để gọi nối chuỗi. */
    public Sender setSocket(@NonNull Socket socket) {
        try {
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ignored) {
        }
        return this;
    }

    /**
     * Vòng gửi: rút cạn hàng đợi, ngủ 33ms, lặp lại.
     *
     * <p><b>Hệ quả thấy được trong game:</b> nhịp ngủ 33ms này là độ trễ cộng thêm
     * cho <i>mọi</i> phản hồi của server — đòn đánh, tin nhắn chat, cập nhật máu.
     * Kết hợp với 33ms bên {@link QueueHandler} là tới <b>66ms</b> trễ do chính
     * server tự thêm vào, chưa tính đường truyền.</p>
     *
     * <p>Cách sửa: dùng {@code messages.take()} chặn thay cho vòng quay có ngủ.</p>
     *
     * <p>Gọi {@code message.cleanup()} sau khi gửi để trả bộ đệm — không làm sẽ
     * rò bộ nhớ dần theo số gói tin.</p>
     */
    @Override
    public void run() {
        byte maCuoi = 0;
        try {
            while (session.isConnected()) {
                while (!messages.isEmpty()) {
                    Message message = messages.poll(5, TimeUnit.SECONDS);
                    if (message != null) {
                        maCuoi = message.command;
                        doSendMessage(message);
                        message.cleanup();
                    }
                }
                TimeUnit.MILLISECONDS.sleep(33); //~30FPS
            }
        } catch (InterruptedException dungLuong) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // GHI LAI thay vi nuot.
            //
            // Ban truoc la `catch (Exception ignored) {}` bao quanh CA vong
            // lap: mot lan ghi hong la thoat vong, luong gui chet han, va tu
            // do ket noi coi nhu dut — ma khong de lai mot dong log nao. Moi
            // loi kieu "tu nhien client van g" deu bien mat o day.
            //
            // Ghi kem MA LENH cua goi dang gui: do la manh thong tin duy nhat
            // chi ra tinh nang nao lam hong ket noi.
            nro.core.log.Logger.logException(Sender.class, e,
                    "Luong gui dung han khi gui goi ma lenh " + maCuoi);
        }
    }

    /**
     * Mã hoá và ghi thẳng một gói tin ra socket <b>ngay tại thread đang gọi</b>.
     *
     * <p><b>Bắt buộc {@code synchronized}.</b> Codec giữ con trỏ khoá có trạng thái
     * ({@code curW} tăng theo từng byte). Hai thread cùng ghi sẽ đan xen byte, làm
     * lệch con trỏ khoá, và client giải mã ra rác từ đó về sau — biểu hiện là
     * "tự nhiên client văng" mà không có lỗi nào.</p>
     *
     * <p>Dùng trực tiếp cho các gói phải đi trước khi thread gửi được bật
     * (gói khoá). Ngoài trường hợp đó nên dùng {@link #sendMessage}.</p>
     */
    public synchronized void doSendMessage(Message message) throws Exception {
        this.sendCollect.doSendMessage(this.session, this.dos, message);
    }

    /**
     * Xếp gói tin vào hàng đợi gửi. Trả về ngay, không chặn.
     *
     * <p><b>Hàng đợi không có trần.</b> Khác {@link QueueHandler#addMessage} có
     * chặn ở 500, ở đây một client "đọc chậm" (hoặc treo mà TCP chưa báo đứt) làm
     * hàng đợi phình vô hạn cho tới khi hết bộ nhớ. Nên có giới hạn và ngắt kết
     * nối khi vượt.</p>
     */
    public void sendMessage(Message msg) {
        try {
            if (session.isConnected()) {
                messages.add(msg);
            }
        } catch (Exception ignored) {
        }
    }

    /** Gắn codec mã hoá. Gọi từ {@code Session.setSendCollect}. */
    public void setSend(IMessageSendCollect sendCollect) {
        this.sendCollect = sendCollect;
    }

    /**
     * Bỏ hết gói đang chờ và đóng luồng ghi.
     *
     * <p>Lưu ý: gói còn trong hàng đợi bị <b>vứt bỏ</b>, không cố gửi nốt.
     * Nên khi kick người chơi, thông báo kick có thể không bao giờ tới nơi —
     * đó là lý do {@code Client.safePut} phải {@code sleep(1000)} trước khi kick.</p>
     */
    public void close() {
        this.messages.clear();
        if (this.dos != null) {
            try {
                this.dos.close();
            } catch (IOException ignored) {
            }
        }
    }

    /** Xoá tham chiếu để GC thu hồi. Gọi sau {@link #close()}. */
    public void dispose() {
        this.session = null;
        this.messages = null;
        this.sendCollect = null;
        this.dos = null;
    }
}
