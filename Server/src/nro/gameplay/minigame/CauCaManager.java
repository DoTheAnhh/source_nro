package nro.gameplay.minigame;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.repository.dao.MiniGameDAO;
import nro.service.MiniGameService;
import nro.service.Service;

/**
 * Trò <b>Câu Cá</b> — quăng cần, rồi vật lộn kéo cá lên.
 *
 * <h2>Ba bước một lượt</h2>
 *
 * <ol>
 *   <li><b>Quăng</b>: trừ tiền mồi, bốc xem con nào cắn <i>và bốc luôn bao lâu
 *       nó mới cắn</i>, gửi cả hai xuống client.</li>
 *   <li><b>Chờ</b>: phao nổi. Hết quãng chờ ấy thì cá cắn — phao giật, nút đổi
 *       thành KÉO.</li>
 *   <li><b>Vật lộn</b>: người chơi giữ nút theo vạch đang chạy để đẩy thanh
 *       tiến trình lên. Bước này chạy ở client vì nó là nhịp tay.</li>
 *   <li><b>Báo kết quả</b>: client báo bắt được hay để tuột; máy chủ trả tiền
 *       hoặc không.</li>
 * </ol>
 *
 * <p><b>Quãng chờ do máy chủ bốc</b>, không để client tự chọn: chờ là một phần
 * của trò, mà client tự chọn thì nó chọn không. Bốc trong khoảng
 * {@link #CHO_CAN_MIN_MS}..{@link #CHO_CAN_MAX_MS} nên mỗi lượt một khác, và
 * người chơi phải để mắt vào phao chứ không đếm nhịp.</p>
 *
 * <h2>Con nào cắn — và vì sao trần thiệt hại đã bịt</h2>
 *
 * <p>Bảng {@link #TRONG_SO_THEO_XA} tính theo phần nghìn, một dòng cho mỗi mức
 * tầm quăng. Kỳ vọng nếu <b>bắt được mọi con</b>:</p>
 *
 * <table border="1">
 *   <tr><th>Tầm quăng</th><th>Kỳ vọng</th></tr>
 *   <tr><td>Gần (&lt; 40)</td><td>27,4 / 20 thỏi — <b>137%</b></td></tr>
 *   <tr><td>Vừa (40–74)</td><td>33,5 / 20 — <b>168%</b></td></tr>
 *   <tr><td>Xa (≥ 75)</td><td>43,9 / 20 — <b>220%</b></td></tr>
 * </table>
 *
 * <p><b>CẢNH BÁO — trần thiệt hại đã mở.</b> Tiền mồi hạ từ 50 xuống 20 theo
 * yêu cầu, còn bảng thưởng {@link #THUONG_CA} giữ nguyên. Kỳ vọng giờ <b>cao
 * hơn</b> tiền mồi ở mọi tầm quăng, nên câu cá là một nguồn sinh vàng chứ không
 * còn là chỗ tiêu vàng.</p>
 *
 * <p>Điều đó quan trọng vì <b>cả tầm quăng lẫn bước vật lộn đều đo ở
 * client</b>. Bản 50 thỏi chịu được chuyện đó: một client sửa luôn gửi tầm 100
 * và luôn báo "bắt được" cũng chỉ đạt 88% — vẫn lỗ, nên không ai buồn sửa. Ở
 * mức 20 thỏi thì đúng kẻ đó thu <b>220%</b>, tức mỗi lượt lãi hơn gấp đôi, và
 * quãng nghỉ {@link #NGHI_MS} ba giây là thứ duy nhất còn giới hạn tốc độ in
 * tiền.</p>
 *
 * <p>Muốn giữ mức 20 mà không mở đường đó thì hạ {@link #THUONG_CA} theo cùng
 * tỉ lệ (chia 2,5): {@code {4, 8, 20, 40, 80}} đưa kỳ vọng về đúng 55/67/88%
 * như cũ.</p>

 *
 * <p><b>Cột "rộng vùng" là chỗ tay nghề lộ ra rõ nhất</b>: vùng xanh của cá
 * ngựa rộng hơn một phần tư thanh, còn của cá vàng chỉ chưa tới một phần mười.
 * Giữ vạch nằm trong một khe hẹp nhường ấy, trong khi khe đó tự di chuyển, là
 * việc phải tập mới làm được.</p>
 *
 * <p><b>Không</b> áp cách này cho chỗ mà một con số sửa được đổi thẳng ra tiền
 * lãi. Ở đó thì luật phải nằm trọn trong máy chủ.</p>
 *
 * <h2>Chặn báo kết quả quá nhanh</h2>
 *
 * <p>Ngoài trần kỳ vọng còn một chốt nữa: thanh tiến trình đi từ
 * {@link #TIEN_TRINH_DAU} tới 100 với tốc độ tối đa là {@code tocDoTang} của
 * con đó, nên có một <b>thời gian tối thiểu</b> không thể nhanh hơn. Báo sớm hơn
 * tám mươi phần trăm thời gian ấy là bịa, và bị bỏ.</p>
 *
 * <h2>Vì sao có quãng nghỉ</h2>
 *
 * <p>{@link #NGHI_MS} giữa hai lần quăng, chặn ở máy chủ chứ không chỉ ở client.
 * Không có nó thì một client tự động quăng liên tục được, và trò biến thành vòng
 * lặp bấm nút.</p>
 */
