namespace Game2.God
{
    /// <summary>
    /// Bảng chỉ số nhỏ của <b>mục tiêu đang chọn</b> — người chơi hoặc boss.
    /// </summary>
    /// <remarks>
    /// <para>Một nút tròn nhỏ gắn ở mép phải khung mục tiêu (tên + thanh máu ở
    /// giữa cạnh trên). Bấm là mở bảng ngay dưới khung, bấm lần nữa là thu gọn;
    /// trạng thái mở/thu giữ nguyên khi đổi mục tiêu.</para>
    ///
    /// <para>Số liệu do máy chủ tính (gói 116, dùng chung với bảng thành viên):
    /// client không tự biết né, chính xác, giáp của người khác. Đang mở thì mỗi
    /// giây hỏi lại một lần để số theo kịp lúc mục tiêu đổi đồ, hóa khỉ…</para>
    /// </remarks>
    public static class ChiSoMucTieuUI
    {
        /// <summary>Đang mở bảng.</summary>
        public static bool moRong;

        /// <summary>Khung mục tiêu vừa vẽ có phải của người chơi / boss không.</summary>
        public static bool coKhungNguoi;

        // Khung mục tiêu vừa vẽ — GameScr.veBangMuc ghi lại.
        private static int xKhung, yKhung, wKhung, hKhung;

        private static int idDangXem = int.MinValue;
        private static long[] cs;
        private static bool daNhan;
        private static long mocXin;

        private static int[] oNut = new int[0];

        private const int CO_NUT = 13;
        private const int RONG_BANG = 236;
        private const int CAO_DONG = 11;

        private static readonly string[] NHAN = {
            "HP", "Sức đánh", "Chí mạng", "ST chí mạng", "Né đòn", "Chính xác",
            "Giáp", "Hút HP"
        };

        /// <summary>GameScr.veBangMuc báo khung vừa vẽ.</summary>
        public static void datKhung(int x, int y, int w, int h)
        {
            xKhung = x;
            yKhung = y;
            wKhung = w;
            hKhung = h;
        }

        /// <summary>Máy chủ trả chỉ số (gói 116).</summary>
        public static void nhanChiSo(int id, long[] boCs)
        {
            if (id == idDangXem)
            {
                cs = boCs;
                daNhan = true;
            }
        }

        /// <summary>Máy chủ báo không có chỉ số (mục tiêu đã rời map…).</summary>
        public static void khongCo(int id)
        {
            if (id == idDangXem)
            {
                cs = null;
                daNhan = true;
            }
        }

        private static Char mucTieu()
        {
            Char toi = Char.myCharz();
            if (toi == null || toi.charFocus == null || toi.charFocus.isPet
                    || toi.mobFocus != null || toi.npcFocus != null)
            {
                return null;
            }
            return toi.charFocus;
        }

