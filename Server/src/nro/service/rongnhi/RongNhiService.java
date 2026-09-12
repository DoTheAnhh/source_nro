package nro.service.rongnhi;

import java.util.List;
import nro.core.consts.ConstNpc;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.repository.dao.RongNhiDAO;
import nro.service.NpcService;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Trứng rồng nhí: mở trứng ra rồng, và đổi mảnh lấy trứng.
 *
 * <h2>Không có gì viết cứng</h2>
 *
 * <p>Ra con gì, chỉ số bao nhiêu, vĩnh viễn hay bao nhiêu ngày, đổi mấy mảnh
 * lấy một quả — tất cả đọc từ bảng trên panel ({@link RongNhiDAO}). Chưa khai
 * thì mở trứng báo "chưa khai" chứ không lặng lẽ rơi ra món lạ.</p>
 *
 * <h2>Hai lần bốc tách rời nhau</h2>
 *
 * <p>Bốc <b>loại rồng</b> là một lần gieo trên cả nhóm (tổng tỉ lệ chia theo
 * tổng thật), còn mỗi <b>dòng chỉ số</b> lại gieo riêng theo tỉ lệ của nó. Hai
 * việc khác nhau: con nào nở ra thì chỉ một, còn chỉ số thì có bao nhiêu dòng
 * trúng là hiện bấy nhiêu.</p>
 */
public class RongNhiService {

    private static RongNhiService instance;

    public static RongNhiService gI() {
        if (instance == null) {
            instance = new RongNhiService();
        }
        return instance;
    }

    /** Loại trứng người chơi đang chọn đổi, giữ giữa hai bảng menu. */
    private final java.util.Map<Long, Integer> dangChonTrung
            = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Người chơi vừa dùng một món — có phải trứng hay mảnh trứng không.
     *
     * @return {@code true} nếu đã xử lý, bên gọi không cần làm gì thêm
     */
    public boolean dungVatPham(Player pl, Item item) {
        if (pl == null || item == null || !item.isNotNullItem()) {
            return false;
        }
        int id = item.template.id;
        int trungThuong = (int) RongNhiDAO.so(RongNhiDAO.K_TRUNG_THUONG, 1879);
        int trungVang = (int) RongNhiDAO.so(RongNhiDAO.K_TRUNG_VANG, 1880);
        int manh = (int) RongNhiDAO.so(RongNhiDAO.K_MANH, 0);
        if (id == trungThuong) {
            moTrung(pl, item, RongNhiDAO.TU_TRUNG_THUONG);
            return true;
        }
        if (id == trungVang) {
            moTrung(pl, item, RongNhiDAO.TU_TRUNG_VANG);
            return true;
        }
        if (manh > 0 && id == manh) {
            moMenuDoi(pl);
            return true;
        }
        return false;
    }

    /* =========================
     * MỞ TRỨNG
     * ========================= */
    private void moTrung(Player pl, Item trung, int loaiTrung) {
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        List<RongNhiDAO.Loai> ds = RongNhiDAO.dsLoaiCuaTrung(loaiTrung);
        if (ds.isEmpty()) {
            Service.gI().sendThongBao(pl,
                    "Trứng này chưa khai loại rồng nhí nào — báo quản trị.");
            return;
        }
        RongNhiDAO.Loai chon = boc(ds);
        if (chon == null) {
            Service.gI().sendThongBao(pl,
                    "Không bốc được loại rồng nhí nào — báo quản trị.");
            return;
        }
        Item con = ItemService.gI().createNewItem((short) chon.itemId);
        if (con == null || con.template == null) {
            Service.gI().sendThongBao(pl,
                    "Vật phẩm rồng nhí khai sai id (" + chon.itemId + ").");
            return;
        }
        con.quantity = 1;
        for (RongNhiDAO.ChiSo cs : RongNhiDAO.dsChiSo(chon.id, true)) {
            if (cs.tiLe <= 0 || !Util.isTrue(cs.tiLe, 100d)) {
                continue;
            }
            int giaTri = (cs.max > cs.min) ? Util.nextInt(cs.min, cs.max) : cs.min;
            con.itemOptions.add(new ItemOption(cs.optionId, giaTri));
        }
        boolean vinhVien = chon.tiLeVinhVien > 0
                && Util.isTrue(chon.tiLeVinhVien, 100d);
        if (!vinhVien) {
            int ngay = (chon.ngayMax > chon.ngayMin)
                    ? Util.nextInt(chon.ngayMin, chon.ngayMax) : chon.ngayMin;
            if (ngay > 0) {
                // Chi so 93 la "Han su dung: # ngay" — cung chi so ma bang don do
                // het han doc, nen do vua no ra da nam dung trong danh sach ay.
                con.itemOptions.add(new ItemOption(93, ngay));
            }
        }
        if (chon.khoa) {
            con.itemOptions.add(new ItemOption(30, 0));
        }
        InventoryService.gI().subQuantityItemsBag(pl, trung, 1);
        InventoryService.gI().addItemBag(pl, con);
        InventoryService.gI().sendItemBag(pl);
        nro.service.combine.CombineService.gI().sendEffectOpenItem(pl,
                trung.template.iconID, con.template.iconID);
        Service.gI().sendThongBao(pl, "Trứng nở ra " + con.template.name
                + (vinhVien ? " (vĩnh viễn)" : "") + "!");
    }

