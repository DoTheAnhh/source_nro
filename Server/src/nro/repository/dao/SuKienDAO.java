package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.schema.LuocDoPanel;

/**
 * Sự kiện chạy theo thời gian — đọc/ghi bốn bảng {@code su_kien*}.
 *
 * <h2>Vì sao có lớp này</h2>
 *
 * <p>Bản gốc quản sự kiện bằng lớp {@code EventManager} với mười một cờ
 * {@code static boolean} gõ cứng, và {@code EventPanel} đi kèm ghi ra
 * {@code active_event.txt} rồi <b>khởi động lại máy chủ sau 10 giây</b>. Cả hai
 * lớp đó đã bị chú thích hết trong {@code src_original}; trong mã đang chạy,
 * {@code Mob.getItemEventMobReward()} còn nguyên tên hàm và lời gọi nhưng thân
 * hàm <b>rỗng</b> — mọi khối TẾT / GIÁNG SINH / HALLOWEEN chỉ còn dòng chú
 * thích. Nghĩa là tới hôm nay máy chủ không rơi vật phẩm sự kiện nào cả.</p>
 *
 * <h2>Ba bảng con, ba việc</h2>
 *
 * <ul>
 *   <li>{@code su_kien_roi} — vật phẩm rơi từ <b>mọi quái</b> khi sự kiện chạy,
 *       <b>mỗi món một tỉ lệ riêng</b></li>
 *   <li>{@code su_kien_doi} — công thức đổi ở Quy Lão: N món này lấy M gói kia</li>
 *   <li>{@code su_kien_hop} — mở gói ra được gì, bốc theo trọng số</li>
 * </ul>
 *
 * <p>Tách {@code su_kien_hop} theo <b>id vật phẩm gói</b> chứ không theo sự
 * kiện: cùng một cái gói dùng lại ở mùa sau thì khỏi khai lại nội dung, và một
 * gói bán ở cửa hàng cũng mở được mà không cần thuộc sự kiện nào.</p>
 *
 * <h2>Không chạy thì không rơi</h2>
 *
 * <p>Mọi thứ ở đây đều đi qua {@link SuKien#dangChay()} — tắt công tắc hoặc hết
 * khung giờ là quái ngừng rơi và Quy Lão ngừng hiện mục đổi, ngay lập tức,
 * không phải khởi động lại.</p>
 */
public final class SuKienDAO {

    private SuKienDAO() {
    }

    // ==================================================================
    //  Mô hình
    // ==================================================================

    /** Một sự kiện. */
    public static final class SuKien {

        public int id;
        public String ten = "";
        /** Công tắc tay. Tắt thì khung giờ có tới đâu cũng không chạy. */
        public boolean bat;
        /** {@code null} = không chặn đầu. */
        public java.sql.Timestamp batDau;
        /** {@code null} = không chặn cuối. */
        public java.sql.Timestamp ketThuc;
        /** Câu loa vàng. Để trống thì không loa gì cả. */
        public String thongBao = "";
        /** Phút giữa hai lần loa. {@code 0} = chỉ loa lúc mở, không nhắc lại. */
        public int phutNhac;
        public String ghiChu = "";

        /** Đang trong khung giờ <b>và</b> công tắc đang bật. */
        public boolean dangChay() {
            if (!bat) {
                return false;
            }
            long now = System.currentTimeMillis();
            if (batDau != null && now < batDau.getTime()) {
                return false;
            }
            return ketThuc == null || now <= ketThuc.getTime();
        }

        public String moTaTrangThai() {
            if (!bat) {
                return "tắt";
            }
            long now = System.currentTimeMillis();
            if (batDau != null && now < batDau.getTime()) {
                return "chờ tới giờ";
            }
            if (ketThuc != null && now > ketThuc.getTime()) {
                return "đã hết hạn";
            }
            return "ĐANG CHẠY";
        }

        public String moTaKhungGio() {
            if (batDau == null && ketThuc == null) {
                return "không giới hạn";
            }
            return (batDau == null ? "…" : gio(batDau))
                    + "  →  " + (ketThuc == null ? "…" : gio(ketThuc));
        }
    }

    /** Một món rơi từ quái trong lúc sự kiện chạy. */
    public static final class VatPhamRoi {

        public int id;
        public int suKienId;
        public int itemId;
        public int soLuongMin = 1;
        public int soLuongMax = 1;
        /** Tỉ lệ rơi mỗi lần giết một con quái, tính <b>phần trăm</b>. */
        public double tiLe = 1;
        public boolean bat = true;

