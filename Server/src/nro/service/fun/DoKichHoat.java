package nro.service.fun;

import java.util.List;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.repository.dao.SetBonusDAO;
import nro.repository.dao.TiLeKichHoatDAO;
import nro.server.Manager;
import nro.service.item.ItemService;

/**
 * Phát đồ set kích hoạt từ hộp / capsule — <b>cùng một luật với nâng Huỷ Diệt</b>
 * (xem {@code NangCapKichHoat}), đọc hết từ panel.
 *
 * <ul>
 * <li><b>Bậc đồ</b> bốc theo trọng số ở tab tỉ lệ set kích hoạt
 * ({@link TiLeKichHoatDAO#bocBac()}); bậc là chỉ số trong
 * {@code Manager.doSKHVip} / {@code Manager.radaSKHVip}.</li>
 * <li><b>Set</b> bốc trong các set đang bật của đúng hệ người chơi, gắn
 * <b>đủ</b> option nhận diện của set kèm dòng mô tả — gắn thiếu là món đồ
 * không tính vào set nào.</li>
 * </ul>
 *
 * <p>Bản cũ viết cứng mẫu đồ nên Hộp quà set kích hoạt chỉ ra đúng bộ vải thô,
 * và chỉ gắn hai option đầu của set.</p>
 */
public final class DoKichHoat {

    private DoKichHoat() {
    }

    /** Tên năm ô, theo thứ tự chỉ số ô dùng ở đây. */
    public static final String[] TEN_O = {"Áo", "Quần", "Găng", "Giày", "Rađa"};

    /** Số ô: áo, quần, găng, giày, rađa. */
    public static final int SO_O = 5;

    /**
     * Mẫu đồ ô {@code o} (0 áo … 3 giày, 4 rađa), bậc {@code bac} của hệ
     * {@code gender}; -1 nếu không có.
     */
    public static short mauDo(int gender, int o, int bac) {
        if (gender < 0 || gender >= Manager.doSKHVip.length) {
            gender = 0;
        }
        short[][] cuaHe = Manager.doSKHVip[gender];
        short[] day = o < cuaHe.length ? cuaHe[o] : Manager.radaSKHVip;
        if (day == null || day.length == 0) {
            return -1;
        }
        return day[Math.min(Math.max(bac, 0), day.length - 1)];
    }

    /** Option của một set bốc ngẫu nhiên hợp hệ (đủ bộ, kèm mô tả); null nếu panel chưa khai set nào. */
    public static int[] optionSet(int gender) {
        int[] m = SetBonusDAO.optionSetMoi(Integer.MAX_VALUE, gender);
        return m == null || m.length == 0 ? null : SetBonusDAO.kemMoTa(m);
    }

    /**
     * Dựng một món: chỉ số gốc của mẫu, option set, khoá giao dịch, rồi các
     * dòng thêm (sao pha lê, "capsule"…).
     */
    public static Item tao(short tempId, int[] opSet, int[]... them) {
        Item it = ItemService.gI().createNewItem(tempId);
        List<ItemOption> goc = ItemService.gI().getListOptionItemShop(tempId);
        if (!goc.isEmpty()) {
            it.itemOptions.addAll(goc);
        } else {
            nro.service.reward.RewardService.gI().initBaseOptionClothes(it.template.id, it.template.type,
                    it.itemOptions);
        }
        for (int op : opSet) {
            it.itemOptions.add(new ItemOption(op, 0));
        }
        it.itemOptions.add(new ItemOption(30, 0));
        for (int[] d : them) {
            it.itemOptions.add(new ItemOption(d[0], d[1]));
        }
        return it;
    }

    /** Báo khi panel chưa khai set cho hệ này — không trừ vật phẩm. */
    public static void baoThieuSet(Player pl) {
        nro.service.Service.gI().sendThongBao(pl, "Chưa khai set kích hoạt nào cho hệ của bạn trên panel"
                + " — báo quản trị.");
    }

    /** Bậc đồ theo panel. */
    public static int bocBac() {
        return TiLeKichHoatDAO.bocBac();
    }
}
