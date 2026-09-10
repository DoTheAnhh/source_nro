using System;
using System.Collections.Generic;

namespace Game1.God
{
    /// <summary>
    /// Báº£ng <b>Ná»i táº¡i</b> â váº½ ÄÃ¨ lÃªn mÃ n chÆ¡i, tá»± báº¯t cháº¡m.
    /// </summary>
    /// <remarks>
    /// <para>TrÆ°á»c ÄÃ¢y ná»i táº¡i chá» vÃ o ÄÆ°á»£c qua má»t menu bá»n nÃºt cá»§a NPC, vÃ 
    /// muá»n xem cÃ³ nhá»¯ng ná»i táº¡i nÃ o thÃ¬ pháº£i má» má»t báº£ng khÃ¡c ná»¯a. Báº£ng nÃ y gom
    /// háº¿t vÃ o má»t chá», má» tháº³ng tá»« tab Ká»¹ nÄng.</para>
    ///
    /// <para><b>Chá» hiá»n ná»i táº¡i cá»§a hÃ nh tinh mÃ¬nh.</b> Danh sÃ¡ch do mÃ¡y chá»§
    /// lá»c sáºµn theo <c>player.gender</c> rá»i má»i gá»­i xuá»ng, nÃªn khÃ´ng cÃ³ ÄÆ°á»ng
    /// nÃ o nhÃ¬n tháº¥y â hay má» trÃºng â ná»i táº¡i cá»§a hÃ nh tinh khÃ¡c.</para>
    ///
    /// <h2>Má»i vÃ¹ng do <see cref="oBang"/> tÃ­nh má»t láº§n</h2>
    ///
    /// <para>Cáº£ pháº§n váº½ láº«n pháº§n báº¯t cháº¡m Äá»u Äá»c tá»« ÄÃ³. Báº£n trÆ°á»c hai bÃªn tá»±
    /// cá»ng láº¥y toáº¡ Äá» báº±ng cÃ¹ng má»t dÃ£y sá» gÃµ tay, vÃ  chá» cáº§n má»t chá» lá»ch vÃ i
    /// Äiá»m lÃ  chá»¯ chá»ng lÃªn nhau cÃ²n nÃºt thÃ¬ báº¥m trÆ°á»£t.</para>
    /// </remarks>
    public class NoiTaiUI
    {
        private static NoiTaiUI instance;

        public static NoiTaiUI getInstance()
        {
            return instance ?? (instance = new NoiTaiUI());
        }

        public bool dangMo;

        // ==================================================================
        //  MÃ u
        // ==================================================================

        private const int MAU_NEN = 0x241D33;
        private const int MAU_NEN_2 = 0x2E2642;
        private const int MAU_VIEN = 0xC8933C;
        private const int MAU_O = 0x3A3154;
        private const int MAU_O_CHON = 0x6B4FA8;
        private const int MAU_O_MO = 0x322B49;
        private const int MAU_NUT_VANG = 0xB8862B;
        private const int MAU_NUT_NGOC = 0x2E8B8B;
        private const int MAU_NUT_TAT = 0x4A4458;
        private const int MAU_NHANH = 0x8A5BD0;

        // ==================================================================
        //  KÃ­ch thÆ°á»c
        // ==================================================================

        /// <summary>Cao má»t dÃ²ng trong danh sÃ¡ch.</summary>
        private const int CAO_DONG = 24;

        /// <summary>Sá» dÃ²ng tháº¥y ÄÆ°á»£c cÃ¹ng lÃºc.</summary>
        private const int SO_DONG_THAY = 5;

        private const int LE = 8;
        private const int CAO_TIEU_DE = 20;
        private const int CAO_THE = 42;
        private const int CAO_HANG_TIEN = 17;
        private const int CAO_NUT = 30;
        private const int CAO_NHAN = 14;
        private const int CAO_NUT_NHO = 16;
        private const int CAO_NUT_NHANH = 26;

        private int x0, y0, rong, cao;
        private int cuon;
        private int dongChon = -1;

        /// <summary>Má»t ná»i táº¡i trong danh sÃ¡ch cá»§a hÃ nh tinh nÃ y.</summary>
        public sealed class Muc
        {
            public int id;
            public int icon;
            public string moTa;
            public int chiSoMin;
            public int chiSoMax;
        }

