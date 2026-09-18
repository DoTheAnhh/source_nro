package nro.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import nro.repository.dao.LoaiChiSo;

/**
 * Ô chọn <b>tác dụng</b> (loại chỉ số + chiêu) có nhóm và tìm kiếm tiếng Việt.
 *
 * <h2>Thay cho ô thả xuống vài trăm dòng</h2>
 *
 * <p>Bấm vào ô là mở một hộp: bên trái là nhóm (Chỉ số cơ bản, Chiêu thức,
 * Hiệu ứng phụ…), trên cùng là ô tìm. Gõ không dấu, nhiều từ, không cần đúng
 * thứ tự — "de trung", "thieu dot kame", "chi mang galick" đều ra đúng dòng.</p>
 *
 * <p>Giữ cách dùng của {@code JComboBox}: {@link #getSelectedIndex()} trả 0 khi
 * chưa chọn, {@code i + 1} khi chọn lựa chọn thứ {@code i}. Nhờ vậy đoạn mã
 * đọc kết quả cũ gần như không phải đổi.</p>
 */
public final class OChonTacDung extends JButton {

    /** Một lựa chọn: mã loại + chiêu + nhãn. */
    public static final class LuaChon {
        public final String ma;
        public final int chieu;
        public final String nhan;
        public final String nhom;
        public final String tenChieu;
        public final boolean coThoiGian;

        public LuaChon(String ma, int chieu, String nhan, String nhom, String tenChieu,
                boolean coThoiGian) {
            this.ma = ma;
            this.chieu = chieu;
            this.nhan = nhan;
            this.nhom = nhom;
            this.tenChieu = tenChieu;
            this.coThoiGian = coThoiGian;
        }
    }

    /**
     * Mọi lựa chọn đang dùng, bung loại theo chiêu thành từng chiêu hợp lệ.
     *
     * @param chiCoOptionDo chỉ lấy lựa chọn có sẵn chỉ số trên đồ (dùng cho hộp
     *                      thêm chỉ số vào món đồ)
     */
    public static List<LuaChon> tatCa(boolean chiCoOptionDo) {
        List<LuaChon> ra = new ArrayList<>();
        for (LoaiChiSo.Loai l : LoaiChiSo.dangDung()) {
            if (!l.theoChieu()) {
                if (chiCoOptionDo && nro.repository.dao.TrangBiBonusDAO.optionCua(l.ma, -1) < 0) {
                    continue;
                }
                ra.add(new LuaChon(l.ma, 0, LoaiChiSo.nhan(l, 0), l.nhom, "", l.coThoiGian));
                continue;
            }
            for (int c : l.chieu) {
                if (chiCoOptionDo && nro.repository.dao.TrangBiBonusDAO.optionCua(l.ma, c) < 0) {
                    continue;
                }
                ra.add(new LuaChon(l.ma, c, LoaiChiSo.nhan(l, c), l.nhom,
                        LoaiChiSo.tenChieu(c), l.coThoiGian));
            }
        }
        return ra;
    }

    /**
     * Thêm vào danh sách một lựa chọn cho mã <b>cũ</b> (đã ẩn) nếu dữ liệu đang
     * dùng nó — để mở dòng cũ ra sửa không bị mất.
     *
     * @return chỉ số của lựa chọn khớp trong danh sách
     */
    public static int timHoacThem(List<LuaChon> ds, String ma, int chieu) {
        for (int i = 0; i < ds.size(); i++) {
            LuaChon x = ds.get(i);
            if (x.ma.equals(ma) && (x.chieu == chieu || !theoChieu(ma))) {
                return i;
            }
        }
        LoaiChiSo.Loai l = LoaiChiSo.get(ma);
        String ten = l == null ? ma : l.ten;
        boolean theoChieu = l != null && l.theoChieu();
        ds.add(new LuaChon(ma, chieu, ten + (theoChieu ? " — " + LoaiChiSo.tenChieu(chieu) : "")
                + " (mã cũ)", "(cũ)", theoChieu ? LoaiChiSo.tenChieu(chieu) : "",
                l != null && l.coThoiGian));
        return ds.size() - 1;
    }

    private static boolean theoChieu(String ma) {
        LoaiChiSo.Loai l = LoaiChiSo.get(ma);
        return l != null && l.theoChieu();
    }

    private final List<LuaChon> ds;
    private final String chuTrong;
    private int dangChon;

