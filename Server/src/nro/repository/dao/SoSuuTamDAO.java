package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.card.Card;
import nro.entity.card.OptionCard;
import nro.entity.card.RadarCard;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.service.Service;
import nro.service.card.RadarService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Bảng {@code radar} — các thẻ của <b>Sổ sưu tầm</b>, sửa ở tab "Sổ sưu tầm"
 * của panel.
 *
 * <p>Giữ nguyên các cột cũ của game (id, iconId, rank, max, type, mob_id, body,
 * name, info, options, aura_id) và <b>tự thêm</b> cột mới lúc khởi động — không
 * cần chạy SQL tay:</p>
 *
 * <ul>
 *   <li>{@code can_moi_cap} — số lượng lên từng cấp, dạng {@code "10,20,30"};
 *       độ dài là cấp tối đa. Rỗng thì dùng cột {@code max} như bản cũ.</li>
 *   <li>{@code aura_tu_cap} — hào quang hiện từ cấp này.</li>
 *   <li>{@code yeu_cau_the}, {@code yeu_cau_cap} — phải có thẻ khác đạt cấp nào
 *       mới dùng được thẻ này.</li>
 *   <li>{@code thu_tu} — thứ tự trong sổ.</li>
 * </ul>
 */
public final class SoSuuTamDAO {

    private SoSuuTamDAO() {
    }

    private static final String LUOC_DO = "CREATE TABLE IF NOT EXISTS `radar` ("
            + " `id` INT(11) NOT NULL,"
            + " `iconId` INT(11) DEFAULT 0,"
            + " `rank` TINYINT(4) DEFAULT 0,"
            + " `max` INT(11) DEFAULT 60,"
            + " `type` INT(11) DEFAULT 0,"
            + " `mob_id` INT(11) DEFAULT 1,"
            + " `body` VARCHAR(500) DEFAULT '[]',"
            + " `name` VARCHAR(500) DEFAULT '',"
            + " `info` VARCHAR(2000) DEFAULT '',"
            + " `options` TEXT,"
            + " `aura_id` SMALLINT(6) DEFAULT -1"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static final String[] COT_MOI = {
        "can_moi_cap VARCHAR(500) NOT NULL DEFAULT ''",
        "aura_tu_cap INT(11) NOT NULL DEFAULT 0",
        "yeu_cau_the INT(11) NOT NULL DEFAULT -1",
        "yeu_cau_cap INT(11) NOT NULL DEFAULT 0",
        "thu_tu INT(11) NOT NULL DEFAULT 0"
    };

    private static volatile boolean daTao;

