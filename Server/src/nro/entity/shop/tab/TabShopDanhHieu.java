package nro.entity.shop.tab;

import java.util.ArrayList;
import nro.entity.player.Player;

/**
 * Tab "nhận danh hiệu" của NPC.
 *
 * <p>Nội dung dựng từ <b>bảng danh hiệu của panel</b>, không phải từ các dòng
 * {@code item_shop} — xem {@code DanhHieuShopService} để biết vì sao.</p>
 */
public class TabShopDanhHieu extends TabShop {

    public TabShopDanhHieu(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;
        this.itemShops.addAll(
                nro.service.badges.DanhHieuShopService.tabNhan(player, this));
    }
}
