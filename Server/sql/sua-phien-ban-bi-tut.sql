-- =====================================================================
--  Sửa số phiên bản dữ liệu bị tụt lùi.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  NGUYÊN NHÂN: MariaDB tắt giữa buổi. ConfigDAO.reload() đọc thất bại nhưng
--  vẫn XOÁ bộ đệm rồi đặt loaded = true, nên num() rơi về DEFAULTS. Một lần
--  tăng phiên bản sau đó ghi mặc-định-cộng-một vào CSDL:
--      vs_skill  110 -> 2      (mặc định 1, +1)
--      vs_item   118 -> 8      (mặc định 5, +3 lần bấm)
--  Số phiên bản tụt lùi → client so với số nó đang cất, thấy lệch, xin tải,
--  cất số mới, so lại vẫn lệch… treo ở màn loading, không lỗi, không log.
--  Đã vá reload() để đọc hỏng thì GIỮ bộ đệm cũ.
--
--  Đặt lại cao hơn mốc cũ (110 và 118) để client chắc chắn tải lại một lần.
--  Phải nằm trong 0..127 vì DataGame ép về byte có dấu.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `panel_config` SET `v` = '120' WHERE `k` = 'vs_skill';
UPDATE `panel_config` SET `v` = '121' WHERE `k` = 'vs_item';
UPDATE `panel_config` SET `v` = '26'  WHERE `k` = 'vs_data';

SELECT `k`, `v` FROM `panel_config` WHERE `k` LIKE 'vs_%';
