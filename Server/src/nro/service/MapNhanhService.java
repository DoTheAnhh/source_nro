package nro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.consts.ConstNpc;
import nro.entity.player.Player;
import nro.repository.dao.MapNhanhDAO;
import nro.service.fun.ChangeMapService;

/**
 * Menu <b>Bản đồ</b> — dịch chuyển nhanh tới một bản đồ bất kỳ.
 *
 * <h2>Đường đi</h2>
 *
 * <ol>
 *   <li>Người chơi bấm "Bản đồ" trong menu Chức Năng của client. Client gửi lên
 *       một dòng chat {@code bando} (xem {@code Command.check}).</li>
 *   <li>{@link #moMenuNhom(Player)} mở menu tầng một: danh sách <b>nhóm</b>.</li>
 *   <li>{@link #chonNhom(Player, int)} nhớ nhóm vừa chọn rồi mở tầng hai:
 *       các bản đồ trong nhóm đó, cộng một mục "Quay lại".</li>
 *   <li>{@link #chonDiem(Player, int)} dịch chuyển.</li>
 * </ol>
 *
 * <h2>Vì sao phải nhớ nhóm</h2>
 *
 * <p>Client chỉ gửi lại <b>vị trí</b> mục vừa bấm, không gửi kèm nội dung. Menu
 * tầng hai thì mỗi nhóm một danh sách khác nhau, nên nếu không nhớ người chơi
 * đang mở nhóm nào thì vị trí đó vô nghĩa. {@link #NHOM_DANG_XEM} giữ tên nhóm
 * giữa hai lần bấm.</p>
 *
 * <p>Danh sách bản đồ đọc lại từ CSDL ở <i>cả hai</i> bước, nên admin sửa trên
 * panel là có hiệu lực ngay với người đang mở menu — không phải khởi động lại
 * máy chủ.</p>
 */
public class MapNhanhService {

    private static MapNhanhService instance;

    public static MapNhanhService gI() {
        if (instance == null) {
            instance = new MapNhanhService();
        }
        return instance;
    }

    /** Nhóm mà mỗi người chơi đang mở, giữa lúc bấm nhóm và lúc bấm bản đồ. */
    private static final Map<Long, String> NHOM_DANG_XEM
            = new ConcurrentHashMap<>();

    /** Nhãn của mục cuối trong menu tầng hai. */
    private static final String QUAY_LAI = "Quay lại";

    /** Mở menu tầng một — danh sách nhóm. */
    public void moMenuNhom(Player player) {
        if (player == null || player.iDMark == null) {
            return;
        }
        List<String> nhom = new ArrayList<>(MapNhanhDAO.theoNhom().keySet());
        if (nhom.isEmpty()) {
            Service.gI().sendThongBao(player,
                    "Chưa có bản đồ nào trong danh sách dịch chuyển.");
            return;
        }
        NpcService.gI().createMenuConMeo(player, ConstNpc.BAN_DO_NHOM, -1,
                "Chọn khu vực muốn tới:",
                nhom.toArray(new String[0]));
    }

    /** Người chơi vừa bấm một nhóm ở tầng một. */
    public void chonNhom(Player player, int viTri) {
        if (player == null || player.iDMark == null) {
            return;
        }
        List<String> nhom = new ArrayList<>(MapNhanhDAO.theoNhom().keySet());
        if (viTri < 0 || viTri >= nhom.size()) {
            // Danh sach vua doi ngay giua hai lan bam — mo lai tang mot con hon
            // la dich chuyen nham cho.
            moMenuNhom(player);
            return;
        }
        String ten = nhom.get(viTri);
        NHOM_DANG_XEM.put(player.id, ten);
        moMenuDiem(player, ten);
    }

    private void moMenuDiem(Player player, String tenNhom) {
        List<MapNhanhDAO.Diem> ds = diemCuaNhom(tenNhom);
        if (ds.isEmpty()) {
            moMenuNhom(player);
            return;
        }
        String[] muc = new String[ds.size() + 1];
        for (int i = 0; i < ds.size(); i++) {
            muc[i] = ds.get(i).nhan();
        }
        muc[ds.size()] = QUAY_LAI;
        NpcService.gI().createMenuConMeo(player, ConstNpc.BAN_DO_DIEM, -1,
                tenNhom + " — chọn bản đồ:", muc);
    }

