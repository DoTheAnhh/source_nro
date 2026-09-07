package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.server.ServerNotify;
import nro.service.Service;
import nro.core.util.Util;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.item.ItemService;
import nro.service.combine.CombineService;
import nro.entity.combine.CombineUtil;
import nro.entity.player.Player;

public class NangCapChanMenh {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendDialogMessage(player, "Cần 1 Chân thiên tử và Tinh thể và Ma quái!");
            return;
        }

        Item TinhThe = null;
        Item MaQuai = null;
        Item ChanMenh = null;
        int level = 0;

        for (Item item : player.combine.itemsCombine) {
            if (item.template.id == 2011) {
                TinhThe = item;
            } else if (item.template.id == 2012) {
                MaQuai = item;
            } else if (item.template.id >= 2002 && item.template.id <= 2010) {
                ChanMenh = item;
                level = item.template.id - 2002;
            }
        }

        if (ChanMenh != null && ChanMenh.template.id == 2010) {
            Service.gI().sendDialogMessage(player, "Chân thiên tử đã đạt cấp tối đa.");
            return;
        }

        if (ChanMenh == null || TinhThe == null || MaQuai == null) {
            Service.gI().sendDialogMessage(player, "Cần 1 Chân thiên tử và Tinh thể và Ma quái.");
            return;
        }

        int TinhTheQuantity = CombineUtil.getMaQuaiVaTinhTheNangcapChanmenh(level);
        int MaQuaiQuantity = CombineUtil.getMaQuaiVaTinhTheNangcapChanmenh(level);
        float tilechanmenh = CombineUtil.getTiLeNangcapChanmenh(level);

        if (TinhThe.quantity < TinhTheQuantity) {
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Bạn không đủ Tinh thể!", "Đóng");
            return;
        }

        if (MaQuai.quantity < MaQuaiQuantity) {
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Bạn không đủ Ma quái!", "Đóng");
            return;
        }

        if (ChanMenh != null && TinhThe != null && MaQuai != null
                && (ChanMenh.template.id >= 2002 && ChanMenh.template.id < 2010)) {

            String npcSay = "Trang sức sau khi nâng cấp là: " + CombineUtil.getNameNangcapChanmenh(level) + "\n"
                    + "|0|Sau khi nâng cấp sẽ được trang sức mạnh hơn\n";

            npcSay += "|0|Tỉ lệ thành công: " + tilechanmenh + "%\n";

            if (TinhTheQuantity >= CombineUtil.getMaQuaiVaTinhTheNangcapChanmenh(level)
                    && MaQuaiQuantity >= CombineUtil.getMaQuaiVaTinhTheNangcapChanmenh(level)) {

                npcSay += "|2|Phí nâng cấp: " + TinhTheQuantity + " Tinh thể và " + TinhTheQuantity + " Ma quái";

                CombineService.gI().whis.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Nâng cấp\nx1 lần",
                        "Nâng cấp\nx10 lần",
                        "Nâng cấp\nx30 lần",
                        "Nâng cấp\nx70 lần");
            }
        }
    }

    public static void nangCapChanMenh(Player player, int... numm) {
        int n = 1;

        if (numm.length > 0) {
            n = numm[0];
        }

        if (player.combine.itemsCombine.size() != 3) {
            return;
        }

        Item TinhThe = null;
        Item MaQuai = null;
        Item ChanMenh = null;
        int capbac = 0;

        for (Item item : player.combine.itemsCombine) {
            if (item.template.id == 2011) {
                TinhThe = item;
            } else if (item.template.id == 2012) {
                MaQuai = item;
            } else if (item.template.id >= 2002 && item.template.id < 2010) {
                ChanMenh = item;
                capbac = item.template.id - 2002;
            }
        }

        if (TinhThe == null || MaQuai == null || ChanMenh == null) {
            return;
        }

        int quantityTinhThe = CombineUtil.getMaQuaiVaTinhTheNangcapChanmenh(capbac);
        int quantityMaQuai = CombineUtil.getMaQuaiVaTinhTheNangcapChanmenh(capbac);

        if (n == 1) {
            if (TinhThe.quantity < quantityTinhThe) {
                Service.gI().sendServerMessage(player, "Bạn không đủ Tinh thể");
                return;
            }

            if (MaQuai.quantity < quantityMaQuai) {
                Service.gI().sendServerMessage(player, "Bạn không đủ Ma quái");
                return;
            }
        }

        int num = 0;
        boolean success = false;

        for (int i = 0; i < n; i++) {
            num = i + 1;

            float tilechanmenh = CombineUtil.getTiLeNangcapChanmenh(capbac);

            if (TinhThe.quantity < quantityTinhThe) {
                Service.gI().sendServerMessage(player, "Sau " + i + " lần nâng cấp thất bại, bạn không đủ Tinh thể để tiếp tục.");
                break;
            }

            if (MaQuai.quantity < quantityMaQuai) {
                Service.gI().sendServerMessage(player, "Sau " + i + " lần nâng cấp thất bại, bạn không đủ Ma quái để tiếp tục.");
                break;
            }

            InventoryService.gI().subQuantityItemsBag(player, TinhThe, quantityTinhThe);
            InventoryService.gI().subQuantityItemsBag(player, MaQuai, quantityMaQuai);

            // Mau so 100 vi bang luu PHAN TRAM THAT. Ban cu so voi 200, nen so
            // 35 trong ma thuc ra la 17,5% — doc bang khong ai doan ra.
            if (Util.isTrue(tilechanmenh, 100)) {
                success = true;
                break;
            }
        }

        if (success) {
            if (capbac > 4) {
                ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa nâng cấp thành công "
                        + ChanMenh.template.name + " lên 1 cấp");
            }

            if (n > 1) {
                Service.gI().sendServerMessage(player, "Thành công sau " + num + " lần nâng cấp.");
            }

            InventoryService.gI().subQuantityItemsBag(player, TinhThe, quantityTinhThe);
            InventoryService.gI().subQuantityItemsBag(player, MaQuai, quantityMaQuai);

            ChanMenh.template = ItemService.gI().getTemplate(ChanMenh.template.id + 1);

            // Chi so khai tren panel (tab "Chan menh") thang cach tinh viet cung.
            // Cap MOI la capbac + 1 vi template.id vua tang mot bac o tren.
            java.util.List<nro.repository.dao.TrangSucDAO.ChiSo> dsCS =
                    nro.repository.dao.TrangSucDAO.chiSoChanMenh(capbac + 1, true);
            if (!dsCS.isEmpty()) {
                for (nro.repository.dao.TrangSucDAO.ChiSo cs : dsCS) {
                    boolean daCo = false;
                    for (ItemOption io : ChanMenh.itemOptions) {
                        if (io.optionTemplate.id == cs.optionId) {
                            io.param = cs.min;
                            daCo = true;
                            break;
                        }
                    }
                    // Chua co dong nay thi THEM vao, khong bo qua: bang tren panel
                    // la ban khai day du chi so cua cap do, khong phai ban va.
                    if (!daCo) {
                        ChanMenh.itemOptions.add(new ItemOption(cs.optionId, cs.min));
                    }
                }
            }
            // Khong con cach tinh viet cung. Cap nao chua khai chi so thi do giu
            // nguyen chi so cu — thay vi lang le dat mot con so ma khong ai go.

            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            InventoryService.gI().subQuantityItemsBag(player, TinhThe, quantityTinhThe);
            InventoryService.gI().subQuantityItemsBag(player, MaQuai, quantityMaQuai);
            CombineService.gI().sendEffectFailCombine(player);
        }

        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}