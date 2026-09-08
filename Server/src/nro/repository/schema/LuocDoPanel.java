package nro.repository.schema;

import nro.core.log.Logger;
import nro.repository.ConnectDB;

/**
 * Tự tạo các bảng riêng của panel nếu CSDL chưa có.
 *
 * <h2>Vì sao cần lớp này</h2>
 *
 * <p>Sáu bảng dưới đây không có trong bản CSDL gốc của game — chúng được thêm
 * cùng với panel quản trị. Trước lớp này chúng chỉ tồn tại vì <b>có người tạo
 * bằng tay</b>, nên bất kỳ chuyện gì làm mất chúng — nhập lại bản dump cũ, dựng
 * máy chủ trên CSDL mới, hay xoá nhầm — đều làm cả mảng panel im lặng hỏng:
 * cấu hình đọc ra mặc định, boss mất phần đè chỉ số, set kích hoạt mất hết dòng,
 * mà không câu lỗi nào nói vì sao.</p>
 *
 * <p>Nay các DAO gọi {@link #damBao()} trước lần đọc đầu tiên. Bảng đã có thì
 * {@code CREATE TABLE IF NOT EXISTS} không làm gì cả — <b>không</b> đụng vào dữ
 * liệu đang có.</p>
 *
 * <h2>Chỉ tạo, không sửa</h2>
 *
 * <p>Lớp này cố tình <b>không</b> đổi cấu trúc bảng đã tồn tại. Việc thêm cột
 * mới do từng DAO tự lo bằng {@code ALTER TABLE ... ADD COLUMN IF NOT EXISTS} —
 * để mỗi thay đổi nằm cạnh đoạn mã cần tới nó.</p>
 */
public final class LuocDoPanel {

    private LuocDoPanel() {
    }

    private static volatile boolean daChay;

