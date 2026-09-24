package nro.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.repository.dao.NroPassDAO;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * <b>NRO Pass</b> — kiểu Royale Pass.
 *
 * <p>Mỗi mùa một dãy cấp. Lên cấp bằng điểm pass, nhận khi làm xong nhiệm
 * vụ Bò Mộng — độ khó càng cao càng nhiều điểm. Mỗi cấp có một ô <b>Miễn phí</b> và một ô <b>Cao cấp</b>;
 * ô cao cấp mở khoá khi người chơi mua pass cao cấp của mùa — mua muộn vẫn
 * nhận lại được mọi ô cao cấp của những cấp đã đạt.</p>
 *
 * <h2>Ưu đãi chỉ số</h2>
 *
 * <p>Mua cao cấp đặt {@code Player.THE_THANG = 2} tới hết mùa. {@code NPoint}
 * vốn đã cộng ưu đãi khi trường ấy còn hạn, nay đọc phần trăm từ cấu hình
 * pass. Không dựng cơ chế ưu đãi thứ hai.</p>
 *
 * <p>Giao diện nằm trong màn Phúc lợi, dữ liệu nối vào <b>cuối</b> gói
 * {@code -58}.</p>
 */
public class NroPassService {

    private static NroPassService instance;

    public static NroPassService gI() {
        if (instance == null) {
            instance = new NroPassService();
        }
        return instance;
    }

    /** Việc client gửi lên trong gói -58. */
    public static final byte VIEC_MUA_CAO_CAP = 2;
    public static final byte VIEC_NHAN_O = 5;

    /** Mở đầu khối pass trong gói -58 (1 là khuôn thẻ tháng cũ, đã bỏ). */
    private static final byte KHOI_PASS = 2;

    /** Mỗi ô gửi tối đa bấy nhiêu món — đủ cho mọi bộ quà hợp lý. */
    private static final int TOI_DA_MON_MOT_O = 4;

    // =====================================================================
    //  Cấp
    // =====================================================================
    /** Cấp đạt được với số điểm này, không quá số cấp của pass. */
    public int capDat(long diem) {
        return (int) Math.min(NroPassDAO.soCap(), diem / NroPassDAO.diemMoiCap());
    }

    /** Nạp cờ cao cấp của mùa hiện tại vào người chơi — gọi ngay sau khi đọc nhân vật. */
    public void dongBoKhiNap(Player pl) {
        if (pl == null) {
            return;
        }
        try {
            int mua = NroPassDAO.mua();
            if (NroPassDAO.tienDo(pl.id, mua).caoCap) {
                pl.THE_THANG = 2;
                pl.LASTTIME_THE_THANG = NroPassDAO.hetMua();
            }
        } catch (Exception ex) {
            Logger.logException(NroPassService.class, ex, "Không nạp được NRO Pass");
        }
    }

