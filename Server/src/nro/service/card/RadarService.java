package nro.service.card;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.entity.card.Card;
import nro.entity.card.OptionCard;
import nro.entity.card.RadarCard;
import nro.entity.item.Item;
import nro.entity.player.Player;
import nro.net.io.Message;
import nro.service.Service;
import nro.service.inventory.InventoryService;

/*
 * @author DoTheAnh
 */

/**
 * Sổ sưu tầm (thẻ ra-đa) — gói 127.
 *
 * <h2>Gói tin (máy chủ → client)</h2>
 *
 * <ul>
 *   <li>{@code 0} cả sổ: short số thẻ; mỗi thẻ: short id, short icon, byte hạng,
 *       int đã góp, int cần để lên cấp, byte kiểu hình (0 quái: short id quái /
 *       1 nhân vật: short head, body, leg, bag), UTF tên, UTF mô tả, byte cấp,
 *       byte đang bật, byte cấp tối đa, short hào quang, byte hào quang từ cấp,
 *       short số dòng chỉ số; mỗi dòng: short id chỉ số, int giá trị, byte cấp
 *       có hiệu lực.</li>
 *   <li>{@code 1} bật/tắt: short id, byte bật.</li>
 *   <li>{@code 2} cấp: short id, byte cấp.</li>
 *   <li>{@code 3} tiến độ: short id, int đã góp, int cần.</li>
 *   <li>{@code 4} hào quang: int id nhân vật, short hào quang, byte hiệu ứng.</li>
 * </ul>
 *
 * <p>Số lượng đi {@code int}, id chỉ số đi {@code short}: client của bản này tự
 * đọc gói (bản gốc không có phần đọc), nên không phải giữ khuôn byte cũ — khuôn
 * cũ cắt cụt ở 127 và làm hỏng mọi chỉ số id trên 127.</p>
 */
public class RadarService {

    /**
     * Mẫu thẻ, nạp bởi {@code SoSuuTamDAO.napLai()}.
     *
     * <p>Nạp lại là thay <b>cả danh sách</b> một lượt, không sửa tại chỗ — luồng
     * khác đang duyệt danh sách cũ vẫn duyệt xong bình thường.</p>
     */
    public volatile List<RadarCard> RADAR_TEMPLATE = new ArrayList<>();

    private static RadarService instance;

    public static RadarService gI() {
        if (instance == null) {
            instance = new RadarService();
        }
        return instance;
    }

    /** Mẫu thẻ theo id vật phẩm, hoặc {@code null}. */
    public RadarCard theo(int id) {
        for (RadarCard r : RADAR_TEMPLATE) {
            if (r != null && r.Id == id) {
                return r;
            }
        }
        return null;
    }

    /** Thẻ người chơi đang giữ, hoặc {@code null}. */
    public static Card theCua(Player pl, int id) {
        if (pl == null || pl.Cards == null) {
            return null;
        }
        for (Card c : pl.Cards) {
            if (c != null && c.Id == id) {
                return c;
            }
        }
        return null;
    }

    /** {đã góp, cần} để client vẽ thanh tiến độ. Đầy cấp thì vẽ thanh đầy. */
    private static int[] tienDo(RadarCard mau, Card card) {
        int toiDa = mau.capToiDa();
        int bac = RadarCard.bac(card);
        if (card == null) {
            return new int[]{0, mau.canLenCap(0)};
        }
        if (bac >= toiDa) {
            int can = mau.canLenCap(toiDa - 1);
            return new int[]{can, can};
        }
        int can = mau.canLenCap(bac);
        return new int[]{Math.max(0, Math.min(card.Amount, can)), can};
    }

