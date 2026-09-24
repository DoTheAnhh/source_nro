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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import nro.entity.template.ItemTemplate;
import nro.repository.dao.TheThangDAO;
import nro.server.Manager;

/**
 * Tab <b>Thẻ tháng</b> của bảng điều khiển.
 *
 * <ul>
 *   <li><b>Gói</b> — giá, loại tiền, số ngày và phần trăm ưu đãi của từng bậc.
 *       Sửa thẳng trong bảng rồi bấm Lưu; ưu đãi có hiệu lực ở lần tính chỉ số
 *       kế tiếp của mỗi người.</li>
 *   <li><b>Quà</b> — chọn bậc và loại (nhận ngay lúc mua / mỗi ngày), thêm vật
 *       phẩm bằng hộp tra cứu.</li>
 *   <li><b>Người đang có thẻ</b> — và ô tặng thẻ theo tên nhân vật, để hỗ trợ
 *       người chơi khi nạp lỗi.</li>
 * </ul>
 */
public class TheThangTab extends JPanel {

    private static final String[] COT_GOI = {"Bậc", "Tên", "Giá", "Loại tiền", "Số ngày",
        "% HP", "% KI", "% Sức đánh", "% Tiềm năng", "Bật"};

    private final DefaultTableModel mGoi = new DefaultTableModel(COT_GOI, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c != 0;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 9 ? Boolean.class : Object.class;
        }
    };
    private final JTable bangGoi = new JTable(mGoi);

    private static final String[] COT_QUA = {"Mã", "Id vật phẩm", "Tên", "Số lượng", "Chỉ số (50=5,77=10)"};

    private final DefaultTableModel mQua = new DefaultTableModel(COT_QUA, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 3 || c == 4;
        }
    };
    private final JTable bangQua = new JTable(mQua);
    private final JComboBox<String> chonBacQua = new JComboBox<>(new String[]{"Bậc 1", "Bậc 2"});
    private final JComboBox<String> chonLoaiQua = new JComboBox<>(
            new String[]{"Nhận ngay lúc mua", "Nhận mỗi ngày"});
    private final JTextField fSoLuongMoi = new JTextField("1", 5);

    private static final String[] COT_NGUOI = {"Id", "Nhân vật", "Thẻ", "Hết hạn", "Còn (ngày)", "Lượt mua"};

    private final DefaultTableModel mNguoi = new DefaultTableModel(COT_NGUOI, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bangNguoi = new JTable(mNguoi);
    private final JTextField fTenTang = new JTextField(14);
    private final JComboBox<String> chonBacTang = new JComboBox<>(new String[]{"Bậc 1", "Bậc 2"});
    private final JTextField fNgayTang = new JTextField("30", 4);
    private final JLabel nhanTrangThai = new JLabel(" ");

    public TheThangTab() {
        setLayout(new BorderLayout(0, 6));
        setBackground(UiTheme.CARD);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel dau = new JLabel("<html>Thẻ tháng — bán trong màn <b>Phúc lợi</b> của game. Người chơi mua"
                + " một lần, được ưu đãi chỉ số suốt thời hạn và mỗi ngày vào nhận quà."
                + " Sửa xong có hiệu lực ngay, không cần khởi động lại.</html>");
        dau.setForeground(UiTheme.TEXT_MUTED);
        add(dau, BorderLayout.NORTH);

        JSplitPane duoi = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, theQua(), theNguoi());
        duoi.setResizeWeight(0.5);
        duoi.setBorder(null);
        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, theGoi(), duoi);
        chia.setResizeWeight(0.3);
        chia.setBorder(null);
        add(chia, BorderLayout.CENTER);

        nhanTrangThai.setForeground(UiTheme.ACCENT_TEXT);
        add(nhanTrangThai, BorderLayout.SOUTH);

        // Nap khi tab mo ra, khong nap trong ham dung: panel dung xong truoc
        // khi may chu nap bang vat pham (xem ThuCungTab).
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
        napGoi();
        napQua();
        napNguoi();
    }

    // =====================================================================
    //  Gói
    // =====================================================================
    private JPanel theGoi() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UiTheme.LINE, 1, true), "Gói thẻ"));
        bangGoi.setRowHeight(24);
        bangGoi.getColumnModel().getColumn(3).setCellEditor(
                new javax.swing.DefaultCellEditor(new JComboBox<>(TheThangDAO.CAC_LOAI_TIEN)));
        p.add(ServerGuiUtils.cuon(bangGoi), BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nut.setBackground(UiTheme.CARD);
        JButton luu = new JButton("Lưu gói");
        ServerGuiUtils.toNut(luu, UiTheme.ACCENT);
        luu.addActionListener(e -> luuGoi());
        nut.add(luu);
        JLabel ghi = new JLabel("Loại tiền: vnd = số dư nạp, hong_ngoc, ngoc. Bỏ tick Bật là ngừng bán gói đó.");
        ghi.setForeground(UiTheme.TEXT_MUTED);
        nut.add(ghi);
        p.add(nut, BorderLayout.SOUTH);
        return p;
    }

    private void napGoi() {
        if (bangGoi.isEditing()) {
            bangGoi.getCellEditor().stopCellEditing();
        }
        mGoi.setRowCount(0);
        for (TheThangDAO.Goi g : TheThangDAO.dsGoi()) {
            mGoi.addRow(new Object[]{g.bac, g.ten, g.gia, g.loaiTien, g.soNgay,
                g.ptHp, g.ptKi, g.ptSd, g.ptTiemNang, g.bat});
        }
    }

    private void luuGoi() {
        if (bangGoi.isEditing()) {
            bangGoi.getCellEditor().stopCellEditing();
        }
        for (int r = 0; r < mGoi.getRowCount(); r++) {
            TheThangDAO.Goi g = new TheThangDAO.Goi();
            g.bac = soNguyen(mGoi.getValueAt(r, 0), 1);
            g.ten = String.valueOf(mGoi.getValueAt(r, 1)).trim();
            g.gia = Math.max(0, soDai(mGoi.getValueAt(r, 2), 0));
            g.loaiTien = String.valueOf(mGoi.getValueAt(r, 3));
            g.soNgay = Math.max(1, soNguyen(mGoi.getValueAt(r, 4), 30));
            g.ptHp = Math.max(0, soNguyen(mGoi.getValueAt(r, 5), 0));
            g.ptKi = Math.max(0, soNguyen(mGoi.getValueAt(r, 6), 0));
            g.ptSd = Math.max(0, soNguyen(mGoi.getValueAt(r, 7), 0));
            g.ptTiemNang = Math.max(0, soNguyen(mGoi.getValueAt(r, 8), 0));
            g.bat = Boolean.TRUE.equals(mGoi.getValueAt(r, 9));
            TheThangDAO.luuGoi(g);
        }
        napGoi();
        bao("Đã lưu gói thẻ.");
    }

    // =====================================================================
    //  Quà
    // =====================================================================
    private JPanel theQua() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UiTheme.LINE, 1, true), "Quà"));

        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        tren.setBackground(UiTheme.CARD);
        tren.add(chonBacQua);
        tren.add(chonLoaiQua);
        chonBacQua.addActionListener(e -> napQua());
        chonLoaiQua.addActionListener(e -> napQua());
        p.add(tren, BorderLayout.NORTH);

        bangQua.setRowHeight(24);
        bangQua.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(ServerGuiUtils.cuon(bangQua), BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nut.setBackground(UiTheme.CARD);
        nut.add(new JLabel("Số lượng:"));
        nut.add(fSoLuongMoi);
        JButton them = new JButton("Chọn vật phẩm & thêm…");
        ServerGuiUtils.toNut(them, UiTheme.OK);
        them.addActionListener(e -> themQua());
        nut.add(them);
        JButton luu = new JButton("Lưu số lượng / chỉ số");
        ServerGuiUtils.toNut(luu, UiTheme.ACCENT);
        luu.addActionListener(e -> luuQua());
        nut.add(luu);
        JButton xoa = new JButton("Xoá dòng");
        ServerGuiUtils.toNut(xoa, UiTheme.DANGER);
        xoa.addActionListener(e -> xoaQua());
        nut.add(xoa);
        p.add(nut, BorderLayout.SOUTH);
        return p;
    }

    private int bacQua() {
        return chonBacQua.getSelectedIndex() + 1;
    }

    private int loaiQua() {
        return chonLoaiQua.getSelectedIndex() == 0 ? TheThangDAO.QUA_MUA : TheThangDAO.QUA_NGAY;
    }

    private void napQua() {
        if (bangQua.isEditing()) {
            bangQua.getCellEditor().stopCellEditing();
        }
        mQua.setRowCount(0);
        for (TheThangDAO.Qua q : TheThangDAO.dsQua(bacQua(), loaiQua())) {
            ItemTemplate t = nro.service.item.ItemService.gI().getTemplate(q.itemId);
            mQua.addRow(new Object[]{q.id, q.itemId, t == null ? "?" : t.name, q.soLuong, q.chiSo});
        }
    }

    private void themQua() {
        int sl = soNguyen(fSoLuongMoi.getText(), 0);
        if (sl <= 0) {
            JOptionPane.showMessageDialog(this, "Gõ số lượng (lớn hơn 0) trước, rồi mới chọn vật phẩm.");
            return;
        }
        List<Integer> chon = OptionPicker.chonNhieuVatPham(this, -1);
        for (int id : chon) {
            TheThangDAO.themQua(bacQua(), loaiQua(), id, sl, "");
        }
        napQua();
        if (!chon.isEmpty()) {
            bao("Đã thêm " + chon.size() + " món.");
        }
    }

    private void luuQua() {
        if (bangQua.isEditing()) {
            bangQua.getCellEditor().stopCellEditing();
        }
        for (int r = 0; r < mQua.getRowCount(); r++) {
            TheThangDAO.suaQua(soNguyen(mQua.getValueAt(r, 0), -1),
                    soNguyen(mQua.getValueAt(r, 3), 1), String.valueOf(mQua.getValueAt(r, 4)));
        }
        napQua();
        bao("Đã lưu quà.");
    }

    private void xoaQua() {
        int r = bangQua.getSelectedRow();
        if (r < 0) {
            return;
        }
        TheThangDAO.xoaQua(soNguyen(mQua.getValueAt(bangQua.convertRowIndexToModel(r), 0), -1));
        napQua();
    }

    // =====================================================================
    //  Người đang có thẻ + tặng thẻ
    // =====================================================================
    private JPanel theNguoi() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UiTheme.LINE, 1, true), "Người đang có thẻ"));
        bangNguoi.setRowHeight(22);
        p.add(ServerGuiUtils.cuon(bangNguoi), BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nut.setBackground(UiTheme.CARD);
        nut.add(new JLabel("Tặng cho:"));
        nut.add(fTenTang);
        nut.add(chonBacTang);
        nut.add(fNgayTang);
        nut.add(new JLabel("ngày"));
        JButton tang = new JButton("Tặng thẻ");
        ServerGuiUtils.toNut(tang, UiTheme.OK);
        tang.addActionListener(e -> tangThe());
        nut.add(tang);
        JButton tai = new JButton("Tải lại");
        tai.addActionListener(e -> napLai());
        nut.add(tai);
        p.add(nut, BorderLayout.SOUTH);
        return p;
    }

    private void napNguoi() {
        mNguoi.setRowCount(0);
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        long bayGio = System.currentTimeMillis();
        for (TheThangDAO.NguoiCoThe n : TheThangDAO.dsNguoiCoThe()) {
            long con = (n.hetHan - bayGio + TheThangDAO.MOT_NGAY_MS - 1) / TheThangDAO.MOT_NGAY_MS;
            mNguoi.addRow(new Object[]{n.playerId, n.ten, TheThangDAO.tenBac(n.bac),
                f.format(new Date(n.hetHan)), con, n.lanMua});
        }
    }

    private void tangThe() {
        String ten = fTenTang.getText().trim();
        int ngay = soNguyen(fNgayTang.getText(), 0);
        if (ten.isEmpty() || ngay <= 0) {
            JOptionPane.showMessageDialog(this, "Cần tên nhân vật và số ngày lớn hơn 0.");
            return;
        }
        long id = TheThangDAO.idTheoTen(ten);
        if (id < 0) {
            JOptionPane.showMessageDialog(this, "Không có nhân vật tên \"" + ten + "\".");
            return;
        }
        int bac = chonBacTang.getSelectedIndex() + 1;
        int chac = JOptionPane.showConfirmDialog(this, "Tặng " + ngay + " ngày "
                + TheThangDAO.tenBac(bac) + " cho " + ten + "?", "Tặng thẻ",
                JOptionPane.OK_CANCEL_OPTION);
        if (chac != JOptionPane.OK_OPTION) {
            return;
        }
        bao(nro.service.TheThangService.gI().tangThe(id, bac, ngay));
        napNguoi();
    }

    // =====================================================================
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

    private static long soDai(Object o, long duPhong) {
        try {
            return Long.parseLong(String.valueOf(o).trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException sai) {
            return duPhong;
        }
    }
}
