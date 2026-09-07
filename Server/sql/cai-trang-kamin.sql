-- =====================================================================
--  Cải Trang Kamin — id part 1956/1957/1958, vat pham 2438
--  Anh: data/icon/x1..x4/17265..17296 (30 tep)
--
--  Id part goc trong part.txt la 2024/2025/2026 — doi thanh 1956/1957/1958 vi
--  client nhan dien part theo THU TU trong data/update_data/part.
--  Xem docs/07-THEM-CAI-TRANG.md.
--
--  BO RES THIEU 2 ANH: 17275, 17281 — khong co tep, cung khong ton tai
--  o dau trong game. Cac frame do thay bang 2955 (anh trong) de
--  khong tao tham chieu treo; co tep that thi doi lai.
--
--  Bo res nay chi co MOT anh du (17296, xem duoi) nen dung lam ca icon
--  tui lan avatar. Game da co 18 cai trang dung icon lon hon 40px
--  (to nhat 100x86) nen co nay chap nhan duoc.
-- =====================================================================

-- ---- 1. Ba part: mu (type 0), ao (type 1), quan (type 2)
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1956, 0, '[[17265,2,-3],[17266,2,-1],[2955,0,0]]'),
(1957, 1, '[[17267,0,-1],[17268,-1,-5],[17269,0,-5],[17270,-1,-5],[17271,0,-6],[17272,1,-5],[17273,1,-4],[17274,1,-1],[2955,0,-3],[17276,-1,-4],[17277,1,-2],[17278,0,-1],[17279,0,0],[17280,2,-4],[2955,-1,-4],[17282,-1,-4],[2955,0,0]]'),
(1958, 2, '[[17283,1,5],[17284,-1,-3],[17285,0,0],[17286,0,0],[17287,0,0],[17288,0,0],[17289,0,0],[17290,1,2],[17291,1,2],[17292,0,0],[17293,3,-1],[17294,0,2],[17295,0,0],[2955,0,0]]');

-- ---- 2. Vat pham cai trang
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2438, 5, 3, 'Cải Trang Kamin',
  'Nữ chiến binh Neo Machine — tái sinh không giới hạn.',
  1, 17296, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1956, 1957, 1958, 0, -1);

-- ---- 3. Avatar (bang head_avatar map id part MU -> id anh)
INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1956, 17296);

-- ---- 4. Tang phien ban du lieu de client tai lai
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
