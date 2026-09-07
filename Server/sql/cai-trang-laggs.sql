-- =====================================================================
--  Cải Trang Laggs — id part 1965/1966/1967, vật phẩm 2441
--  Id part gốc trong part.txt là 2021/2022/2023 — đổi vì client nhận
--  diện part theo THỨ TỰ trong data/update_data/part.
-- =====================================================================

INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1965, 0, '[[17232,-5,-2],[17233,-9,-1],[2955,0,0]]'),
(1966, 1, '[[17234,3,0],[17235,-2,-3],[17236,1,-3],[17237,0,-2],[17238,2,-2],[17239,4,-3],[17240,4,-2],[17241,-1,-3],[17242,-2,-1],[17243,-1,-1],[17244,2,-2],[17245,2,-2],[17246,0,-2],[17247,1,-4],[17248,-1,-2],[17249,1,-3],[2955,0,0]]'),
(1967, 2, '[[17250,6,5],[17251,-1,-2],[17252,1,0],[17253,3,-1],[17254,-1,-2],[17255,1,0],[17256,1,0],[17257,0,2],[17258,-4,1],[17259,-1,-2],[17260,-1,0],[17261,-3,2],[17262,0,0],[2955,0,0]]');

-- head = part type 0, body = type 1, leg = type 2
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2441, 5, 3, 'Cải Trang Laggs',
  'Cải trang Laggs — chiến binh vũ trụ với sức mạnh không đo đếm được.',
  1, 17263, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1965, 1966, 1967, 0, -1);

INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1965, 17264);

-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
