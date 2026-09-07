-- =====================================================================
--  Cải Trang Goku Cận Bản Năng Vô Cực — part 1978..1982, vật phẩm 2446
--  Ảnh: data/icon/x1..x4/16945..16977
--
--  BỘ NÀY CÓ BA PART MŨ (1992/1993/1994 trong tệp gốc) — ba kiểu tóc, đúng
--  kiểu 20 cải trang có sẵn dùng (khoảng cách head→body = 3).
--  Máy chủ này chỉ đọc MỘT mũ: Player.getHead() trả thẳng
--  itemsBody.get(5).template.head, KHÔNG cộng gender. Nên hai mũ còn
--  lại nằm đó dự phòng — muốn đổi kiểu tóc thì sửa cột head thành
--  1979 hoặc 1980.
--
--  Id part gốc 1992..1996 đổi thành 1978..1982 vì client nhận diện part theo
--  THỨ TỰ trong data/update_data/part.
-- =====================================================================

INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1978, 0, '[[16945,-6,-21],[16948,-6,-20],[2955,0,0]]'),
(1979, 0, '[[16946,-6,-19],[16949,-7,-20],[2955,0,0]]'),
(1980, 0, '[[16947,-6,-22],[16950,-4,-21],[2955,0,0]]'),
(1981, 1, '[[16951,-4,-7],[16952,-7,-11],[16953,-10,-13],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[16954,-5,-15],[16955,-12,-17],[16956,-6,-14],[16957,-11,-17],[16958,-12,-19],[16959,-7,-18],[16960,-9,-16],[16961,-11,-15],[16962,-8,-15],[2955,0,0]]'),
(1982, 2, '[[16963,0,5],[16964,-4,-5],[16965,-4,-6],[16966,-9,-19],[16967,-11,-17],[16968,-7,-17],[16969,-7,-17],[16970,-5,-1],[16971,-6,-13],[16972,-6,-7],[16973,-5,-5],[16974,-6,-5],[16975,-4,-8],[2955,0,0]]');

INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2446, 5, 3, 'Cải Trang Goku Cận Bản Năng Vô Cực',
  'Bản năng vô cực chưa hoàn thiện — tóc bạc, mắt bạc, đòn đánh đi trước ý nghĩ.',
  1, 16976, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1978, 1981, 1982, 0, -1);

INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1978, 16977);

-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