        private readonly List<Muc> danhSach = new List<Muc>();

        private string tenDangCo = "";
        private int iconDangCo = -1;
        private long giaVang;
        private int giaNgoc;
        private long vangDangCo;
        private int ngocDangCo;

        /// <summary>ÄÃ£ nháº­n dá»¯ liá»u tá»« mÃ¡y chá»§ láº§n nÃ o chÆ°a.</summary>
        private bool coDuLieu;

        // ==================================================================
        //  Má» / ÄÃ³ng
        // ==================================================================

        public void mo()
        {
            dangMo = true;
            cuon = 0;
            dongChon = -1;
            // Xin lai du lieu moi lan mo: vang, ngoc va gia deu doi giua chung.
            Service.gI().noiTaiXinBang();
        }

        public void dong()
        {
            dangMo = false;
        }

        /// <summary>MÃ¡y chá»§ gá»­i xuá»ng toÃ n bá» dá»¯ liá»u báº£ng.</summary>
        public void nhanBang(string tenHienTai, int icon, long vang, int ngoc,
                long vangCo, int ngocCo, List<Muc> ds)
        {
            tenDangCo = (tenHienTai == null) ? "" : tenHienTai;
            iconDangCo = icon;
            giaVang = vang;
            giaNgoc = ngoc;
            vangDangCo = vangCo;
            ngocDangCo = ngocCo;
            danhSach.Clear();
            if (ds != null)
            {
                danhSach.AddRange(ds);
            }
            if (dongChon >= danhSach.Count)
            {
                dongChon = -1;
            }
            coDuLieu = true;
        }

        // ==================================================================
        //  Toáº¡ Äá» â Má»T nguá»n cho cáº£ váº½ láº«n cháº¡m
        // ==================================================================

        /// <summary>
        /// CÃ¡c vÃ¹ng cá»§a báº£ng: {x, y, rá»ng, cao}.
        /// </summary>
        /// <remarks>
        /// Thá»© tá»±: 0 khung, 1 nÃºt X, 2 tháº» "Äang mang", 3 hÃ ng tiá»n,
        /// 4 nÃºt má» báº±ng vÃ ng, 5 nÃºt má» báº±ng ngá»c, 6 nhÃ£n danh sÃ¡ch,
        /// 7 vÃ¹ng danh sÃ¡ch, 8 nÃºt LÃªn, 9 nÃºt Xuá»ng, 10 nÃºt Má» nhanh.
        /// </remarks>
        private int[][] oBang()
        {
            rong = GameCanvas.w - 24;
            if (rong > 320)
            {
                rong = 320;
            }
            if (rong < 236)
            {
                rong = 236;
            }

            int caoDs = SO_DONG_THAY * CAO_DONG + 4;
            cao = CAO_TIEU_DE + CAO_THE + CAO_HANG_TIEN + CAO_NUT + CAO_NHAN
                    + caoDs + CAO_NUT_NHO + CAO_NUT_NHANH + LE * 3;

            // Man hinh thap qua thi bo bot dong danh sach, dung de bang tran ra
            // ngoai man.
            int caoToiDa = GameCanvas.h - 16;
            int botDong = 0;
            while (cao > caoToiDa && SO_DONG_THAY - botDong > 2)
            {
                botDong++;
                caoDs = (SO_DONG_THAY - botDong) * CAO_DONG + 4;
                cao = CAO_TIEU_DE + CAO_THE + CAO_HANG_TIEN + CAO_NUT + CAO_NHAN
                        + caoDs + CAO_NUT_NHO + CAO_NUT_NHANH + LE * 3;
            }
            soDongThay = SO_DONG_THAY - botDong;

            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;
            if (y0 < 8)
            {
                y0 = 8;
            }

            int xTrong = x0 + LE;
            int rongTrong = rong - LE * 2;
            int nua = (rongTrong - 6) / 2;

            int[][] o = new int[11][];
            o[0] = new int[] { x0, y0, rong, cao };
            o[1] = new int[] { x0 + rong - 22, y0 + 4, 17, 14 };

            int y = y0 + CAO_TIEU_DE;
            o[2] = new int[] { xTrong, y, rongTrong, CAO_THE };
            y += CAO_THE + 3;
            o[3] = new int[] { xTrong, y, rongTrong, CAO_HANG_TIEN - 3 };
            y += CAO_HANG_TIEN;
            o[4] = new int[] { xTrong, y, nua, CAO_NUT - 4 };
            o[5] = new int[] { xTrong + nua + 6, y, nua, CAO_NUT - 4 };
            y += CAO_NUT;
            o[6] = new int[] { xTrong, y, rongTrong, CAO_NHAN - 2 };
            y += CAO_NHAN;
            o[7] = new int[] { xTrong, y, rongTrong, caoDs };
            y += caoDs + 3;
            o[8] = new int[] { xTrong, y, 44, CAO_NUT_NHO - 3 };
            o[9] = new int[] { xTrong + 48, y, 52, CAO_NUT_NHO - 3 };
            y += CAO_NUT_NHO;
            o[10] = new int[] { xTrong, y, rongTrong, CAO_NUT_NHANH - 4 };
            return o;
        }

