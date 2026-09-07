-- =====================================================================
--  Cải trang Sasuke — id part 1947/1948/1949, vật phẩm 2435
--  Ảnh: data/icon/x1..x4/17025.png .. 17053.png (sinh từ data/icon/Sasuke)
--
--  part.txt gốc của bộ res này là bản dump CẢ BẢNG nr_part (100389 dòng,
--  ba dòng Sasuke lặp lại nhiều lần). Chỉ ba dòng dưới đây là mới; id gốc
--  2003/2004/2005 đã đổi thành 1947/1948/1949 vì client nhận diện part theo
--  THỨ TỰ trong data/update_data/part — xem docs/07-THEM-CAI-TRANG.md.
-- =====================================================================

-- ---- 1. Ba part: mũ (type 0), áo (type 1), quần (type 2)
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1947, 0, '[[17025,3,-8],[17026,2,-7],[2955,0,0]]'),
(1948, 1, '[[17027,-4,-7],[17028,-8,-9],[17029,-11,-11],[2955,-7,0],[2955,0,0],[2955,0,0],[2955,0,0],[17030,0,-3],[17031,-4,-8],[17032,-4,-24],[17033,-9,-38],[17034,-27,-7],[17035,-1,-13],[17036,-1,-9],[17037,-6,-8],[17038,-6,-9],[2955,0,0]]'),
(1949, 2, '[[17039,4,5],[17040,-1,-5],[17041,-6,-3],[17053,-7,-16],[17042,-12,-17],[17043,-7,-16],[17044,-9,-17],[17045,-2,-3],[17046,-2,-2],[17047,-3,-5],[17048,-38,-27],[17049,-4,0],[17050,-1,-2],[2955,0,0]]');

-- ---- 2. Vật phẩm cải trang
--  icon_id = 17051 (18x20 ở x1) — ảnh duy nhất còn lại vừa cỡ ô túi.
--  17052 (64x64 ở x1) không part nào dùng, vẫn cài để không mất res.
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2435, 5, 3, 'Cải Trang Sasuke',
  'Cải trang truyền nhân Uchiha — ánh mắt Sharingan lạnh như băng.',
  1, 17051, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1947, 1948, 1949, 0, -1);

-- ---- 3. Tăng phiên bản dữ liệu để client tải lại
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
