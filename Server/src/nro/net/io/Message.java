package nro.net.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import nro.net.api.IMessage;

/**
 * Một gói tin: mã lệnh + phần thân dữ liệu.
 *
 * <p><b>Một gói tin chỉ dùng theo MỘT chiều.</b> Hai constructor tạo ra hai loại
 * đối tượng khác hẳn nhau:</p>
 * <ul>
 *   <li>{@link #Message(byte)} — <b>chế độ ghi</b>. Chỉ có {@code os}/{@code dos};
 *       {@code is}/{@code dis} là {@code null}. Dùng khi server dựng gói gửi đi.</li>
 *   <li>{@link #Message(byte, byte[])} — <b>chế độ đọc</b>. Chỉ có {@code is}/{@code dis};
 *       {@code os}/{@code dos} là {@code null}. Do codec tạo khi nhận gói.</li>
 * </ul>
 *
 * <p>Gọi nhầm chiều là <b>NPE</b>, không phải lỗi rõ ràng: {@code getData()} trên
 * gói chế độ đọc sẽ ném vì {@code os == null}; {@code reader()} trên gói chế độ
 * ghi trả {@code null} rồi NPE ở lời gọi kế tiếp.</p>
 *
 * <p><b>Quy tắc vàng của giao thức:</b> thứ tự ghi ở server phải khớp <b>chính xác</b>
 * thứ tự đọc ở client và ngược lại. Không có tên trường, không có độ dài trường —
 * chỉ là một dòng byte thuần. Ghi thừa một {@code byte} là mọi trường sau đó
 * lệch hết.</p>
 *
 * <p><b>Vòng đời:</b> tạo → ghi/đọc → gửi/xử lý → {@link #cleanup()}. Sau khi được
 * gửi, mảng dữ liệu đã <b>bị codec XOR đè lên tại chỗ</b>, nên một gói tin
 * <i>không gửi lại lần hai được</i>. Muốn gửi cùng nội dung cho nhiều người chơi
 * thì phải tạo gói mới cho từng người.</p>
 */
public class Message implements IMessage {

    /** Mã lệnh. Xem {@code nro.server.Controller.onMessage} để biết nghĩa từng mã. */
    public byte command;

    /** Bộ đệm ghi (chế độ ghi). {@code null} ở chế độ đọc. */
    private ByteArrayOutputStream os;

    /** Lớp bọc kiểu dữ liệu cho {@link #os}. */
    private DataOutputStream dos;

    /** Bộ đệm đọc (chế độ đọc). {@code null} ở chế độ ghi. */
    private ByteArrayInputStream is;

    /** Lớp bọc kiểu dữ liệu cho {@link #is}. */
    private DataInputStream dis;

    /**
     * Tiện ích cho mã lệnh viết dạng {@code int}.
     *
     * <p>Ép về {@code byte} nên chỉ đúng với giá trị trong khoảng -128..127.
     * Truyền số ngoài khoảng sẽ bị cắt bit <b>im lặng</b> thành mã lệnh khác.</p>
     */
    public Message(int command) {
        this((byte) command);
    }

