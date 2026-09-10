package nro.repository.dao;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.task.SubTaskMain;
import nro.entity.task.TaskMain;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;
import nro.server.Manager;

/**
 * Kho dữ liệu <b>nhiệm vụ chính tuyến</b> — hai bảng {@code task_main_template}
 * và {@code task_sub_template}.
 *
 * <h2>Một nhiệm vụ gồm những gì</h2>
 *
 * <p>Mỗi <b>nhiệm vụ chính</b> ({@code task_main_template}) có một cái tên và
 * một đoạn mô tả dài hiện ở bảng nhiệm vụ trong game. Dưới nó là một dãy
 * <b>bước</b> ({@code task_sub_template}) làm lần lượt từ trên xuống; xong bước
 * cuối là xong nhiệm vụ và người chơi nhảy sang nhiệm vụ kế tiếp <b>theo id</b>.
 * Vì thế id nhiệm vụ chính vừa là khoá vừa là <b>thứ tự chơi</b>.</p>
 *
 * <p>Mỗi bước có bốn con số:</p>
 *
 * <ul>
 *   <li><b>Số lượng cần</b> ({@code max_count}) — đánh bao nhiêu con, nhặt bao
 *       nhiêu món thì xong bước. Đây là con số hay phải chỉnh nhất. Để
 *       {@code -1} nghĩa là "không đếm": bước xong bằng một sự kiện khác (nói
 *       chuyện với NPC, tới nơi, hạ một con boss cụ thể).</li>
 *   <li><b>NPC</b> ({@code npc_id}) — mũi tên chỉ dẫn trỏ vào NPC nào.
 *       {@code -1} là không trỏ ai. Số <b>âm nhỏ hơn -1</b> là mã đặc biệt, được
 *       {@code TaskService.transformNpcId} đổi thành NPC thật <i>tuỳ hành
 *       tinh</i> của người chơi — ví dụ {@code -2} là ông thầy ở làng.</li>
 *   <li><b>Bản đồ</b> ({@code map}) — mũi tên chỉ dẫn trỏ tới bản đồ nào.
 *       {@code -1} là không trỏ. Âm nhỏ hơn -1 cũng là mã đặc biệt, đổi theo
 *       hành tinh bởi {@code transformMapId}.</li>
 *   <li><b>Thứ tự</b> ({@code idmain}) — khoá tự tăng, và cũng là thứ tự các
 *       bước trong nhiệm vụ.</li>
 * </ul>
 *
 * <p>Trong tên và mô tả, <b>{@code %1} đến {@code %14}</b> là chỗ trống được
 * thay bằng tên riêng theo hành tinh (tên làng, tên ông thầy, tên loại quái…),
 * nên cùng một dòng chữ đọc đúng ở cả ba hành tinh.</p>
 *
 * <h2>Sửa được cái gì, và không sửa được cái gì</h2>
 *
 * <p><b>Sửa được:</b> tên, mô tả, câu thông báo, số lượng cần, NPC và bản đồ chỉ
 * dẫn. Đổi số lượng cần là có hiệu lực thật — chỗ đếm trong {@code TaskService}
 * đọc thẳng {@code maxCount}.</p>
 *
 * <p><b>Không sửa được:</b> <i>việc gì làm bước đó tiến lên</i>. Cái đó viết
 * cứng trong {@code TaskService} theo đúng cặp số (nhiệm vụ, bước) — hạ con quái
 * nào, nhặt món gì, ở bản đồ nào. Thêm một bước mới vào giữa dãy sẽ <b>dịch số
 * thứ tự của mọi bước sau nó</b> và làm lệch toàn bộ phần viết cứng ấy: nhiệm vụ
 * kẹt lại ở một bước không có gì đẩy nó đi. Nên chỉ thêm bước khi có người viết
 * mã cho nó.</p>
 */
public final class NhiemVuDAO {

    private NhiemVuDAO() {
    }

    /** Một bước của nhiệm vụ, kèm khoá thật trong bảng. */
    public static final class Buoc {

        public int idmain;
        public int nhiemVuId;
        public String ten = "";
        public int soLuong = -1;
        public String thongBao = "";
        public int npcId = -1;
        public int mapId = -1;
    }

    /** Một nhiệm vụ chính, kèm các bước của nó. */
    public static final class NhiemVu {

        public int id;
        public String ten = "";
        public String moTa = "";
        public final List<Buoc> buoc = new ArrayList<>();
    }

