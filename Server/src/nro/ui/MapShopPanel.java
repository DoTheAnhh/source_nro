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
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import nro.repository.dao.MapShopDAO;
import nro.server.Manager;

/**
 * Tab <b>Quản Lý Bản Đồ</b> — bản đồ → NPC đứng trên đó → cửa hàng của NPC.
 *
 * <p>Ba bảng nối nhau theo đúng thứ tự thao tác: chọn bản đồ ở trái, xem và sửa
 * NPC đứng trên đó ở giữa, chọn NPC có cửa hàng thì phần dưới hiện thẻ hàng và
 * danh sách món bán.</p>
 *
 * <h2>Sửa xong có hiệu lực khi nào</h2>
 *
 * <ul>
 *   <li><b>Cửa hàng</b> — ngay, panel gọi {@code Manager.gI().updateShop()}.
 *       Đây là chỗ duy nhất trong máy chủ có sẵn cơ chế nạp lại nóng.</li>
 *   <li><b>NPC trên bản đồ</b> — phải khởi động lại máy chủ: đối tượng NPC được
 *       dựng một lần lúc nạp bản đồ, không có đường nạp lại.</li>
 * </ul>
 */
public class MapShopPanel extends JPanel {

    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color OK_GREEN = new Color(40, 160, 70);
    private static final Color WARN_RED = new Color(200, 60, 60);
    private static final Color GREY = new Color(120, 120, 120);

    private final JLabel lblStatus = new JLabel(" ");

