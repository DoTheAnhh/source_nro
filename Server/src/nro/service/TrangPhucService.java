package nro.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.entity.player.Player;
import nro.entity.skill.NClass;
import nro.entity.template.SkillTemplate;
import nro.net.io.Message;
import nro.repository.dao.TrangPhucDAO;
import nro.server.Manager;

/**
 * Gói <b>106</b> — Trang phục kỹ năng (113 là hiệu ứng bản đồ của game gốc, không dùng được). Xem {@link TrangPhucDAO}.
 *
 * <pre>
 * client → máy chủ
 *   byte 0                          xin danh sách
 *   byte 1, short skillTpl, short mauId   bật (mauId &lt;= 0: về hình gốc)
 *
 * máy chủ → client
 *   byte 0, byte nChieu, { short skillTpl, UTF tenChieu, short iconChieu,
 *            short dangDung, byte nMau, { short id, UTF ten, UTF moTa, short icon,
 *            byte nNap, short[nNap], byte nBay, short[nBay], byte daCo } }
 *   byte 2, int charId, short skillTpl, byte nNap, short[nNap], byte nBay, short[nBay]
 *            — gửi cả khu ngay trước lúc một người tụ chiêu; nNap = nBay = 0
 *            là dùng hình gốc.
 * </pre>
 */
public final class TrangPhucService {

    public static final int GOI = 106;

    private static final TrangPhucService I = new TrangPhucService();

    public static TrangPhucService gI() {
        return I;
    }

    private TrangPhucService() {
    }

    public void nhanGoi(Player pl, Message msg) {
        if (pl == null || msg == null || !pl.isPl()) {
            return;
        }
        try {
            int viec = msg.reader().readByte();
            if (viec == 0) {
                guiDanhSach(pl);
            } else if (viec == 1) {
                int skillTpl = msg.reader().readShort();
                int mauId = msg.reader().readShort();
                chon(pl, skillTpl, mauId);
            }
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Lỗi gói trang phục");
        }
    }

    private void chon(Player pl, int skillTpl, int mauId) {
        if (mauId > 0) {
            TrangPhucDAO.Mau m = TrangPhucDAO.mau(mauId);
            if (m == null || !m.bat || m.skillTpl != skillTpl) {
                Service.gI().sendThongBao(pl, "Trang phục này hiện không dùng được.");
                return;
            }
            if (!TrangPhucDAO.coSoHuu(pl.id, mauId)) {
                Service.gI().sendThongBao(pl, "Bạn chưa có trang phục " + m.ten + " — mở được từ Rương Cao Cấp.");
                return;
            }
            TrangPhucDAO.chon(pl.id, skillTpl, mauId);
            Service.gI().sendThongBao(pl, "Đã bật trang phục " + m.ten + ".");
        } else {
            TrangPhucDAO.chon(pl.id, skillTpl, 0);
            Service.gI().sendThongBao(pl, "Đã về hình gốc.");
        }
        guiDanhSach(pl);
    }

    /**
     * Dùng vật phẩm "Trang phục: …" — mở khoá trang phục đó vĩnh viễn. Đã có
     * rồi thì báo và giữ vật phẩm. Trả true nếu đây là vật phẩm trang phục
     * (đã xử lý), false để UseItem đi tiếp đường thường.
     */
    public boolean dungVatPham(Player pl, nro.entity.item.Item item) {
        if (pl == null || item == null || !item.isNotNullItem()) {
            return false;
        }
        TrangPhucDAO.Mau m = TrangPhucDAO.mauTheoVatPham(item.template.id);
        if (m == null) {
            return false;
        }
        if (TrangPhucDAO.coSoHuu(pl.id, m.id)) {
            Service.gI().sendThongBao(pl, "Bạn đã có trang phục " + m.ten + " rồi.");
            return true;
        }
        if (TrangPhucDAO.themSoHuu(pl.id, m.id)) {
            nro.service.inventory.InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            nro.service.inventory.InventoryService.gI().sendItemBag(pl);
            Service.gI().sendThongBao(pl, "Đã mở khoá trang phục " + m.ten
                    + "! Vào Túi → Chức năng → Hệ thống → Trang phục để bật.");
            guiDanhSach(pl);
        }
        return true;
    }

