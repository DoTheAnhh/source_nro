// Khong "using System": lop Math cua engine trung ten voi System.Math, de ca hai
// thi trinh bien dich khong biet chon cai nao.
using System.Collections.Generic;

namespace Game1.God
{
    /// <summary>
    /// Màn hình Phúc lợi — cột mục bên trái, danh sách mốc bên phải.
    /// </summary>
    /// <remarks>
    /// <para>Vẽ đè lên màn chơi và tự bắt chạm, giống hộp cấu hình VoiceChat:
    /// engine này đổi màn hình là dọn cả trạng thái điều khiển, mà mở bảng quà
    /// thì không có lý do gì để nhân vật đứng khựng.</para>
    ///
    /// <para><b>Dữ liệu đến một lần cả cụm</b> qua gói -58. Bấm qua lại giữa các
    /// mục là việc liên tục; hỏi máy chủ mỗi lần bấm sẽ thấy khựng. Chỉ sau khi
    /// nhận quà máy chủ mới gửi lại cả cụm.</para>
    ///
    /// <para><b>Bố cục tính một lần mỗi khung hình</b> trong <see cref="tinhBoCuc"/>
    /// rồi cả phần vẽ lẫn phần bắt chạm cùng đọc. Trước đây hai bên tự tính lại
    /// theo cùng công thức — sửa một bên là lệch vùng bấm mà không có gì báo.</para>
    /// </remarks>
    public class PhucLoiUI
    {
        private static PhucLoiUI instance;

