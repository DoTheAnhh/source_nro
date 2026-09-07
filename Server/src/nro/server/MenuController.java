package nro.server;

import java.io.IOException;
import nro.core.consts.ConstNpc;
import nro.entity.npc.Npc;
import nro.service.npc.NpcManager;
import nro.entity.player.Player;
import nro.service.Service;
import nro.service.fun.TransactionService;
import nro.net.session.MySession;

public class MenuController {

    private static MenuController instance;

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new MenuController();
        }
        return instance;
    }

    public void openMenuNPC(MySession session, int idnpc, Player player) {
        TransactionService.gI().cancelTrade(player);
        if(player == null || player.zone == null){
            Service.gI().sendThongBao(player, "...!");
            return;
        }
        Npc npc;
        if (idnpc == ConstNpc.CALICK && player.zone.map.mapId != 102) {
            npc = NpcManager.getNpc(ConstNpc.CALICK);
        } else if (idnpc == ConstNpc.LY_TIEU_NUONG) {
            npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
        } else {
            npc = player.zone.map.getNpc(player, idnpc);
        }
        if (npc != null) {
            npc.openBaseMenu(player);
        } else {
            Service.gI().hideWaitDialog(player);
        }
    }

    public void doSelectMenu(Player player, int npcId, int select) throws IOException {
        TransactionService.gI().cancelTrade(player);
        // Nút THÊM của panel nối vào cuối menu gốc, nên vị trí vượt quá số mục
        // gốc chắc chắn là của chúng. Xử lý ở đây rồi dừng — đừng để rơi xuống
        // confirmMenu của NPC, vì switch trong đó không biết những vị trí này.
        if (player != null && player.menuNutThem != null
                && !player.menuNutThem.isEmpty()
                && select >= player.menuGocSoLuong) {
            int i = select - player.menuGocSoLuong;
            if (i < player.menuNutThem.size()) {
                nro.repository.dao.NpcMenuThemDAO.chay(player,
                        player.menuNutThem.get(i));
                return;
            }
        }
        switch (npcId) {

            case ConstNpc.RONG_THIENG:
            case ConstNpc.CON_MEO:
                NpcManager.getNpc((byte) npcId).confirmMenu(player, select);
                break;
            default:
                Npc npc = null;
                if (npcId == ConstNpc.CALICK && player.zone.map.mapId != 102) {
                    npc = NpcManager.getNpc(ConstNpc.CALICK);
                } else if (npcId == ConstNpc.LY_TIEU_NUONG) {
                    npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
                } else if (player.zone != null) {
                    npc = player.zone.map.getNpc(player, npcId);
                }
                if (npc != null) {
                    npc.confirmMenu(player, select);
                } else {
                    Service.gI().hideWaitDialog(player);
                }
                break;
        }
    }
}
