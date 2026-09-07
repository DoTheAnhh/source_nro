-- =====================================================================
--  Vá tiếp: chuyển ô kỹ năng ĐÃ HỌC từ id 15 sang 6.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  Lệnh trong sql/bien-hinh-doi-sang-id-6.sql dùng mẫu '["15,' — SAI. Dạng lưu
--  thật là mỗi ô một chuỗi trong ngoặc vuông:
--      ["[4,1,1787671452522,0]","[5,1,...]","[15,1,0,0]"]
--  nên mẫu đúng là '[15,'. Thanh chiêu thì đã chuyển đúng rồi.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `player` SET `skills` = REPLACE(`skills`, '"[15,', '"[6,')
 WHERE `gender` = 2 AND `skills` LIKE '%"[15,%';

SELECT `NAME`, `skills` FROM `player` WHERE `NAME` IN ('dsadd','dsds2');
