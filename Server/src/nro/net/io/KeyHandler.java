package nro.net.io;

import java.io.IOException;
import nro.net.api.ISession;
import nro.net.api.IKeySessionHandler;

/**
 * Bắt tay khoá mã hoá — bản cài đặt gốc.
 *
 * <p>Khoá <b>không</b> được gửi ở dạng thô. Nó được gửi dưới dạng
 * <b>sai phân XOR liên tiếp</b>: byte đầu gửi nguyên, mỗi byte sau gửi
 * {@code key[i] XOR key[i-1]}. Bên nhận cộng dồn ngược lại để dựng khoá gốc.
 * Đây thuần tuý là làm rối cho khó đọc khi bắt gói, không có giá trị mật mã.</p>
 *
 * <pre>
 * Gửi đi:  [len][key[0]][key[1]^key[0]][key[2]^key[1]] ...
 * Nhận về: key[i] = nhận[i] ^ key[i-1]
 * </pre>
 *
 * <p>Xem {@link MyKeyHandler} — bản mà server thực sự dùng, có nối thêm bước
 * gửi phiên bản tài nguyên.</p>
 */
public class KeyHandler implements IKeySessionHandler {

    /**
     * Server gửi khoá cho client (chiều {@code SERVER}).
     *
     * <p>Ghi bằng {@code session.doSendMessage(...)} chứ không phải
     * {@code sendMessage(...)} — tức là <b>ghi thẳng ra socket ngay</b>, bỏ qua
     * hàng đợi. Bắt buộc phải vậy, vì thread {@code Sender} chưa được bật ở thời
     * điểm này (xem {@code MySession.sendKey()}: gửi khoá xong mới
     * {@code startSend()}).</p>
     *
     * <p>Đặt cờ {@code setSentKey(true)} ở cuối — từ giây phút đó mọi byte đi qua
     * codec đều bắt đầu được XOR.</p>
     *
     * <p><b>Nợ kỹ thuật:</b> {@code catch (Exception ex) {}} rỗng. Bắt tay hỏng thì
     * client treo im lặng, không có log nào để lần.</p>
     */
    @Override
    public void sendKey(ISession session) {
        final Message msg = new Message(-27);
        try {
            final byte[] KEYS = session.getKey();
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            session.doSendMessage(msg);
            msg.cleanup();
            session.setSentKey(true);
        } catch (Exception ex) {
        }
    }

    /**
     * Nhận khoá từ gói tin (chiều {@code CLIENT}) và giải sai phân về khoá gốc.
     *
     * <p>Vòng lặp thứ hai chính là bước giải: {@code KEYS[j+1] ^= KEYS[j]}, chạy
     * tăng dần nên mỗi bước đã có {@code KEYS[j]} là giá trị gốc.</p>
     *
     * <p>Chỉ dùng khi server đóng vai client đi kết nối ra ngoài. Với luồng
     * người chơi bình thường, hàm này không bao giờ chạy.</p>
     */
    @Override
    public void setKey(ISession session, Message message) throws Exception {
        try {
            int b = message.reader().readByte();
            byte[] KEYS = new byte[b];
            for (int i = 0; i < b; ++i) {
                KEYS[i] = message.reader().readByte();
            }
            for (int j = 0; j < KEYS.length - 1; j++) {
                KEYS[j + 1] = (byte) (KEYS[j + 1] ^ KEYS[j]);
            }
            session.setKey(KEYS);
            session.setSentKey(true);
        } catch (IOException ex) {
        }
    }
}
