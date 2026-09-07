-- =====================================================================
--  Kỹ năng Biến Hình Super Xayda (Xayda, id 15): mọi trường số copy y
--  Biến Khỉ cấp 1, trừ sức mạnh yêu cầu.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  LÝ DO: `damage = 0` là trường DUY NHẤT còn lại mà chiêu 15 khác mọi kỹ năng
--  client dùng được — Tái tạo 4, Biến Khỉ 100, Thái Dương Hạ San 3000, không
--  kỹ năng nào để 0. `dx`/`dy` cũng lấy 200/200 của Biến Khỉ (Tái tạo để 0 và
--  vẫn chạy, nên trường này chưa chắc quan trọng, nhưng đang copy Biến Khỉ thì
--  copy cả).
--
--  Đối chiếu Biến Khỉ cấp 1:
--    damage 100, dx 200, dy 200, max_fight 1, mana_use 10, cool_down 300000
--  Giữ khác: power_require 1000 (Biến Khỉ để 250 triệu, nhân vật thử không đủ)
--  và skillId 42 (Biến Khỉ dùng 91, trùng trong Xayda thì client không phân
--  biệt được hai kỹ năng).
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `max_point` = 1,
       `skills` = '["{\"power_require\":1000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":42,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

SELECT `id`, `NAME`, `max_point`, `skills` FROM `skill_template`
 WHERE `nclass_id` = 2 AND `id` = 15;
