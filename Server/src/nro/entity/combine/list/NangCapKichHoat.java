package nro.entity.combine.list;

import java.util.ArrayList;
import java.util.List;
import nro.core.consts.ConstNpc;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.repository.dao.SetBonusDAO;
import nro.repository.dao.TiLeKichHoatDAO;
import nro.server.Manager;
import nro.service.Service;
import nro.service.combine.CombineService;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;
import nro.service.reward.RewardService;

/**
 * Nâng một món Huỷ Diệt thành một món <b>đồ Set Kích Hoạt</b> ngẫu nhiên.
 *
 * <h2>Ba thứ đều bốc, và đều lấy từ panel</h2>
 *
 * <ol>
 *   <li><b>Ô trang bị</b> — áo, quần, găng, giày, rađa; bốc đều.</li>
 *   <li><b>Bậc đồ</b> — bốc theo trọng số trong {@link TiLeKichHoatDAO}
 *       (tab "Tỉ lệ set kích hoạt"), bậc càng cao càng hiếm. Bậc cao nhất là
 *       Lưỡng Long / Jeancalic / Vàng Zealot Tướng tuỳ hành tinh.</li>
 *   <li><b>Set kích hoạt</b> — bốc trong đúng những set đã khai ở tab
 *       "Set kích hoạt" của panel, lọc theo <b>hành tinh người chơi</b>.</li>
 * </ol>
 *
 * <h2>Vì sao bỏ bảng option viết cứng</h2>
 *
 * <p>Bản cũ giữ ba mảng {@code maleOptions}, {@code femaleOptions},
 * {@code otherOptions} — mười tám con số option gõ thẳng vào mã, chia làm ba
 * cặp, bốc theo tỉ lệ 15/75/còn lại. Thêm một set mới trên panel thì đường này
 * <b>không biết gì cả</b>: set mới không bao giờ ra. Đó là hai bản danh sách
 * set song song, và bản trong mã luôn là bản cũ hơn.</p>
 *
 * <p>Chưa khai set nào cho hành tinh của người chơi thì <b>không nâng</b> và nói
 * rõ lý do, thay vì trao ra một món đồ không thuộc set nào.</p>
 */
public class NangCapKichHoat {

    private static final int GOLD_REQUIRE = 2_000_000_000; // 2 tỉ vàng

    public static boolean isDoHuyDiet(Item item) {
        return item != null && item.isNotNullItem()
                && item.template.id >= 650 && item.template.id <= 662;
    }

    // =====================================================================
    //  Bảng chọn
    // =====================================================================

    public static void showInfoCombine(Player player) {
        if (player.combine == null || player.combine.itemsCombine == null
                || player.combine.itemsCombine.size() != 1) {
            Service.gI().sendThongBaoOK(player, "Cần 1 món trang bị Huỷ Diệt để nâng cấp!");
            return;
        }
        Item huyDiet = player.combine.itemsCombine.get(0);
        if (!isDoHuyDiet(huyDiet)) {
            Service.gI().sendThongBaoOK(player, "Vật phẩm không hợp lệ, cần Huỷ Diệt!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBaoOK(player, "Cần 1 ô trống trong hành trang.");
            return;
        }
        List<SetBonusDAO.DinhNghia> setDuoc = setCuaHanhTinh(player.gender);
        if (setDuoc.isEmpty()) {
            Service.gI().sendThongBaoOK(player,
                    "Chưa khai set kích hoạt nào cho " + tenHanhTinh(player.gender)
                    + " trên panel — báo quản trị.");
            return;
        }
        if (player.inventory.gold < GOLD_REQUIRE) {
            Service.gI().sendThongBaoOK(player, "Cần " + Util.soCham(GOLD_REQUIRE)
                    + " vàng để nâng cấp, còn thiếu "
                    + Util.soCham(GOLD_REQUIRE - player.inventory.gold) + ".");
            return;
        }

        StringBuilder t = new StringBuilder();
        t.append("Nâng Huỷ Diệt thành đồ Set Kích Hoạt\n");
        t.append("Hành tinh: ").append(tenHanhTinh(player.gender)).append("\n");
        t.append("Ô trang bị, bậc đồ và set đều NGẪU NHIÊN\n");
        t.append("Cần ").append(Util.soCham(GOLD_REQUIRE)).append(" vàng\n");
        t.append("Cơ hội ra từng bậc:\n");
        for (int b = TiLeKichHoatDAO.SO_BAC - 1; b >= 0; b--) {
            double p = TiLeKichHoatDAO.phanTram(b);
            if (p <= 0) {
                continue;
            }
            t.append("  bậc ").append(b + 1).append(": ")
                    .append(String.format("%.1f", p).replace('.', ',')).append("%\n");
        }
        t.append("Số set có thể ra: ").append(setDuoc.size());

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                t.toString(), "Nâng cấp", "Huỷ");
    }

    // =====================================================================
    //  Thực hiện
    // =====================================================================

