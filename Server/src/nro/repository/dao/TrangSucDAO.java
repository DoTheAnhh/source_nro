package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Kho dữ liệu cho <b>bông tai Porata</b> và <b>Chân Mệnh Thiên Tử</b>.
 *
 * <h2>Vì sao gộp hai thứ vào một lớp</h2>
 *
 * <p>Hai món này cùng một khuôn: mỗi cấp là một mẫu vật phẩm riêng, có tên và
 * icon riêng, và một bộ chỉ số gắn kèm. Tách thành hai lớp thì bốn hàm tạo bảng,
 * bốn hàm đọc, bốn hàm ghi gần như giống nhau từng dòng.</p>
 *
 * <h2>Bốn bảng</h2>
 *
 * <ul>
 *   <li>{@code bong_tai_cap} — từng cấp bông tai: id vật phẩm, tên, icon</li>
 *   <li>{@code bong_tai_chi_so} — bể chỉ số bốc ngẫu nhiên cho mỗi cấp</li>
 *   <li>{@code chan_menh_cap} — từng cấp chân mệnh: id vật phẩm, tên, icon,
 *       tỉ lệ nâng, số nguyên liệu</li>
 *   <li>{@code chan_menh_chi_so} — chỉ số <b>cố định</b> của mỗi cấp</li>
 * </ul>
 *
 * <h2>Bảng rỗng thì giữ nguyên hành vi cũ</h2>
 *
 * <p>Mọi hàm đọc trả danh sách rỗng khi chưa khai gì, và chỗ gọi trong game phải
 * tự quay về số viết cứng. Nhờ vậy thêm hệ thống này <b>không đổi gì</b> cho tới
 * khi quản trị thật sự khai một dòng — không có cái bẫy "cập nhật xong đồ của
 * người chơi rỗng chỉ số".</p>
 */
public class TrangSucDAO {

    private TrangSucDAO() {
    }

    /** Id vật phẩm của năm cấp bông tai, theo đúng thứ tự cấp 1 đến cấp 5. */
    public static final int[] ID_BONG_TAI = {454, 921, 1943, 2105, 2106};

    /**
     * Id vật phẩm của các cấp chân mệnh.
     *
     * <p>Trong mã cũ cấp bậc tính bằng {@code template.id - 2002} và nâng cấp là
     * {@code id + 1}, nên tám id này phải liền nhau.</p>
     */
    public static final int ID_CHAN_MENH_DAU = 2002;

    /** Số cấp chân mệnh có thể nâng. */
    public static final int SO_CAP_CHAN_MENH = 8;

    private static volatile boolean daTaoBang;

