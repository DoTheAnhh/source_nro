package nro.gameplay.minigame;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;

/**
 * <b>Một</b> luồng đập nhịp cho tất cả trò chơi chạy theo phiên.
 *
 * <h2>Vì sao không mỗi trò một luồng</h2>
 *
 * <p>Ba trò theo phiên mà ba luồng cùng ngủ một giây rồi cùng dậy làm vài phép
 * so sánh thì tốn ba lần đánh thức và ba lần chuyển ngữ cảnh cho đúng một việc.
 * Gộp lại một luồng thì chi phí chia ba, mà thứ tự cũng đoán trước được: các trò
 * luôn nhịp theo đúng thứ tự đăng ký.</p>
 *
 * <h2>Một trò hỏng không kéo đổ trò khác</h2>
 *
 * <p>Bọc từng lời gọi trong {@code try}: nếu một trò ném ngoại lệ giữa nhịp thì
 * chỉ nhịp đó của nó hỏng, hai trò kia vẫn chạy tiếp và cả vòng lặp không
 * chết. Một luồng chung nghĩa là một điểm hỏng chung, nên chỗ này phải chắc.</p>
 *
 * <h2>Ngủ bù cho phần đã tiêu</h2>
 *
 * <p>Ngủ đúng phần còn thiếu của một giây chứ không ngủ nguyên một giây: nếu
 * nhịp vừa rồi tốn 40 mili giây thì chỉ ngủ 960. Ngủ đủ giây thì mỗi vòng trôi
 * thêm chỗ đã tiêu, chạy vài giờ là đồng hồ lệch thấy rõ.</p>
 */
public final class NhipMiniGame {

    private static NhipMiniGame instance;

    public static NhipMiniGame gI() {
        if (instance == null) {
            instance = new NhipMiniGame();
        }
        return instance;
    }

    private final List<VongChoi> danhSach = new ArrayList<>();

    private volatile boolean dangChay;

    private NhipMiniGame() {
    }

    /** Đăng ký một trò vào nhịp chung. Gọi trước {@link #batDau()}. */
    public synchronized void dangKy(VongChoi tro) {
        if (tro != null && !danhSach.contains(tro)) {
            danhSach.add(tro);
        }
    }

    /** Khởi động luồng nhịp. Gọi một lần lúc máy chủ dựng xong. */
    public synchronized void batDau() {
        if (dangChay) {
            return;
        }
        dangChay = true;
        Thread t = new Thread(this::vongLap, "Nhip mini game");
        t.setDaemon(true);
        t.start();
        Logger.success("[MINIGAME] Da bat nhip cho " + danhSach.size()
                + " tro choi theo phien.\n");
    }

    private void vongLap() {
        while (dangChay) {
            long batDau = System.currentTimeMillis();
            // Duyet theo chi so tren ban sao tham chieu: danh sach chi bi sua
            // luc dang ky, va viec do xong truoc khi luong nay chay.
            for (int i = 0; i < danhSach.size(); i++) {
                VongChoi tro = danhSach.get(i);
                try {
                    tro.nhip();
                } catch (Exception e) {
                    Logger.logException(NhipMiniGame.class, e);
                }
            }
            long daTieu = System.currentTimeMillis() - batDau;
            long conNgu = 1000L - daTieu;
            if (conNgu < 50L) {
                conNgu = 50L;
            }
            try {
                Thread.sleep(conNgu);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /** Gỡ dấu vết của một người vừa rời game khỏi mọi trò. */
    public void nguoiChoiRoiGame(nro.entity.player.Player pl) {
        for (int i = 0; i < danhSach.size(); i++) {
            try {
                danhSach.get(i).nguoiChoiRoiGame(pl);
            } catch (Exception e) {
                Logger.logException(NhipMiniGame.class, e);
            }
        }
    }
}
