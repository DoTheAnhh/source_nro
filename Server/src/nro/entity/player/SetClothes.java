package nro.entity.player;

import nro.entity.item.Item;
import nro.entity.item.ItemOption;

public class SetClothes {

    private Player player;

    public SetClothes(Player player) {
        this.player = player;
    }
    //all gender
    

    public boolean godClothes;

    /**
     * Số món đang mặc của các set <b>admin tự định nghĩa</b>.
     *
     * <p>Set viết sẵn thì mỗi cái một trường {@code byte} ở trên. Set tạo từ
     * panel không thể sinh thêm trường Java, nên đếm ở đây, tra theo mã set.</p>
     */
    public final java.util.Map<String, Integer> setCauHinh = new java.util.HashMap<>();
    public int ctHaiTac = -1;
    public int ctBunmaTocMau = -1;
    public int ctTiecbaiBien = -1;
    
    public int ctNezuko = -1;
    public int ctTanjiro = -1;
    public int ctInoHashi = -1;
    public int ctInosuke = -1;
    public int ctZenitsu = -1;
    
    public int ctFideDaiCa = -1;
    public int ctDanEmFide = -1;

    public void setup() {
        setDefault();
        setupSetTuCauHinh();
        // Cac set viet cung da bo han: khong dem nua. Mot mon mang option 129
        // gio khong con tinh la "mac set Songoku" o bat ky cho nao.
        this.godClothes = true;
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id > 567 || item.template.id < 555) {
                    this.godClothes = false;
                    break;
                }
            } else {
                this.godClothes = false;
                break;
            }
        }
        Item ct = this.player.inventory.itemsBody.get(5);
        if (ct.isNotNullItem()) {
            switch (ct.template.id) {
                case 618:
                case 619:
                case 620:
                case 621:
                case 622:
                case 623:
                case 624:
                case 626:
                case 627:
                    this.ctHaiTac = ct.template.id;
                    break;
                case 1087:
                    this.ctTanjiro = ct.template.id;
                    break;
                case 1088:
                    this.ctInoHashi = ct.template.id;
                    break;
                case 1089:
                    this.ctInosuke = ct.template.id;
                    break;
                case 1090:
                    this.ctZenitsu = ct.template.id;
                    break;
                case 1091:
                    this.ctNezuko = ct.template.id;
                    break;
                case 1208:
                case 1209:
                case 1210:
                    this.ctBunmaTocMau = ct.template.id;
                    break;
                case 405:
                case 406:
                case 407:
                case 629:
                    this.ctFideDaiCa = ct.template.id;
                    break;
                case 429:
                case 430:
                case 431:
                case 432:
                case 433:
                    this.ctDanEmFide = ct.template.id;
                    break;
                case 1234:
                case 1235:
                case 1236:
                    this.ctTiecbaiBien = ct.template.id;
                    break;

            }
        }

    }

    /**
     * Đếm số món của các set admin tự định nghĩa.
     *
     * <p>Cùng cách nhận diện với set viết sẵn: duyệt năm ô trang bị, món nào có
     * một trong các option đã khai thì tính một món. Một món chỉ tính <b>một
     * lần</b> cho mỗi set dù mang nhiều option của set đó.</p>
     */
    private void setupSetTuCauHinh() {
        setCauHinh.clear();
        java.util.Map<String, nro.repository.dao.SetBonusDAO.DinhNghia> dn;
        try {
            dn = nro.repository.dao.SetBonusDAO.dinhNghia();
        } catch (Exception ex) {
            return;      // chưa đọc được cấu hình -> coi như không có set nào
        }
        if (dn.isEmpty()) {
            return;
        }
        for (nro.repository.dao.SetBonusDAO.DinhNghia d : dn.values()) {
            if (!d.active) {
                continue;
            }
            java.util.Set<Integer> ids = new java.util.HashSet<>();
            for (String p : String.valueOf(d.optionIds).split(",")) {
                try {
                    ids.add(Integer.parseInt(p.trim()));
                } catch (NumberFormatException ignored) {
                    // Dòng cấu hình hỏng -> bỏ qua option đó.
                }
            }
            if (ids.isEmpty()) {
                continue;
            }
            int dem = 0;
            for (int i = 0; i < 5; i++) {
                Item item = this.player.inventory.itemsBody.get(i);
                if (!item.isNotNullItem()) {
                    continue;
                }
                for (ItemOption io : item.itemOptions) {
                    if (io != null && io.optionTemplate != null
                            && ids.contains(io.optionTemplate.id)) {
                        dem++;
                        break;      // mỗi món chỉ tính một lần cho set này
                    }
                }
            }
            if (dem > 0) {
                setCauHinh.put(d.setKey, dem);
            }
        }
    }



    private void setDefault() {
        //else
        this.godClothes = false;
        this.ctHaiTac = -1;
        this.ctInoHashi = -1;
        this.ctInosuke = -1;
        this.ctNezuko = -1;
        this.ctTanjiro = -1;
        this.ctZenitsu = -1;
        this.ctBunmaTocMau = -1;
        this.ctFideDaiCa = -1;
        this.ctDanEmFide = -1;
        this.ctTiecbaiBien = -1;
    }

    public boolean checkSetGod() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id < 555 || item.template.id > 567) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean checkSetDes() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id < 650 || item.template.id > 662) {

                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public void dispose() {
        this.player = null;
    }
}
