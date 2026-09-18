package nro.gameplay.minigame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.repository.dao.MiniGameDAO;
import nro.service.MiniGameService;
import nro.service.Service;

/**
 * Bàn <b>Đua Vịt</b> — năm con vịt, đặt một con, con nào về nhất thì ăn.
 *
 * <p>Trước là Đua Ngựa sáu con; giữ nguyên tên lớp và mã trò (2) để không đổi giao
 * thức hay lịch sử trong cơ sở dữ liệu.</p>
 *
 * <h2>Luật</h2>
 *
 * <p>Năm con cơ hội <b>bằng nhau</b>, mỗi con một phần năm. Trả về 4,5× tiền
 * cược — công bằng là 5×, phần chênh là lợi thế nhà cái, đúng 10 phần trăm.</p>
 *
 * <h2>Đường đua do máy chủ dựng sẵn</h2>
 *
 * <p>Máy chủ xáo <i>thứ tự về đích</i> ngay đầu pha bơi và gửi xuống. Client chỉ
 * dựng hoạt hình theo thứ tự ấy (gieo theo số phiên, nên ai xem cũng thấy cùng một
 * cuộc đua). Để client tự chạy đua rồi báo ai thắng thì kết quả nằm trong tay
 * máy người chơi.</p>
 *
 * <h2>Nhịp</h2>
 *
 * <p>30 giây đặt, 30 giây bơi, 7 giây xem kết quả. Ba mươi giây bơi khớp
 * <code>DV_GIAY_DUA</code> bên client — client xếp con về chậm nhất tới đích trước
 * giây thứ 29.</p>
 */
public final class DuaNguaManager extends VongChoi {

    private static DuaNguaManager instance;

    public static DuaNguaManager gI() {
        if (instance == null) {
            instance = new DuaNguaManager();
        }
        return instance;
    }

    public static final int MA_TRO = MiniGameService.TRO_DUA_NGUA;

    /** Số vịt. Khớp <code>DN_SO_NGUA</code> bên client. */
    public static final int SO_NGUA = 5;

    public static final int GIAY_DAT_CUOC = 30;
    public static final int GIAY_DUA = 30;
    public static final int GIAY_KET_QUA = 7;

    public static final byte GD_DAT_CUOC = 0;
    public static final byte GD_DUA = 1;
    public static final byte GD_KET_QUA = 2;

    public static final long TOI_DA_MOI_CON = 500;
    public static final long TOI_DA_MOT_VAN = 1500;

    /** Trả 4,5× tiền cược. Nhân 10 để tính bằng số nguyên. */
    private static final long NHAN_X10 = 45;

    /** Cùng thứ tự với <code>TEN_VIT</code> và bộ sprite <code>dv_*</code> bên client. */
    public static final String[] TEN_NGUA = {
        "Vịt Goku", "Vịt Naruto", "Vịt Sasuke", "Vịt Vegeta", "Vịt Bulma"
    };

    private static final class Cuoc {
        final Player nguoi;
        final long id;
        final String ten;
        /** Địa chỉ mạng lúc đặt, để giới hạn số nick mỗi địa chỉ. */
        final String ip;
        final long[] theoCon = new long[SO_NGUA];

        Cuoc(Player nguoi) {
            this.nguoi = nguoi;
            this.id = nguoi.id;
            this.ten = nguoi.name;
            this.ip = (nguoi.getSession() != null) ? nguoi.getSession().ipAddress : null;
        }

        long tong() {
            long t = 0;
            for (int i = 0; i < SO_NGUA; i++) {
                t += theoCon[i];
            }
            return t;
        }
    }

    /** Thứ tự về đích: phần tử 0 là con về nhất. */
    private final int[] thuTuVe = new int[SO_NGUA];

    private final long[] tongTheoCon = new long[SO_NGUA];
    private final Map<Long, Cuoc> cuocs = new ConcurrentHashMap<>();

    private DuaNguaManager() {
        try {
            phien = MiniGameDAO.phienLonNhat(MA_TRO);
        } catch (Exception e) {
            Logger.logException(DuaNguaManager.class, e);
        }
        for (int i = 0; i < SO_NGUA; i++) {
            thuTuVe[i] = i;
        }
        moVongMoi();
    }

    // ------------------------------------------------------------------
    //  Vòng chạy
    // ------------------------------------------------------------------

    @Override
    protected void guiNhip() {
        MiniGameService.gI().duaNguaGuiNhip();
    }

    @Override
    protected void hetGio() {
        switch (giaiDoan) {
            case GD_DAT_CUOC:
                bocKetQua();
                giaiDoan = GD_DUA;
                ketThucLuc = System.currentTimeMillis() + GIAY_DUA * 1000L;
                MiniGameService.gI().duaNguaGuiTrangThaiCho();
                break;
            case GD_DUA:
                chotVan();
                break;
            default:
                moVongMoi();
                MiniGameService.gI().duaNguaGuiTrangThaiCho();
                break;
        }
    }

