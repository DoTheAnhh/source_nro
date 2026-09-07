package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Hộp chọn ảnh theo id: gõ id hoặc dải id, bấm vào ảnh là chọn.
 *
 * <p>Thư mục ảnh có hơn 22 nghìn tệp nên <b>không</b> nạp hết — chỉ vẽ đúng dải
 * đang xem. Nạp hết là treo panel vài chục giây và ăn hết bộ nhớ.</p>
 */
public final class IconPicker {

    /** Số ảnh vẽ mỗi lần, đủ nhìn mà không làm chậm. */
    private static final int MOI_TRANG = 240;

    private IconPicker() {
    }

    /**
     * Mở hộp chọn.
     *
     * @param cha   thành phần để canh giữa hộp
     * @param idCu  id đang dùng, dùng làm điểm bắt đầu
     * @return id đã chọn, hoặc {@code null} nếu bấm Huỷ
     */
    public static Integer chon(java.awt.Component cha, int idCu) {
        final Integer[] ketQua = new Integer[1];
        Window owner = SwingUtilities.getWindowAncestor(cha);
        final JDialog dlg = owner instanceof java.awt.Frame
                ? new JDialog((java.awt.Frame) owner, "Chọn ảnh", true)
                : new JDialog((java.awt.Dialog) owner, "Chọn ảnh", true);

        final JPanel luoi = new JPanel(new GridLayout(0, 12, 4, 4));
        luoi.setBackground(Color.WHITE);
        final JLabel lblDem = new JLabel();
        lblDem.setForeground(new Color(110, 110, 110));

        // Bat dau tu vung quanh id dang dung de nhin thay no ngay.
        final int[] tuId = {Math.max(0, idCu - 24)};
        final JTextField fTu = new JTextField(String.valueOf(tuId[0]), 8);

        final Runnable ve = () -> {
            luoi.removeAll();
            int tu;
            try {
                tu = Math.max(0, Integer.parseInt(fTu.getText().trim()));
            } catch (NumberFormatException ex) {
                tu = 0;
            }
            tuId[0] = tu;
            int co = 0;
            for (int id = tu; id < tu + MOI_TRANG; id++) {
                final int idNay = id;
                ImageIcon anh = PlayerManagerPanel.iconOf(id);
                if (anh == null) {
                    continue;
                }
                co++;
                JButton b = new JButton(anh);
                b.setToolTipText("Ảnh " + id);
                b.setText(String.valueOf(id));
                b.setVerticalTextPosition(SwingConstants.BOTTOM);
                b.setHorizontalTextPosition(SwingConstants.CENTER);
                b.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                b.setBackground(id == idCu ? new Color(255, 240, 200) : Color.WHITE);
                b.setPreferredSize(new Dimension(56, 62));
                b.addActionListener(e -> {
                    ketQua[0] = idNay;
                    dlg.dispose();
                });
                luoi.add(b);
            }
            lblDem.setText("  Đang xem id " + tu + "–" + (tu + MOI_TRANG - 1)
                    + " — có ảnh: " + co + (co == 0
                        ? "   (dải này trống, thử dải khác)" : ""));
            luoi.revalidate();
            luoi.repaint();
        };

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.add(new JLabel("Từ id:"));
        top.add(fTu);
        JButton lui = new JButton("◀ 240 trước");
        lui.addActionListener(e -> {
            fTu.setText(String.valueOf(Math.max(0, tuId[0] - MOI_TRANG)));
        });
        JButton toi = new JButton("240 sau ▶");
        toi.addActionListener(e -> {
            fTu.setText(String.valueOf(tuId[0] + MOI_TRANG));
        });
        top.add(lui);
        top.add(toi);
        top.add(lblDem);

        fTu.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ve.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ve.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                ve.run();
            }
        });
        ve.run();

        JScrollPane sc = ServerGuiUtils.cuon(luoi);
        sc.getVerticalScrollBar().setUnitIncrement(24);
        sc.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton huy = new JButton("Huỷ");
        huy.addActionListener(e -> dlg.dispose());
        south.add(new JLabel("Bấm vào một ảnh là chọn.   "));
        south.add(huy);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(8, 8, 8, 8));
        wrap.add(top, BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.setSize(820, 620);
        dlg.setLocationRelativeTo(cha);
        dlg.setVisible(true);
        return ketQua[0];
    }

    /** Các id ảnh đang được kỹ năng dùng — gợi ý sẵn cho người chọn. */
    public static List<Integer> iconDangDung(String bang, String cot) {
        List<Integer> ra = new ArrayList<>();
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT DISTINCT `" + cot + "` c FROM `" + bang
                    + "` ORDER BY c");
            while (rs.next()) {
                ra.add(rs.getInt("c"));
            }
            rs.dispose();
        } catch (Exception ex) {
            nro.core.log.Logger.logException(IconPicker.class, ex,
                    "Lỗi đọc id ảnh đang dùng của " + bang);
        }
        return ra;
    }

    /** Ảnh có tệp thật hay không — để báo đỏ khi gõ id không có ảnh. */
    public static boolean coAnh(int id) {
        return new File("data/icon/x2/" + id + ".png").isFile();
    }
}
