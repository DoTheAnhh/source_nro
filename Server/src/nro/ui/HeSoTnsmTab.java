package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import nro.repository.dao.ConfigDAO;
import nro.repository.dao.HeSoTnsmDAO;
import nro.server.Manager;

/**
 * Tab "Tỉ lệ → 2. Theo bản đồ": tiềm năng gốc mỗi đòn, và hệ số theo nhóm bản
 * đồ.
 *
 * <h2>Một đòn nhận bao nhiêu</h2>
 *
 * <p><b>Tiềm năng gốc</b> (ô trên cùng) × <b>hệ số của nhóm bản đồ</b>, rồi mới
 * tới bùa bà Hạt Mít, bùa x2 TN đệ tử, thẻ, vật phẩm trong game, rồi hệ số
 * chung. Không buff gì thì nhận đúng gốc × hệ số.</p>
 *
 * <p>Mọi ô là số thật — không có kiểu "để trống thì lấy bên trái", trống là 0.
 * "Chỉ đệ" bật thì ô sư phụ tự đánh về 0 và khoá lại. Mọi nhóm luôn bật.</p>
 */
final class HeSoTnsmTab extends JPanel {

    private static final Color XANH = new Color(0, 120, 215);
    private static final Color XANH_LA = new Color(40, 160, 70);
    private static final Color DO = new Color(200, 60, 60);
    private static final Color XAM = new Color(120, 120, 120);

    private static final int C_ID = 0;
    private static final int C_TEN = 1;
    private static final int C_MAP = 2;
    private static final int C_SP = 3;
    private static final int C_DE_DE = 4;
    private static final int C_DE_SP = 5;
    private static final int C_CHIDE = 6;
    private static final int C_GC = 7;

    private static final String[] TEN_COT = {"#", "Nhóm bản đồ", "Các bản đồ",
        "Hệ số", "Đệ nhận ×", "Sư phụ nhận ×", "Chỉ đệ", "Ghi chú"};

    private static final String[] CHU_GIAI = {
        "Số thứ tự, máy tự đặt",
        "Tên nhóm, đặt sao cho dễ nhận ra",
        "Id bản đồ trong nhóm — id đơn hoặc khoảng, ví dụ 68-72,102,103",
        "Sư phụ (người chơi) tự đánh: nhân với tiềm năng gốc. Trống = 0",
        "Đệ tử đánh: hệ số cho phần ĐỆ TỬ nhận. Trống = 0",
        "Đệ tử đánh: hệ số cho phần SƯ PHỤ nhận từ đệ. Trống = 0",
        "Tích thì chỉ đệ tử đánh mới được tiềm năng — ô sư phụ tự đánh về 0",
        "Ghi chú cho chính bạn, máy chủ không đọc tới"
    };

    /** Nhóm cột trên dải phía trên tiêu đề: {cột đầu, cột cuối, tên nhóm}. */
    private static final Object[][] NHOM_COT = {
        {C_SP, C_SP, "Sư phụ tự đánh"},
        {C_DE_DE, C_DE_SP, "Đệ tử đánh"},
    };

