-- =====================================================================
--  Pet Cửu Vĩ — id part 1962/1963/1964, vật phẩm 2440
--  Ảnh: data/icon/x1..x4/16937..16944 (8 tệp)
--
--  PET DÙNG ĐÚNG CƠ CHẾ CỦA CẢI TRANG, chỉ khác hai chỗ:
--    * TYPE = 21 (ô thú cưng) thay vì 5 (ô cải trang)
--        InventoryService.putItemBody: case 21 -> ô số 7 trên người
--    * KHÔNG cần dòng head_avatar — avatar trong khung thông tin lấy từ
--      cải trang, không lấy từ pet. Kiểm chứng: cả ba pet Capybara
--      (head 1505 / 1508 / 1550) đều không có dòng nào trong head_avatar.
--
--  Vẫn dùng head/body/leg như cải trang, và part vẫn là 3 / 17 / 14 frame.
--  Phần lớn frame là 2955 (ảnh trống) vì con thú chỉ chiếm vài khung — part
--  mũ của Cửu Vĩ trống hoàn toàn, giống pet Capybara chỉ có 1 khung ở thân.
--
--  Id part gốc trong part.txt là 1989/1990/1991 — đổi thành 1962/1963/1964
--  vì client nhận diện part theo THỨ TỰ trong data/update_data/part.
--  Xem docs/07-THEM-CAI-TRANG.md.
-- =====================================================================

-- ---- 1. Ba part: mũ (trống hoàn toàn), thân, chân
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1962, 0, '[[2955,0,0],[2955,0,0],[2955,0,0]]'),
(1963, 1, '[[2955,0,0],[16937,0,-13],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]'),
(1964, 2, '[[16940,-25,-13],[16938,-30,-23],[16939,-31,-31],[16940,-34,-27],[16941,-33,-32],[16942,-32,-25],[16943,-28,-29],[16943,-34,-25],[16939,-38,-27],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]');

-- ---- 2. Vật phẩm pet
--  icon_id = 16944 — ảnh dư duy nhất, 25x21 ở x1, đúng cỡ ô túi.
--  gold = 1 giống pet Capybara (1629) để bán lại được vài vàng.
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2440, 21, 3, 'Pet Cửu Vĩ',
  'Hồ ly chín đuôi — lửa yêu khí cuộn quanh mỗi bước chân.',
  1, 16944, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1962, 1963, 1964, 0, -1);

-- ---- 3. Tăng phiên bản dữ liệu để client tải lại
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
