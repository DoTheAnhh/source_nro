package nro.service.intrinsic;

import nro.entity.intrinsic.Intrinsic;
import nro.core.consts.ConstNpc;
import nro.entity.player.Player;
import nro.server.Manager;
import nro.net.io.Message;
import nro.core.util.Util;
import java.util.List;
import nro.repository.dao.ConfigDAO;
import nro.service.NpcService;
import nro.service.PlayerService;
import nro.service.Service;

/**
 * Nội tại — kỹ năng bị động, mở lại là bốc ngẫu nhiên một cái khác.
 *
 * <h2>Bảng riêng thay cho menu NPC</h2>
 *
 * <p>Trước đây tất cả nằm trong một menu bốn nút của NPC ({@link #showMenu}), và
 * chỗ duy nhất vào được là NPC ngoài bản đồ. Nay có một bảng riêng do client vẽ
 * ({@code NoiTaiUI}), mở từ tab Kỹ năng, lấy dữ liệu qua {@link #guiBangNoiTai}.
 * Menu NPC cũ vẫn giữ để đường vào cũ không gãy.</p>
 *
 * <h2>Chỉ nội tại của ĐÚNG hành tinh</h2>
 *
 * <p>{@link #getIntrinsics} lọc theo {@code player.gender}, và mọi đường mở đều
 * bốc trong đúng danh sách ấy. Không có đường nào trao được nội tại của hành
 * tinh khác.</p>
 */
public class IntrinsicService {

    private static IntrinsicService I;

    /**
     * Giá vàng mỗi lần mở, đơn vị <b>triệu</b>, tăng dần theo số lần đã mở.
     */
    private static final int[] COST_OPEN = {10, 20, 40, 80, 160, 320, 640, 1280};

    /** Sức mạnh tối thiểu mới mở được nội tại. */
    private static final long SUC_MANH_TOI_THIEU = 10_000_000_000L;

    /**
     * Số lần bốc nhiều nhất trong <b>một lần bấm</b> "mở nhanh".
     *
     * <p>Vòng lặp này chạy ngay trong luồng xử lý gói tin, nên nó chặn cả khu
     * bản đồ trong lúc chạy. Một nghìn lượt bốc là gần như tức thì, nhưng cứ để
     * trần cho chắc: người chơi có hàng vạn ngọc bấm một cái thì không được
     * phép treo máy chủ. Hết trần mà chưa được thì bấm lại.</p>
     */
    private static final int TOI_DA_BOC_MOT_LUOT = 1000;

    public static IntrinsicService gI() {
        if (IntrinsicService.I == null) {
            IntrinsicService.I = new IntrinsicService();
        }
        return IntrinsicService.I;
    }

    public List<Intrinsic> getIntrinsics(byte playerGender) {
        switch (playerGender) {
            case 0:
                return Manager.INTRINSIC_TD;
            case 1:
                return Manager.INTRINSIC_NM;
            default:
                return Manager.INTRINSIC_XD;
        }
    }

    public Intrinsic getIntrinsicById(int id) {
        for (Intrinsic intrinsic : Manager.INTRINSICS) {
            if (intrinsic.id == id) {
                return new Intrinsic(intrinsic);
            }
        }
        return null;
    }

    public void sendInfoIntrinsic(Player player) {
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.playerIntrinsic.intrinsic.icon);
            msg.writer().writeUTF(player.playerIntrinsic.intrinsic.getName());
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showAllIntrinsic(Player player) {
        List<Intrinsic> listIntrinsic = getIntrinsics(player.gender);
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(1);
            msg.writer().writeByte(1); //count tab
            msg.writer().writeUTF("Nội tại");
            msg.writer().writeByte(listIntrinsic.size() - 1);
            for (int i = 1; i < listIntrinsic.size(); i++) {
                msg.writer().writeShort(listIntrinsic.get(i).icon);
                msg.writer().writeUTF(listIntrinsic.get(i).getDescription());
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showMenu(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.INTRINSIC, -1,
                "Nội tại là một kỹ năng bị động hỗ trợ đặc biệt\nBạn có muốn mở hoặc thay đổi nội tại không?",
                "Xem\ntất cả\nNội Tại", "Mở\nNội Tại", "Mở VIP", "Từ chối");
    }

    public void showConfirmOpen(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC, -1, "Bạn muốn đổi Nội Tại khác\nvới giá là "
                + COST_OPEN[player.playerIntrinsic.countOpen] + " Tr vàng ?", "Mở\nNội Tại", "Từ chối");
    }

