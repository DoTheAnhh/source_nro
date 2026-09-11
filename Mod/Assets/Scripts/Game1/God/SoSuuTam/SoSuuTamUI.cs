// Khong "using System": lop Math cua engine trung ten voi System.Math, de ca hai
// thi trinh bien dich khong biet chon cai nao.
using System.Collections.Generic;

namespace Game1.God
{
    /// <summary>
    /// Sổ sưu tầm — màn vẽ mới, thay khung ảnh cũ của <c>RadarScr</c>.
    /// </summary>
    /// <remarks>
    /// <para>Lưới thẻ bên trái (viền theo độ hiếm, ổ khoá cho thẻ chưa có, vệt
    /// sáng lướt qua thẻ đang chọn, viền xanh nhịp thở cho thẻ đang bật), khung
    /// chi tiết bên phải: hình quái / nhân vật động trong vầng sáng, sao cấp
    /// độ, thanh tiến độ chạy mượt, chỉ số chia theo cấp, nút bật thẻ.</para>
    ///
    /// <para>Vẽ đè lên màn chơi và tự bắt chạm như các màn phụ khác của
    /// <c>ClientManager</c>. Dữ liệu đến qua gói 127 (máy chủ:
    /// <c>RadarService</c>); mở sổ là xin lại cả sổ mới nhất.</para>
    ///
    /// <para>Bố cục tính một lần mỗi khung trong <see cref="tinhBoCuc"/>; vẽ và
    /// bắt chạm cùng đọc, không ai tự tính lại.</para>
    /// </remarks>
    public class SoSuuTamUI
    {
        private static SoSuuTamUI instance;

