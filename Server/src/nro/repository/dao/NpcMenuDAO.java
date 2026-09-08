package nro.repository.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.schema.LuocDoPanel;

/**
 * Đổi tên các mục trong menu NPC — cho <b>mọi</b> NPC, không riêng một con.
 *
 * <h2>Cách hoạt động</h2>
 *
 * <p>Mọi menu NPC đều đi qua {@code Npc.createOtherMenu()}. Ở đó mỗi nhãn được
 * cho qua {@link #doiTen}: có tên admin đặt thì dùng tên đó, không thì giữ
 * nguyên tên gõ trong mã.</p>
 *
 * <h2>Tự ghi lại tên gốc</h2>
 *
 * <p>Tên các mục nằm rải rác trong hàng chục lớp NPC, panel không có cách nào
 * biết trước. Nên lần đầu một mục được gửi cho người chơi, máy chủ <b>ghi lại
 * tên gốc</b> vào bảng. Mở NPC nào một lần là mục của NPC đó hiện ra trong
 * panel để sửa.</p>
 *
 * <h2>Vì sao có bộ nhớ đệm</h2>
 *
 * <p>{@code createOtherMenu} chạy trong luồng game, mỗi lần người chơi bấm vào
 * một NPC. Truy vấn CSDL ở đó sẽ chặn luồng game, nên đọc một lần vào bộ nhớ
 * rồi dùng lại; panel gọi {@link #napLai()} sau khi lưu.</p>
 */
public final class NpcMenuDAO {

    private NpcMenuDAO() {
    }

    /** Một mục menu. */
    public static final class Muc {

        public int npcId;

        /**
         * Mã số menu chứa mục này — chính là {@code indexMenu} mà NPC truyền
         * vào {@code createOtherMenu}.
         *
         * <p>Đây là thứ tách được menu chính với từng menu con: cả hai đều đánh
         * số ô lại từ 0, nên thiếu nó thì ô số 2 của menu chính và ô số 2 của
         * menu "Cửa hàng Bùa" là một.</p>
         */
        public int menuId;

        public int chiSo;
        public String tenGoc = "";
        public String tenMoi;

        /** Tên thật sự gửi cho client. */
        public String tenDung() {
            return tenMoi == null || tenMoi.trim().isEmpty() ? tenGoc : tenMoi;
        }

        /** Số thứ tự đếm từ 1 cho người đọc panel. */
        public int chi_soHienThi() {
            return chiSo + 1;
        }
    }

    /** khoá {@code npcId + "|" + chiSo} → mục. */
    private static final Map<String, Muc> CACHE = new ConcurrentHashMap<>();
    private static volatile boolean daNap;

    /** Các mục đã thấy trong phiên này, để không ghi CSDL lặp lại. */
    private static final Map<String, String> DA_GHI = new ConcurrentHashMap<>();

    /**
     * Khoá của một mục: NPC + menu + <b>tên gốc</b>.
     *
     * <p>Không dùng vị trí ô. Vị trí đổi theo trạng thái người chơi — Bà Hạt Mít
     * chèn thêm ô vào giữa khi có bông tai, chèn lên đầu khi còn lượt thưởng —
     * còn tên gốc thì nằm trong mã nguồn nên cố định.</p>
     */
    private static String khoa(int npcId, int menuId, String tenGoc) {
        return npcId + "|" + menuId + "|" + (tenGoc == null ? "" : tenGoc);
    }

