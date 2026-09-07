package nro.service;

import java.util.List;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.gameplay.taixiu.TaiXiuManager;
import nro.net.io.Message;
import nro.repository.dao.TaiXiuDAO;

/**
 * Đóng gói và gửi dữ liệu <b>Tài Xỉu</b> xuống client.
 *
 * <h2>Mã gói 118</h2>
 *
 * <p>Chọn 118 sau khi đối chiếu <b>cả ba</b> bảng lệnh: {@code Controller} của
 * máy chủ, {@code Controller.cs} và {@code Controller2.cs} của client. Mã 121
 * nhìn thì trống ở máy chủ nhưng {@code Controller2.cs} phía client đã dùng —
 * mà hàm đó chạy <b>trước</b> switch chính và đọc chung một {@code reader}, nên
 * nó nuốt mất byte đầu và gói tới sau sẽ đọc lệch hết.</p>
 *
 * <h2>Byte đầu là loại việc</h2>
 *
 * <p>Một mã lệnh gánh cả chiều lên lẫn chiều xuống, phân biệt bằng byte đầu.
 * Cách này giữ cho bảng lệnh khỏi phình ra bốn mã cho một tính năng.</p>
 *
 * <pre>
 * Client -&gt; máy chủ
 *   0  mở bảng: xin trạng thái và đăng ký nhận nhịp
 *   1  đặt cược: byte cửa (0 Xỉu, 1 Tài), long số thỏi
 *   2  đóng bảng: thôi nhận nhịp
 *
 * Máy chủ -&gt; client
 *   0  trạng thái đầy đủ
 *   1  nhịp mỗi giây (chỉ những thứ đổi liên tục)
 *   2  kết quả ván
 * </pre>
 */
public class TaiXiuService {

    private static TaiXiuService instance;

    public static TaiXiuService gI() {
        if (instance == null) {
            instance = new TaiXiuService();
        }
        return instance;
    }

    public static final int MA_GOI = 118;

    private static final byte LEN_MO_BANG = 0;
    private static final byte LEN_DAT_CUOC = 1;
    private static final byte LEN_DONG_BANG = 2;
    private static final byte LEN_XIN_LS_TOI = 3;
    private static final byte LEN_XIN_LS_SERVER = 4;

    private static final byte XUONG_TRANG_THAI = 0;
    private static final byte XUONG_NHIP = 1;
    private static final byte XUONG_KET_QUA = 2;
    private static final byte XUONG_LS_TOI = 3;
    private static final byte XUONG_LS_SERVER = 4;

    /** Nhận gói từ client. */
    public void nhanGoi(Player pl, Message msg) {
        if (pl == null) {
            return;
        }
        try {
            byte lenh = msg.reader().readByte();
            switch (lenh) {
                case LEN_MO_BANG:
                    TaiXiuManager.gI().themNguoiXem(pl);
                    guiTrangThai(pl);
                    break;
                case LEN_DAT_CUOC: {
                    byte cua = msg.reader().readByte();
                    long soThoi = msg.reader().readLong();
                    TaiXiuManager.gI().datCuoc(pl, cua, soThoi);
                    break;
                }
                case LEN_DONG_BANG:
                    TaiXiuManager.gI().boNguoiXem(pl);
                    break;
                case LEN_XIN_LS_TOI:
                    docLichSuRoiGui(pl, true);
                    break;
                case LEN_XIN_LS_SERVER:
                    docLichSuRoiGui(pl, false);
                    break;
            }
        } catch (Exception e) {
            Logger.logException(TaiXiuService.class, e);
        }
    }

    /**
     * Đọc lịch sử rồi gửi, trên một luồng riêng.
     *
     * <p>Hàm gọi nằm trên luồng đọc gói tin của người chơi đó. Truy vấn chậm —
     * cơ sở dữ liệu bận, ổ cứng kẹt — thì luồng ấy đứng, và người chơi thấy cả
     * game khựng chứ không phải chỉ mỗi bảng lịch sử.</p>
     */
    private void docLichSuRoiGui(Player pl, boolean cuaToi) {
        Thread t = new Thread(() -> {
            try {
                if (cuaToi) {
                    guiLichSuToi(pl, TaiXiuDAO.lichSuCuaToi(pl.id));
                } else {
                    guiLichSuServer(pl, TaiXiuDAO.lichSuServer());
                }
            } catch (Exception e) {
                Logger.logException(TaiXiuService.class, e);
            }
        }, "Tai Xiu doc lich su");
        t.setDaemon(true);
        t.start();
    }

