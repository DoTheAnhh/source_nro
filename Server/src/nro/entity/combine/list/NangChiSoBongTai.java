package nro.entity.combine.list;

import nro.service.inventory.InventoryService;
import nro.entity.player.Player;
import nro.service.Service;
import nro.core.util.FormatStyle;
import nro.core.util.Util;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.service.combine.CombineService;

public class NangChiSoBongTai {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendDialogMessage(player, 
                "Cần bông tai cấp 2 hoặc cấp 3, 99 mảnh hồn Porata và 1 đá xanh lam.");
            return;
        }

        Item bongTai = null;
        Item manhHonBongTai = null;
        Item daXanhLam = null;

        for (Item item : player.combine.itemsCombine) {
            if (item.isNotNullItem()) {
                switch (item.template.id) {
                    case 921: // BT2
                    case 1943: // BT3
                        // Uu tien cap CAO NHAT: dat ca BT2 lan BT3 vao o thi
                        // nang chi so cho BT3. Vong lap cu ghi de theo thu
                        // tu duyet nen ket qua phu thuoc o nao dung truoc.
                        if (bongTai == null
                                || item.template.id > bongTai.template.id) {
                            bongTai = item;
                        }
                        break;
                    case 934: // Mảnh hồn
                        manhHonBongTai = item;
                        break;
                    case 935: // Đá xanh lam
                        daXanhLam = item;
                        break;
                }
            }
        }

        if (bongTai == null || manhHonBongTai == null || daXanhLam == null) {
            Service.gI().sendDialogMessage(player, 
                "Cần bông tai cấp 2 hoặc cấp 3, 99 mảnh hồn Porata và 1 đá xanh lam.");
            return;
        }

        StringBuilder text = new StringBuilder();
        if (bongTai.template.id == 921) {
            text.append(ConstFont.BOLD_BLUE).append("Bông tai Porata [+2] - Nâng chỉ số\n\n");
            text.append(ConstFont.BOLD_GREEN).append("Reset toàn bộ chỉ số, random lại\n");
        } else {
            text.append(ConstFont.BOLD_BLUE).append("Bông tai Porata [+3] - Nâng chỉ số\n\n");
            text.append(ConstFont.BOLD_GREEN).append("Chỉ thay thế dòng cuối bằng random mới\n");
        }

        text.append(ConstFont.BOLD_BLUE).append("Tỉ lệ thành công: 50%\n");
        text.append(manhHonBongTai.quantity >= 99 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
            .append("Cần 99 Mảnh hồn bông tai\n");
        text.append(daXanhLam.quantity >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
            .append("Cần 1 Đá xanh lam\n");
        text.append(player.inventory.getGemAndRuby() >= 250 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
            .append("Cần 250 ngọc\n");

        if (player.inventory.getGemAndRuby() < 250) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                "Còn thiếu\n" + Util.formatNumber(250 - player.inventory.getGemAndRuby(), FormatStyle.VIETNAMESE) + " ngọc");
            return;
        }
        if (daXanhLam.quantity < 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                "Còn thiếu\nĐá xanh lam");
            return;
        }
        if (manhHonBongTai.quantity < 99) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                "Còn thiếu\n" + (99 - manhHonBongTai.quantity) + " Mảnh hồn bông tai");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
            text.toString(), "Nâng cấp\n250 ngọc", "Từ chối");
    }

    public static void nangChiSoBongTai(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            return;
        }

        Item bongTai = null;
        Item manhHonBongTai = null;
        Item daXanhLam = null;

        for (Item item : player.combine.itemsCombine) {
            if (item.isNotNullItem()) {
                switch (item.template.id) {
                    case 921: // BT2
                    case 1943: // BT3
                        // Uu tien cap CAO NHAT: dat ca BT2 lan BT3 vao o thi
                        // nang chi so cho BT3. Vong lap cu ghi de theo thu
                        // tu duyet nen ket qua phu thuoc o nao dung truoc.
                        if (bongTai == null
                                || item.template.id > bongTai.template.id) {
                            bongTai = item;
                        }
                        break;
                    case 934: // Mảnh hồn
                        manhHonBongTai = item;
                        break;
                    case 935: // Đá xanh lam
                        daXanhLam = item;
                        break;
                }
            }
        }

        if (bongTai == null || manhHonBongTai == null || daXanhLam == null
                || player.inventory.getGemAndRuby() < 250
                || daXanhLam.quantity < 1
                || manhHonBongTai.quantity < 99) {
            return;
        }

        if (Util.isTrue(50, 100)) {
            int option;
            int param;
            // Be chi so khai tren panel (tab "Bong tai") thang so viet cung.
            // Chua khai gi thi quay ve bo cu, nen bat he thong nay len khong doi
            // gi cho toi khi quan tri that su them mot dong.
            nro.repository.dao.TrangSucDAO.Cap capBT =
                    nro.repository.dao.TrangSucDAO.capBongTaiTheoItem(
                            bongTai.template.id);
            java.util.List<nro.repository.dao.TrangSucDAO.ChiSo> be =
                    capBT == null ? new java.util.ArrayList<>()
                            : nro.repository.dao.TrangSucDAO.chiSoBongTai(
                                    capBT.cap, true);
            if (capBT == null || be.isEmpty()) {
                // Be chi so la bang tren panel, khong con ban sao trong ma. Cap
                // nay chua khai dong nao thi khong co gi de boc — bao cho nguoi
                // choi biet thay vi im lang tra ve mot bong tai rong chi so.
                Service.gI().sendThongBao(player,
                        "Cấp bông tai này chưa khai chỉ số nào — báo quản trị.");
                CombineService.gI().reOpenItemCombine(player);
                return;
            }
            // Bao nhieu dong chi so: boc theo bang ti le tren panel. Vi du khai
            // "3 dong 5%" va "2 dong 30%" thi 5% ra ba dong, 30% ra hai dong,
            // con lai mot dong.
            int soDong = nro.repository.dao.TrangSucDAO.bocSoDong(capBT.cap);
            if (soDong < 1) {
                soDong = 1;
            }
            // Khong boc trung mot chi so hai lan: hai dong "HP+5%" va "HP+9%"
            // tren cung mot mon thi game chi tinh mot dong, dong kia mat trang.
            java.util.List<Integer> daBoc = new java.util.ArrayList<>();
            java.util.List<ItemOption> dongMoi = new java.util.ArrayList<>();
            for (int lan = 0; lan < soDong && daBoc.size() < be.size(); lan++) {
                nro.repository.dao.TrangSucDAO.ChiSo boc = null;
                // Thu toi da bang so muc trong be roi bo cuoc: be it muc hon so
                // dong can boc thi vong lap vo han neu cu thu mai.
                for (int thu = 0; thu < be.size() * 3; thu++) {
                    nro.repository.dao.TrangSucDAO.ChiSo x =
                            be.get(Util.nextInt(be.size()));
                    if (!daBoc.contains(x.optionId)) {
                        boc = x;
                        break;
                    }
                }
                if (boc == null) {
                    break;
                }
                daBoc.add(boc.optionId);
                dongMoi.add(new ItemOption(boc.optionId,
                        boc.max > boc.min ? Util.nextInt(boc.min, boc.max)
                                : boc.min));
            }
            if (dongMoi.isEmpty()) {
                Service.gI().sendThongBao(player,
                        "Không bốc được chỉ số nào — báo quản trị.");
                CombineService.gI().reOpenItemCombine(player);
                return;
            }
            option = dongMoi.get(0).optionTemplate.id;
            param = dongMoi.get(0).param;

            if (bongTai.template.id == 921) {
                // BT2: reset toàn bộ option rồi gắn đủ số dòng vừa bốc.
                bongTai.itemOptions.clear();
                bongTai.itemOptions.addAll(dongMoi);
                bongTai.itemOptions.add(new ItemOption(38, 0));
            } else {
                // BT3: bo dung SO DONG cuoi roi gan lai bang so do. Ban cu luon
                // bo dung MOT dong; gio boc hai dong thi phai bo hai, khong thi
                // moi lan nang bong tai dai them mot dong mai khong dung.
                for (int i = 0; i < dongMoi.size()
                        && !bongTai.itemOptions.isEmpty(); i++) {
                    bongTai.itemOptions.remove(bongTai.itemOptions.size() - 1);
                }
                bongTai.itemOptions.addAll(dongMoi);
            }

            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            CombineService.gI().sendEffectFailCombine(player);
        }

        // Trừ nguyên liệu
        InventoryService.gI().subQuantityItemsBag(player, manhHonBongTai, 99);
        InventoryService.gI().subQuantityItemsBag(player, daXanhLam, 1);
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
