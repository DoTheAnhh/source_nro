package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.template.ItemTemplate;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Manager;

/**
 * Xoá / vô hiệu một mẫu vật phẩm trong bảng {@code item_template}.
 *
 * <p><b>Vì sao không xoá bừa được:</b> {@code ItemService.getTemplate(id)} là
 * {@code Manager.ITEM_TEMPLATES.get(id)} — <b>id chính là chỉ số mảng</b>. Bảng
 * đang liên tục từ 0, nên xoá một dòng ở giữa sẽ kéo mọi id lớn hơn lùi một bậc:
 * mọi vật phẩm trong túi người chơi có id cao hơn lập tức biến thành món khác.
 * Cùng loại bẫy với bảng {@code part} và {@code item_option_template}.</p>
 *
 * <p>Nên chỉ <b>id lớn nhất</b> mới xoá thật được. Id ở giữa thì
 * {@link #voHieu(int)} giữ nguyên dòng (để chỉ số không xê dịch) nhưng làm nó
 * thành một ô rỗng vô hại.</p>
 */
public final class MauVatPhamDAO {

    /** Tên dùng cho mẫu đã bị vô hiệu — để nhìn danh mục là biết ngay. */
    public static final String TEN_DA_XOA = "(đã xoá)";

    private MauVatPhamDAO() {
    }

    /** Một chỗ đang tham chiếu tới mẫu vật phẩm. */
    public static final class ChoDung {

        public final String moTa;
        public final int soLuong;

        ChoDung(String moTa, int soLuong) {
            this.moTa = moTa;
            this.soLuong = soLuong;
        }
    }