        public static SoSuuTamUI getInstance()
        {
            return instance ?? (instance = new SoSuuTamUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Dữ liệu
        // ------------------------------------------------------------------
        public class The
        {
            public int id;
            public int icon;
            public int hang;
            public int daGop;
            public int canLen;
            public int kieu;
            public int quai = -1;
            public int head = -1;
            public int body = -1;
            public int leg = -1;
            public int bag = -1;
            public string ten = "";
            public string moTa = "";
            /// <summary>0 chưa có · -1 đã mở khoá · 1.. là Lv.</summary>
            public int cap;
            public bool dangBat;
            public int capToiDa = 1;
            public int aura = -1;
            public int auraTuCap;
            public ItemOption[] chiSo = new ItemOption[0];
            /// <summary>Dùng lại phần vẽ quái / nhân vật động của sổ cũ.</summary>
            public Info_RadaScr xem;

            public bool daCo
            {
                get { return cap != 0; }
            }

            public int bac
            {
                get { return cap < 0 ? 0 : cap; }
            }

            public bool toiDa
            {
                get { return daCo && bac >= capToiDa; }
            }
        }

        private readonly List<The> ds = new List<The>();
        private readonly List<The> dsLoc = new List<The>();
        private int idChon = -1;
        /// <summary>0 tất cả · 1 đã có · 2 chưa có · 3 đang bật.</summary>
        private int boLoc;
        private bool dangTai;
        /// <summary>Gói sổ không đọc được theo khuôn nào — máy chủ lệch phiên bản.</summary>
        private bool loiDuLieu;
        private long lucMo;
        /// <summary>Tăng mỗi lần dữ liệu hay lựa chọn đổi — dựng lại phần chữ.</summary>
        private int phienBan;

        // ------------------------------------------------------------------
        //  Màu
        // ------------------------------------------------------------------
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        private static readonly int NEN_TREN = rgb(0x26, 0x1C, 0x4E);
        private static readonly int NEN_DUOI = rgb(0x0C, 0x0A, 0x1E);
        private static readonly int VANG = rgb(0xFF, 0xCB, 0x52);
        private static readonly int VANG_TOI = rgb(0xA8, 0x6F, 0x16);
        private static readonly int DAI_TREN = rgb(0x55, 0x36, 0x9E);
        private static readonly int DAI_DUOI = rgb(0x28, 0x19, 0x55);
        private static readonly int O_TREN = rgb(0x32, 0x2D, 0x5C);
        private static readonly int O_DUOI = rgb(0x15, 0x13, 0x31);
        private static readonly int XAM_TOI = rgb(0x44, 0x42, 0x5E);
        private static readonly int BAC = rgb(0xC9, 0xCE, 0xDA);
        private static readonly int XANH_BAT = rgb(0x4E, 0xE6, 0x80);
        private static readonly int XANH_BAT_TOI = rgb(0x1B, 0x86, 0x44);
        private static readonly int DO_TREN = rgb(0xF2, 0x5A, 0x4E);
        private static readonly int DO_DUOI = rgb(0x9C, 0x1E, 0x1E);
        private const int TRANG = 0xFFFFFF;

        /// <summary>Màu theo hạng: Thường, Tốt, Hiếm, Sử thi, Huyền thoại, Thần thoại, Tối thượng.</summary>
        private static readonly int[] MAU_HANG = {
            rgb(0xB8, 0xC0, 0xCC), rgb(0x58, 0xD4, 0x70), rgb(0x4A, 0x9E, 0xFF),
            rgb(0xB4, 0x6E, 0xFF), rgb(0xFF, 0xA4, 0x36), rgb(0xFF, 0x55, 0x55),
            rgb(0xFF, 0xDA, 0x4C)
        };

        private static readonly string[] TEN_HANG = {
            "Thường", "Tốt", "Hiếm", "Sử thi", "Huyền thoại", "Thần thoại", "Tối thượng"
        };

        private static int kep(int v, int lo, int hi)
        {
            return v < lo ? lo : (v > hi ? hi : v);
        }

        private static int mauHang(int h)
        {
            return MAU_HANG[kep(h, 0, MAU_HANG.Length - 1)];
        }

        private static string tenHang(int h)
        {
            return TEN_HANG[kep(h, 0, TEN_HANG.Length - 1)];
        }

        /// <summary>Trộn hai màu: t = 0 là a, t = 1 là b.</summary>
        private static int tron(int a, int b, float t)
        {
            int ra = (a >> 16) & 255, ga = (a >> 8) & 255, ba = a & 255;
            int rb = (b >> 16) & 255, gb = (b >> 8) & 255, bb = b & 255;
            return ((int)(ra + (rb - ra) * t) << 16) | ((int)(ga + (gb - ga) * t) << 8)
                    | (int)(ba + (bb - ba) * t);
        }

        private static float song(float tocDo, int lech)
        {
            return UnityEngine.Mathf.Sin((GameCanvas.gameTick + lech) * tocDo);
        }

        // ------------------------------------------------------------------
        //  Bố cục
        // ------------------------------------------------------------------
        private const int CAO_DAU = 30;
        private const int CAO_LOC = 18;
        private const int KHE = 6;
        private const int LE = 8;

        private int x0, y0, rong, cao;
        private int xLuoi, yLuoi, rongLuoi, caoLuoi, xLuoiDau;
        private int oCo = 42;
        private int soCot = 1, soHangHien = 1;
        private int cuonHang;
        private int xCT, yCT, rongCT, caoCT;
        private int yTen, ySao, yTienDo, yChu, caoChu;
        private int cuonChu;
        private int caoNoiDung;
        private readonly int[] oXem = new int[4];
        private readonly int[] nutHanhDong = new int[4];
        private readonly int[] nutDong = new int[4];
        private readonly int[][] oLoc = { new int[4], new int[4], new int[4], new int[4] };

        private float hoatAnhMo()
        {
            float t = (mSystem.currentTimeMillis() - lucMo) / 240f;
            if (t >= 1f)
            {
                return 1f;
            }
            if (t <= 0f)
            {
                return 0f;
            }
            float u = 1f - t;
            return 1f - u * u * u;
        }

        private void tinhBoCuc()
        {
            rong = Math.min(GameCanvas.w - 12, 620);
            if (rong < 300)
            {
                rong = Math.min(GameCanvas.w, 300);
            }
            cao = Math.min(GameCanvas.h - 12, 350);
            if (cao < 200)
            {
                cao = Math.min(GameCanvas.h, 200);
            }
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2 + (int)((1f - hoatAnhMo()) * 18f);

            nutDong[0] = x0 + rong - 28;
            nutDong[1] = y0 + 6;
            nutDong[2] = 22;
            nutDong[3] = 18;

            rongCT = Math.max(172, rong * 41 / 100);
            xCT = x0 + rong - LE - rongCT;
            yCT = y0 + CAO_DAU + 6;
            caoCT = y0 + cao - LE - yCT;

            xLuoi = x0 + LE;
            int yLoc = yCT;
            rongLuoi = xCT - 6 - xLuoi;
            int wLoc = (rongLuoi - 3 * 4) / 4;
            for (int i = 0; i < 4; i++)
            {
                oLoc[i][0] = xLuoi + i * (wLoc + 4);
                oLoc[i][1] = yLoc;
                oLoc[i][2] = wLoc;
                oLoc[i][3] = CAO_LOC;
            }
            yLuoi = yLoc + CAO_LOC + 5;
            caoLuoi = y0 + cao - LE - yLuoi;

            oCo = rongLuoi >= 300 ? 44 : 38;
            soCot = Math.max(1, (rongLuoi - 8 + KHE) / (oCo + KHE));
            soHangHien = Math.max(1, (caoLuoi - 8 + KHE) / (oCo + KHE));
            int rongLuoiThat = soCot * (oCo + KHE) - KHE;
            xLuoiDau = xLuoi + (rongLuoi - rongLuoiThat) / 2;

            int yy = yCT + 6;
            int caoXem = Math.max(44, Math.min(84, caoCT * 32 / 100));
            oXem[0] = xCT + 6;
            oXem[1] = yy;
            oXem[2] = rongCT - 12;
            oXem[3] = caoXem;
            yy += caoXem + 5;
            yTen = yy;
            yy += 13;
            ySao = yy;
            yy += 11;
            yTienDo = yy;
            yy += 15;
            yChu = yy;
            nutHanhDong[0] = xCT + 8;
            nutHanhDong[2] = rongCT - 16;
            nutHanhDong[3] = 20;
            nutHanhDong[1] = yCT + caoCT - nutHanhDong[3] - 6;
            caoChu = Math.max(12, nutHanhDong[1] - 5 - yChu);
        }

        // ------------------------------------------------------------------
        //  Mở / đóng / nhận gói
        // ------------------------------------------------------------------
        public void mo()
        {
            dangMo = true;
            lucMo = mSystem.currentTimeMillis();
            dangTai = ds.Count == 0;
            cuonChu = 0;
            phienBan++;
            Service.gI().SendRada(0, -1);
            try
            {
                SoundMn.gI().radarItem();
            }
            catch (System.Exception)
            {
            }
        }

        public void dong()
        {
            dangMo = false;
        }

        private The tim(int id)
        {
            for (int i = 0; i < ds.Count; i++)
            {
                if (ds[i].id == id)
                {
                    return ds[i];
                }
            }
            return null;
        }

        private The theDangChon()
        {
            return tim(idChon);
        }

        /// <summary>Gói 127 — xem ghi chú đầu <c>RadarService</c> phía máy chủ.</summary>
        public void nhanGoi(Message msg)
        {
            try
            {
                sbyte loai = msg.reader().readByte();
                switch (loai)
                {
                    case 0:
                        docCaSo(msg);
                        break;
                    case 1:
                        {
                            int id = msg.reader().readShort();
                            bool bat = msg.reader().readByte() == 1;
                            for (int i = 0; i < ds.Count; i++)
                            {
                                if (ds[i].id == id)
                                {
                                    ds[i].dangBat = bat;
                                }
                                else if (bat)
                                {
                                    ds[i].dangBat = false;
                                }
                            }
                            phienBan++;
                            break;
                        }
                    case 2:
                        {
                            int id = msg.reader().readShort();
                            int cap = msg.reader().readByte();
                            The t = tim(id);
                            if (t != null)
                            {
                                t.cap = cap;
                                phienBan++;
                            }
                            break;
                        }
                    case 3:
                        {
                            myReader r = msg.reader();
                            int id = r.readShort();
                            int gop;
                            int can;
                            // Khuon cu (may chu chua cap nhat) gui hai byte.
                            if (r.buffer.Length - r.posRead <= 2)
                            {
                                gop = r.readUnsignedByte();
                                can = r.readUnsignedByte();
                            }
                            else
                            {
                                gop = r.readInt();
                                can = r.readInt();
                            }
                            The t = tim(id);
                            if (t != null)
                            {
                                t.daGop = gop;
                                t.canLen = can;
                            }
                            break;
                        }
                    case 4:
                        {
                            int idChar = msg.reader().readInt();
                            short aura = msg.reader().readShort();
                            msg.reader().readByte();
                            Char c = (Char.myCharz().charID == idChar)
                                    ? Char.myCharz() : GameScr.findCharInMap(idChar);
                            if (c != null)
                            {
                                c.idAuraEff = aura;
                            }
                            break;
                        }
                }
            }
            catch (System.Exception ex)
            {
                UnityEngine.Debug.LogError("[SoSuuTam] " + ex);
            }
        }

        /// <summary>Đọc cả sổ — thử khuôn mới, lệch thì đọc lại theo khuôn cũ.</summary>
        /// <remarks>
        /// Client và máy chủ không phải lúc nào cũng cập nhật cùng lúc. Máy chủ
        /// cũ gửi số lượng một byte và thiếu cấp tối đa / hào quang; đọc khuôn
        /// mới trên gói ấy là đọc quá cuối gói ("loi doc sbyte eof"). Nên đọc
        /// thử khuôn mới — ném lỗi hoặc còn thừa byte là sai khuôn — rồi quay về
        /// đầu gói đọc khuôn cũ. Cả hai đều hỏng thì báo trên sổ, không quay
        /// vòng tải mãi.
        /// </remarks>
        private void docCaSo(Message msg)
        {
            myReader r = msg.reader();
            int batDau = r.posRead;
            List<The> moi;
            try
            {
                moi = docDanhSach(r, true);
                if (r.posRead != r.buffer.Length)
                {
                    moi = null;
                }
            }
            catch (System.Exception)
            {
                moi = null;
            }
            if (moi == null)
            {
                r.posRead = batDau;
                try
                {
                    moi = docDanhSach(r, false);
                }
                catch (System.Exception ex)
                {
                    loiDuLieu = true;
                    dangTai = false;
                    UnityEngine.Debug.LogError("[SoSuuTam] khong doc duoc goi so: " + ex);
                    return;
                }
            }
            loiDuLieu = false;
            ds.Clear();
            ds.AddRange(moi);
            dangTai = false;
            phienBan++;
            if (tim(idChon) == null)
            {
                idChon = chonMacDinh();
                cuonChu = 0;
            }
        }

        private static List<The> docDanhSach(myReader r, bool khuonMoi)
        {
            int n = r.readShort();
            List<The> ra = new List<The>();
            for (int i = 0; i < n; i++)
            {
                The t = new The();
                t.id = r.readShort();
                t.icon = r.readShort();
                t.hang = r.readByte();
                if (khuonMoi)
                {
                    t.daGop = r.readInt();
                    t.canLen = r.readInt();
                }
                else
                {
                    t.daGop = r.readUnsignedByte();
                    t.canLen = r.readUnsignedByte();
                }
                t.kieu = r.readByte();
                if (t.kieu == 1)
                {
                    t.head = r.readShort();
                    t.body = r.readShort();
                    t.leg = r.readShort();
                    t.bag = r.readShort();
                }
                else
                {
                    t.quai = r.readShort();
                }
                t.ten = r.readUTF();
                t.moTa = r.readUTF();
                t.cap = r.readByte();
                t.dangBat = r.readByte() == 1;
                if (khuonMoi)
                {
                    t.capToiDa = Math.max(1, (int)r.readByte());
                    t.aura = r.readShort();
                    t.auraTuCap = r.readByte();
                }
                int soDong = khuonMoi ? r.readShort() : r.readUnsignedByte();
                t.chiSo = new ItemOption[soDong];
                int capCao = 1;
                for (int j = 0; j < soDong; j++)
                {
                    int idCs = khuonMoi ? r.readShort() : r.readUnsignedByte();
                    int giaTri = khuonMoi ? r.readInt() : r.readUnsignedShort();
                    sbyte tuCap = r.readByte();
                    if (tuCap > capCao)
                    {
                        capCao = tuCap;
                    }
                    try
                    {
                        ItemOption o = new ItemOption(idCs, giaTri);
                        o.activeCard = tuCap;
                        t.chiSo[j] = o;
                    }
                    catch (System.Exception)
                    {
                        t.chiSo[j] = null;
                    }
                }
                if (!khuonMoi)
                {
                    // Khuon cu khong gui cap toi da: lay theo dong chi so cap cao nhat.
                    t.capToiDa = capCao;
                }
                ra.Add(t);
            }
            return ra;
        }

        /// <summary>Thẻ đang bật, không có thì thẻ đầu tiên đã có, không có nữa thì thẻ đầu.</summary>
        private int chonMacDinh()
        {
            for (int i = 0; i < ds.Count; i++)
            {
                if (ds[i].dangBat)
                {
                    return ds[i].id;
                }
            }
            for (int i = 0; i < ds.Count; i++)
            {
                if (ds[i].daCo)
                {
                    return ds[i].id;
                }
            }
            return ds.Count > 0 ? ds[0].id : -1;
        }

        private int demDaCo()
        {
            int n = 0;
            for (int i = 0; i < ds.Count; i++)
            {
                if (ds[i].daCo)
                {
                    n++;
                }
            }
            return n;
        }

        private void locDanhSach()
        {
            dsLoc.Clear();
            for (int i = 0; i < ds.Count; i++)
            {
                The t = ds[i];
                if (boLoc == 1 && !t.daCo)
                {
                    continue;
                }
                if (boLoc == 2 && t.daCo)
                {
                    continue;
                }
                if (boLoc == 3 && !t.dangBat)
                {
                    continue;
                }
                dsLoc.Add(t);
            }
        }

        private int soHangLoc()
        {
            return (dsLoc.Count + soCot - 1) / soCot;
        }

        private void chon(The t)
        {
            if (t == null || t.id == idChon)
            {
                return;
            }
            idChon = t.id;
            cuonChu = 0;
            phienBan++;
            try
            {
                SoundMn.gI().radarClick();
            }
            catch (System.Exception)
            {
            }
        }

        /// <summary>Toạ độ ô thứ <paramref name="i"/> của danh sách đã lọc; false nếu đang cuộn khuất.</summary>
        private bool viTriO(int i, out int x, out int y)
        {
            int hang = i / soCot;
            int cot = i % soCot;
            x = xLuoiDau + cot * (oCo + KHE);
            y = yLuoi + 4 + (hang - cuonHang) * (oCo + KHE);
            return hang >= cuonHang && hang < cuonHang + soHangHien;
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
            locDanhSach();
            float t = hoatAnhMo();
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            g.setColor(0, 0.74f * t);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veKhungChinh(g);
            veDau(g);
            veLoc(g);
            veLuoi(g);
            veChiTiet(g);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }

        private void veKhungChinh(mGraphics g)
        {
            g.setColor(0, 0.5f);
            g.fillRect(x0 + 3, y0 + 5, rong, cao, 12);
            g.veDaiDoc(x0, y0, rong, cao, 12, VANG, VANG_TOI);
            g.veDaiDoc(x0 + 2, y0 + 2, rong - 4, cao - 4, 11, NEN_TREN, NEN_DUOI);
            // Bui sao lap lanh tren nen.
            for (int i = 0; i < 18; i++)
            {
                int sx = x0 + 10 + (i * 97) % Math.max(1, rong - 20);
                int sy = y0 + CAO_DAU + 8 + (i * 53) % Math.max(1, cao - CAO_DAU - 16);
                float a = 0.12f + 0.12f * song(0.07f, i * 13);
                g.setColor(TRANG, a);
                g.fillRect(sx, sy, 2, 2);
            }
            // Hoa van goc vang.
            veGoc(g, x0 + 6, y0 + CAO_DAU + 4, 1, 1);
            veGoc(g, x0 + rong - 7, y0 + CAO_DAU + 4, -1, 1);
            veGoc(g, x0 + 6, y0 + cao - 7, 1, -1);
            veGoc(g, x0 + rong - 7, y0 + cao - 7, -1, -1);
        }

        private static void veGoc(mGraphics g, int x, int y, int hx, int hy)
        {
            g.setColor(VANG, 0.55f);
            g.fillRect(hx > 0 ? x : x - 9, y, 10, 1);
            g.fillRect(x, hy > 0 ? y : y - 9, 1, 10);
        }

        private void veDau(mGraphics g)
        {
            g.veDaiDoc(x0 + 2, y0 + 2, rong - 4, CAO_DAU, 11, DAI_TREN, DAI_DUOI);
            g.setColor(VANG, 0.6f);
            g.fillRect(x0 + 10, y0 + 2 + CAO_DAU - 1, rong - 20, 1);
            g.setColor(TRANG, 0.10f);
            g.fillRect(x0 + 6, y0 + 4, rong - 12, CAO_DAU / 2 - 2, 8);

            mFont.tahoma_7b_yellow.drawString(g, "SỔ SƯU TẦM", x0 + rong / 2, y0 + 5, mFont.CENTER);

            int tong = ds.Count;
            int daCo = demDaCo();
            int wBar = Math.min(150, rong / 3);
            int xb = x0 + rong / 2 - wBar / 2;
            int yb = y0 + 20;
            g.setColor(0, 0.55f);
            g.fillRect(xb, yb, wBar, 5, 3);
            int day = tong <= 0 ? 0 : wBar * daCo / tong;
            if (day > 0)
            {
                g.veDaiDoc(xb, yb, day, 5, 3, VANG, VANG_TOI);
            }
            string td = daCo + "/" + tong;
            mFont.tahoma_7_white.drawString(g, td, xb + wBar + 6, yb - 3, mFont.LEFT);

            // Nut dong.
            int[] o = nutDong;
            g.veDaiDoc(o[0], o[1], o[2], o[3], 6, DO_TREN, DO_DUOI);
            g.setColor(TRANG, 0.18f);
            g.fillRect(o[0] + 2, o[1] + 2, o[2] - 4, o[3] / 2 - 2, 4);
            mFont.tahoma_7b_white.drawString(g, "X", o[0] + o[2] / 2, o[1] + 3, mFont.CENTER);
        }

        private void veLoc(mGraphics g)
        {
            int tong = ds.Count;
            int daCo = demDaCo();
            string[] ten = {
                "Tất cả " + tong, "Đã có " + daCo, "Chưa có " + (tong - daCo), "Đang bật"
            };
            string[] tenNgan = { "Tất cả", "Đã có", "Chưa có", "Đang bật" };
            for (int i = 0; i < 4; i++)
            {
                int[] o = oLoc[i];
                bool sang = boLoc == i;
                if (sang)
                {
                    g.veDaiDoc(o[0], o[1], o[2], o[3], 8, VANG, VANG_TOI);
                }
                else
                {
                    g.setColor(TRANG, 0.09f);
                    g.fillRect(o[0], o[1], o[2], o[3], 8);
                    g.setColor(TRANG, 0.06f);
                    g.fillRect(o[0] + 1, o[1] + 1, o[2] - 2, o[3] / 2 - 1, 6);
                }
                mFont f = sang ? mFont.tahoma_7b_dark : mFont.tahoma_7_white;
                string chu = f.getWidth(ten[i]) + 6 <= o[2] ? ten[i] : tenNgan[i];
                f.drawString(g, chu, o[0] + o[2] / 2, o[1] + 4, mFont.CENTER);
            }
        }

        private void veLuoi(mGraphics g)
        {
            g.setColor(0, 0.28f);
            g.fillRect(xLuoi, yLuoi, rongLuoi, caoLuoi, 8);

            if (ds.Count == 0)
            {
                if (loiDuLieu)
                {
                    mFont.tahoma_7b_red.drawString(g, "Máy chủ chưa cập nhật Sổ sưu tầm",
                            xLuoi + rongLuoi / 2, yLuoi + caoLuoi / 2 - 12, mFont.CENTER);
                    mFont.tahoma_7_white.drawString(g, "Báo quản trị cập nhật máy chủ",
                            xLuoi + rongLuoi / 2, yLuoi + caoLuoi / 2 + 2, mFont.CENTER);
                }
                else if (dangTai)
                {
                    veVongTai(g, xLuoi + rongLuoi / 2, yLuoi + caoLuoi / 2 - 6);
                    mFont.tahoma_7_white.drawString(g, "Đang mở sổ...", xLuoi + rongLuoi / 2,
                            yLuoi + caoLuoi / 2 + 12, mFont.CENTER);
                }
                else
                {
                    mFont.tahoma_7_white.drawString(g, "Sổ chưa có thẻ nào", xLuoi + rongLuoi / 2,
                            yLuoi + caoLuoi / 2 - 5, mFont.CENTER);
                }
                return;
            }
            if (dsLoc.Count == 0)
            {
                mFont.tahoma_7_white.drawString(g, "Không có thẻ nào ở mục này",
                        xLuoi + rongLuoi / 2, yLuoi + caoLuoi / 2 - 5, mFont.CENTER);
                return;
            }
            int soHang = soHangLoc();
            cuonHang = kep(cuonHang, 0, Math.max(0, soHang - soHangHien));

            g.setClip(xLuoi, yLuoi, rongLuoi, caoLuoi);
            for (int i = 0; i < dsLoc.Count; i++)
            {
                int x, y;
                if (viTriO(i, out x, out y))
                {
                    veO(g, dsLoc[i], x, y);
                }
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            if (soHang > soHangHien)
            {
                int xv = xLuoi + rongLuoi - 4;
                int caoRanh = caoLuoi - 8;
                g.setColor(TRANG, 0.10f);
                g.fillRect(xv, yLuoi + 4, 3, caoRanh, 2);
                int caoTay = Math.max(14, caoRanh * soHangHien / soHang);
                int toiDa = soHang - soHangHien;
                int dy = toiDa <= 0 ? 0 : (caoRanh - caoTay) * cuonHang / toiDa;
                g.setColor(VANG, 0.85f);
                g.fillRect(xv, yLuoi + 4 + dy, 3, caoTay, 2);
            }
        }

        private void veO(mGraphics g, The t, int x, int y)
        {
            int mau = mauHang(t.hang);
            bool dangChon = t.id == idChon;

            if (t.dangBat)
            {
                float a = 0.40f + 0.25f * song(0.18f, 0);
                g.setColor(XANH_BAT, a);
                g.fillRect(x - 3, y - 3, oCo + 6, oCo + 6, 10);
            }
            if (dangChon)
            {
                g.setColor(VANG, 0.92f);
                g.fillRect(x - 2, y - 2, oCo + 4, oCo + 4, 9);
            }
            g.veDaiDoc(x, y, oCo, oCo, 8,
                    t.daCo ? tron(mau, TRANG, 0.30f) : tron(mau, 0, 0.55f),
                    t.daCo ? tron(mau, 0, 0.40f) : tron(mau, 0, 0.78f));
            g.veDaiDoc(x + 2, y + 2, oCo - 4, oCo - 4, 7, O_TREN, O_DUOI);
            // Anh mau hang hat len tu day o.
            g.setColor(mau, t.daCo ? 0.30f : 0.10f);
            g.fillRect(x + 2, y + oCo / 2, oCo - 4, oCo / 2 - 2, 7);
            g.setColor(TRANG, 0.07f);
            g.fillRect(x + 3, y + 3, oCo - 6, oCo / 3, 6);

            SmallImage.drawSmallImage(g, t.icon, x + oCo / 2, y + oCo / 2 - 2, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);

            if (!t.daCo)
            {
                g.setColor(0, 0.58f);
                g.fillRect(x + 2, y + 2, oCo - 4, oCo - 4, 7);
                veKhoa(g, x + oCo / 2, y + oCo / 2);
            }
            else
            {
                string cap = t.cap > 0 ? "Lv" + t.cap : "Mới";
                int wc = mFont.tahoma_7_white.getWidth(cap) + 6;
                g.setColor(0, 0.66f);
                g.fillRect(x + 3, y + oCo - 15, wc, 10, 4);
                mFont.tahoma_7_white.drawString(g, cap, x + 6, y + oCo - 16, mFont.LEFT);

                int wTd = oCo - 8;
                int day = t.toiDa ? wTd : (t.canLen <= 0 ? 0 : (int)((long)wTd * t.daGop / t.canLen));
                g.setColor(0, 0.55f);
                g.fillRect(x + 4, y + oCo - 4, wTd, 2);
                g.setColor(t.toiDa ? VANG : mau, 1f);
                g.fillRect(x + 4, y + oCo - 4, kep(day, 0, wTd), 2);
                if (t.toiDa)
                {
                    g.setColor(VANG, 1f);
                    veSao(g, x + oCo - 8, y + 8);
                }
            }
            if (t.dangBat)
            {
                g.setColor(XANH_BAT, 1f);
                g.fillRect(x + oCo - 13, y + 2, 11, 10, 5);
                g.setColor(0x0F3D20, 1f);
                g.drawLine(x + oCo - 11, y + 7, x + oCo - 9, y + 9);
                g.drawLine(x + oCo - 9, y + 9, x + oCo - 5, y + 4);
                g.drawLine(x + oCo - 11, y + 6, x + oCo - 9, y + 8);
                g.drawLine(x + oCo - 9, y + 8, x + oCo - 5, y + 3);
            }
            if (dangChon)
            {
                // Vet sang luot cheo qua o dang chon.
                int chuKy = 42;
                int p = GameCanvas.gameTick % (chuKy + 24);
                if (p < chuKy)
                {
                    int sx = x - 10 + (oCo + 20) * p / chuKy;
                    g.setClip(x + 2, y + 2, oCo - 4, oCo - 4);
                    g.setColor(TRANG, 0.22f);
                    g.fillRect(sx, y, 5, oCo);
                    g.setColor(TRANG, 0.12f);
                    g.fillRect(sx + 7, y, 3, oCo);
                    g.setClip(xLuoi, yLuoi, rongLuoi, caoLuoi);
                }
            }
        }

        private static void veKhoa(mGraphics g, int cx, int cy)
        {
            g.setColor(BAC, 0.95f);
            g.fillRect(cx - 5, cy - 9, 10, 9, 5);
            g.setColor(O_DUOI, 1f);
            g.fillRect(cx - 3, cy - 7, 6, 7, 3);
            g.setColor(BAC, 1f);
            g.fillRect(cx - 7, cy - 3, 14, 10, 3);
            g.setColor(tron(BAC, 0, 0.35f), 1f);
            g.fillRect(cx - 7, cy + 3, 14, 4, 3);
            g.setColor(O_DUOI, 1f);
            g.fillRect(cx - 1, cy, 2, 4);
        }

        /// <summary>Sao hình thoi 7 điểm, dùng màu đang đặt.</summary>
        private static void veSao(mGraphics g, int x, int y)
        {
            for (int i = 0; i <= 3; i++)
            {
                g.fillRect(x - i, y - 3 + i, i * 2 + 1, 1);
                g.fillRect(x - i, y + 3 - i, i * 2 + 1, 1);
            }
        }

        private static void veVongTai(mGraphics g, int cx, int cy)
        {
            int sang = (GameCanvas.gameTick / 2) % 8;
            for (int i = 0; i < 8; i++)
            {
                float goc = i * 0.7854f;
                int dx = (int)(UnityEngine.Mathf.Cos(goc) * 12f);
                int dy = (int)(UnityEngine.Mathf.Sin(goc) * 12f);
                float xa = ((i - sang + 8) % 8) / 8f;
                g.setColor(VANG, 0.15f + 0.85f * (1f - xa));
                g.fillRect(cx + dx - 2, cy + dy - 2, 5, 5, 2);
            }
        }

        // ------------------------------------------------------------------
        //  Khung chi tiết
        // ------------------------------------------------------------------
        private float mucVe;
        private int idMucVe = -1;
        private long lucBamNut;

        private void veChiTiet(mGraphics g)
        {
            g.setColor(0, 0.30f);
            g.fillRect(xCT, yCT, rongCT, caoCT, 8);
            The t = theDangChon();
            if (t == null)
            {
                mFont.tahoma_7_white.drawString(g, "Chọn một thẻ để xem",
                        xCT + rongCT / 2, yCT + caoCT / 2 - 5, mFont.CENTER);
                return;
            }
            int mau = mauHang(t.hang);
            veXemTruoc(g, t, mau);

            mFont fTen = t.daCo ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_white;
            fTen.drawString(g, catRong(t.ten, fTen, rongCT - 12), xCT + rongCT / 2, yTen,
                    mFont.CENTER);
            veSaoCap(g, t);
            veTienDo(g, t, mau);
            veNoiDung(g, t);
            veNutHanhDong(g, t);
        }

        private void veXemTruoc(mGraphics g, The t, int mau)
        {
            int[] v = oXem;
            g.veDaiDoc(v[0], v[1], v[2], v[3], 8, tron(mau, NEN_DUOI, 0.55f), tron(NEN_DUOI, 0, 0.3f));
            int cx = v[0] + v[2] / 2;
            int cy = v[1] + v[3] / 2 + 4;
            float nhip = 0.5f + 0.5f * song(0.09f, 0);
            for (int i = 3; i >= 1; i--)
            {
                int r = 8 + i * 9 + (int)(nhip * 3f);
                g.setColor(mau, t.daCo ? 0.11f : 0.04f);
                g.fillRect(cx - r, cy - r / 2 - 2, r * 2, r, r / 2);
            }
            int yChan = v[1] + v[3] - 9;
            g.setColor(0, 0.4f);
            g.fillRect(cx - 18, yChan - 3, 36, 6, 3);

            g.setClip(v[0], v[1], v[2], v[3]);
            veMoHinh(g, t, cx, yChan, cy);
            if (t.daCo)
            {
                for (int i = 0; i < 7; i++)
                {
                    int chuKy = 46 + i * 7;
                    int p = (GameCanvas.gameTick + i * 17) % chuKy;
                    int sx = v[0] + 10 + (i * 37) % Math.max(1, v[2] - 20);
                    int sy = v[1] + v[3] - 6 - (v[3] - 12) * p / chuKy;
                    g.setColor(i % 2 == 0 ? VANG : mau, 0.9f * (1f - (float)p / chuKy));
                    g.fillRect(sx, sy, 2, 2);
                }
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            if (!t.daCo)
            {
                g.setColor(0, 0.45f);
                g.fillRect(v[0], v[1], v[2], v[3], 8);
                veKhoa(g, cx, cy);
            }

            string th = tenHang(t.hang);
            int wh = mFont.tahoma_7b_dark.getWidth(th) + 10;
            g.veDaiDoc(v[0] + 4, v[1] + 4, wh, 11, 5, tron(mau, TRANG, 0.3f), mau);
            mFont.tahoma_7b_dark.drawString(g, th, v[0] + 4 + wh / 2, v[1] + 4, mFont.CENTER);
            if (t.dangBat)
            {
                string b = "ĐANG BẬT";
                int wb = mFont.tahoma_7b_dark.getWidth(b) + 10;
                g.veDaiDoc(v[0] + v[2] - 4 - wb, v[1] + 4, wb, 11, 5, XANH_BAT, XANH_BAT_TOI);
                mFont.tahoma_7b_dark.drawString(g, b, v[0] + v[2] - 4 - wb / 2, v[1] + 4, mFont.CENTER);
            }
        }

        /// <summary>Hình động của thẻ; thiếu dữ liệu thì vẽ icon thay.</summary>
        private void veMoHinh(mGraphics g, The t, int cx, int yChan, int cy)
        {
            bool daVe = false;
            try
            {
                if (t.xem == null)
                {
                    t.xem = new Info_RadaScr();
                    t.xem.typeMonster = (sbyte)(t.kieu == 1 ? 1 : 0);
                    if (t.kieu == 1)
                    {
                        t.xem.charInfo = Info_RadaScr.SetCharInfo(t.head, t.body, t.leg, t.bag);
                    }
                    else if (t.quai >= 0)
                    {
                        t.xem.mobInfo = new Mob();
                        t.xem.mobInfo.templateId = (short)t.quai;
                    }
                }
                if (t.kieu == 1 && t.head >= 0 && t.body >= 0)
                {
                    t.xem.paintInfo(g, cx, yChan);
                    daVe = true;
                }
                else if (t.kieu != 1 && t.quai >= 0 && Mob.arrMobTemplate != null
                        && t.quai < Mob.arrMobTemplate.Length && Mob.arrMobTemplate[t.quai] != null)
                {
                    t.xem.paintInfo(g, cx, yChan);
                    daVe = Mob.arrMobTemplate[t.quai].data != null;
                }
            }
            catch (System.Exception)
            {
                daVe = false;
            }
            if (!daVe)
            {
                SmallImage.drawSmallImage(g, t.icon, cx, cy, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            }
        }

        private void veSaoCap(mGraphics g, The t)
        {
            int n = t.capToiDa;
            if (n <= 12)
            {
                int buoc = 11;
                int x = xCT + rongCT / 2 - (n - 1) * buoc / 2;
                for (int i = 0; i < n; i++)
                {
                    bool dat = i < t.bac;
                    if (dat)
                    {
                        g.setColor(VANG, 0.35f);
                        g.fillRect(x + i * buoc - 5, ySao - 1, 11, 9, 4);
                    }
                    g.setColor(dat ? VANG : XAM_TOI, 1f);
                    veSao(g, x + i * buoc, ySao + 3);
                }
            }
            else
            {
                mFont.tahoma_7b_yellow.drawString(g, "Lv." + t.bac + " / " + n,
                        xCT + rongCT / 2, ySao - 2, mFont.CENTER);
            }
        }

        private void veTienDo(mGraphics g, The t, int mau)
        {
            int x = xCT + 10;
            int w = rongCT - 20;
            int h = 10;
            int y = yTienDo;
            g.setColor(0, 0.6f);
            g.fillRect(x, y, w, h, 5);
            float muc = !t.daCo ? 0f : (t.toiDa ? 1f
                    : (t.canLen <= 0 ? 0f : (float)t.daGop / t.canLen));
            if (muc > 1f)
            {
                muc = 1f;
            }
            if (idMucVe != t.id)
            {
                idMucVe = t.id;
                mucVe = 0f;
            }
            mucVe += (muc - mucVe) * 0.18f;
            int day = (int)(w * mucVe);
            if (day > 2)
            {
                int dau = t.toiDa ? VANG : tron(mau, TRANG, 0.35f);
                int cuoi = t.toiDa ? VANG_TOI : tron(mau, 0, 0.30f);
                g.veDaiDoc(x, y, day, h, 5, dau, cuoi);
                g.setColor(TRANG, 0.28f);
                g.fillRect(x + 2, y + 1, day - 4, 3, 2);
            }
            string chu;
            if (!t.daCo)
            {
                chu = "Chưa sưu tầm";
            }
            else if (t.toiDa)
            {
                chu = "CẤP TỐI ĐA";
            }
            else
            {
                chu = t.daGop + " / " + t.canLen + "  →  Lv." + (t.bac + 1);
            }
            mFont.tahoma_7_white.drawString(g, chu, x + w / 2, y - 1, mFont.CENTER);
        }

        // ---- Phần chữ: mô tả + chỉ số theo cấp, cuộn được ----
        private class Dong
        {
            public string chu;
            public mFont f;
            public int loai;        // 0 thuong, 1 tieu de, 2 khoang trong
            public bool hieuLuc;
        }

        private readonly List<Dong> dongChu = new List<Dong>();
        private int phienBanDaDung = -1;

        private void them(string chu, mFont f, int loai, bool hieuLuc)
        {
            Dong d = new Dong();
            d.chu = chu;
            d.f = f;
            d.loai = loai;
            d.hieuLuc = hieuLuc;
            dongChu.Add(d);
        }

        private void dungNoiDung(The t)
        {
            dongChu.Clear();
            int w = rongCT - 24;
            if (!string.IsNullOrEmpty(t.moTa))
            {
                string[] cac = mFont.tahoma_7_white.splitFontArray(t.moTa, w);
                for (int i = 0; i < cac.Length; i++)
                {
                    them(cac[i], mFont.tahoma_7_white, 0, true);
                }
                them("", null, 2, false);
            }
            int capMax = 0;
            for (int i = 0; i < t.chiSo.Length; i++)
            {
                if (t.chiSo[i] != null && t.chiSo[i].activeCard > capMax)
                {
                    capMax = t.chiSo[i].activeCard;
                }
            }
            for (int c = 0; c <= capMax; c++)
            {
                bool coDong = false;
                for (int i = 0; i < t.chiSo.Length; i++)
                {
                    if (t.chiSo[i] != null && t.chiSo[i].activeCard == c)
                    {
                        coDong = true;
                        break;
                    }
                }
                if (!coDong)
                {
                    continue;
                }
                bool hieuLuc = c == 0 ? t.daCo : (t.daCo && t.dangBat && t.bac >= c);
                them(c == 0 ? "MỞ KHOÁ  ·  có ngay khi sưu tầm" : "LV." + c + "  ·  khi bật thẻ",
                        hieuLuc ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_white, 1, hieuLuc);
                for (int i = 0; i < t.chiSo.Length; i++)
                {
                    ItemOption o = t.chiSo[i];
                    if (o == null || o.activeCard != c)
                    {
                        continue;
                    }
                    string s;
                    try
                    {
                        s = o.getOptionString();
                    }
                    catch (System.Exception)
                    {
                        s = "";
                    }
                    if (string.IsNullOrEmpty(s))
                    {
                        continue;
                    }
                    mFont f = hieuLuc ? mFont.tahoma_7b_green : mFont.tahoma_7_grey;
                    string[] cac = f.splitFontArray("+ " + s, w - 6);
                    for (int k = 0; k < cac.Length; k++)
                    {
                        them(cac[k], f, 0, hieuLuc);
                    }
                }
            }
            if (t.aura > 0)
            {
                bool coAura = t.dangBat && t.bac >= t.auraTuCap;
                them("HÀO QUANG", coAura ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_white, 1, coAura);
                them("Hiện hào quang khi bật thẻ"
                        + (t.auraTuCap > 0 ? " từ Lv." + t.auraTuCap : ""),
                        coAura ? mFont.tahoma_7b_green : mFont.tahoma_7_grey, 0, coAura);
            }
            if (t.chiSo.Length == 0 && t.aura <= 0)
            {
                them("Thẻ này chưa có chỉ số nào", mFont.tahoma_7_grey, 0, false);
            }
            caoNoiDung = 0;
            for (int i = 0; i < dongChu.Count; i++)
            {
                caoNoiDung += caoDong(dongChu[i]);
            }
        }

        private static int caoDong(Dong d)
        {
            return d.loai == 1 ? 15 : (d.loai == 2 ? 5 : 11);
        }

        private void veNoiDung(mGraphics g, The t)
        {
            int khoa = phienBan * 31 + t.id;
            if (khoa != phienBanDaDung)
            {
                phienBanDaDung = khoa;
                dungNoiDung(t);
            }
            cuonChu = kep(cuonChu, 0, Math.max(0, caoNoiDung - caoChu));
            g.setClip(xCT + 4, yChu, rongCT - 8, caoChu);
            int y = yChu - cuonChu;
            for (int i = 0; i < dongChu.Count; i++)
            {
                Dong d = dongChu[i];
                int h = caoDong(d);
                if (y + h >= yChu && y <= yChu + caoChu)
                {
                    if (d.loai == 1)
                    {
                        g.setColor(d.hieuLuc ? VANG : XAM_TOI, d.hieuLuc ? 0.22f : 0.45f);
                        g.fillRect(xCT + 8, y + 1, rongCT - 16, 12, 5);
                        g.setColor(d.hieuLuc ? VANG : BAC, d.hieuLuc ? 0.9f : 0.35f);
                        g.fillRect(xCT + 8, y + 1, 3, 12, 2);
                        d.f.drawString(g, d.chu, xCT + 15, y + 1, mFont.LEFT);
                    }
                    else if (d.loai == 0)
                    {
                        d.f.drawString(g, d.chu, xCT + 12, y, mFont.LEFT);
                    }
                }
                y += h;
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            if (caoNoiDung > caoChu)
            {
                int xv = xCT + rongCT - 5;
                g.setColor(TRANG, 0.10f);
                g.fillRect(xv, yChu, 2, caoChu, 1);
                int caoTay = Math.max(10, caoChu * caoChu / caoNoiDung);
                int toiDa = caoNoiDung - caoChu;
                int dy = (caoChu - caoTay) * cuonChu / Math.max(1, toiDa);
                g.setColor(VANG, 0.8f);
                g.fillRect(xv, yChu + dy, 2, caoTay, 1);
            }
        }

        private void veNutHanhDong(mGraphics g, The t)
        {
            int[] o = nutHanhDong;
            bool nhan = mSystem.currentTimeMillis() - lucBamNut < 140;
            int dy = nhan ? 1 : 0;
            if (!t.daCo)
            {
                g.setColor(XAM_TOI, 0.9f);
                g.fillRect(o[0], o[1], o[2], o[3], 8);
                mFont.tahoma_7_grey.drawString(g, "CHƯA SƯU TẦM", o[0] + o[2] / 2, o[1] + 5,
                        mFont.CENTER);
                return;
            }
            if (t.dangBat)
            {
                g.veDaiDoc(o[0], o[1] + dy, o[2], o[3], 8, XANH_BAT, XANH_BAT_TOI);
            }
            else
            {
                g.veDaiDoc(o[0], o[1] + dy, o[2], o[3], 8, VANG, VANG_TOI);
            }
            g.setColor(TRANG, 0.22f);
            g.fillRect(o[0] + 3, o[1] + dy + 2, o[2] - 6, o[3] / 2 - 2, 6);
            if (!t.dangBat)
            {
                int chuKy = 60;
                int p = GameCanvas.gameTick % (chuKy + 40);
                if (p < chuKy)
                {
                    int sx = o[0] - 12 + (o[2] + 24) * p / chuKy;
                    g.setClip(o[0] + 2, o[1] + dy, o[2] - 4, o[3]);
                    g.setColor(TRANG, 0.30f);
                    g.fillRect(sx, o[1] + dy, 6, o[3]);
                    g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
                }
            }
            string chu = t.dangBat ? "ĐANG BẬT  ·  CHẠM ĐỂ TẮT" : "BẬT THẺ";
            mFont f = t.dangBat ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark;
            if (f.getWidth(chu) + 8 > o[2])
            {
                chu = t.dangBat ? "TẮT THẺ" : "BẬT";
            }
            f.drawString(g, chu, o[0] + o[2] / 2, o[1] + dy + 5, mFont.CENTER);
        }

        private static string catRong(string s, mFont f, int w)
        {
            if (s == null)
            {
                return "";
            }
            if (f.getWidth(s) <= w)
            {
                return s;
            }
            while (s.Length > 1 && f.getWidth(s + "..") > w)
            {
                s = s.Substring(0, s.Length - 1);
            }
            return s + "..";
        }

        // ------------------------------------------------------------------
        //  Cuộn
        // ------------------------------------------------------------------
        private int yMocKeo;
        private bool keoLuoi;
        private bool keoChu;
        private bool daKeoXa;
        private const int NGUONG_KEO = 6;

        private static bool trong(int px, int py, int x, int y, int w, int h)
        {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }

        private void cuon()
        {
            int toiDaHang = Math.max(0, soHangLoc() - soHangHien);
            int toiDaChu = Math.max(0, caoNoiDung - caoChu);
            if (GameCanvas.pXYScrollMouse != 0)
            {
                int huong = GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
                if (trong(GameCanvas.pxMouse, GameCanvas.pyMouse, xLuoi, yLuoi, rongLuoi, caoLuoi))
                {
                    cuonHang += huong;
                }
                else if (trong(GameCanvas.pxMouse, GameCanvas.pyMouse, xCT, yChu, rongCT, caoChu))
                {
                    cuonChu += huong * 22;
                }
            }
            if (GameCanvas.isPointerDown)
            {
                if (!keoLuoi && !keoChu)
                {
                    if (trong(GameCanvas.pxFirst, GameCanvas.pyFirst, xLuoi, yLuoi, rongLuoi, caoLuoi))
                    {
                        keoLuoi = true;
                    }
                    else if (trong(GameCanvas.pxFirst, GameCanvas.pyFirst, xCT, yChu, rongCT, caoChu))
                    {
                        keoChu = true;
                    }
                    daKeoXa = false;
                    yMocKeo = GameCanvas.pyFirst;
                }
                if (keoLuoi || keoChu)
                {
                    if (Math.abs(GameCanvas.py - GameCanvas.pyFirst) > NGUONG_KEO)
                    {
                        daKeoXa = true;
                    }
                    if (keoLuoi)
                    {
                        int b = oCo + KHE;
                        int buoc = (GameCanvas.py - yMocKeo) / b;
                        if (buoc != 0)
                        {
                            cuonHang -= buoc;
                            yMocKeo += buoc * b;
                        }
                    }
                    else
                    {
                        cuonChu -= GameCanvas.py - yMocKeo;
                        yMocKeo = GameCanvas.py;
                    }
                }
            }
            else
            {
                keoLuoi = false;
                keoChu = false;
            }
            cuonHang = kep(cuonHang, 0, toiDaHang);
            cuonChu = kep(cuonChu, 0, toiDaChu);
        }

        // ------------------------------------------------------------------
        //  Chạm và phím
        // ------------------------------------------------------------------
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            tinhBoCuc();
            locDanhSach();
            cuon();
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            // Lan nha ngon da mo so (tu the He thong) khong duoc tinh la bam.
            if (mSystem.currentTimeMillis() - lucMo < 160)
            {
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            if (daKeoXa)
            {
                daKeoXa = false;
                return true;
            }
            if (cham(nutDong[0], nutDong[1], nutDong[2], nutDong[3]))
            {
                dong();
                return true;
            }
            if (!GameCanvas.isPointerHoldIn(x0, y0, rong, cao))
            {
                GameCanvas.clearAllPointerEvent();
                dong();
                return true;
            }
            for (int i = 0; i < 4; i++)
            {
                if (cham(oLoc[i][0], oLoc[i][1], oLoc[i][2], oLoc[i][3]))
                {
                    boLoc = i;
                    cuonHang = 0;
                    return true;
                }
            }
            if (GameCanvas.isPointerHoldIn(xLuoi, yLuoi, rongLuoi, caoLuoi))
            {
                for (int i = 0; i < dsLoc.Count; i++)
                {
                    int x, y;
                    if (viTriO(i, out x, out y) && cham(x, y, oCo, oCo))
                    {
                        chon(dsLoc[i]);
                        return true;
                    }
                }
            }
            The t = theDangChon();
            if (t != null && t.daCo
                    && cham(nutHanhDong[0], nutHanhDong[1], nutHanhDong[2], nutHanhDong[3]))
            {
                batTat(t);
                return true;
            }
            return true;
        }

        private void batTat(The t)
        {
            lucBamNut = mSystem.currentTimeMillis();
            Service.gI().SendRada(1, t.id);
            try
            {
                SoundMn.gI().radarClick();
            }
            catch (System.Exception)
            {
            }
        }

        /// <summary>Esc đóng sổ, mũi tên đổi thẻ, Enter bật / tắt.</summary>
        public void nhanPhim()
        {
            if (!dangMo)
            {
                return;
            }
            if (UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.Escape))
            {
                dong();
                return;
            }
            int buoc = 0;
            if (UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.RightArrow))
            {
                buoc = 1;
            }
            else if (UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.LeftArrow))
            {
                buoc = -1;
            }
            else if (UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.DownArrow))
            {
                buoc = soCot;
            }
            else if (UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.UpArrow))
            {
                buoc = -soCot;
            }
            if (buoc != 0 && dsLoc.Count > 0)
            {
                int hienTai = 0;
                for (int i = 0; i < dsLoc.Count; i++)
                {
                    if (dsLoc[i].id == idChon)
                    {
                        hienTai = i;
                        break;
                    }
                }
                int moi = kep(hienTai + buoc, 0, dsLoc.Count - 1);
                chon(dsLoc[moi]);
                int hang = moi / soCot;
                if (hang < cuonHang)
                {
                    cuonHang = hang;
                }
                else if (hang >= cuonHang + soHangHien)
                {
                    cuonHang = hang - soHangHien + 1;
                }
                return;
            }
            if (UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.Return)
                    || UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.KeypadEnter))
            {
                The t = theDangChon();
                if (t != null && t.daCo)
                {
                    batTat(t);
                }
            }
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
