package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Đọc/ghi <b>bản đồ, NPC đứng trên bản đồ, và cửa hàng</b> cho panel quản trị.
 *
 * <h2>Bốn bảng nối nhau</h2>
 *
 * <pre>
 * map_template.npcs = [[npcId, x, y], ...]   ← NPC nào đứng ở đâu
 *          │
 *          ▼  nối bằng npc_id
 * shop(id, npc_id, tag_name, type_shop)
 *          │
 *          ▼
 * tab_shop(id, shop_id, NAME)                ← các thẻ trong cửa hàng
 *          │
 *          ▼
 * item_shop(id, tab_id, temp_id, cost, costgold, type_sell, is_sell, is_new)
 *          │
 *          ▼
 * item_shop_option(item_shop_id, option_id, param)
 * </pre>
 *
 * <h2>Sửa xong có hiệu lực khi nào</h2>
 *
 * <ul>
 *   <li><b>Cửa hàng</b> — ngay, gọi {@code Manager.gI().updateShop()}.</li>
 *   <li><b>NPC trên bản đồ</b> — phải khởi động lại: {@code Manager} dựng đối
 *       tượng NPC một lần lúc nạp bản đồ.</li>
 * </ul>
 */
public class MapShopDAO {

    private MapShopDAO() {
    }

    // =====================================================================
    //  Bản đồ
    // =====================================================================

    /** Một bản đồ trong danh sách. */
    public static final class MapRow {

        public int id;
        public String name;
        public int zones;
        public int maxPlayer;
        /** Chuỗi JSON gốc của cột {@code npcs}. */
        public String npcs;
        public int soNpc;
    }

    public static List<MapRow> maps() {
        List<MapRow> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, NAME, zones, max_player, npcs FROM map_template ORDER BY id");
            while (rs.next()) {
                MapRow m = new MapRow();
                m.id = rs.getInt("id");
                m.name = rs.getString("NAME");
                m.zones = rs.getInt("zones");
                m.maxPlayer = rs.getInt("max_player");
                m.npcs = rs.getString("npcs");
                m.soNpc = demNpc(m.npcs);
                out.add(m);
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc map_template");
        } finally {
            dispose(rs);
        }
        return out;
    }

    private static int demNpc(String raw) {
        Object o = org.json.simple.JSONValue.parse(raw);
        return o instanceof org.json.simple.JSONArray ? ((org.json.simple.JSONArray) o).size() : 0;
    }

    /** Một NPC đứng trên bản đồ. */
    public static final class NpcTren {

        public int npcId;
        public int x;
        public int y;
        public String ten;
        /** {@code true} nếu NPC này có cửa hàng. */
        public boolean coShop;
    }

    /** NPC đứng trên một bản đồ, đọc từ cột {@code npcs}. */
    public static List<NpcTren> npcTrenMap(String npcsJson) {
        List<NpcTren> out = new ArrayList<>();
        Object o = org.json.simple.JSONValue.parse(npcsJson);
        if (!(o instanceof org.json.simple.JSONArray)) {
            return out;
        }
        for (Object p : (org.json.simple.JSONArray) o) {
            if (!(p instanceof org.json.simple.JSONArray)) {
                continue;
            }
            org.json.simple.JSONArray a = (org.json.simple.JSONArray) p;
            if (a.size() < 3) {
                continue;
            }
            try {
                NpcTren n = new NpcTren();
                n.npcId = Integer.parseInt(String.valueOf(a.get(0)).trim());
                n.x = Integer.parseInt(String.valueOf(a.get(1)).trim());
                n.y = Integer.parseInt(String.valueOf(a.get(2)).trim());
                n.ten = tenNpc(n.npcId);
                n.coShop = coShop(n.npcId);
                out.add(n);
            } catch (NumberFormatException ignored) {
                // Dòng hỏng trong dữ liệu cũ — bỏ qua, không làm vỡ cả bảng.
            }
        }
        return out;
    }

