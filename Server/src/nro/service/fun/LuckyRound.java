package nro.service.fun;

import nro.service.inventory.InventoryService;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.service.Service;
import nro.core.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import nro.entity.item.ItemOption;
import nro.service.item.ItemService;

public class LuckyRound {

    private static final byte MAX_ITEM_IN_BOX = 100;

    public static final byte USING_GEM = 2;
    public static final byte USING_GOLD = 0;
    public static final byte USING_TICKET = 1;

    private static final byte PRICE_GEM = 4;
    private static final int PRICE_GOLD = 25000000;
    private static final int PRICE_TICKET = 1;
    private static final int TICKET = 821;

    public static final short THOI_VANG_ID = 457;

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
            msg.writer().writeByte(type); // type price
            msg.writer().writeInt(type == USING_GEM ? PRICE_GEM : PRICE_GOLD); // price
            msg.writer().writeShort(-1); // id ticket
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
            msg.writer().writeByte(type); // type price
            msg.writer().writeInt(PRICE_TICKET); // price
            msg.writer().writeShort(TICKET); // id ticket
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
            msg.reader().readByte(); // type
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

    // ===== QUAY BẰNG THỎI VÀNG =====
    public void quayBangThoiVang(Player player, int times) {
        if (player == null || player.inventory == null) {
            return;
        }

        if (times <= 0) {
            return;
        }

        if (times + player.inventory.itemsBoxCrackBall.size() > MAX_ITEM_IN_BOX) {
            Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            return;
        }

        Item tv = InventoryService.gI().findItemBag(player, THOI_VANG_ID);

        if (tv == null || tv.quantity < times) {
            Service.gI().sendThongBao(player, "Bạn không đủ thỏi vàng " + THOI_VANG_ID + " Cần " + times + " thỏi vàng!");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, tv, times);
        InventoryService.gI().sendItemBag(player);

        List<Item> list = getListItemLuckyRound(player, times, false);
        addItemToBox(player, list);
        sendReward(player, list);

        Service.gI().sendThongBao(player, "Đã quay x" + times + " bằng thỏi vàng!");
    }

    private void openBallByGem(Player player, byte count) {
        int gemNeed = count * PRICE_GEM;

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
        int goldNeed = count * PRICE_GOLD;

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
        int ticketNeed = count * PRICE_TICKET;
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

    /**
     * Bốc {@code num} món cho một lượt quay.
     *
     * <h2>Hai nguồn quà, chọn bằng một công tắc</h2>
     *
     * <p>Mặc định vẫn là <b>danh sách viết trong mã</b> ngay dưới đây — đúng thứ
     * vòng quay chạy từ trước tới nay. Bảng {@code vong_quay_qua} trên panel chỉ
     * được dùng khi quy ước {@code vong_quay_panel} bật lên.</p>
     *
     * <p>Để tắt sẵn vì bản gieo đầu tiên <b>ra sai món</b>: id vật phẩm trong mã
     * cũ được tra theo <i>vị trí</i> trong bảng mẫu, mà bảng mẫu đã đổi từ lúc
     * đoạn mã ấy được viết — nên chép nguyên id sang bảng mới cho ra một danh
     * sách quà khác hẳn thứ người chơi vẫn nhận. Bật lên chỉ khi đã soát lại
     * từng dòng trên panel.</p>
     */
    public List<Item> getListItemLuckyRound(Player player, int num, boolean vip) {
        // Qua CHI lay tu bang o panel (tab "Vong quay Thuong De").
        //
        // Danh sach qua viet cung trong ma da bi bo han: moi lan muon them hay
        // bot mot mon deu phai sua ma roi dung may chu, va hai nguon qua song
        // song thi khong ai biet nguoi choi dang quay trung cai nao.
        List<Item> list = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            Item it = null;
            boolean success = Util.isTrue(vip ? 60 : 50, 100);

            if (success) {
                for (int attempt = 0; attempt < 5 && it == null; attempt++) {
                    it = nro.repository.dao.VongQuayDAO.boc(vip);
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

}