    public static void startCombine(Player player) {
        if (player.combine == null || player.combine.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Cần 1 Huỷ Diệt để nâng cấp");
            return;
        }
        Item huyDiet = player.combine.itemsCombine.get(0);
        if (!isDoHuyDiet(huyDiet)) {
            Service.gI().sendThongBao(player, "Đây không phải là trang bị Huỷ Diệt!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần 1 ô trống trong hành trang.");
            return;
        }
        if (player.inventory.gold < GOLD_REQUIRE) {
            Service.gI().sendThongBao(player, "Không đủ vàng, thiếu "
                    + Util.soCham(GOLD_REQUIRE - player.inventory.gold));
            return;
        }
        List<SetBonusDAO.DinhNghia> setDuoc = setCuaHanhTinh(player.gender);
        if (setDuoc.isEmpty()) {
            Service.gI().sendThongBao(player,
                    "Chưa khai set kích hoạt nào cho " + tenHanhTinh(player.gender)
                    + " trên panel — báo quản trị.");
            return;
        }

        // Boc mon do: o trang bi deu, bac theo trong so tren panel.
        int bac = TiLeKichHoatDAO.bocBac();
        short idMoi = bocMauDo(player.gender, bac);
        if (idMoi < 0) {
            Service.gI().sendThongBao(player, "Không bốc được mẫu đồ — báo quản trị.");
            return;
        }

        Item moi = ItemService.gI().createNewItem(idMoi);
        if (moi == null || !moi.isNotNullItem()) {
            Service.gI().sendThongBao(player,
                    "Máy chủ chưa có mẫu vật phẩm " + idMoi + " — báo quản trị.");
            return;
        }

        player.inventory.gold -= GOLD_REQUIRE;
        Service.gI().sendMoney(player);

        RewardService.gI().initBaseOptionClothes(moi.template.id, moi.template.type,
                moi.itemOptions);

        // Boc mot set trong so set da khai cho hanh tinh nay, roi gan HET cac
        // option nhan dien cua set do.
        //
        // SetClothes nhan dien set bang OPTION tren mon do, nen gan thieu mot
        // option la mon do khong tinh vao set nao.
        SetBonusDAO.DinhNghia set = setDuoc.get(Util.nextInt(setDuoc.size()));
        int daGan = 0;
        for (int optId : tachOption(set.optionIds)) {
            moi.itemOptions.add(new ItemOption(optId, 0));
            daGan++;
        }
        if (daGan == 0) {
            Service.gI().sendThongBao(player, "Set \"" + set.ten
                    + "\" chưa khai option nào — báo quản trị.");
            // Vang da tru: tra lai, vi khong trao duoc gi.
            player.inventory.gold += GOLD_REQUIRE;
            Service.gI().sendMoney(player);
            return;
        }
        moi.itemOptions.add(new ItemOption(30, 0));   // khoá

        InventoryService.gI().subQuantityItemsBag(player, huyDiet, 1);
        InventoryService.gI().addItemBag(player, moi);
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().sendEffectSuccessCombine(player);

        Service.gI().sendThongBao(player, "Nhận được " + moi.template.name
                + " (bậc " + (bac + 1) + ") — set " + set.ten + ".");
    }

    // =====================================================================

    /**
     * Bốc một mẫu đồ ở bậc {@code bac} cho hành tinh này.
     *
     * <p>Năm ô đều nhau: áo, quần, găng, giày lấy từ
     * {@code Manager.doSKHVip[hành tinh]}, rađa lấy từ
     * {@code Manager.radaSKHVip} (rađa dùng chung ba hành tinh).</p>
     *
     * @return id mẫu, hoặc {@code -1} nếu dữ liệu hỏng
     */
    private static short bocMauDo(int gender, int bac) {
        if (gender < 0 || gender >= Manager.doSKHVip.length) {
            gender = 0;
        }
        short[][] cuaHanhTinh = Manager.doSKHVip[gender];
        int o = Util.nextInt(cuaHanhTinh.length + 1);   // + 1 cho rađa
        short[] day = (o < cuaHanhTinh.length) ? cuaHanhTinh[o] : Manager.radaSKHVip;
        if (day == null || day.length == 0) {
            return -1;
        }
        // Bac vuot do dai day thi lay muc cao nhat day co — day rada co the
        // ngan hon day ao quan.
        int i = Math.min(Math.max(bac, 0), day.length - 1);
        return day[i];
    }

    /** Tên hành tinh, đúng cách viết mà {@code SetBonusDAO} dùng. */
    private static String tenHanhTinh(int gender) {
        switch (gender) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namếc";
            case 2:
                return "Xayda";
            default:
                return SetBonusDAO.KHAC;
        }
    }

    /**
     * Các set đã khai trên panel, đang bật, thuộc hành tinh của người chơi.
     *
     * <p>Set để trống hành tinh (mục "Khác") <b>cũng lấy</b>: đó là set dùng
     * chung, không riêng hành tinh nào.</p>
     */
    private static List<SetBonusDAO.DinhNghia> setCuaHanhTinh(int gender) {
        String ht = tenHanhTinh(gender);
        List<SetBonusDAO.DinhNghia> ra = new ArrayList<>();
        for (SetBonusDAO.DinhNghia d : SetBonusDAO.dinhNghia().values()) {
            if (d == null || !d.active) {
                continue;
            }
            if (d.optionIds == null || d.optionIds.trim().isEmpty()) {
                continue;
            }
            String cua = (d.hanhTinh == null || d.hanhTinh.trim().isEmpty())
                    ? SetBonusDAO.KHAC : d.hanhTinh.trim();
            if (cua.equals(ht) || cua.equals(SetBonusDAO.KHAC)) {
                ra.add(d);
            }
        }
        return ra;
    }

    /** Tách chuỗi "129,141" thành danh sách id, bỏ mọi phần không phải số. */
    private static List<Integer> tachOption(String s) {
        List<Integer> ra = new ArrayList<>();
        if (s == null) {
            return ra;
        }
        for (String p : s.split(",")) {
            String q = p.trim();
            if (q.isEmpty()) {
                continue;
            }
            try {
                ra.add(Integer.parseInt(q));
            } catch (NumberFormatException boQua) {
                // Dong khai hong thi bo qua dung dong do, khong bo ca set.
            }
        }
        return ra;
    }
}
