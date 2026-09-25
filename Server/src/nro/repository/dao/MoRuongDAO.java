package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Dữ liệu <b>mở rương</b> (tab gacha trong màn Sự kiện).
 *
 * <h2>Bảng</h2>
 * <ul>
 *   <li>{@code mo_ruong_loai} — mỗi loại rương một dòng: tên, mô tả, vật phẩm
 *       làm hình, giá x1 và x10 tính bằng điểm rương, thứ tự, bật/tắt.</li>
 *   <li>{@code mo_ruong_qua} — quà trong từng rương: vật phẩm, số lượng,
 *       <b>trọng số</b> (tỉ lệ thật = trọng số / tổng trọng số của rương) và
 *       độ hiếm (chỉ để tô màu: thường, hiếm, sử thi, huyền thoại).</li>
 *   <li>{@code mo_ruong_diem_loai} — điểm của từng người <b>theo từng loại
 *       rương</b>: mỗi rương một loại điểm riêng, không dùng chung. Cách kiếm
 *       điểm tính sau; hiện quản trị chỉnh trên panel.</li>
 *   <li>{@code mo_ruong_lich_su} — mỗi lượt mở một dòng, để tra khi có người
 *       kêu "mở mãi không ra".</li>
 * </ul>
 *
 * <p>Tự tạo bảng và gieo mặc định lúc dùng lần đầu: máy chủ thật chỉ
 * {@code git pull} rồi chạy, không ai chạy SQL tay.</p>
 */
public class MoRuongDAO {

    /** Tên các độ hiếm, đúng thứ tự con số lưu trong bảng. */
    public static final String[] TEN_HIEM = {"Thường", "Hiếm", "Sử thi", "Huyền thoại", "Thần thoại"};

    private static final long HAN_BO_NHO_MS = 30_000L;

    // =====================================================================
    //  Kiểu dữ liệu
    // =====================================================================
    public static final class Ruong {

        public int id;
        public String ten = "";
        public String moTa = "";
        /** Vật phẩm lấy icon làm hình rương (khi {@link #iconHinh} = 0). */
        public int itemHinh;
        /**
         * Icon id vẽ thẳng làm hình rương; 0 là lấy icon của {@link #itemHinh}.
         * Cho phép dùng hình không gắn với vật phẩm nào, vd trứng Mabư 27997.
         */
        public int iconHinh;
        /** Tên loại điểm của rương này; rỗng thì "Điểm " + tên rương. */
        public String tenDiem = "";
        /**
         * Vật phẩm phiếu dùng để mở rương này; giá x1/x10 tính bằng số phiếu.
         * 0 là rương còn mở bằng điểm (cách cũ).
         */
        public int itemPhieu;

        public String tenDiemDeDoc() {
            return (tenDiem == null || tenDiem.trim().isEmpty()) ? ("Điểm " + ten) : tenDiem.trim();
        }
        public int giaX1;
        public int giaX10;
        public int thuTu;
        public boolean bat = true;
    }

    public static final class Qua {

        public int id;
        public int ruongId;
        public int itemId;
        public int soLuong = 1;
        /** Trọng số; tỉ lệ thật = trọng số / tổng trọng số của rương. */
        public int trongSo = 100;
        /** 0 thường · 1 hiếm · 2 sử thi · 3 huyền thoại. */
        public int hiem;
        public String chiSo = "";
    }

