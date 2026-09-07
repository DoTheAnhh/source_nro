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
 * Trò <b>Cao Thấp</b> — lật một lá, đoán lá sau cao hay thấp hơn.
 *
 * <h2>Luật</h2>
 *
 * <p>Lá từ 1 đến 13. Đoán đúng thì hệ số nhân dồn lên và chơi tiếp với lá vừa
 * lật; đoán sai mất hết. <b>Rút bất cứ lúc nào</b>.</p>
 *
 * <p>Bằng điểm tính là <b>thua</b>. Không tính hoà vì hoà thì phải quyết định
 * trả lại tiền hay chơi lại, mà cả hai đều làm luật rối thêm cho một trường hợp
 * hiếm.</p>
 *
 * <h2>Hệ số theo đúng lá đang cầm</h2>
 *
 * <p>Đây là chỗ khác Đào Vàng: cơ hội đoán đúng <b>phụ thuộc lá hiện tại</b>.
 * Cầm lá 2 mà đoán "cao hơn" thì gần như chắc thắng, nên hệ số phải thấp; cầm lá
 * 12 mà đoán "cao hơn" thì hiếm, hệ số cao.</p>
 *
 * <p>Tính thẳng từ số lá thoả mãn: đoán cao với lá {@code n} thì có
 * {@code 13 - n} lá thắng trên 13 lá, hệ số công bằng là {@code 13 / (13 - n)}.
 * Nhân với {@link #PHAN_TRAM_TRA} để lấy phần lợi thế nhà cái.</p>
 *
 * <h2>Không có phiên</h2>
 *
 * <p>Chơi một mình, mỗi bước một gói tin, không phải chờ đồng hồ nào.</p>
 */
public final class CaoThapManager {

    private static CaoThapManager instance;

    public static CaoThapManager gI() {
        if (instance == null) {
            instance = new CaoThapManager();
        }
        return instance;
    }

    public static final int MA_TRO = MiniGameService.TRO_CAO_THAP;

    /** Lá thấp nhất và cao nhất. */
    public static final int LA_MIN = 1;
    public static final int LA_MAX = 13;

    public static final long CUOC_TOI_DA = 300;
    public static final long CUOC_TOI_THIEU = 10;

    /** Đoán được nhiều nhất bấy nhiêu lần rồi buộc phải rút. */
    public static final int TOI_DA_DOAN = 8;

    /** Phần trăm trả về so với mức công bằng. */
    private static final long PHAN_TRAM_TRA = 94;

    public static final byte DOAN_THAP = 0;
    public static final byte DOAN_CAO = 1;

    private static final class Van {
        final long cuoc;
        int laHienTai;
        int soLanDung;
        /** Hệ số dồn, nhân 100. */
        long heSoX100 = 100;
        boolean ketThuc;

        Van(long cuoc, int la) {
            this.cuoc = cuoc;
            this.laHienTai = la;
        }
    }

    private final Map<Long, Van> vans = new ConcurrentHashMap<>();
    private final SecureRandom ngauNhien = new SecureRandom();

    private CaoThapManager() {
    }

    private int bocLa() {
        return LA_MIN + ngauNhien.nextInt(LA_MAX - LA_MIN + 1);
    }

    /**
     * Hệ số cho một lần đoán, nhân 100.
     *
     * <p>Trả 100 (tức nhân 1) khi nước đoán chắc thắng hoặc chắc thua — không có
     * gì để thưởng cho một lựa chọn không có rủi ro.</p>
     */
    public static long heSoMotBuoc(int la, byte huong) {
        int soLaThang = (huong == DOAN_CAO) ? (LA_MAX - la) : (la - LA_MIN);
        if (soLaThang <= 0) {
            return 100;
        }
        long congBangX100 = (long) LA_MAX * 100 / soLaThang;
        return congBangX100 * PHAN_TRAM_TRA / 100;
    }

    /** Số thỏi rút được nếu dừng ngay bây giờ. */
    private static long tienRut(Van v) {
        return (v.cuoc * v.heSoX100 + 50) / 100;
    }

    // ------------------------------------------------------------------
    //  Các bước chơi
    // ------------------------------------------------------------------

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
        vans.put(pl.id, new Van(cuoc, bocLa()));
        MiniGameService.gI().caoThapGuiTrangThai(pl, -1);
    }

    /** Đoán lá tiếp theo cao hơn hay thấp hơn. */
    public void doan(Player pl, byte huong) {
        if (pl == null) {
            return;
        }
        Van v = vans.get(pl.id);
        if (v == null || v.ketThuc) {
            Service.gI().sendThongBao(pl, "Bạn chưa vào ván nào!");
            return;
        }
        if (huong != DOAN_CAO && huong != DOAN_THAP) {
            return;
        }
        int laMoi;
        boolean dung;
        boolean hetLuot = false;
        long thuong = 0;
        synchronized (v) {
            if (v.ketThuc) {
                return;
            }
            laMoi = bocLa();
            dung = (huong == DOAN_CAO) ? (laMoi > v.laHienTai) : (laMoi < v.laHienTai);
            if (!dung) {
                v.ketThuc = true;
            } else {
                v.heSoX100 = v.heSoX100 * heSoMotBuoc(v.laHienTai, huong) / 100;
                v.laHienTai = laMoi;
                v.soLanDung++;
                if (v.soLanDung >= TOI_DA_DOAN) {
                    // Cham tran: tu rut cho nguoi choi.
                    v.ketThuc = true;
                    hetLuot = true;
                    thuong = tienRut(v);
                }
            }
        }

        if (!dung) {
            Service.gI().sendThongBao(pl, "Cao Thấp: lá " + laMoi
                    + " — đoán sai, mất " + v.cuoc + " thỏi vàng.");
            ghiVan(pl, v, false, 0);
        } else if (hetLuot) {
            traThuong(pl, thuong, v.soLanDung);
            ghiVan(pl, v, true, thuong);
        }
        MiniGameService.gI().caoThapGuiTrangThai(pl, laMoi);
    }

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
            if (v.soLanDung <= 0) {
                Service.gI().sendThongBao(pl, "Đoán đúng ít nhất một lần rồi mới rút được!");
                return;
            }
            v.ketThuc = true;
            thuong = tienRut(v);
        }
        traThuong(pl, thuong, v.soLanDung);
        ghiVan(pl, v, true, thuong);
        MiniGameService.gI().caoThapGuiTrangThai(pl, -1);
    }

    private void traThuong(Player pl, long thuong, int soLan) {
        if (thuong <= 0) {
            return;
        }
        if (KhoVang.themKhoa(pl, thuong)) {
            Service.gI().sendThongBao(pl, "Cao Thấp: đúng " + soLan
                    + " lần, nhận " + thuong + " thỏi vàng khoá!");
        } else {
            Logger.error("[CaoThap] Hanh trang day, khong tra duoc " + thuong
                    + " thoi cho " + pl.name + ".\n");
            Service.gI().sendThongBao(pl,
                    "Cao Thấp: hành trang đầy, không nhận được thưởng!");
        }
    }

    private void ghiVan(Player pl, Van v, boolean thang, long thuong) {
        final long id = pl.id;
        final String ten = pl.name;
        final long cuoc = v.cuoc;
        final int soLan = v.soLanDung;
        Thread t = new Thread(() -> {
            try {
                MiniGameDAO.ghiCuoc(MA_TRO, 0, id, ten, soLan, cuoc, soLan,
                        thang, thuong);
            } catch (Exception e) {
                Logger.logException(CaoThapManager.class, e);
            }
        }, "Cao Thap ghi lich su");
        t.setDaemon(true);
        t.start();
    }

    /** Hoàn tiền cược gốc khi rời game giữa ván — xem chú thích ở Đào Vàng. */
    public void nguoiChoiRoiGame(Player pl) {
        if (pl == null) {
            return;
        }
        Van v = vans.remove(pl.id);
        if (v == null || v.ketThuc) {
            return;
        }
        KhoVang.themKhoa(pl, v.cuoc);
        Logger.warning("[CaoThap] " + pl.name + " roi game giua van, hoan "
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

    public int laHienTai(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 0 : v.laHienTai;
    }

    public int soLanDung(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 0 : v.soLanDung;
    }

    public long heSoX100(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 100 : v.heSoX100;
    }

    public long tienRutDuoc(Player pl) {
        Van v = vans.get(pl.id);
        return v == null ? 0 : tienRut(v);
    }
}
