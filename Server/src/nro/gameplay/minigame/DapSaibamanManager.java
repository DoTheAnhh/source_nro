package nro.gameplay.minigame;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import nro.core.log.Logger;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.repository.dao.DapSaibamanDAO;
import nro.repository.dao.MiniGameDAO;
import nro.service.MiniGameService;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Trò <b>Đập Saibaman</b> — chín cái hố, Saibaman trồi lên, đập trúng thì ăn
 * điểm; lỡ tay đập Bulma thì bị trừ. Điểm cả ván quy ra mốc quà.
 *
 * <h2>Mỗi người một ván riêng</h2>
 *
 * <p>Không có phiên chung cả máy chủ: bấm Bắt đầu là trừ vé và dựng một ván
 * cho riêng người đó.</p>
 *
 * <h2>Lịch trồi do máy chủ bốc, điểm do máy chủ tính</h2>
 *
 * <p>Lúc vào ván, máy chủ bốc sẵn <b>cả lịch</b>: con nào, trồi ở hố nào, lúc
 * nào, trong bao lâu — rồi gửi xuống. Hết giờ, client chỉ báo "đã đập trúng con
 * số mấy, ở mili giây thứ mấy". Máy chủ soát từng cú: đúng con đang trồi, đúng
 * quãng nó còn trên mặt đất, hai cú không sát nhau hơn tay người làm được, và
 * không có cú nào trong tương lai. Điểm tính từ những cú hợp lệ theo bảng điểm
 * của máy chủ — client không gửi điểm lên.</p>
 *
 * <h2>Không in thỏi vàng</h2>
 *
 * <p>Một client sửa vẫn có thể "đập trúng hết" — điều không chặn tuyệt đối được
 * với trò phản xạ. Nên trần thiệt hại nằm ở luật quà chứ không ở chỗ bắt gian:
 * <b>thỏi vàng thưởng mỗi ván luôn bị chặn ở (giá vé − 1)</b>, ngay trong mã,
 * kể cả khi admin sửa bảng mốc ghi số lớn hơn. Nên mỗi ván, dù đạt mốc nào, vẫn
 * rút khỏi máy chủ ít nhất 1 thỏi. Quà vật phẩm thì bị giới hạn bởi số ván mỗi
 * ngày, và mọi quà đều <b>khoá</b> — không giao dịch, không ký gửi.</p>
 */
public final class DapSaibamanManager {

    private static DapSaibamanManager instance;

    public static DapSaibamanManager gI() {
        if (instance == null) {
            instance = new DapSaibamanManager();
        }
        return instance;
    }

    public static final int MA_TRO = MiniGameService.TRO_DAP_SAIBAMAN;

    public static final int SO_HO = 9;

    /** Loại con trồi lên. */
    public static final int LOAI_THUONG = 0;
    public static final int LOAI_VANG = 1;
    public static final int LOAI_BULMA = 2;

    /** Hai cú đập hợp lệ phải cách nhau ít nhất chừng này — nhanh hơn là máy bấm. */
    private static final int CACH_TOI_THIEU_MS = 110;

    /** Nới cho độ trễ khung hình khi soát cú đập nằm trong quãng con trồi. */
    private static final int NOI_MS = 250;

    /** Ván không báo kết quả sau (độ dài + chừng này) thì kết thành 0 điểm. */
    private static final long QUA_HAN_THEM_MS = 20_000L;

    /** Một ván đang chơi. */
    public static final class Van {
        final Player pl;
        final long so;
        final long batDau;
        final int daiMs;
        final long ve;
        public final int[] t;
        public final int[] song;
        public final byte[] ho;
        public final byte[] loai;

        Van(Player pl, long so, long batDau, int daiMs, long ve, int[] t, int[] song,
                byte[] ho, byte[] loai) {
            this.pl = pl;
            this.so = so;
            this.batDau = batDau;
            this.daiMs = daiMs;
            this.ve = ve;
            this.t = t;
            this.song = song;
            this.ho = ho;
            this.loai = loai;
        }
    }

    private final Map<Long, Van> vanDangChoi = new ConcurrentHashMap<>();
    private final SecureRandom ngauNhien = new SecureRandom();
    private final AtomicLong soVan = new AtomicLong(-1);

