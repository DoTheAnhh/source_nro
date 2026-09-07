package nro.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class PinkHeartsPanel extends JPanel implements ActionListener {

    private final java.util.List<Heart> hearts = new ArrayList<>();
    private final Timer timer;

    public PinkHeartsPanel() {
        setBackground(new Color(255, 105, 180)); // Nền hồng neon đậm
        // Tạo trái tim ngẫu nhiên
        for (int i = 0; i < 20; i++) {
            hearts.add(new Heart(
                (int)(Math.random() * 800),
                (int)(Math.random() * 500),
                20 + (int)(Math.random() * 20),
                1 + (int)(Math.random() * 3),
                1 + (int)(Math.random() * 3),
                new Color(255, 20, 147, 150) // hồng trong suốt
            ));
        }
        timer = new Timer(50, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Heart heart : hearts) {
            g.setColor(heart.color);
            drawHeart(g, heart.x, heart.y, heart.size);
        }
    }

    private void drawHeart(Graphics g, int x, int y, int size) {
        int[] xs = {x, x + size / 2, x + size, x + size / 2};
        int[] ys = {y + size / 2, y, y + size / 2, y + size};
        g.fillOval(x, y, size / 2, size / 2);
        g.fillOval(x + size / 2, y, size / 2, size / 2);
        g.fillPolygon(xs, ys, 4);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Heart heart : hearts) {
            heart.x += heart.dx;
            heart.y += heart.dy;

            if (heart.x < 0 || heart.x > getWidth() - heart.size) heart.dx = -heart.dx;
            if (heart.y < 0 || heart.y > getHeight() - heart.size) heart.dy = -heart.dy;
        }
        repaint();
    }

    private static class Heart {
        int x, y, size, dx, dy;
        Color color;

        Heart(int x, int y, int size, int dx, int dy, Color color) {
            this.x = x; this.y = y; this.size = size;
            this.dx = dx; this.dy = dy; this.color = color;
        }
    }
}