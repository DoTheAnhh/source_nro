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
        int n = Math.min(k.length, 40);
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
            Service.gI().sendMessAllPlayerInMap(pl, msg);
        } catch (Exception ex) {
            Logger.logException(TrangPhucService.class, ex, "Không báo được trang phục chiêu");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
