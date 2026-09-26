package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */
import nro.service.inventory.InventoryService;
import nro.service.fun.ChangeMapService;
import nro.service.fun.Input;
import nro.service.NpcService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.FormatStyle;
import nro.core.log.Logger;
import nro.core.util.SkillUtil;
import nro.core.util.TimeUtil;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import java.util.ArrayList;
import nro.repository.dao.PlayerDAO;
import nro.entity.item.Item;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;
import nro.entity.clan.Clan;
import nro.entity.map.treasureundersea.TreasureUnderSea;
import nro.service.map.treasureundersea.TreasureUnderSeaService;
import nro.entity.npc.Npc;
import static nro.service.npc.NpcFactory.PLAYERID_OBJECT;
import nro.entity.player.Player;
import nro.server.Manager;
import nro.service.shop.ShopService;
import nro.entity.skill.Skill;

public class QuyLaoKame extends Npc {

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    /**
     * Nhãn mục giải tán bang, dùng cho cả lúc dựng menu và lúc đọc lựa chọn.
     *
     * <p>Một hằng số chứ không viết chuỗi ở hai chỗ: lệch một dấu cách là phần
     * đọc lựa chọn không nhận ra mục nào, và bấm vào sẽ không có gì xảy ra.</p>
     */
    private static final String NHAN_GIAI_TAN = "Giải tán\nBang hội";

    /** Nhãn mục làm mới hồi chiêu — cùng lý do một hằng số như trên. */
    private static final String NHAN_RESET_HOI_CHIEU = "Làm mới\nhồi chiêu";

    /** Vật phẩm trả cho một lần làm mới hồi chiêu. */
    private static final short ID_THOI_VANG = 457;

    /** Nhãn mục đổi hành tinh. */
    private static final String NHAN_DOI_HANH_TINH = "Đổi\nhành tinh";

    /** Giá đổi hành tinh (Thỏi Vàng). */
    private static final int GIA_DOI_HANH_TINH = 500;

    /** Menu chọn hành tinh mới; menu xác nhận = MENU_XAC_NHAN_HT + hành tinh mới (0/1/2). */
    private static final int MENU_CHON_HT = 9101;
    private static final int MENU_XAC_NHAN_HT = 9110;

    private static final String[] TEN_HANH_TINH = {"Trái Đất", "Namếc", "Xayda"};

    /**
     * Chín kỹ năng của từng hành tinh, CÙNG THỨ TỰ (như lúc tạo nhân vật) — kỹ năng thứ i
     * đổi sang kỹ năng thứ i của hành tinh mới, giữ nguyên cấp.
     */
    private static final int[][] KY_NANG_HT = {
        {0, 1, 6, 9, 10, 20, 22, 19, 24},
        {2, 3, 7, 11, 12, 17, 18, 19, 26},
        {4, 5, 8, 13, 14, 21, 23, 19, 25}};

    /** Ba kiểu tóc mặc định của từng hành tinh (như màn tạo nhân vật). */
    private static final int[][] TOC_HT = {{64, 30, 31}, {9, 29, 32}, {6, 27, 28}};

