package nro.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.*;
import java.net.URL;

public class ServerGuiUtils {

    /**
     * Phông chữ giao diện, chọn theo thứ tự ưu tiên.
     *
     * <p>Dò tên phông thay vì gọi thẳng: máy không có phông đó thì Java lặng lẽ
     * thay bằng một phông thay thế trông rất khác, và không có dấu hiệu gì để
     * biết. Dò trước thì biết chắc mình đang dùng cái nào.</p>
     */
    private static java.awt.Font phongGiaoDien() {
        String[] uuTien = {"Segoe UI Variable Text", "Segoe UI", "Inter",
            "Roboto", "Tahoma"};
        java.util.Set<String> co = new java.util.HashSet<>(java.util.Arrays.asList(
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames()));
        for (String ten : uuTien) {
            if (co.contains(ten)) {
                return new javax.swing.plaf.FontUIResource(ten, java.awt.Font.PLAIN, 13);
            }
        }
        return null;
    }

    private static boolean daDatGiaoDien;

    /**
     * Bật giao diện tối, nhấn cam, cho mọi cửa sổ của bảng điều khiển.
     *
     * <p>Gọi <b>trước</b> khi tạo cửa sổ đầu tiên: FlatLaf chỉ tự vẽ thanh tiêu
     * đề (tối, cùng tông) cho những {@code JFrame} tạo ra sau khi Look and Feel
     * đã đặt. Gọi lại lần nữa thì bỏ qua.</p>
     */
    public static synchronized void setupTheme() {
        if (daDatGiaoDien) {
            return;
        }
        daDatGiaoDien = true;
        try {
            // Thanh tieu de do FlatLaf ve (Windows 10/11): cung tong toi voi noi
            // dung thay vi thanh trang cua he dieu hanh.
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // --- Phong chu ---
            // Segoe UI Variable la phong he thong cua Windows 11, net hon han
            // Dialog mac dinh cua Java o man hinh phan giai cao. Khong co thi
            // lui ve Segoe UI roi moi den mac dinh.
            java.awt.Font phong = phongGiaoDien();
            if (phong != null) {
                UIManager.put("defaultFont", phong);
            }

            // --- Nen ba bac ---
            // Panel, o nhap, bang deu lay tu UiTheme de moi man cung mot tong.
            UIManager.put("Panel.background", UiTheme.CARD);
            UIManager.put("control", UiTheme.CARD);
            UIManager.put("RootPane.background", UiTheme.CONTENT_BG);
            UIManager.put("Viewport.background", UiTheme.CARD);
            UIManager.put("ScrollPane.background", UiTheme.CARD);
            UIManager.put("TextField.background", UiTheme.FIELD);
            UIManager.put("FormattedTextField.background", UiTheme.FIELD);
            UIManager.put("PasswordField.background", UiTheme.FIELD);
            UIManager.put("TextArea.background", UiTheme.FIELD);
            UIManager.put("TextPane.background", UiTheme.FIELD);
            UIManager.put("EditorPane.background", UiTheme.FIELD);
            UIManager.put("Spinner.background", UiTheme.FIELD);
            UIManager.put("ComboBox.background", UiTheme.FIELD);
            UIManager.put("ComboBox.buttonBackground", UiTheme.FIELD);
            UIManager.put("ComboBox.popupBackground", UiTheme.CARD_ALT);
            UIManager.put("TextField.inactiveBackground", UiTheme.FIELD_DISABLED);
            UIManager.put("TextArea.inactiveBackground", UiTheme.FIELD_DISABLED);
            UIManager.put("TextComponent.selectionBackground", UiTheme.SELECTION);
            UIManager.put("TextComponent.selectionForeground", UiTheme.TEXT_ON_DARK);
            UIManager.put("Label.foreground", UiTheme.TEXT);
            UIManager.put("Label.disabledForeground", UiTheme.TEXT_FAINT);

            // --- Bo goc ---
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 14);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("ProgressBar.arc", 999);

            // --- Vien va vung nhan ---
            // Vien mong va vung sang quanh o dang go hep: bang nay co hang chuc o
            // canh nhau, vien day lam ca man nhin nang ne.
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 1);
            UIManager.put("Component.borderWidth", 1);

