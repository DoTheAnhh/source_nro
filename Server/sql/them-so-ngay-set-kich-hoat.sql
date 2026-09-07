-- =====================================================================
--  Thêm quy ước "số ngày còn nhận được set kích hoạt".
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  0 = KHÔNG giới hạn (mặc định). Khác 0 = số ngày kể từ lúc tạo tài khoản.
--  Vật phẩm gia hạn mầm (1784, 1798) vẫn cộng thêm vào số ngày này như cũ.
--
--  Trước đây con số này gõ cứng bằng 1 trong GodGK và chỉ tồn tại ở cờ
--  isNewMember gửi cho client — nó KHÔNG chặn việc rơi, nên tài khoản cũ vẫn
--  nhặt được set kích hoạt. Nay chặn ở đúng chỗ rơi.
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO `panel_config` (`k`, `v`, `note`) VALUES
 ('skh_so_ngay', '0',
  'Số ngày kể từ khi tạo tài khoản còn nhận được set kích hoạt — 0 là không giới hạn')
ON DUPLICATE KEY UPDATE `note` = VALUES(`note`);

SELECT `k`, `v`, `note` FROM `panel_config` WHERE `k` = 'skh_so_ngay';
