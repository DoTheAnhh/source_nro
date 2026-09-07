-- =====================================================================
--  Icon túi nhỏ cho Kamin và Oren
--
--  Hai bộ res này chỉ có MỘT ảnh dư (17296 / 17328, cỡ 57x68 ở mức x1) nên
--  ban đầu dùng luôn làm cả icon túi lẫn avatar. Vào game thì icon tràn ra
--  ngoài ô vật phẩm — icon cải trang có sẵn chỉ 18x20 đến 26x22.
--
--  Đã sinh thêm hai ảnh mới bằng cách thu nhỏ chính ảnh avatar:
--      17329  <- 17296 (Kamin)   x4 = 80x96,  x1 = 20x24
--      17330  <- 17328 (Oren)    x4 = 80x96,  x1 = 20x24
--
--  Avatar vẫn giữ nguyên 17296 / 17328 — chỗ đó cần ảnh to.
-- =====================================================================

UPDATE `item_template` SET `icon_id` = 17329 WHERE `id` = 2438;   -- Kamin
UPDATE `item_template` SET `icon_id` = 17330 WHERE `id` = 2439;   -- Oren

-- Ảnh mới nên client phải tải lại bảng vật phẩm
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` = 'vs_item';
