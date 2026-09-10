using System;
using System.Collections.Generic;

namespace Game2.God
{
    /// <summary>
    /// Bảng chọn kiểu tàn sát — vẽ đè lên màn chơi, tự bắt chạm.
    /// </summary>
    /// <remarks>
    /// <para>Trước đây bấm <b>T</b>, gõ <c>ts</c> hay chọn "Tàn Sát" trong menu
    /// đều làm đúng một việc: bật/tắt một cái công tắc đánh <b>mọi</b> thứ trong
    /// tầm. Các lựa chọn tinh hơn — chỉ đánh vài loài, tránh siêu quái, có đánh
    /// người hay không — vẫn nằm trong mã (<c>Mobs.TypeMobsTanSat</c>,
    /// <c>Mobs.neSieuQuai</c>, <c>Mobs.tsPlayer</c>) nhưng <b>không có đường nào
    /// từ trong game chạm tới</b>. Bảng này là đường đó.</para>
    ///
    /// <para>Danh sách loài dựng từ <b>quái đang có trong bản đồ</b> chứ không
    /// phải một bảng cố định: mỗi bản đồ vài loài, liệt kê hết mọi loài trong
    /// game thì cuộn mãi không tới cái mình cần.</para>
    ///
    /// <para>Không chọn loài nào = <b>đánh tất cả</b>. Đó là cách
    /// <c>FilterMobTanSat</c> vẫn hiểu danh sách rỗng, nên giữ nguyên nghĩa ấy:
    /// người chỉ muốn bật rồi cày không phải chọn gì thêm.</para>
    /// </remarks>
    public class TanSatUI
    {
        private static TanSatUI instance;

        public static TanSatUI getInstance()
        {
            return instance ?? (instance = new TanSatUI());
        }

        public bool dangMo;

        private const int RONG = 258;
        private const int CAO = 246;

        private const int MAU_NEN = 0x332B22;
        private const int MAU_VIEN = 0xC8933C;
        private const int MAU_O = 0x4A4034;
        private const int MAU_O_BAT = 0x2E7D32;
        private const int MAU_O_TAT = 0x5A5A5A;

        /// <summary>Cao mỗi dòng trong danh sách loài.</summary>
        private const int CAO_DONG = 16;

        /// <summary>Số dòng loài thấy được cùng lúc.</summary>
        private const int SO_DONG_THAY = 6;

        private int x0, y0;
        private int cuon;

        /// <summary>Một loài quái đang có trong bản đồ.</summary>
        private sealed class Loai
        {
            public int templateId;
            public string ten;
            public int soCon;
            public bool sieuQuai;
        }

        private readonly List<Loai> loai = new List<Loai>();

        public void mo()
        {
            dangMo = true;
            cuon = 0;
            dungDanhSachLoai();
        }

        public void dong()
        {
            dangMo = false;
        }

        /// <summary>
        /// Quét bản đồ, gom quái theo loài.
        /// </summary>
        /// <remarks>
        /// Gọi lúc mở bảng chứ không mỗi khung hình: quét cả danh sách quái mỗi
        /// khung là việc thừa, mà loài trong một bản đồ thì không đổi.
        /// </remarks>
        private void dungDanhSachLoai()
        {
            loai.Clear();
            try
            {
                for (int i = 0; i < GameScr.vMob.size(); i++)
                {
                    Mob m = (Mob)GameScr.vMob.elementAt(i);
                    if (m == null || m.isMobMe)
                    {
                        continue;
                    }
                    Loai co = null;
                    for (int k = 0; k < loai.Count; k++)
                    {
                        if (loai[k].templateId == m.templateId)
                        {
                            co = loai[k];
                            break;
                        }
                    }
                    if (co == null)
                    {
                        co = new Loai();
                        co.templateId = m.templateId;
                        MobTemplate t = m.getTemplate();
                        co.ten = (t == null || t.name == null || t.name.Length == 0)
                                ? ("Quái " + m.templateId) : t.name;
                        loai.Add(co);
                    }
                    co.soCon++;
                    if (m.levelBoss != 0)
                    {
                        co.sieuQuai = true;
                    }
                }
            }
            catch (Exception)
            {
                // Ban do dang nap do dang thi danh sach quai co the doi giua
                // chung. De trong con hon nem loi ra giua ham ve.
            }
        }

