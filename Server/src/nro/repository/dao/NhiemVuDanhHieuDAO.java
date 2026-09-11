package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.badges.BadgesTaskTemplate;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Manager;

/**
 * Nhiệm vụ để nhận danh hiệu — bảng {@code task_badges_template}.
 *
 * <h2>Loại nhiệm vụ</h2>
 *
 * <p>Bảng gốc chỉ có <i>tên</i>, <i>số lần cần đạt</i> và <i>danh hiệu thưởng</i>.
 * Tiến độ do mã nguồn tự gọi {@code updateCountBagesTask} ở đúng chỗ, nên panel
 * không tạo được nhiệm vụ mới — chỉ sửa chữ của những nhiệm vụ đã viết sẵn.</p>
 *
 * <p>Hai cột thêm ở đây, {@code loai} và {@code tham_so}, mở ra các loại
 * <b>tự đếm</b>: giết quái, hạ boss, mua/dùng/nhặt vật phẩm, tiêu vàng… Máy chủ
 * bắt các sự kiện đó ở một chỗ chung và cộng tiến độ cho mọi nhiệm vụ khớp
 * loại, nên tạo nhiệm vụ mới chỉ là thêm một dòng trên panel.</p>
 *
 * <p>Để trống loại thì giữ nguyên cách cũ — nhiệm vụ viết sẵn vẫn chạy y như
 * trước, không đụng gì tới chúng.</p>
 */
public final class NhiemVuDanhHieuDAO {

    private NhiemVuDanhHieuDAO() {
    }

    private static volatile boolean daThemCot;

    /**
     * Thêm hai cột nếu CSDL chưa có.
     *
     * <p>Đặt ở DAO thay vì {@code LuocDoPanel}: lớp kia cố ý chỉ TẠO bảng chứ
     * không sửa bảng đã tồn tại, để mỗi thay đổi nằm cạnh đoạn mã cần tới nó.</p>
     */
    public static void damBaoCot() {
        if (daThemCot) {
            return;
        }
        daThemCot = true;
        them("ALTER TABLE `task_badges_template` ADD COLUMN IF NOT EXISTS `loai`"
                + " varchar(20) NOT NULL DEFAULT ''");
        them("ALTER TABLE `task_badges_template` ADD COLUMN IF NOT EXISTS `tham_so`"
                + " int(11) NOT NULL DEFAULT -1");
    }

    private static void them(String sql) {
        try {
            ConnectDB.executeUpdate(sql);
        } catch (Exception ex) {
            // Cot da co roi -> MySQL bao loi, bo qua. Day la duong duy nhat
            // "ADD COLUMN IF NOT EXISTS" chay duoc tren moi ban MariaDB.
        }
    }

    /** Một dòng nhiệm vụ. */
    public static final class NhiemVu {

        public int id;
        public String ten = "";
        public int soLan = 1;
        public int danhHieuThuong = -1;
        public String loai = BadgesTaskTemplate.GO_CUNG;
        public int thamSo = -1;

        public String moTaLoai() {
            String t = BadgesTaskTemplate.tenLoai(loai);
            if (thamSo >= 0 && !BadgesTaskTemplate.GO_CUNG.equals(loai)
                    && !BadgesTaskTemplate.HA_NGUOI_CHOI.equals(loai)
                    && !BadgesTaskTemplate.TIEU_VANG.equals(loai)
                    && !BadgesTaskTemplate.TIEU_NGOC.equals(loai)) {
                return t + " #" + thamSo;
            }
            return t + (thamSo < 0 && !BadgesTaskTemplate.GO_CUNG.equals(loai)
                    ? " (bất kỳ)" : "");
        }
    }

