package nro.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import nro.entity.template.ItemTemplate;
import nro.repository.dao.NroPassDAO;
import nro.server.Manager;

/**
 * Tab <b>NRO Pass</b> của bảng điều khiển.
 *
 * <ul>
 *   <li><b>Cấu hình</b> — tên, mùa, số cấp, điểm mỗi cấp, điểm theo độ khó
 *       nhiệm vụ Bò Mộng, giá cao cấp, ưu đãi. Nút sang mùa mới ngay.</li>
 *   <li><b>Quà theo cấp</b> — chọn cấp và hàng (Miễn phí / Cao cấp), thêm vật
 *       phẩm bằng hộp tra cứu.</li>
 *   <li><b>Người chơi</b> — điểm mùa này, ai đã mở cao cấp; mở cao cấp hoặc
 *       cộng điểm cho một nhân vật (hỗ trợ nạp lỗi).</li>
 * </ul>
 */
public class NroPassTab extends JPanel {

    private final DefaultTableModel mCauHinh = new DefaultTableModel(
            new String[]{"Khoá", "Giá trị", "Ý nghĩa"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 1;
        }
    };
    private final JTable bangCauHinh = new JTable(mCauHinh);

    private final DefaultTableModel mQua = new DefaultTableModel(
            new String[]{"Mã", "Id vật phẩm", "Tên", "Số lượng", "Chỉ số (50=5,77=10)"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 3 || c == 4;
        }
    };
    private final JTable bangQua = new JTable(mQua);
    private final JSpinner chonCap = new JSpinner(new SpinnerNumberModel(1, 1, 200, 1));
    private final JComboBox<String> chonHang = new JComboBox<>(new String[]{"Miễn phí", "Cao cấp"});
    private final JTextField fSoLuong = new JTextField("1", 5);

