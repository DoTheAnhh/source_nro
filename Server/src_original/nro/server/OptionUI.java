package nro.server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Enumeration;

public class OptionUI extends JPanel {
    // --- DB ---
    private Connection conn;

    // --- UI: top labels/status ---
    private final JLabel aboutLabel = new JLabel("Options Manager");
    private final JLabel statusLabel = new JLabel("Chưa kết nối");

    // --- Combo chọn shop/tab ---
    private final JComboBox<Shop> cbShop = new JComboBox<>();
    private final JComboBox<Tab> cbTab = new JComboBox<>();

    // --- Bảng items ---
    private final DefaultTableModel tm = new DefaultTableModel(
            new String[]{
                    "idx",
                    "shop_id",
                    "shop_name",
                    "tab_id",
                    "tab_index",
                    "temp_id",
                    "is_new",
                    "cost",
                    "item_spec",
                    "type_sell",
                    "is_sell"
            },
            0
    ) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tm);

    // --- Form nhập ---
    private final JSpinner spTemp = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
    private final JCheckBox chkNew = new JCheckBox("", false);
    private final JSpinner spCost = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000_000, 1));
    private final JSpinner spSpec = new JSpinner(new SpinnerNumberModel(0, -1_000_000, 1_000_000, 1));
    private final JSpinner spType = new JSpinner(new SpinnerNumberModel(0, -128, 127, 1));
    private final JCheckBox chkSell = new JCheckBox("", true);

    // --- Bảng options ---
    private final DefaultTableModel tmOpts = new DefaultTableModel(new String[]{"option_id", "param"}, 0);
    private final JTable tblOpts = new JTable(tmOpts);

    // --- Nút/toolbar giữ tham chiếu để enable/disable khi chưa kết nối ---
    private JButton btnReload;
    private JButton btnAddItem;
    private JButton btnSaveRow;
    private JButton btnDelItem;

    public OptionUI() {
        initUI();
        setControlsEnabled(false); // chưa có DB -> khoá tác vụ
    }

    public OptionUI(Connection conn) {
        this();
        this.conn = conn;
        if (this.conn != null) {
            afterConnected();
        }
    }

    // -------------------------------------------
    // UI build
    // -------------------------------------------
    private void initUI() {
        setLayout(new BorderLayout(6, 6));

        // ----- MENU BAR (đặt bên trong panel) -----
        JMenuBar menuBar = buildMenuBar();

        // ----- ABOUT + STATUS -----
        JPanel aboutPanel = new JPanel(new BorderLayout());
        aboutPanel.setBorder(new EmptyBorder(6, 10, 6, 10));
        aboutLabel.setFont(aboutLabel.getFont().deriveFont(Font.PLAIN, aboutLabel.getFont().getSize() + 1f));
        aboutPanel.add(aboutLabel, BorderLayout.WEST);
        aboutPanel.add(statusLabel, BorderLayout.EAST);

        // ----- TOOLBAR -----
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton btnConnect = new JButton("Kết nối DB");
        btnReload = new JButton("Tải lại");
        cbShop.setPreferredSize(new Dimension(280, 34));
        cbTab.setPreferredSize(new Dimension(280, 34));

        toolbar.add(btnConnect);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(new JLabel("Shop:"));
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(cbShop);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(new JLabel("Tab:"));
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(cbTab);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(btnReload);

        // ----- TABLE (items) -----
        table.setRowHeight(Math.max(28, Math.round(UIManager.getFont("Table.font").getSize2D() * 1.8f)));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tm.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        int[] widths = {60, 70, 150, 70, 80, 150, 80, 70, 90, 90, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane spTable = new JScrollPane(table);
        spTable.setBorder(new EmptyBorder(8, 12, 8, 12));

        // ----- FORM -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(8, 12, 8, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.anchor = GridBagConstraints.WEST;

        int y = 0;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("temp_id:"), g);
        g.gridx = 1; form.add(spTemp, g);
        g.gridx = 2; form.add(new JLabel("is_new:"), g);
        g.gridx = 3; form.add(chkNew, g);
        g.gridx = 4; form.add(new JLabel("cost:"), g);
        g.gridx = 5; form.add(spCost, g);

        y++;
        g.gridx = 0; g.gridy = y; form.add(new JLabel("item_spec:"), g);
        g.gridx = 1; form.add(spSpec, g);
        g.gridx = 2; form.add(new JLabel("type_sell:"), g);
        g.gridx = 3; form.add(spType, g);
        g.gridx = 4; form.add(new JLabel("is_sell:"), g);
        g.gridx = 5; form.add(chkSell, g);

        // ----- OPTIONS TABLE -----
        JPanel optsPanel = new JPanel(new BorderLayout(6, 6));
        optsPanel.setBorder(BorderFactory.createTitledBorder("Menu Options"));
        tblOpts.setRowHeight(24);
        JScrollPane spOpts = new JScrollPane(tblOpts);
        JPanel obtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JButton addOpt = new JButton("Thêm Options");
        JButton delOpt = new JButton("Xóa Options");
        obtn.add(addOpt);
        obtn.add(delOpt);
        optsPanel.add(spOpts, BorderLayout.CENTER);
        optsPanel.add(obtn, BorderLayout.SOUTH);

        // ----- ACTION BUTTONS -----
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        btnAddItem = new JButton("Thêm item");
        btnDelItem = new JButton("Xóa item");
        btnSaveRow = new JButton("Lưu dòng");
        btnAddItem.setToolTipText("Ctrl/Cmd+N");
        btnSaveRow.setToolTipText("Ctrl/Cmd+S");
        btnDelItem.setToolTipText("Delete");
        actions.add(btnAddItem);
        actions.add(btnSaveRow);
        actions.add(btnDelItem);

        // ----- SOUTH (form + options + actions) -----
        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.NORTH);
        south.add(optsPanel, BorderLayout.CENTER);
        south.add(actions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spTable, south);
        split.setResizeWeight(0.6);
        split.setContinuousLayout(true);

        // ----- TOP CONTAINER (menu + about + toolbar) -----
        JPanel top = new JPanel(new BorderLayout());
        top.add(menuBar, BorderLayout.NORTH);
        top.add(aboutPanel, BorderLayout.CENTER);
        top.add(toolbar, BorderLayout.SOUTH);

        // Layout vào panel
        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        // ----- EVENTS -----
        // toolbar/menu
        btnConnect.addActionListener(e -> showLoginDialog());
        btnReload.addActionListener(e -> loadShops());
        // combobox
        cbShop.addActionListener(e -> loadTabs());
        cbTab.addActionListener(e -> loadItems());
        // table
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) fillForm();
            }
        });
        // options
        addOpt.addActionListener(e -> tmOpts.addRow(new Object[]{"0", "0"}));
        delOpt.addActionListener(e -> {
            int r = tblOpts.getSelectedRow();
            if (r != -1) tmOpts.removeRow(r);
        });
        // actions
        btnAddItem.addActionListener(e -> appendFromForm());
        btnSaveRow.addActionListener(e -> saveSelectedRow());
        btnDelItem.addActionListener(e -> deleteSelected());

        // keyboard shortcuts (đăng ký trực tiếp trên panel để tránh getRootPane() null)
        registerKeyboardAction(
                e -> appendFromForm(),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
        registerKeyboardAction(
                e -> deleteSelected(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
        registerKeyboardAction(
                e -> saveSelectedRow(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    // -------------------------------------------
    // MENU
    // -------------------------------------------
    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu mHelp = new JMenu("Trợ giúp");
        mHelp.add(new JMenuItem(new AbstractAction("Phông chữ") {
            @Override
            public void actionPerformed(ActionEvent e) {showFontDialog(); }
        }));
        mHelp.add(new JMenuItem(new AbstractAction("Hướng dẫn") {
            @Override
            public void actionPerformed(ActionEvent e) {showAbout(); }
        }));
        mb.add(mHelp);

        JMenu mTb = new JMenu("Thông báo");
        mTb.add(new JMenuItem(new AbstractAction("Thông Tin Chủ Sources") {
            @Override
            public void actionPerformed(ActionEvent e) {ThongTin(); }
        }));
        mb.add(mTb);

        return mb;
    }

    // -------------------------------------------
    // Kết nối DB (dialog) + sau kết nối
    // -------------------------------------------
    private void showLoginDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog d = (owner != null)
                ? new JDialog(owner, "Đăng nhập MySQL", Dialog.ModalityType.APPLICATION_MODAL)
                : new JDialog((Frame) null, "Đăng nhập MySQL", true);

        JTextField host = new JTextField("localhost", 12);
        JTextField port = new JTextField("3306", 6);
        JTextField db = new JTextField("", 12);
        JTextField user = new JTextField("root", 10);
        JPasswordField pass = new JPasswordField("", 10);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(16, 16, 0, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; p.add(new JLabel("Host:"), gc);
        gc.gridx = 1; p.add(host, gc);
        gc.gridx = 0; gc.gridy = 1; p.add(new JLabel("Port:"), gc);
        gc.gridx = 1; p.add(port, gc);
        gc.gridx = 0; gc.gridy = 2; p.add(new JLabel("Database:"), gc);
        gc.gridx = 1; p.add(db, gc);
        gc.gridx = 0; gc.gridy = 3; p.add(new JLabel("User:"), gc);
        gc.gridx = 1; p.add(user, gc);
        gc.gridx = 0; gc.gridy = 4; p.add(new JLabel("Password:"), gc);
        gc.gridx = 1; p.add(pass, gc);

        JButton ok = new JButton("Kết nối");
        ok.setToolTipText("Kết nối và khởi tạo dữ liệu mẫu nếu trống");
        JButton cancel = new JButton("Hủy");
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bp.add(cancel);
        bp.add(ok);

        d.add(p, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(owner);

        ok.addActionListener(e -> {
            String url = "jdbc:mysql://" + host.getText().trim() + ":" + port.getText().trim() + "/" +
                    db.getText().trim() + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
            try {
                conn = DriverManager.getConnection(url, user.getText().trim(), new String(pass.getPassword()));
                ensureTables();
                sampleIfEmpty();
                d.dispose();
                afterConnected();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> d.dispose());
        d.getRootPane().setDefaultButton(ok);
        d.setVisible(true);
    }

    private void afterConnected() {
        setControlsEnabled(true);
        loadShops();
        statusLabel.setText("Đã kết nối");
    }

    private void setControlsEnabled(boolean ok) {
        cbShop.setEnabled(ok);
        cbTab.setEnabled(ok);
        if (btnReload != null) btnReload.setEnabled(ok);
        if (btnAddItem != null) btnAddItem.setEnabled(ok);
        if (btnSaveRow != null) btnSaveRow.setEnabled(ok);
        if (btnDelItem != null) btnDelItem.setEnabled(ok);
    }

    // -------------------------------------------
    // DB ensure + sample
    // -------------------------------------------
    private void ensureTables() {
        try (Statement st = conn.createStatement()) {
            // Bảng shop
            st.execute("CREATE TABLE IF NOT EXISTS shop (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "tag VARCHAR(255) DEFAULT ''" +
                    ") ENGINE=InnoDB");

            // Bảng tab_shop
            st.execute("CREATE TABLE IF NOT EXISTS tab_shop (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "shop_id INT NOT NULL," +
                    "name VARCHAR(255) DEFAULT ''," +
                    "FOREIGN KEY (shop_id) REFERENCES shop(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            // Bảng item_shop
            st.execute("CREATE TABLE IF NOT EXISTS item_shop (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "tab_id INT NOT NULL," +
                    "temp_id INT NOT NULL," +
                    "is_new TINYINT(1) DEFAULT 1," +
                    "is_sell TINYINT(1) DEFAULT 1," +
                    "type_sell INT DEFAULT 1," +
                    "cost INT DEFAULT 0," +
                    "costgold INT DEFAULT 0," +
                    "icon_spec INT DEFAULT 0," +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (tab_id) REFERENCES tab_shop(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            // Bảng item_shop_option
            st.execute("CREATE TABLE IF NOT EXISTS item_shop_option (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "item_shop_id INT NOT NULL," +
                    "option_id INT NOT NULL," +
                    "param INT NOT NULL," +
                    "FOREIGN KEY (item_shop_id) REFERENCES item_shop(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadTabs() {
        if (conn == null) return;
        cbTab.removeAllItems();
        Shop s = (Shop) cbShop.getSelectedItem();
        if (s == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, NAME FROM tab_shop WHERE shop_id=? ORDER BY id")) {
            ps.setInt(1, s.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cbTab.addItem(new Tab(rs.getInt("id"), rs.getString("NAME")));
                }
            }
        } catch (SQLException ex) {
            show("Lỗi load tab: " + ex.getMessage());
        }
        if (cbTab.getItemCount() > 0) cbTab.setSelectedIndex(0);
    }


    private void sampleIfEmpty() {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM shop");
            rs.next();
            if (rs.getInt(1) == 0) {
                st.execute("INSERT INTO shop(tag) VALUES ('Sample Shop')");
                ResultSet rs2 = st.executeQuery("SELECT id FROM shop LIMIT 1");
                if (rs2.next()) {
                    int sid = rs2.getInt(1);
                    try (PreparedStatement p2 = conn.prepareStatement(
                            "INSERT INTO tab_shop(shop_id,name) VALUES (?,?)")) {
                        p2.setInt(1, sid);
                        p2.setString(2, "Tab 0");
                        p2.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadShops() {
        if (conn == null) return;
        cbShop.removeAllItems();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,tag_name FROM shop ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cbShop.addItem(new Shop(rs.getInt(1), rs.getString(2)));
        } catch (SQLException ex) {
            show("Lỗi load shop: " + ex.getMessage());
        }
        if (cbShop.getItemCount() > 0) cbShop.setSelectedIndex(0);
    }

    private void loadItems() {
        if (conn == null) return;
        tm.setRowCount(0);
        Tab t = (Tab) cbTab.getSelectedItem();
        Shop s = (Shop) cbShop.getSelectedItem();
        if (t == null || s == null) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM item_shop WHERE tab_id=? AND is_sell=1 ORDER BY create_time DESC"
        )) {
            ps.setInt(1, t.id);
            try (ResultSet rs = ps.executeQuery()) {
                int idx = 0;
                while (rs.next()) {
                    int itemId = rs.getInt("id");
                    String options = loadOptionsAsString(itemId);
                    tm.addRow(new Object[]{
                            idx++,
                            s.id,
                            s.tag,
                            t.id,
                            t.name, // ❌ bỏ t.index, chỉ giữ id + name
                            rs.getInt("temp_id"),
                            rs.getBoolean("is_new"),
                            rs.getInt("cost"),
                            rs.getInt("icon_spec"),
                            rs.getInt("type_sell"),
                            rs.getBoolean("is_sell"),
                            options
                    });
                }
            }
        } catch (SQLException ex) {
            show("Lỗi load items: " + ex.getMessage());
        }
    }
  
    private String loadOptionsAsString(int itemShopId) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT option_id, param FROM item_shop_option WHERE item_shop_id=?"
        )) {
            ps.setInt(1, itemShopId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append(rs.getInt("option_id")).append(":").append(rs.getInt("param"));
                    first = false;
                }
            }
        }
        return sb.toString();
    }
    
    // -------------------------------------------
    // Form helpers + CRUD
    // -------------------------------------------
    private void fillForm() {
        int r = table.getSelectedRow();
        if (r == -1) return;
        int m = table.convertRowIndexToModel(r);

        spTemp.setValue(parseIntSafe(tm.getValueAt(m, 5), 0));
        chkNew.setSelected(parseBoolSafe(tm.getValueAt(m, 6), false));
        spCost.setValue(parseIntSafe(tm.getValueAt(m, 7), 0));
        spSpec.setValue(parseIntSafe(tm.getValueAt(m, 8), 0));
        spType.setValue(parseIntSafe(tm.getValueAt(m, 9), 0));
        chkSell.setSelected(parseBoolSafe(tm.getValueAt(m, 10), true));

        // Xóa phần opts nếu không có cột options
        tmOpts.setRowCount(0);
    }


    private int parseIntSafe(Object v, int def) {
        try { return Integer.parseInt(String.valueOf(v).trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private boolean parseBoolSafe(Object v, boolean def) {
        try { return Boolean.parseBoolean(String.valueOf(v).trim()); }
        catch (Exception e) { return def; }
    }

    private void appendFromForm() {
        if (conn == null) { 
            show("Chưa kết nối DB"); 
            return; 
        }
        Tab t = (Tab) cbTab.getSelectedItem();
        if (t == null) { 
            show("Chọn tab"); 
            return; 
        }

        try {
            // Chuẩn bị lệnh INSERT
            String sql = "INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, costgold, icon_spec) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, t.id);
                ps.setInt(2, spTemp.getComponentCount());
                ps.setInt(3, chkNew.isSelected() ? 1 : 0);
                ps.setInt(4, chkSell.isSelected() ? 1 : 0);
                ps.setInt(5, spType.getComponentCount());
                ps.setInt(6, spCost.getComponentCount());
                ps.setInt(7, spCost.getComponentCount());
                ps.setInt(8, spSpec.getComponentCount());

                ps.executeUpdate();
            }

            loadItems();
            clearForm();
            statusLabel.setText("Đã thêm item mới");

        } catch (Exception ex) {
            show("Lỗi thêm: " + ex.getMessage());
        }
    }


    private void saveSelectedRow() {
        if (conn == null) { show("Chưa kết nối DB"); return; }
        int r = table.getSelectedRow();
        if (r == -1) { show("Chọn dòng để lưu"); return; }

        int modelRow = table.convertRowIndexToModel(r);
        int id = parseIntSafe(tm.getValueAt(modelRow, 0), -1);
        if (id < 0) { show("ID không hợp lệ"); return; }

        try (PreparedStatement up = conn.prepareStatement(
                "UPDATE item_shop SET temp_id=?, is_new=?, is_sell=?, type_sell=?, cost=?, costgold=?, icon_spec=? WHERE id=?")) {
            up.setInt(1, (Integer) spTemp.getValue());
            up.setBoolean(2, chkNew.isSelected());
            up.setBoolean(3, chkSell.isSelected());
            up.setInt(4, (Integer) spType.getValue());
            up.setInt(5, (Integer) spCost.getValue());
            up.setInt(6, (Integer) 0);
            up.setInt(7, (Integer) spSpec.getValue());
            up.setInt(8, id);

            up.executeUpdate();
        } catch (Exception ex) {
            show("Lỗi lưu: " + ex.getMessage());
        }

        loadItems(); // reload lại table
        clearForm();
        statusLabel.setText("Đã lưu");
    }

    
    private void deleteSelected() {
        if (conn == null) { show("Chưa kết nối DB"); return; }

        int r = table.getSelectedRow();
        if (r == -1) { show("Chọn item để xóa"); return; }

        int modelRow = table.convertRowIndexToModel(r);
        int id = parseIntSafe(tm.getValueAt(modelRow, 0), -1); // id trong bảng item_shop
        if (id < 0) { show("ID không hợp lệ"); return; }

        if (JOptionPane.showConfirmDialog(this, 
                "Xóa item id " + id + "?",
                "Xác nhận", 
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM item_shop WHERE id=?")) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                loadItems();
                clearForm();
                statusLabel.setText("Đã xóa");
            } else {
                show("Không tìm thấy item id=" + id);
            }
        } catch (Exception ex) {
            show("Lỗi xóa: " + ex.getMessage());
        }
    }

    private void clearForm() {
        spTemp.setValue(0);
        chkNew.setSelected(false);
        spCost.setValue(0);
        spSpec.setValue(0);
        spType.setValue(0);
        chkSell.setSelected(true);
        tmOpts.setRowCount(0);
    }
    // -------------------------------------------
    // Helpers: fonts / about / notify
    // -------------------------------------------

    private void showFontDialog() {
        String input = JOptionPane.showInputDialog(this, "Nhập kích thước font (px):", 14);
        if (input != null && !input.isBlank()) {
            try {
                int size = Integer.parseInt(input.trim());
                String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                String fontFamily = (String) JOptionPane.showInputDialog(
                        this,
                        "Chọn kiểu chữ:",
                        "Kiểu chữ",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        availableFonts,
                        UIManager.getFont("Label.font").getFamily()
                );
                if (fontFamily == null || fontFamily.isBlank()) {
                    fontFamily = UIManager.getFont("Label.font").getFamily();
                }
                FontUIResource f = new FontUIResource(new Font(fontFamily, Font.PLAIN, size));
                Enumeration<?> keys = UIManager.getDefaults().keys();
                while (keys.hasMoreElements()) {
                    Object k = keys.nextElement();
                    Object v = UIManager.get(k);
                    if (v instanceof Font) UIManager.put(k, f);
                }
                SwingUtilities.updateComponentTreeUI(this);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Giá trị không hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAbout() {
        String html = "<html>" +
                "<h2 style='color:#1E90FF;margin:4px 0'>🛒 Shop Manager Controller</h2>" +
                "<div style='color:#444;'>Quản lý Shop dễ dàng</div><hr>" +

                "<b style='color:#FF4500;'>⚡ Phím tắt:</b><ul>" +
                "<li><span style='color:#008000;'><b>Ctrl/Cmd + N</b></span>: Thêm item</li>" +
                "<li><span style='color:#B22222;'><b>Delete</b></span>: Xóa item đang chọn</li>" +
                "<li><span style='color:#1E90FF;'><b>Ctrl/Cmd + S</b></span>: Lưu dòng đang chọn</li>" +
                "</ul>" +

                "<b style='color:#9932CC;'>💡 Mẹo:</b><br>" +
                "<span style='color:#444;'>Nhấp đúp vào dòng để đổ dữ liệu xuống form chỉnh sửa</span>" +
                "</html>";

        JOptionPane.showMessageDialog(
                this,
                new JLabel(html),
                "📘 Hướng dẫn",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
   
    private void ThongTin() {
        String guide =
                "Tao Là Bố";
        JOptionPane.showMessageDialog(this, guide, "Thông tin chủ source", JOptionPane.INFORMATION_MESSAGE);
    }

    private void show(String s) {
        JOptionPane.showMessageDialog(this, s, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------------------------------
    // Types
    // -------------------------------------------
    private static class Shop {
        int id;
        String tag;
        Shop(int id, String tag) { this.id = id; this.tag = tag; }
        @Override
        public String toString() { return "[" + id + "] " + (tag == null ? "" : tag); }
    }

    private static class Tab {
        int id;
        String name;

        Tab(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "id=" + id + " - " + (name == null ? "" : name.replace("<>", " | "));
        }
    }
}
