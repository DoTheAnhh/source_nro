-- =====================================================================
--  Biến Hình Super Xayda: 1 cấp -> 7 cấp. PHÉP THỬ TÁCH BIẾN SỐ.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  BẰNG CHỨNG DẪN TỚI PHÉP THỬ NÀY:
--    • Log ghi MỌI thông điệp client gửi: 38×(-7), 38×(-67), 21×(-32), 14×(54)…
--      và ĐÚNG 0 lần cmd 34. Client không gửi lệnh chọn kỹ năng nào.
--    • Sức mạnh không giải thích được: Chiêu đấm Galick cần 1000 và CHỌN ĐƯỢC;
--      Biến Hình cũng cần 1000 mà KHÔNG chọn được; nhân vật có 1605.
--    • Khác biệt còn lại duy nhất: Biến Hình 1 cấp, mọi kỹ năng khác 7 cấp.
--      Không kỹ năng nào trong bảng chỉ có 1 cấp.
--    • Lần 22:20 client CÓ gửi [34] id=15 — nhưng nhân vật đó có 100 triệu sức
--      mạnh nên hai biến số lẫn nhau. Nay nhân vật yếu, tách được.
--
--  BẢY CẤP ĐỀU CÙNG TÁC DỤNG: hiệu ứng nằm trong hằng số Java, không theo cấp.
--  Người chơi giữ điểm 1 nên về mặt chơi vẫn đúng là một cấp — bảy cấp ở đây
--  chỉ để client đọc được đúng hình dạng.
--
--  ĐẢO LẠI: chạy sql/bien-hinh-1-cap.sql.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `max_point` = 7,
       `skills` = '["{\"power_require\":1000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":42,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}","{\"power_require\":2000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":43,\"point\":2,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}","{\"power_require\":4000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":44,\"point\":3,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}","{\"power_require\":8000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":45,\"point\":4,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}","{\"power_require\":16000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":46,\"point\":5,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}","{\"power_require\":32000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":47,\"point\":6,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}","{\"power_require\":64000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":48,\"point\":7,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]'
 WHERE `nclass_id` = 2 AND `id` = 15;

UPDATE `panel_config` SET `v` = '45' WHERE `k` = 'vs_skill';

SELECT `id`, `NAME`, `max_point` FROM `skill_template`
 WHERE `nclass_id` = 2 AND `id` = 15;
