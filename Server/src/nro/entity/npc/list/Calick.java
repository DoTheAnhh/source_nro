package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.service.fun.ChangeMapService;
import nro.service.MapService;
import nro.service.NpcService;
import nro.service.Service;
import nro.service.TaskService;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.core.consts.ConstTask;
import nro.entity.map.Map;
import nro.entity.npc.Npc;
import nro.entity.player.Player;

public class Calick extends Npc {

    private final byte COUNT_CHANGE = 2;
    private int count;

    public Calick(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private void changeMap() {
        if (this.mapId != 102) {
            count++;
            if (this.count >= COUNT_CHANGE) {
                count = 0;
                this.map.npcs.remove(this);
                Map mapcl = MapService.gI().getMapForCalich();
                this.mapId = mapcl.mapId;
                this.cx = Util.nextInt(100, mapcl.mapWidth - 100);
                this.cy = mapcl.yPhysicInTop(this.cx, 0);
                this.map = mapcl;
                this.map.npcs.add(this);
            }
        }
    }

    @Override
    public void openBaseMenu(Player player) {
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
        if (TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
            Service.gI().hideWaitDialog(player);
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (this.mapId != player.zone.map.mapId) {
            Service.gI().sendThongBao(player, "Calích đã rời khỏi map!");
            Service.gI().hideWaitDialog(player);
            return;
        }

        String[] nut = cacNut(player);
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Chào chú, cháu có thể giúp gì?", nut);
    }

    /** Nhãn của nút "Tiến tới địa ngục". Dùng chung cho lúc vẽ và lúc bấm. */
    private static final String NUT_DIA_NGUC = "Tiến tới\nĐịa ngục";

    /**
     * Bản đồ Địa ngục tầng 1, và toạ độ rơi xuống khi tới nơi.
     *
     * <p>Lấy từ dòng đã có sẵn trong {@code BaHatMit} (đang bị chú thích) chứ
     * không tự đoán — đó là điểm vào đã biết là đứng được, không rơi vào vách
     * hay lơ lửng giữa trời.</p>
     */
    private static final int MAP_DIA_NGUC_1 = 167;
    private static final int Y_DIA_NGUC_1 = 408;

    /**
     * Danh sách nút hiện cho người chơi này.
     *
     * <p>Dựng ở một chỗ duy nhất rồi {@link #confirmMenu} tra <b>theo nhãn</b>
     * chứ không theo số thứ tự. Thêm một nút vào giữa là mọi nút phía sau đổi
     * nghĩa — bấm "Từ chối" lại thành đi tương lai — mà kiểu lỗi đó không báo
     * gì cả, chỉ lặng lẽ làm sai việc.</p>
     */
    private String[] cacNut(Player player) {
        if (this.mapId == 102) {
            return new String[]{"Kể\nChuyện", "Quay về\nQuá khứ"};
        }
        java.util.List<String> ds = new java.util.ArrayList<>();
        ds.add("Kể\nChuyện");
        ds.add("Đi đến\nTương lai");
        if (moDuocDiaNguc(player)) {
            ds.add(NUT_DIA_NGUC);
        }
        ds.add("Từ chối");
        return ds.toArray(new String[0]);
    }

    /**
     * Đã tới bước "Đánh bại tên Vua địa ngục" của nhiệm vụ 31 hay chưa.
     *
     * <p>Dùng dấu lớn-hơn-hoặc-bằng nên ai đã đi qua bước này vẫn quay lại địa
     * ngục được. Chặn đúng bằng thì người chơi làm xong bước là mất luôn đường
     * vào, trong khi mấy bước sau vẫn còn việc ở dưới đó.</p>
     */
    private boolean moDuocDiaNguc(Player player) {
        return TaskService.gI().getIdTask(player) >= ConstTask.TASK_31_3;
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!player.iDMark.isBaseMenu()) {
            return;
        }
        // Tra lai dung danh sach nut da ve cho NGUOI NAY. Npc la doi tuong dung
        // chung cho ca may chu nen khong duoc nho lua chon vao truong cua no —
        // hai nguoi bam cung luc se doc nham cua nhau.
        String[] nut = cacNut(player);
        if (select < 0 || select >= nut.length) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        String chon = nut[select];

        if ("Kể\nChuyện".equals(chon)) {
            NpcService.gI().createTutorial(player, tempId, this.avartar,
                    ConstNpc.CALICK_KE_CHUYEN);
        } else if ("Quay về\nQuá khứ".equals(chon)) {
            ChangeMapService.gI().goToQuaKhu(player);
        } else if ("Đi đến\nTương lai".equals(chon)) {
            changeMap();
            if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_20_0) {
                ChangeMapService.gI().goToTuongLai(player);
            }
        } else if (NUT_DIA_NGUC.equals(chon)) {
            diXuongDiaNguc(player);
        } else {
            // "Tu choi" va moi thu khac: dong thoai, khong lam gi.
            Service.gI().hideWaitDialog(player);
        }
    }

    /**
     * Đưa người chơi xuống Địa ngục tầng 1.
     *
     * <p>Kiểm tra lại điều kiện ngay trước khi đi, dù nút chỉ hiện khi đủ điều
     * kiện: gói tin chọn menu do client gửi lên, sửa được. Tin vào việc "nút
     * không hiện thì không bấm được" là tin vào client.</p>
     */
    private void diXuongDiaNguc(Player player) {
        if (!moDuocDiaNguc(player)) {
            Service.gI().sendThongBao(player,
                    "Chưa tới lúc — hãy nhận nhiệm vụ đánh bại Vua địa ngục đã.");
            Service.gI().hideWaitDialog(player);
            return;
        }
        int x = 200 + Util.nextInt(-100, 100);
        ChangeMapService.gI().changeMapNonSpaceship(player, MAP_DIA_NGUC_1, x,
                Y_DIA_NGUC_1);
    }
}
