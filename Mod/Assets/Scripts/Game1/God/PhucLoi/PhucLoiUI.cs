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

        /// <summary>Một ô quà của NRO Pass (một cấp, một hàng).</summary>
        public class OPass
        {
            /// <summary>0 chưa tới · 1 nhận được · 2 đã nhận · 3 đã tới nhưng khoá.</summary>
            public int trangThai;
            public readonly List<Qua> qua = new List<Qua>();
        }

        /// <summary>Toàn bộ NRO Pass, máy chủ gửi ở cuối gói phúc lợi.</summary>
        public class NroPass
        {
            public string ten = "NRO Pass";
            public string moTaMua = string.Empty;
            public int soCap;
            public int diemMoiCap = 1;
            public long diem;
            public bool caoCap;
            public string giaCaoCap = string.Empty;
            public string uuDaiCaoCap = string.Empty;
            public string nguonDiem = string.Empty;
            public string soDu = string.Empty;
            /// <summary>Ô hàng Miễn phí, chỉ số 0 là cấp 1.</summary>
            public OPass[] mienPhi = new OPass[0];
            /// <summary>Ô hàng Cao cấp.</summary>
            public OPass[] caoCapO = new OPass[0];

            public int capDat()
            {
                int c = (int) (diem / (diemMoiCap < 1 ? 1 : diemMoiCap));
                return c > soCap ? soCap : c;
            }
        }

        private readonly List<Nhom> nhom = new List<Nhom>();
        private int nhomChon;
        private int cuon;

        /// <summary>NRO Pass; <c>null</c> khi máy chủ cũ không gửi.</summary>
        private NroPass pass;

        /// <summary>
        /// Đang xem NRO Pass thay cho một nhóm mốc. Mở bảng lần đầu là vào
        /// thẳng mục này: nó đứng đầu cột.
        /// </summary>
        private bool xemThe = true;

        /// <summary>Đang chờ bấm lần hai để xác nhận mở khoá cao cấp.</summary>
        private bool choXacNhan;

        /// <summary>Lúc bấm lần một; quá <see cref="HAN_XAC_NHAN"/> thì phải bấm lại từ đầu.</summary>
        private long lucChoXacNhan;

        /// <summary>
        /// Bấm "Mở khoá" lần một chỉ đổi nút thành "Bấm lần nữa để mua"; lần hai
        /// mới gửi. Mua là trừ tiền thật, một cú chạm lỡ tay không được tốn tiền.
        /// </summary>
        private const long HAN_XAC_NHAN = 4000L;

        /// <summary>Có mục NRO Pass trên cột trái hay không.</summary>
        private bool coThe()
        {
            return pass != null && pass.soCap > 0;
        }

        /// <summary>Số dòng trên cột trái: NRO Pass (nếu có) rồi tới các nhóm.</summary>
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

        /// <summary>Một dòng trong popup tổng quà.</summary>
        public class QuaDaNhan
        {
            public int icon;
            public int soLuong;
            public string ten;
        }

        private readonly List<QuaDaNhan> tongKet = new List<QuaDaNhan>();
        private bool hienTongKet;

        /// <summary>Lúc bấm Nhận nhanh; 0 là không chờ gì.</summary>
        private long lucBamNhanNhanh;

        /// <summary>
        /// Bấm Nhận nhanh mà quá chừng này không có trả lời thì báo máy chủ cũ.
        /// </summary>
        /// <remarks>
        /// Máy chủ chưa cập nhật hiểu gói nhận nhanh thành gói nhận mốc, đọc
        /// thiếu byte rồi im lặng bỏ qua — người chơi bấm mãi không thấy gì.
        /// </remarks>
        private const long HAN_CHO_NHAN_NHANH = 5000L;

        /// <summary>
        /// Máy chủ mới đã trả dữ liệu (có mục NRO Pass): thôi chờ. Có quà thì
        /// mở popup.
        /// </summary>
        public void nhanTongKet(List<QuaDaNhan> ds)
        {
            lucBamNhanNhanh = 0;
            if (ds == null || ds.Count == 0)
            {
                return;
            }
            tongKet.Clear();
            tongKet.AddRange(ds);
            hienTongKet = true;
        }

        private void veTongKet(mGraphics g)
        {
            g.setColor(0, 0.5f);
            g.fillRect(x0, y0, rong, cao, BO_GOC);

            const int caoDong = 26;
            int w = Math.min(rong - 40, 280);
            int soDongToiDa = Math.max(1, (cao - 90) / caoDong);
            int soDong = Math.min(tongKet.Count, soDongToiDa);
            bool thua = tongKet.Count > soDong;
            int h = 26 + soDong * caoDong + (thua ? 14 : 0) + 34;
            int x = x0 + (rong - w) / 2;
            int y = y0 + (cao - h) / 2;

            veKhungBo(g, x, y, w, h, MAU_NEN, 1f, MAU_VIEN, 1f, 2);
            g.setColor(MAU_NHAN_NHANH, 1f);
            g.fillRect(x + 2, y + 2, w - 4, 20, BO_GOC - 1);
            mFont.tahoma_7b_white.drawString(g, "ĐÃ NHẬN", x + w / 2, y + 6, mFont.CENTER,
                    mFont.tahoma_7b_dark);

            int yy = y + 26;
            for (int i = 0; i < soDong; i++)
            {
                QuaDaNhan q = tongKet[i];
                g.setColor(i % 2 == 0 ? MAU_THE : MAU_THE_MO, 0.95f);
                g.fillRect(x + 6, yy, w - 12, caoDong - 2, 4);
                if (q.icon >= 0)
                {
                    SmallImage.drawSmallImage(g, q.icon, x + 20, yy + caoDong / 2 - 1, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
                mFont.tahoma_7b_dark.drawString(g, catBot(q.ten, 26), x + 36, yy + 7, mFont.LEFT);
                mFont.tahoma_7b_red.drawString(g, "x" + q.soLuong, x + w - 14, yy + 7, mFont.RIGHT);
                yy += caoDong;
            }
            if (thua)
            {
                mFont.tahoma_7.drawString(g, "… và " + (tongKet.Count - soDong) + " món khác",
                        x + w / 2, yy, mFont.CENTER);
                yy += 14;
            }
            veNut(g, x + w / 2 - 40, yy + 6, 80, 20, "Đóng", true);
        }

        /// <summary>Nạp NRO Pass — gọi TRƯỚC <see cref="nhanDuLieu"/>.</summary>
        /// <remarks>
        /// Lần đầu có dữ liệu thì cuộn tới cấp đang làm; các lần làm mới sau
        /// (15 giây một lần, hay sau khi nhận) giữ nguyên chỗ đang xem.
        /// </remarks>
        public void nhanPass(NroPass p)
        {
            if (pass == null && p != null)
            {
                canCanPass = true;
            }
            pass = p;
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
            if (pass != null)
            {
                truotPass();
            }
            if (lucBamNhanNhanh > 0
                    && mSystem.currentTimeMillis() - lucBamNhanNhanh > HAN_CHO_NHAN_NHANH)
            {
                lucBamNhanNhanh = 0;
                GameScr.info1.addInfo("Máy chủ chưa hỗ trợ Nhận nhanh — cần cập nhật máy chủ.", 0);
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

            // Chu trang, bong nau dam: chu do tren dai cam chim han vao nen.
            mFont.tahoma_7b_white.drawString(g, "PHÚC LỢI",
                    x0 + rong / 2, y0 + 4, mFont.CENTER, mFont.tahoma_7b_dark);
            veNut(g, x0 + rong - 20, y0 + 4, 16, 14, "X", false);
            if (soMucChoNhan() > 0)
            {
                veNutNhanNhanh(g);
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
                vePass(g);
            }
            else
            {
                veCotPhai(g);
            }
            if (hienTongKet)
            {
                veTongKet(g);
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
                    if (soOPassNhanDuoc() > 0)
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
            int n = soOPassNhanDuoc();
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
            int w = mFont.tahoma_7b_white.getWidth("Nhận nhanh") + 20;
            return new int[] { x0 + 6, y0 + 3, w, 17 };
        }

        /// <summary>Xanh lá tươi của nút Nhận nhanh, và viền đậm của nó.</summary>
        private static readonly int MAU_NHAN_NHANH = rgb(0x2E, 0xA8, 0x4A);
        private static readonly int MAU_NHAN_NHANH_VIEN = rgb(0x17, 0x62, 0x2A);

        /// <summary>
        /// Nút Nhận nhanh: xanh lá giữa dải cam, chữ trắng viền đậm, quầng sáng
        /// thở nhẹ quanh nút.
        /// </summary>
        /// <remarks>
        /// Màu khác hẳn mọi thứ trên bảng (toàn cam, kem, nâu) — liếc là thấy.
        /// Quầng thở chứ không nhấp nháy: nhấp nháy trên một bảng người ta ngồi
        /// đọc lâu thì chỉ làm mỏi mắt.
        /// </remarks>
        private void veNutNhanNhanh(mGraphics g)
        {
            int[] n = oNutNhanNhanh();
            float tho = 0.5f + 0.5f * (float) System.Math.Sin(mSystem.currentTimeMillis() / 300.0);
            g.setColor(0xB8FFB0, 0.18f + 0.22f * tho);
            g.fillRect(n[0] - 2, n[1] - 2, n[2] + 4, n[3] + 4, 7);
            veKhungBo(g, n[0], n[1], n[2], n[3], MAU_NHAN_NHANH, 1f, MAU_NHAN_NHANH_VIEN, 1f, 2);
            mFont.tahoma_7b_white.drawString(g, "Nhận nhanh", n[0] + n[2] / 2, n[1] + 3,
                    mFont.CENTER, mFont.tahoma_7b_dark);
        }

        /// <summary>Số ô NRO Pass đang nhận được.</summary>
        private int soOPassNhanDuoc()
        {
            if (pass == null)
            {
                return 0;
            }
            int n = 0;
            for (int i = 0; i < pass.soCap; i++)
            {
                if (pass.mienPhi[i].trangThai == 1)
                {
                    n++;
                }
                if (pass.caoCapO[i].trangThai == 1)
                {
                    n++;
                }
            }
            return n;
        }

        // ------------------------------------------------------------------
        //  NRO Pass — dải cấp trải ngang kiểu Royale Pass
        // ------------------------------------------------------------------
        /// <summary>Vàng cát: nền hàng Cao cấp, nhãn Cao cấp.</summary>
        private static readonly int MAU_THE_VANG = rgb(0xE0, 0x9A, 0x3E);

        /// <summary>Cam cháy: dòng NRO Pass đang chọn, thanh tiến độ, nút mở khoá.</summary>
        private static readonly int MAU_THE_CAO = rgb(0xD9, 0x6A, 0x12);

        /// <summary>Nền ô hàng Cao cấp: kem ánh vàng, tách hẳn với hàng Miễn phí.</summary>
        private static readonly int MAU_O_CAO_CAP = rgb(0xFF, 0xE6, 0xAE);

        private const int CAO_DAU_PASS = 30;
        private const int CAO_DAY_PASS = 46;
        private const int RONG_NHAN_HANG = 60;
        private const int RONG_NUT_LAT_P = 15;
        private const int KHE_O_PASS = 6;

        /// <summary>Quãng đã cuộn ngang và quãng đang nhắm tới (điểm ảnh).</summary>
        private float cuonPass;
        private float cuonPassDich;
        private bool dangKeoPass;
        private int xKeoTruocP;
        private int tongKeoP;

        /// <summary>Lần tới vẽ thì cuộn tới cấp đang làm — đặt khi vừa mở mục pass.</summary>
        private bool canCanPass = true;

        /// <summary>Vùng dải cấp: x, y, rộng, cao.</summary>
        private int[] vungPass()
        {
            int x = xPhai + RONG_NHAN_HANG + RONG_NUT_LAT_P + 6;
            int w = rongPhai - RONG_NHAN_HANG - RONG_NUT_LAT_P * 2 - 10;
            int y = yThan + CAO_DAU_PASS + 4;
            int h = caoThan - CAO_DAU_PASS - CAO_DAY_PASS - 10;
            return new int[] { x, y, w, h };
        }

        /// <summary>Cạnh một ô quà, co theo chiều cao có được.</summary>
        private int coOPass()
        {
            int[] v = vungPass();
            int s = (v[3] - 20 - 8) / 2 - 2;
            if (s > 54)
            {
                s = 54;
            }
            return s < 26 ? 26 : s;
        }

        private int buocPass()
        {
            return coOPass() + KHE_O_PASS;
        }

        private int yHangMienPhi()
        {
            return vungPass()[1] + 22;
        }

        private int yHangCaoCap()
        {
            return yHangMienPhi() + coOPass() + 6;
        }

        private float cuonPassToiDa()
        {
            if (pass == null)
            {
                return 0f;
            }
            float du = pass.soCap * buocPass() - KHE_O_PASS - vungPass()[2];
            return du > 0 ? du : 0f;
        }

        /// <summary>x của cột cấp thứ <paramref name="i"/> (0 là cấp 1), đã trừ quãng cuộn.</summary>
        private int xCotPass(int i)
        {
            return vungPass()[0] + i * buocPass() - (int) cuonPass;
        }

        private int[] oNutLatPass(bool trai)
        {
            int[] v = vungPass();
            int y = yHangMienPhi();
            int h = coOPass() * 2 + 6;
            int x = trai ? (v[0] - RONG_NUT_LAT_P - 3) : (v[0] + v[2] + 3);
            return new int[] { x, y, RONG_NUT_LAT_P, h };
        }

        private int[] oNutMoKhoa()
        {
            int y = yThan + caoThan - CAO_DAY_PASS;
            int w = 176;
            return new int[] { xPhai + rongPhai - w - 6, y + 18, w, 22 };
        }

        /// <summary>Đưa cột cấp <paramref name="cap"/> (1..) vào giữa tầm nhìn.</summary>
        private void canGiuaCap(int cap, bool ngay)
        {
            int[] v = vungPass();
            float dich = (cap - 1) * buocPass() - (v[2] - coOPass()) / 2f;
            float tran = cuonPassToiDa();
            cuonPassDich = dich < 0 ? 0 : (dich > tran ? tran : dich);
            if (ngay)
            {
                cuonPass = cuonPassDich;
            }
        }

        /// <summary>Cấp nên nhìn thấy đầu tiên: ô nhận được sớm nhất, không thì cấp đang làm.</summary>
        private int capNenXem()
        {
            for (int i = 0; i < pass.soCap; i++)
            {
                if (pass.mienPhi[i].trangThai == 1 || pass.caoCapO[i].trangThai == 1)
                {
                    return i + 1;
                }
            }
            int c = pass.capDat() + 1;
            return c > pass.soCap ? pass.soCap : c;
        }

        private void vePass(mGraphics g)
        {
            NroPass p = pass;
            if (canCanPass)
            {
                canCanPass = false;
                canGiuaCap(capNenXem(), true);
            }

            // ---- Dau: ten + mua, ben phai cap va thanh diem ----
            veKhungBo(g, xPhai, yThan, rongPhai, CAO_DAU_PASS, MAU_THE, 0.95f, MAU_VIEN, 0.7f, 1);
            mFont.tahoma_7b_red.drawString(g, p.ten, xPhai + 8, yThan + 3, mFont.LEFT);
            mFont.tahoma_7.drawString(g, p.moTaMua, xPhai + 8, yThan + 15, mFont.LEFT);

            int capDat = p.capDat();
            bool kich = capDat >= p.soCap;
            long trongCap = kich ? p.diemMoiCap : p.diem % p.diemMoiCap;
            int rThanh = 120;
            int xThanh = xPhai + rongPhai - rThanh - 8;
            string nhanCap = "CẤP " + capDat + "/" + p.soCap;
            int wCap = mFont.tahoma_7b_white.getWidth(nhanCap) + 12;
            int xCap = xThanh - wCap - 6;
            veKhungBo(g, xCap, yThan + 6, wCap, 18, MAU_THE_CAO, 1f, MAU_VIEN, 1f, 1);
            mFont.tahoma_7b_white.drawString(g, nhanCap, xCap + wCap / 2, yThan + 9, mFont.CENTER,
                    mFont.tahoma_7b_dark);
            g.setColor(MAU_THE_MO, 1f);
            g.fillRect(xThanh, yThan + 17, rThanh, 6, 3);
            int day = (int) (rThanh * trongCap / (p.diemMoiCap < 1 ? 1 : p.diemMoiCap));
            if (day > 0)
            {
                g.setColor(MAU_THE_CAO, 1f);
                g.fillRect(xThanh, yThan + 17, day > rThanh ? rThanh : day, 6, 3);
            }
            mFont.tahoma_7.drawString(g, kich ? "Đã đạt cấp tối đa"
                    : (trongCap + "/" + p.diemMoiCap + " điểm"),
                    xThanh + rThanh, yThan + 4, mFont.RIGHT);

            // ---- Nhan hai hang ----
            int s = coOPass();
            int yM = yHangMienPhi();
            int yC = yHangCaoCap();
            veKhungBo(g, xPhai, yM, RONG_NHAN_HANG, s, MAU_THE, 1f, MAU_VIEN, 0.8f, 1);
            mFont.tahoma_7b_dark.drawString(g, "MIỄN PHÍ", xPhai + RONG_NHAN_HANG / 2,
                    yM + s / 2 - 5, mFont.CENTER);
            veKhungBo(g, xPhai, yC, RONG_NHAN_HANG, s, MAU_THE_VANG, 1f, MAU_THE_CAO, 1f, 1);
            mFont.tahoma_7b_white.drawString(g, "CAO CẤP", xPhai + RONG_NHAN_HANG / 2,
                    yC + s / 2 - (p.caoCap ? 5 : 10), mFont.CENTER, mFont.tahoma_7b_dark);
            if (!p.caoCap)
            {
                veKhoa(g, xPhai + RONG_NHAN_HANG / 2, yC + s / 2 + 7);
            }

            // ---- Dai cap: cat dung vung, ve cac cot ----
            int[] v = vungPass();
            g.setClip(v[0], v[1], v[2], v[3]);
            int buoc = buocPass();
            // Duong tien do noi cac so cap.
            int yDuong = v[1] + 8;
            int xDau = xCotPass(0) + s / 2;
            int xCuoi = xCotPass(p.soCap - 1) + s / 2;
            g.setColor(MAU_THE_MO, 1f);
            g.fillRect(xDau, yDuong, xCuoi - xDau, 4, 2);
            if (capDat > 0)
            {
                int xDat = xCotPass(capDat - 1) + s / 2;
                g.setColor(MAU_THE_CAO, 1f);
                g.fillRect(xDau, yDuong, xDat - xDau, 4, 2);
            }
            for (int i = 0; i < p.soCap; i++)
            {
                int x = xCotPass(i);
                if (x + s < v[0] || x > v[0] + v[2])
                {
                    continue;
                }
                bool dat = i < capDat;
                string so = string.Empty + (i + 1);
                int wS = mFont.tahoma_7b_white.getWidth(so) + 8;
                if (wS < 18)
                {
                    wS = 18;
                }
                veKhungBo(g, x + s / 2 - wS / 2, v[1] + 2, wS, 16, dat ? MAU_THE_CAO : MAU_THE_MO, 1f,
                        MAU_VIEN, dat ? 1f : 0.6f, 1);
                (dat ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark).drawString(g, so, x + s / 2,
                        v[1] + 4, mFont.CENTER);
                veOPass(g, p.mienPhi[i], x, yM, s, false);
                veOPass(g, p.caoCapO[i], x, yC, s, true);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            // ---- Hai nut lat ----
            veNutLatP(g, true, cuonPassDich > 0.5f);
            veNutLatP(g, false, cuonPassDich < cuonPassToiDa() - 0.5f);

            // ---- Day: nguon diem, so du, nut mo khoa ----
            int yD = yThan + caoThan - CAO_DAY_PASS;
            veKhungBo(g, xPhai, yD, rongPhai, CAO_DAY_PASS, MAU_THE, 0.95f, MAU_VIEN, 0.7f, 1);
            int[] nut = oNutMoKhoa();
            int rChu = nut[0] - xPhai - 14;
            string[] dongNguon = mFont.tahoma_7.splitFontArray(p.nguonDiem ?? string.Empty, rChu);
            for (int i = 0; i < dongNguon.Length && i < 2; i++)
            {
                mFont.tahoma_7.drawString(g, dongNguon[i], xPhai + 8, yD + 4 + i * 11, mFont.LEFT);
            }
            mFont.tahoma_7_grey.drawString(g, catBot(p.soDu, 60), xPhai + 8, yD + 30, mFont.LEFT);

            if (p.caoCap)
            {
                veKhungBo(g, nut[0], nut[1], nut[2], nut[3], MAU_THE_VANG, 1f, MAU_THE_CAO, 1f, 1);
                mFont.tahoma_7b_white.drawString(g, "ĐÃ MỞ CAO CẤP", nut[0] + nut[2] / 2, nut[1] + 5,
                        mFont.CENTER, mFont.tahoma_7b_dark);
            }
            else
            {
                bool cho = choXacNhan && mSystem.currentTimeMillis() - lucChoXacNhan < HAN_XAC_NHAN;
                float tho = 0.5f + 0.5f * (float) System.Math.Sin(mSystem.currentTimeMillis() / 320.0);
                g.setColor(0xFFD27A, 0.2f + 0.25f * tho);
                g.fillRect(nut[0] - 2, nut[1] - 2, nut[2] + 4, nut[3] + 4, 7);
                veKhungBo(g, nut[0], nut[1], nut[2], nut[3], MAU_THE_CAO, 1f, rgb(0x7A, 0x33, 0x08), 1f, 2);
                mFont.tahoma_7b_white.drawString(g, cho ? "Bấm lần nữa để mua" : ("Mở Cao cấp · " + p.giaCaoCap),
                        nut[0] + nut[2] / 2, nut[1] + 5, mFont.CENTER, mFont.tahoma_7b_dark);
            }
            if (!string.IsNullOrEmpty(p.uuDaiCaoCap))
            {
                mFont.tahoma_7_blue.drawString(g, catBot("Cao cấp: " + p.uuDaiCaoCap, 60),
                        nut[0] + nut[2], yD + 5, mFont.RIGHT);
            }
        }

        /// <summary>Một ô quà: nền theo hàng, icon món đầu, số lượng, và dấu trạng thái.</summary>
        private void veOPass(mGraphics g, OPass o, int x, int y, int s, bool caoCap)
        {
            bool nhanDuoc = o.trangThai == 1;
            int vien = nhanDuoc ? rgb(0x2E, 0xA8, 0x4A) : (caoCap ? MAU_THE_CAO : MAU_VIEN);
            if (nhanDuoc)
            {
                float tho = 0.5f + 0.5f * (float) System.Math.Sin(mSystem.currentTimeMillis() / 250.0);
                g.setColor(0x9CFF8A, 0.25f + 0.3f * tho);
                g.fillRect(x - 2, y - 2, s + 4, s + 4, 7);
            }
            veKhungBo(g, x, y, s, s, caoCap ? MAU_O_CAO_CAP : MAU_THE, 1f, vien,
                    nhanDuoc ? 1f : 0.8f, nhanDuoc ? 2 : 1);
            if (o.qua.Count > 0)
            {
                Qua q = o.qua[0];
                if (q.icon >= 0)
                {
                    SmallImage.drawSmallImage(g, q.icon, x + s / 2, y + s / 2 - 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
                string sl = "x" + q.soLuong;
                int wSl = mFont.tahoma_7b_dark.getWidth(sl) + 6;
                g.setColor(MAU_VIEN, 0.9f);
                g.fillRect(x + s - wSl - 2, y + s - 13, wSl, 11, 3);
                mFont.tahoma_7b_white.drawString(g, sl, x + s - 2 - wSl / 2, y + s - 13, mFont.CENTER);
                if (o.qua.Count > 1)
                {
                    string them = "+" + (o.qua.Count - 1);
                    int wT = mFont.tahoma_7b_white.getWidth(them) + 6;
                    g.setColor(MAU_THE_CAO, 1f);
                    g.fillRect(x + s - wT - 2, y + 2, wT, 11, 3);
                    mFont.tahoma_7b_white.drawString(g, them, x + s - 2 - wT / 2, y + 2, mFont.CENTER);
                }
            }
            if (o.trangThai == 0)
            {
                // Chua toi: phu mo, nhin la biet con xa.
                g.setColor(MAU_THE, 0.45f);
                g.fillRect(x + 1, y + 1, s - 2, s - 2, BO_GOC - 1);
            }
            else if (o.trangThai == 2)
            {
                g.setColor(0, 0.3f);
                g.fillRect(x + 1, y + 1, s - 2, s - 2, BO_GOC - 1);
                veDauTich(g, x + s / 2, y + s / 2);
            }
            else if (o.trangThai == 3)
            {
                g.setColor(0, 0.35f);
                g.fillRect(x + 1, y + 1, s - 2, s - 2, BO_GOC - 1);
                veKhoa(g, x + s / 2, y + s / 2);
            }
            else
            {
                // Nhan duoc: dai "NHAN" duoi day o.
                g.setColor(rgb(0x2E, 0xA8, 0x4A), 1f);
                g.fillRect(x + 3, y + s - 12, s - 6, 10, 3);
                mFont.tahoma_7b_white.drawString(g, "NHẬN", x + s / 2, y + s - 13, mFont.CENTER);
            }
        }

        /// <summary>Dấu tích xanh, vẽ bằng hai nét chéo.</summary>
        private static void veDauTich(mGraphics g, int tamX, int tamY)
        {
            g.setColor(0xFFFFFF, 1f);
            g.fillRect(tamX - 9, tamY - 9, 18, 18, 9);
            g.setColor(0x2E9E48, 1f);
            for (int i = 0; i < 4; i++)
            {
                g.fillRect(tamX - 5 + i, tamY - 1 + i, 2, 2);
            }
            for (int i = 0; i < 7; i++)
            {
                g.fillRect(tamX - 2 + i, tamY + 2 - i, 2, 2);
            }
        }

        /// <summary>Ổ khoá nhỏ: thân chữ nhật, quai tròn.</summary>
        private static void veKhoa(mGraphics g, int tamX, int tamY)
        {
            g.setColor(0x3A2712, 1f);
            g.fillRect(tamX - 5, tamY - 7, 10, 7, 4);
            g.setColor(0xFFE6AE, 1f);
            g.fillRect(tamX - 3, tamY - 5, 6, 5, 3);
            g.setColor(0x3A2712, 1f);
            g.fillRect(tamX - 7, tamY - 2, 14, 10, 2);
            g.setColor(0xFFC861, 1f);
            g.fillRect(tamX - 1, tamY + 1, 2, 4);
        }

        private void veNutLatP(mGraphics g, bool trai, bool con)
        {
            int[] n = oNutLatPass(trai);
            float mo = con ? 1f : 0.4f;
            veKhungBo(g, n[0], n[1], n[2], n[3], MAU_THE, mo, con ? MAU_THE_CAO : MAU_VIEN, mo, 1);
            int xT = n[0] + n[2] / 2;
            int yT = n[1] + n[3] / 2;
            g.setColor(con ? MAU_THE_CAO : MAU_VIEN, con ? 1f : 0.5f);
            for (int i = 0; i < 4; i++)
            {
                int cao = (4 - i) * 2 - 1;
                int xv = trai ? (xT - 2 + i) : (xT + 1 - i);
                g.fillRect(xv, yT - cao / 2, 1, cao);
            }
        }

        /// <summary>
        /// Kéo ngang dải cấp, lăn chuột, và kẹp trong biên. Gọi ở đầu phần bắt
        /// chạm, TRƯỚC chỗ thoát sớm khi chưa nhả ngón: kéo phải thấy trượt liền.
        /// </summary>
        private void cuonPassCham()
        {
            int[] v = vungPass();
            int yTren = v[1];
            int cao = v[3];
            if (GameCanvas.pXYScrollMouse != 0
                    && GameCanvas.pxMouse >= v[0] && GameCanvas.pxMouse <= v[0] + v[2]
                    && GameCanvas.pyMouse >= yTren && GameCanvas.pyMouse <= yTren + cao)
            {
                cuonPassDich += (GameCanvas.pXYScrollMouse > 0 ? -1 : 1) * buocPass() * 2;
            }
            if (GameCanvas.isPointerDown)
            {
                if (!dangKeoPass)
                {
                    if (GameCanvas.pxFirst >= v[0] && GameCanvas.pxFirst <= v[0] + v[2]
                            && GameCanvas.pyFirst >= yTren && GameCanvas.pyFirst <= yTren + cao)
                    {
                        dangKeoPass = true;
                        xKeoTruocP = GameCanvas.px;
                        tongKeoP = 0;
                    }
                }
                else
                {
                    int dx = GameCanvas.px - xKeoTruocP;
                    xKeoTruocP = GameCanvas.px;
                    tongKeoP += dx < 0 ? -dx : dx;
                    cuonPassDich -= dx;
                    cuonPass = cuonPassDich;
                }
            }
            else
            {
                dangKeoPass = false;
            }
            float tran = cuonPassToiDa();
            if (cuonPassDich > tran)
            {
                cuonPassDich = tran;
            }
            if (cuonPassDich < 0)
            {
                cuonPassDich = 0;
            }
        }

        /// <summary>Trượt dần tới chỗ nhắm — gọi mỗi khung hình.</summary>
        private void truotPass()
        {
            float lech = cuonPassDich - cuonPass;
            if (lech > 0.5f || lech < -0.5f)
            {
                cuonPass += lech * 0.3f;
            }
            else
            {
                cuonPass = cuonPassDich;
            }
        }

        /// <summary>Chạm trong mục NRO Pass (đã nhả ngón, không phải kéo).</summary>
        private void chamPass()
        {
            if (tongKeoP > 6)
            {
                tongKeoP = 0;
                return;
            }
            tongKeoP = 0;
            for (int b = 0; b < 2; b++)
            {
                int[] n = oNutLatPass(b == 0);
                if (cham(n[0], n[1], n[2], n[3]))
                {
                    cuonPassDich += (b == 0 ? -1 : 1) * buocPass() * 3;
                    float tran = cuonPassToiDa();
                    cuonPassDich = cuonPassDich < 0 ? 0 : (cuonPassDich > tran ? tran : cuonPassDich);
                    return;
                }
            }
            int[] nut = oNutMoKhoa();
            if (!pass.caoCap && cham(nut[0], nut[1], nut[2], nut[3]))
            {
                long bayGio = mSystem.currentTimeMillis();
                if (choXacNhan && bayGio - lucChoXacNhan < HAN_XAC_NHAN)
                {
                    choXacNhan = false;
                    Service.gI().phucLoiMoCaoCap();
                }
                else
                {
                    choXacNhan = true;
                    lucChoXacNhan = bayGio;
                }
                return;
            }
            int[] v = vungPass();
            int s = coOPass();
            for (int i = 0; i < pass.soCap; i++)
            {
                int x = xCotPass(i);
                if (x + s < v[0] || x > v[0] + v[2])
                {
                    continue;
                }
                for (int hang = 0; hang < 2; hang++)
                {
                    int y = hang == 0 ? yHangMienPhi() : yHangCaoCap();
                    if (!cham(x, y, s, s))
                    {
                        continue;
                    }
                    OPass o = hang == 0 ? pass.mienPhi[i] : pass.caoCapO[i];
                    if (o.trangThai == 1)
                    {
                        Service.gI().phucLoiNhanO(i + 1, hang);
                    }
                    else if (o.trangThai == 3)
                    {
                        GameScr.info1.addInfo("Mở Cao cấp để nhận ô này.", 0);
                    }
                    else if (o.trangThai == 0)
                    {
                        GameScr.info1.addInfo("Cần đạt cấp " + (i + 1) + ".", 0);
                    }
                    return;
                }
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
            if (hienTongKet)
            {
                if (GameCanvas.isPointerJustRelease)
                {
                    // Bam dau cung dong: popup chi de xem, khong co gi khac de bam.
                    GameCanvas.clearAllPointerEvent();
                    hienTongKet = false;
                }
                return true;
            }
            cuonDanhSach(soDongCuon());
            if (dangXemThe())
            {
                cuonPassCham();
            }
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
            // Nha ngon sau mot nhat keo ngang dai cap: khong tinh la bam.
            if (dangXemThe() && tongKeoP > 6)
            {
                tongKeoP = 0;
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
                    lucBamNhanNhanh = mSystem.currentTimeMillis();
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
                        canCanPass = true;
                    }
                    else
                    {
                        xemThe = false;
                        nhomChon = i - lech;
                    }
                    choXacNhan = false;
                    cuon = 0;
                    return true;
                }
            }

            if (dangXemThe())
            {
                chamPass();
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