    public static void napLai() {
        LuocDoPanel.damBao();
        chuyenBangCu();
        Map<String, Muc> moi = new LinkedHashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT npc_id, menu_id, chi_so, ten_goc, ten_moi"
                    + " FROM npc_menu_ten");
            while (rs.next()) {
                Muc m = new Muc();
                m.npcId = rs.getInt("npc_id");
                m.menuId = rs.getInt("menu_id");
                m.chiSo = rs.getInt("chi_so");
                m.tenGoc = rs.getString("ten_goc");
                m.tenMoi = rs.getString("ten_moi");
                if (m.tenGoc == null) {
                    m.tenGoc = "";
                }
                moi.put(khoa(m.npcId, m.menuId, m.tenGoc), m);
            }
        } catch (Exception ex) {
            Logger.logException(NpcMenuDAO.class, ex, "Lỗi đọc bảng tên mục NPC");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
        CACHE.clear();
        CACHE.putAll(moi);
        daNap = true;
    }

    /** Đã thử chuyển bảng cũ trong phiên này chưa. */
    private static volatile boolean daChuyen;

    /**
     * Chuyển tên đã đặt ở bảng cũ {@code npc_menu} sang bảng mới, một lần.
     *
     * <p>Bảng cũ khoá theo vị trí ô nên không biết mục đó thuộc menu nào — chép
     * sang với {@code menu_id = -1}. Tên đó sẽ <b>không</b> khớp mục nào (mọi
     * mục thật đều có menu_id thật), nên nó chỉ nằm đó cho admin nhìn lại mình
     * từng đặt gì, chứ không âm thầm đổi tên nhầm mục như trước.</p>
     *
     * <p>Chỉ chép dòng CÓ tên mới. Dòng chỉ có tên gốc thì bỏ — máy chủ tự ghi
     * lại đầy đủ vào bảng mới ngay lần đầu mở NPC.</p>
     */
    private static void chuyenBangCu() {
        if (daChuyen) {
            return;
        }
        daChuyen = true;
        try {
            ConnectDB.executeUpdate(
                    "INSERT IGNORE INTO npc_menu_ten"
                    + " (npc_id, menu_id, chi_so, ten_goc, ten_moi)"
                    + " SELECT npc_id, -1, chi_so, ten_goc, ten_moi"
                    + " FROM npc_menu"
                    + " WHERE ten_moi IS NOT NULL AND ten_moi <> ''");
        } catch (Exception ex) {
            // Chua co bang cu, hoac da chuyen roi — khong sao ca.
        }
    }

    private static void damBaoNap() {
        if (!daNap) {
            synchronized (NpcMenuDAO.class) {
                if (!daNap) {
                    napLai();
                }
            }
        }
    }

    /**
     * Tên thật sự của một mục menu.
     *
     * <p>Không bao giờ ném lỗi và không bao giờ trả {@code null}: hỏng CSDL thì
     * giữ nguyên tên gốc, menu vẫn hiện bình thường.</p>
     *
     * @param tenGoc tên gõ trong mã nguồn của NPC
     */
    public static String doiTen(int npcId, int menuId, int chiSo, String tenGoc) {
        if (tenGoc == null) {
            return "";
        }
        try {
            damBaoNap();
            ghiNhanTenGoc(npcId, menuId, chiSo, tenGoc);
            Muc m = CACHE.get(khoa(npcId, menuId, tenGoc));
            // Panel (hoac import tay) tung ghi chuoi "null" - bon ky tu n-u-l-l -
            // vao cot ten_moi thay vi de NULL that. Chuoi do khong rong nen no
            // lot qua moi kiem tra cu, roi duoc gui thang xuong client va hien
            // len nut menu dung chu "null".
            boolean coTenMoi = m != null && m.tenMoi != null
                    && !m.tenMoi.trim().isEmpty()
                    && !"null".equalsIgnoreCase(m.tenMoi.trim());
            if (coTenMoi) {
                // Panel chi go duoc mot dong, nen "\n" hai ky tu la cach duy
                // nhat go xuong dong.
                return m.tenMoi.replace("\\n", "\n");
            }
        } catch (Exception ex) {
            // Doi ten la tien nghi, khong duoc phep lam hong menu.
        }
        return tenGoc;
    }

    /**
     * Ghi lại tên gốc lần đầu thấy, để panel có gì mà liệt kê.
     *
     * <p>Chỉ ghi khi <b>chưa có dòng</b> hoặc <b>tên gốc đã đổi</b> (lập trình
     * viên sửa chuỗi trong mã). Không đụng vào {@code ten_moi} của admin.</p>
     */
    private static void ghiNhanTenGoc(int npcId, int menuId, int chiSo, String tenGoc) {
        String k = khoa(npcId, menuId, tenGoc);
        if (DA_GHI.containsKey(k)) {
            return;
        }
        DA_GHI.put(k, tenGoc);
        Muc m = CACHE.get(k);
        if (m != null) {
            // Da co dong roi. Vi tri o co the vua doi (menu co gian theo trang
            // thai) — cap nhat cho panel hien dung thu tu, nhung KHONG dung toi
            // ten_moi cua admin.
            if (m.chiSo != chiSo) {
                m.chiSo = chiSo;
                try {
                    ConnectDB.executeUpdate(
                            "UPDATE npc_menu_ten SET chi_so = ?"
                            + " WHERE npc_id = ? AND menu_id = ? AND ten_goc = ?",
                            chiSo, npcId, menuId, tenGoc);
                } catch (Exception boQua) {
                }
            }
            return;
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO npc_menu_ten (npc_id, menu_id, chi_so, ten_goc)"
                    + " VALUES (?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE chi_so = VALUES(chi_so)",
                    npcId, menuId, chiSo, tenGoc);
            m = new Muc();
            m.npcId = npcId;
            m.menuId = menuId;
            m.chiSo = chiSo;
            m.tenGoc = tenGoc;
            CACHE.put(k, m);
        } catch (Exception ex) {
            Logger.logException(NpcMenuDAO.class, ex,
                    "Lỗi ghi tên gốc mục menu NPC " + npcId + "/" + menuId);
        }
    }

    /** Toàn bộ mục đã ghi nhận, sắp theo NPC rồi menu rồi vị trí. */
    public static List<Muc> danhSach() {
        damBaoNap();
        List<Muc> ds = new ArrayList<>(CACHE.values());
        ds.sort((a, b) -> {
            if (a.npcId != b.npcId) {
                return Integer.compare(a.npcId, b.npcId);
            }
            if (a.menuId != b.menuId) {
                return Integer.compare(a.menuId, b.menuId);
            }
            return Integer.compare(a.chiSo, b.chiSo);
        });
        return ds;
    }

    /**
     * Đặt tên mới cho một mục. Để trống nghĩa là dùng lại tên gốc.
     *
     * @return {@code null} nếu lưu xong, ngược lại là lý do
     */
    public static String datTen(int npcId, int menuId, String tenGoc, String tenMoi) {
        try {
            LuocDoPanel.damBao();
            // Coi ca chuoi "null" la "de trong". Neu khong, mot lan luu hong se
            // nam lai trong CSDL va moi menu cua NPC do deu hien chu "null".
            String v = tenMoi == null ? null : tenMoi.trim();
            if (v != null && (v.isEmpty() || "null".equalsIgnoreCase(v))) {
                v = null;
            }
            if (v != null && v.length() > 255) {
                return "Tên dài quá 255 ký tự.";
            }
            ConnectDB.executeUpdate(
                    "INSERT INTO npc_menu_ten (npc_id, menu_id, ten_goc, ten_moi)"
                    + " VALUES (?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten_moi = VALUES(ten_moi)",
                    npcId, menuId, tenGoc == null ? "" : tenGoc, v);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(NpcMenuDAO.class, ex,
                    "Lỗi đặt tên mục menu NPC " + npcId + "/" + menuId);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }
}
