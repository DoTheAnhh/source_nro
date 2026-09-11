namespace Game6.God
{
    /// <summary>
    /// Khung "Thông tin đệ tử" — góc trên bên phải, đúng chỗ khung thông báo
    /// boss cũ.
    /// </summary>
    /// <remarks>
    /// <para>Nhỏ gọn: tên và trạng thái, thanh HP, thanh KI, sức mạnh. Bật tắt ở
    /// "Chức năng → Hiển thị" (mã 19), và nhớ lựa chọn qua các lần mở game.</para>
    ///
    /// <para>Máu đọc thẳng từ nhân vật đệ tử đang đứng trên bản đồ — thứ đó được
    /// cập nhật theo từng đòn đánh nên luôn tươi. Tên, trạng thái, sức mạnh và
    /// KI thì xin máy chủ năm giây một lần qua gói -107; gói ấy chỉ cập nhật dữ
    /// liệu, không bật bảng đệ tử lên.</para>
    /// </remarks>
    public class ThongTinDeTu
    {
        private static ThongTinDeTu instance;

        public static ThongTinDeTu getInstance()
        {
            return instance == null ? instance = new ThongTinDeTu() : instance;
        }

        private const string KHOA_LUU = "tt_de_tu";

        private bool daNap;

        private bool dangHien;

        /// <summary>Khung có đang hiện không. Đọc và ghi đều đi qua bộ nhớ máy.</summary>
        public bool isShow
        {
            get
            {
                if (!daNap)
                {
                    daNap = true;
                    try
                    {
                        dangHien = Rms.loadRMSInt(KHOA_LUU) == 1;
                    }
                    catch (System.Exception)
                    {
                        dangHien = false;
                    }
                }
                return dangHien;
            }
            set
            {
                dangHien = value;
                daNap = true;
                try
                {
                    Rms.saveRMSInt(KHOA_LUU, value ? 1 : 0);
                }
                catch (System.Exception)
                {
                }
            }
        }

        private const int LE_PHAI = 4;

        /// <summary>Mép trên — dưới hàng nút góc phải, như khung boss cũ.</summary>
        private const int DONG_DAU = 160;

        private const int RONG = 150;

        private const int CAO = 52;

        private const long HOI_DU_LIEU_MS = 5000;

        private long lanHoiCuoi;

        public void paint(mGraphics g)
        {
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

            int x = GameCanvas.w - LE_PHAI - RONG;
            int y = DONG_DAU;
            if (y + CAO > GameCanvas.h - 40)
            {
                y = GameCanvas.h - 40 - CAO;
            }
            if (y < 4)
            {
                y = 4;
            }

            // Nen bo goc vien vang, cung ho mau voi cac chip trang thai cua mod.
            g.setColor(0xC8933C, 0.9f);
            g.fillRect(x, y, RONG, CAO, 6);
            g.setColor(0x1E1408, 0.82f);
            g.fillRect(x + 1, y + 1, RONG - 2, CAO - 2, 5);

            mFont.tahoma_7b_yellow.drawString(g, catChu(pet.cName, RONG - 60), x + 6, y + 3,
                    mFont.LEFT);
            mFont.tahoma_7_grey.drawString(g, tenTrangThai(pet.petStatus), x + RONG - 6, y + 3,
                    mFont.RIGHT);

            veThanh(g, x + 6, y + 16, RONG - 12, 10, hp, hpFull, 0xD83A3A,
                    "HP " + gon(hp) + " / " + gon(hpFull));
            veThanh(g, x + 6, y + 28, RONG - 12, 10, pet.cMP, pet.cMPFull, 0x2F7BD8,
                    "KI " + gon(pet.cMP) + " / " + gon(pet.cMPFull));

            mFont.tahoma_7b_green.drawString(g, "Sức mạnh " + gon(pet.cPower), x + 6, y + 40,
                    mFont.LEFT);
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
            mFont.tahoma_7b_white.drawString(g, chu, x + w / 2, y - 1, mFont.CENTER);
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
        private static string gon(long v)
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
