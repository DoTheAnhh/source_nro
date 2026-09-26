// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game5.God
{
    /// <summary>
    /// Màn <b>Trang phục kỹ năng</b> — mở từ thẻ Hệ thống của túi, ngay trên
    /// "Sổ sưu tầm". Chọn chiêu bên trái, chọn ô trang phục bên phải, bấm
    /// <b>Bật</b> mới đổi hình; ô đầu (tên kỹ năng) là quay về hình gốc.
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
        private static int idDaXin = -1;

        /// <summary>
        /// Vừa vào game (hoặc đổi nhân vật): xin ngay danh sách trang phục — ảnh mọi khung
        /// được tải trước và skin của mình có sẵn, lần dùng chiêu đầu tiên đã đúng hình, đúng tiếng.
        /// </summary>
        private static void xinKhiVao()
        {
            Char toi = Char.myCharz();
            if (toi == null || toi.charID == idDaXin || SmallImage.imgNew == null)
            {
                return;
            }
            idDaXin = toi.charID;
            getInstance().xin();
        }

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
                else if (loai == 3)
                {
                    // Bi thoi mien boi nguoi co skin Tsukuyomi: ao canh phu kin man hinh.
                    msg.reader().readShort();
                    int ms = msg.reader().readInt();
                    short[] khung = docKhung(msg);
                    if (khung.Length > 0)
                    {
                        taiTruoc(khung);
                        long bayGio = mSystem.currentTimeMillis();
                        if (tmKhung == null || bayGio >= tmHet)
                        {
                            tmBatDau = bayGio;
                        }
                        tmKhung = khung;
                        tmHet = bayGio + ms;
                    }
                }
                else if (loai == 5)
                {
                    // Nguoi co skin vua dung chieu: ca khu nghe tieng cua skin.
                    int charId5 = msg.reader().readInt();
                    int tpl = msg.reader().readShort();
                    int msChieu = 0;
                    try
                    {
                        msChieu = msg.reader().readInt();
                    }
                    catch (System.Exception)
                    {
                    }
                    if (tpl == 22)
                    {
                        long bayGio = mSystem.currentTimeMillis();
                        lock (chanDanh)
                        {
                            chanDanh[charId5] = bayGio + 1500L;
                        }
                        henAm("sharingan", bayGio, 0, AM_SHARINGAN);
                        if (msChieu > 0)
                        {
                            // Sau tieng Sharingan la tieng qua, keo (lap/cat) vua het thoi gian thoi mien.
                            AmCho q = new AmCho();
                            q.ten = "qua";
                            q.luc = bayGio;
                            q.sauTen = "sharingan";
                            q.hetLuc = bayGio + msChieu;
                            q.am = AM_QUA;
                            lock (dsAm)
                            {
                                dsAm.Add(q);
                            }
                        }
                    }
                }
                else if (loai == 4)
                {
                    // Tinh (het thoi mien som): tat ao canh, mo dan.
                    if (tmKhung != null)
                    {
                        tmHet = System.Math.Min(tmHet, mSystem.currentTimeMillis());
                    }
                }
                else if (loai == 2)
                {
                    int charId = msg.reader().readInt();
                    int skillTpl = msg.reader().readShort();
                    short[] nap = docKhung(msg);
                    short[] bay = docKhung(msg);
                    Char c = Char.myCharz().charID == charId ? Char.myCharz() : GameScr.findCharInMap(charId);
                    if (c != null)
                    {
                        c.tpNap = nap.Length > 0 ? nap : null;
                        c.tpBay = bay.Length > 0 ? bay : null;
                        taiTruoc(nap);
                        taiTruoc(bay);
                        c.tpLuc = mSystem.currentTimeMillis();
                        c.tpXong = false;
                        c.tpSkill = skillTpl;
                        c.tpHetLuc = 0;
                        if (bay.Length > 0)
                        {
                            c.tpCoSkin |= 1 << skillTpl;
                        }
                        else
                        {
                            c.tpCoSkin &= ~(1 << skillTpl);
                        }
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
            // Tai truoc moi khung cua moi skin (anh chi tai khi lan dau can ve; hieu
            // ung ngan nhu kunai 0,23 giay la het truoc khi anh kip tai — khong thay gi).
            foreach (Chieu ch in dsChieu)
            {
                foreach (Mau mm in ch.ds)
                {
                    taiTruoc(mm.nap);
                    taiTruoc(mm.bay);
                }
            }
            taiTruoc(ICON_DA);
            // Nho skin dang bat cua chinh minh ngay khi co danh sach.
            Char toi = Char.myCharz();
            if (toi != null)
            {
                foreach (Chieu ch in dsChieu)
                {
                    Mau m = timMau(ch, ch.dangDung);
                    if (m != null && m.daCo)
                    {
                        toi.tpCoSkin |= 1 << ch.tpl;
                    }
                    else
                    {
                        toi.tpCoSkin &= ~(1 << ch.tpl);
                    }
                }
            }
        }

        /// <summary>Xin tải trước các ảnh chưa có (bỏ qua -1 và ảnh đã tải).</summary>
        public static void taiTruoc(short[] ids)
        {
            if (ids == null || SmallImage.imgNew == null)
            {
                return;
            }
            foreach (short id in ids)
            {
                if (id >= 0 && id < SmallImage.imgNew.Length && SmallImage.imgNew[id] == null)
                {
                    SmallImage.createImage(id);
                }
            }
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
                string phu = dung != null ? dung.ten : string.Empty;
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
                string ten = i == 0 ? c.ten : c.ds[i - 1].ten;
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
                int khung = khungXemTruoc(tach(m.nap, 0), tach(m.bay, 0), lucChonO);
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
            mFont.tahoma_7b_yellow.drawString(g, m != null ? m.ten : c.ten, xc, k[1] + 12, mFont.LEFT,
                    mFont.tahoma_7b_dark);
            string moTa = m != null ? m.moTa : string.Empty;
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
                mFont.tahoma_7b_white.drawString(g, oChon == 0 ? "SỬ DỤNG" : "BẬT", n[0] + n[2] / 2, n[1] + 7,
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
            // Khung cham dich ve o lop chung (veToanCuc).
            if (!laQCKK(c))
            {
                return false;
            }
            // Tu xong, dang vung tay nem (qua cau chua roi tay): lap khung nem/cham.
            if (!c.isFlyAndCharge)
            {
                if (c.isUseSkillAfterCharge && c.dart == null)
                {
                    veVongBay(g, c.tpBay, c.cx, c.cy - c.ch - CAO_TAM, TO_CAU);
                    return true;
                }
                return false;
            }
            int k = khungTheoGio(c.tpNap, c.tpBay, c.tpLuc);
            float to = coLucTu(c);
            // Mot tam co dinh du to hay be: phong quanh chinh tam qua cau.
            int tamY = c.cy - c.ch - CAO_TAM;
            veDaBayVao(g, c, c.cx, tamY);
            if (k >= 0)
            {
                veTuQCKK(g, c, c.cx, tamY, to * TO_CAU);
            }
            return true;
        }

        /// <summary>
        /// Quả cầu đang bay (sau khi tụ xong): vẽ khung lặp thay cho hình gốc.
        /// Trả false nếu chủ quả cầu dùng hình gốc — để vẽ như cũ.
        /// </summary>
        public static bool veKhiBay(mGraphics g, Char chu, int x, int y)
        {
            if (!chanGoc(chu))
            {
                return false;
            }
            // Qua cau bay ve o lop chung (veToanCuc) — o day chi chan hinh goc.
            return true;
        }

        // ------------------------------------------------------------------
        //  Đá bay vào lúc tụ (thay đạn trắng gốc của quái)
        // ------------------------------------------------------------------
        /// <summary>Mười hai cục đá cắt từ frame 1 và 2 của Địa Bộc Thiên Tinh.</summary>
        private static readonly short[] ICON_DA = {
            25289, 25290, 25291, 25292, 25293, 25294, 25295, 25296, 25297, 25298, 25299, 25300
        };

        /// <summary>Vẽ đá ở 0,75 cỡ bộ gốc (= 1,5 lần bộ nửa cỡ trước).</summary>
        private const float TI_LE_DA = 0.75f;

        /// <summary>
        /// Hướng mũi nhọn của từng cục đá trong ảnh (độ, chiều kim đồng hồ, 0 = sang
        /// phải) — dò bằng điểm xa tâm nhất. Hai cục có vệt sáng (thứ 10, 12) lật
        /// 180° để mũi đi trước, vệt đi sau.
        /// </summary>
        private static readonly float[] MUI_DA = {
            -82f, 112f, 110f, 126f, -133f, 155f, -141f, -32f, -45f, -23f, 163f, -27f
        };

        /// <summary>Cứ chừng này ms sinh một cục đá.</summary>
        private const long MS_SINH_DA = 50L;

        private static readonly System.Random ngauNhien = new System.Random();

        public class Da
        {
            public short icon;
            public int loai;
            /// <summary>Bán kính, góc xuất phát quanh tâm quả cầu.</summary>
            public float r0;
            public float a0;
            /// <summary>Chiều xoáy: +1 / -1.</summary>
            public float chieu;
            public long batDau;
            public long thoiGian;
        }

        /// <summary>
        /// Đá từ xa bốn phía bay thẳng về tâm quả cầu, nhanh dần như bị hút, lặn vào
        /// sau quả cầu. Chỉ chạy lúc đang tụ và trang phục có khung đá.
        /// </summary>
        private static void veDaBayVao(mGraphics g, Char c, int tamX, int tamY)
        {
            long bayGio = mSystem.currentTimeMillis();
            if (c.tpDa == null)
            {
                c.tpDa = new List<Da>();
                c.tpDaLuc = bayGio;
            }
            // Sinh bu theo gio (khung hinh rot van du so da).
            int sinh = 0;
            while (bayGio - c.tpDaLuc >= MS_SINH_DA && sinh < 4)
            {
                c.tpDaLuc += MS_SINH_DA;
                sinh++;
                if (c.tpDa.Count >= 45)
                {
                    continue;
                }
                Da d = new Da();
                d.loai = ngauNhien.Next(ICON_DA.Length);
                d.icon = ICON_DA[d.loai];
                d.r0 = 160f + (float) ngauNhien.NextDouble() * 100f;
                d.a0 = (float) (ngauNhien.NextDouble() * System.Math.PI * 2);
                d.chieu = ngauNhien.Next(2) == 0 ? 1f : -1f;
                d.batDau = bayGio;
                d.thoiGian = 520L + ngauNhien.Next(320);
                c.tpDa.Add(d);
            }
            if (bayGio - c.tpDaLuc > 500L)
            {
                c.tpDaLuc = bayGio;
            }
            for (int i = c.tpDa.Count - 1; i >= 0; i--)
            {
                Da d = c.tpDa[i];
                float p = (float) (bayGio - d.batDau) / d.thoiGian;
                if (p >= 1f)
                {
                    c.tpDa.RemoveAt(i);
                    continue;
                }
                // Tu xa bay THANG ve tam, nhanh dan (bi hut).
                float e = p * p;
                float r = d.r0 * (1f - e);
                float a = d.a0;
                int x = tamX + (int) (r * System.Math.Cos(a));
                int y = tamY + (int) (r * System.Math.Sin(a) * 0.8f);
                // Huong bay = tu cho da ve tam (nguoc goc xuat phat): xoay de mui nhon chi vao tam.
                float huongBay = (float) (System.Math.Atan2(-System.Math.Sin(a) * 0.8, -System.Math.Cos(a)) * 57.29578);
                SmallImage.veIconXoay(g, d.icon, x, y, TI_LE_DA, huongBay - MUI_DA[d.loai]);
            }
        }

        /// <summary>Phóng cả quả cầu (tụ, ném, bay, nổ) — người dùng muốn to hơn 30%.</summary>
        private const float TO_CAU = 1.3f;

        /// <summary>Tâm quả cầu cao hơn đỉnh đầu nhân vật chừng này điểm.</summary>
        private const int CAO_TAM = 60;

        /// <summary>Quả cầu lúc tụ, hoà dần khung này sang khung kế (tụ một lượt rồi lặp hai khung cuối).</summary>
        private static void veTuQCKK(mGraphics g, Char c, int x, int y, float tiLe)
        {
            short[] nap = c.tpNap;
            if (nap == null || nap.Length == 0)
            {
                veVongBay(g, c.tpBay, x, y, tiLe);
                return;
            }
            long troi = mSystem.currentTimeMillis() - c.tpLuc;
            int n = nap.Length;
            if (troi < n * MS_NAP || n < 2)
            {
                float f = System.Math.Min(n - 1, (float) troi / MS_NAP);
                int a = (int) f;
                int b = System.Math.Min(n - 1, a + 1);
                veHoa(g, nap[a], nap[b], f - a, x, y, tiLe);
                return;
            }
            float f2 = (float) (troi - n * MS_NAP) / MS_BAY;
            int buoc = (int) f2;
            int ka = n - 2 + buoc % 2;
            int kb = n - 2 + (buoc + 1) % 2;
            veHoa(g, nap[ka], nap[kb], f2 - buoc, x, y, tiLe);
        }

        /// <summary>Lặp hai khung đầu của dãy bay (ném ↔ chạm), hoà dần qua lại.</summary>
        private static void veVongBay(mGraphics g, short[] bay, int x, int y, float tiLe)
        {
            if (bay == null || bay.Length == 0)
            {
                return;
            }
            int n = System.Math.Min(2, bay.Length);
            // Tinh bang long: gio he thong ~1e12 ms dua vao float la mat phan le.
            long ms = mSystem.currentTimeMillis();
            long buoc = ms / MS_BAY;
            float t = (float) (ms % MS_BAY) / MS_BAY;
            int a = (int) (buoc % n);
            int b = (int) ((buoc + 1) % n);
            veHoa(g, bay[a], bay[b], t, x, y, tiLe);
        }

        /// <summary>Cỡ nhỏ nhất lúc mới bắt đầu tụ (phần của cỡ đầy đủ).</summary>
        private const float CO_NHO_NHAT = 0.25f;

        /// <summary>Đạt cỡ đầy đủ trước lúc ném chừng này (ms) rồi giữ nguyên.</summary>
        private const long GIU_TO_NHAT = 200L;

        /// <summary>
        /// Cỡ quả cầu lúc đang tụ: lớn dần từ <see cref="CO_NHO_NHAT"/> tới 1, đạt
        /// cỡ đầy đủ đúng <see cref="GIU_TO_NHAT"/> ms trước lúc ném.
        /// </summary>
        /// <remarks>
        /// Game ném khi: bay lên đủ 20 nhịp (lúc đó đặt <c>last</c>), rồi thêm
        /// <c>seconds</c> ms. Của mình thì <c>seconds</c> do máy chủ gửi; người khác
        /// client không biết (để 50000) nên lấy 3 giây.
        /// </remarks>
        private static float coLucTu(Char c)
        {
            long bayGio = mSystem.currentTimeMillis();
            long van = (c.seconds > 0 && c.seconds < 50000) ? c.seconds : 3000L;
            long nem = c.posDisY >= 20 ? c.last + van : bayGio + (20 - c.posDisY) * 33L + van;
            long toNhat = nem - GIU_TO_NHAT;
            long tong = toNhat - c.tpLuc;
            if (tong <= 0)
            {
                return 1f;
            }
            float p = (float) (bayGio - c.tpLuc) / tong;
            if (p >= 1f)
            {
                return 1f;
            }
            if (p < 0f)
            {
                p = 0f;
            }
            // Nhanh luc dau, cham dan khi gan to nhat.
            float e = 1f - (1f - p) * (1f - p);
            return CO_NHO_NHAT + (1f - CO_NHO_NHAT) * e;
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
            long bayGio = mSystem.currentTimeMillis();
            return c != null && c.tpBay != null && c.tpBay.Length > 0 && !c.tpXong
                    && bayGio - c.tpLuc < HAN_HIEU_LUC && (c.tpHetLuc == 0 || bayGio < c.tpHetLuc);
        }

        /// <summary>Còn phải chặn hình gốc: đang có trang phục, hoặc vừa nổ xong chưa quá 2,5 giây.</summary>
        public static bool chanGoc(Char c)
        {
            return conHieuLuc(c) || (c != null && mSystem.currentTimeMillis() < c.tpChanDen) || dangVeChieuCoSkin(c);
        }

        /// <summary>
        /// Game đang vẽ một chiêu mà nhân vật này có skin — theo mã hoạt ảnh của chiêu,
        /// không theo lần ném: Makankosappo 77–83 (cả gồng tay Namếc), Quả cầu kênh
        /// khi 70–76. Chặn cả khi gói trang phục tới trễ hay đã hết hạn.
        /// </summary>
        private static bool dangVeChieuCoSkin(Char c)
        {
            if (c == null || c.tpCoSkin == 0)
            {
                return false;
            }
            int id = c.skillPaint != null ? c.skillPaint.id
                    : (c.dart != null && c.dart.skillPaint != null ? c.dart.skillPaint.id : -1);
            if ((c.tpCoSkin & (1 << 11)) != 0)
            {
                if (id >= 77 && id <= 83)
                {
                    return true;
                }
                if (c.isStandAndCharge && c.cgender == 1)
                {
                    return true;
                }
            }
            if ((c.tpCoSkin & (1 << 20)) != 0 && ((id >= 63 && id <= 69) || (id >= 128 && id <= 134)))
            {
                return true;
            }
            if ((c.tpCoSkin & (1 << 10)) != 0 && id >= 70 && id <= 76)
            {
                return true;
            }
            return false;
        }

        /// <summary>Đánh dấu lần tụ đã nổ, vẫn chặn hình gốc thêm một lát.</summary>
        private static void ketThuc(Char c)
        {
            c.tpXong = true;
            c.tpChanDen = mSystem.currentTimeMillis() + 2500L;
        }

        /// <summary>Đang mang trang phục Quả cầu kênh khi.</summary>
        public static bool laQCKK(Char c)
        {
            return conHieuLuc(c) && c.tpSkill == 10;
        }

        /// <summary>Đang mang trang phục Tự phát nổ.</summary>
        public static bool laTuNo(Char c)
        {
            return conHieuLuc(c) && c.tpSkill == 14;
        }

        // ------------------------------------------------------------------
        //  Dư âm vụ nổ (vẽ trên bản đồ, dưới nhân vật / quái — xem GameScr.paint)
        // ------------------------------------------------------------------
        public class DuAm
        {
            public int icon;
            public int x;
            /// <summary>Mặt đất (chân mục tiêu) tại chỗ nổ.</summary>
            public int dat;
            /// <summary>Tâm ảnh lệch so với mặt đất (dương = thấp xuống).</summary>
            public int dy = -LECH_DAT;
            /// <summary>Phóng ảnh dư âm.</summary>
            public float tiLe = 1f;
            public long batDau;
        }

        private static readonly List<DuAm> dsDuAm = new List<DuAm>();

        /// <summary>Dư âm tồn tại bao lâu sau khi vụ nổ kết thúc (ms), và mờ dần trong bao lâu cuối.</summary>
        private const long MS_DU_AM = 3000L;
        private const long MS_DU_AM_MO = 2500L;

        /// <summary>Tâm ảnh đặt dưới mặt đất chừng này điểm (âm = thấp xuống) để hố nứt nằm sát đất.</summary>
        private const int LECH_DAT = -14;

        /// <summary>
        /// Quả cầu đang bay và khung chạm địch của MỌI nhân vật — gọi trong
        /// GameScr.paint sau khi vẽ nhân vật.
        /// </summary>
        /// <remarks>
        /// Trước vẽ bên trong hàm vẽ của người ném. Game bỏ qua hàm đó khi người
        /// ném ra ngoài màn hình hay đang ở trạng thái đặc biệt, nên lúc có lúc
        /// không thấy quả cầu bay đi và vụ nổ. Vẽ ở đây thì luôn hiện.
        /// </remarks>
        public static void veToanCuc(mGraphics g)
        {
            xinKhiVao();
            xuLyAm();
            veCauBu(g);
            veNoTuNo(g);
            veXoayMo(g);
            veRasenBu(g);
            veKunaiBay(g);
            veKiemBay(g);
            veMotNguoi(g, Char.myCharz());
            for (int i = 0; i < GameScr.vCharInMap.size(); i++)
            {
                Char c = GameScr.vCharInMap.elementAt(i) as Char;
                if (c != null && c != Char.myCharz())
                {
                    veMotNguoi(g, c);
                }
            }
        }

        // ------------------------------------------------------------------
        //  Ném bù: tụ xong mà game không tạo quả cầu bay
        // ------------------------------------------------------------------
        public class CauBu
        {
            public Char chu;
            public short[] bay;
            public int x0, y0, x1, y1, dat;
            public long batDau;
        }

        private static readonly List<CauBu> dsCauBu = new List<CauBu>();

        /// <summary>Quả cầu ném bù bay bao lâu (ms).</summary>
        private const long MS_CAU_BU = 380L;

        /// <summary>
        /// Gọi đầu <c>Char.stopUseChargeSkill</c>. Game chỉ tạo quả cầu bay khi
        /// đúng lúc ném còn mục tiêu; mất mục tiêu (vừa chết, đổi, auto chưa kịp
        /// chọn) là không có quả cầu, không vụ nổ, không dư âm — dù máy chủ vẫn
        /// tính sát thương. Khi đó ném bù quả cầu của trang phục tới mục tiêu cuối
        /// (hay trước mặt), để lần nào cũng có đủ.
        /// </summary>
        public static void nemNeuChuaNem(Char c)
        {
            if (laRasen(c))
            {
                // Gong xong: cho game tao qua cau bay; khong co thi veRasen nem bu.
                if (c.isStandAndCharge && mSystem.currentTimeMillis() - c.tpLuc >= thoiGianGong(c) - 400L)
                {
                    c.tpChoDart = mSystem.currentTimeMillis();
                }
                return;
            }
            if (laTuNo(c))
            {
                noTuSat(c);
                return;
            }
            if (!laQCKK(c) || c.dart != null || c.tpBay.Length < 2)
            {
                return;
            }
            if (!c.isUseSkillAfterCharge && !c.isFlyAndCharge)
            {
                return;
            }
            // Bi ngat ngay dau luc tu (chua kip thanh qua cau) thi thoi.
            if (mSystem.currentTimeMillis() - c.tpLuc < 1200L)
            {
                return;
            }
            IMapObject mt = c.mobFocus != null ? (IMapObject) c.mobFocus : c.charFocus;
            CauBu b = new CauBu();
            b.chu = c;
            b.bay = c.tpBay;
            b.x0 = c.cx;
            b.y0 = c.cy - c.ch - CAO_TAM;
            if (mt != null)
            {
                b.x1 = mt.getX();
                b.dat = mt.getY();
                b.y1 = mt.getY() - mt.getH() / 2;
            }
            else
            {
                b.x1 = c.cx + (c.cdir >= 0 ? 1 : -1) * 120;
                b.dat = c.cy;
                b.y1 = c.cy - 15;
            }
            b.batDau = mSystem.currentTimeMillis();
            c.tpXong = true;
            lock (dsCauBu)
            {
                dsCauBu.Add(b);
            }
        }

        /// <summary>Vẽ các quả cầu ném bù; tới nơi thì nổ + dư âm.</summary>
        private static void veCauBu(mGraphics g)
        {
            if (dsCauBu.Count == 0)
            {
                return;
            }
            long bayGio = mSystem.currentTimeMillis();
            lock (dsCauBu)
            {
                for (int i = dsCauBu.Count - 1; i >= 0; i--)
                {
                    CauBu b = dsCauBu[i];
                    float p = (float) (bayGio - b.batDau) / MS_CAU_BU;
                    if (p >= 1f)
                    {
                        dsCauBu.RemoveAt(i);
                        ghiNo(b.chu, b.bay, b.x1, b.y1, b.dat);
                        continue;
                    }
                    float e = p * p;
                    int x = b.x0 + (int) ((b.x1 - b.x0) * e);
                    int y = b.y0 + (int) ((b.y1 - b.y0) * e);
                    veVongBay(g, b.bay, x, y, TO_CAU);
                }
            }
        }

        /// <summary>
        /// Chốt an toàn: <c>isCreateDark</c> (game dùng để chặn đổi chiêu) bật mà không
        /// còn gồng, không quả cầu, không tư thế ra chiêu quá 1,5 giây — tức kẹt (quả
        /// cầu không được tạo nên không ai tắt) — thì dừng tụ để mở khoá đổi chiêu.
        /// </summary>
        private static void chotMoKhoa(Char c)
        {
            if (!c.me)
            {
                return;
            }
            bool ket = c.isCreateDark && !c.isFlyAndCharge && !c.isStandAndCharge
                    && c.dart == null && c.skillPaint == null;
            if (!ket)
            {
                c.tpLucKet = 0;
                return;
            }
            long bayGio = mSystem.currentTimeMillis();
            if (c.tpLucKet == 0)
            {
                c.tpLucKet = bayGio;
            }
            else if (bayGio - c.tpLucKet > 1500L)
            {
                c.tpLucKet = 0;
                c.stopUseChargeSkill();
            }
        }

        private static void veMotNguoi(mGraphics g, Char c)
        {
            if (c == null)
            {
                return;
            }
            chotMoKhoa(c);
            long bayGio = mSystem.currentTimeMillis();
            // Qua cau dang bay: lap khung nem / cham (9 <-> 10).
            veGongTuNo(g, c);
            veRasen(g, c);
            if (laQCKK(c) && c.dart != null && c.dart.isActive)
            {
                veVongBay(g, c.tpBay, c.dart.x, c.dart.y, TO_CAU);
            }
            // Khung cham dich: giu mot lat tai cho qua cau trung, rung du chan.
            long daQua = bayGio - c.tpTrungLuc;
            if (c.tpTrung >= 0 && daQua < MS_TRUNG)
            {
                int bien = (int) (4 * (MS_TRUNG - daQua) / MS_TRUNG);
                int lech = (GameCanvas.gameTick % 2 == 0) ? bien : -bien;
                SmallImage.veIconXoay(g, c.tpTrung, c.tpTrungX + lech, c.tpTrungY + lech / 2, TO_CAU, 0f);
            }
        }

        /// <summary>Gọi trong GameScr.paint ngay sau lớp bản đồ, trước nhân vật / quái.</summary>
        public static void veDuAm(mGraphics g)
        {
            if (dsDuAm.Count == 0)
            {
                return;
            }
            long bayGio = mSystem.currentTimeMillis();
            lock (dsDuAm)
            {
                for (int i = dsDuAm.Count - 1; i >= 0; i--)
                {
                    DuAm d = dsDuAm[i];
                    long t = bayGio - d.batDau;
                    if (t >= MS_DU_AM)
                    {
                        dsDuAm.RemoveAt(i);
                        continue;
                    }
                    if (t < 0)
                    {
                        continue;
                    }
                    float mo = t < MS_DU_AM - MS_DU_AM_MO ? 1f : (float) (MS_DU_AM - t) / MS_DU_AM_MO;
                    SmallImage.veIconXoay(g, d.icon, d.x, d.dat + d.dy, d.tiLe, 0f, mo);
                }
            }
        }

        /// <summary>Quả cầu vừa trúng: ghi khung chạm (khung thứ hai của dãy bay) để vẽ một lát.</summary>
        public static void ghiChamDich(Char chu, int x, int y, long lucGan)
        {
            if (!conHieuLuc(chu) || chu.tpBay.Length < 2 || lucGan != chu.tpLuc)
            {
                return;
            }
            if (chu.tpSkill == 11)
            {
                noRasen(chu, x, y);
                return;
            }
            IMapObject mt = chu.mobFocus != null ? (IMapObject) chu.mobFocus : chu.charFocus;
            ghiNo(chu, chu.tpBay, x, y, mt != null ? mt.getY() : y + 25);
        }

        /// <summary>Ghi vụ nổ (khung chạm) và dư âm tại (x, y), mặt đất ở <paramref name="dat"/>; kết thúc lần tụ.</summary>
        // ------------------------------------------------------------------
        //  Tự phát nổ (skill 14): gồng quanh thân, nổ, dư âm hố nứt
        // ------------------------------------------------------------------
        /// <summary>
        /// Tách dãy khung theo dấu -1: <paramref name="phan"/> 0 là phần trước (lớp
        /// mặt đất), 1 là phần sau (lớp giữa thân). Không có -1 thì phần 1 rỗng.
        /// </summary>
        public static short[] tach(short[] a, int phan)
        {
            if (a == null)
            {
                return new short[0];
            }
            // Doan thu `phan` giua cac dau -1 (0 = doan dau).
            int tu = 0;
            int dem = 0;
            for (int i = 0; i <= a.Length; i++)
            {
                if (i == a.Length || a[i] == -1)
                {
                    if (dem == phan)
                    {
                        short[] ra = new short[i - tu];
                        System.Array.Copy(a, tu, ra, 0, ra.Length);
                        return ra;
                    }
                    dem++;
                    tu = i + 1;
                }
            }
            return new short[0];
        }

        // ------------------------------------------------------------------
        //  Makankosappo → Rasenshuriken: ba loại xoáy
        //    trên tay (nạp) · ném đi (bay, xoay theo hướng) · ở địch (nổ + xoáy dư âm, to nhất)
        // ------------------------------------------------------------------
        private const float RASEN_TAY = 1f;
        private const float RASEN_BAY = 1.1f;
        private const float RASEN_NO = 1.6f;
        private const float RASEN_XOAY = 1.8f;

        /// <summary>Xoáy dư âm ở địch: giữ rõ bao lâu, rồi mờ tới hết bao lâu (ms).</summary>
        private const long MS_XOAY_RO = 1500L;
        private const long MS_XOAY = 3000L;

        /// <summary>Tốc độ tối đa quả cầu bay (đơn vị PlayerDart.va): Rasenshuriken chậm để thấy rõ.</summary>
        public static int tocDoToiDa(Char c)
        {
            return 8192;
        }

        /// <summary>
        /// Nhân vật của mình vừa bấm chiêu tụ: gắn trang phục đang chọn NGAY từ danh
        /// sách đã tải — gói máy chủ tới trễ vài khung, trong lúc đó hình gốc lọt ra.
        /// Gói máy chủ tới sau chỉ ghi đè cho khớp.
        /// </summary>
        public static void ganTruoc(Char c, int skillTpl)
        {
            TrangPhucUI ui = getInstance();
            if (!ui.coDuLieu)
            {
                ui.xinTruoc();
                return;
            }
            foreach (Chieu ch in ui.dsChieu)
            {
                if (ch.tpl != skillTpl)
                {
                    continue;
                }
                Mau m = timMau(ch, ch.dangDung);
                if (m == null || !m.daCo)
                {
                    return;
                }
                c.tpNap = m.nap != null && m.nap.Length > 0 ? m.nap : null;
                c.tpBay = m.bay != null && m.bay.Length > 0 ? m.bay : null;
                c.tpLuc = mSystem.currentTimeMillis();
                c.tpXong = false;
                c.tpSkill = skillTpl;
                c.tpHetLuc = 0;
                return;
            }
        }

        public static bool laRasen(Char c)
        {
            return conHieuLuc(c) && c.tpSkill == 11;
        }

        /// <summary>Góc quay (độ) sau <paramref name="ms"/> ms với <paramref name="vong"/> vòng/giây.</summary>
        private static float gocQuay(long ms, float vong)
        {
            long chuKy = (long) (1000f / vong);
            return (float) (ms % chuKy) * 360f / chuKy;
        }

        /// <summary>
        /// Nạp: shuriken cầm ở TAY SAU (phía ngược hướng mặt), lớn dần suốt ~60%
        /// thời gian gồng rồi lặp hai khung cuối — luôn quay quanh tâm. Bay: vệt
        /// gió nằm theo hướng bay (không quay) + shuriken quay tít ở đầu.
        /// </summary>
        private static void veRasen(mGraphics g, Char c)
        {
            if (!laRasen(c))
            {
                return;
            }
            long ms = mSystem.currentTimeMillis();
            short[] nap = c.tpNap;
            int n = nap == null ? 0 : nap.Length;
            // Ve thang len qua cau THAT (bay dung toc do goc, khop luc gay sat thuong).
            // Game khong tao qua cau (mat muc tieu) thi nem bu.
            if (c.tpChoDart > 0)
            {
                if (c.dart != null)
                {
                    c.tpChoDart = 0;
                }
                else if (ms - c.tpChoDart > 350L)
                {
                    c.tpChoDart = 0;
                    nemBuRasen(c);
                    return;
                }
            }
            if (c.dart != null && c.dart.isActive)
            {
                short[] bay = tach(c.tpBay, 0);
                float huong = (float) (System.Math.Atan2(c.dart.vy, c.dart.vx) * 57.29578);
                if (bay.Length > 0)
                {
                    // Vet gio: khung bay cuoi (vet dai), mo, nam theo huong bay.
                    SmallImage.veIconXoay(g, bay[bay.Length - 1], c.dart.x, c.dart.y, RASEN_BAY, huong, 0.55f);
                }
                if (n > 0)
                {
                    SmallImage.veIconXoay(g, nap[n - 1], c.dart.x, c.dart.y, RASEN_BAY, gocQuay(ms, 2.5f));
                }
                return;
            }
            if (!c.isStandAndCharge || n == 0)
            {
                return;
            }
            long troi = ms - c.tpLuc;
            long lon = System.Math.Max(300L, thoiGianGong(c) * 6 / 10);
            int x = c.cx - (c.cdir >= 0 ? 1 : -1) * 18;
            int y = c.cy - c.ch / 2 - 2;
            float goc = gocQuay(ms, 2f);
            int a;
            int b;
            float t;
            if (troi < lon || n < 2)
            {
                float fx = System.Math.Min(n - 1, (float) troi / lon * (n - 1));
                a = (int) fx;
                b = System.Math.Min(n - 1, a + 1);
                t = fx - a;
            }
            else
            {
                // Da thanh hinh: mot khung quay deu, khong doi khung nua.
                a = n - 1;
                b = n - 1;
                t = 0f;
            }
            SmallImage.veIconXoay(g, nap[a], x, y, RASEN_TAY, goc);
            if (b != a && t > 0.02f)
            {
                SmallImage.veIconXoay(g, nap[b], x, y, RASEN_TAY, goc, t);
            }
        }

        public class RasenBu
        {
            public short[] bay;
            public short cau;
            public int x0, y0, x1, y1;
            public long batDau;
            /// <summary>Mục tiêu để đuổi theo nếu nó di chuyển (null = điểm cố định).</summary>
            public IMapObject mt;
        }

        private static readonly List<RasenBu> dsRasenBu = new List<RasenBu>();

        /// <summary>Gồng xong mà game không tạo quả cầu (mất mục tiêu): tự ném shuriken tới mục tiêu cuối / trước mặt.</summary>
        private static void nemBuRasen(Char c)
        {
            IMapObject mt = c.mobFocus != null ? (IMapObject) c.mobFocus : c.charFocus;
            RasenBu b = new RasenBu();
            b.bay = c.tpBay;
            b.cau = c.tpNap[c.tpNap.Length - 1];
            b.x0 = c.cx - (c.cdir >= 0 ? 1 : -1) * 18;
            b.y0 = c.cy - c.ch / 2 - 2;
            b.mt = mt;
            if (mt != null)
            {
                b.x1 = mt.getX();
                b.y1 = mt.getY() - mt.getH() / 2;
            }
            else
            {
                b.x1 = c.cx + (c.cdir >= 0 ? 1 : -1) * 140;
                b.y1 = b.y0;
            }
            b.batDau = mSystem.currentTimeMillis();
            ketThuc(c);
            // Chan hinh goc (qua cau that dang bay, don danh phat lai) suot luc bay + no.
            c.tpChanDen = b.batDau + 3500L;
            // Game khong tao qua cau thi cung khong ai tat isCreateDark (co khoa doi
            // chieu) — dung tu o day cho no mo khoa.
            if (c.dart == null)
            {
                c.stopUseChargeSkill();
            }
            lock (dsRasenBu)
            {
                dsRasenBu.Add(b);
            }
        }

        private static void veRasenBu(mGraphics g)
        {
            if (dsRasenBu.Count == 0)
            {
                return;
            }
            long ms = mSystem.currentTimeMillis();
            lock (dsRasenBu)
            {
                for (int i = dsRasenBu.Count - 1; i >= 0; i--)
                {
                    RasenBu b = dsRasenBu[i];
                    if (b.mt != null)
                    {
                        // Duoi theo muc tieu dang chay.
                        b.x1 = b.mt.getX();
                        b.y1 = b.mt.getY() - b.mt.getH() / 2;
                    }
                    float p = (float) (ms - b.batDau) / 300f;
                    if (p >= 1f)
                    {
                        dsRasenBu.RemoveAt(i);
                        noRasenTai(b.bay, b.x1, b.y1);
                        continue;
                    }
                    int x = b.x0 + (int) ((b.x1 - b.x0) * p);
                    int y = b.y0 + (int) ((b.y1 - b.y0) * p);
                    short[] bay = tach(b.bay, 0);
                    float huong = (float) (System.Math.Atan2(b.y1 - b.y0, b.x1 - b.x0) * 57.29578);
                    if (bay.Length > 0)
                    {
                        SmallImage.veIconXoay(g, bay[bay.Length - 1], x, y, RASEN_BAY, huong, 0.55f);
                    }
                    SmallImage.veIconXoay(g, b.cau, x, y, RASEN_BAY, gocQuay(ms, 2.5f));
                }
            }
        }

        // ------------------------------------------------------------------
        //  Đẻ trứng → Vạn Kiếm Quy Tông (50 khung, 7 đoạn):
        //    0 xoáy bên chủ (12: chụm 6 + xoáy 6) · 1 bay tới địch (9) · 2 chụm vào địch (8)
        //    3 quấn quanh địch (9, lặp) · 4 kết thúc ở địch: loé sáng rồi tan (9)
        //    5 bay về chủ (3) · 6 chết (2)
        //  Mỗi cú đánh: kiếm rời xoáy sau lưng chủ → lao tới, chụm vào địch → quấn một
        //  lúc → loé tan, một luồng kiếm bay về → chụm lại sau lưng chủ.
        // ------------------------------------------------------------------
        /// <summary>Mỗi khung chụm kiếm lúc trứng nở / lúc kiếm bay về (ms).</summary>
        private const long VK_TU = 150L;
        private const long VK_CHUM_VE = 95L;
        private const long VK_XOAY = 150L;
        private const long VK_BAY = 45L;
        private const long VK_CHUM = 60L;
        private const long VK_QUAN_KHUNG = 90L;
        private const long VK_KET = 60L;
        private const long VK_VE_KHUNG = 70L;
        private const long VK_THU = 90L;
        private const long VK_HIEN = 150L;
        private const long VK_MO_CHET = 250L;

        /// <summary>Kiếm quấn quanh địch bao lâu (ms); cú đánh mới tới cùng địch thì quấn thêm.</summary>
        private const long VK_QUAN = 400L;

        /// <summary>Mỗi quả trứng nhiều nhất bấy nhiêu đợt kiếm cùng lúc (đấm nhanh quá thì bỏ đợt cũ nhất).</summary>
        private const int VK_TOI_DA = 6;

        /// <summary>Bay bay: chu kỳ (ms) và biên độ nhấp nhô dọc / lắc ngang.</summary>
        private const long VK_CHU_KY_Y = 2000L;
        private const long VK_CHU_KY_X = 3300L;
        private const int VK_LAC_Y = 4;
        private const int VK_LAC_X = 2;

        /// <summary>Xoáy quay bao nhiêu vòng/giây (theo chiều kim đồng hồ, như mũi kiếm trong ảnh).</summary>
        private const float VK_VONG = 0.4f;
        private const float VK_VONG_QUAN = 0.7f;

        /// <summary>Tâm xoáy cao hơn chân quả trứng bấy nhiêu.</summary>
        private const int VK_CAO = 30;

        /// <summary>Một lượt kiếm rời chủ.</summary>
        public class KiemBay
        {
            public Mob nguon;
            public IMapObject dich;
            public int x0, y0;
            public long batDau;

            /// <summary>Lúc tới địch (0 = đang bay), lúc hết quấn, lúc về tới chủ (0 = chưa).</summary>
            public long denLuc;
            public long quanHet;
            public bool daVe;

            /// <summary>Góc bay lúc tới địch — khung chụm giữ hướng này, xoáy quấn quay tiếp từ đó.</summary>
            public float gocDen;
        }

        private static readonly List<KiemBay> dsKiem = new List<KiemBay>();

        /// <summary>
        /// Gọi khi quả trứng (mobMe) vừa nở: chủ có trang phục Đẻ trứng thì gắn khung
        /// cho quả trứng — nó giữ suốt đời, không theo thời hạn gói trang phục.
        /// </summary>
        public static void ganTrung(Char chu, Mob mob)
        {
            if (chu == null || mob == null)
            {
                return;
            }
            if (chu.me && !(chu.tpSkill == 12 && conHieuLuc(chu)))
            {
                ganTruoc(chu, 12);
            }
            if (chu.tpSkill != 12 || !conHieuLuc(chu) || tach(chu.tpBay, 0).Length < 12)
            {
                mob.tpVK = null;
                return;
            }
            mob.tpVK = chu.tpBay;
            mob.tpVKSinh = mSystem.currentTimeMillis();
            mob.tpVKDanh = 0;
            mob.tpVKVe = 0;
            mob.tpVKChet = 0;
            taiTruoc(chu.tpBay);
            // Khong chan hinh goc cua chu (ketThuc): chi danh dau da dung goi nay.
            chu.tpXong = true;
        }

        /// <summary>Quả trứng có skin vừa đánh <paramref name="dich"/>: một luồng kiếm rời xoáy lao tới.</summary>
        public static void vanKiemDanh(Mob m, IMapObject dich)
        {
            if (dich == null || tach(m.tpVK, 1).Length == 0)
            {
                return;
            }
            long ms = mSystem.currentTimeMillis();
            KiemBay k = new KiemBay();
            k.nguon = m;
            k.dich = dich;
            k.x0 = m.x;
            k.y0 = m.y - VK_CAO;
            k.batDau = ms;
            lock (dsKiem)
            {
                int dem = 0;
                int cuNhat = -1;
                for (int i = 0; i < dsKiem.Count; i++)
                {
                    if (dsKiem[i].nguon == m)
                    {
                        dem++;
                        if (cuNhat < 0)
                        {
                            cuNhat = i;
                        }
                    }
                }
                if (dem >= VK_TOI_DA && cuNhat >= 0)
                {
                    dsKiem.RemoveAt(cuNhat);
                }
                dsKiem.Add(k);
            }
        }

        /// <summary>Kiếm của quả trứng này còn rời chủ không (đang bay / ở địch / đang bay về).</summary>
        private static bool conVang(Mob m)
        {
            lock (dsKiem)
            {
                foreach (KiemBay k in dsKiem)
                {
                    if (k.nguon == m && !k.daVe)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        /// <summary>Ảnh nào dài hơn cao thì "mũi" (khối tròn đầu luồng kiếm) lệch khỏi tâm ảnh bấy nhiêu.</summary>
        private static float lechMui(short id)
        {
            var s = (SmallImage.imgNew != null && id >= 0 && id < SmallImage.imgNew.Length) ? SmallImage.imgNew[id] : null;
            if (s == null || s.img == null)
            {
                return 0f;
            }
            float w = mGraphics.getImageWidth(s.img);
            float h = mGraphics.getImageHeight(s.img);
            return System.Math.Max(0f, (w - h) / 2f);
        }

        /// <summary>Vẽ khung sao cho "mũi" nằm đúng (ax, ay), xoay theo góc.</summary>
        private static void veMui(mGraphics g, short id, float ax, float ay, float goc, float tiLe, float mo)
        {
            if (mo <= 0.01f)
            {
                return;
            }
            float lech = lechMui(id) * tiLe;
            double rad = goc / 57.29578;
            int x = (int) System.Math.Round(ax - System.Math.Cos(rad) * lech);
            int y = (int) System.Math.Round(ay - System.Math.Sin(rad) * lech);
            SmallImage.veIconXoay(g, id, x, y, tiLe, goc, mo);
        }

        /// <summary>
        /// Chạy khung tu..den-1 một lượt, gối đầu nhau (khung cũ mờ dần trong nửa sau, khung mới
        /// hiện trong nửa đầu — không tối đi, không giật). Khung cuối gối sang <paramref name="sau"/> nếu có.
        /// </summary>
        private static void veChuoi(mGraphics g, short[] k, int tu, int den, long t, long msKhung,
                float ax, float ay, float tiLe, float goc, float mo, short sau)
        {
            int n = den - tu;
            if (n <= 0 || mo <= 0.01f)
            {
                return;
            }
            if (t < 0)
            {
                t = 0;
            }
            int i = (int) System.Math.Min(n - 1, t / msKhung);
            float h = t >= msKhung * n && sau < 0 ? 0f : System.Math.Min(1f, (float) (t - i * msKhung) / msKhung);
            short a = k[tu + i];
            short b = i + 1 < n ? k[tu + i + 1] : sau;
            float moA = b >= 0 ? System.Math.Min(1f, 2f * (1f - h)) : 1f;
            float moB = System.Math.Min(1f, 2f * h);
            veMui(g, a, ax, ay, goc, tiLe, mo * moA);
            if (b >= 0 && h > 0.01f)
            {
                veMui(g, b, ax, ay, goc, tiLe, mo * moB);
            }
        }

        /// <summary>Lặp qua lại các khung tu..den-1 (gối đầu nhau).</summary>
        private static void veLap(mGraphics g, short[] k, int tu, int den, long tl, long msKhung,
                float ax, float ay, float tiLe, float goc, float mo)
        {
            int n = den - tu;
            if (n <= 0 || mo <= 0.01f)
            {
                return;
            }
            if (n == 1)
            {
                veMui(g, k[tu], ax, ay, goc, tiLe, mo);
                return;
            }
            int chuKy = 2 * (n - 1);
            int pos = (int) ((tl / msKhung) % chuKy);
            float h = (float) (tl % msKhung) / msKhung;
            int a = pos < n ? pos : chuKy - pos;
            int pb = (pos + 1) % chuKy;
            int b = pb < n ? pb : chuKy - pb;
            veMui(g, k[tu + a], ax, ay, goc, tiLe, mo * System.Math.Min(1f, 2f * (1f - h)));
            veMui(g, k[tu + b], ax, ay, goc, tiLe, mo * System.Math.Min(1f, 2f * h));
        }

        /// <summary>
        /// Vẽ quả trứng có skin (gọi từ Mob.paint). Trả false = đừng vẽ gì thêm
        /// (đang chết), true = vẽ tiếp thanh máu như thường.
        /// </summary>
        public static bool veVanKiem(mGraphics g, Mob m)
        {
            short[] cho = tach(m.tpVK, 0);
            short[] chet = tach(m.tpVK, 6);
            if (cho.Length < 12)
            {
                return true;
            }
            long ms = mSystem.currentTimeMillis();
            // Bay bay: nhap nho len xuong, lac nhe ngang, to nho nhe nhu dang tho.
            double phaY = (ms % VK_CHU_KY_Y) * 2.0 * System.Math.PI / VK_CHU_KY_Y;
            double phaX = (ms % VK_CHU_KY_X) * 2.0 * System.Math.PI / VK_CHU_KY_X;
            float x = m.x + (float) (System.Math.Sin(phaX) * VK_LAC_X);
            float y = m.y - VK_CAO + (float) (System.Math.Sin(phaY) * VK_LAC_Y);
            float tho = 1f + 0.04f * (float) System.Math.Sin(phaY + 1.2);
            float goc = gocQuay(ms, VK_VONG);
            if (m.tpVKChet > 0)
            {
                long t = ms - m.tpVKChet;
                if (chet.Length >= 2 && t < VK_THU * 2 + VK_MO_CHET)
                {
                    float mo = t < VK_THU * 2 ? 1f : 1f - (float) (t - VK_THU * 2) / VK_MO_CHET;
                    veChuoi(g, chet, 0, chet.Length, t, VK_THU, x, y, 1f, 0f, mo, -1);
                }
                return false;
            }
            // Moi don dam tung mot dot kiem rieng tu xoay (ve o lop toan cuc) — xoay sau lung chu luon hien.
            // Chum kiem: luc no (cham, to dan tu nho) va luc kiem bay ve (nhanh hon, tu khung 2).
            bool sauDanh = m.tpVKVe > 0;
            long msChum = sauDanh ? VK_CHUM_VE : VK_TU;
            int tu = sauDanh ? 1 : 0;
            long tChum = sauDanh ? ms - m.tpVKVe : ms - m.tpVKSinh;
            long tongChum = msChum * (6 - tu);
            if (tChum < tongChum)
            {
                float p = (float) tChum / tongChum;
                float em = p * p * (3f - 2f * p);
                float tiLe = tho * (0.55f + 0.45f * em);
                float mo = sauDanh ? 1f : System.Math.Min(1f, (float) tChum / VK_HIEN);
                // Kiem xoay nhanh hon luc dang chum, cham dan ve toc do xoay thuong.
                veChuoi(g, cho, tu, 6, tChum, msChum, x, y, tiLe, goc + (1f - em) * 140f, mo, cho[6]);
                return true;
            }
            // Xoay: 6 khung lap qua lai (7..12..7) — bat dau tu khung 7 ngay sau khi chum xong.
            veLap(g, cho, 6, 12, tChum - tongChum, VK_XOAY, x, y, tho, goc, 1f);
            return true;
        }

        private static float dichX(KiemBay k)
        {
            return k.dich.getX();
        }

        private static float dichY(KiemBay k)
        {
            return k.dich.getY() - k.dich.getH() / 2;
        }

        /// <summary>Kiếm rời chủ: bay tới → chụm vào địch → quấn → loé tan + bay về chủ.</summary>
        private static void veKiemBay(mGraphics g)
        {
            if (dsKiem.Count == 0)
            {
                return;
            }
            long ms = mSystem.currentTimeMillis();
            lock (dsKiem)
            {
                for (int i = dsKiem.Count - 1; i >= 0; i--)
                {
                    KiemBay k = dsKiem[i];
                    Mob m = k.nguon;
                    if (m == null || m.tpVK == null || m.tpVKChet > 0 || k.dich == null)
                    {
                        dsKiem.RemoveAt(i);
                        continue;
                    }
                    short[] bay = tach(m.tpVK, 1);
                    short[] chum = tach(m.tpVK, 2);
                    short[] quan = tach(m.tpVK, 3);
                    short[] ket = tach(m.tpVK, 4);
                    short[] ve = tach(m.tpVK, 5);
                    // Bo khung cu (may chu chua cap nhat: 3 doan) — thieu doan nao bo qua doan do,
                    // quan lay 6 khung xoay cua trung.
                    short[] choK = tach(m.tpVK, 0);
                    if (quan.Length == 0 && choK.Length >= 12)
                    {
                        quan = new short[6];
                        System.Array.Copy(choK, 6, quan, 0, 6);
                    }
                    if (chum.Length > 0 && chum.Length < 4)
                    {
                        // Doan 2 cua bo cu la 2 khung thu kiem, khong phai khung chum.
                        chum = new short[0];
                    }
                    if (bay.Length == 0 || quan.Length == 0)
                    {
                        dsKiem.RemoveAt(i);
                        continue;
                    }
                    float tx = dichX(k);
                    float ty = dichY(k);
                    long tBay = VK_BAY * bay.Length;
                    long tChum = VK_CHUM * chum.Length;
                    // Bo cu: dong kiem 10 khung bay cham hon mot chut cho du dai.
                    if (k.denLuc == 0)
                    {
                        long t = ms - k.batDau;
                        if (t < tBay)
                        {
                            // 1. Bay toi: mui luong kiem lao nhanh roi cham lai, cham dung tam dich.
                            float u = (float) t / tBay;
                            float e = 1f - (1f - u) * (1f - u);
                            float ax = k.x0 + (tx - k.x0) * e;
                            float ay = k.y0 + (ty - k.y0) * e;
                            float gocBay = (float) (System.Math.Atan2(ty - k.y0, tx - k.x0) * 57.29578);
                            veChuoi(g, bay, 0, bay.Length, t, VK_BAY, ax, ay, 1f, gocBay, 1f, chum.Length > 0 ? chum[0] : quan[0]);
                            continue;
                        }
                        k.denLuc = k.batDau + tBay;
                        k.gocDen = (float) (System.Math.Atan2(ty - k.y0, tx - k.x0) * 57.29578);
                        // Moi dot kiem rieng: khong nhap vao dot cu.
                        k.quanHet = k.denLuc + tChum + VK_QUAN;
                    }
                    long td = ms - k.denLuc;
                    if (td < tChum)
                    {
                        // 2. Chum vao dich: giu huong bay, khoi tron o dung tam dich.
                        veChuoi(g, chum, 0, chum.Length, td, VK_CHUM, tx, ty, 1f, k.gocDen, 1f, quan[0]);
                        continue;
                    }
                    // Xoay quan quay tiep tu huong luc den nen khong giat.
                    float gocQuan = k.gocDen + gocQuay(ms - k.denLuc - tChum, VK_VONG_QUAN);
                    if (ms < k.quanHet)
                    {
                        // 3. Quan quanh dich, hoi nhap nho.
                        long tq = ms - k.denLuc - tChum;
                        float nhap = (float) System.Math.Sin(tq * 2.0 * System.Math.PI / 700.0) * 2f;
                        veLap(g, quan, 0, quan.Length, tq, VK_QUAN_KHUNG, tx, ty + nhap, 1f, gocQuan, 1f);
                        continue;
                    }
                    long tk = ms - k.quanHet;
                    long tKet = ket.Length > 0 ? VK_KET * ket.Length : VK_KET * 4;
                    if (tk < tKet && ket.Length == 0)
                    {
                        // Khong co khung ket thuc: xoay quan mo dan.
                        veLap(g, quan, 0, quan.Length, tk + VK_QUAN, VK_QUAN_KHUNG, tx, ty, 1f, gocQuan, 1f - (float) tk / tKet);
                    }
                    else if (tk < tKet)
                    {
                        // 4. Ket thuc o dich: loe sang roi tan thanh bui.
                        float mo = tk > tKet - VK_KET ? 1f - (float) (tk - (tKet - VK_KET)) / VK_KET : 1f;
                        veChuoi(g, ket, 0, ket.Length, tk, VK_KET, tx, ty, 1f, gocQuan, mo, -1);
                    }
                    // Dot kiem loe tan roi mo het o dich (khong bay ve).
                    if (tk >= tKet)
                    {
                        dsKiem.RemoveAt(i);
                    }
                }
            }
        }

        // ------------------------------------------------------------------
        //  Âm thanh skin (Resources/res/tp/*.mp3): hẹn giờ ở bất kỳ luồng nào, phát ở
        //  luồng vẽ (Unity chỉ cho đụng AudioSource ở luồng chính).
        // ------------------------------------------------------------------
        private class AmCho
        {
            public string ten;
            public long luc;
            public long dinhLuc;
            public float am;

            /// <summary>Phát ngay sau khi tiếng này (tính từ <see cref="luc"/>) hết.</summary>
            public string sauTen;

            /// <summary>Tắt lúc này (0 = phát hết); tiếng ngắn hơn thì lặp cho đủ.</summary>
            public long hetLuc;

            /// <summary>Cắt bỏ tiếng nổ thứ hai trong tiếng (xem dinhAm).</summary>
            public bool catNoSau;

            /// <summary>Canh tiếng nổ đầu trúng <see cref="dinhLuc"/>, voice không mất đầu (phát nhanh hơn chút nếu cần).</summary>
            public bool khop;
        }

        /// <summary>Tiếng đang lặp chờ tắt đúng giờ (mờ dần 300 ms cuối).</summary>
        private class AmDangPhat
        {
            public UnityEngine.AudioSource src;
            public long hetLuc;
            public float am;

            /// <summary>Lúc trả tốc độ phát về bình thường (0 = không đổi).</summary>
            public long pitchVe;
        }

        /// <summary>Chỗ bắt đầu có tiếng trong file (giây) — bỏ khoảng lặng đầu.</summary>
        private static readonly Dictionary<string, float> amDau = new Dictionary<string, float>();

        /// <summary>Phát nhanh nhất bấy nhiêu lần (giọng gần như không đổi); còn dài thì cắt bớt đoạn đầu.</summary>
        private const float AM_NHANH_TOI_DA = 1.15f;

        private static readonly List<AmDangPhat> dsDangPhat = new List<AmDangPhat>();

        private static bool amDaNap;
        private static bool amDaPhanTich;

        /// <summary>Tiếng quạ theo sau tiếng Sharingan.</summary>
        private const float AM_QUA = 0.3f;

        private static readonly List<AmCho> dsAm = new List<AmCho>();
        private static readonly Dictionary<string, UnityEngine.AudioClip> amClip = new Dictionary<string, UnityEngine.AudioClip>();
        private static readonly Dictionary<string, float> amDinh = new Dictionary<string, float>();
        private static readonly List<UnityEngine.AudioSource> amNguon = new List<UnityEngine.AudioSource>();

        /// <summary>Âm lượng: Sharingan nhỏ vừa đủ nghe; Shinra Tensei to hơn chút.</summary>
        private const float AM_SHARINGAN = 0.39f;
        private const float AM_SHINRA = 0.45f;

        /// <summary>
        /// Hẹn phát tiếng <paramref name="ten"/>: <paramref name="dinhLuc"/> = 0 thì phát từ đầu lúc
        /// <paramref name="luc"/>; khác 0 thì canh để chỗ TO NHẤT của tiếng rơi đúng lúc đó
        /// (phần trước nó nghe trong lúc vận chiêu, phần sau là dư âm).
        /// </summary>
        private static void henAm(string ten, long luc, long dinhLuc, float am)
        {
            AmCho a = new AmCho();
            a.ten = ten;
            a.luc = luc;
            a.dinhLuc = dinhLuc;
            a.am = am;
            lock (dsAm)
            {
                dsAm.Add(a);
            }
        }

        /// <summary>Người vừa dùng Thôi miên có skin (charId → hết lúc): tắt tiếng đấm/chưởng gốc của chiêu.</summary>
        private static readonly Dictionary<int, long> chanDanh = new Dictionary<int, long>();

        /// <summary>
        /// Nhân vật này đang dùng chiêu có skin — bỏ mọi tiếng mặc định của chiêu gốc (gồng, chưởng,
        /// nổ…). Của chính mình thì xét luôn skin đang bật (client vẽ chiêu trước khi máy chủ trả lời).
        /// </summary>
        public static bool tatTiengGoc(Char c)
        {
            if (c == null)
            {
                return false;
            }
            if (chanGoc(c))
            {
                return true;
            }
            return c.me && c.myskill != null && c.myskill.template != null && skinDangBat(c.myskill.template.id);
        }

        /// <summary>Có nên bỏ tiếng đấm gốc của nhân vật này không (đang dùng Thôi miên có skin Tsukuyomi).</summary>
        public static bool chanTiengDanh(Char c)
        {
            if (c == null)
            {
                return false;
            }
            if (tatTiengGoc(c))
            {
                // Moi chieu dang dung skin: bo tieng dam/ret goc cua hoat anh.
                return true;
            }
            if (c.me && c.myskill != null && c.myskill.template != null && c.myskill.template.id == 22 && skinDangBat(22))
            {
                return true;
            }
            long het;
            lock (chanDanh)
            {
                return chanDanh.TryGetValue(c.charID, out het) && mSystem.currentTimeMillis() < het;
            }
        }

        /// <summary>Chính mình đang bật (và có) skin của chiêu này.</summary>
        private static bool skinDangBat(int tpl)
        {
            TrangPhucUI ui = getInstance();
            if (!ui.coDuLieu)
            {
                return false;
            }
            foreach (Chieu ch in ui.dsChieu)
            {
                if (ch.tpl == tpl)
                {
                    Mau m = timMau(ch, ch.dangDung);
                    return m != null && m.daCo;
                }
            }
            return false;
        }

        private static UnityEngine.AudioClip layClip(string ten)
        {
            UnityEngine.AudioClip c;
            if (!amClip.TryGetValue(ten, out c))
            {
                c = UnityEngine.Resources.Load("res/tp/" + ten, typeof(UnityEngine.AudioClip)) as UnityEngine.AudioClip;
                amClip[ten] = c;
            }
            return c;
        }

        /// <summary>Chỗ cắt của tiếng (giây, cắt trước tiếng nổ thứ hai); không có thì = độ dài.</summary>
        private static readonly Dictionary<string, float> amCat = new Dictionary<string, float>();

        /// <summary>
        /// Phân tích tiếng: năng lượng từng cửa sổ 50 ms; hai đỉnh to nhất cách nhau từ 0,4 giây là hai
        /// tiếng nổ. Trả về tiếng nổ ĐẦU (giây) để canh trúng lúc nổ, và ghi chỗ cắt ở quãng lặng nhất
        /// giữa hai tiếng nổ (bỏ tiếng nổ sau). Chỉ có một đỉnh thì lấy đỉnh đó, không cắt.
        /// </summary>
        private static float dinhAm(string ten, UnityEngine.AudioClip c)
        {
            float d;
            if (amDinh.TryGetValue(ten, out d))
            {
                return d;
            }
            d = c.length * 0.5f;
            float cat = c.length;
            try
            {
                if (c.loadState != UnityEngine.AudioDataLoadState.Loaded)
                {
                    c.LoadAudioData();
                    return d;
                }
                int kenh = System.Math.Max(1, c.channels);
                float[] a = new float[c.samples * kenh];
                if (c.GetData(a, 0))
                {
                    int cua = System.Math.Max(1, (int) (c.frequency * 0.05f)) * kenh;
                    int n = a.Length / cua;
                    double[] e = new double[n];
                    for (int k = 0; k < n; k++)
                    {
                        double s = 0;
                        for (int j = k * cua; j < (k + 1) * cua; j++)
                        {
                            s += a[j] * a[j];
                        }
                        e[k] = s;
                    }
                    // Lam min 3 cua so cho khoi bat nham gai nhon.
                    double[] m = new double[n];
                    for (int k = 0; k < n; k++)
                    {
                        m[k] = (e[System.Math.Max(0, k - 1)] + e[k] + e[System.Math.Min(n - 1, k + 1)]) / 3.0;
                    }
                    int k1 = 0;
                    for (int k = 1; k < n; k++)
                    {
                        if (m[k] > m[k1])
                        {
                            k1 = k;
                        }
                    }
                    int k2 = -1;
                    for (int k = 0; k < n; k++)
                    {
                        if (System.Math.Abs(k - k1) >= 8 && m[k] >= m[k1] * 0.35 && (k2 < 0 || m[k] > m[k2]))
                        {
                            k2 = k;
                        }
                    }
                    int dau = k2 >= 0 ? System.Math.Min(k1, k2) : k1;
                    // Dau tieng no dau: lui ve cho nang luong bat dau vot len (con > 30% dinh).
                    int batDauNo = dau;
                    while (batDauNo > 0 && m[batDauNo - 1] > m[dau] * 0.3)
                    {
                        batDauNo--;
                    }
                    d = (batDauNo * cua / kenh) / (float) c.frequency;
                    int coTieng = 0;
                    while (coTieng < dau && m[coTieng] < m[k1] * 0.03)
                    {
                        coTieng++;
                    }
                    amDau[ten] = (coTieng * cua / kenh) / (float) c.frequency;
                    if (k2 >= 0)
                    {
                        int sau = System.Math.Max(k1, k2);
                        int lang = dau + 2;
                        for (int k = dau + 2; k < sau; k++)
                        {
                            if (m[k] < m[lang])
                            {
                                lang = k;
                            }
                        }
                        cat = (lang * cua / kenh) / (float) c.frequency;
                    }
                }
            }
            catch (System.Exception)
            {
            }
            amDinh[ten] = d;
            amCat[ten] = cat;
            return d;
        }

        private static void xuLyAm()
        {
            long ms = mSystem.currentTimeMillis();
            if (!amDaNap)
            {
                // Nap truoc ba tieng skin (lan dau phat khoi khung, va kip phan tich cho cat tieng no).
                amDaNap = true;
                foreach (string ten in new string[] { "shinra", "sharingan", "qua" })
                {
                    UnityEngine.AudioClip c = layClip(ten);
                    if (c != null)
                    {
                        c.LoadAudioData();
                    }
                }
            }
            else if (!amDaPhanTich)
            {
                UnityEngine.AudioClip c = layClip("shinra");
                if (c == null || c.loadState == UnityEngine.AudioDataLoadState.Loaded)
                {
                    amDaPhanTich = true;
                    if (c != null)
                    {
                        dinhAm("shinra", c);
                    }
                }
            }
            for (int i = dsDangPhat.Count - 1; i >= 0; i--)
            {
                AmDangPhat d = dsDangPhat[i];
                if (d.src == null || !d.src.isPlaying)
                {
                    dsDangPhat.RemoveAt(i);
                    continue;
                }
                if (d.pitchVe > 0 && ms >= d.pitchVe)
                {
                    d.src.pitch = 1f;
                    d.pitchVe = 0;
                }
                long con = d.hetLuc - ms;
                if (con <= 0)
                {
                    d.src.Stop();
                    d.src.loop = false;
                    dsDangPhat.RemoveAt(i);
                    continue;
                }
                d.src.volume = con < 300L ? d.am * con / 300f : d.am;
            }
            if (dsAm.Count == 0)
            {
                return;
            }
            lock (dsAm)
            {
                for (int i = dsAm.Count - 1; i >= 0; i--)
                {
                    AmCho a = dsAm[i];
                    UnityEngine.AudioClip clip = layClip(a.ten);
                    if (clip == null || ms - System.Math.Max(a.luc, a.dinhLuc) > 15000L)
                    {
                        dsAm.RemoveAt(i);
                        continue;
                    }
                    if (a.khop)
                    {
                        if (xuLyKhop(a, clip, ms))
                        {
                            dsAm.RemoveAt(i);
                        }
                        continue;
                    }
                    long batDau = a.dinhLuc > 0 ? a.dinhLuc - (long) (dinhAm(a.ten, clip) * 1000f) : a.luc;
                    if (a.dinhLuc > 0 && batDau < a.luc)
                    {
                        // Tieng dai hon thoi gian gong: phat tu DAU ngay luc bat dau gong, khong cat mat doan dau.
                        batDau = a.luc;
                    }
                    if (a.sauTen != null)
                    {
                        UnityEngine.AudioClip truoc = layClip(a.sauTen);
                        batDau = a.luc + (truoc != null ? (long) (truoc.length * 1000f) : 0L);
                    }
                    if (a.hetLuc > 0 && ms >= a.hetLuc)
                    {
                        dsAm.RemoveAt(i);
                        continue;
                    }
                    if (ms < batDau)
                    {
                        continue;
                    }
                    dsAm.RemoveAt(i);
                    float lech = (ms - batDau) / 1000f;
                    if (!GameCanvas.isPlaySound || (a.hetLuc == 0 && lech >= clip.length - 0.05f))
                    {
                        continue;
                    }
                    UnityEngine.AudioSource src = phatAm(clip, a.hetLuc > 0 ? lech % clip.length : lech, a.am);
                    float cat;
                    if (a.catNoSau)
                    {
                        dinhAm(a.ten, clip);
                    }
                    if (src != null && (a.dinhLuc > 0 || a.catNoSau) && amCat.TryGetValue(a.ten, out cat) && cat < clip.length - 0.05f && cat > lech)
                    {
                        // Cat bo tieng no thu hai: mo dan roi tat o quang lang giua hai tieng no.
                        AmDangPhat c2 = new AmDangPhat();
                        c2.src = src;
                        c2.hetLuc = ms + (long) ((cat - lech) * 1000f) + 150L;
                        c2.am = a.am;
                        dsDangPhat.Add(c2);
                    }
                    if (src != null && a.hetLuc > 0)
                    {
                        src.loop = (a.hetLuc - ms) / 1000f > clip.length - src.time;
                        AmDangPhat d = new AmDangPhat();
                        d.src = src;
                        d.hetLuc = a.hetLuc;
                        d.am = a.am;
                        dsDangPhat.Add(d);
                    }
                }
            }
        }

        private static UnityEngine.AudioSource phatAm(UnityEngine.AudioClip clip, float lech, float am)
        {
            try
            {
                UnityEngine.AudioSource src = null;
                foreach (UnityEngine.AudioSource s in amNguon)
                {
                    if (s != null && !s.isPlaying)
                    {
                        src = s;
                        break;
                    }
                }
                if (src == null)
                {
                    if (amNguon.Count >= 4 && amNguon[0] != null)
                    {
                        src = amNguon[0];
                    }
                    else
                    {
                        UnityEngine.GameObject go = new UnityEngine.GameObject("TrangPhucAm");
                        UnityEngine.Object.DontDestroyOnLoad(go);
                        src = go.AddComponent<UnityEngine.AudioSource>();
                        src.playOnAwake = false;
                        amNguon.Add(src);
                    }
                }
                foreach (AmDangPhat d in dsDangPhat)
                {
                    if (d.src == src)
                    {
                        d.hetLuc = 0;
                    }
                }
                src.loop = false;
                src.pitch = 1f;
                src.clip = clip;
                src.volume = am;
                src.time = System.Math.Max(0f, System.Math.Min(lech, clip.length - 0.05f));
                src.Play();
                return src;
            }
            catch (System.Exception)
            {
            }
            return null;
        }

        /// <summary>
        /// Tự phát nổ bắt đầu gồng (gói -45/7, <paramref name="msGong"/> = số ms tới lúc nổ): có skin
        /// Thần La Thiên Chinh thì hẹn tiếng Shinra Tensei — chỗ to nhất rơi đúng lúc nổ. Trả true =
        /// đã thay tiếng gồng gốc.
        /// </summary>
        public static bool amTuNo(Char c, int msGong)
        {
            if (c == null || !(c.tpSkill == 14 && conHieuLuc(c)))
            {
                return false;
            }
            long bayGio = mSystem.currentTimeMillis();
            lock (shinraLuc)
            {
                long truoc;
                if (shinraLuc.TryGetValue(c.charID, out truoc) && bayGio - truoc < 3000L)
                {
                    return true;
                }
                shinraLuc[c.charID] = bayGio;
            }
            AmCho a = new AmCho();
            a.ten = "shinra";
            a.luc = bayGio;
            a.dinhLuc = bayGio + (msGong > 0 ? msGong : 3300);
            a.am = AM_SHINRA;
            a.catNoSau = true;
            a.khop = true;
            lock (dsAm)
            {
                dsAm.Add(a);
            }
            return true;
        }

        /// <summary>Lúc vừa hẹn voice Shinra của từng người — không hẹn trùng.</summary>
        private static readonly Dictionary<int, long> shinraLuc = new Dictionary<int, long>();

        /// <summary>
        /// Voice Shinra Tensei: bỏ khoảng lặng đầu; phần voice trước tiếng nổ đầu vừa khít thời gian
        /// gồng thì phát bình thường, dài hơn thì phát nhanh hơn chút (tối đa 1,3 lần) — tiếng nổ đầu
        /// rơi ĐÚNG lúc nổ gây sát thương, tới đó trả tốc độ thường; tiếng nổ thứ hai bị cắt.
        /// Trả true = đã xong (phát rồi hoặc bỏ).
        /// </summary>
        private static bool xuLyKhop(AmCho a, UnityEngine.AudioClip clip, long ms)
        {
            if (clip.loadState != UnityEngine.AudioDataLoadState.Loaded)
            {
                clip.LoadAudioData();
                return ms > a.dinhLuc;
            }
            float no = dinhAm(a.ten, clip);
            float dau;
            if (!amDau.TryGetValue(a.ten, out dau))
            {
                dau = 0f;
            }
            float gong = (a.dinhLuc - a.luc) / 1000f;
            float can = System.Math.Max(0.01f, no - dau);
            float pitch = 1f;
            float tu = dau;
            long batDau;
            if (can <= gong)
            {
                batDau = a.dinhLuc - (long) (can * 1000f);
            }
            else
            {
                pitch = System.Math.Min(AM_NHANH_TOI_DA, can / System.Math.Max(0.1f, gong));
                tu = System.Math.Max(dau, no - gong * pitch);
                batDau = a.luc;
            }
            if (ms < batDau)
            {
                return false;
            }
            float lech = tu + (ms - batDau) / 1000f * pitch;
            if (!GameCanvas.isPlaySound || lech >= clip.length - 0.05f)
            {
                return true;
            }
            UnityEngine.AudioSource src = phatAm(clip, lech, a.am);
            if (src == null)
            {
                return true;
            }
            src.pitch = pitch;
            float cat;
            if (!amCat.TryGetValue(a.ten, out cat))
            {
                cat = clip.length;
            }
            AmDangPhat d = new AmDangPhat();
            d.src = src;
            d.am = a.am;
            // Toi tieng no dau thi tra toc do thuong.
            d.pitchVe = lech < no ? ms + (long) ((no - lech) / pitch * 1000f) : 0;
            long toiNo = lech < no ? (long) ((no - lech) / pitch * 1000f) : 0;
            float tuNo = System.Math.Max(no, lech);
            d.hetLuc = cat < clip.length - 0.05f && cat > tuNo
                    ? ms + toiNo + (long) ((cat - tuNo) * 1000f) + 150L
                    : ms + 30000L;
            dsDangPhat.Add(d);
            return true;
        }

        // ------------------------------------------------------------------
        //  Thôi miên → Tsukuyomi: kẻ bị thôi miên thấy ảo cảnh (bầy quạ → mắt
        //  Sharingan) phủ kín màn hình, lặp tới khi tỉnh.
        // ------------------------------------------------------------------
        private static short[] tmKhung;
        private static long tmBatDau;
        private static long tmHet;

        /// <summary>Mỗi khung ảo cảnh (ms), hiện dần / tắt dần (ms).</summary>
        private const long TM_KHUNG = 90L;
        private const long TM_HIEN = 250L;
        private const long TM_TAT = 350L;

        /// <summary>Vẽ ảo cảnh phủ kín màn hình (gọi từ GameScr.paint, toạ độ màn hình).</summary>
        public static void veThoiMien(mGraphics g)
        {
            short[] k = tmKhung;
            if (k == null || k.Length == 0)
            {
                return;
            }
            long ms = mSystem.currentTimeMillis();
            if (ms >= tmHet + TM_TAT)
            {
                tmKhung = null;
                return;
            }
            float mo = System.Math.Min(1f, (float) (ms - tmBatDau) / TM_HIEN);
            if (ms > tmHet)
            {
                mo = System.Math.Min(mo, 1f - (float) (ms - tmHet) / TM_TAT);
            }
            if (mo <= 0.01f)
            {
                return;
            }
            int w = GameCanvas.w;
            int h = GameCanvas.h;
            if (mo >= 0.99f)
            {
                // Nen den lot duoi (anh chua tai kip van kin man hinh).
                g.setColor(0);
                g.fillRect(0, 0, w, h);
            }
            short id = k[(int) (((ms - tmBatDau) / TM_KHUNG) % k.Length)];
            var s = (SmallImage.imgNew != null && id >= 0 && id < SmallImage.imgNew.Length) ? SmallImage.imgNew[id] : null;
            if (s == null || s.img == null || mGraphics.getImageWidth(s.img) <= 1)
            {
                SmallImage.veIconXoay(g, id, w / 2, h / 2, 1f, 0f, mo);
                return;
            }
            float iw = mGraphics.getImageWidth(s.img);
            float ih = mGraphics.getImageHeight(s.img);
            // Phu kin: phong theo canh can nhieu hon, cat bot canh kia.
            float tiLe = System.Math.Max(w / iw, h / ih) * 1.02f;
            SmallImage.veIconXoay(g, id, w / 2, h / 2, tiLe, 0f, mo);
        }

        // ------------------------------------------------------------------
        //  Dịch chuyển tức thời → Phi Lôi Thần: kunai bay + chớp xanh
        // ------------------------------------------------------------------
        public class KunaiBay
        {
            public short[] khung;
            public int x0, y0, x1, y1;
            public long batDau;
        }

        private static readonly List<KunaiBay> dsKunai = new List<KunaiBay>();

        /// <summary>Kunai bay từ chỗ đi tới chỗ đến bao lâu (ms) — nhanh như dịch chuyển.</summary>
        private const long MS_KUNAI = 230L;

        /// <summary>Mũi kunai cách tâm ảnh (tâm chuôi) bấy nhiêu điểm theo hướng mũi (212 px ảnh gốc × 0,25).</summary>
        private const float MUI_KUNAI = 53f;

        /// <summary>Mỗi khung sét của kunai / mỗi khung chớp (ms).</summary>
        private const long MS_SET_KUNAI = 70L;
        private const long MS_CHOP = 55L;

        /// <summary>
        /// Gọi từ setSkillPaint của Dịch chuyển tức thời (128–134), TRƯỚC khi nhân vật
        /// nhảy tới mục tiêu: lúc này chỗ đứng còn là chỗ cũ, mục tiêu đang chọn là
        /// kẻ địch. Của chính mình thì client tự nhảy ngay, gói 123 tới sau — nên
        /// phải phóng kunai ở đây mới đúng chỗ cũ. Trả true = đã phóng, bỏ chớp gốc.
        /// </summary>
        public static bool batDauDichChuyen(Char c)
        {
            if (c == null)
            {
                return false;
            }
            long bayGio = mSystem.currentTimeMillis();
            c.tpDiX = c.cx;
            c.tpDiY = c.cy - c.ch / 2;
            c.tpDiLuc = bayGio;
            if (vuaPhatKunai(c))
            {
                return true;
            }
            if (c.me && !(c.tpSkill == 20 && conHieuLuc(c) && bayGio - c.tpLuc <= 1500L))
            {
                ganTruoc(c, 20);
            }
            if (c.tpSkill != 20 || !conHieuLuc(c) || bayGio - c.tpLuc > 1500L)
            {
                return false;
            }
            IMapObject mt = c.mobFocus != null ? (IMapObject) c.mobFocus : c.charFocus;
            if (mt == null)
            {
                // Chua biet ke dich: de goi 123 phong (co diem den that).
                return false;
            }
            phatKunai(c, c.tpDiX, c.tpDiY, mt.getX(), mt.getY() - mt.getH() / 2);
            return true;
        }

        /// <summary>Vừa phóng kunai Phi Lôi Thần (dưới 1,5 giây) — bỏ chớp dịch chuyển gốc.</summary>
        public static bool vuaPhatKunai(Char c)
        {
            return c != null && c.tpKunaiLuc > 0 && mSystem.currentTimeMillis() - c.tpKunaiLuc < 1500L;
        }

        /// <summary>
        /// Gọi từ gói đặt vị trí (123). Kunai đã phóng lúc bắt đầu chiêu thì chỉ bỏ
        /// chớp gốc; chưa phóng (gói trang phục tới sau hoạt ảnh) thì phóng từ chỗ cũ
        /// đã ghi tới tâm kẻ địch. Gói 123 còn dùng cho dịch chuyển khác, nên chỉ
        /// nhận khi gói trang phục của chiêu này vừa tới (còn hiệu lực).
        /// </summary>
        public static bool dichChuyen(Char c, int x, int y)
        {
            if (vuaPhatKunai(c))
            {
                return true;
            }
            if (c == null || c.tpSkill != 20 || !conHieuLuc(c)
                    || mSystem.currentTimeMillis() - c.tpLuc > 1500L)
            {
                return false;
            }
            long bayGio = mSystem.currentTimeMillis();
            // Cho cu: da ghi luc bat dau chieu (neu vua ghi), khong thi van la cho dang dung.
            bool coCu = c.tpDiLuc > 0 && bayGio - c.tpDiLuc < 1500L;
            int x0 = coCu ? c.tpDiX : c.cx;
            int y0 = coCu ? c.tpDiY : c.cy - c.ch / 2;
            // Diem den = TAM ke dich (muc tieu dang chon); khong biet thi lay diem dat chan.
            IMapObject mt = c.mobFocus != null ? (IMapObject) c.mobFocus : c.charFocus;
            int x1 = mt != null ? mt.getX() : x;
            int y1 = mt != null ? mt.getY() - mt.getH() / 2 : y - c.ch / 2;
            phatKunai(c, x0, y0, x1, y1);
            return true;
        }

        private static void phatKunai(Char c, int x0, int y0, int x1, int y1)
        {
            short[] kunai = tach(c.tpBay, 0);
            short[] chop = tach(c.tpBay, 1);
            long bayGio = mSystem.currentTimeMillis();
            if (chop.Length > 0)
            {
                themNo(chop, x0, y0, MS_CHOP, bayGio, 1f);
                themNo(chop, x1, y1, MS_CHOP, bayGio + MS_KUNAI, 1f);
            }
            if (kunai.Length > 0)
            {
                KunaiBay k = new KunaiBay();
                k.khung = kunai;
                k.x0 = x0;
                k.y0 = y0;
                k.x1 = x1;
                k.y1 = y1;
                k.batDau = bayGio;
                lock (dsKunai)
                {
                    dsKunai.Add(k);
                }
            }
            c.tpKunaiLuc = bayGio;
            ketThuc(c);
        }

        /// <summary>Kunai bay: mũi xoay đúng hướng bay, sét hoà dần giữa các khung.</summary>
        private static void veKunaiBay(mGraphics g)
        {
            if (dsKunai.Count == 0)
            {
                return;
            }
            long ms = mSystem.currentTimeMillis();
            lock (dsKunai)
            {
                for (int i = dsKunai.Count - 1; i >= 0; i--)
                {
                    KunaiBay k = dsKunai[i];
                    float p = (float) (ms - k.batDau) / MS_KUNAI;
                    if (p >= 1f)
                    {
                        dsKunai.RemoveAt(i);
                        continue;
                    }
                    // MUI kunai truot dung tren duong thang cho cu -> dich, cham dung tam
                    // dich; anh can tam o CHUOI nen lui anh lai MUI_KUNAI theo huong bay.
                    double rad = System.Math.Atan2(k.y1 - k.y0, k.x1 - k.x0);
                    float mx = k.x0 + (k.x1 - k.x0) * p;
                    float my = k.y0 + (k.y1 - k.y0) * p;
                    int x = (int) (mx - System.Math.Cos(rad) * MUI_KUNAI);
                    int y = (int) (my - System.Math.Sin(rad) * MUI_KUNAI);
                    // Mui kunai trong anh huong PHAI (goc 0): xoay dung goc duong bay.
                    float goc = (float) (rad * 57.29578);
                    long t = ms - k.batDau;
                    int n = k.khung.Length;
                    int a = (int) ((t / MS_SET_KUNAI) % n);
                    int b = (a + 1) % n;
                    float h = (float) (t % MS_SET_KUNAI) / MS_SET_KUNAI;
                    SmallImage.veIconXoay(g, k.khung[a], x, y, 1f, goc);
                    if (b != a && h > 0.02f)
                    {
                        SmallImage.veIconXoay(g, k.khung[b], x, y, 1f, goc, h);
                    }
                }
            }
        }

        public class XoayMo
        {
            public short[] khung;
            public int x, y;
            public long batDau;
        }

        private static readonly List<XoayMo> dsXoay = new List<XoayMo>();

        /// <summary>Trúng địch: chuỗi nổ lớn, rồi xoáy dư âm to nhất 3 giây mờ dần; kết thúc lần nạp.</summary>
        private static void noRasen(Char chu, int x, int y)
        {
            ketThuc(chu);
            noRasenTai(chu.tpBay, x, y);
        }

        /// <summary>Chuỗi nổ (quay) rồi xoáy dư âm tại (x, y), theo dãy khung bay <paramref name="bayDu"/>.</summary>
        private static void noRasenTai(short[] bayDu, int x, int y)
        {
            long bayGio = mSystem.currentTimeMillis();
            short[] no = tach(bayDu, 1);
            if (no.Length > 0)
            {
                themNo(no, x, y, MS_NO, bayGio, RASEN_NO);
                lock (dsNo)
                {
                    dsNo[dsNo.Count - 1].quay = 1.5f;
                }
            }
            short[] xoay = tach(bayDu, 2);
            if (xoay.Length > 0)
            {
                XoayMo m = new XoayMo();
                m.khung = xoay;
                m.x = x;
                m.y = y;
                m.batDau = bayGio + no.Length * MS_NO;
                lock (dsXoay)
                {
                    dsXoay.Add(m);
                }
            }
        }

        private static void veXoayMo(mGraphics g)
        {
            if (dsXoay.Count == 0)
            {
                return;
            }
            long bayGio = mSystem.currentTimeMillis();
            lock (dsXoay)
            {
                for (int i = dsXoay.Count - 1; i >= 0; i--)
                {
                    XoayMo m = dsXoay[i];
                    long t = bayGio - m.batDau;
                    if (t >= MS_XOAY)
                    {
                        dsXoay.RemoveAt(i);
                        continue;
                    }
                    if (t < 0)
                    {
                        continue;
                    }
                    float mo = t < MS_XOAY_RO ? 1f : (float) (MS_XOAY - t) / (MS_XOAY - MS_XOAY_RO);
                    // Mot khung quay deu (khong doi khung = khong giat) + mot lop mo quay
                    // nguoc chieu cho xoay co chieu sau.
                    SmallImage.veIconXoay(g, m.khung[0], m.x, m.y, RASEN_XOAY, gocQuay(t, 1f), mo);
                    if (m.khung.Length > 1)
                    {
                        SmallImage.veIconXoay(g, m.khung[1], m.x, m.y, RASEN_XOAY * 0.9f, -gocQuay(t, 0.6f), mo * 0.45f);
                    }
                }
            }
        }

        /// <summary>Khung Tự phát nổ căn đáy: tâm ảnh cao hơn chân chừng này điểm.</summary>
        /// <remarks>Khung căn theo ĐÁY KHỐI KHÓI CHÍNH (không theo mảnh vụn thấp nhất) nên mọi khung chạm đất như nhau.</remarks>
        private const int TU_NO_TAM = 35;

        /// <summary>Phóng mọi khung Tự phát nổ (người dùng muốn to hơn 50%).</summary>
        private const float TU_NO_TO = 1.5f;

        /// <summary>Tâm lớp mặt đất sau khi phóng: căn đáy nên tâm nâng theo cỡ, đáy vẫn ở mặt đất.</summary>
        /// <summary>Cỡ riêng lớp khói mặt đất: to hơn lớp giữa thân 30%.</summary>
        private const float DAT_TO = TU_NO_TO * 1.3f;

        private static readonly int TAM_TO = (int) (TU_NO_TAM * DAT_TO) - 10;

        /// <summary>Lớp giữa thân thấp hơn giữa người chừng này điểm.</summary>
        private const int GIUA_THAP = 18;

        /// <summary>
        /// Vẽ khung <paramref name="a"/> đục hoàn toàn rồi phủ <paramref name="b"/> lên với
        /// độ đục <paramref name="t"/> — chuyển khung liên tục, không nhảy cóc, mà cũng
        /// không nhạt đi giữa chừng như khi mờ cả hai.
        /// </summary>
        private static void veHoa(mGraphics g, short a, short b, float t, int x, int y, float tiLe)
        {
            SmallImage.veIconXoay(g, a, x, y, tiLe, 0f);
            if (b != a && t > 0.02f)
            {
                SmallImage.veIconXoay(g, b, x, y, tiLe, 0f, t);
            }
        }

        /// <summary>Nhịp khung: gồng đảo qua lại, nổ mặt đất, đá bung giữa thân (ms).</summary>
        private const long MS_GONG = 110L;
        private const long MS_NO = 90L;
        private const long MS_BUNG = 65L;

        public class NoLon
        {
            public short[] khung;
            public int x, tamY;
            public long ms;
            public long batDau;
            public float tiLe = TU_NO_TO;
            /// <summary>Vòng/giây quay quanh tâm (0 = không quay).</summary>
            public float quay;
        }

        private static readonly List<NoLon> dsNo = new List<NoLon>();

        /// <summary>Thời gian gồng (ms): của mình / người khác đều do gói gồng gửi về.</summary>
        private static long thoiGianGong(Char c)
        {
            return (c.seconds > 0 && c.seconds < 50000) ? c.seconds : 3300L;
        }

        /// <summary>
        /// Đang gồng. Lớp mặt đất: khói theo tiến độ gồng, đảo qua lại giữa khung
        /// hiện tại và khung liền trước để lúc nào cũng có chuyển động (hết cảnh
        /// đứng hình rồi nhảy). Lớp giữa thân: đá dồn về (10–11–12–11 lặp), lớn dần.
        /// </summary>
        private static void veGongTuNo(mGraphics g, Char c)
        {
            if (!laTuNo(c) || !c.isStandAndCharge)
            {
                return;
            }
            long troi = mSystem.currentTimeMillis() - c.tpLuc;
            float p = System.Math.Min(1f, (float) troi / System.Math.Max(1L, thoiGianGong(c)));
            short[] dat = tach(c.tpNap, 0);
            if (dat.Length > 0)
            {
                // Tien do lien tuc 0..n-1: hoa dan tu khung nay sang khung ke.
                float f = p * (dat.Length - 1);
                int a = System.Math.Min(dat.Length - 1, (int) f);
                int b = System.Math.Min(dat.Length - 1, a + 1);
                veHoa(g, dat[a], dat[b], f - a, c.cx, c.cy - TAM_TO, DAT_TO);
            }
            short[] giua = tach(c.tpNap, 1);
            if (giua.Length > 0)
            {
                // Vong qua lai 0-1-2-1-0...
                int chu = System.Math.Max(1, giua.Length * 2 - 2);
                float pha = (float) (troi % (chu * MS_GONG * 2)) / (MS_GONG * 2);
                int ja = (int) pha % chu;
                int jb = (ja + 1) % chu;
                int a = ja >= giua.Length ? chu - ja : ja;
                int b = jb >= giua.Length ? chu - jb : jb;
                veHoa(g, giua[a], giua[b], pha - (int) pha, c.cx, c.cy - c.ch / 2 + GIUA_THAP, (0.55f + 0.45f * p) * TU_NO_TO);
            }
        }

        /// <summary>
        /// Gồng xong (gọi đầu stopUseChargeSkill): nổ tại chỗ đứng. Mặt đất: các
        /// khung nổ mỗi khung lặp hai lần theo cặp (6,7,6,7,8,9,8,9) cho dày, khung
        /// cuối thành hố nứt dư âm. Giữa thân: đá bung 1→9 nhanh. Bị ngắt sớm thì thôi.
        /// </summary>
        private static void noTuSat(Char c)
        {
            if (!c.isStandAndCharge || c.tpBay == null || c.tpBay.Length == 0)
            {
                return;
            }
            long bayGio = mSystem.currentTimeMillis();
            if (bayGio - c.tpLuc < thoiGianGong(c) - 400L)
            {
                return;
            }
            short[] dat = tach(c.tpBay, 0);
            int soNo = dat.Length >= 2 ? dat.Length - 1 : dat.Length;
            List<short> day = new List<short>();
            for (int i = 0; i < soNo; i += 2)
            {
                for (int lap = 0; lap < 2; lap++)
                {
                    day.Add(dat[i]);
                    if (i + 1 < soNo)
                    {
                        day.Add(dat[i + 1]);
                    }
                }
            }
            if (day.Count > 0)
            {
                themNo(day.ToArray(), c.cx, c.cy - TAM_TO, MS_NO, bayGio, DAT_TO);
            }
            short[] bung = tach(c.tpBay, 1);
            if (bung.Length > 0)
            {
                themNo(bung, c.cx, c.cy - c.ch / 2 + GIUA_THAP, MS_BUNG, bayGio);
            }
            if (dat.Length >= 2)
            {
                DuAm d = new DuAm();
                d.icon = dat[dat.Length - 1];
                d.x = c.cx;
                d.dat = c.cy;
                d.dy = -TAM_TO;
                d.tiLe = DAT_TO;
                d.batDau = bayGio + day.Count * MS_NO;
                lock (dsDuAm)
                {
                    dsDuAm.Add(d);
                }
            }
            // Giu tat hieu ung no goc them 1,5 giay (tu the no ve ngay sau day).
            c.tpHetLuc = bayGio + 1500L;
            c.tpChanDen = bayGio + 2500L;
        }

        private static void themNo(short[] khung, int x, int tamY, long ms, long batDau)
        {
            themNo(khung, x, tamY, ms, batDau, TU_NO_TO);
        }

        private static void themNo(short[] khung, int x, int tamY, long ms, long batDau, float tiLe)
        {
            NoLon no = new NoLon();
            no.khung = khung;
            no.x = x;
            no.tamY = tamY;
            no.ms = ms;
            no.tiLe = tiLe;
            no.batDau = batDau;
            lock (dsNo)
            {
                dsNo.Add(no);
            }
        }

        /// <summary>Vẽ các vụ nổ Tự phát nổ đang chạy (lớp trên nhân vật).</summary>
        private static void veNoTuNo(mGraphics g)
        {
            if (dsNo.Count == 0)
            {
                return;
            }
            long bayGio = mSystem.currentTimeMillis();
            lock (dsNo)
            {
                for (int i = dsNo.Count - 1; i >= 0; i--)
                {
                    NoLon no = dsNo[i];
                    float f = (float) (bayGio - no.batDau) / no.ms;
                    if (f < 0f)
                    {
                        continue;
                    }
                    int k = (int) f;
                    if (k >= no.khung.Length)
                    {
                        dsNo.RemoveAt(i);
                        continue;
                    }
                    int k2 = System.Math.Min(no.khung.Length - 1, k + 1);
                    float gq = no.quay == 0f ? 0f : gocQuay(bayGio - no.batDau, no.quay);
                    SmallImage.veIconXoay(g, no.khung[k], no.x, no.tamY, no.tiLe, gq);
                    if (no.khung[k2] != no.khung[k] && f - k > 0.02f)
                    {
                        SmallImage.veIconXoay(g, no.khung[k2], no.x, no.tamY, no.tiLe, gq, f - k);
                    }
                }
            }
        }

        private static void ghiNo(Char chu, short[] bay, int x, int y, int dat)
        {
            ketThuc(chu);
            if (bay.Length < 2)
            {
                return;
            }
            chu.tpTrung = bay[1];
            chu.tpTrungX = x;
            chu.tpTrungY = y;
            chu.tpTrungLuc = mSystem.currentTimeMillis();
            if (bay.Length >= 3)
            {
                // Du am: dat o CHAN muc tieu (mat dat), hien sau khi vu no ket thuc.
                DuAm d = new DuAm();
                d.icon = bay[2];
                d.x = x;
                d.dat = dat;
                d.batDau = chu.tpTrungLuc + MS_TRUNG;
                lock (dsDuAm)
                {
                    dsDuAm.Add(d);
                }
            }
        }
    }
}