    /** Tạo gói tin <b>chế độ ghi</b> (server dựng gói để gửi đi). */
    public Message(byte command) {
        this.command = command;
        this.os = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.os);
    }

    /**
     * Tạo gói tin <b>chế độ đọc</b> từ dữ liệu đã giải mã.
     *
     * <p>Chỉ {@code MessageSendCollect.readMessage} gọi constructor này.
     * Mảng {@code data} được dùng trực tiếp, không sao chép.</p>
     */
    public Message(byte command, byte[] data) {
        this.command = command;
        this.is = new ByteArrayInputStream(data);
        this.dis = new DataInputStream(this.is);
    }

    /** Luồng ghi. {@code null} nếu gói ở chế độ đọc. */
    @Override
    public DataOutputStream writer() {
        return this.dos;
    }

    /**
     * Luồng đọc. {@code null} nếu gói ở chế độ ghi.
     *
     * <p>Con trỏ đọc <b>chung cho mọi lời gọi</b>: {@code msg.reader().readByte()}
     * hai lần sẽ đọc hai byte khác nhau, không phải cùng một byte.</p>
     */
    @Override
    public DataInputStream reader() {
        return this.dis;
    }

    /**
     * Toàn bộ phần thân đã ghi, dạng mảng byte.
     *
     * <p>Gọi trên gói chế độ đọc sẽ <b>ném NPE</b> ({@code os == null}).
     * Mỗi lần gọi tạo một bản sao mới của bộ đệm.</p>
     */
    @Override
    public byte[] getData() {
        return this.os.toByteArray();
    }

    /**
     * Ghi một số theo <b>độ rộng phụ thuộc phiên bản client</b>: 4 byte
     * ({@code int}) nếu {@code is} đúng, ngược lại 8 byte ({@code long}).
     *
     * <p>Đây là cách server hỗ trợ nhiều bản client cùng lúc. Chỗ gọi thường
     * truyền vào một phép so sánh phiên bản, ví dụ
     * {@code player.getSession().version < 222}.</p>
     *
     * <p><b>Rủi ro:</b> ép {@code (int)} sẽ <b>tràn im lặng</b> khi giá trị vượt
     * ~2,1 tỉ — đúng ngưỡng mà vàng/ngọc trong game hay chạm tới.</p>
     */
    public void writeCris(long v, boolean is) throws IOException {
        if (is) {
            this.writeInt((int) v);
            return;
        }
        this.writeLong(v);
    }

    /** Như {@link #writeCris} nhưng nhánh hẹp là 2 byte ({@code short}), giới hạn ±32.767. */
    public void writeShortToLong(long v, boolean is) throws IOException {
        if (is) {
            this.writeShort((short) v);
            return;
        }
        this.writeLong(v);
    }

    /** Như {@link #writeCris} nhưng nhánh hẹp là 1 byte, giới hạn ±127. */
    public void writeByteToLong(long v, boolean is) throws IOException {
        if (is) {
            this.writeByte((byte) v);
            return;
        }
        this.writeLong(v);
    }

    /**
     * Đóng cả 4 luồng.
     *
     * <p>Trên {@code ByteArrayInputStream}/{@code ByteArrayOutputStream} thì
     * {@code close()} <b>không làm gì cả</b> — nên hàm này an toàn khi gọi nhiều lần
     * (và thực tế bị gọi 2 lần cho mỗi gói gửi: một lần trong
     * {@code MessageSendCollect.doSendMessage}, một lần trong {@code Sender.run}).</p>
     *
     * <p>Nó <b>không</b> giải phóng bộ nhớ — muốn vậy phải gọi {@link #dispose()}.</p>
     */
    @Override
    public void cleanup() {
        try {
            if (this.is != null) {
                this.is.close();
            }
            if (this.os != null) {
                this.os.close();
            }
            if (this.dis != null) {
                this.dis.close();
            }
            if (this.dos != null) {
                this.dos.close();
            }
        } catch (IOException e) {
        }
    }

    /** {@link #cleanup()} rồi bỏ tham chiếu để GC thu hồi bộ đệm. */
    @Override
    public void dispose() {
        this.cleanup();
        this.dis = null;
        this.is = null;
        this.dos = null;
        this.os = null;
    }

    // =================================================================
    //  Uỷ quyền đọc — tất cả chuyển thẳng cho DataInputStream.
    //  Đọc theo big-endian (thứ tự byte chuẩn của Java DataInput).
    //  Mọi hàm đều đẩy con trỏ đọc chung tiến lên.
    // =================================================================

    /** Đọc 1 byte không dấu (0..255); trả {@code -1} khi hết dữ liệu. */
    @Override
    public int read() throws IOException {
        return this.reader().read();
    }

    /** Đọc tối đa {@code b.length} byte; trả số byte đọc được, <b>có thể ít hơn</b>. */
    @Override
    public int read(byte[] b) throws IOException {
        return this.reader().read(b);
    }

    /** Đọc tối đa {@code len} byte vào {@code b} từ vị trí {@code off}. */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.reader().read(b, off, len);
    }

    /** Đọc 1 byte: khác 0 là {@code true}. */
    @Override
    public boolean readBoolean() throws IOException {
        return this.reader().readBoolean();
    }

    /** Đọc 1 byte có dấu (-128..127). */
    @Override
    public byte readByte() throws IOException {
        return this.reader().readByte();
    }

    /** Đọc 2 byte có dấu. */
    @Override
    public short readShort() throws IOException {
        return this.reader().readShort();
    }

    /** Đọc 4 byte có dấu. */
    @Override
    public int readInt() throws IOException {
        return this.reader().readInt();
    }

    /** Đọc 8 byte có dấu. */
    @Override
    public long readLong() throws IOException {
        return this.reader().readLong();
    }

    /** Đọc 4 byte dạng IEEE-754. Hầu như không dùng trong giao thức này. */
    @Override
    public float readFloat() throws IOException {
        return this.reader().readFloat();
    }

    /** Đọc 8 byte dạng IEEE-754. */
    @Override
    public double readDouble() throws IOException {
        return this.reader().readDouble();
    }

    /** Đọc 2 byte thành một ký tự UTF-16. */
    @Override
    public char readChar() throws IOException {
        return this.reader().readChar();
    }

    /**
     * Đọc chuỗi định dạng UTF sửa đổi của Java: 2 byte độ dài rồi tới nội dung.
     *
     * <p>Đây là cách <b>duy nhất</b> chuỗi được truyền trong giao thức. Giới hạn
     * 65.535 byte. Client Unity phải ghi đúng cùng định dạng này.</p>
     */
    @Override
    public String readUTF() throws IOException {
        return this.reader().readUTF();
    }

    /** Đọc <b>đủ</b> {@code b.length} byte; ném {@code EOFException} nếu thiếu. */
    @Override
    public void readFully(byte[] b) throws IOException {
        this.reader().readFully(b);
    }

    /** Đọc đủ {@code len} byte vào {@code b} từ vị trí {@code off}. */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        this.reader().readFully(b, off, len);
    }

    /**
     * Đọc 1 byte thành 0..255.
     *
     * <p>Dùng khi client gửi số vượt 127 trong một byte — ví dụ chỉ số ô hành trang.
     * Nhầm sang {@link #readByte()} sẽ ra số âm.</p>
     */
    @Override
    public int readUnsignedByte() throws IOException {
        return this.reader().readUnsignedByte();
    }

    /** Đọc 2 byte thành 0..65.535. */
    @Override
    public int readUnsignedShort() throws IOException {
        return this.reader().readUnsignedShort();
    }

    // =================================================================
    //  Uỷ quyền ghi — tất cả chuyển thẳng cho DataOutputStream.
    //  Mỗi hàm nối thêm byte vào cuối phần thân.
    // =================================================================

    /** Ghi nguyên mảng byte. */
    @Override
    public void write(byte[] b) throws IOException {
        this.writer().write(b);
    }

    /** Ghi 1 byte (8 bit thấp của {@code b}). */
    @Override
    public void write(int b) throws IOException {
        this.writer().write(b);
    }

    /** Ghi {@code len} byte của {@code b} bắt đầu từ {@code off}. */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.writer().write(b, off, len);
    }

    /** Ghi 1 byte: {@code 1} nếu {@code true}, {@code 0} nếu {@code false}. */
    @Override
    public void writeBoolean(boolean v) throws IOException {
        this.writer().writeBoolean(v);
    }

    /** Ghi 1 byte. Kiểu tham số là {@code int} nhưng chỉ 8 bit thấp được ghi. */
    @Override
    public void writeByte(int v) throws IOException {
        this.writer().writeByte(v);
    }

    /**
     * Ghi mỗi ký tự thành 1 byte, <b>không có độ dài phía trước</b>.
     *
     * <p>Ký tự ngoài ASCII bị cắt mất byte cao — <b>không dùng cho tiếng Việt</b>.
     * Chuỗi trong game phải dùng {@link #writeUTF}.</p>
     */
    @Override
    public void writeBytes(String s) throws IOException {
        this.writer().writeBytes(s);
    }

    /** Ghi 2 byte UTF-16. */
    @Override
    public void writeChar(int v) throws IOException {
        this.writer().writeChar(v);
    }

    /** Ghi mỗi ký tự thành 2 byte, không có độ dài phía trước. */
    @Override
    public void writeChars(String s) throws IOException {
        this.writer().writeChars(s);
    }

    /** Ghi 8 byte IEEE-754. */
    @Override
    public void writeDouble(double v) throws IOException {
        this.writer().writeDouble(v);
    }

    /** Ghi 4 byte IEEE-754. */
    @Override
    public void writeFloat(float v) throws IOException {
        this.writer().writeFloat(v);
    }

    /** Ghi 4 byte. */
    @Override
    public void writeInt(int v) throws IOException {
        this.writer().writeInt(v);
    }

    /** Ghi 8 byte. */
    @Override
    public void writeLong(long v) throws IOException {
        this.writer().writeLong(v);
    }

    /** Ghi 2 byte (16 bit thấp của {@code v}). */
    @Override
    public void writeShort(int v) throws IOException {
        this.writer().writeShort(v);
    }

    /**
     * Ghi chuỗi định dạng UTF sửa đổi của Java (2 byte độ dài + nội dung).
     *
     * <p>Cách duy nhất để gửi chuỗi. Ném ngoại lệ nếu chuỗi mã hoá ra quá
     * 65.535 byte — cần nhớ với các thông báo dài do admin nhập.</p>
     */
    @Override
    public void writeUTF(String str) throws IOException {
        this.writer().writeUTF(str);
    }

    /**
     * Đọc một ảnh: 4 byte độ dài rồi tới dữ liệu ảnh đã mã hoá (PNG/JPEG).
     *
     * <p><b>Lỗi tiềm tàng:</b> dùng {@code read(dataImage)} chứ không phải
     * {@code readFully} — {@code read} có quyền trả về ít byte hơn yêu cầu, khi đó
     * ảnh đọc ra sẽ cụt và {@code ImageIO.read} trả {@code null} mà không báo lỗi.
     * Nên đổi sang {@code readFully}.</p>
     */
    @Override
    public BufferedImage readImage() throws IOException {
        int size = this.readInt();
        byte[] dataImage = new byte[size];
        this.read(dataImage);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataImage));
        return image;
    }

    /**
     * Ghi một ảnh: 4 byte độ dài rồi tới dữ liệu đã mã hoá.
     *
     * @param format định dạng cho {@code ImageIO}, ví dụ {@code "png"} hoặc {@code "jpg"}
     */
    @Override
    public void writeImage(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage)image, format, baos);
        byte[] dataImage = baos.toByteArray();
        this.writeInt(dataImage.length);
        this.write(dataImage);
    }
}
