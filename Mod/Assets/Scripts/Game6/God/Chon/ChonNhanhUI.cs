using System.Collections.Generic;

namespace Game6.God
{
    /// <summary>
    /// Bảng chọn nhanh: <b>đổi khu</b> và <b>dịch chuyển bằng capsule</b>.
    /// </summary>
    /// <remarks>
    /// <para>Hai bảng này trước đây là <c>Panel</c> loại 3 và loại 14 — cùng bộ
    /// vẽ với hành trang, cửa hàng, bang hội. Bộ đó trượt ngang từ mép màn hình,
    /// mỗi dòng là một dải chữ trơn, và nó chiếm gần hết màn chơi chỉ để hiện
    /// mươi dòng.</para>
    ///
    /// <para><b>Một lớp cho cả hai</b> vì chúng giống nhau đến từng thao tác:
    /// hiện một danh sách, bấm một ô, gửi một con số lên máy chủ. Khác đúng ba
    /// thứ — tiêu đề, nội dung ô, và lệnh gửi đi — nên tách làm hai lớp thì mỗi
    /// lần sửa giao diện phải sửa hai nơi.</para>
    ///
    /// <para>Dùng chung khung với hàng nút HUD (<c>GameScr.veKhungNutNhanh</c>):
    /// vành đồng, viền kép, lòng ô sáng, bốn đinh tán.</para>
    /// </remarks>
    public class ChonNhanhUI
    {
        private static ChonNhanhUI instance;

