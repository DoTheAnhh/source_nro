package nro.net.io;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import nro.net.api.ISession;
import nro.net.api.IMessageSendCollect;

/**
 * Codec thật của giao thức NRO — dịch giữa {@link Message} và byte trên socket.
 *
 * <p>Mỗi session có <b>một</b> đối tượng riêng (tạo trong
 * {@code ServerManager.activeServerSocket()}), vì hai con trỏ khoá bên dưới là
 * trạng thái riêng của từng kết nối.</p>
 *
 * <p><b>Sơ đồ khung tin:</b></p>
 * <pre>
 * Bình thường (2 byte size, tối đa 65.535 byte payload):
 *   [cmd][size_hi][size_lo][payload...]
 *
 * Lệnh payload lớn (3 byte size, tối đa 16 MiB):
 *   [cmd][size_b0 - 128][size_b1 - 128][size_b2 - 128][payload...]
 * </pre>
 *
 * <p>Sau khi bắt tay khoá, <b>từng byte</b> ở trên đều bị XOR với khoá.</p>
 */
public class MessageSendCollect implements IMessageSendCollect {

    /**
     * Do dai than goi toi da chap nhan tu client, byte.
     *
     * <p>Goi lon nhat may chu tu gui la bang mau vat pham (ma lenh -28), va
     * no di duong ba byte do dai. Chieu tu client len thi khong co goi nao
     * lon: dai nhat cung chi la mot cau chat. Dat 8 KiB la rong rai gap
     * nhieu lan nhu cau that.</p>
     */
    private static final int GOI_TOI_DA = 8192;

    /**
     * Con trỏ khoá cho chiều <b>đọc</b>. Tăng một bước mỗi byte giải mã,
     * chạy vòng khi hết độ dài khoá.
     */
    private int curR;

    /**
     * Con trỏ khoá cho chiều <b>ghi</b>. Độc lập hoàn toàn với {@link #curR}
     * vì hai chiều đọc/ghi không đồng bộ với nhau.
     */
    private int curW;

    /** Bắt đầu cả hai con trỏ khoá từ đầu mảng khoá. */
    public MessageSendCollect() {
        this.curR = 0;
        this.curW = 0;
    }

    /**
     * Đọc trọn một gói tin. Chặn cho tới khi đủ byte hoặc socket đứt.
     *
     * <p>Trình tự: đọc 1 byte lệnh → đọc độ dài (2 byte đã mã hoá nếu đã bắt tay,
     * ngược lại là {@code readUnsignedShort} thô) → đọc đủ {@code size} byte
     * (vòng lặp vì {@code read()} có thể trả về từng phần) → giải mã payload.</p>
     *
     * <p><b>Lưu ý:</b> nhánh chưa bắt tay dùng {@code readUnsignedShort} nên gói
     * đầu tiên ({@code GET_SESSION_ID}) luôn đọc được ở dạng thô.</p>
     *
     * @return gói tin đã giải mã; con trỏ đọc của nó ở vị trí 0
     */
    @Override
    public Message readMessage(ISession session, DataInputStream dis) throws Exception {
        byte cmd = dis.readByte();
        if (session.sentKey()) {
            cmd = this.readKey(session, cmd);
        }
        int size;
        if (session.sentKey()) {
            final byte b1 = dis.readByte();
            final byte b2 = dis.readByte();
            size = ((this.readKey(session, b1) & 0xFF) << 8 | (this.readKey(session, b2) & 0xFF));
        } else {
            size = dis.readUnsignedShort();
        }
        // Tran do dai goi.
        //
        // `size` doc thang tu day dan nen no la so cua BEN KIA, khong phai
        // cua minh. Duong hai byte toi da 65535 nen cap phat khong the vo
        // han, nhung mot khach sua doi van co the bom goi 64 KiB lien tuc.
        // Chan o day thi loi hien ra ngay, va phien do bi dong.
        if (size < 0 || size > GOI_TOI_DA) {
            throw new java.io.IOException("Goi tin dai bat thuong: " + size
                    + " byte, ma lenh " + cmd);
        }
        final byte[] data = new byte[size];
        int byteRead = 0;
        while (byteRead < size) {
            int len = dis.read(data, byteRead, size - byteRead);
            // HET DONG giua chung thi phai NEM, khong duoc tra ve goi do.
            //
            // Ban cu chi thoat vong lap khi len == -1, roi van dung mang
            // dang do lam than goi. Ben xu ly nhan mot goi dung ma lenh nhung
            // than rong hoac cut — sinh ra loi o mot noi hoan toan khac, rat
            // kho lan ra nguyen nhan.
            if (len < 0) {
                throw new java.io.EOFException("Dut giua goi: doc duoc "
                        + byteRead + "/" + size + " byte");
            }
            byteRead += len;
        }
        if (session.sentKey()) {
            for (int i = 0; i < data.length; ++i) {
                data[i] = this.readKey(session, data[i]);
            }
        }
        return new Message(cmd, data);
    }

