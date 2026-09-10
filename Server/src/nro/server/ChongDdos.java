package nro.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import nro.core.log.Logger;

/**
 * Chắn lũ kết nối và lũ gói tin.
 *
 * <h2>Vì sao viết lại thay vì sửa {@code Antiddos} cũ</h2>
 *
 * <p>Lớp cũ có ba lỗi khiến nó vừa <b>không bảo vệ được gì</b> vừa <b>tự tạo ra
 * chỗ để đánh sập</b>:</p>
 *
 * <ol>
 *   <li><b>Không nối vào đường nhận kết nối.</b> Cả máy chủ gọi
 *       {@code Antiddos.handleRequest} đúng một lần, với {@code null}, lúc khởi
 *       động — mà tham số {@code null} thì hàm thoát ngay. Không một kết nối
 *       thật nào đi qua nó.</li>
 *   <li><b>Bộ đếm không bao giờ giảm.</b> Tên hằng số là "mỗi giây" nhưng không
 *       có chỗ nào đặt lại bộ đếm theo thời gian. Nếu có nối vào thật thì một
 *       người chơi bình thường vào ra 31 lần trong một tuần cũng bị chặn.</li>
 *   <li><b>Mỗi IP bị chặn đẻ một {@link java.util.Timer}, tức một thread.</b>
 *       Lũ từ mười nghìn IP là mười nghìn thread — chính cái chắn trở thành đòn
 *       đánh sập.</li>
 * </ol>
 *
 * <h2>Lớp này làm gì</h2>
 *
 * <ul>
 *   <li><b>Chặn theo NHỊP, không chỉ theo số kết nối đang mở.</b> Kẻ tấn công
 *       nối rồi ngắt liên tục không bao giờ chạm trần "3 kết nối cùng lúc", mà
 *       mỗi vòng vẫn tốn ba thread của máy chủ.</li>
 *   <li><b>Từ chối TRƯỚC khi dựng session.</b> Đây là điểm quan trọng nhất: mỗi
 *       session đẻ ba thread (đọc, gửi, xử lý hàng đợi), nên phải đóng socket
 *       ngay ở vòng accept, trước khi tốn thread nào.</li>
 *   <li><b>Một tác vụ dọn dẹp duy nhất</b> cho toàn bộ danh sách, thay vì một
 *       thread cho mỗi IP.</li>
 *   <li><b>Trần chung cho cả máy chủ.</b> Lũ rải từ hàng nghìn IP thì trần theo
 *       từng IP không chặn được gì; phải có thêm một cái van tổng.</li>
 *   <li><b>Chặn tăng dần.</b> Vi phạm lần đầu chặn ngắn, tái phạm chặn lâu hơn —
 *       người chơi lỡ tay không bị phạt nặng, còn kẻ cố tình thì bị đẩy ra xa.</li>
 * </ul>
 *
 * <h2>Điều lớp này KHÔNG làm được</h2>
 *
 * <p>Đây là chắn ở <b>tầng ứng dụng</b>. Lũ đủ lớn để lấp băng thông đường
 * truyền hoặc lấp bảng kết nối của hệ điều hành thì gói tin không bao giờ tới
 * được Java — chặn ở đây vô nghĩa. Chống được lớp đó phải là tường lửa, giới
 * hạn SYN ở mức hệ điều hành, hoặc dịch vụ lọc đứng trước máy chủ.</p>
 */
public final class ChongDdos {

    private ChongDdos() {
    }

    // ------------------------------------------------------------------
    //  Ngưỡng
    // ------------------------------------------------------------------

    /** Số kết nối mới tối đa của MỘT IP trong một cửa sổ. */
    private static final int KET_NOI_MOI_TOI_DA = 12;

    /** Độ dài cửa sổ trượt, mili giây. */
    private static final long CUA_SO = 10_000L;

    /** Số kết nối mới tối đa của TOÀN máy chủ trong một giây. */
    private static final int KET_NOI_MOI_CA_MAY_CHU = 60;

    /** Số gói tin tối đa một phiên gửi lên trong một giây. */
    /**
     * So goi tin toi da mot phien gui len trong mot giay.
     *
     * <p>120 la qua chat cho thao tac hang loat binh thuong — mua chin muoi
     * chin mon, nhat mot bai do roi. Nay 400, va nguoi DA DANG NHAP vuot
     * nguong thi bi ham nhip chu khong bi cat ket noi; xem { Collector}.</p>
     */
    private static final int GOI_MOI_GIAY = 400;

