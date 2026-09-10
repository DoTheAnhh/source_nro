package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.repository.dao.SachTuyetKyDAO;
import nro.service.combine.CombineService;
import java.util.HashSet;
import java.util.Set;

/**
 * Tẩy Sách Tuyệt Kỹ: trả các dòng chỉ số đã giám định về "- Chưa giám định".
 *
 * <h2>Vì sao viết lại</h2>
 *
 * <p>Bản cũ duyệt danh sách chỉ số từ đầu và <b>dừng ở dòng đầu tiên mang
 * option 21</b>, thay mọi dòng đứng trước nó bằng dòng chưa giám định. Nhưng
 * thứ tự chỉ số trên một cuốn sách không cố định — nó do công thức ghép và các
 * lần nâng cấp quyết định. Cuốn nào có option 21 nằm ngay đầu thì vòng lặp
 * <b>thoát ở vòng đầu tiên</b>, không tẩy được dòng nào, mà
 * {@code subOptionParam(219, 1)} ngay dưới <b>vẫn trừ một lần tẩy</b>.</p>
 *
 * <p>Người chơi thấy đúng điều đó: mất lần tẩy, chỉ số cũ còn nguyên.</p>
 *
 * <h2>Cách làm mới</h2>
 *
 * <p>Tẩy là <b>phép nghịch đảo của giám định</b>, nên nó đảo đúng những gì giám
 * định làm ra: {@link GiamDinhSach} thay mỗi dòng 217 bằng một chỉ số bốc từ kho
 * trên panel, nên tẩy đưa những dòng <i>thuộc kho ấy</i> về lại 217.</p>
 *
 * <p>Xét theo <b>id chỉ số</b> chứ không theo vị trí. Vị trí thì đổi, còn kho
 * chỉ số là danh sách quản trị viên tự khai nên nó chính xác là tập những dòng
 * có thể tẩy được. Dòng nào không nằm trong kho — độ bền, số lần tẩy, yêu cầu
 * sức mạnh, khoá — không bị đụng tới, dù nằm ở đâu.</p>
 *
 * <p>Và <b>không trừ lần tẩy khi không tẩy được gì</b>.</p>
 */
