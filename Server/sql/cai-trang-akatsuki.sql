-- =====================================================================
--  Cải Trang Akatsuki — id part 1953/1954/1955, vat pham 2437
--  Anh: data/icon/x1..x4/17135..17163 (29 tep)
--
--  Id part goc trong part.txt la 2012/2013/2014 — doi thanh 1953/1954/1955 vi
--  client nhan dien part theo THU TU trong data/update_data/part.
--  Xem docs/07-THEM-CAI-TRANG.md.
-- =====================================================================

-- ---- 1. Ba part: mu (type 0), ao (type 1), quan (type 2)
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1953, 0, '[[17135,-2,-8],[17136,-4,-10],[2955,0,0]]'),
(1954, 1, '[[17137,-4,-5],[17138,-5,-10],[17139,-12,-11],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[17140,-6,-5],[17141,-15,-7],[17142,-7,-11],[17143,-6,-10],[17144,-6,-11],[17145,-10,-12],[17146,-13,-10],[17163,-9,-8],[17147,-17,-9],[2955,0,0]]'),
(1955, 2, '[[17148,4,4],[17149,-3,-3],[17150,-5,-4],[17151,-8,-16],[17152,-12,-17],[17153,-8,-17],[17154,-10,-17],[17155,-4,-2],[17156,-3,-1],[17157,-2,-5],[17158,3,-1],[17159,-4,4],[17160,-4,-4],[2955,0,0]]');

-- ---- 2. Vat pham cai trang
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2437, 5, 3, 'Cải Trang Akatsuki',
  'Áo choàng mây đỏ của tổ chức Akatsuki — bóng tối khoác lên vai.',
  1, 17161, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1953, 1954, 1955, 0, -1);

-- ---- 3. Avatar (bang head_avatar map id part MU -> id anh)
INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1953, 17162);

-- ---- 4. Tang phien ban du lieu de client tai lai
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
