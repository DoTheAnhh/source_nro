package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.schema.LuocDoPanel;

/**
 * Điểm đến của capsule — đọc từ bảng {@code capsule_map}.
 *
 * <h2>Vì sao có lớp này</h2>
 *
 * <p>Danh sách nơi capsule bay tới trước đây <b>gõ cứng</b> trong
 * {@code MapService.getMapCapsule()}: thêm hay bớt một địa điểm là phải sửa mã
 * nguồn rồi biên dịch lại và khởi động lại máy chủ. Nay chỉnh thẳng trên panel.</p>
 *
 * <h2>Ba loại điều kiện</h2>
 *
 * <ul>
 *   <li>{@link #LUON} — luôn hiện</li>
 *   <li>{@link #LUYEN_TAP} — chỉ hiện khi {@code levelLuyenTap > nguong}</li>
 *   <li>{@link #SUC_MANH} — chỉ hiện khi {@code power > nguong}</li>
 * </ul>
 *
 * <p>Cờ {@code theoHanhTinh} nghĩa là bản đồ thật bằng {@code mapId + gender} —
 * dùng cho ba map nhà (21 Nhà Gôhan, 22 Nhà Moori, 23 Nhà Broly) và ba map thi
 * đấu (24, 25, 26). Không có cờ này thì mỗi hành tinh phải một dòng riêng, mà
 * ba dòng đó luôn phải sửa cùng nhau — rất dễ quên một cái.</p>
 *
 * <h2>Bảng rỗng thì gieo lại danh sách gốc</h2>
 *
 * <p>Lần đầu chạy (hoặc sau khi ai đó xoá sạch bảng) thì {@link #danhSach()}
 * gieo lại <b>đúng</b> danh sách đã gõ cứng trước đây. Người chơi không thấy
 * khác gì cả, và admin có sẵn một danh sách chạy được để sửa dần — thay vì mở
 * panel ra thấy trống trơn rồi tưởng hỏng.</p>
 */
public final class CapsuleMapDAO {

    private CapsuleMapDAO() {
    }

    public static final String LUON = "luon";
    public static final String LUYEN_TAP = "luyen_tap";
    public static final String SUC_MANH = "suc_manh";

    /** Một dòng trong bảng. */
    public static final class Diem {

        public int id;
        public int thuTu;
        public int mapId;
        public boolean theoHanhTinh;
        public String dieuKien = LUON;
        public long nguong;
        public boolean bat = true;
        public String ghiChu = "";

        public Diem() {
        }

        public Diem(int thuTu, int mapId, boolean theoHanhTinh,
                String dieuKien, long nguong, String ghiChu) {
            this.thuTu = thuTu;
            this.mapId = mapId;
            this.theoHanhTinh = theoHanhTinh;
            this.dieuKien = dieuKien;
            this.nguong = nguong;
            this.ghiChu = ghiChu;
        }

        /** Mô tả điều kiện cho người đọc panel. */
        public String moTaDieuKien() {
            switch (dieuKien == null ? LUON : dieuKien) {
                case LUYEN_TAP:
                    return "Cấp luyện tập > " + nguong;
                case SUC_MANH:
                    return "Sức mạnh > " + nguong;
                default:
                    return "Luôn hiện";
            }
        }
    }

    /**
     * Danh sách gốc — đúng thứ tự và đúng điều kiện của bản gõ cứng cũ trong
     * {@code MapService.getMapCapsule()}.
     */
    private static List<Diem> danhSachGoc() {
        List<Diem> ds = new ArrayList<>();
        int t = 0;
        ds.add(new Diem(t++, 21, true, LUON, 0, "Nhà (theo hành tinh)"));
        ds.add(new Diem(t++, 47, false, LUON, 0, "Rừng Karin"));
        ds.add(new Diem(t++, 45, false, LUYEN_TAP, 1, "Nơi luyện tập"));
        ds.add(new Diem(t++, 0, false, LUON, 0, "Làng Aru"));
        ds.add(new Diem(t++, 7, false, LUON, 0, "Làng Mori"));
        ds.add(new Diem(t++, 14, false, LUON, 0, "Làng Kakarot"));
        ds.add(new Diem(t++, 5, false, LUON, 0, "Đảo Kamê"));
        ds.add(new Diem(t++, 20, false, LUON, 0, "Vách núi đen"));
        ds.add(new Diem(t++, 13, false, LUON, 0, "Đảo Guru"));
        ds.add(new Diem(t++, 24, true, LUON, 0, "Đại hội (theo hành tinh)"));
        ds.add(new Diem(t++, 27, false, LUON, 0, ""));
        ds.add(new Diem(t++, 19, false, LUON, 0, "Thành phố Vegeta"));
        ds.add(new Diem(t++, 79, false, LUON, 0, ""));
        ds.add(new Diem(t++, 84, false, SUC_MANH, 20_000_000L, ""));
        ds.add(new Diem(t++, 154, false, LUYEN_TAP, 5, ""));
        return ds;
    }