        /// <summary>Sá» dÃ²ng tháº­t sá»± tháº¥y ÄÆ°á»£c á» khung hÃ¬nh vá»«a tÃ­nh.</summary>
        private int soDongThay = SO_DONG_THAY;

        // ==================================================================
        //  Váº½
        // ==================================================================

        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            int[][] o = oBang();

            // Nen mo phu kin man: bang nay chan thao tac, va lam mo phia sau thi
            // mat nguoi choi biet ngay la dang o trong mot hop.
            g.setColor(0, 0.6f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veKhung(g, o[0]);
            veTieuDe(g, o[0], o[1]);
            veTheDangMang(g, o[2]);
            veHangTien(g, o[3]);
            veHaiNutMo(g, o[4], o[5]);

            mFont.tahoma_7_grey.drawString(g, "Ná»i táº¡i cá»§a hÃ nh tinh báº¡n",
                    o[6][0], o[6][1], mFont.LEFT);

            veDanhSach(g, o[7]);
            veNutCuon(g, o[8], o[9], o[7]);
            veNutMoNhanh(g, o[10]);
        }

        private static void veKhung(mGraphics g, int[] k)
        {
            g.setColor(MAU_VIEN, 0.95f);
            g.fillRect(k[0] - 2, k[1] - 2, k[2] + 4, k[3] + 4, 10);
            g.setColor(MAU_NEN, 0.98f);
            g.fillRect(k[0], k[1], k[2], k[3], 9);
        }

        private static void veTieuDe(mGraphics g, int[] k, int[] nutX)
        {
            // Dai tieu de rieng mot mau, de phan biet voi than bang.
            g.setColor(MAU_NEN_2, 1f);
            g.fillRect(k[0] + 2, k[1] + 2, k[2] - 4, CAO_TIEU_DE - 4, 7);
            mFont.tahoma_7b_yellow.drawString(g, "Ná»I Táº I",
                    k[0] + k[2] / 2, k[1] + 4, mFont.CENTER);
            veNut(g, nutX, "X", MAU_O, true);
        }

