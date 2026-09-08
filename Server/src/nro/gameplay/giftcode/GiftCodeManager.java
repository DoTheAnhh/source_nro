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
                // Doc dang CHUOI roi tu doi, khong dung rs.getTimestamp().
                //
                // DAY LA CHO CHUA LOI "code co that ma bao khong ton tai".
                //
                // Hai cot `datecreate` va `expired` khai bao la TEXT, khong phai
                // DATETIME. Panel ghi "0" vao `expired` de noi "khong han", ma
                // rs.getTimestamp() gap chuoi "0" thi NEM LOI vi no khong doi
                // duoc sang moc thoi gian.
                //
                // Loi do bay len toi khoi catch cuoi ham — vong while dut giua
                // duong, nhung ma da doc duoc thi mat, va dong log
                // "LOAD GIFTCODE ... SUCCESS" nam TRONG try nen cung khong bao
                // gio in ra. Ket qua: listGiftCode rong, CheckCode() tra null,
                // va nguoi choi doc duoc cau "Giftcode vua nhap khong ton tai
                // trong he thong" trong khi ma nam ngay trong bang.
                //
                // Dau hieu nhan ra: khoi dong server ma KHONG thay dong
                // "LOAD GIFTCODE (n) SUCCESS" thi buoc nap da chet.
                giftcode.datecreate = docMocThoiGian(rs.getString("datecreate"));
                giftcode.dateexpired = docMocThoiGian(rs.getString("expired"));
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
            // KHONG nuot lang nua.
            //
            // Khoi nay tung la `catch (Exception erorlog) {}` — rong hoan toan.
            // Mot dong giftcode hong la ca buoc nap chet, ma khong de lai mot
            // dau vet nao: khong log, khong ngoai le, chi co nguoi choi bao
            // "code co ma nhap khong duoc". Mat rat nhieu thoi gian moi tim ra.
            //
            // Van khong nem lai — giftcode chet thi may chu van phai len. Nhung
            // gio no keu, va con noi da nap duoc bao nhieu ma truoc khi gay.
            Logger.logException(GiftCodeManager.class, erorlog,
                    "Loi nap giftcode, chi nap duoc " + listGiftCode.size() + " ma");
        }
    }

    /**
     * Đọc một mốc thời gian từ ô kiểu {@code TEXT}.
     *
     * <h2>Vì sao không dùng {@code ResultSet.getTimestamp}</h2>
     *
     * <p>Hai cột {@code datecreate} và {@code expired} của bảng
     * {@code giftcode} khai báo là {@code TEXT}, không phải {@code DATETIME}.
     * Panel quản trị ghi {@code "0"} vào {@code expired} với nghĩa <i>không
     * hết hạn</i>. Trình điều khiển MySQL gặp chuỗi đó thì ném lỗi thay vì trả
     * {@code null}, và vì cả vòng nạp nằm trong một {@code try} duy nhất, một
     * dòng như vậy làm mất toàn bộ giftcode.</p>
     *
     * <p>Nhận ba dạng: chuỗi ngày {@code yyyy-MM-dd HH:mm:ss}, một số mili giây
     * kể từ 1970, và {@code "0"}/rỗng nghĩa là không có mốc.</p>
     *
     * @return mốc thời gian, hoặc {@code null} nếu không có. {@code null} là
     *         giá trị hợp lệ — {@link GiftCode#timeCode()} coi đó là
     *         <b>chưa hết hạn</b>, đúng ý nghĩa của {@code "0"}.
     */
    private static java.sql.Timestamp docMocThoiGian(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty() || t.equals("0")) {
            return null;
        }
        try {
            return java.sql.Timestamp.valueOf(t);
        } catch (IllegalArgumentException khongPhaiNgay) {
            try {
                return new java.sql.Timestamp(Long.parseLong(t));
            } catch (NumberFormatException cungKhongPhaiSo) {
                // Khong doc duoc thi coi nhu khong co moc, hon la lam chet ca
                // buoc nap vi mot o go sai.
                return null;
            }
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