public class TaySach {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        Item sachTuyetKy = player.combine.itemsCombine.get(0);
        if (sachTuyetKy == null || !sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2()) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        int conLai = sachTuyetKy.getOptionParam(219);
        if (conLai <= 0) {
            Service.gI().sendDialogMessage(player,
                    "Cuốn này đã hết lượt tẩy.");
            return;
        }
        int soDong = demDongTayDuoc(sachTuyetKy);
        if (soDong <= 0) {
            Service.gI().sendDialogMessage(player,
                    "Cuốn này chưa có dòng nào đã giám định để tẩy.");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player,
                ConstNpc.MENU_START_COMBINE,
                ConstFont.BOLD_BLUE + "Tẩy Sách Tuyệt Kỹ ?\n"
                + "Sẽ xoá " + soDong + " dòng chỉ số, còn lại "
                + (conLai - 1) + " lượt tẩy.",
                "Đồng ý", "Từ chối");
    }

    /**
     * Những dòng <b>không phải chỉ số giám định</b> — tẩy không được đụng vào.
     *
     * <h2>Vì sao đổi từ danh sách trắng sang danh sách đen</h2>
     *
     * <p>Bản trước lấy tập tẩy được từ <b>kho chỉ số trên panel</b>. Nghe hợp
     * lý, nhưng kho ấy <i>trôi</i>: quản trị viên đổi một chỉ số, bỏ một dòng,
     * hay gieo lại kho bằng bộ khác — thì những cuốn sách đã giám định từ trước
     * mang chỉ số <b>không còn nằm trong kho</b>. Tẩy nhìn vào kho, không nhận
     * ra dòng nào, và cuốn sách thành không tẩy được.</p>
     *
     * <p>Nay xét theo <b>kho chỉ số trên panel</b>, tức đúng tập những dòng mà
     * giám định có thể bốc ra. Dòng nào không nằm trong kho — độ bền, số lần
     * tẩy, yêu cầu sức mạnh, hay bất cứ thứ gì công thức gắn thêm — <b>không bị
     * đụng tới</b>. Nhờ thế số ô giám định của cuốn sách không bao giờ phình
     * ra: tẩy chỉ trả những ô đã bốc về trạng thái chưa bốc.</p>
     *
     * <p>Bản trước làm ngược — giữ lại một danh sách nhỏ rồi tẩy tất cả phần
     * còn lại — và đó là lý do số dòng "Chưa giám định" tự tăng: mấy dòng vốn
     * không phải ô giám định cũng bị biến thành ô giám định.</p>
     */
    private static final Set<Integer> DONG_GIU_LAI = new HashSet<>(
            java.util.Arrays.asList(
                    SachTuyetKyDAO.OPTION_CHUA_GIAM_DINH, // 217
                    219,   // so lan tay — xem OPTION_SO_LAN_TAY
                    21,    // yêu cầu sức mạnh
                    30,    // không thể giao dịch
                    31,    // số lượng
                    72,    // cấp
                    93));  // hạn sử dụng

    /** Chỉ số "Số lần tẩy" — luôn đứng cuối danh sách. */
    private static final int OPTION_SO_LAN_TAY = 219;

    /**
     * Tập id chỉ số mà giám định có thể bốc ra — tức tập tẩy được.
     *
     * <p>Đọc từ kho trên panel, kể cả dòng đang tắt: một chỉ số vừa bị tắt vẫn
     * còn nằm trên những cuốn sách giám định từ trước, và người chơi phải tẩy
     * được nó.</p>
     */
    private static Set<Integer> idChiSoTayDuoc() {
        Set<Integer> ids = new HashSet<>();
        for (SachTuyetKyDAO.ChiSo cs : SachTuyetKyDAO.danhSachChiSo()) {
            if (cs != null) {
                ids.add(cs.optionId);
            }
        }
        return ids;
    }

    /**
     * Xếp lại các dòng của cuốn sách cho dễ đọc.
     *
     * <p>Ba nhóm, theo đúng thứ tự: dòng cấu trúc (yêu cầu sức mạnh, không giao
     * dịch…) — rồi <b>toàn bộ ô giám định liền nhau</b> — rồi "Số lần tẩy" đứng
     * cuối cùng.</p>
     *
     * <p>Không xếp thì ô giám định nằm rải xen giữa mấy dòng khác, vì giám định
     * thay tại chỗ còn công thức thì gắn thêm dòng vào cuối lúc nào cũng được.
     * Nhìn vào một cuốn sách không đếm nổi nó còn mấy ô.</p>
     */
    public static void sapXep(Item sach) {
        if (sach == null || sach.itemOptions == null) {
            return;
        }
        Set<Integer> kho = idChiSoTayDuoc();
        java.util.List<ItemOption> cauTruc = new java.util.ArrayList<>();
        java.util.List<ItemOption> oGiamDinh = new java.util.ArrayList<>();
        ItemOption soLanTay = null;
        for (ItemOption io : sach.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            int id = io.optionTemplate.id;
            if (id == OPTION_SO_LAN_TAY) {
                soLanTay = io;
            } else if (id == SachTuyetKyDAO.OPTION_CHUA_GIAM_DINH
                    || kho.contains(id)) {
                oGiamDinh.add(io);
            } else {
                cauTruc.add(io);
            }
        }
        sach.itemOptions.clear();
        sach.itemOptions.addAll(cauTruc);
        sach.itemOptions.addAll(oGiamDinh);
        if (soLanTay != null) {
            sach.itemOptions.add(soLanTay);
        }
    }

    public static void taySach(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendServerMessage(player, "Cần đúng một Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        Item sachTuyetKy = player.combine.itemsCombine.get(0);
        if (sachTuyetKy == null || !sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2()) {
            Service.gI().sendServerMessage(player, "Vật phẩm này không phải Sách Tuyệt Kỹ.");
            return;
        }
        if (sachTuyetKy.getOptionParam(OPTION_SO_LAN_TAY) <= 0) {
            Service.gI().sendServerMessage(player, "Cuốn này đã hết lượt tẩy.");
            return;
        }

        Set<Integer> kho = idChiSoTayDuoc();
        int daTay = 0;
        for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
            ItemOption io = sachTuyetKy.itemOptions.get(i);
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            if (kho.contains(io.optionTemplate.id)) {
                sachTuyetKy.itemOptions.set(i,
                        new ItemOption(SachTuyetKyDAO.OPTION_CHUA_GIAM_DINH, 0));
                daTay++;
            }
        }

        // Khong tay duoc dong nao thi KHONG tru luot.
        //
        // Day chinh la loi cu: no tru truoc, xet sau — hoac khong xet gi ca.
        if (daTay == 0) {
            Service.gI().sendServerMessage(player,
                    "Cuốn này chưa có dòng nào đã giám định, chưa trừ lượt tẩy.");
            return;
        }

        sachTuyetKy.subOptionParam(OPTION_SO_LAN_TAY, 1);
        sapXep(sachTuyetKy);
        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Đã tẩy " + daTay
                + " dòng chỉ số, còn " + sachTuyetKy.getOptionParam(OPTION_SO_LAN_TAY)
                + " lượt tẩy.");
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }


    private static int demDongTayDuoc(Item sach) {
        if (sach == null || sach.itemOptions == null) {
            return 0;
        }
        Set<Integer> kho = idChiSoTayDuoc();
        int n = 0;
        for (ItemOption io : sach.itemOptions) {
            if (io != null && io.optionTemplate != null
                    && kho.contains(io.optionTemplate.id)) {
                n++;
            }
        }
        return n;
    }
}
