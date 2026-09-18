package nro.repository.dao;

import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Cải trang dựng sẵn trong mã nguồn — tự mọc ra lúc máy chủ khởi động.
 *
 * <h2>Vì sao không dùng tệp SQL</h2>
 *
 * <p>Máy chạy thật chỉ {@code git pull} rồi khởi động lại; một tệp SQL nằm
 * trong {@code sql/} không ai chạy thì vật phẩm không bao giờ có mặt. Hàm
 * {@link #damBao()} thêm dòng {@code item_template} nếu còn thiếu, chạy lại
 * bao nhiêu lần cũng không đẻ thêm.</p>
 *
 * <h2>Nhận diện bằng ẢNH, không bằng id</h2>
 *
 * <p>Id part và id vật phẩm là của riêng từng CSDL: bộ Berus Nhí ở máy này là
 * part 1422/1423/1424, bên NRO NGOL lại là 1612/1613/1614. Nên part được tìm
 * theo kiểu part + ảnh khung đầu tiên (ảnh là tệp trong mã nguồn nên mã ảnh
 * cố định), còn vật phẩm coi là đã có khi có một cải trang trỏ đúng ba part
 * ấy. Id vật phẩm mới lấy {@code MAX(id) + 1}, giống {@link TrungDeTuDAO}.</p>
 *
 * <p>CSDL nào chưa có part thì thêm vào <b>cuối</b> ({@code MAX(id) + 1}):
 * client nhận part theo thứ tự trong {@code data/update_data/part}, xen giữa
 * là lệch hết phía sau.</p>
 *
 * <p>Không phải tự tăng phiên bản: {@code ConnectDB.executeUpdate} thấy câu
 * lệnh đụng {@code item_template}/{@code part} là tự tăng (xem
 * {@code ConfigDAO.tuDayDuLieu}).</p>
 */
public final class CaiTrangDungSanDAO {

    private CaiTrangDungSanDAO() {
    }

    private static final class CaiTrang {

        final String ten;
        final String moTa;
        final int icon;
        final int avatar;
        /** Dữ liệu part đầu, thân, chân — dùng khi CSDL chưa có. */
        final String[] part;

        CaiTrang(String ten, String moTa, int icon, int avatar, String dau,
                String than, String chan) {
            this.ten = ten;
            this.moTa = moTa;
            this.icon = icon;
            this.avatar = avatar;
            this.part = new String[]{dau, than, chan};
        }
    }

    /** Berus Nhí — cũng là hình mặc định của đệ Bill (xem {@code DeTuDAO}). */
    private static final CaiTrang BERUS_NHI = new CaiTrang("Cải Trang Berus Nhí",
            "Thần Hủy Diệt thu nhỏ — tai to, mặt búng ra sữa, cơn giận vẫn nguyên cỡ.",
            12825, 12824,
            "[[12793,3,2],[12794,5,3],[2955,0,0]]",
            "[[12795,3,1],[12796,-1,0],[12797,-1,1],[12798,2,2],[12799,1,1],"
            + "[12800,3,1],[12801,2,1],[12802,2,1],[12803,2,3],[12804,1,4],"
            + "[12805,0,3],[12806,-1,3],[12807,2,3],[12808,2,2],[12809,1,2],"
            + "[12810,0,2],[2955,0,0]]",
            "[[12811,5,-3],[12812,-6,2],[12813,-6,1],[12814,-3,2],[12815,-6,0],"
            + "[12816,0,1],[12817,-1,1],[12818,3,5],[12819,-8,3],[12820,-2,2],"
            + "[12821,2,4],[12822,1,7],[12823,3,3],[2955,0,0]]");

    private static final CaiTrang[] DUNG_SAN = {BERUS_NHI};

    /** Kiểu vật phẩm cải trang. */
    private static final int KIEU_CAI_TRANG = 5;

    /**
     * Thêm các cải trang còn thiếu.
     *
     * <p>Gọi <b>trước</b> {@code Manager.loadDatabase()}: danh sách mẫu vật phẩm
     * và part chỉ đọc một lần lúc khởi động.</p>
     */
    public static synchronized void damBao() {
        for (CaiTrang c : DUNG_SAN) {
            try {
                damBaoMot(c);
            } catch (Exception ex) {
                // Mot bo hong thi bo do van phai thu tiep bo sau.
                Logger.logException(CaiTrangDungSanDAO.class, ex,
                        "Không dựng được " + c.ten);
            }
        }
    }

    /** Id vật phẩm Cải Trang Berus Nhí trong CSDL này, hoặc -1 nếu chưa có. */
    public static int idBerusNhi() {
        try {
            int[] part = new int[3];
            for (int kieu = 0; kieu < 3; kieu++) {
                part[kieu] = timPart(kieu, BERUS_NHI.part[kieu]);
                if (part[kieu] < 0) {
                    return -1;
                }
            }
            return timVatPham(part);
        } catch (Exception ex) {
            Logger.logException(CaiTrangDungSanDAO.class, ex,
                    "Không tra được id Berus Nhí");
            return -1;
        }
    }

    private static void damBaoMot(CaiTrang c) throws Exception {
        int[] part = new int[3];
        for (int kieu = 0; kieu < 3; kieu++) {
            part[kieu] = timPart(kieu, c.part[kieu]);
            if (part[kieu] < 0) {
                part[kieu] = themPart(kieu, c.part[kieu]);
            }
        }
        ConnectDB.executeUpdate("INSERT IGNORE INTO head_avatar (head_id, avatar_id)"
                + " VALUES (?, ?)", part[0], c.avatar);
        if (timVatPham(part) > 0) {
            return;
        }
        int id = idTrong("item_template");
        if (id < 0 || id > 32766) {
            return;
        }
        ConnectDB.executeUpdate("INSERT INTO item_template"
                + " (id, TYPE, gender, NAME, description, level, icon_id, part,"
                + " is_up_to_up, power_require, gold, gold_sell, gem, gem_sell,"
                + " ruby, ruby_sell, head, body, leg, TypeEvent, isGender,"
                + " dung_duoc, aura_id)"
                + " VALUES (?, ?, 3, ?, ?, 1, ?, -1, 0, 0, 0, 0, 0, 0, 0, 0,"
                + " ?, ?, ?, 0, -1, 1, -1)",
                id, KIEU_CAI_TRANG, c.ten, c.moTa, c.icon,
                part[0], part[1], part[2]);
        Logger.success("Đã thêm " + c.ten + " (id " + id + ", part "
                + part[0] + "/" + part[1] + "/" + part[2] + ")\n");
    }

    /** Part cùng kiểu có khung đầu dùng đúng ảnh ấy, hoặc -1. */
    private static int timPart(int kieu, String duLieu) throws Exception {
        String khungDau = duLieu.substring(0, duLieu.indexOf(',') + 1);
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MIN(id) AS m FROM part"
                    + " WHERE type = ? AND data LIKE ?", kieu, khungDau + "%");
            if (!rs.next()) {
                return -1;
            }
            int m = rs.getInt("m");
            // MIN tren tap rong ra NULL, doc thanh 0 — part 0 la dau goc,
            // khong bao gio la bo nay.
            return m > 0 ? m : -1;
        } finally {
            dong(rs);
        }
    }

    private static int themPart(int kieu, String duLieu) throws Exception {
        int id = idTrong("part");
        ConnectDB.executeUpdate("INSERT INTO part (id, type, data) VALUES (?, ?, ?)",
                id, kieu, duLieu);
        return id;
    }

    /** Cải trang trỏ đúng ba part này (id nhỏ nhất), hoặc -1. */
    private static int timVatPham(int[] part) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MIN(id) AS m FROM item_template"
                    + " WHERE TYPE = ? AND head = ? AND body = ? AND leg = ?",
                    KIEU_CAI_TRANG, part[0], part[1], part[2]);
            int m = rs.next() ? rs.getInt("m") : 0;
            return m > 0 ? m : -1;
        } finally {
            dong(rs);
        }
    }

    private static int idTrong(String bang) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT MAX(id) AS m FROM " + bang);
            return rs.next() ? rs.getInt("m") + 1 : -1;
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
