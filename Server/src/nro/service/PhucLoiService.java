package nro.service;

import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.repository.dao.PhucLoiDAO;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Hệ thống <b>Phúc lợi</b> trong game — mở từ nút trên màn hình.
 *
 * <p>Client tự vẽ màn hình riêng (lớp {@code PhucLoiUI}); máy chủ chỉ gửi
 * dữ liệu. Trước đây dùng menu NPC sẵn có cho nhanh, nhưng menu đó hiện
 * lên như một hộp thoại nổi đè sau mỗi lần nhận quà — thứ màn hình riêng
 * đã nói đủ, nên nhánh menu đó đã bỏ hẳn.</p>
 */
public class PhucLoiService {

    private static PhucLoiService instance;

    public static PhucLoiService gI() {
        if (instance == null) {
            instance = new PhucLoiService();
        }
        return instance;
    }

    /**
     * Tiến độ hiện tại của một người theo loại đếm của nhóm.
     *
     * <p>Ba loại đầu đọc thẳng từ người chơi nên luôn đúng thời điểm; hai loại
     * sau lấy từ bảng vì máy chủ tự cộng hoặc quản trị tự đặt.</p>
     */
    public long tienDo(Player pl, String loai) {
        if (pl == null || loai == null) {
            return 0;
        }
        switch (loai) {
            case PhucLoiDAO.LOAI_SUC_MANH:
                return pl.nPoint == null ? 0 : pl.nPoint.power;
            case PhucLoiDAO.LOAI_NHIEM_VU:
                return (pl.playerTask == null || pl.playerTask.taskMain == null)
                        ? 0 : pl.playerTask.taskMain.id;
            case PhucLoiDAO.LOAI_MAY_DAM:
                return pl.dame30s;
            case PhucLoiDAO.LOAI_ONLINE:
                // Doc tu bo nho: con so nay tang moi phut, doc CSDL thi man
                // hinh luon cham mot nhip so voi thuc te.
                return pl.phutOnline();
            default:
                return PhucLoiDAO.tienDo(pl.id, loai);
        }
    }

    /** Mã gói phúc lợi, dùng chung cho cả chiều lên và chiều xuống. */
    public static final byte OPCODE = -58;

    /**
     * Gửi toàn bộ dữ liệu phúc lợi xuống client để nó tự vẽ màn hình.
     *
     * <p>Gửi <b>một lần cả cụm</b> chứ không hỏi từng nhóm: dữ liệu này nhỏ (vài
     * chục dòng) mà bấm qua lại giữa các mục thì liên tục, hỏi lại mỗi lần bấm sẽ
     * thấy khựng. Bù lại phải gửi lại cả cụm sau mỗi lần nhận quà — chấp nhận
     * được vì nhận quà không phải thao tác lặp.</p>
     *
     * <h3>Khuôn gói</h3>
     * <pre>
     * byte  soNhom
     *   UTF   tenNhom
     *   UTF   tenLoai          ("Sức mạnh", "Số phút online"…)
     *   UTF   tienDo           (đã định dạng sẵn, client khỏi lo dấu chấm)
     *   byte  soMoc
     *     int   mocId
     *     UTF   nhanMoc        ("Mốc 50.000")
     *     byte  trangThai      0 chưa đủ · 1 nhận được · 2 đã nhận
     *     UTF   moTaThieu      ("còn thiếu 1.000") — rỗng nếu không thiếu
     *     byte  soQua
     *       short iconId
     *       int   soLuong
     * </pre>
     */
    public void guiDuLieu(Player pl) {
        guiDuLieu(pl, null);
    }

