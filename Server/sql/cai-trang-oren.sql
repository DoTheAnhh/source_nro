-- =====================================================================
--  Cải Trang Oren — id part 1959/1960/1961, vat pham 2439
--  Anh: data/icon/x1..x4/17297..17328 (32 tep)
--
--  Id part goc trong part.txt la 2027/2028/2029 — doi thanh 1959/1960/1961 vi
--  client nhan dien part theo THU TU trong data/update_data/part.
--  Xem docs/07-THEM-CAI-TRANG.md.
--
--  Bo res nay chi co MOT anh du (17328, xem duoi) nen dung lam ca icon
--  tui lan avatar. Game da co 18 cai trang dung icon lon hon 40px
--  (to nhat 100x86) nen co nay chap nhan duoc.
-- =====================================================================

-- ---- 1. Ba part: mu (type 0), ao (type 1), quan (type 2)
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(1959, 0, '[[17297,4,-5],[17298,5,-1],[2955,0,0]]'),
(1960, 1, '[[17299,0,0],[17300,-2,-5],[17301,0,-4],[17302,0,-4],[17303,0,-5],[17304,1,-4],[17305,1,-4],[17306,0,-1],[17307,-1,-3],[17308,0,-3],[17309,1,-1],[17310,1,-1],[17311,0,0],[17312,3,-4],[17313,-2,-4],[17314,-1,-2],[2955,0,0]]'),
(1961, 2, '[[17315,4,5],[17316,-1,-3],[17317,0,0],[17318,2,0],[17319,0,0],[17320,0,1],[17321,1,0],[17322,0,1],[17323,0,2],[17324,-1,-1],[17325,1,0],[17326,0,2],[17327,0,0],[2955,0,0]]');

-- ---- 2. Vat pham cai trang
INSERT INTO `item_template`
 (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`,
  `is_up_to_up`, `power_require`, `gold`, `gold_sell`, `gem`, `gem_sell`,
  `ruby`, `ruby_sell`, `head`, `body`, `leg`, `TypeEvent`, `isGender`)
VALUES
 (2439, 5, 3, 'Cải Trang Oren',
  'Chiến binh Neo Machine — thân xác thép, ý chí sắt.',
  1, 17328, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1959, 1960, 1961, 0, -1);

-- ---- 3. Avatar (bang head_avatar map id part MU -> id anh)
INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (1959, 17328);

-- ---- 4. Tang phien ban du lieu de client tai lai
-- Chia lay du 128 vi DataGame.vs() ep ve mot byte co dau (0..127).
UPDATE `panel_config` SET `v` = (`v` + 1) % 128 WHERE `k` IN ('vs_data', 'vs_item');