    private final DefaultTableModel mNguoi = new DefaultTableModel(
            new String[]{"Id", "Nhân vật", "Điểm", "Cấp", "Cao cấp"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bangNguoi = new JTable(mNguoi);
    private final JTextField fTen = new JTextField(14);
    private final JTextField fDiem = new JTextField("100", 6);

    private final JLabel nhanTrangThai = new JLabel(" ");

    public NroPassTab() {
        setLayout(new BorderLayout(0, 6));
        setBackground(UiTheme.CARD);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel dau = new JLabel("<html><b>NRO Pass</b> — kiểu Royale Pass, trong màn Phúc lợi của game."
                + " Hàng Miễn phí ai cũng nhận, hàng Cao cấp mở khoá theo mùa. Điểm pass nhận khi xong"
                + " nhiệm vụ Bò Mộng. Sửa xong có hiệu lực ngay.</html>");
        dau.setForeground(UiTheme.TEXT_MUTED);
        add(dau, BorderLayout.NORTH);

        JTabbedPane trong = new JTabbedPane();
        trong.addTab("1. Cấu hình", theCauHinh());
        trong.addTab("2. Quà theo cấp", theQua());
        trong.addTab("3. Người chơi", theNguoi());
        add(trong, BorderLayout.CENTER);

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
        napCauHinh();
        napQua();
        napNguoi();
    }

    // =====================================================================
    //  Cấu hình
    // =====================================================================
    private JPanel theCauHinh() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);
        bangCauHinh.setRowHeight(24);
        bangCauHinh.getColumnModel().getColumn(0).setPreferredWidth(140);
        bangCauHinh.getColumnModel().getColumn(1).setPreferredWidth(120);
        bangCauHinh.getColumnModel().getColumn(2).setPreferredWidth(420);
        p.add(ServerGuiUtils.cuon(bangCauHinh), BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nut.setBackground(UiTheme.CARD);
        JButton luu = new JButton("Lưu cấu hình");
        ServerGuiUtils.toNut(luu, UiTheme.ACCENT);
        luu.addActionListener(e -> luuCauHinh());
        nut.add(luu);
        JButton muaMoi = new JButton("Bắt đầu mùa mới ngay");
        ServerGuiUtils.toNut(muaMoi, UiTheme.DANGER);
        muaMoi.addActionListener(e -> batDauMuaMoi());
        nut.add(muaMoi);
        p.add(nut, BorderLayout.SOUTH);
        return p;
    }

    private void napCauHinh() {
        if (bangCauHinh.isEditing()) {
            bangCauHinh.getCellEditor().stopCellEditing();
        }
        mCauHinh.setRowCount(0);
        NroPassDAO.mua();
        for (String[] d : NroPassDAO.CAU_HINH_GOC) {
            mCauHinh.addRow(new Object[]{d[0], NroPassDAO.chu(d[0]), d[2]});
        }
    }

    private void luuCauHinh() {
        if (bangCauHinh.isEditing()) {
            bangCauHinh.getCellEditor().stopCellEditing();
        }
        for (int r = 0; r < mCauHinh.getRowCount(); r++) {
            NroPassDAO.datCauHinh(String.valueOf(mCauHinh.getValueAt(r, 0)),
                    String.valueOf(mCauHinh.getValueAt(r, 1)).trim());
        }
        napCauHinh();
        bao("Đã lưu cấu hình NRO Pass.");
    }

    private void batDauMuaMoi() {
        int chac = JOptionPane.showConfirmDialog(this,
                "Kết thúc mùa " + NroPassDAO.mua() + " và bắt đầu mùa mới ngay bây giờ?\n"
                + "Điểm và ô đã nhận của mùa cũ giữ lại làm lịch sử, mọi người bắt đầu lại từ cấp 0.",
                "Mùa mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chac != JOptionPane.OK_OPTION) {
            return;
        }
        NroPassDAO.batDauMuaMoi();
        napLai();
        bao("Đã sang mùa " + NroPassDAO.mua() + ".");
    }

    // =====================================================================
    //  Quà
    // =====================================================================
    private JPanel theQua() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);

        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        tren.setBackground(UiTheme.CARD);
        tren.add(new JLabel("Cấp:"));
        tren.add(chonCap);
        tren.add(chonHang);
        chonCap.addChangeListener(e -> napQua());
        chonHang.addActionListener(e -> napQua());
        p.add(tren, BorderLayout.NORTH);

        bangQua.setRowHeight(24);
        bangQua.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(ServerGuiUtils.cuon(bangQua), BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nut.setBackground(UiTheme.CARD);
        nut.add(new JLabel("Số lượng:"));
        nut.add(fSoLuong);
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
        JLabel ghi = new JLabel("Mỗi ô hiện tối đa 4 món trong game.");
        ghi.setForeground(UiTheme.TEXT_MUTED);
        nut.add(ghi);
        p.add(nut, BorderLayout.SOUTH);
        return p;
    }

    private int cap() {
        return ((Number) chonCap.getValue()).intValue();
    }

    private int hang() {
        return chonHang.getSelectedIndex() == 1 ? NroPassDAO.HANG_CAO_CAP : NroPassDAO.HANG_MIEN_PHI;
    }

    private void napQua() {
        if (bangQua.isEditing()) {
            bangQua.getCellEditor().stopCellEditing();
        }
        mQua.setRowCount(0);
        for (NroPassDAO.Qua q : NroPassDAO.dsQua(cap(), hang())) {
            ItemTemplate t = nro.service.item.ItemService.gI().getTemplate(q.itemId);
            mQua.addRow(new Object[]{q.id, q.itemId, t == null ? "?" : t.name, q.soLuong, q.chiSo});
        }
    }

