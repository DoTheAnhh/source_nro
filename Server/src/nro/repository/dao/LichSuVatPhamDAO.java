package nro.repository.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import nro.core.log.Logger;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Nhật ký <b>nhận vật phẩm</b> của từng nhân vật.
 *
 * <h2>Vì sao có bảng này</h2>
 *
 * <p>{@link HistoryTransactionDAO} chỉ ghi lượt trao tay giữa hai người chơi.
 * Còn mọi đường khác — quái rơi, nhặt dưới đất, mua ở NPC, ghép, giftcode,
 * nhiệm vụ, sự kiện, admin cấp — thì trước đây <b>không để lại dấu vết nào</b>.
 * Món đồ lạ nằm trong hành trang một người mà không ai trả lời được nó từ đâu
 * ra.</p>
 *
 * <h2>Phương thức nhận suy từ ngăn xếp lời gọi</h2>
 *
 * <p>Có <b>hơn hai trăm</b> chỗ trong mã gọi {@code addItemBag}. Bắt mỗi chỗ tự
 * khai "tôi là nguồn nào" nghĩa là sửa hai trăm chỗ, và chỗ thêm mới về sau sẽ
 * quên khai — nhật ký thủng lỗ chỗ mà không ai biết.</p>
 *
 * <p>Nên nguồn được đọc từ <b>ngăn xếp lời gọi</b>: khung gần nhất nằm ngoài
 * lớp kho vận và lớp này chính là chỗ trao vật phẩm. Cách đó đúng với cả mã
 * hiện có lẫn mã viết sau, không cần ai nhớ gì. Cột {@code nguon_ky_thuat} giữ
 * nguyên văn {@code Lớp.hàm:dòng}; cột {@code phuong_thuc} là tên tiếng Việt
 * tra từ {@link #TEN_QUEN} cho dễ đọc — tra không ra thì để nguyên tên lớp chứ
 * không đoán bừa.</p>
 *
 * <p>Chỗ nào muốn nói rõ hơn ngăn xếp thì gọi {@link #datNguon(String)} trước,
 * và <b>bắt buộc</b> {@link #xoaNguon()} trong khối {@code finally}.</p>
 *
 * <h2>Ghi ở luồng riêng</h2>
 *
 * <p>Nhặt đồ nằm trong vòng lặp game. Một lần {@code INSERT} chờ CSDL trả lời
 * là cả bản đồ khựng theo. Nên {@link #ghi} chỉ <b>bỏ vào hàng đợi</b> rồi trả
 * về ngay; một luồng nền gom thành lô mà ghi. Hàng đợi đầy thì bỏ bản ghi mới
 * và đếm số đã bỏ — mất một dòng nhật ký còn hơn treo máy chủ.</p>
 */
public final class LichSuVatPhamDAO {

    private LichSuVatPhamDAO() {
    }

    // =====================================================================
    //  Bảng
    // =====================================================================

    /**
     * Tự tạo bảng nếu chưa có. Người dùng không phải chạy câu SQL nào bằng tay.
     *
     * <p>{@code luc} để {@code datetime(3)} — hai món nhặt trong cùng một giây
     * là chuyện thường, thiếu phần mili thì không còn biết món nào trước.</p>
     */
    private static final String LUOC_DO
            = "CREATE TABLE IF NOT EXISTS `lich_su_vat_pham` ("
            + " `id` bigint(20) NOT NULL AUTO_INCREMENT,"
            + " `player_id` bigint(20) NOT NULL,"
            + " `player_ten` varchar(64) NOT NULL DEFAULT '',"
            + " `item_id` int(11) NOT NULL DEFAULT 0,"
            + " `item_ten` varchar(120) NOT NULL DEFAULT '',"
            + " `so_luong` int(11) NOT NULL DEFAULT 1,"
            + " `noi_nhan` varchar(24) NOT NULL DEFAULT '',"
            + " `phuong_thuc` varchar(80) NOT NULL DEFAULT '',"
            + " `nguon_ky_thuat` varchar(180) NOT NULL DEFAULT '',"
            + " `map_id` int(11) NOT NULL DEFAULT -1,"
            + " `map_ten` varchar(80) NOT NULL DEFAULT '',"
            + " `chi_so` varchar(500) NOT NULL DEFAULT '',"
            + " `suc_manh` bigint(20) NOT NULL DEFAULT 0,"
            + " `luc` datetime(3) NOT NULL,"
            + " PRIMARY KEY (`id`),"
            + " KEY `idx_nguoi_luc` (`player_id`, `luc`),"
            + " KEY `idx_luc` (`luc`),"
            + " KEY `idx_item` (`item_id`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static volatile boolean daTaoBang;

    private static void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        synchronized (LichSuVatPhamDAO.class) {
            if (daTaoBang) {
                return;
            }
            try {
                ConnectDB.executeUpdate(LUOC_DO);
                daTaoBang = true;
            } catch (Exception ex) {
                Logger.logException(LichSuVatPhamDAO.class, ex,
                        "Không tạo được bảng lich_su_vat_pham");
            }
        }
    }

    // =====================================================================
    //  Một dòng nhật ký
    // =====================================================================

    /** Một lần nhận vật phẩm, như panel nhìn thấy. */
    public static final class Dong {

        public long id;
        public long nguoiId;
        public String nguoiTen = "";
        public int itemId;
        public String itemTen = "";
        public int soLuong;
        public String noiNhan = "";
        public String phuongThuc = "";
        public String nguonKyThuat = "";
        public int mapId = -1;
        public String mapTen = "";
        public String chiSo = "";
        public long sucManh;
        public Timestamp luc;
    }

    // =====================================================================
    //  Hàng đợi ghi
    // =====================================================================

    /**
     * Sức chứa hàng đợi.
     *
     * <p>Hai vạn dòng đủ ôm một đợt dồn dập (boss chết, cả bản đồ nhặt cùng
     * lúc) trong khi luồng nền đang ghi lô trước. Đầy hơn nữa thì vấn đề nằm ở
     * CSDL chứ không phải ở kích thước hàng đợi.</p>
     */
    private static final int SUC_CHUA = 20_000;

    /** Số dòng ghi trong một lô. Một lô là một lần đi CSDL. */
    private static final int MOI_LO = 500;

    private static final ArrayBlockingQueue<Dong> HANG_DOI
            = new ArrayBlockingQueue<>(SUC_CHUA);

    /** Số dòng đã bỏ vì hàng đợi đầy — để còn biết mà kêu. */
    private static final AtomicLong DA_BO = new AtomicLong();

    private static volatile Thread luongGhi;

    private static final String CAU_CHEN
            = "INSERT INTO lich_su_vat_pham"
            + " (player_id, player_ten, item_id, item_ten, so_luong, noi_nhan,"
            + "  phuong_thuc, nguon_ky_thuat, map_id, map_ten, chi_so, suc_manh, luc)"
            + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * Bật luồng ghi nền. Gọi một lần lúc khởi động; gọi lại không sao.
     */
    public static synchronized void batDau() {
        if (luongGhi != null) {
            return;
        }
        damBaoBang();
        Thread t = new Thread(LichSuVatPhamDAO::vongGhi, "ghi-lich-su-vat-pham");
        // Luồng nền: máy chủ tắt thì không đợi nó. Phần chưa ghi được xả nốt
        // trong donDep() lúc tắt có trật tự.
        t.setDaemon(true);
        // Dưới mức thường: nhật ký không bao giờ được tranh máy với vòng lặp game.
        t.setPriority(Thread.NORM_PRIORITY - 1);
        luongGhi = t;
        t.start();
    }

    private static void vongGhi() {
        List<Dong> lo = new ArrayList<>(MOI_LO);
        while (true) {
            try {
                Dong d = HANG_DOI.poll(2, TimeUnit.SECONDS);
                if (d == null) {
                    continue;
                }
                lo.clear();
                lo.add(d);
                HANG_DOI.drainTo(lo, MOI_LO - 1);
                ghiLo(lo);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                Logger.logException(LichSuVatPhamDAO.class, ex,
                        "Lỗi ghi nhật ký nhận vật phẩm");
            }
        }
    }

    private static void ghiLo(List<Dong> lo) {
        if (lo.isEmpty()) {
            return;
        }
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(CAU_CHEN)) {
            for (Dong d : lo) {
                ps.setLong(1, d.nguoiId);
                ps.setString(2, cat(d.nguoiTen, 64));
                ps.setInt(3, d.itemId);
                ps.setString(4, cat(d.itemTen, 120));
                ps.setInt(5, d.soLuong);
                ps.setString(6, cat(d.noiNhan, 24));
                ps.setString(7, cat(d.phuongThuc, 80));
                ps.setString(8, cat(d.nguonKyThuat, 180));
                ps.setInt(9, d.mapId);
                ps.setString(10, cat(d.mapTen, 80));
                ps.setString(11, cat(d.chiSo, 500));
                ps.setLong(12, d.sucManh);
                ps.setTimestamp(13, d.luc);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception ex) {
            Logger.logException(LichSuVatPhamDAO.class, ex,
                    "Không ghi được " + lo.size() + " dòng nhật ký nhận vật phẩm");
        }
    }

    /**
     * Xả nốt phần còn trong hàng đợi. Gọi lúc tắt máy chủ có trật tự.
     */
    public static void donDep() {
        List<Dong> con = new ArrayList<>();
        HANG_DOI.drainTo(con);
        while (!con.isEmpty()) {
            int n = Math.min(MOI_LO, con.size());
            ghiLo(new ArrayList<>(con.subList(0, n)));
            con.subList(0, n).clear();
        }
    }

    /** Số dòng đang chờ ghi. */
    public static int dangCho() {
        return HANG_DOI.size();
    }

    /** Số dòng đã bỏ vì hàng đợi đầy. */
    public static long daBo() {
        return DA_BO.get();
    }

    // =====================================================================
    //  Nguồn khai tay
    // =====================================================================

    private static final ThreadLocal<String> NGUON_TAY = new ThreadLocal<>();

    /**
     * Khai rõ phương thức cho những lần trao vật phẩm sắp tới <b>trên luồng
     * này</b>, đè lên tên suy từ ngăn xếp.
     *
     * <p>Phải trả lại bằng {@link #xoaNguon()} trong {@code finally}, nếu không
     * thì mọi vật phẩm sau đó trên cùng luồng đều mang nhãn sai — luồng trong
     * hồ luồng được dùng lại cho việc khác.</p>
     */
    public static void datNguon(String moTa) {
        NGUON_TAY.set(moTa);
    }

    /** Trả lại nguồn về suy-từ-ngăn-xếp. */
    public static void xoaNguon() {
        NGUON_TAY.remove();
    }

    // =====================================================================
    //  Ghi một lần nhận
    // =====================================================================

    /**
     * Ghi lại một lần nhân vật nhận được vật phẩm.
     *
     * <p><b>Không chạm CSDL</b> — chỉ xếp hàng rồi trả về ngay, nên gọi được từ
     * trong vòng lặp game.</p>
     *
     * @param player  người nhận
     * @param item    vật phẩm; số lượng lấy riêng ở {@code soLuong} vì mã kho
     *                vận đặt {@code item.quantity = 0} sau khi gộp vào ô sẵn có
     * @param soLuong số lượng <b>trước khi</b> gộp
     * @param noiNhan "Hành trang", "Rương", ...
     */
    public static void ghi(Player player, Item item, int soLuong, String noiNhan) {
        try {
            if (player == null || item == null || !item.isNotNullItem()) {
                return;
            }
            if (soLuong <= 0) {
                return;
            }
            if (ConfigDAO.num(ConfigDAO.GHI_LICH_SU_VP) != 1) {
                return;
            }
            Dong d = new Dong();
            d.nguoiId = player.id;
            d.nguoiTen = player.name == null ? "" : player.name;
            d.itemId = item.template.id;
            d.itemTen = item.template.name == null ? "" : item.template.name;
            d.soLuong = soLuong;
            d.noiNhan = noiNhan == null ? "" : noiNhan;
            d.nguonKyThuat = khungGoi();
            d.phuongThuc = tenTiengViet(d.nguonKyThuat);
            if (player.zone != null && player.zone.map != null) {
                d.mapId = player.zone.map.mapId;
                d.mapTen = player.zone.map.mapName == null ? "" : player.zone.map.mapName;
            }
            d.chiSo = chiSo(item);
            d.sucManh = player.nPoint == null ? 0 : player.nPoint.power;
            d.luc = new Timestamp(System.currentTimeMillis());

            if (!HANG_DOI.offer(d)) {
                DA_BO.incrementAndGet();
            }
        } catch (Exception ex) {
            // Nhật ký hỏng thì thôi. Tuyệt đối không được làm hỏng lượt nhận
            // vật phẩm của người chơi vì một lỗi ở chỗ ghi chép.
        }
    }

    /** Các dòng chỉ số của món, gộp thành một chuỗi đọc được. */
    private static String chiSo(Item item) {
        if (item.itemOptions == null || item.itemOptions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ItemOption io : item.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            String s = io.optionTemplate.name;
            if (s == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.replace("#", String.valueOf(io.param)));
        }
        return sb.toString();
    }

    // =====================================================================
    //  Suy nguồn từ ngăn xếp
    // =====================================================================

    /**
     * Những lớp <b>không</b> phải nguồn: chúng là đường ống, ai cũng đi qua.
     */
    private static final String[] BO_QUA = {
        "nro.repository.dao.LichSuVatPhamDAO",
        "nro.service.inventory.InventoryService",
        "nro.service.item.ItemService",
    };

    /**
     * Tên khung gọi gần nhất nằm ngoài đường ống, dạng {@code Lớp.hàm:dòng}.
     *
     * <p>Dùng {@link StackWalker} chứ không {@code new Throwable().getStackTrace()}:
     * chỗ này chạy mỗi lần nhặt đồ, mà cách cũ dựng cả ngăn xếp rồi vứt đi gần
     * hết. {@code StackWalker} đọc lười, tới {@code limit(24)} là dừng.</p>
     */
    private static String khungGoi() {
        String tay = NGUON_TAY.get();
        if (tay != null && !tay.isEmpty()) {
            return tay;
        }
        try {
            return StackWalker.getInstance().walk(s -> s
                    .limit(24)
                    .filter(f -> !laDuongOng(f.getClassName()))
                    .findFirst()
                    .map(f -> tenNgan(f.getClassName()) + "." + f.getMethodName()
                            + ":" + f.getLineNumber())
                    .orElse(""));
        } catch (Exception ex) {
            return "";
        }
    }

    private static boolean laDuongOng(String lop) {
        for (String s : BO_QUA) {
            if (s.equals(lop)) {
                return true;
            }
        }
        return false;
    }

    private static String tenNgan(String lop) {
        int k = lop.lastIndexOf('.');
        String s = k < 0 ? lop : lop.substring(k + 1);
        // Lớp trong (Zone$1) và lambda ($$Lambda) đọc rối, cắt về lớp ngoài.
        int d = s.indexOf('$');
        return d > 0 ? s.substring(0, d) : s;
    }

    /**
     * Tên tiếng Việt của phương thức nhận, tra theo tên lớp gọi.
     *
     * <p>Khớp <b>một phần</b>, xét theo thứ tự khai báo — nên mục nào hẹp hơn
     * phải đứng trước. Không có trong bảng thì <b>trả về nguyên tên lớp</b>:
     * thà đọc thấy {@code "MagicTree"} còn hơn đọc một cái tên bịa cho tròn
     * câu.</p>
     */
    private static final Map<String, String> TEN_QUEN = new LinkedHashMap<>();

    static {
        TEN_QUEN.put("ItemMapService", "Nhặt dưới đất");
        TEN_QUEN.put("ItemMap", "Nhặt dưới đất");
        TEN_QUEN.put("Boss", "Boss rơi");
        TEN_QUEN.put("Mob", "Quái rơi");
        TEN_QUEN.put("Shop", "Mua ở cửa hàng");
        TEN_QUEN.put("GiftCode", "Giftcode");
        TEN_QUEN.put("Giftcode", "Giftcode");
        TEN_QUEN.put("Task", "Nhiệm vụ");
        TEN_QUEN.put("NhiemVu", "Nhiệm vụ");
        TEN_QUEN.put("NangCap", "Nâng cấp");
        TEN_QUEN.put("PhanRa", "Phân rã");
        TEN_QUEN.put("Combine", "Ghép / Chế tạo");
        TEN_QUEN.put("Nhap", "Ghép / Chế tạo");
        TEN_QUEN.put("CheTao", "Ghép / Chế tạo");
        TEN_QUEN.put("Transaction", "Giao dịch");
        TEN_QUEN.put("Trade", "Giao dịch");
        TEN_QUEN.put("Mail", "Thư");
        TEN_QUEN.put("Clan", "Bang hội");
        TEN_QUEN.put("SuKien", "Sự kiện");
        TEN_QUEN.put("Event", "Sự kiện");
        TEN_QUEN.put("CauCa", "Câu cá");
        TEN_QUEN.put("Fishing", "Câu cá");
        TEN_QUEN.put("MagicTree", "Cây đậu thần");
        TEN_QUEN.put("Panel", "Admin cấp qua panel");
        TEN_QUEN.put("Admin", "Admin cấp");
        TEN_QUEN.put("Command", "Lệnh admin");
        TEN_QUEN.put("PhucLoi", "Phúc lợi");
        TEN_QUEN.put("Capsule", "Mở capsule");
        TEN_QUEN.put("MiniGame", "Minigame");
        TEN_QUEN.put("TaiXiu", "Tài xỉu");
        TEN_QUEN.put("Rank", "Phần thưởng xếp hạng");
        TEN_QUEN.put("DauTruong", "Đấu trường");
        TEN_QUEN.put("Pvp", "Đấu trường");
        TEN_QUEN.put("UseItem", "Dùng vật phẩm");
        TEN_QUEN.put("Npc", "NPC");
    }

    /** Chuyển {@code "BaHatMit.muaBua:120"} thành tên đọc được. */
    public static String tenTiengViet(String nguon) {
        if (nguon == null || nguon.isEmpty()) {
            return "Không rõ";
        }
        int k = nguon.indexOf('.');
        String lop = k > 0 ? nguon.substring(0, k) : nguon;
        for (Map.Entry<String, String> e : TEN_QUEN.entrySet()) {
            if (lop.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return lop;
    }

    // =====================================================================
    //  Tra cứu cho panel
    // =====================================================================

    /** Điều kiện lọc của cửa sổ "Lịch sử nhận vật phẩm". */
    public static final class Loc {

        /** Chỉ một nhân vật; {@code 0} là mọi nhân vật. */
        public long nguoiId;
        /** Khớp một phần tên, hoặc đúng id vật phẩm; để trống là không lọc. */
        public String vatPham;
        /** Khớp một phần phương thức nhận; để trống là không lọc. */
        public String phuongThuc;
        /** Mốc đầu, có thể {@code null}. */
        public Timestamp tu;
        /** Mốc cuối, có thể {@code null}. */
        public Timestamp den;
        public int gioiHan = 1000;
    }

    /**
     * Tra nhật ký theo bộ lọc.
     *
     * <p>Lọc <b>trong câu SQL</b> chứ không kéo hết về rồi bỏ đi bằng Java:
     * bảng này lớn nhanh nhất trong cả CSDL — mỗi lần ai đó nhặt một món là một
     * dòng.</p>
     */
    public static List<Dong> tim(Loc f) {
        damBaoBang();
        List<Dong> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, player_id, player_ten, item_id, item_ten, so_luong,"
                + " noi_nhan, phuong_thuc, nguon_ky_thuat, map_id, map_ten,"
                + " chi_so, suc_manh, luc FROM lich_su_vat_pham WHERE 1 = 1");
        List<Object> ts = new ArrayList<>();
        if (f.nguoiId > 0) {
            sql.append(" AND player_id = ?");
            ts.add(f.nguoiId);
        }
        if (coChu(f.vatPham)) {
            String q = f.vatPham.trim();
            sql.append(" AND (item_ten LIKE ? OR CAST(item_id AS CHAR) = ?)");
            ts.add("%" + q + "%");
            ts.add(q);
        }
        if (coChu(f.phuongThuc)) {
            sql.append(" AND (phuong_thuc LIKE ? OR nguon_ky_thuat LIKE ?)");
            String q = "%" + f.phuongThuc.trim() + "%";
            ts.add(q);
            ts.add(q);
        }
        if (f.tu != null) {
            sql.append(" AND luc >= ?");
            ts.add(f.tu);
        }
        if (f.den != null) {
            sql.append(" AND luc <= ?");
            ts.add(f.den);
        }
        sql.append(" ORDER BY luc DESC, id DESC LIMIT ?");
        ts.add(Math.max(1, f.gioiHan));

        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(sql.toString(), ts.toArray());
            while (rs.next()) {
                Dong d = new Dong();
                d.id = rs.getLong("id");
                d.nguoiId = rs.getLong("player_id");
                d.nguoiTen = rs.getString("player_ten");
                d.itemId = rs.getInt("item_id");
                d.itemTen = rs.getString("item_ten");
                d.soLuong = rs.getInt("so_luong");
                d.noiNhan = rs.getString("noi_nhan");
                d.phuongThuc = rs.getString("phuong_thuc");
                d.nguonKyThuat = rs.getString("nguon_ky_thuat");
                d.mapId = rs.getInt("map_id");
                d.mapTen = rs.getString("map_ten");
                d.chiSo = rs.getString("chi_so");
                d.sucManh = rs.getLong("suc_manh");
                d.luc = rs.getTimestamp("luc");
                out.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(LichSuVatPhamDAO.class, ex,
                    "Lỗi tra nhật ký nhận vật phẩm");
        } finally {
            dong(rs);
        }
        return out;
    }

    /** Một dòng của bảng "gộp theo vật phẩm". */
    public static final class Gop {

        public int itemId;
        public String itemTen = "";
        public long tongSoLuong;
        public long soLan;
        public Timestamp lanCuoi;
    }

    /**
     * Gộp theo vật phẩm: nhân vật này tổng cộng đã nhận món nào, bao nhiêu.
     *
     * <p>Gộp <b>bằng SQL</b> chứ không kéo hết dòng về rồi cộng trong Java: một
     * nhân vật chơi lâu có thể có hàng chục nghìn dòng.</p>
     */
    public static List<Gop> gopTheoVatPham(long nguoiId, int gioiHan) {
        damBaoBang();
        List<Gop> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT item_id, MAX(item_ten) ten, SUM(so_luong) tong,"
                    + " COUNT(*) lan, MAX(luc) cuoi"
                    + " FROM lich_su_vat_pham WHERE player_id = ?"
                    + " GROUP BY item_id ORDER BY tong DESC LIMIT ?",
                    nguoiId, Math.max(1, gioiHan));
            while (rs.next()) {
                Gop g = new Gop();
                g.itemId = rs.getInt("item_id");
                g.itemTen = rs.getString("ten");
                g.tongSoLuong = rs.getLong("tong");
                g.soLan = rs.getLong("lan");
                g.lanCuoi = rs.getTimestamp("cuoi");
                out.add(g);
            }
        } catch (Exception ex) {
            Logger.logException(LichSuVatPhamDAO.class, ex,
                    "Lỗi gộp nhật ký nhận vật phẩm");
        } finally {
            dong(rs);
        }
        return out;
    }

    /** Một dòng của bảng "gộp theo phương thức". */
    public static final class GopPT {

        public String phuongThuc = "";
        public long soLan;
        public long tongSoLuong;
    }

    public static List<GopPT> gopTheoPhuongThuc(long nguoiId) {
        damBaoBang();
        List<GopPT> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT phuong_thuc, COUNT(*) lan, SUM(so_luong) tong"
                    + " FROM lich_su_vat_pham WHERE player_id = ?"
                    + " GROUP BY phuong_thuc ORDER BY lan DESC",
                    nguoiId);
            while (rs.next()) {
                GopPT g = new GopPT();
                g.phuongThuc = rs.getString("phuong_thuc");
                g.soLan = rs.getLong("lan");
                g.tongSoLuong = rs.getLong("tong");
                out.add(g);
            }
        } catch (Exception ex) {
            Logger.logException(LichSuVatPhamDAO.class, ex,
                    "Lỗi gộp nhật ký theo phương thức");
        } finally {
            dong(rs);
        }
        return out;
    }

    /** Tổng số dòng của một nhân vật. */
    public static long demCua(long nguoiId) {
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT COUNT(*) c FROM lich_su_vat_pham WHERE player_id = ?",
                    nguoiId);
            if (rs.next()) {
                return rs.getLong("c");
            }
        } catch (Exception ex) {
            Logger.logException(LichSuVatPhamDAO.class, ex,
                    "Lỗi đếm nhật ký nhận vật phẩm");
        } finally {
            dong(rs);
        }
        return 0;
    }

    /**
     * Dọn nhật ký cũ. Chạy một lần lúc khởi động.
     *
     * <p>Số ngày giữ lấy từ {@code panel_config.giu_lich_su_vp_ngay}; đặt
     * {@code 0} là <b>giữ mãi</b>.</p>
     */
    public static void donCu() {
        damBaoBang();
        long soNgay = ConfigDAO.num(ConfigDAO.GIU_LICH_SU_VP_NGAY);
        if (soNgay <= 0) {
            return;
        }
        try {
            // Tính bằng long: 25 ngày đã là 2,16 tỉ mili giây — tràn int thì
            // mốc tính ra sai hoàn toàn và câu DELETE xoá nhầm.
            long moc = System.currentTimeMillis() - soNgay * 24L * 60L * 60L * 1000L;
            ConnectDB.executeUpdate("DELETE FROM lich_su_vat_pham WHERE luc < ?",
                    new Timestamp(moc));
        } catch (Exception ex) {
            Logger.logException(LichSuVatPhamDAO.class, ex,
                    "Lỗi dọn nhật ký nhận vật phẩm cũ");
        }
    }

    /** Xoá sạch nhật ký của một nhân vật. */
    public static int xoaCua(long nguoiId) {
        damBaoBang();
        try {
            return ConnectDB.executeUpdate(
                    "DELETE FROM lich_su_vat_pham WHERE player_id = ?", nguoiId);
        } catch (Exception ex) {
            Logger.logException(LichSuVatPhamDAO.class, ex,
                    "Lỗi xoá nhật ký nhận vật phẩm");
            return 0;
        }
    }

    // =====================================================================

    private static boolean coChu(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String cat(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
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