        /// <summary>Tháº» "Äang mang": icon, tÃªn, vÃ  dÃ²ng mÃ´ táº£ tÃ¡c dá»¥ng.</summary>
        /// <remarks>
        /// TÃªn vÃ  mÃ´ táº£ tÃ¡ch lÃ m hai dÃ²ng riÃªng. Báº£n trÆ°á»c nhÃ©t cáº£ chuá»i mÃ¡y chá»§
        /// gá»­i vÃ o má»t dÃ²ng rá»i cáº¯t bá»t, nÃªn pháº§n <i>tÃ¡c dá»¥ng</i> â thá»© ÄÃ¡ng Äá»c
        /// nháº¥t â luÃ´n lÃ  pháº§n bá» cáº¯t máº¥t.
        /// </remarks>
        private void veTheDangMang(mGraphics g, int[] k)
        {
            g.setColor(MAU_O, 1f);
            g.fillRect(k[0], k[1], k[2], k[3], 5);
            g.setColor(MAU_VIEN, 0.5f);
            g.drawRect(k[0], k[1], k[2], k[3]);

            int oAnh = k[3] - 10;
            g.setColor(MAU_NEN, 1f);
            g.fillRect(k[0] + 5, k[1] + 5, oAnh, oAnh, 4);
            if (iconDangCo >= 0)
            {
                SmallImage.drawSmallImage(g, iconDangCo,
                        k[0] + 5 + oAnh / 2, k[1] + 5 + oAnh / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
            }

            int xChu = k[0] + oAnh + 12;
            int rongChu = k[0] + k[2] - xChu - 6;

            if (!coDuLieu)
            {
                mFont.tahoma_7_grey.drawString(g, "Äang táº£i...",
                        xChu, k[1] + 14, mFont.LEFT);
                return;
            }
            if (tenDangCo.Length == 0)
            {
                mFont.tahoma_7b_yellow.drawString(g, "ChÆ°a cÃ³ ná»i táº¡i",
                        xChu, k[1] + 7, mFont.LEFT);
                mFont.tahoma_7_grey.drawString(g, "Má» má»t cÃ¡i á» dÆ°á»i",
                        xChu, k[1] + 21, mFont.LEFT);
                return;
            }
            mFont.tahoma_7b_yellow.drawString(g,
                    catTheoBeRong(mFont.tahoma_7b_yellow, tenNgan(tenDangCo), rongChu),
                    xChu, k[1] + 7, mFont.LEFT);
            string tacDung = phanTacDung(tenDangCo);
            if (tacDung.Length > 0)
            {
                mFont.tahoma_7_white.drawString(g,
                        catTheoBeRong(mFont.tahoma_7_white, tacDung, rongChu),
                        xChu, k[1] + 21, mFont.LEFT);
            }
        }

        /// <summary>HÃ ng tiá»n: vÃ ng bÃªn trÃ¡i, ngá»c bÃªn pháº£i, khÃ´ng chá»ng nhau.</summary>
        private void veHangTien(mGraphics g, int[] k)
        {
            g.setColor(MAU_O_MO, 1f);
            g.fillRect(k[0], k[1], k[2], k[3], 4);
            mFont.tahoma_7b_yellow.drawString(g, "VÃ ng " + soCham(vangDangCo),
                    k[0] + 6, k[1] + 2, mFont.LEFT);
            mFont.tahoma_7b_green.drawString(g, "Ngá»c " + soCham(ngocDangCo),
                    k[0] + k[2] - 6, k[1] + 2, mFont.RIGHT);
        }

        /// <summary>
        /// Hai nÃºt má», giÃ¡ náº±m <b>trong</b> nÃºt.
        /// </summary>
        /// <remarks>
        /// Báº£n trÆ°á»c Äá» giÃ¡ á» má»t dÃ²ng chá»¯ xÃ¡m ngay <i>dÆ°á»i</i> nÃºt, vÃ  dÃ²ng áº¥y
        /// rÆ¡i ÄÃºng vÃ o chá» pháº§n tiáº¿p theo báº¯t Äáº§u â hai thá»© chá»ng lÃªn nhau. GiÃ¡
        /// lÃ  má»t pháº§n cá»§a cÃ¡i nÃºt, nÃªn nÃ³ thuá»c vá» bÃªn trong nÃºt.
        /// </remarks>
        private void veHaiNutMo(mGraphics g, int[] kv, int[] kn)
        {
            bool duVang = vangDangCo >= giaVang;
            bool duNgoc = ngocDangCo >= giaNgoc;

            veNutHaiDong(g, kv, "Má» báº±ng vÃ ng", soCham(giaVang),
                    duVang ? MAU_NUT_VANG : MAU_NUT_TAT);
            veNutHaiDong(g, kn, "Má» báº±ng ngá»c", giaNgoc + " ngá»c",
                    duNgoc ? MAU_NUT_NGOC : MAU_NUT_TAT);
        }

        private void veDanhSach(mGraphics g, int[] k)
        {
            g.setColor(MAU_O_MO, 1f);
            g.fillRect(k[0], k[1], k[2], k[3], 5);
            g.setColor(MAU_VIEN, 0.35f);
            g.drawRect(k[0], k[1], k[2], k[3]);

            if (!coDuLieu)
            {
                mFont.tahoma_7_grey.drawString(g, "Äang táº£i...",
                        k[0] + k[2] / 2, k[1] + k[3] / 2 - 6, mFont.CENTER);
                return;
            }
            if (danhSach.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "KhÃ´ng cÃ³ ná»i táº¡i nÃ o",
                        k[0] + k[2] / 2, k[1] + k[3] / 2 - 6, mFont.CENTER);
                return;
            }

            int het = Math.min(danhSach.Count, cuon + soDongThay);
            for (int i = cuon; i < het; i++)
            {
                Muc m = danhSach[i];
                int yd = k[1] + 2 + (i - cuon) * CAO_DONG;
                bool chon = (i == dongChon);

                g.setColor(chon ? MAU_O_CHON : MAU_O, chon ? 1f : 0.65f);
                g.fillRect(k[0] + 3, yd, k[2] - 6, CAO_DONG - 2, 4);

                int oAnh = CAO_DONG - 8;
                g.setColor(MAU_NEN, 1f);
                g.fillRect(k[0] + 6, yd + 3, oAnh, oAnh, 3);
                if (m.icon >= 0)
                {
                    SmallImage.drawSmallImage(g, m.icon,
                            k[0] + 6 + oAnh / 2, yd + 3 + oAnh / 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }

                int xChu = k[0] + oAnh + 12;
                int rongChu = k[0] + k[2] - xChu - 6;
                mFont f = chon ? mFont.tahoma_7b_white : mFont.tahoma_7_white;
                f.drawString(g, catTheoBeRong(f, tenNgan(m.moTa), rongChu),
                        xChu, yd + 2, mFont.LEFT);
                mFont.tahoma_7_grey.drawString(g,
                        catTheoBeRong(mFont.tahoma_7_grey, phanTacDung(m.moTa), rongChu),
                        xChu, yd + 12, mFont.LEFT);
            }
        }

        private void veNutCuon(mGraphics g, int[] len, int[] xuong, int[] ds)
        {
            if (danhSach.Count <= soDongThay)
            {
                return;
            }
            veNut(g, len, "LÃªn", MAU_O, cuon > 0);
            veNut(g, xuong, "Xuá»ng", MAU_O,
                    cuon < danhSach.Count - soDongThay);
            mFont.tahoma_7_grey.drawString(g,
                    (cuon + 1) + "-" + Math.min(danhSach.Count, cuon + soDongThay)
                    + " / " + danhSach.Count,
                    ds[0] + ds[2], len[1] + 2, mFont.RIGHT);
        }

        private void veNutMoNhanh(mGraphics g, int[] k)
        {
            bool coChon = (dongChon >= 0 && dongChon < danhSach.Count);
            if (!coChon)
            {
                g.setColor(MAU_O_MO, 1f);
                g.fillRect(k[0], k[1], k[2], k[3], 5);
                mFont.tahoma_7_grey.drawString(g,
                        "Cháº¡m má»t dÃ²ng á» trÃªn Äá» dÃ¹ng \"Má» nhanh\"",
                        k[0] + k[2] / 2, k[1] + (k[3] - 11) / 2, mFont.CENTER);
                return;
            }
            Muc m = danhSach[dongChon];
            veNut(g, k, "Má» NHANH â " + tenNgan(m.moTa)
                    + " (" + m.chiSoMin + "-" + m.chiSoMax + ")",
                    MAU_NHANH, true);
        }

        // ==================================================================
        //  Váº½ nÃºt
        // ==================================================================

        private static void veNut(mGraphics g, int[] k, string chu, int mau,
                bool sang)
        {
            g.setColor(sang ? mau : MAU_NUT_TAT, 1f);
            g.fillRect(k[0], k[1], k[2], k[3], 4);
            g.setColor(MAU_VIEN, sang ? 0.8f : 0.35f);
            g.drawRect(k[0], k[1], k[2], k[3]);
            mFont f = sang ? mFont.tahoma_7b_white : mFont.tahoma_7_grey;
            f.drawString(g, catTheoBeRong(f, chu, k[2] - 8),
                    k[0] + k[2] / 2, k[1] + (k[3] - 11) / 2, mFont.CENTER);
        }

        private static void veNutHaiDong(mGraphics g, int[] k, string tren,
                string duoi, int mau)
        {
            g.setColor(mau, 1f);
            g.fillRect(k[0], k[1], k[2], k[3], 4);
            g.setColor(MAU_VIEN, 0.8f);
            g.drawRect(k[0], k[1], k[2], k[3]);
            mFont.tahoma_7b_white.drawString(g,
                    catTheoBeRong(mFont.tahoma_7b_white, tren, k[2] - 6),
                    k[0] + k[2] / 2, k[1] + 3, mFont.CENTER);
            mFont.tahoma_7_white.drawString(g,
                    catTheoBeRong(mFont.tahoma_7_white, duoi, k[2] - 6),
                    k[0] + k[2] / 2, k[1] + 14, mFont.CENTER);
        }

        // ==================================================================
        //  Chá»¯
        // ==================================================================

        /// <summary>Pháº§n tÃªn, tá»©c chá»¯ trÆ°á»c dáº¥u "+" Äáº§u tiÃªn.</summary>
        /// <remarks>
        /// MÃ¡y chá»§ gá»­i cáº£ cÃ¢u, vÃ­ dá»¥ "ChiÃªu Äáº¥m Galick +5% Äáº¿n 25% sÃ¡t thÆ°Æ¡ng".
        /// TÃ¡ch ra Äá» tÃªn vÃ  tÃ¡c dá»¥ng náº±m hai dÃ²ng, thay vÃ¬ má»t dÃ²ng dÃ i rá»i bá»
        /// cáº¯t máº¥t ÄÃºng pháº§n tÃ¡c dá»¥ng.
        /// </remarks>
        private static string tenNgan(string s)
        {
            if (s == null)
            {
                return "";
            }
            int k = s.IndexOf('+');
            if (k <= 0)
            {
                k = s.IndexOf('[');
            }
            return (k > 0) ? s.Substring(0, k).Trim() : s.Trim();
        }

        /// <summary>Pháº§n tÃ¡c dá»¥ng, tá»©c pháº§n cÃ²n láº¡i sau tÃªn.</summary>
        private static string phanTacDung(string s)
        {
            if (s == null)
            {
                return "";
            }
            int k = s.IndexOf('+');
            if (k < 0)
            {
                k = s.IndexOf('[');
            }
            return (k >= 0 && k < s.Length) ? s.Substring(k).Trim() : "";
        }

        /// <summary>Cáº¯t chuá»i cho vá»«a Bá» Rá»NG, khÃ´ng pháº£i vá»«a sá» kÃ½ tá»±.</summary>
        /// <remarks>
        /// Äáº¿m kÃ½ tá»± thÃ¬ chá»¯ hoa vÃ  chá»¯ cÃ³ dáº¥u rá»ng hÆ¡n háº³n chá»¯ thÆ°á»ng, nÃªn cÃ¹ng
        /// má»t sá» kÃ½ tá»± cÃ³ chuá»i vá»«a khÃ­t, cÃ³ chuá»i trÃ n ra ngoÃ i khung. Äo báº±ng
        /// chÃ­nh phÃ´ng sáº¯p váº½ thÃ¬ khÃ´ng bao giá» trÃ n.
        /// </remarks>
        private static string catTheoBeRong(mFont f, string s, int rongToiDa)
        {
            if (s == null || s.Length == 0 || rongToiDa <= 0)
            {
                return "";
            }
            if (f.getWidth(s) <= rongToiDa)
            {
                return s;
            }
            int n = s.Length;
            while (n > 1 && f.getWidth(s.Substring(0, n) + "â¦") > rongToiDa)
            {
                n--;
            }
            return s.Substring(0, n) + "â¦";
        }

        /// <summary>Sá» nguyÃªn cÃ³ dáº¥u cháº¥m ngÄn nghÃ¬n.</summary>
        private static string soCham(long so)
        {
            string s = so.ToString();
            string ra = "";
            int dem = 0;
            for (int i = s.Length - 1; i >= 0; i--)
            {
                ra = s[i] + ra;
                if (++dem % 3 == 0 && i > 0)
                {
                    ra = "." + ra;
                }
            }
            return ra;
        }

        // ==================================================================
        //  Cháº¡m
        // ==================================================================

        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            int[][] o = oBang();
            cuonDanhSach(o[7]);

            // Nuot MOI cham khi dang mo, ke ca cham ra ngoai khung: bam xuyen
            // qua bang ma bang duoi no nhan duoc thi rat kho chiu.
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            if (daKeoXa)
            {
                // Vua vuot de cuon, khong phai bam chon. Khong chan cho nay thi
                // moi lan vuot xong lai chon trung dong nam duoi ngon.
                daKeoXa = false;
                return true;
            }

            if (cham(o[1]))
            {
                dong();
                return true;
            }
            if (cham(o[4]))
            {
                Service.gI().noiTaiMoBangVang();
                return true;
            }
            if (cham(o[5]))
            {
                Service.gI().noiTaiMoBangNgoc();
                return true;
            }

            int[] ds = o[7];
            int het = Math.min(danhSach.Count, cuon + soDongThay);
            for (int i = cuon; i < het; i++)
            {
                int yd = ds[1] + 2 + (i - cuon) * CAO_DONG;
                if (cham(ds[0] + 3, yd, ds[2] - 6, CAO_DONG - 2))
                {
                    dongChon = (dongChon == i) ? -1 : i;
                    return true;
                }
            }

            if (danhSach.Count > soDongThay)
            {
                if (cham(o[8]))
                {
                    cuon = Math.max(0, cuon - 1);
                    return true;
                }
                if (cham(o[9]))
                {
                    cuon = Math.min(danhSach.Count - soDongThay, cuon + 1);
                    return true;
                }
            }

            if (dongChon >= 0 && dongChon < danhSach.Count && cham(o[10]))
            {
                hoiChiSoMongMuon(danhSach[dongChon]);
                return true;
            }
            return true;
        }