    public static List<NhiemVu> danhSach() {
        damBaoCot();
        List<NhiemVu> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, NAME, maxCount, idBadgesReward, loai, tham_so"
                    + " FROM task_badges_template ORDER BY id");
            while (rs.next()) {
                NhiemVu n = new NhiemVu();
                n.id = rs.getInt("id");
                n.ten = rs.getString("NAME");
                n.soLan = rs.getInt("maxCount");
                n.danhHieuThuong = rs.getInt("idBadgesReward");
                String l = rs.getString("loai");
                n.loai = l == null || l.trim().isEmpty()
                        ? BadgesTaskTemplate.GO_CUNG : l.trim();
                n.thamSo = rs.getInt("tham_so");
                if (n.ten == null) {
                    n.ten = "";
                }
                ds.add(n);
            }
        } catch (Exception ex) {
            Logger.logException(NhiemVuDanhHieuDAO.class, ex,
                    "Lỗi đọc bảng nhiệm vụ danh hiệu");
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

    public static NhiemVu theoId(int id) {
        for (NhiemVu n : danhSach()) {
            if (n.id == id) {
                return n;
            }
        }
        return null;
    }

    public static String them(NhiemVu n) {
        String loi = kiemTra(n);
        if (loi != null) {
            return loi;
        }
        try {
            damBaoCot();
            ConnectDB.executeUpdate(
                    "INSERT INTO task_badges_template (NAME, maxCount,"
                    + " idBadgesReward, loai, tham_so) VALUES (?, ?, ?, ?, ?)",
                    n.ten, n.soLan, n.danhHieuThuong, n.loai, n.thamSo);
            napLaiBoNho();
            return null;
        } catch (Exception ex) {
            Logger.logException(NhiemVuDanhHieuDAO.class, ex, "Lỗi thêm nhiệm vụ");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String sua(NhiemVu n) {
        String loi = kiemTra(n);
        if (loi != null) {
            return loi;
        }
        try {
            damBaoCot();
            int k = ConnectDB.executeUpdate(
                    "UPDATE task_badges_template SET NAME = ?, maxCount = ?,"
                    + " idBadgesReward = ?, loai = ?, tham_so = ? WHERE id = ?",
                    n.ten, n.soLan, n.danhHieuThuong, n.loai, n.thamSo, n.id);
            napLaiBoNho();
            return k == 0 ? "Không có nhiệm vụ id " + n.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(NhiemVuDanhHieuDAO.class, ex, "Lỗi sửa nhiệm vụ");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        try {
            int k = ConnectDB.executeUpdate(
                    "DELETE FROM task_badges_template WHERE id = ?", id);
            napLaiBoNho();
            return k == 0 ? "Không có nhiệm vụ id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(NhiemVuDanhHieuDAO.class, ex, "Lỗi xoá nhiệm vụ");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Nạp lại mẫu nhiệm vụ vào bộ nhớ.
     *
     * <p>Người chơi <b>đang online</b> vẫn giữ danh sách nhiệm vụ dựng lúc đăng
     * nhập, nên nhiệm vụ mới thêm chỉ tới với họ ở lần đăng nhập sau. Cố ý:
     * dựng lại giữa chừng sẽ xoá sạch tiến độ đang có.</p>
     */
    public static void napLaiBoNho() {
        try {
            Manager.TASKS_BADGES_TEMPLATE.clear();
            for (NhiemVu n : danhSach()) {
                BadgesTaskTemplate t = new BadgesTaskTemplate();
                t.id = n.id;
                t.name = n.ten;
                t.count = n.soLan;
                t.idbadgesReward = n.danhHieuThuong;
                t.loai = n.loai;
                t.thamSo = n.thamSo;
                Manager.TASKS_BADGES_TEMPLATE.add(t);
            }
        } catch (Exception ex) {
            Logger.logException(NhiemVuDanhHieuDAO.class, ex,
                    "Lỗi nạp lại nhiệm vụ vào bộ nhớ");
        }
    }

    private static String kiemTra(NhiemVu n) {
        if (n == null) {
            return "Thiếu dữ liệu.";
        }
        if (n.ten == null || n.ten.trim().isEmpty()) {
            return "Tên nhiệm vụ không được để trống.";
        }
        n.ten = n.ten.trim();
        if (n.soLan < 1) {
            return "Số lần cần đạt phải từ 1 trở lên.";
        }
        if (n.loai == null || n.loai.trim().isEmpty()) {
            n.loai = BadgesTaskTemplate.GO_CUNG;
        }
        boolean hopLe = false;
        for (String l : BadgesTaskTemplate.cacLoai()) {
            if (l.equals(n.loai)) {
                hopLe = true;
                break;
            }
        }
        if (!hopLe) {
            return "Loại nhiệm vụ không hợp lệ.";
        }
        return null;
    }
}