    private void themQua() {
        int sl = soNguyen(fSoLuong.getText(), 0);
        if (sl <= 0) {
            JOptionPane.showMessageDialog(this, "Gõ số lượng (lớn hơn 0) trước, rồi mới chọn vật phẩm.");
            return;
        }
        List<Integer> chon = OptionPicker.chonNhieuVatPham(this, -1);
        for (int id : chon) {
            NroPassDAO.themQua(cap(), hang(), id, sl, "");
        }
        napQua();
        if (!chon.isEmpty()) {
            bao("Đã thêm " + chon.size() + " món vào cấp " + cap() + ".");
        }
    }

    private void luuQua() {
        if (bangQua.isEditing()) {
            bangQua.getCellEditor().stopCellEditing();
        }
        for (int r = 0; r < mQua.getRowCount(); r++) {
            NroPassDAO.suaQua(soNguyen(mQua.getValueAt(r, 0), -1),
                    soNguyen(mQua.getValueAt(r, 3), 1), String.valueOf(mQua.getValueAt(r, 4)));
        }
        napQua();
        bao("Đã lưu quà cấp " + cap() + ".");
    }

    private void xoaQua() {
        int r = bangQua.getSelectedRow();
        if (r < 0) {
            return;
        }
        NroPassDAO.xoaQua(soNguyen(mQua.getValueAt(bangQua.convertRowIndexToModel(r), 0), -1));
        napQua();
    }

    // =====================================================================
    //  Người chơi
    // =====================================================================
    private JPanel theNguoi() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UiTheme.CARD);
        bangNguoi.setRowHeight(22);
        p.add(ServerGuiUtils.cuon(bangNguoi), BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nut.setBackground(UiTheme.CARD);
        nut.add(new JLabel("Nhân vật:"));
        nut.add(fTen);
        JButton caoCap = new JButton("Mở cao cấp mùa này");
        ServerGuiUtils.toNut(caoCap, UiTheme.OK);
        caoCap.addActionListener(e -> moCaoCap());
        nut.add(caoCap);
        nut.add(fDiem);
        JButton diem = new JButton("Cộng điểm");
        ServerGuiUtils.toNut(diem, UiTheme.ACCENT);
        diem.addActionListener(e -> congDiem());
        nut.add(diem);
        JButton tai = new JButton("Tải lại");
        tai.addActionListener(e -> napNguoi());
        nut.add(tai);
        p.add(nut, BorderLayout.SOUTH);
        return p;
    }

    private void napNguoi() {
        mNguoi.setRowCount(0);
        long moiCap = NroPassDAO.diemMoiCap();
        int soCap = NroPassDAO.soCap();
        for (NroPassDAO.Dong d : NroPassDAO.dsNguoi(NroPassDAO.mua(), 500)) {
            mNguoi.addRow(new Object[]{d.playerId, d.ten, d.diem,
                Math.min(soCap, d.diem / moiCap), d.caoCap ? "Có" : ""});
        }
    }

    private long idNhanVat() {
        String ten = fTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gõ tên nhân vật.");
            return -1;
        }
        long id = NroPassDAO.idTheoTen(ten);
        if (id < 0) {
            JOptionPane.showMessageDialog(this, "Không có nhân vật tên \"" + ten + "\".");
        }
        return id;
    }

    private void moCaoCap() {
        long id = idNhanVat();
        if (id < 0) {
            return;
        }
        int chac = JOptionPane.showConfirmDialog(this, "Mở " + NroPassDAO.ten() + " cao cấp mùa "
                + NroPassDAO.mua() + " cho " + fTen.getText().trim() + "?", "Mở cao cấp",
                JOptionPane.OK_CANCEL_OPTION);
        if (chac != JOptionPane.OK_OPTION) {
            return;
        }
        bao(nro.service.NroPassService.gI().tangCaoCap(id));
        napNguoi();
    }

    private void congDiem() {
        long id = idNhanVat();
        int diem = soNguyen(fDiem.getText(), 0);
        if (id < 0 || diem <= 0) {
            return;
        }
        bao(nro.service.NroPassService.gI().tangDiem(id, diem));
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
}
