package nro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.repository.dao.ConfigDAO;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Đổi số dư VNĐ lấy Thỏi Vàng <b>theo mốc</b> — chọn gói thay vì gõ số.
 *
 * <h2>Cân mốc</h2>
 *
 * <p>Gốc là 5 Thỏi Vàng / 1.000đ (tỉ lệ đổi phẳng trước đây), thưởng tăng dần
 * theo mốc và <b>chặn trần +30%</b>. Trần ấy để đổi mốc cao vẫn rẻ hơn thấy
 * rõ mà không đẻ vàng quá nhanh: một người đổi 1 triệu nhận 6.500 thỏi thay
 * vì 5.000, không phải 10.000–20.000.</p>
 *
 * <p>Mốc lưu trong cấu hình {@link ConfigDAO#DOI_TV_MOC} dạng
 * {@code "vnd:thoiVang,vnd:thoiVang,…"} — sửa trên panel, có hiệu lực ngay.</p>
 */
public class DoiThoiVangService {

    private static DoiThoiVangService instance;

    public static DoiThoiVangService gI() {
        if (instance == null) {
            instance = new DoiThoiVangService();
        }
        return instance;
    }

    /** Mã vật phẩm Thỏi Vàng. */
    private static final short THOI_VANG = 457;

    /** Người chơi → mốc đang chờ xác nhận {vnd, thoiVang}. */
    private final Map<Long, long[]> choXacNhan = new ConcurrentHashMap<>();

    /** Các mốc đang khai, theo thứ tự tăng dần; dòng hỏng thì bỏ qua. */
    public List<long[]> dsMoc() {
        List<long[]> ra = new ArrayList<>();
        String chuoi = ConfigDAO.chuoi(ConfigDAO.DOI_TV_MOC);
        for (String phan : chuoi.split(",")) {
            String[] kv = phan.trim().split(":");
            if (kv.length != 2) {
                continue;
            }
            try {
                long vnd = Long.parseLong(kv[0].trim().replace(".", ""));
                long tv = Long.parseLong(kv[1].trim().replace(".", ""));
                if (vnd > 0 && tv > 0 && vnd <= 2_000_000_000L && tv <= 2_000_000_000L) {
                    ra.add(new long[]{vnd, tv});
                }
            } catch (NumberFormatException sai) {
                // Dong hong thi bo dong do.
            }
        }
        ra.sort((a, b) -> Long.compare(a[0], b[0]));
        return ra;
    }

    /** "50K", "1.000K" — gọn cho nút bấm. */
    private static String gonTien(long vnd) {
        return vnd % 1000 == 0 ? (Util.soCham(vnd / 1000) + "K") : (Util.soCham(vnd) + "đ");
    }

    /** Phần trăm thưởng so với mốc đầu (mốc rẻ nhất tính là gốc). */
    private static long thuong(long[] moc, long[] goc) {
        double rGoc = (double) goc[1] / goc[0];
        double r = (double) moc[1] / moc[0];
        return Math.round((r / rGoc - 1) * 100);
    }

    /** Mở menu chọn mốc. */
    public void moMenu(Npc npc, Player pl, int maMenu) {
        if (!ConfigDAO.on(ConfigDAO.DOI_TV_BAT)) {
            Service.gI().sendThongBao(pl, "Chức năng đổi Thỏi Vàng đang tạm đóng.");
            return;
        }
        List<long[]> ds = dsMoc();
        if (ds.isEmpty()) {
            Service.gI().sendThongBao(pl, "Chưa có mốc đổi nào.");
            return;
        }
        long soDu = pl.getSession() == null ? 0 : pl.getSession().vnd;
        StringBuilder sb = new StringBuilder("|7|ĐỔI VNĐ LẤY THỎI VÀNG\n|2|Đổi mốc càng cao càng được thêm\n\n");
        List<String> nut = new ArrayList<>();
        for (long[] m : ds) {
            long t = thuong(m, ds.get(0));
            sb.append("|6|").append(gonTien(m[0])).append(" → ").append(Util.soCham(m[1])).append(" Thỏi Vàng")
                    .append(t > 0 ? " (+" + t + "%)" : "").append("\n");
            nut.add(gonTien(m[0]) + "\n" + Util.soCham(m[1]) + " TV");
        }
        sb.append("\n|0|Số dư: ").append(Util.soCham(soDu)).append(" VNĐ");
        nut.add("Đóng");
        npc.createOtherMenu(pl, maMenu, sb.toString(), nut.toArray(new String[0]));
    }

    /** Người chơi bấm một mốc: hỏi lại trước khi trừ tiền thật. */
    public void chonMoc(Npc npc, Player pl, int select, int maXacNhan) {
        List<long[]> ds = dsMoc();
        if (select < 0 || select >= ds.size()) {
            return;
        }
        long[] m = ds.get(select);
        choXacNhan.put(pl.id, m);
        npc.createOtherMenu(pl, maXacNhan, "|7|Xác nhận đổi\n\n|6|" + Util.soCham(m[0]) + " VNĐ → "
                + Util.soCham(m[1]) + " Thỏi Vàng\n\n|0|Số dư: "
                + Util.soCham(pl.getSession() == null ? 0 : pl.getSession().vnd) + " VNĐ",
                "Đồng ý", "Huỷ");
    }

    /** Người chơi bấm Đồng ý / Huỷ ở bước xác nhận. */
    public void xacNhan(Player pl, int select) {
        long[] m = choXacNhan.remove(pl.id);
        if (m == null || select != 0) {
            return;
        }
        synchronized (pl) {
            if (!ConfigDAO.on(ConfigDAO.DOI_TV_BAT)) {
                Service.gI().sendThongBao(pl, "Chức năng đổi Thỏi Vàng đang tạm đóng.");
                return;
            }
            if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
                Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
                return;
            }
            if (pl.getSession() == null || pl.getSession().vnd < m[0]) {
                Service.gI().sendThongBao(pl, "Số dư không đủ, cần " + Util.soCham(m[0]) + " VNĐ.");
                return;
            }
            // Kiem ket qua tru so du: truoc day bo qua, tru loi van phat vang.
            if (!nro.repository.schema.DatabaseUpdater.subVND_byPlayer(pl, (int) m[0])) {
                Service.gI().sendThongBao(pl, "Không trừ được số dư, thử lại sau.");
                return;
            }
            try {
                Item tv = ItemService.gI().createNewItem(THOI_VANG, (int) m[1]);
                tv.quantity = (int) m[1];
                InventoryService.gI().addItemBag(pl, tv);
                InventoryService.gI().sendItemBag(pl);
                Service.gI().sendThongBao(pl, "Đã đổi " + Util.soCham(m[0]) + " VNĐ lấy "
                        + Util.soCham(m[1]) + " Thỏi Vàng.");
            } catch (Exception ex) {
                Logger.logException(DoiThoiVangService.class, ex, "ĐỔI TV: đã trừ " + m[0]
                        + " VNĐ của " + pl.name + " (id " + pl.id + ") nhưng phát vàng lỗi");
            }
        }
    }
}
