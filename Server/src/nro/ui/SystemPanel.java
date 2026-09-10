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
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import nro.repository.dao.BacGiamTnsmDAO;
import nro.repository.dao.ConfigDAO;
import nro.repository.dao.HeSoTnsmDAO;

/**
 * Tab <b>Cấu Hình Hệ Thống</b> — quy ước dùng chung và cấu hình boss.
 *
 * <h2>Quy ước</h2>
 *
 * <p>Các con số vốn viết cứng trong mã nguồn (tỉ lệ đổi thỏi vàng, giá vé, số
 * ngày hiệu lực) nay nằm ở bảng {@code panel_config}. Sửa xong bấm Lưu là
 * {@link ConfigDAO#reload()} chạy ngay — <b>không cần khởi động lại máy chủ</b>,
 * và cả hai đường (người chơi tự mua ở NPC, admin cấp tay ở panel) đều đọc cùng
 * một bộ số nên không thể lệch nhau.</p>

 */
public class SystemPanel extends JPanel {

    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color OK_GREEN = new Color(40, 160, 70);
    private static final Color WARN_RED = new Color(200, 60, 60);
    private static final Color GREY = new Color(120, 120, 120);

    private final JLabel lblStatus = new JLabel(" ");

    public SystemPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Cấu Hình Hệ Thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addTab("Quy ước", buildConfigTab());
        tabs.addTab("Lời chào", buildLoiChaoTab());
        tabs.addTab("Boss", buildBossTongHop());
        tabs.addTab("Đồ rơi từ quái", buildDoRoiTongHop());
        tabs.addTab("Tỉ lệ", buildTiLeTab());
        tabs.addTab("Tỉ lệ nâng sao", buildTiLeSaoTab());
        tabs.addTab("Set kích hoạt", buildSetTab());
        tabs.addTab("Tỉ lệ set kích hoạt", buildTiLeKichHoatTab());
        tabs.addTab("Nội tại", buildNoiTaiTab());
        tabs.addTab("Nhiệm vụ chính tuyến", buildNhiemVuChinhTab());
        tabs.addTab("Kỹ năng", buildKyNangTab());
        tabs.addTab("Hào quang", buildAuraTab());
        tabs.addTab("Danh hiệu", buildDanhHieuTab());
        tabs.addTab("Top máy đấm", buildTopMayDamTab());
        tabs.addTab("Vòng quay Thượng Đế", buildVongQuayTab());
        tabs.addTab("Điểm đến capsule", buildCapsuleTab());
        tabs.addTab("Bản đồ nhanh", buildMapNhanhTab());
        tabs.addTab("Sách tuyệt kỹ", buildSachTuyetKyTab());
        tabs.addTab("Phúc lợi", buildPhucLoiTab());
        tabs.addTab("Bông tai", buildBongTaiTab());
        tabs.addTab("Chân mệnh", buildChanMenhTab());
        tabs.addTab("Đệ tử", buildDeTuTab());
        tabs.addTab("Sự kiện", buildSuKienTab());
        tabs.addTab("Công thức đổi", buildCongThucTab());
        tabs.addTab("Kiểm tra dữ liệu", buildCheckTab());
        // Nap lai tab vua mo, dua theo TEN tab chu khong theo so thu tu: chen
        // them mot tab vao giua la moi con so dich di, va loi kieu do khong
        // bao gi ca, chi lang le nap nham bang.
        tabs.addChangeListener(e -> napTabTen(tabs.getTitleAt(
                Math.max(0, tabs.getSelectedIndex()))));
        add(tabs, BorderLayout.CENTER);

        lblStatus.setForeground(GREY);
        lblStatus.setBorder(new EmptyBorder(4, 2, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);

        // Ten va anh vat pham hien khap noi trong man nay: do roi cua boss, do
        // roi su kien, nguyen lieu cong thuc doi.
        LamMoi.nghe(this::veLaiTheoVatPham, LamMoi.VAT_PHAM, LamMoi.ANH, LamMoi.LOAI);
        LamMoi.nghe(LamMoi.BOSS, this::veLaiBoss);
        LamMoi.nghe(LamMoi.SU_KIEN, this::veLaiSuKien);
    }

    /**
     * Nạp lại bảng của tab vừa mở.
     *
     * <p>Chỉ nạp tab đang xem. Vài tab phải quét cả bảng mẫu vật phẩm, làm hết
     * mười hai tab mỗi lần bấm là đứng hình.</p>
     */
    private void napTabTen(String ten) {
        try {
            switch (ten) {
                case "Quy ước":
                    loadConfigQuiet();
                    break;
                case "Boss":
                    veLaiBoss();
                    napBossData();
                    napNhom();
                    napBossMap();
                    break;
                case "Đồ rơi từ quái":
                    napQuaiDoRoi();
                    break;
                case "Tỉ lệ":
                    napTiLe();
                    break;
                case "Set kích hoạt":
                    loadSet();
                    break;
                case "Phúc lợi":
                    plLoadNhom();
                    break;
                case "Sự kiện":
                    veLaiSuKien();
                    break;
                case "Bản đồ nhanh":
                    napBangMapNhanh();
                    break;
                case "Công thức đổi":
                    loadCongThuc();
                    break;
                case "Nội tại":
                    napBangNoiTai();
                    break;
                case "Vòng quay Thượng Đế":
                    napBangVongQuay();
                    break;
                case "Top máy đấm":
                    napBangTop();
                    break;
                case "Nhiệm vụ chính tuyến":
                    napBangNhiemVuChinh();
                    break;
                case "Lời chào":
                    napLoiChao();
                    break;
                default:
                    // Nhung tab con lai doc du lieu tinh, khong can nap lai.
                    break;
            }
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Lỗi nạp lại tab " + ten);
        }
    }

    private void veLaiBoss() {
        loadBosses();
        loadDrops();
    }

    private void veLaiSuKien() {
        napTatCaSuKien();
    }

    /** Những bảng có kèm tên hoặc ảnh vật phẩm, vẽ lại khi vật phẩm đổi. */
    private void veLaiTheoVatPham() {
        loadDrops();
        napTatCaSuKien();
        loadCongThuc();
    }

    // =====================================================================
    //  Quy ước
    // =====================================================================

    /** Ô nhập của từng khoá cấu hình, giữ đúng thứ tự khai báo. */
    private final Map<String, JTextField> fields = new LinkedHashMap<>();

    /**
     * Bảng đang mở, để nơi khác nhờ vẽ lại tab Quy ước.
     *
     * <p>Chỉ có <b>một</b> bảng trong cả chương trình — {@code ServerManagerUI}
     * dựng đúng một lần. Cần tham chiếu này vì nút bảo trì nay nằm ở thanh bên,
     * mà bật tắt bảo trì thì ô chữ {@code bao_tri} trong tab Quy ước phải đổi
     * theo: ô đó đọc từ {@code ConfigDAO} lúc nạp, không tự biết là giá trị vừa
     * đổi.</p>
     */
    private static volatile SystemPanel dangMo;

    /** Nạp lại tab Quy ước, nếu bảng đang mở. */
    public static void dongBoQuyUoc() {
        SystemPanel b = dangMo;
        if (b != null) {
            SwingUtilities.invokeLater(b::loadConfig);
        }
    }

    private final JTextArea oLoiChao = new JTextArea(8, 60);

    /**
     * Tab <b>Lời chào</b> — dòng chữ hiện ra khi người chơi vừa vào game.
     *
     * <h2>Vì sao tách khỏi tab Quy ước</h2>
     *
     * <p>Tab Quy ước là một cột ô nhập <b>một dòng</b>, hợp với những con số. Lời
     * chào thì là câu chữ, có thể dài và nhiều dòng — nhét vào một ô hẹp thì
     * không đọc được hết, không xuống dòng được, và nằm lẫn giữa mấy chục con số
     * nên chẳng ai nghĩ tới việc sửa nó.</p>
     */
    private JComponent buildLoiChaoTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        oLoiChao.setLineWrap(true);
        oLoiChao.setWrapStyleWord(true);
        oLoiChao.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Lưu", OK_GREEN, e -> luuLoiChao()));
        nut.add(button("Đọc lại", GREY, e -> napLoiChao()));
        nut.add(button("Tắt lời chào", WARN_RED, e -> {
            oLoiChao.setText("");
            luuLoiChao();
        }));

        root.add(nhan("Dòng chữ hiện ra ngay khi người chơi vào game, dưới dạng "
                + "thông báo của quản trị viên. <b>Để trống là không hiện gì</b> — "
                + "đó cũng là việc của nút \"Tắt lời chào\"."
                + "<br><br>"
                + "Xuống dòng cứ bấm Enter, hiện ra trong game đúng như gõ ở đây. "
                + "Có hiệu lực <b>ngay</b> với người đăng nhập sau khi lưu, không "
                + "cần khởi động lại máy chủ."), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(oLoiChao), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napLoiChao();
        return root;
    }

    private void napLoiChao() {
        oLoiChao.setText(ConfigDAO.chuoi(ConfigDAO.LOI_CHAO));
        oLoiChao.setCaretPosition(0);
    }

    private void luuLoiChao() {
        String v = oLoiChao.getText() == null ? "" : oLoiChao.getText();
        if (!ConfigDAO.set(ConfigDAO.LOI_CHAO, v)) {
            note(WARN_RED, "Không lưu được lời chào — xem log máy chủ.");
            return;
        }
        ConfigDAO.reload();
        napLoiChao();
        note(OK_GREEN, v.trim().isEmpty()
                ? "Đã tắt lời chào — vào game sẽ không hiện gì."
                : "Đã lưu lời chào — có hiệu lực ngay.");
    }

    private JComponent buildConfigTab() {
        dangMo = this;
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        for (String key : ConfigDAO.keys()) {
            c.gridx = 0;
            c.gridy = row;
            JLabel k = new JLabel(key);
            k.setFont(new Font("Consolas", Font.BOLD, 12));
            form.add(k, c);

            c.gridx = 1;
            JTextField f = new JTextField(12);
            f.setHorizontalAlignment(JTextField.RIGHT);
            fields.put(key, f);
            form.add(f, c);

            c.gridx = 2;
            JLabel note = new JLabel(ConfigDAO.note(key));
            note.setForeground(GREY);
            form.add(note, c);
            row++;
        }

        // Cot don day o ben phai: GridBagLayout mac dinh dua noi dung ra GIUA
        // khung. Mot o rong an weightx=1 keo phan con lai ve sat trai.
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(javax.swing.Box.createHorizontalGlue(), c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 3;
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        btn.setOpaque(false);
        btn.add(button("Lưu", OK_GREEN, e -> saveConfig()));
        btn.add(button("Đọc lại", GREY, e -> loadConfig()));
        btn.add(button("Đẩy dữ liệu cho client", ACCENT, e -> bumpVersion()));
        // Hai nut "Bat bao tri" va "Khoi dong lai may chu" da chuyen sang
        // thanh ben trai (NutMayChu): chung ngat ket noi cua nguoi dang choi,
        // khac han moi thu khac o day von chi luu mot con so — de lan giua
        // "Luu" va "Doc lai" o cuoi mot bieu mau dai la vua kho tim luc can
        // gap, vua de bam nham luc dang sua con so khac.
        form.add(btn, c);

        c.gridy = row + 1;
        JLabel hint = new JLabel("<html>Lưu xong có hiệu lực <b>ngay</b>, không cần "
                + "khởi động lại máy chủ.<br>Người chơi mua vé ở NPC và admin cấp vé ở "
                + "tab Nạp tiền đều đọc cùng bộ số này.</html>");
        hint.setForeground(GREY);
        form.add(hint, c);

        loadConfig();

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(form, BorderLayout.NORTH);
        return ServerGuiUtils.cuon(wrap);
    }

    private void loadConfig() {
        ConfigDAO.reload();
        for (Map.Entry<String, JTextField> e : fields.entrySet()) {
            e.getValue().setText(giaTri(e.getKey()));
        }
        note(GREY, "Đã đọc lại quy ước từ CSDL.");
    }

    /**
     * Lưu tất cả quy ước.
     *
     * <p>Kiểm tra <b>toàn bộ</b> ô trước khi ghi ô đầu tiên: ghi được nửa chừng
     * rồi báo lỗi sẽ để hệ thống ở trạng thái nửa cũ nửa mới, khó lần ra hơn
     * nhiều so với việc từ chối cả lượt.</p>
     */
    private void saveConfig() {
        for (Map.Entry<String, JTextField> e : fields.entrySet()) {
            if (ConfigDAO.laChuoi(e.getKey())) {
                if (e.getValue().getText().trim().isEmpty()) {
                    note(WARN_RED, "Ô \"" + e.getKey() + "\" không được để trống.");
                    return;
                }
                continue;
            }
            String v = e.getValue().getText().trim().replace(".", "").replace(",", "");
            try {
                long n = Long.parseLong(v);
                if (n < 0) {
                    note(WARN_RED, "Ô \"" + e.getKey() + "\" không được âm.");
                    return;
                }
            } catch (NumberFormatException ex) {
                note(WARN_RED, "Ô \"" + e.getKey() + "\" phải là số nguyên.");
                return;
            }
        }
        int loi = 0;
        for (Map.Entry<String, JTextField> e : fields.entrySet()) {
            // Khoa chu giu NGUYEN dau cham/phay nguoi dung go: bo dau cham o mat
            // khau la doi mat khau ma khong bao ai.
            String v = ConfigDAO.laChuoi(e.getKey())
                    ? e.getValue().getText().trim()
                    : e.getValue().getText().trim().replace(".", "").replace(",", "");
            if (!ConfigDAO.set(e.getKey(), v)) {
                loi++;
            }
        }
        ConfigDAO.reload();
        loadConfigQuiet();
        note(loi == 0 ? OK_GREEN : WARN_RED, loi == 0
                ? "Đã lưu quy ước và áp dụng ngay."
                : "Có " + loi + " quy ước không lưu được — xem log máy chủ.");
    }

    /**
     * Tăng phiên bản dữ liệu để client tải lại.
     *
     * <p>Client giữ gói dữ liệu (part, hiệu ứng, ảnh, bảng vật phẩm) trong bộ nhớ
     * đệm và <b>chỉ tải lại khi số phiên bản đổi</b>. Thêm cải trang hay phụ kiện
     * mới mà quên bước này thì người chơi mặc vào không thấy gì — đó là bẫy hay
     * gặp nhất khi thêm đồ.</p>
     *
     * <p>Người đang online phải <b>thoát ra vào lại</b> mới nhận: gói dữ liệu chỉ
     * gửi một lần lúc đăng nhập.</p>
     */
    private void bumpVersion() {
        long d = ConfigDAO.num(ConfigDAO.VS_DATA);
        long i = ConfigDAO.num(ConfigDAO.VS_ITEM);
        int ok = JOptionPane.showConfirmDialog(this,
                "Tăng phiên bản để client tải lại dữ liệu?\n\n"
                + "vs_data: " + d + " → " + (d + 1) + "   (part, hiệu ứng, ảnh)\n"
                + "vs_item: " + i + " → " + (i + 1) + "   (bảng vật phẩm)\n\n"
                + "Người đang online phải thoát ra vào lại mới nhận —\n"
                + "gói dữ liệu chỉ gửi một lần lúc đăng nhập.",
                "Đẩy dữ liệu cho client", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        // Byte co dau: 127 la het tran, quay ve 0. Client chi so SANH BANG nen
        // quay vong khong sao, mien la khac so cu.
        boolean a = ConfigDAO.set(ConfigDAO.VS_DATA, String.valueOf((d + 1) % 128));
        boolean b = ConfigDAO.set(ConfigDAO.VS_ITEM, String.valueOf((i + 1) % 128));
        ConfigDAO.reload();
        loadConfigQuiet();
        note(a && b ? OK_GREEN : WARN_RED, a && b
                ? "Đã tăng phiên bản — client đăng nhập lại sẽ tải dữ liệu mới."
                : "Không lưu được phiên bản mới, xem log máy chủ.");
    }

    private void loadConfigQuiet() {
        for (Map.Entry<String, JTextField> e : fields.entrySet()) {
            e.getValue().setText(giaTri(e.getKey()));
        }
    }

    /** Giá trị hiện tại của một quy ước, đúng kiểu của nó. */
    private static String giaTri(String key) {
        // So hien dang 1.000 cho de doc. Khong so lech du lieu: khoi luu
        // da bo dau cham/phay truoc khi ghi, va khoa CHUOI thi giu nguyen.
        return ConfigDAO.laChuoi(key)
                ? ConfigDAO.chuoi(key)
                : PlayerManagerPanel.fmt(ConfigDAO.num(key));
    }

    // =====================================================================
    //  Boss
    // =====================================================================

    /**
     * Bảng boss đang sống trong máy chủ.
     *
     * <p>Dữ liệu lấy từ {@code BossManager} chứ không từ CSDL — vì <b>không có
     * bảng boss nào trong CSDL</b>. Cả 129 lớp boss viết cứng chỉ số ngay trong
     * mã nguồn. Hai bảng {@code boss_config} và {@code boss_drop} là lớp
     * <i>đè lên</i> phần viết cứng đó, dòng nào không có thì boss giữ nguyên
     * giá trị gốc.</p>
     */
    private final DefaultTableModel bossModel = new DefaultTableModel(
            new Object[]{"ID", "Tên / phase", "Đang chạy", "Trạng thái", "Bản đồ", "Khu",
                "HP", "Chờ hồi sinh", "Đè chỉ số", "Món rơi thêm"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bossTable = new JTable(bossModel);
    private final JLabel lblBossCount = new JLabel();

    // Chi so cot cua bang do roi. Dat ten thay vi go so: hom nay chen cot
    // "Phase" vao giua da lam moi cot phia sau dich mot o, va kieu loi do
    // khong bao gi ca — chi lang le doc nham o.
    private static final int COT_ROI_ID = 0;
    private static final int COT_ROI_BOSS = 1;
    private static final int COT_ROI_PHASE = 2;
    private static final int COT_ROI_ANH = 3;
    private static final int COT_ROI_ITEM = 4;
    private static final int COT_ROI_SL_MIN = 6;
    private static final int COT_ROI_SL_MAX = 7;
    private static final int COT_ROI_TI_LE = 8;
    private static final int COT_ROI_NV = 11;
    private static final int COT_ROI_MOT_MON = 12;
    private static final int COT_ROI_BAT = 13;
    private static final int COT_ROI_GHI_CHU = 14;

    private final DefaultTableModel dropModel = new DefaultTableModel(
            new Object[]{"ID", "Boss", "Phase", "Ảnh", "Vật phẩm", "Tên vật phẩm",
                "SL tối thiểu", "SL tối đa", "Tỉ lệ %", "HSD (ngày)",
                "% vĩnh viễn", "Điều kiện nhiệm vụ", "Chỉ 1 món", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == COT_ROI_ANH ? javax.swing.ImageIcon.class : Object.class;
        }
    };
    private final JTable dropTable = new JTable(dropModel);

    /**
     * Gom cả ba phần về boss vào <b>một</b> tab.
     *
     * <p>Ba phần trả lời ba câu hỏi khác nhau nên vẫn tách bảng, nhưng chúng
     * cùng nói về boss nên không đáng chiếm ba tab ở hàng ngoài — hàng đó đã
     * mười hai tab, thêm nữa là phải cuộn ngang mới thấy hết.</p>
     */
    private JComponent buildBossTongHop() {
        JTabbedPane trong = new JTabbedPane();
        trong.addTab("1. Đang chạy & đồ rơi", buildBossTab());
        trong.addTab("2. Số liệu", buildBossDataTab());
        trong.addTab("3. Nhóm hồi sinh", buildNhomTab());
        trong.addTab("4. Boss theo bản đồ", buildBossMapTab());
        // Nap lai tab con vua mo. Bat buoc: panel duoc dung TRUOC khi may chu
        // khoi dong, nen luc dung bang boss_theo_map va boss_spawn con rong —
        // khong nap lai thi tab do trong tron doi.
        trong.addChangeListener(e -> napTabBossCon(trong.getSelectedIndex()));
        return trong;
    }

    private void napTabBossCon(int chiSo) {
        try {
            switch (chiSo) {
                case 0:
                    loadBosses();
                    loadDrops();
                    break;
                case 1:
                    napBossData();
                    break;
                case 2:
                    napNhom();
                    break;
                case 3:
                    napBossMap();
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Lỗi nạp lại tab boss " + chiSo);
        }
    }
    /**
     * So sánh theo <b>số</b> cho những cột chứa chữ.
     *
     * <p>Bảng lưu HP dưới dạng chuỗi đã định dạng ("60.000.000 / 60.000.000",
     * "(220.000.000)", "—") nên bộ sắp xếp mặc định so theo bảng chữ cái: "6..."
     * đứng trước "5..." và 60 triệu nhảy lên trên 500 triệu. Bóc số đầu tiên ra
     * rồi so mới đúng thứ tự.</p>
     *
     * <p>Ô không có số nào ("—", "-", rỗng) coi như {@code -1} để dồn xuống cuối
     * — chúng là "chưa biết", không phải "bằng không".</p>
     */
    private static final java.util.Comparator<Object> SO_TRONG_CHU =
            (a, b) -> Long.compare(soDauTien(a), soDauTien(b));

    /** Số đầu tiên trong chuỗi, bỏ dấu phân nhóm. {@code -1} nếu không có số nào. */
    private static long soDauTien(Object o) {
        String s = o == null ? "" : String.valueOf(o);
        StringBuilder sb = new StringBuilder();
        boolean batDau = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch)) {
                sb.append(ch);
                batDau = true;
            } else if (ch == '.' && batDau) {
                // Dau phan nhom giua hai chu so -> bo qua, con dau cham cuoi cum
                // thi ket thuc luon.
                if (i + 1 < s.length() && Character.isDigit(s.charAt(i + 1))) {
                    continue;
                }
                break;
            } else if (batDau) {
                break;
            }
        }
        if (sb.length() == 0) {
            return -1L;
        }
        try {
            return Long.parseLong(sb.toString());
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }

    /**
     * So sánh cột "Chờ hồi sinh" theo <b>tổng số giây</b>.
     *
     * <p>Chuỗi có ba dạng: "45 giây", "57 phút", "57 phút 48 giây". Lấy số đầu
     * tiên thì "5 phút" xếp trước "45 giây" — sai, vì 5 phút lâu hơn hẳn. Phải
     * quy về giây rồi mới so.</p>
     */
    private static final java.util.Comparator<Object> THEO_GIAY =
            (a, b) -> Long.compare(raGiay(a), raGiay(b));

    private static long raGiay(Object o) {
        String s = o == null ? "" : String.valueOf(o);
        long phut = 0;
        long giay = 0;
        int vtPhut = s.indexOf("phút");
        int vtGiay = s.indexOf("giây");
        if (vtPhut >= 0) {
            phut = soDauTien(s.substring(0, vtPhut));
            if (phut < 0) {
                phut = 0;
            }
            if (vtGiay > vtPhut) {
                giay = soDauTien(s.substring(vtPhut + 4, vtGiay));
            }
        } else if (vtGiay >= 0) {
            giay = soDauTien(s.substring(0, vtGiay));
        } else {
            return -1L;     // "-" hoac "—": chua biet, don xuong cuoi
        }
        if (giay < 0) {
            giay = 0;
        }
        return phut * 60 + giay;
    }

    /**
     * Gắn ô tìm lọc dòng cho một bảng.
     *
     * <p>Lọc trên <b>mọi cột</b>, không phân biệt hoa thường, và thoát ký tự
     * đặc biệt trước khi dựng biểu thức — gõ dấu {@code (} hay {@code [} mà
     * không thoát là {@code regexFilter} ném lỗi ngay giữa lúc đang gõ.</p>
     *
     * <p>Bảng sau khi gắn vẫn dùng {@code convertRowIndexToModel} như cũ, nên
     * mọi chỗ đọc dòng đang chọn không phải sửa gì.</p>
     *
     * @return dải ngang chứa nhãn, ô nhập và nút xoá — bỏ vào BorderLayout.NORTH
     */
    private static JPanel oLocBang(final JTable bang, String goiY) {
        return oLocBang(bang, goiY, null);
    }

    /**
     * Như trên, kèm bộ so sánh riêng cho một số cột.
     *
     * @param soSanh cột → bộ so sánh; {@code null} là dùng mặc định cho mọi cột
     */
    private static JPanel oLocBang(final JTable bang, String goiY,
            java.util.Map<Integer, java.util.Comparator<Object>> soSanh) {
        final javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sap =
                new javax.swing.table.TableRowSorter<>(bang.getModel());
        if (soSanh != null) {
            for (java.util.Map.Entry<Integer, java.util.Comparator<Object>> e
                    : soSanh.entrySet()) {
                sap.setComparator(e.getKey(), e.getValue());
            }
        }
        bang.setRowSorter(sap);

        final JTextField o = new JTextField(26);
        final JLabel dem = new JLabel();
        final Runnable loc = () -> {
            String s = o.getText().trim();
            if (s.isEmpty()) {
                sap.setRowFilter(null);
            } else {
                sap.setRowFilter(javax.swing.RowFilter.regexFilter(
                        "(?i)" + java.util.regex.Pattern.quote(s)));
            }
            dem.setText(bang.getRowCount() + " / "
                    + bang.getModel().getRowCount() + " dòng");
        };
        o.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
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
        // Bang duoc do lai sau moi lan "Tai lai" -> cap nhat lai so dem.
        bang.getModel().addTableModelListener(e -> loc.run());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setOpaque(false);
        p.add(new JLabel("Lọc:"));
        p.add(o);
        p.add(button("Xoá lọc", GREY, e -> o.setText("")));
        JLabel g = new JLabel(goiY);
        g.setForeground(GREY);
        p.add(g);
        p.add(dem);
        loc.run();
        return p;
    }

    private JComponent buildBossTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // ---------- danh sách boss ----------
        bossTable.setRowHeight(24);
        // Cho chon nhieu dong de sua hang loat: dat rieng giay hoi sinh cho vai
        // chuc con ma phai bam tung cai thi khong ai lam noi.
        bossTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        bossTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        bossTable.setAutoCreateRowSorter(true);
        int[] wb = {55, 175, 70, 90, 165, 45, 165, 110, 85, 85};
        for (int i = 0; i < wb.length && i < bossTable.getColumnCount(); i++) {
            bossTable.getColumnModel().getColumn(i).setPreferredWidth(wb[i]);
        }
        bossTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bossTable.getSelectedRow() >= 0) {
                    bossXemSoLieu();
                }
            }
        });
        bossTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadDrops();
            }
        });

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.setBorder(titled("Boss trong máy chủ"));
        // Cot 5 "Khu", 6 "HP", 7 "Cho hoi sinh" deu la chuoi da dinh dang nen
        // phai so theo so, khong thi 60 trieu nhay len tren 500 trieu.
        java.util.Map<Integer, java.util.Comparator<Object>> soSanhBoss =
                new java.util.HashMap<>();
        soSanhBoss.put(5, SO_TRONG_CHU);
        soSanhBoss.put(6, SO_TRONG_CHU);
        soSanhBoss.put(7, THEO_GIAY);
        top.add(oLocBang(bossTable, "gõ tên boss, id, bản đồ hay trạng thái",
                soSanhBoss), BorderLayout.NORTH);

        top.add(ServerGuiUtils.cuon(bossTable), BorderLayout.CENTER);

        JPanel btnTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnTop.setOpaque(false);
        // Tab nay CHI DE XEM boss dang co trong game va dat do roi cho chung.
        // Them boss va sua chi so nam o tab 2; nhom hoi sinh nam o tab 3. Moi
        // viec mot cho, khong de cung mot con boss co hai duong sua.

        btnTop.add(button("Xem số liệu", ACCENT, e -> bossXemSoLieu()));
        btnTop.add(button("Hồi sinh ngay", OK_GREEN, e -> bossRespawn()));
        btnTop.add(button("Tải lại", GREY, e -> loadBosses()));
        lblBossCount.setForeground(GREY);
        btnTop.add(lblBossCount);
        JLabel tip = new JLabel("  (chỉ để xem — thêm boss và sửa chỉ số ở tab 2)");
        tip.setForeground(GREY);
        btnTop.add(tip);
        top.add(btnTop, BorderLayout.SOUTH);

        // ---------- vật phẩm rơi ----------
        dropTable.setRowHeight(32);
        dropTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dropTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        // Số phần tử phải KHỚP số cột của bảng. Bỏ một cột mà quên sửa mảng này
        // là getColumn() ném ArrayIndexOutOfBounds ngay lúc dựng giao diện, và
        // máy chủ chết trước khi mở được cổng.
        int[] wd = {45, 55, 80, 40, 70, 220, 90, 90, 75, 85, 90, 45, 180};
        for (int i = 0; i < wd.length && i < dropTable.getColumnCount(); i++) {
            dropTable.getColumnModel().getColumn(i).setPreferredWidth(wd[i]);
        }
        dropTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && dropTable.getSelectedRow() >= 0) {
                    dropDialog(false);
                }
            }
        });

        JPanel bot = new JPanel(new BorderLayout(0, 4));
        bot.setOpaque(false);
        khungRoi = bot;
        bot.setBorder(titled(TIEU_DE_ROI));
        bot.add(ServerGuiUtils.cuon(dropTable), BorderLayout.CENTER);

        JPanel btnBot = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnBot.setOpaque(false);
        btnBot.add(button("Thêm món rơi", OK_GREEN, e -> dropDialog(true)));
        btnBot.add(button("Sửa món rơi", ACCENT, e -> dropDialog(false)));
        btnBot.add(button("Xoá món rơi", WARN_RED, e -> dropDelete()));
        btnBot.add(button("Tải lại", GREY, e -> loadDrops()));
        bot.add(btnBot, BorderLayout.SOUTH);

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, top, bot);
        split.setResizeWeight(0.62);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        // Boss nap xong sau khi may chu khoi dong, nen cho roi thu lai.
        // Cap nhat TAI CHO de khong mat dong dang chon.
        javax.swing.Timer t = new javax.swing.Timer(3000, e -> loadBosses());
        t.start();
        return root;
    }

    /** Đọc danh sách boss đang sống, cập nhật tại chỗ để không mất lựa chọn. */
    private void loadBosses() {
        // Hoi SO DANG KY day du chu khong rieng BossManager. Tau Pay Pay dang ky
        // o FinalBossManager va con duoc Map.initBoss dung them o nhieu ban do —
        // hoi mot manh thi tab nay thieu han nhung con do, dung nhu da thay.
        //
        // tatCaBoss() da tra ban sao co dong bo nen khong con canh
        // ConcurrentModificationException nhu khi duyet thang danh sach goc.
        // CHI hien boss thuoc danh sach dang quan. Con nao do Map.initBoss dung
        // them theo ban do thi khong nam trong danh sach, va tab nay phai la
        // dung cai danh sach do — mot nguon duy nhat, khong lan lon.
        java.util.Set<Integer> trongDs = new java.util.HashSet<>();
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            trongDs.add(d.bossId);
        }
        java.util.List<nro.entity.boss.Boss> bs = new java.util.ArrayList<>();
        for (nro.entity.boss.Boss b : nro.entity.boss.Boss.tatCaBoss()) {
            if (b != null && trongDs.contains((int) b.id)) {
                bs.add(b);
            }
        }
        if (bs.isEmpty()) {
            bossModel.setRowCount(0);
            bossTheoDong.clear();
            phaseTheoDong.clear();
            lblBossCount.setText("  (chưa có boss nào trong máy chủ)");
            return;
        }
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        bossTheoDong.clear();
        phaseTheoDong.clear();
        for (nro.entity.boss.Boss b : bs) {
            if (b == null) {
                continue;
            }
            String map = "(chưa vào map)";
            String khu = "-";
            if (b.zone != null && b.zone.map != null) {
                map = b.zone.map.mapName + " [" + b.zone.map.mapId + "]";
                khu = String.valueOf(b.zone.zoneId);
            }
            String hp = "-";
            if (b.nPoint != null && b.nPoint.hpMax > 0) {
                hp = PlayerManagerPanel.fmt(b.nPoint.hp) + " / "
                        + PlayerManagerPanel.fmt(b.nPoint.hpMax);
            } else {
                // Chua xuat hien -> nPoint chua duoc tinh. Hien muc HP theo cau
                // hinh de admin van doi chieu duoc, kem dau ngoac cho ro.
                try {
                    long[] moc = b.data[0].getHp();
                    hp = "(" + PlayerManagerPanel.fmt(moc[0]) + ")";
                } catch (Exception ignored) {
                    hp = "-";
                }
            }
            // Boss.name chi duoc dat trong initBase(), tuc luc boss XUAT HIEN.
            // Boss dang cho co name = null — khong phai "mat boss". Lay ten tu
            // BossData de dong nao cung doc duoc.
            String ten = b.name;
            if (ten == null || ten.isEmpty()) {
                try {
                    ten = b.data[0].getName() + "  (chưa xuất hiện)";
                } catch (Exception ignored) {
                    ten = "(không rõ)";
                }
            }
            long cho = b.getSecondsUntilRespawn();
            int id = (int) b.id;
            // MOI PHASE MOT DONG. Truoc day mot con boss chi mot dong, cot
            // Phase gop ca chuoi "Black Goku -> [Super Black Goku]" — nhin thi
            // biet co phase 2, nhung khong co dong nao de chon ma dat do roi
            // rieng cho phase do, trong khi bang boss_drop von da co cot phase.
            int soPhase = (b.data == null || b.data.length == 0) ? 1 : b.data.length;
            for (int ph = 0; ph < soPhase; ph++) {
                bossTheoDong.add(b);
                phaseTheoDong.add(ph);

                boolean dangODay = Math.max(0, b.currentLevel) == ph;

                // Map, khu, HP, trang thai chi co that o phase DANG chay. Cac
                // phase khac ghi dau gach, chu khong chep lai so cua phase hien
                // tai — chep lai la doc nham tuong phase 2 cung dang song.
                String mapP = dangODay ? map : "—";
                String khuP = dangODay ? khu : "—";
                String hpP;
                if (dangODay) {
                    hpP = hp;
                } else {
                    try {
                        long[] moc = b.data[ph].getHp();
                        hpP = "(" + PlayerManagerPanel.fmt(moc[0]) + ")";
                    } catch (Exception boQua) {
                        hpP = "—";
                    }
                }
                String tenP = tenPhase(b, ph);
                if (soPhase > 1) {
                    tenP = tenP + "   (phase " + (ph + 1) + "/" + soPhase + ")";
                }
                if (dangODay && (b.name == null || b.name.isEmpty())) {
                    tenP = tenP + "  (chưa xuất hiện)";
                }

                int soRoi = 0;
                for (nro.repository.dao.BossDAO.Drop d
                        : nro.repository.dao.BossDAO.drops(id)) {
                    if (d != null && d.roiOPhase(ph)) {
                        soRoi++;
                    }
                }

                rows.add(new Object[]{id, tenP,
                    dangODay ? "đang ở phase này" : "",
                    dangODay ? moTaTrangThaiBoss(b) : "—",
                    mapP, khuP, hpP,
                    dangODay ? (cho < 0 ? "-" : moTaThoiGian(cho)) : "—",
                    nro.repository.dao.BossDAO.config(id) != null ? "có" : "",
                    soRoi == 0 ? "" : String.valueOf(soRoi)});
            }
        }

        // Cung so dong thi ghi de tung o -> lua chon giu nguyen qua moi nhip 3 giay.
        boolean sameList = rows.size() == bossModel.getRowCount();
        if (sameList) {
            for (int i = 0; i < rows.size(); i++) {
                if (!String.valueOf(bossModel.getValueAt(i, 0))
                        .equals(String.valueOf(rows.get(i)[0]))) {
                    sameList = false;
                    break;
                }
            }
        }
        if (sameList) {
            for (int i = 0; i < rows.size(); i++) {
                Object[] r = rows.get(i);
                for (int c = 0; c < r.length; c++) {
                    Object cur = bossModel.getValueAt(i, c);
                    if (cur == null ? r[c] != null : !cur.equals(r[c])) {
                        bossModel.setValueAt(r[c], i, c);
                    }
                }
            }
        } else {
            bossModel.setRowCount(0);
            for (Object[] r : rows) {
                bossModel.addRow(r);
            }
        }
        lblBossCount.setText("  " + rows.size() + " boss");
    }

    /**
     * Boss ứng với từng dòng của bảng, cùng thứ tự.
     *
     * <p>Nhiều boss <b>dùng chung một id</b> — máy chủ có cả loạt Super Broly id
     * {@code -500}. Tra theo id thì mọi dòng đó trỏ về con đầu tiên, nên bấm
     * "Hồi sinh ngay" ở dòng "Super Broly 90" lại tác động vào "Super Broly 99".
     * Giữ tham chiếu theo dòng mới đúng con đang chọn.</p>
     */
    private final java.util.List<nro.entity.boss.Boss> bossTheoDong =
            new java.util.ArrayList<>();

    /** Phase ung voi tung dong cua bang tab 1 — di cap voi {@link #bossTheoDong}. */
    private final java.util.List<Integer> phaseTheoDong =
            new java.util.ArrayList<>();

    /** Phase cua dong dang chon, hoac {@code -1} neu chua chon dong nao. */
    private int phaseDangChon() {
        int r = bossTable.getSelectedRow();
        if (r < 0) {
            return -1;
        }
        int i = bossTable.convertRowIndexToModel(r);
        return i >= 0 && i < phaseTheoDong.size() ? phaseTheoDong.get(i) : -1;
    }

    /** Boss của dòng đang chọn, hoặc {@code null} nếu chưa chọn dòng nào. */
    private nro.entity.boss.Boss bossDangChon() {
        int r = bossTable.getSelectedRow();
        if (r < 0) {
            return null;
        }
        int i = bossTable.convertRowIndexToModel(r);
        return i >= 0 && i < bossTheoDong.size() ? bossTheoDong.get(i) : null;
    }

    private int bossSelectedId() {
        int r = bossTable.getSelectedRow();
        if (r < 0) {
            return Integer.MIN_VALUE;
        }
        try {
            return Integer.parseInt(String.valueOf(
                    bossModel.getValueAt(bossTable.convertRowIndexToModel(r), 0)).trim());
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
        }
    }

    private String bossSelectedName() {
        int r = bossTable.getSelectedRow();
        return r < 0 ? "" : String.valueOf(
                bossModel.getValueAt(bossTable.convertRowIndexToModel(r), 1));
    }

    /**
     * Chèn dấu chấm mỗi ba chứ số cho từng số trong dãy, giữ nguyên dấu phẩy
     * ngăn các cấp boss: {@code "2000000000"} thành {@code "2.000.000.000"},
     * {@code "1000000,5000000"} thành {@code "1.000.000,5.000.000"}.
     *
     * <p>Đoạn đang gõ dở dang mà chưa thành số thì để yên, không xóa của admin.</p>
     */
    static String chamNganNgan(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder ra = new StringBuilder();
        String[] phan = raw.split(",", -1);
        for (int i = 0; i < phan.length; i++) {
            if (i > 0) {
                ra.append(',');
            }
            String so = phan[i].replace(".", "").replace(" ", "").trim();
            if (so.isEmpty()) {
                continue;
            }
            try {
                ra.append(PlayerManagerPanel.fmt(Long.parseLong(so)));
            } catch (NumberFormatException ex) {
                ra.append(phan[i].trim());
            }
        }
        return ra.toString();
    }

    /** Số ký tự có nghĩa (chứ số và dấu phẩy) nằm trước vị trí {@code den}. */
    private static int demCoNghia(String s, int den) {
        int n = 0;
        for (int i = 0; i < Math.min(den, s.length()); i++) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch) || ch == ',') {
                n++;
            }
        }
        return n;
    }

    /** Vị trí ngay sau ký tự có nghĩa thứ {@code n}. */
    private static int viTriSauCoNghia(String s, int n) {
        if (n <= 0) {
            return 0;
        }
        int d = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch) || ch == ',') {
                d++;
                if (d == n) {
                    return i + 1;
                }
            }
        }
        return s.length();
    }

    /**
     * Cho một ô nhập tự chèn dấu chấm ngăn nghìn ngay khi gõ.
     *
     * <p>Con trỏ được đặt lại theo <b>số chứ số</b> đứng trước nó, không theo vị
     * trí ký tự — nếu không thì mỗi lần thêm một dấu chấm con trỏ sẽ nhảy lùi một
     * ô và admin gõ ra số sai mà không hiểu tại sao.</p>
     */
    static void tuChamNganNgan(final JTextField f) {
        f.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

            private boolean dangSua;

            private void lam() {
                if (dangSua) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    dangSua = true;
                    try {
                        String cu = f.getText();
                        String moi = chamNganNgan(cu);
                        if (moi.equals(cu)) {
                            return;
                        }
                        int coNghia = demCoNghia(cu, f.getCaretPosition());
                        f.setText(moi);
                        f.setCaretPosition(viTriSauCoNghia(moi, coNghia));
                    } finally {
                        dangSua = false;
                    }
                });
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                lam();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                lam();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                lam();
            }
        });
    }

    /** Cho boss đang chọn xuất hiện lại ngay, không phải chờ hết giờ. */
    private void bossRespawn() {
        nro.entity.boss.Boss b = bossDangChon();
        if (b == null) {
            note(WARN_RED, "Chưa chọn boss nào.");
            return;
        }
        try {
            String loi = b.hoiSinhNgay();
            if (loi != null) {
                note(WARN_RED, loi);
                return;
            }
            String ten = b.name != null && !b.name.isEmpty() ? b.name : "#" + b.id;
            note(OK_GREEN, "Đã bỏ thời gian chờ của " + ten
                    + " — boss sẽ xuất hiện trong vài giây.");
            loadBosses();
        } catch (Exception ex) {
            note(WARN_RED, "Không hồi sinh được: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- món rơi

    /**
     * Đổ bảng món rơi: <b>đồ viết cứng trong mã nguồn</b> trước, rồi tới đồ cấu
     * hình trong CSDL.
     *
     * <p>Cột "Nguồn" phân biệt hai loại. Dòng <i>mã nguồn</i> chỉ để xem — sửa
     * được thì phải sửa Java rồi biên dịch lại; panel không giả vờ sửa được.</p>
     */
    /** Tiêu đề khung đồ rơi khi chưa chọn dòng boss nào. */
    private static final String TIEU_DE_ROI = "Vật phẩm rơi của boss đang chọn "
            + "(đồ viết cứng trong mã nguồn đã bị chặn hẳn — đây là nguồn duy nhất)";

    /** Khung bao bảng đồ rơi, giữ lại để đổi tiêu đề theo phase đang chọn. */
    private JPanel khungRoi;
    /**
     * Hộp chọn nhiệm vụ chính — chọn từ danh sách thay vì nhớ id.
     *
     * <p>Máy chủ có hàng trăm nhiệm vụ và id không gợi ý gì; gõ số trần thì
     * không cách nào biết mình đang chặn đúng chặng nào.</p>
     *
     * @param hienTai id đang đặt, {@code -1} là chưa đặt
     * @return id đã chọn, {@code -1} nếu chọn "không đặt", hoặc {@code -2} nếu huỷ
     */
    private int chonNhiemVu(java.awt.Component cha, int hienTai) {
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"ID", "Tên nhiệm vụ"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        m.addRow(new Object[]{-1, "— không đặt điều kiện này —"});
        try {
            for (nro.entity.task.TaskMain t : nro.server.Manager.TASKS) {
                if (t != null) {
                    m.addRow(new Object[]{t.id, t.name});
                }
            }
        } catch (Exception boQua) {
            // Bảng nhiệm vụ chưa nạp -> vẫn mở được hộp, chỉ có mỗi dòng đầu.
        }
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getColumnModel().getColumn(0).setPreferredWidth(60);
        t.getColumnModel().getColumn(1).setPreferredWidth(420);
        for (int i = 0; i < m.getRowCount(); i++) {
            if (Integer.parseInt(String.valueOf(m.getValueAt(i, 0))) == hienTai) {
                t.setRowSelectionInterval(i, i);
                break;
            }
        }
        JScrollPane cuon = new JScrollPane(t);
        cuon.setPreferredSize(new java.awt.Dimension(520, 380));
        if (JOptionPane.showConfirmDialog(cha, cuon, "Chọn nhiệm vụ",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return -2;
        }
        int r = t.getSelectedRow();
        if (r < 0) {
            return -2;
        }
        return Integer.parseInt(String.valueOf(m.getValueAt(
                t.convertRowIndexToModel(r), 0)));
    }

    /** Một hàng "điều kiện nhiệm vụ": ô hiện tên + nút chọn + nút bỏ. */
    private JPanel hangNhiemVu(int[] giuId, JLabel nhan) {
        veNhanNhiemVu(giuId[0], nhan);
        JPanel p = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        p.add(nhan);
        p.add(button("Chọn…", ACCENT, e -> {
            int moi = chonNhiemVu(p, giuId[0]);
            if (moi != -2) {
                giuId[0] = moi;
                veNhanNhiemVu(giuId[0], nhan);
            }
        }));
        p.add(button("Bỏ", GREY, e -> {
            giuId[0] = -1;
            veNhanNhiemVu(giuId[0], nhan);
        }));
        return p;
    }

    private void veNhanNhiemVu(int id, JLabel nhan) {
        if (id < 0) {
            nhan.setText("(không đặt)");
            nhan.setForeground(GREY);
            return;
        }
        String ten = "";
        try {
            for (nro.entity.task.TaskMain t : nro.server.Manager.TASKS) {
                if (t != null && t.id == id) {
                    ten = " — " + t.name;
                    break;
                }
            }
        } catch (Exception boQua) {
        }
        nhan.setText("NV " + id + ten);
        nhan.setForeground(ACCENT);
    }

    /**
     * Đổ bảng đồ rơi của boss <b>và phase</b> đang chọn.
     *
     * <p>Mỗi phase một bảng rơi riêng, coi như những con boss khác nhau. Muốn
     * một món rơi ở cả hai phase thì thêm hai dòng — rõ hơn một dòng ẩn danh áp
     * cho tất cả, và cho phép đặt tỉ lệ khác nhau theo từng phase.</p>
     */
    private void loadDrops() {
        dropModel.setRowCount(0);
        int id = bossSelectedId();
        int ph = phaseDangChon();
        if (khungRoi != null) {
            khungRoi.setBorder(titled(ph < 0 ? TIEU_DE_ROI
                    : "Vật phẩm rơi của phase " + (ph + 1)
                    + " (mỗi phase một danh sách riêng)"));
        }
        // KHONG liet ke do roi viet cung nua: chung da bi chan han, khong mon
        // nao rot. Hien ra chi lam bang day nhung dong khong bao gio xay ra.
        // Muon dua danh sach cu vao cau hinh thi bam "Chep do roi goc xuong".
        for (nro.repository.dao.BossDAO.Drop d : nro.repository.dao.BossDAO.allDrops()) {
            if (id != Integer.MIN_VALUE && d.bossId != id) {
                continue;
            }
            if (ph >= 0 && !d.roiOPhase(ph)) {
                continue;
            }

            nro.entity.template.ItemTemplate t = null;
            try {
                t = nro.service.item.ItemService.gI().getTemplate(d.itemId);
            } catch (Exception ignored) {
                // Bảng mẫu vật phẩm chưa nạp -> vẫn hiện dòng, chỉ thiếu tên.
            }
            boolean laTL = d.itemId
                    == nro.repository.dao.BossDAO.ITEM_THAN_LINH_NGAU_NHIEN;
            dropModel.addRow(new Object[]{d.id, d.bossId,
                "phase " + Math.max(1, d.phase),
                laTL ? null : (t != null ? PlayerManagerPanel.iconOf(t.iconID) : null),
                d.itemId,
                laTL ? "Đồ Thần Linh ngẫu nhiên (13 món)"
                        : (t != null ? t.name : "(không rõ)"),
                PlayerManagerPanel.fmt(d.qtyMin), PlayerManagerPanel.fmt(d.qtyMax),
                phanTramRoi(d.rateNum, d.rateDen) + "%",
                (d.hsdMin > 0 && d.hsdMax > 0)
                        ? (d.hsdMin == d.hsdMax ? String.valueOf(d.hsdMin)
                                : d.hsdMin + "–" + d.hsdMax)
                        : "vĩnh viễn",
                d.hsdVinhVien > 0 ? d.hsdVinhVien + "%" : "",
                d.moTaNhiemVu(),
                d.chiMotMon ? "có" : "",
                d.active ? "có" : "",
                d.note});
        }
    }

    /**
     * Bóc phần số thập phân ra khỏi chuỗi đã định dạng của bảng.
     *
     * <p>Bảng hiện tỉ lệ kèm dấu {@code %} ("50%"), mà hộp sửa lại lấy thẳng ô
     * đó đổ vào ô nhập. Lần lưu sau {@code Double.parseDouble("50%")} ném lỗi và
     * người dùng chỉ thấy câu "phải là số" mà không hiểu sai ở đâu — phải xoá
     * tay dấu phần trăm mới lưu được.</p>
     */
    private static String soSach(Object o) {
        String s = o == null ? "" : String.valueOf(o).trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch) || ch == '.' || ch == ','
                    || (ch == '-' && sb.length() == 0)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Bóc số nguyên ra khỏi chuỗi đã định dạng của bảng.
     *
     * <p>Số lượng trong bảng đi qua {@code PlayerManagerPanel.fmt} nên có dấu
     * phân nhóm: 1000 hiện thành "1.000". Đổ thẳng vào ô nhập rồi
     * {@code Integer.parseInt("1.000")} là ném lỗi — cùng một cái bẫy với dấu
     * phần trăm, chỉ khác chỗ hiện ra.</p>
     */
    private static String soNguyenSach(Object o) {
        String s = o == null ? "" : String.valueOf(o).trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch) || (ch == '-' && sb.length() == 0)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private void dropDialog(boolean them) {
        int bossId = bossSelectedId();
        if (them && bossId == Integer.MIN_VALUE) {
            note(WARN_RED, "Chọn boss trước đã.");
            return;
        }
        int r = dropTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn dòng nào để sửa.");
            return;
        }

        final int id = them ? -1 : intOf(dropModel.getValueAt(r, 0));
        JTextField fBoss = new JTextField(String.valueOf(
                them ? bossId : intOf(dropModel.getValueAt(r, 1))), 8);
        JTextField fItem = new JTextField(them ? "" : String.valueOf(
                dropModel.getValueAt(r, COT_ROI_ITEM)), 8);
        JTextField fMin = new JTextField(them ? "1"
                : soNguyenSach(dropModel.getValueAt(r, COT_ROI_SL_MIN)), 8);
        JTextField fMax = new JTextField(them ? "1"
                : soNguyenSach(dropModel.getValueAt(r, COT_ROI_SL_MAX)), 8);
        String tiLe = them ? "1" : soSach(dropModel.getValueAt(r, COT_ROI_TI_LE));
        JTextField fRate = new JTextField(tiLe, 8);
        JTextField fNote = new JTextField(them ? "" : nz(dropModel.getValueAt(r, COT_ROI_GHI_CHU)), 22);
        javax.swing.JCheckBox cbOn = new javax.swing.JCheckBox("Bật",
                them || "có".equals(String.valueOf(dropModel.getValueAt(r, COT_ROI_BAT))));
        javax.swing.JCheckBox cbMotMon = new javax.swing.JCheckBox(
                "Chỉ rơi khi người giết chưa có món này "
                + "(đếm gộp hành trang + rương ở nhà)",
                !them && "có".equals(String.valueOf(
                        dropModel.getValueAt(r, COT_ROI_MOT_MON))));

        // Phase lay tu DONG DANG CHON o bang boss phia tren, khong hoi lai o
        // day nua: bang tren da moi phase mot dong, hoi lan hai chi tao co hoi
        // chon lech nhau roi khong hieu sao mon vua them khong thay dau.
        final int phaseLuu;
        int phDong = phaseDangChon();
        if (phDong >= 0) {
            phaseLuu = phDong + 1;
        } else if (!them) {
            phaseLuu = Math.max(1, viTriPhase(
                    String.valueOf(dropModel.getValueAt(r, COT_ROI_PHASE))) + 1);
        } else {
            phaseLuu = 1;
        }

                // Doc thang tu DAO chu khong lay tu bang: cot HSD tren bang la chuoi
                // da dinh dang ("7-30", "vinh vien") nen khong dung de dien lai o nhap.
                nro.repository.dao.BossDAO.Drop cuDrop = null;
                if (!them) {
                    for (nro.repository.dao.BossDAO.Drop x : nro.repository.dao.BossDAO.allDrops()) {
                        if (x.id == id) {
                            cuDrop = x;
                            break;
                        }
                    }
                }
                JTextField fHsdMin = new JTextField(
                        String.valueOf(cuDrop == null ? 0 : cuDrop.hsdMin), 8);
                JTextField fHsdMax = new JTextField(
                        String.valueOf(cuDrop == null ? 0 : cuDrop.hsdMax), 8);
                // Chuoi chi so ngau nhien; sua qua hop thoai rieng chu khong go tay.
                final String[] chiSo = {cuDrop == null ? "[]" : nz(cuDrop.options)};
                JTextField fHsdVV = new JTextField(
                        String.valueOf(cuDrop == null ? 0 : cuDrop.hsdVinhVien), 8);
                // Ba dieu kien nhiem vu cua NGUOI GIET, doc lap nhau. Giu trong
                // mang mot phan tu de lambda cua nut "Chon..." sua duoc.
                final int[] nvDung = {cuDrop == null ? -1 : cuDrop.nvDung};
                final int[] nvTu = {cuDrop == null ? -1 : cuDrop.nvTu};
                final int[] nvDen = {cuDrop == null ? -1 : cuDrop.nvDen};

        JLabel lblTen = new JLabel("—");
        lblTen.setForeground(ACCENT);
        Runnable traTen = () -> {
            try {
                nro.entity.template.ItemTemplate t = nro.service.item.ItemService.gI()
                        .getTemplate(Integer.parseInt(fItem.getText().trim()));
                lblTen.setText(t != null ? t.name : "(không có id này)");
                lblTen.setForeground(t != null ? ACCENT : WARN_RED);
            } catch (Exception ex) {
                lblTen.setText("—");
            }
        };
        fItem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                traTen.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                traTen.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                traTen.run();
            }
        });
        traTen.run();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, c, 0, "Boss ID:", fBoss);
        // O id kem nut chon: go id thi phai nho id, ma may chu co hon hai nghin
        // mau vat pham. Van cho go tay vi ai biet id thi go nhanh hon mo hop.
        // Tich vao thi moi lan boss chet boc ngau nhien trong bo Than Linh,
        // khong phai mot mon co dinh.
        javax.swing.JCheckBox cbThanLinh = new javax.swing.JCheckBox(
                "Đồ Thần Linh ngẫu nhiên (13 món của 3 hành tinh)",
                !them && intOf(dropModel.getValueAt(r, COT_ROI_ITEM))
                        == nro.repository.dao.BossDAO.ITEM_THAN_LINH_NGAU_NHIEN);
        JPanel pItem = new JPanel(new BorderLayout(6, 0));
        pItem.setOpaque(false);
        pItem.add(fItem, BorderLayout.CENTER);
        pItem.add(button("Chọn…", ACCENT, ev -> {
            int idChon = OptionPicker.chonVatPham(pItem, laySoAnToan(fItem.getText()));
            if (idChon >= 0) {
                fItem.setText(String.valueOf(idChon));
            }
        }), BorderLayout.EAST);
        addRowC(form, c, 1, "Vật phẩm:", pItem);

        // O tich nam NGAY DUOI hang "Vat pham" vi no quyet dinh hang do con
        // dung khong. Truoc do tôi dat o hang 8 — dung o ma dong goi y dang
        // chiem, nen GridBagLayout de mot cai de cai kia va o tich khong hien.
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        form.add(cbThanLinh, c);
        c.gridwidth = 1;
        // Boc ngau nhien thi o id va nut chon vo nghia -> khoa lai cho ro.
        Runnable batTatItem = () -> {
            boolean tl = cbThanLinh.isSelected();
            fItem.setEnabled(!tl);
            if (tl) {
                lblTen.setText("Mỗi lần rơi bốc lại — có thể ra 13 món khác nhau");
                lblTen.setForeground(ACCENT);
            }
        };
        cbThanLinh.addActionListener(ev -> batTatItem.run());
        batTatItem.run();

        c.gridx = 1;
        c.gridy = 3;
        form.add(lblTen, c);
        addRow(form, c, 4, "SL tối thiểu:", fMin);
        addRow(form, c, 5, "SL tối đa:", fMax);
        addRow(form, c, 6, "Tỉ lệ rơi (%):", fRate);
        addRow(form, c, 7, "HSD ít nhất (ngày):", fHsdMin);
        addRow(form, c, 8, "HSD nhiều nhất (ngày):", fHsdMax);
        addRow(form, c, 9, "Tỉ lệ vĩnh viễn (%):", fHsdVV);
        // Ba dieu kien deu de trong duoc; de trong het la ai giet cung roi.
        addRowC(form, c, 10, "Chỉ ở nhiệm vụ:",
                hangNhiemVu(nvDung, new JLabel()));
        addRowC(form, c, 11, "Từ nhiệm vụ trở đi:",
                hangNhiemVu(nvTu, new JLabel()));
        addRowC(form, c, 12, "Trước nhiệm vụ:",
                hangNhiemVu(nvDen, new JLabel()));
        JPanel pChiSo = new JPanel(new BorderLayout(6, 0));
        pChiSo.setOpaque(false);
        JLabel lblChiSo = new JLabel();
        Runnable veChiSo = () -> lblChiSo.setText(
                "[]".equals(chiSo[0]) || chiSo[0].isEmpty()
                        ? "(không có)" : chiSo[0]);
        veChiSo.run();
        pChiSo.add(lblChiSo, BorderLayout.CENTER);
        pChiSo.add(button("Sửa…", ACCENT, ev -> {
            String moi = suaChiSoNgauNhien(pChiSo, chiSo[0]);
            if (moi != null) {
                chiSo[0] = moi;
                veChiSo.run();
            }
        }), BorderLayout.EAST);
        addRowC(form, c, 13, "Chỉ số ngẫu nhiên:", pChiSo);
        addRow(form, c, 14, "Ghi chú:", fNote);
        c.gridx = 1;
        c.gridx = 1;
        c.gridy = 15;
        form.add(cbMotMon, c);
        c.gridx = 1;
        c.gridy = 16;
        form.add(cbOn, c);

        c.gridx = 0;
        c.gridy = 17;
        c.gridwidth = 2;
        JLabel hint = new JLabel("<html><span style='color:#777'>"
                + "Tỉ lệ <code>1</code> nghĩa là <b>1%</b> — trung bình 100 lần boss chết "
                + "thì rơi một lần. Cần hiếm hơn thì gõ số thập phân: <code>0.05</code>.<br>"
                + "<b>Đồ Thần Linh ngẫu nhiên</b>: mỗi lần rơi bốc lại trong 13 món "
                + "(áo/quần/găng/giày của 3 hành tinh + nhẫn). Số lượng <code>3</code> là "
                + "rơi <b>ba món riêng</b>, có thể khác nhau — không phải ba món giống hệt.<br>"
                + "Có hiệu lực <b>ngay</b>, không cần khởi động lại."
                + "</span></html>");
        form.add(hint, c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                them ? "Thêm món rơi" : "Sửa món rơi", true);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button(them ? "Thêm" : "Lưu", OK_GREEN, e -> {
            nro.repository.dao.BossDAO.Drop d = new nro.repository.dao.BossDAO.Drop();
            d.id = id;
            d.active = cbOn.isSelected();
            d.note = fNote.getText().trim();
            d.options = chiSo[0];
            try {
                d.bossId = Integer.parseInt(fBoss.getText().trim());
                d.itemId = cbThanLinh.isSelected()
                        ? nro.repository.dao.BossDAO.ITEM_THAN_LINH_NGAU_NHIEN
                        : Integer.parseInt(fItem.getText().trim());
                d.qtyMin = Integer.parseInt(fMin.getText().trim());
                d.qtyMax = Integer.parseInt(fMax.getText().trim());
                // Ti le go bang PHAN TRAM (1-100), nhan ca so thap phan.
                // Quy ve tu so tren mau 100.000 de giu duoc ba chu so thap phan
                // — hiem hon 1% la nhu cau thuc te voi do boss.
                double pct = Double.parseDouble(
                        fRate.getText().trim().replace(",", "."));
                if (pct < 0 || pct > 100) {
                    JOptionPane.showMessageDialog(dlg,
                            "Tỉ lệ rơi phải từ 0 đến 100 (%).");
                    return;
                }
                d.rateNum = (int) Math.round(pct * 1000);
                d.rateDen = 100_000;
                d.hsdMin = Integer.parseInt(fHsdMin.getText().trim());
                d.hsdMax = Integer.parseInt(fHsdMax.getText().trim());
                d.hsdVinhVien = Integer.parseInt(fHsdVV.getText().trim());
                // Vi tri i ung voi phase i + 1. Khong con "moi phase": moi phase
                // la mot bang roi rieng.
                d.phase = phaseLuu;
                d.nvDung = nvDung[0];
                d.nvTu = nvTu[0];
                d.nvDen = nvDen[0];
                d.chiMotMon = cbMotMon.isSelected();
                if (d.hsdVinhVien < 0 || d.hsdVinhVien > 100) {
                    JOptionPane.showMessageDialog(dlg,
                            "Tỉ lệ vĩnh viễn phải từ 0 đến 100 (%).");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Số lượng phải là số nguyên, tỉ lệ rơi phải là số từ 0 đến 100.");
                return;
            }
            String loi = nro.repository.dao.BossDAO.saveDrop(d);
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không lưu được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            loadDrops();
            loadBosses();
            LamMoi.bao(LamMoi.BOSS);
            note(OK_GREEN, them ? "Đã thêm món rơi — có hiệu lực ngay."
                    : "Đã sửa món rơi — có hiệu lực ngay.");
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

    private void dropDelete() {
        int[] rs = dropTable.getSelectedRows();
        if (rs.length == 0) {
            note(WARN_RED, "Chưa chọn dòng nào.");
            return;
        }
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        StringBuilder ten = new StringBuilder();
        for (int r : rs) {
            ids.add(intOf(dropModel.getValueAt(r, 0)));
            if (ten.length() < 200) {
                ten.append(ten.length() == 0 ? "" : ", ").append(dropModel.getValueAt(r, 4));
            }
        }
        if (ids.isEmpty()) {
            note(WARN_RED, "Chưa chọn dòng nào để xoá.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá " + ids.size() + " món rơi?\n\n" + ten
                + (ten.length() >= 200 ? " …" : ""),
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        int xong = 0;
        for (int id : ids) {
            if (nro.repository.dao.BossDAO.deleteDrop(id)) {
                xong++;
            }
        }
        note(xong == ids.size() ? OK_GREEN : WARN_RED,
                "Đã xoá " + xong + "/" + ids.size() + " món rơi.");
        loadDrops();
        loadBosses();
    }

    /** Đổi số giây thành chuỗi dễ đọc, ví dụ {@code "5 phút 30 giây"}. */
    private static String moTaThoiGian(long giay) {
        if (giay < 60) {
            return giay + " giây";
        }
        long p = giay / 60;
        long g = giay % 60;
        return g == 0 ? p + " phút" : p + " phút " + g + " giây";
    }

    /**
     * Hộp thoại tạo thêm một boss.
     *
     * <p><b>Chỉ tạo thêm bản sao của loại boss đã có.</b> Mỗi loại boss là một
     * lớp Java riêng và {@code BossManager.createBoss()} là một câu {@code switch}
     * 96 nhánh ánh xạ id sang lớp — không thể sinh ra một <i>loại</i> boss mới mà
     * không viết lớp mới. Nhưng tạo thêm bản sao thì được, và đó là việc thường
     * dùng: cùng một con boss xuất hiện ở nhiều khu cùng lúc.</p>
     *
     * <p>Bản sao mới nhận luôn phần đè chỉ số trong {@code boss_config} vì việc
     * đè nằm ở hàm dựng.</p>
     */
    private void bossSpawnDialog() {
        // Danh sach id lay tu chinh lop BossID bang phan chieu: no la mot dong
        // hang so `public static final int`, khong co cach nao khac de biet ten.
        java.util.List<String> tenId = new java.util.ArrayList<>();
        java.util.Map<String, Integer> theoTen = new java.util.LinkedHashMap<>();
        try {
            for (java.lang.reflect.Field f : nro.entity.boss.BossID.class.getFields()) {
                if (f.getType() == int.class) {
                    int v = f.getInt(null);
                    String nhan = f.getName() + "  (" + v + ")";
                    tenId.add(nhan);
                    theoTen.put(nhan, v);
                }
            }
        } catch (Exception ex) {
            note(WARN_RED, "Không đọc được danh sách BossID: " + ex.getMessage());
            return;
        }
        java.util.Collections.sort(tenId);

        JComboBox<String> cb = new JComboBox<>(tenId.toArray(new String[0]));
        cb.setEditable(false);
        JTextField fSo = new JTextField("1", 6);
        int dangChon = bossSelectedId();
        if (dangChon != Integer.MIN_VALUE) {
            for (String t : tenId) {
                if (theoTen.get(t) == dangChon) {
                    cb.setSelectedItem(t);
                    break;
                }
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Loại boss:"), c);
        c.gridx = 1;
        c.weightx = 1;
        cb.setPreferredSize(new Dimension(330, 26));
        form.add(cb, c);
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Số lượng:"), c);
        c.gridx = 1;
        form.add(fSo, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "Tạo thêm <b>bản sao</b> của loại boss đã có — hữu ích khi muốn cùng "
                + "một con boss xuất hiện ở nhiều khu.<br>"
                + "Một <i>loại</i> boss mới cần viết lớp Java, không tạo từ panel được.<br>"
                + "Bản sao mới nhận luôn phần đè chỉ số đang cấu hình."
                + "</span></html>"), c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner, "Thêm boss", true);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button("Tạo", OK_GREEN, e -> {
            int so;
            try {
                so = Integer.parseInt(fSo.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Số lượng phải là số nguyên.");
                return;
            }
            if (so <= 0 || so > 50) {
                JOptionPane.showMessageDialog(dlg, "Số lượng phải từ 1 đến 50.");
                return;
            }
            Integer bossId = theoTen.get(String.valueOf(cb.getSelectedItem()));
            if (bossId == null) {
                JOptionPane.showMessageDialog(dlg, "Chưa chọn loại boss.");
                return;
            }
            dlg.dispose();
            int truoc = bossModel.getRowCount();
            try {
                nro.service.boss.BossManager.gI().createBoss(bossId, so);
            } catch (Exception ex) {
                note(WARN_RED, "Không tạo được boss: " + ex.getMessage());
                return;
            }
            loadBosses();
            int them = bossModel.getRowCount() - truoc;
            // Bao con so THAT chu khong bao "da tao N": createBoss() tra ve null
            // cho nhung id khong co nhanh nao trong switch, va no nuot lỗi.
            note(them > 0 ? OK_GREEN : WARN_RED, them > 0
                    ? "Đã tạo thêm " + them + " boss (yêu cầu " + so + ")."
                    : "Không tạo được boss nào — id này không có trong "
                      + "BossManager.createBoss().");
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

    private static int intOf(Object o) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String nz(Object o) {
        return o == null || "null".equals(String.valueOf(o)) ? "" : String.valueOf(o);
    }

    private static String blankToNull(String s) {
        String t = s == null ? "" : s.trim();
        return t.isEmpty() ? null : t;
    }

    // =====================================================================
    //  Set kích hoạt
    // =====================================================================

    /** Bộ lọc hành tinh của danh sách set. */
    private final JComboBox<String> cbHanhTinh = new JComboBox<>();
    private final JTextField fTimSet = new JTextField(14);

    /** Danh sách set bên trái. Cột cuối giữ mã set, ẩn khỏi bảng. */
    private final DefaultTableModel dsSetModel = new DefaultTableModel(
            new Object[]{"Set", "Hành tinh", "Số dòng", "Bật", "__key"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable dsSetTable = new JTable(dsSetModel);

    /**
     * Bảng chỉ số của set đang chọn.
     *
     * <p>Gộp <b>hai nguồn</b>: dòng viết cứng trong mã nguồn (chỉ xem) và dòng
     * cấu hình trong CSDL (sửa được). Cột "Nguồn" phân biệt. Trước đây chỉ số
     * gốc nằm trong một dòng chữ chạy ngang phía trên, không đối chiếu được với
     * dòng cấu hình ngay bên dưới.</p>
     */
    private final DefaultTableModel setModel = new DefaultTableModel(
            new Object[]{"ID", "Nguồn", "Từ mấy món", "Loại chỉ số", "Giá trị",
                "Tham số", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable setTable = new JTable(setModel);

    /** Nhãn tên set đang chọn, trên bảng chỉ số. */
    private final JLabel lblGoc = new JLabel();

    /** Câu tóm tắt set đang chọn cho gì, đặt ngay dưới tên set. */
    private final JLabel lblTacDung = new JLabel();

    /**
     * Tab sửa chỉ số của set kích hoạt.
     *
     * <h3>Trước đây các chỉ số này ở đâu</h3>
     *
     * <p>Viết cứng rải rác trong {@code NPoint.java} — khoảng hai mươi khối kiểu
     * {@code if (setClothes.cumber == 5) { dame += calPercent(dame, 20); }}. Muốn
     * đổi phải sửa mã nguồn rồi biên dịch lại, và phải tìm cho hết vì cùng một
     * set nằm ở nhiều chỗ.</p>
     *
     * <h3>Ô "Thay thế hẳn"</h3>
     *
     * <p>Đây là chỗ quan trọng nhất. <b>Tắt</b> thì các dòng ở đây <i>cộng thêm</i>
     * vào phần viết cứng. <b>Bật</b> thì toàn bộ hiệu ứng viết cứng của set đó bị
     * bỏ, chỉ còn những dòng ở đây — nhờ vậy mới đổi được "+100% HP" thành
     * "+100% sức đánh" chứ không phải được cả hai.</p>
     *
     * <p>Có hiệu lực ở lần tính chỉ số kế tiếp (thay đồ, vào map, ăn buff) —
     * không cần khởi động lại máy chủ.</p>
     */
    // =====================================================================
    //  Tỉ lệ rơi & kinh nghiệm
    // =====================================================================

    private final Map<String, JTextField> oTiLe = new LinkedHashMap<>();

    /**
     * Tab chỉnh tỉ lệ rơi đồ, vàng và kinh nghiệm.
     *
     * <h3>Vì sao tách khỏi tab "Quy ước"</h3>
     *
     * <p>Tab Quy ước liệt kê mọi khoá cấu hình theo đúng tên khoá trong CSDL —
     * tiện để sửa nhanh nhưng không nói được ý nghĩa. Những con số ở đây thì
     * <b>đổi trực tiếp cảm giác chơi</b>, nên đáng có chỗ riêng ghi rõ 100 nghĩa
     * là gì và số lớn hơn thì hiếm hay dễ.</p>
     *
     * <h3>Đồ set kích hoạt (vải thô)</h3>
     *
     * <p>Dòng gọi hàm thả đồ này <b>đang bị tắt bằng chú thích</b> trong
     * {@code Mob.java} từ trước, nên trên máy chủ này nó chưa bao giờ rơi. Nay
     * bật/tắt bằng ô bên dưới.</p>
     */
    private JComponent buildTiLeTab() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        y = nhomTiLe(form, c, y, "Hệ số — thang 1 đến 100 lần, 1 là y như gốc");
        y = oChu(form, c, y, ConfigDAO.TL_EXP, "Tiềm năng (kinh nghiệm):");
        y = oNhan(form, c, y, ConfigDAO.TL_EXP);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "<b>Hệ số</b> nhận số thập phân: <code>0.5</code> là còn một nửa, "
                + "<code>10</code> là gấp mười. Để <code>1</code> là y như gốc.<br><br>"
                + "Đây là hệ số <b>chung cho mọi bản đồ</b>, và nó <b>nhân lên trên</b> "
                + "hệ số riêng của từng bản đồ chứ không thay thế. Bản đồ vốn ×1 với "
                + "hệ số <code>0.2</code> thì thành ×0,2; bản đồ vốn ×3 thì thành ×0,6. "
                + "Đúng thứ tự ấy vì hệ số này được nhân <i>sau cùng</i>, khi con số "
                + "tiềm năng đã tính xong mọi thứ khác.<br><br>"
                + "<b>Đồ set kích hoạt đã chuyển sang tab \"Đồ rơi từ quái\".</b> "
                + "Ở đó mở hộp <i>Thêm món rơi</i> rồi tích "
                + "<b>\"Món này là đồ set kích hoạt\"</b>: mỗi dòng tự chọn set, "
                + "tỉ lệ, số lượng, khoảng sao pha lê và phạm vi bản đồ riêng.<br>"
                + "Đặt ở đây thì mọi dòng phải dùng chung một con số, mà bản đồ "
                + "cũng chỉ khai được một danh sách duy nhất."
                + "</span></html>"), c);

        JPanel nam = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nam.setOpaque(false);
        nam.add(button("Lưu", OK_GREEN, e -> luuTiLe()));
        nam.add(button("Tải lại", GREY, e -> napTiLe()));
        nam.add(button("Về mặc định", new Color(120, 90, 160), e -> macDinhTiLe()));

        wrap.add(form, BorderLayout.NORTH);
        wrap.add(nam, BorderLayout.CENTER);
        napTiLe();

        JTabbedPane trong = new JTabbedPane();
        trong.addTab("1. Hệ số chung", ServerGuiUtils.cuon(wrap));
        trong.addTab("2. Theo bản đồ", buildHeSoTnsm());
        trong.addTab("3. Càng mạnh càng giảm", buildBacGiam());
        return trong;
    }

    // =====================================================================
    //  Tỉ lệ — 2. Hệ số tiềm năng theo bản đồ
    // =====================================================================

    private static final int COT_HS_ID = 0;
    private static final int COT_HS_TEN = 1;
    private static final int COT_HS_MAP = 2;
    private static final int COT_HS_HS = 3;
    private static final int COT_HS_HSDT = 4;
    private static final int COT_HS_CHIDT = 5;
    private static final int COT_HS_GAP = 6;
    private static final int COT_HS_BAT = 7;
    private static final int COT_HS_GC = 8;

    private final DefaultTableModel hsModel = new DefaultTableModel(
            new Object[]{"Id", "Nhóm bản đồ", "Các bản đồ trong nhóm",
                "Hệ số", "Hệ số đệ tử", "Chỉ đệ tử", "Một con quái cho",
                "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c != COT_HS_ID && c != COT_HS_GAP;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return (c == COT_HS_CHIDT || c == COT_HS_BAT)
                    ? Boolean.class : String.class;
        }
    };
    private final JTable hsTable = new JTable(hsModel);

    /** Ô "giá trị đúng ra nhận được" — tiềm năng gốc trước mọi hệ số. */
    private final JTextField hsOGoc = new JTextField("1000", 10);

    /**
     * Bảng hệ số tiềm năng theo <b>nhóm bản đồ</b>.
     *
     * <h3>Vì sao có bảng này</h3>
     *
     * <p>Trước đây mấy con số ấy nằm rải ba nơi: {@code NPoint} nhân 6 cho Bản đồ
     * kho báu, nhân 3 cho Doanh trại, nhân 2 cho Khu vực thám hiểm và chia 10 cho
     * sáu bản đồ; {@code Mob} có phép chia riêng cho Ngũ Hành Sơn. Muốn biết một
     * bản đồ cho tiềm năng gấp mấy lần bản đồ thường thì phải đọc cả ba chỗ rồi
     * tự nhân tay.</p>
     *
     * <h3>Ô "giá trị đúng ra nhận được"</h3>
     *
     * <p>Hệ số đứng một mình không nói được gì: <code>0.2</code> nhân với ×6 ra
     * bao nhiêu tiềm năng thật thì vẫn phải nhẩm. Gõ một con số vào ô ấy là cả
     * cột bên phải đổi theo, hiện thẳng số tiềm năng người chơi cầm về ở từng
     * nhóm bản đồ.</p>
     */
    private JComponent buildHeSoTnsm() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        hsTable.setRowHeight(24);
        hsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {36, 160, 210, 62, 82, 62, 190, 40, 240};
        for (int i = 0; i < hsTable.getColumnCount() && i < w.length; i++) {
            hsTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel tren = new JPanel(new BorderLayout(0, 4));
        tren.setOpaque(false);
        tren.add(nhan("Tiềm năng của <b>một nhóm bản đồ</b> so với bản đồ thường."
                + "<br><br>"
                + "<b>Các bản đồ trong nhóm</b>: id đơn hoặc khoảng, ngăn nhau bằng "
                + "dấu phẩy — <code>68-72,102,103</code>. Thêm hay bớt một bản đồ khỏi "
                + "nhóm chỉ là sửa ô ấy.<br>"
                + "<b>Hệ số đệ tử</b> để trống là dùng chung hệ số bên trái. "
                + "<b>Chỉ đệ tử</b> bật thì người chơi thường đánh trong nhóm ấy "
                + "<b>không được tiềm năng</b>, chỉ đệ tử mới có — Ngũ Hành Sơn đang "
                + "đặt như thế.<br>"
                + "Một bản đồ khớp nhiều nhóm thì lấy <b>nhóm đầu tiên</b> rồi dừng, "
                + "không nhân dồn."), BorderLayout.NORTH);

        JPanel oGoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        oGoc.setOpaque(false);
        oGoc.add(new JLabel("Giá trị đúng ra nhận được:"));
        oGoc.add(hsOGoc);
        oGoc.add(new JLabel("<html><span style='color:#777'>tiềm năng gốc của "
                + "một con quái, <b>trước</b> khi nhân hệ số chung "
                + "(" + soGonHs(ConfigDAO.phanTram(ConfigDAO.TL_EXP)) + "). "
                + "Cột \"Một con quái cho\" tính từ chính con số này."
                + "</span></html>"));
        hsOGoc.addCaretListener(e -> capNhatCotGap());
        // Sua he so xong la thay ket qua ngay. Bo qua chinh cot xem truoc,
        // khong thi no tu goi lai chinh no khong dut.
        hsModel.addTableModelListener(e -> {
            if (e.getColumn() != COT_HS_GAP
                    && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                SwingUtilities.invokeLater(this::capNhatCotGap);
            }
        });
        tren.add(oGoc, BorderLayout.SOUTH);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        nut.setOpaque(false);
        nut.add(button("Lưu bảng", OK_GREEN, e -> luuBangHeSo()));
        nut.add(button("Tải lại", GREY, e -> napBangHeSo()));
        nut.add(button("Thêm nhóm", ACCENT, e -> themNhomHeSo()));
        nut.add(button("Về mặc định", new Color(120, 90, 160), e -> {
            if (JOptionPane.showConfirmDialog(this,
                    "Xoá sạch bảng rồi gieo lại các nhóm gốc?",
                    "Về mặc định", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            String loi = HeSoTnsmDAO.gieoLai();
            napBangHeSo();
            note(loi == null ? OK_GREEN : WARN_RED,
                    loi == null ? "Đã gieo lại các nhóm gốc." : loi);
        }));

        root.add(tren, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(hsTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangHeSo();
        return root;
    }

    /** Con số trong ô "giá trị đúng ra nhận được", tối thiểu 0. */
    private double tnGoc() {
        try {
            double v = Double.parseDouble(hsOGoc.getText().trim().replace(',', '.'));
            return v < 0 ? 0 : v;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** Một con quái trong nhóm này cho bao nhiêu tiềm năng, tính cả hệ số chung. */
    private String motConQuaiCho(double heSo, double heSoDeTu, boolean chiDeTu,
            boolean bat) {
        if (!bat) {
            return "— đang tắt, tính như bản đồ thường";
        }
        double goc = tnGoc();
        double chung = ConfigDAO.phanTram(ConfigDAO.TL_EXP);
        if (chung <= 0) {
            chung = 1;
        }
        double dt = heSoDeTu > 0 ? heSoDeTu : heSo;
        String thuong = chiDeTu ? "0 (bị khoá)"
                : PlayerManagerPanel.fmt(Math.round(goc * heSo * chung));
        return "thường " + thuong + " · đệ tử "
                + PlayerManagerPanel.fmt(Math.round(goc * dt * chung));
    }

    /**
     * In hệ số gọn: bỏ đuôi ,0 và cắt còn ba chữ số thập phân.
     *
     * <p>Ba chữ số chứ không phải hai, vì Ngũ Hành Sơn là <b>một phần ba</b> —
     * cắt còn hai chữ số thì lưu lại thành 0,33 và hệ số tụt đi thật.</p>
     */
    private static String soGonHs(double v) {
        String s = String.format(java.util.Locale.US, "%.3f", v);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void napBangHeSo() {
        if (hsTable.isEditing()) {
            hsTable.getCellEditor().stopCellEditing();
        }
        hsModel.setRowCount(0);
        for (HeSoTnsmDAO.Dong d : HeSoTnsmDAO.danhSach()) {
            hsModel.addRow(new Object[]{String.valueOf(d.id), d.ten,
                d.dsMap == null ? "" : d.dsMap,
                soGonHs(d.heSo), d.heSoDeTu <= 0 ? "" : soGonHs(d.heSoDeTu),
                d.chiDeTu, motConQuaiCho(d.heSo, d.heSoDeTu, d.chiDeTu, d.bat),
                d.bat, d.ghiChu == null ? "" : d.ghiChu});
        }
    }

    /**
     * Tính lại cột "Một con quái cho" từ chính những gì đang gõ trong bảng.
     *
     * <p>Đọc ô trong bảng chứ không đọc CSDL — người dùng sửa hệ số xong là thấy
     * ngay kết quả, không phải bấm Lưu rồi mới biết mình vừa đặt cái gì.</p>
     */
    private void capNhatCotGap() {
        for (int r = 0; r < hsModel.getRowCount(); r++) {
            double hs;
            double hsdt;
            try {
                hs = Double.parseDouble(oHs(r, COT_HS_HS).replace(',', '.'));
                String dt = oHs(r, COT_HS_HSDT).replace(',', '.');
                hsdt = dt.isEmpty() ? 0 : Double.parseDouble(dt);
            } catch (NumberFormatException ex) {
                hsModel.setValueAt("hệ số không phải số", r, COT_HS_GAP);
                continue;
            }
            Object cdt = hsModel.getValueAt(r, COT_HS_CHIDT);
            Object bat = hsModel.getValueAt(r, COT_HS_BAT);
            hsModel.setValueAt(motConQuaiCho(hs, hsdt,
                    (cdt instanceof Boolean) && (Boolean) cdt,
                    !(bat instanceof Boolean) || (Boolean) bat), r, COT_HS_GAP);
        }
    }

    private String oHs(int r, int c) {
        Object v = hsModel.getValueAt(r, c);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private void luuBangHeSo() {
        if (hsTable.isEditing()) {
            hsTable.getCellEditor().stopCellEditing();
        }
        java.util.List<HeSoTnsmDAO.Dong> cu = HeSoTnsmDAO.danhSach();
        int hong = 0;
        String hongDau = null;
        for (int r = 0; r < hsModel.getRowCount() && r < cu.size(); r++) {
            HeSoTnsmDAO.Dong d = cu.get(r);
            try {
                d.heSo = Double.parseDouble(oHs(r, COT_HS_HS).replace(',', '.'));
                String dt = oHs(r, COT_HS_HSDT).replace(',', '.');
                d.heSoDeTu = dt.isEmpty() ? 0 : Double.parseDouble(dt);
            } catch (NumberFormatException ex) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Nhóm \"" + oHs(r, COT_HS_TEN) + "\" có hệ số không phải số.";
                }
                continue;
            }
            d.ten = oHs(r, COT_HS_TEN);
            d.dsMap = oHs(r, COT_HS_MAP);
            Object cdt = hsModel.getValueAt(r, COT_HS_CHIDT);
            d.chiDeTu = (cdt instanceof Boolean) && (Boolean) cdt;
            Object bat = hsModel.getValueAt(r, COT_HS_BAT);
            d.bat = !(bat instanceof Boolean) || (Boolean) bat;
            d.ghiChu = oHs(r, COT_HS_GC);
            String loi = HeSoTnsmDAO.luu(d);
            if (loi != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = loi;
                }
            }
        }
        napBangHeSo();
        note(hong == 0 ? OK_GREEN : WARN_RED, hong == 0
                ? "Đã lưu hệ số tiềm năng theo bản đồ — có hiệu lực ngay."
                : hongDau + " (" + hong + " nhóm không lưu được)");
    }

    private void themNhomHeSo() {
        String ten = JOptionPane.showInputDialog(this,
                "Tên nhóm bản đồ mới?\n\n"
                + "Thêm xong thì điền danh sách id bản đồ và hệ số vào bảng rồi bấm Lưu.",
                "Thêm nhóm bản đồ", JOptionPane.QUESTION_MESSAGE);
        if (ten == null || ten.trim().isEmpty()) {
            return;
        }
        HeSoTnsmDAO.Dong d = new HeSoTnsmDAO.Dong();
        d.khoa = "nhom_" + System.currentTimeMillis();
        d.ten = ten.trim();
        d.dsMap = "";
        d.heSo = 1;
        d.thuTu = 100;
        d.bat = true;
        String loi = HeSoTnsmDAO.luu(d);
        napBangHeSo();
        note(loi == null ? OK_GREEN : WARN_RED,
                loi == null ? "Đã thêm nhóm — điền id bản đồ rồi bấm Lưu." : loi);
    }

    // =====================================================================
    //  Tỉ lệ — 3. Sức mạnh càng cao, tiềm năng càng ít
    // =====================================================================

    private static final int COT_BG_MOC = 0;
    private static final int COT_BG_CON = 1;
    private static final int COT_BG_BAT = 2;
    private static final int COT_BG_XEM = 3;
    private static final int COT_BG_GC = 4;

    private final DefaultTableModel bgModel = new DefaultTableModel(
            new Object[]{"Từ mốc sức mạnh", "Còn nhận (%)", "Bật",
                "Một con quái cho", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c != COT_BG_XEM;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == COT_BG_BAT ? Boolean.class : String.class;
        }
    };
    private final JTable bgTable = new JTable(bgModel);

    /** Mốc gốc của từng dòng lúc nạp — để biết dòng nào vừa bị đổi mốc. */
    private final java.util.List<Long> bgMocCu = new ArrayList<>();

    /**
     * Bảng "càng mạnh càng nhận ít tiềm năng".
     *
     * <p>Mười một con số này quyết định người chơi cày bao lâu thì chững lại, tức
     * là quyết định nhịp của cả máy chủ — nhưng trước đây chúng nằm cứng trong
     * {@code NPoint}, muốn nới một bậc phải sửa mã rồi dịch lại.</p>
     *
     * <p>Đổi <b>mốc</b> của một dòng là xoá dòng cũ rồi ghi dòng mới, vì mốc chính
     * là khoá của bảng. Cột "Một con quái cho" dùng chung ô "giá trị đúng ra nhận
     * được" ở tab bên cạnh, để hai bảng nói cùng một con số.</p>
     */
    private JComponent buildBacGiam() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        bgTable.setRowHeight(24);
        bgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {150, 100, 45, 170, 300};
        for (int i = 0; i < bgTable.getColumnCount() && i < w.length; i++) {
            bgTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        nut.setOpaque(false);
        nut.add(button("Lưu bảng", OK_GREEN, e -> luuBacGiam()));
        nut.add(button("Tải lại", GREY, e -> napBacGiam()));
        nut.add(button("Thêm bậc", ACCENT, e -> themBacGiam()));
        nut.add(button("Xoá bậc", WARN_RED, e -> xoaBacGiam()));
        nut.add(button("Về mặc định", new Color(120, 90, 160), e -> {
            if (JOptionPane.showConfirmDialog(this,
                    "Xoá sạch bảng rồi gieo lại mười một bậc gốc?",
                    "Về mặc định", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            String loi = BacGiamTnsmDAO.gieoLai();
            napBacGiam();
            note(loi == null ? OK_GREEN : WARN_RED,
                    loi == null ? "Đã gieo lại các bậc gốc." : loi);
        }));

        root.add(nhan("Sức mạnh càng cao thì tiềm năng nhận được càng ít. Đọc một "
                + "dòng là: <b>từ mốc sức mạnh này trở lên thì chỉ còn nhận ngần "
                + "này phần trăm</b>."
                + "<br><br>"
                + "Lấy <b>bậc cao nhất người chơi đạt tới</b> rồi dừng, không cộng "
                + "dồn nhiều bậc. Dưới bậc thấp nhất là nhận nguyên vẹn 100%. Tắt "
                + "hết mọi dòng thì mạnh yếu nhận như nhau."
                + "<br>"
                + "Bảng áp cho <b>cả sư phụ lẫn đệ tử</b>, mỗi bên tra bằng sức mạnh "
                + "của chính mình — sư phụ mạnh thì phần chia của sư phụ ít đi, "
                + "không ăn theo bậc của đệ."
                + "<br><br>"
                + "Mốc gõ được cả <code>5000000000</code> lẫn <code>5.000.000.000</code>. "
                + "Đổi mốc của một dòng thì dòng cũ bị xoá và ghi lại thành dòng mới."),
                BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(bgTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBacGiam();
        return root;
    }

    /** Một con quái cho bao nhiêu tiềm năng ở bậc này — bản đồ thường. */
    private String bacGiamCho(int conLai, boolean bat) {
        if (!bat) {
            return "— đang tắt";
        }
        double chung = ConfigDAO.phanTram(ConfigDAO.TL_EXP);
        if (chung <= 0) {
            chung = 1;
        }
        return PlayerManagerPanel.fmt(
                Math.round(tnGoc() * chung * conLai / 100.0));
    }

    private void napBacGiam() {
        if (bgTable.isEditing()) {
            bgTable.getCellEditor().stopCellEditing();
        }
        bgModel.setRowCount(0);
        bgMocCu.clear();
        for (BacGiamTnsmDAO.Dong d : BacGiamTnsmDAO.danhSach()) {
            bgMocCu.add(d.moc);
            bgModel.addRow(new Object[]{PlayerManagerPanel.fmt(d.moc),
                String.valueOf(d.conLai), d.bat,
                bacGiamCho(d.conLai, d.bat), d.ghiChu == null ? "" : d.ghiChu});
        }
    }

    /** Đọc một con số có thể đang mang dấu chấm hay dấu phẩy phân nhóm. */
    private static long soLon(String s) {
        return Long.parseLong(s.replaceAll("[^0-9-]", ""));
    }

    private String oBg(int r, int c) {
        Object v = bgModel.getValueAt(r, c);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private void luuBacGiam() {
        if (bgTable.isEditing()) {
            bgTable.getCellEditor().stopCellEditing();
        }
        int hong = 0;
        String hongDau = null;
        for (int r = 0; r < bgModel.getRowCount(); r++) {
            BacGiamTnsmDAO.Dong d = new BacGiamTnsmDAO.Dong();
            try {
                d.moc = soLon(oBg(r, COT_BG_MOC));
                d.conLai = (int) soLon(oBg(r, COT_BG_CON));
            } catch (NumberFormatException ex) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Dòng " + (r + 1) + " có mốc hoặc phần trăm không phải số.";
                }
                continue;
            }
            Object bat = bgModel.getValueAt(r, COT_BG_BAT);
            d.bat = !(bat instanceof Boolean) || (Boolean) bat;
            d.ghiChu = oBg(r, COT_BG_GC);
            // Moc la khoa cua bang: doi moc thi phai bo dong cu di, khong thi
            // bang co ca moc cu lan moc moi va nguoi choi dinh bac khong ai dat.
            Long cu = r < bgMocCu.size() ? bgMocCu.get(r) : null;
            if (cu != null && cu != d.moc) {
                BacGiamTnsmDAO.xoa(cu);
            }
            String loi = BacGiamTnsmDAO.luu(d);
            if (loi != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Dòng " + (r + 1) + ": " + loi;
                }
            }
        }
        napBacGiam();
        note(hong == 0 ? OK_GREEN : WARN_RED, hong == 0
                ? "Đã lưu bậc giảm theo sức mạnh — có hiệu lực ngay."
                : hongDau + " (" + hong + " dòng không lưu được)");
    }

    private void themBacGiam() {
        String s = JOptionPane.showInputDialog(this,
                "Mốc sức mạnh của bậc mới?\n\n"
                + "Từ mốc này trở lên thì người chơi chỉ còn nhận phần trăm\n"
                + "tiềm năng ghi ở cột bên cạnh.",
                "Thêm bậc", JOptionPane.QUESTION_MESSAGE);
        if (s == null || s.trim().isEmpty()) {
            return;
        }
        BacGiamTnsmDAO.Dong d = new BacGiamTnsmDAO.Dong();
        try {
            d.moc = soLon(s);
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Mốc sức mạnh phải là số.");
            return;
        }
        d.conLai = 100;
        d.bat = true;
        String loi = BacGiamTnsmDAO.luu(d);
        napBacGiam();
        note(loi == null ? OK_GREEN : WARN_RED,
                loi == null ? "Đã thêm bậc — sửa phần trăm rồi bấm Lưu." : loi);
    }

    private void xoaBacGiam() {
        int r = bgTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng đã.");
            return;
        }
        long moc;
        try {
            moc = soLon(oBg(r, COT_BG_MOC));
        } catch (NumberFormatException ex) {
            moc = r < bgMocCu.size() ? bgMocCu.get(r) : 0;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá bậc từ " + PlayerManagerPanel.fmt(moc) + " sức mạnh?",
                "Xoá bậc", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = BacGiamTnsmDAO.xoa(moc);
        napBacGiam();
        note(loi == null ? OK_GREEN : WARN_RED,
                loi == null ? "Đã xoá bậc." : loi);
    }

    private int nhomTiLe(JPanel form, GridBagConstraints c, int y, String ten) {
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        c.weightx = 1;
        JLabel l = new JLabel(ten);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(ACCENT);
        l.setBorder(new EmptyBorder(10, 0, 2, 0));
        form.add(l, c);
        c.gridwidth = 1;
        return y + 1;
    }

    private int oTiLe(JPanel form, GridBagConstraints c, int y, String khoa, String nhan) {
        JTextField f = new JTextField(12);
        oTiLe.put(khoa, f);
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 0;
        form.add(new JLabel(nhan), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(f, c);
        return y + 1;
    }

    /** Các ô nhập ở tab Tỉ lệ đang là quy ước dạng chữ. */
    private final java.util.Set<String> oTiLeChu = new java.util.HashSet<>();

    /** Nhãn quy đổi dưới mỗi ô tỉ lệ, tra theo khoá. */
    private final Map<String, JLabel> nhanQuyDoi = new LinkedHashMap<>();

    /**
     * Dòng quy đổi tỉ lệ phần trăm ra thứ đọc được: bao nhiêu con quái mới rơi
     * một món.
     *
     * <p>Phần trăm nhỏ rất khó hình dung — <b>0,011%</b> và <b>1%</b> nhìn na ná
     * nhau nhưng lệch nhau chín mươi lần. Dòng này cập nhật ngay khi gõ nên đặt
     * số xong là biết mình vừa đặt cái gì.</p>
     */
    private int oQuyDoi(JPanel form, GridBagConstraints c, int y, String khoa) {
        JLabel l = new JLabel();
        l.setForeground(new Color(120, 90, 160));
        nhanQuyDoi.put(khoa, l);
        JTextField f = oTiLe.get(khoa);
        if (f != null) {
            f.addCaretListener(e -> capNhatQuyDoi(khoa));
        }
        c.gridx = 1;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 1;
        form.add(l, c);
        capNhatQuyDoi(khoa);
        return y + 1;
    }

    /** Khoá nào hiện quy đổi kiểu "gấp mấy lần" thay vì "bao nhiêu con quái". */
    private final java.util.Set<String> quyDoiHeSo = new java.util.HashSet<>();

    /**
     * Một loại đồ rơi với tỉ lệ nền {@code 1/mauSo}, sau khi nhân hệ số.
     *
     * @return chuỗi kiểu {@code "1/70 (1,4%)"}
     */
    private static String tiLeRoi(int mauSo, double heSo) {
        double coHoi = heSo / mauSo;
        if (coHoi <= 0) {
            return "không rơi";
        }
        if (coHoi >= 1) {
            return "luôn rơi";
        }
        long mau = Math.round(1.0 / coHoi);
        return "1/" + PlayerManagerPanel.fmt(mau)
                + String.format(" (%.2f%%)", coHoi * 100);
    }

    /**
     * Phần trăm số quái rơi ra <b>ít nhất một</b> món trong ba loại rơi chung.
     *
     * <p>Không cộng ba tỉ lệ lại — ba phép thử độc lập nên phải lấy phần bù:
     * {@code 1 - (1-a)(1-b)(1-c)}. Cộng thẳng cho ra con số to hơn thực tế.</p>
     */
    private static String phanTramRoiBatKy(double heSo) {
        double khong = 1;
        for (int mau : new int[]{7, 25, 200}) {
            double p = Math.min(1, heSo / mau);
            khong *= (1 - p);
        }
        return String.format("%.1f%%", (1 - khong) * 100);
    }

    /** Dòng quy đổi cho ô hệ số: nói thẳng gấp mấy lần so với gốc. */
    private int oNhan(JPanel form, GridBagConstraints c, int y, String khoa) {
        quyDoiHeSo.add(khoa);
        return oQuyDoi(form, c, y, khoa);
    }

    private void capNhatQuyDoi(String khoa) {
        JLabel l = nhanQuyDoi.get(khoa);
        JTextField f = oTiLe.get(khoa);
        if (l == null || f == null) {
            return;
        }
        double p;
        try {
            p = Double.parseDouble(f.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            l.setText("  (chưa phải số)");
            return;
        }
        if (quyDoiHeSo.contains(khoa)) {
            double h = p <= 0 ? 1 : p;
            String gap = p <= 0 ? "  (0 hoặc bỏ trống = giữ nguyên như gốc)"
                    : p == 1 ? "  = y như gốc"
                    : "  = " + (p == Math.floor(p) ? String.valueOf((long) p)
                            : String.valueOf(p)) + " lần so với gốc";
            l.setText(gap);
            return;
        }
        if (p <= 0) {
            l.setText("  (0 = tắt hẳn, không rơi)");
            return;
        }
        long cu = Math.round(100.0 / p);
        l.setText("  ≈ trung bình " + PlayerManagerPanel.fmt(cu)
                + " con quái mới rơi một món (ở mức may mắn gốc)");
    }

    /** Các công tắc bật/tắt ở tab Tỉ lệ, tra theo khoá. */
    private final Map<String, JButton> nutBat = new LinkedHashMap<>();
    private final Map<String, Boolean> trangThaiBat = new LinkedHashMap<>();

    /**
     * Một công tắc <b>BẬT / TẮT</b> thay cho ô gõ 1 hoặc 0.
     *
     * <p>Gõ số vào ô để bật một tính năng là chỗ dễ sai mà không báo: gõ 2, gõ
     * chữ, hay để trống đều ra kết quả khó đoán. Nút thì chỉ có hai trạng thái
     * và tự nói ra mình đang ở trạng thái nào.</p>
     */
    private int oBat(JPanel form, GridBagConstraints c, int y, String khoa, String nhan) {
        JButton b = new JButton();
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.addActionListener(e -> {
            datBat(khoa, !Boolean.TRUE.equals(trangThaiBat.get(khoa)));
        });
        nutBat.put(khoa, b);
        datBat(khoa, ConfigDAO.on(khoa));

        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 0;
        form.add(new JLabel(nhan), c);
        c.gridx = 1;
        c.weightx = 1;
        JPanel goi = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        goi.setOpaque(false);
        goi.add(b);
        form.add(goi, c);
        return y + 1;
    }

    private void datBat(String khoa, boolean bat) {
        trangThaiBat.put(khoa, bat);
        JButton b = nutBat.get(khoa);
        if (b != null) {
            b.setText(bat ? "BẬT" : "TẮT");
            b.setBackground(bat ? OK_GREEN : new Color(150, 150, 150));
        }
    }

    private int oChu(JPanel form, GridBagConstraints c, int y, String khoa, String nhan) {
        oTiLeChu.add(khoa);
        return oTiLe(form, c, y, khoa, nhan);
    }

    private void napTiLe() {
        ConfigDAO.reload();
        for (Map.Entry<String, JTextField> e : oTiLe.entrySet()) {
            e.getValue().setText(oTiLeChu.contains(e.getKey())
                    ? ConfigDAO.chuoi(e.getKey())
                    : String.valueOf(ConfigDAO.num(e.getKey())));
        }
        for (String k : nutBat.keySet()) {
            datBat(k, ConfigDAO.on(k));
        }
        for (String k : nhanQuyDoi.keySet()) {
            capNhatQuyDoi(k);
        }
        note(GREY, "Đã đọc lại tỉ lệ từ CSDL.");
    }

    /**
     * Lưu cả nhóm tỉ lệ.
     *
     * <p>Kiểm tra <b>toàn bộ ô</b> trước khi ghi ô đầu tiên: ghi được nửa chừng
     * rồi báo lỗi để lại trạng thái nửa cũ nửa mới, khó lần ra hơn nhiều so với
     * việc từ chối cả lượt.</p>
     */
    private void luuTiLe() {
        for (Map.Entry<String, JTextField> e : oTiLe.entrySet()) {
            String raw = e.getValue().getText().trim();
            if (oTiLeChu.contains(e.getKey())) {
                // O phan tram: GIU dau cham thap phan, chi doi dau phay thanh cham.
                try {
                    double p = Double.parseDouble(raw.replace(",", "."));
                    if (p < 0 || p > 100) {
                        note(WARN_RED, "Ô \"" + e.getKey()
                                + "\" phải từ 0 đến 100 (0 là tắt hẳn).");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    note(WARN_RED, "Ô \"" + e.getKey()
                            + "\" phải là số phần trăm, ví dụ 0.011");
                    return;
                }
                continue;
            }
            String v = raw.replace(".", "").replace(",", "");
            try {
                long n = Long.parseLong(v);
                if (n < 0) {
                    note(WARN_RED, "Ô \"" + e.getKey() + "\" không được âm.");
                    return;
                }
            } catch (NumberFormatException ex) {
                note(WARN_RED, "Ô \"" + e.getKey() + "\" phải là số nguyên.");
                return;
            }
        }
        int loi = 0;
        for (Map.Entry<String, Boolean> e : trangThaiBat.entrySet()) {
            if (!ConfigDAO.set(e.getKey(), e.getValue() ? "1" : "0")) {
                loi++;
            }
        }
        for (Map.Entry<String, JTextField> e : oTiLe.entrySet()) {
            String raw = e.getValue().getText().trim();
            String v = oTiLeChu.contains(e.getKey())
                    ? raw.replace(",", ".")
                    : raw.replace(".", "").replace(",", "");
            if (!ConfigDAO.set(e.getKey(), v)) {
                loi++;
            }
        }
        ConfigDAO.reload();
        napTiLe();
        note(loi == 0 ? OK_GREEN : WARN_RED, loi == 0
                ? "Đã lưu tỉ lệ và áp dụng ngay — không cần khởi động lại."
                : "Có " + loi + " ô không lưu được — xem log máy chủ.");
    }

    private void macDinhTiLe() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Đặt lại toàn bộ tỉ lệ về mặc định?" + "\n" + "\n"
                + "Tiềm năng / vàng / cơ hội rơi = 1 (y như gốc), rơi đồ set kích "
                + "hoạt TẮT, tỉ lệ 1%, danh sách map để trống, sao pha lê TẮT "
                + "(tỉ lệ 10%, 1–2 sao).",
                "Về mặc định", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        oTiLe.get(ConfigDAO.TL_EXP).setText("1");
        luuTiLe();
    }

    /**
     * Tên tiếng Việt của một mã loại chỉ số; không biết thì trả lại chính mã.
     */
    private static String tenLoai(String ma) {
        String t = nro.repository.dao.SetBonusDAO.LOAI.get(ma);
        return (t == null || t.isEmpty()) ? String.valueOf(ma) : t;
    }

    /**
     * Cột "Tham số" hiển thị theo loại chỉ số.
     *
     * <p>Với {@code skill_pct} tham số là id chiêu, nên in kèm tên chiêu — nhìn
     * số 6 trơ trọi thì không ai biết là Thái Dương Hạ San. Loại khác thì tham số
     * chỉ là một con số bình thường.</p>
     */
    private static String tenThamSo(String loai, int thamSo) {
        if (!"skill_pct".equals(loai)) {
            return String.valueOf(thamSo);
        }
        String t = nro.repository.dao.SetBonusDAO.CHIEU.get(thamSo);
        return (t == null) ? String.valueOf(thamSo) : (thamSo + " — " + t);
    }

    /** Cột của bản đọc. */
    private static final String[] COT_SET = {
        "Set", "Hành tinh", "Trạng thái", "Hiệu ứng theo số món", "Ghi chú"
    };

    /**
     * Xuất toàn bộ chỉ số set ra một tệp {@code .xlsx} hai trang.
     *
     * <p>Trang <b>Chỉ số set</b> là dữ liệu thật, sửa rồi nhập lại được. Trang
     * <b>Bảng mã</b> chỉ để tra: liệt kê mọi mã loại chỉ số và mọi id chiêu kèm
     * tên tiếng Việt, để khi điền cột "Loại chỉ số" khỏi phải nhớ hay đoán —
     * chép thẳng từ trang đó sang.</p>
     */
    private void setXuatExcel() {
        javax.swing.JFileChooser ch = new javax.swing.JFileChooser();
        ch.setSelectedFile(new java.io.File("chi-so-set.xlsx"));
        if (ch.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File f = ch.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".xlsx")) {
            f = new java.io.File(f.getParentFile(), f.getName() + ".xlsx");
        }
        try {
            java.util.Map<String, nro.repository.dao.SetBonusDAO.DinhNghia> dn =
                    nro.repository.dao.SetBonusDAO.dinhNghia();

            // Gom chi so theo set, giu thu tu so mon tang dan.
            java.util.LinkedHashMap<String, java.util.List<nro.repository.dao.SetBonusDAO.Bonus>> theoSet =
                    new java.util.LinkedHashMap<>();
            for (nro.repository.dao.SetBonusDAO.Bonus b
                    : nro.repository.dao.SetBonusDAO.tatCa()) {
                theoSet.computeIfAbsent(b.setKey, k -> new java.util.ArrayList<>())
                        .add(b);
            }

            ExcelUtil.Sheet s1 = new ExcelUtil.Sheet("Chỉ số set");
            s1.rong(26, 12, 11, 52, 30);
            s1.them((Object[]) COT_SET);
            for (java.util.Map.Entry<String, java.util.List<nro.repository.dao.SetBonusDAO.Bonus>> e
                    : theoSet.entrySet()) {
                nro.repository.dao.SetBonusDAO.DinhNghia d = dn.get(e.getKey());
                String tenSet = (d == null || d.ten == null || d.ten.isEmpty())
                        ? e.getKey() : d.ten;
                String hanhTinh = (d == null || d.hanhTinh == null)
                        ? "" : d.hanhTinh;
                boolean batSet = (d == null) || d.active;

                boolean dauTien = true;
                for (nro.repository.dao.SetBonusDAO.Bonus b : e.getValue()) {
                    // Ten set chi ghi o DONG DAU cua nhom.
                    //
                    // Lap lai ten o moi dong thi mat cot nhin nhu mot bang du
                    // lieu tho; de trong thi mat doc ra ngay day la mot khoi.
                    s1.them(dauTien ? tenSet : "",
                            dauTien ? hanhTinh : "",
                            dauTien ? (batSet ? "BẬT" : "tắt") : "",
                            b.soMon + " món tăng: "
                                    + moTaGon(b.soMon, b.loai, b.giaTri, b.thamSo)
                                    + (b.active ? "" : "   (dòng đang tắt)"),
                            b.ghiChu == null ? "" : b.ghiChu);
                    dauTien = false;
                }
                // Dong trong ngan giua hai set cho de doc.
                s1.them("", "", "", "", "");
            }

            ExcelUtil.Sheet s2 = new ExcelUtil.Sheet("Bảng chỉ số");
            s2.rong(46, 58);
            s2.soDongTieuDe = 0;
            s2.them("== CÁC LOẠI CHỈ SỐ CÓ THỂ ĐẶT ==", "");
            for (java.util.Map.Entry<String, String> e
                    : nro.repository.dao.SetBonusDAO.LOAI.entrySet()) {
                s2.them(e.getValue(), "");
            }
            s2.them("", "");
            s2.them("== CÁC CHIÊU (dùng cho loại \"Sát thương một chiêu\") ==", "");
            for (java.util.Map.Entry<Integer, String> e
                    : nro.repository.dao.SetBonusDAO.CHIEU.entrySet()) {
                s2.them(e.getValue(), "");
            }

            ExcelUtil.ghi(f, java.util.Arrays.asList(s1, s2));
            JOptionPane.showMessageDialog(this,
                    "Đã xuất " + f.getAbsolutePath(),
                    "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất: " + ex,
                    "Xuất Excel", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mô tả một dòng chỉ số, đã bỏ phần "N món:" ở đầu.
     *
     * <p>{@code SetBonusDAO.moTaChiSo} trả về cả cụm "5 món: +100% HP". Ở đây số
     * món đã nằm sẵn trong câu bao ngoài ("5 món tăng: …") nên giữ lại là lặp
     * hai lần.</p>
     */
    private static String moTaGon(int soMon, String loai, long giaTri, int thamSo) {
        String s = nro.repository.dao.SetBonusDAO.moTaChiSo(soMon, loai, giaTri,
                thamSo);
        if (s == null) {
            return "";
        }
        int i = s.indexOf(":");
        return (i >= 0 && i + 1 < s.length()) ? s.substring(i + 1).trim() : s;
    }

    private JComponent buildSetTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // ---------------- bên trái: danh sách set ----------------
        cbHanhTinh.addItem("Tất cả");
        for (String h : nro.repository.dao.SetBonusDAO.CAC_HANH_TINH) {
            cbHanhTinh.addItem(h);
        }
        cbHanhTinh.addActionListener(e -> napDanhSachSet());
        fTimSet.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                napDanhSachSet();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                napDanhSachSet();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                napDanhSachSet();
            }
        });

        JPanel locTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        locTop.setOpaque(false);
        locTop.add(new JLabel("Hành tinh:"));
        locTop.add(cbHanhTinh);
        locTop.add(new JLabel("Tìm:"));
        locTop.add(fTimSet);

        dsSetTable.setRowHeight(24);
        // Chon duoc nhieu dong de bat/tat mot loat.
        dsSetTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dsSetTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wl = {180, 90, 70, 60};
        for (int i2 = 0; i2 < wl.length && i2 < dsSetTable.getColumnCount(); i2++) {
            dsSetTable.getColumnModel().getColumn(i2).setPreferredWidth(wl[i2]);
        }
        // Cot 5 giu ma set — an di, chi de tra cuu.
        dsSetTable.removeColumn(dsSetTable.getColumnModel().getColumn(4));
        dsSetTable.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                loadSet();
            }
        });
        // Nhay dup mo hop sua — thay cho nut "Sua set" da bo.
        dsSetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && dsSetTable.getSelectedRow() >= 0) {
                    setDinhNghiaDialog(false);
                }
            }
        });

        JPanel locBot = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        locBot.setOpaque(false);
        locBot.add(button("+ Thêm set mới", OK_GREEN, e -> setDinhNghiaDialog(true)));
        locBot.add(button("Bật", ACCENT, e -> datBatSet(true)));
        locBot.add(button("Tắt", new Color(150, 110, 40), e -> datBatSet(false)));
        locBot.add(button("Xoá set", WARN_RED, e -> setDinhNghiaXoa()));
        locBot.add(button("Áp kiểu dòng mô tả", new Color(120, 90, 160),
                e -> apKieuDongMoTa()));
        locBot.add(button("Xuất Excel", new Color(30, 120, 90),
                e -> setXuatExcel()));

        JPanel trai = new JPanel(new BorderLayout(0, 4));
        trai.setOpaque(false);
        trai.add(locTop, BorderLayout.NORTH);
        trai.add(ServerGuiUtils.cuon(dsSetTable), BorderLayout.CENTER);
        trai.add(locBot, BorderLayout.SOUTH);

        // ---------------- bên phải: chỉ số của set ----------------
        setTable.setRowHeight(24);
        setTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] w = {45, 90, 90, 250, 90, 150, 45, 200};
        for (int i2 = 0; i2 < w.length && i2 < setTable.getColumnCount(); i2++) {
            setTable.getColumnModel().getColumn(i2).setPreferredWidth(w[i2]);
        }
        setTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && setTable.getSelectedRow() >= 0) {
                    setDialog(false);
                }
            }
        });

        lblGoc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGoc.setForeground(ACCENT);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btn.setOpaque(false);
        btn.add(button("Thêm dòng", OK_GREEN, e -> setDialog(true)));
        btn.add(button("Sửa dòng", ACCENT, e -> setDialog(false)));
        btn.add(button("Xoá dòng", WARN_RED, e -> setXoa()));

        JPanel phaiTop = new JPanel(new BorderLayout(0, 2));
        phaiTop.setOpaque(false);
        phaiTop.add(lblGoc, BorderLayout.NORTH);
        JPanel duoiTen = new JPanel(new BorderLayout(0, 2));
        duoiTen.setOpaque(false);
        duoiTen.add(lblTacDung, BorderLayout.NORTH);
        JLabel ghiChuSet = new JLabel("<html><span style='color:#777'>"
                + "Hệ set viết cứng cũ đã bỏ hẳn — mọi hiệu ứng set trong game "
                + "lấy từ đúng bảng này.</span></html>");
        duoiTen.add(ghiChuSet, BorderLayout.SOUTH);
        phaiTop.add(duoiTen, BorderLayout.SOUTH);

        JLabel hint = new JLabel("<html><span style='color:#777'>"
                + "Dòng <b>mã nguồn</b> là hiệu ứng viết sẵn — chỉ xem. Muốn đổi thì "
                + "bấm <b>Chép chỉ số gốc xuống</b> rồi sửa số.<br>"
                + "Ô <b>Thay thế hẳn</b> tắt: dòng cấu hình <i>cộng thêm</i> vào mã nguồn. "
                + "Bật: bỏ hết mã nguồn, chỉ dùng dòng cấu hình.<br>"
                + "Giá trị âm cũng được. Có hiệu lực ở lần tính chỉ số kế tiếp — "
                + "không cần khởi động lại."
                + "</span></html>");

        JPanel phaiBot = new JPanel(new BorderLayout(0, 2));
        phaiBot.setOpaque(false);
        phaiBot.add(btn, BorderLayout.NORTH);
        phaiBot.add(hint, BorderLayout.SOUTH);

        JPanel phai = new JPanel(new BorderLayout(0, 4));
        phai.setOpaque(false);
        phai.add(phaiTop, BorderLayout.NORTH);
        phai.add(ServerGuiUtils.cuon(setTable), BorderLayout.CENTER);
        phai.add(phaiBot, BorderLayout.SOUTH);

        javax.swing.JSplitPane sp = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT, trai, phai);
        sp.setResizeWeight(0.34);
        sp.setBorder(null);
        root.add(sp, BorderLayout.CENTER);

        napDanhSachSet();
        return root;
    }

    /** Mã set đang chọn ở danh sách bên trái, hoặc {@code null}. */
    private String setDangChon() {
        int r = dsSetTable.getSelectedRow();
        if (r < 0) {
            return null;
        }
        return String.valueOf(dsSetModel.getValueAt(
                dsSetTable.convertRowIndexToModel(r), 4));
    }

    /**
     * Đổ bảng chỉ số của set đang chọn: dòng <b>mã nguồn</b> trước, rồi dòng
     * cấu hình.
     *
     * <p>Dòng mã nguồn không có id nên cột ID để dấu gạch — và chúng bị chặn ở
     * "Sửa dòng"/"Xoá dòng": sửa được thì phải sửa Java rồi biên dịch lại, panel
     * không giả vờ sửa được.</p>
     */
    private void loadSet() {
        setModel.setRowCount(0);
        String key = setDangChon();
        if (key == null) {
            lblGoc.setText("  (chọn một set ở danh sách bên trái)");
            return;
        }

        int n = 0;
        for (nro.repository.dao.SetBonusDAO.Bonus b : nro.repository.dao.SetBonusDAO.tatCa()) {
            if (!key.equals(b.setKey)) {
                continue;
            }
            n++;
            // Ca hai loai theo chieu deu phai hien ten chieu o cot Tham so —
            // de trong thi khong doc duoc dong do nham chieu nao.
            String thamSo = "skill_pct".equals(b.loai)
                    || "lam_moi_pct".equals(b.loai)
                    ? nro.repository.dao.SetBonusDAO.CHIEU.getOrDefault(
                            b.thamSo, "chiêu " + b.thamSo)
                    : "";
            // Ghi chu de trong thi hien luon tac dung — bang khong bao gio co
            // cot trong ma nguoi doc phai tu suy ra set nay lam gi.
            String ghiChu = b.ghiChu == null || b.ghiChu.trim().isEmpty()
                    ? nro.repository.dao.SetBonusDAO.moTaChiSo(
                            b.soMon, b.loai, b.giaTri, b.thamSo)
                    : b.ghiChu;
            setModel.addRow(new Object[]{b.id, "cấu hình", b.soMon,
                nro.repository.dao.SetBonusDAO.LOAI.getOrDefault(b.loai, b.loai),
                PlayerManagerPanel.fmt(b.giaTri), thamSo, b.active ? "có" : "", ghiChu});
        }

        lblGoc.setText("  " + nro.repository.dao.SetBonusDAO.tenSet(key)
                + "   —   " + nro.repository.dao.SetBonusDAO.hanhTinh(key)
                + "   —   " + n + " dòng chỉ số");
        lblTacDung.setText("<html><span style='color:#555'>Mặc đủ set thì được: <b>"
                + tomTatTacDung(key) + "</b></span></html>");
        note(GREY, "Set " + nro.repository.dao.SetBonusDAO.tenSet(key)
                + ": " + n + " dòng chỉ số.");
    }

    private void setDialog(boolean them) {
        String key = setDangChon();
        if (key == null) {
            note(WARN_RED, "Chưa chọn set nào.");
            return;
        }
        int r = setTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn dòng nào để sửa.");
            return;
        }
        JTextField fMon = new JTextField(them ? "5" : String.valueOf(setModel.getValueAt(r, 2)), 6);
        JTextField fGiaTri = new JTextField(them ? "0"
                : String.valueOf(setModel.getValueAt(r, 4)).replace(".", ""), 12);
        JTextField fGhiChu = new JTextField(them ? "" : nz(setModel.getValueAt(r, 7)), 24);
        javax.swing.JCheckBox cbOn = new javax.swing.JCheckBox("Bật",
                them || "có".equals(String.valueOf(setModel.getValueAt(r, 6))));

        // O chon chieu — dung cho ca skill_pct va lam_moi_pct.
        JComboBox<String> cbChieu = new JComboBox<>();
        for (java.util.Map.Entry<Integer, String> en
                : nro.repository.dao.SetBonusDAO.CHIEU.entrySet()) {
            cbChieu.addItem(en.getKey() + " — " + en.getValue());
        }
        if (!them) {
            String cu = String.valueOf(setModel.getValueAt(r, 5));
            for (int k = 0; k < cbChieu.getItemCount(); k++) {
                if (cbChieu.getItemAt(k).endsWith(cu)) {
                    cbChieu.setSelectedIndex(k);
                    break;
                }
            }
        }

        JComboBox<String> cbLoai = new JComboBox<>();
        for (java.util.Map.Entry<String, String> e
                : nro.repository.dao.SetBonusDAO.LOAI.entrySet()) {
            cbLoai.addItem(e.getValue());
        }
        if (!them) {
            cbLoai.setSelectedItem(String.valueOf(setModel.getValueAt(r, 3)));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Set:"), c);
        c.gridx = 1;
        form.add(new JLabel(key), c);
        addRow(form, c, 1, "Mặc từ mấy món:", fMon);
        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Loại chỉ số:"), c);
        c.gridx = 1;
        c.weightx = 1;
        cbLoai.setPreferredSize(new Dimension(260, 26));
        form.add(cbLoai, c);
        c.weightx = 0;
        addRow(form, c, 3, "Giá trị:", fGiaTri);
        c.gridx = 0;
        c.gridy = 4;
        form.add(new JLabel("Tham số (chiêu):"), c);
        c.gridx = 1;
        c.weightx = 1;
        cbChieu.setPreferredSize(new Dimension(260, 26));
        form.add(cbChieu, c);
        c.weightx = 0;
        addRow(form, c, 5, "Ghi chú:", fGhiChu);
        c.gridx = 1;
        c.gridy = 6;
        form.add(cbOn, c);
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "Loại có đuôi <b>%</b> tính trên giá trị đã có sau khi cộng trang bị.<br>"
                + "Giá trị âm cũng được, để trừ bớt.<br>"
                + "Ô <b>Tham số</b> dùng cho các loại theo chiêu: <i>Sát thương một "
                + "chiêu</i> và <i>Tỉ lệ làm mới một chiêu</i>. Loại khác thì bỏ qua.<br>"
                + "<b>Từ mấy món</b> cho phép làm nhiều mốc: thêm ba dòng 2 món, 4 món, "
                + "5 món là set có ba mức thưởng như trong game."
                + "</span></html>"), c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                them ? "Thêm dòng chỉ số set" : "Sửa dòng chỉ số set", true);
        final int id = them ? -1 : num2(setModel.getValueAt(r, 0));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button(them ? "Thêm" : "Lưu", OK_GREEN, e -> {
            nro.repository.dao.SetBonusDAO.Bonus b = new nro.repository.dao.SetBonusDAO.Bonus();
            b.id = id;
            b.setKey = key;
            b.active = cbOn.isSelected();
            b.ghiChu = fGhiChu.getText().trim();
            try {
                b.soMon = Integer.parseInt(fMon.getText().trim());
                b.giaTri = Long.parseLong(fGiaTri.getText().trim()
                        .replace(".", "").replace(",", ""));
                b.thamSo = Integer.parseInt(
                        String.valueOf(cbChieu.getSelectedItem()).split(" ")[0].trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Số món và giá trị phải là số nguyên.");
                return;
            }
            // Doi nhan hien thi nguoc lai ma loai.
            String nhan = String.valueOf(cbLoai.getSelectedItem());
            for (java.util.Map.Entry<String, String> en
                    : nro.repository.dao.SetBonusDAO.LOAI.entrySet()) {
                if (en.getValue().equals(nhan)) {
                    b.loai = en.getKey();
                }
            }
            String loi = nro.repository.dao.SetBonusDAO.luu(b);
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không lưu được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            loadSet();
            note(OK_GREEN, them ? "Đã thêm dòng chỉ số — có hiệu lực ở lần tính kế tiếp."
                    : "Đã sửa dòng chỉ số — có hiệu lực ở lần tính kế tiếp.");
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

    /**
     * Câu tóm tắt "mặc đủ set thì được gì".
     *
     * <p>Ghép các dòng đang <b>thực sự có tác dụng</b>: bật "Thay thế hẳn" thì
     * chỉ dòng cấu hình, tắt thì cả hai. Nói đúng cái đang chạy, không phải cái
     * đang có trong bảng.</p>
     */
    private static String tomTatTacDung(String key) {
        java.util.List<String> ds = new java.util.ArrayList<>();
        for (nro.repository.dao.SetBonusDAO.Bonus b
                : nro.repository.dao.SetBonusDAO.tatCa()) {
            if (key.equals(b.setKey) && b.active) {
                ds.add(nro.repository.dao.SetBonusDAO.moTaChiSo(
                        b.soMon, b.loai, b.giaTri, b.thamSo));
            }
        }
        if (ds.isEmpty()) {
            return "không gì cả — set này chưa có dòng chỉ số nào";
        }
        return String.join("  •  ", ds);
    }

    private void setXoa() {
        int[] rs = setTable.getSelectedRows();
        if (rs.length == 0) {
            note(WARN_RED, "Chưa chọn dòng nào.");
            return;
        }
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        int boQuaGoc = 0;
        StringBuilder ten = new StringBuilder();
        for (int r : rs) {
            ids.add(num2(setModel.getValueAt(r, 0)));
            if (ten.length() < 200) {
                ten.append(ten.length() == 0 ? "" : ", ").append(setModel.getValueAt(r, 3));
            }
        }
        if (ids.isEmpty()) {
            note(WARN_RED, "Chưa chọn dòng nào để xoá.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá " + ids.size() + " dòng chỉ số?\n\n" + ten
                + (ten.length() >= 200 ? " …" : ""),
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        int xong = 0;
        for (int id : ids) {
            if (nro.repository.dao.SetBonusDAO.xoa(id)) {
                xong++;
            }
        }
        note(xong == ids.size() ? OK_GREEN : WARN_RED,
                "Đã xoá " + xong + "/" + ids.size() + " dòng chỉ số.");
        napDanhSachSet();
        loadSet();
    }

    /**
     * Hộp tạo / sửa một set kích hoạt — <b>một form duy nhất</b>.
     *
     * <h3>Một set gồm bốn thứ</h3>
     *
     * <ol>
     *   <li><b>Tên</b> và <b>mô tả</b> — chữ hiện cho người chơi.</li>
     *   <li><b>Chỉ số nhận diện</b> — món trang bị mang chỉ số này thì tính là
     *       một món của set. Đây là cách game nhận biết set, <b>không</b> phải
     *       bằng danh sách vật phẩm; nên món rơi từ quái hay nhận từ giftcode
     *       cũng tự động thuộc set.</li>
     *   <li><b>Tác dụng</b> — chọn một chỉ số (HP, KI, sức đánh, chí mạng…)
     *       hoặc một chiêu, rồi điền số.</li>
     * </ol>
     *
     * <p>Ô chọn tác dụng gộp cả chỉ số lẫn chiêu vào <b>một danh sách</b>: chọn
     * "Sát thương Kamejoko" hay chọn "HP + %" đều là một thao tác, không phải
     * nhớ rằng chiêu thì phải điền thêm một ô tham số riêng.</p>
     *
     * <p>Sửa set thì form này sửa <b>dòng tác dụng đầu tiên</b>. Set nhiều mốc
     * (2 / 4 / 5 món) thì thêm các dòng còn lại ở bảng bên phải.</p>
     */
    private void setDinhNghiaDialog(boolean them) {
        String key = them ? null : setDangChon();
        nro.repository.dao.SetBonusDAO.DinhNghia cu =
                key == null ? null : nro.repository.dao.SetBonusDAO.dinhNghia().get(key);
        if (!them && cu == null) {
            note(WARN_RED, "Chưa chọn set nào để sửa.");
            return;
        }

        JTextField fTen = new JTextField(them ? "" : nz(cu.ten), 26);
        JTextField fMoTa = new JTextField(them || cu.ghiChu == null ? "" : cu.ghiChu, 26);
        // Mot dong chu cho moi moc so mon. Set thuong co 2 / 4 / 5 mon moi
        // an them tac dung, nen moi moc can cau chu rieng — khong the dung
        // chung mot dong "5 mon +100% HP" cho ca moc 2 mon.
        final int[] MOC = {2, 3, 4, 5};
        java.util.Map<Integer, Integer> dongCu = them
                ? new java.util.TreeMap<>()
                : nro.repository.dao.SetBonusDAO.docMoTaDong(cu.moTaDong);
        java.util.Map<Integer, JTextField> oDong = new java.util.LinkedHashMap<>();
        for (int mon : MOC) {
            Integer idCu = dongCu.get(mon);
            oDong.put(mon, new JTextField(idCu == null ? ""
                    : boTienTo(OptionPicker.tenChiSo(idCu)), 26));
        }
        JComboBox<String> cbHt = new JComboBox<>(
                nro.repository.dao.SetBonusDAO.CAC_HANH_TINH);
        cbHt.setSelectedItem(them ? "Trái Đất"
                : (cu.hanhTinh == null || cu.hanhTinh.trim().isEmpty()
                        ? nro.repository.dao.SetBonusDAO.KHAC : cu.hanhTinh.trim()));

        // ---------- chỉ số nhận diện ----------
        final int[] idNhanDien = {-1};
        if (!them && cu.optionIds != null) {
            String s0 = OptionPicker.laySo(cu.optionIds.split(",")[0].trim());
            if (!s0.isEmpty()) {
                idNhanDien[0] = Integer.parseInt(s0);
            }
        }
        JTextField fNhanDien = new JTextField(24);
        fNhanDien.setEditable(false);
        fNhanDien.setBackground(new Color(245, 245, 245));
        Runnable veNhanDien = () -> fNhanDien.setText(idNhanDien[0] < 0
                ? "(chưa chọn)"
                : idNhanDien[0] + " — " + OptionPicker.tenChiSo(idNhanDien[0]));
        veNhanDien.run();
        JPanel pNhanDien = new JPanel(new BorderLayout(6, 0));
        pNhanDien.setOpaque(false);
        pNhanDien.add(fNhanDien, BorderLayout.CENTER);
        pNhanDien.add(button("Chọn…", ACCENT, e -> {
            int id = OptionPicker.chonChiSo(pNhanDien, idNhanDien[0]);
            if (id >= 0) {
                idNhanDien[0] = id;
                veNhanDien.run();
            }
        }), BorderLayout.EAST);

        // ---------- tác dụng: một ô chọn + một ô số ----------
        java.util.List<String[]> tacDung = new java.util.ArrayList<>();
        for (Map.Entry<String, String> e
                : nro.repository.dao.SetBonusDAO.LOAI.entrySet()) {
            if ("skill_pct".equals(e.getKey()) || "lam_moi_pct".equals(e.getKey())) {
                continue;       // hai loai theo chieu -> tach thanh tung dong rieng
            }
            tacDung.add(new String[]{e.getKey(), "0", e.getValue()});
        }
        for (Map.Entry<Integer, String> e
                : nro.repository.dao.SetBonusDAO.CHIEU.entrySet()) {
            tacDung.add(new String[]{"skill_pct", String.valueOf(e.getKey()),
                "Sát thương chiêu + %  —  " + e.getValue()});
        }
        // Mot dong cho moi chieu, y het cach lam cua sat thuong chieu.
        for (Map.Entry<Integer, String> e
                : nro.repository.dao.SetBonusDAO.CHIEU.entrySet()) {
            tacDung.add(new String[]{"lam_moi_pct", String.valueOf(e.getKey()),
                "Tỉ lệ làm mới kỹ năng sau khi dùng %  —  " + e.getValue()});
        }
        // Moi moc so mon co tac dung RIENG. Muc dau la "khong co gi" de bo
        // trong nhung moc chua dung — set 5 mon thuong chi an o moc 5.
        final String KHONG = "— không có —";

        // Sua set: do lai cac dong tac dung dang co, xep theo moc so mon.
        java.util.Map<Integer, nro.repository.dao.SetBonusDAO.Bonus> bonusCu =
                new java.util.TreeMap<>();
        // Cac dong CHI SO PHU cua moc 5 mon.
        //
        // bonusCu chi giu duoc MOT dong moi moc (no la Map moc -> dong), nen
        // dong thu hai tro di cua moc 5 khong co cho nao trong bang moc. Chung
        // di vao danh sach rieng nay va co khung sua rieng ben duoi.
        java.util.List<nro.repository.dao.SetBonusDAO.Bonus> phuCu =
                new java.util.ArrayList<>();
        if (!them) {
            for (nro.repository.dao.SetBonusDAO.Bonus b
                    : nro.repository.dao.SetBonusDAO.bonusCua(key)) {
                if (b.soMon == 5 && bonusCu.containsKey(5)) {
                    phuCu.add(b);
                } else {
                    bonusCu.put(b.soMon, b);
                }
            }
        }

        java.util.Map<Integer, JComboBox<String>> oTacDung =
                new java.util.LinkedHashMap<>();
        java.util.Map<Integer, JTextField> oGiaTri = new java.util.LinkedHashMap<>();
        for (int mon : MOC) {
            JComboBox<String> cb = new JComboBox<>();
            cb.addItem(KHONG);
            for (String[] x : tacDung) {
                cb.addItem(x[2]);
            }
            cb.setPreferredSize(new Dimension(320, 24));
            JTextField ft = new JTextField(7);
            nro.repository.dao.SetBonusDAO.Bonus b = bonusCu.get(mon);
            if (b != null) {
                // Doi chieu CA loai VA tham so.
                //
                // Truoc day chi doi chieu tham so khi loai la "skill_pct", nen
                // "lam_moi_pct" khop vao muc DAU TIEN cua loai do — tuc Trai
                // Dat / Dragon — bat ke set that nham chieu nao. Bam OK la luu
                // de gia tri sai do: ca bon moc bi doi thanh dam Dragon.
                //
                // Loai khong theo chieu thi tham so cua ca hai ben deu la 0 nen
                // so sanh van dung.
                for (int x = 0; x < tacDung.size(); x++) {
                    if (!tacDung.get(x)[0].equals(b.loai)) {
                        continue;
                    }
                    int thamSoMuc;
                    try {
                        thamSoMuc = Integer.parseInt(tacDung.get(x)[1]);
                    } catch (NumberFormatException ex) {
                        continue;
                    }
                    if (thamSoMuc == b.thamSo) {
                        cb.setSelectedIndex(x + 1);      // +1 vi muc 0 la KHONG
                        break;
                    }
                }
                ft.setText(String.valueOf(b.giaTri));
            }
            oTacDung.put(mon, cb);
            oGiaTri.put(mon, ft);
        }

        // ---------- dựng form ----------
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        addRow(form, c, y++, "Tên set:", fTen);
        addRowC(form, c, y++, "Hành tinh:", cbHt);
        addRow(form, c, y++, "Ghi chú (chỉ admin thấy):", fMoTa);

        addRowC(form, c, y++, "Chỉ số nhận diện:", pNhanDien);

        JComboBox<String> cbMoc = new JComboBox<>(
                nro.repository.dao.SetBonusDAO.CACH_TINH_MOC);
        int mocCu = them ? nro.repository.dao.SetBonusDAO.MOC_CHI_5 : cu.cachTinhMoc;
        cbMoc.setSelectedIndex(Math.max(0, Math.min(mocCu,
                nro.repository.dao.SetBonusDAO.CACH_TINH_MOC.length - 1)));
        addRowC(form, c, y++, "Cách tính mốc:", cbMoc);

        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        JLabel nhomTd = new JLabel("Theo số món đang mặc");
        nhomTd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nhomTd.setForeground(ACCENT);
        nhomTd.setBorder(new EmptyBorder(10, 0, 2, 0));
        form.add(nhomTd, c);

        // Mot bang: moi moc mot hang, chu in tren mon do di lien voi tac dung
        // that cua chinh moc do — nhin la biet mac 4 mon thi duoc gi.
        JPanel bang = new JPanel(new GridBagLayout());
        bang.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        String[] dauCot = {"", "Dòng chữ in trên món đồ", "Chỉ số / chiêu", "Giá trị"};
        for (int i = 0; i < dauCot.length; i++) {
            g.gridx = i;
            g.gridy = 0;
            JLabel h = new JLabel(dauCot[i]);
            h.setFont(new Font("Segoe UI", Font.BOLD, 11));
            h.setForeground(new Color(120, 120, 120));
            bang.add(h, g);
        }
        int hang = 1;
        for (int mon : MOC) {
            g.gridy = hang++;
            g.gridx = 0;
            bang.add(new JLabel(mon + " món"), g);
            g.gridx = 1;
            bang.add(oDong.get(mon), g);
            g.gridx = 2;
            bang.add(oTacDung.get(mon), g);
            g.gridx = 3;
            bang.add(oGiaTri.get(mon), g);
        }
        c.gridy = y++;
        form.add(bang, c);
        c.gridwidth = 1;

        // ---------- chỉ số phụ khi đủ 5 món ----------
        //
        // Bang moc o tren chi cho MOT dong moi moc. Set thuong can them vai
        // chi so nua khi mac du bo — khung nay la cho de them bao nhieu tuy y.
        // Chung duoc luu y nhu moc 5 mon, chi khac la khong co dong chu rieng
        // in tren mon do.
        DefaultTableModel mPhu = new DefaultTableModel(
                new Object[]{"Chỉ số", "Giá trị"}, 0);
        JTable bPhu = new JTable(mPhu);
        bPhu.setRowHeight(24);
        JComboBox<String> oPhuLoai = new JComboBox<>();
        for (String[] t : tacDung) {
            oPhuLoai.addItem(t[2]);
        }
        bPhu.getColumnModel().getColumn(0)
                .setCellEditor(new javax.swing.DefaultCellEditor(oPhuLoai));
        for (nro.repository.dao.SetBonusDAO.Bonus b : phuCu) {
            String nhan = null;
            for (String[] t : tacDung) {
                if (t[0].equals(b.loai) && Integer.parseInt(t[1]) == b.thamSo) {
                    nhan = t[2];
                    break;
                }
            }
            mPhu.addRow(new Object[]{nhan == null ? b.loai : nhan,
                String.valueOf(b.giaTri)});
        }

        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        JLabel nhomPhu = new JLabel("Chỉ số phụ khi đủ 5 món (thêm bao nhiêu dòng tuỳ ý)");
        nhomPhu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nhomPhu.setForeground(ACCENT);
        nhomPhu.setBorder(new EmptyBorder(10, 0, 2, 0));
        form.add(nhomPhu, c);

        c.gridy = y++;
        JScrollPane spPhu = ServerGuiUtils.cuon(bPhu);
        spPhu.setPreferredSize(new Dimension(760, 96));
        form.add(spPhu, c);

        c.gridy = y++;
        JPanel nutPhu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nutPhu.setOpaque(false);
        nutPhu.add(button("Thêm chỉ số phụ", OK_GREEN, ev -> {
            mPhu.addRow(new Object[]{tacDung.get(0)[2], "0"});
        }));
        nutPhu.add(button("Xoá dòng đang chọn", WARN_RED, ev -> {
            int r = bPhu.getSelectedRow();
            if (r >= 0) {
                if (bPhu.isEditing()) {
                    bPhu.getCellEditor().stopCellEditing();
                }
                mPhu.removeRow(bPhu.convertRowIndexToModel(r));
            }
        }));
        form.add(nutPhu, c);
        c.gridwidth = 1;

        javax.swing.JCheckBox cbOn = new javax.swing.JCheckBox("Bật", them || cu.active);
        c.gridx = 1;
        c.gridy = y++;
        form.add(cbOn, c);

        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "<b>Dòng chữ in trên món đồ</b>: chỉ cần gõ <i>nội dung</i>, phần "
                + "\"2 món:\" panel tự thêm — không thì bốn dòng xếp cạnh nhau người chơi "
                + "không biết dòng nào ứng với mấy món. Để trống thì không in mốc đó. "
                + "Sửa chữ rồi bấm OK là đồ người chơi <i>đang cầm</i> cũng đổi theo — "
                + "không phải phát lại đồ.<br>"
                + "<b>Chỉ số nhận diện</b>: món trang bị mang chỉ số này thì tính một món "
                + "của set — kể cả món rơi từ quái hay nhận từ giftcode.<br>"
                + "<b>Mỗi hàng là một mốc</b>: mặc đủ bấy nhiêu món cùng set thì ăn tác "
                + "dụng của hàng đó. Các mốc <b>cộng dồn</b> — mặc 5 món thì ăn cả mốc 2, "
                + "3, 4 và 5. Hàng nào để trống thì mốc đó không có gì.<br>"
                + "<b>Giá trị</b> nhận số âm để trừ bớt. Loại có đuôi <i>%</i> tính trên "
                + "giá trị đã có sau khi cộng trang bị."
                + "</span></html>"), c);

        // Nhan bao loi NAM TRONG hop.
        //
        // Truoc day loi hien o thanh trang thai duoi cua so chinh, con hop thi
        // dong mat — nguoi dung go lai tu dau ca bang bon moc chi vi mot o
        // thieu so. Gio hop mo lai voi dung cac o cu (cung doi tuong
        // JTextField nen giu nguyen chu da go), loi in ngay tren dau.
        JLabel lblLoi = new JLabel(" ");
        lblLoi.setForeground(WARN_RED);
        lblLoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        form.add(lblLoi, c);

        while (true) {
        int ok = JOptionPane.showConfirmDialog(this, form,
                them ? "Tạo set mới" : "Sửa set " + cu.ten,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        lblLoi.setText(" ");

        String ten = fTen.getText().trim();
        if (ten.isEmpty()) {
            lblLoi.setText("Chưa đặt tên set.");
            continue;
        }
        if (idNhanDien[0] < 0) {
            lblLoi.setText("Chưa chọn chỉ số nhận diện — không có nó thì không món "
                    + "nào thuộc set này được.");
            continue;
        }
        // Doc va kiem tra TAT CA cac moc TRUOC khi ghi bat cu thu gi: bao loi
        // giua chung thi set da luu mot nua, cac moc sau chua — kho lan hon la
        // tu choi ngay tu dau.
        java.util.Map<Integer, long[]> tacDungMoi = new java.util.TreeMap<>();
        for (int mon : MOC) {
            int chon = oTacDung.get(mon).getSelectedIndex();
            String so = oGiaTri.get(mon).getText().trim();
            if (chon <= 0) {
                if (!so.isEmpty()) {
                    lblLoi.setText("Mốc " + mon + " món có điền giá trị nhưng chưa "
                            + "chọn chỉ số — chọn chỉ số hoặc xoá giá trị đi.");
                    continue;
                }
                continue;
            }
            if (so.isEmpty()) {
                lblLoi.setText("Mốc " + mon + " món đã chọn chỉ số nhưng chưa "
                        + "điền giá trị.");
                continue;
            }
            try {
                tacDungMoi.put(mon, new long[]{chon - 1,
                    Long.parseLong(so.replace(".", "").replace(",", ""))});
            } catch (NumberFormatException ex) {
                lblLoi.setText("Giá trị của mốc " + mon + " món phải là số nguyên.");
                continue;
            }
        }

        nro.repository.dao.SetBonusDAO.DinhNghia d =
                new nro.repository.dao.SetBonusDAO.DinhNghia();
        d.setKey = them ? maTuTen(ten) : cu.setKey;
        d.ten = ten;
        d.optionIds = String.valueOf(idNhanDien[0]);
        d.ghiChu = fMoTa.getText().trim();
        d.active = cbOn.isSelected();

        // Moi dong chu in tren mon do phai la mot chi so THAT — client chi biet
        // in ten chi so. Nen moi moc duoc cat vao mot chi so rieng "$[n] chu".
        // O nao da co id thi DOI TEN chinh chi so do, id giu nguyen, nen mon do
        // nguoi choi dang cam cung doi chu theo — khong phai phat lai do.
        java.util.Map<Integer, Integer> dongMoi = new java.util.TreeMap<>();
        for (int mon : MOC) {
            String chu = oDong.get(mon).getText().trim();
            Integer idCu = dongCu.get(mon);
            if (chu.isEmpty()) {
                // Xoa chu -> thoi in moc nay, nhung KHONG xoa chi so: do da
                // phat ra van dang tro toi id do, xoa la mon do hien dong trong.
                continue;
            }
            // Dòng in trên món đồ = "<n> món: <chữ>", y như bảng trên panel.
            //
            // Mốc phải nằm trong chính câu chữ, vì client in các dòng chỉ số
            // liền nhau không kèm nhãn gì. Bốn dòng "x1.2 / x1.4 / x1.7 / x2"
            // xếp cạnh nhau thì người chơi không biết dòng nào ứng với mấy món.
            //
            // boTienTo() gỡ phần mốc ở đầu (nếu ô nhập đang có) trước khi ghép,
            // không thì ra "2 món: 2 món: +20%..." — đúng lỗi in hai lần.
            String tenDong = mon + " món: " + boTienTo(chu);
            if (idCu != null) {
                // Chỉ đổi TÊN, giữ nguyên type. Type do nút "Áp kiểu dòng mô tả"
                // quản; đè ở đây thì mỗi lần sửa set là kiểu chữ bị đặt lại,
                // và không cách nào giữ được kiểu riêng cho từng dòng.
                if (!nro.repository.dao.ChiSoOptionDAO.doiTen(idCu, tenDong)) {
                    lblLoi.setText("Không sửa được dòng chữ của mốc " + mon + " món.");
                    continue;
                }
                dongMoi.put(mon, idCu);
            } else {
                int loaiDong = (int) ConfigDAO.num(ConfigDAO.SKH_MOTA_TYPE);
                int idMoi = nro.repository.dao.ChiSoOptionDAO.them(tenDong, loaiDong);
                if (idMoi < 0) {
                    lblLoi.setText("Không tạo được dòng chữ của mốc " + mon
                            + " món — xem log máy chủ.");
                    continue;
                }
                dongMoi.put(mon, idMoi);
            }
        }
        d.moTaDong = nro.repository.dao.SetBonusDAO.ghiMoTaDong(dongMoi);
        d.hanhTinh = String.valueOf(cbHt.getSelectedItem());
        d.cachTinhMoc = cbMoc.getSelectedIndex();
        if (d.setKey == null || d.setKey.isEmpty()) {
            lblLoi.setText("Tên set không sinh được mã hợp lệ — thử tên khác.");
            continue;
        }
        String loi = nro.repository.dao.SetBonusDAO.luuDinhNghia(d);
        if (loi != null) {
            lblLoi.setText(loi);
            continue;
        }

        // Ghi tac dung cua tung moc. Moc bo trong ma truoc do co dong thi XOA
        // dong do — de lai la mac 4 mon van an tac dung nguoi dung vua go bo.
        String loi2 = null;
        for (int mon : MOC) {
            long[] td = tacDungMoi.get(mon);
            nro.repository.dao.SetBonusDAO.Bonus cuMon = bonusCu.get(mon);
            if (td == null) {
                if (cuMon != null) {
                    nro.repository.dao.SetBonusDAO.xoa(cuMon.id);
                }
                continue;
            }
            String[] loai = tacDung.get((int) td[0]);
            nro.repository.dao.SetBonusDAO.Bonus b =
                    new nro.repository.dao.SetBonusDAO.Bonus();
            b.id = cuMon == null ? 0 : cuMon.id;
            b.setKey = d.setKey;
            b.soMon = mon;
            b.loai = loai[0];
            b.thamSo = Integer.parseInt(loai[1]);
            b.giaTri = td[1];
            b.active = true;
            String e = nro.repository.dao.SetBonusDAO.luu(b);
            if (e != null && loi2 == null) {
                loi2 = "Mốc " + mon + " món: " + e;
            }
        }

        // Chi so phu cua moc 5 mon: xoa het dong cu roi ghi lai dong moi.
        //
        // Xoa het roi ghi lai chu khong doi tung dong: so dong thay doi tuy y
        // (them ba, bo mot), nen ghep dong cu voi dong moi theo thu tu la co
        // luc ghep nham va sua trung len dong khac.
        if (bPhu.isEditing()) {
            bPhu.getCellEditor().stopCellEditing();
        }
        for (nro.repository.dao.SetBonusDAO.Bonus b : phuCu) {
            nro.repository.dao.SetBonusDAO.xoa(b.id);
        }
        for (int r = 0; r < mPhu.getRowCount(); r++) {
            String nhan = String.valueOf(mPhu.getValueAt(r, 0));
            String[] loaiPhu = null;
            for (String[] t : tacDung) {
                if (t[2].equals(nhan)) {
                    loaiPhu = t;
                    break;
                }
            }
            if (loaiPhu == null) {
                continue;
            }
            long v;
            try {
                v = Long.parseLong(String.valueOf(mPhu.getValueAt(r, 1))
                        .replace(".", "").replace(",", "").trim());
            } catch (NumberFormatException ex) {
                if (loi2 == null) {
                    loi2 = "Chỉ số phụ dòng " + (r + 1) + " không phải số.";
                }
                continue;
            }
            nro.repository.dao.SetBonusDAO.Bonus bp =
                    new nro.repository.dao.SetBonusDAO.Bonus();
            bp.id = 0;
            bp.setKey = d.setKey;
            bp.soMon = 5;
            bp.loai = loaiPhu[0];
            bp.thamSo = Integer.parseInt(loaiPhu[1]);
            bp.giaTri = v;
            bp.active = true;
            String e = nro.repository.dao.SetBonusDAO.luu(bp);
            if (e != null && loi2 == null) {
                loi2 = "Chỉ số phụ dòng " + (r + 1) + ": " + e;
            }
        }

        napDanhSachSet();
        chonSet(d.setKey);
        loadSet();
        note(loi2 == null ? OK_GREEN : WARN_RED, loi2 == null
                ? (them ? "Đã tạo set \"" + ten + "\" với " + tacDungMoi.size()
                        + " mốc tác dụng."
                        : "Đã sửa set \"" + ten + "\" — " + tacDungMoi.size()
                        + " mốc tác dụng.")
                : "Đã lưu set nhưng có mốc lỗi: " + loi2);
        return;
        }
    }

    /**
     * Sinh mã set từ tên hiển thị: bỏ dấu, bỏ khoảng trắng, giữ chữ và số.
     *
     * <p>Mã chỉ dùng nội bộ (khoá của bảng {@code set_kich_hoat}) nên không đáng
     * bắt người dùng tự nghĩ ra. Tên rỗng thì trả {@code "set_moi"} chứ không trả
     * chuỗi rỗng — chuỗi rỗng sẽ bị {@code luuDinhNghia} từ chối với câu lỗi khó
     * hiểu hơn.</p>
     */
    private static String maTuTen(String ten) {
        String t = java.text.Normalizer.normalize(
                ten == null ? "" : ten, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D');
        StringBuilder sb = new StringBuilder();
        boolean hoa = false;
        for (char ch : t.toCharArray()) {
            if (Character.isLetterOrDigit(ch) && ch < 128) {
                sb.append(hoa ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
                hoa = false;
            } else {
                hoa = sb.length() > 0;
            }
        }
        return sb.length() < 2 ? "set_moi" : sb.toString();
    }

    private void setDinhNghiaXoa() {
        String key = setDangChon();
        if (key == null) {
            return;
        }
        if (!nro.repository.dao.SetBonusDAO.dinhNghia().containsKey(key)) {
            note(WARN_RED, "Set \"" + key + "\" là set viết sẵn trong mã nguồn, không xoá được. "
                    + "Muốn tắt hiệu ứng thì bật \"Thay thế hẳn\" và để bảng chỉ số trống.");
            return;
        }
        int soDong = 0;
        for (nro.repository.dao.SetBonusDAO.Bonus b : nro.repository.dao.SetBonusDAO.tatCa()) {
            if (key.equals(b.setKey)) {
                soDong++;
            }
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá set \"" + key + "\"?\n\nSẽ xoá luôn " + soDong + " dòng chỉ số của nó.",
                "Xác nhận xoá set", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        boolean xong = nro.repository.dao.SetBonusDAO.xoaDinhNghia(key);
        napDanhSachSet();
        loadSet();
        note(xong ? OK_GREEN : WARN_RED, xong
                ? "Đã xoá set \"" + key + "\" và " + soDong + " dòng chỉ số."
                : "Không xoá được.");
    }

    /**
     * Dựng lại danh sách set bên trái theo bộ lọc đang đặt.
     *
     * <p>Giữ nguyên set đang chọn nếu nó còn lọt qua bộ lọc — lọc lại mà mất
     * chỗ đang xem thì phải tìm lại từ đầu.</p>
     */
    private void napDanhSachSet() {
        String dangChon = setDangChon();
        String loc = String.valueOf(cbHanhTinh.getSelectedItem());
        String tim = fTimSet.getText().trim().toLowerCase();
        dsSetModel.setRowCount(0);

        // Gom cac dong hop dieu kien roi SAP XEP: ten set truoc, hanh tinh sau.
        //
        // Truoc day do theo thu tu cacSet() — tuc thu tu MA set trong CSDL, nen
        // "Set Nappa" nam giua hai "Set Qua Cau Kenh Khi", tim mot set trong
        // danh sach dai la phai do mat.
        java.util.List<Object[]> dong = new java.util.ArrayList<>();
        for (String k : nro.repository.dao.SetBonusDAO.cacSet()) {
            String ten = nro.repository.dao.SetBonusDAO.tenSet(k);
            String ht = nro.repository.dao.SetBonusDAO.hanhTinh(k);
            if (!"Tất cả".equals(loc) && !ht.equals(loc)) {
                continue;
            }
            if (!tim.isEmpty() && !ten.toLowerCase().contains(tim)
                    && !k.toLowerCase().contains(tim)) {
                continue;
            }
            int soDong = 0;
            for (nro.repository.dao.SetBonusDAO.Bonus b
                    : nro.repository.dao.SetBonusDAO.tatCa()) {
                if (k.equals(b.setKey)) {
                    soDong++;
                }
            }
            nro.repository.dao.SetBonusDAO.DinhNghia dn =
                    nro.repository.dao.SetBonusDAO.dinhNghia().get(k);
            dong.add(new Object[]{ten, ht, soDong,
                dn != null && dn.active ? "BẬT" : "tắt", k});
        }
        // So sanh theo quy tac tieng Viet: "Ă" phai xep ngay sau "A", khong phai
        // cuoi bang nhu khi so ma ky tu.
        final java.text.Collator vi =
                java.text.Collator.getInstance(new java.util.Locale("vi", "VN"));
        vi.setStrength(java.text.Collator.PRIMARY);
        // HANH TINH truoc, roi TEN set.
        //
        // Gom cac set cung hanh tinh thanh mot khoi: dang sua set cua Xayda thi
        // moi set Xayda nam lien nhau, khong phai do khap danh sach.
        dong.sort((x, y) -> {
            int r = vi.compare(String.valueOf(x[1]), String.valueOf(y[1]));
            if (r != 0) {
                return r;
            }
            r = vi.compare(String.valueOf(x[0]), String.valueOf(y[0]));
            // Trung ca hai -> xep theo ma set cho thu tu on dinh, khong nhay
            // moi lan nap lai.
            return r != 0 ? r : String.valueOf(x[4]).compareTo(String.valueOf(y[4]));
        });

        int chon = -1;
        for (Object[] r : dong) {
            if (String.valueOf(r[4]).equals(dangChon)) {
                chon = dsSetModel.getRowCount();
            }
            dsSetModel.addRow(r);
        }
        if (chon >= 0) {
            dsSetTable.setRowSelectionInterval(chon, chon);
        } else if (dsSetModel.getRowCount() > 0) {
            dsSetTable.setRowSelectionInterval(0, 0);
        } else {
            loadSet();
        }
        note(GREY, "Có " + dsSetModel.getRowCount() + " set.");
    }

    /** Chọn một set theo mã trong danh sách bên trái. */
    private void chonSet(String key) {
        for (int r = 0; r < dsSetModel.getRowCount(); r++) {
            if (key.equals(String.valueOf(dsSetModel.getValueAt(r, 4)))) {
                dsSetTable.setRowSelectionInterval(r, r);
                return;
            }
        }
    }

    private static int num2(Object o) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    // =====================================================================
    //  Kiểm tra dữ liệu
    // =====================================================================

    private final DefaultTableModel checkModel = new DefaultTableModel(
            new Object[]{"Loại vấn đề", "ID", "Tên vật phẩm", "Chi tiết", "Gợi ý xử lý"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable checkTable = new JTable(checkModel);
    private final JLabel lblCheck = new JLabel();

    /**
     * Tab dò tham chiếu gãy trong dữ liệu.
     *
     * <p>Máy chủ <b>không kiểm tra gì</b> khi nạp: các cột {@code head},
     * {@code body}, {@code leg}, {@code part} của {@code item_template} là id
     * trỏ sang bảng {@code part} nhưng không có khoá ngoại nào ràng buộc. Trỏ
     * sai thì máy chủ vẫn chạy, vẫn gửi id đó cho client, client không tìm thấy
     * hình nên vẽ ra thứ khác — và không có dòng log nào.</p>
     */
    // =====================================================================
    //  Hào quang
    // =====================================================================

    private final DefaultTableModel auraModel = new DefaultTableModel(
            new Object[]{"Id", "Tên hào quang", "Chỉ cho cải trang", "Số cải trang gán",
                "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable auraTable = new JTable(auraModel);

    // =====================================================================
    //  Điểm đến của capsule
    // =====================================================================

    // =====================================================================
    //  Danh hiệu
    // =====================================================================

    private final DefaultTableModel dhModel = new DefaultTableModel(
            new Object[]{"Id", "Id hiệu ứng", "Vật phẩm đổi", "Tên danh hiệu",
                "Chỉ số", "Cách nhận", "Số người đang giữ"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable dhTable = new JTable(dhModel);

    private final DefaultTableModel nvModel = new DefaultTableModel(
            new Object[]{"Id", "Tên nhiệm vụ", "Loại", "Số lần cần đạt",
                "Danh hiệu thưởng"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable nvTable = new JTable(nvModel);

    private final DefaultTableModel capsuleModel = new DefaultTableModel(
            new Object[]{"Id", "Thứ tự", "Bản đồ", "Theo hành tinh",
                "Điều kiện", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable capsuleTable = new JTable(capsuleModel);

    /**
     * Bảng tỉ lệ nâng sao pha lê — sửa thẳng trong ô.
     *
     * <p>Tám dòng cố định, không thêm không xoá, nên không cần hộp thoại riêng
     * như các tab khác: gõ thẳng vào ô rồi bấm Lưu là nhanh nhất.</p>
     */
    private final DefaultTableModel saoModel = new DefaultTableModel(
            new Object[]{"Bậc", "Tỉ lệ %", "Vàng mỗi lần", "Ngọc mỗi lần",
                "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c > 0;   // cot "Bac" la nhan, khong sua
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 4 ? Boolean.class : String.class;
        }
    };
    private final JTable saoTable = new JTable(saoModel);

    /** Bảng trọng số bậc đồ khi nâng Huỷ Diệt thành đồ set kích hoạt. */
    private final DefaultTableModel khModel = new DefaultTableModel(
            new Object[]{"Bậc", "Trọng số", "Cơ hội", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 1 || c == 3 || c == 4;   // "Bac" va "Co hoi" chi de xem
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 3 ? Boolean.class : String.class;
        }
    };
    private final JTable khTable = new JTable(khModel);

    /** Cột của bảng "Nội tại" — đặt tên thay vì gõ số. */
    private static final int COT_NOI_ID = 0;
    private static final int COT_NOI_TEN = 1;
    private static final int COT_NOI_TU1 = 2;
    private static final int COT_NOI_DEN1 = 3;
    private static final int COT_NOI_TU2 = 4;
    private static final int COT_NOI_DEN2 = 5;
    private static final int COT_NOI_ICON = 6;
    private static final int COT_NOI_HT = 7;
    private static final int COT_NOI_TD = 8;

    /**
     * Bảng nội tại — chính là bảng {@code intrinsic} mà máy chủ đọc.
     *
     * <p>Cột "Id" và "Tác dụng" chỉ để xem: id là <b>khoá gắn tác dụng</b>, đổi
     * nó bằng cách gõ đè lên ô sẽ lặng lẽ hoán đổi tác dụng giữa hai dòng.</p>
     */
    private final DefaultTableModel noiModel = new DefaultTableModel(
            new Object[]{"Id", "Tên (mẫu)", "Từ 1", "Đến 1", "Từ 2", "Đến 2",
                "Icon", "Hành tinh", "Tác dụng"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c != COT_NOI_ID && c != COT_NOI_TD;
        }
    };
    private final JTable noiTable = new JTable(noiModel);

    // Chi so cot cua bang "Ban do nhanh". Dat ten thay vi go so: chen them mot
    // cot vao giua la moi con so dich di, ma loi kieu do khong bao gi ca.
    private static final int COT_MN_ID = 0;
    private static final int COT_MN_THU_TU = 1;
    private static final int COT_MN_NHOM = 2;

    private final DefaultTableModel mnModel = new DefaultTableModel(
            new Object[]{"Id", "Thứ tự", "Nhóm", "Tên trong menu", "Bản đồ",
                "Toạ độ", "Theo hành tinh", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable mnTable = new JTable(mnModel);

    // Bang kho chi so cua tab "Sach tuyet ky".
    private static final int COT_STK_ID = 0;

    private final DefaultTableModel stkModel = new DefaultTableModel(
            new Object[]{"Id", "Thứ tự", "Mã chỉ số", "Tên chỉ số",
                "Trị nhỏ nhất", "Trị lớn nhất", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable stkTable = new JTable(stkModel);

    private final JSpinner stkDongMin = new JSpinner(
            new SpinnerNumberModel(1, 0, 20, 1));
    private final JSpinner stkDongMax = new JSpinner(
            new SpinnerNumberModel(4, 0, 20, 1));
    private final JCheckBox stkDungChung = new JCheckBox(
            "Dùng chung cho cả ba hành tinh (bỏ dòng \"Dành cho ...\")");

    private final DefaultTableModel skModel = new DefaultTableModel(
            new Object[]{"Id", "Tên sự kiện", "Trạng thái", "Khung giờ",
                "Nhắc lại", "Câu loa", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable skTable = new JTable(skModel);

    private final DefaultTableModel roiModel = new DefaultTableModel(
            new Object[]{"Id", "Vật phẩm", "Số lượng", "Tỉ lệ mỗi quái",
                "Bản đồ", "Bật"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable roiTable = new JTable(roiModel);

    private final DefaultTableModel doiModel = new DefaultTableModel(
            new Object[]{"Id", "Thứ tự", "Cần có", "Nhận về", "Tên mục", "Bật"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable doiTable = new JTable(doiModel);

    private final DefaultTableModel hopModel = new DefaultTableModel(
            new Object[]{"Id", "Gói", "Phần thưởng", "Số lượng", "Trọng số",
                "Tỉ lệ ra", "Bật"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable hopTable = new JTable(hopModel);
    private final JComboBox<String> cbHopDangXem = new JComboBox<>();

    private final JLabel lblSkTomTat = new JLabel(" ");

    private final DefaultTableModel auraCtModel = new DefaultTableModel(
            new Object[]{"Id vật phẩm", "Cải trang", "Hào quang đang gán"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable auraCtTable = new JTable(auraCtModel);

    /** Hào quang gán riêng cho từng nhân vật — bảng thứ ba của tab Hào quang. */
    private final DefaultTableModel auraNvModel = new DefaultTableModel(
            new Object[]{"Id nhân vật", "Nhân vật", "Hào quang", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable auraNvTable = new JTable(auraNvModel);

    /**
     * Tab hào quang.
     *
     * <p>Hai bảng: trên là danh sách hào quang, dưới là cải trang nào đang gán
     * hào quang nào.</p>
     *
     * <p>Cờ <b>Chỉ cho cải trang</b> quyết định người chơi có tự chọn được hay
     * không: bật thì hào quang đó chỉ hiện khi mặc đúng cải trang được gán.</p>
     */
    // =====================================================================
    //  Kỹ năng & nội tại theo hành tinh
    // =====================================================================

    private final JComboBox<String> cbHanhTinhKn = new JComboBox<>(
            new String[]{"0 — Trái Đất", "1 — Namếc", "2 — Xayda"});

    private final DefaultTableModel knModel = new DefaultTableModel(
            new Object[]{"Slot", "Id", "Tên kỹ năng", "Điểm tối đa",
                "Cách tốn KI", "Loại kỹ năng", "Ảnh", "Số cấp",
                "Mô tả sát thương"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable knTable = new JTable(knModel);

    private final DefaultTableModel ntModel = new DefaultTableModel(
            new Object[]{"Id", "Tên nội tại", "Tham số 1 (từ → đến)",
                "Tham số 2 (từ → đến)", "Icon"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable ntTable = new JTable(ntModel);

    /**
     * Tab kỹ năng.
     *
     * <p>Chọn hành tinh ở trên, bảng trên là <b>kỹ năng</b>, bảng dưới là
     * <b>nội tại</b> của hành tinh đó.</p>
     *
     * <p>Id kỹ năng <b>không</b> phải chỉ số mảng ({@code NClass.getSkillTemplate}
     * dò tuyến tính, {@code DataGame.updateSkill} ghi thẳng id xuống client) nên
     * id thưa cũng chạy. Nhưng vẫn phải nằm trong <b>0..127</b> vì client đọc
     * bằng {@code writeByte}.</p>
     */
    private JComponent buildKyNangTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.setOpaque(false);
        top.add(new JLabel("Hành tinh:"));
        top.add(cbHanhTinhKn);
        top.add(button("Tải lại", GREY, e -> napBangKyNang()));
        cbHanhTinhKn.addActionListener(e -> napBangKyNang());
        root.add(top, BorderLayout.NORTH);

        knTable.setRowHeight(24);
        knTable.setAutoCreateRowSorter(true);
        knTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w1 = {45, 45, 200, 80, 150, 210, 50, 60, 240};
        for (int i = 0; i < knTable.getColumnCount() && i < w1.length; i++) {
            knTable.getColumnModel().getColumn(i).setPreferredWidth(w1[i]);
        }
        knTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    suaKyNangDialog(false);
                }
            }
        });

        ntTable.setRowHeight(24);
        ntTable.setAutoCreateRowSorter(true);
        ntTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w2 = {45, 300, 170, 170, 60};
        for (int i = 0; i < ntTable.getColumnCount() && i < w2.length; i++) {
            ntTable.getColumnModel().getColumn(i).setPreferredWidth(w2[i]);
        }
        ntTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    suaNoiTaiDialog(false);
                }
            }
        });

        JPanel nutKn = new JPanel(new GridLayout(0, 3, 6, 6));
        nutKn.setOpaque(false);
        nutKn.add(button("Thêm kỹ năng", OK_GREEN, e -> suaKyNangDialog(true)));
        nutKn.add(button("Sửa kỹ năng", ACCENT, e -> suaKyNangDialog(false)));
        nutKn.add(button("Xoá kỹ năng", WARN_RED, e -> xoaKyNang()));

        JPanel tren = new JPanel(new BorderLayout(0, 6));
        tren.setOpaque(false);
        tren.add(nhan("Kỹ năng của hành tinh — nháy đúp để sửa"),
                BorderLayout.NORTH);
        tren.add(ServerGuiUtils.cuon(knTable), BorderLayout.CENTER);
        tren.add(nutKn, BorderLayout.SOUTH);

        JPanel nutNt = new JPanel(new GridLayout(0, 3, 6, 6));
        nutNt.setOpaque(false);
        nutNt.add(button("Thêm nội tại", OK_GREEN, e -> suaNoiTaiDialog(true)));
        nutNt.add(button("Sửa nội tại", ACCENT, e -> suaNoiTaiDialog(false)));
        nutNt.add(button("Xoá nội tại", WARN_RED, e -> xoaNoiTai()));

        JPanel duoi = new JPanel(new BorderLayout(0, 6));
        duoi.setOpaque(false);
        duoi.add(nhan("Nội tại của hành tinh — nháy đúp để sửa"),
                BorderLayout.NORTH);
        duoi.add(ServerGuiUtils.cuon(ntTable), BorderLayout.CENTER);
        duoi.add(nutNt, BorderLayout.SOUTH);

        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tren, duoi);
        chia.setResizeWeight(0.55);
        chia.setBorder(null);
        root.add(chia, BorderLayout.CENTER);
        napBangKyNang();
        return root;
    }

    /** Lấy phần chữ sau dấu gạch của nhãn, để cột bảng ngắn mà vẫn đọc được. */
    private static String nhanNgan(String[] nhan, int i) {
        if (i < 0 || i >= nhan.length) {
            return String.valueOf(i);
        }
        int v = nhan[i].indexOf('—');
        return v < 0 ? nhan[i] : nhan[i].substring(v + 1).trim();
    }

    private int hanhTinhDangChon() {
        return Math.max(0, cbHanhTinhKn.getSelectedIndex());
    }

    private void napBangKyNang() {
        int ht = hanhTinhDangChon();
        knModel.setRowCount(0);
        for (nro.repository.dao.KyNangDAO.KyNang k
                : nro.repository.dao.KyNangDAO.kyNangCua(ht)) {
            knModel.addRow(new Object[]{k.slot, k.id, k.ten, k.maxPoint,
                nhanNgan(KIEU_KI, k.manaUseType),
                nhanNgan(LOAI_KN, k.type), k.iconId, k.soCap, k.damInfo});
        }
        ntModel.setRowCount(0);
        for (nro.repository.dao.KyNangDAO.NoiTai t
                : nro.repository.dao.KyNangDAO.noiTaiCua(ht)) {
            ntModel.addRow(new Object[]{t.id, t.ten,
                t.from1 + " → " + t.to1, t.from2 + " → " + t.to2, t.icon});
        }
    }

    private nro.repository.dao.KyNangDAO.KyNang kyNangDangChon() {
        int r = knTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn kỹ năng nào.");
            return null;
        }
        int idx = knTable.convertRowIndexToModel(r);
        int id = Integer.parseInt(String.valueOf(knModel.getValueAt(idx, 1)));
        for (nro.repository.dao.KyNangDAO.KyNang k
                : nro.repository.dao.KyNangDAO.kyNangCua(hanhTinhDangChon())) {
            if (k.id == id) {
                return k;
            }
        }
        note(WARN_RED, "Không đọc lại được kỹ năng đang chọn.");
        return null;
    }

    /** Nhãn cho {@code mana_use_type} — đọc ra từ {@code canUseSkillWithMana}. */
    private static final String[] KIEU_KI = {
        "0 — Tốn số KI cố định",
        "1 — Tốn % KI tối đa",
        "2 — Chỉ cần còn KI (không trừ theo số)"
    };

    /**
     * Nhãn cho cột {@code TYPE} của kỹ năng.
     *
     * <p>Chỉ loại 2 là chắc chắn từ mã nguồn: {@code SkillService} thấy
     * {@code type == 2} thì gọi {@code useSkillBuffToPlayer}. Ba loại còn lại
     * đặt tên theo <b>đúng những chiêu đang mang loại đó</b> trong CSDL, không
     * phải theo mã nguồn.</p>
     */
    private static final String[] LOAI_KN = {
        "0 — (không dùng)",
        "1 — Đánh thẳng mục tiêu (Kamejoko, Dragon, Galick…)",
        "2 — Dùng lên người khác (Trị thương)",
        "3 — Dùng lên bản thân (Biến hình, Khiên năng lượng…)",
        "4 — Chưởng nhiều đoạn (Ma phong ba, Super Kamejoko…)"
    };

    /**
     * Cột của bảng các cấp, khớp đúng 11 trường Manager đọc.
     *
     * <p><b>dx / dy</b> là tầm với tính bằng pixel — máy chủ không đọc, chỉ gửi
     * thẳng cho client vẽ. Đọc ra từ dữ liệu thật: chiêu đấm tay không dx 32–44
     * và dy 18 cố định, chiêu chưởng 150–350, Quả cầu kênh khí tới 900, Dịch
     * chuyển tức thời 5000, Makankosappo 20000 (xuyên hết màn hình), còn chiêu
     * dùng lên bản thân như Khiên năng lượng thì <b>0</b>. Tầm tăng dần theo cấp.</p>
     *
     * <p><b>max_fight</b>: máy chủ <b>không đọc</b> trường này ở đâu cả — chỉ
     * nạp, sao chép rồi gửi client. Cả 27 kỹ năng ở mọi cấp đều đang để
     * {@code 1}, nên không suy được nghĩa từ dữ liệu. Giữ nguyên tên gốc thay
     * vì đặt tên đoán.</p>
     */
    private static final String[] COT_CAP = {
        "Cấp", "Điểm", "Sức mạnh yêu cầu", "Sát thương", "KI dùng",
        "Hồi chiêu (ms)", "Tầm ngang (dx)", "Tầm dọc (dy)", "max_fight",
        "Giá", "Ghi chú"
    };

    private void suaKyNangDialog(boolean them) {
        final int ht = hanhTinhDangChon();
        nro.repository.dao.KyNangDAO.KyNang cu = null;
        if (!them) {
            cu = kyNangDangChon();
            if (cu == null) {
                return;
            }
        }
        int idMoi = them ? nro.repository.dao.KyNangDAO.idTrong(ht) : cu.id;
        if (them && idMoi < 0) {
            note(WARN_RED, "Hết id kỹ năng trống trong khoảng 0..127.");
            return;
        }
        JTextField fId = new JTextField(String.valueOf(idMoi), 6);
        fId.setEditable(them);
        JTextField fTen = new JTextField(cu == null ? "" : cu.ten, 24);
        JTextField fSlot = new JTextField(String.valueOf(them
                ? nro.repository.dao.KyNangDAO.slotTiepTheo(ht) : cu.slot), 6);
        JTextField fMax = new JTextField(String.valueOf(cu == null ? 7 : cu.maxPoint), 6);
        JTextField fDam = new JTextField(cu == null ? "" : cu.damInfo, 30);

        final JComboBox<String> cbKi = new JComboBox<>(KIEU_KI);
        cbKi.setSelectedIndex(Math.max(0, Math.min(KIEU_KI.length - 1,
                cu == null ? 0 : cu.manaUseType)));
        final JComboBox<String> cbLoai = new JComboBox<>(LOAI_KN);
        cbLoai.setSelectedIndex(Math.max(0, Math.min(LOAI_KN.length - 1,
                cu == null ? 1 : cu.type)));

        // ---- O anh: co nut mo bang chon, va xem truoc ngay ben canh
        final int[] icon = {cu == null ? 539 : cu.iconId};
        final JLabel lblIcon = new JLabel();
        final JLabel lblIconSo = new JLabel();
        final Runnable veIcon = () -> {
            lblIcon.setIcon(PlayerManagerPanel.iconOf(icon[0]));
            boolean co = nro.ui.IconPicker.coAnh(icon[0]);
            lblIconSo.setText("  id " + icon[0] + (co ? "" : "  — KHÔNG có ảnh này"));
            lblIconSo.setForeground(co ? GREY : WARN_RED);
        };
        veIcon.run();
        JPanel pIcon = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pIcon.setOpaque(false);
        pIcon.add(lblIcon);
        pIcon.add(lblIconSo);
        pIcon.add(button("Chọn ảnh…", ACCENT, e -> {
            Integer moi = nro.ui.IconPicker.chon(this, icon[0]);
            if (moi != null) {
                icon[0] = moi;
                veIcon.run();
            }
        }));

        // ---- Bang cac cap thay cho o JSON
        final DefaultTableModel capModel = new DefaultTableModel(COT_CAP, 0);
        for (Object[] r : nro.repository.dao.KyNangDAO.docCap(
                cu == null ? nro.repository.dao.KyNangDAO.capMacDinh(7, 1000)
                        : cu.capJson)) {
            capModel.addRow(r);
        }
        final JTable capTable = new JTable(capModel);
        capTable.setRowHeight(22);
        capTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        int[] wc = {40, 45, 130, 85, 70, 100, 100, 90, 80, 60, 200};
        for (int x = 0; x < capTable.getColumnCount() && x < wc.length; x++) {
            capTable.getColumnModel().getColumn(x).setPreferredWidth(wc[x]);
        }
        JScrollPane scCap = ServerGuiUtils.cuon(capTable);
        scCap.setPreferredSize(new Dimension(900, 190));

        JPanel nutCap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        nutCap.setOpaque(false);
        nutCap.add(button("Thêm cấp", OK_GREEN, e -> {
            int n = capModel.getRowCount();
            capModel.addRow(new Object[]{n, n + 1,
                PlayerManagerPanel.fmt(1000L * (n + 1)), 100 + n * 20,
                n + 1, 500, 32, 18, 1, 0, ""});
        }));
        nutCap.add(button("Xoá cấp cuối", WARN_RED, e -> {
            if (capModel.getRowCount() > 0) {
                capModel.setRowCount(capModel.getRowCount() - 1);
            }
        }));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Id kỹ năng:", fId);
        addRow(form, c, 1, "Tên:", fTen);
        addRow(form, c, 2, "Slot (thứ tự hiện):", fSlot);
        addRow(form, c, 3, "Điểm tối đa:", fMax);
        addRowC(form, c, 4, "Cách tốn KI:", cbKi);
        addRowC(form, c, 5, "Loại kỹ năng:", cbLoai);
        addRowC(form, c, 6, "Ảnh:", pIcon);
        addRow(form, c, 7, "Mô tả sát thương:", fDam);

        c.gridx = 0;
        c.gridy = 12;
        c.gridwidth = 2;
        form.add(nhan("Các cấp — sửa thẳng trong bảng, nháy đúp vào ô để gõ"), c);
        c.gridy = 9;
        form.add(scCap, c);
        c.gridy = 10;
        form.add(nutCap, c);
        c.gridy = 11;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "<b>Cách tốn KI</b> quyết định cột <b>KI dùng</b> nghĩa là gì: "
                + "số KI cố định, hay phần trăm KI tối đa.<br>"
                + "<b>Sức mạnh yêu cầu</b> là mốc sức mạnh để học được cấp đó. "
                + "<b>Hồi chiêu</b> tính bằng mili giây (500 = nửa giây).<br>"
                + "<b>Tầm ngang / Tầm dọc</b> tính bằng pixel — đấm tay không "
                + "khoảng 32×18, chưởng 150–350, Quả cầu kênh khí tới 900, "
                + "Makankosappo 20000 (xuyên màn hình); chiêu dùng lên bản thân "
                + "để <b>0</b>.<br>"
                + "<b>max_fight</b>: máy chủ không đọc trường này ở đâu, mọi "
                + "kỹ năng đang để 1 — cứ để nguyên 1.<br>"
                + "Id phải trong <b>0..127</b> (client đọc bằng writeByte) và "
                + "<b>không trùng</b> với kỹ năng của hành tinh khác.<br>"
                + "<b>Phải khởi động lại máy chủ</b> — kỹ năng chỉ nạp một lần "
                + "lúc dựng.</span></html>"), c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                (them ? "Thêm kỹ năng cho " : "Sửa kỹ năng của ")
                + nro.repository.dao.KyNangDAO.tenHanhTinh(ht), true);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button("Lưu", OK_GREEN, e -> {
            // Dang go do trong o nao thi chot lai truoc, khong thi mat o do.
            if (capTable.isEditing()) {
                capTable.getCellEditor().stopCellEditing();
            }
            nro.repository.dao.KyNangDAO.KyNang k =
                    new nro.repository.dao.KyNangDAO.KyNang();
            k.hanhTinh = ht;
            try {
                k.id = Integer.parseInt(fId.getText().trim());
                k.slot = Integer.parseInt(fSlot.getText().trim());
                k.maxPoint = Integer.parseInt(fMax.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Id, slot và điểm tối đa phải là số nguyên.");
                return;
            }
            k.manaUseType = cbKi.getSelectedIndex();
            k.type = cbLoai.getSelectedIndex();
            k.iconId = icon[0];
            k.ten = fTen.getText();
            k.damInfo = fDam.getText();

            java.util.List<Object[]> cap = new java.util.ArrayList<>();
            for (int r = 0; r < capModel.getRowCount(); r++) {
                Object[] dong = new Object[COT_CAP.length];
                for (int x = 0; x < COT_CAP.length; x++) {
                    dong[x] = capModel.getValueAt(r, x);
                }
                cap.add(dong);
            }
            String loiCap = nro.repository.dao.KyNangDAO.kiemCap(cap);
            if (loiCap != null) {
                JOptionPane.showMessageDialog(dlg, loiCap, "Bảng cấp chưa đúng",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            k.capJson = nro.repository.dao.KyNangDAO.ghiCap(cap);

            String loi = nro.repository.dao.KyNangDAO.luu(k);
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không lưu được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            napBangKyNang();
            note(OK_GREEN, "Đã lưu kỹ năng " + k.id + " — khởi động lại máy chủ "
                    + "để áp dụng.");
        }));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(form, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void xoaKyNang() {
        nro.repository.dao.KyNangDAO.KyNang k = kyNangDangChon();
        if (k == null) {
            return;
        }
        int nguoi = nro.repository.dao.KyNangDAO.soNguoiDangCo(k.id);
        String hoi = "Xoá kỹ năng " + k.id + " — " + k.ten + " của "
                + nro.repository.dao.KyNangDAO.tenHanhTinh(k.hanhTinh) + "?";
        if (nguoi > 0) {
            hoi += "\n\nĐang có " + nguoi + " người chơi giữ kỹ năng này — "
                    + "sẽ bị từ chối.";
        }
        if (JOptionPane.showConfirmDialog(this, hoi, "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.KyNangDAO.xoa(k.hanhTinh, k.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangKyNang();
        note(OK_GREEN, "Đã xoá kỹ năng " + k.id
                + " — khởi động lại máy chủ để áp dụng.");
    }

    private nro.repository.dao.KyNangDAO.NoiTai noiTaiDangChon() {
        int r = ntTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn nội tại nào.");
            return null;
        }
        int idx = ntTable.convertRowIndexToModel(r);
        int id = Integer.parseInt(String.valueOf(ntModel.getValueAt(idx, 0)));
        for (nro.repository.dao.KyNangDAO.NoiTai t
                : nro.repository.dao.KyNangDAO.noiTaiCua(-1)) {
            if (t.id == id) {
                return t;
            }
        }
        note(WARN_RED, "Không đọc lại được nội tại đang chọn.");
        return null;
    }

    private void suaNoiTaiDialog(boolean them) {
        final int ht = hanhTinhDangChon();
        nro.repository.dao.KyNangDAO.NoiTai cu = null;
        if (!them) {
            cu = noiTaiDangChon();
            if (cu == null) {
                return;
            }
        }
        int idMoi = them ? nro.repository.dao.KyNangDAO.idNoiTaiTrong() : cu.id;
        if (them && idMoi < 0) {
            note(WARN_RED, "Hết id nội tại trống trong khoảng 0..127.");
            return;
        }
        JTextField fId = new JTextField(String.valueOf(idMoi), 6);
        fId.setEditable(them);
        JTextField fTen = new JTextField(cu == null ? "" : cu.ten, 30);
        JTextField fF1 = new JTextField(String.valueOf(cu == null ? 0 : cu.from1), 8);
        JTextField fT1 = new JTextField(String.valueOf(cu == null ? 0 : cu.to1), 8);
        JTextField fF2 = new JTextField(String.valueOf(cu == null ? 0 : cu.from2), 8);
        JTextField fT2 = new JTextField(String.valueOf(cu == null ? 0 : cu.to2), 8);
        JTextField fIcon = new JTextField(String.valueOf(cu == null ? 0 : cu.icon), 8);
        JComboBox<String> cbG = new JComboBox<>(new String[]{
            "0 — Trái Đất", "1 — Namếc", "2 — Xayda", "3 — Tất cả"});
        cbG.setSelectedIndex(cu == null ? ht : Math.max(0, Math.min(3, cu.gender)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Id nội tại:", fId);
        addRow(form, c, 1, "Tên:", fTen);
        addRow(form, c, 2, "Tham số 1 — từ:", fF1);
        addRow(form, c, 3, "Tham số 1 — đến:", fT1);
        addRow(form, c, 4, "Tham số 2 — từ:", fF2);
        addRow(form, c, 5, "Tham số 2 — đến:", fT2);
        addRow(form, c, 6, "Icon:", fIcon);
        c.gridx = 0;
        c.gridy = 7;
        form.add(new JLabel("Hành tinh:"), c);
        c.gridx = 1;
        form.add(cbG, c);
        c.gridx = 0;
        c.gridy = 12;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "Hai tham số là <b>khoảng</b> — máy chủ bốc ngẫu nhiên trong "
                + "khoảng đó khi tạo nội tại cho người chơi.<br>"
                + "Chọn hành tinh <b>3 — Tất cả</b> thì nội tại vào chung cả ba "
                + "hành tinh.<br>"
                + "<b>Phải khởi động lại máy chủ</b> — nội tại chỉ nạp một lần "
                + "lúc dựng.</span></html>"), c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                them ? "Thêm nội tại" : "Sửa nội tại", true);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button("Lưu", OK_GREEN, e -> {
            nro.repository.dao.KyNangDAO.NoiTai t =
                    new nro.repository.dao.KyNangDAO.NoiTai();
            try {
                t.id = Integer.parseInt(fId.getText().trim());
                t.from1 = Integer.parseInt(fF1.getText().trim());
                t.to1 = Integer.parseInt(fT1.getText().trim());
                t.from2 = Integer.parseInt(fF2.getText().trim());
                t.to2 = Integer.parseInt(fT2.getText().trim());
                t.icon = Integer.parseInt(fIcon.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Id, hai khoảng tham số và icon phải là số nguyên.");
                return;
            }
            t.ten = fTen.getText();
            t.gender = cbG.getSelectedIndex();
            String loi = nro.repository.dao.KyNangDAO.luuNoiTai(t);
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không lưu được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            napBangKyNang();
            note(OK_GREEN, "Đã lưu nội tại " + t.id
                    + " — khởi động lại máy chủ để áp dụng.");
        }));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(form, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void xoaNoiTai() {
        nro.repository.dao.KyNangDAO.NoiTai t = noiTaiDangChon();
        if (t == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá nội tại " + t.id + " — " + t.ten + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.KyNangDAO.xoaNoiTai(t.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangKyNang();
        note(OK_GREEN, "Đã xoá nội tại " + t.id
                + " — khởi động lại máy chủ để áp dụng.");
    }

    private JComponent buildAuraTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        auraTable.setRowHeight(24);
        auraTable.setAutoCreateRowSorter(true);
        auraTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w1 = {50, 240, 120, 120, 380};
        for (int i = 0; i < auraTable.getColumnCount() && i < w1.length; i++) {
            auraTable.getColumnModel().getColumn(i).setPreferredWidth(w1[i]);
        }

        auraCtTable.setRowHeight(24);
        auraCtTable.setAutoCreateRowSorter(true);
        auraCtTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w2 = {90, 300, 260};
        for (int i = 0; i < auraCtTable.getColumnCount() && i < w2.length; i++) {
            auraCtTable.getColumnModel().getColumn(i).setPreferredWidth(w2[i]);
        }

        JPanel nutAura = new JPanel(new GridLayout(0, 2, 6, 6));
        nutAura.setOpaque(false);
        nutAura.add(button("Thêm hào quang", OK_GREEN, e -> suaAuraDialog(true)));
        nutAura.add(button("Sửa hào quang", ACCENT, e -> suaAuraDialog(false)));
        nutAura.add(button("Xoá hào quang", WARN_RED, e -> xoaAura()));
        nutAura.add(button("Tải lại", GREY, e -> napBangAura()));

        JPanel tren = new JPanel(new BorderLayout(0, 6));
        tren.setOpaque(false);
        tren.add(nhan("Danh sách hào quang — bật \"Chỉ cho cải trang\" thì người "
                + "chơi không tự chọn được"), BorderLayout.NORTH);
        tren.add(ServerGuiUtils.cuon(auraTable), BorderLayout.CENTER);
        tren.add(nutAura, BorderLayout.SOUTH);

        JPanel nutCt = new JPanel(new GridLayout(0, 2, 6, 6));
        nutCt.setOpaque(false);
        nutCt.add(button("Gán hào quang cho cải trang", OK_GREEN,
                e -> ganAuraDialog()));
        nutCt.add(button("Gỡ hào quang", WARN_RED, e -> goAura()));

        JPanel duoi = new JPanel(new BorderLayout(0, 6));
        duoi.setOpaque(false);
        duoi.add(nhan("Cải trang đang gán hào quang — mặc vào là hiện sau lưng"),
                BorderLayout.NORTH);
        duoi.add(ServerGuiUtils.cuon(auraCtTable), BorderLayout.CENTER);
        duoi.add(nutCt, BorderLayout.SOUTH);

        auraNvTable.setRowHeight(24);
        auraNvTable.setAutoCreateRowSorter(true);
        auraNvTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w3 = {90, 220, 240, 300};
        for (int i = 0; i < auraNvTable.getColumnCount() && i < w3.length; i++) {
            auraNvTable.getColumnModel().getColumn(i).setPreferredWidth(w3[i]);
        }

        JPanel nutNv = new JPanel(new GridLayout(0, 2, 6, 6));
        nutNv.setOpaque(false);
        nutNv.add(button("Gán hào quang cho người chơi", OK_GREEN,
                e -> ganAuraNguoiChoiDialog()));
        nutNv.add(button("Gỡ hào quang người chơi", WARN_RED,
                e -> goAuraNguoiChoi()));

        JPanel nv = new JPanel(new BorderLayout(0, 6));
        nv.setOpaque(false);
        nv.add(nhan("Hào quang gán riêng cho người chơi — ƯU TIÊN CAO HƠN hào "
                + "quang của cải trang, mặc bộ nào cũng không mất"),
                BorderLayout.NORTH);
        nv.add(ServerGuiUtils.cuon(auraNvTable), BorderLayout.CENTER);
        nv.add(nutNv, BorderLayout.SOUTH);

        JSplitPane chiaDuoi = new JSplitPane(JSplitPane.VERTICAL_SPLIT, duoi, nv);
        chiaDuoi.setResizeWeight(0.5);
        chiaDuoi.setBorder(null);

        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tren, chiaDuoi);
        chia.setResizeWeight(0.34);
        chia.setBorder(null);
        root.add(chia, BorderLayout.CENTER);
        napBangAura();
        return root;
    }

    /** Nạp lại bảng hào quang gán riêng cho người chơi. */
    private void napBangAuraNguoiChoi() {
        auraNvModel.setRowCount(0);
        for (nro.repository.dao.AuraDAO.GanRieng g
                : nro.repository.dao.AuraDAO.danhSachGanRieng()) {
            auraNvModel.addRow(new Object[]{
                g.playerId, g.tenNhanVat, g.tenAura,
                g.ghiChu == null ? "" : g.ghiChu});
        }
    }

    /**
     * Hộp gán hào quang cho một nhân vật.
     *
     * <p>Danh sách hào quang lấy <b>toàn bộ</b>, kể cả loại đánh dấu "chỉ cho
     * cải trang": cờ đó chỉ chặn người chơi <i>tự chọn</i>, còn quản trị viên
     * trao tay thì không có lý do gì phải chặn.</p>
     */
    private void ganAuraNguoiChoiDialog() {
        java.util.List<nro.repository.dao.AuraDAO.Aura> ds
                = nro.repository.dao.AuraDAO.tatCa();
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Bảng hào quang đang trống — thêm hào quang ở bảng trên đã.",
                    "Chưa có hào quang", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.List<nro.repository.dao.PlayerDAO.AdminRow> dsNv
                = nro.repository.dao.PlayerDAO.listAllForAdmin();
        if (dsNv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không đọc được danh sách nhân vật.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> cbNv = new JComboBox<>();
        for (nro.repository.dao.PlayerDAO.AdminRow r : dsNv) {
            cbNv.addItem(r.id + " — " + r.name);
        }
        cbNv.setEditable(false);

        JComboBox<nro.repository.dao.AuraDAO.Aura> cbAura = new JComboBox<>();
        for (nro.repository.dao.AuraDAO.Aura a : ds) {
            cbAura.addItem(a);
        }

        JTextField tfGhiChu = new JTextField(20);

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Nhân vật:"));
        form.add(cbNv);
        form.add(new JLabel("Hào quang:"));
        form.add(cbAura);
        form.add(new JLabel("Ghi chú (không bắt buộc):"));
        form.add(tfGhiChu);
        form.add(new JLabel("<html><i>Gán lại cho cùng một nhân vật là "
                + "<b>thay</b>, không đẻ thêm dòng.</i></html>"));

        if (JOptionPane.showConfirmDialog(this, form,
                "Gán hào quang cho người chơi",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return;
        }

        int i = cbNv.getSelectedIndex();
        nro.repository.dao.AuraDAO.Aura a
                = (nro.repository.dao.AuraDAO.Aura) cbAura.getSelectedItem();
        if (i < 0 || i >= dsNv.size() || a == null) {
            return;
        }
        long idNv = dsNv.get(i).id;

        String loi = nro.repository.dao.AuraDAO.datGanRieng(
                idNv, a.id, tfGhiChu.getText().trim());
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không gán được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangAuraNguoiChoi();
        note(OK_GREEN, "Đã gán hào quang " + a + " cho nhân vật " + idNv
                + " — người chơi thấy sau khi đổi bản đồ hoặc đăng nhập lại.");
    }

    /** Gỡ dòng gán riêng đang chọn. */
    private void goAuraNguoiChoi() {
        int r = auraNvTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng trong bảng đã.",
                    "Chưa chọn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        long idNv = Long.parseLong(String.valueOf(auraNvModel.getValueAt(
                auraNvTable.convertRowIndexToModel(r), 0)));
        String loi = nro.repository.dao.AuraDAO.xoaGanRieng(idNv);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không gỡ được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangAuraNguoiChoi();
        note(OK_GREEN, "Đã gỡ hào quang riêng của nhân vật " + idNv + ".");
    }

    /**
     * Gắn một ô tìm vào một ô chọn: gõ tới đâu lọc tới đó.
     *
     * <p>Danh sách gốc giữ nguyên trong {@code nguon}; {@code dangHien} luôn
     * phản ánh đúng những mục đang nằm trong ô chọn, nên chỗ đọc kết quả phải
     * đọc từ {@code dangHien} chứ không phải {@code nguon} — đọc nhầm là chọn
     * đúng dòng nhưng nhận về món khác ngay khi có chữ trong ô tìm.</p>
     *
     * @param nhanCua đổi một mục thành dòng chữ hiện trong ô chọn
     */
    private static <T> void ganOTim(final JTextField oTim,
            final JComboBox<String> oChon, final java.util.List<T> nguon,
            final java.util.List<T> dangHien,
            final java.util.function.Function<T, String> nhanCua) {
        final Runnable loc = () -> {
            String tim = oTim.getText().trim().toLowerCase();
            String dangChon = (String) oChon.getSelectedItem();
            oChon.removeAllItems();
            dangHien.clear();
            for (T x : nguon) {
                String nhan = nhanCua.apply(x);
                if (tim.isEmpty() || nhan.toLowerCase().contains(tim)) {
                    oChon.addItem(nhan);
                    dangHien.add(x);
                }
            }
            // Giu nguyen muc dang chon neu no van con sau khi loc.
            if (dangChon != null) {
                for (int i = 0; i < oChon.getItemCount(); i++) {
                    if (dangChon.equals(oChon.getItemAt(i))) {
                        oChon.setSelectedIndex(i);
                        break;
                    }
                }
            }
        };
        loc.run();
        oTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
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
    }

    // =====================================================================
    //  Tab: Danh hiệu
    // =====================================================================

    /**
     * Quản lý danh hiệu: thêm/sửa/xoá, và cấp cho một nhân vật.
     *
     * <p>Chỉ số của danh hiệu sửa ngay trong hộp thoại — thêm hoặc bớt từng
     * dòng, chọn loại chỉ số từ danh sách thay vì nhớ id.</p>
     */
    private JComponent buildDanhHieuTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        dhTable.setRowHeight(30);
        // Nhay dup mot dong = sua danh hieu do, ke ca phan cach nhan.
        dhTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && dhTable.getSelectedRow() >= 0) {
                    suaDanhHieuDialog(false);
                }
            }
        });
        dhTable.setAutoCreateRowSorter(true);
        dhTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {50, 90, 100, 220, 420, 120};
        for (int i = 0; i < dhTable.getColumnCount() && i < w.length; i++) {
            dhTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel nut = new JPanel(new GridLayout(0, 3, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm danh hiệu", OK_GREEN, e -> suaDanhHieuDialog(true)));
        nut.add(button("Sửa danh hiệu", ACCENT, e -> suaDanhHieuDialog(false)));
        nut.add(button("Xoá danh hiệu", WARN_RED, e -> xoaDanhHieu()));
        nut.add(button("Cấp cho nhân vật…", OK_GREEN, e -> capDanhHieuDialog()));
        nut.add(button("Thu hồi của nhân vật…", WARN_RED, e -> thuHoiDanhHieuDialog()));
        nut.add(button("Cách nhận…", new Color(120, 90, 160), e -> cachNhanDialog()));
        nut.add(button("Tải lại", GREY, e -> napBangDanhHieu()));

        root.add(nhan("Danh hiệu — cột \"Chỉ số\" là các dòng cộng thêm khi đeo. "
                + "Người chơi chọn danh hiệu ở NPC Santa, bấm lại chính danh hiệu "
                + "đang đeo để bỏ."), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(dhTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangDanhHieu();
        return root;
    }


    /** Ảnh của danh hiệu — lấy theo vật phẩm dùng để đổi. */
    private static javax.swing.ImageIcon anhDanhHieu(
            nro.repository.dao.DanhHieuDAO.DanhHieu d) {
        if (d == null || d.idItem <= 0) {
            return null;
        }
        try {
            nro.entity.template.ItemTemplate t
                    = nro.service.item.ItemService.gI().getTemplate(d.idItem);
            return t == null ? null : PlayerManagerPanel.iconOf(t.iconID);
        } catch (Exception boQua) {
            return null;
        }
    }

    private void napBangDanhHieu() {
        dhModel.setRowCount(0);
        java.util.List<nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu> nvs
                = nro.repository.dao.NhiemVuDanhHieuDAO.danhSach();
        for (nro.repository.dao.DanhHieuDAO.DanhHieu d
                : nro.repository.dao.DanhHieuDAO.danhSach()) {
            dhModel.addRow(new Object[]{anhDanhHieu(d), d.id, d.idEffect, d.idItem, d.ten,
                d.moTaChiSo(), cachNhan(d, nvs),
                nro.repository.dao.DanhHieuDAO.soNguoiDangGiu(d.idEffect)});
        }
    }

    /**
     * Câu tóm tắt <b>mọi đường</b> lấy được một danh hiệu.
     *
     * <h2>Vì sao ghép ở đây</h2>
     *
     * <p>Một danh hiệu có tới hai đường vào, nằm ở hai bảng khác nhau: đổi bằng
     * vật phẩm ({@code idItem}), và làm xong một nhiệm vụ danh hiệu. Nhìn riêng
     * bảng nào cũng chỉ thấy một nửa — mở tab Danh hiệu thì không biết phải làm
     * gì mới có, mở tab Nhiệm vụ danh hiệu thì thấy id thưởng mà không biết đó
     * là danh hiệu nào.</p>
     *
     * <p><b>Nhiệm vụ trỏ vào {@code idEffect}, không phải {@code id}.</b> Đó là
     * chỗ dễ nối nhầm: {@code BadgesTaskService} so
     * {@code data.idBadgesReward} với {@code temp.idEffect}. Nối theo {@code id}
     * thì bảng vẫn hiện ra bình thường, chỉ là ghép sai danh hiệu.</p>
     */
    private static String cachNhan(nro.repository.dao.DanhHieuDAO.DanhHieu d,
            java.util.List<nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu> nvs) {
        StringBuilder sb = new StringBuilder();
        if (d.idItem > 0) {
            sb.append("Đổi bằng vật phẩm #").append(d.idItem);
        }
        for (nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n : nvs) {
            if (n.danhHieuThuong != d.idEffect) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(n.ten == null || n.ten.trim().isEmpty()
                    ? ("nhiệm vụ #" + n.id) : n.ten.trim());
            sb.append(" (").append(n.moTaLoai());
            if (n.soLan > 1) {
                sb.append(" ×").append(n.soLan);
            }
            sb.append(')');
        }
        return sb.length() == 0 ? "— chỉ admin cấp tay" : sb.toString();
    }

    /**
     * Hộp thêm một cách nhận, <b>chọn từ mẫu có sẵn</b>.
     *
     * <h2>Vì sao có danh sách mẫu</h2>
     *
     * <p>Phần khó của việc đặt một cách nhận không phải gõ chữ, mà là biết loại
     * nào <b>đếm được</b> và con số nào thì hợp lý. Bảng mẫu trong
     * {@code BadgesTaskTemplate} là các cặp (loại, số lần) đã cân sẵn — chọn một
     * dòng là ba ô dưới tự điền, sửa lại nếu muốn.</p>
     *
     * <p>Ô "Tham số" đổi nhãn theo loại đang chọn, vì cùng một ô ấy khi thì là
     * id quái, khi là id bản đồ, khi là số sao. Loại nào không dùng tham số thì
     * ô bị khoá lại chứ không để trống lửng lơ.</p>
     *
     * @return {@code true} nếu đã thêm được
     */

    /**
     * Hộp sửa một cách nhận đã có.
     *
     * <p>Dùng lại đúng bộ ô của {@link #themCachNhanDialog}: mẫu có sẵn, loại,
     * số lần, tham số. Khác một điểm — danh hiệu thưởng giữ nguyên, vì đổi nó là
     * chuyển nhiệm vụ này sang danh hiệu khác, việc đó nên làm ở tab nhiệm vụ
     * chứ không phải lẫn trong hộp sửa của một danh hiệu.</p>
     *
     * @return {@code true} nếu đã lưu
     */
    private boolean suaCachNhanDialog(int idNhiemVu) {
        nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n
                = nro.repository.dao.NhiemVuDanhHieuDAO.theoId(idNhiemVu);
        if (n == null) {
            note(WARN_RED, "Không tìm thấy nhiệm vụ id " + idNhiemVu + ".");
            return false;
        }
        JTextField fTen = new JTextField(n.ten, 30);
        JComboBox<String> oLoai = new JComboBox<>();
        for (String l : nro.entity.badges.BadgesTaskTemplate.cacLoai()) {
            oLoai.addItem(l);
        }
        oLoai.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                setText(nro.entity.badges.BadgesTaskTemplate
                        .tenLoai(String.valueOf(value)));
                return this;
            }
        });
        oLoai.setSelectedItem(n.loai);
        JTextField fSoLan = new JTextField(String.valueOf(n.soLan), 10);
        JTextField fThamSo = new JTextField(String.valueOf(n.thamSo), 10);
        JLabel nhanThamSo = new JLabel(" ");
        nhanThamSo.setForeground(GREY);
        Runnable capNhat = () -> {
            String y = nro.entity.badges.BadgesTaskTemplate.yNghiaThamSo(
                    String.valueOf(oLoai.getSelectedItem()));
            fThamSo.setEnabled(!y.isEmpty());
            nhanThamSo.setText(y.isEmpty() ? "Loại này không dùng tham số" : y);
        };
        oLoai.addActionListener(e -> capNhat.run());
        capNhat.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        int r = 0;
        addRowC(form, c, r++, "Tên hiện cho người chơi:", fTen);
        addRowC(form, c, r++, "Loại:", oLoai);
        addRowC(form, c, r++, "Số lần cần đạt:", fSoLan);
        addRowC(form, c, r++, "Tham số:", fThamSo);
        c.gridx = 1;
        c.gridy = r++;
        form.add(nhanThamSo, c);

        if (JOptionPane.showConfirmDialog(this, form, "Sửa cách nhận",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return false;
        }
        n.ten = fTen.getText().trim();
        n.loai = String.valueOf(oLoai.getSelectedItem());
        try {
            n.soLan = Integer.parseInt(fSoLan.getText().trim().replace(".", ""));
            n.thamSo = Integer.parseInt(fThamSo.getText().trim().replace(".", ""));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "\"Số lần\" và \"Tham số\" phải là số nguyên.");
            return false;
        }
        String loi = nro.repository.dao.NhiemVuDanhHieuDAO.sua(n);
        if (loi != null) {
            note(WARN_RED, loi);
            return false;
        }
        note(OK_GREEN, "Đã sửa cách nhận.");
        return true;
    }

    private boolean themCachNhanDialog(nro.repository.dao.DanhHieuDAO.DanhHieu d) {
        JComboBox<nro.entity.badges.BadgesTaskTemplate.Mau> oMau
                = new JComboBox<>(nro.entity.badges.BadgesTaskTemplate.cacMau());
        JTextField fTen = new JTextField(26);
        JComboBox<String> oLoai = new JComboBox<>();
        for (String l : nro.entity.badges.BadgesTaskTemplate.cacLoai()) {
            oLoai.addItem(l);
        }
        oLoai.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                setText(nro.entity.badges.BadgesTaskTemplate
                        .tenLoai(String.valueOf(value)));
                return this;
            }
        });
        JTextField fSoLan = new JTextField(10);
        JTextField fThamSo = new JTextField(10);
        JLabel nhanThamSo = new JLabel(" ");
        nhanThamSo.setForeground(GREY);

        Runnable capNhatThamSo = () -> {
            String loai = String.valueOf(oLoai.getSelectedItem());
            String y = nro.entity.badges.BadgesTaskTemplate.yNghiaThamSo(loai);
            boolean dung = !y.isEmpty();
            fThamSo.setEnabled(dung);
            if (!dung) {
                fThamSo.setText("-1");
            }
            nhanThamSo.setText(dung ? y : "Loại này không dùng tham số");
        };
        oLoai.addActionListener(e -> capNhatThamSo.run());

        Runnable dienTuMau = () -> {
            nro.entity.badges.BadgesTaskTemplate.Mau m
                    = (nro.entity.badges.BadgesTaskTemplate.Mau) oMau.getSelectedItem();
            if (m == null) {
                return;
            }
            fTen.setText(m.ten);
            oLoai.setSelectedItem(m.loai);
            fSoLan.setText(String.valueOf(m.soLan));
            fThamSo.setText(String.valueOf(m.thamSo));
            capNhatThamSo.run();
        };
        oMau.addActionListener(e -> dienTuMau.run());
        dienTuMau.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        int r = 0;
        c.gridx = 0;
        c.gridy = r;
        form.add(new JLabel("Mẫu có sẵn:"), c);
        c.gridx = 1;
        form.add(oMau, c);
        c.gridx = 0;
        c.gridy = ++r;
        form.add(new JLabel("Tên hiện cho người chơi:"), c);
        c.gridx = 1;
        form.add(fTen, c);
        c.gridx = 0;
        c.gridy = ++r;
        form.add(new JLabel("Loại:"), c);
        c.gridx = 1;
        form.add(oLoai, c);
        c.gridx = 0;
        c.gridy = ++r;
        form.add(new JLabel("Số lần cần đạt:"), c);
        c.gridx = 1;
        form.add(fSoLan, c);
        c.gridx = 0;
        c.gridy = ++r;
        form.add(new JLabel("Tham số:"), c);
        c.gridx = 1;
        form.add(fThamSo, c);
        c.gridx = 1;
        c.gridy = ++r;
        form.add(nhanThamSo, c);
        c.gridx = 0;
        c.gridy = ++r;
        c.gridwidth = 2;
        form.add(nhan("Thưởng: danh hiệu <b>" + d.ten + "</b> (id hiệu ứng "
                + d.idEffect + ")."), c);

        if (JOptionPane.showConfirmDialog(this, form, "Thêm cách nhận",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return false;
        }
        nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n
                = new nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu();
        n.ten = fTen.getText().trim();
        n.loai = String.valueOf(oLoai.getSelectedItem());
        n.danhHieuThuong = d.idEffect;
        try {
            n.soLan = Integer.parseInt(fSoLan.getText().trim().replace(".", ""));
            n.thamSo = Integer.parseInt(fThamSo.getText().trim().replace(".", ""));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "\"Số lần\" và \"Tham số\" phải là số nguyên.");
            return false;
        }
        String loi = nro.repository.dao.NhiemVuDanhHieuDAO.them(n);
        if (loi != null) {
            note(WARN_RED, loi);
            return false;
        }
        note(OK_GREEN, nro.entity.badges.BadgesTaskTemplate.GO_CUNG.equals(n.loai)
                ? "Đã thêm — nhưng loại \"mã nguồn tự đếm\" thì không có gì đếm "
                + "cho nó, phải có người viết mã."
                : "Đã thêm cách nhận — người đăng nhập sau sẽ thấy.");
        return true;
    }

    /**
     * Quản lý <b>cách nhận</b> danh hiệu đang chọn.
     *
     * <p>Mở thẳng danh sách nhiệm vụ trao danh hiệu này, thêm bớt tại chỗ. Vẫn
     * là bảng {@code task_badges_template} của tab "Nhiệm vụ danh hiệu" — chỉ
     * khác là đã lọc sẵn và điền sẵn danh hiệu thưởng, nên không phải nhớ id
     * hiệu ứng rồi sang tab kia dò.</p>
     */
    private void cachNhanDialog() {
        nro.repository.dao.DanhHieuDAO.DanhHieu d = dhDangChon();
        if (d == null) {
            return;
        }
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Id", "Tên nhiệm vụ", "Loại", "Số lần"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable bang = new JTable(m);
        bang.setRowHeight(24);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Runnable nap = () -> {
            m.setRowCount(0);
            for (nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n
                    : nro.repository.dao.NhiemVuDanhHieuDAO.danhSach()) {
                if (n.danhHieuThuong == d.idEffect) {
                    m.addRow(new Object[]{n.id, n.ten, n.moTaLoai(), n.soLan});
                }
            }
        };
        nap.run();

        JPanel hop = new JPanel(new BorderLayout(0, 8));
        hop.setPreferredSize(new Dimension(760, 380));
        hop.add(nhan("Các nhiệm vụ trao danh hiệu <b>" + d.ten + "</b> "
                + "(id hiệu ứng " + d.idEffect + ")."
                + (d.idItem > 0 ? "<br>Ngoài ra còn đổi được bằng vật phẩm #"
                        + d.idItem + "." : "")
                + "<br><br>Loại \"gõ cứng\" nghĩa là tiến độ do mã nguồn tự cộng ở "
                + "một chỗ đã viết sẵn — sửa chữ thì được, nhưng tạo mới một nhiệm "
                + "vụ loại đó thì không có gì đếm cho nó. Các loại còn lại "
                + "(giết quái, hạ boss, mua/dùng/nhặt vật phẩm, tiêu vàng…) tự đếm, "
                + "nên thêm một dòng là chạy ngay."), BorderLayout.NORTH);
        hop.add(ServerGuiUtils.cuon(bang), BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm cách nhận…", OK_GREEN, e -> {
            if (themCachNhanDialog(d)) {
                nap.run();
                napBangDanhHieu();
            }
        }));
        nut.add(button("Xoá cách nhận", WARN_RED, e -> {
            int r = bang.getSelectedRow();
            if (r < 0) {
                note(WARN_RED, "Chưa chọn dòng nào.");
                return;
            }
            int id = Integer.parseInt(String.valueOf(m.getValueAt(r, 0)));
            if (JOptionPane.showConfirmDialog(this,
                    "Xoá nhiệm vụ \"" + m.getValueAt(r, 1) + "\"?\n\n"
                    + "Ai đang làm dở nhiệm vụ này sẽ mất tiến độ.",
                    "Xoá cách nhận", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            String loi = nro.repository.dao.NhiemVuDanhHieuDAO.xoa(id);
            nap.run();
            napBangDanhHieu();
            note(loi == null ? OK_GREEN : WARN_RED,
                    loi == null ? "Đã xoá." : loi);
        }));
        hop.add(nut, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, hop,
                "Cách nhận danh hiệu", JOptionPane.PLAIN_MESSAGE);
    }

    private nro.repository.dao.DanhHieuDAO.DanhHieu dhDangChon() {
        int r = dhTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một danh hiệu trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(dhModel.getValueAt(
                dhTable.convertRowIndexToModel(r), 1)));
        return nro.repository.dao.DanhHieuDAO.theoId(id);
    }

    private void suaDanhHieuDialog(boolean them) {
        final nro.repository.dao.DanhHieuDAO.DanhHieu d;
        if (them) {
            d = new nro.repository.dao.DanhHieuDAO.DanhHieu();
        } else {
            d = dhDangChon();
            if (d == null) {
                return;
            }
        }

        JTextField fTen = new JTextField(d.ten, 26);
        JTextField fEffect = new JTextField(
                d.idEffect == 0 ? "" : String.valueOf(d.idEffect), 8);
        JButton btnEffect = button("Chọn…", GREY, e -> {
            Integer id = chonIdHieuUng(d.id);
            if (id != null) {
                fEffect.setText(String.valueOf(id));
            }
        });
        JTextField fItem = new JTextField(
                d.idItem == 0 ? "" : String.valueOf(d.idItem), 8);
        JButton btnItem = button("Chọn…", GREY, e -> {
            int id = OptionPicker.chonVatPham(this,
                    fItem.getText().trim().isEmpty() ? -1
                            : Integer.parseInt(fItem.getText().trim()));
            if (id >= 0) {
                fItem.setText(String.valueOf(id));
            }
        });

        // ---- bảng chỉ số: thêm / bớt từng dòng
        DefaultTableModel mOpt = new DefaultTableModel(
                new Object[]{"Loại chỉ số", "Giá trị"}, 0);
        for (nro.entity.item.ItemOption io
                : nro.repository.dao.DanhHieuDAO.docOptions(d.options)) {
            mOpt.addRow(new Object[]{
                String.valueOf(io.optionTemplate == null ? 0 : io.optionTemplate.id),
                String.valueOf(io.param)});
        }
        JTable tOpt = new JTable(mOpt);
        tOpt.setRowHeight(24);
        OptionPicker.install(tOpt, 0);

        JPanel nutOpt = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nutOpt.setOpaque(false);
        nutOpt.add(button("Thêm chỉ số", OK_GREEN,
                e -> mOpt.addRow(new Object[]{"", "0"})));
        nutOpt.add(button("Bớt chỉ số", WARN_RED, e -> {
            int r = tOpt.getSelectedRow();
            if (r >= 0) {
                if (tOpt.isEditing()) {
                    tOpt.getCellEditor().stopCellEditing();
                }
                mOpt.removeRow(r);
            }
        }));

        JPanel form = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new java.awt.Insets(4, 4, 4, 4);
        g.anchor = java.awt.GridBagConstraints.WEST;
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        form.add(new JLabel("Tên danh hiệu:"), g);
        g.gridx = 1;
        form.add(fTen, g);
        g.gridx = 0;
        g.gridy = ++y;
        form.add(new JLabel("Id hiệu ứng:"), g);
        g.gridx = 1;
        JPanel hangEff = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangEff.setOpaque(false);
        hangEff.add(fEffect);
        hangEff.add(btnEffect);
        form.add(hangEff, g);
        g.gridx = 0;
        g.gridy = ++y;
        form.add(new JLabel("Vật phẩm để đổi:"), g);
        g.gridx = 1;
        JPanel hangItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangItem.setOpaque(false);
        hangItem.add(fItem);
        hangItem.add(btnItem);
        form.add(hangItem, g);
        g.gridx = 0;
        g.gridy = ++y;
        g.gridwidth = 2;
        form.add(nhan("Chỉ số cộng thêm khi đeo danh hiệu:"), g);
        g.gridy = ++y;
        JScrollPane sp = ServerGuiUtils.cuon(tOpt);
        sp.setPreferredSize(new java.awt.Dimension(420, 150));
        form.add(sp, g);
        g.gridy = ++y;
        form.add(nutOpt, g);

        // ---------- cách nhận: nhiệm vụ trao danh hiệu này ----------
        //
        // Gop vao day thay vi de mot tab rieng: hai thu la MOT viec. Tach ra
        // thi dat mot danh hieu xong con phai nho sang tab kia, nho id hieu ung
        // cua no, roi do trong mot bang chung cua moi danh hieu. Khong ai lam
        // dung duoc buoc hai, va danh hieu ra doi ma khong co duong nhan.
        DefaultTableModel mNv = new DefaultTableModel(
                new Object[]{"Id", "Tên nhiệm vụ", "Loại", "Số lần"}, 0) {
            @Override
            public boolean isCellEditable(int r, int cc) {
                return false;
            }
        };
        JTable bNv = new JTable(mNv);
        bNv.setRowHeight(24);
        bNv.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Runnable napNv = () -> {
            mNv.setRowCount(0);
            if (them && d.idEffect <= 0) {
                return;
            }
            for (nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n
                    : nro.repository.dao.NhiemVuDanhHieuDAO.danhSach()) {
                if (n.danhHieuThuong == d.idEffect) {
                    mNv.addRow(new Object[]{n.id, n.ten, n.moTaLoai(), n.soLan});
                }
            }
        };
        napNv.run();

        JPanel nutNv = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        nutNv.setOpaque(false);
        nutNv.add(button("Thêm cách nhận…", OK_GREEN, ev -> {
            if (d.idEffect <= 0) {
                note(WARN_RED, "Chọn id hiệu ứng trước đã — cách nhận gắn theo id đó.");
                return;
            }
            if (themCachNhanDialog(d)) {
                napNv.run();
            }
        }));
        nutNv.add(button("Sửa cách nhận…", ACCENT, ev -> {
            int r = bNv.getSelectedRow();
            if (r < 0) {
                note(WARN_RED, "Chưa chọn cách nhận nào.");
                return;
            }
            int idNv = Integer.parseInt(String.valueOf(mNv.getValueAt(r, 0)));
            if (suaCachNhanDialog(idNv)) {
                napNv.run();
            }
        }));
        nutNv.add(button("Xoá cách nhận", WARN_RED, ev -> {
            int r = bNv.getSelectedRow();
            if (r < 0) {
                note(WARN_RED, "Chưa chọn cách nhận nào.");
                return;
            }
            int idNv = Integer.parseInt(String.valueOf(mNv.getValueAt(r, 0)));
            if (JOptionPane.showConfirmDialog(this,
                    "Xoá nhiệm vụ \"" + mNv.getValueAt(r, 1) + "\"?\n\n"
                    + "Ai đang làm dở sẽ mất tiến độ.",
                    "Xoá cách nhận", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            nro.repository.dao.NhiemVuDanhHieuDAO.xoa(idNv);
            napNv.run();
        }));

        g.gridy = ++y;
        form.add(nhan("<b>Cách nhận danh hiệu này</b> — các nhiệm vụ trao nó. "
                + (d.idItem > 0 ? "Ngoài ra còn đổi được bằng vật phẩm ở trên. " : "")
                + "Để trống thì chỉ quản trị viên cấp tay được."), g);
        g.gridy = ++y;
        JScrollPane spNv = ServerGuiUtils.cuon(bNv);
        spNv.setPreferredSize(new java.awt.Dimension(420, 110));
        form.add(spNv, g);
        g.gridy = ++y;
        form.add(nutNv, g);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm danh hiệu" : "Sửa danh hiệu",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (tOpt.isEditing()) {
            tOpt.getCellEditor().stopCellEditing();
        }

        try {
            d.idEffect = Integer.parseInt(fEffect.getText().trim());
            d.idItem = fItem.getText().trim().isEmpty() ? 0
                    : Integer.parseInt(fItem.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Id hiệu ứng và id vật phẩm phải là số.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        d.ten = fTen.getText().trim();

        java.util.List<int[]> dong = new java.util.ArrayList<>();
        for (int i = 0; i < mOpt.getRowCount(); i++) {
            String a = String.valueOf(mOpt.getValueAt(i, 0)).trim();
            String b = String.valueOf(mOpt.getValueAt(i, 1)).trim();
            if (a.isEmpty()) {
                continue;
            }
            try {
                dong.add(new int[]{Integer.parseInt(OptionPicker.laySo(a)),
                    b.isEmpty() ? 0 : Integer.parseInt(b)});
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Dòng chỉ số thứ " + (i + 1) + " không phải số.",
                        "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        d.options = nro.repository.dao.DanhHieuDAO.vietOptions(dong);

        String loi = them ? nro.repository.dao.DanhHieuDAO.them(d)
                : nro.repository.dao.DanhHieuDAO.sua(d);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangDanhHieu();
        note(OK_GREEN, them ? "Đã thêm danh hiệu." : "Đã lưu danh hiệu.");
    }

    private void xoaDanhHieu() {
        nro.repository.dao.DanhHieuDAO.DanhHieu d = dhDangChon();
        if (d == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xoá danh hiệu \"" + d.ten + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.DanhHieuDAO.xoa(d.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangDanhHieu();
        note(OK_GREEN, "Đã xoá danh hiệu.");
    }

    private void capDanhHieuDialog() {
        nro.repository.dao.DanhHieuDAO.DanhHieu d = dhDangChon();
        if (d == null) {
            return;
        }
        JTextField fTen = new JTextField(18);
        JButton btnChonNv = button("Chọn…", GREY, e -> {
            String t = chonNhanVat(fTen.getText());
            if (t != null) {
                fTen.setText(t);
            }
        });
        JTextField fNgay = new JTextField("0", 6);
        JCheckBox cbDeo = new JCheckBox("Đeo luôn cho nhân vật", true);

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Tên nhân vật:"));
        JPanel hangNv = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangNv.setOpaque(false);
        hangNv.add(fTen);
        hangNv.add(btnChonNv);
        p.add(hangNv);
        p.add(new JLabel("Số ngày (0 = vĩnh viễn):"));
        p.add(fNgay);
        p.add(new JLabel(""));
        p.add(cbDeo);

        if (JOptionPane.showConfirmDialog(this, p,
                "Cấp danh hiệu \"" + d.ten + "\"", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int ngay;
        try {
            ngay = Integer.parseInt(fNgay.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số ngày phải là số.",
                    "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String loi = nro.repository.dao.DanhHieuDAO.capChoNhanVat(
                fTen.getText(), d.idEffect, ngay, cbDeo.isSelected());
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không cấp được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangDanhHieu();
        note(OK_GREEN, "Đã cấp \"" + d.ten + "\" cho " + fTen.getText().trim()
                + (ngay <= 0 ? " (vĩnh viễn)." : " (" + ngay + " ngày)."));
    }

    /**
     * Hộp chọn id hiệu ứng cho danh hiệu.
     *
     * <p>Không có bảng hiệu ứng nào trong CSDL — {@code idEffect} chỉ là con số
     * client tự hiểu, nên không liệt kê "tên hiệu ứng" được. Thứ giúp được thật
     * sự là cho thấy <b>id nào đang bị chiếm</b> và <b>id trống tiếp theo</b>:
     * trùng id là hai danh hiệu đè nhau, mà lỗi đó chỉ lộ ra khi người chơi đeo
     * vào mới thấy sai.</p>
     *
     * @param boQuaId id dòng danh hiệu đang sửa, để không tự coi mình là trùng
     * @return id đã chọn, hoặc {@code null} nếu huỷ
     */
    private Integer chonIdHieuUng(int boQuaId) {
        java.util.List<nro.repository.dao.DanhHieuDAO.DanhHieu> ds =
                nro.repository.dao.DanhHieuDAO.danhSach();
        java.util.Set<Integer> daDung = new java.util.TreeSet<>();
        for (nro.repository.dao.DanhHieuDAO.DanhHieu x : ds) {
            if (x.id != boQuaId) {
                daDung.add(x.idEffect);
            }
        }
        int tim = daDung.isEmpty() ? 218
                : ((java.util.TreeSet<Integer>) daDung).last() + 1;
        while (daDung.contains(tim)) {
            tim++;
        }
        final int goiY = tim;

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Id hiệu ứng", "Đang dùng cho"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (nro.repository.dao.DanhHieuDAO.DanhHieu x : ds) {
            m.addRow(new Object[]{x.idEffect,
                x.id == boQuaId ? x.ten + "   (chính dòng đang sửa)" : x.ten});
        }
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextField oId = new JTextField(String.valueOf(goiY), 8);
        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tren.add(new JLabel("Id muốn dùng:"));
        tren.add(oId);
        tren.add(button("Lấy id trống", GREY,
                e -> oId.setText(String.valueOf(goiY))));

        t.getSelectionModel().addListSelectionListener(e -> {
            int r = t.getSelectedRow();
            if (r >= 0) {
                oId.setText(String.valueOf(m.getValueAt(r, 0)));
            }
        });

        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.add(tren, BorderLayout.NORTH);
        JScrollPane sp = ServerGuiUtils.cuon(t);
        sp.setPreferredSize(new java.awt.Dimension(400, 300));
        p.add(sp, BorderLayout.CENTER);
        p.add(nhan("Bấm một dòng để lấy id đó. Id trống tiếp theo: " + goiY
                + " — trùng id là hai danh hiệu đè nhau."), BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, p, "Chọn id hiệu ứng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            int id = Integer.parseInt(oId.getText().trim());
            if (daDung.contains(id)) {
                JOptionPane.showMessageDialog(this,
                        "Id " + id + " đang được danh hiệu khác dùng.",
                        "Trùng id", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return id;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Id phải là số.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    /**
     * Hộp chọn nhân vật — gõ tới đâu lọc tới đó.
     *
     * <p>Máy chủ có hàng nghìn nhân vật; bắt gõ đúng tên thì sai một dấu là cấp
     * nhầm người mà không biết.</p>
     *
     * @return tên đã chọn, hoặc {@code null} nếu huỷ
     */
    private String chonNhanVat(String dangCo) {
        java.util.List<String> tatCa = new java.util.ArrayList<>();
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT NAME FROM player WHERE NAME IS NOT NULL AND NAME <> ''"
                    + " ORDER BY NAME");
            while (rs.next()) {
                tatCa.add(rs.getString("NAME"));
            }
            rs.dispose();
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Lỗi đọc danh sách nhân vật");
        }
        if (tatCa.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không đọc được danh sách nhân vật.\n"
                    + "Kiểm tra MySQL đã chạy chưa (XAMPP → Start MariaDB).",
                    "Chưa có dữ liệu", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        DefaultTableModel m = new DefaultTableModel(new Object[]{"Nhân vật"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextField oTim = new JTextField(dangCo == null ? "" : dangCo, 22);
        final Runnable loc = () -> {
            String tim = oTim.getText().trim().toLowerCase();
            m.setRowCount(0);
            for (String s : tatCa) {
                if (tim.isEmpty() || s.toLowerCase().contains(tim)) {
                    m.addRow(new Object[]{s});
                }
            }
            if (m.getRowCount() > 0) {
                t.setRowSelectionInterval(0, 0);
            }
        };
        loc.run();
        oTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
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

        JPanel p = new JPanel(new BorderLayout(0, 6));
        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tren.add(new JLabel("Tìm:"));
        tren.add(oTim);
        p.add(tren, BorderLayout.NORTH);
        JScrollPane sp = ServerGuiUtils.cuon(t);
        sp.setPreferredSize(new java.awt.Dimension(360, 320));
        p.add(sp, BorderLayout.CENTER);
        p.add(nhan("Có " + tatCa.size() + " nhân vật."), BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, p, "Chọn nhân vật",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        int r = t.getSelectedRow();
        return r < 0 ? null : String.valueOf(m.getValueAt(r, 0));
    }

    private void thuHoiDanhHieuDialog() {
        nro.repository.dao.DanhHieuDAO.DanhHieu d = dhDangChon();
        if (d == null) {
            return;
        }
        String ten = chonNhanVat(null);
        if (ten == null || ten.trim().isEmpty()) {
            return;
        }
        String loi = nro.repository.dao.DanhHieuDAO.thuHoiCuaNhanVat(ten, d.idEffect);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không thu hồi được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangDanhHieu();
        note(OK_GREEN, "Đã thu hồi \"" + d.ten + "\" của " + ten.trim() + ".");
    }

    // =====================================================================
    //  Tab: Nhiệm vụ danh hiệu
    // =====================================================================

    /**
     * Nhiệm vụ để nhận danh hiệu.
     *
     * <p>Loại "Mã nguồn tự đếm" là các nhiệm vụ viết sẵn trong mã — panel chỉ
     * sửa được chữ, số lần và phần thưởng. Các loại còn lại <b>tự đếm</b>: thêm
     * một dòng ở đây là có nhiệm vụ mới chạy được ngay.</p>
     */
    private JComponent buildNhiemVuTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        nvTable.setRowHeight(24);
        nvTable.setAutoCreateRowSorter(true);
        nvTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {50, 420, 200, 120, 240};
        for (int i = 0; i < nvTable.getColumnCount() && i < w.length; i++) {
            nvTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel nut = new JPanel(new GridLayout(0, 4, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm nhiệm vụ", OK_GREEN, e -> suaNhiemVuDialog(true)));
        nut.add(button("Sửa nhiệm vụ", ACCENT, e -> suaNhiemVuDialog(false)));
        nut.add(button("Xoá nhiệm vụ", WARN_RED, e -> xoaNhiemVu()));
        nut.add(button("Tải lại", GREY, e -> napBangNhiemVu()));

        root.add(nhan("<html>Nhiệm vụ hoàn thành thì người chơi nhận danh hiệu "
                + "thưởng trong <b>30 ngày</b>. Nhiệm vụ mới thêm chỉ tới với "
                + "người chơi ở <b>lần đăng nhập sau</b> — dựng lại giữa chừng "
                + "sẽ xoá sạch tiến độ đang có.</html>"), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(nvTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        return root;
    }

    private void napBangNhiemVu() {
        nvModel.setRowCount(0);
        java.util.Map<Integer, String> tenDh = new java.util.HashMap<>();
        for (nro.repository.dao.DanhHieuDAO.DanhHieu d
                : nro.repository.dao.DanhHieuDAO.danhSach()) {
            tenDh.put(d.idEffect, d.ten);
        }
        for (nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n
                : nro.repository.dao.NhiemVuDanhHieuDAO.danhSach()) {
            String dh = n.danhHieuThuong < 0 ? "(không có)"
                    : n.danhHieuThuong + " — " + tenDh.getOrDefault(
                            n.danhHieuThuong, "(KHÔNG có danh hiệu này)");
            nvModel.addRow(new Object[]{n.id, n.ten, n.moTaLoai(),
                PlayerManagerPanel.fmt(n.soLan), dh});
        }
    }

    private nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu nvDangChon() {
        int r = nvTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một nhiệm vụ trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(nvModel.getValueAt(
                nvTable.convertRowIndexToModel(r), 0)));
        return nro.repository.dao.NhiemVuDanhHieuDAO.theoId(id);
    }

    private void suaNhiemVuDialog(boolean them) {
        final nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n;
        if (them) {
            n = new nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu();
        } else {
            n = nvDangChon();
            if (n == null) {
                return;
            }
        }

        JTextField fTen = new JTextField(n.ten, 34);
        JTextField fSoLan = new JTextField(String.valueOf(n.soLan), 10);

        String[] cacLoai = nro.entity.badges.BadgesTaskTemplate.cacLoai();
        String[] nhan = new String[cacLoai.length];
        for (int i = 0; i < cacLoai.length; i++) {
            nhan[i] = nro.entity.badges.BadgesTaskTemplate.tenLoai(cacLoai[i]);
        }
        JComboBox<String> cbLoai = new JComboBox<>(nhan);
        for (int i = 0; i < cacLoai.length; i++) {
            if (cacLoai[i].equals(n.loai)) {
                cbLoai.setSelectedIndex(i);
                break;
            }
        }

        JTextField fThamSo = new JTextField(String.valueOf(n.thamSo), 10);
        JLabel lblThamSo = new JLabel();
        JButton btnThamSo = button("Chọn…", GREY, e -> {
            String l = cacLoai[cbLoai.getSelectedIndex()];
            int id = -1;
            if (nro.entity.badges.BadgesTaskTemplate.MUA_VAT_PHAM.equals(l)
                    || nro.entity.badges.BadgesTaskTemplate.DUNG_VAT_PHAM.equals(l)
                    || nro.entity.badges.BadgesTaskTemplate.NHAT_VAT_PHAM.equals(l)) {
                id = OptionPicker.chonVatPham(this, -1);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Loại này gõ thẳng id (quái/boss), hoặc để -1 cho bất kỳ.",
                        "Chưa có bộ chọn", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (id >= 0) {
                fThamSo.setText(String.valueOf(id));
            }
        });

        final Runnable moTa = () -> {
            String l = cacLoai[cbLoai.getSelectedIndex()];
            boolean dungThamSo =
                    !nro.entity.badges.BadgesTaskTemplate.GO_CUNG.equals(l)
                    && !nro.entity.badges.BadgesTaskTemplate.HA_NGUOI_CHOI.equals(l)
                    && !nro.entity.badges.BadgesTaskTemplate.TIEU_VANG.equals(l)
                    && !nro.entity.badges.BadgesTaskTemplate.TIEU_NGOC.equals(l);
            fThamSo.setEnabled(dungThamSo);
            btnThamSo.setEnabled(dungThamSo);
            if (nro.entity.badges.BadgesTaskTemplate.GO_CUNG.equals(l)) {
                lblThamSo.setText("  Tiến độ do mã nguồn tự cộng — không tự đếm được.");
            } else if (nro.entity.badges.BadgesTaskTemplate.TIEU_VANG.equals(l)
                    || nro.entity.badges.BadgesTaskTemplate.TIEU_NGOC.equals(l)) {
                lblThamSo.setText("  Mỗi đơn vị tiền tính một điểm — số lần = số tiền.");
            } else if (!dungThamSo) {
                lblThamSo.setText("  Loại này không cần tham số.");
            } else {
                lblThamSo.setText("  -1 = bất kỳ");
            }
        };
        moTa.run();
        cbLoai.addActionListener(e -> moTa.run());

        // Danh hiệu thưởng — chọn từ danh sách, khỏi nhớ id hiệu ứng.
        java.util.List<nro.repository.dao.DanhHieuDAO.DanhHieu> dsDh =
                nro.repository.dao.DanhHieuDAO.danhSach();
        String[] nhanDh = new String[dsDh.size() + 1];
        nhanDh[0] = "(không thưởng danh hiệu)";
        for (int i = 0; i < dsDh.size(); i++) {
            nhanDh[i + 1] = dsDh.get(i).idEffect + " — " + dsDh.get(i).ten;
        }
        JComboBox<String> cbDh = new JComboBox<>(nhanDh);
        for (int i = 0; i < dsDh.size(); i++) {
            if (dsDh.get(i).idEffect == n.danhHieuThuong) {
                cbDh.setSelectedIndex(i + 1);
                break;
            }
        }

        JPanel p = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new java.awt.Insets(4, 4, 4, 4);
        g.anchor = java.awt.GridBagConstraints.WEST;
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Tên nhiệm vụ:"), g);
        g.gridx = 1;
        p.add(fTen, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Loại:"), g);
        g.gridx = 1;
        p.add(cbLoai, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Tham số:"), g);
        g.gridx = 1;
        JPanel hangTs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangTs.setOpaque(false);
        hangTs.add(fThamSo);
        hangTs.add(btnThamSo);
        hangTs.add(lblThamSo);
        p.add(hangTs, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Số lần cần đạt:"), g);
        g.gridx = 1;
        p.add(fSoLan, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Danh hiệu thưởng:"), g);
        g.gridx = 1;
        p.add(cbDh, g);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm nhiệm vụ danh hiệu" : "Sửa nhiệm vụ danh hiệu",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            n.soLan = Integer.parseInt(fSoLan.getText().trim().replace(".", ""));
            n.thamSo = Integer.parseInt(fThamSo.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Số lần và tham số phải là số.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        n.ten = fTen.getText().trim();
        n.loai = cacLoai[cbLoai.getSelectedIndex()];
        int iDh = cbDh.getSelectedIndex();
        n.danhHieuThuong = iDh <= 0 ? -1 : dsDh.get(iDh - 1).idEffect;

        String loi = them ? nro.repository.dao.NhiemVuDanhHieuDAO.them(n)
                : nro.repository.dao.NhiemVuDanhHieuDAO.sua(n);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        note(OK_GREEN, them
                ? "Đã thêm nhiệm vụ — người chơi thấy ở lần đăng nhập sau."
                : "Đã lưu nhiệm vụ.");
    }

    private void xoaNhiemVu() {
        nro.repository.dao.NhiemVuDanhHieuDAO.NhiemVu n = nvDangChon();
        if (n == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xoá nhiệm vụ \"" + n.ten + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.NhiemVuDanhHieuDAO.xoa(n.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        note(OK_GREEN, "Đã xoá nhiệm vụ.");
    }

    // =====================================================================
    //  Tab: Điểm đến capsule
    // =====================================================================

    /**
     * Quản lý danh sách nơi capsule bay tới.
     *
     * <p>Trước đây danh sách này gõ cứng trong {@code MapService.getMapCapsule()}
     * — thêm hay bớt một chỗ là phải sửa mã, biên dịch lại rồi khởi động lại
     * máy chủ.</p>
     *
     * <p>Bảng rỗng thì DAO tự gieo lại đúng danh sách cũ, nên mở tab lần đầu
     * đã thấy sẵn 15 địa điểm gốc để sửa dần, không phải nhập tay từ đầu.</p>
     */
    /**
     * Tab "Tỉ lệ nâng sao" — tỉ lệ, vàng và ngọc của từng bậc pha lê hoá.
     */
    /**
     * Tab <b>Nội tại</b> — bảng {@code intrinsic}, thứ máy chủ thật sự đọc.
     *
     * <p>Trước bản này bảng ấy chỉ được đọc <b>một lần lúc khởi động</b> và
     * không có đường nào xem hay sửa: muốn đổi một con số thì phải gõ SQL rồi
     * dựng lại máy chủ. Tab này đọc và ghi thẳng vào chính bảng đó, và nạp lại
     * vào bộ nhớ ngay sau khi lưu.</p>
     */
    // ---------------------------------------------------------------- vòng quay
    private static final int COT_VQ_ID = 0;
    private static final int COT_VQ_NHOM = 1;
    private static final int COT_VQ_ANH = 2;
    private static final int COT_VQ_TEN = 3;
    private static final int COT_VQ_SL = 4;
    private static final int COT_VQ_CS = 5;
    private static final int COT_VQ_TS = 6;
    private static final int COT_VQ_CH = 7;
    private static final int COT_VQ_BAT = 8;
    private static final int COT_VQ_GC = 9;

    /**
     * Bảng kho quà — <b>chỉ để xem</b>, sửa bằng hộp thoại.
     *
     * <p>Sửa thẳng trong ô thì mọi thứ phải là chữ thô: id vật phẩm, id chỉ số,
     * mã riêng gõ tay. Nhìn một bảng toàn số không biết món nào là món nào. Nên
     * bảng lo phần <i>nhìn</i> — ảnh, tên, chỉ số viết ra chữ — còn phần
     * <i>sửa</i> giao cho hộp thoại có ô chọn đàng hoàng.</p>
     */
    private final DefaultTableModel vqModel = new DefaultTableModel(
            new Object[]{"Id", "Vòng quay", "Ảnh", "Vật phẩm", "Số lượng",
                "Chỉ số kèm theo", "Trọng số", "Cơ hội", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == COT_VQ_ANH ? javax.swing.ImageIcon.class : Object.class;
        }
    };
    private final JTable vqTable = new JTable(vqModel);

    /** Bản đang hiện, tra ngược từ dòng bảng về dòng dữ liệu. */
    private java.util.List<nro.repository.dao.VongQuayDAO.Qua> dsVongQuay
            = new java.util.ArrayList<>();

    /**
     * Tab <b>Vòng quay Thượng Đế</b> — kho quà, sửa được từ panel.
     *
     * <p>Danh sách quà vốn viết cứng trong mã. Bảng {@code vong_quay_qua} được
     * gieo đúng danh sách ấy ở lần chạy đầu, nên đổi sang cách này không làm
     * lệch tỉ lệ; từ đó trở đi máy chủ <b>chỉ đọc bảng</b>.</p>
     */
    private JComponent buildVongQuayTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        vqTable.setRowHeight(30);
        vqTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int[] w = {40, 80, 40, 300, 90, 300, 65, 65, 40, 180};
        for (int i = 0; i < vqTable.getColumnCount() && i < w.length; i++) {
            vqTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        // Nhay dup mot dong = sua dong do. Day la thao tac ai cung thu dau tien
        // voi mot bang, nen no phai lam dung viec nguoi ta cho doi.
        vqTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && vqTable.getSelectedRow() >= 0) {
                    suaVongQuayDialog(false);
                }
            }
        });

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        JCheckBox oDung = new JCheckBox("Máy chủ dùng bảng này",
                ConfigDAO.on(nro.repository.dao.VongQuayDAO.KHOA_DUNG_PANEL));
        oDung.setOpaque(false);
        oDung.addActionListener(e -> {
            ConfigDAO.set(nro.repository.dao.VongQuayDAO.KHOA_DUNG_PANEL,
                    oDung.isSelected() ? "1" : "0");
            ConfigDAO.reload();
            capNhatNhanVongQuay();
            note(oDung.isSelected() ? WARN_RED : OK_GREEN, oDung.isSelected()
                    ? "ĐÃ BẬT — vòng quay nay lấy quà từ bảng này. Soát lại từng "
                    + "dòng trước khi để người chơi quay."
                    : "Đã tắt — vòng quay quay về danh sách trong mã như cũ.");
        });
        nut.add(oDung);
        nut.add(button("Sửa dòng đang chọn", ACCENT, e -> suaVongQuayDialog(false)));
        nut.add(button("Thêm dòng quà", OK_GREEN, e -> suaVongQuayDialog(true)));
        nut.add(button("Bật các dòng đã chọn", new Color(90, 140, 90),
                e -> batTatVongQuay(true)));
        nut.add(button("Tắt các dòng đã chọn", new Color(150, 120, 60),
                e -> batTatVongQuay(false)));
        nut.add(button("Xoá các dòng đã chọn", WARN_RED, e -> xoaDongVongQuay()));
        nut.add(button("Gieo lại từ mã nguồn", new Color(120, 90, 160),
                e -> gieoLaiVongQuay()));
        nut.add(button("Tải lại", GREY, e -> napBangVongQuay()));

        vqNhan = nhan("");
        capNhatNhanVongQuay();
        JPanel dau = new JPanel(new BorderLayout(0, 4));
        dau.setOpaque(false);
        dau.add(vqNhan, BorderLayout.NORTH);
        root.add(dau, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(vqTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangVongQuay();
        return root;
    }

    /** Nhãn giải thích của tab, đổi màu theo việc máy chủ có dùng bảng hay không. */
    private JLabel vqNhan;

    private void capNhatNhanVongQuay() {
        if (vqNhan == null) {
            return;
        }
        boolean dung = ConfigDAO.on(nro.repository.dao.VongQuayDAO.KHOA_DUNG_PANEL);
        vqNhan.setText("<html><body style='width:900px'>"
                + (dung
                        ? "<b style='color:#c0392b'>ĐANG BẬT</b> — vòng quay lấy quà "
                        + "từ bảng này."
                        : "<b style='color:#c0392b'>ĐANG TẮT</b> — vòng quay vẫn chạy "
                        + "theo danh sách viết trong mã, bảng này chưa có tác dụng "
                        + "gì. Tắt sẵn vì bản gieo đầu tiên <b>ra sai món</b>: id vật "
                        + "phẩm trong mã cũ được tra theo <i>vị trí</i> trong bảng "
                        + "mẫu, mà bảng mẫu đã đổi từ lúc đoạn mã ấy được viết — nên "
                        + "chép nguyên id sang đây cho ra một danh sách quà khác hẳn "
                        + "thứ người chơi vẫn nhận. Soát lại từng dòng rồi hãy bật.")
                + "<br><br>"
                + "<b>Nháy đúp một dòng để sửa.</b> Chọn nhiều dòng (giữ Ctrl hoặc "
                + "Shift) rồi bấm \"Sửa dòng đang chọn\" thì sửa được cả loạt — trong "
                + "hộp thoại chỉ những ô bạn <i>tích chọn</i> mới được áp, phần còn "
                + "lại của mỗi dòng giữ nguyên."
                + "<br><br>"
                + "Một dòng có thể chứa <b>nhiều vật phẩm</b>: quay trúng dòng đó thì "
                + "bốc ngẫu nhiên một món trong danh sách, cơ hội đều nhau. "
                + "<b>Trọng số</b> là trọng số, không phải phần trăm — cột \"Cơ hội\" "
                + "là phần trăm tính ra từ chính các trọng số đang bật của cùng vòng "
                + "quay. Mỗi dòng chỉ số có <b>khoảng trị số từ–tới</b>, và mỗi dòng "
                + "quà có <b>hạn dùng từ–tới</b> cùng <b>tỉ lệ ra bản vĩnh viễn</b>."
                + "<br><br>"
                + "Quay hụt thì rơi về vàng như cũ, nên tắt hết quà cũng không làm ai "
                + "mất lượt mà chẳng nhận gì.</body></html>");
    }

    /**
     * Xoá sạch bảng rồi gieo lại từ danh sách trong mã.
     *
     * <p>Dùng khi đã sửa lung tung và muốn về mốc ban đầu để soát lại từ đầu.</p>
     */
    private void gieoLaiVongQuay() {
        if (JOptionPane.showConfirmDialog(this,
                "Xoá sạch bảng rồi gieo lại từ danh sách trong mã?\n\n"
                + "Mọi dòng đang có — kể cả dòng bạn tự thêm — sẽ mất.",
                "Gieo lại kho quà", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        for (nro.repository.dao.VongQuayDAO.Qua q
                : nro.repository.dao.VongQuayDAO.danhSach()) {
            nro.repository.dao.VongQuayDAO.xoa(q.id);
        }
        napBangVongQuay();
        note(OK_GREEN, "Đã gieo lại kho quà từ danh sách trong mã.");
    }

    /** Tên vật phẩm của một dòng: một món thì tên thẳng, nhiều món thì gộp. */
    private static String moTaVatPham(nro.repository.dao.VongQuayDAO.Qua q) {
        java.util.List<Integer> ids
                = nro.repository.dao.VongQuayDAO.docDanhSachSo(q.vatPham);
        if (ids.isEmpty()) {
            return "(chưa chọn vật phẩm)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(" / ");
            }
            sb.append(tenVatPham(ids.get(i)));
        }
        if (ids.size() > 1) {
            return "1 trong " + ids.size() + ": " + sb;
        }
        return sb.toString();
    }

    /** Ảnh của dòng: lấy theo món đầu tiên. */
    private static javax.swing.ImageIcon anhVatPham(
            nro.repository.dao.VongQuayDAO.Qua q) {
        java.util.List<Integer> ids
                = nro.repository.dao.VongQuayDAO.docDanhSachSo(q.vatPham);
        if (ids.isEmpty()) {
            return null;
        }
        try {
            nro.entity.template.ItemTemplate t
                    = nro.service.item.ItemService.gI().getTemplate(ids.get(0));
            return t == null ? null : PlayerManagerPanel.iconOf(t.iconID);
        } catch (Exception boQua) {
            return null;
        }
    }

    /** Các dòng chỉ số viết ra chữ, thay vì {@code "87:0,30:0"}. */
    private static String moTaChiSoQua(nro.repository.dao.VongQuayDAO.Qua q) {
        java.util.List<int[]> ds = nro.repository.dao.VongQuayDAO.docChiSo(q.chiSo);
        StringBuilder sb = new StringBuilder();
        for (int[] cs : ds) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            String ten = OptionPicker.tenChiSo(cs[0]);
            // Khoang tri so thi in ca hai dau: "+20 den 30% suc danh".
            String tri = (cs[2] > cs[1]) ? (cs[1] + " đến " + cs[2])
                    : String.valueOf(cs[1]);
            sb.append(cs[1] > 0 || cs[2] > 0 ? ten.replace("#", tri) : ten);
        }
        if (q.hsdMax > 0) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("HSD ").append(Math.max(1, q.hsdMin));
            if (q.hsdMax > q.hsdMin) {
                sb.append(" đến ").append(q.hsdMax);
            }
            sb.append(" ngày");
            if (q.tiLeVinhVien > 0) {
                sb.append(" (").append(q.tiLeVinhVien).append("% ra vĩnh viễn)");
            }
        }
        return sb.length() == 0 ? "(không có)" : sb.toString();
    }

    private void napBangVongQuay() {
        vqModel.setRowCount(0);
        dsVongQuay = nro.repository.dao.VongQuayDAO.danhSach();
        int tongThuong = 0;
        int tongVip = 0;
        for (nro.repository.dao.VongQuayDAO.Qua q : dsVongQuay) {
            if (!q.bat || q.trongSo <= 0) {
                continue;
            }
            if (q.nhom == nro.repository.dao.VongQuayDAO.NHOM_VIP) {
                tongVip += q.trongSo;
            } else {
                tongThuong += q.trongSo;
            }
        }
        for (nro.repository.dao.VongQuayDAO.Qua q : dsVongQuay) {
            int tong = q.nhom == nro.repository.dao.VongQuayDAO.NHOM_VIP
                    ? tongVip : tongThuong;
            String coHoi = (!q.bat || q.trongSo <= 0 || tong <= 0) ? "—"
                    : String.format("%.2f%%", q.trongSo * 100.0 / tong);
            int min = Math.max(1, q.soLuongMin);
            int max = Math.max(min, q.soLuongMax);
            vqModel.addRow(new Object[]{q.id,
                q.nhom == nro.repository.dao.VongQuayDAO.NHOM_VIP ? "VIP" : "Thường",
                anhVatPham(q), moTaVatPham(q),
                min == max ? String.valueOf(min) : (min + " – " + max),
                moTaChiSoQua(q), q.trongSo, coHoi,
                q.bat ? "✔" : "", q.ghiChu == null ? "" : q.ghiChu});
        }
    }

    /** Các dòng dữ liệu ứng với những dòng bảng đang chọn. */
    private java.util.List<nro.repository.dao.VongQuayDAO.Qua> vqDangChon() {
        java.util.List<nro.repository.dao.VongQuayDAO.Qua> ra
                = new java.util.ArrayList<>();
        for (int r : vqTable.getSelectedRows()) {
            int i = vqTable.convertRowIndexToModel(r);
            if (i >= 0 && i < dsVongQuay.size()) {
                ra.add(dsVongQuay.get(i));
            }
        }
        return ra;
    }

    private void batTatVongQuay(boolean bat) {
        java.util.List<nro.repository.dao.VongQuayDAO.Qua> chon = vqDangChon();
        if (chon.isEmpty()) {
            note(WARN_RED, "Chưa chọn dòng nào.");
            return;
        }
        for (nro.repository.dao.VongQuayDAO.Qua q : chon) {
            q.bat = bat;
            nro.repository.dao.VongQuayDAO.luu(q);
        }
        napBangVongQuay();
        note(OK_GREEN, (bat ? "Đã bật " : "Đã tắt ") + chon.size() + " dòng quà.");
    }

    private void xoaDongVongQuay() {
        java.util.List<nro.repository.dao.VongQuayDAO.Qua> chon = vqDangChon();
        if (chon.isEmpty()) {
            note(WARN_RED, "Chưa chọn dòng nào.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá " + chon.size() + " dòng quà đã chọn?",
                "Xoá quà vòng quay", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        for (nro.repository.dao.VongQuayDAO.Qua q : chon) {
            nro.repository.dao.VongQuayDAO.xoa(q.id);
        }
        napBangVongQuay();
        note(OK_GREEN, "Đã xoá " + chon.size() + " dòng quà.");
    }

    /**
     * Hộp sửa một hoặc <b>nhiều</b> dòng quà.
     *
     * <h2>Sửa nhiều dòng thì tích ô nào áp ô đó</h2>
     *
     * <p>Sửa hàng loạt mà áp hết mọi ô là hỏng: hai dòng khác nhau ở vật phẩm
     * nhưng ta chỉ muốn đổi trọng số của cả hai, áp hết thì chúng thành hai dòng
     * giống hệt nhau. Nên mỗi ô có một dấu tích riêng — <b>chỉ ô được tích mới
     * ghi đè</b>, phần còn lại của mỗi dòng giữ nguyên.</p>
     *
     * <p>Sửa một dòng thì mọi ô tích sẵn, vì lúc đó ghi đè hết là đúng ý.</p>
     */
    private void suaVongQuayDialog(boolean them) {
        java.util.List<nro.repository.dao.VongQuayDAO.Qua> chon;
        if (them) {
            nro.repository.dao.VongQuayDAO.Qua moi
                    = new nro.repository.dao.VongQuayDAO.Qua();
            moi.vatPham = "";
            chon = java.util.Collections.singletonList(moi);
        } else {
            chon = vqDangChon();
            if (chon.isEmpty()) {
                note(WARN_RED, "Chưa chọn dòng nào — nháy đúp một dòng để sửa.");
                return;
            }
        }
        final boolean nhieu = chon.size() > 1;
        nro.repository.dao.VongQuayDAO.Qua mau = chon.get(0);

        JComboBox<String> fNhom = new JComboBox<>(new String[]{"Thường", "VIP"});
        fNhom.setSelectedIndex(
                mau.nhom == nro.repository.dao.VongQuayDAO.NHOM_VIP ? 1 : 0);

        // ---- danh sách vật phẩm của dòng ----
        DefaultTableModel mVp = new DefaultTableModel(
                new Object[]{"Ảnh", "Id", "Tên vật phẩm"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? javax.swing.ImageIcon.class : Object.class;
            }
        };
        JTable bVp = new JTable(mVp);
        bVp.setRowHeight(28);
        bVp.getColumnModel().getColumn(0).setPreferredWidth(40);
        bVp.getColumnModel().getColumn(1).setPreferredWidth(60);
        bVp.getColumnModel().getColumn(2).setPreferredWidth(300);
        for (int id : nro.repository.dao.VongQuayDAO.docDanhSachSo(mau.vatPham)) {
            themDongVatPhamVq(mVp, id);
        }

        JPanel nutVp = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        nutVp.setOpaque(false);
        nutVp.add(button("Chọn vật phẩm…", ACCENT, ev -> {
            int id = OptionPicker.chonVatPham(this, -1);
            if (id > 0) {
                themDongVatPhamVq(mVp, id);
            }
        }));
        nutVp.add(button("Bỏ dòng đang chọn", WARN_RED, ev -> {
            int r = bVp.getSelectedRow();
            if (r >= 0) {
                mVp.removeRow(bVp.convertRowIndexToModel(r));
            }
        }));

        // ---- chỉ số kèm theo ----
        DefaultTableModel mCs = new DefaultTableModel(
                new Object[]{"Id", "Chỉ số", "Trị số từ", "tới"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c >= 2;
            }
        };
        JTable bCs = new JTable(mCs);
        bCs.setRowHeight(24);
        bCs.getColumnModel().getColumn(0).setPreferredWidth(45);
        bCs.getColumnModel().getColumn(1).setPreferredWidth(280);
        bCs.getColumnModel().getColumn(2).setPreferredWidth(70);
        bCs.getColumnModel().getColumn(3).setPreferredWidth(70);
        for (int[] cs : nro.repository.dao.VongQuayDAO.docChiSo(mau.chiSo)) {
            mCs.addRow(new Object[]{cs[0], OptionPicker.tenChiSo(cs[0]),
                String.valueOf(cs[1]), String.valueOf(cs[2])});
        }

        JPanel nutCs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        nutCs.setOpaque(false);
        nutCs.add(button("Thêm chỉ số…", ACCENT, ev -> {
            int id = OptionPicker.chonChiSo(this, -1);
            if (id >= 0) {
                mCs.addRow(new Object[]{id, OptionPicker.tenChiSo(id), "0", "0"});
            }
        }));
        nutCs.add(button("Bỏ dòng đang chọn", WARN_RED, ev -> {
            int r = bCs.getSelectedRow();
            if (r >= 0) {
                if (bCs.isEditing()) {
                    bCs.getCellEditor().stopCellEditing();
                }
                mCs.removeRow(bCs.convertRowIndexToModel(r));
            }
        }));

        JTextField fMin = new JTextField(String.valueOf(Math.max(1, mau.soLuongMin)), 6);
        JTextField fMax = new JTextField(
                String.valueOf(Math.max(Math.max(1, mau.soLuongMin), mau.soLuongMax)), 6);
        JTextField fTs = new JTextField(String.valueOf(mau.trongSo), 6);
        JTextField fHsdMin = new JTextField(String.valueOf(mau.hsdMin), 6);
        JTextField fHsdMax = new JTextField(String.valueOf(mau.hsdMax), 6);
        JTextField fVv = new JTextField(String.valueOf(mau.tiLeVinhVien), 6);
        JCheckBox fBat = new JCheckBox("Bật", mau.bat);
        fBat.setOpaque(false);
        JTextField fGc = new JTextField(mau.ghiChu == null ? "" : mau.ghiChu, 30);

        // Dau tich "ap o nay" — chi hien khi sua nhieu dong.
        JCheckBox apNhom = new JCheckBox("áp", !nhieu);
        JCheckBox apVp = new JCheckBox("áp", !nhieu);
        JCheckBox apCs = new JCheckBox("áp", !nhieu);
        JCheckBox apSl = new JCheckBox("áp", !nhieu);
        JCheckBox apTs = new JCheckBox("áp", !nhieu);
        JCheckBox apHsd = new JCheckBox("áp", !nhieu);
        JCheckBox apBat = new JCheckBox("áp", !nhieu);
        JCheckBox apGc = new JCheckBox("áp", !nhieu);
        for (JCheckBox cb : new JCheckBox[]{apNhom, apVp, apCs, apSl, apTs,
            apHsd, apBat, apGc}) {
            cb.setOpaque(false);
            cb.setVisible(nhieu);
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        hangVq(form, c, y++, apNhom, "Vòng quay:", fNhom);

        JPanel hopVp = new JPanel(new BorderLayout(0, 2));
        hopVp.setOpaque(false);
        JScrollPane spVp = ServerGuiUtils.cuon(bVp);
        spVp.setPreferredSize(new Dimension(460, 110));
        hopVp.add(spVp, BorderLayout.CENTER);
        hopVp.add(nutVp, BorderLayout.SOUTH);
        hangVq(form, c, y++, apVp, "Vật phẩm (nhiều món = bốc 1):", hopVp);

        JPanel hopSl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hopSl.setOpaque(false);
        hopSl.add(new JLabel("từ"));
        hopSl.add(fMin);
        hopSl.add(new JLabel("tới"));
        hopSl.add(fMax);
        hangVq(form, c, y++, apSl, "Số lượng:", hopSl);

        JPanel hopCs = new JPanel(new BorderLayout(0, 2));
        hopCs.setOpaque(false);
        JScrollPane spCs = ServerGuiUtils.cuon(bCs);
        spCs.setPreferredSize(new Dimension(460, 110));
        hopCs.add(spCs, BorderLayout.CENTER);
        hopCs.add(nutCs, BorderLayout.SOUTH);
        hangVq(form, c, y++, apCs, "Chỉ số kèm theo:", hopCs);

        hangVq(form, c, y++, apTs, "Trọng số:", fTs);
        JPanel hopHsd = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hopHsd.setOpaque(false);
        hopHsd.add(new JLabel("từ"));
        hopHsd.add(fHsdMin);
        hopHsd.add(new JLabel("tới"));
        hopHsd.add(fHsdMax);
        hopHsd.add(new JLabel("ngày · tỉ lệ ra vĩnh viễn"));
        hopHsd.add(fVv);
        hopHsd.add(new JLabel("%"));
        hangVq(form, c, y++, apHsd, "Hạn dùng:", hopHsd);
        hangVq(form, c, y++, apBat, "Trạng thái:", fBat);
        hangVq(form, c, y++, apGc, "Ghi chú:", fGc);

        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 3;
        form.add(nhan(nhieu
                ? "Đang sửa <b>" + chon.size() + " dòng</b> cùng lúc. Chỉ những ô "
                + "bạn tích <b>áp</b> mới ghi đè; phần còn lại của mỗi dòng giữ "
                + "nguyên."
                : "<b>Trọng số</b> là trọng số, không phải phần trăm. "
                + "<b>Số lượng</b> để hai ô bằng nhau là số cố định."), c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm dòng quà" : (nhieu ? "Sửa " + chon.size() + " dòng quà"
                        : "Sửa dòng quà"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (bCs.isEditing()) {
            bCs.getCellEditor().stopCellEditing();
        }

        StringBuilder vp = new StringBuilder();
        for (int r = 0; r < mVp.getRowCount(); r++) {
            if (vp.length() > 0) {
                vp.append(",");
            }
            vp.append(String.valueOf(mVp.getValueAt(r, 1)).trim());
        }
        StringBuilder cs = new StringBuilder();
        for (int r = 0; r < mCs.getRowCount(); r++) {
            if (cs.length() > 0) {
                cs.append(",");
            }
            cs.append(String.valueOf(mCs.getValueAt(r, 0)).trim()).append(":")
                    .append(String.valueOf(mCs.getValueAt(r, 2)).trim()).append(":")
                    .append(String.valueOf(mCs.getValueAt(r, 3)).trim());
        }
        int min;
        int max;
        int ts;
        int hsdMin;
        int hsdMax;
        int vv;
        try {
            min = Math.max(1, Integer.parseInt(fMin.getText().trim()));
            max = Math.max(min, Integer.parseInt(fMax.getText().trim()));
            ts = Integer.parseInt(fTs.getText().trim());
            hsdMin = Integer.parseInt(fHsdMin.getText().trim());
            hsdMax = Integer.parseInt(fHsdMax.getText().trim());
            vv = Integer.parseInt(fVv.getText().trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Số lượng, trọng số, hạn dùng và tỉ lệ phải là số nguyên.");
            return;
        }

        int hong = 0;
        String hongDau = null;
        for (nro.repository.dao.VongQuayDAO.Qua q : chon) {
            if (apNhom.isSelected()) {
                q.nhom = fNhom.getSelectedIndex() == 1
                        ? nro.repository.dao.VongQuayDAO.NHOM_VIP
                        : nro.repository.dao.VongQuayDAO.NHOM_THUONG;
            }
            if (apVp.isSelected()) {
                q.vatPham = vp.toString();
            }
            if (apCs.isSelected()) {
                q.chiSo = cs.toString();
            }
            if (apSl.isSelected()) {
                q.soLuongMin = min;
                q.soLuongMax = max;
            }
            if (apTs.isSelected()) {
                q.trongSo = ts;
            }
            if (apHsd.isSelected()) {
                q.hsdMin = hsdMin;
                q.hsdMax = hsdMax;
                q.tiLeVinhVien = vv;
            }
            if (apBat.isSelected()) {
                q.bat = fBat.isSelected();
            }
            if (apGc.isSelected()) {
                q.ghiChu = fGc.getText();
            }
            String loi = nro.repository.dao.VongQuayDAO.luu(q);
            if (loi != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = loi;
                }
            }
        }
        napBangVongQuay();
        note(hong == 0 ? OK_GREEN : WARN_RED, hong == 0
                ? (them ? "Đã thêm một dòng quà."
                        : "Đã lưu " + chon.size() + " dòng quà — có hiệu lực ngay "
                        + "lượt quay sau.")
                : hongDau + " (" + hong + " dòng không lưu được)");
    }

    /** Một hàng của hộp sửa: dấu tích "áp" · nhãn · ô nhập. */
    private static void hangVq(JPanel form, GridBagConstraints c, int y,
            JCheckBox ap, String nhan, java.awt.Component o) {
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0;
        form.add(ap, c);
        c.gridx = 1;
        form.add(new JLabel(nhan), c);
        c.gridx = 2;
        c.weightx = 1;
        form.add(o, c);
        c.weightx = 0;
    }

    private static void themDongVatPhamVq(DefaultTableModel m, int id) {
        for (int r = 0; r < m.getRowCount(); r++) {
            if (String.valueOf(m.getValueAt(r, 1)).trim().equals(String.valueOf(id))) {
                return;     // da co roi, dung them lan hai
            }
        }
        javax.swing.ImageIcon anh = null;
        try {
            nro.entity.template.ItemTemplate t
                    = nro.service.item.ItemService.gI().getTemplate(id);
            if (t != null) {
                anh = PlayerManagerPanel.iconOf(t.iconID);
            }
        } catch (Exception boQua) {
            // Bang mau chua nap -> van them dong, chi thieu anh.
        }
        m.addRow(new Object[]{anh, id, tenVatPham(id)});
    }

    // ---------------------------------------------------------------- top máy đấm
    private final DefaultTableModel topModel = new DefaultTableModel(
            new Object[]{"#", "Id", "Tên nhân vật", "Đòn mạnh nhất",
                "Sát thương 30 giây", "Đang online"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 3 || c == 4;
        }
    };
    private final JTable topTable = new JTable(topModel);
    private JComboBox<String> topXepTheo;

    /**
     * Tab <b>Top máy đấm</b> — hai bảng xếp hạng của máy đo sức mạnh.
     *
     * <h2>Dữ liệu nằm trên từng nhân vật</h2>
     *
     * <p>Không có bảng xếp hạng riêng: kỷ lục nằm ở cột
     * {@code player.data_may_dam}, bảng xếp hạng chỉ là một câu sắp xếp trên cột
     * ấy. Nên sửa một dòng ở đây là sửa thẳng kỷ lục của nhân vật đó, và xoá
     * bảng là đặt lại kỷ lục của <b>mọi</b> nhân vật.</p>
     */
    private JComponent buildTopMayDamTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        topTable.setRowHeight(24);
        topTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {40, 70, 220, 150, 170, 90};
        for (int i = 0; i < topTable.getColumnCount() && i < w.length; i++) {
            topTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        topXepTheo = new JComboBox<>(new String[]{
            "Đòn mạnh nhất", "Sát thương 30 giây"});
        topXepTheo.addActionListener(e -> napBangTop());

        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tren.setOpaque(false);
        tren.add(new JLabel("Xếp theo:"));
        tren.add(topXepTheo);

        JCheckBox oResetNgay = new JCheckBox("Tự xoá bảng mỗi ngày",
                ConfigDAO.on(nro.repository.dao.TopMayDamDAO.KHOA_RESET_NGAY));
        oResetNgay.setOpaque(false);
        oResetNgay.addActionListener(e -> {
            ConfigDAO.set(nro.repository.dao.TopMayDamDAO.KHOA_RESET_NGAY,
                    oResetNgay.isSelected() ? "1" : "0");
            ConfigDAO.reload();
            note(OK_GREEN, oResetNgay.isSelected()
                    ? "Đã bật tự xoá bảng xếp hạng mỗi ngày."
                    : "Đã tắt tự xoá theo ngày.");
        });
        tren.add(oResetNgay);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Lưu các dòng đã sửa", OK_GREEN, e -> luuBangTop()));
        nut.add(button("Tải lại", GREY, e -> napBangTop()));
        nut.add(button("Xoá kỷ lục dòng đang chọn", new Color(120, 90, 160),
                e -> xoaMotDongTop()));
        nut.add(button("XOÁ TOÀN BỘ BẢNG", WARN_RED, e -> xoaToanBoTop()));

        JPanel dau = new JPanel(new BorderLayout(0, 4));
        dau.setOpaque(false);
        dau.add(nhan("Bảng xếp hạng máy đo sức mạnh ở NPC Ghi Danh. "
                + "<b>Đòn mạnh nhất</b> là sát thương của một cú đấm lớn nhất từng "
                + "gây ra; <b>Sát thương 30 giây</b> là tổng của lượt đấm 30 giây "
                + "cao nhất."
                + "<br><br>"
                + "Kỷ lục <b>nằm trên từng nhân vật</b> chứ không có bảng riêng, nên "
                + "sửa một dòng ở đây là sửa thẳng kỷ lục của người đó, và xoá bảng "
                + "là đặt lại kỷ lục của mọi nhân vật. Người đang online cũng được "
                + "sửa luôn trong bộ nhớ — không thì vài phút sau họ ghi đè lại con "
                + "số cũ xuống và trông như không lưu được."
                + "<br><br>"
                + "Bật \"tự xoá mỗi ngày\" thì bảng được làm sạch ở lần chạy đầu tiên "
                + "của mỗi ngày. Không dùng hẹn giờ nửa đêm: máy chủ tắt lúc 23h50 "
                + "bật lại lúc 0h10 thì cái hẹn không bao giờ nổ."), BorderLayout.NORTH);
        dau.add(tren, BorderLayout.SOUTH);

        root.add(dau, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(topTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangTop();
        return root;
    }

    private void napBangTop() {
        if (topTable.isEditing()) {
            topTable.getCellEditor().stopCellEditing();
        }
        topModel.setRowCount(0);
        boolean theoDonManh = topXepTheo == null
                || topXepTheo.getSelectedIndex() == 0;
        int i = 1;
        for (nro.repository.dao.TopMayDamDAO.Dong d
                : nro.repository.dao.TopMayDamDAO.danhSach(theoDonManh, 200)) {
            topModel.addRow(new Object[]{i++, d.id, d.ten,
                nro.core.util.Util.soCham(d.donManh),
                nro.core.util.Util.soCham(d.dame30s),
                d.online ? "có" : ""});
        }
    }

    private void luuBangTop() {
        if (topTable.isEditing()) {
            topTable.getCellEditor().stopCellEditing();
        }
        int hong = 0;
        String hongDau = null;
        for (int r = 0; r < topModel.getRowCount(); r++) {
            long id;
            long a;
            long b;
            try {
                id = Long.parseLong(String.valueOf(topModel.getValueAt(r, 1)).trim());
                a = docSo(String.valueOf(topModel.getValueAt(r, 3)));
                b = docSo(String.valueOf(topModel.getValueAt(r, 4)));
            } catch (NumberFormatException ex) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Dòng " + (r + 1) + " có ô không phải số.";
                }
                continue;
            }
            String loi = nro.repository.dao.TopMayDamDAO.sua(id, a, b);
            if (loi != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = loi;
                }
            }
        }
        napBangTop();
        note(hong == 0 ? OK_GREEN : WARN_RED, hong == 0
                ? "Đã lưu bảng xếp hạng."
                : hongDau + " (" + hong + " dòng không lưu được)");
    }

    /** Đọc một ô số có thể đang mang dấu chấm ngăn nghìn. */
    private static long docSo(String s) {
        String t = s == null ? "" : s.replace(".", "").replace(",", "").trim();
        if (t.isEmpty()) {
            return 0;
        }
        return Long.parseLong(t);
    }

    private void xoaMotDongTop() {
        int r = topTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn dòng nào.");
            return;
        }
        r = topTable.convertRowIndexToModel(r);
        long id = Long.parseLong(String.valueOf(topModel.getValueAt(r, 1)).trim());
        String ten = String.valueOf(topModel.getValueAt(r, 2));
        if (JOptionPane.showConfirmDialog(this,
                "Xoá kỷ lục máy đấm của \"" + ten + "\"?",
                "Xoá kỷ lục", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.TopMayDamDAO.sua(id, 0, 0);
        napBangTop();
        note(loi == null ? OK_GREEN : WARN_RED,
                loi == null ? "Đã xoá kỷ lục của " + ten + "." : loi);
    }

    private void xoaToanBoTop() {
        if (JOptionPane.showConfirmDialog(this,
                "XOÁ TOÀN BỘ bảng xếp hạng máy đấm?\n\n"
                + "Kỷ lục của MỌI nhân vật về 0, kể cả người đang online.\n"
                + "Không lấy lại được.",
                "Xoá toàn bộ bảng", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        int n = nro.repository.dao.TopMayDamDAO.resetTatCa();
        napBangTop();
        note(n >= 0 ? OK_GREEN : WARN_RED, n >= 0
                ? "Đã xoá bảng xếp hạng — " + n + " nhân vật về 0."
                : "Không xoá được — xem log máy chủ.");
    }

    // ---------------------------------------------------------------- nhiệm vụ
    private static final int COT_B_STT = 0;
    private static final int COT_B_TEN = 1;
    private static final int COT_B_SL = 2;
    private static final int COT_B_TB = 3;
    private static final int COT_B_NPC = 4;
    private static final int COT_B_MAP = 5;

    /** Danh sách nhiệm vụ đang hiện trên tab, để tra lại theo dòng. */
    private java.util.List<nro.repository.dao.NhiemVuDAO.NhiemVu> dsNhiemVu
            = new java.util.ArrayList<>();

    private final DefaultTableModel nvcModel = new DefaultTableModel(
            new Object[]{"Id", "Tên nhiệm vụ", "Số bước"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable nvcTable = new JTable(nvcModel);

    private final DefaultTableModel buocModel = new DefaultTableModel(
            new Object[]{"Thứ tự", "Tên bước", "Số lượng cần", "Thông báo",
                "NPC", "Bản đồ"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c != COT_B_STT;
        }
    };
    private final JTable buocTable = new JTable(buocModel);

    private final JTextField nvcTen = new JTextField();
    private final JTextArea nvcMoTa = new JTextArea(6, 40);

    /**
     * Tab <b>Nhiệm vụ chính tuyến</b>.
     *
     * <p>Xem {@code NhiemVuDAO} cho phần giải thích một nhiệm vụ gồm những gì.
     * Ở đây chỉ nhắc lại phần người dùng cần biết trước khi gõ.</p>
     */
    private JComponent buildNhiemVuChinhTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        nvcTable.setRowHeight(24);
        nvcTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] wl = {40, 260, 60};
        for (int i = 0; i < nvcTable.getColumnCount() && i < wl.length; i++) {
            nvcTable.getColumnModel().getColumn(i).setPreferredWidth(wl[i]);
        }
        nvcTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienNhiemVuDangChon();
            }
        });

        buocTable.setRowHeight(24);
        buocTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] wb = {55, 300, 90, 300, 60, 60};
        for (int i = 0; i < buocTable.getColumnCount() && i < wb.length; i++) {
            buocTable.getColumnModel().getColumn(i).setPreferredWidth(wb[i]);
        }

        nvcMoTa.setLineWrap(true);
        nvcMoTa.setWrapStyleWord(true);

        JPanel dau = new JPanel(new BorderLayout(6, 4));
        dau.setOpaque(false);
        JPanel dongTen = new JPanel(new BorderLayout(6, 0));
        dongTen.setOpaque(false);
        dongTen.add(new JLabel("Tên nhiệm vụ:"), BorderLayout.WEST);
        dongTen.add(nvcTen, BorderLayout.CENTER);
        dau.add(dongTen, BorderLayout.NORTH);
        dau.add(ServerGuiUtils.cuon(nvcMoTa), BorderLayout.CENTER);
        dau.setBorder(BorderFactory.createTitledBorder("Nhiệm vụ đang chọn"));

        JPanel duoi = new JPanel(new BorderLayout(0, 4));
        duoi.setOpaque(false);
        duoi.add(ServerGuiUtils.cuon(buocTable), BorderLayout.CENTER);
        duoi.setBorder(BorderFactory.createTitledBorder(
                "Các bước — làm lần lượt từ trên xuống"));

        JSplitPane phai = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dau, duoi);
        phai.setResizeWeight(0.28);
        phai.setBorder(null);

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                ServerGuiUtils.cuon(nvcTable), phai);
        chia.setResizeWeight(0.28);
        chia.setBorder(null);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Lưu nhiệm vụ đang chọn", OK_GREEN, e -> luuNhiemVuDangChon()));
        nut.add(button("Tải lại", GREY, e -> napBangNhiemVuChinh()));
        nut.add(button("Thêm nhiệm vụ", ACCENT, e -> themNhiemVu()));
        nut.add(button("Thêm bước vào cuối", new Color(120, 90, 160),
                e -> themBuocCuoi()));
        nut.add(button("▲ Lên", new Color(90, 120, 170), e -> doiChoBuoc(-1)));
        nut.add(button("▼ Xuống", new Color(90, 120, 170), e -> doiChoBuoc(1)));
        nut.add(button("Xoá bước đang chọn", WARN_RED, e -> xoaBuocDangChon()));

        root.add(nhan("Nhiệm vụ chính tuyến: mỗi nhiệm vụ là một dãy <b>bước</b> "
                + "làm lần lượt từ trên xuống, xong bước cuối là sang nhiệm vụ "
                + "có id kế tiếp — nên <b>id vừa là khoá vừa là thứ tự chơi</b>."
                + "<br><br>"
                + "<b>Số lượng cần</b> là con số hay phải chỉnh nhất: đánh bao "
                + "nhiêu con, nhặt bao nhiêu món thì xong bước. Đổi nó là có hiệu "
                + "lực thật. Để <code>-1</code> nghĩa là không đếm — bước xong bằng "
                + "một việc khác (nói chuyện với NPC, tới nơi, hạ một con boss)."
                + "<br><br>"
                + "<b>NPC</b> và <b>Bản đồ</b> là chỗ mũi tên chỉ dẫn trỏ tới; "
                + "<code>-1</code> là không trỏ. Số âm nhỏ hơn -1 là mã đặc biệt, "
                + "được đổi thành NPC/bản đồ thật <i>tuỳ hành tinh</i> của người "
                + "chơi — ví dụ <code>-2</code> là ông thầy ở làng. Trong lời văn, "
                + "<code>%1</code> đến <code>%14</code> cũng được thay theo hành "
                + "tinh, nên một dòng chữ đọc đúng ở cả ba nơi."
                + "<br><br>"
                + "<b>Việc gì làm bước đó tiến lên thì không sửa ở đây được</b> — "
                + "cái đó viết cứng trong mã theo đúng cặp (nhiệm vụ, số thứ tự "
                + "bước). Thêm hay xoá một bước ở <i>giữa</i> dãy sẽ làm lệch mọi "
                + "bước sau nó và nhiệm vụ kẹt lại không đi tiếp được. Chỉ thêm "
                + "bước khi có người viết mã cho nó."
                + "<br><br>"
                + "Lưu xong có hiệu lực với người <b>đăng nhập sau đó</b>; người "
                + "đang online giữ bản cũ tới khi vào lại. Tiến độ của người chơi "
                + "không bị đụng tới."), BorderLayout.NORTH);
        root.add(chia, BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangNhiemVuChinh();
        return root;
    }

    private void napBangNhiemVuChinh() {
        if (buocTable.isEditing()) {
            buocTable.getCellEditor().stopCellEditing();
        }
        int giu = nvcTable.getSelectedRow();
        dsNhiemVu = nro.repository.dao.NhiemVuDAO.danhSach();
        nvcModel.setRowCount(0);
        for (nro.repository.dao.NhiemVuDAO.NhiemVu nv : dsNhiemVu) {
            nvcModel.addRow(new Object[]{nv.id, nv.ten, nv.buoc.size()});
        }
        if (giu >= 0 && giu < nvcModel.getRowCount()) {
            nvcTable.setRowSelectionInterval(giu, giu);
        } else if (nvcModel.getRowCount() > 0) {
            nvcTable.setRowSelectionInterval(0, 0);
        } else {
            hienNhiemVuDangChon();
        }
    }

    private nro.repository.dao.NhiemVuDAO.NhiemVu nhiemVuDangChon() {
        int r = nvcTable.getSelectedRow();
        if (r < 0) {
            return null;
        }
        r = nvcTable.convertRowIndexToModel(r);
        return r < dsNhiemVu.size() ? dsNhiemVu.get(r) : null;
    }

    private void hienNhiemVuDangChon() {
        if (buocTable.isEditing()) {
            buocTable.getCellEditor().stopCellEditing();
        }
        buocModel.setRowCount(0);
        nro.repository.dao.NhiemVuDAO.NhiemVu nv = nhiemVuDangChon();
        if (nv == null) {
            nvcTen.setText("");
            nvcMoTa.setText("");
            return;
        }
        nvcTen.setText(nv.ten);
        nvcMoTa.setText(nv.moTa);
        nvcMoTa.setCaretPosition(0);
        for (nro.repository.dao.NhiemVuDAO.Buoc b : nv.buoc) {
            buocModel.addRow(new Object[]{b.idmain, b.ten,
                String.valueOf(b.soLuong), b.thongBao,
                String.valueOf(b.npcId), String.valueOf(b.mapId)});
        }
    }

    private void luuNhiemVuDangChon() {
        if (buocTable.isEditing()) {
            buocTable.getCellEditor().stopCellEditing();
        }
        nro.repository.dao.NhiemVuDAO.NhiemVu nv = nhiemVuDangChon();
        if (nv == null) {
            note(WARN_RED, "Chưa chọn nhiệm vụ nào.");
            return;
        }
        String loi = nro.repository.dao.NhiemVuDAO.luuNhiemVu(
                nv.id, nvcTen.getText(), nvcMoTa.getText());
        int hong = 0;
        String hongDau = loi;
        if (loi != null) {
            hong++;
        }
        for (int r = 0; r < buocModel.getRowCount(); r++) {
            nro.repository.dao.NhiemVuDAO.Buoc b
                    = new nro.repository.dao.NhiemVuDAO.Buoc();
            try {
                b.idmain = Integer.parseInt(oB(r, COT_B_STT));
                b.soLuong = Integer.parseInt(oB(r, COT_B_SL));
                b.npcId = Integer.parseInt(oB(r, COT_B_NPC));
                b.mapId = Integer.parseInt(oB(r, COT_B_MAP));
            } catch (NumberFormatException ex) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Bước dòng " + (r + 1) + " có ô không phải số.";
                }
                continue;
            }
            b.nhiemVuId = nv.id;
            b.ten = oB(r, COT_B_TEN);
            b.thongBao = oB(r, COT_B_TB);
            String kq = nro.repository.dao.NhiemVuDAO.luuBuoc(b);
            if (kq != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Bước " + b.idmain + ": " + kq;
                }
            }
        }
        String loiNap = nro.repository.dao.NhiemVuDAO.napLaiVaoBoNho();
        napBangNhiemVuChinh();
        if (hong == 0) {
            note(OK_GREEN, loiNap == null
                    ? "Đã lưu nhiệm vụ " + nv.id
                    + " — người đăng nhập sau sẽ nhận bản mới."
                    : loiNap);
        } else {
            note(WARN_RED, hongDau + " (" + hong + " mục không lưu được)");
        }
    }

    private String oB(int r, int c) {
        Object v = buocModel.getValueAt(r, c);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private void themNhiemVu() {
        String s = JOptionPane.showInputDialog(this,
                "Id của nhiệm vụ mới?\n\n"
                + "Id vừa là khoá vừa là THỨ TỰ CHƠI: xong nhiệm vụ này là\n"
                + "người chơi sang nhiệm vụ có id kế tiếp. Chèn một id vào\n"
                + "giữa dãy đang có sẽ xen nhiệm vụ mới vào mạch chính.\n"
                + "Thường thì nên thêm vào cuối.",
                "Thêm nhiệm vụ", JOptionPane.QUESTION_MESSAGE);
        if (s == null) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "\"" + s + "\" không phải là số.");
            return;
        }
        for (nro.repository.dao.NhiemVuDAO.NhiemVu nv : dsNhiemVu) {
            if (nv.id == id) {
                note(WARN_RED, "Đã có nhiệm vụ id " + id + " — chọn nó để sửa.");
                return;
            }
        }
        String loi = nro.repository.dao.NhiemVuDAO.luuNhiemVu(
                id, "Nhiệm vụ mới", "Chi tiết nhiệm vụ");
        nro.repository.dao.NhiemVuDAO.napLaiVaoBoNho();
        napBangNhiemVuChinh();
        note(loi == null ? OK_GREEN : WARN_RED,
                loi == null ? "Đã thêm nhiệm vụ id " + id
                        + " — nhớ thêm bước cho nó." : loi);
    }

    private void themBuocCuoi() {
        nro.repository.dao.NhiemVuDAO.NhiemVu nv = nhiemVuDangChon();
        if (nv == null) {
            note(WARN_RED, "Chưa chọn nhiệm vụ nào.");
            return;
        }
        int chon = JOptionPane.showConfirmDialog(this,
                "Thêm một bước vào cuối nhiệm vụ " + nv.id + "?\n\n"
                + "Bước mới KHÔNG có gì làm nó tiến lên: việc gì hoàn thành\n"
                + "bước nào được viết cứng trong mã theo số thứ tự bước.\n"
                + "Người chơi sẽ kẹt ở bước này cho tới khi có người viết mã\n"
                + "cho nó.",
                "Thêm bước", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.NhiemVuDAO.themBuoc(nv.id, "Bước mới");
        nro.repository.dao.NhiemVuDAO.napLaiVaoBoNho();
        napBangNhiemVuChinh();
        note(loi == null ? OK_GREEN : WARN_RED,
                loi == null ? "Đã thêm bước vào cuối nhiệm vụ " + nv.id + "." : loi);
    }


    /**
     * Đẩy bước đang chọn lên trên hoặc xuống dưới một nấc.
     *
     * <p>Giữ nguyên dòng đang chọn sau khi đổi, để bấm liên tục đẩy được một
     * bước đi xa mà không phải chọn lại mỗi lần.</p>
     *
     * @param huong {@code -1} lên, {@code 1} xuống
     */
    private void doiChoBuoc(int huong) {
        if (buocTable.isEditing()) {
            buocTable.getCellEditor().stopCellEditing();
        }
        int r = buocTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn bước nào.");
            return;
        }
        r = buocTable.convertRowIndexToModel(r);
        int k = r + huong;
        if (k < 0 || k >= buocModel.getRowCount()) {
            note(WARN_RED, huong < 0
                    ? "Bước này đã ở trên cùng." : "Bước này đã ở dưới cùng.");
            return;
        }
        int a;
        int b;
        try {
            a = Integer.parseInt(oB(r, COT_B_STT));
            b = Integer.parseInt(oB(k, COT_B_STT));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Dòng không có số thứ tự hợp lệ.");
            return;
        }
        String loi = nro.repository.dao.NhiemVuDAO.doiCho(a, b);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        nro.repository.dao.NhiemVuDAO.napLaiVaoBoNho();
        // Doc lai TU CSDL chu khong ve lai tu ban nho: dsNhiemVu la ban chup
        // luc nap bang, doi cho xong ma khong doc lai thi bang van hien thu tu
        // cu — nhin nhu nut khong an.
        napBangNhiemVuChinh();
        if (k < buocTable.getRowCount()) {
            buocTable.setRowSelectionInterval(k, k);
        }
        note(OK_GREEN, "Đã đổi chỗ hai bước. Nhớ là việc phải làm ở mỗi bước "
                + "viết cứng theo VỊ TRÍ, nên đổi chỗ là đổi luôn việc.");
    }

    private void xoaBuocDangChon() {
        int r = buocTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn bước nào.");
            return;
        }
        r = buocTable.convertRowIndexToModel(r);
        int stt;
        try {
            stt = Integer.parseInt(oB(r, COT_B_STT));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Dòng này không có số thứ tự hợp lệ.");
            return;
        }
        int chon = JOptionPane.showConfirmDialog(this,
                "Xoá bước \"" + oB(r, COT_B_TEN) + "\"?\n\n"
                + "Mọi bước SAU nó sẽ lùi lên một chỗ, mà việc hoàn thành từng\n"
                + "bước lại viết cứng theo số thứ tự — nhiệm vụ này sẽ lệch và\n"
                + "có thể kẹt không đi tiếp được.\n\n"
                + "Chỉ xoá nếu bạn biết rõ mình đang làm gì.",
                "Xoá bước", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.NhiemVuDAO.xoaBuoc(stt);
        nro.repository.dao.NhiemVuDAO.napLaiVaoBoNho();
        napBangNhiemVuChinh();
        note(loi == null ? OK_GREEN : WARN_RED,
                loi == null ? "Đã xoá bước " + stt + "." : loi);
    }

    private JComponent buildNoiTaiTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        noiTable.setRowHeight(26);
        noiTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Nhay dup mot dong = sua dong do, giong moi bang khac trong panel.
        noiTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && noiTable.getSelectedRow() >= 0) {
                    suaNoiTaiDialog();
                }
            }
        });
        int[] w = {50, 430, 60, 60, 60, 60, 60, 110, 150};
        for (int i = 0; i < noiTable.getColumnCount() && i < w.length; i++) {
            noiTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        JComboBox<String> oHanhTinh = new JComboBox<>(new String[]{
            "Trái Đất", "Namếc", "Xayda", "Dùng chung"});
        noiTable.getColumnModel().getColumn(COT_NOI_HT)
                .setCellEditor(new javax.swing.DefaultCellEditor(oHanhTinh));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Lưu bảng", OK_GREEN, e -> luuBangNoiTai()));
        nut.add(button("Tải lại", GREY, e -> napBangNoiTai()));
        nut.add(button("Sửa dòng đang chọn", new Color(90, 120, 170), e -> suaNoiTaiDialog()));
        nut.add(button("Thêm nội tại", ACCENT, e -> themDongNoiTai()));
        nut.add(button("Xoá dòng đang chọn", WARN_RED, e -> xoaDongNoiTai()));

        root.add(nhan("Mỗi dòng là một nội tại người chơi có thể mở ra. "
                + "<b>Tên là một mẫu câu</b>: <code>p0</code> và <code>p1</code> "
                + "được thay bằng khoảng trị số thứ nhất, <code>p2</code> và "
                + "<code>p3</code> bằng khoảng thứ hai. Ví dụ "
                + "<code>Tăng p0% đến p1% sát thương Kamejoko</code>."
                + "<br><br>"
                + "<b>Id là khoá gắn tác dụng, không phải số thứ tự.</b> Tác dụng "
                + "của từng nội tại viết cứng theo id trong mã (id 1 là Kamejoko "
                + "của Songoku, id 16 là Galick…). Vì thế ô Id không sửa được: đổi "
                + "nó là hoán tác dụng sang chiêu khác mà không có gì báo. Cột "
                + "\"Tác dụng\" cho biết id đó đã có tác dụng viết sẵn hay chưa — "
                + "dòng ghi <b>chưa có</b> vẫn hiện tên và vẫn bốc ra được, chỉ là "
                + "không cộng gì cho người chơi."
                + "<br><br>"
                + "\"Hành tinh\" lọc ai bốc được dòng này; <b>Dùng chung</b> là cả "
                + "ba hành tinh. Sửa xong bấm Lưu — bảng được nạp lại vào bộ nhớ "
                + "ngay, không cần dựng lại máy chủ."), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(noiTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangNoiTai();
        return root;
    }

    private void napBangNoiTai() {
        if (noiTable.isEditing()) {
            noiTable.getCellEditor().stopCellEditing();
        }
        noiModel.setRowCount(0);
        for (nro.entity.intrinsic.Intrinsic it
                : nro.repository.dao.NoiTaiDAO.danhSach()) {
            noiModel.addRow(new Object[]{
                String.valueOf(it.id), it.name,
                String.valueOf(it.paramFrom1), String.valueOf(it.paramTo1),
                String.valueOf(it.paramFrom2), String.valueOf(it.paramTo2),
                String.valueOf(it.icon),
                nro.repository.dao.NoiTaiDAO.tenHanhTinh(it.gender),
                it.id == 0 ? "— (dòng trống)"
                        : (nro.repository.dao.NoiTaiDAO.coTacDung(it.id)
                                ? "Có" : "CHƯA có trong mã")});
        }
    }

    /** Đọc chuỗi hành tinh ở ô về lại mã {@code gender}. */
    private static byte genderTuTen(Object o) {
        String s = o == null ? "" : String.valueOf(o).trim();
        if (s.equalsIgnoreCase("Trái Đất")) {
            return 0;
        }
        if (s.equalsIgnoreCase("Namếc")) {
            return 1;
        }
        if (s.equalsIgnoreCase("Xayda")) {
            return 2;
        }
        return 3;
    }

    private void luuBangNoiTai() {
        if (noiTable.isEditing()) {
            noiTable.getCellEditor().stopCellEditing();
        }
        int loi = 0;
        String loiDau = null;
        for (int r = 0; r < noiModel.getRowCount(); r++) {
            nro.entity.intrinsic.Intrinsic it = new nro.entity.intrinsic.Intrinsic();
            String nhan = "Dòng " + (r + 1);
            try {
                it.id = Integer.parseInt(oNoi(r, COT_NOI_ID));
                nhan = "Nội tại id " + it.id;
                it.name = oNoi(r, COT_NOI_TEN);
                it.paramFrom1 = Short.parseShort(oNoi(r, COT_NOI_TU1));
                it.paramTo1 = Short.parseShort(oNoi(r, COT_NOI_DEN1));
                it.paramFrom2 = Short.parseShort(oNoi(r, COT_NOI_TU2));
                it.paramTo2 = Short.parseShort(oNoi(r, COT_NOI_DEN2));
                it.icon = Short.parseShort(oNoi(r, COT_NOI_ICON));
            } catch (NumberFormatException ex) {
                loi++;
                if (loiDau == null) {
                    loiDau = nhan + " có ô không phải số.";
                }
                continue;
            }
            it.gender = genderTuTen(noiModel.getValueAt(r, COT_NOI_HT));
            String kq = nro.repository.dao.NoiTaiDAO.luu(it);
            if (kq != null) {
                loi++;
                if (loiDau == null) {
                    loiDau = nhan + ": " + kq;
                }
            }
        }
        napBangNoiTai();
        if (loi == 0) {
            note(OK_GREEN, "Đã lưu bảng nội tại — có hiệu lực ngay.");
        } else {
            note(WARN_RED, loiDau + " (" + loi + " dòng không lưu được)");
        }
    }

    /** Đọc một ô của bảng nội tại về chuỗi đã cắt khoảng trắng. */
    private String oNoi(int r, int c) {
        Object v = noiModel.getValueAt(r, c);
        return v == null ? "" : String.valueOf(v).trim();
    }


    /**
     * Hộp sửa một hoặc <b>nhiều</b> nội tại.
     *
     * <p>Bảng vẫn sửa thẳng trong ô được như cũ — hộp này thêm vào cho hai việc
     * ô nhập không làm nổi: sửa <b>cả loạt</b> dòng cùng lúc, và thấy rõ mình
     * đang đổi cái gì thay vì gõ vào một ô hẹp giữa bảy cột.</p>
     *
     * <p>Sửa nhiều dòng thì chỉ ô nào được <b>tích</b> mới ghi đè — không thì
     * mấy dòng khác nhau bị san bằng thành giống hệt nhau.</p>
     */
    private void suaNoiTaiDialog() {
        int[] rows = noiTable.getSelectedRows();
        if (rows.length == 0) {
            note(WARN_RED, "Chưa chọn dòng nào — nháy đúp một dòng để sửa.");
            return;
        }
        java.util.List<nro.entity.intrinsic.Intrinsic> chon
                = new java.util.ArrayList<>();
        for (int r : rows) {
            int i = noiTable.convertRowIndexToModel(r);
            nro.entity.intrinsic.Intrinsic it = new nro.entity.intrinsic.Intrinsic();
            try {
                it.id = Integer.parseInt(oNoi(i, COT_NOI_ID));
                it.name = oNoi(i, COT_NOI_TEN);
                it.paramFrom1 = Short.parseShort(oNoi(i, COT_NOI_TU1));
                it.paramTo1 = Short.parseShort(oNoi(i, COT_NOI_DEN1));
                it.paramFrom2 = Short.parseShort(oNoi(i, COT_NOI_TU2));
                it.paramTo2 = Short.parseShort(oNoi(i, COT_NOI_DEN2));
                it.icon = Short.parseShort(oNoi(i, COT_NOI_ICON));
                it.gender = genderTuTen(noiModel.getValueAt(i, COT_NOI_HT));
            } catch (NumberFormatException ex) {
                note(WARN_RED, "Dòng đang chọn có ô không phải số.");
                return;
            }
            chon.add(it);
        }
        final boolean nhieu = chon.size() > 1;
        nro.entity.intrinsic.Intrinsic mau = chon.get(0);

        JTextField fTen = new JTextField(mau.name == null ? "" : mau.name, 40);
        JTextField fTu1 = new JTextField(String.valueOf(mau.paramFrom1), 6);
        JTextField fDen1 = new JTextField(String.valueOf(mau.paramTo1), 6);
        JTextField fTu2 = new JTextField(String.valueOf(mau.paramFrom2), 6);
        JTextField fDen2 = new JTextField(String.valueOf(mau.paramTo2), 6);
        JTextField fIcon = new JTextField(String.valueOf(mau.icon), 6);
        JComboBox<String> fHt = new JComboBox<>(new String[]{
            "Trái Đất", "Namếc", "Xayda", "Dùng chung"});
        fHt.setSelectedItem(nro.repository.dao.NoiTaiDAO.tenHanhTinh(mau.gender));

        JCheckBox apTen = new JCheckBox("áp", !nhieu);
        JCheckBox apK1 = new JCheckBox("áp", !nhieu);
        JCheckBox apK2 = new JCheckBox("áp", !nhieu);
        JCheckBox apIcon = new JCheckBox("áp", !nhieu);
        JCheckBox apHt = new JCheckBox("áp", !nhieu);
        for (JCheckBox cb : new JCheckBox[]{apTen, apK1, apK2, apIcon, apHt}) {
            cb.setOpaque(false);
            cb.setVisible(nhieu);
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        hangVq(form, c, y++, apTen, "Tên (mẫu p0…p3):", fTen);

        JPanel k1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        k1.setOpaque(false);
        k1.add(new JLabel("từ"));
        k1.add(fTu1);
        k1.add(new JLabel("đến"));
        k1.add(fDen1);
        hangVq(form, c, y++, apK1, "Khoảng trị số 1 (p0…p1):", k1);

        JPanel k2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        k2.setOpaque(false);
        k2.add(new JLabel("từ"));
        k2.add(fTu2);
        k2.add(new JLabel("đến"));
        k2.add(fDen2);
        hangVq(form, c, y++, apK2, "Khoảng trị số 2 (p2…p3):", k2);

        hangVq(form, c, y++, apIcon, "Icon:", fIcon);
        hangVq(form, c, y++, apHt, "Hành tinh:", fHt);

        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 3;
        StringBuilder dsId = new StringBuilder();
        for (nro.entity.intrinsic.Intrinsic it : chon) {
            if (dsId.length() > 0) {
                dsId.append(", ");
            }
            dsId.append(it.id);
        }
        form.add(nhan(nhieu
                ? "Đang sửa <b>" + chon.size() + " nội tại</b> (id " + dsId
                + "). Chỉ những ô bạn tích <b>áp</b> mới ghi đè."
                : "Đang sửa nội tại <b>id " + mau.id + "</b>. Id không đổi được — "
                + "tác dụng gắn cứng theo id."), c);

        if (JOptionPane.showConfirmDialog(this, form,
                nhieu ? "Sửa " + chon.size() + " nội tại" : "Sửa nội tại",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        short tu1;
        short den1;
        short tu2;
        short den2;
        short icon;
        try {
            tu1 = Short.parseShort(fTu1.getText().trim());
            den1 = Short.parseShort(fDen1.getText().trim());
            tu2 = Short.parseShort(fTu2.getText().trim());
            den2 = Short.parseShort(fDen2.getText().trim());
            icon = Short.parseShort(fIcon.getText().trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Các ô trị số và icon phải là số nguyên.");
            return;
        }

        int hong = 0;
        String hongDau = null;
        for (nro.entity.intrinsic.Intrinsic it : chon) {
            if (apTen.isSelected()) {
                it.name = fTen.getText();
            }
            if (apK1.isSelected()) {
                it.paramFrom1 = tu1;
                it.paramTo1 = den1;
            }
            if (apK2.isSelected()) {
                it.paramFrom2 = tu2;
                it.paramTo2 = den2;
            }
            if (apIcon.isSelected()) {
                it.icon = icon;
            }
            if (apHt.isSelected()) {
                it.gender = genderTuTen(fHt.getSelectedItem());
            }
            String loi = nro.repository.dao.NoiTaiDAO.luu(it);
            if (loi != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Nội tại id " + it.id + ": " + loi;
                }
            }
        }
        napBangNoiTai();
        note(hong == 0 ? OK_GREEN : WARN_RED, hong == 0
                ? "Đã lưu " + chon.size() + " nội tại — có hiệu lực ngay."
                : hongDau + " (" + hong + " dòng không lưu được)");
    }

    private void themDongNoiTai() {
        String s = JOptionPane.showInputDialog(this,
                "Id của nội tại mới?\n\n"
                + "Id quyết định TÁC DỤNG, không phải thứ tự hiện ra.\n"
                + "Dùng lại một id đã có là ghi đè dòng đó.\n"
                + "Id chưa có tác dụng viết trong mã thì dòng mới sẽ hiện tên\n"
                + "và bốc ra được, nhưng không cộng gì cho người chơi.",
                "Thêm nội tại", JOptionPane.QUESTION_MESSAGE);
        if (s == null) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "\"" + s + "\" không phải là số.");
            return;
        }
        nro.entity.intrinsic.Intrinsic it = new nro.entity.intrinsic.Intrinsic();
        it.id = id;
        it.name = "Nội tại mới p0% đến p1%";
        it.paramFrom1 = 1;
        it.paramTo1 = 10;
        it.paramFrom2 = 0;
        it.paramTo2 = 0;
        it.icon = -1;
        it.gender = 3;
        String kq = nro.repository.dao.NoiTaiDAO.luu(it);
        napBangNoiTai();
        if (kq == null) {
            note(OK_GREEN, "Đã thêm nội tại id " + id
                    + (nro.repository.dao.NoiTaiDAO.coTacDung(id) ? "."
                            : " — id này CHƯA có tác dụng viết trong mã."));
        } else {
            note(WARN_RED, kq);
        }
    }

    private void xoaDongNoiTai() {
        int r = noiTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn dòng nào.");
            return;
        }
        r = noiTable.convertRowIndexToModel(r);
        int id;
        try {
            id = Integer.parseInt(oNoi(r, COT_NOI_ID));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Dòng này không có id hợp lệ.");
            return;
        }
        int chon = JOptionPane.showConfirmDialog(this,
                "Xoá nội tại id " + id + " — \"" + oNoi(r, COT_NOI_TEN) + "\"?\n\n"
                + "Người chơi đang mang nội tại này sẽ mất tác dụng của nó.",
                "Xoá nội tại", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        String kq = nro.repository.dao.NoiTaiDAO.xoa(id);
        napBangNoiTai();
        note(kq == null ? OK_GREEN : WARN_RED,
                kq == null ? "Đã xoá nội tại id " + id + "." : kq);
    }

    private JComponent buildTiLeSaoTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        saoTable.setRowHeight(26);
        saoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {110, 90, 160, 110, 60, 400};
        for (int i = 0; i < saoTable.getColumnCount() && i < w.length; i++) {
            saoTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Lưu bảng", OK_GREEN, e -> luuBangSao()));
        nut.add(button("Tải lại", GREY, e -> napBangSao()));
        nut.add(button("Về mặc định", new Color(120, 90, 160), e -> {
            String loi = nro.repository.dao.TiLeSaoDAO.datLaiMacDinh();
            napBangSao();
            note(loi == null ? OK_GREEN : WARN_RED,
                    loi == null ? "Đã trả bảng về mặc định." : loi);
        }));

        root.add(nhan("Tỉ lệ đập sao pha lê lên trang bị, mỗi bậc một dòng. "
                + "<b>Tỉ lệ là phần trăm THẬT</b> — gõ <code>70</code> là bảy mươi "
                + "phần trăm, gõ <code>0.25</code> là một phần bốn trăm. "
                + "<br><br>"
                + "<b>Trước bản này con số chạy chỉ bằng một phần mười con số hiện ra:</b> "
                + "bảng chọn trong game in \"Tỉ lệ thành công: 70%\" nhưng phép bốc "
                + "lại tính trên một nghìn, nên thật ra là 7%. Bậc ★7 → ★8 ghi 0.25 "
                + "hoá ra <b>0,025%</b> — trung bình bốn nghìn lần mới lên một cái, "
                + "đúng như đã gặp. Nay hai con số ấy là một."
                + "<br><br>"
                + "Tắt một bậc thì bậc đó <b>không nâng được nữa</b>, và người chơi "
                + "nhận được câu báo rõ chứ không phải bấm mãi không lên. "
                + "Sửa xong bấm Lưu là có hiệu lực ngay."), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(saoTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangSao();
        return root;
    }

    private void napBangSao() {
        // Dung sua o dang go do di khi nguoi dung vua bam Tai lai.
        if (saoTable.isEditing()) {
            saoTable.getCellEditor().stopCellEditing();
        }
        saoModel.setRowCount(0);
        for (nro.repository.dao.TiLeSaoDAO.Bac b
                : nro.repository.dao.TiLeSaoDAO.napLai()) {
            saoModel.addRow(new Object[]{b.moTa(),
                String.valueOf(b.tiLe), String.valueOf(b.vang),
                String.valueOf(b.ngoc), b.bat, b.ghiChu});
        }
    }

    private void luuBangSao() {
        if (saoTable.isEditing()) {
            saoTable.getCellEditor().stopCellEditing();
        }
        int loi = 0;
        String loiDau = null;
        for (int r = 0; r < saoModel.getRowCount(); r++) {
            nro.repository.dao.TiLeSaoDAO.Bac b
                    = new nro.repository.dao.TiLeSaoDAO.Bac();
            b.sao = r;
            try {
                b.tiLe = Double.parseDouble(
                        String.valueOf(saoModel.getValueAt(r, 1)).trim().replace(',', '.'));
                b.vang = Long.parseLong(
                        String.valueOf(saoModel.getValueAt(r, 2)).trim().replace(".", ""));
                b.ngoc = Integer.parseInt(
                        String.valueOf(saoModel.getValueAt(r, 3)).trim().replace(".", ""));
            } catch (NumberFormatException ex) {
                loi++;
                if (loiDau == null) {
                    loiDau = "Dòng ★" + r + " có ô không phải số.";
                }
                continue;
            }
            Object bat = saoModel.getValueAt(r, 4);
            b.bat = (bat instanceof Boolean) ? (Boolean) bat : true;
            Object gc = saoModel.getValueAt(r, 5);
            b.ghiChu = gc == null ? "" : String.valueOf(gc);

            String kq = nro.repository.dao.TiLeSaoDAO.luu(b);
            if (kq != null) {
                loi++;
                if (loiDau == null) {
                    loiDau = "Dòng ★" + r + ": " + kq;
                }
            }
        }
        napBangSao();
        if (loi == 0) {
            note(OK_GREEN, "Đã lưu bảng tỉ lệ nâng sao — có hiệu lực ngay.");
        } else {
            note(WARN_RED, loiDau + " (" + loi + " dòng không lưu được)");
        }
    }

    /**
     * Tab "Tỉ lệ set kích hoạt" — bậc đồ nào hay ra khi nâng Huỷ Diệt.
     */
    private JComponent buildTiLeKichHoatTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        khTable.setRowHeight(26);
        khTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {80, 120, 90, 60, 480};
        for (int i = 0; i < khTable.getColumnCount() && i < w.length; i++) {
            khTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Lưu bảng", OK_GREEN, e -> luuBangKichHoat()));
        nut.add(button("Tải lại", GREY, e -> napBangKichHoat()));
        nut.add(button("Về mặc định", new Color(120, 90, 160), e -> {
            String loi = nro.repository.dao.TiLeKichHoatDAO.datLaiMacDinh();
            napBangKichHoat();
            note(loi == null ? OK_GREEN : WARN_RED,
                    loi == null ? "Đã trả bảng về mặc định." : loi);
        }));

        root.add(nhan("Nâng một món Huỷ Diệt ở Bà Hạt Mít cho ra một món đồ set "
                + "kích hoạt <b>ngẫu nhiên</b>. Bảng này quyết định <b>bậc đồ</b> nào "
                + "hay ra: bậc 1 là mẫu thấp nhất, bậc 7 là cao nhất — Lưỡng Long / "
                + "Jeancalic / Vàng Zealot Tướng tuỳ hành tinh."
                + "<br><br>"
                + "Là <b>trọng số</b>, không phải phần trăm: không cần cộng cho tròn "
                + "một trăm, sửa một dòng không bắt sửa lại các dòng khác. Cột "
                + "\"Cơ hội\" là phần trăm tính ra từ chính các trọng số đang có. "
                + "Đặt 0 hoặc tắt là bậc đó không bao giờ ra."
                + "<br><br>"
                + "<b>Set gắn lên món đồ bốc trong tab \"Set kích hoạt\"</b>, lọc theo "
                + "hành tinh của người chơi (set để trống hành tinh thì hành tinh nào "
                + "cũng ra được). Thêm một set ở tab đó là nó vào ngay vòng bốc — "
                + "trước bản này thì không, vì đường nâng cấp giữ một bản danh sách "
                + "riêng gõ cứng trong mã."), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(khTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangKichHoat();
        return root;
    }

    private void napBangKichHoat() {
        if (khTable.isEditing()) {
            khTable.getCellEditor().stopCellEditing();
        }
        khModel.setRowCount(0);
        for (nro.repository.dao.TiLeKichHoatDAO.Bac b
                : nro.repository.dao.TiLeKichHoatDAO.napLai()) {
            khModel.addRow(new Object[]{"Bậc " + (b.bac + 1),
                String.valueOf(b.trongSo),
                String.format("%.2f", nro.repository.dao.TiLeKichHoatDAO
                        .phanTram(b.bac)).replace('.', ',') + "%",
                b.bat, b.ghiChu});
        }
    }

    private void luuBangKichHoat() {
        if (khTable.isEditing()) {
            khTable.getCellEditor().stopCellEditing();
        }
        int loi = 0;
        String loiDau = null;
        for (int r = 0; r < khModel.getRowCount(); r++) {
            nro.repository.dao.TiLeKichHoatDAO.Bac b
                    = new nro.repository.dao.TiLeKichHoatDAO.Bac();
            b.bac = r;
            try {
                b.trongSo = Long.parseLong(
                        String.valueOf(khModel.getValueAt(r, 1)).trim().replace(".", ""));
            } catch (NumberFormatException ex) {
                loi++;
                if (loiDau == null) {
                    loiDau = "Bậc " + (r + 1) + ": trọng số không phải số.";
                }
                continue;
            }
            Object bat = khModel.getValueAt(r, 3);
            b.bat = (bat instanceof Boolean) ? (Boolean) bat : true;
            Object gc = khModel.getValueAt(r, 4);
            b.ghiChu = gc == null ? "" : String.valueOf(gc);

            String kq = nro.repository.dao.TiLeKichHoatDAO.luu(b);
            if (kq != null) {
                loi++;
                if (loiDau == null) {
                    loiDau = "Bậc " + (r + 1) + ": " + kq;
                }
            }
        }
        napBangKichHoat();
        note(loi == 0 ? OK_GREEN : WARN_RED,
                loi == 0 ? "Đã lưu bảng tỉ lệ set kích hoạt — có hiệu lực ngay."
                        : loiDau + " (" + loi + " dòng không lưu được)");
    }

    private JComponent buildCapsuleTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        capsuleTable.setRowHeight(24);
        capsuleTable.setAutoCreateRowSorter(true);
        capsuleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {50, 60, 260, 110, 190, 50, 300};
        for (int i = 0; i < capsuleTable.getColumnCount() && i < w.length; i++) {
            capsuleTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel nut = new JPanel(new GridLayout(0, 3, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm địa điểm", OK_GREEN, e -> suaCapsuleDialog(true)));
        nut.add(button("Sửa địa điểm", ACCENT, e -> suaCapsuleDialog(false)));
        nut.add(button("Xoá địa điểm", WARN_RED, e -> xoaCapsule()));
        nut.add(button("Lên trên", GREY, e -> doiThuTuCapsule(-1)));
        nut.add(button("Xuống dưới", GREY, e -> doiThuTuCapsule(1)));
        nut.add(button("Tải lại", GREY, e -> napBangCapsule()));

        root.add(nhan("Nơi capsule bay tới — thứ tự ở đây là thứ tự hiện trong "
                + "game. \"Theo hành tinh\" nghĩa là bản đồ thật = id + hành tinh "
                + "(21 → Nhà Gôhan/Moori/Broly). Sửa xong có hiệu lực ngay, "
                + "không cần khởi động lại."), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(capsuleTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangCapsule();
        return root;
    }

    private void napBangCapsule() {
        capsuleModel.setRowCount(0);
        for (nro.repository.dao.CapsuleMapDAO.Diem d
                : nro.repository.dao.CapsuleMapDAO.danhSach()) {
            capsuleModel.addRow(new Object[]{d.id, d.thuTu,
                nro.ui.MapPicker.moTa(String.valueOf(d.mapId)),
                d.theoHanhTinh ? "có" : "", d.moTaDieuKien(),
                d.bat ? "bật" : "tắt", d.ghiChu});
        }
    }

    /** Dòng đang chọn, hoặc {@code null} kèm nhắc chọn dòng. */
    private nro.repository.dao.CapsuleMapDAO.Diem capsuleDangChon() {
        int r = capsuleTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một địa điểm trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(capsuleModel.getValueAt(
                capsuleTable.convertRowIndexToModel(r), 0)));
        for (nro.repository.dao.CapsuleMapDAO.Diem d
                : nro.repository.dao.CapsuleMapDAO.danhSach()) {
            if (d.id == id) {
                return d;
            }
        }
        return null;
    }

    private void suaCapsuleDialog(boolean them) {
        final nro.repository.dao.CapsuleMapDAO.Diem d;
        if (them) {
            d = new nro.repository.dao.CapsuleMapDAO.Diem();
            d.thuTu = capsuleModel.getRowCount();
            // -1 = CHƯA chọn. Không để 0: id 0 là Làng Aru có thật, để 0 thì
            // dòng mới vừa thêm đã thành Làng Aru dù chưa chọn gì.
            d.mapId = -1;
        } else {
            d = capsuleDangChon();
            if (d == null) {
                return;
            }
        }

        final JTextField fMap = new JTextField(
                d.mapId < 0 ? "" : String.valueOf(d.mapId), 6);
        final JLabel lblMap = new JLabel(d.mapId < 0 ? "(chưa chọn bản đồ)"
                : nro.ui.MapPicker.moTa(fMap.getText()));
        lblMap.setForeground(GREY);
        JButton btnChonMap = button("Chọn bản đồ…", ACCENT, e -> {
            // Truyền ĐÚNG giá trị đang có, và để trống khi chưa chọn.
            // MapPicker tích sẵn những id nhận vào; truyền "0" cho dòng mới thì
            // nó tích sẵn Làng Aru, người dùng tích thêm một map nữa là danh
            // sách thành {0, map kia} — lấy phần tử đầu ra đúng Làng Aru. Đó là
            // lỗi "thêm điểm nào cũng ra Làng Aru".
            String moi = nro.ui.MapPicker.chon(this, fMap.getText());
            if (moi == null) {
                return;
            }
            java.util.List<Integer> so = nro.ui.MapPicker.tachSo(moi);
            if (so.isEmpty()) {
                fMap.setText("");
                lblMap.setText("(chưa chọn bản đồ)");
                return;
            }
            fMap.setText(String.valueOf(so.get(0)));
            lblMap.setText(nro.ui.MapPicker.moTa(fMap.getText())
                    + (so.size() > 1 ? "   — tích nhiều, chỉ lấy bản đồ đầu" : ""));
        });

        final JCheckBox cbHanhTinh = new JCheckBox(
                "Theo hành tinh (bản đồ thật = id + hành tinh)", d.theoHanhTinh);
        final JCheckBox cbBat = new JCheckBox("Bật", d.bat);

        final JComboBox<String> cbDieuKien = new JComboBox<>(new String[]{
            "Luôn hiện", "Cấp luyện tập lớn hơn", "Sức mạnh lớn hơn"});
        cbDieuKien.setSelectedIndex(
                nro.repository.dao.CapsuleMapDAO.LUYEN_TAP.equals(d.dieuKien) ? 1
                : nro.repository.dao.CapsuleMapDAO.SUC_MANH.equals(d.dieuKien) ? 2 : 0);
        final JTextField fNguong = new JTextField(String.valueOf(d.nguong), 12);
        fNguong.setEnabled(cbDieuKien.getSelectedIndex() != 0);
        cbDieuKien.addActionListener(
                e -> fNguong.setEnabled(cbDieuKien.getSelectedIndex() != 0));

        final JTextField fThuTu = new JTextField(String.valueOf(d.thuTu), 6);
        final JTextField fGhiChu = new JTextField(d.ghiChu, 28);

        JPanel p = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new java.awt.Insets(4, 4, 4, 4);
        g.anchor = java.awt.GridBagConstraints.WEST;
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Bản đồ:"), g);
        g.gridx = 1;
        JPanel hangMap = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 0));
        hangMap.setOpaque(false);
        hangMap.add(fMap);
        hangMap.add(btnChonMap);
        hangMap.add(lblMap);
        p.add(hangMap, g);
        g.gridx = 1;
        g.gridy = ++y;
        p.add(cbHanhTinh, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Điều kiện:"), g);
        g.gridx = 1;
        p.add(cbDieuKien, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Ngưỡng:"), g);
        g.gridx = 1;
        p.add(fNguong, g);
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
        p.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1;
        p.add(fGhiChu, g);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm địa điểm capsule" : "Sửa địa điểm capsule",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        if (fMap.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa chọn bản đồ. Bấm \"Chọn bản đồ…\" và tích một bản đồ.",
                    "Thiếu bản đồ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            d.mapId = Integer.parseInt(fMap.getText().trim());
            d.thuTu = Integer.parseInt(fThuTu.getText().trim());
            d.nguong = cbDieuKien.getSelectedIndex() == 0 ? 0
                    : Long.parseLong(fNguong.getText().trim().replace(".", ""));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Bản đồ, thứ tự và ngưỡng phải là số.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (d.mapId < 0) {
            JOptionPane.showMessageDialog(this, "Id bản đồ phải từ 0 trở lên.",
                    "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        d.theoHanhTinh = cbHanhTinh.isSelected();
        d.bat = cbBat.isSelected();
        d.ghiChu = fGhiChu.getText().trim();
        d.dieuKien = cbDieuKien.getSelectedIndex() == 1
                ? nro.repository.dao.CapsuleMapDAO.LUYEN_TAP
                : cbDieuKien.getSelectedIndex() == 2
                        ? nro.repository.dao.CapsuleMapDAO.SUC_MANH
                        : nro.repository.dao.CapsuleMapDAO.LUON;

        String loi = them ? nro.repository.dao.CapsuleMapDAO.them(d)
                : nro.repository.dao.CapsuleMapDAO.sua(d);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangCapsule();
        note(OK_GREEN, them ? "Đã thêm địa điểm." : "Đã lưu địa điểm.");
    }

    private void xoaCapsule() {
        nro.repository.dao.CapsuleMapDAO.Diem d = capsuleDangChon();
        if (d == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá địa điểm bản đồ " + d.mapId + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.CapsuleMapDAO.xoa(d.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangCapsule();
        note(OK_GREEN, "Đã xoá địa điểm.");
    }

    /** Đổi chỗ dòng đang chọn với dòng liền trên hoặc liền dưới. */
    private void doiThuTuCapsule(int huong) {
        nro.repository.dao.CapsuleMapDAO.Diem d = capsuleDangChon();
        if (d == null) {
            return;
        }
        java.util.List<nro.repository.dao.CapsuleMapDAO.Diem> ds =
                nro.repository.dao.CapsuleMapDAO.danhSach();
        int i = -1;
        for (int k = 0; k < ds.size(); k++) {
            if (ds.get(k).id == d.id) {
                i = k;
                break;
            }
        }
        int j = i + huong;
        if (i < 0 || j < 0 || j >= ds.size()) {
            return;
        }
        // Doi CHINH thu tu cua hai dong, khong danh so lai ca bang: it cham vao
        // du lieu nhat, va van dung ke ca khi thu tu dang co lo hong.
        nro.repository.dao.CapsuleMapDAO.Diem kia = ds.get(j);
        int tam = d.thuTu;
        d.thuTu = kia.thuTu;
        kia.thuTu = tam;
        if (d.thuTu == kia.thuTu) {
            // Hai dong cung so thu tu -> lech chung ra de doi cho co tac dung.
            kia.thuTu += huong > 0 ? -1 : 1;
        }
        nro.repository.dao.CapsuleMapDAO.sua(d);
        nro.repository.dao.CapsuleMapDAO.sua(kia);
        napBangCapsule();
        // Giu nguyen dong dang chon sau khi bang nap lai.
        for (int r = 0; r < capsuleModel.getRowCount(); r++) {
            if (Integer.parseInt(String.valueOf(capsuleModel.getValueAt(r, 0))) == d.id) {
                int v = capsuleTable.convertRowIndexToView(r);
                if (v >= 0) {
                    capsuleTable.setRowSelectionInterval(v, v);
                }
                break;
            }
        }
    }

    // =====================================================================
    //  Tab: Bản đồ nhanh
    // =====================================================================

    /**
     * Quản lý danh sách hiện trong menu <b>Bản đồ</b> của game.
     *
     * <p>Menu trong game chia hai tầng: chọn <b>nhóm</b> trước, rồi mới ra bản
     * đồ trong nhóm đó. Cột "Nhóm" ở đây quyết định tầng một — gõ trùng tên là
     * gom chung một nhóm, gõ tên mới là sinh thêm một nhóm.</p>
     *
     * <p>Thứ tự nhóm hiện trong game là thứ tự nhóm đó <i>xuất hiện lần đầu</i>
     * trong bảng, nên hai nút Lên/Xuống đổi được cả chỗ đứng của nhóm.</p>
     */
    /**
     * Tab "Sách tuyệt kỹ" — số dòng chưa giám định và kho chỉ số.
     *
     * <p>Hai thứ trên một tab vì chúng là hai nửa của cùng một chuyện: ghép sách
     * ra bao nhiêu dòng {@code - Chưa giám định}, rồi giám định mỗi dòng thành
     * chỉ số gì với trị số bao nhiêu.</p>
     */
    private JComponent buildSachTuyetKyTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        stkTable.setRowHeight(24);
        stkTable.setAutoCreateRowSorter(true);
        stkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {45, 55, 80, 240, 90, 90, 55, 220};
        for (int i = 0; i < stkTable.getColumnCount() && i < w.length; i++) {
            stkTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        stkTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    suaChiSoSachDialog(false);
                }
            }
        });

        // ---- Hàng cài đặt chung ----
        JPanel tren = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 6, 4));
        tren.setOpaque(false);
        tren.add(new JLabel("Số dòng chưa giám định của sách vừa ghép: từ"));
        tren.add(stkDongMin);
        tren.add(new JLabel("đến"));
        tren.add(stkDongMax);
        tren.add(button("Lưu cài đặt", OK_GREEN, e -> luuCaiDatSach()));

        JPanel tren2 = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 6, 0));
        tren2.setOpaque(false);
        tren2.add(stkDungChung);

        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(nhan("Sách mới ra từ công thức \"Đổi Sách Tuyệt kỹ\" ở Bà Hạt "
                + "Mít. Số dòng \"- Chưa giám định\" lấy ngẫu nhiên trong khoảng "
                + "đặt ở đây, KHÔNG lấy theo cột chỉ số của công thức nữa — các "
                + "chỉ số khác của công thức (độ bền, số lần tẩy, yêu cầu sức "
                + "mạnh) vẫn giữ nguyên. Bảng dưới là kho chỉ số: mỗi lần giám "
                + "định một dòng, máy chủ bốc một chỉ số đang bật rồi bốc trị số "
                + "trong khoảng của nó. Sửa xong có hiệu lực ngay, không cần "
                + "khởi động lại."), BorderLayout.NORTH);
        JPanel hang = new JPanel(new java.awt.GridLayout(0, 1));
        hang.setOpaque(false);
        hang.add(tren);
        hang.add(tren2);
        dau.add(hang, BorderLayout.CENTER);

        JPanel nut = new JPanel(new GridLayout(0, 4, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm chỉ số", OK_GREEN, e -> suaChiSoSachDialog(true)));
        nut.add(button("Sửa", ACCENT, e -> suaChiSoSachDialog(false)));
        nut.add(button("Bật / Tắt", ACCENT, e -> batTatChiSoSach()));
        nut.add(button("Xoá", WARN_RED, e -> xoaChiSoSach()));
        nut.add(button("Tải lại", GREY, e -> napBangSach()));
        nut.add(button("Khôi phục kho gốc", WARN_RED, e -> gieoLaiKhoSach()));

        root.add(dau, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(stkTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangSach();
        return root;
    }

    private void napBangSach() {
        nro.repository.dao.SachTuyetKyDAO.CaiDat cd
                = nro.repository.dao.SachTuyetKyDAO.caiDat();
        stkDongMin.setValue(cd.soDongMin);
        stkDongMax.setValue(cd.soDongMax);
        stkDungChung.setSelected(cd.dungChungHanhTinh);

        stkModel.setRowCount(0);
        for (nro.repository.dao.SachTuyetKyDAO.ChiSo cs
                : nro.repository.dao.SachTuyetKyDAO.danhSachChiSo()) {
            stkModel.addRow(new Object[]{cs.id, cs.thuTu, cs.optionId, cs.ten(),
                cs.min, cs.max, cs.bat ? "bật" : "tắt", cs.ghiChu});
        }
    }

    private void luuCaiDatSach() {
        nro.repository.dao.SachTuyetKyDAO.CaiDat cd
                = new nro.repository.dao.SachTuyetKyDAO.CaiDat();
        cd.soDongMin = (Integer) stkDongMin.getValue();
        cd.soDongMax = (Integer) stkDongMax.getValue();
        cd.dungChungHanhTinh = stkDungChung.isSelected();
        String loi = nro.repository.dao.SachTuyetKyDAO.luuCaiDat(cd);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        // Đổi cờ dùng chung phải áp ngay vào bảng vật phẩm đang chạy, không thì
        // admin lưu xong vào game vẫn thấy dòng "Dành cho ...".
        nro.repository.dao.SachTuyetKyDAO.apDungDungChung();
        napBangSach();
        note(OK_GREEN, "Đã lưu. Sách ghép mới sẽ ra "
                + cd.soDongMin + " – " + cd.soDongMax + " dòng chưa giám định"
                + (cd.dungChungHanhTinh
                        ? ", và cả ba hành tinh dùng chung được."
                        : ", sách vẫn khoá theo hành tinh.")
                + " Người chơi đang trong game cần thoát ra vào lại mới thấy "
                + "khoá hành tinh đổi.");
    }

    /** Dòng đang chọn trong kho chỉ số, hoặc {@code null} kèm nhắc chọn dòng. */
    private nro.repository.dao.SachTuyetKyDAO.ChiSo chiSoSachDangChon() {
        int r = stkTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một chỉ số trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(stkModel.getValueAt(
                stkTable.convertRowIndexToModel(r), COT_STK_ID)));
        for (nro.repository.dao.SachTuyetKyDAO.ChiSo cs
                : nro.repository.dao.SachTuyetKyDAO.danhSachChiSo()) {
            if (cs.id == id) {
                return cs;
            }
        }
        return null;
    }

    private void suaChiSoSachDialog(boolean them) {
        final nro.repository.dao.SachTuyetKyDAO.ChiSo cs;
        if (them) {
            cs = new nro.repository.dao.SachTuyetKyDAO.ChiSo();
            cs.thuTu = stkModel.getRowCount();
            cs.optionId = -1;
        } else {
            cs = chiSoSachDangChon();
            if (cs == null) {
                return;
            }
        }

        // Chọn chỉ số từ danh sách thay vì gõ id: gõ nhầm một số là sách ra một
        // dòng chẳng ai hiểu, mà lỗi kiểu đó không báo gì cả.
        final JComboBox<String> cbChiSo = new JComboBox<>();
        int chon = -1;
        java.util.List<Integer> idTheoViTri = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Integer, String> e
                : nro.repository.dao.SachTuyetKyDAO.khoTenChiSo().entrySet()) {
            int id = e.getKey();
            if (id == nro.repository.dao.SachTuyetKyDAO.OPTION_CHUA_GIAM_DINH) {
                continue;
            }
            cbChiSo.addItem(id + " — " + e.getValue());
            idTheoViTri.add(id);
            if (id == cs.optionId) {
                chon = idTheoViTri.size() - 1;
            }
        }
        if (chon >= 0) {
            cbChiSo.setSelectedIndex(chon);
        }

        final JSpinner fMin = new JSpinner(
                new SpinnerNumberModel(cs.min, -100000, 100000, 1));
        final JSpinner fMax = new JSpinner(
                new SpinnerNumberModel(cs.max, -100000, 100000, 1));
        final JTextField fThuTu = new JTextField(String.valueOf(cs.thuTu), 6);
        final JCheckBox cbBat = new JCheckBox("Đang bật", cs.bat);
        final JTextField fGhiChu = new JTextField(cs.ghiChu, 24);

        JPanel p = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new java.awt.Insets(4, 4, 4, 4);
        g.anchor = java.awt.GridBagConstraints.WEST;
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Chỉ số:"), g);
        g.gridx = 1;
        p.add(cbChiSo, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Trị số bốc trong khoảng:"), g);
        g.gridx = 1;
        JPanel hangTri = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 0));
        hangTri.setOpaque(false);
        hangTri.add(new JLabel("từ"));
        hangTri.add(fMin);
        hangTri.add(new JLabel("đến"));
        hangTri.add(fMax);
        JLabel goiY = new JLabel("hai đầu đều có thể bốc trúng");
        goiY.setForeground(GREY);
        hangTri.add(goiY);
        p.add(hangTri, g);
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
        p.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1;
        p.add(fGhiChu, g);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm chỉ số vào kho sách" : "Sửa chỉ số trong kho sách",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        int vt = cbChiSo.getSelectedIndex();
        if (vt < 0 || vt >= idTheoViTri.size()) {
            note(WARN_RED, "Chưa chọn chỉ số.");
            return;
        }
        cs.optionId = idTheoViTri.get(vt);
        cs.min = (Integer) fMin.getValue();
        cs.max = (Integer) fMax.getValue();
        try {
            cs.thuTu = Integer.parseInt(fThuTu.getText().trim());
        } catch (NumberFormatException ex) {
            cs.thuTu = 0;
        }
        cs.bat = cbBat.isSelected();
        cs.ghiChu = fGhiChu.getText().trim();

        String loi = them
                ? nro.repository.dao.SachTuyetKyDAO.them(cs)
                : nro.repository.dao.SachTuyetKyDAO.sua(cs);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        napBangSach();
        note(OK_GREEN, (them ? "Đã thêm " : "Đã sửa ")
                + nro.repository.dao.SachTuyetKyDAO.tenChiSo(cs.optionId)
                + " (" + cs.moTaKhoang() + ").");
    }

    private void batTatChiSoSach() {
        nro.repository.dao.SachTuyetKyDAO.ChiSo cs = chiSoSachDangChon();
        if (cs == null) {
            return;
        }
        String loi = nro.repository.dao.SachTuyetKyDAO.batTat(cs.id, !cs.bat);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        napBangSach();
        note(OK_GREEN, (cs.bat ? "Đã tắt " : "Đã bật ") + cs.ten() + ".");
    }

    private void xoaChiSoSach() {
        nro.repository.dao.SachTuyetKyDAO.ChiSo cs = chiSoSachDangChon();
        if (cs == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá " + cs.ten() + " khỏi kho chỉ số của sách?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.SachTuyetKyDAO.xoa(cs.id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        napBangSach();
        note(OK_GREEN, "Đã xoá " + cs.ten() + " khỏi kho.");
    }

    private void gieoLaiKhoSach() {
        if (JOptionPane.showConfirmDialog(this,
                "Xoá sạch kho hiện tại và gieo lại 12 chỉ số gốc?\n"
                + "Sách người chơi đang giữ không bị ảnh hưởng — chỉ đổi thứ "
                + "bốc ra cho những lần giám định sau.",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.SachTuyetKyDAO.gieoLaiKho();
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        napBangSach();
        note(OK_GREEN, "Đã khôi phục kho chỉ số gốc.");
    }

    private JComponent buildMapNhanhTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        mnTable.setRowHeight(24);
        mnTable.setAutoCreateRowSorter(true);
        mnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w = {45, 55, 130, 170, 250, 100, 90, 45, 220};
        for (int i = 0; i < mnTable.getColumnCount() && i < w.length; i++) {
            mnTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        mnTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    suaMapNhanhDialog(false);
                }
            }
        });

        JPanel nut = new JPanel(new GridLayout(0, 4, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm bản đồ", OK_GREEN, e -> suaMapNhanhDialog(true)));
        nut.add(button("Sửa", ACCENT, e -> suaMapNhanhDialog(false)));
        nut.add(button("Bật / Tắt", ACCENT, e -> batTatMapNhanh()));
        nut.add(button("Xoá", WARN_RED, e -> xoaMapNhanh()));
        nut.add(button("Lên trên", GREY, e -> doiThuTuMapNhanh(-1)));
        nut.add(button("Xuống dưới", GREY, e -> doiThuTuMapNhanh(1)));
        nut.add(button("Tải lại", GREY, e -> napBangMapNhanh()));
        nut.add(button("Khôi phục danh sách sẵn", WARN_RED,
                e -> gieoLaiMapNhanh()));

        root.add(nhan("Danh sách hiện trong menu \"Bản đồ\" của game. Người chơi "
                + "chọn nhóm trước rồi mới chọn bản đồ, nên cột Nhóm quyết định "
                + "menu tầng một — gõ trùng tên là gom chung. Toạ độ để trống "
                + "(\"tự đặt\") thì máy chủ thả người chơi xuống giữa bản đồ. "
                + "\"Theo hành tinh\" nghĩa là bản đồ thật = id + hành tinh "
                + "(21 → Nhà Gôhan/Moori/Broly). Sửa xong có hiệu lực ngay, "
                + "không cần khởi động lại máy chủ."), BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(mnTable), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        napBangMapNhanh();
        return root;
    }

    private void napBangMapNhanh() {
        mnModel.setRowCount(0);
        for (nro.repository.dao.MapNhanhDAO.Diem d
                : nro.repository.dao.MapNhanhDAO.danhSach()) {
            mnModel.addRow(new Object[]{d.id, d.thuTu, d.nhom, d.nhan(),
                nro.ui.MapPicker.moTa(String.valueOf(d.mapId)),
                d.moTaToaDo(), d.theoHanhTinh ? "có" : "",
                d.bat ? "bật" : "tắt", d.ghiChu});
        }
    }

    /** Dòng đang chọn, hoặc {@code null} kèm nhắc chọn dòng. */
    private nro.repository.dao.MapNhanhDAO.Diem mapNhanhDangChon() {
        int r = mnTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một bản đồ trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(mnModel.getValueAt(
                mnTable.convertRowIndexToModel(r), COT_MN_ID)));
        for (nro.repository.dao.MapNhanhDAO.Diem d
                : nro.repository.dao.MapNhanhDAO.danhSach()) {
            if (d.id == id) {
                return d;
            }
        }
        return null;
    }

    private void suaMapNhanhDialog(boolean them) {
        final nro.repository.dao.MapNhanhDAO.Diem d;
        if (them) {
            d = new nro.repository.dao.MapNhanhDAO.Diem();
            d.thuTu = mnModel.getRowCount();
            // -1 = CHUA chon. Khong de 0: id 0 la Lang Aru co that, de 0 thi
            // dong moi vua them da thanh Lang Aru du chua chon gi.
            d.mapId = -1;
            // Lay nhom cua dong dang chon, de them lien tiep vao cung mot nhom
            // ma khong phai go lai ten.
            int r = mnTable.getSelectedRow();
            if (r >= 0) {
                d.nhom = String.valueOf(mnModel.getValueAt(
                        mnTable.convertRowIndexToModel(r), COT_MN_NHOM));
            }
        } else {
            d = mapNhanhDangChon();
            if (d == null) {
                return;
            }
        }

        // O nhom go tay duoc NHUNG co goi y: nhom moi phai go duoc, ma nhom da
        // co thi chon cho khoi go sai chinh ta roi tach thanh hai nhom trong
        // game giong het nhau.
        java.util.Set<String> nhomDaCo = new java.util.LinkedHashSet<>();
        for (nro.repository.dao.MapNhanhDAO.Diem x
                : nro.repository.dao.MapNhanhDAO.danhSach()) {
            if (x.nhom != null && !x.nhom.trim().isEmpty()) {
                nhomDaCo.add(x.nhom.trim());
            }
        }
        final JComboBox<String> cbNhom = new JComboBox<>(
                nhomDaCo.toArray(new String[0]));
        cbNhom.setEditable(true);
        cbNhom.setSelectedItem(d.nhom == null ? "" : d.nhom);

        final JTextField fTen = new JTextField(d.ten, 24);
        final JTextField fMap = new JTextField(
                d.mapId < 0 ? "" : String.valueOf(d.mapId), 6);
        final JLabel lblMap = new JLabel(d.mapId < 0 ? "(chưa chọn bản đồ)"
                : nro.ui.MapPicker.moTa(fMap.getText()));
        lblMap.setForeground(GREY);
        JButton btnChonMap = button("Chọn bản đồ", ACCENT, e -> {
            String moi = nro.ui.MapPicker.chon(this, fMap.getText());
            if (moi == null) {
                return;
            }
            java.util.List<Integer> so = nro.ui.MapPicker.tachSo(moi);
            if (so.isEmpty()) {
                fMap.setText("");
                lblMap.setText("(chưa chọn bản đồ)");
                return;
            }
            fMap.setText(String.valueOf(so.get(0)));
            lblMap.setText(nro.ui.MapPicker.moTa(fMap.getText())
                    + (so.size() > 1 ? "   — tích nhiều, chỉ lấy bản đồ đầu" : ""));
            // Ten con trong thi lay luon ten ban do vua chon, do phai go tay.
            if (fTen.getText().trim().isEmpty()) {
                fTen.setText(nro.ui.MapPicker.tenMap(so.get(0)));
            }
        });

        final JTextField fX = new JTextField(d.x < 0 ? "" : String.valueOf(d.x), 6);
        final JTextField fY = new JTextField(d.y < 0 ? "" : String.valueOf(d.y), 6);
        final JCheckBox cbHanhTinh = new JCheckBox(
                "Theo hành tinh (bản đồ thật = id + hành tinh)", d.theoHanhTinh);
        final JCheckBox cbBat = new JCheckBox("Bật", d.bat);
        final JTextField fThuTu = new JTextField(String.valueOf(d.thuTu), 6);
        final JTextField fGhiChu = new JTextField(d.ghiChu, 28);

        JPanel p = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new java.awt.Insets(4, 4, 4, 4);
        g.anchor = java.awt.GridBagConstraints.WEST;
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Nhóm:"), g);
        g.gridx = 1;
        p.add(cbNhom, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Tên hiện trong menu:"), g);
        g.gridx = 1;
        p.add(fTen, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Bản đồ:"), g);
        g.gridx = 1;
        JPanel hangMap = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 0));
        hangMap.setOpaque(false);
        hangMap.add(fMap);
        hangMap.add(btnChonMap);
        hangMap.add(lblMap);
        p.add(hangMap, g);
        g.gridx = 1;
        g.gridy = ++y;
        p.add(cbHanhTinh, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Toạ độ:"), g);
        g.gridx = 1;
        JPanel hangXY = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 0));
        hangXY.setOpaque(false);
        hangXY.add(new JLabel("x"));
        hangXY.add(fX);
        hangXY.add(new JLabel("y"));
        hangXY.add(fY);
        JLabel goiYXY = new JLabel("để trống là máy chủ tự thả xuống mặt đất");
        goiYXY.setForeground(GREY);
        hangXY.add(goiYXY);
        p.add(hangXY, g);
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
        p.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1;
        p.add(fGhiChu, g);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm bản đồ vào menu" : "Sửa bản đồ trong menu",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        if (fMap.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa chọn bản đồ. Bấm nút chọn bản đồ và tích một bản đồ.",
                    "Thiếu bản đồ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            d.mapId = Integer.parseInt(fMap.getText().trim());
            d.thuTu = Integer.parseInt(fThuTu.getText().trim());
            d.x = fX.getText().trim().isEmpty() ? -1
                    : Integer.parseInt(fX.getText().trim());
            d.y = fY.getText().trim().isEmpty() ? -1
                    : Integer.parseInt(fY.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Bản đồ, thứ tự và toạ độ phải là số.", "Sai dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        d.nhom = String.valueOf(cbNhom.getSelectedItem() == null
                ? "" : cbNhom.getSelectedItem()).trim();
        d.ten = fTen.getText().trim();
        if (d.ten.isEmpty()) {
            d.ten = nro.ui.MapPicker.tenMap(d.mapId);
        }
        d.theoHanhTinh = cbHanhTinh.isSelected();
        d.bat = cbBat.isSelected();
        d.ghiChu = fGhiChu.getText().trim();

        String loi = them ? nro.repository.dao.MapNhanhDAO.them(d)
                : nro.repository.dao.MapNhanhDAO.sua(d);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangMapNhanh();
        note(OK_GREEN, them ? "Đã thêm bản đồ vào menu." : "Đã lưu.");
    }

    private void batTatMapNhanh() {
        nro.repository.dao.MapNhanhDAO.Diem d = mapNhanhDangChon();
        if (d == null) {
            return;
        }
        String loi = nro.repository.dao.MapNhanhDAO.batTat(d.id, !d.bat);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangMapNhanh();
        note(OK_GREEN, (d.bat ? "Đã tắt " : "Đã bật ") + d.nhan() + ".");
    }

    private void xoaMapNhanh() {
        nro.repository.dao.MapNhanhDAO.Diem d = mapNhanhDangChon();
        if (d == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá " + d.nhan() + " khỏi menu Bản đồ?", "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.MapNhanhDAO.xoa(d.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangMapNhanh();
        note(OK_GREEN, "Đã xoá.");
    }

    private void gieoLaiMapNhanh() {
        if (JOptionPane.showConfirmDialog(this,
                "Xoá sạch bảng rồi gieo lại danh sách sẵn?\n"
                + "Mọi dòng bạn tự thêm hoặc tự sửa sẽ mất.", "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.MapNhanhDAO.gieoLaiTuDau();
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không gieo lại được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangMapNhanh();
        note(OK_GREEN, "Đã gieo lại danh sách sẵn.");
    }

    /** Đổi chỗ dòng đang chọn với dòng liền trên hoặc liền dưới. */
    private void doiThuTuMapNhanh(int huong) {
        nro.repository.dao.MapNhanhDAO.Diem d = mapNhanhDangChon();
        if (d == null) {
            return;
        }
        java.util.List<nro.repository.dao.MapNhanhDAO.Diem> ds
                = nro.repository.dao.MapNhanhDAO.danhSach();
        int i = -1;
        for (int k = 0; k < ds.size(); k++) {
            if (ds.get(k).id == d.id) {
                i = k;
                break;
            }
        }
        int j = i + huong;
        if (i < 0 || j < 0 || j >= ds.size()) {
            return;
        }
        nro.repository.dao.MapNhanhDAO.Diem kia = ds.get(j);
        int tam = d.thuTu;
        d.thuTu = kia.thuTu;
        kia.thuTu = tam;
        if (d.thuTu == kia.thuTu) {
            // Hai dong cung so thu tu -> lech chung ra de doi cho co tac dung.
            kia.thuTu += huong > 0 ? -1 : 1;
        }
        nro.repository.dao.MapNhanhDAO.sua(d);
        nro.repository.dao.MapNhanhDAO.sua(kia);
        napBangMapNhanh();
        // Giu nguyen dong dang chon sau khi bang nap lai.
        for (int r = 0; r < mnModel.getRowCount(); r++) {
            if (Integer.parseInt(String.valueOf(
                    mnModel.getValueAt(r, COT_MN_ID))) == d.id) {
                int v = mnTable.convertRowIndexToView(r);
                if (v >= 0) {
                    mnTable.setRowSelectionInterval(v, v);
                }
                break;
            }
        }
    }

    // =====================================================================
    //  Tab: Sự kiện
    // =====================================================================

    /** Tên các sự kiện hay dùng — gợi ý sẵn, vẫn gõ tên khác được. */
    private static final String[] TEN_SU_KIEN_GOI_Y = {
        "Tết Nguyên Đán", "Trung Thu", "Halloween", "Giáng Sinh",
        "Quốc tế Phụ nữ 8/3", "Phụ nữ Việt Nam 20/10", "Nhà giáo 20/11",
        "Giỗ Tổ Hùng Vương", "Vu Lan", "Valentine", "Black Friday",
        "Sự kiện cuối tuần", "Sự kiện hè"};

    /**
     * Bật tắt sự kiện, và khai vật phẩm rơi / đổi quà / nội dung gói.
     *
     * <h2>Vì sao làm lại từ đầu</h2>
     *
     * <p>Bản gốc quản sự kiện bằng lớp {@code EventManager} với mười một cờ
     * {@code static boolean} gõ cứng, và {@code EventPanel} đi kèm ghi ra
     * {@code active_event.txt} rồi <b>khởi động lại máy chủ sau 10 giây</b>.
     * Cả hai lớp đó đã bị chú thích hết; trong mã đang chạy
     * {@code Mob.getItemEventMobReward()} còn nguyên lời gọi nhưng thân hàm
     * <b>rỗng</b> — nghĩa là tới trước bản này, máy chủ không rơi vật phẩm sự
     * kiện nào cả.</p>
     *
     * <h2>Bốn bảng, đọc từ trên xuống</h2>
     *
     * <ol>
     *   <li><b>Sự kiện</b> — tên, công tắc, khung giờ, câu loa</li>
     *   <li><b>Vật phẩm rơi</b> — món nào rơi từ <b>mọi quái</b>, mỗi món một
     *       tỉ lệ riêng; chỉ rơi khi sự kiện đang chạy</li>
     *   <li><b>Đổi ở Quy Lão</b> — N món này lấy M gói kia</li>
     *   <li><b>Nội dung gói</b> — mở gói ra được gì, bốc theo trọng số</li>
     * </ol>
     *
     * <p>Chọn một sự kiện ở bảng trên thì hai bảng giữa lọc theo sự kiện đó.
     * Bảng cuối khoá theo <b>id vật phẩm gói</b> chứ không theo sự kiện, nên
     * mùa sau dùng lại đúng cái gói đó thì khỏi khai lại nội dung.</p>
     */
    // =====================================================================
    //  Tab Phúc lợi
    // =====================================================================
    /**
     * Ba bảng nối tiếp nhau: nhóm → mốc của nhóm → quà của mốc.
     *
     * <p>Chọn dòng ở bảng trên thì bảng dưới đổ lại theo. Gộp cả ba vào một
     * bảng phẳng sẽ phải lặp tên nhóm ở mọi dòng quà, và không còn chỗ nào để
     * sửa riêng thuộc tính của nhóm.</p>
     */
    private final DefaultTableModel plNhomModel = new DefaultTableModel(
            new Object[]{"ID", "Tên mục", "Đếm theo", "Thứ tự", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable plNhomTable = new JTable(plNhomModel);

    private final DefaultTableModel plMocModel = new DefaultTableModel(
            new Object[]{"ID", "Mốc", "Thứ tự", "Bật", "Số quà", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable plMocTable = new JTable(plMocModel);

    private final DefaultTableModel plQuaModel = new DefaultTableModel(
            new Object[]{"ID", "Ảnh", "Vật phẩm", "Tên vật phẩm", "Số lượng", "Chỉ số"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 1 ? javax.swing.ImageIcon.class : Object.class;
        }
    };
    private final JTable plQuaTable = new JTable(plQuaModel);

    private JComponent buildPhucLoiTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        root.add(nhan("Phúc lợi — quà theo mốc tích luỹ. Trong game bấm nút "
                + "\"Phúc lợi\" trên màn hình, hoặc gõ chat: phucloi. "
                + "Sửa xong có hiệu lực ngay, không cần khởi động lại."),
                BorderLayout.NORTH);

        // ---------- nhóm ----------
        plNhomTable.setRowHeight(24);
        plNhomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plNhomTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wN = {45, 200, 130, 55, 45, 260};
        for (int i = 0; i < wN.length && i < plNhomTable.getColumnCount(); i++) {
            plNhomTable.getColumnModel().getColumn(i).setPreferredWidth(wN[i]);
        }
        plNhomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                plLoadMoc();
            }
        });
        plNhomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    plSuaNhom(false);
                }
            }
        });
        JPanel pNhom = new JPanel(new BorderLayout(0, 4));
        pNhom.setOpaque(false);
        pNhom.setBorder(titled("Mục phúc lợi (cột bên trái trong game)"));
        pNhom.add(ServerGuiUtils.cuon(plNhomTable), BorderLayout.CENTER);
        JPanel bNhom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bNhom.setOpaque(false);
        bNhom.add(button("Thêm mục", OK_GREEN, e -> plSuaNhom(true)));
        bNhom.add(button("Sửa mục", ACCENT, e -> plSuaNhom(false)));
        bNhom.add(button("Xoá mục", WARN_RED, e -> plXoaNhom()));
        bNhom.add(button("Tải lại", GREY, e -> plLoadNhom()));
        pNhom.add(bNhom, BorderLayout.SOUTH);

        // ---------- mốc ----------
        plMocTable.setRowHeight(24);
        plMocTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plMocTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wM = {45, 140, 55, 45, 60, 260};
        for (int i = 0; i < wM.length && i < plMocTable.getColumnCount(); i++) {
            plMocTable.getColumnModel().getColumn(i).setPreferredWidth(wM[i]);
        }
        plMocTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                plLoadQua();
            }
        });
        plMocTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    plSuaMoc(false);
                }
            }
        });
        JPanel pMoc = new JPanel(new BorderLayout(0, 4));
        pMoc.setOpaque(false);
        pMoc.setBorder(titled("Mốc của mục đang chọn"));
        pMoc.add(ServerGuiUtils.cuon(plMocTable), BorderLayout.CENTER);
        JPanel bMoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bMoc.setOpaque(false);
        bMoc.add(button("Thêm mốc", OK_GREEN, e -> plSuaMoc(true)));
        bMoc.add(button("Sửa mốc", ACCENT, e -> plSuaMoc(false)));
        bMoc.add(button("Xoá mốc", WARN_RED, e -> plXoaMoc()));
        pMoc.add(bMoc, BorderLayout.SOUTH);

        // ---------- quà ----------
        plQuaTable.setRowHeight(32);
        plQuaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plQuaTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wQ = {45, 40, 80, 240, 70, 200};
        for (int i = 0; i < wQ.length && i < plQuaTable.getColumnCount(); i++) {
            plQuaTable.getColumnModel().getColumn(i).setPreferredWidth(wQ[i]);
        }
        plQuaTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    plSuaQua(false);
                }
            }
        });
        JPanel pQua = new JPanel(new BorderLayout(0, 4));
        pQua.setOpaque(false);
        pQua.setBorder(titled("Quà của mốc đang chọn"));
        pQua.add(ServerGuiUtils.cuon(plQuaTable), BorderLayout.CENTER);
        JPanel bQua = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bQua.setOpaque(false);
        bQua.add(button("Thêm quà", OK_GREEN, e -> plSuaQua(true)));
        bQua.add(button("Sửa quà", ACCENT, e -> plSuaQua(false)));
        bQua.add(button("Xoá quà", WARN_RED, e -> plXoaQua()));
        pQua.add(bQua, BorderLayout.SOUTH);

        javax.swing.JSplitPane duoi = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT, pMoc, pQua);
        duoi.setResizeWeight(0.45);
        duoi.setBorder(null);
        javax.swing.JSplitPane tren = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, pNhom, duoi);
        tren.setResizeWeight(0.35);
        tren.setBorder(null);
        root.add(tren, BorderLayout.CENTER);
        return root;
    }

    /** Id nhóm đang chọn, hoặc {@code -1}. */
    private int plNhomDangChon() {
        int r = plNhomTable.getSelectedRow();
        return r < 0 ? -1 : intOf(plNhomModel.getValueAt(
                plNhomTable.convertRowIndexToModel(r), 0));
    }

    /** Id mốc đang chọn, hoặc {@code -1}. */
    private int plMocDangChon() {
        int r = plMocTable.getSelectedRow();
        return r < 0 ? -1 : intOf(plMocModel.getValueAt(
                plMocTable.convertRowIndexToModel(r), 0));
    }

    private void plLoadNhom() {
        plNhomModel.setRowCount(0);
        for (nro.repository.dao.PhucLoiDAO.Nhom n
                : nro.repository.dao.PhucLoiDAO.dsNhom(false)) {
            plNhomModel.addRow(new Object[]{n.id, n.ten,
                nro.repository.dao.PhucLoiDAO.tenLoai(n.loai), n.thuTu,
                n.bat ? "có" : "", n.ghiChu});
        }
        plLoadMoc();
    }

    private void plLoadMoc() {
        plMocModel.setRowCount(0);
        int nhom = plNhomDangChon();
        if (nhom > 0) {
            for (nro.repository.dao.PhucLoiDAO.Moc m
                    : nro.repository.dao.PhucLoiDAO.dsMoc(nhom, false)) {
                plMocModel.addRow(new Object[]{m.id,
                    PlayerManagerPanel.fmt(m.moc), m.thuTu, m.bat ? "có" : "",
                    nro.repository.dao.PhucLoiDAO.dsQua(m.id).size(), m.ghiChu});
            }
        }
        plLoadQua();
    }

    private void plLoadQua() {
        plQuaModel.setRowCount(0);
        int moc = plMocDangChon();
        if (moc <= 0) {
            return;
        }
        for (nro.repository.dao.PhucLoiDAO.Qua q
                : nro.repository.dao.PhucLoiDAO.dsQua(moc)) {
            nro.entity.template.ItemTemplate t = null;
            try {
                t = nro.service.item.ItemService.gI().getTemplate(q.itemId);
            } catch (Exception boQua) {
                // Bang mau chua nap -> van hien dong, chi thieu ten va anh.
            }
            plQuaModel.addRow(new Object[]{q.id,
                t != null ? PlayerManagerPanel.iconOf(t.iconID) : null,
                q.itemId, t != null ? t.name : "(không rõ)",
                PlayerManagerPanel.fmt(q.soLuong),
                "[]".equals(nz(q.chiSo)) ? "" : nz(q.chiSo)});
        }
    }

    private void plSuaNhom(boolean them) {
        int r = plNhomTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn mục nào để sửa.");
            return;
        }
        int idx = them ? -1 : plNhomTable.convertRowIndexToModel(r);
        JTextField fTen = new JTextField(them ? "" : nz(plNhomModel.getValueAt(idx, 1)), 22);
        JComboBox<String> cbLoai = new JComboBox<>();
        for (String l : nro.repository.dao.PhucLoiDAO.CAC_LOAI) {
            cbLoai.addItem(nro.repository.dao.PhucLoiDAO.tenLoai(l));
        }
        if (!them) {
            cbLoai.setSelectedItem(String.valueOf(plNhomModel.getValueAt(idx, 2)));
        }
        JTextField fThuTu = new JTextField(them ? "0"
                : String.valueOf(plNhomModel.getValueAt(idx, 3)), 6);
        JTextField fGhiChu = new JTextField(them ? "" : nz(plNhomModel.getValueAt(idx, 5)), 24);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                them || "có".equals(String.valueOf(plNhomModel.getValueAt(idx, 4))));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Tên mục:", fTen);
        addRowC(form, c, 1, "Đếm theo:", cbLoai);
        addRow(form, c, 2, "Thứ tự:", fThuTu);
        addRow(form, c, 3, "Ghi chú:", fGhiChu);
        c.gridx = 1;
        c.gridy = 4;
        form.add(cbBat, c);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        form.add(nhan("<html><span style='color:#777'>"
                + "<b>Đếm theo</b> quyết định con số \"Tích luỹ\" hiện trong game:<br>"
                + "Sức mạnh · Nhiệm vụ chính (id) · Sát thương 30 giây (máy đo sức mạnh)<br>"
                + "Số phút online (máy chủ tự cộng) · Tự đặt (quản trị tự nhập cho từng người)"
                + "</span></html>"), c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm mục phúc lợi" : "Sửa mục phúc lợi",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.PhucLoiDAO.Nhom n = new nro.repository.dao.PhucLoiDAO.Nhom();
        n.id = them ? 0 : intOf(plNhomModel.getValueAt(idx, 0));
        n.ten = fTen.getText().trim();
        n.loai = nro.repository.dao.PhucLoiDAO.CAC_LOAI[
                Math.max(0, cbLoai.getSelectedIndex())];
        n.bat = cbBat.isSelected();
        n.ghiChu = fGhiChu.getText().trim();
        try {
            n.thuTu = docSoNguyen(fThuTu.getText(), "thứ tự");
        } catch (IllegalArgumentException ex) {
            note(WARN_RED, ex.getMessage());
            return;
        }
        String loi = nro.repository.dao.PhucLoiDAO.luuNhom(n);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        plLoadNhom();
        note(OK_GREEN, them ? "Đã thêm mục phúc lợi" : "Đã sửa mục phúc lợi");
    }

    private void plXoaNhom() {
        int id = plNhomDangChon();
        if (id <= 0) {
            note(WARN_RED, "Chưa chọn mục nào.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá mục này cùng TOÀN BỘ mốc và quà bên trong?",
                "Xoá mục phúc lợi", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.PhucLoiDAO.xoaNhom(id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        plLoadNhom();
        note(OK_GREEN, "Đã xoá mục phúc lợi");
    }

    private void plSuaMoc(boolean them) {
        int nhom = plNhomDangChon();
        if (nhom <= 0) {
            note(WARN_RED, "Chọn mục phúc lợi trước đã.");
            return;
        }
        int r = plMocTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn mốc nào để sửa.");
            return;
        }
        int idx = them ? -1 : plMocTable.convertRowIndexToModel(r);
        JTextField fMoc = new JTextField(them ? "0"
                : soNguyenSach(plMocModel.getValueAt(idx, 1)), 14);
        JTextField fThuTu = new JTextField(them ? "0"
                : String.valueOf(plMocModel.getValueAt(idx, 2)), 6);
        JTextField fGhiChu = new JTextField(them ? "" : nz(plMocModel.getValueAt(idx, 5)), 24);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                them || "có".equals(String.valueOf(plMocModel.getValueAt(idx, 3))));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Mốc cần đạt:", fMoc);
        addRow(form, c, 1, "Thứ tự:", fThuTu);
        addRow(form, c, 2, "Ghi chú:", fGhiChu);
        c.gridx = 1;
        c.gridy = 3;
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm mốc" : "Sửa mốc", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.PhucLoiDAO.Moc m = new nro.repository.dao.PhucLoiDAO.Moc();
        m.id = them ? 0 : intOf(plMocModel.getValueAt(idx, 0));
        m.nhomId = nhom;
        m.bat = cbBat.isSelected();
        m.ghiChu = fGhiChu.getText().trim();
        try {
            m.moc = docSoNguyen(fMoc.getText(), "mốc");
            m.thuTu = docSoNguyen(fThuTu.getText(), "thứ tự");
        } catch (IllegalArgumentException ex) {
            note(WARN_RED, ex.getMessage());
            return;
        }
        String loi = nro.repository.dao.PhucLoiDAO.luuMoc(m);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        plLoadMoc();
        note(OK_GREEN, them ? "Đã thêm mốc" : "Đã sửa mốc");
    }

    private void plXoaMoc() {
        int id = plMocDangChon();
        if (id <= 0) {
            note(WARN_RED, "Chưa chọn mốc nào.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá mốc này cùng quà của nó?\n"
                + "Lượt đã nhận của người chơi cũng bị xoá theo.",
                "Xoá mốc", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.PhucLoiDAO.xoaMoc(id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        plLoadMoc();
        note(OK_GREEN, "Đã xoá mốc");
    }

    private void plSuaQua(boolean them) {
        int moc = plMocDangChon();
        if (moc <= 0) {
            note(WARN_RED, "Chọn mốc trước đã.");
            return;
        }
        int r = plQuaTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn quà nào để sửa.");
            return;
        }
        int idx = them ? -1 : plQuaTable.convertRowIndexToModel(r);
        JTextField fItem = new JTextField(them ? ""
                : String.valueOf(plQuaModel.getValueAt(idx, 2)), 10);
        JTextField fSl = new JTextField(them ? "1"
                : soNguyenSach(plQuaModel.getValueAt(idx, 4)), 10);
        JTextField fChiSo = new JTextField(them ? ""
                : nz(plQuaModel.getValueAt(idx, 5)), 24);
        JLabel lblTen = new JLabel("—");
        lblTen.setForeground(ACCENT);
        Runnable traTen = () -> {
            try {
                nro.entity.template.ItemTemplate t = nro.service.item.ItemService.gI()
                        .getTemplate(Integer.parseInt(fItem.getText().trim()));
                lblTen.setText(t != null ? t.name : "(không có id này)");
                lblTen.setForeground(t != null ? ACCENT : WARN_RED);
            } catch (Exception ex) {
                lblTen.setText("—");
            }
        };
        fItem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                traTen.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                traTen.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                traTen.run();
            }
        });
        traTen.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "ID vật phẩm:", fItem);
        c.gridx = 2;
        c.gridy = 0;
        form.add(button("Chọn...", GREY, e -> {
            int id = OptionPicker.chonVatPham(this, -1);
            if (id >= 0) {
                fItem.setText(String.valueOf(id));
            }
        }), c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        form.add(lblTen, c);
        c.gridwidth = 1;
        addRow(form, c, 2, "Số lượng:", fSl);
        addRow(form, c, 3, "Chỉ số:", fChiSo);
        javax.swing.JCheckBox cbKhoaQua = OptionPicker.oKhoa(
                OptionPicker.coKhoaBang(them ? "" : nz(plQuaModel.getValueAt(idx, 5))));
        c.gridx = 1;
        c.gridy = 4;
        form.add(cbKhoaQua, c);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 3;
        form.add(nhan("<html><span style='color:#777'>Chỉ số viết như đồ rơi boss: "
                + "<code>50=5,77=10</code> — để trống là không gắn gì.</span></html>"), c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm quà" : "Sửa quà", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.PhucLoiDAO.Qua q = new nro.repository.dao.PhucLoiDAO.Qua();
        q.id = them ? 0 : intOf(plQuaModel.getValueAt(idx, 0));
        q.mocId = moc;
        // O tich khoa ghi thang vao chuoi chi so, khong giu rieng mot cot:
        // cho phat qua chi doc mot cho, khong phai biet den o tich.
        q.chiSo = OptionPicker.datKhoaBang(fChiSo.getText().trim(),
                cbKhoaQua.isSelected());
        try {
            q.itemId = docSoNguyen(fItem.getText(), "id vật phẩm");
            q.soLuong = docSoNguyen(fSl.getText(), "số lượng");
        } catch (IllegalArgumentException ex) {
            note(WARN_RED, ex.getMessage());
            return;
        }
        String loi = nro.repository.dao.PhucLoiDAO.luuQua(q);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        plLoadMoc();
        note(OK_GREEN, them ? "Đã thêm quà" : "Đã sửa quà");
    }

    private void plXoaQua() {
        int r = plQuaTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn quà nào.");
            return;
        }
        int id = intOf(plQuaModel.getValueAt(
                plQuaTable.convertRowIndexToModel(r), 0));
        String loi = nro.repository.dao.PhucLoiDAO.xoaQua(id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        plLoadMoc();
        note(OK_GREEN, "Đã xoá quà");
    }

    /**
     * Tab Sự kiện — xem chú thích ở đầu khối bên trên.
     */
    private JComponent buildSuKienTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---------------- bảng sự kiện ----------------
        skTable.setRowHeight(24);
        skTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w1 = {40, 190, 100, 230, 70, 300, 200};
        for (int i = 0; i < skTable.getColumnCount() && i < w1.length; i++) {
            skTable.getColumnModel().getColumn(i).setPreferredWidth(w1[i]);
        }
        skTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                napBangRoi();
                napBangDoi();
            }
        });

        JPanel nutSk = new JPanel(new GridLayout(0, 5, 6, 6));
        nutSk.setOpaque(false);
        nutSk.add(button("Thêm sự kiện", OK_GREEN, e -> suaSuKienDialog(true)));
        nutSk.add(button("Sửa sự kiện", ACCENT, e -> suaSuKienDialog(false)));
        nutSk.add(button("Bật / Tắt", GREY, e -> doiBatSuKien()));
        nutSk.add(button("Xoá sự kiện", WARN_RED, e -> xoaSuKien()));
        nutSk.add(button("Tải lại tất cả", GREY, e -> napTatCaSuKien()));

        JPanel tren = new JPanel(new BorderLayout(0, 6));
        tren.setOpaque(false);
        tren.setBorder(titled("1. Sự kiện"));
        tren.add(ServerGuiUtils.cuon(skTable), BorderLayout.CENTER);
        tren.add(nutSk, BorderLayout.SOUTH);

        // ---------------- vật phẩm rơi ----------------
        roiTable.setRowHeight(24);
        roiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w2 = {40, 280, 80, 110, 120, 50};
        for (int i = 0; i < roiTable.getColumnCount() && i < w2.length; i++) {
            roiTable.getColumnModel().getColumn(i).setPreferredWidth(w2[i]);
        }
        JPanel nutRoi = new JPanel(new GridLayout(0, 4, 6, 6));
        nutRoi.setOpaque(false);
        nutRoi.add(button("Thêm vật phẩm rơi", OK_GREEN, e -> suaRoiDialog(true)));
        nutRoi.add(button("Sửa", ACCENT, e -> suaRoiDialog(false)));
        nutRoi.add(button("Xoá", WARN_RED, e -> xoaRoi()));
        nutRoi.add(button("Tải lại", GREY, e -> napBangRoi()));

        JPanel pRoi = new JPanel(new BorderLayout(0, 6));
        pRoi.setOpaque(false);
        pRoi.add(nhan("Những món này rơi từ MỌI quái, mỗi món một tỉ lệ riêng, "
                + "và CHỈ rơi khi sự kiện ở bảng trên đang chạy."), BorderLayout.NORTH);
        pRoi.add(ServerGuiUtils.cuon(roiTable), BorderLayout.CENTER);
        pRoi.add(nutRoi, BorderLayout.SOUTH);

        // ---------------- đổi ở Quy Lão ----------------
        doiTable.setRowHeight(24);
        doiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w3 = {40, 60, 260, 260, 200, 50};
        for (int i = 0; i < doiTable.getColumnCount() && i < w3.length; i++) {
            doiTable.getColumnModel().getColumn(i).setPreferredWidth(w3[i]);
        }
        JPanel nutDoi = new JPanel(new GridLayout(0, 4, 6, 6));
        nutDoi.setOpaque(false);
        nutDoi.add(button("Thêm mục đổi", OK_GREEN, e -> suaDoiDialog(true)));
        nutDoi.add(button("Sửa", ACCENT, e -> suaDoiDialog(false)));
        nutDoi.add(button("Xoá", WARN_RED, e -> xoaDoi()));
        nutDoi.add(button("Tải lại", GREY, e -> napBangDoi()));

        JPanel pDoi = new JPanel(new BorderLayout(0, 6));
        pDoi.setOpaque(false);
        pDoi.add(nhan("Mục \"Đổi quà sự kiện\" ở Quy Lão đảo Kamê. Sự kiện tắt "
                + "là mục biến mất khỏi menu, không để lại nút chết."),
                BorderLayout.NORTH);
        pDoi.add(ServerGuiUtils.cuon(doiTable), BorderLayout.CENTER);
        pDoi.add(nutDoi, BorderLayout.SOUTH);

        // ---------------- nội dung gói ----------------
        hopTable.setRowHeight(24);
        hopTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] w4 = {40, 240, 260, 90, 70, 80, 50};
        for (int i = 0; i < hopTable.getColumnCount() && i < w4.length; i++) {
            hopTable.getColumnModel().getColumn(i).setPreferredWidth(w4[i]);
        }
        cbHopDangXem.addActionListener(e -> napBangHop());

        JPanel nutHop = new JPanel(new GridLayout(0, 4, 6, 6));
        nutHop.setOpaque(false);
        nutHop.add(button("Thêm phần thưởng", OK_GREEN, e -> suaHopDialog(true)));
        nutHop.add(button("Sửa", ACCENT, e -> suaHopDialog(false)));
        nutHop.add(button("Xoá", WARN_RED, e -> xoaHop()));
        nutHop.add(button("Tải lại", GREY, e -> napComboHop()));

        JPanel hangHopChon = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hangHopChon.setOpaque(false);
        hangHopChon.add(new JLabel("Xem nội dung gói:"));
        hangHopChon.add(cbHopDangXem);

        JPanel pHop = new JPanel(new BorderLayout(0, 6));
        pHop.setOpaque(false);
        JPanel bacHop = new JPanel(new BorderLayout());
        bacHop.setOpaque(false);
        bacHop.add(nhan("Mỗi lần mở gói bốc ĐÚNG MỘT phần thưởng, theo trọng "
                + "số. Trọng số 30 so với 10 nghĩa là hay ra gấp ba, không "
                + "phải 30%."), BorderLayout.NORTH);
        bacHop.add(hangHopChon, BorderLayout.SOUTH);
        pHop.add(bacHop, BorderLayout.NORTH);
        pHop.add(ServerGuiUtils.cuon(hopTable), BorderLayout.CENTER);
        pHop.add(nutHop, BorderLayout.SOUTH);

        JTabbedPane duoi = new JTabbedPane();
        duoi.addTab("2. Vật phẩm rơi từ quái", pRoi);
        duoi.addTab("3. Đổi ở Quy Lão", pDoi);
        duoi.addTab("4. Nội dung gói", pHop);

        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tren, duoi);
        chia.setResizeWeight(0.42);
        chia.setBorder(null);

        lblSkTomTat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSkTomTat.setForeground(OK_GREEN);
        lblSkTomTat.setBorder(new EmptyBorder(4, 2, 4, 2));

        root.add(lblSkTomTat, BorderLayout.NORTH);
        root.add(chia, BorderLayout.CENTER);
        napTatCaSuKien();
        return root;
    }

    /** Nạp lại cả bốn bảng và đẩy thay đổi vào máy chủ đang chạy. */
    private void napTatCaSuKien() {
        nro.service.event.SuKienService.napLai();
        napBangSuKien();
        napComboHop();
    }

    private void napBangSuKien() {
        int idCu = idSuKienDangChon();
        skModel.setRowCount(0);
        for (nro.repository.dao.SuKienDAO.SuKien s
                : nro.repository.dao.SuKienDAO.danhSach()) {
            skModel.addRow(new Object[]{s.id, s.ten, s.moTaTrangThai(),
                s.moTaKhungGio(),
                s.phutNhac > 0 ? s.phutNhac + " phút" : "",
                s.thongBao, s.ghiChu});
        }
        chonLaiDongSuKien(idCu);
        lblSkTomTat.setText(nro.service.event.SuKienService.tomTat());
        napBangRoi();
        napBangDoi();
    }

    private void chonLaiDongSuKien(int id) {
        for (int r = 0; id >= 0 && r < skModel.getRowCount(); r++) {
            if (Integer.parseInt(String.valueOf(skModel.getValueAt(r, 0))) == id) {
                skTable.setRowSelectionInterval(r, r);
                return;
            }
        }
        if (skModel.getRowCount() > 0) {
            skTable.setRowSelectionInterval(0, 0);
        }
    }

    /** Id sự kiện đang chọn, hoặc {@code -1}. */
    private int idSuKienDangChon() {
        int r = skTable.getSelectedRow();
        if (r < 0 || r >= skModel.getRowCount()) {
            return -1;
        }
        return Integer.parseInt(String.valueOf(skModel.getValueAt(r, 0)));
    }

    private nro.repository.dao.SuKienDAO.SuKien suKienDangChon() {
        int id = idSuKienDangChon();
        if (id < 0) {
            note(WARN_RED, "Chọn một sự kiện ở bảng trên trước.");
            return null;
        }
        for (nro.repository.dao.SuKienDAO.SuKien s
                : nro.repository.dao.SuKienDAO.danhSach()) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }

    private void suaSuKienDialog(boolean them) {
        final nro.repository.dao.SuKienDAO.SuKien s;
        if (them) {
            s = new nro.repository.dao.SuKienDAO.SuKien();
        } else {
            s = suKienDangChon();
            if (s == null) {
                return;
            }
        }

        final JComboBox<String> cbTen = new JComboBox<>(
                nro.repository.dao.SuKienDAO.danhSachTen().toArray(new String[0]));
        cbTen.setEditable(true);
        cbTen.setSelectedItem(s.ten == null ? "" : s.ten);

        final JCheckBox cbBat = new JCheckBox("Bật sự kiện", s.bat);
        final ChonGio cgBatDau = new ChonGio(
                nro.repository.dao.SuKienDAO.gio(s.batDau));
        final ChonGio cgKetThuc = new ChonGio(
                nro.repository.dao.SuKienDAO.gio(s.ketThuc));
        final JTextField fThongBao = new JTextField(s.thongBao, 34);
        final JTextField fPhut = new JTextField(String.valueOf(s.phutNhac), 6);
        final JTextField fGhiChu = new JTextField(s.ghiChu, 34);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        int y = 0;

        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Tên sự kiện:"), g);
        g.gridx = 1;
        JPanel hangTen = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangTen.setOpaque(false);
        hangTen.add(cbTen);
        hangTen.add(button("Sửa danh sách…", GREY, ev -> {
            Object dangChon = cbTen.getSelectedItem();
            suaDanhSachTenSuKien(hangTen);
            cbTen.setModel(new javax.swing.DefaultComboBoxModel<>(
                    nro.repository.dao.SuKienDAO.danhSachTen().toArray(new String[0])));
            cbTen.setSelectedItem(dangChon);
        }));
        p.add(hangTen, g);

        g.gridx = 1;
        g.gridy = ++y;
        p.add(cbBat, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Bắt đầu:"), g);
        g.gridx = 1;
        JPanel hangBD = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangBD.setOpaque(false);
        hangBD.add(cgBatDau.panel);
        hangBD.add(nhan("bỏ tích \"Đặt\" là chạy ngay"));
        p.add(hangBD, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Kết thúc:"), g);
        g.gridx = 1;
        JPanel hangKT = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangKT.setOpaque(false);
        hangKT.add(cgKetThuc.panel);
        hangKT.add(nhan("bỏ tích \"Đặt\" là chạy đến khi tắt tay"));
        p.add(hangKT, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Câu loa:"), g);
        g.gridx = 1;
        p.add(fThongBao, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Nhắc lại mỗi:"), g);
        g.gridx = 1;
        JPanel hangPhut = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangPhut.setOpaque(false);
        hangPhut.add(fPhut);
        hangPhut.add(nhan("phút — 0 là chỉ loa một lần lúc mở"));
        p.add(hangPhut, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1;
        p.add(fGhiChu, g);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm sự kiện" : "Sửa sự kiện",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        Object ten = cbTen.getSelectedItem();
        s.ten = ten == null ? "" : String.valueOf(ten).trim();
        s.bat = cbBat.isSelected();
        s.thongBao = fThongBao.getText().trim();
        s.ghiChu = fGhiChu.getText().trim();

        try {
            s.batDau = nro.repository.dao.SuKienDAO.docGio(cgBatDau.giaTri());
            s.ketThuc = nro.repository.dao.SuKienDAO.docGio(cgKetThuc.giaTri());
            s.phutNhac = docSoNguyen(fPhut.getText(), "số phút nhắc lại");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Nhập chưa đúng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loi = them ? nro.repository.dao.SuKienDAO.them(s)
                : nro.repository.dao.SuKienDAO.sua(s);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napTatCaSuKien();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, (them ? "Đã thêm sự kiện " : "Đã lưu sự kiện ") + s.ten
                + (s.dangChay() ? " — đang chạy ngay bây giờ." : "."));
    }

    private void doiBatSuKien() {
        nro.repository.dao.SuKienDAO.SuKien s = suKienDangChon();
        if (s == null) {
            return;
        }
        String loi = nro.repository.dao.SuKienDAO.datBat(s.id, !s.bat);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napTatCaSuKien();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, (s.bat ? "Đã tắt sự kiện " : "Đã bật sự kiện ") + s.ten + ".");
    }

    private void xoaSuKien() {
        nro.repository.dao.SuKienDAO.SuKien s = suKienDangChon();
        if (s == null) {
            return;
        }
        int soRoi = nro.repository.dao.SuKienDAO.danhSachRoi(s.id).size();
        int soDoi = nro.repository.dao.SuKienDAO.danhSachDoi(s.id).size();
        if (JOptionPane.showConfirmDialog(this,
                "Xoá hẳn sự kiện \"" + s.ten + "\"?\n"
                + "Kèm theo " + soRoi + " vật phẩm rơi và " + soDoi + " mục đổi.",
                "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.SuKienDAO.xoa(s.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napTatCaSuKien();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã xoá sự kiện " + s.ten + ".");
    }

    // ------------------------------------------------ 2. vật phẩm rơi

    private void napBangRoi() {
        roiModel.setRowCount(0);
        int idSk = idSuKienDangChon();
        if (idSk < 0) {
            return;
        }
        for (nro.repository.dao.SuKienDAO.VatPhamRoi v
                : nro.repository.dao.SuKienDAO.danhSachRoi(idSk)) {
            roiModel.addRow(new Object[]{v.id,
                v.itemId + " — " + nro.repository.dao.SuKienDAO.tenVatPham(v.itemId),
                v.moTaSoLuong(), gonSo(v.tiLe) + " %", v.moTaMap(),
                v.bat ? "bật" : "tắt"});
        }
    }

    private nro.repository.dao.SuKienDAO.VatPhamRoi roiDangChon() {
        int r = roiTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng trong bảng vật phẩm rơi trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(roiModel.getValueAt(r, 0)));
        for (nro.repository.dao.SuKienDAO.VatPhamRoi v
                : nro.repository.dao.SuKienDAO.danhSachRoi(idSuKienDangChon())) {
            if (v.id == id) {
                return v;
            }
        }
        return null;
    }

    private void suaRoiDialog(boolean them) {
        int idSk = idSuKienDangChon();
        if (idSk < 0) {
            note(WARN_RED, "Chọn một sự kiện ở bảng trên trước.");
            return;
        }
        final nro.repository.dao.SuKienDAO.VatPhamRoi v;
        if (them) {
            v = new nro.repository.dao.SuKienDAO.VatPhamRoi();
            v.suKienId = idSk;
            v.itemId = -1;
        } else {
            v = roiDangChon();
            if (v == null) {
                return;
            }
        }

        final int[] itemId = {v.itemId};
        final JLabel lblItem = new JLabel(moTaVatPham(itemId[0]));
        JButton btnItem = button("Chọn vật phẩm…", ACCENT, e -> {
            int moi = nro.ui.OptionPicker.chonVatPham(this, itemId[0]);
            if (moi >= 0) {
                itemId[0] = moi;
                lblItem.setText(moTaVatPham(moi));
            }
        });

        final JTextField fMin = new JTextField(String.valueOf(v.soLuongMin), 5);
        final JTextField fMax = new JTextField(String.valueOf(v.soLuongMax), 5);
        final JTextField fTiLe = new JTextField(gonSo(v.tiLe), 8);
        final JCheckBox cbBat = new JCheckBox("Bật", v.bat);

        // Pham vi ban do rieng cua RIENG mon nay, khong dung chung.
        final int[] kieuMap = {v.kieuMap};
        final String[] dsMap = {v.dsMap == null ? "" : v.dsMap};
        final JLabel lblMap = new JLabel();
        final Runnable veMap = () -> {
            nro.repository.dao.SuKienDAO.VatPhamRoi xem
                    = new nro.repository.dao.SuKienDAO.VatPhamRoi();
            xem.kieuMap = kieuMap[0];
            xem.dsMap = dsMap[0];
            lblMap.setText(xem.moTaMap()
                    + (kieuMap[0] == 0 ? "" : " — khớp " + soMapKhop(
                            java.util.Arrays.asList(dsMap[0].split(","))) + " bản đồ"));
        };
        veMap.run();
        JButton btnMap = button("Chọn phạm vi…", ACCENT, e -> {
            String kq = chonPhamViMap(this, kieuMap[0], dsMap[0]);
            if (kq != null) {
                int vach = kq.indexOf('|');
                kieuMap[0] = Integer.parseInt(kq.substring(0, vach));
                dsMap[0] = kq.substring(vach + 1);
                veMap.run();
            }
        });

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        int y = 0;

        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Vật phẩm:"), g);
        g.gridx = 1;
        JPanel hangItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangItem.setOpaque(false);
        hangItem.add(btnItem);
        hangItem.add(lblItem);
        p.add(hangItem, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Số lượng:"), g);
        g.gridx = 1;
        JPanel hangSl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangSl.setOpaque(false);
        hangSl.add(fMin);
        hangSl.add(new JLabel(" đến "));
        hangSl.add(fMax);
        hangSl.add(nhan("bốc đều trong khoảng"));
        p.add(hangSl, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Tỉ lệ rơi:"), g);
        g.gridx = 1;
        JPanel hangTl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangTl.setOpaque(false);
        hangTl.add(fTiLe);
        hangTl.add(nhan("% cho MỖI con quái — 1 nghĩa là trung bình 100 quái ra 1 món"));
        p.add(hangTl, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Bản đồ:"), g);
        g.gridx = 1;
        JPanel hangMap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangMap.setOpaque(false);
        hangMap.add(btnMap);
        hangMap.add(lblMap);
        p.add(hangMap, g);

        g.gridx = 1;
        g.gridy = ++y;
        p.add(cbBat, g);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm vật phẩm rơi" : "Sửa vật phẩm rơi",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        v.itemId = itemId[0];
        v.bat = cbBat.isSelected();
        v.kieuMap = kieuMap[0];
        v.dsMap = dsMap[0];
        try {
            v.soLuongMin = docSoNguyen(fMin.getText(), "số lượng tối thiểu");
            v.soLuongMax = docSoNguyen(fMax.getText(), "số lượng tối đa");
            v.tiLe = docSoThuc(fTiLe.getText(), "tỉ lệ rơi");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Nhập chưa đúng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loi = nro.repository.dao.SuKienDAO.luuRoi(v, them);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napTatCaSuKien();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã lưu vật phẩm rơi: "
                + nro.repository.dao.SuKienDAO.tenVatPham(v.itemId)
                + " — " + gonSo(v.tiLe) + "% mỗi quái.");
    }

    private void xoaRoi() {
        nro.repository.dao.SuKienDAO.VatPhamRoi v = roiDangChon();
        if (v == null) {
            return;
        }
        String loi = nro.repository.dao.SuKienDAO.xoaRoi(v.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napTatCaSuKien();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã xoá một vật phẩm rơi.");
    }

    // ------------------------------------------------ 3. đổi ở Quy Lão

    private void napBangDoi() {
        doiModel.setRowCount(0);
        int idSk = idSuKienDangChon();
        if (idSk < 0) {
            return;
        }
        for (nro.repository.dao.SuKienDAO.CongThucDoi c
                : nro.repository.dao.SuKienDAO.danhSachDoi(idSk)) {
            doiModel.addRow(new Object[]{c.id, c.thuTu,
                moTaCap(c.chuoiNguyenLieu()),
                c.soLuongNhan + " × " + nro.repository.dao.SuKienDAO.tenVatPham(c.itemNhan),
                c.ten, c.bat ? "bật" : "tắt"});
        }
    }

    private nro.repository.dao.SuKienDAO.CongThucDoi doiDangChon() {
        int r = doiTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một mục đổi trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(doiModel.getValueAt(r, 0)));
        for (nro.repository.dao.SuKienDAO.CongThucDoi c
                : nro.repository.dao.SuKienDAO.danhSachDoi(idSuKienDangChon())) {
            if (c.id == id) {
                return c;
            }
        }
        return null;
    }

    /**
     * Thêm hoặc sửa một mục đổi ở Quy Lão.
     *
     * <p>Ô "Cần có" giữ chuỗi {@code id:sl,id:sl} nhưng người dùng không phải
     * nhìn thấy nó: ô để {@code setEditable(false)}, bên cạnh là dòng chữ dịch
     * ra tên vật phẩm, và bấm "Chọn…" thì mở đúng cái bảng chọn nhiều món đã
     * dùng cho công thức của Bà Hạt Mít.</p>
     */
    private void suaDoiDialog(boolean them) {
        int idSk = idSuKienDangChon();
        if (idSk < 0) {
            note(WARN_RED, "Chọn một sự kiện ở bảng trên trước.");
            return;
        }
        final nro.repository.dao.SuKienDAO.CongThucDoi c;
        if (them) {
            c = new nro.repository.dao.SuKienDAO.CongThucDoi();
            c.suKienId = idSk;
            c.thuTu = doiModel.getRowCount();
            c.itemNhan = -1;
        } else {
            c = doiDangChon();
            if (c == null) {
                return;
            }
        }

        // ------------- cần có: nhiều món, mỗi món một số lượng -------------
        final JTextField fCan = new JTextField(c.chuoiNguyenLieu(), 22);
        final JLabel xemCan = new JLabel();
        Runnable veLaiCan = () -> {
            String mo = moTaCap(fCan.getText());
            xemCan.setText(mo.isEmpty() ? "chưa chọn món nào" : mo);
        };
        veLaiCan.run();

        // ------------- nhận về: vẫn đúng một món -------------
        final int[] idNhan = {c.itemNhan};
        final JLabel lblNhan = new JLabel(moTaVatPham(idNhan[0]));
        JButton btnNhan = button("Chọn…", ACCENT, e -> {
            int moi = nro.ui.OptionPicker.chonVatPham(this, idNhan[0]);
            if (moi >= 0) {
                idNhan[0] = moi;
                lblNhan.setText(moTaVatPham(moi));
            }
        });

        final JTextField fSlNhan = new JTextField(String.valueOf(c.soLuongNhan), 5);
        final JTextField fTen = new JTextField(c.ten, 24);
        final JTextField fThuTu = new JTextField(String.valueOf(c.thuTu), 5);
        final JCheckBox cbBat = new JCheckBox("Bật", c.bat);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        int y = 0;

        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Cần có:"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(oChon(fCan, xemCan, veLaiCan,
                () -> suaCapVatPham(p, fCan.getText(), "Nguyên liệu cần có")), g);
        g.fill = GridBagConstraints.NONE;

        g.gridx = 1;
        g.gridy = ++y;
        xemCan.setForeground(GREY);
        p.add(xemCan, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Nhận về:"), g);
        g.gridx = 1;
        JPanel hangNhan = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangNhan.setOpaque(false);
        hangNhan.add(fSlNhan);
        hangNhan.add(new JLabel(" × "));
        hangNhan.add(btnNhan);
        hangNhan.add(lblNhan);
        p.add(hangNhan, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Tên mục:"), g);
        g.gridx = 1;
        JPanel hangTen = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangTen.setOpaque(false);
        hangTen.add(fTen);
        hangTen.add(nhan("để trống thì tự sinh từ số lượng"));
        p.add(hangTen, g);

        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Thứ tự:"), g);
        g.gridx = 1;
        p.add(fThuTu, g);

        g.gridx = 1;
        g.gridy = ++y;
        p.add(cbBat, g);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm mục đổi ở Quy Lão" : "Sửa mục đổi",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        c.nguyenLieu.clear();
        for (String phan : fCan.getText().trim().split(",")) {
            String[] cap = phan.trim().split(":");
            if (cap.length != 2) {
                continue;
            }
            try {
                c.nguyenLieu.add(new int[]{Integer.parseInt(cap[0].trim()),
                    Integer.parseInt(cap[1].trim())});
            } catch (NumberFormatException ignored) {
                // Bang chon sinh ra chuoi nay nen khong the sai, nhung neu co
                // thi bo qua con hon vut ca muc doi.
            }
        }
        c.itemNhan = idNhan[0];
        c.ten = fTen.getText().trim();
        c.bat = cbBat.isSelected();
        try {
            c.soLuongNhan = docSoNguyen(fSlNhan.getText(), "số lượng nhận");
            c.thuTu = docSoNguyen(fThuTu.getText(), "thứ tự");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Nhập chưa đúng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loi = nro.repository.dao.SuKienDAO.luuDoi(c, them);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napTatCaSuKien();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã lưu mục đổi ở Quy Lão.");
    }

    private void xoaDoi() {
        nro.repository.dao.SuKienDAO.CongThucDoi c = doiDangChon();
        if (c == null) {
            return;
        }
        String loi = nro.repository.dao.SuKienDAO.xoaDoi(c.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napTatCaSuKien();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã xoá một mục đổi.");
    }

    // ------------------------------------------------ 4. nội dung gói

    /** Nạp danh sách gói vào ô chọn, giữ nguyên gói đang xem nếu còn. */
    private void napComboHop() {
        Object cu = cbHopDangXem.getSelectedItem();
        cbHopDangXem.removeAllItems();
        for (Integer id : nro.repository.dao.SuKienDAO.cacHop()) {
            cbHopDangXem.addItem(id + " — "
                    + nro.repository.dao.SuKienDAO.tenVatPham(id));
        }
        if (cu != null) {
            cbHopDangXem.setSelectedItem(cu);
        }
        if (cbHopDangXem.getSelectedIndex() < 0 && cbHopDangXem.getItemCount() > 0) {
            cbHopDangXem.setSelectedIndex(0);
        }
        napBangHop();
    }

    /** Id gói đang xem, hoặc {@code -1} khi chưa có gói nào. */
    private int idHopDangXem() {
        Object v = cbHopDangXem.getSelectedItem();
        if (v == null) {
            return -1;
        }
        String s = String.valueOf(v);
        int cach = s.indexOf(' ');
        try {
            return Integer.parseInt(cach < 0 ? s : s.substring(0, cach));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void napBangHop() {
        hopModel.setRowCount(0);
        int idHop = idHopDangXem();
        if (idHop < 0) {
            return;
        }
        java.util.List<nro.repository.dao.SuKienDAO.PhanThuongHop> ds
                = nro.repository.dao.SuKienDAO.danhSachHop(idHop);
        long tong = 0;
        for (nro.repository.dao.SuKienDAO.PhanThuongHop p : ds) {
            if (p.bat && p.trongSo > 0) {
                tong += p.trongSo;
            }
        }
        for (nro.repository.dao.SuKienDAO.PhanThuongHop p : ds) {
            String tiLe = p.bat && tong > 0
                    ? String.format(java.util.Locale.US, "%.2f %%",
                            p.trongSo * 100d / tong) : "";
            hopModel.addRow(new Object[]{p.id,
                nro.repository.dao.SuKienDAO.tenVatPham(p.hopItemId),
                p.itemId + " — " + nro.repository.dao.SuKienDAO.tenVatPham(p.itemId),
                p.moTaSoLuong(), p.trongSo, tiLe, p.bat ? "bật" : "tắt"});
        }
    }

    private nro.repository.dao.SuKienDAO.PhanThuongHop hopDangChon() {
        int r = hopTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một phần thưởng trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(hopModel.getValueAt(r, 0)));
        for (nro.repository.dao.SuKienDAO.PhanThuongHop p
                : nro.repository.dao.SuKienDAO.danhSachHop(idHopDangXem())) {
            if (p.id == id) {
                return p;
            }
        }
        return null;
    }

    private void suaHopDialog(boolean them) {
        final nro.repository.dao.SuKienDAO.PhanThuongHop p;
        if (them) {
            p = new nro.repository.dao.SuKienDAO.PhanThuongHop();
            // Goi dang xem lam mac dinh, de them lien tiep nhieu phan thuong
            // cho cung mot goi khong phai chon lai moi lan.
            p.hopItemId = idHopDangXem();
            p.itemId = -1;
        } else {
            p = hopDangChon();
            if (p == null) {
                return;
            }
        }

        final int[] idHop = {p.hopItemId};
        final int[] idThuong = {p.itemId};
        final JLabel lblHop = new JLabel(moTaVatPham(idHop[0]));
        final JLabel lblThuong = new JLabel(moTaVatPham(idThuong[0]));
        JButton btnHop = button("Chọn…", ACCENT, e -> {
            int moi = nro.ui.OptionPicker.chonVatPham(this, idHop[0]);
            if (moi >= 0) {
                idHop[0] = moi;
                lblHop.setText(moTaVatPham(moi));
            }
        });
        JButton btnThuong = button("Chọn…", ACCENT, e -> {
            int moi = nro.ui.OptionPicker.chonVatPham(this, idThuong[0]);
            if (moi >= 0) {
                idThuong[0] = moi;
                lblThuong.setText(moTaVatPham(moi));
            }
        });

        final JTextField fMin = new JTextField(String.valueOf(p.soLuongMin), 5);
        final JTextField fMax = new JTextField(String.valueOf(p.soLuongMax), 5);
        final JTextField fTrongSo = new JTextField(String.valueOf(p.trongSo), 6);
        final JCheckBox cbBat = new JCheckBox("Bật", p.bat);

        JPanel pn = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        int y = 0;

        g.gridx = 0;
        g.gridy = y;
        pn.add(new JLabel("Gói:"), g);
        g.gridx = 1;
        JPanel hangHop = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangHop.setOpaque(false);
        hangHop.add(btnHop);
        hangHop.add(lblHop);
        pn.add(hangHop, g);

        g.gridx = 0;
        g.gridy = ++y;
        pn.add(new JLabel("Phần thưởng:"), g);
        g.gridx = 1;
        JPanel hangThuong = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangThuong.setOpaque(false);
        hangThuong.add(btnThuong);
        hangThuong.add(lblThuong);
        pn.add(hangThuong, g);

        g.gridx = 0;
        g.gridy = ++y;
        pn.add(new JLabel("Số lượng:"), g);
        g.gridx = 1;
        JPanel hangSl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangSl.setOpaque(false);
        hangSl.add(fMin);
        hangSl.add(new JLabel(" đến "));
        hangSl.add(fMax);
        pn.add(hangSl, g);

        g.gridx = 0;
        g.gridy = ++y;
        pn.add(new JLabel("Trọng số:"), g);
        g.gridx = 1;
        JPanel hangTs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangTs.setOpaque(false);
        hangTs.add(fTrongSo);
        hangTs.add(nhan("so với các phần thưởng khác cùng gói — 30 với 10 là hay ra gấp ba"));
        pn.add(hangTs, g);

        g.gridx = 1;
        g.gridy = ++y;
        pn.add(cbBat, g);

        if (JOptionPane.showConfirmDialog(this, pn,
                them ? "Thêm phần thưởng vào gói" : "Sửa phần thưởng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        p.hopItemId = idHop[0];
        p.itemId = idThuong[0];
        p.bat = cbBat.isSelected();
        try {
            p.soLuongMin = docSoNguyen(fMin.getText(), "số lượng tối thiểu");
            p.soLuongMax = docSoNguyen(fMax.getText(), "số lượng tối đa");
            p.trongSo = docSoNguyen(fTrongSo.getText(), "trọng số");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Nhập chưa đúng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loi = nro.repository.dao.SuKienDAO.luuHop(p, them);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        nro.service.event.SuKienService.napLai();
        String chon = p.hopItemId + " — "
                + nro.repository.dao.SuKienDAO.tenVatPham(p.hopItemId);
        napComboHop();
        cbHopDangXem.setSelectedItem(chon);
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã lưu phần thưởng trong gói "
                + nro.repository.dao.SuKienDAO.tenVatPham(p.hopItemId) + ".");
    }

    private void xoaHop() {
        nro.repository.dao.SuKienDAO.PhanThuongHop p = hopDangChon();
        if (p == null) {
            return;
        }
        String loi = nro.repository.dao.SuKienDAO.xoaHop(p.id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        nro.service.event.SuKienService.napLai();
        napComboHop();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã xoá một phần thưởng khỏi gói.");
    }

    // ------------------------------------------------ dùng chung

    private static String moTaVatPham(int id) {
        return id < 0 ? "(chưa chọn)"
                : id + " — " + nro.repository.dao.SuKienDAO.tenVatPham(id);
    }

    private static String gonSo(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v)
                : String.format(java.util.Locale.US, "%.2f", v);
    }

    /**
     * Đọc số nguyên, bỏ qua dấu phân nhóm và ký hiệu đơn vị.
     *
     * <p>Nhiều ô nhập được đổ sẵn bằng chuỗi <b>đã định dạng</b> lấy từ bảng —
     * "1.000" cho số lượng, "50%" cho tỉ lệ. Bản cũ gọi thẳng
     * {@code Integer.parseInt} nên mở hộp sửa rồi bấm Lưu là báo "không phải số
     * nguyên", dù người dùng chưa gõ gì; phải tự xoá dấu chấm hay dấu phần trăm
     * mới lưu được.</p>
     */
    private static int docSoNguyen(String raw, String ten) {
        String v = raw == null ? "" : raw.trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("Chưa nhập " + ten + ".");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length(); i++) {
            char ch = v.charAt(i);
            if (Character.isDigit(ch) || (ch == '-' && sb.length() == 0)) {
                sb.append(ch);
            }
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ten + " không phải số nguyên.");
        }
    }

    /**
     * Đọc số thực, nhận cả dấu phẩy thập phân kiểu Việt và dấu phân nhóm.
     *
     * <p>Có cả chấm lẫn phẩy ("1.234,5") thì chấm là phân nhóm, phẩy là thập
     * phân. Chỉ có phẩy thì phẩy là thập phân. Chỉ có chấm thì coi là thập phân
     * — người ta gõ tỉ lệ hiếm kiểu "0.05" nhiều hơn là gõ số nghìn có phân
     * nhóm vào ô này.</p>
     */
    private static double docSoThuc(String raw, String ten) {
        String v = raw == null ? "" : raw.trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("Chưa nhập " + ten + ".");
        }
        if (v.indexOf('.') >= 0 && v.indexOf(',') >= 0) {
            v = v.replace(".", "");
        }
        v = v.replace(",", ".");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length(); i++) {
            char ch = v.charAt(i);
            if (Character.isDigit(ch) || ch == '.'
                    || (ch == '-' && sb.length() == 0)) {
                sb.append(ch);
            }
        }
        try {
            return Double.parseDouble(sb.toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ten + " không phải số.");
        }
    }

    /**
     * O chon ngay gio cho su kien, thay cho o go tay "dd/MM/yyyy HH:mm".
     *
     * <p>Dung {@link javax.swing.JSpinner} co san cua Swing nen khong them thu
     * vien ngoai. O tich "Dat" quyet dinh co dat gio hay khong: bo tich la tra
     * ve chuoi rong — dung nghia cu (bat dau trong = chay ngay, ket thuc trong
     * = chay den khi tat tay), nen phia luu khong phai doi gi.</p>
     */
    private static final class ChonGio {

        private static final String MAU = "dd/MM/yyyy HH:mm";

        final javax.swing.JCheckBox bat = new javax.swing.JCheckBox("Đặt");
        final javax.swing.JSpinner spin =
                new javax.swing.JSpinner(new javax.swing.SpinnerDateModel());
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        ChonGio(String banDau) {
            spin.setEditor(new javax.swing.JSpinner.DateEditor(spin, MAU));
            boolean co = banDau != null && !banDau.trim().isEmpty();
            if (co) {
                try {
                    spin.setValue(new java.text.SimpleDateFormat(MAU).parse(banDau.trim()));
                } catch (java.text.ParseException ignored) {
                    // Du lieu cu sai dinh dang -> giu gio hien tai, khong lam vo hop thoai.
                }
            }
            bat.setSelected(co);
            spin.setEnabled(co);
            bat.addActionListener(e -> spin.setEnabled(bat.isSelected()));
            panel.setOpaque(false);
            panel.add(bat);
            panel.add(spin);
        }

        /** Chuoi dd/MM/yyyy HH:mm, hoac rong neu khong dat gio. */
        String giaTri() {
            if (!bat.isSelected()) {
                return "";
            }
            return new java.text.SimpleDateFormat(MAU)
                    .format((java.util.Date) spin.getValue());
        }
    }

    /**
     * Hop thoai soan CHI SO NGAU NHIEN cho mot mon roi cua boss.
     *
     * <p>Tra ve chuoi dang {@code id:min:max,id:min:max} de luu vao cot
     * {@code options} cua bang {@code boss_drop}. Tra ve {@code null} neu nguoi
     * dung bam Huy — cho goi giu nguyen gia tri cu.</p>
     */
    private static String suaChiSoNgauNhien(java.awt.Component cha, String hienTai) {
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Chỉ số", "Tên chỉ số", "Từ", "Đến"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3;
            }
        };
        if (hienTai != null && !hienTai.trim().isEmpty()
                && !"[]".equals(hienTai.trim())) {
            for (String phan : hienTai.trim().split(",")) {
                String[] p = phan.trim().split(":");
                if (p.length != 3) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(p[0].trim());
                    m.addRow(new Object[]{id, OptionPicker.tenCua(id),
                        Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim())});
                } catch (NumberFormatException ignored) {
                    // Dong hong trong CSDL -> bo qua, khong lam vo hop thoai.
                }
            }
        }

        JTable bang = new JTable(m);
        bang.setRowHeight(24);
        JScrollPane sc = ServerGuiUtils.cuon(bang);
        sc.setPreferredSize(new Dimension(460, 220));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.add(button("Thêm chỉ số", OK_GREEN, e -> {
            int id = OptionPicker.chonChiSo(bang, -1);
            if (id >= 0) {
                m.addRow(new Object[]{id, OptionPicker.tenCua(id), 1, 1});
            }
        }));
        nut.add(button("Xoá dòng", WARN_RED, e -> {
            int r = bang.getSelectedRow();
            if (r >= 0) {
                m.removeRow(r);
            }
        }));

        // O tich "Do khoa" nam ngay trong hop chi so dung chung, nen MOI cho goi
        // hop nay deu co san lua chon do — do roi tu boss, do roi tu quai, va
        // bat ky hop nao sau nay dung lai no.
        javax.swing.JCheckBox cbKhoa = OptionPicker.oKhoa(
                OptionPicker.coKhoaKhoang(hienTai));
        // Chi so khoa da co o tich rieng -> khong hien lai trong bang, tranh sua
        // hai noi ma lech nhau.
        for (int r = m.getRowCount() - 1; r >= 0; r--) {
            if (intOf(m.getValueAt(r, 0)) == OptionPicker.CHI_SO_KHOA) {
                m.removeRow(r);
            }
        }
        nut.add(cbKhoa);

        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.add(new JLabel("Mỗi dòng là một chỉ số; giá trị bốc ngẫu nhiên từ \"Từ\" đến \"Đến\"."),
                BorderLayout.NORTH);

        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(nut, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(cha, wrap, "Chỉ số ngẫu nhiên",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        if (bang.isEditing()) {
            bang.getCellEditor().stopCellEditing();
        }
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < m.getRowCount(); r++) {
            int id = intOf(m.getValueAt(r, 0));
            int min = intOf(m.getValueAt(r, 2));
            int max = intOf(m.getValueAt(r, 3));
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(id).append(":").append(min).append(":").append(max);
        }
        return OptionPicker.datKhoaKhoang(sb.toString(), cbKhoa.isSelected());
    }

    /**
     * Popup them / sua / xoa TEN su kien goi y.
     *
     * <p>Danh sach nay truoc day la mang cung trong ma nguon, muon them mot ten
     * la phai sua code roi build lai. Nay luu o bang {@code su_kien_ten}.</p>
     */
    private void suaDanhSachTenSuKien(java.awt.Component cha) {
        javax.swing.DefaultListModel<String> model = new javax.swing.DefaultListModel<>();
        for (String t : nro.repository.dao.SuKienDAO.danhSachTen()) {
            model.addElement(t);
        }
        javax.swing.JList<String> ds = new javax.swing.JList<>(model);
        JScrollPane sc = ServerGuiUtils.cuon(ds);
        sc.setPreferredSize(new Dimension(320, 260));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.add(button("Thêm", OK_GREEN, e -> {
            String t = JOptionPane.showInputDialog(ds, "Tên sự kiện mới:");
            if (t == null || t.trim().isEmpty()) {
                return;
            }
            String loi = nro.repository.dao.SuKienDAO.themTen(t);
            if (loi != null) {
                JOptionPane.showMessageDialog(ds, loi);
                return;
            }
            if (!model.contains(t.trim())) {
                model.addElement(t.trim());
            }
        }));
        nut.add(button("Sửa", ACCENT, e -> {
            String cu = ds.getSelectedValue();
            if (cu == null) {
                return;
            }
            String moi = (String) JOptionPane.showInputDialog(ds, "Tên mới:",
                    "Sửa tên", JOptionPane.PLAIN_MESSAGE, null, null, cu);
            if (moi == null || moi.trim().isEmpty() || moi.trim().equals(cu)) {
                return;
            }
            String loi = nro.repository.dao.SuKienDAO.suaTen(cu, moi);
            if (loi != null) {
                JOptionPane.showMessageDialog(ds, loi);
                return;
            }
            model.set(ds.getSelectedIndex(), moi.trim());
        }));
        nut.add(button("Xoá", WARN_RED, e -> {
            String t = ds.getSelectedValue();
            if (t == null) {
                return;
            }
            if (JOptionPane.showConfirmDialog(ds, "Xoá tên \"" + t + "\" khỏi danh sách gợi ý?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            nro.repository.dao.SuKienDAO.xoaTen(t);
            model.removeElement(t);
        }));

        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.add(new JLabel("Danh sách tên gợi ý — sự kiện đã tạo không bị ảnh hưởng."),
                BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(nut, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(cha, wrap, "Tên sự kiện",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Dòng ghi chú xám ở đầu một tab.
     *
     * <h3>Luôn bọc trong {@code <html>}</h3>
     *
     * <p>{@code JLabel} chỉ hiểu thẻ HTML khi chuỗi <b>bắt đầu</b> bằng
     * {@code <html>}; thiếu nó thì {@code <b>bể chỉ số</b>} hiện ra nguyên văn cả
     * dấu ngoặc. Bọc sẵn ở đây để chỗ gọi khỏi phải nhớ.</p>
     *
     * <p>Kèm {@code width} nên câu dài tự xuống dòng thay vì kéo cả tab rộng ra
     * quá màn hình.</p>
     */
    private JLabel nhan(String s) {
        String noiDung = (s != null && s.trim().toLowerCase().startsWith("<html"))
                ? s : ("<html><body style='width:900px'>" + s + "</body></html>");
        JLabel l = new JLabel(noiDung);
        l.setForeground(GREY);
        l.setBorder(new EmptyBorder(0, 2, 4, 2));
        return l;
    }

    private void napBangAura() {
        nro.repository.dao.AuraDAO.napLai();
        auraModel.setRowCount(0);
        for (nro.repository.dao.AuraDAO.Aura a
                : nro.repository.dao.AuraDAO.tatCa()) {
            auraModel.addRow(new Object[]{a.id, a.ten,
                a.choCaiTrang ? "có" : "",
                nro.repository.dao.AuraDAO.soCaiTrangDangGan(a.id),
                a.ghiChu});
        }
        auraCtModel.setRowCount(0);
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT i.id, i.NAME, i.aura_id, a.ten FROM item_template i"
                    + " LEFT JOIN aura a ON a.id = i.aura_id"
                    + " WHERE i.aura_id <> -1 ORDER BY i.id");
            while (rs.next()) {
                auraCtModel.addRow(new Object[]{rs.getInt("id"), rs.getString("NAME"),
                    rs.getInt("aura_id") + " — "
                            + (rs.getString("ten") == null
                               ? "(KHÔNG có trong bảng aura)" : rs.getString("ten"))});
            }
            rs.dispose();
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Lỗi nạp bảng cải trang gán hào quang");
        }
        napBangAuraNguoiChoi();
    }

    private Integer auraDangChon() {
        int r = auraTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn hào quang nào.");
            return null;
        }
        return Integer.parseInt(String.valueOf(auraModel.getValueAt(
                auraTable.convertRowIndexToModel(r), 0)));
    }

    private void suaAuraDialog(boolean them) {
        Integer idCu = null;
        String tenCu = "";
        boolean choCtCu = false;
        String ghiChuCu = "";
        if (!them) {
            idCu = auraDangChon();
            if (idCu == null) {
                return;
            }
            nro.repository.dao.AuraDAO.Aura a =
                    nro.repository.dao.AuraDAO.theoId(idCu);
            if (a != null) {
                tenCu = a.ten;
                choCtCu = a.choCaiTrang;
                ghiChuCu = a.ghiChu;
            }
        }
        JTextField fId = new JTextField(idCu == null ? "" : String.valueOf(idCu), 8);
        fId.setEditable(them);
        JTextField fTen = new JTextField(tenCu, 26);
        JTextField fGhiChu = new JTextField(ghiChuCu, 30);
        javax.swing.JCheckBox cbCt = new javax.swing.JCheckBox(
                "Chỉ dùng cho cải trang (người chơi không tự chọn được)", choCtCu);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Id hào quang:", fId);
        addRow(form, c, 1, "Tên:", fTen);
        addRow(form, c, 2, "Ghi chú:", fGhiChu);
        c.gridx = 1;
        c.gridy = 3;
        form.add(cbCt, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "Id phải trong <b>0..127</b> — client đọc id này bằng một byte "
                + "có dấu.<br>"
                + "Ảnh hào quang nằm ở <code>data/img_by_name/x1..x4/"
                + "aura_&lt;id&gt;_0.png</code> và phải có dòng tương ứng trong "
                + "bảng <code>img_by_name</code>; thiếu một trong hai là client "
                + "không vẽ gì.</span></html>"), c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                them ? "Thêm hào quang" : "Sửa hào quang", true);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button("Lưu", OK_GREEN, e -> {
            int id;
            try {
                id = Integer.parseInt(fId.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Id phải là số nguyên.");
                return;
            }
            String loi = nro.repository.dao.AuraDAO.luu(id, fTen.getText(),
                    cbCt.isSelected(), fGhiChu.getText());
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không lưu được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            napBangAura();
            note(OK_GREEN, "Đã lưu hào quang " + id + ".");
        }));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(form, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void xoaAura() {
        Integer id = auraDangChon();
        if (id == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xoá hào quang " + id + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.AuraDAO.xoa(id);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không xoá được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangAura();
        note(OK_GREEN, "Đã xoá hào quang " + id + ".");
    }

    private void ganAuraDialog() {
        java.util.List<Object[]> ct = new java.util.ArrayList<>();
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT id, NAME FROM item_template WHERE TYPE = 5"
                    + " AND NOT (head = -1 AND body = -1 AND leg = -1)"
                    + " ORDER BY NAME");
            while (rs.next()) {
                ct.add(new Object[]{rs.getInt("id"), rs.getString("NAME")});
            }
            rs.dispose();
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Lỗi đọc danh sách cải trang");
        }
        if (ct.isEmpty()) {
            note(WARN_RED, "Không đọc được danh sách cải trang.");
            return;
        }
        // Hon 500 cai trang, hang chuc hao quang: cuon tay trong o chon la
        // khong tim noi. Ca HAI o deu co o tim rieng.
        final JComboBox<String> cbCt = new JComboBox<>();
        final JTextField fTimCt = new JTextField(24);
        final java.util.List<Object[]> dangHien = new java.util.ArrayList<>();
        ganOTim(fTimCt, cbCt, ct, dangHien, x -> x[0] + " — " + x[1]);
        // Nguon cua o hao quang: muc dau la "go", roi den tung hao quang.
        // Dung null lam dau cho muc "go" de doc ket qua khong phai dem chi so.
        final java.util.List<nro.repository.dao.AuraDAO.Aura> nguonAura =
                new java.util.ArrayList<>();
        nguonAura.add(null);
        nguonAura.addAll(nro.repository.dao.AuraDAO.tatCa());
        final JComboBox<String> cbAura = new JComboBox<>();
        final JTextField fTimAura = new JTextField(24);
        final java.util.List<nro.repository.dao.AuraDAO.Aura> auraHien =
                new java.util.ArrayList<>();
        ganOTim(fTimAura, cbAura, nguonAura, auraHien,
                a -> a == null ? "-1 — (gỡ hào quang)"
                        : a.id + " — " + a.ten
                          + (a.choCaiTrang ? "" : "   (đang cho tự chọn)"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Tìm cải trang:"), c);
        c.gridx = 1;
        form.add(fTimCt, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Cải trang:"), c);
        c.gridx = 1;
        form.add(cbCt, c);
        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Tìm hào quang:"), c);
        c.gridx = 1;
        form.add(fTimAura, c);
        c.gridx = 0;
        c.gridy = 3;
        form.add(new JLabel("Hào quang:"), c);
        c.gridx = 1;
        form.add(cbAura, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style='color:#777'>"
                + "Hai ô <b>Tìm</b> lọc theo tên hoặc id — danh sách cải trang "
                + "có hơn 500 mục.<br>"
                + "Mặc cải trang này là hào quang hiện sau lưng ngay.<br>"
                + "Hào quang <b>đang cho tự chọn</b> mà gán vào cải trang thì vẫn "
                + "chạy, nhưng nên bật cờ <b>Chỉ cho cải trang</b> để người chơi "
                + "không chọn suông được.</span></html>"), c);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dlg = new JDialog((java.awt.Frame) owner,
                "Gán hào quang cho cải trang", true);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(button("Huỷ", GREY, e -> dlg.dispose()));
        south.add(button("Gán", OK_GREEN, e -> {
            int i = cbCt.getSelectedIndex();
            int j = cbAura.getSelectedIndex();
            if (i < 0 || i >= dangHien.size()) {
                JOptionPane.showMessageDialog(dlg, "Chưa chọn cải trang nào.");
                return;
            }
            if (j < 0 || j >= auraHien.size()) {
                JOptionPane.showMessageDialog(dlg, "Chưa chọn hào quang nào.");
                return;
            }
            // Doc tu danh sach DA LOC cua CA HAI o — doc tu danh sach goc se
            // gan nham ngay khi co chu trong o tim.
            int itemId = (Integer) dangHien.get(i)[0];
            nro.repository.dao.AuraDAO.Aura aChon = auraHien.get(j);
            int auraId = aChon == null ? -1 : aChon.id;
            String loi = nro.repository.dao.AuraDAO.ganChoCaiTrang(itemId, auraId);
            if (loi != null) {
                JOptionPane.showMessageDialog(dlg, loi, "Không gán được",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dlg.dispose();
            napBangAura();
            note(OK_GREEN, auraId == -1
                    ? "Đã gỡ hào quang của vật phẩm " + itemId + "."
                    : "Đã gán hào quang " + auraId + " cho vật phẩm " + itemId + ".");
        }));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(form, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        dlg.setContentPane(wrap);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void goAura() {
        int r = auraCtTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn cải trang nào ở bảng dưới.");
            return;
        }
        int itemId = Integer.parseInt(String.valueOf(auraCtModel.getValueAt(
                auraCtTable.convertRowIndexToModel(r), 0)));
        String loi = nro.repository.dao.AuraDAO.ganChoCaiTrang(itemId, -1);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không gỡ được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBangAura();
        note(OK_GREEN, "Đã gỡ hào quang của vật phẩm " + itemId + ".");
    }

    // =====================================================================
    //  Tab: Công thức đổi (vật phẩm đổi của NPC)
    // =====================================================================

    private final DefaultTableModel ctdModel = new DefaultTableModel(
            new Object[]{"ID", "Mã", "Tên hiện trong game", "NPC cho đổi",
                "Cần có", "Tỉ lệ %", "Nhận được", "Chỉ số kèm theo",
                "Mất khi thất bại", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable ctdTable = new JTable(ctdModel);

    /**
     * Công thức đổi vật phẩm của NPC — trước đây viết cứng trong mã nguồn.
     *
     * <p>Nguyên liệu và hao khi thất bại ghi dạng id:sốLượng ngăn bằng dấu phẩy;
     * kết quả là danh sách id bốc ngẫu nhiên; chỉ số ghi idChỉSố:giáTrị.</p>
     */
    private JComponent buildCongThucTab() {
        ctdTable.setRowHeight(22);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));
        p.add(nhan("Nguyên liệu / Hao khi thất bại: id:sốLượng ngăn bằng dấu phẩy"
                + "   •   Kết quả: danh sách id, bốc ngẫu nhiên một cái"
                + "   •   Chỉ số: idChỉSố:giáTrị   •   sửa xong có hiệu lực ngay"),
                BorderLayout.NORTH);
        p.add(ServerGuiUtils.cuon(ctdTable), BorderLayout.CENTER);
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        btn.add(button("Thêm công thức", OK_GREEN, e -> ctdDialog(true)));
        btn.add(button("Sửa công thức", ACCENT, e -> ctdDialog(false)));
        btn.add(button("Xoá công thức", WARN_RED, e -> ctdXoa()));
        btn.add(button("Tải lại", GREY, e -> loadCongThuc()));
        p.add(btn, BorderLayout.SOUTH);
        loadCongThuc();
        return p;
    }

    private void loadCongThuc() {
        ctdModel.setRowCount(0);
        for (nro.repository.dao.CongThucDoiDAO.CongThuc c
                : nro.repository.dao.CongThucDoiDAO.tatCa()) {
            ctdModel.addRow(new Object[]{c.id, c.ma, c.ten,
                nro.repository.dao.CongThucDoiDAO.tenNpc(c.npc),
                moTaCap(capThanhChuoi(c.nguyenLieu)), c.tyLe,
                moTaDsVatPham(noiSo(c.ketQua)),
                moTaChiSo(capThanhChuoi(c.chiSo)),
                moTaCap(capThanhChuoi(c.haoKhiThatBai)),
                c.bat ? "có" : "", c.ghiChu == null ? "" : c.ghiChu});
        }
    }

    private static String capThanhChuoi(java.util.List<int[]> ds) {
        StringBuilder sb = new StringBuilder();
        for (int[] c : ds) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(c[0]).append(":").append(c[1]);
        }
        return sb.toString();
    }

    private static String noiSo(java.util.List<Integer> ds) {
        StringBuilder sb = new StringBuilder();
        for (int v : ds) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(v);
        }
        return sb.toString();
    }

    /** Tên vật phẩm kèm id, hoặc chỉ id nếu chưa nạp được bảng mẫu. */
    private static String tenVatPham(int id) {
        try {
            nro.entity.template.ItemTemplate t =
                    nro.service.item.ItemService.gI().getTemplate(id);
            return t != null && t.name != null ? t.name : String.valueOf(id);
        } catch (Exception ex) {
            return String.valueOf(id);
        }
    }

    /** Đổi "1281:9999,1282:1" thành "Trang sách cũ x9999, Bìa sách x1". */
    private static String moTaCap(Object raw) {
        String s = raw == null ? "" : String.valueOf(raw).trim();
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String phan : s.split(",")) {
            String[] p = phan.trim().split(":");
            if (p.length != 2) {
                continue;
            }
            try {
                int id = Integer.parseInt(p[0].trim());
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(tenVatPham(id)).append(" x").append(p[1].trim());
            } catch (NumberFormatException ignored) {
                // Phần gõ sai thì bỏ qua, không làm hỏng cả dòng mô tả.
            }
        }
        return sb.toString();
    }

    /** Đổi "1044,1211,1212" thành danh sách tên, ngăn bằng " hoặc ". */
    private static String moTaDsVatPham(Object raw) {
        String s = raw == null ? "" : String.valueOf(raw).trim();
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String phan : s.split(",")) {
            String t = phan.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                if (sb.length() > 0) {
                    sb.append(" hoặc ");
                }
                sb.append(tenVatPham(Integer.parseInt(t)));
            } catch (NumberFormatException ignored) {
                // Bỏ qua id gõ sai.
            }
        }
        return sb.toString();
    }

    /** Đổi "30:0,50:3" thành "Không thể giao dịch=0, Sức đánh=3". */
    private static String moTaChiSo(Object raw) {
        String s = raw == null ? "" : String.valueOf(raw).trim();
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String phan : s.split(",")) {
            String[] p = phan.trim().split(":");
            if (p.length != 2) {
                continue;
            }
            try {
                int id = Integer.parseInt(p[0].trim());
                String ten = OptionPicker.tenCua(id);
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(ten == null || ten.isEmpty() ? String.valueOf(id) : ten)
                        .append("=").append(p[1].trim());
            } catch (NumberFormatException ignored) {
                // Bỏ qua phần gõ sai.
            }
        }
        return sb.toString();
    }

    /**
     * Bảng chọn vật phẩm kèm số lượng, trả về chuỗi id:sốLượng ngăn bằng dấu phẩy.
     *
     * <p>Trả {@code null} nếu người dùng bấm Huỷ — chỗ gọi giữ nguyên giá trị cũ.</p>
     */
    private String suaCapVatPham(java.awt.Component cha, String hienTai, String tieuDe) {
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"ID", "Tên vật phẩm", "Số lượng"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2;
            }
        };
        if (hienTai != null) {
            for (String phan : hienTai.trim().split(",")) {
                String[] p = phan.trim().split(":");
                if (p.length != 2) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(p[0].trim());
                    m.addRow(new Object[]{id, tenVatPham(id),
                        Integer.parseInt(p[1].trim())});
                } catch (NumberFormatException ignored) {
                    // Dòng hỏng trong CSDL -> bỏ qua.
                }
            }
        }
        JTable bang = new JTable(m);
        bang.setRowHeight(24);
        JScrollPane sc = ServerGuiUtils.cuon(bang);
        sc.setPreferredSize(new Dimension(460, 200));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.add(button("Chọn vật phẩm", OK_GREEN, e -> {
            int id = OptionPicker.chonVatPham(bang, -1);
            if (id >= 0) {
                m.addRow(new Object[]{id, tenVatPham(id), 1});
            }
        }));
        nut.add(button("Xoá dòng", WARN_RED, e -> {
            int r = bang.getSelectedRow();
            if (r >= 0) {
                m.removeRow(r);
            }
        }));

        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.add(new JLabel("Cột \"Số lượng\" gõ trực tiếp vào bảng."), BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(nut, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(cha, wrap, tieuDe,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        if (bang.isEditing()) {
            bang.getCellEditor().stopCellEditing();
        }
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < m.getRowCount(); r++) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(intOf(m.getValueAt(r, 0))).append(":")
                    .append(intOf(m.getValueAt(r, 2)));
        }
        return sb.toString();
    }

    /**
     * Bảng chọn danh sách vật phẩm (không có số lượng), trả về "id,id,id".
     *
     * <p>Dùng cho ô "Nhận được": nhiều id nghĩa là mỗi lần đổi bốc ngẫu nhiên
     * một cái trong số đó.</p>
     */
    private String suaDsVatPham(java.awt.Component cha, String hienTai) {
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"ID", "Tên vật phẩm"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        if (hienTai != null) {
            for (String phan : hienTai.trim().split(",")) {
                String t = phan.trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(t);
                    m.addRow(new Object[]{id, tenVatPham(id)});
                } catch (NumberFormatException ignored) {
                    // Bỏ qua id hỏng.
                }
            }
        }
        JTable bang = new JTable(m);
        bang.setRowHeight(24);
        JScrollPane sc = ServerGuiUtils.cuon(bang);
        sc.setPreferredSize(new Dimension(460, 200));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nut.add(button("Chọn vật phẩm", OK_GREEN, e -> {
            int id = OptionPicker.chonVatPham(bang, -1);
            if (id >= 0) {
                m.addRow(new Object[]{id, tenVatPham(id)});
            }
        }));
        nut.add(button("Xoá dòng", WARN_RED, e -> {
            int r = bang.getSelectedRow();
            if (r >= 0) {
                m.removeRow(r);
            }
        }));

        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.add(new JLabel("Nhiều dòng = mỗi lần đổi bốc ngẫu nhiên một món."),
                BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);
        wrap.add(nut, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(cha, wrap, "Vật phẩm nhận được",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < m.getRowCount(); r++) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(intOf(m.getValueAt(r, 0)));
        }
        return sb.toString();
    }

    /**
     * Ô chỉ đọc kèm nút "Chọn…" — thay cho ô gõ id trần.
     *
     * <p>Giá trị thật vẫn nằm trong {@code o} (ẩn đi), nên phần lưu không phải
     * đổi gì. Người dùng chỉ thấy tên vật phẩm và bấm nút để chọn.</p>
     */
    private JPanel oChon(JTextField o, JLabel xem, Runnable veLai,
            java.util.function.Supplier<String> chon) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        o.setEditable(false);
        p.add(o, BorderLayout.CENTER);
        p.add(button("Chọn…", ACCENT, e -> {
            String moi = chon.get();
            if (moi != null) {
                o.setText(moi);
                veLai.run();
            }
        }), BorderLayout.EAST);
        return p;
    }
    private void ctdDialog(boolean them) {
        int r = ctdTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chưa chọn công thức nào.");
            return;
        }
        int idx = them ? -1 : ctdTable.convertRowIndexToModel(r);
        int id = them ? -1 : intOf(ctdModel.getValueAt(idx, 0));
        // Doc GIA TRI GOC tu CSDL, khong lay tu bang: cot tren bang da doi sang
        // ten vat pham cho de doc ("Trang sach cu x9999"), khong dung de sua lai.
        nro.repository.dao.CongThucDoiDAO.CongThuc cu = null;
        if (!them) {
            for (nro.repository.dao.CongThucDoiDAO.CongThuc x
                    : nro.repository.dao.CongThucDoiDAO.tatCa()) {
                if (x.id == id) {
                    cu = x;
                    break;
                }
            }
        }
        JTextField fMa = new JTextField(cu == null ? "" : cu.ma, 22);
        JTextField fTen = new JTextField(cu == null ? "" : cu.ten, 26);
        JTextField fNL = new JTextField(cu == null ? "" : capThanhChuoi(cu.nguyenLieu), 26);
        JTextField fTyLe = new JTextField(cu == null ? "100" : String.valueOf(cu.tyLe), 6);
        JTextField fKQ = new JTextField(cu == null ? "" : noiSo(cu.ketQua), 26);
        JTextField fCS = new JTextField(cu == null ? "" : capThanhChuoi(cu.chiSo), 26);
        JTextField fHao = new JTextField(cu == null ? "" : capThanhChuoi(cu.haoKhiThatBai), 26);
        JTextField fGhi = new JTextField(cu == null || cu.ghiChu == null ? "" : cu.ghiChu, 26);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật", cu == null || cu.bat);

        // O chon NPC: gan cong thuc vao mot NPC la NPC do tu co muc doi, khong
        // phai sua ma. De trong thi cong thuc chi chay tu cho goi viet cung.
        javax.swing.JComboBox<String> cbNpc = new javax.swing.JComboBox<>();
        for (String n : nro.repository.dao.CongThucDoiDAO.CAC_NPC) {
            cbNpc.addItem(nro.repository.dao.CongThucDoiDAO.tenNpc(n));
        }
        cbNpc.setSelectedItem(nro.repository.dao.CongThucDoiDAO.tenNpc(
                cu == null ? nro.repository.dao.CongThucDoiDAO.NPC_KHONG : cu.npc));

        JLabel xemNL = new JLabel();
        JLabel xemKQ = new JLabel();
        JLabel xemCS = new JLabel();
        JLabel xemHao = new JLabel();
        Runnable veLai = () -> {
            xemNL.setText(moTaCap(fNL.getText()));
            xemKQ.setText(moTaDsVatPham(fKQ.getText()));
            xemCS.setText(moTaChiSo(fCS.getText()));
            xemHao.setText(moTaCap(fHao.getText()));
        };
        veLai.run();
        for (JTextField o : new JTextField[]{fNL, fKQ, fCS, fHao}) {
            o.getDocument().addDocumentListener(
                    new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    veLai.run();
                }
            
                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    veLai.run();
                }
            
                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    veLai.run();
                }
            });
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Mã (code gọi):", fMa);
        addRow(form, c, 1, "Tên hiện trong game:", fTen);
        addRowC(form, c, 2, "Cần có:", oChon(fNL, xemNL, veLai,
                () -> suaCapVatPham(form, fNL.getText(), "Nguyên liệu cần có")));
        addRowC(form, c, 3, "", xemNL);
        addRow(form, c, 4, "Tỉ lệ thành công (%):", fTyLe);
        addRowC(form, c, 5, "Nhận được:", oChon(fKQ, xemKQ, veLai,
                () -> suaDsVatPham(form, fKQ.getText())));
        addRowC(form, c, 6, "", xemKQ);
        addRowC(form, c, 7, "Chỉ số kèm theo:", oChon(fCS, xemCS, veLai,
                () -> suaChiSoNgauNhien(form, fCS.getText())));
        addRowC(form, c, 8, "", xemCS);
        addRowC(form, c, 9, "Mất khi thất bại:", oChon(fHao, xemHao, veLai,
                () -> suaCapVatPham(form, fHao.getText(), "Mất khi thất bại")));
        addRowC(form, c, 10, "", xemHao);
        addRowC(form, c, 11, "NPC cho đổi:", cbNpc);
        addRow(form, c, 12, "Ghi chú:", fGhi);
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm công thức đổi" : "Sửa công thức đổi",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int tyLe;
        try {
            tyLe = Integer.parseInt(fTyLe.getText().trim());
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Tỉ lệ phải là số nguyên từ 0 đến 100.");
            return;
        }
        String loi = nro.repository.dao.CongThucDoiDAO.luu(id,
                fMa.getText(), fTen.getText(), fNL.getText().trim(), tyLe,
                fKQ.getText().trim(), fCS.getText().trim(), fHao.getText().trim(),
                cbBat.isSelected(), fGhi.getText().trim(),
                maNpcDaChon(cbNpc.getSelectedIndex()));
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadCongThuc();
        LamMoi.bao(LamMoi.SU_KIEN);
        note(OK_GREEN, "Đã lưu công thức — có hiệu lực ngay.");
    }

    /**
     * Mã NPC ứng với vị trí đang chọn trong ô chọn NPC.
     *
     * <p>Ô chọn hiện tên tiếng Việt cho dễ đọc, nên phải đổi ngược về mã. Đối
     * chiếu theo <b>vị trí</b> chứ không so tên: tên có thể sửa lại cho dễ hiểu
     * hơn mà không phải lo chỗ này đứt.</p>
     */
    private static String maNpcDaChon(int viTri) {
        String[] ds = nro.repository.dao.CongThucDoiDAO.CAC_NPC;
        return (viTri < 0 || viTri >= ds.length)
                ? nro.repository.dao.CongThucDoiDAO.NPC_KHONG : ds[viTri];
    }

    // =====================================================================
    //  Tab "Bông tai" và tab "Chân mệnh"
    // =====================================================================
    //
    // Hai tab cùng một khuôn: bảng CẤP ở trên, bảng CHỈ SỐ của cấp đang chọn ở
    // dưới. Nên phần dựng giao diện gộp vào một hàm, chỉ khác nhãn và bộ hàm
    // đọc/ghi truyền vào.

    private final DefaultTableModel btCapModel = new DefaultTableModel(
            new Object[]{"Cấp", "Vật phẩm", "Tên hiện trong game", "Icon",
                "Số dòng chỉ số", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable btCapTable = new JTable(btCapModel);

    private final DefaultTableModel btChiSoModel = new DefaultTableModel(
            new Object[]{"ID", "Chỉ số", "Từ", "Đến", "Bật"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable btChiSoTable = new JTable(btChiSoModel);

    private final DefaultTableModel btSoDongModel = new DefaultTableModel(
            new Object[]{"ID", "Số dòng", "Tỉ lệ %", "Bật"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable btSoDongTable = new JTable(btSoDongModel);

    private final DefaultTableModel cmCapModel = new DefaultTableModel(
            new Object[]{"Cấp", "Vật phẩm", "Tên hiện trong game", "Icon",
                "Tỉ lệ nâng %", "Tinh thể", "Ma quái", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable cmCapTable = new JTable(cmCapModel);

    private final DefaultTableModel cmChiSoModel = new DefaultTableModel(
            new Object[]{"ID", "Chỉ số", "Giá trị", "Bật"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable cmChiSoTable = new JTable(cmChiSoModel);

    private JComponent buildBongTaiTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        root.add(nhan("Bông tai Porata — mỗi cấp một mẫu vật phẩm riêng, có tên "
                + "và icon riêng. Bảng dưới là <b>bể chỉ số</b>: mỗi lần nâng chỉ "
                + "số ở Bà Hạt Mít, game bốc ngẫu nhiên trong bể của cấp đó. "
                + "Để bể rỗng thì cấp đó giữ nguyên bể viết cứng trong mã."),
                BorderLayout.NORTH);

        btCapTable.setRowHeight(24);
        btCapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        btCapTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wC = {45, 180, 200, 60, 100, 45, 220};
        for (int i = 0; i < wC.length && i < btCapTable.getColumnCount(); i++) {
            btCapTable.getColumnModel().getColumn(i).setPreferredWidth(wC[i]);
        }
        btCapTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btLoadChiSo();
            }
        });
        btCapTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btSuaCap(false);
                }
            }
        });
        JPanel pCap = new JPanel(new BorderLayout(0, 4));
        pCap.setOpaque(false);
        pCap.setBorder(titled("Các cấp bông tai"));
        pCap.add(ServerGuiUtils.cuon(btCapTable), BorderLayout.CENTER);
        JPanel bCap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bCap.setOpaque(false);
        bCap.add(button("Thêm cấp", OK_GREEN, e -> btSuaCap(true)));
        bCap.add(button("Sửa cấp", ACCENT, e -> btSuaCap(false)));
        bCap.add(button("Xoá cấp", WARN_RED, e -> btXoaCap()));
        bCap.add(button("Dựng sẵn 5 cấp", new Color(0, 140, 80),
                e -> btDungSan()));
        bCap.add(button("Tải lại", GREY, e -> btLoadCap()));
        pCap.add(bCap, BorderLayout.SOUTH);

        btChiSoTable.setRowHeight(24);
        btChiSoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        btChiSoTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wS = {45, 260, 70, 70, 45};
        for (int i = 0; i < wS.length && i < btChiSoTable.getColumnCount(); i++) {
            btChiSoTable.getColumnModel().getColumn(i).setPreferredWidth(wS[i]);
        }
        btChiSoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btSuaChiSo(false);
                }
            }
        });
        JPanel pSo = new JPanel(new BorderLayout(0, 4));
        pSo.setOpaque(false);
        pSo.setBorder(titled("Bể chỉ số của cấp đang chọn"));
        pSo.add(ServerGuiUtils.cuon(btChiSoTable), BorderLayout.CENTER);
        JPanel bSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bSo.setOpaque(false);
        bSo.add(button("Thêm chỉ số", OK_GREEN, e -> btSuaChiSo(true)));
        bSo.add(button("Sửa chỉ số", ACCENT, e -> btSuaChiSo(false)));
        bSo.add(button("Xoá chỉ số", WARN_RED, e -> btXoaChiSo()));
        pSo.add(bSo, BorderLayout.SOUTH);

        btSoDongTable.setRowHeight(24);
        btSoDongTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        btSoDongTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wD = {45, 70, 70, 45};
        for (int i = 0; i < wD.length && i < btSoDongTable.getColumnCount(); i++) {
            btSoDongTable.getColumnModel().getColumn(i).setPreferredWidth(wD[i]);
        }
        btSoDongTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btSuaSoDong(false);
                }
            }
        });
        JPanel pDong = new JPanel(new BorderLayout(0, 4));
        pDong.setOpaque(false);
        pDong.setBorder(titled("Tỉ lệ ra bao nhiêu dòng chỉ số"));
        pDong.add(ServerGuiUtils.cuon(btSoDongTable), BorderLayout.CENTER);
        JPanel bDong = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bDong.setOpaque(false);
        bDong.add(button("Thêm mức", OK_GREEN, e -> btSuaSoDong(true)));
        bDong.add(button("Sửa mức", ACCENT, e -> btSuaSoDong(false)));
        bDong.add(button("Xoá mức", WARN_RED, e -> btXoaSoDong()));
        pDong.add(bDong, BorderLayout.SOUTH);

        javax.swing.JSplitPane duoi = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT, pSo, pDong);
        duoi.setResizeWeight(0.62);
        duoi.setBorder(null);
        javax.swing.JSplitPane chia = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, pCap, duoi);
        chia.setResizeWeight(0.42);
        chia.setBorder(null);
        root.add(chia, BorderLayout.CENTER);
        btLoadCap();
        return root;
    }

    private JComponent buildChanMenhTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        root.add(nhan("Chân Mệnh Thiên Tử — mỗi cấp một mẫu vật phẩm, một tỉ lệ "
                + "nâng và số nguyên liệu riêng. Bảng dưới là chỉ số <b>cố định</b> "
                + "của cấp đó (không bốc ngẫu nhiên). Để trống thì cấp đó giữ "
                + "nguyên cách tính viết cứng trong mã."),
                BorderLayout.NORTH);

        cmCapTable.setRowHeight(24);
        cmCapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cmCapTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wC = {45, 180, 200, 60, 90, 75, 75, 45, 200};
        for (int i = 0; i < wC.length && i < cmCapTable.getColumnCount(); i++) {
            cmCapTable.getColumnModel().getColumn(i).setPreferredWidth(wC[i]);
        }
        cmCapTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cmLoadChiSo();
            }
        });
        cmCapTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    cmSuaCap(false);
                }
            }
        });
        JPanel pCap = new JPanel(new BorderLayout(0, 4));
        pCap.setOpaque(false);
        pCap.setBorder(titled("Các cấp Chân Mệnh Thiên Tử"));
        pCap.add(ServerGuiUtils.cuon(cmCapTable), BorderLayout.CENTER);
        JPanel bCap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bCap.setOpaque(false);
        bCap.add(button("Thêm cấp", OK_GREEN, e -> cmSuaCap(true)));
        bCap.add(button("Sửa cấp", ACCENT, e -> cmSuaCap(false)));
        bCap.add(button("Xoá cấp", WARN_RED, e -> cmXoaCap()));
        bCap.add(button("Dựng sẵn 8 cấp", new Color(0, 140, 80),
                e -> cmDungSan()));
        bCap.add(button("Tải lại", GREY, e -> cmLoadCap()));
        pCap.add(bCap, BorderLayout.SOUTH);

        cmChiSoTable.setRowHeight(24);
        cmChiSoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cmChiSoTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wS = {45, 260, 90, 45};
        for (int i = 0; i < wS.length && i < cmChiSoTable.getColumnCount(); i++) {
            cmChiSoTable.getColumnModel().getColumn(i).setPreferredWidth(wS[i]);
        }
        cmChiSoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    cmSuaChiSo(false);
                }
            }
        });
        JPanel pSo = new JPanel(new BorderLayout(0, 4));
        pSo.setOpaque(false);
        pSo.setBorder(titled("Chỉ số của cấp đang chọn"));
        pSo.add(ServerGuiUtils.cuon(cmChiSoTable), BorderLayout.CENTER);
        JPanel bSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bSo.setOpaque(false);
        bSo.add(button("Thêm chỉ số", OK_GREEN, e -> cmSuaChiSo(true)));
        bSo.add(button("Sửa chỉ số", ACCENT, e -> cmSuaChiSo(false)));
        bSo.add(button("Xoá chỉ số", WARN_RED, e -> cmXoaChiSo()));
        pSo.add(bSo, BorderLayout.SOUTH);

        javax.swing.JSplitPane chia = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, pCap, pSo);
        chia.setResizeWeight(0.5);
        chia.setBorder(null);
        root.add(chia, BorderLayout.CENTER);
        cmLoadCap();
        return root;
    }

    // ---------------------------------------------------------------- nạp bảng

    private void btLoadCap() {
        btCapModel.setRowCount(0);
        for (nro.repository.dao.TrangSucDAO.Cap c
                : nro.repository.dao.TrangSucDAO.capBongTai()) {
            btCapModel.addRow(new Object[]{c.cap, moTaVatPham(c.itemId),
                nz(c.ten), c.icon < 0 ? "—" : String.valueOf(c.icon),
                c.soDong, c.bat ? "có" : "", nz(c.ghiChu)});
        }
        btLoadChiSo();
    }

    private void btLoadChiSo() {
        btChiSoModel.setRowCount(0);
        int cap = btCapDangChon();
        if (cap < 0) {
            return;
        }
        for (nro.repository.dao.TrangSucDAO.ChiSo cs
                : nro.repository.dao.TrangSucDAO.chiSoBongTai(cap, false)) {
            btChiSoModel.addRow(new Object[]{cs.id,
                cs.optionId + " — " + nro.ui.OptionPicker.tenChiSo(cs.optionId),
                cs.min, cs.max, cs.bat ? "có" : ""});
        }
        btSoDongModel.setRowCount(0);
        for (nro.repository.dao.TrangSucDAO.SoDong x
                : nro.repository.dao.TrangSucDAO.dsSoDong(cap, false)) {
            btSoDongModel.addRow(new Object[]{x.id, x.soDong,
                soGon(x.tiLe), x.bat ? "có" : ""});
        }
    }

    /**
     * Sửa một mức "ra bao nhiêu dòng chỉ số".
     *
     * <p>Máy chủ xét từ mức <b>nhiều dòng nhất</b> xuống, mức nào trúng tỉ lệ của
     * nó thì lấy luôn. Nên khai "3 dòng 5%" và "2 dòng 30%" là ra đúng nghĩa
     * thường: 5% ba dòng, 30% hai dòng, còn lại một dòng — không phải tự tính cho
     * tổng khớp 100.</p>
     */
    private void btSuaSoDong(boolean them) {
        int cap = btCapDangChon();
        if (cap < 0) {
            note(WARN_RED, "Chọn một cấp trước.");
            return;
        }
        int idCu = -1;
        int r = btSoDongTable.getSelectedRow();
        if (!them) {
            if (r < 0) {
                note(WARN_RED, "Chọn một mức trước.");
                return;
            }
            idCu = intOf(btSoDongModel.getValueAt(
                    btSoDongTable.convertRowIndexToModel(r), 0));
        }
        nro.repository.dao.TrangSucDAO.SoDong cu = null;
        if (idCu > 0) {
            for (nro.repository.dao.TrangSucDAO.SoDong x
                    : nro.repository.dao.TrangSucDAO.dsSoDong(cap, false)) {
                if (x.id == idCu) {
                    cu = x;
                    break;
                }
            }
        }
        JTextField fDong = new JTextField(cu == null ? "2"
                : String.valueOf(cu.soDong), 6);
        JTextField fTiLe = new JTextField(cu == null ? "30" : soGon(cu.tiLe), 8);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                cu == null || cu.bat);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        addRow(form, c, y++, "Số dòng chỉ số:", fDong);
        addRow(form, c, y++, "Tỉ lệ ra (%):", fTiLe);
        y = ghiChuHang(form, c, y,
                "mức nhiều dòng được xét trước; không trúng mức nào thì ra 1 dòng");
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm mức số dòng" : "Sửa mức số dòng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.TrangSucDAO.SoDong moi =
                new nro.repository.dao.TrangSucDAO.SoDong();
        moi.id = idCu;
        moi.cap = cap;
        moi.soDong = laySoAnToan(fDong.getText());
        try {
            moi.tiLe = docSoThuc(fTiLe.getText(), "Tỉ lệ");
        } catch (IllegalArgumentException ex) {
            note(WARN_RED, ex.getMessage());
            return;
        }
        moi.bat = cbBat.isSelected();
        String loi = nro.repository.dao.TrangSucDAO.luuSoDong(moi);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        btLoadChiSo();
        note(OK_GREEN, "Đã lưu mức " + moi.soDong + " dòng.");
    }

    private void btXoaSoDong() {
        int r = btSoDongTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một mức trước.");
            return;
        }
        int id = intOf(btSoDongModel.getValueAt(
                btSoDongTable.convertRowIndexToModel(r), 0));
        String loi = nro.repository.dao.TrangSucDAO.xoaSoDong(id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        btLoadChiSo();
        note(OK_GREEN, "Đã xoá mức.");
    }

    private void cmLoadCap() {
        cmCapModel.setRowCount(0);
        for (nro.repository.dao.TrangSucDAO.Cap c
                : nro.repository.dao.TrangSucDAO.capChanMenh()) {
            cmCapModel.addRow(new Object[]{c.cap, moTaVatPham(c.itemId),
                nz(c.ten), c.icon < 0 ? "—" : String.valueOf(c.icon),
                soGon(c.tiLe), PlayerManagerPanel.fmt(c.tinhThe),
                PlayerManagerPanel.fmt(c.maQuai),
                c.bat ? "có" : "", nz(c.ghiChu)});
        }
        cmLoadChiSo();
    }

    private void cmLoadChiSo() {
        cmChiSoModel.setRowCount(0);
        int cap = cmCapDangChon();
        if (cap < 0) {
            return;
        }
        for (nro.repository.dao.TrangSucDAO.ChiSo cs
                : nro.repository.dao.TrangSucDAO.chiSoChanMenh(cap, false)) {
            cmChiSoModel.addRow(new Object[]{cs.id,
                cs.optionId + " — " + nro.ui.OptionPicker.tenChiSo(cs.optionId),
                cs.min, cs.bat ? "có" : ""});
        }
    }

    /** Cấp bông tai đang chọn, hoặc {@code -1}. */
    private int btCapDangChon() {
        int r = btCapTable.getSelectedRow();
        return r < 0 ? -1 : intOf(btCapModel.getValueAt(
                btCapTable.convertRowIndexToModel(r), 0));
    }

    private int cmCapDangChon() {
        int r = cmCapTable.getSelectedRow();
        return r < 0 ? -1 : intOf(cmCapModel.getValueAt(
                cmCapTable.convertRowIndexToModel(r), 0));
    }

    /** Bỏ đuôi ",0" cho số tròn. */
    private static String soGon(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v)
                : String.format(java.util.Locale.US, "%.2f", v);
    }

    // ------------------------------------------------------------- hộp thoại

    private void btSuaCap(boolean them) {
        int capCu = them ? -1 : btCapDangChon();
        if (!them && capCu < 0) {
            note(WARN_RED, "Chọn một cấp trước.");
            return;
        }
        nro.repository.dao.TrangSucDAO.Cap cu = null;
        if (!them) {
            for (nro.repository.dao.TrangSucDAO.Cap x
                    : nro.repository.dao.TrangSucDAO.capBongTai()) {
                if (x.cap == capCu) {
                    cu = x;
                    break;
                }
            }
        }
        JTextField fCap = new JTextField(cu == null ? "" : String.valueOf(cu.cap), 6);
        JTextField fItem = new JTextField(cu == null ? "" : String.valueOf(cu.itemId), 10);
        JTextField fTen = new JTextField(cu == null ? "" : nz(cu.ten), 26);
        JTextField fIcon = new JTextField(cu == null ? "-1" : String.valueOf(cu.icon), 8);
        JTextField fDong = new JTextField(cu == null ? "1" : String.valueOf(cu.soDong), 6);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                cu == null || cu.bat);
        JTextField fGhi = new JTextField(cu == null ? "" : nz(cu.ghiChu), 26);
        fCap.setEditable(them);

        JLabel xemItem = new JLabel();
        JLabel xemIcon = new JLabel();
        Runnable veLai = () -> {
            xemItem.setText(moTaVatPham(laySoAnToan(fItem.getText())));
            int ic = laySoAnToan(fIcon.getText());
            xemIcon.setText(ic < 0 ? "(không đặt — dùng icon của mẫu)"
                    : ("icon " + ic));
        };
        veLai.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        addRow(form, c, y++, "Cấp:", fCap);
        addRowC(form, c, y++, "Vật phẩm:", oChonSo(fItem, veLai,
                () -> nro.ui.OptionPicker.chonVatPham(this,
                        laySoAnToan(fItem.getText()))));
        addRowC(form, c, y++, "", xemItem);
        addRow(form, c, y++, "Tên hiện trong game:", fTen);
        addRowC(form, c, y++, "Icon:", oChonSo(fIcon, veLai,
                () -> ItemManagerPanel.chonIcon(this,
                        laySoAnToan(fIcon.getText()))));
        addRowC(form, c, y++, "", xemIcon);
        addRow(form, c, y++, "Số dòng chỉ số:", fDong);
        addRow(form, c, y++, "Ghi chú:", fGhi);
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm cấp bông tai" : "Sửa cấp bông tai",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.TrangSucDAO.Cap moi =
                new nro.repository.dao.TrangSucDAO.Cap();
        moi.cap = laySoAnToan(fCap.getText());
        moi.itemId = laySoAnToan(fItem.getText());
        moi.ten = fTen.getText().trim();
        moi.icon = laySoAnToan(fIcon.getText());
        moi.soDong = laySoAnToan(fDong.getText());
        moi.bat = cbBat.isSelected();
        moi.ghiChu = fGhi.getText().trim();
        String loi = nro.repository.dao.TrangSucDAO.luuCapBongTai(moi);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        btLoadCap();
        note(OK_GREEN, "Đã lưu cấp bông tai " + moi.cap + ".");
    }

    private void btXoaCap() {
        int cap = btCapDangChon();
        if (cap < 0) {
            note(WARN_RED, "Chọn một cấp trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá cấp " + cap + " và toàn bộ chỉ số của nó?",
                "Xoá cấp bông tai", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.TrangSucDAO.xoaCapBongTai(cap);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        btLoadCap();
        note(OK_GREEN, "Đã xoá cấp " + cap + ".");
    }

    /**
     * Dựng sẵn năm cấp bông tai theo đúng id mà mã đang dùng.
     *
     * <p>Đỡ phải tự tra id: {@code 454, 921, 1943, 2105, 2106} là năm mẫu mà
     * {@code usePorata} nhận, gõ sai một số là bấm F không ăn.</p>
     */
    private void btDungSan() {
        int[] ids = nro.repository.dao.TrangSucDAO.ID_BONG_TAI;
        int them = 0;
        for (int i = 0; i < ids.length; i++) {
            nro.repository.dao.TrangSucDAO.Cap c =
                    new nro.repository.dao.TrangSucDAO.Cap();
            c.cap = i + 1;
            c.itemId = ids[i];
            try {
                nro.entity.template.ItemTemplate t =
                        nro.service.item.ItemService.gI().getTemplate(ids[i]);
                c.ten = t == null ? ("Bông tai Porata +" + (i + 1)) : t.name;
                c.icon = t == null ? -1 : t.iconID;
            } catch (Exception boQua) {
                c.ten = "Bông tai Porata +" + (i + 1);
                c.icon = -1;
            }
            c.soDong = i + 1;
            if (nro.repository.dao.TrangSucDAO.luuCapBongTai(c) == null) {
                them++;
            }
        }
        btLoadCap();
        note(OK_GREEN, "Đã dựng " + them + " cấp — sửa lại tên, icon tuỳ ý.");
    }

    private void btSuaChiSo(boolean them) {
        int cap = btCapDangChon();
        if (cap < 0) {
            note(WARN_RED, "Chọn một cấp trước.");
            return;
        }
        int idCu = -1;
        int r = btChiSoTable.getSelectedRow();
        if (!them) {
            if (r < 0) {
                note(WARN_RED, "Chọn một dòng chỉ số trước.");
                return;
            }
            idCu = intOf(btChiSoModel.getValueAt(
                    btChiSoTable.convertRowIndexToModel(r), 0));
        }
        nro.repository.dao.TrangSucDAO.ChiSo cu = null;
        if (idCu > 0) {
            for (nro.repository.dao.TrangSucDAO.ChiSo x
                    : nro.repository.dao.TrangSucDAO.chiSoBongTai(cap, false)) {
                if (x.id == idCu) {
                    cu = x;
                    break;
                }
            }
        }
        JTextField fOpt = new JTextField(cu == null ? "0" : String.valueOf(cu.optionId), 8);
        JTextField fMin = new JTextField(cu == null ? "1" : String.valueOf(cu.min), 8);
        JTextField fMax = new JTextField(cu == null ? "1" : String.valueOf(cu.max), 8);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                cu == null || cu.bat);
        JLabel xem = new JLabel();
        Runnable veLai = () -> xem.setText(
                nro.ui.OptionPicker.tenChiSo(laySoAnToan(fOpt.getText())));
        veLai.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        addRowC(form, c, y++, "Chỉ số:", oChonSo(fOpt, veLai,
                () -> nro.ui.OptionPicker.chonChiSo(this,
                        laySoAnToan(fOpt.getText()))));
        addRowC(form, c, y++, "", xem);
        addRow(form, c, y++, "Giá trị từ:", fMin);
        addRow(form, c, y++, "Đến:", fMax);
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm chỉ số bông tai" : "Sửa chỉ số bông tai",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.TrangSucDAO.ChiSo moi =
                new nro.repository.dao.TrangSucDAO.ChiSo();
        moi.id = idCu;
        moi.cap = cap;
        moi.optionId = laySoAnToan(fOpt.getText());
        moi.min = laySoAnToan(fMin.getText());
        moi.max = laySoAnToan(fMax.getText());
        moi.bat = cbBat.isSelected();
        String loi = nro.repository.dao.TrangSucDAO.luuChiSoBongTai(moi);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        btLoadChiSo();
        note(OK_GREEN, "Đã lưu chỉ số.");
    }

    private void btXoaChiSo() {
        int r = btChiSoTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng chỉ số trước.");
            return;
        }
        int id = intOf(btChiSoModel.getValueAt(
                btChiSoTable.convertRowIndexToModel(r), 0));
        String loi = nro.repository.dao.TrangSucDAO.xoaChiSoBongTai(id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        btLoadChiSo();
        note(OK_GREEN, "Đã xoá dòng chỉ số.");
    }

    private void cmSuaCap(boolean them) {
        int capCu = them ? -1 : cmCapDangChon();
        if (!them && capCu < 0) {
            note(WARN_RED, "Chọn một cấp trước.");
            return;
        }
        nro.repository.dao.TrangSucDAO.Cap cu = null;
        if (!them) {
            for (nro.repository.dao.TrangSucDAO.Cap x
                    : nro.repository.dao.TrangSucDAO.capChanMenh()) {
                if (x.cap == capCu) {
                    cu = x;
                    break;
                }
            }
        }
        JTextField fCap = new JTextField(cu == null ? "" : String.valueOf(cu.cap), 6);
        JTextField fItem = new JTextField(cu == null ? "" : String.valueOf(cu.itemId), 10);
        JTextField fTen = new JTextField(cu == null ? "" : nz(cu.ten), 26);
        JTextField fIcon = new JTextField(cu == null ? "-1" : String.valueOf(cu.icon), 8);
        JTextField fTiLe = new JTextField(cu == null ? "100" : soGon(cu.tiLe), 8);
        JTextField fTinhThe = new JTextField(cu == null ? "0" : String.valueOf(cu.tinhThe), 10);
        JTextField fMaQuai = new JTextField(cu == null ? "0" : String.valueOf(cu.maQuai), 10);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                cu == null || cu.bat);
        JTextField fGhi = new JTextField(cu == null ? "" : nz(cu.ghiChu), 26);
        fCap.setEditable(them);

        JLabel xemItem = new JLabel();
        JLabel xemIcon = new JLabel();
        Runnable veLai = () -> {
            xemItem.setText(moTaVatPham(laySoAnToan(fItem.getText())));
            int ic = laySoAnToan(fIcon.getText());
            xemIcon.setText(ic < 0 ? "(không đặt — dùng icon của mẫu)"
                    : ("icon " + ic));
        };
        veLai.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        addRow(form, c, y++, "Cấp:", fCap);
        addRowC(form, c, y++, "Vật phẩm:", oChonSo(fItem, veLai,
                () -> nro.ui.OptionPicker.chonVatPham(this,
                        laySoAnToan(fItem.getText()))));
        addRowC(form, c, y++, "", xemItem);
        addRow(form, c, y++, "Tên hiện trong game:", fTen);
        addRowC(form, c, y++, "Icon:", oChonSo(fIcon, veLai,
                () -> ItemManagerPanel.chonIcon(this,
                        laySoAnToan(fIcon.getText()))));
        addRowC(form, c, y++, "", xemIcon);
        addRow(form, c, y++, "Tỉ lệ nâng (%):", fTiLe);
        addRow(form, c, y++, "Cần Tinh thể:", fTinhThe);
        addRow(form, c, y++, "Cần Ma quái:", fMaQuai);
        addRow(form, c, y++, "Ghi chú:", fGhi);
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm cấp chân mệnh" : "Sửa cấp chân mệnh",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.TrangSucDAO.Cap moi =
                new nro.repository.dao.TrangSucDAO.Cap();
        moi.cap = laySoAnToan(fCap.getText());
        moi.itemId = laySoAnToan(fItem.getText());
        moi.ten = fTen.getText().trim();
        moi.icon = laySoAnToan(fIcon.getText());
        try {
            moi.tiLe = docSoThuc(fTiLe.getText(), "Tỉ lệ nâng");
        } catch (IllegalArgumentException ex) {
            note(WARN_RED, ex.getMessage());
            return;
        }
        moi.tinhThe = laySoAnToan(fTinhThe.getText());
        moi.maQuai = laySoAnToan(fMaQuai.getText());
        moi.bat = cbBat.isSelected();
        moi.ghiChu = fGhi.getText().trim();
        String loi = nro.repository.dao.TrangSucDAO.luuCapChanMenh(moi);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        cmLoadCap();
        note(OK_GREEN, "Đã lưu cấp chân mệnh " + moi.cap + ".");
    }

    private void cmXoaCap() {
        int cap = cmCapDangChon();
        if (cap < 0) {
            note(WARN_RED, "Chọn một cấp trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá cấp " + cap + " và toàn bộ chỉ số của nó?",
                "Xoá cấp chân mệnh", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.TrangSucDAO.xoaCapChanMenh(cap);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        cmLoadCap();
        note(OK_GREEN, "Đã xoá cấp " + cap + ".");
    }

    /**
     * Dựng sẵn tám cấp chân mệnh theo id mà mã đang dùng.
     *
     * <p>Mã cũ tính cấp bằng {@code template.id - 2002} và nâng cấp bằng
     * {@code id + 1}, nên tám id phải liền nhau từ 2002.</p>
     */
    private void cmDungSan() {
        int them = 0;
        for (int i = 0; i < nro.repository.dao.TrangSucDAO.SO_CAP_CHAN_MENH; i++) {
            int id = nro.repository.dao.TrangSucDAO.ID_CHAN_MENH_DAU + i;
            nro.repository.dao.TrangSucDAO.Cap c =
                    new nro.repository.dao.TrangSucDAO.Cap();
            c.cap = i;
            c.itemId = id;
            try {
                nro.entity.template.ItemTemplate t =
                        nro.service.item.ItemService.gI().getTemplate(id);
                c.ten = t == null ? ("Chân Mệnh Thiên Tử cấp " + (i + 1)) : t.name;
                c.icon = t == null ? -1 : t.iconID;
            } catch (Exception boQua) {
                c.ten = "Chân Mệnh Thiên Tử cấp " + (i + 1);
                c.icon = -1;
            }
            if (nro.repository.dao.TrangSucDAO.luuCapChanMenh(c) == null) {
                them++;
            }
        }
        cmLoadCap();
        note(OK_GREEN, "Đã dựng " + them + " cấp — sửa lại tỉ lệ, nguyên liệu, "
                + "chỉ số tuỳ ý.");
    }

    private void cmSuaChiSo(boolean them) {
        int cap = cmCapDangChon();
        if (cap < 0) {
            note(WARN_RED, "Chọn một cấp trước.");
            return;
        }
        int idCu = -1;
        int r = cmChiSoTable.getSelectedRow();
        if (!them) {
            if (r < 0) {
                note(WARN_RED, "Chọn một dòng chỉ số trước.");
                return;
            }
            idCu = intOf(cmChiSoModel.getValueAt(
                    cmChiSoTable.convertRowIndexToModel(r), 0));
        }
        nro.repository.dao.TrangSucDAO.ChiSo cu = null;
        if (idCu > 0) {
            for (nro.repository.dao.TrangSucDAO.ChiSo x
                    : nro.repository.dao.TrangSucDAO.chiSoChanMenh(cap, false)) {
                if (x.id == idCu) {
                    cu = x;
                    break;
                }
            }
        }
        JTextField fOpt = new JTextField(cu == null ? "0" : String.valueOf(cu.optionId), 8);
        JTextField fGt = new JTextField(cu == null ? "0" : String.valueOf(cu.min), 8);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                cu == null || cu.bat);
        JLabel xem = new JLabel();
        Runnable veLai = () -> xem.setText(
                nro.ui.OptionPicker.tenChiSo(laySoAnToan(fOpt.getText())));
        veLai.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        addRowC(form, c, y++, "Chỉ số:", oChonSo(fOpt, veLai,
                () -> nro.ui.OptionPicker.chonChiSo(this,
                        laySoAnToan(fOpt.getText()))));
        addRowC(form, c, y++, "", xem);
        addRow(form, c, y++, "Giá trị:", fGt);
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm chỉ số chân mệnh" : "Sửa chỉ số chân mệnh",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.TrangSucDAO.ChiSo moi =
                new nro.repository.dao.TrangSucDAO.ChiSo();
        moi.id = idCu;
        moi.cap = cap;
        moi.optionId = laySoAnToan(fOpt.getText());
        moi.min = laySoAnToan(fGt.getText());
        moi.max = moi.min;
        moi.bat = cbBat.isSelected();
        String loi = nro.repository.dao.TrangSucDAO.luuChiSoChanMenh(moi);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        cmLoadChiSo();
        note(OK_GREEN, "Đã lưu chỉ số.");
    }

    private void cmXoaChiSo() {
        int r = cmChiSoTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng chỉ số trước.");
            return;
        }
        int id = intOf(cmChiSoModel.getValueAt(
                cmChiSoTable.convertRowIndexToModel(r), 0));
        String loi = nro.repository.dao.TrangSucDAO.xoaChiSoChanMenh(id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        cmLoadChiSo();
        note(OK_GREEN, "Đã xoá dòng chỉ số.");
    }

    /**
     * Ô chỉ đọc kèm nút "Chọn…" cho một ô chứa <b>số</b>.
     *
     * <p>Khác {@code oChon} sẵn có: hàm chọn ở đây trả về {@code int}, và
     * {@code -1} nghĩa là người dùng huỷ nên phải giữ nguyên ô.</p>
     */
    private JPanel oChonSo(JTextField o, Runnable veLai,
            java.util.function.IntSupplier chon) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        o.setEditable(false);
        p.add(o, BorderLayout.CENTER);
        p.add(button("Chọn…", ACCENT, e -> {
            int moi = chon.getAsInt();
            if (moi >= 0) {
                o.setText(String.valueOf(moi));
                veLai.run();
            }
        }), BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    //  Tab "Đệ tử"
    // =====================================================================
    //
    // Ba phần: TRẦN chỉ số của đệ thường, danh sách LOẠI đệ, và BẬC theo bản đồ.
    // Bậc ghi bằng phần nghìn của trần, nên đổi trần là cả thang tự giãn theo.

    // Nhom 1: chi so DE SO SINH — boc ngau nhien luc nhan de.
    private final JTextField fSsHpMin = new JTextField(12);
    private final JTextField fSsHpMax = new JTextField(12);
    private final JTextField fSsDameMin = new JTextField(12);
    private final JTextField fSsDameMax = new JTextField(12);
    private final JTextField fSsGiapMin = new JTextField(8);
    private final JTextField fSsGiapMax = new JTextField(8);
    private final JTextField fSsCritMin = new JTextField(6);
    private final JTextField fSsCritMax = new JTextField(6);
    private final JTextField fSsSmMin = new JTextField(14);
    private final JTextField fSsSmMax = new JTextField(14);

    // Nhom 2: TRAN — chi so cao nhat de nang toi duoc bang tiem nang.
    private final JTextField fMaxHp = new JTextField(12);
    private final JTextField fMaxDame = new JTextField(12);
    private final JTextField fMaxGiap = new JTextField(8);
    private final JTextField fMaxCrit = new JTextField(6);

    private final DefaultTableModel dtLoaiModel = new DefaultTableModel(
            new Object[]{"Mã", "Tên", "Hệ số %", "Hơn đệ thường",
                "Sức mạnh khởi điểm", "Từ trứng", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable dtLoaiTable = new JTable(dtLoaiModel);

    private JComponent buildDeTuTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        root.add(nhan("<html>Đệ tử — <b>đệ sơ sinh</b> bốc chỉ số ngẫu nhiên lúc "
                + "nhận, rồi người chơi nâng lên bằng tiềm năng cho tới <b>trần</b>. "
                + "Trần dưới đây là trần của <b>đệ thường</b>; Mabư / Cell / Bill "
                + "nhân hệ số của loại vào cả chỉ số sơ sinh và trần. Gọi đệ ở đâu "
                + "cũng như nhau — đã bỏ bậc theo bản đồ.</html>"),
                BorderLayout.NORTH);

        // ---------- trần ----------
        JPanel pTran = new JPanel(new GridBagLayout());
        pTran.setOpaque(false);
        pTran.setBorder(titled("Chỉ số đệ tử"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        y = tieuDeNhom(pTran, c, y, "Đệ sơ sinh — bốc ngẫu nhiên lúc nhận đệ");
        addRow(pTran, c, y++, "HP và KI — từ:", fSsHpMin);
        addRow(pTran, c, y++, "đến:", fSsHpMax);
        addRow(pTran, c, y++, "Sức đánh — từ:", fSsDameMin);
        addRow(pTran, c, y++, "đến:", fSsDameMax);
        addRow(pTran, c, y++, "Giáp — từ:", fSsGiapMin);
        addRow(pTran, c, y++, "đến:", fSsGiapMax);
        addRow(pTran, c, y++, "Chí mạng % — từ:", fSsCritMin);
        addRow(pTran, c, y++, "đến:", fSsCritMax);
        addRow(pTran, c, y++, "Sức mạnh — từ:", fSsSmMin);
        addRow(pTran, c, y++, "đến:", fSsSmMax);
        y = ghiChuHang(pTran, c, y, "đệ từ trứng dùng sức mạnh riêng của loại");

        y = tieuDeNhom(pTran, c, y, "Trần — nâng bằng tiềm năng tới đây là hết");
        addRow(pTran, c, y++, "HP và KI tối đa:", fMaxHp);
        addRow(pTran, c, y++, "Sức đánh tối đa:", fMaxDame);
        addRow(pTran, c, y++, "Giáp tối đa:", fMaxGiap);
        addRow(pTran, c, y++, "Chí mạng tối đa (%):", fMaxCrit);
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        JPanel bTran = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bTran.setOpaque(false);
        bTran.add(button("Lưu chỉ số", OK_GREEN, e -> dtLuuTran()));
        bTran.add(button("Dựng sẵn tất cả", new Color(0, 140, 80),
                e -> dtDungSan()));
        bTran.add(button("Tải lại", GREY, e -> dtLoadTatCa()));
        pTran.add(bTran, c);

        // ---------- loại ----------
        dtLoaiTable.setRowHeight(24);
        dtLoaiTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dtLoaiTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] wL = {45, 110, 70, 110, 140, 65, 45, 180};
        for (int i = 0; i < wL.length && i < dtLoaiTable.getColumnCount(); i++) {
            dtLoaiTable.getColumnModel().getColumn(i).setPreferredWidth(wL[i]);
        }
        dtLoaiTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dtSuaLoai(false);
                }
            }
        });
        JPanel pLoai = new JPanel(new BorderLayout(0, 4));
        pLoai.setOpaque(false);
        pLoai.setBorder(titled("Loại đệ tử"));
        pLoai.add(ServerGuiUtils.cuon(dtLoaiTable), BorderLayout.CENTER);
        JPanel bLoai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bLoai.setOpaque(false);
        bLoai.add(button("Thêm loại", OK_GREEN, e -> dtSuaLoai(true)));
        bLoai.add(button("Sửa loại", ACCENT, e -> dtSuaLoai(false)));
        bLoai.add(button("Xoá loại", WARN_RED, e -> dtXoaLoai()));
        pLoai.add(bLoai, BorderLayout.SOUTH);

        // Boc o nhap vao NORTH: GridBagLayout tu can giua theo chieu doc, de tho
        // thi cac o tro xuong giua khung va tren duoi ho hai khoang trong lon.
        JPanel traiBoc = new JPanel(new BorderLayout());
        traiBoc.setOpaque(false);
        traiBoc.add(pTran, BorderLayout.NORTH);

        javax.swing.JSplitPane tren = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT,
                ServerGuiUtils.cuon(traiBoc), pLoai);
        tren.setResizeWeight(0.35);
        tren.setBorder(null);
        root.add(tren, BorderLayout.CENTER);
        dtLoadTatCa();
        return root;
    }

    // ---------------------------------------------------------------- nạp bảng

    private void dtLoadTatCa() {
        nro.repository.dao.DeTuDAO.ChiSo t = nro.repository.dao.DeTuDAO.chiSo();
        fSsHpMin.setText(PlayerManagerPanel.fmt(t.ssHpMin));
        fSsHpMax.setText(PlayerManagerPanel.fmt(t.ssHpMax));
        fSsDameMin.setText(PlayerManagerPanel.fmt(t.ssDameMin));
        fSsDameMax.setText(PlayerManagerPanel.fmt(t.ssDameMax));
        fSsGiapMin.setText(String.valueOf(t.ssGiapMin));
        fSsGiapMax.setText(String.valueOf(t.ssGiapMax));
        fSsCritMin.setText(String.valueOf(t.ssCritMin));
        fSsCritMax.setText(String.valueOf(t.ssCritMax));
        fSsSmMin.setText(PlayerManagerPanel.fmt(t.ssSmMin));
        fSsSmMax.setText(PlayerManagerPanel.fmt(t.ssSmMax));
        fMaxHp.setText(PlayerManagerPanel.fmt(t.maxHp));
        fMaxDame.setText(PlayerManagerPanel.fmt(t.maxDame));
        fMaxGiap.setText(String.valueOf(t.maxGiap));
        fMaxCrit.setText(String.valueOf(t.maxCrit));

        dtLoaiModel.setRowCount(0);
        for (nro.repository.dao.DeTuDAO.Loai l
                : nro.repository.dao.DeTuDAO.dsLoai()) {
            dtLoaiModel.addRow(new Object[]{l.loai, nz(l.ten), gonSo(l.heSo),
                (l.heSo >= 100 ? "+" : "") + gonSo(l.heSo - 100) + "%",
                PlayerManagerPanel.fmt(l.sucManhDau),
                l.tuTrung ? "có" : "", l.bat ? "có" : "", nz(l.ghiChu)});
        }

    }

    /** Dong tieu de nhom trong mot form GridBagLayout. */
    /**
     * Hàng ghi chú chiếm <b>cả hai cột</b> của form.
     *
     * <p>Trước đây dùng {@code addRowC(form, c, y, "", new JLabel(...))}: nhãn
     * rỗng chiếm cột 0, câu ghi chú dồn vào cột 1 rồi tràn ra đè lên hàng bên
     * cạnh. Chiếm cả hai cột thì câu dài vẫn nằm gọn.</p>
     */
    private int ghiChuHang(JPanel form, GridBagConstraints c, int y, String chu) {
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        JLabel l = new JLabel("<html><body style='width:260px'>" + chu
                + "</body></html>");
        l.setForeground(GREY);
        form.add(l, c);
        c.gridwidth = 1;
        return y + 1;
    }

    private int tieuDeNhom(JPanel form, GridBagConstraints c, int y, String chu) {
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        JLabel l = new JLabel(chu);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(ACCENT);
        l.setBorder(new EmptyBorder(8, 0, 2, 0));
        form.add(l, c);
        c.gridwidth = 1;
        return y + 1;
    }

    /**
     * Đọc một ô số nguyên và <b>nói rõ ô nào</b> khi gõ sai.
     *
     * <p>Bản cũ dùng {@code laySoAnToan}, hàm này trả {@code -1} khi ô trống hoặc
     * gõ dở. Số -1 rơi xuống tầng kiểm tra và bật ra câu "không được âm" — người
     * dùng nhìn mười bốn cái ô mà không biết ô nào hỏng.</p>
     *
     * @throws IllegalArgumentException kèm tên ô, chỗ gọi bắt và hiện lên
     */
    private static int docO(JTextField o, String ten) {
        String t = o.getText() == null ? "" : o.getText()
                .replace(".", "").replace(",", "").replace("%", "").trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Ô \"" + ten + "\" chưa nhập số.");
        }
        try {
            int v = Integer.parseInt(t);
            if (v < 0) {
                throw new IllegalArgumentException(
                        "Ô \"" + ten + "\" không được âm.");
            }
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Ô \"" + ten + "\" phải là số nguyên.");
        }
    }

    /** Như {@link #docO} nhưng cho số dài. */
    private static long docOLon(JTextField o, String ten) {
        String t = o.getText() == null ? "" : o.getText()
                .replace(".", "").replace(",", "").replace(" ", "").trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Ô \"" + ten + "\" chưa nhập số.");
        }
        try {
            long v = Long.parseLong(t);
            if (v < 0) {
                throw new IllegalArgumentException(
                        "Ô \"" + ten + "\" không được âm.");
            }
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Ô \"" + ten + "\" phải là số nguyên.");
        }
    }

    private void dtLuuTran() {
        nro.repository.dao.DeTuDAO.ChiSo t =
                new nro.repository.dao.DeTuDAO.ChiSo();
        try {
            t.ssHpMin = docO(fSsHpMin, "Sơ sinh · HP và KI — từ");
            t.ssHpMax = docO(fSsHpMax, "Sơ sinh · HP và KI — đến");
            t.ssDameMin = docO(fSsDameMin, "Sơ sinh · Sức đánh — từ");
            t.ssDameMax = docO(fSsDameMax, "Sơ sinh · Sức đánh — đến");
            t.ssGiapMin = docO(fSsGiapMin, "Sơ sinh · Giáp — từ");
            t.ssGiapMax = docO(fSsGiapMax, "Sơ sinh · Giáp — đến");
            t.ssCritMin = docO(fSsCritMin, "Sơ sinh · Chí mạng — từ");
            t.ssCritMax = docO(fSsCritMax, "Sơ sinh · Chí mạng — đến");
            t.ssSmMin = docOLon(fSsSmMin, "Sơ sinh · Sức mạnh — từ");
            t.ssSmMax = docOLon(fSsSmMax, "Sơ sinh · Sức mạnh — đến");
            t.maxHp = docO(fMaxHp, "Trần · HP và KI tối đa");
            t.maxDame = docO(fMaxDame, "Trần · Sức đánh tối đa");
            t.maxGiap = docO(fMaxGiap, "Trần · Giáp tối đa");
            t.maxCrit = docO(fMaxCrit, "Trần · Chí mạng tối đa");
        } catch (IllegalArgumentException ex) {
            note(WARN_RED, ex.getMessage());
            return;
        }
        String loi = nro.repository.dao.DeTuDAO.luuChiSo(t);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        dtLoadTatCa();
        note(OK_GREEN, "Đã lưu. Chỉ số sơ sinh áp cho đệ tạo mới; trần áp ngay "
                + "cho cả đệ đang có.");
    }

    /**
     * Dựng sẵn toàn bộ bảng đúng như số đang chạy trong mã.
     *
     * <p>Không bắt người dùng gõ lại chín bậc và bốn loại: bấm một cái là có đủ,
     * rồi sửa dòng nào muốn đổi. Ghi đè những dòng cùng khoá — nên bấm lại là
     * <b>trả về mặc định</b>, mất phần đã sửa tay.</p>
     */
    private void dtDungSan() {
        if (JOptionPane.showConfirmDialog(this,
                "Dựng lại toàn bộ theo số mặc định?\n"
                + "Khoảng chỉ số và các loại đã sửa tay sẽ bị ghi đè.",
                "Dựng sẵn bảng đệ tử", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }
        nro.repository.dao.DeTuDAO.luuChiSo(
                new nro.repository.dao.DeTuDAO.ChiSo());

        // Bon loai: thuong, Mabu, Cell, Bill. He so nhan don 5% moi bac.
        int[] maLoai = {nro.core.consts.ConstDetu.NORMAL,
            nro.core.consts.ConstDetu.MABU,
            nro.core.consts.ConstDetu.CELL,
            nro.core.consts.ConstDetu.BILL};
        for (int ma : maLoai) {
            nro.repository.dao.DeTuDAO.Loai l =
                    new nro.repository.dao.DeTuDAO.Loai();
            l.loai = ma;
            l.ten = nro.core.consts.ConstDetu.tenLoai((byte) ma);
            l.heSo = nro.core.consts.ConstDetu.heSoChiSo((byte) ma) * 100d;
            l.tuTrung = ma != nro.core.consts.ConstDetu.NORMAL;
            l.sucManhDau = l.tuTrung
                    ? nro.core.consts.ConstDetu.SUC_MANH_TRUNG : 0;
            nro.repository.dao.DeTuDAO.luuLoai(l);
        }

        dtLoadTatCa();
        note(OK_GREEN, "Đã dựng lại khoảng chỉ số và 4 loại đệ.");
    }

    // ------------------------------------------------------------- hộp thoại

    private void dtSuaLoai(boolean them) {
        int r = dtLoaiTable.getSelectedRow();
        if (!them && r < 0) {
            note(WARN_RED, "Chọn một loại trước.");
            return;
        }
        nro.repository.dao.DeTuDAO.Loai cu = null;
        if (!them) {
            int ma = intOf(dtLoaiModel.getValueAt(
                    dtLoaiTable.convertRowIndexToModel(r), 0));
            for (nro.repository.dao.DeTuDAO.Loai x
                    : nro.repository.dao.DeTuDAO.dsLoai()) {
                if (x.loai == ma) {
                    cu = x;
                    break;
                }
            }
        }
        JTextField fMa = new JTextField(cu == null ? "" : String.valueOf(cu.loai), 6);
        JTextField fTen = new JTextField(cu == null ? "" : nz(cu.ten), 20);
        JTextField fHeSo = new JTextField(cu == null ? "100" : gonSo(cu.heSo), 8);
        JTextField fSm = new JTextField(cu == null ? "0"
                : PlayerManagerPanel.fmt(cu.sucManhDau), 16);
        javax.swing.JCheckBox cbTrung = new javax.swing.JCheckBox(
                "Nhận bằng trứng (dùng sức mạnh khởi điểm thay vì bậc map)",
                cu != null && cu.tuTrung);
        javax.swing.JCheckBox cbBat = new javax.swing.JCheckBox("Bật",
                cu == null || cu.bat);
        JTextField fGhi = new JTextField(cu == null ? "" : nz(cu.ghiChu), 24);
        fMa.setEditable(them);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;
        addRow(form, c, y++, "Mã loại:", fMa);
        addRow(form, c, y++, "Tên:", fTen);
        addRow(form, c, y++, "Hệ số chỉ số (%):", fHeSo);
        y = ghiChuHang(form, c, y, "100 = như đệ thường, 105 = hơn 5%");
        addRow(form, c, y++, "Sức mạnh khởi điểm:", fSm);
        addRowC(form, c, y++, "", cbTrung);
        addRow(form, c, y++, "Ghi chú:", fGhi);
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm loại đệ" : "Sửa loại đệ",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        nro.repository.dao.DeTuDAO.Loai moi = new nro.repository.dao.DeTuDAO.Loai();
        moi.loai = laySoAnToan(fMa.getText());
        moi.ten = fTen.getText().trim();
        try {
            moi.heSo = docSoThuc(fHeSo.getText(), "Hệ số");
        } catch (IllegalArgumentException ex) {
            note(WARN_RED, ex.getMessage());
            return;
        }
        moi.sucManhDau = laySoLonAnToan(fSm.getText());
        moi.tuTrung = cbTrung.isSelected();
        moi.bat = cbBat.isSelected();
        moi.ghiChu = fGhi.getText().trim();
        String loi = nro.repository.dao.DeTuDAO.luuLoai(moi);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        dtLoadTatCa();
        note(OK_GREEN, "Đã lưu loại đệ " + moi.loai + ".");
    }

    private void dtXoaLoai() {
        int r = dtLoaiTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một loại trước.");
            return;
        }
        int ma = intOf(dtLoaiModel.getValueAt(
                dtLoaiTable.convertRowIndexToModel(r), 0));
        String loi = nro.repository.dao.DeTuDAO.xoaLoai(ma);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        dtLoadTatCa();
        note(OK_GREEN, "Đã xoá loại " + ma + " — loại này quay về hệ số trong mã.");
    }

    /** Ô chỉ đọc kèm nút "Chọn…" cho một ô chứa CHUỖI (danh sách map). */
    private JPanel oChonSo2(JTextField o,
            java.util.function.Supplier<String> chon) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        o.setEditable(false);
        p.add(o, BorderLayout.CENTER);
        p.add(button("Chọn…", ACCENT, e -> {
            String moi = chon.get();
            if (moi != null) {
                o.setText(moi);
            }
        }), BorderLayout.EAST);
        return p;
    }

    /** Đọc số dài từ ô, chịu được dấu chấm phân nhóm; sai thì trả 0. */
    private static long laySoLonAnToan(String s) {
        if (s == null) {
            return 0L;
        }
        String t = s.replace(".", "").replace(",", "").replace(" ", "").trim();
        if (t.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(t);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private void ctdXoa() {
        int r = ctdTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chưa chọn công thức nào.");
            return;
        }
        int idx = ctdTable.convertRowIndexToModel(r);
        String ma = nz(ctdModel.getValueAt(idx, 1));
        if (JOptionPane.showConfirmDialog(this,
                "Xoá công thức " + ma + " ?"
                + System.lineSeparator() + System.lineSeparator()
                + "Chức năng gọi mã này trong game sẽ báo đang tạm khoá thay vì chạy.",
                "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.CongThucDoiDAO.xoa(
                intOf(ctdModel.getValueAt(idx, 0)));
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        loadCongThuc();
        note(OK_GREEN, "Đã xoá công thức " + ma + ".");
    }
    private JComponent buildCheckTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        checkTable.setRowHeight(24);
        checkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        checkTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        checkTable.setAutoCreateRowSorter(true);
        int[] w = {230, 55, 220, 200, 420};
        for (int i = 0; i < w.length && i < checkTable.getColumnCount(); i++) {
            checkTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        root.add(ServerGuiUtils.cuon(checkTable), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(button("Quét lại", ACCENT, e -> loadCheck()));
        lblCheck.setForeground(GREY);
        top.add(lblCheck);
        root.add(top, BorderLayout.NORTH);

        JLabel hint = new JLabel("<html><span style='color:#777'>"
                + "Đây là lỗi <b>dữ liệu</b>, không phải lỗi mã nguồn — máy chủ không có "
                + "cách nào tự sửa vì không có sẵn hình.<br>"
                + "Cách xử lý: import phần dữ liệu còn thiếu vào bảng <code>part</code>, "
                + "hoặc gỡ vật phẩm khỏi shop cho tới khi có hình."
                + "</span></html>");
        root.add(hint, BorderLayout.SOUTH);

        loadCheck();
        return root;
    }

    private void loadCheck() {
        checkModel.setRowCount(0);
        java.util.List<nro.repository.dao.DataCheckDAO.Issue> ds;
        try {
            ds = nro.repository.dao.DataCheckDAO.quet();
        } catch (Exception ex) {
            lblCheck.setText("  Lỗi quét: " + ex.getMessage());
            return;
        }
        for (nro.repository.dao.DataCheckDAO.Issue i : ds) {
            checkModel.addRow(new Object[]{i.loai, i.itemId, i.itemName, i.chiTiet, i.goiY});
        }
        lblCheck.setForeground(ds.isEmpty() ? OK_GREEN : WARN_RED);
        lblCheck.setText(ds.isEmpty()
                ? "  Không tìm thấy vấn đề nào."
                : "  Tìm thấy " + ds.size() + " vấn đề.");
    }

    // =====================================================================
    //  Tiện ích
    // =====================================================================

    /**
     * Như {@link #addRow} nhưng nhận thành phần bất kỳ.
     *
     * <p>Để trống {@code label} khi dòng này là chú thích cho dòng ngay trên.</p>
     */
    private static void addRowC(JPanel form, GridBagConstraints c, int y,
                                String label, java.awt.Component comp) {
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0;
        c.gridwidth = 1;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(comp, c);
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

    /**
     * Bỏ tiền tố {@code $[n]} khỏi tên chỉ số để hiện lại đúng chữ admin đã gõ.
     *
     * <p>Tên lưu trong CSDL là {@code "$[5] Tăng mạnh chí mạng"}, nhưng ô nhập
     * chỉ nên hiện {@code "Tăng mạnh chí mạng"} — bắt người dùng gõ lại cả phần
     * {@code $[5]} là mời họ gõ sai.</p>
     */
    private static String boTienTo(String ten) {
        // Giu lai de doc duoc du lieu luu theo dang cu ("$[5] ..." / "$(...)").
        if (ten == null) {
            return "";
        }
        String t = ten.trim();
        // Ma mau "|0|" / "|-1|" o dau chuoi — client doc de chon phong chu.
        t = t.replaceFirst("^\\|-?\\d+\\|", "").trim();
        // Dang cu boc trong "$( ... )" — bo ca hai dau ngoac.
        if (t.startsWith("$(") && t.endsWith(")")) {
            t = t.substring(2, t.length() - 1).trim();
        }
        // Dang cu "$[5] ..." va ky tu $ le.
        t = t.replaceFirst("^\\$\\[\\d+\\]\\s*", "")
                .replaceFirst("^\\$", "").trim();
        // Phan moc o dau cau — do panel tu ghep, hoac do admin tu go tay.
        // Bo di de o nhap chi hien phan noi dung, va de ghep lai khong nhan doi.
        return t.replaceFirst("^\\d+\\s*món\\s*:?\\s*", "").trim();
    }

    /**
     * Đặt lại {@code type} cho <b>mọi</b> dòng chữ mô tả của mọi set, theo
     * {@code skh_mota_type} bên tab Quy ước.
     *
     * <h3>Vì sao cần một nút riêng</h3>
     *
     * <p>Màu chữ trên món đồ do client quyết định từ {@code type}, và không đọc
     * được mã client nên phải thử. Không có nút này thì mỗi lần đổi số lại phải
     * mở từng set bấm OK từng cái — có ba set là chín lần bấm.</p>
     *
     * <p>Chỉ sửa các dòng <b>đang được set trỏ tới</b>; không đụng chỉ số gốc
     * nào của game.</p>
     */
    private void apKieuDongMoTa() {
        int loai = (int) ConfigDAO.num(ConfigDAO.SKH_MOTA_TYPE);
        java.util.Map<Integer, Integer> mocCua = new java.util.HashMap<>();
        for (nro.repository.dao.SetBonusDAO.DinhNghia d
                : nro.repository.dao.SetBonusDAO.dinhNghia().values()) {
            for (Map.Entry<Integer, Integer> e
                    : nro.repository.dao.SetBonusDAO.docMoTaDong(d.moTaDong).entrySet()) {
                mocCua.put(e.getValue(), e.getKey());
            }
        }
        if (mocCua.isEmpty()) {
            note(WARN_RED, "Chưa set nào có dòng chữ mô tả để áp kiểu.");
            return;
        }
        int xong = 0;
        for (Map.Entry<Integer, Integer> e : mocCua.entrySet()) {
            int id = e.getKey();
            String ten = OptionPicker.tenChiSo(id);
            if (ten == null || ten.isEmpty()) {
                continue;
            }
            String tenMoi = e.getValue() + " món: " + boTienTo(ten);
            if (nro.repository.dao.ChiSoOptionDAO.doiTen(id, tenMoi, loai)) {
                xong++;
            }
        }
        note(xong > 0 ? OK_GREEN : WARN_RED,
                "Đã đặt kiểu " + loai + " cho " + xong + " dòng mô tả. "
                + "Vào lại game để xem màu — client tự tải lại tên chỉ số.");
    }

    /**
     * Bật hoặc tắt <b>mọi set đang chọn</b> trong danh sách.
     *
     * <p>Bảng cho chọn nhiều dòng, nên tắt cả loạt set thử nghiệm chỉ mất một
     * lần bấm thay vì mở từng hộp sửa.</p>
     *
     * <p>Tắt set nghĩa là mọi hiệu ứng của nó ngừng áp dụng, và đồ rơi từ quái
     * không còn bốc trúng set đó nữa. <b>Không</b> xoá gì cả — chỉ số nhận diện
     * và các dòng chữ vẫn nguyên, bật lại là chạy tiếp.</p>
     */
    private void datBatSet(boolean bat) {
        int[] dong = dsSetTable.getSelectedRows();
        if (dong.length == 0) {
            note(WARN_RED, "Chưa chọn set nào. Giữ Ctrl hoặc Shift để chọn nhiều set.");
            return;
        }
        int xong = 0;
        int boQua = 0;
        String loi = null;
        for (int r : dong) {
            String key = String.valueOf(dsSetModel.getValueAt(
                    dsSetTable.convertRowIndexToModel(r), 4));
            nro.repository.dao.SetBonusDAO.DinhNghia d =
                    nro.repository.dao.SetBonusDAO.dinhNghia().get(key);
            if (d == null) {
                continue;
            }
            if (d.active == bat) {
                boQua++;      // da o dung trang thai roi
                continue;
            }
            d.active = bat;
            String e = nro.repository.dao.SetBonusDAO.luuDinhNghia(d);
            if (e != null) {
                loi = e;
            } else {
                xong++;
            }
        }
        napDanhSachSet();
        loadSet();
        if (loi != null) {
            note(WARN_RED, "Có set không lưu được: " + loi);
            return;
        }
        note(OK_GREEN, "Đã " + (bat ? "bật " : "tắt ") + xong + " set"
                + (boQua > 0 ? " (" + boQua + " set đã ở trạng thái đó)" : "") + ".");
    }

    /** Số trong ô, hoặc {@code -1} nếu ô rỗng / không phải số. */
    private static int laySoAnToan(String raw) {
        try {
            return Integer.parseInt(raw == null ? "" : raw.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Đổi {@code rateNum/rateDen} thành chuỗi phần trăm để hiện trong bảng.
     *
     * <p>CSDL vẫn lưu dạng phân số vì đó là dạng {@code Util.isTrue()} dùng để
     * quay. Phần trăm chỉ là cách <b>hiện và nhập</b> — người dùng nghĩ bằng
     * phần trăm, không ai nghĩ bằng {@code 1000/100000}.</p>
     */
    private static String phanTramRoi(int num, int den) {
        if (den <= 0) {
            return "0";
        }
        double pct = num * 100.0 / den;
        // Bo duoi ",0" cho so tron, giu ba chu so thap phan cho ti le hiem.
        String s2 = String.format(java.util.Locale.US, "%.3f", pct);
        while (s2.contains(".") && (s2.endsWith("0") || s2.endsWith("."))) {
            s2 = s2.substring(0, s2.length() - 1);
        }
        return s2;
    }

    /**
     * Hộp khai phạm vi bản đồ cho một món rơi.
     *
     * <p>Ba lựa chọn: mọi map, chỉ những map đã khai, hoặc mọi map trừ những
     * map đã khai. Danh sách nhận cả <b>nhóm sống</b> — cả một hành tinh, hoặc
     * mọi map có tên chứa một chữ nào đó — nên khai "trừ mọi map tên có chữ
     * băng" một lần là xong, map băng thêm về sau tự nằm trong nhóm.</p>
     *
     * @return chuỗi {@code kiểu|danh sách}, hoặc {@code null} nếu huỷ
     */
    private String chonPhamViMap(java.awt.Component cha, int kieuCu, String dsCu) {
        final java.util.LinkedHashSet<String> muc = new java.util.LinkedHashSet<>();
        if (dsCu != null) {
            for (String p : dsCu.split(",")) {
                if (!p.trim().isEmpty()) {
                    muc.add(p.trim());
                }
            }
        }

        JComboBox<String> cbKieu = new JComboBox<>(new String[]{
            "Rơi ở mọi bản đồ",
            "CHỈ rơi ở những mục dưới đây",
            "Rơi khắp nơi TRỪ những mục dưới đây"});
        cbKieu.setSelectedIndex(Math.max(0, Math.min(2, kieuCu)));

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Mục", "Nghĩa là"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.getColumnModel().getColumn(0).setPreferredWidth(150);
        t.getColumnModel().getColumn(1).setPreferredWidth(400);

        final JLabel lblDem = new JLabel();
        lblDem.setForeground(GREY);

        final Runnable ve = () -> {
            m.setRowCount(0);
            for (String x : muc) {
                m.addRow(new Object[]{x, nghiaCuaMuc(x)});
            }
            boolean coLoc = cbKieu.getSelectedIndex() != 0;
            t.setEnabled(coLoc);
            lblDem.setText(coLoc
                    ? "  " + muc.size() + " mục — khớp " + soMapKhop(muc) + " bản đồ"
                    : "  Không lọc, món này rơi ở mọi bản đồ.");
        };
        cbKieu.addActionListener(e -> ve.run());

        JPanel nut = new JPanel(new GridLayout(0, 4, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm bản đồ…", ACCENT, e -> {
            String them = nro.ui.MapPicker.chon(t, "");
            if (them != null) {
                for (String p : them.split(",")) {
                    if (!p.trim().isEmpty()) {
                        muc.add(p.trim());
                    }
                }
                ve.run();
            }
        }));
        nut.add(button("Thêm cả hành tinh…", ACCENT, e -> {
            Object ht = JOptionPane.showInputDialog(t, "Chọn hành tinh:",
                    "Thêm nhóm", JOptionPane.PLAIN_MESSAGE, null,
                    new String[]{"0 — Trái Đất", "1 — Namếc", "2 — Xayda"}, null);
            if (ht != null) {
                muc.add("ht:" + String.valueOf(ht).charAt(0));
                ve.run();
            }
        }));
        nut.add(button("Thêm theo tên…", ACCENT, e -> {
            String tu = JOptionPane.showInputDialog(t,
                    "Mọi bản đồ có tên chứa chữ này sẽ thuộc nhóm.\n"
                    + "Ví dụ: băng, tuyết, Hành tinh", "Thêm nhóm theo tên",
                    JOptionPane.PLAIN_MESSAGE);
            if (tu != null && !tu.trim().isEmpty()) {
                muc.add("tk:" + tu.trim());
                ve.run();
            }
        }));
        nut.add(button("Bỏ mục đang chọn", WARN_RED, e -> {
            int r = t.getSelectedRow();
            if (r < 0) {
                return;
            }
            muc.remove(String.valueOf(m.getValueAt(r, 0)));
            ve.run();
        }));

        JPanel tren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tren.setOpaque(false);
        tren.add(new JLabel("Phạm vi:"));
        tren.add(cbKieu);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(620, 400));
        root.add(tren, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);
        JPanel duoi = new JPanel(new BorderLayout(0, 4));
        duoi.setOpaque(false);
        duoi.add(lblDem, BorderLayout.NORTH);
        duoi.add(nut, BorderLayout.SOUTH);
        root.add(duoi, BorderLayout.SOUTH);

        ve.run();
        if (JOptionPane.showConfirmDialog(cha, root, "Phạm vi bản đồ",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        return cbKieu.getSelectedIndex() + "|" + String.join(",", muc);
    }

    /** Giải nghĩa một mục trong danh sách phạm vi, kèm số bản đồ nó khớp. */
    private String nghiaCuaMuc(String muc) {
        if (muc.startsWith("ht:")) {
            String ten;
            switch (muc.substring(3).trim()) {
                case "0":
                    ten = "Trái Đất";
                    break;
                case "1":
                    ten = "Namếc";
                    break;
                case "2":
                    ten = "Xayda";
                    break;
                default:
                    ten = "hành tinh " + muc.substring(3);
                    break;
            }
            return "cả hành tinh " + ten + " — " + demKhop(muc) + " bản đồ";
        }
        if (muc.startsWith("tk:")) {
            return "mọi bản đồ có tên chứa \"" + muc.substring(3) + "\" — "
                    + demKhop(muc) + " bản đồ";
        }
        nro.entity.template.MapTemplate mt = mauMapTheoId(muc);
        return mt == null ? "không có bản đồ id này" : mt.name;
    }

    /** Số bản đồ khớp một mục. */
    private int demKhop(String muc) {
        int n = 0;
        nro.entity.template.MapTemplate[] ds = nro.server.Manager.MAP_TEMPLATES;
        if (ds == null) {
            return 0;
        }
        nro.repository.dao.SuKienDAO.VatPhamRoi thu
                = new nro.repository.dao.SuKienDAO.VatPhamRoi();
        thu.kieuMap = nro.repository.dao.SuKienDAO.VatPhamRoi.MAP_CHI;
        thu.dsMap = muc;
        for (nro.entity.template.MapTemplate mt : ds) {
            if (mt != null && thu.roiOMap(mt.id, mt.planetId, mt.name)) {
                n++;
            }
        }
        return n;
    }

    /** Tổng số bản đồ khớp cả danh sách, không đếm trùng. */
    private int soMapKhop(java.util.Collection<String> muc) {
        nro.entity.template.MapTemplate[] ds = nro.server.Manager.MAP_TEMPLATES;
        if (ds == null || muc.isEmpty()) {
            return 0;
        }
        nro.repository.dao.SuKienDAO.VatPhamRoi thu
                = new nro.repository.dao.SuKienDAO.VatPhamRoi();
        thu.kieuMap = nro.repository.dao.SuKienDAO.VatPhamRoi.MAP_CHI;
        thu.dsMap = String.join(",", muc);
        int n = 0;
        for (nro.entity.template.MapTemplate mt : ds) {
            if (mt != null && thu.roiOMap(mt.id, mt.planetId, mt.name)) {
                n++;
            }
        }
        return n;
    }

    private nro.entity.template.MapTemplate mauMapTheoId(String so) {
        nro.entity.template.MapTemplate[] ds = nro.server.Manager.MAP_TEMPLATES;
        if (ds == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(so.trim());
            for (nro.entity.template.MapTemplate mt : ds) {
                if (mt != null && mt.id == id) {
                    return mt;
                }
            }
        } catch (NumberFormatException boQua) {
            // Muc go sai thi coi nhu khong co ban do nao.
        }
        return null;
    }

    // =====================================================================
    //  Sửa nhiều boss một lượt
    // =====================================================================

    /**
     * Đặt lại vài ô cấu hình cho <b>mọi</b> boss đang được chọn.
     *
     * <p>Mỗi ô có một dấu tích riêng: <b>chỉ ô nào được tích mới bị ghi đè</b>,
     * ô không tích thì từng boss giữ nguyên giá trị cũ của nó. Nếu ghi đè cả
     * những ô để trống thì bấm một cái là xoá sạch cấu hình riêng của vài chục
     * con, mà không có đường lùi.</p>
     */

    /** Một hàng trong hộp sửa nhiều: dấu tích, ô nhập, lời chú. */

    // =====================================================================
    //  Số liệu boss (bảng boss_data)
    // =====================================================================

    private final DefaultTableModel bdModel = new DefaultTableModel(
            new Object[]{"Mã", "Tên", "ID boss", "Thuộc boss", "Số con",
                "Nhóm hồi sinh", "Hệ", "Sức đánh", "HP", "Giáp", "Né %",
                "Chính xác %", "Bản đồ", "Kỹ năng", "Hồi sinh (giây)"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    // Chi so cot cua bang so lieu. Dat ten vi bang nay da bi chen them cot
    // hai lan, va lan nao cung co cho doc nham o ma khong bao gi.
    private static final int COT_BD_MA = 0;
    private static final int COT_BD_ID_BOSS = 2;
    private static final int COT_BD_THUOC = 3;

    private final JTable bdTable = new JTable(bdModel);
    private final JTextField fBdTim = new JTextField(20);

    /**
     * Lọc bảng số liệu.
     *
     * <p>Hai loại: mẫu đang được một con boss trong máy chủ dùng, và mẫu chưa
     * con nào dùng. Đổ hết 196 mẫu ra một bảng thì toàn tên lạ — phần lớn là
     * quân nhỏ đi kèm (XenCon, DeathBeam, ChienBinh) và mẫu chưa đưa vào game.</p>
     */
    private final JComboBox<String> cbBdLoc = new JComboBox<>(new String[]{
        "Đã có trong game", "Chưa có trong game", "Tất cả"});
    private final JLabel lblBdDem = new JLabel();

    /**
     * Màn sửa số liệu boss.
     *
     * <p>Đây là phần <b>số liệu</b> đã đưa ra khỏi mã nguồn: tên, hệ, trang
     * phục, sức đánh, máu, bản đồ, kỹ năng, lời thoại, giây hồi sinh. Phần
     * <b>hành vi</b> — cách đổi phase, kỹ năng riêng, phản ứng theo tình huống
     * — vẫn nằm trong 140 lớp Java và không đưa xuống CSDL được, vì đó là mã
     * chứ không phải số.</p>
     *
     * <p>Số ở đây là số <b>gốc</b> của mẫu. Tab trên còn một lớp đè theo từng
     * con trong {@code boss_config}; con nào có dòng đè thì số đè thắng.</p>
     */
    private JComponent buildBossDataTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 4, 4, 4));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        top.add(new JLabel("Xem:"));
        cbBdLoc.setPreferredSize(new Dimension(260, 26));
        top.add(cbBdLoc);
        top.add(new JLabel("   Tìm theo mã hoặc tên:"));
        top.add(fBdTim);
        lblBdDem.setForeground(GREY);
        top.add(lblBdDem);
        JLabel chu = new JLabel("  Sửa xong phải khởi động lại máy chủ mới có hiệu lực.");
        chu.setForeground(GREY);
        top.add(chu);
        root.add(top, BorderLayout.NORTH);

        bdTable.setRowHeight(22);
        bdTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bdTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        bdTable.setAutoCreateRowSorter(true);
        // Tat tu co gian cot: mac dinh JTable nen cac cot lai cho vua khung, nen
        // bang muoi lam cot thi cot nao cung hep den muc doc khong ra, va thanh
        // cuon ngang khong bao gio hien. Tat di thi cot giu dung be ngang duoi
        // day va JScrollPane bao boc tu co thanh cuon ngang.
        bdTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] w = {120, 120, 60, 150, 90, 130, 50, 95, 115, 80, 60, 85, 95, 145, 75};
        for (int i = 0; i < bdTable.getColumnCount() && i < w.length; i++) {
            bdTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        bdTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    bdSua();
                }
            }
        });
        root.add(ServerGuiUtils.cuon(bdTable), BorderLayout.CENTER);

        JPanel nut = new JPanel(new GridLayout(0, 5, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Tạo boss từ cải trang", OK_GREEN,
                e -> taoBossTuCaiTrang(this)));
        nut.add(button("Thêm vào game", new Color(0, 140, 80),
                e -> bdThemVaoGame()));
        nut.add(button("Sửa số liệu", ACCENT, e -> bdSua()));
        nut.add(button("Sửa số con", ACCENT, e -> bdSuaSoCon()));
        nut.add(button("Xoá mẫu", WARN_RED, e -> bdXoa()));
        nut.add(button("Tải lại", GREY, e -> napBossData()));
        root.add(nut, BorderLayout.SOUTH);

        fBdTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                napBossData();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                napBossData();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                napBossData();
            }
        });
        cbBdLoc.addActionListener(e -> napBossData());
        napBossData();
        return root;
    }

    private void napBossData() {
        bdModel.setRowCount(0);
        String tim = fBdTim.getText().trim().toLowerCase();
        java.util.Map<String, String> phase = banDoPhase();
        java.util.Map<String, String> bossIdCuaMau = banDoBossId();
        java.util.Map<String, Integer> demTheoIdBoss = demBossTheoId();
        java.util.Map<String, Integer> khaiTheoIdBoss = new java.util.HashMap<>();
        java.util.Map<String, String> tenNhomTheoIdBoss = new java.util.HashMap<>();
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            khaiTheoIdBoss.put(String.valueOf(d.bossId), d.soBanSao);
            tenNhomTheoIdBoss.put(String.valueOf(d.bossId),
                    d.nhomId <= 0 ? "—" : tenNhom(d.nhomId));
        }
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT ma, ten, gioi_tinh, suc_danh, hp, map_join, ky_nang,"
                    + " giay_hoi_sinh, kieu_xuat_hien FROM boss_data ORDER BY ma");
            while (rs.next()) {
                String ma = rs.getString("ma");
                String ten = rs.getString("ten");
                String thuoc = phase.getOrDefault(ma, "");
                // "Da co trong game" = co con boss THAT dang cam mau nay VA con
                // do thuoc DANH SACH o tab 1. Boss chi mọc theo ban do (Tau Pay
                // Pay, Mabu, Drabura...) quan o tab 4, khong lam roi bang nay.
                //
                // Truoc day suy tu cot kieu_xuat_hien — sai han: TAN_BINH,
                // TAP_SU, XEN_CON deu khong phai DEFAULT_APPEAR nen bi bao la
                // da co, trong khi khong con nao dung chung.
                if (!thuoc.isEmpty()
                        && !khaiTheoIdBoss.containsKey(
                                bossIdCuaMau.getOrDefault(ma, ""))) {
                    thuoc = "";
                }

                int loc = Math.max(0, cbBdLoc.getSelectedIndex());
                // 0 = chi mau dang co boss cam; 1 = tat ca; 2 = mau khong ai dung
                if (loc == 0 && thuoc.isEmpty()) {
                    continue;
                }
                if (loc == 1 && !thuoc.isEmpty()) {
                    continue;
                }
                if (!tim.isEmpty()
                        && !ma.toLowerCase().contains(tim)
                        && !(ten == null ? "" : ten.toLowerCase()).contains(tim)) {
                    continue;
                }
                String idBoss = bossIdCuaMau.getOrDefault(ma, "");
                bdModel.addRow(new Object[]{ma, ten, idBoss, thuoc,
                    moTaSoCon(idBoss, demTheoIdBoss, khaiTheoIdBoss),
                    tenNhomTheoIdBoss.getOrDefault(idBoss, "—"),
                    PlayerManagerPanel.itemGender((byte) rs.getInt("gioi_tinh")),
                    PlayerManagerPanel.fmt(rs.getLong("suc_danh")),
                    nhieuSoDep(rs.getString("hp")),
                    soHoacGach(rs, "giap"),
                    soHoacGach(rs, "ne_don"),
                    soHoacGach(rs, "chinh_xac"),
                    rs.getString("map_join"),
                    rs.getString("ky_nang"),
                    moTaHoiSinh(idBoss, rs.getInt("giay_hoi_sinh"))});
            }
            rs.dispose();
            lblBdDem.setText("  " + PlayerManagerPanel.fmt(bdModel.getRowCount())
                    + " mẫu");
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Không đọc được bảng boss_data");
            note(WARN_RED, "Không đọc được bảng boss_data — xem log máy chủ.");
        }
    }

    /**
     * Đọc một ô số của {@code boss_data}, {@code -1} thì ghi dấu gạch.
     *
     * <p>Ba cột giáp / né / chính xác dùng {@code -1} với nghĩa "không đặt", khác
     * hẳn {@code 0} — né 0 là đã tắt né hẳn cho con đó. Hiện thẳng số -1 lên bảng
     * thì người đọc tưởng là chỉ số âm.</p>
     */
    private static String soHoacGach(nro.repository.CrisResultSet rs, String cot) {
        try {
            int v = rs.getInt(cot);
            return v < 0 ? "—" : PlayerManagerPanel.fmt(v);
        } catch (Exception thieuCot) {
            return "—";
        }
    }

    /**
     * Giá trị hiện lên ô nhập cho một cột "có thể không đặt".
     *
     * <p>{@code -1} trong CSDL nghĩa là không đặt, nên ô để trống. Hiện thẳng -1
     * thì người dùng bấm Lưu là ghi lại -1 mà tưởng mình vừa đặt chỉ số âm.</p>
     */
    private static String soTrongODat(nro.repository.CrisResultSet rs, String cot) {
        try {
            int v = rs.getInt(cot);
            return v < 0 ? "" : PlayerManagerPanel.fmt(v);
        } catch (Exception thieuCot) {
            return "";
        }
    }

    /**
     * Đọc một ô "có thể để trống" thành số để ghi CSDL.
     *
     * @return {@code -1} nếu ô trống, nghĩa là giữ nguyên chỉ số gốc
     * @throws IllegalArgumentException nếu gõ sai hoặc ngoài khoảng — chỗ gọi đã
     *         bắt loại này và hiện hộp "Nhập chưa đúng"
     */
    private static int oDatSo(String raw, String ten, int min, int max) {
        String t = raw == null ? "" : boCham(raw).trim();
        if (t.isEmpty()) {
            return -1;
        }
        int v;
        try {
            v = Integer.parseInt(t);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ten + " phải là số nguyên, hoặc"
                    + " để trống để giữ nguyên chỉ số gốc.");
        }
        if (v < min || v > max) {
            throw new IllegalArgumentException(ten + " phải từ " + min
                    + " đến " + max + ".");
        }
        return v;
    }

    private void bdSua() {
        int r = bdTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng trong bảng trước.");
            return;
        }
        bdSuaTheoMa(String.valueOf(bdTable.getValueAt(r, 0)), bdSelectedBossId());
    }

    /**
     * Sửa số liệu của MỘT mẫu boss, chỉ định thẳng bằng mã.
     *
     * <p>Tách ra khỏi {@code bdSua()} để hộp "Thêm boss vào game" gọi lại được:
     * ở đó có những mã <b>chưa có dòng nào</b> trong {@code boss_data}, mà bắt
     * người dùng sang tab khác tìm đúng dòng rồi mới sửa thì vô lý — nhất là khi
     * dòng đó còn chưa tồn tại.</p>
     */
    private void bdSuaTheoMa(String ma, int idBossChiDinh) {

        JTextField fTen = new JTextField(24);
        JComboBox<String> cbHe = new JComboBox<>(new String[]{
            "0 — Trái Đất", "1 — Namếc", "2 — Xayda"});
        // Cach xuat hien nam san trong cot kieu_xuat_hien nhung truoc gio chua
        // cho sua. Day chinh la thu quyet dinh mot mau tu hoi sinh hay phai cho
        // con khac goi.
        JComboBox<String> cbXH = new JComboBox<>(NHAN_XUAT_HIEN);

        // Nhom hoi sinh nam o bang boss_spawn (khoa theo id boss), khong phai
        // boss_data. Van cho sua ngay day vi day la cho nguoi dung sua moi thu
        // khac cua con boss — bat ho di tim mot hop thoai khac chi de doi nhom
        // la vo ly.
        final int idBossCuaMau = idBossChiDinh;
        final java.util.List<nro.service.boss.NhomBossService.Nhom> dsNhomSua
                = nro.service.boss.NhomBossService.gI().tatCa();
        JComboBox<String> cbNhomSua = new JComboBox<>();
        cbNhomSua.addItem("Không theo nhóm — dùng giây hồi sinh ở trên");
        for (nro.service.boss.NhomBossService.Nhom n : dsNhomSua) {
            cbNhomSua.addItem(n.id + " — " + n.ten
                    + "  (" + moTaThoiGian(n.chuKyGiay) + ")");
        }
        cbNhomSua.setSelectedIndex(viTriNhomCuaBoss(idBossCuaMau, dsNhomSua));
        cbNhomSua.setEnabled(idBossCuaMau != Integer.MIN_VALUE);
        // Nhung o duoi day GIU gia tri that nhung khong cho go tay — moi o co
        // mot nut mo bang chon, va mot dong chu dich ra tieng nguoi ben canh.
        JTextField fTrangPhuc = new JTextField(20);
        JTextField fDame = new JTextField(14);
        JTextField fHp = new JTextField(24);
        // Ba o nay de trong nghia la "khong dat", luu xuong CSDL thanh -1. Khong
        // dung 0 lam gia tri trong: 0 con la mot lua chon that (tat han ne cua
        // mot con von co ne).
        JTextField fGiap = new JTextField(12);
        JTextField fNe = new JTextField(8);
        JTextField fChinhXac = new JTextField(8);
        JTextField fMap = new JTextField(24);
        JTextField fKyNang = new JTextField(30);
        JTextField fRest = new JTextField(10);
        // Thoai giu trong mang MOT phan tu, KHONG dung JTextField: o mot dong
        // nuot sach ky tu xuong dong, nen mo hop thoai roi bam Luu la ba cau
        // bi noi lien thanh mot. Luc do chatS() cat chuoi theo dau gach doc va
        // nem NumberFormatException moi nhip cap nhat boss — mot lan da lam
        // log phinh mười ba nghìn dòng.
        final String[] vS = {""};
        final String[] vM = {""};
        final String[] vE = {""};
        JLabel xemTrangPhuc = new JLabel();
        JLabel xemMap = new JLabel();
        JLabel xemKyNang = new JLabel();
        JLabel xemS = new JLabel();
        JLabel xemM = new JLabel();
        JLabel xemE = new JLabel();

        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT * FROM boss_data WHERE ma = ?", ma);
            if (!rs.next()) {
                rs.dispose();
                note(WARN_RED, "Không còn dòng " + ma + ".");
                return;
            }
            fTen.setText(rs.getString("ten"));
            cbHe.setSelectedIndex(Math.max(0, Math.min(2, rs.getInt("gioi_tinh"))));
            fTrangPhuc.setText(rs.getString("trang_phuc"));
            // So lon hien dang 1.000 cho de doc; luc luu bo dau cham di.
            fDame.setText(PlayerManagerPanel.fmt(rs.getLong("suc_danh")));
            fHp.setText(nhieuSoDep(rs.getString("hp")));
            fGiap.setText(soTrongODat(rs, "giap"));
            fNe.setText(soTrongODat(rs, "ne_don"));
            fChinhXac.setText(soTrongODat(rs, "chinh_xac"));
            fMap.setText(rs.getString("map_join"));
            fKyNang.setText(rs.getString("ky_nang"));
            fRest.setText(PlayerManagerPanel.fmt(rs.getInt("giay_hoi_sinh")));
            cbXH.setSelectedIndex(viTriXuatHien(rs.getString("kieu_xuat_hien")));
            vS[0] = rs.getString("text_s") == null ? "" : rs.getString("text_s");
            vM[0] = rs.getString("text_m") == null ? "" : rs.getString("text_m");
            vE[0] = rs.getString("text_e") == null ? "" : rs.getString("text_e");
            rs.dispose();
        } catch (Exception ex) {
            note(WARN_RED, "Không đọc được dòng: " + ex.getMessage());
            return;
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        bdHang(form, c, y++, "Tên:", fTen, "");
        bdHang(form, c, y++, "Hệ:", cbHe, "");
        bdHang(form, c, y++, "Trang phục:",
                bdNut(fTrangPhuc, xemTrangPhuc,
                        () -> chonTrangPhucBoss(this, fTrangPhuc.getText())),
                "đầu, thân, chân, đeo lưng, hào quang, hiệu ứng");
        bdHang(form, c, y++, "Sức đánh:", fDame, "");
        bdHang(form, c, y++, "HP:", fHp, "mỗi phase một mốc, ngăn bằng dấu phẩy");
        bdHang(form, c, y++, "Giáp:", fGiap, "để trống là giữ nguyên chỉ số gốc");
        bdHang(form, c, y++, "Né đòn:", fNe,
                "phần trăm 0–100; chính xác của người đánh trừ thẳng vào đây");
        bdHang(form, c, y++, "Chính xác:", fChinhXac,
                "phần trăm 0–100; trừ vào né đòn của người bị đánh");
        bdHang(form, c, y++, "Bản đồ:",
                bdNut(fMap, xemMap, () -> nro.ui.MapPicker.chon(this, fMap.getText())),
                "boss xuất hiện ở những bản đồ này");
        bdHang(form, c, y++, "Kỹ năng:",
                bdNut(fKyNang, xemKyNang,
                        () -> chonKyNangBoss(this, fKyNang.getText())),
                "");
        bdHang(form, c, y++, "Hồi sinh:", fRest, "giây");
        bdHang(form, c, y++, "Cách xuất hiện:", cbXH, "");
        bdHang(form, c, y++, "Nhóm hồi sinh:", cbNhomSua,
                "vào nhóm thì bỏ qua ô Hồi sinh ở trên");
        bdHang(form, c, y++, "Thoại chào sân:",
                oThoai(vS, xemS, "Thoại chào sân — đọc lần lượt từ trên xuống"), "");
        bdHang(form, c, y++, "Thoại lúc đánh:",
                oThoai(vM, xemM, "Thoại lúc đánh — bốc ngẫu nhiên một câu"), "");
        bdHang(form, c, y++, "Thoại lúc chết:",
                oThoai(vE, xemE, "Thoại lúc chết — đọc lần lượt từ trên xuống"), "");

        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 3;
        JLabel chu = new JLabel("<html><body style='width:600px'><i>"
                + "Số ở đây là số <b>gốc</b> của mẫu; "
                + "con nào có dòng đè trong boss_config thì số đè thắng. "
                + "Sửa xong phải khởi động lại máy chủ.</i></body></html>");
        chu.setForeground(GREY);
        form.add(chu, c);

        // Khoa be ngang: khong khoa thi mot dong mo ta dai keo ca hop thoai
        // rong bang man hinh, vi GridBagLayout co gian theo o rong nhat.
        JScrollPane khung = ServerGuiUtils.cuon(form);
        khung.setPreferredSize(new Dimension(760, 480));
        if (JOptionPane.showConfirmDialog(this, khung,
                "Sửa số liệu boss — " + ma,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            nro.repository.ConnectDB.executeUpdate(
                    "UPDATE boss_data SET ten = ?, gioi_tinh = ?, trang_phuc = ?,"
                    + " suc_danh = ?, hp = ?, giap = ?, ne_don = ?, chinh_xac = ?,"
                    + " map_join = ?, ky_nang = ?,"
                    + " text_s = ?, text_m = ?, text_e = ?, giay_hoi_sinh = ?,"
                    + " kieu_xuat_hien = ?"
                    + " WHERE ma = ?",
                    fTen.getText().trim(),
                    cbHe.getSelectedIndex(),
                    fTrangPhuc.getText().trim(),
                    Long.parseLong(boCham(fDame.getText())),
                    boCham(fHp.getText()),
                    oDatSo(fGiap.getText(), "Giáp", 0, Integer.MAX_VALUE),
                    oDatSo(fNe.getText(), "Né đòn", 0, 100),
                    oDatSo(fChinhXac.getText(), "Chính xác", 0, 100),
                    fMap.getText().trim(),
                    fKyNang.getText().trim(),
                    vS[0], vM[0], vE[0],
                    Integer.parseInt(boCham(fRest.getText())),
                    MA_XUAT_HIEN[Math.max(0, cbXH.getSelectedIndex())],
                    ma);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Nhập chưa đúng", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (Exception ex) {
            note(WARN_RED, "Không lưu được: " + ex.getMessage());
            return;
        }
        luuNhomChoBoss(idBossCuaMau, cbNhomSua.getSelectedIndex(), dsNhomSua);
        napBossData();
        LamMoi.bao(LamMoi.BOSS);
        note(OK_GREEN, "Đã lưu số liệu " + ma
                + " — khởi động lại máy chủ để có hiệu lực.");
    }

    /**
     * Xoá một mẫu số liệu khỏi bảng.
     *
     * <p>Cảnh báo khi mẫu đang có boss dùng: xoá đi thì lần khởi động sau máy
     * chủ không tìm thấy dòng nào cho mẫu đó và boss quay về số gõ trong mã
     * nguồn — không sập, nhưng mọi chỉnh sửa của mẫu đó mất hết.</p>
     */
    private void bdXoa() {
        int r = bdTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng trong bảng trước.");
            return;
        }
        String ma = String.valueOf(bdTable.getValueAt(r, 0));
        String thuoc = String.valueOf(bdTable.getValueAt(r, 2));

        String canhBao = thuoc == null || thuoc.trim().isEmpty()
                ? "Mẫu này không con boss nào đang dùng."
                : "Mẫu này đang được dùng bởi: " + thuoc
                  + "\nXoá đi thì lần khởi động sau boss đó quay về số gõ trong"
                  + " mã nguồn.";
        if (JOptionPane.showConfirmDialog(this,
                "Xoá mẫu số liệu \"" + ma + "\"?\n\n" + canhBao,
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            nro.repository.ConnectDB.executeUpdate(
                    "DELETE FROM boss_data WHERE ma = ?", ma);
        } catch (Exception ex) {
            note(WARN_RED, "Không xoá được: " + ex.getMessage());
            return;
        }
        napBossData();
        note(OK_GREEN, "Đã xoá mẫu " + ma + ".");
    }

    /**
     * Một hàng: nhãn, ô nhập, lời chú.
     *
     * <p>Lời chú cắt ngắn và đẩy phần dài vào lời mách. Cột này nằm ngoài cùng
     * bên phải nên câu chú dài là kéo cả hộp thoại rộng theo.</p>
     */
    private void bdHang(JPanel form, GridBagConstraints c, int y, String nhan,
            java.awt.Component o, String chu) {
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        form.add(new JLabel(nhan), c);
        c.gridx = 1;
        form.add(o, c);
        c.gridx = 2;
        JLabel l = nhan(rutGon(chu, 34));
        if (chu != null && chu.length() > 34) {
            l.setToolTipText(chu);
        }
        form.add(l, c);
    }
    // =====================================================================
    //  Các bộ chọn dùng trong hộp sửa số liệu boss
    // =====================================================================

    /**
     * Chọn kỹ năng cho boss: bảng ba cột, gõ để lọc, sửa cấp và hồi chiêu ngay
     * trong bảng.
     *
     * <p>Chuỗi lưu vẫn là {@code id:cấp:hồi,id:cấp:hồi} như cũ — chỉ đổi cách
     * nhập. Gõ tay chuỗi đó thì phải nhớ id nào là chiêu gì, mà bảng kỹ năng có
     * cả trăm dòng chia theo ba hành tinh.</p>
     *
     * @return chuỗi mới, hoặc {@code null} nếu huỷ
     */
    private String chonKyNangBoss(java.awt.Component cha, String hienTai) {
        // Doc chuoi cu vao map de danh dau nhung chieu da chon.
        final java.util.Map<Integer, int[]> daChon = new java.util.LinkedHashMap<>();
        if (hienTai != null) {
            for (String phan : hienTai.split(",")) {
                String[] p = phan.trim().split(":");
                if (p.length < 2) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(p[0].trim());
                    int cap = Integer.parseInt(p[1].trim());
                    int hoi = p.length > 2 ? Integer.parseInt(p[2].trim()) : 0;
                    daChon.put(id, new int[]{cap, hoi});
                } catch (NumberFormatException boQua) {
                    // Phan go sai thi bo qua, giu nhung phan doc duoc.
                }
            }
        }

        final java.util.List<Object[]> tatCa = new java.util.ArrayList<>();
        for (int ht = 0; ht < nro.repository.dao.KyNangDAO.HANH_TINH.length; ht++) {
            for (nro.repository.dao.KyNangDAO.KyNang k
                    : nro.repository.dao.KyNangDAO.kyNangCua(ht)) {
                tatCa.add(new Object[]{k.id, k.ten,
                    nro.repository.dao.KyNangDAO.HANH_TINH[ht]});
            }
        }

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Chọn", "ID", "Kỹ năng", "Hành tinh", "Cấp",
                    "Hồi chiêu (ms)"}, 0) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0 || c == 4 || c == 5;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(22);

        final JLabel lblDem = new JLabel();
        final JTextField fTim = new JTextField(20);

        // Do bang theo tu khoa dang loc. Lua chon nam trong "daChon" chu KHONG
        // nam trong bang: loc lai thi bang chi con vai dong, doc ket qua tu
        // bang la mat sach nhung chieu dang bi loc di.
        final Runnable ve = () -> {
            String q = fTim.getText().trim().toLowerCase();
            m.setRowCount(0);
            for (Object[] r : tatCa) {
                if (!q.isEmpty()
                        && !String.valueOf(r[1]).toLowerCase().contains(q)
                        && !String.valueOf(r[0]).contains(q)) {
                    continue;
                }
                int[] cu = daChon.get((Integer) r[0]);
                m.addRow(new Object[]{cu != null, r[0], r[1], r[2],
                    cu == null ? "" : String.valueOf(cu[0]),
                    cu == null ? "" : String.valueOf(cu[1])});
            }
            lblDem.setText("  Đang chọn: " + daChon.size() + " chiêu");
        };

        // Moi lan nguoi dung tich hay sua o, ghi thang vao daChon.
        m.addTableModelListener(e -> {
            if (e.getFirstRow() < 0 || e.getColumn() < 0) {
                return;
            }
            for (int r = e.getFirstRow();
                    r <= e.getLastRow() && r < m.getRowCount(); r++) {
                int id = (Integer) m.getValueAt(r, 1);
                if (Boolean.TRUE.equals(m.getValueAt(r, 0))) {
                    int[] cu = daChon.get(id);
                    int cap = soHoacMacDinh(m.getValueAt(r, 4), cu == null ? 1 : cu[0]);
                    int hoi = soHoacMacDinh(m.getValueAt(r, 5), cu == null ? 1000 : cu[1]);
                    daChon.put(id, new int[]{Math.max(1, cap), Math.max(0, hoi)});
                } else {
                    daChon.remove(id);
                }
            }
            lblDem.setText("  Đang chọn: " + daChon.size() + " chiêu");
        });

        fTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }
        });
        ve.run();
        int[] w = {50, 50, 210, 90, 60, 120};
        for (int i = 0; i < t.getColumnCount() && i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.setOpaque(false);
        top.add(new JLabel("Tìm theo tên hoặc id:"));
        top.add(fTim);
        top.add(button("Bỏ tích hết", GREY, e -> {
            daChon.clear();
            ve.run();
        }));
        top.add(lblDem);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(680, 440));
        root.add(top, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);
        JLabel chu = new JLabel("<html>Tích chiêu nào thì boss dùng chiêu đó. "
                + "<b>Cấp</b> để trống mặc định 1, <b>Hồi chiêu</b> để trống "
                + "mặc định 1000ms.<br>Hồi chiêu tính bằng mili giây — 1.000 là "
                + "một giây.</html>");
        chu.setForeground(GREY);
        root.add(chu, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(cha, root, "Chọn kỹ năng cho boss",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        if (t.isEditing()) {
            t.getCellEditor().stopCellEditing();
        }
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<Integer, int[]> e : daChon.entrySet()) {
            sb.append(sb.length() > 0 ? "," : "").append(e.getKey())
                    .append(':').append(e.getValue()[0])
                    .append(':').append(e.getValue()[1]);
        }
        return sb.toString();
    }

    /** Số trong ô, hoặc giá trị mặc định nếu ô trống hay gõ sai. */
    private static int soHoacMacDinh(Object o, int macDinh) {
        if (o == null) {
            return macDinh;
        }
        String s = String.valueOf(o).trim().replace(".", "");
        if (s.isEmpty()) {
            return macDinh;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return macDinh;
        }
    }

    /**
     * Chọn trang phục boss: sáu ô {@code {đầu, thân, chân, đeo lưng, hào quang,
     * hiệu ứng}}.
     *
     * <p>Ba ô đầu và ô đeo lưng là <b>mã bộ phận</b> chứ không phải id vật
     * phẩm. Nên cách chọn là <b>nhặt một vật phẩm rồi lấy mã bộ phận của nó</b>
     * — chọn "Áo Thần Linh" thì ô thân lấy đúng số mà áo đó dùng. Gõ tay số bộ
     * phận thì phải tra bảng vật phẩm bằng mắt.</p>
     *
     * @return chuỗi sáu số ngăn bằng dấu phẩy, hoặc {@code null} nếu huỷ
     */

    /**
     * Những part chưa có trong bảng {@code part}.
     *
     * <h3>Vì sao phải kiểm</h3>
     *
     * <p>Cải trang chỉ là ba con số trỏ tới ba dòng part (head / body / leg).
     * Trỏ vào id chưa có thì máy chủ vẫn nhận, vẫn lưu, không báo gì cả — nhưng
     * trong game nhân vật hiện ra <b>trần trụi màu trắng</b> vì client không có
     * hình để vẽ. Nhìn triệu chứng đó rất khó lần ra nguyên nhân là thiếu part,
     * nên chặn ngay lúc chọn.</p>
     *
     * @return danh sách id thiếu, rỗng nghĩa là đủ
     */
    private java.util.List<Integer> partConThieu(int... ids) {
        java.util.List<Integer> thieu = new java.util.ArrayList<>();
        if (ids == null || ids.length == 0) {
            return thieu;
        }
        StringBuilder in = new StringBuilder();
        java.util.List<Integer> canHoi = new java.util.ArrayList<>();
        for (int id : ids) {
            if (id < 0) {
                continue;
            }
            if (in.length() > 0) {
                in.append(',');
            }
            in.append(id);
            canHoi.add(id);
        }
        if (canHoi.isEmpty()) {
            return thieu;
        }
        java.util.Set<Integer> co = new java.util.HashSet<>();
        try {
            nro.repository.CrisResultSet rs = nro.repository.ConnectDB
                    .executeQuery("SELECT id FROM part WHERE id IN (" + in + ")");
            while (rs.next()) {
                co.add(rs.getInt("id"));
            }
            rs.dispose();
        } catch (Exception boQua) {
            // Khong hoi duoc thi thoi, dung chan nguoi dung vi mot loi doc.
            return thieu;
        }
        for (int id : canHoi) {
            if (!co.contains(id)) {
                thieu.add(id);
            }
        }
        return thieu;
    }

    /**
     * Báo nếu bộ cải trang vừa chọn thiếu part.
     *
     * <p>Chỉ cảnh báo chứ không chặn: có thể admin sắp thêm part ngay sau đó,
     * và chặn cứng thì không cách nào khai trước được.</p>
     */
    private void canhBaoThieuPart(java.awt.Component cha, String tenBo,
            int head, int body, int leg) {
        java.util.List<Integer> thieu = partConThieu(head, body, leg);
        if (thieu.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Bộ \"").append(tenBo).append("\" trỏ tới part chưa có trong bảng part:\n\n");
        for (int id : thieu) {
            sb.append("   • part ").append(id);
            if (id == head) {
                sb.append("  (đầu)");
            } else if (id == body) {
                sb.append("  (thân)");
            } else if (id == leg) {
                sb.append("  (chân)");
            }
            sb.append('\n');
        }
        sb.append("\nVẫn đặt được, nhưng trong game boss sẽ hiện ra trắng trơn "
                + "vì client không có hình để vẽ.\nThêm những part đó vào bảng "
                + "part trước rồi hãy dùng bộ này.");
        JOptionPane.showMessageDialog(cha, sb.toString(),
                "Thiếu part cho bộ cải trang", JOptionPane.WARNING_MESSAGE);
    }
    private String chonTrangPhucBoss(java.awt.Component cha, String hienTai) {
        int[] v = {-1, -1, -1, -1, -1, -1};
        if (hienTai != null) {
            String[] p = hienTai.split(",");
            for (int i = 0; i < 6 && i < p.length; i++) {
                try {
                    v[i] = Integer.parseInt(p[i].trim());
                } catch (NumberFormatException boQua) {
                    v[i] = -1;
                }
            }
        }
        JTextField[] o = new JTextField[6];
        for (int i = 0; i < 6; i++) {
            o[i] = new JTextField(String.valueOf(v[i]), 8);
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        String[] nhanO = {"Đầu:", "Thân:", "Chân:", "Đeo lưng:", "Hào quang:",
            "Hiệu ứng:"};
        for (int i = 0; i < 6; i++) {
            c.gridx = 0;
            c.gridy = i;
            form.add(new JLabel(nhanO[i]), c);
            c.gridx = 1;
            form.add(o[i], c);
        }
        // Ba nut chon RIENG tung manh, cho ai muon ghep bo la — vi du dau cua
        // bo nay voi than cua bo khac.
        for (int i = 0; i < 3; i++) {
            final int manh = i;
            c.gridx = 3;
            c.gridy = i;
            form.add(button("Chọn…", GREY, e -> {
                Integer moi = chonPart(form, manh);
                if (moi != null) {
                    o[manh].setText(String.valueOf(moi));
                }
            }), c);
        }

        // Mot nut lay CA head/body/leg tu mot mon do — cai trang hay bo giap
        // von di lien ba manh, chon rieng tung manh la de lech bo.
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 3;
        form.add(button("Chọn cả bộ cải trang…", ACCENT, e -> {
            nro.entity.template.ItemTemplate t = chonCaiTrang(form);
            if (t == null) {
                return;
            }
            o[0].setText(String.valueOf(t.head));
            o[1].setText(String.valueOf(t.body));
            o[2].setText(String.valueOf(t.leg));
            // Lay du ba part cua bo — va kiem luon xem may chu co that su co
            // ba part do khong.
            canhBaoThieuPart(form, t.name, t.head, t.body, t.leg);
        }), c);
        c.gridheight = 1;

        c.gridx = 2;
        c.gridy = 3;
        form.add(button("Chọn đeo lưng…", ACCENT, e -> {
            int id = nro.ui.OptionPicker.chonVatPham(form, -1);
            if (id < 0) {
                return;
            }
            nro.entity.template.ItemTemplate t =
                    PlayerManagerPanel.templateOf(String.valueOf(id));
            if (t != null) {
                o[3].setText(String.valueOf(t.part));
            }
        }), c);

        c.gridx = 2;
        c.gridy = 4;
        form.add(button("Chọn hào quang…", ACCENT, e -> {
            Integer id = chonHaoQuang(form);
            if (id != null) {
                o[4].setText(String.valueOf(id));
            }
        }), c);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 3;
        JLabel chu = new JLabel("<html><i>Đầu / Thân / Chân / Đeo lưng là "
                + "<b>mã bộ phận</b>, không phải id vật phẩm — nên chọn qua "
                + "vật phẩm cho chắc. Để −1 là không mặc gì.</i></html>");
        chu.setForeground(GREY);
        form.add(chu, c);

        if (JOptionPane.showConfirmDialog(cha, form, "Chọn trang phục boss",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int x;
            try {
                x = Integer.parseInt(o[i].getText().trim());
            } catch (NumberFormatException ex) {
                x = -1;
            }
            sb.append(i > 0 ? "," : "").append(x);
        }
        return sb.toString();
    }

    /** Bảng chọn hào quang, lấy từ {@code AuraDAO}. */
    private Integer chonHaoQuang(java.awt.Component cha) {
        java.util.List<nro.repository.dao.AuraDAO.Aura> ds =
                nro.repository.dao.AuraDAO.tatCa();
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(cha, "Chưa có hào quang nào trong CSDL.",
                    "Trống", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Object[] chon = new Object[ds.size() + 1];
        chon[0] = "-1 — không có";
        for (int i = 0; i < ds.size(); i++) {
            chon[i + 1] = ds.get(i).toString();
        }
        Object k = JOptionPane.showInputDialog(cha, "Chọn hào quang:",
                "Hào quang", JOptionPane.PLAIN_MESSAGE, null, chon, chon[0]);
        if (k == null) {
            return null;
        }
        String s = String.valueOf(k);
        try {
            return Integer.parseInt(s.substring(0, s.indexOf(' ')).trim());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Sửa danh sách câu thoại, mỗi câu một dòng trong bảng.
     *
     * <p>Trước đây ba nhóm thoại nằm chung một ô nhiều dòng, nhìn không ra câu
     * nào tách câu nào — nhất là khi câu có mã màu {@code |-1|} ở đầu.</p>
     *
     * @return các câu đã nối bằng ký tự xuống dòng, hoặc {@code null} nếu huỷ
     */
    private String suaThoai(java.awt.Component cha, String tieuDe, String hienTai) {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"Câu thoại"}, 0);
        if (hienTai != null && !hienTai.isEmpty()) {
            for (String d : hienTai.split("\n", -1)) {
                m.addRow(new Object[]{d});
            }
        }
        JTable t = new JTable(m);
        t.setRowHeight(24);

        JPanel nut = new JPanel(new GridLayout(0, 3, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm dòng", OK_GREEN, e -> m.addRow(new Object[]{""})));
        nut.add(button("Xoá dòng", WARN_RED, e -> {
            int r = t.getSelectedRow();
            if (r >= 0) {
                if (t.isEditing()) {
                    t.getCellEditor().stopCellEditing();
                }
                m.removeRow(r);
            }
        }));
        nut.add(button("Xoá hết", GREY, e -> m.setRowCount(0)));

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(560, 320));
        JLabel chu = new JLabel("<html><i>Mỗi dòng là một câu. Boss bốc ngẫu "
                + "nhiên một câu trong nhóm. Tiền tố <b>|-1|</b> hay <b>|-2|</b> "
                + "ở đầu câu là mã màu chữ.</i></html>");
        chu.setForeground(GREY);
        root.add(chu, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(cha, root, tieuDe,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        if (t.isEditing()) {
            t.getCellEditor().stopCellEditing();
        }
        java.util.List<String> ds = new java.util.ArrayList<>();
        for (int i = 0; i < m.getRowCount(); i++) {
            String s = String.valueOf(m.getValueAt(i, 0) == null ? "" : m.getValueAt(i, 0));
            if (!s.trim().isEmpty()) {
                ds.add(s);
            }
        }
        return String.join("\n", ds);
    }

    /** Mô tả ngắn danh sách kỹ năng, để hiện cạnh nút. */
    private String moTaKyNang(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "chưa có chiêu nào";
        }
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (String phan : raw.split(",")) {
            String[] p = phan.trim().split(":");
            if (p.length < 2) {
                continue;
            }
            n++;
            if (n > 3) {
                continue;
            }
            sb.append(sb.length() > 0 ? ", " : "")
                    .append(tenKyNang(p[0].trim())).append(" c").append(p[1].trim());
        }
        return sb + (n > 3 ? " … (" + n + " chiêu)" : "");
    }

    private String tenKyNang(String id) {
        try {
            int x = Integer.parseInt(id);
            for (int ht = 0; ht < nro.repository.dao.KyNangDAO.HANH_TINH.length; ht++) {
                for (nro.repository.dao.KyNangDAO.KyNang k
                        : nro.repository.dao.KyNangDAO.kyNangCua(ht)) {
                    if (k.id == x) {
                        return k.ten;
                    }
                }
            }
            return "chiêu " + x;
        } catch (NumberFormatException ex) {
            return id;
        }
    }

    /**
     * Ô chỉ đọc kèm nút mở bảng chọn, và một dòng chữ dịch giá trị ra tiếng
     * người.
     *
     * <p>Ô giữ đúng chuỗi sẽ ghi xuống CSDL nhưng {@code setEditable(false)}:
     * mấy chuỗi này — {@code 1896,1897,1898,-1,-1,-1} hay
     * {@code 4:7:500,5:7:1000} — gõ tay là sai thứ tự lúc nào không hay.</p>
     */
    private JPanel bdNut(JTextField o, JLabel xem,
            java.util.function.Supplier<String> chon) {
        o.setEditable(false);
        // 16 cot la du nhin ma khong keo hop thoai rong ra: gia tri that co the
        // dai hang tram ky tu, va o nhap tu co gian theo noi dung.
        o.setColumns(16);
        xem.setForeground(GREY);
        veLaiXem(o, xem);

        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        trai.setOpaque(false);
        trai.add(o);
        trai.add(button("Chọn…", ACCENT, e -> {
            String moi = chon.get();
            if (moi != null) {
                o.setText(moi);
                veLaiXem(o, xem);
            }
        }));
        p.add(trai, BorderLayout.WEST);
        p.add(xem, BorderLayout.CENTER);
        return p;
    }

    /**
     * Dịch giá trị trong ô ra câu mô tả.
     *
     * <p>Đoán kiểu dữ liệu theo hình dạng chuỗi thay vì bắt nơi gọi khai báo:
     * dấu hai chấm là kỹ năng, xuống dòng là thoại, còn lại là danh sách số.
     * Chỉ dùng để hiện chữ nên đoán sai cũng không hỏng dữ liệu.</p>
     */
    /**
     * Dịch giá trị trong ô ra câu mô tả, <b>cắt ngắn</b> cho vừa hộp thoại.
     *
     * <p>Chuỗi bản đồ của một con boss có thể liệt kê ba chục tên map. Để
     * nguyên thì cái nhãn kéo hộp thoại rộng ra hết màn hình, vì
     * {@code GridBagLayout} co giãn theo thành phần rộng nhất. Cắt ở đây và
     * đưa bản đầy đủ vào lời mách khi rê chuột.</p>
     *
     * <p>Đoán kiểu dữ liệu theo hình dạng chuỗi thay vì bắt nơi gọi khai báo:
     * dấu hai chấm là kỹ năng, xuống dòng là thoại, còn lại là danh sách số.
     * Chỉ dùng để hiện chữ nên đoán sai cũng không hỏng dữ liệu.</p>
     */
    private void veLaiXem(JTextField o, JLabel xem) {
        String s = o.getText() == null ? "" : o.getText().trim();
        String day;
        if (s.isEmpty()) {
            day = "(trống)";
        } else if (s.contains(":")) {
            day = moTaKyNang(s);
        } else if (s.contains("\n")) {
            String[] cau = s.split("\n", -1);
            day = cau.length + " câu — " + cau[0];
        } else {
            String[] p = s.split(",");
            day = p.length == 6
                    ? "đầu " + p[0] + ", thân " + p[1] + ", chân " + p[2]
                      + ", lưng " + p[3] + ", hào quang " + p[4]
                      + ", hiệu ứng " + p[5]
                    : nro.ui.MapPicker.moTa(s);
        }
        xem.setText(rutGon(day, 52));
        xem.setToolTipText(day.length() > 52 ? "<html><body style='width:360px'>"
                + day.replace("<", "&lt;") + "</body></html>" : null);
    }

    private String rutGon(String s, int n) {
        return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…");
    }

    /** Bỏ dấu chấm ngăn nghìn để lấy lại số thô. */
    private static String boCham(String s) {
        return s == null ? "" : s.trim().replace(".", "").replace(" ", "");
    }

    /** Định dạng một danh sách số ngăn bằng dấu phẩy thành dạng {@code 1.000}. */
    private static String nhieuSoDep(String s) {
        if (s == null || s.trim().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String p : s.split(",")) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            try {
                sb.append(PlayerManagerPanel.fmt(Long.parseLong(p.trim())));
            } catch (NumberFormatException ex) {
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    /**
     * Trạng thái boss, gộp thành <b>ba</b> câu người dùng cần biết.
     *
     * <p>Máy có chín trạng thái ({@code REST}, {@code RESPAWN},
     * {@code JOIN_MAP}, {@code CHAT_S}, {@code ACTIVE}, {@code DIE},
     * {@code CHAT_E}, {@code LEAVE_MAP}, {@code AFK}) nhưng phần lớn là các
     * khoảnh khắc chớp nhoáng trong lúc boss ra vào bản đồ. Nhìn bảng chỉ cần
     * biết: nó đang ở đó, hay đang chờ, hay chờ vì con khác chưa chết.</p>
     *
     * <h3>Vì sao phải hỏi cả TypeAppear</h3>
     *
     * <p>Boss là phase sau cũng nằm ở {@code REST} y hệt boss thường đang chờ
     * giờ — trạng thái không phân biệt được. Cái phân biệt nằm ở
     * {@code TypeAppear}: chỉ {@code DEFAULT_APPEAR} mới tự hồi sinh theo giờ.
     * Đây đúng là điều kiện mà {@code Boss.rest()} xét, nên bảng nói cùng một
     * thứ với máy chứ không đoán riêng.</p>
     */
    private static String moTaTrangThaiBoss(nro.entity.boss.Boss b) {
        if (b == null || b.bossStatus == null) {
            return "-";
        }
        switch (b.bossStatus) {
            case RESPAWN:
            case JOIN_MAP:
            case CHAT_S:
            case ACTIVE:
            case AFK:
                return "đã hồi sinh";
            default:
                break;
        }
        // Con lai la REST / DIE / CHAT_E / LEAVE_MAP — deu la "chua co mat".
        return tuHoiSinhDuoc(b) ? "chưa hồi sinh" : "đang chờ phase trước";
    }

    /**
     * Con boss này có tự hồi sinh theo giờ được không.
     *
     * <p>Xét đúng phần tử mà {@code Boss.rest()} xét: <b>phase kế tiếp</b>, chứ
     * không phải phase đang đứng. Boss hai phase đang ở phase cuối thì lần tới
     * nó quay về phase đầu, và chính phase đầu mới quyết định nó có tự mọc lại
     * hay không.</p>
     */
    private static boolean tuHoiSinhDuoc(nro.entity.boss.Boss b) {
        if (b.data == null || b.data.length == 0) {
            return true;
        }
        int ke = b.currentLevel + 1;
        if (ke >= b.data.length || ke < 0) {
            ke = 0;
        }
        nro.entity.boss.BossData d = b.data[ke];
        return d == null
                || d.getTypeAppear() == nro.entity.boss.TypeAppear.DEFAULT_APPEAR;
    }

    // =====================================================================
    //  Xem số liệu boss (chỉ đọc) — dùng ở tab "1. Đang chạy & đồ rơi"
    // =====================================================================

    /** Mã boss ở dòng đang chọn trong bảng danh sách xuất hiện (tab 3). */
    /**
     * Mã boss của dòng đang chọn ở bảng số liệu (tab 2).
     *
     * <p>Bảng này liệt kê <b>mẫu số liệu</b> chứ không phải con boss, nên mã
     * boss lấy từ cột "ID boss" — cột đó do {@link #banDoBossId()} điền, dựa
     * trên con boss nào đang thật sự cầm mẫu đó.</p>
     *
     * <p>Mẫu chưa con boss nào dùng thì không có mã, và mọi thao tác đè chỉ số
     * đều không áp vào đâu được. Trả {@code Integer.MIN_VALUE} để nơi gọi báo
     * cho người dùng thay vì lặng lẽ ghi một dòng vô chủ.</p>
     */

    /**
     * Id boss đang gắn với một mã mẫu trong {@code boss_spawn}.
     *
     * <p>Cột <i>ID boss</i> ở bảng mẫu để trống khi mẫu chưa vào game, nhưng
     * trống <b>không</b> đồng nghĩa "chưa có id": bảng chỉ hiện id cho những
     * dòng nó ghép được. Cấp id mới mà không tra lại đây thì bấm thêm lần nữa
     * là sinh ra một con boss trùng mã, mỗi con một id — báo thành công mà số
     * con vẫn đứng yên.</p>
     *
     * @return id đang dùng, hoặc {@code Integer.MIN_VALUE} nếu mã này chưa vào game
     */
    private static int idBossTheoMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            return Integer.MIN_VALUE;
        }
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            if (ma.trim().equals(d.ten)) {
                return d.bossId;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Đưa mẫu boss đang chọn vào game — làm trọn một nút.
     *
     * <h3>Vì sao cần nút riêng</h3>
     *
     * <p>Tạo mẫu và đưa vào game là hai việc khác nhau: mẫu nằm ở
     * {@code boss_data}, còn "có mặt trong game mấy con" nằm ở
     * {@code boss_spawn}. Mẫu vừa tạo mà chưa có dòng spawn thì cột <i>ID
     * boss</i> để trống và trong game không thấy đâu cả — nhìn bảng không đoán
     * ra là còn thiếu bước nào.</p>
     *
     * <p>Nút này tự lo hết: chưa có id thì <b>cấp id mới</b>, ghi dòng spawn,
     * dựng boss ngay, và vẽ lại bảng. Người dùng chỉ chọn số con.</p>
     */
    private void bdThemVaoGame() {
        int r = bdTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một mẫu trong bảng trước đã.");
            return;
        }
        int rm = bdTable.convertRowIndexToModel(r);
        String ma = String.valueOf(bdModel.getValueAt(rm, 0)).trim();
        String tenHien = String.valueOf(bdModel.getValueAt(rm, 1)).trim();

        // Tra theo MA truoc, roi moi tin cot ID boss cua bang.
        int idCu = idBossTheoMa(ma);
        if (idCu == Integer.MIN_VALUE) {
            idCu = bdSelectedBossId();
        }
        boolean daCoId = idCu != Integer.MIN_VALUE;

        JTextField fSoCon = new JTextField("1", 5);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        bdHang(form, c, y++, "Mẫu:",
                new JLabel(tenHien + "   (mã " + ma + ")"), "");
        bdHang(form, c, y++, "ID boss:",
                new JLabel(daCoId ? String.valueOf(idCu) : "sẽ cấp id mới"), "");
        bdHang(form, c, y++, "Thêm mấy con:", fSoCon, "dựng ngay, không phải khởi động lại");

        if (JOptionPane.showConfirmDialog(this, form, "Thêm boss vào game",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int soCon;
        try {
            soCon = Integer.parseInt(boCham(fSoCon.getText()));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Số con phải là số.");
            return;
        }
        if (soCon < 1) {
            note(WARN_RED, "Số con phải từ 1 trở lên.");
            return;
        }

        int id = daCoId ? idCu : idBossTuDo();
        String loi = themBossVaoGame(id, ma, soCon);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Chưa thêm được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        note(OK_GREEN, "Đã thêm " + soCon + " con \"" + tenHien
                + "\" vào game (id " + id + ").");
        napBossData();
        veLaiBoss();
    }
    private int bdSelectedBossId() {
        int r = bdTable.getSelectedRow();
        if (r < 0) {
            return Integer.MIN_VALUE;
        }
        try {
            return Integer.parseInt(String.valueOf(
                    bdModel.getValueAt(bdTable.convertRowIndexToModel(r),
                            COT_BD_ID_BOSS)).trim());
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
        }
    }

    private String bdSelectedTen() {
        int r = bdTable.getSelectedRow();
        return r < 0 ? "" : String.valueOf(
                bdModel.getValueAt(bdTable.convertRowIndexToModel(r), 1));
    }

    /**
     * Mẫu số liệu nào thuộc con boss mang mã nào.
     *
     * <p>Dò giống {@link #banDoPhase()} — so chính đối tượng giữa mẫu trong
     * {@code BossesData} và mảng {@code data} của boss đang sống.</p>
     */
    private java.util.Map<String, String> banDoBossId() {
        java.util.Map<String, String> ra = new java.util.HashMap<>();
        java.util.IdentityHashMap<nro.entity.boss.BossData, String> maCua
                = new java.util.IdentityHashMap<>();
        for (java.util.Map.Entry<String, nro.entity.boss.BossData> e
                : nro.repository.dao.BossDataDAO.quetTrongMa().entrySet()) {
            maCua.put(e.getValue(), e.getKey());
        }
        // Hoi SO DANG KY day du chu khong rieng BossManager: muoi bay manager
        // ma chi mot cai cho lay danh sach, nen truoc day boss nam o Yardart,
        // Broly, OtherBoss... deu bi bao nham la "chua co trong game".
        java.util.List<nro.entity.boss.Boss> bs = nro.entity.boss.Boss.tatCaBoss();
        for (nro.entity.boss.Boss b : bs) {
            if (b == null || b.data == null) {
                continue;
            }
            for (nro.entity.boss.BossData d : b.data) {
                String ma = maCua.get(d);
                if (ma != null) {
                    ra.put(ma, String.valueOf(b.id));
                }
            }
        }
        // Boss tao tu panel co doi tuong BossData rieng, khong nam trong danh
        // sach khai o ma nguon nen vong tren ghep truot. Bang boss_spawn giu
        // san ca id lan ma, tra thang o day.
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            if (d.ten != null && !d.ten.trim().isEmpty()) {
                ra.putIfAbsent(d.ten.trim(), String.valueOf(d.bossId));
            }
        }
        return ra;
    }
    /**
     * Một con boss đang sống mang mã này, hoặc {@code null}.
     *
     * <p>Nhiều bản cùng mã thì lấy con đầu tiên — chúng dùng chung số liệu nên
     * đọc con nào cũng ra cùng kết quả.</p>
     */
    private nro.entity.boss.Boss bossSongTheoId(int id) {
        for (nro.entity.boss.Boss b : bossTheoDong) {
            if (b != null && b.id == id) {
                return b;
            }
        }
        return null;
    }

    /**
     * Hiện số liệu của boss đang chọn, <b>chỉ để đọc</b>.
     *
     * <p>Gom cả ba lớp số vào một chỗ và nói rõ lớp nào đang thắng: số gốc của
     * mẫu, số đè riêng cho con này, và quy ước chung. Trước đây muốn biết một
     * con boss thực sự chạy bằng số nào thì phải mở ba tab rồi tự đối chiếu.</p>
     */
    private void bossXemSoLieu() {
        nro.entity.boss.Boss b = bossDangChon();
        if (b == null) {
            note(WARN_RED, "Chọn một dòng boss trước.");
            return;
        }
        int id = (int) b.id;
        nro.repository.dao.BossDAO.Config cf = nro.repository.dao.BossDAO.config(id);
        long chung = nro.repository.dao.ConfigDAO.num(
                nro.repository.dao.ConfigDAO.BOSS_GIAY_HOI_SINH, 0);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><table cellpadding=3>");
        hangXem(sb, "Mã boss", String.valueOf(id));
        hangXem(sb, "Tên", b.name == null ? "(chưa xuất hiện)" : b.name);
        hangXem(sb, "Trạng thái", moTaTrangThaiBoss(b));
        hangXem(sb, "Số phase", String.valueOf(b.data == null ? 0 : b.data.length));

        if (b.data != null) {
            for (int i = 0; i < b.data.length; i++) {
                nro.entity.boss.BossData d = b.data[i];
                if (d == null) {
                    continue;
                }
                String nhan = b.data.length > 1 ? "Phase " + (i + 1) : "Số liệu";
                hangXem(sb, nhan + " — tên", d.getName());
                hangXem(sb, nhan + " — HP",
                        d.getHp() == null ? "-" : nhieuSoDep(soThanhChuoi(d.getHp())));
                hangXem(sb, nhan + " — sức đánh",
                        PlayerManagerPanel.fmt(d.getDame()));
                hangXem(sb, nhan + " — kỹ năng", moTaKyNang(chuoiKyNang(d.getSkillTemp())));
                hangXem(sb, nhan + " — bản đồ",
                        d.getMapJoin() == null ? "-"
                                : nro.ui.MapPicker.moTa(soThanhChuoiInt(d.getMapJoin())));
                hangXem(sb, nhan + " — hồi sinh",
                        PlayerManagerPanel.fmt(d.getSecondsRest()) + " giây");
            }
        }

        sb.append("<tr><td colspan=2><hr></td></tr>");
        hangXem(sb, "Số đè riêng (boss_config)",
                cf == null ? "<i>không có</i>" : moTaDeChiSo(cf));
        hangXem(sb, "Quy ước chung hồi sinh",
                chung <= 0 ? "<i>tắt</i>"
                        : PlayerManagerPanel.fmt(chung) + " giây"
                          + (cf != null && cf.secondsRest != null
                                ? " <i>(không áp — con này có số riêng)</i>" : ""));
        hangXem(sb, "Món rơi thêm",
                String.valueOf(nro.repository.dao.BossDAO.drops(id).size()) + " món");
        sb.append("</table>");
        sb.append("<br><i>Bảng này chỉ để xem. Sửa số gốc ở tab \"2. Số liệu\", "
                + "đè riêng cho con này ở tab \"3. Danh sách xuất hiện\".</i></html>");

        JLabel noiDung = new JLabel(sb.toString());
        JScrollPane sc = ServerGuiUtils.cuon(noiDung);
        sc.setPreferredSize(new Dimension(620, 460));
        JOptionPane.showMessageDialog(this, sc,
                "Số liệu boss #" + id, JOptionPane.PLAIN_MESSAGE);
    }

    private void hangXem(StringBuilder sb, String nhan, String gt) {
        sb.append("<tr><td><b>").append(nhan).append("</b></td><td>")
                .append(gt == null || gt.isEmpty() ? "-" : gt).append("</td></tr>");
    }

    private String moTaDeChiSo(nro.repository.dao.BossDAO.Config c) {
        java.util.List<String> ds = new java.util.ArrayList<>();
        if (c.hp != null && !c.hp.trim().isEmpty()) {
            ds.add("HP " + nhieuSoDep(c.hp));
        }
        if (c.dame != null) {
            ds.add("sức đánh " + PlayerManagerPanel.fmt(c.dame));
        }
        if (c.secondsRest != null) {
            ds.add("hồi sinh " + PlayerManagerPanel.fmt(c.secondsRest) + " giây");
        }
        if (c.mapJoin != null && !c.mapJoin.trim().isEmpty()) {
            ds.add("bản đồ " + c.mapJoin);
        }
        if (c.chanDoGoc) {
            ds.add("chặn đồ rơi viết cứng");
        }
        return ds.isEmpty() ? "<i>có dòng nhưng chưa đè ô nào</i>"
                : String.join(", ", ds);
    }

    private static String soThanhChuoi(long[] a) {
        StringBuilder sb = new StringBuilder();
        for (long v : a) {
            sb.append(sb.length() > 0 ? "," : "").append(v);
        }
        return sb.toString();
    }

    private static String soThanhChuoiInt(int[] a) {
        StringBuilder sb = new StringBuilder();
        for (int v : a) {
            sb.append(sb.length() > 0 ? "," : "").append(v);
        }
        return sb.toString();
    }

    private static String chuoiKyNang(int[][] a) {
        if (a == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int[] r : a) {
            if (r == null || r.length == 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            for (int i = 0; i < r.length; i++) {
                sb.append(i > 0 ? ":" : "").append(r[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Mỗi mẫu số liệu thuộc con boss nào, và là phase thứ mấy.
     *
     * <p>Một con boss nhiều phase <b>không</b> có một dòng số liệu duy nhất —
     * nó cầm nhiều mẫu, mỗi phase một mẫu. Black Goku chẳng hạn dùng
     * {@code BLACK_GOKU} cho phase 1 và {@code SUPER_BLACK_GOKU} cho phase 2.
     * Nhìn bảng thì hai dòng đó trông như hai con boss chẳng liên quan gì
     * nhau — cột này nói rõ chúng thuộc về ai.</p>
     *
     * <p>Cách dò: so <b>chính đối tượng</b> ({@code ==}, không phải
     * {@code equals}) giữa các mẫu trong {@code BossesData} và mảng
     * {@code data} của từng con boss đang sống. So bằng tên hay bằng chỉ số sẽ
     * sai ngay khi hai phase trùng tên, mà điều đó có thật.</p>
     *
     * <p>Mẫu nào không con boss nào cầm thì để trống — đó cũng là thông tin:
     * sửa nó không ảnh hưởng gì tới game.</p>
     */
    private java.util.Map<String, String> banDoPhase() {
        java.util.Map<String, String> ra = new java.util.HashMap<>();
        java.util.IdentityHashMap<nro.entity.boss.BossData, String> maCua
                = new java.util.IdentityHashMap<>();
        for (java.util.Map.Entry<String, nro.entity.boss.BossData> e
                : nro.repository.dao.BossDataDAO.quetTrongMa().entrySet()) {
            maCua.put(e.getValue(), e.getKey());
        }

        // Chup ban sao danh sach boss: thread game sua no lien tuc, duyet
        // thang la ConcurrentModificationException giua luc ve bang.
        // Hoi SO DANG KY day du chu khong rieng BossManager: muoi bay manager
        // ma chi mot cai cho lay danh sach, nen truoc day boss nam o Yardart,
        // Broly, OtherBoss... deu bi bao nham la "chua co trong game".
        java.util.List<nro.entity.boss.Boss> bs = nro.entity.boss.Boss.tatCaBoss();

        for (nro.entity.boss.Boss b : bs) {
            if (b == null || b.data == null) {
                continue;
            }
            for (int i = 0; i < b.data.length; i++) {
                String ma = maCua.get(b.data[i]);
                if (ma == null || ra.containsKey(ma)) {
                    continue;
                }
                String tenBoss = b.data[0] == null || b.data[0].getName() == null
                        ? String.valueOf(b.id) : b.data[0].getName();
                ra.put(ma, b.data.length > 1
                        ? tenBoss + " — phase " + (i + 1) + "/" + b.data.length
                        : tenBoss);
            }
        }
        // Boss tao tu panel: doi tuong BossData rieng nen vong tren ghep truot.
        // Tra bang ma trong boss_spawn, lay ten hien thi tu chinh boss_data.
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            String ma = d.ten == null ? "" : d.ten.trim();
            if (ma.isEmpty() || ra.containsKey(ma)) {
                continue;
            }
            String tenHien = ma;
            nro.repository.CrisResultSet rs = null;
            try {
                rs = nro.repository.ConnectDB.executeQuery(
                        "SELECT ten FROM boss_data WHERE ma = ?", ma);
                if (rs.next()) {
                    String t = rs.getString("ten");
                    if (t != null && !t.trim().isEmpty()) {
                        tenHien = t.trim();
                    }
                }
            } catch (Exception boQua) {
                // Khong doc duoc thi hien ma hang cung du nhan ra.
            } finally {
                if (rs != null) {
                    try {
                        rs.dispose();
                    } catch (Exception boQua2) {
                    }
                }
            }
            ra.put(ma, tenHien);
        }
        return ra;
    }

    /**
     * Cột "Phase" ở tab 1: liệt kê <b>đủ mọi phase</b>, đánh dấu phase đang ở.
     *
     * <p>Tab 2 mỗi mẫu một dòng nên Black Goku hiện thành hai dòng. Tab 1 thì
     * mỗi con boss sống một dòng — một con chỉ đang ở một phase, tách thành hai
     * dòng là nói dối rằng có hai con.</p>
     *
     * <p>Nên thay vì tách dòng, cột này ghi cả chuỗi và bọc phase hiện tại
     * trong ngoặc vuông: {@code Black Goku → [Super Black Goku]}. Nhìn một dòng
     * là biết con này có mấy phase, tên từng phase, và đang ở phase nào.</p>
     *
     * <p>{@code currentLevel} đếm từ 0 và bằng {@code -1} khi boss chưa xuất
     * hiện lần nào, nên phải chặn số âm.</p>
     */
    private static String moTaPhase(nro.entity.boss.Boss b) {
        if (b == null || b.data == null || b.data.length == 0) {
            return "-";
        }
        int nay = Math.max(0, b.currentLevel);
        if (b.data.length == 1) {
            return tenPhase(b, 0);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.data.length; i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            if (i == nay) {
                sb.append('[').append(tenPhase(b, i)).append(']');
            } else {
                sb.append(tenPhase(b, i));
            }
        }
        return sb.toString();
    }

    private static String tenPhase(nro.entity.boss.Boss b, int i) {
        if (b.data[i] == null || b.data[i].getName() == null) {
            return "phase " + (i + 1);
        }
        return b.data[i].getName();
    }

    /** Số phase đọc được từ chữ "phase N" ở cột Phase của bảng, đếm từ 0. */
    private int viTriPhase(String moTa) {
        if (moTa == null || !moTa.startsWith("phase ")) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(moTa.substring(6).trim()) - 1);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // =====================================================================
    //  Cách xuất hiện của một mẫu boss
    // =====================================================================

    /**
     * Bốn cách một mẫu boss có thể xuất hiện, theo thứ tự của
     * {@code TypeAppear}.
     *
     * <p>Đây là thứ quyết định một mẫu <b>tự hồi sinh</b> hay phải <b>chờ con
     * khác</b>. Phase 2, phase 3 của một con boss luôn là
     * {@code ANOTHER_LEVEL}: chúng không tự mọc, mà thay chỗ phase trước khi
     * phase đó bị hạ.</p>
     */
    private static final String[] MA_XUAT_HIEN = {
        "DEFAULT_APPEAR", "ANOTHER_LEVEL", "APPEAR_WITH_ANOTHER", "CALL_BY_ANOTHER"};

    private static final String[] NHAN_XUAT_HIEN = {
        "Tự hồi sinh theo giờ",
        "Là phase sau — chờ phase trước bị hạ",
        "Hiện cùng một con boss khác",
        "Chỉ hiện khi bị gọi ra"};

    /** Vị trí trong ô chọn ứng với tên hằng lưu dưới CSDL. */
    private static int viTriXuatHien(String ma) {
        if (ma != null) {
            for (int i = 0; i < MA_XUAT_HIEN.length; i++) {
                if (MA_XUAT_HIEN[i].equals(ma.trim())) {
                    return i;
                }
            }
        }
        return 0;
    }

    /** Nhãn tiếng Việt của một cách xuất hiện. */
    private static String moTaXuatHien(String ma) {
        return NHAN_XUAT_HIEN[viTriXuatHien(ma)];
    }

    // =====================================================================
    //  Danh mục boss: đã thêm vào game / chưa thêm
    // =====================================================================

    /**
     * Mọi id boss mà mã nguồn biết dựng, kèm tên hằng.
     *
     * <p>Đọc bằng phản chiếu từ lớp {@code BossID} — đó là chỗ duy nhất liệt kê
     * đủ; {@code createBoss()} là một câu {@code switch} nên không duyệt được.
     * Id trùng nhau thì giữ tên đầu tiên: vài hằng chỉ là bí danh của nhau.</p>
     */
    private java.util.Map<Integer, String> danhMucBossID() {
        java.util.Map<Integer, String> ra = new java.util.LinkedHashMap<>();
        try {
            for (java.lang.reflect.Field f : nro.entity.boss.BossID.class.getFields()) {
                if (f.getType() == int.class) {
                    int v = f.getInt(null);
                    if (!ra.containsKey(v)) {
                        ra.put(v, f.getName());
                    }
                }
            }
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Không đọc được danh sách BossID");
        }
        return ra;
    }

    /** Tên hằng trong {@code BossID} ứng với một id, hoặc {@code null}. */
    private String tenHangBossIDPanel(int bossId) {
        return danhMucBossID().get(bossId);
    }
    /**
     * Đưa một boss vào game.
     *
     * <p>Ghi vào {@code boss_spawn} để lần khởi động sau vẫn còn, <b>và</b> dựng
     * ngay các con boss đó để không phải khởi động lại mới thấy.</p>
     *
     * @return câu báo lỗi, hoặc {@code null} nếu xong
     */
    private String themBossVaoGame(int bossId, String ten, int soCon) {
        if (soCon < 1) {
            return "Số con phải từ 1 trở lên.";
        }
        nro.repository.dao.BossSpawnDAO.Dong d = null;
        for (nro.repository.dao.BossSpawnDAO.Dong x
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            if (x.bossId == bossId) {
                d = x;
                break;
            }
        }
        boolean moi = d == null;
        if (moi) {
            d = new nro.repository.dao.BossSpawnDAO.Dong();
            d.bossId = bossId;
            d.ten = ten;
            d.soBanSao = 0;
        }
        // Boss KHONG phai hang so trong BossID thi may chu tra ma cua no trong
        // boss_spawn — nen dong do phai co TRUOC khi dung, khong thi tra khong
        // ra ma va bao "khong co lop Java nao nhan". Dung hut thi xoa dong di
        // ngay ben duoi, khong de lai rac.
        boolean ghiTruoc = tenHangBossIDPanel(bossId) == null;
        if (ghiTruoc) {
            nro.repository.dao.BossSpawnDAO.Dong tam =
                    new nro.repository.dao.BossSpawnDAO.Dong();
            tam.bossId = bossId;
            tam.ten = ten;
            tam.soBanSao = 0;
            tam.bat = true;
            nro.repository.dao.BossSpawnDAO.luu(tam);
        }
        int dung = 0;
        for (int i = 0; i < soCon; i++) {
            if (nro.service.boss.BossManager.gI().createBoss(bossId) != null) {
                dung++;
            }
        }
        if (dung == 0) {
            if (ghiTruoc) {
                nro.repository.dao.BossSpawnDAO.xoa(bossId);
            }
            return "Id " + bossId + " không dựng được con nào — kiểm lại số"
                    + " liệu của mã " + ten + ".";
        }
        d.soBanSao += dung;
        d.bat = true;
        String loi = nro.repository.dao.BossSpawnDAO.luu(d);
        if (loi != null) {
            return loi;
        }
        loadBosses();
        napNhom();
        napBossData();
        LamMoi.bao(LamMoi.BOSS);
        return null;
    }

    /**
     * Hộp chọn boss để đưa vào game, chia hai nhóm rõ ràng.
     *
     * <p>Cột <b>Trong game</b> nói ngay con nào đã có và có mấy con, nên không
     * phải nhớ hay đối chiếu sang tab khác.</p>
     */

    /**
     * Dòng đang chọn của một bảng, tự chọn dòng đầu nếu chưa chọn gì.
     *
     * <p>Bảng vừa nạp lại hoặc vừa lọc là mất lựa chọn. Bắt người dùng bấm lại
     * vào bảng mỗi lần như thế thì nút nhìn như hỏng — họ bấm, hiện "chưa chọn
     * dòng", mà trên bảng thì chẳng thấy khác gì.</p>
     *
     * @return chỉ số dòng, hoặc {@code -1} khi bảng rỗng thật
     */
    private static int dongDangChon(JTable t) {
        int r = t.getSelectedRow();
        if (r >= 0) {
            return r;
        }
        if (t.getRowCount() > 0) {
            t.setRowSelectionInterval(0, 0);
            return 0;
        }
        return -1;
    }

    /** Mã boss này đã có dòng trong {@code boss_data} chưa. */
    private static boolean coSoLieuBoss(String ma) {
        nro.repository.CrisResultSet rs = null;
        try {
            rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT ma FROM boss_data WHERE ma = ?", ma);
            return rs.next();
        } catch (Exception ex) {
            nro.core.log.Logger.logException(SystemPanel.class, ex,
                    "Không kiểm được số liệu boss " + ma);
            // Khong biet chac thi coi nhu DA CO: bao "chua co" roi tao de len
            // mot dong dang ton tai thi mat het so cu.
            return true;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                }
            }
        }
    }

    /**
     * Tạo dòng số liệu cho một mã boss chưa có.
     *
     * <p>Đặt sẵn số <b>dùng được</b> chứ không để rỗng: dòng rỗng sinh ra boss
     * sức đánh 0, đánh mãi không chết ai, mà nhìn bảng lại tưởng đã xong.</p>
     *
     * @return {@code true} nếu tạo xong
     */
    private boolean taoSoLieuBoss(java.awt.Component cha, String ma) {
        try {
            nro.repository.ConnectDB.executeUpdate(
                    "INSERT INTO boss_data (ma, ten, trang_phuc, suc_danh, hp,"
                    + " map_join, ky_nang, giay_hoi_sinh)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    ma, ma, "-1,-1,-1,-1,-1,-1", 1000L, "100000", "0", "1:1:2000", 3600);
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(cha,
                    "Không tạo được dòng số liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    /** Đọc lại một dòng của bảng thêm boss sau khi sửa số liệu. */
    private static void doLaiDongBoss(JTable t, int r, String ma,
            java.util.Map<String, String> tenDep,
            java.util.Map<String, String> trangPhuc) {
        nro.repository.CrisResultSet rs = null;
        try {
            rs = nro.repository.ConnectDB.executeQuery(
                    "SELECT ten, trang_phuc FROM boss_data WHERE ma = ?", ma);
            if (rs.next()) {
                String tn = rs.getString("ten");
                String tp = rs.getString("trang_phuc");
                if (tn != null && !tn.trim().isEmpty()) {
                    tenDep.put(ma, tn.trim());
                    t.setValueAt(tn.trim(), r, 1);
                }
                trangPhuc.put(ma, tp == null ? "" : tp.trim());
                t.setValueAt(tp == null || tp.trim().isEmpty()
                        ? "(chưa đặt)" : tp.trim(), r, 4);
                t.setValueAt("có", r, 3);
            }
        } catch (Exception boQua) {
            // Khong doc lai duoc thi bang hoi cu mot chut, khong sao.
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua2) {
                }
            }
        }
    }

    /**
     * Tạo hẳn một boss mới từ <b>một bộ cải trang</b>.
     *
     * <h3>Vì sao có nút này</h3>
     *
     * <p>Cách thêm boss cũ bắt chọn trong hơn hai trăm mã hằng kiểu
     * {@code TRUNG_UY_XANH_LO_BDKB} — nhìn mã không biết con đó ra sao, chọn
     * xong lại phải tự đi đặt trang phục. Ở đây làm ngược lại: chọn bộ cải
     * trang muốn boss mặc, mọi thứ còn lại máy tự lo.</p>
     *
     * <p>Lấy <b>đủ ba part</b> của bộ (đầu / thân / chân) chứ không chỉ một
     * mảnh, và kiểm luôn ba part đó có thật trong bảng {@code part} không —
     * thiếu part thì trong game boss hiện ra trắng trơn.</p>
     *
     * <p>Mã và id đều tự sinh, không cần khai hằng số trong mã nguồn: máy chủ
     * tra mã từ {@code boss_spawn} nên boss tạo kiểu này chạy như mọi con
     * khác.</p>
     */
    private void taoBossTuCaiTrang(java.awt.Component cha) {
        nro.entity.template.ItemTemplate ct = chonCaiTrang(cha);
        if (ct == null) {
            return;
        }
        canhBaoThieuPart(cha, ct.name, ct.head, ct.body, ct.leg);

        JTextField fTen = new JTextField(ct.name, 24);
        JTextField fDame = new JTextField("1.000", 12);
        JTextField fHp = new JTextField("100.000", 14);
        JTextField fHoiSinh = new JTextField("3.600", 8);
        JTextField fSoCon = new JTextField("1", 5);
        final int[] mapId = {0};
        JLabel lblMap = new JLabel(nro.ui.MapPicker.moTa("0"));
        lblMap.setForeground(GREY);
        JButton btnMap = button("Chọn bản đồ…", ACCENT, e -> {
            String moi = nro.ui.MapPicker.chon(cha, String.valueOf(mapId[0]));
            if (moi == null) {
                return;
            }
            java.util.List<Integer> so = nro.ui.MapPicker.tachSo(moi);
            if (!so.isEmpty()) {
                mapId[0] = so.get(0);
                lblMap.setText(nro.ui.MapPicker.moTa(String.valueOf(mapId[0])));
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        bdHang(form, c, y++, "Cải trang:",
                new JLabel(ct.name + "   (part " + ct.head + " / " + ct.body
                        + " / " + ct.leg + ")"), "");
        bdHang(form, c, y++, "Tên boss:", fTen, "tên hiện trong game");
        bdHang(form, c, y++, "Sức đánh:", fDame, "");
        bdHang(form, c, y++, "HP:", fHp, "");
        JPanel hangMap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangMap.setOpaque(false);
        hangMap.add(btnMap);
        hangMap.add(lblMap);
        bdHang(form, c, y++, "Bản đồ:", hangMap, "");
        bdHang(form, c, y++, "Hồi sinh:", fHoiSinh, "giây — 3.600 là một tiếng");
        bdHang(form, c, y++, "Số con:", fSoCon, "dựng ngay mấy con");

        if (JOptionPane.showConfirmDialog(cha, form, "Tạo boss từ cải trang",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        long dame;
        long hp;
        int hoiSinh;
        int soCon;
        try {
            dame = Long.parseLong(boCham(fDame.getText()));
            hp = Long.parseLong(boCham(fHp.getText()));
            hoiSinh = Integer.parseInt(boCham(fHoiSinh.getText()));
            soCon = Integer.parseInt(boCham(fSoCon.getText()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(cha,
                    "Sức đánh, HP, hồi sinh và số con phải là số.",
                    "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (soCon < 1) {
            JOptionPane.showMessageDialog(cha, "Số con phải từ 1 trở lên.",
                    "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ma = maTuDo("CT_" + ct.id);
        int idMoi = idBossTuDo();
        try {
            nro.repository.ConnectDB.executeUpdate(
                    "INSERT INTO boss_data (ma, ten, gioi_tinh, trang_phuc,"
                    + " suc_danh, hp, map_join, ky_nang, giay_hoi_sinh)"
                    + " VALUES (?, ?, 0, ?, ?, ?, ?, ?, ?)",
                    ma, fTen.getText().trim(),
                    ct.head + "," + ct.body + "," + ct.leg + ",-1,-1,-1",
                    dame, String.valueOf(hp), String.valueOf(mapId[0]),
                    "1:1:2000", hoiSinh);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(cha,
                    "Không tạo được số liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loi = themBossVaoGame(idMoi, ma, soCon);
        if (loi != null) {
            JOptionPane.showMessageDialog(cha,
                    "Đã tạo số liệu " + ma + " nhưng chưa dựng được:\n" + loi,
                    "Chưa dựng được", JOptionPane.WARNING_MESSAGE);
            return;
        }
        note(OK_GREEN, "Đã tạo boss \"" + fTen.getText().trim() + "\" (mã " + ma
                + ", id " + idMoi + ") và dựng " + soCon + " con.");
        napBossData();
        veLaiBoss();
    }

    /** Mã chưa ai dùng trong {@code boss_data}, dựa trên một mã gợi ý. */
    private static String maTuDo(String goiY) {
        String goc = goiY.replaceAll("[^A-Za-z0-9_]", "_");
        for (int i = 0; i < 500; i++) {
            String thu = i == 0 ? goc : goc + "_" + i;
            if (!coSoLieuBoss(thu)) {
                return thu;
            }
        }
        return goc + "_" + System.currentTimeMillis();
    }

    /**
     * Id boss chưa ai dùng.
     *
     * <p>Boss dùng id <b>âm</b>. Lấy nhỏ hơn id nhỏ nhất đang có một đơn vị,
     * và tránh cả những hằng số trong {@code BossID} — trùng vào đó thì lớp
     * Java riêng của con kia giành mất, boss vừa tạo không bao giờ hiện.</p>
     */
    private int idBossTuDo() {
        // Do TU -1000 tro xuong thay vi lay "nho hon hang so nho nhat mot don
        // vi": trong BossID co hang so am rat lon (-302470004), tru tiep tu do
        // ra nhung id vo nghia kieu -302470005.
        java.util.Set<Integer> daDung = new java.util.HashSet<>(
                danhMucBossID().keySet());
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            daDung.add(d.bossId);
        }
        for (int id = -1000; id > -100000; id--) {
            if (!daDung.contains(id)) {
                return id;
            }
        }
        return -1;
    }
    private void chonBossThemVaoGame(java.awt.Component cha) {
        java.util.Map<Integer, String> tatCa = danhMucBossID();
        java.util.Map<Integer, Integer> daCo = new java.util.HashMap<>();
        java.util.Map<Integer, String> tenDaCo = new java.util.HashMap<>();
        for (nro.repository.dao.BossSpawnDAO.Dong x
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            daCo.put(x.bossId, x.soBanSao);
            tenDaCo.put(x.bossId, x.ten);
        }

        // Ten hien thi va trang phuc doc tu boss_data, tra theo MA hang. Danh
        // sach nay von chi co ma hang kieu TRUNG_UY_XANH_LO_BDKB — nhin ma do
        // khong doan duoc con nao ra sao, nen rat de them nham.
        final java.util.Map<String, String> tenDep = new java.util.HashMap<>();
        final java.util.Map<String, String> trangPhuc = new java.util.HashMap<>();
        try {
            nro.repository.CrisResultSet rsBD = nro.repository.ConnectDB
                    .executeQuery("SELECT ma, ten, trang_phuc FROM boss_data");
            while (rsBD.next()) {
                String ma = rsBD.getString("ma");
                String tn = rsBD.getString("ten");
                if (tn != null && !tn.trim().isEmpty()) {
                    tenDep.put(ma, tn.trim());
                }
                String tp = rsBD.getString("trang_phuc");
                trangPhuc.put(ma, tp == null ? "" : tp.trim());
            }
            rsBD.dispose();
        } catch (Exception boQua) {
            // Khong doc duoc thi van hien duoc ma hang.
        }
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"ID", "Tên hiển thị", "Tên hằng", "Số liệu",
                    "Trang phục", "Trong game", "Thêm mấy con"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(22);

        JComboBox<String> cbLoc = new JComboBox<>(new String[]{
            "Chưa thêm vào game", "Đã thêm vào game", "Tất cả"});
        JTextField fTim = new JTextField(18);
        JLabel dem = new JLabel();
        dem.setForeground(GREY);

        final Runnable ve = () -> {
            String q = fTim.getText().trim().toLowerCase();
            int loc = Math.max(0, cbLoc.getSelectedIndex());
            m.setRowCount(0);
            for (java.util.Map.Entry<Integer, String> e : tatCa.entrySet()) {
                int id = e.getKey();
                boolean co = daCo.containsKey(id) && daCo.get(id) > 0;
                if (loc == 0 && co) {
                    continue;
                }
                if (loc == 1 && !co) {
                    continue;
                }
                String ten = e.getValue();
                String hien = tenDep.getOrDefault(ten, "");
                // Tim theo ca ten dep lan ma hang, go kieu nao cung ra.
                if (!q.isEmpty() && !ten.toLowerCase().contains(q)
                        && !hien.toLowerCase().contains(q)
                        && !String.valueOf(id).contains(q)) {
                    continue;
                }
                String tp = trangPhuc.get(ten);
                // Chua co dong trong boss_data thi lay luon ma hang lam ten:
                // ghi "(chua co)" o day thi ca 214 dong deu giong nhau, cot nay
                // thanh vo dung. Viec "co so lieu hay khong" tach ra cot rieng.
                m.addRow(new Object[]{id,
                    hien.isEmpty() ? ten : hien, ten,
                    hien.isEmpty() ? "chưa có" : "có",
                    tp == null || tp.isEmpty() ? "(chưa đặt)" : tp,
                    co ? daCo.get(id) + " con" : "chưa có", ""});
            }
            dem.setText("  " + m.getRowCount() + " dòng");
            // Luon chon san mot dong. Hop mo ra khong co dong nao duoc chon,
            // nen bam thang vao nut la an "chua chon dong" — nguoi dung tuong
            // nut hong chu khong doan ra la phai bam vao bang truoc.
            if (m.getRowCount() > 0 && t.getSelectedRow() < 0) {
                t.setRowSelectionInterval(0, 0);
            }
        };
        cbLoc.addActionListener(e -> ve.run());
        fTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }
        });
        ve.run();
        int[] w = {55, 190, 190, 70, 140, 85, 85};
        for (int i = 0; i < t.getColumnCount() && i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.setOpaque(false);
        top.add(new JLabel("Xem:"));
        top.add(cbLoc);
        top.add(new JLabel("   Tìm:"));
        top.add(fTim);
        top.add(dem);
        JPanel nutHang = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nutHang.setOpaque(false);
        // Hai nut TACH RIENG chu khong gop lam mot: gop lai thi nhin nut khong
        // biet dong dang chon se duoc TAO hay bi SUA, ma hai viec do khac han
        // nhau — mot cai sinh ra dong moi, mot cai dong vao dong dang co.
        nutHang.add(button("Tạo số liệu mới…", OK_GREEN, e -> {
            int r = dongDangChon(t);
            if (r < 0) {
                JOptionPane.showMessageDialog(cha,
                        "Bảng đang trống — đổi bộ lọc \"Xem\" hoặc xoá ô Tìm.",
                        "Không có dòng nào", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = String.valueOf(t.getValueAt(r, 2));
            if (coSoLieuBoss(ma)) {
                JOptionPane.showMessageDialog(cha,
                        ma + " đã có số liệu rồi.\nDùng nút \"Sửa số liệu…\" "
                        + "để chỉnh, hoặc chọn một dòng còn ghi \"chưa có\".",
                        "Đã có số liệu", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (!taoSoLieuBoss(cha, ma)) {
                return;
            }
            bdSuaTheoMa(ma, (Integer) t.getValueAt(r, 0));
            doLaiDongBoss(t, r, ma, tenDep, trangPhuc);
        }));
        nutHang.add(button("Sửa số liệu…", ACCENT, e -> {
            int r = dongDangChon(t);
            if (r < 0) {
                JOptionPane.showMessageDialog(cha,
                        "Bảng đang trống — đổi bộ lọc \"Xem\" hoặc xoá ô Tìm.",
                        "Không có dòng nào", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = String.valueOf(t.getValueAt(r, 2));
            if (!coSoLieuBoss(ma)) {
                JOptionPane.showMessageDialog(cha,
                        ma + " chưa có số liệu.\nBấm \"Tạo số liệu mới…\" trước đã.",
                        "Chưa có số liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            bdSuaTheoMa(ma, (Integer) t.getValueAt(r, 0));
            doLaiDongBoss(t, r, ma, tenDep, trangPhuc);
        }));
        nutHang.add(button("Chọn cải trang cho dòng đang chọn…", ACCENT, e -> {
            int r = dongDangChon(t);
            if (r < 0) {
                JOptionPane.showMessageDialog(cha,
                        "Bảng đang trống — đổi bộ lọc \"Xem\" hoặc xoá ô Tìm.",
                        "Không có dòng nào", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = String.valueOf(t.getValueAt(r, 2));
            String dangCo = trangPhuc.get(ma);
            String moi = chonTrangPhucBoss(cha, dangCo == null ? "" : dangCo);
            if (moi == null) {
                return;
            }
            try {
                // Ghi thang vao boss_data. Con nao chua co dong thi tao moi,
                // lay luon ma hang lam ten tam — khong thi chon xong bam OK
                // van khong luu duoc gi ma cung khong bao loi.
                int n = nro.repository.ConnectDB.executeUpdate(
                        "UPDATE boss_data SET trang_phuc = ? WHERE ma = ?",
                        moi, ma);
                if (n == 0) {
                    // Chua co dong -> tao moi. Dat san so lieu DUNG DUOC chu
                    // khong de rong: dong rong sinh ra boss dame 0, danh mai
                    // khong chet ai, ma nhin bang thi tuong da xong.
                    nro.repository.ConnectDB.executeUpdate(
                            "INSERT INTO boss_data (ma, ten, trang_phuc,"
                            + " suc_danh, hp, map_join, ky_nang, giay_hoi_sinh)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            ma, ma, moi, 1000L, "100000", "0", "1:1:2000", 3600);
                    note(OK_GREEN, "Đã tạo số liệu mặc định cho " + ma
                            + " — sửa lại bằng nút \"Sửa số liệu\".");
                }
                trangPhuc.put(ma, moi);
                t.setValueAt(moi.isEmpty() ? "(chưa đặt)" : moi, r, 4);
                note(OK_GREEN, "Đã đặt trang phục cho " + ma + ".");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(cha,
                        "Không lưu được trang phục: " + ex.getMessage(),
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        }));

        JPanel dau = new JPanel();
        dau.setOpaque(false);
        dau.setLayout(new javax.swing.BoxLayout(dau, javax.swing.BoxLayout.Y_AXIS));
        top.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        nutHang.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        dau.add(top);
        dau.add(nutHang);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(940, 500));
        root.add(dau, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);
        JLabel chu = new JLabel("<html><body style='width:600px'>"
                + "Điền số vào cột <b>Thêm mấy con</b> ở những dòng muốn đưa vào "
                + "game, rồi bấm OK. Boss hiện ra <b>ngay</b>, không phải khởi "
                + "động lại.<br>Id nào không có lớp Java nhận thì không dựng "
                + "được — hộp thoại sẽ báo tên con đó.</body></html>");
        chu.setForeground(GREY);
        root.add(chu, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(cha, root, "Thêm boss vào game",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (t.isEditing()) {
            t.getCellEditor().stopCellEditing();
        }

        int xong = 0;
        java.util.List<String> loi = new java.util.ArrayList<>();
        for (int i = 0; i < m.getRowCount(); i++) {
            int so = soHoacMacDinh(m.getValueAt(i, 6), 0);
            if (so <= 0) {
                continue;
            }
            int id = (Integer) m.getValueAt(i, 0);
            // Cot 2 la MA hang — themBossVaoGame luu ma nay vao boss_spawn.
            String maHang = String.valueOf(m.getValueAt(i, 2));
            String e = themBossVaoGame(id, maHang, so);
            if (e == null) {
                xong++;
            } else {
                loi.add(m.getValueAt(i, 1) + " (" + maHang + "): " + e);
            }
        }
        if (xong == 0 && loi.isEmpty()) {
            note(GREY, "Chưa điền số con nào — không thêm gì cả.");
            return;
        }
        if (loi.isEmpty()) {
            note(OK_GREEN, "Đã thêm " + xong + " loại boss vào game.");
        } else {
            JOptionPane.showMessageDialog(cha,
                    "Thêm được " + xong + " loại.\nKhông thêm được:\n"
                    + String.join("\n", loi),
                    "Có dòng lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    // =====================================================================
    //  Tab 3 — nhóm boss
    // =====================================================================

    private final DefaultTableModel nhomModel = new DefaultTableModel(
            new Object[]{"ID", "Tên nhóm", "Chu kỳ", "Còn lại", "Số boss",
                "Cùng khu", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable nhomTable = new JTable(nhomModel);

    /**
     * Màn quản nhóm hồi sinh.
     *
     * <p>Nhóm là một cái đồng hồ dùng chung: cứ hết một chu kỳ là mọi con boss
     * trong nhóm đang nằm chờ cùng hiện lại. Đồng hồ <b>chạy liên tục</b>,
     * không phụ thuộc con nào chết lúc nào — nhóm còn mười lăm phút nữa tới
     * lượt mà một con bị hạ thì con đó chờ đúng mười lăm phút rồi lên cùng
     * những con kia.</p>
     *
     * <p>Boss không thuộc nhóm nào vẫn đếm giờ riêng như trước.</p>
     */
    private JComponent buildNhomTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel canBiet = new JLabel("<html><body style='width:900px'>"
                + "Nhóm dùng <b>một đồng hồ chung</b>: hết mỗi chu kỳ thì mọi "
                + "con trong nhóm đang nằm chờ cùng hiện lại.<br>Đồng hồ chạy "
                + "liên tục, không phụ thuộc con nào chết lúc nào — còn 15 phút "
                + "nữa tới lượt mà một con bị hạ thì con đó chờ đúng 15 phút "
                + "rồi lên cùng cả nhóm.<br>Boss không thuộc nhóm nào vẫn đếm "
                + "giờ riêng như cũ.</body></html>");
        canBiet.setForeground(GREY);
        canBiet.setBorder(new EmptyBorder(0, 2, 6, 2));
        root.add(canBiet, BorderLayout.NORTH);

        nhomTable.setRowHeight(24);
        nhomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nhomTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        int[] w = {50, 220, 110, 110, 90, 55, 300};
        for (int i = 0; i < nhomTable.getColumnCount() && i < w.length; i++) {
            nhomTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        nhomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    nhomSua(false);
                }
            }
        });
        root.add(oLocBang(nhomTable, "gõ tên nhóm, tên boss hay bản đồ"),
                BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(nhomTable), BorderLayout.CENTER);

        JPanel nut = new JPanel(new GridLayout(0, 5, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Tạo nhóm", OK_GREEN, e -> nhomSua(true)));
        nut.add(button("Sửa nhóm", ACCENT, e -> nhomSua(false)));
        nut.add(button("Chọn boss cho nhóm…", ACCENT, e -> nhomChonBoss()));
        nut.add(button("Hồi sinh cả nhóm", new Color(0, 140, 80),
                e -> nhomHoiSinhTatCa()));
        nut.add(button("Xoá nhóm", WARN_RED, e -> nhomXoa()));
        nut.add(button("Tải lại", GREY, e -> napNhom()));
        root.add(nut, BorderLayout.SOUTH);

        // Dong ho chay lien tuc nen cot "Con lai" phai tu nhich, khong thi nhin
        // nhu dung hinh va nguoi dung tuong nhom hong.
        new javax.swing.Timer(2000, e -> napNhom()).start();
        napNhom();
        return root;
    }

    private void napNhom() {
        int giu = nhomTable.getSelectedRow();
        nhomModel.setRowCount(0);
        java.util.Map<Integer, Integer> demBoss = new java.util.HashMap<>();
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            if (d.nhomId > 0) {
                demBoss.merge(d.nhomId, 1, Integer::sum);
            }
        }
        for (nro.service.boss.NhomBossService.Nhom n
                : nro.service.boss.NhomBossService.gI().tatCa()) {
            nhomModel.addRow(new Object[]{n.id, n.ten,
                moTaThoiGian(n.chuKyGiay),
                n.bat ? moTaThoiGian(n.conLaiGiay()) : "-",
                demBoss.getOrDefault(n.id, 0) + " loại",
                n.cungKhu ? "có" : "", n.bat ? "bật" : "tắt", n.ghiChu});
        }
        if (giu >= 0 && giu < nhomModel.getRowCount()) {
            nhomTable.setRowSelectionInterval(giu, giu);
        }
    }

    private int nhomDangChon() {
        int r = nhomTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một nhóm trong bảng trước.");
            return -1;
        }
        return Integer.parseInt(String.valueOf(nhomModel.getValueAt(r, 0)));
    }

    private void nhomSua(boolean them) {
        int id = -1;
        String ten = "";
        int chuKy = 3600;
        boolean bat = true;
        // Nhom moi mac dinh BAT "cung khu": lap nhom boss von la de chung di
        // voi nhau. De mac dinh tat thi tao nhom xong van thay moi con mot noi,
        // khong doan ra la con thieu mot o tich.
        boolean cungKhu = true;
        String ghiChu = "";
        if (!them) {
            id = nhomDangChon();
            if (id < 0) {
                return;
            }
            for (nro.service.boss.NhomBossService.Nhom n
                    : nro.service.boss.NhomBossService.gI().tatCa()) {
                if (n.id == id) {
                    ten = n.ten;
                    chuKy = n.chuKyGiay;
                    bat = n.bat;
                    cungKhu = n.cungKhu;
                    ghiChu = n.ghiChu;
                    break;
                }
            }
        }

        JTextField fTen = new JTextField(ten, 24);
        JTextField fChuKy = new JTextField(PlayerManagerPanel.fmt(chuKy), 10);
        JTextField fGhiChu = new JTextField(ghiChu, 30);
        JCheckBox cbBat = new JCheckBox("Bật", bat);
        JCheckBox cbCungKhu = new JCheckBox(
                "Cùng xuất hiện một khu của một bản đồ", cungKhu);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        bdHang(form, c, y++, "Tên nhóm:", fTen, "ví dụ: Black Goku");
        bdHang(form, c, y++, "Chu kỳ:", fChuKy, "giây — 3.600 là một tiếng");
        bdHang(form, c, y++, "Ghi chú:", fGhiChu, "");
        c.gridx = 1;
        c.gridy = y++;
        form.add(cbBat, c);
        c.gridx = 1;
        c.gridy = y++;
        form.add(cbCungKhu, c);
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        form.add(new JLabel("<html><span style=.color:#777.>"
                + "Tắt <b>cùng khu</b> thì nhóm chỉ đồng bộ <i>giờ</i> hồi sinh, "
                + "mỗi con vẫn tự chọn khu riêng.<br>"
                + "Bật thì con lên trước chọn khu, những con còn lại vào đúng "
                + "khu đó — kể cả khi mỗi con khai một danh sách bản đồ khác nhau, "
                + "và áp cho <b>mọi bản sao</b> của chúng.</span></html>"), c);
        c.gridwidth = 1;

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Tạo nhóm boss" : "Sửa nhóm " + id,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int ck;
        try {
            ck = Integer.parseInt(boCham(fChuKy.getText()));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Chu kỳ phải là số giây.");
            return;
        }
        if (ck < 5) {
            note(WARN_RED, "Chu kỳ phải từ 5 giây trở lên.");
            return;
        }
        try {
            nro.service.boss.NhomBossService.damBaoBang();
            if (them) {
                nro.repository.ConnectDB.executeUpdate(
                        "INSERT INTO boss_nhom (ten, chu_ky_giay, bat, ghi_chu,"
                        + " cung_khu) VALUES (?, ?, ?, ?, ?)",
                        fTen.getText().trim(), ck, cbBat.isSelected() ? 1 : 0,
                        fGhiChu.getText().trim(), cbCungKhu.isSelected() ? 1 : 0);
            } else {
                nro.repository.ConnectDB.executeUpdate(
                        "UPDATE boss_nhom SET ten = ?, chu_ky_giay = ?, bat = ?,"
                        + " ghi_chu = ?, cung_khu = ? WHERE id = ?",
                        fTen.getText().trim(), ck, cbBat.isSelected() ? 1 : 0,
                        fGhiChu.getText().trim(),
                        cbCungKhu.isSelected() ? 1 : 0, id);
            }
        } catch (Exception ex) {
            note(WARN_RED, "Không lưu được: " + ex.getMessage());
            return;
        }
        nro.service.boss.NhomBossService.gI().napLai();
        napNhom();
        note(OK_GREEN, "Đã lưu nhóm — có hiệu lực ngay.");
    }

    private void nhomXoa() {
        int id = nhomDangChon();
        if (id < 0) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá nhóm " + id + "?\nBoss trong nhóm quay về đếm giờ riêng.",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            nro.repository.ConnectDB.executeUpdate(
                    "DELETE FROM boss_nhom WHERE id = ?", id);
            nro.repository.ConnectDB.executeUpdate(
                    "UPDATE boss_spawn SET nhom_id = 0 WHERE nhom_id = ?", id);
        } catch (Exception ex) {
            note(WARN_RED, "Không xoá được: " + ex.getMessage());
            return;
        }
        nro.service.boss.NhomBossService.gI().napLai();
        napNhom();
        note(OK_GREEN, "Đã xoá nhóm " + id + ".");
    }

    /** Bảng tích chọn boss nào thuộc nhóm đang chọn. */

    /** Gọi dậy mọi con trong nhóm đang chọn, kể cả mọi bản sao. */
    private void nhomHoiSinhTatCa() {
        int id = nhomDangChon();
        if (id < 0) {
            return;
        }
        String ten = "";
        for (nro.service.boss.NhomBossService.Nhom n
                : nro.service.boss.NhomBossService.gI().tatCa()) {
            if (n.id == id) {
                ten = n.ten;
                break;
            }
        }
        if (JOptionPane.showConfirmDialog(this,
                "Cho toàn bộ boss trong nhóm \"" + ten + "\" xuất hiện ngay?\n"
                + "Áp cho mọi bản sao, và các cặp sẽ bốc lại khu từ đầu.",
                "Hồi sinh cả nhóm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        java.util.List<String> loi = new java.util.ArrayList<>();
        int[] kq = nro.service.boss.NhomBossService.gI().hoiSinhCaNhom(id, loi);
        if (kq[0] == 0 && kq[1] == 0) {
            note(WARN_RED, "Nhóm này chưa có con boss nào trong game.");
            return;
        }
        if (kq[1] == 0) {
            note(OK_GREEN, "Đã gọi " + kq[0] + " con trong nhóm \"" + ten
                    + "\" — sẽ xuất hiện trong vài giây.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Gọi được " + kq[0] + " con.\nKhông gọi được " + kq[1]
                    + " con:\n" + String.join("\n", loi),
                    "Có con chưa gọi được", JOptionPane.WARNING_MESSAGE);
        }
        veLaiBoss();
        napNhom();
    }
    private void nhomChonBoss() {
        int id = nhomDangChon();
        if (id < 0) {
            return;
        }
        java.util.List<nro.repository.dao.BossSpawnDAO.Dong> ds
                = nro.repository.dao.BossSpawnDAO.tatCa();
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có boss nào trong game.\nThêm boss ở tab 2 trước.",
                    "Chưa có boss", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Ten hien thi lay tu boss_data (cot "ten"), tra theo ma. Bang
        // boss_spawn chi giu MA hang so kieu TIEU_DOI_TRUONG — doc ma do ma
        // doan ra con nao thi rat de tich nham.
        final java.util.Map<String, String> tenDep = new java.util.HashMap<>();
        try {
            nro.repository.CrisResultSet rsTen = nro.repository.ConnectDB
                    .executeQuery("SELECT ma, ten FROM boss_data");
            while (rsTen.next()) {
                String t = rsTen.getString("ten");
                if (t != null && !t.trim().isEmpty()) {
                    tenDep.put(rsTen.getString("ma"), t.trim());
                }
            }
            rsTen.dispose();
        } catch (Exception boQua) {
            // Khong doc duoc thi van hien duoc ma hang so.
        }
        final java.util.Set<Integer> chon = new java.util.LinkedHashSet<>();
        for (nro.repository.dao.BossSpawnDAO.Dong d : ds) {
            if (d.nhomId == id) {
                chon.add(d.bossId);
            }
        }

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Chọn", "ID", "Tên hiển thị", "Mã hằng", "Số con",
                    "Nhóm hiện tại"}, 0) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(22);
        JTextField fTim = new JTextField(18);
        JLabel dem = new JLabel();
        dem.setForeground(GREY);

        final Runnable ve = () -> {
            String q = fTim.getText().trim().toLowerCase();
            m.setRowCount(0);
            for (nro.repository.dao.BossSpawnDAO.Dong d : ds) {
                String hien = tenDep.getOrDefault(d.ten, d.ten);
                // Tim theo CA ten dep lan ma hang, de go kieu nao cung ra.
                if (!q.isEmpty() && !d.ten.toLowerCase().contains(q)
                        && !hien.toLowerCase().contains(q)
                        && !String.valueOf(d.bossId).contains(q)) {
                    continue;
                }
                String nhomCu = d.nhomId <= 0 ? "—"
                        : (d.nhomId == id ? "nhóm này" : "nhóm " + d.nhomId);
                m.addRow(new Object[]{chon.contains(d.bossId), d.bossId, hien,
                    d.ten, d.soBanSao + " con", nhomCu});
            }
            dem.setText("  Đang chọn: " + chon.size() + " loại boss");
        };
        m.addTableModelListener(e -> {
            if (e.getColumn() != 0 || e.getFirstRow() < 0) {
                return;
            }
            for (int r = e.getFirstRow();
                    r <= e.getLastRow() && r < m.getRowCount(); r++) {
                int bid = (Integer) m.getValueAt(r, 1);
                if (Boolean.TRUE.equals(m.getValueAt(r, 0))) {
                    chon.add(bid);
                } else {
                    chon.remove(bid);
                }
            }
            dem.setText("  Đang chọn: " + chon.size() + " loại boss");
        });
        fTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }
        });
        ve.run();
        int[] w = {50, 70, 240, 80, 120};
        for (int i = 0; i < t.getColumnCount() && i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.setOpaque(false);
        top.add(new JLabel("Tìm theo tên hoặc id:"));
        top.add(fTim);
        top.add(button("Bỏ tích hết", GREY, e -> {
            chon.clear();
            ve.run();
        }));
        top.add(dem);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(640, 420));
        root.add(top, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);
        JLabel chu = new JLabel("<html><body style='width:600px'>Một loại boss "
                + "chỉ thuộc <b>một</b> nhóm. Tích vào đây thì nó rời nhóm cũ "
                + "sang nhóm này.</body></html>");
        chu.setForeground(GREY);
        root.add(chu, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, root,
                "Chọn boss cho nhóm " + id,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            // Go het khoi nhom nay truoc roi gan lai: lam vay thi bo tich mot
            // con cung co tac dung, chu khong chi co viec them.
            nro.repository.ConnectDB.executeUpdate(
                    "UPDATE boss_spawn SET nhom_id = 0 WHERE nhom_id = ?", id);
            for (int bid : chon) {
                nro.repository.ConnectDB.executeUpdate(
                        "UPDATE boss_spawn SET nhom_id = ? WHERE boss_id = ?",
                        id, bid);
            }
        } catch (Exception ex) {
            note(WARN_RED, "Không lưu được: " + ex.getMessage());
            return;
        }
        nro.service.boss.NhomBossService.gI().napLai();
        napNhom();
        note(OK_GREEN, "Đã gán " + chon.size() + " loại boss vào nhóm " + id + ".");
    }

    /**
     * Có bao nhiêu con boss mang mỗi mã, tính trên máy chủ đang chạy.
     *
     * <p>Đếm từ sổ đăng ký chứ không từ {@code boss_spawn}: bảng đó nói con nào
     * sẽ được dựng lúc khởi động, còn đây là số <b>thật sự đang có</b> — hai số
     * lệch nhau khi ai đó vừa thêm boss mà chưa khởi động lại.</p>
     */
    private java.util.Map<String, Integer> demBossTheoId() {
        java.util.Map<String, Integer> ra = new java.util.HashMap<>();
        for (nro.entity.boss.Boss b : nro.entity.boss.Boss.tatCaBoss()) {
            if (b != null) {
                ra.merge(String.valueOf(b.id), 1, Integer::sum);
            }
        }
        return ra;
    }

    /**
     * Chọn một bộ cải trang, lấy luôn ba mảnh đầu/thân/chân của nó.
     *
     * <p>Chỉ liệt kê vật phẩm <b>loại 5 — Cải trang</b>. Bộ chọn vật phẩm chung
     * đổ ra hơn hai nghìn dòng, mà trang phục boss thì gần như luôn là một bộ
     * cải trang; lọc sẵn thì khỏi phải dò.</p>
     *
     * @return mẫu vật phẩm đã chọn, hoặc {@code null} nếu huỷ
     */
    private nro.entity.template.ItemTemplate chonCaiTrang(java.awt.Component cha) {
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Ảnh", "ID", "Tên cải trang", "Đầu", "Thân", "Chân"}, 0) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? javax.swing.ImageIcon.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(32);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextField fTim = new JTextField(20);
        JLabel dem = new JLabel();
        dem.setForeground(GREY);
        final java.util.List<nro.entity.template.ItemTemplate> hien
                = new java.util.ArrayList<>();

        final Runnable ve = () -> {
            String q = fTim.getText().trim().toLowerCase();
            m.setRowCount(0);
            hien.clear();
            for (nro.entity.template.ItemTemplate it : nro.server.Manager.ITEM_TEMPLATES) {
                if (it == null || it.type != 5) {
                    continue;
                }
                String ten = it.name == null ? "" : it.name;
                if (!q.isEmpty() && !ten.toLowerCase().contains(q)
                        && !String.valueOf(it.id).contains(q)) {
                    continue;
                }
                hien.add(it);
                m.addRow(new Object[]{PlayerManagerPanel.iconOf(it.iconID),
                    it.id, ten, it.head, it.body, it.leg});
            }
            dem.setText("  " + m.getRowCount() + " bộ");
        };
        fTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }
        });
        ve.run();
        int[] w = {45, 60, 260, 70, 70, 70};
        for (int i = 0; i < t.getColumnCount() && i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.setOpaque(false);
        top.add(new JLabel("Tìm theo tên hoặc id:"));
        top.add(fTim);
        top.add(dem);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(620, 440));
        root.add(top, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);
        JLabel chu = new JLabel("<html><body style='width:580px'>Chọn xong sẽ "
                + "lấy cả ba mảnh <b>Đầu / Thân / Chân</b> của bộ đó. Cải trang "
                + "vốn đi liền ba mảnh — chọn rời từng mảnh rất dễ lệch bộ."
                + "</body></html>");
        chu.setForeground(GREY);
        root.add(chu, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(cha, root, "Chọn cải trang cho boss",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        int r = t.getSelectedRow();
        if (r < 0) {
            return null;
        }
        // Doc tu "hien" chu khong tu bang: bang chi chua nhung dong sau khi loc,
        // nhung chi so dong thi khop nhau vi hai ben dung cung mot vong duyet.
        int i = t.convertRowIndexToModel(r);
        return i >= 0 && i < hien.size() ? hien.get(i) : null;
    }

    /**
     * Chọn một mã bộ phận (đầu, thân hoặc chân) từ danh sách.
     *
     * <p>Bộ phận <b>không có ảnh riêng</b> để bày ra: cả bộ nằm trong một khối
     * nhị phân {@code data/update_data/part} mà máy chủ chỉ gửi nguyên khối cho
     * client, không tách được từng mảnh.</p>
     *
     * <p>Nên cách nhìn được là <b>qua vật phẩm đang dùng mã đó</b> — mỗi mã bộ
     * phận dùng được đều thuộc về một món đồ nào đó, và món đó thì có ảnh. Cột
     * "Vật phẩm dùng" cho biết mã này trông ra sao.</p>
     *
     * @param cot 0 đầu, 1 thân, 2 chân
     * @return mã bộ phận đã chọn, hoặc {@code null} nếu huỷ
     */
    private Integer chonPart(java.awt.Component cha, int cot) {
        String[] tenCot = {"Đầu", "Thân", "Chân"};
        // Gom theo ma bo phan: mot ma co the duoc nhieu mon dung chung.
        java.util.Map<Integer, java.util.List<nro.entity.template.ItemTemplate>> theoMa
                = new java.util.TreeMap<>();
        for (nro.entity.template.ItemTemplate it : nro.server.Manager.ITEM_TEMPLATES) {
            if (it == null) {
                continue;
            }
            int v = cot == 0 ? it.head : (cot == 1 ? it.body : it.leg);
            if (v < 0) {
                continue;
            }
            theoMa.computeIfAbsent(v, k -> new java.util.ArrayList<>()).add(it);
        }

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"Ảnh", "Mã " + tenCot[cot].toLowerCase(),
                    "Vật phẩm dùng mã này", "Số món"}, 0) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? javax.swing.ImageIcon.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(32);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextField fTim = new JTextField(20);
        JLabel dem = new JLabel();
        dem.setForeground(GREY);
        final java.util.List<Integer> hien = new java.util.ArrayList<>();

        final Runnable ve = () -> {
            String q = fTim.getText().trim().toLowerCase();
            m.setRowCount(0);
            hien.clear();
            for (java.util.Map.Entry<Integer,
                    java.util.List<nro.entity.template.ItemTemplate>> e
                    : theoMa.entrySet()) {
                java.util.List<nro.entity.template.ItemTemplate> ds = e.getValue();
                StringBuilder ten = new StringBuilder();
                for (int i = 0; i < ds.size() && i < 3; i++) {
                    ten.append(i > 0 ? ", " : "").append(ds.get(i).name);
                }
                if (ds.size() > 3) {
                    ten.append("…");
                }
                if (!q.isEmpty() && !ten.toString().toLowerCase().contains(q)
                        && !String.valueOf(e.getKey()).contains(q)) {
                    continue;
                }
                hien.add(e.getKey());
                m.addRow(new Object[]{
                    PlayerManagerPanel.iconOf(ds.get(0).iconID),
                    e.getKey(), ten.toString(), ds.size()});
            }
            dem.setText("  " + m.getRowCount() + " mã");
        };
        fTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }
        });
        ve.run();
        int[] w = {45, 80, 320, 60};
        for (int i = 0; i < t.getColumnCount() && i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.setOpaque(false);
        top.add(new JLabel("Tìm theo tên vật phẩm hoặc mã:"));
        top.add(fTim);
        top.add(dem);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(620, 440));
        root.add(top, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(cha, root,
                "Chọn mã " + tenCot[cot].toLowerCase(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        int r = t.getSelectedRow();
        if (r < 0) {
            return null;
        }
        int i = t.convertRowIndexToModel(r);
        return i >= 0 && i < hien.size() ? hien.get(i) : null;
    }

    /**
     * Đổi số con của một loại boss trong máy chủ.
     *
     * <p><b>Tăng thì có hiệu lực ngay</b> — dựng thêm đúng số còn thiếu.
     * <b>Giảm thì phải khởi động lại</b>: gỡ một con boss đang sống cho sạch
     * còn phải rút nó khỏi khu, huỷ đám boss đi kèm và mấy manager riêng của
     * nó; làm nửa vời sẽ để lại con ma vô hình mà người chơi vẫn đánh trúng.
     * Nên ở đây chỉ ghi số mới xuống CSDL và nói rõ.</p>
     */
    private void bdSuaSoCon() {
        int id = bdSelectedBossId();
        if (id == Integer.MIN_VALUE) {
            note(WARN_RED, "Mẫu này chưa có boss nào dùng — thêm boss vào game trước.");
            return;
        }
        nro.repository.dao.BossSpawnDAO.Dong d = null;
        for (nro.repository.dao.BossSpawnDAO.Dong x
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            if (x.bossId == id) {
                d = x;
                break;
            }
        }
        if (d == null) {
            note(WARN_RED, "Boss " + id + " không nằm trong danh sách — "
                    + "dùng nút \"Thêm boss vào game\".");
            return;
        }

        int dangCo = 0;
        for (nro.entity.boss.Boss b : nro.entity.boss.Boss.tatCaBoss()) {
            if (b != null && b.id == id) {
                dangCo++;
            }
        }

        JTextField fSo = new JTextField(String.valueOf(d.soBanSao), 8);

        // Nhom quyet dinh gio hoi sinh; khong theo nhom thi dung so rieng cua
        // mau ben o "Hoi sinh".
        java.util.List<nro.service.boss.NhomBossService.Nhom> dsNhom
                = nro.service.boss.NhomBossService.gI().tatCa();
        JComboBox<String> cbNhom = new JComboBox<>();
        cbNhom.addItem("Không theo nhóm");
        int viTri = 0;
        for (int i = 0; i < dsNhom.size(); i++) {
            nro.service.boss.NhomBossService.Nhom n = dsNhom.get(i);
            cbNhom.addItem(n.id + " — " + n.ten + "  (" + moTaThoiGian(n.chuKyGiay) + ")");
            if (n.id == d.nhomId) {
                viTri = i + 1;
            }
        }
        cbNhom.setSelectedIndex(viTri);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        bdHang(form, c, y++, "Boss:", new JLabel(d.ten + "  (id " + id + ")"), "");
        bdHang(form, c, y++, "Đang có thật:", new JLabel(dangCo + " con"),
                "đếm trong máy chủ đang chạy");
        bdHang(form, c, y++, "Số con:", fSo, "0 đến 500");
        bdHang(form, c, y++, "Nhóm hồi sinh:", cbNhom,
                "để trống nhóm thì dùng giây hồi sinh riêng của mẫu");
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 3;
        JLabel chu = new JLabel("<html><body style='width:520px'><i>"
                + "Tăng thì dựng thêm <b>ngay</b>. Giảm thì chỉ ghi số mới, "
                + "phải <b>khởi động lại máy chủ</b> mới bớt — gỡ boss đang "
                + "sống giữa chừng dễ để lại con ma vô hình."
                + "</i></body></html>");
        chu.setForeground(GREY);
        form.add(chu, c);

        if (JOptionPane.showConfirmDialog(this, form,
                "Số con của " + d.ten,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int moi;
        try {
            moi = Integer.parseInt(boCham(fSo.getText()));
        } catch (NumberFormatException ex) {
            note(WARN_RED, "Số con phải là số nguyên.");
            return;
        }

        int themNua = moi - dangCo;
        int dungDuoc = 0;
        for (int i = 0; i < themNua; i++) {
            if (nro.service.boss.BossManager.gI().createBoss(id) != null) {
                dungDuoc++;
            }
        }
        d.soBanSao = moi;
        int iN = cbNhom.getSelectedIndex();
        d.nhomId = iN <= 0 ? 0 : dsNhom.get(iN - 1).id;
        String loi = nro.repository.dao.BossSpawnDAO.luu(d);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadBosses();
        napBossData();
        napNhom();
        LamMoi.bao(LamMoi.BOSS);
        note(OK_GREEN, themNua > 0
                ? "Đã dựng thêm " + dungDuoc + " con — có ngay trong game."
                : "Đã ghi số " + moi + " — khởi động lại máy chủ để bớt con.");
    }

    /**
     * Ô "Số con": <b>số đã khai</b> là số chính, số đang sống chỉ ghi kèm khi
     * lệch.
     *
     * <p>Số khai mới là thứ người dùng vừa sửa, nên phải là số đập vào mắt —
     * để số sống lên trước thì sửa xong nhìn như không lưu, đúng chuyện đã xảy
     * ra.</p>
     *
     * <p>Hai số lệch được vì {@code Map.initBoss} còn dựng boss theo bản đồ,
     * và vì bớt số con thì phải khởi động lại mới có hiệu lực.</p>
     */
    private static String moTaSoCon(String idBoss,
            java.util.Map<String, Integer> song,
            java.util.Map<String, Integer> khai) {
        int s = song.getOrDefault(idBoss, 0);
        Integer k = khai.get(idBoss);
        if (k == null) {
            return s == 0 ? "—" : "— (đang có " + s + ")";
        }
        return s == k ? k + " con" : k + " con (đang có " + s + ")";
    }

    // =====================================================================
    //  Tab 4 — boss riêng của bản đồ
    // =====================================================================

    private final DefaultTableModel bmModel = new DefaultTableModel(
            new Object[]{"ID", "Bản đồ", "Boss", "Mỗi khu", "Bật", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bmTable = new JTable(bmModel);

    /**
     * Màn quản boss gắn với một bản đồ cụ thể.
     *
     * <p>Đây là <b>đường sinh boss thứ hai</b>, khác hẳn danh sách boss thường:
     * boss ở đây mọc trong <b>từng khu</b> của đúng một bản đồ — kiểu boss hang,
     * boss phó bản. Bản đồ hai chục khu thì ra hai chục con, và đó là lý do một
     * mình Tàu Pảy Pảy chiếm hơn năm chục con trong máy chủ.</p>
     */
    private JComponent buildBossMapTab() {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel canBiet = new JLabel("<html><body style='width:900px'>"
                + "Boss gắn với <b>một bản đồ cụ thể</b> — boss hang, boss phó "
                + "bản. Khác danh sách ở tab 1: chúng mọc trong <b>từng khu</b> "
                + "của đúng bản đồ đó.<br>Bản đồ có hai chục khu thì ra hai "
                + "chục con — vì vậy một mình Tàu Pảy Pảy chiếm hơn năm chục "
                + "con trong máy chủ.<br><b>Sửa xong phải khởi động lại máy "
                + "chủ.</b></body></html>");
        canBiet.setForeground(GREY);
        canBiet.setBorder(new EmptyBorder(0, 2, 6, 2));
        root.add(canBiet, BorderLayout.NORTH);

        bmTable.setRowHeight(24);
        bmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bmTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        bmTable.setAutoCreateRowSorter(true);
        int[] w = {50, 240, 240, 70, 55, 300};
        for (int i = 0; i < bmTable.getColumnCount() && i < w.length; i++) {
            bmTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        bmTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    bmSua(false);
                }
            }
        });
        root.add(oLocBang(bmTable, "gõ tên bản đồ, tên boss hay id"),
                BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(bmTable), BorderLayout.CENTER);

        JPanel nut = new JPanel(new GridLayout(0, 4, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm dòng", OK_GREEN, e -> bmSua(true)));
        nut.add(button("Sửa dòng", ACCENT, e -> bmSua(false)));
        nut.add(button("Xoá dòng", WARN_RED, e -> bmXoa()));
        nut.add(button("Tải lại", GREY, e -> napBossMap()));
        root.add(nut, BorderLayout.SOUTH);

        napBossMap();
        return root;
    }

    private void napBossMap() {
        bmModel.setRowCount(0);
        java.util.Map<Integer, String> tenBoss = danhMucBossID();
        for (nro.repository.dao.BossTheoMapDAO.Dong d
                : nro.repository.dao.BossTheoMapDAO.tatCa()) {
            bmModel.addRow(new Object[]{d.id,
                d.mapId + " — " + tenMap(d.mapId),
                d.bossId + " — " + tenBoss.getOrDefault(d.bossId, "(không rõ)"),
                d.moiKhu ? "có" : "", d.bat ? "bật" : "tắt", d.ghiChu});
        }
    }

    /** Tên bản đồ theo id, hoặc dấu hỏi nếu chưa nạp bảng bản đồ. */
    private String tenMap(int mapId) {
        nro.entity.template.MapTemplate[] ds = nro.server.Manager.MAP_TEMPLATES;
        if (ds != null) {
            for (nro.entity.template.MapTemplate m : ds) {
                if (m != null && m.id == mapId) {
                    return m.name;
                }
            }
        }
        return "(không rõ)";
    }

    private void bmSua(boolean them) {
        nro.repository.dao.BossTheoMapDAO.Dong d = null;
        if (!them) {
            int r = bmTable.getSelectedRow();
            if (r < 0) {
                note(WARN_RED, "Chọn một dòng trước.");
                return;
            }
            int id = Integer.parseInt(String.valueOf(bmTable.getValueAt(r, 0)));
            for (nro.repository.dao.BossTheoMapDAO.Dong x
                    : nro.repository.dao.BossTheoMapDAO.tatCa()) {
                if (x.id == id) {
                    d = x;
                    break;
                }
            }
            if (d == null) {
                note(WARN_RED, "Dòng này không còn nữa.");
                return;
            }
        } else {
            d = new nro.repository.dao.BossTheoMapDAO.Dong();
        }

        final int[] mapId = {d.mapId};
        final int[] bossId = {d.bossId};
        JLabel lblMap = new JLabel();
        JLabel lblBoss = new JLabel();
        java.util.Map<Integer, String> tenBoss = danhMucBossID();
        Runnable veMap = () -> lblMap.setText(mapId[0] + " — " + tenMap(mapId[0]));
        Runnable veBoss = () -> lblBoss.setText(bossId[0] + " — "
                + tenBoss.getOrDefault(bossId[0], "(chưa chọn)"));
        veMap.run();
        veBoss.run();

        JCheckBox cbMoiKhu = new JCheckBox("Mỗi khu một con", d.moiKhu);
        JCheckBox cbBat = new JCheckBox("Bật", d.bat);
        JTextField fGhiChu = new JTextField(d.ghiChu, 28);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;

        JPanel pMap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pMap.setOpaque(false);
        pMap.add(button("Chọn…", ACCENT, e -> {
            String csv = nro.ui.MapPicker.chon(form, String.valueOf(mapId[0]));
            if (csv != null) {
                java.util.List<Integer> so = nro.ui.MapPicker.tachSo(csv);
                if (!so.isEmpty()) {
                    // Chi lay ban do dau: mot dong ung voi dung mot ban do.
                    mapId[0] = so.get(0);
                    veMap.run();
                }
            }
        }));
        pMap.add(lblMap);
        bdHang(form, c, y++, "Bản đồ:", pMap, "boss mọc ở bản đồ này");

        JPanel pBoss = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pBoss.setOpaque(false);
        pBoss.add(button("Chọn…", ACCENT, e -> {
            Integer v = chonMotBossID(form);
            if (v != null) {
                bossId[0] = v;
                veBoss.run();
            }
        }));
        pBoss.add(lblBoss);
        bdHang(form, c, y++, "Boss:", pBoss, "");

        bdHang(form, c, y++, "Mỗi khu:", cbMoiKhu,
                "tắt thì cả bản đồ chỉ một con ở khu đầu");
        bdHang(form, c, y++, "Ghi chú:", fGhiChu, "");
        c.gridx = 1;
        c.gridy = y;
        form.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, form,
                them ? "Thêm boss cho bản đồ" : "Sửa dòng " + d.id,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (bossId[0] == 0) {
            note(WARN_RED, "Chưa chọn boss.");
            return;
        }
        d.mapId = mapId[0];
        d.bossId = bossId[0];
        d.moiKhu = cbMoiKhu.isSelected();
        d.bat = cbBat.isSelected();
        d.ghiChu = fGhiChu.getText().trim();

        String loi = nro.repository.dao.BossTheoMapDAO.luu(d);
        if (loi != null) {
            JOptionPane.showMessageDialog(this, loi, "Không lưu được",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        napBossMap();
        note(OK_GREEN, "Đã lưu — khởi động lại máy chủ để có hiệu lực.");
    }

    private void bmXoa() {
        int r = bmTable.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng trước.");
            return;
        }
        int id = Integer.parseInt(String.valueOf(bmTable.getValueAt(r, 0)));
        if (JOptionPane.showConfirmDialog(this,
                "Xoá dòng này?\nBản đồ đó sẽ không mọc con boss này nữa.",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.BossTheoMapDAO.xoa(id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        napBossMap();
        note(OK_GREEN, "Đã xoá — khởi động lại máy chủ để có hiệu lực.");
    }

    /** Chọn đúng một mã boss từ danh mục {@code BossID}. */
    private Integer chonMotBossID(java.awt.Component cha) {
        java.util.Map<Integer, String> tatCa = danhMucBossID();
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"ID", "Tên hằng"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTextField fTim = new JTextField(20);
        final java.util.List<Integer> hien = new java.util.ArrayList<>();

        final Runnable ve = () -> {
            String q = fTim.getText().trim().toLowerCase();
            m.setRowCount(0);
            hien.clear();
            for (java.util.Map.Entry<Integer, String> e : tatCa.entrySet()) {
                if (!q.isEmpty() && !e.getValue().toLowerCase().contains(q)
                        && !String.valueOf(e.getKey()).contains(q)) {
                    continue;
                }
                hien.add(e.getKey());
                m.addRow(new Object[]{e.getKey(), e.getValue()});
            }
        };
        fTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                ve.run();
            }
        });
        ve.run();
        t.getColumnModel().getColumn(0).setPreferredWidth(70);
        t.getColumnModel().getColumn(1).setPreferredWidth(280);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.setOpaque(false);
        top.add(new JLabel("Tìm:"));
        top.add(fTim);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setPreferredSize(new Dimension(460, 420));
        root.add(top, BorderLayout.NORTH);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(cha, root, "Chọn boss",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        int r = t.getSelectedRow();
        if (r < 0) {
            return null;
        }
        int i = t.convertRowIndexToModel(r);
        return i >= 0 && i < hien.size() ? hien.get(i) : null;
    }

    /** Tên nhóm theo id, hoặc dấu gạch nếu nhóm đã bị xoá. */
    private String tenNhom(int nhomId) {
        for (nro.service.boss.NhomBossService.Nhom n
                : nro.service.boss.NhomBossService.gI().tatCa()) {
            if (n.id == nhomId) {
                return n.ten + " (" + moTaThoiGian(n.chuKyGiay) + ")";
            }
        }
        return "—";
    }

    /**
     * Ô "Hồi sinh": nói rõ con này đang theo giờ của <b>nhóm</b> hay giờ riêng.
     *
     * <p>Hai nguồn cùng tồn tại nên chỉ hiện con số thì không đủ: boss thuộc
     * nhóm thì số riêng của mẫu <b>không được dùng</b>, mà nhìn bảng lại tưởng
     * nó đang chạy theo số đó.</p>
     */
    private String moTaHoiSinh(String idBoss, int giayRieng) {
        try {
            nro.service.boss.NhomBossService.Nhom n
                    = nro.service.boss.NhomBossService.gI()
                            .nhomCua(Integer.parseInt(idBoss));
            if (n != null) {
                return "theo nhóm " + n.ten + " — " + moTaThoiGian(n.chuKyGiay);
            }
        } catch (NumberFormatException boQua) {
            // Mau chua co boss nao dung thi khong co id, roi xuong duoi.
        }
        return giayRieng <= 0 ? "—" : moTaThoiGian(giayRieng);
    }

    /** Vị trí trong ô chọn ứng với nhóm mà boss này đang thuộc về. */
    private int viTriNhomCuaBoss(int bossId,
            java.util.List<nro.service.boss.NhomBossService.Nhom> ds) {
        if (bossId == Integer.MIN_VALUE) {
            return 0;
        }
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            if (d.bossId != bossId || d.nhomId <= 0) {
                continue;
            }
            for (int i = 0; i < ds.size(); i++) {
                if (ds.get(i).id == d.nhomId) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    /**
     * Ghi nhóm cho một con boss.
     *
     * <p>Nhóm nằm ở {@code boss_spawn}, còn hộp thoại đang sửa {@code boss_data}
     * — hai bảng khác nhau, nên phải ghi riêng. Boss chưa nằm trong danh sách
     * thì bỏ qua chứ không tạo dòng mới: thêm boss vào game là việc của nút
     * riêng, làm lén ở đây thì người dùng không hiểu boss ở đâu ra.</p>
     */
    private void luuNhomChoBoss(int bossId, int viTri,
            java.util.List<nro.service.boss.NhomBossService.Nhom> ds) {
        if (bossId == Integer.MIN_VALUE) {
            return;
        }
        int nhomId = viTri <= 0 || viTri > ds.size() ? 0 : ds.get(viTri - 1).id;
        for (nro.repository.dao.BossSpawnDAO.Dong d
                : nro.repository.dao.BossSpawnDAO.tatCa()) {
            if (d.bossId == bossId) {
                if (d.nhomId == nhomId) {
                    return;
                }
                d.nhomId = nhomId;
                nro.repository.dao.BossSpawnDAO.luu(d);
                nro.service.boss.NhomBossService.gI().napLai();
                return;
            }
        }
    }

    /**
     * Ô thoại: một dòng chữ tóm tắt kèm nút mở bảng sửa từng câu.
     *
     * <p>Giá trị thật nằm trong mảng một phần tử, <b>không đi qua ô nhập nào
     * cả</b>. Cho nó chạy qua {@code JTextField} là mất hết ký tự xuống dòng —
     * ô một dòng lặng lẽ bỏ chúng, và ba câu thoại bị nối liền thành một.</p>
     */
    private JPanel oThoai(String[] giaTri, JLabel xem, String tieuDe) {
        Runnable veLai = () -> {
            String s = giaTri[0] == null ? "" : giaTri[0];
            if (s.trim().isEmpty()) {
                xem.setText("(chưa có câu nào)");
                xem.setToolTipText(null);
                return;
            }
            String[] cau = s.split("\n", -1);
            xem.setText(cau.length + " câu — " + rutGon(cau[0], 44));
            xem.setToolTipText("<html><body style='width:360px'>"
                    + String.join("<br>", cau).replace("<", "&lt;")
                    + "</body></html>");
        };
        veLai.run();
        xem.setForeground(GREY);

        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(button("Sửa…", ACCENT, e -> {
            String moi = suaThoai(p, tieuDe, giaTri[0]);
            if (moi != null) {
                giaTri[0] = moi;
                veLai.run();
            }
        }), BorderLayout.WEST);
        p.add(xem, BorderLayout.CENTER);
        return p;
    }

    // =====================================================================
    //  Đồ rơi từ quái thường
    // =====================================================================

    /**
     * Màn khai vật phẩm rơi từ quái thường.
     *
     * <p>Trước đây phần này gõ cứng hơn hai trăm dòng trong
     * {@code Mob.getItemMobReward()}. Nay bảng trống nghĩa là <b>quái không rơi
     * món nào</b> — mọi thứ khai ở đây.</p>
     *
     * <p>Vẫn còn trong mã những thứ không phải "một món rơi theo tỉ lệ" mà là
     * cơ chế có điều kiện: điểm Ngũ Hành Sơn, đồ set kích hoạt, capsule theo
     * máy dò, thức ăn khi mặc đủ set Thần Linh, bình hút năng lượng. Ép chúng
     * vào bảng thì mất điều kiện.</p>
     */
    private JComponent buildQuaiDoRoiTab(String maDK, String nhanDK, String moTaDK) {
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel canBiet = new JLabel("<html><body style='width:900px'>"
                + "<b>" + nhanDK + ".</b> " + moTaDK
                + "<br>Tỉ lệ tính bằng <b>phần trăm</b> và nhận số thập phân: "
                + "<code>1</code> là 1% (trung bình 100 con ra một món), "
                + "<code>0.01</code> là một phần vạn."
                + "<br>Có hiệu lực <b>ngay</b>, không cần khởi động lại máy chủ."
                + "</body></html>");
        canBiet.setForeground(GREY);
        canBiet.setBorder(new EmptyBorder(0, 2, 6, 2));
        root.add(canBiet, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"ID", "Ảnh", "Vật phẩm", "Tên", "Số lượng", "Tỉ lệ",
                    "Trung bình", "Bản đồ", "HSD", "Bật", "Ghi chú"}, 0) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 1 ? javax.swing.ImageIcon.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        qdrTheoDieuKien.put(maDK, m);
        qdrBangTheoDieuKien.put(maDK, t);

        t.setRowHeight(32);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setAutoCreateRowSorter(true);
        int[] w = {45, 45, 70, 220, 90, 80, 130, 120, 90, 50, 200};
        for (int i = 0; i < t.getColumnCount() && i < w.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    qdrDieuKienDangXem = maDK;
                    qdrSua(false);
                }
            }
        });
        t.getSelectionModel().addListSelectionListener(
                e -> qdrDieuKienDangXem = maDK);
        root.add(ServerGuiUtils.cuon(t), BorderLayout.CENTER);

        JPanel nut = new JPanel(new GridLayout(0, 5, 6, 6));
        nut.setOpaque(false);
        nut.add(button("Thêm món rơi", OK_GREEN, e -> {
            qdrDieuKienDangXem = maDK;
            qdrSua(true);
        }));
        nut.add(button("Sửa", ACCENT, e -> {
            qdrDieuKienDangXem = maDK;
            qdrSua(false);
        }));
        nut.add(button("Bật / Tắt", ACCENT, e -> {
            qdrDieuKienDangXem = maDK;
            qdrBatTat();
        }));
        nut.add(button("Xoá", WARN_RED, e -> {
            qdrDieuKienDangXem = maDK;
            qdrXoa();
        }));
        nut.add(button("Tải lại", GREY, e -> napQuaiDoRoi()));
        root.add(nut, BorderLayout.SOUTH);

        return root;
    }

    /** Đổ lại mọi bảng con, mỗi bảng chỉ những món mang đúng điều kiện của nó. */
    private void napQuaiDoRoi() {
        for (DefaultTableModel m : qdrTheoDieuKien.values()) {
            m.setRowCount(0);
        }
        for (nro.repository.dao.QuaiDoRoiDAO.Dong d
                : nro.repository.dao.QuaiDoRoiDAO.tatCa()) {
            String dk = d.dieuKien == null ? "" : d.dieuKien.trim();
            DefaultTableModel m = qdrTheoDieuKien.get(dk);
            if (m == null) {
                // Ma la — khai tay trong CSDL hoac ma nguon vua bo mot dieu kien.
                // Do vao bang dau de con nhin thay ma sua, khong giau di.
                m = qdrTheoDieuKien.get("");
                if (m == null) {
                    continue;
                }
            }
            nro.entity.template.ItemTemplate t =
                    PlayerManagerPanel.templateOf(String.valueOf(d.itemId));
            // Dong "do set kich hoat" khong gan voi mot vat pham cu the: may
            // chu tu chon do vai tho dung he luc roi. Nen cot Ten hien TEN SET,
            // con cot Vat pham de dau gach — hien so id cua mon chua chon o do
            // se lam nguoi doc tuong dong nay roi dung mon ay.
            String tenHien;
            if (d.skh) {
                tenHien = "Đồ set kích hoạt — "
                        + (d.skhSet == null || d.skhSet.trim().isEmpty()
                                ? "set ngẫu nhiên đúng hệ"
                                : nro.repository.dao.SetBonusDAO.tenSet(d.skhSet))
                        + (d.saoMax > 0
                                ? "  (" + Math.max(1, d.saoMin) + "–" + d.saoMax + " sao)"
                                : "");
            } else {
                tenHien = t != null ? t.name : "(không rõ)";
            }
            m.addRow(new Object[]{d.id,
                d.skh ? null : (t != null ? PlayerManagerPanel.iconOf(t.iconID) : null),
                d.skh ? "—" : String.valueOf(d.itemId), tenHien,
                d.moTaSoLuong(), gonSo(d.tiLe) + " %",
                moTaTrungBinh(d.tiLe), d.moTaMap(),
                d.hsdMin > 0 && d.hsdMax > 0
                        ? (d.hsdMin == d.hsdMax ? d.hsdMin + " ngày"
                                : d.hsdMin + "–" + d.hsdMax + " ngày")
                        : "vĩnh viễn",
                d.bat ? "bật" : "tắt", d.ghiChu});
        }
    }

    /** Câu "trung bình bao nhiêu con quái ra một món", để tỉ lệ dễ hình dung. */
    private static String moTaTrungBinh(double phanTram) {
        if (phanTram <= 0) {
            return "không bao giờ";
        }
        double con = 100.0 / phanTram;
        if (con < 1.5) {
            return "gần như mọi con";
        }
        return "1 trên " + PlayerManagerPanel.fmt(Math.round(con)) + " con";
    }

    private nro.repository.dao.QuaiDoRoiDAO.Dong qdrDangChon() {
        JTable t = qdrBangTheoDieuKien.get(qdrDieuKienDangXem);
        if (t == null) {
            note(WARN_RED, "Chưa mở bảng nào.");
            return null;
        }
        int r = t.getSelectedRow();
        if (r < 0) {
            note(WARN_RED, "Chọn một dòng trong bảng trước.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(t.getValueAt(r, 0)));
        for (nro.repository.dao.QuaiDoRoiDAO.Dong d
                : nro.repository.dao.QuaiDoRoiDAO.tatCa()) {
            if (d.id == id) {
                return d;
            }
        }
        note(WARN_RED, "Dòng này không còn nữa.");
        return null;
    }

    /**
     * Hộp chọn một set kích hoạt, danh sách lấy thẳng từ tab "Set kích hoạt".
     *
     * <p>Chỉ liệt kê set đang <b>bật</b>: chọn một set đã tắt thì món rơi ra
     * không kích hoạt được gì, mà trên bảng nhìn vẫn thấy có set — rất khó đoán
     * ra vì sao.</p>
     *
     * <p>Mục đầu là "bốc ngẫu nhiên": máy chủ tự chọn trong các set đúng hệ của
     * người chơi vừa giết quái. Chọn đích danh một set thì set đó chỉ rơi cho
     * người <b>cùng hành tinh</b> với nó — nên tên hành tinh hiện luôn cạnh tên
     * set để khỏi chọn nhầm.</p>
     *
     * @param dangCo mã set đang chọn, rỗng nghĩa là đang bốc ngẫu nhiên
     * @return mã set mới, chuỗi rỗng nếu chọn bốc ngẫu nhiên, {@code null} nếu huỷ
     */
    private String chonSetKichHoat(String dangCo) {
        java.util.List<String> ma = new java.util.ArrayList<>();
        java.util.List<String> nhan = new java.util.ArrayList<>();
        ma.add("");
        nhan.add("(bốc ngẫu nhiên — theo hệ của người chơi)");
        for (java.util.Map.Entry<String, nro.repository.dao.SetBonusDAO.DinhNghia> e
                : nro.repository.dao.SetBonusDAO.dinhNghia().entrySet()) {
            nro.repository.dao.SetBonusDAO.DinhNghia dn = e.getValue();
            if (dn == null || !dn.active) {
                continue;
            }
            ma.add(e.getKey());
            nhan.add(nro.repository.dao.SetBonusDAO.tenSet(e.getKey())
                    + "   —   " + nro.repository.dao.SetBonusDAO.hanhTinh(e.getKey()));
        }
        if (ma.size() == 1) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có set kích hoạt nào đang bật.\n"
                    + "Sang tab \"Set kích hoạt\" thêm set trước đã.",
                    "Chưa có set", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int dangO = ma.indexOf(dangCo == null ? "" : dangCo.trim());
        if (dangO < 0) {
            dangO = 0;
        }
        JComboBox<String> cb = new JComboBox<>(nhan.toArray(new String[0]));
        cb.setSelectedIndex(dangO);
        if (JOptionPane.showConfirmDialog(this, cb, "Chọn set kích hoạt",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        int i = cb.getSelectedIndex();
        return i < 0 ? null : ma.get(i);
    }
    private void qdrSua(boolean them) {
        final nro.repository.dao.QuaiDoRoiDAO.Dong d;
        if (them) {
            d = new nro.repository.dao.QuaiDoRoiDAO.Dong();
            d.itemId = -1;
        } else {
            d = qdrDangChon();
            if (d == null) {
                return;
            }
        }

        // Khi THEM moi thi cho chon nhieu mon mot luot: khai mot loat da nang
        // cap cung tinh chat thi bam tung mon rat lau. Luc SUA thi chi mot mon,
        // vi dang sua dung dong do.
        final java.util.List<Integer> nhieuItem = new java.util.ArrayList<>();
        final int[] itemId = {d.itemId};
        final JLabel lblItem = new JLabel(moTaVatPham(itemId[0]));
        JButton btnItem = button(them ? "Chọn vật phẩm…" : "Đổi vật phẩm…",
                ACCENT, e -> {
            if (them) {
                java.util.List<Integer> ds =
                        nro.ui.OptionPicker.chonNhieuVatPham(this,
                                nhieuItem.isEmpty() ? -1 : nhieuItem.get(0));
                if (ds == null || ds.isEmpty()) {
                    return;
                }
                nhieuItem.clear();
                nhieuItem.addAll(ds);
                itemId[0] = ds.get(0);
                lblItem.setText(ds.size() == 1 ? moTaVatPham(ds.get(0))
                        : ds.size() + " món — " + moTaVatPham(ds.get(0)) + "…");
                return;
            }
            int moi = nro.ui.OptionPicker.chonVatPham(this, itemId[0]);
            if (moi >= 0) {
                itemId[0] = moi;
                lblItem.setText(moTaVatPham(moi));
            }
        });

        JTextField fMin = new JTextField(String.valueOf(d.soLuongMin), 5);
        JTextField fMax = new JTextField(String.valueOf(d.soLuongMax), 5);
        JTextField fTiLe = new JTextField(gonSo(d.tiLe), 8);
        JTextField fHsdMin = new JTextField(String.valueOf(d.hsdMin), 5);
        JTextField fHsdMax = new JTextField(String.valueOf(d.hsdMax), 5);
        JTextField fHsdVV = new JTextField(String.valueOf(d.hsdVinhVien), 5);
        JTextField fGhiChu = new JTextField(d.ghiChu, 26);
        JCheckBox cbBat = new JCheckBox("Bật", d.bat);

        final JLabel lblTrungBinh = new JLabel();
        Runnable veTb = () -> {
            try {
                lblTrungBinh.setText("  " + moTaTrungBinh(
                        Double.parseDouble(fTiLe.getText().trim().replace(",", "."))));
            } catch (NumberFormatException ex) {
                lblTrungBinh.setText("  (gõ một số)");
            }
        };
        veTb.run();
        lblTrungBinh.setForeground(GREY);
        fTiLe.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                veTb.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                veTb.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                veTb.run();
            }
        });

        final int[] kieuMap = {d.kieuMap};
        final String[] dsMap = {d.dsMap == null ? "" : d.dsMap};
        final JLabel lblMap = new JLabel();
        Runnable veMap = () -> {
            nro.repository.dao.QuaiDoRoiDAO.Dong xem
                    = new nro.repository.dao.QuaiDoRoiDAO.Dong();
            xem.kieuMap = kieuMap[0];
            xem.dsMap = dsMap[0];
            lblMap.setText(xem.moTaMap());
        };
        veMap.run();
        JButton btnMap = button("Chọn phạm vi…", ACCENT, e -> {
            String kq = chonPhamViMap(this, kieuMap[0], dsMap[0]);
            if (kq != null) {
                int vach = kq.indexOf('|');
                kieuMap[0] = Integer.parseInt(kq.substring(0, vach));
                dsMap[0] = kq.substring(vach + 1);
                veMap.run();
            }
        });

        final String[] chiSo = {d.chiSo == null ? "" : d.chiSo};
        final JLabel lblChiSo = new JLabel();
        Runnable veCs = () -> lblChiSo.setText(
                chiSo[0].trim().isEmpty() ? "(không có)" : chiSo[0]);
        veCs.run();
        lblChiSo.setForeground(GREY);
        JButton btnChiSo = button("Sửa…", ACCENT, e -> {
            String moi = suaChiSoNgauNhien(this, chiSo[0]);
            if (moi != null) {
                chiSo[0] = moi;
                veCs.run();
            }
        });

        // ---- Tuy chon "do set kich hoat" ----
        // Bat len thi dong nay khong roi vat pham da chon nua: may chu tu chon
        // do vai tho DUNG HE nguoi choi, lay chi so mac dinh cua chinh mon do,
        // roi gan set va sao. Nen o chon vat pham va o chi so ngau nhien tat di
        // cho khoi hieu nham la chung con tac dung.
        final JCheckBox cbSkh = new JCheckBox("Món này là đồ set kích hoạt", d.skh);
        final String[] skhSet = {d.skhSet == null ? "" : d.skhSet};
        final JLabel lblSet = new JLabel();
        Runnable veSet = () -> lblSet.setText(skhSet[0].trim().isEmpty()
                ? "(bốc ngẫu nhiên trong các set đúng hệ người chơi)"
                : nro.repository.dao.SetBonusDAO.tenSet(skhSet[0]) + "  —  "
                        + nro.repository.dao.SetBonusDAO.hanhTinh(skhSet[0]));
        veSet.run();
        lblSet.setForeground(GREY);
        JButton btnSet = button("Chọn set…", ACCENT, e -> {
            String moi = chonSetKichHoat(skhSet[0]);
            if (moi != null) {
                skhSet[0] = moi;
                veSet.run();
            }
        });
        final JTextField fSaoMin = new JTextField(String.valueOf(d.saoMin), 4);
        final JTextField fSaoMax = new JTextField(String.valueOf(d.saoMax), 4);

        Runnable batTatSkh = () -> {
            boolean on = cbSkh.isSelected();
            btnSet.setEnabled(on);
            lblSet.setEnabled(on);
            fSaoMin.setEnabled(on);
            fSaoMax.setEnabled(on);
            btnItem.setEnabled(!on);
            lblItem.setEnabled(!on);
            btnChiSo.setEnabled(!on);
            lblChiSo.setEnabled(!on);
        };
        batTatSkh.run();
        cbSkh.addActionListener(e -> batTatSkh.run());
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;

        JPanel hangItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangItem.setOpaque(false);
        hangItem.add(btnItem);
        hangItem.add(lblItem);
        bdHang(p, c, y++, "Vật phẩm:", hangItem, "");

        JPanel hangSl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangSl.setOpaque(false);
        hangSl.add(fMin);
        hangSl.add(new JLabel(" đến "));
        hangSl.add(fMax);
        bdHang(p, c, y++, "Số lượng:", hangSl, "bốc đều trong khoảng");

        JPanel hangTl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangTl.setOpaque(false);
        hangTl.add(fTiLe);
        hangTl.add(new JLabel(" %"));
        hangTl.add(lblTrungBinh);
        bdHang(p, c, y++, "Tỉ lệ rơi:", hangTl, "nhận số thập phân, ví dụ 0.01");

        JPanel hangMap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangMap.setOpaque(false);
        hangMap.add(btnMap);
        hangMap.add(lblMap);
        bdHang(p, c, y++, "Bản đồ:", hangMap, "");

        JPanel hangHsd = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangHsd.setOpaque(false);
        hangHsd.add(fHsdMin);
        hangHsd.add(new JLabel(" đến "));
        hangHsd.add(fHsdMax);
        hangHsd.add(new JLabel(" ngày, vĩnh viễn "));
        hangHsd.add(fHsdVV);
        hangHsd.add(new JLabel(" %"));
        bdHang(p, c, y++, "Hạn dùng:", hangHsd, "để 0 là món vĩnh viễn");

        JPanel hangCs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangCs.setOpaque(false);
        hangCs.add(btnChiSo);
        hangCs.add(lblChiSo);
        bdHang(p, c, y++, "Chỉ số ngẫu nhiên:", hangCs, "");

        c.gridx = 1;
        c.gridy = y++;
        p.add(cbSkh, c);

        JPanel hangSet = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangSet.setOpaque(false);
        hangSet.add(btnSet);
        hangSet.add(lblSet);
        bdHang(p, c, y++, "Set kích hoạt:", hangSet,
                "danh sách lấy từ tab Set kích hoạt");

        JPanel hangSao = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hangSao.setOpaque(false);
        hangSao.add(fSaoMin);
        hangSao.add(new JLabel(" đến "));
        hangSao.add(fSaoMax);
        hangSao.add(new JLabel(" sao"));
        bdHang(p, c, y++, "Sao pha lê:", hangSao, "để tối đa = 0 là không gắn sao");
        bdHang(p, c, y++, "Ghi chú:", fGhiChu, "");
        c.gridx = 1;
        c.gridy = y;
        p.add(cbBat, c);

        if (JOptionPane.showConfirmDialog(this, p,
                them ? "Thêm món rơi từ quái" : "Sửa món rơi " + d.id,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            d.itemId = itemId[0];
            d.soLuongMin = docSoNguyen(fMin.getText(), "số lượng tối thiểu");
            d.soLuongMax = docSoNguyen(fMax.getText(), "số lượng tối đa");
            d.tiLe = Double.parseDouble(fTiLe.getText().trim().replace(",", "."));
            d.hsdMin = docSoNguyen(fHsdMin.getText(), "HSD tối thiểu");
            d.hsdMax = docSoNguyen(fHsdMax.getText(), "HSD tối đa");
            d.hsdVinhVien = docSoNguyen(fHsdVV.getText(), "tỉ lệ vĩnh viễn");
            d.saoMin = docSoNguyen(fSaoMin.getText(), "số sao tối thiểu");
            d.saoMax = docSoNguyen(fSaoMax.getText(), "số sao tối đa");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Nhập chưa đúng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        d.kieuMap = kieuMap[0];
        d.dsMap = dsMap[0];
        d.chiSo = chiSo[0];
        d.bat = cbBat.isSelected();
        d.ghiChu = fGhiChu.getText().trim();
        d.skh = cbSkh.isSelected();
        d.skhSet = skhSet[0];
        // Mon them tu tab nao thi mang dieu kien cua tab do.
        if (them) {
            d.dieuKien = qdrDieuKienDangXem;
        }

        // Chon nhieu mon thi tao mot dong cho MOI mon, dung chung moi thiet lap
        // vua khai. Ghi tung dong roi bao mot lan, khong bat bam OK nhieu lan.
        java.util.List<Integer> canLuu = new java.util.ArrayList<>();
        if (them && !nhieuItem.isEmpty()) {
            canLuu.addAll(nhieuItem);
        } else {
            canLuu.add(d.itemId);
        }

        int xong = 0;
        java.util.List<String> loi = new java.util.ArrayList<>();
        for (int idMon : canLuu) {
            nro.repository.dao.QuaiDoRoiDAO.Dong ghi = d;
            if (canLuu.size() > 1) {
                // Moi mon mot doi tuong rieng: dung chung mot doi tuong thi
                // lan luu thu hai mang theo id vua sinh cua lan dau va bien
                // thanh SUA de len dong truoc.
                ghi = new nro.repository.dao.QuaiDoRoiDAO.Dong();
                ghi.soLuongMin = d.soLuongMin;
                ghi.soLuongMax = d.soLuongMax;
                ghi.tiLe = d.tiLe;
                ghi.kieuMap = d.kieuMap;
                ghi.dsMap = d.dsMap;
                ghi.hsdMin = d.hsdMin;
                ghi.hsdMax = d.hsdMax;
                ghi.hsdVinhVien = d.hsdVinhVien;
                ghi.chiSo = d.chiSo;
                ghi.bat = d.bat;
                ghi.ghiChu = d.ghiChu;
                ghi.dieuKien = d.dieuKien;
            }
            ghi.itemId = idMon;
            String e = nro.repository.dao.QuaiDoRoiDAO.luu(ghi);
            if (e == null) {
                xong++;
            } else {
                loi.add(moTaVatPham(idMon) + ": " + e);
            }
        }
        if (!loi.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Lưu được " + xong + " món.\nKhông lưu được:\n"
                    + String.join("\n", loi),
                    "Có dòng lỗi", JOptionPane.WARNING_MESSAGE);
        }
        napQuaiDoRoi();
        note(OK_GREEN, xong > 1 ? "Đã thêm " + xong + " món — có hiệu lực ngay."
                : "Đã lưu — có hiệu lực ngay.");
    }

    private void qdrBatTat() {
        nro.repository.dao.QuaiDoRoiDAO.Dong d = qdrDangChon();
        if (d == null) {
            return;
        }
        d.bat = !d.bat;
        String loi = nro.repository.dao.QuaiDoRoiDAO.luu(d);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        napQuaiDoRoi();
        note(OK_GREEN, d.bat ? "Đã bật." : "Đã tắt.");
    }

    private void qdrXoa() {
        nro.repository.dao.QuaiDoRoiDAO.Dong d = qdrDangChon();
        if (d == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xoá món này khỏi danh sách rơi từ quái?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = nro.repository.dao.QuaiDoRoiDAO.xoa(d.id);
        if (loi != null) {
            note(WARN_RED, loi);
            return;
        }
        napQuaiDoRoi();
        note(OK_GREEN, "Đã xoá.");
    }

    /**
     * Gom đồ rơi từ quái thành nhiều tab con, mỗi điều kiện một tab.
     *
     * <p>Đổ chung một bảng thì không nhìn ra món nào cần điều kiện gì — mà
     * điều kiện lại là thứ quyết định món đó có bao giờ rơi hay không. Tách
     * tab thì mỗi tab tự nói rõ luật của mình ngay trên đầu.</p>
     */
    private JComponent buildDoRoiTongHop() {
        JTabbedPane trong = new JTabbedPane();
        String[][] ds = nro.repository.dao.QuaiDoRoiDAO.DieuKien.DANH_SACH;
        for (int i = 0; i < ds.length; i++) {
            trong.addTab((i + 1) + ". " + ds[i][1],
                    buildQuaiDoRoiTab(ds[i][0], ds[i][1], ds[i][2]));
        }
        // Panel duoc dung TRUOC khi may chu khoi dong nen bang con rong luc do.
        trong.addChangeListener(e -> napQuaiDoRoi());
        return trong;
    }

    /** Bảng và nút của mỗi tab con. Tất cả dùng chung một mô hình bảng. */
    private final java.util.Map<String, DefaultTableModel> qdrTheoDieuKien
            = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, JTable> qdrBangTheoDieuKien
            = new java.util.LinkedHashMap<>();
    private String qdrDieuKienDangXem = "";

}
