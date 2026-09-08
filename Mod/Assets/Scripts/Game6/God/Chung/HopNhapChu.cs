// Khong "using System": lop Math cua engine trung ten voi System.Math.

namespace Game6.God
{
    /// <summary>
    /// Ô nhập chữ dùng chung, <b>có gõ tiếng Việt</b>.
    /// </summary>
    /// <remarks>
    /// <para><b>Vì sao không dùng ô nhập của game.</b> <c>TField.keyPressed</c>
    /// đã bị bỏ trống từ lâu — cả thân hàm nằm trong khối chú thích, hàm chỉ
    /// còn <c>return false</c>. Việc gõ chữ đi qua bàn phím của Unity, nên
    /// không có chỗ nào chen bộ gõ tiếng Việt vào được mà không sửa vào ruột
    /// một lớp cả game đang dùng.</para>
    ///
    /// <para>Ô này tự đọc <c>Input.inputString</c> mỗi khung hình rồi đưa qua
    /// <see cref="BoGoTiengViet"/>, nên nó nắm trọn đường đi của một ký tự —
    /// không phụ thuộc lớp nào khác.</para>
    ///
    /// <h2>Cách dùng</h2>
    /// <code>
    /// HopNhapChu.getInstance().moRa("Chat", "", 100, s => Service.gI().chat(s));
    /// </code>
    /// <para>Hàm gọi lại chỉ chạy khi bấm OK với chuỗi khác rỗng. Bấm Đóng hay
    /// bấm ra ngoài thì không gọi — chỗ gọi khỏi tự phân biệt "huỷ" với
    /// "gửi chuỗi rỗng".</para>
    ///
    /// <para>Đăng ký trong <c>ClientManager</c> cạnh <see cref="BanPhimSo"/>:
    /// xét chạm đầu tiên, vẽ sau cùng.</para>
    /// </remarks>
    public class HopNhapChu
    {
        private static HopNhapChu instance;

        public static HopNhapChu getInstance()
        {
            return instance ?? (instance = new HopNhapChu());
        }

        public bool dangMo;

        /// <summary>Lúc hộp được mở, theo đồng hồ máy.</summary>
        private long lucMo;

        /// <summary>
        /// Hộp vừa mở xong, chưa tới lượt chốt an toàn đụng vào.
        /// </summary>
        /// <remarks>
        /// Chốt trong <c>ClientManager</c> đóng hộp khi thấy bảng của game
        /// đang mở. Nhưng những chỗ mở hộp đều gọi <c>Panel.hide()</c> ngay
        /// trước đó, mà <c>hide()</c> chỉ bật hoạt ảnh trượt ra: bảng còn
        /// <c>isShow</c> suốt mấy khung hình nữa. Hộp vừa mở liền bị đóng —
        /// bấm "Mua nhiều" không thấy ô điền số lượng nào hiện ra.
        ///
        /// Không dùng cờ <c>isClose</c> của bảng để nhận ra việc đó được: cờ
        /// ấy chỉ sống đúng MỘT khung hình rồi bị chính vòng cập nhật của
        /// bảng xoá đi khi bắt đầu trượt, nên đọc nó gần như luôn thấy false.
        ///
        /// Một giây là thừa cho hoạt ảnh trượt, mà vẫn giữ được ý nghĩa của
        /// chốt: hộp nào kẹt mở thật thì sau một giây vẫn bị đóng.
        /// </remarks>
        public bool vuaMo
        {
            get { return dangMo && mSystem.currentTimeMillis() - lucMo < 1000L; }
        }

        private string tieuDe = "Nhập chữ";
        private string noiDung = "";
        private int toiDa = 100;
        private System.Action<string> khiXong;

        // ------------------------------------------------------------------
        //  Màu — giống bộ màn phụ còn lại
        // ------------------------------------------------------------------
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        private static readonly int MAU_NEN = rgb(0xFC, 0xE3, 0xC0);
        private static readonly int MAU_THE = rgb(0xFD, 0xF0, 0xDC);
        private static readonly int MAU_VANG = rgb(0xFF, 0xC4, 0x4D);

