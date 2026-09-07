package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Vật phẩm rơi từ quái thường, lưu ở bảng {@code quai_do_roi}.
 *
 * <h2>Trước đây</h2>
 *
 * <p>Toàn bộ phần này nằm gõ cứng trong {@code Mob.getItemMobReward()} — hơn
 * hai trăm dòng {@code if} lồng nhau, mỗi món một tỉ lệ viết thẳng vào mã. Đổi
 * tỉ lệ một món cũng phải sửa mã và biên dịch lại, mà tìm được đúng dòng cần
 * sửa đã mất công.</p>
 *
 * <h2>Đọc trong luồng game</h2>
 *
 * <p>Hàm này chạy <b>mỗi lần một con quái chết</b>, nên phải đệm trong bộ nhớ.
 * Panel gọi {@link #napLai()} sau khi sửa, có hiệu lực ngay, không cần khởi
 * động lại.</p>
 */
public final class QuaiDoRoiDAO {

    private QuaiDoRoiDAO() {
    }

    /** Một món có thể rơi từ quái. */
    public static final class Dong {

        public int id;
        public int itemId;
        public int soLuongMin = 1;
        public int soLuongMax = 1;

        /**
         * Tỉ lệ rơi mỗi lần giết một con quái, tính <b>phần trăm</b>.
         *
         * <p>Nhận số thập phân vì đồ hiếm cần tỉ lệ rất nhỏ: {@code 0.01} là
         * một phần vạn, tức trung bình một vạn con quái mới ra một món.</p>
         */
        public double tiLe = 1;

        /** Xem {@link SuKienDAO.VatPhamRoi#kieuMap} — cùng luật lọc bản đồ. */
        public int kieuMap;
        public String dsMap = "";

        public int hsdMin;
        public int hsdMax;
        public int hsdVinhVien;
        /** Chỉ số ngẫu nhiên, dạng {@code id:min:max,...}. */
        public String chiSo = "";
        public boolean bat = true;
        public String ghiChu = "";

        /**
         * Điều kiện phải thoả thì món này mới rơi. Rỗng là không điều kiện.
         *
         * <p>Xem {@link DieuKien}. Có cột này vì không phải món nào cũng chỉ
         * là "rơi theo tỉ lệ" — vài món chỉ rơi khi người chơi mặc đủ một bộ,
         * đang cầm máy dò, hay đang bật danh hiệu.</p>
         */
        public String dieuKien = "";

        // ---- Do set kich hoat ----
        // Bat "skh" thi mon roi ra KHONG phai item_id nua: may chu tu chon do
        // vai tho dung he cua nguoi choi, lay chi so mac dinh cua mon do roi
        // gan them mot set kich hoat. Danh sach set doc tu bang set_kich_hoat
        // (tab "Set kich hoat"), de trong skhSet la boc ngau nhien.
        public boolean skh;
        public String skhSet = "";
        public int saoMin;
        public int saoMax;

        public String moTaSoLuong() {
            return soLuongMin == soLuongMax ? String.valueOf(soLuongMin)
                    : soLuongMin + " – " + soLuongMax;
        }

        /** Món này có rơi ở bản đồ đang đứng không. Cùng luật với đồ rơi sự kiện. */
        public boolean roiOMap(int mapId, int hanhTinh, String tenMap) {
            SuKienDAO.VatPhamRoi thu = new SuKienDAO.VatPhamRoi();
            thu.kieuMap = kieuMap;
            thu.dsMap = dsMap;
            return thu.roiOMap(mapId, hanhTinh, tenMap);
        }

        public String moTaMap() {
            SuKienDAO.VatPhamRoi thu = new SuKienDAO.VatPhamRoi();
            thu.kieuMap = kieuMap;
            thu.dsMap = dsMap;
            return thu.moTaMap();
        }
    }

    /**
     * Các điều kiện rơi có sẵn.
     *
     * <p>Mỗi mã ứng với một phép kiểm tra trong {@code Mob}. Thêm mã mới ở đây
     * thì phải thêm nhánh tương ứng bên đó, không thì món khai với mã lạ sẽ
     * không bao giờ rơi.</p>
     */
    public static final class DieuKien {

        public static final String KHONG = "";
        public static final String SET_THAN_LINH = "SET_THAN_LINH";
        public static final String SET_HUY_DIET = "SET_HUY_DIET";
        public static final String MAY_DO = "MAY_DO";
        public static final String MAY_DO_SIEU_HOA = "MAY_DO_SIEU_HOA";
        public static final String DANH_HIEU_THIEN_TU = "DANH_HIEU_THIEN_TU";

        /** Mã và nhãn, theo đúng thứ tự muốn hiện trên panel. */
        public static final String[][] DANH_SACH = {
            {KHONG, "Không điều kiện",
                "Rơi từ mọi con quái, chỉ phụ thuộc tỉ lệ và phạm vi bản đồ."},
            {SET_THAN_LINH, "Mặc đủ 5 món Thần Linh",
                "Chỉ rơi khi người chơi đang mặc đủ năm món đồ Thần Linh trên người."},
            {SET_HUY_DIET, "Mặc đủ 5 món Huỷ Diệt",
                "Chỉ rơi khi người chơi đang mặc đủ năm món đồ Huỷ Diệt trên người."},
            {MAY_DO, "Đang dùng Máy dò",
                "Chỉ rơi khi người chơi đang bật vật phẩm Máy dò capsule."},
            {MAY_DO_SIEU_HOA, "Đang dùng Máy dò siêu hoá",
                "Chỉ rơi khi người chơi đang bật Máy dò siêu hoá."},
            {DANH_HIEU_THIEN_TU, "Đang bật danh hiệu Thiên Tử",
                "Chỉ rơi khi người chơi đang dùng danh hiệu Thiên Tử."}};

        private DieuKien() {
        }

        public static String nhan(String ma) {
            for (String[] d : DANH_SACH) {
                if (d[0].equals(ma == null ? "" : ma.trim())) {
                    return d[1];
                }
            }
            return "(mã lạ: " + ma + ")";
        }

        public static String moTa(String ma) {
            for (String[] d : DANH_SACH) {
                if (d[0].equals(ma == null ? "" : ma.trim())) {
                    return d[2];
                }
            }
            return "Mã điều kiện này không có trong mã nguồn — món sẽ không bao giờ rơi.";
        }
    }

    public static void damBaoBang() throws Exception {
        ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS quai_do_roi ("
                + " id INT(11) NOT NULL AUTO_INCREMENT,"
                + " item_id INT(11) NOT NULL,"
                + " so_luong_min INT(11) NOT NULL DEFAULT 1,"
                + " so_luong_max INT(11) NOT NULL DEFAULT 1,"
                + " ti_le DOUBLE NOT NULL DEFAULT 1,"
                // kieu_map phai la INT: trinh dieu khien doc tinyint(1) ra
                // Boolean, ma cot nay co ba gia tri 0/1/2 nen getInt se nem loi
                // va hong ca ham doc bang.
                + " kieu_map INT(11) NOT NULL DEFAULT 0,"
                + " ds_map VARCHAR(500) NOT NULL DEFAULT '',"
                + " hsd_min INT(11) NOT NULL DEFAULT 0,"
                + " hsd_max INT(11) NOT NULL DEFAULT 0,"
                + " hsd_vinh_vien INT(11) NOT NULL DEFAULT 0,"
                + " chi_so VARCHAR(255) NOT NULL DEFAULT '',"
                + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                + " ghi_chu VARCHAR(255) NOT NULL DEFAULT '',"
                + " dieu_kien VARCHAR(40) NOT NULL DEFAULT '',"
                + " PRIMARY KEY (id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        ConnectDB.executeUpdate("ALTER TABLE quai_do_roi"
                + " MODIFY COLUMN kieu_map INT(11) NOT NULL DEFAULT 0");
        ConnectDB.executeUpdate("ALTER TABLE quai_do_roi ADD COLUMN"
                + " IF NOT EXISTS dieu_kien VARCHAR(40) NOT NULL DEFAULT ''");
        // Bon cot cua tuy chon "do set kich hoat". Them bang ALTER rieng chu
        // khong sua CREATE o tren: may dang chay da co bang roi, sua CREATE
        // khong lam gi ca vi co "IF NOT EXISTS".
        ConnectDB.executeUpdate("ALTER TABLE quai_do_roi ADD COLUMN"
                + " IF NOT EXISTS skh INT(11) NOT NULL DEFAULT 0");
        ConnectDB.executeUpdate("ALTER TABLE quai_do_roi ADD COLUMN"
                + " IF NOT EXISTS skh_set VARCHAR(40) NOT NULL DEFAULT ''");
        ConnectDB.executeUpdate("ALTER TABLE quai_do_roi ADD COLUMN"
                + " IF NOT EXISTS sao_min INT(11) NOT NULL DEFAULT 0");
        ConnectDB.executeUpdate("ALTER TABLE quai_do_roi ADD COLUMN"
                + " IF NOT EXISTS sao_max INT(11) NOT NULL DEFAULT 0");
    }

    private static volatile List<Dong> cache;

    /** Quên bộ nhớ đệm; lần hỏi sau đọc lại từ CSDL. */
    public static void napLai() {
        cache = null;
    }

    /** Các món đang bật, đọc từ bộ nhớ đệm. */
    public static List<Dong> dangBat() {
        List<Dong> c = cache;
        if (c != null) {
            return c;
        }
        List<Dong> ds = tatCa();
        List<Dong> bat = new ArrayList<>();
        for (Dong d : ds) {
            if (d.bat && d.tiLe > 0) {
                bat.add(d);
            }
        }
        cache = bat;
        return bat;
    }

    public static List<Dong> tatCa() {
        List<Dong> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            damBaoBang();
            rs = ConnectDB.executeQuery("SELECT id, item_id, so_luong_min,"
                    + " so_luong_max, ti_le, kieu_map, ds_map, hsd_min, hsd_max,"
                    + " hsd_vinh_vien, chi_so, bat, ghi_chu, dieu_kien,"
                    + " skh, skh_set, sao_min, sao_max"
                    + " FROM quai_do_roi ORDER BY id");
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getInt("id");
                d.itemId = rs.getInt("item_id");
                d.soLuongMin = rs.getInt("so_luong_min");
                d.soLuongMax = rs.getInt("so_luong_max");
                d.tiLe = rs.getDouble("ti_le");
                d.kieuMap = rs.getInt("kieu_map");
                d.dsMap = rs.getString("ds_map");
                d.hsdMin = rs.getInt("hsd_min");
                d.hsdMax = rs.getInt("hsd_max");
                d.hsdVinhVien = rs.getInt("hsd_vinh_vien");
                d.chiSo = rs.getString("chi_so");
                d.bat = rs.getBoolean("bat");
                d.ghiChu = rs.getString("ghi_chu");
                d.dieuKien = rs.getString("dieu_kien");
                d.skh = rs.getInt("skh") != 0;
                d.skhSet = rs.getString("skh_set");
                if (d.skhSet == null) {
                    d.skhSet = "";
                }
                d.saoMin = rs.getInt("sao_min");
                d.saoMax = rs.getInt("sao_max");
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(QuaiDoRoiDAO.class, ex,
                    "Không đọc được bảng quai_do_roi — quái sẽ không rơi món nào");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                    // Dong khong duoc thi thoi.
                }
            }
        }
        return ds;
    }

    public static String luu(Dong d) {
        if (d == null) {
            return "Thiếu dữ liệu.";
        }
        // Dong "do set kich hoat" khong chon vat pham: may chu tu chon do vai
        // tho dung he cua nguoi choi luc roi.
        if (!d.skh && d.itemId < 0) {
            return "Chưa chọn vật phẩm.";
        }
        if (d.skh && d.saoMax < d.saoMin) {
            return "Số sao tối đa không được nhỏ hơn tối thiểu.";
        }
        if (d.soLuongMin < 1 || d.soLuongMax < d.soLuongMin) {
            return "Số lượng phải từ 1, và tối đa không nhỏ hơn tối thiểu.";
        }
        if (d.tiLe < 0 || d.tiLe > 100) {
            return "Tỉ lệ phải từ 0 đến 100 (%).";
        }
        try {
            damBaoBang();
            if (d.id > 0) {
                ConnectDB.executeUpdate("UPDATE quai_do_roi SET item_id = ?,"
                        + " so_luong_min = ?, so_luong_max = ?, ti_le = ?,"
                        + " kieu_map = ?, ds_map = ?, hsd_min = ?, hsd_max = ?,"
                        + " hsd_vinh_vien = ?, chi_so = ?, bat = ?, ghi_chu = ?,"
                        + " dieu_kien = ?, skh = ?, skh_set = ?,"
                        + " sao_min = ?, sao_max = ?"
                        + " WHERE id = ?",
                        d.itemId, d.soLuongMin, d.soLuongMax, d.tiLe, d.kieuMap,
                        d.dsMap, d.hsdMin, d.hsdMax, d.hsdVinhVien, d.chiSo,
                        d.bat ? 1 : 0, d.ghiChu, d.dieuKien,
                        d.skh ? 1 : 0, d.skhSet == null ? "" : d.skhSet,
                        d.saoMin, d.saoMax, d.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO quai_do_roi (item_id,"
                        + " so_luong_min, so_luong_max, ti_le, kieu_map, ds_map,"
                        + " hsd_min, hsd_max, hsd_vinh_vien, chi_so, bat, ghi_chu,"
                        + " dieu_kien, skh, skh_set, sao_min, sao_max)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        d.itemId, d.soLuongMin, d.soLuongMax, d.tiLe, d.kieuMap,
                        d.dsMap, d.hsdMin, d.hsdMax, d.hsdVinhVien, d.chiSo,
                        d.bat ? 1 : 0, d.ghiChu, d.dieuKien,
                        d.skh ? 1 : 0, d.skhSet == null ? "" : d.skhSet,
                        d.saoMin, d.saoMax);
            }
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(QuaiDoRoiDAO.class, ex, "Lỗi lưu quai_do_roi");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM quai_do_roi WHERE id = ?", id);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(QuaiDoRoiDAO.class, ex, "Lỗi xoá quai_do_roi");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }
}
