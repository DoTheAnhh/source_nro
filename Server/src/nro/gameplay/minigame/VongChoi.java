package nro.gameplay.minigame;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.entity.player.Player;
import nro.server.Client;

/**
 * Khuôn chung cho một trò chơi <b>chạy theo phiên</b>.
 *
 * <h2>Một bàn cho cả máy chủ</h2>
 *
 * <p>Mỗi trò kế thừa lớp này có <b>một</b> vòng đếm ngược dùng chung, nên ai mở
 * bảng lên cũng thấy đúng con số giây đó. Trạng thái nằm ở máy chủ, client chỉ
 * vẽ lại — không có bản sao nào ở máy người chơi để mà sửa.</p>
 *
 * <h2>Đếm ngược theo mốc thời gian, không trừ dần</h2>
 *
 * <p>Giữ {@link #ketThucLuc} là <b>mốc tuyệt đối</b> rồi lấy hiệu với hiện tại,
 * chứ không giữ một biến giây rồi trừ mỗi nhịp. Trừ dần thì mỗi nhịp trễ vài
 * mili giây, chạy vài giờ là lệch hẳn — mà đây là tiền, không phải hiệu ứng.</p>
 *
 * <h2>Không tự nuôi luồng</h2>
 *
 * <p>Lớp này <b>không</b> tạo luồng riêng. {@link NhipMiniGame} gọi
 * {@link #nhip()} mỗi giây cho tất cả các trò từ <i>một</i> luồng duy nhất. Ba
 * trò theo phiên mà ba luồng ngủ thì tốn ba lần chuyển ngữ cảnh cho cùng một
 * việc.</p>
 */
public abstract class VongChoi {

    /**
     * Nguồn ngẫu nhiên.
     *
     * <p>Dùng {@link SecureRandom} chứ không phải {@code Random} thường: đây là
     * chỗ ăn thua bằng tài sản trong game, mà {@code Random} thường sinh ra dãy
     * đoán trước được nếu biết vài kết quả liên tiếp. Vài lần tung mỗi phiên thì
     * chi phí không đáng kể.</p>
     */
    protected final SecureRandom ngauNhien = new SecureRandom();

    /** Số phiên hiện tại. */
    protected volatile long phien = 1;

    /**
     * Giai đoạn hiện tại. Ý nghĩa từng số do trò tự đặt.
     *
     * <p><b>volatile</b>: luồng nhịp ghi, các luồng mạng đọc khi người chơi bấm.
     * Không volatile thì luồng đọc có thể thấy giá trị cũ kể cả sau khi luồng
     * kia đã ghi xong — người chơi đặt cược được vào ván đã chốt.</p>
     */
    protected volatile byte giaiDoan;

    /** Mốc thời gian kết thúc giai đoạn đang chạy. */
    protected volatile long ketThucLuc;

    /**
     * Những người đang mở bảng trò này.
     *
     * <p>Chỉ gửi nhịp cho những người này chứ không phát cho cả máy chủ: một gói
     * mỗi giây nhân với toàn bộ người online là lãng phí đường truyền cho thứ mà
     * phần lớn không nhìn.</p>
     */
    protected final Set<Long> nguoiXem = ConcurrentHashMap.newKeySet();

    /** Kết quả vài phiên gần nhất, để client vẽ dải lịch sử. */
    protected final LinkedList<Byte> lichSu = new LinkedList<>();

    /** Số phiên giữ lại trong dải lịch sử. */
    protected static final int SO_LICH_SU = 15;

    /** Số giây còn lại của giai đoạn đang chạy, làm tròn lên. */
    public int giayConLai() {
        long conLai = ketThucLuc - System.currentTimeMillis();
        return conLai <= 0 ? 0 : (int) ((conLai + 999) / 1000);
    }

    public long getPhien() {
        return phien;
    }

    public byte getGiaiDoan() {
        return giaiDoan;
    }

    public List<Byte> getLichSu() {
        synchronized (lichSu) {
            return new ArrayList<>(lichSu);
        }
    }

    protected void ghiLichSuNhanh(byte ketQua) {
        synchronized (lichSu) {
            lichSu.addFirst(ketQua);
            while (lichSu.size() > SO_LICH_SU) {
                lichSu.removeLast();
            }
        }
    }

    public void themNguoiXem(Player pl) {
        if (pl != null) {
            nguoiXem.add(pl.id);
        }
    }

    public void boNguoiXem(Player pl) {
        if (pl != null) {
            nguoiXem.remove(pl.id);
        }
    }

    public boolean coNguoiXem() {
        return !nguoiXem.isEmpty();
    }

    /**
     * Người đang online trong danh sách xem, bỏ qua ai đã thoát.
     *
     * <p>Dọn luôn những id không còn online: danh sách này chỉ được gỡ khi người
     * chơi bấm đóng bảng, mà mất mạng đột ngột thì không có cú bấm nào cả.</p>
     */
    public List<Player> nguoiDangXem() {
        List<Player> ds = new ArrayList<>();
        for (Long id : nguoiXem) {
            Player pl = Client.gI().getPlayerByID(id);
            if (pl == null) {
                nguoiXem.remove(id);
            } else {
                ds.add(pl);
            }
        }
        return ds;
    }

    /**
     * Một nhịp một giây.
     *
     * <p>Còn giờ thì chỉ báo nhịp; hết giờ thì trò tự chuyển giai đoạn.</p>
     */
    public final void nhip() {
        if (giayConLai() > 0) {
            if (coNguoiXem()) {
                guiNhip();
            }
            return;
        }
        hetGio();
    }

    /** Gửi gói nhịp cho người đang xem. Trò tự lo nội dung. */
    protected abstract void guiNhip();

    /** Giai đoạn vừa hết giờ — chuyển sang giai đoạn kế. */
    protected abstract void hetGio();

    /** Hoàn tiền và dọn dấu vết khi một người rời game giữa ván. */
    public abstract void nguoiChoiRoiGame(Player pl);
}