            // --- Nut ---
            // 72 chu khong phai 88: hang cong cu cua man Nguoi choi co san mot
            // nut, hai o tich va mot o go — nut rong 88 la ca hang day ra khoi
            // be ngang va de len tieu de.
            UIManager.put("Button.minimumWidth", 72);
            UIManager.put("Button.margin", new java.awt.Insets(6, 16, 6, 16));
            UIManager.put("Button.background", UiTheme.CARD_ALT);
            UIManager.put("Button.borderColor", UiTheme.LINE);
            UIManager.put("Button.hoverBorderColor", UiTheme.ACCENT);
            UIManager.put("Button.default.background", UiTheme.ACCENT);
            UIManager.put("Button.default.foreground", java.awt.Color.WHITE);
            UIManager.put("Button.default.borderColor", UiTheme.ACCENT);

            // --- Thanh cuon ---
            // Khong ve nut mui ten hai dau: chung chiem cho ma khong ai bam.
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.track", UiTheme.CARD);
            UIManager.put("ScrollBar.thumb", new java.awt.Color(0x3A, 0x3F, 0x48));
            UIManager.put("ScrollBar.hoverThumbColor", UiTheme.ACCENT);
            UIManager.put("ScrollBar.showButtons", false);

            // --- The ---
            UIManager.put("TabbedPane.tabHeight", 34);
            UIManager.put("TabbedPane.tabInsets", new java.awt.Insets(4, 16, 4, 16));
            UIManager.put("TabbedPane.background", UiTheme.CARD);
            UIManager.put("TabbedPane.selectedBackground", UiTheme.CARD_ALT);
            UIManager.put("TabbedPane.hoverColor", new java.awt.Color(0x28, 0x2B, 0x32));
            UIManager.put("TabbedPane.foreground", UiTheme.TEXT_MUTED);
            UIManager.put("TabbedPane.selectedForeground", UiTheme.TEXT_ON_DARK);
            UIManager.put("TabbedPane.contentSeparatorHeight", 1);
            UIManager.put("TabbedPane.contentAreaColor", UiTheme.LINE);
            UIManager.put("TabbedPane.showTabSeparators", false);
            UIManager.put("TabbedPane.underlineColor", UiTheme.ACCENT);
            UIManager.put("TabbedPane.inactiveUnderlineColor", UiTheme.ACCENT_DIM);
            UIManager.put("TabbedPane.tabSelectionHeight", 3);
            UIManager.put("TabbedPane.focusColor", new java.awt.Color(0, 0, 0, 0));

            // --- Hop thoai, chu goi y, khung ---
            UIManager.put("ToolTip.background", new java.awt.Color(0x2A, 0x2E, 0x36));
            UIManager.put("ToolTip.foreground", UiTheme.TEXT_ON_DARK);
            UIManager.put("OptionPane.showIcon", true);
            UIManager.put("OptionPane.background", UiTheme.CARD);
            UIManager.put("SplitPane.dividerSize", 6);
            UIManager.put("SplitPane.background", UiTheme.CONTENT_BG);
            UIManager.put("SplitPaneDivider.gripDotCount", 3);
            UIManager.put("SplitPaneDivider.gripColor", UiTheme.TEXT_FAINT);
            UIManager.put("TitledBorder.titleColor", UiTheme.ACCENT_TEXT);
            UIManager.put("Separator.foreground", UiTheme.LINE);
            UIManager.put("TitlePane.background", UiTheme.SIDEBAR_HEADER);
            UIManager.put("TitlePane.inactiveBackground", UiTheme.SIDEBAR_HEADER);
            UIManager.put("TitlePane.foreground", UiTheme.TEXT_ON_DARK);
            UIManager.put("TitlePane.inactiveForeground", UiTheme.TEXT_MUTED_DARK);
            UIManager.put("TitlePane.embeddedForeground", UiTheme.TEXT_MUTED_DARK);

