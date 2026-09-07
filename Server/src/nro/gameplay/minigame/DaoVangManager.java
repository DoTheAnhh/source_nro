package nro.gameplay.minigame;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.repository.dao.MiniGameDAO;
import nro.service.MiniGameService;
import nro.service.Service;

/**
 * Trò <b>Đào Vàng</b> — lưới 5×5 giấu 3 quả mìn, mở tới đâu ăn tới đó.
 *
 * <h2>Luật</h2>
 *
 * <p>Đặt tiền rồi mở từng ô. Mỗi ô an toàn nâng hệ số nhân lên; <b>rút bất cứ
 * lúc nào</b> để nhận tiền cược nhân hệ số hiện tại. Chạm mìn là mất hết.</p>
 *
 * <p>Đây là trò duy nhất trong khu mà <b>người chơi quyết định lúc dừng</b> —
 * và đó là chỗ hay của nó. Ba trò kia chỉ có đặt rồi chờ.</p>
 *
 * <h2>Không có phiên</h2>
 *
 * <p>Chơi một mình, không phải chờ ai. Giờ thấp điểm ít người vẫn chơi được —
 * lý do chính để có một trò kiểu này bên cạnh mấy bàn theo phiên.</p>
 *
 * <h2>Sơ đồ mìn không bao giờ rời máy chủ</h2>
 *
 * <p>Sơ đồ dựng lúc nhận cược và giữ ở đây. Client chỉ biết ô vừa mở là an toàn
 * hay có mìn, <b>sau khi</b> đã mở. Gửi cả sơ đồ xuống rồi bảo client tự giấu là
 * tự dối mình: dữ liệu đã ở máy người chơi thì đọc được.</p>
 *
 * <h2>Hệ số nhân</h2>
 *
 * <p>Bảng {@link #HE_SO_X100} tính từ xác suất sống sót thật, rồi hạ xuống còn
 * <b>94 phần trăm</b> — phần hụt là lợi thế nhà cái. Trần {@link #TOI_DA_MO} ô
 * để hệ số không leo tới mức một ván ăn đứt cả kho.</p>
 */
public final class DaoVangManager {

    private static DaoVangManager instance;

    public static DaoVangManager gI() {
        if (instance == null) {
            instance = new DaoVangManager();
        }
        return instance;
    }

    public static final int MA_TRO = MiniGameService.TRO_DAO_VANG;

    /** Lưới 5×5. */
    public static final int SO_O = 25;

    /** Số mìn giấu trong lưới. */
    public static final int SO_MIN = 3;

    /** Mở tới ô thứ này thì buộc phải rút — hệ số không leo thêm nữa. */
    public static final int TOI_DA_MO = 12;

    /** Trần tiền cược một ván. */
    public static final long CUOC_TOI_DA = 300;

    /** Sàn tiền cược một ván. */
    public static final long CUOC_TOI_THIEU = 10;

    /**
     * Hệ số nhân sau khi mở được {@code k} ô an toàn, nhân 100.
     *
     * <p>Chỉ số 0 bỏ trống vì mở 0 ô thì chưa có gì để nhân. Con số tính từ xác
     * suất sống sót thật của lưới 25 ô 3 mìn rồi nhân 0,94.</p>
     */
    private static final long[] HE_SO_X100 = {
        100, 107, 115, 124, 134, 146, 159, 175, 193, 214, 240, 270, 307
    };

    /** Một ván đang chơi của một người. */
    private static final class Van {
        final long cuoc;
        final boolean[] coMin = new boolean[SO_O];
        final boolean[] daMo = new boolean[SO_O];
        int soDaMo;
        boolean ketThuc;

        Van(long cuoc) {
            this.cuoc = cuoc;
        }
    }

    /**
     * Ván đang chạy của từng người.
     *
     * <p>{@code ConcurrentHashMap} vì mỗi người chơi tới từ một luồng mạng
     * riêng. Trong một người thì các bước nối tiếp nhau nên không tranh nhau —
     * nhưng {@code datCuoc} và {@code moO} vẫn khoá trên chính đối tượng
     * {@code Van} để một client gửi hai gói cùng lúc không mở được hai ô bằng
     * một lần kiểm tra.</p>
     */
    private final Map<Long, Van> vans = new ConcurrentHashMap<>();