    // ---------------------------------------------------------------- bản đồ
    private final DefaultTableModel mapModel = new DefaultTableModel(
            new Object[]{"ID", "Tên bản đồ", "Khu", "Tối đa", "Số NPC"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable mapTable = new JTable(mapModel);
    private final JTextField fTimMap = new JTextField(16);
    private List<MapShopDAO.MapRow> dsMap = new ArrayList<>();

    // ---------------------------------------------------------------- NPC
    private final DefaultTableModel npcModel = new DefaultTableModel(
            new Object[]{"NPC id", "Tên NPC", "X", "Y", "Có cửa hàng"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable npcTable = new JTable(npcModel);
    private List<MapShopDAO.NpcTren> dsNpc = new ArrayList<>();

    // ---------------------------------------------------------------- cửa hàng
    private final JComboBox<MapShopDAO.TabRow> cbTab = new JComboBox<>();
    private final JLabel lblShop = new JLabel("—");
    private final DefaultTableModel itemModel = new DefaultTableModel(
            new Object[]{"ID", "Ảnh", "Vật phẩm", "Tên vật phẩm", "Giá", "Bán theo",
                "Giá vàng phụ", "Đang bán", "Mới", "Chỉ số"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            // Cot 1 giu ID ANH, khong giu ImageIcon — xem veIcon().
            return Object.class;
        }
    };
    private final JTable itemTable = new JTable(itemModel);
    private List<MapShopDAO.ShopRow> dsShop = new ArrayList<>();

    public MapShopPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Quản Lý Bản Đồ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        JSplitPane doc = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMap(), buildNpc());
        doc.setResizeWeight(0.42);
        doc.setBorder(null);

        JSplitPane ngang = new JSplitPane(JSplitPane.VERTICAL_SPLIT, doc, buildShop());
        ngang.setResizeWeight(0.5);
        ngang.setBorder(null);
        add(ngang, BorderLayout.CENTER);

        lblStatus.setForeground(GREY);
        lblStatus.setBorder(new EmptyBorder(4, 2, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);

        loadMaps();

        // Ten va anh vat pham hien ngay trong bang hang ban, nen sua ben Quan
        // Ly Vat Pham la o day phai ve lai.
        LamMoi.nghe(this::loadItemsNeuCo, LamMoi.VAT_PHAM, LamMoi.ANH, LamMoi.LOAI);
        LamMoi.nghe(LamMoi.SHOP, this::loadShopNeuCo);
    }

    /** Vẽ lại bảng hàng bán, bỏ qua nếu chưa chọn tab cửa hàng nào. */
    private void loadItemsNeuCo() {
        if (cbTab.getItemCount() > 0) {
            loadItems();
        }
    }

    private void loadShopNeuCo() {
        if (npcTable.getRowCount() > 0) {
            loadShop();
        }
    }

    // =====================================================================
    //  Bản đồ
    // =====================================================================

    private JComponent buildMap() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(titled("Bản đồ"));

        mapTable.setRowHeight(22);
        mapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mapTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        mapTable.setAutoCreateRowSorter(true);
        int[] w = {45, 240, 45, 55, 60};
        for (int i = 0; i < w.length; i++) {
            mapTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        mapTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadNpc();
            }
        });
        p.add(ServerGuiUtils.cuon(mapTable), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(new JLabel("Tìm:"));
        top.add(fTimMap);
        top.add(button("Tải lại", GREY, e -> loadMaps()));
        fTimMap.getDocument().addDocumentListener(new Simple(this::filterMaps));
        p.add(top, BorderLayout.NORTH);
        return p;
    }

    private void loadMaps() {
        dsMap = MapShopDAO.maps();
        filterMaps();
        note(GREY, "Có " + dsMap.size() + " bản đồ.");
    }

    private void filterMaps() {
        String key = fTimMap.getText().trim().toLowerCase();
        mapModel.setRowCount(0);
        for (MapShopDAO.MapRow m : dsMap) {
            if (!key.isEmpty() && !String.valueOf(m.name).toLowerCase().contains(key)
                    && !String.valueOf(m.id).equals(key)) {
                continue;
            }
            mapModel.addRow(new Object[]{m.id, m.name, m.zones, m.maxPlayer, m.soNpc});
        }
    }

    private MapShopDAO.MapRow mapDangChon() {
        int r = mapTable.getSelectedRow();
        if (r < 0) {
            return null;
        }
        int id = intOf(mapModel.getValueAt(mapTable.convertRowIndexToModel(r), 0));
        for (MapShopDAO.MapRow m : dsMap) {
            if (m.id == id) {
                return m;
            }
        }
        return null;
    }

    // =====================================================================
    //  NPC
    // =====================================================================

    private JComponent buildNpc() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(titled("NPC đứng trên bản đồ đang chọn"));

        npcTable.setRowHeight(22);
        npcTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        npcTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] w = {55, 220, 60, 60, 80};
        for (int i = 0; i < w.length; i++) {
            npcTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        npcTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadShop();
            }
        });
        npcTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && npcTable.getSelectedRow() >= 0) {
                    npcDialog(false);
                }
            }
        });
        p.add(ServerGuiUtils.cuon(npcTable), BorderLayout.CENTER);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btn.setOpaque(false);
        btn.add(button("Thêm NPC", OK_GREEN, e -> npcDialog(true)));
        btn.add(button("Sửa NPC", ACCENT, e -> npcDialog(false)));
        btn.add(button("Xoá NPC", WARN_RED, e -> npcXoa()));
        btn.add(button("Sửa mẫu NPC...", GREY, e -> npcMauDialog()));
        btn.add(button("Đổi tên mục menu...", ACCENT, e -> npcMenuDialog()));
        JLabel tip = new JLabel("  (đổi NPC cần khởi động lại máy chủ)");
        tip.setForeground(GREY);
        btn.add(tip);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private void loadNpc() {
        npcModel.setRowCount(0);
        itemModel.setRowCount(0);
        cbTab.removeAllItems();
        lblShop.setText("—");
        MapShopDAO.MapRow m = mapDangChon();
        if (m == null) {
            return;
        }
        dsNpc = MapShopDAO.npcTrenMap(m.npcs);
        for (MapShopDAO.NpcTren n : dsNpc) {
            npcModel.addRow(new Object[]{n.npcId, n.ten, n.x, n.y, n.coShop ? "có" : ""});
        }
    }

    private MapShopDAO.NpcTren npcDangChon() {
        int r = npcTable.getSelectedRow();
        return r < 0 || r >= dsNpc.size() ? null : dsNpc.get(r);
    }

    /** Hộp thoại thêm/sửa một NPC trên bản đồ. */
    private void npcDialog(boolean them) {
        MapShopDAO.MapRow m = mapDangChon();
        if (m == null) {
            note(WARN_RED, "Chưa chọn bản đồ nào.");
            return;
        }
        MapShopDAO.NpcTren cu = them ? null : npcDangChon();
        if (!them && cu == null) {
            note(WARN_RED, "Chưa chọn NPC nào để sửa.");
            return;
        }

        // O chon gom HAI nhom: mau NPC co san, roi den cai trang. Chon mot cai
        // trang thi luc bam Luu moi dung mau NPC mang hinh cai trang do —
        // khong dung truoc, tranh de rac lai khi nguoi dung bam Huy.
        JComboBox<Object> cb = new JComboBox<>();
        for (MapShopDAO.NpcMau n : MapShopDAO.npcMau()) {
            cb.addItem(n);
        }
        java.util.List<MapShopDAO.CaiTrangMau> dsCt = MapShopDAO.caiTrangLamNpc();
        for (MapShopDAO.CaiTrangMau x : dsCt) {
            cb.addItem(x);
        }
        if (cu != null) {
            for (int i = 0; i < cb.getItemCount(); i++) {
                Object it = cb.getItemAt(i);
                if (it instanceof MapShopDAO.NpcMau
                        && ((MapShopDAO.NpcMau) it).id == cu.npcId) {
                    cb.setSelectedIndex(i);
                    break;
                }
            }
        }
        JTextField fX = new JTextField(cu != null ? String.valueOf(cu.x) : "300", 8);
        JTextField fY = new JTextField(cu != null ? String.valueOf(cu.y) : "336", 8);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("NPC:"), c);
        c.gridx = 1;
        c.weightx = 1;
        cb.setPreferredSize(new Dimension(300, 26));
        form.add(cb, c);
        c.weightx = 0;
        addRow(form, c, 1, "X:", fX);
        addRow(form, c, 2, "Y:", fY);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        int soHong = MapShopDAO.soNpcHong();
        form.add(new JLabel("<html><span style='color:#777'>"
                + "Y mặt đất thường là <code>336</code>.<br>"
                + "Cuối danh sách là <b>" + dsCt.size() + " cải trang</b> — chọn "
                + "một cái thì máy chủ tự dựng mẫu NPC mang hình cải trang đó "
                + "(có mẫu trùng sẵn thì dùng lại).<br>"
                + (soHong > 0
                    ? "Đã ẩn <b>" + soHong + " mẫu NPC hỏng</b> (không có tên "
                      + "hoặc không có hình).<br>"
                    : "")
                + "Đổi NPC trên bản đồ <b>cần khởi động lại máy chủ</b> — đối tượng NPC "
                + "được dựng một lần lúc nạp bản đồ.</span></html>"), c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                (them ? "Thêm NPC vào " : "Sửa NPC trên ") + m.name, true);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button(them ? "Thêm" : "Lưu", OK_GREEN, e -> {
            Object muc = cb.getSelectedItem();
            if (muc == null) {
                JOptionPane.showMessageDialog(dlg, "Chưa chọn NPC.");
                return;
            }
            int npcId;
            if (muc instanceof MapShopDAO.CaiTrangMau) {
                npcId = MapShopDAO.npcTuCaiTrang((MapShopDAO.CaiTrangMau) muc);
                if (npcId < 0) {
                    JOptionPane.showMessageDialog(dlg,
                            "Không dựng được mẫu NPC từ cải trang này — xem log "
                            + "máy chủ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                npcId = ((MapShopDAO.NpcMau) muc).id;
            }
            int x;
            int y;
            try {
                x = Integer.parseInt(fX.getText().trim());
                y = Integer.parseInt(fY.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "X và Y phải là số nguyên.");
                return;
            }
            if (them) {
                MapShopDAO.NpcTren n = new MapShopDAO.NpcTren();
                n.npcId = npcId;
                n.x = x;
                n.y = y;
                dsNpc.add(n);
            } else {
                cu.npcId = npcId;
                cu.x = x;
                cu.y = y;
            }
            dlg.dispose();
            luuNpc(m);
        }));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(form, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(560, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void npcXoa() {
        MapShopDAO.MapRow m = mapDangChon();
        int[] rs = npcTable.getSelectedRows();
        if (m == null || rs.length == 0) {
            note(WARN_RED, "Chưa chọn NPC nào.");
            return;
        }
        java.util.List<MapShopDAO.NpcTren> bo = new java.util.ArrayList<>();
        StringBuilder ten = new StringBuilder();
        for (int r : rs) {
            int idx = npcTable.convertRowIndexToModel(r);
            if (idx >= 0 && idx < dsNpc.size()) {
                MapShopDAO.NpcTren n = dsNpc.get(idx);
                bo.add(n);
                ten.append(ten.length() == 0 ? "" : ", ").append(n.ten);
            }
        }
        if (bo.isEmpty()) {
            note(WARN_RED, "Chưa chọn NPC nào.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Gỡ " + bo.size() + " NPC khỏi bản đồ " + m.name + "?\n\n" + ten,
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        dsNpc.removeAll(bo);
        luuNpc(m);
    }

    private void luuNpc(MapShopDAO.MapRow m) {
        String loi = MapShopDAO.luuNpcMap(m.id, dsNpc);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadMaps();
        LamMoi.bao(LamMoi.SHOP);
        note(OK_GREEN, "Đã lưu NPC của bản đồ " + m.name
                + " — khởi động lại máy chủ để áp dụng.");
    }

    // =====================================================================
    //  Cửa hàng
    // =====================================================================

    private JComponent buildShop() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(titled("Cửa hàng của NPC đang chọn"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(new JLabel("Cửa hàng:"));
        lblShop.setForeground(ACCENT);
        lblShop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        top.add(lblShop);
        top.add(new JLabel("   Thẻ hàng:"));
        cbTab.setPreferredSize(new Dimension(200, 26));
        top.add(cbTab);
        cbTab.addActionListener(e -> loadItems());
        top.add(button("Thêm thẻ", OK_GREEN, e -> tabDialog(true)));
        top.add(button("Đổi tên thẻ", ACCENT, e -> tabDialog(false)));
        top.add(button("Xoá thẻ", WARN_RED, e -> tabXoa()));
        top.add(button("Cửa hàng...", GREY, e -> shopDialog()));
        top.add(button("Sửa để hiện giá", new Color(120, 90, 160), e -> suaDeHienGia()));
        top.add(button("Xoá cửa hàng", WARN_RED, e -> shopXoa()));
        p.add(top, BorderLayout.NORTH);

        itemTable.setRowHeight(32);
        itemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        itemTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] w = {45, 40, 70, 230, 110, 100, 100, 70, 45, 200};
        for (int i = 0; i < w.length; i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        itemTable.getColumnModel().getColumn(1)
                .setCellRenderer(PlayerManagerPanel.veIcon());
        itemTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && itemTable.getSelectedRow() >= 0) {
                    itemDialog(false);
                }
            }
        });
        p.add(ServerGuiUtils.cuon(itemTable), BorderLayout.CENTER);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btn.setOpaque(false);
        btn.add(button("Thêm món bán", OK_GREEN, e -> itemDialog(true)));
        btn.add(button("Sửa món bán", ACCENT, e -> itemDialog(false)));
        btn.add(button("Sửa nhiều", ACCENT, e -> itemSuaNhieu()));
        btn.add(button("Xoá món bán", WARN_RED, e -> itemXoa()));
        btn.add(button("▲ Lên", ACCENT, e -> itemDoiCho(-1)));
        btn.add(button("▼ Xuống", ACCENT, e -> itemDoiCho(1)));
        btn.add(button("Tải lại", GREY, e -> loadItems()));
        JLabel tip = new JLabel("  (sửa cửa hàng có hiệu lực ngay • nháy đúp để sửa)");
        tip.setForeground(GREY);
        btn.add(tip);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private void loadShop() {
        cbTab.removeAllItems();
        itemModel.setRowCount(0);
        MapShopDAO.NpcTren n = npcDangChon();
        if (n == null) {
            lblShop.setText("—");
            return;
        }
        dsShop = MapShopDAO.shopCuaNpc(n.npcId);
        if (dsShop.isEmpty()) {
            lblShop.setText("NPC này không có cửa hàng");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (MapShopDAO.ShopRow s : dsShop) {
            if (sb.length() > 0) {
                sb.append(" • ");
            }
            sb.append(s.tagName == null ? ("shop " + s.id) : s.tagName);
            for (MapShopDAO.TabRow t : MapShopDAO.tabCuaShop(s.id)) {
                cbTab.addItem(t);
            }
        }
        lblShop.setText(sb.toString());
        loadItems();
    }

    private void loadItems() {
        itemModel.setRowCount(0);
        MapShopDAO.TabRow t = (MapShopDAO.TabRow) cbTab.getSelectedItem();
        if (t == null) {
            return;
        }
        for (MapShopDAO.ItemShopRow it : MapShopDAO.itemCuaTab(t.id)) {
            nro.entity.template.ItemTemplate tp = PlayerManagerPanel.templateOf(
                    String.valueOf(it.tempId));
            itemModel.addRow(new Object[]{it.id,
                tp != null ? (Object) tp.iconID : null,
                it.tempId, tp != null ? tp.name : "(không rõ)",
                PlayerManagerPanel.fmt(it.cost), MapShopDAO.tenLoaiTien(it.typeSell),
                PlayerManagerPanel.fmt(it.costGold), it.isSell ? "có" : "", it.isNew ? "mới" : "",
                it.options});
        }
    }

    /** Hộp thoại thêm/sửa một món bày bán. */
    private void itemDialog(boolean them) {
        MapShopDAO.TabRow tab = (MapShopDAO.TabRow) cbTab.getSelectedItem();
        if (tab == null) {
            note(WARN_RED, "Chưa chọn thẻ hàng nào.");
            return;
        }
        int r = itemTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn món nào để sửa.");
            return;
        }

        final MapShopDAO.ItemShopRow it = new MapShopDAO.ItemShopRow();
        it.tabId = tab.id;
        if (!them) {
            it.id = intOf(itemModel.getValueAt(r, 0));
            it.tempId = intOf(itemModel.getValueAt(r, 2));
        }

        JTextField fId = new JTextField(them ? "" : String.valueOf(it.tempId), 8);
        JLabel lblTen = new JLabel("—");
        lblTen.setForeground(ACCENT);
        JLabel lblAnh = new JLabel();
        JTextField fGia = new JTextField(them ? "0" : soThuan(itemModel.getValueAt(r, 4)), 12);
        JTextField fGiaVang = new JTextField(them ? "0" : soThuan(itemModel.getValueAt(r, 6)), 12);
        JCheckBox cbBan = new JCheckBox("Đang bán", them || "có".equals(
                String.valueOf(itemModel.getValueAt(r, 7))));
        JCheckBox cbMoi = new JCheckBox("Gắn nhãn mới", !them && "mới".equals(
                String.valueOf(itemModel.getValueAt(r, 8))));

        JComboBox<String> cbTien = new JComboBox<>();
        for (int lt : MapShopDAO.LOAI_TIEN) {
            cbTien.addItem(MapShopDAO.tenLoaiTien(lt));
        }
        if (!them) {
            cbTien.setSelectedItem(String.valueOf(itemModel.getValueAt(r, 5)));
        }

        Runnable traTen = () -> {
            nro.entity.template.ItemTemplate tp = PlayerManagerPanel.templateOf(fId.getText());
            lblTen.setText(tp != null ? tp.name + "   " + PlayerManagerPanel.typeName(tp.type)
                    : "(không có id này)");
            lblTen.setForeground(tp != null ? ACCENT : WARN_RED);
            lblAnh.setIcon(tp != null ? PlayerManagerPanel.iconOf(tp.iconID) : null);
        };
        fId.getDocument().addDocumentListener(new Simple(traTen));
        traTen.run();

        // ---------- bảng chỉ số ----------
        DefaultTableModel mOpt = new DefaultTableModel(
                new Object[]{"Chỉ số", "Giá trị", "Ngẫu nhiên tới"}, 0);
        JTable tOpt = new JTable(mOpt);
        tOpt.setRowHeight(24);
        tOpt.getColumnModel().getColumn(0).setPreferredWidth(220);
        tOpt.getColumnModel().getColumn(1).setPreferredWidth(70);
        tOpt.getColumnModel().getColumn(2).setPreferredWidth(110);
        OptionPicker.install(tOpt, 0);
        if (!them) {
            napBangChiSo(mOpt, String.valueOf(itemModel.getValueAt(r, 9)));
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "ID vật phẩm:", fId);
        c.gridx = 2;
        c.gridy = 0;
        form.add(lblAnh, c);
        c.gridx = 3;
        form.add(button("Chọn...", GREY, e -> {
            int id = OptionPicker.chonVatPham(this, -1);
            if (id >= 0) {
                fId.setText(String.valueOf(id));
            }
        }), c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        form.add(lblTen, c);
        c.gridwidth = 1;
        addRow(form, c, 2, "Giá:", fGia);
        c.gridx = 0;
        c.gridy = 3;
        form.add(new JLabel("Bán theo:"), c);
        c.gridx = 1;
        form.add(cbTien, c);
        addRow(form, c, 4, "Giá vàng phụ:", fGiaVang);
        c.gridx = 1;
        c.gridy = 5;
        form.add(cbBan, c);
        c.gridx = 2;
        form.add(cbMoi, c);

        JPanel optBox = new JPanel(new BorderLayout(0, 4));
        optBox.setBorder(titled("Chỉ số kèm theo món này"));
        optBox.add(ServerGuiUtils.cuon(tOpt), BorderLayout.CENTER);
        JPanel ob = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        ob.add(button("Thêm dòng", ACCENT, e -> mOpt.addRow(new Object[]{"0", "0", ""})));
        ob.add(button("Xoá dòng", WARN_RED, e -> {
            int k = tOpt.getSelectedRow();
            if (k >= 0) {
                mOpt.removeRow(k);
            }
        }));
        ob.add(OptionPicker.oKhoaChoBang(mOpt, 3));
        optBox.add(ob, BorderLayout.SOUTH);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                them ? "Thêm món bán" : "Sửa món bán", true);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button(them ? "Thêm" : "Lưu", OK_GREEN, e -> {
            nro.entity.template.ItemTemplate tp = PlayerManagerPanel.templateOf(fId.getText());
            if (tp == null) {
                JOptionPane.showMessageDialog(dlg, "ID vật phẩm không tồn tại.");
                return;
            }
            try {
                it.tempId = tp.id;
                it.cost = Integer.parseInt(fGia.getText().trim().replace(".", "").replace(",", ""));
                it.costGold = Integer.parseInt(
                        fGiaVang.getText().trim().replace(".", "").replace(",", ""));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá phải là số nguyên.");
                return;
            }
            it.typeSell = MapShopDAO.LOAI_TIEN[Math.max(0, cbTien.getSelectedIndex())];
            it.isSell = cbBan.isSelected();
            it.isNew = cbMoi.isSelected();
            // Luon ghi anh cua loai tien. Cua hang kieu 3 thanh toan qua cot
            // nay; de trong la mua se hong.
            int anh = MapShopDAO.anhLoaiTien(it.typeSell);
            if (anh >= 0) {
                it.iconSpec = anh;
            }

            List<int[]> opts = docBangChiSo(tOpt, mOpt);
            if (opts == null) {
                JOptionPane.showMessageDialog(dlg, "Bảng chỉ số phải là số nguyên.");
                return;
            }

            String loi = MapShopDAO.luuItemShop(it, opts);
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không lưu được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            goiYDoiKieuShop(it.typeSell);
            apDungShop(them ? "Đã thêm " + tp.name + " vào cửa hàng"
                    : "Đã sửa " + tp.name);
        }));

        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setBorder(new EmptyBorder(10, 10, 10, 10));
        wrap.add(form, BorderLayout.NORTH);
        wrap.add(optBox, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.setSize(640, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /**
     * Hỏi đổi kiểu cửa hàng khi loại tiền không hiện được giá.
     *
     * <p><b>Vì sao cần.</b> Gói tin của cửa hàng <b>kiểu thường</b> chỉ có hai ô
     * giá: vàng và ngọc. Thỏi vàng và Điểm sự kiện không có ô nào, nên client
     * hiện giá 0 — người chơi thấy "Nhận Miễn phí" dù máy chủ vẫn trừ đúng tiền
     * lúc mua. Đó không phải lỗi hiển thị vặt: nó mời người ta bấm nhầm.</p>
     *
     * <p>Cửa hàng <b>kiểu 3 (bán bằng vật phẩm)</b> gửi <i>ảnh</i> của món dùng
     * làm tiền kèm số lượng, nên hiện được giá của bất kỳ vật phẩm nào.</p>
     *
     * <p>Không tự đổi mà <b>hỏi</b>: đổi kiểu là đổi cả cách client vẽ cửa hàng,
     * không phải chuyện nên làm sau lưng người dùng.</p>
     */
    private void goiYDoiKieuShop(int typeSell) {
        if (!MapShopDAO.kieuThuongKhongHienDuocGia(typeSell)) {
            return;
        }
        MapShopDAO.ShopRow shop = shopDangChon();
        if (shop == null || shop.typeShop == 3) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Cửa hàng này đang là \"" + MapShopDAO.tenKieuShop(shop.typeShop) + "\".\n\n"
                + "Kiểu đó chỉ hiện được giá bằng Vàng và Ngọc, nên món bán theo "
                + MapShopDAO.tenLoaiTien(typeSell) + " sẽ hiện giá 0 —\n"
                + "người chơi thấy \"Nhận Miễn phí\" dù máy chủ vẫn trừ đúng tiền.\n\n"
                + "Đổi cửa hàng sang \"" + MapShopDAO.tenKieuShop(3) + "\" để hiện đúng giá?\n"
                + "(Mọi món trong cửa hàng sẽ được gán ảnh loại tiền tương ứng.)",
                "Cửa hàng không hiện được giá", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            note(WARN_RED, "Giữ nguyên kiểu cửa hàng — món này sẽ hiện giá 0 trong game.");
            return;
        }
        String loi = MapShopDAO.luuShop(shop.id, shop.tagName, 3);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        int n = MapShopDAO.dongBoAnhTien(shop.id);
        note(OK_GREEN, "Đã đổi cửa hàng sang \"" + MapShopDAO.tenKieuShop(3)
                + "\" và gán ảnh loại tiền cho " + n + " món.");
    }




    /**
     * Doi cho mon ban dang chon len tren hoac xuong duoi mot bac.
     *
     * <p>Thu tu nay dung CHUNG voi trong game — doi o day thi cua hang nguoi
     * choi thay cung doi theo, khong can khoi dong lai may chu.</p>
     */
    private void itemDoiCho(int huong) {
        int r = itemTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn món nào.");
            return;
        }
        int i = itemTable.convertRowIndexToModel(r);
        int j = i + huong;
        if (j < 0 || j >= itemModel.getRowCount()) {
            return;
        }
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (int k = 0; k < itemModel.getRowCount(); k++) {
            try {
                ids.add(Integer.parseInt(
                        String.valueOf(itemModel.getValueAt(k, 0)).trim()));
            } catch (NumberFormatException ex) {
                note(WARN_RED, "Bảng có dòng không đọc được id — tải lại rồi thử lại.");
                return;
            }
        }
        int tmp = ids.get(i);
        ids.set(i, ids.get(j));
        ids.set(j, tmp);
        String loi = nro.repository.dao.MapShopDAO.luuThuTu(ids);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadItems();
        if (j < itemTable.getRowCount()) {
            itemTable.setRowSelectionInterval(j, j);
            itemTable.scrollRectToVisible(itemTable.getCellRect(j, 0, true));
        }
        LamMoi.bao(LamMoi.SHOP);
        note(OK_GREEN, "Đã đổi thứ tự — cửa hàng trong game hiện đúng thứ tự này.");
    }

    /**
     * Đổ chuỗi chỉ số {@code "50=5~10, 30=0"} vào bảng ba cột.
     *
     * <p>Phần sau dấu {@code ~} là trần của khoảng ngẫu nhiên; không có dấu đó
     * thì cột "Ngẫu nhiên tới" để trống, nghĩa là trị cố định.</p>
     */
    private static void napBangChiSo(DefaultTableModel mOpt, String chuoi) {
        for (String phan : String.valueOf(chuoi).split(",")) {
            String[] kv = phan.split("=");
            if (kv.length != 2) {
                continue;
            }
            String tri = kv[1].trim();
            int ngan = tri.indexOf('~');
            if (ngan > 0) {
                mOpt.addRow(new Object[]{kv[0].trim(), tri.substring(0, ngan).trim(),
                    tri.substring(ngan + 1).trim()});
            } else {
                mOpt.addRow(new Object[]{kv[0].trim(), tri, ""});
            }
        }
    }

    /**
     * Đọc bảng ba cột thành danh sách {@code {id, min, max}}.
     *
     * @return {@code null} nếu có ô không phải số — chỗ gọi tự báo lỗi
     */
    private static List<int[]> docBangChiSo(JTable tOpt, DefaultTableModel mOpt) {
        if (tOpt.isEditing()) {
            tOpt.getCellEditor().stopCellEditing();
        }
        List<int[]> ra = new ArrayList<>();
        try {
            for (int i = 0; i < mOpt.getRowCount(); i++) {
                String sId = OptionPicker.laySo(String.valueOf(mOpt.getValueAt(i, 0)));
                if (sId.isEmpty()) {
                    continue;
                }
                int min = Integer.parseInt(String.valueOf(mOpt.getValueAt(i, 1)).trim());
                Object oMax = mOpt.getValueAt(i, 2);
                String sMax = oMax == null ? "" : String.valueOf(oMax).trim();
                int max = sMax.isEmpty() ? min : Integer.parseInt(sMax);
                ra.add(new int[]{Integer.parseInt(sId), min, Math.max(min, max)});
            }
        } catch (NumberFormatException ex) {
            return null;
        }
        return ra;
    }



    /**
     * Sua NHIEU mon ban mot luot.
     *
     * <p>Moi truong co mot o tich rieng: chi truong nao duoc tich moi ghi de,
     * cac truong khac giu nguyen cua tung mon. Neu ap dat het moi truong thi
     * sua hang loat se xoa mat gia rieng, chi so rieng cua tung mon.</p>
     */
    private void itemSuaNhieu() {
        int[] hang = itemTable.getSelectedRows();
        if (hang.length < 2) {
            note(WARN_RED, "Chọn từ 2 món trở lên (giữ Ctrl hoặc Shift).");
            return;
        }
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (int h : hang) {
            int idx = itemTable.convertRowIndexToModel(h);
            try {
                ids.add(Integer.parseInt(
                        String.valueOf(itemModel.getValueAt(idx, 0)).trim()));
            } catch (NumberFormatException ex) {
                note(WARN_RED, "Có dòng không đọc được id — bấm Tải lại rồi thử lại.");
                return;
            }
        }

        JCheckBox cbGia = new JCheckBox("Đổi giá:");
        JTextField fGia = new JTextField("0", 10);
        JCheckBox cbTien = new JCheckBox("Đổi loại tiền:");
        JComboBox<String> cboTien = new JComboBox<>();
        for (int lt : MapShopDAO.LOAI_TIEN) {
            cboTien.addItem(MapShopDAO.tenLoaiTien(lt));
        }
        JCheckBox cbBan = new JCheckBox("Đổi trạng thái bán:");
        JComboBox<String> cboBan = new JComboBox<>(new String[]{"đang bán", "ngừng bán"});
        JCheckBox cbMoi = new JCheckBox("Đổi nhãn mới:");
        JComboBox<String> cboMoi = new JComboBox<>(new String[]{"có gắn nhãn", "bỏ nhãn"});

        // Bảng chỉ số dùng chung cho cả lô. Không tích ô thì mỗi món giữ nguyên
        // chỉ số riêng; tích mà để bảng trống là xoá sạch chỉ số của cả lô.
        JCheckBox cbChiSo = new JCheckBox("Đổi chỉ số (ghi đè toàn bộ):");
        DefaultTableModel mOpt = new DefaultTableModel(
                new Object[]{"Chỉ số", "Giá trị", "Ngẫu nhiên tới"}, 0);
        JTable tOpt = new JTable(mOpt);
        tOpt.setRowHeight(24);
        tOpt.getColumnModel().getColumn(0).setPreferredWidth(230);
        tOpt.getColumnModel().getColumn(1).setPreferredWidth(70);
        tOpt.getColumnModel().getColumn(2).setPreferredWidth(110);
        OptionPicker.install(tOpt, 0);
        // Chép sẵn chỉ số của dòng đầu đang chọn: sửa lô thường là "cho giống
        // nhau hết", có sẵn một bộ để chỉnh nhanh hơn gõ lại từ đầu.
        napBangChiSo(mOpt, String.valueOf(itemModel.getValueAt(
                itemTable.convertRowIndexToModel(hang[0]), 9)));

        JScrollPane cuonOpt = new JScrollPane(tOpt);
        cuonOpt.setPreferredSize(new java.awt.Dimension(430, 130));
        JButton btnThemOpt = button("Thêm dòng", ACCENT,
                e -> mOpt.addRow(new Object[]{"0", "0", ""}));
        JButton btnXoaOpt = button("Xoá dòng", WARN_RED, e -> {
            int r = tOpt.getSelectedRow();
            if (r >= 0) {
                mOpt.removeRow(r);
            }
        });
        JButton btnXoaHet = button("Xoá hết", GREY, e -> mOpt.setRowCount(0));
        JPanel nutOpt = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 6, 0));
        nutOpt.setOpaque(false);
        nutOpt.add(btnThemOpt);
        nutOpt.add(btnXoaOpt);
        nutOpt.add(btnXoaHet);
        nutOpt.add(OptionPicker.oKhoaChoBang(mOpt, 3));
        JPanel khungOpt = new JPanel(new BorderLayout(0, 4));
        khungOpt.setOpaque(false);
        khungOpt.add(cuonOpt, BorderLayout.CENTER);
        khungOpt.add(nutOpt, BorderLayout.SOUTH);


        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        form.add(new JLabel("Áp dụng cho " + ids.size() + " món đang chọn."), c);
        c.gridwidth = 1;
        y++;
        hangSua(form, c, y++, cbGia, fGia);
        hangSua(form, c, y++, cbTien, cboTien);
        hangSua(form, c, y++, cbBan, cboBan);
        hangSua(form, c, y++, cbMoi, cboMoi);
        hangSua(form, c, y++, cbChiSo, khungOpt);

        if (JOptionPane.showConfirmDialog(this, form, "Sửa nhiều món bán",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (!cbGia.isSelected() && !cbTien.isSelected()
                && !cbBan.isSelected() && !cbMoi.isSelected()
                && !cbChiSo.isSelected()) {
            note(WARN_RED, "Chưa tích ô nào — không có gì để đổi.");
            return;
        }
        Integer gia = null;
        if (cbGia.isSelected()) {
            try {
                gia = Integer.parseInt(fGia.getText().trim().replace(".", "").replace(",", ""));
            } catch (NumberFormatException ex) {
                note(WARN_RED, "Giá phải là số nguyên.");
                return;
            }
        }
        Integer loaiTien = cbTien.isSelected()
                ? MapShopDAO.LOAI_TIEN[Math.max(0, cboTien.getSelectedIndex())] : null;
        Boolean dangBan = cbBan.isSelected()
                ? cboBan.getSelectedIndex() == 0 : null;
        Boolean nhanMoi = cbMoi.isSelected()
                ? cboMoi.getSelectedIndex() == 0 : null;

        List<int[]> chiSo = null;
        if (cbChiSo.isSelected()) {
            chiSo = docBangChiSo(tOpt, mOpt);
            if (chiSo == null) {
                note(WARN_RED, "Bảng chỉ số phải là số nguyên.");
                return;
            }
        }

        String loi = MapShopDAO.suaNhieuItemShop(ids, gia, loaiTien, dangBan,
                nhanMoi, chiSo);

        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadItems();
        apDungShop("Đã sửa " + ids.size() + " món");
    }

    /** Mot hang trong bang sua nhieu: o tich ben trai, o nhap ben phai. */
    private static void hangSua(JPanel form, GridBagConstraints c, int y,
            JCheckBox tich, java.awt.Component o) {
        o.setEnabled(false);
        tich.addActionListener(e -> o.setEnabled(tich.isSelected()));
        c.gridx = 0;
        c.gridy = y;
        form.add(tich, c);
        c.gridx = 1;
        form.add(o, c);
    }

    private void itemXoa() {
        int[] rs = itemTable.getSelectedRows();
        if (rs.length == 0) {
            note(WARN_RED, "Chưa chọn món nào.");
            return;
        }
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        StringBuilder ten = new StringBuilder();
        for (int r : rs) {
            ids.add(intOf(itemModel.getValueAt(r, 0)));
            if (ten.length() < 200) {
                ten.append(ten.length() == 0 ? "" : ", ").append(itemModel.getValueAt(r, 3));
            }
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Gỡ " + ids.size() + " món khỏi cửa hàng?\n\n" + ten
                + (ten.length() >= 200 ? " …" : ""),
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        int xong = 0;
        for (int id : ids) {
            if (MapShopDAO.xoaItemShop(id)) {
                xong++;
            }
        }
        apDungShop("Đã gỡ " + xong + "/" + ids.size() + " món khỏi cửa hàng");
    }

    /**
     * Nạp lại bảng và đẩy vào bộ nhớ máy chủ.
     *
     * <p>{@code Manager.updateShop()} thay nguyên tham chiếu danh sách shop, nên
     * người chơi mở cửa hàng lần sau là thấy ngay — không cần khởi động lại.</p>
     */
    private void apDungShop(String msg) {
        loadItems();
        try {
            Manager.gI().updateShop();
            LamMoi.bao(LamMoi.SHOP);
            note(OK_GREEN, msg + " — đã áp dụng vào máy chủ ngay.");
        } catch (Exception ex) {
            note(WARN_RED, msg + " (CSDL đã lưu) nhưng KHÔNG nạp lại được: " + ex.getMessage());
        }
    }

    /** Cửa hàng đang xem — cửa hàng đầu tiên của NPC đang chọn. */
    private MapShopDAO.ShopRow shopDangChon() {
        return dsShop.isEmpty() ? null : dsShop.get(0);
    }

    /** Hộp thoại thêm thẻ hàng mới hoặc đổi tên thẻ đang chọn. */
    private void tabDialog(boolean them) {
        MapShopDAO.ShopRow shop = shopDangChon();
        if (shop == null) {
            note(WARN_RED, "NPC đang chọn không có cửa hàng nào.");
            return;
        }
        MapShopDAO.TabRow cu = (MapShopDAO.TabRow) cbTab.getSelectedItem();
        if (!them && cu == null) {
            note(WARN_RED, "Chưa chọn thẻ nào.");
            return;
        }
        String goi = JOptionPane.showInputDialog(this,
                them ? "Tên thẻ hàng mới cho cửa hàng \"" + shop.tagName + "\":"
                     : "Tên mới cho thẻ \"" + cu.name + "\":",
                them ? "" : cu.name);
        if (goi == null || goi.trim().isEmpty()) {
            return;
        }
        String loi = MapShopDAO.luuTab(them ? -1 : cu.id, shop.id, goi.trim());
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadShop();
        apDungShop(them ? "Đã thêm thẻ \"" + goi.trim() + "\""
                : "Đã đổi tên thẻ thành \"" + goi.trim() + "\"");
    }

    /**
     * Xoá thẻ hàng đang chọn.
     *
     * <p>Xoá thẻ là <b>xoá luôn mọi món trong thẻ</b>. Nói rõ số món sẽ mất chứ
     * không chỉ hỏi "có chắc không" — người bấm cần biết mình sắp xoá bao nhiêu.</p>
     */
    private void tabXoa() {
        MapShopDAO.TabRow t = (MapShopDAO.TabRow) cbTab.getSelectedItem();
        if (t == null) {
            note(WARN_RED, "Chưa chọn thẻ nào.");
            return;
        }
        int soMon = MapShopDAO.itemCuaTab(t.id).size();
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá thẻ \"" + t.name + "\"?\n\n"
                + "Sẽ xoá luôn " + soMon + " món hàng trong thẻ và chỉ số của chúng.\n"
                + "Không hoàn tác được.",
                "Xác nhận xoá thẻ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = MapShopDAO.xoaTab(t.id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadShop();
        apDungShop("Đã xoá thẻ \"" + t.name + "\" và " + soMon + " món hàng");
    }

    /**
     * Hộp thoại sửa cửa hàng, hoặc tạo cửa hàng mới cho NPC chưa có.
     *
     * <p>Kiểu cửa hàng quyết định <b>cách trả tiền</b>: bán thường thì trả bằng
     * vàng/ngọc/hồng ngọc theo cột "Bán theo" của từng món; hai kiểu còn lại trả
     * bằng <i>vật phẩm</i>, món phải trả xác định bởi cột {@code icon_spec}.</p>
     */
    /**
     * Đổi cửa hàng đang chọn sang kiểu hiện được giá.
     *
     * <p><b>Vấn đề.</b> Gói tin của cửa hàng <b>bán thường</b> chỉ có hai ô giá:
     * vàng và ngọc. Món bán theo Thỏi vàng hay Điểm sự kiện không có ô nào nên
     * client vẽ giá 0 — người chơi thấy "Nhận Miễn phí" dù máy chủ vẫn trừ đúng
     * tiền. Không phải lỗi hiển thị vặt: nó mời người ta bấm nhầm.</p>
     *
     * <p>Nút này đổi cửa hàng sang <b>đổi bằng vật phẩm</b> và điền cột
     * {@code icon_spec} cho mọi món theo loại tiền của nó.</p>
     */
    private void suaDeHienGia() {
        MapShopDAO.ShopRow shop = shopDangChon();
        if (shop == null) {
            note(WARN_RED, "Chưa chọn cửa hàng nào.");
            return;
        }
        if (shop.typeShop == 3) {
            int n = MapShopDAO.dongBoAnhTien(shop.id);
            note(OK_GREEN, "Cửa hàng đã ở kiểu hiện được giá. "
                    + "Đã điền lại ảnh loại tiền cho " + n + " món.");
            apDungShop(null);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Đổi cửa hàng \"" + nzs(shop.tagName) + "\" sang \""
                + MapShopDAO.tenKieuShop(3) + "\"?" + "\n" + "\n"
                + "Kiểu hiện tại chỉ hiện được giá bằng Vàng và Ngọc. Món bán theo"
                + " Thỏi vàng" + "\n" + "sẽ hiện giá 0 — người chơi thấy \"Nhận Miễn phí\""
                + " dù máy chủ vẫn trừ đúng tiền." + "\n" + "\n"
                + "Sau khi đổi, mọi món sẽ được gán ảnh loại tiền tương ứng.",
                "Sửa để hiện giá", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = MapShopDAO.luuShop(shop.id, shop.tagName, 3);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        int n = MapShopDAO.dongBoAnhTien(shop.id);
        apDungShop("Đã đổi sang \"" + MapShopDAO.tenKieuShop(3)
                + "\" và gán ảnh loại tiền cho " + n + " món — giá hiện đúng ngay.");
    }

    private void shopDialog() {
        MapShopDAO.NpcTren n = npcDangChon();
        if (n == null) {
            note(WARN_RED, "Chưa chọn NPC nào.");
            return;
        }
        MapShopDAO.ShopRow shop = shopDangChon();
        boolean them = shop == null;

        JTextField fTen = new JTextField(them ? "" : nzs(shop.tagName), 20);
        JComboBox<String> cbKieu = new JComboBox<>();
        for (int k : MapShopDAO.KIEU_SHOP) {
            cbKieu.addItem(MapShopDAO.tenKieuShop(k));
        }
        if (!them) {
            cbKieu.setSelectedItem(MapShopDAO.tenKieuShop(shop.typeShop));
        }

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("NPC:"), c);
        c.gridx = 1;
        p.add(new JLabel(n.ten + "  (id " + n.npcId + ")"), c);
        addRow(p, c, 1, "Tên cửa hàng:", fTen);
        c.gridx = 0;
        c.gridy = 2;
        p.add(new JLabel("Kiểu cửa hàng:"), c);
        c.gridx = 1;
        c.weightx = 1;
        cbKieu.setPreferredSize(new Dimension(260, 26));
        p.add(cbKieu, c);
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        p.add(new JLabel("<html><span style='color:#777'>"
                + "<b>Bán thường</b>: trả bằng tiền theo cột \"Bán theo\" của từng món.<br>"
                + "<b>Đổi bằng vật phẩm</b>: trả bằng món khác, xác định bởi cột "
                + "<code>icon_spec</code>."
                + "</span></html>"), c);

        int ok = JOptionPane.showConfirmDialog(this, p,
                them ? "Tạo cửa hàng cho NPC" : "Sửa cửa hàng",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        int kieu = MapShopDAO.KIEU_SHOP[Math.max(0, cbKieu.getSelectedIndex())];
        String loi = them
                ? MapShopDAO.themShop(n.npcId, fTen.getText().trim(), kieu)
                : MapShopDAO.luuShop(shop.id, fTen.getText().trim(), kieu);
        // Kieu 3 thanh toan qua icon_spec. Doi kieu ma khong dien cot do thi mua
        // se tra ra vat pham -1 — hong ngay, ma khong bao gi.
        if (loi == null && kieu == 3 && !them) {
            MapShopDAO.dongBoAnhTien(shop.id);
        }
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadNpc();
        loadShop();
        apDungShop(them ? "Đã tạo cửa hàng cho " + n.ten : "Đã sửa cửa hàng " + n.ten);
    }

    /**
     * Hộp thoại sửa <b>mẫu</b> NPC: tên và giao diện.
     *
     * <p>Khác nút "Sửa NPC" ở trên: nút kia đổi <i>NPC nào đứng ở đâu</i> trên
     * một bản đồ. Hộp này sửa chính mẫu NPC trong {@code npc_template}, nên đổi
     * là đổi cho <b>mọi bản đồ</b> có NPC đó.</p>
     *
     * <p>Đổi mẫu NPC cần khởi động lại máy chủ — {@code Manager} dựng NPC một
     * lần lúc nạp.</p>
     */
    /**
     * Đổi tên các mục trong menu của NPC đang chọn.
     *
     * <h3>Vì sao danh sách có thể rỗng</h3>
     *
     * <p>Tên các mục nằm rải rác trong hàng chục lớp NPC của mã nguồn, panel
     * không có cách nào biết trước. Máy chủ <b>ghi lại tên gốc lần đầu mục đó
     * được gửi cho người chơi</b>, nên phải mở NPC trong game một lần thì các
     * mục của nó mới hiện ra đây.</p>
     */
    private void npcMenuDialog() {
        MapShopDAO.NpcTren n = npcDangChon();
        if (n == null) {
            note(WARN_RED, "Chưa chọn NPC nào.");
            return;
        }
        java.util.List<nro.repository.dao.NpcMenuDAO.Muc> ds =
                new java.util.ArrayList<>();
        for (nro.repository.dao.NpcMenuDAO.Muc m
                : nro.repository.dao.NpcMenuDAO.danhSach()) {
            if (m.npcId == n.npcId) {
                ds.add(m);
            }
        }
        if (ds.isEmpty() && nro.repository.dao.NpcMenuThemDAO
                .danhSach(n.npcId).isEmpty()) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Chưa ghi nhận mục menu nào của NPC \"" + n.ten + "\".\n\n"
                    + "Tên các mục nằm trong mã nguồn nên panel không biết trước.\n"
                    + "Vào game mở NPC này một lần, rồi quay lại đây là thấy.\n\n"
                    + "Vẫn muốn THÊM một nút mới cho NPC này?",
                    "Chưa có dữ liệu", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                nutThemDialog(n.npcId, n.ten, null);
            }
            return;
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new java.awt.Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        java.util.List<JTextField> o = new java.util.ArrayList<>();
        int y = 0;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        JLabel huong = new JLabel("<html>Để trống = dùng lại tên gốc. "
                + "Gõ <b>\\n</b> để xuống dòng trong nút.</html>");
        huong.setForeground(GREY);
        form.add(huong, c);
        c.gridwidth = 1;

        for (nro.repository.dao.NpcMenuDAO.Muc m : ds) {
            c.gridx = 0;
            c.gridy = y++;
            form.add(new JLabel("Mục " + m.chi_soHienThi() + " — gốc: \""
                    + m.tenGoc.replace("\n", "\\n") + "\""), c);
            c.gridx = 1;
            JTextField f = new JTextField(
                    m.tenMoi == null ? "" : m.tenMoi, 26);
            o.add(f);
            form.add(f, c);
        }

        // ---- các nút THÊM của panel, nối vào cuối menu
        java.util.List<nro.repository.dao.NpcMenuThemDAO.Nut> nutThem =
                nro.repository.dao.NpcMenuThemDAO.danhSach(n.npcId);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        JLabel tieuDeThem = new JLabel("<html><br><b>Nút thêm</b> — nối vào "
                + "<i>cuối</i> menu. Chèn giữa sẽ làm lệch vị trí các mục gốc "
                + "và NPC chạy nhầm chức năng.</html>");
        tieuDeThem.setForeground(GREY);
        form.add(tieuDeThem, c);
        for (nro.repository.dao.NpcMenuThemDAO.Nut nt : nutThem) {
            c.gridy = y++;
            JPanel hang = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            hang.setOpaque(false);
            hang.add(new JLabel((nt.bat ? "" : "[tắt] ") + "\""
                    + nt.ten.replace("\n", "\\n") + "\" → " + nt.moTaHanhDong()));
            hang.add(button("Sửa", ACCENT, e -> nutThemDialog(n.npcId, n.ten, nt)));
            hang.add(button("Xoá", WARN_RED, e -> {
                if (JOptionPane.showConfirmDialog(this, "Xoá nút \"" + nt.ten + "\"?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    nro.repository.dao.NpcMenuThemDAO.xoa(nt.id);
                    note(OK_GREEN, "Đã xoá nút. Mở lại hộp thoại để thấy danh sách mới.");
                }
            }));
            form.add(hang, c);
        }
        c.gridy = y++;
        form.add(button("Thêm nút mới…", OK_GREEN,
                e -> nutThemDialog(n.npcId, n.ten, null)), c);
        c.gridwidth = 1;

        if (JOptionPane.showConfirmDialog(this, form,
                "Menu NPC #" + n.npcId + " " + n.ten,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int loi = 0;
        for (int i = 0; i < ds.size(); i++) {
            String kq = nro.repository.dao.NpcMenuDAO.datTen(
                    ds.get(i).npcId, ds.get(i).chiSo, o.get(i).getText());
            if (kq != null) {
                loi++;
            }
        }
        note(loi == 0 ? OK_GREEN : WARN_RED, loi == 0
                ? "Đã lưu tên mục menu — có hiệu lực ngay, không cần khởi động lại."
                : "Có " + loi + " mục không lưu được, xem log máy chủ.");
    }

    /**
     * Thêm hoặc sửa một nút phụ của NPC.
     *
     * @param nut {@code null} nghĩa là thêm mới
     */
    private void nutThemDialog(int npcId, String tenNpc,
            nro.repository.dao.NpcMenuThemDAO.Nut nut) {
        final boolean them = nut == null;
        final nro.repository.dao.NpcMenuThemDAO.Nut n =
                them ? new nro.repository.dao.NpcMenuThemDAO.Nut() : nut;
        if (them) {
            n.npcId = npcId;
            n.thuTu = nro.repository.dao.NpcMenuThemDAO.danhSach(npcId).size();
        }

        JTextField fTen = new JTextField(n.ten, 22);
        JComboBox<String> cbHd = new JComboBox<>(new String[]{
            "Mở cửa hàng", "Hiện thông báo"});
        cbHd.setSelectedIndex(
                nro.repository.dao.NpcMenuThemDAO.THONG_BAO.equals(n.hanhDong) ? 1 : 0);

        // Danh sách tag cửa hàng có thật, để khỏi gõ sai tên.
        java.util.List<String> tags = new java.util.ArrayList<>();
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT tag_name FROM shop ORDER BY tag_name");
            while (rs.next()) {
                tags.add(rs.getString("tag_name"));
            }
            rs.dispose();
        } catch (Exception ignored) {
        }
        JComboBox<String> cbShop = new JComboBox<>(tags.toArray(new String[0]));
        cbShop.setEditable(true);
        cbShop.setSelectedItem(n.thamSo);
        JTextField fChu = new JTextField(n.thamSo, 26);
        JCheckBox cbBat = new JCheckBox("Bật", n.bat);
        JTextField fThuTu = new JTextField(String.valueOf(n.thuTu), 5);

        JPanel theTham = new JPanel(new java.awt.CardLayout());
        theTham.add(cbShop, "shop");
        theTham.add(fChu, "chu");
        final Runnable doiThe = () -> ((java.awt.CardLayout) theTham.getLayout())
                .show(theTham, cbHd.getSelectedIndex() == 1 ? "chu" : "shop");
        doiThe.run();
        cbHd.addActionListener(e -> doiThe.run());

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new java.awt.Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Chữ trên nút:"), g);
        g.gridx = 1;
        p.add(fTen, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Bấm vào thì:"), g);
        g.gridx = 1;
        p.add(cbHd, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Cửa hàng / câu chữ:"), g);
        g.gridx = 1;
        p.add(theTham, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Thứ tự:"), g);
        g.gridx = 1;
        p.add(fThuTu, g);
        g.gridx = 1;
        g.gridy = ++y;
        p.add(cbBat, g);
        g.gridx = 0;
        g.gridy = ++y;
        g.gridwidth = 2;
        JLabel gc = new JLabel("<html>Gõ <b>\\n</b> để xuống dòng trong nút. "
                + "Nút luôn nằm ở cuối menu.</html>");
        gc.setForeground(GREY);
        p.add(gc, g);

        if (JOptionPane.showConfirmDialog(this, p,
                (them ? "Thêm nút cho " : "Sửa nút của ") + tenNpc,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        n.ten = fTen.getText();
        n.bat = cbBat.isSelected();
        n.hanhDong = cbHd.getSelectedIndex() == 1
                ? nro.repository.dao.NpcMenuThemDAO.THONG_BAO
                : nro.repository.dao.NpcMenuThemDAO.MO_SHOP;
        n.thamSo = cbHd.getSelectedIndex() == 1 ? fChu.getText()
                : String.valueOf(cbShop.getSelectedItem());
        try {
            n.thuTu = Integer.parseInt(fThuTu.getText().trim());
        } catch (NumberFormatException ex) {
            n.thuTu = 0;
        }

        String loi = them ? nro.repository.dao.NpcMenuThemDAO.them(n)
                : nro.repository.dao.NpcMenuThemDAO.sua(n);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        LamMoi.bao(LamMoi.SHOP);
        note(OK_GREEN, them
                ? "Đã thêm nút — vào game mở NPC là thấy ngay."
                : "Đã lưu nút.");
    }

    private void npcMauDialog() {
        MapShopDAO.NpcTren n = npcDangChon();
        if (n == null) {
            note(WARN_RED, "Chưa chọn NPC nào.");
            return;
        }
        int[] gd = MapShopDAO.giaoDienNpc(n.npcId);

        JTextField fTen = new JTextField(n.ten, 22);
        JTextField fHead = new JTextField(String.valueOf(gd[0]), 8);
        JTextField fBody = new JTextField(String.valueOf(gd[1]), 8);
        JTextField fLeg = new JTextField(String.valueOf(gd[2]), 8);
        JTextField fAvatar = new JTextField(String.valueOf(gd[3]), 8);
        JTextField fChao = new JTextField(MapShopDAO.loiChao(n.npcId), 30);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Tên NPC:", fTen);
        addRow(form, c, 1, "Head (part id):", fHead);
        addRow(form, c, 2, "Body (part id):", fBody);
        addRow(form, c, 3, "Leg (part id):", fLeg);
        addRow(form, c, 4, "Avatar (ảnh nói chuyện):", fAvatar);
        addRow(form, c, 5, "Câu chào khi nói chuyện:", fChao);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "Đây là <b>mẫu NPC dùng chung</b> — đổi là đổi cho mọi bản đồ có NPC này.<br>"
                + "Đặt <code>-1</code> nghĩa là không có phần đó.<br>"
                + "Part id phải có thật trong bảng <code>part</code>, nếu không client "
                + "không vẽ được — dùng tab \"Kiểm tra dữ liệu\" để dò.<br>"
                + "<b>Câu chào</b> có hiệu lực <b>ngay</b>, không cần khởi động lại. "
                + "Để trống thì dùng câu mặc định \""
                + MapShopDAO.LOI_CHAO_MAC_DINH + "\".<br>"
                + "Câu chào chỉ áp dụng cho NPC <b>có cửa hàng đặt từ panel</b>; "
                + "NPC viết cứng trong mã nguồn vẫn dùng câu của nó.<br>"
                + "<b>Cần khởi động lại máy chủ</b> để áp dụng phần hình ảnh."
                + "</span></html>"), c);

        int ok = JOptionPane.showConfirmDialog(this, form,
                "Sửa mẫu NPC #" + n.npcId, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        int head;
        int body;
        int leg;
        int avatar;
        try {
            head = Integer.parseInt(fHead.getText().trim());
            body = Integer.parseInt(fBody.getText().trim());
            leg = Integer.parseInt(fLeg.getText().trim());
            avatar = Integer.parseInt(fAvatar.getText().trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Head, Body, Leg, Avatar phải là số nguyên.");
            return;
        }
        String loi = MapShopDAO.luuNpcMau(n.npcId, fTen.getText().trim(),
                head, body, leg, avatar, fChao.getText());
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadNpc();
        LamMoi.bao(LamMoi.SHOP);
        note(OK_GREEN, "Đã sửa mẫu NPC #" + n.npcId
                + " — khởi động lại máy chủ để áp dụng.");
    }

    /** Xoá cửa hàng của NPC đang chọn, kèm mọi thẻ và món trong đó. */
    private void shopXoa() {
        MapShopDAO.ShopRow shop = shopDangChon();
        if (shop == null) {
            note(WARN_RED, "NPC đang chọn không có cửa hàng nào.");
            return;
        }
        int soThe = MapShopDAO.tabCuaShop(shop.id).size();
        int soMon = 0;
        for (MapShopDAO.TabRow t : MapShopDAO.tabCuaShop(shop.id)) {
            soMon += MapShopDAO.itemCuaTab(t.id).size();
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá cửa hàng \"" + shop.tagName + "\"?\n\n"
                + "Sẽ xoá luôn " + soThe + " thẻ hàng và " + soMon + " món trong đó.\n"
                + "Không hoàn tác được.",
                "Xác nhận xoá cửa hàng", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = MapShopDAO.xoaShop(shop.id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadNpc();
        loadShop();
        apDungShop("Đã xoá cửa hàng và " + soMon + " món hàng");
    }

    private static String nzs(String s) {
        return s == null ? "" : s;
    }

    // =====================================================================
    //  Tiện ích
    // =====================================================================

    private static String soThuan(Object o) {
        return String.valueOf(o).replace(".", "").replace(",", "").trim();
    }

    private static int intOf(Object o) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
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
        c.weightx = 0;
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
