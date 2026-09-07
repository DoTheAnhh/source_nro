-- =====================================================================
--  Trả kỹ năng "Biến Hình Super Xayda" về template id 15 (đảo lại
--  sql/bien-hinh-doi-sang-id-6.sql — người dùng không muốn dùng chung id với
--  Thái Dương Hạ San).
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template` SET `id` = 15 WHERE `nclass_id` = 2 AND `id` = 6;

UPDATE `player` SET `skills` = REPLACE(`skills`, '"[6,', '"[15,')
 WHERE `gender` = 2 AND `skills` LIKE '%"[6,%';

UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, '[6,', '[15,')
 WHERE `gender` = 2;
UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, ',6,', ',15,')
 WHERE `gender` = 2;
UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, ',6]', ',15]')
 WHERE `gender` = 2;

UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

SELECT `nclass_id`, `id`, `NAME` FROM `skill_template`
 WHERE (`nclass_id` = 2 AND `id` = 15) OR (`nclass_id` = 0 AND `id` = 6);
SELECT `NAME`, `skills_shortcut` FROM `player` WHERE `NAME` IN ('dsadd','dsds2');