    /** Thời gian chặn lần đầu, mili giây. Tái phạm thì nhân đôi. */
    private static final long CHAN_LAN_DAU = 60_000L;

    /** Trần thời gian chặn, mili giây. */
    private static final long CHAN_TOI_DA = 30 * 60_000L;

    /** Quên hẳn một IP sau khi nó im lặng bằng này lâu. */
    private static final long QUEN_SAU = 10 * 60_000L;

    // ------------------------------------------------------------------
    //  Trạng thái
    // ------------------------------------------------------------------

    /** Tình trạng của một địa chỉ. */
    private static final class TinhTrang {

        /** Mốc bắt đầu cửa sổ đếm kết nối hiện tại. */
        volatile long moCuaSo;

        /** Số kết nối mới trong cửa sổ hiện tại. */
        final AtomicInteger soKetNoi = new AtomicInteger();

        /** Số kết nối đang mở của IP này. */
        final AtomicInteger dangMo = new AtomicInteger();

        /** Số lần đã bị chặn — dùng để chặn lâu dần. */
        final AtomicInteger soLanChan = new AtomicInteger();

        /** Chặn tới thời điểm này; 0 là không bị chặn. */
        volatile long chanToi;

        /** Lần cuối thấy IP này, để dọn bảng. */
        volatile long lanCuoi = System.currentTimeMillis();
    }

    private static final Map<String, TinhTrang> BANG = new ConcurrentHashMap<>();

    /** Bộ đếm kết nối mới của cả máy chủ trong giây hiện tại. */
    private static final AtomicInteger DEM_CHUNG = new AtomicInteger();

    private static final AtomicLong GIAY_CHUNG = new AtomicLong();

    /** Số lần đã từ chối, chỉ để báo cáo. */
    private static final AtomicLong DA_TU_CHOI = new AtomicLong();

    private static volatile ScheduledExecutorService donDep;

    // ------------------------------------------------------------------
    //  Vòng đời
    // ------------------------------------------------------------------