            // --- Mau nhan ---
            // FlatLaf mac dinh nhan mau xanh. Doi sang cam cua UiTheme de moi
            // thu duoc chon (dong bang, o dang go, o tich) cung mot mau voi
            // vach chon o thanh ben.
            UIManager.put("Component.accentColor", UiTheme.ACCENT);
            UIManager.put("Component.focusColor", UiTheme.ACCENT_DIM);
            UIManager.put("Component.focusedBorderColor", UiTheme.ACCENT);
            UIManager.put("Component.borderColor", UiTheme.LINE);
            UIManager.put("Component.disabledBorderColor", UiTheme.LINE_SOFT);
            UIManager.put("CheckBox.icon.checkmarkColor", java.awt.Color.WHITE);
            UIManager.put("CheckBox.icon.background", UiTheme.FIELD);
            UIManager.put("CheckBox.icon.borderColor", UiTheme.LINE);
            UIManager.put("CheckBox.icon.selectedBackground", UiTheme.ACCENT);
            UIManager.put("CheckBox.icon.selectedBorderColor", UiTheme.ACCENT);
            UIManager.put("ProgressBar.foreground", UiTheme.ACCENT);
            UIManager.put("ProgressBar.background", UiTheme.FIELD);
            UIManager.put("Slider.thumbColor", UiTheme.ACCENT);
            UIManager.put("Slider.trackValueColor", UiTheme.ACCENT);

            // --- Bang ---
            // Ke soc chan chim va duong ke ngang: bang cua panel nay rong toi
            // 15 cot, khong co soc thi mat de bam nham dong khi doc ngang.
            UIManager.put("Table.background", UiTheme.CARD);
            UIManager.put("Table.foreground", UiTheme.TEXT);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.gridColor", UiTheme.LINE_SOFT);
            UIManager.put("Table.alternateRowColor", UiTheme.CARD_ALT);
            UIManager.put("Table.selectionBackground", UiTheme.SELECTION);
            UIManager.put("Table.selectionForeground", UiTheme.TEXT_ON_DARK);
            UIManager.put("Table.selectionInactiveBackground", new java.awt.Color(0x38, 0x2A, 0x1B));
            UIManager.put("Table.selectionInactiveForeground", UiTheme.TEXT);
            UIManager.put("Table.rowHeight", 28);
            UIManager.put("TableHeader.height", 32);
            UIManager.put("TableHeader.background", new java.awt.Color(0x19, 0x1B, 0x20));
            UIManager.put("TableHeader.foreground", UiTheme.TEXT_MUTED);
            UIManager.put("TableHeader.separatorColor", UiTheme.LINE);
            UIManager.put("TableHeader.bottomSeparatorColor", UiTheme.ACCENT_DIM);

