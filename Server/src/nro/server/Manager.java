package nro.server;

import nro.entity.card.OptionCard;
import nro.entity.card.RadarCard;
import nro.service.card.RadarService;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstMap;
import nro.data.DataGame;
import static nro.data.DataGame.MAP_MOUNT_NUM;
import nro.repository.dao.ShopDAO;
import nro.entity.clan.Clan;
import nro.entity.clan.ClanMember;
import nro.entity.intrinsic.Intrinsic;
import nro.entity.player.TestDame;
import nro.entity.item.Item;
import nro.entity.map.WayPoint;
import nro.entity.npc.Npc;
import nro.service.npc.NpcFactory;
import nro.entity.shop.Shop;
import nro.entity.skill.NClass;
import nro.entity.skill.Skill;
import nro.entity.task.SideTaskTemplate;
import nro.entity.task.SubTaskMain;
import nro.entity.task.TaskMain;
import nro.service.MapService;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import nro.entity.item.ItemOption;
import nro.entity.attribute.Attribute;
import nro.service.attribute.AttributeManager;
import nro.entity.attribute.AttributeTemplate;
import nro.service.attribute.AttributeTemplateManager;
import nro.entity.badges.BadgesTaskTemplate;
import nro.entity.badges.BagesTemplate;
import nro.entity.consignmentstore.ConsignItem;
import nro.service.consignmentstore.ConsignShopManager;
import nro.entity.clan.ClanTaskTemplate;
import nro.entity.map.EffectMap;
import nro.entity.map.Zone;
import nro.entity.npc.NonInteractiveNPC;
import nro.entity.player.Player;
import nro.service.power.CaptionManager;
import nro.service.power.PowerLimitManager;
import nro.service.tambao.TamBaoService;
import nro.entity.task.EventTaskTemplate;
import nro.entity.task.KolTaskTemplate;
import nro.entity.template.AchievementTemplate;
import nro.entity.template.ArrHead2Frames;
import nro.entity.template.BgItem;
import nro.entity.template.FlagBag;
import nro.entity.template.HeadAvatar;
import nro.entity.template.ItemOptionTemplate;
import nro.entity.template.ItemTemplate;
import nro.entity.template.MapTemplate;
import nro.entity.template.MobTemplate;
import nro.entity.template.NpcTemplate;
import nro.entity.template.Part;
import nro.entity.template.PartDetail;
import nro.entity.template.SkillTemplate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public final class Manager {

/*
 * =====================================================================
 *  Manager - kho dữ liệu tĩnh dùng chung của toàn server.
 *
 *  VAI TRÒ: nạp một lần lúc khởi động toàn bộ dữ liệu game từ CSDL và từ
 *  thư mục data/ (bản đồ, item template, quái, NPC, nhiệm vụ, nội tại,
 *  clan, shop...), rồi giữ trong bộ nhớ dưới dạng các trường `public static`
 *  để mọi nơi trong 813 file truy cập trực tiếp.
 *
 *  VÌ SAO ĐÂY LÀ ĐIỂM NGHẼN LỚN NHẤT VỀ MẶT KIẾN TRÚC:
 *
 *  1. Toàn bộ trạng thái là `public static` -> bất kỳ file nào cũng đọc và
 *     GHI được. Không thể lần ra ai đã đổi MAX_PLAYER hay thêm gì vào SHOPS.
 *
 *  2. Các danh sách là ArrayList/HashMap trần, KHÔNG đồng bộ, nhưng bị đọc
 *     từ hàng trăm thread (mỗi người chơi một thread QueueHandler, ~50 thread
 *     nền). `final` chỉ khoá THAM CHIẾU, không khoá NỘI DUNG - vẫn add/remove
 *     đồng thời được và vẫn hỏng cấu trúc bên trong như thường.
 *
 *  3. Trộn hai loại dữ liệu rất khác nhau:
 *       - Bảng tra CHỈ ĐỌC sau khi nạp (MAP_TEMPLATES, ITEM_TEMPLATES, TASKS)
 *         -> nên là danh sách bất biến, khi đó dùng đa luồng hoàn toàn an toàn.
 *       - Trạng thái SỐNG thay đổi liên tục (MAPS, CLANS, SHOPS, NPCS)
 *         -> mới thật sự cần cơ chế đồng bộ.
 *     Tách được hai nhóm này là giải quyết phần lớn rủi ro đa luồng.
 *
 *  4. Cấu hình game (MAX_PLAYER, RATE_EXP_SERVER, MAX_PER_IP, mốc thời gian
 *     sự kiện) nhúng cứng trong mã nguồn -> đổi tỉ lệ EXP phải build lại server.
 *     Đã có sẵn loadProperties() đọc file - nên chuyển hết sang đó.
 *
 *  HƯỚNG SỬA (giai đoạn 9): tách thành `GameData` (bảng tra bất biến) và
 *  `GameState` (trạng thái sống, có khoá), cấu hình đưa ra
 *  config/server.properties.
 * =====================================================================
 */

    /** Thể hiện singleton. Xem ghi chú thiếu đồng bộ ở {@link #gI()}. */
    private static Manager i;
    
    /**
     * Singleton, và <b>nạp toàn bộ dữ liệu game</b> ở lần gọi đầu tiên.
     *
     * <p>Lời gọi đầu tiên tốn vài giây (đọc CSDL + đọc file bản đồ). Các lần sau
     * gần như tức thì. Không an toàn luồng, nhưng vô hại trên thực tế vì luôn được
     * gọi lần đầu từ {@code ServerManager.init()} ở luồng khởi động đơn thread.</p>
     */
    public static Manager gI() {
        if (i == null) {
            i = new Manager();
        }
        return i;
    }
   
    public static boolean readInt = true;
    public static long LAST_TIME_UPDATE_BXH;
    public static boolean HAVE_EFFECT_NIGHT_SKY = false; 
    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = 10;
    /** Số kết nối tối đa cho mỗi IP. {@code ServerManager.canConnectWithIp()} thi hành. */
    public static int MAX_PER_IP = 3;
    /** Số người chơi online tối đa. Chủ server ({@code isFounder}) được miễn. */
    public static int MAX_PLAYER = 1000;
    /** Hệ số nhân kinh nghiệm toàn server. Nên nằm trong file cấu hình, không phải trong code. */
    public static int RATE_EXP_SERVER = 10;
    /**
     * Chế độ "chỉ lưu dữ liệu": mọi lần đăng nhập bị từ chối và
     * {@code ServerManager.init()} bỏ qua bước dọn DB. Dùng khi chạy một bản chỉ để
     * giữ dữ liệu, không phục vụ người chơi.
     */
    public static boolean LOCAL = false;
    public static boolean TEST = false;
    public static boolean DAO_AUTO_UPDATER = false;
    public static int TY_LE_NAP_THOI_VANG = 1;
    public static int TY_LE_NAP_HONG_NGOC = 1;
    // Update //
//    public static byte TNDETU = 1;// tn đệ
    public static byte TNDETU = 2;// tn đệ
    
    public static byte TNPET = 4; // tn đệ
    //ĐUA TOP//
    public static Timestamp timeSuKienDuaTop = Timestamp.valueOf("2026-03-18 20:00:00");

    public static String timeStartDuaTop = "20 giờ ngày 18/3/2026";

    public static String timeEndDuaTop = "17h00 ngày 21/3/2026";

    public static String timeEndNhanGiai = "22/3/2026";
    
    public static Player player;
    
    public static byte KHUYEN_MAI_NAP = 1;
    
    public static boolean Jake_DEBUG = false;
    
    // *** //
    /**
     * Khuôn bản đồ (chỉ đọc sau khi nạp). Chỉ số mảng chính là mapId.
     *
     * <p><b>Mảng, không phải danh sách</b>, nên {@code mapId} vượt biên là
     * {@code ArrayIndexOutOfBoundsException}. Dùng {@link #getMapTemplate(int)}
     * để được kiểm tra biên.</p>
     */
    public static MapTemplate[] MAP_TEMPLATES;
    /**
     * <b>Trạng thái sống</b> của mọi bản đồ: người chơi, quái, boss, vật phẩm rơi.
     *
     * <p>Đây là cấu trúc bị đụng tới nhiều nhất trong server, và là
     * {@code ArrayList} <b>không đồng bộ</b>. {@code final} chỉ khoá tham chiếu,
     * không khoá nội dung.</p>
     */
    public static final List<nro.entity.map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<>();
    public static final List<AchievementTemplate> ACHIEVEMENT_TEMPLATE = new ArrayList<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<ClanTaskTemplate> CLAN_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<ArrHead2Frames> ARR_HEAD_2_FRAMES = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    /**
     * Danh sách shop. <b>Là cấu trúc duy nhất trong nhóm này không phải {@code final}</b>
     * — {@link #updateShop()} thay nguyên cả danh sách.
     *
     * <p>Nghĩa là một thread có thể đang duyệt danh sách cũ trong khi tham chiếu đã
     * bị trỏ sang danh sách mới. Không hỏng ngay, nhưng người chơi sẽ thấy dữ liệu cũ.</p>
     */
    public static List<Shop> SHOPS = new ArrayList<>();
    /** Trạng thái sống của mọi clan. Bị {@code ServerManager.resetClanAllClans()} sửa lúc 00:00. */
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static final List<Item> HONGNGOC_REWARDS = new ArrayList<>();
    public static final List<Item> RUBY_REWARDS = new ArrayList<>();
    public static final List<BgItem> BG_ITEMS = new ArrayList<>();
    public static final List<BadgesTaskTemplate> TASKS_BADGES_TEMPLATE = new ArrayList<>();
    public static final List<BagesTemplate> BAGES_TEMPLATES = new ArrayList<>();
    public static final RandomCollection<Integer> ITEM_VONG_QUAY = new RandomCollection<>();
    public static final List<KolTaskTemplate> KOL_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<EventTaskTemplate> EVENT_TASKS_TEMPLATE = new ArrayList<>();
    
    /*
     * ---------------------------------------------------------------
     *  Các bảng id vật phẩm nhúng cứng bên dưới.
     *
     *  Mỗi mảng là một nhóm trang bị / nguyên liệu theo hệ (Trái Đất,
     *  Namek, Xayda) hoặc theo bộ. Chúng được nhiều hệ thống tra cứu:
     *  ghép đồ, kích hoạt set, nhiệm vụ, phần thưởng.
     *
     *  ĐÂY LÀ DỮ LIỆU, KHÔNG PHẢI CODE. Thêm một món đồ mới vào set là
     *  phải sửa Java và build lại toàn bộ server. Nên chuyển sang bảng
     *  trong CSDL hoặc file JSON (giai đoạn 8) để chỉnh được lúc chạy.
     *
     *  Quy ước tên: td = Trái Đất, nm = Namek, xd = Xayda.
     * ---------------------------------------------------------------
     */
    /** 3 hệ x 4 ô trang bị kích hoạt (áo, quần, găng, giày) - bản thường. */
    public static final short[][] TrangBiKichHoat = {{0, 6, 21, 27}, {1, 7, 22, 28}, {2, 8, 23, 29}};
    /** 3 hệ x 4 ô trang bị kích hoạt - bản VIP. */
    public static final short[][] TrangBiKichHoatVip = {{555, 556, 562, 563}, {557, 558, 564, 565}, {559, 560, 566, 567}};
    
    public static final short[] itemIds_TL = {561};
    public static final short[] itemIds_HD = {650, 652, 654, 651, 653, 655, 657, 659, 661, 658, 660, 662, 656};
    public static final byte[] itemIds_NR_SB = {14, 15, 16};
    public static final int[] itemIds_MANH_SKH = {1394, 1395, 1396, 1397, 1398};
    public static final short[] itemDC12 = {233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277};

    public static final short[] aotd = {138, 139, 230, 231, 232, 233, 555};
    public static final short[] quantd = {142, 143, 242, 243, 244, 245, 556};
    public static final short[] gangtd = {146, 147, 254, 255, 256, 257, 562};
    public static final short[] giaytd = {150, 151, 266, 267, 268, 269, 563};
    public static final short[] aoxd = {170, 171, 238, 239, 240, 241, 559};
    public static final short[] quanxd = {174, 175, 250, 251, 252, 253, 560};
    public static final short[] gangxd = {178, 179, 262, 263, 264, 265, 566};
    public static final short[] giayxd = {182, 183, 274, 275, 276, 277, 567};
    public static final short[] aonm = {154, 155, 234, 235, 236, 237, 557};
    public static final short[] quannm = {158, 159, 246, 247, 248, 249, 558};
    public static final short[] gangnm = {162, 163, 258, 259, 260, 261, 564};
    public static final short[] giaynm = {166, 167, 270, 271, 272, 273, 565};
    public static final short[] radaSKHVip = {186, 187, 278, 279, 280, 281, 561};
    
    public static final short[][][] doSKHVip = {{aotd, quantd, gangtd, giaytd}, {aonm, quannm, gangnm, giaynm}, {aoxd, quanxd, gangxd, giayxd}};
    public static final List<AchievementTemplate> ACHIEVEMENTS = new ArrayList<>();
    
    private final List<AttributeTemplate> listattr = new ArrayList<>();
    /** Thêm một khuôn chỉ số vào danh sách chung. */
    public void add(AttributeTemplate at) {
        listattr.add(at);
    }
    
  private Manager() {
    try {
        loadProperties();
    } catch (IOException ex) {
        Logger.logException(Manager.class, ex, "Lỗi load properites");
        System.exit(0);
    }

    Logger.title("KHỞI TẠO SERVER");

    AttributeTemplateManager.getInstance().load();
    PowerLimitManager.getInstance().load();
    CaptionManager.getInstance().load();

    loadAttributeServer();

    // Cot param_max cua item_shop_option: phai co TRUOC loadDatabase vi
    // ShopDAO doc cot nay ngay trong do. Goi o giua loadDatabase thi treo —
    // ho ket noi chi co mot, ma loadDatabase giu suot.
    nro.repository.dao.MapShopDAO.damBaoCotParamMax();
    // Cua hang Tranh Ngoc Namec ban bang diem san boss — mot lan, truoc loadDatabase.
    nro.repository.dao.MapShopDAO.chuyenShopNamekSangDiemSanBoss();
    // Dao Kame: tra lai NPC Tranh Ngoc Namec (muon hinh Mi Nuong) — mot lan.
    // Cot npcs hong (chuoi JSON cut) lam dut ca luot nap du lieu — chua truoc.
    nro.repository.dao.MapShopDAO.vaChuaNpcMap();
    nro.repository.dao.MapShopDAO.suaNpcDaoKame();

    this.loadDatabase();

    // Bo khoa hanh tinh cho sau cuon Sach Tuyet Ky neu tab panel dang bat.
    // Phai goi SAU loadDatabase chu khong phai trong: ho ket noi cau hinh
    // database.max=1, ma loadDatabase giu ket noi duy nhat do suot luc chay —
    // xin them mot ket noi o giua la treo 30 giay roi bao het ket noi.
    nro.repository.dao.SachTuyetKyDAO.apDungDungChung();

    NpcFactory.createNpcConMeo();
    NpcFactory.createNpcRongThieng();

    this.initMap();
    initRandomItem();

    TamBaoService.loadItem();

    Logger.connect("Finish connect Server | db=" + ConnectDB.DB_NAME);
}
    
    /**
     * Dựng bảng quay thưởng {@code ITEM_VONG_QUAY} với trọng số cho từng vật phẩm.
     *
     * <p>{@code RandomCollection} chọn ngẫu nhiên theo trọng số: trọng số càng lớn
     * thì cơ hội trúng càng cao. <b>Tỉ lệ trúng của vòng quay nằm ở đây</b> — muốn
     * chỉnh tỉ lệ phải build lại server.</p>
     */
    private void initRandomItem() {
        ITEM_VONG_QUAY.add(1, 0);
        ITEM_VONG_QUAY.add(1, 1);
        ITEM_VONG_QUAY.add(1, 2);
        ITEM_VONG_QUAY.add(1, 3);
        ITEM_VONG_QUAY.add(1, 4);
        ITEM_VONG_QUAY.add(1, 5);
        ITEM_VONG_QUAY.add(1, 6);        
    }

//    private void initMap() {
//        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
//        for (MapTemplate mapTemp : MAP_TEMPLATES) {
//            int[][] tileMap = readTileMap(mapTemp.id);
//            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
//            nro.entity.map.Map map = new nro.entity.map.Map(mapTemp.id, mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId, mapTemp.bgType, mapTemp.type, tileMap, tileTop, mapTemp.zones,
//                    mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps, mapTemp.genderType);
//            MAPS.add(map);
//            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
//            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY, mapTemp.npcRes);
//            new Thread(map, "Update map" + map.mapName).start();
//        }
////        new Thread (()-> { //giảm thread scr
////        try {
////            while (!Maintenance.isRunning) {
////                long st = System.currentTimeMillis();
////                for (nro.entity.map.Map map : MAPS) {
////                    for (Zone zone : map.zones) {
////                        try {
////                            zone.update();
////                        } catch (Exception e) {
////                        }
////                    }
////                }
////                long timeDo = System.currentTimeMillis() - st;
////                if (1000-timeDo > 0) {
////                    Thread.sleep(1000 - timeDo);
////                }
////            }
////        } catch (InterruptedException ex) {
////        }
////        },"Update maps").start();
//        new NonInteractiveNPC().initNonInteractiveNPC();
//        TestDame testdame = new TestDame();
//        testdame.initTestDame();
//
//    }
    /**
     * Dựng đối tượng bản đồ sống từ khuôn {@code MAP_TEMPLATES}.
     *
     * <p>Phải chạy <b>sau</b> {@link #loadMap()} (đã có khuôn) và <b>trước</b> khi
     * gắn boss ({@code ServerManager.startBossManagers}).</p>
     */
    private void initMap() {
    int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);

    for (MapTemplate mapTemp : MAP_TEMPLATES) {
        int[][] tileMap = readTileMap(mapTemp.id);
        int[] tileTop = tileTyleTop[mapTemp.tileId - 1];

        nro.entity.map.Map map = new nro.entity.map.Map(
                mapTemp.id,
                mapTemp.name,
                mapTemp.planetId,
                mapTemp.tileId,
                mapTemp.bgId,
                mapTemp.bgType,
                mapTemp.type,
                tileMap,
                tileTop,
                mapTemp.zones,
                mapTemp.maxPlayerPerZone,
                mapTemp.wayPoints,
                mapTemp.effectMaps,
                mapTemp.genderType
        );

        MAPS.add(map);
        map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
        map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY, mapTemp.npcRes);
    }

    new Thread(() -> {
        try {
            while (!Maintenance.isRunning) {
                long st = System.currentTimeMillis();

                for (nro.entity.map.Map map : MAPS) {
                    if (map == null || map.zones == null) {
                        continue;
                    }

                    for (Zone zone : map.zones) {
                        if (zone == null) {
                            continue;
                        }
                        try {
                            zone.update();
                        } catch (Exception e) {
                        }
                    }
                }

                long elapsed = System.currentTimeMillis() - st;
                long sleep = 1000 - elapsed;
                if (sleep < 10) {
                    sleep = 10;
                }
                Thread.sleep(sleep);
            }
        } catch (InterruptedException e) {
        }
    }, "Update maps").start();

    // Ban Tai Xiu dung chung ca may chu: mot vong dem nguoc cho tat ca.
    nro.gameplay.taixiu.TaiXiuManager.gI().batDau();
    // Khu tro choi nho: mot luong nhip chung cho moi tro chay theo phien.
    nro.gameplay.minigame.NhipMiniGame.gI()
            .dangKy(nro.gameplay.minigame.BauCuaManager.gI());
    nro.gameplay.minigame.NhipMiniGame.gI()
            .dangKy(nro.gameplay.minigame.XocDiaManager.gI());
    nro.gameplay.minigame.NhipMiniGame.gI()
            .dangKy(nro.gameplay.minigame.DuaNguaManager.gI());
    nro.gameplay.minigame.NhipMiniGame.gI().batDau();

    new NonInteractiveNPC().initNonInteractiveNPC();

    TestDame testdame = new TestDame();
    testdame.initTestDame();
}

    /**
     * Lấy khuôn bản đồ theo id, <b>có kiểm tra biên</b>.
     *
     * <p>Nên dùng hàm này thay vì truy cập thẳng {@code MAP_TEMPLATES[mapID]}.</p>
     *
     * @return khuôn bản đồ, hoặc {@code null} nếu id không hợp lệ
     */
    public static MapTemplate getMapTemplate(int mapID) {
        for (MapTemplate map : MAP_TEMPLATES) {
            if (map.id == mapID) {
                return map;
            }
        }
        return null;
    }
    
    /**
     * Nạp <b>toàn bộ</b> dữ liệu game từ CSDL vào bộ nhớ. Hàm dài nhất project
     * (khoảng 670 dòng).
     *
     * <p>Đọc lần lượt: item option, item template, quái, NPC, nhiệm vụ chính,
     * nhiệm vụ phụ, nhiệm vụ clan, nội tại (theo từng hệ), avatar, khung avatar,
     * túi cờ, lớp nhân vật, shop, clan, thành tựu, huy hiệu, phần thưởng...</p>
     *
     * <p><b>Chạy đúng một lần</b>, từ {@link #gI()}. Sau đó dữ liệu chỉ nằm trong
     * bộ nhớ; sửa thẳng trong CSDL <b>không có tác dụng</b> cho tới khi khởi động
     * lại server. Đây là lý do mọi thay đổi dữ liệu game hiện nay đều cần restart.</p>
     *
     * <p><b>Hướng cải thiện:</b> tách thành từng hàm nạp riêng cho mỗi loại
     * ({@code loadItemTemplates()}, {@code loadMobs()}...) rồi cho phép nạp lại
     * từng phần lúc chạy. Vừa dễ đọc hơn, vừa bỏ được nhu cầu restart.</p>
     */
    /**
     * Đọc một cột JSON dạng mảng của bảng bản đồ.
     *
     * <p>Cột hỏng hoặc rỗng thì trả <b>mảng rỗng</b>, không trả rỗng tuyệt đối.
     * Bản cũ ép kiểu thẳng kết quả đọc rồi gọi <code>size()</code>: một dòng dữ
     * liệu hỏng là lỗi con trỏ rỗng ném ra giữa lượt nạp, máy chủ dừng ngay tại
     * đó và không lên được.
     */
    private static JSONArray mangJson(String raw) {
        try {
            Object o = raw == null ? null : JSONValue.parse(raw.replaceAll("\\\"", ""));
            return o instanceof JSONArray ? (JSONArray) o : new JSONArray();
        } catch (Exception ex) {
            return new JSONArray();
        }
    }

    private void loadDatabase() {
        long st = System.currentTimeMillis();
        JSONArray dataArray;
        JSONObject dataObject;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = ConnectDB.getConnection();) {
            //load part
            ps = con.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");
                dataArray = mangJson(rs.getString("data"));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = mangJson(String.valueOf(dataArray.get(j)));
                    part.partDetails.add(new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) {
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.partDetails) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            Logger.success("Successfully loaded part (" + parts.size() + ")\n");
            
            //load bg item template
            ps = con.prepareStatement("select * from bg_item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BgItem bgItem = new BgItem();
                bgItem.id = rs.getInt("id");
                bgItem.layer = rs.getByte("layer");
                bgItem.dx = rs.getShort("dx");
                bgItem.dy = rs.getShort("dy");
                bgItem.idImage = rs.getShort("image_id");
                BG_ITEMS.add(bgItem);
            }
            Logger.success("Loaded bg item template (" + BG_ITEMS.size() + ") successfully\n");
            
            //load item template
            ps = con.prepareStatement("select * from item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = rs.getShort("id");
                itemTemp.type = rs.getByte("type");
                itemTemp.gender = rs.getByte("gender");
                itemTemp.name = rs.getString("name");
                itemTemp.description = rs.getString("description");
                itemTemp.level = rs.getByte("level");
                itemTemp.iconID = rs.getShort("icon_id");
                itemTemp.part = rs.getShort("part");
                itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                itemTemp.strRequire = rs.getInt("power_require");
                itemTemp.gold = rs.getInt("gold");
                itemTemp.goldSell = rs.getInt("gold_sell");
                itemTemp.gem = rs.getInt("gem");
                itemTemp.gemSell = rs.getInt("gem_sell");
                itemTemp.ruby = rs.getInt("ruby");
                itemTemp.ruby_sell = rs.getInt("ruby_sell");
                itemTemp.head = rs.getInt("head");
                itemTemp.body = rs.getInt("body");
                itemTemp.leg = rs.getInt("leg");
                itemTemp.TypeEvent = rs.getInt("TypeEvent");
                itemTemp.isGender = rs.getByte("isGender");
                itemTemp.dungDuoc = rs.getBoolean("dung_duoc");
                ITEM_TEMPLATES.add(itemTemp);
            }
            Logger.success("Successfully loaded map item template (" + ITEM_TEMPLATES.size() + ")\n");
            
            //load item option template
            // Doc CA cot type.
            //
            // Truoc day cau nay chi lay id va name, nen optionTemp.type luon la 0
            // cho toan bo 285 chi so — va ItemData gui thang truong do xuong
            // client. Ket qua: cot type trong CSDL co du lieu (0, 1, 2, 4, 5, 6,
            // 7, 9, 10) nhung KHONG BAO GIO den duoc client; client nhan 0 het.
            // Client dung type de chon font/mau khi in dong chi so, nen moi dong
            // deu ra mot mau, va sua type trong CSDL khong thay doi gi ca.
            ps = con.prepareStatement("select id, name, type from item_option_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                optionTemp.type = rs.getInt("type");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            Logger.success("Successfully loaded map item option template (" + ITEM_OPTION_TEMPLATES.size() + ")\n");
            
            //load clan
            ps = con.prepareStatement("select * from clan");
            rs = ps.executeQuery();
            while (rs.next()) {
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.name2 = rs.getString("name_2");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                clan.CongTiemNangSucManhToanBangHoi = rs.getInt("BuffExp");
                clan.LasttimeBuffExp = rs.getLong("LasttimeBuffExp");
                clan.TimeStarBuffExp = rs.getLong("TimeStarBuffExp");
                clan.loadItemsBoxClanFromSQL(rs.getString("items_box_clan"));
                clan.boss_clan_round = rs.getInt("Boss_clan");
                if (clan.level < 1) {
                    clan.level = 1;
                }
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("members"));
                for (int j = 0; j < dataArray.size(); j++) {
                    dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.memberDamage = Long.parseLong(String.valueOf(dataObject.get("member_damage")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (NumberFormatException e) {
                    }
                    clan.addClanMember(cm);
                }
                dataArray = (JSONArray) JSONValue.parse(rs.getString("thanhTichBDKB"));
                if (!dataArray.isEmpty()) {
                    clan.levelDoneBanDoKhoBau = Integer.parseInt(String.valueOf(dataArray.get(0)));
                    clan.thoiGianHoanThanhBDKB = Long.parseLong(String.valueOf(dataArray.get(1)));
                }
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_charms"));
                if (!dataArray.isEmpty()) {
                    clan.BuaTriTue = Long.parseLong(String.valueOf(dataArray.get(0)));
                    clan.BuaManhMe = Long.parseLong(String.valueOf(dataArray.get(1)));
                    clan.BuaDaTrau = Long.parseLong(String.valueOf(dataArray.get(2)));
                }
                dataArray.clear();
                CLANS.add(clan);
            }

            ps = con.prepareStatement("select id from clan order by id desc limit 1");
            rs = ps.executeQuery();
            if (rs.first()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }
            Logger.success("Loaded clan (" + CLANS.size() + ") successfully, clan next id : " + Clan.NEXT_ID + " successfully\n");
            
            //load skill
            ps = con.prepareStatement("select * from skill_template order by nclass_id, slot");
            rs = ps.executeQuery();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{")
                );
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONObject dts = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
            }
            Logger.success("Loaded new skills (" + NCLASS.size() + ") successfully\n");

            //load head avatar
            ps = con.prepareStatement("select * from head_avatar");
            rs = ps.executeQuery();
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                HEAD_AVATARS.add(headAvatar);
            }
            Logger.success("Loaded head_avatar (" + HEAD_AVATARS.size() + ") successfully\n");

            //load flag bag
            ps = con.prepareStatement("select * from flag_bag");
            rs = ps.executeQuery();
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            Logger.success("Loaded flag_bag (" + FLAGS_BAGS.size() + ") successfully\n");
            
            //load array head 2 frames
            ps = con.prepareStatement("select * from array_head_2_frames");
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int j = 0; j < dataArray.size(); j++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(j).toString()));
                }
                ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
            Logger.success("Successfully loaded arr head 2 frames (" + ARR_HEAD_2_FRAMES.size() + ")\n");

            //load intrinsic
            ps = con.prepareStatement("select * from intrinsic");
            rs = ps.executeQuery();
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT:
                        INTRINSIC_TD.add(intrinsic);
                        break;
                    case ConstPlayer.NAMEC: 
                        INTRINSIC_NM.add(intrinsic);
                        break;
                    case ConstPlayer.XAYDA: 
                        INTRINSIC_XD.add(intrinsic);
                        break;
                    default: {
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                        break;
                    }
                }
                INTRINSICS.add(intrinsic);
            }
            Logger.success("Loaded intrinsic (" + INTRINSICS.size() + ") successfully\n");

            //load task
            ps = con.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id "
                    // Thu tu cac BUOC chinh la mach nhiem vu, va TaskService xac
                    // dinh viec phai lam theo dung so thu tu buoc. Truy van cu
                    // khong rang buoc thu tu nao ca — chay dung chi vi may chu
                    // co dua ve theo khoa chinh. Doi bo may luu tru, them mot
                    // chi muc, hay chi la mot lan toi uu bang la thu tu doi, va
                    // moi nhiem vu lech buoc cung mot luc ma khong co gi bao.
                    + "ORDER BY task_main_template.id, task_sub_template.idmain");
            rs = ps.executeQuery();
            int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail");
                    TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = rs.getString("sub_name");
                subTask.maxCount = rs.getShort("max_count");
                subTask.notify = rs.getString("notify");
                subTask.npcId = rs.getByte("npc_id");
                subTask.mapId = rs.getShort("map");
                task.subTasks.add(subTask);
            }
            Logger.success("Loaded task_main_template (" + TASKS.size() + ") successfully\n");
            
            //load achievement template
            ps = con.prepareStatement("select * from achievement_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ACHIEVEMENT_TEMPLATE.add(new AchievementTemplate(rs.getString("info1"), rs.getString("info2"), rs.getInt("money"), rs.getLong("max_count")));
            }
            Logger.success("Loaded archievement_template (" + ACHIEVEMENT_TEMPLATE.size() + ") successfully\n");
            
            //load side task
            ps = con.prepareStatement("select * from side_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            Logger.success("Loaded side_task_template (" + SIDE_TASKS_TEMPLATE.size() + ") successfully\n");
            
            //load clan task
            ps = con.prepareStatement("select * from clan_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ClanTaskTemplate clanTask = new ClanTaskTemplate();
                clanTask.id = rs.getInt("id");
                clanTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                clanTask.count[0][0] = Integer.parseInt(mc1[0]);
                clanTask.count[0][1] = Integer.parseInt(mc1[1]);
                clanTask.count[1][0] = Integer.parseInt(mc2[0]);
                clanTask.count[1][1] = Integer.parseInt(mc2[1]);
                clanTask.count[2][0] = Integer.parseInt(mc3[0]);
                clanTask.count[2][1] = Integer.parseInt(mc3[1]);
                clanTask.count[3][0] = Integer.parseInt(mc4[0]);
                clanTask.count[3][1] = Integer.parseInt(mc4[1]);
                clanTask.count[4][0] = Integer.parseInt(mc5[0]);
                clanTask.count[4][1] = Integer.parseInt(mc5[1]);
                CLAN_TASKS_TEMPLATE.add(clanTask);
            }
            Logger.success("Loaded side_task_clan (" + CLAN_TASKS_TEMPLATE.size() + ") successfully\n");
            
            //load shop
            SHOPS = ShopDAO.getShops(con);
            Logger.success("Loaded shop (" + SHOPS.size() + ") successfully\n");

            //load image by name
            ps = con.prepareStatement("select name, n_frame from img_by_name");
            rs = ps.executeQuery();
            while (rs.next()) {
                IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            Logger.success("Successfully loaded images by name (" + IMAGES_BY_NAME.size() + ")\n");

            //Load mount
            for (ItemTemplate item : ITEM_TEMPLATES) {
                if (item.type == 23 && getNFrameImageByName("mount_" + item.part + "_0") != 0) {
                    MAP_MOUNT_NUM.put(item.id, (short) (item.part + 30000));
                }
            }
            Logger.success("Successfully loaded mount (" + MAP_MOUNT_NUM.size() + ")\n");
            
            // Nhiem vu KOL da go han: khong con truy van task_kol_template
            // nua, nen bang do cung khong con trong luoc do chuan.
            // load side task
            ps = con.prepareStatement("select * from task_event_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                EventTaskTemplate eventTask = new EventTaskTemplate();
                eventTask.id = rs.getInt("id");
                eventTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                eventTask.count[0][0] = Integer.parseInt(mc1[0]);
                eventTask.count[0][1] = Integer.parseInt(mc1[1]);
                eventTask.count[1][0] = Integer.parseInt(mc2[0]);
                eventTask.count[1][1] = Integer.parseInt(mc2[1]);
                eventTask.count[2][0] = Integer.parseInt(mc3[0]);
                eventTask.count[2][1] = Integer.parseInt(mc3[1]);
                eventTask.count[3][0] = Integer.parseInt(mc4[0]);
                eventTask.count[3][1] = Integer.parseInt(mc4[1]);
                eventTask.count[4][0] = Integer.parseInt(mc5[0]);
                eventTask.count[4][1] = Integer.parseInt(mc5[1]);
                EVENT_TASKS_TEMPLATE.add(eventTask);
            }
            Logger.success("Successfully loaded event task (" + EVENT_TASKS_TEMPLATE.size() + ")\n");
            
            // load task badges
            // Hai cột loai/tham_so là phần thêm của panel — bảng cũ chưa có nên
            // tự thêm, và đọc bằng try/catch để chạy được cả trên CSDL cũ.
            nro.repository.dao.NhiemVuDanhHieuDAO.damBaoCot();
            ps = con.prepareStatement("select * from task_badges_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BadgesTaskTemplate badgesTaskTemplate = new BadgesTaskTemplate();
                badgesTaskTemplate.id = rs.getInt("id");
                badgesTaskTemplate.name = rs.getString("NAME");
                badgesTaskTemplate.count = rs.getInt("maxCount");
                badgesTaskTemplate.idbadgesReward = rs.getInt("idbadgesReward");
                try {
                    String l = rs.getString("loai");
                    badgesTaskTemplate.loai = l == null || l.trim().isEmpty()
                            ? BadgesTaskTemplate.GO_CUNG : l.trim();
                    badgesTaskTemplate.thamSo = rs.getInt("tham_so");
                } catch (Exception exCot) {
                    badgesTaskTemplate.loai = BadgesTaskTemplate.GO_CUNG;
                    badgesTaskTemplate.thamSo = -1;
                }
                TASKS_BADGES_TEMPLATE.add(badgesTaskTemplate);
            }
            Logger.success("Loaded task badges (" + TASKS_BADGES_TEMPLATE.size() + ") successfully\n");
            
            ps = con.prepareStatement("select * from data_badges");
            rs = ps.executeQuery();
            while (rs.next()) {
                BagesTemplate template = new BagesTemplate();
                template.id = rs.getInt("id");
                template.idEffect = rs.getInt("idEffect");
                template.idItem = rs.getInt("idItem");
                template.NAME = rs.getString("NAME");

                JSONArray option = (JSONArray) JSONValue.parse(rs.getString("Options"));
                if (option != null) {
                    for (int u = 0; u < option.size(); u++) {
                        JSONObject jsonobject = (JSONObject) option.get(u);
                        int optionId = Integer.parseInt(jsonobject.get("id").toString());
                        int param = Integer.parseInt(jsonobject.get("param").toString());
                        template.options.add(new ItemOption(optionId, param));
                    }
                }
                BAGES_TEMPLATES.add(template);
            }
            Logger.success("Loaded badges template (" + BAGES_TEMPLATES.size() + ") successfully\n");

            //load mob template
            ps = con.prepareStatement("select * from mob_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getInt("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                mobTemp.percent_gold = rs.getByte("percent_gold");
                MOB_TEMPLATES.add(mobTemp);
            }
            Logger.success("Loaded mob template (" + MOB_TEMPLATES.size() + ") successfully\n");

            //load npc template
            ps = con.prepareStatement("select * from npc_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                NPC_TEMPLATES.add(npcTemp);
            }
            Logger.success("Loaded npc_template (" + NPC_TEMPLATES.size() + ") successfully\n");

            //load map template
            ps = con.prepareStatement("select count(id) from map_template");
            rs = ps.executeQuery();
            if (rs.next()) {
                int countRow = rs.getShort(1);
                MAP_TEMPLATES = new MapTemplate[countRow];
                ps = con.prepareStatement("select * from map_template");
                rs = ps.executeQuery();
                short y = 0;
                while (rs.next()) {
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = rs.getInt("id");
                    String mapName = rs.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    mapTemplate.type = rs.getByte("type");
                    mapTemplate.planetId = rs.getByte("planet_id");
                    mapTemplate.bgType = rs.getByte("bg_type");
                    mapTemplate.tileId = rs.getByte("tile_id");
                    mapTemplate.bgId = rs.getByte("bg_id");
                    mapTemplate.zones = rs.getByte("zones");
                    mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
                    mapTemplate.genderType = rs.getByte("genderType");
                    //load waypoints
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ",")
                    );
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = mangJson(String.valueOf(dataArray.get(j)));
                        if (dtwp.size() < 10) {
                            continue;
                        }
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    //load mobs
                    dataArray = mangJson(rs.getString("mobs"));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new int[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = mangJson(String.valueOf(dataArray.get(j)));
                        if (dtm.size() < 5) {
                            mapTemplate.mobTemp[j] = -1;
                            continue;
                        }
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        mapTemplate.mobHp[j] = Integer.parseInt(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    //load npcs
                    dataArray = mangJson(rs.getString("npcs"));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    mapTemplate.npcRes = new byte[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = mangJson(String.valueOf(dataArray.get(j)));
                        if (dtn.size() < 3) {
                            mapTemplate.npcId[j] = -1;
                            continue;
                        }
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        // Phan tu thu 4 (neu co): mau NPC muon hinh.
                        mapTemplate.npcRes[j] = dtn.size() > 3
                                ? Byte.parseByte(String.valueOf(dtn.get(3)).trim()) : mapTemplate.npcId[j];
                        dtn.clear();
                    }
                    dataArray.clear();                    
                    
                    MAP_TEMPLATES[y++] = mapTemplate;
                }
                Logger.success("Successfully loaded map template (" + MAP_TEMPLATES.length + ")\n");
            }

            //Load item ki gui
            ps = con.prepareStatement("SELECT * FROM shop_ky_gui");
            rs = ps.executeQuery();
            while (rs.next()) {
                int y = rs.getInt("id");
                int idPl = rs.getInt("player_id");
                byte tab = rs.getByte("tab");
                short itemId = rs.getShort("item_id");
                int gold = rs.getInt("gold");
                int gem = rs.getInt("gem");
                int quantity = rs.getInt("quantity");
                long isTime = rs.getLong("lasttime");
                boolean isBuy = rs.getByte("isBuy") == 1;
                List<ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new ItemOption(idOptions, param));
                }
                ConsignShopManager.gI().listItem.add(new ConsignItem(y, itemId, idPl, tab, gold, gem, quantity, isTime, op, isBuy));
            }
            Logger.log(Logger.GREEN, "Loaded shop_ky_gui (" + ConsignShopManager.gI().listItem.size() + ") successfully\n");
            
            //load notify
            ps = con.prepareStatement("select * from notify order by id desc");
            rs = ps.executeQuery();
            while (rs.next()) {
                NOTIFY.add(rs.getString("name") + "<>" + rs.getString("text"));
            }
            Logger.success("Loaded notify (" + NOTIFY.size() + ") successfully\n");

            // So suu tam (bang radar) doc qua SoSuuTamDAO: tu tao bang va cot moi,
            // sua duoc tren panel, tab "So suu tam".
            nro.repository.dao.SoSuuTamDAO.napLai();
            
            //TOP
            TopServer.Topserver_data(con);
            
        } catch (Exception e) {
            Logger.logException(Manager.class, e, "Lỗi load database");
            System.exit(0);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }
        Logger.log(Logger.PURPLE, "Tổng thời gian load Database " + (System.currentTimeMillis() - st) + "(ms)\n");
    }

    /**
     * Nạp khuôn bản đồ từ CSDL và dữ liệu ô (tile) từ thư mục {@code data/map/}.
     *
     * <p>Kết hợp hai nguồn: thuộc tính bản đồ nằm trong CSDL, còn lưới ô nằm trong
     * file nhị phân. Xem {@link #readTileMap(int)}.</p>
     */
    public void loadMap() {
    long st = System.currentTimeMillis();
    JSONArray dataArray;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try (Connection con = ConnectDB.getConnection();) {
        BG_ITEMS.clear();
        NPC_TEMPLATES.clear();

        ps = con.prepareStatement("select * from bg_item_template");
        rs = ps.executeQuery();
        while (rs.next()) {
            BgItem bgItem = new BgItem();
            bgItem.id = rs.getInt("id");
            bgItem.layer = rs.getByte("layer");
            bgItem.dx = rs.getShort("dx");
            bgItem.dy = rs.getShort("dy");
            bgItem.idImage = rs.getShort("image_id");
            BG_ITEMS.add(bgItem);
        }

        if (rs != null) rs.close();
        if (ps != null) ps.close();

        ps = con.prepareStatement("select * from npc_template");
        rs = ps.executeQuery();
        while (rs.next()) {
            NpcTemplate npcTemp = new NpcTemplate();
            npcTemp.id = rs.getByte("id");
            npcTemp.name = rs.getString("name");
            npcTemp.head = rs.getShort("head");
            npcTemp.body = rs.getShort("body");
            npcTemp.leg = rs.getShort("leg");
            npcTemp.avatar = rs.getInt("avatar");
            NPC_TEMPLATES.add(npcTemp);
        }

        if (rs != null) rs.close();
        if (ps != null) ps.close();

        ps = con.prepareStatement("select count(id) from map_template");
        rs = ps.executeQuery();
        if (rs.next()) {
            int countRow = rs.getShort(1);
            MAP_TEMPLATES = new MapTemplate[countRow];
        }

        if (rs != null) rs.close();
        if (ps != null) ps.close();

        ps = con.prepareStatement("select * from map_template");
        rs = ps.executeQuery();
        short y = 0;
        while (rs.next()) {
            MapTemplate mapTemplate = new MapTemplate();
            mapTemplate.id = rs.getInt("id");
            mapTemplate.name = rs.getString("name");
            mapTemplate.type = rs.getByte("type");
            mapTemplate.planetId = rs.getByte("planet_id");
            mapTemplate.bgType = rs.getByte("bg_type");
            mapTemplate.tileId = rs.getByte("tile_id");
            mapTemplate.bgId = rs.getByte("bg_id");
            mapTemplate.zones = rs.getByte("zones");
            mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
            mapTemplate.genderType = rs.getByte("genderType");

            dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                    .replaceAll("\\[\"\\[", "[[")
                    .replaceAll("\\]\"\\]", "]]")
                    .replaceAll("\",\"", ","));
            for (int j = 0; j < dataArray.size(); j++) {
                WayPoint wp = new WayPoint();
                JSONArray dtwp = mangJson(String.valueOf(dataArray.get(j)));
                if (dtwp.size() < 10) {
                    continue;
                }
                wp.name = String.valueOf(dtwp.get(0));
                wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                mapTemplate.wayPoints.add(wp);
            }

            dataArray = mangJson(rs.getString("mobs"));
            mapTemplate.mobTemp = new byte[dataArray.size()];
            mapTemplate.mobLevel = new byte[dataArray.size()];
            mapTemplate.mobHp = new int[dataArray.size()];
            mapTemplate.mobX = new short[dataArray.size()];
            mapTemplate.mobY = new short[dataArray.size()];
            for (int j = 0; j < dataArray.size(); j++) {
                JSONArray dtm = mangJson(String.valueOf(dataArray.get(j)));
                if (dtm.size() < 5) {
                    mapTemplate.mobTemp[j] = -1;
                    continue;
                }
                mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                mapTemplate.mobHp[j] = Integer.parseInt(String.valueOf(dtm.get(2)));
                mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
            }

            dataArray = mangJson(rs.getString("npcs"));
            mapTemplate.npcId = new byte[dataArray.size()];
            mapTemplate.npcX = new short[dataArray.size()];
            mapTemplate.npcY = new short[dataArray.size()];
            mapTemplate.npcRes = new byte[dataArray.size()];
            for (int j = 0; j < dataArray.size(); j++) {
                JSONArray dtn = mangJson(String.valueOf(dataArray.get(j)));
                if (dtn.size() < 3) {
                    mapTemplate.npcId[j] = -1;
                    continue;
                }
                mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                // Phan tu thu 4 (neu co): mau NPC muon hinh.
                mapTemplate.npcRes[j] = dtn.size() > 3
                        ? Byte.parseByte(String.valueOf(dtn.get(3)).trim()) : mapTemplate.npcId[j];
            }

            MAP_TEMPLATES[y++] = mapTemplate;
        }

    } catch (Exception e) {
        Logger.logException(Manager.class, e, "Lỗi load map");
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException ex) {
        }
    }

    Logger.log(Logger.PURPLE, "Tổng thời gian load Map " + (System.currentTimeMillis() - st) + "(ms)\n");
}

    /**
     * Đọc cấu hình server từ file properties.
     *
     * <p><b>Cơ chế đúng đã có sẵn ở đây</b> — vấn đề là phần lớn hằng số vẫn nằm
     * trong mã nguồn thay vì đi qua hàm này. Việc chuyển nốt chúng sang file là
     * thay đổi nhỏ, rủi ro thấp, lợi ích vận hành lớn.</p>
     */
    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("data/config/data_base.properties"));
        Object value;
        if ((value = properties.get("server.sv")) != null) {
            SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.name")) != null) {
            String name = String.valueOf(value);
            ServerManager.NAME = name;
        }
        if ((value = properties.get("server.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        String linkServer = "";
        if ((value = properties.get("server.ip")) != null) {
            ServerManager.IP = doDiaChiMayChu(String.valueOf(value));
            linkServer += ServerManager.NAME + ":" + ServerManager.IP + ":" + ServerManager.PORT + ":0,";
            ghiIpChoClient(properties.get("server.ipfile"), ServerManager.IP);
            // Ghi them mot ban CANH BAN CHAY cua client.
            //
            // Ban trong Assets/Resources chi co tac dung khi build lai: Unity
            // nuong ca thu muc Resources vao ban build, nen file .exe da build
            // van giu dia chi cu du server co ghi de. File canh ban chay thi
            // client doc luc chay -> doi wifi xong chi can chay lai server la
            // client tro dung dia chi, khong dung toi Unity.
            // Ghi 127.0.0.1 chu khong phai IP LAN: file nay nam CANH BAN CHAY
            // cua client, tuc la client do o cung may voi server. Loopback thi
            // khong bao gio hong — doi wifi, doi router, rut han day mang deu
            // khong anh huong; con IP LAN thi doi wifi mot cai la sai ngay, ma
            // server van chay binh thuong (no lang nghe 0.0.0.0) nen loi doc ra
            // rat kho hieu.
            ghiIpChoClient(properties.get("server.ipfile.chay"), "127.0.0.1");
            // Ban cho Unity Editor, mang IP LAN THAT.
            //
            // Trong Editor, `Application.dataPath` tro toi Mod/Assets nen client
            // tim file o Mod/server_ip.txt. Truoc day khong ai ghi file do, nen
            // ban chay trong Editor rot xuong buoc cuoi va dung 127.0.0.1 — noi
            // duoc khi may chu o cung may, nhung khong bao gio theo IP that.
            ghiIpChoClient(properties.get("server.ipfile.editor"), ServerManager.IP);
        }
        for (int j = 1; j <= 10; j++) {
            value = properties.get("server.sv" + j);
            if (value != null) {
                linkServer += String.valueOf(value) + ":0,";
            }
        }
        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.waitlogin")) != null) {
            SECOND_WAIT_LOGIN = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.maxperip")) != null) {
            MAX_PER_IP = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.maxplayer")) != null) {
            MAX_PLAYER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.expserver")) != null) {
            RATE_EXP_SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.local")) != null) {
            LOCAL = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.test")) != null) {
            TEST = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.daoautoupdater")) != null) {
            DAO_AUTO_UPDATER = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.daoautoupdater")) != null) {
            DAO_AUTO_UPDATER = String.valueOf(value).equalsIgnoreCase("true");
        }
    }

    /**
     * Địa chỉ mà máy chủ tự giới thiệu với client.
     *
     * <h2>Vì sao cần</h2>
     *
     * <p>Giá trị này đi thẳng vào danh sách máy chủ gửi xuống client, và client
     * <b>lưu lại</b> vào bộ nhớ máy ({@code NRlink3}). Gõ cứng một địa chỉ LAN
     * vào đây là chuốc lấy một cái bẫy: hôm nào DHCP cấp IP khác, hay chép cả
     * thư mục sang máy khác, hoặc tắt một card mạng ảo, là client vẫn nối vào
     * địa chỉ cũ đã chết. Nó không tự sửa được, vì muốn nhận danh sách mới thì
     * phải nối được vào máy chủ trước đã — mà nối thì không nối nổi.</p>
     *
     * <p>Triệu chứng đúng của cái bẫy đó là client báo <i>"Máy chủ tắt hoặc mất
     * sóng"</i> trong khi máy chủ vẫn chạy ngon lành.</p>
     *
     * @param khai giá trị trong {@code data_base.properties}. Để {@code auto}
     *             (hoặc bỏ trống) thì tự dò; gõ thẳng một địa chỉ thì dùng
     *             đúng địa chỉ đó, không dò gì cả.
     */
    /**
     * Ghi địa chỉ vừa dò ra file mà <b>client</b> đọc.
     *
     * <h2>Vì sao server phải ghi file cho client</h2>
     *
     * <p>Client lấy địa chỉ máy chủ từ {@code Assets/Resources/server_ip.txt}
     * (xem {@code ServerListScreen.docIpMayChu}). Trước đây file đó ghi tay,
     * nên nó ôm địa chỉ của mạng <i>cũ</i> — và server dò đúng cũng vô nghĩa
     * khi client vẫn gọi vào một địa chỉ đã chết.</p>
     *
     * <p>Ghi tự động thì đổi mạng chỉ còn hai việc: bật server, rồi build lại
     * mod. Không phải sửa file, không phải nhớ chạy {@code ipconfig}.</p>
     *
     * <p><b>Chỉ ghi khi nội dung thật sự đổi.</b> Unity theo dõi thư mục
     * {@code Assets/} và nạp lại tài nguyên mỗi lần file thay đổi; ghi lại y
     * nguyên mỗi lần khởi động sẽ làm Unity import lại không cần thiết.</p>
     *
     * @param duongDan giá trị khoá {@code server.ipfile}. Bỏ trống thì không
     *                 ghi gì — dùng khi server chạy trên VPS, nơi không có
     *                 thư mục mod nào ở cạnh.
     */
    private static void ghiIpChoClient(Object duongDan, String ip) {
        String p = duongDan == null ? "" : String.valueOf(duongDan).trim();
        if (p.isEmpty()) {
            return;
        }
        java.io.File f = new java.io.File(p);
        // String.join thay vì nối "\r\n" bằng tay: client tách file theo '\n'
        // nên kiểu xuống dòng nào cũng đọc được, và ở đây không còn ký tự
        // escape nào để viết sai.
        String noiDung = String.join(System.lineSeparator(),
                "# FILE NAY DO SERVER TU GHI - dung sua tay.",
                "# Ghi boi Manager.ghiIpChoClient() moi lan server khoi dong,",
                "# lay tu dia chi ma server tu do duoc (khoa server.ip=auto).",
                "# Muon ep cung mot dia chi thi sua server.ip trong",
                "# data/config/data_base.properties, dung sua o day.",
                ip) + System.lineSeparator();
        try {
            if (f.isFile()) {
                byte[] cu = java.nio.file.Files.readAllBytes(f.toPath());
                if (noiDung.equals(new String(cu, "UTF-8"))) {
                    Logger.info("IP", "File client da dung dia chi nay: " + p);
                    return;
                }
            }
            java.io.File cha = f.getParentFile();
            if (cha != null && !cha.isDirectory()) {
                Logger.warn("IP", "Khong co thu muc " + cha.getPath()
                        + " -> bo qua viec ghi file cho client");
                return;
            }
            java.nio.file.Files.write(f.toPath(), noiDung.getBytes("UTF-8"));
            Logger.success("IP", "Da ghi " + ip + " vao " + p);
        } catch (Exception ex) {
            // Khong ghi duoc thi server van chay binh thuong; chi la client
            // phai sua file bang tay.
            Logger.warn("IP", "Khong ghi duoc " + p + " | " + ex.getMessage());
        }
    }

    private static String doDiaChiMayChu(String khai) {
        String s = khai == null ? "" : khai.trim();
        if (!s.isEmpty() && !s.equalsIgnoreCase("auto")) {
            return s;
        }
        String tim = diaChiLanHienTai();
        Logger.success("Địa chỉ máy chủ giới thiệu cho client: " + tim
                + (s.isEmpty() || s.equalsIgnoreCase("auto")
                        ? "  (tự dò)" : ""));
        return tim;
    }

    /**
     * IPv4 mà máy chủ nên giới thiệu cho client.
     *
     * <h2>Vì sao không dùng mẹo UDP-connect nữa</h2>
     *
     * <p>Bản cũ chỉ hỏi bảng định tuyến: mở socket UDP rồi {@code connect} tới
     * {@code 8.8.8.8}, hệ điều hành chọn sẵn card sẽ dùng và
     * {@code getLocalAddress} trả về IP card đó. Mẹo này đúng khi máy chỉ có
     * một đường ra internet, nhưng <b>sai ngay khi máy có VPN</b>.</p>
     *
     * <p>Đúng cái đó đã xảy ra: máy đang bật Cloudflare WARP, nên route ra
     * ngoài đi qua card {@code CloudflareWARP} và mẹo trên trả về
     * {@code 172.16.0.2}. Đó là địa chỉ <i>bên trong</i> tunnel — không một máy
     * nào trong LAN gọi trả được, dù IP LAN thật của máy là
     * {@code 10.15.180.67}. Client nhận địa chỉ đó rồi báo "máy chủ tắt".</p>
     *
     * <h2>Cách chọn</h2>
     *
     * <p>Liệt kê mọi IPv4 rồi cho điểm, cao nhất thắng:</p>
     * <ol>
     *   <li><b>IP công cộng</b> trên card thật — đây chính là trường hợp VPS,
     *       và cũng là lý do hàm này tự chạy đúng khi chuyển lên VPS mà không
     *       phải sửa cấu hình;</li>
     *   <li>IP nội bộ {@code 192.168.*} trên card thật — mạng nhà phổ biến nhất;</li>
     *   <li>IP nội bộ {@code 10.*};</li>
     *   <li>IP nội bộ {@code 172.16-31.*};</li>
     *   <li>cùng đường thì {@code 127.0.0.1} để ít ra máy tại chỗ vẫn chơi được.</li>
     * </ol>
     *
     * <p>Bị loại thẳng: loopback, {@code 169.254.*} (địa chỉ tự phát khi card
     * không xin được DHCP — máy này có tới <b>tám</b> card như vậy), card đang
     * tắt, và card mang tên của VPN hoặc máy ảo.</p>
     *
     * <p>Ghi log cả danh sách ứng viên kèm điểm, nên về sau nhìn log là biết
     * ngay vì sao nó chọn địa chỉ đó — thay vì phải đoán.</p>
     */
    private static String diaChiLanHienTai() {
        java.util.List<String> nhatKy = new java.util.ArrayList<>();
        String tot = null;
        int diemTot = -1;

        try {
            java.util.Enumeration<java.net.NetworkInterface> ds
                    = java.net.NetworkInterface.getNetworkInterfaces();
            while (ds != null && ds.hasMoreElements()) {
                java.net.NetworkInterface card = ds.nextElement();
                String tenCard = tenDayDu(card);

                if (!card.isUp() || card.isLoopback()) {
                    continue;
                }
                boolean cardAo = laCardAo(tenCard);

                java.util.Enumeration<java.net.InetAddress> dc
                        = card.getInetAddresses();
                while (dc.hasMoreElements()) {
                    java.net.InetAddress a = dc.nextElement();
                    if (!(a instanceof java.net.Inet4Address)
                            || a.isLoopbackAddress()
                            || a.isAnyLocalAddress()
                            || a.isLinkLocalAddress()) {
                        continue;
                    }
                    String ip = a.getHostAddress();
                    int diem = chamDiem(a, cardAo);
                    nhatKy.add(ip + " (" + tenCard + ") = " + diem);
                    if (diem > diemTot) {
                        diemTot = diem;
                        tot = ip;
                    }
                }
            }
        } catch (Exception boQua) {
            // Khong doc duoc card mang -> dung 127.0.0.1 ben duoi.
        }

        for (String d : nhatKy) {
            Logger.info("IP", "  ung vien: " + d);
        }
        if (tot == null || diemTot <= 0) {
            Logger.warn("IP", "Khong tim duoc dia chi dung duoc -> dung 127.0.0.1."
                    + " Client tren may khac se KHONG noi duoc.");
            return "127.0.0.1";
        }
        return tot;
    }

    /**
     * Điểm của một địa chỉ. Càng cao càng đáng giới thiệu cho client.
     *
     * <p>Card ảo/VPN bị trừ nặng thay vì loại thẳng: nếu máy <i>chỉ</i> có card
     * loại đó thì vẫn còn hơn không có gì.</p>
     */
    private static int chamDiem(java.net.InetAddress a, boolean cardAo) {
        byte[] b = a.getAddress();
        int o1 = b[0] & 0xFF;
        int o2 = b[1] & 0xFF;

        int diem;
        if (!a.isSiteLocalAddress() && o1 != 100) {
            // IP cong cong -> gan nhu chac chan la VPS.
            diem = 100;
        } else if (o1 == 192 && o2 == 168) {
            diem = 60;
        } else if (o1 == 10) {
            diem = 50;
        } else if (o1 == 172 && o2 >= 16 && o2 <= 31) {
            diem = 40;
        } else if (o1 == 100 && o2 >= 64 && o2 <= 127) {
            // 100.64/10 la CGNAT cua nha mang - client ngoai khong goi vao duoc.
            diem = 5;
        } else {
            diem = 20;
        }
        return cardAo ? diem - 35 : diem;
    }

    /** Tên + mô tả của card, gộp lại để so khớp mẫu tên VPN/máy ảo. */
    private static String tenDayDu(java.net.NetworkInterface card) {
        String ten = card.getName() == null ? "" : card.getName();
        String mo = card.getDisplayName() == null ? "" : card.getDisplayName();
        return (ten + " " + mo).trim();
    }

    /**
     * Card này có phải VPN hoặc card ảo không?
     *
     * <p>{@code NetworkInterface.isVirtual()} <b>không</b> nhận ra những card
     * này — nó chỉ báo true cho card con kiểu {@code eth0:1}. Card của
     * Cloudflare WARP, OpenVPN, VMware… đều là card thật dưới mắt Java, nên
     * phải nhận theo tên.</p>
     */
    private static boolean laCardAo(String tenCard) {
        String t = tenCard.toLowerCase();
        String[] mau = {
            "warp", "cloudflare", "openvpn", "wireguard", "tailscale",
            "zerotier", "radmin", "hamachi", "nordlynx", "proton",
            "tap-windows", "tap adapter", "tun", "vpn",
            "vmware", "virtualbox", "vboxnet", "hyper-v", "vethernet",
            "docker", "wsl", "loopback", "teredo", "isatap", "bluetooth",
            "npcap", "pseudo"
        };
        for (String m : mau) {
            if (t.contains(m)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    /** Đọc bảng phân loại ô (đi được / chặn / nước...) từ file dữ liệu. */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int y = 0; y < numTileMap; y++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[y] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[y][k] = typeIndex;

                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.logException(MapService.class, e);
        }
        return tileIndexTileType;
    }

    /**
     * @param mapId mapId
     * @return tile map for paint
     */
    /**
     * Đọc lưới ô của một bản đồ từ file nhị phân trong {@code data/map/}.
     *
     * @return mảng 2 chiều {@code [dòng][cột]} chứa chỉ số ô
     */
    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_map_data/" + mapId))) {
                int w = dis.readByte();
                int h = dis.readByte();
                tileMap = new int[h][w];
                for (int[] tm : tileMap) {
                    for (int j = 0; j < tm.length; j++) {
                        tm[j] = dis.readByte();
                    }
                }
            }
        } catch (IOException e) {
        }
        return tileMap;
    }

    //service*******************************************************************
    /**
     * Tìm clan theo id.
     *
     * <p>Duyệt tuyến tính {@code CLANS} — chấp nhận được với vài trăm clan, nhưng
     * bị gọi rất thường xuyên. Một {@code Map<Integer, Clan>} sẽ tốt hơn.</p>
     */
    public static Clan getClanById(int id) throws Exception {
        for (Clan clan : CLANS) {
            if (clan.id == id) {
                return clan;
            }
        }
        throw new Exception("Không tìm thấy clan id: " + id);
    }

    /** Thêm clan vào danh sách chung. Không đồng bộ — xem ghi chú đầu lớp. */
    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    /** Số clan hiện có. */
    public static int getNumClan() {
        return CLANS.size();

    }

    /** Tìm khuôn quái theo id. Duyệt tuyến tính; bảng tra sẽ nhanh hơn. */
    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    /** Số khung hình của một ảnh động, tra theo tên trong {@code IMAGES_BY_NAME}. */
    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }
    
    /** Chuỗi hiển thị mốc thời gian sự kiện đua top. <b>Tên hàm sai quy ước Java</b> (phải bắt đầu chữ thường). */
    public static String HienThiTimeEvent() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        String formattedTime = dateFormat.format(timeSuKienDuaTop);
        return formattedTime;
    }

    /** Đếm ngược tới mốc kết thúc sự kiện đua top. <b>Tên hàm sai quy ước Java.</b> */
    public static String DemTimeEvent() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime event = timeSuKienDuaTop.toLocalDateTime();

    if (event.isAfter(now)) {
        long seconds = ChronoUnit.SECONDS.between(now, event);

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) return "(" + days + " ngày " + hours + " giờ nữa)";
        if (hours > 0) return "(" + hours + " giờ " + minutes + " phút nữa)";
        return "(" + minutes + " phút nữa)";
    }
    return "(Đã kết thúc)";
}

   public static long HienThiTimeEventTwo() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime event = timeSuKienDuaTop.toLocalDateTime();
    long seconds = ChronoUnit.SECONDS.between(now, event);
    return Math.max(seconds, 0);
}
    
    /** Nạp chỉ số toàn server (được lưu định kỳ) từ CSDL lúc khởi động. */
    public void loadAttributeServer() {
        PreparedStatement ps;
        ResultSet rs;
        try (Connection con = ConnectDB.getConnection();) {
            AttributeManager am = new AttributeManager();
            ps = con.prepareStatement("SELECT * FROM attribute_server");
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int templateID = rs.getInt("attribute_template_id");
                int value = rs.getInt("value");
                int time = rs.getInt("time");
                Attribute at = Attribute.builder()
                        .id(id)
                        .templateID(templateID)
                        .value(value)
                        .time(time)
                        .build();
                am.add(at);
            }
            ServerManager.gI().setAttributeManager(am);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(Manager.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Ghi chỉ số toàn server xuống CSDL.
     *
     * <p>Gọi mỗi 10 phút từ game loop, và một lần nữa khi tắt server
     * ({@code ServerManager.close()}) để không mất 10 phút cuối.</p>
     */
    public void updateAttributeServer() {
        PreparedStatement ps;
        try (Connection con = ConnectDB.getConnection();) {
            AttributeManager am = ServerManager.gI().getAttributeManager();
            List<Attribute> attributes = am.getAttributes();
            ps = con.prepareStatement("UPDATE attribute_server SET attribute_template_id = ?, value = ?, time = ? WHERE id = ?");
            synchronized (attributes) {
                for (Attribute at : attributes) {
                    try {
                        if (at.isChanged()) {
                            ps.setInt(1, at.getTemplate().getId());
                            ps.setInt(2, at.getValue());
                            ps.setInt(3, at.getTime());
                            ps.setInt(4, at.getId());
                            ps.addBatch();
                        }
                    } catch (SQLException e) {
                    }
                }
            }
            ps.executeBatch();
            ps.close();
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(Manager.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Nạp lại danh sách shop từ CSDL <b>trong lúc server đang chạy</b>.
     *
     * <p>Đây là <b>ví dụ duy nhất trong project về nạp lại dữ liệu nóng</b> — đúng
     * mô hình nên áp dụng cho item, quái, nhiệm vụ ở giai đoạn 8.</p>
     *
     * <p>Cách làm là thay nguyên cả tham chiếu {@code SHOPS}, nên thread đang duyệt
     * danh sách cũ vẫn chạy tới hết trên bản cũ. Không hỏng, nhưng dữ liệu cũ.</p>
     */
    public void updateShop() {
        try ( Connection con = ConnectDB.getConnection();) {
            SHOPS = ShopDAO.getShops(con);
        } catch (Exception ex) {

        }
    }
    
    /**
     * Xoá dữ liệu trong bộ nhớ của một sự kiện.
     *
     * <p>Đi kèm với {@code ServerManager.updateEventPlayer()} vốn xoá phần dữ liệu
     * trong CSDL. <b>Phải làm cả hai</b>, nếu không bộ nhớ và CSDL sẽ lệch nhau.</p>
     *
     * @param eventName tên sự kiện, ví dụ {@code "luna_new_year"}, {@code "christ_mas"}
     */
    public static void resetEventData(String eventName) {
        String defaultJson = "{\"eventPoint\":0,\"lastExpRewardStage\":0}";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE `event` SET `data` = ? WHERE `name` = ?")) {
            ps.setString(1, defaultJson);
            ps.setString(2, eventName);
            ps.executeUpdate();
        } catch (SQLException e) {
        }
    }
}
