package nro.entity.npc.list;

/**
 * @author DoTheAnh
 */

import nro.core.consts.ConstNpc;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.npc.Npc;
import nro.entity.player.Player;
import nro.repository.dao.ConfigDAO;
import nro.service.Service;
import nro.service.TaskService;
import nro.service.fun.ChangeMapService;
import nro.service.inventory.InventoryService;
import nro.service.shop.ShopService;

/**
 * NPC ở Đảo Kamê — đổi <b>điểm săn boss</b> lấy vật phẩm.
 *
 * <p>Sự kiện Tranh Ngọc Namếc đã bỏ hẳn (không còn đăng ký, không còn bảng xếp
 * hạng). Điểm giờ kiếm bằng cách hạ boss có máu tối đa từ ngưỡng quy ước trở
 * lên — xem {@code Boss.congDiemSanBoss}. Điểm lưu chung cột với điểm chiến
 * trường Namếc cũ, nên ai còn điểm cũ vẫn dùng được.</p>
 */
public class TranhNgocNamek extends Npc {

    public TranhNgocNamek(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        long nguong = ConfigDAO.num(ConfigDAO.SAN_BOSS_HP_TOI_THIEU, 500_000_000L);
        long diem = ConfigDAO.num(ConfigDAO.SAN_BOSS_DIEM, 1L);
        int co = player.event.getNamekWarPoint();
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "|7|SĂN BOSS ĐỔI QUÀ\n"
                + "|2|Hạ boss có từ " + Util.soCham(nguong) + " HP trở lên"
                + " nhận " + diem + " điểm săn boss\n"
                + "|6|Điểm hiện có: " + Util.soCham(co),
                "Đổi điểm\nThưởng\n[" + Util.soCham(co) + "]", "Hành tinh Cereal", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || !player.iDMark.isBaseMenu()) {
            return;
        }
        switch (select) {
            case 0:
                ShopService.gI().opendShop(player, "SHOP_NAMEK_WAR", false);
                break;
            case 1: {
                if (TaskService.gI().getIdTask(player) < 28) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ 28 mới có thể đến Hành tinh Cereal!");
                    break;
                }
                Item thoiVang = InventoryService.gI().findItemBag(player, 457);
                if (thoiVang == null || thoiVang.quantity < 5) {
                    Service.gI().sendThongBao(player, "Bạn cần 5 Thỏi vàng để vào Hành tinh Cereal!");
                    break;
                }
                InventoryService.gI().subQuantityItemsBag(player, thoiVang, 5);
                InventoryService.gI().sendItemBag(player);
                ChangeMapService.gI().changeMapNonSpaceship(player, 194, 200 + Util.nextInt(-100, 100), 192);
                break;
            }
            default:
                break;
        }
    }
}