    public static int idLonNhat() {
        try {
            CrisResultSet rs = ConnectDB.executeQuery(
                    "SELECT MAX(id) m FROM item_template");
            int m = rs.next() ? rs.getInt("m") : -1;
            rs.dispose();
            return m;
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex, "Lỗi đọc id lớn nhất");
            return -1;
        }
    }

    /**
     * Đếm số người chơi đang giữ mẫu này trong người, hành trang hoặc rương.
     *
     * <p>Mỗi ô lưu dạng {@code ["[<id>,<số lượng>,...]"}, còn chỉ số của món thì
     * nằm trong chuỗi lồng {@code \"[<id chỉ số>,<giá trị>]\"}. Tìm thô bằng
     * {@code "[<id>,} sẽ dính cả chỉ số, nên phải đòi ký tự đứng trước dấu nháy
     * là {@code [} hoặc {@code ,} — tức đúng đầu một ô.</p>
     */
    public static int soNguoiDangGiu(int id) {
        String[] cot = {"items_body", "items_bag", "items_box",
            "items_box_Collection", "items_box_lucky_round", "item_mails_box"};
        StringBuilder dk = new StringBuilder();
        List<Object> ts = new ArrayList<>();
        for (String c : cot) {
            if (dk.length() > 0) {
                dk.append(" OR ");
            }
            dk.append(c).append(" LIKE ? OR ").append(c).append(" LIKE ?");
            ts.add("%[\"[" + id + ",%");
            ts.add("%,\"[" + id + ",%");
        }
        try {
            CrisResultSet rs = ConnectDB.executeQuery(
                    "SELECT COUNT(*) n FROM player WHERE " + dk, ts.toArray());
            int n = rs.next() ? rs.getInt("n") : 0;
            rs.dispose();
            return n;
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex,
                    "Lỗi đếm người giữ vật phẩm " + id);
            return -1;
        }
    }


    /** Cac cot chua vat pham cua nhan vat. */
    private static final String[] COT_TUI = {"items_body", "items_bag", "items_box",
        "items_box_Collection", "items_box_lucky_round", "item_mails_box"};

    /**
     * Go han mot mau vat pham khoi tui / ruong / hom thu cua MOI nhan vat.
     *
     * <p>Moi o luu dang chuoi {@code "[<id>,<so luong>,<chi so>,<thoi gian>]"}.
     * Go mot mon KHONG duoc xoa phan tu khoi mang: so o phai giu nguyen, neu
     * khong tui nguoi choi bi thut ngan lai. Nen thay bang o rong
     * {@code "[-1,0,\"[]\",0]"} — dung cach game danh dau o trong.</p>
     *
     * <p><b>Nguoi dang online se bi ghi de:</b> tui cua ho nam trong bo nho va
     * se duoc tu luu de len CSDL. Chi goi ham nay khi da chac ho offline, hoac
     * bao ho vao lai sau do.</p>
     *
     * @return so nhan vat bi sua, hoac -1 neu loi
     */
    public static int goKhoiTuiNguoiChoi(int id) {
        int suaDuoc = 0;
        CrisResultSet rs = null;
        try {
            StringBuilder cols = new StringBuilder("id");
            for (String c : COT_TUI) {
                cols.append(", ").append(c);
            }
            rs = ConnectDB.executeQuery("SELECT " + cols + " FROM player");
            List<Object[]> canSua = new ArrayList<>();
            while (rs.next()) {
                int pid = rs.getInt("id");
                String[] moi = new String[COT_TUI.length];
                boolean doi = false;
                for (int i = 0; i < COT_TUI.length; i++) {
                    String cu = rs.getStringOrNull(COT_TUI[i]);
                    moi[i] = thayOTrong(cu, id);
                    if (moi[i] != null && !moi[i].equals(cu)) {
                        doi = true;
                    }
                }
                if (doi) {
                    Object[] hang = new Object[COT_TUI.length + 1];
                    System.arraycopy(moi, 0, hang, 0, COT_TUI.length);
                    hang[COT_TUI.length] = pid;
                    canSua.add(hang);
                }
            }
            rs.dispose();
            rs = null;

            StringBuilder set = new StringBuilder();
            for (String c : COT_TUI) {
                if (set.length() > 0) {
                    set.append(", ");
                }
                set.append(c).append(" = ?");
            }
            for (Object[] hang : canSua) {
                ConnectDB.executeUpdate(
                        "UPDATE player SET " + set + " WHERE id = ?", hang);
                suaDuoc++;
            }
            return suaDuoc;
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex,
                    "Lỗi gỡ vật phẩm " + id + " khỏi túi người chơi");
            return -1;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Thay moi o chua mau {@code id} bang o rong, giu nguyen SO LUONG o.
     *
     * <p>Chi khop dung dau mot o (sau dau {@code ["} hoac {@code ,"}) — tim tho
     * theo {@code "[<id>,} se dinh ca phan chi so nam trong chuoi long.</p>
     */
    private static String thayOTrong(String raw, int id) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        StringBuilder ra = new StringBuilder();
        int i = 0;
        String[] dau = {"[\"[" + id + ",", ",\"[" + id + ","};
        while (i < raw.length()) {
            int khop = -1;
            String mau = null;
            for (String d : dau) {
                if (raw.startsWith(d, i)) {
                    khop = i;
                    mau = d;
                    break;
                }
            }
            if (khop < 0) {
                ra.append(raw.charAt(i));
                i++;
                continue;
            }
            int ketThuc = raw.indexOf("\"", i + mau.length());
            if (ketThuc < 0) {
                ra.append(raw.substring(i));
                break;
            }
            ra.append(mau.charAt(0)).append("\"[-1,0,\\\"[]\\\",0]\"");
            i = ketThuc + 1;
        }
        return ra.toString();
    }

    /** Mọi chỗ trong CSDL đang trỏ tới mẫu này. Rỗng nghĩa là gỡ được. */
    public static List<ChoDung> dangDungODau(int id) {
        List<ChoDung> ra = new ArrayList<>();
        int nguoi = soNguoiDangGiu(id);
        if (nguoi > 0) {
            ra.add(new ChoDung("người chơi đang giữ trong túi/rương", nguoi));
        } else if (nguoi < 0) {
            ra.add(new ChoDung("KHÔNG kiểm được túi người chơi (lỗi truy vấn)", 1));
        }
        dem(ra, "vật phẩm rơi từ boss", "boss_drop", "item_id", id);
        dem(ra, "vật phẩm rơi từ quái", "drop_item", "item_id", id);
        dem(ra, "món bày bán trong shop", "item_shop", "temp_id", id);
        dem(ra, "món đang ký gửi", "shop_ky_gui", "item_id", id);
        return ra;
    }

    private static void dem(List<ChoDung> ra, String moTa, String bang,
            String cot, int id) {
        try {
            CrisResultSet rs = ConnectDB.executeQuery(
                    "SELECT COUNT(*) n FROM `" + bang + "` WHERE `" + cot + "` = ?",
                    id);
            int n = rs.next() ? rs.getInt("n") : 0;
            rs.dispose();
            if (n > 0) {
                ra.add(new ChoDung(moTa, n));
            }
        } catch (Exception ex) {
            // Bang co the khong ton tai o ban cai khac — bo qua, dung chan admin
            Logger.logException(MauVatPhamDAO.class, ex,
                    "Bỏ qua bảng " + bang + " khi kiểm vật phẩm " + id);
        }
    }

    /** Gộp danh sách chỗ dùng thành một câu đọc được. */
    public static String moTaChoDung(List<ChoDung> ds) {
        StringBuilder sb = new StringBuilder();
        for (ChoDung c : ds) {
            sb.append(sb.length() == 0 ? "" : ", ")
                    .append(c.soLuong).append(' ').append(c.moTa);
        }
        return sb.toString();
    }

    /**
     * Xoá hẳn mẫu vật phẩm. Chỉ làm được với id lớn nhất.
     *
     * @return {@code null} nếu xoá xong, ngược lại là lý do không xoá được
     */
    public static String xoa(int id) {
        int max = idLonNhat();
        if (max < 0) {
            return "Không đọc được bảng vật phẩm.";
        }
        if (id != max) {
            return "Chỉ xoá hẳn được vật phẩm có id LỚN NHẤT (hiện là " + max
                    + ").\n\n"
                    + "Id vật phẩm chính là chỉ số trong mảng mà máy chủ và client "
                    + "dùng để tra cứu (ItemService.getTemplate(id) = "
                    + "ITEM_TEMPLATES.get(id)). Xoá id " + id + " ở giữa sẽ kéo "
                    + "mọi id lớn hơn lùi một bậc — mọi món trong túi người chơi "
                    + "biến thành món khác.\n\n"
                    + "Dùng \"Vô hiệu\" để giữ chỗ mà làm món này thành ô rỗng.";
        }
        List<ChoDung> dung = dangDungODau(id);
        if (!dung.isEmpty()) {
            return "Không xoá được vật phẩm " + id + " vì đang có "
                    + moTaChoDung(dung) + ".\n\n"
                    + "Gỡ hết các chỗ đó trước đã.";
        }
        try {
            ConnectDB.executeUpdate("DELETE FROM item_template WHERE id = ?", id);
            // Bo phan tu CUOI: cac vi tri con lai giu nguyen, id van bang vi tri.
            if (Manager.ITEM_TEMPLATES != null
                    && id == Manager.ITEM_TEMPLATES.size() - 1) {
                Manager.ITEM_TEMPLATES.remove(id);
            }
            tangPhienBanVatPham();
            Logger.log(Logger.YELLOW, "Đã xoá mẫu vật phẩm cuối: " + id + "\n");
            return null;
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex, "Lỗi xoá vật phẩm " + id);
            return "Lỗi khi xoá — xem log máy chủ.";
        }
    }

    /**
     * Biến mẫu thành ô rỗng nhưng <b>giữ nguyên dòng</b>, để chỉ số không xê dịch.
     *
     * <p>Dùng cho id ở giữa. Món vẫn còn trong CSDL nhưng không mặc được, không
     * cộng gì, và nhìn danh mục là biết đã bỏ.</p>
     *
     * @return {@code null} nếu xong, ngược lại là lý do
     */
    public static String voHieu(int id) {
        List<ChoDung> dung = dangDungODau(id);
        if (!dung.isEmpty()) {
            return "Không vô hiệu được vật phẩm " + id + " vì đang có "
                    + moTaChoDung(dung) + ".\n\n"
                    + "Người chơi đang giữ món này sẽ thấy nó thành ô rỗng. "
                    + "Gỡ hết các chỗ đó trước đã.";
        }
        try {
            // Giu nguyen id va cac cot khoa; chi lam rong phan noi dung.
            ConnectDB.executeUpdate(
                    "UPDATE item_template SET NAME = ?, description = '',"
                    + " TYPE = 99, gender = 3, part = -1, head = -1, body = -1,"
                    + " leg = -1, power_require = 0, level = 0, is_up_to_up = 0,"
                    + " gold = 0, gold_sell = 0, gem = 0, gem_sell = 0, ruby = 0,"
                    + " ruby_sell = 0 WHERE id = ?", TEN_DA_XOA, id);
            ItemTemplate t = (Manager.ITEM_TEMPLATES != null
                    && id >= 0 && id < Manager.ITEM_TEMPLATES.size())
                    ? Manager.ITEM_TEMPLATES.get(id) : null;
            if (t != null && t.id == id) {
                t.name = TEN_DA_XOA;
                t.description = "";
                t.type = 99;
                t.gender = 3;
                t.part = -1;
                t.head = -1;
                t.body = -1;
                t.leg = -1;
                t.strRequire = 0;
                t.level = 0;
                t.isUpToUp = false;
            }
            tangPhienBanVatPham();
            Logger.log(Logger.YELLOW, "Đã vô hiệu mẫu vật phẩm: " + id + "\n");
            return null;
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex, "Lỗi vô hiệu vật phẩm " + id);
            return "Lỗi khi vô hiệu — xem log máy chủ.";
        }
    }

    /**
     * Đặt dấu "dùng được" cho một mẫu vật phẩm.
     *
     * <p>Chỉ là một dấu tick lưu trong CSDL — không gắn logic gì trong game và
     * không gửi sang client, nên <b>không</b> tăng phiên bản vật phẩm: tăng chỉ
     * làm mọi người chơi tải lại toàn bộ bảng mà chẳng thấy gì khác.</p>
     *
     * @return {@code null} nếu lưu xong, ngược lại là lý do
     */
    public static String datDungDuoc(int id, boolean bat) {
        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE item_template SET dung_duoc = ? WHERE id = ?",
                    bat ? 1 : 0, id);
            if (n == 0) {
                return "Không có vật phẩm id " + id + ".";
            }
            ItemTemplate t = (Manager.ITEM_TEMPLATES != null
                    && id >= 0 && id < Manager.ITEM_TEMPLATES.size())
                    ? Manager.ITEM_TEMPLATES.get(id) : null;
            if (t != null && t.id == id) {
                t.dungDuoc = bat;
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex,
                    "Lỗi đặt dùng được cho vật phẩm " + id);
            return "Lỗi khi lưu — xem log máy chủ.";
        }
    }

    /** Đọc dấu "dùng được" thẳng từ CSDL, không qua bộ nhớ đệm. */
    public static boolean dungDuoc(int id) {
        try {
            CrisResultSet rs = ConnectDB.executeQuery(
                    "SELECT dung_duoc FROM item_template WHERE id = ?", id);
            boolean ra = rs.next() && rs.getBoolean("dung_duoc");
            rs.dispose();
            return ra;
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex,
                    "Lỗi đọc dùng được của vật phẩm " + id);
            return true;
        }
    }

    /**
     * Client phải tải lại bảng vật phẩm thì mới thấy thay đổi.
     *
     * <p>Chia lấy dư 128 vì client đọc phiên bản bằng một byte có dấu.</p>
     */
    public static void tangPhienBanVatPham() {
        try {
            long v = ConfigDAO.num(ConfigDAO.VS_ITEM);
            ConfigDAO.set(ConfigDAO.VS_ITEM, String.valueOf((v + 1) % 128));
        } catch (Exception ex) {
            Logger.logException(MauVatPhamDAO.class, ex,
                    "Lỗi tăng phiên bản vật phẩm");
        }
    }
}
