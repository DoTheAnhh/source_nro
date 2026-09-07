-- =====================================================================
--  Xoá kỹ năng BÓNG khỏi dữ liệu người chơi.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--  ⚠ CHỈ chạy khi nhân vật ĐANG OFFLINE — đang online thì bản lưu trong bộ
--     nhớ vẫn còn mục bóng và sẽ ghi đè lại lúc thoát.
--
--  NGUYÊN NHÂN: ConstPlayer.SKILL_XAYDA ghi id 22 — Thôi miên của TRÁI ĐẤT,
--  Xayda không có. Mỗi lần đăng nhập, vòng cấp kỹ năng trong GodGK gọi
--  createSkillLevel0(22), dựng SkillTemplate RỖNG (tên null, maxPoint 0,
--  không cấp nào) rồi lưu vào cột player.skills. Client nhận danh sách có mục
--  trỏ vào kỹ năng không tồn tại thì hỏng cả bảng kỹ năng: bấm icon không chọn
--  được chiêu nào ngoài đòn đấm.
--
--  Đã sửa gốc: SKILL_XAYDA dùng 23 (Trói — kỹ năng khống chế của Xayda), và
--  GodGK bỏ qua mọi id mà hành tinh đó không có (SkillUtil.coKyNang).
--
--  Mỗi ô là "[<id>,<point>,<lastTime>,<curr>]". Xoá ô id 22 kèm dấu phẩy
--  ngăn cách. Thử cả hai vị trí: giữa mảng và cuối mảng.
-- =====================================================================

SET NAMES utf8mb4;

-- Ô ở giữa: ,"[22,...]"  ->  bỏ hẳn
UPDATE `player`
   SET `skills` = REGEXP_REPLACE(`skills`, ',"\\[22,[^]]*\\]"', '')
 WHERE `gender` = 2 AND `skills` REGEXP ',"\\[22,';

-- Ô ở đầu: ["[22,...]",  ->  bỏ hẳn
UPDATE `player`
   SET `skills` = REGEXP_REPLACE(`skills`, '"\\[22,[^]]*\\]",', '')
 WHERE `gender` = 2 AND `skills` REGEXP '"\\[22,';

SELECT `NAME`, `skills` FROM `player` WHERE `gender` = 2;
