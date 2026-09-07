package nro.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorfulUI extends JFrame implements ActionListener {

    private final List<Heart> hearts = new ArrayList<>();
    private final Random random = new Random();
    private final Timer timer;

    public ColorfulUI() {
        setTitle("🌸 Giao diện Tím Lấp Lánh Có Trái Tim");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        // Panel chính, nền tím pastel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawHearts(g);
            }
        };
        mainPanel.setBackground(new Color(216, 191, 216)); // màu tím pastel nhẹ (thistle)
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // Tiêu đề
        JLabel titleLabel = new JLabel("💜 Hệ thống Quản lý", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(128, 0, 128)); // tím đậm
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Bảng dữ liệu mẫu
        String[] columns = {"ID", "Tên", "Trạng thái"};
        Object[][] data = {
                {1, "Người chơi 1", "Online"},
                {2, "Người chơi 2", "Offline"},
                {3, "Người chơi 3", "Online"}
        };
        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(Color.BLACK);
        table.setBackground(new Color(230, 230, 250)); // lavender nhẹ
        table.setSelectionBackground(new Color(138, 43, 226)); // blueviolet
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(28);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(138, 43, 226), 2));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(new Color(216, 191, 216));

        buttonPanel.add(createPurpleButton("➕ Thêm"));
        buttonPanel.add(createPurpleButton("✏️ Sửa"));
        buttonPanel.add(createPurpleButton("🗑️ Xóa"));

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Timer hiệu ứng trái tim
        timer = new Timer(50, this);
        timer.start();
    }

    private JButton createPurpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(128, 0, 128));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Vẽ trái tim với hiệu ứng lấp lánh alpha
    private void drawHeart(Graphics2D g2, int x, int y, int size, Color color, float alpha) {
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(ac);
        int s = size;
        g2.setColor(color);
        g2.fillOval(x, y, s, s);
        g2.fillOval(x + s / 2, y, s, s);
        int[] xPoints = {x, x + s, x + s * 2};
        int[] yPoints = {y + s / 2, y + s * 2, y + s / 2};
        g2.fillPolygon(xPoints, yPoints, 3);
        // reset alpha
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // Vẽ tất cả trái tim với alpha thay đổi để lấp lánh
    private void drawHearts(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Heart h : hearts) {
            // alpha lấp lánh dao động từ 0.5f đến 1.0f
            float alpha = 0.5f + 0.5f * (float)Math.sin(h.alphaPhase);
            drawHeart(g2, h.x, h.y, h.size, h.color, alpha);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Thỉnh thoảng thêm trái tim mới ở dưới đáy panel
        if (random.nextInt(5) == 0) {
            Heart h = new Heart();
            h.x = random.nextInt(getWidth());
            h.y = getHeight();
            h.size = 10 + random.nextInt(15);
            // màu tím với sắc độ random
            int r = 128 + random.nextInt(128);
            int g = 0;
            int b = 128 + random.nextInt(128);
            h.color = new Color(r, g, b);
            h.speed = 1 + random.nextInt(3);
            h.alphaPhase = random.nextDouble() * Math.PI * 2; // random pha để nhấp nháy không đồng bộ
            hearts.add(h);
        }

        // Di chuyển trái tim lên trên và cập nhật pha alpha
        for (int i = 0; i < hearts.size(); i++) {
            Heart h = hearts.get(i);
            h.y -= h.speed;
            h.alphaPhase += 0.1; // tăng pha để alpha thay đổi
            if (h.y < -20) hearts.remove(i--);
        }

        repaint();
    }

    private static class Heart {
        int x, y, size, speed;
        Color color;
        double alphaPhase; // biến để điều khiển alpha lấp lánh
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ColorfulUI ui = new ColorfulUI();
            ui.setVisible(true);
        });
    }
}