public final class CauCaManager {

    private static CauCaManager instance;

    public static CauCaManager gI() {
        if (instance == null) {
            instance = new CauCaManager();
        }
        return instance;
    }

    public static final int MA_TRO = MiniGameService.TRO_CAU_CA;

    /**
     * Tiền mồi cho một lần quăng, tính bằng thỏi vàng.
     *
     * <p>Client <b>không</b> giữ bản sao của số này: nó đọc từ gói tin
     * ({@code MiniGameService} ghi {@code TIEN_MOI} xuống, chữ "-20" trên nút
     * Quăng cần là in lại số vừa nhận). Đổi ở đây là đổi cả hai bên.</p>
     *
     * <p><b>Trước đây là 50, và 50 là con số giữ cho trò không in ra tiền.</b>
     * Xem bảng kỳ vọng ở đầu lớp. Hạ xuống 20 thì kỳ vọng vượt hẳn tiền mồi —
     * đây là một quyết định về cân bằng, không phải một phép sửa lỗi.</p>
     */
    public static final long TIEN_MOI = 20;

    /** Quãng nghỉ bắt buộc giữa hai lần quăng, tính bằng mili giây. */
    public static final long NGHI_MS = 3000;

    /**
     * Thanh tiến trình khởi từ mức này.
     *
     * <p>Hai mươi phần trăm — trước là ba mươi. Cho khoảng kéo dài ra một chút,
     * và cũng cho cái mốc khởi đầu vẽ trên thanh nằm gần mép trái hơn nên dễ
     * thấy mình đang hơn hay kém lúc đầu.</p>
     */
    public static final int TIEN_TRINH_DAU = 20;

    /** Quãng chờ cá cắn, ngắn nhất và dài nhất, tính bằng mili giây. */
    public static final int CHO_CAN_MIN_MS = 1600;
    public static final int CHO_CAN_MAX_MS = 5200;

    /**
     * Ván vật lộn bỏ dở quá lâu thì dọn đi, tính bằng mili giây.
     *
     * <p>Ba mươi giây. Một lượt thật dài nhất cũng chỉ chừng mười lăm giây —
     * năm giây chờ cá cắn cộng mười giây vật lộn con khó nhất — nên quá ba mươi
     * giây là chắc chắn bỏ dở.</p>
     *
     * <p>Trước là chín mươi giây, và đó là một cái bẫy: client tắt giữa lúc vật
     * lộn thì ván treo lại, và <b>mọi cú quăng sau đều bị từ chối</b> cho tới
     * hết chín mươi giây ấy. Người chơi bấm quăng mà không thấy gì xảy ra, còn
     * lời nhắc "đang kéo một con chưa xong" thì trôi mất trong dòng thông báo.</p>
     */
    private static final long HET_HAN_MS = 30000;

