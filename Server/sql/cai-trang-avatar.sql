-- =====================================================================
--  Avatar cho ba cải trang Naruto / Sasuke / Akaza
--
--  Bảng head_avatar map id part MŨ -> id ảnh avatar. Player.getHead() đọc
--  itemsBody.get(5).template.head (ô 5 là ô cải trang), client tra bảng này
--  ra ảnh để vẽ trong khung thông tin nhân vật.
--
--  head_id là khoá chính và Manager gửi CẢ head_id lẫn avatar_id trong
--  DataGame.sendHeadAvatar(), nên đây là tra cứu thật — KHÔNG mắc bẫy
--  "id = chỉ số" như bảng part.
--
--  Ba ảnh avatar chính là ba tệp "dư" đã cài sẵn cùng bộ res, trước đó chưa
--  part nào dùng:
--      1944 (mũ Naruto) -> 17024  (80x64 ở x1)
--      1947 (mũ Sasuke) -> 17052  (64x64)
--      1950 (mũ Akaza)  -> 17389  (64x64)
--
--  Không cần tăng vs_data/vs_item: sendHeadAvatar() nằm trong gói thông tin
--  nhân vật, gửi lại mỗi phiên. Nhưng Manager.HEAD_AVATARS chỉ nạp lúc khởi
--  động nên PHẢI khởi động lại máy chủ.
-- =====================================================================

INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES
(1944, 17024),
(1947, 17052),
(1950, 17389);
