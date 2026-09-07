-- =====================================================================
--  Hào quang Trần Hà Linh — aura id 45
--
--  ĐÃ ĐỔI ID: tệp trong bộ res tên `aura_58_0.png`, nhưng aura 58 trong game
--  đang có thật (2 khung, 70x360 ở x1). Không ghi đè — 45 là id nhỏ nhất còn
--  trống, xét cả tệp trong data/img_by_name lẫn bảng img_by_name.
--
--  Ảnh hào quang là một DẢI DỌC gồm 4 khung vuông (rộng w, cao 4w) — đúng
--  kiểu của mọi aura có sẵn (aura_10_0 x1 = 80x320, n_frame = 4).
--  Đã dựng ở 544x2176 (x4) để mọi mức zoom chia chẵn: x1 136, x2 272, x3 408.
--
--  Hai chỗ phải khớp nhau, thiếu một là client không vẽ:
--    data/img_by_name/x1..x4/aura_45_0.png   — ảnh
--    img_by_name (name, n_frame)             — số khung, Manager nạp lúc dựng
-- =====================================================================

INSERT INTO `img_by_name` (`name`, `n_frame`) VALUES ('aura_45_0', 4)
  ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);

-- cho_cai_trang = 1: chỉ hiện khi mặc Cải Trang Trần Hà Linh, không cho tự chọn.
INSERT INTO `aura` (`id`, `ten`, `cho_cai_trang`, `ghi_chu`) VALUES
(45, 'Hào quang Trần Hà Linh', 1,
 'Bộ res đánh số 58 nhưng id đó đã có aura khác, đã dời sang 45')
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`),
  `cho_cai_trang` = VALUES(`cho_cai_trang`), `ghi_chu` = VALUES(`ghi_chu`);

-- Gán cho Cải Trang Trần Hà Linh (vật phẩm 2445)
UPDATE `item_template` SET `aura_id` = 45 WHERE `id` = 2445;
