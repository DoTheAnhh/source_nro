-- =====================================================================
--  Dò bản ghi bị MẤT DẤU tiếng Việt.
--
--  Dấu hiệu: chữ tiếng Việt bị đổi thành dấu hỏi ASCII. Ba điều cùng đúng
--  thì gần như chắc chắn là bản ghi hỏng:
--    1. có ký tự '?'
--    2. LENGTH = CHAR_LENGTH  (toàn ASCII, không còn chữ nhiều byte nào)
--    3. có chữ cái quanh dấu '?'  (câu hỏi thật thì '?' đứng cuối câu)
--
--  Chạy: mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
-- =====================================================================

SET NAMES utf8mb4;

SELECT 'skill_template.NAME' AS o, `nclass_id`, `id`, `NAME` AS gia_tri
  FROM `skill_template`
 WHERE `NAME` LIKE '%?%' AND LENGTH(`NAME`) = CHAR_LENGTH(`NAME`)
   AND `NAME` REGEXP '[a-zA-Z]\\?[a-zA-Z]';

SELECT 'skill_template.dam_info' AS o, `nclass_id`, `id`, `dam_info` AS gia_tri
  FROM `skill_template`
 WHERE `dam_info` LIKE '%?%' AND LENGTH(`dam_info`) = CHAR_LENGTH(`dam_info`)
   AND `dam_info` REGEXP '[a-zA-Z]\\?[a-zA-Z]';

SELECT 'item_template.NAME' AS o, `id`, `NAME` AS gia_tri
  FROM `item_template`
 WHERE `NAME` LIKE '%?%' AND LENGTH(`NAME`) = CHAR_LENGTH(`NAME`)
   AND `NAME` REGEXP '[a-zA-Z]\\?[a-zA-Z]';

SELECT 'item_template.description' AS o, `id`, `description` AS gia_tri
  FROM `item_template`
 WHERE `description` LIKE '%?%'
   AND LENGTH(`description`) = CHAR_LENGTH(`description`)
   AND `description` REGEXP '[a-zA-Z]\\?[a-zA-Z]';

SELECT 'npc_template.NAME' AS o, `id`, `NAME` AS gia_tri
  FROM `npc_template`
 WHERE `NAME` LIKE '%?%' AND LENGTH(`NAME`) = CHAR_LENGTH(`NAME`)
   AND `NAME` REGEXP '[a-zA-Z]\\?[a-zA-Z]';

SELECT 'npc_template.loi_chao' AS o, `id`, `loi_chao` AS gia_tri
  FROM `npc_template`
 WHERE `loi_chao` LIKE '%?%' AND LENGTH(`loi_chao`) = CHAR_LENGTH(`loi_chao`)
   AND `loi_chao` REGEXP '[a-zA-Z]\\?[a-zA-Z]';

SELECT 'aura.ten' AS o, `id`, `ten` AS gia_tri
  FROM `aura`
 WHERE `ten` LIKE '%?%' AND LENGTH(`ten`) = CHAR_LENGTH(`ten`)
   AND `ten` REGEXP '[a-zA-Z]\\?[a-zA-Z]';
