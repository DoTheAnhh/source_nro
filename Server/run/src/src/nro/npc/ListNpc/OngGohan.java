package nro.npc.ListNpc;

/**
 * @author Văn Khải
 */
import nro.inventory.InventoryService;
import nro.services.Fun.Input;
import nro.services.NpcService;
import nro.services.Service;
import nro.services.TaskService;
import Utils.Util;
import consts.ConstDailyGift;
import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstTask;
import event.EventGuide;
import event.EventManager;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.List;
import jbcd.ConnectDB;
import models.Item.Item;
import models.Item.ItemService;
import nro.npc.Npc;
import nro.player.DailyGift.DailyGiftService;
import nro.player.Player;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (EventManager.LUNNAR_NEW_YEAR) {
            if (player.NhanLiXiForNPC_1 == 0) {
                String[] chucTetMessages = {
                    "Năm mới sẽ đem an lành thịnh vượng đến với bạn",
                    "Chúc bạn và gia đình có một năm mới hạnh phúc và thịnh vượng",
                    "Phát tài phát lộc",
                    "Vạn sự như ý",
                    "Chúc bạn năm mới vui vẻ, tiền vô như nước, tình duyên rực rỡ",
                    "Năm mới phát tài phát lộc, vạn sự như ý nha",
                    "Xuân sang may mắn tràn đầy, hạnh phúc ngập lối",
                    "Năm mới bình an, vạn sự hanh thông, luôn vui cười",
                    "Tết đến rồi, quẩy hết mình và tận hưởng từng khoảnh khắc nhé",
                    "Năm mới vui như Tết, giàu như mơ, đẹp hơn xưa",
                    "Chúc bạn hạnh phúc tràn đầy, may mắn ngập tràn",
                    "Năm mới rực rỡ như pháo hoa, tươi vui như hoa mai nở",
                    "Tết đến cười thật nhiều, sống thật chill, vui hết mình",
                    "Chúc mừng năm mới"
                };

                String message = chucTetMessages[Util.nextInt(0, chucTetMessages.length - 1)];
                createOtherMenu(player, ConstNpc.NHAN_LI_XI, message, "Ok", "Chúc Mừng\nNăm Mới", "Đóng");
                return;
            }
        }
        List<String> menu = DailyGiftService.checkDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY)
                ? List.of("Điểm Danh Hàng Ngày", "Chức Năng", "Hướng Dẫn Tân Thủ", "Đóng")// "Box Zalo",
                : List.of("Chức Năng", "Hướng Dẫn Tân Thủ", "Đóng");// "Box Zalo",

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

            String message = (TaskService.gI().getIdTask(player) >= ConstTask.TASK_11_0)
                    ? "Con cố gắng theo " + mentor + " học thành tài, đừng lo lắng cho ta."
                    : "Con cần ta giúp gì.";

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
            case ConstNpc.CONFIRM_ACTIVE:

                handleActivation(player, select);
                break;
            case ConstNpc.CONFIRM_THOIVANG:

                Nhanthoivang(player, select);
                break;
            case ConstNpc.MENU_MA_BAO_VE: {
                if (select == 0) {
                    Input.gI().createFormMBV(player);
                }
                break;
            }
        }
    }

    private void handleBaseMenu(Player player, int select) {
        switch (select) {
            case 0:
                if (DailyGiftService.checkDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY)) {
                    if (player.DIEM_DANH == 0) {
                        player.inventory.ruby += 50;
                        InventoryService.gI().addItemBag(player, ItemService.gI().createNewItem((short) 457, 10));
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().sendMoney(player);
                        player.DIEM_DANH++;
                        DailyGiftService.updateDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY);
                        Service.getInstance().sendThongBao(player, "Điểm danh thành công, bạn nhận được 10 thỏi vàng & 50 hồng ngọc");
                    } else {
                        Service.getInstance().sendThongBao(player, "Hôm nay bạn đã điểm danh rồi!");
                    }
                } else {
                    openSupportMenu(player);
                }
                break;
            case 1:
                if (DailyGiftService.checkDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY)) {
                    openSupportMenu(player);
                } else {
                    openTutorialMenu(player);
                }
                break;
            case 2:
                if (DailyGiftService.checkDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY)) {
                    openTutorialMenu(player);
                } else {
                    openZaloGroup(player);
                }
                break;
            case 3:
                if (DailyGiftService.checkDailyGift(player, ConstDailyGift.DIEM_DANH_HANG_NGAY)) {
                    openZaloGroup(player);
                }
                break;
            default:
                break;
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

            case 2:
                Input.gI().createFormGiftCode(player);
                break;
            case 3:
                Input.gI().createFormChangePassword(player);
                break;
            case 4:
                this.createOtherMenu(player, ConstNpc.ONG_GIA_MENU_3, "|0|Menu Skip Nhiệm Vụ\n\n|2|Mời Quý Khách Lựa Chọn!\n",
                        "Skip Nhiệm Vụ Heo Rừng", "Skip Nhiệm Vụ Bulon", "Skip Nhiệm Vụ\n Thách đấu", "Skip Nhiệm Vụ Đại Hội Võ Thuật", "Skip Nhiệm Vụ Trung Uý Trắng", "Đóng");
                break;
            case 5: // Mở menu xác nhận kích hoạt
                String trangThai = player.getSession().actived ? "ĐÃ MỞ THÀNH VIÊN" : "CHƯA MỞ THÀNH VIÊN";
                DecimalFormat df = new DecimalFormat("#,###");

                String soDu = df.format(player.getSession().vnd);

                this.createOtherMenu(player, ConstNpc.CONFIRM_ACTIVE,
                        "|2|Trạng thái: " + trangThai + "\n"
                        + "|2|Số dư: " + soDu + " VND\n"
                        + "Bạn hay nạp lần đầu để được mở thành viên nhé",
                        "Oke");
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
            case 3:
                if (EventManager.LUNNAR_NEW_YEAR) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_TET;
                } else if (EventManager.CHRISTMAS) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_NOEL;
                } else if (EventManager.VU_LAN_FESTIVAL) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_VU_LAN;
                } else if (EventManager.HALLOWEEN) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_HALLOWEEN;
                } else if (EventManager.INTERNATIONAL_WOMANS_DAY) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_QUOC_TE_83;
                } else if (EventManager.TRUNG_THU) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_TRUNG_THU;
                } else if (EventManager.HUNG_VUONG) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_HUNG_VUONG;
                } else if (EventManager.BLACK_FRIDAY) {
                    tutorialId = EventGuide.HUONG_DAN_SU_KIEN_BLACK_FRIDAY;
                } else {
                    tutorialId = "Đang Update...";
                }
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
                case 0: // Skip Heo Rừng (nhánh 13)
                    if (taskId == 13 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Heo Rừng";
                    }
                    break;

                case 1: // Skip Bulon (nhánh 15)
                    if (taskId == 15 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Bulon";
                    }
                    break;
                case 2: // Skip Đại Hội Võ Thuật (nhánh 18)
                    if (taskId == 16 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Thách đấu 10 người";
                    }
                    break;

                case 3: // Skip Đại Hội Võ Thuật (nhánh 18)
                    if (taskId == 18 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Đại Hội Võ Thuật";
                    }
                    break;

                case 4: // Skip Trung Úy Trắng (nhánh 19)
                    if (taskId == 19 && taskIndex < player.playerTask.taskMain.subTasks.size() - 1) {
                        canSkip = true;
                        taskName = "Trung Úy Trắng";
                    }
                    break;

                case 5: // Đóng
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

    private void handleActivation(Player player, int select) {
//        if (select == 0) {
//            // Nếu đã mở rồi thì báo luôn
//            if (player.getSession().actived) {
//                Service.gI().sendThongBao(player, "Bạn đã mở Thành Viên trước đó rồi!");
//                return;
//            }

//            // Nếu chưa mở thì kiểm tra tiền
//            if (player.getSession().vnd >= 10_000) {
//                try {
//                    player.getSession().vnd -= 10_000;
//                    ConnectDB.executeUpdate(
//                            "UPDATE account SET active = ?, vnd = ? WHERE id = ?",
//                            1, player.getSession().vnd, player.account_id);
//
//                    player.getSession().actived = true;
//
//                    Service.gI().sendThongBaoOK(player,
//                            "Mở Thành Viên thành công! Bạn hãy đăng nhập lại sau 5 giây.");
//                    player.iDMark.setLastTimeBan(System.currentTimeMillis());
//                    player.iDMark.setBan(true); // kick user
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Service.getInstance().sendThongBao(player,
//                            "Lỗi mở Thành Viên: " + e.getMessage());
//                }
//            } else {
//                Service.getInstance().sendThongBao(player,
//                        "Bạn không đủ 10K VND để mở Thành Viên!");
//            }
//            try {
//                ConnectDB.executeUpdate(
//                        "UPDATE account SET active = 1 WHERE id = ?",
//                        player.account_id);
//
//                player.getSession().actived = true;
//
//                Service.gI().sendThongBaoOK(player,
//                        "Mở Thành Viên thành công! Bạn hãy đăng nhập lại sau 5 giây.");
//                player.iDMark.setLastTimeBan(System.currentTimeMillis());
//                player.iDMark.setBan(true); // kick user
//            } catch (Exception e) {
//                e.printStackTrace();
//                Service.getInstance().sendThongBao(player,
//                        "Lỗi mở Thành Viên: " + e.getMessage());
//            }
        // } else 
        if (select == 0) {
            Service.gI().sendThongBao(player, "Bạn Hãy nạp trên WEB: ngocrongdark.online nhé!");
        }
    }

    private void openSupportMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.ONG_GIA_MENU_1, "|7|Chức Năng Hỗ Trợ\n|2|Xin Mời Quý Khách Lựa Chọn!",
                "Nhận Ngọc Xanh", "Nhận Thỏi vàng", "Nhập GiftCode", "Đổi \nMật Khẩu", "Skip Nhiệm Vụ", "Mở Thành Viên", "Đóng");
    }

    private void openTutorialMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.ONG_GIA_MENU_2, "|7| Hướng Dẫn Tân Thủ\n|2|Mời Người Chơi Chọn!",
                "Cách Kiếm Vật Phẩm", "Cách Kiếm Đồ", "Thông Tin Boss", "Thông Tin\nSự Kiện", "Đóng");
    }

    private void openZaloGroup(Player player) {
        try {
            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|2|Inbox Vào Zalo: 0867395776\n"
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

                // Tạo thỏi vàng và thêm vào túi
                Item thoiVang = ItemService.gI().createNewItem((short) 457, thoivang);
                InventoryService.gI().addItemBag(player, thoiVang);
                InventoryService.gI().sendItemBag(player);

                // Cập nhật DB: set thỏi vàng về 0
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

                // Set session về 0 để đồng bộ
                player.getSession().goldBar = 0;

                // Gửi thông báo
                Service.gI().sendThongBao(player, "Bạn đã nhận được x" + Util.format(thoivang) + " Thỏi Vàng!");
                Service.gI().sendMoney(player);

                break;
            }

            case 1: {
                Service.gI().sendThongBao(player, "Đã thoát.");
                break;
            }

            default: {
                Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ!");
                break;
            }
        }
    }

}