    /** Ghi lại danh sách NPC của một bản đồ. */
    public static String luuNpcMap(int mapId, List<NpcTren> ds) {
        StringBuilder sb = new StringBuilder("[");
        for (NpcTren n : ds) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append('[').append(n.npcId).append(',').append(n.x).append(',')
                    .append(n.y).append(']');
        }
        sb.append(']');
        try {
            ConnectDB.executeUpdate("UPDATE map_template SET npcs = ? WHERE id = ?",
                    sb.toString(), mapId);
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi lưu NPC của bản đồ " + mapId);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    // =====================================================================
    //  NPC
    // =====================================================================

    /** Một mẫu NPC. */
    public static final class NpcMau {

        public int id;
        public String name;

        @Override
        public String toString() {
            return id + " — " + name;
        }
    }

    private static List<NpcMau> cacheNpc;

    /**
     * Mẫu NPC dùng được, cho ô chọn. Đọc một lần rồi nhớ lại.
     *
     * <p><b>Bỏ qua mẫu hỏng</b>: tên rỗng, hoặc {@code head}/{@code body}/
     * {@code leg} đều {@code -1} (đứng trên bản đồ không vẽ ra gì). Trong CSDL
     * hiện có 15 mẫu tên rỗng — chúng hiện ra trong ô chọn thành những dòng
     * <code>99 —</code>, <code>100 —</code> không biết là ai.</p>
     *
     * <p>Chỉ giấu khỏi ô <b>chọn</b>; dòng vẫn còn trong CSDL và NPC nào đã đặt
     * trên bản đồ vẫn chạy như cũ.</p>
     */
    public static List<NpcMau> npcMau() {
        if (cacheNpc != null) {
            return cacheNpc;
        }
        List<NpcMau> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, NAME, head, body, leg FROM npc_template"
                    + " WHERE NAME IS NOT NULL AND TRIM(NAME) <> ''"
                    + "   AND NOT (head = -1 AND body = -1 AND leg = -1)"
                    + " ORDER BY id");
            while (rs.next()) {
                NpcMau n = new NpcMau();
                n.id = rs.getInt("id");
                n.name = rs.getString("NAME");
                out.add(n);
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc npc_template");
        } finally {
            dispose(rs);
        }
        cacheNpc = out;
        return out;
    }