        // ==================================================================
        //  Vẽ
        // ==================================================================

        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            x0 = GameCanvas.w / 2 - RONG / 2;
            y0 = GameCanvas.h / 2 - CAO / 2;

            g.setColor(0, 0.55f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
            g.setColor(MAU_NEN, 0.97f);
            g.fillRect(x0, y0, RONG, CAO, 8);
            g.setColor(MAU_VIEN);
            g.drawRect(x0, y0, RONG, CAO);

            mFont.tahoma_7b_yellow.drawString(g, "Tàn sát",
                    x0 + RONG / 2, y0 + 4, mFont.CENTER);
            veNut(g, x0 + RONG - 18, y0 + 3, 15, 12, "X", false);

            int y = y0 + 20;

            // ---- ba công tắc chính ----
            veNut(g, x0 + 6, y, RONG - 12, 15,
                    Mobs.IsTanSat ? "ĐANG TÀN SÁT — bấm để tắt" : "Bật tàn sát",
                    Mobs.IsTanSat);
            y += 19;

            int nua = (RONG - 18) / 2;
            veNut(g, x0 + 6, y, nua, 15,
                    Mobs.neSieuQuai ? "Né siêu quái: BẬT" : "Né siêu quái: tắt",
                    Mobs.neSieuQuai);
            veNut(g, x0 + 12 + nua, y, nua, 15,
                    Mobs.tsPlayer ? "Đánh người: BẬT" : "Đánh người: tắt",
                    Mobs.tsPlayer);
            y += 21;

            // ---- nhãn danh sách ----
            string nhan = (Mobs.TypeMobsTanSat.Count == 0)
                    ? "Loài đánh: TẤT CẢ quái trong bản đồ"
                    : ("Loài đánh: đã chọn " + Mobs.TypeMobsTanSat.Count);
            mFont.tahoma_7_white.drawString(g, nhan, x0 + 6, y, mFont.LEFT);
            y += 12;

            // ---- danh sách loài ----
            int yDs = y;
            int caoDs = SO_DONG_THAY * CAO_DONG;
            g.setColor(MAU_O, 0.9f);
            g.fillRect(x0 + 6, yDs, RONG - 12, caoDs, 4);

            if (loai.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Bản đồ này không có quái nào",
                        x0 + RONG / 2, yDs + caoDs / 2 - 6, mFont.CENTER);
            }
            else
            {
                int het = Math.min(loai.Count, cuon + SO_DONG_THAY);
                for (int i = cuon; i < het; i++)
                {
                    Loai l = loai[i];
                    int yd = yDs + (i - cuon) * CAO_DONG;
                    bool chon = Mobs.TypeMobsTanSat.Contains(l.templateId);

                    // O tich
                    g.setColor(chon ? MAU_O_BAT : MAU_O_TAT, 1f);
                    g.fillRect(x0 + 10, yd + 3, 10, 10, 2);
                    if (chon)
                    {
                        mFont.tahoma_7b_white.drawString(g, "v",
                                x0 + 15, yd + 1, mFont.CENTER);
                    }

                    string chu = l.ten + " (" + l.soCon + ")"
                            + (l.sieuQuai ? " [siêu]" : "");
                    (chon ? mFont.tahoma_7b_white : mFont.tahoma_7_white)
                            .drawString(g, catBot(chu, 30), x0 + 26, yd + 2, mFont.LEFT);
                }
            }
            y = yDs + caoDs + 4;

