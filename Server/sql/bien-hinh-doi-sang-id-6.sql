-- =====================================================================
--  Chuyển kỹ năng "Biến Hình Super Xayda" từ template id 15 -> 6.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  BẰNG CHỨNG (logs/bien-hinh.log, sau khi thêm log cho thông điệp 34):
--      [34] dsadd XIN CHON chieu id=15 -> co, diem=1 skillId=42     (2 lần)
--  Client CHỌN được chiêu 15. Nhưng không một dòng `bam -45` nào có
--  chieuDangChon=15 — chọn xong không dùng được. Đối chiếu chiêu 13: chọn xong
--  là có ngay dòng dùng (status=6).
--  => Chọn chiêu đi đường CHUNG theo id; còn DÙNG thì client gõ cứng theo
--     template id, và 15 là id nó không có nhánh nào.
--
--  id 6 = Thái Dương Hạ San của Trái Đất: client biết dùng, cùng TYPE 3 (dùng
--  lên bản thân), và Xayda CHƯA có id này nên trong hành tinh 2 không trùng.
--  Khoá chính của skill_template là (nclass_id, id) nên (2,6) không đụng (0,6).
--
--  Phía máy chủ: case Skill.THAI_DUONG_HA_SAN rẽ theo hành tinh — Xayda thì
--  chạy Biến Hình, Trái Đất giữ nguyên choáng. Trái Đất không có id 15 và
--  Xayda không có choáng, nên rẽ theo hành tinh là không nhập nhằng.
--
--  ĐẢO LẠI: đổi 6 về 15 ở cả ba lệnh UPDATE dưới đây, và bỏ nhánh Xayda trong
--  SkillService.
-- =====================================================================

SET NAMES utf8mb4;

-- 1. Bản mẫu kỹ năng
UPDATE `skill_template` SET `id` = 6 WHERE `nclass_id` = 2 AND `id` = 15;

-- 2. Kỹ năng người chơi đã học. Mỗi ô là ["<id>,<point>,<lastTime>,<curr>"]
--    nên chỉ thay đúng mẫu mở đầu ô, không đụng các số khác trong chuỗi.
UPDATE `player` SET `skills` = REPLACE(`skills`, '["15,', '["6,')
 WHERE `gender` = 2 AND `skills` LIKE '%["15,%';
UPDATE `player` SET `skills` = REPLACE(`skills`, ',"15,', ',"6,')
 WHERE `gender` = 2 AND `skills` LIKE '%,"15,%';

-- 3. Thanh chiêu — mảng 10 số ngăn bằng dấu phẩy. Thay theo từng vị trí để
--    không biến số 15 ở chỗ khác thành 6.
UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, '[15,', '[6,')
 WHERE `gender` = 2;
UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, ',15,', ',6,')
 WHERE `gender` = 2;
UPDATE `player` SET `skills_shortcut` = REPLACE(`skills_shortcut`, ',15]', ',6]')
 WHERE `gender` = 2;

UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

SELECT `nclass_id`, `id`, `NAME`, `slot`, `TYPE`, `max_point`
  FROM `skill_template` WHERE `id` = 6 ORDER BY `nclass_id`;
SELECT `NAME`, `skills_shortcut` FROM `player` WHERE `NAME` IN ('dsadd','dsds2');
