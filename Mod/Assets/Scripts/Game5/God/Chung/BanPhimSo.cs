// Khong "using System": lop Math cua engine trung ten voi System.Math.

namespace Game5.God
{
    /// <summary>
    /// Bàn phím số dùng chung — gọi ở đâu cũng được.
    /// </summary>
    /// <remarks>
    /// <para><b>Vì sao không dùng <c>GameCanvas.inputDlg</c> của game.</b> Hộp
    /// nhập ấy hiện lên được nhưng <b>không bấm được gì</b> khi có màn phụ của
    /// mod đang mở: <c>GameScr.updateKey</c> thấy <c>coManPhuDangMo()</c> trả về
    /// true là gọi <c>UpdateTouch</c> của màn phụ rồi <c>return</c> ngay — hộp
    /// thoại không bao giờ được hỏi tới, mà màn phụ lại nuốt sạch chạm.</para>
    ///
    /// <para>Có thể thêm ngoại lệ vào <c>coManPhuDangMo()</c>, nhưng đó là sửa
    /// một hàm dùng chung cho sáu màn phụ chỉ để một chỗ dùng được. Tự vẽ thì
    /// không động đến ai — và <c>TuiUI</c> đã làm đúng vậy từ trước với hộp số
    /// lượng của nó, nên đây cũng là lối quen của mã nguồn này.</para>
    ///
    /// <h2>Cách dùng</h2>
    /// <code>
    /// BanPhimSo.getInstance().moRa(soDangCo, n => lamGiDoVoi(n));
    /// BanPhimSo.getInstance().moRa("Nhập số thỏi", soDangCo, n => ...);
    /// </code>
    /// <para>Hàm gọi lại chỉ chạy khi người chơi bấm OK với một số dương. Bấm ra
    /// ngoài hay để trống rồi OK thì bàn phím đóng và <b>không</b> gọi lại — chỗ
    /// gọi khỏi phải tự phân biệt "huỷ" với "nhập 0".</para>
    ///
    /// <para>Đăng ký trong <c>ClientManager</c> như mọi màn phụ khác, nhưng
    /// <b>xét chạm đầu tiên và vẽ sau cùng</b>: nó là hộp chặn, phải nằm trên và
    /// ăn chạm trước tất cả — kể cả trước màn đã gọi ra nó.</para>
    /// </remarks>
    public class BanPhimSo
    {
        private static BanPhimSo instance;

        public static BanPhimSo getInstance()
        {
            return instance ?? (instance = new BanPhimSo());
        }

        public bool dangMo;

        /// <summary>Tiêu đề mặc định khi chỗ gọi không nói gì khác.</summary>
        public const string TIEU_DE_MAC_DINH = "Nhập số lượng";

        private string tieuDe = TIEU_DE_MAC_DINH;
        private string soDangGo = "";
        private long toiDa;
        private System.Action<long> khiXong;

        // ------------------------------------------------------------------
        //  Màu — giống bộ màn phụ còn lại
        // ------------------------------------------------------------------
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        private static readonly int MAU_NEN = rgb(0xFC, 0xE3, 0xC0);
        private static readonly int MAU_THE = rgb(0xFD, 0xF0, 0xDC);
        private static readonly int MAU_VIEN = rgb(0xA8, 0x6E, 0x3C);
        private static readonly int MAU_VANG = rgb(0xFF, 0xC4, 0x4D);

        private const int BO_GOC = 6;

        /// <summary>Khung viền kép: vòng vàng ngoài, vòng tối trong, rồi thân.</summary>
        private static void veKhungKep(mGraphics g, int x, int y, int w, int h,
                int mauThan)
        {
            g.setColor(MAU_VANG, 1f);
            g.fillRect(x, y, w, h, BO_GOC + 2);
            g.setColor(0x1A1108, 0.85f);
            g.fillRect(x + 2, y + 2, w - 4, h - 4, BO_GOC + 1);
            g.setColor(mauThan, 1f);
            g.fillRect(x + 3, y + 3, w - 6, h - 6, BO_GOC);
        }

        /// <summary>Tấm nền chìm có vòng vàng mảnh bao quanh.</summary>
        private static void veTamChim(mGraphics g, int x, int y, int w, int h)
        {
            g.setColor(MAU_VANG, 0.85f);
            g.fillRect(x, y, w, h, 5);
            g.setColor(0x000000, 0.42f);
            g.fillRect(x + 1, y + 1, w - 2, h - 2, 4);
        }

        /// <summary>Quầng sáng quanh khung: mấy viền đồng tâm mờ dần.</summary>
        private static void veQuang(mGraphics g, int x, int y, int w, int h,
                int mau, float manh, int lop)
        {
            for (int i = lop; i >= 1; i--)
            {
                g.setColor(mau, manh * (0.30f / lop) * (lop - i + 1));
                g.fillRect(x - i, y - i, w + i * 2, h + i * 2, BO_GOC + i);
            }
        }

        private static void veNenPhim(mGraphics g, int[] o, int mau)
        {
            g.setColor(0x000000, 0.35f);
            g.fillRect(o[0] + 1, o[1] + 2, o[2], o[3], 5);
            g.setColor(0x6B4A18, 1f);
            g.fillRect(o[0], o[1], o[2], o[3], 5);
            g.setColor(mau, 1f);
            g.fillRect(o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, 4);
        }

        // ------------------------------------------------------------------
        //  Mở / đóng
        // ------------------------------------------------------------------

        /// <summary>Mở bàn phím với tiêu đề mặc định.</summary>
        /// <param name="toiDa">Trần cho phép; <c>0</c> là không giới hạn.</param>
        public void moRa(long toiDa, System.Action<long> khiXong)
        {
            moRa(TIEU_DE_MAC_DINH, toiDa, khiXong);
        }

