// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game2.God
{
    /// <summary>
    /// Thẻ <b>Đập Saibaman</b> của bảng Trò Chơi.
    /// </summary>
    /// <remarks>
    /// <para>Mỗi người một ván riêng: bấm Bắt đầu là mua vé, máy chủ gửi xuống
    /// <b>cả lịch trồi</b> của ván (con nào, hố nào, lúc nào, bao lâu). Màn này
    /// chỉ phát lại lịch đó theo đồng hồ máy và ghi lại cú đập trúng; hết giờ
    /// thì gửi danh sách cú đập lên — máy chủ tự chấm điểm và phát quà.</para>
    ///
    /// <para>Saibaman và Bulma vẽ bằng <b>chính bộ phần hình trong game</b>
    /// (đầu/thân/chân của boss Saibamen và NPC Bulma), không cần ảnh riêng.</para>
    ///
    /// <para><b>Bố cục:</b> sân chơi bên trái là thứ duy nhất có màu mạnh; bảng
    /// mốc quà bên phải là một thẻ giấy phẳng, đọc như một danh sách. Không lặp
    /// tên trò trong sân (tab đã nói), không chữ trang trí.</para>
    /// </remarks>
    public partial class TroChoiUI
    {
        private const int THE_DAP_SAIBAMAN = 3;

        private static readonly string[] TEN_THE_2_SB = {
            "Chơi", "Lịch sử của tôi", "Top hôm nay"
        };

        /// <summary>Tên ba mục tầng 2 của trò đang mở.</summary>
        private string[] tenThe2()
        {
            return the == THE_DAP_SAIBAMAN ? TEN_THE_2_SB : TEN_THE_2;
        }

        /// <summary>Trò có dải thẻ tầng 2 (Đặt cược / Lịch sử ...).</summary>
        private bool coThe2()
        {
            return the == THE_TAI_XIU || the == THE_DUA_NGUA || the == THE_DAP_SAIBAMAN;
        }

        // ------------------------------------------------------------------
        //  Phần hình
        // ------------------------------------------------------------------

        /// <summary>Đầu, thân, chân của boss Saibamen (xem Saibamen.java).</summary>
        private const int SB_DAU = 642;
        private const int SB_THAN = 643;
        private const int SB_CHAN = 644;

        /// <summary>Mẫu NPC Bulma (ConstNpc.BUNMA).</summary>
        private const int SB_NPC_BULMA = 7;

        private const int SB_LOAI_THUONG = 0;
        private const int SB_LOAI_VANG = 1;
        private const int SB_LOAI_BULMA = 2;

        // ------------------------------------------------------------------
        //  Màu của sân — chỉ dùng trong sân
        // ------------------------------------------------------------------
        private static readonly int SB_CO = rgb(0x84, 0xC0, 0x5A);
        private static readonly int SB_CO_SOC = rgb(0x7A, 0xB6, 0x51);
        private static readonly int SB_DAT = rgb(0x8B, 0x5E, 0x36);
        private static readonly int SB_DAT_SANG = rgb(0xA9, 0x7A, 0x4B);
        private static readonly int SB_LO = rgb(0x26, 0x17, 0x0E);
        private static readonly int SB_DO = rgb(0xD0, 0x43, 0x3A);

        // ------------------------------------------------------------------
        //  Trạng thái vẽ
        // ------------------------------------------------------------------

        private class SbHieuUng
        {
            public int x;
            public int y;
            public string chu;
            /// <summary>0 cộng điểm, 1 điểm vàng, 2 trừ điểm, 3 hụt.</summary>
            public int loai;
            public long luc;
        }

        private readonly List<SbHieuUng> sbHieuUng = new List<SbHieuUng>();

        /// <summary>Lúc lỡ đập Bulma — rung sân và loé đỏ.</summary>
        private long sbMocPhat;

        /// <summary>Khung trước ngón đã chạm chưa, để bắt đúng cú CHẠM XUỐNG.</summary>
        private bool sbDownTruoc;

        /// <summary>Sân chơi: x, y, w, h.</summary>
        private int[] sbOSan = new int[0];

        /// <summary>Từng hố: tâm x, tâm y, rộng miệng, cao miệng, x ô, đỉnh ô, rộng ô, cao ô.</summary>
        private readonly int[][] sbOHo = new int[9][];

        private int[] sbONutBatDau = new int[0];
        private int[] sbONutChoiLai = new int[0];
        private int[] sbONutDongKQ = new int[0];
        private readonly List<int[]> sbODongMoc = new List<int[]>();

        /// <summary>Đã tắt bảng kết quả (bấm Đóng).</summary>
        private bool sbDaDongKQ;

        /// <summary>Máy chủ gửi lịch ván mới.</summary>
        public void sbVanMoi()
        {
            sbHieuUng.Clear();
            sbDaDongKQ = false;
            sbMocPhat = 0L;
        }

        private static int sbMsTrongVan()
        {
            God.MiniGame m = God.MiniGame.gI();
            return (int) (mSystem.currentTimeMillis() - m.sbMocBatDau);
        }

        /// <summary>
        /// Chạy mỗi khung hình, kể cả khi bảng đóng: hết giờ thì báo kết quả.
        /// </summary>
        private void sbCapNhat()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (m.sbDangChoi && sbMsTrongVan() >= m.sbDaiMs + 200)
            {
                m.sbDangChoi = false;
                m.sbChoKetQua = true;
                m.sbMocBaoKetQua = mSystem.currentTimeMillis();
                m.sbBaoKetQua();
            }
            if (m.sbChoKetQua && mSystem.currentTimeMillis() - m.sbMocBaoKetQua > 30000L)
            {
                // Mat goi ket qua (rot mang): thoi cho, xin lai trang thai.
                m.sbChoKetQua = false;
                God.MiniGame.gI().moBang(God.MiniGame.TRO_DAP_SAIBAMAN);
            }
        }

        private static bool sbDangBan()
        {
            God.MiniGame m = God.MiniGame.gI();
            return m.sbDangChoi || m.sbChoKetQua;
        }

        /// <summary>Đang hiện bảng kết quả trên sân.</summary>
        private bool sbHienKQ()
        {
            God.MiniGame m = God.MiniGame.gI();
            return !m.sbDangChoi && !m.sbChoKetQua && m.sbCoKetQua && !sbDaDongKQ;
        }

        // ==================================================================
        //  Vẽ — màn chơi
        // ==================================================================

        private void veDapSaibaman(mGraphics g)
        {
            sbCapNhat();
            int x = x0 + LE;
            int w = rong - LE * 2;
            int yTren = yNoiDung();
            int yDuoi = y0 + cao - LE;

            const int CAO_DAU = 20;
            veDauSb(g, x, yTren, w);

            int yThan = yTren + CAO_DAU + 4;
            int hThan = yDuoi - yThan;
            int rongBang = w * 33 / 100;
            if (rongBang < 136)
            {
                rongBang = 136;
            }
            if (rongBang > 184)
            {
                rongBang = 184;
            }
            int rongSan = w - rongBang - 8;
            veSanSb(g, x, yThan, rongSan, hThan);
            veBangMocSb(g, x + rongSan + 8, yThan, rongBang, hThan);
        }

        // ---- hàng thông tin ----

        private void veDauSb(mGraphics g, int x, int y, int w)
        {
            God.MiniGame m = God.MiniGame.gI();
            mFont.tahoma_7_grey.drawString(g, "Hôm nay", x, y + 2, mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g, m.sbDaChoi + "/" + m.sbLuotNgay + " ván",
                    x + mFont.tahoma_7_grey.getWidth("Hôm nay") + 4, y + 2, mFont.LEFT);

            string nhan;
            int mau;
            if (m.sbDangChoi)
            {
                int con = (m.sbDaiMs - sbMsTrongVan() + 999) / 1000;
                if (con < 0)
                {
                    con = 0;
                }
                nhan = "Còn  00:" + (con < 10 ? "0" : string.Empty) + con;
                mau = con <= 5 ? MAU_XIU_SANG : MAU_XANH_LA;
            }
            else if (m.sbChoKetQua)
            {
                nhan = "Đang chấm điểm";
                mau = MAU_VANG;
            }
            else
            {
                nhan = "Vé " + m.sbVe + " thỏi  ·  " + m.sbGiay + " giây";
                mau = MAU_VANG;
            }
            int rongDH = 164;
            veTheDem(g, x + w / 2 - rongDH / 2, y, rongDH, nhan, mau);

            // Diem cao nhat trong bang xep hang hom nay, neu da tai.
            if (m.sbTop.Count > 0)
            {
                string cao = m.sbTop[0].diem + string.Empty;
                mFont.tahoma_7b_dark.drawString(g, cao, x + w, y + 2, mFont.RIGHT);
                mFont.tahoma_7_grey.drawString(g, "Cao nhất hôm nay",
                        x + w - mFont.tahoma_7b_dark.getWidth(cao) - 4, y + 2, mFont.RIGHT);
            }
        }

        // ---- sân ----

        private void veSanSb(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            long bayGio = mSystem.currentTimeMillis();

            // Rung san khi lo dap Bulma.
            int rung = 0;
            long tuPhat = bayGio - sbMocPhat;
            if (tuPhat < 260L)
            {
                rung = (int) (3 * UnityEngine.Mathf.Sin(tuPhat / 18f));
            }
            int xs = x + rung;
            sbOSan = new int[] { x, y, w, h };

            // Khung go hai lop, trong la tranh bai co ve san (ngon co, soc cat,
            // hoa nho, vien toi). Thieu tranh thi to co phang co soc.
            g.setColor(0x000000, 0.20f);
            g.fillRect(xs, y + 3, w, h, 10);
            g.setColor(rgb(0x5B, 0x3A, 0x1C), 1f);
            g.fillRect(xs, y, w, h, 10);
            g.setColor(rgb(0xB9, 0x86, 0x4E), 1f);
            g.fillRect(xs + 2, y + 2, w - 4, h - 4, 9);
            g.setClip(xs + 4, y + 4, w - 8, h - 8);
            if (!veTranh(g, "sb_san", xs + 4, y + 4, w - 8, h - 8))
            {
                g.setColor(SB_CO, 1f);
                g.fillRect(xs + 4, y + 4, w - 8, h - 8);
                const int SOC = 26;
                g.setColor(SB_CO_SOC, 1f);
                for (int k = 1; k * SOC < w; k += 2)
                {
                    g.fillRect(xs + 4 + k * SOC, y + 4, SOC, h - 8);
                }
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            // Luoi 3 x 3 ho — tinh vua het chieu cao, hang duoi khong bi cat.
            const int DINH = 30;
            int rongO = (w - 16) / 3;
            int caoO = (h - DINH - 10) / 3;
            int rongMieng = Math.min(rongO * 60 / 100, 56);
            int caoMieng = Math.max(10, rongMieng * 32 / 100);
            for (int i = 0; i < 9; i++)
            {
                int cot = i % 3;
                int hang = i / 3;
                int xo = xs + 8 + cot * rongO;
                int yo = y + DINH + hang * caoO;
                int tamX = xo + rongO / 2;
                int tamY = yo + caoO * 70 / 100;
                sbOHo[i] = new int[] { tamX, tamY, rongMieng, caoMieng, xo, yo, rongO, caoO };
            }

            int ms = sbMsTrongVan();
            for (int i = 0; i < 9; i++)
            {
                veHoSb(g, i, ms, bayGio);
            }

            // Thanh HUD tren dinh san: diem ben trai, thoi gian ben phai.
            if (m.sbDangChoi || m.sbChoKetQua)
            {
                veHuyHieuDiemSb(g, xs + 8, y + 7, m.sbDiemTam());
                float tiLe = m.sbDangChoi ? 1f - (float) ms / m.sbDaiMs : 0f;
                if (tiLe < 0f)
                {
                    tiLe = 0f;
                }
                int wBar = w / 3;
                int xBar = xs + w - 8 - wBar;
                g.setColor(0x000000, 0.25f);
                g.fillRect(xBar, y + 13, wBar, 5, 2);
                bool gap = m.sbDangChoi && m.sbDaiMs - ms <= 5000;
                g.setColor(gap ? SB_DO : 0xFFFFFF, 1f);
                g.fillRect(xBar, y + 13, (int) (wBar * tiLe), 5, 2);
            }

            veHieuUngSb(g, bayGio);

            // Loe do khi dap Bulma.
            if (tuPhat < 260L)
            {
                g.setColor(SB_DO, 0.22f * (1f - tuPhat / 260f));
                g.fillRect(xs + 1, y + 1, w - 2, h - 2, BO_GOC);
            }

            if (m.sbDangChoi && ms < 900)
            {
                veThongDiepSb(g, xs, y, w, h, "Bắt đầu!", 1f - ms / 900f);
            }
            else if (m.sbChoKetQua)
            {
                veThongDiepSb(g, xs, y, w, h, "Đang chấm điểm...", 1f);
            }
            else if (sbHienKQ())
            {
                veKetQuaSb(g, xs, y, w, h);
            }
            else if (!m.sbDangChoi)
            {
                veLoiMoiSb(g, xs, y, w, h);
            }
        }

        /// <summary>Điểm đang có: nhãn nhỏ và con số to trên nền tối trong.</summary>
        private static void veHuyHieuDiemSb(mGraphics g, int x, int y, int diem)
        {
            string so = diem + string.Empty;
            int rongSo = mFont.bigNumber_yellow.getWidth(so);
            int rong = rongSo + 42;
            g.setColor(0x000000, 0.35f);
            g.fillRect(x, y, rong, 18, 9);
            mFont.tahoma_7_white.drawString(g, "Điểm", x + 8, y + 3, mFont.LEFT);
            mFont.bigNumber_yellow.drawString(g, so, x + 36, y + 2, mFont.LEFT);
        }

        /// <summary>Một hố: ụ đất, miệng hố, con đang trồi, mép trước.</summary>
        private void veHoSb(mGraphics g, int i, int ms, long bayGio)
        {
            God.MiniGame m = God.MiniGame.gI();
            int[] o = sbOHo[i];
            int tamX = o[0];
            int tamY = o[1];
            int rm = o[2];
            int cm = o[3];

            // U dat + mieng ho: tranh ve san (soi, go sang, long ho sau dan).
            // Mieng ho trong tranh rong 208/320 be ngang anh, tam o giua anh.
            int wHo = rm * 320 / 208;
            int hHo = wHo * 150 / 320;
            if (!veTranh(g, "sb_ho", tamX - wHo / 2, tamY - hHo / 2, wHo, hHo))
            {
                g.setColor(SB_DAT, 1f);
                g.fillRect(tamX - rm / 2 - 6, tamY - cm / 2 - 3, rm + 12, cm + 7, (cm + 7) / 2);
                g.setColor(SB_LO, 1f);
                g.fillRect(tamX - rm / 2, tamY - cm / 2, rm, cm, cm / 2);
            }

            // Con dang trong ho nay (neu co).
            int loai = -1;
            float troi = 0f;
            int lech = 0;
            if (sbDangBan())
            {
                for (int k = 0; k < m.sbT.Length; k++)
                {
                    if (m.sbHo[k] != i)
                    {
                        continue;
                    }
                    float r = sbDoTroi(k, ms);
                    if (r > 0f)
                    {
                        loai = m.sbLoai[k];
                        troi = r;
                        if (m.sbLucDap[k] >= 0)
                        {
                            // Bi dap: lac ngang khi chui xuong.
                            lech = (int) (3 * UnityEngine.Mathf.Sin((ms - m.sbLucDap[k]) / 14f));
                        }
                        break;
                    }
                }
            }
            else if (!sbHienKQ() && i >= 3 && i <= 5)
            {
                // Man cho: hang giua bay san ba loai de nguoi choi nhin mat.
                loai = i - 3;
                troi = 1f;
            }

            if (loai >= 0)
            {
                const int CAO_NV = 54;
                int yChan = tamY + 8 + (int) ((1f - troi) * CAO_NV);
                int dir = (i % 3 == 2) ? -1 : 1;
                g.setClip(o[4], o[5] - 8, o[6], tamY - o[5] + 9);
                if (loai == SB_LOAI_VANG)
                {
                    // Saibaman vang: quang sang vang sau lung, dap theo nhip.
                    float nhipV = 0.6f + 0.4f * UnityEngine.Mathf.Sin(bayGio / 140f);
                    if (!veTranhMo(g, "tc_quang_vang", tamX - 30, yChan - 58, 60, 60, nhipV * troi))
                    {
                        g.setColor(MAU_VANG, 0.55f * nhipV * troi);
                        g.fillRect(tamX - 20, yChan - 48, 40, 40, 20);
                    }
                }
                int cf = (int) (bayGio / 320L % 2);
                bool daVe;
                if (loai == SB_LOAI_BULMA)
                {
                    daVe = veBulmaSb(g, tamX + lech, yChan, dir, cf);
                }
                else
                {
                    daVe = veNhanVatPhan(g, SB_DAU, SB_THAN, SB_CHAN, tamX + lech, yChan, dir, cf);
                }
                if (!daVe)
                {
                    veConThaySb(g, tamX + lech, yChan, loai);
                }
                g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
                if (loai == SB_LOAI_VANG && troi > 0.6f && sbDangBan())
                {
                    veHatSang(g, tamX, yChan - 30, 5, 22, MAU_VANG, 0.8f, 1100);
                }
            }


            // Nhan diem o man cho.
            if (!sbDangBan() && !sbHienKQ() && i >= 3 && i <= 5)
            {
                int d = m.sbDiemLoai(i - 3);
                string chu = (d >= 0 ? "+" : string.Empty) + d;
                int rc = mFont.tahoma_7b_white.getWidth(chu) + 12;
                int yc = tamY + cm / 2 + 6;
                g.setColor(i == 5 ? SB_DO : (i == 4 ? rgb(0xC8, 0x8A, 0x1E) : MAU_XANH_LA), 1f);
                g.fillRect(tamX - rc / 2, yc, rc, 13, 6);
                mFont.tahoma_7b_white.drawString(g, chu, tamX, yc + 1, mFont.CENTER);
            }
        }

        /// <summary>
        /// Con thứ <paramref name="k"/> đang trồi bao nhiêu (0 là khuất hẳn, 1 là lên hết).
        /// </summary>
        private static float sbDoTroi(int k, int ms)
        {
            God.MiniGame m = God.MiniGame.gI();
            const float LEN = 140f;
            const float XUONG = 150f;
            int pha = ms - m.sbT[k];
            if (pha < 0)
            {
                return 0f;
            }
            if (m.sbLucDap[k] >= 0)
            {
                float d = ms - m.sbLucDap[k];
                float r = 1f - d / 140f;
                return r < 0f ? 0f : r;
            }
            int song = m.sbSong[k];
            if (pha < LEN)
            {
                return pha / LEN;
            }
            if (pha <= song)
            {
                return 1f;
            }
            float c = 1f - (pha - song) / XUONG;
            return c < 0f ? 0f : c;
        }

        /// <summary>
        /// Vẽ một nhân vật theo bộ phần hình (đầu, thân, chân), chân đặt ở <paramref name="cy"/>.
        /// </summary>
        /// <remarks>
        /// Chép đúng nhánh vẽ thường của <c>Char.paintCharBody</c>: đầu, chân rồi
        /// thân, độ lệch lấy từ <c>Char.CharInfo</c>. Thiếu phần hình (dữ liệu
        /// chưa tải) thì trả <c>false</c> để bên gọi vẽ hình thay.
        /// </remarks>
        private static bool veNhanVatPhan(mGraphics g, int dau, int than, int chan, int cx,
                int cy, int dir, int cf)
        {
            Part[] ps = GameScr.parts;
            if (ps == null || dau < 0 || than < 0 || chan < 0
                    || dau >= ps.Length || than >= ps.Length || chan >= ps.Length)
            {
                return false;
            }
            Part ph = ps[dau];
            Part pb = ps[than];
            Part pl = ps[chan];
            if (ph == null || pb == null || pl == null || ph.pi == null || pb.pi == null
                    || pl.pi == null)
            {
                return false;
            }
            int[][] ci = Char.CharInfo[cf];
            int kd = ci[0][0];
            int kc = ci[1][0];
            int kt = ci[2][0];
            if (kd >= ph.pi.Length || kc >= pl.pi.Length || kt >= pb.pi.Length
                    || ph.pi[kd] == null || pl.pi[kc] == null || pb.pi[kt] == null)
            {
                return false;
            }
            int bienDoi = dir == 1 ? 0 : 2;
            int neo = dir == 1 ? 0 : 24;
            int huong = dir == 1 ? 1 : -1;
            SmallImage.drawSmallImage(g, ph.pi[kd].id, cx + (ci[0][1] + ph.pi[kd].dx) * huong,
                    cy - ci[0][2] + ph.pi[kd].dy, bienDoi, neo);
            SmallImage.drawSmallImage(g, pl.pi[kc].id, cx + (ci[1][1] + pl.pi[kc].dx) * huong,
                    cy - ci[1][2] + pl.pi[kc].dy, bienDoi, neo);
            SmallImage.drawSmallImage(g, pb.pi[kt].id, cx + (ci[2][1] + pb.pi[kt].dx) * huong,
                    cy - ci[2][2] + pb.pi[kt].dy, bienDoi, neo);
            return true;
        }

        private static bool veBulmaSb(mGraphics g, int cx, int cy, int dir, int cf)
        {
            NpcTemplate[] ds = Npc.arrNpcTemplate;
            if (ds == null || SB_NPC_BULMA >= ds.Length || ds[SB_NPC_BULMA] == null)
            {
                return false;
            }
            NpcTemplate t = ds[SB_NPC_BULMA];
            return veNhanVatPhan(g, t.headId, t.bodyId, t.legId, cx, cy, dir, cf);
        }

        /// <summary>Hình thay khi phần hình chưa tải: một cái đầu tròn có mắt.</summary>
        private static void veConThaySb(mGraphics g, int cx, int cy, int loai)
        {
            int mau = loai == SB_LOAI_BULMA ? rgb(0x4F, 0xB8, 0xC8)
                    : (loai == SB_LOAI_VANG ? MAU_VANG : rgb(0x6C, 0xB8, 0x48));
            g.setColor(0x000000, 0.4f);
            g.fillRect(cx - 14, cy - 40, 28, 30, 12);
            g.setColor(mau, 1f);
            g.fillRect(cx - 13, cy - 41, 26, 28, 12);
            g.setColor(loai == SB_LOAI_BULMA ? 0x223355 : 0xC81E1E, 1f);
            g.fillRect(cx - 7, cy - 32, 4, 4, 2);
            g.fillRect(cx + 3, cy - 32, 4, 4, 2);
        }

        /// <summary>Chữ điểm bay lên sau cú đập và vòng va chạm ngắn.</summary>
        private void veHieuUngSb(mGraphics g, long bayGio)
        {
            for (int i = sbHieuUng.Count - 1; i >= 0; i--)
            {
                SbHieuUng h = sbHieuUng[i];
                long d = bayGio - h.luc;
                if (d > 700L)
                {
                    sbHieuUng.RemoveAt(i);
                    continue;
                }
                float t = d / 700f;
                if (d < 220L && h.loai != 3)
                {
                    // Chop no hinh sao no to roi mo di, bua go dap xuong trong
                    // 160ms dau. Do la chop do khi lo dap Bulma.
                    float u = d / 220f;
                    int co = 26 + (int) (18 * u);
                    if (!veTranhMo(g, h.loai == 2 ? "sb_no_do" : "sb_no", h.x - co / 2, h.y - co / 2,
                            co, co, 1f - u * 0.8f))
                    {
                        g.setColor(h.loai == 2 ? SB_DO : 0xFFFFFF, 0.5f * (1f - u));
                        g.fillRect(h.x - co / 2, h.y - co / 2, co, co, co / 2);
                    }
                    if (d < 160L)
                    {
                        int lech = (int) (6 * (1f - d / 160f));
                        veTranh(g, "sb_bua", h.x + 4 + lech, h.y - 34 - lech, 40, 40);
                    }
                }
                else if (h.loai == 3 && d < 180L)
                {
                    // Hut: mot cham bui nho.
                    float u = d / 180f;
                    g.setColor(SB_DAT, 0.5f * (1f - u));
                    int co = 6 + (int) (8 * u);
                    g.fillRect(h.x - co / 2, h.y - co / 2, co, co, co / 2);
                }
                if (h.chu == null)
                {
                    continue;
                }
                // Chu diem bay len phia tren, tach khoi bua va chop no.
                int yc = h.y - 30 - (int) (24 * t);
                mFont f = h.loai == 2 ? mFont.tahoma_7b_red
                        : (h.loai == 1 ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_white);
                veChuNoi(g, f, h.chu, h.x, yc, mFont.CENTER);
            }
        }

        /// <summary>Một thông điệp ngắn giữa sân, trên viên mực.</summary>
        private static void veThongDiepSb(mGraphics g, int x, int y, int w, int h, string chu,
                float a)
        {
            if (a <= 0f)
            {
                return;
            }
            int rb = mFont.tahoma_7b_white.getWidth(chu) + 28;
            int xb = x + (w - rb) / 2;
            int yb = y + h / 2 - 11;
            g.setColor(MAU_TIEU_DE, 0.9f * a);
            g.fillRect(xb, yb, rb, 22, 11);
            mFont.tahoma_7b_white.drawString(g, chu, xb + rb / 2, yb + 5, mFont.CENTER);
        }

        /// <summary>Màn chờ: ba con bày ở hàng giữa, một dòng hướng dẫn dưới đáy.</summary>
        private void veLoiMoiSb(mGraphics g, int x, int y, int w, int h)
        {
            string luat = "Chạm để đập  ·  tránh Bulma";
            int rl = mFont.tahoma_7_white.getWidth(luat) + 20;
            int yl = y + 8;
            g.setColor(0x000000, 0.35f);
            g.fillRect(x + (w - rl) / 2, yl, rl, 16, 8);
            mFont.tahoma_7_white.drawString(g, luat, x + w / 2, yl + 2, mFont.CENTER);
        }

        /// <summary>Bảng kết quả giữa sân: điểm to, mốc đạt, quà nhận.</summary>
        private void veKetQuaSb(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            long bayGio = mSystem.currentTimeMillis();
            int rb = Math.min(236, w - 16);
            int cb = Math.min(138, h - 12);
            int xb = x + (w - rb) / 2;
            int yb = y + (h - cb) / 2;
            bool dat = m.sbMocKQ >= 0;

            g.setColor(0x000000, 0.30f);
            g.fillRect(x + 1, y + 1, w - 2, h - 2, BO_GOC);
            veKhungKep(g, xb, yb, rb, cb, MAU_THE, MAU_VIEN, 0.7f);
            g.setClip(xb + 1, yb + 1, rb - 2, 4);
            g.setColor(dat ? MAU_XANH_LA : MAU_VIEN, 1f);
            g.fillRect(xb + 1, yb + 1, rb - 2, 12, BO_GOC);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            mFont.tahoma_7_grey.drawString(g, "Kết thúc ván", xb + rb / 2, yb + 11, mFont.CENTER);
            string so = m.sbDiemKQ + string.Empty;
            int wSo = mFont.bigNumber_yellow.getWidth(so);
            int wCum = wSo + 4 + mFont.tahoma_7b_dark.getWidth("điểm");
            int xCum = xb + rb / 2 - wCum / 2;
            mFont.bigNumber_yellow.drawString(g, so, xCum, yb + 25, mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g, "điểm", xCum + wSo + 4, yb + 28, mFont.LEFT);

            int yMoc = yb + 46;
            if (dat && m.sbMocKQ < m.sbMoc.Count)
            {
                mFont.tahoma_7b_green.drawString(g, "Đạt mốc " + m.sbMoc[m.sbMocKQ].diem,
                        xb + rb / 2, yMoc, mFont.CENTER);
                veHangQuaSb(g, m.sbQuaKQ, xb + 10, yMoc + 15, rb - 20, 24);
                if (bayGio - m.sbMocKetQua < 1500L)
                {
                    veHatSang(g, xb + rb / 2, yb + 34, 8, 44, MAU_VANG, 0.6f, 1300);
                }
            }
            else
            {
                mFont.tahoma_7b_dark.drawString(g, "Chưa tới mốc quà", xb + rb / 2, yMoc,
                        mFont.CENTER);
                string ke = sbMocKe(m.sbDiemKQ);
                if (ke != null)
                {
                    mFont.tahoma_7_grey.drawString(g, ke, xb + rb / 2, yMoc + 14, mFont.CENTER);
                }
            }

            int yn = yb + cb - 30;
            int rn = (rb - 26) / 2;
            sbONutChoiLai = new int[] { xb + 10, yn, rn, 22 };
            sbONutDongKQ = new int[] { xb + 16 + rn, yn, rn, 22 };
            if (sbChoiDuoc() == null)
            {
                veNutVang(g, sbONutChoiLai, "Chơi lại");
            }
            else
            {
                veNutChinh(g, sbONutChoiLai, "Chơi lại", false, MAU_CHON);
            }
            veNutPhu(g, sbONutDongKQ, "Đóng", true);
        }

        /// <summary>"Còn N điểm nữa tới mốc M" — hoặc null nếu đã quá mốc cao nhất.</summary>
        private static string sbMocKe(int diem)
        {
            God.MiniGame m = God.MiniGame.gI();
            for (int i = 0; i < m.sbMoc.Count; i++)
            {
                if (diem < m.sbMoc[i].diem)
                {
                    return "Thiếu " + (m.sbMoc[i].diem - diem) + " điểm để tới mốc "
                            + m.sbMoc[i].diem;
                }
            }
            return null;
        }

        /// <summary>Một hàng ô quà, căn giữa.</summary>
        private static void veHangQuaSb(mGraphics g, List<God.MiniGame.SbQua> ds, int x, int y,
                int w, int co)
        {
            int n = ds.Count;
            if (n == 0)
            {
                return;
            }
            int buoc = co + 4;
            int tong = n * buoc - 4;
            int xd = x + (w - tong) / 2;
            for (int i = 0; i < n; i++)
            {
                veOQuaSb(g, xd + i * buoc, y, co, ds[i]);
            }
        }

        /// <summary>Một ô quà: rãnh chìm, icon giữa, số lượng góc dưới phải.</summary>
        private static void veOQuaSb(mGraphics g, int x, int y, int co, God.MiniGame.SbQua q)
        {
            g.setColor(MAU_LOM, 1f);
            g.fillRect(x, y, co, co, 4);
            if (q.icon >= 0)
            {
                g.setClip(x, y, co, co);
                SmallImage.drawSmallImage(g, q.icon, x + co / 2, y + co / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
                g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            }
            if (q.soLuong > 1)
            {
                string sl = "x" + q.soLuong;
                int rs = mFont.tahoma_7_white.getWidth(sl) + 4;
                g.setColor(MAU_TIEU_DE, 0.85f);
                g.fillRect(x + co - rs, y + co - 10, rs, 10, 3);
                mFont.tahoma_7_white.drawString(g, sl, x + co - 2, y + co - 11, mFont.RIGHT);
            }
        }

        /// <summary>Lý do chưa chơi được, hoặc null nếu chơi được.</summary>
        private static string sbChoiDuoc()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (sbDangBan())
            {
                return m.sbDangChoi ? "Đang chơi..." : "Đang chấm...";
            }
            if (m.sbDaChoi >= m.sbLuotNgay)
            {
                return "Hết lượt hôm nay";
            }
            if (m.tongThoi < m.sbVe)
            {
                return "Thiếu thỏi vàng";
            }
            return null;
        }

        // ---- bảng mốc quà ----

        private void veBangMocSb(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            veKhungKep(g, x, y, w, h, MAU_THE, MAU_VIEN, 0.55f);

            mFont.tahoma_7b_dark.drawString(g, "Mốc quà", x + 10, y + 8, mFont.LEFT);
            mFont.tahoma_7_grey.drawString(g, "theo điểm ván", x + w - 10, y + 8, mFont.RIGHT);
            g.setColor(MAU_VIEN, 0.35f);
            g.fillRect(x + 1, y + 24, w - 2, 1);

            const int CAO_NUT = 26;
            int yNut = y + h - CAO_NUT - 8;
            int yDs = y + 25;
            int caoDs = yNut - 6 - yDs;
            int n = m.sbMoc.Count;
            sbODongMoc.Clear();
            int diemNay = sbDangBan() ? m.sbDiemTam() : (m.sbCoKetQua ? m.sbDiemKQ : 0);
            if (n == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Đang tải...", x + w / 2, yDs + caoDs / 2 - 6,
                        mFont.CENTER);
            }
            else
            {
                int caoDong = caoDs / n;
                if (caoDong > 32)
                {
                    caoDong = 32;
                }
                int ke = -1;
                for (int i = 0; i < n; i++)
                {
                    if (diemNay < m.sbMoc[i].diem)
                    {
                        ke = i;
                        break;
                    }
                }
                for (int i = 0; i < n; i++)
                {
                    God.MiniGame.SbMoc mc = m.sbMoc[i];
                    int yd = yDs + i * caoDong;
                    int hd = caoDong;
                    sbODongMoc.Add(new int[] { x + 1, yd, w - 2, hd, i });
                    bool datRoi = diemNay >= mc.diem && (sbDangBan() || m.sbCoKetQua);
                    bool laKe = i == ke && sbDangBan();

                    if (datRoi)
                    {
                        g.setColor(MAU_XANH_LA, 0.08f);
                        g.fillRect(x + 1, yd, w - 2, hd);
                    }
                    if (laKe)
                    {
                        g.setColor(MAU_CHON, 1f);
                        g.fillRect(x + 1, yd + 3, 3, hd - 6, 1);
                    }
                    if (i < n - 1)
                    {
                        g.setColor(MAU_VIEN, 0.22f);
                        g.fillRect(x + 8, yd + hd - 1, w - 16, 1);
                    }

                    // Diem moc.
                    (datRoi ? mFont.tahoma_7b_green : mFont.tahoma_7b_dark).drawString(g,
                            mc.diem + string.Empty, x + 10, yd + hd / 2 - 6, mFont.LEFT);

                    // O qua.
                    int co = Math.min(hd - 8, 20);
                    int xq = x + 40;
                    int hetCho = x + w - (datRoi ? 24 : 8);
                    for (int k = 0; k < mc.qua.Count; k++)
                    {
                        if (xq + co > hetCho)
                        {
                            mFont.tahoma_7_grey.drawString(g, "+" + (mc.qua.Count - k),
                                    hetCho, yd + hd / 2 - 6, mFont.RIGHT);
                            break;
                        }
                        veOQuaSb(g, xq, yd + (hd - co) / 2, co, mc.qua[k]);
                        xq += co + 3;
                    }

                    if (datRoi)
                    {
                        veDauTichSb(g, x + w - 18, yd + hd / 2 - 6);
                    }
                    else if (laKe)
                    {
                        // Thanh tien do tu moc truoc toi moc nay.
                        int tu = i > 0 ? m.sbMoc[i - 1].diem : 0;
                        float tl = (float) (diemNay - tu) / (mc.diem - tu);
                        if (tl < 0f)
                        {
                            tl = 0f;
                        }
                        g.setColor(MAU_LOM, 1f);
                        g.fillRect(x + 40, yd + hd - 4, w - 50, 2, 1);
                        g.setColor(MAU_CHON, 1f);
                        g.fillRect(x + 40, yd + hd - 4, (int) ((w - 50) * tl), 2, 1);
                    }
                }
            }

            string lyDo = sbChoiDuoc();
            sbONutBatDau = new int[] { x + 8, yNut, w - 16, CAO_NUT };
            if (lyDo == null)
            {
                veNutVang(g, sbONutBatDau, "BẮT ĐẦU  ·  " + m.sbVe + " thỏi");
            }
            else
            {
                veNutChinh(g, sbONutBatDau, lyDo, false, MAU_CHON);
            }
        }

        /// <summary>Dấu tích trong vòng tròn xanh — vẽ bằng hình, font không có ký tự này.</summary>
        private static void veDauTichSb(mGraphics g, int x, int y)
        {
            g.setColor(MAU_XANH_LA, 1f);
            g.fillRect(x, y, 12, 12, 6);
            g.setColor(0xFFFFFF, 1f);
            g.fillRect(x + 2, y + 6, 2, 2);
            g.fillRect(x + 3, y + 7, 2, 2);
            g.fillRect(x + 4, y + 8, 2, 2);
            g.fillRect(x + 5, y + 7, 2, 2);
            g.fillRect(x + 6, y + 6, 2, 2);
            g.fillRect(x + 7, y + 5, 2, 2);
            g.fillRect(x + 8, y + 4, 2, 2);
        }

        // ==================================================================
        //  Hai thẻ lịch sử
        // ==================================================================

        private int sbSoDongLs()
        {
            God.MiniGame m = God.MiniGame.gI();
            return the2 == THE2_LS_TOI ? m.sbLsToi.Count : m.sbTop.Count;
        }

        private static string sbGio(long luc)
        {
            if (luc <= 0L)
            {
                return "-";
            }
            System.DateTime t = new System.DateTime(1970, 1, 1, 0, 0, 0, System.DateTimeKind.Utc)
                    .AddMilliseconds(luc).ToLocalTime();
            return t.ToString("HH:mm dd/MM");
        }

        private void veLsToiSb(mGraphics g)
        {
            God.MiniGame m = God.MiniGame.gI();
            int[] xCot = dvCot(0, 170, 330, 500, 700);
            veNenLs(g, new string[] { "Ván", "Điểm", "Mốc đạt", "Thỏi trả", "Lúc" }, xCot);
            int tong = sbSoDongLs();
            if (tong == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Bạn chưa chơi ván nào.",
                        x0 + rong / 2, yNoiDung() + 40, mFont.CENTER);
                veNutTrang(g, 0);
                return;
            }
            int moiTrang = soDongMotTrang();
            int dau = trang * moiTrang;
            for (int i = 0; i < moiTrang && dau + i < tong; i++)
            {
                God.MiniGame.SbDong d = m.sbLsToi[dau + i];
                int[] o = oDongLs(i);
                bool dat = d.moc >= 0;
                if (i % 2 == 1)
                {
                    g.setColor(MAU_LOM, 0.6f);
                    g.fillRect(o[0], o[1], o[2], o[3], 3);
                }
                mFont.tahoma_7_grey.drawString(g, "#" + d.van, o[0] + xCot[0] + 2, o[1] + 2,
                        mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g, d.diem + string.Empty, o[0] + xCot[1], o[1] + 2,
                        mFont.LEFT);
                string moc = dat && d.moc < m.sbMoc.Count ? "Mốc " + m.sbMoc[d.moc].diem
                        : (dat ? "Mốc " + (d.moc + 1) : "-");
                (dat ? mFont.tahoma_7b_green : mFont.tahoma_7_grey).drawString(g, moc,
                        o[0] + xCot[2], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g, d.thoi > 0 ? "+" + d.thoi : "-",
                        o[0] + xCot[3], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7_grey.drawString(g, sbGio(d.luc), o[0] + xCot[4], o[1] + 2,
                        mFont.LEFT);
            }
            veNutTrang(g, tong);
        }

        private void veTopSb(mGraphics g)
        {
            God.MiniGame m = God.MiniGame.gI();
            int[] xCot = dvCot(0, 110, 560, 780);
            veNenLs(g, new string[] { "Hạng", "Nhân vật", "Điểm cao nhất", "Số ván" }, xCot);
            int tong = sbSoDongLs();
            if (tong == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Hôm nay chưa có ai chơi.",
                        x0 + rong / 2, yNoiDung() + 40, mFont.CENTER);
                veNutTrang(g, 0);
                return;
            }
            int[] mauHuyChuong = { rgb(0xE0, 0xA8, 0x2A), rgb(0x9C, 0xA8, 0xB4), rgb(0xB8, 0x74, 0x40) };
            int moiTrang = soDongMotTrang();
            int dau = trang * moiTrang;
            for (int i = 0; i < moiTrang && dau + i < tong; i++)
            {
                int hang = dau + i;
                God.MiniGame.SbDong d = m.sbTop[hang];
                int[] o = oDongLs(i);
                if (i % 2 == 1)
                {
                    g.setColor(MAU_LOM, 0.6f);
                    g.fillRect(o[0], o[1], o[2], o[3], 3);
                }
                if (hang < 3)
                {
                    g.setColor(mauHuyChuong[hang], 1f);
                    g.fillRect(o[0] + xCot[0] + 3, o[1] + 1, 12, 12, 6);
                    mFont.tahoma_7b_white.drawString(g, (hang + 1) + string.Empty,
                            o[0] + xCot[0] + 9, o[1] + 1, mFont.CENTER);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g, (hang + 1) + string.Empty,
                            o[0] + xCot[0] + 9, o[1] + 2, mFont.CENTER);
                }
                mFont.tahoma_7b_dark.drawString(g, d.ten, o[0] + xCot[1], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g, d.diem + string.Empty, o[0] + xCot[2], o[1] + 2,
                        mFont.LEFT);
                mFont.tahoma_7_grey.drawString(g, d.moc + string.Empty, o[0] + xCot[3], o[1] + 2,
                        mFont.LEFT);
            }
            veNutTrang(g, tong);
        }

        // ==================================================================
        //  Chạm
        // ==================================================================

        /// <summary>
        /// Cú đập: bắt ngay lúc ngón <b>chạm xuống</b>, không đợi nhả tay.
        /// </summary>
        /// <remarks>
        /// Trò phản xạ mà đợi nhả tay thì chậm mất vài trăm mili giây — con vừa
        /// thò lên đã kịp lặn. Gọi ở mọi khung hình (trước cái chặn "chỉ khi vừa
        /// nhả ngón").
        /// </remarks>
        /// <returns><c>true</c> khi đã nuốt cú chạm này.</returns>
        private bool sbChamDap()
        {
            God.MiniGame m = God.MiniGame.gI();
            bool dangGiu = GameCanvas.isPointerDown;
            bool moiCham = dangGiu && !sbDownTruoc;
            sbDownTruoc = dangGiu;
            if (!m.sbDangChoi || sbOSan.Length != 4)
            {
                return false;
            }
            bool trongSan = GameCanvas.isPointerHoldIn(sbOSan[0], sbOSan[1], sbOSan[2], sbOSan[3]);
            if (!trongSan)
            {
                return false;
            }
            if (!moiCham)
            {
                // Van nuot moi cham trong san luc dang choi: khong de cu nha tay
                // lot xuong the / nut phia duoi.
                if (GameCanvas.isPointerJustRelease)
                {
                    GameCanvas.clearAllPointerEvent();
                }
                return true;
            }
            int ms = sbMsTrongVan();
            int px = GameCanvas.px;
            int py = GameCanvas.py;
            for (int k = 0; k < m.sbT.Length; k++)
            {
                if (m.sbLucDap[k] >= 0)
                {
                    continue;
                }
                int pha = ms - m.sbT[k];
                if (pha < 0 || pha > m.sbSong[k] + 60)
                {
                    continue;
                }
                int[] o = sbOHo[m.sbHo[k]];
                if (o == null)
                {
                    continue;
                }
                // Vung bam: tu dinh dau con vat toi mep duoi mieng ho, rong ca o.
                if (px < o[4] || px > o[4] + o[6] || py < o[1] - 62 || py > o[1] + o[3])
                {
                    continue;
                }
                m.sbLucDap[k] = ms;
                int d = m.sbDiemLoai(m.sbLoai[k]);
                SbHieuUng h = new SbHieuUng();
                h.x = o[0];
                h.y = o[1] - 30;
                h.chu = (d >= 0 ? "+" : string.Empty) + d;
                h.loai = m.sbLoai[k] == SB_LOAI_BULMA ? 2 : (m.sbLoai[k] == SB_LOAI_VANG ? 1 : 0);
                h.luc = mSystem.currentTimeMillis();
                sbHieuUng.Add(h);
                if (m.sbLoai[k] == SB_LOAI_BULMA)
                {
                    sbMocPhat = h.luc;
                }
                try
                {
                    SoundMn.gI().buttonClick();
                }
                catch (System.Exception)
                {
                }
                return true;
            }
            // Hut: cham bui nho, khong tru diem.
            SbHieuUng hut = new SbHieuUng();
            hut.x = px;
            hut.y = py;
            hut.loai = 3;
            hut.luc = mSystem.currentTimeMillis();
            sbHieuUng.Add(hut);
            return true;
        }

        /// <summary>Nút và dòng mốc — gọi ở khung vừa nhả ngón.</summary>
        private void chamSb()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (sbHienKQ())
            {
                if (sbONutDongKQ.Length == 4
                        && cham(sbONutDongKQ[0], sbONutDongKQ[1], sbONutDongKQ[2], sbONutDongKQ[3]))
                {
                    sbDaDongKQ = true;
                    return;
                }
                if (sbONutChoiLai.Length == 4
                        && cham(sbONutChoiLai[0], sbONutChoiLai[1], sbONutChoiLai[2], sbONutChoiLai[3]))
                {
                    sbBamBatDau();
                    return;
                }
            }
            if (sbONutBatDau.Length == 4
                    && cham(sbONutBatDau[0], sbONutBatDau[1], sbONutBatDau[2], sbONutBatDau[3]))
            {
                sbBamBatDau();
                return;
            }
            for (int i = 0; i < sbODongMoc.Count; i++)
            {
                int[] o = sbODongMoc[i];
                if (!cham(o[0], o[1], o[2], o[3]))
                {
                    continue;
                }
                if (o[4] < m.sbMoc.Count)
                {
                    God.MiniGame.SbMoc mc = m.sbMoc[o[4]];
                    string chu = "Mốc " + mc.diem + " điểm: ";
                    for (int k = 0; k < mc.qua.Count; k++)
                    {
                        chu += (k > 0 ? ", " : string.Empty) + mc.qua[k].ten + " x"
                                + mc.qua[k].soLuong;
                    }
                    GameScr.info1.addInfo(chu, 0);
                }
                return;
            }
        }

        private void sbBamBatDau()
        {
            string lyDo = sbChoiDuoc();
            if (lyDo != null)
            {
                if (!sbDangBan())
                {
                    GameScr.info1.addInfo(lyDo == "Thiếu thỏi vàng"
                            ? "Cần " + God.MiniGame.gI().sbVe + " thỏi vàng để mua vé!"
                            : "Hôm nay bạn đã chơi hết lượt, mai quay lại nhé!", 0);
                }
                return;
            }
            God.MiniGame.gI().sbBatDau();
        }
    }
}
