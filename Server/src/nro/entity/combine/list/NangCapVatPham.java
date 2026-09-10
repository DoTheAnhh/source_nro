package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.server.ServerNotify;
import nro.service.Service;
import nro.core.util.FormatStyle;
import nro.core.util.Util;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstTaskBadges;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.badges.BadgesTaskService;
import nro.service.combine.CombineService;

public class NangCapVatPham {

    private static int getGold(Item item) {
        int levelItem = item.getOptionParam(72);
        int levelOption = item.template.level;
        int level = levelItem + levelOption;
        if (levelOption >= 7) {
            return 10_000_000;
        } else {
            return level * 10000;
        }
    }

    private static int getDa(Item item) {
        int levelItem = item.getOptionParam(72);
        int levelOption = item.template.level;
        int level = levelItem + levelOption;
        return level + 1;
    }

    private static int getRatio(int level) {
        switch (level) {
            case 0:
                return 80;
            case 1:
                return 50;
            case 2:
                return 20;
            case 3:
                return 10;
            case 4:
                return 7;
            case 5:
                return 5;
            case 6:
                return 3;
            case 7:
                return 1;
            default:
                return 0;
        }
    }

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị và đúng loại đá nâng cấp");
            return;
        }

        Item trangBi = null;
        Item daNangCap = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.template.type < 5) {
                trangBi = item;
            } else if (item.isDaNangCap()) {
                daNangCap = item;
            }
        }
        if (trangBi == null || daNangCap == null || !trangBi.canNangCapWithNDC(daNangCap)) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị và đúng loại đá nâng cấp");
            return;
        }
        Item daBaoVe = InventoryService.gI().findItemBag(player, 987);
        Item daBaoVeKhoa = InventoryService.gI().findItemBag(player, 1143);
        int level = trangBi.getOptionParam(72);
        int gold = getGold(trangBi);
        int da = getDa(trangBi);
        if (level >= CombineService.MAX_LEVEL_ITEM) {
            Service.gI().sendDialogMessage(player, "Vật phẩm đã đạt cấp độ tối đa, không thể nâng cấp nữa");
            return;
        }
        boolean canUseDBV = level == 2 || level == 4 || level == 6 || level == 7;

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Hiện tại ").append(trangBi.template.name);
        if (level > 0) {
            text.append(" [+").append(level).append("]\n");
        } else {
            text.append("\n");
        }
        text.append(ConstFont.BOLD_DARK).append(trangBi.getOptionInfoUpgrade()).append("\n");
        text.append(ConstFont.BOLD_BLUE).append("Sau khi nâng cấp [+").append(level + 1).append("]\n");
        text.append(ConstFont.BOLD_GREEN).append(trangBi.getOptionInfoUpgradeFinal()).append("\n");
        text.append(ConstFont.BOLD_BLUE).append("Tỉ lệ thành công: ").append(getRatio(level)).append("%\n");
        text.append(daNangCap.quantity >= da ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần ").append(da).append(" ").append(daNangCap.template.name).append("\n");
        text.append(player.inventory.gold >= gold ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần ").append(Util.formatNumber(gold, FormatStyle.VIETNAMESE)).append(" vàng");
        if (canUseDBV) {
            text.append("\n");
            text.append(ConstFont.BOLD_BLUE).append("Nếu thất bại sẽ rớt xuống [+").append(level - 1).append("]\n");
            text.append(ConstFont.BOLD_RED).append("Nếu dùng đá bảo vệ sẽ không bị rớt cấp.");
        }
        if (daNangCap.quantity < da) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    text.toString(), "Còn thiếu\n" + Util.formatNumber(da - daNangCap.quantity, FormatStyle.VIETNAMESE) + " " + daNangCap.template.name);
            return;
        }

        if (player.inventory.gold < gold) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    text.toString(), "Còn thiếu\n" + Util.formatNumber(gold - player.inventory.gold, FormatStyle.VIETNAMESE) + " vàng");
            return;
        }

        if (canUseDBV && (daBaoVe != null || daBaoVeKhoa != null)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                    text.toString(), "Nâng cấp", "Nâng cấp\ndùng đá\nbảo vệ", "Đóng");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                text.toString(), "Nâng cấp", "Đóng");
    }

    /**
     * Nâng cấp trang bị bằng đá nâng cấp.
     *
     * <h2>Mọi đường từ chối đều nói ra lý do</h2>
     *
     * <p>Trước đây bảy chỗ ở đầu hàm này {@code return;} <b>trong im lặng</b>.
     * Người chơi bấm "Nâng cấp" và không có gì xảy ra: không hiệu ứng, không
     * chữ, không tiếng. Nhìn ra là nút hỏng.</p>
     *
     * <p>Những chỗ ấy không phải không bao giờ xảy ra — {@code showInfoCombine}
     * kiểm trước rồi mới hiện nút, nhưng giữa lúc xem và lúc bấm thì mọi thứ
     * đều đổi được: dùng mất viên đá, tiêu mất vàng, kéo món đồ ra khỏi ô.</p>
     */
    public static void nangCapVatPham(Player player, boolean useDBV) {
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player,
                    "Cần đúng 1 trang bị và 1 loại đá nâng cấp trong ô.");
            return;
        }

        Item trangBi = null;
        Item daNangCap = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.template.type < 5) {
                trangBi = item;
            } else if (item.isDaNangCap()) {
                daNangCap = item;
            }
        }
        if (trangBi == null || daNangCap == null || !trangBi.canNangCapWithNDC(daNangCap)) {
            Service.gI().sendThongBao(player,
                    "Trang bị và đá nâng cấp không khớp nhau.");
            return;
        }
        Item daBaoVe = InventoryService.gI().findItemBag(player, 987);
        Item daBaoVeKhoa = InventoryService.gI().findItemBag(player, 1143);
        int level = trangBi.getOptionParam(72);
        int gold = getGold(trangBi);
        int da = getDa(trangBi);

        boolean canUseDBV = level == 2 || level == 4 || level == 6 || level == 7;
        if (level >= CombineService.MAX_LEVEL_ITEM) {
            Service.gI().sendThongBao(player, "Món này đã đạt cấp tối đa (+"
                    + CombineService.MAX_LEVEL_ITEM + ").");
            return;
        }
        if (daNangCap.quantity < da) {
            Service.gI().sendThongBao(player, "Không đủ " + daNangCap.template.name
                    + " — cần " + da + ", đang có " + daNangCap.quantity + ".");
            return;
        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBao(player, "Không đủ vàng, còn thiếu "
                    + Util.soCham(gold - player.inventory.gold) + " vàng.");
            return;
        }
        if (canUseDBV && useDBV && daBaoVe == null && daBaoVeKhoa == null) {
            Service.gI().sendThongBao(player,
                    "Không có Đá Bảo Vệ trong hành trang.");
            return;
        }
        if (Util.isTrue(getRatio(level), 100)) {
            for (ItemOption io : trangBi.itemOptions) {
                if (io.isOptionCanUpgrade()) {
                    io.param += (io.param * 10 / 100);
                }
            }
            trangBi.addOptionParam(72, 1);
            CombineService.gI().sendEffectSuccessCombine(player);
            if (level > 1) {
                Service.gI().chatJustForMe(player, player, player.name + ": Vừa nâng cấp thành công " + trangBi.template.name + " lên +" + (level + 1));
            }
            if (level > 5) {
                ServerNotify.gI().notify(player.name + ": Vừa nâng cấp thành công " + trangBi.template.name + " lên +" + (level + 1));
            }
            if (level == 7) {
                BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.THANH_DAP_DO_7, 1);
            }
        } else {
            if (canUseDBV) {
                if (!useDBV || (daBaoVe == null && daBaoVeKhoa == null)) {
                    if (trangBi.template.type < 5) {
                        for (ItemOption io : trangBi.itemOptions) {
                            if (io.isOptionCanUpgrade()) {
                                io.param -= (io.param * 11 / 100);
                            }
                        }
                    }
                    trangBi.subOptionParam(72, 1);
                    trangBi.addOptionParam(209, 1);
                }
            }
            CombineService.gI().sendEffectFailCombine(player);
        }
        if (canUseDBV && useDBV) {
            if (daBaoVe != null) {
                InventoryService.gI().subQuantityItemsBag(player, daBaoVe, 1);
            } else {
                InventoryService.gI().subQuantityItemsBag(player, daBaoVeKhoa, 1);
            }
        }
        player.inventory.gold -= gold;
        InventoryService.gI().subQuantityItemsBag(player, daNangCap, da);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

}
