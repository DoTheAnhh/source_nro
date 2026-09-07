package nro.gameplay.minigame;

import java.util.ArrayList;
import java.util.List;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Đếm, trừ và trả thỏi vàng cho các trò chơi nhỏ.
 *
 * <h2>Vì sao tách riêng</h2>
 *
 * <p>Năm trò trong khu trò chơi đều tiêu và trả cùng một thứ tiền. Nếu mỗi trò
 * tự viết lấy phần cộng trừ thì sớm muộn cũng có một trò làm khác đi một chút —
 * và chỗ khác đi ấy là chỗ thất thoát tài sản của người chơi. Một bản duy nhất
 * thì sửa một lần là cả năm trò đúng theo.</p>
 *
 * <h2>Trừ khoá trước, trả về luôn là khoá</h2>
 *
 * <p>Thỏi vàng là <b>vật phẩm</b> mã 457 trong hành trang, không phải một con số
 * trên nhân vật. Bản khoá là cùng vật phẩm đó mang thuộc tính 30.</p>
 *
 * <p>Đặt cược trừ bản khoá trước rồi mới tới bản thường; tiền trả về — cả gốc
 * lẫn lãi — <b>luôn là bản khoá</b>. Nhờ thế vàng thường chỉ chảy ra chứ không
 * chảy vào, và không ai rửa được vàng khoá thành vàng thường bằng cách đem đánh
 * bạc rồi rút ra.</p>
 *
 * <h2>Toàn bộ lớp là hàm tĩnh, không giữ trạng thái</h2>
 *
 * <p>Các trò gọi từ nhiều luồng khác nhau — luồng mạng của người chơi và luồng
 * nhịp của trò chơi. Không giữ trạng thái thì không có gì để tranh nhau; phần
 * đồng bộ để cho từng trò tự lo trên dữ liệu của nó.</p>
 */
public final class KhoVang {

    /** Mã vật phẩm thỏi vàng trong hành trang. */
    public static final int ID_THOI_VANG = 457;

    /** Mã thuộc tính đánh dấu vật phẩm đã khoá. */
    public static final int OPTION_KHOA = 30;

    private KhoVang() {
    }

    /** Vật phẩm này có phải thỏi vàng không. */
    private static boolean laThoiVang(Item it) {
        return it != null && it.isNotNullItem() && it.template != null
                && it.template.id == ID_THOI_VANG;
    }

    /**
     * Đếm thỏi vàng trong hành trang.
     *
     * @param khoa {@code true} đếm bản khoá, {@code false} đếm bản thường
     */
    public static long dem(Player pl, boolean khoa) {
        if (pl == null || pl.inventory == null || pl.inventory.itemsBag == null) {
            return 0;
        }
        long tong = 0;
        for (Item it : pl.inventory.itemsBag) {
            if (laThoiVang(it)
                    && InventoryService.gI().haveOption(it, OPTION_KHOA) == khoa) {
                tong += it.quantity;
            }
        }
        return tong;
    }

    /** Tổng thỏi vàng đang có, cả khoá lẫn thường. */
    public static long demTatCa(Player pl) {
        return dem(pl, true) + dem(pl, false);
    }

    /**
     * Còn chỗ để nhận thưởng không.
     *
     * <p>Kiểm tra <b>trước khi trừ tiền</b>. Thắng mà hành trang đầy thì tiền
     * thưởng không có đường vào, và người chơi mất luôn cả tiền cược — kiểu hỏng
     * khó chịu nhất vì lỗi không nằm ở nước đi của họ.</p>
     *
     * <p>Đủ chỗ nghĩa là còn một ô trống, <i>hoặc</i> trong túi đã sẵn một chồng
     * thỏi vàng khoá để cộng dồn vào.</p>
     */
    public static boolean conChoNhanThuong(Player pl) {
        if (pl == null) {
            return false;
        }
        return InventoryService.gI().getCountEmptyBag(pl) > 0 || dem(pl, true) > 0;
    }

    /**
     * Trừ thỏi vàng, <b>ưu tiên bản khoá trước</b>.
     *
     * <p>Ví dụ có 10 khoá và 20 thường, trừ 20 thì hết 10 khoá rồi tới 10
     * thường.</p>
     *
     * <p>Gom danh sách vật phẩm ra trước rồi mới trừ, không vừa duyệt vừa sửa:
     * {@code subQuantityItemsBag} có thể xoá vật phẩm khỏi chính danh sách đang
     * duyệt, và duyệt một danh sách đang bị sửa là lỗi ngẫu nhiên khó tìm.</p>
     *
     * @return {@code true} nếu trừ đủ
     */
    public static boolean tru(Player pl, long soThoi) {
        if (pl == null || soThoi <= 0) {
            return false;
        }
        long can = soThoi;
        for (int luot = 0; luot < 2 && can > 0; luot++) {
            boolean khoa = (luot == 0);
            List<Item> ds = new ArrayList<>();
            for (Item it : pl.inventory.itemsBag) {
                if (laThoiVang(it)
                        && InventoryService.gI().haveOption(it, OPTION_KHOA) == khoa) {
                    ds.add(it);
                }
            }
            for (Item it : ds) {
                if (can <= 0) {
                    break;
                }
                int tru = (int) Math.min(can, it.quantity);
                InventoryService.gI().subQuantityItemsBag(pl, it, tru);
                can -= tru;
            }
        }
        InventoryService.gI().sendItemBag(pl);
        return can == 0;
    }

    /**
     * Thêm thỏi vàng <b>khoá</b> vào hành trang.
     *
     * <p>Chia thành nhiều chồng nếu quá lớn: {@code Item.quantity} là số nguyên
     * 32 bit, còn số thỏi ở đây là 64 bit.</p>
     *
     * @return {@code true} nếu vào hết
     */
    public static boolean themKhoa(Player pl, long soThoi) {
        if (pl == null) {
            return false;
        }
        if (soThoi <= 0) {
            return true;
        }
        long con = soThoi;
        while (con > 0) {
            int lan = (int) Math.min(con, Integer.MAX_VALUE);
            Item it = ItemService.gI().createNewItemLock(ID_THOI_VANG, lan);
            if (!InventoryService.gI().addItemBag(pl, it)) {
                InventoryService.gI().sendItemBag(pl);
                return false;
            }
            con -= lan;
        }
        InventoryService.gI().sendItemBag(pl);
        return true;
    }
}
