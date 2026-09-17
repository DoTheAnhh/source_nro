package nro.service.hotong;

import java.util.List;
import nro.core.consts.ConstPlayer;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.map.ItemMap;
import nro.entity.map.WayPoint;
import nro.entity.map.Zone;
import nro.entity.player.DuongTang;
import nro.entity.player.Player;
import nro.repository.dao.HoTongDAO;
import nro.service.MapService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.service.fun.ChangeMapService;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Nhiệm vụ <b>hộ tống Đường Tăng</b> từ Làng Aru sang Đảo Kamê.
 *
 * <h2>Đường Tăng tự đi, người chơi bám theo</h2>
 *
 * <p>Bản cũ ngược lại: Đường Tăng bám theo người chơi, nên nhiệm vụ chỉ là
 * chạy tới đích rồi quay đầu. Nay ông ấy <b>tự đi</b> theo tuyến năm bản đồ
 * ({@link #TUYEN}) với tốc độ khai trên panel, còn người chơi phải theo sát:
 * cách quá {@code khoangCachMap} bản đồ là hỏng việc.</p>
 *
 * <h2>Không cần Vòng Kim Cô, không tích điểm công đức</h2>
 *
 * <p>Cả hai thứ đó đã bỏ. Phần thưởng nay là một danh sách vật phẩm khai trên
 * panel, mỗi món có khoảng số lượng và tỉ lệ rơi riêng.</p>
 */
public final class HoTongService {

    private static HoTongService i;

    public static HoTongService gI() {
        if (i == null) {
            i = new HoTongService();
        }
        return i;
    }

    /**
     * Tuyến đi: Làng Aru → Đồi hoa cúc → Thung lũng tre → Rừng nấm → Rừng xương.
     *
     * <p>Đích không phải là Đảo Kamê mà là <b>cửa sang Đảo Kamê</b> nằm ở mép
     * phải Rừng xương: tới được cửa là xong việc, không phải bước qua.</p>
     */
    public static final int[] TUYEN = {0, 1, 2, 3, 4};

    /** Bản đồ nằm bên kia cửa đích — dùng để tìm đúng cửa ở bản đồ cuối. */
    private static final int MAP_DICH = 5;

    /** Đứng cách cửa trong bấy nhiêu điểm ảnh thì coi như đã tới. */
    private static final int GAN_CUA = 40;

    /**
     * Vị trí của một bản đồ trong tuyến, hoặc -1 nếu không thuộc tuyến.
     */
    public static int thuTuTrenTuyen(int mapId) {
        for (int k = 0; k < TUYEN.length; k++) {
            if (TUYEN[k] == mapId) {
                return k;
            }
        }
        return -1;
    }

    // =====================================================================
    //  Bắt đầu
    // =====================================================================
    /**
     * Người chơi nhận nhiệm vụ.
     *
     * @return câu báo lỗi, hoặc {@code null} nếu đã bắt đầu
     */
    public String batDau(Player pl) {
        if (pl == null) {
            return "Không thực hiện được.";
        }
        HoTongDAO.CauHinh c = HoTongDAO.cauHinh();
        if (!c.bat) {
            return "Nhiệm vụ hộ tống đang tạm đóng.";
        }
        if (pl.HoTongDuongTang) {
            return "Bạn đang hộ tống rồi.";
        }
        long con = conLaiMs(pl);
        if (con > 0) {
            return "Chưa tới lượt hộ tống — còn " + moTaThoiGian(con) + ".";
        }
        if (pl.zone == null || pl.zone.map.mapId != TUYEN[0]) {
            return "Phải đứng ở Làng Aru mới nhận được nhiệm vụ.";
        }
        nro.service.DetuService.DuongTang(pl);
        if (pl.Duongtang == null) {
            return "Không gọi được Đường Tăng.";
        }
        pl.HoTongDuongTang = true;
        pl.lastTimeDuongTang = System.currentTimeMillis();
        if (c.batCo) {
            // Bat co den: ai cung danh duoc nguoi dang ho tong, nen chuyen di
            // thanh cuoc giành nhau chu khong phai mot vong dao choi.
            PlayerService.gI().changeAndSendTypePK(pl, ConstPlayer.PK_ALL);
            Service.gI().sendThongBao(pl,
                    "Cờ đen đã bật — người khác có thể tấn công bạn suốt chuyến đi.");
        }
        Service.gI().sendThongBao(pl, "Hãy đi cùng Đường Tăng tới Đảo Kamê. "
                + "Không được cách ông ấy quá " + c.khoangCachMap + " bản đồ.");
        return null;
    }

    // =====================================================================
    //  Mỗi nhịp
    // =====================================================================
    /**
     * Một nhịp đi của Đường Tăng. Gọi từ {@code DuongTang.update()}.
     */
    public void nhip(DuongTang dt) {
        if (dt == null) {
            return;
        }
        Player chu = dt.master;
        if (chu == null || !chu.HoTongDuongTang) {
            return;
        }
        if (dt.zone == null) {
            return;
        }
        HoTongDAO.CauHinh c = HoTongDAO.cauHinh();
        int viTri = thuTuTrenTuyen(dt.zone.map.mapId);
        if (viTri < 0) {
            // Lac khoi tuyen — coi nhu hong viec, con hon de ong ay di lang
            // thang mai khong bao gio toi dich.
            thatBai(chu, "Đường Tăng lạc khỏi đường đi!");
            return;
        }
        if (!conGanChu(chu, dt, c.khoangCachMap)) {
            thatBai(chu, "Bạn đã cách Đường Tăng quá xa!");
            return;
        }
        if (!Util.canDoWithTime(dt.lucBuocCuoi, c.msMoiBuoc)) {
            return;
        }
        dt.lucBuocCuoi = System.currentTimeMillis();

        WayPoint cua = cuaDiTiep(dt.zone, viTri);
        if (cua == null) {
            // Ban do nay khong co cua di tiep: dung yen cho chu, khong bao hong
            // — loi cua du lieu ban do chu khong phai loi nguoi choi.
            return;
        }
        int xCua = (cua.minX + cua.maxX) / 2;
        int khoang = xCua - dt.location.x;
        if (khoang > -GAN_CUA && khoang < GAN_CUA) {
            toiCua(dt, cua, viTri, chu);
            return;
        }
        int buoc = (khoang > 0) ? c.buocDiemAnh : -c.buocDiemAnh;
        int xMoi = dt.location.x + buoc;
        int yMoi = dt.zone.map.yPhysicInTop(xMoi, dt.location.y);
        PlayerService.gI().playerMove(dt, xMoi, yMoi);
    }

    /** Tới cửa: qua bản đồ kế, hoặc xong việc nếu đó là cửa đích. */
    private void toiCua(DuongTang dt, WayPoint cua, int viTri, Player chu) {
        if (viTri >= TUYEN.length - 1) {
            thanhCong(chu);
            return;
        }
        Zone z = MapService.gI().getMapWithRandZone(cua.goMap);
        if (z == null) {
            return;
        }
        ChangeMapService.gI().changeMap(dt, z, cua.goX, cua.goY);
        Service.gI().chat(dt, "Đi tiếp thôi Ngộ Không!");
    }

    /**
     * Cửa dẫn đi tiếp ở bản đồ đang đứng.
     *
     * <p>Ở bốn bản đồ đầu, cửa là lối sang bản đồ kế trong tuyến. Ở bản đồ
     * cuối (Rừng xương), cửa là lối sang Đảo Kamê — <b>đích</b>.</p>
     */
    private WayPoint cuaDiTiep(Zone z, int viTri) {
        int mapKe = (viTri >= TUYEN.length - 1) ? MAP_DICH : TUYEN[viTri + 1];
        if (z.map.wayPoints == null) {
            return null;
        }
        for (WayPoint w : z.map.wayPoints) {
            if (w != null && w.goMap == mapKe) {
                return w;
            }
        }
        return null;
    }

    /**
     * Người chơi có còn đủ gần không — đếm theo <b>thứ tự trên tuyến</b>.
     *
     * <p>Ở ngoài tuyến hẳn (chui vào nhà, vào map sự kiện) thì tính là quá xa:
     * không có cách nào đo khoảng cách giữa hai chỗ không nối với nhau.</p>
     */
    private boolean conGanChu(Player chu, DuongTang dt, int toiDa) {
        if (chu.zone == null) {
            return false;
        }
        int a = thuTuTrenTuyen(chu.zone.map.mapId);
        int b = thuTuTrenTuyen(dt.zone.map.mapId);
        if (a < 0 || b < 0) {
            return false;
        }
        int lech = a - b;
        if (lech < 0) {
            lech = -lech;
        }
        return lech <= toiDa;
    }

    // =====================================================================
    //  Kết thúc
    // =====================================================================
    public void thatBai(Player chu, String vi) {
        if (chu == null) {
            return;
        }
        ketThuc(chu);
        Service.gI().sendThongBao(chu, vi + "\nNhiệm vụ hộ tống thất bại!");
    }

    public void thanhCong(Player chu) {
        if (chu == null) {
            return;
        }
        ketThuc(chu);
        HoTongDAO.ghiLanCuoi((int) chu.id, System.currentTimeMillis());
        Service.gI().sendThongBao(chu,
                "Đường Tăng đã tới Đảo Kamê an toàn. Đa tạ thí chủ!");
        traQua(chu);
    }

    /** Dọn dẹp chung cho cả hai kết cục. */
    private void ketThuc(Player chu) {
        chu.HoTongDuongTang = false;
        if (chu.Duongtang != null) {
            ChangeMapService.gI().exitMap(chu.Duongtang);
            chu.Duongtang.dispose();
            chu.Duongtang = null;
        }
        if (chu.typePk == ConstPlayer.PK_ALL) {
            // Co bat theo nhiem vu thi tat theo nhiem vu. Nguoi choi tu bat co
            // tu truoc cung bi tat lay — thua con hon de ho mang co suot ngay
            // vi mot chuyen di da xong.
            PlayerService.gI().changeAndSendTypePK(chu, 0);
        }
    }

    // =====================================================================
    //  Phần thưởng
    // =====================================================================
    /**
     * Trả quà: vào <b>rương ở nhà</b>, rương đầy thì rơi xuống đất.
     *
     * <p>Rơi xuống đất có ghi tên chủ, nên người khác đi qua nhặt không được
     * (xem {@code Zone.pickItem}).</p>
     */
    private void traQua(Player chu) {
        List<HoTongDAO.Qua> ds = HoTongDAO.dsQua();
        if (ds.isEmpty()) {
            Service.gI().sendThongBao(chu,
                    "Chưa khai phần thưởng nào cho nhiệm vụ này — báo quản trị.");
            return;
        }
        int daCho = 0;
        for (HoTongDAO.Qua q : ds) {
            if (!q.bat) {
                continue;
            }
            if (Util.nextInt(1, 1000) > q.tiLe) {
                continue;
            }
            int sl = (q.slMax > q.slMin)
                    ? Util.nextInt(q.slMin, q.slMax) : q.slMin;
            if (sl < 1) {
                sl = 1;
            }
            if (traMotMon(chu, q.itemId, sl)) {
                daCho++;
            }
        }
        if (daCho == 0) {
            Service.gI().sendThongBao(chu,
                    "Chuyến này không bốc được món nào — chúc may mắn lần sau.");
        }
    }

    private boolean traMotMon(Player chu, int itemId, int sl) {
        try {
            Item it = ItemService.gI().createNewItem((short) itemId, sl);
            if (it == null || it.template == null) {
                return false;
            }
            if (InventoryService.gI().addItemBox(chu, it)) {
                InventoryService.gI().sendItemBox(chu);
                Service.gI().sendThongBao(chu, "Rương đồ nhận được "
                        + sl + " " + it.template.name + ".");
                return true;
            }
            if (chu.zone != null) {
                Service.gI().dropItemMap(chu.zone, new ItemMap(chu.zone, itemId,
                        sl, chu.location.x, chu.location.y, chu.id));
                Service.gI().sendThongBao(chu, "Rương đã đầy — " + sl + " "
                        + it.template.name + " rơi xuống đất, chỉ bạn nhặt được.");
                return true;
            }
            return false;
        } catch (Exception ex) {
            Logger.logException(HoTongService.class, ex,
                    "Lỗi trả quà hộ tống cho " + chu.name);
            return false;
        }
    }

    // =====================================================================
    //  Thời gian hồi
    // =====================================================================
    /** Còn bao nhiêu mili giây nữa mới tới lượt hộ tống tiếp theo. */
    public long conLaiMs(Player pl) {
        if (pl == null) {
            return 0L;
        }
        HoTongDAO.CauHinh c = HoTongDAO.cauHinh();
        if (c.phutHoi <= 0) {
            return 0L;
        }
        long cuoi = HoTongDAO.lanCuoi((int) pl.id);
        if (cuoi <= 0) {
            return 0L;
        }
        long het = cuoi + c.phutHoi * 60_000L;
        long con = het - System.currentTimeMillis();
        return (con > 0) ? con : 0L;
    }

    /** "1 giờ 5 phút", "3 phút", "40 giây" — chỉ nói phần đáng nói. */
    public static String moTaThoiGian(long ms) {
        long giay = ms / 1000L;
        long gio = giay / 3600L;
        long phut = (giay % 3600L) / 60L;
        long le = giay % 60L;
        if (gio > 0) {
            return gio + " giờ " + phut + " phút";
        }
        if (phut > 0) {
            return phut + " phút " + le + " giây";
        }
        return le + " giây";
    }
}
