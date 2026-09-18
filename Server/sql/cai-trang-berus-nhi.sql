-- =====================================================================
--  Cải Trang Berus Nhí — vật phẩm 2452, part 1422/1423/1424
--  Ảnh: data/icon/x1..x4/12793..12825 (icon 12825, avatar 12824)
--
--  Lấy từ NRO NGOL: ở đó bộ này là part 1612/1613/1614, head_avatar
--  1612 -> 12824, nhưng NGOL không có vật phẩm nào dùng tới.
--
--  Máy chủ này ĐÃ có sẵn cả ba part (id 1422/1423/1424, dữ liệu trùng
--  khớp từng khung) lẫn head_avatar 1422 -> 12824, và ảnh 12793..12825
--  đã đủ bốn mức phóng, bảng smallimage_version cũng khớp. Nên không
--  thêm part hay ảnh nào — chỉ thêm vật phẩm trỏ vào bộ có sẵn.
--  Thêm part mới trùng dữ liệu sẽ chỉ làm lệch thứ tự part của client.
-- =====================================================================

-- head = part type 0, body = type 1, leg = type 2
INSERT IGNORE INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2452, 5, 3, 'Cải Trang Berus Nhí',
  'Thần Hủy Diệt thu nhỏ — tai to, mặt búng ra sữa, cơn giận vẫn nguyên cỡ.',
  1, 12825, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1422, 1423, 1424, 0, -1);

INSERT IGNORE INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1422, 12824);

-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
