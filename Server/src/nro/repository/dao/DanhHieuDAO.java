package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.badges.BagesTemplate;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Client;
import nro.server.Manager;

/**
 * Danh hiệu — đọc/ghi bảng {@code data_badges} và cấp cho người chơi.
 *
 * <h2>Bảng lưu gì</h2>
 *
 * <pre>
 *   id        khoá tự tăng
 *   idEffect  id hiệu ứng, ĐÂY là con số dùng ở mọi nơi khác
 *   idItem    vật phẩm dùng để mua/đổi danh hiệu ở cửa hàng NPC
 *   NAME      tên hiện trong game
 *   Options   JSON các dòng chỉ số, ví dụ [{"param":15,"id":50}]
 * </pre>
 *
 * <h2>Cấp cho người chơi</h2>
 *
 * <p>Người chơi đang online thì sửa thẳng trong bộ nhớ — có hiệu lực ngay và
 * bản lưu tự ghi lúc thoát. Đang offline thì sửa cột {@code player.dataBadges}
 * trong CSDL. <b>Không</b> làm cả hai: người đang online mà ghi thẳng CSDL thì
 * lúc họ thoát, bản trong bộ nhớ ghi đè mất.</p>
 */
public final class DanhHieuDAO {

    private DanhHieuDAO() {
    }

    /** Một danh hiệu trong bảng. */
    public static final class DanhHieu {

        public int id;
        public int idEffect;
        public int idItem;
        public String ten = "";
        public String options = "[]";

