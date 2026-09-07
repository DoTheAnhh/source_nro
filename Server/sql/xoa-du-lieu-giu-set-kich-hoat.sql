-- =====================================================================
--  XOÁ DỮ LIỆU CHẠY, GIỮ LẠI TOÀN BỘ CẤU HÌNH SET KÍCH HOẠT
--  Cơ sở dữ liệu: awnv3
-- =====================================================================
--
--  ĐỌC HẾT PHẦN NÀY TRƯỚC KHI CHẠY.
--
--  Tệp này xoá dữ liệu *phát sinh khi chạy* — tài khoản, nhân vật, bang,
--  giftcode, mọi nhật ký — và giữ nguyên:
--
--    1. Toàn bộ BẢNG MẪU của game (item_template, part, map_template,
--       mob_template, npc_template, shop, skill_template…). Xoá mấy bảng
--       này là game không khởi động được, nên tệp KHÔNG chạm tới.
--
--    2. Cấu hình SET KÍCH HOẠT: set_kich_hoat, set_bonus, set_config.
--
--    3. item_option_template — trong đó có các dòng chữ mô tả set (id từ
--       273 trở lên). KHÔNG được xoá: id của chỉ số chính là vị trí trong
--       mảng ITEM_OPTION_TEMPLATES, xoá một dòng là mọi id sau nó tụt
--       xuống, và đồ người chơi đang mang chỉ số đó âm thầm đổi sang chỉ
--       số khác.
--
--    4. panel_config — mọi tỉ lệ đã chỉnh (EXP, vàng, tỉ lệ rơi SKH, sao
--       pha lê…). Muốn về mặc định thì dùng nút "Về mặc định" ở tab Tỉ lệ,
--       đừng xoá bảng.
--
--  MẤT GÌ: tài khoản và nhân vật bị xoá sạch, kể cả tài khoản admin của
--  bạn. Đăng ký lại từ client sau khi chạy. Sao lưu trước:
--
--      mysqldump -u root awnv3 > awnv3-truoc-khi-xoa.sql
--
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- ---------------------------------------------------------------------
--  1. TÀI KHOẢN VÀ NHÂN VẬT
-- ---------------------------------------------------------------------
--  player.items_body / items_bag / items_box giữ đồ dạng JSON, trong đó
--  có id chỉ số. Xoá nhân vật nên không còn ai trỏ tới chỉ số nào — đây
--  cũng là thời điểm duy nhất an toàn nếu sau này muốn dọn bảng chỉ số.
DELETE FROM `player`;
DELETE FROM `account`;

-- ---------------------------------------------------------------------
--  2. BANG HỘI
-- ---------------------------------------------------------------------
DELETE FROM `clan`;

-- ---------------------------------------------------------------------
--  3. GIFTCODE
-- ---------------------------------------------------------------------
--  giftcode      : các mã đã tạo
--  giftcode_save : ai đã nhập mã nào (chống nhập lại)
DELETE FROM `giftcode`;
DELETE FROM `giftcode_save`;

-- ---------------------------------------------------------------------
--  4. NHẬT KÝ
-- ---------------------------------------------------------------------
--  history_transaction là nhật ký giao dịch giữa người chơi (tab Lịch Sử
--  Giao Dịch). Máy chủ tự dọn theo quy ước `nhat_ky_gd_ngay`, nhưng ở đây
--  xoá hết vì nhân vật cũng không còn.
DELETE FROM `history_transaction`;
DELETE FROM `history_active`;
DELETE FROM `history_bank`;
DELETE FROM `history_exchange`;
DELETE FROM `history_gold`;
DELETE FROM `history_receive_goldbar`;

-- ---------------------------------------------------------------------
--  5. CHỢ / KÝ GỬI / NẠP THẺ
-- ---------------------------------------------------------------------
--  shop_ky_gui giữ đồ người chơi treo bán. Xoá nhân vật mà để lại đây thì
--  còn những dòng trỏ tới id nhân vật không tồn tại.
DELETE FROM `shop_ky_gui`;
DELETE FROM `moc_nap`;
DELETE FROM `payments`;
DELETE FROM `vp_bank`;

