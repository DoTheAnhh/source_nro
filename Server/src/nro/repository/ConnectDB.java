package nro.repository;

import nro.core.log.Logger;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;

/**
 * Cổng vào duy nhất tới MySQL: giữ pool kết nối HikariCP và 4 hàm chạy câu lệnh.
 *
 * <p><b>Tên package viết sai.</b> {@code jbcd} lẽ ra phải là {@code jdbc}. Đã
 * lan ra 13 file và hàng trăm dòng import; nên đổi trong bước chuẩn hoá tên
 * package (giai đoạn 4).</p>
 *
 * <p><b>Cấu hình đọc từ {@code data/config/data_base.properties}</b>, không nhúng
 * trong code — đây là một trong số ít chỗ trong project làm đúng.</p>
 *
 * <p><b>Bốn hàm chạy câu lệnh, và khi nào dùng cái nào:</b></p>
 * <table border="1">
 *   <tr><th>Hàm</th><th>Dùng khi</th></tr>
 *   <tr><td>{@code executeQuery(sql)}</td>
 *       <td>Câu SQL <b>hằng</b>, không có giá trị động nào.</td></tr>
 *   <tr><td>{@code executeQuery(sql, obj...)}</td>
 *       <td><b>Có bất kỳ giá trị động nào</b> — luôn dùng cái này.</td></tr>
 *   <tr><td>{@code executeUpdate(sql)}</td>
 *       <td>INSERT/UPDATE/DELETE hằng.</td></tr>
 *   <tr><td>{@code executeUpdate(sql, obj...)}</td>
 *       <td>INSERT/UPDATE/DELETE có giá trị động.</td></tr>
 * </table>
 *
 * <p><b>Quy tắc bắt buộc:</b> giá trị nào đến từ người chơi thì <b>phải</b> đi qua
 * biến thể {@code Object...}. Nối chuỗi vào SQL là SQL injection — project từng
 * có đúng lỗi này ở chức năng giftcode (đã vá, xem docs/02-AUDIT-VAN-DE.md §A1).</p>
 *
 * <p><b>Khối {@code static} chạy lúc nạp lớp.</b> Chạm tới {@code ConnectDB} lần
 * đầu là pool được dựng ngay. Nếu MySQL chưa bật (XAMPP chưa khởi động), lỗi sẽ
 * nổ ra ở lần chạm đầu tiên chứ không phải lúc gọi truy vấn — vì vậy thông báo
 * lỗi thường không trỏ về đúng nguyên nhân.</p>
 */
public class ConnectDB {

    /** Tên lớp driver JDBC, đọc từ {@code database.driver}. */
    private static String DRIVER;
    private static String DB_HOST;
    private static String DB_PORT;
    /** Tên database. Để {@code public} vì vài nơi khác ghép nó vào câu lệnh. */
    public static String DB_NAME;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static int MIN_CONN;
    private static int MAX_CONN;
    private static long MAX_LIFE_TIME;
    /**
     * Bật ghi log <b>mọi</b> câu lệnh SQL đã chạy.
     *
     * <p>Rất hữu ích khi gỡ lỗi, nhưng bật trên server thật sẽ làm log phình rất
     * nhanh và <b>in cả giá trị tham số</b> — tức là lộ dữ liệu người chơi ra file log.</p>
     */
    public static boolean LOG_QUERY;
    private static HikariConfig CONFIG;
    private static HikariDataSource DS;

    static {
        loadProperties();
        CONFIG = createConfig("User Management", DB_NAME);
        DS = new HikariDataSource(CONFIG);
    }

    /**
     * Mượn một kết nối từ pool.
     *
     * <p><b>Người gọi phải tự đóng</b>, tốt nhất là bằng try-with-resources.
     * "Đóng" ở đây nghĩa là <i>trả về pool</i>, không phải ngắt kết nối thật.
     * Quên đóng là rò kết nối, và khi pool cạn thì <b>toàn server đứng im</b>
     * chờ kết nối — triệu chứng giống hệt treo máy.</p>
     */
    public static Connection getConnection() throws SQLException {
        return ConnectDB.DS.getConnection();
    }

    /** Đóng hẳn pool. Chỉ gọi khi tắt server; sau đó mọi truy vấn đều lỗi. */
    public static void close() {
        ConnectDB.DS.close();
    }