    /**
     * Giải mã một byte: {@code b XOR key[curR]}, rồi đẩy {@code curR} lên một bước
     * (chạy vòng).
     *
     * <p>Vì có trạng thái nên <b>chỉ được gọi từ đúng một thread</b> — thực tế là
     * thread {@code Collector} của session đó.</p>
     */
    @Override
    public byte readKey(ISession session, byte b) {
        final byte i = (byte) ((session.getKey()[this.curR++] & 0xFF) ^ (b & 0xFF));
        if (this.curR >= session.getKey().length) {
            this.curR %= session.getKey().length;
        }
        return i;
    }

    /**
     * Mã hoá và ghi trọn một gói tin ra socket, rồi {@code flush()}.
     *
     * <p><b>Chỗ dễ sai nhất của cả giao thức</b> nằm ở khối {@code if} chọn số byte
     * độ dài. Các lệnh {@code -32, -66, -74, 11, -67, -87, 66, -28} dùng <b>3 byte</b>
     * độ dài (mỗi byte bị trừ 128 sau khi mã hoá). Client Unity phải có đúng cùng
     * danh sách; lệch một mã lệnh là mọi gói sau đó lệch khung và kết nối hỏng.</p>
     *
     * <p>{@code -28} nằm trong danh sách vì nó tải trọn bảng item template,
     * vượt 64 KiB.</p>
     *
     * <p>Hàm này <b>sửa đổi trực tiếp</b> mảng {@code data} của gói tin khi mã hoá,
     * nên một {@link Message} không thể gửi lại lần thứ hai.</p>
     *
     * <p><b>Nợ kỹ thuật:</b> {@code catch (IOException ex) {}} nuốt lỗi — client rớt
     * giữa chừng thì không có dấu vết gì trong log.</p>
     */
    @Override
    public void doSendMessage(ISession session, DataOutputStream dos, Message msg) throws Exception {
        try {
            final byte[] data = msg.getData();
            if (session.sentKey()) {
                final byte b = this.writeKey(session, msg.command);
                dos.writeByte(b);
            } else {
                dos.writeByte(msg.command);
            }
            if (data != null) {
                final int size = data.length;
                // Large payload commands use an encrypted 3-byte length.
                // -28 can carry the complete item-template update (> 64 KiB),
                // so both server and Unity Session_ME must treat it as extended.
                if (msg.command == -32 || msg.command == -66 || msg.command == -74 || msg.command == 11 || msg.command == -67 || msg.command == -87 || msg.command == 66
                        || msg.command == -28) {
                    nro.core.log.Logger.info("GOI_LON", "ma lenh " + msg.command
                            + " | size = " + size
                            + " | 3 byte do dai = " + (size & 0xFF) + ","
                            + ((size >> 8) & 0xFF) + "," + ((size >> 16) & 0xFF));
                    final byte b2 = this.writeKey(session, (byte) size);
                    dos.writeByte(b2 - 128);
                    final byte b3 = this.writeKey(session, (byte) (size >> 8));
                    dos.writeByte(b3 - 128);
                    final byte b4 = this.writeKey(session, (byte) (size >> 16));
                    dos.writeByte(b4 - 128);
                } else if (session.sentKey()) {
                    final int byte1 = this.writeKey(session, (byte) (size >> 8));
                    dos.writeByte(byte1);
                    final int byte2 = this.writeKey(session, (byte) (size & 0xFF));
                    dos.writeByte(byte2);
                } else {
                    dos.writeShort(size);
                }
                if (session.sentKey()) {
                    for (int i = 0; i < data.length; ++i) {
                        data[i] = this.writeKey(session, data[i]);
                    }
                }
                dos.write(data);
            } else {
                dos.writeShort(0);
            }
            dos.flush();
            msg.cleanup();
        } catch (IOException ex) {
            // Goi tin gui do dang -> luong byte lech -> client treo. Truoc gio
            // im lang tuyet doi, khong cach nao biet goi nao chet.
            nro.core.log.Logger.log("[NET] Loi ghi goi tin cmd=" + msg.command
                    + " size=" + (msg.getData() == null ? 0 : msg.getData().length)
                    + ": " + ex + "\n");
        }
    }

    /**
     * Mã hoá một byte: {@code b XOR key[curW]}, rồi đẩy {@code curW} lên một bước.
     *
     * <p>Có trạng thái — đây là lý do {@code Sender.doSendMessage} phải
     * {@code synchronized}.</p>
     */
    @Override
    public byte writeKey(final ISession session, final byte b) {
        final byte i = (byte) ((session.getKey()[this.curW++] & 0xFF) ^ (b & 0xFF));
        if (this.curW >= session.getKey().length) {
            this.curW %= session.getKey().length;
        }
        return i;
    }
}
