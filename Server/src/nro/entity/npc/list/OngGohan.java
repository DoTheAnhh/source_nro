
package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */
import nro.service.inventory.InventoryService;
import nro.service.fun.Input;
import nro.service.NpcService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstDailyGift;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstTask;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.List;
import nro.repository.ConnectDB;
import nro.repository.dao.PlayerDAO;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.entity.npc.Npc;
import nro.service.player.dailygift.DailyGiftService;
import nro.entity.player.Player;
import nro.service.DetuService;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }

        clearExpiredVe(player);


        boolean hasDaily = DailyGiftService.checkDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY);

        // Nhãn hợp thể viết tiếng Việt và mang theo trạng thái hiện tại.
        //
        // Trước là "On/Off Fusion": vừa là tiếng Anh giữa một danh sách tiếng
        // Việt, vừa không cho biết đang bật hay tắt — bấm vào rồi đọc thông báo
        // mới biết vừa đổi sang gì. Ghi sẵn trạng thái thì nhìn menu là biết.
        String nhanHopThe = (player.hienThiHopThe == 1)
                ? "Ẩn\nhợp thể" : "Hiện\nhợp thể";

        // Ô "hào quang riêng" CHỈ hiện với người được trao.
        //
        // Ai không được trao mà thấy ô này thì bấm vào chẳng đổi gì — một ô
        // chết nằm giữa menu. Hàm mucSauHopThe dồn số ô theo đúng điều kiện
        // này, nên hai chỗ luôn khớp nhau.
        //
        // Nhãn mang theo trạng thái hiện tại, giống hệt ô hợp thể: nhìn menu là
        // biết đang bật hay tắt, không phải bấm thử.
        java.util.List<String> menu = new java.util.ArrayList<>();
        if (hasDaily) {
            menu.add("Điểm Danh Hàng Ngày");
        }
        menu.add("Chức Năng");
        menu.add("Hướng Dẫn Tân Thủ");
        menu.add("Nhận\nĐệ tử");
        menu.add(nhanHopThe);
        if (coAuraRieng(player)) {
            menu.add((player.hienThiAuraRieng == 1)
                    ? "Ẩn\nhào quang" : "Hiện\nhào quang");
        }
        menu.add("Mua\nvé tháng");
        menu.add("Mua\nvé tuần");
        menu.add("Đóng");

        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            if (player.baovetaikhoan) {
                this.createOtherMenu(player, ConstNpc.MENU_MA_BAO_VE,
                        "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại",
                        "Quên Mã Bảo Vệ", "Đóng");
                return;
            }

            String mentor;
            switch (player.gender) {
                case ConstPlayer.TRAI_DAT:
                    mentor = "Quy lão Kamê";
                    break;
                case ConstPlayer.NAMEC:
                    mentor = "Trưởng lão Guru";
                    break;
                default:
                    mentor = "Vua Vegeta";
                    break;
            }

            String baseMessage = (TaskService.gI().getIdTask(player) >= ConstTask.TASK_11_0)
                    ? "Con cố gắng theo " + mentor + " học thành tài, đừng lo lắng cho ta."
                    : "Con cần ta giúp gì.";

            String message = baseMessage
                    + "\n\n" + getThongTinVe(player)
                    + "\n\n|5|Vé tháng: 100.000 VND - 30 ngày"
                    + "\n|6|+50 thỏi vàng điểm danh, x4 rơi thỏi vàng"
                    + "\n|5|Vé tuần: 30.000 VND - 7 ngày"
                    + "\n|6|+20 thỏi vàng điểm danh, x2 rơi thỏi vàng";

            this.createOtherMenu(player, ConstNpc.BASE_MENU, message, menu.toArray(new String[0]));
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        switch (player.iDMark.getIndexMenu()) {
            case ConstNpc.NHAN_LI_XI:
                switch (select) {
                    case 1:
                        Item Lixi = ItemService.gI().createNewItem((short) 1760, 1);
                        String[] chucTetMessages = {
                            "Năm mới sẽ đem an lành thịnh vượng đến với bạn",
                            "Chúc bạn và gia đình có một năm mới hạnh phúc và thịnh vượng",
                            "Chúc bạn năm mới vui vẻ, tiền vô như nước, tình duyên rực rỡ",
                            "Năm mới phát tài phát lộc, vạn sự như ý nha",
                            "Xuân sang may mắn tràn đầy, hạnh phúc ngập lối",
                            "Năm mới bình an, vạn sự hanh thông, luôn vui cười",
                            "Tết đến rồi, quẩy hết mình và tận hưởng từng khoảnh khắc nhé",
                            "Năm mới vui như Tết, giàu như mơ, đẹp hơn xưa",
                            "Chúc bạn hạnh phúc tràn đầy, may mắn ngập tràn",
                            "Năm mới rực rỡ như pháo hoa, tươi vui như hoa mai nở",
                            "Tết đến cười thật nhiều, sống thật chill, vui hết mình"
                        };

                        String NpcChat = chucTetMessages[Util.nextInt(0, chucTetMessages.length - 1)];
                        String PlayerChat = chucTetMessages[Util.nextInt(0, chucTetMessages.length - 1)];
                        this.npcChat(player, NpcChat);
                        Service.gI().chat(player, PlayerChat);
                        player.NhanLiXiForNPC_1++;
                        if (Util.isTrue(60, 100)) {
                            Lixi.addOptionParam(30, 0);
                            Lixi.addOptionParam(93, 30);
                            InventoryService.gI().addItemBag(player, Lixi);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được " + Lixi.template.name);
                        } else {
                            Service.gI().sendThongBao(player, "(>_<)");
                        }
                        break;
                }
                break;

            case ConstNpc.BASE_MENU:
                handleBaseMenu(player, select);
                break;

            case ConstNpc.ONG_GIA_MENU_1:
                handleSupportMenu(player, select);
                break;

            case ConstNpc.ONG_GIA_MENU_2:
                handleTutorialMenu(player, select);
                break;

            case ConstNpc.ONG_GIA_MENU_3:
                handleSkipQuestMenu(player, select);
                break;

            case ConstNpc.CONFIRM_THOIVANG:
                Nhanthoivang(player, select);
                break;

            case ConstNpc.NHAN_DE_TU:
                Nhandetu(player, select);
                break;

            case ConstNpc.MENU_MA_BAO_VE:
                if (select == 0) {
                    Input.gI().createFormMBV(player);
                }
                break;

            case ConstNpc.QUY_DOI_HN:
                switch (select) {
                    case 0:
                        Item thoiVang = InventoryService.gI().findItemBag(player, 457);
                        if (thoiVang == null || thoiVang.quantity <= 0) {
                            Service.gI().sendThongBao(player, "Bạn không có Thỏi vàng nào để quy đổi!");
                            return;
                        }
                        Input.gI().createFormDoiNgocHong(player);
                        break;

                    case 1:
                        Service.gI().sendThongBao(player, "Hẹn gặp lại con!");
                        break;
                }
                break;

            case ConstNpc.MTVFREE:
                if (select == 0) {
                    if (!player.getSession().actived) {
                        boolean quaNhiemVu = (player.playerTask != null
                                && player.playerTask.taskMain != null
                                && player.playerTask.taskMain.id >= 25);

                        final int CAN_VND = 20_000;
                        boolean duVnd = player.getSession().tongnap >= CAN_VND;

                        if (quaNhiemVu || duVnd) {
                            player.getSession().actived = true;
                            PlayerDAO.MuaThanhVien(player, 0);
                            Service.gI().sendMoney(player);
                            npcChat(player, "Mở thành viên FREE thành công!");
                        } else {
                            npcChat(player, "Chưa đủ điều kiện! Cần hoàn thành nhiệm vụ hoặc Nạp lần đầu 20K VND.");
                        }
                    } else {
                        npcChat(player, "Bạn đã mở rồi!");
                    }
                    return;
                }
                break;

            case ConstNpc.CONFIRM_BUY_VE_THANG:
                buyVeThang(player, select);
                break;

            case ConstNpc.CONFIRM_BUY_VE_TUAN:
                buyVeTuan(player, select);
                break;
        }
    }

    private void handleBaseMenu(Player player, int select) {
        clearExpiredVe(player);
        boolean hasDaily = DailyGiftService.checkDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY);

        if (hasDaily) {
            switch (select) {
                case 0:
                    if (player.DIEM_DANH == 0) {
                        int soThoiVang = 10;
                        if (hasVeThang(player)) {
                            soThoiVang = 50;
                        } else if (hasVeTuan(player)) {
                            soThoiVang = 20;
                        }

                        player.inventory.ruby += 50;
                        Item item = ItemService.gI().createNewItem((short) 457, soThoiVang);
                        item.addOptionParam(30, 0);
                        InventoryService.gI().addItemBag(player, item);
                        InventoryService.gI().sendItemBag(player);

                        Service.gI().sendMoney(player);
                        player.DIEM_DANH++;
                        DailyGiftService.updateDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY);
                        Service.getInstance().sendThongBao(player,
                                "Điểm danh thành công, bạn nhận được " + soThoiVang + " thỏi vàng & 50 hồng ngọc");
                    } else {
                        Service.getInstance().sendThongBao(player, "Hôm nay bạn đã điểm danh rồi!");
                    }
                    break;

                case 1:
                    openSupportMenu(player);
                    break;

                case 2:
                    openTutorialMenu(player);
                    break;

                case 3:
                    this.createOtherMenu(player, ConstNpc.NHAN_DE_TU,
                            "Con có muốn nhận đệ tử thường không?",
                            "Nhận đệ tử", "Đóng");
                    break;

                case 4:
                    doiHienThiHopThe(player);
                    break;

                default:
                    // Từ ô hợp thể trở xuống, số ô lệch theo hai điều kiện —
                    // xử lý chung ở dưới thay vì chép hai bản.
                    mucSauHopThe(player, select);
                    break;
            }
        } else {
            switch (select) {
                case 0:
                    openSupportMenu(player);
                    break;

                case 1:
                    openTutorialMenu(player);
                    break;

                case 2:
                    this.createOtherMenu(player, ConstNpc.NHAN_DE_TU,
                            "Con có muốn nhận đệ tử thường không?",
                            "Nhận đệ tử", "Đóng");
                    break;

                case 3:
                    doiHienThiHopThe(player);
                    break;

                default:
                    mucSauHopThe(player, select + 1);
                    break;
            }
        }
    }

    /**
     * Các ô nằm SAU ô hợp thể, tính theo thang chung.
     *
     * <h2>Vì sao phải quy về một thang</h2>
     *
     * <p>Danh sách ô của NPC này co giãn theo <b>hai</b> điều kiện: có điểm danh
     * hay chưa, và người chơi có hào quang được trao riêng hay không. Bản cũ đã
     * chép hai bản {@code switch} cho riêng điều kiện thứ nhất, mỗi bản một
     * thang số — thêm điều kiện thứ hai nữa là thành bốn bản, và chỉ cần sót một
     * chỗ là "Mua vé tháng" biến thành "Ẩn hào quang".</p>
     *
     * <p>Quy về một thang thì mỗi mục chỉ khai một lần.</p>
     *
     * @param muc số ô đã quy về thang CÓ điểm danh:
     *            {@code 5} hào quang riêng, {@code 6} vé tháng, {@code 7} vé tuần
     */
    private void mucSauHopThe(Player player, int muc) {
        // Không có hào quang riêng thì ô đó không được vẽ ra, nên mọi ô phía sau
        // dồn lên một bậc — cộng lại đúng một bậc để về thang chuẩn.
        if (!coAuraRieng(player) && muc >= 5) {
            muc++;
        }
        switch (muc) {
            case 5:
                doiHienThiAuraRieng(player);
                break;
            case 6:
                showMenuVeThang(player);
                break;
            case 7:
                showMenuVeTuan(player);
                break;
            default:
                break;
        }
    }

    /** Người chơi có được trao hào quang riêng không. */
    private static boolean coAuraRieng(Player player) {
        return nro.repository.dao.AuraDAO.auraRiengCua(player.id) > 0;
    }

    private void doiHienThiHopThe(Player player) {
        player.hienThiHopThe = (player.hienThiHopThe == 0 ? 1 : 0);
        // Ghi xuong CSDL NGAY, khong doi luc luu nhan vat.
        //
        // Truoc day day chi la mot bien trong bo nho, mac dinh 1. Nguoi choi tat
        // di, may chu dung lai mot cai la hop the hien lai — ma ho khong he bam
        // gi.
        nro.repository.dao.CaiDatNguoiChoiDAO.ghiHienThiHopThe(
                player.account_id, player.hienThiHopThe);
        Service.getInstance().sendThongBao(player,
                player.hienThiHopThe == 1 ? "Đã bật hiển thị hợp thể" : "Đã tắt hiển thị hợp thể");
        Service.getInstance().Send_Caitrang(player);
    }

    /**
     * Bật/tắt hào quang được trao riêng.
     *
     * <p>Ghi xuống CSDL ngay, cùng lý do với ẩn/hiện hợp thể: để trong bộ nhớ
     * thôi thì máy chủ dựng lại một cái là bật trở lại, mà người chơi không hề
     * bấm gì.</p>
     */
    private void doiHienThiAuraRieng(Player player) {
        player.hienThiAuraRieng = (player.hienThiAuraRieng == 0 ? 1 : 0);
        nro.repository.dao.CaiDatNguoiChoiDAO.ghiHienThiAuraRieng(
                player.account_id, player.hienThiAuraRieng);
        Service.getInstance().sendThongBao(player,
                player.hienThiAuraRieng == 1
                        ? "Đã bật hào quang riêng" : "Đã tắt hào quang riêng");
        // Đẩy lại hình dáng NGAY để người xung quanh thấy đổi liền, giống hệt
        // đường của hợp thể.
        Service.getInstance().Send_Caitrang(player);
        nro.service.Service.gI().capNhatAura(player);
    }

    private void showMenuVeThang(Player player) {
        clearExpiredVe(player);

        String trangThai;
        if (hasVeThang(player)) {
            long conLai = getConLaiNgay(player.getSession().vethangExpire);
            trangThai = "\n|1|Bạn đang có vé tháng, còn " + conLai + " ngày";
        } else if (hasVeTuan(player)) {
            long conLai = getConLaiNgay(player.getSession().vetuanExpire);
            trangThai = "\n|1|Bạn đang có vé tuần, còn " + conLai + " ngày";
        } else {
            trangThai = "\n|7|Hiện tại bạn chưa có vé nào";
        }

        this.createOtherMenu(player, ConstNpc.CONFIRM_BUY_VE_THANG,
                "|7|MUA VÉ THÁNG"
                + "\n|2|Giá: 100,000 VND"
                + "\n|6|Thời hạn: 30 ngày"
                + "\n|6|Điểm danh mỗi ngày: + 50 thỏi vàng"
                + "\n|6|Quyền lợi thêm: x4 rơi thỏi vàng"
                + "\n|5|Chỉ mua lại khi vé hiện tại đã hết hạn"
                + trangThai,
                "Mua", "Đóng");
    }

    private void showMenuVeTuan(Player player) {
        clearExpiredVe(player);

        String trangThai;
        if (hasVeTuan(player)) {
            long conLai = getConLaiNgay(player.getSession().vetuanExpire);
            trangThai = "\n|1|Bạn đang có vé tuần, còn " + conLai + " ngày";
        } else if (hasVeThang(player)) {
            long conLai = getConLaiNgay(player.getSession().vethangExpire);
            trangThai = "\n|1|Bạn đang có vé tháng, còn " + conLai + " ngày";
        } else {
            trangThai = "\n|7|Hiện tại bạn chưa có vé nào";
        }

        this.createOtherMenu(player, ConstNpc.CONFIRM_BUY_VE_TUAN,
                "|7|MUA VÉ TUẦN"
                + "\n|2|Giá: 30,000 VND"
                + "\n|6|Thời hạn: 7 ngày"
                + "\n|6|Điểm danh mỗi ngày: + 20 thỏi vàng"
                + "\n|6|Quyền lợi thêm: x2 rơi thỏi vàng"
                + "\n|5|Chỉ mua lại khi vé hiện tại đã hết hạn"
                + trangThai,
                "Mua", "Đóng");
    }

    private boolean hasVeThang(Player player) {
        return player.getSession().vethang == 1
                && player.getSession().vethangExpire > System.currentTimeMillis();
    }

    private boolean hasVeTuan(Player player) {
        return player.getSession().vetuan == 1
                && player.getSession().vetuanExpire > System.currentTimeMillis();
    }

    private boolean hasAnyActiveVe(Player player) {
        return hasVeThang(player) || hasVeTuan(player);
    }

    private void clearExpiredVe(Player player) {
        long now = System.currentTimeMillis();
        boolean needUpdate = false;

        if (player.getSession().vethang == 1 && player.getSession().vethangExpire <= now) {
            player.getSession().vethang = 0;
            player.getSession().vethangExpire = 0;
            needUpdate = true;
        }

        if (player.getSession().vetuan == 1 && player.getSession().vetuanExpire <= now) {
            player.getSession().vetuan = 0;
            player.getSession().vetuanExpire = 0;
            needUpdate = true;
        }

        if (needUpdate) {
            try {
                ConnectDB.executeUpdate(
                        "UPDATE account SET vethang = ?, vethang_expire = ?, vetuan = ?, vetuan_expire = ? WHERE id = ?",
                        player.getSession().vethang,
                        player.getSession().vethangExpire,
                        player.getSession().vetuan,
                        player.getSession().vetuanExpire,
                        player.account_id
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private long getConLaiNgay(long expire) {
        long remain = expire - System.currentTimeMillis();
        if (remain <= 0) {
            return 0;
        }
        long days = remain / (24 * 60 * 60 * 1000);
        if (remain % (24 * 60 * 60 * 1000) != 0) {
            days++;
        }
        return days;
    }

    private String getThongTinVe(Player player) {
        clearExpiredVe(player);

        if (hasVeThang(player)) {
            return "|2|Vé tháng: còn " + getConLaiNgay(player.getSession().vethangExpire) + " ngày"
                    + "\n|6|Quyền lợi: điểm danh +50 thỏi vàng, x4 rơi thỏi vàng";
        } else if (hasVeTuan(player)) {
            return "|2|Vé tuần: còn " + getConLaiNgay(player.getSession().vetuanExpire) + " ngày"
                    + "\n|6|Quyền lợi: điểm danh +20 thỏi vàng, x2 rơi thỏi vàng";
        } else {
            return "|7|Bạn chưa có vé tuần / vé tháng";
        }
    }

    private void buyVeThang(Player player, int select) {
        if (select != 0) {
            return;
        }

        clearExpiredVe(player);

        try {
            if (hasAnyActiveVe(player)) {
                if (hasVeThang(player)) {
                    Service.gI().sendThongBao(player, "Bạn đang có vé tháng, chỉ mua lại khi vé hết hạn!");
                } else {
                    Service.gI().sendThongBao(player, "Bạn đang có vé tuần, chỉ mua vé tháng khi vé tuần hết hạn!");
                }
                return;
            }

            // Gia va so ngay lay tu panel_config -> admin sua tren panel la
            // ca hai duong (nguoi choi tu mua, admin cap tay) doi theo cung luc.
            final int giaVe = (int) nro.repository.dao.ConfigDAO.num(
                    nro.repository.dao.ConfigDAO.VE_THANG_GIA);
            final long soNgay = nro.repository.dao.ConfigDAO.num(
                    nro.repository.dao.ConfigDAO.VE_THANG_NGAY);
            long expire = System.currentTimeMillis() + soNgay * 86_400_000L;

            if (player.getSession().vnd < giaVe) {
                Service.gI().sendThongBao(player, "Bạn không đủ " + Util.format(giaVe)
                        + " VND để mua vé tháng!");
                return;
            }

            player.getSession().vnd -= giaVe;
            player.getSession().vethang = 1;
            player.getSession().vetuan = 0;
            player.getSession().vethangExpire = expire;
            player.getSession().vetuanExpire = 0;

            ConnectDB.executeUpdate(
                    "UPDATE account SET vnd = ?, vethang = 1, vethang_expire = ?, vetuan = 0, vetuan_expire = 0 WHERE id = ?",
                    player.getSession().vnd,
                    expire,
                    player.account_id
            );

            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player,
                    "Mua vé tháng thành công! Vé có hiệu lực 30 ngày, điểm danh mỗi ngày nhận 50 thỏi vàng và x4 rơi thỏi vàng.");
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi xảy ra khi mua vé tháng!");
        }
    }

    private void buyVeTuan(Player player, int select) {
        if (select != 0) {
            return;
        }

        clearExpiredVe(player);

        try {
            if (hasAnyActiveVe(player)) {
                if (hasVeTuan(player)) {
                    Service.gI().sendThongBao(player, "Bạn đang có vé tuần, chỉ mua lại khi vé hết hạn!");
                } else {
                    Service.gI().sendThongBao(player, "Bạn đang có vé tháng, chỉ mua vé tuần khi vé tháng hết hạn!");
                }
                return;
            }

            final int giaVe = (int) nro.repository.dao.ConfigDAO.num(
                    nro.repository.dao.ConfigDAO.VE_TUAN_GIA);
            final long soNgay = nro.repository.dao.ConfigDAO.num(
                    nro.repository.dao.ConfigDAO.VE_TUAN_NGAY);
            long expire = System.currentTimeMillis() + soNgay * 86_400_000L;

            if (player.getSession().vnd < giaVe) {
                Service.gI().sendThongBao(player, "Bạn không đủ " + Util.format(giaVe)
                        + " VND để mua vé tuần!");
                return;
            }

            player.getSession().vnd -= giaVe;
            player.getSession().vetuan = 1;
            player.getSession().vethang = 0;
            player.getSession().vetuanExpire = expire;
            player.getSession().vethangExpire = 0;

            ConnectDB.executeUpdate(
                    "UPDATE account SET vnd = ?, vetuan = 1, vetuan_expire = ?, vethang = 0, vethang_expire = 0 WHERE id = ?",
                    player.getSession().vnd,
                    expire,
                    player.account_id
            );

            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player,
                    "Mua vé tuần thành công! Vé có hiệu lực 7 ngày, điểm danh mỗi ngày nhận 20 thỏi vàng và x2 rơi thỏi vàng.");
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi xảy ra khi mua vé tuần!");
        }
    }

    private void Nhandetu(Player player, int select) {
        switch (select) {
            case 0:
                if (player.Detu == null) {
                    DetuService.gI().createNormalPet(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn đã có đệ tử rồi!");
                }
                break;
        }
    }

    /**
     * Bảng chỉ số đầy đủ của nhân vật, gửi dưới dạng thông báo.
     *
     * <h3>Vì sao là thông báo chứ không phải một khung riêng</h3>
     *
     * <p>Khung thông tin nhân vật ở góc trên bên trái do <b>client tự vẽ</b> —
     * máy chủ chỉ gửi con số, không điều khiển được bố cục hay thêm nút. Muốn
     * một khung như bản NroTino thì phải dựng lại client. Bảng này đưa đúng
     * những con số đó ra bằng đường máy chủ điều khiển được.</p>
     *
     * <p>Chỉ liệt kê các chỉ số <b>thực sự có tác dụng</b> trong mã nguồn này;
     * chỉ số nào đang bằng 0 thì bỏ qua cho gọn, trừ nhóm cơ bản luôn hiện.</p>
     */
    private static String bangChiSoChiTiet(Player pl) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        nro.entity.player.NPoint p = pl.nPoint;
        StringBuilder sb = new StringBuilder();
        sb.append("|7|[ CHỈ SỐ CHI TIẾT ]").append("\n").append("\n");
        sb.append("|0|Máu: ").append(df.format(p.hp)).append(" / ")
                .append(df.format(p.hpMax)).append("\n");
        sb.append("|0|Ki: ").append(df.format(p.mp)).append(" / ")
                .append(df.format(p.mpMax)).append("\n");
        sb.append("|0|Sức đánh: ").append(df.format(p.dame)).append("\n");
        sb.append("|0|Giáp: ").append(df.format(p.def)).append("\n");
        sb.append("|0|Chí mạng: ").append(p.crit).append("%\n");
        sb.append("|0|Sát thương chí mạng: ").append(p.tlSDCM).append("%\n");
        sb.append("|0|Né đòn: ").append(p.tlNeDon).append("%\n");
        sb.append("|0|Chính xác: ").append(p.tlchinhxac).append("%\n");
        sb.append("\n");

        sb.append("|2|[ Xuyên giáp & phản đòn ]\n");
        them(sb, "Xuyên giáp", p.tlxgc, "%");
        them(sb, "Xuyên giáp khi chí mạng", p.tlxgcc, "%");
        them(sb, "Phản sát thương", p.tlPST, "%");
        them(sb, "Giảm sát thương nhận", p.tlGiap, "%");
        them(sb, "Giảm thời gian bị choáng", p.tlFixStun, "%");
        sb.append("\n");

        sb.append("|2|[ Hút & hồi ]\n");
        them(sb, "Hút HP", p.tlHutHp, "%");
        them(sb, "Hút Ki", p.tlHutMp, "%");
        them(sb, "HP hồi mỗi 30 giây", p.tlHpHoi, "%");
        them(sb, "Ki hồi mỗi 30 giây", p.tlMpHoi, "%");
        sb.append("\n");

        sb.append("|2|[ Khác ]\n");
        them(sb, "May mắn", p.tlMayman, "%");
        them(sb, "Vàng rơi thêm", p.tlGold, "%");
        them(sb, "Sát thương Tự sát", p.tlBom, "%");
        sb.append("|3|Giới hạn sức mạnh: bậc ").append(p.limitPower).append("\n");
        return sb.toString();
    }

    /** Thêm một dòng chỉ số, bỏ qua nếu đang bằng 0 cho bảng đỡ dài. */
    private static void them(StringBuilder sb, String ten, long v, String duoi) {
        if (v != 0) {
            sb.append("|0|").append(ten).append(": ").append(v).append(duoi).append("\n");
        }
    }

    private void handleSupportMenu(Player player, int select) {
        switch (select) {
            case 0: {
                if (player.inventory.gem >= 2_000_000) {
                    Service.getInstance().sendThongBao(player, "Đớp ít thôi con!");
                    return;
                }
                player.inventory.gem += 2_000_000;
                Service.gI().sendMoney(player);
                Service.gI().sendThongBao(player, "Bạn nhận được 2 củ ngọc xanh.");
                break;
            }
            case 1: {
                int thoivang = player.getSession().goldBar;
                this.createOtherMenu(player, ConstNpc.CONFIRM_THOIVANG,
                        "|2|Bạn có : " + thoivang + " Thỏi vàng\n",
                        "Nhận", "Từ Chối");
                break;
            }
            case 2: {
                // Muc moi: doi VND sang Thoi Vang. Ty le va han muc lay tu panel.
                if (!nro.repository.dao.ConfigDAO.on(
                        nro.repository.dao.ConfigDAO.DOI_TV_BAT)) {
                    Service.gI().sendThongBao(player, "Chức năng đổi Thỏi Vàng đang tạm đóng.");
                    return;
                }
                Input.gI().createFormDoiThoiVang(player);
                break;
            }

            case 3:
                this.createOtherMenu(player, ConstNpc.MTVFREE,
                        "|7|Mở thành viên FREE"
                        + "\n|6|Yêu cầu hoàn thành nhiệm vụ ADR 19"
                        + "\n|1|OR NẠP lần đầu sẽ được mở FREE",
                        "Đồng ý", "Từ chối");
                break;

            case 4:
                Input.gI().createFormGiftCode(player);
                break;

            case 5:
                Input.gI().createFormChangePassword(player);
                break;

            case 6:
                this.createOtherMenu(player, ConstNpc.ONG_GIA_MENU_3,
                        "|0|Menu Skip Nhiệm Vụ\n\n|2|Mời Quý Khách Lựa Chọn!\n",
                        "Skip Nhiệm Vụ Heo Rừng", "Skip Nhiệm Vụ Bulon", "Skip Nhiệm Vụ\n Thách đấu",
                        "Skip Nhiệm Vụ Đại Hội Võ Thuật", "Skip Nhiệm Vụ Trung Uý Trắng", "Đóng");
                break;

            case 7:
                try {
                    DecimalFormat df = new DecimalFormat("#,###");

                    boolean isActive = player.getSession().actived;
                    String trangThai = isActive ? "§2ĐÃ MỞ THÀNH VIÊN" : "§cCHƯA MỞ THÀNH VIÊN";

                    String soDu = df.format(player.getSession().vnd);
                    String tongNap = df.format(player.getSession().tongnap);

                    StringBuilder sb = new StringBuilder();
                    sb.append("|2|------ THÔNG TIN THÀNH VIÊN ------\n");
                    sb.append("|7|Trạng thái: ").append(trangThai).append("\n");
                    sb.append("|7|Số dư hiện có: ").append(soDu).append(" VND\n");
                    sb.append("|7|Tổng nạp: ").append(tongNap).append(" VND\n");

                    if (!isActive) {
                        sb.append("|1|Hãy nạp lần đầu để mở quyền thành viên!");
                    } else {
                        sb.append("|2|Bạn đã là thành viên! Hãy tận hưởng các quyền lợi đặc biệt.");
                    }

                    this.createOtherMenu(player, ConstNpc.CONFIRM_ACTIVE, sb.toString(), "Đóng");

                } catch (Exception e) {
                    Service.gI().sendThongBao(player, "Lỗi hiển thị thông tin thành viên!");
                    e.printStackTrace();
                }
                break;

            case 8:
                Service.gI().sendThongBaoFromAdmin(player, bangChiSoChiTiet(player));
                break;
        }
    }

    private void handleTutorialMenu(Player player, int select) {
        String tutorialId;
        switch (select) {
            case 0:
                tutorialId = ConstNpc.HUONG_DAN_TANTHU;
                break;
            case 1:
                tutorialId = ConstNpc.HUONG_DAN_TANTHU2;
                break;
            case 2:
                tutorialId = ConstNpc.HUONG_DAN_TANTHU3;
                break;
            default:
                tutorialId = "";
                break;
        }
        if (tutorialId != null) {
            NpcService.gI().createTutorial(player, this.avartar, tutorialId);
        }
    }

    private void handleSkipQuestMenu(Player player, int select) {
        try {
            if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
                Service.gI().sendThongBao(player, "Bạn hiện không có nhiệm vụ nào để bỏ qua.");
                return;
            }

            int taskId = player.playerTask.taskMain.id;
            int taskIndex = player.playerTask.taskMain.index;

            boolean canSkip = false;
            String taskName = "";

            switch (select) {
                case 0:
                    if (taskId == 13 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Heo Rừng";
                    }
                    break;

                case 1:
                    if (taskId == 15 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Bulon";
                    }
                    break;

                case 2:
                    if (taskId == 16 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Thách đấu 10 người";
                    }
                    break;

                case 3:
                    if (taskId == 18 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Đại Hội Võ Thuật";
                    }
                    break;

                case 4:
                    if (taskId == 19 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Trung Úy Trắng";
                    }
                    break;

                case 5:
                    Service.gI().sendThongBao(player, "Đã đóng menu Skip Nhiệm Vụ.");
                    return;

                default:
                    Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ.");
                    return;
            }

            if (canSkip) {
                TaskService.gI().sendNextTaskMain(player);
                Service.gI().sendThongBaoOK(player, "Bạn đã skip nhiệm vụ " + taskName + " thành công!");
            } else {
                Service.gI().sendThongBao(player, "Không thể skip nhiệm vụ này (đã ở cuối nhánh hoặc không thuộc nhánh).");
            }

        } catch (Exception e) {
            Service.gI().sendThongBao(player, "Lỗi skip nhiệm vụ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openSupportMenu(Player player) {
        // Tỉ lệ hiện ngay trên nút để người chơi biết trước khi bấm.
        long moi1k = nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.DOI_TV_MOI_1K);
        // Ti le de trong loi thoai chu khong de tren nut: nut ba dong se cao hon
        // cac nut khac va lam vo hang menu cua client.
        this.createOtherMenu(player, ConstNpc.ONG_GIA_MENU_1,
                "|7|Chức Năng Hỗ Trợ\n|2|Xin Mời Quý Khách Lựa Chọn!"
                + "\n|6|Đổi VND sang Thỏi Vàng: 1.000 VND = " + moi1k + " TV",
                "Nhận Ngọc Xanh", "Nhận Thỏi Vàng", "Đổi VND\nsang TV",
                "MTV FREE", "Nhập GiftCode", "Đổi \nMật Khẩu", "Skip Nhiệm Vụ", "Thông tin",
                "Chỉ số\nchi tiết", "Đóng");
    }

    private void openTutorialMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.ONG_GIA_MENU_2, "|7| Hướng Dẫn Tân Thủ\n|2|Mời Người Chơi Chọn!",
                "Cách Kiếm Vật Phẩm", "Cách Kiếm Đồ", "Thông Tin Boss", "Thông Tin\nSự Kiện", "Đóng");
    }

    private void openZaloGroup(Player player) {
        try {
            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|2|Inbox Vào Zalo: 0373713573\n"
                    + "|2|Nhắn Cho ADMIN Để Được Hỗ Trợ Nhé!",
                    "Ok");
            Desktop.getDesktop().browse(new URI("https://zalo.me/g/iumbtl736"));
        } catch (IOException | URISyntaxException e) {
            Service.getInstance().sendThongBao(player, "Lỗi mở Zalo: " + e.getMessage());
        }
    }

    private void Nhanthoivang(Player player, int select) {
        switch (select) {
            case 0: {
                int thoivang = player.getSession().goldBar;

                if (thoivang <= 0) {
                    Service.gI().sendThongBao(player, "Bạn không có thỏi vàng nào để nhận!");
                    return;
                }
                Item thoiVang = ItemService.gI().createNewItem((short) 457, thoivang);
                InventoryService.gI().addItemBag(player, thoiVang);
                InventoryService.gI().sendItemBag(player);
                try {
                    ConnectDB.executeUpdate(
                            "UPDATE account SET thoi_vang = 0 WHERE id = ?",
                            player.account_id
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    Service.gI().sendThongBao(player, "Lỗi khi cập nhật dữ liệu: " + e.getMessage());
                    return;
                }
                player.getSession().goldBar = 0;
                Service.gI().sendThongBao(player, "Bạn đã nhận được x" + Util.format(thoivang) + " Thỏi Vàng!");
                Service.gI().sendMoney(player);
                break;
            }

            case 1:
                Service.gI().sendThongBao(player, "Đã thoát.");
                break;

            default:
                Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ!");
                break;
        }
    }
}