    /**
     * Bốc một loại theo tỉ lệ khai trên panel.
     *
     * <p>Chia theo <b>tổng thật</b> chứ không bắt tổng phải đúng 100: quản trị
     * khai ba loại 10/20/30 thì hiểu là 1/6, 2/6, 3/6 — khỏi phải ngồi tính.</p>
     */
    private RongNhiDAO.Loai boc(List<RongNhiDAO.Loai> ds) {
        double tong = 0d;
        for (RongNhiDAO.Loai x : ds) {
            tong += x.tiLe;
        }
        if (tong <= 0) {
            return null;
        }
        double diem = Util.nextDouble(tong);
        double cong = 0d;
        RongNhiDAO.Loai cuoi = null;
        for (RongNhiDAO.Loai x : ds) {
            cong += x.tiLe;
            cuoi = x;
            if (diem < cong) {
                return x;
            }
        }
        return cuoi;
    }

    /* =========================
     * ĐỔI MẢNH LẤY TRỨNG
     * ========================= */
    private void moMenuDoi(Player pl) {
        int manh = (int) RongNhiDAO.so(RongNhiDAO.K_MANH, 0);
        int giaThuong = (int) RongNhiDAO.so(RongNhiDAO.K_MANH_THUONG, 49);
        int giaVang = (int) RongNhiDAO.so(RongNhiDAO.K_MANH_VANG, 99);
        int dangCo = soManh(pl, manh);
        String text = "|1|Mảnh trứng rồng nhí\n"
                + "|0|Đang có " + dangCo + " mảnh\n"
                + "|2|" + giaThuong + " mảnh = 1 Trứng rồng nhí\n"
                + "|2|" + giaVang + " mảnh = 1 Trứng vàng rồng nhí";
        NpcService.gI().createMenuConMeo(pl, ConstNpc.DOI_TRUNG_RONG_NHI, -1, text,
                "Trứng\nthường", "Trứng\nvàng", "Từ chối");
    }

    /** Chọn loại trứng muốn đổi. */
    public void chonLoai(Player pl, int select) {
        if (select == 0) {
            dangChonTrung.put(pl.id, RongNhiDAO.TU_TRUNG_THUONG);
        } else if (select == 1) {
            dangChonTrung.put(pl.id, RongNhiDAO.TU_TRUNG_VANG);
        } else {
            Service.gI().sendThongBao(pl, "Đã huỷ.");
            return;
        }
        int gia = giaHienTai(pl);
        int dangCo = soManh(pl, (int) RongNhiDAO.so(RongNhiDAO.K_MANH, 0));
        int toiDa = (gia > 0) ? dangCo / gia : 0;
        String text = "|1|Đổi " + tenTrungDangChon(pl) + "\n"
                + "|0|Mỗi quả cần " + gia + " mảnh\n"
                + "|0|Đang có " + dangCo + " mảnh — đổi được tối đa " + toiDa + " quả";
        NpcService.gI().createMenuConMeo(pl, ConstNpc.DOI_TRUNG_RONG_NHI_SO_LUONG,
                -1, text, "x1", "x10", "Đổi\ntối đa", "Từ chối");
    }

