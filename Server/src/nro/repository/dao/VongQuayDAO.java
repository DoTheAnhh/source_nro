package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.service.item.ItemService;

/**
 * Kho quà của <b>vòng quay Thượng Đế</b> — bảng {@code vong_quay_qua}.
 *
 * <h2>Vì sao có bảng này</h2>
 *
 * <p>Danh sách quà vốn viết cứng trong {@code LuckyRound.buildItemPool()}: một
 * dãy hơn hai chục lời gọi lồng nhau, mỗi món một kiểu tạo riêng. Thêm một món,
 * đổi tỉ lệ, hay chỉ là tắt tạm một món đều phải sửa mã rồi biên dịch lại — nên
 * trên thực tế không ai đụng vào.</p>
 *
 * <p>Bảng này thay chỗ đó. Lần chạy đầu tiên nó được <b>gieo đúng danh sách cũ</b>
 * nên vòng quay không đổi một li; từ đó trở đi mọi thứ sửa trên panel.</p>
 *
 * <h2>Một dòng quà gồm gì</h2>
 *
 * <ul>
 *   <li><b>Nhóm</b> — {@code 0} vòng quay thường, {@code 1} vòng quay VIP.</li>
 *   <li><b>Vật phẩm</b> — một hoặc nhiều id ngăn nhau bằng dấu phẩy. Nhiều id
 *       thì bốc ngẫu nhiên <i>một</i> cái, cơ hội đều nhau. Đó là cách gói gọn
 *       những mục kiểu "một trong năm mảnh".</li>
 *   <li><b>Chỉ số</b> — dạng {@code "87:0,30:0"}, tức id chỉ số hai chấm trị số.
 *       Để trống là món trơn.</li>
 *   <li><b>Trọng số</b> — bao nhiêu thì món đó chiếm bấy nhiêu chỗ trong rổ bốc.
 *       {@code 1} là mức thường; {@code 3} là dễ ra gấp ba lần một món trọng số
 *       một. Không phải phần trăm, nên sửa một dòng không bắt sửa lại dòng
 *       khác.</li>
 *   <li><b>Hạn dùng</b> — bốc trong khoảng ngày đặt sẵn, kèm một tỉ lệ ra bản
 *       VĨNH VIỄN. Hạn tối đa bằng 0 là món không có hạn.</li>
 * </ul>
 */
public final class VongQuayDAO {

    private VongQuayDAO() {
    }

    /**
     * Bật dùng bảng này thay cho danh sách viết trong mã.
     *
     * <p>Mặc định <b>tắt</b>. Bản gieo đầu tiên ra sai món — id vật phẩm trong
     * mã cũ tra theo vị trí trong bảng mẫu, mà bảng mẫu đã đổi từ lúc đoạn mã
     * ấy được viết — nên phải soát lại từng dòng trên panel rồi mới bật.</p>
     */
    public static final String KHOA_DUNG_PANEL = "vong_quay_panel";

    /** Vòng quay thường. */
    public static final int NHOM_THUONG = 0;

    /** Vòng quay VIP. */
    public static final int NHOM_VIP = 1;

    /** Một dòng quà. */
    public static final class Qua {

        public int id;
        public int nhom = NHOM_THUONG;
        public String vatPham = "";
        public String chiSo = "";
        public int trongSo = 1;
        public int soLuongMin = 1;
        public int soLuongMax = 1;

        /** Hạn dùng bốc trong khoảng này, đơn vị ngày. { 0} là không gắn. */
        public int hsdMin;
        public int hsdMax;

