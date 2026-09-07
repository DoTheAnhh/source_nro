package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import nro.gameplay.giftcode.GiftCodeManager;
import nro.repository.dao.GiftCodeDAO;

/**
 * Tab <b>Quản Lý GiftCode</b> — thêm, sửa, xoá, cho dùng lại mã quà tặng.
 *
 * <h2>Vì sao phải kiểm tra định dạng trước khi ghi</h2>
 *
 * <p>Ba cột {@code item}, {@code option}, {@code listIdPlayers} là chuỗi
 * JSON/CSV chứ không phải quan hệ. {@code GiftCodeManager.init()} bọc toàn bộ
 * việc nạp trong một {@code catch (Exception)} <b>rỗng</b>, nên chỉ cần một mã
 * sai định dạng là <i>cả bảng</i> giftcode im lặng không nạp được lúc khởi
 * động — không một dòng log nào. {@link GiftCodeDAO} chặn ngay ở đây.</p>
 *
 * <p>Sửa xong panel gọi {@code GiftCodeManager.reload()} để mã mới dùng được
 * ngay, không phải khởi động lại máy chủ.</p>
 */
public class GiftCodePanel extends JPanel {

    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color OK_GREEN = new Color(40, 160, 70);
    private static final Color WARN_RED = new Color(200, 60, 60);
    private static final Color GREY = new Color(120, 120, 120);

    private final JLabel lblStatus = new JLabel(" ");

    public GiftCodePanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Quản Lý GiftCode");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);
        add(buildGiftTab(), BorderLayout.CENTER);

        lblStatus.setForeground(GREY);
        lblStatus.setBorder(new EmptyBorder(4, 2, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);

        // Bang giftcode hien ten va anh cua vat pham di kem.
        LamMoi.nghe(this::loadGift, LamMoi.VAT_PHAM, LamMoi.ANH);
    }

