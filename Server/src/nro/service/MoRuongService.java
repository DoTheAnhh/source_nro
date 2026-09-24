package nro.service;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.repository.dao.MoRuongDAO;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * <b>Mở rương</b> — tab gacha trong màn Sự kiện.
 *
 * <p>Người chơi chọn loại rương, mở x1 hoặc x10 bằng <b>điểm rương</b>. Máy
 * chủ bốc quà theo trọng số, phát vào hành trang, rồi gửi kết quả; hiệu ứng
 * dải quà trượt kiểu CS:GO là việc của client, máy chủ không dính vào.</p>
 *
 * <h2>Gói 115</h2>
 * <pre>
 * Lên:  byte 0                      xin bảng
 *       byte 1, short id, byte n    mở rương id, n lần (1 hoặc 10)
 * Xuống: byte 0 BẢNG   long diem, UTF tenDiem, byte soRuong
 *                        { short id, UTF ten, UTF moTa, short icon, int giaX1,
 *                          int giaX10, byte soQua
 *                          { short icon, int soLuong, UTF ten, byte hiem, UTF tiLe } }
 *        byte 1 KẾT QUẢ short id, long diemConLai, byte n
 *                        { short icon, int soLuong, UTF ten, byte hiem }
 * </pre>
 */
public class MoRuongService {

    private static MoRuongService instance;

    public static MoRuongService gI() {
        if (instance == null) {
            instance = new MoRuongService();
        }
        return instance;
    }

    public static final int GOI = 115;
    private static final int XIN_BANG = 0;
    private static final int MO = 1;
    private static final int GUI_BANG = 0;
    private static final int GUI_KET_QUA = 1;

    public static final String TEN_DIEM = "Điểm rương";

    public void nhanGoi(Player pl, Message msg) {
        if (pl == null || msg == null || !pl.isPl()) {
            return;
        }
        try {
            int viec = msg.reader().readByte();
            if (viec == XIN_BANG) {
                guiBang(pl);
            } else if (viec == MO) {
                int id = msg.reader().readShort();
                int n = msg.reader().readByte();
                mo(pl, id, n);
            }
        } catch (Exception ex) {
            Logger.logException(MoRuongService.class, ex, "Lỗi gói mở rương");
        }
    }

    // =====================================================================
    //  Mở
    // =====================================================================
    /**
     * Mở rương {@code id} đúng {@code soLan} lần (chỉ nhận 1 hoặc 10).
     *
     * <p>Thứ tự: kiểm hành trang → trừ điểm (một câu lệnh có điều kiện, nên
     * bấm dồn không trừ âm được) → bốc và phát. Hành trang kiểm theo trường
     * hợp xấu nhất — mỗi món một ô — để không có cảnh đã trừ điểm mà không
     * còn chỗ nhét quà.</p>
     */
    public void mo(Player pl, int id, int soLan) {
        if (soLan != 1 && soLan != 10) {
            return;
        }
        synchronized (pl) {
            MoRuongDAO.Ruong r = MoRuongDAO.ruong(id);
            if (r == null || !r.bat) {
                Service.gI().sendThongBao(pl, "Rương này hiện không mở được.");
                return;
            }
            List<MoRuongDAO.Qua> pool = MoRuongDAO.dsQua(id);
            if (pool.isEmpty()) {
                Service.gI().sendThongBao(pl, "Rương này chưa có quà.");
                return;
            }
            if (InventoryService.gI().getCountEmptyBag(pl) < soLan) {
                Service.gI().sendThongBao(pl, "Hành trang cần trống " + soLan + " ô.");
                return;
            }
            int gia = soLan == 10 ? r.giaX10 : r.giaX1;
            if (gia > 0 && !MoRuongDAO.truDiem(pl.id, gia)) {
                Service.gI().sendThongBao(pl, "Không đủ " + TEN_DIEM.toLowerCase() + " — cần "
                        + gia + ", đang có " + MoRuongDAO.diem(pl.id) + ".");
                return;
            }
            List<MoRuongDAO.Qua> trung = new ArrayList<>();
            for (int i = 0; i < soLan; i++) {
                trung.add(boc(pool));
            }
            Service.gI().batGomGoi(pl);
            try {
                for (MoRuongDAO.Qua q : trung) {
                    phat(pl, q);
                    MoRuongDAO.ghiLichSu(pl.id, id, q.itemId, q.soLuong, q.hiem);
                }
            } finally {
                Service.gI().xaGomGoi(pl);
            }
            guiKetQua(pl, id, trung);
            thongBaoHiem(pl, r, trung);
        }
    }

    /** Bốc một món theo trọng số. */
    private MoRuongDAO.Qua boc(List<MoRuongDAO.Qua> pool) {
        long tong = 0;
        for (MoRuongDAO.Qua q : pool) {
            tong += q.trongSo;
        }
        long x = (long) (Math.random() * tong);
        for (MoRuongDAO.Qua q : pool) {
            x -= q.trongSo;
            if (x < 0) {
                return q;
            }
        }
        return pool.get(pool.size() - 1);
    }

