package nro.repository.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Các <b>quy ước</b> chỉnh được từ panel, lưu ở bảng {@code panel_config}.
 *
 * <p>Bảng chỉ có ba cột — {@code k}, {@code v}, {@code note} — chứ không mỗi
 * thiết lập một cột. Lý do: thêm một quy ước mới thì chỉ cần thêm một hằng số ở
 * đây và một dòng trong bảng, không phải sửa lược đồ CSDL.</p>
 *
 * <h2>Vì sao có bộ nhớ đệm</h2>
 *
 * <p>Những giá trị này được đọc <b>trong luồng game</b> — mỗi lần người chơi bấm
 * đổi thỏi vàng hay mua vé. Truy vấn CSDL ở đó sẽ chặn luồng game. Nên đọc một
 * lần vào {@link #CACHE} rồi dùng lại; panel gọi {@link #reload()} sau khi lưu
 * nên thay đổi có hiệu lực ngay, <b>không cần khởi động lại máy chủ</b>.</p>
 *
 * <p>Thiếu bảng hoặc thiếu khoá thì trả về giá trị mặc định trong
 * {@link #DEFAULTS} — máy chủ vẫn chạy bình thường.</p>
 */
public class ConfigDAO {

    private ConfigDAO() {
    }

    // ---------------------------------------------------------------- khoá

    /**
     * Số giây chờ hồi sinh dùng chung cho <b>mọi</b> boss. {@code 0} là tắt.
     *
     * <p>Có khoá này vì boss không nằm gọn một chỗ để đặt từng con: chúng sinh
     * ra từ {@code BossManager.loadBoss()}, từ bản đồ, từ NPC, và từ danh sách
     * "boss đi kèm" của boss khác — gần 190 lớp. Đặt từng dòng trong
     * {@code boss_config} chắc chắn sót.</p>
     *
     * <p>Chỉ là <b>giá trị mặc định</b>: con nào đã khai riêng
     * {@code seconds_rest} trong {@code boss_config} thì vẫn theo số riêng của
     * nó. Muốn một con nhanh hơn hay chậm hơn thì đặt riêng cho con đó.</p>
     */
    public static final String BOSS_GIAY_HOI_SINH = "boss_giay_hoi_sinh";

    /** Số Thỏi Vàng nhận được cho mỗi 1.000 VND. */
    public static final String DOI_TV_MOI_1K = "doi_tv_moi_1k";
    /** Số VND tối thiểu mỗi lần đổi. */
    public static final String DOI_TV_MIN = "doi_tv_min";
    /** Bật/tắt chức năng đổi VND sang Thỏi Vàng. */
    public static final String DOI_TV_BAT = "doi_tv_bat";
    /** Giá vé tuần, đơn vị VND. */
    public static final String VE_TUAN_GIA = "ve_tuan_gia";
    /** Số ngày hiệu lực của vé tuần. */
    public static final String VE_TUAN_NGAY = "ve_tuan_ngay";
    /** Giá vé tháng, đơn vị VND. */
    public static final String VE_THANG_GIA = "ve_thang_gia";
    /** Số ngày hiệu lực của vé tháng. */
    public static final String VE_THANG_NGAY = "ve_thang_ngay";

    /**
     * Phiên bản gói dữ liệu gửi cho client (part, effect, image, skill…).
     *
     * <p>Client giữ bản đã tải trong bộ nhớ đệm và <b>chỉ tải lại khi con số này
     * đổi</b>. Thêm cải trang hay phụ kiện mới mà không tăng số này thì người
     * chơi vẫn dùng dữ liệu cũ — mặc vào không thấy gì, hoặc thấy sai hình.</p>
     */
    public static final String VS_DATA = "vs_data";
    /** Phiên bản bảng vật phẩm. Tăng khi thêm/sửa {@code item_template}. */
    public static final String VS_ITEM = "vs_item";

    /** Bật/tắt dòng hiện id + tên bản đồ + khu ở góc màn hình trong game. */
    public static final String HIEN_TEN_MAP = "hien_ten_map";
    /** Phiên bản bản đồ. */
    public static final String VS_MAP = "vs_map";
    /** Phiên bản kỹ năng. */
    public static final String VS_SKILL = "vs_skill";

    /**
     * Cho đăng ký tài khoản ngay ở màn đăng nhập.
     *
     * <p>Bật thì gõ một tên chưa có là tài khoản được tạo luôn. Tắt thì báo sai
     * tài khoản/mật khẩu như cũ.</p>
     */
    public static final String DANG_KY_TU_DONG = "dang_ky_tu_dong";

    /**
     * Chế độ bảo trì.
     *
     * <p>Bật thì <b>chỉ tài khoản quản trị</b> vào được game; ai khác đăng nhập
     * sẽ nhận một câu báo rồi bị từ chối. Dùng khi cần sửa dữ liệu hay thử tính
     * năng mới mà không muốn đuổi hết người đang chơi bằng cách tắt máy chủ.</p>
     *
     * <p>Nằm ở {@code panel_config} nên bật/tắt được ngay từ panel và
     * <b>không mất khi máy chủ dựng lại</b>.</p>
     */
    public static final String BAO_TRI = "bao_tri";

    /** Vàng của nhân vật mới tạo. */
    public static final String NV_MOI_VANG = "nv_moi_vang";
    /** Ngọc xanh của nhân vật mới tạo. */
    public static final String NV_MOI_NGOC = "nv_moi_ngoc";
    /** Hồng ngọc của nhân vật mới tạo. */
    public static final String NV_MOI_HONG_NGOC = "nv_moi_hong_ngoc";

    /**
     * Giá <b>vàng</b> của gói "Full bùa vĩnh viễn" ở Bà Hạt Mít.
     *
     * <p>Mặc định {@code 500.000.000}. Đổi giá thì thêm dòng
     * {@code bua_vv_gia_vang} vào bảng {@code panel_config} — không phải biên
     * dịch lại. Đặt {@code 0} là cho không.</p>
     *
     * <p>Mua một lần, bảy lá bùa cơ bản hết hạn vào năm 2100. Người đã có đủ
     * bảy lá vĩnh viễn thì Bà Hạt Mít từ chối bán tiếp, nên không có chuyện
     * trả tiền hai lần cho cùng một thứ.</p>
     */
    public static final String BUA_VV_GIA_VANG = "bua_vv_gia_vang";

    /**
     * Hệ số tiềm năng (kinh nghiệm), thang <b>1–100 lần</b>.
     *
     * <p>{@code 1} là y như gốc, {@code 10} là gấp mười. Nhận số thập phân nên
     * {@code 0.5} là một nửa. Nhân vào <b>sau</b> mọi phép tính khác nên không
     * phá công thức gốc.</p>
     *
     * <p>Là quy ước dạng <b>chữ</b> vì cần số thập phân — {@link #num} chỉ trả
     * số nguyên.</p>
     */
    public static final String TL_EXP = "tl_exp";

    /** Hệ số vàng rơi từ quái, thang 1–100 lần. {@code 1} là y như gốc. */
    public static final String TL_VANG = "tl_vang";

    /**
     * Hệ số cơ hội rơi vật phẩm từ quái, thang 1–100 lần.
     *
     * <p>Nhân vào <b>cơ hội</b> rơi chứ không nhân số lượng: {@code 2} nghĩa là
     * dễ rơi gấp đôi, không phải rơi gấp đôi số món.</p>
     */

    /** Bật rơi đồ set kích hoạt (vải thô) từ quái. */
    public static final String SKH_BAT = "skh_bat";

    /**
     * Số ngày kể từ khi tạo tài khoản còn nhận được đồ set kích hoạt.
     *
     * <p>{@code 0} là <b>không giới hạn</b> — ai cũng nhận được, mãi mãi.</p>
     *
     * <p>Trước đây con số này gõ cứng bằng <b>1</b> trong {@code GodGK}, và chỉ
     * nới ra được bằng vật phẩm gia hạn (1784, 1798) cộng vào cột
     * {@code account.accountAgeDays}. Nay đặt trên panel; vật phẩm gia hạn vẫn
     * cộng thêm vào số ngày này như cũ.</p>
     */
    public static final String SKH_SO_NGAY = "skh_so_ngay";

    /**
     * Tỉ lệ giết một con quái thì rơi một món đồ set kích hoạt, tính bằng
     * <b>phần trăm</b>.
     *
     * <p>{@code 1} là 1% — trung bình 100 con quái rơi một món. Nhận số thập
     * phân cho ai cần hiếm hơn; {@code 0} là tắt hẳn.</p>
     *
     * <p>Là quy ước dạng <b>chữ</b> vì cần số thập phân — {@link #num} chỉ trả
     * số nguyên.</p>
     */
    public static final String SKH_TILE = "skh_tile";

    /**
     * Các bản đồ cho rơi đồ set kích hoạt, id ngăn nhau bằng dấu phẩy.
     *
     * <p>Để trống thì giữ luật cũ trong mã nguồn: chỉ map "up SKH" và map riêng
     * tư mới rơi.</p>
     */
    public static final String SKH_MAP = "skh_map";

    /**
     * Loại ({@code type}) của các dòng chữ mô tả set in trên món đồ.
     *
     * <h3>Vì sao phải để đổi được</h3>
     *
     * <p>Màu chữ do <b>client</b> quyết định, trong hàm
     * {@code ItemOption.getOptiongColor()} đã biên dịch sẵn. Client chỉ nhận ba
     * thứ về một chỉ số: {@code id}, {@code name} và {@code type} — nên
     * {@code type} là <b>đòn duy nhất</b> server có để đổi màu dòng chữ.</p>
     *
     * <p>Không đọc được mã client nên không biết giá trị nào ra màu nào. Các
     * loại game đang dùng: 0, 1, 2, 4, 5, 6, 7, 9, 10. Đổi số ở đây rồi bấm
     * "Áp kiểu dòng mô tả" bên tab Set kích hoạt là thấy ngay, không cần build
     * lại máy chủ.</p>
     */
    public static final String SKH_MOTA_TYPE = "skh_mota_type";

    /**
     * Mã màu của các dòng chữ mô tả set in trên món đồ.
     *
     * <h3>Màu nằm ở tiền tố trong TÊN chỉ số</h3>
     *
     * <p>Client đọc mã màu dạng {@code |n|} ở đầu chuỗi — xem
     * {@link nro.core.consts.ConstFont}. Đây mới là thứ quyết định màu chữ,
     * không phải cột {@code type} như tôi tưởng lúc đầu.</p>
     *
     * <p>Bảng mã: {@code 0} đen đậm, {@code 1} xanh lá đậm, {@code 2} xanh dương
     * đậm, {@code 3} đỏ, {@code 4} xanh lá, {@code 5} xanh dương, {@code 7} đỏ
     * đậm, {@code 8} vàng đậm. Để {@code -1} thì không thêm tiền tố nào — chữ
     * theo màu mặc định của khung (xanh lá).</p>
     */

    /** Gắn sao pha lê vào món đồ set kích hoạt rơi ra (1 = bật, 0 = tắt). */
    public static final String SKH_SAO_BAT = "skh_sao_bat";

    /**
     * Tỉ lệ món đồ set kích hoạt rơi ra <b>có</b> sao pha lê, tính phần trăm.
     *
     * <p>Đây là tỉ lệ có sao <i>hay không</i>. Có rồi thì số sao bốc đều trong
     * khoảng {@link #SKH_SAO_MIN}–{@link #SKH_SAO_MAX}.</p>
     *
     * <p>Tách hai bậc như vậy vì chuỗi {@code else if} cũ nhân dồn xác suất:
     * 2 sao chỉ ra khi đã trượt 1 sao, nên con số ghi trong code không phải tỉ
     * lệ thật. Giờ tỉ lệ có sao là một số đọc thẳng, còn số sao rải đều.</p>
     */
    public static final String SKH_SAO_TILE = "skh_sao_tile";

    /** Số sao pha lê ít nhất khi món đồ set kích hoạt có sao. */
    public static final String SKH_SAO_MIN = "skh_sao_min";

    /** Số sao pha lê nhiều nhất khi món đồ set kích hoạt có sao. */
    public static final String SKH_SAO_MAX = "skh_sao_max";

    /** Giãn cách tối thiểu giữa hai lần đổi khu, tính bằng giây. */
    public static final String DOI_KHU_GIAY = "doi_khu_giay";




    /** Luong vang IT NHAT moi lan quai roi vang. 0 = khong dat san. */
    public static final String VANG_ROI_MIN = "vang_roi_min";

    /** Luong vang NHIEU NHAT moi lan quai roi vang. 0 = dung tran cu 300.000 x he so. */
    public static final String VANG_ROI_MAX = "vang_roi_max";

    /** Thoi gian phai cho truoc khi dang nhap lai, tinh bang giay. 0 la tat. */
    public static final String DANG_NHAP_CHO_GIAY = "dang_nhap_cho_giay";

    /** Gian cach toi thieu giua hai lan giao dich, tinh bang giay. 0 la tat. */
    public static final String GIAO_DICH_CHO_GIAY = "giao_dich_cho_giay";

    /**
     * Số ngày giữ nhật ký giao dịch giữa người chơi.
     *
     * <p>Máy chủ dọn nhật ký cũ hơn số ngày này <b>mỗi lần khởi động</b>. Trước
     * đây con số 3 viết cứng trong {@code HistoryTransactionDAO}, nên tab Lịch Sử
     * Giao Dịch không bao giờ xem được quá ba ngày mà không ai biết vì sao.</p>
     *
     * <p>Đặt {@code 0} để <b>giữ mãi</b> — nhật ký này là bằng chứng khi nghi ngờ
     * lỗi nhân đồ, mất là mất dấu vết điều tra.</p>
     */
    public static final String GIU_LICH_SU_GD_NGAY = "giu_lich_su_gd_ngay";

    /**
     * Mở panel quản trị khi có người nhập đúng mật khẩu trong game.
     *
     * <p>Panel nằm <b>trên máy chủ</b>. Người chơi ở máy khác nhập đúng mật khẩu
     * thì cửa sổ bật lên ở máy chạy server chứ không phải máy họ.</p>
     */
    public static final String MO_PANEL_KHI_DUNG_PASS = "mo_panel_khi_dung_pass";

    /**
     * Mật khẩu Quyền Điều Hành Hệ Thống.
     *
     * <p>Trước đây viết cứng {@code "190823"} ngay trong {@code Input.java}: đổi
     * mật khẩu phải biên dịch lại máy chủ, nên trên thực tế không ai đổi, và ai
     * đọc mã nguồn cũng thấy.</p>
     */
    public static final String MAT_KHAU_ADMIN = "mat_khau_admin";

    /** Dòng chữ hiện ra khi người chơi vừa vào game. Để trống là không hiện. */
    public static final String LOI_CHAO = "loi_chao_vao_game";

    /** Nội dung hộp "Sự kiện" — nút trên HUD. Xuống dòng bằng 
. */
    public static final String ND_SU_KIEN = "noi_dung_su_kien";

    /** Nội dung hộp "Phúc lợi" — nút trên HUD. Xuống dòng bằng 
. */
    public static final String ND_PHUC_LOI = "noi_dung_phuc_loi";

    /**
     * Giá trị dùng khi CSDL chưa có khoá đó.
     *
     * <p>Đúng bằng các con số vốn được viết cứng trong {@code OngGohan} trước
     * khi tách ra đây, nên bỏ bảng đi thì hành vi vẫn y như cũ.</p>
     */
    private static final Map<String, Long> DEFAULTS = new LinkedHashMap<>();

    static {
        DEFAULTS.put(BOSS_GIAY_HOI_SINH, 0L);
        DEFAULTS.put(DOI_TV_MOI_1K, 5L);
        DEFAULTS.put(DOI_TV_MIN, 1000L);
        DEFAULTS.put(DOI_TV_BAT, 1L);
        DEFAULTS.put(VE_TUAN_GIA, 30_000L);
        DEFAULTS.put(VE_TUAN_NGAY, 7L);
        DEFAULTS.put(VE_THANG_GIA, 100_000L);
        DEFAULTS.put(VE_THANG_NGAY, 30L);
        DEFAULTS.put(VS_DATA, 9L);
        DEFAULTS.put(VS_ITEM, 5L);
        DEFAULTS.put(HIEN_TEN_MAP, 0L);
        DEFAULTS.put(VS_MAP, 2L);
        DEFAULTS.put(SKH_SO_NGAY, 0L);
        DEFAULTS.put(VS_SKILL, 1L);
        DEFAULTS.put(DANG_KY_TU_DONG, 1L);
        DEFAULTS.put(BAO_TRI, 0L);
        DEFAULTS.put(NV_MOI_VANG, 2000L);
        DEFAULTS.put(NV_MOI_NGOC, 0L);
        DEFAULTS.put(NV_MOI_HONG_NGOC, 0L);
        DEFAULTS.put(DOI_KHU_GIAY, 8L);
        DEFAULTS.put(VANG_ROI_MIN, 0L);
        DEFAULTS.put(VANG_ROI_MAX, 0L);
        DEFAULTS.put(DANG_NHAP_CHO_GIAY, 10L);
        DEFAULTS.put(GIAO_DICH_CHO_GIAY, 30L);
        DEFAULTS.put(MO_PANEL_KHI_DUNG_PASS, 1L);
        DEFAULTS.put(SKH_BAT, 0L);
        DEFAULTS.put(SKH_MOTA_TYPE, 0L);
        DEFAULTS.put(SKH_SAO_BAT, 0L);
        DEFAULTS.put(SKH_SAO_MIN, 1L);
        DEFAULTS.put(SKH_SAO_MAX, 2L);
        DEFAULTS.put(GIU_LICH_SU_GD_NGAY, 30L);
    }

    private static final Map<String, String> CACHE = new LinkedHashMap<>();
    private static volatile boolean loaded;

    /** Mô tả của từng khoá, để panel hiện cho người dùng. */
    public static String note(String key) {
        return NOTES.getOrDefault(key, "");
    }

    private static final Map<String, String> NOTES = new LinkedHashMap<>();

    static {
        NOTES.put(BAO_TRI, "Chế độ bảo trì: bật thì CHỈ tài khoản quản trị vào được game, người chơi thường bị từ chối ở màn đăng nhập (1 = bật, 0 = tắt)");
        NOTES.put(BOSS_GIAY_HOI_SINH, "Giây chờ hồi sinh dùng chung cho MỌI boss (3600 = 1 tiếng, 0 = tắt, mỗi boss giữ số gốc). Boss nào khai riêng seconds_rest ở tab Boss thì vẫn theo số riêng.");
        NOTES.put(DOI_TV_MOI_1K, "Số Thỏi Vàng nhận được cho mỗi 1.000 VND");
        NOTES.put(DOI_TV_MIN, "Số VND tối thiểu mỗi lần đổi");
        NOTES.put(DOI_TV_BAT, "Bật chức năng đổi VND sang Thỏi Vàng (1 = bật, 0 = tắt)");
        NOTES.put(VE_TUAN_GIA, "Giá vé tuần (VND)");
        NOTES.put(VE_TUAN_NGAY, "Số ngày hiệu lực của vé tuần");
        NOTES.put(VE_THANG_GIA, "Giá vé tháng (VND)");
        NOTES.put(VE_THANG_NGAY, "Số ngày hiệu lực của vé tháng");
        NOTES.put(VS_DATA, "Phiên bản gói dữ liệu (part, hiệu ứng, ảnh) — TĂNG khi thêm cải trang / phụ kiện");
        NOTES.put(VS_ITEM, "Phiên bản bảng vật phẩm — TĂNG khi thêm/sửa item_template");
        NOTES.put(HIEN_TEN_MAP, "MẶC ĐỊNH TẮT — dòng này dùng slot 8 của gói 65 mà client chỉ biết slot 0..7, bật lên là client hỏng (không đánh được quái bằng phím Enter / nháy đúp). Chỉ bật nếu đã vá client.");
        NOTES.put(VS_MAP, "Phiên bản bản đồ");
        NOTES.put(VS_SKILL, "Phiên bản kỹ năng");
        NOTES.put(DANG_KY_TU_DONG, "Cho đăng ký tài khoản ngay ở màn login (1 = bật, 0 = tắt)");
        NOTES.put(NV_MOI_VANG, "Vàng của nhân vật mới tạo");
        NOTES.put(NV_MOI_NGOC, "Ngọc xanh của nhân vật mới tạo");
        NOTES.put(NV_MOI_HONG_NGOC, "Hồng ngọc của nhân vật mới tạo");
        NOTES.put(DOI_KHU_GIAY, "Giãn cách giữa hai lần đổi khu (giây) — 0 là tắt");
        NOTES.put(VANG_ROI_MIN,
                "Vàng rơi ÍT NHẤT mỗi lần. Đặt CẢ hai ô min+max thì vàng bốc đều trong khoảng đó, bỏ qua công thức theo máu quái.");
        NOTES.put(VANG_ROI_MAX,
                "Vàng rơi NHIỀU NHẤT mỗi lần. Chỉ đặt một ô thì nó là sàn/trần áp lên công thức; đặt cả hai thì thành khoảng cố định.");
        NOTES.put(DANG_NHAP_CHO_GIAY,
                "Thời gian phải chờ trước khi đăng nhập lại (giây) — 0 là tắt");
        NOTES.put(GIAO_DICH_CHO_GIAY,
                "Giãn cách giữa hai lần giao dịch (giây) — 0 là tắt");
        NOTES.put(MO_PANEL_KHI_DUNG_PASS,
                "Nhập đúng mật khẩu quản trị trong game thì mở panel (1 = bật, 0 = tắt)");
        NOTES.put(MAT_KHAU_ADMIN, "Mật khẩu Quyền Điều Hành Hệ Thống (gõ ở NPC trong game)");
        NOTES.put(LOI_CHAO, "Lời chào hiện ra khi vừa vào game — để trống là không hiện gì");
        NOTES.put(ND_SU_KIEN, "Nội dung nút Sự kiện trên màn hình — xuống dòng bằng \n");
        NOTES.put(ND_PHUC_LOI, "Nội dung nút Phúc lợi trên màn hình — xuống dòng bằng \n");
        NOTES.put(TL_EXP, "Hệ số tiềm năng — 1 là như gốc, 10 là gấp mười");
        NOTES.put(TL_VANG, "Hệ số vàng rơi từ quái — 1 là như gốc");
        NOTES.put(SKH_TILE, "Tỉ lệ giết một quái rơi một món đồ set kích hoạt (%). "
                + "RIÊNG cho đồ SKH — không dính gì tới tỉ lệ của đồ thường "
                + "(tab \"Đồ rơi từ quái\") hay đồ sự kiện (tab \"Sự kiện\")");
        NOTES.put(SKH_BAT, "Bật rơi đồ set kích hoạt (vải thô) từ quái (1 = bật, 0 = tắt)");
        NOTES.put(SKH_SO_NGAY, "Số ngày kể từ khi tạo tài khoản còn nhận được set kích hoạt — 0 là không giới hạn");
        NOTES.put(SKH_MOTA_TYPE, "Loại (type) của dòng chữ mô tả set. Game dùng 0, 1, "
                + "2, 4, 5, 6, 7, 9, 10 — không đổi màu chữ, màu xem khoá skh_mota_mau");
        NOTES.put(SKH_SAO_BAT, "Gắn sao pha lê vào đồ set kích hoạt rơi ra (1 = bật, 0 = tắt)");
        NOTES.put(SKH_SAO_TILE, "Tỉ lệ món đồ set kích hoạt rơi ra có sao pha lê (%)");
        NOTES.put(SKH_SAO_MIN, "Số sao pha lê ít nhất");
        NOTES.put(SKH_SAO_MAX, "Số sao pha lê nhiều nhất");
        NOTES.put(GIU_LICH_SU_GD_NGAY,
                "Số ngày giữ nhật ký giao dịch giữa người chơi — 0 là giữ mãi");
    }

    /**
     * Quy ước có giá trị là <b>chữ</b>, không phải số.
     *
     * <p>Tách riêng khỏi {@link #DEFAULTS} vì {@link #num} phải trả về số; để
     * chung thì một khoá chữ sẽ luôn rơi về 0 mà không ai biết.</p>
     */
    private static final Map<String, String> DEFAULTS_CHUOI = new LinkedHashMap<>();

    static {
        DEFAULTS_CHUOI.put(MAT_KHAU_ADMIN, "190823");
        DEFAULTS_CHUOI.put(LOI_CHAO,
                "Chúc anh em chơi game vui vẻ !!");
        DEFAULTS_CHUOI.put(ND_SU_KIEN,
                "|7|SỰ KIỆN\n|0|Chưa có sự kiện nào đang diễn ra.\n"
                + "|0|Sửa nội dung ở panel: Cấu hình hệ thống -> Quy ước -> noi_dung_su_kien");
        DEFAULTS_CHUOI.put(ND_PHUC_LOI,
                "|7|PHÚC LỢI\n|0|Chưa có phúc lợi nào.\n"
                + "|0|Sửa nội dung ở panel: Cấu hình hệ thống -> Quy ước -> noi_dung_phuc_loi");
        DEFAULTS_CHUOI.put(SKH_MAP, "");
        DEFAULTS_CHUOI.put(SKH_TILE, "1");
        DEFAULTS_CHUOI.put(SKH_SAO_TILE, "10");
        DEFAULTS_CHUOI.put(TL_EXP, "1");
        DEFAULTS_CHUOI.put(TL_VANG, "1");
    }

    /**
     * Các quy ước <b>chỉ hiện ở tab "Tỉ lệ"</b>, không hiện ở tab "Quy ước".
     *
     * <p>Tab Quy ước liệt kê theo tên khoá trong CSDL — tiện sửa nhanh nhưng
     * không nói được ý nghĩa. Những con số này có tab riêng giải thích rõ đơn vị,
     * để cả hai chỗ thì sửa một nơi mà nơi kia vẫn hiện số cũ, dễ tưởng là không
     * lưu được.</p>
     */
    private static final java.util.Set<String> CHI_TAB_TI_LE =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    TL_EXP, TL_VANG, SKH_BAT, SKH_TILE, SKH_MAP, SKH_SO_NGAY,
                    SKH_SAO_BAT, SKH_SAO_TILE, SKH_SAO_MIN, SKH_SAO_MAX));

    /** Tỉ lệ phần trăm của một quy ước dạng chữ, ví dụ {@code "0.05"}. */
    public static double phanTram(String key) {
        try {
            return Double.parseDouble(chuoi(key).replace(",", "."));
        } catch (Exception ex) {
            // Go sai -> coi nhu khong roi, con hon roi tran lan vi mot dau phay.
            return 0;
        }
    }

    /** Giá trị chữ của một quy ước. Khoá lạ hay CSDL hỏng đều rơi về mặc định. */
    public static String chuoi(String key) {
        ensureLoaded();
        String v;
        synchronized (CACHE) {
            v = CACHE.get(key);
        }
        if (v != null && !v.trim().isEmpty()) {
            return v.trim();
        }
        return DEFAULTS_CHUOI.getOrDefault(key, "");
    }

    /** {@code true} nếu khoá này là quy ước chữ. */
    public static boolean laChuoi(String key) {
        return DEFAULTS_CHUOI.containsKey(key);
    }

    /** Danh sách khoá theo đúng thứ tự muốn hiện trên panel. */
    public static String[] keys() {
        java.util.List<String> ds = new java.util.ArrayList<>(DEFAULTS.keySet());
        ds.addAll(DEFAULTS_CHUOI.keySet());
        ds.removeAll(CHI_TAB_TI_LE);
        return ds.toArray(new String[0]);
    }

    // ---------------------------------------------------------------- đọc

    /**
     * Giá trị số của một quy ước.
     *
     * <p>Không bao giờ ném lỗi: khoá lạ, giá trị không phải số, hay CSDL hỏng
     * đều rơi về mặc định. Hàm này chạy trong luồng game nên phải im lặng.</p>
     */
    /**
     * Nhu {@link #num(String)} nhung khi CSDL chua co dong nao thi tra ve
     * {@code duPhong} thay cho gia tri trong DEFAULTS.
     *
     * <p>Dung khi mot thiet lap von nam o file properties duoc chuyen ra panel:
     * chua ai chinh tren panel thi van giu dung con so cu cua file.</p>
     */
    public static long num(String key, long duPhong) {
        ensureLoaded();
        String v;
        synchronized (CACHE) {
            v = CACHE.get(key);
        }
        if (v != null) {
            try {
                return Long.parseLong(v.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return duPhong;
    }

    public static long num(String key) {
        ensureLoaded();
        String v;
        synchronized (CACHE) {
            v = CACHE.get(key);
        }
        if (v != null) {
            try {
                return Long.parseLong(v.trim());
            } catch (NumberFormatException ignored) {
                // Giá trị trong CSDL bị gõ sai -> dùng mặc định thay vì làm hỏng game.
            }
        }
        return DEFAULTS.getOrDefault(key, 0L);
    }

    /** {@code true} nếu quy ước bật/tắt đang bật. */
    public static boolean on(String key) {
        return num(key) != 0;
    }


    /**
     * Số giây người chơi phải chờ trước khi đăng nhập lại.
     *
     * <p>Trước đây lấy từ {@code server.waitlogin} trong data_base.properties —
     * sửa xong phải khởi động lại server. Nay đọc từ panel nên đổi là ăn ngay.
     * Nếu chưa có dòng trong CSDL thì dùng lại đúng giá trị của file properties,
     * nên hành vi không đổi cho tới khi admin thực sự chỉnh.</p>
     */
    public static int giayChoDangNhap() {
        return (int) num(DANG_NHAP_CHO_GIAY, nro.server.Manager.SECOND_WAIT_LOGIN);
    }

    /** Số giây phải chờ giữa hai lần giao dịch. Xem {@link #giayChoDangNhap()}. */
    public static int giayChoGiaoDich() {
        return (int) num(GIAO_DICH_CHO_GIAY);
    }

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    /** Đọc lại toàn bộ bảng. Panel gọi sau khi lưu để thay đổi có hiệu lực ngay. */
    public static void reload() {
        nro.repository.schema.LuocDoPanel.damBao();
        Map<String, String> moi = new LinkedHashMap<>();
        boolean docDuoc = false;
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT k, v FROM panel_config");
            while (rs.next()) {
                moi.put(rs.getString("k"), rs.getString("v"));
            }
            docDuoc = true;
        } catch (Exception ex) {
            Logger.logException(ConfigDAO.class, ex,
                    "Không đọc được panel_config — dùng giá trị mặc định");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception ignored) {
                }
            }
        }
        // CHỈ thay bộ đệm khi đọc THÀNH CÔNG. Đọc hỏng mà vẫn xoá bộ đệm thì
        // num() rơi về DEFAULTS, và một lần "tăng phiên bản" sau đó ghi thẳng
        // mặc-định-cộng-một vào CSDL — số phiên bản TỤT LÙI. Đã xảy ra thật:
        // MariaDB tắt giữa buổi, vs_skill 110 -> 2 và vs_item 118 -> 8, client
        // so lệch rồi xin tải mãi, treo ở màn loading, không lỗi không log.
        if (docDuoc) {
            synchronized (CACHE) {
                CACHE.clear();
                CACHE.putAll(moi);
            }
        }
        // Đặt cờ kể cả khi đọc hỏng: nếu không, mỗi lần gọi lại thử truy vấn một
        // lần nữa, và trong luồng game thì đó là chặn lặp đi lặp lại.
        loaded = true;
    }

    // ---------------------------------------------------------------- ghi

    /**
     * Lưu một quy ước.
     *
     * <p>Dùng {@code INSERT ... ON DUPLICATE KEY UPDATE} để khoá chưa có trong
     * bảng cũng lưu được, khỏi phải kiểm tra trước.</p>
     *
     * @return {@code true} nếu ghi được
     */
    public static boolean set(String key, String value) {
        try {
            ConnectDB.executeUpdate(
                    "INSERT INTO panel_config (k, v, note) VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE v = VALUES(v)",
                    key, value, note(key));
            synchronized (CACHE) {
                CACHE.put(key, value);
            }
            return true;
        } catch (Exception ex) {
            Logger.logException(ConfigDAO.class, ex, "Lỗi lưu quy ước " + key);
            return false;
        }
    }

    /**
     * Nhân một giá trị theo hệ số của một quy ước.
     *
     * <p>Hệ số {@code 1} trả về <b>đúng</b> giá trị cũ, không làm tròn đi đâu cả.
     * Hệ số {@code 0} hay gõ sai cũng trả về giá trị cũ chứ không xoá sạch —
     * "không nhân" an toàn hơn "nhân với không".</p>
     */
    public static long nhanTiLe(String key, long giaTri) {
        double h = phanTram(key);
        if (h <= 0 || h == 1) {
            return giaTri;
        }
        return Math.round(giaTri * h);
    }

    /**
     * {@code true} nếu bản đồ này nằm trong danh sách của một quy ước dạng
     * "các id ngăn nhau bằng dấu phẩy".
     *
     * <p>Danh sách rỗng trả {@code false} — chỗ gọi tự quyết định nghĩa của
     * "rỗng" (thường là giữ luật cũ).</p>
     */
    public static boolean mapNamTrong(String key, int mapId) {
        String raw = chuoi(key);
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        for (String p : raw.split(",")) {
            try {
                if (Integer.parseInt(p.trim()) == mapId) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
                // Rac trong o cau hinh -> bo qua phan tu do.
            }
        }
        return false;
    }

    /** {@code true} nếu quy ước dạng danh sách map đang để trống. */
    public static boolean danhSachMapTrong(String key) {
        String raw = chuoi(key);
        return raw == null || raw.trim().isEmpty();
    }
}
