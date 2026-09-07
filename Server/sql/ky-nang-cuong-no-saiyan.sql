-- =====================================================================
--  Kỹ năng mới cho Xayda: Cuồng Nộ Saiyan (id 15)
--
--  Dùng vào thì:
--    * gồng như Tái tạo năng lượng (EffectSkillService.startCharge)
--    * đè hình ngoài bản đồ thành cải trang 1589 "CT Goku SSJ"
--      (KHÔNG mặc cải trang đó, nên không ăn chỉ số của nó)
--    * cộng 2% HP, KI, sức đánh
--    * hiệu lực 2 phút, hồi chiêu 5 phút
--
--  Ba số này nằm ở hai nơi khác nhau, đừng sửa một chỗ rồi tưởng xong:
--    hồi chiêu  -> cột `skills`, trường "cool_down" (300000 ms = 5 phút)
--    hiệu lực   -> EffectSkill.THOI_GIAN_CUONG_NO (120000 ms = 2 phút)
--    phần trăm  -> EffectSkill.PHAN_TRAM_CUONG_NO
--
--  id = 15 vì đó là id nhỏ nhất còn trống trên CẢ BA hành tinh — trùng id
--  giữa hai hành tinh là đọc nhầm chiêu của nhau.
--  Máy chủ đọc id kỹ năng bằng writeByte nên phải nằm trong 0..127.
--
--  TYPE = 3 (dùng lên bản thân), mana_use_type = 1 (tốn % KI tối đa).
-- =====================================================================

INSERT INTO `skill_template`
 (`nclass_id`, `id`, `NAME`, `max_point`, `mana_use_type`, `TYPE`, `icon_id`,
  `dam_info`, `slot`, `skills`)
VALUES
 (2, 15, 'Cuồng Nộ Saiyan', 1, 1, 3, 30811,
  'Gồng 2 phút: +2% HP, KI, sức đánh và đổi hình', 9,
  '["{"power_require":5000000,"damage":0,"dx":0,"dy":0,"price":0,"max_fight":1,"mana_use":10,"cool_down":300000,"id":0,"point":1,"info":"Gồng 2 phút, cộng 2% HP/KI/sức đánh và đổi hình"}"]')
ON DUPLICATE KEY UPDATE
  `NAME` = VALUES(`NAME`), `max_point` = VALUES(`max_point`),
  `mana_use_type` = VALUES(`mana_use_type`), `TYPE` = VALUES(`TYPE`),
  `icon_id` = VALUES(`icon_id`), `dam_info` = VALUES(`dam_info`),
  `slot` = VALUES(`slot`), `skills` = VALUES(`skills`);

-- Client phải tải lại bảng kỹ năng. Chia lấy dư 128 vì DataGame.vs() ép về
-- một byte có dấu (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_skill';