    /**
     * Bật tác vụ dọn dẹp. Gọi một lần lúc khởi động; gọi nhiều lần vô hại.
     */
    public static synchronized void batDau() {
        if (donDep != null) {
            return;
        }
        donDep = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Chong DDoS - don dep");
            t.setDaemon(true);
            return t;
        });
        donDep.scheduleWithFixedDelay(ChongDdos::don, 30, 30, TimeUnit.SECONDS);
        Logger.success("CHONG_DDOS", "Da bat"
                + " | ket noi moi: " + KET_NOI_MOI_TOI_DA + "/"
                + (CUA_SO / 1000) + "s moi IP"
                + " | " + KET_NOI_MOI_CA_MAY_CHU + "/s toan may chu"
                + " | goi: " + GOI_MOI_GIAY + "/s moi phien");
    }

    /**
     * Xoá những IP đã im lặng lâu.
     *
     * <p>Không dọn thì bảng chỉ phình ra: một lượt quét cổng đi qua vài vạn IP
     * là vài vạn mục nằm lại mãi trong bộ nhớ.</p>
     */
    private static void don() {
        long bayGio = System.currentTimeMillis();
        BANG.entrySet().removeIf(e -> {
            TinhTrang t = e.getValue();
            return t.dangMo.get() <= 0
                    && t.chanToi < bayGio
                    && bayGio - t.lanCuoi > QUEN_SAU;
        });
    }

    private static TinhTrang lay(String ip) {
        return BANG.computeIfAbsent(ip, k -> new TinhTrang());
    }

    // ------------------------------------------------------------------
    //  Cổng chặn
    // ------------------------------------------------------------------

    /**
     * Địa chỉ này có được mở thêm một kết nối không.
     *
     * <p>Gọi ở vòng accept, <b>trước khi dựng session</b>.</p>
     */
    public static boolean choPhepKetNoi(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        long bayGio = System.currentTimeMillis();

        // 1. Van tong: lu rai tu hang nghin IP thi tran theo tung IP khong chan
        //    duoc gi. Khong chan IP nao ca — chi tu choi bot ngay lap tuc, de
        //    may chu con tho.
        long giay = bayGio / 1000L;
        if (GIAY_CHUNG.getAndSet(giay) != giay) {
            DEM_CHUNG.set(0);
        }
        if (DEM_CHUNG.incrementAndGet() > KET_NOI_MOI_CA_MAY_CHU) {
            DA_TU_CHOI.incrementAndGet();
            return false;
        }

        TinhTrang t = lay(ip);
        t.lanCuoi = bayGio;

        // 2. Dang trong thoi gian bi chan.
        if (t.chanToi > bayGio) {
            DA_TU_CHOI.incrementAndGet();
            return false;
        }

        // 3. Tran so ket noi DANG MO cung luc.
        if (t.dangMo.get() >= Manager.MAX_PER_IP) {
            DA_TU_CHOI.incrementAndGet();
            return false;
        }

        // 4. Tran NHIP ket noi moi.
        //
        // Cua so truot don gian: het cua so thi mo cua so moi. Chinh xac hon
        // thi phai giu danh sach moc thoi gian, nhung the la moi IP mot danh
        // sach — dat hon nhieu ma khong doi lai duoc gi dang ke o day.
        if (bayGio - t.moCuaSo > CUA_SO) {
            t.moCuaSo = bayGio;
            t.soKetNoi.set(0);
        }
        if (t.soKetNoi.incrementAndGet() > KET_NOI_MOI_TOI_DA) {
            chan(ip, t, "noi lai qua nhanh");
            return false;
        }

        t.dangMo.incrementAndGet();
        return true;
    }

    /** Một kết nối của IP này vừa đóng. </summary> */
    public static void ngatKetNoi(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        TinhTrang t = BANG.get(ip);
        if (t == null) {
            return;
        }
        t.lanCuoi = System.currentTimeMillis();
        // Chan san o 0: goi thua mot lan khong duoc lam bo dem am, vi dem am la
        // IP do duoc mo them ket noi ngoai tran.
        if (t.dangMo.decrementAndGet() < 0) {
            t.dangMo.set(0);
        }
    }

    /**
     * Ghi một vi phạm và chặn nếu cần.
     *
     * <p>Dùng cho những lỗi <b>chỉ khách sửa mới gây ra được</b>: gói tin sai
     * khuôn, gói dài bất thường, gửi quá nhanh.</p>
     */
    public static void viPham(String ip, String lyDo) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        TinhTrang t = lay(ip);
        t.lanCuoi = System.currentTimeMillis();
        chan(ip, t, lyDo);
    }

    private static void chan(String ip, TinhTrang t, String lyDo) {
        long bayGio = System.currentTimeMillis();
        if (t.chanToi > bayGio) {
            return;
        }
        // Chan lau dan: lan dau mot phut, moi lan tai pham nhan doi, tran o 30
        // phut. Nguoi choi lo tay khong bi phat nang, ke co tinh thi bi day xa.
        int lan = t.soLanChan.incrementAndGet();
        long lau = CHAN_LAN_DAU << Math.min(lan - 1, 20);
        if (lau > CHAN_TOI_DA || lau <= 0) {
            lau = CHAN_TOI_DA;
        }
        t.chanToi = bayGio + lau;
        DA_TU_CHOI.incrementAndGet();
        Logger.warn("CHONG_DDOS", "Chan " + ip
                + " | " + (lau / 1000) + "s"
                + " | lan thu " + lan
                + " | " + lyDo);
    }

    /** Bỏ chặn một địa chỉ — dùng cho bảng điều khiển. */
    public static void boChan(String ip) {
        TinhTrang t = (ip == null) ? null : BANG.get(ip);
        if (t == null) {
            return;
        }
        t.chanToi = 0;
        t.soLanChan.set(0);
        t.soKetNoi.set(0);
        Logger.info("CHONG_DDOS", "Bo chan " + ip);
    }

    // ------------------------------------------------------------------
    //  Nhịp gói tin của một phiên
    // ------------------------------------------------------------------

    /** Bộ đếm gói tin của một phiên. Mỗi phiên giữ một cái. */
    public static final class NhipGoi {

        private long giay;
        private int dem;

        /**
         * Đếm một gói vừa nhận.
         *
         * @return {@code false} nếu phiên này đang gửi nhanh quá mức cho phép
         */
        public boolean them() {
            long g = System.currentTimeMillis() / 1000L;
            if (g != giay) {
                giay = g;
                dem = 0;
            }
            return ++dem <= GOI_MOI_GIAY;
        }
    }

    // ------------------------------------------------------------------
    //  Báo cáo
    // ------------------------------------------------------------------

    /** Một dòng tóm tắt cho bảng điều khiển hoặc log. */
    public static String tomTat() {
        int dangChan = 0;
        long bayGio = System.currentTimeMillis();
        for (TinhTrang t : BANG.values()) {
            if (t.chanToi > bayGio) {
                dangChan++;
            }
        }
        return "theo doi " + BANG.size() + " IP"
                + " | dang chan " + dangChan
                + " | da tu choi " + DA_TU_CHOI.get() + " luot";
    }
}