        /** Rơi ở mọi bản đồ. Bỏ trống {@link #dsMap} cũng ra kết quả này. */
        public static final int MAP_TAT_CA = 0;
        /** Chỉ rơi ở những bản đồ có trong {@link #dsMap}. */
        public static final int MAP_CHI = 1;
        /** Rơi khắp nơi <b>trừ</b> những bản đồ có trong {@link #dsMap}. */
        public static final int MAP_TRU = 2;

        /**
         * Cách hiểu {@link #dsMap}: một trong ba hằng {@code MAP_*} ở trên.
         *
         * <p>Mặc định {@link #MAP_TAT_CA} nên dòng cũ giữ nguyên hành vi — rơi
         * ở mọi bản đồ như trước khi có cột này.</p>
         */
        public int kieuMap = MAP_TAT_CA;

        /**
         * Danh sách bản đồ, các mục ngăn bằng dấu phẩy.
         *
         * <p>Mỗi mục là một trong ba dạng:</p>
         * <ul>
         *   <li>{@code 105} — đúng một bản đồ theo id</li>
         *   <li>{@code ht:2} — cả một hành tinh (0 Trái Đất, 1 Namếc, 2 Xayda)</li>
         *   <li>{@code tk:băng} — mọi bản đồ có tên chứa chữ đó</li>
         * </ul>
         *
         * <p>Hai dạng sau là <b>nhóm sống</b>, không phải danh sách id chép sẵn
         * lúc chọn: khai {@code tk:băng} thì bản đồ băng nào thêm về sau cũng tự
         * nằm trong nhóm. Nếu bung thành id ngay lúc chọn thì mỗi lần thêm bản
         * đồ lại phải nhớ quay vào sửa từng món rơi.</p>
         */
        public String dsMap = "";

        public String moTaSoLuong() {
            return soLuongMin == soLuongMax ? String.valueOf(soLuongMin)
                    : soLuongMin + " – " + soLuongMax;
        }

        /**
         * Món này có rơi ở bản đồ đang đứng không.
         *
         * <p>Khai kiểu lọc mà bỏ trống danh sách thì coi như không lọc. Hiểu
         * theo nghĩa đen sẽ thành hai thái cực vô lý: "chỉ rơi ở" mà không có
         * bản đồ nào là món đó không bao giờ rơi, và người dùng sẽ ngồi soi tỉ
         * lệ chứ không nghĩ tới cái danh sách rỗng.</p>
         *
         * <p>Nhận thẳng tên và hành tinh của bản đồ thay vì tự đi tra: lớp này
         * nằm ở tầng CSDL, thò tay vào {@code Manager} là buộc mọi nơi dùng nó
         * — kể cả lúc kiểm thử — phải nạp sẵn cả máy chủ.</p>
         */
        public boolean roiOMap(int mapId, int hanhTinh, String tenMap) {
            if (kieuMap == MAP_TAT_CA || dsMap == null || dsMap.trim().isEmpty()) {
                return true;
            }
            boolean khop = false;
            String ten = tenMap == null ? "" : tenMap.toLowerCase();
            for (String phan : dsMap.split(",")) {
                String p = phan.trim();
                if (p.isEmpty()) {
                    continue;
                }
                if (p.startsWith("ht:")) {
                    khop = bang(p.substring(3), hanhTinh);
                } else if (p.startsWith("tk:")) {
                    String tu = p.substring(3).trim().toLowerCase();
                    khop = !tu.isEmpty() && ten.contains(tu);
                } else {
                    khop = bang(p, mapId);
                }
                if (khop) {
                    break;
                }
            }
            return kieuMap == MAP_CHI ? khop : !khop;
        }

        private static boolean bang(String so, int gt) {
            try {
                return Integer.parseInt(so.trim()) == gt;
            } catch (NumberFormatException boQua) {
                return false;
            }
        }

        /** Câu mô tả luật lọc, để hiện trong bảng. */
        public String moTaMap() {
            if (kieuMap == MAP_TAT_CA || dsMap == null || dsMap.trim().isEmpty()) {
                return "mọi map";
            }
            int soMap = 0;
            int soNhom = 0;
            for (String phan : dsMap.split(",")) {
                String p = phan.trim();
                if (p.isEmpty()) {
                    continue;
                }
                if (p.startsWith("ht:") || p.startsWith("tk:")) {
                    soNhom++;
                } else {
                    soMap++;
                }
            }
            StringBuilder sb = new StringBuilder(kieuMap == MAP_CHI ? "chỉ " : "trừ ");
            if (soMap > 0) {
                sb.append(soMap).append(" map");
            }
            if (soNhom > 0) {
                sb.append(soMap > 0 ? " + " : "").append(soNhom).append(" nhóm");
            }
            return sb.toString();
        }
    }

