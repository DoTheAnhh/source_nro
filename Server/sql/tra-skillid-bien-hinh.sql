-- =====================================================================
--  Trả skillId của kỹ năng Biến Hình (Xayda, id 15) về 56.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  BẰNG CHỨNG cho việc phải dùng một skillId client BIẾT:
--    • Khi để 56 (Tái tạo năng lượng cấp 1): máy chủ NHẬN được thông điệp -45,
--      dáng gồng hiện ra trong game.
--    • Khi đổi sang 105 — một ô TRỐNG trong dải 0..185: máy chủ không nhận
--      được thông điệp nào (tệp logs/bien-hinh.log rỗng dù người chơi đã đăng
--      nhập và bấm), client chỉ vẽ cú đấm mặc định.
--  Kết luận: client dispatch kỹ năng theo skillId. Ô trống trong dải KHÔNG
--  có nghĩa là client hỗ trợ ô đó.
--
--  Trùng skillId với Tái tạo năng lượng là chấp nhận được: dải 121..127 vốn đã
--  trùng ×3 từ trước (cùng một chiêu trên ba hành tinh) và client chạy bình
--  thường. Giá phải trả duy nhất: thông điệp -94 xoá hồi chiêu sẽ xoá cả hai
--  kỹ năng cùng lúc.
--
--  Loé biến hình KHÔNG lấy skillId này: EffectSkillService.guiHieuUngBienHinh()
--  gửi skillId của Biến Khỉ (91..97) trong chính thông điệp hiệu ứng, vì byte 6
--  kèm skillId 56 ra cú đấm chứ không ra loé biến hình.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `skills` = '["{\"power_require\":1000,\"damage\":0,\"dx\":0,\"dy\":0,\"price\":0,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":56,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

-- Client phải tải lại bảng kỹ năng. Chia lấy dư 128 vì DataGame.vs() ép về
-- một byte có dấu (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

SELECT `nclass_id`, `id`, `NAME`, `skills`
  FROM `skill_template` WHERE `nclass_id` = 2 AND `id` = 15;