    // =====================================================================
    //  Tạo bảng
    // =====================================================================
    private static boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_loai ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " ten VARCHAR(60) NOT NULL DEFAULT '',"
                    + " mo_ta VARCHAR(255) NOT NULL DEFAULT '',"
                    + " item_hinh INT(11) NOT NULL DEFAULT 0,"
                    + " gia_x1 INT(11) NOT NULL DEFAULT 10,"
                    + " gia_x10 INT(11) NOT NULL DEFAULT 90,"
                    + " thu_tu INT(11) NOT NULL DEFAULT 0,"
                    + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_qua ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " ruong_id INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL,"
                    + " so_luong INT(11) NOT NULL DEFAULT 1,"
                    + " trong_so INT(11) NOT NULL DEFAULT 100,"
                    + " hiem INT(11) NOT NULL DEFAULT 0,"
                    + " chi_so VARCHAR(255) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (id), KEY (ruong_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // Diem TACH THEO LOAI RUONG: moi ruong mot loai diem, khong chung.
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_diem_loai ("
                    + " player_id INT(11) NOT NULL,"
                    + " ruong_id INT(11) NOT NULL,"
                    + " diem BIGINT(20) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (player_id, ruong_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_lich_su ("
                    + " id BIGINT(20) NOT NULL AUTO_INCREMENT,"
                    + " player_id INT(11) NOT NULL,"
                    + " ruong_id INT(11) NOT NULL,"
                    + " item_id INT(11) NOT NULL,"
                    + " so_luong INT(11) NOT NULL,"
                    + " hiem INT(11) NOT NULL DEFAULT 0,"
                    + " luc BIGINT(20) NOT NULL,"
                    + " PRIMARY KEY (id), KEY (player_id), KEY (luc)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // Them cot TRUOC khi gieo: gieo ghi vao hai cot moi.
            themCotMoi();
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_cau_hinh ("
                    + " khoa VARCHAR(40) NOT NULL,"
                    + " gia_tri VARCHAR(80) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (khoa)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            if (demDong("mo_ruong_loai") == 0) {
                gieoMacDinh();
            }
            suaQuaTheoIdThat();
            datTrungCellVang();
            doiQuaV2();
            datPhieuV1();
        } catch (Exception ex) {
            daTaoBang = false;
            Logger.logException(MoRuongDAO.class, ex, "Không tạo được bảng mở rương");
        }
    }

    /**
     * Cột thêm sau: icon hình và tên điểm của từng rương.
     */
    private static void themCotMoi() {
        try {
            ConnectDB.executeUpdate("ALTER TABLE mo_ruong_loai ADD COLUMN icon_hinh INT(11) NOT NULL DEFAULT 0");
        } catch (Exception daCo) {
            // Cot da co: khong phai loi.
        }
        try {
            ConnectDB.executeUpdate("ALTER TABLE mo_ruong_loai ADD COLUMN ten_diem VARCHAR(60) NOT NULL DEFAULT ''");
        } catch (Exception daCo) {
            // Cot da co: khong phai loi.
        }
        try {
            ConnectDB.executeUpdate("ALTER TABLE mo_ruong_loai ADD COLUMN item_phieu INT(11) NOT NULL DEFAULT 0");
        } catch (Exception daCo) {
            // Cot da co: khong phai loi.
        }
    }

    /** Cờ: đã sửa quà theo id vật phẩm thật chưa. */
    private static final String CO_SUA_QUA = "qua_id_that_v1";

    /** Tỉ lệ của Trứng Mabư và hai rương thú cưng trong rương, tính bằng phần trăm. */
    private static final double TI_LE_MON_VANG = 2.0;

    /**
     * Đặt Trứng Mabư và hai Rương Thú Cưng vào rương theo <b>id thật của máy
     * này</b> — đúng một lần.
     *
     * <p>Ba món này là vật phẩm tự tạo, máy chủ cấp id lúc dựng nên mỗi máy một
     * khác. Bản đầu viết cứng 2453/2454 theo một máy; trên máy khác hai con số
     * ấy rơi vào quả trứng đệ tử, nên Rương Thường ra "trứng Bill".</p>
     *
     * <p>Làm gì: bỏ mọi dòng quà trỏ 2453/2454 hay trỏ tới chính ba món ấy, rồi
     * thêm lại cho đúng — Rương Thường: Trứng Mabư + Rương Thú Cưng Thường,
     * Rương Sự Kiện: Rương Thú Cưng Cao Cấp; mỗi món 2% và nền vàng (Huyền
     * thoại). Trọng số tính theo tổng của các món còn lại nên những món quản
     * trị đã chỉnh không bị đụng.</p>
     *
     * <p>Chưa tra được id (máy chưa dựng xong trứng / rương) thì không ghi cờ,
     * lần khởi động sau làm lại.</p>
     */
    private static void suaQuaTheoIdThat() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = ?", CO_SUA_QUA);
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        int trung = TrungDeTuDAO.itemCuaLoai(nro.core.consts.ConstDetu.MABU);
        int rtThuong = ThuCungDAO.idRuongThuong();
        int rtCaoCap = ThuCungDAO.idRuongCaoCap();
        if (trung <= 0 || rtThuong <= 0 || rtCaoCap <= 0) {
            Logger.warning("Mở rương: chưa tra được id trứng/rương thú cưng, để lần khởi động sau\n");
            return;
        }
        int thuong = idTheoTenRuong("Rương Thường");
        int suKien = idTheoTenRuong("Rương Sự Kiện");
        if (thuong > 0) {
            thayMonVang(thuong, new int[]{trung, rtThuong}, 2453, 2454, trung, rtThuong, rtCaoCap);
        }
        if (suKien > 0) {
            thayMonVang(suKien, new int[]{rtCaoCap}, 2453, 2454, trung, rtThuong, rtCaoCap);
        }
        ConnectDB.executeUpdate("INSERT IGNORE INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES (?, '1')", CO_SUA_QUA);
        lucDoc = 0;
        Logger.success("Mở rương: đặt Trứng Mabư (" + trung + "), Rương Thú Cưng Thường (" + rtThuong
                + "), Rương Thú Cưng Cao Cấp (" + rtCaoCap + ") theo id thật\n");
    }

    /**
     * Bỏ các dòng trỏ {@code bo}, rồi thêm {@code them} — mỗi món đúng
     * {@link #TI_LE_MON_VANG}% của tổng mới, độ hiếm Huyền thoại.
     */
    private static void thayMonVang(int ruongId, int[] them, int... bo) throws Exception {
        for (int id : bo) {
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_qua WHERE ruong_id = ? AND item_id = ?", ruongId, id);
        }
        long conLai = 0;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT SUM(trong_so) AS t FROM mo_ruong_qua WHERE ruong_id = ?", ruongId);
            if (rs.next()) {
                conLai = rs.getLong("t");
            }
        } finally {
            dong(rs);
        }
        // Cho ra DUNG 2%, khong phai 1,99%: goi D = 100 / 2 = 50 phan. Nhan cac
        // mon con lai len (D - n) lan, moi mon vang lay dung tong cu S. Tong moi
        // = S*(D-n) + n*S = S*D, nen moi mon vang = S / (S*D) = 1/D = 2% tron.
        // Ti le giua cac mon con lai khong doi vi cung nhan mot he so.
        int d = (int) Math.round(100.0 / TI_LE_MON_VANG);
        int n = them.length;
        if (conLai <= 0) {
            conLai = 100;
        } else {
            ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET trong_so = trong_so * ? WHERE ruong_id = ?",
                    d - n, ruongId);
        }
        for (int itemId : them) {
            themQua(ruongId, itemId, 1, (int) Math.min(Integer.MAX_VALUE, conLai), 3, "");
        }
    }

    /** Cờ: đã đặt Trứng Cell trong Rương Sự Kiện thành nền vàng 1,5% chưa. */
    private static final String CO_TRUNG_CELL = "trung_cell_1_5_v1";

    /**
     * Trứng Cell trong Rương Sự Kiện: nền vàng (Huyền thoại), đúng 1,5% — một lần.
     *
     * <p>Rương Thú Cưng Cao Cấp giữ đúng 2%; mọi món khác chia phần còn lại,
     * giữ nguyên tỉ lệ giữa chúng. Chưa có dòng Trứng Cell thì thêm. Chưa tra
     * được id trứng thì không ghi cờ, lần khởi động sau làm lại.</p>
     */
    private static void datTrungCellVang() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = ?", CO_TRUNG_CELL);
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        int cell = TrungDeTuDAO.itemCuaLoai(nro.core.consts.ConstDetu.CELL);
        int rtCaoCap = ThuCungDAO.idRuongCaoCap();
        if (cell <= 0) {
            return;
        }
        int suKien = idTheoTenRuong("Rương Sự Kiện");
        if (suKien > 0) {
            boolean coCell = false;
            lucDoc = 0;
            for (Qua q : dsQuaDayDu(suKien)) {
                if (q.itemId == cell) {
                    coCell = true;
                }
            }
            if (!coCell) {
                themQua(suKien, cell, 1, 1, 3, "");
            }
            ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET hiem = 3 WHERE ruong_id = ? AND item_id = ?",
                    suKien, cell);
            java.util.Map<Integer, Integer> coDinh = new java.util.LinkedHashMap<>();
            coDinh.put(cell, 1500);          // 1,5%
            if (rtCaoCap > 0) {
                coDinh.put(rtCaoCap, 2000);  // 2%
            }
            datTiLeCoDinh(suKien, coDinh);
        }
        ConnectDB.executeUpdate("INSERT IGNORE INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES (?, '1')", CO_TRUNG_CELL);
        lucDoc = 0;
        Logger.success("Mở rương: Trứng Cell (" + cell + ") trong Rương Sự Kiện → nền vàng 1,5%\n");
    }

    /**
     * Đổi quà đời đầu — một lần: đậu thần thành Cuồng nộ + Bổ huyết (x2, chia
     * đôi trọng số), Rương Bạc thành Ngọc Rồng 4 sao, Rương Vàng thành Giáp Xên
     * bọ hung 2, Rương ngọc rồng thành Ngọc Rồng 3 sao. Trọng số tổng không đổi
     * nên tỉ lệ các món vàng giữ nguyên.
     */
    private static void doiQuaV2() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = 'doi_qua_v2'");
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        lucDoc = 0;
        for (Ruong r : dsRuong(false)) {
            for (Qua q : dsQuaDayDu(r.id)) {
                if (q.itemId != 595) {
                    continue;
                }
                int nua = q.trongSo / 2;
                ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET item_id = 381, so_luong = 2, trong_so = ?"
                        + " WHERE id = ?", q.trongSo - nua, q.id);
                if (nua > 0) {
                    themQua(r.id, 382, 2, nua, q.hiem, "");
                }
            }
        }
        ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET item_id = 17, so_luong = 1 WHERE item_id = 571");
        ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET item_id = 1153, so_luong = 1 WHERE item_id = 572");
        ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET item_id = 16, so_luong = 1 WHERE item_id = 1560");
        ConnectDB.executeUpdate("INSERT IGNORE INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES ('doi_qua_v2', '1')");
        lucDoc = 0;
        Logger.success("Mở rương: đổi đậu thần / rương bạc / rương vàng / rương ngọc rồng sang món mới\n");
    }

    // =====================================================================
    //  Phiếu quay rương
    // =====================================================================
    /** Khoá cấu hình giữ id vật phẩm của hai phiếu (id mỗi máy một khác). */
    public static final String K_PHIEU_THUONG = "phieu_thuong";
    public static final String K_PHIEU_SU_KIEN = "phieu_su_kien";

    /** Ảnh hai phiếu — tệp nằm sẵn trong data/icon. */
    private static final int ICON_PHIEU_THUONG = 25252;
    private static final int ICON_PHIEU_SU_KIEN = 25253;

    /** Kiểu vật phẩm linh tinh, cùng kiểu với rương thú cưng. */
    private static final int KIEU_PHIEU = 27;

    /**
     * Dựng hai vật phẩm phiếu nếu chưa có, rồi gắn chúng vào hai rương.
     *
     * <p>Gọi <b>sau</b> {@code loadDatabase} (trong {@code Manager}): phải có
     * danh sách vật phẩm trong bộ nhớ mới nối dòng mới vào đúng chỗ. Id cấp
     * bằng {@code MAX(id) + 1} rồi nhớ vào {@code mo_ruong_cau_hinh} — giống rương
     * thú cưng và trứng đệ tử.</p>
     */
    public static synchronized void damBaoVatPhamPhieu() {
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS mo_ruong_cau_hinh ("
                    + " khoa VARCHAR(40) NOT NULL,"
                    + " gia_tri VARCHAR(80) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (khoa)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            damBaoMotPhieu(K_PHIEU_THUONG, "Phiếu quay rương thường", ICON_PHIEU_THUONG,
                    "Dùng để mở Rương Thường ở tab Mở rương (màn Sự kiện)");
            damBaoMotPhieu(K_PHIEU_SU_KIEN, "Phiếu quay rương trung cấp", ICON_PHIEU_SU_KIEN,
                    "Dùng để mở Rương Trung Cấp ở tab Mở rương (màn Sự kiện)");
            damBaoBang();
            datPhieuV1();
            doiTenTrungCapV1();
            batGopChong();
            doiQuaV3();
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không dựng được phiếu quay rương");
        }
    }

    /** Id các món rương / hộp / phiếu (xếp chồng được) — để gộp ô cũ lúc nạp nhân vật. */
    private static volatile java.util.Set<Integer> monGopChong = java.util.Collections.emptySet();

    public static boolean laMonGopChong(int id) {
        return monGopChong.contains(id);
    }

    /** Điều kiện tên của nhóm rương / hộp / phiếu (vật phẩm linh tinh, kiểu 27). */
    private static final String DK_GOP_CHONG = "TYPE = 27 AND (NAME LIKE 'Rương%' OR NAME LIKE 'Hộp%'"
            + " OR NAME LIKE 'Hòm%' OR NAME LIKE 'Phiếu%')";

    /**
     * Cho mọi rương / hộp / phiếu xếp chồng — mỗi lần khởi động (rẻ, và bắt
     * luôn món mới thêm sau). Các hàm mở đều trừ đúng một cái nên xếp chồng
     * không đổi cách mở. Cập nhật cả bản trong bộ nhớ, và nhớ danh sách id để
     * gộp ô cũ trong túi lúc nạp nhân vật.
     */
    private static void batGopChong() throws Exception {
        int doi = ConnectDB.executeUpdate("UPDATE item_template SET is_up_to_up = 1 WHERE is_up_to_up = 0 AND "
                + DK_GOP_CHONG);
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM item_template WHERE " + DK_GOP_CHONG);
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } finally {
            dong(rs);
        }
        monGopChong = ids;
        boolean coDoi = false;
        List<nro.entity.template.ItemTemplate> ds = nro.server.Manager.ITEM_TEMPLATES;
        if (ds != null) {
            for (int id : ids) {
                if (id >= 0 && id < ds.size() && ds.get(id) != null && !ds.get(id).isUpToUp) {
                    ds.get(id).isUpToUp = true;
                    coDoi = true;
                }
            }
        }
        if (coDoi || doi > 0) {
            nro.ui.LamMoi.bao(nro.ui.LamMoi.VAT_PHAM);
            Logger.success("Rương / hộp / phiếu: bật xếp chồng (" + ids.size() + " món)\n");
        }
    }

    /** Độ hiếm Thần thoại (đỏ) — trên Huyền thoại. */
    public static final int HIEM_DO = 4;

    /**
     * Đổi quà đợt ba — một lần:
     * <ul>
     * <li>Rương sao pha lê (1440) → Hộp sao pha lê (1964), giữ trọng số và độ
     * hiếm; bỏ Rương sao pha lê VIP (1453).</li>
     * <li>Rương Thường thêm Capsule 1 món kích hoạt (1559), Rương Trung Cấp thêm
     * Capsule kích hoạt 1 món tự chọn (1655) — đỏ, đúng 0,5%.</li>
     * </ul>
     * Các món cố định cũ (trứng, rương thú cưng) giữ đúng tỉ lệ; phần còn lại
     * chia lại theo tỉ lệ cũ giữa chúng. Chưa tra được id trứng / rương thú
     * cưng thì không ghi cờ, lần sau làm lại.
     */
    private static void doiQuaV3() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = 'qua_v3'");
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        int mabu = TrungDeTuDAO.itemCuaLoai(nro.core.consts.ConstDetu.MABU);
        int cell = TrungDeTuDAO.itemCuaLoai(nro.core.consts.ConstDetu.CELL);
        int rtThuong = ThuCungDAO.idRuongThuong();
        int rtCaoCap = ThuCungDAO.idRuongCaoCap();
        if (mabu <= 0 || cell <= 0 || rtThuong <= 0 || rtCaoCap <= 0) {
            return;
        }
        int thuong = idTheoTenRuong("Rương Thường");
        int trung = idTheoTenRuong(TEN_TRUNG_CAP);
        for (int r : new int[]{thuong, trung}) {
            if (r > 0) {
                ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET item_id = 1964, so_luong = 1"
                        + " WHERE ruong_id = ? AND item_id = 1440", r);
                ConnectDB.executeUpdate("DELETE FROM mo_ruong_qua WHERE ruong_id = ? AND item_id = 1453", r);
            }
        }
        if (thuong > 0) {
            themMonDo(thuong, 1559);
            java.util.Map<Integer, Integer> coDinh = new java.util.LinkedHashMap<>();
            coDinh.put(mabu, 2000);
            coDinh.put(rtThuong, 2000);
            coDinh.put(1559, 500);
            datTiLeCoDinh(thuong, coDinh);
        }
        if (trung > 0) {
            themMonDo(trung, 1655);
            java.util.Map<Integer, Integer> coDinh = new java.util.LinkedHashMap<>();
            coDinh.put(rtCaoCap, 2000);
            coDinh.put(cell, 1500);
            coDinh.put(1655, 500);
            datTiLeCoDinh(trung, coDinh);
        }
        ConnectDB.executeUpdate("INSERT IGNORE INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES ('qua_v3', '1')");
        lucDoc = 0;
        Logger.success("Mở rương: Hộp sao pha lê thay Rương sao pha lê; thêm capsule kích hoạt đỏ 0,5%\n");
    }

    /** Thêm món đỏ vào rương nếu chưa có, rồi đặt độ hiếm đỏ. */
    private static void themMonDo(int ruongId, int itemId) throws Exception {
        boolean co = false;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM mo_ruong_qua WHERE ruong_id = ? AND item_id = ?", ruongId, itemId);
            co = rs.next();
        } finally {
            dong(rs);
        }
        if (!co) {
            themQua(ruongId, itemId, 1, 1, HIEM_DO, "");
        }
        ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET hiem = ? WHERE ruong_id = ? AND item_id = ?",
                HIEM_DO, ruongId, itemId);
    }

    /** Tên mới của rương thứ hai (trước là "Rương Sự Kiện"). */
    public static final String TEN_TRUNG_CAP = "Rương Trung Cấp";

    /**
     * Đổi "Rương Sự Kiện" thành "Rương Trung Cấp" — một lần: tên rương, mô tả,
     * tên điểm, và tên/mô tả vật phẩm phiếu (cả trong bộ nhớ lẫn CSDL).
     */
    private static void doiTenTrungCapV1() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = 'ten_trung_cap_v1'");
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET ten = ? WHERE ten = 'Rương Sự Kiện'", TEN_TRUNG_CAP);
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET mo_ta = REPLACE(mo_ta, 'Rương cao cấp', 'Rương trung cấp'),"
                + " ten_diem = REPLACE(REPLACE(ten_diem, 'sự kiện', 'trung cấp'), 'Sự Kiện', 'Trung Cấp')"
                + " WHERE ten = ?", TEN_TRUNG_CAP);
        int phieu = idPhieuSuKien();
        if (phieu > 0) {
            String ten = "Phiếu quay rương trung cấp";
            String moTa = "Dùng để mở Rương Trung Cấp ở tab Mở rương (màn Sự kiện)";
            ConnectDB.executeUpdate("UPDATE item_template SET NAME = ?, description = ? WHERE id = ?", ten, moTa, phieu);
            try {
                nro.entity.template.ItemTemplate t = nro.service.item.ItemService.gI().getTemplate(phieu);
                if (t != null) {
                    t.name = ten;
                    t.description = moTa;
                    nro.ui.LamMoi.bao(nro.ui.LamMoi.VAT_PHAM);
                }
            } catch (Exception boQua) {
                // Lan khoi dong sau nap lai tu CSDL.
            }
        }
        ConnectDB.executeUpdate("INSERT IGNORE INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES ('ten_trung_cap_v1', '1')");
        lucDoc = 0;
        Logger.success("Mở rương: đổi Rương Sự Kiện → Rương Trung Cấp\n");
    }

    /** Id vật phẩm phiếu quay rương thường trên máy này, hoặc -1. */
    public static int idPhieuThuong() {
        return soCauHinhRuong(K_PHIEU_THUONG);
    }

    /** Id vật phẩm phiếu quay rương sự kiện trên máy này, hoặc -1. */
    public static int idPhieuSuKien() {
        return soCauHinhRuong(K_PHIEU_SU_KIEN);
    }

    private static int soCauHinhRuong(String khoa) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = ?", khoa);
            return rs.next() ? Integer.parseInt(rs.getString("gia_tri").trim()) : -1;
        } catch (Exception ex) {
            return -1;
        } finally {
            dong(rs);
        }
    }

    private static void damBaoMotPhieu(String khoa, String ten, int icon, String moTa) throws Exception {
        int id = soCauHinhRuong(khoa);
        if (id > 0 && coVatPham(id)) {
            return;
        }
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(id) AS m FROM item_template");
            if (!rs.next()) {
                return;
            }
            id = rs.getInt("m") + 1;
        } finally {
            dong(rs);
        }
        if (id <= 0 || id > 32766) {
            return;
        }
        // is_up_to_up = 1: phieu xep chong trong mot o, dung_duoc = 0: khong bam
        // "dung" duoc, chi dung khi mo ruong.
        ConnectDB.executeUpdate("INSERT INTO item_template"
                + " (id, TYPE, gender, NAME, description, level, icon_id, part,"
                + " is_up_to_up, power_require, gold, gold_sell, gem, gem_sell,"
                + " ruby, ruby_sell, head, body, leg, TypeEvent, isGender,"
                + " dung_duoc, aura_id)"
                + " VALUES (?, ?, 3, ?, ?, 1, ?, -1, 1, 0, 0, 0, 0, 0, 0, 0,"
                + " -1, -1, -1, 0, -1, 0, -1)",
                id, KIEU_PHIEU, ten, moTa, icon);
        ConnectDB.executeUpdate("INSERT INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES (?, ?)"
                + " ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri)", khoa, String.valueOf(id));
        try {
            List<nro.entity.template.ItemTemplate> ds = nro.server.Manager.ITEM_TEMPLATES;
            if (ds != null && ds.size() == id) {
                nro.entity.template.ItemTemplate t = new nro.entity.template.ItemTemplate();
                t.id = (short) id;
                t.type = (byte) KIEU_PHIEU;
                t.gender = 3;
                t.name = ten;
                t.description = moTa;
                t.level = 1;
                t.iconID = (short) icon;
                t.part = -1;
                t.isUpToUp = true;
                t.strRequire = 0;
                t.head = -1;
                t.body = -1;
                t.leg = -1;
                t.isGender = -1;
                t.dungDuoc = false;
                ds.add(t);
                nro.ui.LamMoi.bao(nro.ui.LamMoi.VAT_PHAM);
            }
        } catch (Exception boQua) {
            // Khong noi duoc vao bo nho thi dong trong CSDL van con: lan khoi dong sau se nap.
        }
        Logger.success("Mở rương: thêm " + ten + " (id " + id + ", icon " + icon + ")\n");
    }

    private static boolean coVatPham(int id) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM item_template WHERE id = ?", id);
            return rs.next();
        } finally {
            dong(rs);
        }
    }

    /**
     * Gắn phiếu vào hai rương — một lần: Rương Thường mở bằng phiếu thường,
     * Rương Sự Kiện bằng phiếu sự kiện; giá x1 = 1 phiếu, x10 = 10 phiếu.
     * Chưa có phiếu (chưa dựng xong) thì không ghi cờ, lần sau làm lại.
     */
    private static void datPhieuV1() throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT gia_tri FROM mo_ruong_cau_hinh WHERE khoa = 'phieu_v1'");
            if (rs.next()) {
                return;
            }
        } finally {
            dong(rs);
        }
        int thuong = idPhieuThuong();
        int suKien = idPhieuSuKien();
        if (thuong <= 0 || suKien <= 0) {
            return;
        }
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET item_phieu = ?, gia_x1 = 1, gia_x10 = 10,"
                + " ten_diem = 'Phiếu quay rương thường' WHERE ten = 'Rương Thường'", thuong);
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET item_phieu = ?, gia_x1 = 1, gia_x10 = 10,"
                + " ten_diem = 'Phiếu quay rương trung cấp' WHERE ten IN ('Rương Sự Kiện', 'Rương Trung Cấp')", suKien);
        ConnectDB.executeUpdate("INSERT IGNORE INTO mo_ruong_cau_hinh (khoa, gia_tri) VALUES ('phieu_v1', '1')");
        lucDoc = 0;
        Logger.success("Mở rương: hai rương giờ mở bằng phiếu (x1 = 1 phiếu, x10 = 10 phiếu)\n");
    }

    /** Thang trọng số khi đặt tỉ lệ cố định: 100.000 phần = 100%. */
    private static final int THANG_TI_LE = 100_000;

    /**
     * Đặt một số món của rương về <b>đúng</b> tỉ lệ cho trước; các món còn lại
     * chia phần dư, giữ nguyên tỉ lệ giữa chúng.
     *
     * <p>Mọi trọng số quy về thang {@link #THANG_TI_LE}: món cố định nhận đúng
     * số phần của nó, món còn lại nhận phần tương ứng rồi phần lẻ do làm tròn
     * dồn vào món lớn nhất — tổng luôn tròn 100.000, nên món cố định hiện đúng
     * con số đặt, không lệch 0,01%.</p>
     *
     * @param coDinh id vật phẩm → số phần trên 100.000 (1.500 = 1,5%)
     */
    public static void datTiLeCoDinh(int ruongId, java.util.Map<Integer, Integer> coDinh) throws Exception {
        lucDoc = 0;
        List<Qua> ds = dsQuaDayDu(ruongId);
        long tongKhac = 0;
        int phanCoDinh = 0;
        java.util.Set<Integer> daDat = new java.util.HashSet<>();
        for (Qua q : ds) {
            if (coDinh.containsKey(q.itemId)) {
                if (daDat.add(q.itemId)) {
                    phanCoDinh += coDinh.get(q.itemId);
                }
            } else {
                tongKhac += Math.max(0, q.trongSo);
            }
        }
        int conLai = Math.max(0, THANG_TI_LE - phanCoDinh);
        daDat.clear();
        int daChia = 0;
        Qua lonNhat = null;
        java.util.Map<Integer, Integer> moi = new java.util.HashMap<>();
        for (Qua q : ds) {
            int w;
            if (coDinh.containsKey(q.itemId)) {
                // Trung mon (hai dong cung mot vat pham) thi dong sau ve 0.
                w = daDat.add(q.itemId) ? coDinh.get(q.itemId) : 0;
            } else {
                w = tongKhac <= 0 ? 0 : (int) Math.round((double) Math.max(0, q.trongSo) * conLai / tongKhac);
                daChia += w;
                if (lonNhat == null || w > moi.getOrDefault(lonNhat.id, 0)) {
                    lonNhat = q;
                }
            }
            moi.put(q.id, w);
        }
        if (lonNhat != null) {
            moi.put(lonNhat.id, moi.get(lonNhat.id) + (conLai - daChia));
        }
        for (java.util.Map.Entry<Integer, Integer> e : moi.entrySet()) {
            ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET trong_so = ? WHERE id = ?", e.getValue(), e.getKey());
        }
        lucDoc = 0;
    }

    private static int idTheoTenRuong(String ten) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM mo_ruong_loai WHERE ten = ? OR (? = 'Rương Sự Kiện'"
                    + " AND ten = ?) ORDER BY id LIMIT 1", ten, ten, TEN_TRUNG_CAP);
            return rs.next() ? rs.getInt("id") : -1;
        } finally {
            dong(rs);
        }
    }

    /**
     * Hai rương mặc định. Rương thường rẻ, quà đa phần là đồ dùng hằng ngày;
     * rương sự kiện đắt gấp ba nhưng toàn rương con và có cửa ra thú cưng cao
     * cấp. Tỉ lệ huyền thoại để thấp (1–2%) — thấp quá thì không ai tin là có.
     */
    private static void gieoMacDinh() throws Exception {
        int thuong = themRuong("Rương Thường", "Rương cơ bản — đồ dùng hằng ngày, có cửa ra thú cưng.",
                571, 10, 90, 1);
        themQua(thuong, 457, 1, 4000, 0, "");      // Thoi vang
        themQua(thuong, 381, 2, 1500, 0, "");      // Cuong no
        themQua(thuong, 382, 2, 1500, 0, "");      // Bo huyet
        themQua(thuong, 380, 1, 1500, 1, "");      // Vien capsule ki bi
        themQua(thuong, 987, 1, 1000, 1, "");      // Da bao ve
        themQua(thuong, 17, 1, 400, 2, "");        // Ngoc Rong 4 sao
        // Trung Mabu va Ruong thu cung thuong: them trong suaQuaTheoIdThat (id
        // cua hai mon nay moi may mot khac).

        int suKien = themRuong(TEN_TRUNG_CAP, "Rương trung cấp — toàn rương con, cửa ra thú cưng cao cấp.",
                1960, 30, 270, 2);
        themQua(suKien, 457, 3, 3500, 0, "");      // Thoi vang
        themQua(suKien, 1440, 1, 2500, 1, "");     // Ruong sao pha le
        themQua(suKien, 1153, 1, 2000, 1, "");     // Giap Xen bo hung 2
        themQua(suKien, 16, 1, 1200, 2, "");       // Ngoc Rong 3 sao
        themQua(suKien, 1453, 1, 600, 2, "");      // Ruong sao pha le VIP
        // Ruong thu cung cao cap: them trong suaQuaTheoIdThat.
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET ten_diem = 'Điểm Rương Thường' WHERE id = ?", thuong);
        ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET ten_diem = 'Điểm Rương Trung Cấp' WHERE id = ?", suKien);
        Logger.success("Mở rương: gieo 2 rương mặc định\n");
    }

    // =====================================================================
    //  Rương
    // =====================================================================
    private static final List<Ruong> RUONG = new ArrayList<>();
    private static final List<Qua> QUA = new ArrayList<>();
    private static long lucDoc;

    private static synchronized void doc() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDoc < HAN_BO_NHO_MS && lucDoc != 0) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM mo_ruong_loai ORDER BY thu_tu, id");
            List<Ruong> r = new ArrayList<>();
            while (rs.next()) {
                Ruong x = new Ruong();
                x.id = rs.getInt("id");
                x.ten = khongNull(rs.getString("ten"));
                x.moTa = khongNull(rs.getString("mo_ta"));
                x.itemHinh = rs.getInt("item_hinh");
                try {
                    x.iconHinh = rs.getInt("icon_hinh");
                    x.tenDiem = khongNull(rs.getString("ten_diem"));
                    x.itemPhieu = rs.getInt("item_phieu");
                } catch (Exception chuaCoCot) {
                    x.iconHinh = 0;
                    x.tenDiem = "";
                    x.itemPhieu = 0;
                }
                x.giaX1 = rs.getInt("gia_x1");
                x.giaX10 = rs.getInt("gia_x10");
                x.thuTu = rs.getInt("thu_tu");
                x.bat = rs.getInt("bat") == 1;
                r.add(x);
            }
            dong(rs);
            rs = ConnectDB.executeQuery("SELECT * FROM mo_ruong_qua ORDER BY ruong_id, hiem DESC, trong_so, id");
            List<Qua> q = new ArrayList<>();
            while (rs.next()) {
                Qua x = new Qua();
                x.id = rs.getInt("id");
                x.ruongId = rs.getInt("ruong_id");
                x.itemId = rs.getInt("item_id");
                x.soLuong = rs.getInt("so_luong");
                x.trongSo = rs.getInt("trong_so");
                x.hiem = rs.getInt("hiem");
                x.chiSo = khongNull(rs.getString("chi_so"));
                q.add(x);
            }
            RUONG.clear();
            RUONG.addAll(r);
            QUA.clear();
            QUA.addAll(q);
            lucDoc = bayGio;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được bảng mở rương");
        } finally {
            dong(rs);
        }
    }

    public static synchronized List<Ruong> dsRuong(boolean chiBat) {
        doc();
        List<Ruong> ra = new ArrayList<>();
        for (Ruong r : RUONG) {
            if (!chiBat || r.bat) {
                ra.add(r);
            }
        }
        return ra;
    }

    public static synchronized Ruong ruong(int id) {
        doc();
        for (Ruong r : RUONG) {
            if (r.id == id) {
                return r;
            }
        }
        return null;
    }

    public static synchronized List<Qua> dsQua(int ruongId) {
        doc();
        List<Qua> ra = new ArrayList<>();
        for (Qua q : QUA) {
            if (q.ruongId == ruongId && q.trongSo > 0) {
                ra.add(q);
            }
        }
        return ra;
    }

    /** Mọi quà của rương, kể cả trọng số 0 (panel cần thấy). */
    public static synchronized List<Qua> dsQuaDayDu(int ruongId) {
        doc();
        List<Qua> ra = new ArrayList<>();
        for (Qua q : QUA) {
            if (q.ruongId == ruongId) {
                ra.add(q);
            }
        }
        return ra;
    }

    public static int themRuong(String ten, String moTa, int itemHinh, int giaX1, int giaX10, int thuTu)
            throws Exception {
        ConnectDB.executeUpdate("INSERT INTO mo_ruong_loai (ten, mo_ta, item_hinh, gia_x1, gia_x10, thu_tu)"
                + " VALUES (?, ?, ?, ?, ?, ?)", ten, moTa, itemHinh, giaX1, giaX10, thuTu);
        lucDoc = 0;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(id) AS m FROM mo_ruong_loai");
            return rs.next() ? rs.getInt("m") : 0;
        } finally {
            dong(rs);
        }
    }

    public static void luuRuong(Ruong r) {
        try {
            ConnectDB.executeUpdate("UPDATE mo_ruong_loai SET ten = ?, mo_ta = ?, item_hinh = ?, icon_hinh = ?,"
                    + " ten_diem = ?, gia_x1 = ?, gia_x10 = ?, thu_tu = ?, bat = ?, item_phieu = ? WHERE id = ?", r.ten,
                    r.moTa, r.itemHinh, r.iconHinh, r.tenDiem == null ? "" : r.tenDiem.trim(), r.giaX1, r.giaX10,
                    r.thuTu, r.bat ? 1 : 0, r.itemPhieu, r.id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không lưu được rương");
        }
    }

    public static void xoaRuong(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_qua WHERE ruong_id = ?", id);
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_loai WHERE id = ?", id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không xoá được rương");
        }
    }

    public static void themQua(int ruongId, int itemId, int soLuong, int trongSo, int hiem, String chiSo)
            throws Exception {
        ConnectDB.executeUpdate("INSERT INTO mo_ruong_qua (ruong_id, item_id, so_luong, trong_so, hiem, chi_so)"
                + " VALUES (?, ?, ?, ?, ?, ?)", ruongId, itemId, Math.max(1, soLuong), Math.max(0, trongSo),
                Math.max(0, Math.min(3, hiem)), chiSo == null ? "" : chiSo.trim());
        lucDoc = 0;
    }

    public static void luuQua(Qua q) {
        try {
            ConnectDB.executeUpdate("UPDATE mo_ruong_qua SET so_luong = ?, trong_so = ?, hiem = ?, chi_so = ?"
                    + " WHERE id = ?", Math.max(1, q.soLuong), Math.max(0, q.trongSo),
                    Math.max(0, Math.min(3, q.hiem)), q.chiSo == null ? "" : q.chiSo.trim(), q.id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không lưu được quà rương");
        }
    }

    public static void xoaQua(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM mo_ruong_qua WHERE id = ?", id);
            lucDoc = 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không xoá được quà rương");
        }
    }

    // =====================================================================
    //  Điểm
    // =====================================================================
    /** Điểm của một người ở một loại rương. */
    public static long diem(long playerId, int ruongId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT diem FROM mo_ruong_diem_loai WHERE player_id = ? AND ruong_id = ?",
                    playerId, ruongId);
            return rs.next() ? rs.getLong("diem") : 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được điểm rương");
            return 0;
        } finally {
            dong(rs);
        }
    }

    /** Điểm của một người ở mọi loại rương: id rương → điểm (loại chưa có dòng thì không có khoá). */
    public static java.util.Map<Integer, Long> diemCuaNguoi(long playerId) {
        damBaoBang();
        java.util.Map<Integer, Long> ra = new java.util.HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT ruong_id, diem FROM mo_ruong_diem_loai WHERE player_id = ?", playerId);
            while (rs.next()) {
                ra.put(rs.getInt("ruong_id"), rs.getLong("diem"));
            }
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được điểm rương");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Cộng (hoặc trừ, nếu âm) điểm một loại rương; không để xuống dưới 0. */
    public static void congDiem(long playerId, int ruongId, long them) {
        try {
            damBaoBang();
            if (them >= 0) {
                ConnectDB.executeUpdate("INSERT INTO mo_ruong_diem_loai (player_id, ruong_id, diem) VALUES (?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE diem = diem + VALUES(diem)", playerId, ruongId, them);
            } else {
                ConnectDB.executeUpdate("UPDATE mo_ruong_diem_loai SET diem = GREATEST(0, diem + ?)"
                        + " WHERE player_id = ? AND ruong_id = ?", them, playerId, ruongId);
            }
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không cộng được điểm rương");
        }
    }

    /** Đặt thẳng điểm một loại rương (panel chỉnh tay). */
    public static void datDiem(long playerId, int ruongId, long diem) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO mo_ruong_diem_loai (player_id, ruong_id, diem) VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE diem = VALUES(diem)", playerId, ruongId, Math.max(0, diem));
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đặt được điểm rương");
        }
    }

    /**
     * Trừ điểm một loại rương nếu đủ, trong MỘT câu lệnh.
     *
     * <p>Điều kiện {@code diem >= ?} nằm ngay trong câu UPDATE: hai lượt mở đến
     * cùng lúc thì chỉ lượt nào còn đủ điểm mới trừ được — không có khoảng hở
     * giữa lúc đọc và lúc ghi để trừ âm.</p>
     *
     * @return {@code true} nếu đã trừ
     */
    public static boolean truDiem(long playerId, int ruongId, long gia) {
        try {
            damBaoBang();
            return ConnectDB.executeUpdate("UPDATE mo_ruong_diem_loai SET diem = diem - ?"
                    + " WHERE player_id = ? AND ruong_id = ? AND diem >= ?", gia, playerId, ruongId, gia) > 0;
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không trừ được điểm rương");
            return false;
        }
    }

    public static void ghiLichSu(long playerId, int ruongId, int itemId, int soLuong, int hiem) {
        try {
            ConnectDB.executeUpdate("INSERT INTO mo_ruong_lich_su (player_id, ruong_id, item_id, so_luong, hiem, luc)"
                    + " VALUES (?, ?, ?, ?, ?, ?)", playerId, ruongId, itemId, soLuong, hiem,
                    System.currentTimeMillis());
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không ghi được lịch sử mở rương");
        }
    }

    /** Một dòng lịch sử cho panel. */
    public static final class DongLichSu {

        public String nguoi = "";
        public int ruongId;
        public int itemId;
        public int soLuong;
        public int hiem;
        public long luc;
    }

    public static List<DongLichSu> lichSu(int toiDa) {
        damBaoBang();
        List<DongLichSu> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT l.ruong_id, l.item_id, l.so_luong, l.hiem, l.luc, p.name"
                    + " FROM mo_ruong_lich_su l LEFT JOIN player p ON p.id = l.player_id"
                    + " ORDER BY l.id DESC LIMIT " + Math.max(1, toiDa));
            while (rs.next()) {
                DongLichSu d = new DongLichSu();
                d.ruongId = rs.getInt("ruong_id");
                d.itemId = rs.getInt("item_id");
                d.soLuong = rs.getInt("so_luong");
                d.hiem = rs.getInt("hiem");
                d.luc = rs.getLong("luc");
                d.nguoi = khongNull(rs.getString("name"));
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(MoRuongDAO.class, ex, "Không đọc được lịch sử mở rương");
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static long idTheoTen(String ten) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM player WHERE name = ?", ten);
            return rs.next() ? rs.getLong("id") : -1;
        } catch (Exception ex) {
            return -1;
        } finally {
            dong(rs);
        }
    }

    // =====================================================================
    private static String khongNull(String s) {
        return s == null ? "" : s;
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