    private void phat(Player pl, MoRuongDAO.Qua q) {
        try {
            Item it = ItemService.gI().createNewItem((short) q.itemId, q.soLuong);
            if (it == null || it.template == null) {
                return;
            }
            it.quantity = q.soLuong;
            if (q.chiSo != null && !q.chiSo.trim().isEmpty()) {
                for (String phan : q.chiSo.split(",")) {
                    String[] kv = phan.split("=");
                    if (kv.length == 2) {
                        try {
                            it.itemOptions.add(new ItemOption(Integer.parseInt(kv[0].trim()),
                                    Integer.parseInt(kv[1].trim())));
                        } catch (NumberFormatException sai) {
                            // Dong hong thi bo dong do.
                        }
                    }
                }
            }
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBag(pl);
        } catch (Exception ex) {
            Logger.logException(MoRuongService.class, ex, "Lỗi phát quà rương item " + q.itemId);
        }
    }

    /** Trúng huyền thoại thì báo cả máy chủ — cái "khoe" làm người khác muốn mở. */
    private void thongBaoHiem(Player pl, MoRuongDAO.Ruong r, List<MoRuongDAO.Qua> trung) {
        for (MoRuongDAO.Qua q : trung) {
            if (q.hiem >= 3) {
                nro.entity.template.ItemTemplate t = ItemService.gI().getTemplate(q.itemId);
                try {
                    nro.server.ServerNotify.gI().notify(pl.name + " vừa mở " + r.ten + " ra "
                            + (t == null ? "quà huyền thoại" : t.name) + "!");
                } catch (Exception ex) {
                    // Thong bao loi thi thoi, qua da phat roi.
                }
                return;
            }
        }
    }

    // =====================================================================
    //  Quản trị
    // =====================================================================
    public String congDiem(long playerId, long diem) {
        MoRuongDAO.congDiem(playerId, diem);
        Player p = nro.server.Client.gI().getPlayerByID(playerId);
        if (p != null) {
            Service.gI().sendThongBao(p, "Bạn được cộng " + diem + " " + TEN_DIEM.toLowerCase() + ".");
        }
        return "Đã cộng " + diem + " điểm, giờ có " + MoRuongDAO.diem(playerId) + ".";
    }

    // =====================================================================
    //  Gói tin
    // =====================================================================
    public void guiBang(Player pl) {
        Message msg = null;
        try {
            List<MoRuongDAO.Ruong> ds = MoRuongDAO.dsRuong(true);
            msg = new Message(GOI);
            msg.writer().writeByte(GUI_BANG);
            msg.writer().writeLong(MoRuongDAO.diem(pl.id));
            msg.writer().writeUTF(TEN_DIEM);
            msg.writer().writeByte(ds.size());
            for (MoRuongDAO.Ruong r : ds) {
                msg.writer().writeShort(r.id);
                msg.writer().writeUTF(r.ten);
                msg.writer().writeUTF(r.moTa);
                msg.writer().writeShort(PhucLoiService.gI().iconCua(r.itemHinh));
                msg.writer().writeInt(r.giaX1);
                msg.writer().writeInt(r.giaX10);
                List<MoRuongDAO.Qua> qua = MoRuongDAO.dsQua(r.id);
                long tong = 0;
                for (MoRuongDAO.Qua q : qua) {
                    tong += q.trongSo;
                }
                int n = Math.min(60, qua.size());
                msg.writer().writeByte(n);
                for (int i = 0; i < n; i++) {
                    MoRuongDAO.Qua q = qua.get(i);
                    msg.writer().writeShort(PhucLoiService.gI().iconCua(q.itemId));
                    msg.writer().writeInt(q.soLuong);
                    msg.writer().writeUTF(tenMon(q.itemId));
                    msg.writer().writeByte(q.hiem);
                    msg.writer().writeUTF(tiLe(q.trongSo, tong));
                }
            }
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(MoRuongService.class, ex, "Không gửi được bảng mở rương");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void guiKetQua(Player pl, int id, List<MoRuongDAO.Qua> trung) {
        Message msg = null;
        try {
            msg = new Message(GOI);
            msg.writer().writeByte(GUI_KET_QUA);
            msg.writer().writeShort(id);
            msg.writer().writeLong(MoRuongDAO.diem(pl.id));
            msg.writer().writeByte(trung.size());
            for (MoRuongDAO.Qua q : trung) {
                msg.writer().writeShort(PhucLoiService.gI().iconCua(q.itemId));
                msg.writer().writeInt(q.soLuong);
                msg.writer().writeUTF(tenMon(q.itemId));
                msg.writer().writeByte(q.hiem);
            }
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(MoRuongService.class, ex, "Không gửi được kết quả mở rương");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private static String tenMon(int itemId) {
        nro.entity.template.ItemTemplate t = ItemService.gI().getTemplate(itemId);
        return t == null ? ("Vật phẩm " + itemId) : t.name;
    }

    /** "12,5%" — hai chữ số lẻ khi cần, để tỉ lệ nhỏ không hiện thành 0%. */
    public static String tiLe(long trongSo, long tong) {
        if (tong <= 0) {
            return "0%";
        }
        long phanVan = trongSo * 10000L / tong;
        long nguyen = phanVan / 100;
        long le = phanVan % 100;
        if (le == 0) {
            return nguyen + "%";
        }
        return nguyen + "," + (le % 10 == 0 ? String.valueOf(le / 10) : (le < 10 ? "0" + le : String.valueOf(le))) + "%";
    }
}
