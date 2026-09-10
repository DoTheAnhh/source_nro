package nro.entity.shop.tab;

import java.util.ArrayList;
import nro.entity.player.Player;

/**
 * Tab "sở hữu" của NPC — các danh hiệu người chơi đang có.
 *
 * <p>Cũng dựng từ bảng danh hiệu của panel; xem {@code DanhHieuShopService}.</p>
 */
public class TabShopSoHuu extends TabShop {

    public TabShopSoHuu(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name
                + nro.service.badges.DanhHieuShopService.soDangCo(player);
        this.itemShops.addAll(
                nro.service.badges.DanhHieuShopService.tabSoHuu(player, this));
    }
}
