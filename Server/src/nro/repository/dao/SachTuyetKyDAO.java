package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.template.ItemOptionTemplate;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.schema.LuocDoPanel;
import nro.server.Manager;

/**
 * Cấu hình Sách Tuyệt Kỹ — số dòng chưa giám định và kho chỉ số bốc ra.
 *
 * <h2>Sách chạy thế nào</h2>
 *
 * <p>Sách mới ra lò từ công thức {@code DOI_SACH_TUYET_KY} ở Bà Hạt Mít. Mỗi
 * dòng chỉ số chưa biết là một {@link ItemOption} id <b>217</b> ("- Chưa giám
 * định"). Người chơi ghép sách với bùa giám định (vật phẩm 1284) thì từng dòng
 * 217 được thay bằng một chỉ số thật bốc ngẫu nhiên.</p>
 *
 * <h2>Vì sao đưa ra bảng</h2>
 *
 * <p>Bản cũ viết cứng trong {@code GiamDinhSach}: kho chỉ số là một mảng
 * {@code int[]} và trị số bốc bằng {@code nextInt(1, 10 / nextInt(1, 3))} —
 * công thức lồng nhau, không ai đọc ra được khoảng thật sự là bao nhiêu, mà
 * muốn thêm một chỉ số là phải sửa mã rồi biên dịch lại. Nay kho chỉ số và
 * khoảng trị số nằm trong {@code sach_tuyet_ky_chi_so}, số dòng chưa giám định
 * nằm trong {@code sach_tuyet_ky}, sửa trên panel là ăn ngay.</p>
 *
 * <h2>Dùng chung mọi hành tinh</h2>
 *
 * <p>Sáu vật phẩm sách (1044/1211/1212 là Sách 1, 1278/1279/1280 là Sách 2)
 * vốn mỗi hành tinh một bản, khoá bằng cột {@code gender} của
 * {@code item_template} — client thấy {@code gender} là 0/1/2 thì in "Dành cho
 * Trái Đất / Namếc / Sayda", còn máy chủ chặn ở
 * {@code InventoryService} khi mặc. Đặt {@code gender = 3} là bỏ khoá cả hai
 * phía, không phải sửa client. Xem {@link #apDungDungChung()}.</p>
 */
public final class SachTuyetKyDAO {

    private SachTuyetKyDAO() {
    }

    /** Id vật phẩm của sáu cuốn sách — ba hành tinh × hai bậc. */
    public static final int[] ID_SACH = {1044, 1211, 1212, 1278, 1279, 1280};

    /** Chỉ số "- Chưa giám định". */
    public static final int OPTION_CHUA_GIAM_DINH = 217;

    /** Một dòng trong kho chỉ số. */
    public static final class ChiSo {

        public int id;
        public int optionId;
        public int min = 1;
        public int max = 10;
        public int thuTu;
        public boolean bat = true;
        public String ghiChu = "";

        public ChiSo() {
        }

        public ChiSo(int optionId, int min, int max) {
            this.optionId = optionId;
            this.min = min;
            this.max = max;
        }

        /** Tên chỉ số đọc từ {@code item_option_template}, kèm id để dễ dò. */
        public String ten() {
            return tenChiSo(optionId);
        }

        public String moTaKhoang() {
            return min == max ? String.valueOf(min) : (min + " – " + max);
        }
    }

    /** Cấu hình chung, chỉ một dòng. */
    public static final class CaiDat {

        public int soDongMin = 1;
        public int soDongMax = 4;
        public boolean dungChungHanhTinh = true;
    }

    /**
     * Kho chỉ số mặc định — đúng danh sách viết cứng trong {@code GiamDinhSach}
     * trước đây, giữ nguyên để sách đang có trong game không đổi tính chất.
     */
    private static List<ChiSo> khoGoc() {
        List<ChiSo> ds = new ArrayList<>();
        int[] cu = {77, 103, 50, 108, 94, 14, 80, 81, 175, 5, 214, 216};
        for (int i = 0; i < cu.length; i++) {
            ChiSo cs = new ChiSo(cu[i], 1, 10);
            cs.thuTu = i;
            ds.add(cs);
        }
        return ds;
    }

