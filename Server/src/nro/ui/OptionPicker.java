package nro.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import nro.entity.template.ItemOptionTemplate;
import nro.server.Manager;

/**
 * Ô chọn <b>chỉ số</b> (option) — chọn từ danh sách thay vì gõ id.
 *
 * <p>Bảng {@code item_option_template} có 273 dòng và id không gợi ý gì về ý
 * nghĩa: {@code 6} là {@code HP+#}, {@code 30} là khoá vật phẩm, {@code 50} là
 * sức đánh phần trăm. Bắt người dùng nhớ dãy số đó là cách chắc chắn nhất để
 * đặt nhầm chỉ số.</p>
 *
 * <p>Lớp này gắn một ô chọn vào một cột của {@link JTable}. Ô chọn hiển thị
 * {@code "6 — HP+#"} nhưng <b>ghi vào bảng đúng con số</b> {@code "6"}, nên phần
 * dựng JSON phía sau không phải đổi gì.</p>
 */
public final class OptionPicker {

    private OptionPicker() {
    }

    /** Một chỉ số trong danh sách chọn. */
    public static final class Opt {

        public final int id;
        public final String name;

        Opt(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " — " + name;
        }
    }

    /**
     * Toàn bộ chỉ số, sắp theo id.
     *
     * <p>Bỏ những dòng không có tên thật: bảng được đổ đầy theo chỉ số nên các
     * id trống mang tên giữ chỗ, đưa vào danh sách chọn chỉ làm rối.</p>
     *
     * <p>Hết bảng trong bộ nhớ thì đọc thẳng {@code item_option_template}. Panel
     * dựng giao diện <b>trước</b> lúc máy chủ nạp xong bảng chỉ số, nên nếu chỉ
     * trông vào {@code Manager.ITEM_OPTION_TEMPLATES} thì lúc gắn ô chọn danh
     * sách còn rỗng — và ô chọn im lặng biến thành ô gõ tay.</p>
     */
    public static List<Opt> all() {
        List<Opt> out = new ArrayList<>();
        try {
            if (Manager.ITEM_OPTION_TEMPLATES != null) {
                for (int i = 0; i < Manager.ITEM_OPTION_TEMPLATES.size(); i++) {
                    ItemOptionTemplate t = Manager.ITEM_OPTION_TEMPLATES.get(i);
                    if (t == null || t.name == null || t.name.trim().isEmpty()
                            || "(khong co)".equals(t.name)) {
                        continue;
                    }
                    out.add(new Opt(t.id, t.name));
                }
            }
        } catch (Exception ignored) {
            // Bảng chưa nạp xong -> rơi xuống nhánh đọc CSDL.
        }
        if (out.isEmpty()) {
            for (java.util.Map.Entry<Integer, String> e
                    : nro.repository.dao.SachTuyetKyDAO.khoTenChiSo().entrySet()) {
                if (e.getValue() != null && !e.getValue().trim().isEmpty()) {
                    out.add(new Opt(e.getKey(), e.getValue()));
                }
            }
        }
        return out;
    }

    /**
     * Chỉ số đánh dấu <b>đồ khoá</b> — id 30, tên trong bảng là "Không thể giao dịch".
     *
     * <p>Gom thành hằng số vì con số này rải ở mọi chỗ phát đồ: phát tay từ panel,
     * đồ rơi từ boss, đồ rơi từ quái, giftcode, quà phúc lợi, món bày trong shop.
     * Gõ thẳng 30 ở từng nơi thì đổi một chỗ là quên chỗ khác.</p>
     */
    public static final int CHI_SO_KHOA = 30;

    /**
     * Chuỗi chỉ số kiểu <b>{@code id=giaTri}</b> có đang khoá không.
     *
     * <p>Dùng cho quà phúc lợi và những chỗ chỉ số là trị cố định.</p>
     */
    public static boolean coKhoaBang(String chuoi) {
        return coKhoa(chuoi, "=");
    }

    /**
     * Chuỗi chỉ số kiểu <b>{@code id:min:max}</b> có đang khoá không.
     *
     * <p>Dùng cho đồ rơi từ boss và từ quái — nơi chỉ số bốc ngẫu nhiên.</p>
     */
    public static boolean coKhoaKhoang(String chuoi) {
        return coKhoa(chuoi, ":");
    }

    private static boolean coKhoa(String chuoi, String dauTach) {
        if (chuoi == null) {
            return false;
        }
        for (String phan : chuoi.split(",")) {
            String p = phan.trim();
            int i = p.indexOf(dauTach);
            String id = i < 0 ? p : p.substring(0, i);
            try {
                if (Integer.parseInt(id.trim()) == CHI_SO_KHOA) {
                    return true;
                }
            } catch (NumberFormatException boQua) {
                // Doan khong phai so -> khong phai chi so khoa.
            }
        }
        return false;
    }

