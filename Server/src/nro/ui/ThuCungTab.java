package nro.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import nro.entity.template.ItemTemplate;
import nro.repository.dao.ThuCungDAO;
import nro.server.Manager;

/**
 * Tab <b>Thú cưng</b> của bảng điều khiển: chiêu của từng con, đồ ăn, và mấy
 * con số về cấp.
 *
 * <h2>Ba thẻ con</h2>
 *
 * <ul>
 *   <li><b>Chiêu của thú</b> — trái là danh sách thú (mọi vật phẩm kiểu 21),
 *       phải là ba ô chiêu của con đang chọn. Mỗi con nhiều nhất
 *       {@link ThuCungDAO#SO_KY_NANG} chiêu, thêm bớt ngay tại đây.</li>
 *   <li><b>Đồ ăn</b> — món nào cho thú ăn được và mỗi cái bao nhiêu kinh
 *       nghiệm.</li>
 *   <li><b>Cấu hình</b> — kinh nghiệm mỗi cấp, cấp trần, mức mạnh thêm mỗi
 *       cấp.</li>
 * </ul>
 *
 * <p>Không có con số nào viết cứng trong mã game: mọi thứ người chơi gặp đều
 * lấy từ ba bảng này, sửa xong là chốc lát sau máy chủ theo số mới (xem bộ nhớ
 * tạm trong {@link ThuCungDAO}).</p>
 */
public class ThuCungTab extends JPanel {

