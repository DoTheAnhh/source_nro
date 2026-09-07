package nro.service;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.player.Player;
import nro.entity.template.ItemTemplate;
import nro.net.io.Message;
import nro.repository.dao.SuKienDAO;
import nro.service.item.ItemService;

/**
 * Gửi dữ liệu <b>Sự kiện</b> xuống client để nó vẽ màn hình riêng.
 *
 * <p>Dữ liệu lấy thẳng từ ba bảng mà tab "Sự kiện" trên panel đang quản lý:
 * {@code su_kien}, {@code su_kien_vat_pham_roi}, {@code su_kien_cong_thuc_doi}.
 * Sửa ở panel là trong game thấy ngay, không phải khai lại lần nữa.</p>
 *
 * <h2>Vì sao chỉ gửi, không nhận</h2>
 *
 * <p>Màn này chỉ để <b>xem</b>: người chơi không bấm nhận gì ở đây, đồ rơi thì
 * tự rơi khi đánh quái, đổi đồ thì tới NPC Quy Lão. Nên gói chỉ đi một chiều —
 * client xin, máy chủ trả, hết.</p>
 */
public class SuKienManHinhService {

    private static SuKienManHinhService instance;

    public static SuKienManHinhService gI() {
        if (instance == null) {
            instance = new SuKienManHinhService();
        }
        return instance;
    }

    /** Mã gói màn hình sự kiện. */
    public static final byte OPCODE = -48;

    /**
     * Gửi các sự kiện <b>đang chạy</b> kèm đồ rơi và công thức đổi.
     *
     * <h3>Khuôn gói</h3>
     * <pre>
     * byte  soSuKien
     *   UTF   ten
     *   UTF   khungGio        ("14/02 → 20/02" hoặc "không giới hạn")
     *   UTF   thongBao        (câu loa, có thể rỗng)
     *   byte  soDoRoi
     *     short iconId
     *     UTF   tenMon
     *     int   soLuongMin
     *     int   soLuongMax
     *     UTF   tiLe           ("5%")
     *     &lt;nội dung gói&gt;
     *   byte  soCongThuc
     *     UTF   tenCongThuc
     *     short iconNhan
     *     UTF   tenNhan
     *     int   soLuongNhan
     *     &lt;nội dung gói&gt;
     *     byte  soNguyenLieu
     *       short iconId
     *       UTF   tenMon
     *       int   soLuong
     * </pre>
     *
     * <p>Trong đó &lt;nội dung gói&gt; là:</p>
     * <pre>
     * byte  soMonTrongGoi     (0 nếu món này không phải gói quà)
     *   short iconId
     *   UTF   tenMon
     *   int   soLuongMin
     *   int   soLuongMax
     *   UTF   tiLe            ("30%")
     * </pre>
     *
     * <h3>Vì sao gửi rời từng món thay vì một chuỗi</h3>
     *
     * <p>Trước đây nguyên liệu được gộp sẵn thành "3x Bánh, 1x Kẹo" rồi gửi
     * xuống một dòng. Công thức bốn năm món là dòng đó dài quá bề ngang, client
     * cắt bớt thành "…" — người chơi không biết còn thiếu món gì. Gửi rời thì
     * client mở được bảng chi tiết liệt kê đủ, kèm số lượng từng món.</p>
     */
    public void guiDuLieu(Player pl) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            // Chi gui su kien DANG CHAY: liet ke ca su kien tat hay het han chi
            // lam nguoi choi tuong con nhan duoc.
            List<SuKienDAO.SuKien> chay = new ArrayList<>();
            for (SuKienDAO.SuKien sk : SuKienDAO.danhSach()) {
                if (sk.dangChay()) {
                    chay.add(sk);
                }
            }
            msg = new Message(OPCODE);
            msg.writer().writeByte(chay.size());
            for (SuKienDAO.SuKien sk : chay) {
                msg.writer().writeUTF(nz(sk.ten));
                msg.writer().writeUTF(nz(sk.moTaKhungGio()));
                msg.writer().writeUTF(nz(sk.thongBao));
                guiDoRoi(msg, SuKienDAO.danhSachRoi(sk.id));
                guiCongThuc(msg, SuKienDAO.danhSachDoi(sk.id));
            }
            msg.writer().flush();
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(SuKienManHinhService.class, ex,
                    "Lỗi gửi dữ liệu sự kiện");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Ghi phần đồ rơi thêm từ quái.
     *
     * <p>Gửi khoảng số lượng thành hai số rời chứ không gộp sẵn thành chuỗi
     * "1-3 món": client cần hai đầu để tự viết ra chỗ nào cũng được, và khi hai
     * mốc bằng nhau thì nó biết viết một số cho gọn.</p>
     */
    private void guiDoRoi(Message msg, List<SuKienDAO.VatPhamRoi> roi)
            throws Exception {
        // Dem truoc roi moi ghi: dong bi tat thi bo qua, ma so dem da ghi roi
        // thi client doc lech ca goi tu day tro di.
        int so = 0;
        for (SuKienDAO.VatPhamRoi v : roi) {
            if (v.bat) {
                so++;
            }
        }
        msg.writer().writeByte(so);
        for (SuKienDAO.VatPhamRoi v : roi) {
            if (!v.bat) {
                continue;
            }
            ItemTemplate t = mau(v.itemId);
            msg.writer().writeShort(t == null ? -1 : t.iconID);
            msg.writer().writeUTF(t == null ? ("#" + v.itemId) : t.name);
            msg.writer().writeInt(v.soLuongMin);
            msg.writer().writeInt(v.soLuongMax);
            msg.writer().writeUTF(gonSo(v.tiLe) + "%");
            guiNoiDungGoi(msg, v.itemId);
        }
    }