    /** Số mẫu NPC bị giấu vì hỏng — để nói rõ trên hộp thoại. */
    public static int soNpcHong() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT COUNT(*) n FROM npc_template"
                    + " WHERE NAME IS NULL OR TRIM(NAME) = ''"
                    + "    OR (head = -1 AND body = -1 AND leg = -1)");
            return rs.next() ? rs.getInt("n") : 0;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đếm npc hỏng");
            return 0;
        } finally {
            dispose(rs);
        }
    }

    /** Một cải trang có thể dựng thành NPC. */
    public static final class CaiTrangMau {

        public int itemId;
        public String ten;
        public int head;
        public int body;
        public int leg;

        @Override
        public String toString() {
            return "Cải trang: " + ten;
        }
    }

    /**
     * Cải trang dùng được làm hình cho NPC.
     *
     * <p>Bỏ những cải trang không có hình ({@code head}/{@code body}/{@code leg}
     * đều {@code -1}) và những cải trang đã bị vô hiệu — dựng NPC từ chúng thì
     * NPC đứng trên bản đồ cũng không vẽ ra gì.</p>
     */
    public static List<CaiTrangMau> caiTrangLamNpc() {
        List<CaiTrangMau> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, NAME, head, body, leg FROM item_template"
                    + " WHERE TYPE = 5"
                    + "   AND NOT (head = -1 AND body = -1 AND leg = -1)"
                    + "   AND NAME <> ?"
                    + " ORDER BY NAME", MauVatPhamDAO.TEN_DA_XOA);
            while (rs.next()) {
                CaiTrangMau c = new CaiTrangMau();
                c.itemId = rs.getInt("id");
                c.ten = rs.getString("NAME");
                c.head = rs.getInt("head");
                c.body = rs.getInt("body");
                c.leg = rs.getInt("leg");
                out.add(c);
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc cải trang làm NPC");
        } finally {
            dispose(rs);
        }
        return out;
    }

    /**
     * Dựng một mẫu NPC mang hình của cải trang, trả về id NPC dùng được.
     *
     * <p>Đã có mẫu NPC nào trùng cả ba part thì <b>dùng lại</b> — mỗi lần bấm mà
     * đẻ thêm một mẫu giống hệt thì bảng phình ra vô ích.</p>
     *
     * <p>Mẫu mới bắt buộc lấy id {@code max + 1}: {@code NpcFactory} tra
     * {@code Manager.NPC_TEMPLATES.get(tempId)} nên <b>id chính là chỉ số
     * mảng</b>, bảng đang liên tục 0..117. Chèn id rời sẽ làm mọi NPC phía sau
     * lệch chỗ.</p>
     *
     * @return id NPC, hoặc {@code -1} nếu lỗi
     */
    public static int npcTuCaiTrang(CaiTrangMau c) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id FROM npc_template WHERE head = ? AND body = ?"
                    + " AND leg = ? ORDER BY id LIMIT 1", c.head, c.body, c.leg);
            if (rs.next()) {
                int cu = rs.getInt("id");
                dispose(rs);
                Logger.log(Logger.YELLOW, "Dùng lại mẫu NPC " + cu
                        + " cho cải trang " + c.ten + "\n");
                return cu;
            }
            dispose(rs);

            rs = ConnectDB.executeQuery("SELECT MAX(id) m FROM npc_template");
            int idMoi = (rs.next() ? rs.getInt("m") : -1) + 1;
            dispose(rs);
            if (idMoi <= 0) {
                return -1;
            }
            String ten = c.ten;
            if (ten != null && ten.length() > 50) {
                ten = ten.substring(0, 50);          // cot NAME chi 50 ky tu
            }
            ConnectDB.executeUpdate(
                    "INSERT INTO npc_template (id, NAME, head, body, leg, avatar)"
                    + " VALUES (?, ?, ?, ?, ?, 0)",
                    idMoi, ten, c.head, c.body, c.leg);
            cacheNpc = null;                          // buoc doc lai o chon
            Logger.log(Logger.GREEN, "Tạo mẫu NPC " + idMoi + " từ cải trang "
                    + c.ten + "\n");
            return idMoi;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex,
                    "Lỗi dựng NPC từ cải trang " + (c == null ? "?" : c.ten));
            return -1;
        } finally {
            dispose(rs);
        }
    }

    /**
     * Sửa tên và giao diện của một mẫu NPC.
     *
     * <p>Đổi ở đây là đổi cho <b>mọi bản đồ</b> có NPC đó — {@code npc_template}
     * là bảng mẫu dùng chung, không phải bản riêng của từng bản đồ.</p>
     *
     * <p>{@code head}/{@code body}/{@code leg} là id part; {@code avatar} là id
     * ảnh đại diện lúc nói chuyện. Đặt {@code -1} nghĩa là không có phần đó.</p>
     */
    public static String luuNpcMau(int id, String ten, int head, int body,
            int leg, int avatar, String loiChao) {
        if (ten == null || ten.trim().isEmpty()) {
            return "Tên NPC không được để trống.";
        }
        try {
            ConnectDB.executeUpdate(
                    "UPDATE npc_template SET NAME = ?, head = ?, body = ?, leg = ?,"
                    + " avatar = ?, loi_chao = ? WHERE id = ?",
                    ten, head, body, leg, avatar,
                    loiChao == null ? "" : loiChao.trim(), id);
            cacheNpc = null;      // bộ nhớ đệm đã cũ
            cacheLoiChao = null;
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi lưu npc_template " + id);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /** Câu chào mặc định khi mẫu NPC chưa đặt câu riêng. */
    public static final String LOI_CHAO_MAC_DINH = "Ta có thể giúp gì cho ngươi ?";

    private static volatile java.util.Map<Integer, String> cacheLoiChao;

    /**
     * Câu NPC nói khi bắt đầu nói chuyện.
     *
     * <p>Nhớ đệm vì hàm này bị gọi mỗi lần người chơi bấm vào NPC. Sửa trong
     * panel thì {@link #luuNpcMau} tự bỏ đệm.</p>
     *
     * @return câu đã đặt, hoặc {@link #LOI_CHAO_MAC_DINH} nếu để trống
     */
    public static String loiChao(int npcId) {
        java.util.Map<Integer, String> m = cacheLoiChao;
        if (m == null) {
            m = new java.util.HashMap<>();
            CrisResultSet rs = null;
            try {
                rs = ConnectDB.executeQuery(
                        "SELECT id, loi_chao FROM npc_template"
                        + " WHERE loi_chao IS NOT NULL AND TRIM(loi_chao) <> ''");
                while (rs.next()) {
                    m.put(rs.getInt("id"), rs.getString("loi_chao"));
                }
            } catch (Exception ex) {
                Logger.logException(MapShopDAO.class, ex, "Lỗi đọc câu chào NPC");
            } finally {
                dispose(rs);
            }
            cacheLoiChao = m;
        }
        String v = m.get(npcId);
        return v == null || v.trim().isEmpty() ? LOI_CHAO_MAC_DINH : v;
    }

    /** Giao diện hiện tại của một mẫu NPC: head, body, leg, avatar. */
    public static int[] giaoDienNpc(int id) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT head, body, leg, avatar FROM npc_template WHERE id = ?", id);
            if (rs.next()) {
                return new int[]{rs.getInt("head"), rs.getInt("body"),
                    rs.getInt("leg"), rs.getInt("avatar")};
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc giao diện NPC " + id);
        } finally {
            dispose(rs);
        }
        return new int[]{-1, -1, -1, -1};
    }

    /** Xoá cửa hàng của một NPC, kèm mọi thẻ và món trong đó. */
    public static String xoaShop(int shopId) {
        try {
            for (TabRow t : tabCuaShop(shopId)) {
                xoaTab(t.id);
            }
            ConnectDB.executeUpdate("DELETE FROM shop WHERE id = ?", shopId);
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi xoá shop " + shopId);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    public static String tenNpc(int id) {
        for (NpcMau n : npcMau()) {
            if (n.id == id) {
                return n.name;
            }
        }
        return "(không có NPC id này)";
    }

    private static boolean coShop(int npcId) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM shop WHERE npc_id = ?", npcId);
            return rs.next();
        } catch (Exception ex) {
            return false;
        } finally {
            dispose(rs);
        }
    }

    // =====================================================================
    //  Cửa hàng
    // =====================================================================

    /** Một cửa hàng gắn với NPC. */
    public static final class ShopRow {

        public int id;
        public int npcId;
        public String tagName;
        public int typeShop;
    }

    /** Cửa hàng của một NPC. Một NPC có thể có nhiều cửa hàng. */
    public static List<ShopRow> shopCuaNpc(int npcId) {
        List<ShopRow> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, npc_id, tag_name, type_shop FROM shop WHERE npc_id = ? ORDER BY id",
                    npcId);
            while (rs.next()) {
                ShopRow s = new ShopRow();
                s.id = rs.getInt("id");
                s.npcId = rs.getInt("npc_id");
                s.tagName = rs.getStringOrNull("tag_name");
                s.typeShop = rs.getInt("type_shop");
                out.add(s);
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc shop của NPC " + npcId);
        } finally {
            dispose(rs);
        }
        return out;
    }

    /** Một thẻ trong cửa hàng. */
    public static final class TabRow {

        public int id;
        public int shopId;
        public String name;

        @Override
        public String toString() {
            return name == null ? ("Thẻ " + id) : name;
        }
    }

    public static List<TabRow> tabCuaShop(int shopId) {
        List<TabRow> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, shop_id, NAME FROM tab_shop WHERE shop_id = ? ORDER BY id", shopId);
            while (rs.next()) {
                TabRow t = new TabRow();
                t.id = rs.getInt("id");
                t.shopId = rs.getInt("shop_id");
                t.name = rs.getStringOrNull("NAME");
                out.add(t);
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc tab_shop của shop " + shopId);
        } finally {
            dispose(rs);
        }
        return out;
    }

    /**
     * Thêm/sửa/xoá một thẻ hàng.
     *
     * <p>Xoá thẻ thì <b>xoá luôn món trong thẻ và chỉ số của chúng</b>: bảng
     * không có khoá ngoại nên để lại là thành dữ liệu mồ côi, không bao giờ hiện
     * ra nữa nhưng vẫn chiếm chỗ.</p>
     */
    public static String luuTab(int id, int shopId, String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            return "Tên thẻ không được để trống.";
        }
        try {
            if (id > 0) {
                ConnectDB.executeUpdate("UPDATE tab_shop SET NAME = ? WHERE id = ?", ten, id);
            } else {
                ConnectDB.executeUpdate(
                        "INSERT INTO tab_shop (shop_id, NAME) VALUES (?, ?)", shopId, ten);
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi lưu tab_shop");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    public static String xoaTab(int tabId) {
        try {
            for (ItemShopRow it : itemCuaTab(tabId)) {
                ConnectDB.executeUpdate(
                        "DELETE FROM item_shop_option WHERE item_shop_id = ?", it.id);
            }
            ConnectDB.executeUpdate("DELETE FROM item_shop WHERE tab_id = ?", tabId);
            ConnectDB.executeUpdate("DELETE FROM tab_shop WHERE id = ?", tabId);
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi xoá tab_shop " + tabId);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /**
     * Tên kiểu cửa hàng.
     *
     * <p>Đối chiếu từ {@code ShopService}: {@code NORMAL_SHOP = 0} trả tiền bằng
     * vàng/ngọc/hồng ngọc, {@code SPEC_SHOP = 1} và {@code POINT_SHOP = 3} trả
     * bằng <b>vật phẩm</b> — món phải trả xác định bởi cột {@code icon_spec} của
     * từng dòng hàng.</p>
     */
    public static String tenKieuShop(int typeShop) {
        switch (typeShop) {
            case 0:
                return "Bán thường (trả bằng tiền)";
            case 1:
                return "Đổi bằng vật phẩm";
            case 3:
                return "Đổi bằng điểm / vật phẩm";
            default:
                return "Kiểu " + typeShop;
        }
    }

    public static final int[] KIEU_SHOP = {0, 1, 3};

    /** Sửa tên và kiểu của một cửa hàng. */
    public static String luuShop(int shopId, String tagName, int typeShop) {
        try {
            ConnectDB.executeUpdate(
                    "UPDATE shop SET tag_name = ?, type_shop = ? WHERE id = ?",
                    tagName, typeShop, shopId);
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi lưu shop " + shopId);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /** Tạo một cửa hàng mới cho NPC. */
    public static String themShop(int npcId, String tagName, int typeShop) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return "Tên cửa hàng không được để trống.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO shop (npc_id, tag_name, type_shop) VALUES (?, ?, ?)",
                    npcId, tagName, typeShop);
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi thêm shop cho NPC " + npcId);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /**
     * Bảo đảm {@code item_shop_option} có cột {@code param_max}.
     *
     * <p>Cột này giữ trần của khoảng ngẫu nhiên; {@code -1} là trị cố định như
     * cũ. Thêm bằng {@code ALTER TABLE} lúc chạy chứ không bắt admin tự chạy
     * SQL — bảng này là bảng lõi, không nằm trong {@code LuocDoPanel}.</p>
     */
    private static boolean daKiemCotMax;

    public static synchronized void damBaoCotParamMax() {
        if (daKiemCotMax) {
            return;
        }
        daKiemCotMax = true;
        try {
            ConnectDB.executeUpdate(
                    "ALTER TABLE item_shop_option"
                    + " ADD COLUMN param_max INT NOT NULL DEFAULT -1");
            Logger.warning("Đã thêm cột param_max vào item_shop_option\n");
        } catch (Exception ex) {
            // Cột đã có -> MySQL báo lỗi trùng tên, đó là trường hợp bình
            // thường nhất nên không ghi log ầm ĩ.
        }
    }


    /**
     * Sửa nhiều món bán một lượt.
     *
     * @param chiSo danh sách chỉ số mới, {@code null} là <b>giữ nguyên</b> chỉ số
     *              riêng của từng món. Danh sách rỗng nghĩa là <b>xoá sạch</b>
     *              chỉ số — hai ý đó khác nhau nên không gộp làm một được.
     */
    public static String suaNhieuItemShop(java.util.List<Integer> ids,
            Integer gia, Integer loaiTien, Boolean dangBan, Boolean nhanMoi,
            List<int[]> chiSo) {
        if (ids == null || ids.isEmpty()) {
            return "Chưa chọn món nào.";
        }
        damBaoCotParamMax();
        try {
            for (int id : ids) {
                if (gia != null) {
                    ConnectDB.executeUpdate(
                            "UPDATE item_shop SET cost = ? WHERE id = ?", gia, id);
                }
                if (loaiTien != null) {
                    ConnectDB.executeUpdate(
                            "UPDATE item_shop SET type_sell = ?, icon_spec = ? WHERE id = ?",
                            loaiTien, anhLoaiTien(loaiTien), id);
                }
                if (dangBan != null) {
                    ConnectDB.executeUpdate(
                            "UPDATE item_shop SET is_sell = ? WHERE id = ?",
                            dangBan ? 1 : 0, id);
                }
                if (nhanMoi != null) {
                    ConnectDB.executeUpdate(
                            "UPDATE item_shop SET is_new = ? WHERE id = ?",
                            nhanMoi ? 1 : 0, id);
                }
                if (chiSo != null) {
                    // Xoá rồi ghi lại, giống hệt đường sửa một món: chỉ số là
                    // một bộ, sửa từng dòng thì không bỏ bớt được dòng cũ.
                    ConnectDB.executeUpdate(
                            "DELETE FROM item_shop_option WHERE item_shop_id = ?", id);
                    for (int[] o : chiSo) {
                        // o = {id chỉ số, trị nhỏ nhất, trị lớn nhất}. Ghi cả
                        // khoảng xuống CSDL chứ không bốc sẵn: cửa hàng phải
                        // hiện "5 ~ 10", và mỗi người mua bốc một trị riêng.
                        ConnectDB.executeUpdate(
                                "INSERT INTO item_shop_option (item_shop_id, option_id, param, param_max)"
                                + " VALUES (?, ?, ?, ?)", id, o[0], o[1],
                                o[2] > o[1] ? o[2] : -1);
                    }
                }
            }
            // Khong tu nap lai o day: phia panel goi apDungShop() sau khi luu,
            // giong het duong sua mot mon.
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi sửa nhiều món bán");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Ghi lai thu tu hien thi cho ca danh sach mon ban cua mot tab.
     *
     * <p>Ghi toan bo danh sach chu khong chi hai dong vua doi cho: neu chi sua
     * hai dong thi cac dong khac van co the trung so, thu tu se khong on dinh.</p>
     */
    public static String luuThuTu(java.util.List<Integer> idTheoThuTu) {
        try {
            for (int i = 0; i < idTheoThuTu.size(); i++) {
                ConnectDB.executeUpdate(
                        "UPDATE item_shop SET thu_tu = ? WHERE id = ?",
                        i + 1, idTheoThuTu.get(i));
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi lưu thứ tự món bán");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Một món hàng bày bán. */
    public static final class ItemShopRow {

        public int id;
        public int tabId;
        public int tempId;
        public String tenItem;
        public int cost;
        public int costGold;
        /** 0 vàng, 1 ngọc, 3 hồng ngọc, 4 điểm sự kiện. */
        public int typeSell;
        public boolean isSell;
        public boolean isNew;
        public int iconSpec;
        /** Thu tu hien trong cua hang, dung chung voi trong game. */
        public int thuTu;
        /** Chỉ số kèm theo, dạng {@code "6=25, 30=0"}. */
        public String options;
    }

    /**
     * Tên loại tiền dùng để mua.
     *
     * <p>Đối chiếu từ {@code ShopService}: {@code COST_GOLD = 0},
     * {@code COST_GEM = 1}, {@code COST_RUBY = 3}, {@code COST_EVENT = 4}.
     * Số 2 không được dùng ở đâu cả.</p>
     */
    public static String tenLoaiTien(int typeSell) {
        switch (typeSell) {
            case 0:
                return "Vàng";
            case 1:
                return "Ngọc";
            case 3:
                return "Hồng ngọc";
            case 4:
                return "Điểm sự kiện";
            case 5:
                return "Thỏi vàng";
            case 6:
                return "Điểm săn boss";
            default:
                return "Loại " + typeSell;
        }
    }

    /**
     * Một lần, lúc khởi động: cửa hàng Tranh Ngọc Namếc chuyển sang bán bằng
     * <b>điểm săn boss</b> (loại tiền 6).
     *
     * <p>Món còn dòng "Cần # điểm để đổi" (chỉ số 76) lớn hơn 0 thì giá lấy
     * đúng con số đó, loại tiền thành điểm săn boss; rồi xoá dòng 76 khỏi mọi
     * món của cửa hàng — giá giờ hiện ở ngoài, cạnh ảnh điểm. Ảnh giá
     * ({@code icon_spec}) giữ nguyên. Chạy lại không đổi gì thêm.</p>
     */
    public static void chuyenShopNamekSangDiemSanBoss() {
        try {
            int doi = ConnectDB.executeUpdate("UPDATE item_shop i"
                    + " JOIN tab_shop t ON t.id = i.tab_id"
                    + " JOIN shop s ON s.id = t.shop_id"
                    + " JOIN item_shop_option o ON o.item_shop_id = i.id AND o.option_id = 76"
                    + " SET i.cost = o.param, i.type_sell = 6"
                    + " WHERE s.tag_name = 'SHOP_NAMEK_WAR' AND o.param > 0");
            int xoa = ConnectDB.executeUpdate("DELETE o FROM item_shop_option o"
                    + " JOIN item_shop i ON i.id = o.item_shop_id"
                    + " JOIN tab_shop t ON t.id = i.tab_id"
                    + " JOIN shop s ON s.id = t.shop_id"
                    + " WHERE s.tag_name = 'SHOP_NAMEK_WAR' AND o.option_id = 76");
            if (doi > 0 || xoa > 0) {
                Logger.success("Cửa hàng Tranh Ngọc Namếc: " + doi
                        + " món chuyển sang điểm săn boss, bỏ " + xoa + " dòng \"Cần # điểm\"\n");
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi chuyển cửa hàng Namếc sang điểm săn boss");
        }
    }

    /**
     * Ảnh của điểm săn boss: đúng ảnh cửa hàng Tranh Ngọc Namếc đang dùng — ảnh
     * gặp nhiều nhất trong các món bán bằng điểm săn boss — để người chơi không
     * phải làm quen với một biểu tượng mới.
     */
    private static int anhDiemSanBoss() {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT icon_spec, COUNT(*) AS so FROM item_shop"
                    + " WHERE type_sell = 6 AND icon_spec > 0"
                    + " GROUP BY icon_spec ORDER BY so DESC LIMIT 1");
            if (rs.next()) {
                return rs.getInt("icon_spec");
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc ảnh điểm săn boss");
        } finally {
            dispose(rs);
        }
        return -1;
    }

    /** Các mã loại tiền hợp lệ, theo đúng thứ tự muốn hiện trên ô chọn. */
    public static final int[] LOAI_TIEN = {0, 1, 3, 4, 5, 6};

    /**
     * Ảnh của loại tiền — dùng cho cột {@code icon_spec}.
     *
     * <p>Cửa hàng kiểu <b>3 (bán bằng vật phẩm)</b> không gửi con số giá theo
     * loại tiền; nó gửi <b>ảnh</b> và số lượng, rồi lúc mua thì
     * {@code ShopService.subIemByItemShop} tra ngược ảnh ra vật phẩm và trừ
     * đúng món đó. Nên ảnh ở đây phải là ảnh của chính vật phẩm dùng làm tiền:
     * Vàng = 76, Ngọc = 77, Hồng ngọc = 861, Thỏi vàng = 457.</p>
     *
     * @return id ảnh, hoặc {@code -1} nếu loại tiền không có vật phẩm tương ứng
     */
    public static int anhLoaiTien(int typeSell) {
        int itemId;
        switch (typeSell) {
            case 0:
                itemId = 76;
                break;
            case 1:
                itemId = 77;
                break;
            case 3:
                itemId = 861;
                break;
            case 5:
                itemId = 457;
                break;
            case 6:
                return anhDiemSanBoss();
            default:
                return -1;
        }
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT icon_id FROM item_template WHERE id = ?", itemId);
            if (rs.next()) {
                return rs.getInt("icon_id");
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc ảnh loại tiền " + typeSell);
        } finally {
            dispose(rs);
        }
        return -1;
    }

    /**
     * {@code true} nếu cửa hàng <b>kiểu thường</b> không hiện được giá loại này.
     *
     * <p>Gói tin của kiểu thường chỉ có <b>hai ô giá</b>: vàng và ngọc. Hồng
     * ngọc mượn ô ngọc. Điểm sự kiện và Thỏi vàng không có ô nào, nên client
     * hiện giá 0 — người chơi thấy "Nhận Miễn phí" dù máy chủ vẫn trừ đúng.</p>
     */
    public static boolean kieuThuongKhongHienDuocGia(int typeSell) {
        return typeSell == 4 || typeSell == 5;
    }

    /**
     * Ghi lại {@code icon_spec} cho mọi món của một cửa hàng theo loại tiền.
     *
     * <p>Phải làm cho <b>cả cửa hàng</b> chứ không riêng món vừa sửa: khi cửa
     * hàng chuyển sang kiểu 3, mọi món đều thanh toán qua {@code icon_spec} —
     * món nào còn bỏ trống thì tra ra vật phẩm {@code -1} và mua sẽ hỏng.</p>
     *
     * @return số món đã ghi lại
     */
    public static int dongBoAnhTien(int shopId) {
        int n = 0;
        try {
            for (TabRow t : tabCuaShop(shopId)) {
                for (ItemShopRow it : itemCuaTab(t.id)) {
                    int anh = anhLoaiTien(it.typeSell);
                    if (anh >= 0 && it.iconSpec != anh) {
                        ConnectDB.executeUpdate(
                                "UPDATE item_shop SET icon_spec = ? WHERE id = ?", anh, it.id);
                        n++;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đồng bộ icon_spec shop " + shopId);
        }
        return n;
    }

    public static List<ItemShopRow> itemCuaTab(int tabId) {
        List<ItemShopRow> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, tab_id, temp_id, is_new, is_sell, type_sell, cost,"
                    + " costgold, icon_spec, thu_tu FROM item_shop WHERE tab_id = ?"
                    + " ORDER BY thu_tu, id", tabId);
            while (rs.next()) {
                ItemShopRow it = new ItemShopRow();
                it.id = rs.getInt("id");
                it.thuTu = rs.getInt("thu_tu");
                it.tabId = rs.getInt("tab_id");
                it.tempId = rs.getInt("temp_id");
                it.cost = rs.getInt("cost");
                it.costGold = rs.getInt("costgold");
                it.typeSell = rs.getInt("type_sell");
                it.isSell = rs.getBoolean("is_sell");
                it.isNew = rs.getBoolean("is_new");
                it.iconSpec = rs.getInt("icon_spec");
                out.add(it);
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc item_shop của tab " + tabId);
        } finally {
            dispose(rs);
        }
        for (ItemShopRow it : out) {
            it.options = docOption(it.id);
        }
        return out;
    }

    /** Chỉ số của một món hàng, dạng {@code "6=25, 50=5~10, 30=0"}. */
    public static String docOption(int itemShopId) {
        damBaoCotParamMax();
        StringBuilder sb = new StringBuilder();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT option_id, param, param_max FROM item_shop_option"
                    + " WHERE item_shop_id = ? ORDER BY id", itemShopId);
            while (rs.next()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                int min = rs.getInt("param");
                int max = rs.getInt("param_max");
                sb.append(rs.getInt("option_id")).append('=').append(min);
                if (max > min) {
                    sb.append('~').append(max);
                }
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi đọc chỉ số món hàng " + itemShopId);
        } finally {
            dispose(rs);
        }
        return sb.toString();
    }


    /**
     * Thêm hoặc sửa một món hàng, kèm chỉ số.
     *
     * <p>Chỉ số ghi kiểu <b>xoá hết rồi thêm lại</b>: bảng
     * {@code item_shop_option} không có khoá tự nhiên nào để đối chiếu từng dòng,
     * nên so từng dòng vừa phức tạp vừa dễ sót.</p>
     *
     * @param opts danh sách cặp {@code {optionId, param}}
     * @return {@code null} nếu xong, hoặc câu giải thích lỗi
     */
    public static String luuItemShop(ItemShopRow it, List<int[]> opts) {
        damBaoCotParamMax();
        if (it.tempId < 0) {
            return "Chưa chọn vật phẩm.";
        }
        if (it.cost < 0 || it.costGold < 0) {
            return "Giá không được âm.";
        }
        try {
            if (it.id > 0) {
                ConnectDB.executeUpdate(
                        "UPDATE item_shop SET tab_id = ?, temp_id = ?, is_new = ?, is_sell = ?,"
                        + " type_sell = ?, cost = ?, costgold = ?, icon_spec = ? WHERE id = ?",
                        it.tabId, it.tempId, it.isNew ? 1 : 0, it.isSell ? 1 : 0,
                        it.typeSell, it.cost, it.costGold, it.iconSpec, it.id);
            } else {
                ConnectDB.executeUpdate(
                        "INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell,"
                        + " cost, costgold, icon_spec) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        it.tabId, it.tempId, it.isNew ? 1 : 0, it.isSell ? 1 : 0,
                        it.typeSell, it.cost, it.costGold, it.iconSpec);
                it.id = idVuaThem(it.tabId, it.tempId);
            }
            if (it.id > 0) {
                ConnectDB.executeUpdate("DELETE FROM item_shop_option WHERE item_shop_id = ?", it.id);
                for (int[] o : opts) {
                    // o co the la {id, tri} hoac {id, min, max}. Do dai 2 la tri
                    // co dinh, khong co khoang ngau nhien.
                    int max = o.length > 2 && o[2] > o[1] ? o[2] : -1;
                    ConnectDB.executeUpdate(
                            "INSERT INTO item_shop_option (item_shop_id, option_id, param, param_max)"
                            + " VALUES (?, ?, ?, ?)", it.id, o[0], o[1], max);
                }
            }
            return null;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi lưu món hàng");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    private static int idVuaThem(int tabId, int tempId) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id FROM item_shop WHERE tab_id = ? AND temp_id = ?"
                    + " ORDER BY id DESC LIMIT 1", tabId, tempId);
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi tra id món hàng vừa thêm");
        } finally {
            dispose(rs);
        }
        return -1;
    }

    public static boolean xoaItemShop(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM item_shop_option WHERE item_shop_id = ?", id);
            return ConnectDB.executeUpdate("DELETE FROM item_shop WHERE id = ?", id) == 1;
        } catch (Exception ex) {
            Logger.logException(MapShopDAO.class, ex, "Lỗi xoá món hàng " + id);
            return false;
        }
    }

    private static void dispose(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