    /** Người chơi vừa bấm một bản đồ ở tầng hai. */
    public void chonDiem(Player player, int viTri) {
        if (player == null || player.iDMark == null) {
            return;
        }
        String tenNhom = NHOM_DANG_XEM.get(player.id);
        if (tenNhom == null) {
            moMenuNhom(player);
            return;
        }
        List<MapNhanhDAO.Diem> ds = diemCuaNhom(tenNhom);
        if (viTri < 0 || viTri >= ds.size()) {
            // Vi tri cuoi la "Quay lai"; vuot qua cung coi nhu quay lai.
            moMenuNhom(player);
            return;
        }
        dichChuyen(player, ds.get(viTri));
    }

    private List<MapNhanhDAO.Diem> diemCuaNhom(String tenNhom) {
        List<MapNhanhDAO.Diem> ds = MapNhanhDAO.theoNhom().get(tenNhom);
        return ds == null ? new ArrayList<MapNhanhDAO.Diem>() : ds;
    }

    /**
     * Thả người chơi xuống bản đồ đích.
     *
     * <p>Để {@code x} hoặc {@code y} âm là tự tính: {@code x} lấy giữa bản đồ,
     * {@code y} lấy mặt đất ngay dưới đó — thả bừa một toạ độ cứng thì có bản
     * đồ người chơi rơi vào lòng đất hoặc lơ lửng trên không.</p>
     *
     * <h2>Không đi được thì đứng nguyên tại chỗ</h2>
     *
     * <p>Rất nhiều bản đồ có cổng chặn riêng — phần lớn là <b>chưa làm tới
     * nhiệm vụ</b> mở khu vực đó ({@code ChangeMapService.checkMapCanJoin}),
     * ngoài ra còn map nhà sai hành tinh, Đông Nam Karin đòi sức mạnh dưới 18
     * tỷ, và trường hợp mọi khu của bản đồ đều chật.</p>
     *
     * <p>Hàm này hỏi <b>chính những cổng chặn đó</b> trước khi chuyển. Không
     * qua được thì dừng lại tại bản đồ hiện tại và nói rõ lý do — người chơi
     * không bị dịch chuyển nửa chừng, cũng không phải đoán vì sao bấm mà không
     * thấy gì.</p>
     *
     * <p>Sau khi chuyển vẫn <b>đối chiếu lại</b> bản đồ đang đứng: bên trong
     * {@code changeMap} còn nhiều đường lặng lẽ trả người chơi về chỗ cũ (đeo
     * Ngọc Rồng Namếc mà đổi map quá nhanh, khu vừa đầy nốt...) mà không báo gì
     * cả. Không đối chiếu thì có lúc báo "Đã tới" trong khi người chơi vẫn đứng
     * yên — sai còn tệ hơn im lặng.</p>
     */
    public void dichChuyen(Player player, MapNhanhDAO.Diem d) {
        if (player == null || d == null) {
            return;
        }
        if (player.zone == null || player.zone.map == null) {
            Service.gI().sendThongBao(player, "Chưa vào bản đồ nào.");
            return;
        }
        if (player.isDie()) {
            Service.gI().sendThongBao(player,
                    "Đang chết, hồi sinh đã rồi hãy đi.");
            return;
        }
        // Luot doi ban do truoc chua nap xong thi cho.
        if (nro.service.fun.ChangeMapService.dangDoiMap(player)) {
            Service.gI().hideWaitDialog(player);
            Service.gI().sendThongBao(player, "Đang chuyển bản đồ, chờ một chút.");
            return;
        }
        int mapId = d.theoHanhTinh ? d.mapId + player.gender : d.mapId;
        nro.entity.map.Map map = ChangeMapService.gI().getMapById(mapId);
        if (map == null) {
            Service.gI().sendThongBao(player,
                    "Bản đồ " + mapId + " chưa có trên máy chủ.");
            return;
        }
        if (player.zone.map.mapId == mapId) {
            Service.gI().sendThongBao(player, "Đang ở " + d.nhan() + " rồi.");
            return;
        }

        // Chon san khu se vao, va dung CHINH khu do khi chuyen. Goi
        // changeMapNonSpaceship thi no do lai mot lan nua va co the ra khu
        // khac — kiem tra mot dang, di mot neo.
        nro.entity.map.Zone dich = ChangeMapService.gI().getMapCanJoin(player, mapId);
        if (dich == null) {
            Service.gI().sendThongBao(player, "Khu nào của " + d.nhan()
                    + " cũng đang đầy người. Vẫn ở "
                    + player.zone.map.mapName + ".");
            return;
        }
        // Cong chan cua chinh may chu — hoi truoc, khong tu doan dieu kien.
        // Danh sach dieu kien nam trong checkMapCanJoin va con doi theo tung
        // ban cap nhat; chep lai o day la co ngay hai ban le nhau.
        if (ChangeMapService.gI().checkMapCanJoin(player, dich) == null) {
            // Khong khang dinh chac ly do: rieng Dong Nam Karin thi
            // checkMapCanJoin da tu bao "Suc manh yeu cau Duoi 18 Ty" roi, noi
            // chac cu the o day la mau thuan voi chinh no.
            Service.gI().sendThongBao(player, "Chưa đủ điều kiện tới " + d.nhan()
                    + " (hay gặp: chưa làm tới nhiệm vụ mở khu vực). Vẫn ở "
                    + player.zone.map.mapName + ".");
            return;
        }

        int x = d.x;
        int y = d.y;
        if (y < 0) {
            if (x < 0) {
                x = Math.max(100, map.mapWidth / 2);
            }
            y = map.yPhysicInTop(x, 100);
            if (!coDat(map, x, y)) {
                // Giua ban do co the la troi khong. Tha xuong do la roi thang
                // xuong vuc hoac ket lo lung — dung hon la khong di.
                int khac = timCotCoDat(map);
                if (khac < 0) {
                    Service.gI().sendThongBao(player, "Không tìm được chỗ đứng ở "
                            + d.nhan() + ". Vẫn ở " + player.zone.map.mapName
                            + ".");
                    return;
                }
                x = khac;
                y = map.yPhysicInTop(x, 100);
            }
        }

        String tenMapTruoc = player.zone.map.mapName;
        ChangeMapService.gI().changeMap(player, dich, x, y);

        int mapSau = player.zone != null && player.zone.map != null
                ? player.zone.map.mapId : -1;
        if (mapSau == mapId) {
            Service.gI().sendThongBao(player, "Đã tới " + d.nhan() + ".");
        } else {
            Service.gI().sendThongBao(player, "Không tới được " + d.nhan()
                    + ". Vẫn ở " + tenMapTruoc + ".");
        }
    }