    /**
     * @param ds       danh sách lựa chọn
     * @param chuTrong chữ hiện khi chưa chọn; {@code null} = bắt buộc chọn (không
     *                 có nút "bỏ chọn")
     */
    public OChonTacDung(List<LuaChon> ds, String chuTrong) {
        this.ds = ds;
        this.chuTrong = chuTrong;
        setHorizontalAlignment(LEFT);
        setPreferredSize(new Dimension(340, 26));
        capNhatChu();
        addActionListener(e -> {
            int i = hop(this, ds, dangChon - 1, chuTrong != null);
            if (i != Integer.MIN_VALUE) {
                setSelectedIndex(i + 1);
            }
        });
    }

    public List<LuaChon> danhSach() {
        return ds;
    }

    /** 0 = chưa chọn, {@code i + 1} = lựa chọn thứ {@code i}. */
    public int getSelectedIndex() {
        return dangChon;
    }

    public void setSelectedIndex(int i) {
        int cu = dangChon;
        dangChon = Math.max(0, Math.min(ds.size(), i));
        capNhatChu();
        if (cu != dangChon) {
            firePropertyChange("dangChon", cu, dangChon);
        }
    }

    /** Lựa chọn đang chọn, hoặc {@code null}. */
    public LuaChon getLuaChon() {
        return dangChon <= 0 ? null : ds.get(dangChon - 1);
    }

    private void capNhatChu() {
        LuaChon x = getLuaChon();
        setText(x == null ? (chuTrong == null ? "— bấm để chọn —" : chuTrong) : x.nhan);
        setToolTipText(x == null ? "Bấm để chọn tác dụng" : x.nhom + " › " + x.nhan);
    }

