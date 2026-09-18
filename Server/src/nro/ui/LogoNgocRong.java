package nro.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Logo của bảng điều khiển: <b>ngọc rồng bốn sao</b>.
 *
 * <p>Một hàm vẽ cho cả hai chỗ — biểu tượng cửa sổ/thanh tác vụ và dải thương
 * hiệu trên cùng thanh bên — để hai nơi luôn là cùng một hình. Trước đây biểu
 * tượng cửa sổ là một khung biểu đồ xanh dương chẳng liên quan gì tới game,
 * còn thanh bên lại vẽ ngọc rồng.</p>
 *
 * <p>Vẽ bằng mã, không dùng tệp ảnh: phóng to thu nhỏ cỡ nào cũng nét, và
 * không có tệp nào để quên chép theo.</p>
 */
public final class LogoNgocRong {

    private LogoNgocRong() {
    }

    private static final Color SANG = new Color(0xFF, 0xE7, 0x9A);
    private static final Color GIUA = new Color(0xFF, 0x9F, 0x1C);
    private static final Color TOI = new Color(0xD9, 0x48, 0x0F);
    private static final Color SAO = new Color(0xD3, 0x1F, 0x1F);
    private static final Color VIEN_SAO = new Color(0x8A, 0x10, 0x10);

    /**
     * Quả ngọc rồng bốn sao, vừa khít hình tròn đường kính {@code d} tại (x, y).
     *
     * <p>Đổ màu toả tròn lệch lên góc trên trái — vàng nhạt, cam, cam cháy ở
     * mép — nên quả cầu ra khối chứ không phải một đĩa phẳng. Thêm quầng sáng
     * mờ bên ngoài khi {@code quang} bật, hợp với nền tối.</p>
     */
    public static void ve(Graphics2D g0, float x, float y, float d, boolean quang) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        float cx = x + d / 2f;
        float cy = y + d / 2f;

        if (quang) {
            float r = d * 0.78f;
            g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r,
                    new float[]{0.55f, 1f},
                    new Color[]{new Color(0xFF, 0x8C, 0x1A, 90), new Color(0xFF, 0x8C, 0x1A, 0)}));
            g.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        }

        // Than qua cau.
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(x + d * 0.36f, y + d * 0.32f), d * 0.72f,
                new float[]{0f, 0.45f, 1f}, new Color[]{SANG, GIUA, TOI}));
        g.fill(new Ellipse2D.Float(x, y, d, d));

        // Vien toi mong o mep, cho qua cau tach khoi nen.
        g.setColor(new Color(0x7A, 0x24, 0x05, 150));
        g.setStroke(new BasicStroke(Math.max(1f, d * 0.03f)));
        g.draw(new Ellipse2D.Float(x + 0.5f, y + 0.5f, d - 1f, d - 1f));

        // Bon ngoi sao.
        float rs = d * 0.12f;
        float k = d * 0.17f;
        float[][] tam = {{cx - k, cy - k * 0.85f}, {cx + k, cy - k * 0.85f},
            {cx - k * 0.55f, cy + k * 1.05f}, {cx + k * 0.95f, cy + k * 0.75f}};
        for (float[] t : tam) {
            Path2D sao = sao(t[0], t[1], rs);
            g.setColor(SAO);
            g.fill(sao);
            if (d >= 28) {
                g.setColor(VIEN_SAO);
                g.setStroke(new BasicStroke(d * 0.012f));
                g.draw(sao);
            }
        }

        // Vet bong sang goc tren trai.
        g.setPaint(new GradientPaint(x, y + d * 0.1f, new Color(255, 255, 255, 170),
                x, y + d * 0.42f, new Color(255, 255, 255, 0)));
        g.fill(new Ellipse2D.Float(x + d * 0.16f, y + d * 0.09f, d * 0.42f, d * 0.28f));
        g.dispose();
    }

    /** Ngôi sao năm cánh, tâm (cx, cy), bán kính ngoài r. */
    private static Path2D sao(float cx, float cy, float r) {
        Path2D p = new Path2D.Float();
        for (int i = 0; i < 10; i++) {
            double goc = -Math.PI / 2 + i * Math.PI / 5;
            float bk = (i % 2 == 0) ? r : r * 0.45f;
            float px = cx + (float) (Math.cos(goc) * bk);
            float py = cy + (float) (Math.sin(goc) * bk);
            if (i == 0) {
                p.moveTo(px, py);
            } else {
                p.lineTo(px, py);
            }
        }
        p.closePath();
        return p;
    }

    /**
     * Biểu tượng ứng dụng cỡ {@code n}x{@code n}: ô vuông bo góc nền than, viền
     * cam mảnh, ngọc rồng ở giữa.
     */
    public static BufferedImage bieuTuong(int n) {
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        float le = n * 0.04f;
        float o = n - le * 2;
        float bo = n * 0.24f;
        g.setPaint(new GradientPaint(0, le, new Color(0x2A, 0x2E, 0x36),
                0, n - le, new Color(0x0F, 0x11, 0x15)));
        g.fill(new RoundRectangle2D.Float(le, le, o, o, bo, bo));
        if (n >= 24) {
            g.setColor(new Color(0xFF, 0x8C, 0x1A, 150));
            g.setStroke(new BasicStroke(Math.max(1f, n / 48f)));
            float v = n / 96f;
            g.draw(new RoundRectangle2D.Float(le + v, le + v, o - v * 2, o - v * 2, bo, bo));
        }
        // O nho (16, 20 diem anh) bo quang sang va de qua cau to het co:
        // quang mo o co do chi lam hinh nhoe.
        boolean nho = n < 32;
        float d = o * (nho ? 0.86f : 0.66f);
        ve(g, (n - d) / 2f, (n - d) / 2f, d, !nho);
        g.dispose();
        return img;
    }

    /** Đủ các cỡ để Windows chọn cỡ nét nhất cho thanh tiêu đề và thanh tác vụ. */
    public static List<Image> boBieuTuong() {
        List<Image> ds = new ArrayList<>();
        for (int n : new int[]{16, 20, 24, 32, 40, 48, 64, 128, 256}) {
            ds.add(bieuTuong(n));
        }
        return ds;
    }
}
