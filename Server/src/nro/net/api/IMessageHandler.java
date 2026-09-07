package nro.net.api;

import nro.net.io.Message;

/**
 * Nơi nghiệp vụ game nhận gói tin từ client.
 *
 * <p>Đây là ranh giới giữa <b>tầng mạng</b> và <b>tầng game</b>: mọi thứ dưới
 * interface này chỉ biết byte, mọi thứ trên nó mới biết người chơi, item, boss.</p>
 *
 * <p>Cài đặt duy nhất hiện nay là {@code nro.server.Controller} — một
 * {@code switch} khoảng 80 nhánh theo {@code Message.command}.
 * Kế hoạch tái cấu trúc là thay bằng {@code CommandRouter} tra bảng
 * {@code Map<Byte, CommandHandler>} (xem docs/03-KE-HOACH-TAI-CAU-TRUC.md,
 * giai đoạn 6).</p>
 */
public interface IMessageHandler {

    /**
     * Xử lý một gói tin.
     *
     * <p>Được gọi từ thread {@code QueueHandler} <b>riêng của từng session</b>,
     * nên nhiều người chơi chạy song song ở đây. Mọi truy cập vào trạng thái
     * dùng chung ({@code Manager.MAPS}, {@code Client}, boss...) phải tự lo đồng bộ.</p>
     *
     * @param p0 session gửi gói tin (ép kiểu về {@code MySession} để lấy {@code player})
     * @param p1 gói tin; đọc bằng {@code p1.reader()} <b>theo đúng thứ tự client ghi</b>
     */
    void onMessage(final ISession p0, final Message p1) throws Exception;
}
