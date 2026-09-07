package nro.service.event;

import java.util.ArrayList;
import java.util.List;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.repository.dao.SuKienDAO;
import nro.repository.dao.SuKienDAO.CongThucDoi;
import nro.repository.dao.SuKienDAO.PhanThuongHop;
import nro.repository.dao.SuKienDAO.SuKien;
import nro.repository.dao.SuKienDAO.VatPhamRoi;
import nro.server.ServerNotify;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Sự kiện đang chạy: rơi vật phẩm từ quái, đổi quà ở Quy Lão, mở gói.
 *
 * <h2>Vì sao có bộ nhớ đệm</h2>
 *
 * <p>{@link #roiTuQuai(Player)} được hỏi <b>mỗi lần một con quái chết</b>. Đọc
 * CSDL ở đó sẽ chặn luồng game, đúng như lý do {@code ConfigDAO} có
 * {@code CACHE}. Nên đọc một lần vào bộ nhớ rồi dùng lại; panel gọi
 * {@link #napLai()} sau mỗi lần lưu nên thay đổi có hiệu lực ngay, <b>không cần
 * khởi động lại máy chủ</b>.</p>
 *
 * <p>Khung giờ thì không cần nạp lại: {@link SuKien#dangChay()} so với đồng hồ
 * ngay lúc hỏi, nên sự kiện hẹn 19:00 tự bật đúng 19:00 mà không ai phải bấm
 * gì, và hết giờ tự ngừng rơi.</p>
 *
 * <h2>Sự kiện tắt là không rơi gì cả</h2>
 *
 * <p>Mọi đường đều đi qua {@link SuKien#dangChay()} — cả rơi từ quái lẫn mục
 * đổi ở Quy Lão. Tắt công tắc là quái ngừng rơi và mục đổi biến mất khỏi menu
 * ngay lập tức.</p>
 */
public final class SuKienService {

    private SuKienService() {
    }

    private static volatile List<SuKien> SU_KIEN;
    private static volatile List<VatPhamRoi> ROI;
    private static volatile List<CongThucDoi> DOI;
    private static volatile List<PhanThuongHop> HOP;

    /** Lần loa gần nhất của mỗi sự kiện, khoá theo id. */
    private static final java.util.Map<Integer, Long> LAN_LOA =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** Các sự kiện đã loa câu mở màn, để không loa lại mỗi vòng lặp. */
    private static final java.util.Set<Integer> DA_MO =
            java.util.Collections.newSetFromMap(
                    new java.util.concurrent.ConcurrentHashMap<Integer, Boolean>());

    /** Đọc lại cả bốn bảng. Panel gọi sau mỗi lần lưu. */
    public static synchronized void napLai() {
        SU_KIEN = SuKienDAO.danhSach();
        ROI = SuKienDAO.danhSachRoi(-1);
        DOI = SuKienDAO.danhSachDoi(-1);
        HOP = SuKienDAO.danhSachHop(-1);
    }

    private static void chacChanDaNap() {
        if (SU_KIEN == null) {
            napLai();
        }
    }

    /** Các sự kiện đang bật <b>và</b> đang trong khung giờ. */
    public static List<SuKien> dangChay() {
        chacChanDaNap();
        List<SuKien> ds = new ArrayList<>();
        for (SuKien s : SU_KIEN) {
            if (s.dangChay()) {
                ds.add(s);
            }
        }
        return ds;
    }

    private static boolean suKienDangChay(int suKienId) {
        chacChanDaNap();
        for (SuKien s : SU_KIEN) {
            if (s.id == suKienId) {
                return s.dangChay();
            }
        }
        return false;
    }

    // ==================================================================
    //  Rơi từ quái
    // ==================================================================

    /**
     * Vật phẩm sự kiện rơi ra khi một con quái chết.
     *
     * @return danh sách {@code {idVatPham, soLuong}}, rỗng khi không sự kiện
     *         nào đang chạy hoặc không món nào trúng tỉ lệ
     */
    public static List<int[]> roiTuQuai(Player player) {
        List<int[]> kq = new ArrayList<>();
        chacChanDaNap();
        if (ROI.isEmpty()) {
            return kq;
        }
        // Đệ tử / bố / mẹ đánh hộ thì phần rơi ghi cho chủ, không nhân đôi.
        if (player == null || player.isBoss) {
            return kq;
        }
        int mapId = -1;
        int hanhTinh = -1;
        String tenMap = "";
        if (player.zone != null && player.zone.map != null) {
            mapId = player.zone.map.mapId;
            nro.entity.template.MapTemplate mt = mauMap(mapId);
            if (mt != null) {
                hanhTinh = mt.planetId;
                tenMap = mt.name;
            }
        }
        for (VatPhamRoi v : ROI) {
            if (!v.bat || v.tiLe <= 0) {
                continue;
            }
            if (!suKienDangChay(v.suKienId)) {
                continue;
            }
            // Loc ban do TRUOC khi gieo tỉ lệ. Gieo truoc roi moi loc thi mon
            // nay se an mat lan gieo cua no o map khong duoc phep — khong sai
            // ket qua, nhung phi va kho lan khi soi log.
            if (!v.roiOMap(mapId, hanhTinh, tenMap)) {
                continue;
            }
            if (!trung(v.tiLe)) {
                continue;
            }
            int sl = v.soLuongMax > v.soLuongMin
                    ? Util.nextInt(v.soLuongMin, v.soLuongMax) : v.soLuongMin;
            if (sl < 1) {
                sl = 1;
            }
            kq.add(new int[]{v.itemId, sl});
        }
        return kq;
    }

    /**
     * Tung một lần theo tỉ lệ phần trăm, giữ được cả phần thập phân.
     *
     * <p>Nhân lên 10.000 rồi mới so: {@code Util.isTrue(1, 100)} chỉ nhận số
     * nguyên, nên tỉ lệ 0,5% mà làm tròn thành 1% là <b>gấp đôi</b> con số
     * admin đặt. Đây đúng là cái bẫy đã làm hệ số rơi đồ chết lặng một lần rồi.</p>
     */
    private static boolean trung(double phanTram) {
        if (phanTram >= 100) {
            return true;
        }
        long tuSo = Math.round(phanTram * 10_000d);
        if (tuSo <= 0) {
            return false;
        }
        return Util.isTrue((int) tuSo, 1_000_000);
    }

    // ==================================================================
    //  Đổi quà ở Quy Lão
    // ==================================================================

    /** Các công thức đổi đang dùng được — chỉ của sự kiện đang chạy. */
    public static List<CongThucDoi> congThucDangCo() {
        chacChanDaNap();
        List<CongThucDoi> ds = new ArrayList<>();
        for (CongThucDoi c : DOI) {
            if (c.bat && suKienDangChay(c.suKienId)) {
                ds.add(c);
            }
        }
        return ds;
    }

    /**
     * Mẫu bản đồ theo id, hoặc {@code null} nếu chưa nạp xong.
     *
     * <p>{@code MAP_TEMPLATES} đánh chỉ số theo <b>vị trí</b> chứ không theo id,
     * nên phải dò chứ không tra thẳng được. Danh sách chỉ hơn hai trăm dòng và
     * hàm này chỉ chạy khi một con quái chết, nên dò tuyến tính là đủ.</p>
     */
    private static nro.entity.template.MapTemplate mauMap(int mapId) {
        nro.entity.template.MapTemplate[] ds = nro.server.Manager.MAP_TEMPLATES;
        if (ds == null) {
            return null;
        }
        for (nro.entity.template.MapTemplate m : ds) {
            if (m != null && m.id == mapId) {
                return m;
            }
        }
        return null;
    }

    /** Số món {@code itemId} người chơi đang có trong hành trang. */
    private static int dangCo(Player player, int itemId) {
        Item co = InventoryService.gI().findItemBag(player, itemId);
        return co == null ? 0 : co.quantity;
    }

    /** Liệt kê nguyên liệu kiểu {@code 5 Pháo bông + 2 Bút chì}. */
    private static String keNguyenLieu(CongThucDoi c) {
        StringBuilder sb = new StringBuilder();
        for (int[] cap : c.nguyenLieu) {
            if (sb.length() > 0) {
                sb.append(" + ");
            }
            sb.append(cap[1]).append(' ').append(SuKienDAO.tenVatPham(cap[0]));
        }
        return sb.toString();
    }

    /** Chữ hiện trên nút menu Quy Lão. */
    public static String tenNut(CongThucDoi c) {
        if (c.ten != null && !c.ten.trim().isEmpty()) {
            return c.ten;
        }
        return keNguyenLieu(c)
                + "\n→ " + c.soLuongNhan + " " + SuKienDAO.tenVatPham(c.itemNhan);
    }

    /**
     * Dòng mô tả một công thức, hiện trong lời thoại Quy Lão.
     *
     * <p>Xanh khi đủ <b>toàn bộ</b> nguyên liệu. Mỗi món kèm số đang có để
     * người chơi biết còn thiếu món nào mà không phải mở hành trang ra đếm.</p>
     */
    public static String moTaCongThuc(CongThucDoi c, Player player) {
        StringBuilder ke = new StringBuilder();
        boolean du = true;
        for (int[] cap : c.nguyenLieu) {
            int co = dangCo(player, cap[0]);
            if (co < cap[1]) {
                du = false;
            }
            if (ke.length() > 0) {
                ke.append(" + ");
            }
            ke.append(cap[1]).append(' ').append(SuKienDAO.tenVatPham(cap[0]))
                    .append(" (có ").append(co).append(')');
        }
        return "|" + (du ? "1" : "0") + "|" + ke
                + "  →  " + c.soLuongNhan + " " + SuKienDAO.tenVatPham(c.itemNhan);
    }

    /**
     * Thực hiện một lần đổi.
     *
     * <p>Kiểm tra <b>đủ mọi nguyên liệu</b> và ô trống <b>trước</b> khi trừ bất
     * cứ thứ gì: trừ được ba món rồi mới phát hiện thiếu món thứ tư là người
     * chơi mất trắng ba món đầu, và không có cách nào đòi lại.</p>
     *
     * @return câu báo cho người chơi
     */
    public static String doiQua(Player player, int congThucId) {
        chacChanDaNap();
        CongThucDoi c = null;
        for (CongThucDoi x : congThucDangCo()) {
            if (x.id == congThucId) {
                c = x;
                break;
            }
        }
        if (c == null) {
            return "Mục đổi này không còn nữa.";
        }
        if (c.nguyenLieu.isEmpty()) {
            return "Mục đổi này chưa khai nguyên liệu — báo lại quản trị.";
        }

        // Vong 1: chi kiem tra, chua dong vao hanh trang.
        List<Item> sePhaiTru = new ArrayList<>();
        for (int[] cap : c.nguyenLieu) {
            Item co = InventoryService.gI().findItemBag(player, cap[0]);
            if (co == null || co.quantity < cap[1]) {
                return "Bạn cần " + cap[1] + " " + SuKienDAO.tenVatPham(cap[0])
                        + ", đang có " + (co == null ? 0 : co.quantity) + ".";
            }
            sePhaiTru.add(co);
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            return "Hành trang đã đầy, hãy dọn bớt một ô rồi đổi lại.";
        }
        Item nhan = ItemService.gI().createNewItem((short) c.itemNhan, c.soLuongNhan);
        if (nhan == null || nhan.template == null) {
            return "Vật phẩm nhận về không tồn tại — báo lại quản trị.";
        }

        // Vong 2: tu day tro di khong con duong lui, moi thu da chac chan.
        for (int i = 0; i < c.nguyenLieu.size(); i++) {
            InventoryService.gI().subQuantityItemsBag(player, sePhaiTru.get(i),
                    c.nguyenLieu.get(i)[1]);
        }
        InventoryService.gI().addItemBag(player, nhan);
        InventoryService.gI().sendItemBag(player);
        return "Bạn nhận được " + c.soLuongNhan + " " + SuKienDAO.tenVatPham(c.itemNhan) + ".";
    }

    // ==================================================================
    //  Mở gói
    // ==================================================================

    /** {@code true} nếu vật phẩm này có khai nội dung gói trong panel. */
    public static boolean laHop(int itemId) {
        chacChanDaNap();
        for (PhanThuongHop p : HOP) {
            if (p.hopItemId == itemId && p.bat) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mở một gói: trừ một cái, bốc một phần thưởng theo trọng số.
     *
     * @return {@code true} nếu đã mở, {@code false} nếu gói này không có nội
     *         dung (chỗ gọi cứ để mã cũ xử lý tiếp)
     */
    public static boolean moHop(Player player, Item hop) {
        if (player == null || hop == null || hop.template == null) {
            return false;
        }
        chacChanDaNap();
        List<PhanThuongHop> pool = new ArrayList<>();
        long tong = 0;
        for (PhanThuongHop p : HOP) {
            if (p.hopItemId == hop.template.id && p.bat && p.trongSo > 0) {
                pool.add(p);
                tong += p.trongSo;
            }
        }
        if (pool.isEmpty() || tong <= 0) {
            return false;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player,
                    "Hành trang đã đầy, hãy dọn bớt một ô rồi mở gói.");
            // Van coi la da xu ly: neu tra false thi ma cu se coi la vat pham
            // khong dung duoc, con nguoi choi thi khong hieu vi sao.
            return true;
        }
        long moc = (long) Math.floor(Math.random() * tong);
        PhanThuongHop chon = pool.get(pool.size() - 1);
        long dem = 0;
        for (PhanThuongHop p : pool) {
            dem += p.trongSo;
            if (moc < dem) {
                chon = p;
                break;
            }
        }
        int sl = chon.soLuongMax > chon.soLuongMin
                ? Util.nextInt(chon.soLuongMin, chon.soLuongMax) : chon.soLuongMin;
        if (sl < 1) {
            sl = 1;
        }
        Item thuong = ItemService.gI().createNewItem((short) chon.itemId, sl);
        if (thuong == null || thuong.template == null) {
            Service.gI().sendThongBao(player,
                    "Phần thưởng trong gói không tồn tại — báo lại quản trị.");
            return true;
        }
        InventoryService.gI().subQuantityItemsBag(player, hop, 1);
        InventoryService.gI().addItemBag(player, thuong);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendThongBao(player, "Bạn mở được " + sl + " "
                + SuKienDAO.tenVatPham(chon.itemId) + "!");
        return true;
    }

    // ==================================================================
    //  Thông báo
    // ==================================================================

    /**
     * Loa câu mở màn và câu nhắc lại. Gọi đều đặn từ {@link ServerNotify}.
     *
     * <p>Ghép vào vòng lặp sẵn có thay vì dựng thread riêng: máy chủ này đã
     * khởi tạo khoảng năm mươi thread thô, thêm một cái nữa chỉ để ngủ 1,5 giây
     * là thừa.</p>
     */
    public static void tick() {
        try {
            chacChanDaNap();
            long now = System.currentTimeMillis();
            for (SuKien s : SU_KIEN) {
                if (!s.dangChay()) {
                    // Het gio thi quen di, de lan sau bat lai con loa cau mo man.
                    DA_MO.remove(s.id);
                    LAN_LOA.remove(s.id);
                    continue;
                }
                if (s.thongBao == null || s.thongBao.trim().isEmpty()) {
                    continue;
                }
                if (DA_MO.add(s.id)) {
                    loa(s.thongBao);
                    LAN_LOA.put(s.id, now);
                    continue;
                }
                if (s.phutNhac <= 0) {
                    continue;
                }
                Long truoc = LAN_LOA.get(s.id);
                if (truoc == null || now - truoc >= s.phutNhac * 60_000L) {
                    loa(s.thongBao);
                    LAN_LOA.put(s.id, now);
                }
            }
        } catch (Exception ignored) {
            // Vong lap loa khong duoc phep lam chet thread thong bao chung.
        }
    }

    private static void loa(String text) {
        try {
            ServerNotify.gI().notify(text);
        } catch (Exception ignored) {
        }
    }

    /**
     * Báo cho một người vừa vào game biết sự kiện nào đang chạy.
     *
     * <p>Người vào giữa chừng không nghe được câu loa lúc mở màn, nên nếu không
     * có dòng này thì với họ sự kiện là vô hình.</p>
     */
    public static void chaoNguoiChoi(Player player) {
        if (player == null) {
            return;
        }
        try {
            for (SuKien s : dangChay()) {
                String text = s.thongBao == null || s.thongBao.trim().isEmpty()
                        ? "Sự kiện " + s.ten + " đang diễn ra!" : s.thongBao;
                Service.gI().sendThongBao(player, text);
            }
        } catch (Exception ignored) {
        }
    }

    /** Một dòng tóm tắt cho panel: đang chạy cái gì, rơi những món nào. */
    public static String tomTat() {
        List<SuKien> ds = dangChay();
        if (ds.isEmpty()) {
            return "Không có sự kiện nào đang chạy — quái không rơi vật phẩm sự kiện.";
        }
        StringBuilder sb = new StringBuilder("Đang chạy: ");
        for (int i = 0; i < ds.size(); i++) {
            sb.append(i > 0 ? ", " : "").append(ds.get(i).ten);
        }
        int soMon = 0;
        for (VatPhamRoi v : ROI) {
            if (v.bat && v.tiLe > 0 && suKienDangChay(v.suKienId)) {
                soMon++;
            }
        }
        sb.append("   —   ").append(soMon).append(" món đang rơi từ quái, ")
                .append(congThucDangCo().size()).append(" mục đổi ở Quy Lão.");
        return sb.toString();
    }
}
