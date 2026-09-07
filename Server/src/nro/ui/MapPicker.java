package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import nro.repository.dao.MapShopDAO;

/**
 * Ô chọn <b>bản đồ</b> — tích vào danh sách thay vì gõ dãy id.
 *
 * <p>Cột {@code boss_config.map_join} là một dãy id ngăn nhau bằng dấu phẩy, ví
 * dụ {@code "21,22,23"}. Không ai nhớ được 21 là bản đồ nào; gõ nhầm một số thì
 * boss mọc ở đâu không biết, mà panel lại báo lưu thành công vì dãy số vẫn hợp
 * lệ. Lớp này bày cả danh sách bản đồ có tên để tích chọn, rồi dựng lại đúng
 * dãy CSV cũ nên phần lưu phía sau không đổi gì.</p>
 *
 * <p><b>Id lạ được giữ nguyên.</b> Nếu ô đang có id không còn trong
 * {@code map_template}, dòng đó vẫn hiện ở đầu bảng và vẫn được tích — đánh dấu
 * "không có trong map_template". Âm thầm bỏ đi là sửa dữ liệu của người khác mà
 * không hỏi.</p>
 */
public final class MapPicker {

    private static final Color OK_GREEN = new Color(40, 160, 70);
    private static final Color GREY = new Color(120, 120, 120);

    private MapPicker() {
    }

    /** Một bản đồ trong danh sách chọn. */
    private static final class Dong {

        int id;
        String ten;
        int soKhu;
        boolean con;
    }

    /**
     * Toàn bộ bản đồ, kèm những id lạ đang có trong {@code csv}.
     *
     * <p>Đọc thẳng {@code map_template} chứ không dùng {@code Manager.MAPS}: tab
     * cấu hình phải mở được cả khi máy chủ chưa chạy.</p>
     */
    private static List<Dong> danhSach(String csv) {
        List<Dong> out = new ArrayList<>();
        Set<Integer> daCo = new LinkedHashSet<>();
        for (MapShopDAO.MapRow m : MapShopDAO.maps()) {
            Dong d = new Dong();
            d.id = m.id;
            d.ten = m.name == null || m.name.isEmpty() ? "(chưa đặt tên)" : m.name;
            d.soKhu = m.zones;
            d.con = true;
            out.add(d);
            daCo.add(m.id);
        }
        // Id la dua len dau de nhin thay ngay.
        List<Dong> la = new ArrayList<>();
        for (int id : tachSo(csv)) {
            if (!daCo.contains(id)) {
                Dong d = new Dong();
                d.id = id;
                d.ten = "(không có trong map_template)";
                d.soKhu = 0;
                d.con = false;
                la.add(d);
                daCo.add(id);
            }
        }
        la.addAll(out);
        return la;
    }

