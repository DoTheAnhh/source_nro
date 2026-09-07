-- =====================================================================
--  Nắn chỉ số gốc của đệ tử ĐÃ CÓ về trần hiện hành
-- =====================================================================
--
--  Vì sao cần: đệ tử cũ được bốc theo bậc thang cũ, trong đó bậc cao nhất
--  cho tới 1.000.000 HP và 70.000 sức đánh — vượt hẳn trần bây giờ.
--
--  ---------------------------------------------------------------------
--  Trần lấy TỪ PANEL, không viết cứng trong đây
--  ---------------------------------------------------------------------
--  Bốn con số trần đọc từ bảng `de_tu_chi_so` (tab "Đệ tử" của panel), và
--  hệ số từng loại đệ đọc từ bảng `de_tu_loai`. Bản trước viết cứng
--  600000 / 25000 / 200 / 10 và cứng cả ×1,05 ×1,1025 ×1,157625: sửa trần
--  trong panel rồi chạy lại script này thì nó nắn về con số CŨ, và không
--  có gì báo là hai bên đã lệch nhau.
--
--  Trần của một con đệ = trần đệ thường × he_so của loại nó ÷ 100.
--  Loại nào không có trong `de_tu_loai` thì coi như hệ số 100 (đệ thường).
--
--  ---------------------------------------------------------------------
--  Hình dạng dữ liệu
--  ---------------------------------------------------------------------
--  Đệ tử nằm trong cột player.pet, là một mảng JSON các CHUỖI JSON:
--      $[0] = "[typeDeTu, gender, name, typeFusion, tgFusion, status]"
--      $[1] = "[limitPower, power, tiemNang, stamina, maxStamina,
--               hpg, mpg, dameg, defg, critg, hp, mp, dame]"
--
--  Chỉ số cần chặn nằm ở $[1]: hpg(5) mpg(6) dameg(7) defg(8) critg(9),
--  và hp(10) mp(11) phải hạ theo hpg/mpg, kẻo đệ hiện HP nhiều hơn HP tối đa.
--
--  ---------------------------------------------------------------------
--  CÁCH DÙNG — làm đúng thứ tự
--  ---------------------------------------------------------------------
--   1. SAO LƯU trước:
--        mysqldump -uroot awnv3 player > backup-player.sql
--   2. Chạy phần 0 (đọc trần) rồi phần 1 (xem trước), đối chiếu "sau" với "trước"
--   3. Chạy phần 2 (cập nhật)
--   4. Khởi động lại máy chủ (đệ đang online còn giữ số cũ trong bộ nhớ)
--
--  Nên chạy lúc KHÔNG có ai online: máy chủ tự lưu đệ theo nhịp, đang online
--  mà sửa CSDL thì lần lưu tiếp theo ghi đè lại số cũ.
-- =====================================================================


-- ---------------------------------------------------------------------
--  Phần 0 — Trần đang dùng. Xem cho biết mình sắp nắn về đâu.
-- ---------------------------------------------------------------------
SELECT
    c.max_hp    AS tran_hp_de_thuong,
    c.max_dame  AS tran_dame_de_thuong,
    c.max_giap  AS tran_giap_de_thuong,
    c.max_crit  AS tran_crit_de_thuong
FROM de_tu_chi_so c
WHERE c.id = 1;

SELECT l.loai, l.ten, l.he_so,
       FLOOR(c.max_hp   * l.he_so / 100) AS tran_hp,
       FLOOR(c.max_dame * l.he_so / 100) AS tran_dame,
       FLOOR(c.max_giap * l.he_so / 100) AS tran_giap,
       FLOOR(c.max_crit * l.he_so / 100) AS tran_crit
FROM de_tu_loai l
CROSS JOIN de_tu_chi_so c
WHERE c.id = 1
ORDER BY l.he_so;


-- ---------------------------------------------------------------------
--  Phần 1 — XEM TRƯỚC. Không đổi gì.
-- ---------------------------------------------------------------------
--  LEFT JOIN chứ không phải JOIN: loại đệ lạ (không có trong de_tu_loai)
--  vẫn phải hiện ra để còn nắn, chứ không được rơi âm thầm khỏi danh sách.
-- ---------------------------------------------------------------------
SELECT
    p.id,
    p.name,
    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[0]')), '$[0]') AS loai_de,
    COALESCE(l.ten, '(loại lạ)') AS ten_loai,
    COALESCE(l.he_so, 100)       AS he_so,

    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[5]') AS hpg_truoc,
    LEAST(
        CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[5]') AS UNSIGNED),
        FLOOR(c.max_hp * COALESCE(l.he_so, 100) / 100)
    ) AS hpg_sau,

    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[7]') AS dameg_truoc,
    LEAST(
        CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[7]') AS UNSIGNED),
        FLOOR(c.max_dame * COALESCE(l.he_so, 100) / 100)
    ) AS dameg_sau,

    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[8]') AS giap_truoc,
    LEAST(
        CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[8]') AS UNSIGNED),
        FLOOR(c.max_giap * COALESCE(l.he_so, 100) / 100)
    ) AS giap_sau,

    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[9]') AS crit_truoc,
    LEAST(
        CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')), '$[9]') AS UNSIGNED),
        FLOOR(c.max_crit * COALESCE(l.he_so, 100) / 100)
    ) AS crit_sau