    private static final String[] CAC_BANG = {
        "CREATE TABLE IF NOT EXISTS `panel_config` ("
        + " `k` varchar(64) NOT NULL,"
        + " `v` varchar(255) NOT NULL,"
        + " `note` varchar(255) DEFAULT NULL,"
        + " PRIMARY KEY (`k`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `boss_config` ("
        + " `boss_id` int(11) NOT NULL,"
        + " `name` varchar(100) DEFAULT NULL,"
        + " `hp` varchar(255) DEFAULT NULL,"
        + " `dame` bigint(20) DEFAULT NULL,"
        + " `seconds_rest` int(11) DEFAULT NULL,"
        + " `map_join` varchar(255) DEFAULT NULL,"
        + " `active` tinyint(1) NOT NULL DEFAULT 1,"
        + " `note` varchar(255) DEFAULT NULL,"
        + " `chan_do_goc` tinyint(1) NOT NULL DEFAULT 0,"
        + " PRIMARY KEY (`boss_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `boss_drop` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `boss_id` int(11) NOT NULL,"
        + " `item_id` int(11) NOT NULL,"
        + " `quantity_min` int(11) NOT NULL DEFAULT 1,"
        + " `quantity_max` int(11) NOT NULL DEFAULT 1,"
        + " `rate_num` int(11) NOT NULL DEFAULT 1,"
        + " `rate_den` int(11) NOT NULL DEFAULT 100,"
        + " `options` varchar(255) DEFAULT '[]',"
        + " `active` tinyint(1) NOT NULL DEFAULT 1,"
        + " `note` varchar(255) DEFAULT NULL,"
        + " PRIMARY KEY (`id`), KEY `idx_boss` (`boss_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `set_bonus` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `set_key` varchar(40) NOT NULL,"
        + " `so_mon` int(11) NOT NULL DEFAULT 5,"
        + " `loai` varchar(24) NOT NULL,"
        + " `gia_tri` bigint(20) NOT NULL,"
        + " `active` tinyint(1) NOT NULL DEFAULT 1,"
        + " `ghi_chu` varchar(255) DEFAULT NULL,"
        + " `tham_so` int(11) NOT NULL DEFAULT 0,"
        + " PRIMARY KEY (`id`), KEY `idx_set` (`set_key`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Điểm đến của capsule. Trước đây danh sách này gõ cứng trong
        // MapService.getMapCapsule(), thêm hay bớt một chỗ là phải sửa mã rồi
        // biên dịch lại.
        //
        //  theo_hanh_tinh = 1  ->  bản đồ thật là map_id + gender, dùng cho ba
        //                          map nhà (21) và ba map thi đấu (24)
        //  dieu_kien: 'luon'      luôn hiện
        //             'luyen_tap' chỉ hiện khi levelLuyenTap > nguong
        //             'suc_manh'  chỉ hiện khi power > nguong
        "CREATE TABLE IF NOT EXISTS `capsule_map` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
        + " `map_id` int(11) NOT NULL,"
        + " `theo_hanh_tinh` tinyint(1) NOT NULL DEFAULT 0,"
        + " `dieu_kien` varchar(20) NOT NULL DEFAULT 'luon',"
        + " `nguong` bigint(20) NOT NULL DEFAULT 0,"
        + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
        + " `ghi_chu` varchar(255) DEFAULT NULL,"
        + " PRIMARY KEY (`id`), KEY `idx_thu_tu` (`thu_tu`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Hào quang gán RIÊNG cho từng nhân vật.
        //
        // Ưu tiên CAO HƠN hào quang của cải trang: đây là thứ quản trị viên
        // trao cho một người cụ thể, còn cải trang thì ai mặc cũng có. Trao
        // xong mà mặc một bộ cải trang vào là mất thì món quà thành vô nghĩa.
        //
        // Một nhân vật một dòng (khoá chính là player_id) nên gán lại là thay,
        // không đẻ ra hai dòng chọi nhau.
        "CREATE TABLE IF NOT EXISTS `player_aura` ("
        + " `player_id` bigint(20) NOT NULL,"
        + " `aura_id` int(11) NOT NULL,"
        + " `ghi_chu` varchar(255) DEFAULT NULL,"
        + " PRIMARY KEY (`player_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Điểm đến của menu "Bản đồ" trong game — dịch chuyển nhanh.
        //
        //  nhom            gom nhóm để menu chia hai tầng (Trái Đất, Namếc, ...)
        //  x, y            để -1 là máy chủ tự thả người chơi xuống mặt đất
        //  theo_hanh_tinh  bản đồ thật = map_id + gender (ba map nhà, ba map thi đấu)
        "CREATE TABLE IF NOT EXISTS `map_nhanh` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `nhom` varchar(60) NOT NULL DEFAULT '',"
        + " `ten` varchar(80) NOT NULL DEFAULT '',"
        + " `map_id` int(11) NOT NULL DEFAULT 0,"
        + " `x` int(11) NOT NULL DEFAULT -1,"
        + " `y` int(11) NOT NULL DEFAULT -1,"
        + " `theo_hanh_tinh` int(11) NOT NULL DEFAULT 0,"
        + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
        + " `bat` int(11) NOT NULL DEFAULT 1,"
        + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
        + " PRIMARY KEY (`id`), KEY `k_nhom` (`nhom`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Tên các mục trong menu NPC. Mỗi NPC một dòng cho mỗi vị trí mục.
        //
        //  ten_goc  tên NPC gửi ra trong mã nguồn — máy chủ tự ghi lại lần đầu
        //           thấy, để panel có gì mà liệt kê
        //  ten_moi  tên admin đặt; để trống nghĩa là dùng tên gốc
        "CREATE TABLE IF NOT EXISTS `npc_menu` ("
        + " `npc_id` int(11) NOT NULL,"
        + " `chi_so` int(11) NOT NULL,"
        + " `ten_goc` varchar(255) NOT NULL DEFAULT '',"
        + " `ten_moi` varchar(255) DEFAULT NULL,"
        + " PRIMARY KEY (`npc_id`, `chi_so`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Nút THÊM vào menu NPC — nối vào cuối menu gốc.
        //
        //  hanh_dong: 'mo_shop'   tham_so = tag cửa hàng, ví dụ SHOP_ANWIN
        //             'thong_bao' tham_so = câu chữ hiện lên
        "CREATE TABLE IF NOT EXISTS `npc_menu_them` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `npc_id` int(11) NOT NULL,"
        + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
        + " `ten` varchar(255) NOT NULL,"
        + " `hanh_dong` varchar(20) NOT NULL DEFAULT 'mo_shop',"
        + " `tham_so` varchar(255) NOT NULL DEFAULT '',"
        + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
        + " PRIMARY KEY (`id`), KEY `idx_npc` (`npc_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Sự kiện chạy theo thời gian. Thay cho lớp EventManager cũ vốn giữ
        // mười một cờ static boolean gõ cứng — đổi sự kiện phải sửa mã rồi
        // khởi động lại máy chủ.
        //
        //  bat_dau / ket_thuc  NULL = không chặn đầu / không chặn cuối
        //  phut_nhac           0 = chỉ loa lúc mở, không nhắc lại
        // Danh sach TEN su kien goi y trong o chon. Truoc day la mang cung
        // trong ma nguon nen muon them mot ten phai sua code va build lai.
        "CREATE TABLE IF NOT EXISTS `su_kien_ten` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `ten` varchar(100) NOT NULL,"
        + " PRIMARY KEY (`id`), UNIQUE KEY `uq_ten` (`ten`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `su_kien` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `ten` varchar(100) NOT NULL,"
        + " `bat` tinyint(1) NOT NULL DEFAULT 0,"
        + " `bat_dau` datetime DEFAULT NULL,"
        + " `ket_thuc` datetime DEFAULT NULL,"
        + " `thong_bao` varchar(255) NOT NULL DEFAULT '',"
        + " `phut_nhac` int(11) NOT NULL DEFAULT 0,"
        + " `ghi_chu` varchar(255) DEFAULT NULL,"
        + " PRIMARY KEY (`id`), KEY `idx_bat` (`bat`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Vật phẩm rơi từ MỌI quái trong lúc sự kiện chạy.
        //
        //  ti_le  phần trăm cho MỖI con quái, riêng từng món — hai món trong
        //         cùng một sự kiện đặt tỉ lệ khác nhau được
        "CREATE TABLE IF NOT EXISTS `su_kien_roi` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `su_kien_id` int(11) NOT NULL,"
        + " `item_id` int(11) NOT NULL,"
        + " `so_luong_min` int(11) NOT NULL DEFAULT 1,"
        + " `so_luong_max` int(11) NOT NULL DEFAULT 1,"
        + " `ti_le` double NOT NULL DEFAULT 1,"
        + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
        // kieu_map phai la INT chu KHONG duoc tinyint(1): trinh dieu khien
        // MySQL doc tinyint(1) ra Boolean, ma cot nay co ba gia tri 0/1/2 nen
        // doc bang getInt se nem ClassCastException — va hong ca ham doc bang,
        // tuc la khong mon nao roi nua.
        + " `kieu_map` int(11) NOT NULL DEFAULT 0,"
        + " `ds_map` varchar(500) NOT NULL DEFAULT '',"
        + " PRIMARY KEY (`id`), KEY `idx_su_kien` (`su_kien_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Công thức đổi ở Quy Lão: N món cần lấy M món nhận.
        "CREATE TABLE IF NOT EXISTS `su_kien_doi` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `su_kien_id` int(11) NOT NULL,"
        + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
        + " `ten` varchar(100) NOT NULL DEFAULT '',"
        + " `item_can` int(11) NOT NULL,"
        + " `so_luong_can` int(11) NOT NULL DEFAULT 1,"
        + " `item_nhan` int(11) NOT NULL,"
        + " `so_luong_nhan` int(11) NOT NULL DEFAULT 1,"
        + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
        + " PRIMARY KEY (`id`), KEY `idx_su_kien` (`su_kien_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Mở gói ra được gì. Khoá theo ID VẬT PHẨM GÓI chứ không theo sự kiện:
        // mùa sau dùng lại đúng cái gói đó thì khỏi khai lại nội dung, và một
        // gói bán ở cửa hàng cũng mở được mà không thuộc sự kiện nào.
        //
        //  trong_so  càng lớn càng hay ra; bốc một phần thưởng mỗi lần mở
        "CREATE TABLE IF NOT EXISTS `su_kien_hop` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `hop_item_id` int(11) NOT NULL,"
        + " `item_id` int(11) NOT NULL,"
        + " `so_luong_min` int(11) NOT NULL DEFAULT 1,"
        + " `so_luong_max` int(11) NOT NULL DEFAULT 1,"
        + " `trong_so` int(11) NOT NULL DEFAULT 1,"
        + " `bat` tinyint(1) NOT NULL DEFAULT 1,"
        + " PRIMARY KEY (`id`), KEY `idx_hop` (`hop_item_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Cấu hình Sách Tuyệt Kỹ. Chỉ một dòng — đọc dòng đầu, không có thì ghi
        // dòng mặc định.
        //
        //  so_dong_min/max        số dòng "- Chưa giám định" của một cuốn vừa ghép
        //  dung_chung_hanh_tinh   1 = đặt gender = 3 cho sáu cuốn sách, bỏ khoá
        //                         hành tinh cả ở client lẫn lúc mặc
        "CREATE TABLE IF NOT EXISTS `sach_tuyet_ky` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `so_dong_min` int(11) NOT NULL DEFAULT 1,"
        + " `so_dong_max` int(11) NOT NULL DEFAULT 4,"
        + " `dung_chung_hanh_tinh` int(11) NOT NULL DEFAULT 1,"
        + " PRIMARY KEY (`id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

        // Kho chỉ số bốc ra khi giám định một dòng của Sách Tuyệt Kỹ.
        //
        //  option_id        id trong item_option_template
        //  min_gt, max_gt   khoảng trị số bốc ngẫu nhiên, hai đầu đều tính
        //  bat              tắt là tạm rút khỏi kho mà không phải xoá dòng
        "CREATE TABLE IF NOT EXISTS `sach_tuyet_ky_chi_so` ("
        + " `id` int(11) NOT NULL AUTO_INCREMENT,"
        + " `option_id` int(11) NOT NULL,"
        + " `min_gt` int(11) NOT NULL DEFAULT 1,"
        + " `max_gt` int(11) NOT NULL DEFAULT 10,"
        + " `thu_tu` int(11) NOT NULL DEFAULT 0,"
        + " `bat` int(11) NOT NULL DEFAULT 1,"
        + " `ghi_chu` varchar(255) NOT NULL DEFAULT '',"
        + " PRIMARY KEY (`id`), KEY `k_option` (`option_id`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
    };

    /**
     * Tạo các bảng của panel nếu chưa có. Gọi lại nhiều lần không sao.
     *
     * <p>Chỉ chạy một lần cho mỗi lần khởi động — {@code daChay} chặn các lần
     * gọi sau, vì mọi DAO đều gọi hàm này trước lần đọc đầu tiên.</p>
     */
    public static void damBao() {
        if (daChay) {
            return;
        }
        daChay = true;
        for (String sql : CAC_BANG) {
            try {
                ConnectDB.executeUpdate(sql);
            } catch (Exception ex) {
                Logger.logException(LuocDoPanel.class, ex,
                        "Không tạo được bảng của panel: " + tenBang(sql));
            }
        }
    }

    /** Tên bảng trong câu lệnh, để câu log nói rõ bảng nào hỏng. */
    private static String tenBang(String sql) {
        int a = sql.indexOf('`');
        int b = a < 0 ? -1 : sql.indexOf('`', a + 1);
        return a >= 0 && b > a ? sql.substring(a + 1, b) : "(không rõ)";
    }
}