        private const int BO_GOC = 6;

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

        private static void veTamChim(mGraphics g, int x, int y, int w, int h)
        {
            g.setColor(MAU_VANG, 0.85f);
            g.fillRect(x, y, w, h, 5);
            g.setColor(0x000000, 0.42f);
            g.fillRect(x + 1, y + 1, w - 2, h - 2, 4);
        }

        private static void veQuang(mGraphics g, int x, int y, int w, int h,
                int mau, float manh, int lop)
        {
            for (int i = lop; i >= 1; i--)
            {
                g.setColor(mau, manh * (0.30f / lop) * (lop - i + 1));
                g.fillRect(x - i, y - i, w + i * 2, h + i * 2, BO_GOC + i);
            }
        }

        private static void veNenNut(mGraphics g, int[] o, int mau)
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
        public void moRa(string tieuDe, string banDau, int toiDa,
                System.Action<string> khiXong)
        {
            moRa(tieuDe, banDau, toiDa, true, khiXong);
        }

        /// <summary>
        /// Mở ô nhập.
        /// </summary>
        /// <param name="goTiengViet">
        /// Giữ lại cho chỗ gọi khỏi phải sửa, nhưng <b>không còn tác dụng</b>:
        /// gõ được tiếng Việt hay không là việc của bộ gõ trên máy, ô này nhận
        /// đúng ký tự mà hệ điều hành đưa xuống.
        /// </param>
        public void moRa(string tieuDe, string banDau, int toiDa,
                bool goTiengViet, System.Action<string> khiXong)
        {
            NhatKy.ghi("HOP NHAP mo: " + tieuDe);
        this.lucMo = mSystem.currentTimeMillis();
        this.tieuDe = string.IsNullOrEmpty(tieuDe) ? "Nhập chữ" : tieuDe;
            this.noiDung = banDau ?? "";
            this.toiDa = toiDa < 1 ? 100 : toiDa;
            this.khiXong = khiXong;
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
        private const int CAO = 104;

        private int rong()
        {
            int w = GameCanvas.w - 40;
            if (w > 300)
            {
                w = 300;
            }
            if (w < 180)
            {
                w = 180;
            }
            return w;
        }

        private int[] oKhung()
        {
            int w = rong();
            return new int[] {
                (GameCanvas.w - w) / 2, (GameCanvas.h - CAO) / 2, w, CAO };
        }

        private int[] oNut(bool ok)
        {
            int[] h = oKhung();
            int w = 74;
            int khe = 10;
            int x = h[0] + h[2] / 2 - (w * 2 + khe) / 2;
            return new int[] {
                ok ? x : x + w + khe, h[1] + CAO - 32, w, 22 };
        }

        // ------------------------------------------------------------------
        //  Nhận phím
        // ------------------------------------------------------------------

        /// <summary>
        /// Đọc phím vừa gõ. Gọi từ <c>Main.OnGUI</c> với <b>mọi</b> sự kiện.
        /// </summary>
        /// <remarks>
        /// <para><b>Vì sao không dùng <c>Input.inputString</c>.</b> Nó chỉ có giá
        /// trị trong <c>Update</c>. Cả phần vẽ của bản mod chạy trong
        /// <c>OnGUI</c> và chỉ ở sự kiện <c>Repaint</c>, nên đọc ở đó gần như
        /// luôn thấy chuỗi rỗng — gõ gì cũng không vào. Đó đúng là lỗi "không
        /// nhập được chữ hay số".</para>
        ///
        /// <para><b>Cách đúng cho IMGUI là <c>Event.current</c>:</b> mỗi phím gõ
        /// đến dưới dạng một sự kiện <c>KeyDown</c> riêng, và
        /// <c>Event.current.character</c> là ký tự <b>đã qua bộ gõ của hệ điều
        /// hành</b>. Nhờ vậy tiếng Việt cứ gõ bằng Unikey / EVKey / bộ gõ sẵn
        /// của Windows như mọi ứng dụng khác — game không tự đoán Telex, nên
        /// không có chuyện hai lớp cùng biến đổi một ký tự rồi ra chữ sai.</para>
        ///
        /// <para>Chữ hoa, chữ thường và dấu cách đều vào được vì đây là ký tự
        /// thật của bàn phím, không phải mã phím.</para>
        ///
        /// <para>Phải gọi ở <b>mọi</b> sự kiện chứ không chỉ <c>Repaint</c>:
        /// một sự kiện chỉ mang đúng một phím, bỏ sót sự kiện là mất phím.</para>
        /// </remarks>
        public void docPhim()
        {
            if (!dangMo)
            {
                return;
            }
            UnityEngine.Event e = UnityEngine.Event.current;
            if (e == null || e.type != UnityEngine.EventType.KeyDown)
            {
                return;
            }

            if (e.keyCode == UnityEngine.KeyCode.Backspace)
            {
                if (noiDung.Length > 0)
                {
                    noiDung = noiDung.Substring(0, noiDung.Length - 1);
                }
                e.Use();
                return;
            }
            if (e.keyCode == UnityEngine.KeyCode.Return
                    || e.keyCode == UnityEngine.KeyCode.KeypadEnter)
            {
                // NUOT su kien truoc khi lam gi.
                //
                // Khong nuot thi phim Enter con nguyen trong hang doi va phan
                // xu ly phim cua game doc lai no ngay khung hinh do — mo lai
                // dung cai vua dong, hoac kich hoat them mot lan nua. Do la
                // canh "Enter bi double click".
                e.Use();
                xong();
                return;
            }
            if (e.keyCode == UnityEngine.KeyCode.Escape)
            {
                dong();
                e.Use();
                return;
            }

            char c = e.character;
            // Ky tu dieu khien (Enter, Tab, Backspace...) den kem keyCode o tren
            // roi; o day chi nhan chu that.
            if (c < ' ' || c == 127)
            {
                return;
            }
            if (noiDung.Length < toiDa)
            {
                noiDung += c;
            }
            e.Use();
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

            // O chu, kem con tro nhay.
            veTamChim(g, h[0] + 10, h[1] + 24, h[2] - 20, 24);
            string hien = catVua(noiDung, h[2] - 30);
            bool nhay = (mSystem.currentTimeMillis() / 500) % 2 == 0;
            mFont.tahoma_7b_yellow.drawString(g, hien + (nhay ? "|" : ""),
                    h[0] + 16, h[1] + 30, mFont.LEFT);

            int[] ok = oNut(true);
            int[] dongNut = oNut(false);
            veNenNut(g, ok, MAU_VANG);
            veNenNut(g, dongNut, MAU_THE);
            mFont.tahoma_7b_dark.drawString(g, "OK",
                    ok[0] + ok[2] / 2, ok[1] + 5, mFont.CENTER);
            mFont.tahoma_7b_dark.drawString(g, "Đóng",
                    dongNut[0] + dongNut[2] / 2, dongNut[1] + 5, mFont.CENTER);
        }

        /// <summary>Cắt bớt đầu chuỗi cho vừa ô, giữ lại phần đang gõ ở cuối.</summary>
        private static string catVua(string s, int rongToiDa)
        {
            if (s == null)
            {
                return "";
            }
            while (s.Length > 0
                    && mFont.tahoma_7b_yellow.getWidth(s + "|") > rongToiDa)
            {
                s = s.Substring(1);
            }
            return s;
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
                return true;
            }
            int[] ok = oNut(true);
            if (cham(ok[0], ok[1], ok[2], ok[3]))
            {
                xong();
                return true;
            }
            int[] d = oNut(false);
            if (cham(d[0], d[1], d[2], d[3]))
            {
                dong();
                return true;
            }
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        private void xong()
        {
            string s = noiDung == null ? "" : noiDung.Trim();
            System.Action<string> ham = khiXong;
            // Dong TRUOC khi goi lai: ham goi lai co the mo ngay mot bang khac,
            // dong sau thi cai vua mo bi tat theo.
            dong();
            if (s.Length > 0 && ham != null)
            {
                ham(s);
            }
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
