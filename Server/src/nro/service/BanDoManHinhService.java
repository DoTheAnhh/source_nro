package nro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.repository.dao.MapNhanhDAO;

/**
 * Gửi danh sách <b>bản đồ nhanh</b> xuống client, cho cột phải của thẻ
 * "Nhiệm vụ" trong màn nhân vật kiểu mới.
 *
 * <h2>Vì sao phải có gói riêng</h2>
 *
 * <p>Danh sách điểm đến nằm ở bảng {@code map_nhanh} trong cơ sở dữ liệu —
 * client không biết trước có những bản đồ nào. Trước đây người chơi phải gõ
 * lệnh {@code bando}, rồi máy chủ vẽ <b>menu hai tầng</b> của chính nó: chọn
 * nhóm, rồi chọn bản đồ. Menu đó là màn riêng của máy chủ, không nhét vào một
 * cột của bảng khác được.</p>
 *
 * <p>Gói này trả cả cây nhóm–điểm trong <b>một lần</b>, để client vẽ lưới nhóm
 * và danh sách bản đồ cạnh nhau, chọn nhóm không phải hỏi lại máy chủ.</p>
 *
 * <h2>Vì sao mã gói là 120</h2>
 *
 * <p>120 còn trống ở <b>cả ba</b> chỗ định tuyến: {@code Controller} và
 * {@code Controller2} phía client, và {@code Controller} phía máy chủ. Phải soát
 * đủ ba: {@code Controller2.readMessage} chạy <b>trước</b> switch chính của
 * client trên cùng một luồng đọc, nên một mã đã có chủ ở đó sẽ ngốn mất gói và
 * màn hình đứng im không báo lỗi gì — đúng thứ đã xảy ra với mã -110 của màn
 * Boss.</p>
 */
public class BanDoManHinhService {

    private static BanDoManHinhService instance;

    public static BanDoManHinhService gI() {
        if (instance == null) {
            instance = new BanDoManHinhService();
        }
        return instance;
    }

    /** Mã gói màn bản đồ nhanh. Xem phần đầu lớp về việc chọn số này. */
    public static final byte OPCODE = 120;

    /**
     * Số nhóm và số điểm mỗi nhóm gửi nhiều nhất.
     *
     * <p>Độ dài một gói đóng khung bằng {@code short} (32 KB). Bảng
     * {@code map_nhanh} do người quản trị nhập nên về lý thuyết dài bao nhiêu
     * cũng được; chặn ở đây để một lần nhập tay quá tay không làm đứt kết nối
     * của mọi người chơi.</p>
     */
    private static final int TOI_DA_NHOM = 40;

    private static final int TOI_DA_DIEM = 60;

    /**
     * Gửi cả cây nhóm–điểm.
     *
     * <h3>Khuôn gói</h3>
     * <pre>
     * byte  0                (việc: danh sách)
     * byte  soNhom
     *   UTF   tenNhom
     *   byte  soDiem
     *     int   idDiem       (khoá chính trong map_nhanh)
     *     UTF   tenDiem
     *     short mapId        (số hiện trong ngoặc vuông sau tên)
     * </pre>
     */
    public void guiDanhSach(Player pl) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            Map<String, List<MapNhanhDAO.Diem>> cay = MapNhanhDAO.theoNhom();
            List<String> tenNhom = new ArrayList<>(cay.keySet());
            if (tenNhom.size() > TOI_DA_NHOM) {
                tenNhom = tenNhom.subList(0, TOI_DA_NHOM);
            }

            msg = new Message(OPCODE);
            msg.writer().writeByte(0);
            msg.writer().writeByte(tenNhom.size());
            for (String n : tenNhom) {
                List<MapNhanhDAO.Diem> ds = cay.get(n);
                if (ds == null) {
                    ds = new ArrayList<>();
                }
                int so = Math.min(ds.size(), TOI_DA_DIEM);
                msg.writer().writeUTF(n);
                msg.writer().writeByte(so);
                for (int i = 0; i < so; i++) {
                    MapNhanhDAO.Diem d = ds.get(i);
                    msg.writer().writeInt(d.id);
                    msg.writer().writeUTF(d.ten == null ? "" : d.ten);
                    msg.writer().writeShort(d.mapId);
                }
            }
            msg.writer().flush();
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(BanDoManHinhService.class, ex,
                    "Lỗi gửi danh sách bản đồ nhanh");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Nhận gói từ client: 0 xin danh sách, 1 kèm id điểm để dịch chuyển.
     */
    public void nhanGoi(Player pl, Message msg) {
        if (pl == null || msg == null) {
            return;
        }
        try {
            byte viec = msg.reader().readByte();
            if (viec == 0) {
                guiDanhSach(pl);
            } else {
                dichChuyen(pl, msg.reader().readInt());
            }
        } catch (Exception ex) {
            Logger.logException(BanDoManHinhService.class, ex,
                    "Lỗi nhận gói bản đồ nhanh");
        }
    }

    /**
     * Dịch chuyển tới điểm mang id này.
     *
     * <p>Tra lại điểm theo <b>id</b> chứ không nhận thẳng toạ độ từ client: nhận
     * toạ độ là để client tự chọn nơi muốn tới, kể cả nơi người quản trị đã tắt
     * hoặc chưa từng khai.</p>
     *
     * <p>Việc thả người xuống bản đồ giao lại cho {@link MapNhanhService} — nó
     * đã lo phần khó: toạ độ âm thì tự tính giữa bản đồ và mặt đất ngay dưới,
     * còn không đi được thì đứng nguyên tại chỗ.</p>
     */
    private void dichChuyen(Player pl, int idDiem) {
        for (MapNhanhDAO.Diem d : MapNhanhDAO.danhSach()) {
            if (d != null && d.bat && d.id == idDiem) {
                MapNhanhService.gI().dichChuyen(pl, d);
                return;
            }
        }
    }
}