        /**
         * Mô tả các dòng chỉ số cho người đọc panel.
         *
         * <p><b>Không</b> dựng {@link ItemOption} ở đây. Hàm dựng của lớp đó tra
         * {@code Manager.ITEM_OPTION_TEMPLATES} theo chỉ số mảng, mà bảng ấy còn
         * <b>rỗng lúc panel được tạo</b> — panel dựng trước khi máy chủ nạp xong
         * dữ liệu, nên mỗi dòng chỉ số ném một
         * {@code IndexOutOfBoundsException}. Đọc tên thẳng từ CSDL thay vì đi
         * qua bộ nhớ của game.</p>
         */
        public String moTaChiSo() {
            List<int[]> ds = docCapSo(options);
            if (ds.isEmpty()) {
                return "(không có)";
            }
            StringBuilder sb = new StringBuilder();
            for (int[] d : ds) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                String ten = tenChiSo(d[0]);
                if (ten == null || ten.isEmpty()) {
                    sb.append('#').append(d[0]).append(' ').append(d[1]);
                } else if (ten.contains("#")) {
                    sb.append(ten.replace("#", String.valueOf(d[1])));
                } else {
                    sb.append(ten).append(' ').append(d[1]);
                }
            }
            return sb.toString();
        }
    }

    /**
     * Đọc chuỗi JSON options thành các cặp {@code {id, param}}.
     *
     * <p>Không đụng tới {@code Manager} nên gọi được cả khi máy chủ chưa nạp
     * xong dữ liệu — panel dựng trước lúc đó.</p>
     */
    public static List<int[]> docCapSo(String json) {
        List<int[]> ds = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return ds;
        }
        try {
            org.json.simple.JSONArray arr =
                    (org.json.simple.JSONArray) org.json.simple.JSONValue.parse(json);
            if (arr == null) {
                return ds;
            }
            for (Object o : arr) {
                org.json.simple.JSONObject j = (org.json.simple.JSONObject) o;
                ds.add(new int[]{
                    Integer.parseInt(String.valueOf(j.get("id"))),
                    Integer.parseInt(String.valueOf(j.get("param")))});
            }
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex,
                    "Lỗi đọc chỉ số danh hiệu: " + json);
        }
        return ds;
    }

    /** Tên một loại chỉ số, đọc thẳng CSDL. Không có thì trả {@code null}. */
    public static String tenChiSo(int id) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT NAME FROM item_option_template WHERE id = ?", id);
            return rs.next() ? rs.getString("NAME") : null;
        } catch (Exception ex) {
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Đọc chuỗi JSON options thành danh sách dòng chỉ số. */
    public static List<ItemOption> docOptions(String json) {
        List<ItemOption> ds = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return ds;
        }
        try {
            org.json.simple.JSONArray arr =
                    (org.json.simple.JSONArray) org.json.simple.JSONValue.parse(json);
            if (arr == null) {
                return ds;
            }
            for (Object o : arr) {
                org.json.simple.JSONObject j = (org.json.simple.JSONObject) o;
                int idOpt = Integer.parseInt(String.valueOf(j.get("id")));
                int param = Integer.parseInt(String.valueOf(j.get("param")));
                ds.add(new ItemOption(idOpt, param));
            }
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex,
                    "Lỗi đọc chỉ số danh hiệu: " + json);
        }
        return ds;
    }

    /** Dựng lại chuỗi JSON options từ danh sách. */
    public static String vietOptions(List<int[]> dong) {
        StringBuilder sb = new StringBuilder("[");
        for (int[] d : dong) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append("{\"param\":").append(d[1]).append(",\"id\":").append(d[0]).append('}');
        }
        return sb.append(']').toString();
    }

    public static List<DanhHieu> danhSach() {
        List<DanhHieu> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, idEffect, idItem, NAME, Options FROM data_badges"
                    + " ORDER BY idEffect");
            while (rs.next()) {
                DanhHieu d = new DanhHieu();
                d.id = rs.getInt("id");
                d.idEffect = rs.getInt("idEffect");
                d.idItem = rs.getInt("idItem");
                d.ten = rs.getString("NAME");
                d.options = rs.getString("Options");
                if (d.ten == null) {
                    d.ten = "";
                }
                if (d.options == null || d.options.trim().isEmpty()) {
                    d.options = "[]";
                }
                ds.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex, "Lỗi đọc bảng danh hiệu");
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

    /** @return {@code null} nếu xong, ngược lại là lý do */
    public static String them(DanhHieu d) {
        String loi = kiemTra(d, true);
        if (loi != null) {
            return loi;
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO data_badges (idEffect, idItem, NAME, Options)"
                    + " VALUES (?, ?, ?, ?)",
                    d.idEffect, d.idItem, d.ten, d.options);
            napLaiBoNho();
            return null;
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex, "Lỗi thêm danh hiệu");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** @return {@code null} nếu xong, ngược lại là lý do */
    public static String sua(DanhHieu d) {
        String loi = kiemTra(d, false);
        if (loi != null) {
            return loi;
        }
        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE data_badges SET idEffect = ?, idItem = ?, NAME = ?,"
                    + " Options = ? WHERE id = ?",
                    d.idEffect, d.idItem, d.ten, d.options, d.id);
            if (n == 0) {
                return "Không có danh hiệu id " + d.id + ".";
            }
            napLaiBoNho();
            return null;
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex, "Lỗi sửa danh hiệu");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Xoá một danh hiệu.
     *
     * <p>Chặn nếu còn người chơi đang giữ — xoá bản mẫu mà người chơi còn dòng
     * trỏ tới nó thì chỉ số cộng vào hư không và tên hiện ra rỗng.</p>
     */
    public static String xoa(int id) {
        try {
            DanhHieu d = theoId(id);
            if (d == null) {
                return "Không có danh hiệu id " + id + ".";
            }
            int n = soNguoiDangGiu(d.idEffect);
            if (n > 0) {
                return "Còn " + n + " nhân vật đang giữ danh hiệu này. "
                        + "Thu hồi của họ trước đã.";
            }
            ConnectDB.executeUpdate("DELETE FROM data_badges WHERE id = ?", id);
            napLaiBoNho();
            return null;
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex, "Lỗi xoá danh hiệu");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static DanhHieu theoId(int id) {
        for (DanhHieu d : danhSach()) {
            if (d.id == id) {
                return d;
            }
        }
        return null;
    }

    /** Số nhân vật đang có danh hiệu {@code idEffect}, tính cả người offline. */
    public static int soNguoiDangGiu(int idEffect) {
        CrisResultSet rs = null;
        try {
            // Moi o la {"idBadGes":"<id>",...} nen tim theo dung mau do.
            rs = ConnectDB.executeQuery(
                    "SELECT COUNT(*) n FROM player WHERE dataBadges LIKE ?",
                    "%\"idBadGes\":\"" + idEffect + "\"%");
            int n = rs.next() ? rs.getInt("n") : 0;
            return n;
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex,
                    "Lỗi đếm người giữ danh hiệu " + idEffect);
            return 0;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Nạp lại bản mẫu trong bộ nhớ để thay đổi có hiệu lực ngay. */
    public static void napLaiBoNho() {
        try {
            Manager.BAGES_TEMPLATES.clear();
            for (DanhHieu d : danhSach()) {
                BagesTemplate t = new BagesTemplate();
                t.id = d.id;
                t.idEffect = d.idEffect;
                t.idItem = d.idItem;
                t.NAME = d.ten;
                t.options = docOptions(d.options);
                Manager.BAGES_TEMPLATES.add(t);
            }
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex,
                    "Lỗi nạp lại danh hiệu vào bộ nhớ");
        }
    }

    private static String kiemTra(DanhHieu d, boolean them) {
        if (d == null) {
            return "Thiếu dữ liệu.";
        }
        if (d.ten == null || d.ten.trim().isEmpty()) {
            return "Tên danh hiệu không được để trống.";
        }
        d.ten = d.ten.trim();
        if (d.idEffect <= 0) {
            return "Id hiệu ứng phải lớn hơn 0.";
        }
        for (DanhHieu k : danhSach()) {
            if (k.idEffect == d.idEffect && (them || k.id != d.id)) {
                return "Id hiệu ứng " + d.idEffect + " đã có ở danh hiệu \""
                        + k.ten + "\". Mỗi danh hiệu phải một id riêng.";
            }
        }
        if (d.options == null || d.options.trim().isEmpty()) {
            d.options = "[]";
        }
        return null;
    }

    // =====================================================================
    //  Cấp / thu hồi cho một nhân vật
    // =====================================================================

    /**
     * Cấp danh hiệu cho một nhân vật theo tên.
     *
     * @param soNgay {@code <= 0} nghĩa là vĩnh viễn
     * @return {@code null} nếu xong, ngược lại là lý do
     */
    public static String capChoNhanVat(String tenNhanVat, int idEffect,
            int soNgay, boolean deoLuon) {
        if (tenNhanVat == null || tenNhanVat.trim().isEmpty()) {
            return "Chưa nhập tên nhân vật.";
        }
        tenNhanVat = tenNhanVat.trim();

        Player pl = timOnline(tenNhanVat);
        if (pl != null) {
            nro.service.badges.BadgesService.cap(pl, idEffect, soNgay, deoLuon);
            return null;
        }
        return capChoNguoiOffline(tenNhanVat, idEffect, soNgay, deoLuon);
    }

    /** Thu hồi danh hiệu của một nhân vật. */
    public static String thuHoiCuaNhanVat(String tenNhanVat, int idEffect) {
        if (tenNhanVat == null || tenNhanVat.trim().isEmpty()) {
            return "Chưa nhập tên nhân vật.";
        }
        tenNhanVat = tenNhanVat.trim();

        Player pl = timOnline(tenNhanVat);
        if (pl != null) {
            return nro.service.badges.BadgesService.thuHoi(pl, idEffect)
                    ? null : "Nhân vật không có danh hiệu đó.";
        }
        String cu = docDataBadges(tenNhanVat);
        if (cu == null) {
            return "Không có nhân vật tên \"" + tenNhanVat + "\".";
        }
        List<long[]> ds = tachDataBadges(cu);
        int truoc = ds.size();
        ds.removeIf(x -> x[0] == idEffect);
        if (ds.size() == truoc) {
            return "Nhân vật không có danh hiệu đó.";
        }
        return ghiDataBadges(tenNhanVat, ds);
    }

    private static Player timOnline(String ten) {
        try {
            for (Player p : Client.gI().getPlayersSnapshot()) {
                if (p != null && ten.equalsIgnoreCase(p.name)) {
                    return p;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String capChoNguoiOffline(String ten, int idEffect,
            int soNgay, boolean deoLuon) {
        String cu = docDataBadges(ten);
        if (cu == null) {
            return "Không có nhân vật tên \"" + ten + "\".";
        }
        List<long[]> ds = tachDataBadges(cu);
        long han = soNgay <= 0 ? -1
                : System.currentTimeMillis() + soNgay * 24L * 60L * 60L * 1000L;

        boolean thay = false;
        for (long[] x : ds) {
            if (x[0] == idEffect) {
                if (han == -1 || x[1] == -1) {
                    x[1] = -1;
                } else {
                    long conLai = Math.max(0, x[1] - System.currentTimeMillis());
                    x[1] = System.currentTimeMillis() + conLai
                            + soNgay * 24L * 60L * 60L * 1000L;
                }
                thay = true;
            }
            if (deoLuon) {
                x[2] = 0;
            }
        }
        if (!thay) {
            ds.add(new long[]{idEffect, han, deoLuon ? 1 : 0});
        } else if (deoLuon) {
            for (long[] x : ds) {
                x[2] = x[0] == idEffect ? 1 : 0;
            }
        }
        return ghiDataBadges(ten, ds);
    }

    private static String docDataBadges(String ten) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT dataBadges FROM player WHERE NAME = ?", ten);
            if (!rs.next()) {
                return null;
            }
            String s = rs.getString("dataBadges");
            return s == null ? "[]" : s;
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex,
                    "Lỗi đọc danh hiệu của " + ten);
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Mỗi phần tử: {idBadGes, timeofUseBadges, isUse}. */
    private static List<long[]> tachDataBadges(String json) {
        List<long[]> ds = new ArrayList<>();
        try {
            org.json.simple.JSONArray arr =
                    (org.json.simple.JSONArray) org.json.simple.JSONValue.parse(json);
            if (arr == null) {
                return ds;
            }
            for (Object o : arr) {
                org.json.simple.JSONObject j = o instanceof org.json.simple.JSONObject
                        ? (org.json.simple.JSONObject) o
                        : (org.json.simple.JSONObject)
                                org.json.simple.JSONValue.parse(String.valueOf(o));
                if (j == null) {
                    continue;
                }
                long id = Long.parseLong(String.valueOf(j.get("idBadGes")));
                long han = Long.parseLong(String.valueOf(j.get("timeofUseBadges")));
                boolean dung = Boolean.parseBoolean(String.valueOf(j.get("isUse")));
                ds.add(new long[]{id, han, dung ? 1 : 0});
            }
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex,
                    "Lỗi tách danh hiệu: " + json);
        }
        return ds;
    }

    private static String ghiDataBadges(String ten, List<long[]> ds) {
        StringBuilder sb = new StringBuilder("[");
        for (long[] x : ds) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            // Dung DUNG dinh dang cua BadgesData.toString(): moi truong boc
            // trong dau nhay, ke ca so.
            sb.append("{\"idBadGes\":\"").append(x[0])
                    .append("\",\"timeofUseBadges\":\"").append(x[1])
                    .append("\",\"isUse\":\"").append(x[2] == 1).append("\"}");
        }
        sb.append(']');
        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE player SET dataBadges = ? WHERE NAME = ?",
                    sb.toString(), ten);
            return n == 0 ? "Không có nhân vật tên \"" + ten + "\"." : null;
        } catch (Exception ex) {
            Logger.logException(DanhHieuDAO.class, ex,
                    "Lỗi ghi danh hiệu cho " + ten);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }
}
