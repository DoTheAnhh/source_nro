package nro.repository.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.schema.LuocDoPanel;

/**
 * Điểm đến của menu <b>Bản đồ</b> trong game — đọc từ bảng {@code map_nhanh}.
 *
 * <h2>Menu này là gì</h2>
 *
 * <p>Mục "Bản đồ" trong menu Chức Năng của client gửi lên một lệnh, máy chủ mở
 * ra một menu hai tầng: tầng đầu là <b>nhóm</b> (Trái Đất, Namếc, Xayda, ...),
 * chọn nhóm rồi mới ra danh sách bản đồ trong nhóm đó. Hai tầng vì client chỉ
 * vẽ gọn được chừng mười mấy mục một lần — hơn tám mươi bản đồ đổ ra một menu
 * là tràn màn hình.</p>
 *
 * <h2>Toạ độ</h2>
 *
 * <p>Để {@code x} hoặc {@code y} bằng {@code -1} là máy chủ tự chọn: {@code x}
 * lấy giữa bản đồ, {@code y} lấy mặt đất ngay dưới điểm đó. Gõ số cụ thể khi
 * muốn thả người chơi đúng một chỗ (cạnh NPC, trước cửa hang...).</p>
 *
 * <p>Cờ {@code theoHanhTinh} nghĩa là bản đồ thật bằng {@code mapId + gender} —
 * giống {@link CapsuleMapDAO}, dùng cho ba map nhà và ba map trạm tàu.</p>
 *
 * <h2>Bảng rỗng thì gieo lại danh sách sẵn</h2>
 *
 * <p>Lần đầu chạy, hoặc sau khi ai đó xoá sạch bảng, {@link #danhSach()} gieo
 * lại danh sách trong {@code danhSachGoc()} để panel không mở ra trống trơn.
 * Sửa và thêm thoải mái — chỉ cần bảng còn ít nhất một dòng là không gieo nữa,
 * nên sửa của admin không bao giờ bị ghi đè.</p>
 */
public final class MapNhanhDAO {

    private MapNhanhDAO() {
    }

    /** Một điểm đến. */
    public static final class Diem {

        public int id;
        public String nhom = "";
        public String ten = "";
        public int mapId;
        public int x = -1;
        public int y = -1;
        public boolean theoHanhTinh;
        public int thuTu;
        public boolean bat = true;
        public String ghiChu = "";

        public Diem() {
        }

        public Diem(String nhom, int thuTu, int mapId, String ten) {
            this.nhom = nhom;
            this.thuTu = thuTu;
            this.mapId = mapId;
            this.ten = ten;
        }

        public Diem theoHanhTinh() {
            this.theoHanhTinh = true;
            return this;
        }

        /** Tên hiện trong menu game; để trống thì lấy tạm id bản đồ. */
        public String nhan() {
            return ten == null || ten.trim().isEmpty()
                    ? ("Bản đồ " + mapId) : ten.trim();
        }

        public String moTaToaDo() {
            return x < 0 || y < 0 ? "tự đặt" : (x + " , " + y);
        }
    }

