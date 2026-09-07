-- =====================================================================
--  skillId của kỹ năng Biến Hình (Xayda, id 15): 56 -> 42.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  BẰNG CHỨNG (logs/bien-hinh.log, máy chủ ghi mỗi lần bấm chiêu):
--      [16:20:07] dsadd bam -45 status=1 chieuDangChon=8 "Tái tạo năng lượng"
--  Người chơi bấm cái họ tưởng là Biến Hình, nhưng chiêu được chọn luôn là
--  8 (Tái tạo năng lượng) — chưa lần nào là 15. Kỹ năng 15 dùng skillId 56,
--  đúng bằng skillId cấp 1 của Tái tạo, nên trong danh sách kỹ năng của Xayda
--  hai chiêu đè nhau và client không chọn được Biến Hình.
--
--  Hai lần thử trước, và vì sao trượt:
--    • skillId 105 — ô TRỐNG trong dải 0..185. Client không biết id này:
--      logs/bien-hinh.log không hề được tạo, tức không thông điệp -45 nào tới
--      máy chủ. Ô trống KHÔNG phải ô rảnh.
--    • skillId 56 — client biết, nhưng TRÙNG trong cùng hành tinh (bằng chứng
--      ở trên).
--
--  Nên skillId phải thoả ĐỒNG THỜI: client biết vẽ, và riêng biệt trong hành
--  tinh đó. Xayda đang dùng: 28-34, 35-41, 56-62, 91-97, 98-104, 121-127,
--  135-141, 149-155, 176-185. Chọn 42 = Thái Dương Hạ San cấp 1 của Trái Đất:
--  client biết (hành tinh 0 đang dùng), cùng TYPE 3 (tự dùng lên bản thân),
--  và Xayda chưa dùng. Trùng với Trái Đất là vô hại — dải 121..127 vốn đã
--  trùng ×3 giữa ba hành tinh và client chạy bình thường.
--
--  DÁNG GỒNG KHÔNG PHỤ THUỘC SỐ NÀY: EffectSkillService.sendEffectCharge()
--  gửi skillId của Tái tạo năng lượng trong chính thông điệp hiệu ứng, còn
--  loé biến hình gửi skillId của Biến Khỉ. Đổi skillId của kỹ năng không làm
--  mất hai dáng đó.
--
--  Nếu 42 vẫn không hiện, thử lần lượt các dải client biết mà Xayda chưa dùng:
--      84 (Đẻ trứng, Namếc, TYPE 3) → 49 (Trị thương, Namếc, TYPE 2)
--      → 63 (Kaioken, Trái Đất) → 70 (Quả cầu kênh khí, Trái Đất)
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `skills` = '["{\"power_require\":1000,\"damage\":0,\"dx\":0,\"dy\":0,\"price\":0,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":42,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

-- Client phải tải lại bảng kỹ năng. Chia lấy dư 128 vì DataGame.vs() ép về
-- một byte có dấu (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

-- Kiểm: trong hành tinh 2 không được có skillId nào xuất hiện hai lần.
SELECT `id`, `NAME`, `skills` FROM `skill_template`
 WHERE `nclass_id` = 2 AND `skills` LIKE '%"id":42,%';
