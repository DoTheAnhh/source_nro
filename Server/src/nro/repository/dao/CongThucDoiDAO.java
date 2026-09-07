package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Công thức đổi vật phẩm của NPC — trước đây viết cứng trong mã nguồn.
 *
 * <p>Mỗi dòng mô tả trọn một công thức: nguyên liệu cần, tỉ lệ thành công, kết
 * quả nhận được, chỉ số kèm theo, và phần hao khi thất bại. Sửa trên panel là
 * có hiệu lực ngay, không phải sửa mã rồi build lại.</p>
 *
 * <h3>Định dạng các cột chuỗi</h3>
 * <ul>
 *   <li>{@code nguyen_lieu}, {@code hao_khi_that_bai}: {@code id:sốLượng} ngăn
 *       nhau bằng dấu phẩy — ví dụ {@code 1281:9999,1282:1}</li>
 *   <li>{@code ket_qua}: danh sách id, bốc ngẫu nhiên một cái —
 *       ví dụ {@code 1044,1211,1212}</li>
 *   <li>{@code chi_so_ket_qua}: {@code idChỉSố:giáTrị} ngăn nhau bằng dấu phẩy</li>
 * </ul>
 */
public class CongThucDoiDAO {

    public static final class CongThuc {

        public int id;
        public String ma;
        public String ten;
        /** Cặp {id, số lượng}. */
        public final List<int[]> nguyenLieu = new ArrayList<>();
        /** Phần trăm thành công, 0–100. */
        public int tyLe = 100;
        public final List<Integer> ketQua = new ArrayList<>();
        /** Cặp {id chỉ số, giá trị}. */
        public final List<int[]> chiSo = new ArrayList<>();
        /** Cặp {id, số lượng} bị trừ khi THẤT BẠI. */
        public final List<int[]> haoKhiThatBai = new ArrayList<>();
        public boolean bat = true;
        public String ghiChu;

        /**
         * NPC nào cho đổi công thức này; rỗng là không gắn NPC nào.
         *
         * <p>Trước đây mỗi công thức chỉ chạy được từ chỗ gọi viết cứng trong mã
         * (Bà Hạt Mít, NPC đổi sách…). Cột này cho gắn thêm công thức vào một NPC
         * mà không phải sửa mã: quản trị chọn NPC trên panel là NPC đó tự có mục
         * đổi. Giá trị dùng hằng {@link #NPC_KHONG}, {@link #NPC_WHIS_NGUC_TU}.</p>
         */
        public String npc = NPC_KHONG;
    }

    /** Không gắn NPC — công thức chỉ chạy từ chỗ gọi viết cứng trong mã. */
    public static final String NPC_KHONG = "";

    /** Whis ở hành tinh Ngục Tù (map 155). */
    public static final String NPC_WHIS_NGUC_TU = "WHIS_NGUC_TU";

    /** Các NPC gắn được, để đổ vào ô chọn trên panel. */
    public static final String[] CAC_NPC = {NPC_KHONG, NPC_WHIS_NGUC_TU};

    /** Tên tiếng Việt của một NPC, để hiện trên panel. */
    public static String tenNpc(String npc) {
        if (npc == null || npc.isEmpty()) {
            return "(không gắn NPC)";
        }
        switch (npc) {
            case NPC_WHIS_NGUC_TU:
                return "Whis — hành tinh Ngục Tù";
            default:
                return npc;
        }
    }

