package nro.service.badges;

import java.util.ArrayList;
import java.util.List;
import nro.entity.badges.BadgesData;
import nro.entity.badges.BadgesTask;
import nro.entity.badges.BadgesTaskTemplate;
import nro.entity.badges.BagesTemplate;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.entity.shop.ItemShop;
import nro.entity.template.ItemOptionTemplate;
import nro.repository.dao.ChiSoOptionDAO;
import nro.server.Manager;
import nro.service.item.ItemService;

/**
 * Dựng hai tab danh hiệu của NPC <b>từ bảng danh hiệu trên panel</b>.
 *
 * <h2>Vì sao phải viết lại</h2>
 *
 * <p>Hai tab ấy vốn dựng từ các dòng {@code item_shop} — tức là muốn một danh
 * hiệu hiện ra trong game thì <b>ngoài</b> việc thêm nó ở tab "Danh hiệu" của
 * panel, còn phải nhớ thêm một dòng cửa hàng nữa. Quên bước hai thì danh hiệu
 * có thật, cấp tay được, cộng chỉ số được, chỉ là <b>không ai nhìn thấy nó</b> —
 * và không có gì báo vì sao.</p>
 *
 * <p>Nay danh sách dựng thẳng từ {@code Manager.BAGES_TEMPLATES}, chính là thứ
 * {@code DanhHieuDAO} đổ vào từ bảng của panel. Thêm một dòng trên panel là nó
 * có mặt trong game ngay lần mở NPC kế tiếp, không phải làm gì thêm.</p>
 *
 * <h2>Đã đủ điều kiện thì phải nhìn ra ngay</h2>
 *
 * <p>Bản cũ chỉ hiện dòng "Hoàn thành #%" khi phần trăm <b>khác 0</b>, và ở mức
 * 100% thì nó vẫn là một dòng chữ xám như mọi dòng khác. Người chơi phải bấm
 * vào từng cái mới biết cái nào lấy được. Nay mỗi danh hiệu luôn có một dòng
 * trạng thái, và cái đã đủ điều kiện được đánh dấu bằng một dòng riêng.</p>
 */
public final class DanhHieuShopService {

    private DanhHieuShopService() {
    }

    /** Chỉ số "Hoàn thành #%" vốn đã có sẵn trong bảng. */
    private static final int CHI_SO_PHAN_TRAM = 220;

    /** Chỉ số "HSD # ngày" vốn đã có sẵn. */
    private static final int CHI_SO_HAN_DUNG = 93;

    private static final String TEN_DA_DU = "$★ ĐÃ ĐỦ ĐIỀU KIỆN — bấm để nhận";
    private static final String TEN_CHUA_MO = "$Chưa mở — xem cách nhận ở bảng nhiệm vụ";
    private static final String TEN_DANG_DEO = "$★ ĐANG ĐEO — bấm lại để bỏ";

    /**
     * Id của ba dòng chữ trạng thái, cấp một lần rồi nhớ luôn.
     *
     * <p>{@code -1} nghĩa là chưa tra. Tra <b>theo tên</b> chứ không gõ cứng id:
     * id do {@code ChiSoOptionDAO} cấp theo thứ tự bảng, mỗi máy chủ một khác
     * tuỳ đã thêm bao nhiêu chỉ số trước đó.</p>
     */
    private static volatile int idDaDu = -1;
    private static volatile int idChuaMo = -1;
    private static volatile int idDangDeo = -1;

    /**
     * Tra id của một dòng chữ trạng thái, thêm vào bảng nếu chưa có.
     *
     * <p>Dùng {@code type} 9 — dòng chỉ để đọc, không cộng chỉ số nào. Tên bắt
     * đầu bằng {@code $} nên client in nó khác màu với các dòng chỉ số thật;
     * đó chính là phần "làm nổi lên" mà không cần sửa client.</p>
     */
    private static int idChiSo(String ten) {
        if (Manager.ITEM_OPTION_TEMPLATES != null) {
            for (int i = 0; i < Manager.ITEM_OPTION_TEMPLATES.size(); i++) {
                ItemOptionTemplate t = Manager.ITEM_OPTION_TEMPLATES.get(i);
                if (t != null && ten.equals(t.name)) {
                    return i;
                }
            }
        }
        return ChiSoOptionDAO.them(ten, 9);
    }

    private static synchronized void damBaoChiSo() {
        if (idDaDu < 0) {
            idDaDu = idChiSo(TEN_DA_DU);
        }
        if (idChuaMo < 0) {
            idChuaMo = idChiSo(TEN_CHUA_MO);
        }
        if (idDangDeo < 0) {
            idDangDeo = idChiSo(TEN_DANG_DEO);
        }
    }

    /** Thêm một dòng chữ trạng thái, bỏ qua lặng lẽ nếu chưa cấp được id. */
    private static void themDong(List<ItemOption> ds, int idChiSo) {
        if (idChiSo >= 0 && Manager.ITEM_OPTION_TEMPLATES != null
                && idChiSo < Manager.ITEM_OPTION_TEMPLATES.size()) {
            ds.add(new ItemOption(idChiSo, 0));
        }
    }

    /**
     * Tiến độ của nhiệm vụ trao danh hiệu này, tính theo phần trăm.
     *
     * @return {@code -1} nếu không có nhiệm vụ nào trao danh hiệu này
     */
    private static int tienDo(Player player, int idEffect) {
        if (player.dataTaskBadges == null) {
            return -1;
        }
        int cao = -1;
        for (BadgesTask d : player.dataTaskBadges) {
            if (d.idBadgesReward != idEffect) {
                continue;
            }
            int p = d.isDone() ? 100 : d.getPercentProcess();
            if (p > cao) {
                cao = p;
            }
        }
        return cao;
    }

