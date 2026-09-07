-- =====================================================================
--  Đeo lưng Cờ Văn Hoá Đồng — part 1974, vật phẩm 2444
--
--  ĐEO LƯNG KHÁC CẢI TRANG: TYPE = 11 (ô số 8), và dùng cột `part`
--  trỏ vào MỘT part type 1 (17 khung) — không dùng head/body/leg.
--  Player.getFlagBag() trả thẳng itemsBody.get(8).template.part.
--  Mẫu đối chiếu: "Đeo lưng Kẹo Noel" (vật phẩm 1841, part 149).
--
--  Bộ res có 35 ảnh: 34 khung hoạt ảnh + 17088 đã cắt sẵn làm icon.
--  Part type 1 chỉ chứa 17 khung nên lấy CÁCH MỘT khung (17054, 17056,
--  … 17086) — giữ nguyên tốc độ và độ mượt của vòng lặp.
--
--  Ảnh gốc là khung 400x400 mà hình thật chỉ chiếm 88x88 ở giữa; đã cắt
--  hết phần trong suốt (còn 22x22 ở x1, đúng cỡ đeo lưng có sẵn ~20x15).
--  Không cắt thì client vẽ một ô 100x100 mà 78%% trong suốt, lá cờ nằm
--  lệch xa người chơi.
--
--  dx/dy để 0,0 giống khung đầu của "Ba lô ẩm thực" (part 160). Nhìn
--  trong game thấy lệch thì chỉnh hai số đó, không phải sinh lại ảnh.
-- =====================================================================

INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1974, 1, '[[17054,0,0],[17056,0,0],[17058,0,0],[17060,0,0],[17062,0,0],[17064,0,0],[17066,0,0],[17068,0,0],[17070,0,0],[17072,0,0],[17074,0,0],[17076,0,0],[17078,0,0],[17080,0,0],[17082,0,0],[17084,0,0],[17086,0,0]]');

INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2444, 11, 3, 'Đeo lưng Cờ Văn Hoá Đồng',
  'Lá cờ văn hoá hạng đồng — phất theo từng bước chân.',
  1, 17088, 1974, 0, 0, 1, 0, 0, 0, 0, 0, -1, -1, -1, 0, -1);

-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
