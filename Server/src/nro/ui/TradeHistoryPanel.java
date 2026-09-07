package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import nro.repository.dao.ConfigDAO;
import nro.repository.dao.HistoryTransactionDAO;

/**
 * Nhật ký giao dịch giữa hai người chơi.
 *
 * <h2>Dữ liệu đến từ đâu</h2>
 *
 * <p>Không phải tab này sinh ra dữ liệu. {@code Trade.startTrade()} đã ghi mỗi
 * lượt giao dịch thành công vào bảng {@code history_transaction} từ trước, kèm
 * <b>ảnh chụp hành trang hai bên trước và sau</b>. Tab này chỉ là chỗ đọc lại
 * cho ra hồn.</p>
 *
 * <h2>Vì sao ảnh chụp hành trang lại quan trọng</h2>
 *
 * <p>Đó là thứ duy nhất phân biệt "A đưa B một món" với "món tự sinh ra". Nghi
 * ngờ nhân đồ thì mở một lượt ra, so hành trang trước và sau của cả hai bên: số
 * món phải khớp, không bên nào tự nhiên nhiều thêm.</p>
 *
 * <h2>Máy chủ tự dọn nhật ký cũ</h2>
 *
 * <p>Mỗi lần khởi động, máy chủ xoá các lượt cũ hơn
 * {@code panel_config.giu_lich_su_gd_ngay} ngày. Trước đây con số đó viết cứng
 * là <b>3</b>, nên tab này sẽ không bao giờ xem được quá ba ngày mà không ai
 * hiểu vì sao. Nay mặc định 30 ngày, đặt {@code 0} là giữ mãi.</p>
 */
public class TradeHistoryPanel extends JPanel {

    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color OK_GREEN = new Color(40, 160, 70);
    private static final Color WARN_RED = new Color(200, 60, 60);
    private static final Color GREY = new Color(120, 120, 120);

    private static final java.text.SimpleDateFormat NGAY =
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final JTextField fNguoi = new JTextField(16);
    private final JTextField fVatPham = new JTextField(16);
    private final JTextField fTu = new JTextField(17);
    private final JTextField fDen = new JTextField(17);
    private final JCheckBox cbCoVang = new JCheckBox("Chỉ lượt có vàng");
    private final JCheckBox cbMotChieu = new JCheckBox("Chỉ lượt một chiều");
    private final JComboBox<String> cbGioiHan =
            new JComboBox<>(new String[]{"200", "500", "1000", "5000"});

    private final JLabel lblStatus = new JLabel(" ");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Thời gian", "Người A", "A đưa", "Người B", "B đưa"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    /** Kết quả của lần tra gần nhất — để mở chi tiết không phải truy vấn lại. */
    private List<HistoryTransactionDAO.Dong> ketQua = new java.util.ArrayList<>();

    private final JTextArea chiTiet = new JTextArea();

