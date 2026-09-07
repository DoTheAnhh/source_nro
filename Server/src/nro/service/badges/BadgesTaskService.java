package nro.service.badges;

import nro.entity.badges.BadgesData;
import nro.entity.badges.BadgesTask;
import nro.entity.badges.BadgesTaskTemplate;
import nro.entity.player.Player;
import nro.server.Manager;

/*
 * Author Dev By DoTheAnh
 */
public class BadgesTaskService {

    public static void createAndResetTask(Player player) { // xử lý nếu chưa có nhiệm vụ hoặc reset hằng ngày.
        player.dataTaskBadges.clear();

        for (BadgesTaskTemplate BTT : Manager.TASKS_BADGES_TEMPLATE) {
            BadgesTask data = new BadgesTask();
            data.id = BTT.id;
            data.count = 0;
            data.countMax = BTT.count;
            data.idBadgesReward = BTT.idbadgesReward;
            player.dataTaskBadges.add(data);
        }
    }

    public static void updateDoneTask(Player player) { // xử lý nếu hoàn thành nhiệm vụ tự add vào người
        for (BadgesTask data : player.dataTaskBadges) {
            if (data.isDone()) {
                for (BadgesData bg : player.dataBadges) {
                    if (bg.idBadGes == data.idBadgesReward) {
                        return;
                    }
                }

                BadgesData danhHieu = new BadgesData(player, data.idBadgesReward, 30);
                player.dataBadges.add(danhHieu);
                data.count = 0;
            }
        }
    }

    /**
     * Cộng tiến độ cho <b>mọi</b> nhiệm vụ khớp loại — đường tự đếm.
     *
     * <h3>Vì sao cần</h3>
     *
     * <p>Cách cũ là mỗi chỗ trong mã gọi thẳng
     * {@code updateCountBagesTask(pl, ConstTaskBadges.TRUM_SAN_BOSS, 1)} — id
     * nhiệm vụ <b>gõ cứng</b>, nên panel không tạo được nhiệm vụ mới, chỉ sửa
     * được chữ của những nhiệm vụ đã viết sẵn.</p>
     *
     * <p>Hàm này tra theo <i>loại</i> và <i>tham số</i> trong bản mẫu, nên thêm
     * một dòng trên panel là có nhiệm vụ mới chạy được ngay, không phải sửa mã.</p>
     *
     * @param thamSo id quái / boss / vật phẩm vừa xảy ra; nhiệm vụ để {@code -1}
     *               thì cái nào cũng tính
     */
    public static void tangTheoLoai(Player player, String loai, int thamSo, int amount) {
        if (player == null || player.dataTaskBadges == null
                || loai == null || amount <= 0) {
            return;
        }
        try {
            boolean coTang = false;
            for (BadgesTaskTemplate t : Manager.TASKS_BADGES_TEMPLATE) {
                if (!loai.equals(t.loai)) {
                    continue;
                }
                // thamSo < 0 trong ban mau = "bat ky", khong loc theo id.
                if (t.thamSo >= 0 && t.thamSo != thamSo) {
                    continue;
                }
                updateCountBagesTask(player, t.id, amount);
                coTang = true;
            }
            if (coTang) {
                updateDoneTask(player);
            }
        } catch (Exception ex) {
            // Dem nhiem vu la phan phu, khong duoc phep lam hong vong danh quai.
        }
    }

    public static void updateCountBagesTask(Player player, int id, int amount) {
        for (BadgesTask data : player.dataTaskBadges) {
            if (data.id == id) {
                data.count += amount;

                if (data.count > data.countMax) {
                    data.count = data.countMax;
                }

                break;
            }
        }
    }

    public static int sendPercenBadgesTask(Player player, int idBadgesReward) {
        for (BadgesTask data : player.dataTaskBadges) {
            if (data.idBadgesReward == idBadgesReward) {
                if (data.getPercentProcess() > 0) {
                    return data.getPercentProcess();
                } else {
                    return 0;
                }
            }
        }

        return 0;
    }

    public static int sendDay(Player player, int id) {
        for (BadgesData data : player.dataBadges) {
            if (data.idBadGes == id) {
                long timeDifference = data.timeofUseBadges - System.currentTimeMillis();
                return (int) (timeDifference / (24 * 60 * 60 * 1000L));
            }
        }

        return 0;
    }
}