-- =====================================================================
--  Khu trò chơi nhỏ — hai bảng lịch sử dùng chung
--
--  Lược đồ dựng lại từ CSDL đang chạy, khớp đúng
--  nro/repository/dao/MiniGameDAO.java.
--
--  KHÔNG BẮT BUỘC PHẢI CHẠY FILE NÀY. MiniGameDAO.damBaoBang() tự tạo hai
--  bảng bằng CREATE TABLE IF NOT EXISTS ở lần đọc/ghi đầu tiên. File này
--  có để: dựng sẵn trên máy chủ mới cho khỏi phải chờ lần chạy đầu, đọc
--  được lược đồ mà không phải mở mã Java, và làm chỗ ghi lại ý nghĩa từng
--  cột — thứ mà CREATE TABLE trong mã không chứa nổi.
--
--  HAI BẢNG CHO SÁU TRÒ, KHÔNG PHẢI MƯỜI HAI
--  Cột `tro` cho biết dòng thuộc trò nào. Gộp lại vì ba lẽ: thêm một trò
--  thì không phải thêm bảng; bảng quản lý bên panel đọc một chỗ là thấy
--  hết; và câu hỏi hay gặp nhất — "người này hôm nay được thua bao nhiêu"
--  — chỉ là một câu lệnh chứ không phải sáu câu rồi cộng tay.
--
--  MÃ TRÒ (nro/service/MiniGameService.java)
--    0  Bầu Cua      1  Xóc Đĩa     2  Đua Ngựa
--    3  Đào Vàng     4  Cao Thấp    5  Câu Cá
--
--  BA TRÒ THEO PHIÊN dùng cả hai bảng: Bầu Cua, Xóc Đĩa, Đua Ngựa.
--  BA TRÒ CHƠI MỘT MÌNH chỉ ghi mg_cuoc, cột `phien` mang nghĩa khác —
--  xem chú thích ở từng cột.
-- =====================================================================


