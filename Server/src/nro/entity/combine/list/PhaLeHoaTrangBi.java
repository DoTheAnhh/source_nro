package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.server.ServerNotify;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.repository.dao.TiLeSaoDAO;
import nro.service.combine.CombineService;

/**
 * Pha lê hoá trang bị — gắn thêm một ô Sao Pha Lê.
 *
 * <h2>Ba con số nay nằm trên panel</h2>
 *
 * <p>Tỉ lệ, giá vàng và giá ngọc của từng bậc đọc từ {@link TiLeSaoDAO} (tab
 * "Tỉ lệ nâng sao"). Trước đây chúng là ba hàm {@code switch} viết cứng.</p>
 *
 * <h2>Tỉ lệ hiện ra nay là tỉ lệ thật</h2>
 *
 * <p>Bản cũ bốc bằng {@code Util.isTrue(tiLe, 1000)} — trên một nghìn — trong
 * khi bảng chọn in ra "Tỉ lệ thành công: 70%". Nên mọi bậc chạy đúng một phần
 * mười con số hiện ra, và bậc 7 lên 8 (ghi 0.25) thật ra là <b>0,025%</b>: trung
 * bình bốn nghìn lần mới lên. Nay bốc trên mười nghìn với tỉ lệ nhân một trăm,
 * tức <b>phần trăm thật</b>, giữ được cả phần lẻ như 0,25%.</p>
 */
public class PhaLeHoaTrangBi {

    /**
     * Bốc một lần theo phần trăm thật, giữ được hai chữ số lẻ.
     *
     * <p>Nhân một trăm rồi bốc trên mười nghìn: 0,25% thành 25 trên 10.000. Làm
     * bằng số nguyên nên không phụ thuộc cách làm tròn số thực.</p>
     */
    private static boolean bocTheoPhanTram(double phanTram) {
        if (phanTram <= 0) {
            return false;
        }
        if (phanTram >= 100) {
            return true;
        }
        int diem = (int) Math.round(phanTram * 100);
        return Util.nextInt(10000) < diem;
    }

    private static long getGold(int star) {
        return TiLeSaoDAO.vang(star);
    }

