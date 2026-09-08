package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.combine.CombineService;
import nro.core.util.Util;

/**
 * Nhập 7 viên Ngọc Rồng thành 1 viên cấp cao hơn.
 *
 * <h2>Số lần nhập đi thẳng vào hàm, không qua biến của Player</h2>
 *
 * <p>Bản trước để số lần trong {@code player.nhapNgocRongTimes}: menu ghi vào
 * đó, rồi hàm nhập đọc ra và <b>lập tức đặt lại về 1</b>. Đó là một mẩu trạng
 * thái sống đúng một nhịp giữa hai lời gọi, và bất cứ thứ gì chen vào giữa —
 * gói tin tới hai lần, một luồng khác chạm vào cùng người chơi, một đường mở
 * lại bảng ghép — đều biến "nhập 100" thành "nhập 1" mà không để lại dấu vết
 * nào. Nay số lần là <b>tham số của hàm</b>, không còn khe nào để lọt.</p>
 *
 * <h2>Thiếu thì nhập hết chỗ có, không từ chối</h2>
 *
 * <p>Bấm x100 mà chỉ đủ 31 lần thì làm 31 lần rồi để lại phần dư, chứ không
 * báo lỗi rồi thôi. Cùng luật cho x10, "Nhập hết" và số tự điền.</p>
 */
public class NhapNgocRong {

    private static final int SO_NGOC_CAN = 7;

    /** Id vật phẩm Lọ nước phép — nguyên liệu riêng của mức 1 sao. */
    private static final int LO_NUOC_PHEP = 1029;

    /** Vàng cho mỗi lần nhập 7 Ngọc Rồng 1 sao thành 1 Ngọc Rồng Siêu Cấp. */
    private static final long VANG_MOI_LAN_SIEU_CAP = 500_000_000L;

    /** Ý nghĩa của từng ô trong menu, dùng chung cho cả lúc dựng và lúc bấm. */
    public static final int CHON_X1 = 0;
    public static final int CHON_X10 = 1;
    public static final int CHON_X100 = 2;
    public static final int CHON_HET = 3;
    public static final int CHON_SO_KHAC = 4;

