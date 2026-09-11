package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import nro.repository.dao.ConfigDAO;
import nro.repository.dao.TnGocDAO;

/**
 * Tab "Tỉ lệ → 1. Tiềm năng gốc": cách game tính tiềm năng của một đòn, trước
 * hệ số bản đồ và buff.
 *
 * <p>Tính theo <b>sát thương</b> và <b>cấp</b> như game gốc — tab này chỉ đưa
 * các con số của công thức ra để sửa, và mọi con số đã điền sẵn đúng như game.
 * Lưu là có hiệu lực ngay ở đòn đánh kế tiếp.</p>
 */
final class TnGocTab extends JPanel {

    private static final Color XANH = new Color(0, 120, 215);
    private static final Color XANH_LA = new Color(40, 160, 70);
    private static final Color DO = new Color(200, 60, 60);
    private static final Color XAM = new Color(120, 120, 120);

    /** {khoá quy ước, số gốc của game, chữ đứng bên phải ô nhập}. */
    private static final Object[][] THAM_SO = {
        {ConfigDAO.TN_GOC_GIAM_MOI_CAP, 10L,
            "% bớt đi cho <b>mỗi cấp</b> người chơi <b>cao hơn</b> quái"},
        {ConfigDAO.TN_GOC_TANG_MOI_CAP, 10L,
            "% thêm vào cho <b>mỗi cấp</b> người chơi <b>thấp hơn</b> quái"},
        {ConfigDAO.TN_GOC_NGUONG_MAU_LON, 100_000_000L,
            "máu — quái có máu từ mức này trở lên là <b>quái máu lớn</b>"},
        {ConfigDAO.TN_GOC_NHAN_MAU_LON, 5L,
            "lần — quái máu lớn: phần trăm máu mỗi đòn lấy đi được <b>nhân thêm</b>"},
        {ConfigDAO.TN_GOC_DAO_DONG, 2L,
            "% — mỗi con quái lúc hồi sinh lệch ngẫu nhiên <b>±</b> ngần này"},
    };