        /** Bao nhiêu phần trăm số lần bốc ra bản VĨNH VIỄN (không gắn hạn). */
        public int tiLeVinhVien;
        public boolean bat = true;
        public String ghiChu = "";
    }

    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `vong_quay_qua` ("
            + " `id` int(11) NOT NULL AUTO_INCREMENT,"
            + " `nhom` tinyint(4) NOT NULL DEFAULT 0,"
            + " `vat_pham` varchar(255) NOT NULL DEFAULT '',"
            + " `chi_so` varchar(255) NOT NULL DEFAULT '',"
            + " `trong_so` int(11) NOT NULL DEFAULT 1,"
            + " `sl_min` int(11) NOT NULL DEFAULT 1,"
            + " `sl_max` int(11) NOT NULL DEFAULT 1,"
            + " `hsd_min` int(11) NOT NULL DEFAULT 0,"
            + " `hsd_max` int(11) NOT NULL DEFAULT 0,"
            + " `ti_le_vv` int(11) NOT NULL DEFAULT 0,"
            + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
            + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
            + " PRIMARY KEY (`id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTao;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (VongQuayDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                // Hai cot them sau — bang da co tu ban truoc thi va them vao.
                themCot("ALTER TABLE vong_quay_qua ADD COLUMN IF NOT EXISTS"
                        + " sl_min int(11) NOT NULL DEFAULT 1");
                themCot("ALTER TABLE vong_quay_qua ADD COLUMN IF NOT EXISTS"
                        + " sl_max int(11) NOT NULL DEFAULT 1");
                themCot("ALTER TABLE vong_quay_qua ADD COLUMN IF NOT EXISTS"
                        + " hsd_min int(11) NOT NULL DEFAULT 0");
                themCot("ALTER TABLE vong_quay_qua ADD COLUMN IF NOT EXISTS"
                        + " hsd_max int(11) NOT NULL DEFAULT 0");
                themCot("ALTER TABLE vong_quay_qua ADD COLUMN IF NOT EXISTS"
                        + " ti_le_vv int(11) NOT NULL DEFAULT 0");
                daTao = true;
            } catch (Exception ex) {
                Logger.logException(VongQuayDAO.class, ex,
                        "Không tạo được bảng vong_quay_qua");
            }
        }
    }


    /** Chạy một câu ALTER, bỏ qua nếu cột đã có. */
    private static void themCot(String sql) {
        try {
            ConnectDB.executeUpdate(sql);
        } catch (Exception boQua) {
            // Cot da co roi -> bo qua.
        }
    }

    /**
     * Đọc kho quà, tự gieo danh sách gốc nếu bảng còn trống.
     *
     * <p>Gieo <b>khi bảng trống</b> chứ không phải "chưa từng gieo": vòng quay
     * mà không có món nào thì mở ra chẳng cho gì cả, tệ hơn hẳn việc danh sách
     * cũ mọc lại. Quản trị viên muốn tắt hết thì tắt từng dòng, đừng xoá.</p>
     */
    public static List<Qua> danhSach() {
        damBaoBang();
        List<Qua> ds = doc();
        if (ds.isEmpty()) {
            for (Qua q : khoGoc()) {
                luu(q);
            }
            ds = doc();
            Logger.success("CONFIG", "Đã gieo " + ds.size()
                    + " dòng quà vòng quay Thượng Đế");
        }
        return ds;
    }

    private static List<Qua> doc() {
        List<Qua> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id, nhom, vat_pham, chi_so,"
                    + " trong_so, sl_min, sl_max, hsd_min, hsd_max, ti_le_vv,"
                    + " bat, ghi_chu FROM vong_quay_qua"
                    + " ORDER BY nhom, id");
            while (rs.next()) {
                Qua q = new Qua();
                q.id = rs.getInt("id");
                q.nhom = rs.getInt("nhom");
                q.vatPham = rs.getString("vat_pham");
                q.chiSo = rs.getString("chi_so");
                q.trongSo = rs.getInt("trong_so");
                q.soLuongMin = rs.getInt("sl_min");
                q.soLuongMax = rs.getInt("sl_max");
                q.hsdMin = rs.getInt("hsd_min");
                q.hsdMax = rs.getInt("hsd_max");
                q.tiLeVinhVien = rs.getInt("ti_le_vv");
                q.bat = rs.getBoolean("bat");
                q.ghiChu = rs.getString("ghi_chu");
                ds.add(q);
            }
        } catch (Exception ex) {
            Logger.logException(VongQuayDAO.class, ex, "Lỗi đọc kho quà vòng quay");
        } finally {
            dong(rs);
        }
        return ds;
    }

    public static String luu(Qua q) {
        damBaoBang();
        if (q == null) {
            return "Không có dữ liệu.";
        }
        if (q.vatPham == null || q.vatPham.trim().isEmpty()) {
            return "Phải có ít nhất một id vật phẩm.";
        }
        if (docDanhSachSo(q.vatPham).isEmpty()) {
            return "Ô vật phẩm phải là các số ngăn nhau bằng dấu phẩy.";
        }
        if (q.trongSo < 1) {
            return "Trọng số phải từ 1 trở lên.";
        }
        try {
            if (q.id > 0) {
                ConnectDB.executeUpdate("UPDATE vong_quay_qua SET nhom = ?,"
                        + " vat_pham = ?, chi_so = ?, trong_so = ?, sl_min = ?,"
                        + " sl_max = ?, hsd_min = ?, hsd_max = ?, ti_le_vv = ?,"
                        + " bat = ?, ghi_chu = ? WHERE id = ?",
                        q.nhom, q.vatPham.trim(), q.chiSo == null ? "" : q.chiSo.trim(),
                        q.trongSo, q.soLuongMin, q.soLuongMax,
                        q.hsdMin, q.hsdMax, q.tiLeVinhVien,
                        q.bat ? 1 : 0, q.ghiChu == null ? "" : q.ghiChu, q.id);
            } else {
                ConnectDB.executeUpdate("INSERT INTO vong_quay_qua (nhom,"
                        + " vat_pham, chi_so, trong_so, sl_min, sl_max, hsd_min,"
                        + " hsd_max, ti_le_vv, bat, ghi_chu)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        q.nhom, q.vatPham.trim(), q.chiSo == null ? "" : q.chiSo.trim(),
                        q.trongSo, q.soLuongMin, q.soLuongMax,
                        q.hsdMin, q.hsdMax, q.tiLeVinhVien,
                        q.bat ? 1 : 0, q.ghiChu == null ? "" : q.ghiChu);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(VongQuayDAO.class, ex, "Lỗi lưu quà vòng quay");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        damBaoBang();
        try {
            int n = ConnectDB.executeUpdate("DELETE FROM vong_quay_qua WHERE id = ?", id);
            return n == 0 ? "Không có dòng quà id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(VongQuayDAO.class, ex, "Lỗi xoá quà vòng quay");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    // =====================================================================
    //  Bốc quà
    // =====================================================================

    /**
     * Bốc một món cho vòng quay.
     *
     * @return {@code null} nếu kho rỗng hoặc tắt hết — chỗ gọi phải tự lo, đừng
     *         để người chơi mất lượt quay mà không nhận gì
     */
    public static Item boc(boolean vip) {
        int nhom = vip ? NHOM_VIP : NHOM_THUONG;
        List<Qua> duocBoc = new ArrayList<>();
        int tong = 0;
        for (Qua q : danhSach()) {
            if (q.bat && q.nhom == nhom && q.trongSo > 0) {
                duocBoc.add(q);
                tong += q.trongSo;
            }
        }
        if (tong <= 0) {
            return null;
        }
        int moc = Util.nextInt(tong);
        Qua chon = duocBoc.get(duocBoc.size() - 1);
        for (Qua q : duocBoc) {
            moc -= q.trongSo;
            if (moc < 0) {
                chon = q;
                break;
            }
        }
        return dungMon(chon);
    }

    /** Dựng vật phẩm thật từ một dòng quà. */
    private static Item dungMon(Qua q) {
        List<Integer> ids = docDanhSachSo(q.vatPham);
        if (ids.isEmpty()) {
            return null;
        }
        int id = ids.get(Util.nextInt(ids.size()));
        Item it;
        try {
            it = ItemService.gI().createNewItem((short) id);
        } catch (Exception ex) {
            Logger.error("Vòng quay: không tạo được vật phẩm id " + id + "\n");
            return null;
        }
        if (it == null) {
            return null;
        }
        // Moi dong chi so co mot KHOANG tri so. Hai dau bang nhau la tri co dinh.
        for (int[] cs : docChiSo(q.chiSo)) {
            int min = cs[1];
            int max = Math.max(min, cs[2]);
            it.itemOptions.add(new ItemOption(cs[0],
                    min == max ? min : Util.nextInt(min, max + 1)));
        }
        // So luong tu..toi. Hai o bang nhau la so co dinh; de 1..1 la mot mon.
        int slMin = Math.max(1, q.soLuongMin);
        int slMax = Math.max(slMin, q.soLuongMax);
        it.quantity = (slMin == slMax) ? slMin : Util.nextInt(slMin, slMax + 1);
        themHanDung(it, q);
        return it;
    }

    /**
     * Gắn hạn dùng, trừ khi lần này bốc trúng bản vĩnh viễn.
     *
     * <p>Hai con số này trước đây nằm trong mã: một đoạn cho món 613, một đoạn
     * cho nhóm pet, mỗi đoạn một khoảng ngày và một tỉ lệ vĩnh viễn viết cứng.
     * Nay là ba ô trên panel, đặt riêng cho từng dòng quà.</p>
     *
     * <p>{@code hsdMax} bằng 0 nghĩa là món này <b>không có hạn</b> — không gắn
     * gì cả, kể cả khi tỉ lệ vĩnh viễn để 0.</p>
     */
    private static void themHanDung(Item it, Qua q) {
        if (q.hsdMax <= 0) {
            return;
        }
        if (q.tiLeVinhVien > 0 && Util.isTrue(Math.min(q.tiLeVinhVien, 100), 100)) {
            return;
        }
        int min = Math.max(1, q.hsdMin);
        int max = Math.max(min, q.hsdMax);
        it.itemOptions.add(new ItemOption(93,
                min == max ? min : Util.nextInt(min, max + 1)));
    }

    // =====================================================================
    //  Đọc ô chữ
    // =====================================================================

    /** Đọc "1,2,3" thành danh sách số; bỏ qua phần không phải số. */
    public static List<Integer> docDanhSachSo(String s) {
        List<Integer> ra = new ArrayList<>();
        if (s == null) {
            return ra;
        }
        for (String p : s.split(",")) {
            String t = p.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                ra.add(Integer.parseInt(t));
            } catch (NumberFormatException boQua) {
                // Phan go sai thi bo qua, dung lam hong ca dong.
            }
        }
        return ra;
    }

    /**
     * Đọc chuỗi chỉ số thành các bộ ba {id, trị số từ, trị số tới}.
     *
     * <p>Dạng đầy đủ là {@code "50:20:30"} — chỉ số 50, bốc trong khoảng 20 đến
     * 30. Dạng cũ {@code "30:0"} vẫn đọc được: thiếu vế thứ ba thì tới bằng từ,
     * tức trị số cố định.</p>
     */
    public static List<int[]> docChiSo(String s) {
        List<int[]> ra = new ArrayList<>();
        if (s == null) {
            return ra;
        }
        for (String p : s.split(",")) {
            String t = p.trim();
            if (t.isEmpty()) {
                continue;
            }
            String[] phan = t.split(":");
            try {
                int id = Integer.parseInt(phan[0].trim());
                int tu = phan.length > 1 ? Integer.parseInt(phan[1].trim()) : 0;
                int toi = phan.length > 2 ? Integer.parseInt(phan[2].trim()) : tu;
                ra.add(new int[]{id, tu, Math.max(tu, toi)});
            } catch (Exception boQua) {
                // Nhu tren.
            }
        }
        return ra;
    }

    // =====================================================================
    //  Kho gốc
    // =====================================================================

    private static Qua q(int nhom, String vatPham, String chiSo, int trongSo,
            String ghiChu) {
        Qua x = new Qua();
        x.nhom = nhom;
        x.vatPham = vatPham;
        x.chiSo = chiSo;
        x.trongSo = trongSo;
        x.ghiChu = ghiChu;
        return x;
    }

    private static Qua qHsd(int nhom, String vatPham, String chiSo, int trongSo,
            int hsdMin, int hsdMax, int tiLeVinhVien, String ghiChu) {
        Qua x = q(nhom, vatPham, chiSo, trongSo, ghiChu);
        x.hsdMin = hsdMin;
        x.hsdMax = hsdMax;
        x.tiLeVinhVien = tiLeVinhVien;
        return x;
    }

    /**
     * Đúng danh sách quà đang chạy trước khi có bảng này.
     *
     * <p>Chép từng dòng từ {@code LuckyRound.buildItemPool()}, kể cả trọng số —
     * mấy mục gọi {@code addWeighted(..., n)} thành trọng số {@code n}, mục
     * thường thành trọng số 1.</p>
     *
     * <p>Hai mục vốn phải nhờ mã nguồn tính (món 613 và nhóm pet có hạn dùng)
     * nay <b>viết thẳng ra dữ liệu</b>: khoảng trị số của từng chỉ số, khoảng
     * ngày hạn dùng, và tỉ lệ ra bản vĩnh viễn. Nhờ thế không còn ô nào trong
     * bảng mà panel không sửa được.</p>
     */
    private static List<Qua> khoGoc() {
        List<Qua> ds = new ArrayList<>();

        // Ba dong dung chung cho ca hai vong quay.
        for (int nhom : new int[]{NHOM_THUONG, NHOM_VIP}) {
            ds.add(q(nhom, "18", "", 1, ""));
            ds.add(q(nhom, "1143", "30:0", 1, ""));
            ds.add(q(nhom, "1173", "30:0", 1, ""));
        }

        // ---- vòng quay VIP ----
        ds.add(q(NHOM_VIP, "1150,1151,1152,1153,1154", "86:0", 1, ""));
        ds.add(q(NHOM_VIP, "1404,1405,1406,1407,1409,1410,1411,1412,1413", "87:0", 1, ""));
        ds.add(q(NHOM_VIP, "1408", "87:0", 1, ""));
        ds.add(q(NHOM_VIP, "2062", "87:0", 1, ""));
        ds.add(q(NHOM_VIP, "2069", "87:0", 1, ""));
        ds.add(q(NHOM_VIP, "840,841,842,859,956", "87:0,30:0", 1, ""));
        ds.add(q(NHOM_VIP, "1517,1518", "87:0", 1, ""));

        // ---- vòng quay thường ----
        ds.add(q(NHOM_THUONG, "18,19,20", "", 1, ""));
        ds.add(q(NHOM_THUONG, "381,382,383,384,385", "86:0", 1, ""));
        // Moi cap {vat pham, chi so} thanh mot dong rieng, trong so 1 — gop lai
        // van dung ti le cu vi chung von duoc boc deu nhau.
        ds.add(q(NHOM_THUONG, "220", "68:0", 1, ""));
        ds.add(q(NHOM_THUONG, "221", "70:0", 1, ""));
        ds.add(q(NHOM_THUONG, "222", "69:0", 1, ""));
        ds.add(q(NHOM_THUONG, "223", "71:0", 1, ""));
        ds.add(q(NHOM_THUONG, "224", "67:0", 1, ""));
        ds.add(q(NHOM_THUONG, "441", "95:5", 1, ""));
        ds.add(q(NHOM_THUONG, "442", "96:5", 1, ""));
        ds.add(q(NHOM_THUONG, "443", "97:5", 1, ""));
        ds.add(q(NHOM_THUONG, "444", "98:3", 1, ""));
        ds.add(q(NHOM_THUONG, "445", "99:3", 1, ""));
        ds.add(q(NHOM_THUONG, "446", "100:5", 1, ""));
        ds.add(q(NHOM_THUONG, "447", "101:5", 1, ""));
        ds.add(q(NHOM_THUONG, "2063,2064,2065,2066,2067,2068", "87:0", 1, ""));
        ds.add(q(NHOM_THUONG, "1150,1151,1152,1153,1154", "", 3, ""));
        ds.add(q(NHOM_THUONG, "2431,2432", "", 2, ""));
        // Hai dong duoi day truoc kia nam trong ma nguon.
        ds.add(qHsd(NHOM_THUONG, "613", "30:0,50:20:30,77:20:30,103:20:30,101:30:70",
                2, 1, 5, 5, "Trước ở mã nguồn (createItem613)"));
        ds.add(qHsd(NHOM_THUONG, "1947,1654,1810,1107,1633",
                "30:0,50:8:14,77:8:14,103:8:14",
                3, 1, 5, 5, "Trước ở mã nguồn (createOption93GroupItem)"));

        return ds;
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
