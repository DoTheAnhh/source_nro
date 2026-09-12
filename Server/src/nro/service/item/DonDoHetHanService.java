package nro.service.item;

import java.util.ArrayList;
import java.util.List;
import nro.core.consts.ConstNpc;
import nro.core.log.Logger;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.service.NpcService;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.shop.ShopService;

/**
 * Dọn sạch đồ <b>có hạn sử dụng</b> đang mang trong người.
 *
 * <p>Hạn dùng nằm ở ba chỉ số: 93 tính theo ngày, 260 theo giờ, 261 theo phút —
 * đúng bộ mà {@code ItemService.isOutOfDateTime} đọc để tự xoá món hết hạn. Ở
 * đây là dọn <b>chủ động</b>: người chơi xem trước danh sách rồi tự quyết, chứ
 * không phải chờ tới ngày món tự biến mất.</p>
 *
 * <h2>Luồng</h2>
 *
 * <ol>
 *   <li>Bảng liệt kê tên món, còn bao lâu, món nào đang mặc.</li>
 *   <li>Nút "Xem trong rương" mở đúng bảng vật phẩm có <b>ảnh</b> — nhìn icon
 *       cho chắc trước khi xoá.</li>
 *   <li>Nút "Xoá hết" mới thật sự xoá, và hỏi lại một lần nữa.</li>
 * </ol>
 *
 * <p>Món <b>đang mặc</b> được cởi ra bằng đúng đường cởi đồ thường
 * ({@code itemBodyToBag}) rồi mới xoá, để phần set kích hoạt và hiệu ứng trên
 * người được gỡ đúng cách. Hết chỗ trong hành trang thì bỏ qua món đó và nói
 * rõ, chứ không xoá lén.</p>
 */
public class DonDoHetHanService {

    private static DonDoHetHanService instance;

    public static DonDoHetHanService gI() {
        if (instance == null) {
            instance = new DonDoHetHanService();
        }
        return instance;
    }

    /** Chỉ số mang hạn dùng: 93 ngày, 260 giờ, 261 phút. */
    private static final int[] CHI_SO_HAN = {93, 260, 261};

    /** Số dòng tên món in ra trong bảng — dài hơn thì client cắt mất. */
    private static final int SO_DONG_HIEN = 12;

    public static boolean coHanDung(Item it) {
        if (it == null || !it.isNotNullItem()) {
            return false;
        }
        for (int cs : CHI_SO_HAN) {
            if (it.getOptionParam(cs) > 0) {
                return true;
            }
        }
        return false;
    }

    /** "còn 12 ngày" / "còn 5 giờ" / "còn 30 phút". */
    private static String conLai(Item it) {
        int ngay = it.getOptionParam(93);
        if (ngay > 0) {
            return "còn " + ngay + " ngày";
        }
        int gio = it.getOptionParam(260);
        if (gio > 0) {
            return "còn " + gio + " giờ";
        }
        int phut = it.getOptionParam(261);
        if (phut > 0) {
            return "còn " + phut + " phút";
        }
        return "sắp hết hạn";
    }

    /** Đồ có hạn dùng đang mang: trong hành trang trước, rồi tới đồ đang mặc. */
    public List<Item> dsCoHan(Player pl) {
        List<Item> ra = new ArrayList<>();
        if (pl == null || pl.inventory == null) {
            return ra;
        }
        for (Item it : new ArrayList<>(pl.inventory.itemsBag)) {
            if (coHanDung(it)) {
                ra.add(it);
            }
        }
        for (Item it : new ArrayList<>(pl.inventory.itemsBody)) {
            if (coHanDung(it)) {
                ra.add(it);
            }
        }
        return ra;
    }

    private static boolean dangMac(Player pl, Item it) {
        return pl.inventory.itemsBody.indexOf(it) >= 0;
    }

