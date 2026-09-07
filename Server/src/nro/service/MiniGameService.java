package nro.service;

import java.util.List;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.gameplay.minigame.BauCuaManager;
import nro.gameplay.minigame.CaoThapManager;
import nro.gameplay.minigame.CauCaManager;
import nro.gameplay.minigame.DaoVangManager;
import nro.gameplay.minigame.DuaNguaManager;
import nro.gameplay.minigame.XocDiaManager;
import nro.gameplay.minigame.KhoVang;
import nro.net.io.Message;
import nro.repository.dao.MiniGameDAO;

/**
 * Cổng gói tin của <b>khu trò chơi nhỏ</b> — mọi trò trừ Tài Xỉu.
 *
 * <h2>Mã gói 117</h2>
 *
 * <p>Chọn 117 sau khi soát <b>cả ba</b> nơi: bảng lệnh {@code Controller} của
 * máy chủ, {@code Controller.cs} của client, và mọi lời gọi
 * {@code new Message(...)} ở hai bên. Chỉ soát bảng lệnh là chưa đủ — mã 122
 * nhìn thì trống trong {@code Controller.java} nhưng {@code Service.java} vẫn
 * gửi đi, tức nó đã có chủ.</p>
 *
 * <h2>Một mã gói cho sáu trò</h2>
 *
 * <p>Byte đầu là <b>mã trò</b>, byte thứ hai là <b>việc</b>. Sáu trò mà sáu mã
 * gói thì bảng lệnh phình ra cho một tính năng, mà mã gói là thứ hiếm — cả hệ
 * chỉ có mấy chục chỗ trống.</p>
 *
 * <pre>
 * Client -&gt; máy chủ:  byte trò, byte việc, [dữ liệu]
 *   0  mở bảng: xin trạng thái và đăng ký nhận nhịp
 *   1  hành động: đặt cược / mở ô / rút tiền — tuỳ trò
 *   2  đóng bảng: thôi nhận nhịp
 *   3  xin lịch sử của tôi
 *   4  xin lịch sử máy chủ
 *
 * Máy chủ -&gt; client:  byte trò, byte việc, [dữ liệu]
 *   0  trạng thái đầy đủ
 *   1  nhịp mỗi giây (chỉ những thứ đổi liên tục)
 *   2  kết quả ván
 *   3  lịch sử của tôi
 *   4  lịch sử máy chủ
 * </pre>
 *
 * <h2>Tách hẳn khỏi mã của game</h2>
 *
 * <p>Toàn bộ luật chơi nằm trong package {@code nro.gameplay.minigame}, dữ liệu
 * nằm trong hai bảng {@code mg_*}, và đường mạng đi qua đúng một mã gói. Muốn
 * gỡ cả khu trò chơi thì xoá package đó, hai bảng đó, và một dòng trong
 * {@code Controller} — không đụng gì tới phần còn lại của máy chủ.</p>
 */
public final class MiniGameService {

    private static MiniGameService instance;

    public static MiniGameService gI() {
        if (instance == null) {
            instance = new MiniGameService();
        }
        return instance;
    }

    public static final int MA_GOI = 117;

    // ---- mã trò ----
    public static final byte TRO_BAU_CUA = 0;
    public static final byte TRO_XOC_DIA = 1;
    public static final byte TRO_DUA_NGUA = 2;
    public static final byte TRO_DAO_VANG = 3;
    public static final byte TRO_CAO_THAP = 4;
    public static final byte TRO_CAU_CA = 5;

    // ---- việc, chiều lên ----
    private static final byte LEN_MO_BANG = 0;
    private static final byte LEN_HANH_DONG = 1;
    private static final byte LEN_DONG_BANG = 2;
    private static final byte LEN_XIN_LS_TOI = 3;
    private static final byte LEN_XIN_LS_SERVER = 4;

    // ---- việc, chiều xuống ----
    private static final byte XUONG_TRANG_THAI = 0;
    private static final byte XUONG_NHIP = 1;
    private static final byte XUONG_KET_QUA = 2;
    private static final byte XUONG_LS_TOI = 3;
    private static final byte XUONG_LS_SERVER = 4;

    /**
     * Riêng Câu Cá: cá đã cắn mồi, kèm bộ tham số độ khó.
     *
     * <p>Mã 5 chứ không dùng lại mã 0 (trạng thái): hai gói này mang hai nghĩa
     * khác nhau, và client cần phân biệt để biết lúc nào <b>bắt đầu</b> pha vật
     * lộn. Gói trạng thái tới bất cứ lúc nào, gói này chỉ tới đúng một lần mỗi
     * lượt câu.</p>
     */
    private static final byte XUONG_CA_CAN = 5;

