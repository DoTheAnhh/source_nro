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
 * Bàn <b>Xóc Đĩa</b> — bốn đồng xu, mỗi đồng một mặt đỏ một mặt trắng.
 *
 * <h2>Bốn cửa</h2>
 *
 * <table border="1">
 *   <tr><th>Cửa</th><th>Trúng khi</th><th>Xác suất</th><th>Trả về</th></tr>
 *   <tr><td>Chẵn</td><td>số đồng đỏ là 0, 2 hoặc 4</td><td>8/16</td><td>1,9×</td></tr>
 *   <tr><td>Lẻ</td><td>số đồng đỏ là 1 hoặc 3</td><td>8/16</td><td>1,9×</td></tr>
 *   <tr><td>Bốn đỏ</td><td>cả bốn đồng đỏ</td><td>1/16</td><td>14×</td></tr>
 *   <tr><td>Bốn trắng</td><td>cả bốn đồng trắng</td><td>1/16</td><td>14×</td></tr>
 * </table>
 *
 * <p>Chẵn/Lẻ trả 1,9× thay vì 2× — phần chênh ấy là lợi thế nhà cái, khoảng 5
 * phần trăm. Hai cửa hiếm trả 14× thay vì 16×, cùng một cách nghĩ.</p>
 *
 * <p>"Bốn đỏ" nằm trong "Chẵn", nên đặt cả hai thì trúng cả hai. Đó là chuyện
 * bình thường của trò này, không phải kẽ hở: tổng kỳ vọng vẫn âm cho người
 * chơi.</p>
 *
 * <h2>Vì sao dùng long thay cho boolean[]</h2>
 *
 * <p>Bốn đồng xu gói vào <b>một số nguyên 0..15</b>, mỗi bit một đồng. Nhờ thế
 * kết quả ghi vừa một cột trong bảng lịch sử, gửi vừa một byte xuống client, và
 * đếm số đồng đỏ chỉ là đếm bit.</p>
 */
public final class XocDiaManager extends VongChoi {

    private static XocDiaManager instance;

    public static XocDiaManager gI() {
        if (instance == null) {
            instance = new XocDiaManager();
        }
        return instance;
    }

    public static final int MA_TRO = MiniGameService.TRO_XOC_DIA;

    public static final int SO_CUA = 4;
    public static final int CUA_CHAN = 0;
    public static final int CUA_LE = 1;
    public static final int CUA_BON_DO = 2;
    public static final int CUA_BON_TRANG = 3;

    public static final int GIAY_DAT_CUOC = 20;
    public static final int GIAY_XOC = 5;
    public static final int GIAY_KET_QUA = 4;

    public static final byte GD_DAT_CUOC = 0;
    public static final byte GD_XOC = 1;
    public static final byte GD_KET_QUA = 2;

    public static final long TOI_DA_MOI_CUA = 500;
    public static final long TOI_DA_MOT_VAN = 1500;

    /** Trả về cho Chẵn/Lẻ, nhân 10 để tính bằng số nguyên. */
    private static final long NHAN_CHAN_LE_X10 = 19;

    /** Trả về cho hai cửa hiếm. */
    private static final long NHAN_HIEM = 14;

    private static final String[] TEN_CUA = {
        "Chẵn", "Lẻ", "Bốn đỏ", "Bốn trắng"
    };

    private static final class Cuoc {
        final Player nguoi;
        final long id;
        final String ten;
        final long[] theoCua = new long[SO_CUA];

        Cuoc(Player nguoi) {
            this.nguoi = nguoi;
            this.id = nguoi.id;
            this.ten = nguoi.name;
        }

        long tong() {
            long t = 0;
            for (int i = 0; i < SO_CUA; i++) {
                t += theoCua[i];
            }
            return t;
        }
    }

    /** Bốn đồng xu gói trong bốn bit thấp: bit bật là mặt đỏ. */
    private volatile int dongXu;

    private final long[] tongTheoCua = new long[SO_CUA];
    private final Map<Long, Cuoc> cuocs = new ConcurrentHashMap<>();

