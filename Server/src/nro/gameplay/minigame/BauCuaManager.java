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
 * Bàn <b>Bầu Cua Tôm Cá</b> dùng chung cho cả máy chủ.
 *
 * <h2>Luật</h2>
 *
 * <p>Ba con xúc xắc, mỗi mặt là một con vật: Bầu, Cua, Tôm, Cá, Gà, Nai. Người
 * chơi rải tiền lên <b>nhiều cửa cùng lúc</b> — đây là chỗ khác căn bản với Tài
 * Xỉu, nơi mỗi ván chỉ được một cửa.</p>
 *
 * <p>Trả thưởng theo <b>số con trúng</b>, đúng luật ngoài đời:</p>
 *
 * <table border="1">
 *   <tr><th>Số con ra đúng cửa</th><th>Nhận về</th></tr>
 *   <tr><td>1</td><td>2× tiền cửa đó (gốc + 1 lãi)</td></tr>
 *   <tr><td>2</td><td>3×</td></tr>
 *   <tr><td>3</td><td>4×</td></tr>
 * </table>
 *
 * <p>Cửa không có con nào thì mất tiền cửa đó. Ba cửa khác nhau đặt cùng lúc thì
 * tính riêng từng cửa, không bù trừ.</p>
 *
 * <h2>Vì sao trần cược thấp</h2>
 *
 * <p>Luật này lợi cho nhà cái chừng 8 phần trăm — không nhiều. Người đặt lớn và
 * đặt lâu thì vẫn thua dần, nhưng một ván may mắn có thể lấy đi lượng thỏi vàng
 * lớn khỏi kho chung. Trần {@link #TOI_DA_MOI_CUA} và {@link #TOI_DA_MOT_VAN}
 * giữ cho mỗi ván chỉ là một khoản nhỏ, đúng ý "thưởng ít thôi".</p>
 *
 * <h2>Nhịp ván ngắn hơn Tài Xỉu</h2>
 *
 * <p>25 giây đặt, 6 giây lắc, 4 giây xem kết quả. Tài Xỉu là 30/11/3. Để lệch
 * nhau như vậy thì hai bàn không chốt cùng lúc, và người mở cả hai tab không
 * phải chờ hai đồng hồ trùng nhau.</p>
 */
public final class BauCuaManager extends VongChoi {

    private static BauCuaManager instance;

    public static BauCuaManager gI() {
        if (instance == null) {
            instance = new BauCuaManager();
        }
        return instance;
    }

    /** Mã trò trong bảng lệnh và trong cơ sở dữ liệu. */
    public static final int MA_TRO = MiniGameService.TRO_BAU_CUA;

    /** Số cửa: Bầu, Cua, Tôm, Cá, Gà, Nai. */
    public static final int SO_CUA = 6;

    public static final int GIAY_DAT_CUOC = 25;
    public static final int GIAY_LAC = 6;
    public static final int GIAY_KET_QUA = 4;

    public static final byte GD_DAT_CUOC = 0;
    public static final byte GD_LAC = 1;
    public static final byte GD_KET_QUA = 2;

    /** Trần một cửa trong một ván. */
    public static final long TOI_DA_MOI_CUA = 500;

    /** Trần tổng cả sáu cửa trong một ván. */
    public static final long TOI_DA_MOT_VAN = 2000;

    /** Một người đặt gì trong ván này. */
    private static final class Cuoc {
        final Player nguoi;
        /** Chép ra để luồng ghi lịch sử không phải chạm vào {@code Player}. */
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

    private final int[] xucXac = new int[3];
    private final long[] tongTheoCua = new long[SO_CUA];
    private final Map<Long, Cuoc> cuocs = new ConcurrentHashMap<>();

    private BauCuaManager() {
        try {
            phien = MiniGameDAO.phienLonNhat(MA_TRO);
        } catch (Exception e) {
            Logger.logException(BauCuaManager.class, e);
        }
        moVongMoi();
    }

    // ------------------------------------------------------------------
    //  Vòng chạy
    // ------------------------------------------------------------------

    @Override
    protected void guiNhip() {
        MiniGameService.gI().bauCuaGuiNhip();
    }

    @Override
    protected void hetGio() {
        switch (giaiDoan) {
            case GD_DAT_CUOC:
                tungXucXac();
                giaiDoan = GD_LAC;
                ketThucLuc = System.currentTimeMillis() + GIAY_LAC * 1000L;
                MiniGameService.gI().bauCuaGuiTrangThaiCho();
                break;
            case GD_LAC:
                chotVan();
                break;
            default:
                moVongMoi();
                MiniGameService.gI().bauCuaGuiTrangThaiCho();
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

    /**
     * Tung ba con.
     *
     * <p>Tung ngay ở <b>đầu</b> pha lắc rồi gửi xuống client cùng lúc, giống Tài
     * Xỉu. Nghe thì như để lộ sớm, nhưng cửa đã đóng trước đó rồi — biết trước
     * sáu giây cũng không đặt thêm được đồng nào. Đổi lại thì hoạt hình lắc mới
     * thật thà: mặt hé ra đúng là mặt cuối cùng.</p>
     */
    private synchronized void tungXucXac() {
        for (int i = 0; i < 3; i++) {
            xucXac[i] = ngauNhien.nextInt(SO_CUA);
        }
    }

    /** Số con ra đúng cửa {@code cua}. */
    private int demTrung(int cua) {
        int d = 0;
        for (int i = 0; i < 3; i++) {
            if (xucXac[i] == cua) {
                d++;
            }
        }
        return d;
    }

    /**
     * Số thỏi trả về cho một cửa.
     *
     * <p>Không trúng thì 0 — tiền cược đã bị trừ lúc đặt, nên 0 nghĩa là mất
     * hẳn. Trúng {@code n} con thì nhận lại {@code (n + 1)} lần, trong đó một
     * lần là gốc.</p>
     */
    private long traVeMotCua(long soThoi, int soConTrung) {
        return soConTrung <= 0 ? 0 : soThoi * (soConTrung + 1);
    }

    private synchronized void chotVan() {
        giaiDoan = GD_KET_QUA;
        ketThucLuc = System.currentTimeMillis() + GIAY_KET_QUA * 1000L;

        // Con ra nhieu nhat lam "ket qua" cho dai lich su. Hoa thi lay con nho
        // hon — chi de ve mot cham, khong dinh gi toi tien.
        int[] dem = new int[SO_CUA];
        for (int i = 0; i < 3; i++) {
            dem[xucXac[i]]++;
        }
        byte noiBat = 0;
        for (byte c = 1; c < SO_CUA; c++) {
            if (dem[c] > dem[noiBat]) {
                noiBat = c;
            }
        }
        ghiLichSuNhanh(noiBat);

        long tongCuoc = 0;
        long tongTra = 0;
        for (Cuoc c : cuocs.values()) {
            tongCuoc += c.tong();
            try {
                tongTra += traThuong(c);
            } catch (Exception e) {
                Logger.logException(BauCuaManager.class, e);
            }
        }

        MiniGameService.gI().bauCuaGuiKetQua(xucXac);
        ghiLichSu(noiBat, tongCuoc, tongTra);
        cuocs.clear();
    }

    /** Trả thưởng cho một người, trả về tổng số thỏi đã trả. */
    private long traThuong(Cuoc c) {
        Player pl = c.nguoi;
        long tongTra = 0;
        StringBuilder loi = new StringBuilder();
        for (int cua = 0; cua < SO_CUA; cua++) {
            long dat = c.theoCua[cua];
            if (dat <= 0) {
                continue;
            }
            int trung = demTrung(cua);
            long ve = traVeMotCua(dat, trung);
            if (ve > 0) {
                tongTra += ve;
                loi.append(TEN_CUA[cua]).append(" x").append(trung).append("  ");
            }
        }
        if (pl == null || pl.getSession() == null || !pl.getSession().isConnected()) {
            // Khong the xay ra trong luc chay binh thuong vi nguoiChoiRoiGame()
            // da hoan tien truoc do; ghi lai de con biet neu co ke ho.
            Logger.error("[BauCua] Khong tra duoc " + tongTra + " thoi cho "
                    + c.ten + " (da roi game).\n");
            return tongTra;
        }
        if (tongTra <= 0) {
            Service.gI().sendThongBao(pl, "Bầu Cua: bạn thua "
                    + c.tong() + " thỏi vàng.");
            return 0;
        }
        if (KhoVang.themKhoa(pl, tongTra)) {
            Service.gI().sendThongBao(pl, "Bầu Cua: trúng " + loi.toString().trim()
                    + " — nhận " + tongTra + " thỏi vàng khoá.");
        } else {
            Logger.error("[BauCua] Hanh trang day, khong tra duoc " + tongTra
                    + " thoi cho " + c.ten + ".\n");
            Service.gI().sendThongBao(pl,
                    "Bầu Cua: bạn thắng nhưng hành trang đầy, không nhận được thưởng!");
        }
        return tongTra;
    }

    /** Tên sáu cửa, chỉ dùng cho câu thông báo. */
    private static final String[] TEN_CUA = {
        "Bầu", "Cua", "Tôm", "Cá", "Gà", "Nai"
    };

    /**
     * Ghi ván vừa chốt xuống cơ sở dữ liệu, trên luồng riêng.
     *
     * <p>Chép dữ liệu ra trước khi giao cho luồng kia: {@code cuocs} bị xoá ngay
     * sau lời gọi này, luồng ghi mà đọc thẳng vào đó thì có khi thấy bảng
     * rỗng.</p>
     */
    private void ghiLichSu(byte noiBat, long tongCuoc, long tongTra) {
        final long soPhien = phien;
        final int a = xucXac[0];
        final int b = xucXac[1];
        final int c = xucXac[2];
        final List<Cuoc> banSao = new ArrayList<>(cuocs.values());

        Thread t = new Thread(() -> {
            try {
                MiniGameDAO.ghiPhien(MA_TRO, soPhien, a, b, c, noiBat,
                        tongCuoc, tongTra, banSao.size());
                for (Cuoc cc : banSao) {
                    for (int cua = 0; cua < SO_CUA; cua++) {
                        long dat = cc.theoCua[cua];
                        if (dat <= 0) {
                            continue;
                        }
                        int trung = 0;
                        if (a == cua) {
                            trung++;
                        }
                        if (b == cua) {
                            trung++;
                        }
                        if (c == cua) {
                            trung++;
                        }
                        long ve = trung <= 0 ? 0 : dat * (trung + 1);
                        MiniGameDAO.ghiCuoc(MA_TRO, soPhien, cc.id, cc.ten, cua,
                                dat, noiBat, trung > 0, ve);
                    }
                }
            } catch (Exception e) {
                Logger.logException(BauCuaManager.class, e);
            }
        }, "Bau Cua ghi lich su");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------
    //  Đặt cược
    // ------------------------------------------------------------------

    /**
     * Nhận một lượt đặt.
     *
     * <p>Cộng dồn được, và <b>rải nhiều cửa được</b> — đó là cái hồn của trò
     * này. Nhưng có trần từng cửa và trần cả ván, kiểm ở đây chứ không ở client:
     * client sửa được, máy chủ thì không.</p>
     */
    // synchronized: dat cuoc chay tren luong MANG (moi nguoi mot luong), con
    // chot van chay tren luong nhip. Hai ben cung sua tongTheoCua va bang cuoc.
    public synchronized void datCuoc(Player pl, int cua, long soThoi) {
        if (pl == null) {
            return;
        }
        if (giaiDoan != GD_DAT_CUOC) {
            Service.gI().sendThongBao(pl, "Đã hết giờ đặt cược, chờ ván sau!");
            return;
        }
        if (cua < 0 || cua >= SO_CUA) {
            return;
        }
        if (soThoi <= 0) {
            return;
        }

        Cuoc c = cuocs.get(pl.id);
        long dangCoOCua = (c == null) ? 0 : c.theoCua[cua];
        long dangCoCaVan = (c == null) ? 0 : c.tong();

        if (dangCoOCua + soThoi > TOI_DA_MOI_CUA) {
            Service.gI().sendThongBao(pl, "Mỗi cửa chỉ được đặt tối đa "
                    + TOI_DA_MOI_CUA + " thỏi!");
            return;
        }
        if (dangCoCaVan + soThoi > TOI_DA_MOT_VAN) {
            Service.gI().sendThongBao(pl, "Mỗi ván chỉ được đặt tối đa "
                    + TOI_DA_MOT_VAN + " thỏi!");
            return;
        }
        if (KhoVang.demTatCa(pl) < soThoi) {
            Service.gI().sendThongBao(pl, "Bạn không đủ thỏi vàng!");
            return;
        }
        // Chan truoc khi tru tien: thang ma khong co o de nhan thi tien thuong
        // bien mat, va nguoi choi mat ca tien cuoc vi mot loi khong phai cua ho.
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
        MiniGameService.gI().bauCuaGuiTrangThai(pl);
    }

    /**
     * Hoàn tiền khi người chơi rời game giữa ván.
     *
     * <p>Không hoàn thì thỏi vàng đã trừ biến mất hẳn — máy chủ không có đường
     * nào trả vào hành trang của một người đang offline.</p>
     *
     * <p>Hoàn bằng bản <b>khoá</b>: người chơi có thể đã đặt bằng vàng thường,
     * nhưng hoàn thường lại cho họ một đường rửa vàng khoá thành vàng thường —
     * đặt cược bằng khoá rồi thoát game.</p>
     */
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
            Logger.warning("[BauCua] " + c.ten + " roi game giua van, hoan "
                    + hoan + " thoi vang khoa.\n");
        }
    }

    // ------------------------------------------------------------------
    //  Cho MiniGameService đọc
    // ------------------------------------------------------------------

    public int[] getXucXac() {
        return xucXac;
    }

    public long[] getTongTheoCua() {
        return tongTheoCua;
    }

    /** Số thỏi người đó đang đặt ở từng cửa trong ván này. */
    public long[] cuocCuaToi(Player pl) {
        Cuoc c = (pl == null) ? null : cuocs.get(pl.id);
        return c == null ? new long[SO_CUA] : c.theoCua;
    }
}
