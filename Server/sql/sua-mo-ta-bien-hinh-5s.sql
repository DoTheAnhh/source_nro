-- =====================================================================
--  Sửa mô tả kỹ năng Biến Hình + sách của nó: gồng 2 giây -> 5 giây.
--
--  ⚠ PHẢI chạy bằng TỆP, không được chạy bằng `mysql -e "UPDATE …"`.
--  Trên máy này dòng lệnh không giữ được chữ có dấu: mọi ký tự tiếng Việt
--  bị đổi thành dấu hỏi ASCII (hex 3F) TRƯỚC KHI MySQL nhận được, nên bản
--  ghi vào bảng thành "G?ng 5 gi?y". Dấu hiệu nhận biết:
--      SELECT CHAR_LENGTH(x), LENGTH(x) …
--  chữ tiếng Việt đúng thì LENGTH > CHAR_LENGTH (mỗi chữ có dấu 2–3 byte);
--  bằng nhau tức là đã mất dấu.
--
--  Chạy:  mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `dam_info` = 'Gồng 5 giây rồi biến đổi: +2% HP, KI, sức đánh trong 2 phút'
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `item_template`
   SET `description` = 'Sách học kỹ năng Biến Hình cho Xayda. Gồng 5 giây rồi biến đổi, cộng 2% HP, KI, sức đánh trong 2 phút.'
 WHERE `id` = 2447;

-- Client phải tải lại bảng kỹ năng và bảng vật phẩm mới thấy mô tả mới.
-- Chia lấy dư 128 vì DataGame.vs() ép về một byte có dấu (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128
 WHERE `k` IN ('vs_skill', 'vs_item');

SELECT `id`, CHAR_LENGTH(`dam_info`) AS `so_ky_tu`,
       LENGTH(`dam_info`) AS `so_byte`, `dam_info`
  FROM `skill_template` WHERE `nclass_id` = 2 AND `id` = 15;