    public void showConfirmOpenVip(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC_VIP, -1,
                "Bạn có muốn mở Nội Tại VIP\nvới giá là 200 triệu vàng\nvà tái lập giá vàng quay lại ban đầu không?",
                "Mở\nNội VIP", "Từ chối");
    }

    // =====================================================================
    //  Bốc một nội tại mới
    // =====================================================================

    /**
     * Bốc một nội tại ngẫu nhiên <b>trong danh sách của hành tinh này</b>.
     *
     * <p>Bắt đầu từ chỉ số 1: chỉ số 0 là "chưa có nội tại", không phải một nội
     * tại để bốc trúng.</p>
     */
    private void bocMoi(Player player) {
        List<Intrinsic> ds = getIntrinsics(player.gender);
        if (ds == null || ds.size() < 2) {
            return;
        }
        Intrinsic moi = new Intrinsic(ds.get(Util.nextInt(1, ds.size() - 1)));
        moi.param1 = (short) Util.nextInt(moi.paramFrom1, moi.paramTo1);
        moi.param2 = (short) Util.nextInt(moi.paramFrom2, moi.paramTo2);
        player.playerIntrinsic.intrinsic = moi;
        nro.service.badges.BadgesTaskService.tangTheoLoai(player,
                nro.entity.badges.BadgesTaskTemplate.MO_NOI_TAI, moi.id, 1);
    }

    private void changeIntrinsic(Player player) {
        bocMoi(player);
        baoNoiTaiMoi(player);
        sendInfoIntrinsic(player);
    }

    private void baoNoiTaiMoi(Player player) {
        String ten = player.playerIntrinsic.intrinsic.getName();
        int k = ten.indexOf(" [");
        Service.gI().sendThongBao(player, "Bạn nhận được Nội tại:\n"
                + (k > 0 ? ten.substring(0, k) : ten));
    }

    public void DoiNoiTai(Player player) {
        changeIntrinsic(player);
    }

    // =====================================================================
    //  Mở bằng vàng
    // =====================================================================

    public long giaVang(Player player) {
        int i = player.playerIntrinsic.countOpen;
        if (i < 0) {
            i = 0;
        }
        if (i >= COST_OPEN.length) {
            i = COST_OPEN.length - 1;
        }
        return COST_OPEN[i] * 1_000_000L;
    }

    public void open(Player player) {
        if (player.nPoint.power < SUC_MANH_TOI_THIEU) {
            Service.gI().sendThongBao(player, "Cần sức mạnh tối thiểu "
                    + Util.soCham(SUC_MANH_TOI_THIEU) + ", đang có "
                    + Util.soCham(player.nPoint.power) + ".");
            return;
        }
        long can = giaVang(player);
        if (player.inventory.gold < can) {
            Service.gI().sendThongBao(player, "Không đủ vàng, còn thiếu "
                    + Util.soCham(can - player.inventory.gold) + " vàng.");
            return;
        }
        player.inventory.gold -= can;
        PlayerService.gI().sendInfoHpMpMoney(player);
        changeIntrinsic(player);
        if (player.playerIntrinsic.countOpen < COST_OPEN.length - 1) {
            player.playerIntrinsic.countOpen++;
        }
        guiBangNoiTai(player);
    }

    public void openVip(Player player) {
        if (player.nPoint.power < SUC_MANH_TOI_THIEU) {
            Service.gI().sendThongBao(player, "Cần sức mạnh tối thiểu "
                    + Util.soCham(SUC_MANH_TOI_THIEU) + ".");
            return;
        }
        long can = 200_000_000L;
        if (player.inventory.gold < can) {
            Service.gI().sendThongBao(player, "Không đủ vàng, còn thiếu "
                    + Util.soCham(can - player.inventory.gold) + " vàng.");
            return;
        }
        player.inventory.gold -= can;
        PlayerService.gI().sendInfoHpMpMoney(player);
        changeIntrinsic(player);
        player.playerIntrinsic.countOpen = 0;
        guiBangNoiTai(player);
    }

    // =====================================================================
    //  Mở bằng ngọc
    // =====================================================================

    /** Số ngọc cho một lần mở. Giá <b>cố định</b>, không tăng dần như vàng. */
    public int giaNgoc() {
        long g = ConfigDAO.num(ConfigDAO.NOI_TAI_GIA_NGOC, 50L);
        if (g < 1) {
            g = 1;
        }
        return (int) Math.min(g, Integer.MAX_VALUE);
    }

    public void moBangNgoc(Player player) {
        if (player.nPoint.power < SUC_MANH_TOI_THIEU) {
            Service.gI().sendThongBao(player, "Cần sức mạnh tối thiểu "
                    + Util.soCham(SUC_MANH_TOI_THIEU) + ".");
            return;
        }
        int can = giaNgoc();
        if (player.inventory.gem < can) {
            Service.gI().sendThongBao(player, "Không đủ ngọc, cần " + can
                    + ", đang có " + player.inventory.gem + ".");
            return;
        }
        player.inventory.gem -= can;
        PlayerService.gI().sendInfoHpMpMoney(player);
        Service.gI().sendMoney(player);
        changeIntrinsic(player);
        guiBangNoiTai(player);
    }

    // =====================================================================
    //  Mở nhanh: bốc bằng ngọc tới khi ra thứ mình muốn
    // =====================================================================

    /**
     * Bốc liên tục bằng ngọc cho tới khi ra <b>đúng nội tại</b> đã chọn với chỉ
     * số từ {@code chiSoMuon} trở lên, hoặc tới khi hết ngọc.
     *
     * <h2>Dừng ở đâu</h2>
     *
     * <ul>
     *   <li>Ra đúng nội tại đã chọn và {@code param1 >= chiSoMuon} — xong.</li>
     *   <li>Hết ngọc — dừng, và nói còn thiếu bao nhiêu.</li>
     *   <li>Chạm trần {@link #TOI_DA_BOC_MOT_LUOT} — dừng, bấm lại thì chạy
     *       tiếp. Trần này để một lần bấm không treo cả khu bản đồ.</li>
     * </ul>
     *
     * <p><b>Gộp gói tin trong suốt vòng lặp.</b> Mỗi lần bốc bản thường gửi lại
     * số tiền và một dòng thông báo; một nghìn lượt là hai nghìn gói bắn liên
     * tiếp — client ngập rồi rớt. Xem {@code Service.batGomGoi}.</p>
     *
     * @param idMuon    id nội tại muốn có
     * @param chiSoMuon chỉ số tối thiểu chấp nhận; {@code <= 0} là chỉ cần đúng
     *                  nội tại, chỉ số bao nhiêu cũng được
     */
    public void moNhanh(Player player, int idMuon, int chiSoMuon) {
        if (player == null || player.playerIntrinsic == null) {
            return;
        }
        if (player.nPoint.power < SUC_MANH_TOI_THIEU) {
            Service.gI().sendThongBao(player, "Cần sức mạnh tối thiểu "
                    + Util.soCham(SUC_MANH_TOI_THIEU) + ".");
            return;
        }
        Intrinsic muon = null;
        for (Intrinsic x : getIntrinsics(player.gender)) {
            if (x.id == idMuon) {
                muon = x;
                break;
            }
        }
        if (muon == null || muon.id == 0) {
            Service.gI().sendThongBao(player,
                    "Nội tại đó không có ở hành tinh của bạn.");
            return;
        }
        // Doi chi so cao hon TRAN cua chinh noi tai do thi khong bao gio dat —
        // noi ngay thay vi de nguoi choi dot het ngoc.
        if (chiSoMuon > muon.paramTo1) {
            Service.gI().sendThongBao(player, "Nội tại này cao nhất chỉ tới "
                    + muon.paramTo1 + ", không thể đạt " + chiSoMuon + ".");
            return;
        }
        int gia = giaNgoc();
        if (player.inventory.gem < gia) {
            Service.gI().sendThongBao(player, "Không đủ ngọc, cần " + gia
                    + " cho một lần, đang có " + player.inventory.gem + ".");
            return;
        }

        int lan = 0;
        boolean duoc = false;
        Service.gI().batGomGoi(player);
        try {
            while (lan < TOI_DA_BOC_MOT_LUOT && player.inventory.gem >= gia) {
                player.inventory.gem -= gia;
                lan++;
                bocMoi(player);
                Intrinsic nt = player.playerIntrinsic.intrinsic;
                if (nt != null && nt.id == idMuon
                        && (chiSoMuon <= 0 || nt.param1 >= chiSoMuon)) {
                    duoc = true;
                    break;
                }
            }
        } finally {
            Service.gI().xaGomGoi(player);
        }

        PlayerService.gI().sendInfoHpMpMoney(player);
        Service.gI().sendMoney(player);
        sendInfoIntrinsic(player);
        guiBangNoiTai(player);

        int daTieu = lan * gia;
        if (duoc) {
            Service.gI().sendThongBao(player, "Đã ra " + muon.name.split(" ")[0]
                    + " sau " + lan + " lần, tốn " + Util.soCham(daTieu)
                    + " ngọc.\nCòn " + player.inventory.gem + " ngọc.");
        } else if (player.inventory.gem < gia) {
            Service.gI().sendThongBao(player, "Hết ngọc sau " + lan
                    + " lần bốc (tốn " + Util.soCham(daTieu) + " ngọc). Chưa ra thứ cần.");
        } else {
            Service.gI().sendThongBao(player, "Đã bốc " + lan
                    + " lần chưa ra, bấm lại để chạy tiếp.\nCòn "
                    + player.inventory.gem + " ngọc.");
        }
    }

    // =====================================================================
    //  Gói dữ liệu cho bảng nội tại của client
    // =====================================================================

    /** Mã việc của gói 112 chiều xuống, dành riêng cho bảng nội tại mới. */
    public static final byte XUONG_BANG_NOI_TAI = 5;

    /**
     * Gửi toàn bộ dữ liệu bảng nội tại.
     *
     * <p>Một gói duy nhất chứa đủ mọi thứ bảng cần vẽ, nên bảng không phải hỏi
     * thêm lần nào và không có trạng thái nửa vời.</p>
     *
     * <p>Thứ tự ghi — client đọc đúng thứ tự này:</p>
     * <ol>
     *   <li>byte mã việc (5)</li>
     *   <li>UTF tên nội tại đang có, short icon</li>
     *   <li>long giá vàng, int giá ngọc, long vàng đang có, int ngọc đang có</li>
     *   <li>byte số nội tại, rồi mỗi cái: byte id, short icon, UTF tên,
     *       short chỉ số thấp nhất, short chỉ số cao nhất</li>
     * </ol>
     */
    public void guiBangNoiTai(Player player) {
        if (player == null || player.playerIntrinsic == null) {
            return;
        }
        List<Intrinsic> ds = getIntrinsics(player.gender);
        Message msg = null;
        try {
            msg = new Message(112);
            msg.writer().writeByte(XUONG_BANG_NOI_TAI);

            Intrinsic dangCo = player.playerIntrinsic.intrinsic;
            msg.writer().writeUTF(dangCo == null ? "Chưa có nội tại" : dangCo.getName());
            msg.writer().writeShort(dangCo == null ? -1 : dangCo.icon);

            msg.writer().writeLong(giaVang(player));
            msg.writer().writeInt(giaNgoc());
            msg.writer().writeLong(player.inventory.gold);
            msg.writer().writeInt(player.inventory.gem);

            int n = (ds == null) ? 0 : Math.max(0, ds.size() - 1);
            if (n > 127) {
                n = 127;      // so luong ghi bang mot byte
            }
            msg.writer().writeByte(n);
            for (int i = 1; i <= n; i++) {
                Intrinsic x = ds.get(i);
                msg.writer().writeByte(x.id);
                msg.writer().writeShort(x.icon);
                msg.writer().writeUTF(x.getDescription());
                msg.writer().writeShort(x.paramFrom1);
                msg.writer().writeShort(x.paramTo1);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            nro.core.log.Logger.logException(IntrinsicService.class, e,
                    "Lỗi gửi bảng nội tại");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
