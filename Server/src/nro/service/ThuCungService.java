package nro.service;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.repository.dao.ThuCungDAO;
import nro.service.inventory.InventoryService;

/**
 * Nuôi và dùng <b>thú cưng</b>.
 *
 * <h2>Thú không nằm trong hành trang</h2>
 *
 * <p>Mỗi con thú là một dòng trong {@code thu_cung_so_huu}: của ai, loại gì,
 * cấp mấy, chỉ số bao nhiêu, có đang ra trận không. Mở rương là thêm một dòng
 * chứ không rơi ra vật phẩm, nên thú không ăn ô hành trang nào và không bán
 * hay trao đổi được.</p>
 *
 * <h2>Chỉ số bốc theo bậc</h2>
 *
 * <p>Lúc nhận, mỗi chỉ số bốc trong khoảng của bậc ({@code thu_cung_chi_so_bac},
 * khai trên panel) nên hai con cùng loại vẫn khác nhau. Cấp càng cao chỉ số
 * càng lên theo {@code chi_so_moi_cap}. Con <b>đang ra trận</b> cộng thẳng chỉ
 * số ấy cho chủ.</p>
 *
 * <h2>Một con ra trận</h2>
 *
 * <p>Cho con này ra trận thì mọi con khác tự về nghỉ — chuyện ấy do một câu
 * lệnh trong {@link ThuCungDAO#datRaTran} lo, không phải luật rải trong mã.</p>
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

    // Viec client gui len.
    private static final int VIEC_BANG = 0;
    private static final int VIEC_CHO_AN = 1;
    private static final int VIEC_RA_TRAN = 2;
    private static final int VIEC_DOI_TEN = 3;

    // Viec may chu gui xuong.
    private static final int GUI_BANG = 0;
    private static final int GUI_DANH_SACH = 1;
    private static final int GUI_NO_CHIEU = 2;

    /** Một lớp tăng ích đang chạy trên người chơi. */
    public static final class Buff {

        public int loai;
        public int thamSo;
        public int phanTram;
        /** Mốc thời gian máy lúc hết hiệu lực. */
        public long het;
    }

    // =====================================================================
    //  Con đang ra trận
    // =====================================================================
    /**
     * Con đang ra trận, nhớ sẵn trên {@link Player}.
     *
     * <p>Không hỏi cơ sở dữ liệu ở đây: hàm này bị gọi trong lúc tính sát
     * thương, tức mỗi đòn đánh của mọi người chơi. Chỉ đọc một lần mỗi phiên,
     * và mỗi lần đổi thú thì {@link #quenThu} xoá cho đọc lại.</p>
     */
    public ThuCungDAO.ThuSoHuu thuRaTran(Player pl) {
        if (pl == null || !pl.isPl()) {
            return null;
        }
        if (!pl.tcDaNapThu) {
            pl.tcDaNapThu = true;
            pl.tcThu = ThuCungDAO.thuRaTran((int) pl.id);
        }
        return pl.tcThu;
    }

    /** Quên con đang nhớ, để lần sau đọc lại từ cơ sở dữ liệu. */
    public void quenThu(Player pl) {
        if (pl != null) {
            pl.tcDaNapThu = false;
            pl.tcThu = null;
        }
    }

    // =====================================================================
    //  Chỉ số cộng cho chủ
    // =====================================================================
    /**
     * Cộng chỉ số của con đang ra trận vào người chơi.
     *
     * <p>Gọi từ {@code NPoint.calPoint}, cùng chỗ với chỉ số của trang bị, nên
     * nó đi qua đủ mọi phép nhân về sau (set kích hoạt, hợp thể…) y như đồ.</p>
     */
    public void congChiSo(nro.entity.player.NPoint n, Player chu) {
        if (n == null || chu == null) {
            return;
        }
        ThuCungDAO.ThuSoHuu t = thuRaTran(chu);
        if (t == null) {
            return;
        }
        n.hpAdd += t.theoCap(t.hp);
        n.mpAdd += t.theoCap(t.ki);
        n.dameAdd += t.theoCap(t.sucDanh);
        n.defAdd += t.theoCap(t.giap);
        n.critAdd += t.chiMang;
    }

    // =====================================================================
    //  Ra trận / nghỉ ngơi
    // =====================================================================
    /**
     * Cho một con ra trận, hoặc cho tất cả về nghỉ khi {@code id <= 0}.
     *
     * <p>Đổi xong thì dựng lại chỉ số và thả con thú chạy theo sau lưng — hai
     * việc người chơi thấy ngay, nên làm luôn ở đây thay vì đợi lần tính sau.</p>
     */
    public void raTran(Player pl, int id) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        if (id > 0) {
            ThuCungDAO.ThuSoHuu t = ThuCungDAO.thuTheoId(id);
            if (t == null || t.playerId != (int) pl.id) {
                Service.gI().sendThongBao(pl, "Không thấy thú cưng này");
                return;
            }
        }
        ThuCungDAO.datRaTran((int) pl.id, id);
        quenThu(pl);
        capNhatThuTheoSau(pl);
        pl.nPoint.calPoint();
        pl.nPoint.setFullHpMp();
        Service.gI().point(pl);
        guiDanhSach(pl);
        ThuCungDAO.ThuSoHuu moi = thuRaTran(pl);
        Service.gI().sendThongBao(pl, moi == null
                ? "Thú cưng đã về nghỉ ngơi" : (tenThu(moi) + " đã ra trận"));
    }

    /** Dựng lại con thú chạy theo sau lưng cho khớp con đang ra trận. */
    public void capNhatThuTheoSau(Player pl) {
        ThuCungDAO.ThuSoHuu t = thuRaTran(pl);
        if (t == null) {
            if (pl.PetFollow != null) {
                nro.service.fun.ChangeMapService.gI().exitMap(pl.PetFollow);
                pl.PetFollow.dispose();
                pl.PetFollow = null;
            }
            return;
        }
        nro.entity.template.ItemTemplate m
                = nro.service.item.ItemService.gI().getTemplate(t.itemId);
        if (m == null) {
            return;
        }
        DetuService.PetFollow(pl, m.head, m.body, m.leg);
    }

    /** Tên hiển thị: tên người chơi đặt, không có thì lấy tên loại. */
    public String tenThu(ThuCungDAO.ThuSoHuu t) {
        if (t == null) {
            return "";
        }
        if (t.ten != null && !t.ten.trim().isEmpty()) {
            return t.ten.trim();
        }
        nro.entity.template.ItemTemplate m
                = nro.service.item.ItemService.gI().getTemplate(t.itemId);
        return m == null ? ("Thú #" + t.itemId) : m.name;
    }

    // =====================================================================
    //  Đổi tên
    // =====================================================================
    private static final int DAI_TEN_TOI_DA = 16;

    public void doiTen(Player pl, int id, String ten) {
        if (pl == null || ten == null) {
            return;
        }
        ThuCungDAO.ThuSoHuu t = ThuCungDAO.thuTheoId(id);
        if (t == null || t.playerId != (int) pl.id) {
            Service.gI().sendThongBao(pl, "Không thấy thú cưng này");
            return;
        }
        String sach = ten.trim();
        if (sach.length() > DAI_TEN_TOI_DA) {
            sach = sach.substring(0, DAI_TEN_TOI_DA);
        }
        if (Util.haveSpecialCharacter(sach)) {
            Service.gI().sendThongBao(pl, "Tên không được chứa ký tự đặc biệt");
            return;
        }
        ThuCungDAO.doiTen(id, sach);
        quenThu(pl);
        guiDanhSach(pl);
        Service.gI().sendThongBao(pl, sach.isEmpty()
                ? "Đã trả lại tên gốc" : ("Đã đổi tên thành " + sach));
    }

    // =====================================================================
    //  Cho ăn
    // =====================================================================
    public void choAn(Player pl, int idThu, int viTriDoAn) {
        if (pl == null || pl.inventory == null) {
            return;
        }
        ThuCungDAO.ThuSoHuu t = ThuCungDAO.thuTheoId(idThu);
        if (t == null || t.playerId != (int) pl.id) {
            Service.gI().sendThongBao(pl, "Không thấy thú cưng này");
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
        if (t.cap >= ThuCungDAO.capToiDa()) {
            Service.gI().sendThongBao(pl, "Thú cưng đã đạt cấp cao nhất");
            return;
        }
        String tenMon = mon.template.name;
        InventoryService.gI().subQuantityItemsBag(pl, mon, 1);
        InventoryService.gI().sendItemBag(pl);

        int capCu = t.cap;
        int cap = t.cap;
        int expMoi = t.exp + exp;
        int tran = ThuCungDAO.capToiDa();
        while (cap < tran && expMoi >= ThuCungDAO.expCanChoCap(cap)) {
            expMoi -= ThuCungDAO.expCanChoCap(cap);
            cap++;
        }
        if (cap >= tran) {
            expMoi = 0;
        }
        ThuCungDAO.luuCapExp(t.id, cap, expMoi);
        if (t.raTran) {
            quenThu(pl);
            pl.nPoint.calPoint();
            Service.gI().point(pl);
        }
        guiDanhSach(pl);

        String bao = tenThu(t) + " ăn " + tenMon + ", được " + exp + " kinh nghiệm";
        if (cap > capCu) {
            bao += " — lên cấp " + cap + "!";
            baoChieuVuaMo(pl, t.itemId, capCu, cap);
        }
        Service.gI().sendThongBao(pl, bao);
    }

    /** Lên cấp mà vừa chạm cấp mở của chiêu nào thì nói cho người chơi biết. */
    private void baoChieuVuaMo(Player pl, int itemId, int capCu, int capMoi) {
        for (ThuCungDAO.KyNang k : ThuCungDAO.kyNangCua(itemId)) {
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
    /**
     * Bốc xem chiêu nào nổ — gọi mỗi đòn đánh, từ {@code NPoint.getDameAttack}.
     *
     * <p>Từng chiêu đã mở bốc riêng, ai hồi chiêu xong thì bốc.</p>
     */
    private void bocChieu(Player pl) {
        ThuCungDAO.ThuSoHuu thu = thuRaTran(pl);
        if (thu == null) {
            return;
        }
        long bayGio = System.currentTimeMillis();
        for (ThuCungDAO.KyNang k : ThuCungDAO.kyNangMo(thu.itemId, thu.cap)) {
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
            no(pl, thu, k, bayGio);
        }
    }

    /** Một chiêu vừa nổ: loại nào ăn ngay thì làm ngay, loại nào kéo dài thì đặt lớp. */
    private void no(Player pl, ThuCungDAO.ThuSoHuu thu, ThuCungDAO.KyNang k, long bayGio) {
        int phanTram = k.phanTramTheoCap(thu.cap);
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
        Service.gI().sendThongBao(pl, tenThu(thu) + " dùng " + k.ten
                + ": " + ThuCungDAO.tenLoai(k.loai).replace("%", phanTram + "%")
                + (k.loai == ThuCungDAO.LOAI_HOI_HP ? "" : " trong " + k.giay + " giây"));
        baoNoChieu(pl, thu, k);
    }

    /**
     * Báo cho client biết chiêu vừa nổ, để nó hiện biểu tượng con thú kèm số
     * chiêu ở dãy hiệu lực.
     *
     * <p>Loại ăn ngay (hồi HP) không có thời gian hiệu lực, nhưng vẫn cho hiện
     * mấy giây — không thì chiêu nổ mà màn hình chẳng có dấu hiệu gì.</p>
     */
    private void baoNoChieu(Player pl, ThuCungDAO.ThuSoHuu thu, ThuCungDAO.KyNang k) {
        Message msg = null;
        try {
            int giay = k.loai == ThuCungDAO.LOAI_HOI_HP ? 3 : Math.max(1, k.giay);
            msg = new Message(GOI_THU_CUNG);
            msg.writer().writeByte(GUI_NO_CHIEU);
            msg.writer().writeShort(thu.itemId);
            msg.writer().writeByte(k.thuTu);
            msg.writer().writeShort(giay);
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(ThuCungService.class, ex, "Không báo được chiêu thú cưng");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

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

    public int themChiMang(Player pl) {
        return tong(pl, ThuCungDAO.LOAI_CHI_MANG, -1);
    }

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
     * con trong bậc ấy. Con thú vào thẳng bộ sưu tập, không rơi ra hành trang.
     *
     * @return {@code true} nếu món này đúng là rương (đã xử lý xong)
     */
    public boolean moRuong(Player pl, Item ruong) {
        if (pl == null || ruong == null || ruong.template == null
                || !ThuCungDAO.laRuong(ruong.template.id)) {
            return false;
        }
        java.util.Map<Integer, Integer> tiLe = ThuCungDAO.tiLeRuong(ruong.template.id);
        // Bo nhung bac chua co con thu nao: boc trung bac rong thi mo ra khong
        // duoc gi, ma ruong thi da mat.
        java.util.Iterator<java.util.Map.Entry<Integer, Integer>> it
                = tiLe.entrySet().iterator();
        while (it.hasNext()) {
            if (soThuTheoBac(it.next().getKey()) == 0) {
                it.remove();
            }
        }
        int tong = 0;
        for (int v : tiLe.values()) {
            tong += Math.max(0, v);
        }
        if (tong <= 0) {
            Service.gI().sendThongBao(pl, "Rương này chưa khai tỉ lệ, hoặc chưa có"
                    + " thú cưng nào thuộc các bậc của nó");
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
        nro.entity.template.ItemTemplate chon = ung.get(Util.nextInt(0, ung.size() - 1));
        ThuCungDAO.ThuSoHuu moi = ThuCungDAO.themThu((int) pl.id, chon.id);
        if (moi == null) {
            Service.gI().sendThongBao(pl, "Không thêm được thú cưng, thử lại sau");
            return true;
        }
        InventoryService.gI().subQuantityItemsBag(pl, ruong, 1);
        InventoryService.gI().sendItemBag(pl);
        guiDanhSach(pl);
        Service.gI().sendThongBao(pl, "Bạn nhận được " + chon.name
                + " — bậc " + ThuCungDAO.tenBac(bacRa) + ". Xem ở thẻ Thú cưng.");
        return true;
    }

    private int soThuTheoBac(int bac) {
        int n = 0;
        for (nro.entity.template.ItemTemplate t : nro.server.Manager.ITEM_TEMPLATES) {
            if (t != null && t.type == ThuCungDAO.KIEU_THU_CUNG
                    && ThuCungDAO.bac(t.id) == bac) {
                n++;
            }
        }
        return n;
    }

    // =====================================================================
    //  Vật phẩm thú cưng cũ trong hành trang
    // =====================================================================
    /**
     * Nuốt những con thú còn nằm dạng <b>vật phẩm</b> vào bộ sưu tập.
     *
     * <p>Trước khi có bộ sưu tập, thú cưng là vật phẩm kiểu 21 nằm trong hành
     * trang hoặc ô Pet. Giữ cả hai đường thì người chơi có hai chỗ chứa thú mà
     * chỉ một chỗ dùng được — nên chuyển hết sang bộ sưu tập rồi bỏ vật phẩm
     * đi. Chạy một lần cho mỗi người, lúc họ mở thẻ Thú cưng.</p>
     */
    public void nuotThuCu(Player pl) {
        if (pl == null || !pl.isPl() || pl.inventory == null) {
            return;
        }
        int daNuot = 0;
        List<Item> body = pl.inventory.itemsBody;
        if (body != null && body.size() > ThuCungDAO.O_THU_CUNG) {
            Item it = body.get(ThuCungDAO.O_THU_CUNG);
            if (it != null && it.isNotNullItem() && it.template != null
                    && it.template.type == ThuCungDAO.KIEU_THU_CUNG) {
                if (ThuCungDAO.themThu((int) pl.id, it.template.id) != null) {
                    body.set(ThuCungDAO.O_THU_CUNG,
                            nro.service.item.ItemService.gI().createItemNull());
                    daNuot++;
                }
            }
        }
        List<Item> bag = pl.inventory.itemsBag;
        if (bag != null) {
            for (int i = 0; i < bag.size(); i++) {
                Item it = bag.get(i);
                if (it != null && it.isNotNullItem() && it.template != null
                        && it.template.type == ThuCungDAO.KIEU_THU_CUNG) {
                    if (ThuCungDAO.themThu((int) pl.id, it.template.id) != null) {
                        bag.set(i, nro.service.item.ItemService.gI().createItemNull());
                        daNuot++;
                    }
                }
            }
        }
        if (daNuot > 0) {
            InventoryService.gI().sendItemBag(pl);
            InventoryService.gI().sendItemBody(pl);
            quenThu(pl);
            pl.nPoint.calPoint();
            Service.gI().point(pl);
            Service.gI().sendThongBao(pl, "Đã chuyển " + daNuot
                    + " thú cưng từ hành trang vào thẻ Thú cưng");
        }
    }

    // =====================================================================
    //  Gói tin
    // =====================================================================
    public void nhanGoi(Player pl, Message msg) {
        try {
            int viec = msg.reader().readByte();
            switch (viec) {
                case VIEC_BANG:
                    nuotThuCu(pl);
                    guiBang(pl);
                    guiDanhSach(pl);
                    break;
                case VIEC_CHO_AN: {
                    int idThu = msg.reader().readInt();
                    int viTriDoAn = msg.reader().readByte();
                    choAn(pl, idThu, viTriDoAn);
                    break;
                }
                case VIEC_RA_TRAN: {
                    int idThu = msg.reader().readInt();
                    raTran(pl, idThu);
                    break;
                }
                case VIEC_DOI_TEN: {
                    int idThu = msg.reader().readInt();
                    String ten = msg.reader().readUTF();
                    doiTen(pl, idThu, ten);
                    break;
                }
                default:
                    break;
            }
        } catch (Exception ex) {
            Logger.logException(ThuCungService.class, ex, "Lỗi gói thú cưng");
        }
    }

    /** Bảng chung: cấu hình, chiêu, đồ ăn, hình và bậc từng loại thú. */
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
            msg.writer().writeByte(GUI_BANG);
            msg.writer().writeShort(ThuCungDAO.capToiDa());
            msg.writer().writeInt(ThuCungDAO.expCanChoCap(1));
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

    /** Danh sách thú của chính người chơi này. */
    public void guiDanhSach(Player pl) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        Message msg = null;
        try {
            List<ThuCungDAO.ThuSoHuu> ds = ThuCungDAO.thuCuaNguoi((int) pl.id);
            msg = new Message(GOI_THU_CUNG);
            msg.writer().writeByte(GUI_DANH_SACH);
            msg.writer().writeShort(ds.size());
            for (ThuCungDAO.ThuSoHuu t : ds) {
                msg.writer().writeInt(t.id);
                msg.writer().writeShort(t.itemId);
                msg.writer().writeUTF(t.ten == null ? "" : t.ten);
                msg.writer().writeShort(t.cap);
                msg.writer().writeInt(t.exp);
                msg.writer().writeInt(t.theoCap(t.hp));
                msg.writer().writeInt(t.theoCap(t.ki));
                msg.writer().writeInt(t.theoCap(t.sucDanh));
                msg.writer().writeInt(t.theoCap(t.giap));
                msg.writer().writeShort(t.chiMang);
                msg.writer().writeByte(t.raTran ? 1 : 0);
            }
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(ThuCungService.class, ex, "Không gửi được danh sách thú cưng");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
