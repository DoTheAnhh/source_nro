package nro.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import nro.entity.template.ItemTemplate;
import nro.repository.dao.MoRuongDAO;
import nro.server.Manager;

/**
 * Tab <b>Mở rương</b> của bảng điều khiển — rương gacha trong màn Sự kiện.
 *
 * <ul>
 *   <li><b>Rương & quà</b> — trái là các loại rương (tên, hình, giá x1/x10,
 *       thứ tự, bật), phải là quà của rương đang chọn với trọng số và độ hiếm;
 *       cột "Tỉ lệ" tự tính từ trọng số để khỏi phải cộng nhẩm.</li>
 *   <li><b>Điểm người chơi</b> — mỗi loại rương một loại điểm riêng; gõ tên
 *       nhân vật để xem và sửa điểm của từng loại.</li>
 *   <li><b>Lịch sử</b> — 300 lượt mở gần nhất.</li>
 * </ul>
 */
public class MoRuongTab extends JPanel {

    private final DefaultTableModel mRuong = new DefaultTableModel(
            new String[]{"Id", "Tên", "Mô tả", "Tên điểm", "Icon hình", "Vật phẩm hình", "Giá x1",
                "Giá x10", "Thứ tự", "Bật"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c != 0;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 9 ? Boolean.class : Object.class;
        }
    };
    private final JTable bangRuong = new JTable(mRuong);

    private final DefaultTableModel mQua = new DefaultTableModel(
            new String[]{"Mã", "Id vật phẩm", "Tên", "Số lượng", "Trọng số", "Tỉ lệ", "Độ hiếm", "Chỉ số"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 3 || c == 4 || c == 6 || c == 7;
        }
    };
    private final JTable bangQua = new JTable(mQua);
    private final JTextField fSoLuong = new JTextField("1", 4);
    private final JTextField fTrongSo = new JTextField("100", 5);
    private final JComboBox<String> fHiem = new JComboBox<>(MoRuongDAO.TEN_HIEM);

    private final DefaultTableModel mLichSu = new DefaultTableModel(
            new String[]{"Lúc", "Nhân vật", "Rương", "Vật phẩm", "Số lượng", "Độ hiếm"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bangLichSu = new JTable(mLichSu);
    private final JTextField fTen = new JTextField(14);
    private final JTextField fDiem = new JTextField("100", 6);
    private long idNguoiDangXem = -1;
    private final DefaultTableModel mDiem = new DefaultTableModel(
            new String[]{"Id rương", "Rương", "Tên điểm", "Điểm"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 3;
        }
    };
    private final JTable bangDiem = new JTable(mDiem);
    private final JLabel nhanNguoi = new JLabel("Chưa chọn nhân vật");

    private final JLabel nhanTrangThai = new JLabel(" ");

    public MoRuongTab() {
        setLayout(new BorderLayout(0, 6));
        setBackground(UiTheme.CARD);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel dau = new JLabel("<html><b>Mở rương</b> — tab gacha trong màn Sự kiện của game. Mở x1/x10 bằng"
                + " <b>điểm riêng của từng loại rương</b>. Tỉ lệ = trọng số / tổng trọng số của rương. Độ hiếm chỉ để tô màu;"
                + " trúng Huyền thoại thì báo cả máy chủ.</html>");
        dau.setForeground(UiTheme.TEXT_MUTED);
        add(dau, BorderLayout.NORTH);

        JTabbedPane trong = new JTabbedPane();
        trong.addTab("1. Rương & quà", theRuong());
        trong.addTab("2. Điểm người chơi", theDiem());
        trong.addTab("3. Lịch sử", theLichSu());
        add(trong, BorderLayout.CENTER);
        nhanTrangThai.setForeground(UiTheme.ACCENT_TEXT);
        add(nhanTrangThai, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                napLai();
            }
        });
    }

