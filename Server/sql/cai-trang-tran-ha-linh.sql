-- =====================================================================
--  Cải Trang Trần Hà Linh — id part 1975/1976/1977, vật phẩm 2445
--  Ảnh: data/icon/x1..x4/17390..17422
--
--  ĐÃ ĐỔI ID ẢNH: bộ res đánh số 10814..10846, nhưng dải đó trong game đang
--  có ảnh THẬT (128x160 ở x4, phiên bản hợp lệ). Không ai trỏ tới nhưng
--  vẫn là nội dung thật nên không ghi đè — đã dời sang 17390..17422.
--
--  Tệp part của bộ này tên "part thl.txt" và ở dạng Head:/Body:/Leg:
--  chứ không phải câu INSERT như các bộ khác.
-- =====================================================================

INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1975, 0, '[[17390,0,-11],[17391,-2,-11],[2955,0,0]]'),
(1976, 1, '[[17392,1,-8],[17393,-2,-13],[17394,-4,-12],[17395,-2,-12],[17396,-2,-11],[17397,1,-12],[17398,-1,-12],[17399,0,-17],[17400,-5,-16],[17401,-5,-13],[17402,-2,-10],[17403,-4,-14],[17404,-5,-18],[17405,3,-12],[17406,-3,-12],[17407,-1,-12],[2955,0,0]]'),
(1977, 2, '[[17408,6,-1],[17409,2,-8],[17410,-1,-7],[17411,2,-6],[17412,1,-8],[17413,4,-9],[17414,1,-6],[17415,-2,-6],[17416,-1,-6],[17417,-3,-8],[17418,-4,-9],[17419,-7,-4],[17420,-4,-7],[2955,0,0]]');

INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2445, 5, 3, 'Cải Trang Trần Hà Linh',
  'Cải trang Trần Hà Linh.',
  1, 17422, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1975, 1976, 1977, 0, -1);

INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1975, 17421);

-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