    private MiniGameService() {
    }

    // ==================================================================
    //  Chiều lên
    // ==================================================================

    /** Nhận gói từ client. */
    public void nhanGoi(Player pl, Message msg) {
        if (pl == null) {
            return;
        }
        try {
            byte tro = msg.reader().readByte();
            byte viec = msg.reader().readByte();
            switch (tro) {
                case TRO_BAU_CUA:
                    bauCuaNhanViec(pl, viec, msg);
                    break;
                case TRO_XOC_DIA:
                    xocDiaNhanViec(pl, viec, msg);
                    break;
                case TRO_DUA_NGUA:
                    duaNguaNhanViec(pl, viec, msg);
                    break;
                case TRO_DAO_VANG:
                    daoVangNhanViec(pl, viec, msg);
                    break;
                case TRO_CAO_THAP:
                    caoThapNhanViec(pl, viec, msg);
                    break;
                case TRO_CAU_CA:
                    cauCaNhanViec(pl, viec, msg);
                    break;
                default:
                    // Ma tro la: bo qua trong im lang. Client cu hon may chu
                    // thi gap chuyen nay, khong phai ke pha.
                    break;
            }
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        }
    }

    private void bauCuaNhanViec(Player pl, byte viec, Message msg) throws Exception {
        BauCuaManager m = BauCuaManager.gI();
        switch (viec) {
            case LEN_MO_BANG:
                m.themNguoiXem(pl);
                bauCuaGuiTrangThai(pl);
                break;
            case LEN_HANH_DONG: {
                int cua = msg.reader().readByte();
                long soThoi = msg.reader().readLong();
                m.datCuoc(pl, cua, soThoi);
                break;
            }
            case LEN_DONG_BANG:
                m.boNguoiXem(pl);
                break;
            case LEN_XIN_LS_TOI:
                docLichSuRoiGui(pl, TRO_BAU_CUA, true);
                break;
            case LEN_XIN_LS_SERVER:
                docLichSuRoiGui(pl, TRO_BAU_CUA, false);
                break;
            default:
                break;
        }
    }

    /**
     * Đọc lịch sử rồi gửi, trên một luồng riêng.
     *
     * <p>Hàm gọi nằm trên luồng đọc gói tin của người chơi đó. Truy vấn chậm —
     * cơ sở dữ liệu bận, ổ cứng kẹt — thì luồng ấy đứng, và người chơi thấy cả
     * game khựng chứ không phải chỉ mỗi bảng lịch sử.</p>
     */
    private void docLichSuRoiGui(Player pl, byte tro, boolean cuaToi) {
        Thread t = new Thread(() -> {
            try {
                if (cuaToi) {
                    guiLichSuToi(pl, tro, MiniGameDAO.lichSuCuaToi(tro, pl.id));
                } else {
                    guiLichSuServer(pl, tro, MiniGameDAO.lichSuServer(tro));
                }
            } catch (Exception e) {
                Logger.logException(MiniGameService.class, e);
            }
        }, "Mini game doc lich su");
        t.setDaemon(true);
        t.start();
    }

    // ==================================================================
    //  Chiều xuống — dùng chung
    // ==================================================================