    private final SecureRandom ngauNhien = new SecureRandom();

    private DaoVangManager() {
    }

    /** Hệ số nhân hiện tại, nhân 100. */
    private static long heSo(int soDaMo) {
        if (soDaMo <= 0) {
            return 100;
        }
        int k = Math.min(soDaMo, HE_SO_X100.length - 1);
        return HE_SO_X100[k];
    }

    /** Số thỏi rút được nếu dừng ngay bây giờ. */
    private static long tienRut(long cuoc, int soDaMo) {
        return (cuoc * heSo(soDaMo) + 50) / 100;
    }

    // ------------------------------------------------------------------
    //  Các bước chơi
    // ------------------------------------------------------------------

    /** Bắt đầu một ván: trừ tiền, gieo mìn. */
    public void batDauVan(Player pl, long cuoc) {
        if (pl == null) {
            return;
        }
        Van dangCo = vans.get(pl.id);
        if (dangCo != null && !dangCo.ketThuc) {
            Service.gI().sendThongBao(pl, "Bạn đang có một ván chưa xong!");
            return;
        }
        if (cuoc < CUOC_TOI_THIEU || cuoc > CUOC_TOI_DA) {
            Service.gI().sendThongBao(pl, "Cược từ " + CUOC_TOI_THIEU
                    + " đến " + CUOC_TOI_DA + " thỏi vàng!");
            return;
        }
        if (KhoVang.demTatCa(pl) < cuoc) {
            Service.gI().sendThongBao(pl, "Bạn không đủ thỏi vàng!");
            return;
        }
        if (!KhoVang.conChoNhanThuong(pl)) {
            Service.gI().sendThongBao(pl, "Hành trang đầy, không thể nhận thưởng!");
            return;
        }
        if (!KhoVang.tru(pl, cuoc)) {
            Service.gI().sendThongBao(pl, "Trừ thỏi vàng thất bại!");
            return;
        }

        Van v = new Van(cuoc);
        // Gieo min: boc ngau nhien den khi du. Ba min tren hai muoi lam o nen so
        // lan boc lai khong dang ke.
        int da = 0;
        while (da < SO_MIN) {
            int o = ngauNhien.nextInt(SO_O);
            if (!v.coMin[o]) {
                v.coMin[o] = true;
                da++;
            }
        }
        vans.put(pl.id, v);
        MiniGameService.gI().daoVangGuiTrangThai(pl);
    }

    /** Mở một ô. */
    public void moO(Player pl, int o) {
        if (pl == null) {
            return;
        }
        Van v = vans.get(pl.id);
        if (v == null || v.ketThuc) {
            Service.gI().sendThongBao(pl, "Bạn chưa vào ván nào!");
            return;
        }
        if (o < 0 || o >= SO_O) {
            return;
        }
        boolean trungMin;
        boolean hetLuot;
        long thuong = 0;
        synchronized (v) {
            if (v.ketThuc || v.daMo[o]) {
                return;
            }
            v.daMo[o] = true;
            trungMin = v.coMin[o];
            if (trungMin) {
                v.ketThuc = true;
                hetLuot = true;
            } else {
                v.soDaMo++;
                hetLuot = v.soDaMo >= TOI_DA_MO;
                if (hetLuot) {
                    // Cham tran: tu rut cho nguoi choi, khong bat ho bam them
                    // mot cai nua cho mot lua chon khong con lua chon nao.
                    v.ketThuc = true;
                    thuong = tienRut(v.cuoc, v.soDaMo);
                }
            }
        }

        if (trungMin) {
            Service.gI().sendThongBao(pl, "Đào Vàng: trúng mìn! Mất "
                    + v.cuoc + " thỏi vàng.");
            ghiVan(pl, v, false, 0);
        } else if (hetLuot) {
            traThuong(pl, thuong, v.soDaMo);
            ghiVan(pl, v, true, thuong);
        }
        MiniGameService.gI().daoVangGuiTrangThai(pl);
    }

