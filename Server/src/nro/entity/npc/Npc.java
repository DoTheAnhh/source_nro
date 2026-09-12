package nro.entity.npc;

import nro.service.npc.NpcFactory;
import nro.core.consts.ConstNpc;
import nro.entity.map.Map;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.server.Manager;
import nro.net.io.Message;
import nro.service.MapService;
import nro.service.Service;
import nro.core.log.Logger;
import nro.core.util.Util;
import java.util.ArrayList;
import java.util.List;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.inventory.InventoryService;

public abstract class Npc implements IAtionNpc {

    public int mapId;
    public Map map;

    public int status;

    public int cx;

    public int cy;

    public int tempId;

    public int avartar;

    /** Mẫu NPC mượn hình; -1 là dùng hình của chính mình. */
    public int resId = -1;

    /** Avatar gốc của mẫu {@link #tempId}, để bỏ mượn hình thì trả lại. */
    private int avatarGoc;

    public BaseMenu baseMenu;

    public int indexChat;

    public int timeChat;

    public long lastChatTime;

    public Npc(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        this.map = MapService.gI().getMapById(mapId);
        this.mapId = mapId;
        this.status = status;
        this.cx = cx;
        this.cy = cy;
        this.tempId = tempId;
        this.avartar = avartar;
        this.avatarGoc = avartar;
        Manager.NPCS.add(this);
    }

    /**
     * Id mẫu NPC mà client dùng để vẽ, và gửi lại khi bấm vào NPC. Mượn hình thì
     * là mẫu mượn, không thì chính {@link #tempId}. Mọi gói gửi id NPC xuống
     * client phải dùng id này — gửi id thật thì client không tìm thấy NPC nào
     * mang id đó trên bản đồ.
     */
    public int idHien() {
        return resId >= 0 ? resId : tempId;
    }

    /**
     * Mượn hình (và avatar) của mẫu NPC khác, <b>giữ nguyên</b> hành vi của mình:
     * menu, cửa hàng, nhiệm vụ vẫn theo {@link #tempId}. {@code -1} hoặc trùng id
     * của chính mình là bỏ mượn.
     */
    public void muonHinh(int res) {
        if (res < 0 || res == tempId || Manager.NPC_TEMPLATES == null
                || res >= Manager.NPC_TEMPLATES.size() || Manager.NPC_TEMPLATES.get(res) == null) {
            this.resId = -1;
            this.avartar = this.avatarGoc;
            return;
        }
        this.resId = res;
        this.avartar = Manager.NPC_TEMPLATES.get(res).avatar;
    }

    public void initBaseMenu(String text) {
        text = text.substring(1);
        String[] data = text.split("\\|");
        baseMenu = new BaseMenu();
        baseMenu.npcId = idHien();
        baseMenu.npcSay = data[0].replaceAll("<>", "\n");
        baseMenu.menuSelect = new String[data.length - 1];
        for (int i = 0; i < baseMenu.menuSelect.length; i++) {
            baseMenu.menuSelect[i] = data[i + 1].replaceAll("<>", "\n");
        }
    }