    /**
     * Đọc {@code data/config/data_base.properties}.
     *
     * <p>Mỗi khoá được kiểm tra riêng, thiếu khoá nào thì trường đó giữ giá trị
     * mặc định của Java ({@code null} / {@code 0}).</p>
     *
     * <p><b>Chỗ này dễ mất thời gian gỡ lỗi:</b> nếu file thiếu hoặc sai định dạng,
     * hàm chỉ in một dòng đỏ rồi <b>đi tiếp</b>. Pool sau đó được dựng với host
     * {@code null} và {@code MAX_CONN = 0}, và lỗi thật chỉ lộ ra ở truy vấn đầu
     * tiên dưới dạng một thông báo hoàn toàn không liên quan. Đúng ra phải dừng
     * server ngay tại đây.</p>
     */
    private static void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("data/config/data_base.properties"));
            Object value;

            if ((value = properties.get("database.driver")) != null) {
                ConnectDB.DRIVER = String.valueOf(value);
            }
            if ((value = properties.get("database.host")) != null) {
                ConnectDB.DB_HOST = String.valueOf(value);
            }
            if ((value = properties.get("database.port")) != null) {
                ConnectDB.DB_PORT = String.valueOf(value);
            }
            if ((value = properties.get("database.name")) != null) {
                ConnectDB.DB_NAME = String.valueOf(value);
            }
            if ((value = properties.get("database.user")) != null) {
                ConnectDB.DB_USER = String.valueOf(value);
            }
            if ((value = properties.get("database.pass")) != null) {
                ConnectDB.DB_PASSWORD = String.valueOf(value);
            }
            if ((value = properties.get("database.min")) != null) {
                ConnectDB.MIN_CONN = Integer.parseInt(String.valueOf(value));
            }
            if ((value = properties.get("database.max")) != null) {
                ConnectDB.MAX_CONN = Integer.parseInt(String.valueOf(value));
            }
            if ((value = properties.get("database.lifetime")) != null) {
                ConnectDB.MAX_LIFE_TIME = Integer.parseInt(String.valueOf(value));
            }
            if ((value = properties.get("database.log")) != null) {
                ConnectDB.LOG_QUERY = Boolean.parseBoolean(String.valueOf(value));
            }

