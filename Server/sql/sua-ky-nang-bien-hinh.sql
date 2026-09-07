-- =====================================================================
--  Sửa hai chỗ của kỹ năng Biến Hình (Xayda, id 15).
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  1) skillId TRÙNG.
--     Trường "id" trong JSON các cấp là skillId TOÀN CỤC (0..185), client dùng
--     nó để chọn ảnh động và để khớp đồng hồ hồi chiêu (message -94). Trước đây
--     tôi đặt 56 để lấy dáng gồng của Tái tạo năng lượng — nhưng 56 CHÍNH LÀ
--     của Tái tạo năng lượng cấp 1, thành ra hai kỹ năng dùng chung một skillId.
--     Dải 0..185 còn trống đúng hai chỗ: 105 và 106. Lấy 105.
--     Dáng gồng không mất: EffectSkillService.guiHieuUngBienHinh() gửi kèm
--     skillId của Tái tạo cấp 1 trong chính thông điệp hiệu ứng, tách rời khỏi
--     skillId của kỹ năng.
--
--  2) power_require 5.000.000 là do tôi tự đặt, người dùng không yêu cầu.
--     Client ẩn / chặn kỹ năng khi người chơi chưa đủ sức mạnh, nên nhân vật
--     thử nghiệm không bấm được. Hạ xuống 1000 — bằng Chiêu đấm Galick cấp 1,
--     mức thấp nhất đang dùng trong bảng.
--
--  Kỹ năng chỉ có 1 cấp nên JSON chỉ một phần tử.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `skills` = '["{\"power_require\":1000,\"damage\":0,\"dx\":0,\"dy\":0,\"price\":0,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":105,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]',
       `dam_info` = 'Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút'
 WHERE `nclass_id` = 2 AND `id` = 15;

-- Client phải tải lại bảng kỹ năng. Chia lấy dư 128 vì DataGame.vs() ép về
-- một byte có dấu (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

SELECT `nclass_id`, `id`, `NAME`, `TYPE`, `max_point`, `skills`
  FROM `skill_template` WHERE `nclass_id` = 2 AND `id` = 15;