    public void createOtherMenu(Player player, int indexMenu, String npcSay, String... menuSelect) {
        Message msg;
        try {
            if(player == null || player.iDMark == null){
                Service.gI().sendThongBao(player, "...");
                return;
            }
            nro.core.log.Logger.info("MENU NPC", "Gui menu cho " + player.name + " | npc = " + tempId + " | indexMenu = " + indexMenu + " | so muc = " + menuSelect.length);
            player.iDMark.setIndexMenu(indexMenu);
            // Nút THÊM chỉ nối vào MENU GỐC. Menu con có số mục khác nhau, nối
            // vào đó thì vị trí lệch và NPC chạy nhầm chức năng.
            java.util.List<nro.repository.dao.NpcMenuThemDAO.Nut> nutThem =
                    indexMenu == nro.core.consts.ConstNpc.BASE_MENU
                            ? nro.repository.dao.NpcMenuThemDAO.dangBat(tempId)
                            : new java.util.ArrayList<>();
            player.menuGocSoLuong = menuSelect.length;
            player.menuNutThem = nutThem;

            msg = new Message(32);
            msg.writer().writeShort(idHien());
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length + nutThem.size());
            for (int i = 0; i < menuSelect.length; i++) {
                // Cho phép admin đổi tên mục trên panel. Đây là chỗ DUY NHẤT
                // mọi menu NPC đi qua, nên đổi ở đây là áp cho tất cả NPC —
                // không phải sửa từng lớp NPC một.
                msg.writer().writeUTF(nro.repository.dao.NpcMenuDAO.doiTen(
                        tempId, indexMenu, i, menuSelect[i]));
            }
            for (nro.repository.dao.NpcMenuThemDAO.Nut n : nutThem) {
                msg.writer().writeUTF(n.ten.replace("\\n", "\n"));
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createOtherMenu(Player player, int indexMenu, String npcSay, String[] menuSelect, Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(idHien());
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (int i = 0; i < menuSelect.length; i++) {
                // Cho phép admin đổi tên mục trên panel. Đây là chỗ DUY NHẤT
                // mọi menu NPC đi qua, nên đổi ở đây là áp cho tất cả NPC —
                // không phải sửa từng lớp NPC một.
                msg.writer().writeUTF(nro.repository.dao.NpcMenuDAO.doiTen(
                        tempId, indexMenu, i, menuSelect[i]));
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
            try {
                if (baseMenu != null) {
                    // Di qua createOtherMenu chu khong goi thang baseMenu: chi
                    // duong do moi co doi ten muc (NpcMenuDAO) va nut them
                    // (NpcMenuThemDAO). Goi thang thi moi NPC dung menu trong
                    // CSDL deu khong nhan duoc hai thu do.
                    createOtherMenu(player, ConstNpc.BASE_MENU, baseMenu.npcSay,
                            baseMenu.menuSelect);
                } else {
                    // Menu mac dinh cung di qua createOtherMenu.
                    //
                    // NPC chua khai menu trong CSDL (nhu Goku SSJ o Dao Kame)
                    // truoc day tu ghi goi 32 o day, nen KHONG nhan duoc nut
                    // them cua panel: gan nut cho no thi trong game van chi thay
                    // moi "Tu choi".
                    createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ta có thể giúp gì cho ngươi ?", "Từ chối");
                }
            } catch (Exception e) {
                Logger.logException(Npc.class, e);
            }
        }
    }

    public void npcChat(Player player, String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(idHien());
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void npcChat(Zone zone, String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(idHien());
            msg.writer().writeUTF(text);
            Service.gI().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void npcChat(String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(idHien());
            msg.writer().writeUTF(text);
            for (Zone zone : map.zones) {
                Service.gI().sendMessAllPlayerInMap(zone, msg);
            }
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }
        
    public boolean canOpenNpc(Player player) {
        if (this.tempId == ConstNpc.DAU_THAN) {
            if (player.zone.map.mapId == 21
                    || player.zone.map.mapId == 22
                    || player.zone.map.mapId == 23) {
                return true;
            } else {
                Service.gI().hideWaitDialog(player);
                Service.gI().sendThongBao(player, "Không thể thực hiện");
                return false;
            }
        }
        if (player.zone.map.mapId == this.mapId && (Util.getDistance(this.cx, this.cy, player.location.x, player.location.y) <= 60 || !MapService.gI().isMapBlackBallWar(mapId))) {
            player.iDMark.setNpcChose(this);
            return true;
        } else if (this.tempId == ConstNpc.LY_TIEU_NUONG) {
            return true;
        } else {
            Service.gI().hideWaitDialog(player);
            Service.gI().sendThongBao(player, "Không thể thực hiện khi đứng quá xa");
            return false;
        }
    }
    
    public void giveCapsuleItem(Player pl, int type) {
        Item capsule = InventoryService.gI().findItemBag(pl, 1655);
        if (capsule == null) {
            Service.gI().sendThongBao(pl, "Không tìm thấy Capsule trong hành trang.");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(pl, capsule, 1);

        int[][][] items = {
            { {0, 0}, {1, 1}, {2, 2}, {0, 33}, {1, 41}, {2, 49}, {0, 3}, {1, 4}, {2, 5}, {0, 34}, {0, 136}, {0, 137}, {0, 138}, {0, 139}, {0, 230}, {0, 231}, {0, 232}, {0, 233}, 
              {1, 42}, {1, 152}, {1, 153}, {1, 154}, {1, 155}, {1, 234}, {1, 235}, {1, 236}, {1, 237}, {2, 50}, {2, 168}, {2, 169}, {2, 170}, {2, 171}, {2, 238}, {2, 239}, 
              {2, 240}, {2, 241} },

            { {0, 6}, {1, 7}, {2, 8}, {0, 35}, {1, 43}, {2, 51}, {0, 9}, {1, 10}, {2, 11}, {0, 36}, {0, 140}, {0, 141}, {0, 142}, {0, 143}, {0, 242}, {0, 243}, {0, 244}, {0, 245}, 
              {1, 44}, {1, 156}, {1, 157}, {1, 158}, {1, 159}, {1, 246}, {1, 247}, {1, 248}, {1, 249}, {2, 52}, {2, 172}, {2, 173}, {2, 174}, {2, 175}, {2, 250}, {2, 251}, {2, 252}, 
              {2, 253} },

            { {0, 21}, {1, 22}, {2, 23}, {0, 24}, {1, 46}, {2, 53}, {0, 37}, {1, 25}, {2, 26}, {0, 38}, {0, 144}, {0, 145}, {0, 146}, {0, 147}, {0, 254}, {0, 255}, {0, 256}, {0, 257}, 
              {1, 45}, {1, 160}, {1, 161}, {1, 162}, {1, 163}, {1, 258}, {1, 259}, {1, 260}, {1, 261}, {2, 54}, {2, 176}, {2, 177}, {2, 178}, {2, 179}, {2, 262}, {2, 263}, {2, 264}, 
              {2, 265} },

            { {0, 27}, {1, 28}, {2, 29}, {0, 30}, {1, 47}, {2, 55}, {0, 39}, {1, 31}, {2, 32}, {0, 40}, {0, 148}, {0, 149}, {0, 150}, {0, 151}, {0, 266}, {0, 267}, {0, 268}, {0, 269}, 
              {1, 48}, {1, 164}, {1, 165}, {1, 166}, {1, 167}, {1, 270}, {1, 271}, {1, 272}, {1, 273}, {2, 56}, {2, 180}, {2, 181}, {2, 182}, {2, 183}, {2, 274}, {2, 275}, {2, 276}, 
              {2, 277} },

            { {3, 12}, {3, 57}, {3, 58}, {3, 59}, {3, 184}, {3, 185}, {3, 186}, {3, 187}, {3, 278}, {3, 279}, {3, 280}, {3, 281} }
        };

        if (type < 0 || type >= items.length) {
            Service.gI().sendThongBao(pl, "Loại capsule không hợp lệ.");
            return;
        }

        List<Integer> listId = new ArrayList<>();
        for (int[] data : items[type]) {
            if (data[0] == pl.gender || data[0] == 3) {
                listId.add(data[1]);
            }
        }

        if (listId.isEmpty()) {
            Service.gI().sendThongBao(pl, "Không tìm thấy item phù hợp.");
            return;
        }

        short idItem = listId.get(Util.nextInt(0, listId.size() - 1)).shortValue();

        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống.");
            return;
        }

        Item it = ItemService.gI().createNewItem(idItem);

        List<ItemOption> ops = ItemService.gI().getListOptionItemShop(idItem);
        if (!ops.isEmpty()) {
            it.itemOptions.addAll(ops);
        }

        if (Util.isTrue(70, 100)) {
            int[] opsrand = ItemService.gI().randOptionItemKichHoat(pl.gender);
            it.itemOptions.add(new ItemOption(opsrand[0], 0));
            it.itemOptions.add(new ItemOption(opsrand[1], 0));
        } else {
            int[] opsrand = ItemService.gI().randOptionItemKichHoatNew(pl.gender);
            for (int op : opsrand) {
                it.itemOptions.add(new ItemOption(op, 0));
            }
        }
        it.itemOptions.add(new ItemOption(30, 0));
        it.itemOptions.add(new ItemOption(213, 1));
        InventoryService.gI().addItemBag(pl, it);
        InventoryService.gI().sendItemBag(pl);
        Service.gI().sendThongBao(pl, "Bạn nhận được " + it.template.name);
    }

}
