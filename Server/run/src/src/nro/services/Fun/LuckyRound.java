package nro.services.Fun;

import nro.inventory.InventoryService;
import models.Item.Item;
import nro.player.Player;
import network.io.Message;
import nro.services.Service;
import Utils.Util;
import event.EventManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import models.Item.ItemOption;
import models.Item.ItemService;

public class LuckyRound {

    private static final byte MAX_ITEM_IN_BOX = 100;

    public static final byte USING_GEM = 2;
    public static final byte USING_GOLD = 0;
    public static final byte USING_TICKET = 1;

    private static final byte PRICE_GEM = 4;
    private static final int PRICE_GOLD = 25000000;
    private static final int PRICE_TICKET = 1;
    private static final int TICKET = 821;

    private static LuckyRound instance;

    public static LuckyRound gI() {
        if (instance == null) {
            instance = new LuckyRound();
        }
        return instance;
    }

    public void openCrackBallUI(Player pl, byte type) {
        pl.iDMark.setTypeLuckyRound(type);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(type == USING_GEM ? 7337 + i : 419 + i);
            }
            msg.writer().writeByte(type); //type price
            msg.writer().writeInt(type == USING_GEM ? PRICE_GEM : PRICE_GOLD); //price
            msg.writer().writeShort(-1); //id ticket
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void openCrackBallVipUI(Player pl, byte type) {
        pl.iDMark.setTypeLuckyRound(type);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(7390);
            }
            msg.writer().writeByte(type); //type price
            msg.writer().writeInt(PRICE_TICKET); //price
            msg.writer().writeShort(TICKET); //id ticket
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void readOpenBall(Player player, Message msg) {
        try {
            msg.reader().readByte(); //type
            byte count = msg.reader().readByte();
            switch (player.iDMark.getTypeLuckyRound()) {
                case USING_GEM:
                    openBallByGem(player, count);
                    break;
                case USING_GOLD:
                    openBallByGold(player, count);
                    break;
                case USING_TICKET:
                    openBallByTicket(player, count);
                    break;
            }
        } catch (IOException e) {
            switch (player.iDMark.getTypeLuckyRound()) {
                case USING_TICKET:
                    openCrackBallVipUI(player, player.iDMark.getTypeLuckyRound());
                    break;
                default:
                    openCrackBallUI(player, player.iDMark.getTypeLuckyRound());
                    break;
            }
        }
    }

