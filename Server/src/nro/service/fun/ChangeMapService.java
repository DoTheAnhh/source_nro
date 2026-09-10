package nro.service.fun;

import nro.core.consts.ConstMap;
import nro.core.consts.ConstPlayer;
import nro.core.consts.ConstTask;
import nro.entity.map.Map;
import nro.entity.map.majinbuu12h.MajinBuu12H;
import nro.entity.map.WayPoint;
import nro.entity.map.Zone;
import nro.server.Manager;
import nro.service.MapService;
import nro.entity.mob.Mob;
import nro.entity.player.Player;
import nro.entity.matches.TYPE_LOSE_PVP;
import nro.service.Service;
import nro.core.util.Util;
import nro.service.effect.EffectSkillService;
import nro.service.inventory.InventoryService;
import nro.service.NpcService;
import nro.service.PlayerService;
import nro.service.TaskService;
import nro.core.log.Logger;
import nro.core.util.TimeUtil;
import java.util.List;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstTranhNgocNamek;
import java.io.IOException;
import java.util.ArrayList;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.net.io.Message;
import nro.service.map.blackballwar.BlackBallWarService;
import nro.entity.map.dragonballnamec.NgocRongNamec;
import nro.service.map.dragonnamecwar.TranhNgocService;

public class ChangeMapService {

    private static final byte EFFECT_GO_TO_TUONG_LAI = 0;
    private static final byte EFFECT_GO_TO_BDKB = 1;

    public static final byte AUTO_SPACE_SHIP = -1;
    public static final byte NON_SPACE_SHIP = 0;
    public static final byte DEFAULT_SPACE_SHIP = 1;
    public static final byte TELEPORT_YARDRAT = 2;
    public static final byte TENNIS_SPACE_SHIP = 3;

    private static ChangeMapService instance;

    private ChangeMapService() {

    }

    public static ChangeMapService gI() {
        if (instance == null) {
            instance = new ChangeMapService();
        }
        return instance;
    }