    /** Danh sách gieo sẵn — gom theo hành tinh và theo vùng của bản đồ gốc. */
    private static List<Diem> danhSachGoc() {
        List<Diem> ds = new ArrayList<>();
        int t = 0;

        ds.add(new Diem("Thường dùng", t++, 21, "Nhà").theoHanhTinh());
        ds.add(new Diem("Thường dùng", t++, 24, "Trạm tàu vũ trụ").theoHanhTinh());
        ds.add(new Diem("Thường dùng", t++, 84, "Siêu Thị"));
        ds.add(new Diem("Thường dùng", t++, 104, "Sân sau siêu thị"));
        ds.add(new Diem("Thường dùng", t++, 47, "Rừng Karin"));
        ds.add(new Diem("Thường dùng", t++, 46, "Tháp Karin"));
        ds.add(new Diem("Thường dùng", t++, 45, "Thần điện"));

        ds.add(new Diem("Trái Đất", t++, 0, "Làng Aru"));
        ds.add(new Diem("Trái Đất", t++, 1, "Đồi hoa cúc"));
        ds.add(new Diem("Trái Đất", t++, 2, "Thung lũng tre"));
        ds.add(new Diem("Trái Đất", t++, 3, "Rừng nấm"));
        ds.add(new Diem("Trái Đất", t++, 4, "Rừng xương"));
        ds.add(new Diem("Trái Đất", t++, 5, "Đảo Kamê"));
        ds.add(new Diem("Trái Đất", t++, 6, "Đông Karin"));
        ds.add(new Diem("Trái Đất", t++, 27, "Rừng Bamboo"));
        ds.add(new Diem("Trái Đất", t++, 28, "Rừng dương xỉ"));
        ds.add(new Diem("Trái Đất", t++, 29, "Nam Kamê"));
        ds.add(new Diem("Trái Đất", t++, 30, "Đảo Bulông"));
        ds.add(new Diem("Trái Đất", t++, 42, "Vách núi Aru"));

        ds.add(new Diem("Namếc", t++, 7, "Làng Mori"));
        ds.add(new Diem("Namếc", t++, 8, "Đồi nấm tím"));
        ds.add(new Diem("Namếc", t++, 9, "Thị trấn Moori"));
        ds.add(new Diem("Namếc", t++, 10, "Thung lũng Namếc"));
        ds.add(new Diem("Namếc", t++, 11, "Thung lũng Maima"));
        ds.add(new Diem("Namếc", t++, 12, "Vực Maima"));
        ds.add(new Diem("Namếc", t++, 13, "Đảo Guru"));
        ds.add(new Diem("Namếc", t++, 31, "Núi hoa vàng"));
        ds.add(new Diem("Namếc", t++, 32, "Núi hoa tím"));
        ds.add(new Diem("Namếc", t++, 33, "Nam Guru"));
        ds.add(new Diem("Namếc", t++, 34, "Đông Nam Guru"));
        ds.add(new Diem("Namếc", t++, 43, "Vách núi Moori"));

        ds.add(new Diem("Xayda", t++, 14, "Làng Kakarot"));
        ds.add(new Diem("Xayda", t++, 15, "Đồi hoang"));
        ds.add(new Diem("Xayda", t++, 16, "Làng Plant"));
        ds.add(new Diem("Xayda", t++, 17, "Rừng nguyên sinh"));
        ds.add(new Diem("Xayda", t++, 18, "Rừng thông Xayda"));
        ds.add(new Diem("Xayda", t++, 19, "Thành phố Vegeta"));
        ds.add(new Diem("Xayda", t++, 20, "Vách núi đen"));
        ds.add(new Diem("Xayda", t++, 35, "Rừng cọ"));
        ds.add(new Diem("Xayda", t++, 36, "Rừng đá"));
        ds.add(new Diem("Xayda", t++, 37, "Thung lũng đen"));
        ds.add(new Diem("Xayda", t++, 38, "Bờ vực đen"));
        ds.add(new Diem("Xayda", t++, 44, "Vách núi Kakarot"));

        ds.add(new Diem("Nappa", t++, 63, "Trại lính Fide"));
        ds.add(new Diem("Nappa", t++, 64, "Núi dây leo"));
        ds.add(new Diem("Nappa", t++, 65, "Núi cây quỷ"));
        ds.add(new Diem("Nappa", t++, 66, "Trại quỷ già"));
        ds.add(new Diem("Nappa", t++, 67, "Mê Cung Chết Chóc"));
        ds.add(new Diem("Nappa", t++, 68, "Thung lũng Nappa"));
        ds.add(new Diem("Nappa", t++, 69, "Vực cấm"));
        ds.add(new Diem("Nappa", t++, 70, "Núi Appule"));
        ds.add(new Diem("Nappa", t++, 71, "Căn cứ Raspberry"));
        ds.add(new Diem("Nappa", t++, 72, "Thung lũng Raspberry"));
        ds.add(new Diem("Nappa", t++, 73, "Thung lũng chết"));
        ds.add(new Diem("Nappa", t++, 74, "Đồi cây Fide"));
        ds.add(new Diem("Nappa", t++, 75, "Khe núi tử thần"));

        ds.add(new Diem("Cold", t++, 105, "Cánh đồng tuyết"));
        ds.add(new Diem("Cold", t++, 106, "Rừng tuyết"));
        ds.add(new Diem("Cold", t++, 107, "Núi tuyết"));
        ds.add(new Diem("Cold", t++, 108, "Dòng sông băng"));
        ds.add(new Diem("Cold", t++, 109, "Rừng băng"));
        ds.add(new Diem("Cold", t++, 110, "Hang băng"));
        ds.add(new Diem("Cold", t++, 111, "Đông Nam Karin"));

        ds.add(new Diem("Tương Lai", t++, 92, "Thành phố phía đông"));
        ds.add(new Diem("Tương Lai", t++, 93, "Thành phố phía nam"));
        ds.add(new Diem("Tương Lai", t++, 97, "Thành phố phía bắc"));
        ds.add(new Diem("Tương Lai", t++, 100, "Thị trấn Ginder"));
        ds.add(new Diem("Tương Lai", t++, 94, "Đảo Balê"));
        ds.add(new Diem("Tương Lai", t++, 96, "Cao nguyên"));
        ds.add(new Diem("Tương Lai", t++, 98, "Ngọn núi phía bắc"));
        ds.add(new Diem("Tương Lai", t++, 99, "Thung lũng phía bắc"));
        ds.add(new Diem("Tương Lai", t++, 102, "Nhà Bunma"));

        ds.add(new Diem("Ngũ Hành Sơn", t++, 122, "Ngũ hành sơn 1"));
        ds.add(new Diem("Ngũ Hành Sơn", t++, 123, "Ngũ hành sơn 2"));
        ds.add(new Diem("Ngũ Hành Sơn", t++, 124, "Ngũ hành sơn 3"));

        ds.add(new Diem("Núi khỉ", t++, 79, "Núi khỉ đỏ"));
        ds.add(new Diem("Núi khỉ", t++, 80, "Núi khỉ vàng"));
        ds.add(new Diem("Núi khỉ", t++, 82, "Núi khỉ đen"));
        ds.add(new Diem("Núi khỉ", t++, 83, "Hang khỉ đen"));
        ds.add(new Diem("Núi khỉ", t++, 81, "Hang quỷ chim"));

        ds.add(new Diem("Luyện tập", t++, 48, "Hành tinh Kaio"));
        ds.add(new Diem("Luyện tập", t++, 50, "Thánh địa Kaio"));
        ds.add(new Diem("Luyện tập", t++, 49, "Phòng tập thời gian"));
        ds.add(new Diem("Luyện tập", t++, 51, "Đấu trường"));
        ds.add(new Diem("Luyện tập", t++, 52, "Đại Hội Võ Thuật"));
        ds.add(new Diem("Luyện tập", t++, 103, "Võ đài Xên bọ hung"));
        ds.add(new Diem("Luyện tập", t++, 112, "Võ đài Hạt Mít"));

        return ds;
    }