//            Logger.log(Logger.YELLOW," _   __   ___    _  __        __ __   __ __   ___    ____        ___    ___   ____\n");
//            Logger.log(Logger.PURPLE,"| | / /  / _ |  / |/ /       / //_/  / // /  / _ |  /  _/       / _ \\  / _ \\ / __ \\\n");
//            Logger.log(Logger.BLUE,"| |/ /  / __ | /    /       / ,<    / _  /  / __ | _/ /        / ___/ / , _// /_/ /\n");
//            Logger.log(Logger.RED,"|___/  /_/ |_|/_/|_/       /_/|_|  /_//_/  /_/ |_|/___/       /_/    /_/|_| \\____/\n");
            Logger.log(Logger.GREEN, "Chạy thành công tệp properties!\n");
        } catch (final IOException | NumberFormatException ex) {
            Logger.log(Logger.RED, "Không thể load file properties!\n");
        } finally {
            properties.clear();
        }
    }

    /**
     * Chạy SELECT với câu SQL <b>hằng</b> (không có giá trị động).
     *
     * <p><b>Có giá trị động thì dùng {@link #executeQuery(String, Object...)}</b> —
     * nối chuỗi vào đây là SQL injection.</p>
     *
     * <p>Kết quả trả về là một <b>bản chụp trong bộ nhớ</b>: {@code ConnectResultSet}
     * đọc hết mọi dòng vào RAM rồi đóng {@code ResultSet}. Nhờ vậy kết nối được trả
     * về pool ngay và người gọi dùng kết quả thoải mái sau đó — nhưng cũng nghĩa là
     * <b>một câu {@code SELECT * FROM player} sẽ nạp toàn bộ bảng vào bộ nhớ</b>.
     * Luôn kèm {@code WHERE} và {@code LIMIT}.</p>
     */
    public static CrisResultSet executeQuery(final String query) throws Exception {
        try {
            Connection con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (ConnectDB.LOG_QUERY) {
                        Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
                    }
                    return new ConnectResultSet(rs);
                }
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        } catch (Exception ex) {
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }

    /**
     * Chạy SELECT có tham số. <b>Đây là hàm nên dùng mặc định.</b>
     *
     * <p>Mỗi dấu {@code ?} trong câu lệnh ứng với một phần tử {@code objs} theo thứ
     * tự. Giá trị đi qua {@code PreparedStatement.setObject} nên không bao giờ được
     * hiểu là cú pháp SQL — kể cả khi người chơi gõ {@code ' OR 1=1 --}.</p>
     *
     * <p>Số lượng {@code ?} phải khớp số phần tử {@code objs}; lệch là lỗi lúc chạy.</p>
     *
     * <p>Dùng try-with-resources nên kết nối và statement luôn được trả về pool,
     * kể cả khi có ngoại lệ.</p>
     */
    public static CrisResultSet executeQuery(final String query, final Object... objs) throws Exception {
        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            for (int i = 0; i < objs.length; ++i) {
                ps.setObject(i + 1, objs[i]);
            }

            if (ConnectDB.LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }

            return new ConnectResultSet(ps.executeQuery());
        } catch (final Exception ex) {
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }

    /**
     * Chạy INSERT/UPDATE/DELETE với câu SQL hằng.
     *
     * @return số dòng bị ảnh hưởng, hoặc {@code -1} nếu chưa chạy được
     */
    public static int executeUpdate(final String query) throws Exception {
        int rowUpdated = -1;

        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            if (ConnectDB.LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            rowUpdated = ps.executeUpdate();
        } catch (final Exception e) {
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }

        return rowUpdated;
    }

    /**
     * Chạy INSERT/UPDATE/DELETE có tham số.
     *
     * <p><b>Có một tiện ích ẩn ít ai biết:</b> nếu câu lệnh bắt đầu bằng
     * {@code "insert"} và <b>kết thúc bằng {@code "()"}</b>, hàm sẽ tự sinh đúng số
     * dấu hỏi cần thiết:</p>
     * <pre>
     * executeUpdate("insert into t(a,b,c) values()", 1, 2, 3)
     *   -&gt; "insert into t(a,b,c) values(?,?,?)"
     * </pre>
     *
     * <p><b>Bẫy kèm theo:</b> {@code query.indexOf("insert") == 0} <b>phân biệt chữ
     * hoa/thường</b>. Viết {@code "INSERT"} là tiện ích này im lặng không chạy, và
     * câu lệnh giữ nguyên {@code "()"} rỗng — lỗi cú pháp SQL mà nhìn code thì
     * không thấy gì sai.</p>
     *
     * @return số dòng bị ảnh hưởng
     */
    public static int executeUpdate(String query, final Object... objs) throws Exception {
        if (query.indexOf("insert") == 0 && query.lastIndexOf("()") == query.length() - 2) {
            final StringBuilder sb = new StringBuilder();
            sb.append("(");

            for (int i = 0; i < objs.length; ++i) {
                sb.append("?");
                if (i < objs.length - 1) {
                    sb.append(",");
                } else {
                    sb.append(")");
                }
            }

            query = query.replace("()", sb.toString());
        }

        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            for (int j = 0; j < objs.length; ++j) {
                ps.setObject(j + 1, objs[j]);
            }

            if (ConnectDB.LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }

            return ps.executeUpdate();
        } catch (final Exception ex) {
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }

    /**
     * Dựng cấu hình pool HikariCP.
     *
     * <p>URL JDBC ép {@code useUnicode=yes&characterEncoding=UTF-8} — bắt buộc, nếu
     * không tên nhân vật và tin nhắn tiếng Việt sẽ lưu thành dấu hỏi.</p>
     *
     * <p><b>Ba dòng dưới ghi đè giá trị đọc từ file properties:</b></p>
     * <pre>
     * config.setMaxLifetime(MAX_LIFE_TIME);   // đọc từ database.lifetime
     * ...
     * config.setMaxLifetime(25 * 60 * 1000);  // rồi bị đè bằng 25 phút
     * </pre>
     * <p>Nghĩa là khoá {@code database.lifetime} trong file cấu hình <b>không có
     * tác dụng</b>. Sửa cấu hình mà không thấy gì thay đổi là vì lý do này.
     * Nên bỏ một trong hai.</p>
     *
     * <p>Các mốc thời gian: kết nối sống tối đa 25 phút, nhàn rỗi 10 phút thì đóng,
     * ping giữ sống mỗi 5 phút. Đặt thấp hơn {@code wait_timeout} của MySQL (mặc
     * định 8 tiếng) để pool không đưa ra kết nối đã bị máy chủ cắt.</p>
     *
     * <p>Bộ nhớ đệm prepared statement bị <b>tắt</b> ({@code cachePrepStmts=false},
     * {@code useServerPrepStmts=false}). Bật lên thường nhanh hơn đáng kể, nhưng chỉ
     * nên đổi khi có thể đo lại.</p>
     */
    private static HikariConfig createConfig(String poolName, String databaseName) {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(DRIVER);
        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8",
                DB_HOST, DB_PORT, databaseName);
        // Ghi ra URL that su dung. Loi "Connection refused" chi noi khong ai
        // nghe o dia chi do, khong noi dia chi do la gi - ma URL duoc ghep tu
        // ba gia tri doc tu file, nen khong nhin thay chuoi nay thi phai doan.
        Logger.info("DB", "Ket noi: " + url + " | user=" + DB_USER
                + " | pool=" + MIN_CONN + ".." + MAX_CONN);
        config.setJdbcUrl(url);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMinimumIdle(MIN_CONN);
        config.setMaximumPoolSize(MAX_CONN);
        config.setMaxLifetime(MAX_LIFE_TIME);
        config.setPoolName(poolName);
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);

        config.setMaxLifetime(25 * 60 * 1000);  // 25 phút
        config.setIdleTimeout(10 * 60 * 1000);  // 10 phút
        config.setKeepaliveTime(5 * 60 * 1000); // 5 phút

        config.addDataSourceProperty("cachePrepStmts", "false");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "false");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "true");

        return config;
    }
}