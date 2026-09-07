package nro.service.combine;

import java.util.List;
import nro.core.consts.ConstFont;
import nro.core.consts.ConstNpc;
import nro.core.util.Util;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.repository.dao.CongThucDoiDAO;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Chạy các công thức đổi vật phẩm đọc từ bảng {@code cong_thuc_doi}.
 *
 * <p>Trước đây mỗi công thức là một lớp riêng với nguyên liệu, tỉ lệ và kết quả
 * viết cứng trong mã. Nay toàn bộ nằm trong CSDL, sửa trên panel là có hiệu lực
 * ngay — không phải build lại máy chủ.</p>
 *
 * <p><b>Tỉ lệ hiển thị lấy từ chính con số dùng để bốc</b>, nên không thể lặp lại
 * chuyện chữ ghi 20% mà code chạy 50% như bản viết cứng cũ của
 * "Đổi Sách Tuyệt kỹ".</p>
 */
public class CongThucDoiService {

    private static CongThucDoiService instance;

    public static CongThucDoiService gI() {
        if (instance == null) {
            instance = new CongThucDoiService();
        }
        return instance;
    }

    /**
     * Hiện bảng xác nhận: liệt kê nguyên liệu kèm số đang có, tô đỏ phần còn
     * thiếu, rồi mới cho bấm Đồng ý.
     *
     * @param menuId mã menu để khi người chơi bấm Đồng ý thì NPC biết chạy gì
     */
    public void hienBang(Player player, String maCongThuc, int menuId) {
        CongThucDoiDAO.CongThuc ct = CongThucDoiDAO.theoMa(maCongThuc);
        if (ct == null) {
            Service.gI().sendThongBao(player, "Chức năng này đang tạm khoá.");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_GREEN).append(ct.ten).append("\n");
        boolean du = true;
        for (int[] nl : ct.nguyenLieu) {
            Item co = InventoryService.gI().findItemBag(player, nl[0]);
            int sl = co != null ? co.quantity : 0;
            boolean duMon = sl >= nl[1];
            du &= duMon;
            text.append(duMon ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                    .append(tenVatPham(nl[0])).append(" ").append(sl)
                    .append("/").append(nl[1]).append("\n");
        }
        text.append(du ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Tỉ lệ thành công: ").append(ct.tyLe).append("%\n");
        if (!ct.haoKhiThatBai.isEmpty()) {
            text.append(ConstFont.BOLD_RED).append("Thất bại mất ");
            for (int i = 0; i < ct.haoKhiThatBai.size(); i++) {
                int[] h = ct.haoKhiThatBai.get(i);
                text.append(i == 0 ? "" : " và ").append(h[1]).append(" ")
                        .append(tenVatPham(h[0]));
            }
        }
        if (!du) {
            CombineService.gI().baHatMit.createOtherMenu(player,
                    ConstNpc.IGNORE_MENU, text.toString(), "Từ chối");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, menuId,
                text.toString(), "Đồng ý", "Từ chối");
    }

    /**
     * Danh sách công thức đã gắn vào một NPC, hiện thành menu <b>của chính NPC
     * đó</b>.
     *
     * <p>Không dùng lại {@link #hienBang} vì hàm ấy luôn mở menu của Bà Hạt Mít —
     * đứng ở Ngục Tù nói chuyện với Whis mà bảng lại hiện ảnh Bà Hạt Mít thì
     * người chơi tưởng lạc chỗ.</p>
     *
     * @return {@code false} nếu NPC này chưa được gắn công thức nào; chỗ gọi nên
     *         bỏ luôn mục đổi khỏi menu thay vì mở ra một menu trống
     */
    public boolean moDanhSachNpc(Player player, nro.entity.npc.Npc npc,
            String maNpc, int menuId) {
        List<CongThucDoiDAO.CongThuc> ds = CongThucDoiDAO.theoNpc(maNpc);
        if (ds.isEmpty()) {
            return false;
        }
        String[] muc = new String[ds.size() + 1];
        for (int i = 0; i < ds.size(); i++) {
            CongThucDoiDAO.CongThuc ct = ds.get(i);
            String ten = (ct.ten == null || ct.ten.isEmpty()) ? ct.ma : ct.ten;
            muc[i] = ten + "\n" + ct.tyLe + "%";
        }
        muc[ds.size()] = "Đóng";
        npc.createOtherMenu(player, menuId, "Ngươi muốn đổi gì?", muc);
        return true;
    }

    /**
     * Bảng xác nhận cho công thức thứ {@code viTri} của một NPC.
     *
     * <p>Nhớ lại mã công thức vào {@link Player#maCongThucDoiDangChon} vì menu chỉ
     * gửi về số thứ tự được chọn, không kèm ngữ cảnh — đến bước bấm "Đồng ý" thì
     * không còn biết đang đổi cái gì nữa.</p>
     *
     * @return {@code false} nếu số thứ tự nằm ngoài danh sách (người chơi bấm
     *         "Đóng")
     */
    public boolean chonCongThucNpc(Player player, nro.entity.npc.Npc npc,
            String maNpc, int viTri, int menuXacNhan) {
        List<CongThucDoiDAO.CongThuc> ds = CongThucDoiDAO.theoNpc(maNpc);
        if (viTri < 0 || viTri >= ds.size()) {
            player.maCongThucDoiDangChon = null;
            return false;
        }
        CongThucDoiDAO.CongThuc ct = ds.get(viTri);
        player.maCongThucDoiDangChon = ct.ma;

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_GREEN)
                .append((ct.ten == null || ct.ten.isEmpty()) ? ct.ma : ct.ten)
                .append("\n");
        boolean du = true;
        for (int[] nl : ct.nguyenLieu) {
            Item co = InventoryService.gI().findItemBag(player, nl[0]);
            int sl = co != null ? co.quantity : 0;
            boolean duMon = sl >= nl[1];
            du &= duMon;
            text.append(duMon ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                    .append(tenVatPham(nl[0])).append(" ").append(sl)
                    .append("/").append(nl[1]).append("\n");
        }
        text.append(du ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Tỉ lệ thành công: ").append(ct.tyLe).append("%\n");
        if (!ct.haoKhiThatBai.isEmpty()) {
            text.append(ConstFont.BOLD_RED).append("Thất bại mất ");
            for (int i = 0; i < ct.haoKhiThatBai.size(); i++) {
                int[] h = ct.haoKhiThatBai.get(i);
                text.append(i == 0 ? "" : " và ").append(h[1]).append(" ")
                        .append(tenVatPham(h[0]));
            }
        }
        if (!du) {
            npc.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "Từ chối");
            return true;
        }
        npc.createOtherMenu(player, menuXacNhan, text.toString(),
                "Đồng ý", "Từ chối");
        return true;
    }

    /** Chạy công thức đang chọn của người này, nếu còn nhớ được là công thức nào. */
    public void thucHienDangChon(Player player) {
        if (player.maCongThucDoiDangChon == null) {
            return;
        }
        thucHien(player, player.maCongThucDoiDangChon);
        player.maCongThucDoiDangChon = null;
    }

    /** Thực hiện công thức sau khi người chơi bấm Đồng ý. */
    public void thucHien(Player player, String maCongThuc) {
        CongThucDoiDAO.CongThuc ct = CongThucDoiDAO.theoMa(maCongThuc);
        if (ct == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần 1 ô trống trong hành trang.");
            return;
        }
        // Kiem tra LAI ngay truoc khi tru: giua luc hien bang va luc bam Dong y,
        // nguoi choi co the da ban hoac vut mat nguyen lieu.
        Item[] dangCo = new Item[ct.nguyenLieu.size()];
        for (int i = 0; i < ct.nguyenLieu.size(); i++) {
            int[] nl = ct.nguyenLieu.get(i);
            dangCo[i] = InventoryService.gI().findItemBag(player, nl[0]);
            int sl = dangCo[i] != null ? dangCo[i].quantity : 0;
            if (sl < nl[1]) {
                return;
            }
        }
        CombineService.gI().sendAddItemCombine(player, ConstNpc.BA_HAT_MIT, dangCo);

        boolean thanhCong = Util.isTrue(ct.tyLe, 100);
        if (thanhCong) {
            traThuong(player, ct);
            for (int i = 0; i < ct.nguyenLieu.size(); i++) {
                InventoryService.gI().subQuantityItemsBag(player, dangCo[i],
                        ct.nguyenLieu.get(i)[1]);
            }
        } else {
            CombineService.gI().sendEffFailVip(player);
            Util.setTimeout(() -> CombineService.gI().baHatMit.npcChat(player,
                    "Chúc con may mắn lần sau, đừng buồn con nhé"), 2000);
            // That bai thi tru theo cot rieng — thuong it hon luc thanh cong.
            for (int[] h : ct.haoKhiThatBai) {
                Item it = InventoryService.gI().findItemBag(player, h[0]);
                if (it != null) {
                    InventoryService.gI().subQuantityItemsBag(player, it, h[1]);
                }
            }
        }
        InventoryService.gI().sendItemBag(player);
    }

    private void traThuong(Player player, CongThucDoiDAO.CongThuc ct) {
        if (ct.ketQua.isEmpty()) {
            return;
        }
        int idKq = ct.ketQua.get(Util.nextInt(ct.ketQua.size()));
        Item nhan = ItemService.gI().createNewItem((short) idKq);
        for (int[] cs : ct.chiSo) {
            nhan.itemOptions.add(new ItemOption(cs[0], cs[1]));
        }
        // Sach Tuyet Ky: so dong "- Chua giam dinh" do tab panel quyet dinh,
        // khong phai cot chi_so_ket_qua cua cong thuc. Cac chi so khac (do ben,
        // so lan tay, yeu cau suc manh...) van giu nguyen.
        if (nhan.isSachTuyetKy() || nhan.isSachTuyetKy2()) {
            nro.repository.dao.SachTuyetKyDAO.datDongChuaGiamDinh(nhan);
        }
        InventoryService.gI().addItemBag(player, nhan);
        CombineService.gI().sendEffSuccessVip(player, nhan.template.iconID);
        Util.setTimeout(() -> {
            Service.gI().sendServerMessage(player,
                    "Bạn nhận được " + nhan.template.name);
            CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con nhé");
        }, 2000);
    }

    private String tenVatPham(int id) {
        try {
            return ItemService.gI().getTemplate(id).name;
        } catch (Exception ex) {
            return "Vật phẩm " + id;
        }
    }
}
