package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Quản lý kỹ năng ({@code skill_template}) và nội tại ({@code intrinsic}) theo
 * từng hành tinh.
 *
 * <p><b>Không</b> mắc bẫy "id = chỉ số mảng" như bảng {@code part} hay
 * {@code item_template}: {@code NClass.getSkillTemplate(id)} dò tuyến tính theo
 * id, và {@code DataGame.updateSkill()} ghi thẳng {@code skillTemp.id} xuống
 * client. Nên id kỹ năng thưa cũng không sao.</p>
 *
 * <p>Nhưng id vẫn <b>phải nằm trong 0..127</b> — client đọc nó bằng
 * {@code writeByte}.</p>
 */
public final class KyNangDAO {

    /** Số hành tinh: 0 Trái Đất, 1 Namếc, 2 Xayda. */
    public static final String[] HANH_TINH = {"Trái Đất", "Namếc", "Xayda"};

    private KyNangDAO() {
    }

    /** Một dòng kỹ năng. */
    public static final class KyNang {

        public int hanhTinh;
        public int id;
        public String ten;
        public int maxPoint;
        public int manaUseType;
        public int type;
        public int iconId;
        public String damInfo;
        public int slot;
        public String capJson;

        /** Số cấp đọc được trong cột {@code skills}. */
        public int soCap;
    }

    /** Một dòng nội tại. */
    public static final class NoiTai {

        public int id;
        public String ten;
        public int from1;
        public int to1;
        public int from2;
        public int to2;
        public int icon;
        public int gender;
    }

    public static String tenHanhTinh(int g) {
        return g >= 0 && g < HANH_TINH.length ? HANH_TINH[g] : "Tất cả";
    }

    // =====================================================================
    //  Kỹ năng
    // =====================================================================

