package nro.service;

import java.util.List;
import nro.core.log.Logger;
import nro.entity.clan.Clan;
import nro.entity.map.Zone;
import nro.entity.player.Player;
import nro.net.io.Message;

/**
 * Tiếp âm thoại giữa người chơi — máy chủ chỉ chuyển gói, không đụng vào tiếng.
 *
 * <h2>Ba kênh</h2>
 *
 * <ul>
 *   <li>{@link #KENH_KHU} — cùng khu, cùng bản đồ</li>
 *   <li>{@link #KENH_MAP} — cùng bản đồ, mọi khu</li>
 *   <li>{@link #KENH_BANG} — cùng bang hội, <b>không cần cùng chỗ</b>, chỉ cần
 *       đang online</li>
 * </ul>
 *
 * <p>Mỗi người chỉ bật <b>một</b> kênh. Kênh nằm ngay trong gói client gửi lên,
 * máy chủ không giữ trạng thái "ai đang bật kênh nào" — client tắt mic là ngừng
 * gửi, không cần gói báo tắt, và máy chủ khởi động lại cũng không lệch trạng
 * thái với client.</p>
 *
 * <h2>Vì sao có hạn mức</h2>
 *
 * <p>Tiếng nói là luồng liên tục: một người nói không nghỉ ở 8 kHz µ-law là
 * 8 KB/s, nhân cho số người nghe. Không chặn thì một client sửa đổi có thể bơm
 * gói to gấp trăm lần và làm nghẽn cả khu. {@link #QUA_NHIEU} chặn theo
 * <b>byte mỗi giây</b> chứ không theo số gói — chặn số gói thì chỉ cần gói to
 * hơn là lách được.</p>
 */
public class VoiceService {

    private static VoiceService instance;

    public static VoiceService gI() {
        if (instance == null) {
            instance = new VoiceService();
        }
        return instance;
    }

    public static final byte KENH_TAT = -1;
    public static final byte KENH_KHU = 0;
    public static final byte KENH_MAP = 1;
    public static final byte KENH_BANG = 2;

    /** Mã gói thoại, dùng chung cho cả chiều lên và chiều xuống. */
    public static final byte OPCODE = -109;

    /** Một gói không được dài hơn ngần này byte. */
    private static final int TOI_DA_MOT_GOI = 4096;

    /** Mỗi người gửi tối đa ngần này byte mỗi giây. */
    private static final int TOI_DA_MOI_GIAY = 20_000;

    /**
     * Nhận một gói tiếng từ client và phát lại cho những người cần nghe.
     *
     * <p>Không giải mã, không đổi định dạng: byte nào vào thì byte đó ra. Máy
     * chủ mà đụng vào tiếng thì phải gánh thêm cả việc giải nén cho từng người
     * nói, trong khi chẳng để làm gì.</p>
     */
    public void nhanGoiTieng(Player player, Message msg) {
        if (player == null || player.isDie() || msg == null) {
            return;
        }
        try {
            byte kenh = msg.reader().readByte();
            int soByte = msg.reader().readShort();
            if (soByte <= 0 || soByte > TOI_DA_MOT_GOI) {
                return;
            }
            byte[] tieng = new byte[soByte];
            msg.reader().readFully(tieng);

            if (quaNhieu(player, soByte)) {
                return;
            }
            if (player.clan == null && kenh == KENH_BANG) {
                return;
            }
            phat(player, kenh, tieng);
        } catch (Exception ex) {
            Logger.logException(VoiceService.class, ex, "Lỗi nhận gói thoại");
        }
    }

    /**
     * Đếm lưu lượng trong cửa sổ một giây.
     *
     * @return {@code true} nếu người này đã gửi quá hạn mức, gói phải bỏ
     */
    private boolean quaNhieu(Player player, int soByte) {
        long bayGio = System.currentTimeMillis();
        if (bayGio - player.mocGiayTieng >= 1000L) {
            player.mocGiayTieng = bayGio;
            player.byteTiengTrongGiay = 0;
        }
        player.byteTiengTrongGiay += soByte;
        return player.byteTiengTrongGiay > TOI_DA_MOI_GIAY;
    }

    private void phat(Player nguoiNoi, byte kenh, byte[] tieng) {
        switch (kenh) {
            case KENH_KHU:
                if (nguoiNoi.zone != null) {
                    guiCho(nguoiNoi.zone.getPlayers(), nguoiNoi, kenh, tieng);
                }
                break;
            case KENH_MAP:
                if (nguoiNoi.zone != null && nguoiNoi.zone.map != null
                        && nguoiNoi.zone.map.zones != null) {
                    for (Zone z : nguoiNoi.zone.map.zones) {
                        if (z != null) {
                            guiCho(z.getPlayers(), nguoiNoi, kenh, tieng);
                        }
                    }
                }
                break;
            case KENH_BANG:
                Clan clan = nguoiNoi.clan;
                if (clan != null) {
                    guiCho(clan.membersInGame, nguoiNoi, kenh, tieng);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Gửi gói tiếng cho một danh sách người nghe, bỏ qua chính người nói.
     *
     * <p>Dựng gói <b>một lần</b> rồi gửi cho từng người: dựng lại cho mỗi người
     * nghe là nhân số lần cấp phát bộ nhớ lên bằng số người trong khu, mà nội
     * dung thì giống hệt nhau.</p>
     */
    private void guiCho(List<Player> nguoiNghe, Player nguoiNoi, byte kenh,
            byte[] tieng) {
        if (nguoiNghe == null || nguoiNghe.isEmpty()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(OPCODE);
            msg.writer().writeByte(kenh);
            msg.writer().writeInt((int) nguoiNoi.id);
            msg.writer().writeUTF(nguoiNoi.name == null ? "" : nguoiNoi.name);
            msg.writer().writeShort(tieng.length);
            msg.writer().write(tieng);
            msg.writer().flush();
            for (int i = nguoiNghe.size() - 1; i >= 0; i--) {
                Player pl = nguoiNghe.get(i);
                if (pl != null && pl.isPl() && pl.id != nguoiNoi.id) {
                    pl.sendMessage(msg);
                }
            }
        } catch (Exception ex) {
            Logger.logException(VoiceService.class, ex, "Lỗi phát gói thoại");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