    private synchronized void moVongMoi() {
        phien++;
        giaiDoan = GD_DAT_CUOC;
        ketThucLuc = System.currentTimeMillis() + GIAY_DAT_CUOC * 1000L;
        Arrays.fill(tongTheoCon, 0L);
        cuocs.clear();
    }

    /**
     * Xáo thứ tự về đích.
     *
     * <p>Xáo Fisher–Yates: mỗi hoán vị có đúng một cơ hội như nhau. Cách hay gặp
     * hơn — bốc con thắng rồi xếp năm con còn lại theo thứ tự cũ — cho ra thứ tự
     * về đích đoán được ngay từ vị trí thứ hai, và người chơi để ý một lúc là
     * nhận ra.</p>
     */
    private synchronized void bocKetQua() {
        for (int i = 0; i < SO_NGUA; i++) {
            thuTuVe[i] = i;
        }
        for (int i = SO_NGUA - 1; i > 0; i--) {
            int j = ngauNhien.nextInt(i + 1);
            int tmp = thuTuVe[i];
            thuTuVe[i] = thuTuVe[j];
            thuTuVe[j] = tmp;
        }
    }

    /** Con về nhất. */
    public int conThang() {
        return thuTuVe[0];
    }

    private static long traVe(long soThoi) {
        return (soThoi * NHAN_X10 + 5) / 10;
    }

    private synchronized void chotVan() {
        giaiDoan = GD_KET_QUA;
        ketThucLuc = System.currentTimeMillis() + GIAY_KET_QUA * 1000L;

        byte thang = (byte) conThang();
        ghiLichSuNhanh(thang);

        long tongCuoc = 0;
        long tongTra = 0;
        for (Cuoc c : cuocs.values()) {
            tongCuoc += c.tong();
            try {
                tongTra += traThuong(c, thang);
            } catch (Exception e) {
                Logger.logException(DuaNguaManager.class, e);
            }
        }

        MiniGameService.gI().duaNguaGuiKetQua();
        ghiLichSu(thang, tongCuoc, tongTra);
        cuocs.clear();
    }

    private long traThuong(Cuoc c, int thang) {
        Player pl = c.nguoi;
        long dat = c.theoCon[thang];
        long ve = dat > 0 ? traVe(dat) : 0;
        if (pl == null || pl.getSession() == null || !pl.getSession().isConnected()) {
            Logger.error("[DuaNgua] Khong tra duoc " + ve + " thoi cho "
                    + c.ten + " (da roi game).\n");
            return ve;
        }
        if (ve <= 0) {
            Service.gI().sendThongBao(pl, "Đua Vịt: " + TEN_NGUA[thang]
                    + " về nhất. Bạn thua " + c.tong() + " thỏi vàng.");
            return 0;
        }
        if (KhoVang.themKhoa(pl, ve)) {
            Service.gI().sendThongBao(pl, "Đua Vịt: " + TEN_NGUA[thang]
                    + " về nhất — bạn nhận " + ve + " thỏi vàng khoá!");
        } else {
            Logger.error("[DuaNgua] Hanh trang day, khong tra duoc " + ve
                    + " thoi cho " + c.ten + ".\n");
            Service.gI().sendThongBao(pl,
                    "Đua Vịt: bạn thắng nhưng hành trang đầy, không nhận được thưởng!");
        }
        return ve;
    }