FROM player p
CROSS JOIN de_tu_chi_so c
LEFT JOIN de_tu_loai l
       ON l.loai = CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[0]')), '$[0]') AS SIGNED)
WHERE c.id = 1
  AND p.pet IS NOT NULL
  AND JSON_VALID(p.pet)
  AND JSON_LENGTH(p.pet) >= 2
ORDER BY p.id;


-- ---------------------------------------------------------------------
--  Phần 2 — CẬP NHẬT
-- ---------------------------------------------------------------------
--  JSON_REPLACE ở lớp ngoài nhận một chuỗi VARCHAR nên MariaDB tự bọc lại
--  thành chuỗi JSON có thoát dấu — đúng hình dạng gốc của $[1]. Đừng đổi
--  sang JSON_SET hay CAST(... AS JSON): làm vậy $[1] biến từ CHUỖI thành
--  MẢNG, và bộ đọc của máy chủ (JSONValue.parse trên String.valueOf) sẽ
--  không đọc ra được, đệ tử của người chơi biến mất.
--
--  HP và KI dùng chung trần max_hp: bảng de_tu_chi_so không có cột riêng
--  cho KI. Muốn tách thì thêm cột rồi sửa cả hai chỗ '$[6]' và '$[11]'.
-- ---------------------------------------------------------------------
UPDATE player p
CROSS JOIN de_tu_chi_so c
LEFT JOIN de_tu_loai l
       ON l.loai = CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[0]')), '$[0]') AS SIGNED)
SET p.pet = JSON_REPLACE(
        p.pet,
        '$[1]',
        JSON_REPLACE(
            JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[1]')),
            -- hpg
            '$[5]', LEAST(
                CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[5]') AS UNSIGNED),
                FLOOR(c.max_hp * COALESCE(l.he_so, 100) / 100)),
            -- mpg
            '$[6]', LEAST(
                CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[6]') AS UNSIGNED),
                FLOOR(c.max_hp * COALESCE(l.he_so, 100) / 100)),
            -- dameg
            '$[7]', LEAST(
                CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[7]') AS UNSIGNED),
                FLOOR(c.max_dame * COALESCE(l.he_so, 100) / 100)),
            -- defg
            '$[8]', LEAST(
                CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[8]') AS UNSIGNED),
                FLOOR(c.max_giap * COALESCE(l.he_so, 100) / 100)),
            -- critg
            '$[9]', LEAST(
                CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[9]') AS UNSIGNED),
                FLOOR(c.max_crit * COALESCE(l.he_so, 100) / 100)),
            -- hp hiện tại: không được lớn hơn hpg vừa chặn
            '$[10]', LEAST(
                CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[10]') AS UNSIGNED),
                FLOOR(c.max_hp * COALESCE(l.he_so, 100) / 100)),
            -- mp hiện tại
            '$[11]', LEAST(
                CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[11]') AS UNSIGNED),
                FLOOR(c.max_hp * COALESCE(l.he_so, 100) / 100))
        )
    )
WHERE c.id = 1
  AND p.pet IS NOT NULL
  AND JSON_VALID(p.pet)
  AND JSON_LENGTH(p.pet) >= 2;


-- ---------------------------------------------------------------------
--  Phần 3 — KIỂM LẠI. Phải trả về 0 dòng.
-- ---------------------------------------------------------------------
SELECT p.id, p.name,
    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[5]') AS hpg,
    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[7]') AS dameg,
    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[8]') AS giap,
    JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[9]') AS crit
FROM player p
CROSS JOIN de_tu_chi_so c
LEFT JOIN de_tu_loai l
       ON l.loai = CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[0]')), '$[0]') AS SIGNED)
WHERE c.id = 1
  AND p.pet IS NOT NULL
  AND JSON_VALID(p.pet)
  AND JSON_LENGTH(p.pet) >= 2
  AND (
      CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[5]') AS UNSIGNED)
        > FLOOR(c.max_hp * COALESCE(l.he_so, 100) / 100)
   OR CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[7]') AS UNSIGNED)
        > FLOOR(c.max_dame * COALESCE(l.he_so, 100) / 100)
   OR CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[8]') AS UNSIGNED)
        > FLOOR(c.max_giap * COALESCE(l.he_so, 100) / 100)
   OR CAST(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet,'$[1]')),'$[9]') AS UNSIGNED)
        > FLOOR(c.max_crit * COALESCE(l.he_so, 100) / 100)
  );