    private static final int C_ID = 0;
    private static final int C_TEN = 1;
    private static final int C_MAU = 2;
    private static final int C_PT = 3;
    private static final int C_GOC = 4;
    private static final int C_GC = 5;

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
        "Id", "Tên quái", "Máu gốc", "Tiềm năng (% máu)", "Số gốc của game", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == C_PT || c == C_GC;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case C_ID:
                case C_PT:
                case C_GOC:
                    return Integer.class;
                case C_MAU:
                    return Long.class;
                default:
                    return String.class;
            }
        }
    };

    private final JTable bang = new JTable(model);
    private final TableRowSorter<DefaultTableModel> boLoc = new TableRowSorter<>(model);
    private final JTextField[] oThamSo = new JTextField[THAM_SO.length];
    private final JTextField oTim = new JTextField(18);
    private final JLabel trangThai = new JLabel(" ");
    /** Số lúc nạp của từng dòng — để chỉ ghi những dòng vừa sửa. */
    private final List<TnGocDAO.Dong> dangNap = new ArrayList<>();

    TnGocTab() {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel tren = new JPanel();
        tren.setLayout(new BoxLayout(tren, BoxLayout.Y_AXIS));
        tren.setOpaque(false);
        tren.add(canTrai(new JLabel("<html>"
                + "<b>Tiềm năng gốc của một đòn</b> — đúng cách tính của game, các con số "
                + "đã điền sẵn như game gốc:<br>"
                + "&nbsp;① Đòn đánh lấy đi bao nhiêu <b>% máu tối đa</b> của quái (số "
                + "nguyên — đòn chưa tới 1% máu tính là 0%).<br>"
                + "&nbsp;② Nhân với <b>máu tối đa × % tiềm năng của loại quái</b> (cột "
                + "trong bảng), rồi chia 100.<br>"
                + "&nbsp;③ Mỗi cấp sức mạnh người chơi cao hơn quái thì bớt, thấp hơn "
                + "thì thêm. Ít nhất luôn được 1.<br>"
                + "<span style='color:#666'>Ví dụ: quái 10.000 máu, 5% tiềm năng, đòn "
                + "1.000 sát thương (10% máu), cùng cấp → 10 × 10.000 × 5% ÷ 100 = "
                + "<b>50</b>.<br>"
                + "Sau số gốc này mới tới hệ số bản đồ (tab 2), bùa, thẻ, vật phẩm, "
                + "bậc giảm theo sức mạnh (tab 3) và hệ số chung (tab 4).</span>"
                + "</html>")));
        tren.add(Box.createVerticalStrut(8));
        tren.add(canTrai(formThamSo()));
        tren.add(Box.createVerticalStrut(8));

        JPanel dongTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        dongTim.setOpaque(false);
        dongTim.add(oTim);
        dongTim.add(new JLabel("Tìm quái theo tên hoặc id"));
        tren.add(canTrai(dongTim));
        add(tren, BorderLayout.NORTH);

        caiDatBang();
        add(ServerGuiUtils.cuon(bang), BorderLayout.CENTER);

        JPanel duoi = new JPanel(new BorderLayout());
        duoi.setOpaque(false);
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        nut.setOpaque(false);
        nut.add(nut("Lưu", XANH_LA, this::luu));
        nut.add(nut("Tải lại", XAM, this::nap));
        nut.add(nut("Về số gốc của game", new Color(120, 90, 160), this::veMacDinh));
        duoi.add(nut, BorderLayout.WEST);
        trangThai.setBorder(new EmptyBorder(0, 10, 0, 0));
        duoi.add(trangThai, BorderLayout.CENTER);
        add(duoi, BorderLayout.SOUTH);

        nap();
    }

    /** Ô nhập bên trái, chữ tiếng Việt bên phải. */
    private JComponent formThamSo() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Các con số của công thức"),
                new EmptyBorder(2, 6, 4, 6)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < THAM_SO.length; i++) {
            oThamSo[i] = new JTextField(10);
            oThamSo[i].setHorizontalAlignment(JTextField.RIGHT);
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            p.add(oThamSo[i], c);
            c.gridx = 1;
            c.weightx = 1;
            p.add(new JLabel("<html>" + THAM_SO[i][2] + "</html>"), c);
        }
        return p;
    }

    private void caiDatBang() {
        bang.setRowHeight(24);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setReorderingAllowed(false);
        bang.setRowSorter(boLoc);
        int[] w = {50, 200, 120, 130, 120, 300};
        for (int i = 0; i < w.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        DefaultTableCellRenderer mau = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                setText(v instanceof Number ? PlayerManagerPanel.fmt(((Number) v).longValue()) : "");
            }
        };
        mau.setHorizontalAlignment(SwingConstants.RIGHT);
        bang.getColumnModel().getColumn(C_MAU).setCellRenderer(mau);

        // Dong da sua khac so goc cua game thi to dam, de biet minh da dong vao
        // nhung con nao.
        DefaultTableCellRenderer phanTram = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean chon,
                    boolean tieuDiem, int r, int c) {
                Component o = super.getTableCellRendererComponent(t, v, chon, tieuDiem, r, c);
                int m = t.convertRowIndexToModel(r);
                Object goc = model.getValueAt(m, C_GOC);
                boolean khac = v != null && goc != null && !v.equals(goc);
                setFont(getFont().deriveFont(khac ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
                if (!chon) {
                    o.setForeground(khac ? XANH : t.getForeground());
                }
                return o;
            }
        };
        phanTram.setHorizontalAlignment(SwingConstants.CENTER);
        bang.getColumnModel().getColumn(C_PT).setCellRenderer(phanTram);

        DefaultTableCellRenderer xam = new DefaultTableCellRenderer();
        xam.setHorizontalAlignment(SwingConstants.CENTER);
        xam.setForeground(XAM);
        bang.getColumnModel().getColumn(C_GOC).setCellRenderer(xam);

        oTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loc();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loc();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loc();
            }
        });
    }

    private void loc() {
        String s = oTim.getText().trim();
        boLoc.setRowFilter(s.isEmpty() ? null
                : RowFilter.regexFilter("(?iu)" + Pattern.quote(s), C_ID, C_TEN));
    }

    private void nap() {
        dungSua();
        for (int i = 0; i < THAM_SO.length; i++) {
            oThamSo[i].setText(String.valueOf(
                    ConfigDAO.num((String) THAM_SO[i][0], (Long) THAM_SO[i][1])));
        }
        TnGocDAO.reload();
        model.setRowCount(0);
        dangNap.clear();
        for (TnGocDAO.Dong d : TnGocDAO.danhSach()) {
            dangNap.add(d);
            model.addRow(new Object[]{d.id, d.ten, d.hp, d.phanTram, d.phanTramGoc,
                d.ghiChu});
        }
    }

    private void luu() {
        dungSua();
        // Kiem tra het roi moi ghi, de khong ghi duoc nua chung.
        long[] so = new long[THAM_SO.length];
        for (int i = 0; i < THAM_SO.length; i++) {
            String t = oThamSo[i].getText().trim().replace(".", "").replace(",", "");
            try {
                so[i] = t.isEmpty() ? 0 : Long.parseLong(t);
            } catch (NumberFormatException ex) {
                bao(DO, "Ô \"" + chuTho((String) THAM_SO[i][2]) + "\" phải là số nguyên.");
                return;
            }
            if (so[i] < 0) {
                bao(DO, "Các con số của công thức không được âm.");
                return;
            }
        }
        if (so[0] > 100) {
            bao(DO, "Mỗi cấp bớt đi tối đa 100%.");
            return;
        }
        int hong = 0;
        for (int i = 0; i < THAM_SO.length; i++) {
            if (!ConfigDAO.set((String) THAM_SO[i][0], String.valueOf(so[i]))) {
                hong++;
            }
        }
        ConfigDAO.reload();

        int doi = 0;
        String loiDau = null;
        for (int r = 0; r < model.getRowCount() && r < dangNap.size(); r++) {
            TnGocDAO.Dong cu = dangNap.get(r);
            Object v = model.getValueAt(r, C_PT);
            int pt = v instanceof Number ? ((Number) v).intValue() : 0;
            Object g = model.getValueAt(r, C_GC);
            String gc = g == null ? "" : String.valueOf(g).trim();
            if (pt == cu.phanTram && gc.equals(cu.ghiChu == null ? "" : cu.ghiChu)) {
                continue;
            }
            String loi = TnGocDAO.luu(cu.id, pt, gc);
            if (loi == null) {
                doi++;
            } else {
                hong++;
                if (loiDau == null) {
                    loiDau = loi;
                }
            }
        }
        nap();
        if (hong == 0) {
            bao(XANH_LA, "Đã lưu" + (doi > 0 ? " (" + doi + " loại quái)" : "")
                    + " — có hiệu lực ngay ở đòn đánh kế tiếp.");
        } else {
            bao(DO, (loiDau != null ? loiDau : "Không lưu được một số con số")
                    + " — xem log máy chủ.");
        }
    }

    private void veMacDinh() {
        if (JOptionPane.showConfirmDialog(this,
                "Đưa mọi con số của công thức và % tiềm năng của mọi loại quái\n"
                + "về đúng số gốc của game?",
                "Về số gốc của game", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        for (Object[] t : THAM_SO) {
            ConfigDAO.set((String) t[0], String.valueOf(t[1]));
        }
        ConfigDAO.reload();
        String loi = TnGocDAO.veMacDinh();
        nap();
        bao(loi == null ? XANH_LA : DO, loi == null ? "Đã đưa về số gốc của game." : loi);
    }

    private void dungSua() {
        if (bang.isEditing()) {
            bang.getCellEditor().stopCellEditing();
        }
    }

    private static String chuTho(String html) {
        return html.replaceAll("<[^>]+>", "");
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
}
