package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import nro.repository.dao.AccountDAO;
import nro.repository.dao.PlayerDAO;
import nro.server.Client;
import nro.server.ServerManager;

/**
 * Tab <b>Quản Lý Tài Khoản</b> — xem, xoá một tài khoản, hoặc xoá sạch.
 *
 * <p>Khác tab Quản Lý Người Chơi: bên kia làm việc với <b>nhân vật</b>
 * ({@code player}), tab này với <b>tài khoản</b> ({@code account}). Một tài
 * khoản có thể có nhiều nhân vật, và tiền nạp nằm ở tài khoản chứ không ở nhân
 * vật.</p>
 *
 * <p>Xoá tài khoản thì xoá luôn nhân vật của nó: để lại thì nhân vật thành mồ
 * côi — vẫn hiện trong danh sách, vẫn chiếm tên, nhưng không ai đăng nhập vào
 * được nữa.</p>
 */
public class AccountPanel extends JPanel {

    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color OK_GREEN = new Color(40, 160, 70);
    private static final Color WARN_RED = new Color(200, 60, 60);
    private static final Color GREY = new Color(120, 120, 120);
    private static final Color DO_DAM = new Color(150, 30, 30);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Tên đăng nhập", "Quyền", "Nhân vật", "Số dư (VNĐ)",
                "Tổng nạp", "Số NV", "Đăng nhập lần cuối"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField fTim = new JTextField(18);
    private final JLabel lblStatus = new JLabel(" ");
    private final JLabel lblDem = new JLabel();
    private java.util.List<AccountDAO.Row> tatCa = new java.util.ArrayList<>();

