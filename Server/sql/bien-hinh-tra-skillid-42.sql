-- =====================================================================
--  Trả skillId của Biến Hình Super Xayda về 42 (đảo lại
--  sql/bien-hinh-dung-skillid-cua-bien-khi.sql).
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  PHÉP THỬ ĐÃ THẤT BẠI VÀ TỰ BÁC BỎ: cho chiêu 15 dùng skillId 91 của Biến
--  Khỉ làm THANH CHIÊU HỎNG — người chơi chỉ còn chọn được chiêu đấm, các
--  chiêu khác biến mất. Tức trùng skillId TRONG CÙNG hành tinh thì client
--  không dựng nổi danh sách kỹ năng. Giả định ban đầu của tôi đúng; lần thử
--  này là sai.
--
--  42 = khối của Thái Dương Hạ San (Trái Đất): client biết vẽ, Xayda chưa
--  dùng nên trong hành tinh 2 không trùng.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `skills` = '["{\"power_require\":1000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":42,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

-- Kiểm: trong mỗi hành tinh không được có skillId nào lặp lại.
SELECT `nclass_id`, `id`, `NAME` FROM `skill_template`
 WHERE `nclass_id` = 2 AND `skills` LIKE '%"id":42,%';