    /** Chọn số lượng rồi đổi thật. */
    public void chonSoLuong(Player pl, int select) {
        int gia = giaHienTai(pl);
        int manh = (int) RongNhiDAO.so(RongNhiDAO.K_MANH, 0);
        int dangCo = soManh(pl, manh);
        int toiDa = (gia > 0) ? dangCo / gia : 0;
        int muon;
        switch (select) {
            case 0:
                muon = 1;
                break;
            case 1:
                muon = 10;
                break;
            case 2:
                muon = toiDa;
                break;
            default:
                Service.gI().sendThongBao(pl, "Đã huỷ.");
                return;
        }
        doi(pl, Math.min(muon, toiDa));
    }

    /**
     * Đổi thật.
     *
     * <p>Thiếu thì đổi hết chỗ có chứ không từ chối — cùng luật với phép nhập
     * Ngọc Rồng, để bấm "x10" lúc chỉ đủ 3 quả không thành ra mất công.</p>
     */
    private void doi(Player pl, int soQua) {
        if (soQua <= 0) {
            Service.gI().sendThongBao(pl, "Không đủ mảnh để đổi.");
            return;
        }
        int troLai = InventoryService.gI().getCountEmptyBag(pl);
        if (troLai <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        int gia = giaHienTai(pl);
        int idTrung = idTrungDangChon(pl);
        int manh = (int) RongNhiDAO.so(RongNhiDAO.K_MANH, 0);
        if (gia <= 0 || idTrung <= 0 || manh <= 0) {
            Service.gI().sendThongBao(pl, "Chưa khai đủ cấu hình trứng — báo quản trị.");
            return;
        }
        Item oManh = InventoryService.gI().findItemBag(pl, manh);
        if (oManh == null || soManh(pl, manh) < gia * soQua) {
            Service.gI().sendThongBao(pl, "Không đủ mảnh trứng rồng nhí.");
            return;
        }
        Item trung = ItemService.gI().createNewItem((short) idTrung);
        if (trung == null || trung.template == null) {
            Service.gI().sendThongBao(pl, "Id trứng khai sai — báo quản trị.");
            return;
        }
        trung.quantity = soQua;
        InventoryService.gI().subQuantityItemsBag(pl, oManh, gia * soQua);
        InventoryService.gI().addItemBag(pl, trung);
        InventoryService.gI().sendItemBag(pl);
        Service.gI().sendThongBao(pl, "Đã đổi " + soQua + " "
                + trung.template.name + ".");
    }

    private int giaHienTai(Player pl) {
        Integer loai = dangChonTrung.get(pl.id);
        if (loai != null && loai == RongNhiDAO.TU_TRUNG_VANG) {
            return (int) RongNhiDAO.so(RongNhiDAO.K_MANH_VANG, 99);
        }
        return (int) RongNhiDAO.so(RongNhiDAO.K_MANH_THUONG, 49);
    }

    private int idTrungDangChon(Player pl) {
        Integer loai = dangChonTrung.get(pl.id);
        if (loai != null && loai == RongNhiDAO.TU_TRUNG_VANG) {
            return (int) RongNhiDAO.so(RongNhiDAO.K_TRUNG_VANG, 1880);
        }
        return (int) RongNhiDAO.so(RongNhiDAO.K_TRUNG_THUONG, 1879);
    }

    private String tenTrungDangChon(Player pl) {
        Integer loai = dangChonTrung.get(pl.id);
        return (loai != null && loai == RongNhiDAO.TU_TRUNG_VANG)
                ? "Trứng vàng rồng nhí" : "Trứng rồng nhí";
    }

    /**
     * Số mảnh thật đang có.
     *
     * <p>Đếm bằng {@code soLuongThat} vì vài món gộp số lượng vào dòng chỉ số
     * "Số lượng #" chứ không vào {@code quantity} — đọc thẳng {@code quantity}
     * với những món ấy thì lúc nào cũng ra 1.</p>
     */
    private int soManh(Player pl, int idManh) {
        if (idManh <= 0) {
            return 0;
        }
        int tong = 0;
        try {
            for (Item it : pl.inventory.itemsBag) {
                if (it != null && it.isNotNullItem() && it.template.id == idManh) {
                    tong += InventoryService.soLuongThat(it);
                }
            }
        } catch (Exception ex) {
            Logger.logException(RongNhiService.class, ex);
        }
        return tong;
    }
}