    public TradeHistoryPanel() {
        setLayout(new BorderLayout(0, 6));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Lịch Sử Giao Dịch");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        add(buildThan(), BorderLayout.CENTER);

        lblStatus.setForeground(GREY);
        lblStatus.setBorder(new EmptyBorder(4, 2, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);

        // Lich su giao dich ke ten vat pham, doi mau la phai ve lai.
        LamMoi.nghe(this::load, LamMoi.VAT_PHAM, LamMoi.NGUOI_CHOI);
    }

    private JPanel buildThan() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.add(buildLoc(), BorderLayout.NORTH);

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setAutoCreateRowSorter(true);
        int[] w = {55, 150, 150, 280, 150, 280};
        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienChiTiet();
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    moChiTiet();
                }
            }
        });

        chiTiet.setEditable(false);
        chiTiet.setFont(new Font("Consolas", Font.PLAIN, 12));
        chiTiet.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane spChiTiet = ServerGuiUtils.cuon(chiTiet);
        spChiTiet.setBorder(BorderFactory.createTitledBorder(
                "Chi tiết lượt đang chọn — nháy đúp để xem to"));

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                ServerGuiUtils.cuon(table), spChiTiet);
        sp.setResizeWeight(0.62);
        sp.setBorder(null);
        root.add(sp, BorderLayout.CENTER);

        JPanel act = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        act.setOpaque(false);
        act.add(button("Tra cứu", OK_GREEN, e -> load()));
        act.add(button("Xem chi tiết", ACCENT, e -> moChiTiet()));
        act.add(button("Xuất CSV", GREY, e -> xuatCsv()));
        JLabel tip = new JLabel("  (nháy đúp một dòng để xem chi tiết)");
        tip.setForeground(GREY);
        act.add(tip);
        root.add(act, BorderLayout.SOUTH);

        datKhoangNgay(7);
        load();
        return root;
    }

    private JPanel buildLoc() {
        JPanel hang1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        hang1.setOpaque(false);
        hang1.add(new JLabel("Người chơi:"));
        hang1.add(fNguoi);
        hang1.add(new JLabel("Vật phẩm:"));
        hang1.add(fVatPham);
        hang1.add(new JLabel("Tối đa:"));
        cbGioiHan.setSelectedItem("500");
        hang1.add(cbGioiHan);
        hang1.add(cbCoVang);
        hang1.add(cbMotChieu);

        JPanel hang2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        hang2.setOpaque(false);
        hang2.add(new JLabel("Từ:"));
        hang2.add(fTu);
        hang2.add(new JLabel("Đến:"));
        hang2.add(fDen);
        hang2.add(button("Hôm nay", GREY, e -> {
            datKhoangNgay(0);
            load();
        }));
        hang2.add(button("7 ngày", GREY, e -> {
            datKhoangNgay(7);
            load();
        }));
        hang2.add(button("30 ngày", GREY, e -> {
            datKhoangNgay(30);
            load();
        }));
        hang2.add(button("Tất cả", GREY, e -> {
            fTu.setText("");
            fDen.setText("");
            load();
        }));

        JPanel box = new JPanel(new java.awt.GridLayout(2, 1));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createTitledBorder("Bộ lọc"));
        box.add(hang1);
        box.add(hang2);

        // Go xong bam Enter la tra luon — khong phai voi chuot xuong nut.
        java.awt.event.ActionListener enter = e -> load();
        fNguoi.addActionListener(enter);
        fVatPham.addActionListener(enter);
        fTu.addActionListener(enter);
        fDen.addActionListener(enter);
        cbCoVang.addActionListener(enter);
        cbMotChieu.addActionListener(enter);
        cbGioiHan.addActionListener(enter);
        return box;
    }

    /** Đặt hai ô thời gian thành khoảng {@code soNgay} ngày gần nhất. */
    private void datKhoangNgay(int soNgay) {
        long nay = System.currentTimeMillis();
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(nay);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.add(java.util.Calendar.DAY_OF_MONTH, -soNgay);
        fTu.setText(NGAY.format(c.getTime()));
        fDen.setText(NGAY.format(new java.util.Date(nay)));
    }

    private void load() {
        HistoryTransactionDAO.Loc f = new HistoryTransactionDAO.Loc();
        try {
            f.tu = mocThoiGian(fTu.getText());
            f.den = mocThoiGian(fDen.getText());
        } catch (java.text.ParseException ex) {
            bao(WARN_RED, "Thời gian phải theo dạng yyyy-MM-dd HH:mm:ss "
                    + "(hoặc để trống). Ví dụ: 2026-08-23 00:00:00");
            return;
        }
        f.nguoi = fNguoi.getText();
        f.vatPham = fVatPham.getText();
        f.chiCoVang = cbCoVang.isSelected();
        f.chiMotChieu = cbMotChieu.isSelected();
        f.gioiHan = Integer.parseInt(String.valueOf(cbGioiHan.getSelectedItem()));

        ketQua = HistoryTransactionDAO.tim(f);
        model.setRowCount(0);
        for (HistoryTransactionDAO.Dong d : ketQua) {
            model.addRow(new Object[]{d.id,
                d.luc == null ? "" : NGAY.format(d.luc),
                HistoryTransactionDAO.Dong.chiTen(d.nguoi1), motDong(d.choDi1),
                HistoryTransactionDAO.Dong.chiTen(d.nguoi2), motDong(d.choDi2)});
        }
        chiTiet.setText("");

        int tong = HistoryTransactionDAO.demTatCa();
        java.sql.Timestamp cu = HistoryTransactionDAO.cuNhat();
        long giu = ConfigDAO.num(ConfigDAO.GIU_LICH_SU_GD_NGAY);
        StringBuilder sb = new StringBuilder();
        sb.append("Hiện ").append(ketQua.size()).append(" lượt");
        if (ketQua.size() >= f.gioiHan) {
            sb.append(" (chạm mức tối đa — thu hẹp bộ lọc hoặc tăng \"Tối đa\")");
        }
        sb.append(".  Cả bảng có ").append(tong).append(" lượt");
        if (cu != null) {
            sb.append(", cũ nhất ").append(NGAY.format(cu));
        }
        sb.append(".  Máy chủ giữ ")
                .append(giu <= 0 ? "vĩnh viễn" : giu + " ngày")
                .append(" (sửa ở Cấu Hình → Quy ước, khoá giu_lich_su_gd_ngay).");
        bao(GREY, sb.toString());
    }

    /**
     * Đọc một ô thời gian.
     *
     * @return {@code null} nếu ô để trống — nghĩa là không giới hạn phía đó
     */
    private static java.sql.Timestamp mocThoiGian(String raw) throws java.text.ParseException {
        String t = raw == null ? "" : raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        // Cho phep go moi ngay, tu hieu la 00:00:00 cua ngay do.
        if (t.length() == 10) {
            t = t + " 00:00:00";
        }
        return new java.sql.Timestamp(NGAY.parse(t).getTime());
    }

    /** Rút nội dung nhiều dòng thành một dòng cho vừa ô bảng. */
    private static String motDong(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace("\n", " • ").replace("\r", "").trim();
        return t.length() <= 120 ? t : t.substring(0, 117) + "...";
    }

    private HistoryTransactionDAO.Dong dangChon() {
        int r = table.getSelectedRow();
        if (r < 0) {
            return null;
        }
        int idx = table.convertRowIndexToModel(r);
        return idx >= 0 && idx < ketQua.size() ? ketQua.get(idx) : null;
    }

    private void hienChiTiet() {
        HistoryTransactionDAO.Dong d = dangChon();
        chiTiet.setText(d == null ? "" : moTa(d));
        chiTiet.setCaretPosition(0);
    }

    /**
     * Dựng bản mô tả đầy đủ một lượt.
     *
     * <p>Đặt hành trang <b>trước</b> ngay trên <b>sau</b> của cùng một người để
     * so bằng mắt — đó là việc thật sự cần làm khi soi nghi ngờ nhân đồ.</p>
     */
    private static String moTa(HistoryTransactionDAO.Dong d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lượt #").append(d.id)
                .append("   lúc ").append(d.luc == null ? "?" : NGAY.format(d.luc))
                .append(HistoryTransactionDAO.motChieu(d) ? "   [MỘT CHIỀU]" : "")
                .append("\n\n");
        sb.append("── ").append(d.nguoi1).append(" ──\n");
        sb.append("  đưa đi : ").append(nz(d.choDi1)).append('\n');
        sb.append("  túi trước: ").append(nz(d.tuiTruoc1)).append('\n');
        sb.append("  túi sau  : ").append(nz(d.tuiSau1)).append("\n\n");
        sb.append("── ").append(d.nguoi2).append(" ──\n");
        sb.append("  đưa đi : ").append(nz(d.choDi2)).append('\n');
        sb.append("  túi trước: ").append(nz(d.tuiTruoc2)).append('\n');
        sb.append("  túi sau  : ").append(nz(d.tuiSau2)).append('\n');
        return sb.toString();
    }

    private static String nz(String s) {
        return s == null || s.trim().isEmpty() ? "(không có)" : s.trim();
    }

    private void moChiTiet() {
        HistoryTransactionDAO.Dong d = dangChon();
        if (d == null) {
            bao(WARN_RED, "Chưa chọn lượt nào.");
            return;
        }
        JTextArea ta = new JTextArea(moTa(d));
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setCaretPosition(0);

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((java.awt.Frame) owner, "Lượt giao dịch #" + d.id, true);
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setBorder(new EmptyBorder(10, 10, 10, 10));
        wrap.add(ServerGuiUtils.cuon(ta), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Chép vào clipboard", GREY, e -> {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(ta.getText()), null);
            bao(OK_GREEN, "Đã chép chi tiết lượt #" + d.id + " vào clipboard.");
        }));
        south.add(button("Đóng", ACCENT, e -> dlg.dispose()));
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.setSize(new Dimension(760, 560));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /**
     * Xuất kết quả đang hiện ra tệp CSV.
     *
     * <p>Xuất <b>đúng những gì bộ lọc đang cho ra</b>, không xuất cả bảng: người
     * dùng thấy 40 dòng mà tệp ra 4.000 dòng là một bất ngờ khó chịu.</p>
     */
    private void xuatCsv() {
        if (ketQua.isEmpty()) {
            bao(WARN_RED, "Không có dòng nào để xuất.");
            return;
        }
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setSelectedFile(new java.io.File("lich-su-giao-dich.csv"));
        if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File f = fc.getSelectedFile();
        try (java.io.PrintWriter w = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(f),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            // BOM de Excel doc dung tieng Viet; khong co no thi mo ra day dau hoi.
            w.write('﻿');
            w.println("id,thoi_gian,nguoi_a,a_dua,nguoi_b,b_dua,"
                    + "tui_a_truoc,tui_a_sau,tui_b_truoc,tui_b_sau");
            for (HistoryTransactionDAO.Dong d : ketQua) {
                w.println(String.join(",",
                        o(String.valueOf(d.id)),
                        o(d.luc == null ? "" : NGAY.format(d.luc)),
                        o(d.nguoi1), o(d.choDi1), o(d.nguoi2), o(d.choDi2),
                        o(d.tuiTruoc1), o(d.tuiSau1), o(d.tuiTruoc2), o(d.tuiSau2)));
            }
            bao(OK_GREEN, "Đã xuất " + ketQua.size() + " lượt ra " + f.getAbsolutePath());
        } catch (Exception ex) {
            bao(WARN_RED, "Không ghi được tệp: " + ex.getMessage());
        }
    }

    /** Bọc một ô CSV: nội dung có dấu phẩy và xuống dòng nên luôn phải bọc. */
    private static String o(String s) {
        String t = s == null ? "" : s.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
        return '"' + t + '"';
    }

    private void bao(Color c, String s) {
        lblStatus.setForeground(c);
        lblStatus.setText(s);
    }

    private static JButton button(String text, Color bg, java.awt.event.ActionListener a) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.addActionListener(a);
        return b;
    }
}
