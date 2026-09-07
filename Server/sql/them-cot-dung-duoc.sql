-- =====================================================================
--  Thêm cột `dung_duoc` cho bảng item_template
--
--  Chỉ là một dấu tick để đánh dấu vật phẩm nào còn dùng được — KHÔNG gắn
--  logic gì trong game, không gửi sang client, nên không cần tăng vs_item.
--
--  Mặc định 1 (đã tick) để mọi vật phẩm đang có giữ nguyên trạng thái cũ.
-- =====================================================================

ALTER TABLE `item_template`
  ADD COLUMN `dung_duoc` TINYINT(1) NOT NULL DEFAULT 1
  COMMENT 'Đánh dấu thủ công trong panel: vật phẩm này còn dùng được hay không'
  AFTER `isGender`;
