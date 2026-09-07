package nro.server;

/**
 *
 * @author MaiTienDung
 */

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PinkHeartBackground extends JPanel {
    private static class Heart {
        int x, y, size;
        Color color;
        int speed;

        Heart(int x, int y, int size, Color color, int speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.speed = speed;
        }
    }

    private List<Heart> hearts = new ArrayList<>();
    private Random random = new Random();

    public PinkHeartBackground() {
        setOpaque(true);
        setBackground(new Color(255, 182, 193)); // Màu hồng nền

        // Tạo trái tim ngẫu nhiên
        for (int i = 0; i < 20; i++) {
            hearts.add(new Heart(
                    random.nextInt(800),  // X random
                    random.nextInt(600),  // Y random
                    20 + random.nextInt(15), // Kích thước
                    new Color(255, 105, 180), // Màu hồng đậm
                    1 + random.nextInt(3) // Tốc độ rơi
            ));
        }

        // Timer update trái tim bay
        Timer timer = new Timer(30, e -> {
            for (Heart heart : hearts) {
                heart.y += heart.speed;
                if (heart.y > getHeight()) {
                    heart.y = -heart.size;
                    heart.x = random.nextInt(getWidth());
                }
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Vẽ trái tim
        Graphics2D g2 = (Graphics2D) g;
        for (Heart heart : hearts) {
            g2.setColor(heart.color);
            drawHeart(g2, heart.x, heart.y, heart.size);
        }
    }

    private void drawHeart(Graphics2D g2, int x, int y, int size) {
        int half = size / 2;
        g2.fillArc(x, y, half, half, 0, 180);
        g2.fillArc(x + half, y, half, half, 0, 180);
        int[] xPoints = {x, x + size, x + half};
        int[] yPoints = {y + half, y + half, y + size};
        g2.fillPolygon(xPoints, yPoints, 3);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Màn hình hồng với trái tim");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setContentPane(new PinkHeartBackground());
        frame.setVisible(true);
    }
}

