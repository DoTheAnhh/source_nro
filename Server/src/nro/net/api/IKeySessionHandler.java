package nro.net.api;

import nro.net.io.Message;

/**
 * Bắt tay khoá mã hoá giữa server và client.
 *
 * <p>Giao thức NRO không dùng TLS. Thay vào đó mỗi byte được XOR với một byte
 * của khoá, con trỏ khoá chạy vòng. Khoá được trao đổi <b>một lần</b> ở gói tin
 * đầu tiên:</p>
 * <pre>
 * client  -&gt; server : Message(CommandMessage.GET_SESSION_ID)
 * server  -&gt; client : sendKey()   -- gửi mảng khoá dạng thô
 * client            : setKey()    -- ghi nhận khoá
 * ... từ đây mọi gói tin hai chiều đều được XOR ...
 * </pre>
 *
 * <p>Cài đặt: {@code nro.net.io.MyKeyHandler}. Lưu ý khoá mặc định trong
 * {@code Session} là một chuỗi cố định nhúng thẳng trong mã nguồn — đây là
 * <b>làm rối, không phải bảo mật</b>; ai đọc được client là suy ra được.</p>
 */
public interface IKeySessionHandler {

    /** Server gửi khoá cho client. Gọi khi nhận {@code GET_SESSION_ID}. */
    void sendKey(final ISession p0);

    /** Client trích khoá từ gói tin server gửi về và nạp vào session. */
    void setKey(ISession var1, Message var2) throws Exception;
}
