// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game2.God
{
    /// <summary>
    /// Thẻ <b>Tài Xỉu</b> — mặt bàn, hai cửa, đĩa và bát, cầu, chip.
    /// </summary>
    /// <remarks>
    /// <para><b>Bố cục:</b> một mặt bàn đêm viền vàng chiếm giữa màn — đó là
    /// "sân khấu" của trò, như biển của Câu Cá hay sông của Đua Vịt. Hai cửa
    /// là hai vùng trên bàn, đĩa ở giữa. Dưới bàn là cầu kết quả (giữa) rồi hàng
    /// chip.</para>
    ///
    /// <para><b>Tranh vẽ sẵn</b> (<c>Resources/tc_*.png</c>): chữ TÀI/XỈU, đĩa,
    /// bát sơn mài có núm ngọc rồng, xúc xắc, chip, bi cầu, quầng sáng. Vẽ ở mật
    /// độ gấp bốn rồi co lại bằng <c>veAnhVuaO</c> — nhờ vậy có chuyển sắc thật,
    /// đổ bóng mờ và khử răng cưa mà <c>fillRect</c> không làm được. Thiếu tệp
    /// nào thì chỗ đó vẽ khối màu thay, không hỏng cả màn.</para>
    /// </remarks>
    public partial class TroChoiUI
    {
        private static readonly int TX_BAN_TREN = rgb(0x24, 0x34, 0x64);
        private static readonly int TX_BAN_DUOI = rgb(0x0E, 0x14, 0x30);
        private static readonly int TX_VIEN_VANG = rgb(0xC9, 0x9A, 0x3E);
        private static readonly int TX_VANG_SANG = rgb(0xFF, 0xE0, 0x8A);
        private static readonly int TX_VANG_TOI = rgb(0xE9, 0xA1, 0x28);

        // ------------------------------------------------------------------
        //  Tranh vẽ sẵn
        // ------------------------------------------------------------------

        private static readonly Dictionary<string, Image> khoTranh = new Dictionary<string, Image>();

        /// <summary>Nạp một tranh trong Resources, nhớ lại cả khi thiếu (null).</summary>
        private static Image tranh(string ten)
        {
            Image img;
            if (khoTranh.TryGetValue(ten, out img))
            {
                return img;
            }
            try
            {
                img = Image.createImage(ten);
            }
            catch (System.Exception)
            {
                img = null;
            }
            khoTranh[ten] = img;
            return img;
        }

        private static bool veTranh(mGraphics g, string ten, int x, int y, int w, int h)
        {
            Image img = tranh(ten);
            if (img == null)
            {
                return false;
            }
            g.veAnhVuaO(img, x, y, w, h);
            return true;
        }

        /// <summary>Vẽ tranh với độ trong <paramref name="a"/> (qua GUI.color).</summary>
        private static bool veTranhMo(mGraphics g, string ten, int x, int y, int w, int h, float a)
        {
            Image img = tranh(ten);
            if (img == null)
            {
                return false;
            }
            if (a < 0f)
            {
                a = 0f;
            }
            UnityEngine.GUI.color = new UnityEngine.Color(1f, 1f, 1f, a > 1f ? 1f : a);
            g.veAnhVuaO(img, x, y, w, h);
            UnityEngine.GUI.color = UnityEngine.Color.white;
            return true;
        }

        // ------------------------------------------------------------------
        //  Bố cục
        // ------------------------------------------------------------------

        private const int TX_CAO_CHIP = 28;

        private int yChipTx()
        {
            return y0 + cao - LE - TX_CAO_CHIP;
        }

        /// <summary>Hàng cầu kết quả: dưới mặt bàn, giữa.</summary>
        private int yLsTx()
        {
            return yChipTx() - 22;
        }

        /// <summary>Đỉnh mặt bàn.</summary>
        private int yBanTx()
        {
            return yNoiDung() + 24;
        }

        private int caoBanTx()
        {
            return yLsTx() - 6 - yBanTx();
        }

        /// <summary>Đường kính đĩa: vừa chiều cao bàn, chừa chỗ cho vòng chấm đếm giờ.</summary>
        private int coDia()
        {
            int con = caoBanTx() - 20;
            if (con > CO_DIA_TOI_DA)
            {
                con = CO_DIA_TOI_DA;
            }
            if (con < 70)
            {
                con = 70;
            }
            return con;
        }

        /// <summary>Đỉnh ô vuông ngoại tiếp đĩa (khung dùng cho nặn bát).</summary>
        private int yBan()
        {
            return yBanTx() + caoBanTx() / 2 - coDia() / 2;
        }

        private int tamDiaX()
        {
            return x0 + rong / 2;
        }

        private int tamDiaY(int yKhung)
        {
            return yKhung + coDia() / 2;
        }

        private int rongVungTx()
        {
            return (rong - LE * 2 - coDia() - 44) / 2;
        }

        /// <summary>Vùng cửa Tài (trái) hoặc Xỉu (phải) trên mặt bàn.</summary>
        private int[] oCua(bool tai)
        {
            int rv = rongVungTx();
            int x = tai ? x0 + LE + 10 : x0 + rong - LE - 10 - rv;
            return new int[] { x, yBanTx() + 8, rv, caoBanTx() - 16 };
        }

        private int[] oNutDat(bool tai)
        {
            int[] o = oCua(tai);
            int nw = o[2] - 16 - RONG_NUT_HUY - 6;
            return new int[] { o[0] + 8, o[1] + o[3] - CAO_NUT_CUA - 8, nw, CAO_NUT_CUA };
        }

        private int[] oNutHuy(bool tai)
        {
            int[] o = oCua(tai);
            int nw = o[2] - 16 - RONG_NUT_HUY - 6;
            return new int[] { o[0] + 8 + nw + 6, o[1] + o[3] - CAO_NUT_CUA - 8,
                RONG_NUT_HUY, CAO_NUT_CUA };
        }

        /// <summary>Hàng chip: bốn chip tròn rồi hai nút Tất tay / Nhập số, căn giữa.</summary>
        private int[] oMuc(int i)
        {
            const int co = 28;
            const int khe = 10;
            const int rongVien = 70;
            int tong = MUC.Length * (co + khe) + 2 * (rongVien + khe) - khe;
            int x = x0 + rong / 2 - tong / 2;
            int y = yChipTx();
            if (i < MUC.Length)
            {
                return new int[] { x + i * (co + khe), y, co, co };
            }
            int xv = x + MUC.Length * (co + khe) + (i - MUC.Length) * (rongVien + khe);
            return new int[] { xv, y + 2, rongVien, 24 };
        }

        // ------------------------------------------------------------------
        //  Vẽ
        // ------------------------------------------------------------------

        private void veTaiXiu(mGraphics g)
        {
            int y = yNoiDung();
            int x = x0 + LE;
            int w = rong - LE * 2;
            mFont.tahoma_7_grey.drawString(g, "Phiên", x, y + 2, mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g, "#" + phien,
                    x + mFont.tahoma_7_grey.getWidth("Phiên") + 4, y + 2, mFont.LEFT);
            veDongHo(g, x0 + rong / 2, y);
            string tong = dvSo(tongTai + tongXiu);
            mFont.tahoma_7b_dark.drawString(g, tong, x + w, y + 2, mFont.RIGHT);
            mFont.tahoma_7_grey.drawString(g, "Tổng cược",
                    x + w - mFont.tahoma_7b_dark.getWidth(tong) - 4, y + 2, mFont.RIGHT);

            veBanTx(g);
            veDaiLichSu(g);
            veChipTx(g);
        }

        private void veBanTx(mGraphics g)
        {
            int x = x0 + LE;
            int w = rong - LE * 2;
            int yS = yBanTx();
            int hS = caoBanTx();

            // Mat ban: vien vang, than xanh dem chuyen sac, mot vach sang mong o
            // mep tren cho mat ban co do day.
            g.setColor(0x000000, 0.18f);
            g.fillRect(x, yS + 3, w, hS, 12);
            g.setColor(TX_VIEN_VANG, 1f);
            g.fillRect(x, yS, w, hS, 12);
            g.veDaiDoc(x + 2, yS + 2, w - 4, hS - 4, 10, TX_BAN_TREN, TX_BAN_DUOI);
            g.setColor(0xFFFFFF, 0.06f);
            g.fillRect(x + 12, yS + 5, w - 24, 1);

            int co = coDia();
            int cx = tamDiaX();
            int cy = yBanTx() + hS / 2;
            g.setClip(x + 2, yS + 2, w - 4, hS - 4);
            // Den roi xuong giua ban.
            veTranh(g, "tc_den", cx - (co * 2 + 60) / 2, cy - hS, co * 2 + 60, hS * 2);
            veCua(g, true);
            veCua(g, false);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            veSanXucXac(g, cx, yBan());
        }

        private void veCua(mGraphics g, bool tai)
        {
            int[] o = oCua(tai);
            int xv = o[0];
            int yv = o[1];
            int rv = o[2];
            int hv = o[3];
            int m = tai ? MAU_TAI_SANG : MAU_XIU_SANG;
            bool daDatCuaNay = daDatVanNay() && (cuaCuaToi == CUA_TAI) == tai;
            bool laCuaChon = cuaChon >= 0 && (cuaChon == CUA_TAI) == tai;
            bool biet = daBietKetQua() && ketQuaVan >= 0;
            bool thang = biet && (ketQuaVan == CUA_TAI) == tai;
            bool thua = biet && !thang;
            bool sang = laCuaChon || daDatCuaNay || thang;
            int gx = xv + rv / 2;

            // Nen vung: sang mau cua khi duoc chon / thang, con lai la mot tam
            // toi trong tren mat ban.
            if (thang)
            {
                veTranhMo(g, "tc_quang_vang", gx - (rv + 80) / 2, yv + 30 - 75, rv + 80, 150,
                        0.7f + 0.3f * nhip(600));
            }
            if (sang)
            {
                veTranh(g, tai ? "tc_quang_xanh" : "tc_quang_do", gx - (rv + 40) / 2, yv - 30,
                        rv + 40, 120);
                g.setColor(thang ? TX_VANG_SANG : (tai ? rgb(0x7F, 0xB2, 0xFF) : rgb(0xFF, 0x8A, 0x7A)),
                        0.95f);
                g.fillRect(xv, yv, rv, hv, 10);
                g.veDaiDoc(xv + 2, yv + 2, rv - 4, hv - 4, 9, tronMau(m, TX_BAN_DUOI, 0.45f),
                        tronMau(m, TX_BAN_DUOI, 0.78f));
            }
            else
            {
                g.setColor(0xFFFFFF, 0.10f);
                g.fillRect(xv, yv, rv, hv, 10);
                g.setColor(TX_BAN_DUOI, 0.6f);
                g.fillRect(xv + 1, yv + 1, rv - 2, hv - 2, 9);
            }

            // Chu TAI / XIU lon, co theo be ngang vung.
            int ww = Math.min(108, rv - 16);
            int wh = ww * 46 / 108;
            if (!veTranh(g, tai ? "tc_tai" : "tc_xiu", gx - ww / 2, yv + 24 - wh / 2, ww, wh))
            {
                (tai ? mFont.tahoma_7b_blue : mFont.tahoma_7b_red).drawString(g,
                        tai ? "TÀI" : "XỈU", gx, yv + 18, mFont.CENTER);
            }
            bool rongRai = hv >= 130;
            int yTien = rongRai ? yv + 62 : yv + 48;
            if (rongRai)
            {
                mFont.tahoma_7_white.drawString(g, tai ? "Tổng 11 - 18" : "Tổng 3 - 10", gx,
                        yv + 46, mFont.CENTER);
            }
            if (thang)
            {
                int rb = mFont.tahoma_7b_dark.getWidth("THẮNG") + 14;
                g.veDaiDoc(xv + rv - rb - 8, yv + 8, rb, 15, 7, TX_VANG_SANG, TX_VANG_TOI);
                mFont.tahoma_7b_dark.drawString(g, "THẮNG", xv + rv - 8 - rb / 2, yv + 9,
                        mFont.CENTER);
            }

            // Tong cuoc cua cua: icon + so to vang, can giua ca cum.
            string so = dvSo(tai ? tongTai : tongXiu);
            int ws = mFont.bigNumber_yellow.getWidth(so);
            int xCum = gx - (ws + 20) / 2;
            SmallImage.drawSmallImage(g, ICON_THOI_VANG, xCum + 8, yTien + 9, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
            mFont.bigNumber_yellow.drawString(g, so, xCum + 20, yTien, mFont.LEFT);

            // Dong cua nguoi choi.
            string dong = null;
            mFont fDong = mFont.tahoma_7_white;
            if (daDatCuaNay)
            {
                dong = biet ? (thang ? "Bạn thắng!" : "Bạn thua") : "Bạn đã đặt " + dvSo(soThoiCuaToi);
                fDong = mFont.tahoma_7b_yellow;
            }
            else if (laCuaChon && choDatCuoc())
            {
                dong = tienCho > 0 ? "Đang chọn " + dvSo(tienCho) : "Chọn chip bên dưới";
                fDong = mFont.tahoma_7b_yellow;
            }
            else if (choDatCuoc() && !daDatVanNay())
            {
                dong = "Chạm để chọn cửa";
            }
            int yDong = yTien + 24;
            int dinhNut = oNutDat(tai)[1];
            if (dong != null && yDong + 12 <= dinhNut - 2)
            {
                fDong.drawString(g, dong, gx, yDong, mFont.CENTER);
            }

            veNutXacNhan(g, tai, laCuaChon);
            veNutHuy(g, tai, laCuaChon);

            if (thua)
            {
                g.setColor(rgb(0x05, 0x07, 0x0F), 0.5f);
                g.fillRect(xv, yv, rv, hv, 10);
            }
        }

        /// <summary>Nút vàng nổi trên mặt bàn tối.</summary>
        private static void veNutVang(mGraphics g, int[] n, string chu)
        {
            bool nhan = GameCanvas.isPointerDown && GameCanvas.isPointerHoldIn(n[0], n[1], n[2], n[3]);
            if (nhan)
            {
                g.veDaiDoc(n[0], n[1] + 1, n[2], n[3] - 1, 6, TX_VANG_TOI, TX_VANG_SANG);
            }
            else
            {
                g.veDaiDoc(n[0], n[1], n[2], n[3], 6, TX_VANG_SANG, TX_VANG_TOI);
                g.setColor(rgb(0x7A, 0x4A, 0x08), 1f);
                g.fillRect(n[0] + 2, n[1] + n[3] - 2, n[2] - 4, 2, 1);
            }
            mFont.tahoma_7b_dark.drawString(g, chu, n[0] + n[2] / 2, n[1] + n[3] / 2 - 6, mFont.CENTER);
        }

        private void veNutXacNhan(mGraphics g, bool tai, bool laCuaChon)
        {
            int[] n = oNutDat(tai);
            bool bamDuoc = choDatCuoc() && laCuaChon && tienCho > 0;
            if (bamDuoc)
            {
                veNutVang(g, n, "ĐẶT " + dvSo(tienCho));
                return;
            }
            string nhan;
            if (daDatVanNay())
            {
                nhan = "ĐÃ CHỐT";
            }
            else if (pha != PHA_DAT_CUOC)
            {
                nhan = "CHỜ VÁN SAU";
            }
            else
            {
                nhan = "ĐẶT CƯỢC";
            }
            // Chua chon cua nay thi nut tat trai ca hang (khong co Huy).
            int[] h = oNutHuy(tai);
            int rongNut = laCuaChon && choDatCuoc() ? n[2] : h[0] + h[2] - n[0];
            g.setColor(0xFFFFFF, 0.08f);
            g.fillRect(n[0], n[1], rongNut, n[3], 6);
            mFont.tahoma_7_grey.drawString(g, nhan, n[0] + rongNut / 2, n[1] + n[3] / 2 - 6,
                    mFont.CENTER);
        }

        private void veNutHuy(mGraphics g, bool tai, bool laCuaChon)
        {
            if (!(laCuaChon && choDatCuoc()))
            {
                return;
            }
            int[] n = oNutHuy(tai);
            bool bamDuoc = tienCho > 0;
            g.setColor(0xFFFFFF, bamDuoc ? 0.35f : 0.15f);
            g.fillRect(n[0], n[1], n[2], n[3], 6);
            g.setColor(TX_BAN_DUOI, 0.85f);
            g.fillRect(n[0] + 1, n[1] + 1, n[2] - 2, n[3] - 2, 5);
            (bamDuoc ? mFont.tahoma_7b_white : mFont.tahoma_7_grey).drawString(g, "Huỷ",
                    n[0] + n[2] / 2, n[1] + n[3] / 2 - 6, mFont.CENTER);
        }

        // ---- đĩa, xúc xắc, bát ----

        private void veSanXucXac(mGraphics g, int giua, int y)
        {
            capNhatNan();
            int co = coDia();
            int tx = tamDiaX();
            int ty = tamDiaY(y);

            veVongDemGio(g, tx, ty, co);
            g.setColor(0x000000, 0.45f);
            g.fillRect(tx - co / 2 + 2, ty - co / 2 + 6, co, co, co / 2);
            if (!veTranh(g, "tc_dia", tx - co / 2, ty - co / 2, co, co))
            {
                veTron(g, tx, ty, co, MAU_VANG, 1f);
                veTron(g, tx, ty, co - 12, rgb(0x3E, 0x0F, 0x0A), 1f);
            }
            g.setClip(tx - co / 2, ty - co / 2, co, co);
            veBaXucXac(g, tx, ty);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            veCaiBat(g, tx, ty);

            if (daMoHan)
            {
                long troiMo = mSystem.currentTimeMillis() - mocMoHan;
                if (troiMo < 700)
                {
                    veHatSang(g, tx, ty, 10, co / 2 + 16, MAU_VANG, 0.9f * (1f - troiMo / 700f), 700);
                }
            }
            if (daBietKetQua())
            {
                long troi = mSystem.currentTimeMillis() - mocBietKetQua();
                if (troi >= 0 && troi < 450)
                {
                    veTron(g, tx, ty, co, 0xFFFFFF, 0.45f * (1f - troi / 450f));
                }
                string kq = (ketQuaVan == CUA_TAI ? "Tài " : "Xỉu ") + tongXucXac();
                int rk = mFont.tahoma_7b_yellow.getWidth(kq) + 18;
                int yk = ty + co * 29 / 100;
                g.setColor(0x000000, 0.65f);
                g.fillRect(tx - rk / 2, yk, rk, 16, 8);
                mFont.tahoma_7b_yellow.drawString(g, kq, tx, yk + 2, mFont.CENTER);
            }
        }

        /// <summary>
        /// Vòng chấm đếm giờ quanh đĩa: chấm sáng tắt dần theo thời gian còn lại.
        /// </summary>
        private void veVongDemGio(mGraphics g, int tx, int ty, int co)
        {
            const int SO_CHAM = 40;
            int giay = giayHienThi();
            int dai = DAI_PHA[pha < 0 || pha > 2 ? 0 : pha];
            float con = pha == PHA_KET_QUA || dai <= 0 ? 0f : (float) giay / dai;
            if (con > 1f)
            {
                con = 1f;
            }
            int sangDen = (int) (SO_CHAM * con + 0.999f);
            bool gap = pha == PHA_DAT_CUOC && giay <= 5;
            int mauSang = gap ? MAU_XIU_SANG : (pha == PHA_DAT_CUOC ? rgb(0xFF, 0xD6, 0x6B) : 0xFFFFFF);
            int r = co / 2 + 7;
            for (int i = 0; i < SO_CHAM; i++)
            {
                float goc = -1.5708f + 6.2832f * i / SO_CHAM;
                int px = tx + (int) (UnityEngine.Mathf.Cos(goc) * r);
                int py = ty + (int) (UnityEngine.Mathf.Sin(goc) * r);
                bool sang = i < sangDen;
                g.setColor(sang ? mauSang : 0x000000, sang ? 1f : 0.35f);
                g.fillRect(px - 2, py - 2, 4, 4, 2);
            }
        }

        private void veBaXucXac(mGraphics g, int tx, int ty)
        {
            int banKinh = banKinhXucXac();
            int canh = canhXX();
            xDinh[0] = tx;
            yDinh[0] = ty - banKinh + 2;
            xDinh[1] = tx - banKinh - 1;
            yDinh[1] = ty + banKinh - canh / 4;
            xDinh[2] = tx + banKinh + 1;
            yDinh[2] = ty + banKinh - canh / 4;

            bool rung = pha == PHA_LAC && !choNan();
            for (int i = 0; i < 3; i++)
            {
                int mat = xucXac[i];
                if (mat < 1 || mat > 6)
                {
                    mat = 1;
                }
                int dx = 0;
                int dy = 0;
                if (rung)
                {
                    long t = mSystem.currentTimeMillis();
                    mat = (int) (((t / 70) + i * 3) % 6) + 1;
                    dx = (int) ((nhip(250 + i * 40) - 0.5f) * 13);
                    dy = (int) ((nhip(180 + i * 55) - 0.5f) * 13);
                }
                int xx = xDinh[i] - canh / 2 + dx;
                int yy = yDinh[i] - canh / 2 + dy;
                g.setColor(0x000000, 0.35f);
                g.fillRect(xx + 2, yy + 4, canh, canh, 8);
                if (!veTranh(g, "tc_xx" + mat, xx, yy, canh, canh))
                {
                    veMotXucXac(g, xx, yy, canh, mat, rung);
                }
            }
        }

        private void veCaiBat(mGraphics g, int txDia, int tyDia)
        {
            if (pha == PHA_KET_QUA)
            {
                return;
            }
            float ro = doRoCuaBat();
            if (ro <= 0f)
            {
                return;
            }
            int tx = txDia + (int) nanX;
            int ty = tyDia + (int) nanY;
            if (pha == PHA_LAC && !choNan())
            {
                tx += (int) ((nhip(105) - 0.5f) * 8);
                ty += (int) ((nhip(85) - 0.5f) * 8);
            }
            int cb = coBat();
            g.setColor(0x000000, 0.35f * ro);
            g.fillRect(tx - cb / 2 + 3, ty - cb / 2 + 6, cb, cb, cb / 2);
            if (!veTranhMo(g, "tc_bat", tx - cb / 2, ty - cb / 2, cb, cb, ro))
            {
                veTron(g, tx, ty, cb, MAU_VANG, 0.8f * ro);
                veTron(g, tx, ty, cb - 6, rgb(0xB0, 0x2A, 0x1C), ro);
            }
            if (choNan() && !daMoHan)
            {
                // Vong sang nhac rang bat nay keo duoc.
                veVongNo(g, tx, ty, cb - 10, cb + 22, MAU_VANG, 1400);
            }
        }

        // ---- cầu kết quả, dưới mặt bàn ----

        private void veDaiLichSu(mGraphics g)
        {
            int n = lichSuNhanh.Count;
            if (n == 0)
            {
                return;
            }
            const int co = 14;
            const int khe = 3;
            int toiDa = (rong - LE * 2) / (co + khe);
            if (n > toiDa)
            {
                n = toiDa;
            }
            int y = yLsTx();
            int tong = n * (co + khe) - khe;
            int x = x0 + rong / 2 - tong / 2;
            // Cu nhat ben trai, van vua xong sat phai co vien muc.
            for (int k = 0; k < n; k++)
            {
                int i = n - 1 - k;
                int xb = x + k * (co + khe);
                bool tai = lichSuNhanh[i] == CUA_TAI;
                if (i == 0)
                {
                    g.setColor(MAU_TIEU_DE, 1f);
                    g.fillRect(xb - 2, y - 2, co + 4, co + 4, (co + 4) / 2);
                }
                if (!veTranh(g, tai ? "tc_bi_t" : "tc_bi_x", xb, y, co, co))
                {
                    g.setColor(tai ? MAU_TAI_SANG : MAU_XIU_SANG, 1f);
                    g.fillRect(xb, y, co, co, co / 2);
                    mFont.tahoma_7b_white.drawString(g, tai ? "T" : "X", xb + co / 2, y + 1,
                            mFont.CENTER);
                }
            }
        }

        // ---- hàng chip ----

        private void veChipTx(mGraphics g)
        {
            bool bam = choDatCuoc() && cuaChon >= 0;
            for (int i = 0; i < MUC.Length; i++)
            {
                int[] o = oMuc(i);
                bool nhan = bam && GameCanvas.isPointerDown
                        && GameCanvas.isPointerHoldIn(o[0], o[1], o[2], o[3]);
                int lech = nhan ? 1 : 0;
                if (!veTranhMo(g, "tc_chip" + MUC[i], o[0], o[1] + lech, o[2], o[3], bam ? 1f : 0.45f))
                {
                    veChip(g, o, "+" + MUC[i], bam);
                }
            }
            veChip(g, oMuc(MUC.Length), "Tất tay", bam);
            veChip(g, oMuc(MUC.Length + 1), "Nhập số", bam);
        }

        // ---- bảng báo thắng thua ----

        private void veBaoKetQua(mGraphics g)
        {
            int w = 220;
            int h = 100;
            int x = x0 + (rong - w) / 2;
            int y = y0 + (cao - h) / 2;

            g.setColor(rgb(0x05, 0x07, 0x0F), 0.55f);
            g.fillRect(x0 + 1, y0 + CAO_TIEU_DE, rong - 2, cao - CAO_TIEU_DE - 1, 7);
            if (thangVanRoi)
            {
                veTranhMo(g, "tc_quang_vang", x + w / 2 - 110, y - 60, 220, 150,
                        0.7f + 0.3f * nhip(700));
            }
            g.setColor(0x000000, 0.3f);
            g.fillRect(x, y + 3, w, h, 12);
            g.setColor(TX_VIEN_VANG, 1f);
            g.fillRect(x, y, w, h, 12);
            g.veDaiDoc(x + 2, y + 2, w - 4, h - 4, 10, TX_BAN_TREN, TX_BAN_DUOI);

            string tieuDe = thangVanRoi ? "BẠN THẮNG" : (hoanVanRoi ? "HOÀN TIỀN" : "BẠN THUA");
            (thangVanRoi ? mFont.tahoma_7b_yellow : (hoanVanRoi ? mFont.tahoma_7b_white
                    : mFont.tahoma_7b_red)).drawString(g, tieuDe, x + w / 2, y + 10, mFont.CENTER);

            string so = (thangVanRoi ? "+" : (hoanVanRoi ? "" : "-")) + dvSo(tienVanRoi);
            int ws = mFont.bigNumber_yellow.getWidth(so);
            int xCum = x + w / 2 - (ws + 20) / 2;
            SmallImage.drawSmallImage(g, ICON_THOI_VANG, xCum + 8, y + 37, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
            mFont.bigNumber_yellow.drawString(g, so, xCum + 20, y + 28, mFont.LEFT);
            mFont.tahoma_7_white.drawString(g, hoanVanRoi ? "Cửa bên kia không có ai đặt"
                    : "Phiên #" + phienVanRoi, x + w / 2, y + 52, mFont.CENTER);
            if (thangVanRoi)
            {
                veHatSang(g, x + w / 2, y + 36, 8, 50, MAU_VANG, 0.7f, 1300);
            }
            veNutVang(g, oNutDongBao(), "ĐÓNG");
        }
    }
}
