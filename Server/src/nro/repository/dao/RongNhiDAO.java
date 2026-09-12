package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Kho dữ liệu cho <b>trứng rồng nhí</b>: nở ra con gì, chỉ số bao nhiêu, hạn
 * dùng thế nào, và đổi mảnh lấy trứng theo giá nào.
 *
 * <h2>Ba bảng</h2>
 *
 * <ul>
 *   <li>{@code rong_nhi_cau_hinh} — id trứng thường, id trứng vàng, id mảnh,
 *       số mảnh đổi được mỗi loại trứng</li>
 *   <li>{@code rong_nhi_loai} — từng loại rồng nhí nở ra được: vật phẩm nào, ra
 *       từ trứng nào, tỉ lệ bốc trúng, tỉ lệ vĩnh viễn, số ngày nếu có hạn</li>
 *   <li>{@code rong_nhi_chi_so} — bể chỉ số của từng loại: chỉ số nào, giá trị
 *       từ đâu tới đâu, và bao nhiêu phần trăm thì dòng ấy xuất hiện</li>
 * </ul>
 *
 * <h2>Bảng rỗng thì hệ thống nằm im</h2>
 *
 * <p>Chưa khai loại nào thì mở trứng chỉ báo "chưa khai" chứ không rơi ra món
 * lạ, và mảnh trứng không đổi được gì. Không có đường viết cứng nào trong mã —
 * đúng như yêu cầu: mọi thứ nằm trên panel.</p>
 */
public class RongNhiDAO {

    private RongNhiDAO() {
    }

    /** Trứng rồng nhí thường. */
    public static final String K_TRUNG_THUONG = "trung_thuong";

    /** Trứng vàng rồng nhí. */
    public static final String K_TRUNG_VANG = "trung_vang";

    /** Mảnh trứng rồng nhí — id 0 nghĩa là chưa khai, tính năng đổi nằm im. */
    public static final String K_MANH = "manh";

    /** Bao nhiêu mảnh đổi được một trứng thường. */
    public static final String K_MANH_THUONG = "manh_doi_thuong";

    /** Bao nhiêu mảnh đổi được một trứng vàng. */
    public static final String K_MANH_VANG = "manh_doi_vang";

    /** Loại này ra từ trứng nào: 0 cả hai · 1 chỉ trứng thường · 2 chỉ trứng vàng. */
    public static final int TU_CA_HAI = 0;
    public static final int TU_TRUNG_THUONG = 1;
    public static final int TU_TRUNG_VANG = 2;

    /** Trứng rồng nhí thường. */
    public static final int ID_TRUNG_THUONG = 1879;

    /** Trứng vàng rồng nhí. */
    public static final int ID_TRUNG_VANG = 1880;

    /** Mảnh trứng rồng nhí. */
    public static final int ID_MANH = 1881;

    /** Bảy con rồng nhí, theo thứ tự 1 sao đến 7 sao. */
    public static final int[] ID_RONG_NHI = {1872, 1873, 1874, 1875, 1876, 1877, 1878};

    /** Phiên bản của bộ dữ liệu gieo sẵn. */
    private static final String K_GIEO_VER = "gieo_ver";

    /**
     * Bản gieo hiện tại.
     *
     * <p>Tăng số này khi bộ dữ liệu mặc định đổi: lần khởi động sau, máy nào
     * còn ở bản cũ sẽ được gieo lại. Bản 1 đánh tỉ lệ <b>ngược</b> — rồng 1 sao
     * phổ biến nhất — nên phải gieo đè, không thì máy đã chạy bản ấy giữ mãi
     * bảng sai.</p>
     */
    private static final long PHIEN_BAN_GIEO = 2L;

    /** Chỉ số cộng phần trăm: sức đánh, HP, KI — ba dòng con nào cũng có. */
    private static final int CS_SUC_DANH = 50;
    private static final int CS_HP = 77;
    private static final int CS_KI = 103;

    /** Chí mạng: dòng thêm, con sao càng thấp càng dễ có và càng cao. */
    private static final int CS_CHI_MANG = 14;