    private void guiLichSuToi(Player pl, List<TaiXiuDAO.DongCuoc> ds) {
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(XUONG_LS_TOI);
            msg.writer().writeByte(ds.size());
            for (TaiXiuDAO.DongCuoc d : ds) {
                msg.writer().writeLong(d.phien);
                msg.writer().writeByte(d.cua);
                msg.writer().writeLong(d.soThoi);
                msg.writer().writeByte(d.ketQua);
                msg.writer().writeByte(d.thang ? 1 : 0);
                msg.writer().writeLong(d.tienThang);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(TaiXiuService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void guiLichSuServer(Player pl, List<TaiXiuDAO.DongPhien> ds) {
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(XUONG_LS_SERVER);
            msg.writer().writeByte(ds.size());
            for (TaiXiuDAO.DongPhien d : ds) {
                msg.writer().writeLong(d.phien);
                msg.writer().writeByte(d.x1);
                msg.writer().writeByte(d.x2);
                msg.writer().writeByte(d.x3);
                msg.writer().writeByte(d.ketQua);
                msg.writer().writeLong(d.tongTai);
                msg.writer().writeLong(d.tongXiu);
                msg.writer().writeInt(d.soNguoi);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(TaiXiuService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // ------------------------------------------------------------------
    //  Chiều xuống
    // ------------------------------------------------------------------

    /** Trạng thái đầy đủ cho một người. */
    public void guiTrangThai(Player pl) {
        if (pl == null) {
            return;
        }
        TaiXiuManager m = TaiXiuManager.gI();
        Message msg = null;
        try {
            msg = new Message(MA_GOI);
            msg.writer().writeByte(XUONG_TRANG_THAI);
            msg.writer().writeByte(m.getGiaiDoan());
            msg.writer().writeInt(m.giayConLai());
            msg.writer().writeLong(m.getTongTai());
            msg.writer().writeLong(m.getTongXiu());
            msg.writer().writeByte(m.cuaDangDat(pl));
            msg.writer().writeLong(m.soThoiDangDat(pl));
            msg.writer().writeLong(m.demThoiVang(pl, true));
            msg.writer().writeLong(m.demThoiVang(pl, false));

            int[] xx = m.getXucXac();
            msg.writer().writeByte(xx[0]);
            msg.writer().writeByte(xx[1]);
            msg.writer().writeByte(xx[2]);
            // Cua thang da tinh san. Client khong tu so nguong — luat choi chi
            // duoc viet o mot cho, va cho do la may chu.
            msg.writer().writeByte(m.ketQuaHienTai());

            List<Byte> ls = m.getLichSu();
            msg.writer().writeByte(ls.size());
            for (Byte b : ls) {
                msg.writer().writeByte(b);
            }
            // Dat o CUOI, sau doan dai thay doi: them truong moi vao cuoi thi
            // khong phai dem lai vi tri cua nhung truong da co.
            msg.writer().writeLong(m.getPhien());
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(TaiXiuService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /** Trạng thái đầy đủ cho mọi người đang mở bảng. Dùng khi mở ván mới. */
    public void guiTrangThaiCho() {
        for (Player pl : TaiXiuManager.gI().nguoiDangXem()) {
            guiTrangThai(pl);
        }
    }

    /**
     * Nhịp mỗi giây.
     *
     * <p>Chỉ chở những thứ đổi liên tục — giây và hai con số tổng cược. Số dư
     * thỏi vàng và cược của bản thân chỉ đổi khi người chơi bấm, mà lúc đó máy
     * chủ đã gửi lại trạng thái đầy đủ rồi.</p>
     */
    public void guiNhip() {
        TaiXiuManager m = TaiXiuManager.gI();
        List<Player> ds = m.nguoiDangXem();
        if (ds.isEmpty()) {
            return;
        }
        for (Player pl : ds) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(XUONG_NHIP);
                msg.writer().writeByte(m.getGiaiDoan());
                msg.writer().writeInt(m.giayConLai());
                msg.writer().writeLong(m.getTongTai());
                msg.writer().writeLong(m.getTongXiu());
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(TaiXiuService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    /**
     * Kết quả ván.
     *
     * <p>Phải gọi <b>trước</b> khi xoá bảng cược: số tiền thắng của từng người
     * đọc ra từ chính bảng đó.</p>
     */
    public void guiKetQua(int[] xucXac, byte ketQua) {
        TaiXiuManager m = TaiXiuManager.gI();
        for (Player pl : m.nguoiDangXem()) {
            Message msg = null;
            try {
                msg = new Message(MA_GOI);
                msg.writer().writeByte(XUONG_KET_QUA);
                msg.writer().writeByte(xucXac[0]);
                msg.writer().writeByte(xucXac[1]);
                msg.writer().writeByte(xucXac[2]);
                msg.writer().writeByte(ketQua);
                msg.writer().writeLong(m.tienThangCuaVanVua(pl, ketQua));
                msg.writer().writeLong(m.demThoiVang(pl, true));
                msg.writer().writeLong(m.demThoiVang(pl, false));
                pl.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(TaiXiuService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }
}
