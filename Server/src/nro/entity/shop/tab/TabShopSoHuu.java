package nro.entity.shop.tab;

import nro.entity.player.Player;
import java.util.ArrayList;
import nro.entity.item.ItemOption;
import nro.service.badges.BadgesTaskService;
import nro.entity.badges.BagesTemplate;
import nro.entity.shop.ItemShop;

public class TabShopSoHuu extends TabShop {

    public TabShopSoHuu(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name + BagesTemplate.listEffect(player).size();

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == player.gender || itemShop.temp.gender > 2) {
                boolean shouldAdd = false;
                for (Integer i : BagesTemplate.listEffect(player)) {
                    if (itemShop.temp.id == i) {
                        shouldAdd = true;
                        break;
                    }
                }
                if (shouldAdd) {
                    for (ItemOption option : itemShop.options) {
                        if (option.optionTemplate.id == 93) {
                            option.param = BadgesTaskService.sendDay(player, BagesTemplate.fineIdEffectbyIdItem(itemShop.temp.id));
                            break;
                        }
                    }
                    this.itemShops.add(new ItemShop(itemShop));
                }
            }
        }
    }
}