        /// <summary>
        /// Há»i chá» sá» mong muá»n rá»i giao cho mÃ¡y chá»§ bá»c.
        /// </summary>
        /// <remarks>
        /// NÃ³i rÃµ khoáº£ng chá» sá» cá»§a chÃ­nh ná»i táº¡i ÄÃ³ ngay trÃªn há»p nháº­p: gÃµ má»t
        /// con sá» cao hÆ¡n tráº§n cá»§a nÃ³ thÃ¬ bá»c bao nhiÃªu láº§n cÅ©ng khÃ´ng ra, mÃ 
        /// ngÆ°á»i chÆ¡i khÃ´ng cÃ³ cÃ¡ch nÃ o biáº¿t tráº§n lÃ  bao nhiÃªu náº¿u khÃ´ng nÃ³i.
        /// </remarks>
        private void hoiChiSoMongMuon(Muc m)
        {
            HopNhapChu.getInstance().moRa(
                    "Má» nhanh â " + tenNgan(m.moTa) + "\n"
                    + "Chá» sá» ná»i táº¡i nÃ y tá»« " + m.chiSoMin + " Äáº¿n " + m.chiSoMax + "\n"
                    + "Má»i láº§n bá»c tá»n " + giaNgoc + " ngá»c, Äang cÃ³ " + soCham(ngocDangCo) + "\n"
                    + "GÃµ chá» sá» muá»n Äáº¡t (Äá» trá»ng = chá» cáº§n ÄÃºng ná»i táº¡i):",
                    "", 6, delegate (string s)
                    {
                        int muon = 0;
                        if (s != null)
                        {
                            string so = "";
                            for (int i = 0; i < s.Length; i++)
                            {
                                if (s[i] >= '0' && s[i] <= '9')
                                {
                                    so += s[i];
                                }
                            }
                            if (so.Length > 0 && so.Length < 9)
                            {
                                muon = int.Parse(so);
                            }
                        }
                        Service.gI().noiTaiMoNhanh(m.id, muon);
                    });
        }