    private final DefaultTableModel model = new DefaultTableModel(TEN_COT, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == C_ID) {
                return false;
            }
            // O su phu cua nhom "chi de" khoa o 0.
            return c != C_SP || !chiDe(r);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == C_CHIDE ? Boolean.class : String.class;
        }
    };

    private final DaiNhomCot dai = new DaiNhomCot();

    private final JTable bang = new JTable(model) {
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                @Override
                public String getToolTipText(MouseEvent e) {
                    int i = columnModel.getColumnIndexAtX(e.getPoint().x);
                    if (i < 0) {
                        return null;
                    }
                    int c = columnModel.getColumn(i).getModelIndex();
                    return c >= 0 && c < CHU_GIAI.length ? CHU_GIAI[c] : null;
                }
            };
        }

        /** Dải nhóm cột nằm ngay trên tiêu đề, trong cùng khung cuộn. */
        @Override
        protected void configureEnclosingScrollPane() {
            super.configureEnclosingScrollPane();
            Container p = getParent();
            if (p instanceof JViewport && p.getParent() instanceof JScrollPane) {
                JPanel dau = new JPanel(new BorderLayout());
                dau.add(dai, BorderLayout.NORTH);
                dau.add(getTableHeader(), BorderLayout.CENTER);
                ((JScrollPane) p.getParent()).setColumnHeaderView(dau);
            }
        }
    };

    private final JLabel ghiChuGoc = new JLabel();
    private final JLabel trangThai = new JLabel(" ");
    private final List<Integer> idDong = new ArrayList<>();
    private boolean dangTuSua;

    HeSoTnsmTab() {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel tren = new JPanel();
        tren.setLayout(new BoxLayout(tren, BoxLayout.Y_AXIS));
        tren.setOpaque(false);
        tren.add(canTrai(new JLabel("<html><span style='color:#666'>Mỗi đòn đánh quái nhận "
                + "<b>tiềm năng gốc (tab 1) × hệ số của nhóm bản đồ</b>, rồi mới tới bùa, "
                + "thẻ, vật phẩm trong game. Mọi nhóm luôn bật. Ô để trống là 0. Rê chuột "
                + "lên tiêu đề cột để xem giải thích.</span></html>")));
        tren.add(Box.createVerticalStrut(4));
        ghiChuGoc.setForeground(XAM);
        tren.add(canTrai(ghiChuGoc));
        add(tren, BorderLayout.NORTH);

        caiDatBang();
        add(ServerGuiUtils.cuon(bang), BorderLayout.CENTER);

        JPanel duoi = new JPanel(new BorderLayout());
        duoi.setOpaque(false);
        JPanel nutDuoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        nutDuoi.setOpaque(false);
        nutDuoi.add(nut("Lưu bảng", XANH_LA, this::luuBang));
        nutDuoi.add(nut("Tải lại", XAM, this::napBang));
        nutDuoi.add(nut("Thêm nhóm", XANH, this::themNhom));
        nutDuoi.add(nut("Về mặc định", new Color(120, 90, 160), this::veMacDinh));
        duoi.add(nutDuoi, BorderLayout.WEST);
        trangThai.setBorder(new EmptyBorder(0, 10, 0, 0));
        duoi.add(trangThai, BorderLayout.CENTER);
        add(duoi, BorderLayout.SOUTH);

        napGoc();
        napBang();
    }

    // =====================================================================
    //  Tiềm năng gốc
    // =====================================================================

    private void napGoc() {
        // Hai he so toan may chu van nhan them sau cung. Noi thang ra de khong
        // ai tuong "khong buff ma sao khong ra dung goc x he so".
        double chung = ConfigDAO.phanTram(ConfigDAO.TL_EXP);
        if (chung <= 0) {
            chung = 1;
        }
        double tong = chung * Manager.RATE_EXP_SERVER;
        ghiChuGoc.setText("Sau buff còn nhân exp máy chủ ×" + Manager.RATE_EXP_SERVER
                + " và hệ số chung ×" + soGon(chung) + " = ×" + soGon(tong)
                + (Math.abs(tong - 1) < 1e-9
                        ? " — không buff thì nhận đúng gốc × hệ số." : "."));
    }

    // =====================================================================
    //  Bảng
    // =====================================================================

    private void caiDatBang() {
        bang.setRowHeight(24);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setReorderingAllowed(false);
        int[] w = {30, 170, 200, 90, 90, 110, 60, 330};
        for (int i = 0; i < w.length && i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        DefaultTableCellRenderer giua = new DefaultTableCellRenderer();
        giua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : new int[]{C_DE_DE, C_DE_SP}) {
            bang.getColumnModel().getColumn(c).setCellRenderer(giua);
        }
        bang.getColumnModel().getColumn(C_SP).setCellRenderer(new OSuPhu());

        bang.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                veLaiDai();
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                veLaiDai();
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                veLaiDai();
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                veLaiDai();
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

        // Tich "chi de" la o su phu ve 0 ngay, khong doi bam Luu moi thay.
        model.addTableModelListener(e -> {
            if (dangTuSua || e.getType() != TableModelEvent.UPDATE
                    || e.getColumn() != C_CHIDE) {
                return;
            }
            final int r = e.getFirstRow();
            SwingUtilities.invokeLater(() -> {
                if (r >= 0 && r < model.getRowCount() && chiDe(r)) {
                    dangTuSua = true;
                    try {
                        model.setValueAt("0", r, C_SP);
                    } finally {
                        dangTuSua = false;
                    }
                }
                bang.repaint();
            });
        });
    }

    private boolean chiDe(int r) {
        return r >= 0 && r < model.getRowCount()
                && Boolean.TRUE.equals(model.getValueAt(r, C_CHIDE));
    }

    private void veLaiDai() {
        dai.revalidate();
        dai.repaint();
    }

    private void napBang() {
        dungSua();
        dangTuSua = true;
        try {
            model.setRowCount(0);
            idDong.clear();
            for (HeSoTnsmDAO.Dong d : HeSoTnsmDAO.danhSach()) {
                idDong.add(d.id);
                model.addRow(new Object[]{String.valueOf(d.id), d.ten,
                    d.dsMap == null ? "" : d.dsMap,
                    soGon(d.chiDeTu ? 0 : d.heSo), soGon(d.heSoDeTu), soGon(d.heSoSuPhu),
                    d.chiDeTu, d.ghiChu == null ? "" : d.ghiChu});
            }
        } finally {
            dangTuSua = false;
        }
    }

    private void luuBang() {
        dungSua();
        Map<Integer, HeSoTnsmDAO.Dong> theoId = new HashMap<>();
        for (HeSoTnsmDAO.Dong d : HeSoTnsmDAO.danhSach()) {
            theoId.put(d.id, d);
        }
        int hong = 0;
        String hongDau = null;
        for (int r = 0; r < model.getRowCount() && r < idDong.size(); r++) {
            HeSoTnsmDAO.Dong d = theoId.get(idDong.get(r));
            if (d == null) {
                continue;
            }
            try {
                d.heSo = soThuc(o(r, C_SP));
                d.heSoDeTu = soThuc(o(r, C_DE_DE));
                d.heSoSuPhu = soThuc(o(r, C_DE_SP));
            } catch (NumberFormatException ex) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Nhóm \"" + o(r, C_TEN) + "\" có hệ số không phải số.";
                }
                continue;
            }
            d.ten = o(r, C_TEN);
            d.dsMap = o(r, C_MAP);
            d.chiDeTu = chiDe(r);
            d.bat = true;
            d.ghiChu = o(r, C_GC);
            String loi = HeSoTnsmDAO.luu(d);
            if (loi != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = loi;
                }
            }
        }
        napBang();
        bao(hong == 0 ? XANH_LA : DO, hong == 0
                ? "Đã lưu hệ số tiềm năng theo bản đồ — có hiệu lực ngay."
                : hongDau + " (" + hong + " nhóm không lưu được)");
    }

    private void themNhom() {
        String ten = JOptionPane.showInputDialog(this,
                "Tên nhóm bản đồ mới?\n\nThêm xong thì điền id bản đồ và hệ số rồi bấm Lưu.",
                "Thêm nhóm bản đồ", JOptionPane.QUESTION_MESSAGE);
        if (ten == null || ten.trim().isEmpty()) {
            return;
        }
        HeSoTnsmDAO.Dong d = new HeSoTnsmDAO.Dong();
        d.khoa = "nhom_" + System.currentTimeMillis();
        d.ten = ten.trim();
        d.dsMap = "";
        d.heSo = 1;
        d.heSoDeTu = 1;
        d.heSoSuPhu = 1;
        d.thuTu = 100;
        d.bat = true;
        String loi = HeSoTnsmDAO.luu(d);
        napBang();
        bao(loi == null ? XANH_LA : DO,
                loi == null ? "Đã thêm nhóm — điền id bản đồ rồi bấm Lưu." : loi);
    }

    private void veMacDinh() {
        if (JOptionPane.showConfirmDialog(this, "Xoá sạch bảng rồi gieo lại các nhóm gốc?",
                "Về mặc định", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = HeSoTnsmDAO.gieoLai();
        napBang();
        bao(loi == null ? XANH_LA : DO, loi == null ? "Đã gieo lại các nhóm gốc." : loi);
    }

    // =====================================================================
    //  Tiện ích
    // =====================================================================

    private void dungSua() {
        if (bang.isEditing()) {
            bang.getCellEditor().stopCellEditing();
        }
    }

    private String o(int r, int c) {
        Object v = model.getValueAt(r, c);
        return v == null ? "" : String.valueOf(v).trim();
    }

    /** Ô trống là 0 — không có kiểu "lấy bên trái". */
    private static double soThuc(String s) {
        String v = s.trim().replace(',', '.');
        return v.isEmpty() ? 0 : Double.parseDouble(v);
    }

    /** Hệ số gọn, giữ ba chữ số thập phân — một phần ba không được thành 0,33. */
    private static String soGon(double v) {
        String s = String.format(Locale.US, "%.3f", v);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void bao(Color mau, String chu) {
        trangThai.setForeground(mau);
        trangThai.setText(chu);
    }

    private static JComponent canTrai(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private static JButton nut(String chu, Color nen, Runnable viec) {
        JButton b = new JButton(chu);
        b.setBackground(nen);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        b.setOpaque(true);
        b.addActionListener(e -> viec.run());
        return b;
    }

    /** Ô sư phụ: căn giữa; nhóm "chỉ đệ" thì xám và ghi rõ đang khoá. */
    private final class OSuPhu extends DefaultTableCellRenderer {

        OSuPhu() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean chon,
                boolean tieuDiem, int r, int c) {
            Component o = super.getTableCellRendererComponent(t, v, chon, tieuDiem, r, c);
            boolean khoa = chiDe(t.convertRowIndexToModel(r));
            if (!chon) {
                o.setBackground(khoa ? new Color(238, 238, 238) : t.getBackground());
            }
            o.setForeground(khoa ? XAM : t.getForeground());
            setToolTipText(khoa ? "Nhóm chỉ đệ tử — sư phụ tự đánh luôn nhận 0" : null);
            return o;
        }
    }

    /** Dải nhóm cột phía trên tiêu đề. */
    private final class DaiNhomCot extends JComponent {

        DaiNhomCot() {
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(bang.getColumnModel().getTotalColumnWidth(), 22);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(240, 243, 247));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            TableColumnModel cm = bang.getColumnModel();
            for (Object[] n : NHOM_COT) {
                int dau = (Integer) n[0];
                int cuoi = (Integer) n[1];
                int x0 = -1;
                int x1 = -1;
                int x = 0;
                for (int i = 0; i < cm.getColumnCount(); i++) {
                    int w = cm.getColumn(i).getWidth();
                    int mi = cm.getColumn(i).getModelIndex();
                    if (mi >= dau && mi <= cuoi) {
                        if (x0 < 0) {
                            x0 = x;
                        }
                        x1 = x + w;
                    }
                    x += w;
                }
                if (x0 < 0) {
                    continue;
                }
                g.setColor(new Color(214, 228, 244));
                g.fillRect(x0, 0, x1 - x0, getHeight());
                g.setColor(new Color(176, 196, 222));
                g.drawRect(x0, 0, x1 - x0 - 1, getHeight() - 1);
                String s = (String) n[2];
                g.setColor(new Color(24, 58, 110));
                int tx = x0 + Math.max(4, (x1 - x0 - fm.stringWidth(s)) / 2);
                g.drawString(s, tx, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }
}
