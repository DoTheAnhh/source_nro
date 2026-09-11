package nro.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import nro.core.util.Util;
import nro.entity.player.NPoint;
import nro.entity.player.NPoint.DieuKienTn;
import nro.entity.player.Player;
import nro.repository.dao.ConfigDAO;
import nro.repository.dao.HeSoTnsmDAO;
import nro.server.Client;
import nro.server.Manager;

/**
 * Tab "Tỉ lệ → 2. Theo bản đồ": hệ số tiềm năng theo nhóm bản đồ, và con số
 * ước tính một đòn.
 *
 * <h2>Ước tính phải bằng đúng số trong game</h2>
 *
 * <p>Panel không tự viết lại công thức. Ba cột ước tính gọi thẳng
 * {@link NPoint#tinhTiemNang} và {@link NPoint#giamTheoSucManh} — đúng hai hàm
 * máy chủ dùng khi một con quái bị đánh — theo đúng thứ tự của {@code Mob} và
 * {@code Service.addSMTN}. Cùng một đoạn mã thì hai con số không thể lệch.</p>
 *
 * <p>Để thấy tận mắt: "Kiểm tra với người chơi" lấy bộ điều kiện <b>thật</b>
 * của một người đang online cùng cú đánh gần nhất của họ, rồi đặt con số game
 * trả ra cạnh con số bảng tính ra.</p>
 *
 * <h2>Điều kiện: chung, hoặc riêng từng dòng</h2>
 *
 * <p>Điều kiện là của <b>người chơi</b> (bùa, thẻ, cờ…), không phải của bản đồ,
 * nên mặc định mọi dòng dùng chung một bộ. Muốn so thử một dòng với bộ khác thì
 * bấm đúp ô "Điều kiện" của dòng đó. Điều kiện chỉ để tính thử, không đổi gì
 * trong game.</p>
 */
final class HeSoTnsmTab extends JPanel {

    private static final Color XANH = new Color(0, 120, 215);
    private static final Color XANH_LA = new Color(40, 160, 70);
    private static final Color DO = new Color(200, 60, 60);
    private static final Color XAM = new Color(120, 120, 120);
    private static final Color NEN_UOC = new Color(245, 246, 248);

    private static final int C_ID = 0;
    private static final int C_TEN = 1;
    private static final int C_MAP = 2;
    private static final int C_SP = 3;
    private static final int C_DE_DE = 4;
    private static final int C_DE_SP = 5;
    private static final int C_DK = 6;
    private static final int C_UT_SP = 7;
    private static final int C_UT_DE = 8;
    private static final int C_UT_DESP = 9;
    private static final int C_CHIDE = 10;
    private static final int C_BAT = 11;
    private static final int C_GC = 12;

    private static final String[] TEN_COT = {"#", "Nhóm bản đồ", "Các bản đồ",
        "Hệ số", "Đệ nhận ×", "Sư phụ nhận ×", "Điều kiện",
        "Sư phụ tự đánh", "Đệ tự đánh", "Đệ đánh → SP", "Chỉ đệ", "Bật", "Ghi chú"};

    private static final String[] CHU_GIAI = {
        "Số thứ tự, máy tự đặt",
        "Tên nhóm, đặt sao cho dễ nhận ra",
        "Id bản đồ trong nhóm — id đơn hoặc khoảng, ví dụ 68-72,102,103",
        "Số nhân khi SƯ PHỤ (hoặc người chơi thường) TỰ ĐÁNH. 1 là như bản đồ thường",
        "Đệ tử đánh: số nhân cho phần ĐỆ TỬ nhận. Để trống thì dùng chung số của sư phụ",
        "Đệ tử đánh: số nhân cho phần SƯ PHỤ nhận từ đệ, sau khi đệ đã nhận đủ. Để trống là 1",
        "Điều kiện dùng để tính thử. \"chung\" = bộ điều kiện chung; bấm đúp để đặt riêng cho dòng này",
        "Ước tính: sư phụ tự đánh một đòn thì nhận bao nhiêu — bằng số hiện trong game",
        "Ước tính: đệ tử tự đánh một đòn thì đệ nhận bao nhiêu",
        "Ước tính: đệ tử đánh một đòn thì sư phụ nhận bao nhiêu",
        "Bật thì người chơi tự đánh trong nhóm này KHÔNG được tiềm năng, chỉ đệ tử mới có",
        "Tắt thì cả nhóm coi như không có, các bản đồ trong đó tính như bản đồ thường",
        "Ghi chú cho chính bạn, máy chủ không đọc tới"
    };

    /** Nhóm cột trên dải phía trên tiêu đề: {cột đầu, cột cuối, tên nhóm}. */
    private static final Object[][] NHOM_COT = {
        {C_SP, C_SP, "Sư phụ"},
        {C_DE_DE, C_DE_SP, "Đệ tử đánh"},
        {C_UT_SP, C_UT_DESP, "Ước tính 1 đòn — bằng số nhận trong game"},
    };