    /** Đọc cả hai bảng, ghép lại, xếp theo id nhiệm vụ rồi tới thứ tự bước. */
    public static List<NhiemVu> danhSach() {
        List<NhiemVu> ra = new ArrayList<>();
        java.util.Map<Integer, NhiemVu> theoId = new java.util.LinkedHashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT id, NAME, detail FROM task_main_template ORDER BY id");
            while (rs.next()) {
                NhiemVu nv = new NhiemVu();
                nv.id = rs.getInt("id");
                nv.ten = rs.getString("NAME");
                nv.moTa = rs.getString("detail");
                theoId.put(nv.id, nv);
                ra.add(nv);
            }
        } catch (Exception ex) {
            Logger.logException(NhiemVuDAO.class, ex, "Lỗi đọc task_main_template");
            return ra;
        } finally {
            dong(rs);
        }
        rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT idmain, task_main_id, NAME,"
                    + " max_count, notify, npc_id, map FROM task_sub_template"
                    + " ORDER BY task_main_id, idmain");
            while (rs.next()) {
                Buoc b = new Buoc();
                b.idmain = rs.getInt("idmain");
                b.nhiemVuId = rs.getInt("task_main_id");
                b.ten = rs.getString("NAME");
                b.soLuong = rs.getInt("max_count");
                b.thongBao = rs.getString("notify");
                b.npcId = rs.getInt("npc_id");
                b.mapId = rs.getInt("map");
                NhiemVu nv = theoId.get(b.nhiemVuId);
                if (nv != null) {
                    nv.buoc.add(b);
                }
            }
        } catch (Exception ex) {
            Logger.logException(NhiemVuDAO.class, ex, "Lỗi đọc task_sub_template");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** Lưu phần đầu của một nhiệm vụ (tên và mô tả). */
    public static String luuNhiemVu(int id, String ten, String moTa) {
        if (id < 0) {
            return "Id nhiệm vụ phải từ 0 trở lên.";
        }
        if (ten == null || ten.trim().isEmpty()) {
            return "Nhiệm vụ phải có tên.";
        }
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO task_main_template (id, NAME, detail) VALUES (?,?,?)"
                    + " ON DUPLICATE KEY UPDATE NAME = VALUES(NAME),"
                    + " detail = VALUES(detail)",
                    id, ten.trim(), moTa == null ? "" : moTa);
            return null;
        } catch (Exception ex) {
            Logger.logException(NhiemVuDAO.class, ex, "Lỗi lưu nhiệm vụ " + id);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Lưu một bước đã có.
     *
     * <p>Chỉ ghi đè theo {@code idmain}, <b>không</b> thêm dòng mới: thêm bước là
     * việc riêng của {@link #themBuoc}, vì nó kéo theo hệ quả về thứ tự.</p>
     */
    public static String luuBuoc(Buoc b) {
        if (b == null) {
            return "Không có dữ liệu.";
        }
        if (b.ten == null || b.ten.trim().isEmpty()) {
            return "Bước phải có tên.";
        }
        try {
            int n = ConnectDB.executeUpdate(
                    "UPDATE task_sub_template SET NAME = ?, max_count = ?,"
                    + " notify = ?, npc_id = ?, map = ? WHERE idmain = ?",
                    b.ten.trim(), b.soLuong,
                    b.thongBao == null ? "" : b.thongBao,
                    b.npcId, b.mapId, b.idmain);
            return n == 0 ? "Không có bước nào mang số thứ tự " + b.idmain + "." : null;
        } catch (Exception ex) {
            Logger.logException(NhiemVuDAO.class, ex, "Lỗi lưu bước " + b.idmain);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }


    /**
     * Đổi chỗ hai bước cho nhau.
     *
     * <h2>Đổi NỘI DUNG chứ không đổi số thứ tự</h2>
     *
     * <p>Thứ tự các bước là thứ tự {@code idmain}, mà {@code idmain} là khoá
     * chính tự tăng — đổi nó là đụng vào khoá, và mọi thứ trỏ tới nó lệch theo.
     * Nên ở đây hoán <b>nội dung</b> của hai dòng: tên, số lượng cần, thông báo,
     * NPC, bản đồ. Nhìn từ ngoài đúng là hai bước đã đổi chỗ, còn khoá thì đứng
     * yên.</p>
     *
     * <p>Nhắc lại điều quan trọng: việc gì làm một bước tiến lên được viết cứng
     * theo <b>số thứ tự bước</b>. Đổi chỗ hai bước là đổi luôn việc phải làm ở
     * mỗi vị trí — dùng khi đang dựng một nhiệm vụ mới, đừng dùng trên nhiệm vụ
     * người chơi đang chạy dở.</p>
     */
    public static String doiCho(int idmainA, int idmainB) {
        if (idmainA == idmainB) {
            return null;
        }
        Buoc a = null;
        Buoc b = null;
        for (NhiemVu nv : danhSach()) {
            for (Buoc x : nv.buoc) {
                if (x.idmain == idmainA) {
                    a = x;
                }
                if (x.idmain == idmainB) {
                    b = x;
                }
            }
        }
        if (a == null || b == null) {
            return "Không tìm thấy một trong hai bước.";
        }
        int giuId = a.idmain;
        a.idmain = b.idmain;
        b.idmain = giuId;
        String loi = luuBuoc(a);
        if (loi != null) {
            return loi;
        }
        return luuBuoc(b);
    }

    /** Thêm một bước vào <b>cuối</b> một nhiệm vụ. */
    public static String themBuoc(int nhiemVuId, String ten) {
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO task_sub_template (task_main_id, NAME, max_count,"
                    + " notify, npc_id, map) VALUES (?,?,?,?,?,?)",
                    nhiemVuId, ten == null || ten.trim().isEmpty()
                            ? "Bước mới" : ten.trim(), -1, "", -1, -1);
            return null;
        } catch (Exception ex) {
            Logger.logException(NhiemVuDAO.class, ex, "Lỗi thêm bước");
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    public static String xoaBuoc(int idmain) {
        try {
            int n = ConnectDB.executeUpdate(
                    "DELETE FROM task_sub_template WHERE idmain = ?", idmain);
            return n == 0 ? "Không có bước nào mang số thứ tự " + idmain + "." : null;
        } catch (Exception ex) {
            Logger.logException(NhiemVuDAO.class, ex, "Lỗi xoá bước " + idmain);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Đọc lại cả hai bảng và <b>thay danh sách nhiệm vụ đang chạy</b>.
     *
     * <h2>Người đang chơi thì sao</h2>
     *
     * <p>Người chơi đang online giữ tham chiếu tới đối tượng nhiệm vụ <i>cũ</i>,
     * nên họ vẫn thấy con số cũ cho tới khi đăng nhập lại. Người vào sau đó —
     * và mọi người sau khi đăng nhập lại — nhận bản mới. Không xoá tiến độ của
     * ai: tiến độ nằm ở phía người chơi, không nằm ở bảng mẫu này.</p>
     *
     * <p>Danh sách được thay <b>tại chỗ</b> (clear rồi đổ lại) chứ không gán
     * danh sách mới, vì {@code Manager.TASKS} là {@code final} và nhiều nơi đã
     * giữ tham chiếu tới chính nó.</p>
     */
    public static synchronized String napLaiVaoBoNho() {
        List<NhiemVu> ds = danhSach();
        if (ds.isEmpty()) {
            return "Bảng nhiệm vụ rỗng — giữ nguyên bản đang chạy.";
        }
        List<TaskMain> moi = new ArrayList<>();
        for (NhiemVu nv : ds) {
            TaskMain t = new TaskMain();
            t.id = nv.id;
            t.name = nv.ten;
            t.detail = nv.moTa;
            for (Buoc b : nv.buoc) {
                SubTaskMain s = new SubTaskMain();
                s.name = b.ten;
                s.maxCount = (short) b.soLuong;
                s.notify = b.thongBao;
                s.npcId = (byte) b.npcId;
                s.mapId = (short) b.mapId;
                t.subTasks.add(s);
            }
            moi.add(t);
        }
        Manager.TASKS.clear();
        Manager.TASKS.addAll(moi);
        capNhatNguoiDangOnline();
        Logger.success("CONFIG", "Đã nạp lại " + moi.size() + " nhiệm vụ chính");
        return null;
    }

    /**
     * Dựng lại mạch nhiệm vụ cho <b>người đang online</b>, giữ nguyên tiến độ.
     *
     * <h2>Vì sao cần</h2>
     *
     * <p>{@code Manager.TASKS} chỉ là bảng mẫu. Mỗi người chơi giữ một <b>bản
     * riêng</b> dựng lúc đăng nhập, và bản ấy không đổi khi bảng mẫu đổi. Nên
     * sửa thứ tự các bước trên panel xong, người đang online vẫn chạy theo thứ
     * tự cũ — panel một đằng, trong game một nẻo, và không có gì nói vì sao.</p>
     *
     * <p>Ở đây dựng lại bản riêng ấy từ mẫu mới, rồi <b>chép lại chỉ số bước và
     * số đếm</b> — người chơi đứng nguyên chỗ họ đang đứng, chỉ nội dung các
     * bước là mới. Chỉ số được kẹp vào trong dãy phòng khi mẫu mới ít bước
     * hơn.</p>
     */
    private static void capNhatNguoiDangOnline() {
        for (nro.entity.player.Player pl : nro.server.Client.gI().getPlayersSnapshot()) {
            try {
                if (pl == null || !pl.isPl() || pl.playerTask == null
                        || pl.playerTask.taskMain == null) {
                    continue;
                }
                TaskMain cu = pl.playerTask.taskMain;
                TaskMain moiCuaPl = nro.service.TaskService.gI()
                        .getTaskMainById(pl, cu.id);
                if (moiCuaPl == null || moiCuaPl.subTasks == null
                        || moiCuaPl.subTasks.isEmpty()) {
                    continue;
                }
                int buoc = cu.index;
                if (buoc < 0) {
                    buoc = 0;
                } else if (buoc >= moiCuaPl.subTasks.size()) {
                    buoc = moiCuaPl.subTasks.size() - 1;
                }
                short dem = 0;
                if (cu.subTasks != null && cu.index >= 0
                        && cu.index < cu.subTasks.size()) {
                    dem = cu.subTasks.get(cu.index).count;
                }
                moiCuaPl.index = buoc;
                moiCuaPl.lastTime = cu.lastTime;
                moiCuaPl.subTasks.get(buoc).count = dem;
                pl.playerTask.taskMain = moiCuaPl;
                nro.service.TaskService.gI().sendTaskMain(pl);
            } catch (Exception boQua) {
                // Mot nguoi hong khong duoc chan nhung nguoi con lai.
            }
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