    /**
     * Hộp chọn có nhóm và tìm kiếm.
     *
     * @return chỉ số đã chọn trong {@code ds}; {@code -1} nếu bấm "Bỏ chọn";
     *         {@link Integer#MIN_VALUE} nếu huỷ
     */
    public static int hop(Component cha, List<LuaChon> ds, int dangChon, boolean choBo) {
        final JTextField q = new JTextField();
        q.setToolTipText("Gõ không dấu, nhiều từ: \"de trung\", \"thieu dot kame\"");
        DefaultListModel<String> mNhom = new DefaultListModel<>();
        mNhom.addElement("Tất cả");
        for (String n : LoaiChiSo.CAC_NHOM) {
            mNhom.addElement(n);
        }
        boolean coCu = false;
        for (LuaChon x : ds) {
            if ("(cũ)".equals(x.nhom)) {
                coCu = true;
            }
        }
        if (coCu) {
            mNhom.addElement("(cũ)");
        }
        final JList<String> lNhom = new JList<>(mNhom);
        lNhom.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lNhom.setSelectedIndex(0);

        final DefaultTableModel m = new DefaultTableModel(
                new Object[]{"#", "Nhóm", "Tác dụng", "Chiêu"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        final JTable t = new JTable(m);
        t.setRowHeight(24);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(1).setPreferredWidth(170);
        t.getColumnModel().getColumn(2).setPreferredWidth(330);
        t.getColumnModel().getColumn(3).setPreferredWidth(150);
        final JLabel dem = new JLabel();
        dem.setForeground(new java.awt.Color(120, 120, 120));

        final Runnable loc = () -> {
            String nhom = lNhom.getSelectedIndex() <= 0 ? null : lNhom.getSelectedValue();
            String go = q.getText();
            m.setRowCount(0);
            int chonLai = -1;
            for (int i = 0; i < ds.size(); i++) {
                LuaChon x = ds.get(i);
                if (nhom != null && !nhom.equals(x.nhom)) {
                    continue;
                }
                if (!LoaiChiSo.khop(x.nhom + " " + x.nhan + " " + x.tenChieu + " " + x.ma, go)) {
                    continue;
                }
                if (i == dangChon) {
                    chonLai = m.getRowCount();
                }
                m.addRow(new Object[]{i, x.nhom, x.nhan, x.tenChieu});
            }
            if (chonLai >= 0) {
                t.setRowSelectionInterval(chonLai, chonLai);
                t.scrollRectToVisible(t.getCellRect(chonLai, 0, true));
            } else if (m.getRowCount() > 0) {
                t.setRowSelectionInterval(0, 0);
            }
            dem.setText("  " + m.getRowCount() + " / " + ds.size());
        };
        q.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loc.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loc.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                loc.run();
            }
        });
        lNhom.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loc.run();
            }
        });
        loc.run();

        final int[] ketQua = {Integer.MIN_VALUE};
        final JDialog[] hop = new JDialog[1];
        final Runnable chon = () -> {
            int r = t.getSelectedRow();
            if (r >= 0) {
                ketQua[0] = (Integer) m.getValueAt(r, 0);
                hop[0].dispose();
            }
        };
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    chon.run();
                }
            }
        });
        // Enter o o tim = chon dong dang sang; mui ten len/xuong di chuyen dong.
        q.addActionListener(e -> chon.run());
        q.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int r = t.getSelectedRow();
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && r + 1 < m.getRowCount()) {
                    t.setRowSelectionInterval(r + 1, r + 1);
                    t.scrollRectToVisible(t.getCellRect(r + 1, 0, true));
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP && r > 0) {
                    t.setRowSelectionInterval(r - 1, r - 1);
                    t.scrollRectToVisible(t.getCellRect(r - 1, 0, true));
                }
            }
        });

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setBorder(new EmptyBorder(8, 8, 4, 8));
        top.add(new JLabel("Tìm (không cần dấu):"), BorderLayout.WEST);
        top.add(q, BorderLayout.CENTER);
        top.add(dem, BorderLayout.EAST);

        JScrollPane spNhom = new JScrollPane(lNhom);
        spNhom.setPreferredSize(new Dimension(190, 300));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        if (choBo) {
            JButton bBo = new JButton("Bỏ chọn");
            bBo.addActionListener(e -> {
                ketQua[0] = -1;
                hop[0].dispose();
            });
            nut.add(bBo);
        }
        JButton bHuy = new JButton("Huỷ");
        bHuy.addActionListener(e -> hop[0].dispose());
        JButton bOk = new JButton("Chọn");
        bOk.addActionListener(e -> chon.run());
        nut.add(bHuy);
        nut.add(bOk);

        JLabel goiY = new JLabel("<html><span style='color:#777'>Nháy đúp hoặc Enter để chọn. "
                + "Nhóm <b>Hiệu ứng phụ</b> có thêm ô <b>thời gian (giây)</b> và <b>tỉ lệ</b> "
                + "ngay trên cùng dòng.</span></html>");
        goiY.setBorder(new EmptyBorder(0, 8, 0, 8));
        JPanel duoi = new JPanel(new BorderLayout());
        duoi.add(goiY, BorderLayout.CENTER);
        duoi.add(nut, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.add(top, BorderLayout.NORTH);
        wrap.add(spNhom, BorderLayout.WEST);
        wrap.add(new JScrollPane(t), BorderLayout.CENTER);
        wrap.add(duoi, BorderLayout.SOUTH);

        java.awt.Window owner = SwingUtilities.getWindowAncestor(cha);
        hop[0] = owner instanceof java.awt.Dialog
                ? new JDialog((java.awt.Dialog) owner, "Chọn tác dụng", true)
                : new JDialog((java.awt.Frame) owner, "Chọn tác dụng", true);
        hop[0].setContentPane(wrap);
        hop[0].setSize(900, 540);
        hop[0].setLocationRelativeTo(cha);
        SwingUtilities.invokeLater(q::requestFocusInWindow);
        hop[0].setVisible(true);
        return ketQua[0];
    }

    /**
     * Trình sửa ô bảng: bấm vào ô là mở hộp chọn, ghi <b>nhãn</b> vào ô.
     *
     * <p>Ghi nhãn chứ không ghi chỉ số, vì chỗ đọc bảng đang so theo nhãn.</p>
     */
    public static TableCellEditor celChon(final List<LuaChon> ds) {
        return new CelChon(ds);
    }

    private static final class CelChon extends AbstractCellEditor implements TableCellEditor {
        private final List<LuaChon> ds;
        private final JButton nut = new JButton();
        private Object giaTri;

        CelChon(List<LuaChon> ds) {
            this.ds = ds;
            nut.setHorizontalAlignment(LEFT);
            nut.addActionListener(e -> moHop());
        }

        private void moHop() {
            int dang = -1;
            for (int i = 0; i < ds.size(); i++) {
                if (ds.get(i).nhan.equals(String.valueOf(giaTri))) {
                    dang = i;
                }
            }
            int i = hop(nut, ds, dang, false);
            if (i >= 0) {
                giaTri = ds.get(i).nhan;
                stopCellEditing();
            } else {
                cancelCellEditing();
            }
        }

        @Override
        public Object getCellEditorValue() {
            return giaTri;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            giaTri = value;
            nut.setText(String.valueOf(value));
            // Mo hop ngay khi bat dau sua, khong bat bam them lan nua.
            SwingUtilities.invokeLater(this::moHop);
            return nut;
        }
    }
}
