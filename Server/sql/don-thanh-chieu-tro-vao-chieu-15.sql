-- =====================================================================
--  Dọn mọi tham chiếu tới kỹ năng 15 khỏi dữ liệu người chơi, trong lúc kỹ
--  năng đó đang tạm bị bỏ khỏi bảng.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--  ⚠ CHỈ chạy khi người chơi ĐANG OFFLINE.
--
--  VÌ SAO: thanh chiêu (skills_shortcut) là mảng 10 template id. Nếu nó trỏ
--  vào một kỹ năng KHÔNG có trong bảng thì client không tra được icon và hỏng
--  cả thanh — bấm không chuyển được ô nào. Bỏ kỹ năng khỏi bảng mà để lại
--  tham chiếu thì phép thử vô nghĩa.
--
--  Xoá cả mục đã học để danh sách gửi cho client không còn skillId lạ.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, '[15,', '[-1,');
UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, ',15,', ',-1,');
UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, ',15]', ',-1]');

UPDATE `player` SET `skills` = REGEXP_REPLACE(`skills`, ',"\[15,[^]]*\]"', '')
 WHERE `skills` REGEXP ',"\[15,';
UPDATE `player` SET `skills` = REGEXP_REPLACE(`skills`, '"\[15,[^]]*\]",', '')
 WHERE `skills` REGEXP '"\[15,';

SELECT `NAME`, `gender`, `skills_shortcut`, `skills` FROM `player`;