    /** Toạ độ này có mặt đất để đứng không. */
    private boolean coDat(nro.entity.map.Map m, int x, int y) {
        return x >= 0 && y > 0 && y <= m.mapHeight;
    }

    /**
     * Dò một hoành độ thực sự có mặt đất.
     *
     * <p>Quét đều từ trái sang phải chứ không bốc ngẫu nhiên: cùng một bản đồ
     * thì lần nào cũng ra cùng một chỗ, nên lỗi (nếu có) lặp lại được để mà
     * xem, thay vì thỉnh thoảng mới dính một lần.</p>
     *
     * @return hoành độ, hoặc {@code -1} nếu cả bản đồ không có chỗ nào đứng được
     */
    private int timCotCoDat(nro.entity.map.Map m) {
        int trai = 100;
        int phai = m.mapWidth - 100;
        if (phai <= trai) {
            return m.yPhysicInTop(100, 100) > 0 ? 100 : -1;
        }
        for (int x = trai; x <= phai; x += 24) {
            if (coDat(m, x, m.yPhysicInTop(x, 100))) {
                return x;
            }
        }
        return -1;
    }

    /** Quên nhóm đang mở — gọi khi người chơi thoát, để bảng không phình mãi. */
    public void quen(Player player) {
        if (player != null) {
            NHOM_DANG_XEM.remove(player.id);
        }
    }
}