        public static PhucLoiUI getInstance()
        {
            return instance ?? (instance = new PhucLoiUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Dữ liệu
        // ------------------------------------------------------------------
        public class Qua
        {
            public int icon;
            public int soLuong;
        }

        public class Moc
        {
            public int id;
            public string nhan;
            /// <summary>0 chưa đủ · 1 nhận được · 2 đã nhận.</summary>
            public int trangThai;
            public string moTaThieu;
            /// <summary>Da di duoc bao nhieu phan tram toi moc.</summary>
            public int phanTram;
            public readonly List<Qua> qua = new List<Qua>();
        }

        public class Nhom
        {
            public string ten;
            public string tenLoai;
            public string tienDo;
            public readonly List<Moc> moc = new List<Moc>();
        }

        /// <summary>Một bậc thẻ tháng đang bán.</summary>
        public class GoiThe
        {
            public int bac;
            public string ten;
            public string gia;
            public string uuDai;
            public int soNgay;
            public readonly List<Qua> quaMua = new List<Qua>();
            public readonly List<Qua> quaNgay = new List<Qua>();
        }

        /// <summary>Toàn bộ mục thẻ tháng, máy chủ gửi ở cuối gói phúc lợi.</summary>
        public class TheThang
        {
            public readonly List<GoiThe> goi = new List<GoiThe>();
            /// <summary>Bậc đang dùng, 0 là chưa có hoặc đã hết hạn.</summary>
            public int bacDangCo;
            public int soNgayCon;
            public string hetHan;
            public bool daNhanHomNay;
            public string soDu;
        }

        private readonly List<Nhom> nhom = new List<Nhom>();
        private int nhomChon;
        private int cuon;

        /// <summary>Mục thẻ tháng; <c>null</c> khi máy chủ cũ không gửi.</summary>
        private TheThang theThang;

        /// <summary>
        /// Đang xem mục thẻ tháng thay cho một nhóm mốc. Mở bảng lần đầu là vào
        /// thẳng mục này: nó đứng đầu cột và là thứ duy nhất bán bằng tiền.
        /// </summary>
        private bool xemThe = true;

        /// <summary>Bậc đang chờ bấm lần hai để xác nhận mua, 0 là không có.</summary>
        private int bacChoXacNhan;

        /// <summary>Lúc bấm lần một; quá <see cref="HAN_XAC_NHAN"/> thì phải bấm lại từ đầu.</summary>
        private long lucChoXacNhan;

        /// <summary>
        /// Bấm "Mua" lần một chỉ đổi nút thành "Bấm lần nữa để mua"; lần hai mới
        /// gửi. Mua là trừ tiền thật, một cú chạm lỡ tay không được phép tốn tiền.
        /// </summary>
        private const long HAN_XAC_NHAN = 4000L;

        /// <summary>Có mục thẻ tháng trên cột trái hay không.</summary>
        private bool coThe()
        {
            return theThang != null && theThang.goi.Count > 0;
        }

        /// <summary>Số dòng trên cột trái: thẻ tháng (nếu có) rồi tới các nhóm.</summary>
        private int soMuc()
        {
            return nhom.Count + (coThe() ? 1 : 0);
        }

        private bool dangXemThe()
        {
            return xemThe && coThe();
        }

        // ------------------------------------------------------------------
        //  Màu và kích thước
        // ------------------------------------------------------------------
        // setColor nhan MOT so nguyen RGB. Ghi kem ma hex vi doc so thap phan
        // khong doan duoc ra mau gi — va chinh cho nay tung co hang 225225225
        // tuong la xam nhat, hoa ra la mau xanh la.
        // Bang mau lay DUNG theo man nhan vat kieu moi (TuiMoi/TuiUI): nen kem,
        // vien nau, dai tieu de va o dang chon mau cam. Ba man phu nay truoc day
        // moi cai mot bo mau rieng (nau gan den, xanh than), nen mo lan luot ra
        // nhin nhu ba game khac nhau.
        //
        // Ghi bang rgb() cho doc duoc ma hex; setColor nhan mot so nguyen RGB.
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        /// <summary>Nền bảng.</summary>
        private static readonly int MAU_NEN = rgb(0xFC, 0xE3, 0xC0);

        /// <summary>Dải tiêu đề: cam, cùng màu highlight của thẻ con.</summary>
        private static readonly int MAU_TIEU_DE = rgb(0xF0, 0xA1, 0x64);

        /// <summary>Ô, dòng, nút ở trạng thái thường.</summary>
        private static readonly int MAU_THE = rgb(0xFD, 0xF0, 0xDC);

        /// <summary>Ô mờ hơn: rãnh cuộn, dòng đã xong.</summary>
        private static readonly int MAU_THE_MO = rgb(0xFF, 0xF3, 0xDE);

        /// <summary>Nét viền, và tay cuộn.</summary>
        private static readonly int MAU_VIEN = rgb(0xA8, 0x6E, 0x3C);

        /// <summary>
        /// Nền của ô/nút ĐANG CHỌN.
        /// </summary>
        /// <remarks>
        /// Phải là một màu riêng, không dùng lại <see cref="MAU_VIEN"/>. Bản trước
        /// một hằng số làm cả hai việc — vừa là nét viền vừa là nền ô đang chọn —
        /// nên đổi màu nền kem là ô đang chọn thành nâu đặc, chữ chìm hẳn.
        /// </remarks>
        private static readonly int MAU_CHON = rgb(0xF0, 0xA1, 0x64);

        /// <summary>Bo góc chung, cùng con số với màn nhân vật.</summary>
        private const int BO_GOC = 6;

        /// <summary>
        /// Khung bo góc có viền: tô màu viền cả khối rồi tô nền thụt vào.
        /// </summary>
        /// <remarks>
        /// Dùng <c>fillRect</c> hai lần chứ không <c>drawRect</c>: drawRect của
        /// <c>mGraphics</c> KHÔNG nhận bán kính, nên viền vẽ bằng nó là một khung
        /// vuông chạy quanh một khối bo góc — hở bốn góc.
        /// </remarks>
        private static void veKhungBo(mGraphics g, int x, int y, int w, int h,
                int mauNen, float moNen, int mauVien, float moVien, int day)
        {
            g.setColor(mauVien, moVien);
            g.fillRect(x, y, w, h, BO_GOC);
            g.setColor(mauNen, moNen);
            g.fillRect(x + day, y + day, w - day * 2, h - day * 2, BO_GOC - 1);
        }

        private const int CAO_TIEU_DE = 22;
        private const int RONG_TRAI = 132;
        private const int CAO_MUC = 26;
        private const int CAO_THE = 46;
        private const int LE = 6;

        // Bố cục, tính lại mỗi khung hình
        private int x0, y0, rong, cao;
        private int xTrai, yThan, caoThan;
        private int xPhai, rongPhai, yThe, caoVungThe;
        private int soTheHien;

        /// <summary>Nap du lieu vua nhan, GIU nguyen muc dang xem.</summary>
        /// <remarks>
        /// Khong dat lai nhomChon va cuon: du lieu tu lam moi moi 15 giay, dat
        /// lai la dang xem doc giua chung bi nhay ve dau moi 15 giay mot lan.
        /// </remarks>
        public void nhanDuLieu(List<Nhom> ds)
        {
            nhom.Clear();
            if (ds != null)
            {
                nhom.AddRange(ds);
            }
            if (nhomChon >= nhom.Count)
            {
                nhomChon = 0;
                cuon = 0;
            }
            lucXinCuoi = mSystem.currentTimeMillis();
            dangMo = true;
        }

        /// <summary>Nạp mục thẻ tháng — gọi TRƯỚC <see cref="nhanDuLieu"/>.</summary>
        public void nhanTheThang(TheThang t)
        {
            theThang = t;
        }

        /// <summary>Moc thoi gian lan cuoi xin du lieu.</summary>
        private long lucXinCuoi;

        /// <summary>Bao lau thi xin lai du lieu mot lan, tinh bang mili giay.</summary>
        /// <remarks>
        /// Phut online va suc manh doi lien tuc; khong lam moi thi con so tren
        /// man hinh dung yen tu luc mo den luc dong.
        /// </remarks>
        private const long NHIP_LAM_MOI = 15000L;

        /// <summary>Goi moi khung hinh: het nhip thi xin du lieu moi.</summary>
        public void capNhat()
        {
            if (!dangMo)
            {
                return;
            }
            if (mSystem.currentTimeMillis() - lucXinCuoi >= NHIP_LAM_MOI)
            {
                lucXinCuoi = mSystem.currentTimeMillis();
                Service.gI().phucLoiXin();
            }
        }

        public void dong()
        {
            dangMo = false;
        }

        /// <summary>Tính mọi toạ độ dùng cho cả vẽ lẫn bắt chạm.</summary>
        private void tinhBoCuc()
        {
            // 0,68 bề ngang khả dụng: hẹp hơn thì cột trái phải cắt bớt tên mục,
            // rộng hơn thì mỗi thẻ mốc dài ngoằng mà nội dung thì ít.
            int rongToiDa = GameCanvas.w - 20;
            rong = Math.max(280, rongToiDa * 68 / 100);
            cao = Math.max(150, GameCanvas.h - 30);
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;

            xTrai = x0 + LE;
            yThan = y0 + CAO_TIEU_DE + LE;
            caoThan = cao - CAO_TIEU_DE - LE * 2;

            xPhai = xTrai + RONG_TRAI + LE;
            rongPhai = x0 + rong - LE - xPhai;
            yThe = yThan + 22;
            caoVungThe = caoThan - 22;
            soTheHien = Math.max(1, caoVungThe / CAO_THE);
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
            tinhBoCuc();

            g.setColor(0, 0.62f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veKhungBo(g, x0, y0, rong, cao, MAU_NEN, 0.97f, MAU_VIEN, 1f, 1);
            g.setColor(MAU_TIEU_DE, 1f);
            g.fillRect(x0 + 1, y0 + 1, rong - 2, CAO_TIEU_DE, BO_GOC);

            mFont.tahoma_7b_red.drawString(g, "PHÚC LỢI",
                    x0 + rong / 2, y0 + 4, mFont.CENTER);
            veNut(g, x0 + rong - 20, y0 + 4, 16, 14, "X", false);
            int soCho = soMucChoNhan();
            if (soCho > 0)
            {
                int[] nn = oNutNhanNhanh();
                veNut(g, nn[0], nn[1], nn[2], nn[3], "Nhận nhanh (" + soCho + ")", true);
            }

            if (soMuc() == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Chưa có mục phúc lợi nào",
                        x0 + rong / 2, y0 + cao / 2 - 5, mFont.CENTER);
                return;
            }
            if (!coThe() || nhom.Count == 0)
            {
                // Chi con mot loai muc thi xem dung loai do.
                xemThe = coThe();
            }
            veCotTrai(g);
            if (dangXemThe())
            {
                veTheThang(g);
            }
            else
            {
                veCotPhai(g);
            }
        }

        private void veCotTrai(mGraphics g)
        {
            g.setColor(MAU_THE_MO, 0.55f);
            g.fillRect(xTrai, yThan, RONG_TRAI, caoThan, 6);
            int lech = coThe() ? 1 : 0;
            for (int i = 0; i < soMuc(); i++)
            {
                int yy = yThan + 4 + i * (CAO_MUC + 3);
                if (yy + CAO_MUC > yThan + caoThan - 2)
                {
                    break;
                }
                bool laThe = coThe() && i == 0;
                bool chon = laThe ? dangXemThe() : (!dangXemThe() && i - lech == nhomChon);
                if (laThe)
                {
                    // Dong the thang: nen vang dam, chu trang — la thu duy nhat
                    // tren bang ban bang tien, phai nhin ra ngay.
                    veKhungBo(g, xTrai + 3, yy, RONG_TRAI - 6, CAO_MUC,
                            chon ? MAU_THE_CAO : MAU_THE_VANG, 1f, MAU_VIEN, 0.9f, 1);
                    mFont.tahoma_7b_white.drawString(g, "NRO PASS",
                            xTrai + RONG_TRAI / 2, yy + CAO_MUC / 2 - 5, mFont.CENTER);
                    if (coTheNhanHomNay())
                    {
                        // Cham do: con qua hom nay chua nhan.
                        g.setColor(0xE53935, 1f);
                        g.fillRect(xTrai + RONG_TRAI - 14, yy + 3, 7, 7, 4);
                    }
                    continue;
                }
                g.setColor(chon ? MAU_CHON : MAU_THE, chon ? 1f : 0.95f);
                g.fillRect(xTrai + 3, yy, RONG_TRAI - 6, CAO_MUC, 6);
                if (chon)
                {
                    // Vach nhan mau sang ben trai: nhin luot la biet dang o muc nao,
                    // khong phai so mau nen dam nhat.
                    g.setColor(16777215, 0.85f);
                    g.fillRect(xTrai + 3, yy, 3, CAO_MUC);
                }
                mFont.tahoma_7b_dark.drawString(g, catBot(nhom[i - lech].ten, 20),
                        xTrai + RONG_TRAI / 2, yy + CAO_MUC / 2 - 5, mFont.CENTER);
            }
        }

        /// <summary>
        /// Số mục đang chờ nhận: quà NRO Pass hôm nay cộng mọi mốc đã đủ.
        /// </summary>
        private int soMucChoNhan()
        {
            int n = coTheNhanHomNay() ? 1 : 0;
            foreach (Nhom nh in nhom)
            {
                foreach (Moc m in nh.moc)
                {
                    if (m.trangThai == 1)
                    {
                        n++;
                    }
                }
            }
            return n;
        }

        /// <summary>Nút "Nhận nhanh" ở góc trái dải tiêu đề.</summary>
        /// <remarks>
        /// Chỉ hiện khi có gì để nhận: nút bấm vào mà không ra gì thì người chơi
        /// thôi tin nó.
        /// </remarks>
        private int[] oNutNhanNhanh()
        {
            string chu = "Nhận nhanh (" + soMucChoNhan() + ")";
            int w = mFont.tahoma_7b_dark.getWidth(chu) + 16;
            return new int[] { x0 + 6, y0 + 3, w, 16 };
        }

        /// <summary>Đang có thẻ mà hôm nay chưa nhận quà.</summary>
        private bool coTheNhanHomNay()
        {
            return theThang != null && theThang.bacDangCo > 0 && !theThang.daNhanHomNay;
        }

        // ------------------------------------------------------------------
        //  Mục thẻ tháng
        // ------------------------------------------------------------------
        /// <summary>Đầu thẻ bậc thường: vàng nhạt.</summary>
        private static readonly int MAU_THE_VANG = rgb(0xE0, 0x9A, 0x3E);

        /// <summary>Đầu thẻ cao cấp, và dòng thẻ tháng đang chọn: cam cháy.</summary>
        private static readonly int MAU_THE_CAO = rgb(0xD9, 0x6A, 0x12);

        private const int CAO_DAI_NHAN = 40;

        /// <summary>Vùng thẻ thứ <paramref name="i"/>: x, y, rộng, cao.</summary>
        private int[] oThe(int i)
        {
            int n = theThang.goi.Count;
            int khe = 6;
            int w = (rongPhai - khe * (n - 1)) / n;
            int y = yThan + 22;
            int h = caoThan - 22 - CAO_DAI_NHAN - 6;
            return new int[] { xPhai + i * (w + khe), y, w, h };
        }

        /// <summary>Nút nhận quà hôm nay, ở dải dưới cùng.</summary>
        private int[] oNutNhanNgay()
        {
            int y = yThan + caoThan - CAO_DAI_NHAN;
            return new int[] { xPhai + rongPhai - 80, y + (CAO_DAI_NHAN - 20) / 2, 72, 20 };
        }

        private GoiThe goiDangDung()
        {
            if (theThang == null)
            {
                return null;
            }
            foreach (GoiThe gt in theThang.goi)
            {
                if (gt.bac == theThang.bacDangCo)
                {
                    return gt;
                }
            }
            return null;
        }

        private void veTheThang(mGraphics g)
        {
            TheThang t = theThang;

            // Dai trang thai.
            g.setColor(MAU_THE, 0.9f);
            g.fillRect(xPhai, yThan, rongPhai, 18, 6);
            GoiThe dung = goiDangDung();
            string trangThai = dung == null ? "Chưa có NRO Pass"
                    : (dung.ten + " · còn " + t.soNgayCon + " ngày");
            mFont.tahoma_7b_red.drawString(g, trangThai, xPhai + 8, yThan + 4, mFont.LEFT);
            if (!string.IsNullOrEmpty(t.soDu))
            {
                int rTT = mFont.tahoma_7b_red.getWidth(trangThai);
                if (mFont.tahoma_7.getWidth(t.soDu) + rTT + 24 < rongPhai)
                {
                    mFont.tahoma_7.drawString(g, t.soDu, xPhai + rongPhai - 8, yThan + 4,
                            mFont.RIGHT);
                }
            }

            for (int i = 0; i < t.goi.Count; i++)
            {
                veMotTheThang(g, t.goi[i], oThe(i));
            }
            veDaiNhanNgay(g);
        }

        private void veMotTheThang(mGraphics g, GoiThe gt, int[] o)
        {
            bool dangDung = theThang.bacDangCo == gt.bac;
            bool caoCap = gt.bac >= 2;
            veKhungBo(g, o[0], o[1], o[2], o[3], MAU_THE, 0.97f,
                    dangDung ? MAU_THE_CAO : MAU_VIEN, dangDung ? 1f : 0.8f, dangDung ? 2 : 1);

            // Dau the: dai mau, ten the.
            int mauDau = caoCap ? MAU_THE_CAO : MAU_THE_VANG;
            g.setColor(mauDau, 1f);
            g.fillRect(o[0] + 2, o[1] + 2, o[2] - 4, 17, BO_GOC - 1);
            mFont.tahoma_7b_white.drawString(g, catBot(gt.ten, 24), o[0] + o[2] / 2,
                    o[1] + 5, mFont.CENTER);
            if (dangDung)
            {
                // Nhan "Dang dung" goc tren phai.
                string nhanDD = "Đang dùng";
                int wD = mFont.tahoma_7b_white.getWidth(nhanDD) + 8;
                g.setColor(0x2E9E48, 1f);
                g.fillRect(o[0] + o[2] - wD - 4, o[1] + 22, wD, 12, 4);
                mFont.tahoma_7b_white.drawString(g, nhanDD, o[0] + o[2] - wD / 2 - 4,
                        o[1] + 22, mFont.CENTER);
            }

            int x = o[0] + 8;
            int y = o[1] + 23;
            mFont.tahoma_7b_red.drawString(g, gt.gia, x, y, mFont.LEFT);
            y += 12;
            mFont.tahoma_7_grey.drawString(g, gt.soNgay + " ngày", x, y, mFont.LEFT);
            y += 12;

            // Uu dai: toi da hai dong.
            string[] dong = mFont.tahoma_7.splitFontArray(gt.uuDai ?? string.Empty, o[2] - 16);
            for (int i = 0; i < dong.Length && i < 2; i++)
            {
                mFont.tahoma_7_blue.drawString(g, dong[i], x, y, mFont.LEFT);
                y += 11;
            }
            y += 3;

            int yNut = o[1] + o[3] - 24;
            y = veHangQua(g, "Nhận ngay", gt.quaMua, x, y, o[2] - 16, yNut);
            veHangQua(g, "Mỗi ngày", gt.quaNgay, x, y, o[2] - 16, yNut);

            // Nut mua.
            int[] n = oNutMuaTheoThe(o);
            bool khacBac = theThang.bacDangCo > 0 && !dangDung;
            bool choXN = bacChoXacNhan == gt.bac
                    && mSystem.currentTimeMillis() - lucChoXacNhan < HAN_XAC_NHAN;
            if (khacBac)
            {
                veKhungBo(g, n[0], n[1], n[2], n[3], MAU_THE_MO, 0.8f, MAU_VIEN, 0.4f, 1);
                mFont.tahoma_7_grey.drawString(g, "Đang dùng gói khác", n[0] + n[2] / 2,
                        n[1] + n[3] / 2 - 5, mFont.CENTER);
            }
            else
            {
                string chu = choXN ? "Bấm lần nữa để mua"
                        : (dangDung ? "Gia hạn " + gt.soNgay + " ngày" : "Mua ngay");
                veKhungBo(g, n[0], n[1], n[2], n[3], choXN ? MAU_THE_CAO : MAU_CHON, 1f,
                        MAU_VIEN, 0.9f, 1);
                (choXN ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark)
                        .drawString(g, chu, n[0] + n[2] / 2, n[1] + n[3] / 2 - 5, mFont.CENTER);
            }
        }

        private int[] oNutMuaTheoThe(int[] o)
        {
            return new int[] { o[0] + 8, o[1] + o[3] - 24, o[2] - 16, 18 };
        }

        /// <summary>
        /// Một hàng quà có nhãn ở trên; trả về y cho hàng kế tiếp. Không vẽ
        /// tràn xuống nút mua: hết chỗ thì bỏ hàng.
        /// </summary>
        private int veHangQua(mGraphics g, string nhan, List<Qua> ds, int x, int y,
                int w, int yGioiHan)
        {
            if (ds.Count == 0 || y + 34 > yGioiHan)
            {
                return y;
            }
            mFont.tahoma_7b_dark.drawString(g, nhan + ":", x, y, mFont.LEFT);
            int ix = x;
            int yIcon = y + 11;
            for (int i = 0; i < ds.Count; i++)
            {
                if (ix + 22 > x + w)
                {
                    mFont.tahoma_7.drawString(g, "…", ix + 2, yIcon + 6, mFont.LEFT);
                    break;
                }
                veMotQua(g, ds[i], ix, yIcon);
                ix += 25;
            }
            return yIcon + 25;
        }

        /// <summary>Ô quà 22×22: icon, số lượng trên dải tối dưới đáy.</summary>
        private void veMotQua(mGraphics g, Qua q, int ix, int yIcon)
        {
            g.setColor(MAU_THE_MO, 0.85f);
            g.fillRect(ix, yIcon, 22, 22, 4);
            if (q.icon >= 0)
            {
                SmallImage.drawSmallImage(g, q.icon, ix + 11, yIcon + 10,
                        0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
            g.setColor(0, 0.55f);
            g.fillRect(ix, yIcon + 15, 22, 7);
            mFont.tahoma_7.drawString(g, "x" + q.soLuong, ix + 11, yIcon + 14, mFont.CENTER);
        }

        /// <summary>Dải dưới cùng: quà hôm nay của thẻ đang dùng và nút nhận.</summary>
        private void veDaiNhanNgay(mGraphics g)
        {
            int y = yThan + caoThan - CAO_DAI_NHAN;
            veKhungBo(g, xPhai, y, rongPhai, CAO_DAI_NHAN, MAU_THE, 0.95f, MAU_VIEN, 0.7f, 1);
            GoiThe dung = goiDangDung();
            if (dung == null)
            {
                mFont.tahoma_7b_dark.drawString(g,
                        "Mua NRO Pass để nhận quà mỗi ngày và ưu đãi chỉ số suốt thời hạn",
                        xPhai + rongPhai / 2, y + CAO_DAI_NHAN / 2 - 5, mFont.CENTER);
                return;
            }
            mFont.tahoma_7b_dark.drawString(g, "Quà hôm nay:", xPhai + 8,
                    y + CAO_DAI_NHAN / 2 - 5, mFont.LEFT);
            int ix = xPhai + 12 + mFont.tahoma_7b_dark.getWidth("Quà hôm nay:");
            int yIcon = y + (CAO_DAI_NHAN - 22) / 2;
            int[] n = oNutNhanNgay();
            for (int i = 0; i < dung.quaNgay.Count && ix + 22 < n[0] - 4; i++)
            {
                veMotQua(g, dung.quaNgay[i], ix, yIcon);
                ix += 25;
            }
            if (theThang.daNhanHomNay)
            {
                mFont.tahoma_7.drawString(g, "ĐÃ NHẬN", n[0] + n[2] / 2, n[1] + n[3] / 2 - 5,
                        mFont.CENTER);
            }
            else
            {
                veNut(g, n[0], n[1], n[2], n[3], "Nhận", true);
            }
        }

        /// <summary>Chạm trong mục thẻ tháng.</summary>
        private void chamTheThang()
        {
            for (int i = 0; i < theThang.goi.Count; i++)
            {
                GoiThe gt = theThang.goi[i];
                int[] n = oNutMuaTheoThe(oThe(i));
                if (!cham(n[0], n[1], n[2], n[3]))
                {
                    continue;
                }
                if (theThang.bacDangCo > 0 && theThang.bacDangCo != gt.bac)
                {
                    return;
                }
                long bayGio = mSystem.currentTimeMillis();
                if (bacChoXacNhan == gt.bac && bayGio - lucChoXacNhan < HAN_XAC_NHAN)
                {
                    bacChoXacNhan = 0;
                    Service.gI().phucLoiMuaThe(gt.bac);
                }
                else
                {
                    bacChoXacNhan = gt.bac;
                    lucChoXacNhan = bayGio;
                }
                return;
            }
            int[] nn = oNutNhanNgay();
            if (goiDangDung() != null && !theThang.daNhanHomNay
                    && cham(nn[0], nn[1], nn[2], nn[3]))
            {
                Service.gI().phucLoiNhanThe();
            }
        }

        private void veCotPhai(mGraphics g)
        {
            Nhom n = nhom[nhomChon];

            g.setColor(MAU_THE, 0.9f);
            g.fillRect(xPhai, yThan, rongPhai, 18, 6);
            mFont.tahoma_7b_red.drawString(g, "Tích luỹ: " + n.tienDo,
                    xPhai + 8, yThan + 4, mFont.LEFT);
            mFont.tahoma_7.drawString(g, n.tenLoai,
                    xPhai + rongPhai - 8, yThan + 4, mFont.RIGHT);

            if (cuon > Math.max(0, n.moc.Count - soTheHien))
            {
                cuon = Math.max(0, n.moc.Count - soTheHien);
            }
            for (int i = cuon; i < n.moc.Count && i - cuon < soTheHien; i++)
            {
                veMotThe(g, n.moc[i], yThe + (i - cuon) * CAO_THE);
            }
            if (n.moc.Count > soTheHien)
            {
                // Lăn chuột hoặc kéo tay để xem tiếp, nên chỗ này chỉ còn vach
                // chỉ vị trí, không còn hai nút bấm.
                veVachCuon(g, n.moc.Count, soTheHien);
                int yn = y0 + cao - 20;
                mFont.tahoma_7.drawString(g,
                        (cuon + 1) + "-" + Math.min(n.moc.Count, cuon + soTheHien)
                        + "/" + n.moc.Count,
                        xPhai + rongPhai - 4, yn + 3, mFont.RIGHT);
            }
        }

        private void veMotThe(mGraphics g, Moc m, int y)
        {
            int h = CAO_THE - 5;
            bool xong = m.trangThai == 2;
            bool nhanDuoc = m.trangThai == 1;

            g.setColor(xong ? MAU_THE_MO : MAU_THE, 0.94f);
            g.fillRect(xPhai, y, rongPhai, h, 6);
            g.setColor(nhanDuoc ? MAU_CHON : MAU_THE_MO);
            g.drawRect(xPhai, y, rongPhai, h);
            if (nhanDuoc)
            {
                // Vach sang doc ben trai the dang mo: nhin luot qua ca danh sach
                // la thay ngay cai nao bam duoc.
                g.setColor(MAU_VIEN, 0.9f);
                g.fillRect(xPhai, y, 3, h);
            }

            mFont.tahoma_7b_red.drawString(g, m.nhan, xPhai + 9, y + 4, mFont.LEFT);

            int rongNut = 62;
            int ix = xPhai + 9;
            int yIcon = y + 16;
            for (int i = 0; i < m.qua.Count; i++)
            {
                if (ix + 24 > xPhai + rongPhai - rongNut - 6)
                {
                    mFont.tahoma_7.drawString(g, "…", ix + 4, yIcon + 6, mFont.LEFT);
                    break;
                }
                Qua q = m.qua[i];
                g.setColor(MAU_THE_MO, 0.85f);
                g.fillRect(ix, yIcon, 22, 22, 4);
                if (q.icon >= 0)
                {
                    SmallImage.drawSmallImage(g, q.icon, ix + 11, yIcon + 10,
                            0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
                // So luong nam duoi day o, tren mot dai toi: de chong len giua
                // icon thi che mat mon do.
                g.setColor(0, 0.55f);
                g.fillRect(ix, yIcon + 15, 22, 7);
                mFont.tahoma_7.drawString(g, "x" + q.soLuong,
                        ix + 11, yIcon + 14, mFont.CENTER);
                ix += 25;
            }

            int nx = xPhai + rongPhai - rongNut - 4;
            if (xong)
            {
                mFont.tahoma_7.drawString(g, "ĐÃ NHẬN",
                        nx + rongNut / 2, y + h / 2 - 5, mFont.CENTER);
            }
            else if (nhanDuoc)
            {
                veNut(g, nx, y + h / 2 - 9, rongNut, 18, "Nhận", true);
            }
            else
            {
                mFont.tahoma_7.drawString(g, catBot(m.moTaThieu, 18),
                        nx + rongNut / 2, y + h / 2 - 9, mFont.CENTER);
                veThanhTienDo(g, nx, y + h / 2 + 3, rongNut, m.phanTram);
            }
        }

        /// <summary>Thanh tien do mong, cho biet con bao xa toi moc.</summary>
        private void veThanhTienDo(mGraphics g, int x, int y, int w, int phanTram)
        {
            g.setColor(MAU_THE_MO, 0.9f);
            g.fillRect(x, y, w, 5, 3);
            int day = w * (phanTram < 0 ? 0 : (phanTram > 100 ? 100 : phanTram)) / 100;
            if (day > 0)
            {
                g.setColor(MAU_VIEN, 0.85f);
                g.fillRect(x, y, day, 5, 3);
            }
        }


        /// <summary>Nút bo góc; {@code sang} là nút hành động, nổi hơn.</summary>
        private void veNut(mGraphics g, int x, int y, int w, int h, string chu,
                bool sang)
        {
            // Khong con vet loe o dinh nut: tren nen kem no doc ra nhu mot duong
            // ke lac cho, khong ra hieu ung noi khoi.
            veKhungBo(g, x, y, w, h, sang ? MAU_CHON : MAU_THE,
                    sang ? 1f : 0.95f, MAU_VIEN, 0.85f, 1);
            mFont.tahoma_7b_dark.drawString(g, chu, x + w / 2, y + h / 2 - 5,
                    mFont.CENTER);
        }

        private static string catBot(string s, int toiDa)
        {
            if (s == null)
            {
                return "";
            }
            return s.Length <= toiDa ? s : s.Substring(0, toiDa - 1) + "…";
        }

        // ------------------------------------------------------------------
        //  Cuộn
        /// <summary>Vẽ vạch chỉ vị trí dọc theo mép phải danh sách.</summary>
        /// <remarks>
        /// Không bấm được vào vạch này — nó chỉ cho biết đang ở đâu trong
        /// danh sách. Thêm chỗ bấm vào một dải rộng 3 điểm thì ngón tay
        /// không trúng vào được.
        /// </remarks>
        private void veVachCuon(mGraphics g, int soDong, int soHien)
        {
            int xv = xPhai + rongPhai - 3;
            int caoRanh = soHien * CAO_THE;
            g.setColor(MAU_THE_MO, 0.8f);
            g.fillRect(xv, yThe, 3, caoRanh, 2);
            int caoTay = Math.max(12, caoRanh * soHien / soDong);
            int chay = caoRanh - caoTay;
            int toiDa = soDong - soHien;
            int dy = toiDa <= 0 ? 0 : chay * cuon / toiDa;
            g.setColor(MAU_VIEN, 0.9f);
            g.fillRect(xv, yThe + dy, 3, caoTay, 2);
        }

        /// <summary>Số dòng đang có trong danh sách bên phải.</summary>
        /// <remarks>
        /// Tách thành hàm riêng để phần bắt chạm gọi được ngay từ đầu, trước các
        /// phép kiểm tra danh sách rỗng ở dưới.
        /// </remarks>
        private int soDongCuon()
        {
            if (dangXemThe())
            {
                return 0;
            }
            return (nhomChon >= 0 && nhomChon < nhom.Count)
                    ? nhom[nhomChon].moc.Count : 0;
        }

        // ------------------------------------------------------------------
        /// <summary>Toạ độ y lúc nghìn mốc để đo đã kéo được bao xa.</summary>
        private int yMocKeo;

        /// <summary>Đang kéo dở danh sách.</summary>
        private bool dangKeo;

        /// <summary>Đã kéo quá ngưỡng nên lần nhả ngón này không tính là bấm.</summary>
        private bool daKeoXa;

        /// <summary>Kéo quá bao nhiêu điểm thì coi là kéo chứ không phải bấm.</summary>
        private const int NGUONG_KEO = 6;

        /// <summary>Cuộn danh sách theo lăn chuột và kéo tay.</summary>
        /// <remarks>
        /// <para>Gọi ở đầu phần bắt chạm, TRƯỚC chỗ thoát sớm khi chưa
        /// nhả ngón: kéo phải thấy nhích liên tục, đợi nhả ngón mới cuộn thì
        /// như cái nút bấm chậm.</para>
        ///
        /// <para>Khó ở chỗ <b>kéo và bấm cùng kết thúc bằng một lần nhả
        /// ngón</b>. Nên đo quãng đã đi so với chỗ đặt ngón; quá vài điểm là
        /// bỏ lần nhả đó, không thế thì cuộn xong ngón nhả đúng thẻ nào là
        /// nhấn luôn thẻ đó.</para>
        /// </remarks>
        private void cuonDanhSach(int soDong)
        {
            int toiDa = Math.max(0, soDong - soTheHien);
            if (toiDa <= 0)
            {
                cuon = 0;
                dangKeo = false;
                return;
            }
            if (cuon > toiDa)
            {
                cuon = toiDa;
            }

            // Lăn chuột: mỗi nhịp một dòng.
            if (GameCanvas.pXYScrollMouse != 0
                    && trongVungCuon(GameCanvas.pxMouse, GameCanvas.pyMouse))
            {
                cuon += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
            }

            // Kéo tay.
            if (GameCanvas.isPointerDown)
            {
                if (!dangKeo)
                {
                    if (!trongVungCuon(GameCanvas.pxFirst, GameCanvas.pyFirst))
                    {
                        return;     // đặt ngón ngoài danh sách thì không cuộn
                    }
                    dangKeo = true;
                    daKeoXa = false;
                    yMocKeo = GameCanvas.pyFirst;
                }
                if (Math.abs(GameCanvas.py - GameCanvas.pyFirst) > NGUONG_KEO)
                {
                    daKeoXa = true;
                }
                // Kéo xuống là xem dòng ở trên, giống mọi danh sách cảm ứng.
                int buoc = (GameCanvas.py - yMocKeo) / CAO_THE;
                if (buoc != 0)
                {
                    cuon -= buoc;
                    yMocKeo += buoc * CAO_THE;
                }
            }
            else
            {
                dangKeo = false;
            }

            if (cuon < 0)
            {
                cuon = 0;
            }
            else if (cuon > toiDa)
            {
                cuon = toiDa;
            }
        }

        /// <summary>Điểm này có nằm trong vùng danh sách cuộn được không.</summary>
        private bool trongVungCuon(int x, int y)
        {
            return x >= xPhai && x <= xPhai + rongPhai
                    && y >= yThe && y <= yThe + caoVungThe;
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
            // Nuot MOI cham khi dang mo, ke ca cham ra ngoai khung.
            tinhBoCuc();
            cuonDanhSach(soDongCuon());
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            // Nhả ngón sau một nhát kéo: không tính là bấm vào thẻ bên dưới.
            if (daKeoXa)
            {
                daKeoXa = false;
                return true;
            }

            if (cham(x0 + rong - 20, y0 + 4, 16, 14))
            {
                dong();
                return true;
            }
            if (soMucChoNhan() > 0)
            {
                int[] nn = oNutNhanNhanh();
                if (cham(nn[0], nn[1], nn[2], nn[3]))
                {
                    Service.gI().phucLoiNhanNhanh();
                    return true;
                }
            }
            if (soMuc() == 0)
            {
                return true;
            }
            int lech = coThe() ? 1 : 0;
            for (int i = 0; i < soMuc(); i++)
            {
                int yy = yThan + 4 + i * (CAO_MUC + 3);
                if (yy + CAO_MUC > yThan + caoThan - 2)
                {
                    break;
                }
                if (cham(xTrai + 3, yy, RONG_TRAI - 6, CAO_MUC))
                {
                    if (coThe() && i == 0)
                    {
                        xemThe = true;
                    }
                    else
                    {
                        xemThe = false;
                        nhomChon = i - lech;
                    }
                    bacChoXacNhan = 0;
                    cuon = 0;
                    return true;
                }
            }

            if (dangXemThe())
            {
                chamTheThang();
                return true;
            }
            if (nhom.Count == 0)
            {
                return true;
            }
            Nhom n = nhom[nhomChon];
            for (int i = cuon; i < n.moc.Count && i - cuon < soTheHien; i++)
            {
                int y = yThe + (i - cuon) * CAO_THE;
                int h = CAO_THE - 4;
                if (n.moc[i].trangThai == 1
                        && cham(xPhai + rongPhai - 62, y + h / 2 - 9, 60, 18))
                {
                    Service.gI().phucLoiNhan(n.moc[i].id);
                    return true;
                }
            }
            return true;
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