-- ---------------------------------------------------------------------
--  1. mg_phien — mỗi ván một dòng, lịch sử chung của cả máy chủ
--
--  Chỉ ba trò theo phiên ghi vào đây. Ba trò chơi một mình không có ván
--  chung nào để ghi.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mg_phien` (
  `id`        BIGINT(20)  NOT NULL AUTO_INCREMENT,

  -- Mã trò, xem bảng ở đầu file.
  `tro`       TINYINT(4)  NOT NULL DEFAULT 0,

  -- Số ván. Đếm tiếp từ ván cuối đã ghi chứ không quay về 1 — hai ván
  -- khác nhau mang cùng một số thì lịch sử đọc ra vô nghĩa.
  `phien`     BIGINT(20)  NOT NULL DEFAULT 0,

  -- Ba cột kết quả, đủ chứa mọi trò ở đây:
  --   Bầu Cua   kq1..kq3 = ba con xúc xắc (0..5)
  --   Xóc Đĩa   kq1 = bốn đồng xu gói trong bốn bit thấp (0..15)
  --             kq2 = số đồng đỏ, tức số bit bật
  --   Đua Ngựa  kq1 = con về nhất, kq2 = con về nhì, kq3 = con về ba
  `kq1`       INT(11)     NOT NULL DEFAULT 0,
  `kq2`       INT(11)     NOT NULL DEFAULT 0,
  `kq3`       INT(11)     NOT NULL DEFAULT 0,

  -- Kết quả rút gọn để vẽ dải lịch sử: Bầu Cua là con ra nhiều nhất,
  -- Xóc Đĩa là 0 Chẵn / 1 Lẻ, Đua Ngựa là con về nhất.
  `ket_qua`   INT(11)     NOT NULL DEFAULT 0,

  -- Tổng thỏi vàng đặt vào ván, và tổng đã trả ra. Hai con số này là
  -- cách duy nhất soát được một trò có đang chảy máu kho hay không.
  `tong_cuoc` BIGINT(20)  NOT NULL DEFAULT 0,
  `tong_tra`  BIGINT(20)  NOT NULL DEFAULT 0,

  -- Số người đặt trong ván.
  `so_nguoi`  INT(11)     NOT NULL DEFAULT 0,

  -- Mốc thời gian, mili giây kể từ 1970. Dùng BIGINT chứ không TIMESTAMP
  -- vì mã Java ghi thẳng System.currentTimeMillis() — không phải đổi qua
  -- múi giờ ở hai đầu, và không có chuyện đổi giờ hệ thống làm lệch.
  `luc`       BIGINT(20)  NOT NULL DEFAULT 0,

  PRIMARY KEY (`id`),

  -- Tra theo trò rồi theo ván: mọi truy vấn lịch sử đều lọc `tro` trước.
  KEY `idx_tro_phien` (`tro`, `phien`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- ---------------------------------------------------------------------
--  2. mg_cuoc — mỗi lượt đặt một dòng, lịch sử riêng của từng người
--
--  Cả sáu trò ghi vào đây.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mg_cuoc` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT,

  -- Mã trò, xem bảng ở đầu file.
  `tro`        TINYINT(4)  NOT NULL DEFAULT 0,

  -- Ba trò theo phiên: số ván, khớp mg_phien.phien.
  -- Ba trò chơi một mình không có phiên nên cột này mang nghĩa khác:
  --   Đào Vàng, Cao Thấp   0
  --   Câu Cá               TẦM QUĂNG người chơi căn được, 0..100
  --
  -- Chỗ này là cố ý: tầm quăng do client đo nên nó sửa được, và cột này
  -- là cách soát ra ai luôn gửi tầm 100.
  `phien`      BIGINT(20)  NOT NULL DEFAULT 0,

  -- Người đặt. Giữ cả tên để nhật ký còn đọc được sau khi nhân vật đổi
  -- tên hoặc bị xoá — chỉ giữ id thì mấy tháng sau tra ra một con số trần.
  `player_id`  BIGINT(20)  NOT NULL DEFAULT 0,
  `ten`        VARCHAR(80)          DEFAULT NULL,

  -- Cửa đã đặt, ý nghĩa theo từng trò:
  --   Bầu Cua   0 Bầu, 1 Cua, 2 Tôm, 3 Cá, 4 Gà, 5 Nai
  --   Xóc Đĩa   0 Chẵn, 1 Lẻ, 2 Bốn đỏ, 3 Bốn trắng
  --   Đua Ngựa  0..5 số con ngựa
  --   Đào Vàng  số ô đã mở được
  --   Cao Thấp  số lần đoán đúng
  --   Câu Cá    loại cá: 0 ngựa con, 1 thu, 2 cờ, 3 mập, 4 vàng
  `cua`        INT(11)     NOT NULL DEFAULT 0,

  -- Số thỏi vàng đã bỏ ra. Câu Cá ghi tiền mồi.
  `so_thoi`    BIGINT(20)  NOT NULL DEFAULT 0,

  -- Kết quả ván, ý nghĩa theo từng trò:
  --   ba trò theo phiên   cửa thắng của ván
  --   Đào Vàng, Cao Thấp  số ô mở được / số lần đoán đúng
  --   Câu Cá              1 kéo được, 0 để tuột
  `ket_qua`    INT(11)     NOT NULL DEFAULT 0,

  `thang`      TINYINT(1)  NOT NULL DEFAULT 0,

  -- Số thỏi vàng đã trả về, gồm cả gốc. 0 nghĩa là mất trắng tiền cược.
  `tien_thang` BIGINT(20)  NOT NULL DEFAULT 0,

  `luc`        BIGINT(20)  NOT NULL DEFAULT 0,

  PRIMARY KEY (`id`),

  -- Màn "Lịch sử của tôi" lọc theo người rồi xếp theo id giảm dần.
  KEY `idx_nguoi` (`player_id`, `id`),

  -- Bảng quản lý bên panel lọc theo trò rồi xếp theo id giảm dần.
  KEY `idx_tro` (`tro`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- =====================================================================
--  MẤY CÂU TRA HAY DÙNG
-- =====================================================================

-- Kho vàng của từng trò đang lãi hay lỗ. tong_tra > tong_cuoc là đang
-- chảy máu — chỉ đúng cho ba trò theo phiên.
--
--   SELECT tro,
--          SUM(tong_cuoc) AS thu,
--          SUM(tong_tra)  AS tra,
--          SUM(tong_cuoc) - SUM(tong_tra) AS lai
--     FROM mg_phien
--    GROUP BY tro;

-- Cả sáu trò, tính từ bảng lượt đặt:
--
--   SELECT tro,
--          COUNT(*)            AS so_luot,
--          SUM(so_thoi)        AS thu,
--          SUM(tien_thang)     AS tra,
--          SUM(so_thoi) - SUM(tien_thang) AS lai
--     FROM mg_cuoc
--    GROUP BY tro;

-- Một người được thua bao nhiêu, gộp cả sáu trò:
--
--   SELECT ten,
--          COUNT(*)        AS so_luot,
--          SUM(tien_thang) - SUM(so_thoi) AS duoc_thua
--     FROM mg_cuoc
--    WHERE player_id = ?
--    GROUP BY ten;

-- Soát client sửa ở Câu Cá (tro = 5). Bước vật lộn chạy ở client nên nó
-- báo được "bắt được" mọi lượt. Tỉ lệ gần 100% trên vài trăm lượt là dấu
-- rõ; và tam_tb sát 100 nghĩa là luôn gửi tầm quăng tối đa.
--
--   SELECT ten,
--          COUNT(*)                       AS so_luot,
--          ROUND(AVG(ket_qua) * 100, 1)   AS ti_le_bat_duoc,
--          ROUND(AVG(phien), 1)           AS tam_tb
--     FROM mg_cuoc
--    WHERE tro = 5
--    GROUP BY player_id, ten
--   HAVING so_luot >= 50
--    ORDER BY ti_le_bat_duoc DESC;

-- Dọn lịch sử cũ hơn ba mươi ngày. Hai bảng này chỉ để tra soát, không
-- có gì trong máy chủ đọc lại dòng cũ, nên xoá là an toàn.
--
--   DELETE FROM mg_cuoc
--    WHERE luc < (UNIX_TIMESTAMP() - 30 * 86400) * 1000;
--   DELETE FROM mg_phien
--    WHERE luc < (UNIX_TIMESTAMP() - 30 * 86400) * 1000;