    /**
     * Số thỏi nhận về của từng loại cá.
     *
     * <p><b>Năm loại</b>, đúng bằng số loài trong bộ ảnh cá
     * ({@code data/icon/Ca}). Bản trước có sáu loại trong khi bộ ảnh chỉ có năm
     * loài, nên loại thứ sáu không có hình để mà vẽ.</p>
     */
    private static final int[] THUONG_CA = { 10, 20, 50, 100, 200 };

    /**
     * Trọng số con nào cắn, theo <b>phần nghìn</b>, một dòng cho mỗi mức tầm.
     *
     * <p><b>Mỗi dòng giảm dần từ trái sang phải</b>: con càng đáng tiền thì càng
     * ít cắn. Cột xếp theo giá tăng dần nên đọc ngang một dòng là thấy ngay luật
     * ấy — và nếu sau này sửa số mà một dòng không còn giảm dần, thì chính cách
     * xếp này làm chỗ sai lộ ra ngay.</p>
     *
     * <p>Quăng xa thì cá to hơn, đúng như ngoài đời — và đó là lý do để tập căn
     * tầm chứ không phải cứ bấm cho xong. Ba mức chứ không nội suy trơn: ba mức
     * thì người chơi thấy rõ ranh giới và biết mình đang ở mức nào, còn nội suy
     * trơn thì không ai đọc ra được gì.</p>
     *
     * <p>Phần nghìn chứ không phần trăm để tả được con cá vàng ở mức một phần
     * trăm — phần trăm tròn đã là quá thô cho con trả gấp bốn lần tiền mồi.</p>
     */
    private static final int[][] TRONG_SO_THEO_XA = {
        // ngựa, thu, cờ, mập, vàng
        { 450, 320, 150,  70, 10 },   // gần
        { 400, 300, 180,  95, 25 },   // vừa
        { 330, 280, 200, 130, 60 }    // xa
    };

    /**
     * Ngưỡng tầm để lên mức vừa và mức xa.
     *
     * <p>Hai con số này phải khớp <c>ccMauTheoTam</c> và <c>ccTenMucTam</c> bên
     * client: hiện màu một mức mà tính tiền theo mức khác thì người chơi thấy
     * vòng ngắm xanh rồi nhận cá của mức đỏ.</p>
     */
    private static final int XA_VUA = 40;
    private static final int XA_XA = 75;

    /**
     * Bộ tham số độ khó của từng loại, gửi nguyên xuống client.
     *
     * <p>Bốn cột: <b>tốc độ tăng</b> mỗi giây khi giữ đúng lúc, <b>tốc độ
     * giảm</b> mỗi giây khi giữ sai hoặc buông, <b>tốc độ vạch</b> chạy trên
     * thanh vật lộn, và <b>bề rộng vùng an toàn</b> theo phần trăm thanh.</p>
     *
     * <p>Con càng đáng tiền thì tăng chậm hơn, giảm nhanh hơn, vạch chạy nhanh
     * hơn và vùng an toàn hẹp hơn — bốn cách làm khó cùng một lúc, nên độ khó
     * lên rõ chứ không chỉ nhích một chút.</p>
     *
     * <p><b>Hai cột đầu đã chia đôi so với bản trước</b>: thanh tiến trình lên
     * quá nhanh, một lượt kéo xong trong hơn một giây nên chưa kịp thành một
     * cuộc vật lộn. Chia <i>cả hai</i> chứ không riêng cột tăng — giữ nguyên tỉ
     * lệ giữa lên và xuống thì độ khó không đổi, chỉ dài ra. Hạ mỗi cột tăng thì
     * trò thành ra khó hơn hẳn, mà đó không phải điều được yêu cầu.</p>
     *
     * <p><b>Cột tăng đã chia đôi thêm một lần nữa.</b> Cộng với mốc khởi đầu
     * hạ từ ba mươi xuống hai mươi phần trăm, một lượt hoàn hảo giờ mất chừng
     * <b>bốn giây</b> với cá ngựa và <b>mười giây</b> với cá vàng.</p>
     *
     * <p>Cột giảm giữ nguyên, và hệ số nhân bên client cũng đã hạ xuống một
     * phần mười — nên tụt rất chậm. Nghĩa là con cá gần như không tuột được
     * nữa, chỉ là kéo lâu hay nhanh. Đây là lựa chọn có ý thức: đổi cái căng
     * thẳng "có thể mất" lấy cái chắc chắn "cứ giữ là được".</p>
     *
     * <p>Con số nằm ở <b>máy chủ</b> chứ không ở client, dù client mới là bên
     * dùng: một bảng ở một chỗ thì sửa cân bằng không phải dựng lại client, và
     * không có chuyện hai bên lệch nhau.</p>
     */
    private static final int[][] DO_KHO = {
        // tăng, giảm, tốc vạch, rộng vùng
        { 20,  6, 22, 26 },   // cá ngựa con — dễ nhất
        { 15,  9, 32, 20 },   // cá thu
        { 12, 12, 44, 16 },   // cá cờ
        { 10, 16, 58, 12 },   // cá mập
        {  8, 20, 76,  9 }    // cá vàng
    };