    /**
     * Như trên, kèm <b>tổng quà vừa nhận</b> ở cuối gói để client hiện popup.
     *
     * @param tongKet id vật phẩm → tổng số lượng; {@code null} là không có
     */
    public void guiDuLieu(Player pl, java.util.Map<Integer, Integer> tongKet) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            List<PhucLoiDAO.Nhom> dsNhom = PhucLoiDAO.dsNhom(true);
            msg = new Message(OPCODE);
            msg.writer().writeByte(dsNhom.size());
            for (PhucLoiDAO.Nhom n : dsNhom) {
                long cua = tienDo(pl, n.loai);
                final String dv = PhucLoiDAO.donVi(n.loai);
                msg.writer().writeUTF(n.ten == null ? "" : n.ten);
                msg.writer().writeUTF(PhucLoiDAO.tenLoai(n.loai));
                msg.writer().writeUTF(Util.format(cua) + (dv.isEmpty() ? "" : " " + dv));

                List<PhucLoiDAO.Moc> dsMoc = PhucLoiDAO.dsMoc(n.id, true);
                msg.writer().writeByte(dsMoc.size());
                for (PhucLoiDAO.Moc m : dsMoc) {
                    byte tt;
                    String thieu = "";
                    if (PhucLoiDAO.daNhan(pl.id, m.id, n.loai)) {
                        tt = 2;
                    } else if (cua >= m.moc) {
                        tt = 1;
                    } else {
                        tt = 0;
                        thieu = "còn thiếu " + Util.format(m.moc - cua)
                                + (dv.isEmpty() ? "" : " " + dv);
                    }
                    msg.writer().writeInt(m.id);
                    msg.writer().writeUTF(PhucLoiDAO.tienTo(n.loai) + " "
                            + Util.format(m.moc) + (dv.isEmpty() ? "" : " " + dv));
                    msg.writer().writeByte(tt);
                    msg.writer().writeUTF(thieu);
                    // Phan tram tinh o may chu: client khong biet con so moc goc
                    // (no chi nhan chuoi da dinh dang) nen khong tu tinh duoc.
                    int pt = m.moc <= 0 ? 100 : (int) (cua * 100 / m.moc);
                    msg.writer().writeByte(pt > 100 ? 100 : (pt < 0 ? 0 : pt));

                    List<PhucLoiDAO.Qua> dsQua = PhucLoiDAO.dsQua(m.id);
                    msg.writer().writeByte(dsQua.size());
                    for (PhucLoiDAO.Qua q : dsQua) {
                        msg.writer().writeShort(iconCua(q.itemId));
                        msg.writer().writeInt(q.soLuong);
                    }
                }
            }
            // The thang noi vao CUOI goi: client cu doc het phan phuc loi roi
            // dung, khong lech.
            TheThangService.gI().ghiVaoGoi(pl, msg);
            // Tong qua vua nhan (nhan nhanh): byte 1 roi danh sach, hoac byte 0.
            if (tongKet != null && !tongKet.isEmpty()) {
                msg.writer().writeByte(1);
                msg.writer().writeByte(Math.min(120, tongKet.size()));
                int dem = 0;
                for (java.util.Map.Entry<Integer, Integer> e : tongKet.entrySet()) {
                    if (dem++ >= 120) {
                        break;
                    }
                    nro.entity.template.ItemTemplate t = ItemService.gI().getTemplate(e.getKey());
                    msg.writer().writeShort(iconCua(e.getKey()));
                    msg.writer().writeInt(e.getValue());
                    msg.writer().writeUTF(t == null ? ("Vật phẩm " + e.getKey()) : t.name);
                }
            } else {
                msg.writer().writeByte(0);
            }
            msg.writer().flush();
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(PhucLoiService.class, ex, "Lỗi gửi dữ liệu phúc lợi");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /** Id icon của một vật phẩm, {@code -1} nếu không tra được. */
    short iconCua(int itemId) {
        try {
            nro.entity.template.ItemTemplate t = ItemService.gI().getTemplate(itemId);
            if (t != null) {
                return (short) t.iconID;
            }
        } catch (Exception ignored) {
            // Bang mau chua nap -> client ve o trong, khong sao.
        }
        return -1;
    }