    /** Một công thức đổi ở Quy Lão. */
    public static final class CongThucDoi {

        public int id;
        public int suKienId;
        public int thuTu;
        /** Chữ hiện trên nút menu. Để trống thì tự sinh từ số lượng. */
        public String ten = "";
        /**
         * Nguyên liệu phải nộp, mỗi phần tử là một cặp {@code {id, số lượng}}.
         *
         * <p>Người chơi phải có <b>đủ tất cả</b> mới đổi được — thiếu một món
         * là hỏng cả lượt, không đổi từng phần.</p>
         */
        public final List<int[]> nguyenLieu = new ArrayList<>();
        public int itemNhan;
        public int soLuongNhan = 1;
        public boolean bat = true;

        /** Nguyên liệu dạng {@code id:sl,id:sl} để ghi xuống CSDL. */
        public String chuoiNguyenLieu() {
            StringBuilder sb = new StringBuilder();
            for (int[] cap : nguyenLieu) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(cap[0]).append(':').append(cap[1]);
            }
            return sb.toString();
        }

        /**
         * Món đầu tiên trong danh sách, hoặc {@code -1} nếu chưa khai gì.
         *
         * <p>Chỉ dùng để đổ vào hai cột cũ {@code item_can} và
         * {@code so_luong_can} — hai cột đó {@code NOT NULL} nên vẫn phải có
         * giá trị, nhưng mã đang chạy không còn đọc chúng nữa.</p>
         */
        public int[] capDau() {
            return nguyenLieu.isEmpty() ? new int[]{-1, 1} : nguyenLieu.get(0);
        }
    }

    /** Một phần thưởng có thể bốc ra khi mở gói. */
    public static final class PhanThuongHop {

        public int id;
        /** Id vật phẩm <b>của cái gói</b>, không phải của sự kiện. */
        public int hopItemId;
        public int itemId;
        public int soLuongMin = 1;
        public int soLuongMax = 1;
        /** Trọng số bốc. Càng lớn càng hay ra. */
        public int trongSo = 1;
        public boolean bat = true;

        public String moTaSoLuong() {
            return soLuongMin == soLuongMax ? String.valueOf(soLuongMin)
                    : soLuongMin + " – " + soLuongMax;
        }
    }

    // ==================================================================
    //  Giờ
    // ==================================================================

    private static final java.text.SimpleDateFormat DINH_DANG =
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    /** Giờ dạng {@code dd/MM/yyyy HH:mm} — dạng người Việt gõ được ngay. */
    public static synchronized String gio(java.sql.Timestamp t) {
        return t == null ? "" : DINH_DANG.format(t);
    }

    /**
     * Đọc giờ người dùng gõ. Chuỗi rỗng trả {@code null} (nghĩa là không chặn);
     * gõ sai ném {@link IllegalArgumentException} để chỗ gọi báo lại cho đúng ô.
     */
    public static synchronized java.sql.Timestamp docGio(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return new java.sql.Timestamp(DINH_DANG.parse(raw.trim()).getTime());
        } catch (java.text.ParseException ex) {
            throw new IllegalArgumentException(
                    "Giờ phải viết dạng dd/MM/yyyy HH:mm, ví dụ 24/12/2026 19:00");
        }
    }

    // ==================================================================
    //  su_kien
    // ==================================================================


    // ==================================================================
    //  su_kien_ten  — danh sach ten goi y, sua duoc tu panel
    // ==================================================================

    /** Ten mac dinh, chi nap MOT LAN khi bang con rong. */
    private static final String[] TEN_MAC_DINH = {
        "Tết Nguyên Đán", "Trung Thu", "Halloween", "Giáng Sinh",
        "Quốc tế Phụ nữ 8/3", "Phụ nữ Việt Nam 20/10", "Nhà giáo 20/11",
        "Giỗ Tổ Hùng Vương", "Vu Lan", "Valentine", "Black Friday",
        "Sự kiện cuối tuần", "Sự kiện hè"};

    /**
     * Danh sach ten goi y. Lan dau chay se tu do bo ten mac dinh vao bang,
     * de nguoi dung khong thay o chon trong tron sau khi nang cap.
     */
    public static synchronized List<String> danhSachTen() {
        LuocDoPanel.damBao();
        List<String> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT ten FROM su_kien_ten ORDER BY ten");
            while (rs.next()) {
                ds.add(rs.getString("ten"));
            }
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Không đọc được su_kien_ten");
        } finally {
            dong(rs);
        }
        if (ds.isEmpty()) {
            for (String t : TEN_MAC_DINH) {
                themTen(t);
                ds.add(t);
            }
        }
        return ds;
    }

    /** Them mot ten. Trung ten thi bo qua (cot ten co UNIQUE). */
    public static synchronized String themTen(String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            return "Tên sự kiện không được để trống.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT IGNORE INTO su_kien_ten (ten) VALUES (?)", ten.trim());
            return null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi thêm tên sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Doi ten cu thanh ten moi. */
    public static synchronized String suaTen(String cu, String moi) {
        if (moi == null || moi.trim().isEmpty()) {
            return "Tên sự kiện không được để trống.";
        }
        try {
            ConnectDB.executeUpdate(
                    "UPDATE su_kien_ten SET ten = ? WHERE ten = ?", moi.trim(), cu);
            return null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi sửa tên sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Xoa mot ten khoi danh sach goi y. Khong dung toi su kien da tao. */
    public static synchronized String xoaTen(String ten) {
        try {
            ConnectDB.executeUpdate("DELETE FROM su_kien_ten WHERE ten = ?", ten);
            return null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi xoá tên sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Toàn bộ sự kiện, cái đang bật xếp lên trước. */
    public static List<SuKien> danhSach() {
        LuocDoPanel.damBao();
        List<SuKien> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, ten, bat, bat_dau, ket_thuc, thong_bao,"
                    + " phut_nhac, ghi_chu FROM su_kien ORDER BY bat DESC, id");
            while (rs.next()) {
                SuKien s = new SuKien();
                s.id = rs.getInt("id");
                s.ten = chuoi(rs.getString("ten"));
                s.bat = rs.getBoolean("bat");
                s.batDau = rs.getTimestamp("bat_dau");
                s.ketThuc = rs.getTimestamp("ket_thuc");
                s.thongBao = chuoi(rs.getString("thong_bao"));
                s.phutNhac = rs.getInt("phut_nhac");
                s.ghiChu = chuoi(rs.getString("ghi_chu"));
                ds.add(s);
            }
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi đọc bảng sự kiện");
        } finally {
            dong(rs);
        }
        return ds;
    }

    /** @return {@code null} nếu thêm xong, ngược lại là lý do */
    public static String them(SuKien s) {
        String loi = kiemTra(s);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "INSERT INTO su_kien (ten, bat, bat_dau, ket_thuc,"
                    + " thong_bao, phut_nhac, ghi_chu) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    s.ten, s.bat ? 1 : 0, s.batDau, s.ketThuc,
                    s.thongBao, s.phutNhac, s.ghiChu);
            return null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi thêm sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** @return {@code null} nếu lưu xong, ngược lại là lý do */
    public static String sua(SuKien s) {
        String loi = kiemTra(s);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate(
                    "UPDATE su_kien SET ten = ?, bat = ?, bat_dau = ?,"
                    + " ket_thuc = ?, thong_bao = ?, phut_nhac = ?,"
                    + " ghi_chu = ? WHERE id = ?",
                    s.ten, s.bat ? 1 : 0, s.batDau, s.ketThuc,
                    s.thongBao, s.phutNhac, s.ghiChu, s.id);
            return n == 0 ? "Không có sự kiện id " + s.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi sửa sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Bật/tắt nhanh, không đụng vào các cột khác. */
    public static String datBat(int id, boolean bat) {
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate(
                    "UPDATE su_kien SET bat = ? WHERE id = ?", bat ? 1 : 0, id);
            return n == 0 ? "Không có sự kiện id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi bật/tắt sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Xoá sự kiện <b>và</b> mọi dòng con của nó.
     *
     * <p>Xoá tay từng bảng chứ không nhờ khoá ngoại: các bảng của panel đều
     * dựng bằng {@code CREATE TABLE IF NOT EXISTS} rời nhau, máy chủ nào tạo
     * bảng trước bằng tay thì không có ràng buộc nào cả. Để lại dòng con mồ côi
     * thì lần sau thêm sự kiện trùng id là nó ăn luôn phần rơi của mùa cũ.</p>
     */
    public static String xoa(int id) {
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate("DELETE FROM su_kien_roi WHERE su_kien_id = ?", id);
            ConnectDB.executeUpdate("DELETE FROM su_kien_doi WHERE su_kien_id = ?", id);
            int n = ConnectDB.executeUpdate("DELETE FROM su_kien WHERE id = ?", id);
            return n == 0 ? "Không có sự kiện id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi xoá sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static String kiemTra(SuKien s) {
        if (s == null) {
            return "Thiếu dữ liệu.";
        }
        if (s.ten == null || s.ten.trim().isEmpty()) {
            return "Sự kiện phải có tên.";
        }
        if (s.ten.length() > 100) {
            return "Tên dài quá 100 ký tự.";
        }
        if (s.batDau != null && s.ketThuc != null
                && s.ketThuc.getTime() <= s.batDau.getTime()) {
            return "Giờ kết thúc phải sau giờ bắt đầu.";
        }
        if (s.phutNhac < 0) {
            return "Số phút nhắc lại không được âm.";
        }
        if (s.thongBao != null && s.thongBao.length() > 255) {
            return "Câu thông báo dài quá 255 ký tự.";
        }
        if (s.thongBao == null) {
            s.thongBao = "";
        }
        if (s.ghiChu == null) {
            s.ghiChu = "";
        }
        return null;
    }

    // ==================================================================
    //  su_kien_roi
    // ==================================================================

    /**
     * Thêm hai cột phạm vi bản đồ nếu bảng chưa có.
     *
     * <p>{@code kieu_map} khai {@code INT} chứ <b>không</b> phải
     * {@code tinyint(1)}. Trình điều khiển MySQL đọc {@code tinyint(1)} ra
     * {@link Boolean}, mà cột này mang ba giá trị 0/1/2 nên đọc bằng
     * {@code getInt} sẽ ném {@code ClassCastException} — và hỏng cả hàm đọc
     * bảng, tức là không món nào rơi nữa. Cột {@code bat} bên cạnh vẫn
     * {@code tinyint(1)} vì nó đọc bằng {@code getBoolean}, đúng kiểu.</p>
     */
    private static volatile boolean daVaCotRoi;

    public static void damBaoCotRoi() {
        if (daVaCotRoi) {
            return;
        }
        daVaCotRoi = true;
        try {
            ConnectDB.executeUpdate("ALTER TABLE su_kien_roi ADD COLUMN"
                    + " IF NOT EXISTS kieu_map INT(11) NOT NULL DEFAULT 0");
            ConnectDB.executeUpdate("ALTER TABLE su_kien_roi ADD COLUMN"
                    + " IF NOT EXISTS ds_map VARCHAR(500) NOT NULL DEFAULT ''");
            // Ban cai dat truoc lo tao cot bang tinyint(1); sua ve INT cho
            // chac, chay lai khong sao vi cot da dung kieu thi khong doi gi.
            ConnectDB.executeUpdate("ALTER TABLE su_kien_roi"
                    + " MODIFY COLUMN kieu_map INT(11) NOT NULL DEFAULT 0");
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex,
                    "Không thêm được cột phạm vi bản đồ — món rơi sẽ rơi ở mọi map");
        }
    }

    /** Vật phẩm rơi của một sự kiện. Truyền {@code -1} để lấy của mọi sự kiện. */
    public static List<VatPhamRoi> danhSachRoi(int suKienId) {
        LuocDoPanel.damBao();
        damBaoCotRoi();
        List<VatPhamRoi> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            String sql = "SELECT id, su_kien_id, item_id, so_luong_min,"
                    + " so_luong_max, ti_le, bat, kieu_map, ds_map"
                    + " FROM su_kien_roi";
            rs = suKienId < 0
                    ? ConnectDB.executeQuery(sql + " ORDER BY su_kien_id, id")
                    : ConnectDB.executeQuery(sql + " WHERE su_kien_id = ? ORDER BY id",
                            suKienId);
            while (rs.next()) {
                VatPhamRoi v = new VatPhamRoi();
                v.id = rs.getInt("id");
                v.suKienId = rs.getInt("su_kien_id");
                v.itemId = rs.getInt("item_id");
                v.soLuongMin = rs.getInt("so_luong_min");
                v.soLuongMax = rs.getInt("so_luong_max");
                v.tiLe = rs.getDouble("ti_le");
                v.bat = rs.getBoolean("bat");
                v.kieuMap = rs.getInt("kieu_map");
                v.dsMap = chuoi(rs.getString("ds_map"));
                ds.add(v);
            }
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi đọc vật phẩm rơi sự kiện");
        } finally {
            dong(rs);
        }
        return ds;
    }

    public static String luuRoi(VatPhamRoi v, boolean them) {
        String loi = kiemTraRoi(v);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            damBaoCotRoi();
            if (them) {
                ConnectDB.executeUpdate(
                        "INSERT INTO su_kien_roi (su_kien_id, item_id,"
                        + " so_luong_min, so_luong_max, ti_le, bat,"
                        + " kieu_map, ds_map)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        v.suKienId, v.itemId, v.soLuongMin, v.soLuongMax,
                        v.tiLe, v.bat ? 1 : 0, v.kieuMap, v.dsMap);
                return null;
            }
            int n = ConnectDB.executeUpdate(
                    "UPDATE su_kien_roi SET item_id = ?, so_luong_min = ?,"
                    + " so_luong_max = ?, ti_le = ?, bat = ?,"
                    + " kieu_map = ?, ds_map = ? WHERE id = ?",
                    v.itemId, v.soLuongMin, v.soLuongMax, v.tiLe,
                    v.bat ? 1 : 0, v.kieuMap, v.dsMap, v.id);
            return n == 0 ? "Không có dòng id " + v.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi lưu vật phẩm rơi sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaRoi(int id) {
        return xoaDong("su_kien_roi", id);
    }

    private static String kiemTraRoi(VatPhamRoi v) {
        if (v == null) {
            return "Thiếu dữ liệu.";
        }
        if (v.itemId < 0) {
            return "Chưa chọn vật phẩm.";
        }
        if (v.soLuongMin < 1 || v.soLuongMax < v.soLuongMin) {
            return "Số lượng phải từ 1 trở lên, và tối đa không nhỏ hơn tối thiểu.";
        }
        // Ti le 0 la "khong bao gio roi" — de duoc, do la cach tat mot mon ma
        // van giu lai dong de mua sau bat lai. Nhung am thi la go nham.
        if (v.tiLe < 0 || v.tiLe > 100) {
            return "Tỉ lệ rơi phải từ 0 đến 100 phần trăm.";
        }
        return null;
    }

    // ==================================================================
    //  su_kien_doi
    // ==================================================================

    /**
     * Thêm cột {@code nguyen_lieu} nếu bảng chưa có.
     *
     * <p>Trước đây một mục đổi chỉ nộp được đúng một món, giữ ở hai cột
     * {@code item_can} và {@code so_luong_can}. Cột mới chứa cả danh sách dưới
     * dạng {@code id:sl,id:sl}; dòng cũ chưa có gì trong cột này thì
     * {@link #danhSachDoi(int)} tự dựng lại từ hai cột kia, nên không cần
     * chuyển dữ liệu bằng tay.</p>
     */
    private static volatile boolean daVaCotDoi;

    public static void damBaoCotDoi() {
        if (daVaCotDoi) {
            return;
        }
        daVaCotDoi = true;
        try {
            ConnectDB.executeUpdate("ALTER TABLE su_kien_doi ADD COLUMN"
                    + " IF NOT EXISTS nguyen_lieu VARCHAR(255) NOT NULL DEFAULT ''");
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex,
                    "Không thêm được cột nguyen_lieu — mục đổi chỉ nhận một nguyên liệu");
        }
    }

    /**
     * Tách chuỗi {@code id:sl,id:sl} thành danh sách cặp.
     *
     * <p>Phần nào gõ sai thì bỏ qua phần đó chứ không vứt cả dòng — một ký tự
     * thừa trong CSDL không đáng để mất luôn công thức.</p>
     */
    private static void docCap(List<int[]> ra, String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return;
        }
        for (String phan : raw.trim().split(",")) {
            String[] p = phan.trim().split(":");
            if (p.length != 2) {
                continue;
            }
            try {
                int id = Integer.parseInt(p[0].trim());
                int sl = Integer.parseInt(p[1].trim());
                if (id >= 0 && sl > 0) {
                    ra.add(new int[]{id, sl});
                }
            } catch (NumberFormatException ignored) {
                // Bo qua phan gay sai, giu lai nhung phan doc duoc.
            }
        }
    }

    /** Công thức đổi của một sự kiện. Truyền {@code -1} để lấy của mọi sự kiện. */
    public static List<CongThucDoi> danhSachDoi(int suKienId) {
        LuocDoPanel.damBao();
        damBaoCotDoi();
        List<CongThucDoi> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            String sql = "SELECT id, su_kien_id, thu_tu, ten, item_can,"
                    + " so_luong_can, nguyen_lieu, item_nhan, so_luong_nhan, bat"
                    + " FROM su_kien_doi";
            rs = suKienId < 0
                    ? ConnectDB.executeQuery(sql + " ORDER BY su_kien_id, thu_tu, id")
                    : ConnectDB.executeQuery(sql + " WHERE su_kien_id = ?"
                            + " ORDER BY thu_tu, id", suKienId);
            while (rs.next()) {
                CongThucDoi c = new CongThucDoi();
                c.id = rs.getInt("id");
                c.suKienId = rs.getInt("su_kien_id");
                c.thuTu = rs.getInt("thu_tu");
                c.ten = chuoi(rs.getString("ten"));
                docCap(c.nguyenLieu, rs.getString("nguyen_lieu"));
                if (c.nguyenLieu.isEmpty()) {
                    // Dong tu thoi mot nguyen lieu, chua kip ghi sang cot moi.
                    int id = rs.getInt("item_can");
                    int sl = Math.max(1, rs.getInt("so_luong_can"));
                    if (id >= 0) {
                        c.nguyenLieu.add(new int[]{id, sl});
                    }
                }
                c.itemNhan = rs.getInt("item_nhan");
                c.soLuongNhan = rs.getInt("so_luong_nhan");
                c.bat = rs.getBoolean("bat");
                ds.add(c);
            }
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi đọc công thức đổi sự kiện");
        } finally {
            dong(rs);
        }
        return ds;
    }

    public static String luuDoi(CongThucDoi c, boolean them) {
        String loi = kiemTraDoi(c);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            damBaoCotDoi();
            String ds = c.chuoiNguyenLieu();
            int[] dau = c.capDau();
            if (them) {
                ConnectDB.executeUpdate(
                        "INSERT INTO su_kien_doi (su_kien_id, thu_tu, ten,"
                        + " item_can, so_luong_can, nguyen_lieu, item_nhan,"
                        + " so_luong_nhan, bat)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        c.suKienId, c.thuTu, c.ten, dau[0], dau[1], ds,
                        c.itemNhan, c.soLuongNhan, c.bat ? 1 : 0);
                return null;
            }
            int n = ConnectDB.executeUpdate(
                    "UPDATE su_kien_doi SET thu_tu = ?, ten = ?, item_can = ?,"
                    + " so_luong_can = ?, nguyen_lieu = ?, item_nhan = ?,"
                    + " so_luong_nhan = ?, bat = ? WHERE id = ?",
                    c.thuTu, c.ten, dau[0], dau[1], ds, c.itemNhan,
                    c.soLuongNhan, c.bat ? 1 : 0, c.id);
            return n == 0 ? "Không có dòng id " + c.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi lưu công thức đổi sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static String kiemTraDoi(CongThucDoi c) {
        if (c == null) {
            return "Thiếu dữ liệu.";
        }
        if (c.nguyenLieu.isEmpty()) {
            return "Chưa chọn vật phẩm cần có.";
        }
        if (c.nguyenLieu.size() > 8) {
            return "Nhiều nhất 8 nguyên liệu cho một mục đổi.";
        }
        if (c.itemNhan < 0) {
            return "Chưa chọn vật phẩm nhận về.";
        }
        if (c.soLuongNhan < 1) {
            return "Số lượng nhận phải từ 1 trở lên.";
        }
        java.util.Set<Integer> daCo = new java.util.HashSet<>();
        for (int[] cap : c.nguyenLieu) {
            if (cap[1] < 1) {
                return "Số lượng cần của " + tenVatPham(cap[0]) + " phải từ 1 trở lên.";
            }
            if (!daCo.add(cap[0])) {
                return "Vật phẩm " + tenVatPham(cap[0])
                        + " bị khai hai lần — gộp vào một dòng.";
            }
            // Doi A lay chinh A la vong lap vo han cho nguoi choi bam, va neu
            // so luong nhan > so luong can thi la may in vat pham.
            if (cap[0] == c.itemNhan) {
                return "Vật phẩm cần và vật phẩm nhận không được trùng nhau.";
            }
        }
        if (c.ten == null) {
            c.ten = "";
        }
        if (c.ten.length() > 100) {
            return "Tên mục dài quá 100 ký tự.";
        }
        return null;
    }


    public static String xoaDoi(int id) {
        return xoaDong("su_kien_doi", id);
    }

    // ==================================================================
    //  su_kien_hop
    // ==================================================================

    /** Nội dung một gói. Truyền {@code -1} để lấy nội dung của mọi gói. */
    public static List<PhanThuongHop> danhSachHop(int hopItemId) {
        LuocDoPanel.damBao();
        List<PhanThuongHop> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            String sql = "SELECT id, hop_item_id, item_id, so_luong_min,"
                    + " so_luong_max, trong_so, bat FROM su_kien_hop";
            rs = hopItemId < 0
                    ? ConnectDB.executeQuery(sql + " ORDER BY hop_item_id, id")
                    : ConnectDB.executeQuery(sql + " WHERE hop_item_id = ? ORDER BY id",
                            hopItemId);
            while (rs.next()) {
                PhanThuongHop p = new PhanThuongHop();
                p.id = rs.getInt("id");
                p.hopItemId = rs.getInt("hop_item_id");
                p.itemId = rs.getInt("item_id");
                p.soLuongMin = rs.getInt("so_luong_min");
                p.soLuongMax = rs.getInt("so_luong_max");
                p.trongSo = rs.getInt("trong_so");
                p.bat = rs.getBoolean("bat");
                ds.add(p);
            }
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi đọc nội dung gói sự kiện");
        } finally {
            dong(rs);
        }
        return ds;
    }

    /** Id của mọi gói đang có nội dung — để panel liệt kê. */
    public static List<Integer> cacHop() {
        LuocDoPanel.damBao();
        List<Integer> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT DISTINCT hop_item_id FROM su_kien_hop ORDER BY hop_item_id");
            while (rs.next()) {
                ds.add(rs.getInt("hop_item_id"));
            }
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi đọc danh sách gói sự kiện");
        } finally {
            dong(rs);
        }
        return ds;
    }

    public static String luuHop(PhanThuongHop p, boolean them) {
        String loi = kiemTraHop(p);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            if (them) {
                ConnectDB.executeUpdate(
                        "INSERT INTO su_kien_hop (hop_item_id, item_id,"
                        + " so_luong_min, so_luong_max, trong_so, bat)"
                        + " VALUES (?, ?, ?, ?, ?, ?)",
                        p.hopItemId, p.itemId, p.soLuongMin, p.soLuongMax,
                        p.trongSo, p.bat ? 1 : 0);
                return null;
            }
            int n = ConnectDB.executeUpdate(
                    "UPDATE su_kien_hop SET hop_item_id = ?, item_id = ?,"
                    + " so_luong_min = ?, so_luong_max = ?, trong_so = ?,"
                    + " bat = ? WHERE id = ?",
                    p.hopItemId, p.itemId, p.soLuongMin, p.soLuongMax,
                    p.trongSo, p.bat ? 1 : 0, p.id);
            return n == 0 ? "Không có dòng id " + p.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi lưu nội dung gói sự kiện");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaHop(int id) {
        return xoaDong("su_kien_hop", id);
    }

    private static String kiemTraHop(PhanThuongHop p) {
        if (p == null) {
            return "Thiếu dữ liệu.";
        }
        if (p.hopItemId < 0) {
            return "Chưa chọn vật phẩm gói.";
        }
        if (p.itemId < 0) {
            return "Chưa chọn vật phẩm phần thưởng.";
        }
        if (p.hopItemId == p.itemId) {
            return "Gói không được mở ra chính nó.";
        }
        if (p.soLuongMin < 1 || p.soLuongMax < p.soLuongMin) {
            return "Số lượng phải từ 1 trở lên, và tối đa không nhỏ hơn tối thiểu.";
        }
        if (p.trongSo < 1) {
            return "Trọng số phải từ 1 trở lên.";
        }
        return null;
    }

    // ==================================================================
    //  Dùng chung
    // ==================================================================

    private static String xoaDong(String bang, int id) {
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate("DELETE FROM " + bang + " WHERE id = ?", id);
            return n == 0 ? "Không có dòng id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(SuKienDAO.class, ex, "Lỗi xoá dòng bảng " + bang);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }

    private static String chuoi(String v) {
        return v == null || "null".equals(v) ? "" : v;
    }

    /** Tên vật phẩm để panel và menu NPC đọc được, không phải nhớ id. */
    public static String tenVatPham(int itemId) {
        try {
            for (nro.entity.template.ItemTemplate t : nro.server.Manager.ITEM_TEMPLATES) {
                if (t != null && t.id == itemId) {
                    return t.name;
                }
            }
        } catch (Exception ignored) {
        }
        return "vật phẩm " + itemId;
    }
}