    /** Tách dãy CSV thành các số, bỏ qua phần rác. */
    public static List<Integer> tachSo(String csv) {
        List<Integer> out = new ArrayList<>();
        if (csv == null) {
            return out;
        }
        for (String p : csv.split(",")) {
            String s = p.trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                out.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
                // Rac trong o cu — bo qua, khong lam hong ca danh sach.
            }
        }
        return out;
    }

    /**
     * Diễn dãy id thành chữ đọc được: {@code "21 (Rừng Nấm), 22 (Đồi Cỏ)"}.
     *
     * <p>Dùng cho nhãn xem trước cạnh ô nhập.</p>
     */
    public static String moTa(String csv) {
        List<Integer> ids = tachSo(csv);
        if (ids.isEmpty()) {
            return "(giữ nguyên map gốc trong mã nguồn)";
        }
        java.util.Map<Integer, String> ten = new java.util.HashMap<>();
        try {
            for (MapShopDAO.MapRow m : MapShopDAO.maps()) {
                ten.put(m.id, m.name);
            }
        } catch (Exception ignored) {
            // Khong doc duoc CSDL -> van hien duoc dãy id.
        }
        StringBuilder sb = new StringBuilder();
        for (int id : ids) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            String t = ten.get(id);
            sb.append(id).append(" (").append(t == null ? "không rõ" : t).append(')');
        }
        return sb.toString();
    }

    /**
     * Tên một bản đồ, không kèm id.
     *
     * <p>{@link #moTa(String)} trả về dạng {@code "108 (Dòng sông băng)"} — hợp
     * để hiện cạnh ô nhập, nhưng không dùng làm tên mặc định điền vào ô được.
     * Hàm này trả đúng phần tên.</p>
     *
     * @return tên bản đồ, hoặc {@code "Bản đồ <id>"} khi không tra ra
     */
    public static String tenMap(int id) {
        try {
            for (MapShopDAO.MapRow m : MapShopDAO.maps()) {
                if (m.id == id) {
                    return m.name;
                }
            }
        } catch (Exception boQua) {
            // Khong doc duoc CSDL -> ten thay the ben duoi van dung duoc.
        }
        return "Bản đồ " + id;
    }

    /**
     * Mở hộp thoại chọn bản đồ.
     *
     * @param cha  thành phần để canh vị trí hộp thoại
     * @param csv  dãy id đang có, có thể {@code null}
     * @return dãy id mới, chuỗi rỗng nếu bỏ tích hết, hoặc {@code null} nếu huỷ
     */
    public static String chon(Component cha, String csv) {
        final List<Dong> tatCa = danhSach(csv);
        if (tatCa.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(cha,
                    "Không đọc được bảng map_template.\n"
                    + "Kiểm tra MySQL đã chạy chưa (XAMPP → Start MariaDB).",
                    "Chưa có danh sách bản đồ",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
        final Set<Integer> chon = new LinkedHashSet<>(tachSo(csv));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Chọn", "ID", "Tên bản đồ", "Số khu"}, 0) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : c == 2 ? String.class : Integer.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);

        final JLabel dem = new JLabel();
        final JTextField fTim = new JTextField(22);

        // Do bang theo tu khoa dang loc. Tich nam trong "chon" chu khong nam
        // trong bang, nen loc lai khong lam mat lua chon.
        final Runnable doBang = new Runnable() {
            @Override
            public void run() {
                String q = fTim.getText().trim().toLowerCase();
                model.setRowCount(0);
                for (Dong d : tatCa) {
                    if (!q.isEmpty()
                            && !d.ten.toLowerCase().contains(q)
                            && !String.valueOf(d.id).contains(q)) {
                        continue;
                    }
                    model.addRow(new Object[]{chon.contains(d.id), d.id,
                        d.con ? d.ten : d.ten + "  ⚠", d.soKhu});
                }
                dem.setText("  Đang chọn: " + chon.size() + " bản đồ");
            }
        };
        doBang.run();

        model.addTableModelListener(e -> {
            if (e.getColumn() != 0 || e.getFirstRow() < 0) {
                return;
            }
            for (int r = e.getFirstRow(); r <= e.getLastRow() && r < model.getRowCount(); r++) {
                int id = (Integer) model.getValueAt(r, 1);
                if (Boolean.TRUE.equals(model.getValueAt(r, 0))) {
                    chon.add(id);
                } else {
                    chon.remove(id);
                }
            }
            dem.setText("  Đang chọn: " + chon.size() + " bản đồ");
        });

        fTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                doBang.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doBang.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doBang.run();
            }
        });

        JPanel bac = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bac.add(new JLabel("Tìm theo tên hoặc id:"));
        bac.add(fTim);
        bac.add(button("Bỏ tích hết", GREY, e -> {
            chon.clear();
            doBang.run();
        }));
        bac.add(dem);

        Window owner = SwingUtilities.getWindowAncestor(cha);
        final JDialog dlg = owner instanceof java.awt.Frame
                ? new JDialog((java.awt.Frame) owner, "Chọn bản đồ boss xuất hiện", true)
                : new JDialog((java.awt.Dialog) owner, "Chọn bản đồ boss xuất hiện", true);

        final String[] ketQua = {null};
        JPanel nam = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        nam.add(button("Huỷ", GREY, e -> dlg.dispose()));
        nam.add(button("Xong", OK_GREEN, e -> {
            StringBuilder sb = new StringBuilder();
            for (int id : chon) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(id);
            }
            ketQua[0] = sb.toString();
            dlg.dispose();
        }));

        JLabel goi = new JLabel("<html><span style='color:#777'>"
                + "Tích nhiều bản đồ thì mỗi lần hồi sinh boss chọn ngẫu nhiên một cái.<br>"
                + "Bỏ tích hết = <b>giữ nguyên</b> map gốc viết trong mã nguồn, "
                + "không phải \"boss không mọc ở đâu cả\".<br>"
                + "Dòng có ⚠ là id không còn trong <code>map_template</code> — "
                + "vẫn giữ nguyên nếu bạn không bỏ tích."
                + "</span></html>");
        goi.setBorder(new EmptyBorder(0, 8, 6, 8));

        JPanel duoi = new JPanel(new BorderLayout());
        duoi.add(goi, BorderLayout.NORTH);
        duoi.add(nam, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(6, 6, 6, 6));
        wrap.add(bac, BorderLayout.NORTH);
        wrap.add(ServerGuiUtils.cuon(table), BorderLayout.CENTER);
        wrap.add(duoi, BorderLayout.SOUTH);

        dlg.setContentPane(wrap);
        dlg.setSize(new Dimension(620, 560));
        dlg.setLocationRelativeTo(cha);
        dlg.setVisible(true);
        return ketQua[0];
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
