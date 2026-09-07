package nro.entity.shop.tab;

import nro.entity.player.Player;
import java.util.ArrayList;
import java.util.List;
import nro.entity.item.ItemOption;
import nro.service.badges.BadgesTaskService;
import nro.entity.badges.BagesTemplate;
import nro.entity.shop.ItemShop;

public class TabShopDanhHieu extends TabShop {

    public TabShopDanhHieu(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == player.gender || itemShop.temp.gender > 2) {
                boolean shouldAdd = true;
                for (Integer i : BagesTemplate.listEffect(player)) {
                    if (itemShop.temp.id == i) {
                        shouldAdd = false;
                        break;
                    }
                }
                if (shouldAdd) {
                    List<ItemOption> listOptionBackup = new ArrayList<>(itemShop.options);
                    itemShop.options.clear();
                    int percent = BadgesTaskService.sendPercenBadgesTask(player, BagesTemplate.fineIdEffectbyIdItem(itemShop.temp.id));
                    if (percent != 0) {
                        boolean optionExists = false;
                        for (ItemOption option : listOptionBackup) {
                            if (option.optionTemplate.id == 220) {
                                optionExists = true;
                                option.param = percent;
                                break;
                            }
                        }
                        if (!optionExists) {
                            itemShop.options.add(new ItemOption(220, percent));
                        }
                    }
                    itemShop.options.addAll(listOptionBackup);
                    this.itemShops.add(new ItemShop(itemShop));
                }
            }
        }
    }
}





