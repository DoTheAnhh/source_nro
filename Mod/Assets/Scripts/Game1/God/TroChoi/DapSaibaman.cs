// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game1.God
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
        //  Màu
        // ------------------------------------------------------------------
        private static readonly int SB_CO_SANG = rgb(0x9C, 0xD9, 0x5E);
        private static readonly int SB_CO_TOI = rgb(0x3F, 0x8C, 0x38);
        private static readonly int SB_DAT = rgb(0x8A, 0x5A, 0x30);
        private static readonly int SB_DAT_SANG = rgb(0xB9, 0x84, 0x4E);
        private static readonly int SB_LO = rgb(0x21, 0x13, 0x0A);
        private static readonly int SB_XANH_NUT = rgb(0x4F, 0xA8, 0x5C);
        private static readonly int SB_XANH_NUT_SANG = rgb(0x8F, 0xE8, 0x6B);
        private static readonly int SB_DO = rgb(0xD6, 0x45, 0x3A);

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

        /// <summary>Từng hố: tâm x, tâm y, rộng miệng, cao miệng, x ô, đỉnh ô, rộng ô.</summary>
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

        // ==================================================================
        //  Vẽ — màn chơi
        // ==================================================================

        private void veDapSaibaman(mGraphics g)
        {
            sbCapNhat();
            God.MiniGame m = God.MiniGame.gI();
            int x = x0 + LE;
            int w = rong - LE * 2;
            int yTren = yNoiDung() - 2;
            int yDuoi = y0 + cao - LE;
            int h = yDuoi - yTren;

            const int CAO_DAU = 18;
            veDauSb(g, x, yTren, w, CAO_DAU);

            int yThan = yTren + CAO_DAU + 4;
            int hThan = yDuoi - yThan;
            int rongBang = w * 34 / 100;
            if (rongBang < 132)
            {
                rongBang = 132;
            }
            if (rongBang > 186)
            {
                rongBang = 186;
            }
            int rongSan = w - rongBang - 6;
            veSanSb(g, x, yThan, rongSan, hThan);
            veBangMocSb(g, x + rongSan + 6, yThan, rongBang, hThan);
        }

        // ---- hàng đầu ----

        private void veDauSb(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            string luot = "Hôm nay: " + m.sbDaChoi + "/" + m.sbLuotNgay + " ván";
            mFont.tahoma_7b_dark.drawString(g, luot, x, y + 3, mFont.LEFT);

            string nhan;
            int mau;
            if (m.sbDangChoi)
            {
                int con = (m.sbDaiMs - sbMsTrongVan() + 999) / 1000;
                if (con < 0)
                {
                    con = 0;
                }
                nhan = "CÒN  00:" + (con < 10 ? "0" : string.Empty) + con;
                mau = con <= 5 ? MAU_XIU_SANG : MAU_TAI_SANG;
            }
            else if (m.sbChoKetQua)
            {
                nhan = "ĐANG CHẤM ĐIỂM";
                mau = rgb(0xD8, 0x9A, 0x1E);
            }
            else
            {
                nhan = "VÉ " + m.sbVe + " THỎI · " + m.sbGiay + " GIÂY";
                mau = SB_XANH_NUT;
            }
            int rongDH = 150;
            int xDH = x + w / 2 - rongDH / 2;
            veTheDem(g, xDH, y, rongDH, nhan, mau);
            if (m.sbDangChoi)
            {
                float tiLe = 1f - (float) sbMsTrongVan() / m.sbDaiMs;
                if (tiLe < 0f)
                {
                    tiLe = 0f;
                }
                g.setColor(0x000000, 0.25f);
                g.fillRect(xDH + 6, y + 15, rongDH - 12, 2, 1);
                g.setColor(MAU_VANG, 0.95f);
                g.fillRect(xDH + 6, y + 15, (int) ((rongDH - 12) * tiLe), 2, 1);
            }

            string so = dvSo(m.tongThoi);
            int rongSo = mFont.tahoma_7b_dark.getWidth(so);
            mFont.tahoma_7b_dark.drawString(g, so, x + w, y + 3, mFont.RIGHT);
            SmallImage.drawSmallImage(g, ICON_THOI_VANG, x + w - rongSo - 16, y + 8, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
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

            // Khung go + bai co.
            veKhungKep(g, xs, y, w, h, SB_CO_TOI, MAU_VANG, 0.95f);
            veDaiMau(g, xs + 3, y + 3, w - 6, h - 6, SB_CO_SANG, SB_CO_TOI, 10);
            g.setClip(xs + 3, y + 3, w - 6, h - 6);
            // Soc co cat xen ke.
            int soc = 22;
            for (int k = 0; k * soc < w; k += 2)
            {
                g.setColor(0xFFFFFF, 0.06f);
                g.fillRect(xs + 3 + k * soc, y + 3, soc, h - 6);
            }
            // Vai bong hoa nho, vi tri co dinh theo kich thuoc san.
            for (int k = 0; k < 14; k++)
            {
                int hx = xs + 8 + (k * 53 + 17) % (w - 16);
                int hy = y + 8 + (k * 37 + 11) % (h - 16);
                g.setColor(k % 3 == 0 ? 0xFFF2A8 : (k % 3 == 1 ? 0xFFFFFF : 0xFFB0C8), 0.7f);
                g.fillRect(hx, hy, 3, 3, 1);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            veGocKhung(g, xs, y, w, h, 0.9f);

            // Luoi 3 x 3 hố.
            int dinh = y + 24;
            int caoLuoi = h - 30;
            int rongO = (w - 12) / 3;
            int caoO = caoLuoi / 3;
            int rongMieng = Math.min(rongO * 72 / 100, 62);
            int caoMieng = Math.max(10, rongMieng * 34 / 100);
            for (int i = 0; i < 9; i++)
            {
                int cot = i % 3;
                int hang = i / 3;
                int xo = xs + 6 + cot * rongO;
                int yo = dinh + hang * caoO;
                int tamX = xo + rongO / 2;
                int tamY = yo + caoO * 74 / 100;
                sbOHo[i] = new int[] { tamX, tamY, rongMieng, caoMieng, xo, yo, rongO, caoO };
            }

            int ms = sbMsTrongVan();
            for (int i = 0; i < 9; i++)
            {
                veHoSb(g, i, ms, bayGio);
            }

            // Thanh thoi gian mong tren dinh san.
            if (m.sbDangChoi)
            {
                float tiLe = 1f - (float) ms / m.sbDaiMs;
                if (tiLe < 0f)
                {
                    tiLe = 0f;
                }
                int cuoi = m.sbDaiMs - ms;
                g.setColor(0x000000, 0.3f);
                g.fillRect(xs + 8, y + 6, w - 16, 5, 2);
                g.setColor(cuoi <= 5000 && bayGio / 200 % 2 == 0 ? SB_DO : MAU_VANG, 1f);
                g.fillRect(xs + 8, y + 6, (int) ((w - 16) * tiLe), 5, 2);
            }

            // Huy hieu diem goc trai.
            if (m.sbDangChoi || m.sbChoKetQua)
            {
                veHuyHieuDiemSb(g, xs + 8, y + 13, m.sbDiemTam());
            }

            veHieuUngSb(g, bayGio);

            // Loe do khi dap Bulma.
            if (tuPhat < 260L)
            {
                g.setColor(SB_DO, 0.28f * (1f - tuPhat / 260f));
                g.fillRect(xs + 3, y + 3, w - 6, h - 6, BO_GOC);
            }

            if (m.sbDangChoi && ms < 900)
            {
                veBangRonSb(g, xs, y, w, h, "BẮT ĐẦU!", SB_XANH_NUT, ms / 900f);
            }
            else if (m.sbChoKetQua)
            {
                veBangRonSb(g, xs, y, w, h, "Đang chấm điểm...", rgb(0xD8, 0x9A, 0x1E), 0.3f);
            }
            else if (!m.sbDangChoi && m.sbCoKetQua && !sbDaDongKQ)
            {
                veKetQuaSb(g, xs, y, w, h);
            }
            else if (!m.sbDangChoi)
            {
                veLoiMoiSb(g, xs, y, w, h);
            }
        }

        /// <summary>Huy hiệu điểm: nền tối trong, chữ ĐIỂM nhỏ, số to vàng.</summary>
        private static void veHuyHieuDiemSb(mGraphics g, int x, int y, int diem)
        {
            string so = diem + string.Empty;
            int rongSo = mFont.bigNumber_yellow.getWidth(so);
            int rong = rongSo + 44;
            g.setColor(0x000000, 0.45f);
            g.fillRect(x, y, rong, 20, 8);
            g.setColor(MAU_VANG, 0.9f);
            g.fillRect(x + 2, y + 2, 32, 16, 6);
            mFont.tahoma_7b_dark.drawString(g, "ĐIỂM", x + 18, y + 4, mFont.CENTER);
            mFont.bigNumber_yellow.drawString(g, so, x + 38, y + 3, mFont.LEFT);
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

            // Bong + u dat.
            g.setColor(0x000000, 0.18f);
            g.fillRect(tamX - rm / 2 - 5, tamY - cm / 2 + 2, rm + 10, cm + 6, (cm + 6) / 2);
            g.setColor(SB_DAT, 1f);
            g.fillRect(tamX - rm / 2 - 5, tamY - cm / 2 - 2, rm + 10, cm + 6, (cm + 6) / 2);
            g.setColor(SB_DAT_SANG, 1f);
            g.fillRect(tamX - rm / 2 - 3, tamY - cm / 2 - 2, rm + 6, cm / 2, cm / 4);
            // Mieng ho.
            g.setColor(SB_LO, 1f);
            g.fillRect(tamX - rm / 2, tamY - cm / 2, rm, cm, cm / 2);
            g.setColor(0x000000, 0.35f);
            g.fillRect(tamX - rm / 2 + 3, tamY - cm / 2 + 1, rm - 6, cm / 2, cm / 4);

            // Con dang trong ho nay (neu co).
            int loai = -1;
            float troi = 0f;
            int lech = 0;
            if (m.sbDangChoi || m.sbChoKetQua)
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
            else if (!(m.sbCoKetQua && !sbDaDongKQ) && i >= 3 && i <= 5)
            {
                // Man cho: hang giua bay san ba loai de nguoi choi nhin mat.
                loai = i - 3;
                troi = 1f;
            }

            if (loai >= 0)
            {
                const int CAO_NV = 54;
                int yChan = tamY + 10 + (int) ((1f - troi) * CAO_NV);
                int dir = (i % 3 == 2) ? -1 : 1;
                g.setClip(o[4], o[5] - 6, o[6], tamY - o[5] + 7);
                if (loai == SB_LOAI_VANG)
                {
                    float nhip = 0.55f + 0.35f * UnityEngine.Mathf.Sin(bayGio / 120f);
                    g.setColor(MAU_VANG, 0.45f * nhip * troi);
                    g.fillRect(tamX - 22, yChan - 50, 44, 44, 22);
                    g.setColor(0xFFFFFF, 0.3f * nhip * troi);
                    g.fillRect(tamX - 13, yChan - 42, 26, 26, 13);
                }
                int cf = (int) (bayGio / 320L % 2);
                bool ve;
                if (loai == SB_LOAI_BULMA)
                {
                    ve = veBulmaSb(g, tamX + lech, yChan, dir, cf);
                }
                else
                {
                    ve = veNhanVatPhan(g, SB_DAU, SB_THAN, SB_CHAN, tamX + lech, yChan, dir, cf);
                }
                if (!ve)
                {
                    veConThaySb(g, tamX + lech, yChan, loai);
                }
                g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
                if (loai == SB_LOAI_VANG && troi > 0.6f)
                {
                    veHatSang(g, tamX, yChan - 30, 6, 24, MAU_VANG, 0.9f, 1100);
                }
            }

            // Mep truoc cua ho, de len chan con vat.
            g.setColor(SB_DAT_SANG, 1f);
            g.fillRect(tamX - rm / 2 + 1, tamY + cm / 2 - 3, rm - 2, 4, 2);

            // Nhan diem o man cho.
            if (!sbDangBan() && !(m.sbCoKetQua && !sbDaDongKQ) && i >= 3 && i <= 5)
            {
                int d = m.sbDiemLoai(i - 3);
                string chu = (d >= 0 ? "+" : string.Empty) + d;
                int rc = mFont.tahoma_7b_white.getWidth(chu) + 12;
                int yc = tamY + cm / 2 + 4;
                g.setColor(i == 5 ? SB_DO : (i == 4 ? rgb(0xD8, 0x9A, 0x1E) : SB_XANH_NUT), 0.95f);
                g.fillRect(tamX - rc / 2, yc, rc, 13, 6);
                veChuNoi(g, mFont.tahoma_7b_white, chu, tamX, yc + 1, mFont.CENTER);
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

        /// <summary>Chữ nổi bay lên sau cú đập, vòng va chạm, hạt sáng.</summary>
        private void veHieuUngSb(mGraphics g, long bayGio)
        {
            for (int i = sbHieuUng.Count - 1; i >= 0; i--)
            {
                SbHieuUng h = sbHieuUng[i];
                long d = bayGio - h.luc;
                if (d > 750L)
                {
                    sbHieuUng.RemoveAt(i);
                    continue;
                }
                float t = d / 750f;
                // Vong va cham ngan.
                if (d < 220L && h.loai != 3)
                {
                    float u = d / 220f;
                    int co = 10 + (int) (34 * u);
                    g.setColor(h.loai == 2 ? SB_DO : 0xFFFFFF, 0.55f * (1f - u));
                    g.fillRect(h.x - co / 2, h.y - co / 2, co, co, co / 2);
                }
                if (h.chu == null)
                {
                    continue;
                }
                int yc = h.y - 12 - (int) (26 * t);
                mFont f = h.loai == 2 ? mFont.tahoma_7b_red
                        : (h.loai == 1 ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_white);
                if (t < 0.8f || bayGio / 60 % 2 == 0)
                {
                    veChuNoi(g, f, h.chu, h.x, yc, mFont.CENTER);
                }
            }
        }

        /// <summary>Băng rôn ngang giữa sân: "BẮT ĐẦU!", "Đang chấm điểm...".</summary>
        private static void veBangRonSb(mGraphics g, int x, int y, int w, int h, string chu,
                int mau, float t)
        {
            float a = t < 0.7f ? 1f : 1f - (t - 0.7f) / 0.3f;
            int rb = mFont.tahoma_7b_white.getWidth(chu) + 40;
            int xb = x + (w - rb) / 2;
            int yb = y + h / 2 - 12;
            g.setColor(0x000000, 0.35f * a);
            g.fillRect(xb + 2, yb + 3, rb, 24, 10);
            g.setColor(mau, 0.95f * a);
            g.fillRect(xb, yb, rb, 24, 10);
            veChuyenSac(g, xb, yb, rb, 24, 4);
            veChuNoi(g, mFont.tahoma_7b_white, chu, xb + rb / 2, yb + 6, mFont.CENTER);
        }

        /// <summary>Màn chờ: tiêu đề và luật ngắn, ba con bày sẵn ở hàng giữa.</summary>
        private void veLoiMoiSb(mGraphics g, int x, int y, int w, int h)
        {
            string tieuDe = "ĐẬP SAIBAMAN";
            int rt = mFont.tahoma_7b_white.getWidth(tieuDe) + 36;
            int xt = x + (w - rt) / 2;
            int yt = y + 6;
            veQuang(g, xt, yt, rt, 18, MAU_VANG, 0.7f, 3);
            veDaiMau(g, xt, yt, rt, 18, rgb(0xF0, 0xA1, 0x64), rgb(0xB8, 0x5A, 0x22), 6);
            veChuNoi(g, mFont.tahoma_7b_white, tieuDe, xt + rt / 2, yt + 3, mFont.CENTER);

            string luat = "Đập Saibaman ăn điểm — đừng đập nhầm Bulma!";
            if (mFont.tahoma_7b_white.getWidth(luat) + 20 > w)
            {
                luat = "Đập Saibaman, tránh Bulma!";
            }
            int rl = mFont.tahoma_7b_white.getWidth(luat) + 16;
            int yl = y + h - 20;
            g.setColor(rgb(0x0A, 0x3A, 0x1C), 0.75f);
            g.fillRect(x + (w - rl) / 2, yl, rl, 15, 7);
            mFont.tahoma_7b_white.drawString(g, luat, x + w / 2, yl + 1, mFont.CENTER);
        }

        /// <summary>Bảng kết quả giữa sân: điểm to, mốc đạt, quà nhận.</summary>
        private void veKetQuaSb(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            long bayGio = mSystem.currentTimeMillis();
            int rb = Math.min(250, w - 16);
            int cb = Math.min(132, h - 12);
            int xb = x + (w - rb) / 2;
            int yb = y + (h - cb) / 2;
            bool dat = m.sbMocKQ >= 0;

            g.setColor(0x000000, 0.35f);
            g.fillRect(x + 3, y + 3, w - 6, h - 6, BO_GOC);
            veQuang(g, xb, yb, rb, cb, dat ? MAU_VANG : rgb(0xAA, 0xAA, 0xAA), 0.9f, 4);
            veKhungKep(g, xb, yb, rb, cb, rgb(0xFF, 0xF4, 0xDC), MAU_VANG, 1f);
            veGocKhung(g, xb, yb, rb, cb, 0.9f);
            veDaiMau(g, xb + 3, yb + 3, rb - 6, 16, dat ? rgb(0xF0, 0xA1, 0x64) : rgb(0x8A, 0x8A, 0x8A),
                    dat ? rgb(0xB8, 0x5A, 0x22) : rgb(0x5A, 0x5A, 0x5A), 4);
            veChuNoi(g, mFont.tahoma_7b_white, "KẾT THÚC", xb + rb / 2, yb + 5, mFont.CENTER);

            // Diem to, co vong no.
            int yDiem = yb + 24;
            if (dat)
            {
                veVongNo(g, xb + rb / 2, yDiem + 10, 16, 70, MAU_VANG, 1500);
            }
            string so = m.sbDiemKQ + string.Empty;
            mFont.bigNumber_yellow.drawString(g, so, xb + rb / 2, yDiem, mFont.CENTER);
            mFont.tahoma_7b_dark.drawString(g, "điểm", xb + rb / 2
                    + mFont.bigNumber_yellow.getWidth(so) / 2 + 16, yDiem + 4, mFont.CENTER);

            int yMoc = yDiem + 22;
            if (dat && m.sbMocKQ < m.sbMoc.Count)
            {
                mFont.tahoma_7b_green2.drawString(g, "Đạt mốc " + m.sbMoc[m.sbMocKQ].diem
                        + " — nhận quà:", xb + rb / 2, yMoc, mFont.CENTER);
                veHangQuaSb(g, m.sbQuaKQ, xb + 10, yMoc + 14, rb - 20, 26, bayGio);
            }
            else
            {
                string ke = sbMocKe(m.sbDiemKQ);
                mFont.tahoma_7b_red.drawString(g, "Chưa tới mốc quà nào", xb + rb / 2, yMoc,
                        mFont.CENTER);
                if (ke != null)
                {
                    mFont.tahoma_7_grey.drawString(g, ke, xb + rb / 2, yMoc + 14, mFont.CENTER);
                }
            }

            // Hai nut: Choi lai / Dong.
            int yn = yb + cb - 26;
            int rn = (rb - 30) / 2;
            bool choiDuoc = sbChoiDuoc() == null;
            sbONutChoiLai = new int[] { xb + 10, yn, rn, 20 };
            sbONutDongKQ = new int[] { xb + 20 + rn, yn, rn, 20 };
            veNutXanhSb(g, sbONutChoiLai, "CHƠI LẠI", choiDuoc);
            veKhungBo(g, sbONutDongKQ[0], yn, rn, 20, MAU_THE, 1f, MAU_VIEN, 0.8f, 1);
            veChuyenSac(g, sbONutDongKQ[0] + 1, yn + 1, rn - 2, 18, 4);
            mFont.tahoma_7b_dark.drawString(g, "Đóng", sbONutDongKQ[0] + rn / 2, yn + 4,
                    mFont.CENTER);
        }

        /// <summary>"Còn N điểm nữa tới mốc M" — hoặc null nếu đã quá mốc cao nhất.</summary>
        private static string sbMocKe(int diem)
        {
            God.MiniGame m = God.MiniGame.gI();
            for (int i = 0; i < m.sbMoc.Count; i++)
            {
                if (diem < m.sbMoc[i].diem)
                {
                    return "Còn " + (m.sbMoc[i].diem - diem) + " điểm nữa tới mốc "
                            + m.sbMoc[i].diem;
                }
            }
            return null;
        }

        /// <summary>Một hàng icon vật phẩm kèm số lượng, căn giữa.</summary>
        private static void veHangQuaSb(mGraphics g, List<God.MiniGame.SbQua> ds, int x, int y,
                int w, int co, long bayGio)
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
                God.MiniGame.SbQua q = ds[i];
                int xo = xd + i * buoc;
                veOQuaSb(g, xo, y, co, q);
            }
        }

        /// <summary>Một ô quà: nền kem, icon giữa, số lượng góc dưới.</summary>
        private static void veOQuaSb(mGraphics g, int x, int y, int co, God.MiniGame.SbQua q)
        {
            veKhungBo(g, x, y, co, co, MAU_THE, 1f, MAU_VIEN, 0.7f, 1);
            if (q.icon >= 0)
            {
                g.setClip(x + 1, y + 1, co - 2, co - 2);
                SmallImage.drawSmallImage(g, q.icon, x + co / 2, y + co / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
                g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            }
            if (q.soLuong > 1)
            {
                string sl = "x" + q.soLuong;
                mFont.tahoma_7b_dark.drawString(g, sl, x + co - 1, y + co - 11, mFont.RIGHT);
            }
        }

        /// <summary>Nút xanh lá nổi — nút chính của trò.</summary>
        private static void veNutXanhSb(mGraphics g, int[] o, string chu, bool bamDuoc)
        {
            if (bamDuoc)
            {
                veQuang(g, o[0], o[1], o[2], o[3], SB_XANH_NUT_SANG, 0.8f, 2);
                veDaiMau(g, o[0], o[1], o[2], o[3], SB_XANH_NUT_SANG, SB_XANH_NUT, 6);
                veVetSang(g, o[0], o[1], o[2], o[3], 1600);
                veChuNoi(g, mFont.tahoma_7b_white, chu, o[0] + o[2] / 2, o[1] + o[3] / 2 - 6,
                        mFont.CENTER);
            }
            else
            {
                veKhungBo(g, o[0], o[1], o[2], o[3], rgb(0x88, 0x88, 0x88), 0.45f,
                        MAU_VIEN, 0.35f, 1);
                mFont.tahoma_7b_dark.drawString(g, chu, o[0] + o[2] / 2, o[1] + o[3] / 2 - 6,
                        mFont.CENTER);
            }
        }

        /// <summary>Lý do chưa chơi được, hoặc null nếu chơi được.</summary>
        private static string sbChoiDuoc()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (sbDangBan())
            {
                return m.sbDangChoi ? "ĐANG CHƠI..." : "ĐANG CHẤM...";
            }
            if (m.sbDaChoi >= m.sbLuotNgay)
            {
                return "HẾT LƯỢT HÔM NAY";
            }
            if (m.tongThoi < m.sbVe)
            {
                return "THIẾU THỎI VÀNG";
            }
            return null;
        }

        // ---- bảng mốc quà ----

        private void veBangMocSb(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            long bayGio = mSystem.currentTimeMillis();
            veKhungBo(g, x, y, w, h, MAU_THE, 0.95f, MAU_VIEN, 0.7f, 1);
            veDaiMau(g, x + 2, y + 2, w - 4, 16, rgb(0xF0, 0xA1, 0x64), rgb(0xC8, 0x6E, 0x30), 4);
            veChuNoi(g, mFont.tahoma_7b_white, "MỐC QUÀ", x + w / 2, y + 4, mFont.CENTER);

            const int CAO_NUT = 26;
            int yNut = y + h - CAO_NUT - 4;
            int yDs = y + 22;
            int caoDs = yNut - 4 - yDs;
            int n = m.sbMoc.Count;
            sbODongMoc.Clear();
            int diemNay = m.sbDangChoi || m.sbChoKetQua ? m.sbDiemTam()
                    : (m.sbCoKetQua ? m.sbDiemKQ : 0);
            if (n == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Đang tải...", x + w / 2, yDs + caoDs / 2 - 6,
                        mFont.CENTER);
            }
            else
            {
                int caoDong = caoDs / n;
                if (caoDong > 34)
                {
                    caoDong = 34;
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
                    int hd = caoDong - 3;
                    sbODongMoc.Add(new int[] { x + 3, yd, w - 6, hd, i });
                    bool datRoi = diemNay >= mc.diem;
                    bool laKe = i == ke;
                    if (laKe && sbDangBan())
                    {
                        float nhip = 0.5f + 0.5f * UnityEngine.Mathf.Sin(bayGio / 180f);
                        veQuang(g, x + 3, yd, w - 6, hd, MAU_VANG, nhip, 2);
                    }
                    veKhungBo(g, x + 3, yd, w - 6, hd,
                            datRoi ? rgb(0xD8, 0xF0, 0xC8) : rgb(0xFF, 0xFA, 0xEE), 1f,
                            datRoi ? SB_XANH_NUT : (laKe ? MAU_VANG : MAU_VIEN),
                            datRoi || laKe ? 0.95f : 0.4f, 1);

                    // Vien diem ben trai.
                    int rp = 34;
                    int cp = Math.min(hd - 4, 16);
                    int yp = yd + (hd - cp) / 2;
                    g.setColor(datRoi ? SB_XANH_NUT : rgb(0xB8, 0x5A, 0x22), 1f);
                    g.fillRect(x + 6, yp, rp, cp, cp / 2);
                    mFont.tahoma_7b_white.drawString(g, mc.diem + string.Empty, x + 6 + rp / 2,
                            yp + cp / 2 - 6, mFont.CENTER);

                    // Icon qua.
                    int co = Math.min(hd - 4, 24);
                    int xq = x + 6 + rp + 4;
                    int hetCho = x + w - 6;
                    for (int k = 0; k < mc.qua.Count; k++)
                    {
                        if (xq + co > hetCho)
                        {
                            mFont.tahoma_7b_dark.drawString(g, "...", hetCho - 2, yd + hd / 2 - 6,
                                    mFont.RIGHT);
                            break;
                        }
                        veOQuaSb(g, xq, yd + (hd - co) / 2, co, mc.qua[k]);
                        xq += co + 2;
                    }

                    if (datRoi)
                    {
                        // Dau tich goc phai tren.
                        // Ve bang hinh: font game khong co ky tu dau tich.
                        int xt = x + w - 16;
                        int yt = yd + 1;
                        g.setColor(SB_XANH_NUT, 1f);
                        g.fillRect(xt, yt, 11, 11, 5);
                        g.setColor(0xFFFFFF, 1f);
                        g.fillRect(xt + 2, yt + 5, 2, 2);
                        g.fillRect(xt + 3, yt + 6, 2, 2);
                        g.fillRect(xt + 4, yt + 7, 2, 2);
                        g.fillRect(xt + 5, yt + 6, 2, 2);
                        g.fillRect(xt + 6, yt + 5, 2, 2);
                        g.fillRect(xt + 7, yt + 4, 2, 2);
                        g.fillRect(xt + 8, yt + 3, 2, 2);
                    }
                    else if (laKe && sbDangBan() && i > 0)
                    {
                        // Thanh tien do tu moc truoc toi moc nay.
                        int tu = m.sbMoc[i - 1].diem;
                        float tl = (float) (diemNay - tu) / (mc.diem - tu);
                        if (tl < 0f)
                        {
                            tl = 0f;
                        }
                        g.setColor(0x000000, 0.2f);
                        g.fillRect(x + 6, yd + hd - 3, w - 12, 2, 1);
                        g.setColor(MAU_VANG, 1f);
                        g.fillRect(x + 6, yd + hd - 3, (int) ((w - 12) * tl), 2, 1);
                    }
                }
            }

            // Nut bat dau.
            string lyDo = sbChoiDuoc();
            sbONutBatDau = new int[] { x + 4, yNut, w - 8, CAO_NUT };
            veNutXanhSb(g, sbONutBatDau, lyDo ?? ("BẮT ĐẦU  (" + m.sbVe + " thỏi)"), lyDo == null);
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
                g.setColor(dat ? 0xD8F0C8 : (i % 2 == 0 ? 0xFFFFFF : 0xE8D6B8), dat ? 0.6f : 0.35f);
                g.fillRect(o[0], o[1], o[2], o[3], 3);
                mFont.tahoma_7b_dark.drawString(g, "#" + d.van, o[0] + xCot[0] + 2, o[1] + 2,
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
            int[] mauHuyChuong = { rgb(0xF2, 0xC2, 0x2E), rgb(0xB8, 0xC4, 0xD0), rgb(0xC8, 0x80, 0x48) };
            int moiTrang = soDongMotTrang();
            int dau = trang * moiTrang;
            for (int i = 0; i < moiTrang && dau + i < tong; i++)
            {
                int hang = dau + i;
                God.MiniGame.SbDong d = m.sbTop[hang];
                int[] o = oDongLs(i);
                g.setColor(hang < 3 ? 0xFFF1C8 : (i % 2 == 0 ? 0xFFFFFF : 0xE8D6B8),
                        hang < 3 ? 0.75f : 0.35f);
                g.fillRect(o[0], o[1], o[2], o[3], 3);
                if (hang < 3)
                {
                    g.setColor(mauHuyChuong[hang], 1f);
                    g.fillRect(o[0] + xCot[0] + 2, o[1] + 1, 14, 12, 6);
                    mFont.tahoma_7b_white.drawString(g, (hang + 1) + string.Empty,
                            o[0] + xCot[0] + 9, o[1] + 1, mFont.CENTER);
                }
                else
                {
                    mFont.tahoma_7b_dark.drawString(g, (hang + 1) + string.Empty,
                            o[0] + xCot[0] + 9, o[1] + 2, mFont.CENTER);
                }
                mFont.tahoma_7b_dark.drawString(g, d.ten, o[0] + xCot[1], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_red.drawString(g, d.diem + string.Empty, o[0] + xCot[2], o[1] + 2,
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
            // Hut: vong bui nho, khong tru diem.
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
            if (!m.sbDangChoi && m.sbCoKetQua && !sbDaDongKQ)
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
                    GameScr.info1.addInfo(lyDo == "THIẾU THỎI VÀNG"
                            ? "Cần " + God.MiniGame.gI().sbVe + " thỏi vàng để mua vé!"
                            : "Hôm nay bạn đã chơi hết lượt, mai quay lại nhé!", 0);
                }
                return;
            }
            God.MiniGame.gI().sbBatDau();
        }
    }
}