    /**
     * Nhận gói từ client.
     *
     * <p>{@code 0} là xin dữ liệu, {@code 1} kèm id mốc là xin nhận quà. Gộp hai
     * việc vào một mã gói cho khỏi tốn thêm một mã — chúng luôn đi cùng nhau.</p>
     */
    public void nhanGoi(Player pl, Message msg) {
        if (pl == null || msg == null) {
            return;
        }
        try {
            byte viec = msg.reader().readByte();
            if (viec == 0) {
                guiDuLieu(pl);
                return;
            }
            if (viec == TheThangService.VIEC_MUA) {
                TheThangService.gI().mua(pl, msg.reader().readByte());
                return;
            }
            if (viec == TheThangService.VIEC_NHAN_NGAY) {
                TheThangService.gI().nhanNgay(pl);
                return;
            }
            if (viec == VIEC_NHAN_NHANH) {
                nhanNhanh(pl);
                return;
            }
            int mocId = msg.reader().readInt();
            nhanTheoId(pl, mocId);
        } catch (Exception ex) {
            Logger.logException(PhucLoiService.class, ex, "Lỗi nhận gói phúc lợi");
        }
    }

    /** Việc 4: nhận nhanh mọi thứ đang nhận được. */
    public static final byte VIEC_NHAN_NHANH = 4;

    /**
     * Nhận nhanh: quà NRO Pass hôm nay và mọi mốc đã đủ, trong một lần bấm.
     *
     * <p>Đi qua đúng các hàm nhận lẻ (cùng kiểm hành trang, cùng ghi cờ
     * đã-nhận trước khi phát), chỉ khác là gộp gói: nhận hai chục mốc một lúc
     * mà mỗi mốc bắn một gói hành trang thì client ngập. Hết chỗ giữa chừng
     * thì dừng ở đó, nói rõ còn bao nhiêu mục chưa nhận.</p>
     */
    private void nhanNhanh(Player pl) {
        int duoc = 0;
        int conLai = 0;
        // Giu thu tu nhan de popup liet ke dung thu tu.
        java.util.Map<Integer, Integer> tong = new java.util.LinkedHashMap<>();
        Service.gI().batGomGoi(pl);
        try {
            if (TheThangService.gI().coTheNhanHomNay(pl)) {
                int bacPass = pl.THE_THANG;
                if (TheThangService.gI().nhanNgay(pl, false)) {
                    duoc++;
                    for (nro.repository.dao.TheThangDAO.Qua q : nro.repository.dao.TheThangDAO
                            .dsQua(bacPass, nro.repository.dao.TheThangDAO.QUA_NGAY)) {
                        tong.merge(q.itemId, q.soLuong, Integer::sum);
                    }
                } else {
                    conLai++;
                }
            }
            for (PhucLoiDAO.Nhom n : PhucLoiDAO.dsNhom(true)) {
                long cua = tienDo(pl, n.loai);
                for (PhucLoiDAO.Moc m : PhucLoiDAO.dsMoc(n.id, true)) {
                    if (cua < m.moc || PhucLoiDAO.daNhan(pl.id, m.id, n.loai)) {
                        continue;
                    }
                    if (PhucLoiDAO.dsQua(m.id).isEmpty()) {
                        continue;
                    }
                    if (nhanMoc(pl, m, n)) {
                        duoc++;
                        for (PhucLoiDAO.Qua q : PhucLoiDAO.dsQua(m.id)) {
                            tong.merge(q.itemId, q.soLuong, Integer::sum);
                        }
                    } else {
                        conLai++;
                    }
                }
            }
        } finally {
            Service.gI().xaGomGoi(pl);
        }
        if (duoc == 0 && conLai == 0) {
            Service.gI().sendThongBao(pl, "Không có quà nào đang chờ nhận.");
        } else if (conLai == 0) {
            Service.gI().sendThongBao(pl, "Đã nhận nhanh " + duoc + " mục quà.");
        } else {
            Service.gI().sendThongBao(pl, "Đã nhận " + duoc + " mục, còn " + conLai
                    + " mục chưa nhận được (hành trang đầy?).");
        }
        guiDuLieu(pl, tong);
    }

    /** Nhận quà của một mốc theo id, rồi gửi lại dữ liệu để client vẽ lại. */
    private void nhanTheoId(Player pl, int mocId) {
        for (PhucLoiDAO.Nhom n : PhucLoiDAO.dsNhom(true)) {
            for (PhucLoiDAO.Moc m : PhucLoiDAO.dsMoc(n.id, true)) {
                if (m.id == mocId) {
                    nhanMoc(pl, m, n);
                    guiDuLieu(pl);
                    return;
                }
            }
        }
    }

