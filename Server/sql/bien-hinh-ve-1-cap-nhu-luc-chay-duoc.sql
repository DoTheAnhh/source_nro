-- =====================================================================
--  Biến Hình Super Xayda: về ĐÚNG cấu hình lúc client CHỌN ĐƯỢC chiêu này.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  BẰNG CHỨNG: lúc 22:20 log ghi
--      [34] dsadd XIN CHON chieu id=15 -> co, diem=1 skillId=42
--  tức client CHỌN ĐƯỢC chiêu 15. Cấu hình khi đó: 1 cấp, max_point 1,
--  skillId 42, power_require 1000.
--
--  Sau khi tôi cho 7 cấp (skillId 42..48) thì client không gửi lệnh chọn nào
--  nữa — và 42..48 là TRỌN khối của Thái Dương Hạ San bên Trái Đất, tức từ
--  MỘT id trùng thành BẢY id trùng. Client tra skillId ra kỹ năng, trùng cả
--  khối thì nó không dựng nổi thanh chiêu: bấm icon chỉ nháy.
--
--  Người dùng cũng chỉ cần 1 cấp. max_point = 1 khớp đúng 1 cấp.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `max_point` = 1,
       `skills` = '["{\"power_require\":1000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":42,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = '48' WHERE `k` = 'vs_skill';

SELECT `id`, `NAME`, `max_point`,
       (LENGTH(`skills`) - LENGTH(REPLACE(`skills`, '"point"', ''))) / 7 AS `so_cap`,
       `skills`
  FROM `skill_template` WHERE `nclass_id` = 2 AND `id` = 15;