    private static void damBaoBang() {
        if (daTao) {
            return;
        }
        synchronized (SoSuuTamDAO.class) {
            if (daTao) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                for (String cot : COT_MOI) {
                    ConnectDB.executeUpdate("ALTER TABLE radar ADD COLUMN IF NOT EXISTS " + cot);
                }
                moRongCotChiSo();
                daTao = true;
            } catch (Exception ex) {
                Logger.logException(SoSuuTamDAO.class, ex, "Không tạo được bảng radar");
            }
        }
    }

    /**
     * Cột {@code options} gốc là {@code VARCHAR(2000)} — chừng năm mươi dòng chỉ
     * số. Thẻ nhiều cấp, mỗi cấp vài dòng là tràn. Đổi sang TEXT một lần.
     */
    private static void moRongCotChiSo() {
        CrisResultSet rs = null;
        String kieu = null;
        try {
            rs = ConnectDB.executeQuery("SELECT DATA_TYPE FROM information_schema.COLUMNS"
                    + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'radar'"
                    + " AND COLUMN_NAME = 'options'");
            if (rs.next()) {
                kieu = rs.getString("DATA_TYPE");
            }
        } catch (Exception ex) {
            Logger.logException(SoSuuTamDAO.class, ex, "Không đọc được kiểu cột radar.options");
        } finally {
            dong(rs);
        }
        if (kieu != null && kieu.equalsIgnoreCase("varchar")) {
            try {
                ConnectDB.executeUpdate("ALTER TABLE radar MODIFY COLUMN options TEXT");
            } catch (Exception ex) {
                Logger.logException(SoSuuTamDAO.class, ex, "Không mở rộng được cột radar.options");
            }
        }
    }

    /** Đọc lại cả bảng và thay danh sách mẫu thẻ của máy chủ một lượt. */
    public static synchronized void napLai() {
        damBaoBang();
        List<RadarCard> ds = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM radar ORDER BY thu_tu, id");
            while (rs.next()) {
                try {
                    ds.add(docDong(rs));
                } catch (Exception ex) {
                    Logger.logException(SoSuuTamDAO.class, ex, "Một dòng radar hỏng, bỏ qua dòng đó");
                }
            }
        } catch (Exception ex) {
            Logger.logException(SoSuuTamDAO.class, ex, "Lỗi đọc bảng radar");
            return;
        } finally {
            dong(rs);
        }
        RadarService.gI().RADAR_TEMPLATE = ds;
        Logger.success("Sổ sưu tầm: đã nạp " + ds.size() + " thẻ\n");
    }

    private static RadarCard docDong(CrisResultSet rs) throws Exception {
        RadarCard rd = new RadarCard();
        rd.Id = (short) rs.getInt("id");
        rd.IconId = (short) rs.getInt("iconId");
        rd.Rank = (byte) Math.max(0, Math.min(6, rs.getInt("rank")));
        rd.Max = rs.getInt("max");
        rd.Type = (byte) rs.getInt("type");
        rd.Template = (short) rs.getInt("mob_id");
        rd.Name = chu(rs.getString("name"));
        rd.Info = chu(rs.getString("info"));
        Object than = JSONValue.parse(chu(rs.getString("body")));
        if (than instanceof JSONArray && !((JSONArray) than).isEmpty()
                && ((JSONArray) than).get(0) instanceof JSONObject) {
            JSONObject ob = (JSONObject) ((JSONArray) than).get(0);
            rd.Head = (short) so(ob.get("head"), -1);
            rd.Body = (short) so(ob.get("body"), -1);
            rd.Leg = (short) so(ob.get("leg"), -1);
            rd.Bag = (short) so(ob.get("bag"), -1);
        }
        rd.Options = docChiSo(rs.getString("options"));
        rd.AuraId = (short) rs.getInt("aura_id");
        int[] can = docCanMoiCap(rs.getString("can_moi_cap"));
        rd.CanMoiCap = can == null ? new int[0] : can;
        rd.AuraTuCap = rs.getInt("aura_tu_cap");
        rd.Require = (short) rs.getInt("yeu_cau_the");
        rd.RequireLevel = (short) rs.getInt("yeu_cau_cap");
        rd.ThuTu = rs.getInt("thu_tu");
        return rd;
    }

    /** Đọc JSON chỉ số — nhận cả khoá {@code activeCard} của bảng lẫn {@code active} của dữ liệu người chơi. */
    public static List<OptionCard> docChiSo(String json) {
        List<OptionCard> ra = new ArrayList<>();
        Object o = json == null ? null : JSONValue.parse(json);
        if (!(o instanceof JSONArray)) {
            return ra;
        }
        for (Object x : (JSONArray) o) {
            if (!(x instanceof JSONObject)) {
                continue;
            }
            JSONObject ob = (JSONObject) x;
            Object a = ob.get("activeCard");
            if (a == null) {
                a = ob.get("active");
            }
            int id = so(ob.get("id"), -1);
            if (id < 0) {
                continue;
            }
            ra.add(new OptionCard(id, so(ob.get("param"), 0),
                    (byte) Math.max(0, Math.min(127, so(a, 0)))));
        }
        return ra;
    }

    public static String ghiChiSo(List<OptionCard> ds) {
        StringBuilder sb = new StringBuilder("[");
        if (ds != null) {
            for (OptionCard o : ds) {
                if (o == null) {
                    continue;
                }
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append("{\"id\":").append(o.id).append(",\"param\":").append(o.param)
                        .append(",\"activeCard\":").append(o.active).append('}');
            }
        }
        return sb.append(']').toString();
    }

    /**
     * Đọc "10,20,30". Rỗng trả mảng rỗng; có phần không phải số dương trả
     * {@code null} để nơi gọi báo lỗi.
     */
    public static int[] docCanMoiCap(String s) {
        if (s == null || s.trim().isEmpty()) {
            return new int[0];
        }
        String[] phan = s.trim().split("[,;\\s]+");
        if (phan.length > 100) {
            return null;
        }
        int[] ra = new int[phan.length];
        for (int i = 0; i < phan.length; i++) {
            try {
                ra[i] = Integer.parseInt(phan[i].replace(".", "").trim());
            } catch (NumberFormatException ex) {
                return null;
            }
            if (ra[i] <= 0) {
                return null;
            }
        }
        return ra;
    }

    public static String ghiCanMoiCap(int[] can) {
        StringBuilder sb = new StringBuilder();
        if (can != null) {
            for (int v : can) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(v);
            }
        }
        return sb.toString();
    }

    /**
     * Lưu một thẻ. {@code idCu} là id trước khi sửa ({@code -1} với thẻ mới) — đổi
     * id thì dòng cũ bị xoá.
     *
     * @return {@code null} nếu thành công, ngược lại là lời báo lỗi
     */
    public static String luu(RadarCard c, int idCu) {
        if (c == null) {
            return "Thiếu dữ liệu thẻ.";
        }
        damBaoBang();
        String than = "[{\"head\":" + c.Head + ",\"body\":" + c.Body + ",\"leg\":" + c.Leg
                + ",\"bag\":" + c.Bag + "}]";
        try {
            if (idCu > 0 && idCu != c.Id) {
                ConnectDB.executeUpdate("DELETE FROM radar WHERE id = ?", idCu);
            }
            ConnectDB.executeUpdate("DELETE FROM radar WHERE id = ?", (int) c.Id);
            ConnectDB.executeUpdate("INSERT INTO radar (id, iconId, `rank`, `max`, `type`, mob_id,"
                    + " body, name, info, options, aura_id, can_moi_cap, aura_tu_cap,"
                    + " yeu_cau_the, yeu_cau_cap, thu_tu)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    (int) c.Id, (int) c.IconId, (int) c.Rank, c.Max, (int) c.Type,
                    (int) c.Template, than, chu(c.Name), chu(c.Info), ghiChiSo(c.Options),
                    (int) c.AuraId, ghiCanMoiCap(c.CanMoiCap), c.AuraTuCap,
                    (int) c.Require, (int) c.RequireLevel, c.ThuTu);
        } catch (Exception ex) {
            Logger.logException(SoSuuTamDAO.class, ex, "Lỗi lưu thẻ sổ sưu tầm " + c.Id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
        napLai();
        apDungChoNguoiOnline(c.Id);
        if (idCu > 0 && idCu != c.Id) {
            apDungChoNguoiOnline(idCu);
        }
        return null;
    }

    public static String xoa(int id) {
        damBaoBang();
        try {
            ConnectDB.executeUpdate("DELETE FROM radar WHERE id = ?", id);
        } catch (Exception ex) {
            Logger.logException(SoSuuTamDAO.class, ex, "Lỗi xoá thẻ sổ sưu tầm " + id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
        napLai();
        apDungChoNguoiOnline(id);
        return null;
    }

    /** Người đang online có thẻ này: tính lại chỉ số và hào quang ngay. */
    private static void apDungChoNguoiOnline(int id) {
        List<Player> ds;
        try {
            ds = new ArrayList<>(nro.server.Client.gI().getPlayersSnapshot());
        } catch (Exception ex) {
            return;
        }
        for (Player pl : ds) {
            if (pl == null || pl.Cards == null) {
                continue;
            }
            boolean co = false;
            for (Card cd : new ArrayList<>(pl.Cards)) {
                if (cd != null && cd.Id == id) {
                    co = true;
                    break;
                }
            }
            if (!co) {
                continue;
            }
            try {
                if (pl.nPoint != null) {
                    pl.nPoint.calPoint();
                }
                Service.gI().point(pl);
                RadarService.gI().updateAura(pl);
            } catch (Exception boQua) {
                // Nguoi choi vua thoat giua chung — bo qua nguoi do.
            }
        }
    }

    private static String chu(String s) {
        return s == null ? "" : s;
    }

    private static int so(Object o, int macDinh) {
        if (o == null) {
            return macDinh;
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException ex) {
            return macDinh;
        }
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
