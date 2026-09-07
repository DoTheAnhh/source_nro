-- =====================================================================
--  Cải Trang Broly — id part 1971/1972/1973, vật phẩm 2443
--  Id part gốc trong part.txt là 1790/1791/1792 — đổi vì client nhận
--  diện part theo THỨ TỰ trong data/update_data/part.
--
--  Ảnh 15362–15392 trong game vốn là ô trống 1x1 (phiên bản 255
--  = chưa có ảnh, không part nào dùng) nên nay được điền thật.
--  Bộ res còn 28744/28745 làm icon+avatar, NHƯNG hai id đó trong
--  game đang có ảnh thật (mồ côi, không ai trỏ tới) — không ghi
--  đè, đã cấp id mới 17331 (icon) và 17332 (avatar).
-- =====================================================================

INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1971, 0, '[[15362,-4,-17],[15363,-2,-15],[2955,0,0]]'),
(1972, 1, '[[15364,-8,-8],[15365,-8,-13],[15366,-7,-15],[15367,-9,-17],[15368,-4,-12],[15369,-5,-14],[15370,-6,-12],[15371,-3,-17],[15372,-4,-16],[15373,-12,-16],[15374,-4,-10],[15375,-7,-12],[15376,-3,-21],[15377,3,-13],[15378,-11,-15],[15379,-17,-12],[2955,0,0]]'),
(1973, 2, '[[15380,3,3],[15381,-4,-4],[15382,-5,-6],[15383,-12,-5],[15384,-26,-5],[15385,-13,-3],[15386,-24,-4],[15387,-1,-4],[15388,-1,-7],[15389,-3,-7],[15390,-1,-4],[15391,-3,2],[15392,0,-4],[2955,0,0]]');

-- head = part type 0, body = type 1, leg = type 2
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2443, 5, 3, 'Cải Trang Broly',
  'Cải trang Broly — cơn thịnh nộ Saiyan huyền thoại.',
  1, 17331, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1971, 1972, 1973, 0, -1);

INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1971, 17332);

-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
