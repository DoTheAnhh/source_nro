package nro.repository;

import java.sql.Timestamp;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.sql.ResultSet;
import java.util.Map;

/**
 * Bản chụp trong bộ nhớ của một {@code ResultSet}.
 *
 * <p>Constructor đọc <b>toàn bộ</b> dòng vào RAM rồi đóng {@code ResultSet} và
 * {@code Statement} ngay lập tức, nên kết nối được trả về pool sớm.</p>
 *
 * <p><b>Dữ liệu được lưu hai lần</b> cho hai kiểu truy cập:</p>
 * <ul>
 *   <li>{@code values[dòng][cột]} — tra theo chỉ số cột.</li>
 *   <li>{@code data[dòng]} — map tra theo tên cột. Mỗi giá trị được đưa vào map
 *       <b>hai lần</b>: dưới khoá {@code "cot"} và khoá {@code "bang.cot"}.</li>
 * </ul>
 * <p>Nghĩa là mỗi ô chiếm khoảng <b>ba</b> chỗ tham chiếu trong bộ nhớ. Với truy
 * vấn nhỏ thì không sao, nhưng cần nhớ khi đọc bảng lớn.</p>
 */
public class ConnectResultSet implements CrisResultSet
{
    private Map<String, Object>[] data;
    private Object[][] values;
    private int indexData;
    
    /**
     * Sao chép mọi dòng của {@code rs} vào bộ nhớ.
     *
     * <p>Dùng {@code rs.last()} rồi {@code rs.getRow()} để biết trước số dòng
     * (cần cho việc cấp phát mảng), sau đó {@code rs.beforeFirst()} để quay lại
     * đầu. <b>Cách này đòi hỏi {@code ResultSet} cuộn được</b>; nó chạy được ở đây
     * là nhờ driver MySQL đang dùng nạp sẵn toàn bộ dòng về phía client. Đổi driver
     * hoặc bật chế độ đọc theo luồng (streaming) là {@code rs.last()} sẽ ném ngoại lệ.</p>
     *
     * <p>Tên cột được hạ về chữ thường trước khi làm khoá, nên tra theo tên
     * không phân biệt hoa/thường.</p>
     *
     * <p>Khối {@code finally} luôn đóng {@code Statement} và {@code ResultSet},
     * kể cả khi sao chép hỏng giữa chừng — đây là chỗ giữ cho pool không bị rò.</p>
     */
    public ConnectResultSet(final ResultSet rs) throws Exception {
        this.indexData = -1;
        try {
            rs.last();
            final int nRow = rs.getRow();
            rs.beforeFirst();
            final ResultSetMetaData rsmd = rs.getMetaData();
            final int nColumn = rsmd.getColumnCount();
            this.data = new HashMap[nRow];
            for (int i = 0; i < this.data.length; ++i) {
                this.data[i] = new HashMap<>();
            }
            this.values = new Object[nRow][nColumn];
            int index = 0;
            while (rs.next()) {
                for (int j = 1; j <= nColumn; ++j) {
                    final String tableName = rsmd.getTableName(j);
                    final String columnName = rsmd.getColumnName(j);
                    final Object columnValue = rs.getObject(j);
                    this.data[index].put(columnName.toLowerCase(), columnValue);
                    this.data[index].put(tableName.toLowerCase() + "." + columnName.toLowerCase(), columnValue);
                    this.values[index][j - 1] = columnValue;
                }
                ++index;
            }
        }
        catch (final Exception e) {
            throw e;
        }
        finally {
            if (rs != null) {
                try {
                    rs.getStatement().close();
                    rs.close();
                }
                catch (final Exception ex) {}
            }
        }
    }
    
