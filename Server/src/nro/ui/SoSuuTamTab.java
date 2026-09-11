package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import nro.entity.card.OptionCard;
import nro.entity.card.RadarCard;
import nro.entity.template.ItemTemplate;
import nro.entity.template.MobTemplate;
import nro.repository.dao.AuraDAO;
import nro.repository.dao.SoSuuTamDAO;
import nro.server.Manager;
import nro.service.card.RadarService;
import nro.service.item.ItemService;

/**
 * Tab "Sổ sưu tầm": thêm, sửa, xoá các thẻ của sổ.
 *
 * <p>Mỗi thẻ gắn với một <b>vật phẩm</b> — người chơi dùng vật phẩm đó là góp
 * vào sổ. Món đầu tiên mở khoá thẻ; đủ số lượng của từng cấp thì lên cấp. Mỗi
 * cấp có dòng chỉ số riêng, và thẻ có thể mang một hào quang.</p>
 *
 * <p>Lưu là có hiệu lực ngay: máy chủ đọc lại bảng, người đang online có thẻ
 * đó được tính lại chỉ số và hào quang.</p>
 */
final class SoSuuTamTab extends JPanel {

    private static final Color XANH = new Color(0, 120, 215);
    private static final Color XANH_LA = new Color(40, 160, 70);
    private static final Color DO = new Color(200, 60, 60);
    private static final Color XAM = new Color(120, 120, 120);

    private static final String[] TEN_HANG = {
        "Hạng 1", "Hạng 2", "Hạng 3", "Hạng 4", "Hạng 5", "Hạng 6", "Hạng 7"};
    private static final String[] TEN_KIEU = {
        "Hình con quái", "Hình nhân vật (head / body / leg / bag)"};

    private static final int T_THU_TU = 0;
    private static final int T_ID = 1;
    private static final int T_CAP = 4;
    private static final int T_DONG = 7;

    private final DefaultTableModel mThe = new DefaultTableModel(new Object[]{
        "Thứ tự", "Id", "Tên thẻ", "Hạng", "Cấp tối đa", "Số lượng lên cấp",
        "Hào quang", "Dòng chỉ số"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == T_THU_TU || c == T_ID || c == T_CAP || c == T_DONG
                    ? Integer.class : String.class;
        }
    };
    private final JTable bThe = new JTable(mThe);
    /** Thẻ đang hiện ở bảng, đúng thứ tự dòng. */
    private final List<RadarCard> dangHien = new ArrayList<>();

    private static final int O_CHI_SO = 0;
    private static final int O_GIA_TRI = 1;
    private static final int O_CAP = 2;

    private final DefaultTableModel mOpt = new DefaultTableModel(new Object[]{
        "Chỉ số", "Giá trị", "Có hiệu lực từ cấp"}, 0) {
        @Override
        public Class<?> getColumnClass(int c) {
            return c == O_CHI_SO ? Object.class : Integer.class;
        }
    };
    private final JTable bOpt = new JTable(mOpt);

    private final JTextField oId = new JTextField(7);
    private final JLabel lblVatPham = new JLabel(" ");
    private final JTextField oTen = new JTextField(22);
    private final JTextField oIcon = new JTextField(7);
    private final JComboBox<String> cbHang = new JComboBox<>(TEN_HANG);
    private final JTextField oThuTu = new JTextField(7);
    private final JComboBox<String> cbKieu = new JComboBox<>(TEN_KIEU);
    private final JTextField oQuai = new JTextField(7);
    private final JLabel lblQuai = new JLabel(" ");
    private final JTextField oHead = new JTextField(5);
    private final JTextField oBody = new JTextField(5);
    private final JTextField oLeg = new JTextField(5);
    private final JTextField oBag = new JTextField(5);
    private final JTextField oCanMoiCap = new JTextField(22);
    private final JLabel lblCap = new JLabel(" ");
    private final JComboBox<Object> cbAura = new JComboBox<>();
    private final JTextField oAuraTuCap = new JTextField(5);
    private final JTextField oYeuCauThe = new JTextField(7);
    private final JTextField oYeuCauCap = new JTextField(5);
    private final JTextArea oMoTa = new JTextArea(3, 26);
    private final JLabel lblDangSua = new JLabel(" ");
    private final JLabel trangThai = new JLabel(" ");