    private static volatile boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS rong_nhi_cau_hinh ("
                    + " ten VARCHAR(64) NOT NULL,"
                    + " gia_tri BIGINT NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (ten)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS rong_nhi_loai ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " ten VARCHAR(120) NOT NULL DEFAULT '',"
                    + " item_id INT(11) NOT NULL DEFAULT 0,"
                    + " tu_trung INT(11) NOT NULL DEFAULT 0,"
                    + " ti_le DOUBLE NOT NULL DEFAULT 0,"
                    + " ti_le_vinh_vien DOUBLE NOT NULL DEFAULT 0,"
                    + " ngay_min INT(11) NOT NULL DEFAULT 7,"
                    + " ngay_max INT(11) NOT NULL DEFAULT 7,"
                    + " khoa TINYINT(1) NOT NULL DEFAULT 1,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS rong_nhi_chi_so ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " loai_id INT(11) NOT NULL,"
                    + " option_id INT(11) NOT NULL DEFAULT 0,"
                    + " gia_tri_min INT(11) NOT NULL DEFAULT 1,"
                    + " gia_tri_max INT(11) NOT NULL DEFAULT 1,"
                    + " ti_le DOUBLE NOT NULL DEFAULT 100,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id), KEY idx_loai (loai_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            macDinh(K_TRUNG_THUONG, ID_TRUNG_THUONG);
            macDinh(K_TRUNG_VANG, ID_TRUNG_VANG);
            macDinh(K_MANH, ID_MANH);
            macDinh(K_MANH_THUONG, 49);
            macDinh(K_MANH_VANG, 99);
            // May da chay ban truoc thi dong manh da nam san trong bang voi gia
            // tri 0 (luc do chua biet id), ma INSERT IGNORE khong de len duoc.
            // Chi va khi no van la 0 — quan tri da doi sang so khac thi de yen.
            if (so(K_MANH, 0) <= 0) {
                datSo(K_MANH, ID_MANH);
            }
            gieoLanDau();
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi tạo bảng rồng nhí");
        }
    }

    /**
     * Gieo sẵn bảy con rồng nhí cho <b>từng</b> loại trứng, kèm chỉ số.
     *
     * <h3>Mỗi trứng một bộ dòng riêng, mỗi bộ cộng đúng 100%</h3>
     *
     * <p>Bảy con là chung nhưng tỉ lệ thì khác hẳn nhau, nên trứng thường một
     * bộ bảy dòng, trứng vàng một bộ bảy dòng. Tổng mỗi bộ đúng 100 để đọc con
     * số trên panel là ra ngay phần trăm thật.</p>
     *
     * <h3>Một sao hiếm nhất và mạnh nhất</h3>
     *
     * <p>Thứ tự sao của Ngọc Rồng: một sao là viên đầu, quý nhất. Trứng thường
     * cho 2% ra một sao và 32% ra bảy sao; trứng vàng kéo đều hơn — 8% một sao,
     * 19% bảy sao — nên quả đắt đáng tiền ở chỗ ấy chứ không phải ở chỗ ra con
     * khác.</p>
     *
     * <p>Chỉ số đi cùng thứ tự đó: một sao cộng 13–15%, mỗi bậc sao lùi hai
     * điểm, tới bảy sao còn 1–3%. Chí mạng là dòng thêm — một sao chắc chắn có,
     * mỗi bậc bớt 10% cơ hội.</p>
     *
     * <h3>Gieo lại khi bộ mặc định đổi</h3>
     *
     * <p>Gieo theo {@link #PHIEN_BAN_GIEO}: máy còn ở bản cũ thì <b>xoá sạch rồi
     * gieo lại</b>. Bản 1 đánh tỉ lệ ngược nên không thể để nguyên, và bản ấy mới
     * ra nên gần như chưa ai kịp sửa tay. Từ bản này trở đi, máy đã ở đúng phiên
     * bản sẽ không bị đụng tới nữa.</p>
     */
    private static void gieoLanDau() {
        if (so(K_GIEO_VER, 0) >= PHIEN_BAN_GIEO) {
            return;
        }
        try {
            ConnectDB.executeUpdate("DELETE FROM rong_nhi_chi_so");
            ConnectDB.executeUpdate("DELETE FROM rong_nhi_loai");
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi dọn bảng rồng nhí");
            return;
        }
        // Tong moi bo dung 100.
        int[] tlThuong = {2, 4, 7, 12, 18, 25, 32};
        int[] tlVang = {8, 10, 13, 15, 17, 18, 19};
        for (int i = 0; i < ID_RONG_NHI.length; i++) {
            String ten = "Rồng nhí " + (i + 1) + " sao";
            themLoaiMacDinh(ten, ID_RONG_NHI[i], TU_TRUNG_THUONG,
                    tlThuong[i], 5, 7, 15, i);
            themLoaiMacDinh(ten, ID_RONG_NHI[i], TU_TRUNG_VANG,
                    tlVang[i], 30, 15, 30, i);
        }
        datSo(K_GIEO_VER, PHIEN_BAN_GIEO);
        Logger.success("Đã gieo " + (ID_RONG_NHI.length * 2)
                + " dòng rồng nhí kèm chỉ số\n");
    }

    /**
     * Thêm một loại kèm bể chỉ số của nó.
     *
     * @param bac 0 cho con một sao, 6 cho con bảy sao — càng nhỏ càng mạnh
     */
    private static void themLoaiMacDinh(String ten, int itemId, int tuTrung,
            double tiLe, double tiLeVinhVien, int ngayMin, int ngayMax, int bac) {
        Loai x = new Loai();
        x.ten = ten;
        x.itemId = itemId;
        x.tuTrung = tuTrung;
        x.tiLe = tiLe;
        x.tiLeVinhVien = tiLeVinhVien;
        x.ngayMin = ngayMin;
        x.ngayMax = ngayMax;
        x.khoa = true;
        x.bat = true;
        if (luuLoai(x) != null) {
            return;
        }
        int loaiId = timLoaiVuaThem(itemId, tuTrung);
        if (loaiId <= 0) {
            return;
        }
        // Mot sao 13-15%, moi bac lui hai diem, bay sao con 1-3%.
        int max = 15 - bac * 2;
        int min = Math.max(1, max - 2);
        themChiSoMacDinh(loaiId, CS_SUC_DANH, min, max, 100);
        themChiSoMacDinh(loaiId, CS_HP, min, max, 100);
        themChiSoMacDinh(loaiId, CS_KI, min, max, 100);
        // Chi mang: mot sao chac chan co, moi bac bot 10% co hoi.
        int cmMax = 7 - bac;
        themChiSoMacDinh(loaiId, CS_CHI_MANG, Math.max(1, cmMax - 2), cmMax,
                100 - bac * 10);
    }

    /**
     * Id của dòng vừa thêm.
     *
     * <p>Tra lại bằng {@code item_id} và loại trứng rồi lấy id lớn nhất, thay vì
     * hỏi {@code LAST_INSERT_ID()}: lớp kết nối không trả về số ấy, mà lúc gieo
     * thì bảng vừa được dọn sạch nên không có dòng cũ nào để nhầm.</p>
     */
    private static int timLoaiVuaThem(int itemId, int tuTrung) {
        int id = -1;
        for (Loai x : dsLoai(false)) {
            if (x.itemId == itemId && x.tuTrung == tuTrung && x.id > id) {
                id = x.id;
            }
        }
        return id;
    }

    private static void themChiSoMacDinh(int loaiId, int optionId, int min,
            int max, double tiLe) {
        if (tiLe <= 0) {
            return;
        }
        ChiSo cs = new ChiSo();
        cs.loaiId = loaiId;
        cs.optionId = optionId;
        cs.min = min;
        cs.max = max;
        cs.tiLe = tiLe;
        cs.bat = true;
        luuChiSo(cs);
    }

    /** Ghi giá trị mặc định nếu khoá đó chưa có, không đè lên giá trị đã khai. */
    private static void macDinh(String ten, long giaTri) {
        try {
            ConnectDB.executeUpdate("INSERT IGNORE INTO rong_nhi_cau_hinh"
                    + " (ten, gia_tri) VALUES (?, ?)", ten, giaTri);
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi ghi mặc định " + ten);
        }
    }

    public static long so(String ten, long neuThieu) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM rong_nhi_cau_hinh"
                    + " WHERE ten = ?", ten);
            if (rs.next()) {
                return rs.getLong("gia_tri");
            }
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi đọc cấu hình rồng nhí");
        } finally {
            dong(rs);
        }
        return neuThieu;
    }

    public static String datSo(String ten, long giaTri) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("INSERT INTO rong_nhi_cau_hinh (ten, gia_tri)"
                    + " VALUES (?, ?) ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri)",
                    ten, giaTri);
            return null;
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi lưu cấu hình rồng nhí");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static final class Loai {

        public int id;
        public String ten = "";
        public int itemId;
        /** 0 cả hai · 1 chỉ trứng thường · 2 chỉ trứng vàng. */
        public int tuTrung;
        /** Trọng số bốc trúng, tính bằng phần trăm trong nhóm cùng loại trứng. */
        public double tiLe;
        /** Bao nhiêu phần trăm thì ra bản vĩnh viễn. */
        public double tiLeVinhVien;
        public int ngayMin = 7;
        public int ngayMax = 7;
        /** Gắn khoá "không thể giao dịch". */
        public boolean khoa = true;
        public boolean bat = true;
    }

    public static List<Loai> dsLoai(boolean chiBat) {
        damBaoBang();
        List<Loai> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM rong_nhi_loai"
                    + (chiBat ? " WHERE bat = 1" : "") + " ORDER BY id");
            while (rs.next()) {
                Loai x = new Loai();
                x.id = rs.getInt("id");
                x.ten = rs.getString("ten");
                x.itemId = rs.getInt("item_id");
                x.tuTrung = rs.getInt("tu_trung");
                x.tiLe = rs.getDouble("ti_le");
                x.tiLeVinhVien = rs.getDouble("ti_le_vinh_vien");
                x.ngayMin = rs.getInt("ngay_min");
                x.ngayMax = rs.getInt("ngay_max");
                x.khoa = rs.getBoolean("khoa");
                x.bat = rs.getBoolean("bat");
                ra.add(x);
            }
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi đọc rong_nhi_loai");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Các loại nở ra được từ một quả trứng: đúng loại trứng đó, hoặc "cả hai". */
    public static List<Loai> dsLoaiCuaTrung(int loaiTrung) {
        List<Loai> ra = new ArrayList<>();
        for (Loai x : dsLoai(true)) {
            if (x.itemId > 0 && x.tiLe > 0
                    && (x.tuTrung == TU_CA_HAI || x.tuTrung == loaiTrung)) {
                ra.add(x);
            }
        }
        return ra;
    }

    public static String luuLoai(Loai x) {
        damBaoBang();
        if (x == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (x.itemId <= 0) {
            return "Phải chọn vật phẩm rồng nhí.";
        }
        if (x.tiLe < 0 || x.tiLe > 100 || x.tiLeVinhVien < 0 || x.tiLeVinhVien > 100) {
            return "Tỉ lệ phải từ 0 đến 100.";
        }
        if (x.ngayMin < 0 || x.ngayMax < x.ngayMin) {
            return "Số ngày không hợp lệ.";
        }
        try {
            if (x.id > 0) {
                ConnectDB.executeUpdate("UPDATE rong_nhi_loai SET ten = ?,"
                        + " item_id = ?, tu_trung = ?, ti_le = ?,"
                        + " ti_le_vinh_vien = ?, ngay_min = ?, ngay_max = ?,"
                        + " khoa = ?, bat = ? WHERE id = ?",
                        x.ten, x.itemId, x.tuTrung, x.tiLe, x.tiLeVinhVien,
                        x.ngayMin, x.ngayMax, x.khoa ? 1 : 0, x.bat ? 1 : 0, x.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO rong_nhi_loai (ten, item_id,"
                        + " tu_trung, ti_le, ti_le_vinh_vien, ngay_min, ngay_max,"
                        + " khoa, bat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        x.ten, x.itemId, x.tuTrung, x.tiLe, x.tiLeVinhVien,
                        x.ngayMin, x.ngayMax, x.khoa ? 1 : 0, x.bat ? 1 : 0);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi lưu rong_nhi_loai");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaLoai(int id) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM rong_nhi_chi_so WHERE loai_id = ?", id);
            ConnectDB.executeUpdate("DELETE FROM rong_nhi_loai WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi xoá rong_nhi_loai");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static final class ChiSo {

        public int id;
        public int loaiId;
        public int optionId;
        public int min = 1;
        public int max = 1;
        /** Bao nhiêu phần trăm thì dòng này xuất hiện trên con vừa nở. */
        public double tiLe = 100;
        public boolean bat = true;
    }

    public static List<ChiSo> dsChiSo(int loaiId, boolean chiBat) {
        damBaoBang();
        List<ChiSo> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM rong_nhi_chi_so"
                    + " WHERE loai_id = ?" + (chiBat ? " AND bat = 1" : "")
                    + " ORDER BY id", loaiId);
            while (rs.next()) {
                ChiSo x = new ChiSo();
                x.id = rs.getInt("id");
                x.loaiId = rs.getInt("loai_id");
                x.optionId = rs.getInt("option_id");
                x.min = rs.getInt("gia_tri_min");
                x.max = rs.getInt("gia_tri_max");
                x.tiLe = rs.getDouble("ti_le");
                x.bat = rs.getBoolean("bat");
                ra.add(x);
            }
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi đọc rong_nhi_chi_so");
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static String luuChiSo(ChiSo x) {
        damBaoBang();
        if (x == null) {
            return "Không có dữ liệu để lưu.";
        }
        if (x.max < x.min) {
            return "Giá trị 'đến' phải lớn hơn hoặc bằng 'từ'.";
        }
        if (x.tiLe < 0 || x.tiLe > 100) {
            return "Tỉ lệ phải từ 0 đến 100.";
        }
        try {
            if (x.id > 0) {
                ConnectDB.executeUpdate("UPDATE rong_nhi_chi_so SET loai_id = ?,"
                        + " option_id = ?, gia_tri_min = ?, gia_tri_max = ?,"
                        + " ti_le = ?, bat = ? WHERE id = ?",
                        x.loaiId, x.optionId, x.min, x.max, x.tiLe,
                        x.bat ? 1 : 0, x.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO rong_nhi_chi_so (loai_id,"
                        + " option_id, gia_tri_min, gia_tri_max, ti_le, bat)"
                        + " VALUES (?, ?, ?, ?, ?, ?)",
                        x.loaiId, x.optionId, x.min, x.max, x.tiLe, x.bat ? 1 : 0);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi lưu rong_nhi_chi_so");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaChiSo(int id) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM rong_nhi_chi_so WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(RongNhiDAO.class, ex, "Lỗi xoá rong_nhi_chi_so");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ex) {
            }
        }
    }
}