    /**
     * Tạo bốn bảng nếu chưa có.
     *
     * <p>Gọi ở đầu mọi hàm công khai thay vì trông vào một bước cài đặt riêng:
     * chép mã sang máy khác là chạy được ngay, không phải nhớ chạy SQL.</p>
     */
    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS bong_tai_cap ("
                    + " cap INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL DEFAULT 0,"
                    + " ten VARCHAR(120) NOT NULL DEFAULT '',"
                    + " icon INT(11) NOT NULL DEFAULT -1,"
                    + " so_dong INT(11) NOT NULL DEFAULT 1,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " ghi_chu VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (cap)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS bong_tai_so_dong ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " cap INT(11) NOT NULL,"
                    + " so_dong INT(11) NOT NULL DEFAULT 1,"
                    + " ti_le DOUBLE NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id), KEY idx_cap (cap)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS bong_tai_chi_so ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " cap INT(11) NOT NULL,"
                    + " option_id INT(11) NOT NULL DEFAULT 0,"
                    + " gia_tri_min INT(11) NOT NULL DEFAULT 1,"
                    + " gia_tri_max INT(11) NOT NULL DEFAULT 1,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id), KEY idx_cap (cap)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS chan_menh_cap ("
                    + " cap INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL DEFAULT 0,"
                    + " ten VARCHAR(120) NOT NULL DEFAULT '',"
                    + " icon INT(11) NOT NULL DEFAULT -1,"
                    + " ti_le DOUBLE NOT NULL DEFAULT 100,"
                    + " tinh_the INT(11) NOT NULL DEFAULT 0,"
                    + " ma_quai INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " ghi_chu VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (cap)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS chan_menh_chi_so ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " cap INT(11) NOT NULL,"
                    + " option_id INT(11) NOT NULL DEFAULT 0,"
                    + " gia_tri INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id), KEY idx_cap (cap)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            dienSanNeuRong();
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex,
                    "Không tạo được bảng bông tai / chân mệnh");
        }
    }

    /**
     * Điền sẵn toàn bộ số liệu — <b>chỉ khi bảng còn rỗng</b>.
     *
     * <h3>Bảng là nguồn duy nhất</h3>
     *
     * <p>Từ nay mã nguồn không còn giữ bản sao của những con số này; máy chủ chỉ
     * đọc từ đây. Nên phải điền sẵn, không thì lần khởi động đầu tiên là bông tai
     * và chân mệnh mất hết chỉ số.</p>
     *
     * <h3>Chỉ điền khi rỗng</h3>
     *
     * <p>Kiểm số dòng trước chứ không dùng {@code ON DUPLICATE KEY}: cách đó chạy
     * mỗi lần khởi động và ghi đè lại những dòng quản trị đã sửa tay.</p>
     */
    private static void dienSanNeuRong() throws Exception {
        if (demDong("bong_tai_cap") == 0) {
            for (int i = 0; i < ID_BONG_TAI.length; i++) {
                themCapBongTai(i + 1, ID_BONG_TAI[i], i + 1);
            }
        }
        if (demDong("bong_tai_chi_so") == 0) {
            // Be chi so cua ban viet cung: {77, 103, 50, 108, 94, 14, 80, 81,
            // 175, 5}, gia tri 1-15, rieng 94 va 14 chi 1-10. Ap cho cap 2 va
            // cap 3 — hai cap duy nhat nang chi so duoc.
            int[] cacOption = {77, 103, 50, 108, 94, 14, 80, 81, 175, 5};
            for (int cap = 2; cap <= 3; cap++) {
                for (int op : cacOption) {
                    boolean hep = (op == 94 || op == 14);
                    themChiSo("bong_tai_chi_so", cap, op, 1, hep ? 10 : 15, true);
                }
            }
        }
        if (demDong("bong_tai_so_dong") == 0) {
            // Ban cu luon ra DUNG mot dong. Dien san dung nhu vay: cap nao cung
            // 100% ra 1 dong. Quan tri them dong "2 dong 30%" thi tu do 30% ra
            // hai dong, 70% con lai van ve mot dong.
            for (int cap = 2; cap <= 3; cap++) {
                themSoDong(cap, 1, 100d);
            }
        }
        if (demDong("chan_menh_cap") == 0) {
            // Ti le va nguyen lieu cua ban viet cung. Ti le ghi bang PHAN TRAM
            // THAT: ban cu so voi mau 200 nen so 35 trong ma la 17,5% that.
            double[] tiLe = {17.5d, 10d, 5d, 2.5d, 1.5d, 1d, 0.5d, 0.25d, 0d};
            int[] nguyenLieu = {2, 3, 4, 5, 6, 7, 8, 9, 0};
            for (int cap = 0; cap <= SO_CAP_CHAN_MENH; cap++) {
                themCapChanMenh(cap, ID_CHAN_MENH_DAU + cap, tiLe[cap],
                        nguyenLieu[cap],
                        cap == SO_CAP_CHAN_MENH
                                ? "Cấp cuối — không nâng tiếp" : "");
            }
        }
        if (demDong("chan_menh_chi_so") == 0) {
            // Ban cu: ba chi so 50/77/103 cung nhan gia tri min(30, cap * 3).
            // Cap 0 la cap goc chua nang nen khong co dong nao.
            for (int cap = 1; cap <= SO_CAP_CHAN_MENH; cap++) {
                int gt = Math.min(30, cap * 3);
                for (int op : new int[]{50, 77, 103}) {
                    themChiSo("chan_menh_chi_so", cap, op, gt, gt, false);
                }
            }
        }
    }

    private static int demDong(String bang) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM " + bang);
            return rs.next() ? rs.getInt("n") : 0;
        } finally {
            dong(rs);
        }
    }

    private static void themCapBongTai(int cap, int itemId, int soDong)
            throws Exception {
        ConnectDB.executeUpdate("INSERT INTO bong_tai_cap"
                + " (cap, item_id, ten, icon, so_dong, bat, ghi_chu)"
                + " VALUES (?, ?, ?, ?, ?, 1, ?)",
                cap, itemId, tenMau(itemId, "Bông tai Porata +" + cap),
                iconMau(itemId), soDong, "Cấp " + cap);
    }

    private static void themCapChanMenh(int cap, int itemId, double tiLe,
            int nguyenLieu, String ghiChu) throws Exception {
        ConnectDB.executeUpdate("INSERT INTO chan_menh_cap"
                + " (cap, item_id, ten, icon, ti_le, tinh_the, ma_quai,"
                + " bat, ghi_chu) VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?)",
                cap, itemId,
                tenMau(itemId, "Chân Mệnh Thiên Tử cấp " + cap),
                iconMau(itemId), tiLe, nguyenLieu, nguyenLieu, ghiChu);
    }

    private static void themSoDong(int cap, int soDong, double tiLe)
            throws Exception {
        ConnectDB.executeUpdate("INSERT INTO bong_tai_so_dong"
                + " (cap, so_dong, ti_le, bat) VALUES (?, ?, ?, 1)",
                cap, soDong, tiLe);
    }

    private static void themChiSo(String bang, int cap, int op, int min, int max,
            boolean coKhoang) throws Exception {
        if (coKhoang) {
            ConnectDB.executeUpdate("INSERT INTO " + bang
                    + " (cap, option_id, gia_tri_min, gia_tri_max, bat)"
                    + " VALUES (?, ?, ?, ?, 1)", cap, op, min, max);
        } else {
            ConnectDB.executeUpdate("INSERT INTO " + bang
                    + " (cap, option_id, gia_tri, bat) VALUES (?, ?, ?, 1)",
                    cap, op, min);
        }
    }

    /**
     * Tên của mẫu vật phẩm, hoặc tên tự đặt nếu chưa tra được.
     *
     * <p>Chạy lúc khởi động nên bảng mẫu có thể chưa nạp xong; lúc đó dùng tên tự
     * đặt để dòng vẫn có nghĩa, quản trị sửa lại sau.</p>
     */
    private static String tenMau(int itemId, String duPhong) {
        try {
            nro.entity.template.ItemTemplate t =
                    nro.service.item.ItemService.gI().getTemplate(itemId);
            return (t == null || t.name == null || t.name.isEmpty())
                    ? duPhong : t.name;
        } catch (Exception boQua) {
            return duPhong;
        }
    }

    private static int iconMau(int itemId) {
        try {
            nro.entity.template.ItemTemplate t =
                    nro.service.item.ItemService.gI().getTemplate(itemId);
            return t == null ? -1 : t.iconID;
        } catch (Exception boQua) {
            return -1;
        }
    }

    // =====================================================================
    //  Một cấp — dùng chung cho cả bông tai và chân mệnh
    // =====================================================================
    public static final class Cap {

        public int cap;
        public int itemId;
        public String ten;
        public int icon = -1;
        public boolean bat = true;
        public String ghiChu;

        /** Số dòng chỉ số bốc ra — chỉ bông tai dùng. */
        public int soDong = 1;

        /** Tỉ lệ nâng lên cấp này, phần trăm — chỉ chân mệnh dùng. */
        public double tiLe = 100;

        /** Số Tinh thể cần — chỉ chân mệnh dùng. */
        public int tinhThe;

        /** Số Ma quái cần — chỉ chân mệnh dùng. */
        public int maQuai;
    }

    /** Một dòng chỉ số. Bông tai dùng khoảng min–max, chân mệnh dùng số cố định. */
    public static final class ChiSo {

        public int id;
        public int cap;
        public int optionId;
        public int min = 1;
        public int max = 1;
        public boolean bat = true;
    }

    // =====================================================================
    //  Bông tai
    // =====================================================================
    public static List<Cap> capBongTai() {
        damBaoBang();
        List<Cap> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM bong_tai_cap ORDER BY cap");
            while (rs.next()) {
                Cap c = new Cap();
                c.cap = rs.getInt("cap");
                c.itemId = rs.getInt("item_id");
                c.ten = rs.getStringOrNull("ten");
                c.icon = rs.getInt("icon");
                c.soDong = rs.getInt("so_dong");
                c.bat = rs.getBoolean("bat");
                c.ghiChu = rs.getStringOrNull("ghi_chu");
                ra.add(c);
            }
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi đọc bong_tai_cap");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Cấp bông tai theo id vật phẩm, hoặc {@code null} nếu chưa khai. */
    public static Cap capBongTaiTheoItem(int itemId) {
        for (Cap c : capBongTai()) {
            if (c.itemId == itemId && c.bat) {
                return c;
            }
        }
        return null;
    }

    public static List<ChiSo> chiSoBongTai(int cap, boolean chiBat) {
        return docChiSo("bong_tai_chi_so", cap, chiBat, true);
    }

    /** Một mức "ra bao nhiêu dòng chỉ số" kèm tỉ lệ. */
    public static final class SoDong {

        public int id;
        public int cap;
        public int soDong = 1;
        /** Tỉ lệ ra đúng số dòng này, tính bằng phần trăm. */
        public double tiLe;
        public boolean bat = true;
    }

    public static List<SoDong> dsSoDong(int cap, boolean chiBat) {
        damBaoBang();
        List<SoDong> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM bong_tai_so_dong"
                    + " WHERE cap = ?" + (chiBat ? " AND bat = 1" : "")
                    + " ORDER BY so_dong DESC", cap);
            while (rs.next()) {
                SoDong x = new SoDong();
                x.id = rs.getInt("id");
                x.cap = rs.getInt("cap");
                x.soDong = rs.getInt("so_dong");
                x.tiLe = rs.getDouble("ti_le");
                x.bat = rs.getBoolean("bat");
                ra.add(x);
            }
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi đọc bong_tai_so_dong");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /**
     * Bốc số dòng chỉ số cho một cấp bông tai.
     *
     * <h3>Cách bốc</h3>
     *
     * <p>Xét từ mức <b>nhiều dòng nhất</b> xuống: mức nào trúng tỉ lệ của nó thì
     * lấy luôn. Nhờ vậy khai "3 dòng 5%" và "2 dòng 30%" cho ra đúng nghĩa thông
     * thường — 5% ra ba dòng, 30% ra hai dòng, còn lại một dòng — mà không phải
     * tự tính tỉ lệ cho khớp tổng 100.</p>
     *
     * <p>Xét từ ít lên nhiều thì mức 2 dòng ăn trước và mức 3 dòng gần như không
     * bao giờ tới, vì 30% lớn hơn 5%.</p>
     *
     * <p>Không mức nào trúng thì trả về <b>1</b>: bông tai luôn có ít nhất một
     * dòng, không có chuyện nâng chỉ số xong mà chỉ số rỗng.</p>
     */
    public static int bocSoDong(int cap) {
        List<SoDong> ds = dsSoDong(cap, true);
        for (SoDong x : ds) {
            if (x.soDong <= 1 || x.tiLe <= 0) {
                continue;
            }
            if (nro.core.util.Util.isTrue(x.tiLe, 100d)) {
                return x.soDong;
            }
        }
        return 1;
    }

    public static String luuSoDong(SoDong x) {
        damBaoBang();
        if (x == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (x.soDong < 1) {
            return "Số dòng phải từ 1 trở lên.";
        }
        if (x.tiLe < 0 || x.tiLe > 100) {
            return "Tỉ lệ phải từ 0 đến 100.";
        }
        try {
            if (x.id > 0) {
                ConnectDB.executeUpdate("UPDATE bong_tai_so_dong SET cap = ?,"
                        + " so_dong = ?, ti_le = ?, bat = ? WHERE id = ?",
                        x.cap, x.soDong, x.tiLe, x.bat ? 1 : 0, x.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO bong_tai_so_dong"
                        + " (cap, so_dong, ti_le, bat) VALUES (?, ?, ?, ?)",
                        x.cap, x.soDong, x.tiLe, x.bat ? 1 : 0);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi lưu bong_tai_so_dong");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaSoDong(int id) {
        return xoaChiSo("bong_tai_so_dong", id);
    }

    public static String luuCapBongTai(Cap c) {
        damBaoBang();
        String loi = kiemTraCap(c);
        if (loi != null) {
            return loi;
        }
        if (c.soDong < 0) {
            return "Số dòng chỉ số không được âm.";
        }
        try {
            ConnectDB.executeUpdate("INSERT INTO bong_tai_cap"
                    + " (cap, item_id, ten, icon, so_dong, bat, ghi_chu)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE item_id = VALUES(item_id),"
                    + " ten = VALUES(ten), icon = VALUES(icon),"
                    + " so_dong = VALUES(so_dong), bat = VALUES(bat),"
                    + " ghi_chu = VALUES(ghi_chu)",
                    c.cap, c.itemId, nz(c.ten), c.icon, c.soDong,
                    c.bat ? 1 : 0, c.ghiChu);
            ghiMauVatPham(c);
            return null;
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi lưu bong_tai_cap");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaCapBongTai(int cap) {
        try {
            ConnectDB.executeUpdate("DELETE FROM bong_tai_so_dong WHERE cap = ?",
                    cap);
        } catch (Exception boQua) {
            // Khong xoa duoc thi cac dong do thanh rac, khong lam sap gi.
        }
        return xoaCap("bong_tai_cap", "bong_tai_chi_so", cap);
    }

    public static String luuChiSoBongTai(ChiSo cs) {
        return luuChiSo("bong_tai_chi_so", cs, true);
    }

    public static String xoaChiSoBongTai(int id) {
        return xoaChiSo("bong_tai_chi_so", id);
    }

    // =====================================================================
    //  Chân mệnh
    // =====================================================================
    public static List<Cap> capChanMenh() {
        damBaoBang();
        List<Cap> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM chan_menh_cap ORDER BY cap");
            while (rs.next()) {
                Cap c = new Cap();
                c.cap = rs.getInt("cap");
                c.itemId = rs.getInt("item_id");
                c.ten = rs.getStringOrNull("ten");
                c.icon = rs.getInt("icon");
                c.tiLe = rs.getDouble("ti_le");
                c.tinhThe = rs.getInt("tinh_the");
                c.maQuai = rs.getInt("ma_quai");
                c.bat = rs.getBoolean("bat");
                c.ghiChu = rs.getStringOrNull("ghi_chu");
                ra.add(c);
            }
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi đọc chan_menh_cap");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Cấp chân mệnh đang khai, hoặc {@code null} nếu chưa khai cấp đó. */
    public static Cap capChanMenhTheoCap(int cap) {
        for (Cap c : capChanMenh()) {
            if (c.cap == cap && c.bat) {
                return c;
            }
        }
        return null;
    }

    public static List<ChiSo> chiSoChanMenh(int cap, boolean chiBat) {
        return docChiSo("chan_menh_chi_so", cap, chiBat, false);
    }

    public static String luuCapChanMenh(Cap c) {
        damBaoBang();
        String loi = kiemTraCap(c);
        if (loi != null) {
            return loi;
        }
        if (c.tiLe < 0 || c.tiLe > 100) {
            return "Tỉ lệ phải từ 0 đến 100.";
        }
        if (c.tinhThe < 0 || c.maQuai < 0) {
            return "Số nguyên liệu không được âm.";
        }
        try {
            ConnectDB.executeUpdate("INSERT INTO chan_menh_cap"
                    + " (cap, item_id, ten, icon, ti_le, tinh_the, ma_quai,"
                    + " bat, ghi_chu)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE item_id = VALUES(item_id),"
                    + " ten = VALUES(ten), icon = VALUES(icon),"
                    + " ti_le = VALUES(ti_le), tinh_the = VALUES(tinh_the),"
                    + " ma_quai = VALUES(ma_quai), bat = VALUES(bat),"
                    + " ghi_chu = VALUES(ghi_chu)",
                    c.cap, c.itemId, nz(c.ten), c.icon, c.tiLe, c.tinhThe,
                    c.maQuai, c.bat ? 1 : 0, c.ghiChu);
            ghiMauVatPham(c);
            return null;
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi lưu chan_menh_cap");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaCapChanMenh(int cap) {
        return xoaCap("chan_menh_cap", "chan_menh_chi_so", cap);
    }

    public static String luuChiSoChanMenh(ChiSo cs) {
        return luuChiSo("chan_menh_chi_so", cs, false);
    }

    public static String xoaChiSoChanMenh(int id) {
        return xoaChiSo("chan_menh_chi_so", id);
    }

    // =====================================================================
    //  Phần dùng chung
    // =====================================================================

    /**
     * Đọc bảng chỉ số của một cấp.
     *
     * @param coKhoang bảng này có hai cột min–max ({@code true}, bông tai) hay
     *                 một cột giá trị cố định ({@code false}, chân mệnh)
     */
    private static List<ChiSo> docChiSo(String bang, int cap, boolean chiBat,
            boolean coKhoang) {
        damBaoBang();
        List<ChiSo> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM " + bang
                    + " WHERE cap = ?" + (chiBat ? " AND bat = 1" : "")
                    + " ORDER BY id", cap);
            while (rs.next()) {
                ChiSo cs = new ChiSo();
                cs.id = rs.getInt("id");
                cs.cap = rs.getInt("cap");
                cs.optionId = rs.getInt("option_id");
                if (coKhoang) {
                    cs.min = rs.getInt("gia_tri_min");
                    cs.max = rs.getInt("gia_tri_max");
                } else {
                    cs.min = rs.getInt("gia_tri");
                    cs.max = cs.min;
                }
                cs.bat = rs.getBoolean("bat");
                ra.add(cs);
            }
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi đọc " + bang);
        } finally {
            dong(rs);
        }
        return ra;
    }

    private static String luuChiSo(String bang, ChiSo cs, boolean coKhoang) {
        damBaoBang();
        if (cs.optionId < 0) {
            return "Chưa chọn chỉ số.";
        }
        if (coKhoang && cs.max < cs.min) {
            return "Giá trị tối đa không được nhỏ hơn tối thiểu.";
        }
        try {
            if (coKhoang) {
                if (cs.id > 0) {
                    ConnectDB.executeUpdate("UPDATE " + bang + " SET cap = ?,"
                            + " option_id = ?, gia_tri_min = ?, gia_tri_max = ?,"
                            + " bat = ? WHERE id = ?",
                            cs.cap, cs.optionId, cs.min, cs.max,
                            cs.bat ? 1 : 0, cs.id);
                } else {
                    ConnectDB.executeUpdate("INSERT INTO " + bang
                            + " (cap, option_id, gia_tri_min, gia_tri_max, bat)"
                            + " VALUES (?, ?, ?, ?, ?)",
                            cs.cap, cs.optionId, cs.min, cs.max, cs.bat ? 1 : 0);
                }
            } else {
                if (cs.id > 0) {
                    ConnectDB.executeUpdate("UPDATE " + bang + " SET cap = ?,"
                            + " option_id = ?, gia_tri = ?, bat = ? WHERE id = ?",
                            cs.cap, cs.optionId, cs.min, cs.bat ? 1 : 0, cs.id);
                } else {
                    ConnectDB.executeUpdate("INSERT INTO " + bang
                            + " (cap, option_id, gia_tri, bat)"
                            + " VALUES (?, ?, ?, ?)",
                            cs.cap, cs.optionId, cs.min, cs.bat ? 1 : 0);
                }
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi lưu " + bang);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static String xoaChiSo(String bang, int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM " + bang + " WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi xoá " + bang);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Xoá một cấp <b>và</b> mọi dòng chỉ số của nó.
     *
     * <p>Xoá cấp mà để lại chỉ số thì những dòng đó thành rác không đường nào
     * thấy trên panel, mà vẫn nằm trong CSDL — lần sau ai tạo lại cấp cùng số là
     * chỉ số cũ đột nhiên sống lại.</p>
     */
    private static String xoaCap(String bangCap, String bangChiSo, int cap) {
        try {
            ConnectDB.executeUpdate("DELETE FROM " + bangChiSo + " WHERE cap = ?", cap);
            ConnectDB.executeUpdate("DELETE FROM " + bangCap + " WHERE cap = ?", cap);
            return null;
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex, "Lỗi xoá cấp " + bangCap);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Ghi tên và icon của một cấp <b>vào chính bảng mẫu vật phẩm</b>.
     *
     * <h3>Vì sao phải ghi sang item_template</h3>
     *
     * <p>Tên và icon mà người chơi thấy đọc từ {@code item_template}, không phải
     * từ bảng của tab này. Giữ tên riêng trong {@code bong_tai_cap} thì panel hiện
     * một đằng, trong game hiện một nẻo — mà người sửa lại tin là đã đổi xong.</p>
     *
     * <p>Bỏ qua ô để trống: tên rỗng ghi xuống là món mất tên hẳn, còn icon
     * {@code -1} nghĩa là "không đặt", giữ icon sẵn có.</p>
     *
     * <p>Đổi mẫu vật phẩm thì <b>phải khởi động lại máy chủ</b> mới có hiệu lực —
     * bảng mẫu nạp một lần lúc khởi động vào {@code Manager.ITEM_TEMPLATES}.</p>
     */
    private static void ghiMauVatPham(Cap c) {
        if (c == null || c.itemId <= 0) {
            return;
        }
        try {
            if (c.ten != null && !c.ten.trim().isEmpty()) {
                ConnectDB.executeUpdate(
                        "UPDATE item_template SET NAME = ? WHERE id = ?",
                        c.ten.trim(), c.itemId);
            }
            if (c.icon >= 0) {
                ConnectDB.executeUpdate(
                        "UPDATE item_template SET icon_id = ? WHERE id = ?",
                        c.icon, c.itemId);
            }
        } catch (Exception ex) {
            Logger.logException(TrangSucDAO.class, ex,
                    "Không ghi được tên/icon cho mẫu " + c.itemId);
        }
    }

    private static String kiemTraCap(Cap c) {
        if (c == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (c.cap < 0) {
            return "Cấp không được âm.";
        }
        if (c.itemId <= 0) {
            return "Chưa chọn vật phẩm cho cấp này.";
        }
        return null;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception boQua) {
                // Dong that bai thi khong con gi de lam.
            }
        }
    }
}
