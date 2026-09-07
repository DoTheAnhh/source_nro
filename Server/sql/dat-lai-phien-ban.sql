-- =====================================================================
--  Đặt lại số phiên bản dữ liệu về mốc an toàn.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  VÌ SAO PHẢI ĐẶT LẠI: bộ đếm chỉ có 0..127 (DataGame ép về byte có dấu) nên
--  nó VÒNG. Bài kiểm tra gọi KyNangDAO.luu() / MauVatPhamDAO.xoa(), mỗi lần
--  gọi tăng một, nên chạy vài lượt là vòng qua 127 rồi tụt về 0 — số phiên bản
--  ĐI LÙI. Client so với số nó đang cất, thấy lệch, xin tải, cất số mới, so
--  lại vẫn lệch… treo ở màn loading, không lỗi, không log.
--
--  Đặt về giữa dải, cách xa cả 0 và 127, để còn nhiều chỗ tăng trước khi vòng.
--  Chọn số KHÁC hẳn giá trị client đang cất để nó tải lại đúng một lần.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `panel_config` SET `v` = '40' WHERE `k` = 'vs_skill';
UPDATE `panel_config` SET `v` = '41' WHERE `k` = 'vs_item';
UPDATE `panel_config` SET `v` = '42' WHERE `k` = 'vs_data';

SELECT `k`, `v` FROM `panel_config` WHERE `k` LIKE 'vs_%';
