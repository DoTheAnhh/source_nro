-- =====================================================================
--  Cải trang Naruto — id part 1944/1945/1946, vật phẩm 2434
--  Ảnh: data/icon/x1..x4/16996.png .. 17024.png (sinh từ data/icon/Naruto)
--
--  VÌ SAO ID PART LÀ 1944 CHỨ KHÔNG PHẢI 2000 NHƯ part.txt GỐC:
--  Manager.loadDatabase() ghi data/update_data/part chỉ gồm
--  writeShort(số part) rồi từng part là writeByte(type) + các frame —
--  KHÔNG ghi id. Client nhận diện part theo THỨ TỰ trong tệp, nên id part
--  bắt buộc phải liên tục từ 0. Bảng đang có đúng 1944 dòng (0..1943);
--  chèn ở 2000 sẽ để lại lỗ 1944..1999 và mọi part sau đó lệch 56 chỗ.
-- =====================================================================

-- ---- 1. Ba part: mũ (type 0), áo (type 1), quần (type 2)
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1944, 0, '[[16996,-3,-9],[16997,-6,-11],[2955,0,0]]'),
(1945, 1, '[[16998,-14,-7],[16999,-8,-9],[17000,-11,-13],[2955,-13,-19],[2955,0,0],[2955,0,0],[2955,0,0],[17001,-8,-14],[17002,-4,-4],[17003,-10,-29],[17004,-13,-29],[17005,-10,-18],[17006,-12,-12],[17007,-4,-11],[17008,-17,-9],[17009,-18,-8],[2955,0,0]]'),
(1946, 2, '[[17010,6,5],[17011,-1,-2],[17012,-5,-3],[17013,-8,-19],[17014,-13,-19],[17015,-7,-18],[17016,-9,-18],[17017,-5,-2],[17018,-1,-2],[17019,-5,-6],[17020,-48,-42],[17021,-3,2],[17022,-1,-1],[2955,0,0]]');

-- ---- 2. Vật phẩm cải trang
--  TYPE = 5      : ô cải trang (giống mọi cải trang khác)
--  gender = 3    : cả ba hành tinh dùng được
--  part = -1     : cải trang không dùng cột part, dùng head/body/leg
--  isGender = -1 : không khoá theo giới tính
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2434, 5, 3, 'Cải Trang Naruto',
  'Cải trang ninja Lá — mang theo ý chí của Hokage.',
  1, 17023, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1944, 1945, 1946, 0, -1);

-- ---- 3. Tăng phiên bản dữ liệu để client tải lại
--  vs_data : gói -87 updateData, có chứa data/update_data/part
--  vs_item : bảng item_template
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