    /** Bảng danh sách + các nút. */
    public void mo(Player pl) {
        if (pl == null || pl.inventory == null) {
            return;
        }
        List<Item> ds = dsCoHan(pl);
        if (ds.isEmpty()) {
            Service.gI().sendThongBao(pl, "Bạn không mang món nào có hạn sử dụng.");
            return;
        }
        int soMac = 0;
        for (Item it : ds) {
            if (dangMac(pl, it)) {
                soMac++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("|7|ĐỒ CÓ HẠN SỬ DỤNG — ").append(ds.size()).append(" món\n");
        int hien = Math.min(ds.size(), SO_DONG_HIEN);
        for (int i = 0; i < hien; i++) {
            Item it = ds.get(i);
            boolean mac = dangMac(pl, it);
            sb.append(mac ? "|1|" : "|2|").append(i + 1).append(". ")
                    .append(it.template.name).append(mac ? " (đang mặc)" : "")
                    .append(" — ").append(conLai(it)).append("\n");
        }
        if (ds.size() > hien) {
            sb.append("|0|... và ").append(ds.size() - hien).append(" món nữa\n");
        }
        if (soMac > 0) {
            sb.append("|1|").append(soMac)
                    .append(" món đang mặc sẽ được cởi ra rồi xoá\n");
        }
        sb.append("|6|Xoá rồi không lấy lại được.");
        NpcService.gI().createMenuConMeo(pl, ConstNpc.DON_DO_HET_HAN, -1, sb.toString(),
                "Xem\ntrong rương", "Xoá hết\n" + ds.size() + " món", "Đóng");
    }

    /** Người chơi bấm một mục của bảng dọn đồ. */
    public void chon(Player pl, int select) {
        if (pl == null) {
            return;
        }
        switch (select) {
            case 0:
                ShopService.gI().moDanhSachChiXem(pl, "Đồ có\nhạn dùng",
                        "\n|1|Món có hạn sử dụng", dsCoHan(pl));
                Service.gI().sendThongBao(pl,
                        "Xem xong bấm lại NPC để xoá.");
                break;
            case 1:
                hoiLai(pl);
                break;
            default:
                break;
        }
    }

    /** Hỏi lại lần cuối trước khi xoá. */
    private void hoiLai(Player pl) {
        List<Item> ds = dsCoHan(pl);
        if (ds.isEmpty()) {
            Service.gI().sendThongBao(pl, "Bạn không mang món nào có hạn sử dụng.");
            return;
        }
        NpcService.gI().createMenuConMeo(pl, ConstNpc.DON_DO_HET_HAN_XAC_NHAN, -1,
                "|7|XOÁ " + ds.size() + " MÓN CÓ HẠN SỬ DỤNG\n"
                + "|1|Xoá rồi không lấy lại được, kể cả đồ đang mặc.\n"
                + "|2|Chắc chưa?",
                "Xoá", "Thôi");
    }

    /** Người chơi bấm ở bảng hỏi lại. */
    public void xacNhan(Player pl, int select) {
        if (pl == null || select != 0) {
            return;
        }
        xoa(pl);
    }

    private void xoa(Player pl) {
        int daXoa = 0;
        int coiRa = 0;
        int thieuCho = 0;
        try {
            for (Item it : dsCoHan(pl)) {
                int oMac = pl.inventory.itemsBody.indexOf(it);
                if (oMac >= 0) {
                    if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
                        thieuCho++;
                        continue;
                    }
                    // Coi ra bang dung duong coi do thuong: set kich hoat va
                    // hieu ung tren nguoi duoc go dung cach.
                    InventoryService.gI().itemBodyToBag(pl, oMac);
                    coiRa++;
                }
                if (pl.inventory.itemsBag.indexOf(it) >= 0) {
                    InventoryService.gI().removeItem(pl, it);
                    daXoa++;
                }
            }
        } catch (Exception ex) {
            Logger.logException(DonDoHetHanService.class, ex,
                    "Lỗi dọn đồ hết hạn của " + (pl.name == null ? "?" : pl.name));
        }
        InventoryService.gI().sendItemBag(pl);
        if (coiRa > 0) {
            InventoryService.gI().sendItemBody(pl);
            if (pl.nPoint != null) {
                pl.nPoint.calPoint();
            }
            Service.gI().point(pl);
            Service.gI().Send_Caitrang(pl);
            Service.gI().sendFlagBag(pl);
        }
        StringBuilder bao = new StringBuilder();
        bao.append("Đã xoá ").append(daXoa).append(" món có hạn sử dụng");
        if (coiRa > 0) {
            bao.append(" (").append(coiRa).append(" món phải cởi ra)");
        }
        bao.append('.');
        if (thieuCho > 0) {
            bao.append("\nCòn ").append(thieuCho)
                    .append(" món đang mặc chưa xoá được vì hành trang đã đầy.");
        }
        Service.gI().sendThongBao(pl, bao.toString());
    }
}