            // --- Danh sach va menu ---
            UIManager.put("List.background", UiTheme.CARD);
            UIManager.put("List.selectionBackground", UiTheme.SELECTION);
            UIManager.put("List.selectionForeground", UiTheme.TEXT_ON_DARK);
            UIManager.put("PopupMenu.background", UiTheme.CARD_ALT);
            UIManager.put("PopupMenu.borderColor", UiTheme.LINE);
            UIManager.put("MenuItem.selectionBackground", UiTheme.SELECTION);
            UIManager.put("Menu.selectionBackground", UiTheme.SELECTION);
            UIManager.put("Tree.background", UiTheme.CARD);
            UIManager.put("Tree.selectionBackground", UiTheme.SELECTION);
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF: " + e.getMessage());
        }
    }

    /**
     * Vùng cuộn có bước lăn chuột tử tế.
     *
     * <p>{@code JScrollPane} mặc định hỏi phần nội dung xem mỗi nấc lăn nên đi
     * bao xa. {@code JTable} trả về <b>khoảng cách còn lại tới mép dòng kế
     * tiếp</b>, thường chỉ vài điểm ảnh, nên lăn chuột trong bảng bò từng tí
     * một. Với một {@code JPanel} thường thì còn tệ hơn: đúng 1 điểm ảnh mỗi
     * nấc.</p>
     *
     * <p>Đặt cứng bước cuộn ở đây, dùng thay cho {@code new JScrollPane(...)}
     * trong mọi panel để chỗ nào cũng cuộn giống nhau.</p>
     */
    public static JScrollPane cuon(Component noiDung) {
        JScrollPane sc = new JScrollPane(noiDung);
        // 48 = ba dong bang cao 16, hoac mot dong bang cao 32. Lan mot nac di
        // duoc mot quang nhin thay ro ma khong nhay coc.
        sc.getVerticalScrollBar().setUnitIncrement(48);
        sc.getVerticalScrollBar().setBlockIncrement(240);
        sc.getHorizontalScrollBar().setUnitIncrement(48);
        return sc;
    }

    public static TitledBorder createSectionBorder(String title) {
        return BorderFactory.createTitledBorder(
            new LineBorder(UiTheme.LINE, 1, true), title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), UiTheme.ACCENT_TEXT
        );
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toNut(b, bg);
        return b;
    }

    /**
     * Làm một nút lệnh có màu cho hợp nền tối: bo góc, không viền, chữ trắng.
     *
     * <p>Các hàm {@code button(...)} của từng panel gắn {@code EmptyBorder} để
     * nới lề — viền ấy thay mất viền của FlatLaf nên nút vẽ thành khối vuông
     * vức. Ở đây trả lại viền FlatLaf rồi nới lề bằng {@code margin}.</p>
     *
     * <p>Màu truyền vào được quy về tông nút (xem {@link #nenNut}): các panel
     * dùng chung một hằng cho chữ xám lẫn nút xám, mà xám sáng thì hợp chữ
     * nhưng làm nền cho chữ trắng thì không đọc nổi.</p>
     */
    public static void toNut(JButton b, Color nen) {
        Color c = nenNut(nen);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(UIManager.getBorder("Button.border"));
        b.setMargin(new Insets(6, 14, 6, 14));
        String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
        b.putClientProperty("FlatLaf.style", "arc: 12; borderColor: " + hex
                + "; focusedBorderColor: " + hex + "; hoverBorderColor: " + hex);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Màu nền nút cho nền tối, từ màu mà panel truyền vào. */
    public static Color nenNut(Color c) {
        if (c == null) {
            return UiTheme.NUT_TAT;
        }
        if (c.equals(UiTheme.TEXT_MUTED) || c.equals(UiTheme.TEXT_FAINT)) {
            return new Color(0x47, 0x4D, 0x57);
        }
        if (c.equals(UiTheme.OK)) {
            return UiTheme.NUT_XANH_LA;
        }
        if (c.equals(UiTheme.DANGER)) {
            return new Color(0xC6, 0x42, 0x42);
        }
        if (c.equals(UiTheme.ACCENT)) {
            return new Color(0xE3, 0x7B, 0x12);
        }
        if (c.equals(UiTheme.WARN)) {
            return UiTheme.NUT_NAU;
        }
        return c;
    }

    public static JLabel createStyledLabel(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }

    // Load icon từ file ảnh, nếu không có sẽ tự vẽ fallback icon
    public static Icon loadIcon(String path) {
        try {
            URL url = ServerGuiUtils.class.getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            // Ignore error
        }
        // Nếu không tìm thấy file ảnh, sử dụng icon vẽ bằng code
        return createFallbackIcon(path);
    }

    // Vẽ icon bằng Java 2D Graphics (Vector style)
    private static Icon createFallbackIcon(String path) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);

                String p = path.toLowerCase();

                // 1. Dashboard
                if (p.contains("dashboard")) {
                    g2.setColor(new Color(0, 120, 215)); // Blue
                    g2.fill(new RoundRectangle2D.Double(1, 1, 8, 8, 2, 2));
                    g2.fill(new RoundRectangle2D.Double(11, 1, 8, 8, 2, 2));
                    g2.fill(new RoundRectangle2D.Double(1, 11, 8, 8, 2, 2));
                    g2.setColor(new Color(255, 140, 0)); // Orange accent
                    g2.fill(new RoundRectangle2D.Double(11, 11, 8, 8, 2, 2));
                }
                // 2. Account
                else if (p.contains("account")) {
                    g2.setColor(new Color(65, 105, 225)); // Royal Blue
                    g2.fill(new RoundRectangle2D.Double(2, 4, 16, 12, 3, 3)); // ID Card body
                    g2.setColor(new Color(200, 200, 255));
                    g2.fill(new Ellipse2D.Double(4, 6, 6, 6)); // Avatar circle
                    g2.setColor(Color.WHITE);
                    g2.fill(new Rectangle2D.Double(11, 7, 5, 2)); // Text lines
                    g2.fill(new Rectangle2D.Double(11, 10, 4, 2));
                }
                // 3. Player / User
                else if (p.contains("user") || p.contains("player")) {
                    g2.setColor(new Color(40, 167, 69)); // Green
                    g2.fill(new Ellipse2D.Double(6, 2, 8, 8)); // Head
                    g2.fill(new Arc2D.Double(2, 11, 16, 8, 0, 180, Arc2D.CHORD)); // Body
                }
                // 4. Shop
                else if (p.contains("shop")) {
                    g2.setColor(new Color(220, 53, 69)); // Red
                    Path2D cart = new Path2D.Double();
                    cart.moveTo(2, 5); cart.lineTo(18, 5); cart.lineTo(16, 14); cart.lineTo(4, 14); cart.closePath();
                    g2.fill(cart); // Cart body
                    g2.fill(new Ellipse2D.Double(5, 15, 3, 3)); // Wheel 1
                    g2.fill(new Ellipse2D.Double(12, 15, 3, 3)); // Wheel 2
                }
                // 5. Giftcode
                else if (p.contains("gift")) {
                    g2.setColor(new Color(255, 193, 7)); // Yellow box
                    g2.fill(new Rectangle2D.Double(3, 5, 14, 12));
                    g2.setColor(new Color(220, 53, 69)); // Red ribbon
                    g2.fill(new Rectangle2D.Double(8, 5, 4, 12)); // Vertical ribbon
                    g2.fill(new Rectangle2D.Double(2, 9, 16, 4)); // Horizontal ribbon
                }
                // 6. Topup / Reward
                else if (p.contains("topup") || p.contains("reward")) {
                    g2.setColor(new Color(102, 51, 153)); // Purple card
                    g2.fill(new RoundRectangle2D.Double(2, 5, 16, 10, 2, 2));
                    g2.setColor(new Color(255, 215, 0)); // Gold chip
                    g2.fill(new Rectangle2D.Double(4, 8, 3, 4));
                }
                // 7. Event / Calendar
                else if (p.contains("calendar") || p.contains("event")) {
                    g2.setColor(new Color(23, 162, 184)); // Cyan
                    g2.fill(new Rectangle2D.Double(3, 4, 14, 14)); // Calendar body
                    g2.setColor(Color.WHITE);
                    g2.fillRect(3, 8, 14, 10); // White bottom part
                    g2.setColor(new Color(23, 162, 184));
                    g2.fill(new Rectangle2D.Double(6, 2, 2, 4)); // Ring 1
                    g2.fill(new Rectangle2D.Double(12, 2, 2, 4)); // Ring 2
                }
                // 8. Security / Shield / Firewall
                else if (p.contains("shield") || p.contains("security") || p.contains("firewall")) {
                    g2.setColor(new Color(220, 53, 69)); // Red shield
                    Path2D shield = new Path2D.Double();
                    shield.moveTo(10, 1);
                    shield.lineTo(17, 4);
                    shield.lineTo(17, 9);
                    shield.curveTo(17, 16, 10, 19, 10, 19);
                    shield.curveTo(10, 19, 3, 16, 3, 9);
                    shield.lineTo(3, 4);
                    shield.closePath();
                    g2.fill(shield);
                }
                // 9. Boss / Monster
                else if (p.contains("monster") || p.contains("boss")) {
                    g2.setColor(new Color(52, 58, 64)); // Dark Gray
                    g2.fill(new Ellipse2D.Double(2, 2, 16, 16)); // Face
                    g2.setColor(new Color(255, 69, 0)); // Red Eyes
                    g2.fill(new Ellipse2D.Double(5, 7, 3, 3));
                    g2.fill(new Ellipse2D.Double(12, 7, 3, 3));
                }
                // 10. Map Template
                else if (p.contains("map")) {
                    g2.setColor(new Color(40, 167, 69)); // Green Map
                    Path2D map = new Path2D.Double();
                    map.moveTo(2, 5); map.lineTo(7, 2); map.lineTo(13, 5); map.lineTo(18, 2);
                    map.lineTo(18, 15); map.lineTo(13, 18); map.lineTo(7, 15); map.lineTo(2, 18);
                    map.closePath();
                    g2.fill(map);
                    g2.setColor(new Color(255, 255, 255, 80)); 
                    Path2D fold = new Path2D.Double();
                    fold.moveTo(7, 2); fold.lineTo(13, 5); fold.lineTo(13, 18); fold.lineTo(7, 15); fold.closePath();
                    g2.fill(fold);
                }
                // 11. Item Template
                else if (p.contains("item")) {
                    g2.setColor(new Color(253, 126, 20)); // Orange Box
                    g2.fill(new Rectangle2D.Double(4, 5, 12, 12)); 
                    g2.setColor(new Color(255, 255, 255, 100));
                    g2.fill(new Rectangle2D.Double(4, 5, 12, 4));
                    g2.setColor(new Color(200, 80, 0));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new Rectangle2D.Double(4, 5, 12, 12));
                }
                // 12. Data Badges (ĐÃ FIX LỖI Ở ĐÂY)
                else if (p.contains("badge")) {
                    g2.setColor(new Color(255, 193, 7)); // Gold color
                    Path2D star = new Path2D.Double();
                    double cx = 10, cy = 10, outerRadius = 9, innerRadius = 4;
                    int numRays = 5;
                    double startAngle = -Math.PI / 2;
                    double delta = Math.PI / numRays;
                    
                    for (int i = 0; i < numRays * 2; i++) {
                        double angle = startAngle + i * delta;
                        double r = (i % 2 == 0) ? outerRadius : innerRadius;
                        double px = cx + Math.cos(angle) * r;
                        double py = cy + Math.sin(angle) * r;
                        
                        // FIX: Kiểm tra nếu là điểm đầu tiên thì dùng moveTo, ngược lại dùng lineTo
                        if (i == 0) {
                            star.moveTo(px, py);
                        } else {
                            star.lineTo(px, py);
                        }
                    }
                    star.closePath();
                    g2.fill(star);
                    g2.setColor(new Color(200, 150, 0));
                    g2.draw(star);
                }
                // 13. Radar
                else if (p.contains("radar")) {
                    g2.setColor(new Color(46, 204, 113)); // Emerald Green
                    g2.setStroke(new BasicStroke(1.5f)); // Viền đậm hơn chút
                    g2.draw(new Ellipse2D.Double(3, 3, 14, 14)); // Vòng tròn ngoài

                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new Ellipse2D.Double(7, 7, 6, 6));   // Vòng tròn trong

                    // Vẽ đường quét (Chữ thập)
                    g2.drawLine(10, 3, 10, 17); // Dọc
                    g2.drawLine(3, 10, 17, 10); // Ngang
                    
                    // Vẽ chấm đỏ (Mục tiêu)
                    g2.setColor(new Color(231, 76, 60)); // Red
                    g2.fill(new Ellipse2D.Double(12, 5, 3, 3));
                }
                
                // Default fallback
                else {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fill(new Ellipse2D.Double(5, 5, 10, 10));
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 20; }

            @Override
            public int getIconHeight() { return 20; }
        };
    }
}