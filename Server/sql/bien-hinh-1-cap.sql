-- =====================================================================
--  Kỹ năng Biến Hình Super Xayda (Xayda, id 15): về ĐÚNG 1 CẤP.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  Trước đó tôi cấp 7 cấp theo giả thuyết "client cần đúng 7 cấp". GIẢ THUYẾT
--  ĐÓ SAI: trong lúc CSDL đang có 7 cấp và máy chủ đã nạp lại, người chơi bấm
--  kỹ năng và logs/bien-hinh.log vẫn KHÔNG có dòng nào chieuDangChon=15.
--  Số cấp chưa bao giờ là nguyên nhân. Người dùng chỉ cần 1 cấp nên trả về 1.
--
--  max_point phải bằng số cấp — KyNangDAO.luu() nay chặn nếu lệch.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `max_point` = 1,
       `skills` = '["{\"power_require\":1000,\"damage\":0,\"dx\":0,\"dy\":0,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":42,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

SELECT `id`, `NAME`, `max_point`, `skills` FROM `skill_template`
 WHERE `nclass_id` = 2 AND `id` = 15;