    /** Id của thẻ đang sửa; -1 là đang tạo thẻ mới. */
    private int idDangSua = -1;
    /** Đang đổ dữ liệu vào bảng — bỏ qua sự kiện chọn dòng sinh ra giữa chừng. */
    private boolean dangDoDuLieu;

    SoSuuTamTab() {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(new JLabel("<html><b>Sổ sưu tầm</b> — mỗi thẻ gắn với một <b>vật phẩm</b>: người "
                + "chơi dùng vật phẩm đó là góp vào sổ (thẻ loại 33, hoặc bất kỳ vật phẩm "
                + "nào có trong bảng này — góp một lượt cả chồng).<br>"
                + "Món đầu tiên <b>mở khoá</b> thẻ; đủ <b>số lượng lên cấp</b> thì lên Lv.1, "
                + "Lv.2... Người chơi mở sổ ở <b>Chức năng → Hệ thống → Sổ sưu tầm</b> và "
                + "bật được <b>một</b> thẻ.<br>"
                + "Dòng chỉ số <b>cấp 0</b>: có ngay khi đã sưu tầm, không cần bật. Dòng "
                + "<b>cấp N</b>: có khi thẻ <b>đang bật</b> và đạt Lv.N trở lên (cộng dồn). "
                + "Lưu là áp dụng ngay.</html>"), BorderLayout.NORTH);

        caiDatBangThe();
        JPanel trai = new JPanel(new BorderLayout(0, 4));
        trai.setOpaque(false);
        trai.add(ServerGuiUtils.cuon(bThe), BorderLayout.CENTER);
        JPanel nutTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nutTrai.setOpaque(false);
        nutTrai.add(nut("Thêm thẻ mới", XANH, this::moi));
        nutTrai.add(nut("Xoá thẻ", DO, this::xoa));
        nutTrai.add(nut("Tải lại từ CSDL", XAM, this::taiLaiTuCsdl));
        trai.add(nutTrai, BorderLayout.SOUTH);

        JPanel phai = new JPanel(new BorderLayout(0, 6));
        phai.setOpaque(false);
        phai.add(formThe(), BorderLayout.NORTH);
        phai.add(khungChiSo(), BorderLayout.CENTER);
        JPanel nutPhai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nutPhai.setOpaque(false);
        nutPhai.add(nut("Lưu thẻ", XANH_LA, this::luu));
        nutPhai.add(lblDangSua);
        phai.add(nutPhai, BorderLayout.SOUTH);

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trai,
                ServerGuiUtils.cuon(phai));
        chia.setResizeWeight(0.42);
        chia.setBorder(null);
        add(chia, BorderLayout.CENTER);

