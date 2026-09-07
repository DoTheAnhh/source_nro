-- =====================================================================
--  Nâng sức mạnh nhân vật thử và cấp quyền admin, để TÁCH yếu tố sức mạnh
--  khỏi phép thử kỹ năng.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--  ⚠ CHỈ chạy khi nhân vật ĐANG OFFLINE — đang online thì bản lưu trong bộ
--     nhớ ghi đè lại lúc thoát.
--
--  VÌ SAO: nhân vật dsd2d có 1.905 sức mạnh. Mọi kỹ năng Xayda khác đều cần
--  nhiều hơn (Antomic 10.000, Tái tạo 60.000, Huýt sáo / Trói / Khiên 10 triệu,
--  Biến khỉ / Tự phát nổ 250 triệu, Cađíc 60 tỷ) nên client nháy icon là ĐÚNG
--  LUẬT — không phải lỗi. Chỉ Chiêu đấm Galick (1.000) và Biến Hình (1.000) là
--  đủ, mà Biến Hình vẫn nháy — đó mới là chỗ bất thường.
--
--  Nâng lên 500 triệu thì mọi kỹ năng trừ Cađíc đều mở, nên nếu vẫn không
--  chuyển được chiêu thì chắc chắn KHÔNG phải do sức mạnh.
--
--  data_point = [limitPower, power, tiemNang, stamina, maxStamina, ...]
--  nên phải thay đúng phần tử thứ HAI.
--
--  isFounder = 1 để dùng được lệnh admin trong khung chat, ví dụ:
--      sm 500000000     (đặt sức mạnh)
--      tn 100000000     (đặt tiềm năng)
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `player`
   SET `data_point` = CONCAT(
         SUBSTRING_INDEX(`data_point`, ',', 1), ',500000000,',
         SUBSTRING(`data_point`,
                   LENGTH(SUBSTRING_INDEX(`data_point`, ',', 2)) + 2))
 WHERE `NAME` = 'dsd2d';

UPDATE `account` SET `isFounder` = 1, `is_admin` = 1
 WHERE `id` = (SELECT `account_id` FROM `player` WHERE `NAME` = 'dsd2d');

SELECT `NAME`, `data_point` FROM `player` WHERE `NAME` = 'dsd2d';