    public ThuCungTab() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.CARD);
        JTabbedPane trong = new JTabbedPane();
        trong.addTab("1. Chiêu của thú", theChieu());
        trong.addTab("2. Đồ ăn", theDoAn());
        trong.addTab("3. Rương", theRuong());
        trong.addTab("4. Chỉ số theo bậc", theChiSoBac());
        trong.addTab("5. Cấu hình", theCauHinh());
        add(trong, BorderLayout.CENTER);
        // KHONG nap bang ngay trong ham dung.
        //
        // Cua so panel dung xong TRUOC khi may chu nap bang vat pham, nen tra
        // ten mon o day la doc mot danh sach con rong — truoc day cho nay lam
        // ca may chu chet ngay o main. Nap khi tab that su duoc mo ra.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                napLai();
            }
        });
    }

    /** Đã nạp bảng lần nào chưa. */
    private boolean daNap;

    /** Đọc lại cả ba bảng; gọi khi mở tab và khi bấm nút Tải lại. */
    private void napLai() {
        if (Manager.ITEM_TEMPLATES.isEmpty()) {
            // Chua nap xong bang vat pham: de nguyen, lan ve sau thu lai.
            return;
        }
        daNap = true;
        napThu();
        napDoAn();
        napRuong();
        napChiSoBac();
    }

    /**
     * Nạp muộn ở lần vẽ đầu tiên.
     *
     * <p>Chỗ dựa chính là {@code componentShown}, nhưng tab lồng trong tab thì
     * sự kiện ấy không phải lúc nào cũng tới. Vẽ thì chắc chắn có, nên kiểm
     * thêm ở đây — một lần, không tốn gì.</p>
     */
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (!daNap) {
            napLai();
        }
    }

    // =====================================================================
    //  Thẻ 1: chiêu của từng con
    // =====================================================================
    private static final String[] COT_THU = {"Id", "Tên thú", "Bậc", "Số chiêu"};

    /**
     * Cột Bậc sửa được ngay trong bảng; các cột khác chỉ để đọc.
     *
     * <p>Chọn bậc xong là lưu luôn (xem {@code napThu}), không có nút Lưu riêng:
     * một ô chọn bảy giá trị thì thêm một bước bấm nữa chỉ tổ quên.</p>
     */
    private final DefaultTableModel mThu = new DefaultTableModel(COT_THU, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 2;
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            super.setValueAt(v, r, c);
            if (c != 2) {
                return;
            }
            int id = ((Number) getValueAt(r, 0)).intValue();
            for (int i = 0; i < ThuCungDAO.TEN_BAC.length; i++) {
                if (ThuCungDAO.TEN_BAC[i].equals(String.valueOf(v))) {
                    ThuCungDAO.datBac(id, i);
                    return;
                }
            }
        }
    };

    /** Lọc danh sách theo bậc; mục đầu là "tất cả". */
    private final JComboBox<String> locBac = new JComboBox<>(tenBacVaTatCa());

    private static String[] tenBacVaTatCa() {
        String[] ra = new String[ThuCungDAO.TEN_BAC.length + 1];
        ra[0] = "Tất cả bậc";
        System.arraycopy(ThuCungDAO.TEN_BAC, 0, ra, 1, ThuCungDAO.TEN_BAC.length);
        return ra;
    }

    private final JTable bangThu = new JTable(mThu);
    private final JTextField oTim = new JTextField(14);
    private final JLabel lblThu = new JLabel("Chưa chọn con nào");

    /** Ba ô chiêu của con đang chọn. */
    private final ODung[] oChieu = new ODung[ThuCungDAO.SO_KY_NANG];

    private JPanel theChieu() {
        JPanel trai = new JPanel(new BorderLayout(0, 6));
        trai.setBackground(UiTheme.CARD);
        JPanel hangTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        hangTim.setBackground(UiTheme.CARD);
        hangTim.add(new JLabel("Tìm theo tên / id:"));
        hangTim.add(oTim);
        JButton nutTim = new JButton("Lọc");
        ServerGuiUtils.toNut(nutTim, UiTheme.ACCENT);
        nutTim.addActionListener(e -> napThu());
        hangTim.add(nutTim);
        hangTim.add(new JLabel("Bậc:"));
        locBac.addActionListener(e -> napThu());
        hangTim.add(locBac);
        trai.add(hangTim, BorderLayout.NORTH);

        bangThu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // O chon bay bac ngay trong cot, khong phai mo hop thoai rieng.
        bangThu.getColumnModel().getColumn(2).setCellEditor(
                new javax.swing.DefaultCellEditor(new JComboBox<>(ThuCungDAO.TEN_BAC)));
        bangThu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                napChieuCuaThuDangChon();
            }
        });
        trai.add(ServerGuiUtils.cuon(bangThu), BorderLayout.CENTER);

        JPanel phai = new JPanel(new BorderLayout(0, 6));
        phai.setBackground(UiTheme.CARD);
        lblThu.setFont(UiTheme.ui(java.awt.Font.BOLD, 14));
        lblThu.setForeground(UiTheme.ACCENT_TEXT);
        lblThu.setBorder(BorderFactory.createEmptyBorder(6, 8, 2, 8));
        phai.add(lblThu, BorderLayout.NORTH);

        JPanel cot = new JPanel();
        cot.setLayout(new javax.swing.BoxLayout(cot, javax.swing.BoxLayout.Y_AXIS));
        cot.setBackground(UiTheme.CARD);
        for (int i = 0; i < oChieu.length; i++) {
            oChieu[i] = new ODung(i + 1);
            cot.add(oChieu[i]);
        }
        phai.add(ServerGuiUtils.cuon(cot), BorderLayout.CENTER);

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trai, phai);
        chia.setResizeWeight(0.34);
        chia.setBorder(null);
        JPanel bao = new JPanel(new BorderLayout());
        bao.setBackground(UiTheme.CARD);
        bao.add(chia, BorderLayout.CENTER);
        return bao;
    }

    /** Mọi vật phẩm kiểu thú cưng, lọc theo ô tìm. */
    private void napThu() {
        String tim = oTim.getText() == null ? "" : oTim.getText().trim().toLowerCase();
        mThu.setRowCount(0);
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t == null || t.type != ThuCungDAO.KIEU_THU_CUNG) {
                continue;
            }
            if (!tim.isEmpty()
                    && !String.valueOf(t.id).equals(tim)
                    && (t.name == null || !t.name.toLowerCase().contains(tim))) {
                continue;
            }
            if (locBac.getSelectedIndex() > 0
                    && ThuCungDAO.bac(t.id) != locBac.getSelectedIndex() - 1) {
                continue;
            }
            mThu.addRow(new Object[]{(int) t.id, t.name,
                ThuCungDAO.tenBac(ThuCungDAO.bac(t.id)),
                ThuCungDAO.kyNangCua(t.id).size()});
        }
    }

    private int idThuDangChon() {
        int r = bangThu.getSelectedRow();
        if (r < 0) {
            return -1;
        }
        return ((Number) mThu.getValueAt(bangThu.convertRowIndexToModel(r), 0)).intValue();
    }

    private void napChieuCuaThuDangChon() {
        int id = idThuDangChon();
        if (id < 0) {
            lblThu.setText("Chưa chọn con nào");
            for (ODung o : oChieu) {
                o.dat(-1, null);
            }
            return;
        }
        ItemTemplate t = nro.service.item.ItemService.gI().getTemplate((short) id);
        lblThu.setText(id + " — " + (t == null ? "?" : t.name));
        List<ThuCungDAO.KyNang> ds = ThuCungDAO.kyNangCua(id);
        for (int i = 0; i < oChieu.length; i++) {
            ThuCungDAO.KyNang k = null;
            for (ThuCungDAO.KyNang x : ds) {
                if (x.thuTu == i + 1) {
                    k = x;
                    break;
                }
            }
            oChieu[i].dat(id, k);
        }
    }

    /**
     * Một ô chiêu: đủ mọi trường của một dòng {@code thu_cung_ky_nang}.
     *
     * <p>Chiêu chưa có thì các ô vẫn hiện với giá trị mặc định và nút ghi thành
     * "Thêm chiêu" — không phải bấm nút riêng để tạo dòng rồi mới sửa.</p>
     */
    private final class ODung extends JPanel {

        private final int thuTu;
        private int itemId = -1;
        private boolean daCo;

        private final JTextField fTen = new JTextField(18);
        private final JTextField fMoTa = new JTextField(30);
        private final JComboBox<String> fLoai = new JComboBox<>(
                new DefaultComboBoxModel<>(ThuCungDAO.TEN_LOAI));
        private final JTextField fThamSo = new JTextField(5);
        private final JTextField fPhanTram = new JTextField(5);
        private final JTextField fGiay = new JTextField(5);
        private final JTextField fTiLe = new JTextField(5);
        private final JTextField fHoiChieu = new JTextField(5);
        private final JTextField fCapMo = new JTextField(5);
        private final JCheckBox fBat = new JCheckBox("Bật", true);
        private final JButton nutLuu = new JButton("Lưu");
        private final JButton nutXoa = new JButton("Xoá chiêu");
        private final JLabel lblThamSo = new JLabel(" ");

        ODung(int thuTu) {
            this.thuTu = thuTu;
            setLayout(new GridBagLayout());
            setBackground(UiTheme.CARD);
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(UiTheme.LINE, 1, true),
                    "Chiêu " + thuTu));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 6, 3, 6);
            c.anchor = GridBagConstraints.WEST;
            int y = 0;

            them(c, 0, y, new JLabel("Tên chiêu:"));
            c.gridwidth = 3;
            them(c, 1, y++, fTen);
            c.gridwidth = 1;

            them(c, 0, y, new JLabel("Mô tả:"));
            c.gridwidth = 3;
            them(c, 1, y++, fMoTa);
            c.gridwidth = 1;

            them(c, 0, y, new JLabel("Hiệu ứng:"));
            c.gridwidth = 3;
            them(c, 1, y++, fLoai);
            c.gridwidth = 1;

            them(c, 0, y, new JLabel("Tham số:"));
            them(c, 1, y, fThamSo);
            c.gridwidth = 2;
            them(c, 2, y++, lblThamSo);
            c.gridwidth = 1;

            them(c, 0, y, new JLabel("Mức (%):"));
            them(c, 1, y, fPhanTram);
            them(c, 2, y, new JLabel("Kéo dài (giây):"));
            them(c, 3, y++, fGiay);

            them(c, 0, y, new JLabel("Tỉ lệ nổ (%):"));
            them(c, 1, y, fTiLe);
            them(c, 2, y, new JLabel("Hồi chiêu (giây):"));
            them(c, 3, y++, fHoiChieu);

            them(c, 0, y, new JLabel("Mở ở cấp:"));
            them(c, 1, y, fCapMo);
            fBat.setBackground(UiTheme.CARD);
            fBat.setForeground(UiTheme.TEXT);
            them(c, 2, y++, fBat);

            JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            nut.setBackground(UiTheme.CARD);
            ServerGuiUtils.toNut(nutLuu, UiTheme.OK);
            ServerGuiUtils.toNut(nutXoa, UiTheme.DANGER);
            nutLuu.addActionListener(e -> luu());
            nutXoa.addActionListener(e -> xoa());
            nut.add(nutLuu);
            nut.add(nutXoa);
            c.gridwidth = 4;
            them(c, 0, y, nut);
            c.gridwidth = 1;

            fLoai.addActionListener(e -> capNhatThamSo());
            dat(-1, null);
        }

        private void them(GridBagConstraints c, int x, int y, java.awt.Component o) {
            c.gridx = x;
            c.gridy = y;
            add(o, c);
        }

        /** Ô tham số chỉ có nghĩa với vài loại; loại khác thì nói rõ là bỏ trống. */
        private void capNhatThamSo() {
            int loai = fLoai.getSelectedIndex();
            String y = (loai >= 0 && loai < ThuCungDAO.Y_NGHIA_THAM_SO.length)
                    ? ThuCungDAO.Y_NGHIA_THAM_SO[loai] : "";
            boolean dung = y != null && !y.isEmpty();
            fThamSo.setEnabled(dung);
            lblThamSo.setText(dung ? y : "(loại này không dùng tham số)");
            lblThamSo.setForeground(dung ? UiTheme.TEXT : UiTheme.TEXT_MUTED);
        }

        void dat(int itemId, ThuCungDAO.KyNang k) {
            this.itemId = itemId;
            this.daCo = k != null;
            boolean coThu = itemId > 0;
            fTen.setText(k == null ? "" : k.ten);
            fMoTa.setText(k == null ? "" : k.moTa);
            fLoai.setSelectedIndex(k == null ? 0
                    : Math.max(0, Math.min(ThuCungDAO.TEN_LOAI.length - 1, k.loai)));
            fThamSo.setText(String.valueOf(k == null ? 0 : k.thamSo));
            fPhanTram.setText(String.valueOf(k == null ? 10 : k.phanTram));
            fGiay.setText(String.valueOf(k == null ? 10 : k.giay));
            fTiLe.setText(String.valueOf(k == null ? 10 : k.tiLe));
            fHoiChieu.setText(String.valueOf(k == null ? 30 : k.hoiChieu));
            fCapMo.setText(String.valueOf(k == null ? (thuTu == 1 ? 1 : thuTu * 10) : k.capMo));
            fBat.setSelected(k == null || k.bat);
            nutLuu.setText(daCo ? "Lưu" : "Thêm chiêu");
            nutXoa.setEnabled(daCo && coThu);
            for (java.awt.Component o : getComponents()) {
                o.setEnabled(coThu);
            }
            setEnabled(coThu);
            if (coThu) {
                nutXoa.setEnabled(daCo);
            }
            capNhatThamSo();
        }

        private void luu() {
            if (itemId <= 0) {
                return;
            }
            ThuCungDAO.KyNang k = new ThuCungDAO.KyNang();
            k.itemId = itemId;
            k.thuTu = thuTu;
            k.ten = fTen.getText().trim();
            k.moTa = fMoTa.getText().trim();
            k.loai = Math.max(0, fLoai.getSelectedIndex());
            k.thamSo = so(fThamSo, 0);
            k.phanTram = so(fPhanTram, 10);
            k.giay = so(fGiay, 10);
            k.tiLe = so(fTiLe, 10);
            k.hoiChieu = so(fHoiChieu, 30);
            k.capMo = Math.max(1, so(fCapMo, 1));
            k.bat = fBat.isSelected();
            if (k.ten.isEmpty()) {
                JOptionPane.showMessageDialog(ThuCungTab.this, "Chiêu phải có tên.");
                return;
            }
            ThuCungDAO.luuKyNang(k);
            napThu();
            napChieuCuaThuDangChon();
        }

        private void xoa() {
            if (itemId <= 0 || !daCo) {
                return;
            }
            int chon = JOptionPane.showConfirmDialog(ThuCungTab.this,
                    "Xoá chiêu " + thuTu + " của con này?", "Xoá chiêu",
                    JOptionPane.YES_NO_OPTION);
            if (chon != JOptionPane.YES_OPTION) {
                return;
            }
            ThuCungDAO.xoaKyNang(itemId, thuTu);
            napThu();
            napChieuCuaThuDangChon();
        }
    }

    private static int so(JTextField f, int duPhong) {
        try {
            return Integer.parseInt(f.getText().trim());
        } catch (NumberFormatException sai) {
            return duPhong;
        }
    }

    // =====================================================================
    //  Thẻ 2: đồ ăn
    // =====================================================================
    private static final String[] COT_AN = {"Id", "Tên món", "Kinh nghiệm", "Bật"};

    private final DefaultTableModel mAn = new DefaultTableModel(COT_AN, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 2 || c == 3;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 3 ? Boolean.class : (c == 2 ? Integer.class : Object.class);
        }
    };

    private final JTable bangAn = new JTable(mAn);
    private final JTextField fIdMoi = new JTextField(6);
    private final JTextField fExpMoi = new JTextField(6);

    private JPanel theDoAn() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(UiTheme.CARD);

        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tren.setBackground(UiTheme.CARD);
        tren.add(new JLabel("Thêm món: id"));
        tren.add(fIdMoi);
        tren.add(new JLabel("kinh nghiệm"));
        tren.add(fExpMoi);
        JButton nutThem = new JButton("Thêm");
        ServerGuiUtils.toNut(nutThem, UiTheme.OK);
        nutThem.addActionListener(e -> themDoAn());
        tren.add(nutThem);
        JButton nutXoa = new JButton("Xoá dòng đang chọn");
        ServerGuiUtils.toNut(nutXoa, UiTheme.DANGER);
        nutXoa.addActionListener(e -> xoaDoAn());
        tren.add(nutXoa);
        JButton nutLuu = new JButton("Lưu bảng");
        ServerGuiUtils.toNut(nutLuu, UiTheme.ACCENT);
        nutLuu.addActionListener(e -> luuDoAn());
        tren.add(nutLuu);
        p.add(tren, BorderLayout.NORTH);

        bangAn.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(ServerGuiUtils.cuon(bangAn), BorderLayout.CENTER);

        JLabel ghi = new JLabel("Sửa thẳng cột Kinh nghiệm / Bật rồi bấm Lưu bảng."
                + " Món không nằm trong bảng thì thú không ăn được.");
        ghi.setForeground(UiTheme.TEXT_MUTED);
        ghi.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
        p.add(ghi, BorderLayout.SOUTH);
        return p;
    }

    private void napDoAn() {
        mAn.setRowCount(0);
        for (ThuCungDAO.DoAn d : ThuCungDAO.tatCaDoAn()) {
            ItemTemplate t = nro.service.item.ItemService.gI().getTemplate((short) d.itemId);
            mAn.addRow(new Object[]{d.itemId, t == null ? "?" : t.name, d.exp, d.bat});
        }
    }

    private void themDoAn() {
        int id = so(fIdMoi, -1);
        int exp = so(fExpMoi, 0);
        if (id < 0 || exp <= 0) {
            JOptionPane.showMessageDialog(this, "Cần id món và số kinh nghiệm lớn hơn 0.");
            return;
        }
        if (nro.service.item.ItemService.gI().getTemplate((short) id) == null) {
            JOptionPane.showMessageDialog(this, "Không có vật phẩm id " + id);
            return;
        }
        ThuCungDAO.luuDoAn(id, exp, true);
        fIdMoi.setText("");
        fExpMoi.setText("");
        napDoAn();
    }

    private void xoaDoAn() {
        int r = bangAn.getSelectedRow();
        if (r < 0) {
            return;
        }
        int id = ((Number) mAn.getValueAt(bangAn.convertRowIndexToModel(r), 0)).intValue();
        ThuCungDAO.xoaDoAn(id);
        napDoAn();
    }

    private void luuDoAn() {
        if (bangAn.isEditing()) {
            bangAn.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < mAn.getRowCount(); i++) {
            int id = ((Number) mAn.getValueAt(i, 0)).intValue();
            int exp = ((Number) mAn.getValueAt(i, 2)).intValue();
            boolean bat = Boolean.TRUE.equals(mAn.getValueAt(i, 3));
            ThuCungDAO.luuDoAn(id, exp, bat);
        }
        napDoAn();
        JOptionPane.showMessageDialog(this, "Đã lưu bảng đồ ăn.");
    }

    // =====================================================================
    //  Thẻ 3: rương thú cưng
    // =====================================================================
    private static final String[] COT_RUONG = {"Id", "Tên rương", "Số bậc"};

    private final DefaultTableModel mRuong = new DefaultTableModel(COT_RUONG, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private static final String[] COT_TI_LE = {"Bậc", "Tỉ lệ", "Ra khoảng", "Số thú bậc này"};

    /**
     * Bậc mà rương đang chọn có thể ra.
     *
     * <p>Cột <b>Tỉ lệ</b> là <i>phần</i>, không bắt buộc cộng tròn 100: máy chủ
     * cộng tổng rồi bốc theo phần, nên 70/25/5 và 14/5/1 cho cùng kết quả. Cột
     * "Ra khoảng" quy ra phần trăm thật để khỏi phải nhẩm.</p>
     */
    private final DefaultTableModel mTiLe = new DefaultTableModel(COT_TI_LE, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 1;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 1 ? Integer.class : Object.class;
        }
    };

    private final JTable bangRuong = new JTable(mRuong);
    private final JTable bangTiLe = new JTable(mTiLe);
    private final JTextField fIdRuongMoi = new JTextField(6);
    private final JComboBox<String> fBacThem = new JComboBox<>(ThuCungDAO.TEN_BAC);
    private final JLabel lblRuong = new JLabel("Chưa chọn rương nào");

    private JPanel theRuong() {
        // ----- trai: danh sach ruong -----
        JPanel trai = new JPanel(new BorderLayout(0, 6));
        trai.setBackground(UiTheme.CARD);
        JPanel hangTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        hangTrai.setBackground(UiTheme.CARD);
        hangTrai.add(new JLabel("Thêm rương, id:"));
        hangTrai.add(fIdRuongMoi);
        JButton themRuong = new JButton("Thêm rương");
        ServerGuiUtils.toNut(themRuong, UiTheme.OK);
        themRuong.addActionListener(e -> themRuongMoi());
        hangTrai.add(themRuong);
        trai.add(hangTrai, BorderLayout.NORTH);

        bangRuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangRuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                napTiLeCuaRuongDangChon();
            }
        });
        trai.add(ServerGuiUtils.cuon(bangRuong), BorderLayout.CENTER);

        // ----- phai: cac bac cua ruong dang chon -----
        JPanel phai = new JPanel(new BorderLayout(0, 6));
        phai.setBackground(UiTheme.CARD);
        lblRuong.setFont(UiTheme.ui(java.awt.Font.BOLD, 14));
        lblRuong.setForeground(UiTheme.ACCENT_TEXT);
        lblRuong.setBorder(BorderFactory.createEmptyBorder(6, 8, 2, 8));
        phai.add(lblRuong, BorderLayout.NORTH);
        phai.add(ServerGuiUtils.cuon(bangTiLe), BorderLayout.CENTER);

        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        duoi.setBackground(UiTheme.CARD);
        duoi.add(new JLabel("Bậc:"));
        duoi.add(fBacThem);
        JButton themBac = new JButton("Thêm bậc");
        ServerGuiUtils.toNut(themBac, UiTheme.OK);
        themBac.addActionListener(e -> themBacChoRuong());
        duoi.add(themBac);
        JButton xoaBac = new JButton("Xoá bậc đang chọn");
        ServerGuiUtils.toNut(xoaBac, UiTheme.DANGER);
        xoaBac.addActionListener(e -> xoaBacCuaRuong());
        duoi.add(xoaBac);
        JButton luu = new JButton("Lưu tỉ lệ");
        ServerGuiUtils.toNut(luu, UiTheme.ACCENT);
        luu.addActionListener(e -> luuTiLe());
        duoi.add(luu);
        phai.add(duoi, BorderLayout.SOUTH);

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trai, phai);
        chia.setResizeWeight(0.42);
        chia.setBorder(null);
        JPanel bao = new JPanel(new BorderLayout());
        bao.setBackground(UiTheme.CARD);
        bao.add(chia, BorderLayout.CENTER);
        return bao;
    }

    private void napRuong() {
        int giu = idRuongDangChon();
        mRuong.setRowCount(0);
        for (int id : ThuCungDAO.dsRuong()) {
            ItemTemplate t = nro.service.item.ItemService.gI().getTemplate((short) id);
            mRuong.addRow(new Object[]{id, t == null ? "?" : t.name,
                ThuCungDAO.tiLeRuong(id).size()});
        }
        // Giu nguyen dong dang chon sau khi nap lai, khong nhay ve dau bang.
        for (int i = 0; i < mRuong.getRowCount(); i++) {
            if (((Number) mRuong.getValueAt(i, 0)).intValue() == giu) {
                bangRuong.setRowSelectionInterval(i, i);
                return;
            }
        }
        // Chua chon gi thi chon san ruong dau: mo the ra la thay ngay ti le,
        // khong phai bam them mot cai vao danh sach ben trai.
        if (mRuong.getRowCount() > 0) {
            bangRuong.setRowSelectionInterval(0, 0);
            return;
        }
        napTiLeCuaRuongDangChon();
    }

    private int idRuongDangChon() {
        int r = bangRuong.getSelectedRow();
        if (r < 0) {
            return -1;
        }
        return ((Number) mRuong.getValueAt(bangRuong.convertRowIndexToModel(r), 0)).intValue();
    }

    private void napTiLeCuaRuongDangChon() {
        mTiLe.setRowCount(0);
        int id = idRuongDangChon();
        if (id < 0) {
            lblRuong.setText("Chưa chọn rương nào");
            return;
        }
        ItemTemplate t = nro.service.item.ItemService.gI().getTemplate((short) id);
        lblRuong.setText(id + " — " + (t == null ? "?" : t.name));
        Map<Integer, Integer> tl = ThuCungDAO.tiLeRuong(id);
        int tong = 0;
        for (int v : tl.values()) {
            tong += Math.max(0, v);
        }
        for (Map.Entry<Integer, Integer> e : tl.entrySet()) {
            int phanTram = tong > 0 ? Math.round(e.getValue() * 1000f / tong) : 0;
            mTiLe.addRow(new Object[]{ThuCungDAO.tenBac(e.getKey()), e.getValue(),
                (phanTram / 10f) + "%", demThuBac(e.getKey())});
        }
    }

    /** Có bao nhiêu loại thú đang đứng ở bậc này — bậc rỗng thì rương ra hụt. */
    private int demThuBac(int bac) {
        int n = 0;
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t != null && t.type == ThuCungDAO.KIEU_THU_CUNG && ThuCungDAO.bac(t.id) == bac) {
                n++;
            }
        }
        return n;
    }

    private void themRuongMoi() {
        int id = so(fIdRuongMoi, -1);
        if (id < 0 || nro.service.item.ItemService.gI().getTemplate((short) id) == null) {
            JOptionPane.showMessageDialog(this, "Không có vật phẩm id " + fIdRuongMoi.getText());
            return;
        }
        // Them mot bac mac dinh de ruong hien ra trong danh sach; admin sua sau.
        ThuCungDAO.luuTiLeRuong(id, 0, 100);
        fIdRuongMoi.setText("");
        napRuong();
    }

    private void themBacChoRuong() {
        int id = idRuongDangChon();
        if (id < 0) {
            return;
        }
        ThuCungDAO.luuTiLeRuong(id, fBacThem.getSelectedIndex(), 10);
        napRuong();
        napTiLeCuaRuongDangChon();
    }

    private void xoaBacCuaRuong() {
        int id = idRuongDangChon();
        int r = bangTiLe.getSelectedRow();
        if (id < 0 || r < 0) {
            return;
        }
        String bac = String.valueOf(mTiLe.getValueAt(bangTiLe.convertRowIndexToModel(r), 0));
        for (int i = 0; i < ThuCungDAO.TEN_BAC.length; i++) {
            if (ThuCungDAO.TEN_BAC[i].equals(bac)) {
                ThuCungDAO.xoaTiLeRuong(id, i);
                break;
            }
        }
        napRuong();
        napTiLeCuaRuongDangChon();
    }

    private void luuTiLe() {
        int id = idRuongDangChon();
        if (id < 0) {
            return;
        }
        if (bangTiLe.isEditing()) {
            bangTiLe.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < mTiLe.getRowCount(); i++) {
            String bac = String.valueOf(mTiLe.getValueAt(i, 0));
            int tiLe = ((Number) mTiLe.getValueAt(i, 1)).intValue();
            for (int b = 0; b < ThuCungDAO.TEN_BAC.length; b++) {
                if (ThuCungDAO.TEN_BAC[b].equals(bac)) {
                    ThuCungDAO.luuTiLeRuong(id, b, tiLe);
                    break;
                }
            }
        }
        napTiLeCuaRuongDangChon();
        JOptionPane.showMessageDialog(this, "Đã lưu tỉ lệ rương.");
    }

    //  Thẻ: khoảng chỉ số theo bậc
    // =====================================================================
    private static final String[] COT_CS = {"Bậc", "HP từ", "HP đến", "KI từ", "KI đến",
        "SĐ từ", "SĐ đến", "Giáp từ", "Giáp đến", "Chí mạng từ", "Chí mạng đến"};

    /**
     * Khoảng chỉ số của từng bậc.
     *
     * <p>Nhận một con thú thì mỗi chỉ số bốc ngẫu nhiên trong khoảng của bậc
     * ấy, nên hai con cùng loại vẫn khác nhau. Cột "Bậc" chỉ để đọc; mười cột
     * còn lại sửa thẳng rồi bấm Lưu.</p>
     */
    private final DefaultTableModel mCs = new DefaultTableModel(COT_CS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c > 0;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 0 ? Object.class : Integer.class;
        }
    };

    private final JTable bangCs = new JTable(mCs);

    private JPanel theChiSoBac() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(UiTheme.CARD);

        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tren.setBackground(UiTheme.CARD);
        JButton luu = new JButton("Lưu bảng");
        ServerGuiUtils.toNut(luu, UiTheme.ACCENT);
        luu.addActionListener(e -> luuChiSoBac());
        tren.add(luu);
        JLabel ghi = new JLabel("Chỉ số cộng thẳng cho người chơi khi con thú ra trận."
                + " Cấp càng cao càng tăng thêm theo 'chi_so_moi_cap' ở thẻ Cấu hình.");
        ghi.setForeground(UiTheme.TEXT_MUTED);
        tren.add(ghi);
        p.add(tren, BorderLayout.NORTH);

        bangCs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(ServerGuiUtils.cuon(bangCs), BorderLayout.CENTER);
        return p;
    }

    private void napChiSoBac() {
        mCs.setRowCount(0);
        for (ThuCungDAO.ChiSoBac c : ThuCungDAO.tatCaChiSoBac()) {
            mCs.addRow(new Object[]{ThuCungDAO.tenBac(c.bac), c.hpMin, c.hpMax,
                c.kiMin, c.kiMax, c.sdMin, c.sdMax, c.giapMin, c.giapMax,
                c.cmMin, c.cmMax});
        }
    }

    private void luuChiSoBac() {
        if (bangCs.isEditing()) {
            bangCs.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < mCs.getRowCount(); i++) {
            ThuCungDAO.ChiSoBac c = new ThuCungDAO.ChiSoBac();
            c.bac = i;
            c.hpMin = so(mCs.getValueAt(i, 1));
            c.hpMax = so(mCs.getValueAt(i, 2));
            c.kiMin = so(mCs.getValueAt(i, 3));
            c.kiMax = so(mCs.getValueAt(i, 4));
            c.sdMin = so(mCs.getValueAt(i, 5));
            c.sdMax = so(mCs.getValueAt(i, 6));
            c.giapMin = so(mCs.getValueAt(i, 7));
            c.giapMax = so(mCs.getValueAt(i, 8));
            c.cmMin = so(mCs.getValueAt(i, 9));
            c.cmMax = so(mCs.getValueAt(i, 10));
            ThuCungDAO.luuChiSoBac(c);
        }
        napChiSoBac();
        JOptionPane.showMessageDialog(this, "Đã lưu khoảng chỉ số theo bậc.");
    }

    private static int so(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException sai) {
            return 0;
        }
    }

    // =====================================================================
    // =====================================================================
    //  Thẻ 4: cấu hình
    // =====================================================================
    private final List<String[]> khoaCauHinh = new ArrayList<>();
    private final List<JTextField> oCauHinh = new ArrayList<>();

    private JPanel theCauHinh() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UiTheme.CARD);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        Map<String, String[]> ds = ThuCungDAO.cauHinhDayDu();
        for (Map.Entry<String, String[]> e : ds.entrySet()) {
            String khoa = e.getKey();
            // Hai khoa nay la id chi so may chu tu cap, khong phai cho nguoi sua.
            if (khoa.startsWith("chi_so_")) {
                continue;
            }
            JTextField f = new JTextField(e.getValue()[0], 8);
            khoaCauHinh.add(new String[]{khoa});
            oCauHinh.add(f);
            c.gridx = 0;
            c.gridy = y;
            p.add(new JLabel(khoa), c);
            c.gridx = 1;
            p.add(f, c);
            c.gridx = 2;
            JLabel mo = new JLabel(e.getValue()[1] == null ? "" : e.getValue()[1]);
            mo.setForeground(UiTheme.TEXT_MUTED);
            p.add(mo, c);
            y++;
        }
        JButton luu = new JButton("Lưu cấu hình");
        ServerGuiUtils.toNut(luu, UiTheme.ACCENT);
        luu.addActionListener(e -> {
            for (int i = 0; i < khoaCauHinh.size(); i++) {
                ThuCungDAO.datCauHinh(khoaCauHinh.get(i)[0], oCauHinh.get(i).getText().trim());
            }
            JOptionPane.showMessageDialog(this, "Đã lưu cấu hình thú cưng.");
        });
        c.gridx = 1;
        c.gridy = y;
        p.add(luu, c);

        JPanel bao = new JPanel(new BorderLayout());
        bao.setBackground(UiTheme.CARD);
        bao.add(p, BorderLayout.NORTH);
        return bao;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(900, 560);
    }

}