    private XocDiaManager() {
        try {
            phien = MiniGameDAO.phienLonNhat(MA_TRO);
        } catch (Exception e) {
            Logger.logException(XocDiaManager.class, e);
        }
        moVongMoi();
    }

    // ------------------------------------------------------------------
    //  Vòng chạy
    // ------------------------------------------------------------------

    @Override
    protected void guiNhip() {
        MiniGameService.gI().xocDiaGuiNhip();
    }

    @Override
    protected void hetGio() {
        switch (giaiDoan) {
            case GD_DAT_CUOC:
                xoc();
                giaiDoan = GD_XOC;
                ketThucLuc = System.currentTimeMillis() + GIAY_XOC * 1000L;
                MiniGameService.gI().xocDiaGuiTrangThaiCho();
                break;
            case GD_XOC:
                chotVan();
                break;
            default:
                moVongMoi();
                MiniGameService.gI().xocDiaGuiTrangThaiCho();
                break;
        }
    }

    private synchronized void moVongMoi() {
        phien++;
        giaiDoan = GD_DAT_CUOC;
        ketThucLuc = System.currentTimeMillis() + GIAY_DAT_CUOC * 1000L;
        Arrays.fill(tongTheoCua, 0L);
        cuocs.clear();
    }

    private synchronized void xoc() {
        dongXu = ngauNhien.nextInt(16);
    }

    /** Số đồng đỏ, tức số bit bật. */
    public int soDo() {
        return Integer.bitCount(dongXu & 0x0F);
    }

    /** Cửa này có trúng với kết quả hiện tại không. */
    private boolean trung(int cua) {
        int do_ = soDo();
        switch (cua) {
            case CUA_CHAN:
                return (do_ & 1) == 0;
            case CUA_LE:
                return (do_ & 1) == 1;
            case CUA_BON_DO:
                return do_ == 4;
            case CUA_BON_TRANG:
                return do_ == 0;
            default:
                return false;
        }
    }

    /**
     * Số thỏi trả về cho một cửa trúng.
     *
     * <p>Tính bằng số nguyên chứ không nhân với {@code 1.9} rồi làm tròn: 1,9
     * không biểu diễn chính xác được bằng số thực nhị phân, nên phép nhân ra
     * thiếu một chút và làm tròn xuống sai một thỏi. Nhân 19 rồi chia 10 thì
     * không có sai số nào cả, và {@code + 5} là làm tròn nửa lên.</p>
     */
    private static long traVe(int cua, long soThoi) {
        if (cua == CUA_CHAN || cua == CUA_LE) {
            return (soThoi * NHAN_CHAN_LE_X10 + 5) / 10;
        }
        return soThoi * NHAN_HIEM;
    }

    private synchronized void chotVan() {
        giaiDoan = GD_KET_QUA;
        ketThucLuc = System.currentTimeMillis() + GIAY_KET_QUA * 1000L;

        byte ketQua = (byte) ((soDo() & 1) == 0 ? CUA_CHAN : CUA_LE);
        ghiLichSuNhanh(ketQua);

        long tongCuoc = 0;
        long tongTra = 0;
        for (Cuoc c : cuocs.values()) {
            tongCuoc += c.tong();
            try {
                tongTra += traThuong(c);
            } catch (Exception e) {
                Logger.logException(XocDiaManager.class, e);
            }
        }

        MiniGameService.gI().xocDiaGuiKetQua(dongXu);
        ghiLichSu(ketQua, tongCuoc, tongTra);
        cuocs.clear();
    }

