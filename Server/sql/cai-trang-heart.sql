-- =====================================================================
--  Cải Trang Heart — id part 1968/1969/1970, vật phẩm 2442
--  Id part gốc trong part.txt là 2018/2019/2020 — đổi vì client nhận
--  diện part theo THỨ TỰ trong data/update_data/part.
--
--  Bộ res chỉ có MỘT ảnh dư (17231, 68x57 ở x1) — quá to cho ô
--  vật phẩm, nên icon túi là ảnh thu nhỏ riêng id 17333 (20x17).
-- =====================================================================

INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1968, 0, '[[17200,4,-3],[17201,6,-2],[2955,0,0]]'),
(1969, 1, '[[17202,-8,-5],[17203,-1,-9],[17204,-5,-10],[17205,0,-10],[17206,-3,-10],[17207,-5,-10],[17208,-8,-10],[17209,-5,-6],[17210,-1,-6],[17211,-4,-9],[17212,-6,-9],[17213,-3,-7],[17214,-4,-5],[17215,-1,-10],[17216,-5,-9],[17217,-10,-7],[2955,0,0]]'),
(1970, 2, '[[17218,4,4],[17219,-1,-4],[17220,-1,-4],[17221,1,-4],[17222,-1,-4],[17223,-2,-4],[17224,0,-3],[17225,-1,-1],[17226,0,0],[17227,-1,-5],[17228,-1,-2],[17229,-1,0],[17230,-1,0],[2955,0,0]]');

-- head = part type 0, body = type 1, leg = type 2
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2442, 5, 3, 'Cải Trang Heart',
  'Cải trang Hearts — kẻ huỷ diệt thần linh, ánh mắt lạnh như hư không.',
  1, 17333, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1968, 1969, 1970, 0, -1);

INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1968, 17231);

-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
