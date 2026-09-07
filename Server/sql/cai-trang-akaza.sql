-- =====================================================================
--  Cải trang Akaza — id part 1950/1951/1952, vật phẩm 2436
--  Ảnh: data/icon/x1..x4/17361..17389 (27 tệp, thiếu 17369 và 17386)
--
--  ⚠ BỘ RES THIẾU HAI ẢNH
--  part.txt gốc dùng 27 ảnh: 17361..17387. Nhưng thư mục data/icon/Akaza
--  chỉ có 17369 và 17386 KHÔNG CÓ TỆP, mà lại có thêm 17388/17389 không
--  part nào dùng (17388 = icon túi, 17389 = 64x64 không dùng — giống hệt
--  cặp ảnh dư của bộ Naruto và Sasuke, nên đây KHÔNG phải hai frame thiếu).
--
--  Trong game hiện tại, 0 trong 13338 ảnh mà part dùng bị thiếu tệp — chưa
--  bao giờ có part trỏ vào ảnh không tồn tại, nên không có bằng chứng client
--  chịu được. DataGame.sendIcon() gặp tệp null thì im lặng return, client có
--  thể chờ mãi. Vì vậy hai frame đó tạm thay bằng 2955 (ảnh trống mà chính
--  bộ res dùng cho các ô rỗng):
--      part 1951 (áo)   frame thứ 11 (index 10): 17369 -> 2955
--      part 1952 (quần) frame thứ 12 (index 11): 17386 -> 2955
--  Có tệp 17369.png / 17386.png thì cài vào 4 mức zoom, đổi hai chỗ 2955 này
--  về đúng id, tăng vs_data rồi khởi động lại là xong.
-- =====================================================================

-- ---- 1. Ba part: mũ (type 0), áo (type 1), quần (type 2)
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1950, 0, '[[17361,2,-8],[17362,2,-9],[2955,0,0]]'),
(1951, 1, '[[17363,-1,-6],[17364,-3,-8],[17365,-2,-9],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[17366,-7,-4],[17367,-8,-6],[17368,-13,-12],[2955,-22,-15],[17370,-14,-34],[17371,-2,-12],[17372,-21,-18],[17373,-15,-8],[17374,-4,-9],[2955,0,0]]'),
(1952, 2, '[[17375,1,5],[17376,-3,-5],[17377,0,-4],[17378,-3,-16],[17379,-1,-15],[17380,-4,-16],[17381,-2,-15],[17382,-2,-3],[17383,-1,-2],[17384,-2,-5],[17385,-18,-3],[2955,-8,-15],[17387,-8,-38],[2955,0,0]]');

-- ---- 2. Vật phẩm cải trang
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2436, 5, 3, 'Cải Trang Akaza',
  'Cải trang Thượng Huyền Tam — quyền pháp phá hủy, không hề biết lùi.',
  1, 17388, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1950, 1951, 1952, 0, -1);

-- ---- 3. Tăng phiên bản dữ liệu để client tải lại
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