        public void moRa(string tieuDe, long toiDa, System.Action<long> khiXong)
        {
            this.tieuDe = string.IsNullOrEmpty(tieuDe) ? TIEU_DE_MAC_DINH : tieuDe;
            this.toiDa = toiDa;
            this.khiXong = khiXong;
            soDangGo = "";
            dangMo = true;
            GameCanvas.clearAllPointerEvent();
        }

        public void dong()
        {
            dangMo = false;
            // Bo ham goi lai: giu lai thi lan mo sau ma cho goi quen truyen ham
            // moi se chay nham ham cua lan truoc.
            khiXong = null;
        }

        // ------------------------------------------------------------------
        //  Bố cục
        // ------------------------------------------------------------------
        private const int RONG = 172;
        private const int CAO = 196;

        private int[] oKhung()
        {
            return new int[] {
                (GameCanvas.w - RONG) / 2,
                (GameCanvas.h - CAO) / 2,
                RONG, CAO };
        }

        /// <summary>
        /// Ô phím thứ <paramref name="i"/> trên lưới 3 cột.
        /// </summary>
        /// <remarks>
        /// Thứ tự 1..9, rồi Xoá, 0, OK — xếp như bàn phím điện thoại (số 1 ở
        /// trên cùng bên trái), không như bàn phím máy tính.
        /// </remarks>
        private int[] oPhim(int i)
        {
            int[] h = oKhung();
            const int cot = 3;
            const int khe = 5;
            const int le = 10;
            const int cao = 26;
            int w = (h[2] - le * 2 - khe * (cot - 1)) / cot;
            return new int[] {
                h[0] + le + (i % cot) * (w + khe),
                h[1] + 62 + (i / cot) * (cao + khe),
                w, cao };
        }

        private static string nhan(int i)
        {
            if (i < 9)
            {
                return "" + (i + 1);
            }
            if (i == 9)
            {
                return "Xoá";
            }
            return i == 10 ? "0" : "OK";
        }

        // ------------------------------------------------------------------
        //  Vẽ
        // ------------------------------------------------------------------
        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            int[] h = oKhung();

            g.setColor(0, 0.55f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veQuang(g, h[0], h[1], h[2], h[3], MAU_VANG, 0.7f, 3);
            veKhungKep(g, h[0], h[1], h[2], h[3], MAU_NEN);

            mFont.tahoma_7b_dark.drawString(g, tieuDe,
                    h[0] + h[2] / 2, h[1] + 8, mFont.CENTER);

            // O hien so dang go.
            veTamChim(g, h[0] + 10, h[1] + 24, h[2] - 20, 22);
            mFont.tahoma_7b_yellow.drawString(g,
                    soDangGo.Length == 0 ? "0" : soDangGo,
                    h[0] + h[2] / 2, h[1] + 29, mFont.CENTER);

            if (toiDa > 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Tối đa " + toiDa,
                        h[0] + h[2] / 2, h[1] + 48, mFont.CENTER);
            }

            for (int i = 0; i < 12; i++)
            {
                int[] o = oPhim(i);
                veNenPhim(g, o, i == 11 ? MAU_VANG : MAU_THE);
                mFont.tahoma_7b_dark.drawString(g, nhan(i),
                        o[0] + o[2] / 2, o[1] + 7, mFont.CENTER);
            }
        }

        // ------------------------------------------------------------------
        //  Chạm
        // ------------------------------------------------------------------
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            if (!GameCanvas.isPointerJustRelease)
            {
                // Van nuot cham khi dang giu ngon, khong de no lot xuong duoi.
                return true;
            }

            for (int i = 0; i < 12; i++)
            {
                int[] o = oPhim(i);
                if (!cham(o[0], o[1], o[2], o[3]))
                {
                    continue;
                }
                if (i < 9)
                {
                    themChuSo((char) ('1' + i));
                }
                else if (i == 9)
                {
                    if (soDangGo.Length > 0)
                    {
                        soDangGo = soDangGo.Substring(0, soDangGo.Length - 1);
                    }
                }
                else if (i == 10)
                {
                    themChuSo('0');
                }
                else
                {
                    xong();
                }
                return true;
            }

            // Bam ra ngoai la huy: dong lai va KHONG goi ham goi lai.
            dong();
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        private void xong()
        {
            long n;
            System.Action<long> ham = khiXong;
            bool hopLe = long.TryParse(soDangGo, out n) && n > 0;
            if (hopLe && toiDa > 0 && n > toiDa)
            {
                n = toiDa;
            }
            // Dong TRUOC khi goi lai: ham goi lai co the mo ngay mot bang khac,
            // dong sau thi cai vua mo bi tat theo.
            dong();
            if (hopLe && ham != null)
            {
                ham(n);
            }
        }

        /// <summary>
        /// Thêm một chữ số vào chuỗi đang gõ.
        /// </summary>
        /// <remarks>
        /// Chặn ở 12 chữ số: dài hơn là tràn kiểu <c>long</c> lúc đọc ra. Bỏ số 0
        /// đứng đầu cho khỏi thành "007".
        /// </remarks>
        private void themChuSo(char c)
        {
            if (soDangGo.Length >= 12)
            {
                return;
            }
            if (soDangGo.Length == 0 && c == '0')
            {
                return;
            }
            soDangGo += c;
        }

        private static bool cham(int x, int y, int w, int h)
        {
            if (GameCanvas.isPointerHoldIn(x, y, w, h))
            {
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            return false;
        }
    }
}
