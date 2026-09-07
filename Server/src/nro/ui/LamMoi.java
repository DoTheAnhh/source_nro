package nro.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import nro.core.log.Logger;

/**
 * Chỗ hẹn để các màn báo nhau "dữ liệu vừa đổi, vẽ lại đi".
 *
 * <h2>Vì sao cần</h2>
 *
 * <p>Cùng một mẩu dữ liệu hiện ở nhiều nơi. Thêm một vật phẩm là đụng tới bảng
 * danh mục, ô lọc theo loại, bộ chọn vật phẩm của cửa hàng, của công thức đổi,
 * của đồ rơi từ boss — mỗi chỗ giữ một bản sao riêng. Trước đây mỗi nơi chỉ nạp
 * đúng một lần lúc dựng giao diện, nên sửa ở màn này thì màn kia vẫn hiện số
 * cũ cho tới khi khởi động lại panel.</p>
 *
 * <p>Bắt từng chỗ sửa phải nhớ gọi tay đủ mọi nơi liên quan là không xong được:
 * thêm một màn mới là phải đi sửa lại hết các chỗ sửa cũ, và chắc chắn sẽ sót.
 * Ở đây thì ngược lại — nơi sửa chỉ việc <b>hô một tiếng</b> theo chủ đề, nơi
 * nào quan tâm thì tự đăng ký nghe.</p>
 *
 * <h2>Dùng thế nào</h2>
 *
 * <pre>
 *   // luc dung man hinh
 *   LamMoi.nghe(LamMoi.VAT_PHAM, this::fillCatalog);
 *
 *   // sau khi ghi CSDL xong
 *   LamMoi.bao(LamMoi.VAT_PHAM);
 * </pre>
 *
 * <p>Việc vẽ lại luôn chạy trên luồng giao diện, nên gọi {@link #bao} từ đâu
 * cũng được.</p>
 */
public final class LamMoi {

    /** Mẫu vật phẩm: thêm, sửa, xoá, đổi ảnh hay loại của một món. */
    public static final String VAT_PHAM = "vat_pham";

    /** Tên các loại vật phẩm trong bảng {@code loai_vat_pham}. */
    public static final String LOAI = "loai";

    /** Tệp ảnh icon trên đĩa. */
    public static final String ANH = "anh";

    /** Cửa hàng, tab cửa hàng, món bày bán. */
    public static final String SHOP = "shop";

    /** Nhân vật và tài khoản. */
    public static final String NGUOI_CHOI = "nguoi_choi";

    /** Sự kiện, đồ rơi theo sự kiện, công thức đổi. */
    public static final String SU_KIEN = "su_kien";

    /** Boss và đồ rơi từ boss. */
    public static final String BOSS = "boss";

    private static final Map<String, List<Runnable>> NGUOI_NGHE
            = new LinkedHashMap<>();

    /**
     * Đang trong lượt phát tin nào đó.
     *
     * <p>Chặn vòng lặp: một người nghe vẽ lại bảng, việc đó lại làm bắn ra một
     * tiếng hô nữa, và cứ thế tới khi tràn ngăn xếp. Đang phát thì bỏ qua
     * tiếng hô mới.</p>
     */
    private static boolean dangPhat;

    private LamMoi() {
    }

    /** Đăng ký một việc cần làm khi chủ đề này thay đổi. */
    public static void nghe(String chuDe, Runnable viec) {
        if (chuDe == null || viec == null) {
            return;
        }
        NGUOI_NGHE.computeIfAbsent(chuDe, k -> new ArrayList<>()).add(viec);
    }

    /** Đăng ký một việc cho nhiều chủ đề cùng lúc. */
    public static void nghe(Runnable viec, String... cacChuDe) {
        for (String chuDe : cacChuDe) {
            nghe(chuDe, viec);
        }
    }

    /** Báo cho mọi nơi đang nghe chủ đề này rằng dữ liệu đã đổi. */
    public static void bao(String chuDe) {
        if (SwingUtilities.isEventDispatchThread()) {
            phat(chuDe);
        } else {
            SwingUtilities.invokeLater(() -> phat(chuDe));
        }
    }

    /** Báo nhiều chủ đề một lượt. */
    public static void bao(String... cacChuDe) {
        for (String chuDe : cacChuDe) {
            bao(chuDe);
        }
    }

    private static void phat(String chuDe) {
        List<Runnable> ds = NGUOI_NGHE.get(chuDe);
        if (ds == null || ds.isEmpty() || dangPhat) {
            return;
        }
        dangPhat = true;
        try {
            // Duyet ban sao: mot nguoi nghe co the dang ky them nguoi khac
            // trong luc chay, sua thang danh sach dang duyet la nem loi.
            for (Runnable viec : new ArrayList<>(ds)) {
                try {
                    viec.run();
                } catch (Exception ex) {
                    // Mot man ve hong khong duoc keo do ca day. Ghi log roi di
                    // tiep, khong thi bang o man sau cung dung im theo.
                    Logger.logException(LamMoi.class, ex,
                            "Lỗi khi vẽ lại theo chủ đề " + chuDe);
                }
            }
        } finally {
            dangPhat = false;
        }
    }
}