    /**
     * Các công thức <b>đang bật</b> đã gắn vào một NPC, theo thứ tự id.
     *
     * <p>Trả danh sách rỗng nếu NPC đó chưa có công thức nào — chỗ gọi nên bỏ hẳn
     * mục đổi khỏi menu thay vì mở ra một menu trống.</p>
     */
    public static List<CongThuc> theoNpc(String npc) {
        List<CongThuc> ds = new ArrayList<>();
        if (npc == null || npc.isEmpty()) {
            return ds;
        }
        damBaoCotNpc();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM cong_thuc_doi"
                    + " WHERE bat = 1 AND npc = ? ORDER BY id", npc);
            while (rs.next()) {
                ds.add(doc(rs));
            }
        } catch (Exception ex) {
            Logger.logException(CongThucDoiDAO.class, ex,
                    "Không đọc được công thức đổi của NPC " + npc);
        } finally {
            dong(rs);
        }
        return ds;
    }

    private static boolean daThemCotNpc;

    /**
     * Thêm cột {@code npc} nếu bảng chưa có.
     *
     * <p>Bảng này nằm trong bản dump chứ không do mã tự tạo, nên không thể trông
     * vào {@code CREATE TABLE IF NOT EXISTS} như các bảng khác. Chép mã sang máy
     * đang chạy bản cũ mà thiếu bước này là mọi câu đọc bảng đều lỗi cột.</p>
     */
    public static synchronized void damBaoCotNpc() {
        if (daThemCotNpc) {
            return;
        }
        daThemCotNpc = true;
        try {
            ConnectDB.executeUpdate("ALTER TABLE cong_thuc_doi"
                    + " ADD COLUMN npc VARCHAR(32) NOT NULL DEFAULT ''");
        } catch (Exception ignored) {
            // Da co cot roi -> MySQL bao loi trung ten, dung y muon.
        }
    }

    /**
     * Đọc một công thức theo mã. Trả {@code null} nếu không có hoặc đang tắt —
     * chỗ gọi phải tự xử lý, không được coi như công thức rỗng.
     */
    public static CongThuc theoMa(String ma) {
        damBaoCotNpc();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT * FROM cong_thuc_doi WHERE ma = ? AND bat = 1", ma);
            if (!rs.next()) {
                return null;
            }
            return doc(rs);
        } catch (Exception ex) {
            Logger.logException(CongThucDoiDAO.class, ex,
                    "Không đọc được công thức đổi " + ma);
            return null;
        } finally {
            dong(rs);
        }
    }

    /** Toàn bộ công thức, kể cả cái đang tắt — dùng cho panel. */
    public static List<CongThuc> tatCa() {
        damBaoCotNpc();
        List<CongThuc> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM cong_thuc_doi ORDER BY id");
            while (rs.next()) {
                ds.add(doc(rs));
            }
        } catch (Exception ex) {
            Logger.logException(CongThucDoiDAO.class, ex, "Không đọc được bảng công thức đổi");
        } finally {
            dong(rs);
        }
        return ds;
    }

    private static CongThuc doc(CrisResultSet rs) throws Exception {
        CongThuc c = new CongThuc();
        c.id = rs.getInt("id");
        c.ma = rs.getString("ma");
        c.ten = rs.getString("ten");
        c.tyLe = rs.getInt("ty_le");
        c.bat = rs.getBoolean("bat");
        c.ghiChu = rs.getStringOrNull("ghi_chu");
        try {
            String n = rs.getStringOrNull("npc");
            c.npc = n == null ? NPC_KHONG : n;
        } catch (Exception ignored) {
            // Ban CSDL cu chua co cot npc -> coi nhu khong gan NPC nao.
            c.npc = NPC_KHONG;
        }
        docCap(c.nguyenLieu, rs.getStringOrNull("nguyen_lieu"));
        docCap(c.haoKhiThatBai, rs.getStringOrNull("hao_khi_that_bai"));
        docCap(c.chiSo, rs.getStringOrNull("chi_so_ket_qua"));
        String kq = rs.getStringOrNull("ket_qua");
        if (kq != null) {
            for (String s : kq.split(",")) {
                String t = s.trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    c.ketQua.add(Integer.parseInt(t));
                } catch (NumberFormatException ignored) {
                    // Gõ sai một id thì bỏ id đó, không làm hỏng cả công thức.
                }
            }
        }
        return c;
    }

    /** Đọc chuỗi {@code a:b,c:d} thành danh sách cặp. Phần nào sai thì bỏ qua. */
    private static void docCap(List<int[]> ra, String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return;
        }
        for (String phan : raw.split(",")) {
            String[] p = phan.trim().split(":");
            if (p.length != 2) {
                continue;
            }
            try {
                ra.add(new int[]{Integer.parseInt(p[0].trim()),
                    Integer.parseInt(p[1].trim())});
            } catch (NumberFormatException ignored) {
                // Dòng gõ sai -> bỏ riêng dòng đó.
            }
        }
    }

    /** Ghi lại một công thức. {@code id <= 0} là thêm mới. */
    public static String luu(int id, String ma, String ten, String nguyenLieu,
            int tyLe, String ketQua, String chiSo, String haoKhiThatBai,
            boolean bat, String ghiChu, String npc) {
        if (ma == null || ma.trim().isEmpty()) {
            return "Mã công thức không được để trống.";
        }
        if (tyLe < 0 || tyLe > 100) {
            return "Tỉ lệ thành công phải từ 0 đến 100.";
        }
        damBaoCotNpc();
        String n = npc == null ? NPC_KHONG : npc;
        try {
            if (id > 0) {
                ConnectDB.executeUpdate(
                        "UPDATE cong_thuc_doi SET ma = ?, ten = ?, nguyen_lieu = ?,"
                        + " ty_le = ?, ket_qua = ?, chi_so_ket_qua = ?,"
                        + " hao_khi_that_bai = ?, bat = ?, ghi_chu = ?, npc = ?"
                        + " WHERE id = ?",
                        ma.trim(), ten, nguyenLieu, tyLe, ketQua, chiSo,
                        haoKhiThatBai, bat ? 1 : 0, ghiChu, n, id);
            } else {
                ConnectDB.executeUpdate(
                        "INSERT INTO cong_thuc_doi (ma, ten, nguyen_lieu, ty_le,"
                        + " ket_qua, chi_so_ket_qua, hao_khi_that_bai, bat,"
                        + " ghi_chu, npc)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        ma.trim(), ten, nguyenLieu, tyLe, ketQua, chiSo,
                        haoKhiThatBai, bat ? 1 : 0, ghiChu, n);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(CongThucDoiDAO.class, ex, "Lỗi lưu công thức đổi");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM cong_thuc_doi WHERE id = ?", id);
            return null;
        } catch (Exception ex) {
            Logger.logException(CongThucDoiDAO.class, ex, "Lỗi xoá công thức đổi");
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
}
