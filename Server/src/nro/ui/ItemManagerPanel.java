package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import nro.core.log.Logger;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.entity.template.ItemTemplate;
import nro.repository.dao.PlayerDAO;
import nro.server.Client;
import nro.server.Manager;
import nro.server.ServerManager;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;

/**
 * Tab <b>Quản Lý Vật Phẩm</b> — tra cứu toàn bộ mẫu vật phẩm của máy chủ và phát
 * cho người chơi đang online.
 *
 * <p>Tách khỏi {@link PlayerManagerPanel} vì hai việc khác hẳn nhau: bên kia sửa
 * <i>một người</i>, bên này làm việc với <i>bảng mẫu dùng chung</i> rồi mới chọn
 * người nhận.</p>
 *
 * <h2>Ba phần</h2>
 * <ol>
 *   <li><b>Trái</b> — danh mục mẫu vật phẩm, lọc theo loại và theo tên/ID.</li>
 *   <li><b>Phải trên</b> — chọn người nhận, ID vật phẩm, số lượng.</li>
 *   <li><b>Phải dưới</b> — bảng chỉ số (option) sửa được tuỳ ý.</li>
 * </ol>
 *
 * <h2>Vì sao phải đi qua {@code InventoryService.addItemBag()}</h2>
 *
 * <p>Không phải mẫu vật phẩm nào cũng nằm trong một ô. Vàng, Ngọc, Hồng ngọc
 * cộng vào ví; Bùa cộng thời gian hiệu lực; có id lại là "mở rộng thêm một ô".
 * Đặt thẳng những thứ đó vào ô thì gói tin túi đồ chứa một vật phẩm mà client
 * không biết vẽ — nhân vật biến mất, không di chuyển được, giao diện loạn.</p>
 *
 * <p>Nên khi thêm vào <b>hành trang</b>, panel gọi đúng hàm mà game vẫn dùng để
 * nhặt đồ. Rương ở nhà không có hàm tương đương nên đặt thẳng vào ô, và những
 * vật phẩm đặc biệt bị chặn trước bằng {@link PlayerManagerPanel#rawUnsafe}.</p>
 */
public class ItemManagerPanel extends JPanel {

    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color OK_GREEN = new Color(40, 160, 70);
    private static final Color WARN_RED = new Color(200, 60, 60);
    private static final Color GREY = new Color(120, 120, 120);

    /**
     * Số dòng tối đa đổ ra bảng một lần.
     *
     * <p>Máy chủ có hơn hai nghìn mẫu vật phẩm. Đổ hết kèm ảnh thì phải đọc chừng
     * ấy tệp PNG trên luồng giao diện, cửa sổ đứng vài giây. Cắt ở đây và
     * <b>nói rõ đã cắt bao nhiêu</b> ngay trên nhãn đếm.</p>
     */
    private static final int CATALOG_LIMIT = 600;

    /**
     * Bật khi chính panel đang đổ dữ liệu vào bảng.
     *
     * <p>{@code addRow} và {@code setValueAt} cũng bắn ra sự kiện y hệt người
     * dùng bấm tick. Không có cờ này thì mỗi lần lọc lại danh mục là panel ghi
     * ngược 600 dòng xuống CSDL.</p>
     */
    private boolean dangDoBang;

    // ------------------------------------------------------------------ danh mục
    /**
     * Cột "Dùng được" — dấu tick thủ công, lưu thẳng vào
     * {@code item_template.dung_duoc}.
     *
     * <p>Đặt ngay sau cột ID: cột ID phải giữ nguyên chỉ số 1 vì
     * {@link #mauDangChon()} và mấy chỗ khác đọc {@code getValueAt(row, 1)}.</p>
     */
    private static final int COT_DUNG_DUOC = 2;

    private final DefaultTableModel catModel = new DefaultTableModel(
            new Object[]{"Ảnh", "ID", "Dùng được", "Tên vật phẩm", "Loại", "Hệ",
                "Xếp chồng", "SM yêu cầu", "Ảnh (icon)", "Part", "Head", "Body",
                "Leg", "Mô tả"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == COT_DUNG_DUOC;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            // Cot 0 giu ID ANH chu khong giu ImageIcon; PlayerManagerPanel
            // .veIcon() doc anh luc ve, chi cho nhung dong dang nhin thay.
            // Tra ve Boolean thi JTable tu ve o tick va cho bam.
            return c == COT_DUNG_DUOC ? Boolean.class : Object.class;
        }
    };
    private final JTable catTable = new JTable(catModel);
    private final JComboBox<TypeOpt> cbType = new JComboBox<>();
    private final JTextField fSearch = new JTextField(18);
    private final JLabel lblCount = new JLabel();

    // ------------------------------------------------------------------ phát đồ
    private final JComboBox<PlayerOpt> cbPlayer = new JComboBox<>();

    /**
     * Ô tìm người nhận theo tên hoặc id.
     *
     * <p>Danh sách người online có thể vài trăm mục; cuộn tay trong ô chọn để tìm
     * đúng một người là chậm và dễ chọn nhầm người tên gần giống.</p>
     */
    private final JTextField fTimNguoi = new JTextField(16);
    private final JLabel lblSoNguoi = new JLabel();
    private final JTextField fId = new JTextField(8);
    private final JTextField fQty = new JTextField("1", 6);


    /**
     * Cho phép giao dịch món vừa phát hay không.
     *
     * <p>Bỏ tick thì món nhận thêm chỉ số <b>id 30 — "Không thể giao dịch"</b>.
     * Đó là cách game khoá giao dịch: không có cột riêng nào, chỉ là một option
     * gắn vào chính vật phẩm.</p>
     */
    private final javax.swing.JCheckBox cbGiaoDich =
            new javax.swing.JCheckBox("Cho giao dịch", true);

    /**
     * Set kích hoạt gắn vào món trang bị sắp phát; mục đầu là "không gắn".
     *
     * <p>Chỉ có nghĩa với năm ô trang bị (áo, quần, găng, giày, rada — loại 0–4).
     * Món khác thì bỏ qua, vì set kích hoạt nhận diện bằng chỉ số trên trang bị,
     * gắn vào đậu thần hay ngọc rồng thì không chỗ nào đọc tới.</p>
     *
     * <p>Chọn thẳng set chứ không bốc ngẫu nhiên như quái rơi: đây là công cụ
     * quản trị, phát đúng set mình muốn mới kiểm tra được.</p>
     */
    private final javax.swing.JComboBox<String> cbSet = new javax.swing.JComboBox<>();

    /** Mã set theo từng mục của {@link #cbSet}; {@code null} là mục "không gắn". */
    private final java.util.List<String> maSet = new java.util.ArrayList<>();

    /**
     * Số <b>ô sao</b> pha lê trên món trang bị sắp phát — chỉ số 107.
     *
     * <p>Đây là số ô <i>trống</i> để người chơi tự nạp sao, không phải sao đã
     * gắn. Món đồ rơi từ boss ngoài kia mang đúng chỉ số này.</p>
     */
    private final JTextField fSoOSao = new JTextField("0", 4);

    /**
     * Các viên sao gắn sẵn: <b>id vật phẩm sao → số lượng</b>.
     *
     * <p>Gắn được <b>nhiều loại</b> cùng lúc, miễn tổng số không vượt số ô sao —
     * đúng như người chơi tự nạp từng viên khác màu vào từng ô.</p>
     *
     * <p>Gắn sao là hai việc: tăng chỉ số 102 (số sao đã gắn) và cộng chỉ số
     * riêng của từng viên. Panel làm y hệt {@code EpSaoTrangBi} — nếu chỉ đặt
     * 102 mà quên chỉ số kia thì món đồ hiện có sao nhưng không cộng gì.</p>
     */
    private final java.util.Map<Integer, Integer> saoGan =
            new java.util.LinkedHashMap<>();

    /** Ô chỉ để đọc, hiện tóm tắt các viên sao đang chọn. */
    private final JTextField fSaoGan = new JTextField(14);

    /**
     * Món này có ô sao pha lê hay không.
     *
     * <p><b>Không tích thì phần sao bị bỏ qua hoàn toàn</b> — không kiểm, không
     * gắn chỉ số nào. Trước đây không có ô này: số ô sao gõ lần trước còn nằm
     * trong ô, nên phát một món <i>không phải trang bị</i> (đậu thần, hộp quà…)
     * là bị từ chối với câu "không phải trang bị nên không có ô sao pha lê" —
     * người dùng không hiểu vì sao đột nhiên không phát được gì.</p>
     */
    private final javax.swing.JCheckBox cbCoSao =
            new javax.swing.JCheckBox("Có sao pha lê", false);
    private final JLabel lblName = new JLabel("—");
    private final JLabel lblIcon = new JLabel();
    private final JLabel lblStatus = new JLabel(" ");

    /** Bảng chỉ số của vật phẩm sắp phát. Cả hai cột đều sửa được. */
    private final DefaultTableModel optModel = new DefaultTableModel(
            new Object[]{"ID chỉ số", "Giá trị", "Ý nghĩa"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c < 2;       // cột "Ý nghĩa" chỉ để đọc
        }
    };
    private final JTable optTable = new JTable(optModel);

    private boolean catalogReady;