    private boolean daNap;

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (!daNap) {
            napLai();
        }
    }

    private void napLai() {
        if (Manager.ITEM_TEMPLATES.isEmpty()) {
            return;
        }
        daNap = true;
        napRuong();
        napLichSu();
    }

    // =====================================================================
    //  Rương & quà
    // =====================================================================
    private JPanel theRuong() {
        JPanel trai = new JPanel(new BorderLayout(0, 4));
        trai.setBackground(UiTheme.CARD);
        bangRuong.setRowHeight(24);
        bangRuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangRuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                napQua();
            }
        });
        trai.add(ServerGuiUtils.cuon(bangRuong), BorderLayout.CENTER);
        JPanel nutTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nutTrai.setBackground(UiTheme.CARD);
        JButton themR = new JButton("Thêm rương");
        ServerGuiUtils.toNut(themR, UiTheme.OK);
        themR.addActionListener(e -> themRuong());
        nutTrai.add(themR);
        JButton luuR = new JButton("Lưu rương");
        ServerGuiUtils.toNut(luuR, UiTheme.ACCENT);
        luuR.addActionListener(e -> luuRuong());
        nutTrai.add(luuR);
        JButton hinh = new JButton("Chọn hình…");
        hinh.addActionListener(e -> chonHinh());
        nutTrai.add(hinh);
        JButton xoaR = new JButton("Xoá rương");
        ServerGuiUtils.toNut(xoaR, UiTheme.DANGER);
        xoaR.addActionListener(e -> xoaRuong());
        nutTrai.add(xoaR);
        trai.add(nutTrai, BorderLayout.SOUTH);

        JPanel phai = new JPanel(new BorderLayout(0, 4));
        phai.setBackground(UiTheme.CARD);
        bangQua.setRowHeight(24);
        bangQua.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangQua.getColumnModel().getColumn(6).setCellEditor(
                new javax.swing.DefaultCellEditor(new JComboBox<>(MoRuongDAO.TEN_HIEM)));
        phai.add(ServerGuiUtils.cuon(bangQua), BorderLayout.CENTER);
        JPanel nutPhai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nutPhai.setBackground(UiTheme.CARD);
        nutPhai.add(new JLabel("SL"));
        nutPhai.add(fSoLuong);
        nutPhai.add(new JLabel("Trọng số"));
        nutPhai.add(fTrongSo);
        nutPhai.add(fHiem);
        JButton themQ = new JButton("Chọn vật phẩm & thêm…");
        ServerGuiUtils.toNut(themQ, UiTheme.OK);
        themQ.addActionListener(e -> themQua());
        nutPhai.add(themQ);
        JButton luuQ = new JButton("Lưu quà");
        ServerGuiUtils.toNut(luuQ, UiTheme.ACCENT);
        luuQ.addActionListener(e -> luuQua());
        nutPhai.add(luuQ);
        JButton xoaQ = new JButton("Xoá quà");
        ServerGuiUtils.toNut(xoaQ, UiTheme.DANGER);
        xoaQ.addActionListener(e -> xoaQua());
        nutPhai.add(xoaQ);
        phai.add(nutPhai, BorderLayout.SOUTH);

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trai, phai);
        chia.setResizeWeight(0.45);
        chia.setBorder(null);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UiTheme.CARD);
        p.add(chia, BorderLayout.CENTER);
        return p;
    }

    private int ruongDangChon() {
        int r = bangRuong.getSelectedRow();
        return r < 0 ? -1 : soNguyen(mRuong.getValueAt(bangRuong.convertRowIndexToModel(r), 0), -1);
    }

    private void napRuong() {
        int giu = ruongDangChon();
        if (bangRuong.isEditing()) {
            bangRuong.getCellEditor().stopCellEditing();
        }
        mRuong.setRowCount(0);
        int chonLai = -1;
        for (MoRuongDAO.Ruong r : MoRuongDAO.dsRuong(false)) {
            ItemTemplate t = nro.service.item.ItemService.gI().getTemplate(r.itemHinh);
            mRuong.addRow(new Object[]{r.id, r.ten, r.moTa, r.tenDiem, r.iconHinh,
                r.itemHinh + (t == null ? "" : " · " + t.name), r.giaX1, r.giaX10, r.thuTu, r.bat});
            if (r.id == giu) {
                chonLai = mRuong.getRowCount() - 1;
            }
        }
        if (mRuong.getRowCount() > 0) {
            int dong = chonLai >= 0 ? chonLai : 0;
            bangRuong.setRowSelectionInterval(dong, dong);
        }
        napQua();
    }

    private void themRuong() {
        String ten = JOptionPane.showInputDialog(this, "Tên rương mới:", "Rương mới");
        if (ten == null || ten.trim().isEmpty()) {
            return;
        }
        try {
            MoRuongDAO.themRuong(ten.trim(), "", 571, 10, 90, 99);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thêm được: " + ex.getMessage());
        }
        napRuong();
        bao("Đã thêm rương \"" + ten.trim() + "\" — chọn nó để thêm quà.");
    }

    private void luuRuong() {
        if (bangRuong.isEditing()) {
            bangRuong.getCellEditor().stopCellEditing();
        }
        for (int r = 0; r < mRuong.getRowCount(); r++) {
            MoRuongDAO.Ruong x = new MoRuongDAO.Ruong();
            x.id = soNguyen(mRuong.getValueAt(r, 0), -1);
            x.ten = String.valueOf(mRuong.getValueAt(r, 1)).trim();
            x.moTa = String.valueOf(mRuong.getValueAt(r, 2)).trim();
            x.tenDiem = String.valueOf(mRuong.getValueAt(r, 3)).trim();
            x.iconHinh = Math.max(0, soNguyen(mRuong.getValueAt(r, 4), 0));
            String hinh = String.valueOf(mRuong.getValueAt(r, 5));
            x.itemHinh = soNguyen(hinh.contains("·") ? hinh.substring(0, hinh.indexOf('·')).trim() : hinh, 0);
            x.giaX1 = Math.max(0, soNguyen(mRuong.getValueAt(r, 6), 10));
            x.giaX10 = Math.max(0, soNguyen(mRuong.getValueAt(r, 7), 90));
            x.thuTu = soNguyen(mRuong.getValueAt(r, 8), 0);
            x.bat = Boolean.TRUE.equals(mRuong.getValueAt(r, 9));
            MoRuongDAO.luuRuong(x);
        }
        napRuong();
        bao("Đã lưu rương.");
    }

    private void chonHinh() {
        int r = bangRuong.getSelectedRow();
        if (r < 0) {
            return;
        }
        int id = OptionPicker.chonVatPham(this, -1);
        if (id < 0) {
            return;
        }
        ItemTemplate t = nro.service.item.ItemService.gI().getTemplate(id);
        int dong = bangRuong.convertRowIndexToModel(r);
        mRuong.setValueAt(id + (t == null ? "" : " · " + t.name), dong, 5);
        // Chon hinh theo vat pham thi bo icon go tay, khong thi icon go tay van de len.
        mRuong.setValueAt(0, dong, 4);
        bao("Đã đổi hình theo vật phẩm — bấm Lưu rương để ghi. Muốn dùng hình riêng thì gõ id icon vào cột Icon hình.");
    }

    private void xoaRuong() {
        int id = ruongDangChon();
        if (id < 0) {
            return;
        }
        int chac = JOptionPane.showConfirmDialog(this, "Xoá rương và toàn bộ quà của nó?", "Xoá rương",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chac != JOptionPane.OK_OPTION) {
            return;
        }
        MoRuongDAO.xoaRuong(id);
        napRuong();
    }

    private void napQua() {
        if (bangQua.isEditing()) {
            bangQua.getCellEditor().stopCellEditing();
        }
        mQua.setRowCount(0);
        int id = ruongDangChon();
        if (id < 0) {
            return;
        }
        List<MoRuongDAO.Qua> ds = MoRuongDAO.dsQuaDayDu(id);
        long tong = 0;
        for (MoRuongDAO.Qua q : ds) {
            tong += Math.max(0, q.trongSo);
        }
        for (MoRuongDAO.Qua q : ds) {
            ItemTemplate t = nro.service.item.ItemService.gI().getTemplate(q.itemId);
            mQua.addRow(new Object[]{q.id, q.itemId, t == null ? "?" : t.name, q.soLuong, q.trongSo,
                nro.service.MoRuongService.tiLe(Math.max(0, q.trongSo), tong),
                MoRuongDAO.TEN_HIEM[Math.max(0, Math.min(4, q.hiem))], q.chiSo});
        }
    }

    private void themQua() {
        int id = ruongDangChon();
        if (id < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một rương bên trái trước.");
            return;
        }
        int sl = soNguyen(fSoLuong.getText(), 0);
        int ts = soNguyen(fTrongSo.getText(), -1);
        if (sl <= 0 || ts < 0) {
            JOptionPane.showMessageDialog(this, "Cần số lượng > 0 và trọng số ≥ 0.");
            return;
        }
        List<Integer> chon = OptionPicker.chonNhieuVatPham(this, -1);
        for (int itemId : chon) {
            try {
                MoRuongDAO.themQua(id, itemId, sl, ts, fHiem.getSelectedIndex(), "");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thêm được: " + ex.getMessage());
                break;
            }
        }
        napQua();
    }

    private void luuQua() {
        if (bangQua.isEditing()) {
            bangQua.getCellEditor().stopCellEditing();
        }
        for (int r = 0; r < mQua.getRowCount(); r++) {
            MoRuongDAO.Qua q = new MoRuongDAO.Qua();
            q.id = soNguyen(mQua.getValueAt(r, 0), -1);
            q.soLuong = soNguyen(mQua.getValueAt(r, 3), 1);
            q.trongSo = soNguyen(mQua.getValueAt(r, 4), 0);
            q.hiem = java.util.Arrays.asList(MoRuongDAO.TEN_HIEM).indexOf(String.valueOf(mQua.getValueAt(r, 6)));
            q.chiSo = String.valueOf(mQua.getValueAt(r, 7));
            MoRuongDAO.luuQua(q);
        }
        napQua();
        bao("Đã lưu quà.");
    }

    private void xoaQua() {
        int r = bangQua.getSelectedRow();
        if (r < 0) {
            return;
        }
        MoRuongDAO.xoaQua(soNguyen(mQua.getValueAt(bangQua.convertRowIndexToModel(r), 0), -1));
        napQua();
    }

    // =====================================================================
    //  Điểm & lịch sử
    // =====================================================================
    private JPanel theDiem() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);
        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        tren.setBackground(UiTheme.CARD);
        tren.add(new JLabel("Nhân vật:"));
        tren.add(fTen);
        JButton tim = new JButton("Xem điểm");
        ServerGuiUtils.toNut(tim, UiTheme.ACCENT);
        tim.addActionListener(e -> xemDiem());
        fTen.addActionListener(e -> xemDiem());
        tren.add(tim);
        nhanNguoi.setForeground(UiTheme.TEXT_MUTED);
        tren.add(nhanNguoi);
        p.add(tren, BorderLayout.NORTH);

        bangDiem.setRowHeight(24);
        bangDiem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(ServerGuiUtils.cuon(bangDiem), BorderLayout.CENTER);

        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        duoi.setBackground(UiTheme.CARD);
        JButton luu = new JButton("Lưu điểm");
        ServerGuiUtils.toNut(luu, UiTheme.OK);
        luu.addActionListener(e -> luuDiem());
        duoi.add(luu);
        duoi.add(new JLabel("   Cộng nhanh vào dòng đang chọn:"));
        duoi.add(fDiem);
        JButton cong = new JButton("Cộng (âm là trừ)");
        cong.addActionListener(e -> congNhanh());
        duoi.add(cong);
        JLabel ghi = new JLabel("  Sửa thẳng cột Điểm rồi bấm Lưu. Người đang online thấy ngay.");
        ghi.setForeground(UiTheme.TEXT_MUTED);
        duoi.add(ghi);
        p.add(duoi, BorderLayout.SOUTH);
        return p;
    }

    private void xemDiem() {
        String ten = fTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gõ tên nhân vật.");
            return;
        }
        long id = MoRuongDAO.idTheoTen(ten);
        if (id < 0) {
            JOptionPane.showMessageDialog(this, "Không có nhân vật tên \"" + ten + "\".");
            return;
        }
        idNguoiDangXem = id;
        nhanNguoi.setText(ten + " (id " + id + ")");
        napDiem();
    }

    private void napDiem() {
        if (bangDiem.isEditing()) {
            bangDiem.getCellEditor().stopCellEditing();
        }
        mDiem.setRowCount(0);
        if (idNguoiDangXem < 0) {
            return;
        }
        java.util.Map<Integer, Long> diem = MoRuongDAO.diemCuaNguoi(idNguoiDangXem);
        for (MoRuongDAO.Ruong r : MoRuongDAO.dsRuong(false)) {
            mDiem.addRow(new Object[]{r.id, r.ten, r.tenDiemDeDoc(), diem.getOrDefault(r.id, 0L)});
        }
    }

    private void luuDiem() {
        if (idNguoiDangXem < 0) {
            return;
        }
        if (bangDiem.isEditing()) {
            bangDiem.getCellEditor().stopCellEditing();
        }
        java.util.Map<Integer, Long> cu = MoRuongDAO.diemCuaNguoi(idNguoiDangXem);
        StringBuilder kq = new StringBuilder();
        for (int r = 0; r < mDiem.getRowCount(); r++) {
            int ruongId = soNguyen(mDiem.getValueAt(r, 0), -1);
            long moi;
            try {
                moi = Math.max(0, Long.parseLong(String.valueOf(mDiem.getValueAt(r, 3)).trim()));
            } catch (NumberFormatException sai) {
                continue;
            }
            if (moi != cu.getOrDefault(ruongId, 0L)) {
                if (kq.length() > 0) {
                    kq.append(" · ");
                }
                kq.append(nro.service.MoRuongService.gI().datDiem(idNguoiDangXem, ruongId, moi));
            }
        }
        napDiem();
        bao(kq.length() == 0 ? "Không có điểm nào đổi." : kq.toString());
    }

    private void congNhanh() {
        int r = bangDiem.getSelectedRow();
        int them = soNguyen(fDiem.getText(), 0);
        if (idNguoiDangXem < 0 || r < 0 || them == 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng rương và gõ số điểm (âm là trừ).");
            return;
        }
        int dong = bangDiem.convertRowIndexToModel(r);
        int ruongId = soNguyen(mDiem.getValueAt(dong, 0), -1);
        long moi = Math.max(0, MoRuongDAO.diem(idNguoiDangXem, ruongId) + them);
        bao(nro.service.MoRuongService.gI().datDiem(idNguoiDangXem, ruongId, moi));
        napDiem();
    }

    private JPanel theLichSu() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);
        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        tren.setBackground(UiTheme.CARD);
        JButton tai = new JButton("Tải lại lịch sử");
        tai.addActionListener(e -> napLichSu());
        tren.add(tai);
        p.add(tren, BorderLayout.NORTH);
        bangLichSu.setRowHeight(22);
        p.add(ServerGuiUtils.cuon(bangLichSu), BorderLayout.CENTER);
        return p;
    }

    private void napLichSu() {
        mLichSu.setRowCount(0);
        SimpleDateFormat f = new SimpleDateFormat("dd/MM HH:mm:ss");
        java.util.Map<Integer, String> tenRuong = new java.util.HashMap<>();
        for (MoRuongDAO.Ruong r : MoRuongDAO.dsRuong(false)) {
            tenRuong.put(r.id, r.ten);
        }
        for (MoRuongDAO.DongLichSu d : MoRuongDAO.lichSu(300)) {
            ItemTemplate t = nro.service.item.ItemService.gI().getTemplate(d.itemId);
            mLichSu.addRow(new Object[]{f.format(new Date(d.luc)), d.nguoi,
                tenRuong.getOrDefault(d.ruongId, "#" + d.ruongId), t == null ? ("#" + d.itemId) : t.name,
                d.soLuong, MoRuongDAO.TEN_HIEM[Math.max(0, Math.min(4, d.hiem))]});
        }
    }

    private void bao(String s) {
        nhanTrangThai.setText(s);
    }

    private static int soNguyen(Object o, int duPhong) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException sai) {
            return duPhong;
        }
    }
}
