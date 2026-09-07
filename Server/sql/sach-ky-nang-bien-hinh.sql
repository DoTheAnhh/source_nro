-- =====================================================================
--  Đổi tên kỹ năng 15 thành "Biến Hình" + sách học nó (vật phẩm 2447)
--
--  ⚠ TRÙNG TÊN: Xayda đã có kỹ năng id 13 tên "Biến hình" (hằng số
--  Skill.BIEN_KHI). Sau lệnh này Xayda có HAI kỹ năng cùng tên — id khác nhau
--  nên máy chủ chạy đúng, nhưng người chơi nhìn danh sách sẽ thấy hai dòng
--  giống nhau. Muốn phân biệt thì đổi tên một trong hai.
--
--  SÁCH HỌC KỸ NĂNG — ba thứ phải khớp nhau:
--    1. TYPE = 7        UseItem.doItem: case 7 -> learnSkill()
--    2. Tên kết thúc bằng SỐ CẤP. learnSkill lấy KÝ TỰ CUỐI của tên làm cấp
--       (`name.split("")` rồi đọc phần tử cuối). Khuôn có sẵn là
--       "<tên kỹ năng> lv<N>", ví dụ "Tái tạo năng lượng lv1".
--       Tên không kết thúc bằng số là Byte.parseByte ném lỗi, bị try/catch
--       nuốt mất — bấm sách không có gì xảy ra và không báo gì cả.
--    3. SkillUtil.getTempSkillSkillByItemID + getSkillByItemID phải map id
--       vật phẩm này sang Skill.CUONG_NO_SAIYAN (đã thêm trong mã nguồn).
--
--  gender = 2 (Xayda): learnSkill từ chối nếu gender vật phẩm khác hành tinh
--  người chơi và cũng khác 3.
--
--  Kỹ năng chỉ có 1 cấp nên chỉ cần 1 sách. Các kỹ năng cũ lấy cả dải 7 id
--  liền nhau vì chúng có 7 cấp.
--
--  Id vật phẩm 2447 = max + 1. Bắt buộc, vì
--  ItemService.getTemplate(id) = ITEM_TEMPLATES.get(id) — id là chỉ số mảng.
-- =====================================================================

UPDATE `skill_template` SET `NAME` = 'Biến Hình',
  `dam_info` = 'Gồng 5 giây rồi biến đổi: +2% HP, KI, sức đánh trong 2 phút'
 WHERE `nclass_id` = 2 AND `id` = 15;

INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2447, 7, 2, 'Biến Hình lv1',
  'Sách học kỹ năng Biến Hình cho Xayda. Gồng 5 giây rồi biến đổi, cộng 2% HP, KI, sức đánh trong 2 phút.',
  1, 30811, -1, 0, 0, 1, 0, 0, 0, 0, 0, -1, -1, -1, 0, -1)
ON DUPLICATE KEY UPDATE
  `TYPE` = VALUES(`TYPE`), `gender` = VALUES(`gender`), `NAME` = VALUES(`NAME`),
  `description` = VALUES(`description`), `icon_id` = VALUES(`icon_id`);

-- Client phải tải lại cả bảng kỹ năng (đổi tên) và bảng vật phẩm (sách mới).
-- Chia lấy dư 128 vì DataGame.vs() ép về một byte có dấu (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128
 WHERE `k` IN ('vs_skill', 'vs_item');
