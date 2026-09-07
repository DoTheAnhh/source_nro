package nro.gameplay.giftcode;

import nro.entity.player.Player;
import nro.service.Service;
import nro.core.log.Logger;
import nro.core.util.TimeUtil;
import nro.repository.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import nro.entity.item.ItemOption;
import nro.repository.CrisResultSet;

/**
 *
 * @author DoTheAnh
 */
public class GiftCodeManager {
    public String name;
    public final ArrayList<GiftCode> listGiftCode = new ArrayList<>();

    private static GiftCodeManager instance;

    public static GiftCodeManager gI() {
        if (instance == null) {
            instance = new GiftCodeManager();
        }
        return instance;
    }

    /**
     * Đọc lại toàn bộ giftcode từ CSDL.
     *
     * <p>{@link #init()} chỉ <b>thêm vào</b> danh sách chứ không xoá, nên gọi
     * thẳng init() lần hai sẽ nhân đôi mọi mã. Phải xoá trước.</p>
     */
    public void reload() {
        synchronized (listGiftCode) {
            listGiftCode.clear();
        }
        init();
    }

    public void init() {
        // Ban CSDL cu chua co cot limit_per_player. Va cot TRUOC khi doc, neu
        // khong moi ma deu roi ve 1 lan/nguoi va admin dat gioi han bao nhieu
        // cung khong an.
        nro.repository.dao.GiftCodeDAO.damBaoCot();
        try (Connection con = ConnectDB.getConnection();) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                ArrayList<Integer> tempListIdPlayer = new ArrayList<>();
                String tempDBListIdPlayers;
                giftcode.code = rs.getString("code");
                giftcode.countLeft = rs.getInt("count_left");
                try {
                    giftcode.gioiHanMoiNguoi = Math.max(1, rs.getInt("limit_per_player"));
                } catch (Exception thieuCot) {
                    // Ban CSDL cu chua co cot -> giu hanh vi cu: moi nguoi mot lan.
                    giftcode.gioiHanMoiNguoi = 1;
                }
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                String dbListIdPlayer = rs.getString("listIdPlayers");
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("item"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);
                        int idItem = Integer.valueOf(jsonObj.get("id").toString());
                        giftcode.detail.put(idItem,
                                Integer.valueOf(jsonObj.get("quantity").toString()));
                        // Chi so RIENG cua tung vat pham (khoa "options"). Ma cu
                        // khong co khoa nay -> giu nguyen hanh vi cu.
                        Object rieng = jsonObj.get("options");
                        if (rieng instanceof JSONArray) {
                            java.util.ArrayList<ItemOption> ds = new java.util.ArrayList<>();
                            for (Object o : (JSONArray) rieng) {
                                if (o instanceof JSONObject) {
                                    JSONObject j = (JSONObject) o;
                                    ds.add(new ItemOption(
                                            Integer.parseInt(j.get("id").toString()),
                                            Integer.parseInt(j.get("param").toString())));
                                }
                            }
                            giftcode.optionTheoItem.put(idItem, ds);
                        }
                        jsonObj.clear();
                    }
                }
                JSONArray option = (JSONArray) JSONValue.parse(rs.getString("option"));
                if (option != null) {
                    for (int u = 0; u < option.size(); u++) {
                        JSONObject jsonobject = (JSONObject) option.get(u);
                        giftcode.option.add(new ItemOption(Integer.parseInt(jsonobject.get("id").toString()),
                                Integer.parseInt(jsonobject.get("param").toString())));
                        jsonobject.clear();
                    }
                }
                if (!dbListIdPlayer.isEmpty()) {
                    tempDBListIdPlayers = dbListIdPlayer = removeCharAt(dbListIdPlayer, 0);
                    tempDBListIdPlayers = dbListIdPlayer = removeCharAt(dbListIdPlayer, dbListIdPlayer.length() - 1);
                    String[] resultTempDBListPlayer = tempDBListIdPlayers.split(",");
                    for (String item : resultTempDBListPlayer) {
                        if (!item.isEmpty())
                            tempListIdPlayer.add(Integer.parseInt(item));
                    }
                    giftcode.listIdPlayer = tempListIdPlayer;
                }
                listGiftCode.add(giftcode);
            }
            con.close();
            ps.close();
            rs.close();
            Logger.log(Logger.GREEN,"LOAD GIFTCODE (" + listGiftCode.size() + ") SUCCESS\n");
        } catch (Exception erorlog) {
        }
    }

    public void updateGiftCodeListIdPlayer(ArrayList<Integer> listIdPlayers, String code) {
        try {
            String sql = "UPDATE giftcode set listIdPlayers=? where code=?";
            // KHONG loc trung nua: so lan mot nguoi da nhap duoc dem bang so lan
            // id cua ho xuat hien trong danh sach. Loc trung la moi nguoi mai mai
            // chi nhap duoc mot lan, du gioi han dat bao nhieu.
            ConnectDB.executeUpdate(sql, JSONValue.toJSONString(listIdPlayers), code);
        } catch (Exception e) {
        }
    }
    
    public void sizeList(Player pl) {
        Service.gI().sendThongBao(pl, "" + GiftCode.class);
    }

    public GiftCode checkUseGiftCode(int idPlayer, String code) {
        for (GiftCode giftCode : listGiftCode) {
            if (giftCode.code.equals(code) && giftCode.countLeft > 0 && !giftCode.daDungHet(idPlayer)) {
                giftCode.countLeft -= 1;
                giftCode.addPlayerUsed(idPlayer);
                updateGiftCodeListIdPlayer(giftCode.listIdPlayer, code);
                return giftCode;
            }
        }
        return null;
    }
    
    public GiftCode CheckCode(int idPlayer, String code) {
        for (GiftCode giftCode : listGiftCode) {
            if (giftCode.code.equals(code) && giftCode.countLeft > 0 && !giftCode.daDungHet(idPlayer)) {
                return giftCode;
            }
        }
        return null;
    }

    public void checkInfomationGiftCode(Player p) throws Exception {
        CrisResultSet rs = ConnectDB.executeQuery("SELECT * FROM Giftcode WHERE id > 0");
        String textGift = "|7|[ - THÔNG TIN GIFTCODE - ]\n\n";
         while (rs.next()) {
            String code = rs.getString("code");
            int Luot = rs.getInt("count_left");
           String hsd = TimeUtil.getTimeNow(rs.getString("datecreate"));
           String hhsd = TimeUtil.getTimeNow(rs.getString("expired"));
            textGift += "|0|Giftcode : " + code + "\n"
                    + "|0|Số Lượng Còn : " + (Luot == 0 ? "[HẾT]" : + Luot + " Lượt") + "\n"
                    +"|3|[Hạn Sử Dụng : " + hsd + " ---> " + hhsd + "]\n\n";
        }
        rs.dispose();
        Service.gI().sendThongBaoFromAdmin(p, textGift);
    }

    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }
}
