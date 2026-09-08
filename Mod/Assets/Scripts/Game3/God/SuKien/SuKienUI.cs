// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game3.God
{
    /// <summary>
    /// Màn hình Sự kiện — cột sự kiện bên trái, chi tiết bên phải.
    /// </summary>
    /// <remarks>
    /// <para>Dữ liệu lấy thẳng từ tab "Sự kiện" trên panel: vật phẩm rơi thêm từ
    /// quái và vật phẩm đổi ở Quy Lão. Sửa ở panel là trong game thấy ngay.</para>
    ///
    /// <para>Màn này chỉ để <b>xem</b> — không có nút nhận. Đồ rơi thì tự rơi khi
    /// đánh quái, đổi đồ thì tới NPC. Nên gói chỉ đi một chiều.</para>
    ///
    /// <para><b>Vì sao có bảng chi tiết riêng.</b> Một công thức đổi có thể cần bốn
    /// năm món; nhồi hết vào một dòng thì dòng đó dài quá bề ngang và bị cắt thành
    /// "…", người chơi không biết còn thiếu gì. Nên mỗi công thức có hai nút mở
    /// bảng liệt kê đủ: nguyên liệu cần và phần nhận được, kèm số lượng.</para>
    ///
    /// <para>Bố cục tính một lần trong <see cref="tinhBoCuc"/> rồi cả phần vẽ lẫn
    /// phần bắt chạm cùng đọc, tránh hai bên tính lệch nhau.</para>
    /// </remarks>
    public class SuKienUI
    {
        private static SuKienUI instance;

        public static SuKienUI getInstance()
        {
            return instance ?? (instance = new SuKienUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Dữ liệu
        // ------------------------------------------------------------------
        /// <summary>Một món kèm số lượng — dùng cho cả nguyên liệu và phần thưởng.</summary>
        public class VatPham
        {
            public int icon;
            public string ten;

            /// <summary>Số lượng, hoặc đầu dưới của khoảng với đồ rơi.</summary>
            public int soLuong;

            /// <summary>Đầu trên của khoảng; bằng <see cref="soLuong"/> nếu cố định.</summary>
            public int soLuongMax;

            /// <summary>Tỉ lệ rơi đã kèm dấu %; rỗng với nguyên liệu và quà đổi.</summary>
            public string tiLe;

            /// <summary>"x3" hoặc "x1–3" — viết gọn số lượng để đứng cuối dòng.</summary>
            public string soLuongChu()
            {
                if (soLuongMax > soLuong)
                {
                    return "x" + soLuong + "–" + soLuongMax;
                }
                return "x" + soLuong;
            }
        }

        /// <summary>Một dòng trong cột phải: đồ rơi, công thức đổi, hoặc tiêu đề nhóm.</summary>
        public class Dong
        {
            public int icon;
            public string ten;
            public string moTa;

            /// <summary>Dòng tiêu đề nhóm — vẽ khác, không bấm được.</summary>
            public bool laTieuDe;

            /// <summary>Có hai nút chi tiết hay không (chỉ công thức đổi mới có).</summary>
            public bool laCongThuc;

            /// <summary>Nguyên liệu cần để đổi.</summary>
            public readonly List<VatPham> yeuCau = new List<VatPham>();

            /// <summary>Nhận được gì.</summary>
            public readonly List<VatPham> qua = new List<VatPham>();

            /// <summary>Nếu món nhận được là một gói quà thì đây là những gì bên trong.</summary>
            /// <remarks>
            /// Tỉ lệ trong này là tỉ lệ bốc <b>giữa các món trong gói</b> — mở một
            /// gói ra đúng một món, nên cộng lại tròn 100%. Khác với tỉ lệ ở dòng
            /// đồ rơi, thứ đó là xác suất quái có nhả món ấy hay không.
            /// </remarks>
            public readonly List<VatPham> trongQua = new List<VatPham>();
        }

        public class SuKien
        {
            public string ten;
            public string khungGio;
            public string thongBao;
            public readonly List<Dong> doRoi = new List<Dong>();
            public readonly List<Dong> congThuc = new List<Dong>();
        }

        private readonly List<SuKien> ds = new List<SuKien>();
        private int chon;
        private int cuon;

        // ------------------------------------------------------------------
        //  Bảng chi tiết
        // ------------------------------------------------------------------
        /// <summary>Đang mở bảng chi tiết; lúc này cả màn dưới không nhận chạm.</summary>
        private bool moChiTiet;
        private string tieuDeChiTiet;
        private string phuDeChiTiet;
        private List<VatPham> dsChiTiet;

        /// <summary>Mục thứ hai của bảng — nội dung gói, có thể rỗng.</summary>
        private string tieuDeMuc2;
        private List<VatPham> dsChiTiet2;

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
        private const int CAO_MUC = 28;
        private const int CAO_DONG = 30;
        private const int LE = 6;

        /// <summary>Bề ngang một nút chi tiết trong dòng công thức.</summary>
        private const int RONG_NUT_CT = 56;

        /// <summary>Bề cao một dòng trong bảng chi tiết.</summary>
        private const int CAO_DONG_CT = 24;

        private int x0, y0, rong, cao;
        private int xTrai, yThan, caoThan;
        private int xPhai, rongPhai, yDong, caoVungDong;
        private int soDongHien;

        // Bố cục bảng chi tiết, tính cùng chỗ với phần còn lại.
        private int xCT, yCT, rongCT, caoCT;

        public void nhanDuLieu(List<SuKien> moi)
        {
            ds.Clear();
            if (moi != null)
            {
                ds.AddRange(moi);
            }
            if (chon >= ds.Count)
            {
                chon = 0;
                cuon = 0;
            }
            dangMo = true;
        }

        public void dong()
        {
            dangMo = false;
            dongChiTiet();
        }

        private void dongChiTiet()
        {
            moChiTiet = false;
            dsChiTiet = null;
            dsChiTiet2 = null;
        }

        /// <summary>Mở bảng chi tiết: một mục chính, kèm mục thứ hai nếu có.</summary>
        private void moBang(string tieuDe, string phuDe, List<VatPham> ds1,
                string tieuDe2, List<VatPham> ds2)
        {
            tieuDeChiTiet = tieuDe;
            phuDeChiTiet = phuDe;
            dsChiTiet = ds1;
            tieuDeMuc2 = tieuDe2;
            dsChiTiet2 = (ds2 != null && ds2.Count > 0) ? ds2 : null;
            moChiTiet = true;
        }

        /// <summary>Số dòng bảng chi tiết phải vẽ, kể cả dòng tiêu đề mục.</summary>
        private int soDongBang()
        {
            int n = dsChiTiet == null ? 1 : Math.max(1, dsChiTiet.Count);
            if (dsChiTiet2 != null)
            {
                n += dsChiTiet2.Count + 1;      // +1 cho dòng tiêu đề mục hai
            }
            return n;
        }

        /// <summary>Gộp đồ rơi và công thức thành một danh sách có tiêu đề xen giữa.</summary>
        /// <remarks>
        /// Dựng lại mỗi lần vẽ thay vì giữ sẵn: hai phần có thể rỗng, mà dòng
        /// tiêu đề chỉ nên hiện khi phần dưới nó có nội dung.
        /// </remarks>
        private List<Dong> dongHienThi()
        {
            List<Dong> ra = new List<Dong>();
            if (ds.Count == 0)
            {
                return ra;
            }
            SuKien sk = ds[chon];
            if (sk.doRoi.Count > 0)
            {
                ra.Add(tieuDe("Vật phẩm rơi từ quái"));
                ra.AddRange(sk.doRoi);
            }
            if (sk.congThuc.Count > 0)
            {
                ra.Add(tieuDe("Vật phẩm đổi ở Quy Lão"));
                ra.AddRange(sk.congThuc);
            }
            if (ra.Count == 0)
            {
                ra.Add(tieuDe("Sự kiện này chưa khai đồ rơi hay vật phẩm đổi"));
            }
            return ra;
        }

        private static Dong tieuDe(string chu)
        {
            Dong d = new Dong();
            d.laTieuDe = true;
            d.icon = -1;
            d.ten = chu;
            d.moTa = null;
            return d;
        }

        private void tinhBoCuc()
        {
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
            yDong = yThan + 34;
            caoVungDong = caoThan - 34;
            soDongHien = Math.max(1, caoVungDong / CAO_DONG);

            rongCT = Math.max(200, Math.min(320, rong - 60));
            // 24 tieu de + 14 phu de + cac dong + 26 nut Dong
            caoCT = 24 + 14 + soDongBang() * CAO_DONG_CT + 26;
            caoCT = Math.min(caoCT, GameCanvas.h - 20);
            xCT = (GameCanvas.w - rongCT) / 2;
            yCT = (GameCanvas.h - caoCT) / 2;
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

            mFont.tahoma_7b_red.drawString(g, "SỰ KIỆN",
                    x0 + rong / 2, y0 + 4, mFont.CENTER);
            veNut(g, x0 + rong - 20, y0 + 4, 16, 14, "X", false);

            if (ds.Count == 0)
            {
                mFont.tahoma_7b_dark.drawString(g,
                        "Hiện không có sự kiện nào đang diễn ra",
                        x0 + rong / 2, y0 + cao / 2 - 5, mFont.CENTER);
                return;
            }
            veCotTrai(g);
            veCotPhai(g);
            if (moChiTiet)
            {
                veBangChiTiet(g);
            }
        }

        private void veCotTrai(mGraphics g)
        {
            g.setColor(MAU_THE_MO, 0.55f);
            g.fillRect(xTrai, yThan, RONG_TRAI, caoThan, 6);
            for (int i = 0; i < ds.Count; i++)
            {
                int yy = yThan + 4 + i * (CAO_MUC + 3);
                if (yy + CAO_MUC > yThan + caoThan - 2)
                {
                    break;
                }
                bool dangChon = (i == chon);
                veKhungBo(g, xTrai + 3, yy, RONG_TRAI - 6, CAO_MUC,
                        dangChon ? MAU_CHON : MAU_THE, dangChon ? 1f : 0.95f,
                        MAU_VIEN, dangChon ? 0.9f : 0.45f, 1);
                mFont.tahoma_7b_dark.drawString(g, catBot(ds[i].ten, 20),
                        xTrai + RONG_TRAI / 2, yy + CAO_MUC / 2 - 5, mFont.CENTER);
            }
        }

        private void veCotPhai(mGraphics g)
        {
            SuKien sk = ds[chon];

            g.setColor(MAU_THE, 0.9f);
            g.fillRect(xPhai, yThan, rongPhai, 30, 6);
            mFont.tahoma_7b_red.drawString(g, catBot(sk.ten, 34),
                    xPhai + 8, yThan + 3, mFont.LEFT);
            mFont.tahoma_7.drawString(g, catBot(sk.khungGio, 40),
                    xPhai + 8, yThan + 16, mFont.LEFT);

            List<Dong> dong = dongHienThi();
            if (cuon > Math.max(0, dong.Count - soDongHien))
            {
                cuon = Math.max(0, dong.Count - soDongHien);
            }
            for (int i = cuon; i < dong.Count && i - cuon < soDongHien; i++)
            {
                veMotDong(g, dong[i], yDong + (i - cuon) * CAO_DONG);
            }
            if (dong.Count > soDongHien)
            {
                // Lăn chuột hoặc kéo tay để xem tiếp, nên chỗ này chỉ còn vạch
                // chỉ vị trí, không còn hai nút bấm.
                veVachCuon(g, dong.Count, soDongHien);
                int yn = y0 + cao - 20;
                mFont.tahoma_7.drawString(g,
                        (cuon + 1) + "-" + Math.min(dong.Count, cuon + soDongHien)
                        + "/" + dong.Count,
                        xPhai + rongPhai - 4, yn + 3, mFont.RIGHT);
            }
        }

        private void veMotDong(mGraphics g, Dong d, int y)
        {
            if (d.laTieuDe)
            {
                // Dong tieu de nhom: khong khung, chu cam, co vach ngang cho ro
                // ranh gioi hai phan.
                mFont.tahoma_7b_red.drawString(g, d.ten, xPhai + 4, y + 8,
                        mFont.LEFT);
                g.setColor(MAU_VIEN, 0.35f);
                g.fillRect(xPhai + 4, y + 22, rongPhai - 8, 1);
                return;
            }
            int h = CAO_DONG - 4;
            g.setColor(MAU_THE, 0.9f);
            g.fillRect(xPhai, y, rongPhai, h, 5);

            g.setColor(MAU_THE_MO, 0.85f);
            g.fillRect(xPhai + 4, y + 3, 20, 20, 4);
            if (d.icon >= 0)
            {
                SmallImage.drawSmallImage(g, d.icon, xPhai + 14, y + 13,
                        0, mGraphics.VCENTER | mGraphics.HCENTER);
            }

            // Bề ngang còn lại cho chữ: dòng có nút phải nhường chỗ cho chúng.
            int soNut = soNutCua(d);
            int rongChu = rongPhai - 33
                    - (soNut > 0 ? RONG_NUT_CT * 2 + 14 : 8);
            int soChu = Math.max(8, rongChu / 5);

            mFont.tahoma_7b_dark.drawString(g, catBot(d.ten, soChu),
                    xPhai + 29, y + 3, mFont.LEFT);
            if (d.moTa != null)
            {
                mFont.tahoma_7.drawString(g, catBot(d.moTa, soChu + 6),
                        xPhai + 29, y + 14, mFont.LEFT);
            }
            if (soNut > 0)
            {
                int xn = xNutYeuCau();
                if (d.yeuCau.Count > 0)
                {
                    veNut(g, xn, y + 5, RONG_NUT_CT, 16, "Yêu cầu", false);
                }
                veNut(g, xn + RONG_NUT_CT + 4, y + 5, RONG_NUT_CT, 16, "Xem quà",
                        true);
            }
        }

        /// <summary>Dòng này có nút chi tiết hay không.</summary>
        /// <remarks>
        /// Đồ rơi không có nguyên liệu nên không cần nút "Yêu cầu"; nhưng nếu món
        /// rơi ra lại là một gói quà thì vẫn cần nút "Xem quà" để soi bên trong.
        /// Cả hai loại dùng chung một chỗ tính toạ độ, nên nút "Xem quà" luôn đứng
        /// ở cùng một cột — mắt không phải tìm.
        /// </remarks>
        private static int soNutCua(Dong d)
        {
            if (d.laTieuDe)
            {
                return 0;
            }
            if (d.laCongThuc)
            {
                return 2;
            }
            return d.trongQua.Count > 0 ? 1 : 0;
        }

        /// <summary>Câu phụ đề của bảng "phần nhận được".</summary>
        private static string phuDeQua(Dong d)
        {
            if (d.laCongThuc)
            {
                return "Đổi ở Quy Lão";
            }
            return "Rơi ra từ quái khi sự kiện đang chạy";
        }

        /// <summary>Mép trái nút "Yêu cầu"; nút "Xem quà" nằm ngay sau nó.</summary>
        /// <remarks>
        /// Tính một chỗ rồi cả phần vẽ lẫn phần bắt chạm cùng gọi. Trước đây các
        /// vùng bấm tự tính lại theo cùng công thức, sửa một bên là lệch mà không
        /// có gì báo.
        /// </remarks>
        private int xNutYeuCau()
        {
            return xPhai + rongPhai - RONG_NUT_CT * 2 - 8;
        }

        /// <summary>Bảng liệt kê đủ các món, nổi lên giữa màn.</summary>
        private void veBangChiTiet(mGraphics g)
        {
            g.setColor(0, 0.55f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veKhungBo(g, xCT, yCT, rongCT, caoCT, MAU_NEN, 0.98f,
                    MAU_VIEN, 1f, 1);
            g.setColor(MAU_TIEU_DE, 1f);
            g.fillRect(xCT + 1, yCT + 1, rongCT - 2, 22, BO_GOC);

            mFont.tahoma_7b_red.drawString(g, tieuDeChiTiet,
                    xCT + rongCT / 2, yCT + 4, mFont.CENTER);
            veNut(g, xCT + rongCT - 20, yCT + 4, 16, 14, "X", false);

            int y = yCT + 26;
            if (phuDeChiTiet != null && phuDeChiTiet.Length > 0)
            {
                mFont.tahoma_7.drawString(g, catBot(phuDeChiTiet, 44),
                        xCT + rongCT / 2, y, mFont.CENTER);
            }
            y += 14;

            if (dsChiTiet == null || dsChiTiet.Count == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "(chưa khai món nào)",
                        xCT + rongCT / 2, y + 6, mFont.CENTER);
                y += CAO_DONG_CT;
            }
            else
            {
                for (int i = 0; i < dsChiTiet.Count; i++)
                {
                    veMonChiTiet(g, dsChiTiet[i], y);
                    y += CAO_DONG_CT;
                }
            }
            if (dsChiTiet2 != null)
            {
                // Vạch ngang + tiêu đề mục: mở gói ra được đúng một món, nên phải
                // tách rõ khỏi mục trên, kẻo tưởng nhận hết cả danh sách.
                g.setColor(MAU_VIEN, 0.35f);
                g.fillRect(xCT + 8, y + 4, rongCT - 16, 1);
                mFont.tahoma_7b_red.drawString(g, tieuDeMuc2,
                        xCT + rongCT / 2, y + 8, mFont.CENTER);
                y += CAO_DONG_CT;
                for (int i = 0; i < dsChiTiet2.Count; i++)
                {
                    veMonChiTiet(g, dsChiTiet2[i], y);
                    y += CAO_DONG_CT;
                }
            }
            veNut(g, xCT + rongCT / 2 - 28, yCT + caoCT - 22, 56, 16, "Đóng",
                    true);
        }

        private void veMonChiTiet(mGraphics g, VatPham v, int y)
        {
            g.setColor(MAU_THE, 0.9f);
            g.fillRect(xCT + 6, y, rongCT - 12, CAO_DONG_CT - 3, 4);

            g.setColor(MAU_THE_MO, 0.85f);
            g.fillRect(xCT + 9, y + 1, 19, 19, 3);
            if (v.icon >= 0)
            {
                SmallImage.drawSmallImage(g, v.icon, xCT + 18, y + 10,
                        0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
            mFont.tahoma_7b_dark.drawString(g, catBot(v.ten, 22),
                    xCT + 32, y + 6, mFont.LEFT);

            string phai = v.soLuongChu();
            if (v.tiLe != null && v.tiLe.Length > 0)
            {
                phai = phai + "  ·  " + v.tiLe;
            }
            mFont.tahoma_7b_green2.drawString(g, phai, xCT + rongCT - 10, y + 6,
                    mFont.RIGHT);
        }

        private void veNut(mGraphics g, int x, int y, int w, int h, string chu,
                bool sang)
        {
            // Khong con vet loe trang o dinh nut: tren nen kem no doc ra nhu mot
            // duong ke lac cho, khong ra hieu ung noi khoi.
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
        // ------------------------------------------------------------------
        /// <summary>Số dòng đang có trong danh sách bên phải.</summary>
        private int soDongCuon()
        {
            return (chon >= 0 && chon < ds.Count) ? dongHienThi().Count : 0;
        }

        /// <summary>Vẽ vạch chỉ vị trí dọc theo mép phải danh sách.</summary>
        /// <remarks>
        /// Không bấm được vào vạch này — nó chỉ cho biết đang ở đâu trong danh
        /// sách. Thêm chỗ bấm vào một dải rộng 3 điểm thì ngón tay không trúng
        /// vào được.
        /// </remarks>
        private void veVachCuon(mGraphics g, int soDong, int soHien)
        {
            int xv = xPhai + rongPhai - 3;
            int caoRanh = soHien * CAO_DONG;
            g.setColor(MAU_THE_MO, 0.8f);
            g.fillRect(xv, yDong, 3, caoRanh, 2);
            int caoTay = Math.max(12, caoRanh * soHien / soDong);
            int chay = caoRanh - caoTay;
            int toiDa = soDong - soHien;
            int dy = toiDa <= 0 ? 0 : chay * cuon / toiDa;
            g.setColor(MAU_VIEN, 0.9f);
            g.fillRect(xv, yDong + dy, 3, caoTay, 2);
        }

        /// <summary>Toạ độ y lúc ghim mốc để đo đã kéo được bao xa.</summary>
        private int yMocKeo;

        private bool dangKeo;

        /// <summary>Đã kéo quá ngưỡng nên lần nhả ngón này không tính là bấm.</summary>
        private bool daKeoXa;

        private const int NGUONG_KEO = 6;

        /// <summary>Cuộn danh sách theo lăn chuột và kéo tay.</summary>
        /// <remarks>
        /// <para>Gọi ở đầu phần bắt chạm, TRƯỚC chỗ thoát sớm khi chưa nhả ngón:
        /// kéo phải thấy nhích liên tục, đợi nhả ngón mới cuộn thì như cái nút
        /// bấm chậm.</para>
        ///
        /// <para>Khó ở chỗ <b>kéo và bấm cùng kết thúc bằng một lần nhả ngón</b>.
        /// Nên đo quãng đã đi so với chỗ đặt ngón; quá vài điểm là bỏ lần nhả đó,
        /// không thế thì cuộn xong ngón nhả đúng thẻ nào là nhấn luôn thẻ đó.</para>
        /// </remarks>
        private void cuonDanhSach(int soDong)
        {
            int toiDa = Math.max(0, soDong - soDongHien);
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
                int buoc = (GameCanvas.py - yMocKeo) / CAO_DONG;
                if (buoc != 0)
                {
                    cuon -= buoc;
                    yMocKeo += buoc * CAO_DONG;
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

        private bool trongVungCuon(int x, int y)
        {
            return x >= xPhai && x <= xPhai + rongPhai
                    && y >= yDong && y <= yDong + caoVungDong;
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
            tinhBoCuc();

            // Bảng chi tiết mở thì nuốt hết chạm: chỉ nút đóng của nó ăn.
            if (moChiTiet)
            {
                if (GameCanvas.isPointerJustRelease
                        && (cham(xCT + rongCT - 20, yCT + 4, 16, 14)
                            || cham(xCT + rongCT / 2 - 28, yCT + caoCT - 22, 56, 16)))
                {
                    dongChiTiet();
                }
                return true;
            }

            cuonDanhSach(soDongCuon());
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            // Nhả ngón sau một nhát kéo: không tính là bấm vào dòng bên dưới.
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
            if (ds.Count == 0)
            {
                return true;
            }
            for (int i = 0; i < ds.Count; i++)
            {
                int yy = yThan + 4 + i * (CAO_MUC + 3);
                if (yy + CAO_MUC > yThan + caoThan - 2)
                {
                    break;
                }
                if (cham(xTrai + 3, yy, RONG_TRAI - 6, CAO_MUC))
                {
                    chon = i;
                    cuon = 0;
                    return true;
                }
            }
            return chamNutCongThuc();
        }

        /// <summary>Bắt hai nút chi tiết trên các dòng công thức đang hiện.</summary>
        private bool chamNutCongThuc()
        {
            List<Dong> dong = dongHienThi();
            int xn = xNutYeuCau();
            for (int i = cuon; i < dong.Count && i - cuon < soDongHien; i++)
            {
                Dong d = dong[i];
                if (soNutCua(d) == 0)
                {
                    continue;
                }
                int y = yDong + (i - cuon) * CAO_DONG;
                if (d.yeuCau.Count > 0 && cham(xn, y + 5, RONG_NUT_CT, 16))
                {
                    moBang("VẬT PHẨM YÊU CẦU",
                            "Cần đủ những món này để đổi " + d.ten, d.yeuCau,
                            null, null);
                    return true;
                }
                if (cham(xn + RONG_NUT_CT + 4, y + 5, RONG_NUT_CT, 16))
                {
                    moBang("PHẦN NHẬN ĐƯỢC", phuDeQua(d), d.qua,
                            "MỞ GÓI RA MỘT TRONG SỐ NÀY", d.trongQua);
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
