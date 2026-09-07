package nro.service.badges;

import java.util.ArrayList;
import java.util.List;
import nro.entity.badges.BadgesData;
import nro.entity.badges.BagesTemplate;
import nro.entity.player.Player;
import nro.server.Manager;
import nro.service.Service;

/*
 * Author Dev By DoTheAnh
 */
public class BadgesService {

    /** Bật một danh hiệu, tự tắt các danh hiệu khác. */
    public static void turnOnBadges(Player player, int id) {
        if (player.dataBadges != null) {
            for (BadgesData data : player.dataBadges) {
                if (data.idBadGes == id) {
                    data.isUse = true;
                } else {
                    data.isUse = false;
                }
            }
        }
    }

    /**
     * Tắt hết danh hiệu — người chơi không đeo cái nào.
     *
     * <p>Trước đây không có đường nào tắt: {@link #turnOnBadges} luôn bật đúng
     * một cái, nên đã đeo là không gỡ ra được nữa.</p>
     */
    public static void tatHet(Player player) {
        if (player.dataBadges == null) {
            return;
        }
        for (BadgesData d : player.dataBadges) {
            d.isUse = false;
        }
    }

    /** Danh hiệu đang đeo, hoặc {@code null}. */
    public static BadgesData dangDeo(Player player) {
        if (player.dataBadges == null) {
            return null;
        }
        for (BadgesData d : player.dataBadges) {
            if (d.isUse) {
                return d;
            }
        }
        return null;
    }

    /** Người chơi đã có danh hiệu {@code idEffect} chưa. */
    public static BadgesData timTheoId(Player player, int idEffect) {
        if (player.dataBadges == null) {
            return null;
        }
        for (BadgesData d : player.dataBadges) {
            if (d.idBadGes == idEffect) {
                return d;
            }
        }
        return null;
    }

    /**
     * Cấp danh hiệu cho người chơi.
     *
     * <p>Đã có sẵn thì <b>cộng thêm hạn</b> chứ không tạo dòng thứ hai — hai
     * dòng cùng một danh hiệu làm {@code BagesTemplate.sendListItemOption()}
     * đọc nhầm và chỉ số cộng lung tung.</p>
     *
     * @param soNgay số ngày; {@code <= 0} nghĩa là <b>vĩnh viễn</b>
     * @param deoLuon bật luôn danh hiệu vừa cấp
     */
    public static void cap(Player player, int idEffect, int soNgay, boolean deoLuon) {
        if (player == null) {
            return;
        }
        if (player.dataBadges == null) {
            player.dataBadges = new ArrayList<>();
        }
        long han = soNgay <= 0 ? -1
                : System.currentTimeMillis() + soNgay * 24L * 60L * 60L * 1000L;

        BadgesData d = timTheoId(player, idEffect);
        if (d == null) {
            d = new BadgesData(idEffect, han, false);
            player.dataBadges.add(d);
        } else if (han == -1 || d.timeofUseBadges == -1) {
            // Một trong hai là vĩnh viễn -> giữ vĩnh viễn.
            d.timeofUseBadges = -1;
        } else {
            // Cộng dồn phần còn lại, không ghi đè: cấp thêm 7 ngày cho người
            // còn 3 ngày phải thành 10, chứ không phải tụt xuống 7.
            long conLai = Math.max(0, d.timeofUseBadges - System.currentTimeMillis());
            d.timeofUseBadges = System.currentTimeMillis() + conLai
                    + soNgay * 24L * 60L * 60L * 1000L;
        }
        if (deoLuon) {
            turnOnBadges(player, idEffect);
        }
        capNhat(player);
    }

    /** Thu hồi một danh hiệu. */
    public static boolean thuHoi(Player player, int idEffect) {
        if (player == null || player.dataBadges == null) {
            return false;
        }
        boolean bo = player.dataBadges.removeIf(d -> d.idBadGes == idEffect);
        if (bo) {
            capNhat(player);
        }
        return bo;
    }

    /**
     * Bỏ các danh hiệu đã hết hạn.
     *
     * @return số danh hiệu vừa bỏ
     */
    public static int donHetHan(Player player) {
        if (player == null || player.dataBadges == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        int truoc = player.dataBadges.size();
        player.dataBadges.removeIf(d -> d.timeofUseBadges > 0 && d.timeofUseBadges <= now);
        int bo = truoc - player.dataBadges.size();
        if (bo > 0) {
            capNhat(player);
        }
        return bo;
    }

    /** Tên danh hiệu theo id hiệu ứng. */
    public static String ten(int idEffect) {
        for (BagesTemplate t : Manager.BAGES_TEMPLATES) {
            if (t.idEffect == idEffect) {
                return t.NAME;
            }
        }
        return "#" + idEffect;
    }

    /** Danh sách danh hiệu người chơi đang có, đã bỏ cái hết hạn. */
    public static List<BadgesData> danhSachCua(Player player) {
        donHetHan(player);
        return player.dataBadges == null ? new ArrayList<>() : player.dataBadges;
    }

    /**
     * Vẽ lại người chơi sau khi đổi danh hiệu.
     *
     * <p>Danh hiệu cộng chỉ số qua {@code BagesTemplate.sendListItemOption()},
     * nên phải tính lại điểm rồi gửi — không thì chỉ số cũ giữ nguyên tới lần
     * đổi map sau.</p>
     */
    private static void capNhat(Player player) {
        try {
            if (player.nPoint != null) {
                player.nPoint.calPoint();
            }
            Service.gI().point(player);
        } catch (Exception ignored) {
            // Cap danh hieu tu panel co the goi khi nguoi choi dang offline.
        }
    }
}
