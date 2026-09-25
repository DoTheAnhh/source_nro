package nro.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * {@link FlowLayout} biết <b>xuống hàng</b> khi hẹp.
 *
 * <p>FlowLayout thường vẫn xếp tràn sang hàng hai, nhưng lại báo chiều cao ưa
 * thích bằng đúng một hàng — cha nó (BorderLayout...) cắt mất phần dưới, nên
 * các nút cuối hàng biến mất khi cửa sổ hẹp. Lớp này tính chiều cao theo số
 * hàng thật ở bề rộng hiện tại.</p>
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return tinh(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension d = tinh(target, false);
        d.width -= getHgap() + 1;
        return d;
    }

    private Dimension tinh(Container target, boolean uaThich) {
        synchronized (target.getTreeLock()) {
            int rongCha = target.getSize().width;
            Container c = target;
            while (c.getSize().width == 0 && c.getParent() != null) {
                c = c.getParent();
            }
            rongCha = c.getSize().width;
            if (rongCha == 0) {
                rongCha = Integer.MAX_VALUE;
            }
            int hgap = getHgap();
            int vgap = getVgap();
            Insets in = target.getInsets();
            int le = in.left + in.right + hgap * 2;
            int rongToiDa = rongCha - le;

            Dimension ket = new Dimension(0, 0);
            int rongHang = 0;
            int caoHang = 0;
            int n = target.getComponentCount();
            for (int i = 0; i < n; i++) {
                Component m = target.getComponent(i);
                if (!m.isVisible()) {
                    continue;
                }
                Dimension d = uaThich ? m.getPreferredSize() : m.getMinimumSize();
                if (rongHang + d.width > rongToiDa) {
                    themHang(ket, rongHang, caoHang);
                    rongHang = 0;
                    caoHang = 0;
                }
                if (rongHang != 0) {
                    rongHang += hgap;
                }
                rongHang += d.width;
                caoHang = Math.max(caoHang, d.height);
            }
            themHang(ket, rongHang, caoHang);
            ket.width += le;
            ket.height += in.top + in.bottom + vgap * 2;
            // Nam trong JScrollPane thi chua cho mot chut de khong nhay thanh cuon.
            Container sp = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (sp != null && target.isValid()) {
                ket.width -= hgap + 1;
            }
            return ket;
        }
    }

    private void themHang(Dimension ket, int rongHang, int caoHang) {
        ket.width = Math.max(ket.width, rongHang);
        if (ket.height > 0) {
            ket.height += getVgap();
        }
        ket.height += caoHang;
    }
}
