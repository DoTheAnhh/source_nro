-- =====================================================================
--  Trả nhân vật về Làng Kakarot (map 14).
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  NGUYÊN NHÂN MẤT VỊ TRÍ: tôi tắt máy chủ bằng `Stop-Process -Force`, tức
--  chấm dứt JVM ngay — móc lưu dữ liệu không chạy, vị trí đang chơi không được
--  ghi, nhân vật rơi về bản lưu cũ là map 0 (Làng Aru).
--
--  data_location = [mapId, x, y]. Toạ độ lấy đúng chỗ ảnh chụp: x=850 y=408.
--
--  ⚠ CHỈ chạy khi nhân vật ĐANG OFFLINE. Đang online thì bản lưu trong bộ nhớ
--  sẽ ghi đè lại lúc thoát.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `player` SET `data_location` = '[14,850,408]'
 WHERE `NAME` = 'dsd2d';

SELECT `NAME`, `data_location` FROM `player` WHERE `NAME` = 'dsd2d';
