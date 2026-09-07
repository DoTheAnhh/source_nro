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

    private static String khoa(int npcId, int chiSo) {
        return npcId + "|" + chiSo;
    }

    public static void napLai() {
        LuocDoPanel.damBao();
        Map<String, Muc> moi = new LinkedHashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT npc_id, chi_so, ten_goc, ten_moi FROM npc_menu");
            while (rs.next()) {
                Muc m = new Muc();
                m.npcId = rs.getInt("npc_id");
                m.chiSo = rs.getInt("chi_so");
                m.tenGoc = rs.getString("ten_goc");
                m.tenMoi = rs.getString("ten_moi");
                if (m.tenGoc == null) {
                    m.tenGoc = "";
                }
                moi.put(khoa(m.npcId, m.chiSo), m);
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
    public static String doiTen(int npcId, int chiSo, String tenGoc) {
        if (tenGoc == null) {
            return "";
        }
        try {
            damBaoNap();
            ghiNhanTenGoc(npcId, chiSo, tenGoc);
            Muc m = CACHE.get(khoa(npcId, chiSo));
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
    private static void ghiNhanTenGoc(int npcId, int chiSo, String tenGoc) {
        String k = khoa(npcId, chiSo);
        if (tenGoc.equals(DA_GHI.get(k))) {
            return;
        }
        DA_GHI.put(k, tenGoc);
        Muc m = CACHE.get(k);
        if (m != null && tenGoc.equals(m.tenGoc)) {
            return;
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO npc_menu (npc_id, chi_so, ten_goc) VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten_goc = VALUES(ten_goc)",
                    npcId, chiSo, tenGoc);
            if (m == null) {
                m = new Muc();
                m.npcId = npcId;
                m.chiSo = chiSo;
                CACHE.put(k, m);
            }
            m.tenGoc = tenGoc;
        } catch (Exception ex) {
            Logger.logException(NpcMenuDAO.class, ex,
                    "Lỗi ghi tên gốc mục menu NPC " + npcId + "/" + chiSo);
        }
    }

    /** Toàn bộ mục đã ghi nhận, sắp theo NPC rồi vị trí. */
    public static List<Muc> danhSach() {
        damBaoNap();
        List<Muc> ds = new ArrayList<>(CACHE.values());
        ds.sort((a, b) -> a.npcId != b.npcId
                ? Integer.compare(a.npcId, b.npcId)
                : Integer.compare(a.chiSo, b.chiSo));
        return ds;
    }

    /**
     * Đặt tên mới cho một mục. Để trống nghĩa là dùng lại tên gốc.
     *
     * @return {@code null} nếu lưu xong, ngược lại là lý do
     */
    public static String datTen(int npcId, int chiSo, String tenMoi) {
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
                    "INSERT INTO npc_menu (npc_id, chi_so, ten_goc, ten_moi)"
                    + " VALUES (?, ?, '', ?)"
                    + " ON DUPLICATE KEY UPDATE ten_moi = VALUES(ten_moi)",
                    npcId, chiSo, v);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(NpcMenuDAO.class, ex,
                    "Lỗi đặt tên mục menu NPC " + npcId + "/" + chiSo);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }
}