    /**
     * Mở tab chuyển map
     *
     * @param pl
     */
    public void openChangeMapTab(Player pl) {
        List<Zone> list;
        Message msg = null;
        try {
            msg = new Message(-91);
            switch (pl.iDMark.getTypeChangeMap()) {
                case ConstMap.CHANGE_CAPSULE:
                    list = (pl.mapCapsule = MapService.gI().getMapCapsule(pl));
                    msg.writer().writeByte(list.size());
                    for (int i = 0; i < pl.mapCapsule.size(); i++) {
                        Zone zone = pl.mapCapsule.get(i);
                        if (i == 0 && pl.mapBeforeCapsule != null) {
                            msg.writer().writeUTF("Về chỗ cũ: " + zone.map.mapName);
                        } else if (zone.map.mapName.equals("Nhà Broly") || zone.map.mapName.equals("Nhà Gôhan")
                                || zone.map.mapName.equals("Nhà Moori")) {
                            msg.writer().writeUTF("Về nhà");
                        } else {
                            msg.writer().writeUTF(zone.map.mapName);
                        }
                        msg.writer().writeUTF(zone.map.planetName);
                    }
                    if (pl.HoTongDuongTang) {
                        Service.getInstance().sendThongBao(pl, "Đang hộ tống, không thể Capsule");
                        return;
                    }
                    break;
                case ConstMap.CHANGE_BLACK_BALL:
                    list = (pl.mapBlackBall != null ? pl.mapBlackBall
                            : (pl.mapBlackBall = MapService.gI().getMapBlackBall()));
                    msg.writer().writeByte(list.size());
                    for (Zone zone : list) {
                        msg.writer().writeUTF(zone.map.mapName);
                        msg.writer().writeUTF(zone.map.planetName);
                    }
                    break;
                case ConstMap.CHANGE_MAP_MA_BU:
                    list = (pl.mapMaBu != null ? pl.mapMaBu
                            : (pl.mapMaBu = MapService.gI().getMapMaBu()));
                    msg.writer().writeByte(list.size());
                    for (Zone zone : list) {
                        msg.writer().writeUTF(zone.map.mapName);
                        msg.writer().writeUTF(zone.map.planetName);
                    }
                    break;
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(ChangeMapService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Mở tab chọn khu
     *
     * @param pl
     */
    public void openZoneUI(Player pl) {
        if (pl.zone == null) {
            Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
            return;
        }
        if (!pl.isFounder()) {
            if (MapService.gI().isMapOffline(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapBangHoi(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapPhoBan(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapTayKarin(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapMaBu12H(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapMabu14H(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (pl.zone.map.mapId == 51) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapHungVuongEvent(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapRiengTu(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
        }
        Message msg = null;
        try {
            msg = new Message(29);
            msg.writer().writeByte(pl.zone.map.zones.size());
            for (Zone zone : pl.zone.map.zones) {
                msg.writer().writeByte(zone.zoneId);
                int numPlayers = zone.getNumOfPlayers();
                msg.writer().writeByte((numPlayers < 5 ? 0 : (numPlayers < 8 ? 1 : 2)));
                msg.writer().writeByte(numPlayers);
                msg.writer().writeByte(zone.maxPlayer);
                if (zone.isCompeting) {
                    msg.writer().writeByte(1);
                    msg.writer().writeUTF(zone.rankName1);
                    msg.writer().writeInt(zone.rank1);
                    msg.writer().writeUTF(zone.rankName2);
                    msg.writer().writeInt(zone.rank2);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(ChangeMapService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Chuyển khu
     *
     * @param pl
     * @param zoneId
     */
    /**
     * Giãn cách tối thiểu giữa hai lần đổi khu, tính bằng mili-giây.
     *
     * <p>Không có giãn cách thì đổi khu liên tục được: mỗi lần đổi là một lượt
     * rời vùng và vào vùng mới, kéo theo gói tin cho mọi người trong cả hai khu.
     * Bấm nhanh vài chục lần là dội gói tin cho cả server.</p>
     *
     * <p>Chỉnh được ở tab "Cấu Hình → Quy ước", khoá {@code doi_khu_giay}.</p>
     */
    private static long giayDoiKhu() {
        return nro.repository.dao.ConfigDAO.num("doi_khu_giay") * 1000L;
    }

    public void changeZone(Player pl, int zoneId) {
        if (pl.zone == null) {
            Service.gI().sendThongBaoOK(pl, "Không thể đến khu vực này");
            return;
        }
        // Admin khong bi gian cach: con phai di kiem tra nhieu khu.
        if (!pl.isFounder()) {
            long cho = giayDoiKhu();
            long daQua = System.currentTimeMillis() - pl.lanCuoiDoiKhu;
            if (cho > 0 && daQua < cho) {
                Service.gI().sendThongBao(pl, "Đợi "
                        + ((cho - daQua + 999) / 1000) + " giây nữa mới đổi khu được.");
                return;
            }
        }
        pl.lanCuoiDoiKhu = System.currentTimeMillis();
        if (!pl.isFounder()) {
            if (MapService.gI().isMapOffline(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đến khu vực này");
                return;
            }
            if (MapService.gI().isMapBangHoi(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đến khu vực này");
                return;
            }
            if (MapService.gI().isMapPhoBan(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đến khu vực này");
                return;
            }
            if (MapService.gI().isMapTayKarin(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đến khu vực này");
                return;
            }
            if (MapService.gI().isMapMaBu12H(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đến khu vực này");
                return;
            }
            if (MapService.gI().isMapMabu14H(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đến khu vực này");
                return;
            }
            if (pl.zone.map.mapId == 51) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapHungVuongEvent(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
            if (MapService.gI().isMapRiengTu(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
                return;
            }
        }

        if (pl.isFounder() || pl.isQuanTriVien() || pl.isBoss || Util.canDoWithTime(pl.iDMark.getLastTimeChangeZone(), 10000)) {
            if (pl.itemTime.isUseTDLT) {
                pl.iDMark.setLastTimeChangeZone(System.currentTimeMillis() - 5000);
            } else {
                pl.iDMark.setLastTimeChangeZone(System.currentTimeMillis());
            }
            Map map = pl.zone.map;
            if (zoneId >= 0 && zoneId <= map.zones.size() - 1) {
                Zone zoneJoin = map.zones.get(zoneId);
                if (zoneJoin != null && (zoneJoin.getNumOfPlayers() >= zoneJoin.maxPlayer && (!pl.isFounder() || !pl.isQuanTriVien()) && !pl.isBoss)) {
                    Service.gI().sendThongBaoFromAdmin(pl, "Khu vực đã đầy");
                    return;
                }
                if (zoneJoin != null) {
                    changeMap(pl, zoneJoin, -1, -1, pl.location.x, pl.location.y, NON_SPACE_SHIP);
                }
            } else {
                Service.gI().sendThongBaoFromAdmin(pl, "Không thể thực hiện");
            }
        } else if (!pl.isFounder() || !pl.isQuanTriVien() || !pl.isBoss) {
            Service.gI().sendThongBaoFromAdmin(pl, "Đổi khu quá nhanh, vui lòng đợi " + TimeUtil.getTimeLeft(pl.iDMark.getLastTimeChangeZone(), 10));
        } else if (pl.isPl() && pl.itemTime.isUseTDLT) {
            Service.gI().sendThongBaoFromAdmin(pl, "Đổi khu quá nhanh, vui lòng đợi " + TimeUtil.getTimeLeft(pl.iDMark.getLastTimeChangeZone(), 5));
        }
    }

    /**
     * Chuyển map bằng tàu vũ trụ
     *
     * @param pl
     * @param mapId
     * @param zone
     * @param x
     */
    public void changeMapBySpaceShip(Player pl, int mapId, int zone, int x) {
        if (!pl.isBoss) {
            changeMap(pl, null, mapId, zone, x, 5, AUTO_SPACE_SHIP);
            if (pl.isDie()) {
                if (pl.haveTennisSpaceShip) {
                    Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
                } else {
                    Service.gI().hsChar(pl, 1, 1);
                }
            } else {
                if (pl.haveTennisSpaceShip) {
                    pl.nPoint.setFullHpMp();
                    PlayerService.gI().sendInfoHpMp(pl);
                }
            }
            if (MapService.gI().isMap5000NamTruoc(pl.zone.map.mapId)) {
                Service.gI().sendThongBaoFromAdmin(pl, "Không thể di chuyển bằng tàu vũ trụ!");
            }
        }
    }

    public void changeMapBySpaceShip(Player pl, Zone zoneJoin, int x) {
        if (pl.isDie()) {
            if (pl.haveTennisSpaceShip) {
                Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
            } else {
                Service.gI().hsChar(pl, 1, 1);
            }
        } else {
            if (pl.haveTennisSpaceShip) {
                pl.nPoint.setFullHpMp();
                PlayerService.gI().sendInfoHpMp(pl);
            }
        }
        if (MapService.gI().isMap5000NamTruoc(pl.zone.map.mapId)) {
            Service.gI().sendThongBaoFromAdmin(pl, "Không thể di chuyển bằng tàu vũ trụ!");
            return;
        }
        changeMap(pl, zoneJoin, -1, -1, x, 5, AUTO_SPACE_SHIP);
    }

    /**
     * Chuyển map đứng trên mặt đất
     *
     * @param pl
     * @param mapId
     * @param zoneId
     * @param x
     */
    public void changeMapInYard(Player pl, int mapId, int zoneId, int x) {
        Zone zoneJoin = MapService.gI().getMapCanJoin(pl, mapId, zoneId);
        if (pl.HoTongDuongTang) {
            Service.gI().sendThongBaoFromAdmin(pl, "Không thể chuyển map khi đang Hộ Tống!");
            return;
        }
        if (zoneJoin != null) {
            if (zoneJoin.map.mapWidth - 100 < 100) {
                x = x != -1 ? x : Util.nextInt(100, 700);
            } else {
                x = x != -1 ? x : Util.nextInt(100, zoneJoin.map.mapWidth - 100);
            }
            changeMap(pl, zoneJoin, -1, -1, x, zoneJoin.map.yPhysicInTop(x, 100), NON_SPACE_SHIP);
        }
    }

    /**
     * Chuyển map đứng trên mặt đất
     *
     * @param pl
     * @param zoneJoin
     * @param x
     */
    public void changeMapInYard(Player pl, Zone zoneJoin, int x) {
        if (pl.HoTongDuongTang) {
            Service.gI().sendThongBaoFromAdmin(pl, "Không thể chuyển map khi đang Hộ Tống!");
            return;
        }
        changeMap(pl, zoneJoin, -1, -1, x, zoneJoin.map.yPhysicInTop(x, 100), NON_SPACE_SHIP);
    }

    /**
     * Chuyển map
     *
     * @param pl
     * @param mapId
     * @param zone
     * @param x
     * @param y
     */
    public void changeMap(Player pl, int mapId, int zone, int x, int y) {
        changeMap(pl, null, mapId, zone, x, y, NON_SPACE_SHIP);
    }

    /**
     * Chuyển map chỉ lấy 1 người
     *
     * @param player
     * @param mapId
     * @param zone
     * @param x
     * @param y
     */
    public void changeMapPlayer(Player player, int mapId, int zone, int x, int y) {
        Map map = MapService.gI().getMapById(mapId);
        if (map == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy bản đồ!");
            return;
        }
        if (zone == -1 || MapService.gI().isMapHungVuongEvent(mapId) || MapService.gI().isMapRiengTu(mapId)) {
            int minPlayers = Integer.MAX_VALUE;
            int selectedZone = -1;
            for (int i = 0; i < map.zones.size(); i++) {
                Zone z = map.getZoneByIndex(i);
                if (z != null) {
                    int count = z.getNumOfPlayers();
                    if (count < z.maxPlayer) {
                        if (count < minPlayers) {
                            minPlayers = count;
                            selectedZone = i;
                        }
                    }
                }
            }
            if (selectedZone == -1) {
                Service.gI().sendThongBao(player, "Tất cả khu của bản đồ này đã đầy người!");
                return;
            }
            zone = selectedZone;
        }
        changeMap(player, null, mapId, zone, x, y, NON_SPACE_SHIP);
    }

    /**
     * Chuyển map chỉ lấy 1 người
     *
     * @param player
     * @param mapId
     * @param zone
     * @param x
     * @param y
     */
    public void changeMapPlayerRandomZone(Player player, int mapId, int zone, int x, int y) {
        Map map = MapService.gI().getMapById(mapId);
        if (map == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy bản đồ!");
            return;
        }
        if (zone == -1 || MapService.gI().isMapHungVuongEvent(mapId) || MapService.gI().isMapRiengTu(mapId)) {
            int minPlayers = Integer.MAX_VALUE;
            List<Integer> leastPopulatedZones = new ArrayList<>();
            for (int i = 0; i < map.zones.size(); i++) {
                Zone z = map.getZoneByIndex(i);
                if (z != null) {
                    int count = z.getNumOfPlayers();
                    if (count < z.maxPlayer) {
                        if (count < minPlayers) {
                            minPlayers = count;
                            leastPopulatedZones.clear();
                            leastPopulatedZones.add(i);
                        } else if (count == minPlayers) {
                            leastPopulatedZones.add(i);
                        }
                    }
                }
            }
            if (leastPopulatedZones.isEmpty()) {
                Service.gI().sendThongBao(player, "Tất cả khu của bản đồ này đã đầy người!");
                return;
            }
            zone = leastPopulatedZones.get(Util.nextInt(leastPopulatedZones.size()));
        }
        changeMap(player, null, mapId, zone, x, y, NON_SPACE_SHIP);
    }

    /**
     * Chuyển map
     *
     * @param pl
     * @param zoneJoin
     * @param x
     * @param y
     */
    public void changeMap(Player pl, Zone zoneJoin, int x, int y) {
        changeMap(pl, zoneJoin, -1, -1, x, y, NON_SPACE_SHIP);
    }

    /**
     * Chuyển map bằng dịch chuyển
     *
     * @param pl
     * @param zoneJoin
     * @param x
     * @param y
     */
    public void changeMapYardrat(Player pl, Zone zoneJoin, int x, int y) {
        if (pl.HoTongDuongTang) {
            Service.gI().sendThongBaoFromAdmin(pl, "Không thể dịch chuyển khi đang Hộ Tống!");
            return;
        }
        changeMap(pl, zoneJoin, -1, -1, x, y, TELEPORT_YARDRAT);
    }

    /**
     * Kéo toạ độ hạ cánh vào trong viền bản đồ đích, và xuống mặt đất.
     *
     * <p>Chừa 60 điểm mỗi bên — đúng khoảng mà {@code resetPoint} vẫn dùng, để
     * nhân vật không dính sát mép và không lọt qua viền.</p>
     *
     * <p>Nếu cột đó không có mặt đất thì bốc lại một cột có đất: đứng lơ lửng
     * trên khoảng trống thì client không đặt được nhân vật.</p>
     */
    private void keoVaoVienBanDo(Player pl, Zone zoneJoin) {
        if (pl == null || pl.location == null || zoneJoin == null
                || zoneJoin.map == null) {
            return;
        }
        Map m = zoneJoin.map;
        int le = 60;
        int trai = le;
        int phai = m.mapWidth - le;
        if (phai < trai) {
            trai = 0;
            phai = Math.max(0, m.mapWidth);
        }
        if (pl.location.x < trai) {
            pl.location.x = trai;
        } else if (pl.location.x > phai) {
            pl.location.x = phai;
        }
        if (pl.location.y < 0) {
            pl.location.y = 0;
        } else if (pl.location.y > m.mapHeight) {
            pl.location.y = m.mapHeight;
        }
        // Duoi chan co dat khong. yPhysicInTop tra 0 khi quet het xuong duoi ma
        // khong gap o mat dat nao.
        int dat = m.yPhysicInTop(pl.location.x, Math.max(1, pl.location.y - 100));
        if (dat <= 0) {
            int xMoi = chonHoanhDoCoDat(zoneJoin, 100);
            int datMoi = m.yPhysicInTop(xMoi, 100);
            if (datMoi > 0) {
                pl.location.x = xMoi;
                pl.location.y = datMoi;
            }
        }
    }

    /** Số lần bốc ngẫu nhiên trước khi chịu thua và dùng phương án dự phòng. */
    private static final int SO_LAN_BOC_CHO_HA_CANH = 30;

    /**
     * Chọn hoành độ hạ cánh <b>có mặt đất bên dưới</b>.
     *
     * <h3>Lỗi capsule bay làm đứng màn hình</h3>
     *
     * <p>Khi tới bằng capsule hay tàu vũ trụ thì {@code x = -1}, và bản cũ bốc
     * thẳng {@code Util.nextInt(100, mapWidth - 100)} rồi thả người chơi từ
     * {@code y = 5} (đỉnh bản đồ) cho rơi xuống. Bản đồ nào có hố hay khoảng
     * trống mà bốc trúng cột đó thì <b>không có ô địa hình nào để đứng</b>:
     * client không đặt được nhân vật nên đứng im, không di chuyển được. Vì phụ
     * thuộc số ngẫu nhiên nên lỗi chỉ <i>thi thoảng</i> mới xảy ra — đúng như
     * người dùng mô tả.</p>
     *
     * <p>{@link Map#yPhysicInTop} trả về <b>0</b> khi quét hết xuống dưới mà
     * không gặp ô mặt đất nào. Đó là chốt kiểm ở đây.</p>
     *
     * <p>Bốc trượt hết thì lấy hoành độ của một điểm dịch chuyển — chỗ đó chắc
     * chắn đứng được, vì người chơi vẫn đi bộ qua lại hằng ngày.</p>
     */
    private int chonHoanhDoCoDat(Zone zoneJoin, int y) {
        Map m = zoneJoin.map;
        int trai = 100;
        int phai = m.mapWidth - 100;
        if (phai <= trai) {
            return 100;
        }
        for (int lan = 0; lan < SO_LAN_BOC_CHO_HA_CANH; lan++) {
            int thu = Util.nextInt(trai, phai);
            if (m.yPhysicInTop(thu, y) > 0) {
                return thu;
            }
        }
        if (m.wayPoints != null) {
            for (WayPoint wp : m.wayPoints) {
                int giua = (wp.minX + wp.maxX) / 2;
                if (giua > 0 && m.yPhysicInTop(giua, y) > 0) {
                    return giua;
                }
            }
        }
        // Ca bản đồ không có chỗ nào đứng được thì trả về như cũ — ít nhất
        // không tệ hơn bản cũ, và không treo vòng lặp.
        return trai;
    }

    /**
     * Một lượt đổi bản đồ treo quá lâu thì tự mở khoá, tính bằng mili giây.
     *
     * <p>Client bình thường báo nạp xong trong dưới một giây. Năm giây là để
     * dành cho máy yếu và mạng kém. Quá thế thì gói {@code -39} đã mất, và giữ
     * khoá mãi nghĩa là người chơi <b>không đổi bản đồ được nữa</b> cho tới khi
     * thoát game — tệ hơn hẳn lỗi ban đầu.</p>
     */
    private static final long KHOA_DOI_MAP_HET_HAN_MS = 5000;

    /**
     * Quãng nghỉ tối thiểu giữa hai lần đi qua cổng dịch chuyển, mili giây.
     *
     * <p>Một giây, đúng như đã yêu cầu. Khoá {@code dangDoiMap} ở trên đã chặn
     * được hai lượt <b>chồng nhau</b>; con số này chặn thêm kiểu chạy đi chạy
     * lại qua cổng liên tục — lượt trước vừa xong là lượt sau đã bắt đầu, và
     * client chưa kịp dựng xong cảnh cũ.</p>
     */
    private static final long CHO_QUA_CONG_MS = 1000;

    /** Đang có một lượt đổi bản đồ chưa xong hay không. */
    public static boolean dangDoiMap(Player pl) {
        if (pl == null || !pl.dangDoiMap) {
            return false;
        }
        if (System.currentTimeMillis() - pl.mocBatDauDoiMap > KHOA_DOI_MAP_HET_HAN_MS) {
            // Het han: coi nhu xong, khong giu nguoi choi lai mai mai.
            pl.dangDoiMap = false;
            return false;
        }
        return true;
    }

    /**
     * Xin bắt đầu một lượt đổi bản đồ.
     *
     * @return {@code false} nếu đang có lượt khác chạy dở — lúc đó đã báo cho
     *         người chơi và đóng hộp "Xin chờ" rồi
     */
    public static boolean xinDoiMap(Player pl) {
        if (pl == null) {
            return false;
        }
        if (dangDoiMap(pl)) {
            // Dong hop "Xin cho" lai: client mo no ngay khi gui yeu cau, khong
            // dong thi nguoi choi ngoi nhin chu "Xin cho" ma khong hieu gi.
            Service.gI().hideWaitDialog(pl);
            Service.gI().sendThongBao(pl, "Đang chuyển bản đồ, chờ một chút.");
            return false;
        }
        pl.dangDoiMap = true;
        pl.mocBatDauDoiMap = System.currentTimeMillis();
        return true;
    }

    /** Lượt đổi bản đồ đã xong (client báo nạp xong, hoặc đổi thất bại). */
    public static void xongDoiMap(Player pl) {
        if (pl != null) {
            pl.dangDoiMap = false;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Một yêu cầu đổi bản đồ <b>bị hoãn lại</b> vì lượt trước chưa nạp xong.
     *
     * <p>Giữ nguyên xi bộ tham số của lời gọi, để chạy lại y hệt khi client báo
     * đã dựng xong bản đồ.</p>
     */
    public static final class YeuCauDoiMap {

        final Zone zoneJoin;
        final int mapId;
        final int zoneId;
        final int x;
        final int y;
        final byte typeSpace;
        final long moc = System.currentTimeMillis();

        YeuCauDoiMap(Zone zoneJoin, int mapId, int zoneId, int x, int y, byte typeSpace) {
            this.zoneJoin = zoneJoin;
            this.mapId = mapId;
            this.zoneId = zoneId;
            this.x = x;
            this.y = y;
            this.typeSpace = typeSpace;
        }
    }

    /**
     * Cửa duy nhất của <b>mọi</b> cách đổi bản đồ.
     *
     * <h2>Chờ nạp xong rồi mới cho sang</h2>
     *
     * <p>Mọi đường đi bản đồ đều đổ về đây: cổng dịch chuyển, nút dịch nhanh,
     * capsule, tàu vũ trụ, dịch chuyển Yardrat, đổi khu, NPC đưa đi, và các lệnh
     * của quản trị viên. Trước bản này chỉ riêng đường <b>cổng dịch chuyển</b>
     * kiểm tra xem lượt trước đã nạp xong chưa; những đường còn lại thì không,
     * nên vẫn chèn được một lượt thứ hai vào giữa lúc dữ liệu bản đồ đang chạy
     * xuống client. Hai bộ dữ liệu đan nhau ra đúng cảnh đã gặp: hình bản đồ bị
     * xé, nhân vật đứng im, bấm gì cũng không ăn.</p>
     *
     * <p>Đặt cổng chặn ở đây thì <b>không đường nào lọt</b>, kể cả đường viết
     * sau này.</p>
     *
     * <h2>Hoãn lại, không vứt đi</h2>
     *
     * <p>Yêu cầu tới sớm được <b>giữ lại</b> và chạy ngay khi client báo nạp
     * xong, chứ không bị bỏ. Vứt đi thì những lượt đi <b>do máy chủ tự quyết</b>
     * — hết giờ đưa về làng, kết thúc phó bản, sự kiện dời người — sẽ mất hẳn
     * nếu chẳng may rơi trúng lúc người chơi đang nạp bản đồ, và người đó kẹt
     * lại một nơi đáng ra phải rời khỏi. Chỉ giữ <b>yêu cầu mới nhất</b>: bấm
     * dồn năm cái thì đi một cái cuối, không phải đi năm chặng.</p>
     *
     * <p>Riêng nút dịch nhanh và cổng dịch chuyển thì {@code changeMapWaypoint}
     * đã chặn từ trước khi tới đây, nên bấm dồn vẫn chỉ đi một bản đồ như cũ —
     * chủ ý là thế, để cái bấm thừa không thành một chặng đi bất ngờ.</p>
     *
     * <p>Khoá chỉ áp cho <b>người chơi thật</b>. Boss, đệ tử, phân thân, thú
     * cưng không phải là client và không bao giờ gửi gói báo "đã nạp xong", nên
     * khoá chúng lại là khoá vĩnh viễn cho tới lúc hết hạn — chúng sẽ đứng đờ
     * năm giây mỗi lần đổi bản đồ.</p>
     */
    private void changeMap(Player pl, Zone zoneJoin, int mapId, int zoneId, int x, int y, byte typeSpace) {
        if (pl != null && pl.isPl() && dangDoiMap(pl)) {
            pl.yeuCauDoiMapDangCho
                    = new YeuCauDoiMap(zoneJoin, mapId, zoneId, x, y, typeSpace);
            // Dong hop "Xin cho": client mo no ngay khi gui yeu cau, khong dong
            // thi nguoi choi ngoi nhin chu "Xin cho" ma khong hieu gi.
            Service.gI().hideWaitDialog(pl);
            return;
        }
        // Toi day nghia la dang doi ban do THAT. Moi yeu cau hoan tu truoc deu
        // da cu: no doi mot ban do khac, tu mot hoan canh khac. Bo di, khong thi
        // no se no ra sau lan nap nay va keo nguoi choi di dau do bat ngo.
        if (pl != null) {
            pl.yeuCauDoiMapDangCho = null;
        }
        if (pl.idNRNM != -1 && !Util.canDoWithTime(pl.lastTimePickNRNM, 30000)) {
            resetPoint(pl);
            Service.gI().sendThongBao(pl, "Không thể chuyển map quá nhanh khi đeo Ngọc Rồng Namếc");
            return;
        }
        if (pl.idNRNM != -1 && zoneJoin != null) {
            int idNRNM = pl.idNRNM;
            NgocRongNamec.gI().mapNrNamec[idNRNM - 353] = zoneJoin.map.mapId;
            NgocRongNamec.gI().nameNrNamec[idNRNM - 353] = zoneJoin.map.mapName;
            NgocRongNamec.gI().zoneNrNamec[idNRNM - 353] = (byte) zoneJoin.zoneId;
            NgocRongNamec.gI().pNrNamec[idNRNM - 353] = pl.name;
            NgocRongNamec.gI().idpNrNamec[idNRNM - 353] = (int) pl.id;
            pl.lastTimePickNRNM = System.currentTimeMillis();
        }
        if (pl.idNRNM != -1 && !NgocRongNamec.gI().isMapNRNM(zoneJoin.map.mapId)) {
            NgocRongNamec.gI().dropNamekBall(pl);
        }
        TransactionService.gI().cancelTrade(pl);
        if (pl.iDMark.getTranhNgoc() != -1) {
            zoneJoin = getZoneTranhNgoc(pl.iDMark.getTranhNgoc());
            if (pl.iDMark.getTranhNgoc() == 1) {
                zoneJoin.addPlayersCadic(pl);
            } else if (pl.iDMark.getTranhNgoc() == 2) {
                zoneJoin.addplayersFide(pl);
            }
            if (!zoneJoin.startZoneTranhNgoc) {
                zoneJoin.lastTimeStartTranhNgoc = System.currentTimeMillis();
                zoneJoin.startZoneTranhNgoc = true;
            }
            Service.gI().changeFlag(pl, pl.iDMark.getTranhNgoc());
            TranhNgocService.getInstance().sendCreatePhoBan(pl);
        }
        if (zoneJoin == null) {
            if (mapId != -1) {
                zoneJoin = MapService.gI().getMapCanJoin(pl, mapId, zoneId);
            }
        }
        if (typeSpace == TELEPORT_YARDRAT) {
            zoneJoin = checkMapCanJoinByYardart(pl, zoneJoin);
        }
        zoneJoin = checkMapCanJoin(pl, zoneJoin);
        if (zoneJoin != null) {
            boolean currMapIsCold = MapService.gI().isMapCold(pl.zone.map);
            boolean nextMapIsCold = MapService.gI().isMapCold(zoneJoin.map);
            boolean currMapIsChristMas = MapService.gI().isMapChristMasEvent(pl.zone.map.mapId);
            boolean nextMapIsChristMas = MapService.gI().isMapChristMasEvent(zoneJoin.map.mapId);
            boolean currMapIsCereal = MapService.gI().isMapCereal(pl.zone.map);
            boolean nextMapIsCereal = MapService.gI().isMapCereal(zoneJoin.map);
            boolean currMapIsQuaKhu = MapService.gI().isMap5000NamTruoc(pl.zone.map.mapId);
            boolean nextMapIsQuaKhu = MapService.gI().isMap5000NamTruoc(zoneJoin.map.mapId);
            boolean nextMapIsMabu = MapService.gI().isMapMaBu12H(zoneJoin.map.mapId);
            boolean sameZone = pl.zone.map.mapId == zoneJoin.map.mapId;
            if (typeSpace == AUTO_SPACE_SHIP) {
                // Không phát hiệu ứng tàu khi về map 0 và ba map NHÀ (21 Nhà
                // Gôhan, 22 Nhà Moori, 23 Nhà Broly).
                //
                // Các map khác giữ hiệu ứng tàu: người chơi đứng ở y = 5 (đỉnh
                // bản đồ) chờ hoạt ảnh tàu của client thả xuống. Hụt hoạt ảnh
                // đó là kẹt luôn trên đỉnh — toạ độ vẫn Y: 5, nhân vật không
                // hiện ra, không đi được. Đây chính là "kẹt map khi dùng
                // capsule về nhà". Nhánh map 0 đã được sửa theo cách này từ
                // trước; ba map nhà là đích đến của lệnh "về nhà" nên dính
                // nhiều nhất.
                if (zoneJoin.map.mapId == 0 || zoneJoin.map.mapId == 21
                        || zoneJoin.map.mapId == 22 || zoneJoin.map.mapId == 23) {
                    pl.iDMark.setIdSpaceShip(NON_SPACE_SHIP); // không có tàu

                    // Hạ ngay xuống mặt đất để tránh treo camera
                    int xSpawn;
                    if (x != -1) {
                        xSpawn = x;
                    } else {
                        // Bốc chỗ CÓ mặt đất, đừng bốc bừa: rơi trúng cột
                        // trống thì không có ô địa hình nào để đứng.
                        xSpawn = chonHoanhDoCoDat(zoneJoin, 100);
                    }
                    int ground = zoneJoin.map.yPhysicInTop(xSpawn, 100);
                    if (ground <= 0 || ground > zoneJoin.map.mapHeight) {
                        // Hoành độ chỉ định sẵn cũng có thể rơi vào chỗ trống —
                        // bốc lại một chỗ chắc chắn có đất thay vì dùng số 432
                        // gõ cứng, vì bản đồ nào cũng có mặt đất ở độ cao khác.
                        xSpawn = chonHoanhDoCoDat(zoneJoin, 100);
                        ground = zoneJoin.map.yPhysicInTop(xSpawn, 100);
                        if (ground <= 0 || ground > zoneJoin.map.mapHeight) {
                            ground = 432; // hết cách, giữ như bản cũ
                        }
                    }
                    x = xSpawn;
                    y = ground;
                } else {
                    // Gói -65 phát NGAY tại đây, ở bản đồ CŨ. Đây là chỗ đúng
                    // của nó, đừng dời đi.
                    //
                    // Tên hàm là "spaceShipArrive" nên rất dễ tưởng đây là cảnh
                    // tàu hạ xuống nơi mới. Không phải: client dựng một
                    // {@code Teleport} kiểu 0 — con tàu bốc nhân vật bay LÊN —
                    // và ngay khi nhận gói nó đặt {@code isStopReadMessage},
                    // tức là <b>ngừng xử lý mọi gói tới</b> cho tới khi con tàu
                    // ra khỏi mép trên màn hình. Cảnh hạ xuống ở nơi mới là
                    // việc của {@code iDMark.setIdSpaceShip} ngay dưới đây, đi
                    // kèm gói dữ liệu bản đồ.
                    //
                    // Vì thế quãng ngừng đọc ấy phải nằm TRÙNG với lúc dữ liệu
                    // bản đồ mới đang chạy xuống: client xếp hàng chúng lại,
                    // cất cánh xong thì xử lý một lượt. Dời gói này xuống sau
                    // finishLoadMap — như bản trước đã làm — là bắt client
                    // đóng băng giữa lúc đã đứng trong bản đồ mới, và mọi gói
                    // gửi sau đó (người xung quanh, hiệu ứng, nhiệm vụ) kẹt lại
                    // sau một hoạt ảnh cất cánh vô duyên. Chính là lỗi bản đồ
                    // nghiêm trọng vừa gặp.
                    byte loaiTau = pl.haveTennisSpaceShip
                            ? TENNIS_SPACE_SHIP : DEFAULT_SPACE_SHIP;
                    spaceShipArrive(pl, (byte) 0, loaiTau);
                    pl.iDMark.setIdSpaceShip(loaiTau);
                }
            } else {
                pl.iDMark.setIdSpaceShip(typeSpace);
            }

            if (pl.effectSkill.isCharging) {
                EffectSkillService.gI().stopCharge(pl);
            }
            if (pl.effectSkill.useTroi) {
                EffectSkillService.gI().removeUseTroi(pl);
            }
            if (x != -1) {
                pl.location.x = x;
            } else {
                pl.location.x = chonHoanhDoCoDat(zoneJoin, y);
            }
            pl.location.y = y;
            // Kep vao trong VIEN ban do dich.
            //
            // Toa do di kem loi goi la toa do cua ban do CU: cong dich chuyen
            // ghi san goX/goY, capsule ghi toa do noi vua dung. Ban do dich co
            // the hep hon, va tha nguoi choi ra ngoai vien la client khong tim
            // ra o dia hinh nao — nhan vat dung im, khong di duoc, khong bam
            // duoc. Chinh la canh "ket duoi vien duoi ban do".
            keoVaoVienBanDo(pl, zoneJoin);
            // Khoa lai cho toi khi client bao nap xong (goi -39). Dat SAT truoc
            // goToMap: tu day tro di la du lieu ban do da bat dau chay xuong
            // client, va mot luot thu hai chen vao la hai bo du lieu dan nhau.
            //
            // Chi khoa NGUOI CHOI THAT: boss, de tu, phan than, thu cung khong
            // phai client va khong bao gio gui goi -39, nen khoa chung lai la
            // khoa cho toi khi het han — dung do nam giay moi lan doi ban do.
            if (pl.isPl()) {
                pl.dangDoiMap = true;
                pl.mocBatDauDoiMap = System.currentTimeMillis();
            }
            this.goToMap(pl, zoneJoin);
            if (pl.Detu != null) {
                pl.Detu.joinMapMaster();
            }

            Service.gI().clearMap(pl);
            // Fix Lỗi Load Map 15/09/2023
            if (!pl.isPl()) {
                pl.zone.load_Me_To_Another(pl);
            } else {
                zoneJoin.mapInfo(pl); //-24
                pl.timeChangeZone = System.currentTimeMillis();
            }
            pl.iDMark.setIdSpaceShip(NON_SPACE_SHIP);
            if (pl.isPl() && nextMapIsMabu) {
                if (zoneJoin.map.mapId == 117) {
                    Service.gI().sendThongBao(pl, "Đây là không gian cao trọng lực, hãy cẩn thận");
                }
                if (!sameZone) {
                    if (zoneJoin.map.mapId != 114) {
                        pl.fightMabu.clear();
                    }
                    if (zoneJoin.map.mapId != 114) {
                        int param = zoneJoin.map.mapId - 114;
                        param = (zoneJoin.map.mapId > 116) ? param - 1 : param;
                        Item item = ItemService.gI().createNewItem(((short) 521));
                        item.itemOptions.add(new ItemOption(1, param * 5));
                        InventoryService.gI().addItemBag(pl, item);
                        InventoryService.gI().sendItemBag(pl);
                        Service.gI().sendThongBao(pl, "Bạn nhận được " + param * 5 + " phút " + item.template.name);
                    }
                }
            }
            if (currMapIsCold != nextMapIsCold && !pl.isBoss && !pl.nPoint.isKhongLanh) {
                if (!currMapIsCold && nextMapIsCold) {
                    Service.gI().sendThongBao(pl, "Bạn đã đến hành tinh Cold");
                    Service.gI().sendThongBao(pl, "Sức tấn công và HP của bạn bị giảm 50% vì quá lạnh");
                } else {
                    Service.gI().sendThongBao(pl, "Bạn đã rời hành tinh Cold");
                    Service.gI().sendThongBao(pl, "Sức tấn công và HP của bạn đã trở lại bình thường");
                }
                Service.gI().point(pl);
                Service.gI().Send_Info_NV(pl);
            }
            if (currMapIsChristMas != nextMapIsChristMas && !pl.isBoss && !pl.nPoint.isKhongLanh) {
                if (!currMapIsChristMas && nextMapIsChristMas) {
                    Service.gI().sendThongBao(pl, "Bạn đã đến map sự kiện");
                    Service.gI().sendThongBao(pl, "Sức tấn công và HP của bạn bị giảm 50% vì quá lạnh");
                } else {
                    Service.gI().sendThongBao(pl, "Bạn đã rời map sự kiện");
                    Service.gI().sendThongBao(pl, "Sức tấn công và HP của bạn đã trở lại bình thường");
                }
                Service.gI().point(pl);
                Service.gI().Send_Info_NV(pl);
            }
            if (pl.gender == ConstPlayer.XAYDA) {
                if (currMapIsCereal != nextMapIsCereal) {
                    if (!currMapIsCereal && nextMapIsCereal) {
                        Service.getInstance().sendThongBao(pl, "Bạn Đã Đến Hành Tinh Cereal");
                        Service.getInstance().sendThongBao(pl, "Vì Là Người Xayda Nên Bạn Bị Giảm 50% Sức Mạnh Từ Lời Nguyền Của Dân Tộc Cereal!");
                    } else {
                        Service.getInstance().sendThongBao(pl, "Bạn Đã Rời Hành Tinh Cereal");
                        Service.getInstance().sendThongBao(pl, "Sức Mạnh Của Bạn Đã Trở Lại Bình Thường!");
                    }
                    Service.gI().point(pl);
                    Service.getInstance().Send_Info_NV(pl);
                }
            }
            if (!pl.isBoss) {
                if (currMapIsQuaKhu != nextMapIsQuaKhu) {
                    if (!currMapIsQuaKhu && nextMapIsQuaKhu) {
                        Service.getInstance().sendThongBaoFromAdmin(pl, "Bạn đã quay về Quá Khứ\b" + "Sức mạnh của bạn sẽ bị giảm 90%");
                    } else {
                        Service.getInstance().sendThongBao(pl, "Bạn đã quay về hiện tại");
                        Service.getInstance().sendThongBao(pl, "Sức mạnh của bạn đã trở về bình thường!");
                    }
                    Service.gI().point(pl);
                    Service.getInstance().Send_Info_NV(pl);
                }
            }
            if (zoneJoin.map.mapId == 47) {
                if (TaskService.gI().getIdTask(pl) == ConstTask.TASK_10_1) {
                    Service.getInstance().CallTauPayPay(pl);
                }
            }
            checkJoinSpecialMap(pl);
            checkJoinMapMaBu(pl);
            TranhNgocService.getInstance().sendUpdateLift(pl);
        } else {
            int plX = pl.location.x;
            if (pl.location.x >= pl.zone.map.mapWidth - 60) {
                plX = pl.zone.map.mapWidth - 60;
            } else if (pl.location.x <= 60) {
                plX = 60;
            }
            Service.gI().resetPoint(pl, plX, pl.location.y);
        }
    }

    public void changeMapWaypoint(Player player) {
        Zone zoneJoin = null;
        WayPoint wp = null;
        if (player.zone == null || player.location == null) {
            resetPoint(player);
            return;
        }
        // Lượt trước chưa nạp xong thì BỎ QUA lượt này.
        //
        // Đây là cửa mà J/K/L và ba nút mũi tên trên điện thoại đều đi vào. Bấm
        // nhanh hai cái là hai lượt đổi bản đồ chồng nhau, và client nhận hai
        // bộ dữ liệu đan xen — "xé hình bản đồ, HP/KI 0/0, bấm gì cũng không
        // ăn". Chặn ở đây thì cái bấm thứ hai chỉ đơn giản là không làm gì.
        //
        // Không tự đặt khoá ở đây: khoá do chính {@code changeMap} đặt, đúng
        // lúc dữ liệu bản đồ bắt đầu chạy xuống client. Đặt sớm hơn thì mọi
        // đường thoát giữa chừng (không có cổng, khu đầy, chưa đủ nhiệm vụ)
        // đều phải nhớ nhả ra — và quên một đường là người chơi kẹt luôn.
        if (dangDoiMap(player)) {
            Service.gI().hideWaitDialog(player);
            resetPoint(player);
            return;
        }
        int xGo = (player.location.x);
        int yGo = player.location.y;

        if (player.zone.map.mapId == 45 || player.zone.map.mapId == 46) {
            int x = player.location.x;
            int y = player.location.y;
            if (x >= 35 && x <= 685 && y >= 550 && y <= 560) {
                xGo = player.zone.map.mapId == 45 ? 420 : 636;
                yGo = 150;
                zoneJoin = MapService.gI().getMapCanJoin(player, player.zone.map.mapId + 1, -1);
            }
        }
        if (zoneJoin == null) {
            wp = MapService.gI().getWaypointPlayerIn(player);
            if (wp == null) {
                // Khong dung tren cong thi lay CONG GAN NHAT cua ban do nay.
                //
                // Nut dich chuyen nhanh (J K L tren may tinh, ba nut mui ten
                // goc trai duoi tren dien thoai) goi thang toi day. Doi phai
                // dung lot trong o cong la o nha bam nut ra lang thi nhan
                // "Ban chua the den khu vuc nay" — trong khi ly do that chi la
                // chua buoc toi cua, va cau tra loi do khong he noi ra dieu ay.
                //
                // Chi bo dung mot doi hoi: phai dung len o cong. Moi cong chan
                // khac giu nguyen — getMapCanJoin va checkMapCanJoin ben duoi
                // van chan nhiem vu chua toi, suc manh chua du, khu day nguoi.
                wp = MapService.gI().getWaypointGanNhat(player);
            }
            if (wp != null) {
                zoneJoin = MapService.gI().getMapCanJoin(player, wp.goMap, -1);
                if (zoneJoin != null) {
                    xGo = wp.goX;
                    yGo = wp.goY;
                }
            }
        }
        if (zoneJoin != null) {
            //Change Map Khi Gas
            if (MapService.gI().shouldChangeMap(player.zone.map.mapId, zoneJoin.map.mapId)) {
                player.iDMark.setZoneKhiGasHuyDiet(zoneJoin);
                player.iDMark.setXMapKhiGasHuyDiet(xGo);
                player.iDMark.setYMapKhiGasHuyDiet(yGo);
                player.type = 3;
                player.maxTime = 5;
                effectChangeMap(player, 5, (byte) 1);
                return;
            }
            //Change Map 144 CDRD
            if (player.isPl() && player.clan != null && player.clan.ConDuongRanDoc != null
                    && player.joinCDRD && player.clan.ConDuongRanDoc.allMobsDead
                    && player.talkToThanMeo && player.zone.map.mapId == 47
                    && zoneJoin.map.mapId == 1) {
                ChangeMapService.gI().changeMapYardrat(player, player.clan.ConDuongRanDoc.getMapById(144), 300 + Util.nextInt(-100, 100), 312);
                player.timeChangeMap144 = System.currentTimeMillis();
                return;
            }
            if (player.HoTongDuongTang && !Util.canDoWithTime(player.lastTimeDuongTang, 15_000)) {
                resetPoint(player);
                Service.gI().sendThongBao(player, "Không thể chuyển map quá nhanh khi đang hộ tống");
                return;
            }
            if (player.HoTongDuongTang) {
                player.lastTimeDuongTang = System.currentTimeMillis();
            }
            // Quang nghi toi thieu giua hai lan qua cong, cho MOI ban do.
            //
            // Ba thay doi:
            //  1. Con so cu la 0 — tuc khong gioi han gi ca, dieu kien luon
            //     dung. Nay lay CHO_QUA_CONG_MS.
            //  2. Bo mien tru cho ban do NHA: nha khong khac gi cac ban do
            //     khac ve mat nay, ma dung la cho de kich lien tuc nhat.
            //  3. Doi cau bao. Cau cu chui nguoi choi vi mot viec ho khong lam
            //     sai — bam nhanh khong phai loi cua ho.
            if (!Util.canDoWithTime(player.MapTransitionTime, CHO_QUA_CONG_MS)) {
                resetPoint(player);
                Service.gI().sendThongBao(player, "Chờ một chút rồi hãy qua tiếp.");
                return;
            }
            player.MapTransitionTime = System.currentTimeMillis();
            changeMap(player, zoneJoin, -1, -1, xGo, yGo, NON_SPACE_SHIP);
        } else {
            resetPoint(player);
            if (MapService.gI().isMapPhoBan(player.zone.map.mapId)) {
                Service.gI().sendThongBao(player, "Chưa hạ hết đối thủ");
                return;
            }
            // Noi ro hai truong hop khac han nhau.
            //
            // Cau cu — "Ban chua the den khu vuc nay" — dung cho ca hai, va
            // trong ca hai deu goi y sai rang nguoi choi thieu dieu kien gi do.
            // Ban do khong co cong nao thi khong phai chuyen dieu kien, con
            // cong co ma khong vao duoc thi ly do nam o dau kia.
            if (wp == null) {
                Service.gI().sendThongBao(player,
                        "Bản đồ này không có cổng đi tiếp.");
                return;
            }
            nro.entity.map.Map dich = getMapById(wp.goMap);
            String ten = (dich == null || dich.mapName == null)
                    ? ("bản đồ " + wp.goMap) : dich.mapName;
            Service.gI().sendThongBao(player, "Chưa vào được " + ten
                    + " lúc này (hay gặp: chưa làm tới nhiệm vụ mở khu vực, "
                    + "hoặc mọi khu đang đầy người).");
        }
    }

    public void resetPoint(Player player) {
        int x = player.location.x;
        if (player.location.x >= player.zone.map.mapWidth - 60) {
            x = player.zone.map.mapWidth - 60;
        } else if (player.location.x <= 60) {
            x = 60;
        }
        Service.gI().resetPoint(player, x, player.location.y);
    }

    public void finishLoadMap(Player player) {
        // Client bao da dung xong ban do: mo khoa cho luot doi ke tiep.
        //
        // Mo khoa o DAU ham chu khong o cuoi: phan duoi co the nem loi (khoi
        // try/catch nuot lang le ngay day la bang chung), va nem loi thi khoa
        // khong bao gio duoc mo — nguoi choi ket, khong doi ban do duoc nua.
        xongDoiMap(player);
        // Hoat anh tau KHONG phat o day — no thuoc ve ban do cu, xem chu thich
        // trong changeMap. Van xoa co cho sach, phong khi con sot lai tu mot
        // luot doi ban do dang do cua ban truoc.
        player.tauChoThaSauKhiNapMap = -1;
        try {
            TaskService.gI().sendUpdateCountSubTask(player);
            player.zone.load_Me_To_Another(player);
            player.zone.load_Another_To_Me(player);
        } catch (Exception e) {
        }
        Service.gI().sendEffAllPlayerMapToMe(player);
        Service.gI().sendEffPlayer(player);
        sendEffectMapToMe(player);
        sendEffectMeToMap(player);
        Service.gI().sendEffMabuHoldAllPlayerMapToMe(player);
        if (player.zone != null && player.zone.map.mapId != 128 && player.maBuHold != null) {
            EffectSkillService.gI().removeMabuHold(player);
        }
        TaskService.gI().checkDoneTaskGoToMap(player, player.zone);
        if (player.fightMabu.pointMabu >= player.fightMabu.POINT_MAX && MapService.gI().isMapMaBu12H(player.zone.map.mapId)) {
            MajinBuu12H.gI().xuongTangDuoi(player);
        }
        if (player.teleTapTuDong) {
            player.teleTapTuDong = false;
            NpcService.gI().createMenuConMeo(player, ConstNpc.TAP_TU_DONG_CONFIRM, -1, player.thongBaoTapTuDong,
                    "Về chỗ cũ", "Ở lại đây");
        }
        if (player.thongBaoChangeMap) {
            player.thongBaoChangeMap = false;
            Service.gI().sendThongBao(player, player.textThongBaoChangeMap);
            player.textThongBaoChangeMap = null;
        }
        if (player.thongBaoThua && player.zone != null && player.zone.map.mapId == player.gender + 21) {
            player.thongBaoThua = false;
            Service.gI().sendThongBao(player, player.textThongBaoThua);
            player.textThongBaoThua = null;
        }
        if (player.isPl() && player.effectSkill != null && player.effectSkill.isBodyChangeTechnique) {
            EffectSkillService.gI().removeBodyChangeTechnique(player);
        }
        try {
            for (int i = player.zone.getPlayers().size() - 1; i >= 0; i--) {
                Player pl = player.zone.getPlayers().get(i);
                if (pl.isPl() && pl.effectSkill != null && pl.effectSkill.isBodyChangeTechnique) {
                    Service.gI().playerInfoUpdate(pl, player, "!Tiểu đội trưởng", 180, 181, 182);
                }
            }
        } catch (Exception e) {
        }
        if (player.zone != null && player.zone.map.mapId == 126) {
            player.zone.sendBigBoss(player);
        }
        if (player.zone != null && player.zone.map.mapId == (21 + player.gender)) {
            if (player.mabuEgg != null) {
                player.mabuEgg.sendMabuEgg();
            }
            if (player.duahau != null) {
                player.duahau.sendDuaHau();
            }
        }
        if (player.zone != null && player.zone.map.mapId == 126) {
            nro.entity.map.hirudegarn.MapHirudegarn.gI().joinMap22h(player);
            player.zone.sendBigBoss(player);
        }
        // Nhiem vu danh hieu loai "toi ban do". Dat o day chu khong o luc bat
        // dau doi: chi khi client bao da dung xong thi nguoi choi moi thuc su
        // CO MAT o ban do do.
        try {
            nro.service.badges.BadgesTaskService.tangTheoLoai(player,
                    nro.entity.badges.BadgesTaskTemplate.DEN_BAN_DO,
                    player.zone == null ? -1 : player.zone.map.mapId, 1);
        } catch (Exception boQua) {
        }
        chayYeuCauDoiMapDangCho(player);
    }

    /**
     * Chạy lượt đổi bản đồ đã bị hoãn, nếu có.
     *
     * <p>Gọi ở <b>cuối</b> {@code finishLoadMap}: bản đồ vừa dựng xong phải được
     * dựng cho trọn — hiệu ứng, người xung quanh, nhiệm vụ — rồi mới rời đi. Bỏ
     * dở giữa chừng thì cảnh cũ để lại rác trên client.</p>
     *
     * <p>Xoá tay cầm <b>trước</b> khi gọi: lượt mới có thể lại bị hoãn lần nữa và
     * tự đặt một yêu cầu khác vào đúng chỗ này, xoá sau là xoá mất nó.</p>
     */
    private void chayYeuCauDoiMapDangCho(Player player) {
        if (player == null || player.yeuCauDoiMapDangCho == null) {
            return;
        }
        YeuCauDoiMap yc = player.yeuCauDoiMapDangCho;
        player.yeuCauDoiMapDangCho = null;
        // Qua cu thi thoi. Goi -39 co the mat han; luc do khoa tu het han sau
        // KHOA_DOI_MAP_HET_HAN_MS va cac luot sau chay binh thuong, nhung yeu
        // cau hoan nay thi nam lai. No ma no ra o mot lan nap ban do nao do rat
        // lau sau se keo nguoi choi di dau do khong duyen co gi.
        if (System.currentTimeMillis() - yc.moc > KHOA_DOI_MAP_HET_HAN_MS * 2) {
            return;
        }
        try {
            changeMap(player, yc.zoneJoin, yc.mapId, yc.zoneId, yc.x, yc.y,
                    yc.typeSpace);
        } catch (Exception ex) {
            nro.core.log.Logger.logException(ChangeMapService.class, ex,
                    "Lỗi chạy lượt đổi bản đồ bị hoãn");
        }
    }

    private void sendEffectMeToMap(Player player) {
        Message msg = null;
        try {
            if (player.effectSkill.isShielding) {
                msg = new Message(-124);
                msg.writer().writeByte(1);
                msg.writer().writeByte(0);
                msg.writer().writeByte(33);
                msg.writer().writeInt((int) player.id);
                Service.gI().sendMessAnotherNotMeInMap(player, msg);
                msg.cleanup();
            }

            if (player.DeTrung != null) {
                msg = new Message(-95);
                msg.writer().writeByte(0);//type
                msg.writer().writeInt((int) player.id);
                msg.writer().writeShort(player.DeTrung.tempId);
                msg.writeCris(Util.CrisGH(player.DeTrung.point.gethp()), Manager.readInt);// hp mob
                Service.gI().sendMessAnotherNotMeInMap(player, msg);
                msg.cleanup();
            }
            if (player.Detu != null && player.Detu.DeTrung != null) {
                msg = new Message(-95);
                msg.writer().writeByte(0);//type
                msg.writer().writeInt((int) player.Detu.DeTrung.id);
                msg.writer().writeShort(player.Detu.DeTrung.tempId);
                msg.writeCris(Util.CrisGH(player.Detu.DeTrung.point.gethp()), Manager.readInt);// hp mob
                Service.gI().sendMessAnotherNotMeInMap(player, msg);
                msg.cleanup();
            }

        } catch (Exception e) {
            Logger.logException(ChangeMapService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void sendEffectMapToMe(Player player) {
        if (player == null || player.zone == null) {
            return;
        }
        Message msg = null;
        try {
            for (Mob mob : player.zone.mobs) {
                if (mob.isDie()) {
                    continue;
                }
                if (mob.effectSkill.isThoiMien) {
                    msg = new Message(-124);
                    msg.writer().writeByte(1); //b5
                    msg.writer().writeByte(1); //b6
                    msg.writer().writeByte(41); //num6
                    msg.writer().writeByte(mob.id); //b7
                    player.sendMessage(msg);
                    msg.cleanup();
                }
                if (mob.effectSkill.isSocola) {
                    msg = new Message(-112);
                    msg.writer().writeByte(1);
                    msg.writer().writeByte(mob.id); //b4
                    msg.writer().writeShort(4133);//b5
                    player.sendMessage(msg);
                    msg.cleanup();
                }
                if (mob.effectSkill.isStun || mob.effectSkill.isBlindDCTT) {
                    msg = new Message(-124);
                    msg.writer().writeByte(1);
                    msg.writer().writeByte(1);
                    msg.writer().writeByte(40);
                    msg.writer().writeByte(mob.id);
                    player.sendMessage(msg);
                    msg.cleanup();
                }
            }
        } catch (IOException e) {
            Logger.logException(ChangeMapService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
        try {
            List<Player> players = player.zone.getHumanoids();
            for (Player pl : players) {
                if (!player.equals(pl)) {

                    if (pl.effectSkill.isShielding) {
                        msg = new Message(-124);
                        msg.writer().writeByte(1);
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(33);
                        msg.writer().writeInt((int) pl.id);
                        player.sendMessage(msg);
                        msg.cleanup();
                    }
                    if (pl.effectSkill.isThoiMien) {
                        msg = new Message(-124);
                        msg.writer().writeByte(1); //b5
                        msg.writer().writeByte(0); //b6
                        msg.writer().writeByte(41); //num3
                        msg.writer().writeInt((int) pl.id); //num4
                        player.sendMessage(msg);
                        msg.cleanup();
                    }
                    if (pl.effectSkill.isBlindDCTT || pl.effectSkill.isStun) {
                        msg = new Message(-124);
                        msg.writer().writeByte(1);
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(40);
                        msg.writer().writeInt((int) pl.id);
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(32);
                        player.sendMessage(msg);
                        msg.cleanup();
                    }

                    if (pl.effectSkill.useTroi) {
                        if (pl.effectSkill.plAnTroi != null) {
                            msg = new Message(-124);
                            msg.writer().writeByte(1); //b5
                            msg.writer().writeByte(0);//b6
                            msg.writer().writeByte(32);//num3
                            msg.writer().writeInt((int) pl.effectSkill.plAnTroi.id);//num4
                            msg.writer().writeInt((int) pl.id);//num9
                            player.sendMessage(msg);
                            msg.cleanup();
                        }
                        if (pl.effectSkill.mobAnTroi != null) {
                            msg = new Message(-124);
                            msg.writer().writeByte(1); //b4
                            msg.writer().writeByte(1);//b5
                            msg.writer().writeByte(32);//num8
                            msg.writer().writeByte(pl.effectSkill.mobAnTroi.id);//b6
                            msg.writer().writeInt((int) pl.id);//num9
                            player.sendMessage(msg);
                            msg.cleanup();
                        }
                    }
                    if (pl.DeTrung != null) {
                        msg = new Message(-95);
                        msg.writer().writeByte(0);//type
                        msg.writer().writeInt((int) pl.id);
                        msg.writer().writeShort(pl.DeTrung.tempId);
                        msg.writeCris(Util.CrisGH(pl.DeTrung.point.gethp()), Manager.readInt);// hp mob
                        player.sendMessage(msg);
                        msg.cleanup();
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void spaceShipArrive(Player player, byte typeSendMSG, byte typeSpace) {
        Message msg = null;
        try {
            msg = new Message(-65);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(typeSpace);
            switch (typeSendMSG) {
                case 0: //cho tất cả
                    Service.gI().sendMessAllPlayerInMap(player, msg);
                    break;
                case 1: //cho bản thân
                    player.sendMessage(msg);
                    break;
                case 2: //cho người chơi trong map
                    Service.gI().sendMessAnotherNotMeInMap(player, msg);
                    break;
            }
        } catch (Exception e) {
            Logger.logException(ChangeMapService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void goToMap(Player player, Zone zoneJoin) {
        Zone oldZone = player.zone;
        if (oldZone != null) {
            this.exitMap(player);
            if (player.DeTrung != null) {
                player.DeTrung.goToMap(zoneJoin);
            }
        }
        player.zone = zoneJoin;
        player.zone.addPlayer(player);
    }

    public void exitMap(Player player) {
        if (player.zone != null) {
            //xử thua pvp
            if (player.pvp != null) {
                player.pvp.lose(player, TYPE_LOSE_PVP.RUNS_AWAY);
            }
            if (player.isPl() && player.zone != null && player.isPKDHVT) {
                player.isPKDHVT = false;
            }
            if (player.isPl() && player.clan != null && player.clan.ConDuongRanDoc != null
                    && player.joinCDRD && player.clan.ConDuongRanDoc.allMobsDead && player.talkToThanMeo) {
                player.timeChangeMap144 = System.currentTimeMillis();
            }
            BlackBallWarService.gI().dropBlackBall(player);
            if (player.effectSkill.useTroi) {
                EffectSkillService.gI().removeUseTroi(player);
            }
            if (player.effectSkin.xHPKI > 1 && !MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
                player.effectSkin.xHPKI = 1;
                Service.gI().point(player);
            }
            if (player.effectSkin.xDame > 1 && !MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
                player.effectSkin.xDame = 1;
                Service.gI().point(player);
            }
            player.zone.removePlayer(player);
            if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
                Message msg = null;
                try {
                    msg = new Message(-6);
                    msg.writer().writeInt((int) player.id);
                    Service.gI().sendMessAnotherNotMeInMap(player, msg);
                    player.zone = null;
                } catch (Exception e) {
                    Logger.logException(MapService.class, e);
                } finally {
                    if (msg != null) {
                        msg.cleanup();
                    }
                }
            }
        }
    }

    private void effectChangeMap(Player player, int seconds, byte type) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(seconds);
            msg.writer().writeByte(type);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(ChangeMapService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public Zone checkMapCanJoinByYardart(Player player, Zone zoneJoin) {
        if ((!player.isBoss && !player.isFounder()) && (zoneJoin.map.mapId == 122 || zoneJoin.map.mapId == 123 || zoneJoin.map.mapId == 124)) {
            return null;
        }
        return zoneJoin;
    }

    public void changeMapNonSpaceship(Player player, int mapid, int x, int y) {
        Zone zone = getMapCanJoin(player, mapid);
        ChangeMapService.gI().changeMap(player, zone, -1, -1, x, y, NON_SPACE_SHIP);
    }

    public Zone getMapCanJoin(Player player, int mapId) {
        if (MapService.gI().isMapOffline(player.zone.map.mapId) || MapService.gI().isMapBangHoi(player.zone.map.mapId)) {
            return getZoneJoinByMapIdAndZoneId(player, mapId, 0);
        }
        Zone mapJoin = null;
        Map map = getMapById(mapId);
        for (Zone zone : map.zones) {
            if (zone.getNumOfPlayers() < Zone.PLAYERS_TIEU_CHUAN_TRONG_MAP) {
                mapJoin = zone;
                break;
            }
        }
        return mapJoin;
    }

    public Zone getZoneJoinByMapIdAndZoneId(Player player, int mapId, int zoneId) {
        Map map = getMapById(mapId);
        Zone zoneJoin = null;
        try {
            if (map != null) {
                zoneJoin = map.zones.get(zoneId);
            }
        } catch (Exception e) {
        }
        return zoneJoin;
    }

    public Map getMapById(int mapId) {
        for (Map map : Manager.MAPS) {
            if (map.mapId == mapId) {
                return map;
            }
        }
        return null;
    }

    public void changeMapTuQuaKhuVeHienTai(Player pl, int mapId, int zone, int x) {
        if (!pl.isBoss) {
            if (pl.isDie()) {
                if (pl.haveTennisSpaceShip) {
                    Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
                } else {
                    Service.gI().hsChar(pl, 1, 1);
                }
            } else {
                if (pl.haveTennisSpaceShip) {
                    pl.nPoint.setFullHpMp();
                    PlayerService.gI().sendInfoHpMp(pl);
                }
            }
            changeMap(pl, null, mapId, zone, x, 5, AUTO_SPACE_SHIP);
        }
    }

    public void goToHome(Player player) {
        if (!player.iDMark.isGoToHome()) {
            player.iDMark.setLastTimeGoToHome(System.currentTimeMillis());
            player.iDMark.setGoToHome(true);

        }
    }

    public void goToTuongLai(Player player) {
        if (!player.iDMark.isGotoFuture()) {
            player.iDMark.setLastTimeGoToFuture(System.currentTimeMillis());
            player.iDMark.setGotoFuture(true);
            player.type = 0;
            spaceShipArrive(player, (byte) 1, player.haveTennisSpaceShip ? TENNIS_SPACE_SHIP : DEFAULT_SPACE_SHIP);
            effectChangeMap(player, 60, EFFECT_GO_TO_TUONG_LAI);
        }
    }

    public void goToDBKB(Player player) {
        if (!player.iDMark.isGoToBDKB()) {
            if (Util.isAfterMidnight(player.lastTimeJoinBDKB)) {
                player.timesPerDayBDKB = 1;
            } else if (player.lastTimeJoinBDKB != player.clan.lastTimeOpenBanDoKhoBau) {
                player.lastTimeJoinBDKB = player.clan.lastTimeOpenBanDoKhoBau;
                player.timesPerDayBDKB++;
                if (player.timesPerDayBDKB > 3) {
                    Service.gI().sendThongBao(player, "Bạn đã vào hang kho báu 3 lần trong hôm nay, hẹn gặp lại ngày mai");
                    return;
                }
            }
            player.iDMark.setLastTimeGoToBDKB(System.currentTimeMillis());
            player.iDMark.setGoToBDKB(true);
            player.type = 1;
            player.maxTime = 5;
            effectChangeMap(player, 5, EFFECT_GO_TO_BDKB);
        }
    }

    public void goToQuaKhu(Player player) {
        ChangeMapService.this.changeMapBySpaceShip(player, 24, -1, -1);
    }

    public void goToPotaufeu(Player player) {
        ChangeMapService.this.changeMapBySpaceShip(player, 139, -1, Util.nextInt(60, 200));
    }

    public void DuHanhThoiGian(Player player) {
        if (!player.iDMark.isGoToDuHanhThoiGian()) {
            player.iDMark.setLastTimeDuHanhThoiGian(System.currentTimeMillis());
            player.iDMark.setGoToDuHanhThoiGian(true);
            spaceShipArrive(player, (byte) 1, DEFAULT_SPACE_SHIP);
            effectChangeMap(player, 7, EFFECT_GO_TO_BDKB);
            player.type = 8;
            player.maxTime = 10;
        }
    }

    public void TroVeThoiGian(Player player) {
        if (!player.iDMark.isGoToTroVeThoiGian()) {
            player.iDMark.setLastTimeTroVeThoiGian(System.currentTimeMillis());
            player.iDMark.setGoToTroVeThoiGian(true);
            spaceShipArrive(player, (byte) 1, DEFAULT_SPACE_SHIP);
            effectChangeMap(player, 7, EFFECT_GO_TO_BDKB);
            player.type = 8;
            player.maxTime = 10;
        }
    }

    public void TroVeThoiGian2(Player player) {
        if (!player.iDMark.isGoToTroVeThoiGian2()) {
            player.iDMark.setLastTimeTroVeThoiGian2(System.currentTimeMillis());
            player.iDMark.setGoToTroVeThoiGian2(true);
            spaceShipArrive(player, (byte) 1, DEFAULT_SPACE_SHIP);
            effectChangeMap(player, 7, EFFECT_GO_TO_BDKB);
            player.type = 8;
            player.maxTime = 10;
        }
    }

    //kiểm tra map có thể vào với nhiệm vụ hiện tại
    public Zone checkMapCanJoin(Player player, Zone zoneJoin) {
        Item SoDo = InventoryService.gI().findItemBag(player, 9999);
        if (zoneJoin == null) {
            return null;
        }
        if (zoneJoin.map.mapId == -1 || zoneJoin.map.mapId == -1) {
            return null;
        }
        if (player.isDeTu || player.isNguoiYeu || player.isConOne || player.isConTwo || player.isConThree || player.isPhanThan || player.isBoss || player.isPetFollow || player.isDuongTang) {
            return zoneJoin;
        }
        if (zoneJoin != null) {
            switch (zoneJoin.map.mapId) {
                case 1: //đồi hoa cúc
                case 8: //đồi nấm tím
                case 15: //đồi hoang
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_1_0) {
                        return null;
                    }
                    break;
                case 42: //vách aru
                case 43: //vách moori
                case 44: //vách kakarot
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_2_0) {
                        return null;
                    }
                    break;
                case 2: //thung lũng tre
                case 9: //thị trấn moori
                case 16: //làng plane
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_3_0) {
                        return null;
                    }
                    break;
                case 24: //trạm tàu vũ trụ trái đất
                case 25: //trạm tàu vũ trụ namếc
                case 26: //trạm tàu vũ trụ xayda
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_4_0) {
                        return null;
                    }
                    break;
                case 3: //rừng nấm
                case 11: //thung lũng maima
                case 17: //rừng nguyên sinh
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_7_0) {
                        return null;
                    }
                    break;
                case 27: //rừng bamboo
                case 28: //rừng dương xỉ
                case 31: //núi hoa vàng
                case 32: //núi hoa tím
                case 35: //rừng cọ
                case 36: //rừng đá
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_13_0) {
                        return null;
                    }
                    break;
                case 30: //đảo bulong
                case 34: //đông nam guru
                case 38: //bờ vực đen
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_15_0) {
                        return null;
                    }
                    break;
                case 6: //đông karin
                case 10: //thung lũng namếc
                case 19: //thành phố vegeta
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_16_0) {
                        return null;
                    }
                    break;
                case 68: //thung lũng nappa
                case 69: //vực cấm
                case 70: //núi appule
                case 71: //căn cứ rasphery
                case 72: //thung lũng rasphery
                case 64: //núi dây leo
                case 65: //núi cây quỷ
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
                        return null;
                    }
                    break;
                case 63: //trại lính fide
                case 66: //trại quỷ già
                case 67: //vực chết
                case 73: //thung lũng chết
                case 74: //đồi cây fide
                case 75: //khe núi tử thần
                case 76: //núi đá
                case 77: //rừng đá
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_21_0) {
                        return null;
                    }
                    break;
                case 81: //hang quỷ chim
                case 82: //núi khỉ đen
                case 83: //hang khỉ đen
                case 79: //núi khỉ đỏ
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_22_0) {
                        return null;
                    }
                    break;
                case 80: //núi khỉ vàng
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
                        return null;
                    }
                    break;
                case 105: //cánh đồng tuyết
                case 106: //rừng tuyết
                case 107: //núi tuyết
                case 108: //dòng sông băng
                case 109: //rừng băng
                case 110: //hang băng
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_29_0) {
                        return null;
                    }
                    break;
                case 102: //nhà bunma
                case 92: //thành phố phía đông
                case 93: //thành phố phía nam
                case 94: //đảo balê
                case 96: //cao nguyên
                case 97: //thành phố phía bắc
                case 98: //ngọn núi phía bắc
                case 99: //thung lũng phía bắc
                case 100: //thị trấn ginder
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
                        return null;
                    }
                    break;
                case 103: //võ đài xên
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_29_0) {
                        return null;
                    }
                    break;
                case 111://dnamkr
                    if (player.nPoint.power >= 18_000_000_000L) {
                        Service.gI().sendThongBao(player, "Sức mạnh yêu cầu Dưới 18 Tỷ");
                        return null;
                    }
                    break;
//                case 21:
//                case 22:
//                case 23:
//                    if (SoDo != null) {
//                        Service.gI().sendThongBao(player, "Bạn không có Sổ Đỏ nên không thể về nhà!");
//                        return null;
//                    }
//                    break;
                case 45:
//                    if (player.levelLuyenTap < 2) {
//                        Service.gI().sendThongBao(player, "Hãy đánh bại Thần Mèo Karin trước đã!");
//                        Service.gI().setPos(player, player.location.x, 408);
//                        return null;
//                    }
//                    break;
                case 48:
//                    if (player.levelLuyenTap < 4) {
//                        Service.gI().sendThongBao(player, "Hãy đánh bại Thượng Đế trước đã!");
//                        return null;
//                    }
//                    break;
                case 50:
                case 154:
//                    if (player.levelLuyenTap < 6) {
//                        Service.gI().sendThongBao(player, "Hãy đánh bại Thần Vũ Trụ trước đã!");
//                        return null;
//                    }
//                    break;
            }
        }
        if (zoneJoin != null) {
            switch (player.gender) {
                case ConstPlayer.TRAI_DAT:
                    if (zoneJoin.map.mapId == 22 || zoneJoin.map.mapId == 23) {
                        zoneJoin = null;
                    }
                    break;
                case ConstPlayer.NAMEC:
                    if (zoneJoin.map.mapId == 21 || zoneJoin.map.mapId == 23) {
                        zoneJoin = null;
                    }
                    break;
                case ConstPlayer.XAYDA:
                    if (zoneJoin.map.mapId == 21 || zoneJoin.map.mapId == 22) {
                        zoneJoin = null;
                    }
                    break;
            }
        }
        return zoneJoin;
    }

    private void checkJoinSpecialMap(Player player) {
        if (player != null && player.zone != null) {
            switch (player.zone.map.mapId) {
                //map ngọc rồng đen
                case 85:
                case 86:
                case 87:
                case 88:
                case 89:
                case 90:
                case 91:
                    BlackBallWarService.gI().joinMapBlackBallWar(player);
                    break;
            }
        }
    }

    private void checkJoinMapMaBu(Player player) {
        if (player != null && player.zone != null) {
            switch (player.zone.map.mapId) {
                //map mabu
                case 114:
                case 115:
                case 117:
                case 118:
                case 119:
                case 120:
                    MajinBuu12H.gI().joinMapMabu(player);
                    break;
            }
        }
    }

    public Zone getZoneTranhNgoc(byte type) {
        Zone z = null;
        Map map = MapService.gI().getMapById(ConstTranhNgocNamek.MAP_ID);
        if (map != null) {
            for (int i = 0; i < map.zones.size(); i++) {
                Zone zone = map.zones.get(i);
                if (type == 1 && zone.getPlayersCadic().size() < 10) {
                    z = zone;
                    break;
                } else if (type == 2 && zone.getPlayersFide().size() < 10) {
                    z = zone;
                    break;
                }
            }
        }
        return z;
    }

   
}
