-- =====================================================================
--  Sửa max_point lệch số cấp của Biến Hình Super Xayda.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  LỆCH: max_point = 1 nhưng cột skills có 7 cấp. DataGame gửi cho client
--  writeByte(maxPoint) rồi writeByte(skillss.size()) — client được báo 1 cấp
--  rồi nhận 7 mục. Đó là lý do bấm icon chỉ NHÁY LIÊN TỤC mà không dùng được:
--  client không dựng nổi kỹ năng nên coi là chưa đủ điều kiện.
--
--  Ba chiêu cuối (Super Kamejoko, Ma phong ba, Cađíc) lệch 9/10 là kiểu CÓ SẴN
--  từ trước — không sửa, và KyNangDAO.luu() cần nới chốt để còn sửa được chúng
--  từ panel.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template` SET `max_point` = 7
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = '46' WHERE `k` = 'vs_skill';

SELECT `id`, `NAME`, `max_point`,
       (LENGTH(`skills`) - LENGTH(REPLACE(`skills`, '"point"', ''))) / 7 AS `so_cap`
  FROM `skill_template` WHERE `nclass_id` = 2 AND `id` = 15;