    private void guiLichSuToi(Player pl, byte tro, List<MiniGameDAO.DongCuoc> ds) {
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(tro);
            msg.writer().writeByte(XUONG_LS_TOI);
            msg.writer().writeByte(ds.size());
            for (MiniGameDAO.DongCuoc d : ds) {
                msg.writer().writeLong(d.phien);
                msg.writer().writeByte(d.cua);
                msg.writer().writeLong(d.soThoi);
                msg.writer().writeByte(d.ketQua);
                msg.writer().writeByte(d.thang ? 1 : 0);
                msg.writer().writeLong(d.tienThang);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void guiLichSuServer(Player pl, byte tro, List<MiniGameDAO.DongPhien> ds) {
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(tro);
            msg.writer().writeByte(XUONG_LS_SERVER);
            msg.writer().writeByte(ds.size());
            for (MiniGameDAO.DongPhien d : ds) {
                msg.writer().writeLong(d.phien);
                msg.writer().writeByte(d.kq1);
                msg.writer().writeByte(d.kq2);
                msg.writer().writeByte(d.kq3);
                msg.writer().writeByte(d.ketQua);
                msg.writer().writeLong(d.tongCuoc);
                msg.writer().writeLong(d.tongTra);
                msg.writer().writeInt(d.soNguoi);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // ==================================================================
    //  Bầu Cua
    // ==================================================================

    /** Trạng thái đầy đủ cho một người. */
    public void bauCuaGuiTrangThai(Player pl) {
        if (pl == null) {
            return;
        }
        BauCuaManager m = BauCuaManager.gI();
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_BAU_CUA);
            msg.writer().writeByte(XUONG_TRANG_THAI);
            msg.writer().writeLong(m.getPhien());
            msg.writer().writeByte(m.getGiaiDoan());
            msg.writer().writeInt(m.giayConLai());

            long[] tong = m.getTongTheoCua();
            long[] cuaToi = m.cuocCuaToi(pl);
            for (int i = 0; i < BauCuaManager.SO_CUA; i++) {
                msg.writer().writeLong(tong[i]);
                msg.writer().writeLong(cuaToi[i]);
            }

            msg.writer().writeLong(KhoVang.dem(pl, true));
            msg.writer().writeLong(KhoVang.dem(pl, false));

            int[] xx = m.getXucXac();
            msg.writer().writeByte(xx[0]);
            msg.writer().writeByte(xx[1]);
            msg.writer().writeByte(xx[2]);

            List<Byte> ls = m.getLichSu();
            msg.writer().writeByte(ls.size());
            for (Byte b : ls) {
                msg.writer().writeByte(b);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /** Trạng thái đầy đủ cho mọi người đang mở bảng. */
    public void bauCuaGuiTrangThaiCho() {
        for (Player pl : BauCuaManager.gI().nguoiDangXem()) {
            bauCuaGuiTrangThai(pl);
        }
    }

    /**
     * Nhịp mỗi giây.
     *
     * <p>Chỉ gửi những thứ đổi liên tục — giai đoạn, số giây, tổng cược sáu cửa.
     * Không gửi lịch sử và xúc xắc: chúng chỉ đổi lúc chốt ván, mà lúc đó đã có
     * gói trạng thái đầy đủ rồi. Một gói mỗi giây nhân với số người đang xem nên
     * mỗi byte thừa đều tính.</p>
     */
    public void bauCuaGuiNhip() {
        BauCuaManager m = BauCuaManager.gI();
        byte gd = m.getGiaiDoan();
        int giay = m.giayConLai();
        long[] tong = m.getTongTheoCua();
        for (Player pl : m.nguoiDangXem()) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(TRO_BAU_CUA);
                msg.writer().writeByte(XUONG_NHIP);
                msg.writer().writeByte(gd);
                msg.writer().writeInt(giay);
                for (int i = 0; i < BauCuaManager.SO_CUA; i++) {
                    msg.writer().writeLong(tong[i]);
                }
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(MiniGameService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    /** Kết quả ván: ba con và số thỏi người đó nhận được. */
    public void bauCuaGuiKetQua(int[] xucXac) {
        BauCuaManager m = BauCuaManager.gI();
        for (Player pl : m.nguoiDangXem()) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(TRO_BAU_CUA);
                msg.writer().writeByte(XUONG_KET_QUA);
                msg.writer().writeLong(m.getPhien());
                msg.writer().writeByte(xucXac[0]);
                msg.writer().writeByte(xucXac[1]);
                msg.writer().writeByte(xucXac[2]);
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(MiniGameService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    // ==================================================================
    //  Xóc Đĩa
    // ==================================================================

    private void xocDiaNhanViec(Player pl, byte viec, Message msg) throws Exception {
        XocDiaManager m = XocDiaManager.gI();
        switch (viec) {
            case LEN_MO_BANG:
                m.themNguoiXem(pl);
                xocDiaGuiTrangThai(pl);
                break;
            case LEN_HANH_DONG: {
                int cua = msg.reader().readByte();
                long soThoi = msg.reader().readLong();
                m.datCuoc(pl, cua, soThoi);
                break;
            }
            case LEN_DONG_BANG:
                m.boNguoiXem(pl);
                break;
            case LEN_XIN_LS_TOI:
                docLichSuRoiGui(pl, TRO_XOC_DIA, true);
                break;
            case LEN_XIN_LS_SERVER:
                docLichSuRoiGui(pl, TRO_XOC_DIA, false);
                break;
            default:
                break;
        }
    }

    public void xocDiaGuiTrangThai(Player pl) {
        if (pl == null) {
            return;
        }
        XocDiaManager m = XocDiaManager.gI();
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_XOC_DIA);
            msg.writer().writeByte(XUONG_TRANG_THAI);
            msg.writer().writeLong(m.getPhien());
            msg.writer().writeByte(m.getGiaiDoan());
            msg.writer().writeInt(m.giayConLai());
            long[] tong = m.getTongTheoCua();
            long[] cuaToi = m.cuocCuaToi(pl);
            for (int i = 0; i < XocDiaManager.SO_CUA; i++) {
                msg.writer().writeLong(tong[i]);
                msg.writer().writeLong(cuaToi[i]);
            }
            msg.writer().writeLong(KhoVang.dem(pl, true));
            msg.writer().writeLong(KhoVang.dem(pl, false));
            msg.writer().writeByte(m.getDongXu());
            List<Byte> ls = m.getLichSu();
            msg.writer().writeByte(ls.size());
            for (Byte b : ls) {
                msg.writer().writeByte(b);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void xocDiaGuiTrangThaiCho() {
        for (Player pl : XocDiaManager.gI().nguoiDangXem()) {
            xocDiaGuiTrangThai(pl);
        }
    }

    public void xocDiaGuiNhip() {
        XocDiaManager m = XocDiaManager.gI();
        byte gd = m.getGiaiDoan();
        int giay = m.giayConLai();
        long[] tong = m.getTongTheoCua();
        for (Player pl : m.nguoiDangXem()) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(TRO_XOC_DIA);
                msg.writer().writeByte(XUONG_NHIP);
                msg.writer().writeByte(gd);
                msg.writer().writeInt(giay);
                for (int i = 0; i < XocDiaManager.SO_CUA; i++) {
                    msg.writer().writeLong(tong[i]);
                }
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(MiniGameService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void xocDiaGuiKetQua(int dongXu) {
        XocDiaManager m = XocDiaManager.gI();
        for (Player pl : m.nguoiDangXem()) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(TRO_XOC_DIA);
                msg.writer().writeByte(XUONG_KET_QUA);
                msg.writer().writeLong(m.getPhien());
                msg.writer().writeByte(dongXu);
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(MiniGameService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    // ==================================================================
    //  Đua Ngựa
    // ==================================================================

    private void duaNguaNhanViec(Player pl, byte viec, Message msg) throws Exception {
        DuaNguaManager m = DuaNguaManager.gI();
        switch (viec) {
            case LEN_MO_BANG:
                m.themNguoiXem(pl);
                duaNguaGuiTrangThai(pl);
                break;
            case LEN_HANH_DONG: {
                int con = msg.reader().readByte();
                long soThoi = msg.reader().readLong();
                m.datCuoc(pl, con, soThoi);
                break;
            }
            case LEN_DONG_BANG:
                m.boNguoiXem(pl);
                break;
            case LEN_XIN_LS_TOI:
                docLichSuRoiGui(pl, TRO_DUA_NGUA, true);
                break;
            case LEN_XIN_LS_SERVER:
                docLichSuRoiGui(pl, TRO_DUA_NGUA, false);
                break;
            default:
                break;
        }
    }

    public void duaNguaGuiTrangThai(Player pl) {
        if (pl == null) {
            return;
        }
        DuaNguaManager m = DuaNguaManager.gI();
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_DUA_NGUA);
            msg.writer().writeByte(XUONG_TRANG_THAI);
            msg.writer().writeLong(m.getPhien());
            msg.writer().writeByte(m.getGiaiDoan());
            msg.writer().writeInt(m.giayConLai());
            long[] tong = m.getTongTheoCon();
            long[] cuaToi = m.cuocCuaToi(pl);
            for (int i = 0; i < DuaNguaManager.SO_NGUA; i++) {
                msg.writer().writeLong(tong[i]);
                msg.writer().writeLong(cuaToi[i]);
            }
            msg.writer().writeLong(KhoVang.dem(pl, true));
            msg.writer().writeLong(KhoVang.dem(pl, false));
            // Thu tu ve dich chi co nghia tu pha dua tro di; o pha dat cuoc thi
            // no la thu tu cu, client khong dung toi.
            int[] tt = m.getThuTuVe();
            for (int i = 0; i < DuaNguaManager.SO_NGUA; i++) {
                msg.writer().writeByte(tt[i]);
            }
            List<Byte> ls = m.getLichSu();
            msg.writer().writeByte(ls.size());
            for (Byte b : ls) {
                msg.writer().writeByte(b);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void duaNguaGuiTrangThaiCho() {
        for (Player pl : DuaNguaManager.gI().nguoiDangXem()) {
            duaNguaGuiTrangThai(pl);
        }
    }

    public void duaNguaGuiNhip() {
        DuaNguaManager m = DuaNguaManager.gI();
        byte gd = m.getGiaiDoan();
        int giay = m.giayConLai();
        long[] tong = m.getTongTheoCon();
        for (Player pl : m.nguoiDangXem()) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(TRO_DUA_NGUA);
                msg.writer().writeByte(XUONG_NHIP);
                msg.writer().writeByte(gd);
                msg.writer().writeInt(giay);
                for (int i = 0; i < DuaNguaManager.SO_NGUA; i++) {
                    msg.writer().writeLong(tong[i]);
                }
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(MiniGameService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void duaNguaGuiKetQua() {
        DuaNguaManager m = DuaNguaManager.gI();
        int[] tt = m.getThuTuVe();
        for (Player pl : m.nguoiDangXem()) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(TRO_DUA_NGUA);
                msg.writer().writeByte(XUONG_KET_QUA);
                msg.writer().writeLong(m.getPhien());
                for (int i = 0; i < DuaNguaManager.SO_NGUA; i++) {
                    msg.writer().writeByte(tt[i]);
                }
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(MiniGameService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    // ==================================================================
    //  Đào Vàng
    // ==================================================================

    private void daoVangNhanViec(Player pl, byte viec, Message msg) throws Exception {
        DaoVangManager m = DaoVangManager.gI();
        switch (viec) {
            case LEN_MO_BANG:
                daoVangGuiTrangThai(pl);
                break;
            case LEN_HANH_DONG: {
                // Byte dau la viec con: 0 vao van, 1 mo o, 2 rut tien.
                byte viecCon = msg.reader().readByte();
                if (viecCon == 0) {
                    m.batDauVan(pl, msg.reader().readLong());
                } else if (viecCon == 1) {
                    m.moO(pl, msg.reader().readByte());
                } else {
                    m.rutTien(pl);
                }
                break;
            }
            case LEN_XIN_LS_TOI:
                docLichSuRoiGui(pl, TRO_DAO_VANG, true);
                break;
            default:
                break;
        }
    }

    public void daoVangGuiTrangThai(Player pl) {
        if (pl == null) {
            return;
        }
        DaoVangManager m = DaoVangManager.gI();
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_DAO_VANG);
            msg.writer().writeByte(XUONG_TRANG_THAI);
            msg.writer().writeByte(m.coVan(pl) ? 1 : 0);
            msg.writer().writeLong(m.cuocCuaVan(pl));
            msg.writer().writeByte(m.soDaMo(pl));
            msg.writer().writeLong(m.tienRutDuoc(pl));
            msg.writer().writeLong(m.tienNeuMoThem(pl));
            byte[] o = m.trangThaiO(pl);
            msg.writer().writeByte(o.length);
            for (byte b : o) {
                msg.writer().writeByte(b);
            }
            msg.writer().writeLong(KhoVang.dem(pl, true));
            msg.writer().writeLong(KhoVang.dem(pl, false));
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // ==================================================================
    //  Cao Thấp
    // ==================================================================

    private void caoThapNhanViec(Player pl, byte viec, Message msg) throws Exception {
        CaoThapManager m = CaoThapManager.gI();
        switch (viec) {
            case LEN_MO_BANG:
                caoThapGuiTrangThai(pl, -1);
                break;
            case LEN_HANH_DONG: {
                byte viecCon = msg.reader().readByte();
                if (viecCon == 0) {
                    m.batDauVan(pl, msg.reader().readLong());
                } else if (viecCon == 1) {
                    m.doan(pl, msg.reader().readByte());
                } else {
                    m.rutTien(pl);
                }
                break;
            }
            case LEN_XIN_LS_TOI:
                docLichSuRoiGui(pl, TRO_CAO_THAP, true);
                break;
            default:
                break;
        }
    }

    /**
     * Trạng thái Cao Thấp.
     *
     * @param laVuaLat lá vừa lật, hoặc {@code -1} khi không phải nước đoán —
     *                 client dùng để chạy hoạt hình lật bài
     */
    public void caoThapGuiTrangThai(Player pl, int laVuaLat) {
        if (pl == null) {
            return;
        }
        CaoThapManager m = CaoThapManager.gI();
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_CAO_THAP);
            msg.writer().writeByte(XUONG_TRANG_THAI);
            msg.writer().writeByte(m.coVan(pl) ? 1 : 0);
            msg.writer().writeLong(m.cuocCuaVan(pl));
            msg.writer().writeByte(m.laHienTai(pl));
            msg.writer().writeByte(m.soLanDung(pl));
            msg.writer().writeInt((int) m.heSoX100(pl));
            msg.writer().writeLong(m.tienRutDuoc(pl));
            msg.writer().writeByte(laVuaLat);
            // Hai he so cho nuoc doan ke tiep, de client hien truoc khi bam.
            int la = m.laHienTai(pl);
            msg.writer().writeInt((int) CaoThapManager.heSoMotBuoc(la,
                    CaoThapManager.DOAN_THAP));
            msg.writer().writeInt((int) CaoThapManager.heSoMotBuoc(la,
                    CaoThapManager.DOAN_CAO));
            msg.writer().writeLong(KhoVang.dem(pl, true));
            msg.writer().writeLong(KhoVang.dem(pl, false));
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // ==================================================================
    //  Câu Cá
    // ==================================================================

    private void cauCaNhanViec(Player pl, byte viec, Message msg) throws Exception {
        CauCaManager m = CauCaManager.gI();
        switch (viec) {
            case LEN_MO_BANG:
                cauCaGuiTrangThai(pl);
                break;
            case LEN_HANH_DONG: {
                // Byte viec con: 0 quang can, 1 bao ket qua vat lon.
                byte viecCon = msg.reader().readByte();
                if (viecCon == 0) {
                    // Byte tam quang: 0..100. Phai doc du byte nay du co dung
                    // hay khong — bo lai thi no nam trong luong doc va moi goi
                    // toi sau deu lech, ma loi hien ra o mot tinh nang khac han.
                    int xa = msg.reader().readByte();
                    m.quangCan(pl, xa);
                } else {
                    m.baoKetQua(pl, msg.reader().readByte() == 1);
                }
                break;
            }
            case LEN_XIN_LS_TOI:
                docLichSuRoiGui(pl, TRO_CAU_CA, true);
                break;
            default:
                break;
        }
    }

    public void cauCaGuiTrangThai(Player pl) {
        if (pl == null) {
            return;
        }
        CauCaManager m = CauCaManager.gI();
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_CAU_CA);
            msg.writer().writeByte(XUONG_TRANG_THAI);
            msg.writer().writeInt(m.giayConCho(pl));
            msg.writer().writeLong(CauCaManager.TIEN_MOI);
            msg.writer().writeByte(m.dangVatLon(pl) ? 1 : 0);
            msg.writer().writeLong(KhoVang.dem(pl, true));
            msg.writer().writeLong(KhoVang.dem(pl, false));
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Cá đã cắn mồi: gửi loại cá kèm bộ tham số độ khó.
     *
     * <p>Gửi cả bộ tham số xuống chứ không để client tự giữ bảng: bảng nằm một
     * chỗ ở máy chủ thì sửa cân bằng không phải dựng lại client, và không có
     * chuyện hai bên lệch số.</p>
     */
    public void cauCaGuiCaCan(Player pl, int loai, int choMs) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_CAU_CA);
            msg.writer().writeByte(XUONG_CA_CAN);
            msg.writer().writeByte(loai);
            int[] dk = CauCaManager.doKho(loai);
            msg.writer().writeByte(dk[0]);
            msg.writer().writeByte(dk[1]);
            msg.writer().writeByte(dk[2]);
            msg.writer().writeByte(dk[3]);
            msg.writer().writeByte(CauCaManager.TIEN_TRINH_DAU);
            msg.writer().writeInt(CauCaManager.thuongCua(loai));
            msg.writer().writeInt(choMs);
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Kết quả một lượt câu.
     *
     * @param batDuoc máy chủ đã <b>xét lại</b> báo cáo của client — gửi con số
     *                nó chốt chứ không để client dùng con số nó tự báo
     */
    public void cauCaGuiKetQua(Player pl, int loai, long thuong,
            boolean batDuoc) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(TRO_CAU_CA);
            msg.writer().writeByte(XUONG_KET_QUA);
            msg.writer().writeByte(loai);
            msg.writer().writeLong(thuong);
            msg.writer().writeByte(batDuoc ? 1 : 0);
            msg.writer().writeLong(KhoVang.dem(pl, true));
            msg.writer().writeLong(KhoVang.dem(pl, false));
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(MiniGameService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
