// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game5.God
{
    /// <summary>
    /// Màn <b>Trang phục kỹ năng</b> — mở từ thẻ Hệ thống của túi, ngay trên
    /// "Sổ sưu tầm". Chọn chiêu bên trái, chọn ô trang phục bên phải, bấm
    /// <b>Bật</b> mới đổi hình; ô "Hình gốc" là quay về như cũ.
    /// </summary>
    /// <remarks>
    /// <para>Chỉ đổi hình, không đổi sức mạnh. Mới có Quả cầu kênh khi.</para>
    ///
    /// <para><b>Người khác cũng thấy.</b> Ngay trước lúc ai đó tụ chiêu, máy chủ
    /// gửi cả khu gói 106 loại 2 kèm các khung hình (rỗng = hình gốc). Client
    /// gắn vào <c>Char.tpNap</c> / <c>Char.tpBay</c>; hai hàm vẽ tĩnh ở cuối
    /// lớp này dùng chúng lúc tụ và lúc quả cầu bay.</para>
    /// </remarks>
    public class TrangPhucUI
    {
        private static TrangPhucUI instance;

        public static TrangPhucUI getInstance()
        {
            return instance ?? (instance = new TrangPhucUI());
        }

        public bool dangMo;
        private long lucMo;

        public class Mau
        {
            public int id;
            public string ten;
            public string moTa;
            public int icon;
            public short[] nap;
            public short[] bay;
            /// <summary>Đã sở hữu chưa — chưa có thì ô xám mờ, có ổ khoá, không bật được.</summary>
            public bool daCo;
        }

        public class Chieu
        {
            public int tpl;
            public string ten;
            public int icon;
            public int dangDung;
            public readonly List<Mau> ds = new List<Mau>();
        }

        private readonly List<Chieu> dsChieu = new List<Chieu>();
        private bool coDuLieu;
        private int chieuChon;
        /// <summary>Ô đang xem: 0 = hình gốc, còn lại là id trang phục.</summary>
        private int oChon = -1;
        private long lucChonO;

        // ------------------------------------------------------------------
        //  Màu (cùng tông kem – cam với các màn khác)
        // ------------------------------------------------------------------
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        private static readonly int VIEN = rgb(0x9A, 0x4A, 0x10);
        private static readonly int KEM_TREN = rgb(0xFF, 0xF8, 0xEC);
        private static readonly int KEM_DUOI = rgb(0xF6, 0xE2, 0xC4);
        private static readonly int CAM_TREN = rgb(0xFB, 0xB0, 0x5C);
        private static readonly int CAM_DUOI = rgb(0xE0, 0x74, 0x1E);
        private static readonly int NEN_XEM = rgb(0x1C, 0x21, 0x2E);

        // ------------------------------------------------------------------
        //  Mở / đóng / gói
        // ------------------------------------------------------------------
        /// <summary>Lúc xin gần nhất; 0 = chưa xin.</summary>
        private long lucXin;

        /// <summary>Xin quá chừng này không có trả lời thì báo máy chủ chưa hỗ trợ.</summary>
        private const long HAN_CHO = 5000L;

        public void mo()
        {
            dangMo = true;
            lucMo = mSystem.currentTimeMillis();
            xin();
        }

        /// <summary>
        /// Xin trước lúc mở túi, để bấm "Trang phục" là có ngay, không phải chờ.
        /// Đã có dữ liệu thì xin lại nhiều nhất mỗi 30 giây.
        /// </summary>
        public void xinTruoc()
        {
            if (!coDuLieu || mSystem.currentTimeMillis() - lucXin > 30000L)
            {
                xin();
            }
        }

        private void xin()
        {
            lucXin = mSystem.currentTimeMillis();
            Service.gI().trangPhucXin();
        }

        public void dong()
        {
            dangMo = false;
        }

        public void nhanGoi(Message msg)
        {
            try
            {
                int loai = msg.reader().readByte();
                if (loai == 0)
                {
                    docDanhSach(msg);
                }
                else if (loai == 2)
                {
                    int charId = msg.reader().readInt();
                    msg.reader().readShort();
                    short[] nap = docKhung(msg);
                    short[] bay = docKhung(msg);
                    Char c = Char.myCharz().charID == charId ? Char.myCharz() : GameScr.findCharInMap(charId);
                    if (c != null)
                    {
                        c.tpNap = nap.Length > 0 ? nap : null;
                        c.tpBay = bay.Length > 0 ? bay : null;
                        c.tpLuc = mSystem.currentTimeMillis();
                    }
                }
            }
            catch (System.Exception)
            {
                // Goi hong thi bo; nhan vat giu hinh goc.
            }
        }

        private static short[] docKhung(Message msg)
        {
            int n = msg.reader().readUnsignedByte();
            short[] k = new short[n];
            for (int i = 0; i < n; i++)
            {
                k[i] = msg.reader().readShort();
            }
            return k;
        }

        private void docDanhSach(Message msg)
        {
            int giuTpl = chieuChon < dsChieu.Count ? dsChieu[chieuChon].tpl : -1;
            dsChieu.Clear();
            int n = msg.reader().readUnsignedByte();
            for (int i = 0; i < n; i++)
            {
                Chieu c = new Chieu();
                c.tpl = msg.reader().readShort();
                c.ten = msg.reader().readUTF();
                c.icon = msg.reader().readShort();
                c.dangDung = msg.reader().readShort();
                int m = msg.reader().readUnsignedByte();
                for (int k = 0; k < m; k++)
                {
                    Mau x = new Mau();
                    x.id = msg.reader().readShort();
                    x.ten = msg.reader().readUTF();
                    x.moTa = msg.reader().readUTF();
                    x.icon = msg.reader().readShort();
                    x.nap = docKhung(msg);
                    x.bay = docKhung(msg);
                    x.daCo = msg.reader().readByte() == 1;
                    c.ds.Add(x);
                }
                dsChieu.Add(c);
            }
            chieuChon = 0;
            for (int i = 0; i < dsChieu.Count; i++)
            {
                if (dsChieu[i].tpl == giuTpl)
                {
                    chieuChon = i;
                }
            }
            if (dsChieu.Count > 0 && (oChon < 0 || timMau(dsChieu[chieuChon], oChon) == null && oChon != 0))
            {
                oChon = dsChieu[chieuChon].dangDung;
                lucChonO = mSystem.currentTimeMillis();
            }
            coDuLieu = true;
        }

        private static Mau timMau(Chieu c, int id)
        {
            foreach (Mau m in c.ds)
            {
                if (m.id == id)
                {
                    return m;
                }
            }
            return null;
        }

        // ------------------------------------------------------------------
        //  Bố cục
        // ------------------------------------------------------------------
        private int x0, y0, rong, cao;
        private const int CAO_DAU = 26;
        private const int RONG_TRAI = 118;
        private const int CAO_CHIEU = 34;
        private const int O = 46;

        private void tinhBoCuc()
        {
            rong = Math.min(GameCanvas.w - 16, 470);
            cao = Math.min(GameCanvas.h - 16, 290);
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;
        }

        private int[] nutDong()
        {
            return new int[] { x0 + rong - 28, y0 + 4, 22, 18 };
        }

        private int[] oChieu(int i)
        {
            return new int[] { x0 + 8, y0 + CAO_DAU + 8 + i * (CAO_CHIEU + 4), RONG_TRAI - 8, CAO_CHIEU };
        }

        private int xPhai()
        {
            return x0 + RONG_TRAI + 8;
        }

        private int rongPhai()
        {
            return rong - RONG_TRAI - 16;
        }

        /// <summary>Ô thứ <paramref name="i"/> trong hàng trang phục (0 là hình gốc).</summary>
        private int[] oMau(int i)
        {
            return new int[] { xPhai() + 8 + i * (O + 8), y0 + CAO_DAU + 26, O, O };
        }

        /// <summary>Khung xem trước: x, y, rộng, cao.</summary>
        private int[] khungXem()
        {
            int y = y0 + CAO_DAU + 26 + O + 12;
            return new int[] { xPhai(), y, rongPhai(), y0 + cao - 8 - y };
        }

        private int[] nutBat()
        {
            int[] k = khungXem();
            int w = 118;
            return new int[] { k[0] + k[2] - w - 10, k[1] + k[3] - 34, w, 26 };
        }

        // ------------------------------------------------------------------
        //  Vẽ
        // ------------------------------------------------------------------
        private static void veKhung(mGraphics g, int x, int y, int w, int h, int r, int vien, int tren, int duoi)
        {
            g.setColor(vien, 1f);
            g.fillRect(x, y, w, h, r);
            g.veDaiDoc(x + 1, y + 1, w - 2, h - 2, r - 1, tren, duoi);
        }

        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            tinhBoCuc();
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            g.setColor(0, 0.6f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            g.setColor(0, 0.4f);
            g.fillRect(x0 + 3, y0 + 5, rong, cao, 12);
            veKhung(g, x0, y0, rong, cao, 12, VIEN, KEM_TREN, KEM_DUOI);
            // Dai tieu de cam.
            g.veDaiDoc(x0 + 1, y0 + 1, rong - 2, CAO_DAU, 11, CAM_TREN, CAM_DUOI);
            g.setColor(0xFFFFFF, 0.2f);
            g.fillRect(x0 + 6, y0 + 3, rong - 12, 5, 3);
            mFont.tahoma_7b_white.drawString(g, "TRANG PHỤC KỸ NĂNG", x0 + rong / 2, y0 + 7, mFont.CENTER,
                    mFont.tahoma_7b_dark);
            int[] d = nutDong();
            veKhung(g, d[0], d[1], d[2], d[3], 6, rgb(0x8A, 0x14, 0x14), rgb(0xF0, 0x5A, 0x4A), rgb(0xC0, 0x24, 0x1C));
            mFont.tahoma_7b_white.drawString(g, "X", d[0] + d[2] / 2, d[1] + 3, mFont.CENTER);

            if (!coDuLieu)
            {
                bool quaHan = lucXin > 0 && mSystem.currentTimeMillis() - lucXin > HAN_CHO;
                mFont.tahoma_7b_dark.drawString(g, quaHan ? "Máy chủ chưa hỗ trợ Trang phục — cần cập nhật máy chủ."
                        : "Đang tải…", x0 + rong / 2, y0 + cao / 2, mFont.CENTER);
                return;
            }
            if (dsChieu.Count == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Chưa có trang phục nào", x0 + rong / 2, y0 + cao / 2,
                        mFont.CENTER);
                return;
            }
            veCotTrai(g);
            veHangMau(g);
            veXemTruoc(g);
        }

        private void veCotTrai(mGraphics g)
        {
            g.setColor(0xFFFFFF, 0.35f);
            g.fillRect(x0 + 4, y0 + CAO_DAU + 4, RONG_TRAI, cao - CAO_DAU - 12, 8);
            for (int i = 0; i < dsChieu.Count; i++)
            {
                int[] o = oChieu(i);
                if (o[1] + o[3] > y0 + cao - 8)
                {
                    break;
                }
                bool c = i == chieuChon;
                if (c)
                {
                    veKhung(g, o[0], o[1], o[2], o[3], 7, VIEN, CAM_TREN, CAM_DUOI);
                }
                else
                {
                    veKhung(g, o[0], o[1], o[2], o[3], 7, rgb(0xD8, 0xB8, 0x8C), KEM_TREN, KEM_DUOI);
                }
                if (dsChieu[i].icon >= 0)
                {
                    SmallImage.veIconVuaO(g, dsChieu[i].icon, o[0] + 16, o[1] + o[3] / 2, 22);
                }
                string ten = dsChieu[i].ten;
                if (ten.Length > 16)
                {
                    ten = ten.Substring(0, 15) + "…";
                }
                if (c)
                {
                    mFont.tahoma_7b_white.drawString(g, ten, o[0] + 32, o[1] + 5, mFont.LEFT, mFont.tahoma_7b_dark);
                }
                else
                {
                    mFont.tahoma_7b_dark.drawString(g, ten, o[0] + 32, o[1] + 5, mFont.LEFT);
                }
                Mau dung = timMau(dsChieu[i], dsChieu[i].dangDung);
                string phu = dung != null ? dung.ten : "Hình gốc";
                if (phu.Length > 16)
                {
                    phu = phu.Substring(0, 15) + "…";
                }
                if (c)
                {
                    mFont.tahoma_7_white.drawString(g, phu, o[0] + 32, o[1] + 18, mFont.LEFT);
                }
                else
                {
                    mFont.tahoma_7.drawString(g, phu, o[0] + 32, o[1] + 18, mFont.LEFT);
                }
            }
        }

        private void veHangMau(mGraphics g)
        {
            Chieu c = dsChieu[chieuChon];
            mFont.tahoma_7b_dark.drawString(g, "Chọn trang phục cho " + c.ten, xPhai() + 8, y0 + CAO_DAU + 8,
                    mFont.LEFT);
            for (int i = 0; i <= c.ds.Count; i++)
            {
                int[] o = oMau(i);
                if (o[0] + o[2] > x0 + rong - 8)
                {
                    break;
                }
                int id = i == 0 ? 0 : c.ds[i - 1].id;
                bool xem = id == oChon;
                bool dung = id == c.dangDung;
                if (xem)
                {
                    float tho = 0.5f + 0.5f * (float) System.Math.Sin(mSystem.currentTimeMillis() / 250.0);
                    g.setColor(0xFFD24A, 0.35f + 0.35f * tho);
                    g.fillRect(o[0] - 3, o[1] - 3, o[2] + 6, o[3] + 6, 9);
                }
                veKhung(g, o[0], o[1], o[2], o[3], 7, xem ? rgb(0xF5, 0xA6, 0x23) : rgb(0x5A, 0x4A, 0x3A),
                        rgb(0x3A, 0x33, 0x2E), NEN_XEM);
                int icon = i == 0 ? c.icon : c.ds[i - 1].icon;
                bool khoa = i > 0 && !c.ds[i - 1].daCo;
                if (icon >= 0)
                {
                    SmallImage.veIconVuaO(g, icon, o[0] + o[2] / 2, o[1] + o[3] / 2, o[2] - 8);
                }
                if (khoa)
                {
                    // Chua co: phu xam mo len ca o, o khoa giua icon.
                    g.setColor(rgb(0x6A, 0x6A, 0x6A), 0.72f);
                    g.fillRect(o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, 6);
                    veOKhoa(g, o[0] + o[2] / 2, o[1] + o[3] / 2);
                }
                if (dung)
                {
                    // Dau tich xanh goc tren phai: dang dung.
                    g.setColor(rgb(0x17, 0x62, 0x2A), 1f);
                    g.fillRect(o[0] + o[2] - 13, o[1] - 3, 16, 14, 7);
                    g.setColor(rgb(0x6C, 0xDC, 0x7C), 1f);
                    g.fillRect(o[0] + o[2] - 12, o[1] - 2, 14, 12, 6);
                    mFont.tahoma_7b_white.drawString(g, "✓", o[0] + o[2] - 5, o[1] - 2, mFont.CENTER);
                }
                string ten = i == 0 ? "Hình gốc" : c.ds[i - 1].ten;
                if (ten.Length > 10)
                {
                    ten = ten.Substring(0, 9) + "…";
                }
                if (khoa)
                {
                    mFont.tahoma_7_grey.drawString(g, ten, o[0] + o[2] / 2, o[1] + o[3] + 1, mFont.CENTER);
                }
                else
                {
                    mFont.tahoma_7.drawString(g, ten, o[0] + o[2] / 2, o[1] + o[3] + 1, mFont.CENTER);
                }
            }
        }

        /// <summary>Ổ khoá vẽ tay (thân vàng, quai xám) — tâm tại (x, y).</summary>
        private static void veOKhoa(mGraphics g, int x, int y)
        {
            // Bong do.
            g.setColor(0x000000, 0.45f);
            g.fillRect(x - 9, y - 3, 20, 16, 4);
            // Quai: hinh chu U nguoc.
            g.setColor(rgb(0xE8, 0xE8, 0xE8), 1f);
            g.fillRect(x - 6, y - 11, 3, 10, 1);
            g.fillRect(x + 3, y - 11, 3, 10, 1);
            g.fillRect(x - 6, y - 12, 12, 3, 2);
            // Than khoa.
            g.setColor(rgb(0x8A, 0x5A, 0x0A), 1f);
            g.fillRect(x - 9, y - 4, 18, 15, 3);
            g.veDaiDoc(x - 8, y - 3, 16, 13, 2, rgb(0xFF, 0xD8, 0x5A), rgb(0xE0, 0x9A, 0x18));
            // Lo khoa.
            g.setColor(rgb(0x4A, 0x2A, 0x05), 1f);
            g.fillRect(x - 1, y, 3, 3, 1);
            g.fillRect(x, y + 2, 1, 4);
        }

        private void veXemTruoc(mGraphics g)
        {
            Chieu c = dsChieu[chieuChon];
            int[] k = khungXem();
            if (k[3] < 60)
            {
                return;
            }
            veKhung(g, k[0], k[1], k[2], k[3], 9, VIEN, rgb(0x2C, 0x33, 0x45), rgb(0x16, 0x19, 0x24));
            Mau m = timMau(c, oChon);
            int oXem = Math.min(k[3] - 16, k[2] / 2 - 10);
            int tamX = k[0] + 8 + oXem / 2;
            int tamY = k[1] + k[3] / 2;
            g.setColor(0xFFFFFF, 0.05f);
            g.fillRect(k[0] + 8, k[1] + 8, oXem, k[3] - 16, 8);
            if (m != null)
            {
                int khung = khungXemTruoc(m.nap, m.bay, lucChonO);
                if (khung >= 0)
                {
                    SmallImage.veIconVuaO(g, khung, tamX, tamY, oXem);
                }
            }
            else if (c.icon >= 0)
            {
                SmallImage.veIconVuaO(g, c.icon, tamX, tamY, 40);
            }

            int xc = k[0] + 16 + oXem;
            mFont.tahoma_7b_yellow.drawString(g, m != null ? m.ten : "Hình gốc", xc, k[1] + 12, mFont.LEFT,
                    mFont.tahoma_7b_dark);
            string moTa = m != null ? m.moTa : "Quả cầu nguyên bản của game.";
            int rongChu = k[0] + k[2] - 10 - xc;
            string[] dong = mFont.tahoma_7_white.splitFontArray(moTa, Math.max(60, rongChu));
            for (int i = 0; i < dong.Length && i < 4; i++)
            {
                mFont.tahoma_7_white.drawString(g, dong[i], xc, k[1] + 30 + i * 12, mFont.LEFT);
            }
            if (m == null || m.daCo)
            {
                mFont.tahoma_7_grey.drawString(g, "Chỉ đổi hình, không đổi sức mạnh.", xc, k[1] + k[3] - 50, mFont.LEFT);
            }

            int[] n = nutBat();
            bool dangDung = oChon == c.dangDung;
            if (m != null && !m.daCo)
            {
                veKhung(g, n[0], n[1], n[2], n[3], 8, rgb(0x5A, 0x4A, 0x3A), rgb(0x9A, 0x9A, 0x9A), rgb(0x6E, 0x6E, 0x6E));
                mFont.tahoma_7b_white.drawString(g, "CHƯA SỞ HỮU", n[0] + n[2] / 2, n[1] + 7, mFont.CENTER,
                        mFont.tahoma_7b_dark);
                mFont.tahoma_7b_yellow.drawString(g, "Mở từ Rương Cao Cấp", n[0] + n[2] / 2, n[1] - 14, mFont.CENTER,
                        mFont.tahoma_7b_dark);
            }
            else if (dangDung)
            {
                veKhung(g, n[0], n[1], n[2], n[3], 8, rgb(0x5A, 0x4A, 0x3A), rgb(0xB8, 0xA8, 0x90), rgb(0x8A, 0x7A, 0x66));
                mFont.tahoma_7b_white.drawString(g, "ĐANG DÙNG", n[0] + n[2] / 2, n[1] + 7, mFont.CENTER,
                        mFont.tahoma_7b_dark);
            }
            else
            {
                float tho = 0.5f + 0.5f * (float) System.Math.Sin(mSystem.currentTimeMillis() / 300.0);
                g.setColor(0x9CFF8A, 0.2f + 0.25f * tho);
                g.fillRect(n[0] - 3, n[1] - 3, n[2] + 6, n[3] + 6, 10);
                veKhung(g, n[0], n[1], n[2], n[3], 8, rgb(0x17, 0x62, 0x2A), rgb(0x72, 0xDE, 0x80), rgb(0x2A, 0x94, 0x40));
                mFont.tahoma_7b_white.drawString(g, oChon == 0 ? "DÙNG HÌNH GỐC" : "BẬT", n[0] + n[2] / 2, n[1] + 7,
                        mFont.CENTER, mFont.tahoma_7b_dark);
            }
        }

        // ------------------------------------------------------------------
        //  Chạm
        // ------------------------------------------------------------------
        /// <summary>Trả true nếu đang mở (nuốt mọi chạm).</summary>
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            tinhBoCuc();
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            // Lan nha ngon da mo man (tu the He thong) khong tinh la bam.
            if (mSystem.currentTimeMillis() - lucMo < 160)
            {
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            int[] d = nutDong();
            if (cham(d[0], d[1], d[2], d[3]) || !GameCanvas.isPointerHoldIn(x0, y0, rong, cao))
            {
                GameCanvas.clearAllPointerEvent();
                dong();
                return true;
            }
            if (!coDuLieu || dsChieu.Count == 0)
            {
                return true;
            }
            for (int i = 0; i < dsChieu.Count; i++)
            {
                int[] o = oChieu(i);
                if (cham(o[0], o[1], o[2], o[3]))
                {
                    chieuChon = i;
                    oChon = dsChieu[i].dangDung;
                    lucChonO = mSystem.currentTimeMillis();
                    return true;
                }
            }
            Chieu c = dsChieu[chieuChon];
            for (int i = 0; i <= c.ds.Count; i++)
            {
                int[] o = oMau(i);
                if (cham(o[0], o[1], o[2], o[3] + 12))
                {
                    oChon = i == 0 ? 0 : c.ds[i - 1].id;
                    lucChonO = mSystem.currentTimeMillis();
                    return true;
                }
            }
            int[] n = nutBat();
            Mau mChon = timMau(c, oChon);
            bool duocBat = oChon == 0 || (mChon != null && mChon.daCo);
            if (oChon != c.dangDung && duocBat && cham(n[0], n[1], n[2], n[3]))
            {
                Service.gI().trangPhucChon(c.tpl, oChon);
                return true;
            }
            GameCanvas.clearAllPointerEvent();
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

        // ------------------------------------------------------------------
        //  Vẽ chiêu cho nhân vật (gọi từ Char / PlayerDart)
        // ------------------------------------------------------------------
        /// <summary>Mỗi khung tụ bao lâu, mỗi khung lặp bao lâu (ms).</summary>
        /// <remarks>
        /// Tụ chạy MỘT MẠCH (8 khung ~0,8 giây) rồi lặp hai khung cuối — chạy chậm
        /// từng khung trông như trình chiếu ảnh, giật.
        /// </remarks>
        private const long MS_NAP = 100L;
        private const long MS_BAY = 110L;

        /// <summary>Khung chạm địch giữ bao lâu (ms) — dư chấn.</summary>
        private const long MS_TRUNG = 300L;

        /// <summary>
        /// Khung lúc TỤ ở thời điểm hiện tại: chạy hết dãy tụ một lượt, rồi lặp
        /// hai khung tụ cuối cho tới khi ném. -1 nếu không có khung nào.
        /// </summary>
        public static int khungTheoGio(short[] nap, short[] bay, long batDau)
        {
            long troi = mSystem.currentTimeMillis() - batDau;
            int soNap = nap == null ? 0 : nap.Length;
            if (soNap == 0)
            {
                return bay != null && bay.Length > 0 ? bay[0] : -1;
            }
            if (troi < soNap * MS_NAP)
            {
                return nap[(int) (troi / MS_NAP)];
            }
            if (soNap < 2)
            {
                return nap[0];
            }
            long sau = troi - soNap * MS_NAP;
            return nap[soNap - 2 + (int) ((sau / MS_BAY) % 2)];
        }

        /// <summary>Xem trước trong menu: tụ → ném → chạm, rồi lặp lại từ đầu.</summary>
        public static int khungXemTruoc(short[] nap, short[] bay, long batDau)
        {
            int soNap = nap == null ? 0 : nap.Length;
            int soBay = bay == null ? 0 : bay.Length;
            int tong = soNap + soBay;
            if (tong == 0)
            {
                return -1;
            }
            const long MOI = 220L;
            int i = (int) (((mSystem.currentTimeMillis() - batDau) / MOI) % (tong + 3));
            if (i >= tong)
            {
                // Nghi mot nhip tren khung cham truoc khi quay lai tu dau.
                i = tong - 1;
            }
            return i < soNap ? nap[i] : bay[i - soNap];
        }

        /// <summary>
        /// Lúc đang tụ Quả cầu kênh khi: vẽ quả cầu trang phục trên đầu. Trả
        /// false nếu nhân vật đang dùng hình gốc (không vẽ gì).
        /// </summary>
        public static bool veKhiTu(mGraphics g, Char c)
        {
            if (c == null)
            {
                return false;
            }
            // Khung cham dich: hien mot lat tai cho qua cau trung.
            long daQua = mSystem.currentTimeMillis() - c.tpTrungLuc;
            if (c.tpTrung >= 0 && daQua < MS_TRUNG)
            {
                // Du chan: rung trai phai vai diem, nho dan ve 0 het 0,3 giay.
                int bien = (int) (4 * (MS_TRUNG - daQua) / MS_TRUNG);
                int lech = (GameCanvas.gameTick % 2 == 0) ? bien : -bien;
                SmallImage.drawSmallImage(g, c.tpTrung, c.tpTrungX + lech, c.tpTrungY + lech / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
            }
            if (!conHieuLuc(c))
            {
                return false;
            }
            // Tu xong, dang vung tay nem (qua cau chua roi tay): lap khung nem/cham.
            if (!c.isFlyAndCharge)
            {
                if (c.isUseSkillAfterCharge && c.dart == null)
                {
                    int kn = c.tpBay[(int) ((mSystem.currentTimeMillis() / MS_BAY) % c.tpBay.Length)];
                    SmallImage.drawSmallImage(g, kn, c.cx, c.cy - c.ch - 50, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    return true;
                }
                return false;
            }
            int k = khungTheoGio(c.tpNap, c.tpBay, c.tpLuc);
            if (k >= 0)
            {
                SmallImage.drawSmallImage(g, k, c.cx, c.cy - c.ch - 50, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
            return true;
        }

        /// <summary>
        /// Quả cầu đang bay (sau khi tụ xong): vẽ khung lặp thay cho hình gốc.
        /// Trả false nếu chủ quả cầu dùng hình gốc — để vẽ như cũ.
        /// </summary>
        public static bool veKhiBay(mGraphics g, Char chu, int x, int y)
        {
            if (!conHieuLuc(chu))
            {
                return false;
            }
            // Tu luc nem toi luc cham: lap cac khung bay (9 <-> 10).
            int k = chu.tpBay[(int) ((mSystem.currentTimeMillis() / MS_BAY) % chu.tpBay.Length)];
            SmallImage.drawSmallImage(g, k, x, y, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            return true;
        }

        /// <summary>Trang phục chiêu còn hiệu lực bao lâu kể từ lúc tụ (ms).</summary>
        /// <remarks>
        /// Chống dính: ném trượt (không có mục tiêu) thì không có quả cầu bay, khung
        /// không được bỏ ở cuối đường bay — nếu không có hạn, nó sẽ che hiệu ứng
        /// của chiêu khác dùng sau đó. Tụ tối đa ~10 giây cộng lúc bay là đủ.
        /// </remarks>
        private const long HAN_HIEU_LUC = 15000L;

        /// <summary>Nhân vật đang mang khung trang phục chiêu còn hạn.</summary>
        public static bool conHieuLuc(Char c)
        {
            return c != null && c.tpBay != null && c.tpBay.Length > 0
                    && mSystem.currentTimeMillis() - c.tpLuc < HAN_HIEU_LUC;
        }

        /// <summary>Quả cầu vừa trúng: ghi khung chạm (khung thứ hai của dãy bay) để vẽ một lát.</summary>
        public static void ghiChamDich(Char chu, int x, int y)
        {
            if (!conHieuLuc(chu) || chu.tpBay.Length < 2)
            {
                return;
            }
            chu.tpTrung = chu.tpBay[1];
            chu.tpTrungX = x;
            chu.tpTrungY = y;
            chu.tpTrungLuc = mSystem.currentTimeMillis();
        }
    }
}
