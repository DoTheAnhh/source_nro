package nro.service.chat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.server.Client;
import nro.service.Service;

/**
 * Chat thế giới: một người gõ, cả máy chủ đọc được.
 *
 * <p><b>Vì sao nằm ở đây chứ không ở client.</b> Client chỉ gửi lên một chuỗi
 * và vẽ lại những gì nhận được. Toàn bộ phần quyết định — dài bao nhiêu thì
 * chấp nhận, bao lâu mới được gửi tiếp, gửi cho những ai — nằm trong lớp này.
 * Để client tự giới hạn thì sửa client là bỏ qua được hết.</p>
 *
 * <p><b>Khác với "loa thế giới" sẵn có.</b> Biểu mẫu {@code LOA_TO_THE_GIOI}
 * tiêu một vật phẩm loa mỗi lần phát và hiện ra dạng thông báo chạy. Kênh này
 * miễn phí và chỉ đi vào khung chat, nên phải có nhịp chờ — nếu không nó thành
 * chỗ spam cho cả máy chủ.</p>
 */
public class ChatTheGioiService {

    private static ChatTheGioiService instance;

    public static ChatTheGioiService gI() {
        if (instance == null) {
            instance = new ChatTheGioiService();
        }
        return instance;
    }

    /** Mã lệnh dùng cho cả hai chiều: client gửi lên, máy chủ phát xuống. */
    public static final int MA_LENH = 59;

    /** Số ký tự tối đa một dòng. */
    private static final int DAI_TOI_DA = 200;

    /** Nhịp chờ giữa hai lần gửi của cùng một người, tính bằng mili giây. */
    private static final long NHIP_CHO = 10_000L;

    /**
     * Lần gửi gần nhất của từng người chơi.
     *
     * <p>Dùng {@link ConcurrentHashMap} vì các luồng xử lý gói tin chạy song
     * song: hai gói của hai người tới cùng lúc là chuyện bình thường.</p>
     */
    private final Map<Long, Long> lanCuoi = new ConcurrentHashMap<>();

    /**
     * Nhận một dòng chat thế giới và phát cho toàn máy chủ.
     *
     * @param nguoiGui người gửi; bỏ qua nếu {@code null}
     * @param noiDung  nội dung thô do client gửi lên
     */
    public void nhanVaPhat(Player nguoiGui, String noiDung) {
        if (nguoiGui == null || noiDung == null) {
            return;
        }
        String chu = noiDung.trim();
        if (chu.isEmpty()) {
            return;
        }
        if (chu.length() > DAI_TOI_DA) {
            chu = chu.substring(0, DAI_TOI_DA);
        }

        // Không có nhịp chờ: chủ máy chủ chọn cho chat liên tục và miễn phí.
        // Vẫn giữ trần độ dài ở trên — một dòng dài vô hạn làm hỏng bố cục
        // khung chat của tất cả mọi người, đó là chuyện khác với spam.
        phat(nguoiGui.name, chu);
        Logger.info("CHAT_TG", nguoiGui.name + ": " + chu);
    }

    /**
     * Gửi một dòng cho mọi người đang trong máy chủ.
     *
     * <p>Duyệt trên <b>bản chụp</b> danh sách người chơi chứ không trên danh
     * sách gốc: người vào/ra liên tục, duyệt thẳng danh sách gốc thì gặp
     * {@code ConcurrentModificationException} giữa chừng và nửa số người không
     * nhận được tin.</p>
     */
    private void phat(String ten, String chu) {
        List<Player> ds = Client.gI().getPlayersSnapshot();
        if (ds == null) {
            return;
        }
        for (Player pl : ds) {
            if (pl == null || pl.isBoss || !pl.isPl()) {
                continue;
            }
            Message msg = null;
            try {
                msg = new Message(MA_LENH);
                msg.writer().writeUTF(ten);
                msg.writer().writeUTF(chu);
                nro.core.log.Logger.info("CHAT_TG", "gui cho " + pl.name
                        + " | so byte than goi = " + msg.getData().length
                        + " | ten=" + ten + " chu=" + chu);
                pl.sendMessage(msg);
            } catch (Exception boQua) {
                // Một người mất kết nối giữa chừng không được làm hỏng lượt
                // gửi của những người còn lại.
            }
            // KHÔNG gọi msg.cleanup() ở đây.
            //
            // Sender.sendMessage chỉ XẾP gói vào hàng đợi rồi trả về ngay;
            // luồng gửi mới nối tiếp mà chuyển nó thành byte. Gọi cleanup()
            // ngay sau đó là xoá sạch thân gói TRƯỚC khi nó kịp được ghi ra,
            // nên bên nhận thấy mã lệnh 48 với thân RỖNG và ném
            // "loi doc sbyte eof" — lỗi đó chạy ngược lên luồng đọc và giết
            // cả phiên, tức là người chơi bị văng khỏi game ngay khi chat.
        }
    }
}