        // ==================================================================
        //  Cuộn danh sách
        // ==================================================================

        /// <summary>Kéo bao nhiêu điểm thì coi là vuốt chứ không phải bấm chọn.</summary>
        private const int NGUONG_KEO = 6;

        /// <summary>Ma sát của quán tính: mỗi khung hình còn lại bấy nhiêu.</summary>
        /// <remarks>
        /// 0,90 cho quãng trôi chừng nửa giây ở 60 khung hình mỗi giây — đủ để
        /// lướt hết một danh sách dài bằng vài cú vuốt, không lâu tới mức phải
        /// chờ nó dừng mới bấm được vào dòng.
        /// </remarks>
        private const float MA_SAT_CUON = 0.90f;

        private bool dangKeo;

        /// <summary>Vừa vuốt để cuộn, nên lần nhả ngón này không tính là chọn.</summary>
        private bool daKeoXa;

        private int yMocKeo;

        /// <summary>Phần lẻ của dòng đang tích lại, khoảng -1 tới 1.</summary>
        private float duCuon;

        /// <summary>Vận tốc cuộn, tính bằng DÒNG mỗi khung hình.</summary>
        private float vanTocCuon;

        /// <summary>
        /// Lăn chuột và vuốt màn hình để cuộn danh sách nội tại.
        /// </summary>
        /// <remarks>
        /// Cùng một lối làm với hành trang, vì cùng một vấn đề: chia nguyên
        /// quãng ngón vừa đi cho chiều cao một dòng thì phải kéo trọn hai chục
        /// điểm danh sách mới nhúc nhích, rồi nó nhảy thẳng một dòng — đúng cảm
        /// giác "kéo mãi không đi rồi giật một cái". Ở đây phần lẻ được giữ lại
        /// ở <c>duCuon</c> nên danh sách chạy đều theo ngón, chỉ phần nguyên mới
        /// đổi thành số dòng, nhờ thế mọi công thức vẽ và bắt chạm theo dòng vẫn
        /// đúng y nguyên.
        /// </remarks>
        private void cuonDanhSach(int[] k)
        {
            int toiDa = danhSach.Count - soDongThay;
            if (toiDa <= 0)
            {
                cuon = 0;
                dangKeo = false;
                duCuon = 0f;
                vanTocCuon = 0f;
                return;
            }
            if (GameCanvas.pXYScrollMouse != 0
                    && trongVung(k, GameCanvas.pxMouse, GameCanvas.pyMouse))
            {
                cuon += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
            }
            if (GameCanvas.isPointerDown)
            {
                if (!dangKeo)
                {
                    if (!trongVung(k, GameCanvas.pxFirst, GameCanvas.pyFirst))
                    {
                        return;
                    }
                    dangKeo = true;
                    daKeoXa = false;
                    yMocKeo = GameCanvas.pyFirst;
                    duCuon = 0f;
                    vanTocCuon = 0f;
                }
                if (Math.abs(GameCanvas.py - GameCanvas.pyFirst) > NGUONG_KEO)
                {
                    daKeoXa = true;
                }
                int dy = GameCanvas.py - yMocKeo;
                if (dy != 0)
                {
                    yMocKeo = GameCanvas.py;
                    float dong = (float) dy / CAO_DONG;
                    duCuon -= dong;
                    // Van toc lay trung binh truot, khong lay rieng quang cuoi:
                    // mot cai giat nho luc nha ngon khong duoc quyet dinh ca cu
                    // quan tinh.
                    vanTocCuon = vanTocCuon * 0.55f - dong * 0.45f;
                    apDuCuon();
                }
            }
            else
            {
                dangKeo = false;
                if (vanTocCuon > 0.01f || vanTocCuon < -0.01f)
                {
                    duCuon += vanTocCuon;
                    apDuCuon();
                    vanTocCuon *= MA_SAT_CUON;
                }
                else
                {
                    vanTocCuon = 0f;
                    duCuon = 0f;
                }
            }
            int truoc = cuon;
            if (cuon < 0)
            {
                cuon = 0;
            }
            if (cuon > toiDa)
            {
                cuon = toiDa;
            }
            if (cuon != truoc)
            {
                // Cham bien: dung han, dung de van toc day mai vao tuong.
                vanTocCuon = 0f;
                duCuon = 0f;
            }
        }

        /// <summary>Chuyển phần NGUYÊN của <c>duCuon</c> thành số dòng đã cuộn.</summary>
        /// <remarks>
        /// Giữ lại phần lẻ. Ép kiểu <c>int</c> là làm tròn về 0 chứ không làm
        /// tròn gần nhất: làm tròn gần nhất thì một cú chạm rung tay nhích nửa
        /// dòng cũng đủ đẩy danh sách đi một dòng.
        /// </remarks>
        private void apDuCuon()
        {
            int buoc = (int) duCuon;
            if (buoc != 0)
            {
                cuon += buoc;
                duCuon -= buoc;
            }
        }

        private static bool trongVung(int[] k, int x, int y)
        {
            return x >= k[0] && x <= k[0] + k[2]
                    && y >= k[1] && y <= k[1] + k[3];
        }

        private static bool cham(int[] k)
        {
            return cham(k[0], k[1], k[2], k[3]);
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