    public AccountPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Quản Lý Tài Khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        table.setRowHeight(22);
        // Cho chon nhieu dong de xoa mot lot. Nut "Sua tai khoan" van chi lam
        // viec voi dong dau tien duoc chon — sua hang loat khong co nghia gi.
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setAutoCreateRowSorter(true);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    suaTaiKhoan();
                }
            }
        });
        int[] w = {55, 170, 80, 260, 120, 120, 60, 150};
        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel giua = new JPanel(new BorderLayout(0, 4));
        giua.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(new JLabel("Tìm (tên tài khoản hoặc tên nhân vật):"));
        top.add(fTim);
        top.add(button("Tải lại", GREY, e -> load()));
        lblDem.setForeground(GREY);
        top.add(lblDem);
        fTim.getDocument().addDocumentListener(new Simple(this::loc));
        giua.add(top, BorderLayout.NORTH);
        giua.add(ServerGuiUtils.cuon(table), BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout(0, 4));
        bot.setOpaque(false);
        JPanel act = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        act.setOpaque(false);
        act.add(button("Sửa tài khoản", ACCENT, e -> suaTaiKhoan()));
        act.add(button("Xoá tài khoản này", WARN_RED, e -> xoaMot()));
        act.add(button("Xoá HẾT tài khoản", DO_DAM, e -> xoaHet(false)));
        act.add(button("Xoá hết, GIỮ Admin", new Color(190, 110, 40), e -> xoaHet(true)));
        bot.add(act, BorderLayout.NORTH);
        lblStatus.setForeground(GREY);
        bot.add(lblStatus, BorderLayout.SOUTH);
        add(bot, BorderLayout.SOUTH);

        load();

        // Doi so du hay tong nap tu man khac thi bang tai khoan phai hien theo.
        LamMoi.nghe(LamMoi.NGUOI_CHOI, this::load);
    }

    private void load() {
        tatCa = AccountDAO.listAll();
        loc();
        note(GREY, "Có " + tatCa.size() + " tài khoản.");
    }

    private void loc() {
        String key = fTim.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (AccountDAO.Row r : tatCa) {
            // Tim theo ca ten dang nhap LAN ten nhan vat: nguoi bao loi thuong
            // chi biet ten nhan vat chu khong biet tai khoan.
            if (!key.isEmpty()
                    && !String.valueOf(r.username).toLowerCase().contains(key)
                    && !String.valueOf(r.tenNhanVat).toLowerCase().contains(key)
                    && !String.valueOf(r.id).equals(key)) {
                continue;
            }
            model.addRow(new Object[]{r.id, r.username, r.quyen,
                r.tenNhanVat == null ? "(chưa có nhân vật)" : r.tenNhanVat,
                PlayerManagerPanel.fmt(r.vnd), PlayerManagerPanel.fmt(r.tongNap),
                r.soNhanVat, r.lanCuoiDangNhap});
        }
        lblDem.setText("  " + model.getRowCount() + " / " + tatCa.size());
    }

    /**
     * Sửa quyền và tiền của một tài khoản ngay tại tab này.
     *
     * <p>Ghi <b>đè</b> chứ không cộng thêm, và <b>không</b> ghi vào lịch sử nạp:
     * đây là sửa số liệu sai, ghi vào lịch sử sẽ làm thống kê doanh thu lệch.
     * Muốn cộng tiền như một lần nạp thật thì dùng nút "Nạp ngay" ở tab Quản Lý
     * Người Chơi.</p>
     *
     * <p>Người đang online: {@code isFounder}/{@code isQuanTriVien} và tiền được
     * đồng bộ sang phiên ngay; cờ {@code is_admin} thì phiên không giữ nên phải
     * đăng nhập lại.</p>
     */
    private void suaTaiKhoan() {
        int r = table.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn tài khoản nào.");
            return;
        }
        int idx = table.convertRowIndexToModel(r);
        final int accId = intOf(model.getValueAt(idx, 0));
        String user = String.valueOf(model.getValueAt(idx, 1));

        AccountDAO.Money m = AccountDAO.load(accId);
        if (m == null) {
            note(WARN_RED, "Không đọc được tài khoản này.");
            return;
        }
        AccountDAO.Quyen q = AccountDAO.loadQuyen(accId);
        String quyenCu = PlayerDAO.tenQuyen(q.admin, q.founder, q.quanTriVien);

        javax.swing.JComboBox<String> cbQuyen =
                new javax.swing.JComboBox<>(PlayerDAO.CAC_QUYEN);
        cbQuyen.setSelectedItem(quyenCu);
        JTextField fVnd = new JTextField(String.valueOf(m.vnd), 14);
        JTextField fTongNap = new JTextField(String.valueOf(m.tongNap), 14);
        JTextField fCoin = new JTextField(String.valueOf(m.coin), 14);

        JPanel p = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));
        p.add(new JLabel("Tài khoản:"));
        p.add(new JLabel(user + "  (id " + accId + ")"));
        p.add(new JLabel("Quyền:"));
        p.add(cbQuyen);
        p.add(new JLabel("Số dư (VNĐ):"));
        p.add(fVnd);
        p.add(new JLabel("Tổng đã nạp:"));
        p.add(fTongNap);
        p.add(new JLabel("Coin:"));
        p.add(fCoin);

        int ok = JOptionPane.showConfirmDialog(this, p, "Sửa tài khoản",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        long vnd;
        long tongNap;
        long coin;
        try {
            vnd = docSo(fVnd);
            tongNap = docSo(fTongNap);
            coin = docSo(fCoin);
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Các ô tiền phải là số nguyên.");
            return;
        }
        if (vnd < 0 || tongNap < 0 || coin < 0) {
            note(WARN_RED, "Tiền không được âm.");
            return;
        }

        String quyenMoi = String.valueOf(cbQuyen.getSelectedItem());
        AccountDAO.Quyen qm = new AccountDAO.Quyen();
        qm.admin = PlayerDAO.QUYEN_ADMIN.equals(quyenMoi);
        qm.founder = PlayerDAO.QUYEN_ADMIN.equals(quyenMoi);
        qm.quanTriVien = PlayerDAO.QUYEN_COLAB.equals(quyenMoi);

        boolean a = AccountDAO.saveQuyen(accId, qm);
        // Giu nguyen thoi vang: tab nay khong hien cot do nua nen khong duoc
        // im lang dat lai ve 0.
        boolean b = AccountDAO.setTien(accId, vnd, tongNap, coin,
                AccountDAO.thoiVangOf(accId));
        dongBoPhien(accId, qm, vnd, tongNap, coin);
        load();
        note(a && b ? OK_GREEN : WARN_RED, a && b
                ? "Da sua \"" + user + "\": quyen " + quyenCu + " -> " + quyenMoi
                  + ", so du " + PlayerManagerPanel.fmt(vnd)
                  + ", tong nap " + PlayerManagerPanel.fmt(tongNap) + "."
                : "Luu khong tron ven - xem log may chu.");
    }

    /**
     * Đồng bộ sang phiên của người đang online.
     *
     * <p><b>Bắt buộc.</b> Các đường khác ghi tiền xuống CSDL <i>từ phiên</i>, nên
     * chỉ sửa CSDL mà không sửa phiên thì lần lưu sau ghi đè ngược lại số cũ.</p>
     */
    private void dongBoPhien(int accId, AccountDAO.Quyen q, long vnd, long tongNap,
                             long coin) {
        if (!ServerManager.isRunning) {
            return;
        }
        try {
            for (nro.entity.player.Player pl : Client.gI().getPlayersSnapshot()) {
                if (pl == null || pl.getSession() == null
                        || pl.getSession().userId != accId) {
                    continue;
                }
                nro.net.session.MySession ss = pl.getSession();
                ss.isFounder = q.founder;
                ss.isQuanTriVien = q.quanTriVien;
                ss.vnd = (int) Math.min(vnd, Integer.MAX_VALUE);
                ss.tongnap = (int) Math.min(tongNap, Integer.MAX_VALUE);
                ss.coin = (int) Math.min(coin, Integer.MAX_VALUE);
                try {
                    nro.service.Service.gI().sendMoney(pl);
                } catch (Exception ignored) {
                    // Nguoi choi vua thoat -> du lieu trong CSDL van dung.
                }
            }
        } catch (Exception ignored) {
            // Khong doc duoc danh sach online -> bo qua, CSDL da dung.
        }
    }

    /** Đọc số từ ô nhập, bỏ dấu ngăn nghìn. */
    private static long docSo(JTextField f) {
        return Long.parseLong(f.getText().trim().replace(".", "").replace(",", ""));
    }

    private void xoaMot() {
        int[] rs = table.getSelectedRows();
        if (rs.length == 0) {
            note(WARN_RED, "Chưa chọn tài khoản nào.");
            return;
        }
        // Doc het thong tin TRUOC khi xoa dong nao: xoa xong thi load() dung lai
        // bang va cac chi so dong con lai deu lech.
        java.util.List<int[]> ds = new java.util.ArrayList<>();
        StringBuilder ten = new StringBuilder();
        int tongNv = 0;
        for (int r : rs) {
            int idx = table.convertRowIndexToModel(r);
            int id = intOf(model.getValueAt(idx, 0));
            int soNv = intOf(model.getValueAt(idx, 6));
            ds.add(new int[]{id, soNv});
            tongNv += soNv;
            if (ten.length() < 200) {
                ten.append(ten.length() == 0 ? "" : ", ")
                        .append(model.getValueAt(idx, 1));
            }
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá " + ds.size() + " tài khoản?\n\n" + ten
                + (ten.length() >= 200 ? " …" : "") + "\n\n"
                + "Sẽ xoá luôn " + tongNv + " nhân vật và lịch sử nạp.\n"
                + "Không hoàn tác được.",
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        int xong = 0;
        for (int[] d : ds) {
            if (AccountDAO.xoaTaiKhoan(d[0])) {
                xong++;
            }
        }
        load();
        note(xong == ds.size() ? OK_GREEN : WARN_RED,
                "Đã xoá " + xong + "/" + ds.size() + " tài khoản, " + tongNv + " nhân vật."
                + (xong == ds.size() ? "" : " Xem log máy chủ."));
    }

    /**
     * Xoá toàn bộ tài khoản.
     *
     * <p><b>Ba lớp chặn</b>, cùng cách với nút xoá hết nhân vật: từ chối khi còn
     * người online, hiện con số thật, và bắt gõ đúng câu xác nhận.</p>
     *
     * <p>Từ chối khi còn người online vì phiên của họ vẫn giữ nhân vật trong bộ
     * nhớ, và lần lưu tự động kế tiếp sẽ ghi trở lại một nhân vật thuộc tài
     * khoản vừa bị xoá.</p>
     *
     * @param giuAdmin giữ lại các tài khoản quyền Admin
     */
    private void xoaHet(boolean giuAdmin) {
        int dangOnline = 0;
        if (ServerManager.isRunning) {
            try {
                dangOnline = Client.gI().getPlayersSnapshot().size();
            } catch (Exception ignored) {
                // Không đọc được thì coi như không có ai; hai lớp chặn sau vẫn còn.
            }
        }
        if (dangOnline > 0) {
            JOptionPane.showMessageDialog(this,
                    "Đang có " + dangOnline + " người chơi online.\n\n"
                    + "Hãy kick hết rồi mới xoá: phiên của người đang chơi vẫn giữ "
                    + "nhân vật trong bộ nhớ, và lần lưu tự động kế tiếp sẽ ghi họ "
                    + "trở lại một tài khoản vừa bị xoá.",
                    "Chưa xoá được", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int tong = AccountDAO.demTaiKhoan();
        if (tong < 0) {
            note(WARN_RED, "Không đọc được số tài khoản — huỷ thao tác.");
            return;
        }
        if (tong == 0) {
            note(WARN_RED, "Không có tài khoản nào để xoá.");
            return;
        }
        int giuLai = 0;
        if (giuAdmin) {
            for (AccountDAO.Row r : tatCa) {
                if (PlayerDAO.QUYEN_ADMIN.equals(r.quyen)) {
                    giuLai++;
                }
            }
        }

        int b1 = JOptionPane.showConfirmDialog(this,
                (giuAdmin
                        ? "XOÁ " + (tong - giuLai) + " TÀI KHOẢN, giữ lại " + giuLai + " Admin?"
                        : "XOÁ TOÀN BỘ " + tong + " TÀI KHOẢN?")
                + "\n\nKHÔNG HOÀN TÁC ĐƯỢC.\n\n"
                + "Xoá luôn mọi nhân vật và lịch sử nạp của các tài khoản đó.",
                "CẢNH BÁO", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (b1 != JOptionPane.YES_OPTION) {
            return;
        }
        String go = JOptionPane.showInputDialog(this,
                "Gõ chính xác  XOA HET  để xác nhận:",
                "Xác nhận lần cuối", JOptionPane.WARNING_MESSAGE);
        if (go == null || !"XOA HET".equals(go.trim())) {
            note(GREY, "Đã huỷ — không xoá gì cả.");
            return;
        }

        int n = AccountDAO.xoaHetTaiKhoan(giuAdmin);
        if (n < 0) {
            note(WARN_RED, "XOÁ THẤT BẠI — xem log máy chủ.");
            return;
        }
        load();
        note(WARN_RED, "Đã xoá " + n + " tài khoản"
                + (giuAdmin ? " (giữ lại " + giuLai + " Admin)." : " và toàn bộ nhân vật."));
    }

    private static int intOf(Object o) {
        try {
            return Integer.parseInt(String.valueOf(o).replace(".", "").trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void note(Color color, String text) {
        lblStatus.setForeground(color);
        lblStatus.setText(text);
    }

    private static JButton button(String text, Color bg, java.awt.event.ActionListener a) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.addActionListener(a);
        return b;
    }

    /** {@link DocumentListener} chỉ cần một hành động cho cả ba loại thay đổi. */
    private static final class Simple implements DocumentListener {

        private final Runnable r;

        Simple(Runnable r) {
            this.r = r;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            r.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            r.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            r.run();
        }
    }
}