    private void ghiLichSu(byte thang, long tongCuoc, long tongTra) {
        final long soPhien = phien;
        final int nhi = thuTuVe[1];
        final int ba = thuTuVe[2];
        final List<Cuoc> banSao = new ArrayList<>(cuocs.values());
        Thread t = new Thread(() -> {
            try {
                MiniGameDAO.ghiPhien(MA_TRO, soPhien, thang, nhi, ba, thang,
                        tongCuoc, tongTra, banSao.size());
                for (Cuoc cc : banSao) {
                    for (int con = 0; con < SO_NGUA; con++) {
                        long dat = cc.theoCon[con];
                        if (dat <= 0) {
                            continue;
                        }
                        boolean th = (con == thang);
                        MiniGameDAO.ghiCuoc(MA_TRO, soPhien, cc.id, cc.ten, con,
                                dat, thang, th, th ? traVe(dat) : 0);
                    }
                }
            } catch (Exception e) {
                Logger.logException(DuaNguaManager.class, e);
            }
        }, "Dua Ngua ghi lich su");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------
    //  Đặt cược
    // ------------------------------------------------------------------

    public synchronized void datCuoc(Player pl, int con, long soThoi) {
        if (pl == null) {
            return;
        }
        if (giaiDoan != GD_DAT_CUOC) {
            Service.gI().sendThongBao(pl, "Đã hết giờ đặt cược, chờ ván sau!");
            return;
        }
        if (con < 0 || con >= SO_NGUA || soThoi <= 0) {
            return;
        }
        Cuoc c = cuocs.get(pl.id);
        if (c != null) {
            // MOT con moi van. Rai tien len ca bon, nam con thi ty le "thang"
            // len 80, 100 phan tram: lai moi van mot chut, thua mot van la mat
            // ca bon cua — ve lau dai van lo dung phan loi the nha cai, nhung
            // ca tro choi thanh mot chuoi thang vat vanh khong con gi hoi hop.
            // Cong them vao dung con da chon thi van duoc.
            for (int k = 0; k < SO_NGUA; k++) {
                if (k != con && c.theoCon[k] > 0) {
                    Service.gI().sendThongBao(pl, "Mỗi ván chỉ được đặt một con vịt! Bạn đã chọn "
                            + TEN_NGUA[k] + ".");
                    return;
                }
            }
        }
        if (c == null && pl.getSession() != null && pl.getSession().ipAddress != null) {
            // Nick moi vao van: chan mot dia chi mang dua ca tram nick vao ban.
            String ip = pl.getSession().ipAddress;
            int cungIp = 0;
            for (Cuoc k : cuocs.values()) {
                if (ip.equals(k.ip)) {
                    cungIp++;
                }
            }
            if (cungIp >= nro.gameplay.taixiu.TaiXiuManager.TOI_DA_NICK_MOT_IP) {
                Service.gI().sendThongBao(pl, "Mỗi địa chỉ mạng chỉ được "
                        + nro.gameplay.taixiu.TaiXiuManager.TOI_DA_NICK_MOT_IP
                        + " nhân vật đặt cược mỗi ván!");
                return;
            }
        }
        long oCon = (c == null) ? 0 : c.theoCon[con];
        long caVan = (c == null) ? 0 : c.tong();
        if (oCon + soThoi > TOI_DA_MOI_CON) {
            Service.gI().sendThongBao(pl, "Mỗi con chỉ được đặt tối đa "
                    + TOI_DA_MOI_CON + " thỏi!");
            return;
        }
        if (caVan + soThoi > TOI_DA_MOT_VAN) {
            Service.gI().sendThongBao(pl, "Mỗi ván chỉ được đặt tối đa "
                    + TOI_DA_MOT_VAN + " thỏi!");
            return;
        }
        if (KhoVang.demTatCa(pl) < soThoi) {
            Service.gI().sendThongBao(pl, "Bạn không đủ thỏi vàng!");
            return;
        }
        if (!KhoVang.conChoNhanThuong(pl)) {
            Service.gI().sendThongBao(pl, "Hành trang đầy, không thể nhận thưởng!");
            return;
        }
        if (!KhoVang.tru(pl, soThoi)) {
            Service.gI().sendThongBao(pl, "Trừ thỏi vàng thất bại!");
            return;
        }
        if (c == null) {
            c = new Cuoc(pl);
            cuocs.put(pl.id, c);
        }
        c.theoCon[con] += soThoi;
        tongTheoCon[con] += soThoi;
        MiniGameService.gI().duaNguaGuiTrangThai(pl);
    }

    @Override
    public synchronized void nguoiChoiRoiGame(Player pl) {
        if (pl == null) {
            return;
        }
        nguoiXem.remove(pl.id);
        Cuoc c = cuocs.remove(pl.id);
        if (c == null) {
            return;
        }
        long hoan = c.tong();
        for (int con = 0; con < SO_NGUA; con++) {
            tongTheoCon[con] -= c.theoCon[con];
        }
        if (giaiDoan != GD_DAT_CUOC) {
            // Dang boi: KHONG hoan. Thu tu ve dich da gui xuong client tu dau pha
            // boi, nen hoan o day thi ai dat thua chi can thoat game la lay lai
            // tien. Chot luon theo ket qua da dinh: trung thi tra ngay bay gio,
            // truot thi mat nhu moi nguoi.
            long dat = c.theoCon[conThang()];
            if (dat > 0) {
                KhoVang.themKhoa(pl, traVe(dat));
            }
            return;
        }
        if (hoan > 0) {
            KhoVang.themKhoa(pl, hoan);
            Logger.warning("[DuaNgua] " + c.ten + " roi game giua van, hoan "
                    + hoan + " thoi vang khoa.\n");
        }
    }

    // ------------------------------------------------------------------
    //  Cho MiniGameService đọc
    // ------------------------------------------------------------------

    public int[] getThuTuVe() {
        return thuTuVe;
    }

    public long[] getTongTheoCon() {
        return tongTheoCon;
    }

    public long[] cuocCuaToi(Player pl) {
        Cuoc c = (pl == null) ? null : cuocs.get(pl.id);
        return c == null ? new long[SO_NGUA] : c.theoCon;
    }
}
