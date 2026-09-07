package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.util.FormatStyle;
import nro.core.util.Util;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.combine.CombineService;

public class NangCapSaoPhaLe {

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần 1 ô trống trong hành trang.");
            Service.gI().hideWaitDialog(player);
            return;
        }
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Cần 1 đá Hematite và 1 Sao Pha Lê cấp 1");
            return;
        }
        Item hematite = null;
        Item saoPhaLeC1 = null;

        for (Item item : player.combine.itemsCombine) {
            if (item.template.id == 1423 || item.template.id == 1441) {
                hematite = item;
            } else if (item.isDaPhaLeC1()) {
                saoPhaLeC1 = item;
            }
        }

        if (hematite == null || saoPhaLeC1 == null) {
            Service.gI().sendDialogMessage(player, "Cần 1 đá Hematite và 1 Sao Pha Lê cấp 1");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Nâng cấp Sao Pha lê lên cấp 2\n");
        text.append(ConstFont.BOLD_GREEN).append("Cần 1 Hematite\n");
        text.append(ConstFont.BOLD_GREEN).append("Cần 1 ").append(saoPhaLeC1.template.name).append("\n");
        text.append(ConstFont.BOLD_GREEN).append("Tỉ lệ thành công: 50%\n");
        text.append(player.inventory.gold >= 100_000_000 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần 100 Tr vàng\n");
        text.append(player.inventory.getGemAndRuby() >= 50 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần 50 ngọc");
        if (player.inventory.getGemAndRuby() < 50) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\n" + Util.formatNumber(50 - player.inventory.getGemAndRuby(), FormatStyle.VIETNAMESE) + " ngọc");
            return;
        }
        if (player.inventory.gold < 100_000_000) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\n" + Util.formatNumber(100_000_000 - player.inventory.gold, FormatStyle.VIETNAMESE) + " vàng");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player,
                ConstNpc.MENU_START_COMBINE, text.toString(),
                "Làm phép", "Làm đến\nkhi được", "Từ chối");
    }

    /** Số lần thử nhiều nhất trong một lượt bấm "Làm đến khi được". */
    private static final int TOI_DA_LAN = 200;

    /**
     * Thử nâng cấp <b>liên tục cho tới khi thành công</b> hoặc hết nguyên liệu.
     *
     * <h3>Vì sao cần</h3>
     *
     * <p>Tỉ lệ 50% nên trung bình hai lần là được, nhưng ai có vài chục viên thì
     * phải bấm qua lại giữa hai menu vài chục lượt. Gộp vào một lần bấm.</p>
     *
     * <h3>Không hiện hiệu ứng từng lần</h3>
     *
     * <p>Bản một lần có hiệu ứng nổ và câu thoại của Bà Hạt Mít; lặp bốn chục
     * lượt mà mỗi lượt một hiệu ứng thì client bị dội. Nên chỉ báo <b>một lần</b>
     * ở cuối, kèm số lượt đã thử.</p>
     *
     * <h3>Có trần lượt thử</h3>
     *
     * <p>{@link #TOI_DA_LAN} chặn ở 200: người có mười nghìn viên bấm một cái là
     * vòng lặp chạy mười nghìn lượt ngay trong luồng xử lý gói, treo cả khu.</p>
     */
    public static void lamDenKhiDuoc(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần 1 ô trống trong hành trang.");
            return;
        }
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }
        Item hematite = null;
        Item saoPhaLeC1 = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.template.id == 1423 || item.template.id == 1441) {
                hematite = item;
            } else if (item.isDaPhaLeC1()) {
                saoPhaLeC1 = item;
            }
        }
        if (hematite == null || saoPhaLeC1 == null) {
            return;
        }
        // Nho lai mau TRUOC khi vong lap tru mat vien dau tien: den luc trao qua
        // thi vien nguyen lieu co the da bien khoi hanh trang.
        Item mau = saoPhaLeC1.cloneItem();
        int idC1 = saoPhaLeC1.template.id;

        int soLan = 0;
        boolean thanhCong = false;
        while (soLan < TOI_DA_LAN
                && hematite.isNotNullItem() && hematite.quantity > 0
                && saoPhaLeC1.isNotNullItem() && saoPhaLeC1.quantity > 0
                && player.inventory.getGemAndRuby() >= 50
                && player.inventory.gold >= 100_000_000) {
            soLan++;
            player.inventory.subGemAndRuby(50);
            player.inventory.gold -= 100_000_000;
            InventoryService.gI().subQuantityItemsBag(player, hematite, 1);
            InventoryService.gI().subQuantityItemsBag(player, saoPhaLeC1, 1);
            if (Util.isTrue(50, 100)) {
                thanhCong = true;
                break;
            }
        }

        if (soLan == 0) {
            Service.gI().sendThongBao(player,
                    "Không đủ nguyên liệu, vàng hoặc ngọc để làm.");
            return;
        }
        if (thanhCong) {
            Item saoPhaLeC2 = mau.cloneItem();
            saoPhaLeC2.quantity = 1;
            saoPhaLeC2.template = ItemService.gI().getTemplate(idC1 + 975);
            saoPhaLeC2.itemOptions.add(new ItemOption(30, 0));
            saoPhaLeC2.itemOptions.add(new ItemOption(87, 0));
            CombineService.gI().sendEffectCombineItem(player, (byte) 7,
                    (short) saoPhaLeC2.template.iconID, (short) -1);
            InventoryService.gI().addItemBag(player, saoPhaLeC2);
            final int lan = soLan;
            Util.setTimeout(() -> {
                Service.gI().sendServerMessage(player, "Bạn nhận được "
                        + saoPhaLeC2.template.name + " sau " + lan + " lần thử");
                CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con nhé");
            }, 2000);
        } else {
            CombineService.gI().sendEffectCombineItem(player, (byte) 8,
                    (short) -1, (short) -1);
            final int lan = soLan;
            Util.setTimeout(() -> {
                Service.gI().sendServerMessage(player, "Đã thử " + lan
                        + " lần mà chưa được — hết nguyên liệu hoặc hết lượt.");
                CombineService.gI().baHatMit.npcChat(player,
                        "Chúc con may mắn lần sau, đừng buồn con nhé");
            }, 2000);
        }
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    public static void nangCapSaoPhaLe(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            return;
        }
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }
        Item hematite = null;
        Item saoPhaLeC1 = null;

        for (Item item : player.combine.itemsCombine) {
            if (item.template.id == 1423 || item.template.id == 1441) {
                hematite = item;
            } else if (item.isDaPhaLeC1()) {
                saoPhaLeC1 = item;
            }
        }

        if (hematite == null || saoPhaLeC1 == null) {
            return;
        }
        if (player.inventory.getGemAndRuby() < 50 || player.inventory.gold < 100_000_000) {
            return;
        }
        CombineService.gI().baHatMit.npcChat(player, "Bư cô lô, ba cô la, bư ra bư zô...");
        if (Util.isTrue(50, 100)) {
            Item saoPhaLeC2 = saoPhaLeC1.cloneItem();
            saoPhaLeC2.quantity = 1;
            saoPhaLeC2.template = ItemService.gI().getTemplate(saoPhaLeC1.template.id + 975);
            saoPhaLeC2.itemOptions.add(new ItemOption(30, 0));
            saoPhaLeC2.itemOptions.add(new ItemOption(87, 0));
            CombineService.gI().sendEffectCombineItem(player, (byte) 7, (short) saoPhaLeC2.template.iconID, (short) -1);
            InventoryService.gI().addItemBag(player, saoPhaLeC2);
            Util.setTimeout(() -> {
                Service.gI().sendServerMessage(player, "Bạn nhận được " + saoPhaLeC2.template.name);
                CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con nhé");
            }, 2000);
        } else {
            CombineService.gI().sendEffectCombineItem(player, (byte) 8, (short) -1, (short) -1);
            Util.setTimeout(() -> {
                CombineService.gI().baHatMit.npcChat(player, "Chúc con may mắn lần sau, đừng buồn con nhé");
            }, 2000);
        }
        player.inventory.subGemAndRuby(50);
        player.inventory.gold -= 100_000_000;
        InventoryService.gI().subQuantityItemsBag(player, hematite, 1);
        InventoryService.gI().subQuantityItemsBag(player, saoPhaLeC1, 1);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
