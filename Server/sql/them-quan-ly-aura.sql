-- =====================================================================
--  Quản lý hào quang (aura)
--
--  Player.getAura() trả về một byte id mà client tự vẽ. Trước đây id đó bị
--  gõ cứng trong mã nguồn theo id cải trang (2013/2014/2015/2059), thêm một
--  cái là phải sửa mã và biên dịch lại.
--
--  Ba bảng/cột:
--    aura                    — danh sách hào quang, có cờ "chỉ dùng cho cải trang"
--    item_template.aura_id   — cải trang này mặc vào thì hiện hào quang nào
--    player.aura_chon        — hào quang người chơi tự chọn
--
--  Thứ tự ưu tiên giữ nguyên như cũ, chỉ thêm bước "tự chọn":
--    cải trang đang mặc -> tự chọn -> thẻ radar -> theo sức mạnh
-- =====================================================================

CREATE TABLE IF NOT EXISTS `aura` (
  `id`            SMALLINT      NOT NULL,
  `ten`           VARCHAR(100)  NOT NULL,
  `cho_cai_trang` TINYINT(1)    NOT NULL DEFAULT 0
      COMMENT '1 = chỉ hiện khi mặc cải trang được gán, không cho tự chọn',
  `ghi_chu`       VARCHAR(255)  NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Các id đang thật sự được dùng trong mã nguồn và bảng radar.
INSERT IGNORE INTO `aura` (`id`, `ten`, `cho_cai_trang`, `ghi_chu`) VALUES
(1,  'Hào quang thẻ radar',      0, 'Thẻ Khủng long, Thẻ Rồng Thần Namếc'),
(6,  'Hào quang sức mạnh 80 tỷ', 0, 'auraPower() tự gán theo sức mạnh'),
(10, 'Hào quang cải trang 2013', 1, 'Trước đây gõ cứng trong getAura()'),
(11, 'Hào quang cải trang 2059', 1, 'Trước đây gõ cứng trong getAura()'),
(14, 'Hào quang cải trang 2014', 1, 'Trước đây gõ cứng trong getAura()'),
(15, 'Hào quang cải trang 2015', 1, 'Trước đây gõ cứng trong getAura()'),
(40, 'Hào quang Oozaru',         0, 'Thẻ Oozaru'),
(41, 'Hào quang Oozaru cấp 2',   0, 'Thẻ Oozaru cấp 2'),
(42, 'Hào quang Oozaru cấp 3',   0, 'Thẻ Oozaru cấp 3'),
(82, 'Hào quang sức mạnh 110 tỷ', 0, 'auraPower()'),
(83, 'Hào quang sức mạnh 120 tỷ', 0, 'auraPower()'),
(84, 'Hào quang sức mạnh 180 tỷ', 0, 'auraPower()');

-- Cải trang nào hiện hào quang nào. -1 = không có.
ALTER TABLE `item_template`
  ADD COLUMN `aura_id` SMALLINT NOT NULL DEFAULT -1
  COMMENT 'Mặc cải trang này thì hiện hào quang có id đó; -1 = không'
  AFTER `dung_duoc`;

-- Giữ đúng hành vi cũ: bốn cải trang từng gõ cứng trong getAura().
UPDATE `item_template` SET `aura_id` = 10 WHERE `id` = 2013;
UPDATE `item_template` SET `aura_id` = 14 WHERE `id` = 2014;
UPDATE `item_template` SET `aura_id` = 15 WHERE `id` = 2015;
UPDATE `item_template` SET `aura_id` = 11 WHERE `id` = 2059;

-- Hào quang người chơi tự chọn. -1 = không chọn gì.
ALTER TABLE `player`
  ADD COLUMN `aura_chon` SMALLINT NOT NULL DEFAULT -1
  COMMENT 'Hào quang người chơi tự chọn; chỉ nhận id có cho_cai_trang = 0';