    /**
     * Ghi nội dung bên trong một gói quà, nếu vật phẩm này là gói.
     *
     * <p>Nội dung khoá theo <b>id vật phẩm gói</b> trong bảng {@code su_kien_hop},
     * không theo sự kiện — nên chỉ cần biết id món thưởng là tra được, dù nó
     * thuộc sự kiện nào hay bán ngoài shop.</p>
     *
     * <p>Bảng lưu <b>trọng số</b> chứ không lưu phần trăm: quản trị thêm một dòng
     * mới là các dòng cũ tự loãng ra, không phải ngồi cộng lại cho tròn 100. Đổi
     * sang phần trăm ở đây vì người chơi đọc "30%" mới hiểu, chứ "trọng số 3"
     * thì không nói lên gì khi chưa biết tổng.</p>
     *
     * <p>Món không phải gói thì ghi số 0 — client đọc xong biết không có gì bên
     * trong. Vẫn phải ghi một byte: bỏ hẳn thì client đọc lệch cả gói.</p>
     */
    private void guiNoiDungGoi(Message msg, int itemId) throws Exception {
        List<SuKienDAO.PhanThuongHop> trong = SuKienDAO.danhSachHop(itemId);
        int tong = 0;
        int so = 0;
        for (SuKienDAO.PhanThuongHop p : trong) {
            if (p.bat && p.trongSo > 0) {
                tong += p.trongSo;
                so++;
            }
        }
        msg.writer().writeByte(so);
        if (so == 0) {
            return;
        }
        for (SuKienDAO.PhanThuongHop p : trong) {
            if (!p.bat || p.trongSo <= 0) {
                continue;
            }
            ItemTemplate t = mau(p.itemId);
            msg.writer().writeShort(t == null ? -1 : t.iconID);
            msg.writer().writeUTF(t == null ? ("#" + p.itemId) : t.name);
            msg.writer().writeInt(p.soLuongMin);
            msg.writer().writeInt(p.soLuongMax);
            msg.writer().writeUTF(gonSo(p.trongSo * 100.0 / tong) + "%");
        }
    }

    /**
     * Ghi phần công thức đổi ở Quy Lão, mỗi công thức kèm danh sách nguyên liệu.
     */
    private void guiCongThuc(Message msg, List<SuKienDAO.CongThucDoi> doi)
            throws Exception {
        int so = 0;
        for (SuKienDAO.CongThucDoi c : doi) {
            if (c.bat) {
                so++;
            }
        }
        msg.writer().writeByte(so);
        for (SuKienDAO.CongThucDoi c : doi) {
            if (!c.bat) {
                continue;
            }
            ItemTemplate tn = mau(c.itemNhan);
            msg.writer().writeUTF(nz(c.ten));
            msg.writer().writeShort(tn == null ? -1 : tn.iconID);
            msg.writer().writeUTF(tn == null ? ("#" + c.itemNhan) : tn.name);
            msg.writer().writeInt(c.soLuongNhan);
            guiNoiDungGoi(msg, c.itemNhan);

            int soNL = 0;
            for (int[] nl : c.nguyenLieu) {
                if (nl != null && nl.length >= 2) {
                    soNL++;
                }
            }
            msg.writer().writeByte(soNL);
            for (int[] nl : c.nguyenLieu) {
                if (nl == null || nl.length < 2) {
                    continue;
                }
                ItemTemplate t = mau(nl[0]);
                msg.writer().writeShort(t == null ? -1 : t.iconID);
                msg.writer().writeUTF(t == null ? ("#" + nl[0]) : t.name);
                msg.writer().writeInt(nl[1]);
            }
        }
    }

    /** Bỏ đuôi ",0" cho số tròn: "5" đọc dễ hơn "5.0". */
    private String gonSo(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v)
                : String.format(java.util.Locale.US, "%.2f", v);
    }

    private ItemTemplate mau(int id) {
        try {
            return ItemService.gI().getTemplate(id);
        } catch (Exception ignored) {
            // Bang mau chua nap -> client hien "#id" thay ten.
            return null;
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Dùng chung với {@link Util} cho khỏi cảnh báo import thừa. */
    @SuppressWarnings("unused")
    private static String dinhDang(long v) {
        return Util.format(v);
    }
}