    /** Rút tiền và kết thúc ván. */
    public void rutTien(Player pl) {
        if (pl == null) {
            return;
        }
        Van v = vans.get(pl.id);
        if (v == null || v.ketThuc) {
            return;
        }
        long thuong;
        synchronized (v) {
            if (v.ketThuc) {
                return;
            }
            if (v.soDaMo <= 0) {
                Service.gI().sendThongBao(pl, "Mở ít nhất một ô rồi mới rút được!");
                return;
            }
            v.ketThuc = true;
            thuong = tienRut(v.cuoc, v.soDaMo);
        }
        traThuong(pl, thuong, v.soDaMo);
        ghiVan(pl, v, true, thuong);
        MiniGameService.gI().daoVangGuiTrangThai(pl);
    }

    private void traThuong(Player pl, long thuong, int soDaMo) {
        if (thuong <= 0) {
            return;
        }
        if (KhoVang.themKhoa(pl, thuong)) {
            Service.gI().sendThongBao(pl, "Đào Vàng: mở được " + soDaMo
                    + " ô, nhận " + thuong + " thỏi vàng khoá!");
        } else {
            Logger.error("[DaoVang] Hanh trang day, khong tra duoc " + thuong
                    + " thoi cho " + pl.name + ".\n");
            Service.gI().sendThongBao(pl,
                    "Đào Vàng: hành trang đầy, không nhận được thưởng!");
        }
    }

    private void ghiVan(Player pl, Van v, boolean thang, long thuong) {
        final long id = pl.id;
        final String ten = pl.name;
        final long cuoc = v.cuoc;
        final int soMo = v.soDaMo;
        Thread t = new Thread(() -> {
            try {
                MiniGameDAO.ghiCuoc(MA_TRO, 0, id, ten, soMo, cuoc, soMo,
                        thang, thuong);
            } catch (Exception e) {
                Logger.logException(DaoVangManager.class, e);
            }
        }, "Dao Vang ghi lich su");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Người chơi rời game giữa ván.
     *
     * <p>Hoàn <b>tiền cược gốc</b> chứ không phải tiền đang có nhân hệ số: hoàn
     * theo hệ số thì thoát game thành một nước đi — mở vài ô, thấy hệ số đẹp,
     * rút phích. Hoàn gốc thì thoát giữa chừng không lợi mà cũng không thiệt.</p>
     */
    public void nguoiChoiRoiGame(Player pl) {
        if (pl == null) {
            return;
        }
        Van v = vans.remove(pl.id);
        if (v == null || v.ketThuc) {
            return;
        }
        KhoVang.themKhoa(pl, v.cuoc);
        Logger.warning("[DaoVang] " + pl.name + " roi game giua van, hoan "
                + v.cuoc + " thoi vang khoa.\n");
    }

    // ------------------------------------------------------------------
    //  Cho MiniGameService đọc
    // ------------------------------------------------------------------

    public boolean coVan(Player pl) {
        Van v = vans.get(pl.id);
        return v != null && !v.ketThuc;
    }

    public long cuocCuaVan(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 0 : v.cuoc;
    }

    public int soDaMo(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 0 : v.soDaMo;
    }

    public long tienRutDuoc(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 0 : tienRut(v.cuoc, v.soDaMo);
    }

    /** Hệ số nếu mở thêm một ô nữa — để client hiện "mở tiếp được bao nhiêu". */
    public long tienNeuMoThem(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 0 : tienRut(v.cuoc, v.soDaMo + 1);
    }

    /**
     * Trạng thái từng ô để client vẽ.
     *
     * <p>0 chưa mở, 1 đã mở an toàn, 2 mìn đã nổ. Ô chưa mở luôn là 0 kể cả khi
     * bên dưới có mìn — <b>không lộ sơ đồ</b>. Chỉ khi ván đã kết thúc mới lộ
     * hết, lúc đó không còn gì để lợi dụng.</p>
     */
    public byte[] trangThaiO(Player pl) {
        byte[] ra = new byte[SO_O];
        Van v = vans.get(pl.id);
        if (v == null) {
            return ra;
        }
        for (int i = 0; i < SO_O; i++) {
            if (v.daMo[i]) {
                ra[i] = (byte) (v.coMin[i] ? 2 : 1);
            } else if (v.ketThuc && v.coMin[i]) {
                ra[i] = 3;
            }
        }
        return ra;
    }
}
