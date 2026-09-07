-- =====================================================================
--  Kỹ năng Biến Hình (Xayda, id 15): 1 cấp -> 7 cấp.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  BẰNG CHỨNG: mọi kỹ năng client dùng được đều có ĐÚNG 7 cấp —
--    Tái tạo 56..62, Biến Khỉ 91..97, Tự phát nổ 98..104,
--    Thái Dương Hạ San 42..48, Khiên năng lượng 121..127, …
--  Kỹ năng 15 là kỹ năng DUY NHẤT chỉ có 1 cấp (max_point = 1). Nó nằm trong
--  thanh chiêu của người chơi (skills_shortcut = [4,-1,8,15,-1,13,…]), icon
--  hiện ra, nhưng bấm thì client KHÔNG gửi thông điệp -45 nào: trong
--  logs/bien-hinh.log, 218 nghìn dòng và chưa một dòng nào có
--  chieuDangChon=15, trong khi chiêu 4, 8, 13 của cùng người chơi đều có.
--  Client này đầy giả định bảng cố định (bảng phiên bản ảnh 32767 byte, id
--  part phải liền mạch) nên 1 cấp gần như chắc là hình dạng nó không đọc được.
--
--  skillId 42..48 = khối của Thái Dương Hạ San (Trái Đất): client BIẾT vẽ, và
--  Xayda chưa dùng khối này nên trong hành tinh 2 không trùng. Trùng với Trái
--  Đất là vô hại — khối 121..127 vốn đã dùng chung cho cả ba hành tinh.
--
--  ⚠ GIẢ ĐỊNH tôi tự đặt, bạn nói nếu muốn khác: hiệu ứng của kỹ năng (gồng
--  5 giây, +2%, 2 phút) nằm trong hằng số Java chứ không theo cấp, nên BẢY CẤP
--  ĐỀU CHO CÙNG MỘT HIỆU QUẢ. Bảy cấp ở đây chỉ để client đọc được đúng hình
--  dạng. Muốn mỗi cấp mạnh dần thì phải sửa EffectSkill.PHAN_TRAM_CUONG_NO
--  thành hàm theo point — nói tôi làm.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `skill_template`
   SET `max_point` = 7,
       `skills` = CONCAT(
 '["{\"power_require\":1000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":42,\"point\":1,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}",',
 '"{\"power_require\":2000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":43,\"point\":2,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}",',
 '"{\"power_require\":4000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":44,\"point\":3,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}",',
 '"{\"power_require\":8000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":45,\"point\":4,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}",',
 '"{\"power_require\":16000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":46,\"point\":5,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}",',
 '"{\"power_require\":32000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":47,\"point\":6,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}",',
 '"{\"power_require\":64000,\"damage\":100,\"dx\":200,\"dy\":200,\"price\":9999,\"max_fight\":1,\"mana_use\":10,\"cool_down\":300000,\"id\":48,\"point\":7,\"info\":\"Gồng 5 giây rồi biến hình: +2% HP, KI, sức đánh trong 2 phút\"}"]')
 WHERE `nclass_id` = 2 AND `id` = 15;

-- Client phải tải lại bảng kỹ năng. Chia lấy dư 128 vì DataGame.vs() ép về
-- một byte có dấu (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';

SELECT `id`, `NAME`, `max_point`, `skills` FROM `skill_template`
 WHERE `nclass_id` = 2 AND `id` = 15;