    public ItemManagerPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Quản Lý Vật Phẩm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildCatalog(), buildGive());
        split.setResizeWeight(0.55);
        split.setBorder(null);

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Danh mục & phát đồ", split);
        tab.addTab("Loại vật phẩm", buildLoaiTab());
        tab.addTab("Ảnh (icon)", buildAnhTab());
        // Chuyen tab la nap lai tab vua mo. Du da co LamMoi, van can buoc nay:
        // du lieu con doi tu ben ngoai panel — sua thang trong CSDL, hay mot
        // man khac ghi de — thi khong ai ho len duoc.
        tab.addChangeListener(e -> napTabHienTai(tab.getSelectedIndex()));
        add(tab, BorderLayout.CENTER);

        // Ba man deu soi chung mot mo du lieu nen cung nghe mot chu de.
        LamMoi.nghe(this::fillCatalog, LamMoi.VAT_PHAM, LamMoi.LOAI);
        LamMoi.nghe(this::buildTypeCombo, LamMoi.VAT_PHAM, LamMoi.LOAI);
        LamMoi.nghe(LamMoi.LOAI, this::napLoaiNeuCo);
        LamMoi.nghe(LamMoi.ANH, this::napAnhNeuCo);
        LamMoi.nghe(LamMoi.NGUOI_CHOI, this::refreshPlayers);

        lblStatus.setForeground(GREY);
        lblStatus.setBorder(new EmptyBorder(4, 2, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);

        // Bảng mẫu vật phẩm nạp xong sau khi máy chủ khởi động, nên phải chờ.
        // Một nhịp 2 giây, dựng được thì tự tắt — rẻ hơn nhiều so với đoán thời điểm.
        // Bang mau vat pham nap xong SAU khi may chu khoi dong, nen phai cho.
        // Danh sach sao doc tu bang do — goi lúc dung giao dien thi con rong.
        Timer t = new Timer(2000, e -> {
            buildTypeCombo();
            refreshPlayers();
            veTomTatSao();
        });
        t.start();
    }

    /**
     * Nạp lại đúng tab vừa mở.
     *
     * <p>Chỉ nạp tab đang nhìn thấy chứ không nạp cả ba: tab Ảnh phải quét thư
     * mục hơn hai vạn tệp, làm việc đó mỗi lần bấm sang tab khác là phí.</p>
     */
    private void napTabHienTai(int chiSo) {
        switch (chiSo) {
            case 0:
                buildTypeCombo();
                fillCatalog();
                break;
            case 1:
                napLoaiNeuCo();
                break;
            case 2:
                napAnhNeuCo();
                break;
            default:
                break;
        }
    }

    /**
     * Nạp lại bảng loại, bỏ qua nếu tab đó chưa dựng xong.
     *
     * <p>{@link LamMoi} có thể hô lên ngay trong lúc hàm dựng còn đang chạy,
     * khi bảng chưa kịp có cột nào.</p>
     */
    private void napLoaiNeuCo() {
        if (loaiTable.getColumnCount() > 0) {
            napLoai();
        }
    }

    private void napAnhNeuCo() {
        if (anhList != null) {
            napAnh();
        }
    }

    // =====================================================================
    //  Danh mục vật phẩm
    // =====================================================================

    /** Một mục trong ô chọn loại. {@code code < 0} là "tất cả". */
    private static final class TypeOpt {

        final int code;
        final String label;

        TypeOpt(int code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private JComponent buildCatalog() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(new JLabel("Loại:"));
        cbType.setPreferredSize(new Dimension(230, 26));
        top.add(cbType);
        top.add(new JLabel("   Tìm theo tên / ID:"));
        top.add(fSearch);
        lblCount.setForeground(GREY);
        top.add(lblCount);
        root.add(top, BorderLayout.NORTH);

        catTable.setRowHeight(32);
        // Cho chon nhieu dong: don mot loat vat pham hong (tab "Kiem tra du
        // lieu" dang bao 41 mon) ma phai bam tung cai thi khong ai lam noi.
        catTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        catTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        catTable.setAutoCreateRowSorter(true);
        catTable.getColumnModel().getColumn(0)
                .setCellRenderer(PlayerManagerPanel.veIcon());
        int[] w = {40, 55, 70, 210, 130, 60, 70, 90, 70, 55, 55, 55, 55, 220};
        // Duyet theo SO COT THAT chu khong theo do dai mang: doi cot ma quen
        // sua mang la nem ArrayIndexOutOfBounds ngay luc dung panel, va may chu
        // chet truoc khi kip mo cong.
        for (int i = 0; i < catTable.getColumnCount() && i < w.length; i++) {
            catTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        // Bam vao o tick la luu thang xuong CSDL. Khong dung nut "Luu" rieng:
        // mot dau tick ma bat nguoi dung bam hai lan thi de quen.
        catModel.addTableModelListener(e -> {
            if (dangDoBang || e.getColumn() != COT_DUNG_DUOC
                    || e.getType() != javax.swing.event.TableModelEvent.UPDATE) {
                return;
            }
            int hang = e.getFirstRow();
            if (hang < 0 || hang >= catModel.getRowCount()) {
                return;
            }
            int id;
            try {
                id = Integer.parseInt(
                        String.valueOf(catModel.getValueAt(hang, 1)).trim());
            } catch (NumberFormatException ex) {
                return;
            }
            boolean bat = Boolean.TRUE.equals(
                    catModel.getValueAt(hang, COT_DUNG_DUOC));
            String loi = nro.repository.dao.MauVatPhamDAO.datDungDuoc(id, bat);
            if (loi != null) {
                note(WARN_RED, loi);
                // Ghi that bai thi tra o tick ve dung trang thai trong CSDL,
                // khong de man hinh noi mot dang ma CSDL mot dang.
                dangDoBang = true;
                try {
                    catModel.setValueAt(
                            nro.repository.dao.MauVatPhamDAO.dungDuoc(id), hang,
                            COT_DUNG_DUOC);
                } finally {
                    dangDoBang = false;
                }
                return;
            }
            note(OK_GREEN, "Vật phẩm " + id + ": "
                    + (bat ? "đã đánh dấu dùng được." : "đã bỏ đánh dấu."));
        });

        // Chọn một dòng là điền luôn ID sang bên phải — khỏi phải gõ tay.
        catTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int r = catTable.getSelectedRow();
            if (r < 0) {
                return;
            }
            Object id = catModel.getValueAt(catTable.convertRowIndexToModel(r), 1);
            fId.setText(String.valueOf(id));
        });
        root.add(ServerGuiUtils.cuon(catTable), BorderLayout.CENTER);

        cbType.addActionListener(e -> fillCatalog());
        fSearch.getDocument().addDocumentListener(new Simple(this::fillCatalog));
        return root;
    }

    /**
     * Dựng danh sách loại từ dữ liệu thật, kèm số lượng mỗi loại.
     *
     * <p>Đếm theo <b>đúng bộ lọc</b> mà {@link #fillCatalog()} dùng, nếu không con
     * số trên ô chọn và số dòng thật sự hiện ra sẽ lệch nhau.</p>
     */
    private void buildTypeCombo() {
        if (catalogReady || Manager.ITEM_TEMPLATES == null || Manager.ITEM_TEMPLATES.isEmpty()) {
            return;
        }
        Map<Integer, Integer> count = new java.util.TreeMap<>();
        int tong = 0;
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t != null && t.name != null) {
                count.merge((int) t.type, 1, Integer::sum);
                tong++;
            }
        }
        catalogReady = true;
        cbType.addItem(new TypeOpt(-1, "Tất cả (" + PlayerManagerPanel.fmt(tong) + ")"));
        for (Map.Entry<Integer, Integer> e : count.entrySet()) {
            cbType.addItem(new TypeOpt(e.getKey(),
                    PlayerManagerPanel.typeName(e.getKey()) + " — " + e.getValue()));
        }
        fillCatalog();
    }

    /**
     * Đổ lại bảng danh mục.
     *
     * <p>Bọc trong {@code try/finally} để cờ {@link #dangDoBang} luôn được tắt —
     * thân hàm có nhiều nhánh {@code return} sớm, tắt cờ ở cuối thân là có
     * đường thoát bỏ qua và từ đó mọi cú bấm tick im lặng không lưu.</p>
     */
    private void fillCatalog() {
        dangDoBang = true;
        try {
            doBangDanhMuc();
        } finally {
            dangDoBang = false;
        }
    }

    private void doBangDanhMuc() {
        catModel.setRowCount(0);
        if (Manager.ITEM_TEMPLATES == null) {
            return;
        }
        TypeOpt sel = (TypeOpt) cbType.getSelectedItem();
        int type = sel != null ? sel.code : -1;
        String key = fSearch.getText().trim().toLowerCase();

        // Loc truoc, roi SAP XEP THEO LOAI.
        //
        // Bang goc do theo thu tu id nen ao, quan, rada, dau than, ngoc rong
        // nam xen ke nhau — muon xem cho het mot loai phai cuon qua ca danh
        // sach. Cung loai thi giu thu tu id de vi tri on dinh.
        List<ItemTemplate> loc = new ArrayList<>();
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t == null || t.name == null) {
                continue;
            }
            if (type >= 0 && t.type != type) {
                continue;
            }
            if (!key.isEmpty() && !t.name.toLowerCase().contains(key)
                    && !String.valueOf(t.id).equals(key)) {
                continue;
            }
            loc.add(t);
        }
        // Thu tu: LOAI -> HE -> SUC MANH YEU CAU (be den lon) -> id.
        //
        // He xep theo so: 0 Trai Dat, 1 Namec, 2 Xayda, 3 dung chung. Trong moi
        // he thi suc manh yeu cau la thu tu tu nhien — do so cap len cao cap.
        //
        // id la muc CUOI, chi de thu tu ON DINH: hai mon trung ca ba muc tren
        // phai luon xep nhu nhau, khong nhay moi lan nap lai.
        loc.sort((x, y) -> {
            if (x.type != y.type) {
                return Integer.compare(x.type, y.type);
            }
            if (x.gender != y.gender) {
                return Integer.compare(x.gender, y.gender);
            }
            if (x.strRequire != y.strRequire) {
                return Long.compare(x.strRequire, y.strRequire);
            }
            return Integer.compare(x.id, y.id);
        });

        int khop = 0;
        int hien = 0;
        for (ItemTemplate t : loc) {
            khop++;
            if (hien < CATALOG_LIMIT) {
                hien++;
                catModel.addRow(new Object[]{
                    t.iconID, t.id, t.dungDuoc, t.name,
                    PlayerManagerPanel.typeName(t.type),
                    PlayerManagerPanel.itemGender(t.gender),
                    t.isUpToUp ? "có" : "",
                    PlayerManagerPanel.fmt(t.strRequire),
                    t.iconID, t.part, t.head, t.body, t.leg,
                    t.description == null ? "" : t.description.replace('\n', ' ')
                });
            }
        }
        // Noi ro DA CAT O DAU. Danh sach sap theo loai nen phan bi cat luon la
        // cac loai phia sau — khong noi ra thi nhin nhu may chu khong co nhung
        // loai do.
        String cat = "";
        if (hien < khop && hien > 0 && hien < loc.size()) {
            int loaiCuoi = loc.get(hien - 1).type;
            java.util.Set<Integer> conLai = new java.util.TreeSet<>();
            for (int i = hien; i < loc.size(); i++) {
                conLai.add((int) loc.get(i).type);
            }
            conLai.remove(loaiCuoi);
            if (!conLai.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int dem = 0;
                for (int l : conLai) {
                    if (dem++ == 3) {
                        sb.append("…");
                        break;
                    }
                    sb.append(dem == 1 ? "" : ", ")
                            .append(PlayerManagerPanel.typeName(l));
                }
                cat = " — chưa hiện loại: " + sb;
            }
        }
        lblCount.setText(hien < khop
                ? "   Hiện " + PlayerManagerPanel.fmt(hien) + " / "
                  + PlayerManagerPanel.fmt(khop) + cat + " — chọn Loại để xem đủ"
                : "   Hiện " + PlayerManagerPanel.fmt(hien) + " vật phẩm");
    }

    // =====================================================================
    //  Phát vật phẩm
    // =====================================================================

    /** Một người chơi đang online trong ô chọn người nhận. */
    private static final class PlayerOpt {

        final Player p;

        PlayerOpt(Player p) {
            this.p = p;
        }

        @Override
        public String toString() {
            return p.name + "  [id=" + p.id + "]";
        }
    }

    private JComponent buildGive() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);

        // ---------- người nhận + vật phẩm ----------
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(titled("Phát vật phẩm"));

        // Khoa be rong toi thieu = be rong ua thich. Khung hep thi GridBagLayout
        // lui ve kich thuoc TOI THIEU, ma toi thieu cua JTextField gan nhu bang 0
        // — dung la loi "o nhap bi bop lai con vai pixel".
        khoaBeRong(fId, 90);
        khoaBeRong(fQty, 90);
        khoaBeRong(cbPlayer, 240);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Tìm người:"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        khoaBeRong(fTimNguoi, 240);
        form.add(fTimNguoi, c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        form.add(button("Làm mới", GREY, e -> {
            refreshPlayers();
            napDanhSachSet();
        }), c);

        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Người nhận:"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(cbPlayer, c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 2;
        lblSoNguoi.setForeground(GREY);
        form.add(lblSoNguoi, c);

        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("ID vật phẩm:"), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        form.add(fId, c);
        c.gridx = 2;
        form.add(lblIcon, c);
        c.gridx = 3;
        form.add(button("Chọn...", GREY, e -> {
            int id = OptionPicker.chonVatPham(this, -1);
            if (id >= 0) {
                fId.setText(String.valueOf(id));
            }
        }), c);

        // Ten vat pham xuong HANG RIENG chu khong nam canh o nhap: ten dai
        // (vi du "Vien Capsule dac biet") an het cho cua o nhap khi khung hep.
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        lblName.setForeground(ACCENT);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(lblName, c);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;

        c.gridx = 0;
        c.gridy = 4;
        form.add(new JLabel("Số lượng:"), c);
        c.gridx = 1;
        form.add(fQty, c);
        c.gridx = 2;
        form.add(cbGiaoDich, c);

        c.gridx = 0;
        c.gridy = 5;
        form.add(new JLabel("Set kích hoạt:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        napDanhSachSet();
        form.add(cbSet, c);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;

        // Mot hang duy nhat cho ca sao: o so + tom tat + nut chon.
        //
        // Truoc do tach thanh hai hang, lam form cao them hai dong va o "So o
        // sao" bi cat khoi khung ben phai — nhin thay nhung khong go duoc.
        c.gridx = 0;
        c.gridy = 6;
        form.add(cbCoSao, c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel pSao = new JPanel(new BorderLayout(6, 0));
        pSao.setOpaque(false);
        JPanel pSoO = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pSoO.setOpaque(false);
        pSoO.add(new JLabel("số ô:"));
        pSoO.add(fSoOSao);
        pSao.add(pSoO, BorderLayout.WEST);
        fSaoGan.setEditable(false);
        fSaoGan.setBackground(new Color(245, 245, 245));
        veTomTatSao();
        pSao.add(fSaoGan, BorderLayout.CENTER);
        JButton btnChonSao = button("Chọn sao…", ACCENT, e -> chonSaoDialog());
        pSao.add(btnChonSao, BorderLayout.EAST);
        // Khong tich thi khoa cac o lai cho ro rang, khoi go vao roi thac mac
        // vi sao khong co tac dung.
        Runnable batTatSao = () -> {
            boolean bat = cbCoSao.isSelected();
            fSoOSao.setEnabled(bat);
            fSaoGan.setEnabled(bat);
            btnChonSao.setEnabled(bat);
        };
        cbCoSao.addActionListener(e -> batTatSao.run());
        batTatSao.run();
        form.add(pSao, c);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;

        root.add(form, BorderLayout.NORTH);

        // ---------- chỉ số ----------
        JPanel opts = new JPanel(new BorderLayout(0, 4));
        opts.setOpaque(false);
        opts.setBorder(titled("Chỉ số của vật phẩm (sửa trực tiếp trong bảng)"));

        optTable.setRowHeight(24);
        optTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        optTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        OptionPicker.install(optTable, 0);
        optTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        optTable.getColumnModel().getColumn(2).setPreferredWidth(320);
        // Gõ id chỉ số tới đâu hiện ý nghĩa tới đó -> tránh đặt nhầm.
        optModel.addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                SwingUtilities.invokeLater(this::refreshOptMeanings);
            }
        });
        opts.add(ServerGuiUtils.cuon(optTable), BorderLayout.CENTER);

        JPanel optBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        optBtn.setOpaque(false);
        optBtn.add(button("Thêm dòng", ACCENT, e -> optModel.addRow(new Object[]{"0", "0", ""})));
        optBtn.add(button("Xoá dòng", WARN_RED, e -> {
            int r = optTable.getSelectedRow();
            if (r >= 0) {
                optModel.removeRow(r);
            }
        }));
        optBtn.add(button("Xoá hết", GREY, e -> optModel.setRowCount(0)));
        optBtn.add(button("Chỉ số gốc", new Color(120, 90, 160),
                e -> dienChiSoSanCo(true)));
        // Do khoa: mot o tich thay vi bat nguoi dung nho id 30.
        optBtn.add(OptionPicker.oKhoaChoBang(optModel, 3));
        opts.add(optBtn, BorderLayout.SOUTH);
        root.add(opts, BorderLayout.CENTER);

        // ---------- nút phát ----------
        // Lưới 2 cột chứ KHÔNG dùng FlowLayout: cột phải hẹp nên FlowLayout
        // phải xuống hàng, mà preferredSize của nó vẫn báo đúng MỘT hàng —
        // BorderLayout.SOUTH cấp đúng chừng đó chiều cao rồi cắt cụt các hàng
        // sau. Đó là lý do "Sửa mẫu vật phẩm", "Sửa mô tả" và "Xoá vật phẩm"
        // biến mất khỏi màn hình. GridLayout(0, 2) tính chiều cao theo số hàng
        // thật nên nút nào cũng hiện.
        JPanel act = new JPanel(new GridLayout(0, 2, 6, 6));
        act.setOpaque(false);
        act.setBorder(new EmptyBorder(6, 0, 0, 0));
        act.add(button("Thêm vào hành trang", OK_GREEN, e -> give(true)));
        act.add(button("Thêm vào rương ở nhà", ACCENT, e -> give(false)));
        act.add(button("Thêm vật phẩm", OK_GREEN, e -> themMauVatPham()));
        act.add(button("Sửa mẫu vật phẩm", ACCENT, e -> suaMauVatPham()));
        act.add(button("Sửa mô tả", GREY, e -> suaMoTa()));
        act.add(button("Xoá vật phẩm", WARN_RED, e -> xoaMauVatPham()));
        root.add(act, BorderLayout.SOUTH);

        fId.getDocument().addDocumentListener(new Simple(this::onIdChanged));
        fTimNguoi.getDocument().addDocumentListener(new Simple(this::refreshPlayers));
        return root;
    }

    /** Gõ id tới đâu hiện tên và ảnh tới đó — tránh phát nhầm vật phẩm. */
    private void onIdChanged() {
        ItemTemplate t = PlayerManagerPanel.templateOf(fId.getText());
        if (t == null) {
            lblName.setText("(không có id này)");
            lblName.setForeground(WARN_RED);
            lblIcon.setIcon(null);
            return;
        }
        String vichSao = PlayerManagerPanel.rawUnsafe(t);
        lblName.setText(t.name + "   " + PlayerManagerPanel.typeName(t.type));
        lblName.setForeground(ACCENT);
        lblIcon.setIcon(PlayerManagerPanel.iconOf(t.iconID));
        // Ao, quan, gang, giay, rada... deu co chi so san trong game — dien
        // luon de chi viec sua, khoi phai tu tra tung id. Chi dien khi bang con
        // nguyen nhu lan dien truoc, de khong xoa mat so ai do vua go.
        dienChiSoSanCo(false);
        if (vichSao != null) {
            note(GREY, "\"" + t.name + "\": " + vichSao
                    + " — thêm vào hành trang thì game tự xử lý đúng, "
                    + "còn rương ở nhà sẽ từ chối.");
        } else {
            note(GREY, " ");
        }
    }

    /** Nạp lại danh sách người đang online vào ô chọn người nhận. */
    private void refreshPlayers() {
        if (!ServerManager.isRunning) {
            return;
        }
        Object cur = cbPlayer.getSelectedItem();
        long curId = cur instanceof PlayerOpt ? ((PlayerOpt) cur).p.id : -1;
        String key = fTimNguoi.getText().trim().toLowerCase();
        int tong = 0;
        cbPlayer.removeAllItems();
        try {
            for (Player p : Client.gI().getPlayersSnapshot()) {
                if (p == null || p.name == null) {
                    continue;
                }
                tong++;
                if (!key.isEmpty() && !p.name.toLowerCase().contains(key)
                        && !String.valueOf(p.id).contains(key)) {
                    continue;
                }
                cbPlayer.addItem(new PlayerOpt(p));
            }
            lblSoNguoi.setText("  " + cbPlayer.getItemCount() + " / " + tong + " người online");
        } catch (Exception ex) {
            Logger.logException(ItemManagerPanel.class, ex, "Lỗi đọc danh sách người chơi");
            return;
        }
        // Giữ nguyên người đang chọn qua mỗi lần làm mới, nếu họ còn online.
        for (int i = 0; i < cbPlayer.getItemCount(); i++) {
            if (cbPlayer.getItemAt(i).p.id == curId) {
                cbPlayer.setSelectedIndex(i);
                break;
            }
        }
    }

    /** Điền cột "Ý nghĩa" theo id chỉ số đang gõ. */
    private void refreshOptMeanings() {
        for (int i = 0; i < optModel.getRowCount(); i++) {
            int id;
            try {
                id = Integer.parseInt(String.valueOf(optModel.getValueAt(i, 0)).trim());
            } catch (NumberFormatException ex) {
                optModel.setValueAt("(id phải là số)", i, 2);
                continue;
            }
            // Qua OptionPicker: no co san duong doc CSDL khi bang chi so
            // trong bo nho chua nap xong.
            optModel.setValueAt(OptionPicker.tenCua(id), i, 2);
        }
    }

    // =====================================================================
    //  Chỉ số sẵn có của vật phẩm
    // =====================================================================

    /**
     * Dấu vết của lần tự điền gần nhất, để biết bảng có còn nguyên hay không.
     *
     * <p>Rỗng nghĩa là chưa tự điền lần nào, hoặc người dùng đã tự sửa.</p>
     */
    private String optDauVetTuDien = "";

    /**
     * Chỉ số <b>sẵn có trong game</b> của một mẫu vật phẩm.
     *
     * <h3>Lấy ở đâu ra</h3>
     *
     * <p>Ở chính bảng bán của NPC. Áo, quần, găng, giày, rađa… đều được bày bán
     * ở đâu đó, và dòng bán ấy mang đúng bộ chỉ số mà người chơi nhận được khi
     * mua — nên nó là con số thật của món đồ, không phải con số tôi đoán.</p>
     *
     * <p>Món không bày bán ở NPC nào (đồ rơi từ boss, đồ sự kiện) thì trả về
     * danh sách rỗng; lúc ấy {@link #khungChiSoTheoLoai} dựng sẵn khung trống
     * đúng loại để chỉ việc điền số.</p>
     *
     * @return các cặp {@code {id chỉ số, giá trị}}, rỗng nếu không tra được
     */
    private static java.util.List<int[]> chiSoSanCo(ItemTemplate t) {
        java.util.List<int[]> ds = new java.util.ArrayList<>();
        if (t == null) {
            return ds;
        }
        try {
            for (nro.entity.shop.Shop s : nro.server.Manager.SHOPS) {
                if (s == null || s.tabShops == null) {
                    continue;
                }
                for (nro.entity.shop.tab.TabShop tab : s.tabShops) {
                    if (tab == null || tab.itemShops == null) {
                        continue;
                    }
                    for (nro.entity.shop.ItemShop is : tab.itemShops) {
                        if (is == null || is.temp == null || is.temp.id != t.id
                                || is.options == null || is.options.isEmpty()) {
                            continue;
                        }
                        for (nro.entity.item.ItemOption o : is.options) {
                            if (o != null) {
                                ds.add(new int[]{o.optionTemplate.id, o.param});
                            }
                        }
                        return ds;
                    }
                }
            }
        } catch (Exception ex) {
            // Bang shop chua nap xong (panel mo truoc khi may chu chay) thi coi
            // nhu khong tra duoc — van dung khung trong theo loai.
            ds.clear();
        }
        return ds;
    }

    /**
     * Khung chỉ số trống của một loại trang bị — đúng dòng, giá trị để 0.
     *
     * <p>Dành cho món không bày bán ở NPC nào. Biết một cái áo thì phải có dòng
     * HP còn cái găng thì phải có dòng sức đánh là việc của máy, không nên bắt
     * người dùng nhớ id nào đi với loại nào.</p>
     */
    private static java.util.List<int[]> khungChiSoTheoLoai(int type) {
        java.util.List<int[]> ds = new java.util.ArrayList<>();
        switch (type) {
            case 0:                             // Áo
                ds.add(new int[]{47, 0});       // HP
                break;
            case 1:                             // Quần
                ds.add(new int[]{22, 0});       // giáp
                break;
            case 2:                             // Găng
                ds.add(new int[]{0, 0});        // sức đánh
                break;
            case 3:                             // Giày
                ds.add(new int[]{23, 0});       // né đòn
                break;
            case 4:                             // Rađa
                ds.add(new int[]{14, 0});       // chí mạng
                break;
            default:
                return ds;                      // loại khác: không dựng khung
        }
        ds.add(new int[]{21, 0});               // yêu cầu sức mạnh
        return ds;
    }

    /** Loại này có phải trang bị mang chỉ số không. */
    private static boolean laTrangBi(int type) {
        return type >= 0 && type <= 4;
    }

    /** Chuỗi nhận dạng nội dung bảng chỉ số, để so xem có ai sửa tay chưa. */
    private String dauVetBangChiSo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < optModel.getRowCount(); i++) {
            sb.append(optModel.getValueAt(i, 0)).append('=')
                    .append(optModel.getValueAt(i, 1)).append(';');
        }
        return sb.toString();
    }

    /**
     * Điền chỉ số sẵn có của mẫu vật phẩm vào bảng.
     *
     * @param batBuoc {@code true} là ghi đè dù người dùng đã sửa tay — dùng cho
     *        nút bấm; {@code false} là chỉ điền khi bảng còn nguyên như lần tự
     *        điền trước, để không xoá mất số ai đó vừa gõ
     */
    private void dienChiSoSanCo(boolean batBuoc) {
        ItemTemplate t = PlayerManagerPanel.templateOf(fId.getText());
        if (t == null) {
            return;
        }
        if (!batBuoc && !dauVetBangChiSo().equals(optDauVetTuDien)) {
            return;
        }
        java.util.List<int[]> ds = chiSoSanCo(t);
        boolean tuShop = !ds.isEmpty();
        if (ds.isEmpty()) {
            ds = khungChiSoTheoLoai(t.type);
        }
        if (ds.isEmpty()) {
            if (batBuoc) {
                note(GREY, "\"" + t.name + "\" không có chỉ số sẵn trong game "
                        + "và cũng không phải trang bị — cứ tự thêm dòng.");
            }
            if (!batBuoc && optModel.getRowCount() > 0) {
                optModel.setRowCount(0);
                optDauVetTuDien = dauVetBangChiSo();
            }
            return;
        }
        optModel.setRowCount(0);
        for (int[] o : ds) {
            optModel.addRow(new Object[]{String.valueOf(o[0]),
                String.valueOf(o[1]), OptionPicker.tenCua(o[0])});
        }
        optDauVetTuDien = dauVetBangChiSo();
        if (batBuoc || tuShop) {
            note(GREY, tuShop
                    ? "Đã lấy chỉ số của \"" + t.name + "\" theo đúng bảng bán ở NPC "
                    + "— sửa thoải mái trước khi phát."
                    : "\"" + t.name + "\" không bày bán ở NPC nào nên không tra được "
                    + "chỉ số thật; đây là khung trống đúng loại, điền số vào là được.");
        }
    }

    /**
     * Phát vật phẩm đang cấu hình cho người nhận.
     *
     * @param bag {@code true} là hành trang, {@code false} là rương ở nhà
     */
    private void give(boolean bag) {
        Object sel = cbPlayer.getSelectedItem();
        if (!(sel instanceof PlayerOpt)) {
            note(WARN_RED, "Chưa chọn người nhận. Chỉ phát được cho người đang online.");
            return;
        }
        final Player p = ((PlayerOpt) sel).p;

        final ItemTemplate t = PlayerManagerPanel.templateOf(fId.getText());
        if (t == null) {
            note(WARN_RED, "ID vật phẩm không tồn tại.");
            return;
        }
        final int qty;
        try {
            qty = Integer.parseInt(fQty.getText().trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Số lượng phải là số nguyên.");
            return;
        }
        if (qty <= 0) {
            note(WARN_RED, "Số lượng phải lớn hơn 0.");
            return;
        }

        final List<ItemOption> opts;
        try {
            opts = readOptions();
        } catch (Exception ex) {
            note(WARN_RED, "Bảng chỉ số sai: " + ex.getMessage());
            return;
        }
        // Chi so cua set kich hoat. Gan TRUOC khoa giao dich de chi so 30 luon
        // nam cuoi danh sach, giong moi cho khac trong game.
        int[] chiSoSet = chiSoSetDangChon(t);
        if (cbSet.getSelectedIndex() > 0 && !laTrangBi(t)) {
            note(WARN_RED, "\"" + t.name + "\" không phải trang bị (áo, quần, găng, "
                    + "giày, rada) nên không gắn set kích hoạt được.");
            return;
        }
        java.util.Set<Integer> daCoChiSo = new java.util.HashSet<>();
        for (ItemOption io : opts) {
            if (io != null && io.optionTemplate != null) {
                daCoChiSo.add(io.optionTemplate.id);
            }
        }
        for (int id : chiSoSet) {
            // Bang chi so ben tren co the da co dong nay -> khong gan hai lan,
            // vi client in moi dong mot lan, trung la nguoi choi tuong bi loi.
            if (daCoChiSo.add(id)) {
                opts.add(new ItemOption(id, 0));
            }
        }

        // ---------- Sao pha lê ----------
        //
        // Hai chi so rieng biet: 107 la SO O sao (cho trong), 102 la SO SAO DA
        // GAN. Gan sao con phai cong chi so rieng cua vien sao do — dat 102 ma
        // quen thi mon do hien co sao nhung khong cong gi, y het mot loi that.
        int soOSao = 0;
        if (cbCoSao.isSelected()) {
            try {
                soOSao = Integer.parseInt(fSoOSao.getText().trim());
            } catch (NumberFormatException ex) {
                note(WARN_RED, "Số ô sao phải là số nguyên.");
                return;
            }
        }
        if (soOSao < 0) {
            note(WARN_RED, "Số ô sao không được âm.");
            return;
        }
        int soSaoGan = cbCoSao.isSelected() ? tongSaoGan() : 0;
        if (soSaoGan > soOSao) {
            note(WARN_RED, "Đang gắn " + soSaoGan + " sao mà chỉ có " + soOSao
                    + " ô — game không cho gắn quá số ô.");
            return;
        }
        if ((soOSao > 0 || soSaoGan > 0) && !laTrangBi(t)) {
            note(WARN_RED, "\"" + t.name + "\" không phải trang bị nên không có "
                    + "ô sao pha lê.");
            return;
        }
        if (soOSao > 0) {
            opts.removeIf(io -> io != null && io.optionTemplate != null
                    && io.optionTemplate.id == 107);
            opts.add(new ItemOption(107, soOSao));
        }
        if (soSaoGan > 0) {
            opts.removeIf(io -> io != null && io.optionTemplate != null
                    && io.optionTemplate.id == 102);
            opts.add(new ItemOption(102, soSaoGan));
            // Cong chi so cua TUNG loai sao, mỗi loai nhan theo so luong — dung
            // nhu EpSaoTrangBi lam khi nguoi choi nap tung vien vao tung o.
            for (Map.Entry<Integer, Integer> e : saoGan.entrySet()) {
                ItemOption chuan = chiSoCuaSao(e.getKey());
                if (chuan == null) {
                    continue;
                }
                int idCs = chuan.optionTemplate.id;
                int cong = chuan.param * e.getValue();
                boolean daCoCs = false;
                for (ItemOption io : opts) {
                    if (io != null && io.optionTemplate != null
                            && io.optionTemplate.id == idCs) {
                        io.param += cong;
                        daCoCs = true;
                        break;
                    }
                }
                if (!daCoCs) {
                    opts.add(new ItemOption(idCs, cong));
                }
            }
        }

        // Khoa giao dich la mot CHI SO gan vao vat pham (id 30), khong phai cot
        // rieng. Bo tick thi them vao, tick thi go ra neu bang chi so co san.
        opts.removeIf(io -> io != null && io.optionTemplate != null
                && io.optionTemplate.id == 30);
        if (!cbGiaoDich.isSelected()) {
            opts.add(new ItemOption(30, 0));
        }

        // Rương ở nhà không có hàm phát của game -> phải chặn vật phẩm đặc biệt
        // ở đây, nếu không client sẽ vỡ giao diện.
        String vichSao = PlayerManagerPanel.rawUnsafe(t);
        if (!bag && vichSao != null) {
            JOptionPane.showMessageDialog(this,
                    "Không đặt \"" + t.name + "\" vào rương được: " + vichSao + ".\n"
                    + "Hãy dùng \"Thêm vào hành trang\" — game sẽ xử lý đúng cách.",
                    "Vật phẩm không nằm trong ô", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kho = bag ? "hành trang" : "rương ở nhà";
        int ok = JOptionPane.showConfirmDialog(this,
                "Phát \"" + t.name + "\" x" + qty + " vào " + kho + " của " + p.name + "?"
                + (opts.isEmpty() ? "" : "\nKèm " + opts.size() + " chỉ số.")
                + (chiSoSet.length == 0 ? ""
                        : "\nGắn set: " + cbSet.getSelectedItem()
                        + " (" + chiSoSet.length + " dòng chỉ số).")
                + (soOSao == 0 ? "" : "\n" + soOSao + " ô sao"
                        + (soSaoGan > 0 ? " — " + fSaoGan.getText() : ""))
                + (cbGiaoDich.isSelected() ? "" : "\nMón này KHÔNG giao dịch được."),
                "Xác nhận phát", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        boolean added;
        try {
            Item it = ItemService.gI().createNewItem(t.id, qty);
            it.itemOptions = opts;
            it.info = it.getInfo();
            it.content = it.getContent();
            if (bag) {
                // Đúng hàm mà game dùng khi người chơi nhặt đồ: nó biết Vàng thì
                // cộng vào ví, Bùa thì cộng thời gian, "mở rộng túi" thì thêm ô…
                added = InventoryService.gI().addItemBag(p, it);
                if (added) {
                    InventoryService.gI().sendItemBag(p);
                }
            } else {
                added = putInBox(p, it);
                if (added) {
                    InventoryService.gI().sendItemBox(p);
                }
            }
        } catch (Exception ex) {
            Logger.logException(ItemManagerPanel.class, ex, "Lỗi phát vật phẩm cho " + p.name);
            note(WARN_RED, "Lỗi khi phát: " + ex.getMessage());
            return;
        }

        if (!added) {
            note(WARN_RED, kho.substring(0, 1).toUpperCase() + kho.substring(1)
                    + " của " + p.name + " đã đầy — chưa phát được.");
            return;
        }
        LamMoi.bao(LamMoi.NGUOI_CHOI);
        note(OK_GREEN, "Đã phát \"" + t.name + "\" x" + qty + " vào " + kho
                + " của " + p.name + " — đang lưu CSDL...");
        save(p, t.name);
    }

    /** Đặt vào ô trống đầu tiên của rương. {@code false} nếu rương đã đầy. */
    private static boolean putInBox(Player p, Item it) {
        if (p.inventory == null || p.inventory.itemsBox == null) {
            return false;
        }
        List<Item> box = p.inventory.itemsBox;
        for (int i = 0; i < box.size(); i++) {
            Item cur = box.get(i);
            if (cur == null || !cur.isNotNullItem()) {
                box.set(i, it);
                return true;
            }
        }
        return false;
    }

    /** Đọc bảng chỉ số thành danh sách option. Ném lỗi nếu có ô không phải số. */
    private List<ItemOption> readOptions() {
        List<ItemOption> out = new ArrayList<>();
        for (int i = 0; i < optModel.getRowCount(); i++) {
            String sId = String.valueOf(optModel.getValueAt(i, 0)).trim();
            String sVal = String.valueOf(optModel.getValueAt(i, 1)).trim();
            if (sId.isEmpty()) {
                continue;
            }
            int id;
            int param;
            try {
                id = Integer.parseInt(sId);
                param = Integer.parseInt(sVal);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("dòng " + (i + 1) + " không phải số");
            }
            // Kiem qua OptionPicker cho khop voi cot "Y nghia": doi chieu bang
            // trong bo nho thoi thi luc no chua nap xong se chan nham moi dong.
            if ("(không có id này)".equals(OptionPicker.tenCua(id))) {
                throw new IllegalArgumentException("dòng " + (i + 1) + " có id chỉ số " + id
                        + " không tồn tại");
            }
            out.add(new ItemOption(id, param));
        }
        return out;
    }

    /**
     * Lưu người chơi xuống CSDL sau khi phát.
     *
     * <p>Cần thiết vì {@code Service.AutoSavedDataBase()} chỉ chạy mỗi <b>một
     * tiếng</b>; không lưu ngay thì máy chủ tắt bất thường là mất món vừa phát.</p>
     */
    private void save(Player p, String tenItem) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    PlayerDAO.updatePlayer(p);
                    return true;
                } catch (Exception ex) {
                    Logger.logException(ItemManagerPanel.class, ex, "Lỗi lưu CSDL");
                    return false;
                }
            }

            @Override
            protected void done() {
                boolean ok = false;
                try {
                    ok = get();
                } catch (Exception ignored) {
                }
                note(ok ? OK_GREEN : WARN_RED, ok
                        ? "Đã phát \"" + tenItem + "\" cho " + p.name + " và lưu CSDL."
                        : "Đã phát vào bộ nhớ nhưng LƯU CSDL THẤT BẠI.");
            }
        }.execute();
    }

    // =====================================================================
    //  Tiện ích giao diện
    // =====================================================================

    /**
     * Khoá bề rộng tối thiểu của một ô nhập.
     *
     * <p>Mặc định {@code JTextField.getMinimumSize()} gần như bằng 0. Khi khung
     * chứa hẹp hơn bề rộng ưa thích, {@code GridBagLayout} lùi về kích thước tối
     * thiểu — và ô nhập co lại chỉ còn vài pixel, không gõ được. Đặt tối thiểu
     * bằng một con số thật thì ô luôn còn dùng được, khung hẹp thì cắt chỗ khác.</p>
     */
    private static void khoaBeRong(javax.swing.JComponent comp, int rong) {
        Dimension d = new Dimension(rong, comp.getPreferredSize().height);
        comp.setMinimumSize(d);
        comp.setPreferredSize(d);
    }

    /**
     * Sửa mô tả của mẫu vật phẩm đang chọn trong danh mục.
     *
     * <p>Ghi thẳng vào {@code item_template.description}. Client đọc mô tả từ gói
     * bảng vật phẩm gửi lúc đăng nhập, nên phải <b>tăng phiên bản</b> ở tab
     * "Cấu Hình → Đẩy dữ liệu cho client" thì người chơi mới thấy mô tả mới.</p>
     */
    /**
     * Sửa <b>mẫu vật phẩm</b> trong bảng {@code item_template}.
     *
     * <h3>Head / Body / Leg là gì</h3>
     *
     * <p>Ba id trỏ vào bảng {@code part} — bộ ảnh vẽ nhân vật khi mặc món này.
     * Áo dùng {@code body}, quần dùng {@code leg}, còn cải trang dùng cả ba vì
     * nó thay luôn cả đầu. {@code -1} nghĩa là món không đổi phần đó.</p>
     *
     * <p><b>Đây là chỗ dễ làm hỏng hình nhất.</b> Trỏ vào một id không có trong
     * bảng {@code part} thì client vẽ ra khoảng trống hoặc lấy nhầm ảnh của món
     * khác. Hộp thoại kiểm tra ngay lúc gõ và báo id nào không tồn tại — nhưng
     * vẫn cho lưu, vì có bản mod thêm part sau.</p>
     *
     * <h3>Sửa xong phải đẩy dữ liệu</h3>
     *
     * <p>Client giữ bảng vật phẩm trong bộ nhớ đệm và chỉ tải lại khi số phiên
     * bản đổi. Không bấm <b>Cấu Hình → Đẩy dữ liệu cho client</b> thì người chơi
     * vẫn thấy y như cũ.</p>
     */
    /**
     * Xoá một mẫu vật phẩm khỏi danh mục.
     *
     * <p>Id vật phẩm <b>chính là chỉ số mảng</b>
     * ({@code ItemService.getTemplate(id)} = {@code ITEM_TEMPLATES.get(id)}), nên
     * chỉ id lớn nhất mới xoá hẳn được. Id ở giữa thì đổi sang <b>vô hiệu</b>:
     * giữ nguyên dòng cho chỉ số khỏi xê dịch, nhưng làm nó thành ô rỗng.</p>
     */
    private void xoaMauVatPham() {
        int[] hang = catTable.getSelectedRows();
        if (hang.length == 0) {
            note(WARN_RED, "Chưa chọn vật phẩm nào trong danh mục.");
            return;
        }
        if (hang.length > 1) {
            xoaNhieuMau(hang);
            return;
        }
        ItemTemplate t = mauDangChon();
        if (t == null) {
            return;
        }
        int id = t.id;
        int max = nro.repository.dao.MauVatPhamDAO.idLonNhat();
        boolean cuoiBang = id == max;

        java.util.List<nro.repository.dao.MauVatPhamDAO.ChoDung> dung =
                nro.repository.dao.MauVatPhamDAO.dangDungODau(id);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><div style='width:480px'>");
        sb.append("<b>").append(esc(t.name)).append("</b>  [id=").append(id)
                .append("]<br><br>");
        if (dung.isEmpty()) {
            sb.append("<span style='color:#1a7f37'>Không có chỗ nào đang dùng "
                    + "món này.</span><br><br>");
        } else {
            sb.append("<span style='color:#b00020'><b>Đang có: ")
                    .append(esc(nro.repository.dao.MauVatPhamDAO.moTaChoDung(dung)))
                    .append("</b></span><br><br>");
        }
        if (cuoiBang) {
            sb.append("Đây là id <b>lớn nhất</b> nên xoá hẳn được — dòng biến mất "
                    + "khỏi CSDL.");
        } else {
            sb.append("Đây <b>không phải</b> id lớn nhất (lớn nhất là ")
                    .append(max).append(").<br>")
                    .append("Id vật phẩm chính là chỉ số mảng mà máy chủ tra cứu, "
                            + "nên xoá hẳn id ở giữa sẽ kéo mọi id lớn hơn lùi một "
                            + "bậc — <b>mọi món trong túi người chơi có id cao hơn "
                            + "sẽ biến thành món khác</b>.<br><br>")
                    .append("Chỉ <b>vô hiệu</b> được: dòng vẫn còn (chỉ số không xê "
                            + "dịch) nhưng thành ô rỗng, tên đổi thành \"")
                    .append(nro.repository.dao.MauVatPhamDAO.TEN_DA_XOA)
                    .append("\".");
        }
        sb.append("</div></html>");

        // Chi moi lua chon "go khoi tui" khi THUC SU co nguoi dang giu —
        // khong co ai ma van hoi thi chi lam roi hop thoai.
        boolean coNguoiGiu = nro.repository.dao.MauVatPhamDAO.soNguoiDangGiu(id) > 0;
        String[] chon = cuoiBang
                ? (coNguoiGiu
                        ? new String[]{"Xoá hẳn", "Xoá + gỡ khỏi túi", "Vô hiệu", "Huỷ"}
                        : new String[]{"Xoá hẳn", "Vô hiệu", "Huỷ"})
                : (coNguoiGiu
                        ? new String[]{"Vô hiệu + gỡ khỏi túi", "Vô hiệu", "Huỷ"}
                        : new String[]{"Vô hiệu", "Huỷ"});
        int ok = JOptionPane.showOptionDialog(this, new JLabel(sb.toString()),
                "Xoá vật phẩm " + id, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, chon, chon[chon.length - 1]);
        if (ok < 0 || chon[ok].equals("Huỷ")) {
            return;
        }

        int daGo = -2;
        if (chon[ok].contains("gỡ khỏi túi")) {
            int soNguoi = nro.repository.dao.MauVatPhamDAO.soNguoiDangGiu(id);
            if (JOptionPane.showConfirmDialog(this,
                    "<html><div style='width:420px'>Sẽ gỡ món này khỏi túi, rương và"
                    + " hòm thư của <b>" + soNguoi + " nhân vật</b>.<br><br>"
                    + "Ô chứa nó thành ô trống, <b>không hoàn lại gì</b>."
                    + "<br><br><b>Không thể hoàn tác.</b> Nên sao lưu CSDL trước."
                    + "<br><br>Người đang online cần vào lại, nếu không dữ liệu"
                    + " trong bộ nhớ sẽ ghi đè lại.</div></html>",
                    "Xác nhận gỡ khỏi túi người chơi",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            daGo = nro.repository.dao.MauVatPhamDAO.goKhoiTuiNguoiChoi(id);
            if (daGo < 0) {
                JOptionPane.showMessageDialog(this,
                        "Không gỡ được khỏi túi người chơi — xem log máy chủ.",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        String loi = chon[ok].startsWith("Xoá hẳn")
                ? nro.repository.dao.MauVatPhamDAO.xoa(id)
                : nro.repository.dao.MauVatPhamDAO.voHieu(id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không thực hiện được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        fillCatalog();
        LamMoi.bao(LamMoi.VAT_PHAM);
        note(OK_GREEN, (chon[ok].startsWith("Xoá hẳn") ? "Đã xoá hẳn" : "Đã vô hiệu")
                + (daGo >= 0 ? " và gỡ khỏi túi của " + daGo + " nhân vật," : "")
                + " vật phẩm " + id + " — người chơi phải vào lại để thấy.");
    }

    /**
     * Xoá nhiều mẫu vật phẩm một lượt.
     *
     * <p>Mỗi món tự chọn cách phù hợp: id lớn nhất và không ai dùng thì xoá hẳn,
     * còn lại thì vô hiệu. Món nào đang có người giữ hoặc shop/boss trỏ tới thì
     * <b>bỏ qua</b> và báo tên ra — không im lặng ép xoá.</p>
     */
    private void xoaNhieuMau(int[] hang) {
        java.util.List<int[]> ds = new java.util.ArrayList<>();   // {id}
        java.util.List<String> ten = new java.util.ArrayList<>();
        for (int h : hang) {
            int idx = catTable.convertRowIndexToModel(h);
            try {
                ds.add(new int[]{Integer.parseInt(
                        String.valueOf(catModel.getValueAt(idx, 1)).trim())});
                ten.add(String.valueOf(catModel.getValueAt(idx, 3)));
            } catch (NumberFormatException ignored) {
                // dong khong doc duoc id thi bo qua
            }
        }
        if (ds.isEmpty()) {
            note(WARN_RED, "Không đọc được id của các dòng đang chọn.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><div style='width:520px'>");
        sb.append("Đang chọn <b>").append(ds.size())
                .append("</b> vật phẩm.<br><br>");
        sb.append("Món nào là id lớn nhất và không ai dùng thì <b>xoá hẳn</b>; "
                + "còn lại chỉ <b>vô hiệu</b> (giữ nguyên dòng cho chỉ số khỏi xê "
                + "dịch, đổi tên thành \"")
                .append(nro.repository.dao.MauVatPhamDAO.TEN_DA_XOA)
                .append("\").<br><br>");
        sb.append("Món đang có người giữ, hoặc shop / boss / quái trỏ tới, sẽ "
                + "được <b>bỏ qua</b> và liệt kê lại.");
        sb.append("</div></html>");

        String[] chon = {"Làm đi", "Huỷ"};
        int ok = JOptionPane.showOptionDialog(this, new JLabel(sb.toString()),
                "Xoá " + ds.size() + " vật phẩm", JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, chon, chon[1]);
        if (ok != 0) {
            return;
        }

        int xoaHan = 0;
        int voHieu = 0;
        java.util.List<String> boQua = new java.util.ArrayList<>();
        for (int i = 0; i < ds.size(); i++) {
            int id = ds.get(i)[0];
            int max = nro.repository.dao.MauVatPhamDAO.idLonNhat();
            String loi;
            if (id == max) {
                loi = nro.repository.dao.MauVatPhamDAO.xoa(id);
                if (loi == null) {
                    xoaHan++;
                    continue;
                }
                // Khong xoa han duoc thi thu vo hieu
                loi = nro.repository.dao.MauVatPhamDAO.voHieu(id);
            } else {
                loi = nro.repository.dao.MauVatPhamDAO.voHieu(id);
            }
            if (loi == null) {
                voHieu++;
            } else {
                boQua.add("#" + id + " " + ten.get(i));
            }
        }

        fillCatalog();
        StringBuilder kq = new StringBuilder();
        kq.append("<html><div style='width:520px'>");
        kq.append("Xoá hẳn: <b>").append(xoaHan).append("</b><br>");
        kq.append("Vô hiệu: <b>").append(voHieu).append("</b><br>");
        kq.append("Bỏ qua: <b>").append(boQua.size()).append("</b>");
        if (!boQua.isEmpty()) {
            kq.append("<br><br>Các món bị bỏ qua vì đang được dùng:<br>");
            for (String x : boQua) {
                kq.append("&nbsp;&nbsp;• ").append(esc(x)).append("<br>");
            }
            kq.append("<br>Gỡ hết chỗ dùng (túi người chơi, shop, boss, quái) "
                    + "rồi làm lại.");
        }
        kq.append("</div></html>");
        JOptionPane.showMessageDialog(this, new JLabel(kq.toString()),
                "Kết quả", JOptionPane.INFORMATION_MESSAGE);
        LamMoi.bao(LamMoi.VAT_PHAM);
        note(OK_GREEN, "Xoá hẳn " + xoaHan + ", vô hiệu " + voHieu
                + ", bỏ qua " + boQua.size() + ".");
    }

    /** Chèn văn bản vào HTML mà không để dấu ngoặc phá cấu trúc. */
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }


    /**
     * Them mot MAU vat pham moi vao bang {@code item_template}.
     *
     * <h3>Vi sao KHONG cho go id</h3>
     *
     * <p>Client tra bang vat pham theo <b>vi tri trong danh sach</b> chu khong
     * theo cot id: server gui lan luot tung mon, client danh so 0, 1, 2... theo
     * thu tu nhan duoc. Vi vay id trong CSDL bat buoc phai lien tuc tu 0. Chi
     * can chen mot id thua so hay bo trong mot khoang la moi vat pham tu do tro
     * di bi lech ten va lech anh tren toan may chu.</p>
     *
     * <p>Nen id moi luon la {@code MAX(id) + 1}, khong cho sua.</p>
     */

    /** Danh sach id icon co that trong data/icon/x2 — quet MOT lan roi nho lai. */

    /** Doc so tu o nhap; go sai hay de trong thi tra -1 chu khong nem loi. */
    private static int laySo(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception ex) {
            return -1;
        }
    }

    private static java.util.List<Integer> DS_ICON;

    private static synchronized java.util.List<Integer> dsIcon() {
        if (DS_ICON != null) {
            return DS_ICON;
        }
        java.util.List<Integer> ds = new java.util.ArrayList<>();
        try {
            String[] ten = new java.io.File("data/icon/x2/").list();
            if (ten != null) {
                for (String t : ten) {
                    if (!t.endsWith(".png")) {
                        continue;
                    }
                    try {
                        ds.add(Integer.parseInt(t.substring(0, t.length() - 4)));
                    } catch (NumberFormatException ignored) {
                        // File khong phai dang <so>.png -> bo qua.
                    }
                }
            }
        } catch (Exception ignored) {
            // Khong doc duoc thu muc -> tra danh sach rong, van go tay duoc.
        }
        java.util.Collections.sort(ds);
        DS_ICON = ds;
        return ds;
    }

    /**
     * Bang chon icon dang luoi.
     *
     * <p>Co hon HAI MUOI NGHIN icon nen khong nap anh truoc. {@link javax.swing.JList}
     * chi ve nhung o dang nhin thay, ma {@code iconOf} lai co bo nho dem — nen anh
     * chi duoc doc dung luc can, keo den dau nap den do.</p>
     *
     * @return id icon da chon, hoac {@code -1} neu huy
     */
    /**
     * Hộp chọn icon — dùng chung cho cả tab Bông tai và Chân mệnh.
     *
     * <p>Để {@code static public} vì {@link SystemPanel} cũng cần: hai tab kia
     * cũng cho đặt icon riêng cho từng cấp, mà chép lại cả hộp chọn icon thì
     * sửa một bên là hai bên lệch nhau.</p>
     */
    public static int chonIcon(java.awt.Component cha, int idHienTai) {
        java.util.List<Integer> tatCa = dsIcon();
        javax.swing.DefaultListModel<Integer> model = new javax.swing.DefaultListModel<>();
        for (int id : tatCa) {
            model.addElement(id);
        }

        javax.swing.JList<Integer> ds = new javax.swing.JList<>(model);
        ds.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        ds.setVisibleRowCount(-1);
        ds.setFixedCellWidth(76);
        ds.setFixedCellHeight(60);
        ds.setCellRenderer(new javax.swing.ListCellRenderer<Integer>() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<? extends Integer> list, Integer value,
                    int index, boolean sel, boolean focus) {
                JLabel l = new JLabel(String.valueOf(value),
                        PlayerManagerPanel.iconOf(value), JLabel.CENTER);
                l.setHorizontalTextPosition(JLabel.CENTER);
                l.setVerticalTextPosition(JLabel.BOTTOM);
                l.setOpaque(true);
                l.setBackground(sel ? new Color(0xCCE4FF) : Color.WHITE);
                l.setBorder(new EmptyBorder(4, 2, 4, 2));
                return l;
            }
        });
        if (idHienTai >= 0) {
            ds.setSelectedValue(idHienTai, true);
        }

        JScrollPane sc = ServerGuiUtils.cuon(ds);
        sc.setPreferredSize(new Dimension(620, 420));
        sc.getVerticalScrollBar().setUnitIncrement(40);

        JTextField fTim = new JTextField(10);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.add(new JLabel("Nhảy tới id:"));
        top.add(fTim);
        top.add(new JLabel("(" + tatCa.size() + " icon)"));
        fTim.getDocument().addDocumentListener(new Simple(() -> {
            String q = fTim.getText().trim();
            if (q.isEmpty()) {
                return;
            }
            try {
                int id = Integer.parseInt(q);
                int vt = java.util.Collections.binarySearch(tatCa, id);
                if (vt >= 0) {
                    ds.setSelectedIndex(vt);
                    ds.ensureIndexIsVisible(vt);
                }
            } catch (NumberFormatException ignored) {
                // Go chua xong so -> khong nhay.
            }
        }));

        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.add(top, BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(cha, wrap, "Chọn icon",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return -1;
        }
        Integer chon = ds.getSelectedValue();
        return chon == null ? -1 : chon;
    }

    private void themMauVatPham() {
        int idMoi;
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT COALESCE(MAX(id), -1) AS m FROM item_template");
            rs.next();
            idMoi = rs.getInt("m") + 1;
            rs.dispose();
        } catch (Exception ex) {
            note(WARN_RED, "Không đọc được id lớn nhất: " + ex.getMessage());
            return;
        }

        JTextField fTen = new JTextField("", 26);
        JTextField fIcon = new JTextField("0", 8);
        JTextField fPart = new JTextField("-1", 8);
        JTextField fHead = new JTextField("-1", 8);
        JTextField fBody = new JTextField("-1", 8);
        JTextField fLeg = new JTextField("-1", 8);
        JTextField fSm = new JTextField("0", 12);
        JTextField fMoTa = new JTextField("", 26);

        // Loai chon qua hop rieng chu khong tha xuong tu 0..40 nhu truoc: Linh
        // Thu (70), Chien Linh (71) hay loai 99 deu nam ngoai dai do nen truoc
        // day khong the chon. Giu ma trong mang mot phan tu, khong doc theo vi
        // tri trong o chon — vi tri va ma khong con trung nhau.
        final int[] maLoaiChon = {0};
        JPanel pLoai = oChonLoai(maLoaiChon, new JLabel());

        JComboBox<String> cbHe = new JComboBox<>(
                new String[]{"0 — Trái Đất", "1 — Namếc", "2 — Xayda", "3 — Dùng chung"});
        cbHe.setSelectedIndex(3);

        javax.swing.JCheckBox cbChong = new javax.swing.JCheckBox(
                "Xếp chồng được (nhiều món gộp vào một ô, có số lượng)");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        hang(form, c, y++, "ID (tự cấp):", new JLabel(String.valueOf(idMoi)));
        hang(form, c, y++, "Tên vật phẩm:", fTen);
        hang(form, c, y++, "Loại:", pLoai);
        hang(form, c, y++, "Hệ:", cbHe);
        hang(form, c, y++, "Xếp chồng:", cbChong);
        JPanel pIcon = new JPanel(new BorderLayout(6, 0));
        pIcon.setOpaque(false);
        JLabel xemIcon = new JLabel();
        pIcon.add(fIcon, BorderLayout.CENTER);
        pIcon.add(xemIcon, BorderLayout.WEST);
        pIcon.add(button("Chọn…", ACCENT, ev -> {
            int id = chonIcon(pIcon, laySo(fIcon.getText()));
            if (id >= 0) {
                fIcon.setText(String.valueOf(id));
                xemIcon.setIcon(PlayerManagerPanel.iconOf(id));
            }
        }), BorderLayout.EAST);
        hang(form, c, y++, "Ảnh (icon id):", pIcon);
        // Part/Head/Body/Leg chi co nghia voi do HIEN TREN NGUOI (ao, quan,
        // gang, giay, cai trang, giap, pet, van bay...). Voi nguyen lieu hay
        // do an thi luon la -1. Gap lai sau mot o tich de khoi phai nhin.
        javax.swing.JCheckBox cbHienTrenNguoi = new javax.swing.JCheckBox(
                "Vật phẩm hiển thị trên người (áo, cải trang, pet, ván bay…)");
        Runnable batTatPart = () -> {
            boolean b = cbHienTrenNguoi.isSelected();
            fPart.setEnabled(b);
            fHead.setEnabled(b);
            fBody.setEnabled(b);
            fLeg.setEnabled(b);
            if (!b) {
                fPart.setText("-1");
                fHead.setText("-1");
                fBody.setText("-1");
                fLeg.setText("-1");
            }
        };
        cbHienTrenNguoi.addActionListener(ev -> batTatPart.run());
        batTatPart.run();
        hang(form, c, y++, "", cbHienTrenNguoi);
        hang(form, c, y++, "Part:", fPart);
        hang(form, c, y++, "Head:", fHead);
        hang(form, c, y++, "Body:", fBody);
        hang(form, c, y++, "Leg:", fLeg);
        hang(form, c, y++, "Sức mạnh yêu cầu:", fSm);
        hang(form, c, y++, "Mô tả:", fMoTa);
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#666'>"
                + "ID do máy chủ tự cấp và <b>không sửa được</b> — client tra bảng vật phẩm "
                + "theo thứ tự nên id phải liên tục.<br>"
                + "Máy chủ tự đẩy dữ liệu mới cho client."
                + "</span></html>"), c);

        if (JOptionPane.showConfirmDialog(this, form,
                "Thêm vật phẩm mới — id " + idMoi,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        String ten = fTen.getText().trim();
        if (ten.isEmpty()) {
            note(WARN_RED, "Tên vật phẩm không được để trống.");
            return;
        }
        int icon;
        int part;
        int head;
        int body;
        int leg;
        int sm;
        try {
            icon = Integer.parseInt(fIcon.getText().trim());
            part = Integer.parseInt(fPart.getText().trim());
            head = Integer.parseInt(fHead.getText().trim());
            body = Integer.parseInt(fBody.getText().trim());
            leg = Integer.parseInt(fLeg.getText().trim());
            sm = Integer.parseInt(fSm.getText().trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Ảnh, Part, Head, Body, Leg và Sức mạnh phải là số nguyên.");
            return;
        }
        int loai = maLoaiChon[0];
        int he = Math.max(0, cbHe.getSelectedIndex());
        try {
            // Phai liet ke DU moi cot NOT NULL khong co gia tri mac dinh, neu
            // khong ket noi cua may chu se tu choi: "Field ... doesn't have a
            // default value". Hai cot tung bi bo sot la is_up_to_up va
            // isGender.
            //
            // isGender = -1 la gia tri cua 1991 trong 2447 mon dang co, nghia
            // la khong co xu ly gioi tinh gi dac biet. Chi khi bang 1 thi
            // InventoryService.findItemGender moi dem xia toi.
            nro.repository.ConnectDB.executeUpdate(
                    "INSERT INTO item_template (id, NAME, TYPE, gender, description,"
                    + " icon_id, part, head, body, leg, power_require, is_up_to_up,"
                    + " isGender)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    idMoi, ten, loai, he, fMoTa.getText().trim(),
                    icon, part, head, body, leg, sm,
                    cbChong.isSelected() ? 1 : 0, -1);
        } catch (Exception ex) {
            note(WARN_RED, "Không thêm được: " + ex.getMessage());
            return;
        }

        // Nap luon vao bo nho thay vi bat khoi dong lai may chu. Id moi luon la
        // MAX(id) + 1 nen noi vao CUOI danh sach la giu dung thu tu — client tra
        // bang vat pham theo vi tri chu khong theo id, xen vao giua la lech het.
        ItemTemplate moi = new ItemTemplate();
        moi.id = (short) idMoi;
        moi.type = (byte) loai;
        moi.gender = (byte) he;
        moi.name = ten;
        moi.description = fMoTa.getText().trim();
        moi.level = 0;
        moi.iconID = (short) icon;
        moi.part = (short) part;
        moi.isUpToUp = cbChong.isSelected();
        moi.strRequire = sm;
        moi.head = head;
        moi.body = body;
        moi.leg = leg;
        moi.isGender = -1;
        moi.dungDuoc = true;
        Manager.ITEM_TEMPLATES.add(moi);

        LamMoi.bao(LamMoi.VAT_PHAM);
        note(OK_GREEN, "Đã thêm \"" + ten + "\" (id " + idMoi
                + "). Client đăng nhập lại là thấy.");
    }

    private void suaMauVatPham() {
        ItemTemplate t = mauDangChon();
        if (t == null) {
            return;
        }
        JTextField fTen = new JTextField(t.name == null ? "" : t.name, 26);
        JTextField fIcon = new JTextField(String.valueOf(t.iconID), 8);
        JTextField fPart = new JTextField(String.valueOf(t.part), 8);
        JTextField fHead = new JTextField(String.valueOf(t.head), 8);
        JTextField fBody = new JTextField(String.valueOf(t.body), 8);
        JTextField fLeg = new JTextField(String.valueOf(t.leg), 8);
        JTextField fSm = new JTextField(String.valueOf(t.strRequire), 12);

        final int[] maLoaiChon = {t.type};
        JPanel pLoai = oChonLoai(maLoaiChon, new JLabel());

        JComboBox<String> cbHe = new JComboBox<>(new String[]{
            "0 — Trái Đất", "1 — Namếc", "2 — Xayda", "3 — Tất cả"});
        cbHe.setSelectedIndex(Math.max(0, Math.min(3, t.gender)));

        javax.swing.JCheckBox cbChong = new javax.swing.JCheckBox(
                "Xếp chồng được (nhiều món gộp vào một ô, có số lượng)",
                t.isUpToUp);

        JLabel lblXem = new JLabel();
        JLabel lblPart = new JLabel();
        Runnable soi = () -> {
            lblXem.setIcon(PlayerManagerPanel.iconOf((short) soDuong(fIcon, t.iconID)));
            StringBuilder thieu = new StringBuilder();
            int[][] cap = {{soDuong(fPart, t.part), 0}, {soDuong(fHead, t.head), 1},
                {soDuong(fBody, t.body), 2}, {soDuong(fLeg, t.leg), 3}};
            String[] ten = {"Part", "Head", "Body", "Leg"};
            for (int[] c : cap) {
                if (c[0] >= 0 && !coPart(c[0])) {
                    thieu.append(thieu.length() == 0 ? "" : ", ")
                            .append(ten[c[1]]).append(' ').append(c[0]);
                }
            }
            lblPart.setForeground(thieu.length() == 0 ? OK_GREEN : WARN_RED);
            lblPart.setText(thieu.length() == 0
                    ? "Các id part đều có thật."
                    : "KHÔNG có trong bảng part: " + thieu + " — client sẽ vẽ thiếu.");
        };
        for (JTextField f : new JTextField[]{fIcon, fPart, fHead, fBody, fLeg}) {
            f.addCaretListener(e -> soi.run());
        }
        soi.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        hang(form, c, y++, "Tên:", fTen);
        hang(form, c, y++, "Loại:", pLoai);
        hang(form, c, y++, "Hệ:", cbHe);
        hang(form, c, y++, "Xếp chồng:", cbChong);
        hang(form, c, y++, "Sức mạnh yêu cầu:", fSm);
        hang(form, c, y++, "Ảnh (icon_id):", fIcon);
        hang(form, c, y++, "Xem ảnh:", lblXem);
        hang(form, c, y++, "Part:", fPart);
        hang(form, c, y++, "Head:", fHead);
        hang(form, c, y++, "Body:", fBody);
        hang(form, c, y++, "Leg:", fLeg);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        form.add(lblPart, c);
        form.add(new JLabel("<html><span style='color:#777'>"
                + "<b>Head / Body / Leg</b> trỏ vào bảng <code>part</code> — bộ ảnh vẽ "
                + "nhân vật khi mặc món này. Áo dùng Body, quần dùng Leg, cải trang dùng "
                + "cả ba.<br><code>-1</code> là không đổi phần đó.<br>"
                + "Máy chủ tự đẩy dữ liệu mới cho client, "
                + "nếu không người chơi vẫn thấy y như cũ."
                + "</span></html>"), c);
        c.gridy = y;
        form.add(new JLabel(" "), c);

        int ok = JOptionPane.showConfirmDialog(this, form,
                "Sửa mẫu vật phẩm — " + t.name + " (id " + t.id + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String ten = fTen.getText().trim();
        if (ten.isEmpty()) {
            note(WARN_RED, "Tên vật phẩm không được để trống.");
            return;
        }
        int icon;
        int part;
        int head;
        int body;
        int leg;
        int sm;
        try {
            icon = Integer.parseInt(fIcon.getText().trim());
            part = Integer.parseInt(fPart.getText().trim());
            head = Integer.parseInt(fHead.getText().trim());
            body = Integer.parseInt(fBody.getText().trim());
            leg = Integer.parseInt(fLeg.getText().trim());
            sm = Integer.parseInt(fSm.getText().trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Ảnh, Part, Head, Body, Leg và Sức mạnh phải là số nguyên.");
            return;
        }
        int loai = maLoaiChon[0];
        int he = Math.max(0, cbHe.getSelectedIndex());
        boolean chong = cbChong.isSelected();
        try {
            nro.repository.ConnectDB.executeUpdate(
                    "UPDATE item_template SET NAME = ?, TYPE = ?, gender = ?,"
                    + " icon_id = ?, part = ?, head = ?, body = ?, leg = ?,"
                    + " power_require = ?, is_up_to_up = ? WHERE id = ?",
                    ten, loai, he, icon, part, head, body, leg, sm,
                    chong ? 1 : 0, (int) t.id);
        } catch (Exception ex) {
            note(WARN_RED, "Không lưu được: " + ex.getMessage());
            return;
        }
        // Sua ca ban trong bo nho: CSDL va bo nho phai khop, neu khong bang van
        // hien so cu cho toi lan khoi dong sau.
        t.name = ten;
        t.type = (byte) loai;
        t.gender = (byte) he;
        t.iconID = (short) icon;
        t.part = (short) part;
        t.head = head;
        t.body = body;
        t.leg = leg;
        t.strRequire = sm;
        t.isUpToUp = chong;
        fillCatalog();
        LamMoi.bao(LamMoi.VAT_PHAM);
        note(OK_GREEN, "Đã sửa mẫu \"" + ten + "\" (id " + t.id
                + ") — nhớ bấm \"Đẩy dữ liệu cho client\".");
    }

    /** Mẫu vật phẩm của dòng đang chọn trong danh mục, hoặc {@code null}. */
    private ItemTemplate mauDangChon() {
        int r = catTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn vật phẩm nào trong danh mục.");
            return null;
        }
        int idx = catTable.convertRowIndexToModel(r);
        ItemTemplate t = PlayerManagerPanel.templateOf(
                String.valueOf(catModel.getValueAt(idx, 1)));
        if (t == null) {
            note(WARN_RED, "Không đọc được vật phẩm đang chọn.");
        }
        return t;
    }

    /** Số trong ô, hoặc {@code macDinh} nếu ô đang gõ dở. */
    private static int soDuong(JTextField f, int macDinh) {
        try {
            return Integer.parseInt(f.getText().trim());
        } catch (NumberFormatException ex) {
            return macDinh;
        }
    }

    /**
     * Tập id có thật trong bảng {@code part}.
     *
     * <p>Đọc <b>một lần</b> rồi giữ lại: {@link #coPart} chạy mỗi lần gõ một ký
     * tự trong hộp thoại, truy vấn CSDL ở đó là giật cả giao diện. Máy chủ không
     * giữ bảng này trong bộ nhớ — nó đọc rồi ghi thẳng ra tệp
     * {@code data/update_data/part} cho client, nên phải tự đọc lấy.</p>
     *
     * <p>{@code null} nghĩa là chưa đọc được; khi đó {@link #coPart} trả
     * {@code true} cho mọi id — thà không cảnh báo còn hơn cảnh báo sai.</p>
     */
    private static java.util.Set<Integer> partCoThat;

    private static boolean coPart(int id) {
        if (id < 0) {
            return true;
        }
        if (partCoThat == null) {
            java.util.Set<Integer> tam = new java.util.HashSet<>();
            nro.repository.CrisResultSet rs = null;
            try {
                rs = nro.repository.ConnectDB.executeQuery("SELECT id FROM part");
                while (rs.next()) {
                    tam.add(rs.getInt("id"));
                }
                partCoThat = tam;
            } catch (Exception ex) {
                return true;
            } finally {
                if (rs != null) {
                    try {
                        rs.dispose();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return partCoThat.contains(id);
    }

    /** Một hàng nhãn + ô trong form. */
    private static void hang(JPanel form, GridBagConstraints c, int y,
                             String nhan, java.awt.Component o) {
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 0;
        form.add(new JLabel(nhan), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(o, c);
    }

    private void suaMoTa() {
        int r = catTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn vật phẩm nào trong danh mục.");
            return;
        }
        int idx = catTable.convertRowIndexToModel(r);
        int id = -1;
        try {
            id = Integer.parseInt(String.valueOf(catModel.getValueAt(idx, 1)).trim());
        } catch (NumberFormatException ignored) {
            // rơi xuống kiểm tra bên dưới
        }
        ItemTemplate t = PlayerManagerPanel.templateOf(String.valueOf(id));
        if (t == null) {
            note(WARN_RED, "Không đọc được vật phẩm đang chọn.");
            return;
        }

        javax.swing.JTextArea ta = new javax.swing.JTextArea(
                t.description == null ? "" : t.description, 6, 44);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.add(new JLabel("Mô tả của \"" + t.name + "\" (id " + t.id + "):"),
                BorderLayout.NORTH);
        p.add(ServerGuiUtils.cuon(ta), BorderLayout.CENTER);
        p.add(new JLabel("<html><span style='color:#777'>Sửa xong nhớ bấm "
                + "tự đẩy dữ liệu mới cho client, nhưng người chơi "
                + "vẫn thấy mô tả cũ.</span></html>"), BorderLayout.SOUTH);

        int ok = JOptionPane.showConfirmDialog(this, p, "Sửa mô tả vật phẩm",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String moi = ta.getText();
        try {
            nro.repository.ConnectDB.executeUpdate(
                    "UPDATE item_template SET description = ? WHERE id = ?", moi, (int) t.id);
        } catch (Exception ex) {
            note(WARN_RED, "Không lưu được mô tả: " + ex.getMessage());
            return;
        }
        // Sua ca ban trong bo nho de bang hien ngay, khong phai khoi dong lai
        // moi thay -- CSDL va bo nho phai khop nhau.
        t.description = moi;
        fillCatalog();
        LamMoi.bao(LamMoi.VAT_PHAM);
        note(OK_GREEN, "Đã sửa mô tả của " + t.name
                + " — nhớ bấm \"Đẩy dữ liệu cho client\".");
    }

    private void note(Color color, String text) {
        lblStatus.setForeground(color);
        lblStatus.setText(text);
        // Ghi ra nhat ky may chu, khong chi de tren mot cai nhan trong panel.
        //
        // Cai nhan nam duoi cung cua the, doi mot dong chu — nguoi dung bam
        // "Them vao hanh trang" roi nhin vao hanh trang trong game, khong nhin
        // xuong day panel, nen ho chi thay "khong them duoc" ma khong biet vi
        // sao. Co dong log thi lan sau doc log la ra ngay ly do.
        if (color == WARN_RED) {
            nro.core.log.Logger.warning("[PANEL VAT PHAM] " + text);
        } else {
            nro.core.log.Logger.info("PANEL", text);
        }
    }

    private static javax.swing.border.Border titled(String t) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215)), t);
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

    /**
     * Đọc danh sách set kích hoạt từ panel vào ô chọn.
     *
     * <p>Đọc lúc dựng giao diện <b>và</b> mỗi lần bấm "Làm mới", vì admin hay
     * tạo set xong quay sang phát đồ ngay.</p>
     */
    private void napDanhSachSet() {
        String dangChon = cbSet.getSelectedIndex() > 0
                ? String.valueOf(cbSet.getSelectedItem()) : null;
        cbSet.removeAllItems();
        maSet.clear();
        cbSet.addItem("— không gắn set —");
        maSet.add(null);
        // Hanh tinh truoc, roi ten set — cung thu tu voi danh sach ben tab Cau
        // Hinh He Thong, de hai cho khong bao gio hien khac nhau.
        List<nro.repository.dao.SetBonusDAO.DinhNghia> ds = new ArrayList<>();
        for (nro.repository.dao.SetBonusDAO.DinhNghia d
                : nro.repository.dao.SetBonusDAO.dinhNghia().values()) {
            if (d.active) {
                ds.add(d);
            }
        }
        final java.text.Collator vi =
                java.text.Collator.getInstance(new java.util.Locale("vi", "VN"));
        vi.setStrength(java.text.Collator.PRIMARY);
        ds.sort((x, y) -> {
            int r = vi.compare(String.valueOf(x.hanhTinh), String.valueOf(y.hanhTinh));
            if (r != 0) {
                return r;
            }
            r = vi.compare(String.valueOf(x.ten), String.valueOf(y.ten));
            return r != 0 ? r : String.valueOf(x.setKey).compareTo(
                    String.valueOf(y.setKey));
        });
        for (nro.repository.dao.SetBonusDAO.DinhNghia d : ds) {
            String ht = d.hanhTinh == null || d.hanhTinh.trim().isEmpty()
                    ? nro.repository.dao.SetBonusDAO.KHAC : d.hanhTinh.trim();
            cbSet.addItem(d.ten + "  —  " + ht);
            maSet.add(d.setKey);
        }
        if (dangChon != null) {
            cbSet.setSelectedItem(dangChon);
        }
    }

    /**
     * Các chỉ số của set đang chọn: chỉ số nhận diện cộng các dòng chữ mô tả.
     *
     * <p>Trả mảng rỗng nếu không chọn set, hoặc món này không phải trang bị.</p>
     */
    private int[] chiSoSetDangChon(nro.entity.template.ItemTemplate t) {
        int i = cbSet.getSelectedIndex();
        if (i <= 0 || i >= maSet.size() || maSet.get(i) == null) {
            return new int[0];
        }
        if (t == null || t.type < 0 || t.type > 4) {
            return new int[0];        // chi ao/quan/gang/giay/rada moi co set
        }
        nro.repository.dao.SetBonusDAO.DinhNghia d =
                nro.repository.dao.SetBonusDAO.dinhNghia().get(maSet.get(i));
        if (d == null || d.optionIds == null) {
            return new int[0];
        }
        java.util.List<Integer> ra = new java.util.ArrayList<>();
        for (String p : d.optionIds.split(",")) {
            try {
                ra.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException ignored) {
                // dong cau hinh hong -> bo qua chi so do
            }
        }
        ra.addAll(nro.repository.dao.SetBonusDAO.docMoTaDong(d.moTaDong).values());
        int[] m = new int[ra.size()];
        for (int j = 0; j < m.length; j++) {
            m[j] = ra.get(j);
        }
        return m;
    }

    /** {@code true} nếu là một trong năm ô trang bị: áo, quần, găng, giày, rada. */
    private static boolean laTrangBi(nro.entity.template.ItemTemplate t) {
        if (t == null) {
            return false;
        }
        // 0-4: ao, quan, gang, giay, nhan.
        if (t.type >= 0 && t.type <= 4) {
            return true;
        }
        // 32: giap tap luyen. Trong game EpSaoTrangBi KHONG loc theo type — no
        // chi doi mon do co o sao (chi so 107). Nen giap tap luyen gan sao duoc
        // that, ma cho nay lai chan, bao "khong phai trang bi".
        return java.util.Arrays.binarySearch(TYPE_CO_O_SAO, t.type) >= 0;
    }

    /**
     * Các {@code type} vật phẩm gắn sao pha lê được ngoài trang bị 0–4.
     *
     * <p>Phải sắp xếp tăng dần: {@code binarySearch} ở trên trông vào đó. Thêm
     * type mới thì chèn đúng chỗ, đừng nối vào cuối.</p>
     */
    private static final int[] TYPE_CO_O_SAO = {32};


    /**
     * Chỉ số mà một viên sao cộng vào trang bị, hoặc {@code null} nếu không phải sao.
     *
     * <p>Dùng chính {@code Item.getOptionDaPhaLe()} thay vì chép lại bảng tra:
     * chép lại là hai bảng, sửa một bên thì bên kia lệch mà không ai biết.</p>
     */
    private static ItemOption chiSoCuaSao(int itemId) {
        try {
            ItemTemplate t = PlayerManagerPanel.templateOf(String.valueOf(itemId));
            if (t == null) {
                return null;
            }
            Item it = ItemService.gI().createNewItem((short) itemId, 1);
            return it == null ? null : it.getOptionDaPhaLe();
        } catch (Exception ex) {
            return null;
        }
    }

    /** Tổng số sao đang chọn. */
    private int tongSaoGan() {
        int n = 0;
        for (int v : saoGan.values()) {
            n += v;
        }
        return n;
    }

    /** Viết lại ô tóm tắt: "2 × Sao pha lê đỏ, 1 × Sao pha lê lam". */
    private void veTomTatSao() {
        if (saoGan.isEmpty()) {
            fSaoGan.setText("(không gắn sao)");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : saoGan.entrySet()) {
            ItemTemplate t = PlayerManagerPanel.templateOf(String.valueOf(e.getKey()));
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getValue()).append(" × ")
                    .append(t != null ? t.name : "id " + e.getKey());
        }
        fSaoGan.setText(tongSaoGan() + " sao:  " + sb);
    }

    /**
     * Hộp chọn <b>nhiều loại</b> sao pha lê cùng lúc.
     *
     * <p>Mỗi dòng là một loại sao kèm ô số lượng; gắn bao nhiêu loại cũng được,
     * miễn <b>tổng</b> không vượt số ô sao. Người chơi ngoài game cũng nạp từng
     * viên khác màu vào từng ô như vậy.</p>
     *
     * <p>Cột "Cộng gì" đọc thẳng từ bảng tra của game để khỏi phải đoán viên nào
     * cho chỉ số nào.</p>
     */
    private void chonSaoDialog() {
        java.util.List<ItemTemplate> ds = new ArrayList<>();
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t != null && t.name != null && chiSoCuaSao(t.id) != null) {
                ds.add(t);
            }
        }
        if (ds.isEmpty()) {
            note(WARN_RED, "Chưa nạp được bảng mẫu vật phẩm — thử lại sau vài giây.");
            return;
        }

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Số lượng", "Viên sao", "Cộng gì", "__id"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }
        };
        for (ItemTemplate t : ds) {
            ItemOption o = chiSoCuaSao(t.id);
            m.addRow(new Object[]{
                String.valueOf(saoGan.getOrDefault((int) t.id, 0)), t.name,
                "+" + o.param + " " + OptionPicker.tenChiSo(o.optionTemplate.id),
                (int) t.id});
        }
        JTable bang = new JTable(m);
        bang.setRowHeight(24);
        // Chot o dang sua khi mat tieu diem. Khong dat co nay thi go so vao o
        // roi bam OK ngay la con so VAN nam trong bo soan thao, chua vao model —
        // doc ra 0, tong van la "0 / 8 o", va khong gan duoc vien sao nao.
        bang.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        int[] w = {70, 220, 260};
        for (int i = 0; i < w.length && i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        bang.removeColumn(bang.getColumnModel().getColumn(3));

        int soO;
        try {
            soO = Integer.parseInt(fSoOSao.getText().trim());
        } catch (NumberFormatException ex) {
            soO = 0;
        }
        final int oToiDa = soO;
        JLabel lblTong = new JLabel();
        Runnable demLai = () -> {
            int t = 0;
            for (int r = 0; r < m.getRowCount(); r++) {
                t += soAnToan(m.getValueAt(r, 0));
            }
            lblTong.setText("  Tổng: " + t + " / " + oToiDa + " ô");
            lblTong.setForeground(t > oToiDa ? WARN_RED : OK_GREEN);
        };
        demLai.run();
        m.addTableModelListener(e -> demLai.run());

        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        JLabel note = new JLabel("<html><span style='color:#777'>"
                + "Điền số lượng cho từng loại. Gắn bao nhiêu loại cũng được, miễn "
                + "<b>tổng</b> không vượt số ô sao.</span></html>");
        note.setBorder(new EmptyBorder(8, 8, 4, 8));
        wrap.add(note, BorderLayout.NORTH);
        wrap.add(ServerGuiUtils.cuon(bang), BorderLayout.CENTER);
        wrap.add(lblTong, BorderLayout.SOUTH);
        wrap.setPreferredSize(new Dimension(620, 420));

        if (JOptionPane.showConfirmDialog(this, wrap, "Chọn sao pha lê",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return;
        }
        // Chot lai lan nua cho chac: co terminateEditOnFocusLost o tren chi an
        // khi tieu diem chuyen di, ma bam Enter roi bam OK bang chuot thi tieu
        // diem khong roi khoi o.
        if (bang.isEditing()) {
            bang.getCellEditor().stopCellEditing();
        }
        java.util.Map<Integer, Integer> moi = new java.util.LinkedHashMap<>();
        int tong = 0;
        for (int r = 0; r < m.getRowCount(); r++) {
            int n = soAnToan(m.getValueAt(r, 0));
            if (n <= 0) {
                continue;
            }
            moi.put(soAnToan(m.getValueAt(r, 3)), n);
            tong += n;
        }
        if (tong > oToiDa) {
            note(WARN_RED, "Chọn " + tong + " sao mà chỉ có " + oToiDa
                    + " ô — sửa số ô sao trước, hoặc bớt sao đi.");
            return;
        }
        saoGan.clear();
        saoGan.putAll(moi);
        veTomTatSao();
        note(GREY, tong == 0 ? "Đã bỏ hết sao gắn sẵn."
                : "Đã chọn " + tong + " sao cho " + moi.size() + " loại.");
    }

    /** Số trong ô, hoặc {@code 0} nếu ô rỗng / không phải số. */
    private static int soAnToan(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // =====================================================================
    //  Loại vật phẩm
    // =====================================================================

    private final DefaultTableModel loaiModel = new DefaultTableModel(
            new Object[]{"Mã", "Tên loại", "Số vật phẩm"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable loaiTable = new JTable(loaiModel);

    /**
     * Màn sửa tên các loại vật phẩm.
     *
     * <p>Có một điều phải nói thẳng ngay trên màn hình, nên chỗ này in hẳn ra
     * cho người dùng đọc: <b>thêm một loại mới ở đây không dạy cho game biết
     * làm gì với loại đó.</b> Con số loại đã bị gán ý nghĩa cứng ở cả máy chủ
     * lẫn client. Bảng này chỉ đổi cái nhãn, để tìm và gom nhóm cho dễ.</p>
     */
    private JComponent buildLoaiTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel canBiet = new JLabel("<html><b>Đây chỉ là cái nhãn.</b> "
                + "Thêm một loại mới không dạy cho game biết làm gì với loại "
                + "đó — số loại đã gán ý nghĩa cứng trong mã (0–4 là năm ô "
                + "trang bị, 9/10/34 là Vàng/Ngọc/Hồng ngọc, 13 là Bùa...). "
                + "Vật phẩm mang loại lạ sẽ nằm trong hành trang như một món "
                + "thường. Bảng này để gọi tên và gom nhóm cho dễ tìm.</html>");
        canBiet.setForeground(GREY);
        canBiet.setBorder(new EmptyBorder(0, 2, 6, 2));
        root.add(canBiet, BorderLayout.NORTH);

        loaiTable.setRowHeight(24);
        loaiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loaiTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        loaiTable.setAutoCreateRowSorter(true);
        int[] w = {60, 280, 100};
        for (int i = 0; i < loaiTable.getColumnCount() && i < w.length; i++) {
            loaiTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        root.add(ServerGuiUtils.cuon(loaiTable), BorderLayout.CENTER);

        JPanel nut = new JPanel(new GridLayout(0, 4, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm loại", OK_GREEN, e -> loaiDialog(true)));
        nut.add(button("Sửa tên", ACCENT, e -> loaiDialog(false)));
        nut.add(button("Xoá loại", WARN_RED, e -> loaiXoa()));
        nut.add(button("Tải lại", GREY, e -> napLoai()));
        root.add(nut, BorderLayout.SOUTH);

        napLoai();
        return root;
    }

    private void napLoai() {
        loaiModel.setRowCount(0);
        nro.repository.dao.LoaiVatPhamDAO.napLai();
        Map<Integer, Integer> dem = demTheoLoai();
        for (Map.Entry<Integer, String> e
                : nro.repository.dao.LoaiVatPhamDAO.tatCa().entrySet()) {
            loaiModel.addRow(new Object[]{e.getKey(), e.getValue(),
                PlayerManagerPanel.fmt(
                        dem.getOrDefault(e.getKey(), 0))});
        }
    }

    /**
     * Đếm vật phẩm theo loại, một lượt cho cả bảng.
     *
     * <p>Đếm bằng mẫu đã nạp sẵn trong bộ nhớ chứ không bắn mỗi loại một câu
     * truy vấn: bảng có gần năm chục loại, hỏi từng cái là năm chục lượt đi về
     * CSDL chỉ để vẽ xong một màn hình.</p>
     */
    private Map<Integer, Integer> demTheoLoai() {
        Map<Integer, Integer> ra = new java.util.HashMap<>();
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t != null) {
                ra.merge((int) t.type, 1, Integer::sum);
            }
        }
        return ra;
    }

    private int loaiDangChon() {
        int r = loaiTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một loại trong bảng trước.");
            return -1;
        }
        return Integer.parseInt(String.valueOf(
                loaiTable.getValueAt(r, 0)));
    }

    private void loaiDialog(boolean them) {
        int maCu = -1;
        String tenCu = "";
        if (!them) {
            maCu = loaiDangChon();
            if (maCu < 0) {
                return;
            }
            tenCu = nro.repository.dao.LoaiVatPhamDAO.ten(maCu);
        }

        JTextField fMa = new JTextField(them ? "" : String.valueOf(maCu), 8);
        JTextField fTen = new JTextField(tenCu == null ? "" : tenCu, 24);
        fMa.setEditable(them);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Mã loại:"), c);
        c.gridx = 1;
        JPanel hangMa = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hangMa.setOpaque(false);
        hangMa.add(fMa);
        JLabel gioiHan = new JLabel("0 tới "
                + nro.repository.dao.LoaiVatPhamDAO.MA_TOI_DA);
        gioiHan.setForeground(GREY);
        hangMa.add(gioiHan);
        form.add(hangMa, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Tên loại:"), c);
        c.gridx = 1;
        form.add(fTen, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm loại vật phẩm" : "Sửa tên loại " + maCu,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        int ma;
        try {
            ma = Integer.parseInt(fMa.getText().trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Mã loại phải là số nguyên.");
            return;
        }
        String loi = nro.repository.dao.LoaiVatPhamDAO.luu(ma, fTen.getText(), them);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napLoai();
        buildTypeCombo();
        LamMoi.bao(LamMoi.LOAI);
        note(OK_GREEN, "Đã lưu loại " + ma + ".");
    }

    private void loaiXoa() {
        int ma = loaiDangChon();
        if (ma < 0) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá tên của loại " + ma + "?\nVật phẩm không bị xoá, chỉ mất tên.",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.LoaiVatPhamDAO.xoa(ma);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napLoai();
        buildTypeCombo();
        LamMoi.bao(LamMoi.LOAI);
        note(OK_GREEN, "Đã xoá loại " + ma + ".");
    }

    // =====================================================================
    //  Quản lý ảnh (icon)
    // =====================================================================

    private final javax.swing.DefaultListModel<Integer> anhModel
            = new javax.swing.DefaultListModel<>();
    private final javax.swing.JList<Integer> anhList
            = new javax.swing.JList<>(anhModel);
    private final JComboBox<String> cbAnhLoc = new JComboBox<>(new String[]{
        "Ảnh đang có", "Ô còn trống", "Ảnh không vật phẩm nào dùng"});
    private final JTextField fAnhNhay = new JTextField(8);
    private final JLabel lblAnhTom = new JLabel();
    private final JLabel lblAnhDung = new JLabel();

    /**
     * Màn xem và thêm ảnh vật phẩm.
     *
     * <p>Hơn hai vạn ảnh nên không nạp trước cái nào: {@link javax.swing.JList}
     * chỉ vẽ ô đang nhìn thấy, và {@code iconOf} có bộ nhớ đệm, nên kéo tới đâu
     * đọc đĩa tới đó.</p>
     *
     * <p>Việc thêm ảnh gọi thẳng {@link nro.tool.ThemIcon}, tức đúng đoạn mã mà
     * dòng lệnh chạy — không chép lại, để hai đường không lệch nhau về sau.</p>
     */
    private JComponent buildAnhTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 4, 4, 4));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(new JLabel("Xem:"));
        cbAnhLoc.setPreferredSize(new Dimension(230, 26));
        top.add(cbAnhLoc);
        top.add(new JLabel("   Nhảy tới id:"));
        top.add(fAnhNhay);
        lblAnhTom.setForeground(GREY);
        top.add(lblAnhTom);
        root.add(top, BorderLayout.NORTH);

        anhList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        anhList.setVisibleRowCount(-1);
        anhList.setFixedCellWidth(76);
        anhList.setFixedCellHeight(62);
        anhList.setCellRenderer(new javax.swing.ListCellRenderer<Integer>() {
            // Dung lai MOT o thay vi tao JLabel moi moi lan ve. Luoi nay ve lai
            // lien tuc luc keo, tao moi la sinh rac lien mien.
            private final JLabel o = new JLabel("", JLabel.CENTER);

            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<? extends Integer> list, Integer value,
                    int index, boolean sel, boolean focus) {
                o.setText(String.valueOf(value));
                o.setIcon(PlayerManagerPanel.iconOf(value));
                o.setHorizontalTextPosition(JLabel.CENTER);
                o.setVerticalTextPosition(JLabel.BOTTOM);
                o.setOpaque(true);
                o.setBackground(sel ? new Color(0xCCE4FF) : Color.WHITE);
                o.setBorder(new EmptyBorder(4, 2, 4, 2));
                return o;
            }
        });
        anhList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                veAnhDangChon();
            }
        });
        root.add(ServerGuiUtils.cuon(anhList), BorderLayout.CENTER);

        JPanel duoi = new JPanel(new BorderLayout(0, 4));
        duoi.setOpaque(false);
        lblAnhDung.setForeground(GREY);
        lblAnhDung.setBorder(new EmptyBorder(2, 2, 4, 2));
        duoi.add(lblAnhDung, BorderLayout.NORTH);

        JPanel nut = new JPanel(new GridLayout(0, 4, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm ảnh mới…", OK_GREEN, e -> themAnh(-1)));
        nut.add(button("Thay ảnh đang chọn…", ACCENT, e -> themAnh(anhDangChon())));
        nut.add(button("Xoá ảnh đang chọn", WARN_RED, e -> xoaAnh()));
        nut.add(button("Tải lại", GREY, e -> napAnh()));
        duoi.add(nut, BorderLayout.SOUTH);
        root.add(duoi, BorderLayout.SOUTH);

        cbAnhLoc.addActionListener(e -> napAnh());
        fAnhNhay.addActionListener(e -> nhayToiAnh());
        napAnh();
        return root;
    }

    private int anhDangChon() {
        Integer v = anhList.getSelectedValue();
        return v == null ? -1 : v;
    }

    /** Nạp danh sách id theo bộ lọc đang chọn. */
    private void napAnh() {
        int giu = anhDangChon();
        int loc = Math.max(0, cbAnhLoc.getSelectedIndex());
        java.util.Set<Integer> dangDung = idAnhDangDung();

        anhModel.clear();
        List<Integer> ra = new ArrayList<>();
        // Liet ke thu muc MOT lan. Hoi File.isFile() cho tung id la hon ba van
        // luot cham dia moi lan doi bo loc, du de panel dung hinh.
        java.util.Set<Integer> daCo = nro.tool.ThemIcon.idDaCoAnh();
        for (int id = 0; id <= nro.tool.ThemIcon.idToiDa(); id++) {
            boolean trong = !daCo.contains(id);
            boolean nhan;
            if (loc == 1) {
                nhan = trong;
            } else if (loc == 2) {
                nhan = !trong && !dangDung.contains(id);
            } else {
                nhan = !trong;
            }
            if (nhan) {
                ra.add(id);
            }
        }
        for (int id : ra) {
            anhModel.addElement(id);
        }
        lblAnhTom.setText("   " + PlayerManagerPanel.fmt(ra.size())
                + (loc == 1 ? " ô còn trống" : " ảnh"));
        if (giu >= 0) {
            anhList.setSelectedValue(giu, true);
        }
        veAnhDangChon();
    }

    /** Id ảnh đang được ít nhất một vật phẩm dùng. */
    private java.util.Set<Integer> idAnhDangDung() {
        java.util.Set<Integer> ra = new java.util.HashSet<>();
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t != null) {
                ra.add((int) t.iconID);
            }
        }
        return ra;
    }

    /** Kể tên vài vật phẩm đang dùng ảnh đang chọn. */
    private void veAnhDangChon() {
        int id = anhDangChon();
        if (id < 0) {
            lblAnhDung.setText("Chưa chọn ảnh nào.");
            return;
        }
        List<String> ten = new ArrayList<>();
        int dem = 0;
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t != null && t.iconID == id) {
                dem++;
                if (ten.size() < 5) {
                    ten.add(t.name + " (" + t.id + ")");
                }
            }
        }
        if (dem == 0) {
            lblAnhDung.setText("Ảnh " + id + " — chưa vật phẩm nào dùng.");
            return;
        }
        lblAnhDung.setText("Ảnh " + id + " — " + dem + " vật phẩm dùng: "
                + String.join(", ", ten) + (dem > ten.size() ? "…" : ""));
    }

    private void nhayToiAnh() {
        try {
            int id = Integer.parseInt(fAnhNhay.getText().trim());
            int i = anhModel.indexOf(id);
            if (i < 0) {
                note(WARN_RED, "Id " + id + " không nằm trong danh sách đang xem.");
                return;
            }
            anhList.setSelectedIndex(i);
            anhList.ensureIndexIsVisible(i);
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Gõ một số id.");
        }
    }

    /**
     * Chọn tệp ảnh rồi chuyển thành icon.
     *
     * @param id id muốn ghi đè, hoặc {@code -1} để lấy ô trống đầu tiên
     */
    private void themAnh(int id) {
        boolean deLen = id >= 0;
        if (!deLen) {
            id = nro.tool.ThemIcon.idTrongDauTien();
            if (id < 0) {
                JOptionPane.showMessageDialog(this,
                        "Hết ô trống — không còn id nào chưa dùng.",
                        "Không thêm được", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        javax.swing.JFileChooser chon = new javax.swing.JFileChooser();
        chon.setDialogTitle("Chọn ảnh cho icon " + id);
        chon.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Ảnh (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (chon.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }

        JTextField fId = new JTextField(String.valueOf(id), 8);
        JTextField fRong = new JTextField(
                String.valueOf(nro.tool.ThemIcon.canhMacDinh()), 5);
        JTextField fCao = new JTextField(
                String.valueOf(nro.tool.ThemIcon.caoMacDinh()), 5);
        javax.swing.JCheckBox cbCat = new javax.swing.JCheckBox(
                "Cắt viền trong suốt và xoá nền một màu", true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Ghi vào id:"), c);
        c.gridx = 1;
        form.add(fId, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Khung ở mức x1:"), c);
        c.gridx = 1;
        JPanel pKhung = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pKhung.setOpaque(false);
        pKhung.add(fRong);
        pKhung.add(new JLabel(" x "));
        pKhung.add(fCao);
        pKhung.add(new JLabel("  ô hành trang là 34 x 23 — để 30 x 21 là lấp gần đầy"));
        form.add(pKhung, c);
        c.gridx = 1;
        c.gridy = 2;
        form.add(cbCat, c);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        form.add(new JLabel("<html><i>Ảnh sẽ được sinh ở cả bốn mức phóng"
                + " x1 tới x4 và ghi vào Server/data/icon.</i></html>"), c);

        if (JOptionPane.showConfirmDialog(this, form,
                deLen ? "Thay ảnh " + id : "Thêm ảnh mới",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        int idGhi;
        int canh;
        int cao;
        try {
            idGhi = Integer.parseInt(fId.getText().trim());
            canh = Integer.parseInt(fRong.getText().trim());
            cao = Integer.parseInt(fCao.getText().trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Id và khung phải là số nguyên.");
            return;
        }
        if (!nro.tool.ThemIcon.conTrong(idGhi)
                && JOptionPane.showConfirmDialog(this,
                        "Id " + idGhi + " đang có ảnh.\nGhi đè sẽ đổi icon của"
                        + " MỌI vật phẩm đang dùng id này. Tiếp tục?",
                        "Ghi đè ảnh", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }

        List<String> nhatKy = new ArrayList<>();
        String loi = nro.tool.ThemIcon.chuyen(chon.getSelectedFile(), idGhi,
                canh, cao, cbCat.isSelected(), nhatKy);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không thêm được ảnh",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Anh cu cua id nay con nam trong bo nho dem cua iconOf; khong don thi
        // panel van ve anh cu du tep tren dia da thay.
        PlayerManagerPanel.quenAnh(idGhi);
        LamMoi.bao(LamMoi.ANH, LamMoi.VAT_PHAM);
        anhList.setSelectedValue(idGhi, true);

        JOptionPane.showMessageDialog(this,
                "Xong, icon_id = " + idGhi + "."
                + "\n\n" + String.join("\n", nhatKy)
                + "\n\nNgười chơi đang đăng nhập phải thoát ra vào lại thì"
                + "\nclient mới nhận bảng phiên bản ảnh mới.",
                "Đã thêm ảnh", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xoaAnh() {
        int id = anhDangChon();
        if (id < 0) {
            note(WARN_RED, "Chọn một ảnh trước.");
            return;
        }
        int dem = 0;
        for (ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t != null && t.iconID == id) {
                dem++;
            }
        }
        if (dem > 0) {
            JOptionPane.showMessageDialog(this,
                    "Còn " + dem + " vật phẩm đang dùng ảnh này.\n"
                    + "Xoá đi thì chúng hiện ra ô trống. Đổi ảnh cho chúng"
                    + " trước rồi hãy xoá.",
                    "Không xoá được", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá ảnh " + id + " ở cả bốn mức phóng?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.tool.ThemIcon.xoa(id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        PlayerManagerPanel.quenAnh(id);
        napAnh();
        LamMoi.bao(LamMoi.ANH, LamMoi.VAT_PHAM);
        note(OK_GREEN, "Đã xoá ảnh " + id + ".");
    }

    /**
     * Hộp chọn loại vật phẩm: gõ để lọc, và thêm sửa xoá ngay tại chỗ.
     *
     * <p>Trước đây chỗ này là một {@code JComboBox} thả xuống gần năm chục
     * dòng, muốn đặt tên cho một loại thì phải thoát ra tab khác. Gộp cả hai
     * việc vào một hộp thì lúc đang thêm vật phẩm mà thấy loại chưa có tên,
     * đặt tên được luôn.</p>
     *
     * @param maHienTai loại đang chọn, dùng để bôi sẵn dòng đó
     * @return mã loại đã chọn, hoặc {@code -1} nếu huỷ
     */
    private int chonLoai(java.awt.Component cha, int maHienTai) {
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Mã", "Tên loại", "Số vật phẩm"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {50, 260, 90};
        for (int i = 0; i < t.getColumnCount() && i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JTextField fTim = new JTextField(18);
        final int[] ketQua = {-1};

        // Nap lai bang theo chu dang go. Giu lai ma dang chon de bam Sua hay
        // Xoa xong van dung o dong cu.
        final Runnable nap = () -> {
            int giu = t.getSelectedRow() < 0 ? maHienTai
                    : Integer.parseInt(String.valueOf(t.getValueAt(
                            t.getSelectedRow(), 0)));
            String tim = fTim.getText().trim().toLowerCase();
            Map<Integer, Integer> dem = demTheoLoai();
            m.setRowCount(0);
            int chon = -1;
            for (Map.Entry<Integer, String> e
                    : nro.repository.dao.LoaiVatPhamDAO.tatCa().entrySet()) {
                String nhan = e.getKey() + " " + e.getValue();
                if (!tim.isEmpty() && !nhan.toLowerCase().contains(tim)) {
                    continue;
                }
                if (e.getKey() == giu) {
                    chon = m.getRowCount();
                }
                m.addRow(new Object[]{e.getKey(), e.getValue(),
                    PlayerManagerPanel.fmt(dem.getOrDefault(e.getKey(), 0))});
            }
            if (chon >= 0) {
                t.setRowSelectionInterval(chon, chon);
                t.scrollRectToVisible(t.getCellRect(chon, 0, true));
            }
        };
        fTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                nap.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                nap.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                nap.run();
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(new JLabel("Lọc theo mã hoặc tên:"));
        top.add(fTim);

        JPanel nut = new JPanel(new GridLayout(0, 3, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm loại", OK_GREEN, e -> {
            if (suaLoaiTaiCho(t, true, -1)) {
                nap.run();
            }
        }));
        nut.add(button("Sửa tên", ACCENT, e -> {
            int ma = maTrongBang(t);
            if (ma >= 0 && suaLoaiTaiCho(t, false, ma)) {
                nap.run();
            }
        }));
        nut.add(button("Xoá loại", WARN_RED, e -> {
            int ma = maTrongBang(t);
            if (ma < 0) {
                return;
            }
            String loi = nro.repository.dao.LoaiVatPhamDAO.xoa(ma);
            if (loi != null) {
                JOptionPane.showMessageDialog(t, loi, "Không xoá được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            nap.run();
        }));

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(520, 420));
        root.add(top, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);

        JPanel duoi = new JPanel(new BorderLayout(0, 4));
        duoi.setOpaque(false);
        JLabel canBiet = new JLabel("<html><i>Số loại đã gán ý nghĩa cứng trong "
                + "mã. Loại mới chỉ là cái nhãn để gom nhóm, game không xử lý "
                + "gì thêm.</i></html>");
        canBiet.setForeground(GREY);
        duoi.add(canBiet, BorderLayout.NORTH);
        duoi.add(nut, BorderLayout.SOUTH);
        root.add(duoi, BorderLayout.SOUTH);

        nap.run();
        // Bam doi vao mot dong la chon luon, khoi phai voi xuong nut OK.
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ketQua[0] = maTrongBang(t);
                    java.awt.Window w = SwingUtilities.getWindowAncestor(t);
                    if (w != null) {
                        w.setVisible(false);
                    }
                }
            }
        });

        int chon = JOptionPane.showConfirmDialog(cha, root, "Chọn loại vật phẩm",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ketQua[0] >= 0) {
            return ketQua[0];
        }
        return chon == JOptionPane.OK_OPTION ? maTrongBang(t) : -1;
    }

    /** Mã loại ở dòng đang chọn, hoặc {@code -1} nếu chưa chọn dòng nào. */
    private int maTrongBang(JTable t) {
        int r = t.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(t, "Chọn một loại trong bảng trước.",
                    "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return Integer.parseInt(String.valueOf(t.getValueAt(r, 0)));
    }

    /**
     * Hộp nhỏ thêm hoặc sửa một loại, dùng trong {@link #chonLoai}.
     *
     * @return {@code true} nếu đã lưu
     */
    private boolean suaLoaiTaiCho(java.awt.Component cha, boolean them, int ma) {
        JTextField fMa = new JTextField(them ? "" : String.valueOf(ma), 8);
        JTextField fTen = new JTextField(them ? ""
                : String.valueOf(nro.repository.dao.LoaiVatPhamDAO.ten(ma)), 24);
        fMa.setEditable(them);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Mã loại:"), c);
        c.gridx = 1;
        JPanel hangMa = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hangMa.setOpaque(false);
        hangMa.add(fMa);
        JLabel gioiHan = new JLabel("0 tới "
                + nro.repository.dao.LoaiVatPhamDAO.MA_TOI_DA);
        gioiHan.setForeground(GREY);
        hangMa.add(gioiHan);
        form.add(hangMa, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Tên loại:"), c);
        c.gridx = 1;
        form.add(fTen, c);

        if (JOptionPane.showConfirmDialog(cha, form,
                them ? "Thêm loại vật phẩm" : "Sửa tên loại " + ma,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return false;
        }
        int maMoi;
        try {
            maMoi = Integer.parseInt(fMa.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(cha, "Mã loại phải là số nguyên.",
                    "Nhập chưa đúng", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String loi = nro.repository.dao.LoaiVatPhamDAO.luu(maMoi, fTen.getText(), them);
        if (loi != null) {
            JOptionPane.showMessageDialog(cha, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        buildTypeCombo();
        return true;
    }

    /** Ô chọn loại dạng một dòng chữ kèm nút, dùng trong hộp thoại vật phẩm. */
    private JPanel oChonLoai(int[] ma, JLabel nhan) {
        nhan.setText(PlayerManagerPanel.typeName(ma[0]));
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(nhan, BorderLayout.CENTER);
        p.add(button("Chọn…", ACCENT, e -> {
            int moi = chonLoai(p, ma[0]);
            if (moi >= 0) {
                ma[0] = moi;
                nhan.setText(PlayerManagerPanel.typeName(moi));
            }
        }), BorderLayout.EAST);
        return p;
    }
}
