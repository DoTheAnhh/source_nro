package nro.service;

import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.repository.dao.PhucLoiDAO;
import nro.repository.dao.TheThangDAO;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * <b>Thẻ tháng</b> — mua một lần, dùng 30 ngày, mỗi ngày vào nhận quà.
 *
 * <h2>Nối vào trường có sẵn</h2>
 *
 * <p>Người chơi đã có {@code THE_THANG} (1 thường, 2 cao cấp) và
 * {@code LASTTIME_THE_THANG}; {@code NPoint} đã cộng ưu đãi khi hai trường ấy
 * còn hạn. Chỉ là trước đây không có chỗ nào bán thẻ. Lớp này chỉ việc đặt
 * đúng hai trường ấy — không dựng cơ chế ưu đãi thứ hai chạy song song.</p>
 *
 * <p>Giao diện nằm trong màn Phúc lợi, đi chung gói {@code -58}: dữ liệu thẻ
 * nối vào <b>cuối</b> gói phúc lợi, client cũ đọc xong phần của nó thì dừng.</p>
 */
public class TheThangService {

    private static TheThangService instance;

    public static TheThangService gI() {
        if (instance == null) {
            instance = new TheThangService();
        }
        return instance;
    }

    /** Việc client gửi lên trong gói -58 (0, 1 là của phúc lợi). */
    public static final byte VIEC_MUA = 2;
    public static final byte VIEC_NHAN_NGAY = 3;

    // =====================================================================
    //  Trạng thái
    // =====================================================================
    /** Thẻ còn hạn không. */
    public boolean conHan(Player pl) {
        return pl != null && pl.THE_THANG != 0
                && pl.LASTTIME_THE_THANG > System.currentTimeMillis();
    }

    /** Số ngày còn lại, làm tròn lên: còn 1 giờ vẫn là "còn 1 ngày". */
    public int soNgayCon(Player pl) {
        if (!conHan(pl)) {
            return 0;
        }
        long con = pl.LASTTIME_THE_THANG - System.currentTimeMillis();
        return (int) ((con + TheThangDAO.MOT_NGAY_MS - 1) / TheThangDAO.MOT_NGAY_MS);
    }

    /**
     * Nạp thẻ từ bảng vào người chơi — gọi ngay sau khi đọc dữ liệu nhân vật.
     *
     * <p>Bảng {@code the_thang_nguoi} là nguồn sự thật: dữ liệu nhân vật chỉ lưu
     * theo nhịp, nên sau một lần sập máy nó có thể cũ hơn bảng.</p>
     */
    public void dongBoKhiNap(Player pl) {
        if (pl == null) {
            return;
        }
        try {
            TheThangDAO.The t = TheThangDAO.theCua(pl.id);
            if (t != null) {
                pl.THE_THANG = t.bac;
                pl.LASTTIME_THE_THANG = t.hetHan;
            }
        } catch (Exception ex) {
            Logger.logException(TheThangService.class, ex, "Không nạp được thẻ tháng");
        }
    }