    private static int viTri(int[] a, int v) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == v) {
                return i;
            }
        }
        return -1;
    }

    private static long demThoiVang(Player player) {
        long co = 0;
        for (Item it : player.inventory.itemsBag) {
            if (it != null && it.isNotNullItem() && it.template.id == ID_THOI_VANG) {
                co += it.quantity;
            }
        }
        return co;
    }

    private static boolean conMacDo(Player player) {
        for (Item it : player.inventory.itemsBody) {
            if (it != null && it.isNotNullItem()) {
                return true;
            }
        }
        return false;
    }

    /** Mở chọn hành tinh: phải tháo hết trang bị trước. */
    private void moDoiHanhTinh(Player player) {
        if (conMacDo(player)) {
            Service.gI().sendThongBao(player, "Con hãy tháo hết trang bị đang mặc ra trước rồi mới đổi hành tinh.");
            return;
        }
        String[] chon = new String[3];
        int k = 0;
        for (int g = 0; g < 3; g++) {
            if (g != player.gender) {
                chon[k++] = TEN_HANH_TINH[g];
            }
        }
        chon[2] = "Thôi";
        createOtherMenu(player, MENU_CHON_HT,
                "Con muốn chuyển sang hành tinh nào?\nGiá " + GIA_DOI_HANH_TINH + " Thỏi Vàng.\n"
                + "Giữ nguyên sức mạnh, tiềm năng, chỉ số, đệ tử, thú cưng, hành trang, rương đồ, sổ sưu tầm, skin; "
                + "kỹ năng đổi sang kỹ năng tương ứng (giữ cấp); nội tại về chưa có.", chon);
    }

    /**
     * Đổi hành tinh. Giữ nguyên mọi thứ khác (đệ tử, thú cưng, hành trang, rương, sổ sưu tầm,
     * skin kỹ năng, sức mạnh, tiềm năng, chỉ số gốc); đổi: hành tinh, tóc (kiểu tương ứng),
     * chín kỹ năng (sang kỹ năng cùng vị trí của hành tinh mới, giữ cấp), ô kỹ năng ngoài màn
     * hình; nội tại về chưa có. Lưu rồi cho đăng nhập lại để client nạp đúng hành tinh mới.
     */
    private void doiHanhTinh(Player player, int moi) {
        int cu = player.gender;
        if (moi < 0 || moi > 2 || moi == cu || cu < 0 || cu > 2) {
            return;
        }
        if (conMacDo(player)) {
            Service.gI().sendThongBao(player, "Con hãy tháo hết trang bị đang mặc ra trước rồi mới đổi hành tinh.");
            return;
        }
        if (demThoiVang(player) < GIA_DOI_HANH_TINH) {
            Service.gI().sendThongBao(player, "Cần " + GIA_DOI_HANH_TINH + " Thỏi Vàng để đổi hành tinh.");
            return;
        }
        int can = GIA_DOI_HANH_TINH;
        for (Item it : new ArrayList<>(player.inventory.itemsBag)) {
            if (can <= 0) {
                break;
            }
            if (it != null && it.isNotNullItem() && it.template.id == ID_THOI_VANG) {
                int bot = Math.min(can, it.quantity);
                InventoryService.gI().subQuantityItemsBag(player, it, bot);
                can -= bot;
            }
        }
        InventoryService.gI().sendItemBag(player);

        // Ky nang: vi tri thu i -> ky nang thu i cua hanh tinh moi, giu cap.
        java.util.List<nro.entity.skill.Skill> dsMoi = new ArrayList<>();
        int chonCu = -1;
        for (int i = 0; i < player.playerSkill.skills.size(); i++) {
            nro.entity.skill.Skill sk = player.playerSkill.skills.get(i);
            if (sk == player.playerSkill.skillSelect) {
                chonCu = i;
            }
            int vt = sk == null || sk.template == null ? -1 : viTri(KY_NANG_HT[cu], sk.template.id);
            if (vt < 0) {
                dsMoi.add(sk);
                continue;
            }
            int tplMoi = KY_NANG_HT[moi][vt];
            nro.entity.skill.Skill s2 = sk.point > 0 ? SkillUtil.createSkill(tplMoi, sk.point) : null;
            if (s2 == null) {
                s2 = SkillUtil.createSkillLevel0(tplMoi);
            }
            s2.currLevel = sk.currLevel;
            s2.lastTimeUseThisSkill = 0;
            dsMoi.add(s2);
        }
        player.playerSkill.skills.clear();
        player.playerSkill.skills.addAll(dsMoi);
        if (!dsMoi.isEmpty()) {
            player.playerSkill.skillSelect = dsMoi.get(chonCu >= 0 && chonCu < dsMoi.size() ? chonCu : 0);
        }
        // O ky nang ngoai man hinh: doi theo.
        byte[] oTat = player.playerSkill.skillShortCut;
        for (int i = 0; i < oTat.length; i++) {
            int vt = viTri(KY_NANG_HT[cu], oTat[i]);
            if (vt >= 0) {
                oTat[i] = (byte) KY_NANG_HT[moi][vt];
            }
        }
        // Noi tai ve chua co.
        nro.entity.intrinsic.Intrinsic khong = nro.service.intrinsic.IntrinsicService.gI().getIntrinsicById(0);
        if (khong != null) {
            khong.param1 = 0;
            khong.param2 = 0;
            player.playerIntrinsic.intrinsic = khong;
        }
        player.effectSkill.isIntrinsic = false;
        // Toc: kieu cung vi tri cua hanh tinh moi.
        int vtToc = viTri(TOC_HT[cu], player.head);
        player.head = (short) TOC_HT[moi][vtToc >= 0 ? vtToc : 0];
        player.gender = (byte) moi;
        Service.gI().addBoughtSkillAttack(player);
        try {
            nro.repository.ConnectDB.executeUpdate("UPDATE player SET gender = ?, head = ? WHERE id = ?",
                    moi, player.head, player.id);
        } catch (Exception ex) {
            Logger.logException(QuyLaoKame.class, ex, "Không lưu được hành tinh mới");
        }
        Logger.info("DOI_HANH_TINH", player.name + " " + TEN_HANH_TINH[cu] + " -> " + TEN_HANH_TINH[moi]);
        Service.gI().sendThongBao(player, "Đã chuyển sang hành tinh " + TEN_HANH_TINH[moi]
                + ". Con sẽ được đăng nhập lại để hoàn tất.");
        final Player nguoi = player;
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            nro.server.Client.gI().kickSession(nguoi.getSession());
        }, "doi-hanh-tinh").start();
    }

    /**
     * Dựng danh sách mục của menu gốc.
     *
     * <p>Tách riêng vì <b>phần đọc lựa chọn cũng cần đúng danh sách này</b>. Menu
     * có mấy mục điều kiện (đổi quà sự kiện, giao Rùa con, giải tán bang) nên chỉ
     * số của mục cuối đổi theo tình trạng người chơi; đối chiếu theo nhãn lấy từ
     * chính danh sách này thì không bao giờ chọn lệch một mục.</p>
     */
    private ArrayList<String> dungMenuGoc(Player player) {
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        ArrayList<String> menu = new ArrayList<>();
        menu.add("Nói\nchuyện");
        menu.add("Sự kiện VIP");
        // Mục này chỉ hiện khi có sự kiện đang chạy VÀ sự kiện đó có khai
        // công thức đổi. Sự kiện tắt là mục biến mất, không để lại nút chết.
        boolean coDoiSuKien =
                !nro.service.event.SuKienService.congThucDangCo().isEmpty();
        if (coDoiSuKien) {
            menu.add("Đổi quà\nsự kiện");
        }
        if (ruacon != null && ruacon.quantity >= 1) {
            menu.add("Giao\nRùa con");
        }
        // Giải tán bang ngay ở MENU GỐC.
        //
        // Mục này vẫn còn trong menu con "Nói chuyện", nhưng ở đó nó nằm hai lớp
        // sâu, cạnh "Nhiệm vụ" và "Học kỹ năng" — chủ bang gần như không tìm ra.
        menu.add(NHAN_RESET_HOI_CHIEU);
        menu.add(NHAN_DOI_HANH_TINH);
        if (player.clan != null && player.clan.isLeader(player)) {
            menu.add(NHAN_GIAI_TAN);
        }
        return menu;
    }


    /**
     * Làm mới hồi chiêu của <b>toàn bộ</b> kỹ năng, giá một Thỏi Vàng.
     *
     * <p>Việc gửi cho client và đặt lại mốc đã có sẵn ở
     * {@code Service.releaseCooldownSkill} — chỗ này chỉ thu tiền rồi gọi nó.</p>
     *
     * <p>Phải tự tay xoá {@code mocSanSang} trước: chiêu đánh liên tục (đấm,
     * chưởng) chạy theo mốc đó chứ không theo {@code lastTimeUseThisSkill}, mà
     * hàm kia chỉ lùi cái thứ hai. Quên nó thì bấm xong thấy Thỏi Vàng mất mà
     * đấm vẫn phải chờ.</p>
     */
    private void lamMoiHoiChieu(Player player) {
        if (player == null || player.playerSkill == null
                || player.playerSkill.skills == null) {
            return;
        }
        Item tv = InventoryService.gI().findItemBag(player, ID_THOI_VANG);
        if (tv == null || tv.quantity < 1) {
            Service.gI().sendThongBao(player,
                    "Cần 1 Thỏi Vàng để làm mới hồi chiêu.");
            return;
        }
        for (nro.entity.skill.Skill sk : player.playerSkill.skills) {
            if (sk != null) {
                sk.mocSanSang = 0;
            }
        }
        InventoryService.gI().subQuantityItemsBag(player, tv, 1);
        InventoryService.gI().sendItemBag(player);
        Service.gI().releaseCooldownSkill(player);
        Service.gI().sendThongBao(player,
                "Đã làm mới hồi chiêu toàn bộ kỹ năng.");
    }

    @Override
    public void openBaseMenu(Player player) {
        Service.gI().addBoughtSkillAttack(player);
        if (canOpenNpc(player)) {
            String[] menus = dungMenuGoc(player).toArray(new String[0]);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        Item ThiepChucTet1 = InventoryService.gI().findItemBag(player, 1191);
        Item ThiepChucTet2 = InventoryService.gI().findItemBag(player, 1192);
        Item ThiepChucTet3 = InventoryService.gI().findItemBag(player, 1193);
        int HongNgoc;
        Item ThoiVang = ItemService.gI().createNewItem((short) 457);
        if (canOpenNpc(player)) {
            switch (player.iDMark.getIndexMenu()) {
                case MENU_CHON_HT: {
                    // Hai hanh tinh con lai, theo thu tu 0-1-2 bo hanh tinh hien tai.
                    int k = 0;
                    for (int g = 0; g < 3; g++) {
                        if (g == player.gender) {
                            continue;
                        }
                        if (k == select) {
                            createOtherMenu(player, MENU_XAC_NHAN_HT + g,
                                    "Xác nhận đổi sang hành tinh " + TEN_HANH_TINH[g] + " với giá " + GIA_DOI_HANH_TINH
                                    + " Thỏi Vàng?\n(Nội tại sẽ về chưa có, kỹ năng đổi sang kỹ năng tương ứng giữ nguyên cấp)",
                                    "Đồng ý", "Từ chối");
                            break;
                        }
                        k++;
                    }
                    break;
                }
                case MENU_XAC_NHAN_HT:
                case MENU_XAC_NHAN_HT + 1:
                case MENU_XAC_NHAN_HT + 2: {
                    if (select == 0) {
                        doiHanhTinh(player, player.iDMark.getIndexMenu() - MENU_XAC_NHAN_HT);
                    }
                    break;
                }
                case ConstNpc.BASE_MENU: {
                    // Mục giải tán đối chiếu theo NHÃN, không theo chỉ số.
                    //
                    // Nó là mục cuối và chỉ có với chủ bang, nên chỉ số của nó đổi
                    // theo việc có "Đổi quà sự kiện" hay "Giao Rùa con" hay không.
                    ArrayList<String> menuGoc = dungMenuGoc(player);
                    if (select >= 0 && select < menuGoc.size()
                            && NHAN_RESET_HOI_CHIEU.equals(menuGoc.get(select))) {
                        lamMoiHoiChieu(player);
                        break;
                    }
                    if (select >= 0 && select < menuGoc.size()
                            && NHAN_DOI_HANH_TINH.equals(menuGoc.get(select))) {
                        moDoiHanhTinh(player);
                        break;
                    }
                    if (select >= 0 && select < menuGoc.size()
                            && NHAN_GIAI_TAN.equals(menuGoc.get(select))) {
                        Clan bang = player.clan;
                        if (bang != null && bang.isLeader(player)) {
                            createOtherMenu(player, 3,
                                    "Con có chắc muốn giải tán bang hội không?",
                                    "Đồng ý", "Từ chối");
                        }
                        break;
                    }
                    switch (select) {
                        case 0: {
                            ArrayList<String> menu = new ArrayList<>();
                            menu.add("Nhiệm vụ");
                            menu.add("Học\nKỹ năng");
                            Clan clan = player.clan;
                            if (clan != null) {
                                menu.add("Về khu\nvực bang");
                                if (clan.isLeader(player)) {
                                    menu.add("Giải tán\nBang hội");
                                }
                            }
                            menu.add("Kho báu\ndưới biển");
                            String[] menus = menu.toArray(String[]::new);

                            this.createOtherMenu(player, 0,
                                    "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menus);
                            break;
                        }
//                        case 2: {
//                            Item ruacon = InventoryService.gI().findItemBag(player, 874);
//                            if (ruacon != null && ruacon.quantity >= 1) {
//                                this.createOtherMenu(player, ConstNpc.MENU_GIAO_RUA_CON,
//                                        "Cảm ơn cậu đã cứu con rùa của ta\nĐể cảm ơn ta sẽ tặng cậu món quà.",
//                                        "Nhận quà", "Đóng");
//                                break;
//                            }
//                            break;
//                        }
//                        case 2:
//
//                            this.createOtherMenu(player, ConstNpc.QUY_DOI_HN,
//                                    "|7|QUY ĐỔI HỒNG NGỌC"
//                                    + "\n|6|Quy dổi Hồng ngọc từ thỏi vàng tại đây"
//                                    + "\n|1|1 Thỏi vàng = 100 Hồng ngọc"
//                                    + "\n\n|5|Nhập 1 được 100 Hồng Ngọc",
//                                    "Đồng ý", "Từ chối");
//
//                            break;
                        case 1:
                            long tongNap2 = player.getSession().coin;
                            long soHopCoTheNhan = tongNap2 / 20000; // 20k = 1 hộp

                            this.createOtherMenu(player, ConstNpc.QUY_DOI_XU,
                                    "|7|Sự kiện Quy Đổi Cải VIP"
                                    + "\n|6|Các chiến binh sẽ không mất số dư tài khoản khi quy đổi"
                                    + "\n|6|Thời gian: 19/10 - 23/10"
                                    + "\n|6|Mỗi 20.000 VND nạp = 1 Hộp Cải Trang VIP"
                                    + "\n|1|Bạn hiện có tổng : " + Util.format(tongNap2) + " VND"
                                    + "\n|1|=> Có thể nhận: " + Util.format(soHopCoTheNhan) + " hộp"
                                    + "\n\n|5|Chọn hộp bạn muốn nhận:",
                                    "SDCM","HP,KI", "Từ chối");
                            break;
                        case 2:
                            moMenuDoiSuKien(player);
                            break;
//                        case 2:
//                                            this.createOtherMenu(player, 3345,
//                                                    "|7|Meta mùa 1 từ năm này sang năm sau"
//                                                    + "\n|5|Ma phong ba gây dame theo phần trăm Hp được chuyển sáng thanh phần trăm Ki"
//                                                    + "\n|6|-Trái đất :Giảm 20% đòn chưởng Kamejoko, dịch chuyển thức thời, Tăng 20%Kaioken và Tăng thêm 150% quả cầu khênh khi"
//                                                    + "\n|6|-Xayda : Tăng 20% đòn đánh Galick,Tăng 50% Atomic và Tăng sát thương bom + 200%"
//                                                    + "\n|6|-Namek :Tăng 10% đòn đánh Liên Hoàn ,Tăng sát thương laze thêm 200% ",
//                                                    //               + "\nCHÚC AE " + Manager.SERVER_NAME + " NĂM MỚI VUI VẺ...",
//                                                    "Ok");
//                                            break;    
                    }
                    break;
                }
                case 0: {
                    switch (select) {
                        case 0:
                            NpcService.gI().createTutorial(player, tempId, avartar, player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                            break;
                        case 1:
                            if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
                                player.LearnSkill.Time = -1;
                                try {
                                    var curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId),
                                            SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId).point);
                                    player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                    SkillUtil.setSkill(player, curSkill);
                                    var msg = Service.gI().messageSubCommand((byte) 62);
                                    msg.writer().writeShort(curSkill.skillId);
                                    player.sendMessage(msg);
                                    msg.cleanup();
                                    PlayerService.gI().sendInfoHpMpMoney(player);
                                } catch (Exception e) {
                                    Logger.log(e.toString());
                                }
                                return;
                            }
                            if (player.LearnSkill.Time != -1) {
                                int ngoc = 5;
                                long time = player.LearnSkill.Time - System.currentTimeMillis();
                                if (time / 600_000 >= 2) {
                                    ngoc += time / 600_000;
                                }
                                String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
                                byte level = Byte.parseByte(subName[subName.length - 1]);
                                this.createOtherMenu(player, ConstNpc.HOC_SKILL_1,
                                        "Con đang học kỹ năng\n" + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name + " cấp " + level + "\nThời gian còn lại " + TimeUtil.getTime(time), "Học\nCấp tốc\n" + ngoc + " ngọc",
                                        "Huỷ", "Bỏ qua");
                            } else {
                                ShopService.gI().opendShop(player, "SHOP_LEARN_SKILL", false);
                            }
                            break;
                        case 2: {
                            Clan clan = player.clan;
                            if (clan != null && select == 2) {
                                ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
                            } else {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                            break;
                        }
                        case 3: {
                            boolean clanCheck = true;
                            Clan clan = player.clan;
                            if (clan != null) {
                                clanCheck = false;
                                if (clan.isLeader(player)) {
                                    createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                                } else {
                                    clanCheck = true;
                                }
                            }
                            if (clanCheck) {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                            break;
                        }
                        case 4: {
                            if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                        "Bang hội con đang ở hang kho báu cấp "
                                        + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                        "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                        "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                        "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                            }
                            break;
                        }

                    }
                    break;
                }
                case 3:
                    Clan clan = player.clan;
                    if (clan != null) {
                        if (clan.isLeader(player)) {
                            if (select == 0) {
                                Input.gI().createFormGiaiTanBangHoi(player);
                            }
                        }
                    }
                    break;
               
                case ConstNpc.DOI_QUA_SU_KIEN: {
                    java.util.List<nro.repository.dao.SuKienDAO.CongThucDoi> ds
                            = nro.service.event.SuKienService.congThucDangCo();
                    // "Từ chối" là mục cuối, và danh sách có thể đã đổi giữa
                    // lúc mở menu và lúc bấm (sự kiện vừa hết giờ). Ra ngoài
                    // khoảng thì báo, chứ đừng để rơi vào IndexOutOfBounds.
                    if (select < 0 || select >= ds.size()) {
                        Service.gI().sendThongBao(player, "Hẹn gặp lại con!");
                        break;
                    }
                    Service.gI().sendThongBao(player,
                            nro.service.event.SuKienService.doiQua(
                                    player, ds.get(select).id));
                    moMenuDoiSuKien(player);
                    break;
                }

                case ConstNpc.QUY_DOI_XU: {
                    switch (select) {
                        case 0: 
                            long coinCanTru = 20000; // Mỗi hộp 20.000 coin
                            long coinHienTai = player.getSession().coin;

                            if (coinHienTai < coinCanTru) {
                                Service.gI().sendThongBao(player, "|7|Không đủ 20.000 Coin để quy đổi!");
                                return;
                            }
                            PlayerDAO.subTongNap2(player, coinCanTru);
                            Item hopBill = ItemService.gI().createNewItem((short) 1922);
                            InventoryService.gI().addItemBag(player, hopBill);
                            InventoryService.gI().sendItemBag(player);

                            Service.gI().sendThongBao(player,
                                    "|7|Bạn đã quy đổi 20.000 Coin để nhận 1 Hộp Cải Trang VIP!"
                                    + "\n|1|Coin còn lại: " + Util.format(player.getSession().tongnap2));
                            break;
                        case 1: // Đồng ý – nhận 1 Hộp Bill
                            long coinCanTru1 = 20000; // Mỗi hộp 20.000 coin
                            long coinHienTai1 = player.getSession().coin;

                            if (coinHienTai1 < coinCanTru1) {
                                Service.gI().sendThongBao(player, "|7|Không đủ 20.000 Coin để quy đổi!");
                                return;
                            }
                            PlayerDAO.subTongNap2(player, coinCanTru1);
                            Item hopBill1 = ItemService.gI().createNewItem((short) 1923);
                            InventoryService.gI().addItemBag(player, hopBill1);
                            InventoryService.gI().sendItemBag(player);

                            Service.gI().sendThongBao(player,
                                    "|7|Bạn đã quy đổi 20.000 Coin để nhận 1 Hộp Cải Trang VIP"
                                    + "\n|1|Coin còn lại: " + Util.format(player.getSession().tongnap2));
                            break;    

                        case 2: 
                            Service.gI().sendThongBao(player, "Hẹn gặp lại con!");
                            break;
                    }
                    break;
                }

                case ConstNpc.MENU_OPENED_DBKB: {
                    switch (select) {
                        case 0:// Top bang hội
                            Service.gI().showTopClanBDKB(player);
                            break;
                        case 1:
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Bạn không có bang hội!");
                                return;
                            }
                            Service.getInstance().showMyTopClanBDKB(player);
                            break;
                        case 2: {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isFounder() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                ChangeMapService.gI().goToDBKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.formatNumber(TreasureUnderSea.POWER_CAN_GO_TO_DBKB, FormatStyle.VIETNAMESE));
                            }
                            break;
                        }
                    }
                    break;
                }
                case ConstNpc.MENU_OPEN_DBKB: {
                    switch (select) {
                        case 0:// Top bang hội
                            Service.gI().showTopClanBDKB(player);
                            break;
                        case 1:
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Bạn không có bang hội!");
                                return;
                            }
                            Service.getInstance().showMyTopClanBDKB(player);
                            break;
                        case 2: {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isFounder() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                Input.gI().createFormChooseLevelBDKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.formatNumber(TreasureUnderSea.POWER_CAN_GO_TO_DBKB, FormatStyle.VIETNAMESE));
                            }
                            break;
                        }
                    }
                    break;
                }
                case ConstNpc.MENU_ACCEPT_GO_TO_BDKB: {
                    switch (select) {
                        case 0:
                            TreasureUnderSeaService.gI().openBanDoKhoBau(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                            break;
                    }
                    break;
                }
                case ConstNpc.MENU_GIAO_RUA_CON: {
                    if (select == 0) {
                        Item ruacon = InventoryService.gI().findItemBag(player, 874);
                        if (ruacon != null && ruacon.quantity >= 1) {
                            InventoryService.gI().subQuantityItemsBag(player, ruacon, 1);

                            // Danh sách các vật phẩm có thể nhận được
                            short[] itemIds = {
                                1589, 1590, 1591, 1592, 1593, 1594, 1611, 1621, 1612,
                                611, 1622, 1620, 1641, 1642, 2048, 2049, 1643, 457
                            };

                            // Xác suất nhận từng vật phẩm
                            int chance = 5; // Xác suất 5%
                            Item itemReceived;

                            if (Util.isTrue(chance, 100)) {
                                itemReceived = ItemService.gI().createNewItem(itemIds[Util.nextInt(0, itemIds.length - 1)]);
                            } else {
                                itemReceived = ItemService.gI().createNewItem((short) 457); // Mặc định nếu không trúng vật phẩm hiếm
                            }

                            InventoryService.gI().addItemBag(player, itemReceived);
                            InventoryService.gI().sendItemBag(player);
                            Service.getInstance().sendThongBao(player, "Bạn nhận được " + itemReceived.Name());
                        } else {
                            Service.getInstance().sendThongBao(player, "Bạn không có Rùa Con");
                        }
                    }
                    break;
                }
                case ConstNpc.MENU_PE_NA: {
                    switch (select) {
                        case 0:
                            RewardService.gI().rewardBeNa(player);
                            break;
                    }
                    break;
                }
                case ConstNpc.MENU_LAN_CON: {
                    switch (select) {
                        case 0:
                            RewardService.gI().rewardLancon(player);
                            break;
                    }
                    break;
                }
                case ConstNpc.HOC_SKILL_1: {
                    if (select == 1) {
                        this.createOtherMenu(player, ConstNpc.HOC_SKILL_2, "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
                    } else if (select == 0) {
                        long time = player.LearnSkill.Time - System.currentTimeMillis();
                        int ngoc = 5;
                        if (time / 600_000 >= 2) {
                            ngoc += time / 600_000;
                        }
                        if (player.inventory.gem < ngoc) {
                            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                            return;
                        }
                        player.inventory.subGem(ngoc);
                        player.LearnSkill.Time = -1;
                        try {
                            String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
                            byte level = Byte.parseByte(subName[subName.length - 1]);
                            Skill curSkill = SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId);
                            if (curSkill.point == 0) {
                                player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);
                                SkillUtil.setSkill(player, curSkill);
                                var msg = Service.getInstance().messageSubCommand((byte) 23);
                                msg.writer().writeShort(curSkill.skillId);
                                player.sendMessage(msg);
                                msg.cleanup();
                            } else {
                                player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);
                                SkillUtil.setSkill(player, curSkill);
                                var msg = Service.getInstance().messageSubCommand((byte) 62);
                                msg.writer().writeShort(curSkill.skillId);
                                player.sendMessage(msg);
                                msg.cleanup();
                            }
                            PlayerService.gI().sendInfoHpMpMoney(player);
                        } catch (Exception e) {
                            Logger.log(e.toString());
                        }
                    }
                    break;
                }
                case ConstNpc.HOC_SKILL_2: {
                    if (select == 0) {
                        player.nPoint.tiemNang += player.LearnSkill.Potential / 2;
                        PlayerService.gI().sendTNSM(player, (byte) 1, player.LearnSkill.Potential / 2);
                        player.LearnSkill.Time = -1;
                        Service.gI().point(player);
                        PlayerService.gI().sendInfoHpMpMoney(player);
                        Service.gI().ClosePanel(player);
                        NpcService.gI().createTutorial(player, NpcService.gI().getAvatar(13 + player.gender), "Con đã huỷ học kĩ năng thành công, ta sẽ trả lại con 50% tiềm năng đã học");
                    }
                    break;
                }
                case ConstNpc.NHAN_KEO_HALLOWEEN: {
                    switch (select) {
                        case 0:
                            Item KeoBanTay = ItemService.gI().createNewItem((short) 901, 1);
                            KeoBanTay.addOptionParam(86, 0);
                            KeoBanTay.addOptionParam(93, 35);
                            int quality = Util.nextInt(1, 3);
                            KeoBanTay.quantity = quality;
                            InventoryService.gI().addItemBag(player, KeoBanTay);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().chat(player, "Haha xin được " + quality + " kẹo bàn tay rồi");
                            player.NhanKeoHayBiGheoNpc_13++;
                            break;
                        case 1:
                            player.NhanKeoHayBiGheoNpc_13++;
                            break;
                    }
                    break;
                }

            }
        }
    }
    /**
     * Menu đổi quà sự kiện — danh sách công thức lấy từ panel.
     *
     * <p>Dựng lại mỗi lần mở chứ không nhớ sẵn: sự kiện hết giờ giữa chừng thì
     * lần bấm sau đã thấy danh sách mới, và {@code doiQua} vẫn kiểm tra lại
     * theo id nên không đổi được món của sự kiện đã tắt.</p>
     */
    private void moMenuDoiSuKien(Player player) {
        java.util.List<nro.repository.dao.SuKienDAO.CongThucDoi> ds
                = nro.service.event.SuKienService.congThucDangCo();
        if (ds.isEmpty()) {
            Service.gI().sendThongBao(player, "Lúc này không có sự kiện nào cả.");
            return;
        }
        StringBuilder loi = new StringBuilder("|7|ĐỔI QUÀ SỰ KIỆN");
        ArrayList<String> menu = new ArrayList<>();
        for (nro.repository.dao.SuKienDAO.CongThucDoi c : ds) {
            loi.append("\n").append(
                    nro.service.event.SuKienService.moTaCongThuc(c, player));
            menu.add(nro.service.event.SuKienService.tenNut(c));
        }
        menu.add("Từ chối");
        this.createOtherMenu(player, ConstNpc.DOI_QUA_SU_KIEN,
                loi.toString(), menu.toArray(new String[0]));
    }
}