    /* =========================
     * HIỂN THỊ MENU
     * ========================= */
    public static void showInfoCombine(Player player) {

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendDialogMessage(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
            return;
        }

        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "Cần 7 viên Ngọc Rồng");
            return;
        }

        Item item = player.combine.itemsCombine.get(0);
        if (!isValid(item)) {
            Service.gI().sendDialogMessage(player, "Cần 7 viên Ngọc Rồng hợp lệ");
            return;
        }

        // ===== NGỌC 2–7 SAO =====
        if (item.template.id > 14 && item.template.id <= 20) {

            if (item.template.id <= 16) {
                Service.gI().sendDialogMessage(player, "Không thể nhập Ngọc Rồng 3 sao hoặc thấp hơn!");
                return;
            }

            int toiDa = item.quantity / SO_NGOC_CAN;
            String text = ConstFont.BOLD_BLUE
                    + "Con có muốn biến 7 " + item.template.name + " thành\n"
                    + "1 viên " + ItemService.gI().getTemplate((short) (item.template.id - 1)).name + "\n"
                    + ConstFont.BOLD_GREEN + "Cần 7 " + item.template.name + "\n"
                    + ConstFont.BOLD_BLUE + "Đang có " + item.quantity
                    + " viên, nhập được tối đa " + toiDa + " lần";

            moMenuChonSoLan(player, text);
            return;
        }

        // ===== NGỌC 1 SAO → SIÊU CẤP =====
        if (item.template.id == 14) {

            Item loNuocPhep = InventoryService.gI().findItemBag(player, LO_NUOC_PHEP);

            StringBuilder text = new StringBuilder();
            text.append(ConstFont.BOLD_BLUE)
                    .append("Con có muốn biến 7 Ngọc Rồng 1 sao thành\n")
                    .append("1 viên Ngọc Rồng Siêu Cấp\n")
                    .append(ConstFont.BOLD_GREEN)
                    .append("Cần 7 Ngọc Rồng 1 sao\n")
                    .append(player.inventory.gold >= VANG_MOI_LAN_SIEU_CAP
                            ? ConstFont.BOLD_GREEN : ConstFont.BOLD_RED)
                    // Ghi đúng số vàng thật sự bị trừ.
                    //
                    // Dòng này trước ghi "150.000.000" trong khi hàm nhập trừ
                    // 500.000.000 — người chơi đọc một số, mất một số khác, và
                    // ô chữ còn xanh (đủ tiền) trong lúc phép nhập lặng lẽ
                    // không chạy vì thiếu tiền.
                    .append("Cần ").append(Util.numberToMoney(VANG_MOI_LAN_SIEU_CAP))
                    .append(" vàng mỗi lần\n")
                    .append(loNuocPhep == null ? ConstFont.BOLD_RED : ConstFont.BOLD_GREEN)
                    .append("Cần 1 Lọ nước phép mỗi lần\n")
                    .append(ConstFont.BOLD_BLUE)
                    .append("Tỉ lệ thành công: 50%");

            if (loNuocPhep == null || player.inventory.gold < VANG_MOI_LAN_SIEU_CAP) {
                CombineService.gI().baHatMit.createOtherMenu(
                        player,
                        ConstNpc.IGNORE_MENU,
                        text.toString(),
                        "Thiếu\nNguyên liệu"
                );
                return;
            }

            moMenuChonSoLan(player, text.toString());
        }
    }

    /**
     * Menu chọn số lần nhập.
     *
     * <p>Nhãn <b>tối đa hai dòng</b>: menu của bản mod chỉ vẽ hai dòng đầu mỗi
     * nút, dòng thứ ba bị bỏ lặng lẽ.</p>
     */
    private static void moMenuChonSoLan(Player player, String text) {
        CombineService.gI().baHatMit.createOtherMenu(
                player,
                ConstNpc.MENU_START_COMBINE,
                text,
                "Nhập x1",
                "Nhập x10",
                "Nhập x100",
                "Nhập hết",
                "Nhập số\nkhác",
                "Từ chối"
        );
    }

    /** Mở ô cho người chơi tự gõ số lần nhập. */
    public static void hoiSoLan(Player player) {
        Service.gI().moHopNhapChu(player,
                "Ngươi muốn nhập bao nhiêu lần?",
                ConstNpc.O_NHAP_SO_LAN_NHAP_NGOC);
    }

    /* =========================
     * XỬ LÝ NHẬP
     * ========================= */

    /**
     * Nhập ngọc.
     *
     * @param soLanMuon số lần người chơi xin làm. Truyền
     *                  {@link Integer#MAX_VALUE} nghĩa là "nhập hết"; hàm tự
     *                  hạ xuống đúng số lần nguyên liệu cho phép.
     */
    public static void nhapNgocRong(Player player, int soLanMuon) {

        if (soLanMuon <= 0) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendServerMessage(player, "Hành trang không đủ chỗ trống");
            return;
        }
        if (player.combine.itemsCombine.size() != 1) {
            return;
        }

        Item item = player.combine.itemsCombine.get(0);
        if (!isValid(item)) {
            return;
        }

        int soLan = Math.min(soLanMuon, item.quantity / SO_NGOC_CAN);
        if (soLan <= 0) {
            Service.gI().sendServerMessage(player, "Không đủ số lượng Ngọc Rồng");
            return;
        }

        // ===== NGỌC THƯỜNG =====
        if (item.template.id > 14 && item.template.id <= 20) {

            if (item.template.id <= 16) {
                Service.gI().sendServerMessage(player, "Không thể nhập Ngọc Rồng 3 sao hoặc thấp hơn!");
                return;
            }

            Item nr = ItemService.gI().createNewItem((short) (item.template.id - 1));
            nr.quantity = soLan;
            nr.itemOptions.add(new ItemOption(30, 1));

            CombineService.gI().sendEffectCombineDB(player, nr.template.iconID);
            InventoryService.gI().addItemBag(player, nr);
            InventoryService.gI().subQuantityItemsBag(player, item, soLan * SO_NGOC_CAN);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Đã nhập " + soLan + " lần, còn dư "
                    + (item.quantity) + " " + item.template.name);
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        // ===== 1 SAO → SIÊU CẤP =====
        if (item.template.id == 14) {

            Item loNuocPhep = InventoryService.gI().findItemBag(player, LO_NUOC_PHEP);
            if (loNuocPhep == null || !loNuocPhep.isNotNullItem()) {
                Service.gI().sendServerMessage(player, "Cần Lọ nước phép");
                return;
            }

            // Hạ số lần xuống theo NGUYÊN LIỆU HIẾM NHẤT, thay vì từ chối cả
            // lượt. Bấm "nhập hết" mà chỉ có 3 lọ nước phép thì làm 3 lần.
            soLan = Math.min(soLan, loNuocPhep.quantity);
            soLan = (int) Math.min(soLan, player.inventory.gold / VANG_MOI_LAN_SIEU_CAP);
            if (soLan <= 0) {
                Service.gI().sendServerMessage(player,
                        "Không đủ Lọ nước phép hoặc vàng");
                return;
            }

            long vangCan = VANG_MOI_LAN_SIEU_CAP * soLan;

            int thanhCong = 0;
            for (int i = 0; i < soLan; i++) {
                if (Util.isTrue(50, 100)) {
                    thanhCong++;
                }
            }

            if (thanhCong > 0) {
                Item nrSC = ItemService.gI().createNewItem((short) 1015);
                nrSC.quantity = thanhCong;
                nrSC.itemOptions.add(new ItemOption(30, 0));
                nrSC.itemOptions.add(new ItemOption(87, 0));
                InventoryService.gI().addItemBag(player, nrSC);
                CombineService.gI().sendEffectCombineDB(player, nrSC.template.iconID);
            } else {
                CombineService.gI().sendEffectFailCombine(player);
            }

            InventoryService.gI().subQuantityItemsBag(player, item, soLan * SO_NGOC_CAN);
            InventoryService.gI().subQuantityItemsBag(player, loNuocPhep, soLan);
            player.inventory.gold -= vangCan;

            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Nhập " + soLan + " lần, thành công "
                    + thanhCong + " viên Ngọc Rồng Siêu Cấp");
            CombineService.gI().reOpenItemCombine(player);
        }
    }

    private static boolean isValid(Item item) {
        return item != null
                && item.isNotNullItem()
                && item.template.id >= 14
                && item.template.id <= 20
                && item.quantity >= SO_NGOC_CAN;
    }
}
