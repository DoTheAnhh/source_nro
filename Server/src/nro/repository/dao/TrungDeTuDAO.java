package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.consts.ConstDetu;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Ba quả <b>trứng đệ tử</b>: Mabư, Cell, Berus.
 *
 * <h2>Vật phẩm tự mọc ra, không phải chạy SQL tay</h2>
 *
 * <p>Ba quả trứng này là vật phẩm mới hoàn toàn — bảng {@code item_template}
 * chưa có dòng nào cho chúng. Thay vì đưa người quản trị một tệp SQL phải nhớ
 * chạy, {@link #damBaoVatPham()} tự thêm dòng lúc máy chủ khởi động và ghi lại
 * id đã cấp vào bảng {@code trung_de_tu}.</p>
 *
 * <h3>Vì sao không ghi cứng id vật phẩm</h3>
 *
 * <p>Id vật phẩm trống là của riêng từng máy chủ: bản này đang dùng tới 2451,
 * bản khác có thể đã thêm đồ tới 2600. Ghi cứng một con số là hoặc đè lên vật
 * phẩm người ta đã có, hoặc chèn không được. Ở đây id lấy bằng
 * {@code MAX(id) + 1} ngay lúc thêm, rồi <b>nhớ lại</b> — những lần khởi động
 * sau đọc bảng ánh xạ chứ không cấp thêm.</p>
 *
 * <h3>Icon thì ngược lại: ghi cứng</h3>
 *
 * <p>Ba mã icon 27997–27999 đi kèm ba tệp ảnh đã nằm sẵn trong
 * {@code data/icon/x1..x4} và trong bảng phiên bản ảnh. Ảnh là tệp trong mã
 * nguồn nên mã của nó cũng thuộc về mã nguồn.</p>
 *
 * <h3>Client tự cập nhật</h3>
 *
 * <p>Không phải bấm nút "đẩy dữ liệu": {@code ConnectDB.executeUpdate} thấy câu
 * lệnh có đụng {@code item_template} là tự tăng phiên bản bảng vật phẩm (xem
 * {@code ConfigDAO.tuDayDuLieu}), nên client tải lại danh sách ngay lần đăng
 * nhập kế tiếp.</p>
 */
public final class TrungDeTuDAO {

    private TrungDeTuDAO() {
    }

    /** Một quả trứng: nở ra loại đệ nào, là vật phẩm số mấy. */
    public static final class Trung {

        public byte loai;
        public int itemId;
        public String ten;
        public int iconId;
        public boolean bat = true;
    }

    /**
     * Ba quả trứng dựng sẵn: {loại đệ, tên, mã icon, mô tả}.
     *
     * <p>Mã loại phải khớp {@link ConstDetu} — chính con số ấy là thứ
     * {@code DeTuDAO.loai()} dùng để tra hệ số chỉ số mà quản trị đã chỉnh trên
     * panel, nên trứng Cell nở ra đúng con đệ mang chỉ số của dòng "Cell".</p>
     */
    private static final Object[][] DUNG_SAN = {
        {ConstDetu.MABU, "Trứng Mabư", 27997, "Nở ra đệ tử Mabư"},
        {ConstDetu.CELL, "Trứng Cell", 27998, "Nở ra đệ tử Cell"},
        {ConstDetu.BILL, "Trứng Berus", 27999, "Nở ra đệ tử Berus"}
    };

    /** Kiểu vật phẩm dùng được — cùng kiểu với trứng rồng nhí, bình hút... */
    private static final int KIEU_DUNG_DUOC = 27;

    private static volatile boolean daDamBao;

    /**
     * Dựng bảng ánh xạ và thêm ba vật phẩm nếu chúng chưa có.
     *
     * <p>Gọi <b>trước</b> {@code Manager.loadDatabase()}: danh sách mẫu vật phẩm
     * chỉ đọc một lần lúc khởi động, thêm sau đó thì phải tới lần chạy sau ba
     * quả trứng mới có mặt trong bộ nhớ máy chủ.</p>
     */
    public static synchronized void damBaoVatPham() {
        if (daDamBao) {
            return;
        }
        daDamBao = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS trung_de_tu ("
                    + " loai INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " icon_id INT(11) NOT NULL DEFAULT -1,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (loai)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            for (Object[] d : DUNG_SAN) {
                byte loai = (byte) (int) (Integer) d[0];
                String ten = (String) d[1];
                int icon = (Integer) d[2];
                String moTa = (String) d[3];
                if (daKhai(loai)) {
                    continue;
                }
                int itemId = themVatPham(ten, moTa, icon);
                if (itemId < 0) {
                    continue;
                }
                ConnectDB.executeUpdate("INSERT INTO trung_de_tu"
                        + " (loai, item_id, ten, icon_id, bat)"
                        + " VALUES (?, ?, ?, ?, 1)",
                        (int) loai, itemId, ten, icon);
                Logger.success("Đã thêm vật phẩm " + ten + " (id " + itemId
                        + ", icon " + icon + ")\n");
            }
            xoaDem();
        } catch (Exception ex) {
            Logger.logException(TrungDeTuDAO.class, ex,
                    "Không dựng được vật phẩm trứng đệ tử");
        }
    }

    /**
     * Đã có dòng cho loại này, và vật phẩm nó trỏ tới vẫn còn.
     *
     * <p>Kiểm cả hai vế: chỉ hỏi bảng ánh xạ thì một hôm ai đó xoá dòng
     * {@code item_template} là trứng biến mất mà máy chủ vẫn tưởng có.</p>
     */
    private static boolean daKhai(byte loai) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT t.item_id FROM trung_de_tu t"
                    + " JOIN item_template i ON i.id = t.item_id"
                    + " WHERE t.loai = " + (int) loai);
            return rs.next();
        } finally {
            dong(rs);
        }
    }

    /**
     * Thêm một dòng {@code item_template} và trả về id vừa cấp.
     *
     * @return id vật phẩm, hoặc -1 nếu không thêm được
     */
    private static int themVatPham(String ten, String moTa, int icon)
            throws Exception {
        int id = idTrong();
        if (id < 0) {
            return -1;
        }
        ConnectDB.executeUpdate("INSERT INTO item_template"
                + " (id, TYPE, gender, NAME, description, level, icon_id, part,"
                + " is_up_to_up, power_require, gold, gold_sell, gem, gem_sell,"
                + " ruby, ruby_sell, head, body, leg, TypeEvent, isGender,"
                + " dung_duoc, aura_id)"
                + " VALUES (?, ?, 3, ?, ?, 1, ?, -1, 1, 0, 0, 0, 0, 0, 0, 0,"
                + " -1, -1, -1, 0, -1, 1, -1)",
                id, KIEU_DUNG_DUOC, ten, moTa, icon);
        return id;
    }

    /** Id vật phẩm còn trống — ngay sau id lớn nhất đang dùng. */
    private static int idTrong() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT MAX(id) AS m FROM item_template");
            if (!rs.next()) {
                return -1;
            }
            int max = rs.getInt("m");
            // Client doc id vat pham bang readShort, nen 32766 la tran.
            return (max >= 32766) ? -1 : (max + 1);
        } finally {
            dong(rs);
        }
    }

    // =====================================================================
    //  Đọc
    // =====================================================================
    private static List<Trung> dem;
    private static long lucDoc;

    /** Bao lâu thì đọc lại bảng, tính bằng mili giây. */
    private static final long HAN_DEM = 15_000L;

    /** Ba quả trứng đang khai. <b>Không bao giờ trả {@code null}.</b> */
    public static synchronized List<Trung> dsTrung() {
        long gio = System.currentTimeMillis();
        if (dem != null && gio - lucDoc < HAN_DEM) {
            return dem;
        }
        List<Trung> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT * FROM trung_de_tu ORDER BY loai");
            while (rs.next()) {
                Trung t = new Trung();
                t.loai = (byte) rs.getInt("loai");
                t.itemId = rs.getInt("item_id");
                t.ten = rs.getString("ten");
                t.iconId = rs.getInt("icon_id");
                t.bat = rs.getInt("bat") == 1;
                ds.add(t);
            }
        } catch (Exception ex) {
            Logger.logException(TrungDeTuDAO.class, ex,
                    "Lỗi đọc bảng trung_de_tu");
        } finally {
            dong(rs);
        }
        dem = ds;
        lucDoc = gio;
        return ds;
    }

    /**
     * Kiểm lại và thêm những quả còn thiếu, dù đã chạy một lần trong phiên.
     *
     * <p>Dành cho nút trên panel: bảng bị xoá tay thì không phải khởi động lại
     * máy chủ mới dựng lại được dòng vật phẩm.</p>
     */
    public static synchronized void dungLai() {
        daDamBao = false;
        damBaoVatPham();
    }

    /** Bỏ nhớ tạm — gọi sau khi sửa bảng để có hiệu lực ngay. */
    public static synchronized void xoaDem() {
        dem = null;
    }

    /**
     * Vật phẩm này là trứng của loại đệ nào.
     *
     * @return mã loại trong {@link ConstDetu}, hoặc -1 nếu không phải trứng
     */
    public static byte loaiCuaVatPham(int itemId) {
        for (Trung t : dsTrung()) {
            if (t.bat && t.itemId == itemId) {
                return t.loai;
            }
        }
        return -1;
    }

    /** Tên quả trứng nở ra loại đệ này, hoặc tên loại đệ nếu chưa khai. */
    public static String tenTrung(byte loai) {
        for (Trung t : dsTrung()) {
            if (t.loai == loai) {
                return t.ten;
            }
        }
        return "Trứng " + ConstDetu.tenLoai(loai);
    }

    private static void dong(CrisResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.dispose();
        } catch (Exception boQua) {
            // Dong khong duoc thi cung khong lam gi them duoc.
        }
    }
}