        public static ChonNhanhUI getInstance()
        {
            return instance ?? (instance = new ChonNhanhUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Kieu bang
        // ------------------------------------------------------------------

        private const int KIEU_KHU = 0;
        private const int KIEU_MAP = 1;

        private int kieu = KIEU_KHU;

        /// <summary>Một ô trong lưới.</summary>
        private class Dong
        {
            /// <summary>Chữ chính, ví dụ "Khu 3" hoặc "Rừng Nấm".</summary>
            public string ten;

            /// <summary>Chữ phụ dòng dưới, ví dụ "12/50" hoặc "Trái Đất".</summary>
            public string phu;

            /// <summary>Phần đã đầy, 0..100 — chỉ khu mới dùng.</summary>
            public int phanDay;

            /// <summary>Ô ứng với chỗ mình đang đứng.</summary>
            public bool dangODay;
        }

        private readonly List<Dong> ds = new List<Dong>();

        // ------------------------------------------------------------------
        //  Mo bang
        // ------------------------------------------------------------------

        /// <summary>
        /// Mở bảng đổi khu từ dữ liệu <c>GameScr</c> vừa nhận.
        /// </summary>
        /// <remarks>
        /// <b>Thứ tự trong danh sách chính là số gửi đi.</b> Máy chủ đọc con số
        /// ấy làm chỉ số khu, nên không được sắp xếp lại hay lọc bớt — làm vậy
        /// là bấm một khu lại sang một khu khác.
        /// </remarks>
        public void moKhu()
        {
            ds.Clear();
            kieu = KIEU_KHU;
            GameScr gs = GameScr.gI();
            if (gs != null && gs.zones != null)
            {
                for (int i = 0; i < gs.zones.Length; i++)
                {
                    Dong d = new Dong();
                    d.ten = "Khu " + gs.zones[i];
                    int dang = soAnToan(gs.numPlayer, i);
                    int tran = soAnToan(gs.maxPlayer, i);
                    d.phu = dang + "/" + (tran > 0 ? tran : 0);
                    d.phanDay = tran > 0 ? (dang * 100 / tran) : 0;
                    d.dangODay = (gs.zones[i] == TileMap.zoneID);
                    ds.Add(d);
                }
            }
            batDau();
        }

        /// <summary>Mở bảng dịch chuyển của capsule.</summary>
        public void moMap(string[] tenMap, string[] tenHanhTinh)
        {
            ds.Clear();
            kieu = KIEU_MAP;
            if (tenMap != null)
            {
                for (int i = 0; i < tenMap.Length; i++)
                {
                    Dong d = new Dong();
                    d.ten = tenMap[i];
                    d.phu = (tenHanhTinh != null && i < tenHanhTinh.Length)
                            ? tenHanhTinh[i] : "";
                    d.phanDay = -1;
                    d.dangODay = false;
                    ds.Add(d);
                }
            }
            batDau();
        }

        private void batDau()
        {
            cuon = 0;
            chon = -1;
            // Hop "vui long cho" duoc bat truoc khi xin du lieu, ma bang cu dua
            // vao Panel.show() de tat no. Bang nay khong di qua Panel nen phai
            // tu tat, khong thi hop cho nam lai tren man hinh.
            InfoDlg.hide();
            dangMo = true;
        }

        public void dong()
        {
            dangMo = false;
        }

        private static int soAnToan(int[] a, int i)
        {
            return (a != null && i < a.Length) ? a[i] : 0;
        }

        // ------------------------------------------------------------------
        //  Mau
        // ------------------------------------------------------------------

        /// <summary>Nền lòng bảng — tối và ít bão hoà, cho ô nổi hẳn lên.</summary>
        /// <remarks>
        /// Nền bảng HUD là nâu vừa, mà ô cũng nâu — hai thứ cùng tông thì viền ô
        /// chìm vào nền và cả bảng đọc ra một mảng nâu. Phủ một lớp tối lên lòng
        /// bảng thì ô sáng bật hẳn ra.
        /// </remarks>
        private const int MAU_LONG_BANG = 0x241A0F;

        /// <summary>Vạch đánh dấu chỗ đang đứng.</summary>
        private const int MAU_DANG_O = 0x8CF07A;

        // ------------------------------------------------------------------
        //  Bo cuc
        // ------------------------------------------------------------------

        private const int CAO_TIEU_DE = 22;
        private const int LE = 8;

        /// <summary>Ô cao hai dòng chữ: tên ở trên, chữ phụ ở dưới.</summary>
        private const int CAO_O = 30;

        private const int KHE = 5;

        /// <summary>Số cột trong lưới.</summary>
        private const int COT = 3;

        /// <summary>Số hàng hiện cùng lúc; phần còn lại phải cuộn.</summary>
        private const int SO_HANG_THAY = 5;

        private int cuon;
        private int chon = -1;

        private int x0;
        private int y0;
        private int rong;
        private int cao;
        private int rongO;
        private int soHangThay;
        private int tongHang;

        private void tinhBoCuc()
        {
            tongHang = (ds.Count + COT - 1) / COT;
            if (tongHang < 1)
            {
                tongHang = 1;
            }
            soHangThay = tongHang < SO_HANG_THAY ? tongHang : SO_HANG_THAY;

            // Ten ban do dai hon "Khu 12" nen o rong hon; co lai khi man hep.
            rongO = (kieu == KIEU_MAP) ? 104 : 84;
            int toiDaO = (GameCanvas.w - 24 - LE * 2 - (COT - 1) * KHE) / COT;
            if (rongO > toiDaO)
            {
                rongO = toiDaO;
            }
            if (rongO < 52)
            {
                rongO = 52;
            }

            rong = LE * 2 + COT * rongO + (COT - 1) * KHE;
            cao = CAO_TIEU_DE + LE + soHangThay * CAO_O
                    + (soHangThay - 1) * KHE + LE;
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;

            int toiDaCuon = tongHang - soHangThay;
            if (toiDaCuon < 0)
            {
                toiDaCuon = 0;
            }
            if (cuon > toiDaCuon)
            {
                cuon = toiDaCuon;
            }
            if (cuon < 0)
            {
                cuon = 0;
            }
        }

        /// <summary>Góc trên trái của ô thứ <paramref name="i"/> trong danh sách.</summary>
        /// <returns><c>false</c> nếu ô đang bị cuộn ra ngoài tầm nhìn.</returns>
        private bool viTriO(int i, out int x, out int y)
        {
            int hang = i / COT - cuon;
            x = 0;
            y = 0;
            if (hang < 0 || hang >= soHangThay)
            {
                return false;
            }
            int cot = i % COT;
            x = x0 + LE + cot * (rongO + KHE);
            y = y0 + CAO_TIEU_DE + LE / 2 + hang * (CAO_O + KHE);
            return true;
        }

        // ------------------------------------------------------------------
        //  Ve
        // ------------------------------------------------------------------

        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            tinhBoCuc();

            // Lam toi ca man phia sau: mat bam ngay vao bang, va no cung noi ro
            // rang phan duoi dang khong nhan cham.
            g.setColor(0, 0.45f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            GameScr.veNenBangHud(g, x0, y0, rong, cao);

            // Long bang toi lai cho o noi len.
            g.setColor(MAU_LONG_BANG, 0.55f);
            g.fillRect(x0 + 4, y0 + CAO_TIEU_DE + 2, rong - 8,
                    cao - CAO_TIEU_DE - 6, 6);

            // Dai tieu de.
            g.setColor(0x3A2409, 0.6f);
            g.fillRect(x0 + 3, y0 + 3, rong - 6, CAO_TIEU_DE - 2, 6);
            mFont.tahoma_7b_yellow.drawString(g,
                    kieu == KIEU_MAP ? "DỊCH CHUYỂN" : "CHỌN KHU",
                    x0 + rong / 2, y0 + 6, mFont.CENTER);

            veNutDong(g, x0 + rong - 21, y0 + 4);

            for (int i = 0; i < ds.Count; i++)
            {
                int x, y;
                if (!viTriO(i, out x, out y))
                {
                    continue;
                }
                Dong d = ds[i];
                // O dang dung ve o trang thai SANG — cung cach mot nut dang bam.
                // Doi mau chu thi khong du: chu trong o nho, mau nao cung chim.
                GameScr.veKhungNutNhanh(g, x, y, rongO, CAO_O,
                        i == chon || d.dangODay);

                int caoChu = mFont.tahoma_7b_yellow.getHeight();
                int yTen = y + 4;
                int yPhu = y + CAO_O - caoChu - 5;

                // Chu TRANG cho o dang dung, vang cho o con lai.
                //
                // Ban truoc dung tahoma_7b_green2 — ten nghe la mau xanh nhung
                // anh chu that la #005325, xanh gan den. Dat tren long o nau
                // toi thi doc khong ra, dung canh "Khu 0" mo tit trong anh chup.
                mFont mfTen = d.dangODay
                        ? mFont.tahoma_7b_white : mFont.tahoma_7b_yellow;
                mFont.tahoma_7b_dark.drawString(g,
                        catVua(d.ten, rongO - 14, mfTen), x + 8, yTen + 1,
                        mFont.LEFT);
                mfTen.drawString(g, catVua(d.ten, rongO - 14, mfTen),
                        x + 7, yTen, mFont.LEFT);

                if (d.phu != null && d.phu.Length > 0)
                {
                    // Chu phu mau TRANG.
                    //
                    // tahoma_7_grey la xam dam, dat tren long o nau thi
                    // gan nhu doc khong ra — "0/12" hay "Trai Dat" chim
                    // han vao nen.
                    mFont mfPhu = mFont.tahoma_7_white;
                    mFont.tahoma_7b_dark.drawString(g, d.phu,
                            x + rongO - 7, yPhu + 1, mFont.RIGHT);
                    mfPhu.drawString(g, d.phu, x + rongO - 8, yPhu,
                            mFont.RIGHT);
                }

                // Thanh do day cua khu: doc duoc muc dong ma khong phai doc so.
                if (d.phanDay >= 0)
                {
                    int wT = rongO - 14;
                    int yT = y + CAO_O - 4;
                    g.setColor(0x000000, 0.5f);
                    g.fillRect(x + 7, yT, wT, 2, 1);
                    int mau = d.phanDay >= 90 ? 0xE05A4A
                            : (d.phanDay >= 60 ? 0xE0B84A : 0x7CE07C);
                    g.setColor(mau, 0.95f);
                    g.fillRect(x + 7, yT, wT * d.phanDay / 100, 2, 1);
                }

                // Vach sang o mep trai: dau hieu thu hai cho cho dang dung, de
                // nguoi phan biet mau kem van nhan ra.
                if (d.dangODay)
                {
                    g.setColor(MAU_DANG_O, 0.95f);
                    g.fillRect(x + 3, y + 5, 2, CAO_O - 10, 1);
                }
            }

            // Hai nut cuon, chi hien khi con hang bi cat.
            if (tongHang > soHangThay)
            {
                int xN = x0 + rong - 15;
                veNutCuon(g, xN, y0 + CAO_TIEU_DE + LE / 2, true);
                veNutCuon(g, xN, y0 + cao - LE / 2 - 12, false);
            }
        }

        /// <summary>Cắt chuỗi cho vừa <paramref name="rongToiDa"/> điểm.</summary>
        /// <remarks>
        /// Cắt theo bề rộng đo được chứ không theo số ký tự: chữ Việt có dấu
        /// rộng hẹp khác nhau nên đếm ký tự lúc thừa chỗ lúc vẫn tràn.
        /// </remarks>
        private static string catVua(string s, int rongToiDa, mFont mf)
        {
            if (s == null || mf == null)
            {
                return "";
            }
            if (mf.getWidth(s) <= rongToiDa)
            {
                return s;
            }
            for (int n = s.Length - 1; n > 0; n--)
            {
                string thu = s.Substring(0, n) + "…";
                if (mf.getWidth(thu) <= rongToiDa)
                {
                    return thu;
                }
            }
            return "";
        }

        /// <summary>Nút cuộn nhỏ: tam giác dựng từ các vạch ngang.</summary>
        private static void veNutCuon(mGraphics g, int x, int y, bool len)
        {
            g.setColor(0x6B4718, 1f);
            g.fillRect(x, y, 12, 12, 5);
            g.setColor(0xFFE9A3, 1f);
            for (int i = 0; i < 4; i++)
            {
                int r = len ? (i * 2 + 1) : (7 - i * 2);
                g.fillRect(x + 6 - r / 2, y + 4 + i, r, 1);
            }
        }

        private static void veNutDong(mGraphics g, int x, int y)
        {
            const int canh = 16;
            g.setColor(0x1A1006, 1f);
            g.fillRect(x, y, canh, canh, 5);
            g.setColor(0xD84A3A, 1f);
            g.fillRect(x + 1, y + 1, canh - 2, canh - 2, 4);
            g.setColor(0xFFFFFF, 1f);

            // Can giua theo BE RONG THAT cua dau X, khong dem tu tam nut: moi
            // dau la mot diem 2x2 nen bay diem buoc 1px trai rong 6 + 2 = 8px.
            const int soDiem = 7;
            const int coDiem = 2;
            const int ben = soDiem - 1 + coDiem;
            int xX = x + (canh - ben) / 2;
            int yX = y + (canh - ben) / 2;
            for (int i = 0; i < soDiem; i++)
            {
                g.fillRect(xX + i, yX + i, coDiem, coDiem);
                g.fillRect(xX + ben - coDiem - i, yX + i, coDiem, coDiem);
            }
        }

        // ------------------------------------------------------------------
        //  Cham
        // ------------------------------------------------------------------

        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            tinhBoCuc();

            // Lan chuot: xet truoc ca nha ngon vi banh xe khong sinh ra su kien
            // nha ngon nao, de sau cac nhanh kia thi khong bao gio toi luot.
            if (GameCanvas.pXYScrollMouse != 0
                    && GameCanvas.pxMouse >= x0
                    && GameCanvas.pxMouse <= x0 + rong
                    && GameCanvas.pyMouse >= y0
                    && GameCanvas.pyMouse <= y0 + cao)
            {
                cuon += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
                return true;
            }

            if (!GameCanvas.isPointerJustRelease)
            {
                // Van nuot cham khi dang giu ngon: khong thi ngon de len bang
                // se dieu khien nhan vat phia duoi.
                return true;
            }

            if (cham(x0 + rong - 21, y0 + 4, 16, 16))
            {
                dong();
                return true;
            }

            if (tongHang > soHangThay)
            {
                int xN = x0 + rong - 15;
                if (cham(xN, y0 + CAO_TIEU_DE + LE / 2, 12, 12))
                {
                    cuon--;
                    return true;
                }
                if (cham(xN, y0 + cao - LE / 2 - 12, 12, 12))
                {
                    cuon++;
                    return true;
                }
            }

            for (int i = 0; i < ds.Count; i++)
            {
                int x, y;
                if (!viTriO(i, out x, out y))
                {
                    continue;
                }
                if (cham(x, y, rongO, CAO_O))
                {
                    chon = i;
                    lamViec(i);
                    return true;
                }
            }

            // Bam ra ngoai khung KHONG dong bang — chi dau X moi dong. Van nuot
            // cu cham: khong nuot thi no roi xuong ban do va nhan vat chay toi.
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        /// <summary>Gửi lựa chọn lên máy chủ rồi đóng bảng.</summary>
        private void lamViec(int i)
        {
            if (kieu == KIEU_MAP)
            {
                Service.gI().requestMapSelect(i);
                InfoDlg.showWait();
                dong();
                return;
            }
            // Dang o dung khu do thi khong xin doi: may chu se tu choi, ma
            // nguoi choi lai phai ngoi nhin hop "vui long cho" mot luc.
            GameScr gs = GameScr.gI();
            if (gs != null && gs.zones != null && i < gs.zones.Length
                    && gs.zones[i] == TileMap.zoneID)
            {
                GameScr.info1.addInfo(mResources.ZONE_HERE, 0);
                dong();
                return;
            }
            Service.gI().requestChangeZone(i, -1);
            InfoDlg.showWait();
            dong();
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