    /**
     * Tên chỉ số theo id, đọc từ {@code item_option_template}.
     *
     * <p>Đọc thẳng CSDL chứ không dùng {@code Manager.ITEM_OPTION_TEMPLATES}:
     * panel dựng tab ngay lúc khởi động, sớm hơn lúc máy chủ nạp xong bảng chỉ
     * số, nên bảng trong bộ nhớ còn rỗng và cả cột tên ra "Chỉ số 77". Nhớ đệm
     * lại vì hàm này gọi mỗi ô của bảng.</p>
     */
    public static String tenChiSo(int optionId) {
        String ten = khoTenChiSo().get(optionId);
        return ten == null || ten.trim().isEmpty()
                ? ("Chỉ số " + optionId) : ten.trim();
    }

    /** Toàn bộ chỉ số của máy chủ, id → tên, sắp theo id. */
    public static synchronized java.util.Map<Integer, String> khoTenChiSo() {
        if (!TEN_CHI_SO.isEmpty()) {
            return TEN_CHI_SO;
        }
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, NAME FROM item_option_template ORDER BY id");
            while (rs.next()) {
                TEN_CHI_SO.put(rs.getInt("id"), rs.getString("NAME"));
            }
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi đọc tên chỉ số");
        } finally {
            dong(rs);
        }
        if (TEN_CHI_SO.isEmpty()) {
            // CSDL chưa sẵn sàng: lấy tạm bảng trong bộ nhớ, có gì dùng nấy.
            try {
                for (ItemOptionTemplate t : Manager.ITEM_OPTION_TEMPLATES) {
                    TEN_CHI_SO.put((int) t.id, t.name);
                }
            } catch (Exception boQua) {
            }
        }
        return TEN_CHI_SO;
    }

    private static final java.util.Map<Integer, String> TEN_CHI_SO
            = new java.util.LinkedHashMap<>();

    // ------------------------------------------------------------------
    // Cài đặt chung
    // ------------------------------------------------------------------
    public static CaiDat caiDat() {
        LuocDoPanel.damBao();
        CaiDat cd = new CaiDat();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT so_dong_min, so_dong_max, dung_chung_hanh_tinh"
                    + " FROM sach_tuyet_ky ORDER BY id LIMIT 1");
            if (rs.next()) {
                cd.soDongMin = rs.getInt("so_dong_min");
                cd.soDongMax = rs.getInt("so_dong_max");
                cd.dungChungHanhTinh = rs.getInt("dung_chung_hanh_tinh") != 0;
                return cd;
            }
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi đọc cài đặt sách tuyệt kỹ");
            return cd;
        } finally {
            dong(rs);
        }
        // Bảng rỗng: ghi dòng mặc định để panel có cái mà sửa.
        luuCaiDat(cd);
        return cd;
    }

    /** @return {@code null} nếu lưu xong, ngược lại là lý do */
    public static String luuCaiDat(CaiDat cd) {
        if (cd.soDongMin < 0) {
            return "Số dòng ít nhất không được âm.";
        }
        if (cd.soDongMax < cd.soDongMin) {
            return "Số dòng nhiều nhất phải từ số dòng ít nhất trở lên.";
        }
        if (cd.soDongMax > 20) {
            return "Nhiều nhất 20 dòng — hơn nữa thì bảng chỉ số tràn khung "
                    + "thông tin vật phẩm bên client.";
        }
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate(
                    "UPDATE sach_tuyet_ky SET so_dong_min = ?, so_dong_max = ?,"
                    + " dung_chung_hanh_tinh = ?",
                    cd.soDongMin, cd.soDongMax, cd.dungChungHanhTinh ? 1 : 0);
            if (n == 0) {
                ConnectDB.executeUpdate(
                        "INSERT INTO sach_tuyet_ky (so_dong_min, so_dong_max,"
                        + " dung_chung_hanh_tinh) VALUES (?, ?, ?)",
                        cd.soDongMin, cd.soDongMax,
                        cd.dungChungHanhTinh ? 1 : 0);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi lưu cài đặt sách tuyệt kỹ");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    // ------------------------------------------------------------------
    // Kho chỉ số
    // ------------------------------------------------------------------
    public static List<ChiSo> danhSachChiSo() {
        LuocDoPanel.damBao();
        List<ChiSo> ds = doc();
        if (ds.isEmpty()) {
            for (ChiSo cs : khoGoc()) {
                them(cs);
            }
            ds = doc();
        }
        return ds;
    }

    private static List<ChiSo> doc() {
        List<ChiSo> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, option_id, min_gt, max_gt, thu_tu, bat, ghi_chu"
                    + " FROM sach_tuyet_ky_chi_so ORDER BY thu_tu, id");
            while (rs.next()) {
                ChiSo cs = new ChiSo();
                cs.id = rs.getInt("id");
                cs.optionId = rs.getInt("option_id");
                cs.min = rs.getInt("min_gt");
                cs.max = rs.getInt("max_gt");
                cs.thuTu = rs.getInt("thu_tu");
                cs.bat = rs.getInt("bat") != 0;
                cs.ghiChu = rs.getString("ghi_chu") == null
                        ? "" : rs.getString("ghi_chu");
                ds.add(cs);
            }
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi đọc kho chỉ số sách tuyệt kỹ");
        } finally {
            dong(rs);
        }
        return ds;
    }

    private static String kiemTra(ChiSo cs) {
        if (cs.optionId < 0) {
            return "Chưa chọn chỉ số.";
        }
        if (cs.optionId == OPTION_CHUA_GIAM_DINH) {
            return "Không đưa chính \"Chưa giám định\" (217) vào kho — giám "
                    + "định xong lại ra chưa giám định thì sách không bao giờ "
                    + "xong.";
        }
        if (cs.min > cs.max) {
            return "Trị nhỏ nhất phải bé hơn hoặc bằng trị lớn nhất.";
        }
        return null;
    }

    public static String them(ChiSo cs) {
        String loi = kiemTra(cs);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "INSERT INTO sach_tuyet_ky_chi_so (option_id, min_gt,"
                    + " max_gt, thu_tu, bat, ghi_chu) VALUES (?, ?, ?, ?, ?, ?)",
                    cs.optionId, cs.min, cs.max, cs.thuTu, cs.bat ? 1 : 0,
                    cs.ghiChu == null ? "" : cs.ghiChu);
            return null;
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex, "Lỗi thêm chỉ số sách");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String sua(ChiSo cs) {
        String loi = kiemTra(cs);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "UPDATE sach_tuyet_ky_chi_so SET option_id = ?, min_gt = ?,"
                    + " max_gt = ?, thu_tu = ?, bat = ?, ghi_chu = ?"
                    + " WHERE id = ?",
                    cs.optionId, cs.min, cs.max, cs.thuTu, cs.bat ? 1 : 0,
                    cs.ghiChu == null ? "" : cs.ghiChu, cs.id);
            return null;
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex, "Lỗi sửa chỉ số sách");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "DELETE FROM sach_tuyet_ky_chi_so WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex, "Lỗi xoá chỉ số sách");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String batTat(int id, boolean bat) {
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "UPDATE sach_tuyet_ky_chi_so SET bat = ? WHERE id = ?",
                    bat ? 1 : 0, id);
            return null;
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi bật/tắt chỉ số sách");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Xoá sạch kho rồi gieo lại danh sách gốc. */
    public static String gieoLaiKho() {
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate("DELETE FROM sach_tuyet_ky_chi_so");
            for (ChiSo cs : khoGoc()) {
                them(cs);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi gieo lại kho chỉ số sách");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    // ------------------------------------------------------------------
    // Dùng lúc chạy game
    // ------------------------------------------------------------------
    /** Số dòng "Chưa giám định" cho một cuốn sách vừa ghép ra. */
    public static int bocSoDong() {
        CaiDat cd = caiDat();
        int min = Math.max(0, cd.soDongMin);
        int max = Math.max(min, cd.soDongMax);
        return Util.nextInt(min, max);
    }

    /**
     * Bốc một chỉ số thật để thay cho một dòng chưa giám định.
     *
     * @return {@code null} nếu kho rỗng hoặc tắt hết — chỗ gọi phải giữ nguyên
     *         dòng 217 chứ đừng làm hỏng cuốn sách.
     */
    public static ItemOption bocMotChiSo() {
        List<ChiSo> bat = new ArrayList<>();
        for (ChiSo cs : danhSachChiSo()) {
            if (cs.bat) {
                bat.add(cs);
            }
        }
        if (bat.isEmpty()) {
            return null;
        }
        ChiSo cs = bat.get(Util.nextInt(bat.size()));
        return new ItemOption(cs.optionId, Util.nextInt(cs.min, cs.max));
    }

    /**
     * Đặt lại số dòng chưa giám định cho một cuốn sách vừa tạo.
     *
     * <p>Bỏ hết dòng 217 mà công thức đã gắn rồi thêm lại đúng số bốc được, nên
     * số dòng do tab panel quyết định chứ không phải cột {@code chi_so_ket_qua}
     * của công thức. Các chỉ số khác của công thức (độ bền, số lần tẩy, yêu cầu
     * sức mạnh...) giữ nguyên.</p>
     */
    public static void datDongChuaGiamDinh(Item sach) {
        if (sach == null || sach.itemOptions == null) {
            return;
        }
        sach.itemOptions.removeIf(io -> io.optionTemplate != null
                && io.optionTemplate.id == OPTION_CHUA_GIAM_DINH);
        int soDong = bocSoDong();
        for (int i = 0; i < soDong; i++) {
            sach.itemOptions.add(new ItemOption(OPTION_CHUA_GIAM_DINH, 0));
        }
    }

    /**
     * Bỏ khoá hành tinh của sáu cuốn sách nếu cài đặt đang bật.
     *
     * <p>Ghi thẳng vào {@code item_template} <b>và</b> sửa luôn bản đã nạp trong
     * bộ nhớ: máy chủ đọc bảng một lần lúc khởi động, chỉ ghi CSDL thì phải khởi
     * động lại mới thấy tác dụng.</p>
     *
     * <p><b>Còn phải tăng {@code vs_item}.</b> Client không tải bảng vật phẩm mỗi
     * lần vào game — nó cất một bản rồi chỉ tải lại khi phiên bản máy chủ quảng
     * bá khác phiên bản nó đang giữ. Sửa cột {@code gender} mà không tăng phiên
     * bản thì client vẫn đọc bản cũ trong máy và tiếp tục in "Dành cho Sayda",
     * dù máy chủ đã cho mặc bình thường — đúng cái cảnh dùng được nhưng vẫn thấy
     * dòng chữ đó.</p>
     *
     * <p>Chỉ tăng khi thật sự có gì đổi. Tăng mỗi lần khởi động là bắt mọi client
     * tải lại bảng vật phẩm không vì lý do gì.</p>
     *
     * <p>Tắt lại thì trả về khoá cũ theo bậc sách: 1044/1278 Trái Đất,
     * 1211/1279 Namếc, 1212/1280 Xayda.</p>
     */
    public static void apDungDungChung() {
        try {
            CaiDat cd = caiDat();
            boolean coDoi = false;
            for (int id : ID_SACH) {
                int gioiTinh = cd.dungChungHanhTinh ? 3 : gioiTinhGoc(id);
                if (gioiTinhTrongBang(id) == gioiTinh) {
                    continue;
                }
                coDoi = true;
                ConnectDB.executeUpdate(
                        "UPDATE item_template SET gender = ? WHERE id = ?",
                        gioiTinh, id);
                try {
                    nro.entity.template.ItemTemplate t
                            = nro.service.item.ItemService.gI().getTemplate(id);
                    if (t != null) {
                        t.gender = (byte) gioiTinh;
                    }
                } catch (Exception boQua) {
                    // Chưa nạp xong bảng vật phẩm thì thôi, lần khởi động sau
                    // đọc từ CSDL là đã đúng rồi.
                }
            }
            if (coDoi) {
                tangPhienBanVatPham();
            }
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi áp dụng dùng chung hành tinh cho sách tuyệt kỹ");
        }
    }

    private static int gioiTinhTrongBang(int id) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT gender FROM item_template WHERE id = ?", id);
            if (rs.next()) {
                return rs.getInt("gender");
            }
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi đọc gender của vật phẩm " + id);
        } finally {
            dong(rs);
        }
        return -1;
    }

    /**
     * Tăng {@code vs_item} lên một, để client tải lại bảng vật phẩm.
     *
     * <p>Đây là {@code byte} gửi xuống client nên chạy vòng 1..127 rồi quay lại
     * 1 — chỉ cần <b>khác</b> con số client đang giữ là nó tải lại, không cần
     * lớn hơn.</p>
     */
    private static void tangPhienBanVatPham() {
        try {
            long cu = nro.repository.dao.ConfigDAO.num(
                    nro.repository.dao.ConfigDAO.VS_ITEM);
            long moi = cu >= 127 || cu < 1 ? 1 : cu + 1;
            nro.repository.dao.ConfigDAO.set(
                    nro.repository.dao.ConfigDAO.VS_ITEM, String.valueOf(moi));
            Logger.warning("Sách tuyệt kỹ đổi khoá hành tinh — vs_item "
                    + cu + " → " + moi + ", client sẽ tải lại bảng vật phẩm.\n");
        } catch (Exception ex) {
            Logger.logException(SachTuyetKyDAO.class, ex,
                    "Lỗi tăng vs_item");
        }
    }

    private static int gioiTinhGoc(int id) {
        switch (id) {
            case 1044:
            case 1278:
                return 0;
            case 1211:
            case 1279:
                return 1;
            default:
                return 2;
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception boQua) {
            }
        }
    }
}
