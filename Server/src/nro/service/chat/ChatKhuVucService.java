package nro.service.chat;

import java.util.List;

import nro.core.log.Logger;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.net.io.Message;

/**
 * Chat theo <b>bản đồ</b> và theo <b>khu</b>.
 *
 * <p>Hai kênh hẹp hơn chat thế giới: một dòng chỉ tới những người đang đứng
 * cùng bản đồ, hoặc hẹp hơn nữa là cùng khu. Người chơi cần rủ nhau đánh boss ở
 * đúng chỗ mình đứng thì không phải hét cho cả máy chủ nghe.</p>
 *
 * <p><b>Vì sao nằm ở máy chủ.</b> Client chỉ gửi lên một chuỗi và vẽ lại những
 * gì nhận được. Việc quyết định <i>ai nhận được</i> phải nằm ở đây — để client
 * tự chọn thì sửa client là gửi cho ai cũng được.</p>
 *
 * <p><b>Không lọc chữ.</b> Chủ máy chủ chọn tạm bỏ chặn ký tự đặc biệt và từ
 * tục tĩu. Trần độ dài thì vẫn giữ: một dòng dài vô hạn làm vỡ bố cục khung
 * chat của tất cả mọi người, đó là chuyện khác với lọc nội dung.</p>
 */
public class ChatKhuVucService {

    private static ChatKhuVucService instance;

    public static ChatKhuVucService gI() {
        if (instance == null) {
            instance = new ChatKhuVucService();
        }
        return instance;
    }

    /**
     * Mã lệnh chat bản đồ, dùng cho cả hai chiều.
     *
     * <p>Đã dò cả ba nơi trước khi chọn — {@code Controller.java} của máy chủ,
     * {@code Controller.cs} và {@code Controller2.cs} của client — vì một mã đã
     * có nghĩa ở bất kỳ nơi nào trong ba nơi đó là client hiểu sai gói và ngắt
     * phiên. Chuyện này đã xảy ra một lần với mã 48.</p>
     */
    public static final int MA_LENH_MAP = 60;

    /** Mã lệnh chat khu. Xem ghi chú ở {@link #MA_LENH_MAP}. */
    public static final int MA_LENH_KHU = 67;

    /** Số ký tự tối đa một dòng. */
    private static final int DAI_TOI_DA = 200;

    /** Nhận một dòng chat bản đồ và phát cho mọi người trong cùng bản đồ. */
    public void nhanVaPhatMap(Player nguoiGui, String noiDung) {
        String chu = chuanHoa(noiDung);
        if (nguoiGui == null || chu == null || nguoiGui.zone == null
                || nguoiGui.zone.map == null) {
            return;
        }
        List<Zone> khu = nguoiGui.zone.map.zones;
        if (khu == null) {
            return;
        }
        int dem = 0;
        for (Zone z : khu) {
            if (z == null) {
                continue;
            }
            dem += phatTrongKhu(z, MA_LENH_MAP, nguoiGui.name, chu);
        }
        Logger.info("CHAT_MAP", nguoiGui.name + " @ map "
                + nguoiGui.zone.map.mapId + " -> " + dem + " nguoi: " + chu);
    }

    /** Nhận một dòng chat khu và phát cho mọi người trong cùng khu. */
    public void nhanVaPhatKhu(Player nguoiGui, String noiDung) {
        String chu = chuanHoa(noiDung);
        if (nguoiGui == null || chu == null || nguoiGui.zone == null) {
            return;
        }
        int dem = phatTrongKhu(nguoiGui.zone, MA_LENH_KHU, nguoiGui.name, chu);
        Logger.info("CHAT_KHU", nguoiGui.name + " @ khu "
                + nguoiGui.zone.zoneId + " -> " + dem + " nguoi: " + chu);
    }

    /** Cắt khoảng trắng thừa và chặn trần độ dài; rỗng thì trả {@code null}. */
    private String chuanHoa(String noiDung) {
        if (noiDung == null) {
            return null;
        }
        String chu = noiDung.trim();
        if (chu.isEmpty()) {
            return null;
        }
        return chu.length() > DAI_TOI_DA ? chu.substring(0, DAI_TOI_DA) : chu;
    }

    /**
     * Gửi một dòng cho mọi người chơi trong một khu.
     *
     * <p>Mỗi người một gói riêng: {@code Sender} biến gói thành byte ở luồng
     * gửi của chính người đó, dùng chung một đối tượng gói cho nhiều người là
     * hai luồng cùng đọc một bộ đệm.</p>
     *
     * <p><b>Không gọi {@code msg.cleanup()}.</b> {@code sendMessage} chỉ xếp gói
     * vào hàng đợi rồi trả về ngay; luồng gửi mới chuyển nó thành byte. Gọi
     * {@code cleanup()} ngay sau đó là xoá sạch thân gói trước khi nó kịp được
     * ghi ra, bên nhận thấy mã lệnh với thân rỗng và ném lỗi đọc — lỗi đó chạy
     * ngược lên luồng đọc và giết cả phiên, tức người chơi văng khỏi game ngay
     * khi chat.</p>
     *
     * @return số người thực sự được gửi
     */
    private int phatTrongKhu(Zone z, int maLenh, String ten, String chu) {
        List<Player> ds = z.getHumanoids();
        if (ds == null) {
            return 0;
        }
        int dem = 0;
        for (Player pl : ds) {
            if (pl == null || pl.isBoss || !pl.isPl()) {
                continue;
            }
            try {
                Message msg = new Message(maLenh);
                msg.writer().writeUTF(ten);
                msg.writer().writeUTF(chu);
                pl.sendMessage(msg);
                dem++;
            } catch (Exception boQua) {
                // Một người mất kết nối giữa chừng không được làm hỏng lượt
                // gửi của những người còn lại.
            }
        }
        return dem;
    }
}