    private DapSaibamanManager() {
        java.util.concurrent.ScheduledExecutorService nhip
                = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread th = new Thread(r, "Dap Saibaman quet van");
                    th.setDaemon(true);
                    return th;
                });
        nhip.scheduleWithFixedDelay(this::donVanQuaHan, 5, 5,
                java.util.concurrent.TimeUnit.SECONDS);
    }

    // ------------------------------------------------------------------
    //  Cấu hình (đọc từ DB)
    // ------------------------------------------------------------------

    public static long ve() {
        return Math.max(1, DapSaibamanDAO.so("ve"));
    }

    public static int luotNgay() {
        return (int) Math.max(1, DapSaibamanDAO.so("luot_ngay"));
    }

    public static int giay() {
        long g = DapSaibamanDAO.so("giay");
        return (int) Math.max(10, Math.min(60, g));
    }

    public static int diem(int loai) {
        switch (loai) {
            case LOAI_VANG:
                return (int) DapSaibamanDAO.so("diem_vang");
            case LOAI_BULMA:
                return (int) DapSaibamanDAO.so("diem_bulma");
            default:
                return (int) DapSaibamanDAO.so("diem_thuong");
        }
    }

    private static int oTrongCan() {
        return (int) Math.max(1, DapSaibamanDAO.so("o_trong"));
    }

    // ------------------------------------------------------------------
    //  Vào ván
    // ------------------------------------------------------------------

    public void batDau(Player pl) {
        if (pl == null) {
            return;
        }
        if (vanDangChoi.containsKey(pl.id)) {
            Service.gI().sendThongBao(pl, "Bạn đang chơi dở một ván!");
            return;
        }
        long ve = ve();
        int daChoi = DapSaibamanDAO.soVanHomNay(pl.id);
        if (daChoi >= luotNgay()) {
            Service.gI().sendThongBao(pl, "Hôm nay bạn đã chơi đủ " + luotNgay()
                    + " ván Đập Saibaman, mai quay lại nhé!");
            return;
        }
        if (KhoVang.demTatCa(pl) < ve) {
            Service.gI().sendThongBao(pl, "Cần " + ve + " thỏi vàng để mua vé!");
            return;
        }
        int can = oTrongCan();
        if (InventoryService.gI().getCountEmptyBag(pl) < can) {
            Service.gI().sendThongBao(pl, "Hành trang cần trống ít nhất " + can
                    + " ô để nhận quà!");
            return;
        }
        if (!KhoVang.tru(pl, ve)) {
            Service.gI().sendThongBao(pl, "Trừ thỏi vàng thất bại!");
            return;
        }
        if (soVan.get() < 0) {
            soVan.compareAndSet(-1, DapSaibamanDAO.vanLonNhat());
        }
        Van v = bocLich(pl, soVan.incrementAndGet(), ve);
        vanDangChoi.put(pl.id, v);
        MiniGameService.gI().sbGuiVanMoi(pl, v, daChoi + 1);
    }

    /**
     * Bốc lịch trồi cho cả ván.
     *
     * <p>Nhịp trồi dày dần và mỗi con lặn nhanh dần theo tiến độ ván: đầu ván
     * chừng 0,85 giây một con, mỗi con đứng 1,25 giây; cuối ván chừng 0,42 giây
     * một con, đứng 0,7 giây. Một hố không trồi hai con chồng lên nhau.</p>
     */
    private Van bocLich(Player pl, long so, long ve) {
        int daiMs = giay() * 1000;
        List<int[]> ds = new ArrayList<>();
        long[] hoRanhLuc = new long[SO_HO];
        int t = 700;
        while (t < daiMs - 500) {
            float p = (float) t / daiMs;
            int khoang = (int) (850 - 430 * p);
            khoang = khoang * (85 + ngauNhien.nextInt(31)) / 100;
            int song = (int) (1250 - 550 * p);
            int loai = LOAI_THUONG;
            int r = ngauNhien.nextInt(100);
            if (r < 8) {
                loai = LOAI_VANG;
                song = song * 65 / 100;
            } else if (r < 22) {
                loai = LOAI_BULMA;
            }
            if (t + song > daiMs) {
                song = daiMs - t;
            }
            // Hố rảnh: bốc ngẫu nhiên trong số hố đã trống ở thời điểm t.
            int[] ranh = new int[SO_HO];
            int n = 0;
            for (int h = 0; h < SO_HO; h++) {
                if (hoRanhLuc[h] <= t) {
                    ranh[n++] = h;
                }
            }
            if (n > 0 && song >= 250) {
                int h = ranh[ngauNhien.nextInt(n)];
                hoRanhLuc[h] = t + song + 150L;
                ds.add(new int[] { t, song, h, loai });
            }
            t += Math.max(250, khoang);
        }
        int m = ds.size();
        int[] mt = new int[m];
        int[] ms = new int[m];
        byte[] mh = new byte[m];
        byte[] ml = new byte[m];
        for (int i = 0; i < m; i++) {
            int[] d = ds.get(i);
            mt[i] = d[0];
            ms[i] = d[1];
            mh[i] = (byte) d[2];
            ml[i] = (byte) d[3];
        }
        return new Van(pl, so, System.currentTimeMillis(), daiMs, ve, mt, ms, mh, ml);
    }

    // ------------------------------------------------------------------
    //  Kết thúc ván
    // ------------------------------------------------------------------

    /**
     * Client báo các cú đập trúng.
     *
     * @param chiSo  số thứ tự con đã đập (theo lịch máy chủ gửi)
     * @param lucMs  mili giây tính từ đầu ván của cú đập ấy
     */
    public void baoKetQua(Player pl, int[] chiSo, int[] lucMs) {
        if (pl == null) {
            return;
        }
        Van v = vanDangChoi.get(pl.id);
        if (v == null) {
            return;
        }
        long daQua = System.currentTimeMillis() - v.batDau;
        if (daQua < v.daiMs - 500) {
            // Chua het gio ma da bao: client sua. Khong nhan, van con treo cho
            // toi luc het han thi ket 0 diem.
            Logger.warning("[DapSaibaman] " + pl.name + " bao ket qua som ("
                    + daQua + "ms / " + v.daiMs + "ms). Bo qua.\n");
            return;
        }
        if (vanDangChoi.remove(pl.id) == null) {
            return;
        }
        int diem = tinhDiem(v, chiSo, lucMs, daQua, pl.name);
        ketThuc(v, diem);
    }

    /** Soát từng cú đập rồi cộng điểm. */
    private static int tinhDiem(Van v, int[] chiSo, int[] lucMs, long daQua, String ten) {
        int n = Math.min(chiSo.length, lucMs.length);
        int[][] cu = new int[n][];
        for (int i = 0; i < n; i++) {
            cu[i] = new int[] { chiSo[i], lucMs[i] };
        }
        Arrays.sort(cu, (a, b) -> Integer.compare(a[1], b[1]));
        boolean[] daDap = new boolean[v.t.length];
        int lucTruoc = Integer.MIN_VALUE / 2;
        int diem = 0;
        int boQua = 0;
        for (int[] c : cu) {
            int i = c[0];
            int luc = c[1];
            if (i < 0 || i >= v.t.length || daDap[i]
                    || luc < v.t[i] || luc > v.t[i] + v.song[i] + NOI_MS
                    || luc > daQua + NOI_MS
                    || luc - lucTruoc < CACH_TOI_THIEU_MS) {
                boQua++;
                continue;
            }
            daDap[i] = true;
            lucTruoc = luc;
            diem += diem(v.loai[i]);
        }
        if (boQua > 3) {
            Logger.warning("[DapSaibaman] " + ten + " co " + boQua
                    + " cu dap khong hop le trong van " + v.so + ".\n");
        }
        return Math.max(0, diem);
    }

    /** Chốt ván: tìm mốc, phát quà, ghi lịch sử, báo client. */
    private void ketThuc(Van v, int diem) {
        Player pl = v.pl;
        List<DapSaibamanDAO.Moc> ds = DapSaibamanDAO.moc();
        int chiMoc = -1;
        for (int i = 0; i < ds.size(); i++) {
            if (diem >= ds.get(i).diem) {
                chiMoc = i;
            }
        }
        long thoiTra = 0;
        List<int[]> daNhan = new ArrayList<>();
        if (chiMoc >= 0) {
            // Tran thoi vang: moi van luon tieu it nhat 1 thoi (xem dau lop).
            long tranThoi = Math.max(0, v.ve - 1);
            for (DapSaibamanDAO.Qua q : ds.get(chiMoc).qua) {
                int sl = q.soLuong;
                if (q.itemId == KhoVang.ID_THOI_VANG) {
                    sl = (int) Math.min(sl, tranThoi - thoiTra);
                    if (sl <= 0) {
                        continue;
                    }
                    thoiTra += sl;
                }
                if (phat(pl, q.itemId, sl)) {
                    daNhan.add(new int[] { q.itemId, sl });
                }
            }
            InventoryService.gI().sendItemBag(pl);
        }
        ghiVan(pl, v, diem, chiMoc, thoiTra);
        if (chiMoc >= 0) {
            Service.gI().sendThongBao(pl, "Đập Saibaman: " + diem + " điểm — đạt mốc "
                    + ds.get(chiMoc).diem + "!");
        } else {
            Service.gI().sendThongBao(pl, "Đập Saibaman: " + diem
                    + " điểm, chưa tới mốc quà nào.");
        }
        MiniGameService.gI().sbGuiKetQua(pl, diem, chiMoc, daNhan);
    }

    /** Phát một món quà, luôn khoá (không giao dịch được). */
    private static boolean phat(Player pl, int itemId, int soLuong) {
        try {
            if (soLuong <= 0) {
                return false;
            }
            Item it = ItemService.gI().createNewItemLock(itemId, soLuong);
            if (InventoryService.gI().addItemBag(pl, it)) {
                return true;
            }
            Logger.error("[DapSaibaman] Hanh trang day, khong phat duoc " + itemId
                    + " x" + soLuong + " cho " + pl.name + ".\n");
            Service.gI().sendThongBao(pl, "Hành trang đầy, mất một phần quà!");
        } catch (Exception e) {
            Logger.logException(DapSaibamanManager.class, e, "Loi phat qua Dap Saibaman");
        }
        return false;
    }

    private static void ghiVan(Player pl, Van v, int diem, int chiMoc, long thoiTra) {
        final long id = pl.id;
        final String ten = pl.name;
        Thread th = new Thread(() -> {
            try {
                MiniGameDAO.ghiCuoc(MA_TRO, v.so, id, ten, chiMoc, v.ve, diem,
                        chiMoc >= 0, thoiTra);
            } catch (Exception e) {
                Logger.logException(DapSaibamanManager.class, e);
            }
        }, "Dap Saibaman ghi lich su");
        th.setDaemon(true);
        th.start();
    }

    /** Ván không ai báo kết quả (tắt game, rớt mạng) thì kết 0 điểm. */
    private void donVanQuaHan() {
        try {
            long bayGio = System.currentTimeMillis();
            for (Map.Entry<Long, Van> e : vanDangChoi.entrySet()) {
                Van v = e.getValue();
                if (bayGio - v.batDau <= v.daiMs + QUA_HAN_THEM_MS) {
                    continue;
                }
                if (vanDangChoi.remove(e.getKey()) == null || v.pl == null) {
                    continue;
                }
                ketThuc(v, 0);
            }
        } catch (Exception ex) {
            Logger.logException(DapSaibamanManager.class, ex, "Loi quet van Dap Saibaman");
        }
    }

    /**
     * Người chơi rời game giữa ván: ván kết 0 điểm, vé không hoàn.
     *
     * <p>Hoàn vé thì thoát game thành một nước đi — thấy điểm thấp là rút
     * phích làm lại.</p>
     */
    public void nguoiChoiRoiGame(Player pl) {
        if (pl == null) {
            return;
        }
        Van v = vanDangChoi.remove(pl.id);
        if (v != null) {
            ghiVan(pl, v, 0, -1, 0);
        }
    }

    public boolean dangChoi(Player pl) {
        return pl != null && vanDangChoi.containsKey(pl.id);
    }
}