    /**
     * Tên hiển thị, cùng thứ tự với các cột trọng số.
     *
     * <p>Sáu tên này khớp <b>đúng sáu loài</b> trong bộ sprite cá của client
     * (dải {@code cc_ca}), theo đúng thứ tự. Đổi tên ở đây mà không đổi dải ảnh
     * thì người chơi thấy tên một con nhưng hình một con khác.</p>
     */
    public static final String[] TEN_CA = {
        "Cá ngựa con", "Cá thu", "Cá cờ", "Cá mập", "Cá vàng"
    };

    /** Một ván vật lộn đang treo, chờ client báo kết quả. */
    private static final class VanCho {
        final int loai;
        final long luc;
        /** Quãng chờ cá cắn của lượt này, để tính lại thời gian tối thiểu. */
        final int choMs;

        /** Tầm quăng, ghi xuống lịch sử để panel soát được. */
        final int xa;

        VanCho(int loai, long luc, int choMs, int xa) {
            this.loai = loai;
            this.luc = luc;
            this.choMs = choMs;
            this.xa = xa;
        }
    }

    /** Ván đang treo của từng người. */
    private final Map<Long, VanCho> vanCho = new ConcurrentHashMap<>();

    /** Mốc lần quăng gần nhất của từng người, để tính quãng nghỉ. */
    private final Map<Long, Long> quangGanNhat = new ConcurrentHashMap<>();

    private final SecureRandom ngauNhien = new SecureRandom();

    /** Tổng trọng số từng dòng, tính một lần lúc dựng. */
    private final int[] tongTrongSo = new int[TRONG_SO_THEO_XA.length];

    private CauCaManager() {
        for (int m = 0; m < TRONG_SO_THEO_XA.length; m++) {
            int t = 0;
            for (int i = 0; i < TRONG_SO_THEO_XA[m].length; i++) {
                t += TRONG_SO_THEO_XA[m][i];
            }
            tongTrongSo[m] = t;
        }
    }

    /** Mức tầm ứng với một con số tầm quăng 0..100. */
    private static int mucTam(int xa) {
        if (xa >= XA_XA) {
            return 2;
        }
        if (xa >= XA_VUA) {
            return 1;
        }
        return 0;
    }

    /** Bốc loại cá cắn mồi theo trọng số của mức tầm. */
    private int bocCa(int xa) {
        int m = mucTam(xa);
        int[] dong = TRONG_SO_THEO_XA[m];
        int diem = ngauNhien.nextInt(tongTrongSo[m]);
        for (int i = 0; i < dong.length; i++) {
            diem -= dong[i];
            if (diem < 0) {
                return i;
            }
        }
        return 0;
    }

    /** Bộ tham số độ khó của một loại, để service gửi xuống. */
    public static int[] doKho(int loai) {
        if (loai < 0 || loai >= DO_KHO.length) {
            return DO_KHO[0];
        }
        return DO_KHO[loai];
    }

    public static int thuongCua(int loai) {
        if (loai < 0 || loai >= THUONG_CA.length) {
            return 0;
        }
        return THUONG_CA[loai];
    }

