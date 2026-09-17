package nro.entity.combine.list;

import nro.core.util.Util;
import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.combine.CombineService;

public class TinhAnTrangBi {

    /**
     * Món nào ấn được: đồ <b>Thần Linh</b>, <b>Huỷ Diệt</b> và <b>set kích
     * hoạt</b>.
     *
     * <p>Bản cũ chỉ xét <code>isDTL()</code> và <code>isDHD()</code> — hai hàm đó đọc
     * <code>template.level</code> (13 và 14). Đồ set kích hoạt do Bà Hạt Mít nâng ra
     * mang mẫu đồ bậc khác, không rơi vào hai mức ấy, nên bỏ vào là nhận ngay
     * "Vật phẩm này không thể hóa ấn" — Tinh Ấn, Nguyệt Ấn, Nhật Ấn đều không
     * làm được trên bộ đồ mạnh nhất của người chơi.</p>
     *
     * <p>Đồ set kích hoạt nhận ra bằng option nhận diện của set (xem
     * <code>SetBonusDAO.laDoSetKichHoat</code>), nên set nào khai thêm trên panel
     * cũng ấn được ngay.</p>
     */
    private static boolean isTrangBiAn(Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        return item.isDTL() || item.isDHD()
                || nro.repository.dao.SetBonusDAO.laDoSetKichHoat(item);
    }

    /** Ba mẫu đá ấn: Tinh Ấn, Nguyệt Ấn, Nhật Ấn. */
    private static boolean laDaAn(Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        int id = item.template.id;
        return id == 1724 || id == 1725 || id == 1726;
    }

    /**
     * Hai ô ghép, đọc theo <b>món là gì</b> chứ không theo ô nào trước.
     *
     * <h3>Vì sao không đọc theo thứ tự ô</h3>
     *
     * <p>Bản cũ lấy cứng ô 0 làm trang bị và ô 1 làm đá. Bỏ đá vào trước thì
     * chính hòn đá bị đem đi kiểm \"có ấn được không\", và người chơi nhận câu
     * \"Vật phẩm này không thể hóa ấn\" dù đã bỏ đúng và đủ đồ. Không có gì
     * trên màn hình nói rằng thứ tự bỏ vào lại quan trọng.</p>
     *
     * <p>Hai món khác hẳn nhau — một bên là đá ấn, bên kia là trang bị — nên
     * nhận ra bằng chính món đó là chắc chắn, và bỏ vào ô nào cũng xong.</p>
     *
     * <return>{trang bị, đá ấn}, hoặc <code>null</code> nếu chưa đủ hai món</return>
     */
    private static Item[] doiMon(Player player) {
        if (player.combine == null || player.combine.itemsCombine == null
                || player.combine.itemsCombine.size() < 2) {
            return null;
        }
        Item a = player.combine.itemsCombine.get(0);
        Item b = player.combine.itemsCombine.get(1);
        if (laDaAn(a) && !laDaAn(b)) {
            return new Item[]{b, a};
        }
        return new Item[]{a, b};
    }

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
            return;
        }

        if (player.combine.itemsCombine.size() != 2) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
            return;
        }

        Item[] doi = doiMon(player);
        Item item = doi[0];
        Item dangusac = doi[1];

        if (!isTrangBiAn(item)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Vật phẩm này không thể hóa ấn", "Đóng");
            return;
        }

        if (!laDaAn(dangusac) || dangusac.quantity < 99) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
            return;
        }

        String loaiTrangBi = item.isDTL() ? "thần linh"
                : (item.isDHD() ? "hủy diệt"
                : (nro.repository.dao.SetBonusDAO.laDoSetKichHoat(item)
                        ? "set kích hoạt" : "này"));

        String npcSay = item.template.name + "\n|2|";
        for (ItemOption io : item.itemOptions) {
            npcSay += io.getOptionString() + "\n";
        }

        npcSay += "|1|Con có muốn biến trang bị " + loaiTrangBi + " " + item.template.name + " thành\n"
                + "trang bị Ấn không?\n|4|Tỉ lệ thành công 100%\n"
                + "|7|Cần 99 " + dangusac.template.name;

        CombineService.gI().baHatMit.createOtherMenu(player,
                ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
    }

    public static void startCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            return;
        }

        if (player.combine.itemsCombine.size() < 2) {
            return;
        }

        Item[] doi = doiMon(player);
        Item item = doi[0];
        Item dangusac = doi[1];

        if (!isTrangBiAn(item)) {
            Service.gI().sendThongBao(player, "Không thể tinh ấn vật phẩm này");
            return;
        }

        if (!laDaAn(dangusac) || dangusac.quantity < 99) {
            Service.gI().sendThongBao(player, "Thiếu đá ngũ sắc");
            return;
        }

        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 34
                    || io.optionTemplate.id == 35
                    || io.optionTemplate.id == 36) {
                Service.gI().sendThongBao(player, "Trang bị đã có ấn rồi");
                return;
            }
        }

        boolean isSuccess = Util.nextInt(100) < 100;

        InventoryService.gI().subQuantityItemsBag(player, dangusac, 99);

        if (isSuccess) {
            switch (dangusac.template.id) {
                case 1724:
                    item.itemOptions.add(new ItemOption(34, 1));
                    break;
                case 1725:
                    item.itemOptions.add(new ItemOption(35, 1));
                    break;
                case 1726:
                    item.itemOptions.add(new ItemOption(36, 1));
                    break;
                default:
                    break;
            }

            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Tinh ấn thành công!");
        } else {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Tinh ấn thất bại! Trang bị đã bị phá hủy.");
        }

        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}