    // =====================================================================
    //  Điểm pass
    // =====================================================================
    /**
     * Xong một nhiệm vụ Bò Mộng — nguồn điểm pass duy nhất.
     *
     * <p>Độ khó càng cao càng nhiều điểm (cấu hình {@code diem_nv_*}). Gọi
     * trong {@code TaskService.paySideTask}, đúng lúc trả thưởng, nên nhiệm vụ
     * huỷ ngang hay chưa xong không được tính.</p>
     */
    public void congNhiemVuBoMong(Player pl, int doKho) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        long diem = NroPassDAO.diemNhiemVu(doKho);
        if (diem <= 0) {
            return;
        }
        int mua = NroPassDAO.mua();
        long truoc = NroPassDAO.tienDo(pl.id, mua).diem;
        congDiem(pl, diem);
        int capTruoc = capDat(truoc);
        int capSau = capDat(truoc + diem);
        Service.gI().sendThongBao(pl, "+" + diem + " điểm " + NroPassDAO.ten()
                + (capSau > capTruoc ? " — lên cấp " + capSau + "!" : ""));
    }

    private void congDiem(Player pl, long them) {
        if (them > 0) {
            NroPassDAO.congDiem(pl.id, NroPassDAO.mua(), them);
        }
    }

    // =====================================================================
    //  Mở khoá cao cấp
    // =====================================================================
    /**
     * Mua pass cao cấp của mùa hiện tại.
     *
     * <p>Khoá trên người chơi: bấm hai lần liền tay thì lần hai phải thấy cờ
     * cao cấp của lần một, không thì trừ tiền hai lần.</p>
     */
    public void muaCaoCap(Player pl) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        synchronized (pl) {
            int mua = NroPassDAO.mua();
            if (NroPassDAO.tienDo(pl.id, mua).caoCap) {
                Service.gI().sendThongBao(pl, "Bạn đã mở " + NroPassDAO.ten() + " cao cấp mùa này rồi.");
                return;
            }
            if (!truTien(pl, NroPassDAO.so(NroPassDAO.K_GIA_CAO_CAP), NroPassDAO.chu(NroPassDAO.K_LOAI_TIEN))) {
                return;
            }
            if (!NroPassDAO.moCaoCap(pl.id, mua)) {
                Logger.error("NRO PASS: đã trừ tiền nhưng KHÔNG ghi được cao cấp cho " + pl.name
                        + " (id " + pl.id + "), mùa " + mua + "\n");
            }
            pl.THE_THANG = 2;
            pl.LASTTIME_THE_THANG = NroPassDAO.hetMua();
            capNhatChiSo(pl);
            Service.gI().sendThongBao(pl, "Đã mở khoá " + NroPassDAO.ten()
                    + " cao cấp! Nhận lại được mọi ô cao cấp của các cấp đã đạt.");
            PhucLoiService.gI().guiDuLieu(pl);
        }
    }

    /** Trừ tiền; không đủ thì báo và trả {@code false}. */
    private boolean truTien(Player pl, long gia, String loai) {
        gia = Math.max(0, gia);
        if (NroPassDAO.TIEN_HONG_NGOC.equals(loai)) {
            if (pl.inventory.ruby < gia) {
                Service.gI().sendThongBao(pl, "Không đủ hồng ngọc, cần " + Util.soCham(gia) + ".");
                return false;
            }
            pl.inventory.subRuby((int) gia);
            Service.gI().sendMoney(pl);
            return true;
        }
        if (NroPassDAO.TIEN_NGOC.equals(loai)) {
            if (pl.inventory.gem < gia) {
                Service.gI().sendThongBao(pl, "Không đủ ngọc, cần " + Util.soCham(gia) + ".");
                return false;
            }
            pl.inventory.subGem((int) gia);
            Service.gI().sendMoney(pl);
            return true;
        }
        if (pl.getSession() == null) {
            return false;
        }
        if (pl.getSession().vnd < gia) {
            Service.gI().sendThongBao(pl, "Số dư không đủ, cần " + Util.soCham(gia)
                    + " VNĐ, đang có " + Util.soCham(pl.getSession().vnd) + " VNĐ.");
            return false;
        }
        if (!nro.repository.schema.DatabaseUpdater.subVND_byPlayer(pl, (int) gia)) {
            Service.gI().sendThongBao(pl, "Không trừ được số dư, thử lại sau.");
            return false;
        }
        return true;
    }

    private void capNhatChiSo(Player pl) {
        try {
            pl.nPoint.calPoint();
            Service.gI().point(pl);
        } catch (Exception ex) {
            Logger.logException(NroPassService.class, ex, "Không tính lại được chỉ số sau NRO Pass");
        }
    }

    // =====================================================================
    //  Nhận quà
    // =====================================================================
    /** Nhận một ô (bấm vào ô trên màn hình). */
    public void nhanO(Player pl, int cap, int hang) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        synchronized (pl) {
            int mua = NroPassDAO.mua();
            NroPassDAO.TienDo td = NroPassDAO.tienDo(pl.id, mua);
            nhanMotO(pl, mua, td, NroPassDAO.daNhan(pl.id, mua), cap, hang, true);
            PhucLoiService.gI().guiDuLieu(pl);
        }
    }

    /**
     * Nhận mọi ô đang nhận được — cho nút Nhận nhanh.
     *
     * @param tong cộng dồn quà đã phát vào đây (id vật phẩm → số lượng)
     * @return {số ô nhận được, số ô còn chưa nhận được}
     */
    public int[] nhanTatCa(Player pl, Map<Integer, Integer> tong) {
        int duoc = 0;
        int conLai = 0;
        if (pl == null || !pl.isPl()) {
            return new int[]{0, 0};
        }
        synchronized (pl) {
            int mua = NroPassDAO.mua();
            NroPassDAO.TienDo td = NroPassDAO.tienDo(pl.id, mua);
            Set<Integer> daNhan = NroPassDAO.daNhan(pl.id, mua);
            int dat = capDat(td.diem);
            for (int cap = 1; cap <= dat; cap++) {
                for (int hang = 0; hang <= (td.caoCap ? 1 : 0); hang++) {
                    if (daNhan.contains(cap * 2 + hang) || NroPassDAO.dsQua(cap, hang).isEmpty()) {
                        continue;
                    }
                    if (nhanMotO(pl, mua, td, daNhan, cap, hang, false)) {
                        duoc++;
                        for (NroPassDAO.Qua q : NroPassDAO.dsQua(cap, hang)) {
                            tong.merge(q.itemId, q.soLuong, Integer::sum);
                        }
                    } else {
                        conLai++;
                    }
                }
            }
        }
        return new int[]{duoc, conLai};
    }

    /**
     * Một ô. Ghi cờ đã-nhận <b>trước</b> rồi mới phát: ghi sau mà phát lỗi
     * thì bấm lại nhận tiếp được, tức nhận vô hạn. Phát không được món nào
     * thì trả lại cờ.
     */
    private boolean nhanMotO(Player pl, int mua, NroPassDAO.TienDo td, Set<Integer> daNhan,
            int cap, int hang, boolean baoLoi) {
        if (cap < 1 || cap > NroPassDAO.soCap() || (hang != 0 && hang != 1)) {
            return false;
        }
        if (capDat(td.diem) < cap) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Chưa đạt cấp " + cap + ".");
            }
            return false;
        }
        if (hang == NroPassDAO.HANG_CAO_CAP && !td.caoCap) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Ô này thuộc hàng Cao cấp — mở khoá "
                        + NroPassDAO.ten() + " cao cấp để nhận.");
            }
            return false;
        }
        if (daNhan.contains(cap * 2 + hang)) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Ô này đã nhận rồi.");
            }
            return false;
        }
        List<NroPassDAO.Qua> qua = NroPassDAO.dsQua(cap, hang);
        if (qua.isEmpty()) {
            return false;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) < qua.size()) {
            Service.gI().sendThongBao(pl, "Hành trang cần trống " + qua.size() + " ô.");
            return false;
        }
        if (!NroPassDAO.ghiNhan(pl.id, mua, cap, hang)) {
            return false;
        }
        if (phatQua(pl, qua) == 0) {
            NroPassDAO.boNhan(pl.id, mua, cap, hang);
            return false;
        }
        daNhan.add(cap * 2 + hang);
        return true;
    }

    private int phatQua(Player pl, List<NroPassDAO.Qua> qua) {
        int duoc = 0;
        for (NroPassDAO.Qua q : qua) {
            try {
                Item it = ItemService.gI().createNewItem((short) q.itemId, q.soLuong);
                if (it == null || it.template == null) {
                    continue;
                }
                it.quantity = q.soLuong;
                ganChiSo(it, q.chiSo);
                InventoryService.gI().addItemBag(pl, it);
                duoc++;
            } catch (Exception ex) {
                Logger.logException(NroPassService.class, ex, "Lỗi phát quà NRO Pass item " + q.itemId);
            }
        }
        if (duoc > 0) {
            InventoryService.gI().sendItemBag(pl);
        }
        return duoc;
    }

    private static void ganChiSo(Item it, String chuoi) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            return;
        }
        for (String phan : chuoi.split(",")) {
            String[] kv = phan.split("=");
            if (kv.length != 2) {
                continue;
            }
            try {
                it.itemOptions.add(new ItemOption(Integer.parseInt(kv[0].trim()),
                        Integer.parseInt(kv[1].trim())));
            } catch (NumberFormatException sai) {
                // Dong hong thi bo dong do.
            }
        }
    }

    // =====================================================================
    //  Quản trị
    // =====================================================================
    /** Mở cao cấp mùa này cho một nhân vật (hỗ trợ nạp lỗi). */
    public String tangCaoCap(long playerId) {
        int mua = NroPassDAO.mua();
        if (!NroPassDAO.moCaoCap(playerId, mua)) {
            return "Không ghi được vào CSDL.";
        }
        Player p = nro.server.Client.gI().getPlayerByID(playerId);
        if (p != null) {
            synchronized (p) {
                p.THE_THANG = 2;
                p.LASTTIME_THE_THANG = NroPassDAO.hetMua();
            }
            capNhatChiSo(p);
            Service.gI().sendThongBao(p, "Bạn được tặng " + NroPassDAO.ten() + " cao cấp mùa " + mua + ".");
        }
        return "Đã mở cao cấp mùa " + mua + (p != null ? " (đang online, có hiệu lực ngay)" : "");
    }

    /** Cộng điểm pass cho một nhân vật. */
    public String tangDiem(long playerId, long diem) {
        NroPassDAO.congDiem(playerId, NroPassDAO.mua(), diem);
        return "Đã cộng " + Util.soCham(diem) + " điểm pass.";
    }

    // =====================================================================
    //  Gói tin
    // =====================================================================
    /**
     * Nối khối NRO Pass vào cuối gói phúc lợi.
     *
     * <pre>
     * byte  2
     * UTF   ten, moTaMua ("Mùa 1 · còn 23 ngày")
     * short soCap
     * int   diemMoiCap
     * long  diem
     * byte  caoCap
     * UTF   giaCaoCap, uuDaiCaoCap
     * soCap × 2 ô (cấp 1..soCap, hàng 0 rồi 1):
     *   byte trangThai  0 chưa tới · 1 nhận được · 2 đã nhận · 3 đã tới nhưng khoá
     *   byte soMon { short icon, int soLuong }
     * UTF   nguonDiem, soDu
     * </pre>
     */
    public void ghiVaoGoi(Player pl, Message msg) throws Exception {
        int mua = NroPassDAO.mua();
        NroPassDAO.TienDo td = NroPassDAO.tienDo(pl.id, mua);
        Set<Integer> daNhan = NroPassDAO.daNhan(pl.id, mua);
        int soCap = NroPassDAO.soCap();
        int dat = capDat(td.diem);
        long conMs = NroPassDAO.hetMua() - System.currentTimeMillis();
        long conNgay = Math.max(0, (conMs + NroPassDAO.MOT_NGAY_MS - 1) / NroPassDAO.MOT_NGAY_MS);

        msg.writer().writeByte(KHOI_PASS);
        msg.writer().writeUTF(NroPassDAO.ten());
        msg.writer().writeUTF("Mùa " + mua + " · còn " + conNgay + " ngày");
        msg.writer().writeShort(soCap);
        msg.writer().writeInt((int) Math.min(Integer.MAX_VALUE, NroPassDAO.diemMoiCap()));
        msg.writer().writeLong(td.diem);
        msg.writer().writeByte(td.caoCap ? 1 : 0);
        msg.writer().writeUTF(NroPassDAO.giaCaoCap());
        msg.writer().writeUTF(NroPassDAO.uuDaiCaoCap());
        for (int cap = 1; cap <= soCap; cap++) {
            for (int hang = 0; hang <= 1; hang++) {
                byte tt;
                if (cap > dat) {
                    tt = 0;
                } else if (daNhan.contains(cap * 2 + hang)) {
                    tt = 2;
                } else if (hang == NroPassDAO.HANG_CAO_CAP && !td.caoCap) {
                    tt = 3;
                } else {
                    tt = 1;
                }
                List<NroPassDAO.Qua> qua = NroPassDAO.dsQua(cap, hang);
                if (qua.isEmpty() && tt == 1) {
                    tt = 2; // o trong: coi nhu xong, khong treo nut nhan
                }
                msg.writer().writeByte(tt);
                int n = Math.min(TOI_DA_MON_MOT_O, qua.size());
                msg.writer().writeByte(n);
                for (int i = 0; i < n; i++) {
                    msg.writer().writeShort(PhucLoiService.gI().iconCua(qua.get(i).itemId));
                    msg.writer().writeInt(qua.get(i).soLuong);
                }
            }
        }
        StringBuilder nguon = new StringBuilder("Làm nhiệm vụ Bò Mộng (tối đa "
                + NroPassDAO.soNhiemVuMoiNgay() + "/ngày):");
        for (int i = 0; i < NroPassDAO.TEN_DO_KHO.length; i++) {
            nguon.append(i == 0 ? " " : " · ").append(NroPassDAO.TEN_DO_KHO[i]).append(" +")
                    .append(NroPassDAO.diemNhiemVu(i));
        }
        msg.writer().writeUTF(nguon.toString());
        long vnd = pl.getSession() == null ? 0 : pl.getSession().vnd;
        msg.writer().writeUTF("Số dư: " + Util.soCham(vnd) + " VNĐ · "
                + Util.soCham(pl.inventory.ruby) + " hồng ngọc · "
                + Util.soCham(pl.inventory.gem) + " ngọc");
    }
}