    public static List<KyNang> kyNangCua(int hanhTinh) {
        List<KyNang> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT nclass_id, id, NAME, max_point, mana_use_type, TYPE,"
                    + " icon_id, dam_info, slot, skills FROM skill_template"
                    + " WHERE nclass_id = ? ORDER BY slot", hanhTinh);
            while (rs.next()) {
                KyNang k = new KyNang();
                k.hanhTinh = rs.getInt("nclass_id");
                k.id = rs.getInt("id");
                k.ten = rs.getString("NAME");
                k.maxPoint = rs.getInt("max_point");
                k.manaUseType = rs.getInt("mana_use_type");
                k.type = rs.getInt("TYPE");
                k.iconId = rs.getInt("icon_id");
                k.damInfo = rs.getString("dam_info");
                k.slot = rs.getInt("slot");
                k.capJson = rs.getString("skills");
                k.soCap = demCap(k.capJson);
                ra.add(k);
            }
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex,
                    "Lỗi đọc kỹ năng của hành tinh " + hanhTinh);
        } finally {
            dispose(rs);
        }
        return ra;
    }

    /**
     * Đếm số cấp trong cột {@code skills}.
     *
     * <p>Cột này là JSON bị bọc nhiều lớp nháy — {@code Manager} phải gỡ bằng
     * bốn phép {@code replaceAll} mới đọc được. Ở đây chỉ cần đếm nên đếm số
     * dấu mở ngoặc nhọn cho chắc, không dựng lại cả bộ phân tích.</p>
     */
    public static int demCap(String json) {
        if (json == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < json.length(); i++) {
            if (json.charAt(i) == '{') {
                n++;
            }
        }
        return n;
    }

    /** Id kỹ năng còn trống trong một hành tinh, hoặc {@code -1}. */
    public static int idTrong(int hanhTinh) {
        boolean[] dung = new boolean[128];
        CrisResultSet rs = null;
        try {
            // Chiem cho tren TOAN BO hanh tinh: nguoi choi doi hanh tinh thi
            // danh sach ky nang doi theo, trung id giua hai hanh tinh la doc
            // nham chieu cua nhau.
            rs = ConnectDB.executeQuery("SELECT DISTINCT id FROM skill_template");
            while (rs.next()) {
                int i = rs.getInt("id");
                if (i >= 0 && i < 128) {
                    dung[i] = true;
                }
            }
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex, "Lỗi tìm id kỹ năng trống");
            return -1;
        } finally {
            dispose(rs);
        }
        for (int i = 0; i < 128; i++) {
            if (!dung[i]) {
                return i;
            }
        }
        return -1;
    }

    /** Slot còn trống của một hành tinh (đứng cuối danh sách). */
    public static int slotTiepTheo(int hanhTinh) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT MAX(slot) m FROM skill_template WHERE nclass_id = ?",
                    hanhTinh);
            return (rs.next() ? rs.getInt("m") : -1) + 1;
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex, "Lỗi đọc slot kỹ năng");
            return 0;
        } finally {
            dispose(rs);
        }
    }

    /**
     * Thêm hoặc sửa một kỹ năng.
     *
     * @return {@code null} nếu xong, ngược lại là lý do
     */
    public static String luu(KyNang k) {
        if (k.ten == null || k.ten.trim().isEmpty()) {
            return "Chưa đặt tên kỹ năng.";
        }
        if (k.id < 0 || k.id > 127) {
            return "Id kỹ năng phải trong 0..127 — client đọc id này bằng "
                    + "writeByte.";
        }
        if (k.hanhTinh < 0 || k.hanhTinh > 2) {
            return "Hành tinh phải là 0 (Trái Đất), 1 (Namếc) hoặc 2 (Xayda).";
        }
        if (k.maxPoint < 1) {
            return "Số điểm tối đa phải từ 1 trở lên.";
        }
        int soCap = demCap(k.capJson);
        if (soCap == 0) {
            return "Cột các cấp đang rỗng — kỹ năng không có cấp nào thì học "
                    + "vào cũng không dùng được.";
        }
        // Chan ghi de lam LECH so cap voi Diem toi da. Da xay ra that: hop
        // thoai mo luc CSDL con 1 cap, bam Luu sau khi CSDL da co 7 cap ->
        // ghi de mat cap 2..7, con max_point = 7. Client doc bang cap theo so
        // nay, lech la bam ky nang khong co gi xay ra.
        // Ba ky nang cuoi cua moi hanh tinh (Super Kamejoko, Ma phong ba,
        // Cadic) VON co max_point 9 voi 10 cap — kieu co san, khong phai loi.
        // Nen chi chan khi lech NHIEU hon mot cap.
        if (Math.abs(soCap - k.maxPoint) > 1) {
            return "Số cấp trong bảng (" + soCap + ") lệch quá xa Điểm tối đa ("
                    + k.maxPoint + "). Client đọc bảng cấp theo số này; lệch"
                    + " là bấm kỹ năng chỉ nháy icon mà không dùng được. Đóng"
                    + " hộp thoại rồi mở lại nếu bảng đang là dữ liệu cũ.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO skill_template (nclass_id, id, NAME, max_point,"
                    + " mana_use_type, TYPE, icon_id, dam_info, slot, skills)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE NAME = VALUES(NAME),"
                    + " max_point = VALUES(max_point),"
                    + " mana_use_type = VALUES(mana_use_type),"
                    + " TYPE = VALUES(TYPE), icon_id = VALUES(icon_id),"
                    + " dam_info = VALUES(dam_info), slot = VALUES(slot),"
                    + " skills = VALUES(skills)",
                    k.hanhTinh, k.id, k.ten.trim(), k.maxPoint, k.manaUseType,
                    k.type, k.iconId, k.damInfo == null ? "" : k.damInfo,
                    k.slot, k.capJson);
            tangPhienBanKyNang();
            return null;
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex,
                    "Lỗi lưu kỹ năng " + k.hanhTinh + "/" + k.id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Số người chơi đang có kỹ năng này trong cột {@code player.skills}. */
    public static int soNguoiDangCo(int id) {
        try {
            // Moi o la ["<id>,<point>,<lastTime>,<curr>"] nen dau o la ["<id>,
            // hoac ,"<id>, — doi ky tu truoc dau nhay de khong dinh nham cac so
            // khac trong cung mot o.
            CrisResultSet rs = ConnectDB.executeQuery(
                    "SELECT COUNT(*) n FROM player"
                    + " WHERE skills LIKE ? OR skills LIKE ?",
                    "%[\"[" + id + ",%", "%,\"[" + id + ",%");
            int n = rs.next() ? rs.getInt("n") : -1;
            rs.dispose();
            return n;
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex,
                    "Lỗi đếm người có kỹ năng " + id);
            return -1;
        }
    }

    /**
     * Xoá một kỹ năng.
     *
     * <p>Từ chối nếu còn người chơi đang có — xoá đi thì ô kỹ năng của họ trỏ
     * vào một mẫu không còn tồn tại.</p>
     */
    public static String xoa(int hanhTinh, int id) {
        int nguoi = soNguoiDangCo(id);
        if (nguoi > 0) {
            return "Không xoá được kỹ năng " + id + " vì còn " + nguoi
                    + " người chơi đang có nó.\n\n"
                    + "Gỡ khỏi người chơi trước đã.";
        }
        if (nguoi < 0) {
            return "Không kiểm được người chơi đang có kỹ năng này — "
                    + "xem log máy chủ.";
        }
        try {
            int n = ConnectDB.executeUpdate(
                    "DELETE FROM skill_template WHERE nclass_id = ? AND id = ?",
                    hanhTinh, id);
            if (n == 0) {
                return "Không có kỹ năng " + id + " ở hành tinh "
                        + tenHanhTinh(hanhTinh) + ".";
            }
            tangPhienBanKyNang();
            return null;
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex,
                    "Lỗi xoá kỹ năng " + hanhTinh + "/" + id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Dựng sẵn cột {@code skills} cho một kỹ năng mới.
     *
     * <p>Giữ <b>đúng định dạng lạ</b> mà {@code Manager} đang đọc: cả mảng là
     * một chuỗi JSON, mỗi cấp lại là một chuỗi JSON lồng bên trong. Tự chế định
     * dạng khác thì máy chủ nạp lên là hỏng cả hành tinh.</p>
     */
    public static String capMacDinh(int soCap, long smBatDau) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < soCap; i++) {
            if (i > 0) {
                sb.append(',');
            }
            long sm = smBatDau * (i + 1);
            sb.append("\"{\"power_require\":").append(sm)
                    .append(",\"damage\":").append(100 + i * 20)
                    .append(",\"dx\":32,\"dy\":18,\"price\":0,\"max_fight\":1")
                    .append(",\"mana_use\":").append(i + 1)
                    .append(",\"cool_down\":500,\"id\":").append(i)
                    .append(",\"point\":").append(i + 1)
                    .append(",\"info\":\"\"}\"");
        }
        return sb.append(']').toString();
    }

    /**
     * Đọc cột {@code skills} ra từng dòng cho bảng trong panel.
     *
     * <p>Thứ tự cột khớp {@code SystemPanel.COT_CAP}: cấp, điểm, sức mạnh yêu
     * cầu, sát thương, KI dùng, hồi chiêu, dx, dy, đánh tối đa, giá, ghi chú.</p>
     *
     * <p>Gỡ lớp nháy <b>y hệt</b> cách {@code Manager.loadDatabase()} gỡ — dùng
     * cách khác là đọc ra một đằng, máy chủ nạp một nẻo.</p>
     */
    public static List<Object[]> docCap(String json) {
        List<Object[]> ra = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return ra;
        }
        try {
            String sach = json
                    .replaceAll("\\[\"", "[")
                    .replaceAll("\"\\[", "[")
                    .replaceAll("\"\\]", "]")
                    .replaceAll("\\]\"", "]")
                    .replaceAll("\\}\",\"\\{", "},{");
            org.json.simple.JSONArray m =
                    (org.json.simple.JSONArray) org.json.simple.JSONValue.parse(sach);
            if (m == null) {
                return ra;
            }
            for (Object o : m) {
                org.json.simple.JSONObject d = o instanceof org.json.simple.JSONObject
                        ? (org.json.simple.JSONObject) o
                        : (org.json.simple.JSONObject)
                          org.json.simple.JSONValue.parse(String.valueOf(o));
                if (d == null) {
                    continue;
                }
                ra.add(new Object[]{
                    so(d, "id"), so(d, "point"), so(d, "power_require"),
                    so(d, "damage"), so(d, "mana_use"), so(d, "cool_down"),
                    so(d, "dx"), so(d, "dy"), so(d, "max_fight"), so(d, "price"),
                    d.get("info") == null ? "" : String.valueOf(d.get("info"))});
            }
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex, "Lỗi đọc các cấp kỹ năng");
        }
        return ra;
    }

    private static long so(org.json.simple.JSONObject d, String khoa) {
        try {
            return Long.parseLong(String.valueOf(d.get(khoa)).trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Kiểm bảng cấp trước khi ghi.
     *
     * @return {@code null} nếu đúng, ngược lại là câu báo lỗi
     */
    public static String kiemCap(List<Object[]> cap) {
        if (cap == null || cap.isEmpty()) {
            return "Kỹ năng phải có ít nhất một cấp.";
        }
        for (int r = 0; r < cap.size(); r++) {
            Object[] d = cap.get(r);
            if (d.length < 11) {
                return "Dòng " + (r + 1) + " thiếu cột.";
            }
            for (int x = 0; x < 10; x++) {
                try {
                    Long.parseLong(String.valueOf(d[x]).trim());
                } catch (NumberFormatException ex) {
                    return "Dòng " + (r + 1) + ", cột thứ " + (x + 1)
                            + " phải là số nguyên (đang là \""
                            + String.valueOf(d[x]) + "\").";
                }
            }
            long diem = Long.parseLong(String.valueOf(d[1]).trim());
            if (diem < 1) {
                return "Dòng " + (r + 1) + ": cột Điểm phải từ 1 trở lên.";
            }
        }
        return null;
    }

    /**
     * Ghi bảng cấp trở lại đúng khuôn JSON bọc nhiều lớp nháy mà Manager đọc.
     *
     * <p>Đây là chỗ nguy hiểm nhất của cả tính năng: sai một dấu nháy là máy chủ
     * nạp lên hỏng cả hành tinh. Khuôn phải khớp từng ký tự với
     * {@link #capMacDinh}.</p>
     */
    public static String ghiCap(List<Object[]> cap) {
        StringBuilder sb = new StringBuilder("[");
        for (int r = 0; r < cap.size(); r++) {
            Object[] d = cap.get(r);
            if (r > 0) {
                sb.append(',');
            }
            sb.append("\"{\"power_require\":").append(nguyen(d[2]))
                    .append(",\"damage\":").append(nguyen(d[3]))
                    .append(",\"dx\":").append(nguyen(d[6]))
                    .append(",\"dy\":").append(nguyen(d[7]))
                    .append(",\"price\":").append(nguyen(d[9]))
                    .append(",\"max_fight\":").append(nguyen(d[8]))
                    .append(",\"mana_use\":").append(nguyen(d[4]))
                    .append(",\"cool_down\":").append(nguyen(d[5]))
                    .append(",\"id\":").append(nguyen(d[0]))
                    .append(",\"point\":").append(nguyen(d[1]))
                    .append(",\"info\":\"").append(chuoiAnToan(d[10]))
                    .append("\"}\"");
        }
        return sb.append(']').toString();
    }

    private static long nguyen(Object o) {
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Bỏ ký tự phá vỡ chuỗi JSON trong ô ghi chú.
     *
     * <p>Cột này bị bọc hai lớp nháy nên một dấu nháy kép hay dấu chéo ngược do
     * người dùng gõ vào là đứt cả mảng.</p>
     */
    private static String chuoiAnToan(Object o) {
        if (o == null) {
            return "";
        }
        return String.valueOf(o).replace("\\", " ").replace("\"", " ")
                .replace("{", " ").replace("}", " ")
                .replace("[", " ").replace("]", " ").trim();
    }

    // =====================================================================
    //  Nội tại
    // =====================================================================

    /**
     * Nội tại của một hành tinh.
     *
     * @param hanhTinh 0/1/2, hoặc {@code -1} để lấy hết
     */
    public static List<NoiTai> noiTaiCua(int hanhTinh) {
        List<NoiTai> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = hanhTinh < 0
                    ? ConnectDB.executeQuery("SELECT * FROM intrinsic ORDER BY id")
                    : ConnectDB.executeQuery(
                            "SELECT * FROM intrinsic WHERE gender = ? ORDER BY id",
                            hanhTinh);
            while (rs.next()) {
                NoiTai t = new NoiTai();
                t.id = rs.getInt("id");
                t.ten = rs.getString("NAME");
                t.from1 = rs.getInt("param_from_1");
                t.to1 = rs.getInt("param_to_1");
                t.from2 = rs.getInt("param_from_2");
                t.to2 = rs.getInt("param_to_2");
                t.icon = rs.getInt("icon");
                t.gender = rs.getInt("gender");
                ra.add(t);
            }
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex, "Lỗi đọc nội tại");
        } finally {
            dispose(rs);
        }
        return ra;
    }

    public static int idNoiTaiTrong() {
        boolean[] dung = new boolean[128];
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM intrinsic");
            while (rs.next()) {
                int i = rs.getInt("id");
                if (i >= 0 && i < 128) {
                    dung[i] = true;
                }
            }
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex, "Lỗi tìm id nội tại trống");
            return -1;
        } finally {
            dispose(rs);
        }
        for (int i = 0; i < 128; i++) {
            if (!dung[i]) {
                return i;
            }
        }
        return -1;
    }

    /** Thêm hoặc sửa một nội tại. */
    public static String luuNoiTai(NoiTai t) {
        if (t.ten == null || t.ten.trim().isEmpty()) {
            return "Chưa đặt tên nội tại.";
        }
        if (t.id < 0 || t.id > 127) {
            return "Id nội tại phải trong 0..127 — Manager đọc bằng getByte.";
        }
        if (t.from1 > t.to1 || t.from2 > t.to2) {
            return "Khoảng tham số bị ngược: giá trị ĐẦU phải nhỏ hơn hoặc bằng "
                    + "giá trị CUỐI.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO intrinsic (id, NAME, param_from_1, param_to_1,"
                    + " param_from_2, param_to_2, icon, gender)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE NAME = VALUES(NAME),"
                    + " param_from_1 = VALUES(param_from_1),"
                    + " param_to_1 = VALUES(param_to_1),"
                    + " param_from_2 = VALUES(param_from_2),"
                    + " param_to_2 = VALUES(param_to_2),"
                    + " icon = VALUES(icon), gender = VALUES(gender)",
                    t.id, t.ten.trim(), t.from1, t.to1, t.from2, t.to2,
                    t.icon, t.gender);
            return null;
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex, "Lỗi lưu nội tại " + t.id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaNoiTai(int id) {
        try {
            int n = ConnectDB.executeUpdate("DELETE FROM intrinsic WHERE id = ?",
                    id);
            return n == 0 ? "Không có nội tại id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex, "Lỗi xoá nội tại " + id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Client phải tải lại bảng kỹ năng thì mới thấy thay đổi. */
    public static void tangPhienBanKyNang() {
        try {
            long v = ConfigDAO.num(ConfigDAO.VS_SKILL);
            ConfigDAO.set(ConfigDAO.VS_SKILL, String.valueOf((v + 1) % 128));
        } catch (Exception ex) {
            Logger.logException(KyNangDAO.class, ex,
                    "Lỗi tăng phiên bản kỹ năng");
        }
    }

    private static void dispose(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
                // dong roi thi thoi
            }
        }
    }
}