    /** Bật/tắt khoá trong chuỗi kiểu {@code id=giaTri}. */
    public static String datKhoaBang(String chuoi, boolean khoa) {
        return datKhoa(chuoi, khoa, "=", CHI_SO_KHOA + "=0");
    }

    /** Bật/tắt khoá trong chuỗi kiểu {@code id:min:max}. */
    public static String datKhoaKhoang(String chuoi, boolean khoa) {
        return datKhoa(chuoi, khoa, ":", CHI_SO_KHOA + ":0:0");
    }

    /**
     * Thêm hoặc bỏ đoạn khoá, giữ nguyên mọi đoạn khác.
     *
     * <p>Ghi lại cả chuỗi thay vì nối thêm đuôi: nối thêm mà bấm hai lần là có
     * hai đoạn khoá, mà bỏ khoá thì lại chỉ xoá được một.</p>
     */
    private static String datKhoa(String chuoi, boolean khoa, String dauTach,
            String doanKhoa) {
        StringBuilder sb = new StringBuilder();
        String s = chuoi == null ? "" : chuoi.trim();
        if (!"[]".equals(s) && !s.isEmpty()) {
            for (String phan : s.split(",")) {
                String p = phan.trim();
                if (p.isEmpty()) {
                    continue;
                }
                int i = p.indexOf(dauTach);
                String id = i < 0 ? p : p.substring(0, i);
                boolean laKhoa;
                try {
                    laKhoa = Integer.parseInt(id.trim()) == CHI_SO_KHOA;
                } catch (NumberFormatException ex) {
                    laKhoa = false;
                }
                if (laKhoa) {
                    continue;       // bo doan khoa cu, lat nua them lai neu can
                }
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(p);
            }
        }
        if (khoa) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(doanKhoa);
        }
        return sb.length() == 0 ? "[]" : sb.toString();
    }

    /**
     * Ô tích "Đồ khoá" gắn vào một bảng chỉ số kiểu {@code (id, giá trị)}.
     *
     * <p>Tích thì thêm một dòng chỉ số {@link #CHI_SO_KHOA}, bỏ tích thì xoá
     * dòng đó. Làm việc thẳng trên bảng thay vì giữ trạng thái riêng: chỗ đọc
     * kết quả vẫn duyệt bảng như cũ, không phải sửa gì.</p>
     *
     * @param mangCot số cột của bảng — 2 hay 3 tuỳ hộp, dòng thêm vào phải đủ ô
     */
    public static javax.swing.JCheckBox oKhoaChoBang(
            final javax.swing.table.DefaultTableModel bang, final int soCot) {
        final javax.swing.JCheckBox cb = new javax.swing.JCheckBox(
                "Đồ khoá (không giao dịch được)", coDongKhoa(bang));
        cb.setToolTipText("Gắn chỉ số " + CHI_SO_KHOA
                + " — \"Không thể giao dịch\" — vào món đồ");
        cb.addActionListener(e -> {
            xoaDongKhoa(bang);
            if (cb.isSelected()) {
                Object[] dong = new Object[soCot];
                dong[0] = String.valueOf(CHI_SO_KHOA);
                for (int i = 1; i < soCot; i++) {
                    dong[i] = i == 1 ? "0" : "";
                }
                bang.addRow(dong);
            }
        });
        return cb;
    }

    /** Bảng có dòng chỉ số khoá chưa. */
    public static boolean coDongKhoa(javax.swing.table.DefaultTableModel bang) {
        for (int r = 0; r < bang.getRowCount(); r++) {
            if (laKhoa(bang.getValueAt(r, 0))) {
                return true;
            }
        }
        return false;
    }

    private static void xoaDongKhoa(javax.swing.table.DefaultTableModel bang) {
        for (int r = bang.getRowCount() - 1; r >= 0; r--) {
            if (laKhoa(bang.getValueAt(r, 0))) {
                bang.removeRow(r);
            }
        }
    }

    private static boolean laKhoa(Object o) {
        try {
            return Integer.parseInt(laySo(String.valueOf(o))) == CHI_SO_KHOA;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /** Ô tích "Đồ khoá" kèm dòng giải thích, dùng chung cho mọi hộp phát đồ. */

    /** Ô tích "Đồ khoá" kèm dòng giải thích, dùng chung cho mọi hộp phát đồ. */
    public static javax.swing.JCheckBox oKhoa(boolean dangKhoa) {
        javax.swing.JCheckBox cb = new javax.swing.JCheckBox(
                "Đồ khoá (không giao dịch được)", dangKhoa);
        cb.setToolTipText("Gắn chỉ số " + CHI_SO_KHOA
                + " — \"Không thể giao dịch\" — vào món đồ");
        return cb;
    }


    /** Tên của một chỉ số, hoặc câu báo nếu id không tồn tại. */
    public static String tenCua(int id) {
        try {
            if (Manager.ITEM_OPTION_TEMPLATES != null
                    && id >= 0 && id < Manager.ITEM_OPTION_TEMPLATES.size()) {
                ItemOptionTemplate t = Manager.ITEM_OPTION_TEMPLATES.get(id);
                if (t != null && t.name != null && !t.name.trim().isEmpty()) {
                    return t.name;
                }
            }
        } catch (Exception ignored) {
            // rơi xuống nhánh đọc CSDL
        }
        String ten = nro.repository.dao.SachTuyetKyDAO.khoTenChiSo().get(id);
        if (ten != null && !ten.trim().isEmpty()) {
            return ten.trim();
        }
        return "(không có id này)";
    }

    /** Đổ danh sách chỉ số vào ô chọn, giữ nguyên mục đang chọn nếu có. */
    private static void napDanhSach(JComboBox<Opt> cb) {
        List<Opt> ds = all();
        if (ds.isEmpty()) {
            return;
        }
        Object dangChon = cb.getSelectedItem();
        cb.removeAllItems();
        for (Opt o : ds) {
            cb.addItem(o);
        }
        if (dangChon != null) {
            cb.setSelectedItem(dangChon);
        }
    }

    /**
     * Cho <b>gõ để lọc</b> danh sách chỉ số trong một ô chọn.
     *
     * <h3>Vì sao cần</h3>
     *
     * <p>Bảng chỉ số có hơn ba trăm dòng. Ô chọn thả xuống mười tám dòng một lúc,
     * nên tìm "Né đòn" phải cuộn rất lâu; mà gõ tay thì lại phải nhớ id. Gõ vài
     * chữ để lọc là cách nhanh nhất, và vẫn gõ được id trần khi cần.</p>
     *
     * <h3>Lọc theo cả tên và id</h3>
     *
     * <p>Gõ "ne" ra "Né đòn", gõ "17" ra đúng dòng id 17. Bỏ dấu trước khi so
     * nên gõ không dấu vẫn tìm được — không ai gõ "né" có dấu khi đang lọc.</p>
     *
     * <h3>Cạm bẫy</h3>
     *
     * <p>Thay danh sách của ô chọn ngay trong lúc người dùng đang gõ sẽ làm ô
     * chọn tự đặt lại nội dung ô gõ, ăn mất chữ vừa nhập. Nên phải khoá lại bằng
     * cờ {@code dangLoc} và hoàn chữ về đúng như trước.</p>
     */
    private static void gocLoc(final JComboBox<Opt> cb, final boolean[] dangLoc) {
        final java.awt.Component ed = cb.getEditor().getEditorComponent();
        if (!(ed instanceof javax.swing.text.JTextComponent)) {
            return;
        }
        final javax.swing.text.JTextComponent o =
                (javax.swing.text.JTextComponent) ed;
        o.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loc();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loc();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }

            private void loc() {
                if (dangLoc[0]) {
                    return;
                }
                final String go = o.getText();
                javax.swing.SwingUtilities.invokeLater(() -> apLoc(cb, o, go, dangLoc));
            }
        });
    }

    /** Dựng lại danh sách ô chọn theo chữ đang gõ, giữ nguyên chữ đó. */
    private static void apLoc(JComboBox<Opt> cb, javax.swing.text.JTextComponent o,
            String go, boolean[] dangLoc) {
        if (dangLoc[0]) {
            return;
        }
        dangLoc[0] = true;
        try {
            String khoa = boDau(go);
            List<Opt> ds = all();
            List<Opt> khop = new java.util.ArrayList<>();
            for (Opt x : ds) {
                if (khoa.isEmpty()
                        || boDau(x.name).contains(khoa)
                        || String.valueOf(x.id).startsWith(khoa)) {
                    khop.add(x);
                }
            }
            // Khong con dong nao khop -> giu nguyen danh sach cu. Xoa het thi o
            // chon thanh rong tron, nguoi dung tuong minh vua lam hong gi.
            if (khop.isEmpty()) {
                return;
            }
            javax.swing.DefaultComboBoxModel<Opt> mm =
                    new javax.swing.DefaultComboBoxModel<>();
            for (Opt x : khop) {
                mm.addElement(x);
            }
            // Nho cho con tro TRUOC khi doi danh sach: setModel se viet lai noi
            // dung o go, khong hoan lai thi con tro nhay ve dau moi lan go.
            int viTri = o.getCaretPosition();
            cb.hidePopup();
            cb.setModel(mm);
            o.setText(go);
            o.setCaretPosition(Math.min(viTri, go.length()));
            if (cb.isShowing()) {
                cb.showPopup();
            }
        } catch (Exception boQua) {
            // Loc chi la tien ich; loi o day khong duoc lam ket o chon.
        } finally {
            dangLoc[0] = false;
        }
    }

    /**
     * Bỏ dấu tiếng Việt và hạ chữ thường, để so khớp lúc lọc.
     *
     * <p>Dùng {@code Normalizer} thay vì bảng đổi tay: bảng tay bao giờ cũng
     * thiếu một vài chữ, mà thiếu chữ nào thì lọc chữ đó lặng lẽ không ra gì.</p>
     */
    private static String boDau(String s) {
        if (s == null) {
            return "";
        }
        String t = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return t.replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    /**
     * Gắn ô chọn chỉ số vào một cột của bảng.
     *
     * <p>Ô chọn để {@code setEditable(true)} nên vẫn gõ tay được khi cần id lạ
     * chưa có tên — không khoá cứng người dùng vào danh sách.</p>
     *
     * @param table bảng cần gắn
     * @param col   chỉ số cột <b>theo màn hình</b>
     */
    public static void install(JTable table, int col) {
        // KHÔNG bỏ cuộc khi danh sách còn rỗng. Panel dựng bảng trước lúc máy
        // chủ nạp xong bảng chỉ số; bản cũ return ở đây nên ô chọn lặng lẽ trở
        // lại thành ô gõ số, mà không có dấu hiệu gì để lần ra.
        final JComboBox<Opt> cb = new JComboBox<>();
        napDanhSach(cb);
        cb.setEditable(true);
        cb.setMaximumRowCount(18);

        // Cờ dùng chung giữa phần lọc và phần chốt ô.
        //
        // Đây là chỗ khó nhất của việc gõ-để-lọc trong một ô chọn của bảng:
        // DefaultCellEditor gắn một ActionListener vào ô chọn, và MỌI sự kiện
        // action — kể cả sự kiện do chính việc đổi danh sách sinh ra — đều gọi
        // stopCellEditing(), tức là đóng ô ngay. Nên gõ một chữ là ô tự đóng và
        // chữ vừa gõ bay mất.
        //
        // Cách chặn: trong lúc lọc thì stopCellEditing() trả về false, ô ở lại.
        final boolean[] dangLoc = {false};

        TableColumn c = table.getColumnModel().getColumn(col);
        c.setCellEditor(new DefaultCellEditor(cb) {
            @Override
            public boolean stopCellEditing() {
                if (dangLoc[0]) {
                    return false;
                }
                return super.stopCellEditing();
            }

            @Override
            public Object getCellEditorValue() {
                // Ghi vao bang DUNG con so, khong phai nhan "6 — HP+#": phan
                // dung JSON phia sau van doc so nhu cu.
                Object v = super.getCellEditorValue();
                if (v instanceof Opt) {
                    return String.valueOf(((Opt) v).id);
                }
                return laySo(String.valueOf(v));
            }

            @Override
            public Component getTableCellEditorComponent(JTable t, Object value,
                                                         boolean sel, int r, int cc) {
                // Nạp muộn: lần đầu mở ô chọn mà danh sách còn rỗng thì thử lại,
                // lúc này máy chủ đã nạp xong bảng chỉ số.
                if (cb.getItemCount() == 0) {
                    napDanhSach(cb);
                }
                // Mo o chon dung ngay muc tuong ung voi so dang co trong o.
                Component comp = super.getTableCellEditorComponent(t, value, sel, r, cc);
                try {
                    int id = Integer.parseInt(String.valueOf(value).trim());
                    for (int i = 0; i < cb.getItemCount(); i++) {
                        if (cb.getItemAt(i).id == id) {
                            cb.setSelectedIndex(i);
                            return comp;
                        }
                    }
                    cb.setSelectedItem(String.valueOf(id));
                } catch (NumberFormatException ignored) {
                    // Ô đang trống hoặc gõ dở -> để ô chọn tự do
                }
                return comp;
            }
        });

        // Gan bo loc SAU khi editor da duoc dat: bo loc can chinh co dangLoc ma
        // editor o tren doc, nen thu tu nay bat buoc.
        gocLoc(cb, dangLoc);

        // Ngoai EDITOR con phai co RENDERER. Thieu no thi o chi hien con so tho,
        // phai bam vao o moi thay ten chi so — dung luc dang sua, con luc nhin
        // ca bang thi khong biet 50, 77, 103 la gi.
        c.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int r, int cc) {
                Component comp = super.getTableCellRendererComponent(
                        t, value, sel, focus, r, cc);
                String hien = String.valueOf(value == null ? "" : value).trim();
                if (!hien.isEmpty()) {
                    try {
                        int id = Integer.parseInt(hien);
                        String ten = tenCua(id);
                        hien = (ten == null || ten.isEmpty())
                                ? String.valueOf(id) : (id + " — " + ten);
                    } catch (NumberFormatException ignored) {
                        // O dang go do -> hien nguyen van nguoi dung go.
                    }
                }
                setText(hien);
                return comp;
            }
        });
    }

    /**
     * Hộp tra cứu vật phẩm — chọn bằng ảnh và tên thay vì nhớ id.
     *
     * <p>Máy chủ có hơn hai nghìn mẫu vật phẩm; gõ id trần thì không có cách nào
     * biết mình đang chọn đúng món hay không cho tới khi phát ra rồi.</p>
     *
     * @return id vật phẩm đã chọn, hoặc {@code -1} nếu huỷ
     */
    public static int chonVatPham(java.awt.Component cha, int idHienTai) {
        java.util.List<Integer> ds = chonNhieuVatPham(cha, idHienTai);
        return ds.isEmpty() ? -1 : ds.get(0);
    }

    /**
     * Như {@link #chonVatPham} nhưng cho chọn <b>nhiều</b> dòng một lúc.
     *
     * <p>Giữ Ctrl hoặc Shift để chọn nhiều rồi bấm <b>Chọn</b>. Nháy đúp vẫn
     * chọn đúng một dòng như cũ — thao tác quen thuộc không đổi.</p>
     *
     * @return các id đã chọn, rỗng nếu huỷ
     */
    public static java.util.List<Integer> chonNhieuVatPham(java.awt.Component cha,
                                                           int idHienTai) {
        javax.swing.JTextField q = new javax.swing.JTextField();
        // Lua chon giu RIENG o day, khong nam trong bang. Bang chi chua nhung
        // dong dang hien sau khi loc — doc ket qua tu bang la mat sach nhung
        // mon da tich roi bi loc di.
        final java.util.LinkedHashSet<Integer> daTich = new java.util.LinkedHashSet<>();
        if (idHienTai >= 0) {
            daTich.add(idHienTai);
        }
        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(
                new Object[]{"Chọn", "Ảnh", "ID", "Tên vật phẩm", "Loại"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                // Cot 0 la o tich. Cot anh giu ID icon (so), renderer doi thanh anh —
                // xem phan setCellRenderer ben duoi.
                return c == 0 ? Boolean.class : Object.class;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(32);
        t.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int[] w = {45, 40, 55, 280, 160};
        for (int i = 0; i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        // Loc theo loai: hon hai nghin mau, tim theo ten khong phai luc nao
        // cung biet go gi — loc "Cai trang" hay "Ao" thi ra ngay.
        final javax.swing.JComboBox<String> cbLoai = new javax.swing.JComboBox<>();
        cbLoai.addItem("Tất cả loại");
        java.util.TreeSet<Integer> loaiCo = new java.util.TreeSet<>();
        for (nro.entity.template.ItemTemplate tp : Manager.ITEM_TEMPLATES) {
            if (tp != null) {
                loaiCo.add((int) tp.type);
            }
        }
        for (int lo : loaiCo) {
            cbLoai.addItem(PlayerManagerPanel.typeName(lo));
        }
        // Anh nap LUOI: JTable chi ve dong dang nhin thay, ma iconOf co bo nho
        // dem — nen do het 2447 dong van nhanh. Truoc day nap anh ngay luc
        // dung dong nen phai cat bot con 300.
        t.getColumnModel().getColumn(1).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable tb, Object value, boolean sel,
                    boolean focus, int r, int cc) {
                java.awt.Component comp = super.getTableCellRendererComponent(
                        tb, "", sel, focus, r, cc);
                setIcon(value == null ? null
                        : PlayerManagerPanel.iconOf(soCua(value)));
                setHorizontalAlignment(CENTER);
                return comp;
            }
        });
        final javax.swing.JLabel dem = new javax.swing.JLabel();
        dem.setForeground(new java.awt.Color(120, 120, 120));
        Runnable timKiem = () -> {
            String key = q.getText().trim().toLowerCase();
            m.setRowCount(0);
            int khop = 0;
            int hien = 0;
            for (nro.entity.template.ItemTemplate tp : Manager.ITEM_TEMPLATES) {
                if (tp == null || tp.name == null) {
                    continue;
                }
                if (cbLoai.getSelectedIndex() > 0
                        && !PlayerManagerPanel.typeName(tp.type)
                                .equals(cbLoai.getSelectedItem())) {
                    continue;
                }
                if (!key.isEmpty() && !tp.name.toLowerCase().contains(key)
                        && !String.valueOf(tp.id).equals(key)) {
                    continue;
                }
                khop++;
                // Khong con cat o 300 nua: cot anh da nap luoi nen do het hon hai
                // nghin dong cung nhanh.
                {
                    hien++;
                    m.addRow(new Object[]{daTich.contains((int) tp.id),
                        (int) tp.iconID, tp.id,
                        tp.name, PlayerManagerPanel.typeName(tp.type)});
                }
            }
            dem.setText(hien < khop
                    ? "  Hiện " + hien + " / " + khop + " — gõ thêm để thu hẹp"
                    : "  " + hien + " vật phẩm");
        };
        q.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                timKiem.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                timKiem.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                timKiem.run();
            }
        });
        // Gan o day, trong than ham — KHONG dat trong insertUpdate:
        // de trong do thi chi dang ky khi nguoi dung go chu, va moi lan go
        // lai dang ky them mot listener nua.
        cbLoai.addActionListener(ev -> timKiem.run());
        timKiem.run();

        // Moi lan tich hay bo tich, ghi thang vao daTich.
        m.addTableModelListener(ev -> {
            if (ev.getColumn() != 0 || ev.getFirstRow() < 0) {
                return;
            }
            for (int r = ev.getFirstRow();
                    r <= ev.getLastRow() && r < m.getRowCount(); r++) {
                int id = soCua(m.getValueAt(r, 2));
                if (Boolean.TRUE.equals(m.getValueAt(r, 0))) {
                    daTich.add(id);
                } else {
                    daTich.remove(id);
                }
            }
            dem.setText("  Đang chọn: " + daTich.size() + " món");
        });

        // Nhay dup = chon luon MOT mon, khoi phai tich roi bam nut.
        final java.util.List<Integer> ketQua = new java.util.ArrayList<>();
        final javax.swing.JDialog[] hop = new javax.swing.JDialog[1];
        final Runnable lay = () -> {
            ketQua.clear();
            ketQua.addAll(daTich);
        };
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && t.getSelectedRow() >= 0) {
                    int id = soCua(m.getValueAt(t.getSelectedRow(), 2));
                    if (id >= 0) {
                        daTich.clear();
                        daTich.add(id);
                    }
                    lay.run();
                    hop[0].dispose();
                }
            }
        });

        javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.BorderLayout(6, 6));
        top.setBorder(new javax.swing.border.EmptyBorder(8, 8, 4, 8));
        javax.swing.JPanel trai = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        trai.setOpaque(false);
        trai.add(new javax.swing.JLabel("Loại:"));
        trai.add(cbLoai);
        trai.add(new javax.swing.JLabel("Tìm theo tên hoặc id:"));
        javax.swing.JButton boTich = new javax.swing.JButton("Bỏ tích hết");
        boTich.addActionListener(ev -> {
            daTich.clear();
            timKiem.run();
            dem.setText("  Đang chọn: 0 món");
        });
        trai.add(boTich);
        top.add(trai, java.awt.BorderLayout.WEST);
        top.add(q, java.awt.BorderLayout.CENTER);
        top.add(dem, java.awt.BorderLayout.EAST);

        javax.swing.JPanel wrap = new javax.swing.JPanel(new java.awt.BorderLayout());
        wrap.add(top, java.awt.BorderLayout.NORTH);
        wrap.add(new javax.swing.JScrollPane(t), java.awt.BorderLayout.CENTER);
        javax.swing.JLabel goiY = new javax.swing.JLabel(
                "  Nháy đúp để chọn một dòng — giữ Ctrl hoặc Shift để chọn nhiều "
                + "rồi bấm Chọn.");
        goiY.setForeground(new java.awt.Color(120, 120, 120));
        javax.swing.JPanel nam = new javax.swing.JPanel(new java.awt.BorderLayout(6, 0));
        nam.add(goiY, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel nut = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 6));
        javax.swing.JButton bHuy = new javax.swing.JButton("Huỷ");
        bHuy.addActionListener(e -> {
            ketQua.clear();
            hop[0].dispose();
        });
        javax.swing.JButton bChon = new javax.swing.JButton("Chọn");
        bChon.addActionListener(e -> {
            lay.run();
            hop[0].dispose();
        });
        nut.add(bHuy);
        nut.add(bChon);
        nam.add(nut, java.awt.BorderLayout.EAST);
        wrap.add(nam, java.awt.BorderLayout.SOUTH);

        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(cha);
        // Chu cha co the la CUA SO CHINH (Frame) hoac MOT HOP KHAC (Dialog) —
        // hop "Them mon roi" chinh la truong hop thu hai. Ep thang sang Frame
        // thi bam nut khong ra gi ca: ClassCastException bi nuot trong luong
        // giao dien, khong mot dau hieu nao.
        hop[0] = owner instanceof java.awt.Dialog
                ? new javax.swing.JDialog((java.awt.Dialog) owner, "Chọn vật phẩm", true)
                : new javax.swing.JDialog((java.awt.Frame) owner, "Chọn vật phẩm", true);
        hop[0].setContentPane(wrap);
        hop[0].setSize(680, 520);
        hop[0].setLocationRelativeTo(cha);
        if (idHienTai > 0) {
            q.setText(String.valueOf(idHienTai));
        }
        hop[0].setVisible(true);
        return ketQua;
    }

    private static int soCua(Object o) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Lấy phần số ở đầu chuỗi.
     *
     * <p>Người dùng có thể gõ tay {@code "6"} hoặc chọn rồi sửa thành
     * {@code "6 — HP+#"}; cả hai đều phải ra {@code "6"}.</p>
     */
    static String laySo(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        int i = 0;
        while (i < t.length() && (Character.isDigit(t.charAt(i)) || (i == 0 && t.charAt(i) == '-'))) {
            i++;
        }
        return i == 0 ? t : t.substring(0, i);
    }

    /**
     * Hộp chọn <b>chỉ số</b> có ô tìm.
     *
     * <p>Bảng {@code item_option_template} có 273 dòng. Ô chọn thả xuống bắt
     * người dùng cuộn qua từng dòng để tìm — mà tên chỉ số thì dài và na ná
     * nhau, nên rất dễ lướt qua đúng cái mình cần. Hộp này gõ vài chữ là lọc
     * ngay.</p>
     *
     * @return id đã chọn, hoặc {@code -1} nếu huỷ
     */
    public static int chonChiSo(java.awt.Component cha, int idHienTai) {
        final List<Opt> tatCa = new ArrayList<>(all());
        javax.swing.JTextField q = new javax.swing.JTextField();
        final javax.swing.JCheckBox chiDanhDau = new javax.swing.JCheckBox(
                "Chỉ hiện chỉ số đánh dấu set", true);
        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(
                new Object[]{"ID", "Tên chỉ số"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(24);
        t.getColumnModel().getColumn(0).setPreferredWidth(60);
        t.getColumnModel().getColumn(1).setPreferredWidth(430);

        final javax.swing.JLabel dem = new javax.swing.JLabel();
        dem.setForeground(new java.awt.Color(120, 120, 120));
        final Runnable loc = () -> {
            String key = q.getText().trim().toLowerCase();
            m.setRowCount(0);
            for (Opt o : tatCa) {
                if (chiDanhDau.isSelected() && !laDanhDauSet(o.name)) {
                    continue;
                }
                if (!key.isEmpty() && !o.name.toLowerCase().contains(key)
                        && !String.valueOf(o.id).equals(key)) {
                    continue;
                }
                m.addRow(new Object[]{o.id, o.name});
            }
            dem.setText("  " + m.getRowCount() + " / " + tatCa.size() + " chỉ số");
        };
        q.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loc.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loc.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                loc.run();
            }
        });
        chiDanhDau.addActionListener(e -> loc.run());
        loc.run();

        final int[] ketQua = {-1};
        final javax.swing.JDialog[] hop = new javax.swing.JDialog[1];
        final Runnable chon = () -> {
            int r = t.getSelectedRow();
            if (r >= 0) {
                ketQua[0] = soCua(m.getValueAt(r, 0));
                hop[0].dispose();
            }
        };
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    chon.run();
                }
            }
        });

        javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.BorderLayout(6, 6));
        top.setBorder(new javax.swing.border.EmptyBorder(8, 8, 4, 8));
        top.add(new javax.swing.JLabel("Tìm theo tên hoặc id:"), java.awt.BorderLayout.WEST);
        top.add(q, java.awt.BorderLayout.CENTER);
        top.add(dem, java.awt.BorderLayout.EAST);

        javax.swing.JPanel nut = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 6));
        // Them / sua chi so ngay trong hop: khong phai mo phpMyAdmin de tao
        // mot chi so danh dau set moi.
        javax.swing.JButton bThem = new javax.swing.JButton("Thêm chỉ số mới");
        bThem.addActionListener(e -> {
            String ten = javax.swing.JOptionPane.showInputDialog(hop[0],
                    "Tên chỉ số mới:\n\n"
                    + "Đặt tên bắt đầu bằng \"Set \" nếu dùng làm chỉ số nhận diện set.",
                    "Thêm chỉ số", javax.swing.JOptionPane.PLAIN_MESSAGE);
            if (ten == null || ten.trim().isEmpty()) {
                return;
            }
            int id = nro.repository.dao.ChiSoOptionDAO.them(ten.trim());
            if (id < 0) {
                javax.swing.JOptionPane.showMessageDialog(hop[0],
                        "Không thêm được — xem log máy chủ.", "Lỗi",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            tatCa.clear();
            tatCa.addAll(all());
            q.setText(String.valueOf(id));
            loc.run();
            if (m.getRowCount() > 0) {
                t.setRowSelectionInterval(0, 0);
            }
        });
        javax.swing.JButton bSua = new javax.swing.JButton("Đổi tên");
        bSua.addActionListener(e -> {
            int r = t.getSelectedRow();
            if (r < 0) {
                return;
            }
            int id = soCua(m.getValueAt(r, 0));
            String ten = javax.swing.JOptionPane.showInputDialog(hop[0],
                    "Tên mới cho chỉ số " + id + ":",
                    String.valueOf(m.getValueAt(r, 1)));
            if (ten == null || ten.trim().isEmpty()) {
                return;
            }
            if (!nro.repository.dao.ChiSoOptionDAO.doiTen(id, ten.trim())) {
                javax.swing.JOptionPane.showMessageDialog(hop[0],
                        "Không đổi được tên — xem log máy chủ.", "Lỗi",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            tatCa.clear();
            tatCa.addAll(all());
            loc.run();
        });
        javax.swing.JButton bXoa = new javax.swing.JButton("Xoá");
        bXoa.addActionListener(e -> {
            int r = t.getSelectedRow();
            if (r < 0) {
                return;
            }
            int id = soCua(m.getValueAt(r, 0));
            // Hoi truoc khi xoa. DAO tu tu choi neu khong phai id cuoi hoac
            // dang co cho dung, va tra ve nguyen van ly do de hien o day.
            if (javax.swing.JOptionPane.showConfirmDialog(hop[0],
                    "Xoá chỉ số " + id + " — \"" + m.getValueAt(r, 1) + "\"?",
                    "Xoá chỉ số", javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE)
                    != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
            String loi = nro.repository.dao.ChiSoOptionDAO.xoa(id);
            if (loi != null) {
                javax.swing.JOptionPane.showMessageDialog(hop[0], loi,
                        "Không xoá được chỉ số", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            tatCa.clear();
            tatCa.addAll(all());
            q.setText("");
            loc.run();
        });

        javax.swing.JButton bHuy = new javax.swing.JButton("Huỷ");
        bHuy.addActionListener(e -> hop[0].dispose());
        javax.swing.JButton bOk = new javax.swing.JButton("Chọn");
        bOk.addActionListener(e -> chon.run());
        nut.add(bThem);
        nut.add(bSua);
        nut.add(bXoa);
        nut.add(bHuy);
        nut.add(bOk);

        javax.swing.JPanel giaiThich = new javax.swing.JPanel(new java.awt.BorderLayout());
        giaiThich.add(chiDanhDau, java.awt.BorderLayout.WEST);
        javax.swing.JLabel note = new javax.swing.JLabel("<html><span style='color:#777'>"
                + "Đây là chỉ số <b>nhận diện</b> set — chỉ để biết món nào thuộc set. "
                + "<b>Tác dụng</b> của set khai riêng ở bảng chỉ số (chọn loại + điền số).<br>"
                + "Chỉ số kiểu <code>$(5 món +100% HP)</code> chỉ là <i>dòng chữ mô tả</i> "
                + "in trên món, không tự cộng gì cả — đừng dùng làm chỉ số nhận diện."
                + "</span></html>");
        note.setBorder(new javax.swing.border.EmptyBorder(2, 8, 6, 8));

        javax.swing.JPanel duoi = new javax.swing.JPanel(new java.awt.BorderLayout());
        duoi.add(giaiThich, java.awt.BorderLayout.NORTH);
        duoi.add(note, java.awt.BorderLayout.CENTER);
        duoi.add(nut, java.awt.BorderLayout.SOUTH);

        javax.swing.JPanel wrap = new javax.swing.JPanel(new java.awt.BorderLayout());
        wrap.add(top, java.awt.BorderLayout.NORTH);
        wrap.add(new javax.swing.JScrollPane(t), java.awt.BorderLayout.CENTER);
        wrap.add(duoi, java.awt.BorderLayout.SOUTH);

        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(cha);
        hop[0] = owner instanceof java.awt.Frame
                ? new javax.swing.JDialog((java.awt.Frame) owner, "Chọn chỉ số", true)
                : new javax.swing.JDialog((java.awt.Dialog) owner, "Chọn chỉ số", true);
        hop[0].setContentPane(wrap);
        hop[0].setSize(660, 560);
        hop[0].setLocationRelativeTo(cha);
        if (idHienTai > 0) {
            q.setText(String.valueOf(idHienTai));
        }
        hop[0].setVisible(true);
        return ketQua[0];
    }

    /**
     * {@code true} nếu chỉ số này là <b>chỉ số đánh dấu set</b>.
     *
     * <p>Bảng {@code item_option_template} trộn hai thứ khác hẳn nhau:</p>
     *
     * <ul>
     *   <li><b>Đánh dấu set</b> — tên dạng {@code "Set Nappa"}. Đây mới là thứ
     *       quyết định món đồ thuộc set nào.</li>
     *   <li><b>Dòng mô tả</b> — tên dạng {@code "$(5 món +100% HP)"}. Chỉ là chữ
     *       in lên món cho người chơi đọc; <b>không tự cộng gì cả</b>.</li>
     * </ul>
     *
     * <p>Hai loại nằm cạnh nhau trong danh sách nên rất dễ chọn nhầm: set Nappa
     * cộng 100% HP, mà cả {@code "Set Nappa"} lẫn {@code "$(5 món +100% HP)"}
     * đều có trong danh sách. Chọn nhầm dòng mô tả thì set không bao giờ kích
     * hoạt.</p>
     */
    static boolean laDanhDauSet(String ten) {
        if (ten == null) {
            return false;
        }
        String t = ten.trim();
        // Dong mo ta luon bat dau bang $ hoac $[n] — bo het.
        if (t.startsWith("$")) {
            return false;
        }
        String th = t.toLowerCase();
        return th.startsWith("set ") || th.startsWith("bộ ");
    }

    /** Tên của một chỉ số theo id, hoặc {@code "(không rõ)"} nếu không có. */
    static String tenChiSo(int id) {
        for (Opt o : all()) {
            if (o.id == id) {
                return o.name;
            }
        }
        return "(không rõ)";
    }
}
