-- =====================================================================
--  Biến Khỉ (Xayda) hoá thành CẢI TRANG 1589 thay cho dạng khỉ.
--
--  ⚠ PHẢI chạy bằng TỆP. `mysql -e "…"` trên máy này làm mất dấu tiếng Việt.
--     mysql -uroot --default-character-set=utf8mb4 awnv3 < tệp này
--
--  VÌ SAO PHẢI ĐI QUA BIẾN KHỈ: client Unity (IL2CPP, không có mã nguồn) chỉ
--  dispatch một tập template id gõ cứng. Kỹ năng thêm mới với template id 15 —
--  id chưa hành tinh nào dùng — không bao giờ được client gửi lệnh dùng:
--  0 dòng `chieuDangChon=15` trên hơn 218 nghìn dòng logs/bien-hinh.log, dù
--  icon vẫn hiện trong thanh chiêu và skillId của nó client vẫn vẽ được.
--  Bằng chứng client gõ cứng theo id: nó gửi status=6 riêng cho chiêu 13,
--  status=1/2 cho chiêu 8, status=-1 cho đòn đánh; và metadata client có sẵn
--  `isMonkey`, `isWaitMonkey`, `monkeyRun`.
--
--  0 = giữ dạng khỉ như gốc. Khác 0 = id cải trang.
--  Cơ chế Biến Khỉ giữ NGUYÊN: vận chiêu + đóng băng + thời lượng + chỉ số.
--  Chỉ phần hình đổi — cả đầu, thân, chân (Biến Khỉ gốc chỉ đổi đầu).
--  Cửa sổ vận chiêu đã đổi 1500 ms -> 5000 ms trong mã.
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO `panel_config` (`k`, `v`, `note`) VALUES
 ('bien_khi_ct', '1589',
  'Cải trang mà Biến Khỉ hoá thành, thay dạng khỉ. 0 = giữ dạng khỉ như gốc.')
ON DUPLICATE KEY UPDATE `v` = VALUES(`v`), `note` = VALUES(`note`);

SELECT `k`, `v`, `note` FROM `panel_config` WHERE `k` = 'bien_khi_ct';
