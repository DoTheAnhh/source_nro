package nro.service;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.repository.dao.ThuCungDAO;
import nro.service.inventory.InventoryService;

/**
 * Nuôi và dùng <b>thú cưng</b>.
 *
 * <h2>Con thú là một món đồ</h2>
 *
 * <p>Thú cưng vốn đã là vật phẩm kiểu 21, mặc vào ô {@link ThuCungDAO#O_THU_CUNG}
 * là nó chạy theo sau lưng. Ở đây thêm ba thứ: <b>cấp</b>, <b>kinh nghiệm</b>
 * và <b>chiêu</b>.</p>
 *
 * <p>Cấp và kinh nghiệm là hai chỉ số phụ gắn thẳng vào món, nên chúng theo con
 * thú qua trao đổi, hiện sẵn trong bảng mô tả, và client đọc được ngay từ gói
 * hành trang cũ — không phải thêm gói tin nào cho việc hiển thị.</p>
 *
 * <h2>Một con ra trận</h2>
 *
 * <p>Ô thú cưng chỉ có một, nên "ra trận" chính là mặc vào ô ấy và mọi con còn
 * lại nằm trong hành trang là "nghỉ ngơi" — không cần thêm cột trạng thái nào,
 * cũng không có cách nào cho hai con cùng ra trận.</p>
 *
 * <h2>Chiêu nổ thế nào</h2>
 *
 * <p>Mỗi đòn đánh, từng chiêu <b>đã mở</b> của con đang ra trận tự bốc theo tỉ
 * lệ của nó, ai hồi chiêu xong thì bốc. Nổ trúng thì đặt một lớp tăng ích
 * ({@link Buff}) sống trong mấy giây; hết giờ thì lớp ấy tự rụng. Nhiều chiêu
 * nổ cùng lúc thì cộng dồn.</p>
 */
public class ThuCungService {

    private static ThuCungService instance;

    public static ThuCungService gI() {
        if (instance == null) {
            instance = new ThuCungService();
        }
        return instance;
    }

    /** Mã gói tin thú cưng, khớp với client ({@code Controller}, case 111). */
    public static final int GOI_THU_CUNG = 111;

    private static final int VIEC_BANG = 0;
    private static final int VIEC_CHO_AN = 1;

    /** Một lớp tăng ích đang chạy trên người chơi. */
    public static final class Buff {

        public int loai;
        public int thamSo;
        public int phanTram;
        /** Mốc thời gian máy lúc hết hiệu lực. */
        public long het;
    }

    // =====================================================================
    //  Cấp và kinh nghiệm của một con
    // =====================================================================
    public static boolean laThuCung(Item it) {
        return it != null && it.isNotNullItem() && it.template != null
                && it.template.type == ThuCungDAO.KIEU_THU_CUNG;
    }

    public int cap(Item it) {
        int c = doChiSo(it, ThuCungDAO.idChiSoCap());
        return c < 1 ? 1 : c;
    }

    public int exp(Item it) {
        return Math.max(0, doChiSo(it, ThuCungDAO.idChiSoExp()));
    }