            // ---- nút cuộn, chỉ hiện khi có gì để cuộn ----
            if (loai.Count > SO_DONG_THAY)
            {
                veNut(g, x0 + 6, y, 40, 14, "Lên", false);
                veNut(g, x0 + 50, y, 40, 14, "Xuống", false);
                mFont.tahoma_7_grey.drawString(g,
                        (cuon + 1) + "-" + Math.min(loai.Count, cuon + SO_DONG_THAY)
                        + "/" + loai.Count, x0 + 96, y + 2, mFont.LEFT);
            }
            y += 18;

            // ---- hai nút chọn nhanh ----
            veNut(g, x0 + 6, y, nua, 15, "Chọn tất cả", false);
            veNut(g, x0 + 12 + nua, y, nua, 15, "Bỏ chọn (đánh hết)", false);
        }

        private static string catBot(string s, int toiDa)
        {
            if (s == null)
            {
                return "";
            }
            return s.Length <= toiDa ? s : (s.Substring(0, toiDa - 1) + "…");
        }

        private static void veNut(mGraphics g, int x, int y, int w, int h,
                string chu, bool bat)
        {
            g.setColor(bat ? MAU_O_BAT : MAU_O, 1f);
            g.fillRect(x, y, w, h, 3);
            g.setColor(MAU_VIEN, 0.7f);
            g.drawRect(x, y, w, h);
            mFont.tahoma_7_white.drawString(g, chu, x + w / 2, y + 2, mFont.CENTER);
        }

        // ==================================================================
        //  Chạm
        // ==================================================================

        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            // Nuot MOI cham khi dang mo, ke ca cham ra ngoai khung: bam xuyen
            // qua hop ma nhan vat chay di thi rat kho chiu.
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }

            if (cham(x0 + RONG - 18, y0 + 3, 15, 12))
            {
                dong();
                return true;
            }

            int y = y0 + 20;

            if (cham(x0 + 6, y, RONG - 12, 15))
            {
                Mobs.IsTanSat = !Mobs.IsTanSat;
                return true;
            }
            y += 19;

            int nua = (RONG - 18) / 2;
            if (cham(x0 + 6, y, nua, 15))
            {
                Mobs.neSieuQuai = !Mobs.neSieuQuai;
                return true;
            }
            if (cham(x0 + 12 + nua, y, nua, 15))
            {
                Mobs.tsPlayer = !Mobs.tsPlayer;
                return true;
            }
            y += 21;
            y += 12;   // qua nhãn

            int yDs = y;
            int caoDs = SO_DONG_THAY * CAO_DONG;
            int het = Math.min(loai.Count, cuon + SO_DONG_THAY);
            for (int i = cuon; i < het; i++)
            {
                int yd = yDs + (i - cuon) * CAO_DONG;
                if (cham(x0 + 6, yd, RONG - 12, CAO_DONG))
                {
                    doiChon(loai[i].templateId);
                    return true;
                }
            }
            y = yDs + caoDs + 4;

            if (loai.Count > SO_DONG_THAY)
            {
                if (cham(x0 + 6, y, 40, 14))
                {
                    cuon = Math.max(0, cuon - 1);
                    return true;
                }
                if (cham(x0 + 50, y, 40, 14))
                {
                    cuon = Math.min(loai.Count - SO_DONG_THAY, cuon + 1);
                    return true;
                }
            }
            y += 18;

            if (cham(x0 + 6, y, nua, 15))
            {
                Mobs.TypeMobsTanSat.Clear();
                for (int i = 0; i < loai.Count; i++)
                {
                    Mobs.TypeMobsTanSat.Add(loai[i].templateId);
                }
                return true;
            }
            if (cham(x0 + 12 + nua, y, nua, 15))
            {
                // Danh sach rong = danh TAT CA. Xem chu thich dau lop.
                Mobs.TypeMobsTanSat.Clear();
                return true;
            }
            return true;
        }

        private static void doiChon(int templateId)
        {
            if (Mobs.TypeMobsTanSat.Contains(templateId))
            {
                Mobs.TypeMobsTanSat.Remove(templateId);
            }
            else
            {
                Mobs.TypeMobsTanSat.Add(templateId);
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