    private static SkillTemplate timChieu(int tpl) {
        for (NClass nc : Manager.NCLASS) {
            SkillTemplate t = nc.getSkillTemplate(tpl);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public void guiDanhSach(Player pl) {
        Message msg = null;
        try {
            Map<Integer, List<TrangPhucDAO.Mau>> theoChieu = new LinkedHashMap<>();
            for (TrangPhucDAO.Mau m : TrangPhucDAO.tatCa()) {
                if (m.bat) {
                    theoChieu.computeIfAbsent(m.skillTpl, k -> new ArrayList<>()).add(m);
                }
            }
            msg = new Message(GOI);
            msg.writer().writeByte(0);
            msg.writer().writeByte(theoChieu.size());
            for (Map.Entry<Integer, List<TrangPhucDAO.Mau>> e : theoChieu.entrySet()) {
                int tpl = e.getKey();
                SkillTemplate t = timChieu(tpl);
                TrangPhucDAO.Mau dung = TrangPhucDAO.dangDung(pl.id, tpl);
                msg.writer().writeShort(tpl);
                msg.writer().writeUTF(t != null ? t.name : "Kỹ năng " + tpl);
                msg.writer().writeShort(t != null ? t.iconId : -1);
                msg.writer().writeShort(dung != null ? dung.id : 0);
                msg.writer().writeByte(e.getValue().size());
                for (TrangPhucDAO.Mau m : e.getValue()) {
                    msg.writer().writeShort(m.id);
                    msg.writer().writeUTF(m.ten);
                    msg.writer().writeUTF(m.moTa);
                    msg.writer().writeShort(m.icon);
                    ghiKhung(msg, m.khungNap);
                    ghiKhung(msg, m.khungBay);
                    msg.writer().writeByte(TrangPhucDAO.coSoHuu(pl.id, m.id) ? 1 : 0);
                }
            }
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Không gửi được danh sách trang phục");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private static void ghiKhung(Message msg, short[] k) throws java.io.IOException {
        int n = Math.min(k.length, 255);
        msg.writer().writeByte(n);
        for (int i = 0; i < n; i++) {
            msg.writer().writeShort(k[i]);
        }
    }

    /**
     * Báo cả khu hình chiêu của người này — gọi ngay TRƯỚC gói tụ chiêu, để
     * client đã biết dùng hình nào khi bắt đầu vẽ. Quái / boss / đệ tử không
     * gửi gì (luôn hình gốc).
     */
    public void baoTruocKhiTu(Player pl, int skillTpl) {
        bao(pl, skillTpl, null, false);
    }

    /** Chỉ báo cho người khác trong bản đồ (người này vào bản đồ, mang theo trứng đang nở). */
    public void baoNguoiKhac(Player pl, int skillTpl) {
        bao(pl, skillTpl, null, true);
    }

    /** Chỉ báo cho một người (người ấy vừa vào bản đồ, thấy trứng của {@code pl}). */
    public void baoCho(Player nhan, Player pl, int skillTpl) {
        if (nhan != null) {
            bao(pl, skillTpl, nhan, false);
        }
    }

    /**
     * Thôi miên trúng {@code nan}: người thi triển có skin Tsukuyomi thì báo riêng
     * cho nạn nhân — client họ phủ ảo cảnh kín màn hình trong {@code ms} mili giây.
     * Gói: byte 3, short skillTpl, int ms, byte n, short[n].
     */
    public void thoiMien(Player tu, Player nan, int ms) {
        phuManHinh(tu, nan, nro.entity.skill.Skill.THOI_MIEN, ms);
    }

    /**
     * Chiêu có skin phủ màn hình nạn nhân (Tsukuyomi, Say Cheese…): báo riêng nạn nhân khung
     * của skin và thời gian. Gói: byte 3, short skillTpl, int ms, byte n, short[n].
     */
    public void phuManHinh(Player tu, Player nan, int skillTpl, int ms) {
        if (tu == null || nan == null || !nan.isPl() || ms <= 0) {
            return;
        }
        Message msg = null;
        try {
            TrangPhucDAO.Mau m = TrangPhucDAO.dangDung(tu.id, skillTpl);
            if (m == null || m.khungBay == null || m.khungBay.length == 0) {
                return;
            }
            msg = new Message(GOI);
            msg.writer().writeByte(3);
            msg.writer().writeShort(skillTpl);
            msg.writer().writeInt(ms);
            ghiKhung(msg, m.khungBay);
            nan.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Không báo được ảo cảnh thôi miên");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Người có skin của chiêu này vừa dùng chiêu: cả khu nghe tiếng của skin
     * (một lần). Gói: byte 5, int charId, short skillTpl, int ms (thời gian chiêu, để tiếng
     * theo sau kéo vừa hết chiêu).
     */
    public void amThanh(Player pl, int skillTpl, int ms) {
        if (pl == null || !pl.isPl() || pl.zone == null) {
            return;
        }
        Message msg = null;
        try {
            if (TrangPhucDAO.dangDung(pl.id, skillTpl) == null) {
                return;
            }
            msg = new Message(GOI);
            msg.writer().writeByte(5);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(skillTpl);
            msg.writer().writeInt(ms);
            Service.gI().sendMessAllPlayerInMap(pl, msg);
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Không báo được tiếng trang phục");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Skin Trói Hồn: mục tiêu ({@code loai} 1 người / 0 quái, {@code id}) bị trói {@code ms} mili
     * giây — cả khu vẽ xích quấn quanh nó (thay cái bình). Gói: byte 6, byte loai, int id, int ms,
     * int tuId, byte n, short[n] (khung skin của người dùng chiêu).
     */
    public void troiHon(Player tu, int loai, long id, int ms) {
        if (tu == null || tu.zone == null) {
            return;
        }
        Message msg = null;
        try {
            TrangPhucDAO.Mau m = TrangPhucDAO.dangDung(tu.id, nro.entity.skill.Skill.MA_PHONG_BA);
            if (m == null) {
                return;
            }
            msg = new Message(GOI);
            msg.writer().writeByte(6);
            msg.writer().writeByte(loai);
            msg.writer().writeInt((int) id);
            msg.writer().writeInt(ms);
            msg.writer().writeInt((int) tu.id);
            ghiKhung(msg, m.khungBay);
            Service.gI().sendMessAllPlayerInMap(tu, msg);
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Không báo được trói hồn");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /** Hết thôi miên (tỉnh sớm hay hết giờ): tắt ảo cảnh. Gói: byte 4. */
    public void hetThoiMien(Player nan) {
        if (nan == null || !nan.isPl()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(GOI);
            msg.writer().writeByte(4);
            nan.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Không tắt được ảo cảnh thôi miên");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void bao(Player pl, int skillTpl, Player nhan, boolean truMinh) {
        if (pl == null || !pl.isPl() || pl.zone == null) {
            return;
        }
        Message msg = null;
        try {
            TrangPhucDAO.Mau m = TrangPhucDAO.dangDung(pl.id, skillTpl);
            msg = new Message(GOI);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(skillTpl);
            ghiKhung(msg, m != null ? m.khungNap : new short[0]);
            ghiKhung(msg, m != null ? m.khungBay : new short[0]);
            if (nhan != null) {
                nhan.sendMessage(msg);
            } else if (truMinh) {
                Service.gI().sendMessAnotherNotMeInMap(pl, msg);
            } else {
                Service.gI().sendMessAllPlayerInMap(pl, msg);
            }
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Không báo được trang phục chiêu");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
