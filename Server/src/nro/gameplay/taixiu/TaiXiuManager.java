package nro.gameplay.taixiu;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.repository.dao.TaiXiuDAO;
import nro.server.Client;
import nro.service.Service;
import nro.service.TaiXiuService;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Bàn Tài Xỉu dùng chung cho cả máy chủ.
 *
 * <h2>Một bàn, không phải mỗi người một bàn</h2>
 *
 * <p>Cả máy chủ chung <b>một</b> vòng đếm ngược, nên ai mở bảng lên cũng thấy
 * đúng con số giây đó và đúng số tiền hai cửa. Đây là điểm khác căn bản so với
 * một trò chơi đơn: trạng thái nằm ở máy chủ, client chỉ vẽ lại.</p>
 *
 * <h2>Đếm ngược theo mốc thời gian, không trừ dần</h2>
 *
 * <p>Vòng lặp giữ {@link #ketThucLuc} là <b>mốc thời gian tuyệt đối</b> rồi lấy
 * hiệu với hiện tại, chứ không giữ một biến giây rồi trừ đi mỗi nhịp. Trừ dần
 * thì mỗi nhịp trễ vài mili giây, chạy vài giờ là lệch hẳn — mà đây là tiền,
 * không phải hiệu ứng.</p>
 *
 * <h2>Thỏi vàng: trừ khoá trước, trả về luôn là khoá</h2>
 *
 * <p>Thỏi vàng là <b>vật phẩm</b> mã 457 trong hành trang, không phải một con số
 * trên nhân vật. Bản khoá là cùng vật phẩm đó có thuộc tính 30. Đặt cược trừ
 * khoá trước rồi mới tới thường; tiền trả về (cả gốc lẫn lãi) luôn là bản khoá,
 * nên vàng thường chỉ chảy ra chứ không chảy vào.</p>
 */
public class TaiXiuManager {

    private static TaiXiuManager instance;

    public static TaiXiuManager gI() {
        if (instance == null) {
            instance = new TaiXiuManager();
        }
        return instance;
    }

    // ------------------------------------------------------------------
    //  Luật chơi
    // ------------------------------------------------------------------

    /** Mã vật phẩm thỏi vàng trong hành trang. */
    public static final int ID_THOI_VANG = 457;

    /** Mã thuộc tính đánh dấu vật phẩm đã khoá. */
    public static final int OPTION_KHOA = 30;

    /** Trả 1.7 lần tiền cược: 1.0 là gốc, 0.7 là tiền thắng. */
    public static final double TY_LE_THANG = 1.7;

    /** Giây mỗi vòng đặt cược. */
    public static final int GIAY_DAT_CUOC = 30;

    /**
     * Giây xóc bát — bát rung, rồi hé dần cho người chơi "nặn".
     *
     * <p><b>Xúc xắc tung ngay ở ĐẦU pha này</b> và gửi xuống client cùng lúc.
     * Nghe thì như để lộ sớm, nhưng cửa cược đã đóng trước đó rồi — biết trước
     * mười giây cũng không đặt thêm được đồng nào, nên không có gì để lợi
     * dụng.</p>
     *
     * <p>Mười một giây chia làm hai: một giây đầu xóc, mười giây sau để người
     * chơi tự nặn bát. Client biết cách chia này qua hằng số riêng của nó.</p>
     *
     * <p>Đổi lại thì hiệu ứng kéo bát mới <b>thật thà</b>: mặt hé ra dưới vành
     * bát đúng là mặt cuối cùng. Nếu giấu tới phút chót thì client buộc phải vẽ
     * mặt giả trong lúc hé, rồi tới lúc mở hẳn con số nhảy sang số khác ngay
     * trước mắt người chơi — nhìn là biết bịp.</p>
     */
    public static final int GIAY_LAC = 11;

    /** Giây hiện xúc xắc trước khi mở ván mới. */
    public static final int GIAY_KET_QUA = 3;

    public static final byte GIAI_DOAN_DAT_CUOC = 0;
    public static final byte GIAI_DOAN_LAC = 1;
    public static final byte GIAI_DOAN_KET_QUA = 2;

    public static final byte CUA_XIU = 0;
    public static final byte CUA_TAI = 1;

    /** Số ván gần nhất giữ lại để vẽ dải lịch sử. */
    private static final int SO_LICH_SU = 15;

    // ------------------------------------------------------------------
    //  Trạng thái bàn
    // ------------------------------------------------------------------

    /**
     * Một lượt cược của một người.
     *
     * <p>Giữ cả {@code Player} lẫn tên: {@code Player} để trả thưởng nhanh, tên
     * để ghi nhật ký còn đọc được sau khi người đó thoát.</p>
     */
    private static class Cuoc {
        final Player nguoi;
        /** Chép ra để luồng ghi lịch sử không phải chạm vào {@code Player}. */
        final long id;
        final String ten;
        byte cua;
        long soThoi;

        Cuoc(Player nguoi, byte cua, long soThoi) {
            this.nguoi = nguoi;
            this.id = nguoi.id;
            this.ten = nguoi.name;
            this.cua = cua;
            this.soThoi = soThoi;
        }
    }

    private long phien = 1;

    // volatile: luong Tai Xiu ghi, cac luong mang doc (khi nguoi choi bam).
    // Khong volatile thi luong doc co the thay gia tri cu ke ca sau khi luong
    // kia da ghi xong — nguoi choi dat cuoc duoc vao van da chot.
    private volatile byte giaiDoan = GIAI_DOAN_DAT_CUOC;
    private volatile long ketThucLuc;

    private final int[] xucXac = new int[3];
    private volatile long tongTai;
    private volatile long tongXiu;

    private final Map<Long, Cuoc> cuocs = new ConcurrentHashMap<>();
    private final LinkedList<Byte> lichSu = new LinkedList<>();

    /**
     * Những người đang mở bảng.
     *
     * <p>Chỉ gửi nhịp đếm ngược cho những người này chứ không phát cho cả máy
     * chủ: một gói mỗi giây nhân với toàn bộ người online là lãng phí đường
     * truyền cho thứ mà phần lớn không nhìn.</p>
     */
    private final Set<Long> nguoiXem = ConcurrentHashMap.newKeySet();

    /**
     * Nguồn ngẫu nhiên.
     *
     * <p>Dùng {@link SecureRandom} chứ không phải {@code Random} thường: đây là
     * chỗ ăn thua bằng tài sản trong game, mà {@code Random} thường sinh ra dãy
     * đoán trước được nếu biết vài kết quả liên tiếp. Ba lần tung mỗi 36 giây
     * thì chi phí không đáng kể.</p>
     */
    private final SecureRandom ngauNhien = new SecureRandom();

    private TaiXiuManager() {
        // Dem tiep tu van cuoi da ghi, khong quay ve 1. Hai van khac nhau mang
        // cung mot so thi lich su doc ra vo nghia, ma khoa chinh cung dung nhau.
        try {
            phien = TaiXiuDAO.phienLonNhat();
        } catch (Exception e) {
            Logger.logException(TaiXiuManager.class, e);
        }
        moVongMoi();
    }

    // ------------------------------------------------------------------
    //  Vòng chạy
    // ------------------------------------------------------------------

    /** Khởi động luồng đếm ngược. Gọi một lần lúc máy chủ dựng xong. */
    public void batDau() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    chay();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    Logger.logException(TaiXiuManager.class, e);
                }
            }
        }, "Tai Xiu");
        t.setDaemon(true);
        t.start();
    }

    private void chay() {
        if (giayConLai() > 0) {
            TaiXiuService.gI().guiNhip();
            return;
        }
        switch (giaiDoan) {
            case GIAI_DOAN_DAT_CUOC:
                tungXucXac();
                giaiDoan = GIAI_DOAN_LAC;
                ketThucLuc = System.currentTimeMillis() + GIAY_LAC * 1000L;
                TaiXiuService.gI().guiTrangThaiCho();
                break;
            case GIAI_DOAN_LAC:
                chotVan();
                break;
            default:
                moVongMoi();
                TaiXiuService.gI().guiTrangThaiCho();
                break;
        }
    }

    public int giayConLai() {
        long conLai = ketThucLuc - System.currentTimeMillis();
        return conLai <= 0 ? 0 : (int) ((conLai + 999) / 1000);
    }

    private synchronized void moVongMoi() {
        phien++;
        giaiDoan = GIAI_DOAN_DAT_CUOC;
        ketThucLuc = System.currentTimeMillis() + GIAY_DAT_CUOC * 1000L;
        tongTai = 0;
        tongXiu = 0;
        cuocs.clear();
    }

    /**
     * Chia thưởng theo ba con đã tung, rồi chuyển sang pha hiện kết quả.
     *
     * <p>Xúc xắc đã tung từ đầu pha xóc bát; ở đây chỉ đọc ra và trả tiền. Trả
     * thưởng để tới đây chứ không làm ngay lúc tung, để thông báo "bạn thắng"
     * không bay tới trước khi bát được mở.</p>
     */
    /**
     * Tung ba con xúc xắc.
     *
     * <p><b>Không có luật bão.</b> Bộ ba giống nhau vẫn cộng tổng như thường —
     * tổng 3..10 là Xỉu, 11..18 là Tài.</p>
     */
    private synchronized void tungXucXac() {
        for (int i = 0; i < 3; i++) {
            xucXac[i] = ngauNhien.nextInt(6) + 1;
        }
    }

    /**
     * Cửa thắng của ván đang chạy, hoặc {@code -1} khi chưa tung xúc xắc.
     *
     * <p>Luật "tổng bao nhiêu thì là Tài" <b>chỉ được viết ở đây</b>. Client đọc
     * con số này để vẽ chứ không tự so ngưỡng: luật nằm trong mã client là luật
     * sửa được, và nếu sau này đổi ngưỡng thì client cũ sẽ hiện một đằng còn
     * máy chủ trả tiền một nẻo.</p>
     */
    public synchronized byte ketQuaHienTai() {
        if (giaiDoan == GIAI_DOAN_DAT_CUOC) {
            return -1;
        }
        int tong = xucXac[0] + xucXac[1] + xucXac[2];
        return tong >= 11 ? CUA_TAI : CUA_XIU;
    }

    private synchronized void chotVan() {
        int tong = xucXac[0] + xucXac[1] + xucXac[2];
        byte ketQua = tong >= 11 ? CUA_TAI : CUA_XIU;

        lichSu.addFirst(ketQua);
        while (lichSu.size() > SO_LICH_SU) {
            lichSu.removeLast();
        }

        giaiDoan = GIAI_DOAN_KET_QUA;
        ketThucLuc = System.currentTimeMillis() + GIAY_KET_QUA * 1000L;

        for (Cuoc c : cuocs.values()) {
            try {
                traThuong(c, ketQua);
            } catch (Exception e) {
                Logger.logException(TaiXiuManager.class, e);
            }
        }
        TaiXiuService.gI().guiKetQua(xucXac, ketQua);
        ghiLichSu(ketQua);
        cuocs.clear();
    }

    /**
     * Ghi ván vừa chốt xuống cơ sở dữ liệu.
     *
     * <p>Đẩy sang luồng riêng: hàm này gọi từ giữa vòng chơi, mà một câu lệnh
     * ghi chậm là cả bàn đứng theo — đồng hồ đếm ngược của mọi người khựng lại
     * vì một chuyện chẳng ai nhìn thấy.</p>
     *
     * <p>Chép dữ liệu ra trước khi giao cho luồng kia: {@code cuocs} bị xoá ngay
     * sau lời gọi này, luồng ghi mà đọc thẳng vào đó thì có khi thấy bảng rỗng.</p>
     */
    private void ghiLichSu(byte ketQua) {
        final long soPhien = phien;
        final int a = xucXac[0];
        final int b = xucXac[1];
        final int c = xucXac[2];
        final long tai = tongTai;
        final long xiu = tongXiu;
        final List<Cuoc> banSao = new ArrayList<>(cuocs.values());

        Thread t = new Thread(() -> {
            try {
                TaiXiuDAO.ghiPhien(soPhien, a, b, c, ketQua, tai, xiu,
                        banSao.size());
                for (Cuoc cc : banSao) {
                    boolean thang = cc.cua == ketQua;
                    TaiXiuDAO.ghiCuoc(soPhien, cc.id, cc.ten, cc.cua, cc.soThoi,
                            ketQua, thang, thang ? tinhTraVe(cc.soThoi) : 0);
                }
            } catch (Exception e) {
                Logger.logException(TaiXiuManager.class, e);
            }
        }, "Tai Xiu ghi lich su");
        t.setDaemon(true);
        t.start();
    }

    private void traThuong(Cuoc c, byte ketQua) {
        Player pl = c.nguoi;
        if (pl == null || pl.getSession() == null || !pl.getSession().isConnected()) {
            // Nguoi choi da roi game: khong tra vao dau duoc. Khong the xay ra
            // trong luc chay binh thuong vi nguoiChoiRoiGame() da hoan tien
            // truoc do; ghi lai de con biet neu co ke ho.
            Logger.error("[TaiXiu] Khong tra duoc thuong cho " + c.ten
                    + " (da roi game), cuoc " + c.soThoi + " thoi.\n");
            return;
        }
        if (c.cua != ketQua) {
            Service.gI().sendThongBao(pl, "Tài Xỉu: bạn thua "
                    + c.soThoi + " thỏi vàng.");
            return;
        }
        long traVe = tinhTraVe(c.soThoi);
        if (themThoiVangKhoa(pl, traVe)) {
            Service.gI().sendThongBao(pl, "Tài Xỉu: bạn thắng! Nhận "
                    + traVe + " thỏi vàng khoá.");
        } else {
            Logger.error("[TaiXiu] Hanh trang day, khong tra duoc " + traVe
                    + " thoi cho " + c.ten + ".\n");
            Service.gI().sendThongBao(pl,
                    "Tài Xỉu: bạn thắng nhưng hành trang đầy, không nhận được thưởng!");
        }
    }

    // ------------------------------------------------------------------
    //  Đặt cược
    // ------------------------------------------------------------------

    /**
     * Nhận một lượt đặt cược.
     *
     * <p>Cộng dồn nếu người đó đã đặt trong ván này, nhưng <b>không cho đổi
     * cửa</b>: đặt cả hai cửa thì thắng thua triệt tiêu nhau, chỉ tổ làm rối
     * bảng tổng.</p>
     */
    // synchronized: dat cuoc chay tren luong MANG (moi nguoi choi mot luong),
    // con chot van va mo van chay tren luong Tai Xiu. Hai ben cung sua tongTai,
    // tongXiu va bang cuoc. Khong khoa thi hai nguoi dat cung luc co the lam
    // mat mot lan cong don — ma day la tien.
    public synchronized void datCuoc(Player pl, byte cua, long soThoi) {
        if (pl == null) {
            return;
        }
        if (giaiDoan != GIAI_DOAN_DAT_CUOC) {
            Service.gI().sendThongBao(pl, "Đã hết giờ đặt cược, chờ ván sau!");
            return;
        }
        if (cua != CUA_TAI && cua != CUA_XIU) {
            return;
        }
        if (soThoi <= 0) {
            return;
        }

        // Moi van MOT lan dat, khong cong don va khong doi cua.
        //
        // Nguoi choi go so roi bam DAT la chot — client khoa luon hai nut do lai
        // cho toi van sau. Chan ca o day chu khong chi o client: client sua duoc,
        // may chu thi khong.
        if (cuocs.containsKey(pl.id)) {
            Service.gI().sendThongBao(pl, "Bạn đã đặt cược ván này rồi!");
            return;
        }

        long dangCoTrongTui = demThoiVang(pl, true) + demThoiVang(pl, false);
        if (dangCoTrongTui < soThoi) {
            Service.gI().sendThongBao(pl, "Bạn không đủ thỏi vàng!");
            return;
        }

        // Chan truoc khi tru tien: thang ma khong co o de nhan thi tien thuong
        // bien mat. Tra ve luon la mot chong thoi vang khoa nen can it nhat mot
        // o trong, tru khi trong tui da co san chong thoi vang khoa de cong don.
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0
                && demThoiVang(pl, true) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đầy, không thể nhận thưởng!");
            return;
        }

        if (!truThoiVang(pl, soThoi)) {
            Service.gI().sendThongBao(pl, "Trừ thỏi vàng thất bại!");
            return;
        }

        cuocs.put(pl.id, new Cuoc(pl, cua, soThoi));
        if (cua == CUA_TAI) {
            tongTai += soThoi;
        } else {
            tongXiu += soThoi;
        }
        TaiXiuService.gI().guiTrangThai(pl);
    }

    /**
     * Hoàn tiền khi người chơi rời game giữa ván.
     *
     * <p>Gọi từ {@code Client.removePlayerFromCollections}. Không hoàn thì thỏi
     * vàng đã trừ biến mất hẳn — máy chủ không có đường nào trả vào hành trang
     * của một người đang offline.</p>
     */
    public synchronized void nguoiChoiRoiGame(Player pl) {
        if (pl == null) {
            return;
        }
        nguoiXem.remove(pl.id);
        Cuoc c = cuocs.remove(pl.id);
        if (c == null) {
            return;
        }
        if (c.cua == CUA_TAI) {
            tongTai -= c.soThoi;
        } else {
            tongXiu -= c.soThoi;
        }
        // Hoan bang ban KHOA. Nguoi choi co the da dat bang vang thuong, nhung
        // hoan thuong lai cho ho mot duong rua vang khoa thanh vang thuong:
        // dat cuoc bang khoa, thoat game, nhan lai thuong.
        themThoiVangKhoa(pl, c.soThoi);
        Logger.warning("[TaiXiu] " + c.ten + " roi game giua van, hoan "
                + c.soThoi + " thoi vang khoa.\n");
    }

    // ------------------------------------------------------------------
    //  Thỏi vàng
    // ------------------------------------------------------------------

    /** Đếm thỏi vàng trong hành trang, {@code khoa} chọn bản khoá hay bản thường. */
    public long demThoiVang(Player pl, boolean khoa) {
        if (pl == null || pl.inventory == null || pl.inventory.itemsBag == null) {
            return 0;
        }
        long tong = 0;
        for (Item it : pl.inventory.itemsBag) {
            if (laThoiVang(it) && InventoryService.gI().haveOption(it, OPTION_KHOA) == khoa) {
                tong += it.quantity;
            }
        }
        return tong;
    }

    private static boolean laThoiVang(Item it) {
        return it != null && it.isNotNullItem() && it.template != null
                && it.template.id == ID_THOI_VANG;
    }

    /**
     * Trừ thỏi vàng, <b>ưu tiên bản khoá trước</b>.
     *
     * <p>Ví dụ có 10 khoá và 20 thường, cược 20 thì trừ hết 10 khoá rồi trừ
     * tiếp 10 thường.</p>
     *
     * <p>Gom danh sách vật phẩm ra trước rồi mới trừ, không vừa duyệt vừa sửa:
     * {@code subQuantityItemsBag} có thể xoá vật phẩm khỏi chính danh sách đang
     * duyệt.</p>
     */
    private boolean truThoiVang(Player pl, long soThoi) {
        long can = soThoi;
        for (int luot = 0; luot < 2 && can > 0; luot++) {
            boolean khoa = (luot == 0);
            List<Item> ds = new ArrayList<>();
            for (Item it : pl.inventory.itemsBag) {
                if (laThoiVang(it) && InventoryService.gI().haveOption(it, OPTION_KHOA) == khoa) {
                    ds.add(it);
                }
            }
            for (Item it : ds) {
                if (can <= 0) {
                    break;
                }
                int tru = (int) Math.min(can, it.quantity);
                InventoryService.gI().subQuantityItemsBag(pl, it, tru);
                can -= tru;
            }
        }
        InventoryService.gI().sendItemBag(pl);
        return can == 0;
    }

    /** Thêm thỏi vàng <b>khoá</b> vào hành trang. */
    private boolean themThoiVangKhoa(Player pl, long soThoi) {
        if (soThoi <= 0) {
            return true;
        }
        // Item.quantity la int, nen chia thanh nhieu chong neu qua lon.
        long con = soThoi;
        while (con > 0) {
            int lan = (int) Math.min(con, Integer.MAX_VALUE);
            Item it = ItemService.gI().createNewItemLock(ID_THOI_VANG, lan);
            if (!InventoryService.gI().addItemBag(pl, it)) {
                InventoryService.gI().sendItemBag(pl);
                return false;
            }
            con -= lan;
        }
        InventoryService.gI().sendItemBag(pl);
        return true;
    }

    // ------------------------------------------------------------------
    //  Cho TaiXiuService đọc
    // ------------------------------------------------------------------

    /** Số phiên hiện tại. Người chơi đọc số này khi khiếu nại một ván. */
    public long getPhien() {
        return phien;
    }

    public byte getGiaiDoan() {
        return giaiDoan;
    }

    public long getTongTai() {
        return tongTai;
    }

    public long getTongXiu() {
        return tongXiu;
    }

    public int[] getXucXac() {
        return xucXac;
    }

    public List<Byte> getLichSu() {
        return new ArrayList<>(lichSu);
    }

    public void themNguoiXem(Player pl) {
        nguoiXem.add(pl.id);
    }

    public void boNguoiXem(Player pl) {
        nguoiXem.remove(pl.id);
    }

    /** Cửa người đó đang đặt ván này, {@code -1} nếu chưa đặt. */
    public byte cuaDangDat(Player pl) {
        Cuoc c = cuocs.get(pl.id);
        return c == null ? -1 : c.cua;
    }

    /** Số thỏi người đó đang đặt ván này. */
    public long soThoiDangDat(Player pl) {
        Cuoc c = cuocs.get(pl.id);
        return c == null ? 0 : c.soThoi;
    }

    /** Số thỏi thắng được của người đó ở ván vừa chốt, 0 nếu thua. */
    public long tienThangCuaVanVua(Player pl, byte ketQua) {
        Cuoc c = cuocs.get(pl.id);
        if (c == null || c.cua != ketQua) {
            return 0;
        }
        return tinhTraVe(c.soThoi);
    }

    /**
     * Số thỏi trả về khi thắng: 1.7 lần tiền cược, <b>làm tròn nửa lên</b>.
     *
     * <p>5.4 thành 5, 5.6 thành 6, và đúng 5.5 cũng thành 6.</p>
     *
     * <p>Tính bằng số nguyên chứ không nhân với {@code 1.7} rồi làm tròn:
     * {@code 1.7} không biểu diễn chính xác được bằng số thực nhị phân, nên
     * {@code 5 * 1.7} ra 8.4999... và làm tròn thành 8 thay vì 9. Nhân 17 rồi
     * chia 10 thì không có sai số nào cả.</p>
     *
     * <p>Một chỗ tính duy nhất cho cả lúc trả thưởng lẫn lúc báo cho client —
     * hai công thức riêng là sớm muộn cũng lệch nhau.</p>
     */
    private static long tinhTraVe(long soThoi) {
        return (soThoi * 17 + 5) / 10;
    }

    /** Người đang online trong danh sách xem, bỏ qua ai đã thoát. */
    public List<Player> nguoiDangXem() {
        List<Player> ds = new ArrayList<>();
        for (Long id : nguoiXem) {
            Player pl = Client.gI().getPlayerByID(id);
            if (pl != null) {
                ds.add(pl);
            }
        }
        return ds;
    }
}