    /**
     * Thời gian tối thiểu để kéo được con này lên, tính bằng mili giây.
     *
     * <p>Thanh đi từ {@link #TIEN_TRINH_DAU} tới 100 mà không tụt lần nào, ở tốc
     * độ tăng tối đa của con đó. Nhanh hơn con số này là không thể.</p>
     */
    private static long thoiGianToiThieu(int loai, int choMs) {
        int tang = doKho(loai)[0];
        if (tang <= 0) {
            return choMs;
        }
        // Cong ca quang cho: dong ho tinh tu luc QUANG, ma nguoi choi khong the
        // bat dau keo truoc khi ca can. Khong cong thi moi luot cho lau deu bi
        // coi la hop le du bao ket qua ngay lap tuc.
        return choMs + (long) (100 - TIEN_TRINH_DAU) * 1000L / tang;
    }

    // ------------------------------------------------------------------
    //  Bước 1: quăng cần
    // ------------------------------------------------------------------

    /**
     * Quăng cần.
     *
     * @param xa tầm quăng người chơi căn được, 0..100. Máy chủ <b>ép về khoảng
     *           này</b> chứ không tin con số gửi lên — trần thiệt hại đã bịt
     *           bằng bảng trọng số, xem phần bàn ở đầu lớp.
     */
    public void quangCan(Player pl, int xa) {
        if (pl == null) {
            return;
        }
        if (xa < 0) {
            xa = 0;
        }
        if (xa > 100) {
            xa = 100;
        }
        donVanHetHan();

        VanCho dangCo = vanCho.get(pl.id);
        if (dangCo != null) {
            Service.gI().sendThongBao(pl, "Bạn đang kéo một con chưa xong!");
            return;
        }
        long bayGio = System.currentTimeMillis();
        Long truoc = quangGanNhat.get(pl.id);
        if (truoc != null && bayGio - truoc < NGHI_MS) {
            long con = (NGHI_MS - (bayGio - truoc) + 999) / 1000;
            Service.gI().sendThongBao(pl, "Chờ " + con + " giây nữa mới quăng tiếp được!");
            return;
        }
        if (KhoVang.demTatCa(pl) < TIEN_MOI) {
            Service.gI().sendThongBao(pl, "Cần " + TIEN_MOI + " thỏi vàng tiền mồi!");
            return;
        }
        if (!KhoVang.conChoNhanThuong(pl)) {
            Service.gI().sendThongBao(pl, "Hành trang đầy, không thể nhận thưởng!");
            return;
        }
        if (!KhoVang.tru(pl, TIEN_MOI)) {
            Service.gI().sendThongBao(pl, "Trừ thỏi vàng thất bại!");
            return;
        }
        quangGanNhat.put(pl.id, bayGio);

        int loai = bocCa(xa);
        int choMs = CHO_CAN_MIN_MS
                + ngauNhien.nextInt(CHO_CAN_MAX_MS - CHO_CAN_MIN_MS + 1);
        vanCho.put(pl.id, new VanCho(loai, bayGio, choMs, xa));
        MiniGameService.gI().cauCaGuiCaCan(pl, loai, choMs);
    }

    // ------------------------------------------------------------------
    //  Bước 3: client báo kết quả vật lộn
    // ------------------------------------------------------------------