    private void openBallByGem(Player player, byte count) {
        int gemNeed = (count * PRICE_GEM);
        if (player.inventory.ruby < gemNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ hồng ngọc để mở");
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                player.inventory.subRuby(gemNeed);
                List<Item> list = getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByGold(Player player, byte count) {
        int goldNeed = (count * PRICE_GOLD);
        if (player.inventory.gold < goldNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng để mở");
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                player.inventory.gold -= goldNeed;
                List<Item> list = getListItemLuckyRound(player, count, false);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByTicket(Player player, byte count) {
        int ticketNeed = (count * PRICE_TICKET);
        Item ticket = InventoryService.gI().findItemBag(player, TICKET);
        if (ticket == null || ticket.quantity < ticketNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + ItemService.gI().createNewItem((short) TICKET).template.name + " để quay");
            sendReward(player, new ArrayList<>());
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                InventoryService.gI().subQuantityItemsBag(player, ticket, ticketNeed);
                InventoryService.gI().sendItemBag(player);
                List<Item> list = getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void sendReward(Player player, List<Item> items) {
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.iconID);
            }
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void addItemToBox(Player player, List<Item> items) {
        player.inventory.itemsBoxCrackBall.addAll(items);
    }
    
    
    public List<Item> getListItemLuckyRound(Player player, int num, boolean vip) {
        List<Item> list = new ArrayList<>();
        List<Supplier<Item>> itemPool = buildItemPool(vip);

        for (int i = 0; i < num; i++) {
            Item it = null;
            boolean success = Util.isTrue(vip ? 60 : 50, 100);

            if (success && !itemPool.isEmpty()) {
                for (int attempt = 0; attempt < 5 && it == null; attempt++) {
                    it = itemPool.get(Util.nextInt(itemPool.size())).get(); // thử tối đa 5 lần
                }
            }

            if (it == null) {
                it = ItemService.gI().createNewItem((short) 189);
                it.quantity = Util.nextInt(5, 30) * 1000;
            }

            list.add(it);
        }

        return list;
    }
    
    private void addEventItemMultipleTimes(List<Supplier<Item>> pool, Supplier<Item> supplier, int times) {
        for (int i = 0; i < times; i++) {
            pool.add(supplier);
        }
    }

    private List<Supplier<Item>> buildItemPool(boolean vip) {
        List<Supplier<Item>> pool = new ArrayList<>();

        // === Vật phẩm mặc định ===
        pool.add(() -> ItemService.gI().createNewItem((short) 17));
        pool.add(() -> createItemWithOptions(1143, new int[][]{{30, 0}}));
        pool.add(() -> createItemWithOptions(1173, new int[][]{{30, 0}}));

        if (vip) {
            pool.add(() -> createRandomItem(new int[]{1150, 1151, 1152, 1153, 1154}, 86));
            pool.add(() -> createRandomItem(new int[]{1404, 1405, 1406, 1407, 1409, 1410, 1411, 1412, 1413}, 87));
            pool.add(() -> createItemWithOptions(1408, new int[][]{{87, 0}}));
            pool.add(() -> createItemWithOptions(2062, new int[][]{{87, 0}}));
            pool.add(() -> createItemWithOptions(2069, new int[][]{{87, 0}}));
            pool.add(() -> createItemWithOptionsMulti(new int[]{840, 841, 842, 859, 956}, new int[][]{{87, 0}, {30, 0}}));
            pool.add(() -> createRandomItem(new int[]{1517, 1518}, 87));
        } else {
            pool.add(() -> createRandomItem(new int[]{18, 19, 20}));
            pool.add(() -> createRandomItem(new int[]{663, 664, 665, 666, 667}));
            pool.add(() -> createRandomItem(new int[]{381, 382, 383, 384, 385}, 86));
            pool.add(() -> createSpecialItem(new int[][]{{220, 68}, {221, 70}, {222, 69}, {223, 71}, {224, 67}}));
            pool.add(() -> createSpecialItemWithValue(new int[][]{
                {441, 95}, {442, 96}, {443, 97}, {444, 98}, {445, 99}, {446, 100}, {447, 101}
            }));
            pool.add(() -> createRandomItem(new int[]{2063, 2064, 2065, 2066, 2067, 2068}, 87));
        }

        // === Vật phẩm sự kiện (chỉ thêm nếu khác null) ===
        if (EventManager.LUNNAR_NEW_YEAR) {
            Item e = LuckyRoundEventItems.getLunarNewYearItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getLunarNewYearItem(vip), 3);
        }
        if (EventManager.CHRISTMAS) {
            Item e = LuckyRoundEventItems.getChristmasItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getChristmasItem(vip), 3);
        }
        if (EventManager.VU_LAN_FESTIVAL) {
            Item e = LuckyRoundEventItems.getVuLanItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getVuLanItem(vip), 3);
        }
        if (EventManager.HALLOWEEN) {
            Item e = LuckyRoundEventItems.getHalloweenItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getHalloweenItem(vip), 4); // tăng tỷ lệ hơn
        }
        if (EventManager.INTERNATIONAL_WOMANS_DAY) {
            Item e = LuckyRoundEventItems.getInternationalWomensDayItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getInternationalWomensDayItem(vip), 3);
        }
        if (EventManager.TRUNG_THU) {
            Item e = LuckyRoundEventItems.getTrungThuItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getTrungThuItem(vip), 3);
        }
        if (EventManager.HUNG_VUONG) {
            Item e = LuckyRoundEventItems.getHungVuongItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getHungVuongItem(vip), 3);
        }
        if (EventManager.BLACK_FRIDAY) {
            Item e = LuckyRoundEventItems.getBlackFridayItem(vip);
            if (e != null) addEventItemMultipleTimes(pool, () -> LuckyRoundEventItems.getBlackFridayItem(vip), 3);
        }

        return pool;
    }

    // Hàm tạo item có option (1 option)
    private Item createItemWithOptions(int id, int[][] options) {
        Item item = ItemService.gI().createNewItem((short) id);
        for (int[] opt : options) {
            item.itemOptions.add(new ItemOption(opt[0], opt[1]));
        }
        return item;
    }

    // Hàm tạo item từ danh sách itemId và gán option (1 option)
    private Item createRandomItem(int[] itemIds, int optionId) {
        int itemid = itemIds[Util.nextInt(itemIds.length)];
        return createItemWithOptions(itemid, new int[][]{{optionId, 0}});
    }

    // Hàm tạo item từ danh sách không option
    private Item createRandomItem(int[] itemIds) {
        int itemid = itemIds[Util.nextInt(itemIds.length)];
        return ItemService.gI().createNewItem((short) itemid);
    }

    // Hàm tạo item với nhiều option (VD: item cao cấp)
    private Item createItemWithOptionsMulti(int[] itemIds, int[][] options) {
        int itemid = itemIds[Util.nextInt(itemIds.length)];
        return createItemWithOptions(itemid, options);
    }

    private Item createSpecialItem(int[][] data) {
        int[] entry = data[Util.nextInt(data.length)];
        return createItemWithOptions(entry[0], new int[][]{{entry[1], 0}});
    }

    private Item createSpecialItemWithValue(int[][] data) {
        int[] entry = data[Util.nextInt(data.length)];
        int value = (entry[1] == 98 || entry[1] == 99) ? 3 : 5;
        return createItemWithOptions(entry[0], new int[][]{{entry[1], value}});
    }
    
}