package nro.net.api;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Hợp đồng đọc/ghi của một gói tin.
 *
 * <p>Về bản chất đây là bản sao của {@code java.io.DataInput} + {@code DataOutput}
 * gộp lại, thêm hai hàm ảnh và hai hàm dọn dẹp. Cài đặt duy nhất là
 * {@code nro.net.io.Message}, và mọi hàm ở đó chỉ uỷ quyền thẳng cho
 * {@code DataInputStream}/{@code DataOutputStream}.</p>
 *
 * <p><b>Interface này có cần không?</b> Một cài đặt duy nhất, không có test giả
 * lập, không có khả năng thay thế nào đang dùng. Có thể bỏ và dùng thẳng
 * {@code Message} — bớt được một lớp gián tiếp và ~70 dòng khai báo trùng.
 * Giữ lại thì vô hại nhưng cũng không mang lại gì.</p>
 *
 * <p><b>Quan trọng:</b> một đối tượng chỉ dùng được <b>một chiều</b>. Gói ở chế
 * độ đọc thì gọi hàm {@code write*} sẽ NPE, và ngược lại. Xem
 * {@code nro.net.io.Message} để biết chi tiết từng hàm và các bẫy kèm theo.</p>
 */
public interface IMessage {

    // ---------- Đọc: số nguyên, chuỗi, mảng byte ----------

    /** Đọc 1 byte không dấu; {@code -1} khi hết dữ liệu. */
    public int read() throws IOException;

    /** Đọc <b>tối đa</b> {@code var1.length} byte; có thể trả về ít hơn. */
    public int read(byte[] var1) throws IOException;

    /** Đọc tối đa {@code var3} byte vào {@code var1} từ vị trí {@code var2}. */
    public int read(byte[] var1, int var2, int var3) throws IOException;

    /** Đọc 1 byte: khác 0 là {@code true}. */
    public boolean readBoolean() throws IOException;

    /** Đọc 1 byte có dấu (-128..127). */
    public byte readByte() throws IOException;

    /** Đọc 2 byte có dấu. */
    public short readShort() throws IOException;

    /** Đọc 4 byte có dấu. */
    public int readInt() throws IOException;

    /** Đọc 8 byte có dấu. */
    public long readLong() throws IOException;

    /** Đọc 4 byte IEEE-754. */
    public float readFloat() throws IOException;

    /** Đọc 8 byte IEEE-754. */
    public double readDouble() throws IOException;

    /** Đọc 2 byte thành một ký tự UTF-16. */
    public char readChar() throws IOException;

    /** Đọc chuỗi UTF sửa đổi của Java (2 byte độ dài + nội dung). Cách duy nhất truyền chuỗi. */
    public String readUTF() throws IOException;

    /** Đọc <b>đủ</b> số byte yêu cầu; ném {@code EOFException} nếu thiếu. Nên ưu tiên hơn {@code read}. */
    public void readFully(byte[] var1) throws IOException;

    /** Đọc đủ {@code var3} byte vào {@code var1} từ vị trí {@code var2}. */
    public void readFully(byte[] var1, int var2, int var3) throws IOException;

    /** Đọc 1 byte thành 0..255. Dùng khi client gửi số vượt 127 trong một byte. */
    public int readUnsignedByte() throws IOException;

    /** Đọc 2 byte thành 0..65.535. */
    public int readUnsignedShort() throws IOException;

    // ---------- Ghi: số nguyên, chuỗi, mảng byte ----------

    /** Ghi nguyên mảng byte. */
    public void write(byte[] var1) throws IOException;

    /** Ghi 1 byte (8 bit thấp). */
    public void write(int var1) throws IOException;

    /** Ghi {@code var3} byte của {@code var1} từ vị trí {@code var2}. */
    public void write(byte[] var1, int var2, int var3) throws IOException;

    /** Ghi 1 byte: 1 hoặc 0. */
    public void writeBoolean(boolean var1) throws IOException;

    /** Ghi 1 byte (8 bit thấp của tham số {@code int}). */
    public void writeByte(int var1) throws IOException;

    /** Ghi mỗi ký tự thành 1 byte, không có độ dài. <b>Hỏng với tiếng Việt</b> — dùng {@link #writeUTF}. */
    public void writeBytes(String var1) throws IOException;

    /** Ghi 2 byte UTF-16. */
    public void writeChar(int var1) throws IOException;

    /** Ghi mỗi ký tự thành 2 byte, không có độ dài. */
    public void writeChars(String var1) throws IOException;

    /** Ghi 8 byte IEEE-754. */
    public void writeDouble(double var1) throws IOException;

    /** Ghi 4 byte IEEE-754. */
    public void writeFloat(float var1) throws IOException;

    /** Ghi 4 byte. */
    public void writeInt(int var1) throws IOException;

    /** Ghi 8 byte. */
    public void writeLong(long var1) throws IOException;

    /** Ghi 2 byte (16 bit thấp). */
    public void writeShort(int var1) throws IOException;

    /** Ghi chuỗi UTF sửa đổi của Java. Giới hạn 65.535 byte sau khi mã hoá. */
    public void writeUTF(String var1) throws IOException;

    // ---------- Ảnh ----------

    /** Đọc ảnh: 4 byte độ dài + dữ liệu PNG/JPEG đã mã hoá. */
    public BufferedImage readImage() throws IOException;

    /** Ghi ảnh: 4 byte độ dài + dữ liệu. {@code var2} là định dạng, ví dụ {@code "png"}. */
    public void writeImage(BufferedImage var1, String var2) throws IOException;

    // ---------- Truy cập luồng thô & dọn dẹp ----------

    /** Luồng ghi thô. {@code null} nếu gói ở chế độ đọc. */
    DataOutputStream writer();

    /** Luồng đọc thô. {@code null} nếu gói ở chế độ ghi. Con trỏ đọc dùng chung. */
    DataInputStream reader();

    /** Toàn bộ phần thân đã ghi. <b>NPE</b> nếu gói ở chế độ đọc. */
    byte[] getData();

    /** Đóng các luồng. An toàn khi gọi nhiều lần; <b>không</b> giải phóng bộ nhớ. */
    void cleanup();

    /** {@link #cleanup()} rồi bỏ tham chiếu bộ đệm cho GC. */
    void dispose();
}
