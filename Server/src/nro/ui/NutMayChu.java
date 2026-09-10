package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import nro.repository.dao.ConfigDAO;

/**
 * Hai nút điều khiển máy chủ, đặt ở <b>thanh bên trái</b>.
 *
 * <h2>Vì sao không để trong tab Quy ước nữa</h2>
 *
 * <p>Bật bảo trì và khởi động lại máy chủ là hai việc <b>khác hẳn</b> mọi thứ
 * khác trong tab ấy: các nút kia lưu một con số, hai nút này ngắt kết nối của
 * người đang chơi. Chúng nằm lẫn giữa "Lưu" và "Đọc lại" ở cuối một biểu mẫu
 * dài, phải cuộn xuống mới thấy — vừa khó tìm lúc cần gấp, vừa dễ bấm nhầm lúc
 * đang sửa con số khác.</p>
 *
 * <p>Ở thanh bên thì lúc nào cũng thấy, ở mọi tab, và tách hẳn khỏi biểu mẫu.</p>
 */
public class NutMayChu extends JPanel {

    private final JButton nutBaoTri = new JButton();
    private final JLabel trangThai = new JLabel(" ");

    public NutMayChu() {
        setLayout(new BorderLayout(0, 6));
        setBackground(UiTheme.SIDEBAR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, UiTheme.SIDEBAR_LINE),
                new EmptyBorder(10, 12, 10, 12)));

        JLabel tieuDe = new JLabel("MÁY CHỦ");
        tieuDe.setForeground(UiTheme.TEXT_MUTED_DARK);
        tieuDe.setFont(tieuDe.getFont().deriveFont(java.awt.Font.BOLD, 11f));
        add(tieuDe, BorderLayout.NORTH);

        JPanel giua = new JPanel(new GridLayout(0, 1, 0, 6));
        giua.setOpaque(false);
        nutBaoTri.addActionListener(e -> doiBaoTri());
        trangThaiNut(nutBaoTri);
        giua.add(nutBaoTri);

        JButton nutKhoiDong = new JButton("Khởi động lại máy chủ");
        trangThaiNut(nutKhoiDong);
        nutKhoiDong.setBackground(UiTheme.DANGER);
        nutKhoiDong.addActionListener(e -> khoiDongLaiMayChu());
        giua.add(nutKhoiDong);
        add(giua, BorderLayout.CENTER);

        trangThai.setForeground(UiTheme.TEXT_MUTED_DARK);
        trangThai.setFont(trangThai.getFont().deriveFont(11f));
        add(trangThai, BorderLayout.SOUTH);

        capNhatNutBaoTri();
    }

    private static void trangThaiNut(JButton b) {
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(8, 10, 8, 10));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(0, 34));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /**
     * Vẽ lại nhãn và màu nút bảo trì theo trạng thái đang lưu.
     *
     * <p>Ghi thẳng trạng thái lên nhãn chứ không để một nút trung tính như
     * "Bảo trì": nhìn nút là biết máy chủ đang ở chế độ nào, khỏi phải bấm thử
     * rồi đọc thông báo mới biết vừa đổi sang gì.</p>
     */
    private void capNhatNutBaoTri() {
        boolean dangBaoTri = ConfigDAO.on(ConfigDAO.BAO_TRI);
        nutBaoTri.setText(dangBaoTri ? "⛔ ĐANG BẢO TRÌ — mở lại" : "Bật bảo trì");
        nutBaoTri.setBackground(dangBaoTri ? UiTheme.DANGER : UiTheme.SIDEBAR_ACTIVE);
    }

    private void bao(Color mau, String chu) {
        trangThai.setForeground(mau);
        trangThai.setText("<html><body style='width:210px'>" + chu + "</body></html>");
    }

    /**
     * Bật hoặc tắt chế độ bảo trì.
     *
     * <p>Bật thì <b>đá luôn mọi người đang chơi</b>, chỉ chừa tài khoản quản
     * trị, chứ không chỉ chặn đăng nhập mới. Trước đây người đã ở trong game thì
     * ở nguyên đó — nên bật bảo trì xong sân vẫn đông, và mọi lý do để bảo trì
     * đều diễn ra ngay giữa lúc người chơi vẫn đang chơi.</p>
     *
     * <p>Việc đá đi qua đường lưu dữ liệu bình thường, nên không ai mất đồ.</p>
     */
    private void doiBaoTri() {
        boolean dangBaoTri = ConfigDAO.on(ConfigDAO.BAO_TRI);
        String hoi = dangBaoTri
                ? String.join(System.lineSeparator(),
                        "Tắt chế độ bảo trì?",
                        "",
                        "Mọi người sẽ đăng nhập lại được như bình thường.")
                : String.join(System.lineSeparator(),
                        "Bật chế độ bảo trì?",
                        "",
                        "Chỉ tài khoản quản trị đăng nhập được, và MỌI NGƯỜI",
                        "ĐANG CHƠI sẽ bị ngắt kết nối ngay — trừ quản trị viên.",
                        "",
                        "Ai bị đá đều được lưu dữ liệu trước, không mất đồ.");
        if (JOptionPane.showConfirmDialog(this, hoi, "Chế độ bảo trì",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        if (!ConfigDAO.set(ConfigDAO.BAO_TRI, dangBaoTri ? "0" : "1")) {
            bao(UiTheme.DANGER, "Không lưu được chế độ bảo trì — xem log.");
            return;
        }
        ConfigDAO.reload();
        capNhatNutBaoTri();
        SystemPanel.dongBoQuyUoc();
        if (dangBaoTri) {
            bao(UiTheme.OK, "Đã tắt bảo trì — mọi người vào được.");
            return;
        }
        // Da SAU khi da ghi co: ai dang tren duong dang nhap trong luc dang da
        // cung bi cong dang nhap tu choi, khong lot lai vao duoc.
        //
        // Chay o luong rieng: moi nguoi bi da la mot lan ghi CSDL, dong nguoi
        // thi mat vai giay — lam ngay trong luong giao dien la ca panel dong
        // bang, nhin nhu treo.
        bao(UiTheme.DANGER, "ĐANG BẢO TRÌ — đang ngắt kết nối người chơi…");
        new Thread(() -> {
            int da = nro.server.Client.gI().daNguoiThuong();
            SwingUtilities.invokeLater(() -> bao(UiTheme.DANGER,
                    "ĐANG BẢO TRÌ — đã ngắt " + da + " người chơi."));
        }, "Da nguoi khi bao tri").start();
    }

    /**
     * Tắt máy chủ rồi tự bật lại.
     *
     * <p>Người chơi đang online sẽ <b>bị đá ra</b> — dữ liệu của họ được lưu
     * trước khi thoát, nhưng vẫn phải hỏi lại vì đây là việc không rút lại
     * được.</p>
     *
     * <p>Chỉ <b>bật lại</b> chứ không dựng lại mã nguồn. Sửa mã xong thì vẫn
     * phải build rồi mới chạy — nút này không thay được việc đó.</p>
     */
    private void khoiDongLaiMayChu() {
        int soNguoi = 0;
        try {
            soNguoi = nro.server.Client.gI().getPlayers().size();
        } catch (Exception boQua) {
            // Khong dem duoc thi van cho khoi dong lai.
        }
        String hoi = "Tắt máy chủ rồi bật lại ngay?";
        if (soNguoi > 0) {
            hoi += "\n\nĐang có " + soNguoi + " người chơi online — họ sẽ bị"
                    + " đá ra.\nDữ liệu được lưu trước khi tắt.";
        }
        hoi += "\n\nMáy chủ lên lại sau khoảng 5–10 giây."
                + "\nLưu ý: chỉ bật lại, KHÔNG dựng lại mã nguồn.";
        if (JOptionPane.showConfirmDialog(this, hoi, "Khởi động lại máy chủ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        bao(UiTheme.DANGER, "Đang tắt máy chủ — sẽ tự bật lại sau vài giây…");
        // Chay o luong khac: khoiDongLai() ket thuc bang System.exit(0), goi
        // thang tren luong giao dien thi cua so treo cung truoc khi ve xong.
        new Thread(() -> {
            try {
                Thread.sleep(400);
            } catch (InterruptedException boQua) {
                Thread.currentThread().interrupt();
            }
            nro.server.ServerManager.gI().khoiDongLai();
        }, "KhoiDongLaiMayChu").start();
    }
}
