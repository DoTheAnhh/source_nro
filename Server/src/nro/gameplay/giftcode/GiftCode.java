package nro.gameplay.giftcode;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import nro.entity.item.ItemOption;

/**
 *
 * @author DoTheAnh
 */

public class GiftCode {
    
    public String code;
    public int id;
    public int countLeft;

    /**
     * Mỗi người được nhập mã này tối đa bao nhiêu lần.
     *
     * <p>Mặc định 1 — đúng hành vi cũ. Số lần đã nhập đếm bằng số lần id người
     * chơi xuất hiện trong {@link #listIdPlayer}, nên danh sách đó
     * <b>không được lọc trùng</b> nữa.</p>
     */
    public int gioiHanMoiNguoi = 1;
    public HashMap<Integer, Integer> detail = new HashMap<>();
    public ArrayList<Integer> listIdPlayer = new ArrayList<>();
    /**
     * Chỉ số dùng chung cho mọi vật phẩm trong mã.
     *
     * <p>Giữ lại để 217 mã cũ vẫn chạy đúng. Mã mới nên đặt chỉ số
     * <b>riêng cho từng vật phẩm</b> ở {@link #optionTheoItem}.</p>
     */
    public ArrayList<ItemOption> option = new ArrayList<>();

    /**
     * Chỉ số riêng của từng vật phẩm, tra theo id mẫu vật phẩm.
     *
     * <p>Đọc từ khoá {@code "options"} trong mảng {@code item}:</p>
     * <pre>{@code [{"id":457,"quantity":10,"options":[{"id":6,"param":25}]}]}</pre>
     *
     * <p>Vật phẩm không có khoá đó thì rơi về {@link #option} dùng chung.</p>
     */
    public HashMap<Integer, ArrayList<ItemOption>> optionTheoItem = new HashMap<>();

    /**
     * Chỉ số áp cho một vật phẩm — <b>bản sao</b>, không phải danh sách gốc.
     *
     * <p>Phải sao chép: trước đây gán thẳng {@code itemOptions = giftcode.option}
     * nên mọi vật phẩm phát ra <i>dùng chung một đối tượng danh sách</i>. Sửa chỉ
     * số của một món là sửa luôn của mọi món khác và của cả mã gốc trong bộ nhớ.</p>
     */
    public ArrayList<ItemOption> optionCua(int idItem) {
        ArrayList<ItemOption> nguon = optionTheoItem.get(idItem);
        if (nguon == null) {
            nguon = option;
        }
        ArrayList<ItemOption> ban = new ArrayList<>();
        for (ItemOption io : nguon) {
            ban.add(new ItemOption(io));
        }
        return ban;
    }
    public Timestamp datecreate;
    public Timestamp dateexpired;
    
    /** Số lần một người đã nhập mã này. */
    public int soLanDaDung(int idPlayer) {
        int n = 0;
        for (Integer id : listIdPlayer) {
            if (id != null && id == idPlayer) {
                n++;
            }
        }
        return n;
    }

    /** {@code true} nếu người này đã dùng hết số lần cho phép. */
    public boolean daDungHet(int idPlayer) {
        return soLanDaDung(idPlayer) >= Math.max(1, gioiHanMoiNguoi);
    }

    public boolean isUsedGiftCode(int idPlayer) {
        return daDungHet(idPlayer);
    }

    public void addPlayerUsed(int idPlayer) {
        listIdPlayer.add(idPlayer);
    }
    
    public boolean timeCode() {
        if (this.datecreate == null || this.dateexpired == null) {
            return false;
        }
        return this.datecreate.getTime() > this.dateexpired.getTime();
    }
}