    /** Đọc toàn bộ, đã sắp theo thứ tự hiện. Bảng rỗng thì gieo lại danh sách gốc. */
    public static List<Diem> danhSach() {
        LuocDoPanel.damBao();
        List<Diem> ds = doc();
        if (ds.isEmpty()) {
            gieoLai();
            ds = doc();
        }
        return ds;
    }

    private static List<Diem> doc() {
        List<Diem> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, thu_tu, map_id, theo_hanh_tinh, dieu_kien,"
                    + " nguong, bat, ghi_chu FROM capsule_map"
                    + " ORDER BY thu_tu, id");
            while (rs.next()) {
                Diem d = new Diem();
                d.id = rs.getInt("id");
                d.thuTu = rs.getInt("thu_tu");
                d.mapId = rs.getInt("map_id");
                d.theoHanhTinh = rs.getBoolean("theo_hanh_tinh");
                d.dieuKien = rs.getString("dieu_kien");
                d.nguong = rs.getLong("nguong");
                d.bat = rs.getBoolean("bat");
                d.ghiChu = rs.getString("ghi_chu");
                if (d.ghiChu == null) {
                    d.ghiChu = "";
                }
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(CapsuleMapDAO.class, ex,
                    "Lỗi đọc bảng điểm đến capsule");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
        return ds;
    }

    /** Ghi lại danh sách gốc. Chỉ gọi khi bảng đang rỗng. */
    public static void gieoLai() {
        LuocDoPanel.damBao();
        for (Diem d : danhSachGoc()) {
            them(d);
        }
    }

    /** @return {@code null} nếu thêm xong, ngược lại là lý do */
    public static String them(Diem d) {
        String loi = kiemTra(d);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "INSERT INTO capsule_map (thu_tu, map_id, theo_hanh_tinh,"
                    + " dieu_kien, nguong, bat, ghi_chu)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?)",
                    d.thuTu, d.mapId, d.theoHanhTinh ? 1 : 0,
                    d.dieuKien, d.nguong, d.bat ? 1 : 0,
                    d.ghiChu == null ? "" : d.ghiChu);
            return null;
        } catch (Exception ex) {
            Logger.logException(CapsuleMapDAO.class, ex, "Lỗi thêm điểm đến capsule");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** @return {@code null} nếu lưu xong, ngược lại là lý do */
    public static String sua(Diem d) {
        String loi = kiemTra(d);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate(
                    "UPDATE capsule_map SET thu_tu = ?, map_id = ?,"
                    + " theo_hanh_tinh = ?, dieu_kien = ?, nguong = ?,"
                    + " bat = ?, ghi_chu = ? WHERE id = ?",
                    d.thuTu, d.mapId, d.theoHanhTinh ? 1 : 0,
                    d.dieuKien, d.nguong, d.bat ? 1 : 0,
                    d.ghiChu == null ? "" : d.ghiChu, d.id);
            return n == 0 ? "Không có dòng id " + d.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(CapsuleMapDAO.class, ex, "Lỗi sửa điểm đến capsule");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** @return {@code null} nếu xoá xong, ngược lại là lý do */
    public static String xoa(int id) {
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate("DELETE FROM capsule_map WHERE id = ?", id);
            return n == 0 ? "Không có dòng id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(CapsuleMapDAO.class, ex, "Lỗi xoá điểm đến capsule");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static String kiemTra(Diem d) {
        if (d == null) {
            return "Thiếu dữ liệu.";
        }
        if (d.mapId < 0) {
            return "Id bản đồ phải từ 0 trở lên.";
        }
        if (d.dieuKien == null) {
            d.dieuKien = LUON;
        }
        if (!LUON.equals(d.dieuKien) && !LUYEN_TAP.equals(d.dieuKien)
                && !SUC_MANH.equals(d.dieuKien)) {
            return "Điều kiện phải là luon, luyen_tap hoặc suc_manh.";
        }
        if (d.nguong < 0) {
            return "Ngưỡng không được âm.";
        }
        return null;
    }
}