    // =====================================================================
    //  Mua
    // =====================================================================
    /**
     * Mua (hoặc gia hạn) thẻ bậc {@code bac}.
     *
     * <p>Khoá trên người chơi: bấm hai lần liền tay thì lần hai phải thấy tiền
     * đã trừ của lần một, không thì trừ một lần mà cộng hai lần ngày.</p>
     *
     * <p>Đang dùng bậc khác còn hạn thì <b>không</b> cho mua: đổi bậc giữa
     * chừng thì số ngày còn lại của bậc cũ tính sao cũng có người thấy thiệt.
     * Nói rõ lý do, chờ hết hạn rồi mua.</p>
     */
    public void mua(Player pl, int bac) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        synchronized (pl) {
            TheThangDAO.Goi g = TheThangDAO.goi(bac);
            if (g == null || !g.bat) {
                Service.gI().sendThongBao(pl, "Gói thẻ này hiện không bán.");
                return;
            }
            boolean conHan = conHan(pl);
            if (conHan && pl.THE_THANG != bac) {
                Service.gI().sendThongBao(pl, "Bạn đang dùng " + TheThangDAO.tenBac(pl.THE_THANG)
                        + ", còn " + soNgayCon(pl) + " ngày. Hết hạn mới mua được gói khác.");
                return;
            }
            List<TheThangDAO.Qua> qua = TheThangDAO.dsQua(bac, TheThangDAO.QUA_MUA);
            if (InventoryService.gI().getCountEmptyBag(pl) < qua.size()) {
                Service.gI().sendThongBao(pl, "Hành trang cần trống " + qua.size() + " ô để nhận quà.");
                return;
            }
            if (!truTien(pl, g)) {
                return;
            }
            long moc = conHan ? pl.LASTTIME_THE_THANG : System.currentTimeMillis();
            long hetHan = moc + g.soNgay * TheThangDAO.MOT_NGAY_MS;
            // Dat vao nguoi choi TRUOC khi ghi bang: tien da tru roi, ghi bang
            // loi thi it nhat nguoi choi van co the cho toi lan luu du lieu.
            pl.THE_THANG = bac;
            pl.LASTTIME_THE_THANG = hetHan;
            if (!TheThangDAO.datThe(pl.id, bac, hetHan, true)) {
                Logger.error("THẺ THÁNG: đã trừ tiền nhưng KHÔNG ghi được bảng cho "
                        + pl.name + " (id " + pl.id + "), bậc " + bac + ", hết hạn " + hetHan + "\n");
            }
            phatQua(pl, qua);
            capNhatChiSo(pl);
            Service.gI().sendThongBao(pl, (conHan ? "Đã gia hạn " : "Đã mua ") + g.ten
                    + " — " + g.soNgay + " ngày, còn " + soNgayCon(pl) + " ngày.");
            PhucLoiService.gI().guiDuLieu(pl);
        }
    }

    /** Trừ tiền theo loại tiền của gói; không đủ thì báo và trả {@code false}. */
    private boolean truTien(Player pl, TheThangDAO.Goi g) {
        long gia = Math.max(0, g.gia);
        switch (g.loaiTien == null ? TheThangDAO.TIEN_VND : g.loaiTien) {
            case TheThangDAO.TIEN_HONG_NGOC:
                if (pl.inventory.ruby < gia) {
                    Service.gI().sendThongBao(pl, "Không đủ hồng ngọc, cần " + Util.soCham(gia) + ".");
                    return false;
                }
                pl.inventory.subRuby((int) gia);
                Service.gI().sendMoney(pl);
                return true;
            case TheThangDAO.TIEN_NGOC:
                if (pl.inventory.gem < gia) {
                    Service.gI().sendThongBao(pl, "Không đủ ngọc, cần " + Util.soCham(gia) + ".");
                    return false;
                }
                pl.inventory.subGem((int) gia);
                Service.gI().sendMoney(pl);
                return true;
            default:
                if (pl.getSession() == null) {
                    return false;
                }
                if (pl.getSession().vnd < gia) {
                    Service.gI().sendThongBao(pl, "Số dư không đủ, cần " + Util.soCham(gia)
                            + " VNĐ, đang có " + Util.soCham(pl.getSession().vnd) + " VNĐ.");
                    return false;
                }
                if (!nro.repository.schema.DatabaseUpdater.subVND_byPlayer(pl, (int) gia)) {
                    Service.gI().sendThongBao(pl, "Không trừ được số dư, thử lại sau.");
                    return false;
                }
                return true;
        }
    }

    /** Tính lại chỉ số ngay: ưu đãi thẻ phải thấy liền, không đợi lần tính sau. */
    private void capNhatChiSo(Player pl) {
        try {
            pl.nPoint.calPoint();
            Service.gI().point(pl);
        } catch (Exception ex) {
            Logger.logException(TheThangService.class, ex, "Không tính lại được chỉ số sau thẻ tháng");
        }
    }

    // =====================================================================
    //  Nhận quà mỗi ngày
    // =====================================================================
    /**
     * Nhận quà hôm nay.
     *
     * <p>Ghi lượt nhận <b>trước</b> rồi mới phát: ghi sau mà phát lỗi thì bấm lại
     * nhận tiếp được, tức nhận vô hạn. Ô hành trang kiểm trước khi ghi nên
     * đường "ghi rồi mà không phát được" gần như không còn; lỡ gặp thì trả lại
     * lượt.</p>
     */
    public void nhanNgay(Player pl) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        synchronized (pl) {
            if (!conHan(pl)) {
                Service.gI().sendThongBao(pl, "Bạn chưa có thẻ tháng, hoặc thẻ đã hết hạn.");
                return;
            }
            List<TheThangDAO.Qua> qua = TheThangDAO.dsQua(pl.THE_THANG, TheThangDAO.QUA_NGAY);
            if (qua.isEmpty()) {
                Service.gI().sendThongBao(pl, "Gói thẻ này chưa gắn quà mỗi ngày.");
                return;
            }
            if (InventoryService.gI().getCountEmptyBag(pl) < qua.size()) {
                Service.gI().sendThongBao(pl, "Hành trang cần trống " + qua.size() + " ô.");
                return;
            }
            int ngay = PhucLoiDAO.ngayHomNay();
            if (!TheThangDAO.ghiNhan(pl.id, ngay)) {
                Service.gI().sendThongBao(pl, "Hôm nay bạn đã nhận quà thẻ tháng rồi.");
                return;
            }
            if (phatQua(pl, qua) == 0) {
                TheThangDAO.boNhan(pl.id, ngay);
                Service.gI().sendThongBao(pl, "Không phát được quà, thử lại sau.");
                return;
            }
            Service.gI().sendThongBao(pl, "Đã nhận quà thẻ tháng hôm nay. Còn "
                    + soNgayCon(pl) + " ngày.");
            PhucLoiService.gI().guiDuLieu(pl);
        }
    }

    /** Phát một danh sách quà vào hành trang; trả số món phát được. */
    private int phatQua(Player pl, List<TheThangDAO.Qua> qua) {
        int duoc = 0;
        for (TheThangDAO.Qua q : qua) {
            try {
                Item it = ItemService.gI().createNewItem((short) q.itemId, q.soLuong);
                if (it == null || it.template == null) {
                    continue;
                }
                it.quantity = q.soLuong;
                ganChiSo(it, q.chiSo);
                InventoryService.gI().addItemBag(pl, it);
                duoc++;
            } catch (Exception ex) {
                Logger.logException(TheThangService.class, ex, "Lỗi phát quà thẻ tháng item " + q.itemId);
            }
        }
        if (duoc > 0) {
            InventoryService.gI().sendItemBag(pl);
        }
        return duoc;
    }

    /** Chỉ số dạng "50=5,77=10" — cùng cách viết với quà phúc lợi. */
    private static void ganChiSo(Item it, String chuoi) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            return;
        }
        for (String phan : chuoi.split(",")) {
            String[] kv = phan.split("=");
            if (kv.length != 2) {
                continue;
            }
            try {
                it.itemOptions.add(new ItemOption(Integer.parseInt(kv[0].trim()),
                        Integer.parseInt(kv[1].trim())));
            } catch (NumberFormatException sai) {
                // Dong hong thi bo dong do.
            }
        }
    }

    // =====================================================================
    //  Quản trị tặng thẻ
    // =====================================================================
    /**
     * Cộng {@code soNgay} ngày thẻ bậc {@code bac} cho một nhân vật (đang chơi
     * hay không). Đang có bậc khác còn hạn thì thay bậc, tính ngày từ hôm nay.
     *
     * @return dòng kết quả để panel hiện
     */
    public String tangThe(long playerId, int bac, int soNgay) {
        long bayGio = System.currentTimeMillis();
        TheThangDAO.The cu = TheThangDAO.theCua(playerId);
        long moc = (cu != null && cu.bac == bac && cu.hetHan > bayGio) ? cu.hetHan : bayGio;
        long hetHan = moc + soNgay * TheThangDAO.MOT_NGAY_MS;
        if (!TheThangDAO.datThe(playerId, bac, hetHan, false)) {
            return "Không ghi được vào CSDL.";
        }
        Player dangChoi = nro.server.Client.gI().getPlayerByID(playerId);
        if (dangChoi != null) {
            synchronized (dangChoi) {
                dangChoi.THE_THANG = bac;
                dangChoi.LASTTIME_THE_THANG = hetHan;
            }
            capNhatChiSo(dangChoi);
            Service.gI().sendThongBao(dangChoi, "Bạn được tặng " + soNgay + " ngày "
                    + TheThangDAO.tenBac(bac) + ".");
        }
        return "Đã tặng " + soNgay + " ngày " + TheThangDAO.tenBac(bac)
                + ", hết hạn " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                        .format(new java.util.Date(hetHan))
                + (dangChoi != null ? " (đang online, có hiệu lực ngay)" : "");
    }

    // =====================================================================
    //  Gói tin
    // =====================================================================
    /**
     * Nối dữ liệu thẻ tháng vào cuối gói phúc lợi.
     *
     * <pre>
     * byte  1                 (có mục thẻ tháng)
     * byte  soGoi
     *   byte  bac
     *   UTF   ten, gia ("50.000 VNĐ"), uuDai
     *   short soNgay
     *   byte  soQuaMua   { short icon, int soLuong }
     *   byte  soQuaNgay  { short icon, int soLuong }
     * byte  bacDangCo (0 = không có / hết hạn)
     * short soNgayCon
     * UTF   hetHan ("17/10/2026 20:15", rỗng nếu không có)
     * byte  daNhanHomNay (1/0)
     * UTF   soDu ("Số dư: 120.000 VNĐ · 500 hồng ngọc · 20 ngọc")
     * </pre>
     */
    public void ghiVaoGoi(Player pl, Message msg) throws Exception {
        List<TheThangDAO.Goi> dsGoi = new java.util.ArrayList<>();
        for (TheThangDAO.Goi g : TheThangDAO.dsGoi()) {
            if (g.bat) {
                dsGoi.add(g);
            }
        }
        msg.writer().writeByte(1);
        msg.writer().writeByte(dsGoi.size());
        for (TheThangDAO.Goi g : dsGoi) {
            msg.writer().writeByte(g.bac);
            msg.writer().writeUTF(g.ten);
            msg.writer().writeUTF(g.giaDeDoc());
            msg.writer().writeUTF(g.uuDai());
            msg.writer().writeShort(g.soNgay);
            ghiQua(msg, TheThangDAO.dsQua(g.bac, TheThangDAO.QUA_MUA));
            ghiQua(msg, TheThangDAO.dsQua(g.bac, TheThangDAO.QUA_NGAY));
        }
        boolean con = conHan(pl);
        msg.writer().writeByte(con ? pl.THE_THANG : 0);
        msg.writer().writeShort(soNgayCon(pl));
        msg.writer().writeUTF(con ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(new java.util.Date(pl.LASTTIME_THE_THANG)) : "");
        msg.writer().writeByte(con && TheThangDAO.daNhan(pl.id, PhucLoiDAO.ngayHomNay()) ? 1 : 0);
        long vnd = pl.getSession() == null ? 0 : pl.getSession().vnd;
        msg.writer().writeUTF("Số dư: " + Util.soCham(vnd) + " VNĐ · "
                + Util.soCham(pl.inventory.ruby) + " hồng ngọc · "
                + Util.soCham(pl.inventory.gem) + " ngọc");
    }

    private void ghiQua(Message msg, List<TheThangDAO.Qua> ds) throws Exception {
        msg.writer().writeByte(ds.size());
        for (TheThangDAO.Qua q : ds) {
            msg.writer().writeShort(PhucLoiService.gI().iconCua(q.itemId));
            msg.writer().writeInt(q.soLuong);
        }
    }
}