    /**
     * Nhận báo cáo kết quả vật lộn.
     *
     * @param batDuoc client báo đã kéo được lên hay để tuột
     */
    public void baoKetQua(Player pl, boolean batDuoc) {
        if (pl == null) {
            return;
        }
        VanCho v = vanCho.remove(pl.id);
        if (v == null) {
            // Khong co van nao dang treo: hoac da bao roi, hoac goi tin bia.
            return;
        }

        long daQua = System.currentTimeMillis() - v.luc;
        // Bon muoi phan tram, khong phai tam muoi.
        //
        // Tien trinh gio len bang tung CU GIAT ROI chu khong tang lien tuc, nen
        // nguoi choi giat nhanh co the ve dich som hon mo hinh lien tuc du doan.
        // Chot nay chi de chan cu bao TUC THI, khong phai de do dung toc do —
        // giu tam muoi thi no chan oan nguoi choi giat nhanh.
        long toiThieu = thoiGianToiThieu(v.loai, v.choMs) * 40 / 100;
        if (batDuoc && daQua < toiThieu) {
            Logger.warning("[CauCa] " + pl.name + " bao bat duoc "
                    + TEN_CA[v.loai] + " sau " + daQua + "ms, toi thieu "
                    + toiThieu + "ms. Bo qua.\n");
            batDuoc = false;
        }

        long thuong = batDuoc ? THUONG_CA[v.loai] : 0;
        if (!batDuoc) {
            Service.gI().sendThongBao(pl, "Câu Cá: để tuột mất "
                    + TEN_CA[v.loai] + "!");
        } else if (thuong <= 0) {
            Service.gI().sendThongBao(pl, "Câu Cá: chỉ vớt được "
                    + TEN_CA[v.loai] + "...");
        } else if (KhoVang.themKhoa(pl, thuong)) {
            Service.gI().sendThongBao(pl, "Câu Cá: kéo được " + TEN_CA[v.loai]
                    + " — nhận " + thuong + " thỏi vàng khoá!");
        } else {
            Logger.error("[CauCa] Hanh trang day, khong tra duoc " + thuong
                    + " thoi cho " + pl.name + ".\n");
            Service.gI().sendThongBao(pl,
                    "Câu Cá: hành trang đầy, không nhận được thưởng!");
        }

        ghiVan(pl, v.loai, thuong, batDuoc, v.xa);
        MiniGameService.gI().cauCaGuiKetQua(pl, v.loai, thuong, batDuoc);
    }

    /**
     * Dọn những ván treo quá lâu.
     *
     * <p>Client tắt giữa lúc vật lộn thì không có ai báo kết quả, và ván treo mãi
     * — người chơi không quăng tiếp được vì máy chủ tưởng còn đang kéo. Bỏ luôn
     * chứ không trả lại tiền mồi: mồi đã mất từ lúc quăng, đúng như khi để tuột.</p>
     */
    private void donVanHetHan() {
        long bayGio = System.currentTimeMillis();
        for (Map.Entry<Long, VanCho> e : vanCho.entrySet()) {
            if (bayGio - e.getValue().luc > HET_HAN_MS) {
                vanCho.remove(e.getKey());
            }
        }
    }

    /**
     * Ghi lượt câu xuống cơ sở dữ liệu.
     *
     * <p>Cột {@code cua} giữ loại cá, {@code ket_qua} giữ 1 hay 0 cho bắt được
     * hay tuột. Nhờ thế panel trả lời được câu đáng hỏi nhất: người này bắt được
     * bao nhiêu phần trăm — tỉ lệ gần một trăm là dấu của client sửa.</p>
     */
    private void ghiVan(Player pl, int loai, long thuong, boolean batDuoc,
            int xa) {
        final long id = pl.id;
        final String ten = pl.name;
        // Cot "phien" giu TAM QUANG — tro nay khong co phien nen cot do dang
        // trong. Nho the panel doc ra ngay ai luon gui tam 100.
        Thread t = new Thread(() -> {
            try {
                MiniGameDAO.ghiCuoc(MA_TRO, xa, id, ten, loai, TIEN_MOI,
                        batDuoc ? 1 : 0, thuong > 0, thuong);
            } catch (Exception e) {
                Logger.logException(CauCaManager.class, e);
            }
        }, "Cau Ca ghi lich su");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Người chơi rời game.
     *
     * <p>Bỏ ván đang treo, không hoàn tiền mồi: mồi mất từ lúc quăng. Hoàn thì
     * thoát game giữa lúc vật lộn thành một nước đi — thấy sắp tuột là rút phích.</p>
     */
    public void nguoiChoiRoiGame(Player pl) {
        if (pl != null) {
            quangGanNhat.remove(pl.id);
            vanCho.remove(pl.id);
        }
    }

    /** Số giây còn phải chờ mới quăng tiếp được. */
    public int giayConCho(Player pl) {
        if (pl == null) {
            return 0;
        }
        Long truoc = quangGanNhat.get(pl.id);
        if (truoc == null) {
            return 0;
        }
        long con = NGHI_MS - (System.currentTimeMillis() - truoc);
        return con <= 0 ? 0 : (int) ((con + 999) / 1000);
    }

    /** Đang có ván vật lộn treo hay không. */
    public boolean dangVatLon(Player pl) {
        return pl != null && vanCho.containsKey(pl.id);
    }
}