    // ========== GỬI TOÀN BỘ DANH SÁCH THẺ ==========
    public void sendRadar(Player pl, List<Card> cards) {
        if (pl == null) {
            return;
        }
        List<RadarCard> ds = RADAR_TEMPLATE;
        Message m = null;
        try {
            m = new Message(127);
            m.writer().writeByte(0);
            m.writer().writeShort(ds.size());
            for (RadarCard radar : ds) {
                Card card = theCua(pl, radar.Id);
                int[] td = tienDo(radar, card);
                int toiDa = radar.capToiDa();
                m.writer().writeShort(radar.Id);
                m.writer().writeShort(radar.IconId);
                m.writer().writeByte(radar.Rank);
                m.writer().writeInt(td[0]);
                m.writer().writeInt(td[1]);
                m.writer().writeByte(radar.Type == 1 ? 1 : 0);
                if (radar.Type == 1) {
                    m.writer().writeShort(radar.Head);
                    m.writer().writeShort(radar.Body);
                    m.writer().writeShort(radar.Leg);
                    m.writer().writeShort(radar.Bag);
                } else {
                    m.writer().writeShort(radar.Template);
                }
                m.writer().writeUTF(radar.Name == null ? "" : radar.Name);
                m.writer().writeUTF(radar.Info == null ? "" : radar.Info);
                m.writer().writeByte(card == null ? 0 : card.Level);
                m.writer().writeByte(card == null ? 0 : card.Used);
                m.writer().writeByte(Math.min(127, toiDa));
                m.writer().writeShort(radar.AuraId);
                m.writer().writeByte(Math.max(0, Math.min(127, radar.AuraTuCap)));
                List<OptionCard> ops = radar.Options == null ? new ArrayList<>() : radar.Options;
                m.writer().writeShort(ops.size());
                for (OptionCard option : ops) {
                    m.writer().writeShort(option.id);
                    m.writer().writeInt(option.param);
                    m.writer().writeByte(option.active);
                }
            }
            m.writer().flush();
            pl.sendMessage(m);
        } catch (Exception e) {
            Logger.logException(RadarService.class, e, "Lỗi gửi sổ sưu tầm");
        } finally {
            if (m != null) {
                m.cleanup();
            }
        }
    }

    // ========== GÓP THẺ VÀO SỔ ==========

    /**
     * Người chơi dùng một vật phẩm có trong sổ.
     *
     * <p>Góp <b>cả chồng</b> một lượt, tới đúng số cần để đầy cấp tối đa — không
     * bắt bấm từng món. Món đầu tiên mở khoá thẻ; mỗi lần đủ số lượng của cấp
     * đang lên thì lên một cấp, số dư chuyển sang cấp kế.</p>
     */
    public void suuTam(Player pl, Item item) {
        if (pl == null || item == null || !item.isNotNullItem()) {
            return;
        }
        RadarCard mau = theo(item.template.id);
        if (mau == null) {
            Service.gI().sendThongBao(pl, "Vật phẩm này không có trong Sổ sưu tầm");
            return;
        }
        if (mau.Require != -1) {
            RadarCard truoc = theo(mau.Require);
            Card co = theCua(pl, mau.Require);
            if (truoc != null && (co == null || co.Level == 0
                    || RadarCard.bac(co) < mau.RequireLevel)) {
                Service.gI().sendThongBao(pl, "Bạn cần sưu tầm " + truoc.Name
                        + (mau.RequireLevel > 0 ? " đạt Lv." + mau.RequireLevel : "")
                        + " trước mới dùng được thẻ này");
                return;
            }
        }
        Card card = theCua(pl, mau.Id);
        int toiDa = mau.capToiDa();
        int bacTruoc = RadarCard.bac(card);
        if (card != null && bacTruoc >= toiDa) {
            Service.gI().sendThongBao(pl, mau.Name + " đã đạt cấp tối đa (Lv." + toiDa + ")");
            return;
        }
        long conThieu = card == null ? 0 : -card.Amount;
        for (int b = bacTruoc; b < toiDa; b++) {
            conThieu += mau.canLenCap(b);
        }
        int co = InventoryService.gI().demTongTrongTui(pl, mau.Id);
        if (co <= 0) {
            return;
        }
        int dung = (int) Math.max(1, Math.min(co, conThieu));
        int daTru = InventoryService.gI().truTongTrongTui(pl, mau.Id, dung);
        if (daTru <= 0) {
            return;
        }
        boolean moi = false;
        if (card == null) {
            card = new Card(mau.Id, 0, mau.canLenCap(0), (byte) -1, mau.Options, (byte) 0);
            pl.Cards.add(card);
            moi = true;
        }
        card.Amount += daTru;
        while (RadarCard.bac(card) < toiDa && card.Amount >= mau.canLenCap(RadarCard.bac(card))) {
            card.Amount -= mau.canLenCap(RadarCard.bac(card));
            card.Level = (byte) (RadarCard.bac(card) + 1);
        }
        int bacSau = RadarCard.bac(card);
        if (bacSau >= toiDa) {
            card.Amount = 0;
            card.MaxAmount = mau.canLenCap(toiDa - 1);
        } else {
            card.MaxAmount = mau.canLenCap(bacSau);
        }
        InventoryService.gI().sendItemBag(pl);
        guiTienDo(pl, card, mau);

        if (moi || bacSau != bacTruoc) {
            if (pl.nPoint != null) {
                pl.nPoint.calPoint();
            }
            Service.gI().point(pl);
            if (card.Used == 1) {
                updateAura(pl);
            }
        }
        String bao;
        if (bacSau > bacTruoc) {
            bao = mau.Name + " lên Lv." + bacSau
                    + (bacSau >= toiDa ? " — cấp tối đa!" : " (" + card.Amount + "/"
                    + card.MaxAmount + " lên Lv." + (bacSau + 1) + ")");
        } else if (moi) {
            bao = "Đã thêm " + mau.Name + " vào Sổ sưu tầm (" + card.Amount + "/"
                    + card.MaxAmount + " lên Lv.1)";
        } else {
            bao = "Đã góp " + daTru + " " + mau.Name + " (" + card.Amount + "/"
                    + card.MaxAmount + " lên Lv." + (bacSau + 1) + ")";
        }
        Service.gI().sendThongBao(pl, bao);
    }