    /**
     * Trao quà của một mốc.
     *
     * <p>Kiểm đủ ba điều kiện rồi mới phát: đủ chỗ trong hành trang, đủ tiến độ,
     * và chưa nhận. Ghi cờ đã-nhận <b>trước</b> khi phát quà — nếu phát lỗi giữa
     * chừng thì người chơi mất quà một lần, còn ghi sau mà lỗi thì họ nhận được
     * vô hạn.</p>
     */
    private boolean nhanMoc(Player pl, PhucLoiDAO.Moc m, PhucLoiDAO.Nhom nhom) {
        if (nhom == null) {
            return false;
        }
        long cua = tienDo(pl, nhom.loai);
        if (cua < m.moc) {
            Service.gI().sendThongBao(pl, "Chưa đủ mốc — còn thiếu "
                    + Util.format(m.moc - cua));
            return false;
        }
        if (PhucLoiDAO.daNhan(pl.id, m.id, nhom.loai)) {
            Service.gI().sendThongBao(pl, "Mốc này đã nhận rồi.");
            return false;
        }
        List<PhucLoiDAO.Qua> qua = PhucLoiDAO.dsQua(m.id);
        if (qua.isEmpty()) {
            Service.gI().sendThongBao(pl, "Mốc này chưa gắn quà nào.");
            return false;
        }
        if (soO(pl) < qua.size()) {
            Service.gI().sendThongBao(pl, "Hành trang cần trống "
                    + qua.size() + " ô.");
            return false;
        }
        if (!PhucLoiDAO.ghiDaNhan(pl.id, m.id, nhom.loai)) {
            // Khoa chinh chan trung -> co nguoi vua nhan xong trong tich tac.
            Service.gI().sendThongBao(pl, "Mốc này đã nhận rồi.");
            return false;
        }
        StringBuilder tom = new StringBuilder();
        for (PhucLoiDAO.Qua q : qua) {
            try {
                Item it = ItemService.gI().createNewItem((short) q.itemId, q.soLuong);
                if (it == null || it.template == null) {
                    continue;
                }
                it.quantity = q.soLuong;
                apChiSo(it, q.chiSo);
                InventoryService.gI().addItemBag(pl, it);
                if (tom.length() > 0) {
                    tom.append(", ");
                }
                tom.append(it.template.name).append(" x").append(q.soLuong);
            } catch (Exception ex) {
                Logger.logException(PhucLoiService.class, ex,
                        "Lỗi phát quà phúc lợi item " + q.itemId);
            }
        }
        InventoryService.gI().sendItemBag(pl);
        return true;
    }

    /** Nhóm theo id, hoặc null. */
    private PhucLoiDAO.Nhom nhomTheoId(int id) {
        for (PhucLoiDAO.Nhom n : PhucLoiDAO.dsNhom(true)) {
            if (n.id == id) {
                return n;
            }
        }
        return null;
    }

    /** Số ô trống trong hành trang. */
    private int soO(Player pl) {
        int n = 0;
        try {
            for (Item it : pl.inventory.itemsBag) {
                if (it == null || !it.isNotNullItem()) {
                    n++;
                }
            }
        } catch (Exception ignored) {
            // Hanh trang chua nap xong -> coi nhu khong con o.
        }
        return n;
    }

    /**
     * Gắn chỉ số cho món quà.
     *
     * <p>Chuỗi dạng {@code "50=5,77=10"} — cùng cách viết với chỉ số món rơi của
     * boss, để quản trị chỉ phải nhớ một kiểu.</p>
     */
    private void apChiSo(Item it, String chuoi) {
        if (chuoi == null || chuoi.trim().isEmpty() || "[]".equals(chuoi.trim())) {
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
            } catch (NumberFormatException ignored) {
                // Dong hong -> bo qua dong do, cac dong khac van gan duoc.
            }
        }
    }
}
