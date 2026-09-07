-- =====================================================================
--  Xoá bản mẫu sách "Sách biến hình Siêu Xayda lv1" (item 2447).
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  Đã dọn ô chứa sách này khỏi túi người chơi trước, nên không còn ai tham
--  chiếu tới nó. Xoá bản mẫu mà còn người giữ trong túi thì client không tra
--  được vật phẩm và hỏng túi.
--
--  Tăng vs_item để client tải lại bảng vật phẩm.
-- =====================================================================

SET NAMES utf8mb4;

DELETE FROM `item_template` WHERE `id` = 2447;

UPDATE `panel_config` SET `v` = '60' WHERE `k` = 'vs_item';

SELECT COUNT(*) AS `con_lai` FROM `item_template` WHERE `id` = 2447;