    /** Toàn bộ điểm đến, đã sắp. Bảng rỗng thì gieo lại danh sách sẵn. */
    public static List<Diem> danhSach() {
        LuocDoPanel.damBao();
        List<Diem> ds = doc();
        if (ds.isEmpty()) {
            gieoLai();
            ds = doc();
        }
        return ds;
    }

    /**
     * Chỉ những điểm đang bật, gom theo nhóm và giữ nguyên thứ tự nhóm xuất
     * hiện lần đầu — chính là thứ tự menu tầng một hiện ra trong game.
     */
    public static Map<String, List<Diem>> theoNhom() {
        Map<String, List<Diem>> ra = new LinkedHashMap<>();
        for (Diem d : danhSach()) {
            if (!d.bat) {
                continue;
            }
            String n = d.nhom == null || d.nhom.trim().isEmpty()
                    ? "Khác" : d.nhom.trim();
            List<Diem> trong = ra.get(n);
            if (trong == null) {
                trong = new ArrayList<>();
                ra.put(n, trong);
            }
            trong.add(d);
        }
        return ra;
    }

    private static List<Diem> doc() {
        List<Diem> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, nhom, ten, map_id, x, y, theo_hanh_tinh,"
                    + " thu_tu, bat, ghi_chu FROM map_nhanh"
                    + " ORDER BY thu_tu, id");
            while (rs.next()) {
                Diem d = new Diem();
                d.id = rs.getInt("id");
                d.nhom = chuoi(rs.getString("nhom"));
                d.ten = chuoi(rs.getString("ten"));
                d.mapId = rs.getInt("map_id");
                d.x = rs.getInt("x");
                d.y = rs.getInt("y");
                d.theoHanhTinh = rs.getInt("theo_hanh_tinh") != 0;
                d.thuTu = rs.getInt("thu_tu");
                d.bat = rs.getInt("bat") != 0;
                d.ghiChu = chuoi(rs.getString("ghi_chu"));
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(MapNhanhDAO.class, ex,
                    "Lỗi đọc bảng bản đồ nhanh");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                }
            }
        }
        return ds;
    }

    private static String chuoi(String s) {
        return s == null ? "" : s;
    }

    /** Ghi lại danh sách sẵn. Chỉ gọi khi bảng đang rỗng. */
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
                    "INSERT INTO map_nhanh (nhom, ten, map_id, x, y,"
                    + " theo_hanh_tinh, thu_tu, bat, ghi_chu)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    chuoi(d.nhom), chuoi(d.ten), d.mapId, d.x, d.y,
                    d.theoHanhTinh ? 1 : 0, d.thuTu, d.bat ? 1 : 0,
                    chuoi(d.ghiChu));
            return null;
        } catch (Exception ex) {
            Logger.logException(MapNhanhDAO.class, ex, "Lỗi thêm bản đồ nhanh");
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
                    "UPDATE map_nhanh SET nhom = ?, ten = ?, map_id = ?,"
                    + " x = ?, y = ?, theo_hanh_tinh = ?, thu_tu = ?,"
                    + " bat = ?, ghi_chu = ? WHERE id = ?",
                    chuoi(d.nhom), chuoi(d.ten), d.mapId, d.x, d.y,
                    d.theoHanhTinh ? 1 : 0, d.thuTu, d.bat ? 1 : 0,
                    chuoi(d.ghiChu), d.id);
            return n == 0 ? "Không có dòng id " + d.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(MapNhanhDAO.class, ex, "Lỗi sửa bản đồ nhanh");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** @return {@code null} nếu xoá xong, ngược lại là lý do */
    public static String xoa(int id) {
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate(
                    "DELETE FROM map_nhanh WHERE id = ?", id);
            return n == 0 ? "Không có dòng id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(MapNhanhDAO.class, ex, "Lỗi xoá bản đồ nhanh");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Bật hoặc tắt một dòng mà không phải mở hộp thoại sửa. */
    public static String batTat(int id, boolean bat) {
        try {
            LuocDoPanel.damBao();
            int n = ConnectDB.executeUpdate(
                    "UPDATE map_nhanh SET bat = ? WHERE id = ?", bat ? 1 : 0, id);
            return n == 0 ? "Không có dòng id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(MapNhanhDAO.class, ex, "Lỗi bật/tắt bản đồ nhanh");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Xoá sạch rồi gieo lại — nút "Khôi phục danh sách sẵn" trên panel. */
    public static String gieoLaiTuDau() {
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate("DELETE FROM map_nhanh");
            gieoLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(MapNhanhDAO.class, ex, "Lỗi gieo lại bản đồ nhanh");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static String kiemTra(Diem d) {
        if (d == null) {
            return "Thiếu dữ liệu.";
        }
        if (d.mapId < 0) {
            return "ID bản đồ phải từ 0 trở lên.";
        }
        if (d.nhan().isEmpty()) {
            return "Phải đặt tên hiện trong menu.";
        }
        return null;
    }
}