    /** Người chơi đã sở hữu danh hiệu này chưa. */
    private static boolean daCo(Player player, int idEffect) {
        if (player.dataBadges == null) {
            return false;
        }
        for (BadgesData d : player.dataBadges) {
            if (d.idBadGes == idEffect) {
                return true;
            }
        }
        return false;
    }

    private static boolean dangDeo(Player player, int idEffect) {
        if (player.dataBadges == null) {
            return false;
        }
        for (BadgesData d : player.dataBadges) {
            if (d.idBadGes == idEffect && d.isUse) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dựng một dòng cửa hàng từ một danh hiệu của panel.
     *
     * @return {@code null} nếu danh hiệu trỏ vào một vật phẩm không có thật —
     *         bỏ qua chứ không để nó làm vỡ cả bảng
     */
    private static ItemShop dongCoBan(BagesTemplate t, nro.entity.shop.tab.TabShop tab) {
        if (t == null || t.idItem < 0) {
            return null;
        }
        nro.entity.template.ItemTemplate temp;
        try {
            temp = ItemService.gI().getTemplate(t.idItem);
        } catch (Exception ex) {
            return null;
        }
        if (temp == null) {
            return null;
        }
        ItemShop is = new ItemShop();
        is.tabShop = tab;
        is.id = t.id;
        is.temp = temp;
        is.cost = 0;
        is.cost_2 = 0;
        // Gia 0 vang: hai tab nay khong ban gi, bam vao la nhan hoac deo.
        is.typeSell = 0;
        return is;
    }

    /**
     * Tab "nhận danh hiệu": mọi danh hiệu người chơi <b>chưa có</b>.
     *
     * <p>Hiện cả cái chưa đủ điều kiện — người chơi cần thấy để biết mà nhắm
     * tới. Bản cũ cũng hiện, nhưng không nói được gì về tiến độ khi tiến độ
     * bằng 0, nên nhìn hệt như một món hàng không mua được và không rõ vì sao.</p>
     */
    public static List<ItemShop> tabNhan(Player player, nro.entity.shop.tab.TabShop tab) {
        damBaoChiSo();
        List<ItemShop> ra = new ArrayList<>();
        if (Manager.BAGES_TEMPLATES == null) {
            return ra;
        }
        for (BagesTemplate t : Manager.BAGES_TEMPLATES) {
            if (daCo(player, t.idEffect)) {
                continue;
            }
            ItemShop is = dongCoBan(t, tab);
            if (is == null) {
                continue;
            }
            int p = tienDo(player, t.idEffect);
            if (p >= 100) {
                themDong(is.options, idDaDu);
                is.isNew = true;
            } else if (p >= 0) {
                is.options.add(new ItemOption(CHI_SO_PHAN_TRAM, p));
            } else {
                themDong(is.options, idChuaMo);
            }
            for (ItemOption io : t.options) {
                is.options.add(new ItemOption(io));
            }
            ra.add(is);
        }
        return ra;
    }

    /** Tab "sở hữu": những danh hiệu người chơi đang có. */
    public static List<ItemShop> tabSoHuu(Player player, nro.entity.shop.tab.TabShop tab) {
        damBaoChiSo();
        List<ItemShop> ra = new ArrayList<>();
        if (Manager.BAGES_TEMPLATES == null) {
            return ra;
        }
        for (BagesTemplate t : Manager.BAGES_TEMPLATES) {
            if (!daCo(player, t.idEffect)) {
                continue;
            }
            ItemShop is = dongCoBan(t, tab);
            if (is == null) {
                continue;
            }
            if (dangDeo(player, t.idEffect)) {
                themDong(is.options, idDangDeo);
                is.isNew = true;
            }
            int ngay = BadgesTaskService.sendDay(player, t.idEffect);
            if (ngay > 0) {
                is.options.add(new ItemOption(CHI_SO_HAN_DUNG, ngay));
            }
            for (ItemOption io : t.options) {
                is.options.add(new ItemOption(io));
            }
            ra.add(is);
        }
        return ra;
    }

    /** Số danh hiệu đang sở hữu, để ghi lên tên tab. */
    public static int soDangCo(Player player) {
        if (player.dataBadges == null || Manager.BAGES_TEMPLATES == null) {
            return 0;
        }
        int n = 0;
        for (BagesTemplate t : Manager.BAGES_TEMPLATES) {
            if (daCo(player, t.idEffect)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Tên nhiệm vụ đang trao danh hiệu này, để nói cho người chơi biết phải làm
     * gì. Chuỗi rỗng nghĩa là chưa có cách nào ngoài admin cấp tay.
     */
    public static String cachNhan(int idEffect) {
        StringBuilder sb = new StringBuilder();
        if (Manager.TASKS_BADGES_TEMPLATE == null) {
            return "";
        }
        for (BadgesTaskTemplate t : Manager.TASKS_BADGES_TEMPLATE) {
            if (t.idbadgesReward != idEffect) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" hoặc ");
            }
            sb.append(t.name == null || t.name.trim().isEmpty()
                    ? BadgesTaskTemplate.tenLoai(t.loai) : t.name.trim());
            if (t.count > 1) {
                sb.append(" (").append(t.count).append(" lần)");
            }
        }
        return sb.toString();
    }
}
