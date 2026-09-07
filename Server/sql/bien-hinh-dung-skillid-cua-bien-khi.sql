-- =====================================================================
--  Biến Hình Super Xayda (id 15) dùng skillId 91 — skillId cấp 1 của Biến Khỉ.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  Ý TƯỞNG: client quyết định CÁCH DÙNG một kỹ năng theo con số nào? Log cho
--  thấy nó gửi `status=6` cho chiêu 13 (template id 13, skillId 91) và không
--  gửi gì cho chiêu 15 (template id 15, skillId 42). Hai con số cùng khác nhau
--  nên chưa phân định được nó khoá theo template id hay theo skillId.
--
--  Phép thử này tách bạch: giữ template id 15, đổi skillId sang 91 của Biến
--  Khỉ. Nếu client khoá theo skillId thì nó sẽ gửi status=6 và kỹ năng chạy;
--  nếu khoá theo template id thì vẫn không gửi gì.
--
--  Trước đây tôi loại 91 vì "trùng skillId trong cùng hành tinh". Lo đó là SUY
--  ĐOÁN CHƯA KIỂM: việc chọn kỹ năng đi theo `template.id` (SkillService
--  .selectSkill khớp `skill.template.id == skillId`), nên trùng skillId không
--  ảnh hưởng đến chọn. Log đã xác nhận: chọn chiêu 15 luôn thành công.
--
--  Các trường số khác giữ y Biến Khỉ cấp 1: damage 100, dx 200, dy 200,
--  max_fight 1, mana_use 10, cool_down 300000. Chỉ power_require để 1000 cho
--  nhân vật thử dùng được (Biến Khỉ để 250 triệu).
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `max_point` = 1,
       `skills` = '["{\"power_require\":1000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":91,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = '122' WHERE `k` = 'vs_skill';

SELECT `id`, `NAME`, `max_point`, `skills` FROM `skill_template`
 WHERE `nclass_id` = 2 AND `id` IN (13, 15) ORDER BY `id`;
