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
        /** Mạnh bao nhiêu, tính bằng phần vạn (1.000 = 10%). */
        public int phanVan;
        /** Mốc thời gian máy lúc hết hiệu lực. */
        public long het;
        /** Khiên: lượng sát thương còn chặn được. */
        public long khien;
        /** Vùng hồi máu: mốc lần hồi tiếp theo. */
        public long lanHoiToi;
        /** Id hình hiệu ứng đang hiện trên người chơi, để gỡ khi hết; 0 là không. */
        public int hieuUng;
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

    /**
     * Tên thú chỉ cấm ký tự đặc biệt: chữ hoa, chữ thường, chữ có dấu, chữ số
     * và dấu cách đều cho qua.
     *
     * Không dùng Util.haveSpecialCharacter được: hàm ấy viết cho tên tài khoản
     * nên nó chặn cả dấu tiếng Việt lẫn dấu cách. isLetterOrDigit đọc theo
     * bảng Unicode nên "Sư Tử Lửa 2" qua được, còn "abc@#" thì không.
     */
    private boolean tenHopLe(String ten) {
        for (int i = 0; i < ten.length(); i++) {
            char c = ten.charAt(i);
            if (c != ' ' && !Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

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
        if (!tenHopLe(sach)) {
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
        // Theo CAP CHIEU cua con nay, khong theo cap thu.
        int phanVan = k.phanVanTheoCapChieu(thu.capChieu(k.thuTu));
        String them = "";
        switch (k.loai) {
            case ThuCungDAO.LOAI_HOI_HP: {
                long hoi = phanCua(pl.nPoint.hpMax, phanVan);
                pl.nPoint.hp = Math.min(pl.nPoint.hpMax, pl.nPoint.hp + hoi);
                nro.service.PlayerService.gI().sendInfoHpMpMoney(pl);
                break;
            }
            case ThuCungDAO.LOAI_HOI_KI: {
                long hoi = phanCua(pl.nPoint.mpMax, phanVan);
                pl.nPoint.mp = Math.min(pl.nPoint.mpMax, pl.nPoint.mp + hoi);
                nro.service.PlayerService.gI().sendInfoHpMpMoney(pl);
                break;
            }
            case ThuCungDAO.LOAI_NO_KICH:
                // An vao CHINH don dang tinh: dameSauThuCung doc va xoa ngay sau
                // khi bocChieu tra ve.
                pl.tcNoKich += phanVan;
                break;
            case ThuCungDAO.LOAI_SET_LAN:
                them = " — trúng " + setLan(pl, k, phanVan) + " quái";
                break;
            default: {
                Buff b = new Buff();
                b.loai = k.loai;
                b.thamSo = k.thamSo;
                b.phanVan = phanVan;
                b.het = bayGio + Math.max(1, k.giay) * 1000L;
                if (k.loai == ThuCungDAO.LOAI_KHIEN) {
                    b.khien = phanCua(pl.nPoint.hpMax, phanVan);
                    them = " (chặn " + nro.core.util.Util.soCham(b.khien) + " sát thương)";
                }
                if (k.loai == ThuCungDAO.LOAI_VUNG_HOI_MAU) {
                    // Hoi lan dau ngay, khong bat doi mot giay moi thay gi.
                    b.lanHoiToi = bayGio;
                }
                b.hieuUng = k.hieuUng;
                danhSachBuff(pl).add(b);
                break;
            }
        }
        hienHinh(pl, k);
        Service.gI().sendThongBao(pl, tenThu(thu) + " dùng " + k.ten
                + ": " + ThuCungDAO.tenLoai(k.loai).replace("%", inPhanVan(phanVan) + "%")
                + (ThuCungDAO.laTucThi(k.loai) ? "" : " trong " + k.giay + " giây") + them);
        baoNoChieu(pl, thu, k);
    }

    /** {@code phanVan} phần vạn của {@code goc}, không tràn với số rất lớn. */
    private static long phanCua(long goc, int phanVan) {
        return (long) (goc * (phanVan / 10000d));
    }

    /**
     * Danh sách lớp tăng ích của người chơi, tạo nếu chưa có.
     *
     * <p>CopyOnWrite: danh sách bị đọc từ luồng đánh, từ lúc bị đánh và từ vòng
     * {@code update} cùng lúc. ArrayList thường sẽ ném lỗi sửa-khi-đang-duyệt
     * ngay giữa trận. Danh sách chỉ vài phần tử nên chép khi ghi là rẻ.</p>
     */
    private static java.util.List<Buff> danhSachBuff(Player pl) {
        if (pl.tcBuff == null) {
            pl.tcBuff = new java.util.concurrent.CopyOnWriteArrayList<>();
        }
        return pl.tcBuff;
    }

    /** Hiện hình hiệu ứng của chiêu cho cả khu thấy; loại tức thì thì tự gỡ sau 2 giây. */
    private void hienHinh(Player pl, ThuCungDAO.KyNang k) {
        if (k.hieuUng <= 0 || pl.zone == null) {
            return;
        }
        try {
            Service.gI().sendEffAllPlayer(pl, k.hieuUng, 1, -1, -1);
            if (ThuCungDAO.laTucThi(k.loai)) {
                final int id = k.hieuUng;
                Util.setTimeout(() -> Service.gI().removeEffPlayer(pl, id), 2000);
            }
        } catch (Exception ex) {
            Logger.logException(ThuCungService.class, ex, "Không hiện được hình chiêu thú cưng");
        }
    }

    /**
     * Sét lan: đánh {@code phanVan} phần vạn sức đánh vào mọi quái còn sống
     * trong bán kính quanh chủ.
     *
     * <p>Đi qua đúng {@code Mob.injured} như chiêu thường, nên quái chết thì
     * rơi đồ, cộng tiềm năng, tính nhiệm vụ y như bị đánh bằng tay. Duyệt trên
     * bản chép của danh sách quái: đánh chết một con có thể khiến khu sửa danh
     * sách ngay giữa vòng lặp.</p>
     *
     * @return số quái trúng
     */
    private int setLan(Player pl, ThuCungDAO.KyNang k, int phanVan) {
        if (pl.zone == null || pl.zone.mobs == null || pl.location == null) {
            return 0;
        }
        int banKinh = k.thamSo > 0 ? k.thamSo : ThuCungDAO.BAN_KINH_MAC_DINH;
        long dame = phanCua(pl.nPoint.dame, phanVan);
        if (dame <= 0) {
            return 0;
        }
        int trung = 0;
        for (nro.entity.mob.Mob m : new ArrayList<>(pl.zone.mobs)) {
            try {
                if (m == null || m.isDie() || m.location == null) {
                    continue;
                }
                if (Util.getDistance(pl.location.x, pl.location.y,
                        m.location.x, m.location.y) > banKinh) {
                    continue;
                }
                m.injured(pl, dame, true);
                trung++;
            } catch (Exception ex) {
                Logger.logException(ThuCungService.class, ex, "Sét lan lỗi trên một con quái");
            }
        }
        return trung;
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
            int giay = ThuCungDAO.laTucThi(k.loai) ? 3 : Math.max(1, k.giay);
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
        for (Buff b : pl.tcBuff) {
            if (b != null && b.het <= bayGio && b.hieuUng > 0) {
                Service.gI().removeEffPlayer(pl, b.hieuUng);
            }
        }
        pl.tcBuff.removeIf(b -> b == null || b.het <= bayGio);
    }

    /**
     * Việc theo thời gian của chiêu thú cưng — gọi mỗi vòng {@code Player.update}.
     *
     * <p>Vùng hồi máu cần nhịp một giây; lớp hết hạn cần gỡ hình ngay cả khi
     * người chơi đứng yên không đánh ai (lúc đó không có đòn nào gọi
     * {@link #tong} để dọn hộ). Không có lớp nào thì trả về ngay: hàm này chạy
     * cho mọi người chơi mọi vòng.</p>
     */
    public void capNhat(Player pl) {
        if (pl == null || pl.tcBuff == null || pl.tcBuff.isEmpty()) {
            return;
        }
        long bayGio = System.currentTimeMillis();
        try {
            for (Buff b : pl.tcBuff) {
                if (b.loai != ThuCungDAO.LOAI_VUNG_HOI_MAU || b.het <= bayGio
                        || bayGio < b.lanHoiToi) {
                    continue;
                }
                b.lanHoiToi = bayGio + 1000L;
                hoiVung(pl, b);
            }
            donBuff(pl, bayGio);
        } catch (Exception ex) {
            Logger.logException(ThuCungService.class, ex, "Lỗi nhịp chiêu thú cưng");
        }
    }

    /**
     * Một nhịp vùng hồi máu: hồi cho chủ, đệ tử của chủ, và người cùng bang
     * đứng trong bán kính.
     *
     * <p>Chỉ người cùng bang chứ không phải ai đứng gần: hồi cho cả người lạ
     * thì đứng cạnh kẻ đang đánh mình cũng hồi máu cho nó.</p>
     */
    private void hoiVung(Player chu, Buff b) {
        if (chu.zone == null) {
            return;
        }
        int banKinh = b.thamSo > 0 ? b.thamSo : ThuCungDAO.BAN_KINH_MAC_DINH;
        for (Player p : new ArrayList<>(chu.zone.getPlayers())) {
            if (p == null || p.isDie() || p.nPoint == null || p.location == null) {
                continue;
            }
            boolean laMinh = p == chu || p == chu.Detu;
            boolean cungBang = chu.clan != null && p.clan != null
                    && chu.clan.id == p.clan.id;
            if (!laMinh && !cungBang) {
                continue;
            }
            if (p != chu && Util.getDistance(chu, p) > banKinh) {
                continue;
            }
            if (p.nPoint.hp >= p.nPoint.hpMax) {
                continue;
            }
            long hoi = phanCua(p.nPoint.hpMax, b.phanVan);
            if (hoi <= 0) {
                continue;
            }
            p.nPoint.hp = Math.min(p.nPoint.hpMax, p.nPoint.hp + hoi);
            nro.service.PlayerService.gI().sendInfoHpMpMoney(p);
        }
    }

    /**
     * Cho khiên hứng sát thương trước. Khiên cạn thì vỡ (gỡ hình luôn), phần
     * sát thương còn thừa mới tới người.
     */
    private double anKhien(Player pl, double damage) {
        if (pl == null || pl.tcBuff == null || pl.tcBuff.isEmpty() || damage <= 0) {
            return damage;
        }
        long bayGio = System.currentTimeMillis();
        for (Buff b : pl.tcBuff) {
            if (b.loai != ThuCungDAO.LOAI_KHIEN || b.het <= bayGio || b.khien <= 0) {
                continue;
            }
            double chan = Math.min(damage, b.khien);
            b.khien -= (long) chan;
            damage -= chan;
            if (b.khien <= 0) {
                b.het = bayGio;
            }
            if (damage <= 0) {
                return 0;
            }
        }
        return damage;
    }

    /** Phần trăm hút máu cộng thêm (nguyên), đọc ở chỗ game tính hút máu. */
    public int themHutMau(Player pl) {
        return tong(pl, ThuCungDAO.LOAI_HUT_MAU, -1) / 100;
    }

    /** Phần trăm phản sát thương cộng thêm, đọc ở chỗ game tính phản sát thương. */
    public int themPhanDon(Player pl) {
        return tong(pl, ThuCungDAO.LOAI_PHAN_DON, -1) / 100;
    }

    /** Phần trăm né đòn cộng thêm, đọc ở chỗ game tính né. */
    public int themNeDon(Player pl) {
        return tong(pl, ThuCungDAO.LOAI_NE_DON, -1) / 100;
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
            t += b.phanVan;
        }
        return t;
    }

    /** Phần vạn viết ra phần trăm: 1000 → "10", 1050 → "10,5", 1025 → "10,25". */
    public static String inPhanVan(int phanVan) {
        int nguyen = phanVan / 100;
        int le = Math.abs(phanVan % 100);
        if (le == 0) {
            return String.valueOf(nguyen);
        }
        return nguyen + "," + (le % 10 == 0 ? String.valueOf(le / 10)
                : (le < 10 ? "0" + le : String.valueOf(le)));
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
        int phanVan = tong(pl, ThuCungDAO.LOAI_SUC_DANH, -1)
                + tong(pl, ThuCungDAO.LOAI_CHIEU, idChieu)
                + pl.tcNoKich;
        // No kich chi an dung mot don.
        pl.tcNoKich = 0;
        if (phanVan <= 0) {
            return dameGoc;
        }
        // Nhan kieu double: dameGoc * phanVan co the tran long voi dame cuc lon.
        return dameGoc + (long) (dameGoc * (phanVan / 10000d));
    }

    /** Phần trăm chí mạng cộng thêm; lẻ thì làm tròn xuống vì chí mạng tính nguyên. */
    public int themChiMang(Player pl) {
        return tong(pl, ThuCungDAO.LOAI_CHI_MANG, -1) / 100;
    }

    public double giamSatThuong(Player pl, double damage) {
        damage = anKhien(pl, damage);
        int phanVan = tong(pl, ThuCungDAO.LOAI_GIAM_SAT_THUONG, -1);
        if (phanVan <= 0) {
            return damage;
        }
        if (phanVan > 9000) {
            // Chan o 90%: de admin go nham 100 la nhan vat bat tu.
            phanVan = 9000;
        }
        return damage - damage * phanVan / 10000d;
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
            // Ghi o CUOI goi: client cu doc het phan tren roi dung, khong lech.
            msg.writer().writeShort(ThuCungDAO.chieuCongMoiCap());
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
            // Cap chieu cua tung con, gom thanh mot khoi o CUOI goi thay vi chen
            // vao tung ban ghi: client cu doc het cac ban ghi roi dung, khoi
            // nay nam thua o cuoi chu khong lam lech ban ghi nao.
            for (ThuCungDAO.ThuSoHuu t : ds) {
                for (int i = 1; i <= ThuCungDAO.SO_KY_NANG; i++) {
                    msg.writer().writeShort(t.capChieu(i));
                }
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