    private long traThuong(Cuoc c) {
        Player pl = c.nguoi;
        long tong = 0;
        StringBuilder loi = new StringBuilder();
        for (int cua = 0; cua < SO_CUA; cua++) {
            long dat = c.theoCua[cua];
            if (dat > 0 && trung(cua)) {
                tong += traVe(cua, dat);
                loi.append(TEN_CUA[cua]).append(' ');
            }
        }
        if (pl == null || pl.getSession() == null || !pl.getSession().isConnected()) {
            Logger.error("[XocDia] Khong tra duoc " + tong + " thoi cho "
                    + c.ten + " (da roi game).\n");
            return tong;
        }
        if (tong <= 0) {
            Service.gI().sendThongBao(pl, "Xóc Đĩa: bạn thua "
                    + c.tong() + " thỏi vàng.");
            return 0;
        }
        if (KhoVang.themKhoa(pl, tong)) {
            Service.gI().sendThongBao(pl, "Xóc Đĩa: trúng "
                    + loi.toString().trim() + " — nhận " + tong + " thỏi vàng khoá.");
        } else {
            Logger.error("[XocDia] Hanh trang day, khong tra duoc " + tong
                    + " thoi cho " + c.ten + ".\n");
            Service.gI().sendThongBao(pl,
                    "Xóc Đĩa: bạn thắng nhưng hành trang đầy, không nhận được thưởng!");
        }
        return tong;
    }

    private void ghiLichSu(byte ketQua, long tongCuoc, long tongTra) {
        final long soPhien = phien;
        final int xu = dongXu;
        final List<Cuoc> banSao = new ArrayList<>(cuocs.values());
        Thread t = new Thread(() -> {
            try {
                MiniGameDAO.ghiPhien(MA_TRO, soPhien, xu, Integer.bitCount(xu & 0x0F),
                        0, ketQua, tongCuoc, tongTra, banSao.size());
                for (Cuoc cc : banSao) {
                    for (int cua = 0; cua < SO_CUA; cua++) {
                        long dat = cc.theoCua[cua];
                        if (dat <= 0) {
                            continue;
                        }
                        int do_ = Integer.bitCount(xu & 0x0F);
                        boolean th;
                        switch (cua) {
                            case CUA_CHAN: th = (do_ & 1) == 0; break;
                            case CUA_LE: th = (do_ & 1) == 1; break;
                            case CUA_BON_DO: th = do_ == 4; break;
                            default: th = do_ == 0; break;
                        }
                        MiniGameDAO.ghiCuoc(MA_TRO, soPhien, cc.id, cc.ten, cua,
                                dat, ketQua, th, th ? traVe(cua, dat) : 0);
                    }
                }
            } catch (Exception e) {
                Logger.logException(XocDiaManager.class, e);
            }
        }, "Xoc Dia ghi lich su");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------
    //  Đặt cược
    // ------------------------------------------------------------------

    public synchronized void datCuoc(Player pl, int cua, long soThoi) {
        if (pl == null) {
            return;
        }
        if (giaiDoan != GD_DAT_CUOC) {
            Service.gI().sendThongBao(pl, "Đã hết giờ đặt cược, chờ ván sau!");
            return;
        }
        if (cua < 0 || cua >= SO_CUA || soThoi <= 0) {
            return;
        }
        Cuoc c = cuocs.get(pl.id);
        long oCua = (c == null) ? 0 : c.theoCua[cua];
        long caVan = (c == null) ? 0 : c.tong();
        if (oCua + soThoi > TOI_DA_MOI_CUA) {
            Service.gI().sendThongBao(pl, "Mỗi cửa chỉ được đặt tối đa "
                    + TOI_DA_MOI_CUA + " thỏi!");
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
        c.theoCua[cua] += soThoi;
        tongTheoCua[cua] += soThoi;
        MiniGameService.gI().xocDiaGuiTrangThai(pl);
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
        for (int cua = 0; cua < SO_CUA; cua++) {
            tongTheoCua[cua] -= c.theoCua[cua];
        }
        if (hoan > 0) {
            KhoVang.themKhoa(pl, hoan);
            Logger.warning("[XocDia] " + c.ten + " roi game giua van, hoan "
                    + hoan + " thoi vang khoa.\n");
        }
    }

    // ------------------------------------------------------------------
    //  Cho MiniGameService đọc
    // ------------------------------------------------------------------

    public int getDongXu() {
        return dongXu;
    }

    public long[] getTongTheoCua() {
        return tongTheoCua;
    }

    public long[] cuocCuaToi(Player pl) {
        Cuoc c = (pl == null) ? null : cuocs.get(pl.id);
        return c == null ? new long[SO_CUA] : c.theoCua;
    }
}
