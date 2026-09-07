package nro.repository;

import java.sql.Timestamp;

/**
 * Kết quả truy vấn <b>đã tách rời khỏi CSDL</b> — một bản chụp nằm trong bộ nhớ.
 *
 * <p>Khác {@code java.sql.ResultSet} (giữ kết nối mở trong lúc đọc), mọi dòng ở
 * đây đã được sao chép vào RAM và kết nối đã trả về pool. Nhờ vậy có thể giữ và
 * đọc kết quả bao lâu tuỳ ý.</p>
 *
 * <p><b>Cái giá:</b> truy vấn trả về bao nhiêu dòng thì bấy nhiêu dòng nằm trong
 * bộ nhớ. Một câu {@code SELECT * FROM player} không có {@code WHERE} sẽ nạp
 * toàn bộ bảng.</p>
 *
 * <p><b>Mỗi cột đọc được theo hai cách:</b> theo chỉ số (bắt đầu từ 1, như JDBC)
 * hoặc theo tên cột (không phân biệt hoa/thường; hỗ trợ cả dạng
 * {@code "bang.cot"} khi truy vấn JOIN nhiều bảng có cột trùng tên).</p>
 *
 * <p><b>CẢNH BÁO về {@link #first()}</b> — hàm này <i>không</i> làm việc như tên
 * gọi và như {@code ResultSet.first()} của JDBC. Xem Javadoc của
 * {@code ConnectResultSet.first()} trước khi dùng.</p>
 *
 * <p>Cài đặt duy nhất: {@link ConnectResultSet}.</p>
 */
public interface CrisResultSet
{
    byte getByte(final int p0) throws Exception;
    
    byte getByte(final String p0) throws Exception;
    
    int getInt(final int p0) throws Exception;
    
    int getInt(final String p0) throws Exception;
    
    short getShort(final int p0) throws Exception;
    
    short getShort(final String p0) throws Exception;
    
    float getFloat(final int p0) throws Exception;
    
    float getFloat(final String p0) throws Exception;
    
    double getDouble(final int p0) throws Exception;
    
    double getDouble(final String p0) throws Exception;
    
    long getLong(final int p0) throws Exception;
    
    long getLong(final String p0) throws Exception;
    
    String getString(final int p0) throws Exception;
    
    String getString(final String p0) throws Exception;

    /**
     * Như {@link #getString(String)} nhưng ô {@code NULL} trả về {@code null} thật
     * thay vì chuỗi bốn ký tự {@code "null"}.
     */
    String getStringOrNull(final String p0) throws Exception;
    
    boolean getBoolean(final int p0) throws Exception;
    
    boolean getBoolean(final String p0) throws Exception;
    
    Object getObject(final int p0) throws Exception;
    
    Object getObject(final String p0) throws Exception;
    
    Timestamp getTimestamp(final int p0) throws Exception;
    
    Timestamp getTimestamp(final String p0) throws Exception;
    
    void dispose();
    
    boolean next() throws Exception;
    
    boolean first() throws Exception;
    
    boolean gotoResult(final int p0) throws Exception;
    
    boolean gotoFirst() throws Exception;
    
    void gotoBeforeFirst();
    
    boolean gotoLast() throws Exception;
    
    int getRows() throws Exception;
}




