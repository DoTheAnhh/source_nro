package nro.combine;

import nro.inventory.InventoryService;
import nro.player.Player;
import consts.ConstNpc;
import java.io.IOException;
import models.Item.Item;
import network.io.Message;
import nro.combine.ListCombine.CheTaoTrangBiThienSu;
import nro.combine.ListCombine.CuongHoaLoSaoPhaLe;
import nro.combine.ListCombine.DanhBongSaoPhaLe;
import nro.combine.ListCombine.DapDoAoHoa;
import nro.combine.ListCombine.EpSaoTrangBi;
import nro.combine.ListCombine.GiaHanVatPham;
import nro.combine.ListCombine.GiamDinhSach;
import nro.combine.ListCombine.HoiPhucSach;
import nro.combine.ListCombine.MoKhoaItem;
import nro.combine.ListCombine.NangCapBongTai;
import nro.combine.ListCombine.NangCapChanMenh;
import nro.combine.ListCombine.NangCapKichHoat;
import nro.combine.ListCombine.NangCapKichHoatVip;
import nro.combine.ListCombine.NangCapSachTuyetKy;
import nro.combine.ListCombine.NangCapSaoPhaLe;
import nro.combine.ListCombine.NangCapVatPham;
import nro.combine.ListCombine.NangChiSoBongTai;
import nro.combine.ListCombine.NangGiapLuyenTap;
import nro.combine.ListCombine.NhapNgocRong;
import nro.combine.ListCombine.PhaLeHoaTrangBi;
import nro.combine.ListCombine.PhanRaSach;
import nro.combine.ListCombine.PhapSuHoa;
import nro.combine.ListCombine.RemoveOptionItem;
import nro.combine.ListCombine.SieuHoaCaiTrang;
import nro.combine.ListCombine.TaoDaHematite;
import nro.combine.ListCombine.TaySach;
import nro.combine.ListCombine.TinhAnTrangBi;
import nro.combine.ListCombine.TinhThachHoa;
import nro.npc.Npc;
import nro.npc.NpcManager;

public class CombineService {

    private static final int COST = 500000000;
    private static final int TIME_COMBINE = 1500;
    public static final byte MAX_STAR_ITEM = 8;
    public static final byte MAX_LEVEL_ITEM = 8;
    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte combineSUCCESS = 2;
    private static final byte combineFAIL = 3;
    private static final byte combineCHANGE_OPTION = 4;
    private static final byte combineDRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;
    public static final int EP_SAO_TRANG_BI = 500;
    public static final int PHA_LE_HOA_TRANG_BI = 501;
    public static final int CHUYEN_HOA_TRANG_BI_DUNG_VANG = 502;
    public static final int CHUYEN_HOA_TRANG_BI_DUNG_NGOC = 503;
    public static final int NHAP_DA = 504;
    public static final int NANG_CAP_SAO_PHA_LE = 100;
    public static final int DANH_BONG_SAO_PHA_LE = 101;
    public static final int CUONG_HOA_LO_SAO_PHA_LE = 102;
    public static final int TAO_DA_HEMATITE = 103;
    public static final int GIAM_DINH_SACH = 104;
    public static final int TAY_SACH = 105;
    public static final int NANG_CAP_SACH_TUYET_KY = 106;
    public static final int HOI_PHUC_SACH = 107;
    public static final int PHAN_RA_SACH = 108;
    public static final int CHE_TAO_TRANG_BI_THIEN_SU = 109;
    public static final int NANG_CAP_VAT_PHAM = 510;
    public static final int NANG_CAP_BONG_TAI = 511;
    public static final int LAM_PHEP_NHAP_DA = 512;
    public static final int NHAP_NGOC_RONG = 513;
    public static final int NANG_CHI_SO_BONG_TAI = 517;
    public static final int NANG_CAP_KICH_HOAT = 518;
    public static final int NANG_CAP_KICH_HOAT_VIP = 519;
    public static final int NANG_CAP_BONG_TAI_3 = 530;      // nâng BT2 -> BT3
    public static final int NANG_CHI_SO_BONG_TAI_3 = 531;   // mở chỉ số BT3

