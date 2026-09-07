-- =====================================================================
--  PHÉP THỬ: tạm bỏ kỹ năng 15 khỏi bảng, để biết nó có phải thứ làm hỏng
--  thanh chiêu của CẢ BA hành tinh hay không.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  GIẢ THUYẾT: Trái Đất 9 kỹ năng, Namếc 9, Xayda 10 (9 gốc + kỹ năng mới).
--  Client có thể có mảng cố định 9 mục mỗi hành tinh; mục thứ 10 làm tràn và
--  hỏng cả danh sách. Xayda là hành tinh gửi CUỐI trong gói dữ liệu nên lệch
--  pha ở đó làm hỏng phần đuôi — khớp với triệu chứng cả ba hành tinh đều
--  không chuyển được chiêu.
--
--  Client này đầy giả định bảng cố định: bảng phiên bản ảnh 32767 byte, id
--  part phải liền mạch, template id phải nằm trong tập gõ cứng.
--
--  PHỤC HỒI: chạy sql/phuc-hoi-chieu-15.sql (đã lưu nguyên vẹn hàng này).
-- =====================================================================

SET NAMES utf8mb4;

DELETE FROM `skill_template` WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = '50' WHERE `k` = 'vs_skill';

SELECT `nclass_id`, COUNT(*) AS `so_ky_nang` FROM `skill_template`
 GROUP BY `nclass_id`;