        trangThai.setBorder(new EmptyBorder(2, 4, 0, 0));
        add(trangThai, BorderLayout.SOUTH);
        moi();
    }

    // =====================================================================
    //  Dựng giao diện
    // =====================================================================

    private void caiDatBangThe() {
        bThe.setRowHeight(24);
        bThe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bThe.getTableHeader().setReorderingAllowed(false);
        int[] w = {55, 55, 170, 60, 70, 130, 140, 80};
        for (int i = 0; i < w.length; i++) {
            bThe.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        bThe.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || dangDoDuLieu) {
                return;
            }
            int r = bThe.getSelectedRow();
            if (r >= 0 && r < dangHien.size()) {
                doVaoForm(dangHien.get(r));
            }
        });
    }

    private JComponent formThe() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Thẻ"), new EmptyBorder(2, 6, 4, 6)));
        int y = 0;
        dong(p, y++, "Id vật phẩm", hang(oId, nut("Chọn vật phẩm…", XAM, this::chonVatPham),
                lblVatPham));
        dong(p, y++, "Tên thẻ", hang(oTen));
        dong(p, y++, "Icon", hang(oIcon, nut("Chọn icon…", XAM, this::chonIcon)));
        dong(p, y++, "Hạng (màu khung)", hang(cbHang));
        dong(p, y++, "Thứ tự trong sổ", hang(oThuTu, ghiChu("số nhỏ đứng trước")));
        dong(p, y++, "Hình trong sổ", hang(cbKieu));
        dong(p, y++, "Id quái", hang(oQuai, lblQuai));
        dong(p, y++, "Head / Body / Leg / Bag", hang(oHead, oBody, oLeg, oBag));
        dong(p, y++, "Số lượng lên cấp", hang(oCanMoiCap, lblCap));
        dong(p, y++, "", hang(ghiChu("vd 10,20,30: mở khoá xong cần 10 lên Lv.1, thêm 20 lên"
                + " Lv.2, thêm 30 lên Lv.3 — tối đa Lv.3")));
        dong(p, y++, "Hào quang", hang(cbAura));
        dong(p, y++, "Hào quang từ cấp", hang(oAuraTuCap,
                ghiChu("0 = bật thẻ là có hào quang")));
        dong(p, y++, "Cần thẻ khác trước", hang(oYeuCauThe, new JLabel("đạt Lv."), oYeuCauCap,
                ghiChu("id thẻ; -1 = không cần")));
        oMoTa.setLineWrap(true);
        oMoTa.setWrapStyleWord(true);
        dong(p, y++, "Mô tả", new JScrollPane(oMoTa));

        cbKieu.addActionListener(e -> capNhatKieu());
        nghe(oId, this::capNhatTenVatPham);
        nghe(oQuai, this::capNhatTenQuai);
        nghe(oCanMoiCap, this::capNhatCap);
        return p;
    }

    private JComponent khungChiSo() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Chỉ số theo cấp"),
                new EmptyBorder(2, 6, 4, 6)));
        bOpt.setRowHeight(24);
        bOpt.getTableHeader().setReorderingAllowed(false);
        bOpt.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        OptionPicker.install(bOpt, O_CHI_SO);
        bOpt.getColumnModel().getColumn(O_CHI_SO).setPreferredWidth(280);
        bOpt.getColumnModel().getColumn(O_GIA_TRI).setPreferredWidth(90);
        bOpt.getColumnModel().getColumn(O_CAP).setPreferredWidth(170);
        DefaultTableCellRenderer cap = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                int n = soCua(v, 0);
                setText(n <= 0 ? "0 — Mở khoá (có ngay)" : n + " — Lv." + n + " (khi bật)");
            }
        };
        bOpt.getColumnModel().getColumn(O_CAP).setCellRenderer(cap);
        JScrollPane cuon = new JScrollPane(bOpt);
        cuon.setPreferredSize(new Dimension(480, 210));
        p.add(cuon, BorderLayout.CENTER);

        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        hangNut.setOpaque(false);
        hangNut.add(nut("Thêm dòng mở khoá", XANH, () -> themDong(0)));
        hangNut.add(nut("Thêm dòng cấp kế", XANH, this::themDongCapKe));
        hangNut.add(nut("Xoá dòng", DO, this::xoaDong));
        hangNut.add(ghiChu("sửa ô \"Có hiệu lực từ cấp\" bằng số: 0 là mở khoá"));
        p.add(hangNut, BorderLayout.SOUTH);
        return p;
    }

    private static void dong(JPanel p, int y, String nhan, Component o) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0;
        p.add(new JLabel(nhan), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(o, c);
    }

    private static JPanel hang(Component... cs) {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        h.setOpaque(false);
        for (Component c : cs) {
            h.add(c);
        }
        return h;
    }

    private static JLabel ghiChu(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(XAM);
        return l;
    }

    private static JButton nut(String chu, Color mau, Runnable viec) {
        JButton b = ServerGuiUtils.createStyledButton(chu, mau, Color.WHITE);
        b.addActionListener(e -> viec.run());
        return b;
    }

    private static void nghe(JTextField o, Runnable viec) {
        o.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                viec.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                viec.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                viec.run();
            }
        });
    }

    // =====================================================================
    //  Nạp và hiển thị
    // =====================================================================

    /** Gọi khi mở tab: hiện đúng danh sách máy chủ đang dùng. */
    void nap() {
        if (RadarService.gI().RADAR_TEMPLATE.isEmpty()) {
            SoSuuTamDAO.napLai();
        }
        hienBang(RadarService.gI().RADAR_TEMPLATE);
        napAura(idDangSua > 0 ? auraDangChon() : -1);
        if (idDangSua > 0) {
            chonTheoId(idDangSua);
        }
    }

    private void taiLaiTuCsdl() {
        SoSuuTamDAO.napLai();
        hienBang(RadarService.gI().RADAR_TEMPLATE);
        napAura(auraDangChon());
        if (idDangSua > 0) {
            chonTheoId(idDangSua);
        }
        bao("Đã đọc lại " + dangHien.size() + " thẻ từ CSDL.", XAM);
    }

    private void hienBang(List<RadarCard> ds) {
        dangDoDuLieu = true;
        try {
            mThe.setRowCount(0);
            dangHien.clear();
            for (RadarCard c : ds) {
                if (c == null) {
                    continue;
                }
                dangHien.add(c);
                mThe.addRow(new Object[]{c.ThuTu, (int) c.Id, c.Name,
                    TEN_HANG[Math.max(0, Math.min(TEN_HANG.length - 1, c.Rank))],
                    c.capToiDa(), canMoiCapHien(c), tenAura(c),
                    c.Options == null ? 0 : c.Options.size()});
            }
        } finally {
            dangDoDuLieu = false;
        }
    }

    private static String canMoiCapHien(RadarCard c) {
        if (c.CanMoiCap != null && c.CanMoiCap.length > 0) {
            return SoSuuTamDAO.ghiCanMoiCap(c.CanMoiCap);
        }
        return "mỗi cấp " + Math.max(1, c.Max);
    }

    private static String tenAura(RadarCard c) {
        if (c.AuraId <= 0) {
            return "—";
        }
        AuraDAO.Aura a = null;
        try {
            a = AuraDAO.theoId(c.AuraId);
        } catch (Exception boQua) {
            // Chua nap duoc bang hao quang thi hien id tran.
        }
        String ten = a == null ? "#" + c.AuraId : a.id + " — " + a.ten;
        return c.AuraTuCap > 0 ? ten + " (từ Lv." + c.AuraTuCap + ")" : ten;
    }

    private void chonTheoId(int id) {
        for (int i = 0; i < dangHien.size(); i++) {
            if (dangHien.get(i).Id == id) {
                bThe.setRowSelectionInterval(i, i);
                bThe.scrollRectToVisible(bThe.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void doVaoForm(RadarCard c) {
        idDangSua = c.Id;
        oId.setText(String.valueOf(c.Id));
        oTen.setText(c.Name);
        oIcon.setText(String.valueOf(c.IconId));
        cbHang.setSelectedIndex(Math.max(0, Math.min(TEN_HANG.length - 1, c.Rank)));
        oThuTu.setText(String.valueOf(c.ThuTu));
        cbKieu.setSelectedIndex(c.Type == 1 ? 1 : 0);
        oQuai.setText(String.valueOf(c.Template));
        oHead.setText(String.valueOf(c.Head));
        oBody.setText(String.valueOf(c.Body));
        oLeg.setText(String.valueOf(c.Leg));
        oBag.setText(String.valueOf(c.Bag));
        if (c.CanMoiCap != null && c.CanMoiCap.length > 0) {
            oCanMoiCap.setText(SoSuuTamDAO.ghiCanMoiCap(c.CanMoiCap));
        } else {
            // Dong kieu cu: dien san dung so dang chay (cot max cho moi cap),
            // luu mot lan la thanh dang moi.
            int[] cu = new int[c.capToiDa()];
            java.util.Arrays.fill(cu, Math.max(1, c.Max));
            oCanMoiCap.setText(SoSuuTamDAO.ghiCanMoiCap(cu));
        }
        napAura(c.AuraId);
        oAuraTuCap.setText(String.valueOf(c.AuraTuCap));
        oYeuCauThe.setText(String.valueOf(c.Require));
        oYeuCauCap.setText(String.valueOf(c.RequireLevel));
        oMoTa.setText(c.Info);
        oMoTa.setCaretPosition(0);
        if (bOpt.isEditing()) {
            bOpt.getCellEditor().cancelCellEditing();
        }
        mOpt.setRowCount(0);
        if (c.Options != null) {
            for (OptionCard o : c.Options) {
                mOpt.addRow(new Object[]{String.valueOf(o.id), o.param, (int) o.active});
            }
        }
        lblDangSua.setText("Đang sửa thẻ " + c.Id + " — " + c.Name);
        capNhatKieu();
    }

    private void moi() {
        idDangSua = -1;
        bThe.clearSelection();
        oId.setText("");
        oTen.setText("");
        oIcon.setText("0");
        cbHang.setSelectedIndex(0);
        oThuTu.setText(String.valueOf(dangHien.size() + 1));
        cbKieu.setSelectedIndex(0);
        oQuai.setText("1");
        oHead.setText("-1");
        oBody.setText("-1");
        oLeg.setText("-1");
        oBag.setText("-1");
        oCanMoiCap.setText("10,20,30");
        if (cbAura.getItemCount() == 0) {
            cbAura.addItem("-1 — Không có");
        }
        cbAura.setSelectedIndex(0);
        oAuraTuCap.setText("0");
        oYeuCauThe.setText("-1");
        oYeuCauCap.setText("0");
        oMoTa.setText("");
        if (bOpt.isEditing()) {
            bOpt.getCellEditor().cancelCellEditing();
        }
        mOpt.setRowCount(0);
        lblDangSua.setText("Đang tạo thẻ mới");
        capNhatKieu();
    }

    private void napAura(int chon) {
        DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<>();
        String khong = "-1 — Không có";
        m.addElement(khong);
        Object chonO = khong;
        try {
            for (AuraDAO.Aura a : AuraDAO.tatCa()) {
                m.addElement(a);
                if (a.id == chon) {
                    chonO = a;
                }
            }
        } catch (Exception boQua) {
            // Bang hao quang chua san sang — van cho chon "Khong co".
        }
        if (chon > 0 && chonO == khong) {
            String la = chon + " — (chưa có trong tab Hào quang)";
            m.addElement(la);
            chonO = la;
        }
        cbAura.setModel(m);
        cbAura.setSelectedItem(chonO);
    }

    private int auraDangChon() {
        Object o = cbAura.getSelectedItem();
        if (o instanceof AuraDAO.Aura) {
            return ((AuraDAO.Aura) o).id;
        }
        return soDau(o == null ? "" : o.toString(), -1);
    }

    // =====================================================================
    //  Ô gợi ý bên cạnh
    // =====================================================================

    private void capNhatKieu() {
        boolean quai = cbKieu.getSelectedIndex() != 1;
        oQuai.setEnabled(quai);
        oHead.setEnabled(!quai);
        oBody.setEnabled(!quai);
        oLeg.setEnabled(!quai);
        oBag.setEnabled(!quai);
    }

    private void capNhatTenVatPham() {
        int id = soCua(oId.getText(), -1);
        ItemTemplate t = mauVatPham(id);
        if (oId.getText().trim().isEmpty()) {
            lblVatPham.setText(" ");
        } else if (t == null) {
            lblVatPham.setForeground(DO);
            lblVatPham.setText("chưa có vật phẩm này");
        } else {
            lblVatPham.setForeground(t.type == 33 ? XANH_LA : XANH);
            lblVatPham.setText(t.name + " (loại " + t.type + (t.type == 33 ? " — thẻ" : "") + ")");
        }
    }

    private void capNhatTenQuai() {
        int id = soCua(oQuai.getText(), -1);
        String ten = null;
        try {
            for (MobTemplate t : new ArrayList<>(Manager.MOB_TEMPLATES)) {
                if (t != null && t.id == id) {
                    ten = t.name;
                    break;
                }
            }
        } catch (Exception boQua) {
            // Chua nap mau quai.
        }
        lblQuai.setForeground(ten == null ? DO : XAM);
        lblQuai.setText(ten == null ? "không có quái này" : ten);
    }

    private void capNhatCap() {
        int[] can = SoSuuTamDAO.docCanMoiCap(oCanMoiCap.getText());
        if (can == null) {
            lblCap.setForeground(DO);
            lblCap.setText("sai — các số dương cách nhau dấu phẩy");
        } else if (can.length == 0) {
            lblCap.setForeground(DO);
            lblCap.setText("nhập ít nhất một số");
        } else {
            long tong = 0;
            for (int v : can) {
                tong += v;
            }
            lblCap.setForeground(XANH_LA);
            lblCap.setText("tối đa Lv." + can.length + " — tổng " + tong + " món");
        }
    }

    private static ItemTemplate mauVatPham(int id) {
        if (id < 0) {
            return null;
        }
        try {
            return ItemService.gI().getTemplate(id);
        } catch (Exception ex) {
            return null;
        }
    }

    private void chonVatPham() {
        int id = OptionPicker.chonVatPham(this, soCua(oId.getText(), -1));
        if (id < 0) {
            return;
        }
        oId.setText(String.valueOf(id));
        ItemTemplate t = mauVatPham(id);
        if (t != null) {
            if (oTen.getText().trim().isEmpty()) {
                oTen.setText(t.name);
            }
            if (soCua(oIcon.getText(), 0) <= 0) {
                oIcon.setText(String.valueOf(t.iconID));
            }
            if (oMoTa.getText().trim().isEmpty() && t.description != null) {
                oMoTa.setText(t.description);
            }
        }
    }

    private void chonIcon() {
        Integer ic = IconPicker.chon(this, soCua(oIcon.getText(), 0));
        if (ic != null) {
            oIcon.setText(ic.toString());
        }
    }

    // =====================================================================
    //  Dòng chỉ số
    // =====================================================================

    private void themDong(int cap) {
        mOpt.addRow(new Object[]{"", 0, cap});
        int r = mOpt.getRowCount() - 1;
        bOpt.setRowSelectionInterval(r, r);
        bOpt.scrollRectToVisible(bOpt.getCellRect(r, 0, true));
    }

    private void themDongCapKe() {
        int cao = 0;
        for (int i = 0; i < mOpt.getRowCount(); i++) {
            cao = Math.max(cao, soCua(mOpt.getValueAt(i, O_CAP), 0));
        }
        themDong(cao + 1);
    }

    private void xoaDong() {
        if (bOpt.isEditing()) {
            bOpt.getCellEditor().stopCellEditing();
        }
        int[] rs = bOpt.getSelectedRows();
        for (int i = rs.length - 1; i >= 0; i--) {
            mOpt.removeRow(rs[i]);
        }
    }

    // =====================================================================
    //  Lưu / xoá
    // =====================================================================

    private void luu() {
        if (bOpt.isEditing()) {
            bOpt.getCellEditor().stopCellEditing();
        }
        RadarCard c = new RadarCard();
        int id = soCua(oId.getText(), -1);
        if (id <= 0 || id > Short.MAX_VALUE) {
            bao("Id vật phẩm không hợp lệ.", DO);
            return;
        }
        RadarCard trung = RadarService.gI().theo(id);
        if (trung != null && id != idDangSua) {
            bao("Id " + id + " đã có trong sổ (" + trung.Name + ") — chọn dòng đó để sửa.", DO);
            return;
        }
        c.Id = (short) id;
        c.Name = oTen.getText().trim();
        if (c.Name.isEmpty()) {
            bao("Chưa nhập tên thẻ.", DO);
            return;
        }
        int[] can = SoSuuTamDAO.docCanMoiCap(oCanMoiCap.getText());
        if (can == null || can.length == 0) {
            bao("Số lượng lên cấp phải là các số dương cách nhau dấu phẩy, vd 10,20,30.", DO);
            return;
        }
        c.CanMoiCap = can;
        c.Max = can[0];
        c.IconId = (short) soCua(oIcon.getText(), 0);
        c.Rank = (byte) Math.max(0, cbHang.getSelectedIndex());
        c.ThuTu = soCua(oThuTu.getText(), 0);
        c.Type = (byte) (cbKieu.getSelectedIndex() == 1 ? 1 : 0);
        c.Template = (short) soCua(oQuai.getText(), 1);
        c.Head = (short) soCua(oHead.getText(), -1);
        c.Body = (short) soCua(oBody.getText(), -1);
        c.Leg = (short) soCua(oLeg.getText(), -1);
        c.Bag = (short) soCua(oBag.getText(), -1);
        c.Info = oMoTa.getText().trim();
        c.AuraId = (short) auraDangChon();
        c.AuraTuCap = Math.max(0, soCua(oAuraTuCap.getText(), 0));
        c.Require = (short) soCua(oYeuCauThe.getText(), -1);
        c.RequireLevel = (short) Math.max(0, soCua(oYeuCauCap.getText(), 0));
        if (c.Require == c.Id) {
            bao("Thẻ không thể yêu cầu chính nó.", DO);
            return;
        }
        for (int i = 0; i < mOpt.getRowCount(); i++) {
            String sId = OptionPicker.laySo(String.valueOf(mOpt.getValueAt(i, O_CHI_SO)));
            if (sId.isEmpty()) {
                continue;
            }
            int cap = soCua(mOpt.getValueAt(i, O_CAP), 0);
            if (cap < 0 || cap > can.length) {
                bao("Dòng chỉ số " + (i + 1) + " ở cấp " + cap + " nhưng thẻ chỉ tới Lv."
                        + can.length + ".", DO);
                return;
            }
            c.Options.add(new OptionCard(Integer.parseInt(sId),
                    soCua(mOpt.getValueAt(i, O_GIA_TRI), 0), (byte) cap));
        }
        String loi = SoSuuTamDAO.luu(c, idDangSua);
        if (loi != null) {
            bao(loi, DO);
            return;
        }
        idDangSua = c.Id;
        hienBang(RadarService.gI().RADAR_TEMPLATE);
        chonTheoId(c.Id);
        bao("Đã lưu " + c.Name + " — có hiệu lực ngay, người chơi mở lại sổ là thấy.", XANH_LA);
    }

    private void xoa() {
        if (idDangSua <= 0) {
            bao("Chọn một thẻ trong bảng trước.", DO);
            return;
        }
        RadarCard c = RadarService.gI().theo(idDangSua);
        String ten = c == null ? String.valueOf(idDangSua) : c.Name;
        int chon = JOptionPane.showConfirmDialog(this, "Xoá thẻ \"" + ten + "\" khỏi sổ?\n"
                + "Người chơi đã sưu tầm thẻ này sẽ mất chỉ số và hào quang của nó.",
                "Xoá thẻ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = SoSuuTamDAO.xoa(idDangSua);
        if (loi != null) {
            bao(loi, DO);
            return;
        }
        hienBang(RadarService.gI().RADAR_TEMPLATE);
        moi();
        bao("Đã xoá " + ten + ".", XANH_LA);
    }

    private void bao(String s, Color mau) {
        trangThai.setForeground(mau);
        trangThai.setText(s);
    }

    private static int soCua(Object v, int macDinh) {
        if (v == null) {
            return macDinh;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        String s = v.toString().trim();
        if (s.isEmpty()) {
            return macDinh;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return macDinh;
        }
    }

    /** Số nguyên đứng đầu chuỗi ("-1 — Không có" → -1). */
    private static int soDau(String s, int macDinh) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\s*(-?\\d+)").matcher(s);
        return m.find() ? Integer.parseInt(m.group(1)) : macDinh;
    }
}