    private int doChiSo(Item it, int idChiSo) {
        if (it == null || it.itemOptions == null || idChiSo <= 0) {
            return 0;
        }
        for (ItemOption io : it.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == idChiSo) {
                return io.param;
            }
        }
        return 0;
    }

    /**
     * Ghi một chỉ số phụ, thêm dòng mới nếu món chưa có.
     *
     * <p>Dòng cấp đặt lên <b>đầu</b> danh sách để bảng mô tả mở ra là thấy ngay,
     * không phải dò giữa mấy dòng cộng máu cộng sức đánh.</p>
     */
    private void datChiSo(Item it, int idChiSo, int giaTri, boolean lenDau) {
        if (it == null || it.itemOptions == null || idChiSo <= 0) {
            return;
        }
        for (ItemOption io : it.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == idChiSo) {
                io.param = giaTri;
                return;
            }
        }
        ItemOption moi = new ItemOption(idChiSo, giaTri);
        if (moi.optionTemplate == null) {
            return;
        }
        if (lenDau) {
            it.itemOptions.add(0, moi);
        } else {
            it.itemOptions.add(moi);
        }
    }

    /**
     * Cộng kinh nghiệm cho một con thú, lên cấp bao nhiêu lần thì lên.
     *
     * @return số cấp vừa lên
     */
    public int themExp(Item thu, int them) {
        if (!laThuCung(thu) || them <= 0) {
            return 0;
        }
        int cap = cap(thu);
        int exp = exp(thu) + them;
        int tran = ThuCungDAO.capToiDa();
        int lenCap = 0;
        while (cap < tran && exp >= ThuCungDAO.expCanChoCap(cap)) {
            exp -= ThuCungDAO.expCanChoCap(cap);
            cap++;
            lenCap++;
        }
        if (cap >= tran) {
            // Toi tran thi khong giu phan thua: giu lai chi lam nguoi choi tuong
            // con len duoc nua.
            exp = 0;
        }
        datChiSo(thu, ThuCungDAO.idChiSoCap(), cap, true);
        datChiSo(thu, ThuCungDAO.idChiSoExp(), exp, false);
        return lenCap;
    }

    // =====================================================================
    //  Cho ăn
    // =====================================================================
    /**
     * Cho con thú ở {@code viTriThu} ăn món ở ô {@code viTriDoAn} của hành trang.
     *
     * @param oTrangBi thú đang mặc (ô 7) hay đang nằm trong hành trang
     */
    public void choAn(Player pl, boolean oTrangBi, int viTriThu, int viTriDoAn) {
        if (pl == null || pl.inventory == null) {
            return;
        }
        Item thu = oTrangBi
                ? layO(pl.inventory.itemsBody, ThuCungDAO.O_THU_CUNG)
                : layO(pl.inventory.itemsBag, viTriThu);
        if (!laThuCung(thu)) {
            Service.gI().sendThongBao(pl, "Không thấy thú cưng");
            return;
        }
        Item mon = layO(pl.inventory.itemsBag, viTriDoAn);
        if (mon == null || !mon.isNotNullItem() || mon.template == null) {
            Service.gI().sendThongBao(pl, "Không thấy món ăn");
            return;
        }
        int exp = ThuCungDAO.expCuaDoAn(mon.template.id);
        if (exp <= 0) {
            Service.gI().sendThongBao(pl, "Thú cưng không ăn được món này");
            return;
        }
        if (cap(thu) >= ThuCungDAO.capToiDa()) {
            Service.gI().sendThongBao(pl, "Thú cưng đã đạt cấp cao nhất");
            return;
        }
        String tenMon = mon.template.name;
        int capCu = cap(thu);
        InventoryService.gI().subQuantityItemsBag(pl, mon, 1);
        themExp(thu, exp);
        InventoryService.gI().sendItemBag(pl);
        if (oTrangBi) {
            InventoryService.gI().sendItemBody(pl);
        }
        String bao = thu.template.name + " ăn " + tenMon + ", được " + exp + " kinh nghiệm";
        int capMoi = cap(thu);
        if (capMoi > capCu) {
            bao += " — lên cấp " + capMoi + "!";
            baoChieuVuaMo(pl, thu, capCu, capMoi);
        }
        Service.gI().sendThongBao(pl, bao);
    }

    /** Lên cấp mà vừa chạm cấp mở của chiêu nào thì nói cho người chơi biết. */
    private void baoChieuVuaMo(Player pl, Item thu, int capCu, int capMoi) {
        for (ThuCungDAO.KyNang k : ThuCungDAO.kyNangCua(thu.template.id)) {
            if (k.bat && k.capMo > capCu && k.capMo <= capMoi) {
                Service.gI().sendThongBao(pl, "Mở chiêu " + k.thuTu + ": " + k.ten);
            }
        }
    }

    private static Item layO(List<Item> ds, int i) {
        return (ds != null && i >= 0 && i < ds.size()) ? ds.get(i) : null;
    }

    // =====================================================================
    //  Chiêu
    // =====================================================================
    /** Con thú đang ra trận, hoặc {@code null}. */
    public Item thuRaTran(Player pl) {
        if (pl == null || pl.inventory == null) {
            return null;
        }
        Item it = layO(pl.inventory.itemsBody, ThuCungDAO.O_THU_CUNG);
        return laThuCung(it) ? it : null;
    }

    /**
     * Bốc xem chiêu nào nổ — gọi mỗi đòn đánh, từ {@code NPoint.getDameAttack}.
     *
     * <p>Từng chiêu đã mở bốc riêng, ai hồi chiêu xong thì bốc. Không giới hạn
     * hai chiêu cùng chạy: cộng dồn cũng chỉ là mấy chục phần trăm.</p>
     */
    private void bocChieu(Player pl) {
        Item thu = thuRaTran(pl);
        if (thu == null) {
            return;
        }
        long bayGio = System.currentTimeMillis();
        int cap = cap(thu);
        for (ThuCungDAO.KyNang k : ThuCungDAO.kyNangMo(thu.template.id, cap)) {
            if (k.tiLe <= 0 || k.phanTram <= 0 || k.thuTu < 1
                    || k.thuTu > ThuCungDAO.SO_KY_NANG) {
                continue;
            }
            if (pl.tcLanNo == null) {
                pl.tcLanNo = new long[ThuCungDAO.SO_KY_NANG + 1];
            }
            if (bayGio - pl.tcLanNo[k.thuTu] < k.hoiChieu * 1000L) {
                continue;
            }
            if (Util.nextInt(0, 99) >= k.tiLe) {
                continue;
            }
            pl.tcLanNo[k.thuTu] = bayGio;
            no(pl, thu, k, cap, bayGio);
        }
    }

    /** Một chiêu vừa nổ: loại nào ăn ngay thì làm ngay, loại nào kéo dài thì đặt lớp. */
    private void no(Player pl, Item thu, ThuCungDAO.KyNang k, int cap, long bayGio) {
        int phanTram = k.phanTramTheoCap(cap);
        if (k.loai == ThuCungDAO.LOAI_HOI_HP) {
            long hoi = pl.nPoint.hpMax * phanTram / 100L;
            pl.nPoint.hp = Math.min(pl.nPoint.hpMax, pl.nPoint.hp + hoi);
            nro.service.PlayerService.gI().sendInfoHpMpMoney(pl);
        } else {
            if (pl.tcBuff == null) {
                pl.tcBuff = new ArrayList<>();
            }
            Buff b = new Buff();
            b.loai = k.loai;
            b.thamSo = k.thamSo;
            b.phanTram = phanTram;
            b.het = bayGio + Math.max(1, k.giay) * 1000L;
            pl.tcBuff.add(b);
        }
        Service.gI().sendThongBao(pl, thu.template.name + " dùng " + k.ten
                + ": " + ThuCungDAO.tenLoai(k.loai).replace("%", phanTram + "%")
                + (k.loai == ThuCungDAO.LOAI_HOI_HP ? "" : " trong " + k.giay + " giây"));
    }

    /** Bỏ những lớp đã hết giờ. */
    private void donBuff(Player pl, long bayGio) {
        if (pl.tcBuff == null || pl.tcBuff.isEmpty()) {
            return;
        }
        pl.tcBuff.removeIf(b -> b == null || b.het <= bayGio);
    }

    /** Tổng phần trăm của một loại đang chạy; {@code idChieu} chỉ dùng cho loại chiêu. */
    private int tong(Player pl, int loai, int idChieu) {
        if (pl == null || pl.tcBuff == null || pl.tcBuff.isEmpty()) {
            return 0;
        }
        long bayGio = System.currentTimeMillis();
        donBuff(pl, bayGio);
        int t = 0;
        for (Buff b : pl.tcBuff) {
            if (b.loai != loai) {
                continue;
            }
            if (loai == ThuCungDAO.LOAI_CHIEU && b.thamSo != idChieu) {
                continue;
            }
            t += b.phanTram;
        }
        return t;
    }

    /**
     * Sức đánh sau khi tính thú cưng, và <b>nhân tiện bốc xem có chiêu nào nổ
     * không</b>.
     *
     * <p>Gọi ngay đầu {@code NPoint.getDameAttack} nên mỗi đòn bốc đúng một
     * lần. Cộng cả loại "% sức đánh" lẫn loại "% sát thương của một chiêu" khi
     * chiêu đang chọn đúng là chiêu ấy.</p>
     */
    public long dameSauThuCung(Player pl, long dameGoc) {
        if (pl == null || !pl.isPl()) {
            return dameGoc;
        }
        bocChieu(pl);
        int idChieu = -1;
        if (pl.playerSkill != null && pl.playerSkill.skillSelect != null
                && pl.playerSkill.skillSelect.template != null) {
            idChieu = pl.playerSkill.skillSelect.template.id;
        }
        int phanTram = tong(pl, ThuCungDAO.LOAI_SUC_DANH, -1)
                + tong(pl, ThuCungDAO.LOAI_CHIEU, idChieu);
        if (phanTram <= 0) {
            return dameGoc;
        }
        return dameGoc + dameGoc * phanTram / 100L;
    }

    /** Cộng thêm bao nhiêu phần trăm tỉ lệ chí mạng. */
    public int themChiMang(Player pl) {
        return tong(pl, ThuCungDAO.LOAI_CHI_MANG, -1);
    }

    /** Sát thương phải chịu sau khi trừ phần thú cưng đỡ giúp. */
    public double giamSatThuong(Player pl, double damage) {
        int phanTram = tong(pl, ThuCungDAO.LOAI_GIAM_SAT_THUONG, -1);
        if (phanTram <= 0) {
            return damage;
        }
        if (phanTram > 90) {
            // Chan o 90%: de admin go nham 100 la nhan vat bat tu.
            phanTram = 90;
        }
        return damage - damage * phanTram / 100d;
    }

    // =====================================================================
    //  Rương thú cưng
    // =====================================================================
    /**
     * Mở một rương: bốc <b>bậc</b> theo tỉ lệ khai trên panel, rồi bốc đều một
     * con trong bậc ấy.
     *
     * <p>Bốc hai nấc chứ không gộp làm một: admin chỉnh tỉ lệ theo bậc, còn
     * trong cùng một bậc thì con nào cũng như con nào — thêm một con mới vào
     * bậc là nó tự có phần, không phải chia lại tỉ lệ.</p>
     *
     * @return {@code true} nếu món này đúng là rương (đã xử lý xong)
     */
    public boolean moRuong(Player pl, Item ruong) {
        if (pl == null || ruong == null || ruong.template == null
                || !ThuCungDAO.laRuong(ruong.template.id)) {
            return false;
        }
        java.util.Map<Integer, Integer> tiLe = ThuCungDAO.tiLeRuong(ruong.template.id);
        int tong = 0;
        for (int v : tiLe.values()) {
            tong += Math.max(0, v);
        }
        if (tong <= 0) {
            Service.gI().sendThongBao(pl, "Rương này chưa khai tỉ lệ");
            return true;
        }
        int boc = Util.nextInt(0, tong - 1);
        int bacRa = -1;
        for (java.util.Map.Entry<Integer, Integer> e : tiLe.entrySet()) {
            boc -= Math.max(0, e.getValue());
            if (boc < 0) {
                bacRa = e.getKey();
                break;
            }
        }
        if (bacRa < 0) {
            return true;
        }
        List<nro.entity.template.ItemTemplate> ung = new ArrayList<>();
        for (nro.entity.template.ItemTemplate t : nro.server.Manager.ITEM_TEMPLATES) {
            if (t != null && t.type == ThuCungDAO.KIEU_THU_CUNG
                    && ThuCungDAO.bac(t.id) == bacRa) {
                ung.add(t);
            }
        }
        if (ung.isEmpty()) {
            Service.gI().sendThongBao(pl, "Chưa có thú cưng nào bậc "
                    + ThuCungDAO.tenBac(bacRa));
            return true;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang của bạn không đủ chỗ trống");
            return true;
        }
        nro.entity.template.ItemTemplate chon = ung.get(Util.nextInt(0, ung.size() - 1));
        Item thu = nro.service.item.ItemService.gI().createNewItem((short) chon.id);
        InventoryService.gI().subQuantityItemsBag(pl, ruong, 1);
        InventoryService.gI().addItemBag(pl, thu);
        InventoryService.gI().sendItemBag(pl);
        Service.gI().sendThongBao(pl, "Bạn nhận được " + chon.name
                + " — bậc " + ThuCungDAO.tenBac(bacRa));
        return true;
    }

    // =====================================================================
    //  Gói tin
    // =====================================================================
    public void nhanGoi(Player pl, Message msg) {
        try {
            int viec = msg.reader().readByte();
            switch (viec) {
                case VIEC_BANG:
                    guiBang(pl);
                    break;
                case VIEC_CHO_AN:
                    boolean oTrangBi = msg.reader().readByte() == 1;
                    int viTriThu = msg.reader().readByte();
                    int viTriDoAn = msg.reader().readByte();
                    choAn(pl, oTrangBi, viTriThu, viTriDoAn);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            Logger.logException(ThuCungService.class, ex, "Lỗi gói thú cưng");
        }
    }

    /**
     * Gửi bảng chiêu và bảng đồ ăn cho client.
     *
     * <p>Client cần hai bảng này để vẽ ô chiêu của từng con và để lọc ra những
     * món cho ăn được. Chỉ gửi khi client hỏi (lúc mở thẻ Thú cưng), không nhồi
     * vào lúc đăng nhập.</p>
     */
    public void guiBang(Player pl) {
        Message msg = null;
        try {
            List<ThuCungDAO.KyNang> kn = new ArrayList<>();
            for (ThuCungDAO.KyNang k : ThuCungDAO.tatCaKyNang()) {
                if (k.bat) {
                    kn.add(k);
                }
            }
            List<ThuCungDAO.DoAn> da = new ArrayList<>();
            for (ThuCungDAO.DoAn d : ThuCungDAO.tatCaDoAn()) {
                if (d.bat && d.exp > 0) {
                    da.add(d);
                }
            }
            msg = new Message(GOI_THU_CUNG);
            msg.writer().writeByte(VIEC_BANG);
            msg.writer().writeShort(ThuCungDAO.capToiDa());
            msg.writer().writeInt(ThuCungDAO.expCanChoCap(1));
            msg.writer().writeShort(ThuCungDAO.idChiSoCap());
            msg.writer().writeShort(ThuCungDAO.idChiSoExp());
            msg.writer().writeShort(kn.size());
            for (ThuCungDAO.KyNang k : kn) {
                msg.writer().writeShort(k.itemId);
                msg.writer().writeByte(k.thuTu);
                msg.writer().writeUTF(k.ten == null ? "" : k.ten);
                msg.writer().writeUTF(k.moTa == null ? "" : k.moTa);
                msg.writer().writeByte(k.loai);
                msg.writer().writeShort(k.thamSo);
                msg.writer().writeShort(k.phanTram);
                msg.writer().writeShort(k.giay);
                msg.writer().writeShort(k.tiLe);
                msg.writer().writeShort(k.hoiChieu);
                msg.writer().writeShort(k.capMo);
            }
            msg.writer().writeShort(da.size());
            for (ThuCungDAO.DoAn d : da) {
                msg.writer().writeShort(d.itemId);
                msg.writer().writeInt(d.exp);
            }
            // Hinh tung loai thu: mu, than, chan.
            //
            // Client khong co ba so nay — mau vat pham ben do chi co icon va
            // `part`. Khong gui thi khung xem truoc dung duoc moi con dang deo
            // (may chu co bao hinh qua goi 105), con nhung con nam trong hanh
            // trang thi khong ve duoc.
            List<nro.entity.template.ItemTemplate> thu = new ArrayList<>();
            for (nro.entity.template.ItemTemplate t : nro.server.Manager.ITEM_TEMPLATES) {
                if (t != null && t.type == ThuCungDAO.KIEU_THU_CUNG) {
                    thu.add(t);
                }
            }
            msg.writer().writeShort(thu.size());
            for (nro.entity.template.ItemTemplate t : thu) {
                msg.writer().writeShort(t.id);
                msg.writer().writeShort(t.head);
                msg.writer().writeShort(t.body);
                msg.writer().writeShort(t.leg);
                msg.writer().writeByte(ThuCungDAO.bac(t.id));
            }
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(ThuCungService.class, ex, "Không gửi được bảng thú cưng");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
