package nro.repository.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Quản lý hào quang (aura) và việc gán hào quang cho cải trang.
 *
 * <p>{@code Player.getAura()} trả về một byte id mà client tự vẽ. Trước đây id
 * đó bị gõ cứng theo id cải trang, thêm một cái là phải sửa mã và biên dịch lại.
 * Nay đọc từ bảng {@code aura} và cột {@code item_template.aura_id}.</p>
 *
 * <p>Nhớ đệm trong bộ nhớ vì {@code getAura()} bị gọi mỗi lần vẽ người chơi —
 * truy vấn CSDL ở đó là tự bóp cổ máy chủ. Sửa trong panel thì gọi
 * {@link #napLai()}.</p>
 */
public final class AuraDAO {

    /** Một hào quang. */
    public static final class Aura {

        public int id;
        public String ten;
        public boolean choCaiTrang;
        public String ghiChu;

        @Override
        public String toString() {
            return id + " — " + ten;
        }
    }

    private static volatile List<Aura> cache;
    private static volatile Map<Integer, Integer> auraCuaVatPham;
    private static volatile Map<Integer, Aura> theoId;

    private AuraDAO() {
    }

    /** Bỏ bộ nhớ đệm, lần đọc sau lấy lại từ CSDL. */
    public static void napLai() {
        cache = null;
        auraCuaVatPham = null;
        theoId = null;
        auraRieng = null;
    }

    public static List<Aura> tatCa() {
        List<Aura> c = cache;
        if (c != null) {
            return c;
        }
        List<Aura> ra = new ArrayList<>();
        Map<Integer, Aura> m = new HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, ten, cho_cai_trang, ghi_chu FROM aura ORDER BY id");
            while (rs.next()) {
                Aura a = new Aura();
                a.id = rs.getInt("id");
                a.ten = rs.getString("ten");
                a.choCaiTrang = rs.getBoolean("cho_cai_trang");
                a.ghiChu = rs.getString("ghi_chu");
                ra.add(a);
                m.put(a.id, a);
            }
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex, "Lỗi đọc bảng aura");
        } finally {
            dispose(rs);
        }
        cache = ra;
        theoId = m;
        return ra;
    }

    /** Hào quang người chơi được phép tự chọn — tức KHÔNG dành riêng cải trang. */
    public static List<Aura> choTuChon() {
        List<Aura> ra = new ArrayList<>();
        for (Aura a : tatCa()) {
            if (!a.choCaiTrang) {
                ra.add(a);
            }
        }
        return ra;
    }

    public static Aura theoId(int id) {
        tatCa();
        Map<Integer, Aura> m = theoId;
        return m == null ? null : m.get(id);
    }

    /** Người chơi có được tự chọn hào quang này không. */
    public static boolean duocTuChon(int id) {
        Aura a = theoId(id);
        return a != null && !a.choCaiTrang;
    }

    /**
     * Hào quang gán cho một mẫu cải trang, hoặc {@code -1}.
     *
     * <p>Gọi rất dày (mỗi lần vẽ người chơi) nên đọc từ bộ nhớ đệm.</p>
     */
    public static int auraCuaCaiTrang(int itemId) {
        Map<Integer, Integer> m = auraCuaVatPham;
        if (m == null) {
            m = new HashMap<>();
            CrisResultSet rs = null;
            try {
                rs = ConnectDB.executeQuery(
                        "SELECT id, aura_id FROM item_template WHERE aura_id <> -1");
                while (rs.next()) {
                    m.put(rs.getInt("id"), rs.getInt("aura_id"));
                }
            } catch (Exception ex) {
                Logger.logException(AuraDAO.class, ex, "Lỗi đọc aura của cải trang");
            } finally {
                dispose(rs);
            }
            auraCuaVatPham = m;
        }
        Integer v = m.get(itemId);
        return v == null ? -1 : v;
    }

    /**
     * Thêm hoặc sửa một hào quang.
     *
     * @return {@code null} nếu xong, ngược lại là lý do
     */
    public static String luu(int id, String ten, boolean choCaiTrang, String ghiChu) {
        if (ten == null || ten.trim().isEmpty()) {
            return "Chưa đặt tên cho hào quang.";
        }
        // Client doc id hao quang bang MOT BYTE co dau; ngoai 0..127 la tran so
        // và ve ra hao quang khac han cai minh chon.
        if (id < 0 || id > 127) {
            return "Id hào quang phải trong khoảng 0..127 — client đọc id này "
                    + "bằng một byte có dấu.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO aura (id, ten, cho_cai_trang, ghi_chu)"
                    + " VALUES (?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " cho_cai_trang = VALUES(cho_cai_trang),"
                    + " ghi_chu = VALUES(ghi_chu)",
                    id, ten.trim(), choCaiTrang ? 1 : 0,
                    ghiChu == null ? "" : ghiChu.trim());
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex, "Lỗi lưu aura " + id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Xoá một hào quang.
     *
     * <p>Từ chối nếu còn cải trang đang gán hoặc người chơi đang chọn — xoá đi
     * thì chỗ đó trỏ vào một hào quang không còn tồn tại.</p>
     */
    public static String xoa(int id) {
        try {
            int soCaiTrang = dem("SELECT COUNT(*) n FROM item_template"
                    + " WHERE aura_id = ?", id);
            int soNguoi = dem("SELECT COUNT(*) n FROM player WHERE aura_chon = ?", id);
            if (soCaiTrang > 0 || soNguoi > 0) {
                StringBuilder sb = new StringBuilder();
                if (soCaiTrang > 0) {
                    sb.append(soCaiTrang).append(" cải trang đang gán");
                }
                if (soNguoi > 0) {
                    sb.append(sb.length() == 0 ? "" : ", ")
                            .append(soNguoi).append(" người chơi đang chọn");
                }
                return "Không xoá được hào quang " + id + " vì còn " + sb + ".\n\n"
                        + "Gỡ hết các chỗ đó trước đã.";
            }
            ConnectDB.executeUpdate("DELETE FROM aura WHERE id = ?", id);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex, "Lỗi xoá aura " + id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Gán hào quang cho một cải trang; {@code -1} là gỡ. */
    public static String ganChoCaiTrang(int itemId, int auraId) {
        if (auraId != -1) {
            Aura a = theoId(auraId);
            if (a == null) {
                return "Không có hào quang id " + auraId + " trong bảng aura.";
            }
        }
        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE item_template SET aura_id = ? WHERE id = ?",
                    auraId, itemId);
            if (n == 0) {
                return "Không có vật phẩm id " + itemId + ".";
            }
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex,
                    "Lỗi gán aura cho vật phẩm " + itemId);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Lưu hào quang người chơi tự chọn.
     *
     * <p>Ghi bằng một câu {@code UPDATE} riêng chứ không chen vào câu lưu người
     * chơi khổng lồ trong {@code PlayerDAO} — câu đó truyền tham số theo vị trí,
     * thêm một cột vào giữa là lệch hết phần sau.</p>
     *
     * @return {@code null} nếu xong, ngược lại là lý do
     */
    public static String luuAuraChon(long playerId, int auraId) {
        if (auraId != -1 && !duocTuChon(auraId)) {
            return "Hào quang này chỉ hiện khi mặc cải trang được gán, "
                    + "không tự chọn được.";
        }
        try {
            ConnectDB.executeUpdate(
                    "UPDATE player SET aura_chon = ? WHERE id = ?",
                    auraId, playerId);
            return null;
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex,
                    "Lỗi lưu aura tự chọn cho người chơi " + playerId);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /** Đếm số cải trang đang gán một hào quang. */
    public static int soCaiTrangDangGan(int auraId) {
        return dem("SELECT COUNT(*) n FROM item_template WHERE aura_id = ?", auraId);
    }

    private static int dem(String sql, Object... ts) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(sql, ts);
            return rs.next() ? rs.getInt("n") : 0;
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex, "Lỗi đếm: " + sql);
            return 0;
        } finally {
            dispose(rs);
        }
    }

    // ================= HÀO QUANG GÁN RIÊNG CHO NGƯỜI CHƠI =================

    /** Một dòng gán riêng, để panel hiện ra. */
    public static final class GanRieng {

        public long playerId;
        public String tenNhanVat;
        public int auraId;
        public String tenAura;
        public String ghiChu;
    }

    private static volatile Map<Long, Integer> auraRieng;

    /**
     * Hào quang gán riêng cho một nhân vật, hoặc {@code -1}.
     *
     * <p>Gọi mỗi lần vẽ người chơi nên phải đọc từ bộ nhớ đệm, y như
     * {@link #auraCuaCaiTrang}. Panel sửa xong thì gọi {@link #napLai()}.</p>
     */
    public static int auraRiengCua(long playerId) {
        Map<Long, Integer> m = auraRieng;
        if (m == null) {
            m = new HashMap<>();
            CrisResultSet rs = null;
            try {
                nro.repository.schema.LuocDoPanel.damBao();
                rs = ConnectDB.executeQuery(
                        "SELECT player_id, aura_id FROM player_aura");
                while (rs.next()) {
                    m.put(rs.getLong("player_id"), rs.getInt("aura_id"));
                }
            } catch (Exception ex) {
                Logger.logException(AuraDAO.class, ex,
                        "Lỗi đọc hào quang gán riêng");
            } finally {
                dispose(rs);
            }
            auraRieng = m;
        }
        Integer v = m.get(playerId);
        return v == null ? -1 : v;
    }

    /** Toàn bộ dòng gán riêng, kèm tên nhân vật và tên hào quang, cho panel. */
    public static List<GanRieng> danhSachGanRieng() {
        List<GanRieng> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            nro.repository.schema.LuocDoPanel.damBao();
            // LEFT JOIN: nhân vật đã xoá mà dòng gán còn sót thì vẫn phải hiện
            // ra để quản trị viên gỡ được, chứ không lặng lẽ biến mất.
            rs = ConnectDB.executeQuery(
                    "SELECT pa.player_id, pa.aura_id, pa.ghi_chu, p.name"
                    + " FROM player_aura pa LEFT JOIN player p ON p.id = pa.player_id"
                    + " ORDER BY p.name");
            while (rs.next()) {
                GanRieng g = new GanRieng();
                g.playerId = rs.getLong("player_id");
                g.auraId = rs.getInt("aura_id");
                g.ghiChu = rs.getString("ghi_chu");
                String ten = rs.getString("name");
                g.tenNhanVat = (ten == null || ten.isEmpty())
                        ? "(nhân vật đã xoá)" : ten;
                Aura a = theoId(g.auraId);
                g.tenAura = a == null ? String.valueOf(g.auraId) : a.toString();
                ra.add(g);
            }
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex,
                    "Lỗi liệt kê hào quang gán riêng");
        } finally {
            dispose(rs);
        }
        return ra;
    }

    /**
     * Gán hào quang cho một nhân vật. Đã có thì thay, không đẻ dòng thứ hai.
     *
     * @return {@code null} nếu xong, ngược lại là lý do
     */
    public static String datGanRieng(long playerId, int auraId, String ghiChu) {
        try {
            nro.repository.schema.LuocDoPanel.damBao();
            ConnectDB.executeUpdate(
                    "INSERT INTO player_aura (player_id, aura_id, ghi_chu)"
                    + " VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE aura_id = VALUES(aura_id),"
                    + " ghi_chu = VALUES(ghi_chu)",
                    playerId, auraId, ghiChu);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex, "Lỗi gán hào quang riêng");
            return ex.getMessage();
        }
    }

    /** Gỡ hào quang gán riêng của một nhân vật. */
    public static String xoaGanRieng(long playerId) {
        try {
            ConnectDB.executeUpdate(
                    "DELETE FROM player_aura WHERE player_id = ?", playerId);
            napLai();
            return null;
        } catch (Exception ex) {
            Logger.logException(AuraDAO.class, ex, "Lỗi gỡ hào quang riêng");
            return ex.getMessage();
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