    private final DefaultTableModel giftModel = new DefaultTableModel(
            new Object[]{"ID", "Mã", "Vật phẩm (JSON)", "Chỉ số (JSON)",
                "Tạo lúc", "Hết hạn", "Còn lượt", "Mỗi người", "Lượt đã dùng"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable giftTable = new JTable(giftModel);

    private JComponent buildGiftTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        giftTable.setRowHeight(24);
        giftTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        giftTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        giftTable.setAutoCreateRowSorter(true);
        int[] w = {45, 130, 240, 180, 130, 130, 70, 80, 90};
        for (int i = 0; i < w.length; i++) {
            giftTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        // Nháy đúp để sửa — cùng thao tác với các bảng khác trong panel.
        giftTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && giftTable.getSelectedRow() >= 0) {
                    giftDialog(false);
                }
            }
        });
        root.add(ServerGuiUtils.cuon(giftTable), BorderLayout.CENTER);

        JPanel act = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        act.setOpaque(false);
        act.add(button("Thêm mã", OK_GREEN, e -> giftDialog(true)));
        act.add(button("Sửa mã", ACCENT, e -> giftDialog(false)));
        act.add(button("Xoá mã", WARN_RED, e -> giftDelete()));
        act.add(button("Cho dùng lại", GREY, e -> giftResetUsers()));
        act.add(button("Xoá HẾT mã", new Color(150, 30, 30), e -> giftDeleteAll()));
        act.add(button("Tải lại", GREY, e -> loadGift()));
        JLabel tip = new JLabel("  (nháy đúp vào một dòng để sửa)");
        tip.setForeground(GREY);
        act.add(tip);
        root.add(act, BorderLayout.SOUTH);

        loadGift();
        return root;
    }

    private void loadGift() {
        giftModel.setRowCount(0);
        for (GiftCodeDAO.Row r : GiftCodeDAO.list()) {
            giftModel.addRow(new Object[]{r.id, r.code, r.item, r.option,
                r.taoLuc, r.hetHan, r.conLai, r.gioiHanMoiNguoi, r.daDung});
        }
        note(GREY, "Có " + giftModel.getRowCount() + " giftcode.");
    }

    private int giftSelectedModelRow() {
        int r = giftTable.getSelectedRow();
        return r < 0 ? -1 : giftTable.convertRowIndexToModel(r);
    }

    /**
     * Hộp thoại thêm/sửa giftcode.
     *
     * <p>Vật phẩm và chỉ số hiện thành <b>bảng có ảnh</b> chứ không phải một ô
     * JSON thô: nhìn chuỗi {@code [{"id":457,"quantity":10}]} thì không ai biết
     * 457 là món gì. Bảng tự tra tên và ảnh theo id, gõ tới đâu hiện tới đó.</p>
     *
     * <p>Lúc lưu, hai bảng được dựng lại thành JSON đúng định dạng mà
     * {@code GiftCodeManager.init()} đọc.</p>
     *
     * @param them {@code true} là thêm mới, {@code false} là sửa dòng đang chọn
     */
    private void giftDialog(boolean them) {
        int r = giftSelectedModelRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn mã nào để sửa.");
            return;
        }

        String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date());
        String sau30 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(System.currentTimeMillis() + 30L * 86_400_000L));

        JTextField fCode = new JTextField(them ? "" : str(r, 1), 18);
        JTextField fTao = new JTextField(them ? now : str(r, 4), 18);
        JTextField fHet = new JTextField(them ? sau30 : str(r, 5), 18);
        JTextField fLuot = new JTextField(them ? "100" : str(r, 6), 8);
        JTextField fMoiNguoi = new JTextField(them ? "1" : str(r, 7), 8);

        // ---------- bảng vật phẩm ----------
        DefaultTableModel mItem = new DefaultTableModel(
                new Object[]{"Ảnh", "ID", "Tên vật phẩm", "Loại", "Số lượng",
                    "Số chỉ số", "__json", "Khoá"}, 0) {
            @Override
            public boolean isCellEditable(int rr, int cc) {
                return cc == 1 || cc == 4 || cc == 7;   // ID, Số lượng, Khoá
            }

            @Override
            public Class<?> getColumnClass(int cc) {
                if (cc == 0) {
                    return javax.swing.ImageIcon.class;
                }
                return cc == 7 ? Boolean.class : Object.class;
            }
        };
        JTable tItem = new JTable(mItem);
        tItem.setRowHeight(32);
        int[] wi = {40, 60, 220, 130, 70, 70};
        for (int k = 0; k < wi.length; k++) {
            tItem.getColumnModel().getColumn(k).setPreferredWidth(wi[k]);
        }
        // Cot 6 giu JSON chi so cua rieng dong do — an di, chi de luu tru.
        tItem.removeColumn(tItem.getColumnModel().getColumn(6));
        // Sau khi bo cot an, "Khoa" la cot xem thu 6.
        tItem.getColumnModel().getColumn(6).setPreferredWidth(60);
        // Chi so hien theo dong DANG CHON nen van chi mot dong lam chuan; cho
        // chon nhieu chi de xoa mot lot.
        tItem.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Doi ID la tra lai ten/anh/loai ngay, khong phai bam gi them.
        mItem.addTableModelListener(e -> {
            if (e.getColumn() == 1) {
                SwingUtilities.invokeLater(() -> traCuuItem(mItem));
            }
        });

        // ---------- bảng chỉ số ----------
        DefaultTableModel mOpt = new DefaultTableModel(
                new Object[]{"ID chỉ số", "Giá trị", "Ý nghĩa"}, 0) {
            @Override
            public boolean isCellEditable(int rr, int cc) {
                return cc < 2;
            }
        };
        JTable tOpt = new JTable(mOpt);
        tOpt.setRowHeight(24);
        tOpt.getColumnModel().getColumn(0).setPreferredWidth(180);
        // Chon chi so tu danh sach thay vi go id: 273 chi so va id khong goi y
        // gi ve y nghia — go tay la cach chac chan nhat de dat nham.
        OptionPicker.install(tOpt, 0);
        tOpt.getColumnModel().getColumn(1).setPreferredWidth(80);
        tOpt.getColumnModel().getColumn(2).setPreferredWidth(320);
        mOpt.addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                SwingUtilities.invokeLater(() -> traCuuOpt(mOpt));
            }
        });

        if (!them) {
            // Chi so chung cua ma cu duoc gan lam chi so cua TUNG vat pham, de
            // ma cu mo ra van thay dung nhung gi no dang phat.
            doJsonVaoBangItem(mItem, str(r, 2), str(r, 3));
        } else {
            doJsonVaoBangItem(mItem, "[]", "[]");
        }

        // Chon dong vat pham nao thi bang duoi hien chi so cua dong do.
        final boolean[] dangDoiDong = {false};
        tItem.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) {
                return;
            }
            dangDoiDong[0] = true;
            int k = tItem.getSelectedRow();
            doJsonVaoBangOpt(mOpt, k < 0 ? "[]" : String.valueOf(mItem.getValueAt(k, 6)));
            traCuuOpt(mOpt);
            dangDoiDong[0] = false;
        });
        // Sua bang chi so thi ghi nguoc vao dong vat pham dang chon.
        mOpt.addTableModelListener(ev -> {
            if (dangDoiDong[0]) {
                return;
            }
            int k = tItem.getSelectedRow();
            if (k < 0) {
                return;
            }
            try {
                mItem.setValueAt(bangOptThanhJson(mOpt), k, 6);
                mItem.setValueAt(mOpt.getRowCount(), k, 5);
            } catch (Exception ignored) {
                // Dang go do, chua thanh so — bo qua, luc Luu se bao loi ro rang.
            }
        });

        // ---------- form trên ----------
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Mã:", fCode);
        addRow(form, c, 1, "Tạo lúc:", fTao);
        addRow(form, c, 2, "Hết hạn:", fHet);
        addRow(form, c, 3, "Tổng số lượt:", fLuot);
        addRow(form, c, 4, "Mỗi người nhập tối đa:", fMoiNguoi);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        JLabel hintTime = new JLabel("<html><span style='color:#777'>"
                + "Thời gian dạng <code>yyyy-MM-dd HH:mm:ss</code>. "
                + "<b>Tổng số lượt</b> là của cả máy chủ; <b>mỗi người nhập tối đa</b> "
                + "là của riêng từng nhân vật (1 = như cũ).<br>"
                + "Chọn một dòng ở bảng vật phẩm rồi sửa bảng chỉ số bên dưới — "
                + "<b>mỗi vật phẩm có bộ chỉ số riêng</b>. Cột <b>Khoá</b> là "
                + "vật phẩm không giao dịch được.</span></html>");
        form.add(hintTime, c);

        JPanel itemBox = new JPanel(new BorderLayout(0, 4));
        itemBox.setBorder(titled("Vật phẩm trong mã"));
        itemBox.add(ServerGuiUtils.cuon(tItem), BorderLayout.CENTER);
        JPanel bi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bi.add(button("Chọn vật phẩm...", OK_GREEN, e -> {
            // Chon tu hop tra cuu co anh + ten. Go id tran thi khong biet minh
            // dang chon dung mon hay khong cho toi luc phat ra roi.
            // Chon duoc NHIEU mon mot lot — them tung mon mot cho ma nhieu qua.
            for (int id : OptionPicker.chonNhieuVatPham(this, -1)) {
                mItem.addRow(new Object[]{null, String.valueOf(id), "", "", "1", 0,
                    "[]", Boolean.FALSE});
            }
            traCuuItem(mItem);
        }));
        bi.add(button("Thêm dòng trống", ACCENT,
                e -> mItem.addRow(new Object[]{null, "0", "", "", "1", 0,
                    "[]", Boolean.FALSE})));
        bi.add(button("Đổi vật phẩm", GREY, e -> {
            int k = tItem.getSelectedRow();
            if (k < 0) {
                return;
            }
            int id = OptionPicker.chonVatPham(this,
                    Integer.parseInt(OptionPicker.laySo(
                            String.valueOf(mItem.getValueAt(k, 1)) + "0")));
            if (id >= 0) {
                mItem.setValueAt(String.valueOf(id), k, 1);
                traCuuItem(mItem);
            }
        }));
        bi.add(button("Xoá dòng", WARN_RED, e -> {
            int[] nhieu = tItem.getSelectedRows();
            if (nhieu.length > 1) {
                // Xoa tu duoi len: xoa tu tren xuong thi cac chi so con lai tut
                // mot bac va se xoa nham dong.
                for (int q = nhieu.length - 1; q >= 0; q--) {
                    mItem.removeRow(nhieu[q]);
                }
                return;
            }
            int k = tItem.getSelectedRow();
            if (k >= 0) {
                mItem.removeRow(k);
            }
        }));
        itemBox.add(bi, BorderLayout.SOUTH);

        JPanel optBox = new JPanel(new BorderLayout(0, 4));
        optBox.setBorder(titled("Chỉ số RIÊNG của vật phẩm đang chọn ở bảng trên"));
        optBox.add(ServerGuiUtils.cuon(tOpt), BorderLayout.CENTER);
        JPanel bo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bo.add(button("Thêm dòng", ACCENT, e -> mOpt.addRow(new Object[]{"0", "0", ""})));
        bo.add(button("Xoá dòng", WARN_RED, e -> {
            int k = tOpt.getSelectedRow();
            if (k >= 0) {
                mOpt.removeRow(k);
            }
        }));
        bo.add(OptionPicker.oKhoaChoBang(mOpt, 3));
        optBox.add(bo, BorderLayout.SOUTH);

        javax.swing.JSplitPane sp = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, itemBox, optBox);
        sp.setResizeWeight(0.6);
        sp.setBorder(null);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                them ? "Thêm giftcode" : "Sửa giftcode", true);

        final int id = them ? -1 : num(r, 0);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button(them ? "Thêm" : "Lưu", OK_GREEN, e -> {
            int luot;
            int moiNguoi;
            try {
                luot = Integer.parseInt(fLuot.getText().trim());
                moiNguoi = Integer.parseInt(fMoiNguoi.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Tổng số lượt và giới hạn mỗi người phải là số nguyên.");
                return;
            }
            if (moiNguoi < 1) {
                JOptionPane.showMessageDialog(dlg,
                        "Giới hạn mỗi người phải từ 1 trở lên.");
                return;
            }
            String jsonItem;
            // Chi so chung khong con dung nua: moi vat pham tu mang chi so cua no.
            // Van ghi "[]" vao cot cu de GiftCodeManager doc khong bi null.
            String jsonOpt = "[]";
            try {
                // Ghi not chi so dang sua cua dong dang chon truoc khi dung JSON.
                int k = tItem.getSelectedRow();
                if (k >= 0) {
                    mItem.setValueAt(bangOptThanhJson(mOpt), k, 6);
                }
                jsonItem = bangItemThanhJson(mItem);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Dữ liệu sai",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String loi = them
                    ? GiftCodeDAO.them(fCode.getText().trim(), jsonItem, jsonOpt,
                            fTao.getText().trim(), fHet.getText().trim(), luot, moiNguoi)
                    : GiftCodeDAO.sua(id, fCode.getText().trim(), jsonItem, jsonOpt,
                            fTao.getText().trim(), fHet.getText().trim(), luot, moiNguoi);
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không lưu được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            applyGift(them ? "Đã thêm mã " + fCode.getText().trim()
                    : "Đã sửa mã " + fCode.getText().trim());
        }));

        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setBorder(new EmptyBorder(10, 10, 10, 10));
        wrap.add(form, BorderLayout.NORTH);
        wrap.add(sp, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.setSize(760, 620);
        dlg.setLocationRelativeTo(this);
        traCuuItem(mItem);
        traCuuOpt(mOpt);
        dlg.setVisible(true);
    }

    // ---------------------------------------------------------------- đọc/ghi JSON

    /**
     * Đổ mảng vật phẩm vào bảng.
     *
     * <p>Chỉ số của từng dòng lấy từ khoá {@code "options"} của chính vật phẩm
     * đó; vật phẩm nào không có thì dùng {@code optionChung} — nhờ vậy mã cũ
     * (chỉ có một bộ chỉ số dùng chung) mở ra vẫn thấy đúng thứ nó đang phát.</p>
     */
    static void doJsonVaoBangItem(DefaultTableModel m, String raw, String optionChung) {
        m.setRowCount(0);
        Object o = org.json.simple.JSONValue.parse(raw);
        if (!(o instanceof org.json.simple.JSONArray)) {
            return;
        }
        String chung = optionChung == null || optionChung.trim().isEmpty() ? "[]" : optionChung;
        for (Object p : (org.json.simple.JSONArray) o) {
            if (p instanceof org.json.simple.JSONObject) {
                org.json.simple.JSONObject j = (org.json.simple.JSONObject) p;
                Object rieng = j.get("options");
                // Dung lai chuoi theo thu tu CO DINH id truoc param. JSONObject la
                // HashMap nen toJSONString() tra ra thu tu ngau nhien; moi lan mo
                // roi luu lai se thay JSON "doi" du y nghia y het.
                String json = rieng == null ? chung : chuanHoaOption(rieng);
                // Tach chi so 30 ("Không thể giao dịch") ra thanh o tich "Khoa":
                // de no nam trong bang chi so thi vua co o tich vua co dong 30,
                // sua mot trong hai cho la lech nhau.
                boolean khoa = coKhoa(json);
                json = boKhoa(json);
                int dem = 0;
                Object parsed = org.json.simple.JSONValue.parse(json);
                if (parsed instanceof org.json.simple.JSONArray) {
                    dem = ((org.json.simple.JSONArray) parsed).size();
                }
                m.addRow(new Object[]{null, String.valueOf(j.get("id")), "", "",
                    String.valueOf(j.get("quantity")), dem, json, khoa});
            }
        }
    }

    /** Đổ chuỗi {@code [{"id":6,"param":25}]} vào bảng chỉ số. */
    static void doJsonVaoBangOpt(DefaultTableModel m, String raw) {
        m.setRowCount(0);
        Object o = org.json.simple.JSONValue.parse(raw);
        if (!(o instanceof org.json.simple.JSONArray)) {
            return;
        }
        for (Object p : (org.json.simple.JSONArray) o) {
            if (p instanceof org.json.simple.JSONObject) {
                org.json.simple.JSONObject j = (org.json.simple.JSONObject) p;
                m.addRow(new Object[]{String.valueOf(j.get("id")),
                    String.valueOf(j.get("param")), ""});
            }
        }
    }

    /** Id chỉ số "Không thể giao dịch" trong {@code item_option_template}. */
    private static final int OPT_KHOA = 30;

    /** {@code true} nếu mảng chỉ số có chỉ số khoá giao dịch. */
    static boolean coKhoa(String json) {
        Object o = org.json.simple.JSONValue.parse(json);
        if (!(o instanceof org.json.simple.JSONArray)) {
            return false;
        }
        for (Object p : (org.json.simple.JSONArray) o) {
            if (p instanceof org.json.simple.JSONObject
                    && String.valueOf(((org.json.simple.JSONObject) p).get("id"))
                            .trim().equals(String.valueOf(OPT_KHOA))) {
                return true;
            }
        }
        return false;
    }

    /** Mảng chỉ số sau khi bỏ hết chỉ số khoá giao dịch. */
    static String boKhoa(String json) {
        Object o = org.json.simple.JSONValue.parse(json);
        if (!(o instanceof org.json.simple.JSONArray)) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Object p : (org.json.simple.JSONArray) o) {
            if (!(p instanceof org.json.simple.JSONObject)) {
                continue;
            }
            org.json.simple.JSONObject j = (org.json.simple.JSONObject) p;
            if (String.valueOf(j.get("id")).trim().equals(String.valueOf(OPT_KHOA))) {
                continue;
            }
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(j.get("id"))
                    .append(",\"param\":").append(j.get("param")).append('}');
        }
        return sb.append(']').toString();
    }

    /** Mảng chỉ số kèm chỉ số khoá giao dịch. */
    static String themKhoa(String json) {
        String s = boKhoa(json);
        String than = s.substring(1, s.length() - 1);
        return than.isEmpty()
                ? "[{\"id\":" + OPT_KHOA + ",\"param\":0}]"
                : "[" + than + ",{\"id\":" + OPT_KHOA + ",\"param\":0}]";
    }

    /** Dựng lại mảng option theo thứ tự khoá cố định {@code id} rồi {@code param}. */
    private static String chuanHoaOption(Object mang) {
        if (!(mang instanceof org.json.simple.JSONArray)) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Object o : (org.json.simple.JSONArray) mang) {
            if (!(o instanceof org.json.simple.JSONObject)) {
                continue;
            }
            org.json.simple.JSONObject j = (org.json.simple.JSONObject) o;
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(j.get("id"))
                    .append(",\"param\":").append(j.get("param")).append('}');
        }
        return sb.append(']').toString();
    }

    /** Điền ảnh, tên và loại theo id đang gõ ở cột 1. */
    private static void traCuuItem(DefaultTableModel m) {
        for (int i = 0; i < m.getRowCount(); i++) {
            nro.entity.template.ItemTemplate t =
                    PlayerManagerPanel.templateOf(String.valueOf(m.getValueAt(i, 1)));
            m.setValueAt(t != null ? PlayerManagerPanel.iconOf(t.iconID) : null, i, 0);
            m.setValueAt(t != null ? t.name : "(không có id này)", i, 2);
            m.setValueAt(t != null ? PlayerManagerPanel.typeName(t.type) : "", i, 3);
        }
    }

    /** Điền ý nghĩa theo id chỉ số đang gõ ở cột 0. */
    private static void traCuuOpt(DefaultTableModel m) {
        for (int i = 0; i < m.getRowCount(); i++) {
            String ten = "(id phải là số)";
            try {
                int id = Integer.parseInt(String.valueOf(m.getValueAt(i, 0)).trim());
                if (nro.server.Manager.ITEM_OPTION_TEMPLATES != null
                        && id >= 0 && id < nro.server.Manager.ITEM_OPTION_TEMPLATES.size()) {
                    ten = nro.server.Manager.ITEM_OPTION_TEMPLATES.get(id).name;
                } else {
                    ten = "(không có id này)";
                }
            } catch (NumberFormatException ignored) {
                // giữ nguyên câu báo ở trên
            }
            m.setValueAt(ten, i, 2);
        }
    }

    /** Dựng lại JSON vật phẩm. Ném lỗi có câu giải thích nếu dữ liệu sai. */
    static String bangItemThanhJson(DefaultTableModel m) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < m.getRowCount(); i++) {
            int id;
            int sl;
            try {
                id = Integer.parseInt(String.valueOf(m.getValueAt(i, 1)).trim());
                sl = Integer.parseInt(String.valueOf(m.getValueAt(i, 4)).trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Vật phẩm dòng " + (i + 1) + ": ID và Số lượng phải là số nguyên.");
            }
            if (sl <= 0) {
                throw new IllegalArgumentException(
                        "Vật phẩm dòng " + (i + 1) + ": số lượng phải lớn hơn 0.");
            }
            if (PlayerManagerPanel.templateOf(String.valueOf(id)) == null) {
                throw new IllegalArgumentException(
                        "Vật phẩm dòng " + (i + 1) + ": không có mẫu vật phẩm id " + id + ".");
            }
            String opt = String.valueOf(m.getValueAt(i, 6));
            if (opt == null || opt.trim().isEmpty() || "null".equals(opt)) {
                opt = "[]";
            }
            // O tich "Khoa" la nguon duy nhat quyet dinh chi so 30: bo het 30
            // dang co roi them lai neu duoc tich. Lam vay thi mo ra luu lai bao
            // nhieu lan cung khong sinh ra hai dong 30.
            opt = boKhoa(opt);
            if (Boolean.TRUE.equals(m.getValueAt(i, 7))) {
                opt = themKhoa(opt);
            }
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(id)
                    .append(",\"quantity\":").append(sl)
                    .append(",\"options\":").append(opt).append('}');
        }
        return sb.append(']').toString();
    }

    /** Dựng lại JSON chỉ số. */
    static String bangOptThanhJson(DefaultTableModel m) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < m.getRowCount(); i++) {
            int id;
            int param;
            try {
                id = Integer.parseInt(String.valueOf(m.getValueAt(i, 0)).trim());
                param = Integer.parseInt(String.valueOf(m.getValueAt(i, 1)).trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Chỉ số dòng " + (i + 1) + ": phải là số nguyên.");
            }
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(id).append(",\"param\":").append(param).append('}');
        }
        return sb.append(']').toString();
    }

    /**
     * Xoá toàn bộ giftcode.
     *
     * <p>Cùng cách chặn với nút xoá hết nhân vật: hiện số thật rồi bắt gõ đúng
     * câu xác nhận, vì bấm nhầm là mất sạch mã không lấy lại được.</p>
     */
    private void giftDeleteAll() {
        int tong = giftModel.getRowCount();
        if (tong == 0) {
            note(WARN_RED, "Không có mã nào để xoá.");
            return;
        }
        int b1 = JOptionPane.showConfirmDialog(this,
                "XOÁ TOÀN BỘ " + tong + " GIFTCODE?\n\nKHÔNG HOÀN TÁC ĐƯỢC.",
                "CẢNH BÁO", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (b1 != JOptionPane.YES_OPTION) {
            return;
        }
        String go = JOptionPane.showInputDialog(this,
                "Gõ chính xác  XOA HET  để xác nhận xoá " + tong + " mã:",
                "Xác nhận lần cuối", JOptionPane.WARNING_MESSAGE);
        if (go == null || !"XOA HET".equals(go.trim())) {
            note(GREY, "Đã huỷ — không xoá gì cả.");
            return;
        }
        int n = 0;
        for (GiftCodeDAO.Row row : GiftCodeDAO.list()) {
            if (GiftCodeDAO.xoa(row.id)) {
                n++;
            }
        }
        applyGift("Đã xoá " + n + "/" + tong + " giftcode");
    }

    private void giftDelete() {
        int[] rs = giftTable.getSelectedRows();
        if (rs.length == 0) {
            note(WARN_RED, "Chưa chọn mã nào.");
            return;
        }
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        StringBuilder ten = new StringBuilder();
        for (int r : rs) {
            int idx = giftTable.convertRowIndexToModel(r);
            ids.add(num(idx, 0));
            if (ten.length() < 200) {
                ten.append(ten.length() == 0 ? "" : ", ").append(str(idx, 1));
            }
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá " + ids.size() + " giftcode?\n\n" + ten
                + (ten.length() >= 200 ? " …" : "") + "\n\nKhông khôi phục lại được.",
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        int xong = 0;
        for (int id : ids) {
            if (GiftCodeDAO.xoa(id)) {
                xong++;
            }
        }
        applyGift("Đã xoá " + xong + "/" + ids.size() + " mã.");
    }

    private void giftResetUsers() {
        int r = giftSelectedModelRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn mã nào.");
            return;
        }
        String code = str(r, 1);
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá danh sách người đã dùng mã \"" + code + "\"?\n\n"
                + "Những người đã nhận sẽ nhận lại được lần nữa.",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        if (!GiftCodeDAO.xoaLichSuDung(num(r, 0))) {
            note(WARN_RED, "Không xoá được.");
            return;
        }
        applyGift("Đã cho dùng lại mã " + code);
    }

    /**
     * Nạp lại bảng và <b>đẩy vào bộ nhớ máy chủ</b>.
     *
     * <p>{@code GiftCodeManager} giữ một bản sao trong RAM, đọc một lần lúc khởi
     * động. Không nạp lại thì mã vừa thêm chưa dùng được, mã vừa xoá vẫn dùng
     * được — đúng cho tới lần khởi động sau.</p>
     */
    private void applyGift(String msg) {
        loadGift();
        try {
            GiftCodeManager.gI().reload();
            note(OK_GREEN, msg + " — đã áp dụng vào máy chủ ngay.");
        } catch (Exception ex) {
            note(WARN_RED, msg + " (CSDL đã lưu) nhưng KHÔNG nạp lại được vào máy chủ: "
                    + ex.getMessage());
        }
    }

    private String str(int modelRow, int col) {
        Object v = giftModel.getValueAt(modelRow, col);
        return v == null ? "" : String.valueOf(v);
    }

    private int num(int modelRow, int col) {
        try {
            return Integer.parseInt(str(modelRow, col).trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static void addRow(JPanel form, GridBagConstraints c, int y,
                               String label, JTextField f) {
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(f, c);
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

    private static javax.swing.border.Border titled(String t) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215)), t);
    }
}
