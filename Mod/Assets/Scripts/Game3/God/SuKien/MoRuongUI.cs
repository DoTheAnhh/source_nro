// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game3.God
{
    /// <summary>
    /// Tab <b>Mở rương</b> trong màn Sự kiện — kiểu gacha: chọn loại rương, mở
    /// x1 hoặc x10 bằng điểm rương, dải quà trượt rồi dừng dưới vạch giữa như
    /// mở hòm CS:GO.
    /// </summary>
    /// <remarks>
    /// <para><b>Máy chủ bốc, client chỉ diễn.</b> Kết quả đến trước khi dải quà
    /// chạy: client dựng một dải ngẫu nhiên từ chính bộ quà của rương, đặt món
    /// đã trúng vào đúng ô sẽ dừng dưới vạch, rồi cho trượt chậm dần tới đó.
    /// Nên hiệu ứng có gay cấn cỡ nào cũng không đổi được kết quả.</para>
    ///
    /// <para><b>Hoạt ảnh tính theo đồng hồ lúc vẽ.</b> Màn Sự kiện không có vòng
    /// cập nhật riêng; vị trí dải quà là hàm của thời gian đã trôi, nên khung
    /// hình có rớt thì dải vẫn dừng đúng lúc, đúng chỗ.</para>
    ///
    /// <para>Gói 115 — xem <c>MoRuongService</c> bên máy chủ.</para>
    /// </remarks>
    public class MoRuongUI
    {
        private static MoRuongUI instance;

        public static MoRuongUI getInstance()
        {
            return instance ?? (instance = new MoRuongUI());
        }

        // ------------------------------------------------------------------
        //  Dữ liệu
        // ------------------------------------------------------------------
        public class Mon
        {
            public int icon;
            public int soLuong;
            public string ten;
            /// <summary>0 thường · 1 hiếm · 2 sử thi · 3 huyền thoại.</summary>
            public int hiem;
            public string tiLe;
        }

        public class Ruong
        {
            public int id;
            public string ten;
            public string moTa;
            public int icon;
            public int giaX1;
            public int giaX10;
            public readonly List<Mon> qua = new List<Mon>();
        }

        private readonly List<Ruong> ds = new List<Ruong>();
        private long diem;
        private string tenDiem = "Điểm rương";
        private int chon;
        private bool coDuLieu;

        /// <summary>Lúc xin bảng lần cuối; 0 là chưa xin.</summary>
        private long lucXin;

        /// <summary>Đã gửi lệnh mở, đang chờ máy chủ trả kết quả.</summary>
        private bool dangCho;
        private long lucGui;

        /// <summary>Quá chừng này không có trả lời thì báo máy chủ chưa hỗ trợ.</summary>
        private const long HAN_CHO = 5000L;

        // ------------------------------------------------------------------
        //  Hoạt ảnh
        // ------------------------------------------------------------------
        /// <summary>Dải quà đang chạy (hoặc vừa chạy xong).</summary>
        private readonly List<Mon> dai = new List<Mon>();

        /// <summary>Ô trong dải sẽ dừng dưới vạch.</summary>
        private const int O_TRUNG = 42;

        /// <summary>Số ô dựng cho một lượt quay.</summary>
        private const int SO_O_DAI = 50;

        private long lucQuay;
        private long thoiGianQuay;
        private bool dangQuay;

        /// <summary>Lệch ngẫu nhiên trong ô trúng, để dải không lần nào cũng dừng đúng tâm ô.</summary>
        private float lechTrongO;

        /// <summary>Kết quả của lượt vừa quay, hiện sau khi dải dừng.</summary>
        private readonly List<Mon> ketQua = new List<Mon>();
        private bool hienKetQua;

        private bool hienXemTruoc;
        private int cuonXemTruoc;

        private readonly System.Random rd = new System.Random();

        // ------------------------------------------------------------------
        //  Màu
        // ------------------------------------------------------------------
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        private static readonly int MAU_THE = rgb(0xFD, 0xF0, 0xDC);
        private static readonly int MAU_THE_MO = rgb(0xFF, 0xF3, 0xDE);
        private static readonly int MAU_VIEN = rgb(0xA8, 0x6E, 0x3C);
        private static readonly int MAU_CHON = rgb(0xF0, 0xA1, 0x64);
        private static readonly int MAU_CAM = rgb(0xD9, 0x6A, 0x12);
        private static readonly int MAU_NEN_DAI = rgb(0x2A, 0x20, 0x18);
        private static readonly int MAU_XANH = rgb(0x2E, 0xA8, 0x4A);

        /// <summary>Màu độ hiếm, theo đúng quy ước quen của game gacha.</summary>
        private static readonly int[] MAU_HIEM = {
            rgb(0xB0, 0xB7, 0xC3),   // thuong: xam
            rgb(0x3B, 0x82, 0xF6),   // hiem: xanh duong
            rgb(0x9B, 0x4D, 0xDB),   // su thi: tim
            rgb(0xF5, 0xA6, 0x23)    // huyen thoai: vang cam
        };

        private static readonly string[] TEN_HIEM = { "Thường", "Hiếm", "Sử thi", "Huyền thoại" };

        private static int mauHiem(int h)
        {
            return MAU_HIEM[h < 0 ? 0 : (h > 3 ? 3 : h)];
        }

        private const int BO_GOC = 6;

        private static void veKhungBo(mGraphics g, int x, int y, int w, int h,
                int mauNen, float moNen, int mauVien, float moVien, int day)
        {
            g.setColor(mauVien, moVien);
            g.fillRect(x, y, w, h, BO_GOC);
            g.setColor(mauNen, moNen);
            g.fillRect(x + day, y + day, w - day * 2, h - day * 2, BO_GOC - 1);
        }

        private static void veNut(mGraphics g, int x, int y, int w, int h, string chu,
                int mauNen, bool chuTrang)
        {
            veKhungBo(g, x, y, w, h, mauNen, 1f, MAU_VIEN, 0.9f, 1);
            if (chuTrang)
            {
                mFont.tahoma_7b_white.drawString(g, chu, x + w / 2, y + h / 2 - 5, mFont.CENTER,
                        mFont.tahoma_7b_dark);
            }
            else
            {
                mFont.tahoma_7b_dark.drawString(g, chu, x + w / 2, y + h / 2 - 5, mFont.CENTER);
            }
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
        //  Gói tin
        // ------------------------------------------------------------------
        /// <summary>Xin bảng khi mở tab (và làm mới sau mỗi 20 giây xem).</summary>
        public void xinNeuCan()
        {
            long bayGio = mSystem.currentTimeMillis();
            if (!coDuLieu || bayGio - lucXin > 20000L)
            {
                lucXin = bayGio;
                Service.gI().moRuongXin();
            }
        }

        public void nhanGoi(Message msg)
        {
            try
            {
                int loai = msg.reader().readByte();
                if (loai == 0)
                {
                    docBang(msg);
                }
                else if (loai == 1)
                {
                    docKetQua(msg);
                }
            }
            catch (System.Exception)
            {
                // Goi hong thi bo, lan xin sau lam lai.
            }
        }

        private void docBang(Message msg)
        {
            diem = msg.reader().readLong();
            tenDiem = msg.reader().readUTF();
            int n = msg.reader().readUnsignedByte();
            ds.Clear();
            for (int i = 0; i < n; i++)
            {
                Ruong r = new Ruong();
                r.id = msg.reader().readShort();
                r.ten = msg.reader().readUTF();
                r.moTa = msg.reader().readUTF();
                r.icon = msg.reader().readShort();
                r.giaX1 = msg.reader().readInt();
                r.giaX10 = msg.reader().readInt();
                int soQua = msg.reader().readUnsignedByte();
                for (int k = 0; k < soQua; k++)
                {
                    Mon m = new Mon();
                    m.icon = msg.reader().readShort();
                    m.soLuong = msg.reader().readInt();
                    m.ten = msg.reader().readUTF();
                    m.hiem = msg.reader().readByte();
                    m.tiLe = msg.reader().readUTF();
                    r.qua.Add(m);
                }
                ds.Add(r);
            }
            if (chon >= ds.Count)
            {
                chon = 0;
            }
            coDuLieu = true;
        }

        private void docKetQua(Message msg)
        {
            int id = msg.reader().readShort();
            diem = msg.reader().readLong();
            int n = msg.reader().readUnsignedByte();
            ketQua.Clear();
            for (int i = 0; i < n; i++)
            {
                Mon m = new Mon();
                m.icon = msg.reader().readShort();
                m.soLuong = msg.reader().readInt();
                m.ten = msg.reader().readUTF();
                m.hiem = msg.reader().readByte();
                ketQua.Add(m);
            }
            dangCho = false;
            batDauQuay(id);
        }

        // ------------------------------------------------------------------
        //  Quay
        // ------------------------------------------------------------------
        private Ruong ruongTheoId(int id)
        {
            foreach (Ruong r in ds)
            {
                if (r.id == id)
                {
                    return r;
                }
            }
            return ds.Count > 0 ? ds[chon] : null;
        }

        /// <summary>Bốc một món để làm "vỏ" dải quà, nghiêng về đồ thường cho giống thật.</summary>
        private Mon monNgauNhien(Ruong r)
        {
            if (r == null || r.qua.Count == 0)
            {
                return null;
            }
            // Trong so hien thi: thuong 60, hiem 25, su thi 11, huyen thoai 4.
            int[] ts = { 60, 25, 11, 4 };
            int tong = 0;
            foreach (Mon m in r.qua)
            {
                tong += ts[m.hiem < 0 ? 0 : (m.hiem > 3 ? 3 : m.hiem)];
            }
            int x = rd.Next(tong);
            foreach (Mon m in r.qua)
            {
                x -= ts[m.hiem < 0 ? 0 : (m.hiem > 3 ? 3 : m.hiem)];
                if (x < 0)
                {
                    return m;
                }
            }
            return r.qua[r.qua.Count - 1];
        }

        /// <summary>
        /// Dựng dải quà với món trúng ở ô <see cref="O_TRUNG"/> rồi cho chạy.
        /// </summary>
        /// <remarks>
        /// x10 thì dải dừng ở món hiếm nhất trong mười món — khoảnh khắc đáng xem
        /// nhất — rồi bảng kết quả liệt kê đủ cả mười.
        /// </remarks>
        private void batDauQuay(int id)
        {
            Ruong r = ruongTheoId(id);
            dai.Clear();
            if (ketQua.Count == 0)
            {
                return;
            }
            Mon trung = ketQua[0];
            foreach (Mon m in ketQua)
            {
                if (m.hiem > trung.hiem)
                {
                    trung = m;
                }
            }
            for (int i = 0; i < SO_O_DAI; i++)
            {
                Mon m = (i == O_TRUNG) ? trung : monNgauNhien(r);
                dai.Add(m ?? trung);
            }
            lechTrongO = (float) (rd.NextDouble() * 0.7 - 0.35);
            lucQuay = mSystem.currentTimeMillis();
            thoiGianQuay = ketQua.Count > 1 ? 3800L : 4800L;
            dangQuay = true;
            hienKetQua = false;
        }

        /// <summary>
        /// Quãng đã trượt ở thời điểm hiện tại, theo đường giảm tốc bậc bốn —
        /// lao nhanh lúc đầu, bò chậm mấy ô cuối như hòm CS:GO.
        /// </summary>
        private float quangTruot(int buoc, int rongCua)
        {
            float dich = (O_TRUNG + 0.5f + lechTrongO) * buoc - rongCua / 2f;
            long troi = mSystem.currentTimeMillis() - lucQuay;
            float t = thoiGianQuay <= 0 ? 1f : (float) troi / thoiGianQuay;
            if (t >= 1f)
            {
                if (dangQuay)
                {
                    dangQuay = false;
                    // Dung han roi moi hien bang: mot nhip de mat kip nhin mon trung.
                    lucDung = mSystem.currentTimeMillis();
                }
                return dich;
            }
            float con = 1f - t;
            float e = 1f - con * con * con * con;
            return dich * e;
        }

        private long lucDung;

        // ------------------------------------------------------------------
        //  Bố cục (tính theo vùng thân màn Sự kiện truyền vào)
        // ------------------------------------------------------------------
        private int bx, by, bw, bh;
        private const int RONG_DS = 120;
        private const int CAO_MUC = 34;

        private int xP()
        {
            return bx + RONG_DS + 6;
        }

        private int rP()
        {
            return bw - RONG_DS - 6;
        }

        private int oCua()
        {
            return 50;
        }

        /// <summary>Cửa sổ dải quà: x, y, rộng, cao.</summary>
        private int[] cuaSo()
        {
            int y = by + 40;
            return new int[] { xP(), y, rP(), oCua() + 22 };
        }

        private int[] oNutMo(int i)
        {
            int[] c = cuaSo();
            int y = c[1] + c[3] + 10;
            int w = (rP() - 12) / 3;
            return new int[] { xP() + i * (w + 6), y, w, 24 };
        }

        // ------------------------------------------------------------------
        //  Vẽ
        // ------------------------------------------------------------------
        public void ve(mGraphics g, int x, int y, int w, int h)
        {
            bx = x;
            by = y;
            bw = w;
            bh = h;
            if (dangCho && mSystem.currentTimeMillis() - lucGui > HAN_CHO)
            {
                dangCho = false;
                GameScr.info1.addInfo("Máy chủ chưa hỗ trợ mở rương — cần cập nhật máy chủ.", 0);
            }
            if (!coDuLieu)
            {
                mFont.tahoma_7b_dark.drawString(g, "Đang tải rương…", x + w / 2, y + h / 2 - 5,
                        mFont.CENTER);
                return;
            }
            if (ds.Count == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Chưa có rương nào đang mở bán", x + w / 2,
                        y + h / 2 - 5, mFont.CENTER);
                return;
            }
            veDanhSach(g);
            veBenPhai(g);
            if (!dangQuay && dai.Count > 0 && ketQua.Count > 0 && !hienKetQua
                    && lucDung > 0 && mSystem.currentTimeMillis() - lucDung > 450L)
            {
                hienKetQua = true;
                lucDung = 0;
            }
            if (hienKetQua)
            {
                veKetQua(g);
            }
            if (hienXemTruoc)
            {
                veXemTruoc(g);
            }
        }

        private void veDanhSach(mGraphics g)
        {
            g.setColor(MAU_THE_MO, 0.55f);
            g.fillRect(bx, by, RONG_DS, bh, 6);
            for (int i = 0; i < ds.Count; i++)
            {
                int yy = by + 4 + i * (CAO_MUC + 3);
                if (yy + CAO_MUC > by + bh - 30)
                {
                    break;
                }
                bool c = i == chon;
                veKhungBo(g, bx + 3, yy, RONG_DS - 6, CAO_MUC, c ? MAU_CHON : MAU_THE, 1f,
                        MAU_VIEN, c ? 1f : 0.5f, 1);
                if (ds[i].icon >= 0)
                {
                    SmallImage.drawSmallImage(g, ds[i].icon, bx + 20, yy + CAO_MUC / 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
                mFont.tahoma_7b_dark.drawString(g, catBot(ds[i].ten, 14), bx + 36, yy + CAO_MUC / 2 - 5,
                        mFont.LEFT);
            }
            // Diem rieng, day cot.
            int yD = by + bh - 26;
            veKhungBo(g, bx + 3, yD, RONG_DS - 6, 22, MAU_CAM, 1f, MAU_VIEN, 1f, 1);
            mFont.tahoma_7b_white.drawString(g, catBot(tenDiem + ": " + diem, 22), bx + RONG_DS / 2, yD + 6,
                    mFont.CENTER, mFont.tahoma_7b_dark);
        }

        private void veBenPhai(mGraphics g)
        {
            Ruong r = ds[chon];
            int x = xP();
            int w = rP();

            // Ten + mo ta.
            veKhungBo(g, x, by, w, 34, MAU_THE, 0.95f, MAU_VIEN, 0.7f, 1);
            mFont.tahoma_7b_red.drawString(g, r.ten, x + 8, by + 4, mFont.LEFT);
            mFont.tahoma_7.drawString(g, catBot(r.moTa, 70), x + 8, by + 18, mFont.LEFT);

            veCuaSo(g, r);

            // Ba nut.
            bool ban = dangQuay || dangCho;
            int[] n1 = oNutMo(0);
            int[] n10 = oNutMo(1);
            int[] nx = oNutMo(2);
            veNutMo(g, n1, "Mở x1 · " + r.giaX1, diem >= r.giaX1 && !ban);
            veNutMo(g, n10, "Mở x10 · " + r.giaX10, diem >= r.giaX10 && !ban);
            veNut(g, nx[0], nx[1], nx[2], nx[3], "Xem quà", MAU_THE, false);

            // Hang "quy hiem nhat" duoi nut: nhin la biet rương nay dang gia gi.
            int yH = nx[1] + nx[3] + 10;
            if (yH + 34 < by + bh)
            {
                mFont.tahoma_7b_dark.drawString(g, "Quà hiếm nhất:", x, yH, mFont.LEFT);
                int ix = x;
                int dem = 0;
                for (int hi = 3; hi >= 0 && dem < 6; hi--)
                {
                    foreach (Mon m in r.qua)
                    {
                        if (m.hiem != hi || dem >= 6)
                        {
                            continue;
                        }
                        veOMon(g, m, ix, yH + 12, 30, false);
                        ix += 34;
                        dem++;
                    }
                }
            }
        }

        private void veNutMo(mGraphics g, int[] n, string chu, bool duoc)
        {
            if (duoc)
            {
                float tho = 0.5f + 0.5f * (float) System.Math.Sin(mSystem.currentTimeMillis() / 300.0);
                g.setColor(0x9CFF8A, 0.15f + 0.2f * tho);
                g.fillRect(n[0] - 2, n[1] - 2, n[2] + 4, n[3] + 4, 7);
                veNut(g, n[0], n[1], n[2], n[3], chu, MAU_XANH, true);
            }
            else
            {
                veKhungBo(g, n[0], n[1], n[2], n[3], MAU_THE_MO, 0.8f, MAU_VIEN, 0.4f, 1);
                mFont.tahoma_7_grey.drawString(g, chu, n[0] + n[2] / 2, n[1] + n[3] / 2 - 5,
                        mFont.CENTER);
            }
        }

        /// <summary>Cửa sổ dải quà: nền tối, các ô trượt ngang, vạch vàng ở giữa.</summary>
        private void veCuaSo(mGraphics g, Ruong r)
        {
            int[] c = cuaSo();
            veKhungBo(g, c[0], c[1], c[2], c[3], MAU_NEN_DAI, 1f, MAU_VIEN, 1f, 2);
            int o = oCua();
            int buoc = o + 4;
            int yO = c[1] + (c[3] - o) / 2;
            g.setClip(c[0] + 2, c[1] + 2, c[2] - 4, c[3] - 4);

            if (dai.Count == 0)
            {
                // Chua quay lan nao: bay san bo qua cua ruong, troi cham cho co hon.
                float troi = (mSystem.currentTimeMillis() / 40L) % (buoc * Math.max(1, r.qua.Count));
                int soO = c[2] / buoc + 3;
                for (int i = 0; i < soO; i++)
                {
                    if (r.qua.Count == 0)
                    {
                        break;
                    }
                    Mon m = r.qua[i % r.qua.Count];
                    int x = c[0] + i * buoc - (int) troi % buoc;
                    veOMon(g, m, x, yO, o, false);
                }
            }
            else
            {
                float quang = quangTruot(buoc, c[2] - 4);
                for (int i = 0; i < dai.Count; i++)
                {
                    int x = c[0] + 2 + i * buoc - (int) quang;
                    if (x + o < c[0] || x > c[0] + c[2])
                    {
                        continue;
                    }
                    int tamX = c[0] + c[2] / 2;
                    bool duoiVach = tamX >= x && tamX < x + o;
                    veOMon(g, dai[i], x, yO, o, duoiVach && !dangQuay);
                }
            }
            // Hai mep toi dan, cho dai nhu chay vao bong toi.
            for (int k = 0; k < 6; k++)
            {
                g.setColor(MAU_NEN_DAI, 0.5f - k * 0.08f);
                g.fillRect(c[0] + 2 + k * 4, c[1] + 2, 4, c[3] - 4);
                g.fillRect(c[0] + c[2] - 6 - k * 4, c[1] + 2, 4, c[3] - 4);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            // Vach giua: vang, kem hai tam giac tren duoi.
            int tam = c[0] + c[2] / 2;
            g.setColor(0xFFD24A, 1f);
            g.fillRect(tam - 1, c[1] + 3, 2, c[3] - 6);
            for (int k = 0; k < 5; k++)
            {
                g.fillRect(tam - 5 + k, c[1] + 1 + k, (5 - k) * 2 - 0, 1);
                g.fillRect(tam - 5 + k, c[1] + c[3] - 2 - k, (5 - k) * 2 - 0, 1);
            }
        }

        /// <summary>Một ô món: nền tối, dải màu độ hiếm dưới đáy, icon, số lượng.</summary>
        private void veOMon(mGraphics g, Mon m, int x, int y, int o, bool sang)
        {
            if (m == null)
            {
                return;
            }
            int mau = mauHiem(m.hiem);
            if (sang)
            {
                float tho = 0.5f + 0.5f * (float) System.Math.Sin(mSystem.currentTimeMillis() / 180.0);
                g.setColor(mau, 0.35f + 0.35f * tho);
                g.fillRect(x - 3, y - 3, o + 6, o + 6, 8);
            }
            veKhungBo(g, x, y, o, o, rgb(0x3A, 0x2E, 0x24), 1f, mau, 1f, sang ? 2 : 1);
            // Anh sang mau do hiem tu duoi len.
            g.setColor(mau, 0.28f);
            g.fillRect(x + 2, y + o / 2, o - 4, o / 2 - 2, 4);
            g.setColor(mau, 1f);
            g.fillRect(x + 2, y + o - 5, o - 4, 3, 1);
            if (m.icon >= 0)
            {
                SmallImage.drawSmallImage(g, m.icon, x + o / 2, y + o / 2 - 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
            }
            if (m.soLuong > 1 && o >= 30)
            {
                mFont.tahoma_7b_white.drawString(g, "x" + m.soLuong, x + o - 3, y + o - 16, mFont.RIGHT,
                        mFont.tahoma_7b_dark);
            }
        }

        /// <summary>Bảng kết quả sau khi dải dừng.</summary>
        private void veKetQua(mGraphics g)
        {
            g.setColor(0, 0.55f);
            g.fillRect(bx - 6, by - 6, bw + 12, bh + 12, BO_GOC);
            bool mot = ketQua.Count == 1;
            int o = mot ? 64 : 44;
            int cot = mot ? 1 : 5;
            int hang = (ketQua.Count + cot - 1) / cot;
            int w = Math.max(200, cot * (o + 10) + 30);
            int h = 30 + hang * (o + 24) + 36;
            int x = bx + (bw - w) / 2;
            int y = by + (bh - h) / 2;
            veKhungBo(g, x, y, w, h, rgb(0x2A, 0x20, 0x18), 0.98f, rgb(0xF5, 0xA6, 0x23), 1f, 2);
            mFont.tahoma_7b_yellow.drawString(g, mot ? "BẠN NHẬN ĐƯỢC" : "KẾT QUẢ MỞ x" + ketQua.Count,
                    x + w / 2, y + 8, mFont.CENTER, mFont.tahoma_7b_dark);
            int xDau = x + (w - (cot * (o + 10) - 10)) / 2;
            for (int i = 0; i < ketQua.Count; i++)
            {
                Mon m = ketQua[i];
                int cx = xDau + (i % cot) * (o + 10);
                int cy = y + 28 + (i / cot) * (o + 24);
                veOMon(g, m, cx, cy, o, m.hiem >= 2);
                mFont.tahoma_7.drawString(g, catBot(m.ten, mot ? 26 : 9), cx + o / 2, cy + o + 3,
                        mFont.CENTER);
            }
            if (mot && ketQua.Count == 1)
            {
                mFont.tahoma_7b_white.drawString(g, TEN_HIEM[ketQua[0].hiem < 0 ? 0 : (ketQua[0].hiem > 3 ? 3 : ketQua[0].hiem)],
                        x + w / 2, y + 28 + o + 15, mFont.CENTER, mFont.tahoma_7b_dark);
            }
            veNut(g, x + w / 2 - 40, y + h - 28, 80, 20, "Nhận", MAU_XANH, true);
        }

        /// <summary>Bảng xem trước: mọi quà của rương, độ hiếm và tỉ lệ.</summary>
        private void veXemTruoc(mGraphics g)
        {
            Ruong r = ds[chon];
            g.setColor(0, 0.55f);
            g.fillRect(bx - 6, by - 6, bw + 12, bh + 12, BO_GOC);
            int w = Math.min(bw - 20, 300);
            int h = bh - 10;
            int x = bx + (bw - w) / 2;
            int y = by + 5;
            veKhungBo(g, x, y, w, h, MAU_THE, 1f, MAU_VIEN, 1f, 2);
            g.setColor(MAU_CAM, 1f);
            g.fillRect(x + 2, y + 2, w - 4, 20, BO_GOC - 1);
            mFont.tahoma_7b_white.drawString(g, "QUÀ TRONG " + r.ten.ToUpper(), x + w / 2, y + 6,
                    mFont.CENTER, mFont.tahoma_7b_dark);

            const int caoDong = 28;
            int soHien = Math.max(1, (h - 26 - 32) / caoDong);
            int toiDa = Math.max(0, r.qua.Count - soHien);
            if (cuonXemTruoc > toiDa)
            {
                cuonXemTruoc = toiDa;
            }
            int yy = y + 26;
            for (int i = cuonXemTruoc; i < r.qua.Count && i - cuonXemTruoc < soHien; i++)
            {
                Mon m = r.qua[i];
                g.setColor(i % 2 == 0 ? MAU_THE_MO : MAU_THE, 1f);
                g.fillRect(x + 6, yy, w - 12, caoDong - 2, 4);
                g.setColor(mauHiem(m.hiem), 1f);
                g.fillRect(x + 6, yy, 4, caoDong - 2, 2);
                veOMon(g, m, x + 14, yy + 1, 24, false);
                mFont.tahoma_7b_dark.drawString(g, catBot(m.ten, 22) + " x" + m.soLuong, x + 44, yy + 3,
                        mFont.LEFT);
                mFont.tahoma_7.drawString(g, TEN_HIEM[m.hiem < 0 ? 0 : (m.hiem > 3 ? 3 : m.hiem)], x + 44,
                        yy + 14, mFont.LEFT);
                mFont.tahoma_7b_red.drawString(g, m.tiLe, x + w - 12, yy + 8, mFont.RIGHT);
                yy += caoDong;
            }
            if (toiDa > 0)
            {
                mFont.tahoma_7.drawString(g, (cuonXemTruoc + 1) + "-" + Math.min(r.qua.Count, cuonXemTruoc + soHien)
                        + "/" + r.qua.Count + " · lăn chuột / kéo để xem tiếp", x + w / 2, y + h - 44, mFont.CENTER);
            }
            veNut(g, x + w / 2 - 40, y + h - 28, 80, 20, "Đóng", MAU_CHON, false);
        }

        // ------------------------------------------------------------------
        //  Chạm
        // ------------------------------------------------------------------
        private int yKeoXT;

        /// <summary>Trả true nếu đã nuốt chạm (luôn true khi tab đang hiện).</summary>
        public bool capNhatCham()
        {
            // Xem truoc: lan chuot / keo de cuon.
            if (hienXemTruoc)
            {
                if (GameCanvas.pXYScrollMouse != 0)
                {
                    cuonXemTruoc += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
                    if (cuonXemTruoc < 0)
                    {
                        cuonXemTruoc = 0;
                    }
                }
                if (GameCanvas.isPointerDown)
                {
                    int buoc = (GameCanvas.py - yKeoXT) / 28;
                    if (buoc != 0)
                    {
                        cuonXemTruoc -= buoc;
                        yKeoXT += buoc * 28;
                        if (cuonXemTruoc < 0)
                        {
                            cuonXemTruoc = 0;
                        }
                    }
                }
                else
                {
                    yKeoXT = GameCanvas.py;
                }
            }
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            if (hienXemTruoc)
            {
                int w = Math.min(bw - 20, 300);
                int h = bh - 10;
                int x = bx + (bw - w) / 2;
                int y = by + 5;
                if (cham(x + w / 2 - 40, y + h - 28, 80, 20) || !trong(x, y, w, h))
                {
                    hienXemTruoc = false;
                }
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            if (hienKetQua)
            {
                // Bam dau cung dong bang ket qua.
                hienKetQua = false;
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            if (dangQuay)
            {
                // Dang quay: cham vao cua so thi tua nhanh toi cuoi.
                int[] c = cuaSo();
                if (cham(c[0], c[1], c[2], c[3]))
                {
                    lucQuay = mSystem.currentTimeMillis() - thoiGianQuay;
                }
                return true;
            }
            if (!coDuLieu || ds.Count == 0)
            {
                return true;
            }
            for (int i = 0; i < ds.Count; i++)
            {
                int yy = by + 4 + i * (CAO_MUC + 3);
                if (yy + CAO_MUC > by + bh - 30)
                {
                    break;
                }
                if (cham(bx + 3, yy, RONG_DS - 6, CAO_MUC))
                {
                    if (chon != i)
                    {
                        chon = i;
                        dai.Clear();
                    }
                    return true;
                }
            }
            Ruong r = ds[chon];
            int[] n1 = oNutMo(0);
            int[] n10 = oNutMo(1);
            int[] nx = oNutMo(2);
            if (cham(nx[0], nx[1], nx[2], nx[3]))
            {
                hienXemTruoc = true;
                cuonXemTruoc = 0;
                return true;
            }
            if (!dangCho && cham(n1[0], n1[1], n1[2], n1[3]))
            {
                gui(r, 1, r.giaX1);
                return true;
            }
            if (!dangCho && cham(n10[0], n10[1], n10[2], n10[3]))
            {
                gui(r, 10, r.giaX10);
                return true;
            }
            return true;
        }

        private void gui(Ruong r, int n, int gia)
        {
            if (diem < gia)
            {
                GameScr.info1.addInfo("Không đủ " + tenDiem.ToLower() + " — cần " + gia + ".", 0);
                return;
            }
            dangCho = true;
            lucGui = mSystem.currentTimeMillis();
            Service.gI().moRuongMo(r.id, n);
        }

        private static bool trong(int x, int y, int w, int h)
        {
            return GameCanvas.px >= x && GameCanvas.px <= x + w && GameCanvas.py >= y
                    && GameCanvas.py <= y + h;
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