-- ---------------------------------------------------------------------
--  6. WEB (cvh_*) VÀ CHẶN IP
-- ---------------------------------------------------------------------
DELETE FROM `cvh_baiviet`;
DELETE FROM `cvh_giftcode`;
DELETE FROM `cvh_history_giftcode`;
DELETE FROM `cvh_messages`;
DELETE FROM `cvh_recharge`;
DELETE FROM `cvh_sell_item`;
DELETE FROM `posts`;
DELETE FROM `blockip_list`;

-- ---------------------------------------------------------------------
--  7. PHIÊN CƯỢC (tài xỉu / chẵn lẻ)
-- ---------------------------------------------------------------------
--  pariry_* là dữ liệu từng phiên cược, gắn với id nhân vật -> xoá.
--  CÒN `tai_xiu` và `chan_le` (20 dòng mỗi bảng) là bảng phần thưởng,
--  KHÔNG xoá. Muốn xoá thì mở xem nội dung trước.
DELETE FROM `pariry_players`;
DELETE FROM `pariry_session`;

-- ---------------------------------------------------------------------
--  8. CẤU HÌNH BOSS TRÊN PANEL
-- ---------------------------------------------------------------------
--  Không thuộc set kích hoạt nên xoá theo yêu cầu "chỉ giữ set kích hoạt".
--  Bỏ hai dòng dưới nếu muốn giữ boss tự thêm và món rơi thêm.
DELETE FROM `boss_drop`;
DELETE FROM `boss_config`;

-- ---------------------------------------------------------------------
--  9. ĐỒ RƠI ĐANG NẰM TRÊN MAP
-- ---------------------------------------------------------------------
DELETE FROM `drop_item`;

-- ---------------------------------------------------------------------
--  10. ĐẶT LẠI BỘ ĐẾM TỰ TĂNG
-- ---------------------------------------------------------------------
--  Để nhân vật mới bắt đầu từ id 1 thay vì tiếp số cũ.
ALTER TABLE `player` AUTO_INCREMENT = 1;
ALTER TABLE `account` AUTO_INCREMENT = 1;
ALTER TABLE `clan` AUTO_INCREMENT = 1;
ALTER TABLE `giftcode` AUTO_INCREMENT = 1;
ALTER TABLE `history_transaction` AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- =====================================================================
--  KIỂM TRA SAU KHI CHẠY
-- =====================================================================
--  Ba con số đầu phải là 0, các con số sau phải GIỮ NGUYÊN như trước.

SELECT 'player'                AS bang, COUNT(*) AS con_lai FROM `player`
UNION ALL SELECT 'account',              COUNT(*) FROM `account`
UNION ALL SELECT 'clan',                 COUNT(*) FROM `clan`
UNION ALL SELECT '--- giu lai ---',      NULL
UNION ALL SELECT 'set_kich_hoat',        COUNT(*) FROM `set_kich_hoat`
UNION ALL SELECT 'set_bonus',            COUNT(*) FROM `set_bonus`
UNION ALL SELECT 'panel_config',         COUNT(*) FROM `panel_config`
UNION ALL SELECT 'item_option_template', COUNT(*) FROM `item_option_template`
UNION ALL SELECT 'item_template',        COUNT(*) FROM `item_template`
UNION ALL SELECT 'part',                 COUNT(*) FROM `part`
UNION ALL SELECT 'map_template',         COUNT(*) FROM `map_template`
UNION ALL SELECT 'mob_template',         COUNT(*) FROM `mob_template`
UNION ALL SELECT 'npc_template',         COUNT(*) FROM `npc_template`
UNION ALL SELECT 'item_shop',            COUNT(*) FROM `item_shop`
UNION ALL SELECT 'skill_template',       COUNT(*) FROM `skill_template`;

-- Các set kích hoạt còn nguyên và các dòng chữ mô tả chúng đang trỏ tới:
SELECT s.set_key, s.ten, s.hanh_tinh, s.active, s.option_ids, s.option_mo_ta,
       (SELECT COUNT(*) FROM `set_bonus` b WHERE b.set_key = s.set_key) AS so_dong
FROM `set_kich_hoat` s
ORDER BY s.set_key;