        public static void ve(mGraphics g)
        {
            Char f = mucTieu();
            if (f == null || !coKhungNguoi || wKhung <= 0)
            {
                oNut = new int[0];
                return;
            }
            if (f.charID != idDangXem)
            {
                idDangXem = f.charID;
                cs = null;
                daNhan = false;
                mocXin = 0L;
            }

            // ---- nut tron o mep phai khung
            int xn = xKhung + wKhung - CO_NUT / 2;
            int yn = yKhung + hKhung - CO_NUT / 2 - 1;
            oNut = new int[] { xn - 4, yn - 4, CO_NUT + 8, CO_NUT + 8 };
            g.setColor(0x000000, 0.35f);
            g.fillRect(xn + 1, yn + 1, CO_NUT, CO_NUT, CO_NUT / 2);
            g.setColor(0xFFC44D, 1f);
            g.fillRect(xn, yn, CO_NUT, CO_NUT, CO_NUT / 2);
            g.setColor(moRong ? 0x7A3A12 : 0x3A2410, 1f);
            g.fillRect(xn + 1, yn + 1, CO_NUT - 2, CO_NUT - 2, CO_NUT / 2 - 1);
            // Mui ten: xuong khi dang thu, len khi dang mo.
            g.setColor(0xFFE9A8, 1f);
            int tx = xn + CO_NUT / 2;
            int ty = yn + CO_NUT / 2;
            for (int i = 0; i < 4; i++)
            {
                int w = 7 - i * 2;
                int yy = moRong ? ty + 1 - i : ty - 2 + i;
                g.fillRect(tx - w / 2, yy, w, 1);
            }

            if (!moRong)
            {
                return;
            }

            long bayGio = mSystem.currentTimeMillis();
            if (bayGio - mocXin > 1000L)
            {
                mocXin = bayGio;
                Service.gI().xinChiSoNhanVat(idDangXem);
            }

            // ---- bang nho duoi khung
            // Hai cot, moi cot mot nua so dong: trai HP/SD/chi mang/ST chi mang,
            // phai ne/chinh xac/giap/hut HP.
            int soDong = (NHAN.Length + 1) / 2;
            int cao = 6 + soDong * CAO_DONG + 4;
            int x = xKhung + wKhung / 2 - RONG_BANG / 2;
            if (x < 2)
            {
                x = 2;
            }
            if (x + RONG_BANG > GameCanvas.w - 2)
            {
                x = GameCanvas.w - 2 - RONG_BANG;
            }
            int y = yKhung + hKhung + 5;
            g.setColor(0x000000, 0.3f);
            g.fillRect(x + 1, y + 2, RONG_BANG, cao, 6);
            g.setColor(0xC8913A, 0.95f);
            g.fillRect(x, y, RONG_BANG, cao, 6);
            g.setColor(0x24160C, 0.9f);
            g.fillRect(x + 1, y + 1, RONG_BANG - 2, cao - 2, 5);

            if (!daNhan || cs == null)
            {
                mFont.tahoma_7_grey.drawString(g, daNhan ? "Không xem được chỉ số" : "Đang tải…",
                        x + RONG_BANG / 2, y + cao / 2 - 5, mFont.CENTER);
                return;
            }
            string[] giaTri = {
                NinjaUtil.getMoneys(cs[2]),
                NinjaUtil.getMoneys(cs[4]),
                cs[6] < 0 ? "-" : cs[6] + "%",
                cs[7] < 0 ? "-" : cs[7] + "%",
                cs[8] < 0 ? "-" : cs[8] + "%",
                cs[9] < 0 ? "-" : cs[9] + "%",
                cs[5] < 0 ? "-" : NinjaUtil.getMoneys(cs[5]),
                cs[10] < 0 ? "-" : cs[10] + "%"
            };
            int rongCot = (RONG_BANG - 6) / 2;
            for (int hang = 0; hang < soDong; hang++)
            {
                int yd = y + 4 + hang * CAO_DONG;
                if (hang % 2 == 1)
                {
                    g.setColor(0xFFFFFF, 0.05f);
                    g.fillRect(x + 3, yd, RONG_BANG - 6, CAO_DONG, 2);
                }
            }
            // Vach chia hai cot.
            g.setColor(0xC8913A, 0.5f);
            g.fillRect(x + 3 + rongCot, y + 5, 1, cao - 10);
            for (int i = 0; i < NHAN.Length; i++)
            {
                int cot = i / soDong;
                int hang = i % soDong;
                int xc = x + 3 + cot * rongCot;
                int yd = y + 4 + hang * CAO_DONG;
                mFont.tahoma_7_white.drawString(g, NHAN[i], xc + 5, yd, mFont.LEFT);
                mFont.tahoma_7b_yellow.drawString(g, giaTri[i], xc + rongCot - 5, yd,
                        mFont.RIGHT);
            }
        }

        /// <summary>Bắt chạm nút tròn. Trả <c>true</c> khi đã nuốt cú chạm.</summary>
        public static bool capNhatCham()
        {
            if (oNut.Length != 4 || mucTieu() == null)
            {
                return false;
            }
            if (GameCanvas.isPointerMove
                    || !GameCanvas.isPointerHoldIn(oNut[0], oNut[1], oNut[2], oNut[3]))
            {
                return false;
            }
            if (GameCanvas.isPointerJustRelease)
            {
                GameCanvas.clearAllPointerEvent();
                moRong = !moRong;
                mocXin = 0L;
                SoundMn.gI().buttonClick();
            }
            return true;
        }
    }
}
