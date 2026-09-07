package nro.repository.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.repository.schema.LuocDoPanel;

/**
 * Nút <b>thêm</b> vào menu NPC — nối vào cuối menu gốc.
 *
 * <h2>Vì sao nối vào cuối chứ không chèn giữa</h2>
 *
 * <p>Mỗi lớp NPC xử lý lựa chọn bằng {@code switch (select)} theo <b>vị trí</b>
 * mục. Chèn một mục vào giữa là mọi mục sau nó lệch số, và NPC chạy nhầm chức
 * năng — bấm "Cửa hàng" lại ra "Xoá tài khoản". Nối vào cuối thì mọi vị trí cũ
 * giữ nguyên, và những vị trí <b>vượt quá số mục gốc</b> chắc chắn là của
 * bảng này.</p>
 *
 * <h2>Chỉ nối vào menu gốc</h2>
 *
 * <p>NPC còn mở nhiều menu con, mỗi cái một số mục khác nhau. Nối vào menu con
 * thì vị trí lại lệch. Nên chỉ nối khi NPC mở <b>menu gốc</b>.</p>
 */
public final class NpcMenuThemDAO {

    private NpcMenuThemDAO() {
    }

    public static final String MO_SHOP = "mo_shop";
    public static final String THONG_BAO = "thong_bao";

    /** Một nút thêm. */
    public static final class Nut {

        public int id;
        public int npcId;
        public int thuTu;
        public String ten = "";
        public String hanhDong = MO_SHOP;
        public String thamSo = "";
        public boolean bat = true;

        public String moTaHanhDong() {
            return THONG_BAO.equals(hanhDong)
                    ? "Hiện thông báo: " + thamSo
                    : "Mở cửa hàng: " + thamSo;
        }
    }

    /** npcId → danh sách nút, đã sắp thứ tự. */
    private static final Map<Integer, List<Nut>> CACHE = new ConcurrentHashMap<>();
    private static volatile boolean daNap;

    public static void napLai() {
        LuocDoPanel.damBao();
        Map<Integer, List<Nut>> moi = new LinkedHashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, npc_id, thu_tu, ten, hanh_dong, tham_so, bat"
                    + " FROM npc_menu_them ORDER BY npc_id, thu_tu, id");
            while (rs.next()) {
                Nut n = new Nut();
                n.id = rs.getInt("id");
                n.npcId = rs.getInt("npc_id");
                n.thuTu = rs.getInt("thu_tu");
                n.ten = rs.getString("ten");
                n.hanhDong = rs.getString("hanh_dong");
                n.thamSo = rs.getString("tham_so");
                n.bat = rs.getBoolean("bat");
                if (n.ten == null) {
                    n.ten = "";
                }
                if (n.thamSo == null) {
                    n.thamSo = "";
                }
                moi.computeIfAbsent(n.npcId, k -> new ArrayList<>()).add(n);
            }
        } catch (Exception ex) {
            Logger.logException(NpcMenuThemDAO.class, ex, "Lỗi đọc nút thêm của NPC");
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
            synchronized (NpcMenuThemDAO.class) {
                if (!daNap) {
                    napLai();
                }
            }
        }
    }

    /** Các nút đang bật của một NPC. Không bao giờ trả {@code null}. */
    public static List<Nut> dangBat(int npcId) {
        List<Nut> ra = new ArrayList<>();
        try {
            damBaoNap();
            List<Nut> ds = CACHE.get(npcId);
            if (ds != null) {
                for (Nut n : ds) {
                    if (n.bat && !n.ten.trim().isEmpty()) {
                        ra.add(n);
                    }
                }
            }
        } catch (Exception ex) {
            // Nut them la tien nghi, khong duoc phep lam hong menu NPC.
        }
        return ra;
    }

    /** Toàn bộ nút của một NPC, kể cả nút đang tắt. */
    public static List<Nut> danhSach(int npcId) {
        damBaoNap();
        List<Nut> ds = CACHE.get(npcId);
        return ds == null ? new ArrayList<>() : new ArrayList<>(ds);
    }

    /**
     * Chạy hành động của một nút thêm.
     *
     * @return {@code true} nếu đã xử lý
     */
    public static boolean chay(Player player, Nut n) {
        if (player == null || n == null) {
            return false;
        }
        try {
            if (THONG_BAO.equals(n.hanhDong)) {
                nro.service.Service.gI().sendThongBao(player,
                        n.thamSo.replace("\\n", "\n"));
                return true;
            }
            if (n.thamSo == null || n.thamSo.trim().isEmpty()) {
                nro.service.Service.gI().sendThongBao(player,
                        "Nút này chưa gắn cửa hàng nào.");
                return true;
            }
            nro.service.shop.ShopService.gI().opendShop(player, n.thamSo.trim(), false);
            return true;
        } catch (Exception ex) {
            Logger.logException(NpcMenuThemDAO.class, ex,
                    "Lỗi chạy nút thêm " + n.id + " của NPC " + n.npcId);
            nro.service.Service.gI().sendThongBao(player, "Không thể thực hiện");
            return true;
        }
    }

    public static String them(Nut n) {
        String loi = kiemTra(n);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "INSERT INTO npc_menu_them (npc_id, thu_tu, ten, hanh_dong,"
                    + " tham_so, bat) VALUES (?, ?, ?, ?, ?, ?)",
                    n.npcId, n.thuTu, n.ten, n.hanhDong, n.thamSo, n.bat ? 1 : 0);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(NpcMenuThemDAO.class, ex, "Lỗi thêm nút NPC");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String sua(Nut n) {
        String loi = kiemTra(n);
        if (loi != null) {
            return loi;
        }
        try {
            LuocDoPanel.damBao();
            int k = ConnectDB.executeUpdate(
                    "UPDATE npc_menu_them SET thu_tu = ?, ten = ?, hanh_dong = ?,"
                    + " tham_so = ?, bat = ? WHERE id = ?",
                    n.thuTu, n.ten, n.hanhDong, n.thamSo, n.bat ? 1 : 0, n.id);
            napLai();
            return k == 0 ? "Không có nút id " + n.id + "." : null;
        } catch (Exception ex) {
            Logger.logException(NpcMenuThemDAO.class, ex, "Lỗi sửa nút NPC");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoa(int id) {
        try {
            LuocDoPanel.damBao();
            int k = ConnectDB.executeUpdate(
                    "DELETE FROM npc_menu_them WHERE id = ?", id);
            napLai();
            return k == 0 ? "Không có nút id " + id + "." : null;
        } catch (Exception ex) {
            Logger.logException(NpcMenuThemDAO.class, ex, "Lỗi xoá nút NPC");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    private static String kiemTra(Nut n) {
        if (n == null) {
            return "Thiếu dữ liệu.";
        }
        if (n.ten == null || n.ten.trim().isEmpty()) {
            return "Tên nút không được để trống.";
        }
        n.ten = n.ten.trim();
        if (n.hanhDong == null) {
            n.hanhDong = MO_SHOP;
        }
        if (!MO_SHOP.equals(n.hanhDong) && !THONG_BAO.equals(n.hanhDong)) {
            return "Hành động phải là mo_shop hoặc thong_bao.";
        }
        if (MO_SHOP.equals(n.hanhDong)
                && (n.thamSo == null || n.thamSo.trim().isEmpty())) {
            return "Chưa chọn cửa hàng cho nút này.";
        }
        if (n.thamSo == null) {
            n.thamSo = "";
        }
        return null;
    }
}