    private final DefaultTableModel model = new DefaultTableModel(TEN_COT, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c != C_ID && c != C_DK && c != C_UT_SP && c != C_UT_DE && c != C_UT_DESP;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return (c == C_CHIDE || c == C_BAT) ? Boolean.class : String.class;
        }
    };

    private final DaiNhomCot dai = new DaiNhomCot();

    private final JTable bang = new JTable(model) {
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                @Override
                public String getToolTipText(MouseEvent e) {
                    int i = columnModel.getColumnIndexAtX(e.getPoint().x);
                    if (i < 0) {
                        return null;
                    }
                    int c = columnModel.getColumn(i).getModelIndex();
                    return c >= 0 && c < CHU_GIAI.length ? CHU_GIAI[c] : null;
                }
            };
        }

        /**
         * Đặt dải nhóm cột ngay trên tiêu đề, trong cùng khung cuộn ngang.
         *
         * <p>Swing không có tiêu đề hai tầng. Dải này nằm trong vùng tiêu đề
         * của khung cuộn nên cuộn ngang theo bảng, và vẽ lại mỗi khi kéo rộng
         * hẹp một cột.</p>
         */
        @Override
        protected void configureEnclosingScrollPane() {
            super.configureEnclosingScrollPane();
            Container p = getParent();
            if (p instanceof JViewport && p.getParent() instanceof JScrollPane) {
                JPanel dau = new JPanel(new BorderLayout());
                dau.add(dai, BorderLayout.NORTH);
                dau.add(getTableHeader(), BorderLayout.CENTER);
                ((JScrollPane) p.getParent()).setColumnHeaderView(dau);
            }
        }
    };

    private final JTextField oGoc = new JTextField("1000", 10);
    private final BangDieuKien chung = new BangDieuKien();
    private final Map<Integer, BangDieuKien> rieng = new HashMap<>();
    private final JLabel tomTatChung = new JLabel();
    private final JComboBox<NguoiChoiMuc> hopNguoiChoi = new JComboBox<>();
    private final JLabel ketQuaKiemTra = new JLabel(" ");
    private final JLabel trangThai = new JLabel(" ");
    private final List<Integer> idDong = new ArrayList<>();
    private boolean dangTuSua;

    HeSoTnsmTab() {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        chung.macDinhMayChu();

        // ------------------------------------------------ phan tren
        JPanel tren = new JPanel();
        tren.setLayout(new BoxLayout(tren, BoxLayout.Y_AXIS));
        tren.setOpaque(false);

        tren.add(canTrai(new JLabel("<html><span style='color:#666'>Hệ số tiềm năng theo "
                + "<b>nhóm bản đồ</b>. Rê chuột lên tiêu đề cột để xem giải thích. Cột "
                + "<b>Ước tính</b> chạy đúng công thức máy chủ — dùng <b>Kiểm tra với người "
                + "chơi</b> để so với số thật trong game.</span></html>")));
        tren.add(Box.createVerticalStrut(6));

        JPanel dongGoc = dongTrai();
        dongGoc.add(new JLabel("Giá trị gốc 1 đòn:"));
        oGoc.setHorizontalAlignment(JTextField.RIGHT);
        dongGoc.add(oGoc);
        dongGoc.add(ghiChuXam("trước mọi hệ số. Muốn đúng một cú đánh thật thì lấy từ người chơi."));
        tren.add(dongGoc);

        JPanel dongDk = dongTrai();
        dongDk.add(new JLabel("Điều kiện chung:"));
        tomTatChung.setForeground(new Color(40, 60, 90));
        dongDk.add(tomTatChung);
        dongDk.add(nut("Sửa điều kiện chung", XANH, this::suaDieuKienChung));
        tren.add(dongDk);

        JPanel dongKt = dongTrai();
        dongKt.add(new JLabel("Kiểm tra với người chơi:"));
        hopNguoiChoi.setPrototypeDisplayValue(new NguoiChoiMuc(null));
        dongKt.add(hopNguoiChoi);
        dongKt.add(nut("Làm mới", XAM, this::napNguoiChoi));
        dongKt.add(nut("Lấy điều kiện & so", XANH_LA, this::kiemTraNguoiChoi));
        tren.add(dongKt);

        ketQuaKiemTra.setBorder(new EmptyBorder(2, 8, 4, 0));
        tren.add(canTrai(ketQuaKiemTra));
        add(tren, BorderLayout.NORTH);

        // ------------------------------------------------ bang
        caiDatBang();
        add(ServerGuiUtils.cuon(bang), BorderLayout.CENTER);

        // ------------------------------------------------ phan duoi
        JPanel duoi = new JPanel(new BorderLayout());
        duoi.setOpaque(false);
        JPanel nutDuoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        nutDuoi.setOpaque(false);
        nutDuoi.add(nut("Lưu bảng", XANH_LA, this::luuBang));
        nutDuoi.add(nut("Tải lại", XAM, this::napBang));
        nutDuoi.add(nut("Thêm nhóm", XANH, this::themNhom));
        nutDuoi.add(nut("Về mặc định", new Color(120, 90, 160), this::veMacDinh));
        duoi.add(nutDuoi, BorderLayout.WEST);
        trangThai.setBorder(new EmptyBorder(0, 10, 0, 0));
        duoi.add(trangThai, BorderLayout.CENTER);
        add(duoi, BorderLayout.SOUTH);

        oGoc.getDocument().addDocumentListener(new KhiGo(this::capNhatUocTinhSau));
        chung.khiDoi.add(this::capNhatUocTinhSau);
        napNguoiChoi();
        napBang();
    }

    /** Giá trị gốc đang gõ — tab "Càng mạnh càng giảm" xem trước bằng số này. */
    double giaTriGoc() {
        try {
            double v = Double.parseDouble(oGoc.getText().trim().replace(".", "").replace(',', '.'));
            return v < 0 ? 0 : v;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // =====================================================================
    //  Bảng
    // =====================================================================

    private void caiDatBang() {
        bang.setRowHeight(24);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setReorderingAllowed(false);
        int[] w = {30, 150, 170, 60, 80, 100, 200, 115, 100, 110, 55, 40, 230};
        for (int i = 0; i < w.length && i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        DefaultTableCellRenderer giua = new DefaultTableCellRenderer();
        giua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : new int[]{C_SP, C_DE_DE, C_DE_SP}) {
            bang.getColumnModel().getColumn(c).setCellRenderer(giua);
        }
        for (int c : new int[]{C_UT_SP, C_UT_DE, C_UT_DESP}) {
            bang.getColumnModel().getColumn(c).setCellRenderer(new OUocTinh());
        }
        bang.getColumnModel().getColumn(C_DK).setCellRenderer(new ODieuKien());

        bang.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                veLaiDai();
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                veLaiDai();
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                veLaiDai();
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                veLaiDai();
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                int r = bang.rowAtPoint(e.getPoint());
                int c = bang.columnAtPoint(e.getPoint());
                if (r >= 0 && c >= 0 && bang.convertColumnIndexToModel(c) == C_DK) {
                    suaDieuKienDong(r);
                }
            }
        });

        model.addTableModelListener(e -> {
            if (dangTuSua || e.getType() != TableModelEvent.UPDATE) {
                return;
            }
            int c = e.getColumn();
            if (c == C_DK || c == C_UT_SP || c == C_UT_DE || c == C_UT_DESP) {
                return;
            }
            capNhatUocTinhSau();
        });
    }

    private void veLaiDai() {
        dai.revalidate();
        dai.repaint();
    }

    private void napBang() {
        dungSua();
        dangTuSua = true;
        try {
            model.setRowCount(0);
            idDong.clear();
            for (HeSoTnsmDAO.Dong d : HeSoTnsmDAO.danhSach()) {
                idDong.add(d.id);
                model.addRow(new Object[]{String.valueOf(d.id), d.ten,
                    d.dsMap == null ? "" : d.dsMap,
                    soGon(d.heSo),
                    d.heSoDeTu <= 0 ? "" : soGon(d.heSoDeTu),
                    d.heSoSuPhu <= 0 ? "" : soGon(d.heSoSuPhu),
                    "", "", "", "",
                    d.chiDeTu, d.bat, d.ghiChu == null ? "" : d.ghiChu});
            }
        } finally {
            dangTuSua = false;
        }
        capNhatUocTinh();
    }

    /** Một dòng dựng từ chính các ô đang gõ — xem trước được cả khi chưa lưu. */
    private HeSoTnsmDAO.Dong dongTuO(int r) {
        HeSoTnsmDAO.Dong d = new HeSoTnsmDAO.Dong();
        try {
            d.heSo = soThuc(o(r, C_SP), 1);
            d.heSoDeTu = soThuc(o(r, C_DE_DE), 0);
            d.heSoSuPhu = soThuc(o(r, C_DE_SP), 0);
        } catch (NumberFormatException ex) {
            return null;
        }
        d.chiDeTu = Boolean.TRUE.equals(model.getValueAt(r, C_CHIDE));
        Object bat = model.getValueAt(r, C_BAT);
        d.bat = !(bat instanceof Boolean) || (Boolean) bat;
        return d;
    }

    private void capNhatUocTinhSau() {
        SwingUtilities.invokeLater(this::capNhatUocTinh);
    }

    private void capNhatUocTinh() {
        long goc = Math.max(1L, Math.round(giaTriGoc()));
        dangTuSua = true;
        try {
            for (int r = 0; r < model.getRowCount(); r++) {
                int id = r < idDong.size() ? idDong.get(r) : -1;
                BangDieuKien bdk = rieng.getOrDefault(id, chung);
                model.setValueAt(rieng.containsKey(id) ? "riêng: " + bdk.tomTat() : "chung", r, C_DK);
                HeSoTnsmDAO.Dong d = dongTuO(r);
                if (d == null) {
                    model.setValueAt("?", r, C_UT_SP);
                    model.setValueAt("?", r, C_UT_DE);
                    model.setValueAt("?", r, C_UT_DESP);
                    continue;
                }
                long[] u = uocTinh(d, goc, bdk.dkSuPhu(), bdk.dkDeTu());
                model.setValueAt(d.bat && d.chiDeTu ? "khoá" : fmt(u[0]), r, C_UT_SP);
                model.setValueAt(fmt(u[1]), r, C_UT_DE);
                model.setValueAt(fmt(u[2]), r, C_UT_DESP);
            }
        } finally {
            dangTuSua = false;
        }
        tomTatChung.setText(chung.tomTat());
    }

    // =====================================================================
    //  Ước tính — đúng đường đi của máy chủ
    // =====================================================================

    /**
     * Một con quái bị đánh: đúng thứ tự trong {@code Mob.getTiemNangForPlayer}.
     *
     * <p>Hệ số bản đồ ≤ 0 thì trả 0 như máy chủ; nhân ra 0 thì làm tròn lên 1;
     * rồi mọi điều kiện của người đánh, rồi hệ số chung.</p>
     */
    private static long quaMob(long goc, double heSo, DieuKienTn dk) {
        if (heSo <= 0) {
            return 0;
        }
        long tn = Math.max(1L, goc);
        if (heSo != 1) {
            tn = Math.round(tn * heSo);
            if (tn <= 0) {
                tn = 1;
            }
        }
        tn = Util.CrisGH(NPoint.tinhTiemNang(tn, dk));
        return ConfigDAO.nhanTiLe(ConfigDAO.TL_EXP, tn);
    }

    /**
     * Ba con số của một dòng: sư phụ tự đánh, đệ tự đánh, đệ đánh sư phụ nhận.
     *
     * <p>Hệ số chọn đúng như {@code HeSoTnsmDAO.heSo} và {@code heSoSuPhu}: dòng
     * tắt là bản đồ thường; "chỉ đệ" khoá người tự đánh; ô đệ để trống dùng số
     * của sư phụ. Phần sư phụ nhận từ đệ đi đúng {@code Service.addSMTN}: giảm
     * theo sức mạnh <b>của sư phụ</b>, rồi nhân hệ số sư phụ.</p>
     */
    private static long[] uocTinh(HeSoTnsmDAO.Dong d, long goc, DieuKienTn sp, DieuKienTn de) {
        boolean ap = d != null && d.bat;
        long spTu = ap && d.chiDeTu ? 0 : quaMob(goc, ap ? d.heSo : 1, sp);
        double hsDe = ap ? (d.heSoDeTu > 0 ? d.heSoDeTu : d.heSo) : 1;
        long deTu = quaMob(goc, hsDe, de);
        long spNhan = NPoint.giamTheoSucManh(deTu, sp.sucManh, sp.gioiHanSucManh);
        double hsSp = ap && d.heSoSuPhu > 0 ? d.heSoSuPhu : 1;
        if (hsSp != 1) {
            spNhan = Math.round(spNhan * hsSp);
        }
        return new long[]{spTu, deTu, spNhan};
    }

    // =====================================================================
    //  Kiểm tra với người chơi thật
    // =====================================================================

    private void napNguoiChoi() {
        Object dangChon = hopNguoiChoi.getSelectedItem();
        hopNguoiChoi.removeAllItems();
        try {
            for (Player p : Client.gI().getPlayers()) {
                if (p != null && p.nPoint != null && p.isPl()) {
                    NguoiChoiMuc m = new NguoiChoiMuc(p);
                    hopNguoiChoi.addItem(m);
                    if (dangChon instanceof NguoiChoiMuc && ((NguoiChoiMuc) dangChon).pl == p) {
                        hopNguoiChoi.setSelectedItem(m);
                    }
                }
            }
        } catch (Exception ex) {
            bao(DO, "Máy chủ chưa chạy — chưa có người chơi nào.");
        }
    }

    /**
     * Lấy bộ điều kiện thật của một người chơi và so ba con số với game.
     *
     * <p>Con số game là cú đánh gần nhất máy chủ đã ghi lại; con số bảng là
     * đúng hàm tính ấy chạy lại với bộ điều kiện vừa lấy. Buff hết hạn hay sức
     * mạnh qua bậc giữa lúc đánh và lúc bấm thì có thể lệch — đánh xong bấm
     * ngay.</p>
     */
    private void kiemTraNguoiChoi() {
        Object o = hopNguoiChoi.getSelectedItem();
        if (!(o instanceof NguoiChoiMuc) || ((NguoiChoiMuc) o).pl == null) {
            bao(DO, "Chọn một người chơi đang online trước.");
            return;
        }
        Player pl = ((NguoiChoiMuc) o).pl;
        DieuKienTn sp = pl.nPoint.dieuKienTnHienTai();
        DieuKienTn de = pl.Detu != null && pl.Detu.nPoint != null
                ? pl.Detu.nPoint.dieuKienTnHienTai() : null;
        chung.gan(sp, de, true);
        if (pl.tnMapCuoi >= 0 && pl.tnGocCuoi > 0) {
            oGoc.setText(String.valueOf(pl.tnGocCuoi));
        }

        StringBuilder sb = new StringBuilder("<html><b>").append(esc(pl.name))
                .append("</b> — điều kiện đã lấy đúng như nhân vật lúc này.<br>");
        if (pl.tnMapCuoi >= 0) {
            double hs = HeSoTnsmDAO.heSo(pl.tnMapCuoi, false);
            dongSoSanh(sb, "Sư phụ tự đánh (map " + pl.tnMapCuoi + ")",
                    pl.tnGocCuoi, pl.tnNhanCuoi, quaMob(pl.tnGocCuoi, hs, sp));
        } else {
            sb.append("Sư phụ chưa đánh con quái nào từ lúc máy chủ bật.<br>");
        }
        if (pl.Detu != null && de != null && pl.Detu.tnMapCuoi >= 0) {
            int map = pl.Detu.tnMapCuoi;
            double hs = HeSoTnsmDAO.heSo(map, true);
            dongSoSanh(sb, "Đệ tự đánh (map " + map + ")",
                    pl.Detu.tnGocCuoi, pl.Detu.tnNhanCuoi, quaMob(pl.Detu.tnGocCuoi, hs, de));
            long est = NPoint.giamTheoSucManh(pl.Detu.tnNhanCuoi, sp.sucManh, sp.gioiHanSucManh);
            double hsSp = HeSoTnsmDAO.heSoSuPhu(map);
            if (hsSp != 1) {
                est = Math.round(est * hsSp);
            }
            dongSoSanh(sb, "Đệ đánh → sư phụ nhận", -1, pl.tnTuDeCuoi, est);
        } else if (pl.Detu != null) {
            sb.append("Đệ tử chưa đánh con quái nào từ lúc máy chủ bật.<br>");
        }
        sb.append("<span style='color:#888'>Buff hết hạn hay sức mạnh qua bậc giữa lúc đánh "
                + "và lúc bấm thì có thể lệch — đánh xong bấm ngay.</span></html>");
        ketQuaKiemTra.setText(sb.toString());
        capNhatUocTinh();
    }

    private static void dongSoSanh(StringBuilder sb, String nhan, long goc, long game, long bangTinh) {
        boolean khop = game == bangTinh;
        sb.append(nhan).append(": ");
        if (goc >= 0) {
            sb.append("gốc ").append(fmt(goc)).append(" → ");
        }
        sb.append("game <b>").append(fmt(game)).append("</b> · bảng tính <b>")
                .append(fmt(bangTinh)).append("</b> ")
                .append(khop ? "<span style='color:#28a046'>✓ khớp</span>"
                        : "<span style='color:#c83c3c'>✗ lệch</span>")
                .append("<br>");
    }

    // =====================================================================
    //  Điều kiện chung / riêng
    // =====================================================================

    private void suaDieuKienChung() {
        BangDieuKien b = chung.banSao();
        int chon = JOptionPane.showOptionDialog(this, cuonNho(b),
                "Điều kiện chung — áp cho mọi dòng chưa đặt riêng",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Dùng", "Về mặc định (không buff)", "Huỷ"}, "Dùng");
        if (chon == 0) {
            chung.gan(b.dkSuPhu(), b.dkDeTu(), b.dangLayThat());
        } else if (chon == 1) {
            chung.gan(new DieuKienTn(), new DieuKienTn(), false);
            chung.macDinhMayChu();
        }
        capNhatUocTinh();
    }

    private void suaDieuKienDong(int r) {
        int id = r < idDong.size() ? idDong.get(r) : -1;
        String ten = o(r, C_TEN);
        BangDieuKien b = rieng.getOrDefault(id, chung).banSao();
        int chon = JOptionPane.showOptionDialog(this, cuonNho(b),
                "Điều kiện riêng — " + ten,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Dùng cho dòng này", "Bỏ riêng, dùng chung", "Huỷ"},
                "Dùng cho dòng này");
        if (chon == 0) {
            rieng.put(id, b);
        } else if (chon == 1) {
            rieng.remove(id);
        }
        capNhatUocTinh();
    }

    private static JScrollPane cuonNho(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setPreferredSize(new Dimension(980, 430));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // =====================================================================
    //  Lưu / thêm / về mặc định
    // =====================================================================

    private void luuBang() {
        dungSua();
        Map<Integer, HeSoTnsmDAO.Dong> theoId = new HashMap<>();
        for (HeSoTnsmDAO.Dong d : HeSoTnsmDAO.danhSach()) {
            theoId.put(d.id, d);
        }
        int hong = 0;
        String hongDau = null;
        for (int r = 0; r < model.getRowCount() && r < idDong.size(); r++) {
            HeSoTnsmDAO.Dong d = theoId.get(idDong.get(r));
            if (d == null) {
                continue;
            }
            try {
                d.heSo = soThuc(o(r, C_SP), 1);
                d.heSoDeTu = soThuc(o(r, C_DE_DE), 0);
                d.heSoSuPhu = soThuc(o(r, C_DE_SP), 0);
            } catch (NumberFormatException ex) {
                hong++;
                if (hongDau == null) {
                    hongDau = "Nhóm \"" + o(r, C_TEN) + "\" có hệ số không phải số.";
                }
                continue;
            }
            d.ten = o(r, C_TEN);
            d.dsMap = o(r, C_MAP);
            d.chiDeTu = Boolean.TRUE.equals(model.getValueAt(r, C_CHIDE));
            Object bat = model.getValueAt(r, C_BAT);
            d.bat = !(bat instanceof Boolean) || (Boolean) bat;
            d.ghiChu = o(r, C_GC);
            String loi = HeSoTnsmDAO.luu(d);
            if (loi != null) {
                hong++;
                if (hongDau == null) {
                    hongDau = loi;
                }
            }
        }
        napBang();
        bao(hong == 0 ? XANH_LA : DO, hong == 0
                ? "Đã lưu hệ số tiềm năng theo bản đồ — có hiệu lực ngay."
                : hongDau + " (" + hong + " nhóm không lưu được)");
    }

    private void themNhom() {
        String ten = JOptionPane.showInputDialog(this,
                "Tên nhóm bản đồ mới?\n\nThêm xong thì điền id bản đồ và hệ số rồi bấm Lưu.",
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
        napBang();
        bao(loi == null ? XANH_LA : DO,
                loi == null ? "Đã thêm nhóm — điền id bản đồ rồi bấm Lưu." : loi);
    }

    private void veMacDinh() {
        if (JOptionPane.showConfirmDialog(this, "Xoá sạch bảng rồi gieo lại các nhóm gốc?",
                "Về mặc định", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        String loi = HeSoTnsmDAO.gieoLai();
        rieng.clear();
        napBang();
        bao(loi == null ? XANH_LA : DO, loi == null ? "Đã gieo lại các nhóm gốc." : loi);
    }

    // =====================================================================
    //  Tiện ích
    // =====================================================================

    private void dungSua() {
        if (bang.isEditing()) {
            bang.getCellEditor().stopCellEditing();
        }
    }

    private String o(int r, int c) {
        Object v = model.getValueAt(r, c);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static double soThuc(String s, double deTrong) {
        String v = s.trim().replace(',', '.');
        return v.isEmpty() ? deTrong : Double.parseDouble(v);
    }

    /** In hệ số gọn, giữ ba chữ số thập phân — một phần ba không được thành 0,33. */
    private static String soGon(double v) {
        String s = String.format(Locale.US, "%.3f", v);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String fmt(long v) {
        return PlayerManagerPanel.fmt(v);
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void bao(Color mau, String chu) {
        trangThai.setForeground(mau);
        trangThai.setText(chu);
    }

    private static JPanel dongTrai() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JComponent canTrai(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private static JLabel ghiChuXam(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(XAM);
        return l;
    }

    private static JButton nut(String chu, Color nen, Runnable viec) {
        JButton b = new JButton(chu);
        b.setBackground(nen);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        b.setOpaque(true);
        b.addActionListener(e -> viec.run());
        return b;
    }

    /** Lắng nghe gõ phím cho ô chữ, gọn một dòng. */
    private static final class KhiGo implements DocumentListener {

        private final Runnable r;

        KhiGo(Runnable r) {
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

    private static final class NguoiChoiMuc {

        final Player pl;

        NguoiChoiMuc(Player pl) {
            this.pl = pl;
        }

        @Override
        public String toString() {
            if (pl == null) {
                return "Tên nhân vật dài dài";
            }
            return pl.name + (pl.Detu != null ? "  (có đệ)" : "");
        }
    }

    /** Ô ước tính: căn phải, chữ đậm, nền xám — nhìn là biết chỉ để xem. */
    private static final class OUocTinh extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean chon,
                boolean tieuDiem, int r, int c) {
            Component o = super.getTableCellRendererComponent(t, v, chon, tieuDiem, r, c);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (!chon) {
                o.setBackground(NEN_UOC);
            }
            o.setForeground("khoá".equals(String.valueOf(v)) ? DO : new Color(40, 60, 90));
            o.setFont(o.getFont().deriveFont(Font.BOLD));
            return o;
        }
    }

    /** Ô điều kiện: "chung" xám nghiêng, "riêng" xanh đậm. */
    private static final class ODieuKien extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean chon,
                boolean tieuDiem, int r, int c) {
            Component o = super.getTableCellRendererComponent(t, v, chon, tieuDiem, r, c);
            boolean rieng = String.valueOf(v).startsWith("riêng");
            if (!chon) {
                o.setForeground(rieng ? XANH : XAM);
            }
            o.setFont(o.getFont().deriveFont(rieng ? Font.BOLD : Font.ITALIC));
            setToolTipText("Bấm đúp để đặt điều kiện riêng cho dòng này");
            return o;
        }
    }

    /** Dải nhóm cột phía trên tiêu đề. */
    private final class DaiNhomCot extends JComponent {

        DaiNhomCot() {
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(bang.getColumnModel().getTotalColumnWidth(), 22);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(240, 243, 247));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            TableColumnModel cm = bang.getColumnModel();
            for (Object[] n : NHOM_COT) {
                int dau = (Integer) n[0];
                int cuoi = (Integer) n[1];
                int x0 = -1;
                int x1 = -1;
                int x = 0;
                for (int i = 0; i < cm.getColumnCount(); i++) {
                    int w = cm.getColumn(i).getWidth();
                    int mi = cm.getColumn(i).getModelIndex();
                    if (mi >= dau && mi <= cuoi) {
                        if (x0 < 0) {
                            x0 = x;
                        }
                        x1 = x + w;
                    }
                    x += w;
                }
                if (x0 < 0) {
                    continue;
                }
                g.setColor(new Color(214, 228, 244));
                g.fillRect(x0, 0, x1 - x0, getHeight());
                g.setColor(new Color(176, 196, 222));
                g.drawRect(x0, 0, x1 - x0 - 1, getHeight() - 1);
                String s = (String) n[2];
                int rong = x1 - x0 - 8;
                while (s.length() > 3 && fm.stringWidth(s) > rong) {
                    s = s.substring(0, s.length() - 2) + "…";
                }
                g.setColor(new Color(24, 58, 110));
                int tx = x0 + Math.max(4, (x1 - x0 - fm.stringWidth(s)) / 2);
                g.drawString(s, tx, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }

    // =====================================================================
    //  Bộ điều kiện tính thử
    // =====================================================================

    /**
     * Mọi ô điều kiện — mỗi ô ứng đúng một trường của {@link DieuKienTn}.
     *
     * <p>Lấy từ người chơi thật thì giữ nguyên bộ điều kiện ấy để tính cho chính
     * xác (kể cả những thứ không có ô); sửa bất kỳ ô nào là chuyển sang tính
     * theo ô.</p>
     */
    static final class BangDieuKien extends JPanel {

        final JTextField smSp = oSo();
        final JTextField smDe = oSo();
        final JTextField suKien = oSo();
        final JTextField heSoMayChu = oSo();
        final JComboBox<String> theTuan = new JComboBox<>(new String[]{"Không", "Thường +20%", "Cao +50%"});
        final JComboBox<String> theThang = new JComboBox<>(new String[]{"Không", "Thường +100%", "Cao +150%"});
        final JComboBox<String> theNam = new JComboBox<>(new String[]{"Không", "Thường +200%", "Cao +300%"});
        final JCheckBox theChiTon = o("Thẻ chí tôn +500%");
        final JComboBox<String> co = new JComboBox<>(new String[]{"Không cờ", "Cờ thường +5%", "Cờ 8 +10%"});
        final JCheckBox[] buaTri = {o("x2"), o("x3"), o("x4"), o("x5"), o("x7"), o("x10"), o("x20")};
        final JCheckBox khauTrang = o("Khẩu trang +20%");
        final JCheckBox vip = o("VIP");
        final JCheckBox veTinh = o("Vệ tinh trí lực");
        final JCheckBox chibi = o("Chibi TN");
        final JCheckBox rongXuong3 = o("Rồng xương 3");
        final JCheckBox duoiKhi = o("Đuôi khỉ TN");
        final JCheckBox cong20 = o("+20% khi đánh quái");
        final JTextField noiTai = oSo();
        final JTextField tnTrangBi = oSo();
        final JTextField tnSet = oSo();
        final JTextField bangBuff = oSo();
        final JTextField bangCap = oSo();
        final JComboBox<String> loaiDe = new JComboBox<>(new String[]{
            "0 · Thường (×1)", "1 · Mabư (×1)", "2 · Sơn Tinh (÷2)", "3 · Thuỷ Tinh (÷2)",
            "4 · (÷2)", "5 · Bư Nhí / Xên Nhí / Fide (÷4)"});
        final JCheckBox buaTnDe = o("Bùa TN đệ (vật phẩm)");
        final JCheckBox[] buaDe = {o("+2×"), o("+3×"), o("+4×"), o("+5×"), o("+6×"), o("+8×"),
            o("+10×"), o("+20×")};
        final JCheckBox spRongXuong = o("Rồng xương (sư phụ)");
        final JTextField tnDeTrenDoSp = oSo();
        final JTextField tnTrangBiDe = oSo();
        final JComboBox<String> coDe = new JComboBox<>(new String[]{"Không cờ", "Cờ thường +5%", "Cờ 8 +10%"});

        final List<Runnable> khiDoi = new ArrayList<>();
        private DieuKienTn spThat;
        private DieuKienTn deThat;
        private boolean dangGan;

        BangDieuKien() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setBorder(new EmptyBorder(6, 6, 6, 6));
            add(cot("Chung",
                    hang("Sức mạnh sư phụ", smSp),
                    hang("Sức mạnh đệ tử", smDe),
                    hang("Sự kiện TN máy chủ %", suKien),
                    hang("Hệ số exp máy chủ", heSoMayChu)));
            add(Box.createHorizontalStrut(18));
            add(cot("Sư phụ",
                    hang("Thẻ tuần", theTuan),
                    hang("Thẻ tháng", theThang),
                    hang("Thẻ năm", theNam),
                    hang(null, theChiTon),
                    hang("Cờ", co),
                    hang("Bùa trí tuệ", nhomO(buaTri)),
                    hang(null, nhomO(khauTrang, vip, veTinh)),
                    hang(null, nhomO(chibi, rongXuong3, duoiKhi)),
                    hang(null, cong20),
                    hang("Nội tại TN %", noiTai),
                    hang("TN trang bị % (10,20…)", tnTrangBi),
                    hang("TN từ set %", tnSet),
                    hang("Buff TN bang (lần)", bangBuff),
                    hang("Bùa trí tuệ bang: cấp bang", bangCap)));
            add(Box.createHorizontalStrut(18));
            add(cot("Đệ tử",
                    hang("Loại đệ", loaiDe),
                    hang(null, buaTnDe),
                    hang("Bùa đệ tử", nhomO(buaDe)),
                    hang(null, spRongXuong),
                    hang("TN cho đệ trên đồ SP %", tnDeTrenDoSp),
                    hang("TN trang bị đệ %", tnTrangBiDe),
                    hang("Cờ đệ", coDe)));
            noiThayDoi();
        }

        /** Hai con số máy chủ đang dùng thật lúc này. */
        void macDinhMayChu() {
            suKien.setText(String.valueOf(NPoint.phanTramSuKienTnMayChu()));
            heSoMayChu.setText(String.valueOf(Manager.RATE_EXP_SERVER));
        }

        boolean dangLayThat() {
            return spThat != null;
        }

        private void noiThayDoi() {
            Runnable doi = () -> {
                if (dangGan) {
                    return;
                }
                spThat = null;
                deThat = null;
                bao();
            };
            KhiGo kg = new KhiGo(doi);
            for (JTextField f : new JTextField[]{smSp, smDe, suKien, heSoMayChu, noiTai,
                tnTrangBi, tnSet, bangBuff, bangCap, tnDeTrenDoSp, tnTrangBiDe}) {
                f.getDocument().addDocumentListener(kg);
            }
            for (JComboBox<?> h : new JComboBox<?>[]{theTuan, theThang, theNam, co, loaiDe, coDe}) {
                h.addActionListener(e -> doi.run());
            }
            for (JCheckBox c : tatCaO()) {
                c.addActionListener(e -> doi.run());
            }
        }

        private void bao() {
            for (Runnable r : khiDoi) {
                r.run();
            }
        }

        private List<JCheckBox> tatCaO() {
            List<JCheckBox> ds = new ArrayList<>();
            for (JCheckBox c : buaTri) {
                ds.add(c);
            }
            for (JCheckBox c : buaDe) {
                ds.add(c);
            }
            ds.add(theChiTon);
            ds.add(khauTrang);
            ds.add(vip);
            ds.add(veTinh);
            ds.add(chibi);
            ds.add(rongXuong3);
            ds.add(duoiKhi);
            ds.add(cong20);
            ds.add(buaTnDe);
            ds.add(spRongXuong);
            return ds;
        }

        /** Đổ một bộ điều kiện vào các ô. {@code giuNguyen} thì tính đúng theo bộ ấy. */
        void gan(DieuKienTn sp, DieuKienTn de, boolean giuNguyen) {
            dangGan = true;
            try {
                if (sp != null) {
                    smSp.setText(String.valueOf(sp.sucManh));
                    suKien.setText(String.valueOf(sp.thuocTinhMayChu));
                    heSoMayChu.setText(String.valueOf(sp.heSoMayChu));
                    theTuan.setSelectedIndex(muc3(sp.theTuan));
                    theThang.setSelectedIndex(muc3(sp.theThang));
                    theNam.setSelectedIndex(muc3(sp.theNam));
                    theChiTon.setSelected(sp.theChiTon);
                    co.setSelectedIndex(mucCo(sp.co));
                    boolean[] b = {sp.buaTriTue, sp.buaTriTue3, sp.buaTriTue4, sp.buaTriTue5,
                        sp.buaTriTue7, sp.buaTriTue10, sp.buaTriTue20};
                    for (int i = 0; i < buaTri.length; i++) {
                        buaTri[i].setSelected(b[i]);
                    }
                    khauTrang.setSelected(sp.khauTrang);
                    vip.setSelected(sp.vip);
                    veTinh.setSelected(sp.veTinhTriLuc);
                    chibi.setSelected(sp.chibiTn);
                    rongXuong3.setSelected(sp.rongXuong3);
                    duoiKhi.setSelected(sp.duoiKhiTn);
                    cong20.setSelected(sp.cong20KhiDanhQuai);
                    noiTai.setText(sp.noiTaiTn == 0 ? "" : String.valueOf(sp.noiTaiTn));
                    tnTrangBi.setText(noi(sp.tlTnsm));
                    tnSet.setText(sp.setTnPct == 0 ? "" : String.valueOf(sp.setTnPct));
                    bangBuff.setText(sp.bangCongTn == 0 ? "" : String.valueOf(sp.bangCongTn));
                    bangCap.setText(sp.bangBuaTriTue < 0 ? "" : String.valueOf(sp.bangBuaTriTue));
                }
                if (de != null) {
                    smDe.setText(String.valueOf(de.sucManh));
                    loaiDe.setSelectedIndex(de.loaiDeTu >= 0 && de.loaiDeTu <= 5 ? de.loaiDeTu : 0);
                    buaTnDe.setSelected(de.spBuaTnsmDeTu);
                    boolean[] b = {de.spBuaDeTu, de.spBuaDeTu2, de.spBuaDeTu3, de.spBuaDeTu4,
                        de.spBuaDeTu5, de.spBuaDeTu7, de.spBuaDeTu10, de.spBuaDeTu20};
                    for (int i = 0; i < buaDe.length; i++) {
                        buaDe[i].setSelected(b[i]);
                    }
                    spRongXuong.setSelected(de.spRongXuong);
                    tnDeTrenDoSp.setText(de.spTlTnsmPet == 0 ? "" : String.valueOf(de.spTlTnsmPet));
                    tnTrangBiDe.setText(noi(de.tlTnsm));
                    coDe.setSelectedIndex(mucCo(de.co));
                }
            } finally {
                dangGan = false;
            }
            spThat = giuNguyen ? sp : null;
            deThat = giuNguyen ? de : null;
            bao();
        }

        DieuKienTn dkSuPhu() {
            if (spThat != null) {
                return spThat;
            }
            DieuKienTn dk = new DieuKienTn();
            dk.sucManh = soL(smSp, 0);
            dk.tlTnsm = danhSach(tnTrangBi);
            dk.co = coTu(co.getSelectedIndex());
            dk.thuocTinhMayChu = (int) soL(suKien, 0);
            dk.theTuan = theTuan.getSelectedIndex();
            dk.theThang = theThang.getSelectedIndex();
            dk.theNam = theNam.getSelectedIndex();
            dk.theChiTon = theChiTon.isSelected();
            dk.khauTrang = khauTrang.isSelected();
            dk.buaTriTue = buaTri[0].isSelected();
            dk.buaTriTue3 = buaTri[1].isSelected();
            dk.buaTriTue4 = buaTri[2].isSelected();
            dk.buaTriTue5 = buaTri[3].isSelected();
            dk.buaTriTue7 = buaTri[4].isSelected();
            dk.buaTriTue10 = buaTri[5].isSelected();
            dk.buaTriTue20 = buaTri[6].isSelected();
            dk.noiTaiTn = (int) soL(noiTai, 0);
            dk.chibiTn = chibi.isSelected();
            dk.bangCongTn = soL(bangBuff, 0);
            dk.bangBuaTriTue = bangCap.getText().trim().isEmpty() ? -1 : (int) soL(bangCap, -1);
            dk.veTinhTriLuc = veTinh.isSelected();
            dk.vip = vip.isSelected();
            dk.rongXuong3 = rongXuong3.isSelected();
            dk.duoiKhiTn = duoiKhi.isSelected();
            dk.cong20KhiDanhQuai = cong20.isSelected();
            dk.setTnPct = (int) soL(tnSet, 0);
            dk.heSoMayChu = (int) soL(heSoMayChu, Manager.RATE_EXP_SERVER);
            return dk;
        }

        DieuKienTn dkDeTu() {
            if (deThat != null) {
                return deThat;
            }
            DieuKienTn dk = new DieuKienTn();
            dk.sucManh = soL(smDe, 0);
            dk.tlTnsm = danhSach(tnTrangBiDe);
            dk.co = coTu(coDe.getSelectedIndex());
            dk.laDeTu = true;
            dk.coSuPhu = true;
            dk.loaiDeTu = loaiDe.getSelectedIndex();
            dk.spBuaTnsmDeTu = buaTnDe.isSelected();
            dk.spBuaDeTu = buaDe[0].isSelected();
            dk.spBuaDeTu2 = buaDe[1].isSelected();
            dk.spBuaDeTu3 = buaDe[2].isSelected();
            dk.spBuaDeTu4 = buaDe[3].isSelected();
            dk.spBuaDeTu5 = buaDe[4].isSelected();
            dk.spBuaDeTu7 = buaDe[5].isSelected();
            dk.spBuaDeTu10 = buaDe[6].isSelected();
            dk.spBuaDeTu20 = buaDe[7].isSelected();
            dk.spRongXuong = spRongXuong.isSelected();
            // The cua su phu cung cong cho de, dung nhu may chu doc tu su phu.
            dk.spTheTuan = theTuan.getSelectedIndex();
            dk.spTheThang = theThang.getSelectedIndex();
            dk.spTheNam = theNam.getSelectedIndex();
            dk.spTheChiTon = theChiTon.isSelected();
            dk.spTlTnsmPet = soL(tnDeTrenDoSp, 0);
            dk.spCoNPoint = true;
            dk.heSoMayChu = (int) soL(heSoMayChu, Manager.RATE_EXP_SERVER);
            return dk;
        }

        BangDieuKien banSao() {
            BangDieuKien b = new BangDieuKien();
            b.gan(dkSuPhu(), dkDeTu(), spThat != null);
            return b;
        }

        /** Tóm tắt ngắn những gì đang bật. */
        String tomTat() {
            List<String> ds = new ArrayList<>();
            if (spThat != null) {
                ds.add("lấy từ người chơi");
            }
            for (JCheckBox c : buaTri) {
                if (c.isSelected()) {
                    ds.add("trí tuệ " + c.getText());
                }
            }
            if (theTuan.getSelectedIndex() > 0) {
                ds.add("thẻ tuần");
            }
            if (theThang.getSelectedIndex() > 0) {
                ds.add("thẻ tháng");
            }
            if (theNam.getSelectedIndex() > 0) {
                ds.add("thẻ năm");
            }
            if (theChiTon.isSelected()) {
                ds.add("chí tôn");
            }
            if (co.getSelectedIndex() > 0) {
                ds.add("cờ");
            }
            if (vip.isSelected()) {
                ds.add("VIP");
            }
            for (JCheckBox c : buaDe) {
                if (c.isSelected()) {
                    ds.add("bùa đệ " + c.getText());
                }
            }
            if (buaTnDe.isSelected()) {
                ds.add("bùa TN đệ");
            }
            if (ds.isEmpty()) {
                return "không buff";
            }
            String s = String.join(" · ", ds);
            return s.length() > 70 ? s.substring(0, 67) + "…" : s;
        }

        // ----------------------------------------------- tien ich cua bang

        private static int muc3(int v) {
            return v == 0 ? 0 : (v == 1 ? 1 : 2);
        }

        private static int mucCo(int v) {
            return v == 0 ? 0 : (v == 8 ? 2 : 1);
        }

        private static int coTu(int muc) {
            return muc == 0 ? 0 : (muc == 2 ? 8 : 1);
        }

        private static long soL(JTextField f, long deTrong) {
            String s = f.getText().trim().replace(".", "").replace(",", "").replace(" ", "");
            if (s.isEmpty()) {
                return deTrong;
            }
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ex) {
                return deTrong;
            }
        }

        private static List<Integer> danhSach(JTextField f) {
            List<Integer> ds = new ArrayList<>();
            for (String p : f.getText().split("[,;\\s]+")) {
                String t = p.trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    ds.add(Integer.parseInt(t));
                } catch (NumberFormatException boQua) {
                    // Go sai thi bo qua phan do.
                }
            }
            return ds;
        }

        private static String noi(List<Integer> ds) {
            if (ds == null || ds.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (Integer i : ds) {
                if (i == null) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(i);
            }
            return sb.toString();
        }

        private static JTextField oSo() {
            JTextField f = new JTextField(9);
            f.setHorizontalAlignment(JTextField.RIGHT);
            return f;
        }

        private static JCheckBox o(String t) {
            JCheckBox c = new JCheckBox(t);
            c.setOpaque(false);
            return c;
        }

        private static JPanel nhomO(JCheckBox... ds) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            p.setOpaque(false);
            for (JCheckBox c : ds) {
                p.add(c);
            }
            return p;
        }

        private static JComponent hang(String nhan, JComponent comp) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
            p.setOpaque(false);
            if (nhan != null) {
                JLabel l = new JLabel(nhan);
                l.setPreferredSize(new Dimension(170, 22));
                p.add(l);
            }
            p.add(comp);
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
            return p;
        }

        private static JPanel cot(String ten, JComponent... hangs) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);
            JLabel t = new JLabel(ten);
            t.setFont(t.getFont().deriveFont(Font.BOLD, 13f));
            t.setForeground(XANH);
            t.setBorder(new EmptyBorder(0, 4, 4, 0));
            t.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(t);
            for (JComponent h : hangs) {
                p.add(h);
            }
            p.setAlignmentY(Component.TOP_ALIGNMENT);
            return p;
        }
    }
}
