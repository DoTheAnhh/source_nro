namespace Game3.God
{
    /// <summary>
    /// Khung "Thông tin đệ tử" — cột phải, ngay dưới hàng nút góc trên.
    /// </summary>
    /// <remarks>
    /// <para>Nhỏ gọn: tên và trạng thái, thanh HP, thanh KI, rồi ba hàng chỉ số
    /// hai cột — sức mạnh / tiềm năng, sức đánh / giáp, chí mạng / thể lực. Bề
    /// rộng tự co theo chữ.</para>
    ///
    /// <para>Nút <b>–</b> ở góc thu khung lại thành một chip nhỏ "Đệ tử 45%"; bấm
    /// chip là mở lại. Lựa chọn được nhớ qua các lần mở game. Tắt hẳn thì ở
    /// "Chức năng → Hiển thị" (mã 19).</para>
    ///
    /// <para><b>Không đè nút nào.</b> Mép trên neo vào mép dưới hàng nút góc phải
    /// (<c>TabControll.mepDuoiHangNut</c>), không phải một con số cố định — con
    /// số cố định 160 cũ rơi đúng vào nút Chuyển mục tiêu trên điện thoại. Mép
    /// dưới không được lấn xuống cụm nút dưới (Đấm / Chuyển mục tiêu trên điện
    /// thoại, Capsule / Đậu thần trên máy tính): không đủ chỗ thì tự thu thành
    /// chip. Danh sách người chơi cùng cột đọc <see cref="mepDuoi"/> để xếp
    /// xuống dưới khung.</para>
    ///
    /// <para>Máu đọc thẳng từ nhân vật đệ tử đang đứng trên bản đồ — thứ đó được
    /// cập nhật theo từng đòn đánh nên luôn tươi. Các chỉ số còn lại xin máy chủ
    /// năm giây một lần qua gói -107; gói ấy chỉ cập nhật dữ liệu, không bật bảng
    /// đệ tử lên.</para>
    /// </remarks>
    public class ThongTinDeTu
    {
        private static ThongTinDeTu instance;

        public static ThongTinDeTu getInstance()
        {
            return instance == null ? instance = new ThongTinDeTu() : instance;
        }

        private const string KHOA_LUU = "tt_de_tu";

        private const string KHOA_GON = "tt_de_tu_gon";

        private bool daNap;

        private bool dangHien;

        private bool dangGon;

        /// <summary>Khung có đang hiện không. Đọc và ghi đều đi qua bộ nhớ máy.</summary>
        public bool isShow
        {
            get
            {
                napLuaChon();
                return dangHien;
            }
            set
            {
                napLuaChon();
                dangHien = value;
                luu(KHOA_LUU, value);
            }
        }

        /// <summary>Đang thu thành chip nhỏ.</summary>
        private bool gon
        {
            get
            {
                napLuaChon();
                return dangGon;
            }
            set
            {
                napLuaChon();
                dangGon = value;
                luu(KHOA_GON, value);
            }
        }

        private void napLuaChon()
        {
            if (daNap)
            {
                return;
            }
            daNap = true;
            try
            {
                dangHien = Rms.loadRMSInt(KHOA_LUU) == 1;
                dangGon = Rms.loadRMSInt(KHOA_GON) == 1;
            }
            catch (System.Exception)
            {
                dangHien = false;
                dangGon = false;
            }
        }

        private static void luu(string khoa, bool v)
        {
            try
            {
                Rms.saveRMSInt(khoa, v ? 1 : 0);
            }
            catch (System.Exception)
            {
            }
        }

        private const int LE_PHAI = 4;

        private const int RONG_MIN = 124;

        private const int RONG_MAX = 172;

        /// <summary>Tên, hai thanh, rồi ba hàng chỉ số mỗi hàng 10 điểm.</summary>
        private const int CAO = 68;

        private const int CAO_CHIP = 14;

        /// <summary>Cạnh nút thu nhỏ.</summary>
        private const int O_NUT = 10;

        private const long HOI_DU_LIEU_MS = 5000;

        private long lanHoiCuoi;

        // O da ve o khung hinh vua roi. Phan bat cham va danh sach nguoi choi
        // doc lai dung cac con so nay, khong tu tinh lai mot kieu khac.
        private bool daVe;

        private int xVe;

        private int yVe;

        private int rongVe;

        private int caoVe;

        private int xNut;

        private int yNut;

        private int wNut;

        private int hNut;

        /// <summary>Mép dưới của khung vừa vẽ; 0 nếu khung không hiện.</summary>
        public int mepDuoi()
        {
            return daVe ? yVe + caoVe : 0;
        }

        /// <summary>Mép dưới được phép — trên cụm nút ở nửa dưới cột phải.</summary>
        private static int mepDuoiChoPhep()
        {
            int gioiHan = GameCanvas.h - 40;
            try
            {
                int[] dauThan = GameScr.oNutDauThan();
                if (GameCanvas.isTouch)
                {
                    // Tren dien thoai nut Chuyen muc tieu nam tren nut Dam, ma
                    // cum nut nhanh neo ngang tam nut Dam: chua them mot nut nua.
                    gioiHan = dauThan[1] - 50;
                }
                else
                {
                    gioiHan = dauThan[1] - 6;
                }
            }
            catch (System.Exception)
            {
            }
            return gioiHan;
        }

        public void paint(mGraphics g)
        {
            daVe = false;
            wNut = 0;
            if (!isShow || GameCanvas.currentScreen != GameScr.gI())
            {
                return;
            }
            Char me = Char.myCharz();
            if (me == null)
            {
                return;
            }

            long bayGio = mSystem.currentTimeMillis();
            if (bayGio - lanHoiCuoi > HOI_DU_LIEU_MS)
            {
                lanHoiCuoi = bayGio;
                try
                {
                    Service.gI().petInfo();
                }
                catch (System.Exception)
                {
                }
            }

            Char pet = Char.myPetz();
            if (pet == null || string.IsNullOrEmpty(pet.cName))
            {
                // Chua co de tu, hoac chua nhan duoc goi du lieu dau tien.
                return;
            }

            long hp = (long)pet.cHP;
            long hpFull = (long)pet.cHPFull;
            Char deTrenMap = null;
            try
            {
                deTrenMap = GameScr.findCharInMap(-me.charID);
            }
            catch (System.Exception)
            {
            }
            if (deTrenMap != null && deTrenMap.cHPFull > 0)
            {
                hp = (long)deTrenMap.cHP;
                hpFull = (long)deTrenMap.cHPFull;
            }

            int y = TabControll.mepDuoiHangNut() + 4;
            if (gon || y + CAO > mepDuoiChoPhep())
            {
                veChip(g, y, hp, hpFull);
                return;
            }

            string[] trai =
            {
                "Sức mạnh " + soGon(pet.cPower),
                "Sức đánh " + soGon(pet.cDamFull),
                "Chí mạng " + pet.cCriticalFull + "%"
            };
            string[] phai =
            {
                "Tiềm năng " + soGon(pet.cTiemNang),
                "Giáp " + soGon(pet.cDefull),
                "Thể lực " + soGon(pet.cStamina) + "/" + soGon(pet.cMaxStamina)
            };
            int rong = RONG_MIN;
            for (int i = 0; i < trai.Length; i++)
            {
                int can = mFont.tahoma_7.getWidth(trai[i]) + mFont.tahoma_7.getWidth(phai[i]) + 20;
                if (can > rong)
                {
                    rong = can;
                }
            }
            if (rong > RONG_MAX)
            {
                rong = RONG_MAX;
            }

            int x = GameCanvas.w - LE_PHAI - rong;

            // Nen bo goc vien vang, cung ho mau voi cac chip trang thai cua mod.
            g.setColor(0xC8933C, 0.9f);
            g.fillRect(x, y, rong, CAO, 6);
            g.setColor(0x1E1408, 0.82f);
            g.fillRect(x + 1, y + 1, rong - 2, CAO - 2, 5);

            // Nut thu nho o goc tren phai.
            xNut = x + rong - 4 - O_NUT;
            yNut = y + 3;
            wNut = O_NUT;
            hNut = O_NUT;
            veNut(g, xNut, yNut, "–");

            mFont.tahoma_7b_yellow.drawString(g, catChu(pet.cName, rong - 64), x + 5, y + 2,
                    mFont.LEFT);
            mFont.tahoma_7_grey.drawString(g, tenTrangThai(pet.petStatus), xNut - 3, y + 2,
                    mFont.RIGHT);

            veThanh(g, x + 5, y + 15, rong - 10, 9, hp, hpFull, 0xD83A3A,
                    "HP " + soGon(hp) + " / " + soGon(hpFull));
            veThanh(g, x + 5, y + 26, rong - 10, 9, pet.cMP, pet.cMPFull, 0x2F7BD8,
                    "KI " + soGon(pet.cMP) + " / " + soGon(pet.cMPFull));

            // Ba hang chi so, hai cot: cot trai can trai, cot phai can phai.
            for (int i = 0; i < trai.Length; i++)
            {
                int yHang = y + 37 + i * 10;
                (i == 0 ? mFont.tahoma_7_green : mFont.tahoma_7_yellow)
                        .drawString(g, trai[i], x + 5, yHang, mFont.LEFT);
                mFont.tahoma_7_yellow.drawString(g, phai[i], x + rong - 5, yHang, mFont.RIGHT);
            }

            ghiO(x, y, rong, CAO);
        }

        /// <summary>Khung đã thu: một chip "+ Đệ tử · HP 45%" (máu đệ còn lại), bấm vào là mở.</summary>
        private void veChip(mGraphics g, int y, long hp, long hpFull)
        {
            long phanTram = hpFull > 0 ? hp * 100 / hpFull : 0;
            string chu = "Đệ tử · HP " + phanTram + "%";
            int rong = mFont.tahoma_7b_yellow.getWidth(chu) + O_NUT + 12;
            int x = GameCanvas.w - LE_PHAI - rong;
            g.setColor(0xC8933C, 0.9f);
            g.fillRect(x, y, rong, CAO_CHIP, 5);
            g.setColor(0x1E1408, 0.85f);
            g.fillRect(x + 1, y + 1, rong - 2, CAO_CHIP - 2, 4);
            veNut(g, x + 3, y + 2, "+");
            mFont.tahoma_7b_yellow.drawString(g, chu, x + O_NUT + 7, y + 1, mFont.LEFT);
            // Ca chip la nut: bam dau cung mo.
            xNut = x;
            yNut = y;
            wNut = rong;
            hNut = CAO_CHIP;
            ghiO(x, y, rong, CAO_CHIP);
        }

        private void ghiO(int x, int y, int rong, int cao)
        {
            xVe = x;
            yVe = y;
            rongVe = rong;
            caoVe = cao;
            daVe = true;
        }

        private static void veNut(mGraphics g, int x, int y, string dau)
        {
            g.setColor(0xC8933C, 1f);
            g.fillRect(x, y, O_NUT, O_NUT, 3);
            g.setColor(0x3A2710, 1f);
            g.fillRect(x + 1, y + 1, O_NUT - 2, O_NUT - 2, 2);
            mFont.tahoma_7b_white.drawString(g, dau, x + O_NUT / 2, y - 1, mFont.CENTER);
        }

        /// <summary>
        /// Bấm nút thu nhỏ / chip. Chạm vào thân khung thì nuốt luôn, để nhân vật
        /// không chạy tới chỗ vừa chạm.
        /// </summary>
        public void UpdateTouch()
        {
            if (!daVe)
            {
                return;
            }
            if (wNut > 0 && GameCanvas.isPointerHoldIn(xNut - 3, yNut - 3, wNut + 6, hNut + 6))
            {
                GameCanvas.isPointerJustDown = false;
                GameScr.gI().isPointerDowning = false;
                if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                {
                    gon = !gon;
                    GameCanvas.clearAllPointerEvent();
                }
                return;
            }
            if (GameCanvas.isPointerHoldIn(xVe, yVe, rongVe, caoVe))
            {
                GameCanvas.isPointerJustDown = false;
                GameScr.gI().isPointerDowning = false;
                if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                {
                    GameCanvas.clearAllPointerEvent();
                }
            }
        }

        /// <summary>Một thanh máu/KI có chữ đè giữa.</summary>
        private static void veThanh(mGraphics g, int x, int y, int w, int h, long gt, long max,
                int mau, string chu)
        {
            g.setColor(0x000000, 0.55f);
            g.fillRect(x, y, w, h, 3);
            if (max > 0 && gt > 0)
            {
                long co = gt > max ? max : gt;
                int dai = (int)((long)(w - 2) * co / max);
                if (dai > 0)
                {
                    g.setColor(mau, 0.95f);
                    g.fillRect(x + 1, y + 1, dai, h - 2, 2);
                }
            }
            mFont.tahoma_7b_white.drawString(g, chu, x + w / 2, y - 2, mFont.CENTER);
        }

        private static string tenTrangThai(sbyte s)
        {
            switch (s)
            {
                case 0:
                    return mResources.follow;
                case 1:
                    return mResources.defend;
                case 2:
                    return mResources.attack;
                case 3:
                    return mResources.gohome;
                case 4:
                    return mResources.fusion;
                case 5:
                    return mResources.fusionForever;
                default:
                    return string.Empty;
            }
        }

        /// <summary>Số gọn: 12,3 Tỉ / 4,5Tr / 6,7K.</summary>
        private static string soGon(long v)
        {
            if (v < 0)
            {
                v = 0;
            }
            if (v >= 1000000000L)
            {
                return motSoLe(v, 1000000000L) + " Tỉ";
            }
            if (v >= 1000000L)
            {
                return motSoLe(v, 1000000L) + "Tr";
            }
            if (v >= 1000L)
            {
                return motSoLe(v, 1000L) + "K";
            }
            return v.ToString();
        }

        private static string motSoLe(long v, long don)
        {
            long nguyen = v / don;
            long le = v % don * 10 / don;
            return le == 0 ? nguyen.ToString() : nguyen + "," + le;
        }

        /// <summary>Cắt tên dài cho vừa chỗ, thêm dấu ba chấm.</summary>
        private static string catChu(string s, int rongToiDa)
        {
            if (s == null)
            {
                return string.Empty;
            }
            if (mFont.tahoma_7b_yellow.getWidth(s) <= rongToiDa)
            {
                return s;
            }
            string t = s;
            while (t.Length > 1 && mFont.tahoma_7b_yellow.getWidth(t + "…") > rongToiDa)
            {
                t = t.Substring(0, t.Length - 1);
            }
            return t + "…";
        }
    }
}
