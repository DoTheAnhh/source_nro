package nro.repository.dao;

import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Sách học tuyệt kỹ (chiêu thứ 9) bày trong cửa hàng Học kỹ năng của Quy Lão.
 *
 * <p>Ba cuốn có sẵn trong item_template: 1341 Sách Super Kamejoko (Trái Đất),
 * 1342 Sách Ma phong ba (Namếc), 1343 Sách Cađíc liên hoàn chưởng (Xayda). Trước
 * đây chỉ học được ở Whis bằng 9999 bí kiếp; nay bày luôn trong tab "Sách Chưởng"
 * (tab 22, cửa hàng SHOP_LEARN_SKILL) — cửa hàng tự lọc theo hành tinh của sách.
 * Bấm mua là học thẳng cấp 1 (xem {@code ShopService.hocTuyetKy}).</p>
 *
 * <p>Phải chạy TRƯỚC khi nạp cửa hàng (Manager gọi trước loadDatabase). Không cần
 * cờ: dòng nào chưa có mới thêm.</p>
 */
public final class SachHocTuyetKyDAO {

    private static final int TAB_SACH_CHUONG = 22;
    private static final int[] SACH = {1341, 1342, 1343};

    private SachHocTuyetKyDAO() {
    }

    public static void damBao() {
        try {
            int thuTu = 57;
            for (int id : SACH) {
                CrisResultSet rs = null;
                boolean co;
                try {
                    rs = ConnectDB.executeQuery("SELECT id FROM item_shop WHERE tab_id = ? AND temp_id = ?",
                            TAB_SACH_CHUONG, id);
                    co = rs.next();
                } finally {
                    if (rs != null) {
                        rs.dispose();
                    }
                }
                if (!co) {
                    ConnectDB.executeUpdate("INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost,"
                            + " costgold, icon_spec, thu_tu) VALUES (?,?,1,1,1,1,0,4028,?)", TAB_SACH_CHUONG, id, thuTu);
                    Logger.success("Cửa hàng Học kỹ năng: thêm sách tuyệt kỹ " + id + "\n");
                }
                thuTu++;
            }
        } catch (Exception ex) {
            Logger.logException(SachHocTuyetKyDAO.class, ex, "Không thêm được sách tuyệt kỹ vào cửa hàng");
        }
    }
}