    /**
     * Giải phóng dữ liệu đã chụp.
     *
     * <p><b>Hai vòng lặp bên trong không làm gì cả.</b> Gán {@code map = null} và
     * {@code o = null} chỉ đặt lại <i>biến lặp</i>, không đụng tới phần tử trong
     * mảng. Thứ thật sự có tác dụng là hai dòng {@code this.data = null} và
     * {@code this.values = null} ở cuối — bỏ tham chiếu gốc là đủ để GC thu hồi.</p>
     *
     * <p>Có thể rút gọn cả hàm còn đúng hai dòng đó.</p>
     */
    @Override
    public void dispose() {
        for (Map map : this.data) {
            map.clear();
            map = null;
        }
        this.data = null;
        for (final Object[] array : this.values) {
            Object[] obj = array;
            for (Object o : array) {
                o = null;
            }
            obj = null;
        }
        this.values = null;
    }
    
    /**
     * Tiến tới dòng kế tiếp.
     *
     * <p>Đây là hàm duyệt <b>đúng đắn</b>, dùng theo mẫu quen thuộc:</p>
     * <pre>
     * while (rs.next()) { ... rs.getString("ten") ... }
     * </pre>
     *
     * @return {@code true} nếu đã đứng trên một dòng hợp lệ
     */
    @Override
    public boolean next() throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        ++this.indexData;
        return this.indexData < this.data.length;
    }
    
    /**
     * <b>CẢNH BÁO: hàm này KHÔNG làm việc như tên gọi.</b>
     *
     * <p>Người đọc (và cả {@code ResultSet.first()} của JDBC) sẽ nghĩ nó nghĩa là
     * "nhảy về dòng đầu, trả về true nếu có dữ liệu". Thực tế thân hàm là:</p>
     * <pre>
     * ++this.indexData;
     * return this.indexData == this.data.length - 1;
     * </pre>
     * <p>Tức là nó <i>tiến con trỏ lên một bước</i> (giống {@link #next()}) rồi trả
     * về {@code true} <b>chỉ khi con trỏ đang đứng ở dòng CUỐI</b>.</p>
     *
     * <p>Với con trỏ khởi tạo bằng {@code -1}, kết quả thực tế là:</p>
     * <pre>
     * số dòng = 0  -&gt;  false
     * số dòng = 1  -&gt;  true      &lt;-- trường hợp duy nhất trả về true
     * số dòng = 2  -&gt;  false
     * số dòng = 3  -&gt;  false
     * </pre>
     *
     * <p><b>Nói cách khác: {@code first()} nghĩa là "kết quả có ĐÚNG MỘT dòng".</b></p>
     *
     * <p><b>Vì sao chưa ai phát hiện.</b> Cả 9 chỗ đang gọi nó đều truy vấn theo
     * khoá duy nhất hoặc có {@code LIMIT 1} (tra tài khoản theo username, tra nhân
     * vật theo tên, {@code SELECT COUNT(*)}...), nên luôn trả về đúng 1 dòng và
     * hàm tình cờ chạy đúng.</p>
     *
     * <p><b>Chỗ đang có rủi ro thật:</b> {@code GiftCodeService.giftCode()} kiểm tra
     * {@code SELECT * FROM giftcode_save WHERE player_id = ? AND code_da_nhap = ?}
     * — câu này <i>không</i> có {@code LIMIT} và bảng không bảo đảm duy nhất. Nếu
     * một người chơi có 2 dòng cho cùng một mã, {@code first()} trả về
     * {@code false}, server tưởng họ chưa nhập mã, và họ <b>nhập lại được mã đó
     * mãi mãi</b>.</p>
     *
     * <p><b>Cách sửa an toàn:</b> đừng đổi hàm này (9 chỗ đang dựa vào hành vi
     * hiện tại). Thêm một hàm mới rõ nghĩa, ví dụ {@code hasAnyRow()} trả về
     * {@code getRows() > 0}, rồi chuyển từng chỗ gọi sang nó và kiểm tra lại.</p>
     */
    @Override
    public boolean first() throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        ++this.indexData;
        return this.indexData == this.data.length - 1;
    }
    
    /**
     * Nhảy tới dòng thứ {@code index}.
     *
     * <p><b>Kiểm tra biên sai đối tượng:</b> hàm kiểm tra {@code this.indexData}
     * (vị trí <i>hiện tại</i>) thay vì {@code index} (vị trí <i>muốn tới</i>).
     * Hệ quả:</p>
     * <ul>
     *   <li>Gọi ngay sau khi tạo, khi con trỏ còn {@code -1} → ném ngoại lệ dù
     *       {@code index} hoàn toàn hợp lệ.</li>
     *   <li>Truyền {@code index} vượt biên → <b>không</b> bị chặn; lỗi chỉ nổ ra
     *       sau đó ở lời gọi {@code getX()} dưới dạng
     *       {@code ArrayIndexOutOfBoundsException}.</li>
     * </ul>
     * <p>Đúng ra phải kiểm tra {@code index}. Hiện chưa nơi nào gọi hàm này.</p>
     */
    @Override
    public boolean gotoResult(final int index) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Index out of bound");
        }
        this.indexData = index;
        return true;
    }
    
    /**
     * Đưa con trỏ về dòng đầu tiên (chỉ số 0).
     *
     * <p><b>Đây mới là hàm làm đúng điều mà {@link #first()} trông như đang làm.</b>
     * Ném ngoại lệ nếu không có dòng nào.</p>
     */
    @Override
    public boolean gotoFirst() throws Exception {
        if (this.data == null || this.data.length == 0) {
            throw new Exception("No data available");
        }
        this.indexData = 0;
        return true;
    }
    
    /**
     * Đưa con trỏ về trước dòng đầu ({@code -1}), để duyệt lại từ đầu bằng
     * {@link #next()}. Là hàm duy nhất trong nhóm không ném ngoại lệ.
     */
    @Override
    public void gotoBeforeFirst() {
        this.indexData = -1;
    }
    
    /** Đưa con trỏ tới dòng cuối. */
    @Override
    public boolean gotoLast() throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        this.indexData = this.data.length - 1;
        return true;
    }
    
    /**
     * Số dòng của kết quả.
     *
     * <p>Đây là cách <b>đáng tin</b> để kiểm tra "có dữ liệu hay không":
     * {@code rs.getRows() > 0} — thay cho {@link #first()}.</p>
     */
    @Override
    public int getRows() throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        return this.data.length;
    }
    
    // =================================================================
    //  Nhóm hàm đọc giá trị.
    //
    //  Mỗi kiểu có hai bản: theo CHỈ SỐ cột (bắt đầu từ 1, như JDBC) và theo
    //  TÊN cột (không phân biệt hoa/thường; dùng được cả dạng "bang.cot").
    //
    //  Mọi hàm đều kiểm tra hai điều kiện trước khi đọc:
    //    - đã dispose() chưa  -> "No data available"
    //    - con trỏ còn ở -1   -> "Results need to be prepared in advance",
    //      nghĩa là phải gọi next() / gotoFirst() trước.
    //
    //  Giá trị NULL trong CSDL trở thành null trong Java, nên các hàm trả về
    //  kiểu nguyên thuỷ sẽ ném NullPointerException khi cột rỗng. Với cột cho
    //  phép NULL, hãy dùng getObject() và tự kiểm tra.
    // =================================================================

    /** Đọc cột theo chỉ số (bắt đầu từ 1) dưới dạng {@code byte}. */
    @Override
    public byte getByte(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.values[this.indexData][column - 1]).byteValue();
    }
    
    @Override
    public byte getByte(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.data[this.indexData].get(column.toLowerCase())).byteValue();
    }
    
    @Override
    public int getInt(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.values[this.indexData][column - 1]).intValue();
    }
    
    @Override
    public int getInt(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.data[this.indexData].get(column.toLowerCase())).intValue();
    }
    
    @Override
    public float getFloat(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.values[this.indexData][column - 1]).floatValue();
    }
    
    @Override
    public float getFloat(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.data[this.indexData].get(column.toLowerCase())).floatValue();
    }
    
    @Override
    public double getDouble(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.values[this.indexData][column - 1]).doubleValue();
    }
    
    @Override
    public double getDouble(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.data[this.indexData].get(column.toLowerCase())).doubleValue();
    }
    
    @Override
    public long getLong(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.values[this.indexData][column - 1]).longValue();
    }
    
    @Override
    public long getLong(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.data[this.indexData].get(column.toLowerCase())).longValue();
    }
    
    @Override
    public String getString(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return String.valueOf(this.values[this.indexData][column - 1]);
    }
    
    @Override
    public String getString(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return String.valueOf(this.data[this.indexData].get(column.toLowerCase()));
    }

    /**
     * Như {@link #getString(String)} nhưng ô {@code NULL} trả về {@code null} thật.
     *
     * <p>{@code getString()} dùng {@code String.valueOf()}, nên ô {@code NULL}
     * trong CSDL biến thành <b>chuỗi bốn ký tự {@code "null"}</b> chứ không phải
     * giá trị rỗng. Chỗ nào phân biệt "chưa đặt" với "đã đặt" thì phải dùng hàm
     * này, nếu không sẽ tưởng ô trống là có dữ liệu.</p>
     *
     * <p>Không sửa thẳng {@code getString()} vì cả dự án đang gọi nó ở hàng trăm
     * chỗ; đổi kiểu trả về thành {@code null} sẽ làm những chỗ gọi
     * {@code .isEmpty()} ngay sau đó ném NPE.</p>
     */
    public String getStringOrNull(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        final Object o = this.data[this.indexData].get(column.toLowerCase());
        return o == null ? null : String.valueOf(o);
    }
    
    @Override
    public Object getObject(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return this.values[this.indexData][column - 1];
    }
    
    @Override
    public Object getObject(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return this.data[this.indexData].get(column.toLowerCase());
    }
    
    @Override
    public boolean getBoolean(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return bool(this.values[this.indexData][column - 1]);
    }
    
    @Override
    public boolean getBoolean(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return bool(this.data[this.indexData].get(column.toLowerCase()));
    }
    
    @Override
    public Timestamp getTimestamp(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return (Timestamp)this.values[this.indexData][column - 1];
    }
    
    @Override
    public Timestamp getTimestamp(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return (Timestamp) this.data[this.indexData].get(column.toLowerCase());
    }
    
    @Override
    public short getShort(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.values[this.indexData][column - 1]).shortValue();
    }
    
    @Override
    public short getShort(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData == -1) {
            throw new Exception("Results need to be prepared in advance");
        }
        return num(this.data[this.indexData].get(column.toLowerCase())).shortValue();
    }

    /**
     * Đọc một ô số về {@link Number}.
     *
     * <p>Ô rỗng ({@code NULL} trong CSDL) trả về 0 thay vì ném NPE — chỗ gọi
     * thường là kiểu nguyên thuỷ nên không có cách nào biểu diễn "không có giá
     * trị", và trả 0 an toàn hơn là làm sập cả luồng đăng nhập.</p>
     *
     * <p><b>Nhận cả {@code Boolean}.</b> Driver MySQL/MariaDB trả cột
     * {@code TINYINT(1)} về dưới dạng {@code Boolean} chứ không phải số. Bản
     * trước ép thẳng sang {@code Number} nên mọi lần {@code getInt} trên một cột
     * như thế đều ném {@code ClassCastException} — và vì chỗ gọi thường bọc
     * trong {@code try/catch} rồi trả giá trị mặc định, lỗi này <b>không hề lộ
     * ra</b>: cài đặt cứ âm thầm quay về mặc định sau mỗi lần đăng nhập. Đó đúng
     * là cảnh "ẩn hợp thể, đăng xuất vào lại là mất".</p>
     *
     * @throws ClassCastException nếu ô không phải kiểu số hay luận lý
     */
    private static Number num(final Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        return (Number) o;
    }

    /**
     * Đọc một ô về {@code boolean}.
     *
     * <p>MySQL lưu {@code tinyint(1)} nên JDBC có thể trả về {@code Boolean}
     * hoặc một kiểu số tuỳ driver và tuỳ cấu hình. Nhận cả hai; số khác 0 là
     * {@code true}. Ô rỗng là {@code false}.</p>
     */
    private static boolean bool(final Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(o));
    }
}