    private static int getGem(int star) {
        return TiLeSaoDAO.ngoc(star);
    }

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "Trang bị không phù hợp");
            return;
        }
        Item item = player.combine.itemsCombine.get(0);
        if (item == null || !item.isNotNullItem()) {
            Service.gI().sendDialogMessage(player, "Trang bị không phù hợp");
            return;
        }
        if (item.isHaveOption(93)) {
            Service.gI().sendDialogMessage(player, "Trang bị có hạn sử dụng, không thể thực hiện");
            return;
        }
        if (!item.canPhaLeHoa()) {
            Service.gI().sendDialogMessage(player, "Trang bị không phù hợp");
            return;
        }
        int star = item.getOptionParam(107);
        if (star >= CombineService.MAX_STAR_ITEM) {
            Service.gI().sendDialogMessage(player, "Đã đạt số pha lê tối đa ("
                    + CombineService.MAX_STAR_ITEM + " sao)");
            return;
        }
        TiLeSaoDAO.Bac bac = TiLeSaoDAO.bac(star);
        if (bac == null || !bac.bat || bac.tiLe <= 0) {
            Service.gI().sendDialogMessage(player,
                    "Bậc ★" + star + " → ★" + (star + 1)
                    + " đang đóng, chưa nâng được.");
            return;
        }
        long gold = bac.vang;
        int gem = bac.ngoc;

        // Moi con so can biet deu nam trong KHUNG THOAI, khong nhet vao nhan
        // nut. Nhan nut chi hien ba dong ngan; nhet gia vao do thi bon nut nhin
        // giong het nhau — dung cai canh "nut nao cung ghi Nang cap 1 ngoc".
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append(item.template.name).append("\n");
        text.append(ConstFont.BOLD_DARK).append(item.getOptionInfo()).append("\n");
        text.append(ConstFont.BOLD_GREEN).append("★").append(star)
                .append(" → ★").append(star + 1).append("\n");
        text.append(ConstFont.BOLD_BLUE).append("Tỉ lệ thành công: ")
                .append(TiLeSaoDAO.tiLeChu(star)).append("%\n");
        text.append(player.inventory.gold < gold ? ConstFont.BOLD_RED : ConstFont.BOLD_BLUE)
                .append("Mỗi lần: ").append(Util.soCham(gold)).append(" vàng\n");
        text.append(player.inventory.gem < gem ? ConstFont.BOLD_RED : ConstFont.BOLD_BLUE)
                .append("Mỗi lần: ").append(gem).append(" ngọc");

        if (player.inventory.gold < gold) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "Còn thiếu\n" + Util.soCham(gold - player.inventory.gold) + "\nvàng");
            return;
        }
        if (player.inventory.gem < gem) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "Còn thiếu\n" + (gem - player.inventory.gem) + "\nngọc");
            return;
        }
        // BON muc so lan, moi muc mot nhan RIENG BIET.
        //
        // Ban cu chi co ba muc va ca ba deu mo dau bang "Nâng cấp\n<n> ngọc",
        // phan phan biet ("x100 lần") nam o dong THU BA — ma khung nut cua
        // client cat bot dong thu ba khi chu dai. Nguoi choi nhin thay ba nut
        // giong het nhau, deu ghi "Nâng cấp 1 ngọc".
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                text.toString(),
                "Đập x100", "Đập x50", "Đập x10", "Đập x1", "Từ chối");
    }

    /**
     * Đập sao, tối đa {@code n} lần, dừng ngay khi thành công.
     *
     * @param numm số lần; không truyền thì một lần
     */
    public static void phaLeHoa(Player player, int... numm) {
        int n = 1;
        if (numm.length > 0 && numm[0] > 0) {
            n = numm[0];
        }
        if (player.combine.itemsCombine.isEmpty()) {
            Service.gI().sendServerMessage(player, "Chưa đặt trang bị nào vào ô.");
            return;
        }
        Item item = player.combine.itemsCombine.get(0);
        if (item == null || !item.isNotNullItem()) {
            Service.gI().sendServerMessage(player, "Chưa đặt trang bị nào vào ô.");
            return;
        }
        if (item.isHaveOption(93)) {
            Service.gI().sendServerMessage(player, "Trang bị có hạn sử dụng, không thể thực hiện.");
            return;
        }
        if (!item.canPhaLeHoa()) {
            Service.gI().sendServerMessage(player, "Trang bị này không pha lê hoá được.");
            return;
        }
        int star = item.getOptionParam(107);
        if (star >= CombineService.MAX_STAR_ITEM) {
            Service.gI().sendServerMessage(player, "Đã đạt số pha lê tối đa.");
            return;
        }
        TiLeSaoDAO.Bac bac = TiLeSaoDAO.bac(star);
        if (bac == null || !bac.bat || bac.tiLe <= 0) {
            Service.gI().sendServerMessage(player,
                    "Bậc ★" + star + " → ★" + (star + 1) + " đang đóng.");
            return;
        }
        long gold = bac.vang;
        int gem = bac.ngoc;

        // Bao truoc SO LAN LAM DUOC, roi moi lam.
        //
        // Ban cu cu the chay va bao "sau i lan that bai, khong du ngoc" giua
        // chung. Tinh truoc thi nguoi choi biet ngay minh du cho bao nhieu lan,
        // va vong lap khong phai kiem tra tien o moi vong.
        int theoNgoc = gem > 0 ? player.inventory.gem / gem : n;
        long theoVang = gold > 0 ? player.inventory.gold / gold : n;
        int lamDuoc = (int) Math.min(n, Math.min(theoNgoc, theoVang));
        if (lamDuoc <= 0) {
            Service.gI().sendServerMessage(player, "Không đủ tiền cho một lần đập.\nCần "
                    + Util.soCham(gold) + " vàng và " + gem + " ngọc mỗi lần.");
            return;
        }
        if (lamDuoc < n) {
            Service.gI().sendServerMessage(player, "Chỉ đủ cho " + lamDuoc
                    + " lần (bấm x" + n + ").");
        }

        int num = 0;
        boolean success = false;
        for (int i = 0; i < lamDuoc; i++) {
            num = i + 1;
            player.inventory.gold -= gold;
            player.inventory.gem -= gem;
            if (bocTheoPhanTram(bac.tiLe)) {
                success = true;
                break;
            }
        }

        if (success) {
            item.addOptionParam(107, 1);
            // Loa ca may chu tu bay sao tro len. Dieu kien cu la `star > 7`,
            // ma star la so sao TRUOC khi nang va tran la 8 — nen no chi dung
            // khi star = 8, tuc luc da het nang duoc. Cau loa chua bao gio chay.
            if (star + 1 >= 7) {
                ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa "
                        + "thành công " + item.template.name + " lên " + (star + 1) + " sao pha lê");
            }
            Service.gI().sendServerMessage(player, "Lên ★" + (star + 1)
                    + " sau " + num + " lần đập.");
            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            Service.gI().sendServerMessage(player, "Đập " + num
                    + " lần chưa lên. Tỉ lệ mỗi lần " + TiLeSaoDAO.tiLeChu(star) + "%.");
            CombineService.gI().sendEffectFailCombine(player);
        }
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