    /** Gửi tiến độ và cấp của một thẻ cho client. */
    public void guiTienDo(Player pl, Card card, RadarCard mau) {
        int[] td = tienDo(mau, card);
        RadarSetAmount(pl, card.Id, td[0], td[1]);
        RadarSetLevel(pl, card.Id, card.Level);
    }

    // ========== DÙNG / THÁO CARD ==========
    public void Radar1(Player pl, short id, int use) {
        if (pl == null || pl.Cards == null) {
            return;
        }
        for (Card card : pl.Cards) {
            if (card == null) {
                continue;
            }
            if (card.Id == id) {
                card.Used = (byte) (use == 1 ? 1 : 0);
            } else if (use == 1) {
                // Chi bat duoc mot the: bat the nay la tat the cu.
                card.Used = 0;
            }
        }
        Message message = null;
        try {
            message = new Message(127);
            message.writer().writeByte(1);
            message.writer().writeShort(id);
            message.writer().writeByte(use);
            message.writer().flush();
            pl.sendMessage(message);
        } catch (Exception e) {
            Logger.logException(RadarService.class, e, "Lỗi gửi bật/tắt thẻ");
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
        updateAura(pl);
    }

    // ========== ĐỔI LEVEL CARD ==========
    public void RadarSetLevel(Player pl, int id, int level) {
        Message message = null;
        try {
            message = new Message(127);
            message.writer().writeByte(2);
            message.writer().writeShort(id);
            message.writer().writeByte(level);
            message.writer().flush();
            pl.sendMessage(message);
        } catch (Exception e) {
            Logger.logException(RadarService.class, e, "Lỗi gửi cấp thẻ");
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    // ========== ĐỔI SỐ LƯỢNG CARD ==========
    public void RadarSetAmount(Player pl, int id, int amount, int max_amount) {
        Message message = null;
        try {
            message = new Message(127);
            message.writer().writeByte(3);
            message.writer().writeShort(id);
            message.writer().writeInt(amount);
            message.writer().writeInt(max_amount);
            message.writer().flush();
            pl.sendMessage(message);
        } catch (Exception e) {
            Logger.logException(RadarService.class, e, "Lỗi gửi tiến độ thẻ");
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    // ========== GỬI AURA ==========
    public void sendAura(Player pl, int id_Aura, int id_Eff_Set_Item) {
        Message message = null;
        try {
            message = new Message(127);
            message.writer().writeByte(4);
            message.writer().writeInt((int) pl.id);
            message.writer().writeShort(id_Aura);
            message.writer().writeByte(id_Eff_Set_Item);
            Service.gI().sendMessAllPlayerInMap(pl, message);
        } catch (Exception e) {
            Logger.logException(RadarService.class, e, "Lỗi gửi hào quang");
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    /** Gửi hào quang hiện tại của người chơi cho cả bản đồ; -1 là tắt. */
    public void updateAura(Player pl) {
        if (pl == null) {
            return;
        }
        try {
            sendAura(pl, pl.getAura(), 0);
        } catch (Exception e) {
            Logger.logException(RadarService.class, e, "Lỗi cập nhật hào quang");
        }
    }
}