    public static final int DAP_DO_AO_HOA = 520;
    public static final int PS_HOA_TRANG_BI = 521;
    public static final int TAY_PS_HOA_TRANG_BI = 522;
    public static final int MO_KHOA_ITEM = 523;
    public static final int NANG_CAP_CHAN_MENH = 524;
    public static final int AN_TRANG_BI = 525;
    public static final int GIA_HAN_VAT_PHAM = 526;
    public static final int SIEU_HOA = 527;
    public static final int TINH_THACH_HOA = 528;
    public static final int NANG_GIAP_LUYEN_TAP = 529;
    public static final int NANG_CAP_DE_TU = 600;

    private static CombineService instance;

    public final Npc baHatMit;
    public final Npc whis;

    private CombineService() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.whis = NpcManager.getNpc(ConstNpc.WHIS);
    }

    public static CombineService gI() {
        if (instance == null) {
            instance = new CombineService();
        }
        return instance;
    }

    /**
     * Hiển thị thông tin đập đồ
     *
     * @param player
     * @param index
     */
    public void showInfoCombine(Player player, int[] index) {
        if (player.combine == null) {
            return;
        }
        player.combine.clearItemCombine();
        if (index.length > 0) {
            for (int i = 0; i < index.length; i++) {
                player.combine.itemsCombine.add(player.inventory.itemsBag.get(index[i]));
            }
        }
        switch (player.combine.typeCombine) {
            case EP_SAO_TRANG_BI:
                EpSaoTrangBi.showInfoCombine(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.showInfoCombine(player);
                break;
            case NHAP_NGOC_RONG:
                NhapNgocRong.showInfoCombine(player);
                break;
            case NANG_CAP_VAT_PHAM:
                NangCapVatPham.showInfoCombine(player);
                break;
            case NANG_CAP_BONG_TAI:
                NangCapBongTai.showInfoCombine(player);
                break;
            case NANG_CHI_SO_BONG_TAI:
                NangChiSoBongTai.showInfoCombine(player);
                break;
            case NANG_CAP_BONG_TAI_3:
                NangCapBongTai.showInfoCombine(player); // dùng chung class NangCapBongTai
                break;
            case NANG_CHI_SO_BONG_TAI_3:
                NangChiSoBongTai.showInfoCombine(player); // dùng chung class NangChiSoBongTai
                break;

            case NANG_CAP_SAO_PHA_LE:
                NangCapSaoPhaLe.showInfoCombine(player);
                break;
            case DANH_BONG_SAO_PHA_LE:
                DanhBongSaoPhaLe.showInfoCombine(player);
                break;
            case CUONG_HOA_LO_SAO_PHA_LE:
                CuongHoaLoSaoPhaLe.showInfoCombine(player);
                break;
            case TAO_DA_HEMATITE:
                TaoDaHematite.showInfoCombine(player);
                break;
            case GIAM_DINH_SACH:
                GiamDinhSach.showInfoCombine(player);
                break;
            case TAY_SACH:
                TaySach.showInfoCombine(player);
                break;
            case NANG_CAP_SACH_TUYET_KY:
                NangCapSachTuyetKy.showInfoCombine(player);
                break;
            case HOI_PHUC_SACH:
                HoiPhucSach.showInfoCombine(player);
                break;
            case PHAN_RA_SACH:
                PhanRaSach.showInfoCombine(player);
                break;
            case CHE_TAO_TRANG_BI_THIEN_SU:
                CheTaoTrangBiThienSu.showInfoCombine(player);
                break;
            case NANG_CAP_KICH_HOAT:
                NangCapKichHoat.showInfoCombine(player);
                break;
            case NANG_CAP_KICH_HOAT_VIP:
                NangCapKichHoatVip.showInfoCombine(player);
                break;
            case DAP_DO_AO_HOA:
                DapDoAoHoa.showInfoCombine(player);
                break;
            case PS_HOA_TRANG_BI:
                PhapSuHoa.showInfoCombine(player);
                break;
            case TAY_PS_HOA_TRANG_BI:
                RemoveOptionItem.showInfoCombine(player);
                break;
            case MO_KHOA_ITEM:
                MoKhoaItem.showInfoCombine(player);
                break;
            case NANG_CAP_CHAN_MENH:
                NangCapChanMenh.showInfoCombine(player);
                break;
            case AN_TRANG_BI:
                TinhAnTrangBi.showInfoCombine(player);
                break;
            case GIA_HAN_VAT_PHAM:
                GiaHanVatPham.showInfoCombine(player);
                break;
            case SIEU_HOA:
                SieuHoaCaiTrang.showInfoCombine(player);
                break;
            case TINH_THACH_HOA:
                TinhThachHoa.showInfoCombine(player);
                break;
            case NANG_GIAP_LUYEN_TAP:
                NangGiapLuyenTap.showInfoCombine(player);
                break;
            case NANG_CAP_DE_TU:
                NangCapDeTu.showInfoCombine(player);
                break;

        }
    }

    /**
     * Bắt đầu đập đồ - điều hướng từng loại đập đồ
     *
     * @param player
     * @param n
     */
    public void startCombine(Player player, int... n) {
        int num = 0;
        if (n.length > 0) {
            num = n[0];
        }
        switch (player.combine.typeCombine) {
            case EP_SAO_TRANG_BI:
                EpSaoTrangBi.epSaoTrangBi(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.phaLeHoa(player, num);
                break;
            case NHAP_NGOC_RONG:
                NhapNgocRong.nhapNgocRong(player, num == 1);
                break;
            case NANG_CAP_VAT_PHAM:
                NangCapVatPham.nangCapVatPham(player, num == 1);
                break;
            case NANG_CAP_BONG_TAI:
                NangCapBongTai.nangCapBongTai(player);
                break;
            case NANG_CHI_SO_BONG_TAI:
                NangChiSoBongTai.nangChiSoBongTai(player);
                break;
            case NANG_CAP_BONG_TAI_3:
                NangCapBongTai.nangCapBongTai(player);
                break;
            case NANG_CHI_SO_BONG_TAI_3:
                NangChiSoBongTai.nangChiSoBongTai(player);
                break;

            case NANG_CAP_SAO_PHA_LE:
                NangCapSaoPhaLe.nangCapSaoPhaLe(player);
                break;
            case DANH_BONG_SAO_PHA_LE:
                DanhBongSaoPhaLe.danhBongSaoPhaLe(player);
                break;
            case CUONG_HOA_LO_SAO_PHA_LE:
                CuongHoaLoSaoPhaLe.cuongHoaLoSaoPhaLe(player);
                break;
            case TAO_DA_HEMATITE:
                TaoDaHematite.taoDaHematite(player);
                break;
            case GIAM_DINH_SACH:
                GiamDinhSach.giamDinhSach(player);
                break;
            case TAY_SACH:
                TaySach.taySach(player);
                break;
            case NANG_CAP_SACH_TUYET_KY:
                NangCapSachTuyetKy.nangCapSachTuyetKy(player);
                break;
            case HOI_PHUC_SACH:
                HoiPhucSach.hoiPhucSach(player);
                break;
            case PHAN_RA_SACH:
                PhanRaSach.phanRaSach(player);
                break;
            case CHE_TAO_TRANG_BI_THIEN_SU:
                CheTaoTrangBiThienSu.cheTaoTrangBiThienSu(player);
                break;
            case NANG_CAP_KICH_HOAT:
                NangCapKichHoat.startCombine(player);
                break;
            case NANG_CAP_KICH_HOAT_VIP:
                NangCapKichHoatVip.startCombine(player);
                break;
            case DAP_DO_AO_HOA:
                DapDoAoHoa.startCombine(player);
                break;
            case PS_HOA_TRANG_BI:
                PhapSuHoa.startCombine(player);
                break;
            case TAY_PS_HOA_TRANG_BI:
                RemoveOptionItem.startCombine(player);
                break;
            case MO_KHOA_ITEM:
                MoKhoaItem.startCombine(player);
                break;
            case NANG_CAP_CHAN_MENH:
                NangCapChanMenh.nangCapChanMenh(player, num);
                break;
            case AN_TRANG_BI:
                TinhAnTrangBi.startCombine(player);
                break;
            case GIA_HAN_VAT_PHAM:
                GiaHanVatPham.startCombine(player);
                break;
            case SIEU_HOA:
                SieuHoaCaiTrang.startCombine(player);
                break;
            case TINH_THACH_HOA:
                TinhThachHoa.startCombine(player);
                break;
            case NANG_GIAP_LUYEN_TAP:
                NangGiapLuyenTap.startCombine(player);
                break;
            case NANG_CAP_DE_TU:
                NangCapDeTu.startCombine(player);
                break;

        }

        player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combine.clearParamCombine();
        player.combine.lastTimeCombine = System.currentTimeMillis();

    }

    /**
     * Mở tab đập đồ
     *
     * @param player
     * @param type kiểu đập đồ
     */
    public void openTabCombine(Player player, int type) {
        player.combine.setTypeCombine(type);
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            if (player.iDMark.getNpcChose() != null) {
                msg.writer().writeShort(player.iDMark.getNpcChose().tempId);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng mở item
     *
     * @param player
     * @param icon1
     * @param icon2
     */
    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendEffectCombineItem(Player player, byte type, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(type);
            switch (type) {
                case 0:
                    msg.writer().writeUTF("");
                    msg.writer().writeUTF("");
                    break;
                case 1:
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(-1);
                    break;
                case 2: // success 0 eff 0
                case 3: // success 1 eff 0
                    break;
                case 4: // success 0 eff 1
                    msg.writer().writeShort(icon1);
                    break;
                case 5: // success 0 eff 2
                    msg.writer().writeShort(icon1);
                    break;
                case 6: // success 0 eff 3
                    msg.writer().writeShort(icon1);
                    msg.writer().writeShort(icon2);
                    break;
                case 7: // success 0 eff 4
                    msg.writer().writeShort(icon1);
                    break;
                case 8: // success 1 eff 4
                    break;
            }
            msg.writer().writeShort(-1); // id npc
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng đập đồ thành công
     *
     * @param player
     */
    public void sendEffectSuccessCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(combineSUCCESS);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng đập đồ thất bại
     *
     * @param player
     */
    public void sendEffectFailCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(combineFAIL);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Gửi lại danh sách đồ trong tab combine
     *
     * @param player
     */
    public void reOpenItemCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combine.itemsCombine.size());
            for (Item it : player.combine.itemsCombine) {
                for (int j = 0; j < player.inventory.itemsBag.size(); j++) {
                    if (it == player.inventory.itemsBag.get(j)) {
                        msg.writer().writeByte(j);
                    }
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng ghép ngọc rồng
     *
     * @param player
     * @param icon
     */
    public void sendEffectCombineDB(Player player, short icon) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(combineDRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendAddItemCombine(Player player, int npcId, Item... items) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("VanKhaiPro");
            msg.writer().writeUTF("VanKhaiPro");
            msg.writer().writeShort(npcId);
            player.sendMessage(msg);
            msg.cleanup();
            msg = new Message(-81);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.length);
            for (Item item : items) {
                msg.writer().writeByte(InventoryService.gI().getIndexItemBag(player, item));
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffSuccessVip(Player player, int iconID) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(7);
            msg.writer().writeShort(iconID);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffFailVip(Player player) {
        try {
            Message msg;
            msg = new Message(-81);
            msg.writer().writeByte(8);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private String getTextTopTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở nên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case CHUYEN_HOA_TRANG_BI_DUNG_VANG:
            case CHUYEN_HOA_TRANG_BI_DUNG_NGOC:
                return "Lưu ý trang bị mới\nphải hơn trang bị gốc\n1 bậc";
            case NHAP_NGOC_RONG:
                return "Ta sẽ phù phép\ncho 7 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case NHAP_DA:
                return "Ta sẽ phù phép\ncho 10 mảnh đá vụn\ntrở thành 1 đá nâng cấp";
            case NANG_CAP_VAT_PHAM:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở nên mạnh mẽ";
            case NANG_CAP_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành cấp 2";
            case NANG_CHI_SO_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case NANG_CAP_BONG_TAI_3:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\nthành cấp 3";
            case NANG_CHI_SO_BONG_TAI_3:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 3 của ngươi\ncó thêm chỉ số ngẫu nhiên";

            case NANG_CAP_SAO_PHA_LE:
                return "Ta sẽ phù phép\nnâng cấp Sao Pha Lê\nthành cấp 2";
            case DANH_BONG_SAO_PHA_LE:
                return "Đánh bóng\nSao pha lê cấp 2";
            case CUONG_HOA_LO_SAO_PHA_LE:
                return "Cường hóa\nÔ Sao Pha Lê";
            case TAO_DA_HEMATITE:
                return "Ta sẽ phù phép\ntạo đá hematite";
            case GIAM_DINH_SACH:
                return "Ta sẽ phù phép\ngiám định sách đó cho ngươi";
            case TAY_SACH:
                return "Ta sẽ phù phép\ntẩy sách đó cho ngươi";
            case NANG_CAP_CHAN_MENH:
                return "Ta sẽ phù phép\ncho chân thiên tử\nthành 1 chân thiên tử cấp cao";
            case NANG_CAP_SACH_TUYET_KY:
                return "Ta sẽ phù phép\nnâng cấp Sách Tuyệt Kỹ cho ngươi";
            case HOI_PHUC_SACH:
                return "Ta sẽ phù phép\nphục hồi sách cho ngươi";
            case PHAN_RA_SACH:
                return "Ta sẽ phù phép\nphân rã sách đó cho ngươi";
            case CHE_TAO_TRANG_BI_THIEN_SU:
                return "Chế tạo\ntrang bị thiên sứ";
            case LAM_PHEP_NHAP_DA:
                return "Ta sẽ phù phép\n"
                        + "cho 10 mảnh đá vụn\n"
                        + "trở thành 1 đá nâng cấp";
            case NANG_CAP_KICH_HOAT:
                return "Ta sẽ phù phép\nchế tạo trang bị Huỷ Diệt\nthành trang bị Kích Hoạt";
            case NANG_CAP_KICH_HOAT_VIP:
                return "Ta sẽ phù phép\nchế tạo trang bị Thiên Sứ\nthành trang bị Kích Hoạt Vip";
            case GIA_HAN_VAT_PHAM:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\nthêm hạn sử dụng";
            case SIEU_HOA:
                return "Ta sẽ giúp con siêu hóa\n Cải trang";
            case TINH_THACH_HOA:
                return "Ta sẽ giúp con Tinh Thạch đồ";
            case DAP_DO_AO_HOA:
                return "Ta sẽ giúp ngươi ảo hóa đồ để có thuộc tính cao hơn";
            case PS_HOA_TRANG_BI:
                return "Pháp sư hóa pet, linh thú, ván bay";
            case TAY_PS_HOA_TRANG_BI:
                return "Tẩy đồ";
            case MO_KHOA_ITEM:
                return "Mở Khóa giao dịch Item";
            case AN_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị Ấn";
            case NANG_CAP_DE_TU:
                return "Ta sẽ giúp con nâng cấp đệ tử Bư\nthành Đệ Black với sức mạnh bóng tối";

            default:
                return "";
        }
    }

    private String getTextInfoTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\nChọn loại sao pha lê\nSau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI:
                return "Vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case CHUYEN_HOA_TRANG_BI_DUNG_VANG:
            case CHUYEN_HOA_TRANG_BI_DUNG_NGOC:
                return "Vào hành trang\nChọn trang bị gốc\n(Áo,quần,găng,giày hoặc rađa)\ntừ cấp [+4] trở lên\nChọn tiếp trang bị mới\nchưa nâng cấp cần nhập thể\nsau đó chọn 'Nâng cấp'";
            case NHAP_NGOC_RONG:
                return "Vào hành trang\nChọn 7 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case NHAP_DA:
                return "Vào hành trang\nChọn 10 mảnh đá vụn\nChọn 1 bình nước phép\n(mua tại Uron ở trạm tàu vũ trụ)\nSau đó chọn 'Làm phép'";
            case NANG_CAP_VAT_PHAM:
                return "Vào hành trang\nChọn trang bị\n(Áo,quần,găng,giày hoặc rađa)\nChọn loại đá để nâng cấp\nSau đó chọn 'Nâng cấp'";
            case NANG_CAP_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh bông tai để nâng cấp, số lượng 9999 cái\nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh hồn porata số lượng 99\ncái và đá xanh lam để nâng cấp.\nSau đó chọn 'Nâng cấp chỉ số'";
            case NANG_CAP_BONG_TAI_3:
                return "Vào hành trang\nChọn bông tai Porata cấp 2\nChọn mảnh bông tai cấp 3 (9999 cái)\nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI_3:
                return "Vào hành trang\nChọn bông tai Porata cấp 3\nChọn mảnh hồn porata (99 cái)\nvà đá xanh lam\nSau đó chọn 'Nâng cấp chỉ số'";

            case NANG_CAP_SAO_PHA_LE:
                return "Vào hành trang\nChọn đá Hematite\nChọn loại sao pha lê (cấp 1)\nSau đó chọn 'Nâng cấp'";
            case DANH_BONG_SAO_PHA_LE:
                return "Vào hành trang\nChọn loại sao pha lê cấp 2 có từ 2 viên trở lên\nChọn 1 đá mài\nSau đó chọn 'Đánh bóng'";
            case CUONG_HOA_LO_SAO_PHA_LE:
                return "Vào hành trang\nChọn trang bị có Ô sao thứ 8 trở lên chưa cường hóa\nChọn đá Hematite\nChọn dùi đục\nSau đó chọn 'Cường hóa'";
            case TAO_DA_HEMATITE:
                return "Vào hành trang\nChọn 5 sao pha lê cấp 2 cùng màu\nChọn 'Tạo đá Hematite'";
            case NANG_CAP_CHAN_MENH:
                return "Vào hành trang\nChọn trang sức chân thiên tử\nChọn nguyên liệu Ma quái và Tinh thể\nSau đó chọn 'Nâng cấp'";
            case GIAM_DINH_SACH:
                return "Vào hành trang chọn\n1 sách cần giám định";
            case TAY_SACH:
                return "Vào hành trang chọn\n1 sách cần tẩy";
            case NANG_CAP_SACH_TUYET_KY:
                return "Vào hành trang chọn\nSách Tuyệt Kỹ 1 cần nâng cấp và 10 Kìm bấm giấy";
            case HOI_PHUC_SACH:
                return "Vào hành trang chọn\nCác Sách Tuyệt Kỹ cần phục hồi";
            case PHAN_RA_SACH:
                return "Vào hành trang chọn\n1 sách cần phân rã";
            case CHE_TAO_TRANG_BI_THIEN_SU:
                return "Cần 1 công thức\nMảnh trang bị tương ứng\n1 đá nâng cấp (tùy chọn)\n1 đá may mắn (tùy chọn)";
            case LAM_PHEP_NHAP_DA:
                return "Vào hành trang\n"
                        + "Chọn 10 mảnh đá vụn\n"
                        + "Chọn 1 bình nước phép\n"
                        + "(mua tại Uron ở trạm tàu vũ trụ)\n"
                        + "Sau đó chọn 'Làm phép'";
            case NANG_CAP_KICH_HOAT:
                return "Vào hành trang\nChọn 1 trang bị Huỷ Diệt\nChọn 1 món thần linh bất kì\nSau đó chọn 'Nâng cấp'";
            case NANG_CAP_KICH_HOAT_VIP:
                return "Vào hành trang\nChọn 1 trang bị Thiên Sứ\nChọn 1 món hủy diệt bất kì\nSau đó chọn 'Nâng cấp'";
            case DAP_DO_AO_HOA:
                return "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)"
                        + "\nChọn loại đá quý để nâng cấp\n"
                        + "\nCó thể thêm đá bảo vệ để tránh tụt cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PS_HOA_TRANG_BI:
                return "Vào hành trang\nChọn 1 trang bị có thể hắc hóa (pet, linh thú, chân mệnh, ván bay,..) và đá pháp sư \n "
                        + " để nâng cấp chỉ số pháp sư"
                        + "Chỉ cần chọn 'Nâng Cấp'";
            case MO_KHOA_ITEM:
                return "vào hành trang\nChọn 1 trang bị khóa giao dịch ( bông tai, item sự kiện, thỏi vàng,..) và Đá Hoàng Kim \n "
                        + " để mở khóa giao dịch Item"
                        + "Chỉ cần chọn 'Mở Khóa'";

            case TAY_PS_HOA_TRANG_BI:
                return "vào hành trang\nChọn 1 trang bị có thể tẩy ( trang bị,linh thú,pet,..) và đá tẩy \n "
                        + " để xoá nâng cấp chỉ số trang bị như sao pha lê đã ép, ....."
                        + "Chỉ cần chọn 'Nâng Cấp'";

            case AN_TRANG_BI:
                return "Vào hành trang\nChọn 1 Trang bị(Áo, Quần ,Giày ,Găng ,Rada) Hủy Diệt và 99 mảnh Ấn\nSau đó chọn 'Làm phép'\n--------\nTinh ấn (5 món +15%HP)\n Nhật ấn (5 món +15%KI\n Nguyệt ấn (5 món +15%SD)";

            case GIA_HAN_VAT_PHAM:
                return "Vào hành trang\n"
                        + "Chọn 1 trang bị có hạn sử dụng\n"
                        + "Chọn Đá Hoàng Kim\n"
                        + "Sau đó chọn 'Gia hạn'";
            case SIEU_HOA:
                return "Vào hành trang\n"
                        + "Chọn 1 Cải trang\n"
                        + "Chọn Đá Siêu Hóa\n"
                        + "Sau đó chọn 'Nâng Cấp'";
            case TINH_THACH_HOA:
                return "Vào hành trang\n"
                        + "Chọn 1 Vật Phẩm (Pet, Linh Thú, VPDL)\n"
                        + "Chọn 1 loại đá Tinh thạch\n"
                        + "Sau đó chọn 'Nâng Cấp'";

            case NANG_GIAP_LUYEN_TAP:
                return "Vào hành trang\n"
                        + "Chọn 1 Giáp luyện tập\n"
                        + "Chọn đá hổ phách\n"
                        + "Sau đó chọn 'Nâng Cấp'";
            case NANG_CAP_DE_TU:
                return "Cần đệ tử Mabư có sức mạnh trên 70 tỷ\n"
                        + "Cần 10 món Thần Linh bất kỳ\n"
                        + "Cần 200 Thỏi vàng\n"
                        + "Sau đó chọn 'Nâng cấp'";

            default:
                return "";
        }
    }

}
