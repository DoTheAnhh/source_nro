package nro.ui;

import nro.core.log.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Detu;
import nro.entity.player.NPoint;
import nro.entity.player.Player;
import nro.entity.template.ItemOptionTemplate;
import nro.entity.template.ItemTemplate;
import nro.repository.dao.AccountDAO;
import nro.repository.dao.ConfigDAO;
import nro.repository.dao.LichSuVatPhamDAO;
import nro.repository.dao.PlayerDAO;
import nro.server.Client;
import nro.server.Manager;
import nro.server.ServerManager;
import nro.service.Service;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Bảng quản trị người chơi — làm việc <b>trực tiếp trên đối tượng đang sống
 * trong bộ nhớ</b>, không qua CSDL.
 *
 * <h2>Vì sao viết lại từ đầu</h2>
 * <p>{@code PlayersPanel} cũ đọc bảng {@code player} bằng SQL, nên chỉ thấy dữ
 * liệu của <i>lần lưu gần nhất</i>, và mọi thay đổi nó ghi xuống đều bị đè mất
 * ở lần tự lưu kế tiếp (mỗi 8 giây, ghi bằng giá trị trong RAM).</p>
 *
 * <h2>Ba bước của mọi thay đổi</h2>
 * <ol>
 *   <li><b>Sửa bộ nhớ</b> — gán thẳng vào {@code player.nPoint} / {@code player.inventory}.</li>
 *   <li><b>Đẩy xuống client</b> — {@code Service.point()}, {@code InventoryService.sendItem*()}.</li>
 *   <li><b>Ghi CSDL ngay</b> — {@code PlayerDAO.updatePlayer()} ở thread nền.</li>
 * </ol>
 * <p>Xem {@link #applyAndPush(Player, String, Runnable)}.</p>
 *
 * <h2>Danh sách: cả người đang online lẫn offline</h2>
 * <p>Người đang online lấy từ {@code Client.gI()} (đối tượng sống). Người offline
 * đọc từ CSDL. <b>Online luôn xếp lên trên.</b></p>
 * <p>Chỉ <b>người đang online mới sửa được</b> — offline không có đối tượng trong
 * bộ nhớ để sửa, và ghi thẳng CSDL sẽ bị vòng tự lưu của người khác ghi đè.
 * Chọn người offline thì mọi ô nhập bị khoá và panel nói rõ lý do.</p>
 *
 * <h2>Vì sao ô nhập từng bị "trả về số cũ"</h2>
 * <p>Đây là lỗi đã sửa. Mỗi nhịp làm mới, bảng người chơi bị dựng lại
 * ({@code setRowCount(0)} rồi thêm dòng), việc đó <b>xoá lựa chọn</b>; ngay sau
 * đó code chọn lại dòng cũ, và thao tác chọn lại này <b>kích hoạt
 * listener</b> → gọi nạp lại <i>cưỡng bức</i> → ghi đè đúng ô đang gõ.</p>
 * <p>Sửa bằng hai lớp chặn:</p>
 * <ul>
 *   <li>{@link #syncing} — bật trong lúc chương trình tự chọn lại dòng, để
 *       listener biết đây không phải người dùng bấm.</li>
 *   <li>{@link #dirty} — ô nào người dùng đã gõ vào thì nhịp làm mới
 *       <b>không đụng tới nữa</b>, cho tới khi bấm "Áp dụng" hoặc "Đọc lại".
 *       Chỉ dựa vào {@code hasFocus()} là không đủ: gõ xong chuyển sang ô khác
 *       là ô trước mất tiêu.</li>
 * </ul>
 */
public class PlayerManagerPanel extends JPanel {

    /** Chu kỳ đọc lại dữ liệu sống, tính bằng mili giây. */
    public static final int REFRESH_MS = 1000;

    /** Bộ icon dùng cho bảng vật phẩm. x2 vừa mắt ở chiều cao dòng 32px. */
    private static final String ICON_DIR = "data/icon/x2/";

    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color OK_GREEN = new Color(34, 139, 34);
    private static final Color WARN_RED = new Color(192, 57, 43);
    private static final Color GREY = new Color(120, 120, 120);
    /** Nền của nút đang bị khoá. */
    private static final Color DISABLED_BG = new Color(200, 200, 200);
    /** Màu chấm của người đang online. Đậm hơn OK_GREEN để nổi trên nền trắng. */
    private static final Color ONLINE_GREEN = new Color(0, 170, 60);

    // ---------------------------------------------------------------- danh sách
    private final DefaultTableModel playerModel;
    private final JTable playerTable;
    private final JTextField txtSearch = new JTextField(12);
    private final JCheckBox chkAuto = new JCheckBox("Tự cập nhật", true);
    private final JCheckBox chkOnlyOnline = new JCheckBox("Chỉ người đang online", false);
    private final JLabel lblCount = new JLabel();
    private final JLabel lblStatus = new JLabel(" ");

    /** Người chơi đang chọn, nếu đang online. {@code null} khi chọn người offline. */
    private Player selected;

    /** Dòng đang chọn (dùng cho cả online lẫn offline). */
    private Row selectedRow;

    // ------------------------------------------------------------- ô nhập chỉ số
    private final JTextField fHpg = new JTextField(12);
    private final JTextField fMpg = new JTextField(12);
    private final JTextField fDameg = new JTextField(12);
    private final JTextField fDefg = new JTextField(12);
    private final JTextField fCritg = new JTextField(12);
    private final JTextField fPower = new JTextField(12);
    private final JTextField fTiemNang = new JTextField(12);
    private final JTextField fLimitPower = new JTextField(12);
    private final JTextField fHp = new JTextField(12);
    private final JTextField fMp = new JTextField(12);
    private final JTextField fStamina = new JTextField(12);
    private final JTextField fMaxStamina = new JTextField(12);
    private final JTextField fGold = new JTextField(12);
    private final JTextField fGem = new JTextField(12);
    private final JTextField fRuby = new JTextField(12);

    // ---- O nhap cua DE TU. De tu la mot Player rieng (lop Detu) treo o
    // Player.Detu, co nPoint va inventory rieng, luu trong cot `pet` cua bang
    // player. Sua chi so cua chu KHONG anh huong gi den de tu.
    private final JTextField fDtTen = new JTextField(12);
    private final JTextField fDtHpg = new JTextField(12);
    private final JTextField fDtMpg = new JTextField(12);
    private final JTextField fDtDameg = new JTextField(12);
    private final JTextField fDtDefg = new JTextField(12);
    private final JTextField fDtCritg = new JTextField(12);
    private final JTextField fDtPower = new JTextField(12);
    private final JTextField fDtTiemNang = new JTextField(12);
    private final JTextField fDtLimitPower = new JTextField(12);
    private final JTextField fDtHp = new JTextField(12);
    private final JTextField fDtMp = new JTextField(12);
    private final JTextField fDtStamina = new JTextField(12);
    private final JTextField fDtMaxStamina = new JTextField(12);

    private final JTextField[] allFields = {
        fHpg, fMpg, fDameg, fDefg, fCritg, fPower, fTiemNang, fLimitPower,
        fHp, fMp, fStamina, fMaxStamina, fGold, fGem, fRuby,
        fDtTen, fDtHpg, fDtMpg, fDtDameg, fDtDefg, fDtCritg,
        fDtPower, fDtTiemNang, fDtLimitPower, fDtHp, fDtMp, fDtStamina,
        fDtMaxStamina
    };

    /** Nhan chi doc cho chi so dan xuat cua de tu. */
    private final JLabel vDtHpMax = new JLabel();
    private final JLabel vDtMpMax = new JLabel();
    private final JLabel vDtDame = new JLabel();
    private final JLabel vDtDef = new JLabel();
    private final JLabel vDtCrit = new JLabel();

    /** Cho biet nguoi dang chon co de tu hay khong. */
    private final JLabel lblDtTrangThai = new JLabel();

    private final javax.swing.JComboBox<String> cbDtLoai =
            new javax.swing.JComboBox<>(new String[]{
                "0 — Đệ tử thường", "1 — Mabu", "2 — U Bư", "3 — Kid Jiren",
                "4 — Kid Beer", "5 — Black"});

    private final javax.swing.JComboBox<String> cbDtTrangThai =
            new javax.swing.JComboBox<>(new String[]{
                "0 — Đi theo (FOLLOW)", "1 — (không dùng)", "2 — Đánh (ATTACK)",
                "3 — Về nhà (GOHOME)", "4 — Hợp thể (FUSION)"});

    /** Nhãn chỉ đọc hiển thị giá trị dẫn xuất sau {@code calPoint()}. */
    private final JLabel vHpMax = new JLabel();
    private final JLabel vMpMax = new JLabel();
    private final JLabel vDame = new JLabel();
    private final JLabel vDef = new JLabel();
    private final JLabel vCrit = new JLabel();

    /** Số dư và tổng đã nạp — chỉ đọc, sửa qua tab "Nạp tiền". */
    private final JLabel vVnd = new JLabel();
    private final JLabel vTongNap = new JLabel();
    private final JLabel vTongNap2 = new JLabel();
    private final JLabel vCoin = new JLabel();

    private final JLabel lblWho = new JLabel("Chưa chọn người chơi");

    /**
     * Những ô người dùng đã gõ vào. Nhịp làm mới không đụng tới chúng.
     * Xoá khi bấm "Áp dụng", "Đọc lại", hoặc khi đổi người chơi.
     */
    private final Set<JTextField> dirty = new HashSet<>();

    /** Bật khi <b>chương trình</b> đang ghi vào ô nhập, để không tự đánh dấu bẩn. */
    private boolean writing;

    /** Bật khi chương trình tự chọn lại dòng, để listener bỏ qua. */
    private boolean syncing;

    // -------------------------------------------------------------- bảng vật phẩm
    private final DefaultTableModel bodyModel = itemModel();
    private final DefaultTableModel bagModel = itemModel();
    private final DefaultTableModel boxModel = itemModel();
    private final JTable bodyTable = itemTable(bodyModel);
    private final JTable bagTable = itemTable(bagModel);
    private final JTable boxTable = itemTable(boxModel);

    /**
     * Mọi nút thay đổi dữ liệu. Gom lại một chỗ để khoá hết khi chọn người
     * offline — nút sáng mà bấm vào chỉ báo lỗi thì gây hiểu nhầm.
     */
    private final List<JButton> editButtons = new ArrayList<>();

    private final Timer timer;

    /**
     * Một dòng trong danh sách. Gộp chung người online và offline để bảng chỉ
     * có một nguồn dữ liệu.
     */
    private static final class Row {

        long id;
        String name;
        byte gender;
        long power;
        String map;
        long tongNap;     // tổng đã nạp của TÀI KHOẢN sở hữu nhân vật
        String quyen = "—";   // Admin / Founder / User…
        boolean online;
        Player live;      // chỉ có khi online
    }

    private List<Row> rows = new ArrayList<>();

    /** Bao lâu mới đọc lại danh sách nhân vật từ CSDL (mili giây). */
    private static final long OFFLINE_CACHE_MS = 15_000;

    /** Bộ định dạng số dùng chung: dấu chấm ngăn nghìn. */
    private static final DecimalFormat NUM_FMT = buildNumFormat();

    private static DecimalFormat buildNumFormat() {
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", sym);
    }

    private List<Row> offlineCache;
    private long offlineCacheAt;

    public PlayerManagerPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        playerModel = new DefaultTableModel(
                new Object[]{"", "ID", "Tên", "Hệ", "Quyền", "Sức mạnh", "Tổng nạp",
                "Bản đồ"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        playerTable = new JTable(playerModel);
        playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerTable.setRowHeight(24);
        playerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        playerTable.getColumnModel().getColumn(0).setPreferredWidth(26);
        playerTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        playerTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        playerTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        // Sức mạnh có thể tới hàng trăm tỉ; kèm dấu ngăn nghìn là ~15 ký tự,
        // hẹp hơn thì bảng cắt bớt thành "..." và không đọc được số nữa.
        playerTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        playerTable.getColumnModel().getColumn(5).setPreferredWidth(135);
        playerTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        playerTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        playerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(c == 0 ? CENTER : LEADING);
                if (!sel && r < rows.size()) {
                    boolean on = rows.get(r).online;
                    // Cột 0 là chấm trạng thái: xanh lá cho người đang online,
                    // xám cho người offline. Các cột còn lại chỉ đen/xám.
                    comp.setForeground(c == 0
                            ? (on ? ONLINE_GREEN : GREY)
                            : (on ? Color.BLACK : GREY));
                }
                return comp;
            }
        });

        add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildPlayerList(), buildDetail());
        split.setDividerLocation(560);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        playerTable.getSelectionModel().addListSelectionListener(e -> {
            // syncing = chương trình đang chọn lại dòng sau khi dựng lại bảng.
            // Không có chốt này thì mỗi nhịp làm mới sẽ nạp lại cưỡng bức và
            // xoá mất số người dùng đang gõ dở.
            if (!e.getValueIsAdjusting() && !syncing) {
                onSelectPlayer();
            }
        });

        // Gõ vào ô nào thì đánh dấu ô đó, nhịp làm mới sẽ chừa ra.
        for (JTextField f : allFields) {
            f.getDocument().addDocumentListener(new DocumentListener() {
                private void mark() {
                    if (!writing) {
                        dirty.add(f);
                    }
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    mark();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    mark();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    mark();
                }
            });
        }

        chkOnlyOnline.addActionListener(e -> {
            offlineCache = null;
            refresh();
        });

        timer = new Timer(REFRESH_MS, e -> refresh());
        timer.start();
        refresh();
        setEditable(false);

        // Bang do cua nguoi choi hien ten va anh lay tu mau vat pham, nen sua
        // ben Quan Ly Vat Pham la o day phai ve lai theo.
        LamMoi.nghe(() -> loadItems(true),
                LamMoi.VAT_PHAM, LamMoi.ANH, LamMoi.LOAI);
        LamMoi.nghe(LamMoi.NGUOI_CHOI, this::refresh);
    }

    // =====================================================================
    //  Dựng giao diện
    // =====================================================================

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("Quản Lý Người Chơi (realtime)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT);
        // Cho tieu de duoc phep co lai.
        //
        // BorderLayout cap du be rong mong muon cho ca WEST lan EAST; tong hai
        // ben vuot qua cua so thi chung DE LEN NHAU chu khong tu xuong dong.
        // Dat be rong toi thieu nho thi khi chat, tieu de bi cat bot chu con
        // hang cong cu ben phai van doc duoc — thu can dung hon.
        title.setMinimumSize(new java.awt.Dimension(60, 1));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(new JLabel("Tìm:"));
        right.add(txtSearch);
        JButton btnNow = new JButton("Làm mới");
        btnNow.addActionListener(e -> {
            offlineCache = null;   // ép đọc lại CSDL ngay
            refresh();
        });
        right.add(btnNow);
        right.add(chkOnlyOnline);
        right.add(chkAuto);
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        right.add(lblCount);

        chkAuto.addActionListener(e -> {
            if (chkAuto.isSelected()) {
                timer.start();
            } else {
                timer.stop();
            }
        });
        txtSearch.addActionListener(e -> refresh());

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent buildPlayerList() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(ServerGuiUtils.cuon(playerTable), BorderLayout.CENTER);

        JPanel act = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        act.setOpaque(false);
        act.add(button("Gửi thông báo", ACCENT, e -> doNotify()));
        act.add(button("Đổi tên", ACCENT, e -> doDoiTen()));
        act.add(button("Kick", WARN_RED, e -> doKick()));
        act.add(button("Hồi hết chiêu", ACCENT, e -> doResetChieu()));
        act.add(button("Chi tiết HP", ACCENT, e -> doChiTietHp()));
        act.add(button("Đặt nhiệm vụ", ACCENT, e -> doDatNhiemVu()));
        // KHÔNG dùng editButton: nút này chỉ đọc nhật ký trong CSDL nên xem
        // được cả người đang offline — mà đó mới là lúc hay cần xem nhất.
        act.add(button("Lịch sử vật phẩm", new Color(120, 80, 170),
                e -> moLichSuVatPham()));
        act.add(button("Lưu DB ngay", OK_GREEN, e -> doSaveNow()));
        act.add(button("Xoá HẾT nhân vật", new Color(150, 30, 30), e -> doDeleteAll()));
        p.add(act, BorderLayout.SOUTH);
        return p;
    }

    private JComponent buildDetail() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);

        lblWho.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblWho.setBorder(new EmptyBorder(0, 6, 0, 0));
        p.add(lblWho, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        // Cửa sổ hẹp thì tiêu đề tab xuống hai hàng, đẩy nội dung xuống và dễ
        // bấm nhầm. Cho cuộn ngang thay vì xuống hàng.
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addTab("Chỉ số & Tiềm năng", buildStatTab());
        tabs.addTab("Đệ tử", buildDetuTab());
        tabs.addTab("Đồ đang mặc", buildItemTab(bodyTable, Slot.BODY));
        tabs.addTab("Hành trang", buildItemTab(bagTable, Slot.BAG));
        tabs.addTab("Rương ở nhà", buildItemTab(boxTable, Slot.BOX));
        tabs.addTab("Nạp tiền", buildTopUpTab());
        tabs.addTab("Chi tiết", buildChiTietTab());
        // Doi tab la doc lai nguoi dang chon. Dua theo TEN chu khong theo so
        // thu tu, de sau nay chen them tab thi khong lang le nap nham bang.
        tabs.addChangeListener(e -> napTabNguoiChoi(tabs.getTitleAt(
                Math.max(0, tabs.getSelectedIndex()))));
        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    /**
     * Nạp lại tab vừa mở của người đang chọn.
     *
     * <p>Truyền {@code force = true}: mấy hàm này có bộ nhớ đệm theo thời gian,
     * không ép thì bấm sang tab khác rồi quay lại vẫn thấy số cũ — đúng cái mà
     * việc nạp lại này sinh ra để tránh.</p>
     */
    private void napTabNguoiChoi(String ten) {
        try {
            switch (ten) {
                case "Chỉ số & Tiềm năng":
                    loadStats(true);
                    break;
                case "Đồ đang mặc":
                case "Hành trang":
                case "Rương ở nhà":
                    loadItems(true);
                    break;
                case "Chi tiết":
                    loadChiTiet();
                    break;
                case "Nạp tiền":
                    loadHistory();
                    break;
                default:
                    // Tab De tu tu nap khi chon nguoi, khong can lam gi them.
                    break;
            }
        } catch (Exception ex) {
            Logger.logException(PlayerManagerPanel.class, ex,
                    "Lỗi nạp lại tab " + ten);
        }
    }

    /**
     * Tab chỉ số.
     *
     * <p>Cột trái là chỉ số <b>gốc</b> (sửa được), cột phải là giá trị
     * <b>thực tế</b> sau khi {@code calPoint()} cộng thêm trang bị/thẻ bài.
     * {@code calPoint()} tính lại phần dẫn xuất từ phần gốc, nên sửa thẳng
     * {@code dame} là vô nghĩa — nó bị tính đè ngay ở nhịp sau.</p>
     */
    private JComponent buildStatTab() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int row = 0;

        row = section(grid, c, row, "Chỉ số gốc (sửa ở đây — calPoint() sẽ tính lại phần dẫn xuất)");
        row = field(grid, c, row, "HP gốc (hpg)", fHpg, "Thực tế:", vHpMax);
        row = field(grid, c, row, "KI gốc (mpg)", fMpg, "Thực tế:", vMpMax);
        row = field(grid, c, row, "Sức đánh gốc (dameg)", fDameg, "Thực tế:", vDame);
        row = field(grid, c, row, "Giáp gốc (defg)", fDefg, "Thực tế:", vDef);
        row = field(grid, c, row, "Chí mạng gốc (critg)", fCritg, "Thực tế:", vCrit);

        row = section(grid, c, row, "Sức mạnh & tiềm năng");
        row = field(grid, c, row, "Sức mạnh (power)", fPower, null, null);
        row = field(grid, c, row, "Tiềm năng", fTiemNang, null, null);
        row = field(grid, c, row, "Giới hạn sức mạnh", fLimitPower, null, null);

        row = section(grid, c, row, "Trạng thái hiện tại");
        row = field(grid, c, row, "HP hiện tại", fHp, null, null);
        row = field(grid, c, row, "KI hiện tại", fMp, null, null);
        row = field(grid, c, row, "Thể lực", fStamina, null, null);
        row = field(grid, c, row, "Thể lực tối đa", fMaxStamina, null, null);

        row = section(grid, c, row, "Tài sản");
        row = field(grid, c, row, "Vàng", fGold, null, null);
        row = field(grid, c, row, "Ngọc", fGem, null, null);
        row = field(grid, c, row, "Hồng ngọc", fRuby, null, null);

        // Tiền nạp nằm ở bảng `account`, không phải bảng `player`, nên chỉ đọc ở
        // đây; muốn nạp thì sang tab "Nạp tiền".
        row = section(grid, c, row, "Tiền nạp (thuộc tài khoản, sửa ở tab Nạp tiền)");
        row = readOnly(grid, c, row, "Số dư (VNĐ)", vVnd);
        row = readOnly(grid, c, row, "Tổng đã nạp", vTongNap);
        row = readOnly(grid, c, row, "Tổng nạp (cách 2)", vTongNap2);
        row = readOnly(grid, c, row, "Coin", vCoin);

        JPanel act = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        act.setOpaque(false);
        act.add(editButton("Áp dụng ngay", OK_GREEN, e -> applyStats()));
        act.add(editButton("Hồi đầy HP/KI", ACCENT, e -> doFullHeal()));
        act.add(button("Đọc lại từ server", GREY, e -> {
            dirty.clear();
            loadStats(true);
        }));

        // GridBagLayout tự canh giữa phần dư -> bọc thêm một lớp để bám mép trái.
        JPanel anchor = new JPanel(new BorderLayout());
        anchor.setOpaque(false);
        anchor.add(grid, BorderLayout.WEST);

        JScrollPane sc = ServerGuiUtils.cuon(anchor);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(act, BorderLayout.SOUTH);
        return wrap;
    }

    /**
     * Tab đệ tử.
     *
     * <p>Đệ tử là một {@link Detu} riêng treo ở {@code Player.Detu}, có
     * {@code nPoint} và {@code inventory} riêng, lưu trong cột {@code pet} của
     * bảng {@code player}. Sửa chỉ số của chủ không ảnh hưởng gì tới đệ tử.</p>
     *
     * <p>Cột trái là chỉ số <b>gốc</b>, cột phải là giá trị <b>thực tế</b> sau
     * khi {@code calPoint()} chạy — giống tab chỉ số của người chơi.</p>
     */
    private JComponent buildDetuTab() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int row = 0;

        lblDtTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridy = row++;
        c.gridx = 0;
        c.gridwidth = 4;
        grid.add(lblDtTrangThai, c);
        c.gridwidth = 1;

        row = section(grid, c, row, "Nhận dạng");
        row = field(grid, c, row, "Tên đệ tử", fDtTen, null, null);
        row = combo(grid, c, row, "Loại (typeDeTu)", cbDtLoai);
        row = combo(grid, c, row, "Trạng thái (status)", cbDtTrangThai);

        row = section(grid, c, row,
                "Chỉ số gốc (sửa ở đây — calPoint() sẽ tính lại phần dẫn xuất)");
        row = field(grid, c, row, "HP gốc (hpg)", fDtHpg, "Thực tế:", vDtHpMax);
        row = field(grid, c, row, "KI gốc (mpg)", fDtMpg, "Thực tế:", vDtMpMax);
        row = field(grid, c, row, "Sức đánh gốc (dameg)", fDtDameg, "Thực tế:", vDtDame);
        row = field(grid, c, row, "Giáp gốc (defg)", fDtDefg, "Thực tế:", vDtDef);
        row = field(grid, c, row, "Chí mạng gốc (critg)", fDtCritg, "Thực tế:", vDtCrit);

        row = section(grid, c, row, "Sức mạnh & tiềm năng");
        row = field(grid, c, row, "Sức mạnh (power)", fDtPower, null, null);
        row = field(grid, c, row, "Tiềm năng", fDtTiemNang, null, null);
        row = field(grid, c, row, "Giới hạn sức mạnh", fDtLimitPower, null, null);

        row = section(grid, c, row, "Trạng thái hiện tại");
        row = field(grid, c, row, "HP hiện tại", fDtHp, null, null);
        row = field(grid, c, row, "KI hiện tại", fDtMp, null, null);
        row = field(grid, c, row, "Thể lực", fDtStamina, null, null);
        row = field(grid, c, row, "Thể lực tối đa", fDtMaxStamina, null, null);

        // Do de tu dang mac nam o Detu.inventory.itemsBody; tab "Do dang mac"
        // chi hien cua chu — noi ro de khong ai di tim nham.
        row = section(grid, c, row, "Ghi chú");
        c.gridy = row++;
        c.gridx = 0;
        c.gridwidth = 4;
        JLabel ghiChu = new JLabel("<html><span style='color:#777'>"
                + "Đệ tử có túi và ô trang bị <b>riêng</b> "
                + "(<code>Detu.inventory</code>); tab <b>Đồ đang mặc</b> chỉ hiện "
                + "đồ của chủ.<br>"
                + "Đệ tử không có phiên kết nối riêng — thay đổi được đẩy qua "
                + "phiên của chủ.<br>"
                + "Chủ chưa có đệ tử thì các ô ở đây bị khoá; panel không tạo "
                + "đệ tử mới."
                + "</span></html>");
        grid.add(ghiChu, c);
        c.gridwidth = 1;

        JPanel act = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        act.setOpaque(false);
        act.add(editButton("Áp dụng ngay", OK_GREEN, e -> applyDetuStats()));
        act.add(editButton("Hồi đầy HP/KI", ACCENT, e -> doDetuFullHeal()));
        act.add(button("Đọc lại từ server", GREY, e -> {
            dirty.clear();
            loadStats(true);
        }));

        JPanel anchor = new JPanel(new BorderLayout());
        anchor.setOpaque(false);
        anchor.add(grid, BorderLayout.WEST);

        JScrollPane sc = ServerGuiUtils.cuon(anchor);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(act, BorderLayout.SOUTH);
        return wrap;
    }

    /** Một dòng có ô chọn thay vì ô nhập. */
    private int combo(JPanel g, GridBagConstraints c, int row, String label,
                      javax.swing.JComboBox<String> cb) {
        c.gridy = row;
        c.gridx = 0;
        g.add(new JLabel(label), c);
        c.gridx = 1;
        g.add(cb, c);
        return row + 1;
    }

    /**
     * Nạp thông tin đệ tử vào tab.
     *
     * <p>Chủ offline hoặc chưa có đệ tử thì xoá trắng và khoá. Để số của người
     * trước nằm lại rồi bấm "Áp dụng" là ghi chỉ số người này sang người khác.</p>
     */
    private void loadDetu(Player chu, boolean force) {
        Detu dt = chu != null ? chu.Detu : null;
        boolean co = dt != null && dt.nPoint != null;
        khoaODeTu(co);

        if (!co) {
            lblDtTrangThai.setForeground(GREY);
            lblDtTrangThai.setText(chu == null
                    ? "○ Chưa chọn người chơi, hoặc người này đang offline."
                    : "○ " + chu.name + " chưa có đệ tử.");
            boolean truoc = writing;
            writing = true;
            try {
                for (JTextField f : oDeTu()) {
                    f.setText("");
                }
                for (JLabel l : new JLabel[]{vDtHpMax, vDtMpMax, vDtDame,
                    vDtDef, vDtCrit}) {
                    l.setText("");
                }
            } finally {
                writing = truoc;
            }
            return;
        }

        lblDtTrangThai.setForeground(OK_GREEN);
        lblDtTrangThai.setText("● Đệ tử của " + chu.name + ": " + dt.name
                + "   [id=" + dt.id + "]");

        NPoint n = dt.nPoint;
        if (force || !dirty.contains(fDtTen)) {
            fDtTen.setText(dt.name == null ? "" : dt.name);
        }
        set(fDtHpg, n.hpg, force);
        set(fDtMpg, n.mpg, force);
        set(fDtDameg, n.dameg, force);
        set(fDtDefg, n.defg, force);
        set(fDtCritg, n.critg, force);
        set(fDtPower, n.power, force);
        set(fDtTiemNang, n.tiemNang, force);
        set(fDtLimitPower, n.limitPower, force);
        set(fDtHp, n.hp, force);
        set(fDtMp, n.mp, force);
        set(fDtStamina, n.stamina, force);
        set(fDtMaxStamina, n.maxStamina, force);
        if (force) {
            chonTheoSo(cbDtLoai, dt.typeDeTu);
            chonTheoSo(cbDtTrangThai, dt.status);
        }
        vDtHpMax.setText(fmt(n.hpMax));
        vDtMpMax.setText(fmt(n.mpMax));
        vDtDame.setText(fmt(n.dame));
        vDtDef.setText(fmt(n.def));
        vDtCrit.setText(fmt(n.crit));
    }

    private JTextField[] oDeTu() {
        return new JTextField[]{fDtTen, fDtHpg, fDtMpg, fDtDameg,
            fDtDefg, fDtCritg, fDtPower, fDtTiemNang, fDtLimitPower, fDtHp,
            fDtMp, fDtStamina, fDtMaxStamina};
    }

    private void khoaODeTu(boolean cho) {
        for (JTextField f : oDeTu()) {
            f.setEditable(cho);
            f.setBackground(cho ? Color.WHITE : new Color(245, 245, 245));
        }
        cbDtLoai.setEnabled(cho);
        cbDtTrangThai.setEnabled(cho);
    }

    /** Ô chọn được đánh số ở đầu nhãn ("2 — U Bư") nên chỉ số = giá trị. */
    private static void chonTheoSo(javax.swing.JComboBox<String> cb, int gt) {
        if (gt >= 0 && gt < cb.getItemCount()) {
            cb.setSelectedIndex(gt);
        }
    }

    private void applyDetuStats() {
        final Player chu = selected;
        if (chu == null || chu.Detu == null || chu.Detu.nPoint == null) {
            warn("Người này chưa có đệ tử.");
            return;
        }
        final Detu dt = chu.Detu;
        applyAndPush(chu, "cập nhật chỉ số đệ tử", () -> {
            NPoint n = dt.nPoint;
            String ten = fDtTen.getText().trim();
            if (!ten.isEmpty()) {
                dt.name = ten;
            }
            dt.typeDeTu = (byte) cbDtLoai.getSelectedIndex();
            dt.status = (byte) cbDtTrangThai.getSelectedIndex();
            n.hpg = num(fDtHpg, n.hpg);
            n.mpg = num(fDtMpg, n.mpg);
            n.dameg = num(fDtDameg, n.dameg);
            n.defg = (int) num(fDtDefg, n.defg);
            n.critg = (int) num(fDtCritg, n.critg);
            n.power = num(fDtPower, n.power);
            n.tiemNang = num(fDtTiemNang, n.tiemNang);
            n.limitPower = (byte) num(fDtLimitPower, n.limitPower);
            n.stamina = (int) num(fDtStamina, n.stamina);
            n.maxStamina = (int) num(fDtMaxStamina, n.maxStamina);

            // calPoint() truoc roi moi kep HP/KI — nguoc lai thi hpMax con la
            // so cu, dat HP moi bi kep xuong tran cu.
            n.calPoint();
            n.setHp(Math.min(num(fDtHp, n.hp), n.hpMax));
            n.setMp(Math.min(num(fDtMp, n.mp), n.mpMax));

            // De tu khong co phien rieng: point() tu bo qua goi -42 cho de tu,
            // con thong tin trang bi/avatar day qua phien cua CHU.
            Service.gI().point(dt);
            Service.gI().showInfoPet(chu);
        });
    }

    private void doDetuFullHeal() {
        final Player chu = selected;
        if (chu == null || chu.Detu == null || chu.Detu.nPoint == null) {
            warn("Người này chưa có đệ tử.");
            return;
        }
        final Detu dt = chu.Detu;
        applyAndPush(chu, "hồi đầy HP/KI cho đệ tử", () -> {
            dt.nPoint.calPoint();
            dt.nPoint.setHp(dt.nPoint.hpMax);
            dt.nPoint.setMp(dt.nPoint.mpMax);
            Service.gI().point(dt);
            Service.gI().showInfoPet(chu);
        });
    }

    /** Một dòng chỉ đọc: nhãn trái + giá trị phải. */
    private int readOnly(JPanel g, GridBagConstraints c, int row, String label, JLabel value) {
        c.gridy = row;
        c.gridx = 0;
        g.add(new JLabel(label), c);
        c.gridx = 1;
        value.setFont(new Font("Consolas", Font.BOLD, 12));
        value.setForeground(new Color(180, 90, 0));
        g.add(value, c);
        return row + 1;
    }

    private int section(JPanel g, GridBagConstraints c, int row, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(ACCENT);
        l.setBorder(new EmptyBorder(10, 0, 2, 0));
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 4;
        g.add(l, c);
        c.gridwidth = 1;
        return row + 1;
    }

    private int field(JPanel g, GridBagConstraints c, int row,
                      String label, JTextField f, String rLabel, JLabel rValue) {
        c.gridy = row;
        c.gridx = 0;
        g.add(new JLabel(label), c);
        c.gridx = 1;
        g.add(f, c);
        if (rLabel != null) {
            c.gridx = 2;
            JLabel l = new JLabel(rLabel);
            l.setForeground(GREY);
            g.add(l, c);
            c.gridx = 3;
            rValue.setFont(new Font("Consolas", Font.BOLD, 12));
            g.add(rValue, c);
        }
        return row + 1;
    }

    private JComponent buildItemTab(JTable table, Slot slot) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(ServerGuiUtils.cuon(table), BorderLayout.CENTER);

        // Nháy đúp vào một dòng là mở luôn hộp thoại: ô có đồ thì sửa, ô trống
        // thì đặt item mới. Nhanh hơn và chắc hơn "chọn dòng rồi bấm nút" —
        // nhất là khi bảng đang tự cập nhật.
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                int r = table.rowAtPoint(e.getPoint());
                if (r < 0) {
                    return;
                }
                table.getSelectionModel().setSelectionInterval(r, r);
                Object id = table.getValueAt(r, 2);
                boolean empty = id == null || "-".equals(String.valueOf(id));
                itemDialog(slot, table, !empty);
            }
        });

        JPanel act = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        act.setOpaque(false);
        act.add(editButton("Thêm / Đặt item", OK_GREEN, e -> itemDialog(slot, table, false)));
        act.add(editButton("Xoá item", WARN_RED, e -> deleteItem(slot, table)));
        // Khong con nut "Sua item": nhay dup vao dong la sua, gon hon mot buoc.
        JLabel tip = new JLabel("  (nháy đúp để sửa • giữ Ctrl/Shift để chọn nhiều ô rồi xoá)");
        tip.setForeground(GREY);
        act.add(tip);
        p.add(act, BorderLayout.SOUTH);
        return p;
    }

    /** Khuôn bảng vật phẩm. Cột 1 là ảnh icon của item. */
    private static DefaultTableModel itemModel() {
        return new DefaultTableModel(
                new Object[]{"Ô", "Ảnh", "ID", "Tên vật phẩm", "Loại", "SL",
                    "Chỉ số (option)"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                // Cột ảnh phải khai báo kiểu Icon thì JTable mới vẽ ảnh
                // thay vì gọi toString().
                return c == 1 ? ImageIcon.class : Object.class;
            }
        };
    }

    private static JTable itemTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(32);
        // Cho chọn nhiều ô cùng lúc (giữ Ctrl / Shift) để xoá hàng loạt.
        // Nháy đúp vẫn thu về đúng một dòng trước khi mở hộp thoại sửa.
        t.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer grey = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                           boolean foc, int r, int col) {
                Component comp = super.getTableCellRendererComponent(tb, v, sel, foc, r, col);
                Object id = tb.getValueAt(r, 2);
                boolean empty = id == null || "-".equals(String.valueOf(id));
                if (!sel) {
                    comp.setForeground(empty ? new Color(170, 170, 170) : Color.BLACK);
                }
                return comp;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) {
            if (i != 1) {
                t.getColumnModel().getColumn(i).setCellRenderer(grey);
            }
        }
        t.getColumnModel().getColumn(0).setPreferredWidth(36);
        t.getColumnModel().getColumn(1).setPreferredWidth(40);
        t.getColumnModel().getColumn(2).setPreferredWidth(50);
        t.getColumnModel().getColumn(3).setPreferredWidth(210);
        t.getColumnModel().getColumn(4).setPreferredWidth(130);
        t.getColumnModel().getColumn(5).setPreferredWidth(50);
        t.getColumnModel().getColumn(6).setPreferredWidth(360);
        return t;
    }

    /** Như {@link #button} nhưng ghi thêm vào danh sách nút bị khoá khi xem offline. */
    private JButton editButton(String text, Color bg, java.awt.event.ActionListener a) {
        JButton b = button(text, bg, a);
        // Nhớ màu gốc để lúc khoá còn đổi sang xám rồi trả lại đúng màu.
        b.putClientProperty("bgOn", bg);
        editButtons.add(b);
        return b;
    }

    private JButton button(String text, Color bg, java.awt.event.ActionListener a) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.addActionListener(a);
        return b;
    }

    // =====================================================================
    //  Ảnh vật phẩm
    // =====================================================================

    /**
     * Bộ nhớ đệm icon đã nạp.
     *
     * <p>Thư mục icon có hơn 22.000 file. Nạp sẵn tất cả là vô nghĩa — panel chỉ
     * hiện vài chục ô một lúc. Nên nạp theo nhu cầu rồi giữ lại.
     * {@code null} được lưu luôn để không thử đọc lại file không tồn tại.</p>
     */
    private static final Map<Integer, ImageIcon> ICONS = new HashMap<>();

    /**
     * Icon của một vật phẩm, đã thu về 28px cho vừa chiều cao dòng.
     *
     * @return {@code null} nếu không có file icon — bảng để trống ô đó
     */
    static ImageIcon iconOf(int iconId) {
        if (ICONS.containsKey(iconId)) {
            return ICONS.get(iconId);
        }
        ImageIcon icon = null;
        try {
            File f = new File(ICON_DIR + iconId + ".png");
            if (f.isFile()) {
                java.awt.image.BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    int w = img.getWidth();
                    int h = img.getHeight();
                    if (w > 0 && h > 0) {
                        // Giữ tỉ lệ, cạnh dài nhất bằng 28px.
                        double k = 28.0 / Math.max(w, h);
                        icon = new ImageIcon(k < 1 ? thuNho(img,
                                Math.max(1, (int) (w * k)),
                                Math.max(1, (int) (h * k))) : img);
                    }
                }
            }
        } catch (Exception ignored) {
            // Icon hỏng không phải lỗi đáng dừng panel — cứ để trống ô ảnh.
        }
        ICONS.put(iconId, icon);
        return icon;
    }

    /**
     * Quên ảnh đã đệm của một id, để lần vẽ sau đọc lại từ đĩa.
     *
     * <p>Phải gọi sau khi ghi đè một tệp icon. {@link #ICONS} giữ cả kết quả
     * {@code null}, nên không dọn thì panel cứ vẽ ảnh cũ — hoặc vẫn bỏ trống ô
     * dù ảnh mới đã nằm sẵn trên đĩa.</p>
     */
    static void quenAnh(int iconId) {
        ICONS.remove(iconId);
    }

    /**
     * Thu nhỏ ảnh bằng {@link java.awt.Graphics2D}, không dùng
     * {@code getScaledInstance}.
     *
     * <p>{@code getScaledInstance} với {@code SCALE_SMOOTH} chạy chậm hơn hẳn —
     * nó vẽ theo lối cũ, đơn luồng, và trả về một {@code Image} lười, phải đợi
     * lúc {@code ImageIcon} nạp mới thật sự tính. Nhân với sáu trăm dòng trong
     * bảng vật phẩm thì thành khựng thấy rõ. Ở cỡ 28px, nội suy song tuyến cho
     * ảnh không khác mắt thường nhìn được.</p>
     */
    private static java.awt.image.BufferedImage thuNho(
            java.awt.image.BufferedImage goc, int w, int h) {
        java.awt.image.BufferedImage ra = new java.awt.image.BufferedImage(
                w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = ra.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(goc, 0, 0, w, h, null);
        g.dispose();
        return ra;
    }

    /**
     * Bộ vẽ ô ảnh cho bảng: <b>ô chứa id icon</b>, ảnh chỉ đọc lúc cần vẽ.
     *
     * <p>Nhét thẳng {@code ImageIcon} vào mô hình bảng nghĩa là nạp hết ảnh của
     * mọi dòng ngay lúc dựng bảng, kể cả những dòng người dùng chẳng bao giờ
     * kéo tới. Để id trong ô rồi vẽ ở đây thì {@code JTable} chỉ gọi cho các
     * dòng đang nhìn thấy, và {@link #iconOf} có sẵn bộ nhớ đệm nên kéo qua
     * kéo lại không đọc đĩa lần nữa.</p>
     */
    static DefaultTableCellRenderer veIcon() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, "", sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                setIcon(v instanceof Number
                        ? iconOf(((Number) v).intValue()) : null);
                return this;
            }
        };
    }

    // =====================================================================
    //  Đọc dữ liệu
    // =====================================================================

    private enum Slot {
        /** Đồ đang mặc trên người. */
        BODY,
        /** Hành trang mang theo. */
        BAG,
        /** Rương cất ở nhà. */
        BOX
    }

    private List<Item> listOf(Player p, Slot s) {
        if (p == null || p.inventory == null) {
            return null;
        }
        switch (s) {
            case BODY:
                return p.inventory.itemsBody;
            case BAG:
                return p.inventory.itemsBag;
            case BOX:
                return p.inventory.itemsBox;
            default:
                return null;
        }
    }

    /**
     * Nhịp làm mới: dựng lại danh sách rồi nạp lại chi tiết người đang chọn.
     *
     * <p>Người online lấy từ {@code Client.gI()} (đối tượng sống), người offline
     * đọc từ CSDL. <b>Online xếp lên trên</b>, trong mỗi nhóm sắp theo tên.</p>
     *
     * <p>Giữ dòng đang chọn bằng cách so id chứ không so chỉ số dòng — danh sách
     * đổi liên tục khi có người vào/ra.</p>
     */
    private void refresh() {
        List<Row> list = new ArrayList<>();
        Set<Long> onlineIds = new HashSet<>();

        // --- người đang online: lấy từ bộ nhớ ---
        // Phải kiểm tra isRunning trước khi chạm Client.gI(): Client là singleton
        // lazy, constructor của nó bật thread chạy `while (isRunning)`. Gọi sớm
        // là thread đó thoát ngay và không bao giờ được tạo lại.
        if (ServerManager.isRunning) {
            try {
                for (Player p : Client.gI().getPlayersSnapshot()) {
                    if (p == null || p.name == null) {
                        continue;
                    }
                    Row r = new Row();
                    r.id = p.id;
                    r.name = p.name;
                    r.gender = p.gender;
                    r.power = p.nPoint != null ? p.nPoint.power : 0;
                    r.map = (p.zone != null && p.zone.map != null) ? p.zone.map.mapName : "-";
                    // Người đang online: lấy tổng nạp từ phiên vì họ có thể vừa
                    // nạp trong game, còn bản trong CSDL chỉ cập nhật lúc lưu.
                    r.tongNap = p.getSession() != null ? p.getSession().tongnap : 0;
                    r.online = true;
                    r.live = p;
                    list.add(r);
                    onlineIds.add(p.id);
                }
            } catch (Exception ex) {
                lblStatus.setForeground(WARN_RED);
                lblStatus.setText("Không lấy được danh sách online: " + ex.getMessage());
            }
        }

        // Bang nguoi choi doc mot lan roi dem lai; dung lam nguon QUYEN cho
        // ca nguoi online. Phien dang nhap khong giu co is_admin, chi co
        // isFounder/isQuanTriVien — lay tu phien thi cung mot tai khoan se hien
        // quyen khac nhau tuy dang online hay khong.
        long nowQ = System.currentTimeMillis();
        if (offlineCache == null || nowQ - offlineCacheAt > OFFLINE_CACHE_MS) {
            offlineCache = loadOffline();
            offlineCacheAt = nowQ;
        }
        java.util.Map<Long, String> quyenTheoId = new HashMap<>();
        for (Row r : offlineCache) {
            quyenTheoId.put(r.id, r.quyen);
        }
        for (Row r : list) {
            r.quyen = quyenTheoId.getOrDefault(r.id, "—");
        }

        // --- người offline: đọc CSDL, CÓ ĐỆM ---
        // Không truy vấn mỗi nhịp: danh sách nhân vật gần như không đổi, mà
        // nhịp là 1 lần/giây. Truy vấn cả bảng player mỗi giây vừa phí vừa
        // làm luồng giao diện khựng, và cú khựng đó từng làm mất lựa chọn dòng.
        if (!chkOnlyOnline.isSelected()) {
            for (Row r : offlineCache) {
                if (!onlineIds.contains(r.id)) {
                    list.add(r);
                }
            }
        }

        // Online trước, rồi theo tên.
        Collections.sort(list, (a, b) -> {
            if (a.online != b.online) {
                return a.online ? -1 : 1;
            }
            return a.name.compareToIgnoreCase(b.name);
        });

        String key = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        List<Row> shown = new ArrayList<>();
        for (Row r : list) {
            if (key.isEmpty() || r.name.toLowerCase().contains(key)
                    || String.valueOf(r.id).contains(key)) {
                shown.add(r);
            }
        }

        long keepId = selectedRow != null ? selectedRow.id : -1;
        rows = shown;

        // Chỉ dựng lại bảng khi DANH SÁCH THỰC SỰ ĐỔI.
        //
        // Trước đây nhịp nào cũng setRowCount(0) rồi thêm lại từng dòng. Việc đó
        // xoá lựa chọn, và nếu nhịp rơi đúng vào giữa lúc người dùng NHẤN và THẢ
        // chuột thì: cú nhấn chọn dòng mới (valueIsAdjusting=true, bị bỏ qua),
        // bảng dựng lại và khôi phục dòng CŨ, rồi cú thả không tạo ra thay đổi
        // nào nữa -> click bị nuốt, panel không đổi người.
        //
        // Cập nhật tại chỗ thì lựa chọn không bao giờ bị xoá, nên click luôn ăn.
        // Kèm lợi ích: bảng không nháy và không nhảy về đầu khi đang cuộn.
        boolean sameList = shown.size() == playerModel.getRowCount();
        if (sameList) {
            for (int i = 0; i < shown.size(); i++) {
                if (!String.valueOf(shown.get(i).id).equals(
                        String.valueOf(playerModel.getValueAt(i, 1)))) {
                    sameList = false;
                    break;
                }
            }
        }

        syncing = true;
        try {
            if (sameList) {
                // Chỉ ba cột này đổi theo thời gian; id/tên/hệ thì không.
                for (int i = 0; i < shown.size(); i++) {
                    Row r = shown.get(i);
                    setCell(i, 0, r.online ? "●" : "○");
                    setCell(i, 4, r.quyen);
                    setCell(i, 5, fmt(r.power));
                    setCell(i, 6, fmt(r.tongNap));
                    setCell(i, 7, r.online ? r.map : "(offline)");
                }
                int cur = playerTable.getSelectedRow();
                if (cur >= 0 && cur < shown.size()) {
                    selectedRow = shown.get(cur);
                    selected = shown.get(cur).live;
                }
            } else {
                playerModel.setRowCount(0);
                int sel = -1;
                for (int i = 0; i < shown.size(); i++) {
                    Row r = shown.get(i);
                    playerModel.addRow(new Object[]{
                        r.online ? "●" : "○",
                        r.id,
                        r.name,
                        gender(r.gender),
                        r.quyen,
                        fmt(r.power),
                        fmt(r.tongNap),
                        r.online ? r.map : "(offline)"
                    });
                    if (r.id == keepId) {
                        sel = i;
                    }
                }
                if (sel >= 0) {
                    playerTable.getSelectionModel().setSelectionInterval(sel, sel);
                    selectedRow = shown.get(sel);
                    selected = shown.get(sel).live;
                } else if (keepId != -1) {
                    selectedRow = null;
                    selected = null;
                    lblWho.setText("Người chơi không còn trong danh sách");
                    clearDetail();
                    setEditable(false);
                }
            }
        } finally {
            syncing = false;
        }

        int on = 0;
        for (Row r : shown) {
            if (r.online) {
                on++;
            }
        }
        lblCount.setText("  Online: " + on + " / Hiển thị: " + shown.size());

        if (lblStatus.getText().startsWith("Đang chờ server")) {
            lblStatus.setForeground(OK_GREEN);
            lblStatus.setText("Server đang chạy — cập nhật mỗi " + REFRESH_MS + "ms.");
        } else if (!ServerManager.isRunning) {
            lblStatus.setForeground(GREY);
            lblStatus.setText("Đang chờ server khởi động — danh sách dưới đây đọc từ CSDL.");
        }

        if (selectedRow != null) {
            loadStats(false);
            loadItems(false);
        }
    }

    /**
     * Đọc danh sách nhân vật từ CSDL (những người không online).
     *
     * <p>Chỉ lấy 4 cột cần cho danh sách. {@code data_point} là mảng JSON, phần
     * tử thứ <b>1</b> là sức mạnh — thứ tự này định nghĩa trong
     * {@code GodGK.login()}, đổi ở đó thì phải đổi cả ở đây.</p>
     */
    private List<Row> loadOffline() {
        List<Row> out = new ArrayList<>();
        for (PlayerDAO.AdminRow a : PlayerDAO.listAllForAdmin()) {
            Row r = new Row();
            r.id = a.id;
            r.name = a.name;
            r.gender = a.gender;
            r.power = a.power;
            r.tongNap = a.tongNap;
            r.quyen = a.quyen != null ? a.quyen : "—";
            r.online = false;
            out.add(r);
        }
        return out;
    }

    /** Chỉ ghi vào ô khi giá trị thật sự khác — tránh vẽ lại bảng vô ích. */
    private void setCell(int row, int col, Object value) {
        Object cur = playerModel.getValueAt(row, col);
        if (cur == null || !cur.equals(value)) {
            playerModel.setValueAt(value, row, col);
        }
    }

    /**
     * Định dạng số theo kiểu Việt Nam: dấu chấm ngăn nghìn, ví dụ {@code 1.000}.
     *
     * <p>Ô nhập cũng hiển thị dạng này. {@link #num} khi đọc ngược sẽ bỏ hết dấu
     * chấm và dấu phẩy, nên gõ {@code 1.000}, {@code 1,000} hay {@code 1000}
     * đều ra cùng một số.</p>
     */
    static String fmt(long v) {
        return NUM_FMT.format(v);
    }

    private static String gender(byte g) {
        switch (g) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namek";
            case 2:
                return "Xayda";
            default:
                return "?";
        }
    }

    /** Người dùng bấm chọn dòng khác: nạp lại toàn bộ, bỏ mọi dấu bẩn. */
    private void onSelectPlayer() {
        int r = playerTable.getSelectedRow();
        if (r < 0 || r >= rows.size()) {
            return;
        }
        selectedRow = rows.get(r);
        selected = selectedRow.live;
        dirty.clear();
        loadStats(true);
        loadItems(true);
        loadHistory();
        loadVe();
        loadChiTiet();
        setEditable(selectedRow.online);
    }

    /** Bật/tắt khả năng sửa. Người offline chỉ xem được. */
    private void setEditable(boolean on) {
        for (JTextField f : allFields) {
            f.setEditable(on);
            f.setBackground(on ? Color.WHITE : new Color(245, 245, 245));
        }
        // allFields co ca o cua de tu. Vong tren vua mo het theo trang thai
        // online; o de tu con doi hoi CO de tu that, nen khoa lai o day.
        khoaODeTu(on && selected != null && selected.Detu != null
                && selected.Detu.nPoint != null);
        for (JButton b : editButtons) {
            b.setEnabled(on);
            // setEnabled(false) không tự làm mờ nút vì nền là màu tự đặt.
            // Không đổi màu thì nút trông vẫn bấm được -> gây hiểu nhầm.
            Object bgOn = b.getClientProperty("bgOn");
            b.setBackground(on && bgOn instanceof Color ? (Color) bgOn : DISABLED_BG);
        }
    }

    /**
     * Nạp chỉ số vào các ô nhập.
     *
     * @param force {@code true} thì ghi đè cả ô người dùng đang gõ (dùng khi đổi
     *              người chơi hoặc bấm "Đọc lại"); {@code false} thì bỏ qua các
     *              ô đã đánh dấu bẩn — đây là thứ giữ cho số đang gõ không bị
     *              nhịp làm mới xoá mất.
     */
    private void loadStats(boolean force) {
        Row row = selectedRow;
        if (row == null) {
            clearDetail();
            return;
        }

        writing = true;
        try {
            if (row.online && row.live != null && row.live.nPoint != null) {
                Player p = row.live;
                NPoint n = p.nPoint;
                String acc = p.getSession() != null
                        ? (" | acc=" + p.getSession().userId + " | ip=" + p.getSession().ipAddress)
                        : " | (không có phiên)";
                lblWho.setText("● " + p.name + "   [id=" + p.id + "]" + acc);
                lblWho.setForeground(OK_GREEN);

                set(fHpg, n.hpg, force);
                set(fMpg, n.mpg, force);
                set(fDameg, n.dameg, force);
                set(fDefg, n.defg, force);
                set(fCritg, n.critg, force);
                set(fPower, n.power, force);
                set(fTiemNang, n.tiemNang, force);
                set(fLimitPower, n.limitPower, force);
                set(fHp, n.hp, force);
                set(fMp, n.mp, force);
                set(fStamina, n.stamina, force);
                set(fMaxStamina, n.maxStamina, force);
                if (p.inventory != null) {
                    set(fGold, p.inventory.gold, force);
                    set(fGem, p.inventory.gem, force);
                    set(fRuby, p.inventory.ruby, force);
                }
                loadMoney(row.id);
                loadDetu(p, force);
                vHpMax.setText(fmt(n.hpMax));
                vMpMax.setText(fmt(n.mpMax));
                vDame.setText(fmt(n.dame));
                vDef.setText(fmt(n.def));
                vCrit.setText(fmt(n.crit));
            } else {
                lblWho.setText("○ " + row.name + "   [id=" + row.id
                        + "]   — ĐANG OFFLINE, chỉ xem được");
                lblWho.setForeground(GREY);
                // Chi doc CSDL khi DOI nguoi chon (force), khong doc lai moi
                // nhip: du lieu offline khong tu thay doi, ma moi lan doc la
                // mot truy van - nhan voi 1 lan/giay la lang phi vo ich.
                if (force) {
                    loadOfflineDetail(row);
                    loadMoney(row.id);
                }
                // Nguoi offline khong co doi tuong Detu trong bo nho de sua.
                loadDetu(null, force);
            }
        } finally {
            writing = false;
        }
    }

    /**
     * Nạp chỉ số của người offline từ cột {@code data_point}.
     *
     * <p>Thứ tự phần tử trong mảng JSON lấy đúng theo {@code GodGK.login()}:
     * 0=giới hạn sức mạnh, 1=sức mạnh, 2=tiềm năng, 3/4=thể lực,
     * 5/6/7/8/9 = hp/mp/dame/def/crit gốc.</p>
     */
    private void loadOfflineDetail(Row row) {
        clearDetail();
        try {
            String[] cols = PlayerDAO.loadPointAndInventoryForAdmin(row.id);
            if (cols != null) {
                JSONArray dp = (JSONArray) JSONValue.parse(cols[0]);
                fLimitPower.setText(fmtRaw(dp.get(0)));
                fPower.setText(fmtRaw(dp.get(1)));
                fTiemNang.setText(fmtRaw(dp.get(2)));
                fStamina.setText(fmtRaw(dp.get(3)));
                fMaxStamina.setText(fmtRaw(dp.get(4)));
                fHpg.setText(fmtRaw(dp.get(5)));
                fMpg.setText(fmtRaw(dp.get(6)));
                fDameg.setText(fmtRaw(dp.get(7)));
                fDefg.setText(fmtRaw(dp.get(8)));
                fCritg.setText(fmtRaw(dp.get(9)));
                fHp.setText(fmtRaw(dp.get(11)));
                fMp.setText(fmtRaw(dp.get(12)));

                JSONArray inv = (JSONArray) JSONValue.parse(cols[1]);
                if (inv != null && inv.size() >= 3) {
                    fGold.setText(fmtRaw(inv.get(0)));
                    fGem.setText(fmtRaw(inv.get(1)));
                    fRuby.setText(fmtRaw(inv.get(2)));
                }
            }
        } catch (Exception ex) {
            lblStatus.setForeground(WARN_RED);
            lblStatus.setText("Không đọc được chỉ số offline: " + ex.getMessage());
        }
    }

    /** Định dạng một giá trị JSON; không phải số thì trả nguyên văn. */
    private static String fmtRaw(Object v) {
        try {
            return fmt(Long.parseLong(String.valueOf(v)));
        } catch (NumberFormatException ex) {
            return String.valueOf(v);
        }
    }

    /** Ghi giá trị vào ô nhập, trừ khi người dùng đã gõ vào ô đó. */
    private void set(JTextField f, long v, boolean force) {
        if (force || !dirty.contains(f)) {
            f.setText(fmt(v));
        }
    }

    private void clearDetail() {
        // Lưu rồi trả lại thay vì gán cứng false: hàm này được gọi TỪ BÊN TRONG
        // loadStats() vốn cũng đang bật cờ. Gán cứng false ở finally sẽ tắt cờ
        // giữa chừng, khiến những lần setText sau đó của loadStats bị hiểu nhầm
        // là người dùng gõ.
        boolean prev = writing;
        writing = true;
        try {
            for (JTextField f : allFields) {
                f.setText("");
            }
            loadDetu(null, true);
        } finally {
            writing = prev;
        }
        for (JLabel l : new JLabel[]{vHpMax, vMpMax, vDame, vDef, vCrit}) {
            l.setText("");
        }
        bodyModel.setRowCount(0);
        bagModel.setRowCount(0);
        boxModel.setRowCount(0);
    }

    /** Dựng lại cả ba bảng vật phẩm. */
    private void loadItems(boolean force) {
        Row row = selectedRow;
        if (row == null) {
            return;
        }
        if (row.online && row.live != null) {
            fillItems(bodyModel, listOf(row.live, Slot.BODY));
            fillItems(bagModel, listOf(row.live, Slot.BAG));
            fillItems(boxModel, listOf(row.live, Slot.BOX));
        } else if (force) {
            // Xem ghi chu o loadStats: du lieu offline tinh, doc mot lan la du.
            loadOfflineItems(row);
        }
    }

    /**
     * Đổ danh sách vật phẩm vào bảng.
     *
     * <p><b>Ô trống vẫn hiển thị</b> (dòng id {@code -}) chứ không bị bỏ qua:
     * chỉ số ô chính là địa chỉ để đặt item, ẩn đi thì không đặt vào ô cụ thể được.</p>
     */
    /**
     * Đổ danh sách vật phẩm vào bảng.
     *
     * <p><b>Ô trống vẫn hiển thị</b> (dòng id {@code -}) chứ không bị bỏ qua:
     * chỉ số ô chính là địa chỉ để đặt item, ẩn đi thì không đặt vào ô cụ thể được.</p>
     */
    private void fillItems(DefaultTableModel model, List<Item> items) {
        if (items == null) {
            model.setRowCount(0);
            return;
        }

        // Cập nhật TẠI CHỖ khi số ô không đổi — giống lý do ở bảng người chơi:
        // setRowCount(0) rồi thêm lại sẽ XOÁ LỰA CHỌN, mà nhịp làm mới chạy mỗi
        // giây. Hệ quả là vừa chọn một ô xong là mất, không bấm "Sửa item" kịp.
        boolean same = model.getRowCount() == items.size();
        if (!same) {
            model.setRowCount(0);
        }

        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            Object[] row;
            if (it != null && it.isNotNullItem()) {
                row = new Object[]{
                    i, iconOf(it.template.iconID), it.template.id, it.template.name,
                    typeName(it.template.type), fmt(it.quantity), optionText(it)
                };
            } else {
                row = new Object[]{i, null, "-", "(ô trống)", "", "", ""};
            }
            if (same) {
                for (int c = 1; c < row.length; c++) {
                    Object cur = model.getValueAt(i, c);
                    if (cur == null ? row[c] != null : !cur.equals(row[c])) {
                        model.setValueAt(row[c], i, c);
                    }
                }
            } else {
                model.addRow(row);
            }
        }
    }

    /**
     * Đọc vật phẩm của người offline từ ba cột JSON.
     *
     * <p>Định dạng lấy đúng theo {@code GodGK.login()}: mỗi phần tử là một chuỗi
     * JSON {@code [tempId, soLuong, "<mảng option>", thoiDiemTao]}.</p>
     */
    private void loadOfflineItems(Row row) {
        try {
            String[] cols = PlayerDAO.loadItemsForAdmin(row.id);
            if (cols != null) {
                fillJsonItems(bodyModel, cols[0]);
                fillJsonItems(bagModel, cols[1]);
                fillJsonItems(boxModel, cols[2]);
            }
        } catch (Exception ex) {
            lblStatus.setForeground(WARN_RED);
            lblStatus.setText("Không đọc được vật phẩm offline: " + ex.getMessage());
        }
    }

    private void fillJsonItems(DefaultTableModel model, String raw) {
        model.setRowCount(0);
        if (raw == null || raw.isEmpty()) {
            return;
        }
        JSONArray arr = (JSONArray) JSONValue.parse(raw);
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.size(); i++) {
            try {
                JSONArray d = (JSONArray) JSONValue.parse(String.valueOf(arr.get(i)));
                short tempId = Short.parseShort(String.valueOf(d.get(0)));
                if (tempId == -1) {
                    model.addRow(new Object[]{i, null, "-", "(ô trống)", "", "", ""});
                    continue;
                }
                ItemTemplate t = ItemService.gI().getTemplate(tempId);
                String name = t != null ? t.name : "(chưa nạp dữ liệu item)";
                ImageIcon ic = t != null ? iconOf(t.iconID) : null;

                StringBuilder opt = new StringBuilder();
                JSONArray options = (JSONArray) JSONValue.parse(
                        String.valueOf(d.get(2)).replaceAll("\"", ""));
                if (options != null) {
                    for (Object o : options) {
                        JSONArray pair = (JSONArray) JSONValue.parse(String.valueOf(o));
                        if (opt.length() > 0) {
                            opt.append(" | ");
                        }
                        opt.append(pair.get(0)).append('=').append(pair.get(1));
                    }
                }
                model.addRow(new Object[]{i, ic, tempId, name,
                    t != null ? typeName(t.type) : "", fmtRaw(d.get(1)), opt.toString()});
            } catch (Exception ignored) {
                model.addRow(new Object[]{i, null, "?", "(không đọc được)", "", "", ""});
            }
        }
    }

    /** Chuỗi mô tả option của item, dạng {@code id=param(tên)}. */
    private static String optionText(Item it) {
        if (it.itemOptions == null || it.itemOptions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ItemOption io : it.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(io.optionTemplate.id).append('=').append(io.param)
              .append('(').append(io.getOptionString()).append(')');
        }
        return sb.toString();
    }

    // =====================================================================
    //  Áp dụng thay đổi
    // =====================================================================

    /**
     * Khuôn chung cho mọi thay đổi: sửa bộ nhớ, đẩy gói tin, ghi CSDL.
     *
     * <p>Thiếu bước đẩy gói tin thì server đã đổi mà người chơi vẫn thấy số cũ;
     * thiếu bước ghi CSDL thì thay đổi bay mất nếu server sập trước lần tự lưu.</p>
     *
     * <p>Ghi CSDL chạy ở {@link SwingWorker} vì {@code updatePlayer()} ghi hàng
     * chục cột JSON — gọi thẳng trên luồng giao diện sẽ làm cửa sổ đứng.</p>
     */
    private void applyAndPush(Player p, String what, Runnable change) {
        if (p == null) {
            warn("Chỉ sửa được người đang online.");
            return;
        }
        try {
            change.run();
        } catch (Exception ex) {
            Logger.logException(PlayerManagerPanel.class, ex, "Lỗi khi " + what);
            warn("Lỗi khi " + what + ": " + ex.getMessage());
            return;
        }

        dirty.clear();          // đã ghi vào server -> ô nhập không còn "bẩn"
        lblStatus.setForeground(GREY);
        lblStatus.setText("Đã áp dụng: " + what + " cho " + p.name + " — đang lưu CSDL...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    PlayerDAO.updatePlayer(p);
                    return true;
                } catch (Exception ex) {
                    Logger.logException(PlayerManagerPanel.class, ex, "Lỗi lưu CSDL");
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
                lblStatus.setForeground(ok ? OK_GREEN : WARN_RED);
                lblStatus.setText(ok
                        ? "Đã áp dụng và lưu CSDL: " + what + " — " + p.name
                        : "Đã áp dụng vào bộ nhớ nhưng LƯU CSDL THẤT BẠI: " + what);
                loadItems(true);
                loadStats(true);
            }
        }.execute();
    }

    /**
     * Ghi các chỉ số trong ô nhập vào nhân vật.
     *
     * <p>Thứ tự quan trọng: gán chỉ số gốc → {@code calPoint()} tính lại phần dẫn
     * xuất → <i>rồi mới</i> kẹp {@code hp}/{@code mp} theo giới hạn mới. Làm
     * ngược lại sẽ kẹp theo giới hạn cũ.</p>
     */
    private void applyStats() {
        final Player p = selected;
        applyAndPush(p, "cập nhật chỉ số", () -> {
            NPoint n = p.nPoint;
            n.hpg = num(fHpg, n.hpg);
            n.mpg = num(fMpg, n.mpg);
            n.dameg = num(fDameg, n.dameg);
            n.defg = (int) num(fDefg, n.defg);
            n.critg = (int) num(fCritg, n.critg);
            n.power = num(fPower, n.power);
            n.tiemNang = num(fTiemNang, n.tiemNang);
            n.limitPower = (byte) num(fLimitPower, n.limitPower);
            n.stamina = (int) num(fStamina, n.stamina);
            n.maxStamina = (int) num(fMaxStamina, n.maxStamina);

            if (p.inventory != null) {
                p.inventory.gold = num(fGold, p.inventory.gold);
                p.inventory.gem = (int) num(fGem, p.inventory.gem);
                p.inventory.ruby = (int) num(fRuby, p.inventory.ruby);
            }

            n.calPoint();
            n.setHp(Math.min(num(fHp, n.hp), n.hpMax));
            n.setMp(Math.min(num(fMp, n.mp), n.mpMax));

            Service.gI().point(p);
            Service.gI().sendMoney(p);
            p.sendInfoHPMP();
        });
    }

    private void doFullHeal() {
        final Player p = selected;
        applyAndPush(p, "hồi đầy HP/KI", () -> {
            p.nPoint.calPoint();
            p.nPoint.setHp(p.nPoint.hpMax);
            p.nPoint.setMp(p.nPoint.mpMax);
            Service.gI().point(p);
            p.sendInfoHPMP();
        });
    }

    /**
     * Hồi hết thời gian chờ của <b>mọi</b> chiêu người chơi đang chọn.
     *
     * <p>Thời gian chờ nằm ở {@code Skill.lastTimeUseThisSkill} của từng chiêu —
     * đặt về 0 là chiêu nào cũng dùng được ngay. Cả hiệu ứng gồng và biến hình
     * cũng gỡ luôn, vì chúng có chốt "đang có hiệu lực thì không bấm lại".</p>
     */
    /**
     * Tính lại trần máu của người đang chọn và bày <b>từng bước</b> ra một bảng:
     * gốc, % trên đồ, thẻ, bùa, bổ huyết, huýt sáo, set, phần đệ tử khi hợp
     * thể… Bước nào không làm đổi con số thì không có dòng.
     */
    private void doChiTietHp() {
        final Player p = selected;
        if (p == null || p.nPoint == null) {
            warn("Chỉ xem được người đang online.");
            return;
        }
        java.util.List<String[]> ds;
        try {
            ds = p.nPoint.giaiThichHp();
        } catch (Exception ex) {
            Logger.logException(PlayerManagerPanel.class, ex, "Lỗi tính chi tiết HP");
            warn("Không tính được: " + ex.getMessage());
            return;
        }
        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(
                new Object[]{"#", "Bước", "Thay đổi", "HP tối đa sau bước"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        int i = 0;
        for (String[] r : ds) {
            long doi = Long.parseLong(r[1]);
            m.addRow(new Object[]{++i, r[0], (doi >= 0 ? "+" : "-") + fmt(Math.abs(doi)),
                fmt(Long.parseLong(r[2]))});
        }
        javax.swing.JTable t = new javax.swing.JTable(m);
        t.setRowHeight(22);
        int[] w = {30, 430, 140, 150};
        for (int c = 0; c < w.length; c++) {
            t.getColumnModel().getColumn(c).setPreferredWidth(w[c]);
        }
        javax.swing.JScrollPane sc = new javax.swing.JScrollPane(t);
        sc.setPreferredSize(new java.awt.Dimension(780, 440));
        JOptionPane.showMessageDialog(this, sc, "Chi tiết HP — " + p.name
                + " — HP tối đa " + fmt(p.nPoint.hpMax), JOptionPane.PLAIN_MESSAGE);
    }

    private void doResetChieu() {
        final Player p = selected;
        applyAndPush(p, "hồi hết chiêu", () -> {
            int n = 0;
            if (p.playerSkill != null && p.playerSkill.skills != null) {
                for (nro.entity.skill.Skill sk : p.playerSkill.skills) {
                    if (sk != null) {
                        sk.lastTimeUseThisSkill = 0;
                        sk.lastTimeUseThisSkillbot = 0;
                        n++;
                    }
                }
            }
            // Client GIU DONG HO HOI CHIEU RIENG cua no. Sua moc thoi gian ben
            // may chu thi gate canUseSkillWithCooldown cho qua, nhung icon chieu
            // tren may nguoi choi van dang dem — bam vao khong ra chieu. Phai
            // gui message -94 (releaseCooldownSkill) de client xoa dong ho.
            Service.gI().releaseCooldownSkill(p);
            // skillSelect la MOT THAM CHIEU RIENG, khong nhat thiet nam trong
            // playerSkill.skills — SkillUtil.setSkill thay ca doi tuong trong
            // danh sach moi lan hoc / nang cap. Ma canUseSkillWithCooldown lai
            // doc dung skillSelect.lastTimeUseThisSkill. Bo dong nay thi chieu
            // dang chon (chinh chieu nguoi choi vua bam) van con hoi chieu.
            if (p.playerSkill != null && p.playerSkill.skillSelect != null) {
                p.playerSkill.skillSelect.lastTimeUseThisSkill = 0;
                p.playerSkill.skillSelect.lastTimeUseThisSkillbot = 0;
            }
            // Chieu cua de tu / phan than cung phai hoi.
            for (Player khac : new Player[]{p.Detu, p.PhanThan}) {
                if (khac == null || khac.playerSkill == null) {
                    continue;
                }
                if (khac.playerSkill.skills != null) {
                    for (nro.entity.skill.Skill sk : khac.playerSkill.skills) {
                        if (sk != null) {
                            sk.lastTimeUseThisSkill = 0;
                            sk.lastTimeUseThisSkillbot = 0;
                        }
                    }
                }
                if (khac.playerSkill.skillSelect != null) {
                    khac.playerSkill.skillSelect.lastTimeUseThisSkill = 0;
                    khac.playerSkill.skillSelect.lastTimeUseThisSkillbot = 0;
                }
            }
            Service.gI().point(p);
            Service.gI().sendThongBao(p, "Đã hồi " + n + " chiêu.");
        });
    }

    private void doSaveNow() {
        applyAndPush(selected, "lưu trạng thái hiện tại", () -> {
        });
    }

    private void doNotify() {
        Player p = selected;
        if (p == null) {
            warn("Chỉ gửi được cho người đang online.");
            return;
        }
        String text = JOptionPane.showInputDialog(this, "Nội dung gửi tới " + p.name + ":");
        if (text != null && !text.trim().isEmpty()) {
            Service.gI().sendThongBaoFromAdmin(p, text);
            lblStatus.setForeground(OK_GREEN);
            lblStatus.setText("Đã gửi thông báo tới " + p.name);
        }
    }

    private void doKick() {
        Player p = selected;
        if (p == null || p.getSession() == null) {
            warn("Người chơi này không có phiên kết nối (offline hoặc là bot).");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Kick " + p.name + " ra khỏi game?\nDữ liệu sẽ được lưu trước khi ngắt.",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            Client.gI().kickSession(p.getSession());
            selected = null;
            selectedRow = null;
            refresh();
            lblStatus.setForeground(OK_GREEN);
            lblStatus.setText("Đã kick " + p.name);
        }
    }

    // =====================================================================
    //  Thao tác vật phẩm
    // =====================================================================

    /** Đẩy đúng kho vừa đổi xuống client. Sai kho là người chơi không thấy thay đổi. */
    private void pushItems(Player p, Slot slot) {
        switch (slot) {
            case BODY:
                InventoryService.gI().sendItemBody(p);
                // Đổi trang bị là đổi chỉ số -> phải tính lại và gửi.
                p.nPoint.calPoint();
                Service.gI().point(p);
                Service.gI().player(p);
                break;
            case BAG:
                InventoryService.gI().sendItemBag(p);
                break;
            case BOX:
                InventoryService.gI().sendItemBox(p);
                break;
            default:
                break;
        }
    }

    /**
     * Xoá vật phẩm ở <b>tất cả</b> các ô đang chọn.
     *
     * <p>Bảng cho chọn nhiều dòng (Ctrl/Shift) nên phải quét cả mảng chỉ số chứ
     * không chỉ {@code getSelectedRow()}. Ô trống trong vùng chọn được bỏ qua
     * lặng lẽ — chọn cả một vùng dài mà bắt bẻ từng ô trống thì rất phiền.</p>
     */
    private void deleteItem(Slot slot, JTable table) {
        final Player p = selected;
        if (p == null) {
            warn("Chỉ sửa được người đang online.");
            return;
        }
        final List<Item> list = listOf(p, slot);
        if (list == null) {
            warn("Không đọc được kho vật phẩm.");
            return;
        }

        // Lọc ra những ô THẬT SỰ có đồ; ô trống và ô ngoài phạm vi bỏ qua.
        final List<Integer> rows = new ArrayList<>();
        StringBuilder ten = new StringBuilder();
        for (int r : table.getSelectedRows()) {
            if (r < 0 || r >= list.size()) {
                continue;
            }
            Item cur = list.get(r);
            if (cur == null || !cur.isNotNullItem()) {
                continue;
            }
            rows.add(r);
            if (rows.size() <= 8) {
                ten.append("\n  • ô ").append(r).append(": ").append(cur.template.name);
            }
        }
        if (rows.isEmpty()) {
            warn(table.getSelectedRowCount() == 0
                    ? "Chưa chọn ô nào trong bảng."
                    : "Các ô đang chọn đều trống.");
            return;
        }
        if (rows.size() > 8) {
            ten.append("\n  … và ").append(rows.size() - 8).append(" ô nữa");
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá " + rows.size() + " vật phẩm của " + p.name + "?" + ten,
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        applyAndPush(p, "xoá " + rows.size() + " item", () -> {
            for (int r : rows) {
                // Đặt Item rỗng chứ không remove: số ô phải giữ nguyên, nếu không
                // mọi ô phía sau bị dồn lên và client hiển thị lệch hết.
                list.set(r, new Item());
            }
            // Đẩy MỘT lần sau khi xoá hết, không đẩy từng ô: mỗi lần đẩy là một
            // gói tin gửi cho client, xoá 20 ô mà gửi 20 gói là thừa.
            pushItems(p, slot);
        });
    }

    /**
     * Hộp thoại thêm/sửa item.
     *
     * @param edit {@code true} là sửa item ở ô đang chọn, {@code false} là đặt item mới
     */
    private void itemDialog(Slot slot, JTable table, boolean edit) {
        final Player p = selected;
        if (p == null) {
            warn("Chỉ sửa được người đang online.");
            return;
        }
        final List<Item> list = listOf(p, slot);
        if (list == null) {
            warn("Không đọc được kho vật phẩm.");
            return;
        }
        int row = table.getSelectedRow();
        if (edit) {
            if (row < 0 || row >= list.size()) {
                warn("Chưa chọn ô nào để sửa.");
                return;
            }
            Item cur0 = list.get(row);
            if (cur0 == null || !cur0.isNotNullItem()) {
                warn("Ô này trống — dùng \"Thêm / Đặt item\" thay vì \"Sửa\".");
                return;
            }
        }

        final Item cur = (edit && row >= 0) ? list.get(row) : null;
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                (edit ? "Sửa item — " : "Đặt item vào ô — ") + p.name, true);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTextField fSlot = new JTextField(String.valueOf(row >= 0 ? row : firstEmpty(list)));
        fSlot.setEnabled(!edit);
        JTextField fId = new JTextField(cur != null ? String.valueOf(cur.template.id) : "");
        JTextField fQty = new JTextField(cur != null ? String.valueOf(cur.quantity) : "1");
        // Bang chi so thay cho o go tay "id=param, id=param". Cot 0 co o chon
        // nen khong phai nho id nao ung voi chi so nao.
        DefaultTableModel mOpt = new DefaultTableModel(
                new Object[]{"Chỉ số", "Giá trị"}, 0);
        JTable tOpt = new JTable(mOpt);
        tOpt.setRowHeight(24);
        tOpt.getColumnModel().getColumn(0).setPreferredWidth(230);
        tOpt.getColumnModel().getColumn(1).setPreferredWidth(90);
        OptionPicker.install(tOpt, 0);
        if (cur != null && cur.itemOptions != null) {
            for (ItemOption io : cur.itemOptions) {
                if (io != null && io.optionTemplate != null) {
                    mOpt.addRow(new Object[]{String.valueOf(io.optionTemplate.id),
                        String.valueOf(io.param)});
                }
            }
        }
        JLabel lblName = new JLabel(cur != null ? cur.template.name : "—");
        lblName.setForeground(ACCENT);
        JLabel lblIcon = new JLabel();
        if (cur != null) {
            lblIcon.setIcon(iconOf(cur.template.iconID));
        }

        form.add(new JLabel("Ô số (0-based):"));
        form.add(fSlot);
        form.add(new JLabel("ID vật phẩm:"));
        form.add(fId);
        form.add(new JLabel("Tên:"));
        form.add(lblName);
        form.add(new JLabel("Ảnh:"));
        form.add(lblIcon);
        form.add(new JLabel("Số lượng:"));
        form.add(fQty);
        // Bang chi so KHONG duoc nam trong form: form dung GridLayout, moi o
        // cao bang nhau, nen bang bi bop con dung mot dong — nhin thay moi hai
        // nut Them/Xoa, tuong nhu mat bang. Dat rieng ra giua hop thoai.
        JPanel optWrap = new JPanel(new BorderLayout(0, 4));
        optWrap.setBorder(javax.swing.BorderFactory.createTitledBorder("Chỉ số (option)"));
        optWrap.add(ServerGuiUtils.cuon(tOpt), BorderLayout.CENTER);
        JPanel optBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        optBtn.add(button("Thêm", OK_GREEN, e -> mOpt.addRow(new Object[]{"0", "0"})));
        optBtn.add(button("Xoá", WARN_RED, e -> {
            int k = tOpt.getSelectedRow();
            if (k >= 0) {
                mOpt.removeRow(k);
            }
        }));
        optBtn.add(OptionPicker.oKhoaChoBang(mOpt, 2));
        optBtn.add(new JLabel("  (nháy đúp ô \"Chỉ số\" để chọn từ danh sách)"));
        optWrap.add(optBtn, BorderLayout.SOUTH);

        // Gõ id tới đâu hiện tên và ảnh tới đó -> tránh đặt nhầm item.
        fId.addCaretListener(e -> {
            ItemTemplate t = templateOf(fId.getText());
            lblName.setText(t != null ? t.name : "(không có id này)");
            lblName.setForeground(t != null ? ACCENT : WARN_RED);
            lblIcon.setIcon(t != null ? iconOf(t.iconID) : null);
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton btnPick = button("Tra cứu ID...", GREY, e -> pickTemplate(dlg, fId));
        JButton btnOk = button(edit ? "Lưu thay đổi" : "Đặt vào ô", OK_GREEN, e -> {
            ItemTemplate t = templateOf(fId.getText());
            if (t == null) {
                JOptionPane.showMessageDialog(dlg, "ID vật phẩm không tồn tại.");
                return;
            }
            String vichSao = rawUnsafe(t);
            if (vichSao != null) {
                JOptionPane.showMessageDialog(dlg,
                        "Không đặt \"" + t.name + "\" vào ô được: " + vichSao
                        + ".\nĐặt thẳng vào ô sẽ làm hỏng giao diện client."
                        + "\nDùng tab \"Quản Lý Vật Phẩm\" để phát đúng cách.",
                        "Vật phẩm không nằm trong ô", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int slotIdx;
            int qty;
            try {
                slotIdx = Integer.parseInt(fSlot.getText().trim());
                qty = Integer.parseInt(fQty.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Ô số và số lượng phải là số nguyên.");
                return;
            }
            if (slotIdx < 0 || slotIdx >= list.size()) {
                JOptionPane.showMessageDialog(dlg,
                        "Ô phải nằm trong khoảng 0.." + (list.size() - 1));
                return;
            }
            List<ItemOption> opts;
            try {
                // Doc thang tu bang. Neu nguoi dung dang go do trong mot o thi
                // dung o dang sua lai truoc, neu khong gia tri vua go se mat.
                if (tOpt.isEditing()) {
                    tOpt.getCellEditor().stopCellEditing();
                }
                opts = docBangChiSo(mOpt);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Chỉ số sai: " + ex.getMessage());
                return;
            }
            dlg.dispose();
            applyAndPush(p, (edit ? "sửa" : "đặt") + " item ô " + slotIdx, () -> {
                Item it = ItemService.gI().createNewItem(t.id, Math.max(1, qty));
                it.itemOptions = opts;
                it.info = it.getInfo();
                it.content = it.getContent();
                list.set(slotIdx, it);
                gopThoiVang(list, slotIdx);
                pushItems(p, slot);
            });
        });
        JButton btnCancel = button("Huỷ", GREY, e -> dlg.dispose());
        south.add(btnPick);
        south.add(btnCancel);
        south.add(btnOk);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(form, BorderLayout.NORTH);
        wrap.add(optWrap, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.setSize(640, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // =====================================================================
    //  Tên loại vật phẩm
    // =====================================================================

    /**
     * Nhãn loại kèm mã số, ví dụ {@code "Áo (0)"}.
     *
     * <p>Cột {@code type} trong CSDL chỉ là một con số, mã nguồn cũng không có
     * hằng số nào đặt tên cho nó — chỉ có những câu {@code if (type == 0)} rải
     * rác. Tên nay giữ ở bảng {@code loai_vat_pham} và sửa được trong panel;
     * xem {@link nro.repository.dao.LoaiVatPhamDAO}.</p>
     *
     * <p>Loại chưa đặt tên hiện thành {@code "Loại <số>"} chứ không ném lỗi —
     * thà hiện số còn hơn để trống.</p>
     */
    static String typeName(int type) {
        String n = nro.repository.dao.LoaiVatPhamDAO.ten(type);
        return n != null ? n + " (" + type + ")" : "Loại " + type;
    }

    /** Mọi mã loại đang có tên, để đổ vào ô chọn loại. */
    static java.util.List<Integer> cacLoai() {
        return new java.util.ArrayList<>(
                nro.repository.dao.LoaiVatPhamDAO.tatCa().keySet());
    }

    /** Hệ của một vật phẩm: 3 nghĩa là mọi hệ đều dùng được. */
    static String itemGender(byte g) {
        switch (g) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namek";
            case 2:
                return "Xayda";
            default:
                return "Chung";
        }
    }

    /**
     * Vì sao vật phẩm này <b>không</b> được đặt thẳng vào một ô, hay {@code null}
     * nếu đặt được.
     *
     * <p>Đây là nguyên nhân làm hỏng giao diện client: có những mẫu vật phẩm
     * <i>không bao giờ nằm trong ô</i>. Game xử lý chúng ở
     * {@code InventoryService.addItemBag()} rồi trả về ngay:</p>
     * <ul>
     *   <li>type 9 / 10 / 34 — Vàng, Ngọc, Hồng ngọc: cộng vào ví, không chiếm ô</li>
     *   <li>type 13 — Bùa: cộng thời gian hiệu lực, không chiếm ô</li>
     *   <li>id 517 / 1627 / 518 — mở rộng thêm một ô hành trang / rương</li>
     *   <li>id 988 — nâng giới hạn vàng</li>
     *   <li>id 718 — vé tặng ngọc</li>
     *   <li>Ngọc Rồng đen và Ngọc Rồng Namek — có bộ xử lý riêng</li>
     * </ul>
     *
     * <p>Nhét thẳng một trong số đó vào ô thì gói tin túi đồ gửi cho client chứa
     * một vật phẩm mà client không biết vẽ — nhân vật biến mất, không di chuyển
     * được, giao diện túi/rương loạn.</p>
     */
    static String rawUnsafe(ItemTemplate t) {
        if (t == null) {
            return "không có mẫu vật phẩm này";
        }
        switch (t.type) {
            case 9:
                return "đây là Vàng — cộng thẳng vào ví, không nằm trong ô";
            case 10:
                return "đây là Ngọc — cộng thẳng vào ví, không nằm trong ô";
            case 34:
                return "đây là Hồng ngọc — cộng thẳng vào ví, không nằm trong ô";
            case 13:
                return "đây là Bùa — game cộng thời gian hiệu lực, không nằm trong ô";
            default:
                break;
        }
        switch (t.id) {
            case 517:
            case 1627:
                return "đây là vật phẩm mở rộng hành trang, không nằm trong ô";
            case 518:
                return "đây là vật phẩm mở rộng rương, không nằm trong ô";
            case 988:
                return "đây là vật phẩm nâng giới hạn vàng, không nằm trong ô";
            case 718:
                return "đây là vé tặng ngọc, game xử lý riêng";
            default:
                break;
        }
        try {
            if (nro.service.item.ItemMapService.gI().isBlackBall(t.id)
                    || nro.service.item.ItemMapService.gI().isNamecBall(t.id)
                    || nro.service.item.ItemMapService.gI().isNamecBallStone(t.id)) {
                return "đây là Ngọc Rồng, game có bộ xử lý riêng";
            }
        } catch (Exception ignored) {
            // Bảng ngọc rồng chưa nạp thì bỏ qua, các nhánh trên đã chặn phần lớn.
        }
        return null;
    }

    /**
     * Đọc bảng chỉ số thành danh sách option.
     *
     * <p>Ô chọn ghi vào bảng đúng con số, nhưng người dùng vẫn gõ tay được, nên
     * vẫn phải cắt lấy phần số ở đầu chuỗi.</p>
     */
    private static List<ItemOption> docBangChiSo(DefaultTableModel m) {
        List<ItemOption> out = new ArrayList<>();
        for (int i = 0; i < m.getRowCount(); i++) {
            String sId = OptionPicker.laySo(String.valueOf(m.getValueAt(i, 0)));
            String sVal = String.valueOf(m.getValueAt(i, 1)).trim();
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
            out.add(new ItemOption(id, param));
        }
        return out;
    }

    /** Chỉ số ô trống đầu tiên, hoặc 0 nếu kho đã đầy. */
    /**
     * Gộp các ô Thỏi vàng trùng nhau về một ô.
     *
     * <p><b>Vì sao cần.</b> Thanh tiền dưới cùng của client chỉ đọc <i>một</i> ô
     * Thỏi vàng, nên có hai ô là con số hiện ra sai. Trong game chuyện này không
     * xảy ra: {@code InventoryService.addItemBag} đã gộp sẵn. Nhưng "Đặt item vào
     * ô" của panel ghi thẳng vào ô — cố ý, vì đó là chức năng sửa từng ô — nên nó
     * tạo được ô thứ hai.</p>
     *
     * <p>Chỉ gộp <b>Thỏi vàng</b> (id 457) và chỉ gộp những ô <b>cùng trạng thái
     * khoá</b> — đúng luật {@code addItemBag} đang dùng. Không gộp bừa mọi vật
     * phẩm: nhiều món không chồng được, gộp lại là hỏng túi.</p>
     */
    private static void gopThoiVang(List<Item> list, int slotVua) {
        if (list == null || slotVua < 0 || slotVua >= list.size()) {
            return;
        }
        Item vua = list.get(slotVua);
        if (vua == null || !vua.isNotNullItem() || vua.template.id != ID_THOI_VANG) {
            return;
        }
        boolean khoaVua = coKhoa(vua);
        for (int i = 0; i < list.size(); i++) {
            if (i == slotVua) {
                continue;
            }
            Item it = list.get(i);
            if (it == null || !it.isNotNullItem() || it.template.id != ID_THOI_VANG) {
                continue;
            }
            if (coKhoa(it) != khoaVua) {
                continue;
            }
            vua.quantity += it.quantity;
            // O rong chu KHONG remove: bo han mot phan tu la moi o phia sau don
            // len mot bac va client hien lech het.
            list.set(i, new Item());
        }
    }

    /** Id mẫu Thỏi vàng. */
    private static final int ID_THOI_VANG = 457;

    /** {@code true} nếu vật phẩm mang chỉ số "Không thể giao dịch". */
    private static boolean coKhoa(Item it) {
        if (it.itemOptions == null) {
            return false;
        }
        for (ItemOption io : it.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == 30) {
                return true;
            }
        }
        return false;
    }

    private static int firstEmpty(List<Item> list) {
        for (int i = 0; i < list.size(); i++) {
            Item it = list.get(i);
            if (it == null || !it.isNotNullItem()) {
                return i;
            }
        }
        return 0;
    }

    private static String rawOptions(Item it) {
        StringBuilder sb = new StringBuilder();
        if (it.itemOptions != null) {
            for (ItemOption io : it.itemOptions) {
                if (io == null || io.optionTemplate == null) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(io.optionTemplate.id).append('=').append(io.param);
            }
        }
        return sb.toString();
    }

    /** Phân tích chuỗi option người dùng gõ. Id không tồn tại thì báo lỗi ngay. */
    private static List<ItemOption> parseOptions(String raw) {
        List<ItemOption> out = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return out;
        }
        for (String part : raw.split(",")) {
            String s = part.trim();
            if (s.isEmpty()) {
                continue;
            }
            int eq = s.indexOf('=');
            if (eq <= 0) {
                throw new IllegalArgumentException("thiếu dấu = ở \"" + s + "\"");
            }
            int id = Integer.parseInt(s.substring(0, eq).trim());
            int param = Integer.parseInt(s.substring(eq + 1).trim());
            ItemOptionTemplate t = ItemService.gI().getItemOptionTemplate(id);
            if (t == null) {
                throw new IllegalArgumentException("không có option id " + id);
            }
            out.add(new ItemOption(t, param));
        }
        return out;
    }

    static ItemTemplate templateOf(String idText) {
        try {
            return ItemService.gI().getTemplate(Integer.parseInt(idText.trim()));
        } catch (Exception ex) {
            return null;
        }
    }

    /** Hộp tra cứu id vật phẩm theo tên — kèm ảnh để chọn cho chắc. */
    private void pickTemplate(JDialog parent, JTextField target) {
        JDialog d = new JDialog(parent, "Tra cứu vật phẩm", true);
        JTextField q = new JTextField();
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Ảnh", "ID", "Tên", "Loại"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? ImageIcon.class : Object.class;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(32);
        t.getColumnModel().getColumn(0).setPreferredWidth(40);
        t.getColumnModel().getColumn(1).setPreferredWidth(50);
        t.getColumnModel().getColumn(2).setPreferredWidth(260);

        Runnable search = () -> {
            String key = q.getText().trim().toLowerCase();
            m.setRowCount(0);
            int n = 0;
            for (ItemTemplate tpl : Manager.ITEM_TEMPLATES) {
                if (tpl == null || tpl.name == null) {
                    continue;
                }
                if (key.isEmpty() || tpl.name.toLowerCase().contains(key)
                        || String.valueOf(tpl.id).equals(key)) {
                    m.addRow(new Object[]{iconOf(tpl.iconID), tpl.id, tpl.name, tpl.type});
                    // Chặn ở 300 dòng: bảng item template rất dài, đổ hết vào
                    // bảng kèm ảnh sẽ làm hộp thoại đứng vài giây.
                    if (++n >= 300) {
                        break;
                    }
                }
            }
        };
        q.addCaretListener(e -> search.run());
        search.run();

        JButton ok = button("Chọn", OK_GREEN, e -> {
            int r = t.getSelectedRow();
            if (r >= 0) {
                target.setText(String.valueOf(m.getValueAt(r, 1)));
                d.dispose();
            }
        });

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setBorder(new EmptyBorder(8, 8, 4, 8));
        top.add(new JLabel("Tìm theo tên hoặc id:"), BorderLayout.WEST);
        top.add(q, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.add(ok);

        d.setLayout(new BorderLayout());
        d.add(top, BorderLayout.NORTH);
        d.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);
        d.add(bot, BorderLayout.SOUTH);
        d.setSize(600, 480);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }


    // =====================================================================
    //  Nạp tiền & quy đổi ra vật phẩm
    // =====================================================================

    /** Ô nhập số tiền muốn nạp cho người chơi. */
    private final JTextField fTopUp = new JTextField(14);

    /** Bảng lịch sử nạp tiền của tài khoản đang chọn. */
    private final DefaultTableModel logModel = new DefaultTableModel(
            new Object[]{"Thời gian", "Nguồn", "Số tiền (VNĐ)", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable logTable = new JTable(logModel);

    /** Ô ghi chú cho lần nạp tay, sẽ nằm trong lịch sử. */
    private final JTextField fTopUpNote = new JTextField(22);

    /**
     * Tab nạp tiền.
     *
     * <p>Hai phần: nạp vào số dư, và xem lịch sử nạp.</p>
     *
     * <p>Lịch sử gộp từ hai bảng vì đó là hai đường nạp khác nhau:
     * {@code history_bank} (chuyển khoản, và cả các lần admin nạp tay ở đây) và
     * {@code napthe} (thẻ cào). Xem {@code AccountDAO.history()}.</p>
     */

    /** Bang hai cot Muc / Gia tri cho tab Chi tiet. */
    private final DefaultTableModel ctModel = new DefaultTableModel(
            new Object[]{"Mục", "Giá trị"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable ctTable = new JTable(ctModel);

    /**
     * Tab gom moi thong tin cua nhan vat vao mot cho.
     *
     * <p>Chi ĐỌC — khong sua gi o day. Cac tab khac da lo phan sua roi; tron them
     * o nhap vao day chi lam de bam nham.</p>
     */
    private JComponent buildChiTietTab() {
        ctTable.setRowHeight(22);
        ctTable.getColumnModel().getColumn(0).setPreferredWidth(210);
        ctTable.getColumnModel().getColumn(1).setPreferredWidth(420);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setOpaque(false);
        p.add(ServerGuiUtils.cuon(ctTable), BorderLayout.CENTER);
        return p;
    }

    /** Do lai bang Chi tiet theo nhan vat dang chon. */
    private void loadChiTiet() {
        ctModel.setRowCount(0);
        Player p = selected;
        if (p == null) {
            return;
        }
        try {
            muc("Tên nhân vật", p.name);
            muc("ID nhân vật", String.valueOf(p.id));
            muc("Hệ", gender(p.gender));
            muc("Đang online", p.isPl() ? "có" : "không");
            if (p.zone != null && p.zone.map != null) {
                muc("Bản đồ", p.zone.map.mapName + " [" + p.zone.map.mapId + "]"
                        + " — khu " + p.zone.zoneId);
            }
            muc("Bang hội", p.clan == null ? "(không có)" : p.clan.name);
            ctModel.addRow(new Object[]{"", ""});

            NPoint n = p.nPoint;
            if (n != null) {
                muc("Sức mạnh", fmt(n.power));
                muc("Tiềm năng", fmt(n.tiemNang));
                muc("HP", fmt(n.hp) + " / " + fmt(n.hpMax));
                muc("KI", fmt(n.mp) + " / " + fmt(n.mpMax));
                muc("Sức đánh", fmt(n.dame));
                muc("Giáp", fmt(n.def));
                muc("Chí mạng", n.crit + "%");
                muc("Hút HP (option 95)", n.tlHutHp + "%");
                muc("Tỉ lệ vàng thêm", n.tlGold + "%");
            }
            ctModel.addRow(new Object[]{"", ""});

            if (p.playerTask != null && p.playerTask.taskMain != null) {
                nro.entity.task.TaskMain t = p.playerTask.taskMain;
                muc("Nhiệm vụ chính", t.id + " — " + t.name);
                if (t.subTasks != null && t.index < t.subTasks.size()) {
                    muc("Bước hiện tại", (t.index + 1) + "/" + t.subTasks.size()
                            + " — " + t.subTasks.get(t.index).name);
                }
            } else {
                muc("Nhiệm vụ chính", "(chưa nạp)");
            }

            if (p.playerSkill != null && p.playerSkill.skills != null) {
                muc("Số kỹ năng đã học", String.valueOf(p.playerSkill.skills.size()));
            }
            if (p.inventory != null) {
                muc("Vàng", fmt(p.inventory.gold));
                muc("Ngọc", fmt(p.inventory.gem));
                muc("Hồng ngọc", fmt(p.inventory.ruby));
            }
        } catch (Exception ex) {
            // Nhan vat co the dang duoc nap do dang -> hien duoc bao nhieu thi hien.
            muc("(lỗi đọc)", String.valueOf(ex));
        }
    }

    private void muc(String ten, String giaTri) {
        ctModel.addRow(new Object[]{ten, giaTri});
    }

    private JComponent buildTopUpTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---------- nạp tiền ----------
        // Dùng GridBagLayout chứ không FlowLayout: FlowLayout xuống dòng khi
        // hẹp, nhưng nó nằm trong BorderLayout.NORTH vốn chỉ cấp đúng chiều cao
        // của MỘT hàng -> phần xuống dòng bị cắt mất (đúng lỗi form đang gặp).
        JPanel nap = new JPanel(new GridBagLayout());
        nap.setOpaque(false);
        nap.setBorder(titled("Nạp tiền vào số dư tài khoản"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        nap.add(new JLabel("Số tiền (VNĐ):"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        nap.add(fTopUp, g);
        g.fill = GridBagConstraints.NONE;

        g.gridx = 0;
        g.gridy = 1;
        nap.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        nap.add(fTopUpNote, g);
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;

        g.gridx = 2;
        g.gridy = 0;
        g.gridheight = 2;
        // KHONG dua vao editButtons: nap tien chi ghi bang `account`, khong can
        // doi tuong Player song. Nguoi choi offline van nap duoc binh thuong.
        nap.add(button("Nạp ngay", OK_GREEN, e -> doTopUp()), g);
        g.gridheight = 1;

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 3;
        JLabel hint = new JLabel("Cộng vào cả số dư lẫn tổng đã nạp, "
                + "và ghi một dòng vào lịch sử bên dưới.");
        hint.setForeground(GREY);
        nap.add(hint, g);
        g.gridwidth = 1;

        // ---------- lịch sử ----------
        logTable.setRowHeight(22);
        logTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        logTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(360);

        JPanel logWrap = new JPanel(new BorderLayout(0, 4));
        logWrap.setOpaque(false);
        logWrap.setBorder(titled("Lịch sử nạp tiền (chuyển khoản, admin nạp tay, thẻ cào)"));
        logWrap.add(ServerGuiUtils.cuon(logTable), BorderLayout.CENTER);

        JPanel logBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        logBtns.setOpaque(false);
        logBtns.add(button("Tải lại lịch sử", GREY, e -> loadHistory()));
        logWrap.add(logBtns, BorderLayout.SOUTH);

        JPanel bac = new JPanel(new BorderLayout(0, 6));
        bac.setOpaque(false);
        bac.add(nap, BorderLayout.NORTH);
        bac.add(buildVeBox(), BorderLayout.CENTER);

        root.add(bac, BorderLayout.NORTH);
        root.add(logWrap, BorderLayout.CENTER);
        return root;
    }

    /** Trạng thái vé hiện tại của tài khoản đang chọn. */
    private final JLabel lblVe = new JLabel("—");

    /** Số ngày admin muốn cấp; để trống là dùng số ngày chuẩn trong cấu hình. */
    private final JTextField fVeNgay = new JTextField(5);

    /**
     * Ô quản lý vé tuần / vé tháng.
     *
     * <p>Giá và số ngày lấy từ {@link ConfigDAO} — đúng bộ số mà người chơi tự
     * mua ở NPC Ông Gôhan, nên admin cấp tay và người chơi tự mua luôn khớp
     * nhau. Sửa giá ở tab "Cấu Hình" là cả hai đường đổi theo.</p>
     */
    private JComponent buildVeBox() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(titled("Vé tuần / vé tháng"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Đang có:"), g);
        g.gridx = 1;
        g.gridwidth = 3;
        lblVe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblVe.setForeground(ACCENT);
        p.add(lblVe, g);
        g.gridwidth = 1;

        g.gridx = 0;
        g.gridy = 1;
        p.add(new JLabel("Số ngày:"), g);
        g.gridx = 1;
        fVeNgay.setToolTipText("Để trống là dùng số ngày chuẩn trong tab Cấu Hình");
        p.add(fVeNgay, g);
        g.gridx = 2;
        JLabel mienPhi = new JLabel("(admin cấp — KHÔNG trừ tiền người chơi)");
        mienPhi.setForeground(GREY);
        p.add(mienPhi, g);
        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 4;
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btn.setOpaque(false);
        btn.add(button("Sửa quyền", ACCENT, e -> doEditQuyen()));
        btn.add(button("Sửa tiền", ACCENT, e -> doEditTien()));
        btn.add(button("Cấp vé tuần", OK_GREEN, e -> doGrantVe(false)));
        btn.add(button("Cấp vé tháng", ACCENT, e -> doGrantVe(true)));
        btn.add(button("Gỡ vé", WARN_RED, e -> doClearVe()));
        btn.add(button("Tải lại", GREY, e -> loadVe()));
        p.add(btn, g);
        return p;
    }

    /** Đọc và hiện trạng thái vé của tài khoản đang chọn. */
    private void loadVe() {
        int accId = currentAccountId();
        if (accId < 0) {
            lblVe.setText("—");
            return;
        }
        AccountDAO.Ve v = AccountDAO.loadVe(accId);
        lblVe.setForeground(v.conHan() ? OK_GREEN : GREY);
        lblVe.setText(v.moTa()
                + "   |   giá vé tuần " + fmt(ConfigDAO.num(ConfigDAO.VE_TUAN_GIA))
                + " VND / " + ConfigDAO.num(ConfigDAO.VE_TUAN_NGAY) + " ngày"
                + "   •   vé tháng " + fmt(ConfigDAO.num(ConfigDAO.VE_THANG_GIA))
                + " VND / " + ConfigDAO.num(ConfigDAO.VE_THANG_NGAY) + " ngày");
    }

    /**
     * Cấp vé cho tài khoản đang chọn.
     *
     * <p><b>Không trừ tiền.</b> Admin cấp tay là cấp thẳng — số dư của người
     * chơi giữ nguyên, và không ghi dòng nào vào lịch sử nạp vì không có giao
     * dịch tiền nào. Đường trừ tiền chỉ nằm ở NPC Ông Gôhan, nơi người chơi tự
     * bỏ tiền ra mua.</p>
     *
     * <p>Giá vé trong tab Cấu Hình vẫn dùng ở đây, nhưng chỉ để suy ra
     * <b>số ngày chuẩn</b> khi admin để trống ô số ngày.</p>
     */
    private void doGrantVe(boolean thang) {
        int accId = currentAccountId();
        if (accId < 0) {
            warn("Chưa chọn người chơi.");
            return;
        }
        long ngayChuan = ConfigDAO.num(thang ? ConfigDAO.VE_THANG_NGAY : ConfigDAO.VE_TUAN_NGAY);

        int ngay;
        String gonhap = fVeNgay.getText().trim();
        if (gonhap.isEmpty()) {
            ngay = (int) ngayChuan;
        } else {
            try {
                ngay = Integer.parseInt(gonhap.replace(".", "").replace(",", ""));
            } catch (NumberFormatException ex) {
                warn("Số ngày phải là số nguyên.");
                return;
            }
        }
        if (ngay <= 0) {
            warn("Số ngày phải lớn hơn 0.");
            return;
        }

        AccountDAO.Ve dangCo = AccountDAO.loadVe(accId);
        StringBuilder hoi = new StringBuilder();
        hoi.append("Cấp ").append(thang ? "vé tháng" : "vé tuần")
                .append(" ").append(ngay).append(" ngày cho ")
                .append(selectedRow.name).append("?");
        if (dangCo.conHan()) {
            hoi.append("\n\nNgười này đang có: ").append(dangCo.moTa())
                    .append("\nCấp vé mới sẽ GHI ĐÈ vé đang có.");
        }
        hoi.append("\n\nKHÔNG trừ tiền — số dư của người chơi giữ nguyên.");

        int ok = JOptionPane.showConfirmDialog(this, hoi.toString(),
                "Xác nhận cấp vé", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        // Doc so du truoc va sau de CHUNG MINH tien khong bi dong toi.
        long duTruoc = AccountDAO.vndOf(accId);
        String loi = AccountDAO.grantVe(accId, thang, ngay);
        if (loi != null) {
            warn("Không cấp được vé: " + loi);
            return;
        }
        syncVeToSession(accId, thang, ngay);
        long duSau = AccountDAO.vndOf(accId);

        lblStatus.setForeground(OK_GREEN);
        lblStatus.setText("Đã cấp " + (thang ? "vé tháng " : "vé tuần ") + ngay + " ngày cho "
                + selectedRow.name + " — số dư giữ nguyên " + fmt(duSau)
                + (duTruoc == duSau ? " VND." : " VND (CẢNH BÁO: số dư đã đổi!)"));
        loadVe();
        loadMoney(selectedRow.id);
        loadHistory();
    }

    /**
     * Sửa quyền của tài khoản đang chọn.
     *
     * <h3>Ba mức, đặt trên ba cờ có sẵn</h3>
     *
     * <p>Bảng {@code account} không có cột "quyền" — chỉ có ba cờ riêng lẻ
     * {@code is_admin}, {@code isFounder}, {@code isQuanTriVien}, và trong dữ
     * liệu thật chúng chồng nhau lung tung. Panel gom lại thành ba mức dứt khoát
     * và <b>ghi cả ba cờ cùng lúc</b> nên không còn tổ hợp nửa vời:</p>
     *
     * <table border="1">
     *   <tr><th>Mức</th><th>is_admin</th><th>isFounder</th><th>isQuanTriVien</th></tr>
     *   <tr><td>Admin</td><td>1</td><td>1</td><td>0</td></tr>
     *   <tr><td>Colab</td><td>0</td><td>0</td><td>1</td></tr>
     *   <tr><td>User</td><td>0</td><td>0</td><td>0</td></tr>
     * </table>
     *
     * <p>Người đang online: {@code isFounder} và {@code isQuanTriVien} cập nhật
     * ngay trong phiên, còn {@code is_admin} thì phiên không giữ nên phải đăng
     * nhập lại mới đủ quyền.</p>
     */
    private void doEditQuyen() {
        int accId = currentAccountId();
        if (accId < 0) {
            warn("Chưa chọn người chơi.");
            return;
        }
        AccountDAO.Quyen q = AccountDAO.loadQuyen(accId);
        String dangCo = PlayerDAO.tenQuyen(q.admin, q.founder, q.quanTriVien);

        javax.swing.JComboBox<String> cb = new javax.swing.JComboBox<>(PlayerDAO.CAC_QUYEN);
        cb.setSelectedItem(dangCo);

        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));
        p.add(new JLabel("Quyền của " + selectedRow.name + " (tài khoản " + accId + "):"));
        p.add(cb);
        p.add(new JLabel("<html><span style='color:#777'>"
                + "<b>Admin</b> — toàn quyền (is_admin + isFounder)<br>"
                + "<b>Colab</b> — cộng tác viên (isQuanTriVien)<br>"
                + "<b>User</b> — người chơi thường<br><br>"
                + "Người đang online phải đăng nhập lại mới nhận đủ quyền: "
                + "phiên không giữ cờ <code>is_admin</code>.</span></html>"));

        int ok = JOptionPane.showConfirmDialog(this, p, "Sửa quyền",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String chon = String.valueOf(cb.getSelectedItem());
        AccountDAO.Quyen moi = new AccountDAO.Quyen();
        moi.admin = PlayerDAO.QUYEN_ADMIN.equals(chon);
        moi.founder = PlayerDAO.QUYEN_ADMIN.equals(chon);
        moi.quanTriVien = PlayerDAO.QUYEN_COLAB.equals(chon);

        if (!AccountDAO.saveQuyen(accId, moi)) {
            warn("Không lưu được quyền.");
            return;
        }
        if (selectedRow.online && selectedRow.live != null
                && selectedRow.live.getSession() != null) {
            selectedRow.live.getSession().isFounder = moi.founder;
            selectedRow.live.getSession().isQuanTriVien = moi.quanTriVien;
        }
        offlineCache = null;      // bảng có cột Quyền -> phải đọc lại
        offlineCacheAt = 0;
        refresh();
        lblStatus.setForeground(OK_GREEN);
        lblStatus.setText("Đã đổi quyền của " + selectedRow.name + ": "
                + dangCo + " → " + chon + ".");
    }

    /**
     * Sửa thẳng các con số tiền của tài khoản.
     *
     * <p>Khác nút "Nạp ngay": nạp là <b>cộng thêm</b> và ghi một dòng vào lịch sử
     * vì đó là giao dịch thật. Đây là <b>đặt đè</b> để sửa số liệu sai, nên không
     * ghi vào lịch sử — ghi vào đó sẽ làm thống kê doanh thu sai lệch.</p>
     */
    private void doEditTien() {
        int accId = currentAccountId();
        if (accId < 0) {
            warn("Chưa chọn người chơi.");
            return;
        }
        AccountDAO.Money m = AccountDAO.load(accId);
        if (m == null) {
            warn("Không đọc được tiền của tài khoản này.");
            return;
        }
        int tv = AccountDAO.thoiVangOf(accId);

        JTextField fVnd = new JTextField(String.valueOf(m.vnd), 14);
        JTextField fTongNap = new JTextField(String.valueOf(m.tongNap), 14);
        JTextField fCoin = new JTextField(String.valueOf(m.coin), 14);
        JTextField fTv = new JTextField(String.valueOf(tv), 14);

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));
        p.add(new JLabel("Số dư VNĐ:"));
        p.add(fVnd);
        p.add(new JLabel("Tổng đã nạp:"));
        p.add(fTongNap);
        p.add(new JLabel("Coin:"));
        p.add(fCoin);
        p.add(new JLabel("Thỏi vàng chưa nhận:"));
        p.add(fTv);

        int ok = JOptionPane.showConfirmDialog(this, p,
                "Sửa tiền — " + selectedRow.name, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        long vnd;
        long tongNap;
        long coin;
        long thoiVang;
        try {
            vnd = docSo(fVnd);
            tongNap = docSo(fTongNap);
            coin = docSo(fCoin);
            thoiVang = docSo(fTv);
        } catch (NumberFormatException ex) {
            warn("Các ô tiền phải là số nguyên.");
            return;
        }
        if (vnd < 0 || tongNap < 0 || coin < 0 || thoiVang < 0) {
            warn("Tiền không được âm.");
            return;
        }
        if (!AccountDAO.setTien(accId, vnd, tongNap, coin, thoiVang)) {
            warn("Không lưu được tiền.");
            return;
        }
        // BAT BUOC dong bo sang phien: PlayerDAO va cac duong khac ghi tien tu
        // phien, khong dong bo thi lan luu sau ghi de nguoc lai so cu.
        if (selectedRow.online && selectedRow.live != null
                && selectedRow.live.getSession() != null) {
            nro.net.session.MySession ss = selectedRow.live.getSession();
            ss.vnd = (int) Math.min(vnd, Integer.MAX_VALUE);
            ss.tongnap = (int) Math.min(tongNap, Integer.MAX_VALUE);
            ss.coin = (int) Math.min(coin, Integer.MAX_VALUE);
            ss.goldBar = (int) Math.min(thoiVang, Integer.MAX_VALUE);
            try {
                Service.gI().sendMoney(selectedRow.live);
            } catch (Exception ignored) {
                // Người chơi vừa thoát -> dữ liệu trong CSDL vẫn đúng.
            }
        }
        offlineCache = null;      // cột "Tổng nạp" trong bảng phải đọc lại
        offlineCacheAt = 0;
        refresh();
        loadMoney(selectedRow.id);
        lblStatus.setForeground(OK_GREEN);
        lblStatus.setText("Đã đặt lại tiền cho " + selectedRow.name
                + " — số dư " + fmt(vnd) + " VNĐ, tổng nạp " + fmt(tongNap)
                + ", coin " + fmt(coin) + ", thỏi vàng " + fmt(thoiVang) + ".");
    }

    /** Đọc số từ ô nhập, bỏ dấu ngăn nghìn. */
    private static long docSo(JTextField f) {
        return Long.parseLong(f.getText().trim().replace(".", "").replace(",", ""));
    }

    /** Gỡ vé của tài khoản đang chọn. Không hoàn tiền. */
    private void doClearVe() {
        int accId = currentAccountId();
        if (accId < 0) {
            warn("Chưa chọn người chơi.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Gỡ vé của " + selectedRow.name + "?\n\nKhông hoàn lại tiền.",
                "Xác nhận gỡ vé", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        if (!AccountDAO.clearVe(accId)) {
            warn("Không gỡ được vé.");
            return;
        }
        syncVeToSession(accId, false, 0);
        lblStatus.setForeground(GREY);
        lblStatus.setText("Đã gỡ vé của " + selectedRow.name + ".");
        loadVe();
    }

    /**
     * Đồng bộ vé và số dư sang phiên của người đang online.
     *
     * <p><b>Bắt buộc.</b> Client đọc trạng thái vé từ phiên, không đọc lại
     * CSDL. Chỉ sửa CSDL mà không sửa phiên thì người đang online vẫn thấy
     * trạng thái vé cũ cho tới lần đăng nhập sau.</p>
     *
     * <p>Không đụng tới {@code vnd}: admin cấp vé là miễn phí.</p>
     *
     * @param ngay {@code 0} nghĩa là gỡ vé
     */
    private void syncVeToSession(int accId, boolean thang, int ngay) {
        if (selectedRow == null || !selectedRow.online || selectedRow.live == null) {
            return;
        }
        nro.net.session.MySession ss = selectedRow.live.getSession();
        if (ss == null || ss.userId != accId) {
            return;
        }
        if (ngay <= 0) {
            ss.vetuan = 0;
            ss.vethang = 0;
            ss.vetuanExpire = 0;
            ss.vethangExpire = 0;
        } else {
            long hetHan = System.currentTimeMillis() + ngay * 86_400_000L;
            ss.vethang = thang ? 1 : 0;
            ss.vetuan = thang ? 0 : 1;
            ss.vethangExpire = thang ? hetHan : 0;
            ss.vetuanExpire = thang ? 0 : hetHan;
        }
        try {
            Service.gI().sendMoney(selectedRow.live);
        } catch (Exception ignored) {
            // Người chơi vừa thoát -> không gửi được, dữ liệu trong CSDL vẫn đúng.
        }
    }

    /** Đọc lịch sử nạp của tài khoản đang chọn. */
    private void loadHistory() {
        logModel.setRowCount(0);
        int accId = currentAccountId();
        if (accId < 0) {
            return;
        }
        for (AccountDAO.TopUpLog t : AccountDAO.history(accId, 200)) {
            logModel.addRow(new Object[]{t.time, t.source, fmt(t.amount), t.note});
        }
    }

    private static javax.swing.border.Border titled(String t) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)), t);
    }

    /**
     * Xoá toàn bộ nhân vật trong CSDL.
     *
     * <p><b>Ba lớp chặn</b>, vì việc này không hoàn tác được:</p>
     * <ol>
     *   <li>Từ chối thẳng khi còn người đang online — xoá nhân vật của người
     *       đang chơi thì phiên của họ vẫn giữ đối tượng trong bộ nhớ, và lần
     *       lưu tự động kế tiếp sẽ ghi họ trở lại. Vừa không sạch vừa khó hiểu.</li>
     *   <li>Hộp cảnh báo kèm <b>số nhân vật thật</b> sẽ bị xoá.</li>
     *   <li>Bắt gõ đúng chữ {@code XOA HET} — bấm nhầm nút thì không gõ được
     *       câu đó.</li>
     * </ol>
     *
     * <p>Chỉ xoá bảng {@code player}. Tài khoản, tiền nạp, lịch sử nạp giữ
     * nguyên — người chơi vẫn đăng nhập và tạo nhân vật mới được.</p>
     */
    /**
     * Đổi tên nhân vật đang chọn.
     *
     * <p><b>Vì sao có nút này.</b> Ô nhập tên ở màn tạo nhân vật của client lọc
     * ký tự trong <i>mã đã biên dịch</i> — không gõ được chữ hoa hay chữ có dấu,
     * và chỗ đó không sửa được từ máy chủ. Máy chủ thì chấp nhận cả ba (hoa, dấu,
     * dấu cách). Nút này đặt tên thẳng vào CSDL, nên đặt được tên mà ô nhập của
     * client không cho gõ.</p>
     *
     * <p><b>Người đang online bị kick.</b> Tên nhân vật là khoá tra cứu ở rất
     * nhiều chỗ đang chạy — bang, bạn bè, chợ. Đổi tên dưới chân một phiên đang
     * sống là để lại nửa hệ thống trỏ tên cũ. Kick rồi vào lại là sạch.</p>
     */

    /**
     * Dat nhiem vu chinh cua nhan vat dang chon — dung de BO QUA nhiem vu.
     *
     * <p>Chi lam duoc voi nhan vat DANG ONLINE: nhiem vu nam trong bo nho cua
     * phien choi, sua thang vao CSDL luc nguoi ta dang online se bi ghi de khi
     * he thong tu luu.</p>
     *
     * <p>Chon duoc ca buoc nho (sub task) vi mot nhiem vu chinh gom nhieu buoc;
     * dat ve buoc 0 la bat dau lai tu dau nhiem vu do.</p>
     */
    private void doDatNhiemVu() {
        Player p = selected;
        if (p == null) {
            warn("Chưa chọn nhân vật nào.");
            return;
        }
        if (p.playerTask == null) {
            warn("Nhân vật này chưa nạp dữ liệu nhiệm vụ.");
            return;
        }
        java.util.List<nro.entity.task.TaskMain> ds = nro.server.Manager.TASKS;
        if (ds == null || ds.isEmpty()) {
            warn("Máy chủ chưa nạp bảng nhiệm vụ.");
            return;
        }

        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(
                new Object[]{"ID", "Tên nhiệm vụ", "Số bước"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (nro.entity.task.TaskMain t : ds) {
            m.addRow(new Object[]{t.id, t.name,
                t.subTasks == null ? 0 : t.subTasks.size()});
        }
        JTable bang = new JTable(m);
        bang.setRowHeight(22);
        bang.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sc = ServerGuiUtils.cuon(bang);
        sc.setPreferredSize(new java.awt.Dimension(520, 320));

        JTextField fBuoc = new JTextField("0", 6);
        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        duoi.add(new JLabel("Bước nhỏ (0 là bước đầu):"));
        duoi.add(fBuoc);

        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.add(new JLabel("Nhiệm vụ hiện tại: "
                + (p.playerTask.taskMain == null ? "(không có)"
                        : p.playerTask.taskMain.id + " — " + p.playerTask.taskMain.name)),
                BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(duoi, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, wrap,
                "Đặt nhiệm vụ cho " + p.name,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int r = bang.getSelectedRow();
        if (r < 0) {
            warn("Chưa chọn nhiệm vụ nào.");
            return;
        }
        int idTask = Integer.parseInt(String.valueOf(m.getValueAt(r, 0)));
        int soBuoc = Integer.parseInt(String.valueOf(m.getValueAt(r, 2)));
        int buoc;
        try {
            buoc = Integer.parseInt(fBuoc.getText().trim());
        } catch (NumberFormatException ex) {
            warn("Bước nhỏ phải là số nguyên.");
            return;
        }
        if (buoc < 0 || (soBuoc > 0 && buoc >= soBuoc)) {
            warn("Nhiệm vụ này có " + soBuoc + " bước, phải nhập từ 0 đến " + (soBuoc - 1) + ".");
            return;
        }
        try {
            nro.entity.task.TaskMain moi =
                    nro.service.TaskService.gI().getTaskMainById(p, idTask);
            moi.index = buoc;
            p.playerTask.taskMain = moi;
            nro.service.TaskService.gI().sendTaskMain(p);
            nro.service.Service.gI().sendThongBao(p,
                    "Nhiệm vụ của bạn đã được đổi thành: " + moi.name);
        } catch (Exception ex) {
            warn("Không đặt được nhiệm vụ: " + ex.getMessage());
            return;
        }
        lblStatus.setText("Đã đặt nhiệm vụ " + idTask + " (bước " + buoc
                + ") cho " + p.name + ".");
    }

    private void doDoiTen() {
        Player p = selected;
        if (p == null) {
            warn("Chưa chọn nhân vật nào.");
            return;
        }
        String moi = JOptionPane.showInputDialog(this,
                "Tên mới cho \"" + p.name + "\":\n\n"
                + "Được dùng chữ hoa, chữ có dấu và dấu cách.\n"
                + "Dài 4–16 ký tự, không có hai dấu cách liền nhau.",
                p.name);
        if (moi == null) {
            return;
        }
        moi = moi.trim();
        if (moi.equals(p.name)) {
            return;
        }
        String loi = nro.server.Controller.kiemTraTenNhanVat(moi);
        if (loi != null) {
            warn(loi);
            return;
        }
        if (PlayerDAO.tonTaiTen(moi)) {
            warn("Đã có nhân vật tên \"" + moi + "\".");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Đổi \"" + p.name + "\" thành \"" + moi + "\"?\n\n"
                + "Nhân vật đang online sẽ bị kick để vào lại với tên mới.",
                "Xác nhận đổi tên", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        String cu = p.name;
        if (!PlayerDAO.doiTen(p.id, moi)) {
            warn("Không ghi được tên mới — xem log máy chủ.");
            return;
        }
        try {
            // Doi luon trong bo nho roi kick: neu chi doi CSDL, lan luu tu dong
            // ke tiep se ghi de ten CU nguoc len tren.
            p.name = moi;
            if (p.getSession() != null) {
                p.getSession().disconnect();
            }
        } catch (Exception ignored) {
            // Nguoi choi vua thoat -> CSDL da dung roi.
        }
        refresh();
        lblStatus.setForeground(OK_GREEN);
        lblStatus.setText("Đã đổi tên \"" + cu + "\" thành \"" + moi + "\".");
    }

    private void doDeleteAll() {
        int dangOnline = 0;
        if (ServerManager.isRunning) {
            try {
                dangOnline = Client.gI().getPlayersSnapshot().size();
            } catch (Exception ignored) {
                // Không đọc được thì coi như không có ai, các lớp chặn sau vẫn còn.
            }
        }
        if (dangOnline > 0) {
            JOptionPane.showMessageDialog(this,
                    "Đang có " + dangOnline + " người chơi online.\n\n"
                    + "Hãy kick hết rồi mới xoá: phiên của người đang chơi vẫn giữ "
                    + "nhân vật trong bộ nhớ, và lần lưu tự động kế tiếp sẽ ghi họ "
                    + "trở lại CSDL.",
                    "Chưa xoá được", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int tong = PlayerDAO.demNhanVat();
        if (tong < 0) {
            warn("Không đọc được số nhân vật — huỷ thao tác.");
            return;
        }
        if (tong == 0) {
            warn("Bảng nhân vật đang rỗng, không có gì để xoá.");
            return;
        }

        int b1 = JOptionPane.showConfirmDialog(this,
                "XOÁ TOÀN BỘ " + fmt(tong) + " NHÂN VẬT khỏi cơ sở dữ liệu?\n\n"
                + "KHÔNG HOÀN TÁC ĐƯỢC.\n\n"
                + "Tài khoản, tiền nạp và lịch sử nạp vẫn giữ nguyên —\n"
                + "chỉ nhân vật bị xoá.",
                "CẢNH BÁO", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (b1 != JOptionPane.YES_OPTION) {
            return;
        }

        String go = JOptionPane.showInputDialog(this,
                "Gõ chính xác  XOA HET  để xác nhận xoá " + fmt(tong) + " nhân vật:",
                "Xác nhận lần cuối", JOptionPane.WARNING_MESSAGE);
        if (go == null || !"XOA HET".equals(go.trim())) {
            warn("Đã huỷ — không xoá gì cả.");
            return;
        }

        int n = PlayerDAO.xoaHetNhanVat();
        if (n < 0) {
            lblStatus.setForeground(WARN_RED);
            lblStatus.setText("XOÁ THẤT BẠI — xem log máy chủ.");
            return;
        }
        // Bo dem di, neu khong danh sach van hien cac nhan vat vua xoa.
        offlineCache = null;
        offlineCacheAt = 0;
        selectedRow = null;
        selected = null;
        refresh();
        lblStatus.setForeground(WARN_RED);
        lblStatus.setText("Đã xoá " + fmt(n) + " nhân vật khỏi CSDL. Tài khoản giữ nguyên.");
    }

    /** Nạp và hiển thị phần tiền của tài khoản sở hữu nhân vật đang chọn. */
    private void loadMoney(long playerId) {
        int accId = currentAccountId();
        if (accId < 0) {
            vVnd.setText("—");
            vTongNap.setText("—");
            vTongNap2.setText("—");
            vCoin.setText("—");
            return;
        }
        AccountDAO.Money m = AccountDAO.load(accId);
        if (m == null) {
            vVnd.setText("(không đọc được)");
            return;
        }
        vVnd.setText(fmt(m.vnd));
        vTongNap.setText(fmt(m.tongNap));
        vTongNap2.setText(fmt(m.tongNap2));
        vCoin.setText(fmt(m.coin));
    }

    /**
     * Id tài khoản của người đang chọn.
     *
     * <p>Người online lấy thẳng từ phiên; người offline phải tra CSDL vì bảng
     * {@code player} và {@code account} là hai bảng khác nhau.</p>
     */
    private int currentAccountId() {
        if (selectedRow == null) {
            return -1;
        }
        if (selectedRow.online && selectedRow.live != null
                && selectedRow.live.getSession() != null) {
            return selectedRow.live.getSession().userId;
        }
        return AccountDAO.accountIdOfPlayer(selectedRow.id);
    }

    /** Cộng tiền nạp cho tài khoản của người đang chọn. */
    private void doTopUp() {
        if (selectedRow == null) {
            warn("Chưa chọn người chơi.");
            return;
        }
        long amount = parseNum(fTopUp.getText(), -1);
        if (amount <= 0) {
            warn("Số tiền phải lớn hơn 0.");
            return;
        }
        int accId = currentAccountId();
        if (accId < 0) {
            warn("Không tìm được tài khoản của nhân vật này.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Nạp " + fmt(amount) + " VNĐ cho " + selectedRow.name + "?",
                "Xác nhận nạp", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        if (!AccountDAO.addTopUp(accId, amount, fTopUpNote.getText())) {
            warn("Nạp thất bại — xem log để biết chi tiết.");
            return;
        }
        // Người đang online giữ số dư trong phiên; không cập nhật thì họ vẫn
        // thấy số cũ và lần lưu sau sẽ ghi đè ngược lại giá trị vừa nạp.
        if (selectedRow.online && selectedRow.live != null
                && selectedRow.live.getSession() != null) {
            selectedRow.live.getSession().vnd += amount;
            selectedRow.live.getSession().tongnap += amount;
            Service.gI().sendThongBaoFromAdmin(selectedRow.live,
                    "Bạn vừa được nạp " + fmt(amount) + " VNĐ.");
        }
        fTopUp.setText("");
        fTopUpNote.setText("");
        loadMoney(selectedRow.id);
        loadHistory();
        lblStatus.setForeground(OK_GREEN);
        lblStatus.setText("Đã nạp " + fmt(amount) + " VNĐ cho " + selectedRow.name);
    }

    /** Đọc số từ chuỗi người dùng gõ, bỏ dấu ngăn nghìn. */
    private static long parseNum(String text, long fallback) {
        try {
            String s = text == null ? "" : text.trim().replace(".", "").replace(",", "");
            return s.isEmpty() ? fallback : Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    // =====================================================================
    //  Lịch sử nhận vật phẩm
    // =====================================================================

    /** Giờ phút giây trước, rồi mới ngày tháng năm. */
    private static final java.text.SimpleDateFormat GIO_NGAY
            = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    /**
     * Mở cửa sổ nhật ký nhận vật phẩm của nhân vật đang chọn.
     *
     * <p><b>Đọc từ CSDL</b>, không đọc đối tượng trong bộ nhớ — nên người chơi
     * offline cũng tra được. Nhật ký do {@link LichSuVatPhamDAO} ghi lại ở mọi
     * đường nhận: quái rơi, nhặt dưới đất, mua ở NPC, ghép, giftcode, nhiệm vụ,
     * sự kiện, admin cấp.</p>
     *
     * <p>Cửa sổ <b>không chặn</b> (modeless) để còn vừa xem nhật ký vừa làm
     * việc khác trong panel, và mọi lần đọc CSDL đều chạy ở {@link SwingWorker}
     * — bảng này có thể lên hàng chục nghìn dòng, đọc thẳng trên luồng vẽ là
     * đơ cả panel.</p>
     */
    private void moLichSuVatPham() {
        if (selectedRow == null) {
            warn("Chọn một nhân vật trong danh sách trước đã.");
            return;
        }
        final long pid = selectedRow.id;
        final String pten = selectedRow.name;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Lịch sử nhận vật phẩm — " + pten + " (" + pid + ")",
                java.awt.Dialog.ModalityType.MODELESS);
        dlg.setSize(1180, 640);
        dlg.setLocationRelativeTo(this);

        // --- hàng lọc ---
        JTextField fVatPham = new JTextField(14);
        JTextField fPhuongThuc = new JTextField(12);
        JComboBox<String> cbNgay = new JComboBox<>(new String[]{
            "Tất cả", "Hôm nay", "3 ngày", "7 ngày", "30 ngày"});
        JComboBox<String> cbGioiHan = new JComboBox<>(new String[]{
            "200", "500", "1000", "5000", "20000"});
        cbGioiHan.setSelectedItem("1000");

        JLabel lblTong = new JLabel(" ");
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // --- bảng chi tiết ---
        String[] cot = {"Thời gian", "Vật phẩm", "ID", "SL", "Phương thức",
            "Nơi nhận", "Bản đồ", "Chỉ số", "Sức mạnh lúc đó", "Nguồn (kỹ thuật)"};
        DefaultTableModel mCT = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tCT = new JTable(mCT);
        tCT.setRowHeight(24);
        tCT.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] rong = {150, 220, 60, 70, 150, 100, 150, 300, 130, 240};
        for (int i = 0; i < rong.length; i++) {
            tCT.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        // --- bảng gộp theo vật phẩm ---
        DefaultTableModel mVP = new DefaultTableModel(
                new String[]{"Vật phẩm", "ID", "Tổng số lượng", "Số lần nhận", "Lần cuối"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tVP = new JTable(mVP);
        tVP.setRowHeight(24);

        // --- bảng gộp theo phương thức ---
        DefaultTableModel mPT = new DefaultTableModel(
                new String[]{"Phương thức nhận", "Số lần", "Tổng số lượng"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tPT = new JTable(mPT);
        tPT.setRowHeight(24);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Chi tiết từng lần", ServerGuiUtils.cuon(tCT));
        tabs.addTab("Gộp theo vật phẩm", ServerGuiUtils.cuon(tVP));
        tabs.addTab("Gộp theo phương thức", ServerGuiUtils.cuon(tPT));

        Runnable nap = () -> {
            LichSuVatPhamDAO.Loc f = new LichSuVatPhamDAO.Loc();
            f.nguoiId = pid;
            f.vatPham = fVatPham.getText();
            f.phuongThuc = fPhuongThuc.getText();
            f.tu = mocThoiGian((String) cbNgay.getSelectedItem());
            f.gioiHan = Integer.parseInt((String) cbGioiHan.getSelectedItem());
            lblTong.setForeground(GREY);
            lblTong.setText("Đang đọc...");

            new SwingWorker<Object[], Void>() {
                @Override
                protected Object[] doInBackground() {
                    return new Object[]{
                        LichSuVatPhamDAO.tim(f),
                        LichSuVatPhamDAO.gopTheoVatPham(pid, 500),
                        LichSuVatPhamDAO.gopTheoPhuongThuc(pid),
                        LichSuVatPhamDAO.demCua(pid)
                    };
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void done() {
                    try {
                        Object[] kq = get();
                        List<LichSuVatPhamDAO.Dong> ds
                                = (List<LichSuVatPhamDAO.Dong>) kq[0];
                        mCT.setRowCount(0);
                        for (LichSuVatPhamDAO.Dong d : ds) {
                            mCT.addRow(new Object[]{
                                d.luc == null ? "" : GIO_NGAY.format(d.luc),
                                d.itemTen, d.itemId, fmt(d.soLuong),
                                d.phuongThuc, d.noiNhan,
                                d.mapTen + (d.mapId >= 0 ? " (" + d.mapId + ")" : ""),
                                d.chiSo, fmt(d.sucManh), d.nguonKyThuat});
                        }

                        mVP.setRowCount(0);
                        for (LichSuVatPhamDAO.Gop g
                                : (List<LichSuVatPhamDAO.Gop>) kq[1]) {
                            mVP.addRow(new Object[]{g.itemTen, g.itemId,
                                fmt(g.tongSoLuong), fmt(g.soLan),
                                g.lanCuoi == null ? "" : GIO_NGAY.format(g.lanCuoi)});
                        }

                        mPT.setRowCount(0);
                        for (LichSuVatPhamDAO.GopPT g
                                : (List<LichSuVatPhamDAO.GopPT>) kq[2]) {
                            mPT.addRow(new Object[]{g.phuongThuc,
                                fmt(g.soLan), fmt(g.tongSoLuong)});
                        }

                        long tong = (Long) kq[3];
                        lblTong.setForeground(tong == 0 ? WARN_RED : OK_GREEN);
                        lblTong.setText(tong == 0
                                ? "Chưa có dòng nào — nhật ký chỉ ghi từ lúc bản này chạy trở đi."
                                : "Hiện " + fmt(ds.size()) + " / tổng " + fmt(tong)
                                + " dòng" + (LichSuVatPhamDAO.dangCho() > 0
                                        ? "  ·  còn " + LichSuVatPhamDAO.dangCho()
                                        + " dòng đang chờ ghi" : ""));
                    } catch (Exception ex) {
                        lblTong.setForeground(WARN_RED);
                        lblTong.setText("Lỗi đọc nhật ký: " + ex.getMessage());
                        Logger.logException(PlayerManagerPanel.class, ex,
                                "Lỗi đọc nhật ký nhận vật phẩm");
                    }
                }
            }.execute();
        };

        JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        loc.add(new JLabel("Vật phẩm:"));
        loc.add(fVatPham);
        loc.add(new JLabel("Phương thức:"));
        loc.add(fPhuongThuc);
        loc.add(new JLabel("Trong:"));
        loc.add(cbNgay);
        loc.add(new JLabel("Tối đa:"));
        loc.add(cbGioiHan);
        loc.add(button("Xem", ACCENT, e -> nap.run()));
        loc.add(button("Xuất CSV", OK_GREEN, e -> xuatCsv(dlg, mCT, pten, pid)));
        loc.add(button("Xoá nhật ký người này", WARN_RED, e -> {
            int c = JOptionPane.showConfirmDialog(dlg,
                    "Xoá toàn bộ nhật ký nhận vật phẩm của " + pten + "?\n"
                    + "Không lấy lại được.",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                int n = LichSuVatPhamDAO.xoaCua(pid);
                JOptionPane.showMessageDialog(dlg, "Đã xoá " + fmt(n) + " dòng.");
                nap.run();
            }
        }));
        // Gõ xong bấm Enter là xem luôn, khỏi với chuột.
        fVatPham.addActionListener(e -> nap.run());
        fPhuongThuc.addActionListener(e -> nap.run());
        cbNgay.addActionListener(e -> nap.run());
        cbGioiHan.addActionListener(e -> nap.run());

        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        duoi.add(lblTong);

        JPanel noi = new JPanel(new BorderLayout(0, 4));
        noi.setBorder(new EmptyBorder(6, 8, 6, 8));
        noi.add(loc, BorderLayout.NORTH);
        noi.add(tabs, BorderLayout.CENTER);
        noi.add(duoi, BorderLayout.SOUTH);
        dlg.setContentPane(noi);
        dlg.setVisible(true);
        nap.run();
    }

    /** Mốc "tính từ lúc nào" của ô chọn khoảng thời gian. */
    private static java.sql.Timestamp mocThoiGian(String chon) {
        if (chon == null || "Tất cả".equals(chon)) {
            return null;
        }
        long ngay;
        switch (chon) {
            case "Hôm nay":
                ngay = 1;
                break;
            case "3 ngày":
                ngay = 3;
                break;
            case "7 ngày":
                ngay = 7;
                break;
            case "30 ngày":
                ngay = 30;
                break;
            default:
                return null;
        }
        // Nhân bằng long: 25 ngày đã tràn int khi tính ra mili giây.
        return new java.sql.Timestamp(
                System.currentTimeMillis() - ngay * 24L * 60L * 60L * 1000L);
    }

    /**
     * Ghi bảng đang xem ra tệp CSV.
     *
     * <p>Có <b>BOM UTF-8</b> ở đầu tệp: thiếu nó thì Excel đọc CSV theo bảng mã
     * của Windows và mọi chữ tiếng Việt thành ký tự rác.</p>
     */
    private void xuatCsv(java.awt.Component cha, DefaultTableModel m,
            String tenNguoi, long idNguoi) {
        if (m.getRowCount() == 0) {
            JOptionPane.showMessageDialog(cha, "Bảng đang trống, không có gì để xuất.");
            return;
        }
        javax.swing.JFileChooser ch = new javax.swing.JFileChooser();
        ch.setSelectedFile(new File("lich-su-vat-pham-" + idNguoi + ".csv"));
        if (ch.showSaveDialog(cha) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = ch.getSelectedFile();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(f),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            pw.print('﻿');
            pw.println("Nhật ký nhận vật phẩm của " + oCsv(tenNguoi) + " (" + idNguoi + ")");
            StringBuilder h = new StringBuilder();
            for (int c = 0; c < m.getColumnCount(); c++) {
                if (c > 0) {
                    h.append(',');
                }
                h.append(oCsv(m.getColumnName(c)));
            }
            pw.println(h);
            for (int r = 0; r < m.getRowCount(); r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < m.getColumnCount(); c++) {
                    if (c > 0) {
                        sb.append(',');
                    }
                    Object v = m.getValueAt(r, c);
                    sb.append(oCsv(v == null ? "" : v.toString()));
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(cha, "Đã ghi " + fmt(m.getRowCount())
                    + " dòng vào\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(cha, "Không ghi được tệp: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Bọc một ô CSV: nhân đôi dấu nháy kép rồi bao ngoài bằng nháy kép. */
    private static String oCsv(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
    }

    // =====================================================================
    //  Tiện ích
    // =====================================================================

    /** Đọc số từ ô nhập; ô rỗng hoặc sai định dạng thì giữ nguyên giá trị cũ. */
    private static long num(JTextField f, long fallback) {
        try {
            String s = f.getText().trim().replace(".", "").replace(",", "");
            return s.isEmpty() ? fallback : Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private void warn(String msg) {
        lblStatus.setForeground(WARN_RED);
        lblStatus.setText(msg);
        JOptionPane.showMessageDialog(this, msg, "Chú ý", JOptionPane.WARNING_MESSAGE);
    }

    /** Dừng timer khi panel bị gỡ khỏi cửa sổ, tránh để thread chạy vô ích. */
    @Override
    public void removeNotify() {
        if (timer != null) {
            timer.stop();
        }
        super.removeNotify();
    }
}
